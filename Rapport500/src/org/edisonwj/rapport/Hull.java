package org.edisonwj.rapport;

/**
 * Hull class implements a class corresponding
 * to the convex hull (in counter clockwise order)
 * of a set of points in the plane.
 */

import java.awt.*;
import java.util.*;

public class Hull
{
	/**
	 * Array containing the points in the convex hull
	 * in counterclockwise order.
	 */
	private Pointd[] h;
	private ArrayList al;	/* Antipodal pairs */
	private ArrayList am;	/* Antipodal pairs at max distance */
	private double diameter;

	/**
	 * Class constructor with parameter being the set
	 * of points.
	 */
	public Hull(Pointd[] p)
	{
		Pointd[] v;
		int n;

		n = p.length;
		v = new Pointd[n];								/* Make a copy of the input array */
		System.arraycopy( p, 0, v, 0, n );
		Swap(v, 0, FindLowest(v));						/* Put rightmost lowest vertex first */
		Qsort(v);										/* Sort by increasing angle */
		n = Compress(v);								/* Remove collinear points */
		h = Graham(v, n);								/* Scan for hull */

		/* Create: */
//		al = antipodalPairs();							/*   list of anti-pairs */
//		am = null;										/*   anti-pairs at max dist */
//														/*   diameter */
//		Pair ap;
//		double dist = 0.0, maxdist =0.0;
//		for (int i = 0; i < al.size(); i++)
//		{
//			ap = (Pair)al.get(i);
//			dist = ap.getDist();
//			if ( maxdist < dist )
//			{
//				maxdist = dist;
//				am = new ArrayList();
//				am.add(ap);
//			}
//			else if ( maxdist == dist )
//				am.add(ap);
//		}
//		diameter = diameter();
//		if (diameter != maxdist)
//			System.out.println("Hull.diameter discrepancy - diameter= "
//								+ diameter + " maxdist= " + maxdist);
	}

	/**
	 * Class method returning number of points in the convex hull.
	 */
	public final int size()
	{
		return h.length;
	}

	/**
	 * Returns a String representation of the hull.
	 */
	 public String toString()
	 {
		String hullString = "";
		for (int i = 0; i < h.length; i++)
			hullString += ("h[" + i + "]= " + h[i] +"\n");
		return hullString;
	}

	/**
	 * Class method returning the convex hull as an array of points.
	 */
	public final Pointd[] getPoints()
	{
		return h;
	}

	/**
	 * Class method returning a specific point in the convex hull.
	 */
	public final Pointd getPoint(int i)
	{
		return h[i];
	}

	/**
	 * Returns twice the area of the hull formed by the array of vertices
	 */
 	public double computeArea2()
 	{
		double sum = 0;

		for (int i = 1; i <= h.length - 2; i++)
			sum += Geometry.twice_area(h[0], h[i], h[i+1]);

		return sum;
 	}

	/**
	 * Computes hull area
	 */
	public double computeArea()
	{
		return .5D * computeArea2();
	}

	/**
	 * Computes hull perimeter
	 */
	public double computePerimeter()
	{
		double per = 0.0D;

		for (int i = 0; i < h.length - 1; i++)
			per += Math.sqrt(Geometry.distance2(h[i], h[i+1]));
		per += Math.sqrt(Geometry.distance2(h[h.length-1], h[0]));
		return per;
	}

	/**
	 * Draw the hull.
	 */
	public void drawHull(Graphics g, RPanel rp)
	{
		int x, y;
		Pointd pt;

		if ( size() > 0 )
		{
			Polygon p = new Polygon();
			for (int i = 0; i < size(); i++)
			{
				pt = getPoint(i);
				x = rp.iX(pt.getx());
				y = rp.iY(pt.gety());
				p.addPoint(x, y);
			}
			Color c = g.getColor();
			g.setColor(Color.red);
			g.drawPolygon(p);

//			drawApairs(g, rp);

			g.setColor(c);
		}
	}

	public void drawApairs(Graphics g, RPanel rp)
	{
		Color saveC = g.getColor();

		Pair ap;
		for (int i = 0; i < al.size(); i++)
		{
			ap = (Pair)al.get(i);
			int x1 = rp.iX(ap.getp1().getx());
			int y1 = rp.iY(ap.getp1().gety());
			int x2 = rp.iX(ap.getp2().getx());
			int y2 = rp.iY(ap.getp2().gety());
			g.setColor(Color.yellow);
			g.drawLine(x1, y1, x2, y2);
		}

		for (int i = 0; i < am.size(); i++)
		{
			ap = (Pair)am.get(i);
			int x1 = rp.iX(ap.getp1().getx());
			int y1 = rp.iY(ap.getp1().gety());
			int x2 = rp.iX(ap.getp2().getx());
			int y2 = rp.iY(ap.getp2().gety());
			g.setColor(Color.magenta);
			g.drawLine(x1, y1, x2, y2);
		}

		g.setColor(saveC);
	}

