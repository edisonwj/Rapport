package org.edisonwj.rapport;

/**
 * RInfo defines the abstract class covering the various lower level
 * class specifying the parameters associated with the objects 
 * Rapport creates.
 */

import java.io.*;

public abstract class RInfo implements Cloneable, RapportDefaults
{
	public String title = "RObject Info";

	public int nv;
	public String Snv;

	public int numb_objects = 1;
	public int max_attempts = MAX_ATTEMPTS;
	public int debug = DEBUG;
	public long seed = Math.abs(System.currentTimeMillis());
	public long seed2 = Math.abs(System.currentTimeMillis()/777777);

	public boolean keepseeds = false;
	public boolean color = false;
	public boolean display = true;
	public boolean axes = true;
	public boolean unitsquare = true;
	public int step = 1;
	public int pause = 100;
	public int pixelDim = 800;
	public double logicalDim = 1.1D;
	public double specialDim = 2.2D;
	public boolean center = false;
	public boolean lowleft = true;

	public boolean file = false;
	public String fname = "";
	public File outputFile;

	public String Snumb_objects = String.valueOf(numb_objects);
	public String Smax_attempts = String.valueOf(max_attempts);
	public String Sseed = String.valueOf(seed);
	public String Sseed2 = String.valueOf(seed2);
	public String Sdebug = String.valueOf(debug);
	public String Sstep = String.valueOf(step);
	public String Spause = String.valueOf(pause);
	public String SpixelDim = String.valueOf(pixelDim);
	public String SlogicalDim = String.valueOf(logicalDim);
	public String SspecialDim = String.valueOf(specialDim);
	public String Sfname = "";

	public String SdispInfo = "";

	public Object clone()
	{
		try
		{
			RInfo ri = (RInfo)super.clone();

			if (ri.logicalDim < 1.0)
			{
				ri.logicalDim = 1.0;
				ri.SlogicalDim = String.valueOf(ri.logicalDim);
			}
			return ri;

		}
		catch (CloneNotSupportedException e)
		{
			return null;
		}
	}
}

class fiInfo extends RInfo					/* Files */
{
//	public int nv;
//	public String Snv;

	public fiInfo(int n)
	{
		nv = n;
		Snv = String.valueOf(nv);
		SdispInfo = "Num. items: " + Snv;
	}
}

class ptInfo extends RInfo					/* Points */
{
	public boolean uniform = false;
	public boolean normal = true;
	public boolean poisson = false;

	public int num_items;
	public int num_clusters;
	public double std_dev;
	public double density;

	public String Snum_items;
	public String Snum_clusters;
	public String Sstd_dev;
	public String Sdensity;

	ptInfo()
	{
		num_items = 1000;
		num_clusters = NUM_CLUSTERS;
		std_dev = LOWDEV;
		density = DENSITY;
		logicalDim = 1.1D;
		center = false;
		lowleft = true;

		Snum_items = String.valueOf(num_items);
		Snum_clusters = String.valueOf(num_clusters);
		SlogicalDim = String.valueOf(logicalDim);
		Sstd_dev = String.valueOf(std_dev);
		Sdensity = String.valueOf(density);
		SdispInfo = "Num. points: " + Snum_items;
	}

	ptInfo(int n)
	{
		num_items = n;
		num_clusters = 0;
		std_dev = LOWDEV;
		density = DENSITY;
		logicalDim = 1.1D;
		center = false;
		lowleft = true;

		Snum_items = String.valueOf(num_items);
		Snum_clusters = String.valueOf(num_clusters);
		SlogicalDim = String.valueOf(logicalDim);
		Sstd_dev = String.valueOf(std_dev);
		Sdensity = String.valueOf(density);
		SdispInfo = "Num. points: " + Snum_items;
	}
}

class reInfo extends RInfo					/* Edges */
{
	public int num_items;

	public String Snum_items;

	reInfo()
	{
		num_items = 1;
		logicalDim = 1.1D;
		center = false;
		lowleft = true;

		Snum_items = String.valueOf(num_items);
		SlogicalDim = String.valueOf(logicalDim);
		SdispInfo = "Num. points: " + Snum_items;
	}

