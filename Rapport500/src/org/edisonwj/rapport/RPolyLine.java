package org.edisonwj.rapport;
/**
 * RPolyLine defines the class of polyline objects
 */

import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;

class RPolyLine extends RObject implements RapportDefaults
{
	private static final double epsilon = DEFAULTEPSILON;
	private static final double minx = RXMIN;
	private static final double miny = RYMIN;
	private static final double maxx = RXMAX;
	private static final double maxy = RYMAX;

	/* Region Bounding Vertices */
	private static final Pointd[] rb = {new Pointd(minx, miny),
										new Pointd(maxx, miny),
										new Pointd(maxx, maxy),
										new Pointd(minx, maxy)};

// 	private int nv;				/* Number of vertices */
	private Pointd[] v;			/* Array of vertices */
	protected lpInfo ri;		/* Link to RInfo */

	private double length;		/* Length of polyline */
	private double nonlinearity;/* Measure of non-linearity */

	public RPolyLine()
	{
		type = TYPE_POLYLINE;	
	}
	
	public RPolyLine(lpInfo ri, Rand mr)			/* Generate polyline */
	{
		this();
		this.ri = ri;

//		Randomly select number of vertices
		nv = mr.uniform(4, ri.num_items);
		genLine(ri, mr);
	}

	public RPolyLine(Pointd[] pa)
	{
		this();
		v = pa;
		nv = pa.length;
	}
	
