package org.edisonwj.rapport;

/**
 * RSegmentSet class specifies a set of randomly generated line segments
 */

import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;

public class RSegmentSet extends RObject implements RapportDefaults
{
	private ArrayList ss;
	private Hull h;
	protected lnInfo ri;				/* Link to RInfo */
	
	public RSegmentSet()
	{
		type = TYPE_SEGMENT_SET;
		nv = 0;
		ss = new ArrayList();
	}

	public RSegmentSet(lnInfo ri, Rand mr)
	{
		this();
		this.ri = ri;
		
		if (ri.uniform)
			uniform_line_generation(ri, mr);
		else if (ri.normal)
			bivariate_line_generation(ri, mr);
		else if (ri.poisson)
			nonstationary_poisson_line_generation(ri, mr);
		else
			System.out.println("RSegmentSet:constructor - parameter error");
	}

	public RSegmentSet(RSegment s)
	{
		this();
		ss.add(s);
		nv = ss.size();
	}

	public RSegmentSet(RSegment[] sa)
	{
		this();
		ss.addAll(Arrays.asList(sa));
		nv = ss.size();
	}

	public RSegmentSet(RLineField lf)
	{
		this();

		RPointp p;
		for (int i = 0; i  < lf.size(); i++)
		{
			p = lf.get(i);
			ss.add(p.toSegment());
		}
		nv = ss.size();
	}

	public void writeData(boolean draw3d, PrintWriter out) throws IOException
	{
//		System.out.println("RSegmentSet writeData type: " + type);
		
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
						", Number-of-lines = " + ri.Snum_items +
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

			String fmt = DRAW3D_NUMBER_FORMAT;
			for (int i = 0; i < ss.size(); i++)
			{
				RSegment s = (RSegment)ss.get(i);
				lineOut  = "Line: ";
				
				lineOut += 	String.format(fmt,s.getp1().getx()) + ", " +
							String.format(fmt,s.getp1().gety()) + ", " +
							String.format(fmt,0.0) + ", " +
							String.format(fmt,s.getp2().getx()) + ", " +
							String.format(fmt,s.getp2().gety()) + ", " +
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
			out.println("#NV=" + ss.size());
			for (int i = 0; i < ss.size(); i++)
			{
				RSegment s = (RSegment)ss.get(i);
				out.println(	nf.format(s.getp1().getx()) + ", " +
								nf.format(s.getp1().gety()) + ", " +
								nf.format(s.getp2().getx()) + ", " +
								nf.format(s.getp2().gety()) );
			}
		}
	}

	public void readData(BufferedReader in) throws IOException
	{
		String s;
		for (int i=0; i < nv; i++)
		{
			s = in.readLine();
			ss.add(new RSegment(s));
		}
	}
	
	public RSegmentSet(BufferedReader in) throws IOException
	{
		this();
//		System.out.println("RSegmentSet(Buffered Reader) this: " + this.hashCode());
		String s;
		String[] sttp;
		String stitle;
		sttp = readTypeTitleParms(in);
		type = Integer.parseInt(sttp[0]);
		stitle = sttp[1];
		nv = readNV(in);
		if (nv >= 0)
		{
			for (int i = 0; i < nv; i++) {
				s = in.readLine();
				ss.add(new RSegment(s));
			}
			ri = new lnInfo(ss.size());
			ri.title = stitle;
//			System.out.println("RSegmentSet this: " + this.hashCode());
//			System.out.println("RSegmentSet ri: " + ri.hashCode() + ", ri.title: " + ri.title);
		}
		
		else {
			System.out.println("RSegmentSet: Error constructing segment set from file input");
			ss = null;
			nv = 0;
		}
	}
	
	public boolean isGend()
	{
		return (ss != null && nv == ss.size());
	}

	public void add(RSegment is)
	{
		ss.add(is);
		nv = ss.size();
	}

	public void addAll(ArrayList al)
	{
		if (al != null)
		{
			ss.addAll(al);
			nv = ss.size();
		}
	}

	public void addAll(RSegmentSet iss)
	{
		this.addAll(iss.getAll());
	}

	public RSegment get(int i)
	{
		return (RSegment)ss.get(i);
	}

	public ArrayList getAll()
	{
		return ss;
	}

	public int size()
	{
		return ss.size();
	}

	public String toString()
	{
		return ss.toString();
	}

	public void draw(Graphics g, RPanel rp, Color c)
	{
		if ( nv > 0 )
		{
			Color saveC = g.getColor();
			if (c != null)
				g.setColor(c);

			for (int i = 0; i < nv; i++)
				((RSegment)ss.get(i)).draw(g, rp);

			g.setColor(saveC);
		}
	}

	public void draw(Graphics g, RPanel rp)
	{
		this.draw(g, rp, null);
	}

