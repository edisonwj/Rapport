package org.edisonwj.rapport;

/**
 * PointE class implements a class corresponding
 * to 2-dimensional points in the plane that are
 * end points of line segments. The points are
 * specified by type double x and y coordinates
 * and by a boolean variable left indicating left
 * endpoint or right endpoint. A point is the left
 * endpoint of a line segment if x.left < x.right
 * or if x.left = x.right, y.left < y.right.
 */

import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;

class PointE implements RapportDefaults
{
	/**
	 * Class variables providing for inexact point comparison.
	 */
	private static final double epsilon = DEFAULTEPSILON;
	private static final boolean exact_comp = EXACT_COMP;

	/**
	 * Variables specifying a point instance.
	 */
	double x;
	double y;
	int id;
	boolean left;

	/**
	 * PointE constructor.
	 */
	public PointE(double x, double y, int i, boolean l)
	{
		this.x = x;
		this.y = y;
		this.id = i;
		this.left = l;
	}

	/**
	 * Default PointE constructor.
	 */
	public PointE()
	{
		this(0.0, 0.0, 0, true);
	}

	/**
	 * PointE constructor
	 */
	 public PointE(Pointd p, int i, boolean l)
	 {
		 this(p.getx(), p.gety(), i, l);
	 }

	/**
	 * Accessor for x-coordinate.
	 */
	public final double getx()
	{
		return x;
	}

	/**
	 * Accessor for y-coordinate.
	 */
	public final double gety()
	{
		return y;
	}

	/**
	 * Accessor for id
	 */
	public final int getId()
	{
		return id;
	}

	/**
	 * Accessor for left.
	 */
	 public final boolean isLeft()
	 {
		 return left;
	 }

	/**
	 * Return string representation of Pointd instance.
	 */
	public String toString()
	{
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(8);
		return ("(" + nf.format(x) + ", " + nf.format(y) + "), id= " + id + ", left= " + left);
	}

	/**
	 * Draw the PointE instance on an RPanel determining point
	 * location on the panel using the iX and iY methods.
	 */
	public void draw(Graphics g, RPanel rp, Color c)
	{
		int x1 = rp.iX(x);
		int y1 = rp.iY(y);
		if ( c != null) g.setColor(c);
		g.fillOval(x1-2, y1-2, 5, 5);
	}

	/**
	 * Draw the Pointd instance on an RPanel using the current color.
	 */
	public void draw(Graphics g, RPanel rp)
	{
		draw(g, rp, null);
	}

	/**
	 * Format and write the point data to a text file.
	 */

	public void writeData(boolean draw3d, PrintWriter out) throws IOException
	{
		System.out.println("PointE writeData");
		
		/* Create output for Draw3D */
		if (draw3d) {
			String lineOut = "PointE: ";
			String fmt = DRAW3D_NUMBER_FORMAT;
			lineOut += 	String.format(fmt,x) + ", " +
						String.format(fmt,y) + ", " +
						String.format(fmt,0.0);
			if (left)
				lineOut += ", true";
			else
				lineOut += ", false";
			out.println(lineOut);
		}
		else {
			NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMaximumFractionDigits(DEFAULT_MAX_FRACTION_DIGITS);
			nf.setMinimumFractionDigits(DEFAULT_MIN_FRACTION_DIGITS);
			nf.setMinimumIntegerDigits(DEFAULT_MIN_INTEGER_DIGITS);
	
			out.println(  nf.format(x) + ", "
							+ nf.format(y) + " "
							+ id + " "
							+ left );
		}
	}

	/**
	 * Return true if the point instance is less than point p
	 * where less means smaller x coordinate or if equal x, smaller y.
	 */
	public final boolean xless(PointE p)
	{
		if ( this.x <  p.getx() ||
			 this.x == p.getx() && this.y < p.gety() )
			return true;
		else
			return false;
	}

	/**
	 * Return true if the point instance equals point p.
	 * Exact or inexact comparison is supported.
	 */
	public final boolean equals(PointE p)
	{
		if (exact_comp)
		{
			if ( this.x == p.getx() && this.y == p.gety() )
				return true;
			else
				return false;
		}
		else
		{
			if ( (Math.abs(this.x - p.getx()) < epsilon) &&
				 (Math.abs(this.y - p.gety()) < epsilon) )
				return true;
			else
				return false;
		}
	}
}