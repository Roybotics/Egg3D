package egg3d.serial;

import gnu.trove.list.array.TByteArrayList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class Serial
{
	public final static int cFLOWCONTROL_NONE = SerialPort.FLOWCONTROL_NONE;
	public final static int cFLOWCONTROL_RTSCTS = SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT;
	public final static int cFLOWCONTROL_XONXOFF = SerialPort.FLOWCONTROL_XONXOFF_IN | SerialPort.FLOWCONTROL_XONXOFF_OUT;
	private static final int cReadTimeOutInMilliseconds = 10 * 1000;

	private final String mPortNameHint;
	private final int mBaudRate;
	private Character mEndOfMessageCharacter = null;
	private int mFlowControl = cFLOWCONTROL_NONE;
	private boolean mEcho = false;
	private final int mConnectionTimeOutInMs = 2000;
	private volatile boolean mBinaryMode = false;
	private volatile int mMessageLength = 1;
	private volatile boolean mNotifyEvents = true;

	private final CopyOnWriteArrayList<SerialListener> mListenerList = new CopyOnWriteArrayList<SerialListener>();

	private SerialPort mSerialPort;

	private volatile boolean mIsMessageReceived;
	private final TByteArrayList mBuffer = new TByteArrayList(1024);
	private volatile String mTextMessageReceived;
	private volatile byte[] mBinaryMessageReceived;

	/**
	 * Constructs an instance of the Serial class given a port name hint and a
	 * baudrate
	 * 
	 * @param pPortNameHint
	 *          port name hint
	 * @param pBaudRate
	 *          baud rate
	 */
	public Serial(final String pPortNameHint, final int pBaudRate)
	{
		mPortNameHint = pPortNameHint;
		mBaudRate = pBaudRate;
	}

	/**
	 * Constructs an instance of the Serial class
	 * 
	 * @param pBaudRate
	 *          baud rate
	 */
	public Serial(final int pBaudRate)
	{
		mPortNameHint = null;
		mBaudRate = pBaudRate;
	}

	/**
	 * Returns the list of all serial ports available
	 * 
	 * @return list of all serial ports available
	 */
	public static ArrayList<String> getListOfAllSerialCommPorts()
	{
		final ArrayList<String> lListOfFreeCommPorts = new ArrayList<String>();
		final String[] lPortNameList = SerialPortList.getPortNames(	Pattern.compile("tty\\..+"),
																																new Comparator<String>()
																																{
																																	@Override
																																	public int compare(	final String pO1,
																																											final String pO2)
																																	{
																																		return -1;
																																	}
																																});

		for (final String lPortName : lPortNameList)
		{
			lListOfFreeCommPorts.add(lPortName);
		}
		return lListOfFreeCommPorts;
	}

	/**
	 * Returns the list of all serial ports containing a specific substring hint
	 * 
	 * @param pNameHint
	 *          substring hint
	 * @return list of serial ports names
	 */
	public static ArrayList<String> getListOfAllSerialCommPortsWithNameContaining(final String pNameHint)
	{
		final ArrayList<String> lListOfFreeCommPorts = getListOfAllSerialCommPorts();

		final ArrayList<String> lListOfSelectedCommPorts = new ArrayList<String>();
		for (final String lPortName : lListOfFreeCommPorts)
		{
			if (lPortName.contains(pNameHint))
				lListOfSelectedCommPorts.add(lPortName);
		}
		return lListOfSelectedCommPorts;
	}

	/**
	 * Returns the first port name containing a given substraing hint.
	 * 
	 * @param pNameHint
	 *          substring hint
	 * @return serial port anme
	 */
	public static String getOneSerialCommPortWithNameContaining(final String pNameHint)
	{
		final ArrayList<String> lListOfAllSerialCommPortsWithNameContaining = getListOfAllSerialCommPortsWithNameContaining(pNameHint);
		if (lListOfAllSerialCommPortsWithNameContaining.size() > 0)
			return lListOfAllSerialCommPortsWithNameContaining.get(0);
		else
			return null;
	}

	/**
	 * Connects to serial device.
	 * 
	 * @return true if connected, false if not
	 * @throws SerialPortException
	 *           exception
	 * @throws SerialComException
	 *           exception
	 */
	public final boolean connect() throws SerialPortException,
																SerialComException
	{
		if (mPortNameHint == null)
			throw new SerialComException("No hint given for port name.");
		final String lPortName = getOneSerialCommPortWithNameContaining(mPortNameHint);
		System.out.format("Connecting to '%s'\n", lPortName);
		return connect(lPortName);
	}

	/**
	 * Connects to a given port name
	 * 
	 * @param pPortName
	 *          serial pot name
	 * @return true if connected
	 * @throws SerialPortException
	 *           exception
	 */
	public final boolean connect(final String pPortName) throws SerialPortException
	{
		if (pPortName != null)
		{
			mSerialPort = new SerialPort(pPortName);

			mSerialPort.openPort();
			mSerialPort.setParams(mBaudRate,
														SerialPort.DATABITS_8,
														SerialPort.STOPBITS_1,
														SerialPort.PARITY_NONE);

			mSerialPort.setFlowControlMode(mFlowControl);

			// System.out.println("Flow Control: " +
			// mSerialPort.getFlowControlMode());

			if (mNotifyEvents)
				mSerialPort.addEventListener(new Serial.SerialReaderEventBased(mSerialPort));
			return true;
		}

		return false;
	}

	/**
	 * Adds SerialListener listener to this Serial object.
	 * 
	 * @param pSerialListener
	 *          listener to add.
	 */
	public final void addListener(final SerialListener pSerialListener)
	{
		mListenerList.add(pSerialListener);
	}

	/**
	 * Sends a string to connected serial device.
	 * 
	 * @param pString
	 *          string to send
	 * @throws SerialPortException
	 *           exception
	 */
	public final void write(final String pString) throws SerialPortException
	{
		mSerialPort.writeBytes(pString.getBytes());
	}

	/**
	 * Sends an array of byts to connected device.
	 * 
	 * @param pBytes
	 *          bytes to send
	 * @throws SerialPortException
	 *           exception
	 */
	public final void write(final byte[] pBytes) throws SerialPortException
	{
		mSerialPort.writeBytes(pBytes);
	}

	/**
	 * Sends a single byte to connected device.
	 * 
	 * @param pByte
	 *          byte to send
	 * @throws SerialPortException
	 *           exception
	 */
	public final void write(final byte pByte) throws SerialPortException
	{
		mSerialPort.writeByte(pByte);
	}

	/**
	 * Purges serial port.
	 * 
	 * @throws SerialPortException
	 *           exception
	 */
	public final void purge() throws SerialPortException
	{
		mSerialPort.purgePort(SerialPort.PURGE_TXCLEAR | SerialPort.PURGE_RXCLEAR);
	}

	/**
	 * Notifies listeners that a text message has been received from connected
	 * device.
	 * 
	 * @param pMessage
	 *          text message received
	 */
	private final void textMessageReceived(final String pMessage)
	{
		mTextMessageReceived = pMessage;
		mIsMessageReceived = true;

		for (final SerialListener lSerialListener : mListenerList)
		{
			lSerialListener.textMessageReceived(this, pMessage);
		}
	}

	/**
	 * Notifies listeners that a binary message has been received from connected
	 * device.
	 * 
	 * @param pMessage
	 *          message received.
	 */
	private final void binaryMessageReceived(final byte[] pMessage)
	{
		mBinaryMessageReceived = pMessage;
		mIsMessageReceived = true;

		for (final SerialListener lSerialListener : mListenerList)
		{
			lSerialListener.binaryMessageReceived(this, pMessage);
		}
	}

	/**
	 * Notifies listeners that an error has occurred.
	 * 
	 * @param pException
	 *          exception that occured.
	 */
	private final void errorOccured(final Throwable pException)
	{
		for (final SerialListener lSerialListener : mListenerList)
		{
			lSerialListener.errorOccured(this, pException);
		}
	}

	/**
	 * Sets the echo flag.
	 * 
	 * @param echo
	 *          echo flag state.
	 */
	public final void setEcho(final boolean echo)
	{
		this.mEcho = echo;
	}

	/**
	 * Returns the state of the echo flag.
	 * 
	 * @return echo flag state
	 */
	public final boolean isEcho()
	{
		return mEcho;
	}

	/**
	 * Sets flow control flags.
	 * 
	 * @param flowControl
	 *          flow control flags.
	 */
	public final void setFlowControl(final int flowControl)
	{
		mFlowControl = flowControl;
	}

	/**
	 * Returns the state of the flow control flags.
	 * 
	 * @return flow control state
	 */
	public final int getFlowControl()
	{
		return mFlowControl;
	}

	/**
	 * Sets line termination character.
	 * 
	 * @param pChar
	 *          line termination character.
	 */
	public void setLineTerminationCharacter(final char pChar)
	{
		mEndOfMessageCharacter = pChar;
	}

	/**
	 * Returns the line termination character.
	 * 
	 * @return line termination character.
	 */
	public Character getMessageTerminationCharacter()
	{
		return mEndOfMessageCharacter;
	}

	/**
	 * Sets the binary mode flag.
	 * 
	 * @param pBinaryMode
	 *          binary mode flag state.
	 */
	public void setBinaryMode(final boolean pBinaryMode)
	{
		mBinaryMode = pBinaryMode;
	}

	/**
	 * Returns the binary mode flag.
	 * 
	 * @return binary mode flag state.
	 */
	public boolean isBinaryMode()
	{
		return mBinaryMode;
	}

	/**
	 * Sets the message length.
	 * 
	 * @param pMessageLength
	 *          message length
	 */
	public void setMessageLength(final int pMessageLength)
	{
		mMessageLength = pMessageLength;
	}

	/**
	 * Returns the message length.
	 * 
	 * @return message length.
	 */
	public int getMessageLength()
	{
		return mMessageLength;
	}

	/**
	 * Returns the state of the notify events flag.
	 * 
	 * @return notify-events flag state
	 */
	public boolean isNotifyEvents()
	{
		return mNotifyEvents;
	}

	/**
	 * Sets the notify-events flag state
	 * 
	 * @param notifyEvents
	 *          notify-events flag state
	 */
	public void setNotifyEvents(final boolean notifyEvents)
	{
		mNotifyEvents = notifyEvents;
	}

	/**
	 * Returns true if a connection is established.
	 * 
	 * @return true if connected.
	 */
	public boolean isConnected()
	{
		return mSerialPort.isOpened();
	}

	/**
	 * Class SerialReaderEventBased
	 * 
	 * Instances of this class ...
	 *
	 * @author Loic Royer 2014
	 *
	 */
	public final class SerialReaderEventBased	implements
																						SerialPortEventListener
	{
		public SerialReaderEventBased(final SerialPort pSerialPort)
		{
		}

		@Override
		public void serialEvent(final SerialPortEvent event)
		{
			if (event.getEventType() == SerialPortEvent.RXCHAR)
			{
				if (mBinaryMode)
				{
					final byte[] lMessage = readBinaryMessage();
					final String lMessageString = new String(lMessage);
					if (lMessage != null)
					{
						binaryMessageReceived(lMessage);
					}
				}
				else
				{
					final String lMessage = readTextMessageAsString();
					if (lMessage != null)
					{
						textMessageReceived(lMessage);
					}
				}
			}
			else if (event.getEventType() == SerialPortEvent.ERR)
			{
				System.err.println(this.getClass().getSimpleName() + ": Serial connection error!");
			}
			else if (event.getEventType() == SerialPortEvent.BREAK)
			{
				System.out.println(this.getClass().getSimpleName() + ": Serial connection broken!");
			}
		}
	}

	/**
	 * Closes the connection.
	 * 
	 * @throws SerialPortException
	 *           exception
	 */
	public final void close() throws SerialPortException
	{
		if (mSerialPort != null)
		{
			try
			{
				mSerialPort.removeEventListener();
			}
			catch (final Throwable e)
			{
			}
			if (mSerialPort.isOpened())
				mSerialPort.closePort();
			mSerialPort = null;
		}
	}

	/**
	 * Waits for an answer to arrive from the serial port.
	 * 
	 * @param pWaitTime
	 *          wait time in milliseconds.
	 * @return answer
	 */
	public String waitForAnswer(final int pWaitTime)
	{
		while (!mIsMessageReceived)
		{
			try
			{
				Thread.sleep(pWaitTime);
			}
			catch (final InterruptedException e)
			{
			}
		}
		mIsMessageReceived = false;
		return mTextMessageReceived;
	}

	/**
	 * Waits for at most 10 seconds for a binary message to arrive and returns it.
	 * 
	 * @return binary message received.
	 */
	public byte[] readBinaryMessage()
	{
		return readBinaryMessage(cReadTimeOutInMilliseconds);
	}

	/**
	 * Waits for at most pTimeOutInMilliseconds milliseocnds for a binary message
	 * to arrive and returns it.
	 * 
	 * @param pTimeOutInMilliseconds
	 *          timout in ms
	 * @return binary message
	 */
	public byte[] readBinaryMessage(final int pTimeOutInMilliseconds)
	{
		try
		{
			if (mEndOfMessageCharacter != null)
			{
				byte[] lByte;
				do
				{
					lByte = mSerialPort.readBytes(1, pTimeOutInMilliseconds);
				}
				while (lByte[0] != mEndOfMessageCharacter);
			}
			final byte[] lReadBytes = mSerialPort.readBytes(mMessageLength,
																											pTimeOutInMilliseconds);
			return lReadBytes;
		}
		catch (final Throwable e)
		{
			errorOccured(e);
			return null;
		}
	}

	/**
	 * Waits for a complete text message (with line terminating character) to
	 * arrive and returns it as a byte array.
	 * 
	 * @return received text message as byte array
	 */
	public byte[] readTextMessage()
	{

		int data;
		try
		{
			mBuffer.clear();
			while ((data = mSerialPort.readBytes(1)[0]) != mEndOfMessageCharacter.charValue())
			{
				mBuffer.add((byte) data);
			}

			if (mEcho)
			{
				final String lMessage = new String(	mBuffer.toArray(),
																						0,
																						mBuffer.size());
				System.out.print(lMessage);
			}

			return mBuffer.toArray();

		}
		catch (final Throwable e)
		{
			errorOccured(e);
			return null;
		}

	}

	/**
	 * Waits for a complete text message (with line terminating character) to
	 * arrive and returns it as a String.
	 * 
	 * @return received text message as a String
	 */
	public String readTextMessageAsString()
	{
		final byte[] lReadTextMessage = readTextMessage();
		final String lMessage = new String(	lReadTextMessage,
																				0,
																				lReadTextMessage.length);
		return lMessage;
	}

}
