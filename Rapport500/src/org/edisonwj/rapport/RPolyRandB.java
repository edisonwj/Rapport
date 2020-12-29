package org.edisonwj.rapport;

/**
 * Generate simple polygons by exhaustive enumeration.
 * Creates all possible combinations in lexicograhpic order,
 * and all free ring permutations for each combination.
 */

import java.util.*;

class RPolyRandB implements RapportDefaults
{
	private static final double epsilon = DEFAULTEPSILON;
	private static final boolean exact_comp = EXACT_COMP;

	// RInfo
	private static pfInfo ri;

	// Generation parms
	private static int t;		/* Total number of trials to run */
	private static int tr;		/* Current trial number */
	private static int n;		/* Number of random points */
	private static int p=3;		/* Minimum length of combination to return */
	private static int k;		/* Combination generation index */

	private static int[] x;		/* Combination generation array */
	private static Pointd[] randomPoints;	/* Random points */

	// Random number source
	private static long seed;
	private static Rand mr;
	private static Rand mr2;

	// Thread control
	private static int nthread;	/* Number of threads to run */

	// Result
	private static Vector pv;	/* Polygon vector */

	public RPolyRandB(pfInfo iri, Rand imr, Rand imr2)	/* Generate polygon from random points */
	{
//		System.out.println("RPolyRandB(pfInfo iri, Rand imr, Rand imr2) constructor: " +
//				this.hashCode() +
//				" ri: " + iri.hashCode());
		ri = iri;
		mr = imr;
		mr2 = imr2;
		t = ri.numb_objects;
		n = ri.nv;
		nthread = 2;
		if ((int)ri.alpha > 3)
		{
			p = (int)ri.alpha;
		}
	}

	public Vector genRPolyRandB()
	{
		pv = new Vector();

		// Run trials.
		for (int tr = 0; tr < t; tr++)
		{
			// Generate random set of points
			randomPoints = new Pointd[n];
			for ( int i=0; i < n; i++ )
				randomPoints[i] = new Pointd(mr.uniform(), mr.uniform());

			//	Setup combination generation array
			x = new int[n+1];
			k = 1;
			x[k] = 1;

			// Start threads to loop thru combinations and permutations
			// checking for polygons.

			PermProc[] th = new PermProc[nthread];
			for (int i = 0; i < nthread; i++)
			{
				th[i] = new PermProc(i, randomPoints);
				th[i].start();
			}
			try
			{
				for (int i = 0; i <= nthread; i++)
					th[i].join();
			} catch (Exception e) {}
		}

	//	// Shuffle the results
	//	// Skiena, p. 248
	//	PolygonA ptemp;
	//	int pn = pv.size();
	//	int pi;
	//	for (int i = 0; i <= pn-2; i++)
	//	{
	//		ptemp = (PolygonA)pv.elementAt(i);
	//		pi = mr.uniform(i,pn-1);
	//		pv.setElementAt((PolygonA)pv.elementAt(pi),i);
	//		pv.setElementAt(ptemp,pi);
	//	}

//		System.out.println("RPolyRandB pv: " + pv.hashCode());	
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
		PolygonA p = new PolygonA(v);
		
//		if (p.isOther())
//		{
			pv.add(new PolygonA(v));
			if (pv.size()%10000 == 0)
				System.out.println("Number of polygons generated: " + pv.size());
//				System.out.println("RPolyRandB Object #" + pv.size() + ", Seed: " + mr.getSeed());
//		}
//		else
//			p = null;
	}

