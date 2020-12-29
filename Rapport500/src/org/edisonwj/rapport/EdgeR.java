package org.edisonwj.rapport;

/**
 * EdgeR class defines edge objects as determined
 * by two vertices. 
 */

import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;

public class EdgeR implements RapportDefaults
{
	private Pointd v1;
	private Pointd v2;

	public EdgeR(Pointd v1, Pointd v2)
	{
		this.v1 = v1;
		this.v2 = v2;
	}

	public EdgeR()
	{
		this(new Pointd(), new Pointd());
	}

	public EdgeR(String s)
	{
		StringTokenizer t = new StringTokenizer(s, "|");
		v1 = new Pointd(Double.parseDouble(t.nextToken()),
						Double.parseDouble(t.nextToken()));
		v2 = new Pointd(Double.parseDouble(t.nextToken()),
						Double.parseDouble(t.nextToken()));
	}

	public Pointd getv1()
	{
		return v1;
	}

	public Pointd getv2()
	{
		return v2;
	}

	public double angle(EdgeR e)
	{
		double angle, s1, s2;

		/* Translate start to origin */
		double x1 = v2.getx()-v1.getx();
		double y1 = v2.gety()-v1.gety();
		double x2 = e.getv2().getx()-e.getv1().getx();
		double y2 = e.getv2().gety()-e.getv1().gety();

		if (x1 != 0.0 && x2 != 0.0)
		{
			s1 = y1/x1;
			s2 = y2/x2;
			angle = Math.atan((s2 - s1)/(1 + s2*s1));
			if (x2 < 0)
			{
				if (y2 >=0)
					angle += Math.PI;
				else
					angle -= Math.PI;
			}
		}
		else if (x1 == 0.0 && x2 == 0.0)
		{
			angle = 0.0;
		}
		else if (x1 != 0.0)
		{
			s1 = y1/x1;
			angle = Math.PI/2 - Math.atan(s1);
		}
		else
		{
			s2 = y2/x2;
			angle = Math. atan(s2);
			if (angle < 0.0)
				angle -= Math.PI/2;
			else
				angle += Math.PI/2;
		}
		return angle;
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
		return ("[" + v1 + " -> " + v2 + "]");
	}

	public void erase(Graphics g, RPanel rp, Color c)
	{
		int x1 = rp.iX(v1.getx());
		int y1 = rp.iY(v1.gety());
		int x2 = rp.iX(v2.getx());
		int y2 = rp.iY(v2.gety());
		g.setColor(rp.getBackground());		/* Remove existing line */
		g.drawLine(x1, y1, x2, y2);
		g.setColor(c);
		g.fillOval(x1-2, y1-2, 5, 5);
		g.fillOval(x2-2, y2-2, 5, 5);
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
	}

	public void erase(Graphics2D g2d, RPanel rp)
	{
		int x1 = rp.iX(v1.getx());
		int y1 = rp.iY(v1.gety());
		int x2 = rp.iX(v2.getx());
		int y2 = rp.iY(v2.gety());
		Color c = g2d.getColor();
		g2d.setColor(g2d.getBackground());		/* Remove existing line */
		g2d.setStroke(new BasicStroke());
		g2d.drawLine(x1, y1, x2, y2);
		g2d.setColor(c);
	}

	public void draw(Graphics2D g2d, RPanel rp, boolean solid, Color c)
	{
		Stroke stroke;

		int x1 = rp.iX(v1.getx());
		int y1 = rp.iY(v1.gety());
		int x2 = rp.iX(v2.getx());
		int y2 = rp.iY(v2.gety());

		if (c != null) g2d.setColor(c);

		if (solid)
			stroke = new BasicStroke();
		else
			stroke = new BasicStroke(2f, BasicStroke.CAP_BUTT,
						BasicStroke.JOIN_MITER, 5f, new float[] {5f}, 0f);

		g2d.setStroke(stroke);
		g2d.drawLine(x1, y1, x2, y2);

		g2d.fillOval(x2-2, y2-2, 5, 5);
	}

	public void draw(Graphics2D g2d, RPanel rp, boolean solid)
	{
		draw(g2d, rp, solid, null);
	}
}