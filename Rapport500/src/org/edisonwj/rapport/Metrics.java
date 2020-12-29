package org.edisonwj.rapport;

import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;

/*
 * Computes and manages polygon metrics.
 * Metrics are:
 *				number of vertices
 *				area
 *				perimeter
 *				complexity
 *				compacity
 *
 * Methods provided are:
 *				constructor
 *				count
 *				print stats
 */
class Metrics implements RapportDefaults
{
	private static final double epsilon = DEFAULTEPSILON;
	private static final boolean exact_comp = EXACT_COMP;
	private static final int debug = 1;

	private class MetricsException
		extends RuntimeException
	{
		public MetricsException()
		{
			super();
		}

		public MetricsException(String s)
		{
			super(s);
		}
	}

	// Metrics Exceptions
	protected MetricsException erroneous_state1 =
		new MetricsException("erroneous state1");
	protected MetricsException erroneous_state2 =
		new MetricsException("erroneous state2");

	// Metrics Storage
	protected Hull h=null;		/* Temporary working area for polygon hull */

	protected double total_polygons;

	protected double total_vertices;
	protected double total2_vertices;
	protected double mean_vertices;
	protected double var_vertices;
	protected double min_vertices;
	protected double max_vertices;

	protected double total_area;
	protected double total2_area;
	protected double mean_area;
	protected double var_area;
	protected double min_area;
	protected double max_area;

	protected double total_perimeter;
	protected double total2_perimeter;
	protected double mean_perimeter;
	protected double var_perimeter;
	protected double min_perimeter;
	protected double max_perimeter;

	protected double total_compacity;
	protected double total2_compacity;
	protected double mean_compacity;
	protected double var_compacity;
	protected double min_compacity;
	protected double max_compacity;
	protected double[] bin_compacity;

	protected double total_complexity;
	protected double total2_complexity;
	protected double mean_complexity;
	protected double var_complexity;
	protected double min_complexity;
	protected double max_complexity;
	protected double[] bin_complexity;

	protected double total_areaRatio;
	protected double total2_areaRatio;
	protected double mean_areaRatio;
	protected double var_areaRatio;
	protected double min_areaRatio;
	protected double max_areaRatio;

	protected double total_perimeterRatio;
	protected double total2_perimeterRatio;
	protected double mean_perimeterRatio;
	protected double var_perimeterRatio;
	protected double min_perimeterRatio;
	protected double max_perimeterRatio;

	/*
	 * Constructor - setup storage and initialize for tracking polygon metrics.
	 */
	public Metrics()
	{
		diag(3,"Construct metrics manager");
		initialize();
	}

	/*
	 * Initialize storage
	 */
	 private void initialize()
	 {
		total_polygons = 0.0;

		total_vertices = 0.0;
		total2_vertices = 0.0;
		mean_vertices = 0.0;
		var_vertices = 0.0;
		min_vertices = Double.MAX_VALUE;
		max_vertices = Double.MIN_VALUE;

		total_area = 0.0;
		total2_area = 0.0;
		mean_area = 0.0;
		var_area = 0.0;
		min_area = Double.MAX_VALUE;
		max_area = Double.MIN_VALUE;

		total_perimeter = 0.0;
		total2_perimeter = 0.0;
		mean_perimeter = 0.0;
		var_perimeter = 0.0;
		min_perimeter = Double.MAX_VALUE;
		max_perimeter = Double.MIN_VALUE;

		total_compacity = 0.0;
		total2_compacity = 0.0;
		mean_compacity = 0.0;
		var_compacity = 0.0;
		min_compacity = Double.MAX_VALUE;
		max_compacity = Double.MIN_VALUE;

		total_complexity = 0.0;
		total2_complexity = 0.0;
		mean_complexity = 0.0;
		var_complexity = 0.0;
		min_complexity = Double.MAX_VALUE;
		max_complexity = Double.MIN_VALUE;

		total_areaRatio = 0.0;
		total2_areaRatio = 0.0;
		mean_areaRatio = 0.0;
		var_areaRatio = 0.0;
		min_areaRatio = Double.MAX_VALUE;
		max_areaRatio = Double.MIN_VALUE;

		total_perimeterRatio = 0.0;
		total2_perimeterRatio = 0.0;
		mean_perimeterRatio = 0.0;
		var_perimeterRatio = 0.0;
		min_perimeterRatio = Double.MAX_VALUE;
		max_perimeterRatio = Double.MIN_VALUE;

		bin_complexity = new double[1002];
		for (int i = 0; i < bin_complexity.length; i++)
			bin_complexity[i] = 0.0;

		bin_compacity = new double[1002];
		for (int i = 0; i < bin_compacity.length; i++)
			bin_compacity[i] = 0.0;
	 }

