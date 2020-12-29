package org.edisonwj.rapport;

/**
 * Generate all convex polygons for a given point set.
 * Uses algorithm of Mitchell et al, Elsevier 1999.
 */

import java.awt.*;
import java.util.*;

class RPolyRandC implements RapportDefaults
{
	private static final double epsilon = DEFAULTEPSILON;
	private static final boolean exact_comp = EXACT_COMP;

	// RInfo
	private pfInfo ri;
	private RPanel rp;

	// Generation parms
	protected int n;		/* Number of random points */
	private int t;			/* Total number of trials to run */

	protected Pointd[] v;	/* Random points */
	protected Pointd[] u;	/* Subset of random points above s */
	private Pointd sp;		/* Current lower most point */

	// Random number source
	private Rand mr;

	// Counters
	private Composite[][] f;
	protected long[] count;
	protected long totalCount;

	// Result
	private Vector pv;				/* Polygon vector */

	public RPolyRandC(pfInfo iri, Rand imr, Rand imr2, RPanel irp)
	{
//		System.out.println("RPolyRandC(pfInfo iri, Rand imr, Rand imr2, RPanel irp) constructor: " +
//				this.hashCode() +
//				" ri: " + iri.hashCode());
		ri = iri;
		mr = imr;
		t = ri.numb_objects;
		n = ri.nv;
		rp = irp;
	}

