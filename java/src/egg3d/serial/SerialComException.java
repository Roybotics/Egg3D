package egg3d.serial;

/**
 * Class SerialComException
 * 
 * Exceptions occuring during Serial communications.
 *
 * @author Loic Royer 2014
 *
 */
public class SerialComException extends Exception
{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an instance of the SerialComException class
	 * 
	 * @param pString
	 */
	public SerialComException(final String pString)
	{
		super(pString);
	}

}
