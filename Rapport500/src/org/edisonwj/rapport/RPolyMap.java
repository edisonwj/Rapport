package org.edisonwj.rapport;

/**
 * RPolyMap defines the general class of poly maps,
 * i.e. collections of adjacent conjoined polygons
 * in a map like configuraiton.
 * Four variants are provided for:
 * - Cell polymaps
 * - Poisson Linefield defined polymaps
 * - Random polyline defined polymaps
 * - Voronoi diagrams (polymaps) determined by a random point set  
 */

import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.StringTokenizer;

class RPolyMap extends RObject implements RapportDefaults
{
	protected RInfo ri;
	
	protected RapportException illegal_state = new RapportException("illegal state");
	protected RapportException undefined = new RapportException("undefined process");

	protected int np;				/* Number of polygons */
	protected PolygonA[] pa;		/* Array of polygons */
	protected Pointd[] sites;		/* Sites for Voronoi polymap */

	protected Rand cr;				/* Random color generation */
	protected long seedColor;		/* Random color seed for drawing polygons */

	protected double total_vertices;
	protected double total2_vertices;
	protected double mean_vertices;
	protected double var_vertices;
	protected double min_vertices;
	protected double max_vertices;
	protected double chi2_vertices;
	protected double chi2_inner_vertices;

	protected double total_area;
	protected double total2_area;
	protected double mean_area;
	protected double var_area;
	protected double min_area;
	protected double max_area;

	protected double total_perimeter;
	protected double total2_perimeter;
	protected double mean_perimeter;
	protected double var_perimeter;
	protected double min_perimeter;
	protected double max_perimeter;

	protected double total_compacity;
	protected double total2_compacity;
	protected double mean_compacity;
	protected double var_compacity;
	protected double min_compacity;
	protected double max_compacity;
	protected int cnt_compacity;

	protected double total_complexity;
	protected double total2_complexity;
	protected double mean_complexity;
	protected double var_complexity;
	protected double min_complexity;
	protected double max_complexity;

	protected double total_simplexity;
	protected double total2_simplexity;
	protected double mean_simplexity;
	protected double var_simplexity;
	protected double min_simplexity;
	protected double max_simplexity;

	protected int npv[];		/* Number of polygons of each size */

	public RPolyMap()
	{
//		System.out.println("RPolyMap() constructor: " + this.hashCode());
		this.ri = ri;
		type = TYPE_POLYMAP;
	}
		
	public RPolyMap(BufferedReader in) throws IOException
	{
		this();
//		System.out.println("RPolyMap(Buffered Reader) constructor this: " + this.hashCode());
		String[] sttp;
		String stitle;
		sttp = readTypeTitleParms(in);
		type = Integer.parseInt(sttp[0]);
		stitle = sttp[1];
		np = readNP(in);
		if (np >= 0)
		{
			pa = new PolygonA[np];
			for (int i = 0; i < np; i++)
				pa[i] = new PolygonA(TYPE_POLYMAP, in);
			nv = np;
			ri = new fiInfo(pa.length);
			ri.title = stitle;
//			System.out.println("RPolyMap this: " + this.hashCode());
//			System.out.println("RPolyMap ri: " + ri.hashCode() + ", ri.title: " + ri.title);
		}
		else
		{
			System.out.println("RPolyMap: Error constructing point distribution from file input");
			pa = null;
			nv = 0;
		}
	}
	
	public int readNP(BufferedReader in) throws IOException
	{
		String s;
		StringTokenizer t;
		int np = 0;
		s = in.readLine();
		t = new StringTokenizer(s, "#= \t\n\r");
		if ( s.charAt(0) == '#' && "NP".equals(t.nextToken()) )
			np = Integer.parseInt(t.nextToken());
		return np;
	}
		
	public void translate(double xdelta, double ydelta)
	{
		for (int i = 0; i < np; i ++)
			pa[i].translate(xdelta, ydelta);
	}

	public void scale(double factor)
	{
		for (int i = 0; i < np; i ++)
			pa[i].scale(factor);
	}
	
	public void setColor() {
		int r, g, b;
		Color pcolor;
		String xcolor;

		for (int i = 0; i < np; i++) {
			r = cr.uniform(0, 255);
			g = cr.uniform(0, 255);
			b = cr.uniform(0, 255);
			pcolor = new Color(r, g, b);
			xcolor =	"0x" + 
					Integer.toHexString(0x100 | r).substring(1) +
					Integer.toHexString(0x100 | g).substring(1) +
					Integer.toHexString(0x100 | b).substring(1) +
				"ff";					
			pa[i].setColor(pcolor);
			pa[i].setXColor(xcolor);
		}
	}
	
	public void draw(Graphics g, RPanel rp)
	{
		if ( np > 0 ) {
			Color saveC = g.getColor();
			for (int i = 0; i < np; i++)
				if (pa[i].pcolor != null) {
					boolean fill = true;
					pa[i].draw(g, rp, pa[i].pcolor, fill);
				}
				else {
					pa[i].draw(g, rp);	
				}
			g.setColor(saveC);
		}
	}
	
