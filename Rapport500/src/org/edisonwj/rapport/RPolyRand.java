package org.edisonwj.rapport;

/**
 * RPolyRand defines the general class of polygons determined
 * by random sets of points.
 */

import java.awt.*;
import java.io.*;
import java.text.NumberFormat;
import java.util.*;

class RPolyRand extends RObject implements RapportDefaults
{
	protected pfInfo ri;						/* Link to RInfo */
	protected Vector rv;						/* Vector of generated polygons */
	protected Rand mr;
	protected Rand mr2;
	protected RPanel rp;
		
	public RPolyRand(pfInfo ri, Rand mr, Rand mr2, RPanel rp)		/* Constructor for random point polygons creation */
	{
		super();
		this.ri = ri;
		this.mr = mr;
		this.mr2 = mr2;
		this.rp = rp;
		this.mr = new Rand(mr.getSeed());

//		System.out.println("RPolyRand(pfInfo ri, Rand mr, Rand mr2, RPanel rp) constructor: " +
//							this.hashCode() +
//							", ri: " + ri.hashCode());
		
		genRPolyRand(ri, mr, mr2, rp);
	}
	
	private void genRPolyRand(pfInfo ri, Rand mr, Rand mr2, RPanel rp) {
		rv = new Vector(ri.numb_objects);
		if (ri.a)
		{
//			ri.title += "-Enumeration Generation Convex";
			RPolyRandA pr = new RPolyRandA(ri, mr, mr2);
			rv = pr.genRPolyRandA();
//			System.out.println("RPolyRand genRPolyRandA complete - rv: " + rv.hashCode() +
//								", rv.size: " + rv.size() + " convex polygons");		
		}
		else if (ri.b)
		{
//			ri.title += "-Enumeration Generation Simple, Alpha = " + ri.Salpha;
			RPolyRandB pr = new RPolyRandB(ri, mr, mr2);
			rv = pr.genRPolyRandB();
//			System.out.println("RPolyRand genRPolyRandB complete - rv: " + rv.hashCode() +
//								", rv.size: " + rv.size() + " simple polygons");
		}
		else if (ri.c)
		{
//			ri.title += "-Convex Generation Mitchell";
			RPolyRandC pr = new RPolyRandC(ri, mr, mr2, rp);
			rv = pr.genRPolyRandC();
//			System.out.println("RPolyRand genRPolyRandC complete - rv: " + rv.hashCode() +
//								", rv.size: " + rv.size() + " convex polygons");
		}

		else if (ri.d)
		{
//			ri.title += "-Count Convex Mitchell";
			RPolyRandD pr = new RPolyRandD(ri, mr, mr2);
			rv = pr.genRPolyRandD();
//			System.out.println("RPolyRand RPolyRandD - complete - rv: " + rv.hashCode());
		}

		else if (ri.e)
		{
//			ri.title += "-Count K-gons Mitchell";
			RPolyRandE pr = new RPolyRandE(ri, mr, mr2);
			rv = pr.genRPolyRandE();
//			System.out.println("RPolyRand RPolyRandE - complete - rv: " + rv.hashCode());
		}

		else if (ri.f)
		{
//			ri.title += "-Count Convex Mitchell Improved";
			RPolyRandF pr = new RPolyRandF(ri, mr, mr2);
			rv = pr.genRPolyRandF();
//			System.out.println("RPolyRand RPolyRandF - complete - rv: " + rv.hashCode());
		}

//		int[] shapeCounts = new int[10];
//		i = 0;
//		Enumeration e = rv.elements();
//		while (e.hasMoreElements())
//		{
//			i++;
// 	  		PolygonA pa = (PolygonA)e.nextElement();
//
//	   		String shape = pa.getShape();
//     		shapeCounts[0]++;
//     		if (shape == "T")
//     			shapeCounts[1]++;
//     		else if (shape == "C")
//     			shapeCounts[2]++;
//     		else if (shape == "M")
//     			shapeCounts[3]++;
//     		else if (shape == "*")
//     			shapeCounts[4]++;
//     		else if (shape == "S")
//     			shapeCounts[5]++;
//     		else if (shape == "N")
//     			shapeCounts[6]++;
//
//			System.out.println("\nShape Counts");
//			for (i = 0; i < shapeCounts.length; i++)
//				System.out.println("shapeCounts[" + i + "]= " + shapeCounts[i]);
//			System.out.println();
//		}
	}

	public pfInfo getRInfo() {
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
	public int size(){return rv.size();}
	public int sizeDecomp(){return 0;}
	public int sizeHull(){return 0;}
	public void readData(BufferedReader in) throws IOException{}
	public void scaleToUnitSquare(){}
	public void drawSpecial(Graphics g, RPanel rp){}
	public void writeData(boolean draw3d, PrintWriter out) throws IOException{}
	public void draw(Graphics g, RPanel rp){}
}