	public Vector genRPolyRandC()
	{
		pv = new Vector();

		// Run trials.
		for (int tr = 0; tr < t; tr++)
		{
			// Generate random set of points, S.
//			System.out.println("Trial " + tr + " seed: " + mr.getSeed());
			n=5;
			v = new Pointd[n];
//			for ( int i=0; i < n; i++ )
//			v[i] = new Pointd(mr.uniform(), mr.uniform());
//			v[0] = new Pointd(0.5648, 0.8003);
//			v[1] = new Pointd(0.7284, 0.0982);
//			v[2] = new Pointd(0.7264, 0.3230);
//			v[3] = new Pointd(0.8926, 0.8214);
//			v[4] = new Pointd(0.2045, 0.6593);
			
			v[0] = new Pointd(0.5648, 0.8003);
			v[1] = new Pointd(0.7000, 0.0982);
			v[2] = new Pointd(0.9200, 0.6000);
			v[3] = new Pointd(0.8926, 0.8214);
			v[4] = new Pointd(0.2045, 0.6593);
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
//			pv.add(new PolygonA(v));

			// For each point s, find the number of convex polygons determined
			// by S such that s is the lowest vertex of the polygon.

			count = new long[n];
			totalCount = 0;

			for (int i = 0; i < n-2; i++)
			{
				// Sort points around s
				sp = v[i];
//				System.out.println("\nNew sp= " + v[i]);
				u = new Pointd[n-i];
				System.arraycopy(v, i, u, 0, n-i);
				sortAroundsp(u);

//				System.out.println("Sorted about s= " + s);
//				for (int j = 0; j < u.length; j++)
//					System.out.println("u[" + j + "]= " + u[j]);
//				System.out.println();

//				if (u.length > 2)
//				pv.add(new PolygonA(u));

				// Find all polygons about (above) s
				// Initialize couting arrays
				int s = u.length-1;
				f = new Composite[u.length][];
				for (int p = 1; p < u.length-1; p++)
				{
					f[p] = new Composite[u.length];

					f[p][0] = new Composite(s, p, 0);
//					f[p][0].showComposite();
				}
//				System.out.println("End initialization");

				// Recursively find counts
//				for (int p = 2; p < u.length-1; p++)
				for (int p = 1; p < u.length-1; p++)
				{
//					for (int q = 1; q < p; q++)
					for (int q = 0; q < p; q++)
					{
//						System.out.println("Working: f("+p+","+q+","+s+")");
						f[p][q] = findPqs(p, q, s);
//						f[p][q].showComposite();
						f[p][q].addPolys(pv);
//						totalCount += f[p][q];
//						System.out.println("f("+p+","+q+","+s+")= " + f[p][q]);
					}
				}

//				for (int p = 3; p < u.length-1; p++)
//				{
//					for (int q = 0; q < p; q++)
//					{
//						f[p][q] = findPPqs(p, q, s);
//						totalCount += f[p][q];
////						System.out.println("f("+p+","+q+","+s+")= " + f[p][q]);
//					}
//				}

			}

//			System.out.println("\nCounts");
//			for (int i = 0; i < n; i++)
//				System.out.println("count[" + i + "]= " + count[i]);

			System.out.println("Trial " + tr + " seed: " + mr.getSeed() + " " + pv.size());
		}
		
		System.out.println("RPolyRandC complete - pv: " + pv.hashCode());
		
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

	public Composite findPqs(int p, int q, int s)
	{

		if ( f[p][q] != null )
		{
//			System.out.println("Return f(" + p + ", " + q + ", " + s + ")");
			return f[p][q];
		}
		else
		{
//			System.out.println("Compute f(" + p + ", " + q + ", " + s + ")");
			f[p][q] = new Composite(s, p, q);
			for (int r = 0; r < q; r++)
			{
				if ( Geometry.left(u[p], u[q], u[r]) && Geometry.left(u[s], u[q], u[r]) )
				{
//					System.out.println("Merge f(" + q + ", " + r + ", " + s + ")");
					f[p][q].merge(findPqs(q, r, s));
				}
			}
			return f[p][q];
		}
	}

//	public Composite findPPqs(int p, int q, int s)
//	{
//		int sum = 0;
//		if (f[p][q][s] != null)
//			return f[p][q][s];
//
//		else if ( p == (q+1) )
//			return findPqs(p, q, s);
//
//		else
//		{
//			sum += findPPqs(p-1, q, s);
//			for (int r = 0; r < q; r++)
//			{
//				if ( Geometry.left(u[p], u[q], u[r]) && Geometry.left(u[q], u[p-1], u[r]) )
//				sum += findPqs(q, r, s);
//			}
//			f[p][q][s] = sum;
//			return sum;
//		}
//	}

	private void drawPqs(int p, int q, int s)
	{
		rp.drawEdge(new EdgeR(u[s], u[p]), Color.red);
		rp.drawEdge(new EdgeR(u[p], u[q]), Color.red);
		rp.drawEdge(new EdgeR(u[q], u[s]), Color.red);
		pause();
	}

	public void pause()
	{
		if ( ri.pause > 100)
		{
			try
			{
				Thread.sleep(ri.pause);
			}
			catch (InterruptedException e){}
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

	private class Composite
	{
		int size;				/* Number of polygons */
		int[] base;				/* Common triangle component */
		ArrayList chains;		/* Vertices for polygons off base */

		public Composite(int is, int ip, int iq)
		{
			base = new int[3];
			base[0] = is;
			base[1] = ip;
			base[2] = iq;
		}

//		public void addVertex(int r)
//		{
//			if (next > indices.length-1)
//			{
//				int[] newIndices = new int[2*next];
//				System.arraycopy(indices, 0, newIndices, 0, indices.length);
//				indices = newIndices;
//			}
//
//			count[0]++;				/* Increment total polygon count */
//			count[next]++;			/* Increment triangle polygon count */
//			indices[next++] = r;	/* Store additional vertex */
//		}

		public int[] getBase()
		{
			return base;
		}

		public ArrayList getChains()
		{
			return chains;
		}

		public boolean hasChains()
		{
			if (chains == null)
				return false;
			else
				return true;
		}

		public void merge(Composite c)
		{
//			System.out.println("Entering merge. Show composite base.");
//			showComposite();
//			System.out.println("Entering merge. Show composite to be merged in.");
//			c.showComposite();

			if (chains == null)
				chains = new ArrayList();

			int basenode = (c.getBase())[2];
//			System.out.println("basenode: " + basenode);


			if (c.hasChains())
			{
				ArrayList temp = c.getChains();
				for (int i = 0; i < temp.size(); i++)
				{
					int[] subchain = (int[])temp.get(i);
					int[] newchain = new int[subchain.length+1];
					newchain[0] = basenode;
					System.arraycopy(subchain, 0, newchain, 1, subchain.length);
					chains.add(newchain);
				}
			}
			else
			{
				int[] newchain = new int[1];
				newchain[0] = basenode;
				chains.add(newchain);
			}
		}

		public void addPolys(Vector vt)
		{
			int nv = 0;

			ArrayList baseIndices = new ArrayList();
			ArrayList baseVertices = new ArrayList();

			count[0]++;
			count[2]++;
			for ( int i = 0; i < 3; i++)
			{
				baseIndices.add(new Integer(base[i]));
				baseVertices.add(u[base[i]]);
			}
//			showPolyIndices(baseIndices);
			vt.add(new PolygonA(baseVertices));
//			showPoly(baseIndices, baseVertices);

			if (hasChains())
			{
				int[] subchain = null;
				int[] prevchain = null;
				ArrayList prevIndices = null;
				for (int i = 0; i < chains.size(); i++)
				{
					ArrayList polyIndices = new ArrayList();
					ArrayList polyVertices = new ArrayList();
					polyIndices.addAll(baseIndices);
					polyVertices.addAll(baseVertices);
					subchain = (int[])chains.get(i);
					for (int j = 0; j < subchain.length; j++)
					{
						polyIndices.add(new Integer(subchain[j]));
						polyVertices.add(u[subchain[j]]);
						if ( isPrefix(polyIndices, prevIndices) )
							continue;
//						showPolyIndices(polyIndices);
						count[0]++;
						count[polyVertices.size()-1]++;
						vt.add(new PolygonA(polyVertices));
//						showPoly(polyIndices, polyVertices);
					}
					prevIndices = polyIndices;
				}
			}
		}

		private boolean isPrefix(ArrayList subchain, ArrayList prevchain)
		{
			if (prevchain == null || subchain.size() >= prevchain.size())
				return false;

			else
			{
				for (int i = 0; i < subchain.size(); i++)
				{
					if ( !((Integer)subchain.get(i)).equals((Integer)prevchain.get(i)) )
						return false;
				}
				return true;
			}
		}

		public void showComposite()
		{
			System.out.print("Composite base: ");
			for (int i = 0; i < 3; i++)
				System.out.print(base[i] + " " );
			System.out.println();
			if (hasChains())
			{
				for (int i = 0; i < chains.size(); i++)
				{
					int[] subchain = (int[])chains.get(i);
					System.out.print("Composite chain(" + i + ")= ");
					for (int j = 0; j < subchain.length; j++)
						System.out.print(subchain[j] + " " );
					System.out.println();
				}
			}
		}

		public void showPolyIndices(ArrayList polyIndices)
		{
			System.out.print("PolyIndices: ");
			for (int i = 0; i < polyIndices.size(); i++)
				System.out.print((Integer)polyIndices.get(i) + " ");
			System.out.println();
		}

		public void showPoly(ArrayList polyIndices, ArrayList polyVertices)
		{
			System.out.print("PolyIndices: ");
			for (int i = 0; i < polyIndices.size(); i++)
				System.out.print((Integer)polyIndices.get(i) + " ");
			System.out.println();

			System.out.print("PolyVertices: ");
			for (int i = 0; i < polyVertices.size(); i++)
				System.out.print((Pointd)polyVertices.get(i) + " ");
			System.out.println();
		}
	}
}