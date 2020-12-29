package org.edisonwj.rapport;

/**
 * RPointSet defines a point distribution object.
 */

import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;

class RPointSet extends RObject implements RapportDefaults
{
	protected Pointd [] pa;
	protected Hull h;
	protected ptInfo ri;				/* Link to RInfo */

	protected double area;
	protected double hullArea;
	protected double areaRatio;
	protected double perimeter;
	protected double hullPerimeter;
	protected double perRatio;
	protected double compacity;
	protected double chi2;

	public RPointSet()
	{
		type = TYPE_POINT_SET;
		nv = 0;
	}
	
	public RPointSet(int np)
	{
		this();
		pa = new Pointd[np];
	}

	public RPointSet(ptInfo ri, Rand mr)
	{
		this(ri.num_items);
		this.ri = ri;

		if ( ri.uniform )
			uniform_point_generation(ri, mr);
		else if ( ri.normal )
			bivariate_point_generation(ri, mr);
		else if ( ri.poisson )
			nonstationary_poisson(ri, mr);
		else
			System.out.println("RPointSet:constructor - parameter error");
	}

	public RPointSet(BufferedReader in) throws IOException
	{
		this();
//		System.out.println("RPointSet(Buffered Reader) this: " + this.hashCode());
		String[] sttp;
		String stitle;
		sttp = readTypeTitleParms(in);
		type = Integer.parseInt(sttp[0]);
		stitle = sttp[1];
		nv = readNV(in);
		if (nv >= 0)
		{
			pa = new Pointd[nv];
			readData(in);
			ri = new ptInfo(pa.length);
			ri.title = stitle;
//			System.out.println("RPointSet this: " + this.hashCode());
//			System.out.println("RPointSet ri: " + ri.hashCode() + ", ri.title: " + ri.title);
		}
		else
		{
			System.out.println("RPointSet: Error constructing point distribution from file input");
			pa = null;
			nv = 0;
		}
	}

	public void readData(BufferedReader in) throws IOException
	{
		String s;
		for (int i=0; i < nv; i++)
		{
			s = in.readLine();
			pa[i] = new Pointd(s);
		}
	}

	public boolean isGend()
	{
		return (pa != null && nv == pa.length);
	}

	public void reSize()
	{
		Pointd[] N = new Pointd[nv];
		System.arraycopy( pa, 0, N, 0, nv );
		pa = N;
	}

	public void add(Pointd p)
	{
		pa[nv++] = p;
	}

	public void set(Pointd p, int i)
	{
		pa[i] = p;
	}

	public Pointd get(int i)
	{
		return pa[i];
	}

	public int size()
	{
		return pa.length;
	}

	public void setVertex(Pointd p, int i)
	{
	}

	public Pointd getVertex(int i)
	{
		return null;
	}

	public int sizeHull()
	{
		if (h == null)
			return 0;
		else
			return h.size();
	}

	public void draw(Graphics g, RPanel rp)
	{
		int i, x, y;

		g.setColor(Color.blue);
		for (i = 0; i < nv; i++)
		{
			x = rp.iX(pa[i].getx());
			y = rp.iY(pa[i].gety());
			g.fillOval(x-1, y-1, 2, 2);
		}
	}

	public void findHull()
	{
		h = new Hull(pa);
	}

	public Hull getHull()
	{
		if (h == null)
			this.findHull();
		return h;
	}

