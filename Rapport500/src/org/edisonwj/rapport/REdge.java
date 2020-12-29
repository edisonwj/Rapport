package org.edisonwj.rapport;

/**
 * REdge specifies a directed edge object polygon component
 */

import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

public class REdge extends RObject implements RapportDefaults
{
	private static int count=0;	/* Id numbers for objects */

	private Pointd v1;			/* Start vertex of directed edge */
	private Pointd v2;			/* End vertex of directed edge */
	private int id;				/* Unique edge id */
	private int polygon1;		/* Polygon id to left of directed edge */
	private int polygon2;		/* Polygon id to left of edge mirror */
	private boolean checked;	/* Edged checked indicator */

	public REdge(Pointd v1, Pointd v2, int id, int p1, int p2)
	{
		this.v1 = v1;
		this.v2 = v2;
		this.id = id;
		this.polygon1 = p1;
		this.polygon2 = p2;
		this.checked = false;

//		System.out.println("REdge constructor - id: " + id +
//							" " + v1 + "->" + v2);
	}

	public REdge(Pointd v1, Pointd v2, int p1, int p2)
	{
		this(v1, v2, count++, p1, p2);
	}

	public REdge(Pointd v1, Pointd v2, int p1)
	{
		this(v1, v2, count++, p1, Integer.MIN_VALUE);
	}

	public REdge(Pointd v1, Pointd v2)
	{
		this(v1, v2, count++, Integer.MIN_VALUE, Integer.MIN_VALUE);
	}

	public REdge()
	{
		this(new Pointd(), new Pointd(), count++, Integer.MIN_VALUE, Integer.MIN_VALUE);
	}

	public REdge(RSegment rs)
	{
		this(rs.getp1(), rs.getp2(), rs.getId());
	}

	public REdge(String s)
	{
		StringTokenizer t = new StringTokenizer(s, "|");
		v1 = new Pointd(Double.parseDouble(t.nextToken()),
						Double.parseDouble(t.nextToken()));
		v2 = new Pointd(Double.parseDouble(t.nextToken()),
						Double.parseDouble(t.nextToken()));
	}

	public static synchronized int getcount()
	{
		return count++;
	}

	public Pointd getv1()
	{
		return v1;
	}

	public Pointd getv2()
	{
		return v2;
	}

	public Pointd getv(int pid)
	{
		if (polygon1 == pid)
			return v1;
		else
			return v2;
	}

	public int getid()
	{
		return id;
	}

	public int getpoly1()
	{
		return polygon1;
	}

	public int getpoly2()
	{
		return polygon2;
	}

	public boolean checked()
	{
		return checked;
	}

	public static void reset()
	{
		count = 0;
	}

	public void setpoly1(int p1)
	{
		this.polygon1 = p1;
	}

	public void setpoly2(int p2)
	{
		this.polygon2 = p2;
	}

	public void updatePoly(int spltid, int newid)
	{
		if (polygon1 == spltid)
			polygon1 = newid;
		else
			polygon2 = newid;
	}

	public void reverse()
	{
		Pointd vt = v1;
		int pt = polygon1;
		v1 = v2;
		v2 = vt;
		polygon1 = polygon2;
		polygon2 = pt;
	}

	public void setchecked(boolean f)
	{
		this.checked = f;
	}

	public Pointd midpoint()
	{
		double mx, my;

		mx = (v1.getx() + v2.getx()) / 2.0;
		my = (v1.gety() + v2.gety()) / 2.0;
		return new Pointd(mx, my);
	}

	public double slope()
	{
		return (v2.gety() - v1.gety()) / (v2.getx() - v1.getx());
	}

	public Pointd ranpoint(Rand mr)
	{
		double x, y, x1, x2, y1, y2, dx, dy, rdx;
		x1 = v1.getx();
		y1 = v1.gety();
		x2 = v2.getx();
		y2 = v2.gety();
		dx = x2 - x1;
		dy = y2 - y1;
		if (dx == 0.0)
			return new Pointd(x1, y1 + dy * mr.uniform());
		else if (dy == 0.0)
			return new Pointd(x1 + dx * mr.uniform(), y1);
		else
		{
			rdx = dx * mr.uniform();
			x = x1 + rdx;
			y = rdx * this.slope() + y1;	/* Zero and infinite slopes have been eliminated */
			return new Pointd(x, y);
		}
	}

	public Pointd random_point_from_edge(boolean bounded, double radial_exp_rate, Rand mr)
	{
		double x, y;
		double r, theta;

		/* Get midpoint of edge */
//		Pointd mp = midpoint();

		/* Get random point in edge */
		Pointd mp = ranpoint(mr);

		/* Pick a random radius */
		if (bounded)
			r = mr.bounded_exponential(radial_exp_rate);
		else
			r = mr.exponential(radial_exp_rate);

		/* Pick a random angle */
		theta = mr.uniform(0.0, 2*Math.PI);
//		theta = mr.uniform(0.0, Math.PI);

		x = mp.getx() + r * Math.cos(theta);
		y = mp.gety() + r * Math.sin(theta);
		return new Pointd(x, y);
	}

	public String toString()
	{
		return ("REdge " + id + ":"
				+ " [" + v1 + " -> " + v2 + "]"
				+ " poly1: " + polygon1
				+ " poly2: " + polygon2
				+ " checked: " + checked);
	}

	public boolean isGend()
	{
		return true;
	}

	public void draw(Graphics g, RPanel rp)
	{
		int x1 = rp.iX(v1.getx());
		int y1 = rp.iY(v1.gety());
		int x2 = rp.iX(v2.getx());
		int y2 = rp.iY(v2.gety());

		g.fillOval(x1-2, y1-2, 5, 5);
		g.fillOval(x2-2, y2-2, 5, 5);
		g.drawLine(x1, y1, x2, y2);

		Pointd m = midpoint();
		int xm = rp.iX(m.getx());
		int ym = rp.iY(m.gety());
		g.drawString(String.valueOf(id), xm, ym);
	}

	public int size() {return 1;}
	public void findHull() {}
	public Hull getHull() {return null;}
	public void reSize() {}
	public void reSize(int n) {}
	public void add(Pointd p) {}
	public void drawDecomp(Graphics g, RPanel rp) {}
	public Pointd getVertex(int n) { return null;}
	public void setVertex(Pointd p, int n) {}
	public int sizeDecomp() {return 0;}
	public int sizeHull() {return 0;}
	public void writeData(boolean draw3d, PrintWriter out) throws IOException {}
	public void readData(BufferedReader in) throws IOException {}
	public void scaleToUnitSquare() {}
	public void drawSpecial(Graphics g, RPanel rp) {}

	public static void main(String[] args)
	{
		reGen rg;
		reInfo ri;
		RPanel rp;
		RObject ro1, ro2;

		ri = new reInfo();
		ri.num_items = 1;
		rg = new reGen(ri);
		ro1 = new REdge(new Pointd(.25, .25), new Pointd(.75, .75));
		ro1.nv = 1;
		ro2 = new REdge(new Pointd(.75, .75), new Pointd(.75, .00));
		ro2.nv = 1;
		rg.rv = new Vector(ri.numb_objects);
		rg.rv.add(ro1);
		rg.rv.add(ro2);

   		JFrame f = new RFrame(rg);
		f.setVisible(true);
	}

}