package org.edisonwj.rapport;

/**
 * RDialog defines the dialog interactions for the various
 * object generation specifications.
 */

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

public class RDialog extends JDialog
	implements ActionListener, RapportDefaults
{
	JPanel pCommon, pUnique;

	JTextField Snumb_objects;
	JTextField Smax_attempts;
	JTextField Sseed;
	JTextField Sseed2;
	JTextField Sdebug;

	boolean initial;

	JCheckBox keepseedsBox;
	JCheckBox colorBox;
	JCheckBox displayBox;
	JCheckBox axesBox;
	JCheckBox unitsquareBox;

	JTextField Sstep;
	JTextField Spause;
	JTextField SpixelDim;
	JTextField SlogicalDim;
	JRadioButton centerRadioButton;
	JRadioButton lowleftRadioButton;

	boolean file;
	JButton fileButton;
	JTextField Sfname;
	File outputFile;

	boolean ok;
	JButton okButton;
	JButton cancelButton;

	RDialog(JFrame parent, String title)
	{
		super(parent, title, true);
		initial = true;
		buildCommon();
		buildUnique();
		Container contentPane = getContentPane();
		GridBagLayout gbl = new GridBagLayout();
		contentPane.setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0;
		gbc.weighty = 100;
		add(pUnique, gbc, 0, 0, 1, 1);
		add(pCommon, gbc, 0, 1, 1, 1);
   }

	public boolean showDialog(RInfo rinf)
	{
		setCommon(rinf);
		setUnique(rinf);
		ok = false;
	    show();
	    if (ok)
	    {
			getCommon(rinf);
			getUnique(rinf);
	    }
	    return ok;
	}

	public void buildUnique(){}

	public void setUnique(RInfo rinf){}

	public void getUnique(RInfo rinf){}

	public void buildCommon()
	{
		pCommon = new JPanel();
		pCommon.setLayout(new GridLayout(4,1));
		Border etched = BorderFactory.createEtchedBorder();

		JPanel p3 = new JPanel();
		Border titledGen = BorderFactory.createTitledBorder(etched, "Generation Control");
		p3.setBorder(titledGen);
		p3.setLayout(new GridLayout(3, 4, 3, 3));
		p3.add(new JLabel("Generations:"));
		p3.add(Snumb_objects = new JTextField(""));
		p3.add(new JLabel("Max. attempts:"));
		p3.add(Smax_attempts = new JTextField(""));
		p3.add(new JLabel("Random seed1:"));
		p3.add(Sseed = new JTextField(""));
		p3.add(new JLabel("Random seed2:"));
		p3.add(Sseed2 = new JTextField(""));
		p3.add(new JLabel("Debug:"));
		p3.add(Sdebug = new JTextField(""));
		keepseedsBox = new JCheckBox("Keep seeds");
		p3.add(keepseedsBox);
		colorBox = new JCheckBox("Color");
		p3.add(colorBox);
		
		pCommon.add(p3);

		JPanel p4 = new JPanel();
		Border titledDisp = BorderFactory.createTitledBorder(etched, "Display Parameters");
		p4.setBorder(titledDisp);
		p4.setLayout(new BorderLayout());
		JPanel p41 = new JPanel();
		p41.setLayout(new GridLayout(1,3));
		displayBox = new JCheckBox("Display");
		p41.add(displayBox);
		axesBox = new JCheckBox("Axes");
		p41.add(axesBox);
		unitsquareBox = new JCheckBox("Unit-square");
		p41.add(unitsquareBox);
		p4.add("North", p41);

		JPanel p42 = new JPanel();
		p42.setLayout(new GridLayout(2, 4, 4, 4));
		p42.add(new JLabel("Step:"));
		p42.add(Sstep = new JTextField(""));
		p42.add(new JLabel("Pause:"));
		p42.add(Spause = new JTextField(""));
		p42.add(new JLabel("FrameSize (Pixels):"));
		p42.add(SpixelDim = new JTextField(""));
		p42.add(new JLabel("Logical Dimension:"));
		p42.add(SlogicalDim = new JTextField(""));
		p4.add("Center",p42);

		JPanel p43 = new JPanel();
		p43.setLayout(new GridLayout(1,2));
		ButtonGroup group = new ButtonGroup();
		lowleftRadioButton = new JRadioButton("Origin Lower Left", true);
		group.add(lowleftRadioButton);
		p43.add(lowleftRadioButton);
		centerRadioButton = new JRadioButton("Origin Center", false);
		group.add(centerRadioButton);
		p43.add(centerRadioButton);
		p4.add("South",p43);
		pCommon.add(p4);

		JPanel p6 = new JPanel();
		Border titledFile = BorderFactory.createTitledBorder(etched, "File Parameters");
		p6.setBorder(titledFile);
		fileButton = addButton(p6, "File");
		p6.add(new JLabel("File name:"));
		p6.add(Sfname = new JTextField(24));
		pCommon.add(p6);

		Panel p7 = new Panel();
	   okButton = addButton(p7, "Ok");
	   cancelButton = addButton(p7, "Cancel");
	   pCommon.add(p7);
	}

	void setCommon(RInfo rinf)
	{
		if (initial || !rinf.keepseeds )
		{
			rinf.seed = Math.abs((int)(new Date().getTime()));
			rinf.Sseed = String.valueOf(rinf.seed);
			rinf.seed2 = Math.abs((int)(new Date().getTime()-777777));
			rinf.Sseed2 = String.valueOf(rinf.seed2);
			initial = false;
		}

		Snumb_objects.setText(rinf.Snumb_objects);
		Smax_attempts.setText(rinf.Smax_attempts);
		Sseed.setText(rinf.Sseed);
		Sseed2.setText(rinf.Sseed2);
		Sdebug.setText(rinf.Sdebug);

		keepseedsBox.setSelected(rinf.keepseeds);
		colorBox.setSelected(rinf.color);
		displayBox.setSelected(rinf.display);
		axesBox.setSelected(rinf.axes);
		unitsquareBox.setSelected(rinf.unitsquare);
		centerRadioButton.setSelected(rinf.center);
		lowleftRadioButton.setSelected(rinf.lowleft);
		
		Sstep.setText(rinf.Sstep);
		Spause.setText(rinf.Spause);
		SpixelDim.setText(rinf.SpixelDim);
		SlogicalDim.setText(rinf.SlogicalDim);

		file = false;
		Sfname.setText(rinf.Sfname);
	}

	void getCommon(RInfo rinf)
	{
		rinf.Snumb_objects = Snumb_objects.getText();
		rinf.Smax_attempts = Smax_attempts.getText();
		rinf.Sseed = Sseed.getText();
		rinf.Sseed2 = Sseed2.getText();
		rinf.Sdebug = Sdebug.getText();

		rinf.Sstep = Sstep.getText();
		rinf.Spause = Spause.getText();
		rinf.SpixelDim = SpixelDim.getText();
		rinf.SlogicalDim = SlogicalDim.getText();

		rinf.Sfname = Sfname.getText();

		rinf.numb_objects = Integer.parseInt(rinf.Snumb_objects);
		rinf.max_attempts = Integer.parseInt(rinf.Smax_attempts);
		rinf.seed = Long.parseLong(rinf.Sseed);
		rinf.seed2 = Long.parseLong(rinf.Sseed2);
		rinf.debug = Integer.parseInt(rinf.Sdebug);

		rinf.step = Integer.parseInt(rinf.Sstep);
		rinf.pause = Integer.parseInt(rinf.Spause);
		rinf.pixelDim = Integer.parseInt(rinf.SpixelDim);
		rinf.logicalDim = Double.parseDouble(rinf.SlogicalDim);

		rinf.keepseeds = keepseedsBox.isSelected();
		rinf.color = colorBox.isSelected();
		rinf.display = displayBox.isSelected();
		rinf.axes = axesBox.isSelected();
		rinf.unitsquare = unitsquareBox.isSelected();
		rinf.center = centerRadioButton.isSelected();
		rinf.lowleft = lowleftRadioButton.isSelected();
	}

	void add(Component c, GridBagConstraints gbc,
	      	int x, int y, int w, int h)
	{
	    gbc.gridx = x;
	    gbc.gridy = y;
	    gbc.gridwidth = w;
	    gbc.gridheight = h;
	    getContentPane().add(c, gbc);
    }

	JButton addButton(Container c, String name)
	{
		JButton button = new JButton(name);
		button.addActionListener(this);
		c.add(button);
		return button;
	}

   public void actionPerformed(ActionEvent evt)
   {  Object source = evt.getSource();
      if(source == okButton)
      {
			ok = true;
         setVisible(false);
      }
      else if (source == cancelButton)
         setVisible(false);
		else if (source == fileButton)
		{
			JFrame cf = new JFrame("Select Save File");
			JFileChooser fc = new JFileChooser();
			fc.setCurrentDirectory(new File("."));
			int result = fc.showSaveDialog(cf);
			if ( result == JFileChooser.APPROVE_OPTION )
				{
					file = true;
					outputFile = fc.getSelectedFile();
					Sfname.setText(outputFile.getPath());
				}
		}
   }
}

class ptDialog extends RDialog				/* Points */
{
	private boolean uniform;
	private boolean normal;
	private boolean poisson;

	private JRadioButton uniformRadioButton;
	private JRadioButton normalRadioButton;
	private JRadioButton poissonRadioButton;