	public void writeData(boolean draw3d, PrintWriter out) throws IOException
	{
//		System.out.println("RPointSet writeData type: " + type);
		
		String lineOut;
		String parmsOut;
		String distribution;
		
		if (ri != null) {
			if (ri.uniform)
				distribution = "Uniform";
			else if (ri.normal)
				distribution = "Normal";
			else if (ri.poisson)
				distribution = "Poisson";
			else
				distribution = "Unknown";
			
			parmsOut = "Distribution = " + distribution + 
						", Number-of-points = " + ri.Snum_items +
						", Number-of-clusters = " + ri.Snum_clusters +
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

			for (int i = 0; i < nv; i++) {
				lineOut = "Point: ";
				String fmt = DRAW3D_NUMBER_FORMAT;
				lineOut += String.format(fmt,pa[i].getx()) + ", " +
						   String.format(fmt,pa[i].gety()) + ", " +
						   String.format(fmt,0.0);
				out.println(lineOut);
			}
		}
		else {	
			NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMaximumFractionDigits(DEFAULT_MAX_FRACTION_DIGITS);
			nf.setMinimumFractionDigits(DEFAULT_MIN_FRACTION_DIGITS);
			nf.setMinimumIntegerDigits(DEFAULT_MIN_INTEGER_DIGITS);
	
			out.println("#TYPE=" + type + " " + ri.title);
			out.println("#PARMS=" + parmsOut);
			out.println("#NV=" + nv);
			for (int i = 0; i < nv; i++)
				out.println(  nf.format(pa[i].getx()) + ", "
								+ nf.format(pa[i].gety()) );
		}
	}

	public int sizeDecomp()
	{
		return 0;
	}

	public void drawDecomp(Graphics g, RPanel rp)
	{
	}

	/**
	 * Generate points uniformly in the unit-square.
	 */
	private void uniform_point_generation(ptInfo pti, Rand mr)
	{
		if (pti.debug >= 3)
			System.out.println("DB3: unif_p_gen: n= " + pti.num_items);

		for (int i = 0; i < pa.length; i++)
			pa[i] = new Pointd(mr.uniform(), mr.uniform());

		nv = pa.length;
	}

	public Pointd pointInD(Rand mr, Pointd[] v)
	{
		int n = v.length;
		double[] ran = new double[n-1];
		double[] space = new double[n];
		double x = 0.0;
		double y = 0.0;

		for (int i = 0; i < n-1; i++)
			ran[i] = mr.uniform();
		Arrays.sort(ran);

		space[0] = ran[0];
		for (int i = 0; i < n-2; i++)
			space[i+1] = ran[i+1] - ran[i];
		space[n-1] = 1.0 - ran[n-2];

		for (int i = 0; i < n; i++)
		{
			x += space[i]*v[i].getx();
			y += space[i]*v[i].gety();
		}
		return new Pointd(x,y);
	}

	/**
	 * Use a bivariate normal distribution with independent 
	 * variables to generate clustered points in the unit-square.
	 */
	private void bivariate_point_generation (ptInfo ri, Rand mr)
	{
		double x, y;
		int which_cluster;
		
		if (ri.debug >= 3)
			System.out.println("D3: biv_point: n= " + ri.num_items
									+ ", clus= " + ri.num_clusters
									+ ", std= " + ri.std_dev);
		
		Pointd[] ca = new Pointd[ri.num_clusters];

		/* Pick random points as seeds for the clusters */
		for (int i = 0; i < ca.length; i++)
			ca[i] = new Pointd(mr.uniform(), mr.uniform());

		/* Next, generate the points */
		for (int i = 0; i < ri.num_items; i++)
		{
			/* For each point, choose a cluster first */
			which_cluster = mr.uniform (0, ri.num_clusters-1);

			/* Now generate a point, making sure it lies in unit-square */
			do {
				x = mr.Normal(ca[which_cluster].getx(), ri.std_dev);
			} while ( (x < 0) || (x > 1) );

			do {
				y = mr.Normal(ca[which_cluster].gety(), ri.std_dev);
			} while ( (y < 0) || (y > 1) );

			pa[i] = new Pointd(x, y);
  		}
  		nv = pa.length;
	}

