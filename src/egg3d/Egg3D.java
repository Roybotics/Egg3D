package egg3d;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.CopyOnWriteArraySet;

import jssc.SerialPortException;
import serialcom.Serial;
import serialcom.SerialInterface;
import serialcom.SerialListener;

public class Egg3D implements Closeable
{
	private static final String cDefaultWindowsComPort = "COM14";

	static final int cMessageLength = 18;

	Serial mSerial;
	ByteBuffer mMessageReceivedByteBuffer;

	final CopyOnWriteArraySet<Egg3DListener> mEgg3DListenerList = new CopyOnWriteArraySet<Egg3DListener>();

	public Egg3D()
	{
		super();
		mMessageReceivedByteBuffer = ByteBuffer.allocate(cMessageLength)
																						.order(ByteOrder.LITTLE_ENDIAN);
		mSerial = new Serial("Egg3D", 115200);

		mSerial.setBinaryMode(true);
		mSerial.setMessageLength(cMessageLength);

		mSerial.addListener(new SerialListener()
		{

			@Override
			public void textMessageReceived(final SerialInterface pSerial,
																			final String pMessage)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void errorOccured(	final Serial pSerial,
																final Throwable pException)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void binaryMessageReceived(final SerialInterface pSerial,
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

	private float convert(final short s)
	{
		return (((float) s) / 16384.0f);
	}

	private float convertButton(final byte b)
	{
		return (int) (b & 0xFF);
	}

	public boolean connect() throws SerialPortException
	{
		try
		{
			final String lOSName = System.getProperty("os.name")
																		.toLowerCase();
			if (lOSName.contains("mac"))
				return mSerial.connect("/dev/tty.Egg3D-DevB");
			else if (lOSName.contains("win"))
				return mSerial.connect(cDefaultWindowsComPort);
			else
				return mSerial.connect();
		}
		catch (final Throwable e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public void addEgg3DListener(final Egg3DListener pEgg3DListener)
	{
		mEgg3DListenerList.add(pEgg3DListener);
	}

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

}
