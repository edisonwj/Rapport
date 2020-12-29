package org.edisonwj.rapport;

/**
 * RCell defines the class specifying an individual cell in a cell polygon
 */

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

class RCell implements RapportDefaults
{
	private int row;			/* Cell row number */
	private int col;			/* Cell column number */
	private int pn;				/* Polygon number */
	private byte neighbors;		/* Neighbor cell available indicators */
	private byte interior;		/* Interior edge indicator */

	private static final int[][] states;	/* Neighbor presence states */

	static
	{
		states = new int[16][];
		states[15] = new int[] {0};
		states[14] = new int[] {1, 0};
		states[13] = new int[] {1, 1};
		states[11] = new int[] {1, 2};
		states[7]  = new int[] {1, 3};
		states[12] = new int[] {2, 0, 1};
		states[10] = new int[] {2, 0, 2};
		states[6]  = new int[] {2, 0, 3};
		states[9]  = new int[] {2, 1, 2};
		states[5]  = new int[] {2, 1, 3};
		states[3]  = new int[] {2, 2, 3};
		states[8]  = new int[] {3, 0, 1, 2};
		states[4]  = new int[] {3, 0, 1, 3};
		states[2]  = new int[] {3, 0, 2, 3};
		states[1]  = new int[] {3, 1, 2, 3};
		states[0]  = new int[] {4, 0, 1, 2, 3};
	}

	public RCell(int r, int c)
	{
		row = r;
		col = c;
		pn = -1;
		neighbors = 0;
//		System.out.println("RCell constructor: " + r + " " + c + " " + pn + " " + neighbors);
	}

	public int getRow()
	{
		return row;
	}

	public int getCol()
	{
		return col;
	}

	public void setPn(int pn)
	{
		this.pn = pn;
	}

	public int getPn()
	{
		return pn;
	}

	public void setNeighbors(int in)
	{
//		System.out.println("RCell setNeighbor - r, c: " + row + ", " + col
//							+ ", in: " + in
//							+ ", neighbors: " + neighbors);
		if ( in > ALL_NEIGHBORS )
			System.out.println("RCell index error");
		else
		{
			byte mask = 1;
			mask = (byte)(mask << in);
			neighbors = (byte)(neighbors | mask);
		}
//		System.out.println("RCell setNeighbor - r, c: " + row + ", " + col
//							+ ", in: " + in
//							+ ", neighbors: " + neighbors);
	}

	public byte getNeighbors()
	{
		return neighbors;
	}

	public void setInterior(int in)
	{
		if ( in > ALL_NEIGHBORS )
			System.out.println("RCell index error");
		else
		{
			byte mask = 1;
			mask = (byte)(mask << in);
			interior = (byte)(interior | mask);
		}
//		System.out.println("RCell setInterior - r, c: " + row + ", " + col
//							+ ", in: " + in
//							+ ", interior: " + interior);
	}

	public byte getInterior()
	{
		return interior;
	}

	public boolean isInterior(int in)
	{
		byte mask = 1;
		mask = (byte)(mask << in);
		if ( (byte)(interior & mask) != 0 )
			return true;
		else
			return false;
	}

	public boolean available()
	{
		if (neighbors == ALL_NEIGHBORS)
			return false;
		else
			return true;
	}

	public int select(Rand mr, int method)
	{
		int ix;
		int num = states[neighbors][0];

		ix = 1;
		if ( num > 1 )
		{
//			ix = mr.uniform(1, num);

			switch (method)
			{
				case 1:	/* Uniform */
				ix = mr.uniform(1,num);	/* get cell index */
				break;

				case 2: /* Exponential */
				ix = (int)mr.exponential((double)1,(double)(num+1));	/* get cell index */
				break;

				case 3: /* Mixture */
				if (mr.uniform() > .5)
					ix = mr.uniform(1,num);	/* get cell index */
				else
					ix = (int)mr.exponential((double)1,(double)(num+1));	/* get cell index */
				break;
			}
		}

		return states[neighbors][ix];
	}

	public String toString()
	{
		return "RCell (" + row + ", "+ col + "), " + pn + ", " + neighbors + ", " + interior;
	}

}