	/**
	 * Use MMPP to generate clustered points in the unit-square.
	 */
	 private void nonstationary_poisson (ptInfo ri, Rand mr)
	 {
		int num_items = ri.num_items;
		int i, j;
		
		if (ri.debug >= 3)
	  		System.out.println("D3: NonStat: n= " + ri.num_items
					+ ", clus= " + ri.num_clusters);

		double density = 2.0;
		double temp;
		double [] X = new double [num_items + 1];
		double [] Y = new double [num_items + 1];

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
		/* */
		double upper_bound = 2 * Math.PI;

		/* Generate X and Y arrays independently */
 			X = mr.mmpp(num_items, xdivisions, alpha_A, alpha_B, lambda_A, lambda_B, 1.0);
 	 		Y = mr.mmpp(num_items, ydivisions, alpha_A, alpha_B, lambda_A, lambda_B, 1.0);


 		/* Randomly permute the X values */
			for (i=1; i<num_items; i++)
			{
				/* Pick randomly from i,...,num_items */
				j = mr.uniform (i, num_items);
				/* Swap i-th and j-th values */
				temp = X[i];
				X[i] = X[j];
				X[j] = temp;
 			}

 		/* Randomly permute the Y values */
 			for (i=1; i<num_items; i++)
			{
 				/* Pick randomly from i,...,num_items */
 				j = mr.uniform (i, num_items);
 				/* Swap i-th and j-th values */
 				temp = Y[i];
 				Y[i] = Y[j];
 				Y[j] = temp;
 			}

 		for (i = 0; i < num_items; i++) {
			pa[i] = new Pointd(X[i], Y[i]);
//	 		System.out.println("pa[i]: " + pa[i].toString());
//			pa[i] = new Pointd(mr.uniform(), Y[i+1]);
// 			pa[i] = new Pointd(X[i+1], Y[i+1]);
		}

	  	nv = num_items;
	}


//	/*  Generate samples from a bivariate joint density */
//	/*  Ripley, Applied Statistics 28, 1979 */
//
//	public RPoint uniform_point_generation(ptInfo ri)
//	{
//		/* Set parameters for testing */
//		double c = 0.4;
//		double r = 0.08;
//
//		int rn = ri.num_items;		/* rn is the number of points */
//		int mm = 4 * rn;			/* mm is the number of steps */
//
//		int i, j, id;
//
//		double[] x = new double[rn];
//		double[] y = new double[rn];
//		double f;
//
//	/* Generate initial coordinates */
//
//		for (i = 0; i < rn; i++)
//		{
//			x[i] = mr.Normal(.5, .2);
//			y[i] = mr2.Normal(.1, .1);
//		}
//
//	/* Generate samples from bivariate density */
//
//		for (i = 0; i < mm; i++)
//		{
//			id = mr.uniform(1, rn-1);
//			x[id] = x[0];
//			y[id] = y[0];
//
//			do
//			{
//				x[0] = mr.uniform();
//				y[0] = mr2.uniform();
//				f = den(x, y, rn, c, r);
//			}
//			while (f <= mr.uniform());
//		}
//
//		RPoint pa = new RPoint(rn, ri.num_clusters);
//
//		for (i = 0; i < rn; i++)
//			pa.add(new Pointd(x[i], y[i]));
//
//		return pa;
//	}
//
//	private double den(double[] x, double[] y, int n, double c, double r)
//
//	Algorithm AS 137.1 Appl. Statist. (1979) Vol. 28, No. 1, Ripley
//
//	Calculates Strauss conditional density. Den is proportional
//	to c ** (number of neighbors of the first point).
//
//	{
//		double den = 1.0;
//		double rr = r * r;
//		double x0, y0;
//
//		for (int i = 1; i < n; i++)
//		{
//			x0 = fa(x[i] - x[0]);
//			y0 = fa(y[i] - y[0]);
//			if ((x0 * x0 + y0 * y0) < rr) den = c * den;
//		}
//
//		/* Normalize by the max of the density */
//
//		if (c > 1.0)
//			den = den / Math.pow(c, (n-1));
//		return den;
//	}
//
//	private double fa(double a)
//	{
//		return a;
//
//		/* For Torus, use fa(a) = amin1(abs(a), 1.0 - abs(a)) */
//
//	}
//
//	/* Test Poisson generation */
//  /* Byron J. T. Morgan, Elements of Simulation, p. 84, and */
//	/* Stoyan & Stoyan, Fractals, Random Shapes and Point Fields, p. 218 */
//
//	public RPoint bivariate_point_generation(ptInfo ri)
//	{
//		/* Set parameters for testing */
//
//		double L = 1.0;				/* Lambda */
//		int N = ri.num_items;		/* n is the number of points */
//		double T, Z, max;
//		int i, j;
//		double[] x = new double[N];
//		double[] y = new double[N];
//
//	/* Generate Poisson coordinates */
//		max = 1.0;
//		T = Math.exp(-L);
//		for (i = 0; i < N; i++)
//		{
//			j = 0;
//			Z = mr.uniform();
//			System.out.println(i + " " + j + " " + Z + " " + T);
//			while ( Z >= T )
//			{
//				j++;
//				Z = Z * mr.uniform();
//				System.out.println(i + " " + j + " " + Z + " " + T);
//			}
//			x[i] = j;
//			if (max < j)
//				max = j;
//		}
//
//	/* Generate y coordinates as uniform */
//		for (i = 0; i < N; i++)
//		{
//			x[i] = x[i]/max;
//			y[i] = mr.uniform();
//			System.out.println(i + " " + x[i] + " " + y[i]);
//		}
//
//		RPoint RP = new RPoint(N, ri.num_clusters);
//
//		for (i = 0; i < N; i++)
//			RP.add(new Pointd(x[i], y[i]));
//
//		return RP;
//	}

