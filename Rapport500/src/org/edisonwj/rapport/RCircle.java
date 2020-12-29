package org.edisonwj.rapport;

/**
 * RCircle specifies a circle object
 */

import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.StringTokenizer;

public class RCircle extends RObject
{
	private int dimension;
	private double centerx;		/* Circle center */
	private double centery;
	private double radius;		/* Radius */
	private elInfo ri;			/* Link to RInfo */

	protected Pointd cg;
	protected double area;
	protected double hullArea;
	protected double areaRatio;
	protected double perimeter;
	protected double hullPerimeter;
	protected double perRatio;
	protected double compacity;
	protected long notches;
	protected double notches_norm;
	protected double notches_squared, notches_quad;
	protected double freq;
	protected double ampl;
	protected double conv;
	protected double complexity;

	public RCircle()
	{
		type = TYPE_CIRCLE;
		dimension = 2;
		nv = 3;
	}
	
	public RCircle(double x, double y, double r)
	{
		this();
		centerx = x;
		centery = y;
		radius = r;
	}
	
	public RCircle(elInfo ri, Rand mr)
	{
		this();
		this.ri = ri;
		genCircle(ri, mr);
	}
	
	public void genCircle(elInfo ri, Rand mr)
	{
		double minrad = 0.0, maxrad = 0.0;

		if (ri.trim)
		{
			/* Generate center */
			centerx = mr.uniform(ri.minlen, 1.0 - ri.minlen);
			centery = mr.uniform(ri.minlen, 1.0 - ri.minlen);

			/* Generate radius */
			minrad = ri.minlen;
			maxrad = Math.min(ri.maxlen,
						Math.min(
								Math.min(centerx, centery), 
								Math.min(1.0 - centerx, 1.0 - centery)));
			radius = mr.uniform(minrad, maxrad);
		}

		else if (ri.offset)
		{
			/* Generate radius */
			minrad = ri.minlen;
			maxrad = Math.min(ri.maxlen, 0.5);
			radius = mr.uniform(minrad, maxrad);

			/* Generate center */
			centerx = mr.uniform(radius, 1.0 - radius);
			centery = mr.uniform(radius, 1.0 - radius);
		}
	}
		
	public RCircle(BufferedReader in) throws IOException
	{
		this();
//		System.out.println("RCircle(Buffered Reader) this: " + this.hashCode());
		String[] sttp;
		String stitle;
		sttp = readTypeTitleParms(in);
		type = Integer.parseInt(sttp[0]);
		stitle = sttp[1];
		nv = readNV(in);
		if (nv > 0)
		{
			String s = in.readLine();
			StringTokenizer t = new StringTokenizer(s, ",|");
			centerx = Double.parseDouble(t.nextToken());
			centery = Double.parseDouble(t.nextToken());
			radius = Double.parseDouble(t.nextToken());
			ri = new elInfo();
			ri.title = stitle;
//			System.out.println("RCircle this: " + this.hashCode());
//			System.out.println("RCircle ri: " + ri.hashCode() + ", ri.title: " + ri.title);
		}
		else
		{
			System.out.println("RCircle: Error constructing point distribution from file input");
			nv = 0;
		}
	}
	
	public int size()
	{
		return nv;
	}

	public String toString()
	{
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(4);
		return ("circle - center: (" + nf.format(centerx) +
					", " + nf.format(centery) + ")" +
					", radius: " + nf.format(radius) + "\n" );
	}

	public void draw(Graphics g, RPanel rp, Color c)
	{
		int xul, yul, ext;

		Color saveC = g.getColor();
		if (c != null)
			g.setColor(c);

		/* Upper left corner */
		xul = rp.iX(centerx - radius);
		yul = rp.iY(centery + radius);
		ext = rp.iL(2.0 * radius);
//		System.out.println(this);
//		System.out.println(xul + " " + yul + " " + ext);
		g.drawOval(xul, yul, ext, ext);
		g.setColor(saveC);
	}

