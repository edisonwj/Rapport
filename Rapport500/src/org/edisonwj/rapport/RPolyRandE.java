package org.edisonwj.rapport;

/*
 * Count all convex k-gons for a given point set.
 * Uses algorithm by Mitchell et al, Elsevier 1999.
 */

import java.text.*;
import java.util.*;

class RPolyRandE implements RapportDefaults
{
	private static final double epsilon = DEFAULTEPSILON;
	private static final boolean exact_comp = EXACT_COMP;

	// RInfo
	private pfInfo ri;

	// Generation parms
	protected static int n;	/* Number of random points */
	protected static int t;	/* Total number of trials to run */
	protected static int tr;/* Current trial number */

	protected Pointd[] v;	/* Random points */
	protected Pointd[] u;	/* Subset of random points above s */
	private Pointd sp;		/* Current lower most point */

	// Random number source
	private Rand mr;
	private Rand mr2;

	// Counters
	private int[][][] f;
	protected static long[][] count;

	// Result
	private Vector pv;				/* Polygon vector */

	public RPolyRandE(pfInfo iri, Rand imr, Rand imr2)
	{
//		System.out.println("RPolyRandE(pfInfo iri, Rand imr, Rand imr2) constructor: " +
//				this.hashCode() +
//				" ri: " + iri.hashCode());
		ri = iri;
		mr = imr;
		mr2 = imr2;
		t = ri.numb_objects;
		n = ri.nv;
		count = new long[t][];
	}

	public Vector genRPolyRandE()
	{
		pv = new Vector();

		// Run trials.
		for (tr = 0; tr < t; tr++)
		{
			count[tr] = new long[n];

			// Generate random set of points, S.
//			System.out.println("Trial " + tr + " seed: " + mr.getSeed());
			v = new Pointd[n];
			for ( int i=0; i < n; i++ )
				v[i] = new Pointd(mr.uniform(), mr.uniform());

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

			// For each point sp, find the number of convex polygons determined
			// by S such that sp is the lowest vertex of the polygon.

			for (int i = 0; i < n-2; i++)
			{
				// Sort points around s
				sp = v[i];
				u = new Pointd[n-i];
				System.arraycopy(v, i, u, 0, n-i);
				sortAroundsp(u);
//				System.out.println("\nNew sp= " + v[i] + ", u.length= " + u.length);
//				System.out.println("Sorted pointd around sp");
//				for (int iu = 0; iu < u.length; iu++)
//					System.out.println("u[" + iu + "]= " + u[iu]);
//				System.out.println();

				// Find all polygons about (above) s
				// Initialize couting arrays
				int s = u.length-1;
				f = new int[u.length][][];
				for (int p = 1; p < u.length-1; p++)
					f[p] = new int[u.length][];

				// Recursively find counts
//				for (int p = 2; p < u.length-1; p++)
				for (int p = 1; p < u.length-1; p++)
				{
//					for (int q = 1; q < p; q++)
					for (int q = 0; q < p; q++)
					{
//						System.out.println("Working: f("+p+", "+q+", "+s+")");
						f[p][q] = findPqs(p, q, s);
//						showInt(p, q);
						addCount(tr, p, q);
					}
				}
			}
			System.gc();
		}
		printCounts();
		printStats();
		return pv;
	}