	reInfo(int n)
	{
		num_items = n;
		logicalDim = 1.1D;
		center = false;
		lowleft = true;

		Snum_items = String.valueOf(num_items);
		SlogicalDim = String.valueOf(logicalDim);
		SdispInfo = "Num. points: " + Snum_items;
	}
}

class lnInfo extends RInfo					/* Lines */
{
	public boolean uniform = false;
	public boolean normal = true;
	public boolean poisson = false;

	public int num_items;
	public int num_clusters;
	public double std_dev;
	public double density;

	public String Snum_items;
	public String Snum_clusters;
	public String Sstd_dev;
	public String Sdensity;

	lnInfo()
	{
		num_items = 100;
		num_clusters = NUM_CLUSTERS;
		std_dev = LOWDEV;
		density = DENSITY;
		logicalDim = 1.1D;
		center = false;
		lowleft = true;

		Snum_items = String.valueOf(num_items);
		Snum_clusters = String.valueOf(num_clusters);
		SlogicalDim = String.valueOf(logicalDim);
		Sstd_dev = String.valueOf(std_dev);
		Sdensity = String.valueOf(density);
		SdispInfo = "Num. lines: " + Snum_items;
	}

	lnInfo(int n)
	{
		num_items = n;
		num_clusters = 0;
		std_dev = LOWDEV;
		density = DENSITY;
		logicalDim = 1.1D;
		center = false;
		lowleft = true;

		Snum_items = String.valueOf(num_items);
		Snum_clusters = String.valueOf(num_clusters);
		SlogicalDim = String.valueOf(logicalDim);
		Sstd_dev = String.valueOf(std_dev);
		Sdensity = String.valueOf(density);
		SdispInfo = "Num. lines: " + Snum_items;
	}
}

class lpInfo extends RInfo					/* PolyLine*/
{
	public boolean rotate  = true;
	public boolean smooth1 = false;
	public boolean smooth2 = true;
	public boolean spare  = false;

	public boolean xuniform = true;
	public boolean xnormal = false;
	public boolean xexponential = false;
	public boolean xmmpp = false;

	public boolean yuniform = true;
	public boolean ynormal = false;
	public boolean yexponential = false;
	public boolean ymmpp = false;

	public int num_items;
	public double smoothparm;
	public double std_dev;
	public double density;

	public String Snum_items;
	public String Ssmoothparm;
	public String Sstd_dev;
	public String Sdensity;

	lpInfo()
	{
		num_items = 5;
		smoothparm = 0.3;
		std_dev = LOWDEV;
		density = DENSITY;
		logicalDim = 3.4D;
		center = true;
		lowleft = false;

		Snum_items = String.valueOf(num_items);
		Ssmoothparm = String.valueOf(smoothparm);
		Sstd_dev = String.valueOf(std_dev);
		Sdensity = String.valueOf(density);
		SlogicalDim = String.valueOf(logicalDim);
		SdispInfo = "Num. vertices: " + Snum_items;
	}

	lpInfo(int n)
	{
		num_items = 64;
		smoothparm = 1.0;
		std_dev = LOWDEV;
		density = DENSITY;
		logicalDim = 3.4D;
		center = true;
		lowleft = false;

		Snum_items = String.valueOf(num_items);
		Ssmoothparm = String.valueOf(smoothparm);
		Sstd_dev = String.valueOf(std_dev);
		Sdensity = String.valueOf(density);
		SlogicalDim = String.valueOf(logicalDim);
		SdispInfo = "Num. vertices: " + Snum_items;
	}

}

class lfInfo extends RInfo				/* Poisson Line Field */
{
	public int num_items;

	public String Snum_items;

	lfInfo()
	{
		num_items = 5;
		logicalDim = 1.1D;
		center = true;
		lowleft = false;

		Snum_items = String.valueOf(num_items);
		SlogicalDim = String.valueOf(logicalDim);
		SdispInfo = "Num. lines: " + Snum_items;
	}

	lfInfo(int n)
	{
		num_items = n;
		logicalDim = 1.1D;
		center = true;
		lowleft = false;

		Snum_items = String.valueOf(num_items);
		SlogicalDim = String.valueOf(logicalDim);
		SdispInfo = "Num. lines: " + Snum_items;
	}
}

class rwInfo extends RInfo				/* Random walk */
{
	public boolean A = true;
	public boolean B = false;
	public boolean C = false;

