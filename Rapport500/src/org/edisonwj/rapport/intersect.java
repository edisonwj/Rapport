package org.edisonwj.rapport;

/*
 * Intersect supports line intersect processing
 */

public class intersect
{
	Pointd p;
	char code;

	public intersect(Pointd p, char c)
	{
		this.p = p;
		this.code = c;
	}

	public Pointd getPoint()
	{
		return p;
	}

	public char getCode()
	{
		return code;
	}

	public String toString()
	{
		return (code + " " + p);
	}
}