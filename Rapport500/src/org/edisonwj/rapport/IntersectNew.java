package org.edisonwj.rapport;

/*
 * IntersectNew supports line intersect detection of various sorts
 */

public class IntersectNew
{
	Pointd p;
	Pointd q;
	int code;

	//	code == 0	no intersection
	//	code == 1	proper intersection
	//	code == 2	end point of one segment is on the other, not collinear
	//	code == 10+	segments collinearly overlap

	public IntersectNew(Pointd p, Pointd q, int i)
	{
		this.p = p;
		this.q = q;
		this.code = i;
	}

	public Pointd getp()
	{
		return p;
	}

	public Pointd getq()
	{
		return q;
	}

	public int getcode()
	{
		return code;
	}

	public String toString()
	{
		return (code + " " + p + " " + q);
	}
}