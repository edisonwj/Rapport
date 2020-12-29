package org.edisonwj.rapport;

/**
 * Generate convex polygons by enumeration.
 * Creates all possible combinations in lexicographic order.
 */

import java.util.*;

class RPolyRandA implements RapportDefaults
{
	private static final double epsilon = DEFAULTEPSILON;
	private static final boolean exact_comp = EXACT_COMP;

	// RInfo
	private pfInfo ri;

	// Generation parms
	static int t;		/* Total number of trials to run */
	static int tr;		/* Current trial number */
	static int n;		/* Number of random points */
	static int p;		/* Minimum length of combination to return */
	static int k;		/* Combination generation index */
	static long tc;	/* total combinations */

	static Pointd[] randomPoints;	/* Random points */
	static int[] x;	/* Combination generation array */

	// Thread info
	static int nthread;	/* Number of threads to run */

	// Random number source
	private Rand mr;
	private Rand mr2;

	// Metric array
	static long[][] totalConvex;

	// Result
	static Vector pv;				/* Polygon vector */

	public RPolyRandA(pfInfo iri, Rand imr, Rand imr2)
	{
//		System.out.println("RPolyRandA(pfInfo iri, Rand imr, Rand imr2) constructor: " +
//							this.hashCode() +
//							" ri: " + iri.hashCode());
		ri = iri;
		mr = imr;
		mr2 = imr2;
		t = ri.numb_objects;
		n = ri.nv;
		p = 3;
		nthread = 2;
		totalConvex	= new long[t][];
	}

	public Vector genRPolyRandA()
	{
		pv = new Vector();

		// Run trials.
		for (int tr = 0; tr < t; tr++)
		{
			totalConvex[tr] = new long[n+1];

			// Generate random set of points
			randomPoints = new Pointd[n];
			for ( int i=0; i < n; i++ )
				randomPoints[i] = new Pointd(mr.uniform(), mr.uniform());

			//	Setup combination generation array
			x = new int[n+1];
			k = 1;
			x[k] = 1;

//			Start threads to loop thru combinations
//			checking for convex polygons.

			CombProc[] th = new CombProc[nthread];
			for (int i = 0; i < nthread; i++)
			{
				th[i] = new CombProc(i, randomPoints);
				th[i].start();
			}
			try
			{
				for (int i = 0; i <= nthread; i++)
					th[i].join();
			} catch (Exception e) {}

//			for (int i = 0; i < n; i++)
//				System.out.println("totalConvex[" + tr + "][" + i + "]= " +
//								totalConvex[tr][i]);

		}
//		System.out.println("RPolyRandA pv: " + pv.hashCode());
		
		PolygonA p;
		for (int i = 0; i < pv.size(); i++) {
			p = (PolygonA)pv.get(i);
			p.setRInfo(ri);
			if (ri.color) {
				p.setColor(DEFAULT_POLYGON_COLOR);
				p.setXColor(DEFAULT_POLYGON_XCOLOR);
			}
		}

		return pv;
	}

 	public static synchronized void addPoly(Pointd[] v)
	{
		pv.add(new PolygonA(v));
		totalConvex[tr][0]++;
		totalConvex[tr][v.length]++;
	}

	/*
	 *	Based on:
	 *	Semba, Ichiro. Journal of Algorithms 5, 281-283 (1984).
	 *
	 *	Enumerate Convex Polygons
	 *  Returns next combinination in lexcographi order of length >= p.
	 */
	public static synchronized int[] nextComb()
	{
		int[] c = null;
		int nc = 0;
		while (nc == 0 && k > 0)
		{
			if (k >= p)
			{
				nc = k;
				c = new int[nc+1];
				for (int j = 1; j <= k; j++)
					c[j] = x[j];
			}

			if (x[k] == n)
			{
				k--;
				x[k]++;
			}

			else
			{
				k++;
				x[k] = x[k-1] + 1;
			}
		}
		return c;
	}
}

