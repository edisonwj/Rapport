package org.edisonwj.rapport;

/**
 * RPolyMap Poisson defines the Poisson polymap class
 */

import java.io.*;
import java.text.*;
import java.util.*;

class RPolyMapPoisson extends RPolyMap implements RapportDefaults
{
	protected pmInfo ri;					/* Link to RInfo */
	
	private int pmdebug;

	public RPolyMapPoisson(pmInfo ri, Rand mr)	/* Generate polymap from Poisson line field */
	{
		super();
		this.ri = ri;
//		System.out.println("RPolyMapPoisson(pmInfo ri, Rand mr) constructor: " + this.hashCode() +
//				", ri: " + ri.hashCode() + 
//				", type: " + type);
		
		this.cr = new Rand(mr.getSeed());
		this.pmdebug = ri.debug;

		ArrayList al;
		if ( ri.num_items == 0)			/* if no lines, return unit square */
		{
			Pointd[] square = { new Pointd( .5, .5),
								new Pointd(-.5, .5),
								new Pointd(-.5,-.5),
								new Pointd( .5, -.5)};
			np = 1;
			pa = new PolygonA[np];
			pa[0] = new PolygonA(square);
		}
		else							/* else find Poisson polygons */
		{
			al = genPolyMap(ri, mr);
			np = al.size();
			pa = new PolygonA[np];
			for (int i = 0; i < np; i++) {
				pa[i] = new PolygonA((ArrayList)al.get(i),TYPE_POLYGON);
				pa[i].setRInfo(ri);
			}
		}
		this.translate(0.5, 0.5);
		computeMetrics();
		if (ri.color)
			setColor();
	}
	
