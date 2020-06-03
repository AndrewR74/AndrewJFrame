
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.websocket.CloseReason;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class PrinterApp extends JFrame {
	public JButton button;
	public JLabel label;
	private JComboBox<String> dropdown;
	private static PrinterApp context;
	public JTextArea textArea;
	private boolean isRunning;
	private ProxyClient pc;
	public String salt;
	public String hash;
	private JTextField txtMl, txtMr, txtMt, txtMb, txtWidth, txtHeight;
	private Session session;
	public int totalPrinted = 0;
	private JLabel printedLabel;
	private String appVersion = "2.0";

	public PrinterApp() {


		setSize(800, 400);
		setTitle("Swiftium Printer Proxy v" + appVersion);
		createGUI();
		setVisible(true);
		//salt = getSaltString(5);


		loadSettings();
		printedLabel.setText("Total Printed: " + totalPrinted);
		label.setText(salt == null ? "Pending..." : "Access Code: " + salt);
	}

	public static void main(String[] args) {
		try {
			File newDirectory = new File("Cache");

			if (!newDirectory.exists())
				newDirectory.mkdir();
		} catch (Exception e) {

		}

		context = new PrinterApp();
	}

	private void updateProxy()
	{
		try {
			if(pc != null) {
				pc.printerName = (String) dropdown.getSelectedItem();
				pc.ml = Double.parseDouble(txtMl.getText());
				pc.mr = Double.parseDouble(txtMr.getText());
				pc.mt = Double.parseDouble(txtMt.getText());
				pc.mb = Double.parseDouble(txtMb.getText());
				pc.w = Double.parseDouble(txtWidth.getText());
				pc.h = Double.parseDouble(txtHeight.getText());
				saveSettings();
			}
		}
		catch(Exception e)
		{
			textArea.setText("One or more of the values entered is incorrect" + "\n\n" + textArea.getText());
		}
	}

	private void startProxy() {
		if (!isRunning) {
			isRunning = true;
			Runnable myRunnable = new Runnable() {
				public void run() {
					ClientManager client = ClientManager.createClient();


					try {
						pc = new ProxyClient((String) dropdown.getSelectedItem(), context,
								Double.parseDouble(txtMl.getText()),
								Double.parseDouble(txtMr.getText()),
								Double.parseDouble(txtMt.getText()),
								Double.parseDouble(txtMb.getText()),
								Double.parseDouble(txtWidth.getText()),
								Double.parseDouble(txtHeight.getText())
						);

						ClientManager.ReconnectHandler reconnectHandler = new ClientManager.ReconnectHandler() {

							private int counter = 0;

							@Override
							public boolean onDisconnect(CloseReason closeReason) {
								if(closeReason.getCloseCode() == CloseReason.CloseCodes.NORMAL_CLOSURE) {
									isRunning = false;
									SwingUtilities.invokeLater(new Runnable() {
										public void run() {
											button.setText("Connect");
										}
									});
									return false;
								}
								try {
									Thread.sleep(5000);
								} catch (InterruptedException e) {
								}
								return true;
							}

							@Override
							public boolean onConnectFailure(Exception exception) {
								try {
									Thread.sleep(5000);
								} catch (InterruptedException e) {
								}
								return true;
							}

							@Override
							public long getDelay() {
								return 2;
							}
						};

						client.getProperties().put(ClientProperties.RECONNECT_HANDLER, reconnectHandler);
						session = client.connectToServer(pc, new URI("wss://swiftium.co:1000/PrintRelay"));



					} catch (Exception e) {

						throw new RuntimeException(e);

					}

				}
			};
			Thread thread = new Thread(myRunnable);
			thread.start();
		}
		else
		{
			try {
				button.setText("Disconnecting");
				session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE,"User close"));
			} catch (IOException e) {
			}
		}
	}
	
	
	public static String getSaltString(int len)
	{
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < len)
        { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }
	
	private void loadSettings()
	{
		    String data = ""; 
		    try {
		    	File f = new File("settings.txt");
		    	if(f.exists())
		    	{
		    		data = new String(Files.readAllBytes(Paths.get("settings.txt")));
		    		String[] lines = data.split("\r\n", -1);
		    		for(int i =0; i<lines.length; i++)
		    		{
		    			String[] parts = lines[i].split(": ", -1);
		    			if(parts.length == 2)
		    			{
		    				if(parts[0].equals("Printer"))
		    				{
		    					dropdown.setSelectedItem(parts[1]);
		    				}
		    				else if(parts[0].equals("Salt"))
		    				{
		    					salt = parts[1];
		    				}
		    				else if(parts[0].equals("Hash"))
		    				{
		    					hash = parts[1];
		    				}
							else if(parts[0].equals("Margin Left"))
							{
								txtMl.setText(parts[1]);
							}
							else if(parts[0].equals("Margin Right"))
							{
								txtMr.setText(parts[1]);
							}
							else if(parts[0].equals("Margin Bottom"))
							{
								txtMb.setText(parts[1]);
							}
							else if(parts[0].equals("Margin Top"))
							{
								txtMt.setText(parts[1]);
							}
							else if(parts[0].equals("Page Width"))
							{
								txtWidth.setText(parts[1]);
							}
							else if(parts[0].equals("Page Height"))
							{
								txtHeight.setText(parts[1]);
							}
							else if(parts[0].equals("Total Printed"))
							{
								try {
									totalPrinted = (Integer.parseInt(parts[1]));
								}
								catch(Exception e)
								{

								}
							}
		    			}
		    		}
		    	}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
		    this.repaint();
	}
	
	public void saveSettings()
	{
		String content = 
				"Printer: " + dropdown.getSelectedItem() + 
				"\r\nSalt: " + (salt==null?"":salt) +
				"\r\nHash: " + (hash==null?"":hash) +
				"\r\nMargin Left: " + txtMl.getText() +
				"\r\nMargin Right: " + txtMr.getText() +
				"\r\nMargin Bottom: " + txtMb.getText() +
				"\r\nMargin Top: " + txtMt.getText() +
				"\r\nPage Height: " + txtHeight.getText() +
				"\r\nPage Width: " + txtWidth.getText() +
				"\r\nTotal Printed: " + totalPrinted
				;
		String path = "settings.txt";
		try {
			Files.write(Paths.get(path), content.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.repaint();
	}
	
	private void createDropdown()
	{
		dropdown = new JComboBox<String>();
		PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        System.out.println("Number of print services: " + printServices.length);

        for (PrintService printer : printServices)
        	dropdown.addItem(printer.getName());
	}

	public void incPrintedTotal()
	{
		this.totalPrinted++;
		this.printedLabel.setText("Total Printed: " + totalPrinted);
		saveSettings();

	}
	
	private void createGUI()
	{
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		Container window = getContentPane();
		window.setLayout(null);
		Insets insets = window.getInsets();
		
		createDropdown();
		
		label = new JLabel("A");
		label.setFont(new Font("Arial", Font.BOLD, 15));
		printedLabel = new JLabel("Total Printed: 00000");
		printedLabel.setFont(new Font("Arial", Font.PLAIN, 12));
		
		// use one listener to handle all buttons
		textArea = new JTextArea(21,37);
		button = new JButton("Start");
		txtMb = new JTextField();
		txtWidth = new JTextField();
		txtMt = new JTextField();
		txtMr = new JTextField();
		txtMl = new JTextField();
		txtHeight = new JTextField();

		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				button.setText("Connecting...");
				startProxy();
				saveSettings();
			}
		});

		dropdown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateProxy();
			}
		});
		
		window.add(button);
		Dimension size = button.getPreferredSize();
		button.setBounds((365/2) - (130 / 2), 220 + insets.top,
	             130, size.height);
		//3
		window.add(label);
		size = label.getPreferredSize();
		label.setBounds((365/2) - (150 / 2), 280 + insets.top,
	             150, size.height);

		window.add(printedLabel);
		size = printedLabel.getPreferredSize();
		printedLabel.setBounds((365/2) - (size.width / 2), 320 + insets.top,
				size.width, size.height);
		
		window.add(dropdown);
		size = dropdown.getPreferredSize();
		dropdown.setBounds((365/2) - (size.width / 2), 10 + insets.top,
	             size.width, size.height);
		//4
		window.add(textArea);
		size = textArea.getPreferredSize();
		textArea.setBounds((350), 13 + insets.top,
	             size.width, size.height);

		JTextField[] ps = new JTextField[]
				{
						txtMl, txtMr, txtMb, txtMt, txtHeight, txtWidth
				};
		String[] labels = new String[]
				{
					"Margin Left", "Margin Right", "Margin Bottom", "Margin Top", "Page Height", "Page Width"

				};
		Double[] iValues = new Double[]
				{
					0.5d, 0.5d, 0.5d, 0.5d, 11.0d, 8.5d
				};

		int i = 0;
		for(JTextField t : ps) {

			t.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void insertUpdate(DocumentEvent e) {
					updateProxy();
				}

				@Override
				public void removeUpdate(DocumentEvent e) {
					updateProxy();
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					updateProxy();
				}
			});

			JLabel lbl = new JLabel(labels[i]);
			window.add(lbl);
			size = lbl.getPreferredSize();
			lbl.setBounds(75, 60 + (i * 25) + insets.top,
					size.width, size.height);

			window.add(t);
			size = t.getPreferredSize();
			t.setBounds((365 / 2) - (35 / 2), 60 + (i * 25) + insets.top,
					35, size.height);
			t.setText(iValues[i].toString());

			lbl = new JLabel("inches");
			window.add(lbl);
			size = lbl.getPreferredSize();
			lbl.setBounds(((365 / 2) - (35 / 2)) + 40, 60 + (i++ * 25) + insets.top,
					size.width, size.height);


		}
	}
}