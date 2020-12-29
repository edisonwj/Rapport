package org.edisonwj.rapport;

/**
 * RectangleR is the general abstract class for rectangle specification.
 * Four derivative classes specified depending on the form of rectangle 
 * definition, namely:
 * 	RectangleR2ce - lower left corner and side extents
 *  RectangleR2cn - rectangle center and half extents 
 *  RectangleR2cr - rectangle lower left and upper right corners
 */

import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

public abstract class RectangleR extends RObject
{
	protected rcInfo ri;				/* Link to RInfo */
	protected int dimension;

	public int getDimension()
	{
		return dimension;
	}

	public void setDimension(int d)
	{
		dimension = d;
	}
	
	public rcInfo getRInfo() {
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
	public void writeData(boolean draw3d, PrintWriter out) throws IOException{}
	public void readData(BufferedReader in) throws IOException{}
	public void scaleToUnitSquare(){}
	public void drawSpecial(Graphics g, RPanel rp){}
}

class RectangleR2cr extends RectangleR
{
	private double lowleftx;		/* Lower left corner */
	private double lowlefty;
	private double uprightx;		/* Upper right corner */
	private double uprighty;

	public RectangleR2cr()
	{
		type = TYPE_RECTANGLER2CR;
		dimension = 2;
		nv = 4;
	}
	
	public RectangleR2cr(rcInfo ri, double llx, double lly, double urx, double ury)
	{
		this();
		this.ri = ri;
		lowleftx = llx;
		lowlefty = lly;
		uprightx = urx;
		uprighty = ury;
	}
	
	public RectangleR2cr(rcInfo ri)
	{
		this();
		this.ri = ri;
	}
	
	public RectangleR2cr(BufferedReader in) throws IOException
	{
		this();
//		System.out.println("RectangleR2cr(Buffered Reader) this: " + this.hashCode());
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
			lowleftx = Double.parseDouble(t.nextToken());
			lowlefty = Double.parseDouble(t.nextToken());
			uprightx = Double.parseDouble(t.nextToken());
			uprighty = Double.parseDouble(t.nextToken());
			ri = new rcInfo();
			ri.title = stitle;
//			System.out.println("RectangleR2cr this: " + this.hashCode());
//			System.out.println("RectangleR2cr ri: " + ri.hashCode() + ", ri.title: " + ri.title);
		}
		else
		{
			System.out.println("RectangleR2cr: Error constructing point distribution from file input");
			nv = 0;
		}
	}
	
	public ArrayList genRectangleR2cr(rcInfo ri, Rand mr)
	{
		double xll, yll;
		double xur, yur;
		double minext, maxext;
		ArrayList rl = new ArrayList();
		
		if (ri.wrap)
			System.out.println("Wrap-around option has no effect with the corners method.");
		else if (ri.force)
			System.out.println("Force option has no effect with the corners method.");
			
		
		/* Randomly alternate generating lower left or upper right first */
		int x = mr.uniform(0,  1);			
		
		if (x == 0) {
			/* Generate lower left */
			xll = mr.uniform(0.0, 1.0 - ri.minlen);
			yll = mr.uniform(0.0, 1.0 - ri.minlen);
	
			/* Generate upper right */
			minext = ri.minlen;		
			maxext = Math.min(1.0 - xll, ri.maxlen);
			xur = mr.uniform(xll + minext, xll + maxext);
	
			maxext = Math.min(1.0 - yll, ri.maxlen);
			yur = mr.uniform(yll + minext, yll + maxext);
			
			rl.add(new RectangleR2cr(ri, xll, yll, xur, yur));
//			System.out.println("RectangleR2cr(0): " + rl.get(rl.size()-1).toString());
		
		}
		else {
			/* Generate upper right */
			xur = mr.uniform(ri.minlen, 1.0);
			yur = mr.uniform(ri.minlen, 1.0);

			/* Generate lower left */
			minext = ri.minlen;		
			maxext = Math.min(xur, ri.maxlen);
			xll = mr.uniform(0.0, maxext);
			yll = mr.uniform(0.0, maxext);
			
			rl.add(new RectangleR2cr(ri, xll, yll, xur, yur));
//			System.out.println("RectangleR2cr(1): " + rl.get(rl.size()-1).toString());
		}
		return rl;
	}