	public int[] findPqs(int p, int q, int s)
	{
		int[] temp;

		if ( f[p][q] != null )
		{
//			System.out.println("Return f(" + p + ", " + q + ", " + s + ")");
//			showInt(p, q);
			return f[p][q];
		}
		else
		{
//			System.out.println("Compute f(" + p + ", " + q + ", " + s + ")");
			f[p][q] = new int[u.length];
			f[p][q][0] = 1;
			f[p][q][2] = 1;

			for (int r = 0; r < q; r++)
			{
				if ( Geometry.left(u[p], u[q], u[r]) && Geometry.left(u[s], u[q], u[r]) )
				{
//					System.out.println("Merge f(" + q + ", " + r + ", " + s + ")");
					temp = findPqs(q, r, s);
					f[p][q][0] += temp[0];
					for (int i = 2; i < temp.length-1; i++)
						f[p][q][i+1] += temp[i];
//					showInt(p, q);
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

	private void sortAroundsp(Pointd[] u)
	{
		Arrays.sort(u, new Comparator() {
			public int compare(Object a, Object b)
			{
				int as = area_sign(sp, (Pointd)a, (Pointd)b);
				if ( as > 0)		/* left turn  */
					return 1;
				else if (as < 0)	/* right turn */
					return -1;
				else 					/* collinear  */
				{
					double x = Math.abs(((Pointd)a).getx() - sp.getx()) - Math.abs(((Pointd)b).getx() - sp.getx());
					double y = Math.abs(((Pointd)a).gety() - sp.gety()) - Math.abs(((Pointd)b).gety() - sp.gety());
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

	private void addCount(int t, int p, int q)
	{
		for (int i = 0; i < f[p][q].length; i++)
			count[t][i] += f[p][q][i];
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

	private static boolean lefton (Pointd a, Pointd b, Pointd c)
	{
		double x;
		double Two_Area;
		Two_Area =  (a.x * b.y - a.y * b.x
					+  b.x * c.y - b.y * c.x
					+  c.x * a.y - c.y * a.x);
		if (exact_comp)
		{
			if (Two_Area >= 0) return true;
				else return false;
		}
		else
		{
	    	x = Two_Area;
	    	if ( (x > epsilon) || (Math.abs(x) <= epsilon) ) return true;
	    	else return false;
	  	}
  	}

	private static int next (int i, int n)
	{
	  if (i < n-1) return i+1;
	  else return 0;
	}

	private static void printCounts()
	{
		System.out.println("\nK-gon Counts");
		for (int i = 0; i < n; i++)
		{
			System.out.print("count[" + i + "]= ");
			for (int j = 0; j < t; j++)
				System.out.print(count[j][i] + " " );
			System.out.println();
		}
	}

	private static void printStats()
	{
		System.out.println("\nK-gon Stats");
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
//		nf.setMinimumIntegerDigits(4);

		double[] avgcon = new double[n];
		double[] varcon = new double[n];
		double[] stdcon = new double[n];
		double[] mincon = new double[n];
		double[] maxcon = new double[n];
		double[] cvcon  = new double[n];

		for (int i = 0; i < n; i++)
		{
			avgcon[i] = 0.0;
			varcon[i] = 0.0;
			stdcon[i] = 0.0;
			cvcon[i] = 0.0;
			mincon[i] = Double.MAX_VALUE;
			maxcon[i] = 0.0;
		}

		for (int i = 0; i < n; i++)
		{
			for (int j = 0; j < t; j++)
			{
				avgcon[i] += count[j][i];
				varcon[i] += count[j][i]*count[j][i];
				if (count[j][i] < mincon[i])
					mincon[i] = count[j][i];
				if (count[j][i] > maxcon[i])
					maxcon[i] = count[j][i];
			}
			varcon[i] = (t*varcon[i] - avgcon[i]*avgcon[i])/(t*(t-1));
			stdcon[i] = Math.sqrt(varcon[i]);
			avgcon[i] = avgcon[i]/t;
			cvcon[i] = stdcon[i]/avgcon[i];
		}

		for (int i = 0; i < n; i++)
			System.out.println(i +  " Avg= " + nf.format(avgcon[i]) +
									", Std= " + nf.format(stdcon[i]) +
									", CV = " + nf.format(cvcon[i]) +
									", Min= " + nf.format(mincon[i]) +
									", Max= " + nf.format(maxcon[i]));

		System.out.println();
	}

	public void showInt(int p, int q)
	{
		System.out.print("Int(" + p + ", " + q +")= ");
		for (int i = 0; i < f[p][q].length; i++)
			System.out.print(f[p][q][i] + " " );
		System.out.println();
	}
}