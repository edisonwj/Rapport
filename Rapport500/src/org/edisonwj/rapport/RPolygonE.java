package org.edisonwj.rapport;

/**
 * RPolygonE defines the class of polyline generated polygons
 */

import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

class RPolygonE extends RObject implements RapportDefaults
{
	public class RPolygonEException
		extends RuntimeException
	{
		public RPolygonEException()
		{
			super();
		}

		public RPolygonEException(String s)
		{
			super(s);
		}
	}

	// List Exceptions
	protected RPolygonEException InvalidPolygonException =
		new RPolygonEException("Invalid Polygon");
	protected RPolygonEException InvalidStateException =
		new RPolygonEException("Invalid State");


	private static int count=0;	/* Id numbers for objects */

	protected CircularList xl;	/* List of indices of REdge objects composing polygon */
	protected int id;			/* Polygon id number */

	public RPolygonE()
	{
		type = TYPE_POLYGONE;
		nv = 0;
		xl = new CircularList();
		id = count++;
	}

	public RPolygonE(ArrayList al)
	{
		type = TYPE_POLYGONE;
		xl = new CircularList(al);
		nv = xl.size();
		id = count++;
	}
	
	public RPolygonE(ArrayList al, int type)
	{
		System.out.println("Set polyline type: " + type);
		this.type = type;
		xl = new CircularList(al);
		nv = xl.size();
		id = count++;
	}

	public static void reset()
	{
		count = 0;
	}

	public int getid()
	{
		return id;
	}

	public void orient(ArrayList el)
	{
		REdge re;
		for (int i = 0; i < xl.size(); i++)
		{
			re = (REdge)el.get(((Integer)xl.get(i)).intValue());
			if (re.getpoly1() != id)
				re.reverse();

		}

		REdge e0 = (REdge)el.get(((Integer)xl.get(0)).intValue());
		REdge e1 = (REdge)el.get(((Integer)xl.get(1)).intValue());
		if (!(e0.getv2()).equals(e1.getv1()))
		{
			xl.reverse();
//			System.out.println("reversed polygon list: " + this.toString());
		}

		if (!isValid(el))
			throw InvalidPolygonException;
	}

	/*
	 * Check for valid counter clockwise oriented edges and polygon
	 */
	public boolean isValid(ArrayList el)
	{
		REdge rea = null;
		REdge reb = null;
		Pointd v1 = null;
		Pointd v2 = null;
		rea = (REdge)el.get(((Integer)xl.get(0)).intValue());
//		System.out.println("re[0]= " + rea);
		if (rea.getpoly1() != id)
			return false;
		for (int i = 1; i < xl.size(); i++)
		{
			reb = (REdge)el.get(((Integer)xl.get(i)).intValue());
//			System.out.println("re[" + i + "]= " + reb);
			if (reb.getpoly1() != id)
				return false;
			v2 = rea.getv2();
			v1 = reb.getv1();
//			System.out.println("v2= " + v2 + " v1= " + v1);
			if (!v2.equals(v1))
				return false;
			rea = reb;
		}
		rea = (REdge)el.get(((Integer)xl.get(0)).intValue());
		if (!reb.getv2().equals(rea.getv1()))
			return false;
		else
			return true;
	}

	public CircularList getList()
	{
		return xl;
	}

	public void setCurrent(int id)
	{
		xl.setCurrent(new Integer(id));
	}

	public int next()
	{
		return (((Integer)xl.next()).intValue());
	}

	public int prev()
	{
		return (((Integer)xl.prev()).intValue());
	}

	public int get()
	{
		return (((Integer)xl.get()).intValue());
	}

	public int get(int i)
	{
		return (((Integer)xl.get(i)).intValue());
	}

	public int find(int id)
	{
		return xl.indexOf(new Integer(id));
	}

	public void add(int id)
	{
		xl.add(new Integer(id));
	}

	public void add(int i, int id)
	{
		xl.add(i,new Integer(id));
	}