	public boolean xuniform = true;
	public boolean xnormal = false;
	public boolean xexponential = false;

	public boolean yuniform = true;
	public boolean ynormal = false;
	public boolean yexponential = false;

	public int num_items;
	public double xmin;
	public double ymin;
	public double xmax;
	public double ymax;
	public double std_dev;

	public String Snum_items;
	public String Sxmin;
	public String Symin;
	public String Sxmax;
	public String Symax;
	public String Sstd_dev;

	rwInfo()
	{
		num_items = 64;
		std_dev = LOWDEV;
		xmin = -1.0;
		ymin = -1.0;
		xmax = 1.0;
		ymax = 1.0;
		logicalDim = 3.4D;
		unitsquare = true;
		center = true;
		lowleft = false;

		Snum_items = String.valueOf(num_items);
		Sxmin = String.valueOf(xmin);
		Symin = String.valueOf(ymin);
		Sxmax = String.valueOf(xmax);
		Symax = String.valueOf(ymax);
		Sstd_dev = String.valueOf(std_dev);
		SlogicalDim = String.valueOf(logicalDim);
		SdispInfo = "Num. vertices: " + Snum_items;
	}

	rwInfo(int n)
	{
		num_items = 64;
		xmin = -1.0;
		ymin = -1.0;
		xmax = 1.0;
		ymax = 1.0;
		std_dev = LOWDEV;
		logicalDim = 3.4D;
		unitsquare = true;
		center = true;
		lowleft = false;

		Snum_items = String.valueOf(num_items);
		Sxmin = String.valueOf(xmin);
		Symin = String.valueOf(ymin);
		Sxmax = String.valueOf(xmax);
		Symax = String.valueOf(ymax);
		Sstd_dev = String.valueOf(std_dev);
		SlogicalDim = String.valueOf(logicalDim);
		SdispInfo = "Num. vertices: " + Snum_items;
	}
}

class phInfo extends RInfo				/* Hull Polygon */
{
	public boolean a = true;
	public boolean b = false;
	public boolean c = false;
	public boolean d = false;
	public boolean e = false;
	public boolean f = false;

//	public int nv;
	public double alpha;
//	public String Snv;
	public String Salpha;

	public phInfo()
	{
		nv = 12;
		Snv = String.valueOf(nv);
		alpha = 3.0;
		Salpha = String.valueOf(alpha);
	}
}

class pfInfo extends RInfo				/* Random Point Polygon */
{
	public boolean a = true;
	public boolean b = false;
	public boolean c = false;
	public boolean d = false;
	public boolean e = false;
	public boolean f = false;

//	public int nv;
	public double alpha;
//	public String Snv;
	public String Salpha;

	public pfInfo()
	{
//		System.out.println("RInfo pfInfo constructor: " + this.hashCode());
		nv = 12;
		Snv = String.valueOf(nv);
		alpha = 3.0;
		Salpha = String.valueOf(alpha);
	}
}

class ppInfo extends RInfo				/* Poisson Polygon */
{
//	public int nv;
//	public String Snv;

	public ppInfo()
	{
		nv = NUM_ITEMS;
		Snv = String.valueOf(nv);
	}
}

class elInfo extends RInfo				/* Ellipse/Circle */
{
	public boolean circle = true;
	public boolean ellipse = false;
	public boolean offset = false;
	public boolean trim = true;

	public double minlen;
	public String Sminlen;
	public double maxlen;
	public String Smaxlen;

//	public int nv;

	public elInfo()
	{
		numb_objects = 10;
		minlen = .1;
		Sminlen = String.valueOf(minlen);
		maxlen = .5;
		Smaxlen = String.valueOf(maxlen);
	}
}

class rcInfo extends RInfo				/* Rectangle */
{
	public boolean corners = true;
	public boolean centerext = true;
	public boolean cornerext = false;
	public boolean force = false;
	public boolean wrap = false;

	public double minlen;
	public String Sminlen;
	public double maxlen;
	public String Smaxlen;

//	public int nv;

   public rcInfo()
   {
		nv = 4;
		Snv = String.valueOf(nv);
		minlen = .01;
		Sminlen = String.valueOf(minlen);
		maxlen = .5;
		Smaxlen = String.valueOf(maxlen);
		center = false;
		lowleft = true;
	}
}

