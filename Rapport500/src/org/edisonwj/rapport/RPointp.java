package org.edisonwj.rapport;

/**
 * RPointp defines a point in polar coordinates.
 */

import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;

class RPointp implements RapportDefaults
{
	double p;		/* length */
	double phi;		/* angle */
	int ptype;		/* type of line point */

	public RPointp(double p, double phi, int ptype)
	{
		this.p = p;
		this.phi = phi;
		this.ptype = ptype;
	}

	public RPointp()
	{
		this(0.0, 0.0, 0);
	}

	public RPointp(String s)
	{
		StringTokenizer t = new StringTokenizer(s, ",|");
		p = Double.parseDouble(t.nextToken());
		phi = Double.parseDouble(t.nextToken());
		ptype = 0;
	}

	public String toString()
	{
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(8);
		return ("Polar: (" + nf.format(p) + ", " + nf.format(phi) + "), " +
				"Cart:  (" + nf.format(p * Math.cos(phi)) + ", " + nf.format(p * Math.sin(phi)) + ")" +
				"Ptype: " + ptype);
	}

	public RSegment toSegment()
	{
		double x, y;
		double a, b;
		double hix, lox, hiy, loy;
		double x1, y1, x2, y2;

		x = p * Math.cos(phi);
//		System.out.println("Rpointp x: " + p + " " + phi + " " + x);
		y = p * Math.sin(phi);
//		System.out.println("Rpointp y: " + p + " " + phi + " " + y);

//		Extend lines only within unit square
		hix = .5;
		lox = -.5;
		hiy = .5;
		loy = -.5;

		if (phi != 0.0 && phi != Math.PI)
		{
			a = -1.0/Math.tan(phi);
			b = y - a * x;

			x1 = lox;
			x2 = hix;
			y1 = a * x1 + b;
			y2 = a * x2 + b;

			if (y1 > hiy)
			{
				y1 = hiy;
				x1 = (y1 - b)/a;
			}
			else if (y1 < loy)
			{
				y1 = loy;
				x1 = (y1 - b)/a;
			}

			if (y2 > hiy)
			{
				y2 = hiy;
				x2 = (y2 - b)/a;
			}
			else if (y2 < loy)
			{
				y2 = loy;
				x2 = (y2 - b)/a;
			}
		}
		else
		{
			x1 = p;
			y1 = loy;
			x2 = p;
			y2 = hiy;
		}

		return (new RSegment(new Pointd(x1,y1), new Pointd(x2,y2)));

	}

	public void draw(Graphics g, RPanel rp, Color c)
	{
		int xi, yi;
		double x, y;
		double a, b;
		double hix, lox, hiy, loy;
		double x1, y1, x2, y2;

		if (c == null)
		{
			switch( ptype )
			{		
				case 1:
					g.setColor(Color.blue);
				break;

				case 2:
					g.setColor(Color.yellow);
				break;

				case 3:
					g.setColor(Color.red);
				break;
			}
		}
		else
			g.setColor(c);

		x = p * Math.cos(phi);
//		System.out.println("Rpointp x: " + p + " " + phi + " " + x);
		xi = rp.iX(x);
		y = p * Math.sin(phi);
//		System.out.println("Rpointp y: " + p + " " + phi + " " + y);
		yi = rp.iY(y);
		g.fillOval(xi-2, yi-2, 5, 5);

//		Extend lines full extent of window
//		hix = rp.getrWidth()/2;
//		lox = -hix;
//		hiy = rp.getrHeight()/2;
//		loy = -hiy;

//		Extend lines only within unit square
		hix = .5;
		lox = -.5;
		hiy = .5;
		loy = -.5;

		if (phi != 0.0 && phi != Math.PI)
		{
			a = -1.0/Math.tan(phi);
			b = y - a * x;

			x1 = lox;
			x2 = hix;
			y1 = a * x1 + b;
			y2 = a * x2 + b;

			if (y1 > hiy)
			{
				y1 = hiy;
				x1 = (y1 - b)/a;
			}
			else if (y1 < loy)
			{
				y1 = loy;
				x1 = (y1 - b)/a;
			}

			if (y2 > hiy)
			{
				y2 = hiy;
				x2 = (y2 - b)/a;
			}
			else if (y2 < loy)
			{
				y2 = loy;
				x2 = (y2 - b)/a;
			}
		}
		else
		{
			x1 = p;
			y1 = loy;
			x2 = p;
			y2 = hiy;
		}
//		if (ptype == TYPE_POLAR_POINT)
		g.drawLine(rp.iX(x1), rp.iY(y1), rp.iX(x2), rp.iY(y2));

//		g.setColor(Color.gray);
//		g.drawLine(rp.iX(0.0), rp.iY(0.0), rp.iX(x), rp.iY(y));
	}

