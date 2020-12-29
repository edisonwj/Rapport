package org.edisonwj.rapport;

/**
 * PolyganA is the primary polygon definition class
 * and contains methods for polygon processing.
 */

import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;

class PolygonA extends RObject implements RapportDefaults
{
	public class PolygonAException
		extends RuntimeException
	{
		public PolygonAException()
		{
			super();
		}

		public PolygonAException(String s)
		{
			super(s);
		}
	}

	// PolygonA Exceptions
	private PolygonAException erroneous_state1 =
		new PolygonAException("erroneous state1");
	private PolygonAException erroneous_state2 =
		new PolygonAException("erroneous state2");
	private PolygonAException erroneous_shape =
		new PolygonAException("erroneous shape");


	private static final double epsilon = DEFAULTEPSILON;
	private static final boolean exact_comp = EXACT_COMP;
	private static final int debug = DEBUG;

	private static final String[] shapes = {"P", "T", "C", "M", "*", "S", "N", "R", ""};
								/* P = simple polygon, T = triangle, C = convex  */
								/* M = monotone, * = star, S = spiral, N = other, R = rectilinear */
	protected Pointd[] v;		/* Array of points (vertices); number is given by v.length */
	protected Hull h;			/* Convex hull for the polygon */
	protected Triangle[] tr;	/* Triangulation */
	protected long seedColor;	/* Seed for triangle color generation */
	protected Color pcolor;		/* Polygon color */
	protected String xcolor;	/* Hex color for Draw3D */
	protected RInfo ri;			/* Link to RInfo */

// Polygon metrics
	protected double[] xang;	/* External angles */
	protected double[] pang;	/* Polar angles */
	protected double[] rang;	/* Relativized polar angles */
	protected WedgeList wl;		/* WedgeList for polygon */

	protected boolean convex;
	protected boolean ccw;

	protected boolean[] shapeb = new boolean[shapes.length];
	protected String shape;
	protected Pointd cg;

	protected double area;
	protected double hullArea;
	protected double areaRatio;
	protected double perimeter;
	protected double hullPerimeter;
	protected double perRatio;
	protected double compacity;
	protected double pdRatio;

	protected int intercnt;
	protected long notches;
	protected double notches_norm;
	protected double notches_squared, notches_quad;
	protected double freq;
	protected double ampl;
	protected double conv;
	protected double complexity;
	protected double simplexity;

	protected long[] df;

	public PolygonA()
	{
		type = TYPE_POLYGON;
		nv = 0;
	}
	public PolygonA(int n)
	{
		this();
		v = new Pointd[n];
	}

	public PolygonA(int n, Rand mr)
	{
//		System.out.println("PolygonA constructor with initial triangle");
		this();
		v = new Pointd[n];

		Pointd pa, pb, pc;

		/* Get first point */
		pa = new Pointd(mr.uniform(), mr.uniform());
//		System.out.println("PolygonA point 0: " + pa);

		/* Get unique second point */
		do
		{
			pb = new Pointd(mr.uniform(), mr.uniform());
		} while (pa.equals(pb));

//		} while (!pa.isDistinct(pb));
//		System.out.println("PolygonA point 1: " + pb);

		/* Get unique and non-collinear third point */
		do
		{
			pc = new Pointd(mr.uniform(), mr.uniform());
		} while (pa.equals(pc) ||
				 pb.equals(pc) ||
				 Geometry.collinear(pa, pb, pc));

//		} while (!pa.isDistinct(pc) ||
//				 !pb.isDistinct(pc) ||
//				 Geometry.collinear(pa, pb, pc));
//		System.out.println("PolygonA point 2: " + pc);

		add(pa);
		add(pb);
		add(pc);

		if (!Ccw())
			Reverse();

//		for (int i = 0; i < 3; i++)
//			System.out.println("PolygonA point " + i + ": " + v[i]);
	}

	public PolygonA(Pointd[] p, boolean test)
	{
		this();
		v = new Pointd[p.length];
		System.arraycopy( p, 0, v, 0, p.length );
		nv = v.length;
		if (test && !checkPolygon())
			System.out.println("PolygonA: Invalid polygon");
	}
	
	public PolygonA(Pointd[] p, boolean test, int type)
	{
		this.type = type;
		v = new Pointd[p.length];
		System.arraycopy( p, 0, v, 0, p.length );
		nv = v.length;
		
		if (test && !checkPolygon())
			System.out.println("PolygonA: Invalid polygon");
	}

	public PolygonA(Pointd[] p)
	{
		this();
		v = new Pointd[p.length];
		System.arraycopy( p, 0, v, 0, p.length );
		nv = v.length;
		if (!checkPolygon())
			System.out.println("PolygonA: Invalid polygon");
	}

	public PolygonA(Pointd[] p, int type)
	{
		this.type = type;
		v = new Pointd[p.length];
		System.arraycopy( p, 0, v, 0, p.length );
		nv = v.length;
		if (!checkPolygon())
			System.out.println("PolygonA: Invalid polygon");
	}

	public PolygonA(ArrayList al)
	{
		this();
		nv = al.size();
		v = new Pointd[nv];
		for (int i = 0; i < nv; i++)
			v[i] = (Pointd)al.get(i);
		if (!checkPolygon())
			System.out.println("PolygonA: Invalid polygon");
	}
	
	public PolygonA(ArrayList al, int type)
	{
		this.type = type;
		nv = al.size();
		v = new Pointd[nv];
		for (int i = 0; i < nv; i++)
			v[i] = (Pointd)al.get(i);
		if (!checkPolygon())
			System.out.println("PolygonA: Invalid polygon");
	}

