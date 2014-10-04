package egg3d;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArraySet;

import jssc.SerialPortException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;

import egg3d.serial.Serial;
import egg3d.serial.SerialComException;
import egg3d.serial.SerialListener;

/**
 * Class Egg3D
 * 
 * Main class for connectivity to the Egg3D device
 *
 * @author Loic Royer 2014
 *
 */
public class Egg3D implements AutoCloseable
{

	static final int cMessageLength = 18;

	Serial mSerial;
	ByteBuffer mMessageReceivedByteBuffer;

	final CopyOnWriteArraySet<Egg3DListener> mEgg3DListenerList = new CopyOnWriteArraySet<Egg3DListener>();

	/**
	 * Constructs an instance of the Egg3D class.
	 */
	public Egg3D()
	{
		super();
		mMessageReceivedByteBuffer = ByteBuffer.allocate(cMessageLength)
																						.order(ByteOrder.LITTLE_ENDIAN);
		mSerial = new Serial("Egg3D", 115200);

		mSerial.setBinaryMode(true);
		mSerial.setMessageLength(cMessageLength);
		mSerial.setLineTerminationCharacter('\n');

		mSerial.addListener(new SerialListener()
		{

			@Override
			public void textMessageReceived(final Serial pSerial,
																			final String pMessage)
			{
				System.out.println(pMessage);
			}

			@Override
			public void errorOccured(	final Serial pSerial,
																final Throwable pException)
			{
				System.err.println(pException.getLocalizedMessage());
			}

			@Override
			public void binaryMessageReceived(final Serial pSerial,
																				final byte[] pMessage)
			{
				mMessageReceivedByteBuffer.clear();
				mMessageReceivedByteBuffer.put(pMessage);
				mMessageReceivedByteBuffer.rewind();
				final float lQuatW = convert(mMessageReceivedByteBuffer.getShort());
				final float lQuatX = convert(mMessageReceivedByteBuffer.getShort());
				final float lQuatY = convert(mMessageReceivedByteBuffer.getShort());
				final float lQuatZ = convert(mMessageReceivedByteBuffer.getShort());
				final float lAccX = convert(mMessageReceivedByteBuffer.getShort());
				final float lAccY = convert(mMessageReceivedByteBuffer.getShort());
				final float lAccZ = convert(mMessageReceivedByteBuffer.getShort());
				final float lButton1 = convertButton(mMessageReceivedByteBuffer.get());
				final float lButton2 = convertButton(mMessageReceivedByteBuffer.get());
				final float lButton3 = convertButton(mMessageReceivedByteBuffer.get());

				notifyListeners(lQuatW,
												lQuatX,
												lQuatY,
												lQuatZ,
												lAccX,
												lAccY,
												lAccZ,
												lButton1,
												lButton2,
												lButton3);

			}

		});
	}

	/**
	 * Connects to the Egg3D device using default port information.
	 * 
	 * @return
	 * @throws SerialPortException
	 *           when error while connecting to serial port
	 */
	public boolean connect() throws SerialPortException
	{
		try
		{
			if (SystemUtils.IS_OS_MAC_OSX)
				return mSerial.connect("/dev/tty.Egg3D-DevB");
			else if (SystemUtils.IS_OS_WINDOWS)
			{
				File lUserDirectory = FileUtils.getUserDirectory();
				File lConfigurationFile = new File(	lUserDirectory,
																						".egg3d.conf.txt");
				Properties lProperties = new Properties();
				lProperties.load(new FileInputStream(lConfigurationFile));

				String lPort = lProperties.getProperty("port");

				return connect(lPort);
			}
			else
				return connect(null);
		}
		catch (final Throwable e)
		{
			return false;
		}
	}

	/**
	 * Connects to the Egg3D device bound to a given port.
	 * 
	 * @param pPortName
	 * @return
	 * @throws SerialPortException
	 * @throws SerialComException
	 */
	public boolean connect(String pPortName) throws SerialPortException,
																					SerialComException
	{
		if (pPortName == null)
			mSerial.connect();
		return mSerial.connect(pPortName);
	}

	/**
	 * Adds Egg3D listener.
	 * 
	 * @param pEgg3DListener
	 *          class implementing Egg3DListener
	 */
	public void addEgg3DListener(final Egg3DListener pEgg3DListener)
	{
		mEgg3DListenerList.add(pEgg3DListener);
	}

	/**
	 * Notifies listeners of Egg3D events
	 * 
	 * @param pQuatW
	 * @param pQuatX
	 * @param pQuatY
	 * @param pQuatZ
	 * @param pAccX
	 * @param pAccY
	 * @param pAccZ
	 * @param pButton1
	 * @param pButton2
	 * @param pButton3
	 */
	private void notifyListeners(	final float pQuatW,
																final float pQuatX,
																final float pQuatY,
																final float pQuatZ,
																final float pAccX,
																final float pAccY,
																final float pAccZ,
																final float pButton1,
																final float pButton2,
																final float pButton3)
	{
		for (final Egg3DListener lEgg3DListener : mEgg3DListenerList)
		{
			lEgg3DListener.update(pQuatW,
														pQuatX,
														pQuatY,
														pQuatZ,
														pAccX,
														pAccY,
														pAccZ,
														pButton1,
														pButton2,
														pButton3);
		}

	}

	/**
	 * Closes connection to Egg3d device.
	 * 
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close()
	{
		try
		{
			mSerial.close();
		}
		catch (final SerialPortException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Converts short value to float
	 * 
	 * @param pShortValue
	 *          value to convert
	 * @return
	 */
	private float convert(final short pShortValue)
	{
		return ((pShortValue) / 16384.0f);
	}

	/**
	 * Converts button value from byte to normalised float between 0 and 1
	 * 
	 * @param pByteValue
	 *          value to convert
	 * @return
	 */
	private float convertButton(final byte pByteValue)
	{
		return ((pByteValue & 0xFF));
	}

	/**
	 * Interface method implementation
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "Egg3D [mSerial=" + mSerial
						+ ", mEgg3DListenerList="
						+ mEgg3DListenerList
						+ "]";
	}

}
