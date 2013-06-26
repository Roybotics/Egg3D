package egg3d.test;

import static org.junit.Assert.assertTrue;
import jssc.SerialPortException;

import org.junit.Test;

import egg3d.Egg3D;
import egg3d.Egg3DListener;

public class Egg3DTests
{

	@Test
	public void test() throws SerialPortException, InterruptedException
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
			}

		});

		while (true)
		{
			Thread.sleep(100);
		}

		// lEgg3D.close();
	}
}