	public PolygonA(int type, BufferedReader in) throws IOException
	{
//		System.out.println("PolygonA(BufferedReader) constructor");

		String[] sttp;
		String stitle;
		sttp = readTypeTitleParms(in);
		this.type = Integer.parseInt(sttp[0]);
		stitle = sttp[1];
		pcolor = makeColor(sttp[3]);
		nv = readNV(in);
		v = new Pointd[nv];
		readData(in);
		ri = new fiInfo(v.length);
		ri.title = stitle;
//		System.out.println("PolygonA(BufferedReader) type: " + type + ", ri.title: " + ri.title);
		
		if (!checkPolygon())
		{
			v = null;
			nv = 0;
			System.out.println("PolygonA(BufferedReader): Invalid polygon");
		}
	}
	
	public Color makeColor(String s) 
	{
		Color color;
		if (!s.equals("None" )) {
			int r = Integer.parseInt(s.substring(2,4), 16);
			int g = Integer.parseInt(s.substring(4,6), 16);
			int b = Integer.parseInt(s.substring(6,8), 16);
			color = new Color(r, g, b);
		}
		else
			color = null;
		return color;
	}

	public Pointd[] rotate(Pointd[] iv, double phi)
	{
		Pointd[] newv = new Pointd[iv.length];
		double x,y;
		double c = Math.cos(phi);
		double s = Math.sin(phi);
		double r11 = c;
		double r12 = s;
		double r21 = -s;
		double r22 = c;

		for (int i = 0; i < iv.length; i++)
		{
			x = r11*iv[i].getx() + r21*iv[i].gety();
			y = r12*iv[i].getx() + r22*iv[i].gety();
			newv[i] = new Pointd(x,y);
		}
		return newv;
	}

	public PolygonA rotate(double phi)
	{

		PolygonA pa = new PolygonA(nv);
		double x,y;
		double c = Math.cos(phi);
		double s = Math.sin(phi);
		double r11 = c;
		double r12 = s;
		double r21 = -s;
		double r22 = c;

		for (int i = 0; i < nv; i++)
		{
			x = r11*v[i].getx() + r21*v[i].gety();
			y = r12*v[i].getx() + r22*v[i].gety();
			pa.add(new Pointd(x,y));
		}
		return pa;
	}

	public void firstThree(Rand mr)
	{
		Pointd pa, pb, pc;

		/* Get first point */
		pa = new Pointd(mr.uniform(), mr.uniform());
		//	System.out.println("PolygonA point 0: " + pa);

		/* Get unique second point */
		do
		{
			pb = new Pointd(mr.uniform(), mr.uniform());
		} while (pa.equals(pb));
		//	System.out.println("PolygonA point 1: " + pb);

		/* Get unique and non-collinear third point */
		do
		{
			pc = new Pointd(mr.uniform(), mr.uniform());
		} while (pa.equals(pc) ||
				 pb.equals(pc) ||
				 Geometry.collinear(pa, pb, pc));
		//	System.out.println("PolygonA point 2: " + pc);

		add(pa);
		add(pb);
		add(pc);

		if (!Ccw())
			Reverse();
	}

	public int vSize()
	{
		return v.length;
	}

	public int size()
	{
//		return v.length;
		return nv;
	}

	public void reSize()
	{
		Pointd[] u = new Pointd[nv];
		System.arraycopy( v, 0, u, 0, nv );
		v = u;
	}

	public void reSize(int n)
	{
		nv = n;
		this.reSize();
	}

	public void set(Pointd[] v)
	{
		this.v = v;
		nv = v.length;
	}

	public void setVertex(Pointd p, int i)
	{
  		if (i < v.length)
			v[i] = p;
		else
			System.out.println("setVertex index exception");
	}

	public Pointd getVertex(int i)
	{
		if (i < v.length)
			return v[i];
		else
		{
			System.out.println("getVertex index exception");
			return null;
		}
	}

	public void add(Pointd p)
	{
		if (nv < v.length)
		{
			v[nv++] = p;
		}
		else
		{
			System.out.println("addVertex index exception");
		}
	}


	public int addVertex(Pointd p)
	{
		if (nv < v.length)
		{
			v[nv++] = p;
			return nv-1;
		}
		else
		{
			System.out.println("addVertex index exception");
			return -1;
		}
	}

	public EdgeR getEdge(int i, int j)
	{
		EdgeR edge = new EdgeR(v[i], v[j]);
		return new EdgeR(v[i], v[j]);
	}

	public boolean[] getShapeb()
	{
		return shapeb;
	}

	public String getShape()
	{
		String result = "";
		for (int i = 1; i < shapeb.length; i++)
		{
//			System.out.println("PolygonA shapeb[" + i + "]= " + shapeb[i] + " shapes[i]= " + shapes[i]);
			if (shapeb[i])
				result += shapes[i];
		}
		return result;
	}

	public int getwlsize()
	{
		if (shapeb[2])
			return 1;
		else
			return wl.size();
	}

	public double getArea()
	{
		if (area == 0.0)
			area = computeArea();
		return area;
	}

	public double getPerimeter()
	{
		if (perimeter == 0.0)
			perimeter = computePerimeter();
		return perimeter;
	}

	public int getIntercnt()
	{
		if (intercnt == 0)
			intercnt = computeIntercnt();
		return intercnt;
	}

	public long[] getDf()
	{
		return df;
	}

	public int computeIntercnt()
	{
		int i, j;
//		System.out.println("Start compute intercnt");
		intercnt = 0;
		if (cg == null)
			cg = computeCG();
		Pointd hleft	= new Pointd(cg.getx()-10.0, cg.gety());
		Pointd hright	= new Pointd(cg.getx()+10.0, cg.gety());
		Pointd vlow		= new Pointd(cg.getx(), cg.gety()-10.0);
		Pointd vhi		= new Pointd(cg.getx(), cg.gety()+10.0);
		for (i = 0; i < nv ; i++)
		{
			j = Geometry.next(i, nv);
			if ( (Geometry.SegSegInt(v[i], v[j], hleft, hright)).getCode() != '0' )
				intercnt++;
			if ( (Geometry.SegSegInt(v[i], v[j], vlow, vhi)).getCode() != '0' )
				intercnt++;
//			System.out.println( "Count: " + intercnt + " " + i + " " + j +
//								" Edge: " + new EdgeR(v[i],v[j]) );
		}
//		System.out.println("End compute intercnt");
		return intercnt;
	}