	private JTextField Snum_items;
	private JTextField Snum_clusters;
	private JTextField Sstd_dev;
	private JTextField Sdensity;

	public ptDialog(JFrame parent)
	{
		super(parent, TITLE_POINT);
        setSize(500,600);
		setLocation(100,50);
	}

	public void buildUnique()
	{
		pUnique = new JPanel();
		Border etched = BorderFactory.createEtchedBorder();
		Border titledGenP = BorderFactory.createTitledBorder(etched, "Generation Parameters");
		pUnique.setBorder(titledGenP);
		pUnique.setLayout(new GridLayout(2,1));

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(1,2));
		ButtonGroup group = new ButtonGroup();
		uniformRadioButton = new JRadioButton("Uniform", false);
		group.add(uniformRadioButton);
		p1.add(uniformRadioButton);
		normalRadioButton = new JRadioButton("Normal", true);
		group.add(normalRadioButton);
		p1.add(normalRadioButton);
		poissonRadioButton = new JRadioButton("Poisson", false);
		group.add(poissonRadioButton);
		p1.add(poissonRadioButton);
		pUnique.add(p1);

        JPanel p2 = new JPanel();
        p2.setLayout(new GridLayout(2, 2, 4, 4));
        p2.add(new JLabel("Number of points:"));
        p2.add(Snum_items = new JTextField(""));
		p2.add(new JLabel("Number of clusters:"));
		p2.add(Snum_clusters = new JTextField(""));
        p2.add(new JLabel("Standard deviation:"));
        p2.add(Sstd_dev = new JTextField(""));
		p2.add(new JLabel("Density:"));
		p2.add(Sdensity = new JTextField(""));
		pUnique.add(p2);
	}

	public boolean showDialog(ptInfo rinf)
	{
		setCommon(rinf);
		setUnique(rinf);
		ok = false;
	    show();
	    if (ok)
	    {
			getCommon(rinf);
			getUnique(rinf);
	    }
	    return ok;
	}

	public void setUnique(ptInfo rinf)
	{
		rinf.title = this.getTitle();
		
		Snum_items.setText(rinf.Snum_items);
		Snum_clusters.setText(rinf.Snum_clusters);
		Sstd_dev.setText(rinf.Sstd_dev);
		Sdensity.setText(rinf.Sdensity);
	}

	public void getUnique(ptInfo rinf)
	{	
		rinf.Snum_items = Snum_items.getText();
		rinf.Snum_clusters = Snum_clusters.getText();
		rinf.Sstd_dev = Sstd_dev.getText();
		rinf.Sdensity = Sdensity.getText();

		rinf.num_items = Integer.parseInt(rinf.Snum_items);
		rinf.num_clusters = Integer.parseInt(rinf.Snum_clusters);
		rinf.std_dev = Double.parseDouble(rinf.Sstd_dev);
		rinf.density = Double.parseDouble(rinf.Sdensity);

		rinf.uniform = uniformRadioButton.isSelected();
		rinf.normal = normalRadioButton.isSelected();
		rinf.poisson = poissonRadioButton.isSelected();

		rinf.SdispInfo = "Num. points: " + rinf.Snum_items + ",  Num. clusters: " + rinf.Snum_clusters;
	}
}

class lnDialog extends RDialog				/* Lines */
{
	private JRadioButton uniformRadioButton;
	private JRadioButton normalRadioButton;
	private JRadioButton poissonRadioButton;

	private JTextField Snum_items;
	private JTextField Snum_clusters;
	private JTextField Sstd_dev;
	private JTextField Sdensity;

	public lnDialog(JFrame parent)
	{
		super(parent, TITLE_LINE);
     	setSize(500,600);
		setLocation(100,50);
	}

	public void buildUnique()
	{
		pUnique = new JPanel();
		Border etched = BorderFactory.createEtchedBorder();
		Border titledGenP = BorderFactory.createTitledBorder(etched, "Generation Parameters");
		pUnique.setBorder(titledGenP);
		pUnique.setLayout(new GridLayout(2,1));

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(1,2));
		ButtonGroup group = new ButtonGroup();
		uniformRadioButton = new JRadioButton("Uniform", false);
		group.add(uniformRadioButton);
		p1.add(uniformRadioButton);
		normalRadioButton = new JRadioButton("Normal", true);
		group.add(normalRadioButton);
		p1.add(normalRadioButton);
		poissonRadioButton = new JRadioButton("Poisson", false);
		group.add(poissonRadioButton);
		p1.add(poissonRadioButton);
		pUnique.add(p1);

     	JPanel p2 = new JPanel();
     	p2.setLayout(new GridLayout(2, 2, 4, 4));
     	p2.add(new JLabel("Number of lines:"));
     	p2.add(Snum_items = new JTextField(""));
		p2.add(new JLabel("Number of clusters:"));
		p2.add(Snum_clusters = new JTextField(""));
     	p2.add(new JLabel("Standard deviation:"));
     	p2.add(Sstd_dev = new JTextField(""));
		p2.add(new JLabel("Density:"));
		p2.add(Sdensity = new JTextField(""));
		pUnique.add(p2);
	}

	public boolean showDialog(lnInfo rinf)
	{
		setCommon(rinf);
		setUnique(rinf);
		ok = false;
	  	show();
	  	if (ok)
	  	{
			getCommon(rinf);
			getUnique(rinf);
	  	}
	  	return ok;
	}

	public void setUnique(lnInfo rinf)
	{
		rinf.title = this.getTitle();
		
		Snum_items.setText(rinf.Snum_items);
		Snum_clusters.setText(rinf.Snum_clusters);
		Sstd_dev.setText(rinf.Sstd_dev);
		Sdensity.setText(rinf.Sdensity);
	}

	public void getUnique(lnInfo rinf)
	{
		rinf.Snum_items = Snum_items.getText();
		rinf.Snum_clusters = Snum_clusters.getText();
		rinf.Sstd_dev = Sstd_dev.getText();
		rinf.Sdensity = Sdensity.getText();

		rinf.num_items = Integer.parseInt(rinf.Snum_items);
		rinf.num_clusters = Integer.parseInt(rinf.Snum_clusters);
		rinf.std_dev = Double.parseDouble(rinf.Sstd_dev);
		rinf.density = Double.parseDouble(rinf.Sdensity);

		rinf.uniform = uniformRadioButton.isSelected();
		rinf.normal = normalRadioButton.isSelected();
		rinf.poisson = poissonRadioButton.isSelected();

		rinf.SdispInfo = "Num. points: " + rinf.Snum_items + ",  Num. clusters: " + rinf.Snum_clusters;
	}
}

class lpDialog extends RDialog				/* PolyLines */
{
	private JCheckBox rotateCheckBox;
	private JCheckBox smooth1CheckBox;
	private JCheckBox smooth2CheckBox;
	private JCheckBox spareCheckBox;

	private JRadioButton xuniformRadioButton;
	private JRadioButton xnormalRadioButton;
	private JRadioButton xexponentialRadioButton;
	private JRadioButton xmmppRadioButton;

	private JRadioButton yuniformRadioButton;
	private JRadioButton ynormalRadioButton;
	private JRadioButton yexponentialRadioButton;
	private JRadioButton ymmppRadioButton;

	private JTextField Snum_items;
	private JTextField Ssmoothparm;
	private JTextField Sstd_dev;
	private JTextField Sdensity;

	public lpDialog(JFrame parent)
	{
		super(parent, TITLE_POLYLINE);
     	setSize(500,600);
		setLocation(100,50);
	}

	public void buildUnique()
	{
		pUnique = new JPanel();
		Border etched = BorderFactory.createEtchedBorder();
		Border titledGenP = BorderFactory.createTitledBorder(etched, "Generation Parameters");
		pUnique.setBorder(titledGenP);
		pUnique.setLayout(new GridLayout(2,1));

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(3,4));

		rotateCheckBox = new JCheckBox("Rotate", false);
		p1.add(rotateCheckBox);
		smooth1CheckBox = new JCheckBox("Smooth1", false);
		p1.add(smooth1CheckBox);
		smooth2CheckBox = new JCheckBox("Smooth2", false);
		p1.add(smooth2CheckBox);
		spareCheckBox = new JCheckBox("", false);
		p1.add(spareCheckBox);

		ButtonGroup group3 = new ButtonGroup();
		xuniformRadioButton = new JRadioButton("X-Uniform", true);
		group3.add(xuniformRadioButton);
		p1.add(xuniformRadioButton);
		xnormalRadioButton = new JRadioButton("X-Normal", false);
		group3.add(xnormalRadioButton);
		p1.add(xnormalRadioButton);
		xexponentialRadioButton = new JRadioButton("X-Exponential", false);
		group3.add(xexponentialRadioButton);
		p1.add(xexponentialRadioButton);
		xmmppRadioButton = new JRadioButton("X-MMPP", false);
		group3.add(xmmppRadioButton);
		p1.add(xmmppRadioButton);