	public double getArea()
	{
		if (area == 0.0)
			area = computeArea();
		return area;
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

	public double getChi2()
	{
		return chi2;
	}

	/* Compute area */
	public double computeArea()
	{
		return 1.0;
	}

	/* Compute perimeter */
	public double computePerimeter()
	{
		return 4.0;
	}

	/* Compute compacity */
	public double computeCompacity()
	{
		if (area == 0.0)
			computeArea();
		if (perimeter == 0.0)
			computePerimeter();
		compacity = (4*Math.PI*area)/(perimeter*perimeter);
		return compacity;
	}

	public void computeMetrics()
	{

		int i, j, k, l;
		int mres = DEFAULTMRES;
		int[][] cell_points = new int[mres][mres];

		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(0);
		nf.setMinimumIntegerDigits(4);

		if (h == null)
			findHull();
		area = computeArea();
		hullArea = h.computeArea();
		areaRatio = area/hullArea;
		perimeter = computePerimeter();
		hullPerimeter = h.computePerimeter();
		perRatio = perimeter/hullPerimeter;
		compacity = computeCompacity();

//		Find cells for vertices
		double size = 1.0/mres;
		for (i = 0; i < nv; i++)
		{
			Pointd pt = pa[i];
			k = (int)Math.floor(pt.getx()/size);
			l = (int)Math.floor(pt.gety()/size);
//			System.out.println("Cell counts: " + j
//					+ " (" + k + ", " + l + ")"
//					+ " (" + pt.getx() + ", " + pt.gety() + ")");
			if (k >= mres)
				k = mres -1;
			if (l >= mres)
				l = mres - 1;
			cell_points[k][l]++;
		}

//		Compute chi2 for point distribution
		chi2 = 0.0;
		double evert = nv/mres/mres;
		double diff=0.0;
		for (i = 0; i < mres; i++)
			for (j = 0; j < mres;)
			{
//				System.out.println(
//					"cell_points[" + i + "][" + j + "] = " +
//					nf.format(cell_points[i][j]) +
//					"\t cell_points[" + i + "][" + (j+1) + "] = " +
//					nf.format(cell_points[i][j+1]) );

				diff = cell_points[i][j] - evert;
				chi2 += diff*diff/evert;
				diff = cell_points[i][j+1] - evert;
				chi2 += diff*diff/evert;
				j +=2;
			}
//		System.out.println("Chi2 = " + chi2 + "\n");
	}
	public void scaleToUnitSquare(){}
	public void drawSpecial(Graphics g, RPanel rp){}
	
	public ptInfo getRInfo() {
		return ri;
	}
}