	public void writeData(boolean draw3d, PrintWriter out) throws IOException
	{
//		System.out.println("RPolyMap writeData - type: " + type);
		
		PolygonA pl;
		String lineOut = "";
		String parmsOut = "";
		
		/* Create output for Draw3D */
		if (draw3d) {
			lineOut = "DataGroup:";
			out.println(lineOut);
			lineOut = "Title1: " + ri.title;
			out.println(lineOut);
			lineOut = "Title2: " + parmsOut;
			out.println(lineOut);

			for (int i = 0; i < np; i++)
			{
	   	  		pl = pa[i];
	     		pl.writeData(draw3d, out);
			}
		}
		
		/* Create standard output */
		else {
			NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMaximumFractionDigits(DEFAULT_MAX_FRACTION_DIGITS);
			nf.setMinimumFractionDigits(DEFAULT_MIN_FRACTION_DIGITS);
			nf.setMinimumIntegerDigits(DEFAULT_MIN_INTEGER_DIGITS);
	
			out.println("#TYPE=" + type + " " + ri.title);
			out.println("#PARMS=");
			out.println("#NP=" + np);
			for (int i = 0; i < np; i++)
			{
	   	  		pl = pa[i];
	     		pl.writeData(draw3d, out);
			}
		}
	}

	public void computeMetrics()
	{
//		System.out.println("RPolyMap Compute PolyMap metrics");
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(0);
		nf.setMinimumIntegerDigits(4);

		int i, j, k, l, n;
		double c, a, p, x, s;
		Pointd g, v;

		double minval = -100000000.0;
		double maxval =  100000000.0;
		double min_x = minval;
		double max_x = maxval;
		double min_y = minval;
		double max_y = maxval;

		total_vertices = 0.0;
		total2_vertices = 0.0;
		mean_vertices = 0.0;
		var_vertices = 0.0;
		min_vertices = maxval;
		max_vertices = minval;
		chi2_vertices = 0.0;

		total_area = 0.0;
		total2_area = 0.0;
		mean_area = 0.0;
		var_area = 0.0;
		min_area = maxval;
		max_area = minval;

		total_perimeter = 0.0;
		total2_perimeter = 0.0;
		mean_perimeter = 0.0;
		var_perimeter = 0.0;
		min_perimeter = maxval;
		max_perimeter = minval;

		total_compacity = 0.0;
		total2_compacity = 0.0;
		mean_compacity = 0.0;
		var_compacity = 0.0;
		min_compacity = maxval;
		max_compacity = minval;
		cnt_compacity = 0;

		total_complexity = 0.0;
		total2_complexity = 0.0;
		mean_complexity = 0.0;
		var_complexity = 0.0;
		min_complexity = maxval;
		max_complexity = minval;
		
		total_simplexity = 0.0;
		total2_simplexity = 0.0;
		mean_simplexity = 0.0;
		var_simplexity = 0.0;
		min_simplexity = maxval;
		max_simplexity = minval;

		npv = new int[100];
		for (i = 0; i < npv.length; i++)
			npv[i] = 0;

		for (i = 0; i < np; i++)
		{
//			System.out.println("Compute metrics for polygon: " + i + " of " + np);
   	  		PolygonA pl = pa[i];
     		n = pl.size();
//     		System.out.println("n = " + n);
     		a = pl.getArea();
//    		System.out.println("a = " + a);
 	    	p = pl.getPerimeter();
// 	    	System.out.println("p = " + p);
			c = pl.getCompacity();
//			System.out.println("c = " + c);
			x = pl.getComplexity();
//			x = 0.0;
//			System.out.println("x = " + x);
			s = pl.getSimplexity();
//			System.out.println("s = " + s);
     		g = pl.getCG();
//     		System.out.println("g = " + g);

			if (n < npv.length)
				npv[n]++;

	     	total_vertices += n;
	     	total2_vertices += n*n;
     		total_area += a;
     		total2_area += a*a;
     		total_perimeter += p;
     		total2_perimeter += p*p;

			cnt_compacity++;
			total_compacity += c;
			total2_compacity += c*c;

			total_complexity += x;
			total2_complexity += x*x;
			
			total_simplexity += s;
			total2_simplexity += s*s;

	     	if (n < min_vertices)
     			min_vertices = n;
     		if (n > max_vertices)
     			max_vertices = n;

	     	if (a < min_area)
     			min_area = a;
     		if (a > max_area)
     			max_area = a;

	     	if (p < min_perimeter)
     			min_perimeter = p;
     		if (p > max_perimeter)
     			max_perimeter = p;

	     	if (c < min_compacity)
     			min_compacity = c;
     		if (c > max_compacity)
     			max_compacity = c;

	     	if (x < min_complexity)
     			min_complexity = x;
     		if (x > max_complexity)
     			max_complexity = x;
     		
	     	if (s < min_simplexity)
     			min_simplexity = s;
     		if (s > max_simplexity)
     			max_simplexity = s;
		}

     	var_vertices = (total2_vertices - (total_vertices*total_vertices)/i)/(i-1);
   		var_area = (total2_area - (total_area*total_area)/i)/(i-1);
   	 	var_perimeter = (total2_perimeter - (total_perimeter*total_perimeter)/i)/(i-1);
//		int ic = cnt_compacity;
//		var_compacity = (var_compacity - (total_compacity*total_compacity)/i)/(ic-1);
		var_compacity = (total2_compacity - (total_compacity*total_compacity)/i)/(i-1);
		var_complexity = (total2_complexity - (total_complexity*total_complexity)/i)/(i-1);
		var_simplexity = (total2_simplexity - (total_simplexity*total_simplexity)/i)/(i-1);

     	mean_vertices = total_vertices/i;
   		mean_area = total_area/i;
   		mean_perimeter = total_perimeter/i;
//		mean_compacity = total_compacity/ic;
		mean_compacity = total_compacity/i;
		mean_complexity = total_complexity/i;
		mean_simplexity = total_simplexity/i;
	}