//	Class to obtain a combination and process it

class CombProc extends Thread implements RapportDefaults
{
	private static final double epsilon = DEFAULTEPSILON;
	private static final boolean exact_comp = EXACT_COMP;

	private int n;					/* Number of random points */
	private int threadId;			/* Number to easily identify thread */
	private int nv;					/* Number of values */
	private int[] c;				/* Combination values */
	private Pointd[] randomPoints;	/* Base array of random points */
	private Pointd[] v;				/* Array for polygon vertices */
	private int i2, i3;

	// Metric arrays
	private long[] fail;
	private long[] convex;
	private long count;
	private long newcount;
	private long oldcount;

	public CombProc(int ti, Pointd[] rp)
	{
		threadId = ti;
		randomPoints = rp;
		n = randomPoints.length;
		fail	= new long[n+1];
		convex	= new long[n+1];
		count = 0;
		newcount = 0;
		oldcount = 0;
	}

	public void run()
	{
		while (true)			/* Find polygons */
		{
			c = RPolyRandA.nextComb();
			if (c == null)
				break;
//			showComb(threadId, c);
			nv = c.length-1;
			v = new Pointd[nv];
			for (int j = 0; j < nv; j++)
				v[j] = randomPoints[c[j+1]-1];
//			showPoly(threadId, v);

			count++;
			if (checkConvexity())
			{
				convex[0]++;
				convex[nv]++;
				RPolyRandA.addPoly(v);
			}

			newcount = count/1000000;
			if (newcount > oldcount)
			{
				oldcount = newcount;
//				System.out.println( "Thread: " + threadId +
//									" Checked: " + count +
//									" Found: " + convex[0]);
			}

		}/* End of loop thru all combinations */
	}

 	/* Returns true if polygon is convex, else returns false */
 	private boolean checkConvexity()
 	{
		swap(0, findLowest());
		Arrays.sort(v, new Comparator() {
			public int compare(Object a, Object b)
			{
				int as = area_sign(v[0], (Pointd)a, (Pointd)b);
				if ( as > 0)		/* left turn  */
					return -1;
				else if (as < 0)	/* right turn */
					return 1;
				else 					/* collinear  */
				{
					double x = Math.abs(((Pointd)a).getx() - v[0].getx()) - Math.abs(((Pointd)b).getx() - v[0].getx());
					double y = Math.abs(((Pointd)a).gety() - v[0].gety()) - Math.abs(((Pointd)b).gety() - v[0].gety());
					if ( (x < 0) || (y < 0) )
						return -1;
					else if ( (x > 0) || (y > 0) )
						return 1;
					else		// points are coincident
						return 0;
				}
			}
		});

 		for (int i=0; i < nv; i++)
 		{
 			i2 = next(i, nv);
 			i3 = next(i2, nv);
 			if ( !lefton(	v[i], v[i2], v[i3]) )
 				return false;
 		}
 		return true;
	}

	private int findLowest()
	{
		int low = 0;
		for (int i = 1; i < nv; i++)
		{
			if ( (v[i].gety() < v[low].gety()) ||
				 ((v[i].gety() == v[low].gety()) && (v[i].getx() > v[low].getx())) )
				 low = i;
		}
		return low;
	}

	private void swap(int i, int j)
	{
		Pointd temp;
		temp = v[i];
		v[i] = v[j];
		v[j] = temp;
	}

	private static synchronized void showComb(int ti, int[] c)
	{
		int id = ti;
		System.out.print("Thread: " + id + " - ShowComb: ");
		for (int i = 1; i < c.length; i++)
			System.out.print(c[i] + " " );
		System.out.println();
	}

	private static synchronized void showPoly(int ti, Pointd[] v)
	{
		int id = ti;
		System.out.print("Thread: " + id + " - ShowPoly: ");
		for (int i = 0; i < v.length; i++)
			System.out.print(v[i] + " " );
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
}