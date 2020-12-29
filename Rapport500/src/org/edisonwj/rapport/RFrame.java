package org.edisonwj.rapport;

/**
 * RFrame specifies the action listeners associated with various 
 * object display panels. Common selectable actions include:
 * - File save group
 * - File save object displayed
 * - Save jpeg image of object displayed
 * - Print image of object displayed
 * - Change object displayed
 * - Change scale (zoom level) of object displayed
 * - Change center of oject displayed
 * - Select Actions:
 * -    Group metrics
 * -    Object metrics
 * -    Generate convex hull
 * -    Triangulate
 * -    Scale of unit square
 * -    Show polar diagram
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.text.*;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

class RFrame extends JFrame
	implements DocumentListener, RapportDefaults
{
	private RGen frg;
	private RObject fro;
	private RInfo fri;
	private RPanel frp;
	private MetricsDialog md;

	private JCheckBox draw3dBox;
	private boolean draw3d;
	private JButton filegrpButton;
	private JButton fileButton;
	private JButton jpegButton;
	private JButton printButton;
	private JButton resetButton;

	private JLabel roLabel;
	private JLabel scLabel;

	private JScrollBar roBar;
	private JScrollBar scBar;
	private JSlider roSlider;

	private TextField roTextField;
	private TextField scaleTextField;

	public RFrame(RGen rg)
	{
//		System.out.println("RFrame(RGen rg) constructor: " + this.hashCode() +
//				", rg: " + rg.hashCode());
		frg = rg;

   		setTitle(rg.getTitle());
		setSize(rg.ri.pixelDim, rg.ri.pixelDim);
		setLocation(100,100);

		JMenuBar mb = new JMenuBar();

		JMenu m = new JMenu("File");
		JMenuItem mia = new JMenuItem("Save Group");
		JMenuItem mis = new JMenuItem("Save Object");
		JMenuItem mip = new JMenuItem("Print");
		JMenuItem mid = new JMenuItem("Properties");
		JMenuItem mic = new JMenuItem("Close");

		JMenu a = new JMenu("Actions");
		JMenuItem mig = new JMenuItem("Group Metrics");
		JMenuItem mim = new JMenuItem("Object Metrics");
		JMenuItem mih = new JMenuItem("Convex Hull");
		JMenuItem mit = new JMenuItem("Triangulate");
		JMenuItem mir = new JMenuItem("Scale to Unit Square");
		JMenuItem mpd = new JMenuItem("Polar Diagram");

		setJMenuBar(mb);
		m.add(mia);
		m.add(mis);
		m.add(mip);
		m.add(mid);
		m.add(mic);
		mb.add(m);

		a.add(mig);
		a.add(mim);
		a.add(mih);
		a.add(mit);
		a.add(mir);
		a.add(mpd);
		mb.add(a);

		Container contentPane = getContentPane();

		JPanel dp = new JPanel();
		Border border = BorderFactory.createEtchedBorder();
		dp.setBorder(border);
		dp.setLayout(new BorderLayout());

		JPanel dp1 = new JPanel();
		int initialRO = 0;
		dp1.add(roLabel = new JLabel("Object"));
		dp1.add(roBar = new JScrollBar(Adjustable.HORIZONTAL, initialRO, 0, -1, 100));
		roBar.setUnitIncrement(1);
		roBar.setBlockIncrement(4);
		dp1.add(roTextField = new TextField(6));
		roTextField.setText("" + initialRO);

		dp1.add(scLabel = new JLabel("Scale"));
		dp1.add(scBar = new JScrollBar(Adjustable.HORIZONTAL,
							10*(int)frg.ri.logicalDim, 0, 1, 200));
		scBar.setUnitIncrement(1);
		scBar.setBlockIncrement(10);
		dp1.add(scaleTextField = new TextField(4));
		scaleTextField.setText("" + frg.ri.SlogicalDim);
		dp.add(dp1, "North");

		JPanel dp2 = new JPanel();
		draw3dBox = new JCheckBox("Draw3d Format");
		dp2.add(draw3dBox);
		draw3dBox.setSelected(DRAW3D_FORMAT);
		filegrpButton = new JButton("Save Group");
		dp2.add(filegrpButton);
		fileButton = new JButton("Save Object");
		dp2.add(fileButton);
		printButton = new JButton("Print");
		dp2.add(printButton);
		jpegButton = new JButton("Jpeg");
		dp2.add(jpegButton);
		resetButton = new JButton("Clear");
		dp2.add(resetButton);
		dp.add(dp2, "South");

		contentPane.add(dp, "South");

		JPanel pp = new JPanel();
		pp.setBorder(border);
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM);
		if (frg.rf == null)
			pp.add(new JLabel(df.format(frg.getTimestamp()) +
				"      Seeds: " + frg.getRI().Sseed + " / " + frg.getRI().Sseed2));
		else
			pp.add(new JLabel("File: " + frg.rf.getName() + "      " + df.format(frg.getTimestamp()) +
				"      Seeds: " + frg.getRI().Sseed + " / " + frg.getRI().Sseed2));
		contentPane.add(pp, "North");

//		System.out.println("RFrame creates RPanel");
		frp = new RPanel(rg);
//		System.out.println("RFrame created RPanel frp: " + frp.hashCode());
		frp.setRO(initialRO);
		contentPane.add(frp, "Center");

		mia.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				draw3d = draw3dBox.isSelected();
				JFrame cf = new JFrame("Select Save Group File");
				JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new File("."));
				fc.setSelectedFile(new File("test_group.txt"));
				fc.setFileFilter(new txtFilter());
				fc.addChoosableFileFilter(fc.getAcceptAllFileFilter());
				int result = fc.showSaveDialog(cf);
				if ( result == JFileChooser.APPROVE_OPTION )
				{
						File outputFile = fc.getSelectedFile();
						frg.setRF(outputFile);
						saveGroupData(outputFile);
						System.out.println("Save group to file: " + outputFile.getPath());;
				}
			}
		});

		mis.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				draw3d = draw3dBox.isSelected();
				JFrame cf = new JFrame("Select Save Object File");
				JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new File("."));
				fc.setSelectedFile(new File("test.txt"));
				fc.setFileFilter(new txtFilter());
				fc.addChoosableFileFilter(fc.getAcceptAllFileFilter());
				int result = fc.showSaveDialog(cf);
				if ( result == JFileChooser.APPROVE_OPTION )
				{
						File outputFile = fc.getSelectedFile();
						frg.setRF(outputFile);
						saveObjectData(outputFile);
						System.out.println("Save object to file: " + outputFile.getPath());;
				}
			}
		});

		mip.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				print();
			}
		});

		mid.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				JOptionPane.showMessageDialog(null,
					frg.getRI().SdispInfo,
					"Properties",
					JOptionPane.PLAIN_MESSAGE);
			}
		});

		mic.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				dispose();
			}
		});

		mih.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
//				RObject o = frg.getRO();
//				o.findHull();
//				Graphics g = frp.getGraphics();
//				o.drawHull(g, frp);
				frp.doHull();
				frp.repaint();
			}
		});

		mim.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				int i = frp.getCurrentRO();

				if (i > -1 )
				{
					RObject rb = (RObject)frg.rv.get(i);
					int t = rb.getType();

					if (t == TYPE_POINT_SET)
					{
						RPointSet ps = (RPointSet)frg.rv.get(i);
						ps.computeMetrics();
						MetricsDialog md = new MetricsDialog(null, i, ps);
						md.show();
					}

					else if (t == TYPE_CIRCLE)
					{
						RCircle c = (RCircle)frg.rv.get(i);
						c.computeMetrics();
						MetricsDialog md = new MetricsDialog(null, i, c);
						md.show();
					}
					
					else if (t == TYPE_ELLIPSE)
					{
						REllipse e = (REllipse)frg.rv.get(i);
						e.computeMetrics();
						MetricsDialog md = new MetricsDialog(null, i, e);
						md.show();
					}

					else if (t == TYPE_POLYMAP)
					{
						RPolyMap pm = (RPolyMap)frg.rv.get(i);
						pm.computeMetrics();
						MetricsDialog md = new MetricsDialog(null, i, pm);
						md.show();
					}

					else if (t == TYPE_POLYGON || t == TYPE_POLYGON_CELL )
					{
						PolygonA p = (PolygonA)frg.rv.get(i);
						p.computeMetrics();
						MetricsDialog md = new MetricsDialog(null, i, p);
						md.show();
					}
					
					else
					{
						System.out.println("Object metrics not implemented for this data type.");
					}
				}

				else
				{
					RObject rb = (RObject)frg.rv.get(0);
					int t = rb.getType();
					if (t == TYPE_POLYGON || t == TYPE_POLYGON_CELL || t == TYPE_POLYMAP)
					{
						frg.computeMetrics();
						GroupMetricsDialog gmd = new GroupMetricsDialog(null, frg);
						gmd.show();
					}
					else
						System.out.println("Group metrics not implemented for this data type.");
				}
			}
		});

		mig.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				RObject rb = (RObject)frg.rv.get(0);
				int t = rb.getType();
				if (t == TYPE_POLYGON || t == TYPE_POLYGON_CELL || t == TYPE_POLYMAP)
				{
					frg.computeMetrics();
					GroupMetricsDialog gmd = new GroupMetricsDialog(null, frg);
					gmd.show();
				}
				else
					System.out.println("Group metrics not implemented for this data type.");
			}
		});

		mit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				frp.doDecomp();
				frp.repaint();
			}
		});


		mir.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				frp.doScale();
				frp.repaint();
			}
		});

		mpd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				int value = roBar.getValue();
				fro = (RObject)(frg.rv).get(value);
				fri = frg.ri;
				JFrame f2 = new RFrame(fro, fri);
	  			f2.setVisible(true);
			}
		});

   		addWindowListener(new WindowAdapter()
	  	{ public void windowClosing(WindowEvent e)
			{ dispose(); }
		} );

		filegrpButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				draw3d = draw3dBox.isSelected();
				JFrame cf = new JFrame("RFrame Select Save Group File");
				JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new File("."));
				fc.setSelectedFile(new File("test.txt"));
				fc.setFileFilter(new txtFilter());
				fc.addChoosableFileFilter(fc.getAcceptAllFileFilter());
				int result = fc.showSaveDialog(cf);
				if ( result == JFileChooser.APPROVE_OPTION )
				{
						File outputFile = fc.getSelectedFile();
						frg.setRF(outputFile);
						saveGroupData(outputFile);
						System.out.println("Save group to file: " + outputFile.getPath());;
				}
			}
		} );

		fileButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				draw3d = draw3dBox.isSelected();
				JFrame cf = new JFrame("Select Save File");
				JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new File("."));
				fc.setSelectedFile(new File("test.txt"));
				fc.setFileFilter(new txtFilter());
				fc.addChoosableFileFilter(fc.getAcceptAllFileFilter());
				int result = fc.showSaveDialog(cf);
				if ( result == JFileChooser.APPROVE_OPTION )
				{
						File outputFile = fc.getSelectedFile();
						frg.setRF(outputFile);
						saveObjectData(outputFile);
						System.out.println("Save object to file: " + outputFile.getPath());;
				}
			}
		} );

		jpegButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				JFrame cf = new JFrame("Select Jpeg File");
				JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new File("."));
				fc.setSelectedFile(new File("test.jpg"));
				fc.setFileFilter(new jpgFilter());
				fc.addChoosableFileFilter(fc.getAcceptAllFileFilter());
				int result = fc.showSaveDialog(cf);
				if ( result == JFileChooser.APPROVE_OPTION )
				{
						File outputFile = fc.getSelectedFile();
						frg.setRF(outputFile);
						saveJpeg(outputFile);
						System.out.println("Save image to jpeg file: " + outputFile.getPath());;
				}
			}
		} );

		printButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				print();
			}
		} );

		resetButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				frp.doClear();
				frp.repaint();
			}
		} );

		roBar.addAdjustmentListener(new AdjustmentListener()
		{
			public void adjustmentValueChanged(AdjustmentEvent evt)
			{
				roBar.setMaximum(frg.rv.size()-1);
				int value = roBar.getValue();
				roTextField.setText("" + value);
				frp.setRO(value);
				frp.repaint();
			}
		} );

		scBar.addAdjustmentListener(new AdjustmentListener()
		{
			public void adjustmentValueChanged(AdjustmentEvent evt)
			{
				double scale = scBar.getValue()/10.0;
				scaleTextField.setText("" + scale);
				frg.ri.SlogicalDim = String.valueOf(scale);
				frg.ri.logicalDim = scale;
				frp.repaint();
			}
		} );
	}

	public RFrame(RObject ro, RInfo ri)
	{
		fro = ro;
		fri = ri;

		setTitle(fri.title);
		setSize(fri.pixelDim, fri.pixelDim);
		setLocation(100,100);

		JMenuBar mb = new JMenuBar();

		JMenu m = new JMenu("File");
		JMenuItem mis = new JMenuItem("Save");
		JMenuItem mip = new JMenuItem("Print");
		JMenuItem mid = new JMenuItem("Properties");
		JMenuItem mic = new JMenuItem("Close");

		setJMenuBar(mb);
		m.add(mis);
		m.add(mip);
		m.add(mid);
		m.add(mic);
		mb.add(m);

		Container contentPane = getContentPane();

		JPanel dp = new JPanel();
		Border border = BorderFactory.createEtchedBorder();
		dp.setBorder(border);
		dp.setLayout(new BorderLayout());

		JPanel dp1 = new JPanel();

		dp1.add(scLabel = new JLabel("Scale"));
		dp1.add(scBar = new JScrollBar(Adjustable.HORIZONTAL,
							10*(int)fri.logicalDim, 0, 1, 200));
		scBar.setUnitIncrement(1);
		scBar.setBlockIncrement(10);
		dp1.add(scaleTextField = new TextField(4));
		scaleTextField.setText("" + fri.SlogicalDim);
		dp.add(dp1, "North");

		JPanel dp2 = new JPanel();
		fileButton = new JButton("File");
		dp2.add(fileButton);
		printButton = new JButton("Print");
		dp2.add(printButton);
		jpegButton = new JButton("Jpeg");
		dp2.add(jpegButton);
		resetButton = new JButton("Clear");
		dp2.add(resetButton);
		dp.add(dp2, "South");

		contentPane.add(dp, "South");

		JPanel pp = new JPanel();
		pp.setBorder(border);
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM);
		if (fri.outputFile == null)
			pp.add(new JLabel(df.format(fro.getTimestamp()) +
				"      Seeds: " + fri.Sseed + " / " + fri.Sseed2));
		else
			pp.add(new JLabel("File: " + fri.outputFile.getName() + "      " + df.format(fro.getTimestamp()) +
				"      Seeds: " + fri.Sseed + " / " + fri.Sseed2));


		pp.add(new JLabel(df.format(new Date()) +
			"      Seeds: " + fri.Sseed + " / " + fri.Sseed2));
		contentPane.add(pp, "North");

		frp = new RPanel(fro, fri);
		contentPane.add(frp, "Center");

		mis.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				JFrame cf = new JFrame("Select Save File");
				JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new File("."));
				fc.setSelectedFile(new File("test.txt"));
				fc.setFileFilter(new txtFilter());
				fc.addChoosableFileFilter(fc.getAcceptAllFileFilter());
				int result = fc.showSaveDialog(cf);
				if ( result == JFileChooser.APPROVE_OPTION )
				{
						File outputFile = fc.getSelectedFile();
						frg.setRF(outputFile);
						saveObjectData(outputFile);
						System.out.println("Save data to file: " + outputFile.getPath());;
				}
			}
		});

		mip.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				print();
			}
		});

		mid.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				JOptionPane.showMessageDialog(null,
					fri.SdispInfo,
					"Properties",
					JOptionPane.PLAIN_MESSAGE);
			}
		});

		mic.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
//				System.exit(0);
				dispose();
			}
		});

    	addWindowListener(new WindowAdapter()
 	  	{ public void windowClosing(WindowEvent e)
 			{ dispose(); }
 		} );

 		fileButton.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent evt)
 			{
 				JFrame cf = new JFrame("Select Save Object File");
 				JFileChooser fc = new JFileChooser();
 				fc.setCurrentDirectory(new File("."));
 				fc.setSelectedFile(new File("test.txt"));
 				fc.setFileFilter(new txtFilter());
 				fc.addChoosableFileFilter(fc.getAcceptAllFileFilter());
 				int result = fc.showSaveDialog(cf);
 				if ( result == JFileChooser.APPROVE_OPTION )
 				{
 						File outputFile = fc.getSelectedFile();
 						saveObjectData(outputFile);
 						System.out.println("Save object to file: " + outputFile.getPath());;
 				}
 			}
 		} );

 		jpegButton.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent evt)
 			{
 				JFrame cf = new JFrame("Select Jpeg File");
 				JFileChooser fc = new JFileChooser();
 				fc.setCurrentDirectory(new File("."));
 				fc.setSelectedFile(new File("test.jpg"));
 				fc.setFileFilter(new jpgFilter());
 				fc.addChoosableFileFilter(fc.getAcceptAllFileFilter());
 				int result = fc.showSaveDialog(cf);
 				if ( result == JFileChooser.APPROVE_OPTION )
 				{
 						File outputFile = fc.getSelectedFile();
 						saveJpeg(outputFile);
 						System.out.println("Save image to jpeg file: " + outputFile.getPath());;
 				}
 			}
 		} );

 		printButton.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent evt)
 			{
 				print();
 			}
 		} );

 		resetButton.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent evt)
 			{
 				frp.doClear();
 				frp.repaint();
 			}
 		} );

 		scBar.addAdjustmentListener(new AdjustmentListener()
 		{
 			public void adjustmentValueChanged(AdjustmentEvent evt)
 			{
 				double scale = scBar.getValue()/10.0;
 				scaleTextField.setText("" + scale);
 				fri.SlogicalDim = String.valueOf(scale);
 				fri.logicalDim = scale;
 				frp.repaint();
 			}
		} );
	}


	public void insertUpdate(DocumentEvent evt)
	{
		setDim();
	}

	public void removeUpdate(DocumentEvent evt)
	{
		setDim();
	}

	public void changedUpdate(DocumentEvent evt)
	{
	}

	public void setDim()
	{
//		frg.ri.SlogicalDim = dimField.getText();
//		frg.ri.logicalDim = Double.parseDouble(frg.ri.SlogicalDim);
		frp.repaint();
	}

	public void saveJpeg(File outputFile)
	{
//		Rectangle rect = this.getBounds();
//		BufferedImage bi = (BufferedImage)createImage(rect.width, rect.height);
//		Graphics g = bi.getGraphics();
//		this.paint(g);
//
//		try
//		{
//			FileOutputStream fos = new FileOutputStream(outputFile);
//			JPEGImageEncoder jie = JPEGCodec.createJPEGEncoder(fos);
//			jie.encode(bi);
//			fos.close();
//		}
//		catch(FileNotFoundException ex)
//		{
//			ex.printStackTrace();
//		}
//		catch(IOException ex)
//		{
//			ex.printStackTrace();
//		} 
		
		Rectangle rect = this.getBounds();
		BufferedImage image = (BufferedImage)createImage(rect.width, rect.height);
    	BufferedImage imageRGB = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.OPAQUE); // Remove alpha-channel from buffered image.
    	Graphics2D graphics = imageRGB.createGraphics();
    	this.paint(graphics);
    	try {
    		ImageIO.write(imageRGB, "jpg", outputFile);
    	} catch (IOException ex) {
    		System.out.println(ex.getMessage());
    	}
    	graphics.dispose();
	}
	
	public void print()
	{
//		PrinterJob pj = PrinterJob.getPrinterJob();
//		pj.setPrintable((Printable)this);
//		if (pj.printDialog())
//		{
//			try
//			{
//				pj.print();
//			}
//			catch(PrinterException ex)
//			{
//				ex.printStackTrace();
//			}
//		}
//	}

		Properties props = new Properties();
		PrintJob pj = getToolkit().getPrintJob(this, "Rapport Printout", props);
		if (pj != null)
		{
			Graphics pg = pj.getGraphics();
//			Rectangle rect = this.getBounds();
//			BufferedImage image = (BufferedImage)createImage(rect.width, rect.height);
//	    	BufferedImage imageRGB = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.OPAQUE); // Remove alpha-channel from buffered image.
//	    	Graphics2D pg = imageRGB.createGraphics();
			
			
			if (pg != null)
			{
				Dimension pd = pj.getPageDimension();
				Dimension od = this.getSize();
				pg.translate((pd.width - od.width)/2, (pd.height - od.height)/2);
				this.paint(pg);
				pg.dispose();
				pj.end();
			}
		}
	}
	
	private void saveObjectData(File outputFile)
	{
//		System.out.println("\nRFrame saveObjectData");
		int start = frp.getCurrentRO();
		int num = 1;
		if ( start == -1) {
			start = 0;
			num = frg.rv.size();
		}	
		saveDataFile(start, num, outputFile);
	}
	
	private void saveGroupData(File outputFile)
	{
//		System.out.println("\nRFrame saveGroupData");
		int start = 0;
		int num = frg.rv.size();
		saveDataFile(start, num, outputFile);
	}

	private void saveDataFile(int start, int num, File outputFile)
	{
//		System.out.println("RFrame saveDataFile - start: " + start + ", num: " + num);
		RObject rb = (RObject) frg.rv.get(start);
		RInfo ri = rb.getRInfo();
		int type = rb.type;
		String title = ri.title;
//		System.out.println("RFrame saveDataFile - rb: " + rb.hashCode() +
//												", ri: " + ri.hashCode() + 
//												", type: " + type + 
//												", title: " + title);
		
		try
		{
			PrintWriter out = new PrintWriter(new FileWriter(outputFile));
	
			if (draw3d) {
				String lineOut = "View: true, true, false, false, true, false, false, false, true\n";
				lineOut += "Range: 1.0, 1.0, 1.0\n";
				if (ri.center) {
					lineOut += "Camera: 0.0 ,  -180.0 ,  -1100.0\n";
					lineOut += "Origin: 0, 0, 0";
				}
				else {
					lineOut += "Camera: 0.0 ,  -180.0 ,  -800.0\n";
					lineOut += "Origin: -100, -100, 0";
				}
				out.println(lineOut);
			}
			else {
				out.println("#TYPE=" + TYPE_GROUP + " GROUP " +
									Integer.toString(type) + " " +	/* Object type */
									title);							/* Object title */
				out.println("#NI=" + num);							/* Number of generations */
			}
			
			String parms = "";
			for (int i = start; i < start + num; i++)
			{
				rb = (RObject)frg.rv.get(i);						/* Generated item */
//				System.out.println("RFrame writeData - rb: " + rb.hashCode() +
//						", type: " + rb.type +
//						", draw3d: " + draw3d);
				rb.writeData(draw3d, out);
			}			
			out.close();
		}
		catch(IOException e)
		{
			System.out.println("PrintWriter Error: " + e);
			return;
		}
	}
}