	public int size()
	{
		return 4;
	}
	
	public void writeData(boolean draw3d, PrintWriter out) throws IOException
	{
//		System.out.println("RectangleR RectangleR2cr writeData - type: " + type);
//		System.out.println("RectangleR RectangleR2cr writeData - title: " + ri.title);
		
		double extx = Math.abs(uprightx - lowleftx);
		double exty =  Math.abs(uprighty - lowlefty);
		double centerx = lowleftx + .5 * extx;
		double centery = lowlefty + .5 * exty;
		
		String lineOut = "";
		String parmsOut = "";
		
		if (ri != null) {
			parmsOut += "Corners Min. side length = " + ri.minlen + " Max. side length = " + ri.maxlen;
		}
		else
			parmsOut = "Parameters unknown";
		
		/* Create output for Draw3D */
		if (draw3d) {
			lineOut = "DataGroup:";
			out.println(lineOut);
			lineOut = "Title1: " + ri.title;
			out.println(lineOut);
			lineOut = "Title2: " + parmsOut;
			out.println(lineOut);

			lineOut = "Rectangle: ";
			String fmt = DRAW3D_NUMBER_FORMAT;
			lineOut += 	String.format(fmt,centerx) + ", " +
						String.format(fmt,centery) + ", " +
						String.format(fmt,0.0) + ", " +
						String.format(fmt,extx) + ", " +
						String.format(fmt,exty) + ", " +
						"90, 0, 0, LINE, NONE";
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
			
			out.println(nf.format(lowleftx) + ", " +
						nf.format(lowlefty) + ", " +
						nf.format(uprightx) + ", " +
						nf.format(uprighty));
			
//			System.out.println(nf.format(lowleftx) + ", " +
//					nf.format(lowlefty) + ", " +
//					nf.format(uprightx) + ", " +
//					nf.format(uprighty));
		}
	}

	public String toString()
	{
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(4);
		return ("2cr - lower-left: (" + nf.format(lowleftx) +
					", " + nf.format(lowlefty) + ")" +
					", upper-right: (" + nf.format(uprightx) +
					", " + nf.format(uprighty) + ")\n" );
	}

	public void draw(Graphics g, RPanel rp)
	{
		int x, y;

		Polygon p = new Polygon();

		/* Lower left corner */
		x = rp.iX(lowleftx);
		y = rp.iY(lowlefty);
		p.addPoint(x, y);
		g.fillOval(x-2, y-2, 5, 5);

		/* Lower right corner */
		x = rp.iX(uprightx);
		y = rp.iY(lowlefty);
		p.addPoint(x, y);
		g.fillOval(x-2, y-2, 5, 5);

		/* Upper right corner */
		x = rp.iX(uprightx);
		y = rp.iY(uprighty);
		p.addPoint(x, y);
		g.fillOval(x-2, y-2, 5, 5);

		/* Upper left corner */
		x = rp.iX(lowleftx);
		y = rp.iY(uprighty);
		p.addPoint(x, y);
		g.fillOval(x-2, y-2, 5, 5);

		g.drawPolygon(p);
	}
}

class RectangleR2cn extends RectangleR
{
	private double centerx;		/* Rectangle center */
	private double centery;
	private double extx;		/* Rectangle half extents */
	private double exty;

	public RectangleR2cn()
	{
		type = TYPE_RECTANGLER2CN;
		dimension = 2;
		nv = 4;
	}
	
	public RectangleR2cn(rcInfo ri, double x, double y, double xl, double yl)
	{
		this();
		this.ri = ri;
		centerx = x;
		centery = y;
		extx = xl;
		exty = yl;
	}
	
	public RectangleR2cn(rcInfo ri)
	{
		this();
		this.ri = ri;
	}
	
