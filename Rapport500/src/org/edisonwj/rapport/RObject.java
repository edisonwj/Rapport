package org.edisonwj.rapport;

/**
 * RObject defines the abstract class covering all of the ojects
 * that Rapport generates.
 */

import java.awt.*;
import java.io.*;
import java.util.*;

public abstract class RObject implements RapportDefaults
{
	protected int type;						/* Object type */
	protected int nv;						/* Current number of components in object */
	protected Date timestamp= new Date();	/* Current date and time */
	protected RInfo ri;						/* Link to RInfo */

	protected static int count=0;			/* Id numbers for objects */

	protected double regionXmax = RXMAX;	/* Region x boundaries */
	protected double regionXmin = RXMIN;
	protected double regionYmax = RYMAX;	/* Region y boundaries */
	protected double regionYmin = RYMIN;

	public static synchronized int getId()
	{
		return count++;
	}

	public int getnv()
	{
		return nv;
	}

	public void setnv(int n)
	{
		nv = n;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int n)
	{
		type = n;
	}

	public Date getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(Date d)
	{
		timestamp = d;
	}
	
	public RInfo getRInfo() {
		return ri;
	}
	
	public String[] readTypeTitleParms(BufferedReader in) throws IOException
	{
		String[] sttp = new String[3];
		String s;
		StringTokenizer t;
		s = in.readLine();
		t = new StringTokenizer(s, "#= \t\n\r");
		if ( s.charAt(0) == '#' && "TYPE".equals(t.nextToken()) ) {
			sttp[0] = t.nextToken();
			sttp[1] = t.nextToken();
			s = in.readLine();
			t = new StringTokenizer(s, "#= \t\n\r");
			if ( s.charAt(0) == '#' && "PARMS".equals(t.nextToken()) ) {
				sttp[2] = s;
			}
		}
		else {
			System.out.println("RObject readTypeTitleParms - error reading input file");
			sttp = null;
		}
		return sttp;
	}
	
	public int readNV(BufferedReader in) throws IOException
	{
		String s;
		StringTokenizer t;
		int nv = 0;
		s = in.readLine();
		t = new StringTokenizer(s, "#= \t\n\r");
		if ( s.charAt(0) == '#' && "NV".equals(t.nextToken()) )
			nv = Integer.parseInt(t.nextToken());
		return nv;
	}

	public abstract void setVertex(Pointd p, int i);
	public abstract Pointd getVertex(int i);
	public abstract void add(Pointd p);
	public abstract void draw(Graphics g, RPanel rp);
	public abstract void drawDecomp(Graphics g, RPanel rp);
	public abstract void findHull();
	public abstract Hull getHull();
	public abstract void reSize();
	public abstract int size();
	public abstract int sizeDecomp();
	public abstract int sizeHull();
	public abstract void writeData(boolean draw3d, PrintWriter out) throws IOException;
	public abstract void readData(BufferedReader in) throws IOException;
	public abstract void scaleToUnitSquare();
	public abstract void drawSpecial(Graphics g, RPanel rp);
}