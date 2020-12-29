package org.edisonwj.rapport;

/* SweepStatus defines the object containing current status of the line sweep.
 * Entries identify line segments that intersect the sweep line.
 * The ordered set of all line segments currently intersecting the sweep
 * line is the status of the sweep.
 */

import java.awt.*;

public class SweepStatus implements RapportDefaults
{
	private Pointd p;
	private RSegmentSet ss;

	public SweepStatus(Pointd p, RSegmentSet ss)
	{
		this.p = p;
		this.ss = ss;
	}

	public SweepStatus(Pointd p)
	{
		this(p, null);
	}

	public Pointd getPoint()
	{
		return p;
	}

	public RSegmentSet getSegmentSet()
	{
		return ss;
	}

	public String toString()
	{
		return ("SweepStatus - point:" + p + " segment set: " + ss);
	}

	public void draw(Graphics g, RPanel rp, Color c)
	{
		Color saveC = g.getColor();
		if (c != null)
			g.setColor(c);

		p.draw(g, rp);
		ss.draw(g, rp);

		g.setColor(saveC);
	}

	public void draw(Graphics g, RPanel rp)
	{
		draw(g, rp, null);
	}

}