
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.*;
import org.glassfish.tyrus.client.ClientManager;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

public class PrinterApp extends JFrame
{
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
	
	public PrinterApp()
	{

		setSize(800,400);
		setTitle("Swiftium Printer Proxy");
		createGUI();
		setVisible(true);
		//salt = getSaltString(5);
		

		loadSettings();
		label.setText(salt==null? "Pending...": "Access Code: " + salt);
	}
	
	public static void main(String[] args)
	{
		try
		{
			File newDirectory = new File("Cache");

			if(!newDirectory.exists())
				newDirectory.mkdir();
		}
		catch(Exception e)
		{

		}

		context = new PrinterApp();
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
						client.connectToServer(pc, new URI("wss://swiftium.co:1000/PrintRelay"));

					} catch (Exception e) {

						throw new RuntimeException(e);

					}

				}
			};
			Thread thread = new Thread(myRunnable);
			thread.start();
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
				"\r\nHash: " + (hash==null?"":hash);
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
	
	private void createGUI()
	{
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		Container window = getContentPane();
		window.setLayout(null);
		Insets insets = window.getInsets();
		
		createDropdown();
		
		label = new JLabel("A");
		label.setFont(new Font("Arial", Font.BOLD, 15));
		
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
		
		window.add(button);
		Dimension size = button.getPreferredSize();
		button.setBounds((365/2) - (130 / 2), 220 + insets.top,
	             130, size.height);
		//3
		window.add(label);
		size = label.getPreferredSize();
		label.setBounds((365/2) - (150 / 2), 300 + insets.top,
	             150, size.height);
		
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

	private void updateMargins()
	{

	}
}