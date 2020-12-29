package org.edisonwj.rapport;

/**
 * RPolyMap Voronoi defines the Voronoi diagram polymap class.
 * This class utilizes Christoph Nahr's org.kynosarges.tektosyne.geometry
 * classes to generate the Voronoi diagram.
 */

import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.RectD;
import org.kynosarges.tektosyne.geometry.Voronoi;
import org.kynosarges.tektosyne.geometry.VoronoiResults;

import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.StringTokenizer;

class RPolyMapVoronoi extends RPolyMap implements RapportDefaults
{
	protected vmInfo ri;					/* Link to RInfo */

	private int pmdebug;
	
	public RPolyMapVoronoi(vmInfo ri, Rand mr)	/* Generate Voronoi diagram polymap */
	{
		super();
		this.ri = ri;
		this.cr = new Rand(mr.getSeed());
		this.pmdebug = ri.debug;

		if ( ri.num_items == 0)			/* if no items, return unit square */
		{
			Pointd[] square = { new Pointd( .5, .5),
								new Pointd(-.5, .5),
								new Pointd(-.5,-.5),
								new Pointd( .5, -.5)};
			np = 1;
			pa = new PolygonA[np];
			pa[0] = new PolygonA(square);
		}
		else							/* else find Voronoi polygons */
		{
			ptInfo pi = new ptInfo();
			pi.uniform = ri.uniform;
			pi.normal = ri.normal;
			pi.poisson = ri.poisson;
			pi.num_items = ri.num_items;
			pi.num_clusters = ri.num_clusters;
			pi.std_dev = ri.std_dev;
			pi.density = ri.density;
			
			RPointSet ps = new RPointSet(pi, mr);
			sites = ps.pa;
			PointD[] sitesD = new PointD[ps.pa.length];
			for (int i = 0; i < ps.pa.length; i++)
				sitesD[i] = new PointD(ps.pa[i].getx(), ps.pa[i].gety());
			
			RectD bbox = new RectD(0.0, 0.0, 1.0, 1.0);
			VoronoiResults vr = Voronoi.findAll(sitesD, bbox);
			PointD[][] regions = vr.voronoiRegions();
			np = regions.length;
			pa = new PolygonA[np];
			Pointd vertex = new Pointd();
			for (int i = 0; i < np; i++) {
				pa[i] = new PolygonA(regions[i].length);
				for (int j = 0; j < regions[i].length; j++) {
					vertex = new Pointd(regions[i][j].x, regions[i][j].y);
					pa[i].setVertex(vertex, j);
//					System.out.println("vertex: " + vertex);
				}
				pa[i].setRInfo(ri);
				pa[i].setnv(regions[i].length);
				pa[i].setType(TYPE_POLYGON);
				pa[i].setnv(regions[i].length);
				pa[i].checkPolygon();
//				System.out.println("RPolyMapVoronoiPolygon i: " + i + " " + pa[i].toString());
//				System.out.println("RPolyMapVoronoiPolygon i: " + i + " " + pa[i].ri.hashCode());
			}
		}
		// this.translate(0.5, 0.5);
		computeMetrics();
		if (ri.color)
			setColor();
	}
	
	public RPolyMapVoronoi(BufferedReader in) throws IOException
	{
//		System.out.println("RPolyMapVoronoi(Buffered Reader) constructor this: " + this.hashCode());
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
				pa[i] = new PolygonA(TYPE_POLYGON, in);
			nv = np;
//			System.out.println("RPolyMapVoronoi read sites");
			int ns = readNS(in);						/* Get number of sites (points) */
//			System.out.println("RPolyMapVoronoi ns: " + ns);
			sites = new Pointd[ns];
			if (ns > 0) {
				String s;
				for (int i=0; i < ns; i++)
				{
					s = in.readLine();
//					System.out.println("RPolyMapVoronoi readData line: i: " + i + " " + s);
					sites[i] = new Pointd(s);
				}
			}
			ri = new vmInfo(pa.length);
			ri.title = stitle;
//			System.out.println("RPolyMapVoronoi this: " + this.hashCode());
//			System.out.println("RPolyMapVoronoi ri: " + ri.hashCode() + ", ri.title: " + ri.title);
		}
		else
		{
			System.out.println("RPolyMapVoronoi: Error constructing point distribution from file input");
			pa = null;
			nv = 0;
		}
	}
	
	public int readNS(BufferedReader in) throws IOException
	{
		String s;
		StringTokenizer t;
		int ns = 0;
		s = in.readLine();
		t = new StringTokenizer(s, "#= \t\n\r");
		if ( s.charAt(0) == '#' && "NS".equals(t.nextToken()) )
			ns = Integer.parseInt(t.nextToken());
		return ns;
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
			g.setColor(Color.BLACK);
		
			for (int i = 0; i < sites.length; i++) {
				sites[i].draw(g, rp);
			}
			g.setColor(saveC);
		}
	}
		
	public void writeData(boolean draw3d, PrintWriter out) throws IOException {
//		System.out.println("RPolyMapVoronoi writeData - type: " + type);
		
		PolygonA pl;
		Pointd pt;
		String lineOut = "";
		String parmsOut = "";
		String distribution = "";
		
		if (ri != null) {		
			if (ri.uniform)
				distribution = "Uniform";
			else if (ri.normal)
				distribution = "Normal";
			else if (ri.poisson)
				distribution = "Poisson";
			else
				distribution = "Unknown";
			
			parmsOut = "Distribution = " + distribution + 
						", Number-of-points = " + ri.Snum_items +
						", Number-of-clusters = " + ri.Snum_clusters +
						", Standard-deviation = " + ri.Sstd_dev +
						", Density = " + ri.Sdensity;	
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
			for (int i = 0; i < sites.length; i++) {
	   	  		pt = sites[i];
	   	  		pt.writeData(draw3d, out);
			}
		}
		
		/* Create standard output */
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
			
     		out.println("#NS=" + sites.length);
			for (int i = 0; i < sites.length; i++) {
	   	  		pt = sites[i];
	   	  		pt.writeData(draw3d, out);
			}
		}
	}
	
	public vmInfo getRInfo() {
		return ri;
	}
}