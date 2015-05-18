package egg3d.serial.demo;

import jssc.SerialPortException;

import org.junit.Test;

import egg3d.serial.Serial;
import egg3d.serial.SerialComException;
import egg3d.serial.SerialListenerAdapter;

public class SerialDemo
{

	@Test
	public void test() throws InterruptedException,
										SerialPortException,
										SerialComException
	{

		final Serial lSerial = new Serial("Egg3D", 115200);
		lSerial.setBinaryMode(true);
		lSerial.setMessageLength(18);

		lSerial.addListener(new SerialListenerAdapter()
		{

			@Override
			public void binaryMessageReceived(final Serial pSerial,
																				final byte[] pMessage)
			{
				System.out.format("message received length=%d, index=%d \n",
													pMessage.length,
													pMessage[0]);
			}
		});

		System.out.println("Connecting...");
		lSerial.connect();

		Thread.sleep(1000000);
		lSerial.close();
	}
}