		ButtonGroup group4 = new ButtonGroup();
		yuniformRadioButton = new JRadioButton("Y-Uniform", true);
		group4.add(yuniformRadioButton);
		p1.add(yuniformRadioButton);
		ynormalRadioButton = new JRadioButton("Y-Normal", false);
		group4.add(ynormalRadioButton);
		p1.add(ynormalRadioButton);
		yexponentialRadioButton = new JRadioButton("Y-Exponential", false);
		group4.add(yexponentialRadioButton);
		p1.add(yexponentialRadioButton);
		ymmppRadioButton = new JRadioButton("Y-MMPP", false);
		group4.add(ymmppRadioButton);
		p1.add(ymmppRadioButton);
		pUnique.add(p1);

     	JPanel p2 = new JPanel();
     	p2.setLayout(new GridLayout(4, 2, 4, 4));
     	p2.add(new JLabel("Max. number of points:"));
     	p2.add(Snum_items = new JTextField(""));
     	p2.add(new JLabel("Smooth parameter:"));
     	p2.add(Ssmoothparm = new JTextField(""));
     	p2.add(new JLabel("Standard deviation:"));
     	p2.add(Sstd_dev = new JTextField(""));
		p2.add(new JLabel("Density:"));
		p2.add(Sdensity = new JTextField(""));
		pUnique.add(p2);
	}

	public boolean showDialog(lpInfo rinf)
	{
		setCommon(rinf);
		setUnique(rinf);
		ok = false;
	  	show();
	  	if (ok)
	  	{
			getCommon(rinf);
			getUnique(rinf);
	  	}
	  	return ok;
	}

	public void setUnique(lpInfo rinf)
	{
		rinf.title = this.getTitle();
		
		Snum_items.setText(rinf.Snum_items);
		Ssmoothparm.setText(rinf.Ssmoothparm);
		Sstd_dev.setText(rinf.Sstd_dev);
		Sdensity.setText(rinf.Sdensity);
	}

	public void getUnique(lpInfo rinf)
	{
		rinf.Snum_items = Snum_items.getText();
		rinf.Ssmoothparm = Ssmoothparm.getText();
		rinf.Sstd_dev = Sstd_dev.getText();
		rinf.Sdensity = Sdensity.getText();

		rinf.num_items = Integer.parseInt(rinf.Snum_items);
		rinf.smoothparm = Double.parseDouble(rinf.Ssmoothparm);
		rinf.std_dev = Double.parseDouble(rinf.Sstd_dev);
		rinf.density = Double.parseDouble(rinf.Sdensity);

		rinf.rotate = rotateCheckBox.isSelected();
		rinf.smooth1 = smooth1CheckBox.isSelected();
		rinf.smooth2 = smooth2CheckBox.isSelected();
		rinf.spare = spareCheckBox.isSelected();

		rinf.xuniform = xuniformRadioButton.isSelected();
		rinf.xnormal = xnormalRadioButton.isSelected();
		rinf.xexponential = xexponentialRadioButton.isSelected();
		rinf.xmmpp = xmmppRadioButton.isSelected();

		rinf.yuniform = yuniformRadioButton.isSelected();
		rinf.ynormal = ynormalRadioButton.isSelected();
		rinf.yexponential = yexponentialRadioButton.isSelected();
		rinf.ymmpp = ymmppRadioButton.isSelected();

		rinf.SdispInfo = "Num. vertices: " + rinf.Snum_items + "\n"
							+ "Seed: " + rinf.Sseed + "\n"
							+ "Rotate: " + rinf.rotate
							+ " Smooth1: " + rinf.smooth1
							+ " Smooth2: " + rinf.smooth2
							+ " Spare: " + rinf.spare + "\n"
							+ "X-Uniform: " + rinf.xuniform
							+ " X-Normal: " + rinf.xnormal
							+ " X-Exponential: " + rinf.xexponential
							+ " X-MMPP: " + rinf.xmmpp + "\n"
							+ "Y-Uniform: " + rinf.yuniform
							+ " Y-Normal: " + rinf.ynormal
							+ " Y-Exponential: " + rinf.yexponential
							+ " Y-MMPP: " + rinf.ymmpp + "\n"
							+ "Smooth parm: " + rinf.smoothparm
							+ " Std. dev: " + rinf.std_dev + "\n"
							+ "Density: " + rinf.density;
	}
}

class rwDialog extends RDialog				/* Random Walk */
{
	private JRadioButton ARadioButton;
	private JRadioButton BRadioButton;
	private JRadioButton CRadioButton;

	private JRadioButton xuniformRadioButton;
	private JRadioButton xnormalRadioButton;
	private JRadioButton xexponentialRadioButton;

	private JRadioButton yuniformRadioButton;
	private JRadioButton ynormalRadioButton;
	private JRadioButton yexponentialRadioButton;

	private JTextField Snum_items;
	private JTextField Sxmin;
	private JTextField Symin;
	private JTextField Sxmax;
	private JTextField Symax;
	private JTextField Sstd_dev;

	public rwDialog(JFrame parent)
	{
		super(parent, TITLE_RANDOM_WALK);
     	setSize(500,600);
		setLocation(100,50);
	}

	public void buildUnique()
	{
		pUnique = new JPanel();
		Border etched = BorderFactory.createEtchedBorder();
		Border titledGenP = BorderFactory.createTitledBorder(etched, "Generation Parameters");
		pUnique.setBorder(titledGenP);
		pUnique.setLayout(new GridLayout(2,1));

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(3,3));

		ButtonGroup group1 = new ButtonGroup();
		ARadioButton = new JRadioButton("Rand. y incr.", true);
		group1.add(ARadioButton);
		p1.add(ARadioButton);
		BRadioButton = new JRadioButton("Rand. x and y", false);
		group1.add(BRadioButton);
		p1.add(BRadioButton);
		CRadioButton = new JRadioButton("MMPP x and y", false);
		group1.add(CRadioButton);
		p1.add(CRadioButton);

		ButtonGroup group3 = new ButtonGroup();
		xuniformRadioButton = new JRadioButton("X-Uniform", true);
		group3.add(xuniformRadioButton);
		p1.add(xuniformRadioButton);
		xnormalRadioButton = new JRadioButton("X-Normal", false);
		group3.add(xnormalRadioButton);
		p1.add(xnormalRadioButton);
		xexponentialRadioButton = new JRadioButton("X-Exponential", false);
		group3.add(xexponentialRadioButton);
		p1.add(xexponentialRadioButton);

		ButtonGroup group4 = new ButtonGroup();
		yuniformRadioButton = new JRadioButton("Y-Uniform", true);
		group4.add(yuniformRadioButton);
		p1.add(yuniformRadioButton);
		ynormalRadioButton = new JRadioButton("Y-Normal", false);
		group4.add(ynormalRadioButton);
		p1.add(ynormalRadioButton);
		yexponentialRadioButton = new JRadioButton("Y-Exponential", false);
		group4.add(yexponentialRadioButton);
		p1.add(yexponentialRadioButton);

		pUnique.add(p1);

     	JPanel p2 = new JPanel();
     	p2.setLayout(new GridLayout(6, 2, 4, 4));
     	p2.add(new JLabel("Number of points:"));
     	p2.add(Snum_items = new JTextField(""));
     	p2.add(new JLabel("Minimum X:"));
     	p2.add(Sxmin = new JTextField(""));
     	p2.add(new JLabel("Minimum Y:"));
     	p2.add(Symin = new JTextField(""));
    	p2.add(new JLabel("Maximum X:"));
     	p2.add(Sxmax = new JTextField(""));
     	p2.add(new JLabel("Maximum Y:"));
     	p2.add(Symax = new JTextField(""));
     	p2.add(new JLabel("Standard deviation:"));
     	p2.add(Sstd_dev = new JTextField(""));
		pUnique.add(p2);
	}

	public boolean showDialog(rwInfo rinf)
	{
		setCommon(rinf);
		setUnique(rinf);
		ok = false;
	  	show();
	  	if (ok)
	  	{
			getCommon(rinf);
			getUnique(rinf);
	  	}
	  	return ok;
	}

	public void setUnique(rwInfo rinf)
	{
		rinf.title = this.getTitle();
		
		Snum_items.setText(rinf.Snum_items);
		Sxmin.setText(rinf.Sxmin);
		Symin.setText(rinf.Symin);
		Sxmax.setText(rinf.Sxmax);
		Symax.setText(rinf.Symax);
		Sstd_dev.setText(rinf.Sstd_dev);
	}

	public void getUnique(rwInfo rinf)
	{
		rinf.Snum_items = Snum_items.getText();
		rinf.Sxmin = Sxmin.getText();
		rinf.Symin = Symin.getText();
		rinf.Sxmax = Sxmax.getText();
		rinf.Symax = Symax.getText();
		rinf.Sstd_dev = Sstd_dev.getText();

		rinf.num_items = Integer.parseInt(rinf.Snum_items);
		rinf.xmin = Double.parseDouble(rinf.Sxmin);
		rinf.ymin = Double.parseDouble(rinf.Symin);
		rinf.xmax = Double.parseDouble(rinf.Sxmax);
		rinf.ymax = Double.parseDouble(rinf.Symax);
		rinf.std_dev = Double.parseDouble(rinf.Sstd_dev);

		rinf.A = ARadioButton.isSelected();
		rinf.B = BRadioButton.isSelected();
		rinf.C = CRadioButton.isSelected();

		rinf.xuniform = xuniformRadioButton.isSelected();
		rinf.xnormal = xnormalRadioButton.isSelected();
		rinf.xexponential = xexponentialRadioButton.isSelected();

		rinf.yuniform = yuniformRadioButton.isSelected();
		rinf.ynormal = ynormalRadioButton.isSelected();
		rinf.yexponential = yexponentialRadioButton.isSelected();

		rinf.SdispInfo = "Num. vertices: " + rinf.Snum_items + "\n"
							+ "Seed: " + rinf.Sseed + "\n"
							+ "A: " + rinf.A
							+ " B: " + rinf.B
							+ " C: " + rinf.C + "\n"
							+ "X-Uniform: " + rinf.xuniform
							+ " X-Normal: " + rinf.xnormal
							+ " X-Exponential: " + rinf.xexponential
							+ "Y-Uniform: " + rinf.yuniform
							+ " Y-Normal: " + rinf.ynormal
							+ " Y-Exponential: " + rinf.yexponential + "\n"
							+ "xmin: " + rinf.xmin
							+ " ymin: " + rinf.ymin
							+ " xmax: " + rinf.xmax
							+ " ymax: " + rinf.ymax + "\n"
							+ " Std. dev: " + rinf.std_dev;
	}
}

