package org.edisonwj.rapport;

/**
 * RPolyMap Cell defines the cell polymap class 
 */

import java.io.*;
import java.text.*;
import java.util.*;

class RPolyMapCell extends RPolyMap implements RapportDefaults
{
	protected cmInfo ri;						/* Link to RInfo */
		
	public RPolyMapCell(cmInfo ri, Rand mr)		/* Constructor for PolyMap Cell creation */
	{
		super();
		this.ri = ri;
		this.cr = new Rand(mr.getSeed());
//		System.out.println("RPolyMapCell(cmInfo ri, Rand mr) constructor: " + this.hashCode() +
//							", ri: " + ri.hashCode());
		
//		Generate cell matrix
		RCellSet cl = new RCellSet(ri, mr);
		
//		Generate PolyMap		
		genPolyMap(cl, mr);
		
//		Compute metrics and colors
		computeMetrics();
		if (ri.color)
			setColor();
	}
	
	public RPolyMapCell(RCellSet cl, Rand mr)	/* Constructor for cell polygon creation from cell set*/
	{
		super();
		this.cr = new Rand(mr.getSeed());
//		System.out.println("RPolyMapCell(RCellSet cl, Rand mr) constructor: " + this.hashCode() +
//							", cl: " + cl.hashCode());
		
//		Generate cell polygon in PolyMap with one member		
		genPolyMap(cl, mr);
	}
	
