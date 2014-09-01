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
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import jssc.SerialPortException;

import org.apache.commons.lang.SystemUtils;

import egg3d.Egg3D;
import egg3d.Egg3DListener;

/**
 * Class Egg3DTCPServer
 * 
 * This class implements is the entry point for the Egg3D TCP server program.
 * This program connects to the
 *
 * @author Loic Royer 2014
 *
 */
public class Egg3DTCPServer implements Egg3DListener
{

	private static final int cEgg3DTCPport = 4444;
	private static DataOutputStream sOutToClient;
	private static BufferedReader sInFromClient;
	private static volatile Socket sConnectionSocket;
	private static volatile boolean sOn = true;
	private static MenuItem sInfoItem;
	private static TrayIcon sTrayIcon;
	private static SystemTray sSystemTray;

	/**
	 * Program entry point
	 * 
	 * @param argv
	 * @throws Exception
	 */
	public static void main(final String argv[]) throws Exception
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				setUpSysTray();
			}
		});

		startEgg3dThread();

		final ServerSocket lWelcomeSocket = new ServerSocket(cEgg3DTCPport);
		lWelcomeSocket.setSoTimeout(250);
		sConnectionSocket = null;
		while (sOn)
		{
			System.out.println("Waiting for TCP connection...");

			while (sConnectionSocket != null)
			{
				Thread.sleep(100);
			}

			while (sOn && sConnectionSocket == null)
			{
				try
				{
					sConnectionSocket = lWelcomeSocket.accept();
				}
				catch (SocketTimeoutException e)
				{
				}
			}

			Socket lConnectionSocket = sConnectionSocket;
			if (lConnectionSocket != null && lConnectionSocket.isConnected()
					&& !lConnectionSocket.isClosed())
			{
				try
				{
					System.out.println("Connection received from: " + lConnectionSocket.getInetAddress());
					sInFromClient = new BufferedReader(new InputStreamReader(lConnectionSocket.getInputStream()));
					sOutToClient = new DataOutputStream(lConnectionSocket.getOutputStream());
					sInfoItem.setLabel("Client connected");
				}
				catch (Throwable e)
				{
					e.printStackTrace();
					sConnectionSocket=null;
				}
			}

		}
		lWelcomeSocket.close();

		sSystemTray.remove(sTrayIcon);
		// System.exit(0);
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
					lEgg3D.addEgg3DListener(new Egg3DTCPServer());
					System.out.println("Connecting to Egg3D...");
					try
					{
						if (lEgg3D.connect())
						{
							System.out.println("Connected to Egg3D!");
						}
						else
						{
							System.err.println("Not connected to Egg3D!");
							sOn = false;
						}
					}
					catch (Throwable e)
					{
						System.err.println("Exception while trying to connect to Egg3D: " + e.getLocalizedMessage());
						System.out.println("make sure that your Egg3D is paired (Bluetooth) and switched on!");
						sOn = false;
					}

					while (sOn)
					{
						Thread.sleep(100);
					}
				}
				catch (final InterruptedException e)
				{
					e.printStackTrace();
				}

				if (lEgg3D != null)
					lEgg3D.close();
			}
		};

		lThread.setDaemon(true);
		lThread.start();

	}

	private static void setUpSysTray()
	{
		final PopupMenu popup = new PopupMenu();

		if (SystemUtils.IS_OS_WINDOWS)
		{
			sTrayIcon = new TrayIcon(createImage(	"icon/Egg.64x64.jpg",
																						"tray icon"));
		}
		else
		{
			sTrayIcon = new TrayIcon(createImage(	"icon/Egg.256x256.alpha.png",
																						"tray icon"));
		}

		sSystemTray = SystemTray.getSystemTray();

		// Create a pop-up menu components
		final MenuItem lEgg3DItem = new MenuItem("Egg3D");
		sInfoItem = new MenuItem("...");
		final MenuItem lExitItem = new MenuItem("Exit");
		lExitItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent pE)
			{
				sOn = false;
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

		sTrayIcon.setPopupMenu(popup);

		try
		{
			sSystemTray.add(sTrayIcon);
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
				/*System.out.format("[%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.1f,%.1f,%.1f]\n",
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
					if (sConnectionSocket != null)
					{
						sConnectionSocket.close();
						sConnectionSocket = null;
					}
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
		final URL imageURL = Egg3DTCPServer.class.getResource(path);

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