class phDialog extends RDialog				/* Hull Polygons */
{
	private JRadioButton aRadioButton;
	private JRadioButton bRadioButton;
	private JRadioButton cRadioButton;
	private JRadioButton dRadioButton;
	private JRadioButton eRadioButton;
	private JRadioButton fRadioButton;

	private JTextField Snv;
	private JTextField Salpha;

	public phDialog(JFrame parent)
	{
		super(parent, TITLE_POLYGON_CONVEX_HULL);
		setSize(500,600);
		setLocation(100,50);
	}

	public void buildUnique()
	{
		pUnique = new JPanel();
		Border etched = BorderFactory.createEtchedBorder();
		Border titledGenP = BorderFactory.createTitledBorder(etched, "Generation Parameters");
		pUnique.setBorder(titledGenP);
		pUnique.setLayout(new GridLayout(2,2));

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(2,3));
		ButtonGroup group1 = new ButtonGroup();

		aRadioButton = new JRadioButton("Unit square", true);
		group1.add(aRadioButton);
		p1.add(aRadioButton);

		bRadioButton = new JRadioButton("Convex-stable sets", false);
		group1.add(bRadioButton);
		p1.add(bRadioButton);

		cRadioButton = new JRadioButton("Random disc", false);
		group1.add(cRadioButton);
		p1.add(cRadioButton);

		dRadioButton = new JRadioButton("Unit disc", false);
		group1.add(dRadioButton);
		p1.add(dRadioButton);

		eRadioButton = new JRadioButton("Incremental convex", false);
		group1.add(eRadioButton);
		p1.add(eRadioButton);

		fRadioButton = new JRadioButton("", false);
		group1.add(fRadioButton);
		p1.add(fRadioButton);

		pUnique.add(p1);

		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(2, 2, 4, 4));
		p2.add(new JLabel("Num. Rand. Points:"));
		p2.add(Snv = new JTextField(""));
		p2.add(new JLabel("Alpha:"));
		p2.add(Salpha = new JTextField(""));
		pUnique.add(p2);
	}

	public boolean showDialog(phInfo rinf)
	{
		setCommon(rinf);
		setUnique(rinf);
		ok = false;
	    show();
	    if (ok)
	    {
			getCommon(rinf);
			getUnique(rinf);
	    }
	    return ok;
	}

	public void setUnique(phInfo rinf)
	{
		rinf.title = this.getTitle();
		
		Snv.setText(rinf.Snv);
		Salpha.setText(rinf.Salpha);
	}

	public void getUnique(phInfo rinf)
	{
		rinf.a = aRadioButton.isSelected();
		rinf.b = bRadioButton.isSelected();
		rinf.c = cRadioButton.isSelected();
		rinf.d = dRadioButton.isSelected();
		rinf.e = eRadioButton.isSelected();
		rinf.f = fRadioButton.isSelected();

		rinf.Snv = Snv.getText();
		rinf.nv = Integer.parseInt(rinf.Snv);
		rinf.Salpha = Salpha.getText();
		rinf.alpha = Double.parseDouble(rinf.Salpha);

		rinf.SdispInfo =	"Num. Rand. Pts.: " + rinf.Snv + "\n" +
							"Alpha: " + rinf.Salpha + "\n" +
							"Seed: " + rinf.Sseed + "\n" +
							"a: " + rinf.a + "\n" +
							"b: " + rinf.b + "\n" +
							"c: " + rinf.c + "\n" +
							"d: " + rinf.d + "\n" +
							"e: " + rinf.e + "\n" +
							"f: " + rinf.f + "\n";
	}
}

class pfDialog extends RDialog				/* Random Point Polygons */
{
	private JRadioButton aRadioButton;
	private JRadioButton bRadioButton;
	private JRadioButton cRadioButton;
	private JRadioButton dRadioButton;
	private JRadioButton eRadioButton;
	private JRadioButton fRadioButton;

	private JTextField Snv;
	private JTextField Salpha;

	public pfDialog(JFrame parent)
	{
		super(parent, TITLE_POLYGON_RANDOM_POINTS);
		setSize(700,600);
		setLocation(100,50);
	}

	public void buildUnique()
	{
		pUnique = new JPanel();
		Border etched = BorderFactory.createEtchedBorder();
		Border titledGenP = BorderFactory.createTitledBorder(etched, "Generation Parameters");
		pUnique.setBorder(titledGenP);
		pUnique.setLayout(new GridLayout(2,2));

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(2,3));
		ButtonGroup group1 = new ButtonGroup();

		aRadioButton = new JRadioButton("Gen. by Enumeration Convex", true);
		group1.add(aRadioButton);
		p1.add(aRadioButton);

		bRadioButton = new JRadioButton("Gen. by Enumeration Simple", false);
		group1.add(bRadioButton);
		p1.add(bRadioButton);

		cRadioButton = new JRadioButton("Gen. Convex Mitchell", false);
		group1.add(cRadioButton);
		p1.add(cRadioButton);

		dRadioButton = new JRadioButton("Count Convex Mitchell", true);
		group1.add(dRadioButton);
		p1.add(dRadioButton);

		eRadioButton = new JRadioButton("Count Convex K-gons", false);
		group1.add(eRadioButton);
		p1.add(eRadioButton);

		fRadioButton = new JRadioButton("Conunt Convex Mitchell Improved", false);
		group1.add(fRadioButton);
		p1.add(fRadioButton);

		pUnique.add(p1);

		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(2, 2, 4, 4));
		p2.add(new JLabel("Num. Rand. Points:"));
		p2.add(Snv = new JTextField(""));
		p2.add(new JLabel("Alpha:"));
		p2.add(Salpha = new JTextField(""));
		pUnique.add(p2);
	}

	public boolean showDialog(pfInfo rinf)
	{
		setCommon(rinf);
		setUnique(rinf);
		ok = false;
	    show();   
	    if (ok)
	    {
			getCommon(rinf);
			getUnique(rinf);
	    }
	    return ok;
	}

	public void setUnique(pfInfo rinf)
	{
		rinf.title = this.getTitle();
		
		Snv.setText(rinf.Snv);
		Salpha.setText(rinf.Salpha);
	}

	public void getUnique(pfInfo rinf)
	{
		rinf.a = aRadioButton.isSelected();
		rinf.b = bRadioButton.isSelected();
		rinf.c = cRadioButton.isSelected();
		rinf.d = dRadioButton.isSelected();
		rinf.e = eRadioButton.isSelected();
		rinf.f = fRadioButton.isSelected();

		rinf.Snv = Snv.getText();
		rinf.nv = Integer.parseInt(rinf.Snv);
		rinf.Salpha = Salpha.getText();
		rinf.alpha = Double.parseDouble(rinf.Salpha);

		rinf.SdispInfo =	"Num. Rand. Pts.: " + rinf.Snv + "\n" +
							"Alpha: " + rinf.Salpha + "\n" +
							"Seed: " + rinf.Sseed + "\n" +
							"a: " + rinf.a + "\n" +
							"b: " + rinf.b + "\n" +
							"c: " + rinf.c + "\n" +
							"d: " + rinf.d + "\n" +
							"e: " + rinf.e + "\n" +
							"f: " + rinf.f + "\n";
	}
}

class elDialog extends RDialog				/* Ellipses/Circles */
{
	private JRadioButton circleRadioButton;
	private JRadioButton ellipseRadioButton;
	private JRadioButton offsetRadioButton;
	private JRadioButton trimRadioButton;

	private JTextField Sminlen;
	private JTextField Smaxlen;

	public elDialog(JFrame parent)
   {
		super(parent, TITLE_CIRCLE_ELLIPSE);
		setSize(500,600);
		setLocation(100,50);
   }