	public double getCompacity()
	{
		if (compacity == 0.0)
			compacity = computeCompacity();
		return compacity;
	}

	public double getComplexity()
	{
		if (complexity == 0.0)
			complexity = computeComplexity();
		return complexity;
	}

	public Pointd getCG()
	{
		if (cg == null)
			cg = computeCG();
		return cg;
	}
	
	public void setColor(Color pcolor) {
		this.pcolor = pcolor;
	}
	
	public Color getColor() {
		return pcolor;
	}
	
	public void setXColor(String xcolor) {
		this.xcolor = xcolor;
	}
	
	public String getXColor() {
		return xcolor;
	}

	public String toString()
	{
		String result = "";
		for (int i = 0; i < nv; i++)
			result += "v[" + i + "]= " + v[i].toString() + "\n" ;
		return result;
	}

	public void draw(Graphics g, RPanel rp, Color c, boolean fill)
	{
		boolean fl = fill;
		int x, y;

		if ( nv > 0 )
		{
			Color saveC = g.getColor();
			if (c != null)
				g.setColor(c);
			else if (pcolor != null) {
				g.setColor(pcolor);
				fl = true;
			}

			Polygon p = new Polygon();
			for (int i = 0; i < nv; i++)
			{
				x = rp.iX(v[i].getx());
				y = rp.iY(v[i].gety());
				p.addPoint(x, y);
				g.fillOval(x-2, y-2, 5, 5);
			}
			g.drawPolygon(p);
			if (fl) {
				g.fillPolygon(p);
			}

			// Draw center of gravity, cg
//			g.setColor(Color.black);
//			cg = computeCG();
//			int cx = rp.iX(cg.getx());
//			int cy = rp.iY(cg.gety());
//			System.out.println("PolygonA draw cg: " + cg.toString());
//			g.fillOval(rp.iX(cx)-2, rp.iY(cy)-2, 10, 10);
//			
			// Draw chords from cg to vertices
//			for (int i = 0; i < nv; i++)
//			{
//				x = rp.iX(v[i].getx());
//				y = rp.iY(v[i].gety());
//				g.drawLine(cx, cy, x, y);
//			}
			
			// Display shape indicators
			g.setColor(Color.gray);
			g.drawString(("" + shape), 300, 10 );
			g.setColor(saveC);
		}
	}

	public void draw(Graphics g, RPanel rp, Color c)
	{
		draw(g, rp, c, false);
	}

	public void draw(Graphics g, RPanel rp)
	{
		draw(g, rp, null);
	}

	public void show()
	{
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(DEFAULT_MAX_FRACTION_DIGITS);
		nf.setMinimumFractionDigits(DEFAULT_MIN_FRACTION_DIGITS);
		nf.setMinimumIntegerDigits(DEFAULT_MIN_INTEGER_DIGITS);

		System.out.println("#TYPE=" + type + " " + ri.title);
		System.out.println("#NV=" + nv);
		for (int i = 0; i < nv; i++)
			System.out.println(  nf.format(v[i].getx()) + "|"
							+ nf.format(v[i].gety()) );
	}
	
	public void writeData(boolean draw3d, PrintWriter out) throws IOException
	{
		String parms = "";
		writeData(draw3d, out, parms);
	}

	public void writeData(boolean draw3d, PrintWriter out, String parms) throws IOException
	{
		String lineOut = "";
		String parmsOut = "";
		String vertexFlag, drawMode, color;
		
		if (xcolor != null)
			parmsOut += "Color " + xcolor + " ";
		else
			parmsOut += "Color None ";
		parmsOut += parms;
		
		/* Create output for Draw3D */
		if (draw3d) {
			if (xcolor != null) {
				vertexFlag = "false";
				drawMode = "FILL";
				color = xcolor;
			}
			else {
				vertexFlag = "true";
				drawMode = "LINE";
				color = "0xfaebd7ff"; 	
			}
			
			StringTokenizer t = new StringTokenizer(ri.title, "-");
//			if (type == TYPE_POLYGON_CELL || t.nextToken().equals("PolyMap")) {
			if (t.nextToken().equals("PolyMap")) {
				lineOut += "Polygon: ";
			}
			else {
				lineOut = "DataGroup:";
				out.println(lineOut);
				
				if (ri.title != null)
					lineOut = "Title1: " + ri.title;
				else
					lineOut = "Title1: Polygon";
				out.println(lineOut);
				
				lineOut = "Title2: " + parmsOut;
				out.println(lineOut);
				
				lineOut = "Polygon: ";
			}

			String fmt = DRAW3D_NUMBER_FORMAT;
			for (int i = 0; i < nv; i++) {
				lineOut +=  String.format(fmt,v[i].getx()) + ", " +
							String.format(fmt,v[i].gety()) + ", " +
							String.format(fmt,0.0) + "; ";
			}
			lineOut += " # ,  true , " + vertexFlag  + " , false , " + drawMode + " , NONE , " +  color;	
			out.println(lineOut);
			
			if (this.tr != null) {
				lineOut = "// Triangulation";
				out.println(lineOut);
				lineOut = "DataGroup: ";
				out.println(lineOut);
				
				for (int i = 0; i < tr.length; i++) {
					lineOut = "Polygon: ";
					lineOut += 	String.format(fmt, tr[i].A.getx()) + ", " +
								String.format(fmt, tr[i].A.gety()) + ", " +
								String.format(fmt, 0.0) + "; ";
					lineOut += 	String.format(fmt, tr[i].B.getx()) + ", " +
								String.format(fmt, tr[i].B.gety()) + ", " +
								String.format(fmt, 0.0) + "; ";
					lineOut += 	String.format(fmt, tr[i].C.getx()) + ", " +
								String.format(fmt, tr[i].C.gety()) + ", " +
								String.format(fmt, 0.0) + "; ";
					lineOut += " # ,  true , " + vertexFlag  + " , false , FILL , NONE , " +  tr[i].xcolor;	
					out.println(lineOut);		
				}
			}
		}
		
		/* Create standard output */
		else {		
			NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMaximumFractionDigits(DEFAULT_MAX_FRACTION_DIGITS);
			nf.setMinimumFractionDigits(DEFAULT_MIN_FRACTION_DIGITS);
			nf.setMinimumIntegerDigits(DEFAULT_MIN_INTEGER_DIGITS);
	
			out.println("#TYPE=" + type + " " + ri.title);
			out.println("#PARMS= " + parmsOut);
			out.println("#NV=" + nv);
			for (int i = 0; i < nv; i++)
				out.println(nf.format(v[i].getx()) + ", " +
						    nf.format(v[i].gety()));
		}
	}
	
