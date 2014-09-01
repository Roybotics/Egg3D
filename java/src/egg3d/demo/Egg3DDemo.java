package egg3d.demo;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import egg3d.Egg3D;
import egg3d.Egg3DListener;

/**
 * Class Egg3DDemo
 * 
 * Simple demo on how to instantiate and use the Egg3D class and received Egg3D
 * events. make sure your Egg3D is switched on, paired (bluetooth) and that on
 * windows you have the '.egg3d.cong.txt' at the root of your home (user) folder
 * in which the property port is set e.g. "port = COM1" if you know that your
 * Egg3D is connected on port COM1.
 *
 * @author Loic Royer 2014
 *
 */
public class Egg3DDemo
{

	protected volatile boolean sQuitDemo = false;

	/**
	 * Egg3D events are displayed as they come. if you shake your Egg3D strong
	 * enough you will quit this demo.
	 */
	@Test
	public void demo()
	{
		try
		{
			final Egg3D lEgg3D = new Egg3D();
			System.out.println("Connecting to Egg3d...");
			assertTrue(lEgg3D.connect());
			System.out.println("Connected!");

			lEgg3D.addEgg3DListener(new Egg3DListener()
			{

				@Override
				public void update(	final float pQuatW,
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
					float lAbsoluteAcceleration = Math.abs(pAccX) + Math.abs(pAccY) + Math.abs(pAccZ);
					if (lAbsoluteAcceleration > 0.5)
					{
						System.out.println("SHAKE (aa=" + lAbsoluteAcceleration
																+ ")");
					}

						System.out.format(" q = (%g,%g,%g,%g) ; a = (%g,%g,%g ; b = (%f,%f,%f) \n",
															pQuatW,
															pQuatX,
															pQuatY,
															pQuatZ,
															pAccX,
															pAccY,
															pAccZ,
															pButton1,
															pButton2,
															pButton3);

					if (lAbsoluteAcceleration > 1.5)
					{
						sQuitDemo = true;
						System.out.println("Quitting!!");
					}

				}

			});

			while (!sQuitDemo)
			{
				Thread.sleep(100);
			}

			lEgg3D.close();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			fail();
		}
	}
}