	/*
	 * Compute and tally metrics for polygon
	 */
	public synchronized void count(Pointd[] v)
	{
		diag(3,"Count individual metrics");

//		if (isConvex(v))		/* Exclude convex polygons from count */
//			return;

		double n, a, p, ah, ph, ar, pr, nt, c, x;

		total_polygons++;

		/* Find polygon hull */
		h = new Hull(v);					/* Find hull */

		/* Compute metrics */
		n = v.length;						/* number of vertices */
		a = computeArea(v);					/* area */
		p = computePerimeter(v);			/* perimeter */
		ah = h.computeArea();				/* hull area */
		ph = h.computePerimeter();			/* hull perimeter */
		ar = computeAreaRatio(a, ah);		/* area to hull-area ratio */
		pr = computePerimeterRatio(p, ph);	/* perimeter to hull-perimeter ratio */
		c = computeCompacity(v, a, p);			/* compacity */
		nt = countNotches(v);				/* notches */
		x = computeComplexity(n, a, p, nt, ah, ph);	/* complexity */

		total_vertices += n;
		total_area += a;
		total_perimeter += p;
		total_areaRatio += ar;
		total_perimeterRatio += pr;
		total_complexity += x;
		total_compacity += c;

		total2_vertices += n*n;
		total2_area += a*a;
		total2_perimeter += p*p;
		total2_areaRatio += ar;
		total2_perimeterRatio += pr;
		total2_complexity += x*x;
		total2_compacity += c*c;

		if (n < min_vertices)
			min_vertices = n;
		if (n > max_vertices)
			max_vertices = n;

		if (a < min_area)
			min_area = a;
		if (a > max_area)
			max_area = a;

		if (p < min_perimeter)
			min_perimeter = p;
		if (p > max_perimeter)
			max_perimeter = p;

		if (ar < min_areaRatio)
			min_areaRatio = ar;
		if (ar > max_areaRatio)
			max_areaRatio = ar;

		if (pr < min_perimeterRatio)
			min_perimeterRatio = pr;
		if (pr > max_perimeterRatio)
			max_perimeterRatio = pr;

		if (c < min_compacity)
			min_compacity = c;
		if (c > max_compacity)
			max_compacity = c;

		if (x < min_complexity)
			min_complexity = x;
		if (x > max_complexity)
			max_complexity = x;

		int k;
//		k = (int)Math.floor(x/.01);

		k = (int)Math.floor(x/.001);
		if (k < 0)
		{
			System.out.println("Negative k= " + k + ", x= " + x);
			k = 0;
		}
		bin_complexity[k]++;

//		if (c < .9)
//			k = (int)Math.floor(c/.1);
//		else if (c >= .9 && c < .99 )
//			k = (int)Math.floor((c-.9)/.01) + 9;
//		else
//			k = (int)Math.floor((c-.99)/.001) + 18;

		k = (int)Math.floor(c/.001);
		bin_compacity[k]++;
	}

