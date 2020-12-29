package org.edisonwj.rapport;

/**
 * RPolygonMono defines the class of monotone polygons
 */

import java.io.*;
import java.text.*;
import java.util.*;

class RPolygonMono extends PolygonA implements RapportDefaults
{
	protected poInfo ri;				/* Link to RInfo */

	public RPolygonMono(poInfo ri, Rand mr)	/* Generate monotone polygon */
	{
		super(ri.nv);
		this.ri = ri;
		
		if (!genPolygon(ri, mr))
		{
			v = null;
			nv = 0;
		}
		if (ri.color)
			setColor();
	}

	private boolean genPolygon(poInfo ri, Rand mr)	/* Generate polygon */
	{
		int n = ri.nv;

		/* Randomize n */
		if (ri.random && n > 10)
			n = mr.uniform(10,n);

		/* Assume line of monotonicity i sthe x-axis */
		/* Generate random points */
		Pointd[] u = new Pointd[n];
		for (int i = 0; i < n; i++)
			u[i] = new Pointd(mr.uniform(), mr.uniform());

		/* Sort so minimum x is in v[0] and maximum is in v[n-1] */
		sortByX(u);

		/* Form chains */
		ArrayList cl = new ArrayList();
		ArrayList cr = new ArrayList();
		cl.add(u[0]);
		for (int i = 1; i < n-1; i++)
		{
			if (Geometry.left(u[0],u[n-1],u[i]))
					cl.add(u[i]);
				else
					cr.add(u[i]);
		}
		cl.add(u[n-1]);

		/* Merge chains */
		for (int i = 0; i < cr.size(); i++)
			cl.add(cr.get(cr.size()-i-1));

		v = new Pointd[n];
		for (int i = 0; i < n; i++)
			v[i] = (Pointd)cl.get(i);
		nv = n;

		/* Check for valid polygon */
 		if (checkPolygon())
 		{
  			computeMetrics();
  			return true;
		}
  		else
			return false;
	}

	private void sortByX(Pointd[] v)
	{
		Arrays.sort(v, new Comparator() {
			public int compare(Object a, Object b)
			{
				if ( ((Pointd)a).equals((Pointd)b) )
					return 0;
				else if ( ((Pointd)a).xless((Pointd)b) )
					return -1;
				else
					return 1;
			}
		});
	}

	public void writeData(boolean draw3d, PrintWriter out) throws IOException
	{
//		System.out.println("RPolygonMono writeData - type: " + type);
		
		String lineOut = "";
		String parmsOut = "";
		
		if (xcolor != null)
			parmsOut += "Color " + xcolor + " ";
		else
			parmsOut += "Color None ";
		
		if (ri != null) {		
			if (ri.random)
				parmsOut += "Randomize number of vertices, ";
			parmsOut += "Max.-number-of-vertices = " + ri.Snv;
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
					
			String vertexFlag = "true";
			String drawMode = "LINE";
			String color = "0xfaebd7ff";
						
			if (xcolor != null) {
				vertexFlag = "false";
				drawMode = "FILL";
				color = xcolor;
			}

			lineOut = "Polygon: ";
			String fmt = DRAW3D_NUMBER_FORMAT;
			for (int i = 0; i < nv; i++) {
				lineOut += 	String.format(fmt,v[i].getx()) + ", " +
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
					lineOut +=  String.format(fmt, tr[i].B.getx()) + ", " +
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
				out.println(  nf.format(v[i].getx()) + ","
								+ nf.format(v[i].gety()) );
		}
	}
	
	public poInfo getRInfo() {
		return ri;
	}
}