	/*
	 *	Based on:
	 *	Semba, Ichiro. Journal of Algorithms 5, 281-283 (1984).
	 *
	 *	Create all combinations with 3 or more items from n items.
	 *
 	 *	Returns the next combination of length >= p
	 */
	public static synchronized int[] nextComb()
	{
		int [] c = null;
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

//	Class to obtain a combination and process all requisite permuations.

class PermProc extends Thread implements RapportDefaults
{
	private static final double epsilon = DEFAULTEPSILON;
	private static final boolean exact_comp = EXACT_COMP;

	private int n;					/* Number of random points */
	private int threadId;			/* Number to easily identify thread */
	private int nv;					/* Number of values */
	private int[] a;				/* Permutation values */
	private int[] c;				/* Combination values */
	private int[] b;				/* Permutation generation */
	private boolean[] d;			/* Permutation generation */
	private Pointd[] randomPoints;	/* Base array of random points */
	private Pointd[] v;				/* Array for polygon vertices */
	private Pointd[] u;				/* Utility polygon copy array */
	private double[] pang;			/* Polar angles of polygon */
	private double[] rang;			/* Relativized polar angles of polygon */
	private double[] xang;			/* External angles of polygon */
	private boolean first;			/* Control value for perm generation */
	private long tp;				/* Total number of perms for a comb */
	private long count;				/* Count of permutations checked */

	// Metric arrays
	private long[] success;
	private long[] fail;
	private long[] convex;
	private long[] simple;
	private long[] mono;
	private long[] star;
	private long[] spiral;
	private long[] intcnt;

	// Wedgelist variables
	private WedgeList wl;			/* Wedgelist for polygon type checking */
	private int wlsize;				/* Number of entries in the wedgelist */
	private int wlmult;				/* Multiplicity of first entry in the wedgelist */

	public PermProc(int ti, Pointd[] rp)
	{
		threadId = ti;
		randomPoints = rp;
		n = randomPoints.length;

		success	= new long[n+1];
		fail	= new long[n+1];
		convex	= new long[n+1];
		simple	= new long[n+1];
		mono	= new long[n+1];
		star	= new long[n+1];
		spiral	= new long[n+1];
		intcnt	= new long[2*n];
		count = 0;
	}

	/*
     * Run a thread to check permutations for polygons
     */
	public void run()
	{
		while (true)			/* Find polygons */
		{
			c = RPolyRandB.nextComb();
			if (c == null)
				break;
//				showComb(threadId, c);

			a = new int[c.length];
			nv = c.length-1;
			v = new Pointd[nv];
			u = new Pointd[nv];
			pang = new double[nv];
			rang = new double[nv];
			xang = new double[nv];

//			Create all ring permutations of the combination

//			Use hashmap initially to check for dups or not created.
//			HashMap hm = new HashMap();
//			String key;

			tp = factorial(nv - 1)/2;		/* Get unique ring perms */
//			tp = factorial(nv - 1);			/* Get all circular perms */
//			long htp = tp/2;

			first = true;
			for (int i = 1; i <= tp; i++)	/* Get perms */
			{
				nextPerm();
//				showPerm(threadId, c, a, count);


//				if (i <= htp)
//				{
//					key = permKey();
//					System.out.println(i + " p: " + key);
//					if (hm.containsKey(key))
//						System.out.println("Key already present: " + key);
//					else
//						hm.put(key, null);
//				}
//				else
//				{
//					key = permRevKey();
//					if (hm.containsKey(key))
//						System.out.println(i + " f: " + key);
//					else
//						System.out.println(i + " n: " + key);
//				}


				for (int j = 0; j < nv; j++)
					v[j] = randomPoints[a[j]-1];
//				showPoly(threadId, v);

				count++;

				if (isSimple())
				{
					RPolyRandB.addPoly(v);
					success[0]++;
					success[nv]++;
				}

//				if (isSimple())
//				{
//					success[0]++;				/* Simple polygon */
//					success[nv]++;
//
//					// Put vertices in counter clockwise order
//					if (!ccw())
//						reverse();
//
//					// Compute polar angles, external angles, and wedgelist
//					computePolar();
//					computeExternal();
//					wl = new WedgeList(rang);
//
//					if (!isConvex() && isMonotone())	/* Non-convex monotone polygon */
//					{
//						RPolyRandB.addPoly(v);
//					}
//				}

				else
				{
					fail[0]++;
					fail[nv]++;
				}
			}
		}
	}

	/*
	 * Adapted from Johnson-Trotter algorithm as described in:
	 * Sedgewick. Permutation Generation Methods.
	 * ACM Computing Surveys, Vol. 9, No. 2, June 1977.
	 */
	private void nextPerm()
	{
		if (first)
		{
			// Consider c[1] fixed. Permute remaining values.
			for (int i = 1; i <= nv; i++)
				a[i-1] = c[i];

			b = new int[nv];
			d = new boolean[nv];
			for (int i = 2; i < nv; i++)
			{
				b[i] = 1;
				d[i] = true;
			}
			b[1] = 0;
			first = false;
			return;
		}

		else
		{
			int k = 0;
			int i = nv-1;
			int x = 0;
			while(b[i] == i)
			{
				if (!d[i])
					x++;
				d[i] = !d[i];
				b[i] = 1;
				i--;
			}

			while (i > 1)
			{
				if (d[i])
					k = b[i]+x;
				else
					k=i-b[i]+x;
				swap(k, k+1);
				b[i] = b[i]+1;
				return;
			}
		}
	}

	/*
	 * Swap array elements
	 */
	private void swap(int i, int j)
	{
		int temp;
		temp = a[i];
		a[i] = a[j];
		a[j] = temp;
	}

	/*
	 * Compute key from polygon indices
	 */
	public String permKey()
	{
		String s = "";
		for ( int i = 0; i < nv; i++)
			s += a[i];
		return s;
	}

	/*
	 * Compute reversed key from polygon indices
	 */
	public String permRevKey()
	{
		String s = "";
		s += a[0];
		for ( int i = nv-1; i > 0; i--)
			s += a[i];
		return s;
	}

	/*
	 * Compute n factorial
	 */
	public static long factorial(int n)
	{
		long fact = 1;

		if (n > 0 && n < 21)
			for (int i = 1; i <= n; i++)
				fact *= i;
		else
			fact = 0;
		return fact;
	}

	/*
	 * Check for simple polygon
	 */
	public boolean isSimple()
	{
		/* First let's check that vertices are distinct */
  		for(int i=0; i<nv-1; i++)
    		for(int j=i+1; j<nv; j++)
				if ( v[i].equals(v[j]) )
					return false;

		/* Check simplicity: no two edges should intersect */
       	/* improperly. Run through the edges 1,...,n-1 and check  */
       	/* them against 2,...,n                                   */
		/* Check the edge from v[i] to v[next(i,n)] */
		/* against the edge starting from v[j]      */
  		for(int i = 0; i < nv-1; i++)
    		for(int j = i+1; j < nv; j++)
				if ( ! Geometry.valid_edges(v[i],v[next(i,nv)], v[j],v[next(j,nv)]) )
					return false;
		return true;
	}

	/*
	 * Check for convex polygon.
	 */
	private boolean isConvex()
	{
		/* Find polar diagram */
		if (rang == null)
			computePolar();

		/* Find antipodal wedges with multiplicity 1 */
		if (wl == null)
			wl = new WedgeList(rang);

		if (wl.size() == 1 && wl.getFirstMult() == 1)
			return true;
		else
			return false;
	}

	/*
	 * Check for monotone polygon.
	 * Preparata and Supowit, Info. Proc. Lett. 12(4) (1981) 161-164.
	 */
	private boolean isMonotone()
	{
		/* Find polar diagram */
		if (rang == null)
			computePolar();

		/* Find antipodal wedges with multiplicity 1 */
		if (wl == null)
			wl = new WedgeList(rang);

		return wl.findMonotone();
	}

	/*
	 * Compute polar and relativized polar angles of polygon.
	 */
	private void computePolar()
	{
		// Sets u (point utility array) and pang (polar angles),
		// and rang (relativized polar angles).

		int i, j;

		// Translate all edges to start at origin
		for (i = 0; i < nv; i++)
		{
			j = next(i, nv);
			u[i] = new Pointd(v[j].getx()-v[i].getx(),
					  		  v[j].gety()-v[i].gety() );
		}

		// Find polar angles
//		rang = new double[v.length];
		for (i = 0; i < pang.length; i++)
		{
			double x = u[i].getx();
			double y = u[i].gety();
			double atan = Math.atan(y/x);
			if ( x >= 0.0 && y >= 0.0 )
				pang[i] = atan;
			else if ( x < 0.0 && y >= 0.0 )
				pang[i] =  Math.PI + atan;
			else if ( x < 0.0 && y < 0.0 )
				pang[i] = Math.PI + atan;
			else
				pang[i] = 2*Math.PI + atan;
		}

		// Find edge with least polar angle.
		j = findMin(pang);
		double mina = pang[j];

		// Relativize all edge angles to edge with least angle.
		// Effectively rotate all edges clockwise, so least angle is zero.
		// Make the least angle (edge) the first.

//		rang = new double[pang.length];
		for (i = 0; i < pang.length; i++)
		{
			rang[i] = pang[j] - mina;
			j = next(j, pang.length);
		}
	}

	/*
	 * Find minimum value.
	 */
	private int findMin(double[] a)
	{
		int mini = 0;
		double min = Double.MAX_VALUE;

		for (int i = 0; i < a.length; i++)
		{
			if (a[i] < min)
			{
				min = a[i];
				mini = i;
			}
		}

		return mini;
	}

	/*
	 * Computes external angles from polar angles.
	 */
	private void computeExternal()
	{
		/* Sets xang (external angles array) */

		int i, j;

		/* Find external angles at each vertex */
		double diff;
		for (i = 0; i < nv; i++)
		{
			j = prev(i, nv);
			diff = pang[i] - pang[j];
			if (Math.abs(diff) > Math.PI)
			{
				if ( pang[i] < Math.PI && pang[j] > Math.PI)
					xang[i] =  (Math.PI*2.0 - pang[j] + pang[i]);
				else if ( pang[j] < Math.PI && pang[i] > Math.PI)
					xang[i] = -(Math.PI*2.0 - pang[i] + pang[j]);
			}
			else
				xang[i] = pang[i] - pang[j];
		}
	}

	/*
	 * Show combination
	 */
	private static synchronized void showComb(int ti, int[] c)
	{
		int id = ti;
		System.out.print("Thread: " + id + " - ShowComb: ");
		for (int i = 1; i < c.length; i++)
			System.out.print(c[i] + " " );
		System.out.println();
	}

	/*
	 * Show permutation
	 */
	private static synchronized void showPerm(int ti, int[] c, int[] a, long ct)
	{
		int id = ti;
		System.out.print("Thread: " + id + ", ct: " + ct + " - Show Comb/Perm: ");
		for (int i = 1; i < c.length; i++)
			System.out.print(c[i] + " " );
		System.out.print(" - ");
		for (int i = 0; i < a.length; i++)
			System.out.print(a[i] + " " );
		System.out.println();
	}

	/*
	 * Show polygon
	 */
	private static synchronized void showPoly(int ti, Pointd[] v)
	{
		int id = ti;
		System.out.print("Thread: " + id + " - ShowPoly: ");
		for (int i = 0; i < v.length; i++)
			System.out.print(v[i] + " " );
		System.out.println();
	}

	/*
	 * Return true if vertices are in counter clockwise order
	 */
	public boolean ccw()
	{
		if (area2() > 0 )
			return true;
		else
			return false;
	}

 	/*
 	 * Reverse vertex order to make it Ccw
 	 */
 	public void reverse()
 	{
 		System.arraycopy( v, 0, u, 0, nv );
 		for (int i = 0; i < nv; i++)
 			v[i] = u[nv - i - 1];
 	}

	/*
	 * Return twice the area of the polygon
	 */
 	public double area2()
 	{
		double sum = 0;
		for (int i = 1; i <= nv - 2; i++)
			sum += Geometry.twice_area(v[0], v[i], v[i+1]);
		return sum;
 	}

	/*
	 * Return next index modulo n
	 */
	public int next(int i, int n)
	{
	  if (i < n-1) return i+1;
	  else return 0;
	}

	/*
	 * Return previous index modulo n
	 */
	public int prev(int i, int n)
	{
		if (i==0) return n-1;
		else return i-1;
	}

}