class MetricsDialog extends JDialog
{
	public MetricsDialog(JFrame parent, int id, RPointSet p)
	{
		super(parent, "PointSet Metrics - " + id, false);

		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);Box b = Box.createVerticalBox();
		nf.setMinimumIntegerDigits(1);b.add(Box.createGlue());

		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM);
		b.add(new JLabel("Timestamp: " + (df.format(((RObject)p).getTimestamp()))));
		b.add(new JLabel("  # points: " + p.size()));
		b.add(new JLabel("  area: " + nf.format(p.area)));
		b.add(new JLabel("  hullArea: " + nf.format(p.hullArea)));
		b.add(new JLabel("  areaRatio: " + nf.format(p.areaRatio)));
		b.add(new JLabel("  perimeter: " + nf.format(p.perimeter)));
		b.add(new JLabel("  hullPerimeter: " + nf.format(p.hullPerimeter)));
		b.add(new JLabel("  perRatio: " + nf.format(p.perRatio)));
		b.add(new JLabel("  compacity: " + nf.format(p.compacity)));
		b.add(new JLabel("  Chi2 vertices: " + nf.format(p.chi2)));
		b.add(Box.createGlue());
		getContentPane().add(b, "Center");

		JPanel p2 = new JPanel();
		JButton ok = new JButton("Ok");
		p2.add(ok);
		getContentPane().add(p2, "South");

		ok.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
				{ setVisible(false); }
		} );

		setLocation(450,50);
		setSize(250,400);
	}

	public MetricsDialog(JFrame parent, int id, PolygonA p)
	{
		super(parent, "Polygon Metrics - " + id, false);

		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);Box b = Box.createVerticalBox();
		nf.setMinimumIntegerDigits(1);b.add(Box.createGlue());

		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM);
		b.add(new JLabel("Timestamp: " + (df.format(((RObject)p).getTimestamp()))));
		b.add(new JLabel("  # vertices: " + p.size()));
		b.add(new JLabel("  convex: " + p.convex));
		b.add(new JLabel("  ccw: " + p.ccw));
		b.add(new JLabel("  cg: " + p.cg));
		b.add(new JLabel("  area: " + nf.format(p.area)));
		b.add(new JLabel("  hullArea: " + nf.format(p.hullArea)));
		b.add(new JLabel("  areaRatio: " + nf.format(p.areaRatio)));
		b.add(new JLabel("  perimeter: " + nf.format(p.perimeter)));
		b.add(new JLabel("  hullPerimeter: " + nf.format(p.hullPerimeter)));
		b.add(new JLabel("  perRatio: " + nf.format(p.perRatio)));
		b.add(new JLabel("  compacity: " + nf.format(p.compacity)));
		b.add(new JLabel("  # notches: " + nf.format(p.notches)));
		b.add(new JLabel("  frequency: " + nf.format(p.freq)));
		b.add(new JLabel("  amplitude: " + nf.format(p.ampl)));
		b.add(new JLabel("  convexity: " + nf.format(p.conv)));
		b.add(new JLabel("  complexity: " + nf.format(p.complexity)));
		b.add(new JLabel("  simplexity: " + nf.format(p.simplexity)));
		b.add(Box.createGlue());
		getContentPane().add(b, "Center");

		JPanel p2 = new JPanel();
		JButton ok = new JButton("Ok");
		p2.add(ok);
		getContentPane().add(p2, "South");

		ok.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
				{ setVisible(false); }
		} );

		setLocation(450,50);
		setSize(250,400);
	}

	public MetricsDialog(JFrame parent, int id, RPolyMap pm)
	{
		super(parent, "Polymap Metrics - " + id, false);

		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(4);
		nf.setMinimumFractionDigits(2);
		nf.setMinimumIntegerDigits(1);
		Box b1 = Box.createVerticalBox();
		b1.add(Box.createGlue());

		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM);
		b1.add(new JLabel("Timestamp: " + (df.format(pm.getTimestamp()))));
		b1.add(new JLabel("# Polygons: " + nf.format(pm.getSize())));
		b1.add(new JLabel(" "));

		b1.add(new JLabel("Total # vertices: " + nf.format(pm.getTotal_vertices())));
		b1.add(new JLabel("Mean # vertices: " + nf.format(pm.getMean_vertices())));
		b1.add(new JLabel("Min  # vertices: " + nf.format(pm.getMin_vertices())));
		b1.add(new JLabel("Max  # vertices: " + nf.format(pm.getMax_vertices())));
		b1.add(new JLabel("Std  # vertices: " + nf.format(Math.sqrt(pm.getVar_vertices()))));
		b1.add(new JLabel("Var  # vertices: " + nf.format(pm.getVar_vertices())));
		b1.add(new JLabel("Chi2 vertices: " + nf.format(pm.getChi2_vertices())));
		b1.add(new JLabel(" "));

		b1.add(new JLabel("Total area: " + nf.format(pm.getTotal_area())));
		b1.add(new JLabel("Mean area: " + nf.format(pm.getMean_area())));
		b1.add(new JLabel("Min  area: " + nf.format(pm.getMin_area())));
		b1.add(new JLabel("Max  area: " + nf.format(pm.getMax_area())));
		b1.add(new JLabel("Std  area: " + nf.format(Math.sqrt(pm.getVar_area()))));
		b1.add(new JLabel("Var  area: " + nf.format(pm.getVar_area())));
		b1.add(new JLabel(" "));

		b1.add(new JLabel("Total perimeter: " + nf.format(pm.getTotal_perimeter())));
		b1.add(new JLabel("Mean  perimeter: " + nf.format(pm.getMean_perimeter())));
		b1.add(new JLabel("Min  perimeter: " + nf.format(pm.getMin_perimeter())));
		b1.add(new JLabel("Max  perimeter: " + nf.format(pm.getMax_perimeter())));
		b1.add(new JLabel("Std  perimeter: " + nf.format(Math.sqrt(pm.getVar_perimeter()))));
		b1.add(new JLabel("Var  perimeter: " + nf.format(pm.getVar_perimeter())));
		b1.add(new JLabel(" "));
		b1.add(Box.createGlue());

		Box b2 = Box.createVerticalBox();
		b2.add(Box.createGlue());

		b2.add(new JLabel("Total compacity: " + nf.format(pm.getTotal_compacity())));
		b2.add(new JLabel("Mean  compacity: " + nf.format(pm.getMean_compacity())));