	public void buildUnique()
	{
		pUnique = new JPanel();
		Border etched = BorderFactory.createEtchedBorder();
		Border titledGenP = BorderFactory.createTitledBorder(etched, "Generation Parameters");
		pUnique.setBorder(titledGenP);
		pUnique.setLayout(new GridLayout(3,1));

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(1,2));
		ButtonGroup group1 = new ButtonGroup();
		circleRadioButton = new JRadioButton("Circle", true);
		group1.add(circleRadioButton);
		p1.add(circleRadioButton);
		ellipseRadioButton = new JRadioButton("Ellipse", false);
		group1.add(ellipseRadioButton);
		p1.add(ellipseRadioButton);
		pUnique.add(p1);

		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(1,2));
		ButtonGroup group2 = new ButtonGroup();
		offsetRadioButton = new JRadioButton("Offset", false);
		group2.add(offsetRadioButton);
		p2.add(offsetRadioButton);
		trimRadioButton = new JRadioButton("Trim", true);
		group2.add(trimRadioButton);
		p2.add(trimRadioButton);
		pUnique.add(p2);

		JPanel p3 = new JPanel();
		p3.setLayout(new GridLayout(2, 4, 4, 4));
		p3.add(new JLabel("Minimum Length:"));
		p3.add(Sminlen = new JTextField(""));
		p3.add(new JLabel("Maximum Length:"));
		p3.add(Smaxlen = new JTextField(""));
		pUnique.add(p3);
	}

	public boolean showDialog(elInfo rinf)
	{
		setCommon(rinf);
		setUnique(rinf);
		ok = false;
	    show();
	    if (ok)
	    {
			getCommon(rinf);
			getUnique(rinf);
	    }
	    return ok;
	}

	public void setUnique(elInfo rinf)
	{
		rinf.title = this.getTitle();
		
		Sminlen.setText(rinf.Sminlen);
		Smaxlen.setText(rinf.Smaxlen);
	}

	public void getUnique(elInfo rinf)
	{
		rinf.circle = circleRadioButton.isSelected();
		rinf.ellipse = ellipseRadioButton.isSelected();
		rinf.offset = offsetRadioButton.isSelected();
		rinf.trim = trimRadioButton.isSelected();

		rinf.Sminlen = Sminlen.getText();
		rinf.Smaxlen = Smaxlen.getText();
		rinf.minlen = Double.parseDouble(rinf.Sminlen);
		rinf.maxlen = Double.parseDouble(rinf.Smaxlen);

		rinf.SdispInfo =	"Circle: " + rinf.circle +
							"  Ellipse: " + rinf.ellipse + "\n" +
							"Offset: " + rinf.offset +
							"  Trim: " + rinf.trim + "\n" +
							"Min. length: " + rinf.Sminlen + "\n" +
							"Max. length: " + rinf.Smaxlen + "\n" ;
	}
}

class rcDialog extends RDialog				/* Rectangles */
{
	private JRadioButton cornersRadioButton;
	private JRadioButton centerextRadioButton;
	private JRadioButton cornerextRadioButton;
	private JRadioButton spareRadioButton;
	private JRadioButton forceRadioButton;
	private JRadioButton wrapRadioButton;

	private JTextField Sminlen;
	private JTextField Smaxlen;

	public rcDialog(JFrame parent)
   {
		super(parent, TITLE_RECTANGLE);
		setSize(500,600);
		setLocation(100,50);
   }

	public void buildUnique()
	{
		pUnique = new JPanel();
		Border etched = BorderFactory.createEtchedBorder();
		Border titledGenP = BorderFactory.createTitledBorder(etched, "Generation Parameters");
		pUnique.setBorder(titledGenP);
		pUnique.setLayout(new GridLayout(3,1));

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(1,2));
		ButtonGroup group1 = new ButtonGroup();
		cornersRadioButton = new JRadioButton("Corners", true);
		group1.add(cornersRadioButton);
		p1.add(cornersRadioButton);
		centerextRadioButton = new JRadioButton("Center/Extents", false);
		group1.add(centerextRadioButton);
		p1.add(centerextRadioButton);
		cornerextRadioButton = new JRadioButton("Corner/Extents", false);
		group1.add(cornerextRadioButton);
		p1.add(cornerextRadioButton);
		pUnique.add(p1);

		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(1,2));
		ButtonGroup group2 = new ButtonGroup();
		spareRadioButton = new JRadioButton("", true);
		group2.add(spareRadioButton);
		p2.add(spareRadioButton);
		forceRadioButton = new JRadioButton("Force", false);
		group2.add(forceRadioButton);
		p2.add(forceRadioButton);
		wrapRadioButton = new JRadioButton("Wrap-around", false);
		group2.add(wrapRadioButton);
		p2.add(wrapRadioButton);
		pUnique.add(p2);

		JPanel p3 = new JPanel();
		p3.setLayout(new GridLayout(2, 4, 4, 4));
		p3.add(new JLabel("Minimum Side Length:"));
		p3.add(Sminlen = new JTextField(""));
		p3.add(new JLabel("Maximum Side Length:"));
		p3.add(Smaxlen = new JTextField(""));
		pUnique.add(p3);
	}

	public boolean showDialog(rcInfo rinf)
	{
		setCommon(rinf);
		setUnique(rinf);
		ok = false;
	    show();
	    if (ok)
	    {
			getCommon(rinf);
			getUnique(rinf);
	    }
	    return ok;
	}

	public void setUnique(rcInfo rinf)
	{
		rinf.title = this.getTitle();
		
		Sminlen.setText(rinf.Sminlen);
		Smaxlen.setText(rinf.Smaxlen);
	}

	public void getUnique(rcInfo rinf)
	{
		rinf.corners = cornersRadioButton.isSelected();
		rinf.centerext = centerextRadioButton.isSelected();
		rinf.cornerext = cornerextRadioButton.isSelected();
		rinf.wrap = wrapRadioButton.isSelected();
		rinf.force = forceRadioButton.isSelected();

		rinf.Sminlen = Sminlen.getText();
		rinf.Smaxlen = Smaxlen.getText();
		rinf.minlen = Double.parseDouble(rinf.Sminlen);
		rinf.maxlen = Double.parseDouble(rinf.Smaxlen);

		rinf.SdispInfo = 	"Min. side length: " + rinf.Sminlen + "\n" +
							"Max. side length: " + rinf.Smaxlen + "\n" +
							"Corners: " + rinf.corners + "\n" +
							"Center: " + rinf.center + "\n" +
							"Corner/Ext: " + rinf.cornerext + "\n" +
							"Wrap: " + rinf.wrap +
							", Force: " + rinf.force;
	}
}

class pcDialog extends RDialog				/* Cell Polygon */
{
	private JRadioButton uniformRadioButton;
	private JRadioButton exponentialRadioButton;
	private JRadioButton mixedRadioButton;

	private JTextField Snum_poly;
	private JTextField Snum_cells;

	public pcDialog(JFrame parent)
	{
		super(parent, TITLE_POLYGON_CELL);
     	setSize(500,600);
		setLocation(100,50);
	}

	public void buildUnique()
	{
		pUnique = new JPanel();
		Border etched = BorderFactory.createEtchedBorder();
		Border titledGenP = BorderFactory.createTitledBorder(etched, "Generation Parameters");
		pUnique.setBorder(titledGenP);
		pUnique.setLayout(new GridLayout(2,1));

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(1,3));

		ButtonGroup group2 = new ButtonGroup();
		uniformRadioButton = new JRadioButton("Uniform", true);
		group2.add(uniformRadioButton);
		p1.add(uniformRadioButton);
		exponentialRadioButton = new JRadioButton("Exponential", false);
		group2.add(exponentialRadioButton);
		p1.add(exponentialRadioButton);
		mixedRadioButton = new JRadioButton("Mixed", false);
		group2.add(mixedRadioButton);
		p1.add(mixedRadioButton);
		pUnique.add(p1);

     	JPanel p2 = new JPanel();
     	p2.setLayout(new GridLayout(1, 2, 4, 4));
     	p2.add(new JLabel("Cell row size:"));
     	p2.add(Snum_cells = new JTextField(""));
		pUnique.add(p2);
	}

	public boolean showDialog(pcInfo rinf)
	{
		setCommon(rinf);
		setUnique(rinf);
		ok = false;
	  	show();
	  	if (ok)
	  	{
			getCommon(rinf);
			getUnique(rinf);
	  	}
	  	return ok;
	}

	public void setUnique(pcInfo rinf)
	{
		rinf.title = this.getTitle();
		
		Snum_cells.setText(rinf.Snum_cells);
	}

	public void getUnique(pcInfo rinf)
	{
		rinf.Snum_cells = Snum_cells.getText();
		rinf.num_cells = Integer.parseInt(rinf.Snum_cells);

		rinf.uniform = uniformRadioButton.isSelected();
		rinf.exponential = exponentialRadioButton.isSelected();
		rinf.mixed = mixedRadioButton.isSelected();

		rinf.SdispInfo = "Num. cells: " + rinf.Snum_cells
							+ ", Cell row size: " + rinf.Snum_cells
							+ ", Seed1: " + rinf.Sseed
							+ ", Seed2: " + rinf.Sseed2
							+ "\nUniform: " + rinf.uniform
							+ ", Exponential: " + rinf.exponential
							+ ", Mixed: " + rinf.mixed;
	}
}

class poDialog extends RDialog				/* Monotone Polygons */
{
	private JCheckBox randomBox;

	private JTextField Snv;

	public poDialog(JFrame parent)
	{
		super(parent, TITLE_POLYGON_MONOTONE);
		setSize(500,600);
		setLocation(100,50);
	}

