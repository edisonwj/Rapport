package org.edisonwj.rapport;

/**
 * Generate line field within the unit square about origin.
 * Modified algorithm from Stoyan and Stoyan, pp. 357-358.
 * Fractals, Random Shapes, and Point Fields, 1994.
 */ 

import java.awt.*;
import java.io.*;
import java.text.*;

class RLineField extends RObject implements RapportDefaults
{
	private RPointp[] pa;
	protected lfInfo ri;					/* Link to RInfo */

	public RLineField()
	{
		type = TYPE_LINEFIELD;				/* Poisson Line Field */
	}
	
	public RLineField(int num_lines)
	{
		this();								/* Poisson Line Field */
		pa = new RPointp [num_lines];		/* Lines represented as points */
		nv = 0;								/* Number of lines actually generated */
	}
	
	public RLineField(int num_lines, Rand mr)
	{
		this(num_lines);
		genLines(num_lines, mr);
	}
	
	public RLineField(lfInfo ri, Rand mr)
	{
		this(ri.num_items);
		this.ri = ri;
		genLines(ri.num_items, mr);
	}

	public RLineField(RPointp[] ipa)
	{
		this();
		pa = ipa;
		nv = ipa.length;
	}

	public RLineField(BufferedReader in) throws IOException
	{
		this();
//		System.out.println("RLineField(Buffered Reader) this: " + this.hashCode());
		String[] sttp;
		String stitle;
		sttp = readTypeTitleParms(in);
		type = Integer.parseInt(sttp[0]);
		stitle = sttp[1];
		nv = readNV(in);
		if (nv > 0)
		{
			pa = new RPointp[nv];
			readData(in);
			ri = new lfInfo();
			ri.title = stitle;

//			System.out.println("RLineField this: " + this.hashCode());
//			System.out.println("RLineField ri: " + ri.hashCode() + ", ri.title: " + ri.title);
		}
		else
		{
			System.out.println("RLineField: Error constructing point distribution from file input");
			nv = 0;
		}
	}
	
	public void readData(BufferedReader in) throws IOException
	{
//		System.out.println("RLineField readData");
		String s;
		for (int i=0; i < nv; i++)
		{
			s = in.readLine();
			pa[i] = new RPointp(s);
		}
	}	

		
	private void genLines(int nl, Rand mr)
	{
//		System.out.println("Create Poisson line field");
		double u, v, p, phi, x, y, t;
		double sqr2 = Math.sqrt(2);
		double sqr2i = 1.0/sqr2;

		while ( nv < nl )
	  	{
			u = mr.uniform();
			v = mr.uniform();
			p = u/sqr2;
			phi = 2.0 * Math.PI * v;
			x = p * Math.cos(phi);
			y = p * Math.sin(phi);
			t = sqr2i * ( Math.abs(Math.sin(phi)) + Math.abs(Math.cos(phi)) );

			if ( u > sqr2i && u > t )
				continue;
			else
				pa[nv++] = new RPointp(p, phi, 1);
		}
	}

	public void reSize()
	{
		RPointp[] nw = new RPointp[nv];
		System.arraycopy( pa, 0, nw, 0, nv );
		pa = nw;
	}

	public void add(RPointp p)
	{
		pa[nv++] = p;
	}

	public void set(RPointp p, int i)
	{
		pa[i] = p;
	}

	public RPointp get(int i)
	{
		return pa[i];
	}

	public int size()
	{
		return pa.length;
	}

	public void draw(Graphics g, RPanel rp)
	{
		/* Draw unit square between x:[-.5, .5] and y:[-.5. .5] */
		PolygonA s = new PolygonA( new Pointd[]
						{   new Pointd( .5,  .5),
							new Pointd(-.5,  .5),
							new Pointd(-.5, -.5),
							new Pointd( .5, -.5) } );
		s.draw(g, rp, Color.gray);

		/* Draw circle centered at origin with radius sqrt(2) */
		RCircle c1 = new RCircle( 0.0, 0.0, 1.0/Math.sqrt(2) );
		c1.draw(g, rp, Color.gray);

		/* Draw circle centered at origin with radius .5 */
		RCircle c2 = new RCircle( 0.0, 0.0, 0.5 );
		c2.draw(g, rp, Color.gray);

		/* Draw line points */
		for (int i = 0; i < nv; i++)
			pa[i].draw(g, rp);
	}

	public void writeData(boolean draw3d, PrintWriter out) throws IOException
	{
//		System.out.println("RLineField writeData type: " + type);
		
		String lineOut;
		String parmsOut;
		
		if (ri != null) {		
			parmsOut = "Number-of-lines = " + ri.Snum_items;
		}
		else
			parmsOut = "Parameters unknown";
		
		/* Create output for Draw3D */
		if (draw3d) {
			lineOut = "DataGroup:";
			out.println(lineOut);
			lineOut = "Title1: " + ri.title;
			out.println(lineOut);
			lineOut = "Title2: " + parmsOut;
			out.println(lineOut);

			for (int i = 0; i < nv; i++)
				pa[i].writeData(draw3d, out);
		}
		else {		
			NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMaximumFractionDigits(DEFAULT_MAX_FRACTION_DIGITS);
			nf.setMinimumFractionDigits(DEFAULT_MIN_FRACTION_DIGITS);
			nf.setMinimumIntegerDigits(DEFAULT_MIN_INTEGER_DIGITS);
	
			out.println("#TYPE=" + type + " " + ri.title);
			out.println("#PARMS=" + parmsOut);
			out.println("#NV=" + nv);
			for (int i = 0; i < nv; i++)
				pa[i].writeData(draw3d, out);
		}
	}

	public void add(Pointd p)
	{
	}

	public void setVertex(Pointd p, int i)
	{
	}

	public Pointd getVertex(int i)
	{
		return null;
	}

	public void findHull()
	{
	}

	public int sizeHull()
	{
		return 0;
	}

	public void drawHull(Graphics g, RPanel rp)
	{
	}

	public int sizeDecomp()
	{
		return 0;
	}

	public void drawDecomp(Graphics g, RPanel rp)
	{
	}

	public void drawSpecial(Graphics g, RPanel rp)
	{
	}

	public void scaleToUnitSquare()
	{
	}

	public Hull getHull()
	{
		return null;
	}
	
	public lfInfo getRInfo() {
		return ri;
	}
}