	public void addNoAdvance(int id)
	{
		xl.addNoAdvance(new Integer(id));
	}

	public void addHead(int id)
	{
		xl.addHead(new Integer(id));
	}

	public void addTail(int id)
	{
		xl.addTail(new Integer(id));
	}

	public void insert(int id)
	{
		xl.insert(new Integer(id));
	}

	public void insert(int i, int id)
	{
		xl.insert(i,new Integer(id));
	}

	public void insertNoAdvance(int id)
	{
		xl.insertNoAdvance(new Integer(id));
	}

	public void set(int i, int id)
	{
		xl.set(i,new Integer(id));
	}

	public int remove(int i)
	{
		return ((Integer)xl.remove(i)).intValue();
	}

	public int size()
	{
		return xl.size();
	}

	/*
	 * Returns true if ordering of polygon edges is CCW.
	 * REdge at location eix in polygon has poly to right.
	 */
	public boolean isCCW(ArrayList elst, int eix)
	{
		/* Get base edge */
		REdge eb = (REdge)elst.get(this.get(eix));
		/* Get successor edge */
		REdge es = (REdge)elst.get(this.next());
		/* Move current back to eix */
		this.prev();

		/* Get vertices */
		/* For this polygon, eb is known to be oriented */
		/* such that v1->v2 has this poly to the right  */
		Pointd v1 = eb.getv1();
		Pointd v2 = eb.getv2();
		Pointd s1 = es.getv1();
		Pointd s2 = es.getv2();

		/* Check succession */
		if (v1.equals(s1) || v1.equals(s2))
		{
//			System.out.println("isCCW true");
			return true;
		}
		else if (v2.equals(s1) || v2.equals(s2))
		{
//			System.out.println("isCCW false");
			return false;
		}
		else
			throw InvalidStateException;
	}

	public String toString()
	{
		String result = "";
		for (int i = 0; i < xl.size(); i++)
			result += xl.get(i) + " ";
		return result;
	}

	public void draw(Graphics g, RPanel rp, Color c, boolean fill)
	{
		int x, y;

//		if ( nv > 0 )
//		{
//			Color saveC = g.getColor();
//			if (c != null)
//				g.setColor(c);
//
//			Polygon p = new Polygon();
//			for (int i = 0; i < nv; i++)
//			{
//				System.out.println("PolygonA draw v[" + i + "]= " + v[i]);
//				x = rp.iX(v[i].getx());
//				y = rp.iY(v[i].gety());
//				p.addPoint(x, y);
//				if ( !fill )
//					g.fillOval(x-2, y-2, 5, 5);
//			}
//			if ( fill )
//				g.fillPolygon(p);
//			else
//				g.drawPolygon(p);
//
//			if (!fill && getType() != 3)
//			{
//				cg = computeCG();
//				g.setColor(Color.green);
//				g.fillOval(rp.iX(cg.getx())-2, rp.iY(cg.gety())-2, 5, 5);
//			}
//			g.setColor(saveC);
//		}
	}

	public void draw(Graphics g, RPanel rp, Color c)
	{
		draw(g, rp, c, false);
	}

	public void draw(Graphics g, RPanel rp)
	{
		draw(g, rp, null);
	}

		public boolean isGend()
	{
//		if (nv > 0 && v != null && check_polygon())
			return true;
//		else
//			return false;

//		return (nv == v.length);
	}

	public void setVertex(Pointd p, int i){}
	public Pointd getVertex(int i){return null;}
	public void add(Pointd p){}
	public void drawDecomp(Graphics g, RPanel rp){}
	public void findHull(){}
	public Hull getHull(){return null;}
	public void reSize(){}
//	public int size(){return nv;}
	public int sizeDecomp(){return 0;}
	public int sizeHull(){return 0;}
	public void writeData(boolean draw3d, PrintWriter out) throws IOException{}
	public void readData(BufferedReader in) throws IOException{}
	public void scaleToUnitSquare() {}
	public void drawSpecial(Graphics g, RPanel rp) {}
}