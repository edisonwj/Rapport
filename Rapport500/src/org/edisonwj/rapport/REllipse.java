package org.edisonwj.rapport;

/**
 * REllipse defines an ellipse object
 */

import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.StringTokenizer;

public class REllipse extends RObject
{
	private int dimension;
	private double centerx;		/* Ellipse center */
	private double centery;
	private double extx;		/* x-extent */
	private double exty;		/* y-extent */
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

	public REllipse()
	{
		type = TYPE_ELLIPSE;
		dimension = 2;
		nv = 4;
	}
		
	public REllipse(elInfo ri, Rand mr)
	{
		this();
		this.ri = ri;
		genEllipse(ri, mr);
	}
	
	public void genEllipse(elInfo ri, Rand mr)
	{
		double mina = 0.0 , minb = 0.0;
		double maxa, maxb;

		if (ri.trim)
		{
			/* Generate center */
			centerx = mr.uniform(ri.minlen, 1.0 - ri.minlen);
			centery = mr.uniform(ri.minlen, 1.0 - ri.minlen);

			/* Generate extents */
			extx = 0.0;
			while (extx == 0.0) {
				maxa = Math.min(ri.maxlen, Math.min(centerx, 1.0 - centerx));
				extx = mr.uniform(mina, maxa);
			}

			exty = 0.0;
			while (exty == 0.0) {
				minb = ri.minlen;
				maxb = Math.min(ri.maxlen, Math.min(centery, 1.0 - centery));
				exty = mr.uniform(minb, maxb);
			}
		}

		else if (ri.offset)
		{
			/* Generate extents */
			extx = 0.0;
			while (extx == 0.0) {
				mina = ri.minlen;
				maxa = Math.min(ri.maxlen, 0.5);
				extx = mr.uniform(mina, maxa);
			}
			exty = 0.0;
			while ( exty == 0.0) {
				minb = ri.minlen;
				maxb = Math.min(ri.maxlen, 0.5);
				exty = mr.uniform(minb, maxb);
			}

			/* Generate center */
			centerx = mr.uniform(extx, 1.0 - extx);
			centery = mr.uniform(exty, 1.0 - exty);
		}
	}
	
	public REllipse(BufferedReader in) throws IOException
	{
		this();
//		System.out.println("REllipse(Buffered Reader) this: " + this.hashCode());
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
			extx = Double.parseDouble(t.nextToken());
			exty = Double.parseDouble(t.nextToken());
			ri = new elInfo();
			ri.title = stitle;
//			System.out.println("REllipse this: " + this.hashCode());
//			System.out.println("REllipse ri: " + ri.hashCode() + ", ri.title: " + ri.title);
		}
		else
		{
			System.out.println("REllipse: Error constructing point distribution from file input");
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
		return ("ellipse - center: (" + nf.format(centerx) +
					", " + nf.format(centery) + ")" +
					", x-extent: " + nf.format(extx) + ", " +
					", y-extent: " + nf.format(exty) + "\n" );
	}
	
	public void draw(Graphics g, RPanel rp, Color c)
	{
		int xul, yul;
		int width, height;
		
		Color saveC = g.getColor();
		if (c != null)
			g.setColor(c);

		xul = rp.iX(centerx - extx);
		yul = rp.iY(centery + exty);
		width = rp.iL(2.0 * extx);
		height = rp.iL(2.0 * exty);
		g.drawOval(xul, yul, width, height);
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
	
	/* Compute ellipse metrics */
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
		return Math.PI*extx*exty;
	}

	/* Compute perimeter */
	public double computePerimeter()
	{
		double abdif = extx - exty;
		double absum = extx + exty;
		double h, p;
		h = (abdif*abdif) / (absum*absum);
		p = Math.PI * absum * (1 + 0.25*h + 0.0156*h*h + 0.0039*h*h*h);
		return p;
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
//		System.out.println("REllipse writeData - type: " + type);
//		System.out.println("REllipse writeData - title: " + ri.title);
		
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
			
			lineOut = "Oval: ";
			String fmt = DRAW3D_NUMBER_FORMAT;
			lineOut += 	String.format(fmt,centerx) + ", " +
						String.format(fmt,centery) + ", " +
						String.format(fmt,0.0) + ", " +
						String.format(fmt,extx) + ", " +
						String.format(fmt,exty) + ", " +
						"16, 90, 0, 0";
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
					    nf.format(extx) + ", " +
					    nf.format(exty));
		}
	}
	
	public elInfo getRInfo() {
		return ri;
	}

	public void setVertex(Pointd p, int i){}
	public Pointd getVertex(int i){return null;}
	public void add(Pointd p){}
	public void drawDecomp(Graphics g, RPanel rp){}
	public void findHull(){}
	public Hull getHull(){return null;}
	public void reSize(){}
	public int sizeDecomp(){return 0;}
	public int sizeHull(){return 0;}
	public void readData(BufferedReader in) throws IOException{}
	public void scaleToUnitSquare(){}
	public void drawSpecial(Graphics g, RPanel rp){}
}