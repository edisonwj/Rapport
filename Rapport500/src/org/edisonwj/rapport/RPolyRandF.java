package org.edisonwj.rapport;

/**
 * Count all convex polygons for a given point set.
 * Uses improved algorithm of Mitchell et al, Elsevier 1999.
 */

import java.text.*;
import java.util.*;

class RPolyRandF implements RapportDefaults
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
    private Pointd[] a;
    private Pointd[] b;
	private Pointd s;

	// Random number source
	private Rand mr;
	private Rand mr2;

	// Counters
	private int[][] f;
	long[] count;

	// Result
	private Vector pv;				/* Polygon vector */

	public RPolyRandF(pfInfo iri, Rand imr, Rand imr2)
	{
//		System.out.println("RPolyRandF(pfInfo iri, Rand imr, Rand imr2) constructor: " +
//				this.hashCode() +
//				" ri: " + iri.hashCode());
		ri = iri;
		mr = imr;
		mr2 = imr2;
		t = ri.numb_objects;
		n = ri.nv;
	}

	public Vector genRPolyRandF()
	{
		pv = new Vector();
		count = new long[t];

		// Run trials.
		for (int tr = 0; tr < t; tr++)
		{
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

			// Sort S by y coordinate, and largest x if y's equal.
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
				sortAround(s, u);

//				System.out.println("Sorted about s: " + s);
//				for (int j = 0; j < u.length; j++)
//					System.out.println("u[" + j + "] = " + u[j]);
//				System.out.println();
				
				// Initialize arrays for counting
				f = new int[u.length][];
				for (int j = 0; j < u.length; j++)
					f[j] = new int[u.length];
				a = new Pointd[u.length];

				// Find all polygons about (above) s
				// Loop thru q's
				int si = u.length-1;
				for (int q = 0; q < u.length-2; q++)
				{
					// Select p's left of q,s
					int ai = 0;
					for (int p = q+1; p < u.length-1; p++)
						if (Geometry.left(u[q], u[si], u[p]))
							a[ai++] = u[p];
					b = new Pointd[ai];
					System.arraycopy(a, 0, b, 0, ai);
					sortAround(u[q], b);
					
//					System.out.println("Sorted around u[ " + q + "]: " + u[q]);
//					for (int k = 0; k < b.length; k++)
//						System.out.println("b[" + k + "]= " + b[k]);
//					System.out.println();
					
					// Evaluate first p
					int p = findPinU(b[0], u);
					f[p][q] = findPqs(p,q,si);
					count[tr] += f[p][q];
						
					// Loop thru remaining selected p's
					for (int bp = 1; bp < b.length; bp++)
					{
						p = findPinU(b[bp], u);
						f[p][q] = findPPqs(bp, q, si);
						count[tr] += f[p][q];
					}
				}
			}
		}
		printStats();
		return pv;
	}
	
	public int findPinU(Pointd p, Pointd[] u) {
		int index;
		for (index = 0; index < u.length; index++ ) {
			if (p.equals(u[index])) {
				break;
			}
		}
		return index;
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
	
	public int findPPqs(int bp, int q, int s)
	{
		int pi = findPinU(b[bp], u);
		int sum = 0;
		if (f[pi][q] > 0)
		{
			return f[pi][q];
		}
		else
		{
			sum += findPPqs((bp-1), q, s);
			int pm = findPinU(b[bp-1], u);
			for (int r = 0; r < q; r++)
			{
				if ( Geometry.left(u[pi], u[q], u[r]) && Geometry.left(u[q], u[pm], u[r]) )
				{
					sum += findPqs(q, r, s);
				}
			}
			f[pi][q] = sum;
			return sum;
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
	
	public int compareEqual(Object a, Object b)
	{
		if ( ((Pointd)a).equals((Pointd)b) )
			return 0;
		else if ( ((Pointd)a).less((Pointd)b) )
			return -1;
		else
			return 1;
	}

	private void sortAround(Pointd p, Pointd[] u)
	{
		final Pointd c;
		c = p;
		Arrays.sort(u, new Comparator() {
			public int compare(Object a, Object b)
			{
				int as = area_sign(c, (Pointd)a, (Pointd)b);
				if ( as > 0)		/* left turn  */
					return 1;
				else if (as < 0)	/* right turn */
					return -1;
				else 					/* collinear  */
				{
					double x = Math.abs(((Pointd)a).getx() - c.getx()) - Math.abs(((Pointd)b).getx() - c.getx());
					double y = Math.abs(((Pointd)a).gety() - c.gety()) - Math.abs(((Pointd)b).gety() - c.gety());
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