//		b2.add(new JLabel("Mean  compacity: " + nf.format(pm.getMean_compacity()) + " of " +
//							nf.format(pm.getCnt_compacity())));
		b2.add(new JLabel("Min  compacity: " + nf.format(pm.getMin_compacity())));
		b2.add(new JLabel("Max  compacity: " + nf.format(pm.getMax_compacity())));
		b2.add(new JLabel("Std  compacity: " + nf.format(Math.sqrt(pm.getVar_compacity()))));
		b2.add(new JLabel("Var  compacity: " + nf.format(pm.getVar_compacity())));
		b2.add(new JLabel(" "));

		b2.add(new JLabel("Total complexity: " + nf.format(pm.getTotal_complexity())));
		b2.add(new JLabel("Mean  complexity: " + nf.format(pm.getMean_complexity())));
		b2.add(new JLabel("Min  complexity: " + nf.format(pm.getMin_complexity())));
		b2.add(new JLabel("Max  complexity: " + nf.format(pm.getMax_complexity())));
		b2.add(new JLabel("Std  complexity: " + nf.format(Math.sqrt(pm.getVar_complexity()))));
		b2.add(new JLabel("Var  complexity: " + nf.format(pm.getVar_complexity())));
		b2.add(new JLabel(" "));

		b2.add(new JLabel("Total simplexity: " + nf.format(pm.getTotal_simplexity())));
		b2.add(new JLabel("Mean  simplexity: " + nf.format(pm.getMean_simplexity())));
		b2.add(new JLabel("Min  simplexity: " + nf.format(pm.getMin_simplexity())));
		b2.add(new JLabel("Max  simplexity: " + nf.format(pm.getMax_simplexity())));
		b2.add(new JLabel("Std  simplexity: " + nf.format(Math.sqrt(pm.getVar_simplexity()))));
		b2.add(new JLabel("Var  simplexity: " + nf.format(pm.getVar_simplexity())));
		b2.add(new JLabel(" "));
		b2.add(Box.createGlue());

		getContentPane().add(b1, "West");
		getContentPane().add(b2, "East");

		JPanel p2 = new JPanel();
		JButton ok = new JButton("Ok");
		p2.add(ok);
		getContentPane().add(p2, "South");

		ok.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
				{ setVisible(false); }
		} );

		setSize(400,640);
		setLocation(350,50);
	}

	public MetricsDialog(JFrame parent, int id, RCircle p)
	{
		super(parent, "RCircle Metrics - " + id, false);
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);Box b = Box.createVerticalBox();
		nf.setMinimumIntegerDigits(1);b.add(Box.createGlue());
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM);

		b.add(new JLabel("Timestamp: " + (df.format(((RObject)p).getTimestamp()))));