	public void buildUnique()
	{
		pUnique = new JPanel();
		Border etched = BorderFactory.createEtchedBorder();
		Border titledGenP = BorderFactory.createTitledBorder(etched, "Generation Parameters");
		pUnique.setBorder(titledGenP);
		pUnique.setLayout(new GridLayout(2,1));

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(1,1));
		randomBox = new JCheckBox("Randomize number of vertices");
		p1.add(randomBox);
		pUnique.add(p1);

		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(1, 2, 4, 4));
		p2.add(new JLabel("Max. number of vertices:"));
		p2.add(Snv = new JTextField(""));

		pUnique.add(p2);
	}

	public boolean showDialog(poInfo rinf)
	{
		setCommon(rinf);
		setUnique(rinf);
		ok = false;
	   show();
	   if (ok)
	   {
			getCommon(rinf);
			getUnique(rinf);
	   }
	   return ok;
	}

	public void setUnique(poInfo rinf)
	{
		rinf.title = this.getTitle();
		
		randomBox.setSelected(rinf.random);
		Snv.setText(rinf.Snv);
	}

	public void getUnique(poInfo rinf)
	{	
		rinf.Snv = Snv.getText();
		rinf.nv = Integer.parseInt(rinf.Snv);

		rinf.random = randomBox.isSelected();

		rinf.SdispInfo = "Num. vertices: " + rinf.Snv + ",  Seed: " + rinf.Sseed
								+ "\nrandom: " + rinf.random;
	}
}

class psDialog extends RDialog				/* Star Polygon */
{
	private boolean polar;
	private boolean points;

	private boolean angle_unif;
	private boolean angle_exp;
	private boolean angle_mmpp;
	private boolean radial_unif;
	private boolean radial_exp;

	private boolean smooth1;
	private boolean smooth2;
	private boolean nosmooth;

	private boolean angsum;
	private boolean bounded;
	private boolean random;

	private JRadioButton pointsRadioButton;
	private JRadioButton polarRadioButton;

	private JRadioButton angle_unifRadioButton;
	private JRadioButton angle_expRadioButton;
	private JRadioButton angle_mmppRadioButton;

	private JRadioButton radial_unifRadioButton;
	private JRadioButton radial_expRadioButton;

	private JRadioButton smooth1RadioButton;
	private JRadioButton smooth2RadioButton;
	private JRadioButton nosmoothRadioButton;

	private JCheckBox angsumBox;
	private JCheckBox boundedBox;
	private JCheckBox randomBox;

	private JTextField Snv;
	private JTextField Sdensity;
	private JTextField Ssmooth_factor;
	private JTextField Sradial_exp_rate;
	private JTextField Smarkov_param;

	public psDialog(JFrame parent)
	{
		super(parent, TITLE_POLYGON_STAR);
    	setSize(500,800);
		setLocation(100,50);
	}

	public void buildUnique()
	{
		pUnique = new JPanel();
		Border etched = BorderFactory.createEtchedBorder();
		Border titledGenP = BorderFactory.createTitledBorder(etched, "Generation Parameters");
		pUnique.setBorder(titledGenP);
		pUnique.setLayout(new GridLayout(3,1));

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(3,3));
		ButtonGroup group0 = new ButtonGroup();
		polarRadioButton = new JRadioButton("Polar angle", true);
		group0.add(polarRadioButton);
		p1.add(polarRadioButton);
		pointsRadioButton = new JRadioButton("Random points", true);
		group0.add(pointsRadioButton);
		p1.add(pointsRadioButton);
		JRadioButton nullRadioButton = new JRadioButton("", false);
		group0.add(nullRadioButton);
		p1.add(nullRadioButton);

		ButtonGroup group1 = new ButtonGroup();
		angle_unifRadioButton = new JRadioButton("Uniform angle", true);
		group1.add(angle_unifRadioButton);
		p1.add(angle_unifRadioButton);
		angle_expRadioButton = new JRadioButton("Exponential angle", true);
		group1.add(angle_expRadioButton);
		p1.add(angle_expRadioButton);
		angle_mmppRadioButton = new JRadioButton("Markov angle", false);
		group1.add(angle_mmppRadioButton);
		p1.add(angle_mmppRadioButton);
		ButtonGroup group2 = new ButtonGroup();
		radial_unifRadioButton = new JRadioButton("Uniform radius", true);
		group2.add(radial_unifRadioButton);
		p1.add(radial_unifRadioButton);
		radial_expRadioButton = new JRadioButton("Exponential radius", false);
		group2.add(radial_expRadioButton);
		p1.add(radial_expRadioButton);
		pUnique.add(p1);

		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(2,3));
		ButtonGroup group3 = new ButtonGroup();
		smooth1RadioButton = new JRadioButton("Smooth1 radius", true);
		group3.add(smooth1RadioButton);
		p2.add(smooth1RadioButton);
		smooth2RadioButton = new JRadioButton("Smooth2 radius", false);
		group3.add(smooth2RadioButton);
		p2.add(smooth2RadioButton);
		nosmoothRadioButton = new JRadioButton("No Smooth", false);
		group3.add(nosmoothRadioButton);
		p2.add(nosmoothRadioButton);
		angsumBox = new JCheckBox("Angle Sum");
		p2.add(angsumBox);
		boundedBox = new JCheckBox("Bounded");
		p2.add(boundedBox);
		randomBox = new JCheckBox("Randomize #");
		p2.add(randomBox);
		pUnique.add(p2);

      	JPanel p3 = new JPanel();
      	p3.setLayout(new GridLayout(5, 2, 4, 4));
      	p3.add(new JLabel("Number of vertices:"));
      	p3.add(Snv = new JTextField(""));
		p3.add(new JLabel("Density:"));
		p3.add(Sdensity = new JTextField(""));
		p3.add(new JLabel("Smooth_factor (divisor of Pi):"));
		p3.add(Ssmooth_factor = new JTextField(""));
		p3.add(new JLabel("Radial exp. rate:"));
		p3.add(Sradial_exp_rate = new JTextField(""));
		p3.add(new JLabel("Markov_param:"));
		p3.add(Smarkov_param = new JTextField(""));
		pUnique.add(p3);
	}

	public boolean showDialog(psInfo rinf)
	{
		setCommon(rinf);
		setUnique(rinf);
		ok = false;
	   show();
	   if (ok)
	   {
			getCommon(rinf);
			getUnique(rinf);
	   }
	   return ok;
	}

	public void setUnique(psInfo rinf)
	{
		rinf.title = this.getTitle();
		
		Snv.setText(rinf.Snv);
		Sdensity.setText(rinf.Sdensity);
		Ssmooth_factor.setText(rinf.Ssmooth_factor);
		Sradial_exp_rate.setText(rinf.Sradial_exp_rate);
		Smarkov_param.setText(rinf.Smarkov_param);

		angsumBox.setSelected(rinf.angsum);
		boundedBox.setSelected(rinf.bounded);
		randomBox.setSelected(rinf.random);
	}

	public void getUnique(psInfo rinf)
	{
		rinf.polar = polarRadioButton.isSelected();
		rinf.points = pointsRadioButton.isSelected();

		rinf.angle_unif = angle_unifRadioButton.isSelected();
		rinf.angle_exp = angle_expRadioButton.isSelected();
		rinf.angle_mmpp = angle_mmppRadioButton.isSelected();

		rinf.radial_unif = radial_unifRadioButton.isSelected();
		rinf.radial_exp = radial_expRadioButton.isSelected();

		rinf.smooth1 = smooth1RadioButton.isSelected();
		rinf.smooth2 = smooth2RadioButton.isSelected();
		rinf.nosmooth = nosmoothRadioButton.isSelected();

		rinf.angsum = angsumBox.isSelected();
		rinf.bounded = boundedBox.isSelected();
		rinf.random = randomBox.isSelected();

		rinf.Snv = Snv.getText();
		rinf.Sdensity = Sdensity.getText();
		rinf.Ssmooth_factor = Ssmooth_factor.getText();
		rinf.Sradial_exp_rate = Sradial_exp_rate.getText();
		rinf.Smarkov_param = Smarkov_param.getText();

		rinf.nv = Integer.parseInt(rinf.Snv);
		rinf.density = Double.parseDouble(rinf.Sdensity);
		rinf.smooth_factor = Double.parseDouble(rinf.Ssmooth_factor);
//		rinf.smooth_factor = Math.PI / Double.parseDouble(rinf.Ssmooth_factor);
		rinf.radial_exp_rate = Double.parseDouble(rinf.Sradial_exp_rate);
		rinf.markov_param = Double.parseDouble(rinf.Smarkov_param);

		rinf.SdispInfo = "Num. vertices: " + rinf.Snv + "\n"
								+ "Seed: " + rinf.Sseed + "\n"
								+ "Randomize #: " + rinf.random + "\n"
								+ "Points: " + rinf.points + "\n"
								+ "Polar: " + rinf.polar + "\n"
								+ "Angle_unif: " + rinf.angle_unif + "\n"
								+ "Angle_exp: " + rinf.angle_exp + "\n"
								+ "Angle_mmpp: " + rinf.angle_mmpp + "\n"
								+ "Radial_unif: " + rinf.radial_unif + "\n"
								+ "Radial_exp: " + rinf.radial_exp + "\n"
								+ "Angsum: " + rinf.angsum + ",  Smooth_factor: " + rinf.Ssmooth_factor + "\n"
								+ "Bounded: " + rinf.bounded + ",  Radial_exp_rate: " + rinf.Sradial_exp_rate + "\n"
								+ "Smooth1: " + rinf.smooth1 + "\n"
								+ "Smooth2: " + rinf.smooth2 + "\n"
								+ "Markov_param: " + rinf.markov_param + "\n"
								+ "Density: " + rinf.density;
   }
}