	public RPolyMapCell(BufferedReader in) throws IOException
	{
//		System.out.println("RPolyMapCell(Buffered Reader) constructor this: " + this.hashCode());
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
				pa[i] = new PolygonA(TYPE_POLYGON_CELL, in);
			nv = np;
			ri = new cmInfo(pa.length);
			ri.title = stitle;
//			System.out.println("RPolyMapCell this: " + this.hashCode());
//			System.out.println("RPolyMapCell ri: " + ri.hashCode() + ", ri.title: " + ri.title);
		}
		else
		{
			System.out.println("RPolyMap: Error constructing point distribution from file input");
			pa = null;
			nv = 0;
		}
	}
	
	private void genPolyMap(RCellSet cl, Rand mr)	/* Create PolyMap from generated cell matrix*/
	{
//		System.out.println("RPolyMapCell genPolyMap(RCellSet cl, Rand mr): " + cl.hashCode());
//		System.out.println("cmInfo: " + ri.hashCode() + ", title: " + ri.title);
		
		np = cl.getnp();
		pa = new PolygonA[np];

		int r;		/* row index */
		int c;		/* col index */
		Set entries;				/* set for enumerating hash entries */
		java.util.Iterator iter;	/* iterator for enumerating hash entries */

//		Find exterior cells/vertices
		ArrayList[] active = cl.getActive();
		ArrayList[] inactive = cl.getInactive();
		if ( np == 1 )
			inactive[0].addAll(active[0]);
		Pointd[][] v = new Pointd[np][] ;

//		Build hash table of exterior edges
		for (int i = 0; i < np; i++)
		{
			Map vmap = new HashMap();	/* Exterior edges */
			Map lmap = new HashMap();	/* Loop map */
			ArrayList vlst = new ArrayList();
			Pointd v0, v1, v2, v3;
			Pointd lowpoint = new Pointd(1.0, 1.0);
			RCell curc;
			int vcnt = 0;
			double xl, yl, xh, yh;
			double hw = 1.0/cl.getnc();

			for (int k = 0; k < inactive[i].size(); k++)
			{
				String sv;
				curc = (RCell)inactive[i].get(k);
				r = curc.getRow();
				c = curc.getCol();
				xl = c*hw;
				yl = r*hw;
				xh = (c+1)*hw;
				yh = (r+1)*hw;
				v0 = new Pointd(xl, yl);
				v1 = new Pointd(xh, yl);
				v2 = new Pointd(xh, yh);
				v3 = new Pointd(xl, yh);
				for (int j = 0; j < 4; j++)
				{
					if ( curc.isInterior(j) )
						continue;
					else
					{
						switch (j)
						{
							case 0:
								sv = v0.toString();
								if ( vmap.containsKey(sv) )
									lmap.put(sv, v1);
								else
								{
									vmap.put(sv, v1);
									vcnt++;
								}
//								System.out.println("case 0: " + v0 + ", " + v1);
								if (v0.gety() < lowpoint.gety() ||
									(v0.gety() == lowpoint.gety() &&
									 v0.getx() <  lowpoint.getx() ) )
									 lowpoint = v0;
							break;

							case 1:
								sv = v1.toString();
								if ( vmap.containsKey(sv) )
									lmap.put(sv, v2);
								else
								{
									vmap.put(sv, v2);
									vcnt++;
								}
//								System.out.println("case 1: " + v1 + ", " + v2);
								if (v1.gety() < lowpoint.gety() ||
									(v1.gety() == lowpoint.gety() &&
									 v1.getx() <  lowpoint.getx() ) )
									 lowpoint = v1;
							break;

							case 2:
								sv = v2.toString();
								if ( vmap.containsKey(sv) )
									lmap.put(sv, v3);
								else
								{
									vmap.put(sv, v3);
									vcnt++;
								}
//								System.out.println("case 2: " + v2 + ", " + v3);
								if (v3.gety() < lowpoint.gety() ||
									(v3.gety() == lowpoint.gety() &&
									 v3.getx() <  lowpoint.getx() ) )
									 lowpoint = v3;
							break;

							case 3:
								sv = v3.toString();
								if ( vmap.containsKey(sv) )
									lmap.put(sv, v0);
								else
								{
									vmap.put(sv, v0);
									vcnt++;
								}
//								System.out.println("case 3: " + v3 + ", " + v0);
								if (v0.gety() < lowpoint.gety() ||
									(v0.gety() == lowpoint.gety() &&
									 v0.getx() <  lowpoint.getx() ) )
									 lowpoint = v0;
							break;
						} /* End switch */
					} /* End else - not interior */
				} /* End for - interior edge check */
			} /* End for - cycle through cells of polygon */

//			System.out.println("Completed process to build hash entries for polygon " + i);

//			List hash entries
//			int count;
//			entries = vmap.entrySet();
//			iter = entries.iterator();
//			count = 0;
//			while ( iter.hasNext() )
//			{
//				Map.Entry entry = (Map.Entry)iter.next();
//				String key = (String)entry.getKey();
//				Pointd val = (Pointd)entry.getValue();
//				System.out.println("RPolyMapCell - Hash entry " + count++ + ": " + key + " => " + val);
//			}

//			List loop entries, if any
//			entries = lmap.entrySet();
//			iter = entries.iterator();
//			count = 0;
//			while ( iter.hasNext() )
//			{
//				Map.Entry entry = (Map.Entry)iter.next();
//				String key = (String)entry.getKey();
//				Pointd val = (Pointd)entry.getValue();
//				System.out.println("RPolyMapCell - Loop entry " + count++ + ": " + key + " => " + val);
//			}

//			Set starting vertex, least y, then x coordinate
			Pointd current = lowpoint;
			Pointd next;
			Pointd[] pt = new Pointd[vcnt];
			int pc = 0;

//			Cycle through hash entries, starting from lowpoint, to determine
//			polygon boundary and vertices, eliminating collinear vertices.
			do
			{
//				Remove intermediate collinear points
//				System.out.println("pc= " + pc + " " + current);
				if (pc > 1 &&
					Geometry.collinear(pt[pc-2], pt[pc-1], current) )
				{
//					System.out.println("Delete previous");
					pt[pc-1] = current;
				}
				else
				{
					pt[pc++] = current;
				}
//				System.out.println("pt[" + (pc-1) + "]= " + current);
//				System.out.println("contains " + vmap.containsKey(current.toString()));

				next = (Pointd)vmap.remove(current.toString());
//				System.out.println("RPolyMapCell next: " + next);
				if ( next == null )
				{
					System.out.println("RPolyMapCell loop condition *****");
					next = (Pointd)lmap.remove(current.toString());
					if ( next != null )
					{
						for (int m=0; m<pc; m++)
							System.out.println("RPolyMapCell pt["+m+"] = " + pt[m]);
						pc--;
						while ( !current.equals(pt[pc-1]) )
						{
							System.out.println("RPolyMapCell remove pt[" + pc + "]= " + pt[pc]);
							pc--;
						}
						pt[pc++] = next;
					}
					else
					{
						System.out.println("RPolyMapCell null exception *****");

						entries = vmap.entrySet();
						iter = entries.iterator();
						while ( iter.hasNext() )
						{
							Map.Entry entry = (Map.Entry)iter.next();
							String key = (String)entry.getKey();
							Pointd val = (Pointd)entry.getValue();
							System.out.println("RPolyMapCell - remaining hash entries: " + key + " => " + val);
						}

						for (int m=0; m<pc; m++)
							System.out.println("pt["+m+"] = " + pt[m]);
					}
				}
				current = next;

//			System.out.println("RPolyMapCell pt[" + (pc-1) + "]= " + pt[pc-1] + ", current= " + current);
			} while ( !pt[0].equals(current) && pc < vcnt);

//			Check for last collinearity
			if ( Geometry.collinear(pt[pc-2], pt[pc-1], pt[0] ) )
			{
//				System.out.println("Delete previous");
				pc = pc - 1;
			}

//			Create polygon vertex array
			v[i] = new Pointd[pc];
			System.arraycopy( pt, 0, v[i], 0, pc );
//			for (int j = 0; j < pc; j++)
//				System.out.println("v[" + i + "][" + j + "]=" + v[i][j]);

//			List polygon holes
			entries = vmap.entrySet();
			iter = entries.iterator();
			while ( iter.hasNext() )
			{
				Map.Entry entry = (Map.Entry)iter.next();
				String key = (String)entry.getKey();
				Pointd val = (Pointd)entry.getValue();
				System.out.println("RPolyMapCell - polygon hole edges: " + key + " => " + val);
			}

//			Add polygon to map
			pa[i] = new PolygonA(v[i],TYPE_POLYGON_CELL);
			pa[i].setRInfo(ri);
		}
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

	public void setNp(int in)
	{
		np = in;
	}

	public void setPa(PolygonA[] ipa)
	{
		pa = ipa;
	}

	public void setSeedColor(long iseed)
	{
		seedColor = iseed;
	}

	public double getTotal_vertices()
	{
		return total_vertices;
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

	public double getTotal_area()
	{
		return total_area;
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

	public int getSize()
	{
		return np;
	}

	public PolygonA getPoly(int i)
	{
		return pa[i];
	}
	
	public void writeData(boolean draw3d, PrintWriter out) throws IOException
	{
//		System.out.println("RPolyMapCell writeData - type: " + type);
		
		PolygonA pl;
		String lineOut = "";
		String parmsOut = "";
		String distribution = "";
		
		if (ri != null) {
			if (ri.uniform)
				distribution = "Uniform";
			else if (ri.exponential)
				distribution = "Exponential";
			else if (ri.mixed)
				distribution = "Mixed";
			else
				distribution = "Unknown";
			
			parmsOut = "Distribution= " + distribution + 
						", Number-of-polygons = " + ri.Snum_poly +
						", Cell-row-size = " + ri.Snum_cells;	
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

			for (int i = 0; i < np; i++) {
	   	  		pl = pa[i];
	     		pl.writeData(draw3d, out);
			}
		}
		else {	
			NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMaximumFractionDigits(DEFAULT_MAX_FRACTION_DIGITS);
			nf.setMinimumFractionDigits(DEFAULT_MIN_FRACTION_DIGITS);
			nf.setMinimumIntegerDigits(DEFAULT_MIN_INTEGER_DIGITS);
	
			out.println("#TYPE=" + type + " " + ri.title);
			out.println("#PARMS=" + parmsOut);
			out.println("#NP=" + np);
			for (int i = 0; i < np; i++) {
	   	  		pl = pa[i];
	     		pl.writeData(draw3d, out);
			}
		}
	}
	
	public cmInfo getRInfo() {
		return ri;
	}
}