	/*
	 * Compute and print final statistics
	 */
	public void printStats()
	{
		double t = total_polygons;

		mean_vertices = total_vertices/t;
		mean_area = total_area/t;
		mean_perimeter = total_perimeter/t;
		mean_areaRatio = total_areaRatio/t;
		mean_perimeterRatio = total_perimeterRatio/t;
		mean_complexity = total_complexity/t;
		mean_compacity = total_compacity/t;

		var_vertices = (total2_vertices - (total_vertices*total_vertices)/t)/(t-1);
		var_area = (total2_area - (total_area*total_area)/t)/(t-1);
		var_perimeter = (total2_perimeter - (total_perimeter*total_perimeter)/t)/(t-1);
		var_areaRatio = (total2_areaRatio - (total_areaRatio*total_areaRatio)/t)/(t-1);
		var_perimeterRatio = (total2_perimeterRatio - (total_perimeterRatio*total_perimeterRatio)/t)/(t-1);
		var_complexity = (total2_complexity - (total_complexity*total_complexity)/t)/(t-1);
		var_compacity = (total2_compacity - (total_compacity*total_compacity)/t)/(t-1);

		System.out.println("Polygon Metrics - total number of polygons = " + total_polygons);

		System.out.println("Mean vertices = " + mean_vertices +
							"\nVar. vertices = " + var_vertices +
							"\nStd. vertices = " + Math.sqrt(var_vertices) +
							"\nMin. vertices = " + min_vertices +
							"\nMax. vertices = " + max_vertices +
							"\n");

		System.out.println("Mean area = " + mean_area +
							"\nVar. area = " + var_area +
							"\nStd. area = " + Math.sqrt(var_area) +
							"\nMin. area = " + min_area +
							"\nMax. area = " + max_area +
							"\n");

		System.out.println("Mean perimeter = " + mean_perimeter +
							"\nVar. perimeter = " + var_perimeter +
							"\nStd. perimeter = " + Math.sqrt(var_perimeter) +
							"\nMin. perimeter = " + min_perimeter +
							"\nMax. perimeter = " + max_perimeter +
							"\n");

		System.out.println("Mean area ratio = " + mean_areaRatio +
							"\nVar. area ratio = " + var_areaRatio +
							"\nStd. area ratio = " + Math.sqrt(var_areaRatio) +
							"\nMin. area ratio = " + min_areaRatio +
							"\nMax. area ratio = " + max_areaRatio +
							"\n");

		System.out.println("Mean perimeter ratio = " + mean_perimeterRatio +
							"\nVar. perimeter ratio = " + var_perimeterRatio +
							"\nStd. perimeter ratio = " + Math.sqrt(var_perimeterRatio) +
							"\nMin. perimeter ratio = " + min_perimeterRatio +
							"\nMax. perimeter ratio = " + max_perimeterRatio +
							"\n");

		System.out.println("Mean complexity = " + mean_complexity +
							"\nVar. complexity = " + var_complexity +
							"\nStd. complexity = " + Math.sqrt(var_complexity) +
							"\nMin. complexity = " + min_complexity +
							"\nMax. complexity = " + max_complexity +
							"\n");


		System.out.println("Mean compacity = " + mean_compacity +
							"\nVar. compacity = " + var_compacity +
							"\nStd. compacity = " + Math.sqrt(var_compacity) +
							"\nMin. compacity = " + min_compacity +
							"\nMax. compacity = " + max_compacity +
							"\n");

//		for (int i = 0; i < bin_complexity.length; i++)
//			System.out.println("bin_complexity[" + i + "]=  " + bin_complexity[i]);
//		System.out.println();

		if (debug > 1)
		{
		for (int i = 0; i < bin_compacity.length; i++)
			System.out.println("bin_compacity[" + i + "]=  " + bin_compacity[i]);
		System.out.println();
		}
	}

	/*
	 * Print diagnostic messages
	 */
	private void diag(int i, String s)
	{
		if (debug >= i)
			System.out.println(s);
	}

	/*
	 * Computes the area of the polygon
	 */
 	private double computeArea(Pointd[] v)
 	{
		double sum = 0;
		for (int i = 1; i <= v.length - 2; i++)
			sum += twiceArea(v[0], v[i], v[i+1]);
		return 0.5D * sum;
 	}

	/*
	 * Computes twice the area of the triangle abc
	 */
	private double twiceArea(Pointd a, Pointd b, Pointd c)
	{
		return (   a.getx() * b.gety() - a.gety() * b.getx()
				+  b.getx() * c.gety() - b.gety() * c.getx()
				+  c.getx() * a.gety() - c.gety() * a.getx());
	}