	public RectangleR2cn(BufferedReader in) throws IOException
	{
		this();
//		System.out.println("RectangleR2cn(Buffered Reader) this: " + this.hashCode());
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
			ri = new rcInfo();
			ri.title = stitle;
//			System.out.println("RectangleR2cn this: " + this.hashCode());
//			System.out.println("RectangleR2cn ri: " + ri.hashCode() + ", ri.title: " + ri.title);
		}
		else
		{
			System.out.println("RectangleR2cn: Error constructing point distribution from file input");
			nv = 0;
		}
	}
	
	public ArrayList genRectangleR2cn(rcInfo ri, Rand mr)
	{
		double x,y;
		double xl,yl;
		double minext = ri.minlen * 0.5;
		double maxext = ri.maxlen * 0.5;
		ArrayList rl = new ArrayList();

		if (!ri.force && !ri.wrap) {
			System.out.println("Method must use Force or Wrap-around. Defaults to Force.");
			ri.force = true;
		}
		
		if (ri.force)
		{
			/* Generate center */
			x = mr.uniform(0.0 + minext, 1.0 - minext);
			y = mr.uniform(0.0 + minext, 1.0 - minext);

			/* Generate half extents */
			do
			{
				xl = mr.uniform(minext, maxext);
			} while (x - xl <= 0.0 || x + xl >= 1.0);

			do
			{
				yl = mr.uniform(minext, maxext);
			} while (y - yl <= 0.0 || y + yl >= 1.0);

			rl.add(new RectangleR2cn(ri, x, y, xl, yl));
		}
		else if (ri.wrap)
		{
			double xext, yext, xdif, ydif;
			
			/* Generate center */
			x = mr.uniform(0.0 + minext, 1.0 - minext);
			y = mr.uniform(0.0 + minext, 1.0 - minext);

			/* Generate half extents */
			xext = mr.uniform(ri.minlen, ri.maxlen) * 0.5;
			yext = mr.uniform(ri.minlen, ri.maxlen) * 0.5;
			
			xdif = x + xext - 1.0;
			ydif = y + yext - 1.0;
			
//			System.out.println("x: " + x + ", y:" + y +
//								", xext: " + xext + ", yext: " + yext);

			double wxext, wyext;
			if (xdif > 0.0 && ydif > 0.0)
			{
//				System.out.println("(xdif > 0.0 && ydif > 0.0)");
//				System.out.println("xdif: " + xdif + ", ydif: " + ydif);
				wxext = 1.0 - x;
				wyext = 1.0 - y;
				if (wxext >= ri.minlen && wyext >= ri.minlen &&
					wxext <= ri.maxlen && wyext <= ri.maxlen) {
					rl.add(new RectangleR2ce(ri, x, y, wxext, wyext));
//					System.out.println("+++Add -00: " + rl.get(rl.size()-1).toString());
				}
//				else
//					System.out.println("***Fail-00 wxext: " + wxext + ", wyext: " + wyext);
				wxext = xdif;
				wyext = 1.0 - y;
				if (wxext >= ri.minlen && wyext >= ri.minlen &&
					wxext <= ri.maxlen && wyext <= ri.maxlen) {
					rl.add(new RectangleR2ce(ri, 0.0, y, wxext, wyext));
//					System.out.println("+++Add -01: " + rl.get(rl.size()-1).toString());
				}
//				else
//					System.out.println("***Fail-01 wxext: " + wxext + ", wyext: " + wyext);
				wxext = 1.0 - x;
				wyext = ydif;
				if (wxext >= ri.minlen && wyext >= ri.minlen &&
					wxext <= ri.maxlen && wyext <= ri.maxlen) {
					rl.add(new RectangleR2ce(ri, x, 0.0, wxext, wyext));
//					System.out.println("+++Add -02: " + rl.get(rl.size()-1).toString());
				}
//				else
//					System.out.println("***Fail-02 wxext: " + wxext + ", wyext: " + wyext);
				wxext = xdif;
				wyext = ydif;
				if (wxext >= ri.minlen && wyext >= ri.minlen &&
					wxext <= ri.maxlen && wyext <= ri.maxlen) {
					rl.add(new RectangleR2ce(ri, 0.0, 0.0, wxext, wyext));
//					System.out.println("+++Add -03: " + rl.get(rl.size()-1).toString());
				}
//				else
//					System.out.println("***Fail-03 wxext: " + wxext + ", wyext: " + wyext);
			}

			else if (xdif > 0.0)
			{
//				System.out.println("(xdif > 0.0)");
//				System.out.println("xdif: " + xdif + ", ydif: " + ydif);
				wxext = 1.0 - x;
				wyext = yext;
				if (wxext >= ri.minlen && wyext >= ri.minlen &&
					wxext <= ri.maxlen && wyext <= ri.maxlen) {
					rl.add(new RectangleR2ce(ri, x, y, wxext, wyext));
//					System.out.println("+++Add -10: " + rl.get(rl.size()-1).toString());
				}
//				else
//					System.out.println("***Fail-10 wxext: " + wxext + ", wyext: " + wyext);
				wxext = xdif;
				wyext = yext;
				if (wxext >= ri.minlen && wyext >= ri.minlen &&
					wxext <= ri.maxlen && wyext <= ri.maxlen) {
					rl.add(new RectangleR2ce(ri, 0.0, y, wxext, wyext));
//					System.out.println("+++Add -11: " + rl.get(rl.size()-1).toString());
				}
//				else
//					System.out.println("***Fail-11 wxext: " + wxext + ", wyext: " + wyext);
			}

			else if (ydif > 0.0)
			{
//				System.out.println("(ydif > 0.0)");
//				System.out.println("xdif: " + xdif + ", ydif: " + ydif);
				wxext = xext;
				wyext = 1.0 - y;
				if (wxext >= ri.minlen && wyext >= ri.minlen &&
					wxext <= ri.maxlen && wyext <= ri.maxlen) {
					rl.add(new RectangleR2ce(ri, x, y, wxext, wyext));
//					System.out.println("+++Add -20: " + rl.get(rl.size()-1).toString());
				}
//				else
//					System.out.println("***Fail-20 wxext: " + wxext + ", wyext: " + wyext);
				wxext = xext;
				wyext = ydif;
				if (wxext >= ri.minlen && wyext >= ri.minlen &&
					wxext <= ri.maxlen && wyext <= ri.maxlen) {
					rl.add(new RectangleR2ce(ri, x, 0.0, wxext, wyext));
//					System.out.println("+++Add -21: " + rl.get(rl.size()-1).toString());
				}
//				else
//					System.out.println("***Fail-21 wxext: " + wxext + ", wyext: " + wyext);
			}

			else
			{
//				System.out.println("other");
//				System.out.println("xdif: " + xdif + ", ydif: " + ydif);
				rl.add(new RectangleR2ce(ri, x, y, xext, yext));
//				System.out.println("+++Add -30: " + rl.get(rl.size()-1).toString());
			}
		}

		return rl;
	}

	public int size()
	{
		return 4;
	}
	
	public void writeData(boolean draw3d, PrintWriter out) throws IOException
	{
//		System.out.println("RectangleR RectangleR2cr writeData - type: " + type);
//		System.out.println("RectangleR RectangleR2cr writeData - title: " + ri.title);
		
		String lineOut = "";
		String parmsOut = "";
		
		if (ri != null) {
			parmsOut += "Center-Extents ";
			if (ri.force)
				parmsOut += "Force ";
			else if (ri.wrap)
				parmsOut += "Wrap-around ";
			parmsOut += "Min. side length = " + ri.minlen + " Max. side length = " + ri.maxlen;
		}
		else
			parmsOut = "Parameters unknown";
		
		/* Create output for Draw3D */
		if (draw3d) {
			lineOut = "DataGroup:";
			out.println(lineOut);
			lineOut = "Title1: " + ri.title;
			out.println(lineOut);
			lineOut = "Title2: " + parmsOut;
			out.println(lineOut);

			lineOut = "Rectangle: ";
			String fmt = DRAW3D_NUMBER_FORMAT;
			lineOut += 	String.format(fmt,centerx) + ", " +
						String.format(fmt,centery) + ", " +
						String.format(fmt,0.0) + ", " +
						String.format(fmt,extx) + ", " +
						String.format(fmt,exty) + ", " +
						"90, 0, 0, LINE, NONE";
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
	
	public String toString()
	{
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(4);
		return ("2cn - center: (" + nf.format(centerx) +
					", " + nf.format(centery) + ")" +
					", half-extents: (" + nf.format(extx) +
					", " + nf.format(exty) + ")\n" );
	}

	public void draw(Graphics g, RPanel rp)
	{
		int x, y;

		Polygon p = new Polygon();

		/* Lower left corner */
		x = rp.iX(centerx - extx);
		y = rp.iY(centery - exty);
		p.addPoint(x, y);
		g.fillOval(x-2, y-2, 5, 5);

		/* Lower right corner */
		x = rp.iX(centerx + extx);
		y = rp.iY(centery - exty);
		p.addPoint(x, y);
		g.fillOval(x-2, y-2, 5, 5);

		/* Upper right corner */
		x = rp.iX(centerx + extx);
		y = rp.iY(centery + exty);
		p.addPoint(x, y);
		g.fillOval(x-2, y-2, 5, 5);

		/* Upper left corner */
		x = rp.iX(centerx - extx);
		y = rp.iY(centery + exty);
		p.addPoint(x, y);
		g.fillOval(x-2, y-2, 5, 5);

		g.drawPolygon(p);
	}
}

class RectangleR2ce extends RectangleR
{
	private double lowleftx;		/* Lower left corner */
	private double lowlefty;
	private double extx;			/* Side lengths */
	private double exty;

	public RectangleR2ce()
	{
		type = TYPE_RECTANGLER2CE;
		dimension = 2;
		nv = 4;
	}
	
	public RectangleR2ce(rcInfo ri, double llx, double lly, double ex, double ey)
	{
		this();
		this.ri = ri;
		lowleftx = llx;
		lowlefty = lly;
		extx = ex;
		exty = ey;
	}
	
	public RectangleR2ce(rcInfo ri)
	{
		this();
		this.ri = ri;
	}
	
	public RectangleR2ce(BufferedReader in) throws IOException
	{
		this();
//		System.out.println("RectangleR2ce(Buffered Reader) this: " + this.hashCode());
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
			lowleftx = Double.parseDouble(t.nextToken());
			lowlefty = Double.parseDouble(t.nextToken());
			extx = Double.parseDouble(t.nextToken());
			exty = Double.parseDouble(t.nextToken());
			ri = new rcInfo();
			ri.title = stitle;
//			System.out.println("RectangleR2ce this: " + this.hashCode());
//			System.out.println("RectangleR2ce ri: " + ri.hashCode() + ", ri.title: " + ri.title);
		}
		else
		{
			System.out.println("RectangleR2ce: Error constructing point distribution from file input");
			nv = 0;
		}
	}
	
	public ArrayList genRectangleR2ce(rcInfo ri, Rand mr)
	{
		double xll, yll;
		double xext, yext;
		double xdif, ydif;
		double minext, maxext;
		ArrayList rl = new ArrayList();

		if (!ri.force && !ri.wrap) {
			System.out.println("Method must use Force or Wrap-around. Defaults to Force.");
			ri.force = true;
		}
			
		if (ri.force)
		{
			/* Generate lower left */
			xll = mr.uniform(0.0, 1.0 - ri.minlen);
			yll = mr.uniform(0.0, 1.0 - ri.minlen);

			/* Generate extents */
			minext = ri.minlen;

			maxext = Math.min(1.0 - xll, ri.maxlen);
			xext = mr.uniform(minext, maxext);

			maxext = Math.min(1.0 - yll, ri.maxlen);
			yext = mr.uniform(minext, maxext);

			rl.add(new RectangleR2ce(ri, xll, yll, xext, yext));
		}

		else if (ri.wrap)
		{
			/* Generate lower left */
			xll = mr.uniform(0.0, 1.0);
			yll = mr.uniform(0.0, 1.0);

			/* Generate extents */
			xext = mr.uniform(ri.minlen, ri.maxlen);
			yext = mr.uniform(ri.minlen, ri.maxlen);
			
			xdif = xll + xext - 1.0;
			ydif = yll + yext - 1.0;
			
//			System.out.println("xll: " + xll + ", yll:" + yll +
//								", xext: " + xext + ", yext: " + yext);

			double wxext, wyext;
			if (xdif > 0.0 && ydif > 0.0)
			{
//				System.out.println("(xdif > 0.0 && ydif > 0.0)");
//				System.out.println("xdif: " + xdif + ", ydif: " + ydif);
				wxext = 1.0 - xll;
				wyext = 1.0 - yll;
				if (wxext >= ri.minlen && wyext >= ri.minlen &&
					wxext <= ri.maxlen && wyext <= ri.maxlen) {
					rl.add(new RectangleR2ce(ri, xll, yll, wxext, wyext));
//					System.out.println("+++Add -00: " + rl.get(rl.size()-1).toString());
				}
//				else
//					System.out.println("***Fail-00 wxext: " + wxext + ", wyext: " + wyext);
				wxext = xdif;
				wyext = 1.0 - yll;
				if (wxext >= ri.minlen && wyext >= ri.minlen &&
					wxext <= ri.maxlen && wyext <= ri.maxlen) {
					rl.add(new RectangleR2ce(ri, 0.0, yll, wxext, wyext));
//					System.out.println("+++Add -01: " + rl.get(rl.size()-1).toString());
				}
//				else
//					System.out.println("***Fail-01 wxext: " + wxext + ", wyext: " + wyext);
				wxext = 1.0 - xll;
				wyext = ydif;
				if (wxext >= ri.minlen && wyext >= ri.minlen &&
					wxext <= ri.maxlen && wyext <= ri.maxlen) {
					rl.add(new RectangleR2ce(ri, xll, 0.0, wxext, wyext));
//					System.out.println("+++Add -02: " + rl.get(rl.size()-1).toString());
				}
//				else
//					System.out.println("***Fail-02 wxext: " + wxext + ", wyext: " + wyext);
				wxext = xdif;
				wyext = ydif;
				if (wxext >= ri.minlen && wyext >= ri.minlen &&
					wxext <= ri.maxlen && wyext <= ri.maxlen) {
					rl.add(new RectangleR2ce(ri, 0.0, 0.0, wxext, wyext));
//					System.out.println("+++Add -03: " + rl.get(rl.size()-1).toString());
				}
//				else
//					System.out.println("***Fail-03 wxext: " + wxext + ", wyext: " + wyext);
			}

			else if (xdif > 0.0)
			{
//				System.out.println("(xdif > 0.0)");
//				System.out.println("xdif: " + xdif + ", ydif: " + ydif);
				wxext = 1.0 - xll;
				wyext = yext;
				if (wxext >= ri.minlen && wyext >= ri.minlen &&
					wxext <= ri.maxlen && wyext <= ri.maxlen) {
					rl.add(new RectangleR2ce(ri, xll, yll, wxext, wyext));
//					System.out.println("+++Add -10: " + rl.get(rl.size()-1).toString());
				}
//				else
//					System.out.println("***Fail-10 wxext: " + wxext + ", wyext: " + wyext);
				wxext = xdif;
				wyext = yext;
				if (wxext >= ri.minlen && wyext >= ri.minlen &&
					wxext <= ri.maxlen && wyext <= ri.maxlen) {
					rl.add(new RectangleR2ce(ri, 0.0, yll, wxext, wyext));
//					System.out.println("+++Add -11: " + rl.get(rl.size()-1).toString());
				}
//				else
//					System.out.println("***Fail-11 wxext: " + wxext + ", wyext: " + wyext);
			}

			else if (ydif > 0.0)
			{
//				System.out.println("(ydif > 0.0)");
//				System.out.println("xdif: " + xdif + ", ydif: " + ydif);
				wxext = xext;
				wyext = 1.0 - yll;
				if (wxext >= ri.minlen && wyext >= ri.minlen &&
					wxext <= ri.maxlen && wyext <= ri.maxlen) {
					rl.add(new RectangleR2ce(ri, xll, yll, wxext, wyext));
//					System.out.println("+++Add -20: " + rl.get(rl.size()-1).toString());
				}
//				else
//					System.out.println("***Fail-20 wxext: " + wxext + ", wyext: " + wyext);
				wxext = xext;
				wyext = ydif;
				if (wxext >= ri.minlen && wyext >= ri.minlen &&
					wxext <= ri.maxlen && wyext <= ri.maxlen) {
					rl.add(new RectangleR2ce(ri, xll, 0.0, wxext, wyext));
//					System.out.println("+++Add -21: " + rl.get(rl.size()-1).toString());
				}
//				else
//					System.out.println("***Fail-21 wxext: " + wxext + ", wyext: " + wyext);
			}

			else
			{
//				System.out.println("other");
//				System.out.println("xdif: " + xdif + ", ydif: " + ydif);
				rl.add(new RectangleR2ce(ri, xll, yll, xext, yext));
//				System.out.println("+++Add -30: " + rl.get(rl.size()-1).toString());
			}
		}

		return rl;
	}

	public int size()
	{
		return 4;
	}
	
	public void writeData(boolean draw3d, PrintWriter out) throws IOException
	{
//		System.out.println("RectangleR RectangleR2cr writeData - type: " + type);
//		System.out.println("RectangleR RectangleR2cr writeData - title: " + ri.title);
		
		double centerx = lowleftx + extx * .5;
		double centery = lowlefty + exty * .5;
		
		String lineOut = "";
		String parmsOut = "";
		
		if (ri != null) {
			parmsOut += "Corner-Extents ";
			if (ri.force)
				parmsOut += "Force ";
			else if (ri.wrap)
				parmsOut += "Wrap-around ";
			parmsOut += "Min. side length = " + ri.minlen + " Max. side length = " + ri.maxlen;
		}
		else
			parmsOut = "Parameters unknown";
		
		/* Create output for Draw3D */
		if (draw3d) {
			lineOut = "DataGroup:";
			out.println(lineOut);
			lineOut = "Title1: " + ri.title;
			out.println(lineOut);
			lineOut = "Title2: " + parmsOut;
			out.println(lineOut);
			
			lineOut = "Rectangle: ";
			String fmt = DRAW3D_NUMBER_FORMAT;
			lineOut += 	String.format(fmt,centerx) + ", " +
						String.format(fmt,centery) + ", " +
						String.format(fmt,0.0) + ", " +
						String.format(fmt,extx) + ", " +
						String.format(fmt,exty) + ", " +
						"90, 0, 0, LINE, NONE";
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
			out.println(nf.format(lowleftx) + ", " +
					    nf.format(lowlefty) + ", " + 
					    nf.format(extx) + ", " +
					    nf.format(exty));
		}
	}

	public String toString()
	{
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(4);
		return ("2ce - lower-left: (" + nf.format(lowleftx) +
					", " + nf.format(lowlefty) + ")" +
					", side-lengths: (" + nf.format(extx) +
					", " + nf.format(exty) + ")\n" );
	}

	public void draw(Graphics g, RPanel rp)
	{
		int x, y;

		Polygon p = new Polygon();

		/* Lower left corner */
		x = rp.iX(lowleftx);
		y = rp.iY(lowlefty);
		p.addPoint(x, y);
		g.fillOval(x-2, y-2, 5, 5);

		/* Lower right corner */
		x = rp.iX(lowleftx + extx);
		y = rp.iY(lowlefty);
		p.addPoint(x, y);
		g.fillOval(x-2, y-2, 5, 5);

		/* Upper right corner */
		x = rp.iX(lowleftx + extx);
		y = rp.iY(lowlefty + exty);
		p.addPoint(x, y);
		g.fillOval(x-2, y-2, 5, 5);

		/* Upper left corner */
		x = rp.iX(lowleftx);
		y = rp.iY(lowlefty + exty);
		p.addPoint(x, y);
		g.fillOval(x-2, y-2, 5, 5);

		g.drawPolygon(p);
	}
}