//		b.add(new JLabel("  # vertices: " + nv));
//		b.add(new JLabel("  convex: " + p.convex));
//		b.add(new JLabel("  ccw: " + p.ccw));
//		b.add(new JLabel("  cg: " + p.cg));
		b.add(new JLabel("  area: " + nf.format(p.area)));
		b.add(new JLabel("  hullArea: " + nf.format(p.hullArea)));
		b.add(new JLabel("  areaRatio: " + nf.format(p.areaRatio)));
		b.add(new JLabel("  perimeter: " + nf.format(p.perimeter)));
		b.add(new JLabel("  hullPerimeter: " + nf.format(p.hullPerimeter)));
		b.add(new JLabel("  perRatio: " + nf.format(p.perRatio)));
//		b.add(new JLabel("  # notches: " + nf.format(p.notches)));
//		b.add(new JLabel("  frequency: " + nf.format(p.freq)));
//		b.add(new JLabel("  amplitude: " + nf.format(p.ampl)));
//		b.add(new JLabel("  convexity: " + nf.format(p.conv)));
		b.add(new JLabel("  compacity: " + nf.format(p.compacity)));
		b.add(new JLabel("  complexity: " + nf.format(p.complexity)));
//		b.add(new JLabel("  simplexity: " + nf.format(p.simplexity)));
		b.add(Box.createGlue());
		getContentPane().add(b, "Center");

		JPanel p2 = new JPanel();
		JButton ok = new JButton("Ok");
		p2.add(ok);
		getContentPane().add(p2, "South");

		ok.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
				{ setVisible(false); }
		} );

		setLocation(400,520);
		setSize(250,400);
	}

	public MetricsDialog(JFrame parent, int id, REllipse p)
	{
		super(parent, "REllipse Metrics - " + id, false);
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);Box b = Box.createVerticalBox();
		nf.setMinimumIntegerDigits(1);b.add(Box.createGlue());
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM);
		
		b.add(new JLabel("Timestamp: " + (df.format(((RObject)p).getTimestamp()))));
		//	b.add(new JLabel("  # vertices: " + nv));
		//	b.add(new JLabel("  convex: " + p.convex));
		//	b.add(new JLabel("  ccw: " + p.ccw));
		//	b.add(new JLabel("  cg: " + p.cg));
		b.add(new JLabel("  area: " + nf.format(p.area)));
		b.add(new JLabel("  hullArea: " + nf.format(p.hullArea)));
		b.add(new JLabel("  areaRatio: " + nf.format(p.areaRatio)));
		b.add(new JLabel("  perimeter: " + nf.format(p.perimeter)));
		b.add(new JLabel("  hullPerimeter: " + nf.format(p.hullPerimeter)));
		b.add(new JLabel("  perRatio: " + nf.format(p.perRatio)));
		//	b.add(new JLabel("  # notches: " + nf.format(p.notches)));
		//	b.add(new JLabel("  frequency: " + nf.format(p.freq)));
		//	b.add(new JLabel("  amplitude: " + nf.format(p.ampl)));
		//	b.add(new JLabel("  convexity: " + nf.format(p.conv)));
		b.add(new JLabel("  compacity: " + nf.format(p.compacity)));
		b.add(new JLabel("  complexity: " + nf.format(p.complexity)));
		
		//	b.add(new JLabel("  simplexity: " + nf.format(p.simplexity)));
		b.add(Box.createGlue());
		getContentPane().add(b, "Center");
		
		JPanel p2 = new JPanel();
		JButton ok = new JButton("Ok");
		p2.add(ok);
		getContentPane().add(p2, "South");
		
			ok.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
					{ setVisible(false); }
			} );
		
			setLocation(400,520);
			setSize(250,400);
		}
}

