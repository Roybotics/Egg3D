package egg3d.serial;

/**
 * Interface SerialListener
 * 
 * Instances of this class can receive serial communication events
 *
 * @author Loic Royer 2014
 *
 */
public interface SerialListener
{

	/**
	 * Called when a complete text message has been received.
	 * 
	 * @param pSerial
	 *          Serial object from which message originates
	 * @param pMessage
	 *          text message received
	 */
	void textMessageReceived(Serial pSerial, String pMessage);

	/**
	 * Called when a complete binary message has been received.
	 * 
	 * @param pSerial
	 *          Serial object from which message originates
	 * @param pMessage
	 *          binary message received
	 */
	void binaryMessageReceived(Serial pSerial, byte[] pMessage);

	/**
	 * @param pSerial
	 *          Serial object from which message originates
	 * @param pException
	 *          Exception that occurred.
	 */
	void errorOccured(Serial pSerial, Throwable pException);

}
