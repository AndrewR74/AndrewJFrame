/* 
 * This program shows how to handle events with a single listener.
 */

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

public class DrawExample extends JFrame implements ActionListener
{
	private JButton button1, button2, button3;
	private JPanel panel;
	
	
	public static void main(String[] args)
	{
		DrawExample myFrame = new DrawExample();
		myFrame.setSize(350,310);
		myFrame.setTitle("GUI Design Example");
		myFrame.createGUI();
		myFrame.setVisible(true);
	}
	
	private void createGUI()
	{
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		Container window = getContentPane();
		window.setLayout(new FlowLayout());
		
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(300,200));
		panel.setBackground(Color.LIGHT_GRAY);
		window.add(panel);
		
		// use one listener to handle all buttons
		
		button1 = new JButton("Button 1");
		button2 = new JButton("Button 2");
		button3 = new JButton("Button 3");
		button1.addActionListener(this);
		button2.addActionListener(this);
		button3.addActionListener(this);
		
		window.add(button1);
		window.add(button2);
		window.add(button3);
	}
	
	public void actionPerformed(ActionEvent event)
	{
		Graphics g = panel.getGraphics();
		
		/* can also set theEvent = event.getSource();
		 * if (theEvent.equals(button1));
		 */
		
		if (event.getSource().equals(button1))
		{
			g.drawLine(50, 50, 50, 150);
			g.drawLine(50, 100, 100, 100);
		}
		else if (event.getSource().equals(button2))
		{
			g.drawLine(100, 100, 100, 150);
			g.drawLine(150, 150, 150, 100);
		}
		else if (event.getSource().equals(button3))
		{
			g.drawLine(40, 160, 160, 160);
		}
	}
}