class GroupMetricsDialog extends JDialog implements RapportDefaults
{
	public GroupMetricsDialog(JFrame parent, RGen rg)
	{
		super(parent, "Group Metrics", false);

		if (rg.getGroupType() == TYPE_POLYGON || rg.getGroupType() == TYPE_POLYGON_CELL)
		{
			NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMaximumFractionDigits(4);
			nf.setMinimumFractionDigits(2);
			nf.setMinimumIntegerDigits(1);

			Box b1 = Box.createVerticalBox();
			b1.add(Box.createGlue());

			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM);
			b1.add(new JLabel("Timestamp: " + (df.format(rg.getTimestamp()))));
			b1.add(new JLabel("Seed: " + rg.getRI().Sseed));
			b1.add(new JLabel("# Polygons: " + nf.format(rg.getSize())+
							 " # Points: " + nf.format(rg.getRI().nv)));
			b1.add(new JLabel(" "));

			b1.add(new JLabel("Total # vertices: " + nf.format(rg.getTotal_vertices())));
			b1.add(new JLabel("Mean # vertices: " + nf.format(rg.getMean_vertices())));
			b1.add(new JLabel("Min  # vertices: " + nf.format(rg.getMin_vertices())));
			b1.add(new JLabel("Max  # vertices: " + nf.format(rg.getMax_vertices())));
			b1.add(new JLabel("Std  # vertices: " + nf.format(Math.sqrt(rg.getVar_vertices()))));
			b1.add(new JLabel("Var  # vertices: " + nf.format(rg.getVar_vertices())));
			b1.add(new JLabel("Chi2 vertices: " + nf.format(rg.getChi2_vertices())));
			b1.add(new JLabel("Chi2 inner vertices: " + nf.format(rg.getChi2_inner_vertices())));
			b1.add(new JLabel("Chi2 non-corner vertices: " + nf.format(rg.getChi2_non_corner_vertices())));
			b1.add(new JLabel(" "));

			b1.add(new JLabel("Total area: " + nf.format(rg.getTotal_area())));
			b1.add(new JLabel("Mean area: " + nf.format(rg.getMean_area())));
			b1.add(new JLabel("Min  area: " + nf.format(rg.getMin_area())));
			b1.add(new JLabel("Max  area: " + nf.format(rg.getMax_area())));
			b1.add(new JLabel("Std  area: " + nf.format(Math.sqrt(rg.getVar_area()))));
			b1.add(new JLabel("Var  area: " + nf.format(rg.getVar_area())));
			b1.add(new JLabel(" "));

			b1.add(new JLabel("Total perimeter: " + nf.format(rg.getTotal_perimeter())));
			b1.add(new JLabel("Mean  perimeter: " + nf.format(rg.getMean_perimeter())));
			b1.add(new JLabel("Min  perimeter: " + nf.format(rg.getMin_perimeter())));
			b1.add(new JLabel("Max  perimeter: " + nf.format(rg.getMax_perimeter())));
			b1.add(new JLabel("Std  perimeter: " + nf.format(Math.sqrt(rg.getVar_perimeter()))));
			b1.add(new JLabel("Var  perimeter: " + nf.format(rg.getVar_perimeter())));
			b1.add(new JLabel(" "));
			b1.add(Box.createGlue());

			Box b2 = Box.createVerticalBox();
			b2.add(Box.createGlue());

			b2.add(new JLabel(" "));
			b2.add(new JLabel("Total compacity: " + nf.format(rg.getTotal_compacity())));
			b2.add(new JLabel("Mean  compacity: " + nf.format(rg.getMean_compacity())));
//			b2.add(new JLabel("Mean  compacity: " + nf.format(rg.getMean_compacity()) + " of " +
//								nf.format(rg.getCnt_compacity())));
			b2.add(new JLabel("Min  compacity: " + nf.format(rg.getMin_compacity())));
			b2.add(new JLabel("Max  compacity: " + nf.format(rg.getMax_compacity())));
			b2.add(new JLabel("Std  compacity: " + nf.format(Math.sqrt(rg.getVar_compacity()))));
			b2.add(new JLabel("Var  compacity: " + nf.format(rg.getVar_compacity())));
			b2.add(new JLabel(" "));
			
			b2.add(new JLabel("Total complexity: " + nf.format(rg.getTotal_complexity())));
			b2.add(new JLabel("Mean  complexity: " + nf.format(rg.getMean_complexity())));
			b2.add(new JLabel("Min  complexity: " + nf.format(rg.getMin_complexity())));
			b2.add(new JLabel("Max  complexity: " + nf.format(rg.getMax_complexity())));
			b2.add(new JLabel("Std  complexity: " + nf.format(Math.sqrt(rg.getVar_complexity()))));
			b2.add(new JLabel("Var  complexity: " + nf.format(rg.getVar_complexity())));
			b2.add(new JLabel(" "));

			b2.add(new JLabel("Total simplexity: " + nf.format(rg.getTotal_simplexity())));
			b2.add(new JLabel("Mean  simplexity: " + nf.format(rg.getMean_simplexity())));
			b2.add(new JLabel("Min  simplexity: " + nf.format(rg.getMin_simplexity())));
			b2.add(new JLabel("Max  simplexity: " + nf.format(rg.getMax_simplexity())));
			b2.add(new JLabel("Std  simplexity: " + nf.format(Math.sqrt(rg.getVar_simplexity()))));
			b2.add(new JLabel("Var  simplexity: " + nf.format(rg.getVar_simplexity())));
			b2.add(new JLabel(" "));
			b2.add(Box.createGlue());

			getContentPane().add(b1, "West");
			getContentPane().add(b2, "East");
		}
		else if (rg.getGroupType() == TYPE_POLYMAP)
		{
			NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMaximumFractionDigits(4);
			nf.setMinimumFractionDigits(2);
			nf.setMinimumIntegerDigits(1);

			Box b1 = Box.createVerticalBox();
			b1.add(Box.createGlue());

			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM);
			b1.add(new JLabel("Timestamp: " + (df.format(rg.getTimestamp()))));
			b1.add(new JLabel("Seed: " + rg.getRI().Sseed));
			b1.add(new JLabel("# Polymaps: " + nf.format(rg.getTotal_polymaps())));
			b1.add(new JLabel(" "));