class pcInfo extends RInfo				/* Cell Polygon */
{
	public boolean uniform = true;
	public boolean exponential  = false;
	public boolean mixed  = false;

	public int num_poly;
	public int num_cells;

	public String Snum_cells;

	pcInfo()
	{
		num_poly = 1;
		num_cells = 12;
		logicalDim = 1.1D;
		center = false;
		lowleft = true;

		Snum_cells = String.valueOf(num_cells);
		SlogicalDim = String.valueOf(logicalDim);
		SdispInfo = "Num. cells: " + Snum_cells;
	}

}

class psInfo extends RInfo				/* Star Polygon */
{
	public boolean polar = false;
	public boolean points = true;

	public boolean angle_unif = true;
	public boolean angle_exp = false;
	public boolean angle_mmpp = false;

	public boolean radial_unif = true;
	public boolean radial_exp = false;

	public boolean angsum = true;
	public boolean bounded = false;
	public boolean random = false;

	public boolean smooth1 = true;
	public boolean smooth2 = false;
	public boolean nosmooth = false;

//	public int nv;
//	public String Snv;
	public double density;
	public String Sdensity;
	public double smooth_factor;
	public String Ssmooth_factor;
	public double radial_exp_rate;
	public String Sradial_exp_rate;
	public double markov_param;
	public String Smarkov_param;

   public psInfo()
   {
		nv = NUM_ITEMS;
		Snv = String.valueOf(nv);
		density = DENSITY;
		Sdensity = String.valueOf(density);
		smooth_factor = SMOOTH_FACTOR;
		Ssmooth_factor = String.valueOf(smooth_factor);
		radial_exp_rate = RADIAL_EXP_RATE;
		Sradial_exp_rate = String.valueOf(radial_exp_rate);
		markov_param = MARKOV_PARAM;
		Smarkov_param = String.valueOf(markov_param);
		center = false;
		lowleft = true;
	}
}

class poInfo extends RInfo				/* Monotone Polygon */
{
	public boolean random = false;

	public poInfo()
	{
		nv = 50;
		Snv = String.valueOf(nv);
		center = false;
		lowleft = true;
	}
}

class cuInfo extends RInfo				/* Circle Uniform Polygon */
{
	public boolean angsum = true;
	public boolean smooth = true;

	public double smooth_factor;
	public String Ssmooth_factor;

   public cuInfo()
   {
		nv = NUM_ITEMS;
		Snv = String.valueOf(nv);
		smooth_factor = SMOOTH_FACTOR;
		Ssmooth_factor = String.valueOf(smooth_factor);
	}
}

class trInfo extends RInfo				/* Spiral Polygon */
{
	public boolean bounded = true;
	public boolean hash = true;
	public boolean random = false;

	public double radial_exp_rate;
	public String Sradial_exp_rate;

	public trInfo()
	{
		nv = 50;
		radial_exp_rate = 5.0;

		Snv = String.valueOf(nv);
		Sradial_exp_rate = String.valueOf(radial_exp_rate);
		center = true;
		lowleft = false;
	}
}

class cmInfo extends RInfo			/*  Cell PolyMap */
{                                                                    	
	public boolean uniform = true;
	public boolean exponential  = false;
	public boolean mixed  = false;

	public int num_poly;
	public int num_cells;

	public String Snum_poly;
	public String Snum_cells;

	cmInfo()
	{
		num_poly = 50;
		num_cells = 100;
		logicalDim = 1.1D;
		center = false;
		lowleft = true;
		color = true;

		Snum_poly = String.valueOf(num_poly);
		Snum_cells = String.valueOf(num_cells);
		SlogicalDim = String.valueOf(logicalDim);
		SdispInfo = "Num. poly: " + Snum_poly + " Num. cells: " + Snum_cells;
	}

	cmInfo(int n)
	{
		num_poly = n;
		num_cells = 100;
		logicalDim = 1.1D;
		center = false;
		lowleft = true;
		color = true;

		Snum_poly = String.valueOf(num_poly);
		Snum_cells = String.valueOf(num_cells);
		SlogicalDim = String.valueOf(logicalDim);
		SdispInfo = "Num. poly: " + Snum_poly + " Num. cells: " + Snum_cells;
	}
}

class pmInfo extends RInfo				/* Poisson Polymap */
{
	public double alpha;
	public int num_items;

