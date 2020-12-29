package org.edisonwj.rapport;

/**
 * Class for performing a line sweep intersection
 * test on a set of line segments
 */

import java.util.*;

class LineIntersection implements RapportDefaults
{
	int nv;
	RSegment[] rs;
	PointE[] ep;
	RedBlackTree st;

	/*
	 * Perform line sweep on polygon points in v
	 */
	public LineIntersection(Pointd[] v)
	{
		nv = v.length;

		// Print input array.
		System.out.println("Initial point set");
		for (int i = 0; i < nv; i++)
			System.out.println("v[" + i + "]= " + v[i]);
		System.out.println();

		// Create arrays of segments and endpoints.
		rs = new RSegment[nv];
		ep = new PointE[2*nv];
		for (int i = 0; i < nv; i++)
		{
			rs[i] = new RSegment(v[i], v[Geometry.next(i,nv)]);
			// Set left/right end points.
			if (v[i].xless(v[Geometry.next(i,nv)]))
			{
				ep[i]	= new PointE(v[i], i, true);
				ep[nv+i]	= new PointE(v[Geometry.next(i,nv)], i, false);
			}
			else
			{
				ep[i]	= new PointE(v[Geometry.next(i,nv)], i, true);
				ep[nv+i]	= new PointE(v[i], i, false);
			}
		}

		// Print input array.
		System.out.println("Initial end point set");
		for (int i = 0; i < 2*nv; i++)
			System.out.println("ep[" + i + "]= " + ep[i]);
		System.out.println();

		// Sort S by x coordinate, and y is x's equal.
		sortByX(ep);

		// Print sorted array.
		System.out.println("Sorted end point set");
		for (int i = 0; i < 2*nv; i++)
			System.out.println("ep[" + i + "]= " + ep[i]);
		System.out.println();
	}

	public boolean isSimple()
	{
		PointE p;
		int sid;
		RSegment a, b, s;
		st = new RedBlackTree(new RSegmentSortTool());

		// Run line sweep
		for (int i = 0; i < 2*nv; i++)
		{
			p = ep[i];
			sid = p.getId();
			s = rs[sid];
			System.out.println(s
			);

			if (p.isLeft())
			{
				System.out.println("left.p= " + p.toString());
				st.add(s);
				a = (RSegment)st.findPredecessor(s);
				b = (RSegment)st.findSuccessor(s);
				if (a != null)
				{
					if (a.getp1() != s.getp1() )
					{
						IntersectNew ip = Geometry.SegSegIntNew(  a.getp1(), a.getp2(),
											s.getp1(), s.getp2() );
						int ic = ip.getcode();
						if (ic != 0)
							return false;
						else
							System.out.println("No as intersection for: " + a + "\n" + s);
					}
					/* else and s share starting left end point - ok */
				}

				if (b != null)
				{
					if (b.getp1() != s.getp1())		/* if not same left endpoint */
					{
						IntersectNew ip = Geometry.SegSegIntNew(  b.getp1(), b.getp2(),
											s.getp1(), s.getp2() );
						int ic = ip.getcode();
						if (ic != 0)
							return false;
						else
							System.out.println("No bs intersection for: " + b + "\n" + s);
					}
				}
				/* else b and s share starting left end point - ok */
			}
			else
			{
				System.out.println("right.p= " + p.toString());
				a = (RSegment)st.findPredecessor(s);
				b = (RSegment)st.findSuccessor(s);
				if (a != null && b != null)
				{
						IntersectNew ip = Geometry.SegSegIntNew(  a.getp1(), a.getp2(),
											b.getp1(), b.getp2() );
						int ic = ip.getcode();
						if (ic != 0)
							return false;
						else
							System.out.println("No ab intersection for: " + a + "\n" + b);
				}

				st.remove(s);
			}
		}
		return true;
	}

	/*
	 * Sort end points
	 */
	private void sortByX(PointE[] v)
	{
		Arrays.sort(v, new Comparator() {
			public int compare(Object a, Object b)
			{
				if ( ((PointE)a).equals((PointE)b) )
					return 0;
				else if ( ((PointE)a).xless((PointE)b) )
					return -1;
				else
					return 1;
			}
		});
	}
}