class trDialog extends RDialog				/* Spiral Polygon */
{
	private boolean bounded;
	private boolean hash;
	private boolean random;

	private JCheckBox boundedBox;
	private JCheckBox hashBox;
	private JCheckBox randomBox;

	private JTextField Snv;
	private JTextField Sdensity;
	private JTextField Ssmooth_factor;
	private JTextField Sradial_exp_rate;

	public trDialog(JFrame parent)
	{
		super(parent, TITLE_POLYGON_SPIRAL);
		setSize(500,600);
		setLocation(100,50);
	}

	public void buildUnique()
	{
		pUnique = new JPanel();
		Border etched = BorderFactory.createEtchedBorder();
		Border titledGenP = BorderFactory.createTitledBorder(etched, "Generation Parameters");
		pUnique.setBorder(titledGenP);
		pUnique.setLayout(new GridLayout(2,1));

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(1,3));
		boundedBox = new JCheckBox("Bounded");
		p1.add(boundedBox);
		hashBox = new JCheckBox("Hash");
		p1.add(hashBox);
		randomBox = new JCheckBox("Randomize #");
		p1.add(randomBox);
		pUnique.add(p1);

		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(1, 2, 4, 4));
		p2.add(new JLabel("Number of vertices:"));
		p2.add(Snv = new JTextField(""));
		p2.add(new JLabel("Radial exp. rate:"));
		p2.add(Sradial_exp_rate = new JTextField(""));
		pUnique.add(p2);
	}

	public boolean showDialog(trInfo rinf)
	{
		setCommon(rinf);
		setUnique(rinf);
		ok = false;
	   show();
	   if (ok)
	   {
			getCommon(rinf);
			getUnique(rinf);
	   }
	   return ok;
	}

	public void setUnique(trInfo rinf)
	{
		rinf.title = this.getTitle();
		
		Snv.setText(rinf.Snv);
		Sradial_exp_rate.setText(rinf.Sradial_exp_rate);

		boundedBox.setSelected(rinf.bounded);
		hashBox.setSelected(rinf.hash);
		randomBox.setSelected(rinf.random);
	}

	public void getUnique(trInfo rinf)
	{
		rinf.Snv = Snv.getText();
		rinf.Sradial_exp_rate = Sradial_exp_rate.getText();

		rinf.nv = Integer.parseInt(rinf.Snv);
		rinf.radial_exp_rate = Double.parseDouble(rinf.Sradial_exp_rate);

		rinf.bounded = boundedBox.isSelected();
		rinf.hash = hashBox.isSelected();
		rinf.random = randomBox.isSelected();

		rinf.SdispInfo = "Num. vertices: " + rinf.Snv + ",  Seed: " + rinf.Sseed
								+ "\nBounded: " + rinf.bounded
								+ "\nHash: " + rinf.hash
								+ "\nRandom: " + rinf.random
								+ "\nRadial_exp_rate: " + rinf.Sradial_exp_rate;
	}
}

class cmDialog extends RDialog				/* Cell PolyMap */
{
	private JRadioButton uniformRadioButton;
	private JRadioButton exponentialRadioButton;
	private JRadioButton mixedRadioButton;

	private JTextField Snum_poly;
	private JTextField Snum_cells;

	public cmDialog(JFrame parent)
	{
		super(parent, TITLE_POLYMAP_CELL);
     	setSize(500,600);
		setLocation(100,50);
	}

	public void buildUnique()
	{
		pUnique = new JPanel();
		Border etched = BorderFactory.createEtchedBorder();
		Border titledGenP = BorderFactory.createTitledBorder(etched, "Generation Parameters");
		pUnique.setBorder(titledGenP);
		pUnique.setLayout(new GridLayout(2,1));

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(1,3));
		ButtonGroup group2 = new ButtonGroup();
		uniformRadioButton = new JRadioButton("Uniform", true);
		group2.add(uniformRadioButton);
		p1.add(uniformRadioButton);
		exponentialRadioButton = new JRadioButton("Exponential", false);
		group2.add(exponentialRadioButton);
		p1.add(exponentialRadioButton);
		mixedRadioButton = new JRadioButton("Mixed", false);
		group2.add(mixedRadioButton);
		p1.add(mixedRadioButton);

		pUnique.add(p1);

     	JPanel p2 = new JPanel();
     	p2.setLayout(new GridLayout(2, 2, 4, 4));
     	p2.add(new JLabel("Number of polygons:"));
     	p2.add(Snum_poly = new JTextField(""));
     	p2.add(new JLabel("Cell row size:"));
     	p2.add(Snum_cells = new JTextField(""));
		pUnique.add(p2);
	}

	public boolean showDialog(cmInfo rinf)
	{
		setCommon(rinf);
		setUnique(rinf);
		ok = false;
	  	show();
	  	if (ok)
	  	{
			getCommon(rinf);
			getUnique(rinf);
	  	}
	  	return ok;
	}

	public void setUnique(cmInfo rinf)
	{
		rinf.title = this.getTitle();
		
		Snum_poly.setText(rinf.Snum_poly);
		Snum_cells.setText(rinf.Snum_cells);
	}

	public void getUnique(cmInfo rinf)
	{	
		rinf.uniform = uniformRadioButton.isSelected();
		rinf.exponential = exponentialRadioButton.isSelected();
		rinf.mixed = mixedRadioButton.isSelected();
		
		rinf.Snum_poly = Snum_poly.getText();
		rinf.Snum_cells = Snum_cells.getText();

		rinf.num_poly = Integer.parseInt(rinf.Snum_poly);
		rinf.num_cells = Integer.parseInt(rinf.Snum_cells);

		rinf.SdispInfo = "Num. poly: " + rinf.Snum_poly
							+ ", Cell row size: " + rinf.Snum_cells
							+ ", Seed1: " + rinf.Sseed
							+ ", Seed2: " + rinf.Sseed2
							+ "\nUniform: " + rinf.uniform
							+ ", Exponential: " + rinf.exponential
							+ ", Mixed: " + rinf.mixed;
	}
}

class pmDialog extends RDialog				/* Poisson Polymap */
{
	private JTextField Salpha;
	private JTextField Snum_items;

	public pmDialog(JFrame parent)
	{
		super(parent, TITLE_POLYMAP_POISSON);
     	setSize(500,600);
		setLocation(100,50);
	}

	public void buildUnique()
	{
		pUnique = new JPanel();
		Border etched = BorderFactory.createEtchedBorder();
		Border titledGenP = BorderFactory.createTitledBorder(etched, "Generation Parameters");
		pUnique.setBorder(titledGenP);
		pUnique.setLayout(new GridLayout(1,1));

     	JPanel p1 = new JPanel();
     	p1.setLayout(new GridLayout(2, 2, 4, 4));
     	p1.add(new JLabel("Alpha:"));
     	p1.add(Salpha = new JTextField(""));
     	p1.add(new JLabel("Number of points:"));
     	p1.add(Snum_items = new JTextField(""));
		pUnique.add(p1);
	}

	public boolean showDialog(pmInfo rinf)
	{
		setCommon(rinf);
		setUnique(rinf);
		ok = false;
	  	show();
	  	if (ok)
	  	{
			getCommon(rinf);
			getUnique(rinf);
	  	}
	  	return ok;
	}

	public void setUnique(pmInfo rinf)
	{
		rinf.title = this.getTitle();
		
		Salpha.setText(rinf.Salpha);
		Snum_items.setText(rinf.Snum_items);
	}

	public void getUnique(pmInfo rinf)
	{	
		rinf.Salpha = Salpha.getText();
		rinf.Snum_items = Snum_items.getText();

		rinf.alpha = Double.parseDouble(rinf.Salpha);
		rinf.num_items = Integer.parseInt(rinf.Snum_items);

		rinf.SdispInfo = "Alpha: " + rinf.Salpha
				+ ", Num. points: " + rinf.Snum_items
				+ ", Seed1: " + rinf.Sseed
				+ ", Seed2: " + rinf.Sseed2;
	}
}

class vmDialog extends RDialog				/* Voronoi Polymap */
{
	private boolean uniform;
	private boolean normal;
	private boolean poisson;

	private JRadioButton uniformRadioButton;
	private JRadioButton normalRadioButton;
	private JRadioButton poissonRadioButton;

	private JTextField Snum_items;
	private JTextField Snum_clusters;
	private JTextField Sstd_dev;
	private JTextField Sdensity;

	public vmDialog(JFrame parent)
	{
		super(parent, TITLE_POLYMAP_VORONOI);
        setSize(500,600);
		setLocation(100,50);
	}

	public void buildUnique()
	{
		pUnique = new JPanel();
		Border etched = BorderFactory.createEtchedBorder();
		Border titledGenP = BorderFactory.createTitledBorder(etched, "Generation Parameters");
		pUnique.setBorder(titledGenP);
		pUnique.setLayout(new GridLayout(2,1));

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(1,2));
		ButtonGroup group = new ButtonGroup();
		uniformRadioButton = new JRadioButton("Uniform", false);
		group.add(uniformRadioButton);
		p1.add(uniformRadioButton);
		normalRadioButton = new JRadioButton("Normal", true);
		group.add(normalRadioButton);
		p1.add(normalRadioButton);
		poissonRadioButton = new JRadioButton("Poisson", false);
		group.add(poissonRadioButton);
		p1.add(poissonRadioButton);
		pUnique.add(p1);

