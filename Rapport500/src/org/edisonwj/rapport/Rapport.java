package org.edisonwj.rapport;

/**
* RAPPORT main application which provides a GUI and program interface
* for generating random points, lines, and shapes.
* Structure based on code from Core Java by Cay Horstman.
* 
* @author William Edison
* @version 5.00 November 2020
*
* Copyright 2003 William J. Edison
* The code may be freely reused.
*/

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;

public class Rapport extends JFrame
   implements ActionListener, RapportDefaults
{
	public Rapport()
   {  setTitle("RAPPORT " + VERSION);
	  setSize(500, 500);
	  setLocation(200, 50);
      addWindowListener(new WindowAdapter()
      {  public void windowClosing(WindowEvent e)
         {  System.exit(0);
         }
      } );

	  Container contentPane = getContentPane();
	  contentPane.add(new TitlePanel());

      JMenuBar mbar = new JMenuBar();
      setJMenuBar(mbar);

/* Setup File Menu */
      JMenu fileMenu = new JMenu("File");

      newItem = new JMenuItem("New");
      openItem = new JMenuItem("Open");
      exitItem = new JMenuItem("Exit");

	  pointItem = new JMenuItem(TITLE_POINT);

	  lineItem = new JMenuItem(TITLE_LINE);
	  linepolyItem = new JMenuItem(TITLE_POLYLINE);
	  linefieldItem = new JMenuItem(TITLE_POISSON_LINE_FIELD);
	  randomwalkItem = new JMenuItem(TITLE_RANDOM_WALK);

	  poly_randomItem = new JMenuItem(TITLE_POLYGON_RANDOM_POINTS);
	  poly_hullItem = new JMenuItem(TITLE_POLYGON_CONVEX_HULL);
	  poly_cellItem = new JMenuItem(TITLE_POLYGON_CELL);
	  poly_monoItem = new JMenuItem(TITLE_POLYGON_MONOTONE);
	  poly_starItem = new JMenuItem(TITLE_POLYGON_STAR);
	  poly_spiralItem = new JMenuItem(TITLE_POLYGON_SPIRAL);
	  poly_drawItem = new JMenuItem(TITLE_POLYGON_DRAW);
	  poly_testItem = new JMenuItem(TITLE_POLYGON_TEST);

	  rectangleItem = new JMenuItem(TITLE_RECTANGLE);
	  ellipseItem = new JMenuItem(TITLE_CIRCLE_ELLIPSE);

	  cmapItem = new JMenuItem(TITLE_POLYMAP_CELL);
	  pmapItem = new JMenuItem(TITLE_POLYMAP_POISSON);
	  lmapItem = new JMenuItem(TITLE_POLYMAP_POLYLINE);
	  vmapItem = new JMenuItem(TITLE_POLYMAP_VORONOI);

	  mbar.add(makeMenu(fileMenu,
			  new Object[]
			  {makeMenu("New",
					new Object[]
					{
					pointItem,
					lineItem,
					linepolyItem,
					linefieldItem,
					randomwalkItem,

//				{	makeMenu("Points",
//						new Object[]
//	 					{	"Uniform",
//							"Exponential",
//							"Bi-variate normal"
//						},
//						this),

//					makeMenu("Lines",
//						new Object[]
//						{	"Uniform",
// 							"Exponential",
// 							"Bi-variate normal"
// 						},
// 						this),

					makeMenu("Shapes",
						new Object[]
						{
							poly_randomItem,
							null,
							poly_hullItem,
							poly_cellItem,
							poly_monoItem,
							poly_starItem,
							poly_spiralItem,
							poly_drawItem,
							poly_testItem,
							null,
							rectangleItem,
							ellipseItem
						},
						this),
					makeMenu("Polymaps",
						new Object[]
						{	cmapItem,
							pmapItem,
							lmapItem,
							vmapItem
						},
						this)
				},
				this),
				openItem,
		        null,
		        exitItem
			  },
      this));

/* Setup Help Menu */

      JMenu helpMenu = new JMenu("Help");
      helpMenu.setMnemonic('H');

      mbar.add(makeMenu(helpMenu,
         new Object[]
         {  helpItem = new JMenuItem("Help Document"),
            aboutItem = new JMenuItem("About")
         },
         this));
   }

   public void actionPerformed(ActionEvent evt)
   {  Object source = evt.getSource();
   
		if (source == helpItem)					/* Help Document */
		{
			desktop = Desktop.getDesktop();
			String filename = "Rapport_Help.txt";
		    String filePath = findDefaultDirectory() + "/" + filename;
			File file = new File(filePath);
	    	System.out.println("Open file: " + file.toString());
	        try {
	            desktop.open(file);
	        } catch (IOException ex) {
	        	System.out.println("Error opening help file");
	        }
		}
		
		if (source == aboutItem)				/* About Description */
		{
			JFrame aFrame = new JFrame("RAPPORT 5.00");
			aFrame.getContentPane().add(new JFrameGraphics());
			aFrame.setSize(400, 200);
			aFrame.setVisible(true);
		}

		if (source == pointItem)				/* Points */
		{
			if (transpt == null)
				transpt = new ptInfo();
			else
				transpt= (ptInfo)transpt.clone();
			if (ptD == null)
				ptD = new ptDialog(this);
			if (ptD.showDialog(transpt))
         {
				RGen rg = new ptGen(transpt);
				setCursor(Cursor.getPredefinedCursor(
						Cursor.WAIT_CURSOR));
				rg.start();
				setCursor(Cursor.getPredefinedCursor(
						Cursor.DEFAULT_CURSOR));
			}
		}

		else if (source == lineItem)			/* Lines */
		{
			if (transln == null)
				transln = new lnInfo();
			else
				transln= (lnInfo)transln.clone();
			if (lnD == null)
				lnD = new lnDialog(this);
			if (lnD.showDialog(transln))
         	{
				RGen rg = new lnGen(transln);
				rg.start();
			}
		}

		else if (source == linepolyItem)		/* Polylines */
		{
			if (translp == null)
				translp = new lpInfo();
			else
				translp= (lpInfo)translp.clone();
			if (lpD == null)
				lpD = new lpDialog(this);
			if (lpD.showDialog(translp))
         	{
				RGen rg = new lpGen(translp);
				rg.start();
			}
		}

		else if (source == linefieldItem)		/* Poisson line field */
		{
			if (translf == null)
				translf = new lfInfo();
			else
				translf= (lfInfo)translf.clone();
			if (lfD == null)
				lfD = new lfDialog(this);
			if (lfD.showDialog(translf))
         	{
				RGen rg = new lfGen(translf);
				rg.start();
			}
		}

		else if (source == randomwalkItem)		/* Random walk */
		{
			if (transrw == null)
				transrw = new rwInfo();
			else
				transrw= (rwInfo)transrw.clone();
			if (rwD == null)
				rwD = new rwDialog(this);
			if (rwD.showDialog(transrw))
         	{
				RGen rg = new rwGen(transrw);
				rg.start();
			}
		}

		else if (source == poly_hullItem)		/* Hull polygon */
		{
			if (transph == null)
				transph = new phInfo();
			else
				transph= (phInfo)transph.clone();
			if (phD == null)
				phD = new phDialog(this);
			if (phD.showDialog(transph))
		    {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				RGen rg = new phGen(transph);
				rg.start();
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}

		else if (source == poly_randomItem)		/* Random point polygon */
		{
			if (transpf == null)
				transpf = new pfInfo();
			else
				transpf= (pfInfo)transpf.clone();
			if (pfD == null)
				pfD = new pfDialog(this);
			if (pfD.showDialog(transpf))
		    {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				RGen rg = new pfGen(transpf);
				rg.start();
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}

		else if (source == poly_cellItem)		/* Cell polygons */
      	{
			if (transpc == null)
				transpc = new pcInfo();
			else
				transpc = (pcInfo)transpc.clone();
			if (pcD == null)
				pcD = new pcDialog(this);
			if (pcD.showDialog(transpc))
         	{
				RGen rg = new pcGen(transpc);
				rg.start();
			}
		}

		else if (source == poly_monoItem)	/* Monotone polygons */
      	{
			if (transpo == null)
				transpo = new poInfo();
			else
				transpo = (poInfo)transpo.clone();
			if (poD == null)
				poD = new poDialog(this);
			if (poD.showDialog(transpo))
         	{
				RGen rg = new poGen(transpo);
				rg.start();
			}
		}

		else if (source == poly_starItem)	/* Star polygons */
      	{
			if (transps == null)
				transps = new psInfo();
			else
				transps = (psInfo)transps.clone();
			if (psD == null)
				psD = new psDialog(this);
			if (psD.showDialog(transps))
         	{
				RGen rg = new psGen(transps);
				rg.start();
			}
		}

		else if (source == poly_spiralItem)	/* Spiral polygons */
		{
			if (transtr == null)
				transtr = new trInfo();
			else
				transtr = (trInfo)transtr.clone();
		   if (trD == null)
		   	trD = new trDialog(this);
		   if (trD.showDialog(transtr))
		   {
				RGen rg = new trGen(transtr);
				rg.start();
		   }
		}

		else if (source == ellipseItem)			/* Ellipses/circles */
		{
			if (transel == null)
				transel = new elInfo();
			else
				transel= (elInfo)transel.clone();
			if (elD == null)
				elD = new elDialog(this);
			if (elD.showDialog(transel))
         	{
				RGen rg = new elGen(transel);
				rg.start();
			}
		}

		else if (source == rectangleItem)		/* Rectangles */
		{
			if (transrc == null)
				transrc = new rcInfo();
			else
				transrc= (rcInfo)transrc.clone();
			if (rcD == null)
				rcD = new rcDialog(this);
			if (rcD.showDialog(transrc))
         	{
				RGen rg = new rcGen(transrc);
				rg.start();
			}
		}

		else if (source == cmapItem)			/* Cell polymap */
		{
			if (transcm == null)
				transcm = new cmInfo();
			else
				transcm= (cmInfo)transcm.clone();
			if (cmD == null)
				cmD = new cmDialog(this);
			if (cmD.showDialog(transcm))
		    {
				RGen rg = new cmGen(transcm);
				rg.start();
			}
		}

		else if (source == pmapItem)			/* Poisson polymap */
		{
			if (transpm == null)
				transpm = new pmInfo();
			else
				transpm= (pmInfo)transpm.clone();
			if (pmD == null)
				pmD = new pmDialog(this);
			if (pmD.showDialog(transpm))
         	{
				RGen rg = new pmGen(transpm);
				rg.start();
			}
		}

		else if (source == vmapItem)			/* Voronoi polymap */
		{
			if (transvm == null)
				transvm = new vmInfo();
			else
				transvm= (vmInfo)transvm.clone();
			if (vmD == null)
				vmD = new vmDialog(this);
			if (vmD.showDialog(transvm))
         	{
				RGen rg = new vmGen(transvm);
				rg.start();
			}
		}

		else if (source == lmapItem)			/* PolyLine polymap */
		{
			if (translm == null)
				translm = new lmInfo();
			else
				translm= (lmInfo)translm.clone();
			if (lmD == null)
				lmD = new lmDialog(this);
			if (lmD.showDialog(translm))
         	{
				RGen rg = new lmGen(translm);
				rg.start();
			}
		}

		else if (source == poly_testItem)		/* Test polygon */
		{
			if (transts == null)
				transts = new tsInfo();
			else
				transts = (tsInfo)transts.clone();
		   if (tsD == null)
		   	tsD = new tsDialog(this);
		   if (tsD.showDialog(transts))
		   {
				RGen rg = new tsGen(transts);
				rg.start();
			}
		}

		else if (source == poly_drawItem)		/* Draw polygon */
		{
			if (transdr == null)
				transdr = new drInfo();
			else
				transdr = (drInfo)transdr.clone();
		   if (drD == null)
		   	drD = new drDialog(this);
		   if (drD.showDialog(transdr))
		   {
				RGen rg = new drGen(transdr);
				rg.start();
			}
		}

		else if (source == openItem)
		{
			JFrame cf = new JFrame("Select File");
			JFileChooser fc = new JFileChooser();
			fc.setCurrentDirectory(new File("."));
			fc.setSelectedFile(new File("test.txt"));
			fc.setFileFilter(new txtFilter());
			fc.addChoosableFileFilter(fc.getAcceptAllFileFilter());
			int result = fc.showOpenDialog(cf);
			if ( result == JFileChooser.APPROVE_OPTION )
			{
				File inputFile = fc.getSelectedFile();
				try
				{
					RGen rg = new RGen(inputFile);
				}
				catch (IOException e)
				{
					System.out.println("RGen errpr: " + e);
				}
			}
		}

		else if (source == exitItem)
			System.exit(0);

		repaint();
   }

	public static JMenu makeMenu(Object parent,
	   Object[] items, Object target)
	{
		JMenu m = null;
	   if (parent instanceof JMenu)
	      m = (JMenu)parent;
	   else if (parent instanceof String)
	      m = new JMenu((String)parent);
	   else
	      return null;
	     for (int i = 0; i < items.length; i++)
	   {  if (items[i] == null)
	         m.addSeparator();
	      else
	         m.add(makeMenuItem(items[i], target));
	   }
	   return m;
	}

	public static JMenuItem makeMenuItem(Object item,
	   Object target)
	{
		JMenuItem r = null;
	   if (item instanceof String)
	      r = new JMenuItem((String)item);
	   else if (item instanceof JMenuItem)
	      r = (JMenuItem)item;
	   else return null;
	     if (target instanceof ActionListener)
	      r.addActionListener((ActionListener)target);
	   return r;
	}

	public static void main(String[] args)
	{
   		JFrame f = new Rapport();
		f.setVisible(true);
	}
	
    private String findDefaultDirectory()
    {
    	String currentDirectory = null;
    	String userDir = System.getProperty("user.dir");
	    	currentDirectory = userDir + "\\bin\\org\\edisonwj\\rapport\\";
	    	File dir = new File(currentDirectory);
			if (!dir.exists()) {
		    	currentDirectory = userDir + "\\org\\edisonwj\\rapport\\";
		    	dir = new File(currentDirectory);
			}
	    	return currentDirectory;
   }

	private Desktop desktop;
	private JMenuItem helpItem;
	private JMenuItem aboutItem;
	
	private JFrame framep;

	private JMenuItem newItem;
	private JMenuItem openItem;
	private JMenuItem exitItem;

	private JMenuItem pointItem;

	private JMenuItem lineItem;
	private JMenuItem linepolyItem;
	private JMenuItem linefieldItem;
	private JMenuItem randomwalkItem;

	private JMenuItem poly_randomItem;
	private JMenuItem poly_hullItem;
	private JMenuItem poly_cellItem;
	private JMenuItem poly_monoItem;
	private JMenuItem poly_starItem;
	private JMenuItem poly_spiralItem;

	private JMenuItem poly_testItem;
	private JMenuItem poly_drawItem;

	private JMenuItem ellipseItem;
	private JMenuItem rectangleItem;

	private JMenuItem cmapItem;
	private JMenuItem pmapItem;
	private JMenuItem vmapItem;
	private JMenuItem lmapItem;

	private ptInfo transpt = null;

	private lnInfo transln = null;
	private lpInfo translp = null;
	private lfInfo translf = null;
	private rwInfo transrw = null;

	private phInfo transph = null;
	private ppInfo transpp = null;
	private pfInfo transpf = null;

	private pcInfo transpc = null;
	private poInfo transpo = null;
	private psInfo transps = null;
	private trInfo transtr = null;

	private tsInfo transts = null;
	private drInfo transdr = null;

	private elInfo transel = null;
	private rcInfo transrc = null;

	private cmInfo transcm = null;
	private pmInfo transpm = null;
	private vmInfo transvm = null;
	private lmInfo translm = null;

	private ptDialog ptD = null;

	private lnDialog lnD = null;
	private lpDialog lpD = null;
	private lfDialog lfD = null;
	private rwDialog rwD = null;

	private pfDialog pfD = null;
	private phDialog phD = null;
	private pcDialog pcD = null;
	private poDialog poD = null;
	private psDialog psD = null;
	private trDialog trD = null;

	private tsDialog tsD = null;
	private drDialog drD = null;

	private elDialog elD = null;
	private rcDialog rcD = null;

	private cmDialog cmD = null;
	private pmDialog pmD = null;
	private vmDialog vmD = null;
	private lmDialog lmD = null;

	private RFrame cuF = null;
}

class TitlePanel extends JPanel
	implements RapportDefaults
{
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		Border lbb = BorderFactory.createLoweredBevelBorder();
		setBorder(lbb);

		setFonts(g);
		String s1 = "RAPPORT ";
		String s2 = "Generates RAndom Points, POlygons and Regions for Testing";
		int w1 = f1m.stringWidth(s1);
		int w2 = f2m.stringWidth(s2);

		Dimension d = getSize();
		int c1x = (d.width - w1) / 2;
		int c2x = (d.width - w2) / 2;
		int c1y = (d.height - f1m.getHeight() - f2m.getHeight()) / 2 + f1m.getAscent();
		int c2y = c1y + f2m.getHeight();
		c1y += 50;
		c2y += 50;


		g.setColor(Color.red);
		g.setFont(f1);
		g.drawString(s1, c1x, c1y);

		g.setColor(Color.black);
		g.setFont(f2);
		g.drawString(s2, c2x, c2y);
	}

	private void setFonts(Graphics g)
	{
		if (f1 != null) return;
		f1 = new Font("SansSerif", Font.BOLD, 48);
		f2 = new Font("Serif", Font.BOLD + Font.ITALIC, 16);
		f1m = g.getFontMetrics(f1);
		f2m = g.getFontMetrics(f2);
	}

	private Font f1;
	private Font f2;
	private FontMetrics f1m;
	private FontMetrics f2m;
}