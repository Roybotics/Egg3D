package egg3d.server;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

import javax.swing.ImageIcon;

import jssc.SerialPortException;
import egg3d.Egg3D;
import egg3d.Egg3DListener;

public class TcpServer implements Egg3DListener
{

	private static DataOutputStream sOutToClient;
	private static BufferedReader sInFromClient;
	private static Socket sConnectionSocket;
	private static boolean mOn = true;
	private static MenuItem sInfoItem;

	public static void main(final String argv[]) throws Exception
	{
		setUpSysTray();

		startEgg3dThread();

		final ServerSocket lWelcomeSocket = new ServerSocket(4444);
		while (mOn)
		{
			System.out.println("Waiting for tcp connection...");
			sConnectionSocket = lWelcomeSocket.accept();
			System.out.println("Connection received from: " + sConnectionSocket.getInetAddress());
			sInFromClient = new BufferedReader(new InputStreamReader(sConnectionSocket.getInputStream()));
			sOutToClient = new DataOutputStream(sConnectionSocket.getOutputStream());
			sInfoItem.setLabel("client connected");
		}
		lWelcomeSocket.close();

		/**/
	}

	private static void startEgg3dThread() throws SerialPortException
	{
		final Thread lThread = new Thread()
		{
			@Override
			public void run()
			{
				Egg3D lEgg3D = null;
				try
				{
					super.run();
					lEgg3D = new Egg3D();
					lEgg3D.addEgg3DListener(new TcpServer());
					System.out.println("Connecting to Egg3D...");
					if (lEgg3D.connect())
						System.out.println("Connected to Egg3D!");

					while (mOn)
					{
						Thread.sleep(100);
					}
				}
				catch (final SerialPortException e)
				{
					e.printStackTrace();
				}
				catch (final InterruptedException e)
				{
					e.printStackTrace();
				}

				if (lEgg3D != null)
					lEgg3D.close();
			}
		};

		lThread.start();

	}

	private static void setUpSysTray()
	{
		final PopupMenu popup = new PopupMenu();
		final TrayIcon trayIcon = new TrayIcon(createImage(	"icon/Egg.256x256.alpha.png",
																												"tray icon"));
		final SystemTray tray = SystemTray.getSystemTray();

		// Create a pop-up menu components
		final MenuItem lEgg3DItem = new MenuItem("Egg3D");
		sInfoItem = new MenuItem("...");
		final MenuItem lExitItem = new MenuItem("Exit");
		lExitItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent pE)
			{
				mOn = false;
				try
				{
					Thread.sleep(500);
				}
				catch (final InterruptedException e)
				{
					e.printStackTrace();
				}
				System.exit(0);
			}
		});

		// Add components to pop-up menu

		popup.add(lEgg3DItem);
		popup.addSeparator();
		popup.add(sInfoItem);
		popup.add(lExitItem);

		trayIcon.setPopupMenu(popup);

		try
		{
			tray.add(trayIcon);
		}
		catch (final AWTException e)
		{
			System.out.println("TrayIcon could not be added.");
		}
	}

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
		if (sOutToClient != null)
			try
			{
				System.out.format("[%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.1f,%.1f,%.1f]\n",
													pQuatW,
													pQuatX,
													pQuatY,
													pQuatZ,
													pAccX,
													pAccY,
													pAccZ,
													pButton1,
													pButton2,
													pButton3);/**/
				sOutToClient.writeByte('[');
				sOutToClient.writeBytes(String.format("%.3f", pQuatW));
				sOutToClient.writeByte(',');
				sOutToClient.writeBytes(String.format("%.3f", pQuatX));
				sOutToClient.writeByte(',');
				sOutToClient.writeBytes(String.format("%.3f", pQuatY));
				sOutToClient.writeByte(',');
				sOutToClient.writeBytes(String.format("%.3f", pQuatZ));
				sOutToClient.writeByte(',');
				sOutToClient.writeBytes(String.format("%.3f", pAccX));
				sOutToClient.writeByte(',');
				sOutToClient.writeBytes(String.format("%.3f", pAccY));
				sOutToClient.writeByte(',');
				sOutToClient.writeBytes(String.format("%.3f", pAccZ));
				sOutToClient.writeByte(',');
				sOutToClient.writeBytes(String.format("%.1f", pButton1));
				sOutToClient.writeByte(',');
				sOutToClient.writeBytes(String.format("%.1f", pButton2));
				sOutToClient.writeByte(',');
				sOutToClient.writeBytes(String.format("%.1f", pButton3));
				sOutToClient.writeByte(']');
				sOutToClient.writeByte('\n');/**/
				sOutToClient.flush();
			}
			catch (final Throwable e)
			{
				sInfoItem.setLabel("no client connected");
				sOutToClient = null;
				try
				{
					sConnectionSocket.close();
				}
				catch (final IOException e1)
				{
					e1.printStackTrace();
				}
				e.printStackTrace();
			}

	}

	// Obtain the image URL
	protected static Image createImage(	final String path,
																			final String description)
	{
		final URL imageURL = TcpServer.class.getResource(path);

		if (imageURL == null)
		{
			System.err.println("Resource not found: " + path);
			return null;
		}
		else
		{
			return (new ImageIcon(imageURL, description)).getImage();
		}
	}
}