	public String Salpha;
	public String Snum_items;

	pmInfo()
	{
		alpha = 2.0;
		num_items = 20;
		logicalDim = 1.1D;
		center = false;
		lowleft = true;
		color = true;

		Salpha = String.valueOf(alpha);
		Snum_items = String.valueOf(num_items);
		SlogicalDim = String.valueOf(logicalDim);
		SdispInfo = "Alpha: " + Salpha
					+ ", Num. lines: " + Snum_items;
	}

	pmInfo(double a)
	{
		alpha = a;
		num_items = 0;
		logicalDim = 1.1D;
		center = false;
		lowleft = true;
		color = true;

		Salpha = String.valueOf(alpha);
		Snum_items = String.valueOf(num_items);
		SlogicalDim = String.valueOf(logicalDim);
		SdispInfo = "Alpha: " + Salpha
					+ ", Num. lines: " + Snum_items;
	}

	pmInfo(int n)
	{
		alpha = 2.0;
		num_items = n;
		logicalDim = 1.1D;
		center = false;
		lowleft = true;
		color = true;

		Salpha = String.valueOf(alpha);
		Snum_items = String.valueOf(num_items);
		SlogicalDim = String.valueOf(logicalDim);
		SdispInfo = "Alpha: " + Salpha
				+ ", Num. lines: " + Snum_items;
	}
}

class vmInfo extends RInfo			/* Voronoi Polymap */
{
	public boolean uniform = false;
	public boolean normal = true;
	public boolean poisson = false;

	public int num_items;
	public int num_clusters;
	public double std_dev;
	public double density;

	public String Snum_items;
	public String Snum_clusters;
	public String Sstd_dev;
	public String Sdensity;

	vmInfo()
	{
		num_items = 20;
		num_clusters = 1;
		std_dev = .5;
		density = DENSITY;
		logicalDim = 1.1D;
		center = false;
		lowleft = true;
		color = true;

		Snum_items = String.valueOf(num_items);
		Snum_clusters = String.valueOf(num_clusters);
		Sstd_dev = String.valueOf(std_dev);
		Sdensity = String.valueOf(density);
		SlogicalDim = String.valueOf(logicalDim);
		SdispInfo = "Num. points: " + Snum_items;		
	}

	vmInfo(int n)
	{
		num_items = n;
		num_clusters = 1;
		std_dev = .5;
		density = DENSITY;
		logicalDim = 1.1D;
		center = false;
		lowleft = true;
		color = true;

		Snum_items = String.valueOf(num_items);
		Snum_clusters = String.valueOf(num_clusters);
		Sstd_dev = String.valueOf(std_dev);
		Sdensity = String.valueOf(density);
		SlogicalDim = String.valueOf(logicalDim);
		SdispInfo = "Num. points: " + Snum_items;	
	}
}

class lmInfo extends RInfo			/* PolyLine Polymap */
{
	public boolean A = false;
	public boolean B = true;
	public boolean random = false;

	public int num_items;

	public String Snum_items;

	lmInfo()
	{
		num_items = 10;
		logicalDim = 1.1D;
		center = false;
		lowleft = true;
		color = true;

		Snum_items = String.valueOf(num_items);
		SlogicalDim = String.valueOf(logicalDim);
		SdispInfo = "Num. lines: " + Snum_items;
	}

	lmInfo(int n)
	{
		num_items = n;
		logicalDim = 1.1D;
		center = false;
		lowleft = true;
		color = true;

		Snum_items = String.valueOf(num_items);
		SlogicalDim = String.valueOf(logicalDim);
		SdispInfo = "Num. lines: " + Snum_items;
	}
}

class tsInfo extends RInfo				/* Test Polygon */
{
	public boolean hull = true;
	public boolean square = false;

//	public int nv;
//	public String Snv;

	public tsInfo()
	{
		nv = 8;
		logicalDim = 10.0D;
		center = true;
		lowleft = false;
		
		Snv = String.valueOf(nv);
		SlogicalDim = String.valueOf(logicalDim);
	}
}

class drInfo extends RInfo				/* Draw Polygon */
{
//	public int nv;
//	public String Snv;

	public drInfo()
	{
		nv = NUM_ITEMS;

		Snv = String.valueOf(nv);
	}
}