	public int[] getNpv()
	{
		return npv;
	}

	public double getTotal_vertices()
	{
		return total_vertices;
	}

	public double getTotal2_vertices()
	{
		return total2_vertices;
	}

	public double getMin_vertices()
	{
		return min_vertices;
	}

	public double getMax_vertices()
	{
		return max_vertices;
	}

	public double getMean_vertices()
	{
		return mean_vertices;
	}

	public double getVar_vertices()
	{
		return var_vertices;
	}

	public double getChi2_vertices()
	{
		return chi2_vertices;
	}

	public double getChi2_inner_vertices()
	{
		return chi2_inner_vertices;
	}

	public double getTotal_area()
	{
		return total_area;
	}

	public double getTotal2_area()
	{
		return total2_area;
	}

	public double getMin_area()
	{
		return min_area;
	}

	public double getMax_area()
	{
		return max_area;
	}

	public double getMean_area()
	{
		return mean_area;
	}

	public double getVar_area()
	{
		return var_area;
	}

	public double getTotal_perimeter()
	{
		return total_perimeter;
	}

	public double getTotal2_perimeter()
	{
		return total2_perimeter;
	}

	public double getMin_perimeter()
	{
		return min_perimeter;
	}

	public double getMax_perimeter()
	{
		return max_perimeter;
	}

	public double getMean_perimeter()
	{
		return mean_perimeter;
	}

	public double getVar_perimeter()
	{
		return var_perimeter;
	}

	public double getTotal_compacity()
	{
		return total_compacity;
	}

	public double getTotal2_compacity()
	{
		return total2_compacity;
	}

	public double getMin_compacity()
	{
		return min_compacity;
	}

	public double getMax_compacity()
	{
		return max_compacity;
	}

	public double getMean_compacity()
	{
		return mean_compacity;
	}

	public double getVar_compacity()
	{
		return var_compacity;
	}

	public double getCnt_compacity()
	{
		return cnt_compacity;
	}

	public double getTotal_complexity()
	{
		return total_complexity;
	}

	public double getTotal2_complexity()
	{
		return total2_complexity;
	}

	public double getMin_complexity()
	{
		return min_complexity;
	}

	public double getMax_complexity()
	{
		return max_complexity;
	}

	public double getMean_complexity()
	{
		return mean_complexity;
	}

	public double getVar_complexity()
	{
		return var_complexity;
	}

	public double getTotal_simplexity()
	{
		return total_simplexity;
	}

	public double getTotal2_simplexity()
	{
		return total2_simplexity;
	}

	public double getMin_simplexity()
	{
		return min_simplexity;
	}

	public double getMax_simplexity()
	{
		return max_simplexity;
	}

	public double getMean_simplexity()
	{
		return mean_simplexity;
	}

	public double getVar_simplexity()
	{
		return var_simplexity;
	}

	public int getSize()
	{
		return np;
	}
	
	public PolygonA getPoly(int i)
	{
		return pa[i];
	}
	
	public RInfo getRInfo() {
		return ri;
	}

	public void setVertex(Pointd p, int i){}
	public Pointd getVertex(int i){return null;}
	public void add(Pointd p){}
	public void drawDecomp(Graphics g, RPanel rp){}
	public void drawHull(Graphics g, RPanel rp){}
	public void findHull(){}
	public Hull getHull(){return null;}
	public void reSize(){}
	public int size(){return np;}
	public int sizeDecomp(){return 0;}
	public int sizeHull(){return 0;}
	public void readData(BufferedReader in) throws IOException{}
	public void scaleToUnitSquare(){}
	public void drawSpecial(Graphics g, RPanel rp){}
}