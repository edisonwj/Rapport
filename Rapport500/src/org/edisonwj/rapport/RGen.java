package org.edisonwj.rapport;

/**
 * RGen creates selected object classes and
 * invokes object generation threads.
 */

import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

public class RGen extends Thread implements RapportDefaults
{
	public class RGenException
			extends RuntimeException
	{
		public RGenException()
		{
			super();
		}

		public RGenException(String s)
		{
			super(s);
		}
	}

	protected RGenException nowedge1 =
		new RGenException("no wedge1");

	protected RInfo ri;
	protected RPanel rp;
	protected RObject ro;
	protected Vector rv;
	protected File rf;
	protected Date timestamp;
	protected int groupType;

	protected double total_polymaps;
	protected double total_polygons;
	protected double total2_polygons;
	protected double mean_polygons;
	protected double var_polygons;
	protected double min_polygons;
	protected double max_polygons;

	protected double total_vertices;
	protected double total2_vertices;
	protected double mean_vertices;
	protected double var_vertices;
	protected double min_vertices;
	protected double max_vertices;
	protected double chi2_vertices;
	protected double chi2_inner_vertices;
	protected double chi2_non_corner_vertices;

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

	protected int[] total_npv;
	protected int[] mean_npv;

	RGen(RInfo rinf)
	{
	//	System.out.println("RGen constructor: " + this.hashCode() +
	//			", sets rg.ri: " + rinf.hashCode());
		ri = rinf;
		timestamp = new Date();
	}

	RGen(File inputFile) throws IOException
	{
		System.out.println("Open file: " + inputFile.getPath());
		int ni = 0;									/* Number of objects */
		String[] typeTitle;
		rf = inputFile;
		BufferedReader in = new BufferedReader(new FileReader(rf));
		typeTitle = readType(in);
		if (typeTitle == null)
		{
			System.out.println("RGen: Invalid file input");
			return;
		}
		int gtype = Integer.parseInt(typeTitle[0]);	/* Group type */
		int type = Integer.parseInt(typeTitle[2]);	/* Object type */
		String gtitle = typeTitle[1];				/* Group title */
		String title = typeTitle[3];				/* Object title */
//		System.out.println("RGen File input option - type: " + type + ", title: " + title);
		if (gtype == TYPE_GROUP)							/* Group of objects */
			ni = readNI(in);
		else {
			System.out.println("ERR reading input file");
			return;
		}

		rv = new Vector(ni);									/* Vector of objects */
		for (int i = 0 ; i < ni; i++) {
//			System.out.println("RGen file input i: " + i);	
			ro = null;
			switch (type) {
				case TYPE_POLYGON:								/* Polygon */
				case TYPE_POLYGON_CELL:
					ro = (RObject) new PolygonA(type,in);
				break;
			
				case TYPE_POINT_SET:
					ro = (RObject)new RPointSet(in);			/* RPoint Set */
				break;
				
				case TYPE_LINEFIELD:							/* Poisson Line Field */
					ro = (RObject)new RLineField(in);			
				break;
				
				case TYPE_POLYLINE:								/* PolyLine */
					ro = (RObject)new RPolyLine(in);			
				break;
				
				case TYPE_RANDOM_WALK:							/* Random Walk */
					ro = (RObject)new RRandomWalk(in);			
				break;
				
				case TYPE_RECTANGLER2CR:			/* Lower left and Upper Right Corners */
					ro = (RectangleR2cr) new RectangleR2cr(in);
				break;
				
				case TYPE_RECTANGLER2CN:			/* Center and Half Side Extents */
					ro = (RectangleR2cn) new RectangleR2cn(in);
				break;
				
				case TYPE_RECTANGLER2CE:			/* Lower Left Corner and Side Extents */
					ro = (RectangleR2ce) new RectangleR2ce(in);
				break;
				
				case TYPE_SEGMENT_SET:		
					ro = (RObject)new RSegmentSet(in);			/* RSegment Set */
				break;
				
				case TYPE_CIRCLE:								/* Circle */
					ro = (RObject)new RCircle(in);			
				break;
				
				case TYPE_ELLIPSE:								/* Ellipse */
					ro = (RObject)new REllipse(in);			
				break;
				
				case TYPE_POLYMAP:								/* PolyMap */
					if (title.equals("PolyMap-Cell"))
						ro = new RPolyMapCell(in);
					
					else if (title.equals("PolyMap-Poisson"))
						ro = new RPolyMapPoisson(in);
					
					else if (title.equals("PolyMap-PolyLine"))
						ro = new RPolyMapPolyLines(in);
					
					else if (title.equals("PolyMap-Voronoi"))
						ro = new RPolyMapVoronoi(in);
					
					else {
						System.out.println("ERR unknonw title: " + title);
						break;
					}
					break;
			}
			ri = ro.getRInfo();
			ri.seed = (long) 0.0;
			ri.seed2 = (long) 0.0;
			ri.Sseed = "";
			ri.Sseed2 = "";
			rv.add(ro);
		}
	
		timestamp = new Date(rf.lastModified());
		in.close();
		JFrame f = new RFrame(this);
		f.setVisible(true);
	}

	public String[] readType(BufferedReader in) throws IOException
	{
		boolean more = true;
		String[] stt = new String[4];
		String s;
		StringTokenizer t;
		s = in.readLine();
		t = new StringTokenizer(s, "#=: \t\n\r");
		if ( s.charAt(0) == '#' && "TYPE".equals(t.nextToken()) ) {
			stt[0] = t.nextToken();
			stt[1] = t.nextToken();
			stt[2] = t.nextToken();
			stt[3] = t.nextToken();
			return stt;
		}
		else
			return null;
	}
	
	public int readNI(BufferedReader in) throws IOException
	{
		boolean more = true;
		String s;
		while (more)
		{
			s = in.readLine();
 			StringTokenizer t = new StringTokenizer(s, "#= \t\n\r");
			if ( s.charAt(0) == '#' && "NI".equals(t.nextToken()) )
				return Integer.parseInt(t.nextToken());
		}
		return -1;
	}

	public String getTitle()
	{
		return ri.title;
	}

	public RInfo getRI()
	{
		return ri;
	}

	public RObject getRO()
	{
		return ro;
	}

	public int getGroupType()
	{
		return groupType;
	}

	public void setRO(RObject ro)
	{
		this.ro = ro;
	}

	public void setRP(RPanel rp)
	{
		this.rp = rp;
	}