	public void draw(Graphics g, RPanel rp)
	{
		draw(g, rp, null);
	}
	
	public double[] getLineEndPoints()
	{
		double x, y;
		double a, b;
		double hix, lox, hiy, loy;
		double x1, y1, x2, y2;
		double[] lineEnds = new double[4];
		x = p * Math.cos(phi);
//		System.out.println("Rpointp x: " + p + " " + phi + " " + x);
		y = p * Math.sin(phi);
//		System.out.println("Rpointp y: " + p + " " + phi + " " + y);


//		Extend lines full extent of window
//		hix = rp.getrWidth()/2;
//		lox = -hix;
//		hiy = rp.getrHeight()/2;
//		loy = -hiy;

//		Extend lines only within unit square
		hix = .5;
		lox = -.5;
		hiy = .5;
		loy = -.5;

		if (phi != 0.0 && phi != Math.PI)
		{
			a = -1.0/Math.tan(phi);
			b = y - a * x;

			x1 = lox;
			x2 = hix;
			y1 = a * x1 + b;
			y2 = a * x2 + b;

			if (y1 > hiy)
			{
				y1 = hiy;
				x1 = (y1 - b)/a;
			}
			else if (y1 < loy)
			{
				y1 = loy;
				x1 = (y1 - b)/a;
			}

			if (y2 > hiy)
			{
				y2 = hiy;
				x2 = (y2 - b)/a;
			}
			else if (y2 < loy)
			{
				y2 = loy;
				x2 = (y2 - b)/a;
			}
		}
		else
		{
			x1 = p;
			y1 = loy;
			x2 = p;
			y2 = hiy;
		}

		lineEnds[0] = x1;
		lineEnds[1] = y1;
		lineEnds[2] = x2;
		lineEnds[3] = y2;
		return lineEnds;
	}


	public void writeData(boolean draw3d, PrintWriter out) throws IOException
	{
//		System.out.println("RPointp writeData");
		
		// Create output for Draw3D
		// Saves the point as a line (vector)
		if (draw3d) {
			double[] lineEnds = getLineEndPoints();
			String lineOut = "Line: ";
			String fmt = DRAW3D_NUMBER_FORMAT;
			lineOut += String.format(fmt,lineEnds[0]) + ", " +
					String.format(fmt,lineEnds[1]) + ", " +
					String.format(fmt,0.0) + ", " +
					String.format(fmt,lineEnds[2]) + ", " +
					String.format(fmt,lineEnds[3]) + ", " +
					String.format(fmt,0.0);
			out.println(lineOut);
		}
		else {
			NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMaximumFractionDigits(DEFAULT_MAX_FRACTION_DIGITS);
			nf.setMinimumFractionDigits(DEFAULT_MIN_FRACTION_DIGITS);
			nf.setMinimumIntegerDigits(DEFAULT_MIN_INTEGER_DIGITS);
			
			out.println(  nf.format(p) + ", "
							+ nf.format(phi) );
		}
	}

	public final double getp()
	{
		return p;
	}

	public final double getphi()
	{
		return phi;
	}

	public final int getptype()
	{
		return ptype;
	}

	public final double getx()
	{
		return p * Math.cos(phi);
	}

	public final double gety()
	{
		return p * Math.sin(phi);
	}

	public final void setp(double p)
	{
		this.p = p;
	}

	public final void setphi(double phi)
	{
		this.phi = phi;
	}

	public final void setptype(int ptype)
	{
		this.ptype = ptype;
	}

	public final boolean isDistinct(RPointp t)
	{
		if( (Math.abs(this.p-t.getp()) <= DEFAULTEPSILON) &&
			(Math.abs(this.phi-t.getphi()) <= DEFAULTEPSILON) )
			return false;
		else
			return true;
	}
}