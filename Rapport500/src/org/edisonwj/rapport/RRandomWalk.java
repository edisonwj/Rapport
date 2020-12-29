package org.edisonwj.rapport;

import java.awt.*;
import java.io.*;
import java.text.*;

/*
 * RRandomWalk generates rand walks using three differenct stratgies:
 * - A -fixed x increment and random (uniform, normal, or exponential) y increment
 * - B -random (uniform, normal, or exponential) x and y coordinates over specified ranges
 * - C -random MMPP x and y coordinates
 */
class RRandomWalk extends RObject implements RapportDefaults
{
	private Pointd[] v;			/* Array of vertices */
	protected rwInfo ri;		/* Link to RInfo */

	private double length;		/* Length of random walk */
	private double nonlinearity;/* Measure of non-linearity */

	public RRandomWalk()
	{
		type = TYPE_RANDOM_WALK;
	}
	
	public RRandomWalk(rwInfo ri, Rand mr)			/* Generate random walk */
	{
		this();
		this.ri = ri;

//		Select generation method
		if (ri.A)
			genWalkA(ri, mr);
		else if (ri.B)
			genWalkB(ri, mr);
		else if (ri.C)
			genWalkC(ri, mr);
	}

	public RRandomWalk(Pointd[] pa)
	{
		this();
		v = pa;
		nv = pa.length;
	}
	
	public RRandomWalk(BufferedReader in) throws IOException
	{
		this();
//		System.out.println("RRandomWalk(Buffered Reader) this: " + this.hashCode());
		String[] sttp;
		String stitle;
		sttp = readTypeTitleParms(in);
		type = Integer.parseInt(sttp[0]);
		stitle = sttp[1];
		nv = readNV(in);
		if (nv > 0)
		{
			v = new Pointd[nv];
			readData(in);
			ri = new rwInfo();
			ri.title = stitle;
//			System.out.println("RRandomWalk this: " + this.hashCode());
//			System.out.println("RRandomWalk ri: " + ri.hashCode() + ", ri.title: " + ri.title);
		}
		else
		{
			System.out.println("RRandomWalk: Error constructing point distribution from file input");
			nv = 0;
		}
	}
	
	public void readData(BufferedReader in) throws IOException
	{
		String s;
		for (int i=0; i < nv; i++)
		{
			s = in.readLine();
			v[i] = new Pointd(s);
		}
	}	

//	Walk in the park
//	Generate fixed x increment and random y increment
	private void genWalkA(rwInfo ri, Rand mr)
	{
//		System.out.println("Use fixed x increment and generate random y increment.");
		int n = ri.num_items;
		v = new Pointd[n];
		double xi = 1.0/n;
		double x = 0.0, y = 0.0;
		v[0] = new Pointd(x, y);
		for (nv = 1; nv < n; nv++) {
			x += xi;
			if (ri.yuniform)
				y = v[nv-1].gety() + mr.uniform(-1.0, 1.0)/(n-1);
			else if (ri.ynormal)
				y = v[nv-1].gety() + mr.Normal(0.0, 1.0)/(n-1);
			else if (ri.yexponential)
				y = v[nv-1].gety() + mr.exponential(-1.0, 1.0)/(n-1);
			
			v[nv] = new Pointd(x, y);
		}
	}

//		Compute ranges of coordinates
//		double xrange = ri.xmax - ri.xmin;
//		double yrange = ri.ymax - ri.ymin;

//	Generate random x and y coordinates
	private void genWalkB(rwInfo ri, Rand mr)
	{
//		System.out.println("Generate random x and y coordinates independently.");
		int n = ri.num_items;
		v = new Pointd[n];
		double x = 0.0;
		double y = 0.0;
		for (nv = 0; nv < n; nv++) {
			if (ri.xuniform)
				x = mr.uniform(ri.xmin, ri.xmax);
			else if (ri.xnormal)
				x = mr.Normal(0.0, ri.std_dev);
			else if (ri.xexponential)
				x = mr.exponential(ri.xmin, ri.xmax);
			
			if (ri.yuniform)
				y = mr.uniform(ri.ymin, ri.ymax);
			else if (ri.ynormal)
				y = mr.Normal(0.0, ri.std_dev);
			else if (ri.yexponential)
				y = mr.exponential(ri.ymin, ri.ymax);
			
			v[nv] = new Pointd(x, y);
		}
	}
	
// Generate x and y coordinates using mmpp
	private void genWalkC(rwInfo ri, Rand mr)
	{
//		System.out.println("Generate x and y coordinates independently using mmpp.");
		nv = ri.num_items;
		double[] x = new double[nv];
		double[] y = new double[nv];
		v = new Pointd[nv];			
		double density = 2.0;
		/* MAXCLUSTERS is the absolute maximum for MMPP */
		long max_xdiv = (long)Math.ceil( Math.sqrt ( (double) MAXCLUSTERS_MMPP) );
		long max_ydiv = (long)Math.ceil ( (double) MAXCLUSTERS_MMPP / max_xdiv);
		/* Note: the number of intervals is still random */
		int xdivisions = (int)mr.uniform (1, max_xdiv);
		int ydivisions = (int)mr.uniform (1, max_ydiv);
		double alpha_A = 1.0;
		/* Randomize size of B-intervals */
		double alpha_B = mr.uniform (0.5, 2.0);
		double lambda_A = 1.0;
		/* Linearly pick a high lambda based on density specified */
		double lambda_B = 1.0 + (HIGHLAMBDA - 1.0) * (density - 1.0) / 9.0;

		/* Generate x and y arrays independently */
 		x = mr.mmpp(nv, xdivisions, alpha_A, alpha_B, lambda_A, lambda_B, 1.0);
 		y = mr.mmpp(nv, ydivisions, alpha_A, alpha_B, lambda_A, lambda_B, 1.0);

 	 	for (int i = 0; i < nv; i++) {
 	 		v[i] = new Pointd(x[i], y[i]);
// 	 		System.out.println("MMPP Walk Point: " + v[i].toString());
 	 	}
	}

