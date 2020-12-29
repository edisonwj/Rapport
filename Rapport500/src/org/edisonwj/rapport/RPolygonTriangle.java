package org.edisonwj.rapport;

/**
 * RPolygonTriangle defines the class of spiral polygons
 */

import java.io.*;
import java.text.*;
import java.util.*;

class RPolygonTriangle extends PolygonA implements RapportDefaults
{
	protected trInfo ri;				/* Link to RInfo */
	
	public RPolygonTriangle(trInfo ri, Rand mr)	/* Generate polygon using triangle method */
	{
		super(ri.nv);
		this.ri = ri;
		boolean success = false;
		
		if (ri.hash)
		{
			success = genPolyH(ri, mr);
//			scaleToUnitSquare();
		}
		else
		{
			success = genPoly(ri, mr);
//			scaleToUnitSquare();
		}

		if ( !success )
		{
			v = null;
			nv = 0;
		}
		else
		{
			if (ri.color)
				setColor();
		}
	}

	private boolean genPoly(trInfo tri, Rand mr)	/* Generate polygon */
	{
  		int n = tri.nv;

  		/* Randomize n */
		if (tri.random && n > 10)
			n = mr.uniform(10,n);

		int i, k;
		int num_points_generated = 0;
		double r, theta;
		boolean successful;

		Pointd a, b, c;
		EdgeR edge, e1, e2, e3;
		LinkedList edge_list = new LinkedList();
//		PolygonA pa = new PolygonA(n);

		/* First we generate a triangle */
		a = new Pointd(0, 0);
		r = mr.bounded_exponential(tri.radial_exp_rate);
		theta = mr.uniform(0.0, 2.0*Math.PI);
		b = new Pointd(r * Math.cos(theta), r * Math.sin(theta));
		e1 = new EdgeR(a, b);

		if (tri.debug >= 3)
			System.out.println("Initial edge:\n" + e1);

		/* Use the two points to generate the third */
		c = e1.random_point_from_edge(tri.bounded, tri.radial_exp_rate, mr);

		if (tri.debug >= 3)
			System.out.println("Initial third point: " + c);

		/* Next, place the 3 edges in the edge list */
//		edge_list.insertTail(e1);
		edge_list.addLast(e1);
		e2 = new EdgeR(b,c);
		e3 = new EdgeR(c,a);
//		edge_list.insertTail(e2);
//		edge_list.insertTail(e3);
		edge_list.addLast(e2);
		edge_list.addLast(e3);

		if (tri.debug >= 3)
			  System.out.println("Edge List:\n" + edge_list);

		/* Now pick edges randomly and create triangles */
		num_points_generated = 3;
		while (num_points_generated < n)
		{
			if (tri.debug >= 3)
				System.out.println("Generating point # " + (int)(num_points_generated+1));
			successful = false;
			k = tri.max_attempts;
			while ( (k != 0) && (!successful) )
			{
				if (tri.debug >= 3)
					System.out.println("Attempt k= " + k);

				successful = expand_random_edge(tri, mr, edge_list);

				k--;
			}
			if (tri.debug >= 3)
				System.out.println("Edge List:\n" + edge_list);

			if (k==0)
			{
	  			System.out.println("Triangle method failure: giving up after "
											+ tri.max_attempts + " attempts");
//	   		pa = null;
//	   		return pa;
			return false;
			}
			else
				num_points_generated++;
		}

		/* Edges have been generated. Create the polygon. */
//		if (edge_list.length() != n)
		if (edge_list.size() != n)
		{
			System.out.println("ERR in triangle_method: n=" + n
									+ ", edgelist.numitems=" + edge_list.size());
//									+ ", edgelist.numitems=" + edge_list.length());
			return false;
		}

//		edge = (EdgeR)edge_list.removeHead();
		edge = (EdgeR)edge_list.removeFirst();
		this.addVertex(edge.getv1());
		for (i=1; i<n; i++)
		{
		  this.addVertex(edge.getv2());
//		  edge = (EdgeR)edge_list.removeHead();
		  edge = (EdgeR)edge_list.removeFirst();
		}
		reSize(n);

   		if (this.checkPolygon())
  		{
 			scale(0.5);
 			translate(0.5, 0.5);
  			return true;
		}
  		else
			return false;


//		edge = (EdgeR)edge_list.removeHead();
//		pa.addVertex(edge.getv1());
//		for (i=1; i<n; i++)
//		{
//		  pa.addVertex(edge.getv2());
//		  edge = (EdgeR)edge_list.removeHead();
//		}
//
//		if (!pa.check_polygon())
//			pa = null;
//
// 		return pa;
	}

