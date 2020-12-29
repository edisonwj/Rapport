package org.edisonwj.rapport;

/**
 * RPolygonCell defines the cell polygon class
 */
		

import java.io.*;
import java.text.*;

class RPolygonCell extends PolygonA implements RapportDefaults
{
	protected pcInfo ri;					/* Link to RInfo */

	public RPolygonCell(pcInfo ri, Rand mr)	/* Generate cell polygon */
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

	private boolean genPolygon(pcInfo ri, Rand mr) {
//		System.out.println("RPolygonCell genPolygon(pcInfo ri, Rand mr) constructor: " + ri.hashCode() + 
//							", ri: " + ri.hashCode());
		RCellSet cl = new RCellSet((pcInfo)ri, mr);
		RPolyMapCell pm = new RPolyMapCell(cl, mr);
		PolygonA p = pm.getPoly(0);
		this.v = p.v;
		this.nv = v.length;
		this.type = TYPE_POLYGON_CELL;
		if (ri.color) {
			p.setColor(DEFAULT_POLYGON_COLOR);
			p.setXColor(DEFAULT_POLYGON_XCOLOR);
		}	
		shapeb[0] = true;
		shapeb[7] = true;
		shape = getShape();
		computeMetrics();
		
		return true;

	}
	
	public void writeData(boolean draw3d, PrintWriter out) throws IOException
	{
//		System.out.println("PolygonA writeData - type: " + type);
//		System.out.println("Title: " + ri.title);
		String lineOut = "";
		String parmsOut = "";
		String vertexFlag, drawMode, color;
		
		if (xcolor != null)
			parmsOut += "Color " + xcolor + " ";
		else
			parmsOut += "Color None ";
		
		if (ri != null) {
			if (ri.uniform)
				parmsOut += "Uniform";
			else if (ri.exponential)
				parmsOut += "Exponential";
			else if (ri.mixed)
				parmsOut += "Mixed";
			else
				parmsOut += "Unknown ";
			
			parmsOut += " Cell-row-size = " + ri.Snum_cells; 
		}
		else
			parmsOut = "Parameters unknown";
		
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
			
			lineOut = "DataGroup:";
			out.println(lineOut);
			lineOut = "Title1: " + ri.title;
			out.println(lineOut);
			lineOut = "Title2: " + parmsOut;
			out.println(lineOut);
			lineOut = "Polygon: ";

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
					lineOut +=  String.format(fmt, tr[i].A.getx()) + ", " +
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
	
	public pcInfo getRInfo() {
		return ri;
	}
}