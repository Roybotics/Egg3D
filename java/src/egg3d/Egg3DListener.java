package egg3d;

/**
 * Interface Egg3DListener
 * 
 * Classes implementing this interface can receive Egg3D events in the form of a
 * quaternion (w,x,y,z), an acceleration vector (ax,ay,az) and the 3 button
 * states.
 *
 * @author Loic Royer 2014
 *
 */
public interface Egg3DListener
{

	/**
	 * Egg3D event consisting of a quaternion (w,x,y,z), an acceleration vector
	 * (ax,ay,az) and the 3 button states.
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
	void update(final float pQuatW,
							final float pQuatX,
							final float pQuatY,
							final float pQuatZ,
							final float pAccX,
							final float pAccY,
							final float pAccZ,
							final float pButton1,
							final float pButton2,
							final float pButton3);

}
