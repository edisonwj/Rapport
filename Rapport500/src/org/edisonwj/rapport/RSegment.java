package org.edisonwj.rapport;

/**
 * RSegment defines the class of line segments
 */
		

import java.awt.*;
import java.util.*;

public class RSegment implements RapportDefaults
{
	private Pointd p1;
	private Pointd p2;
	private int id;

	private static double sx;
	private static int count=0;
	private static Object synchO = new Object();

	public RSegment(Pointd p1, Pointd p2)
	{
		setVars(p1, p2);
	}

	public RSegment(String s)
	{
		StringTokenizer t = new StringTokenizer(s, "|,");
		p1 = new Pointd(Double.parseDouble(t.nextToken()),
						Double.parseDouble(t.nextToken()));
		p2 = new Pointd(Double.parseDouble(t.nextToken()),
						Double.parseDouble(t.nextToken()));
		setVars(p1, p2);
	}

	public RSegment()
	{
		setVars(new Pointd(), new Pointd());
	}

	private void setVars(Pointd p1, Pointd p2)
	{
		if (p1.before(p2))
		{
			this.p1 = p1;
			this.p2 = p2;
		}
		else
		{
			this.p1 = p2;
			this.p2 = p1;
		}

		synchronized(synchO)
		{
			id = count++;
		}
	}

	public Pointd getp1()
	{
		return p1;
	}

	public Pointd getp2()
	{
		return p2;
	}

	public int getId()
	{
		return id;
	}

	public double getsx()
	{
		return sx;
	}

	public void setsx(double sx)
	{
		this.sx = sx;
	}



//	public double slope()
//	{
//		/* If rise == 0.0, Java returns +- Infinity */
//		return (p2.gety() - p1.gety()) / (p2.getx() - p1.getx());
//	}
//
//	public double yIntercept()
//	{
//		/* If slope is Infinity, returns +- Infinity */
//		return p1.gety() - slope()*p1.getx();
//	}
//
//	public Pointd midpoint()
//	{
//		double mx, my;
//
//		mx = (p1.getx() + p2.getx()) * 0.5;
//		my = (p1.gety() + p2.gety()) * 0.5;
//		return new Pointd(mx, my);
//	}
//
//	public Pointd ranpoint(Rand mr)
//	{
//		double x, y, x1, x2, y1, y2, dx, dy, rdx;
//		x1 = p1.getx();
//		y1 = p1.gety();
//		x2 = p2.getx();
//		y2 = p2.gety();
//		dx = x2 - x1;
//		dy = y2 - y1;
//		if (dx == 0.0)
//			return new Pointd(x1, y1 + dy * mr.uniform());
//		else if (dy == 0.0)
//			return new Pointd(x1 + dx * mr.uniform(), y1);
//		else
//		{
//			rdx = dx * mr.uniform();
//			x = x1 + rdx;
//			y = rdx * slope() + y1;	/* Zero and infinite slopes have been eliminated */
//			return new Pointd(x, y);
//		}
//	}
//
//	public Pointd random_point_from_edge(boolean bounded, double radial_exp_rate, Rand mr, RPanel rp)
//	{
//		double x, y;
//		double r, theta;
//
//		/* Get midpoint of edge */
////	Pointd mp = midpoint();
//
//		/* Get random point in edge */
//		Pointd mp = ranpoint(mr);
//
//		/* Pick a random radius */
//		if (bounded)
//			r = mr.bounded_exponential(radial_exp_rate);
//		else
//			r = mr.exponential(radial_exp_rate);
//
//		/* Pick a random angle */
//		theta = mr.uniform(0.0, 2*Math.PI);
////		theta = mr.uniform(0.0, Math.PI);
//
//		x = mp.getx() + r * Math.cos(theta);
//		y = mp.gety() + r * Math.sin(theta);
//		return new Pointd(x, y);
//	}
//
	public String toString()
	{
		return ("[" + p1 + " -> " + p2 + "]");
	}

	public Pointd midpoint()
	{
		double mx, my;

		mx = (p1.getx() + p2.getx()) / 2.0;
		my = (p1.gety() + p2.gety()) / 2.0;
		return new Pointd(mx, my);
	}

	public void draw(Graphics g, RPanel rp)
	{
		int x1 = rp.iX(p1.getx());
		int y1 = rp.iY(p1.gety());
		int x2 = rp.iX(p2.getx());
		int y2 = rp.iY(p2.gety());

		g.fillOval(x1-2, y1-2, 5, 5);
		g.fillOval(x2-2, y2-2, 5, 5);
		g.drawLine(x1, y1, x2, y2);

		Pointd m = midpoint();
		int xm = rp.iX(m.getx());
		int ym = rp.iY(m.gety());
		g.drawString(String.valueOf(id), xm, ym);
	}

	public boolean equals(RSegment is)
	{
		if ( (p1.equals(is.getp1()) && p2.equals(is.getp2()) ) ||
			 (p1.equals(is.getp2()) && p2.equals(is.getp1()) ) )
			 return true;
		else
			return false;
	}

	public int compareTo(RSegment is)
	{
		double sy1 = -100.0;
		double sy2 =  100.0;
		IntersectNew ip1 = Geometry.SegSegIntNew(this.p1,    this.p2,    new Pointd(sx,sy1), new Pointd(sx,sy2));
		IntersectNew ip2 = Geometry.SegSegIntNew(is.getp1(), is.getp2(), new Pointd(sx,sy1), new Pointd(sx,sy2));
		if ( (ip1.getp()).equals(ip2.getp()) )
			return 0;
		else if ( (ip1.getp()).before(ip2.getp()) )
			return -1;
		else
			return 1;
	}
}