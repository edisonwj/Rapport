package org.edisonwj.rapport;

/**
 * SweepItem defines a line segment during sweep processing. 
 * Entries identify line segments that intersect the sweep line.
 * The ordered set of all line segments currently intersecting the sweep
 * line is the status of the sweep.
 */

import java.awt.*;

public class SweepItem implements RapportDefaults
{
	private RSegment s;
	private SweepTree t;
	private long id;

	private static long count=0;
	private static Object synchO = new Object();

	protected RapportException illegal_state = new RapportException("illegal state");

	public SweepItem(RSegment s, SweepTree t)
	{
		this.s = s;
		this.t = t;
		synchronized(synchO)
		{
			id = count++;
		}
	}

	public RSegment getSegment()
	{
		return s;
	}

	public SweepTree getTree()
	{
		return t;
	}

	public long getId()
	{
		return id;
	}

	public String toString()
	{
		return ("SweepItem - segment: " + s);
	}

	public void draw(Graphics g, RPanel rp, Color c)
	{
		Color saveC = g.getColor();
		if (c != null)
			g.setColor(c);

		s.draw(g, rp);

		g.setColor(saveC);
	}

	public void draw(Graphics g, RPanel rp)
	{
		draw(g, rp, null);
	}

	public boolean is_trivial()
	{
		Pointd psweep = t.getPsweep();
		if ( psweep.equals(s.getp1()) && psweep.equals(s.getp2()) )
			return true;
		else
			return false;
	}

	public boolean equals(SweepItem si)
	{
		if (s.equals(si.getSegment()) && t == si.getTree())
			return true;
		else
			return false;
	}

	public int compareTo(SweepItem si)
	{
		int result;

		Pointd psweep = t.getPsweep();

		if (this.equals(si))
			return 0;

		result = 0;

//		System.out.println("compareTo psweep: " + psweep);
//		System.out.println("compareTo this: " + this);
//		System.out.println("CompareTo si:   " + si);

		if (psweep.equals(s.getp1()))
		{
			result = Geometry.area_sign(si.getSegment().getp1(), si.getSegment().getp2(), psweep);
//			System.out.println("Geometry.area_sign(si.getSegment().getp1(), si.getSegment().getp2(), psweep)= " + result);

		}
		else if (psweep.equals(si.getSegment().getp1()))
		{
			result = Geometry.area_sign(s.getp1(), s.getp2(), psweep);
//			System.out.println("Geometry.area_sign(s.getp1(), s.getp2(), psweep)= " + result);
		}

//		else
//			throw illegal_state;

		if (result != 0 || this.is_trivial() || si.is_trivial())
		{
//			System.out.println("compareTo result: " + result);
			return result;
		}

//NOT	result = Geometry.area_sign(si.getSegment().getp1(), si.getSegment().getp2(), s.getp1());
//above failed at (.73,.27) for (0,1)-(1,0) against si of (.6,1)-(.6,0)
//NOT	result = Geometry.area_sign(si.getSegment().getp1(), si.getSegment().getp2(), s.getp2());
//above failed at (.23,.77) for (0,1)-(1,0) against si of (.2,.9)-(.5,.05)
		result = Geometry.area_sign(si.getSegment().getp1(), si.getSegment().getp2(), psweep);
//		System.out.println("Geometry.area_sign(si.getSegment().getp1(), si.getSegment().getp2(), psweep)= " + result);
		if (result == 0)
		{
//NOT		result = Geometry.area_sign(s.getp2(), psweep, si.getSegment().getp2() );
			result = Geometry.area_sign(si.getSegment().getp1(), psweep, s.getp1() );
//			System.out.println("Geometry.area_sign(si.getSegment().getp1(), psweep, s.getp1())= " + result);
		}
//		result = Geometry.area_sign(s.getp1(), psweep, si.getSegment().getp2());
//		System.out.println("compareTo result: " + result);

		if (result != 0)
			return result;
		else if (id < si.getId())
			return -1;
		else if (id > si.getId())
			return 1;
		else
			return 0;
	}

}