	public void setRF(File f)
	{
		this.rf = f;
	}

	public Date getTimestamp()
	{
		return timestamp;
	}

	public void pause()
	{
		if ( ri.pause > 100)
		{
			try
			{
				Thread.sleep(ri.pause);
			}
			catch (InterruptedException e){}
		}
	}

	public void computeMetrics()
	{
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
//		nf.setMinimumIntegerDigits(4);

		NumberFormat nf1 = NumberFormat.getNumberInstance();
		nf1.setMaximumFractionDigits(6);
		nf1.setMinimumFractionDigits(6);
		nf1.setMinimumIntegerDigits(1);

		NumberFormat nf2 = NumberFormat.getNumberInstance();
		nf2.setMaximumFractionDigits(0);
//		nf2.setMinimumFractionDigits(0);
//		nf2.setMinimumIntegerDigits(4);

		int mres = DEFAULTMRES;
		int[][] cell_vertices = new int[mres][mres];
		int[][] cell_cg = new int[mres][mres];
		int[] cell_area = new int[mres];
		int[] cell_perimeter = new int[mres];
//		int[] cell_compacity = new int[mres];
		int[] cell_compacity = new int[1000];
//		int[] cell_complexity = new int[mres];
		int[] cell_complexity = new int[1000];
//		int[] cell_simplexity = new int[mres];
		int[] cell_simplexity = new int[1000];

		int[] vertex_count = new int[101];

		double[] total_df = new double[6];

		int cnt;
		int i, j, k, l;
		double c, n, a, p, s, t, x, np;
		Pointd g, v;
		double gx, gy;
		double size = 1.0/mres;

		double min, max;
		double minval = -100000000.0;
		double maxval =  100000000.0;
		double min_x = minval;
		double max_x = maxval;
		double min_y = minval;
		double max_y = maxval;

		total_polymaps = 0.0;
		total_polygons = 0.0;
		total2_polygons = 0.0;
		mean_polygons = 0.0;
		var_polygons = 0.0;
		min_polygons = maxval;
		max_polygons = minval;

		total_vertices = 0.0;
		total2_vertices = 0.0;
		mean_vertices = 0.0;
		var_vertices = 0.0;
		min_vertices = maxval;
		max_vertices = minval;
		chi2_vertices = 0.0;
		chi2_inner_vertices = 0.0;
		chi2_non_corner_vertices = 0.0;

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

		total_npv = new int[16];
		mean_npv = new int[16];

		for (j = 0; j < total_npv.length; j++)
		{
			total_npv[j] = 0;
			mean_npv[j] = 0;
		}

		int[] npv;

		int[] shapeCounts = new int[10];
		int[] listSize = new int[12];

		groupType = ((RObject)rv.get(0)).getType();
		cnt = 0;
		Enumeration e = rv.elements();
		while (e.hasMoreElements())
		{
			cnt++;
//			System.out.println("RGen computeMetrics S0 groupType: " + groupType);

			if (groupType == TYPE_POLYGON || groupType == TYPE_POLYGON_CELL) /* Polygons */
			{
//				System.out.println("RGen computeMetrics S1 groupType: " + groupType);
				
				total_polygons = cnt;
				PolygonA pa = (PolygonA)e.nextElement();
				n = pa.size();

				boolean[] shapeb = pa.getShapeb();
				for (i = 0; i < shapeb.length; i++)
				{
					if (shapeb[i])
						shapeCounts[i]++;
//					System.out.println("RGen shapeb[" + i + "]: " + shapeb[i] +
//										", shapeCounts[" + i + "]: " + shapeCounts[i]);
				}
				if (shapeb[3] && shapeb[4])			/* if both monotone and star */
					shapeCounts[9]++;	/* count separately */

//				if (groupType != TYPE_POLYGON_CELL)
//				{
//					if (pa.getwlsize() > 0 && pa.getwlsize() < 11)
//						listSize[pa.getwlsize()]++;
//					else
//						System.out.println("wlsize exception = " + pa.getwlsize());
//				}

//	     		System.out.println("n = " + n);
				a = pa.getArea();
//	     		System.out.println("a = " + a);
				p = pa.getPerimeter();
//	 	    	System.out.println("p = " + p);
				c = pa.getCompacity();
//				System.out.println("c = " + c);
				x = pa.getComplexity();
//				System.out.println("x = " + x);
				s = pa.getSimplexity();
//				System.out.println("s = " + s);
				g = pa.getCG();
//	     		System.out.println("g = " + g);

	//			if (pa.isConvex())
	//				System.out.println("Convex polgon with compacity: " + c + " " + (cnt-1));
	//			else if (c <= .50)
	//				System.out.println("Non-convex polgon with compacity: " + c + " " + (cnt-1));

				vertex_count[pa.size()]++;
				total_vertices += n;
				total_area += a;
				total_perimeter += p;
				total2_vertices += n*n;
				total2_area += a*a;
				total2_perimeter += p*p;

	//			if (n > 5)
	//			{
	//				cnt_complexity++;
	//				cnt_compacity++;
	//				total_complexity += x;
	//				total_compacity += c;
	//				total2_complexity += x*x;
	//				total2_compacity += c*c;
	//			}

				total_complexity += x;
				total_compacity += c;
				total_simplexity += s;
				total2_complexity += x*x;
				total2_compacity += c*c;
				total2_simplexity += s*s;

	// 			Sum timing information
				long[] tdf = pa.getDf();
				if (tdf != null)
				{
					for (j = 0; j < tdf.length; j++)
						total_df[j] += tdf[j];
				}

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
				if (x > max_simplexity)
					max_simplexity = s;

	//			Find cell for center of gravity
				gx = g.getx();
				gy = g.gety();
				k = (int)Math.floor(gx/size);
				l = (int)Math.floor(gy/size);
				if (k >= mres)
					k = mres -1;
				if (l >= mres)
					l = mres - 1;
				if (k < 0)
					k = 0;
				if (l < 0)
					l = 0;
				cell_cg[k][l]++;

	//			Find cells for vertices
				for (j = 0; j < n; j++)
				{
					v = pa.getVertex(j);
					k = (int)Math.floor(v.getx()/size);
					l = (int)Math.floor(v.gety()/size);
	//				System.out.println("Cell counts: " + j
	//						+ " (" + k + ", " + l + ")"
	//						+ " (" + v.getx() + ", " + v.gety() + ")");
					if (k >= mres)
						k = mres -1;
					if (l >= mres)
						l = mres - 1;
					if (k < 0)
						k = 0;
					if (l < 0)
						l = 0;
					cell_vertices[k][l]++;
				}
			} /* End if groupType == TYPE_POLYGON || groupType == TYPE_POLYGON_CELL */

			else if (groupType == TYPE_POLYMAP) /* Polymaps */
			{
//				System.out.println("RGen computeMetrics S2 groupType: " + groupType);
				RPolyMap pm = (RPolyMap)e.nextElement();

				total_polymaps = cnt;
				np = pm.getSize();
				total_polygons += np;
				total2_polygons += np*np;
				if (min_polygons > np)
					min_polygons = np;
				if (max_polygons < np)
					max_polygons = np;

				total_area += pm.getTotal_area();
				total2_area += pm.getTotal2_area();
				min = pm.getMin_area();
				if (min_area > min)
					min_area = min;
				max = pm.getMax_area();
				if (max_area < max)
					max_area = max;

				total_perimeter += pm.getTotal_perimeter();
				total2_perimeter += pm.getTotal2_perimeter();
				min = pm.getMin_perimeter();
				if (min_perimeter > min)
					min_perimeter = min;
				max = pm.getMax_perimeter();
				if (max_perimeter < max)
					max_perimeter = max;

				total_compacity += pm.getTotal_compacity();
				total2_compacity += pm.getTotal2_compacity();
				min = pm.getMin_compacity();
				if (min_compacity > min)
					min_compacity = min;
				max = pm.getMax_compacity();
				if (max_compacity < max)
					max_compacity = max;

				total_complexity += pm.getTotal_complexity();
				total2_complexity += pm.getTotal2_complexity();
				min = pm.getMin_complexity();
				if (min_complexity > min)
					min_complexity = min;
				max = pm.getMax_complexity();
				if (max_complexity < max)
					max_complexity = max;

				total_simplexity += pm.getTotal_simplexity();
				total2_simplexity += pm.getTotal2_simplexity();
				min = pm.getMin_simplexity();
				if (min_simplexity > min)
					min_simplexity = min;
				max = pm.getMax_simplexity();
				if (max_simplexity < max)
					max_simplexity = max;

				total_vertices += pm.getTotal_vertices();
				total2_vertices += pm.getTotal2_vertices();
				min = pm.getMin_vertices();
				if (min_vertices > min)
					min_vertices = min;
				max = pm.getMax_vertices();
				if (max_vertices < max)
					max_vertices = max;

				chi2_vertices += pm.getChi2_vertices();

				npv = pm.getNpv();
				for (j = 0; j < total_npv.length; j++)
					total_npv[j] += npv[j];

//				if (n > 5)
//				{
//					cnt_compacity++;
//					total_compacity += c;
//				}

			} /* End if groupType == TYPE_POLYMAP */

		} /* End while enumeration */

//		Compute ranges and scales
		double arange = max_area - min_area;
		double asize = arange/mres;
		double prange = max_perimeter - min_perimeter;
		double psize = prange/mres;
		double crange = max_compacity - min_compacity;
		double csize = crange/mres;
		double xrange = max_complexity - min_complexity;
		double xsize = xrange/mres;
		double srange = max_simplexity - min_simplexity;
		double ssize = srange/mres;
		double diff = 0.0;

//		System.out.println( "arange: " + arange + "\n" +
//							"prange: " + prange + "\n" +
//							"crange: " + crange + "\n" +
//							"xrange: " + xrange + "\n" +
//							"srange: " + xrange + "\n");
//		System.out.println("RGen computeMetrics S3 groupType: " + groupType);

		if (groupType == TYPE_POLYGON || groupType == TYPE_POLYGON_CELL) /* Polygons */
		{
//			System.out.println("RGen computeMetrics S4 groupType: " + groupType);
//			int ic = cnt_compacity;

			t = total_polygons;
			mean_vertices = total_vertices/t;
			mean_area = total_area/t;
			mean_perimeter = total_perimeter/t;
			mean_complexity = total_complexity/t;
			mean_compacity = total_compacity/t;
			mean_simplexity = total_simplexity/t;

			var_vertices = (total2_vertices - (total_vertices*total_vertices)/t)/(t-1);
			var_area = (total2_area - (total_area*total_area)/t)/(t-1);
			var_perimeter = (total2_perimeter - (total_perimeter*total_perimeter)/t)/(t-1);
			var_complexity = (total2_complexity - (total_complexity*total_complexity)/t)/(t-1);
			var_compacity = (total2_compacity - (total_compacity*total_compacity)/t)/(t-1);
			var_simplexity = (total2_simplexity - (total_simplexity*total_simplexity)/t)/(t-1);

	//		Find cells for other metrics
			cnt = 0;
			e = rv.elements();
			while (e.hasMoreElements())
			{
				cnt++;

				PolygonA pa = (PolygonA)e.nextElement();
				a = pa.getArea();
				p = pa.getPerimeter();
				c = pa.getCompacity();
				x = pa.getComplexity();
				s = pa.getSimplexity();

				diff = a - min_area;
				k = diff > 0.0 ? ((int)Math.ceil(diff/asize))-1 : 0;
	//			System.out.println("k: " + k +
	//			" a: " + a +
	//			" min_area: " + min_area +
	//			" max_area: " + max_area +
	//			" arange: " + arange +
	//			" asize: " + asize);
				cell_area[k]++;

				diff = p - min_perimeter;
				k = diff > 0.0 ? ((int)Math.ceil(diff/psize))-1 : 0;
	//			System.out.println("k: " + k +
	//			" p: " + p +
	//			" min_perimeter: " + min_perimeter +
	//			" max_perimeter: " + max_perimeter +
	//			" prange: " + prange +
	//			" psize: " + psize);
				cell_perimeter[k]++;

	//			diff = c - min_compacity;
	//			k = diff > 0.0 ? ((int)Math.ceil(diff/csize))-1 : 0;
	//			System.out.println("k: " + k +
	//			" c: " + c +
	//			" min_compacity: " + min_compacity +
	//			" max_compacity: " + max_compacity +
	//			" crange: " + crange +
	//			" csize: " + csize);

	//			if (c < .9)
	//				k = (int)Math.floor(c/.1);
	//			else if (c >= .9 && c < .99 )
	//				k = (int)Math.floor((c-.9)/.01) + 9;
	//			else
	//				k = (int)Math.floor((c-.99)/.001) + 18;

				k = (int)Math.floor(c/.001);
				cell_compacity[k]++;

//				diff = x - min_complexity;
//				k = diff > 0.0 ? ((int)Math.ceil(diff/xsize))-1 : 0;
//				cell_complexity[k]++;

//				k = (int)Math.floor(x/.01);
				k = (int)Math.floor(x/.001);
				if (k < 0)
				{
					System.out.println("Negative k= " + k + ", x= " + x);
					k = 0;
				}
				cell_complexity[k]++;
//				System.out.println("k: " + k +
//				" x: " + x +
//				" min_complexity: " + min_complexity +
//				" max_complexity: " + max_complexity +
//				" xrange: " + xrange +
//				" xsize: " + xsize +
//				" cell_complexity[" + k + "]: " + cell_complexity[k]);

				diff = x - min_simplexity;
				k = diff > 0.0 ? ((int)Math.ceil(diff/ssize))-1 : 0;
//				System.out.println("k: " + k +
//				" s: " + s +
//				" min_simplexity: " + min_simplexity +
//				" max_simplexity: " + max_simplexity +
//				" xrange: " + xrange +
//				" xsize: " + xsize);
				cell_simplexity[k]++;

			} /* End while enumeration */
			
//			System.out.println("\nShape Counts");
//			for (i = 0; i < shapeCounts.length; i++)
//				System.out.println("shapeCounts[" + i + "]= " + shapeCounts[i]);
			   System.out.println();

			System.out.println("\nRGen Polygon totals");
			System.out.println("Total simple=\t\t" + shapeCounts[0]);
			System.out.println("Total triangle=\t\t"+ shapeCounts[1]);
			System.out.println("Total convex=\t\t"  + shapeCounts[2]);
			System.out.println("Total monotone=\t\t" + (shapeCounts[3]-shapeCounts[9]));
			System.out.println("Total star=\t\t" + (shapeCounts[4]-shapeCounts[9]));
			System.out.println("Total monostar=\t\t"+ shapeCounts[9]);
			System.out.println("Total spiral=\t\t"  + shapeCounts[5]);
			System.out.println("Total rectilinear=\t" + shapeCounts[7]);
			System.out.println("Total other=\t\t" + shapeCounts[6]);

			System.out.println("\nTiming Information");
//			System.out.println("isSimple= \t"  + nf2.format(total_df[0]) +
//								" \tpertest= " + nf1.format(total_df[0]/sumall));
			System.out.println("isConvex= \t"  + nf2.format(total_df[2]) +
								" \tpertest= " + total_df[2]/(shapeCounts[0]-shapeCounts[1]));
			System.out.println("isSpiral= \t"  + nf2.format(total_df[5]) +
								" \tpertest= " + total_df[5]/(shapeCounts[0]-shapeCounts[2]));
			System.out.println("isMonotone= \t"+ nf2.format(total_df[3]) +
								" \tpertest= " + total_df[3]/(shapeCounts[0]-shapeCounts[2]-shapeCounts[5]));
			System.out.println("isStar=     \t"+ nf2.format(total_df[4]) +
								" \tpertest= " + total_df[4]/(shapeCounts[0]-shapeCounts[2]-shapeCounts[5]));
			System.out.println();

//			System.out.println("\nWedge List Sizes");
//			for (i = 0; i < listSize.length; i++)
//				System.out.println("listSize[" + i + "]= " + listSize[i]);
//			System.out.println();

		} /* End if groupType == TYPE_POLYGON || groupType == TYPE_POLYGON_CELL */

		else if (groupType == TYPE_POLYMAP) /* Polymaps */
		{
//			System.out.println("RGen computeMetrics S5 groupType: " + groupType);
			t = total_polymaps;
			var_polygons = (total2_polygons - (total_polygons*total_polygons)/t)/(t-1);
			mean_polygons = total_polygons/t;
			chi2_vertices = chi2_vertices/t;

//			int ic = cnt_compacity;
			t = total_polygons;
			mean_vertices = total_vertices/t;
			mean_area = total_area/t;
			mean_perimeter = total_perimeter/t;
			mean_complexity = total_complexity/t;
			mean_compacity = total_compacity/t;
			mean_simplexity = total_simplexity/t;

			var_vertices = (total2_vertices - (total_vertices*total_vertices)/t)/(t-1);
			var_area = (total2_area - (total_area*total_area)/t)/(t-1);
			var_perimeter = (total2_perimeter - (total_perimeter*total_perimeter)/t)/(t-1);
			var_complexity = (total2_complexity - (total_complexity*total_complexity)/t)/(t-1);
			var_compacity = (total2_compacity - (total_compacity*total_compacity)/t)/(t-1);
			var_simplexity = (total2_simplexity - (total_simplexity*total_simplexity)/t)/(t-1);

			for (j = 0; j < total_npv.length; j++)
			{
				mean_npv[j] = total_npv[j]/(int)total_polymaps;
//				System.out.println(" prob(X=" + j + ")= " + total_npv[j]/total_polygons +
//									", total_npv[" + j + "]=" + total_npv[j] +
//									", mean_npv[" + j + "]=" + mean_npv[j]);
			}
		}
		
//		System.out.println("RGen computeMetrics S6 groupType: " + groupType);

		if (groupType == TYPE_POLYGON || groupType == TYPE_POLYGON_CELL) /* Polygon vertices chi2 */			
		{
//			System.out.println("RGen computeMetrics S7 groupType: " + groupType);
			double evert = total_vertices/mres/mres;
			double psum = 0.0;
			double ncsum = 0.0;
			diff=0.0;
			chi2_vertices = 0.0;
			for (i = 0; i < mres; i++)
			{
				for (j = 0; j < mres; j++ )
				{
	//				System.out.println(
	//					"cell_vertices[" + i + "][" + j + "] = " +
	//					nf.format(cell_vertices[i][j]));

					diff = cell_vertices[i][j] - evert;
					chi2_vertices += diff*diff/evert;

					if (i > 0 && i < mres-1)
					{
						if (j > 0 && j < mres-1)
							psum += cell_vertices[i][j];
					}

					if ((i == 0 || i == mres-1) && (j == 0 || j == mres-1))
						continue;
					else
						ncsum += cell_vertices[i][j];
				}
			}
	//		System.out.println("Chi2_vertices = " + chi2_vertices + "\n");

			evert = psum/(mres-2)/(mres-2);
	//		System.out.println("Inner vertices - psum: " + psum + " evert: " + evert);
			diff=0.0;
			chi2_inner_vertices = 0.0;
			for (i = 1; i < mres-1; i++)
				for (j = 1; j < mres-1; )
				{
	//				System.out.println(
	//					"cell_vertices[" + i + "][" + j + "] = " +
	//					nf.format(cell_vertices[i][j]) +
	//					"\t cell_vertices[" + i + "][" + (j+1) + "] = " +
	//					nf.format(cell_vertices[i][j+1]) );

					diff = cell_vertices[i][j] - evert;
					chi2_inner_vertices += diff*diff/evert;
					diff = cell_vertices[i][j+1] - evert;
					chi2_inner_vertices += diff*diff/evert;

					j +=2;
				}
	//		System.out.println("Chi2_inner_vertices = " + chi2_inner_vertices + "\n");


			evert = ncsum/(mres*mres - 4);
	//		System.out.println("Non-corner vertices - ncsum: " + ncsum + " evert: " + evert);
			diff=0.0;
			chi2_non_corner_vertices = 0.0;
			for (i = 0; i < mres; i++)
			{
				for (j = 0; j < mres; j++ )
				{
					if ((i == 0 || i == mres-1) && (j == 0 || j == mres-1))
						continue;
	//				System.out.println("cell_vertices[" + i + "][" + j + "] = " +
	//					nf.format(cell_vertices[i][j]));
					diff = cell_vertices[i][j] - evert;
					chi2_non_corner_vertices += diff*diff/evert;
				}
			}
	//		System.out.println("Chi2_non_corner_vertices = " + chi2_non_corner_vertices + "\n");

		} /* End if polygon chi2 */

	//		for (i = 0; i < mres; i++)
	//			for (j = 0; j < mres; )
	//			{
	//				System.out.println(
	//					"cell_cg[" + i + "][" + j + "] = " +
	//					nf.format(cell_cg[i][j]) +
	//					"\tcell_cg[" + i + "][" + (j+1) + "] = " +
	//					nf.format(cell_cg[i][j]) );
	//				j += 2;
	//			}
	//		System.out.println();

	//		System.out.println();
	//		for (i = 0; i < cell_complexity.length; i++)
	//			System.out.println("cell_complexity[" + i + "] = " + nf.format(cell_complexity[i]));

	//c		System.out.println();
	//c		for (i = 0; i < cell_compacity.length; i++)
	//c			System.out.println("cell_compacity[" + i + "] = " + nf.format(cell_compacity[i]));

	//n		System.out.println();
	//n		for (i=0; i < vertex_count.length; i++)
	//n			System.out.println("vertex_count[" + i + "]= " + vertex_count[i]);

	//		for (i = 0; i < mres; i++)
	//				"cell_area[" + i + "] = " + nf.format(cell_area[i]) +
	//				"  cell_perimeter[" + i + "] = " + nf.format(cell_perimeter[i]) +
	//				"  cell_compacity[" + i + "] = " + nf.format(cell_compacity[i]) +
	//				"  cell_complexity[" + i + "] = " + nf.format(cell_complexity[i]) +
	//				"  cell_simplexity[" + i + "] = " + nf.format(cell_simplexity[i]) );



	}

//	public void writeData(boolean draw3d, PrintWriter out) throws IOException
//	{
//		System.out.println("RGen writeData");
//		RObject ro;
//		
//		if (!draw3d) {
//			NumberFormat nf = NumberFormat.getNumberInstance();
//			nf.setMaximumFractionDigits(DEFAULT_MAX_FRACTION_DIGITS);
//			nf.setMinimumFractionDigits(DEFAULT_MIN_FRACTION_DIGITS);
//			nf.setMinimumIntegerDigits(DEFAULT_MIN_INTEGER_DIGITS);
//			out.println("#TYPE=" + TYPE_GROUP + " GROUP");			/* Group identifier */
//			out.println("#NI=" + rv.size());			/* Number of generations */
//		}
//		
//		for (int i = 0; i < rv.size(); i++)
//		{
//			ro = (RObject)rv.get(i);					/* Generated item */
//			System.out.println("RGen writeData - type: " + ro.type);
//			ro.writeData(draw3d, out);
//		}
// 	}