	public RPolyMapPoisson(BufferedReader in) throws IOException
	{
//		System.out.println("RPolyMapPoisson(Buffered Reader) constructor this: " + this.hashCode());
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
			ri = new pmInfo(pa.length);
			ri.title = stitle;
//			System.out.println("RPolyMapPoisson this: " + this.hashCode());
//			System.out.println("RPolyMapPoisson ri: " + ri.hashCode() + ", ri.title: " + ri.title);
		}
		else
		{
			System.out.println("RPolyMap: Error constructing point distribution from file input");
			pa = null;
			nv = 0;
		}
	}

	private ArrayList genPolyMap(pmInfo ri, Rand mr)
	{
//		System.out.println("RPolyMapPoisson genPolyMap(pmInfo ri, Rand mr): " + ri.hashCode());
		pmdebug = ri.debug;

		Pointd endp1, endp2, op;
		Vector ipset = new Vector();
		ArrayList[] svlist;				/* Segment vertex lists */

//		Generate line field
		RLineField lf = new RLineField(ri.num_items, mr);

//		Create set of line segments from line field
		RSegmentSet rss = new RSegmentSet(lf);

//		Set of test segments
//		RSegmentSet rss = new RSegmentSet( new RSegment[] {
//						new RSegment(new Pointd(0.6,0.0), new Pointd(0.6,1.0)),
//						new RSegment(new Pointd(0.0,1.0), new Pointd(1.0,0.0)),
//						new RSegment(new Pointd(0.2,0.0), new Pointd(1.0,0.4)),
//						new RSegment(new Pointd(0.2,0.9), new Pointd(0.4,0.05)) } );

		RSegment rs;
		int baseid = (rss.get(0)).getId();

//		Set up segment-vertex lists
		int rsx = ri.num_items;
		svlist = new ArrayList[rsx+4];
		for (int k = 0; k < rsx+4; k++)
			svlist[k] = new ArrayList();

//		Add edge segment start points
		svlist[rsx].add(  new Pointd(-0.5,  0.5));	/* top edge */
		svlist[rsx+1].add(new Pointd(-0.5,  0.5));	/* left edge */
		svlist[rsx+2].add(new Pointd( 0.5,  0.5));	/* right edge */
		svlist[rsx+3].add(new Pointd(-0.5, -0.5));	/* bottom edge */

//		Initialize event queue with segment endpoints
		RedBlackTree eventtree = new RedBlackTree(new EventSortTool());
		EventPoint ep;
		for (int k = 0; k < rss.size(); k++)
		{
			rs = rss.get(k);
			if (pmdebug >= 3)
				System.out.println("rs: " + rs);
			endp1 = rs.getp1();
			ep = new EventPoint(endp1, rs);
			eventtree.add(ep);
			endp2 = rs.getp2();
			ep = new EventPoint(endp2, rs);
			eventtree.add(ep);

//			Validate segment id's
			rsx = rs.getId()-baseid;
			if ( k != rsx )
				System.out.println("RGen invalid sement id k= " + k + ", rsx= " + rsx);
		}

//		eventtree.ShowTree();

//		Initialize status structure
		SweepTree sweeptree = new SweepTree(new SweepSortTool());

//		Sweep the lines, finding intersections
		Pointd ip;
		RSegmentSet ss;
		RSegment s0, s1;
		int s0id, s1id;

		while (eventtree.getCount() > 0)
		{
			ep = (EventPoint)eventtree.removeFirst();

//			Record points in segment vertex lists

			int et = ep.getType();
			switch(et)
			{
				case 0:				/* Upper end point */
					ip = ep.getPoint();
					ss = ep.getSegmentSet();
					s0 = ss.get(0);
					s0id = s0.getId();
					if (pmdebug >= 3)
					{
						System.out.println("handleEvent upper endpoint ip= " + ip);
						System.out.println("handleEvent s0id= " + s0id + ", s0= " + s0);
					}
					svlist[s0id-baseid].add(ip);
					if (pmdebug >= 3)
						System.out.println("handleEvent svlist[" + s0id + "]= " + svlist[s0id-baseid]);

					rsx = rss.size();
					if (ip.gety() == .5)
						svlist[rsx].add(ip);
					else if (ip.getx() == -.5)
						svlist[rsx+1].add(ip);
					else if (ip.getx() == .5)
						svlist[rsx+2].add(ip);
					else if (ip.gety() == -.5)
						svlist[rsx+3].add(ip);
				break;

				case 1:				/* Lower end point */
					ip = ep.getPoint();
					ss = ep.getSegmentSet();
					s0 = ss.get(0);
					s0id = s0.getId();
					if (pmdebug >= 3)
					{
						System.out.println("handleEvent lower endpoint ip= " + ip);
						System.out.println("handleEvent s0id= " + s0id + ", s0= " + s0);
					}
					svlist[s0id-baseid].add(ip);
					if (pmdebug >= 3)
						System.out.println("handleEvent svlist[" + s0id + "]= " + svlist[s0id-baseid]);

					rsx = rss.size();
					if (ip.gety() == .5)
						svlist[rsx].add(ip);
					else if (ip.getx() == -.5)
						svlist[rsx+1].add(ip);
					else if (ip.getx() == .5)
						svlist[rsx+2].add(ip);
					else if (ip.gety() == -.5)
						svlist[rsx+3].add(ip);
				break;

				case 2:				/* Intersection point */
					ip = ep.getPoint();
					ss = ep.getSegmentSet();
					s0 = ss.get(0);
					s1 = ss.get(1);
					s0id = s0.getId();
					s1id = s1.getId();
					if (pmdebug >= 3)
					{
						System.out.println("handleEvent ip= " + ip);
						System.out.println("handleEvent s0id= " + s0id + ", s0= " + s0);
						System.out.println("handleEvent s1id= " + s1id + ", s1= " + s1);
					}
					svlist[s0id-baseid].add(ip);
					if (pmdebug >= 3)
						System.out.println("handleEvent svlist[" + s0id + "]= " + svlist[s0id-baseid]);
					svlist[s1id-baseid].add(ip);
					if (pmdebug >= 3)
						System.out.println("handleEvent svlist[" + s1id + "]= " + svlist[s1id-baseid]);
				break;
			}

			op = handleEvent(eventtree, sweeptree, ep);
			if (op != null)
				ipset.addElement(op);
			if (pmdebug >= 3)
				System.out.println("\nEventTree");
//			eventtree.ShowTree();
		}

//		Add edge segment endpoints
		rsx = ri.num_items;
		svlist[rsx].add(  new Pointd( 0.5,  0.5));	/* top edge */
		svlist[rsx+1].add(new Pointd(-0.5, -0.5));	/* left edge */
		svlist[rsx+2].add(new Pointd( 0.5, -0.5));	/* right edge */
		svlist[rsx+3].add(new Pointd( 0.5, -0.5));	/* bottom edge */
//		System.out.println("RPolyMapPoisson: Completed finding intersection points.");

//		Print array of intersection points
//		and segment vertex lists
//		System.out.println("RPolyMap-Poisson #intersections= " + ipset.size());
		if (pmdebug >= 3)
		{
			System.out.println("Intersection points");
			for (int z = 0; z < ipset.size(); z++)
			{
				System.out.println("ipset[" + z + "]= " + ipset.elementAt(z));
			}

			for (int i = 0; i < rsx+4; i++)
				for (int j = 0; j < svlist[i].size(); j++)
					System.out.println("genPoisson svlist["
						+ i + "](" + j + ")= " + svlist[i].get(j));
		}

//		Find edges
		HashMap emap = new HashMap();
		ArrayList elist;
		String sp;
		for (int i = 0; i < rsx+4; i++)
		{
			int lsize = svlist[i].size();
			endp1 = (Pointd)svlist[i].get(0);
			for (int j = 1; j < lsize; j++)
			{
				endp2 = (Pointd)svlist[i].get(j);

				sp = endp1.toString();
				if (emap.containsKey(sp))
					elist = (ArrayList)emap.get(sp);
				else
					elist = new ArrayList();
				if (!(( endp1.getx() ==  0.5 && endp2.getx() ==  0.5 &&
						endp1.gety() > endp2.gety() )				||

					  ( endp1.getx() == -0.5 && endp2.getx() == -0.5 &&
					  	endp1.gety() < endp2.gety() )				||

					  ( endp1.gety() ==  0.5 && endp2.gety() ==  0.5 &&
					  	endp1.getx() < endp2.getx() )				||

					  ( endp1.gety() == -0.5 && endp2.gety() == -0.5 &&
					  	endp1.getx() > endp2.getx() )))
				{
					elist.add(endp2);
					emap.put(sp, elist);
				}

				sp = endp2.toString();
				if (emap.containsKey(sp))
					elist = (ArrayList)emap.get(sp);
				else
					elist = new ArrayList();

				if (!(( endp2.getx() ==  0.5 && endp1.getx() ==  0.5 &&
						endp2.gety() > endp1.gety() )				||

					  ( endp2.getx() == -0.5 && endp1.getx() == -0.5 &&
					  	endp2.gety() < endp1.gety() )				||

					  ( endp2.gety() ==  0.5 && endp1.gety() ==  0.5 &&
					  	endp2.getx() < endp1.getx() )				||

					  ( endp2.gety() == -0.5 && endp1.gety() == -0.5 &&
					  	endp2.getx() > endp1.getx() )))
				{
					elist.add(endp1);
					emap.put(sp, elist);
				}

				endp1 = endp2;
			}
		}
//		System.out.println("RPolyMapPoisson: Completed finding edges.");

		if (pmdebug >= 3)
		{
//		Print hash entries
			int k = 0;
			Set entries = emap.entrySet();
			java.util.Iterator iter = entries.iterator();
			while ( iter.hasNext() )
			{
				Map.Entry entry = (Map.Entry)iter.next();
//				Pointd key = (Pointd)entry.getKey();
				String key = (String)entry.getKey();
				ArrayList val = (ArrayList)entry.getValue();
				for (int i = 0; i < val.size(); i++)
					System.out.println("RGen - emap entry " + k +": " + i + " "
							+ key + " => " + (Pointd)val.get(i) );
				k++;
			}
		}


//		Find polygons
		Pointd p1, p2, p3, start;
		ArrayList polygons = new ArrayList();		/* ArrayList of polygons */
		int pc = -1;								/* Count of polygons */
		while ( !emap.isEmpty() )
		{
			pc++;
//			System.out.println("Start polygon " + pc);
			ArrayList plist = new ArrayList();
			Set entries = emap.entrySet();
			java.util.Iterator iter = entries.iterator();
			Map.Entry entry = (Map.Entry)iter.next();
			String key = (String)entry.getKey();
//			System.out.println("key= " + key);
			ArrayList val = (ArrayList)emap.get(key);
			p1 = (Pointd)val.get(0);
//			System.out.println("val(0)= " + p1);
			val = (ArrayList)emap.get(p1.toString());
			p2 = (Pointd)val.get(0);

			plist.add(p1);
			start = p1;
			plist.add(p2);
			val.remove(val.indexOf(p2));
			if ( val.size() == 0 )
				emap.remove(p1.toString());
			else
				emap.put(p1.toString(), val);

			if (pmdebug >= 3)
			{
				System.out.println("p1: " + p1);
				System.out.println("p2: " + p2);
			}

			do
			{
				val = (ArrayList)emap.get(p2.toString());
				if (val.size() == 1)
					p3 = (Pointd)val.get(0);
				else
					p3 = Geometry.minAngle(p1, p2, val);
//				System.out.println("p3= " + p3);
				int i = val.indexOf(p3);
				plist.add(p3);
				val.remove(i);
				if ( val.size() == 0 )
//					emap.remove(p2);
					emap.remove(p2.toString());
				else
					emap.put(p2.toString(), val);
				p1 = p2;
				p2 = p3;
			} while ( !p2.equals(start) );
			plist.remove(plist.size()-1);

			if (pmdebug >= 3)
			{
				System.out.println();
				for (int j = 0; j < plist.size(); j++)
					System.out.println("RGen - Poisson Polygon " + pc + " (" + j + ")= " + plist.get(j));
				System.out.println();
			}

			polygons.add(plist);
		}
//		System.out.println("RPolyMapPoisson: Completed finding " + polygons.size() + " polygons.");

		return polygons;

//		lf.reSizeI();
//		return lf;

//		return rss;

//		Find all polygon regions

//		Map all border edges

//		In the sweep process, as internal vertices are discovered, create
//		linked lists for each segment listing all vertices on that segment.

//		Process each segment list, identifying all edges, using bi-directional
//		structure.

//		Create hash table of edges, indexed by starting vertex.

//		Start with a border edge.
//		Process all edges, creating polygons, following the linkage of vertices.
//		As polygons are created, delete edges from the hash table.
//		Store polygons in a vector.


	}

	private Pointd handleEvent( RedBlackTree eventtree,
									SweepTree sweeptree,
									EventPoint ep)
	{
		Pointd op = null;
		intersect ip = null;

		if (pmdebug >= 3)
			System.out.println("ep: " + ep);

		int type = ep.getType();
		int ntype;
		sweeptree.setPsweep(ep.getPoint());

		if (pmdebug >= 3)
			System.out.println("psweep: " + sweeptree.getPsweep());

		if (type == 0)			/* upper endpoint */
		{
			if (pmdebug >= 3)
				System.out.println("Handle event - upper endpoint");

			Pointd p0 = ep.getPoint();
			RSegment s0 = (ep.getSegmentSet()).get(0);

			if (pmdebug >= 3)
				System.out.println("s0: " + s0);

			Pointd p1 = s0.getp1();
			Pointd p2 = s0.getp2();
			SweepItem si = new SweepItem(s0, sweeptree);
			sweeptree.add(si);

			Object[] ni = sweeptree.findNeighbors(si);
			for (int i = 0; i < 2; i++)
			{
				if (ni[i] == null)
					continue;
				else
				{
					RSegment s1 = ((SweepItem)ni[i]).getSegment();

					if (pmdebug >= 3)
						System.out.println("s1: " + s1);

					ip = Geometry.SegSegInt(p1, p2, s1.getp1(), s1.getp2());
					if (ip.getCode() != '0' &&
						(sweeptree.getPsweep()).before(ip.getPoint()) )
					{
						if (pmdebug >= 3)
							System.out.println("intersect: " + ip + " " + s0 + " " + s1);

						eventtree.add( new EventPoint(  2,
														ip.getPoint(),
													    new RSegmentSet(new RSegment[] {s0, s1})) );
					}
				}
			}
		}

		else if (type == 1)		/* lower endpoint */
		{
			if (pmdebug >= 3)
				System.out.println("Handle event - lower endpoint");

			Pointd p0 = ep.getPoint();
			RSegment s = new RSegment(p0, p0);
			SweepItem si = new SweepItem(s, sweeptree);

			Object[] ni = sweeptree.findNeighbors(si);
			sweeptree.remove(si);
			if ( ni[0] != null && ni[1] != null )
			{
				RSegment s0 = ((SweepItem)ni[0]).getSegment();
				RSegment s1 = ((SweepItem)ni[1]).getSegment();
				ip = Geometry.SegSegInt( 	s0.getp1(), s0.getp2(),
													s1.getp1(), s1.getp2() );
				if (ip.getCode() != '0' &&
					(sweeptree.getPsweep()).before(ip.getPoint()) )
				{
					if (pmdebug >= 3)
						System.out.println("intersect: " + ip + " " + s0 + " " + s1);

					eventtree.add( new EventPoint(  2,
													ip.getPoint(),
												    new RSegmentSet(new RSegment[] {s0, s1})) );
				}
			}
		}

		else if (type == 2)		/* Intersection point */
		{

			SweepItem si0, si1, sip;
			RSegment s0, s1;

			if (pmdebug >= 3)
				System.out.println("Handle event - intersection");

			op = ep.getPoint();

			if (pmdebug >= 3)
				System.out.println("Handle Event - Intersection Point Output: " + op);

			RSegmentSet ss = ep.getSegmentSet();
			if ( ss.size() != 2 )
				throw illegal_state;

			si0 = new SweepItem(ss.get(0), sweeptree);
			si1 = new SweepItem(ss.get(1), sweeptree);

			if (pmdebug >= 3)
				System.out.println("Handle event - intersection si0/si1:\n"
								+ si0 + "\n"
								+ si1);

//			sweeptree.ShowTree();

			SweepItem[] si = new SweepItem[2];

			if (pmdebug >= 3)
				System.out.println("sip = (SweepItem)sweeptree.findPredecessor(si0);");
			sip = (SweepItem)sweeptree.findPredecessor(si0);
			if ( sip != null && (sip.getSegment()).equals(si1.getSegment()) )
			{
				if (pmdebug >= 3)
					System.out.println("1, sip " + sip);

				si[0] = si1;
				si[1] = si0;
			}
			else
			{
				if (pmdebug >= 3)
					System.out.println("2");

				sip = (SweepItem)sweeptree.findPredecessor(si1);
				if ( sip != null && (sip.getSegment()).equals(si0.getSegment()) )
				{
					if (pmdebug >= 3)
						System.out.println("3, sip " + sip);

					si[0] = si0;
					si[1] = si1;
				}
			}

			if (pmdebug >= 3)
				System.out.println("Segment set: si[0] and si[1] \n"
						+ si[0] + "\n"
						+ si[1] );

			// Note: pred(si[0]) before reverse = pred(si[1]) after reverse

			sip = (SweepItem)sweeptree.findPredecessor(si[0]);
			if ( sip != null )
			{
				if (pmdebug >= 3)
					System.out.println("Find intersection pred(si[1]) and si[1]:\n"
									+ sip + "\n"
									+ si[1] );
				s0 = ((SweepItem)sip).getSegment();
				s1 = ((SweepItem)si[1]).getSegment();
				ip = Geometry.SegSegInt( 	s0.getp1(), s0.getp2(),
													s1.getp1(), s1.getp2() );
				if (ip.getCode() != '0' &&
					(sweeptree.getPsweep()).before(ip.getPoint()) )
				{
					if (pmdebug >= 3)
						System.out.println("intersect: " + ip + " " + s0 + " " + s1);

					eventtree.add( new EventPoint(  2,
													ip.getPoint(),
												    new RSegmentSet(new RSegment[] {s0, s1})) );
				}
			}

			// Note: succ(si[1]) before reverse = succ(si[0]) after reverse

			sip = (SweepItem)sweeptree.findSuccessor(si[1]);
			if ( sip != null )
			{
				if (pmdebug >= 3)
					System.out.println("Find intersection si[0] and succ(si[0]):\n"
									+ si[0] + "\n"
									+ sip );
				s0 = ((SweepItem)si[0]).getSegment();
				s1 = ((SweepItem)sip).getSegment();
				ip = Geometry.SegSegInt( 	s0.getp1(), s0.getp2(),
												s1.getp1(), s1.getp2() );
				if (ip.getCode() != '0' &&
					(sweeptree.getPsweep()).before(ip.getPoint()) )
				{
					if (pmdebug >= 3)
						System.out.println("intersect: " + ip + " " + s0 + " " + s1);

					eventtree.add( new EventPoint(  2,
												ip.getPoint(),
											    new RSegmentSet(new RSegment[] {s0, s1})) );
				}
			}

			if (pmdebug >= 3)
				System.out.println("Reverse items");

			sweeptree.reverse(si0, si1);

			if (pmdebug >= 4)
			{
				System.out.println("Items reversed");
				sweeptree.ShowTree();
			}
		}
		else
			throw illegal_state;

		return op;
	}

	private void FindNewEvent(RedBlackTree eventtree, EventPoint slf, EventPoint srt, EventPoint p)
	{
		RSegment s1 = (slf.getSegmentSet()).get(0);
		RSegment s2 = (srt.getSegmentSet()).get(0);
		Pointd pp  = p.getPoint();

		intersect ip = Geometry.SegSegInt(  s1.getp1(), s1.getp2(),
											s2.getp1(), s2.getp2() );

		if ( ip.getCode() == 2 && pp.before(ip.getPoint()) )
		{
			RSegmentSet ssc = slf.getSegmentSet();
			ssc.addAll(srt.getSegmentSet());
			EventPoint newep = new EventPoint(	2,
												ip.getPoint(),
												ssc );
//			if (!eventtree.find(newep))
				eventtree.add(newep);
		}
	}
	
	public void writeData(boolean draw3d, PrintWriter out) throws IOException
	{
		PolygonA pl;
		
//		System.out.println("RPolyMapPoisson writeData type: " + type);
		String lineOut = "";
		String parmsOut = "";
		
		if (ri != null) {				
			parmsOut = "Number-of-lines = " + ri.Snum_items;	
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
	
	public pmInfo getRInfo() {
		return ri;
	}
}