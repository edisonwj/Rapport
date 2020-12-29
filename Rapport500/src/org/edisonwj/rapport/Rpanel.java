package org.edisonwj.rapport;
/**
 * RPanel defines responses to mouse actions
 */

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

class RPanel extends JPanel implements RapportDefaults
{
	private RGen rg;					/* Generation trans. assoc. with object */
	private RObject ro;					/* Object to be displayed */
	private Vector rv;					/* Vector of objects to be displayed */
	private RInfo ri;					/* Object information */ /* added 06092002 */

	private int maxX, maxY;				/*	pixel dimensions of panel */
	private int centerX, centerY;		/* pixel center of panel */
	private int centerXT, centerYT;		/* pixel translated center */
	private int diffX=0, diffY=0;		/* pixel difference between center and centerT */

	private double rWidth, rHeight;		/* logical dimensions of panel */
	private double pixelSize;			/* logical dimensions - pixel size */
	private String lowX, hiX;			/* logical dimensions - bounds of visible x axis */
	private String lowY, hiY;			/* logical dimensions - bounds of visible y axis */

	private NumberFormat nf;			/* number formating object */
	private Font f;
	private FontMetrics fm;
	private int fd, fh, fw;

	private boolean clearPanel = false;		/* clear panel */
	private boolean drawHull = false;		/* draw convex hull */
	private boolean drawDecomp = false;		/* draw decomposition */
	private boolean drawScale = false;		/* scale to unit square */
	private boolean drawing = false;		/* drawing polygon */

	private double x0, y0, xA, yA, dfx, dfy; /* variables for polygon draw */

	private boolean allRO = true;			/* Overlayed display of all rv objects */
	private int currentRO = 0;				/* Index of current rv object displayed */
	private RObject rb = null;

	public RPanel(RGen rg)
	{
//		System.out.println("Rpanel RPanel constructor: " + this.hashCode());
		addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent evt)
			{
				if (currentRO > -1)
				{
					rb = (RObject)rv.get(currentRO);
					if (rb.getType() == TYPE_POLYGON_DRAW)
						drawing = true;
				}

				if (drawing)
				{
					xA = dx(evt.getX());
					yA = dy(evt.getY());

					if (rb.getnv() == 0)
					{
						x0 = xA;
						y0 = yA;
						Pointd p = new Pointd(xA, yA);
						rb.add(p);
					}

					else
					{
						dfx = xA - x0;
						dfy = yA - y0;

						if (rb.getnv() > 0 &&
							dfx * dfx + dfy * dfy < 8 * pixelSize * pixelSize)
						{
							rb.reSize();
							rb.setType(TYPE_POLYGON);
							drawing = false;
						}
						else
						{
							Pointd p = new Pointd(xA, yA);
							rb.add(p);
						}
					}
					if (((PolygonA)rb).getnv() == ((PolygonA)rb).vSize())
					{
						rb.setType(TYPE_POLYGON);
						if (ri.color)
							((PolygonA)rb).setColor();
						((PolygonA)rb).checkPolygon();
						System.out.println("Polygon drawing complete");
					}
				}

				else if (evt.getClickCount() < 2)
				{
					diffX = centerXT - evt.getX();
					diffY = centerYT - evt.getY();
					centerXT = centerX + diffX;
					centerYT = centerY + diffY;
				}
				else
				{
					diffX = 0;
					diffY = 0;
					centerXT = centerX;
					centerYT = centerY;
				}
				repaint();
			}
		});

		this.rg = rg;
		this.rv = rg.rv;
		this.ri = rg.ri;