	/**
	 * Private class method determining convex hull using Graham scan.
	 * See O'Rourke, pp. 77-87.
	 */
	private Pointd[] Graham(Pointd[] p, int np)
	{
		HullStack hs = new HullStack(np);
		hs.hullPush(p[0]);
		hs.hullPush(p[1]);
		int i = 2;
		while (i < np)
		{
			if ( hs.isHull(p[i]) )
				hs.hullPush(p[i++]);
			else
				hs.hullPop();
		}
		return hs.hullArray();
	}

	/**
	 * Private class method to find the lowest (starting) point for scan.
	 */
	private int FindLowest(Pointd[] v)
	{
		int low = 0;
		for (int i = 1; i < v.length; i++)
		{
			if ( (v[i].gety() < v[low].gety()) ||
				 ((v[i].gety() == v[low].gety()) && (v[i].getx() > v[low].getx())) )
				 low = i;
		}
		return low;
	}

	/**
	 * Private method to swap two points during hull construction.
	 */
	private void Swap(Pointd[] v, int i, int j)
	{
		if (i != j)
		{
			Pointd pt = v[i];
			v[i] = v[j];
			v[j] = pt;
		}
	}

	/**
	 * Quick sort method.
	 */
	private void Qsort(Pointd[] v)
	{
		Sort(v, 1, v.length-1);
	}

	/**
	 * Supporting sort method for quick sort.
	 */
	private void Sort(Pointd[] v, int first, int last)
	{
		int lo, hi;

		if (first >= last)
			return;

		Pointd piv = v[(first + last)/2];
		lo = first;
		hi = last;

		do
		{
			while (lo <= last  && compare(v, lo, piv) < 0)
				lo++;

			while (hi >= first && compare(v, hi, piv) > 0)
				hi--;

			if (lo <= hi)
			{
				if (compare(v, lo, hi) == 0)
				{
					lo++;
					hi--;
				}
				else
					Swap(v, lo, hi);
			}
		} while (lo <= hi);
		Sort(v, first, hi);
		Sort(v, lo, last);
	}

	/**
	 * Private class method to
	 */
	private int Compress(Pointd[] p)
	{
		int i, j;

		for (i = 1, j=1; i <= p.length - 2; i++)
		{
			if ( Geometry.area_sign(p[0], p[i], p[i+1]) != 0 )
				p[j++] = p[i];
		}
		p[j++] = p[i++];		/* Last point is always on the hull */

		for (i = j; i <= p.length - 1; i++)
			p[i] = null;

		return j;
	}

	/**
	 * Method for comparing two points within the hull array.
	 */
	private int compare(Pointd[] p, int i, int j)
	{
		int as = Geometry.area_sign(p[0], p[i], p[j]);
		if ( as > 0)		/* left turn  */
			return -1;
		else if (as < 0)	/* right turn */
			return 1;
		else 					/* collinear  */
		{
			double x = Math.abs(p[i].getx() - p[0].getx()) - Math.abs(p[j].getx() - p[0].getx());
			double y = Math.abs(p[i].gety() - p[0].gety()) - Math.abs(p[j].gety() - p[0].gety());
			if ( (x < 0) || (y < 0) )
				return -1;
			else if ( (x > 0) || (y > 0) )
				return 1;
			else		// points are coincident
				return 0;
		}
	}

	/**
	 * Method for comparing a point in the array with another specified point.
	 */
	private int compare(Pointd[] p, int i, Pointd m)
	{
		int as = Geometry.area_sign(p[0], p[i], m);
		if ( as > 0)		/* left turn  */
			return -1;
		else if (as < 0)	/* right turn */
			return 1;
		else 					/* collinear  */
		{
			double x = Math.abs(p[i].getx() - p[0].getx()) - Math.abs(m.getx() - p[0].getx());
			double y = Math.abs(p[i].gety() - p[0].gety()) - Math.abs(m.gety() - p[0].gety());
			if ( (x < 0) || (y < 0) )
				return -1;
			else if ( (x > 0) || (y > 0) )
				return 1;
			else		// points are coincident
				return 0;
		}
	}

	/**
	 * Method for finding antipodal pairs of the hull.
	 * Adopted from algorithm in Preparata and Shamos, p. 180.
	 * See also Pirzadeh, p. 12.
	 */
	public ArrayList antipodalPairs()
	{
		// Assumes input hull points are in
		// array h ordered counterclockwise.

		// Returns ArrayList of antipodal pairs w/ distances

		ArrayList al = new ArrayList();
		int p, p0, pn, q, q0;
		int n = h.length-1;

		p0 = 0;
		pn = n;

		p = pn;
		q = next(p);

		// Traverse hull counterclockwise to find point, q0,
		// furthest from pn-p0.

		while ( Geometry.twice_area(h[p], h[next(p)], h[next(q)]) >
				Geometry.twice_area(h[p], h[next(p)], h[q]) )
			q = next(q);
		q0 = q;

		// Move p counterclockwise from pn to q0
		// and q from q0 to pn, each change creating
		// an anitpodal pair.

		scan: while ( q != p0 )
		{
			p = next(p);
			al.add(new Pair(h[p], h[q]));

			while ( Geometry.twice_area(h[p], h[next(p)], h[next(q)]) >
					Geometry.twice_area(h[p], h[next(p)], h[q]) )
			{
//				System.out.println("p= " + p + " q= " + q);
				q = next(q);
				if ( !(p == q0 && q == p0) )
					al.add(new Pair(h[p], h[q]));
				else
					break scan;
			}

			// Handle parallel lines
//			System.out.println("Parallel lines");
			if ( Geometry.twice_area(h[p], h[next(p)], h[next(q)]) ==
			   	 Geometry.twice_area(h[p], h[next(p)], h[q]) )
				if ( !(p == q0 && q == pn) )
					al.add(new Pair(h[p], h[next(q)]));
				else
					al.add(new Pair(h[next(p)], h[q]));
		}
		return al;
	}