			b1.add(new JLabel("Total # polygons: " + nf.format(rg.getTotal_polygons())));
			b1.add(new JLabel("Mean # polygons: " + nf.format(rg.getMean_polygons())));
			b1.add(new JLabel("Min  # polygons: " + nf.format(rg.getMin_polygons())));
			b1.add(new JLabel("Max  # polygons: " + nf.format(rg.getMax_polygons())));
			b1.add(new JLabel("Std  # polygons: " + nf.format(Math.sqrt(rg.getVar_polygons()))));
			b1.add(new JLabel("Var  # polygons: " + nf.format(rg.getVar_polygons())));
			b1.add(new JLabel(" "));

			b1.add(new JLabel("Total # vertices: " + nf.format(rg.getTotal_vertices())));
			b1.add(new JLabel("Mean # vertices: " + nf.format(rg.getMean_vertices())));
			b1.add(new JLabel("Min  # vertices: " + nf.format(rg.getMin_vertices())));
			b1.add(new JLabel("Max  # vertices: " + nf.format(rg.getMax_vertices())));
			b1.add(new JLabel("Std  # vertices: " + nf.format(Math.sqrt(rg.getVar_vertices()))));
			b1.add(new JLabel("Var  # vertices: " + nf.format(rg.getVar_vertices())));
			b1.add(new JLabel("Chi2 vertices: " + nf.format(rg.getChi2_vertices())));
			b1.add(new JLabel(" "));