	public RPolyLine(BufferedReader in) throws IOException
	{
		this();
//		System.out.println("RPolyLine(Buffered Reader) this: " + this.hashCode());
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
			ri = new lpInfo();
			ri.title = stitle;
//			System.out.println("RPolyLine this: " + this.hashCode());
//			System.out.println("RPolyLine ri: " + ri.hashCode() + ", ri.title: " + ri.title);
		}
		else
		{
			System.out.println("RPolyLine: Error constructing point distribution from file input");
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

//	Left to right generation
	private void genLine(lpInfo ri, Rand mr)
	{
		double junk;
		double xw, yw;

//		Compute ranges of coordinates
//		double xrange = ri.xmax - ri.xmin;
//		double yrange = ri.ymax - ri.ymin;

//		Generate x and y coordinates
		double[] x = new double[nv];
		double[] y = new double[nv];
		
		if ((ri.xuniform || ri.xnormal || ri.xexponential ||
			ri.yuniform || ri.ynormal || ri.yexponential) &&
			(!ri.xmmpp && !ri.ymmpp)) {
			
//			Generate x coordinates
			x[0] = 0.0;
			x[nv-1] = 1.0;
			for (int i = 1; i < nv-1; i++)
			{
				if (ri.xuniform)
					x[i] = mr.uniform(0.0, 1.0);
				else if (ri.xnormal)
					x[i] = mr.Normal(0.5, ri.std_dev);
				else if (ri.xexponential)
					x[i] = mr.exponential(0.0, 1.0);
			}
			
//			Generate y coordinates
			for (int i = 0; i < nv; i++)
			{
				if (ri.yuniform)
					y[i] = mr.uniform(0.0, 1.0);
				else if (ri.ynormal)
					y[i] = mr.Normal(0.5, ri.std_dev);
				else if (ri.yexponential)
					y[i] = mr.exponential(0.0, 1.0);
			}
		}

		else if (ri.xmmpp && ri.ymmpp) {
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
	 	 }
		
		else {
			nv = 0;
			System.out.println("Invalid paramenter combination.");
			return;
		}

//		Sort x coordinates
		Arrays.sort(x);

//		Smooth y coordinates
		double smoothparm = ri.smoothparm;
		if (ri.smooth1)
		{
			for (int i = 1; i < nv; i++)
			{
				y[i] = (y[i] + y[i-1])/smoothparm;
				junk = mr.uniform(0.0, 1.0);
			}
		}
		else if (ri.smooth2)
		{
			double delx, newy;
			for (int i = 1; i < nv; i++)
			{
				delx = (x[i] - x[i-1])/smoothparm;
				newy = (1.0 - delx)*y[i-1] + delx*mr.uniform(0.0, 1.0);
				if (y[i] > 0.0 && newy < 0.0)
					y[i] = mr.uniform(0.00, 0.25);
				else if (y[i] < 1.0 && newy > 1.0)
					y[i] = mr.uniform(0.75, 1.00);
				else
					y[i] = newy;
			}
		}
		else
		{
			for (int i = 1; i < nv; i++)
				junk = mr.uniform(0.0, 1.0);
		}

//		Rotate line
		if (ri.rotate)
		{
			double beta = mr.uniform(0.0, 2.0 * Math.PI);
			double centerx = 0.5;
//			double centerx = mr.uniform(0.0, 1.0);
			double centery = 0.5;
//			double centery = mr.uniform(0.0, 1.0);
			double newx;
			double newy;
			double c = Math.cos(beta);
			double s = Math.sin(beta);

			for (int i = 0; i < nv; i++)
			{
				newx = 	x[i]*c - y[i]*s
							- centerx*c + centery*s + centerx;
				newy =  x[i]*s + y[i]*c
							- centerx*s - centery*c + centery;
				x[i] = newx;
				y[i] = newy;

//				newx[i] = centerx
//						+ (x[i] - centerx)*c
//						- (y[i] - centery)*s;
//				newy[i] = centery
//						+ (x[i] - centerx)*s
//						+ (y[i] - centery)*c;
			}
		}

//		Get random numbers to keep seeds in sync for object comparison
		else
		{
			junk = mr.uniform(0.0, 1.0);
			junk = mr.uniform(0.0, 1.0);
//			junk = mr.uniform(0.0, 1.0);
		}

//		Generate vertices, insuring they are inside region
		int nc = 0;
		Pointd[] u = new Pointd[nv];
		for (int i = 0; i < nv;i++)
		{
			Pointd p = new Pointd(x[i],y[i]);
			if (inside(p))
				u[nc++] = p;
		}
		if (nc != nv)
		{
			nv = nc;
			v = new Pointd[nv];
			System.arraycopy(u,0,v,0,nv);
		}
		else
			v = u;

		if (nv <= 1)
		{
			nv = 2;
			v = new Pointd[nv];
			v[0] = new Pointd(minx + mr.uniform(0,maxx-minx), miny + mr.uniform(0,maxy-miny));
			v[1] = new Pointd(minx + mr.uniform(0,maxx-minx), miny + mr.uniform(0,maxy-miny));
		}

//		Move end vertices to boundary
		if (!onBoundary(v[0]))
			v[0] = findBoundaryInt(v[1], v[0]);
		if (!onBoundary(v[nv-1]))
			v[nv-1] = findBoundaryInt(v[nv-2], v[nv-1]);

//		If closer than epsilon, move to boundary.
		for (int i = 0; i < nv; i++)
		{
			xw = v[i].getx();
			yw = v[i].gety();

			if (Math.abs(xw - minx) < epsilon)
				v[i] = new Pointd(minx,yw);
			if (Math.abs(xw - maxx) < epsilon)
				v[i] = new Pointd(maxx,yw);
			if (Math.abs(yw - miny) < epsilon)
				v[i] = new Pointd(xw,miny);
			if (Math.abs(yw - maxy) < epsilon)
				v[i] = new Pointd(xw,maxy);
		}

		length = Geometry.distance(v[0], v[nv-1]);

		if ( ri.debug >= 3 )
			System.out.println("RPolyLine: length = " + length);

	}

	private boolean inside(Pointd v)
	{
		double x = v.getx();
		double y = v.gety();
		if (minx <= x && x <= maxx && miny <= y && y <= maxy)
			return true;
		else
			return false;
	}

	private boolean onBoundary(Pointd v)
	{
		if (	Geometry.between(rb[0], rb[1], v)
			||	Geometry.between(rb[1], rb[2], v)
			||  Geometry.between(rb[2], rb[3], v)
			||  Geometry.between(rb[3], rb[0], v) )
			return true;
		else
			return false;
	}

	private Pointd findBoundaryInt(Pointd v0, Pointd v1)
	{
//d		System.out.println("Start findBoundaryInt");
//d		System.out.println( 	"v0= " + v0 +
//d								", v1= " + v1);
		Pointd rp;
		IntersectNew[] it = new IntersectNew[2];
		Pointd[] itp = new Pointd[2];
		int[] itc = new int[2];
		int id;

		id = findDir(v0, v1);
		for (int i = 0; i < 2; i++)
		{
			it[i] = Geometry.RaySegIntNew(v0, v1, rb[id], rb[Geometry.next(id,4)]);
			itc[i] = it[i].getcode();
			id = Geometry.next(id,4);
		}

		if ((itc[0] == 1 || itc[0] == 2) && (itc[1] == 1 || itc[1] == 2))
		{
			itp[0] = it[0].getp();
			itp[1] = it[1].getp();
			if (Geometry.distance(itp[0], v1) < Geometry.distance(itp[1], v1))
				rp =  itp[0];
			else
				rp = itp[1];
		}
		else if (itc[0] == 1 || itc[0] == 2)
		{
			rp = it[0].getp();
		}
		else if (itc[1] == 1 || itc[1] == 2)
		{
			rp = it[1].getp();
		}
		else
			rp = null;

		if (rp != null)
		{
			double px = rp.getx();
			double py = rp.gety();
			if (px < minx)
				px = minx;
			if (px > maxx)
				px = maxx;
			if (py < miny)
				py = miny;
			if (py > maxy)
				py = maxy;
		}
		return rp;
	}

	public int findDir(Pointd v1, Pointd v2)
	{
		double xdiff;
		double ydiff;
		int code = -1;

		xdiff = v2.getx() - v1.getx();
		ydiff = v2.gety() - v1.gety();

		if (xdiff >= 0.0 && ydiff >= 0.0)
			code = 1;
		else if (xdiff <  0.0 && ydiff >= 0.0)
			code = 2;
		else if (xdiff <  0.0 && ydiff <  0.0)
			code = 3;
		else if (xdiff >= 0.0 && ydiff <  0.0)
			code = 0;

		return code;
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

//			/* Draw unit square */
//			PolygonA u = new PolygonA( new Pointd[]
//						{   new Pointd(-1., -1.),
//							new Pointd( 1., -1.),
//							new Pointd( 1.,  1.),
//							new Pointd(-1.,  1.) } );
//			u.draw(g, rp, Color.gray);

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

		System.out.println("#TYPE=" + type + " " + ri.title);
		System.out.println("#NV=" + nv);
		for (int i = 0; i < nv; i++)
			System.out.println(  nf.format(v[i].getx()) + ","
							+ nf.format(v[i].gety()) );
	}
	
	public void writeData(boolean draw3d, PrintWriter out) throws IOException
	{
//		System.out.println("RPolyLine writeData - type: " + type);
	
		String lineOut;
		String parmsOut = "";
		String distributionx = "";
		String distributiony = "";
		
		if (ri != null) {	
			if (ri.xuniform)
				distributionx = "x-uniform";
			else if (ri.xnormal)
				distributionx = "x-normal";
			else if (ri.xexponential)
				distributionx = "x-exponential";
			else if (ri.xmmpp)
				distributionx = "x-mmpp";
			
			if (ri.yuniform)
				distributiony = "y-uniform";
			else if (ri.ynormal)
				distributiony = "y-normal";
			else if (ri.yexponential)
				distributiony = "y-exponential";
			else if (ri.ymmpp)
				distributiony = "y-mmpp";
			parmsOut = "Distribution = "  + distributionx + " " + distributiony;
			
			if (ri.rotate)
				parmsOut += ", rotate";
			if (ri.smooth1)
				parmsOut += ", smooth1";
			if (ri.smooth2)
				parmsOut += ", smooth2";
			
			parmsOut += ", Number-of-points = " + ri.Snum_items +
						", Smooth-parm = " + ri.Ssmoothparm +
						", Standard-deviation = " + ri.Sstd_dev +
						", Density = " + ri.Sdensity;
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

	public Pointd get(int i)
	{
		return v[i];
	}

	public void setVertex(Pointd p, int i){}
	public Pointd getVertex(int i){return null;}
	public void add(Pointd p){}
	public void drawDecomp(Graphics g, RPanel rp){}
	public void findHull(){}
	public Hull getHull(){return null;}
	public void reSize(){}
	public int size(){return nv;}
	public int sizeDecomp(){return 0;}
	public int sizeHull(){return 0;}
	public void scaleToUnitSquare(){}
	public void drawSpecial(Graphics g, RPanel rp){}
	
	public lpInfo getRInfo() {
		return ri;
	}
}