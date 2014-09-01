package egg3d.serial;


/**
 * Class SerialListenerAdapter
 * 
 * Adapter class for SerialListener interface implementors.
 *
 * @author Loic Royer 2014
 *
 */
public class SerialListenerAdapter implements SerialListener
{

	/**
	 * Interface method implementation
	 * 
	 * @see egg3d.serial.SerialListener#textMessageReceived(egg3d.serial.Serial,
	 *      java.lang.String)
	 */
	@Override
	public void textMessageReceived(final Serial pSerial,
																	final String pMessage)
	{
	}

	/**
	 * Interface method implementation
	 * 
	 * @see egg3d.serial.SerialListener#binaryMessageReceived(egg3d.serial.Serial,
	 *      byte[])
	 */
	@Override
	public void binaryMessageReceived(final Serial pSerial,
																		final byte[] pMessage)
	{
	}

	/**
	 * Interface method implementation
	 * 
	 * @see egg3d.serial.SerialListener#errorOccured(egg3d.serial.Serial,
	 *      java.lang.Throwable)
	 */
	@Override
	public void errorOccured(	final Serial pSerial,
														final Throwable pException)
	{
		System.out.format("%s\n", pSerial.toString());
		pException.printStackTrace();
	}
}