	public void findHull()
	{
		RSegment rs;
		int nl = this.size();
		Pointd[] pa = new Pointd[2*nl];

		for (int i = 0; i < nl; i++)
		{
			rs = (RSegment)ss.get(i);
			pa[2*i] = rs.getp1();
			pa[2*i+1] = rs.getp2();
		}
		h = new Hull(pa);
	}

	public Hull getHull()
	{
		if (h == null)
			this.findHull();
		return h;
	}
	
	
	/* Get convex hull size */
	public int sizeHull()
	{
		if (h == null)
			return 0;
		else
			return h.size();
	}

	/**
	 * Generate line segments uniformly in the unit-square.
	 */
	private void uniform_line_generation(lnInfo ri, Rand mr)
	{
		if (ri.debug >= 3)
			System.out.println("D3.RSegmentSet: unif_ln_gen: n= " + ri.num_items);

	  	for (int i = 0; i < ri.num_items; i++)
			ss.add(new RSegment(new Pointd(mr.uniform(), mr.uniform()),
							new Pointd(mr.uniform(), mr.uniform())));
		nv = ss.size();

	}

	/**
	 * Use a bivariate normal distribution with independent variables 
	 * to generate clustered line segments in the unit-square.
	 */
	private void bivariate_line_generation(lnInfo ri, Rand mr)
	{
		double [] x = new double [2];
		double [] y = new double [2];
		int which_cluster;
		
		if (ri.debug >= 3)
			System.out.println("D3.RSegmentSet: biv_point: n= " + ri.num_items
						+ ", clus= " + ri.num_clusters
						+ ", std= " + ri.std_dev);

		Pointd[] ca = new Pointd[ri.num_clusters];

		/* Pick random points as seeds of clusters */
		for (int i = 0; i < ri.num_clusters; i++)
			ca[i] = new Pointd(mr.uniform(), mr.uniform());

		/* Next, generate the lines */
		for (int i = 0; i < ri.num_items; i++)
		{
			/* For each line, choose a cluster first */
			which_cluster = mr.uniform (0, ri.num_clusters-1);

			/* Now generate a line, making sure it lies in unit-square */
			for (int j = 0; j < 2; j++)
			{
				do {
					x[j] = mr.Normal(ca[which_cluster].getx(), ri.std_dev);
				} while ( (x[j] < 0) || (x[j] > 1) );


				do {
					y[j] = mr.Normal (ca[which_cluster].gety(), ri.std_dev);
				} while ( (y[j] < 0) || (y[j] > 1) );

			}
			this.add(new RSegment(new Pointd(x[0], y[0]),
								  new Pointd(x[1], y[1])));
  		}
  		nv = ss.size();
	}
	
	/**
	 * Use MMPP to generate clustered line segments in the unit-square.
	 */
	 private void nonstationary_poisson_line_generation(lnInfo ri, Rand mr)
	 {
		int num_items = ri.num_items;
		int i, j;
		
		if (ri.debug >= 3)
	  		System.out.println("DB3: NonStat: n= " + ri.num_items);

		double density = 2.0;
		double temp;
		double [] X = new double [2*num_items + 1];
		double [] Y = new double [2*num_items + 1];

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
		X = mr.mmpp(2*num_items, xdivisions, alpha_A, alpha_B, lambda_A, lambda_B, 1.0);
 		Y = mr.mmpp(2*num_items, ydivisions, alpha_A, alpha_B, lambda_A, lambda_B, 1.0);

 		/* Randomly permute the X values */
		for (i=1; i<2*num_items; i++)
		{
			/* Pick randomly from i,...,2*num_items */
			j = mr.uniform (i, 2*num_items);
			/* Swap i-th and j-th values */
			temp = X[i];
			X[i] = X[j];
			X[j] = temp;
		}

 		/* Randomly permute the Y values */
		for (i=1; i<2*num_items; i++)
		{
			/* Pick randomly from i,...,2*num_items */
			j = mr.uniform (i, 2*num_items);
			/* Swap i-th and j-th values */
			temp = Y[i];
			Y[i] = Y[j];
			Y[j] = temp;
		}
 		
		RSegment rs;
 		for (i = 0; i < 2*num_items; i += 2) {
 			rs = new RSegment(new Pointd(X[i], Y[i]),
					  		  new Pointd(X[i+1], Y[i+1]));
 			this.add(rs);
//			System.out.println("RSegment i: " + i/2 + " " + rs.toString());
 		}
  		nv = ss.size();
	}
	 
	public void reSize() {};
	public void reSize(int n) {};
	public void add(Pointd p) {};
	public void drawDecomp(Graphics g, RPanel rp) {};
	public Pointd getVertex(int n) { return null;}
	public void setVertex(Pointd p, int n) {};
	public int sizeDecomp() {return 0;};
	public void scaleToUnitSquare(){}
	public void drawSpecial(Graphics g, RPanel rp){}
	
	public lnInfo getRInfo() {
		return ri;
	}
}