	public void draw(Graphics g, RPanel rp)
	{
		draw(g, rp, null);
	}

	public int getDimension()
	{
		return dimension;
	}

	public void setDimension(int d)
	{
		dimension = d;
	}

	/* Compute circle metrics */
	public void computeMetrics()
	{
		cg = new Pointd(centerx, centery);
		area = computeArea();
		hullArea = area;
		areaRatio = area/hullArea;
		perimeter = computePerimeter();
		hullPerimeter = perimeter;
		perRatio = perimeter/hullPerimeter;
		compacity = computeCompacity();
		complexity = computeComplexity();
	}

	/* Compute area */
	public double computeArea()
	{
		return Math.PI*radius*radius;
	}

	/* Compute perimeter */
	public double computePerimeter()
	{
		return 2.0*Math.PI*radius;
	}

	/* Compute compacity */
	public double computeCompacity()
	{
		if (area == 0.0)
			computeArea();
		if (perimeter == 0.0)
			computePerimeter();
		compacity = (4*Math.PI*area)/(perimeter*perimeter);
		compacity = 1 - compacity;
		return compacity;
	}

	/* Compute complexity */
	public double computeComplexity()
	{
		return 0.0;
	}

	public double getPerimeter()
	{
		if (perimeter == 0.0)
			perimeter = computePerimeter();
		return perimeter;
	}

	public double getCompacity()
	{
		if (compacity == 0.0)
			compacity = computeCompacity();
		return compacity;
	}

	public double getComplexity()
	{
		if (complexity == 0.0)
			complexity = computeComplexity();
		return complexity;
	}
	
	public void writeData(boolean draw3d, PrintWriter out) throws IOException
	{
//		System.out.println("RCircle writeData - type: " + type);
//		System.out.println("RCircle writeData - title: " + ri.title);
		
		String lineOut = "";
		String parmsOut = "";
		
		if (ri != null) {
			if (ri.offset)
				parmsOut += "Offset ";
			else if (ri.trim)
				parmsOut += "Trim ";
			parmsOut += "Min. radius length = " + ri.minlen + " Max. radius length = " + ri.maxlen;
		}
		
		/* Create output for Draw3D */
		if (draw3d) {
			lineOut = "DataGroup:";
			out.println(lineOut);
			lineOut = "Title1: " + ri.title;
			out.println(lineOut);
			lineOut = "Title2: " + parmsOut;
			out.println(lineOut);

			lineOut = "Circle: ";
			String fmt = DRAW3D_NUMBER_FORMAT;
			lineOut += 	String.format(fmt,centerx) + ", " +
						String.format(fmt,centery) + ", " +
						String.format(fmt,0.0) + ", " +
						String.format(fmt,radius) + ", " +
						"90, 0, 0";
			out.println(lineOut);
		}
		else {
			NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMaximumFractionDigits(DEFAULT_MAX_FRACTION_DIGITS);
			nf.setMinimumFractionDigits(DEFAULT_MIN_FRACTION_DIGITS);
			nf.setMinimumIntegerDigits(DEFAULT_MIN_INTEGER_DIGITS);
			
			out.println("#TYPE=" + type + " " + ri.title);
			out.println("#PARMS= " + parmsOut);
			out.println("#NV=" + nv);
			out.println(nf.format(centerx) + ", " +
					    nf.format(centery) + ", " + 
					    nf.format(radius));
		}
	}
	
	public elInfo getRInfo() {
		return ri;
	}

	public void setVertex(Pointd p, int i){}
	public Pointd getVertex(int i){return null;}
	public void add(Pointd p){}
	public void drawDecomp(Graphics g, RPanel rp){}
	public void drawHull(Graphics g, RPanel rp){}
	public void findHull(){}
	public Hull getHull(){return null;}
	public void reSize(){}
	public int sizeDecomp(){return 0;}
	public int sizeHull(){return 0;}
	public void readData(BufferedReader in) throws IOException{}
	public void scaleToUnitSquare(){}
	public void drawSpecial(Graphics g, RPanel rp){}
}