	public boolean expand_random_edge(trInfo tri, Rand mr, LinkedList edge_list)
	{
		EdgeR e1, e2, e3,e4, et;
		int i=1;
		int j, n;
		Pointd v1, v2, u1, u2, c;

		/* Get a random edge number */
		n = edge_list.size();
		if (n > 0)
			i = mr.uniform(1, n-1);
		else
		  	System.out.println("ERR:get_random_edge: num_items<1: num_items=" + n);
//		e1 = (EdgeR)edge_list.getItem(i);
		e1 = (EdgeR)edge_list.get(i);

		if (tri.debug >= 3)
			System.out.println("Edge selected: e[" + i + "]= " + e1);

		v1 = e1.getv1();
		v2 = e1.getv2();

		/* Next, generate a point randomly using this edge */
		c = e1.random_point_from_edge(tri.bounded, tri.radial_exp_rate, mr);
		if (tri.debug >= 3)
			System.out.println("Point selected: " + c);

		/* The new potential edges are e3=(v1,c) and e4=(c,v2). We need
		   to test these edges against the others, a tedious task */
		e3 = new EdgeR(v1, c);
		e4 = new EdgeR(c, v2);
		if (tri.debug >= 3)
			System.out.println("New edges:\n" + e3 + "\n" + e4);

//		Enumeration e = edge_list.elements();
//		while (e.hasMoreElements())
//		{
//			et = (EdgeR)e.nextElement();
//			if ((et != e1) &&

		ListIterator it = edge_list.listIterator();
		while (it.hasNext())
		{
			et = (EdgeR)it.next();
//			System.out.println("RPolygonTriangle expand_random_edge et:" + et.toString());
			if ((et != e1) &&
				( ! Geometry.valid_edges(e3, et) ||
				  ! Geometry.valid_edges(e4, et) ))
			{
				if (tri.debug >=3)
					System.out.println("Valid_edge check failed:\n" + e3 + "\n" + e4 + "\n" + et);
				return false;
			}
		}

		/* If we've reached here, it means we have a valid point.
			This means we delete e1 and store e3=(v1,c) and e4=(c,v2) in the list */
//		edge_list.insertItem(e3);
//	  	edge_list.insertItem(e4);
//	  	edge_list.removeItem();
		edge_list.add(e3);
		edge_list.add(e4);
		edge_list.remove(e1);

		return true;
	}

