package org.edisonwj.rapport;

/**
 * HullStack class implements a stack data structure
 * customized to support hull construction.
 */

public class HullStack
{
	/**
	 * Variables implementing the stack array
	 * and count of points currently in the stack.
	 */
	Pointd[] hstack;
	int count;

	/**
	 * Constructor for the HullStack class with parameter
	 * specifying maximum number of points possibly in the stack.
	 */
	HullStack(int n)
	{
		hstack = new Pointd[n];
		count = 0;
	}

	/**
	 * Push a point onto the stack.
	 */
	public void hullPush(Pointd p)
	{
		hstack[count++] = p;
	}

	/**
	 * Pop a point from the top of the stack.
	 */
	public Pointd hullPop()
	{
		return hstack[count--];
	}

	/**
	 * Return true if the top two points in the stack and the
	 * specified third point form a left turn proceeding
	 * clockwise around the hull.
	 */
	public boolean isHull(Pointd p)
	{
		return Geometry.left(hstack[count-2], hstack[count-1], p);
	}

	/**
	 * Return a string representation of the hull stack.
	 */
	public String toString()
	{
		String result = "";
		for (int i = 0; i < count; i++)
			result += "s[" + i + "] = " + hstack[i] + "\n";
		return result;
	}

	/**
	 * Return the contents of the stack as an array of points.
	 */
	public Pointd[] hullArray()
	{
		Pointd[] h = new Pointd[count];
		System.arraycopy(hstack, 0, h, 0, count);
		return h;
	}
}
