package org.edisonwj.rapport;

/**
 * EventPoint defines an event object for the sweep line algorithm.
 * RSegmentSet ss contains all line segments whose upper end point
 * is the event point p.
 */  

import java.awt.*;

public class EventPoint implements RapportDefaults
{
	private int type;	/* 0 = upper endpoint */
						/* 1 = lower endpoint */
						/* 2 = intersection   */
	private Pointd p;
	private RSegmentSet ss;

	protected RapportException illegal_state = new RapportException("illegal state");


	public EventPoint(Pointd p, RSegment s)
	{
		this.p = p;

		Pointd p1 = s.getp1();
		Pointd p2 = s.getp2();

		if (p.equals(p1) || p.equals(p2))
			if ( (p.equals(p1) && p1.before(p2)) ||
				 (p.equals(p2) && p2.before(p1)) )
			{
				this.type = 0;
				this.ss = new RSegmentSet(s);
			}
			else
			{
				this.type = 1;
				this.ss = new RSegmentSet(s);
			}
		else
			throw illegal_state;
	}

	public EventPoint(Pointd p, RSegmentSet ss)
	{
		this(0, p, ss);
	}

	public EventPoint(Pointd p)
	{
		this.type = 1;
		this.p = p;
		this.ss = null;
	}

	public EventPoint(int type, Pointd p, RSegmentSet ss)
	{
		if (type == 2)
		{
//			System.out.println("EventPoint constructor: " + type + " " + p + " " + ss);
			this.type = type;
			this.p = p;
			this.ss = ss;
		}
		else if (type == 0)
		{
			for (int i = 0; i < ss.size(); i++)
				if ( p != (ss.get(i)).getp1() )
					throw illegal_state;
			this.type = type;
			this.p = p;
			this.ss = ss;
		}
		else
			throw illegal_state;

	}

//	public EventPoint()
//	{
//	}

	public int getType()
	{
		return type;
	}

	public Pointd getPoint()
	{
		return p;
	}

	public RSegmentSet getSegmentSet()
	{
		return ss;
	}

	public void setPoint(Pointd p)
	{
		this.p = p;
	}

	public void addSegment(RSegment s)
	{
		this.ss.add(s);
	}

	public String toString()
	{
		return "" + type + " " + p + " " + ss;
	}

	public void draw(Graphics g, RPanel rp, Color c)
	{
		Color saveC = g.getColor();
		if (c != null)
			g.setColor(c);

		p.draw(g, rp);
		if (ss != null)
			ss.draw(g, rp);

		g.setColor(saveC);
	}

	public void draw(Graphics g, RPanel rp)
	{
		draw(g, rp, null);
	}

	public int compareTo(EventPoint ep)
	{
		Pointd q = ep.getPoint();
		if ( p.equals(q) )
			return 0;
		else if ( p.before(q) )
			return -1;
		else
			return 1;
	}

	public EventPoint merge(EventPoint ep)
	{
		Pointd q = ep.getPoint();
		if ( !p.equals(q) )
			return null;
		else
		{
			ss.addAll( ep.getSegmentSet().getAll() );
			return (new EventPoint(p, ss));
		}
	}

}