	private void genWalkD(rwInfo ri, Rand mr)
	{
		System.out.println("Option not implemented.");
	}

	public int getSize()
	{
		return nv;
	}

	public void draw(Graphics g, RPanel rp, Color c)
	{
		int x1, x2, y1, y2;
		int ylow, yhi;
		Color pc;

		if ( nv > 0 )
		{
			Color saveC = g.getColor();

			if (c != null)
				pc = c;
			else
				pc = saveC;

			ylow = rp.iY(0.0);
			yhi  = rp.iY(1.0);
			x1 = rp.iX(v[0].getx());
			y1 = rp.iY(v[0].gety());
			g.setColor(pc);
			g.fillOval(x1-2, y1-2, 5, 5);
			for (int i = 1; i < nv; i++)
			{
				x2 = rp.iX(v[i].getx());
				y2 = rp.iY(v[i].gety());

//				g.setColor(Color.gray);
//				g.drawLine(x2, ylow, x2, yhi);

				g.setColor(pc);
				g.fillOval(x2-2, y2-2, 5, 5);
				g.drawLine(x1, y1, x2, y2);

				x1 = x2;
				y1 = y2;
			}
			g.setColor(saveC);
		}
	}

	public void draw(Graphics g, RPanel rp)
	{
		draw(g, rp, null);
	}

	public void show()
	{
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(DEFAULT_MAX_FRACTION_DIGITS);
		nf.setMinimumFractionDigits(DEFAULT_MIN_FRACTION_DIGITS);
		nf.setMinimumIntegerDigits(DEFAULT_MIN_INTEGER_DIGITS);

		System.out.println("#TYPE=32 (Random Walk)");
		System.out.println("#NV=" + nv);
		for (int i = 0; i < nv; i++)
			System.out.println(  nf.format(v[i].getx()) + "|"
							+ nf.format(v[i].gety()) );
	}

	public Pointd get(int i)
	{
		return v[i];
	}
	
	public void writeData(boolean draw3d, PrintWriter out) throws IOException
	{
//		System.out.println("RRandomWalk writeData - type: " + type);
		
		String lineOut;
		String parmsOut = "";
		String distributionx = "";
		String distributiony = "";
		
		if (ri != null) {			
			if (ri.A) 
				parmsOut = "Random Y Incr.";
			else if (ri.B)
				parmsOut = "Random X and Y";
			else if (ri.C) {
				parmsOut = "MMPP X and Y";
				distributionx = "x-mmpp";
				distributiony = "y-mmpp";
			}
			
			if (! ri.C && ri.xuniform)
				distributionx = "x-uniform";
			else if (ri.xnormal)
				distributionx = "x-normal";
			else if (ri.xexponential)
				distributionx = "x-exponential";
			
			if (!ri.C && ri.yuniform)
				distributiony = "y-uniform";
			else if (ri.ynormal)
				distributiony = "y-normal";
			else if (ri.yexponential)
				distributiony = "y-exponential";

			parmsOut += ", Distribution = "  + distributionx + " " + distributiony;
			
			
			parmsOut += ", Number-of-points = " + ri.Snum_items +
						", Standard-deviation = " + ri.Sstd_dev;
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

			lineOut = "PolyLine: ";
			String fmt = DRAW3D_NUMBER_FORMAT;
			for (int i = 0; i < nv; i++) {
				lineOut += 	String.format(fmt,v[i].getx()) + ", " +
							String.format(fmt,v[i].gety()) + ", " +
							String.format(fmt,0.0) + "; ";
			}
			out.println(lineOut);
		}
		
		/* Create standard output */
		else {
			NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMaximumFractionDigits(DEFAULT_MAX_FRACTION_DIGITS);
			nf.setMinimumFractionDigits(DEFAULT_MIN_FRACTION_DIGITS);
			nf.setMinimumIntegerDigits(DEFAULT_MIN_INTEGER_DIGITS);
	
			out.println("#TYPE=" + type + " " + ri.title);
			out.println("#PARMS=" + parmsOut);
			out.println("#NV=" + nv);
			for (int i = 0; i < nv; i++)
				out.println(nf.format(v[i].getx()) + "," +
							nf.format(v[i].gety()) );
		}
	}

	public void setVertex(Pointd p, int i){}
	public Pointd getVertex(int i){return null;}
	public void add(Pointd p){}
	public void drawDecomp(Graphics g, RPanel rp){}
	public void findHull(){}
	public Hull getHull(){return null;}
	public int sizeHull(){return 0;}
	public void reSize(){}
	public int size(){return nv;}
	public int sizeDecomp(){return 0;}
	public void scaleToUnitSquare(){}
	public void drawSpecial(Graphics g, RPanel rp){}
	
	public rwInfo getRInfo() {
		return ri;
	}
	
}