	public int getSize()
	{
		return rv.size();
	}

	public double getTotal_polymaps()
	{
		return total_polymaps;
	}

	public double getTotal_polygons()
	{
		return total_polygons;
	}

	public double getMean_polygons()
	{
		return mean_polygons;
	}

	public double getVar_polygons()
	{
		return var_polygons;
	}

	public double getMin_polygons()
	{
		return min_polygons;
	}

	public double getMax_polygons()
	{
		return max_polygons;
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

	public double getChi2_vertices()
	{
		return chi2_vertices;
	}

	public double getChi2_inner_vertices()
	{
		return chi2_inner_vertices;
	}

	public double getChi2_non_corner_vertices()
	{
		return chi2_non_corner_vertices;
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

	public int getCnt_compacity()
	{
		return cnt_compacity;
	}

	public double getTotal_complexity()
	{
		return total_complexity;
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

}

class ptGen extends RGen			/* Generate point sets */
{
	private Rand mr;

	ptGen(ptInfo ri)
	{
		super(ri);
		rv = new Vector(ri.numb_objects);
		mr = new Rand(ri.seed);

		JFrame f = new RFrame(this);
	  	f.setVisible(true);
	}

	public void run()
	{
		int i,j;
		ptInfo ri = (ptInfo) getRI();

		for (i = 0; i < ri.numb_objects; i++)
		{
			for (j = 0; j < ri.max_attempts; j++)
			{
				RPointSet pt = new RPointSet(ri, mr);
				if (pt.isGend())
				{
					rv.add(pt);
					break;
				}
				else
					System.out.println("RGen ptGen: attempt failed # " + i + " " + j);
			}
		}
		System.out.println("RGen ptGen complete");
	}
}

class reGen extends RGen			/* Generate edges */
{
	private Rand mr;

	reGen(reInfo ri)
	{
		super(ri);
		rv = new Vector(ri.numb_objects);
		mr = new Rand(ri.seed);

		JFrame f = new RFrame(this);
	  	f.setVisible(true);
	}

	public void run()
	{
		int i,j;
		reInfo ri = (reInfo) getRI();

		for (i = 0; i < ri.numb_objects; i++)
		{
			for (j = 0; j < ri.max_attempts; j++)
			{
				REdge re = new REdge(new Pointd(0,0), new Pointd(1,1));
				if (re.isGend())
				{
					rv.add(re);
					break;
				}
				else
					System.out.println("RGen reGen: attempt failed # " + i + " " + j);
			}
		}
		System.out.println("RGen reGen complete");
	}
}

class lnGen extends RGen			/* Generate line segment sets */
{
	private Rand mr;

	lnGen(lnInfo ri)
	{
		super(ri);
		rv = new Vector(ri.numb_objects);
		mr = new Rand(ri.seed);

		JFrame f = new RFrame(this);
	  	f.setVisible(true);
	}

	public void run()
	{
		int i,j;
		lnInfo ri = (lnInfo) getRI();

		for (i = 0; i < ri.numb_objects; i++)
		{
			for (j = 0; j < ri.max_attempts; j++)
			{
				RSegmentSet ln = new RSegmentSet(ri, mr);
				RHashtable ht = new RHashtable();
				ht.addAll(ln);

				if (ln.isGend())
				{
					rv.add(ln);
					break;
				}
				else
					System.out.println("RGen lnGen run: attempt failed # " + i + " " + j);
			}
		}
		System.out.println("RGen lnGen complete");
	}
}

class lpGen extends RGen			/* Generate polylines */
{
	private Rand mr;

	lpGen(lpInfo ri)
	{
		super(ri);
		rv = new Vector(ri.numb_objects);
		mr = new Rand(ri.seed);

		JFrame f = new RFrame(this);
	  	f.setVisible(true);
	}

	public void run()
	{
		int i, k;
		lpInfo ri = (lpInfo) getRI();

		for (i = 0; i < ri.numb_objects; i++)
			rv.add(new RPolyLine(ri, mr));
		System.out.println("RGen lpGen complete");
	}
}

class lfGen extends RGen			/* Generate Poisson Line Field */
{
	private Rand mr;
	private Rand mr2;
	protected RapportException illegal_state = new RapportException("illegal state");

	lfGen(lfInfo ri)
	{
		super(ri);
		rv = new Vector(ri.numb_objects);
		mr = new Rand(ri.seed);
		mr2 = new Rand(ri.seed2);

		JFrame f = new RFrame(this);
	  	f.setVisible(true);
	}

	public void run()
	{
		int i,j;
		lfInfo ri = (lfInfo) getRI();

		for (i = 0; i < ri.numb_objects; i++)
		{
			for (j = 0; j < ri.max_attempts; j++)
			{
				RLineField lf = new RLineField(ri, mr);
				if (lf != null)
				{
//					lf.translate(0.5, 0.5);
					rv.add(lf);
					break;
				}
				else
					System.out.println("RGen lfGen: attempt failed # " + i + " " + j);
			}
		}
		System.out.println("RGen lfGen complete");
	}
}

class rwGen extends RGen			/* Generate Random Walk */
{
	private Rand mr;
	private Rand mr2;
	protected RapportException illegal_state = new RapportException("illegal state");

	rwGen(rwInfo ri)
	{
		super(ri);
		rv = new Vector(ri.numb_objects);
		mr = new Rand(ri.seed);
		mr2 = new Rand(ri.seed2);

		JFrame f = new RFrame(this);
	  	f.setVisible(true);
	}

	public void run()
	{
		int i,j;
		rwInfo ri = (rwInfo) getRI();

		for (i = 0; i < ri.numb_objects; i++)
		{
			for (j = 0; j < ri.max_attempts; j++)
			{
				RRandomWalk rw = new RRandomWalk(ri, mr);
				if (rw != null)
				{
					rv.add(rw);
					break;
				}
				else
					System.out.println("RGen rwGen: attempt failed # " + i + " " + j);
			}
		}
		System.out.println("RGen rwGen complete");
	}
}

class elGen extends RGen		/* Generate circles/ellipses */
{
	private Rand mr;

	elGen(elInfo ri)
	{
		super(ri);
		rv = new Vector(ri.numb_objects);
		mr = new Rand(ri.seed);

		JFrame f = new RFrame(this);
	  	f.setVisible(true);
	}

	public void run()
	{
		int i, k;
		elInfo ri = (elInfo) getRI();

		for (i = 0; i < ri.numb_objects; i++)
		{
			if (ri.circle) {
				rv.add(new RCircle(ri, mr));
			}

			else if (ri.ellipse) {
				rv.add(new REllipse(ri, mr));
			}
		}
		System.out.println("RGen elGen complete");
	}
}

class rcGen extends RGen		/* Generate rectangles */
{
	private Rand mr;

	rcGen(rcInfo ri)
	{
		super(ri);
		rv = new Vector(ri.numb_objects);
		mr = new Rand(ri.seed);

		JFrame f = new RFrame(this);
	  	f.setVisible(true);
	}
	
	public void run()
	{
		int i, j;
		rcInfo ri = (rcInfo) getRI();

		for (i = 0; i < ri.numb_objects; )
		{
			for (j = 0; j < ri.max_attempts; j++)
			{
				ArrayList ra = new ArrayList();
	
				if (ri.corners) {
					RectangleR2cr rc = new RectangleR2cr(ri);
					ra = rc.genRectangleR2cr(ri, mr);
				}
				
				else if (ri.centerext) {
					RectangleR2cn rc = new RectangleR2cn(ri);
					ra = rc.genRectangleR2cn(ri, mr);
				}
	
				else if (ri.cornerext) {
					RectangleR2ce rc = new RectangleR2ce(ri);
					ra = rc.genRectangleR2ce(ri, mr);
				}
				
				if (ra != null)
				{
					ListIterator li = ra.listIterator();
					while ( li.hasNext() && i < ri.numb_objects )
					{
						rv.add(li.next());
						i++;
					}
					break;
				}
				else
					System.out.println("RGen rcGen: attempt failed # " + i + " " + j);
			}
		}
		System.out.println("RGen rcGen complete");
	}
}

class phGen extends RGen		/* Gen polygons w/ hull method */
{
	private Rand mr;
	private Rand mr2;

	phGen(phInfo ri)
	{
		super(ri);
		rv = new Vector(ri.numb_objects);
		mr = new Rand(ri.seed);
		mr2 = new Rand(ri.seed2);

		JFrame f = new RFrame(this);
	  	f.setVisible(true);
	}

	public void run()
	{
		int i,j;
		int c,d;
		phInfo ri = (phInfo) getRI();

		if ( ri.a || ri.b || ri.c || ri.d || ri.e)	/* Generate collection a polygon at a time */
		{
			for (i = 0; i < ri.numb_objects; i++)
			{
				for (j = 0; j < ri.max_attempts; j++)
				{
					RPolygonHull pa = new RPolygonHull(ri, mr, mr2);

					if (pa.isGend())
					{
						rv.add(pa);
						break;
					}
					else
						System.out.println("RGen phGen: attempt failed # " + i + " " + j);
				}
			}
			System.out.println("RGen phGen complete");
		}

		else								/* Generate whole collection */
		{
			RPolySetHull ps = new RPolySetHull(ri, mr, mr2);
			rv = ps.genRPolySetHull();
			System.out.println("RPolySetHull complete");
		}
	}
}

class pfGen extends RGen		/* Find polygon in random points */
{
	private Rand mr;
	private Rand mr2;

	pfGen(pfInfo ri)
	{
		super(ri);
		rv = new Vector(ri.numb_objects);
		mr = new Rand(ri.seed);
		mr2 = new Rand(ri.seed2);

		if (ri.a || ri.b || ri.c)
		{
			JFrame f = new RFrame(this);
		  	f.setVisible(true);
		}
	}

	public void run()
	{
		int i,j;
		pfInfo ri = (pfInfo) getRI();

		if (ri.a || ri.b || ri.c)
		{
			for (i = 0; i < ri.numb_objects; i++)
			{
				for (j = 0; j < ri.max_attempts; j++)
				{
					RPolyRand pr = new RPolyRand(ri, mr, mr2, rp);
	
					if (pr.rv != null && rv.addAll(pr.rv))
					{
						System.out.println("RGen pfGen created group of: " + pr.rv.size());
						break;
					}
					else
						System.out.println("RGen pfGen: attempt failed # " + i + " " + j);
				}	
			}
		}
		
		else
		{
			RPolyRand pr = new RPolyRand(ri, mr, mr2, rp);
			rv = null;
		}
		System.out.println("RGen pfGen complete");
	}
}

class pcGen extends RGen		/* Generate cell polygon */
{
	private Rand mr;

	pcGen(pcInfo ri)
	{
		super(ri);
		rv = new Vector(ri.numb_objects);
		mr = new Rand(ri.seed);

		JFrame f = new RFrame(this);
	  	f.setVisible(true);
	}

	public void run()
	{
		int i,j;
		pcInfo ri = (pcInfo) getRI();

		for (i = 0; i < ri.numb_objects; i++)
		{
			for (j = 0; j < ri.max_attempts; j++)
			{		
				RPolygonCell pa = new RPolygonCell(ri, mr);

				if (pa != null)
				{
					rv.add(pa);
					break;
				}
				else
					System.out.println("RGen pcGen: attempt failed # " + i + " " + j);
			}
		}
		System.out.println("RGen pcGen complete");
	}
}

class poGen extends RGen		/* Gen monotone polygons */
{
	private Rand mr;

	poGen(poInfo ri)
	{
		super(ri);
		rv = new Vector(ri.numb_objects);
		mr = new Rand(ri.seed);

		JFrame f = new RFrame(this);
	  	f.setVisible(true);
	}

	public void run()
	{
		int i,j;
		poInfo ri = (poInfo) getRI();

		for (i = 0; i < ri.numb_objects; i++)
		{
			for (j = 0; j < ri.max_attempts; j++)
			{
				RPolygonMono pa = new RPolygonMono(ri, mr);
//				if (pa.isGend() && pa.isMonotone() && pa.isStar())
				if (pa.isGend() && pa.isMonotone())
				{
					rv.add(pa);
					break;
				}
				else
					System.out.println("RGen poGen: attempt failed # " + i + " " + j);
			}
		}
		System.out.println("RGen poGen complete");
	}
}

class psGen extends RGen		/* Gen star polygons */
{
	private Rand mr;

	psGen(psInfo ri)
	{
		super(ri);
		rv = new Vector(ri.numb_objects);
		mr = new Rand(ri.seed);

		JFrame f = new RFrame(this);
	  	f.setVisible(true);
	}

	public void run()
	{
		int i,j;
		psInfo ri = (psInfo) getRI();

		for (i = 0; i < ri.numb_objects; i++)
		{
			for (j = 0; j < ri.max_attempts; j++)
			{
				RPolygonStar pa = new RPolygonStar(ri, mr);
//				if (pa.isGend() && pa.isStar() && !pa.isMonotone())
				if (pa.isGend() && pa.isStar())
				{
					rv.add(pa);
					break;
				}
				else
					System.out.println("RGen psGen: attempt failed # " + i + " " + j);
			}
		}
		System.out.println("RGen psGen complete");
	}
}

class trGen extends RGen		/* Gen polygons w/ triangle method */
{
private Rand mr;

	trGen(trInfo ri)
	{
		super(ri);
		rv = new Vector(ri.numb_objects);
		mr = new Rand(ri.seed);

		JFrame f = new RFrame(this);
	  	f.setVisible(true);
	}

	public void run()
	{
		int i,j;
		trInfo ri = (trInfo) getRI();

		for (i = 0; i < ri.numb_objects; i++)
		{
			for (j = 0; j < ri.max_attempts; j++)
			{
				RPolygonTriangle pa = new RPolygonTriangle(ri, mr);
				if (pa.isGend())
				{
					rv.add(pa);
					break;
				}
				else
					System.out.println("RGen trGen: attempt failed # " + i + " " + j +
							", Non-hash method generally fails with 10 or more vertices");
			}
		}
		System.out.println("RGen trGen complete");
	}
}

class tsGen extends RGen			/* Generate test cases */
{
	private Rand mr;

	tsGen(tsInfo ri)
	{
		super(ri);
		rv = new Vector(ri.numb_objects);
		mr = new Rand(ri.seed);

		JFrame f = new RFrame(this);
	  	f.setVisible(true);
	}

	public void run()
	{
		int i,j;
		tsInfo ri = (tsInfo) getRI();

		for (i = 0; i < ri.numb_objects; i++)
		{
			for (j = 0; j < ri.max_attempts; j++)
			{
				PolygonA pa = genPoly(ri);
				if (pa != null)
				{
					pa.setRInfo(ri);
					if (ri.color)
						pa.setColor();
					rv.add(pa);
					break;
				}
				else
					System.out.println("RGen tsGen: attempt failed # " + i + " " + j);
			}
		}
		System.out.println("RGen tsGen complete");
	}

	public PolygonA genPoly(tsInfo ri)
	{
		int i, k = 0;
		PolygonA pa = null;
		Pointd[] tv = null;

		if (ri.hull)
		{

			ri.nv = 19;
			pa = new PolygonA(ri.nv);
			tv = new Pointd[ri.nv];

			tv[0] = new Pointd(3,3);
			tv[1] = new Pointd(3,5);
			tv[2] = new Pointd(2,5);
			tv[3] = new Pointd(0,5);
			tv[4] = new Pointd(0,1);
			tv[5] = new Pointd(-2,2);
			tv[6] = new Pointd(-3,4);
			tv[7] = new Pointd(-5,2);
			tv[8] = new Pointd(-3,2);
			tv[9] = new Pointd(-5,1);
			tv[10] = new Pointd(-5,-1);
			tv[11] = new Pointd(0,0);
			tv[12] = new Pointd(-3,-2);
			tv[13] = new Pointd(1,-2);
			tv[14] = new Pointd(3,-2);
			tv[15] = new Pointd(4,2);
			tv[16] = new Pointd(5,1);
			tv[17] = new Pointd(7,4);
			tv[18] = new Pointd(6,5);			
		}

		else if (ri.square)
		{
			if (ri.nv > 8)
				ri.nv = 8;
			pa = new PolygonA(ri.nv);
			tv = new Pointd[8];

			tv[0] = new Pointd(0, 1);
			tv[1] = new Pointd(-1, 1);
			tv[2] = new Pointd(-1, 0);
			tv[3] = new Pointd(-1, -1);
			tv[4] = new Pointd(0, -1);
			tv[5] = new Pointd(1, -1);
			tv[6] = new Pointd(1, 0);
			tv[7] = new Pointd(1, 1);
		}
		EdgeR edge;
		Pointd pt;

		for ( i=0; i<ri.nv; i++ )
			k = pa.addVertex(tv[i]);

  		if (ri.debug >= 3)
  			System.out.println(pa);

 		if (!pa.checkPolygon())
 			pa = null;
  
  		return pa;
	}
}

class drGen extends RGen			/* Draw a polygon */
{
	private Rand mr;

	drGen(drInfo ri)
	{
		super(ri);
		rv = new Vector(ri.numb_objects);
		mr = new Rand(ri.seed);

		JFrame f = new RFrame(this);
	  	f.setVisible(true);
	}

	public void run()
	{
		drInfo ri = (drInfo) getRI();
		
		if (ri.numb_objects > 1)
			System.out.println("Draw supports creation of only one generation per invocation.");
		
		int nd = 1;
		for (int i = 0; i < nd; i++)
		{
			PolygonA pa = new PolygonA(ri.nv);
			pa.setRInfo(ri);
			pa.setType(TYPE_POLYGON_DRAW);
			rv.add(pa);
		}
	}
}

class cmGen extends RGen		/* Generate Cell PolyMap */
{
	private Rand mr;

	cmGen(cmInfo ri)
	{
		super(ri);
		rv = new Vector(ri.numb_objects);
		mr = new Rand(ri.seed);

		JFrame f = new RFrame(this);
		f.setVisible(true);
	}

	public void run()
	{
		int i,j;
		cmInfo ri = (cmInfo) getRI();
		RObject cm = null;

		for (i = 0; i < ri.numb_objects; i++)
		{
			for (j = 0; j < ri.max_attempts; j++)
			{
				cm = new RPolyMapCell(ri, mr);

				if (cm != null)
				{
					rv.add(cm);
					break;
				}
			 	else
					System.out.println("RGen cmGen: attempt failed # " + i + " " + j);
			}
		}
		System.out.println("RGen cmGen complete");
	}
}

class pmGen extends RGen		/* Generate Poisson PolyMap */
{
	private Rand mr;

	pmGen(pmInfo ri)
	{
		super(ri);
		rv = new Vector(ri.numb_objects);
		mr = new Rand(ri.seed);

		JFrame f = new RFrame(this);
	  	f.setVisible(true);
	}

	public void run()
	{
		int i,j;
		pmInfo ri = (pmInfo) getRI();

		boolean useAlpha = false;
		int[] prv = null;
		if (ri.num_items == 0)
		{
			useAlpha = true;
			prv = mr.poisson(ri.numb_objects, 4*ri.alpha);
		}

		for (i = 0; i < ri.numb_objects; i++)
		{
			for (j = 0; j < ri.max_attempts; j++)
			{
				if (useAlpha)
					ri.num_items = prv[i];

				RPolyMapPoisson pm = new RPolyMapPoisson(ri, mr);
				if (pm != null)
				{
					rv.add(pm);
					break;
				}
				else
					System.out.println("RGen pmGen: attempt failed # " + i + " " + j);
			}
		}
		System.out.println("RGen pmGen complete");
	}
}

class lmGen extends RGen			/* Generate PolyLine PolyMap */
{
	private Rand mr;

	lmGen(lmInfo ri)
	{
		super(ri);
		rv = new Vector(ri.numb_objects);
		mr = new Rand(ri.seed);

		JFrame f = new RFrame(this);
	  	f.setVisible(true);
	}

	public void run()
	{
		int i,j;
		lmInfo ri = (lmInfo) getRI();

		for (i = 0; i < ri.numb_objects; i++)
		{
			for (j = 0; j < ri.max_attempts; j++)
			{
				RPolyMapPolyLines lm = new RPolyMapPolyLines(ri, mr);
				if (lm != null)
				{
					rv.add(lm);
					break;
				}
				else
					System.out.println("RGen lmGen: attempt failed # " + i + " " + j);
			}
		}
		System.out.println("RGen lmGen complete");
	}
}
		
class vmGen extends RGen			/* Generate Voronoi diagram polymap */
{
	private Rand mr;

	vmGen(vmInfo ri)
	{
		super(ri);
		rv = new Vector(ri.numb_objects);
		mr = new Rand(ri.seed);

		JFrame f = new RFrame(this);
	  	f.setVisible(true);
	}

	public void run()
	{
		int i,j;
		vmInfo ri = (vmInfo) getRI();

		for (i = 0; i < ri.numb_objects; i++)
		{
			for (j = 0; j < ri.max_attempts; j++)
			{

				RPolyMapVoronoi vm = new RPolyMapVoronoi(ri, mr);
				if (vm != null)
				{
					rv.add(vm);
					break;
				}
				else
					System.out.println("RGen vmGen: attempt failed # " + i + " " + j);
			}
		}
		System.out.println("RGen vmGen complete");
	}
}