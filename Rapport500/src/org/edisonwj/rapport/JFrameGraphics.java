package org.edisonwj.rapport;
/**
 *  Rapport program description
 * 
 */

import java.awt.Graphics;
import javax.swing.JPanel;

public class JFrameGraphics extends JPanel
{
	public void paint(Graphics g)
	{
		g.drawString("Author: William Edison",10,15);
		g.drawString("Copyright 2003 William J. Edison",10, 30);
		g.drawString("All rights reserved; however the code may be freely reused.",10,45);
		g.drawString("The original version of Rapport was published in 2003.",10,75);
		g.drawString("Version 5.00 was created in 2020 using Java Version 8.",10,90);
	}
}