	public String[] readTypeTitleParms(BufferedReader in) throws IOException
	{
		String[] sttp = new String[4];
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
					if ( t.hasMoreTokens() && "Color".equals(t.nextToken()) ) {
						sttp[3] = t.nextToken();
					}
			}
		}
		else {
			System.out.println("PolygonA readTypeTitleParms - error reading input file");
			sttp = null;
		}
		return sttp;
	}
	
	public void readData(BufferedReader in) throws IOException
	{
		String s;
		for (int i=0; i < nv; i++)
		{
			s = in.readLine();
			v[i] = new Pointd(s);
		}
	}
	
	public boolean isGend()
	{
//		if (nv > 0 && v != null && checkPolygon())
		if (nv > 0 && v != null)
			return true;
		else
			return false;

//		return (nv == v.length);
	}

	public void setHull(Hull h)
	{
		this.h = h;
	}

	/* Find convex hull */
	public void findHull()
	{
//		for (int i = 0; i < v.length; i++)
//			System.out.println("v[" + i + "]= " + v[i]);
		if ( h == null)
		{
			h = new Hull(v);
//		for (int i = 0; i < h.size(); i++)
//			System.out.println("h[" + i + "]= " + h.getPoint(i));
		}
	}

	public int sizeHull()
	{
		if (h == null)
			return 0;
		else
			return h.size();
	}

	public Hull getHull()
	{
		if (h == null)
			this.findHull();
		return h;
	}

	/* Compute perimeter/distance ratio */
	public double computePDRatio()
	{
		int i, j;
		double p = 0.0;
		double d = 0.0;
		// Compute segment lengths
		for (i = 0; i < nv; i++)
		{
			for (j = i+1; j < nv; j++)
			{
				p = Geometry.distance(v[i], v[j]);
			}
			d = Geometry.distance(v[i], v[j]);
		}
		return (p/d);
	}

	/* Compute polygon metrics */
	public void computeMetrics()
	{
		if (h == null)
			findHull();
		convex = isConvex();
		ccw = Ccw();
		cg = computeCG();
		area = computeArea();
		hullArea = h.computeArea();
		areaRatio = area/hullArea;
		perimeter = computePerimeter();
		intercnt = computeIntercnt();
		hullPerimeter = h.computePerimeter();
		perRatio = hullPerimeter/perimeter;
		compacity = computeCompacity();
		complexity = computeComplexity();
		simplexity = computeSimplexity();

//		System.out.println("# vertices: " + v.length);
//		System.out.println("# vertices: " + v.length);
//		System.out.println("convex: " + convex);
//		System.out.println("ccw: " + ccw);
//		System.out.println("cg: " + cg);
//		System.out.println("area: " + area);
//		System.out.println("hullArea: " + hullArea);
//		System.out.println("areaRatio: " + areaRatio);
//		System.out.println("perimeter: " + perimeter);
//		System.out.println("hullPerimeter: " + hullPerimeter);
//		System.out.println("perRatio: " + perRatio);

//		System.out.println("notches: " + notches);
//		System.out.println("notches_norm: " + notches_norm);
//		System.out.println("notches_squared: " + notches_squared);
//		System.out.println("freq: " + freq);
//		System.out.println("ampl: " + ampl);
//		System.out.println("conv: " + conv);
//		System.out.println("complexity: " + complexity);
	}

	/* Finds polygon's center of gravity */
	public Pointd computeCG()
	{
		double A2, areaSum2 = 0;  	//partial area sum
		Pointd cent3 = new Pointd();
		double x = 0.0;
		double y = 0.0;

//		for (int i = 0; i < v.length-1; i++)
		for (int i = 0; i < nv-1 ; i++)
		{
			cent3 = Geometry.Centroid3(v[0], v[i], v[i+1]);
			A2 = Geometry.twice_area(v[0], v[i], v[i+1]);
			x = x + (A2 * cent3.getx());
			y = y + (A2 * cent3.gety());
			areaSum2 = areaSum2 + A2;
		}
		//Division by 3 is delayed to the last moment.
		x = x / (3*areaSum2);
		y = y / (3*areaSum2);
		return (new Pointd(x,y));
	}

	/* Returns twice the area of a polygon formed by the list of vertices
	*/
 	public double Area2()
 	{
		double sum = 0;

		for (int i = 1; i <= nv - 2; i++) {
			sum += Geometry.twice_area(v[0], v[i], v[i+1]);
		}

		return sum;
 	}

	/* Compute polygon area */
	public double computeArea()
	{
		return .5D * Area2();
	}

	/* Compute perimeter */
	public double computePerimeter()
	{
		double per = 0.0D;

		for (int i = 0; i < v.length - 1; i++)
			per += Math.sqrt(Geometry.distance2(v[i], v[i+1]));
		per += Math.sqrt(Geometry.distance2(v[v.length-1], v[0]));
		return per;
	}

	/**
	 * Compute compacity
	 * 
	 * Stoyan, Dietrich and Helga Stoyan.
	 * Fractals, Random Shapes, and Point Fields.
	 * Chichester, UK: John Wiley & Sons, 1994.
	 * 
	 */
	public double computeCompacity()
	{
		if (area == 0.0)
			computeArea();
		if (perimeter == 0.0)
			computePerimeter();
		compacity = (4*Math.PI*area)/(perimeter*perimeter);
		compacity = 1.0 - compacity;
//		if (isConvex())
//			compacity = compacity * 0.25D;
		return compacity;
	}

	/* Counts notches in a ccw oriented polygon
	*/
	public long countNotches()
	{
		int i2, i3;
		long count = 0;

		for (int i=0; i < nv; i++)
		{
			i2 = Geometry.next(i, v.length);
			i3 = Geometry.next(i2, v.length);
			if ( !Geometry.lefton(	v[i], v[i2], v[i3]) )
				count++;
		}
		return count;
	}

	/**
	 * Compute complexity
	 * Brinkhoff, Thomas, et al. Measuring the Complexity of Polygonal Objects
	 * In GIS 1995, Proceedings of the 3rd ACM International Workshop
	 * on Advances in Geographic Information Systems,
	 * Baltimore, Maryland, December 1-2, 1995, 109-118.
	 * New York, New York: ACM Press, 1995.
	 */
	public double computeComplexity()
	{
		if (area == 0.0)
			computeArea();
		if (perimeter == 0.0)
			computePerimeter();
		if (notches == 0 )
			notches = countNotches();

		if (h == null)
			findHull();
		if (hullArea == 0.0)
			hullArea = h.computeArea();
		if (hullPerimeter == 0.0)
			hullPerimeter = h.computePerimeter();

		if (isConvex())
		{
			notches_norm = 0;
		}
		else
		{
			notches_norm = notches / (nv - 3.0);
		}
		
		notches_squared = (notches_norm - 0.5) * (notches_norm - 0.5);
		notches_quad = notches_squared * notches_squared;
		freq = 16.0*notches_quad - 8.0*notches_squared + 1.0;
		if (Math.abs(perimeter - hullPerimeter) < epsilon)
			ampl = 0.0;
		else
			ampl = (perimeter - hullPerimeter) / perimeter;
		if (Math.abs(hullArea - area) < epsilon)
			conv = 0.0;
		else
			conv = (hullArea - area) / hullArea;
		complexity = 0.8 * ampl * freq + .2 * conv;
		if (complexity < 0.0)
			complexity = 0.0;
		return complexity;
	}

	/**
	 * Compute simplexity
	 * 
	 * Simha, Rahul.
	 * The George Washington University.
	 * Washington, DC
	 */
	public double computeSimplexity()
	{
		double d;
		double p;
		double r;
		double rs;

		p = 0.0;
		d = 0.0;
		r = 0.0;
		rs = 0.0;
		for(int i = 1; i < nv; i++)
		{
			p += Geometry.distance(v[i-1],v[i]);
			d = Geometry.distance(v[0], v[i]);
			r = p/d;
			rs += r;
//			System.out.println("i= " + i + ", p= " + p + ", d = " + d + ", r= " + r);
		}
//		System.out.println("rs = " + rs + ", ln(rs)= " + Math.log(rs));
		return Math.log(rs);
//		return 0.0;
	}

	public double getSimplexity()
	{
		if (simplexity == 0.0)
			simplexity = computeSimplexity();
		return simplexity;
	}

	/* Determine if the polygon/list is oriented counterclockwise (ccw).
	 * (A more efficient method is possible, but here we use the available
	 * Area2() O' Rourke)
	 */
	public boolean Ccw()
	{
		if (Area2() > 0 )
			return true;
		else
			return false;
	}

	/*
	 * Translate polygon vertices to new location
	 */
	public void translate(double xdelta, double ydelta)
	{
		for (int i = 0; i < nv; i++)
			v[i] = v[i].translate(xdelta, ydelta);
	}

	/*
	 * Scale polygon to specified size
	 */
	public void scale(double factor)
	{
		for (int i = 0; i < nv; i++)
			v[i] = v[i].scale(factor);
	}

	/*
	 * Reverses vertex order to make it Ccw
	 */
	public void Reverse()
	{
		int n = v.length;
		Pointd[] u = new Pointd[n];
		System.arraycopy( v, 0, u, 0, nv );
		for (int i = 0; i < nv; i++)
			v[i] = u[nv - i - 1];
	}

	/*
	 * Returns true if polygon is convex, else returns false
     */
	public boolean isConvex()
	{
//		/* Find polar diagram */
//		if (rang == null)
//			computePolar();
//
//		/* Find antipodal wedges with multiplicity 1 */
//		if (wl == null)
//			wl = new WedgeList(rang);
//
//		if (wl.size() == 1 && wl.getFirstMult() == 1)
//			return true;
//		else
//			return false;

		int i2, i3;
		for (int i1 = 0; i1 < nv; i1++)
		{
			i2 = Geometry.next(i1, v.length);
			i3 = Geometry.next(i2, v.length);
			if ( !Geometry.lefton(	v[i1], v[i2], v[i3]) )
				return false;
		}
//		System.out.println("isConvex() done");
		return true;
	}

	/* Check for monotone polygon */
	/* Preparata and Supowit, Info. Proc. Lett. 12(4) (1981) 161-164. */
	public boolean isMonotone()
	{
		/* Find polar diagram */
		if (rang == null)
			computePolar();

		/* Find antipodal wedges with multiplicity 1 */
		if (wl == null)
			wl = new WedgeList(rang);

//		System.out.println("WedgeList size= " + wl.size());
//		System.out.println("isMonotone() done");
		return wl.findMonotone();
	}

	public boolean wedge1()
	{
		/* Find polar diagram */
		if (rang == null)
			computePolar();

		/* Find antipodal wedges with multiplicity 1 */
		if (wl == null)
			wl = new WedgeList(rang);

//		System.out.println("wedge1() done");
		return wl.wedge1();
	}

	public void computePolar()
	{
		int i, j;

		/* Translate all edges to start at origin */
		Pointd[] u = new Pointd[nv];
		for (i = 0; i < nv; i++)
		{
			j = next(i, nv);
			u[i] = new Pointd(v[j].getx()-v[i].getx(),
					  		  v[j].gety()-v[i].gety() );
		}

		/* Find angles */
		pang = new double[nv];
		for (i = 0; i < pang.length; i++)
		{
			double x = u[i].getx();
			double y = u[i].gety();
			double atan = Math.atan(y/x);
			if ( x >= 0.0 && y >= 0.0 )
				pang[i] = atan;
			else if ( x < 0.0 && y >= 0.0 )
				pang[i] =  Math.PI + atan;
			else if ( x < 0.0 && y < 0.0 )
				pang[i] = Math.PI + atan;
			else
				pang[i] = 2*Math.PI + atan;
		}

		if (debug > 2)
		{
			for (i = 0; i < pang.length; i++)
				System.out.println("pang[" + i + "]= " + pang[i] + " " + (pang[i]*180.0/Math.PI) );
			System.out.println();
		}

		// Find edge with least polar angle.
		j = findMin(pang);
		double mina = pang[j];

		// Relativize all edge angles to edge with least angle.
		// Rotate all edges clockwise, so least angle is zero.
		// Make the least angle (edge) the first.

		rang = new double[pang.length];
		for (i = 0; i < pang.length; i++)
		{
			rang[i] = pang[j] - mina;
			j = next(j, pang.length);
		}

		if (debug > 2)
		{
			for (i = 0; i < rang.length; i++)
				System.out.println("rang[" + i + "]= " + rang[i] + " " + (rang[i]*180.0/Math.PI) );
			System.out.println();
		}
	}

	private int findMin(double[] a)
	{
		int mini = 0;
		double min = Double.MAX_VALUE;

		for (int i = 0; i < a.length; i++)
		{
			if (a[i] < min)
			{
				min = a[i];
				mini = i;
			}
		}

		return mini;
	}

	private void computeExternal()
	{
		int i, j;

		if (pang == null)
			computePolar();

		/* Find external angles at each vertex */
		xang = new double[nv];
		double diff;
		for (i = 0; i < nv; i++)
		{
			j = prev(i, nv);
			diff = pang[i] - pang[j];
			if (Math.abs(diff) > Math.PI)
			{
				if ( pang[i] < Math.PI && pang[j] > Math.PI)
					xang[i] =  (Math.PI*2.0 - pang[j] + pang[i]);
				else if ( pang[j] < Math.PI && pang[i] > Math.PI)
					xang[i] = -(Math.PI*2.0 - pang[i] + pang[j]);
			}
			else
				xang[i] = pang[i] - pang[j];
		}

		if (debug > 2)
		{
			for (i = 0; i < nv; i++)
				System.out.println("xang[" + i + "]= " + xang[i] + " " + (xang[i]*180.0/Math.PI) );
			System.out.println();
		}
	}

	public void drawSpecial(Graphics g, RPanel rp)
	{
		int x, y;

		if (rang == null)
			computePolar();

		int	x0 = rp.iX(0.0);
		int	y0 = rp.iY(0.0);

		for (int i = 0; i < pang.length; i++)
		{
			x = rp.iX(Math.cos(rang[i]));
			y = rp.iY(Math.sin(rang[i]));
			g.drawString((" " + i), x-15, y+5);
			g.drawLine(x0,y0,x,y);
		}
	}

	/*
	 * Check for point included in polygon
	 */
	 public boolean includes(Pointd p)
	 {

		cg = computeCG();
		double cx = cg.getx();
		double cy = cg.gety();

		return false;
	}

	/*
	 * Return indicator of shape other
	 */
	public boolean isOther()
	{
		return shapeb[6];
	}

	/*
	 * Check for spiral polygon
	 * R. Cole and M. Goodrich. "Optimal Parallel Algorithms for Polygon and Point-Set Problems."
	 * ACM Proceedings of the 4th Annual Symposium on Computational Geometry, 1988, p. 202.
	 */
	public boolean isSpiral()
	{
		int i, j;

		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(4);
		nf.setMinimumFractionDigits(4);
		nf.setMinimumIntegerDigits(1);

		/* Compute polar angles */
		if (pang == null)
			computePolar();

		/* Compute external angles */
		if (xang == null)
			computeExternal();

		/* Compute curvature sums */

		double[] ai0 = new double[nv];
		double[] a0i = new double[nv];
		double[] fi = new double[nv];
		double[] bi = new double[nv];
		double low;

//		System.out.println("\nSpiral check");
//		for (i = 0; i < nv; i++)
//			System.out.println("v[" + i + "]= " + v[i]);
//		System.out.println();

		a0i[0] = 0;
		fi[1] = a0i[0];
		low  = a0i[0];
		for (i = 1; i < nv; i++)
		{
			a0i[i] = a0i[i-1] + xang[i];
//			System.out.println( "a0i["  + i     + "]= " + nf.format(a0i[i]*180.0/Math.PI) +
//								" a0i[" + (i-1) + "]= " + nf.format(a0i[i-1]*180.0/Math.PI) +
//								" xang["+ i     + "]= " + nf.format(xang[i]*180.0/Math.PI));

			if (i > 1)
				fi[i] = Math.max(fi[i-1],a0i[i-1]);
//			System.out.println("fi[" + i + "]= " + nf.format(fi[i]*180.0/Math.PI));
			if (a0i[i] < low)
				low = a0i[i];
//			System.out.println("low= " + nf.format(low*180.0/Math.PI));
		}
		fi[0] = Math.max(fi[nv-1],a0i[nv-1]);
//		System.out.println("fi[0]= " + nf.format(fi[0]*180.0/Math.PI));
//		System.out.println();

		ai0[0] = 0;
		bi[nv-1] = xang[0];
		for (i = nv-1; i > 0; i--)
		{
			ai0[i] = ai0[next(i,nv)] + xang[next(i,nv)];
//			System.out.println( "ai0[" + i     + "]= " + nf.format(ai0[i]*180.0/Math.PI) +
//								" ai0[" + next(i,nv) + "]= " + nf.format(ai0[next(i,nv)]*180.0/Math.PI) +
//								" xang["+ next(i,nv) + "]= " + nf.format(xang[next(i,nv)]*180.0/Math.PI));

			if (i < nv-1)
				bi[i] = Math.max(bi[next(i,nv)],ai0[i]);
//			System.out.println("bi[" + i + "]= " + nf.format(bi[i]*180.0/Math.PI));
		}
		bi[0] = ai0[0];
//		System.out.println("bi[0]= " + nf.format(bi[0]*180.0/Math.PI));
//		System.out.println();

		double pi3 = 3.0*Math.PI;
		if (fi[0] - low >= pi3)
		{
//			System.out.println("Spiral true - low check");
			return true;
		}

		for (i = 0; i < nv; i++)
			if (fi[i] + bi[i] >= pi3)
			{
//				System.out.println("Spiral true - sum check - i= " + i);
				return true;
			}

//		System.out.println("Spiral false");
		return false;
	}

	/*
	 * Check for star polygon
	 */
	public boolean isStar()
	{
		KernelList kl = new KernelList(v);
		return kl.isStar();
//		return false;
	}

	/*
	 * Check for simple polygon
	 */
	public boolean isSimple()
	{
		int i, j;

//		LineIntersection li = new LineIntersection(v);
//		if (li.isSimple())
//			System.out.println("true from new isSImple()");
//		else
//			System.out.println("false from new isSimple()");

		/* First let's check that vertices are distinct */

  		for(i=0; i<nv-1; i++)
    		for(j=i+1; j<nv; j++)
//    	 		if( (Math.abs(v[i].getx()-v[j].getx()) <= epsilon) && (Math.abs(v[i].gety()-v[j].gety())<=epsilon) )
//				if ( !v[i].isDistinct(v[j]) )
				if ( v[i].equals(v[j]) )
					return false;

  		/* Now we check simplicity: no two edges should intersect */
  	   /* improperly. Run through the edges 1,...,n-1 and check  */
  	   /* them against 2,...,n                                   */

		/* Check the edge from v[i] to v[next(i,n)] */
		/* against the edge starting from v[j]      */

  		for(i=0; i<nv-1; i++)
    		for(j=i+1; j<nv; j++)
				if ( ! Geometry.valid_edges(v[i],v[Geometry.next(i,nv)], v[j],v[Geometry.next(j,nv)]) )
					return false;

//		System.out.println("isSimple() done");
		return true;
	}

	/* Check polygon checks that a randomly created polygon is
	   actually a valid simple polygon by looking for duplicate
	   vertices and testing edge intersections */

	public boolean checkPolygon()
	{
		int i, j;
		Date dst;
		df = new long[6];

		dst = new Date();
		shapeb[0] = isSimple();
		df[0] += (new Date().getTime())-dst.getTime();
		if (shapeb[0])				/* simple ? */
		{
			if (!Ccw())				/* check for counter clockwise */
				Reverse();

			if (type == TYPE_POLYGON_CELL )		/* rectilinear ? */
			{
				shapeb[7] = true;
			}

			else if (nv == 3)					/* triangle ? */
			{
				shapeb[1] = true;	/* triangle */
				shapeb[2] = true;	/* convex */
				shapeb[3] = true;	/* monotone */
				shapeb[4] = true;	/* star */
			}
			else /* not triangle */
			{
				dst = new Date();
				shapeb[2] = isConvex();
				df[2] += (new Date().getTime())-dst.getTime();
				if (shapeb[2])					/* convex ? */
				{
					shapeb[3] = true;	/* monotone */
					shapeb[4] = true;	/* star */
				}
				else /* not convex */
				{
					dst = new Date();
					shapeb[5] = isSpiral();
					df[5] += (new Date().getTime())-dst.getTime();
					if (shapeb[5])		/* spiral ? */
					{
					}
					else /* not spiral - monotone or star ? */
					{
						// Check monotone - compute wedgelist
						dst = new Date();
						wl = new WedgeList(rang);
						shapeb[3] = isMonotone();
						df[3] += (new Date().getTime())-dst.getTime();

						// Check star
						dst = new Date();
						shapeb[4] = isStar();
						df[4] += (new Date().getTime())-dst.getTime();
					} /* end else not spiral */
				} /* end else not convex */
			} /* end else not triangle */

			if (!shapeb[1] && !shapeb[2] && !shapeb[3] && !shapeb[4] && !shapeb[5] && !shapeb[7])
				shapeb[6] = true;		/* other */

			shape = getShape();
			return true;
		} /* end if simple */
		else /* not simple */
			return false;
	}

	/* Checks if point is distinct from all existing polygon vertices */
	public boolean isDistinct(Pointd p)
	{
		for (int i = 0; i < nv - 1; i++)
//			if (!v[i].isDistinct(p))
			if ( v[i].equals(p) )
				return false;
		return true;
	}

	/* Checks if point is collinear with any existing edge of polygon */
	public boolean isCollinear(Pointd p)
	{
		for (int i = 0; i < nv - 1; i++)
			if ( Geometry.collinear(v[i], v[i+1], p) )
				return true;
		return false;
	}

	/* Checks if point is valid new convex vertex */
	public boolean isValidConvexVertex(Pointd p)
	{
//		if (!isDistinct(p) ||
//			isCollinear(p) ||
//			!Geometry.left(v[nv-2], v[nv-1], p) )
//			return false;

		if ( !isDistinct(p) )
		{
			System.out.println("Not distinct: " + p);
			return false;
		}

		if ( isCollinear(p) )
		{
//			System.out.println("Collinear: " + p);
			return false;
		}

		if ( !Geometry.left(v[nv-2], v[nv-1], p) )
		{
//			System.out.println("Not left: " + p);
			return false;
		}

		if ( !Geometry.left(v[nv-1], p, v[0]) )
		{
//			System.out.println("Not left to origin: " + p);
			return false;
		}

		if ( !Geometry.left(p, v[0], v[1]) )
		{
//			System.out.println("Not left at origin: " + p);
			return false;
		}

		for (int i = 0; i < nv-2; i++)
			if ( !Geometry.valid_edges(v[i], v[i+1], v[nv-1], p) )
			{
//				System.out.println("Not valid edge: " + v[i] + " " + v[i+1] + " " + p);
				return false;
			}

		return true;
	}

	/* Checks if point is valid new non-convex vertex */
	public boolean isValidNonConvexVertex(Pointd p)
	{
		if (!isDistinct(p) ||
			isCollinear(p) )
			return false;

		for (int i = 0; i < nv-2; i++)
			if ( !Geometry.valid_edges(v[i], v[i+1], v[nv-1], p) )
				return false;
		return true;
	}

   	public void triangulate()
	{
		int n;

		if (tr == null)
		{
			n = v.length;
			if ( !Ccw() )		/* if not counter-clockwise, reverse order; */
				Reverse();
			int ntr = n - 2;
			tr = new Triangle[ntr];		   
			if (n == 3)
				tr[0] = new Triangle(v[0], v[1], v[2]);
				
			else
				Tools2D.triangulate(v, tr);
			setColor(tr, seedColor);
		}
    }
   	
   	public void setColor() {
   		this.pcolor = DEFAULT_POLYGON_COLOR;
   		this.xcolor = DEFAULT_POLYGON_XCOLOR;
   	}
   	
   	public void setColor(Triangle[] tr, long seedColor) {
   		int r, g, b;
   		Color pcolor;
   		String xcolor;
		if (seedColor == 0)
			seedColor = Math.abs((int)(new Date().getTime()));
		Rand cr = new Rand(seedColor);
		for (int i = 0; i < tr.length; i++) {
	   		r = cr.uniform(0, 255);
			g = cr.uniform(0, 255);
			b = cr.uniform(0, 255);
			pcolor = new Color(r, g, b);
			xcolor = "0x" + 
					Integer.toHexString(0x100 | r).substring(1) +
					Integer.toHexString(0x100 | g).substring(1) +
					Integer.toHexString(0x100 | b).substring(1) +
					"ff";					
			tr[i].setColor(pcolor);
			tr[i].setXColor(xcolor);
		}
   	}

	public int sizeDecomp()
	{
		if (tr == null)
			return 0;
		else
			return tr.length;
	}

	public void drawDecomp(Graphics g, RPanel rp)
	{
		int n;

		if (tr == null)
			triangulate();

		if (tr != null && tr.length > 0)
		{
			n = tr.length;
//			if (seedColor == 0)
//				seedColor = Math.abs((int)(new Date().getTime()));
//			Rand mr = new Rand(seedColor);
			Color saveColor = g.getColor();
			for (int j=0; j<n; j++)
			{
//				Color c = new Color(mr.uniform(0, 255), mr.uniform(0, 255), mr.uniform(0,255));
//				System.out.println("PolygonA drawDecomp color: " + c);
				int[] x = new int[3], y = new int[3];
				x[0] = rp.iX(tr[j].A.x); y[0] = rp.iY(tr[j].A.y);
				x[1] = rp.iX(tr[j].B.x); y[1] = rp.iY(tr[j].B.y);
				x[2] = rp.iX(tr[j].C.x); y[2] = rp.iY(tr[j].C.y);
//				g.setColor(c);
				g.setColor(tr[j].pcolor);
				g.fillPolygon(x, y, 3);
			}
			g.setColor(saveColor);
		}
	}

	public double getAbsMaxX()
	{
		double max = 0.0;
		double val;
		for (int i = 0; i < nv; i++)
		{
			val = Math.abs(v[i].getx());
			if (val > max)
				max = val;
		}
		return max;
	}

	public double getAbsMaxY()
	{
		double max = 0.0;
		double val;
		for (int i = 0; i < nv; i++)
		{
			val = Math.abs(v[i].gety());
			if (val > max)
				max = val;
		}
		return max;
	}

	public void scaleToUnitSquare()
	{
		double maxX = getAbsMaxX();
		double maxY = getAbsMaxY();
		double scale = 1.0/(maxX > maxY ? maxX : maxY);

		if (scale < 1.0)
		{
			for (int i = 0; i < nv; i++)
				v[i] = new Pointd(scale*v[i].getx(), scale*v[i].gety());
		}
	}

	public int next(int i, int n)
	{
	  if (i < n-1) return i+1;
	  else return 0;
	}

	public int prev(int i, int n)
	{
		if (i==0) return n-1;
		else return i-1;
	}

	/**
	 * Private class method to find the lowest rightmost point.
	 */
	private int findLowest(Pointd[] v)
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

	/*
	 * Print diagnostic messages.
	 */
	private void diag(int i, String s)
	{
		if (debug >= i)
			System.out.println(s);
	}
	
	public RInfo getRInfo() {
//		System.out.println("PolygonA getRInfo: " + this.ri.hashCode());
		return this.ri;
	}
	
	public void setRInfo(RInfo ri) {
		this.ri = ri;
	}
}