//		System.out.println("RPanel gets rg: " + this.rg.hashCode() +
//				", rv: " + this.rv.hashCode() +
//				", ri: " + this.ri.hashCode());
//		this.ro = rg.getRO();
//		System.out.println("RPanel sets rp in rg");
		rg.setRP(this);


		if (ri.lowleft)
		{
			diffX =	-(int)(.46 * ri.pixelDim);
			diffY =  +(int)(.38 * ri.pixelDim);
		}
		centerXT = centerX + diffX;
		centerYT = centerY + diffY;
		rWidth = ri.logicalDim;
		rHeight = ri.logicalDim;

		nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		nf.setMinimumIntegerDigits(1);
	}

	public RPanel(RObject ro, RInfo ri)
	{
		this.ro = ro;
		this.ri = ri;

		addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent evt)
			{
				if (evt.getClickCount() < 2)
				{
					diffX = evt.getX()-centerX;
					diffY = evt.getY()-centerY;
					repaint();
				}
				else
				{
					diffX = 0; diffY = 0;
					repaint();
				}
			}
		});

		if (ri.lowleft)
		{
			diffX =	-(int)(.46 * ri.pixelDim);
			diffY = +(int)(.38 * ri.pixelDim);
		}

		centerXT = centerX + diffX;
		centerYT = centerY + diffY;
		rWidth = ri.logicalDim;
		rHeight = ri.logicalDim;

		nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		nf.setMinimumIntegerDigits(1);
	}

	void initgr()
	{
		if (rg != null)
		{
			this.rg = rg;
			this.rv = rg.rv;
		}
		Dimension d = getSize();
    	maxX = d.width - 1;
		maxY = d.height - 1;
		centerX = maxX/2;
		centerY = maxY/2;
		centerXT = centerX + diffX;
		centerYT = centerY + diffY;
		rWidth = ri.logicalDim;
		rHeight = ri.logicalDim;
		pixelSize = Math.max(rWidth/maxX, rHeight/maxY);
		lowX = "-" + nf.format((pixelSize * maxX * .5D) + (pixelSize * diffX));
		lowY = "-" + nf.format((pixelSize * maxY * .5D) - (pixelSize * diffY));
		hiX  = "+" + nf.format((pixelSize * maxX * .5D) - (pixelSize * diffX));
		hiY  = "+" + nf.format((pixelSize * maxY * .5D) + (pixelSize * diffY));
	}

	int iX(double x){return (int)Math.round(centerXT + x/pixelSize);}
	int iY(double y){return (int)Math.round(centerYT - y/pixelSize);}
	int iL(double l){return (int)Math.round(l/pixelSize);}

	double dx(int X){return (X - centerXT) * pixelSize;}
	double dy(int Y){return (centerYT - Y) * pixelSize;}

	double getrWidth(){return rWidth;}
	double getrHeight(){return rHeight;}

	public void drawPoint(Pointd p)
	{
		this.drawPoint(p, Color.blue);
	}

	public void drawPoint(Pointd p, Color c)
	{
		Graphics g = (Graphics)getGraphics();
		initgr();
		g.setColor(c);
		p.draw(g, this);
		g.dispose();
	}

	public void drawEdge(EdgeR e)
	{
		this.drawEdge(e, Color.blue);
	}

	public void drawEdge(EdgeR e, Color c)
	{
		Graphics g = getGraphics();
		initgr();
		g.setColor(c);
		e.draw(g, this);
		g.dispose();
	}

//	public void drawEdge(EdgeR e, boolean solid)
//	{
//		Graphics2D g = (Graphics2D)getGraphics();
//		initgr();
//		g.setColor(Color.blue);
//		e.draw(g, this, solid);
//		g.dispose();
//	}
//
//	public void eraseEdge(EdgeR e, Color c)
//	{
//		Graphics g = getGraphics();
//		initgr();
//		e.erase(g, this, c);
//		g.dispose();
//	}
//
//	public void drawTriangle(Triangle tr, Color c)
//	{
//		Graphics g = getGraphics();
//		initgr();
//		int[] x = new int[3], y = new int[3];
//		x[0] = iX(tr.A.x); y[0] = iY(tr.A.y);
//		x[1] = iX(tr.B.x); y[1] = iY(tr.B.y);
//		x[2] = iX(tr.C.x); y[2] = iY(tr.C.y);
//		g.setColor(c);
//		g.fillPolygon(x, y, 3);
//		g.dispose();
//	}

	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		initgr();