			b1.add(new JLabel("Total area: " + nf.format(rg.getTotal_area())));
			b1.add(new JLabel("Mean area: " + nf.format(rg.getMean_area())));
			b1.add(new JLabel("Min  area: " + nf.format(rg.getMin_area())));
			b1.add(new JLabel("Max  area: " + nf.format(rg.getMax_area())));
			b1.add(new JLabel("Std  area: " + nf.format(Math.sqrt(rg.getVar_area()))));
			b1.add(new JLabel("Var  area: " + nf.format(rg.getVar_area())));
			b1.add(new JLabel(" "));

			b1.add(new JLabel("Total perimeter: " + nf.format(rg.getTotal_perimeter())));
			b1.add(new JLabel("Mean  perimeter: " + nf.format(rg.getMean_perimeter())));
			b1.add(new JLabel("Min  perimeter: " + nf.format(rg.getMin_perimeter())));
			b1.add(new JLabel("Max  perimeter: " + nf.format(rg.getMax_perimeter())));
			b1.add(new JLabel("Std  perimeter: " + nf.format(Math.sqrt(rg.getVar_perimeter()))));
			b1.add(new JLabel("Var  perimeter: " + nf.format(rg.getVar_perimeter())));
			b1.add(new JLabel(" "));
			b1.add(Box.createGlue());

