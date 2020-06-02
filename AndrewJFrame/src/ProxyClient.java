import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import javax.imageio.*;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.SwingUtilities;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.Imaging;
import org.glassfish.tyrus.client.ClientManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
@ClientEndpoint
public class ProxyClient
{	 
	private String printerName;
	private PrinterApp context;
	private double ml, mr, mt, mb, w, h;
	
	public ProxyClient(String printerName, PrinterApp context, double ml, double mr, double mt, double mb, double w, double h)
	{
		this.printerName = printerName;
		this.context = context;
		this.ml = ml;
		this.mr = mr;
		this.mt = mt;
		this.mb = mb;
		this.w = w;
		this.h = h;
	}
	
    @OnOpen

    public void onOpen(Session session)
    {
    	SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				context.button.setText("Connected");
			}
		});
    	System.out.println("Connected: " + this.printerName);
        try {
        	if(context.salt == null || context.salt.length() == 0 || context.hash == null || context.hash.length() == 0)
			session.getBasicRemote().sendText("[\"OPEN\"]");
        	else
        	{
        		String payload = "[\"OPEN\",\""+context.salt+ "\",\""+context.hash+ "\"]";
        		session.getBasicRemote().sendText(payload);
        	}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

 

    @OnMessage

	public void onMessage(String message, Session session) {
		Gson gson = new Gson();
		ArrayList<Object> args = gson.fromJson(message, new TypeToken<ArrayList<Object>>() {
		}.getType());

		if (args.size() == 3) {
			if ((boolean) args.get(0)) {
				context.salt = (String) args.get(1);
				context.hash = (String) args.get(2);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						context.label.setText("Access Code: " + context.salt);
						context.saveSettings();
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								context.textArea.setText("Access code validated" + "\n\n" + context.textArea.getText());
							}
						});

					}
				});
			} else
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						context.textArea.setText("Access code was not valid!" + "\n\n" + context.textArea.getText());
					}
				});

		} else {
			handleDownload((String) args.get(1), this.printerName, (String) args.get(0));
		}

		System.out.println("Message Received: " + message);
		// printImage("https://swiftium.co/Reporting/images/Partners/SwiftiumLogo.png",
		// this.printerName, PrinterApp.getSaltString(6));
	}

 

    @OnClose

    public void onClose(Session session, CloseReason closeReason)
    {
    	System.out.println("Closed");
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				context.textArea.setText("Connection closed" + "\n\n" + context.textArea.getText());
			}
		});

    }
    
    private void handleDownload(String imageURL, String printerName, String sourceId)
    {
		final String fimageURL = imageURL.replace("EventManagementSystemKioskPageTemplates", "EventManagementSystem/KioskPageTemplates");

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				context.textArea.setText("Incoming print job: [" + sourceId + "] [" + printerName + "]\r\n" + fimageURL + "\n\n" + context.textArea.getText());
			}
		});


    	Runnable myRunnable = new Runnable() {
			public void run() {
				try {
					URL url = new URL(fimageURL);
					String data = new String (downloadUrl(url), "UTF-8");
					Gson gson = new Gson();
			    	ArrayList<Object> args = gson.fromJson(data, new TypeToken<ArrayList<Object>>(){}.getType());
			    	if(args != null)
			    	{
			    		ArrayList<Object> imgs = ((ArrayList<Object>)args.get(2));
			    		for(int i=0; i<imgs.size(); i++)
			    		{
			    			printImage(Base64.getDecoder().decode(((String)imgs.get(i)).getBytes("UTF-8")), printerName, sourceId);
			    		}
			    	}
					
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		};
		Thread thread = new Thread(myRunnable);
		thread.start();
    }
    
	private void printImage(byte[] imgByte, String printerName, final String sourceId) {
		Runnable myRunnable = new Runnable() {
			public void run() {
				PrinterJob printJob = PrinterJob.getPrinterJob();
				try {
					PageFormat pf = printJob.defaultPage();
					Paper paper = pf.getPaper();

					double width = w * 72d;
					double height = h * 72d;
					//double margin = 1d * 72d;



					paper.setSize(width, height);
					paper.setImageableArea(
							ml * 72d,
							mt * 72d,
							width - (mr * 72d) - (ml * 72d),
							height - (mb * 72d) - (mt * 72d));

					pf.setOrientation(PageFormat.PORTRAIT);
					pf.setPaper(paper);

					//ByteArrayInputStream bis = new ByteArrayInputStream(imgByte);
					//BufferedImage img = ImageIO.read(bis);
					BufferedImage img = Imaging.getBufferedImage(imgByte);
					ImageInfo imgInfo = Imaging.getImageInfo(imgByte);
					//bis.close();
					try {
						File outputfile = new File("Cache\\" + sourceId + "-" + System.currentTimeMillis() + ".png");

						//BufferedImage bImg = new BufferedImage(250, 250, BufferedImage.TYPE_INT_ARGB);
						//Graphics2D cg = bImg.createGraphics();
						//cg.drawImage(img, 0, 0, 250, 250, 0, 0, img.getWidth(), img.getHeight(), null);
						ImageIO.write(img, "png", outputfile);

						//Imaging.writeImage(img, outputfile, ImageFormats.PNG, null);
					} catch (Exception e) {

					}
					printJob.setPrintService(findPrintService(printerName));
					printJob.setPrintable(new Printable() {
						public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
								throws PrinterException {
							if (pageIndex != 0) {
								return NO_SUCH_PAGE;
							}

							System.out.println("Width: " +  img.getWidth());
							System.out.println("Height: " + img.getHeight());

//							Dimension _scaled = getScaledDimension(
//									new Dimension(img.getWidth(null), img.getHeight(null)),
//									new Dimension((img.getWidth(null) / 300) * 72, (img.getHeight(null) / 300) * 72)
//							);

//							System.out.println("S-Width: " +  d.width);
//							System.out.println("S-Height: " + d.height);

							Graphics2D g = (Graphics2D)graphics;

							//int x = (int)Math.round((pageFormat.getImageableWidth() - img.getWidth()) / 2f);
							//int y = (int)Math.round((pageFormat.getImageableHeight() - img.getHeight()) / 2f);

							g.setColor(Color.BLACK);

							g.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
							g.draw(new Rectangle2D.Double(0, 0, pageFormat.getImageableWidth() - 1, pageFormat.getImageableHeight() - 1));
							g.drawImage(img, 0, 0, (img.getWidth() / imgInfo.getPhysicalWidthDpi()) * 72, (img.getHeight() / imgInfo.getPhysicalHeightDpi()) * 72, 0, 0, img.getWidth(), img.getHeight(), null);
							return PAGE_EXISTS;
						}
					}, pf);
					printJob.print();
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							context.textArea.setText("Printed [" + sourceId + "]" + "\n\n" + context.textArea.getText());
						}
					});
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		};
		Thread thread = new Thread(myRunnable);
		thread.start();
	}
	
	
	
	private PrintService findPrintService(String printerName)
	{
		PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
		for (PrintService printService : printServices)
		{
			if (printService.getName().trim().equals(printerName))
			{
				return printService;
			}
		}
		return null;
	}
	
	private byte[] downloadUrl(URL toDownload) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] _result = null;

		try {
			byte[] chunk = new byte[4096];
			int bytesRead;
			InputStream stream = toDownload.openStream();

			while ((bytesRead = stream.read(chunk)) > 0) {
				outputStream.write(chunk, 0, bytesRead);
			}

			_result = outputStream.toByteArray();
			outputStream.close();

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return _result;
	}
}