//		this.setBackground(Color.white);

		if ( ri.axes )
		{
			g.setColor(Color.gray);
			(new Pointd(0, 0)).draw(g, this);
			g.drawLine(centerXT, -maxY, centerXT, maxY);
			g.drawLine(-maxX, centerYT, maxX, centerYT);
			setupFont(g);
			g.drawString(lowY, centerXT, maxY-fd);
			g.drawString(hiY, centerXT, fh);
			g.drawString(lowX, 4, centerYT);
			g.drawString(hiX, maxX-fw, centerYT);
		}

		if ( ri.unitsquare )
		{
			g.setColor(Color.gray);
			int ln = iL(2.0);
			g.drawRect(iX(-1.0), iY(1.0), ln, ln);

			g.drawLine(iX(-1.0), iY(-.75), iX( 1.0), iY(-.75));
			g.drawLine(iX(-1.0), iY(-.50), iX( 1.0), iY(-.50));
			g.drawLine(iX(-1.0), iY(-.25), iX( 1.0), iY(-.25));
			g.drawLine(iX(-1.0), iY( .25), iX( 1.0), iY( .25));
			g.drawLine(iX(-1.0), iY( .50), iX( 1.0), iY( .50));
			g.drawLine(iX(-1.0), iY( .75), iX( 1.0), iY( .75));

			g.drawLine(iX(-.75), iY(-1.0), iX(-.75), iY(1.0));
			g.drawLine(iX(-.50), iY(-1.0), iX(-.50), iY(1.0));
			g.drawLine(iX(-.25), iY(-1.0), iX(-.25), iY(1.0));
			g.drawLine(iX( .25), iY(-1.0), iX( .25), iY(1.0));
			g.drawLine(iX( .50), iY(-1.0), iX( .50), iY(1.0));
			g.drawLine(iX( .75), iY(-1.0), iX( .75), iY(1.0));
		}
		
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		g.setColor(Color.blue);
		if (rv != null && rv.size() > 0)
		{
			if (allRO)
				for (int i = 0; i < rv.size(); i++)
				{
					RObject rb = (RObject)rv.get(i);
					rb.draw(g, this);
				}
			else
			{
//				System.out.println("RPanel - Draw RObject current");
//				System.out.println("RPanel rv: " + rv.hashCode());
				RObject rb = (RObject)rv.get(currentRO);
//				System.out.println("rb: " + rb.type + " " + rb.hashCode());
				rb.draw(g, this);
			}

			if (clearPanel)
				clearPanel = false;

			if (drawHull)
			{
				if (currentRO > -1)
				{
					rb = (RObject)rv.get(currentRO);
					(rb.getHull()).drawHull(g, this);
				}
			}

			if (drawDecomp)
			{
				if (currentRO > -1)
				{
					for (int i = 0; i < rv.size(); i++)
					{
						rb = (RObject)rv.get(i);
						((PolygonA)rb).triangulate();			
					}
					rb = (RObject)rv.get(currentRO);
					rb.drawDecomp(g, this);
				}		
			}

			if (drawScale)
			{
				if (currentRO > -1)
				{
					rb = (RObject)rv.get(currentRO);
					rb.scaleToUnitSquare();
				}
			}

		}
		else if (ro != null)
		{
			ro.drawSpecial(g, this);
		}
	}

	private void setupFont(Graphics g)
	{
		f = new Font("SansSerif", Font.PLAIN, 12);
		g.setFont(f);
		fm = g.getFontMetrics(f);
		fw = fm.stringWidth(hiX);
		fh = fm.getHeight();
		fd = fm.getMaxDescent();
	}

	public void setRO(int i)
	{
		currentRO = i;
		if (i < 0)
			allRO = true;
		else
			allRO = false;
	}

	public void doClear()
	{
		clearPanel = true;
		drawDecomp = false;
		drawHull = false;
		drawScale = false;
	}

	public void doDecomp()
	{
		drawDecomp = true;
	}

	public void doHull()
	{
		drawHull = true;
	}

	public void doScale()
	{
		drawScale = true;
	}

	public int getCurrentRO()
	{
		return currentRO;
	}
}