			Box b2 = Box.createVerticalBox();
			b2.add(Box.createGlue());

			b2.add(new JLabel(" "));
			b2.add(new JLabel("Total compacity: " + nf.format(rg.getTotal_compacity())));
			b2.add(new JLabel("Mean  compacity: " + nf.format(rg.getMean_compacity())));
			b2.add(new JLabel("Min  compacity: " + nf.format(rg.getMin_compacity())));
			b2.add(new JLabel("Max  compacity: " + nf.format(rg.getMax_compacity())));
			b2.add(new JLabel("Std  compacity: " + nf.format(Math.sqrt(rg.getVar_compacity()))));
			b2.add(new JLabel("Var  compacity: " + nf.format(rg.getVar_compacity())));
			b2.add(new JLabel(" "));
			
			b2.add(new JLabel("Total complexity: " + nf.format(rg.getTotal_complexity())));
			b2.add(new JLabel("Mean  complexity: " + nf.format(rg.getMean_complexity())));
			b2.add(new JLabel("Min  complexity: " + nf.format(rg.getMin_complexity())));
			b2.add(new JLabel("Max  complexity: " + nf.format(rg.getMax_complexity())));
			b2.add(new JLabel("Std  complexity: " + nf.format(Math.sqrt(rg.getVar_complexity()))));
			b2.add(new JLabel("Var  complexity: " + nf.format(rg.getVar_complexity())));
			b2.add(new JLabel(" "));

			b2.add(new JLabel("Total simplexity: " + nf.format(rg.getTotal_simplexity())));
			b2.add(new JLabel("Mean  simplexity: " + nf.format(rg.getMean_simplexity())));
			b2.add(new JLabel("Min  simplexity: " + nf.format(rg.getMin_simplexity())));
			b2.add(new JLabel("Max  simplexity: " + nf.format(rg.getMax_simplexity())));
			b2.add(new JLabel("Std  simplexity: " + nf.format(Math.sqrt(rg.getVar_simplexity()))));
			b2.add(new JLabel("Var  simplexity: " + nf.format(rg.getVar_simplexity())));
			b2.add(new JLabel(" "));
			b2.add(Box.createGlue());

			getContentPane().add(b1, "West");
			getContentPane().add(b2, "East");
		}

		JPanel p2 = new JPanel();
		JButton ok = new JButton("Ok");
		p2.add(ok);
		getContentPane().add(p2, "South");

		ok.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
				{ setVisible(false); }
		} );

		setSize(400,620);
		setLocation(350,50);
	}
}