	/**
	 * Method for finding diameter of the convex hull
	 * Adopted from algorithm in Preparata and Shamos, p. 180.
	 */
	public double diameter()
	{
		// Assumes input hull points are in
		// array h ordered counterclockwise.

		// Returns ArrayList of antipodal pairs w/ distances

		int p, p0, pn, q, q0;
		int n = h.length-1;
		double dist = 0.0;
		double maxdist = 0.0;

		p0 = 0;
		pn = n;

		p = pn;
		q = next(p);

		// Traverse hull counterclockwise to find point, q0,
		// furthest from pn-p0.

		while ( Geometry.twice_area(h[p], h[next(p)], h[next(q)]) >
				Geometry.twice_area(h[p], h[next(p)], h[q]) )
			q = next(q);
		q0 = q;

		// Move p counterclockwise from pn to q0
		// and q from q0 to pn, each change creating
		// an anitpodal pair.

		scan: while ( q != p0 )
		{
			p = next(p);
			dist = Geometry.distance2(h[p], h[q]);
			if (maxdist < dist)
				maxdist = dist;

			while ( Geometry.twice_area(h[p], h[next(p)], h[next(q)]) >
					Geometry.twice_area(h[p], h[next(p)], h[q]) )
			{
				q = next(q);
				if ( !(p == q0 && q == p0) )
				{
					dist = Geometry.distance2(h[p], h[q]);
					if (maxdist < dist)
						maxdist = dist;
				}
				else
					break scan;
			}

			// Handle parallel lines

			if ( Geometry.twice_area(h[p], h[next(p)], h[next(q)]) ==
			   	 Geometry.twice_area(h[p], h[next(p)], h[q]) )
			{
				if ( !(p == q0 && q == pn) )
					dist = Geometry.distance2(h[p], h[next(q)]);
				else
					dist = Geometry.distance2(h[next(p)], h[q]);
				if (maxdist < dist)
					maxdist = dist;
			}
		}
		return Math.sqrt(maxdist);
	}

	/**
	 * Inner class defining antipodal pairs.
	 */
	private class Pair
	{
		Pointd p1;
		Pointd p2;
		double d;

		Pair(Pointd a, Pointd b)
		{
			p1 = a;
			p2 = b;
			d = Geometry.distance(a, b);
		}

		public Pointd getp1()
		{
			return p1;
		}

		public Pointd getp2()
		{
			return p2;
		}

		public double getDist()
		{
			return d;
		}

		public String toString()
		{
			return ("" + p1 + " : " + p2 + " Distance= " + d);
		}
	}

	/**
	 * Return next index cycling back to zero after n-1.
	 */
	private int next(int i)
	{
		return i == (h.length-1) ? 0 : i + 1;
	}

	/**
	 * Main method for testing class implementation.
	 * Test case from O'Rourke, pp. 85-86.
	 */
	public static void main(String [] args)
	{
		Pointd[] tv = new Pointd[19];
		tv[0] = new Pointd(3,3);
		tv[1] = new Pointd(3,5);
		tv[2] = new Pointd(0,1);
		tv[3] = new Pointd(2,5);
		tv[4] = new Pointd(-2,2);
		tv[5] = new Pointd(-3,2);
		tv[6] = new Pointd(6,5);
		tv[7] = new Pointd(-3,4);
		tv[8] = new Pointd(-5,2);
		tv[9] = new Pointd(-5,-1);
		tv[10] = new Pointd(1,-2);
		tv[11] = new Pointd(-3,-2);
		tv[12] = new Pointd(4,2);
		tv[13] = new Pointd(5,1);
		tv[14] = new Pointd(-5,1);
		tv[15] = new Pointd(3,-2);
		tv[16] = new Pointd(0,5);
		tv[17] = new Pointd(0,0);
		tv[18] = new Pointd(7,4);

		for (int i = 0; i < tv.length; i++)
			System.out.println("tv[" + i + "]= " + tv[i]);

		Hull htest = new Hull(tv);
		System.out.println("\nHull:\n" + htest.toString());

		double dist = 0.0, maxdist = 0.0;
		ArrayList al = htest.antipodalPairs();
		for (int i = 0; i < al.size(); i++)
		{
			System.out.println("Antipodal Pairs[" + i + "]= " + al.get(i));
			dist = ((Pair)al.get(i)).getDist();
			if ( maxdist < dist )
				maxdist = dist;
		}
		System.out.println("Antipodal Pairs max distance= " + maxdist);

		System.out.println("\nDiameter= " + htest.diameter());
	}
}