  	/*
 	 * Computes the perimeter of the polygon
 	 */
 	private double computePerimeter(Pointd[] v)
 	{
 		double per = 0.0D;

 		for (int i = 0; i < v.length - 1; i++)
 			per += Math.sqrt(distance2(v[i], v[i+1]));
 		per += Math.sqrt(distance2(v[v.length-1], v[0]));
 		return per;
	}

	/*
	 * Computes compacity
	 */
	private double computeCompacity(Pointd[] v, double area, double perimeter)
	{
		double compacity = 0.0D;
		compacity = (4*Math.PI*area)/(perimeter*perimeter);
		compacity = 1.0 - compacity;
//		if (isConvex(v))
//			compacity = compacity * 0.25D;

		return compacity;
	}

	/*
	 * Computes areaRatio
	 */
	private double computeAreaRatio(double area, double hullArea)
	{
//		System.out.println("a= " + area + ", ah= " + hullArea + ", ar= " + area/hullArea);
		return area/hullArea;
	}

	/*
	 * Computes perimeterRatio
	 */
	private double computePerimeterRatio(double perimeter, double hullPerimeter)
	{
//		System.out.println("p= " + perimeter + ", ph= " + hullPerimeter + ", pr= " + hullPerimeter/perimeter);
		return hullPerimeter/perimeter;
	}

	/*
	 * Counts notches in a ccw oriented polygon
	 */
	private int countNotches(Pointd[] v)
	{
		int i2, i3;
		int count = 0;

		for (int i=0; i < v.length; i++)
		{
			i2 = next(i, v.length);
			i3 = next(i2, v.length);
			if ( !lefton(	v[i], v[i2], v[i3]) )
				count++;
		}
		return count;
	}

	/*
	 * Computes Brinkhoff complexity
	 */
	private double computeComplexity(double n,
									 double area,
									 double perimeter,
									 double notches,
									 double hullArea,
									 double hullPerimeter)
	{
		double notches_norm, notches_squared, notches_quad;
		double freq, ampl, conv, complexity;

		if (n == 3)
		{
			notches_norm = 0.0D;
			notches_squared = .25D;
		}
		else
		{
			notches_norm = notches / (n - 3.0);
			notches_squared = (notches_norm - 0.5) * (notches_norm - 0.5);
		}
		notches_quad = notches_squared * notches_squared;
		freq = 16.0*notches_quad - 8.0*notches_squared + 1.0;
		ampl = (perimeter - hullPerimeter) / perimeter;
		conv = (hullArea - area) / hullArea;
		complexity = 0.8 * ampl * freq + .2 * conv;
		if (complexity < 0.0)
			complexity = 0.0;
		return complexity;
	}

	/*
	 * Computes square of distance between two points
	 */
	private double distance2(Pointd a, Pointd b)
	{
		double dx = a.getx() - b.getx();
		double dy = a.gety() - b.gety();
		return dx * dx + dy * dy;
	}

	/*
	 * Computes distance between two points
	 */
	private double distance(Pointd a, Pointd b)
	{
		return Math.sqrt(distance2(a, b));
	}

	/*
	 * Returns true if point c is left of or on line ab
	 */
	private boolean lefton (Pointd a, Pointd b, Pointd c)
	{
		double twoArea =  twiceArea(a, b, c);

		if (exact_comp)
		{
			if (twoArea >= 0)
				return true;
			else
				return false;
		}
		else
		{
	    	if ( (twoArea > epsilon) || (Math.abs(twoArea) <= epsilon) )
	    		return true;
	    	else
	    		return false;
	  	}
	}

	/*
	 * Returns i+1 modulo n
	 */
	private int next (int i, int n)
	{
		if (i < n-1)
			return i+1;
		else
			return 0;
	}

	/*
	 * Returns i-1 module n
	 */
	private int prev(int i, int n)
	{
		if (i==0) return n-1;
		else return i-1;
	}

	/*
	 * Check for convex polygon.
	 */
	private boolean isConvex(Pointd[] v)
	{
		int i2, i3;
		for (int i = 0; i < v.length; i++)
		{
			i2 = next(i, v.length);
			i3 = next(i2, v.length);
			if ( !lefton(	v[i], v[i2], v[i3]) )
				return false;
		}
		return true;
	}
}