        JPanel p2 = new JPanel();
        p2.setLayout(new GridLayout(2, 2, 4, 4));
        p2.add(new JLabel("Number of points:"));
        p2.add(Snum_items = new JTextField(""));
		p2.add(new JLabel("Number of clusters:"));
		p2.add(Snum_clusters = new JTextField(""));
        p2.add(new JLabel("Standard deviation:"));
        p2.add(Sstd_dev = new JTextField(""));
		p2.add(new JLabel("Density:"));
		p2.add(Sdensity = new JTextField(""));
		pUnique.add(p2);
	}

	public boolean showDialog(vmInfo rinf)
	{
		setCommon(rinf);
		setUnique(rinf);
		ok = false;
	    show();
	    if (ok)
	    {
			getCommon(rinf);
			getUnique(rinf);
	    }
	    return ok;
	}

	public void setUnique(vmInfo rinf)
	{
		rinf.title = this.getTitle();
		
		Snum_items.setText(rinf.Snum_items);
		Snum_clusters.setText(rinf.Snum_clusters);
		Sstd_dev.setText(rinf.Sstd_dev);
		Sdensity.setText(rinf.Sdensity);
	}

	public void getUnique(vmInfo rinf)
	{
		rinf.Snum_items = Snum_items.getText();
		rinf.Snum_clusters = Snum_clusters.getText();
		rinf.Sstd_dev = Sstd_dev.getText();
		rinf.Sdensity = Sdensity.getText();

		rinf.num_items = Integer.parseInt(rinf.Snum_items);
		rinf.num_clusters = Integer.parseInt(rinf.Snum_clusters);
		rinf.std_dev = Double.parseDouble(rinf.Sstd_dev);
		rinf.density = Double.parseDouble(rinf.Sdensity);

		rinf.uniform = uniformRadioButton.isSelected();
		rinf.normal = normalRadioButton.isSelected();
		rinf.poisson = poissonRadioButton.isSelected();

		rinf.SdispInfo = "Num. points: " + rinf.Snum_items + ",  Num. clusters: " + rinf.Snum_clusters;
	}
}

class lmDialog extends RDialog				/* PolyLine Polymap */
{

	private boolean random;

	private JRadioButton randomRadioButton;

	private JTextField Snum_items;

	public lmDialog(JFrame parent)
	{
		super(parent, TITLE_POLYMAP_POLYLINE);
		setSize(500,600);
		setLocation(100,50);
	}

	public void buildUnique()
	{
		pUnique = new JPanel();
		Border etched = BorderFactory.createEtchedBorder();
		Border titledGenP = BorderFactory.createTitledBorder(etched, "Generation Parameters");
		pUnique.setBorder(titledGenP);
		pUnique.setLayout(new GridLayout(2,1));

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(1,2));
		ButtonGroup group = new ButtonGroup();
		randomRadioButton = new JRadioButton("Randomize", false);
		group.add(randomRadioButton);
		p1.add(randomRadioButton);
		pUnique.add(p1);

     	JPanel p2 = new JPanel();
     	p2.setLayout(new GridLayout(2, 2, 4, 4));
     	p2.add(new JLabel("Number of lines:"));
     	p2.add(Snum_items = new JTextField(""));
		pUnique.add(p2);
	}

	public boolean showDialog(lmInfo rinf)
	{
		setCommon(rinf);
		setUnique(rinf);
		ok = false;
	  	show();
	  	if (ok)
	  	{
			getCommon(rinf);
			getUnique(rinf);
	  	}
	  	return ok;
	}

	public void setUnique(lmInfo rinf)
	{
		rinf.title = this.getTitle();
		
		Snum_items.setText(rinf.Snum_items);
	}

	public void getUnique(lmInfo rinf)
	{
		
		rinf.Snum_items = Snum_items.getText();
		rinf.num_items = Integer.parseInt(rinf.Snum_items);
		rinf.random = randomRadioButton.isSelected();
		rinf.SdispInfo = "Num. lines: " + rinf.Snum_items
							+ ",Seed1: " + rinf.Sseed
							+ ", Seed2: " + rinf.Sseed2
							+ "\nrandom= " + random;
	}
}

class lfDialog extends RDialog				/* Poisson Line Field */
{
	private JTextField Snum_items;

	public lfDialog(JFrame parent)
	{
		super(parent, TITLE_POISSON_LINE_FIELD);
     	setSize(500,600);
		setLocation(100,50);
	}

	public void buildUnique()
	{
		pUnique = new JPanel();
		Border etched = BorderFactory.createEtchedBorder();
		Border titledGenP = BorderFactory.createTitledBorder(etched, "Generation Parameters");
		pUnique.setBorder(titledGenP);
		pUnique.setLayout(new GridLayout(1,1));

     	JPanel p2 = new JPanel();
     	p2.setLayout(new GridLayout(2, 2, 4, 4));
     	p2.add(new JLabel("Number of lines:"));
     	p2.add(Snum_items = new JTextField(""));
		pUnique.add(p2);
	}

	public boolean showDialog(lfInfo rinf)
	{
		setCommon(rinf);
		setUnique(rinf);
		ok = false;
	  	show();
	  	if (ok)
	  	{
			getCommon(rinf);
			getUnique(rinf);
	  	}
	  	return ok;
	}

	public void setUnique(lfInfo rinf)
	{
		rinf.title = this.getTitle();
		
		Snum_items.setText(rinf.Snum_items);
	}

	public void getUnique(lfInfo rinf)
	{	
		rinf.Snum_items = Snum_items.getText();
		rinf.num_items = Integer.parseInt(rinf.Snum_items);

		rinf.SdispInfo = "Num. points: " + rinf.Snum_items
							+ ",Seed1: " + rinf.Sseed
							+ ", Seed2: " + rinf.Sseed2;
	}
}

class tsDialog extends RDialog				/* Test Polygon */
{
	private JRadioButton hullRadioButton;
	private JRadioButton squareRadioButton;

	private JTextField Snv;

	public tsDialog(JFrame parent)
   {
		super(parent, TITLE_POLYGON_TEST);
		setSize(500,600);
		setLocation(100,50);
   }

	public void buildUnique()
	{
		pUnique = new JPanel();
		Border etched = BorderFactory.createEtchedBorder();
		Border titledGenP = BorderFactory.createTitledBorder(etched, "Generation Parameters");
		pUnique.setBorder(titledGenP);
		pUnique.setLayout(new GridLayout(2,1));

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(1,2));
		ButtonGroup group = new ButtonGroup();
		hullRadioButton = new JRadioButton("Hull", false);
		group.add(hullRadioButton);
		p1.add(hullRadioButton);
		squareRadioButton = new JRadioButton("Square", true);
		group.add(squareRadioButton);
		p1.add(squareRadioButton);
		pUnique.add(p1);

		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(2, 4, 4, 4));
		p2.add(new JLabel("Number of vertices:"));
		p2.add(Snv = new JTextField(""));
		pUnique.add(p2);
	}

	public boolean showDialog(tsInfo rinf)
	{
		setCommon(rinf);
		setUnique(rinf);
		ok = false;
	    show();
	    if (ok)
	    {
			getCommon(rinf);
			getUnique(rinf);
	    }
	    return ok;
	}

	public void setUnique(tsInfo rinf)
	{
		rinf.title = this.getTitle();
		
		Snv.setText(rinf.Snv);
	}

	public void getUnique(tsInfo rinf)
	{
		rinf.hull = hullRadioButton.isSelected();
		rinf.square = squareRadioButton.isSelected();

		rinf.Snv = Snv.getText();
		rinf.nv = Integer.parseInt(rinf.Snv);

		rinf.SdispInfo = "Num. vertices: " + rinf.Snv;
	}
}

class drDialog extends RDialog				/* Draw Polygon */
{
	private JTextField Snv;

	public drDialog(JFrame parent)
   {
		super(parent, TITLE_POLYGON_DRAW);
		setSize(500,600);
		setLocation(100,50);
   }

	public void buildUnique()
	{
		pUnique = new JPanel();
		Border etched = BorderFactory.createEtchedBorder();
		Border titledGenP = BorderFactory.createTitledBorder(etched, "Generation Parameters");
		pUnique.setBorder(titledGenP);
		pUnique.setLayout(new GridLayout(2,1));

		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(2, 4, 4, 4));
		p2.add(new JLabel("Number of vertices:"));
		p2.add(Snv = new JTextField(""));
		pUnique.add(p2);
	}

	public boolean showDialog(drInfo rinf)
	{
		setCommon(rinf);
		setUnique(rinf);
		ok = false;
	    show();
	    if (ok)
	    {
			getCommon(rinf);
			getUnique(rinf);
	    }
	    return ok;
	}

	public void setUnique(drInfo rinf)
	{
		rinf.title = this.getTitle();
		
		Snv.setText(rinf.Snv);
	}

	public void getUnique(drInfo rinf)
	{
		rinf.Snv = Snv.getText();
		rinf.nv = Integer.parseInt(rinf.Snv);

		rinf.SdispInfo = "Num. vertices: " + rinf.Snv;
	}
}