	public boolean genPolyH(trInfo tri, Rand mr)
	{
		int n = tri.nv;

  		/* Randomize n */
  		if (tri.random && n > 10)
			n = mr.uniform(10,n);

		int i, k;
		int num_points_generated = 0;
		double r, theta;
		boolean successful;

		Pointd a, b, c;
		EdgeR edge, e1, e2, e3;
		edgeList edge_list = new edgeList(n);
//		PolygonA pa = new PolygonA(n);

		/* First we generate a triangle */
		a = new Pointd(0, 0);
		r = mr.bounded_exponential(tri.radial_exp_rate);
		theta = mr.uniform(0.0, 2.0*Math.PI);
		b = new Pointd(r * Math.cos(theta), r * Math.sin(theta));
		e1 = new EdgeR(a, b);

		if (tri.debug >= 3)
			System.out.println("Initial edge:\n" + e1);

		/* Use the two points to generate the third */
		c = e1.random_point_from_edge(tri.bounded, tri.radial_exp_rate, mr);

		if (tri.debug >= 3)
			System.out.println("Initial third point: " + c);

		/* Next, place the 3 edges in the edge list */
		edge_list.put(e1);
		e2 = new EdgeR(b,c);
		e3 = new EdgeR(c,a);
		edge_list.put(e2);
		edge_list.put(e3);

		if (tri.debug >= 3)
			  System.out.println("Edge List:\n" + edge_list);

		/* Now pick edges randomly and create triangles */
		num_points_generated = 3;
		while (num_points_generated < n)
		{
			if (tri.debug >= 3)
				System.out.println("Generating point # " + (int)(num_points_generated+1));
			successful = false;
			k = tri.max_attempts;
			while ( (k != 0) && (!successful) )
			{
				if (tri.debug >= 3)
					System.out.println("Attempt k= " + k);

				successful = expand_random_edgeH(tri, mr, edge_list);
				k--;
			}
			if (tri.debug >= 3)
				System.out.println("Edge List:\n" + edge_list);

			if (k==0)
			{
	  			System.out.println("Triangle method failure: giving up after "
											+ tri.max_attempts + " attempts");
				return false;
//	   			pa = null;
//	   			return pa;
			}
			else
				num_points_generated++;
		}

		/* Edges have been generated. Create the polygon. */
		if (edge_list.length() != n)
		{
			System.out.println("ERR in triangle_method: n=" + n
									+ ", edgelist.numitems=" + edge_list.length());
			return false;
		}

		Pointd [] vertices = edge_list.getvertices();
		for (i = 0; i < vertices.length; i++)
			this.addVertex(vertices[i]);
		reSize(n);

   		if (this.checkPolygon())
  		{
 			scale(0.5);
// 			translate(0.5, 0.5);
  			return true;
		}
  		else
			return false;

//		if (!pa.check_polygon())
//			pa = null;
//
// 		return pa;
	}


	public boolean expand_random_edgeH(trInfo tri, Rand mr, edgeList edge_list)
	{
		EdgeR e1, e2, e3,e4, et;
		int i=1;
		int j, n;
		Pointd v1, v2, u1, u2, c;

		/* Get a random edge */
		n = edge_list.length();
		e1 = null;
		if (n > 0)
			e1 = edge_list.random();
		else
		  	System.out.println("ERR:get_random_edge: num_items<1: num_items=" + n);

		if (tri.debug >= 3)
			System.out.println("Edge selected: e[" + i + "]= " + e1);

		v1 = e1.getv1();
		v2 = e1.getv2();

		/* Next, generate a point randomly using this edge */
		c = e1.random_point_from_edge(tri.bounded, tri.radial_exp_rate, mr);
		if (tri.debug >= 3)
			System.out.println("Point selected: " + c);

		/* The new potential edges are e3=(v1,c) and e4=(c,v2). We need
		   to test these edges against the others, a tedious task */
		e3 = new EdgeR(v1, c);
		e4 = new EdgeR(c, v2);
		if (tri.debug >= 3)
			System.out.println("New edges:\n" + e3 + "\n" + e4);

		Enumeration e = new edgeEnumeration(edge_list);
		while (e.hasMoreElements())
		{
			et = (EdgeR)e.nextElement();
			if ((et != e1) &&
				( ! Geometry.valid_edges(e3, et) ||
				  ! Geometry.valid_edges(e4, et) ))
			{
				if (tri.debug >=3)
					System.out.println("Valid_edge check failed:\n" + e3 + "\n" + e4 + "\n" + et);
	   			return false;
			}
		}

		/* If we've reached here, it means we have a valid point.
			This means we delete e1 and store e3=(v1,c) and e4=(c,v2) in the list */
		edge_list.replace(e1, c);

		return true;
	}
	
	public void writeData(boolean draw3d, PrintWriter out) throws IOException
	{
//		System.out.println("RPolygonTriangle (Spiral) writeData - type: " + type);
		
		String lineOut = "";
		String parmsOut = "";
		
		if (xcolor != null)
			parmsOut += "Color " + xcolor + " ";
		else
			parmsOut += "Color None ";
		
		if (ri != null) {			
			if (ri.bounded)
				parmsOut += "Bounded ";
			if (ri.hash)
				parmsOut += "Hash ";
			if (ri.random)
				parmsOut += "Randomize number of vertices ";
			parmsOut += "Max.-number-of-vertices = " + ri.Snv + " Radial-exp.-rate = " + ri.Sradial_exp_rate;
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
	
	public trInfo getRInfo() {
		return ri;
	}
}