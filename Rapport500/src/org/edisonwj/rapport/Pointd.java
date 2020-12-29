package org.edisonwj.rapport;

/**
 * Pointd class implements a class corresponding
 * to 2-dimensional points in the plane specified
 * by type double x and y coordinates.
 */

import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;

public class Pointd implements RapportDefaults
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

	/**
	 * Pointd constructor.
	 */
	public Pointd(double x, double y)
	{
		this.x = x;
		this.y = y;
	}

	/**
	 * Default Pointd constructor.
	 */
	public Pointd()
	{
		this(0.0, 0.0);
	}

	/**
	 * Constructor taking a string containing
	 * two floating point numbers as argument.
	 */
	public Pointd(String s)
	{
		StringTokenizer t = new StringTokenizer(s, "|,");
		x = Double.parseDouble(t.nextToken());
		y = Double.parseDouble(t.nextToken());
	}

	/**
	 * Translate point location by xdelta and ydelta.
	 */
	public Pointd translate(double xdelta, double ydelta)
	{
		return new Pointd(x + xdelta, y + ydelta);
	}

	/**
	 * Scale point coordinate values by a scalar factor.
	 */
	public Pointd scale(double factor)
	{
		return new Pointd(x*factor, y*factor);
	}

	/**
	 * Return string representation of Pointd instance.
	 */
	public String toString()
	{
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(DEFAULT_MAX_FRACTION_DIGITS);
		return ("(" + nf.format(x) + ", " + nf.format(y) + ")");
	}

	/**
	 * Draw the Pointd instance on an RPanel determining point
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
		/* Create output for Draw3D */
		if (draw3d) {
			String lineOut = "Point: ";
			String fmt = DRAW3D_NUMBER_FORMAT;
			lineOut += String.format(fmt,x) + ", " +
					   String.format(fmt,y) + ", " +
					   String.format(fmt,0.0);
			out.println(lineOut);
		}
		else {
			NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMaximumFractionDigits(DEFAULT_MAX_FRACTION_DIGITS);
			nf.setMinimumFractionDigits(DEFAULT_MIN_FRACTION_DIGITS);
			nf.setMinimumIntegerDigits(DEFAULT_MIN_INTEGER_DIGITS);
	
			out.println(  nf.format(x) + ", "
							+ nf.format(y) );
		}
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
	 * Settor for x-coordinate.
	 */
	public final void setx(double x)
	{
		this.x = x;
	}

	/**
	 * Settor for y-coordinate.
	 */
	public final void sety(double y)
	{
		this.y = y;
	}

	/**
	 * Return true if the point instance is within the circle
	 * centered at point p with radius r.
	 */
	public final boolean inCircle(double r, Pointd p)
	{
		double cx = x - p.x;
		double cy = y - p.y;

		if (Math.sqrt(cx*cx + cy*cy) <= r )
			return true;
		else
			return false;
	}

	/**
	 * Return true if the point instance occurs before point p
	 * during a horizontal line sweep.
	 */
	public final boolean before(Pointd p)		/* used for sweep line */
	{
		if ( this.y >  p.gety() ||
			 this.y == p.gety() && this.x < p.getx() )
			return true;
		else
			return false;
	}

	/**
	 * Return true if the point instance occurs before point p
	 * during a vertical line sweep.
	 */
	public final boolean vbefore(Pointd p)		/* used for sweep line */
	{
		if (exact_comp)
		{
			if ( this.x < p.getx() ||
				 this.x == p.getx() && this.y < p.gety() )
				return true;
			else
				return false;
		}
		else
		{
			if ( this.x < p.getx() ||
				(Math.abs(this.x - p.getx()) < epsilon) && this.y < p.gety() )
				return true;
			else
				return false;
		}
	}

	/**
	 * Return true if the point instance is less than point p
	 * during a counter clockwise polygon scan test.
	 */
	public final boolean less(Pointd p)			/* used for counter clockwise polygon */
	{
		if ( this.y <  p.gety() ||
			 this.y == p.gety() && this.x < p.getx() )
			return true;
		else
			return false;
	}

	/**
	 * Return true if the point instance is less than point p
	 * where less means smaller x coordinate or if equal x, smaller y.
	 */
	public final boolean xless(Pointd p)
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
	public final boolean equals(Pointd p)
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