package org.edisonwj.rapport;

/**
 * Count all convex polygons for a given point set.
 * Uses basic algorithm of Mitchell et al, Elsevier 1999.
 */

import java.text.*;
import java.util.*;

class RPolyRandD implements RapportDefaults
{
	private static final double epsilon = DEFAULTEPSILON;
	private static final boolean exact_comp = EXACT_COMP;

	// RInfo
	private pfInfo ri;

	// Generation parms
	private int n;		/* Number of random points */
	private int t;		/* Total number of trials to run */

	private Pointd[] v;	/* Random points */
	private Pointd[] u;
	private Pointd s;

	// Random number source
	private Rand mr;

	// Counters
	private int[][] f;
	long[] count;

	// Result
	private Vector pv;				/* Polygon vector */

	public RPolyRandD(pfInfo iri, Rand imr, Rand imr2)
	{
//		System.out.println("RPolyRandD(pfInfo iri, Rand imr, Rand imr2) constructor: " +
//				this.hashCode() +
//				" ri: " + iri.hashCode());
		ri = iri;
		mr = imr;
		t = ri.numb_objects;
		n = ri.nv;
	}

	public Vector genRPolyRandD()
	{
//		System.out.println("genRPolyRandD");
		pv = new Vector();
		count = new long[t];

		// Run trials.
		for (int tr = 0; tr < t; tr++)
		{
//			System.out.println("\nStart trial: " + tr);
			count[tr] = 0;
			
			// Generate random set of points, S.
			v = new Pointd[n];
			for ( int i=0; i < n; i++ )
				v[i] = new Pointd(mr.uniform(), mr.uniform());
			
//			n = 6;
//			v = new Pointd[n];
//			v[0] = new Pointd(0.5648, 0.8003);
//			v[1] = new Pointd(0.7000, 0.0982);
//			v[2] = new Pointd(0.9200, 0.6000);
//			v[3] = new Pointd(0.8926, 0.8214);
//			v[4] = new Pointd(0.2045, 0.6593);
//			v[5] = new Pointd(0.9500, 0.7500);

//			System.out.println("Initial point set");
//			for (int i = 0; i < n; i++)
//				System.out.println("v[" + i + "]= " + v[i]);
//			System.out.println();

			// Sort S by y coordiante, and largest x if y's equal.
			sortByY(v);

//			System.out.println("Sorted point set");
//			for (int i = 0; i < n; i++)
//				System.out.println("v[" + i + "]= " + v[i]);
//			System.out.println();

			// For each point s, find the number of convex polygons determined
			// by S such that s is the lowest vertex of the polygon.

			for (int i = 0; i < n-2; i++)
			{
				// Sort points around s
				s = v[i];
				u = new Pointd[n-i];
				System.arraycopy(v, i, u, 0, n-i);
				sortArounds(u);

//				System.out.println("Sorted about s= " + s);
//				for (int j = 0; j < u.length; j++)
//					System.out.println("u[" + j + "]= " + u[j]);
//				System.out.println();

				// Find all polygons about (above) s
				int si = u.length-1;
				f = new int[u.length][];
				for (int p = 1; p < u.length-1; p++)
				{
					f[p] = new int[u.length];

					for (int q = 0; q < p; q++)
					{
						f[p][q] = findPqs(p, q, si);
						count[tr] += f[p][q];
					}
				}
			}
		}	
		printStats();
		return pv;
	}
	
	public int findPqs(int p, int q, int s)
	{
		if (f[p][q] > 0)
		{
			return f[p][q];
		}
		else
		{
			f[p][q] = 1;

			for (int r = 0; r < q; r++)
			{
				if ( Geometry.left(u[p], u[q], u[r]) && Geometry.left(u[s], u[q], u[r]) )
				{
					f[p][q] += findPqs(q, r, s);
				}
			}
			return f[p][q];
		}
	}

	private void sortByY(Pointd[] v)
	{
		Arrays.sort(v, new Comparator() {
			public int compare(Object a, Object b)
			{
				if ( ((Pointd)a).equals((Pointd)b) )
					return 0;
				else if ( ((Pointd)a).less((Pointd)b) )
					return -1;
				else
					return 1;
			}
		});
	}

	private void sortArounds(Pointd[] u)
	{
		Arrays.sort(u, new Comparator() {
			public int compare(Object a, Object b)
			{
				int as = area_sign(s, (Pointd)a, (Pointd)b);
				if ( as > 0)		/* left turn  */
					return 1;
				else if (as < 0)	/* right turn */
					return -1;
				else 				/* collinear  */
				{
					double x = Math.abs(((Pointd)a).getx() - s.getx()) - Math.abs(((Pointd)b).getx() - s.getx());
					double y = Math.abs(((Pointd)a).gety() - s.gety()) - Math.abs(((Pointd)b).gety() - s.gety());
					if ( (x < 0) || (y < 0) )
						return 1;
					else if ( (x > 0) || (y > 0) )
						return -1;
					else		// points are coincident
						return 0;
				}
			}
		});
	}

	private void printStats()
	{
		System.out.println("\nCounts:");
		for (int i = 0; i < t; i++)
			System.out.print(" " + count[i]);
		System.out.println();

		System.out.println("\nStats");
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
//		nf.setMinimumIntegerDigits(4);

		double avgcon;
		double varcon;
		double stdcon;
		double mincon;
		double maxcon;
		double cvcon ;


		avgcon = 0.0;
		varcon = 0.0;
		stdcon = 0.0;
		cvcon = 0.0;
		mincon = Double.MAX_VALUE;
		maxcon = 0.0;

		for (int i = 0; i < t; i++)
		{
			avgcon += count[i];
			varcon += count[i]*count[i];
			if (count[i] < mincon)
				mincon = count[i];
			if (count[i] > maxcon)
				maxcon = count[i];
		}
		varcon = (varcon - (avgcon*avgcon)/t)/(t-1);
		stdcon = Math.sqrt(varcon);
		avgcon = avgcon/t;
		cvcon = stdcon/avgcon;

		System.out.println( "Avg= " + nf.format(avgcon) +
							", Std= " + nf.format(stdcon) +
							", CV = " + nf.format(cvcon) +
							", Min= " + nf.format(mincon) +
							", Max= " + nf.format(maxcon));
		System.out.println();
	}

	private static int area_sign (Pointd a, Pointd b, Pointd c)
	{
	  double area2;
	  area2 = (a.x * b.y - a.y * b.x
	          +  b.x * c.y - b.y * c.x
	          +  c.x * a.y - c.y * a.x); /*twice_area (a, b, c);*/
	  if (exact_comp)
	  {
	    if (area2 < 0) return -1;
	    else if (area2 > 0) return 1;
	    else return 0;
	  }
	  else
	  {
	    if (area2 < -epsilon) return -1;
	    else if (area2 > epsilon) return 1;
	    else return 0;
	  }
	}
}