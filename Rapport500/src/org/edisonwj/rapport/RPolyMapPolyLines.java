package org.edisonwj.rapport;

/**
 * RPolyMapPolyLines defines the polyline polymap class
 */


import java.io.*;
import java.text.*;
import java.util.*;

class RPolyMapPolyLines extends RPolyMap implements RapportDefaults
{
	protected lmInfo ri;					/* Link to RInfo */
	
	int countPoly = 0;		/* Polygon count - determines poly id */
	int countEdge = 0;		/* Edge count - determines edge id */

	ArrayList pl;			/* Extendable ArrayList of RPolygonE objects */
	ArrayList el;			/* Extendable ArrayList of REdgeR objects */
//	ArrayList ppcl;			/* Preprocessed chain */
//	ArrayList partial_ppcl;	/* Partial preprocessed chain */
	RHashtable ht;			/* Hashtable for edges */

	public RPolyMapPolyLines(lmInfo ri, Rand mr)	/* Generate polymap from polylines */
	{
		super();
		this.ri = ri;
		this.cr = new Rand(mr.getSeed());
		genPolyMap(ri, mr);
		computeMetrics();
		if (ri.color)
			setColor();
	}
	
	public RPolyMapPolyLines(BufferedReader in) throws IOException
	{
//		System.out.println("RPolyMapPolyLines(Buffered Reader) constructor this: " + this.hashCode());
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
			ri = new lmInfo(pa.length);
			ri.title = stitle;
//			System.out.println("RPolyMapPolyLines this: " + this.hashCode());
//			System.out.println("RPolyMapPolyLines ri: " + ri.hashCode() + ", ri.title: " + ri.title);
		}
		else
		{
			System.out.println("RPolyMapPolyLines: Error constructing point distribution from file input");
			pa = null;
			nv = 0;
		}
	}
	
	private void genPolyMap(lmInfo ri, Rand mr)
	{
		pl = new ArrayList();		/* Initialize polygon list */
		el = new ArrayList();		/* Initialize edge list */
		ht = new RHashtable();		/* Initialize edge hashtable */
		REdge.reset();				/* Set edge count back to zero */
		RPolygonE.reset();			/* Set polygon count back to zero */

		/* Set up for test */
//		setupTest();

		/* Form initial bounding region */
		initialize();
		lpInfo lpi = new lpInfo();
		int nl = ri.num_items;

		/* Randomize number of polylines? */
		if (ri.random && nl > 10)
			nl = mr.uniform(10,nl);

		for (int i = 0; i < nl; i++)
		{
//			System.out.println("\nProcessing line " + i);
			RPolyLine rpl = new RPolyLine(lpi, mr);
//			System.out.println("line #: " + i + ", # vertices: " + rpl.size());
//			rpl.show();
			add(rpl);
		}

		np = pl.size();
		pa = new PolygonA[np];
		for (int i = 0 ; i < np; i++) {
			pa[i] = getEPoly(i);
			pa[i].setRInfo(ri);
		}
	}

	private void initialize()
	{
		ArrayList al;

		/* Initialize with boundary edges */
		al = new ArrayList();
		REdge r0 = new REdge(new Pointd(0.0,0.0),new Pointd(1.0,0.0),0);
		REdge r1 = new REdge(new Pointd(1.0,0.0),new Pointd(1.0,1.0),0);
		REdge r2 = new REdge(new Pointd(1.0,1.0),new Pointd(0.0,1.0),0);
		REdge r3 = new REdge(new Pointd(0.0,1.0),new Pointd(0.0,0.0),0);
		al.add(r0);
		al.add(r1);
		al.add(r2);
		al.add(r3);
		el.addAll(al);
		ht.addAll(al);

		al = new ArrayList();
		al.add(new Integer(0));
		al.add(new Integer(1));
		al.add(new Integer(2));
		al.add(new Integer(3));
		pl.add(new RPolygonE(al));

//		System.out.println("\nREdge list");
//		for (int i = 0; i < el.size(); i++)
//			System.out.println(((REdge)el.get(i)).toString());

//		System.out.println("\nRHashtable\n" + ht);

//		System.out.println("\nRPolygonE list");
//		for (int i = 0; i < pl.size(); i++)
//			System.out.println(pl.get(i));
	}

	private PolygonA getEPoly(int in)
	{
//		System.out.println("Processing polygon " + in);
		REdge re;
		int eid;
		RPolygonE pe;
		int pid;
		pe = (RPolygonE)pl.get(in);
		pid = pe.getid();
		int nv = pe.size();
		Pointd[] v = new Pointd[nv];
		for (int i = 0; i < nv; i++)
		{
			re = (REdge)el.get(pe.get(i));
			eid = re.getid();
			v[i] = re.getv(pid);
//			System.out.println("v[" + i + "]= " + v[i] + " eid= " + eid
//								+ "\n\tv1= " + re.getv1() + "->" + re.getv2());
		}

		return new PolygonA(v,true,TYPE_POLYGON);
//		return new PolygonA(v,false); /* Suppresses polygon type check */
	}

	public void add(RPolyLine c)
	{
		ArrayList ppcl = preprocessChain(c);
		ppcl = setnextOn(ppcl);
//		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

//		for (int i = 0; i < ppcl.size(); i++)
//			System.out.println("ppcl(" + i + ")+ " + ppcl.get(i));

		int oncount = 0;
		int[] ppx = new int[2];
		for (int i = 0; i < ppcl.size(); i++)
		{
			ppcNode ppn = (ppcNode)ppcl.get(i);
			if ( ppn.isIntersection() )
				ppx[oncount++] = i;

			if ( oncount == 2 )
			{
				split_polygon(ppcl, ppx[0], ppx[1]);
				ppx[0] = ppx[1];
				oncount--;
			}
		}
	}

	private void split_polygon(ArrayList ppcl, int i1, int i2)
	{
		/*
		  At input v11-->p1-->v12 and v21--p2-->v22 are available
		     where (v11,v12) and (v21,v22) are polygon edges
		     and p1 and p2 are intersections in the respective edges.
		  Split of (v11,v12) will yield edges (v11,p1) and (p1,v12)
		  Split of (v21,v22) will yield edges (v21,p2) and (p2,v22)
		  ide1 = id of (v11,v12)
		  ide2 = id of (v21,v22)
		*/

		int px = gettosplit(ppcl, i1, i2);	/* Find out polygon to split */
//		System.out.println("\nsplit: " + px + "\n  " + ppcl.get(i1) + "\n  " + ppcl.get(i2));
		((RPolygonE)pl.get(px)).orient(el);	/* Orient polygon edges and traversal counter clockwise */

		ppcNode ppc1 = (ppcNode)ppcl.get(i1);
		ppcNode ppc2 = (ppcNode)ppcl.get(i2);

		Pointd p1 = ppc1.getp();
		Pointd p2 = ppc2.getp();

		int ide1 = ppc1.getid();
		int ide2 = ppc2.getid();

		int e1poly1 = ((REdge)el.get(ide1)).getpoly1();
		int e1poly2 = ((REdge)el.get(ide1)).getpoly2();
		int e2poly1 = ((REdge)el.get(ide2)).getpoly1();
		int e2poly2 = ((REdge)el.get(ide2)).getpoly2();

		Pointd v11 = ((REdge)el.get(ide1)).getv1();
		Pointd v12 = ((REdge)el.get(ide1)).getv2();
		Pointd v21 = ((REdge)el.get(ide2)).getv1();
		Pointd v22 = ((REdge)el.get(ide2)).getv2();

		RPolygonE ptmp = null;
		RPolygonE pold = (RPolygonE)pl.get(px);
//		System.out.println("poldid= " + pold.getid());
		RPolygonE pnew = new RPolygonE();
		pl.add(pnew);
//		System.out.println("pnewid= " + pnew.getid());

		/* Find edge (v11,v12) in polygon list */
		int e1x = pold.find(ide1);
		/* Find edge (v21,v22) in polygon list */
		int e2x = pold.find(ide2);
//		System.out.println("Split edges");
//		System.out.println("e1x= " + e1x + ", e2x= " + e2x);
//		System.out.println("ide1= " + ide1 + ", ide2= " + ide2);
//		System.out.println("ide1 = " + ((REdge)el.get(ide1)));
//		System.out.println("ide2 = " + ((REdge)el.get(ide2)));

		int nume;
		int eid;
		int eix;

		/* Case 1: Intersection points on same polygon edge                       */
		/* p1 and p2 are on edge (v11, v12) 									  */
		if (ide1 == ide2)
		{
			/* Case 1.1:   p1  = v11                                                  */
			if (p1.equals(v11))
			{
				/* Case 1.1.1: p1  = v11 & p2  = v12                                  */
				if (p2.equals(v12))
				{
					System.out.println("Case 1.1.1");
					throw undefined;
				}
				/* Case 1.1.2: p1  = v11 & p2 != v12                                  */
				else
				{
//					System.out.println("Case 1.1.2");

					/* Form new edge (p1(=v11),p2) as segment of e1 */
					/* Uses new edgeid */
					/* Uses new polyid */
					REdge npp = new REdge(p1,p2,pnew.getid(),e1poly2);
//					System.out.println("creating npp= " + npp);
					el.add(npp);
					ht.add(npp);
					pnew.add(npp.getid());
//					System.out.println("pnew= " + pnew);
					if (e1poly2 != Integer.MIN_VALUE)
					{
						ptmp =	(RPolygonE)pl.get(e1poly2);
						eix = ptmp.find(ide1); /* Sets current to ide1 */
						if (ptmp.isCCW(el, eix))
							ptmp.add(npp.getid());
						else
							ptmp.insert(npp.getid());
//						System.out.println("inserting id= " + npp.getid() + " before " + ide1);
//						System.out.println("ptmp= " + ptmp);
					}
//					System.out.println("updating ppcl " + ide1 + " " + npp.getid());
					updateppcl(ppcl,(i2+1),ide1,npp.getid());

					/* Update existing edge e1 to form edge (p2,v12) */
					/* Uses e1 edgeid */
					/* Uses e1 polyid's */
					REdge ne12= new REdge(p2,v12,ide1,e1poly1,e1poly2);
//					System.out.println("update ne12= " + ne12);
					el.set(ide1,ne12); /* also updates polygon e1poly1 and e1poly2 */
					ht.remove((REdge)el.get(ide1));
					ht.add(ne12);

					/* Insert new edges betweeen (p1,p2) and edge npp*/
					int ix = i1;
					pold.setCurrent(ide1);
					ppcNode ppn1 = (ppcNode)ppcl.get(ix);
					ppcNode ppn2 = null;
					while (ix != i2)
					{
						ppn2 = (ppcNode)ppcl.get(++ix);
						REdge re = new REdge(ppn1.getp(), ppn2.getp(), pold.getid(), pnew.getid());
//						System.out.println("adding re= " + re);
						el.add(re);
						ht.add(re);
						pold.insertNoAdvance(re.getid());
						pnew.addNoAdvance(re.getid());
						ppn1 = ppn2;
					}
//					System.out.println("pold= " + pold);
//					System.out.println("pnew= " + pnew);
				}
			}

			/* Case 1.2:   p1  = v12                                              */
			else if (p1.equals(v12))
			{
				/* Case 1.2.1: p1  = v12 & p2  = v11                              */
				if (p2.equals(v11))
				{
					System.out.println("Case 1.2.1");
					throw undefined;
				}
				/* Case 1.2.2: p1  = v12 & p2 != v11                              */
				else
				{
//					System.out.println("Case 1.2.2");

					/* Form new edge (p2,p1(=v12)) as segment of e1 */
					/* Uses new edgeid */
					/* Uses new polyid */
					REdge npp = new REdge(p2,p1,pnew.getid(),e1poly2);
//					System.out.println("creating npp= " + npp);
					el.add(npp);
					ht.add(npp);
					pnew.add(npp.getid());
//					System.out.println("pnew= " + pnew);
					if (e1poly2 != Integer.MIN_VALUE)
					{
						ptmp =	(RPolygonE)pl.get(e1poly2);
						eix = ptmp.find(ide1); /* Sets current to ide1 */
						if (ptmp.isCCW(el, eix))
							ptmp.insert(npp.getid());
						else
							ptmp.add(npp.getid());
//						System.out.println("inserting id= " + npp.getid() + " before " + ide1);
//						System.out.println("ptmp= " + ptmp);
					}
//					System.out.println("updating ppcl " + ide1 + " " + npp.getid());
					updateppcl(ppcl,(i2+1),ide1,npp.getid());

					/* Update existing edge e1 to form edge (v11,p2) */
					/* Uses e1 edgeid */
					/* Uses e1 polyid's */
					REdge ne11= new REdge(v11,p2,ide1,e1poly1,e1poly2);
//					System.out.println("update ne11= " + ne11);
					el.set(ide1,ne11); /* also updates polygon e1poly1 and e1poly2 */
					ht.remove((REdge)el.get(ide1));
					ht.add(ne11);

					/* Insert new edges betweeen (p1,p2) and edge npp*/
					int ix = i1;
					pold.setCurrent(ide1);
					ppcNode ppn1 = (ppcNode)ppcl.get(ix);
					ppcNode ppn2 = null;
					while (ix != i2)
					{
						ppn2 = (ppcNode)ppcl.get(++ix);
						REdge re = new REdge(ppn1.getp(), ppn2.getp(), pnew.getid(), pold.getid());
//						System.out.println("adding re= " + re);
						el.add(re);
						ht.add(re);
						pold.addNoAdvance(re.getid());
						pnew.insertNoAdvance(re.getid());
						ppn1 = ppn2;
					}
//					System.out.println("pold= " + pold);
//					System.out.println("pnew= " + pnew);
				}
			}

			/* Case 1.3:   p1 != v11 & p1 != v12                                 */
			else
			{
			/* Case 1.3.1: p1 != v11 & p1 != v12 & p2 = v11                           */
				if (p2.equals(v11))
				{
					System.out.println("Case 1.3.1");
					throw undefined;
				}
			/* Case 1.3.2: p1 != v11 & p1 != v12 & p2 = v12                           */
				else if (p2.equals(v12))
				{
					System.out.println("Case 1.3.2");
					throw undefined;
				}
			/* Case 1.3.3: p1 != v11 & p1 != v12 & p2 != v11 & p2 != v12              */
				else
				{
//					System.out.println("Case 1.3.3");
					Pointd pf, ps;	/* First and second point relative to v11 */
					if (Geometry.distance(v11,p1) < Geometry.distance(v11,p2))
					{
						pf = p1;
						ps = p2;
					}
					else
					{
						pf = p2;
						ps = p1;
					}

					/* Form new edge (v11,pf) as segment of e1 */
					/* Uses new edgeid */
					/* Uses e1 polyid's */
					REdge ne11 = new REdge(v11,pf,e1poly1,e1poly2);
//					System.out.println("add ne11= " + ne11);
					el.add(ne11);
					ht.add(ne11);
					/* Insert in polygon to the right */
					if (e1poly2 != Integer.MIN_VALUE)
					{
						ptmp =	(RPolygonE)pl.get(e1poly2);
						eix = ptmp.find(ide1); /* Sets current to ide1 */
						if (ptmp.isCCW(el, eix))
							ptmp.add(ne11.getid());
						else
							ptmp.insert(ne11.getid());
//						System.out.println("adding id= " + ne11.getid() + " after/before " + ide1);
//						System.out.println("ptmp= " + ptmp);
					}
					/* Insert in polygon to the left, the one being split */
					pold.setCurrent(ide1);
					pold.insert(ne11.getid());
//					System.out.println("inserting id= " + ne11.getid() + ", before " + ide1);
//					System.out.println("pold= " + pold);
					/* Update ppcl for intersections with ne11 */
//					System.out.println("updating ppcl " + ide1 + " " + ne11.getid());
					updateppcl(ppcl,(i2+1),ide1,ne11.getid());

					/* Form new edge (pf,ps) as segment of e1 */
					/* Uses new edgeid */
					/* Uses new polyid */
					REdge npp = new REdge(pf, ps, pnew.getid(), e1poly2);
//					System.out.println("creating npp= " + npp);
					el.add(npp);
					ht.add(npp);
					pnew.add(npp.getid());
//					System.out.println("pnew= " + pnew);
					if (e1poly2 != Integer.MIN_VALUE)
					{
						ptmp =	(RPolygonE)pl.get(e1poly2);
						eix = ptmp.find(ide1); /* Sets current to ide1 */
						if (ptmp.isCCW(el, eix))
							ptmp.add(npp.getid());
						else
							ptmp.insert(npp.getid());
//						System.out.println("adding id= " + npp.getid() + " after/before " + ide1);
//						System.out.println("ptmp= " + ptmp);
					}
//					System.out.println("updating ppcl " + ide1 + " " + npp.getid());
					updateppcl(ppcl,(i2+1),ide1,npp.getid());

					/* Update existing edge e1 to form edge (ps,v12) */
					/* Uses e1 edgeid */
					/* Uses e1 polyid's */
					REdge ne12= new REdge(ps,v12,ide1,e1poly1,e1poly2);
//					System.out.println("update ne12= " + ne12);
					el.set(ide1,ne12); /* also updates polygon e1poly1 and e1poly2 */
					ht.remove((REdge)el.get(ide1));
					ht.add(ne12);

					/* Insert new edges betweeen (ps,pf) and edge npp*/
					if (pf == p1)
					{
						int ix = i1;
						pold.setCurrent(ide1);
						ppcNode ppn1 = (ppcNode)ppcl.get(ix);
						ppcNode ppn2 = null;
						while (ix != i2)
						{
							ppn2 = (ppcNode)ppcl.get(++ix);
							REdge re = new REdge(ppn1.getp(), ppn2.getp(), pold.getid(), pnew.getid());
//							System.out.println("adding re= " + re);
							el.add(re);
							ht.add(re);
							pold.insertNoAdvance(re.getid());
							pnew.addNoAdvance(re.getid());
							ppn1 = ppn2;
						}
					}
					else /* pf == p2 */
					{
						int ix = i1;
						pold.setCurrent(ide1);
						ppcNode ppn1 = (ppcNode)ppcl.get(ix);
						ppcNode ppn2 = null;
						while (ix != i2)
						{
							ppn2 = (ppcNode)ppcl.get(++ix);
							REdge re = new REdge(ppn1.getp(), ppn2.getp(), pnew.getid(), pold.getid());
//							System.out.println("adding re= " + re);
							el.add(re);
							ht.add(re);
							pold.insert(re.getid());
							pnew.add(re.getid());
							ppn1 = ppn2;
						}
					}
//					System.out.println("pold= " + pold);
//					System.out.println("pnew= " + pnew);
				}
			}
		}

		else /* ide1 != ide2 */
		/* Case 2: Intersection points on different polygon edges                 */
		/*         p1 is on edge (v11, v12) and p2 is on edge (v21, v22)          */
		{
			/* Case 2.1:   p1  = v11                                              */
			if (p1.equals(v11))
			{
				/* Case 2.1.1: p1  = v11 & p2  = v21                              */
				if (p2.equals(v21))
				{
					System.out.println("Case 2.1.1");
					throw undefined;
				}
				/* Case 2.1.2: p1  = v11 & p2  = v22                              */
				else if (p2.equals(v22))
				{
					System.out.println("Case 2.1.2");
					throw undefined;
				}
				/* Case 2.1.3: p1  = v11 & p2 != v21 & p2 != v22                  */
				else
				{
//					System.out.println("Case 2.1.3 - idp1 = " + ide1 + ", idp2= " + ide2
//											+ "\np1= " + p1 + ", p2= " + p2
//											+ "\nv11-v12 = " + v11 + "-" + v12
//											+ "\nv21-v22 = " + v21 + "-" + v22);

					/* Move old edges betweeen (e2,e1) from pold to pnew */
					if (e2x < e1x)
						nume = e1x-e2x;
					else
						nume = e1x+pold.size()-e2x;
					eix = Geometry.next(e2x,pold.size());
					for (int i = 0; i < nume-1; i++)
					{
						eid = pold.remove(eix);
//						System.out.println("eid=" + eid);
						REdge re = (REdge)el.get(eid);
						eid = re.getid();
						re.updatePoly(px,pnew.getid());
						pnew.add(re.getid());
//						System.out.println("move id= " + eid + ", re= " + re);
						if (eix > pold.size()-1)
							eix = 0;
					}
//					System.out.println("pold= " + pold);
//					System.out.println("pnew= " + pnew);

					/* Insert new edges betweeen (p1,p2) */
					int ix = i1;
					pold.setCurrent(ide2);
					ppcNode ppn1 = (ppcNode)ppcl.get(ix);
					ppcNode ppn2 = null;
					while (ix != i2)
					{
						ppn2 = (ppcNode)ppcl.get(++ix);
						REdge re = new REdge(ppn1.getp(), ppn2.getp(), pnew.getid(), pold.getid());
//						System.out.println("adding re= " + re);
						el.add(re);
						ht.add(re);
						pnew.add(re.getid());
						pold.addNoAdvance(re.getid());
						ppn1 = ppn2;
					}
//					System.out.println("pold= " + pold);
//					System.out.println("pnew= " + pnew);

					/* Form new edge (p2,v22) as segment of e2 */
					/* Uses new edgeid */
					/* Uses new polyid */
					REdge ne22 = new REdge(p2, v22, pnew.getid(), e2poly2);
//					System.out.println("add ne22= " + ne22);
					el.add(ne22);
					ht.add(ne22);
					pnew.add(ne22.getid());
//					System.out.println("pnew= " + pnew);
					if (e2poly2 != Integer.MIN_VALUE)
					{
						ptmp =	(RPolygonE)pl.get(e2poly2);
						eix = ptmp.find(ide2); /* Sets current to ide2 */
						if (ptmp.isCCW(el, eix))
							ptmp.insert(ne22.getid());
						else
							ptmp.add(ne22.getid());
//						System.out.println("adding id= " + ne22.getid() + " after/before " + ide2);
//						System.out.println("ptmp= " + ptmp);
					}
					updateppcl(ppcl,(i2+1),ide2,ne22.getid());
//					System.out.println("pold= " + pold);
//					System.out.println("pnew= " + pnew);

					/* Update existing edge e2 to form edge (v21,p2) */
					/* Uses e2 edgeid */
					/* Uses e2 polyid's */
					REdge ne21= new REdge(v21, p2, ide2, e2poly1, e2poly2);
//					System.out.println("update ne21= " + ne21);
					el.set(ide2,ne21); /* also updates polygon e2poly1 */
					ht.remove((REdge)el.get(ide2));
					ht.add(ne21);
//					System.out.println("pold= " + pold);
//					System.out.println("pnew= " + pnew);

//					System.out.println("pold= " + pold);
//					System.out.println("pnew= " + pnew);

//					System.out.println("\nREdge list");
//					for (int i = 0; i < el.size(); i++)
//							System.out.println(((REdge)el.get(i)).toString());
//					System.out.println();

//					System.out.println("\nRHashtable\n" + ht);
//					System.out.println();

//					System.out.println("\nRPolygonE list");
//					for (int i = 0; i < pl.size(); i++)
//						System.out.println("i= " + i + " " + pl.get(i));
//					System.out.println();
				}
			}
			/* Case 2.2:   p1  = v12                                              */
			else if (p1. equals(v12))
			{
				/* Case 2.2.1: p1  = v12 & p2  = v21                              */
				if (p2.equals(v21))
				{
					System.out.println("Case 2.2.1");
					throw undefined;
				}
				/* Case 2.2.2: p1  = v12 & p2  = v22                              */
				else if (p2.equals(v22))
				{
					System.out.println("Case 2.2.2");
					throw undefined;
				}
				/* Case 2.2.3: p1  = v12 & p2 != v21 & p2 != v22                  */
				else
				{
//					System.out.println("Case 2.2.3 - idp1 = " + ide1 + ", idp2= " + ide2
//											+ "\np1= " + p1 + ", p2= " + p2
//											+ "\nv11-v12 = " + v11 + "-" + v12
//											+ "\nv21-v22 = " + v21 + "-" + v22);

					/* Move old edges betweeen (e2,e1) from pold to pnew */
					/* Includes v11->p1 */
					if (e2x < e1x)
						nume = e1x-e2x;
					else
						nume = e1x+pold.size()-e2x;
					eix = Geometry.next(e2x,pold.size());
					for (int i = 0; i < nume; i++)
					{
						eid = pold.remove(eix);
//						System.out.println("eid=" + eid);
						REdge re = (REdge)el.get(eid);
						eid = re.getid();
						re.updatePoly(px,pnew.getid());
						pnew.add(re.getid());
//						System.out.println("move id= " + eid + ", re= " + re);
						if (eix > pold.size()-1)
							eix = 0;
					}
//					System.out.println("pold= " + pold);
//					System.out.println("pnew= " + pnew);

					/* Insert new edges betweeen (p1,p2) */
					int ix = i1;
					pold.setCurrent(ide2);
					ppcNode ppn1 = (ppcNode)ppcl.get(ix);
					ppcNode ppn2 = null;
					while (ix != i2)
					{
						ppn2 = (ppcNode)ppcl.get(++ix);
						REdge re = new REdge(ppn1.getp(), ppn2.getp(), pnew.getid(), pold.getid());
//						System.out.println("adding re= " + re);
						el.add(re);
						ht.add(re);
						pnew.add(re.getid());
						pold.addNoAdvance(re.getid());
						ppn1 = ppn2;
					}
//					System.out.println("pold= " + pold);
//					System.out.println("pnew= " + pnew);

					/* Form new edge (p2,v22) as segment of e2 */
					/* Uses new edgeid */
					/* Uses new polyid */
					REdge ne22 = new REdge(p2, v22, pnew.getid(), e2poly2);
//					System.out.println("add ne22= " + ne22);
					el.add(ne22);
					ht.add(ne22);
					pnew.add(ne22.getid());
//					System.out.println("pnew= " + pnew);
					if (e2poly2 != Integer.MIN_VALUE)
					{
						ptmp =	(RPolygonE)pl.get(e2poly2);
						eix = ptmp.find(ide2); /* Sets current to ide2 */
						if (ptmp.isCCW(el, eix))
							ptmp.insert(ne22.getid());
						else
							ptmp.add(ne22.getid());
//						System.out.println("adding id= " + ne22.getid() + " after/before " + ide2);
//						System.out.println("ptmp= " + ptmp);
					}
					updateppcl(ppcl,(i2+1),ide2,ne22.getid());
//					System.out.println("pold= " + pold);
//					System.out.println("pnew= " + pnew);

					/* Update existing edge e2 to form edge (v21,p2) */
					/* Uses e2 edgeid */
					/* Uses e2 polyid's */
					REdge ne21= new REdge(v21, p2, ide2, e2poly1, e2poly2);
//					System.out.println("update ne21= " + ne21);
					el.set(ide2,ne21); /* also updates polygon e2poly1 */
					ht.remove((REdge)el.get(ide2));
					ht.add(ne21);
//					System.out.println("pold= " + pold);
//					System.out.println("pnew= " + pnew);

//					System.out.println("\nREdge list");
//					for (int i = 0; i < el.size(); i++)
//							System.out.println(((REdge)el.get(i)).toString());
//					System.out.println();

//					System.out.println("\nRHashtable\n" + ht);
//					System.out.println();

//					System.out.println("\nRPolygonE list");
//					for (int i = 0; i < pl.size(); i++)
//						System.out.println("i= " + i + " " + pl.get(i));
//					System.out.println();
				}
			}
			/* Case 2.3:   p1 != v11 & p1 != v12                                  */
			else
			{
				/* Case 2.3.1: p1 != v11 & p1 != v12 & p2  = v21                  */
				if (p2.equals(v21))
				{
					System.out.println("Case 2.3.1");
					throw undefined;
				}
				/* Case 2.3.2: p1 != v11 & p1 != v12 & p2  = v22                  */
				else if (p2.equals(v22))
				{
					System.out.println("Case 2.3.2");
					throw undefined;
				}
				/* Case 2.3.3: p1 != v11 & p1 != v12 & p2 != v21 & p2 != v22      */
				else
				{
//					System.out.println("Case 2.3.3 - idp1 = " + ide1 + ", idp2= " + ide2
//											+ "\np1= " + p1 + ", p2= " + p2
//											+ "\nv11-v12 = " + v11 + "-" + v12
//											+ "\nv21-v22 = " + v21 + "-" + v22);

					/* Move old edges betweeen (e2,e1) from pold to pnew */
					if (e2x < e1x)
						nume = e1x-e2x;
					else
						nume = e1x+pold.size()-e2x;
					eix = Geometry.next(e2x,pold.size());
					for (int i = 0; i < nume-1; i++)
					{
						eid = pold.remove(eix);
//						System.out.println("eid=" + eid);
						REdge re = (REdge)el.get(eid);
						eid = re.getid();
						re.updatePoly(px,pnew.getid());
						pnew.add(re.getid());
//						System.out.println("move id= " + eid + ", re= " + re);
						if (eix > pold.size()-1)
							eix = 0;
					}
//					System.out.println("pold= " + pold);
//					System.out.println("pnew= " + pnew);

					/* Form new edge (v11,p1) */
					/* Uses new edgeid */
					/* Uses new polyid */
					REdge ne11 = new REdge(v11, p1, pnew.getid(), e1poly2);
//					System.out.println("ne11= " + ne11);
					el.add(ne11);
					ht.add(ne11);
					pnew.add(ne11.getid());
					if (e1poly2 != Integer.MIN_VALUE)
					{
						ptmp =	(RPolygonE)pl.get(e1poly2);
						eix = ptmp.find(ide1); /* Sets current to ide1 */
						if (ptmp.isCCW(el, eix))
							ptmp.add(ne11.getid());
						else
							ptmp.insert(ne11.getid());
//						System.out.println("adding id= " + ne11.getid() + " after/before " + ide1);
//						System.out.println("ptmp= " + ptmp);
					}
//					System.out.println("pnew= " + pnew);
//					System.out.println("pold= " + pold);
//					System.out.println("updating ppcl " + ide1 + " " + ne11.getid());
					updateppcl(ppcl,(i2+1),ide1, ne11.getid());

					/* Update edge as segment of e1*/
					/* Uses e1 edgeid */
					/* Uses e1 polyid's */
					REdge ne12 = new REdge(p1,v12,ide1,e1poly1,e1poly2);
//					System.out.println("ne12= " + ne12);
					el.set(ide1,ne12); /* also updates polygon e1poly1 */
					ht.remove((REdge)el.get(ide1));
					ht.add(ne12);

					/* Insert new edges betweeen (p1,p2) */
//					System.out.println("pold= " + pold);
					int ix = i1;
					pold.setCurrent(ide2);
					ppcNode ppn1 = (ppcNode)ppcl.get(ix);
					ppcNode ppn2 = null;
					while (ix != i2)
					{
						ppn2 = (ppcNode)ppcl.get(++ix);
						REdge re = new REdge(ppn1.getp(), ppn2.getp(), pnew.getid(), pold.getid());
//						System.out.println("adding re= " + re);
						el.add(re);
						ht.add(re);
						pold.addNoAdvance(re.getid());
						pnew.add(re.getid());
						ppn1 = ppn2;
					}
//					System.out.println("pold= " + pold);
//					System.out.println("pnew= " + pnew);

					/* Form edge (p2,v22) */
					/* Uses new edgeid */
					/* Uses new polyid */
					REdge ne22 = new REdge(p2, v22, pnew.getid(), e2poly2);
//					System.out.println("add ne22= " + ne22);
					el.add(ne22);
					ht.add(ne22);
					pnew.add(ne22.getid());
//					System.out.println("pnew= " + pnew);
					if (e2poly2 != Integer.MIN_VALUE)
					{
						ptmp =	(RPolygonE)pl.get(e2poly2);
						eix = ptmp.find(ide2); /* Sets current to ide2 */
						if (ptmp.isCCW(el, eix))
							ptmp.insert(ne22.getid());
						else
							ptmp.add(ne22.getid());
//						System.out.println("adding id= " + ne22.getid() + " after/before " + ide2);
//						System.out.println("ptmp= " + ptmp);
					}
					else
						ptmp = null;
					updateppcl(ppcl,(i2+1),ide2,ne22.getid());
//					System.out.println("pnew= " + pnew);
//					System.out.println("ptmp= " + ptmp);

					/* Update existing edge to form edge (v21,p2) */
					/* Uses e2 edgeid */
					/* Uses e2 polyid's */
					REdge ne21 = new REdge(v21, p2, ide2, e2poly1, e2poly2);
					el.set(ide2,ne21); /* also updates polygon e2poly2 */
//					System.out.println("ne21= " + ne21);
					ht.remove((REdge)el.get(ide2));
					ht.add(ne21);

//					System.out.println("\nREdge list");
//					for (int i = 0; i < el.size(); i++)
//							System.out.println(((REdge)el.get(i)).toString());
//					System.out.println();

//					System.out.println("\nRHashtable\n" + ht);
//					System.out.println();

//					System.out.println("\nRPolygonE list");
//					for (int i = 0; i < pl.size(); i++)
//						System.out.println("i= " + i + " " + pl.get(i));
//					System.out.println();
				}

			} /* end else case 2.3 */
		} /* end if cases */

//		for (int i = 0; i < ppcl.size(); i++)
//			System.out.println("ppcl(" + i + ")+ " + ppcl.get(i));
	}

	/*
	 * Update ppcl when splitting edges
	 */
	private void updateppcl(ArrayList ppcl, int i1, int oldid, int newid)
	{
		Pointd ip;
		REdge  re;
		ppcNode ppcn;

		/* Loop thru remaining entries checking for changed edge intersections */

		for (int i = i1; i < ppcl.size(); i++)
		{
//			System.out.println("updateppcl " + i + " oldid= " + oldid + " newid= " + newid);
			ppcn = (ppcNode)ppcl.get(i);

			if (ppcn.isIntersection() && ppcn.getid() == oldid)
			{
//				System.out.println("found intersection node - oldid= " + oldid);
				ip = ppcn.getp();
				re = (REdge)el.get(newid);
//				System.out.println("v1= " + re.getv1() + " v2= " + re.getv2() + " ip= " + ip);
				if (Geometry.between(re.getv1(), re.getv2(), ip))
				{
//					System.out.println("Updating ppcn= " + ppcn
//					+ "\noldid= " + oldid + " newid = " + newid);
					ppcn.setedgeId(newid);
				}
				else
				{
//					System.out.println("No intersection detected");
				}
			}
		}
	}

	private int gettosplit(ArrayList ppcl, int i1, int i2)
	{
	//	poly index equal to Integer.MIN_VALUE implies the exterior region.

//		System.out.println("gettosplit - i1: " + i1 + " i2: " + i2);

		boolean found = false;
		int polytosplit = Integer.MIN_VALUE;

		double px, py;

		ppcNode pp1 = (ppcNode)ppcl.get(i1);
		ppcNode pp2 = (ppcNode)ppcl.get(i2);

		int ne1 = pp1.getid();
		int ne2 = pp2.getid();

		Pointd v1 = pp1.getp();
		Pointd v2 = pp2.getp();
		Pointd p = null;

		REdge e1 = (REdge)el.get(ne1);
		REdge e2 = (REdge)el.get(ne2);

		int p1b = e1.getpoly1();
		int p1i = e1.getpoly2();
		int p2b = e2.getpoly1();
		int p2i = e2.getpoly2();

		if ( p1b == Integer.MIN_VALUE)
		{
			if ( p1i == p2i )
			{
				polytosplit = p1i;
				found = true;
			}
			else if ( p2b != Integer.MIN_VALUE &&
						p1i == p2b )
			{
				polytosplit = p1i;
				found = true;
			}
		}

		if ( p1i == Integer.MIN_VALUE)
		{
			if ( p1b == p2b )
			{
				polytosplit = p1b;
				found = true;
			}
			else if ( p2i != Integer.MIN_VALUE &&
						p1b == p2i )
			{
				polytosplit = p1b;
				found = true;
			}
		}

		else if ( p2b == Integer.MIN_VALUE)
		{
			if ( p1i == p2i )
			{
				polytosplit = p2i;
				found = true;
			}
			else if ( p1b != Integer.MIN_VALUE &&
						p2i == p1b )
			{
				polytosplit = p2i;
				found = true;
			}
		}

		else if ( p2i == Integer.MIN_VALUE)
		{
			if ( p1b == p2b )
			{
				polytosplit = p2b;
				found = true;
			}
			else if ( p1i != Integer.MIN_VALUE &&
						p2b == p1i )
			{
				polytosplit = p2b;
				found = true;
			}
		}

		else if ( (p1b == p2b && p1i == p2i) ||
				  (p1b == p2i && p1i == p2b) )
		{
			if ( ppcl.get(i1+1) == pp2 )
			{
				p = new Pointd(
					((pp1.getp()).getx() + (pp2.getp()).getx())/2,
					((pp1.getp()).gety() + (pp2.getp()).gety())/2 );
			}
			else
			{
				ppcNode ppn = (ppcNode)ppcl.get(i1+1);
				p = new Pointd(
					(ppn.getp()).getx(),
					(ppn.getp()).gety() );
			}
			if ( rayCrossing(p, (RPolygonE)pl.get(p1b)) )
			{
				polytosplit = p1b;
				found = true;
			}
			else
			{
				polytosplit = p1i;
				found = true;
			}
		}

		else if ( p1b == p2b && p1i != p2i )
		{
			polytosplit = p1b;
			found = true;
		}

		else if ( p1b == p2i && p1i != p2b )
		{
			polytosplit = p1b;
			found = true;
		}

		else if ( p1b != p2i && p1i == p2b )
		{
			polytosplit = p2b;
			found = true;
		}

		else if ( p1b != p2b && p1i == p2i )
		{
			polytosplit = p1i;
			found = true;
		}

		if ( !found )
			throw illegal_state;

		return polytosplit;
	}

	private boolean rayCrossing( Pointd p, RPolygonE poly )
	{
		double px = p.getx();
		double py = p.gety();
		Pointd c = new Pointd(RXMAX, py);
		long numCross = 0;
		boolean inside = false;

		for (int i = 0; i < poly.size(); i++)
		{
			int eix = poly.get(i);
			REdge re = (REdge)el.get(eix);
			Pointd v1 = re.getv1();
			Pointd v2 = re.getv2();
			double v1y = v1.gety();
			double v2y = v2.gety();

			if ( Geometry.between(v1, v2, p) )
			{
				inside = true;
				break;
			}

			if ( (v1y > py  && v2y <= py) ||
				 (v1y <= py && v2y > py ) )
			{
				IntersectNew intobj = Geometry.SegSegIntNew(v1, v2, p, c);
				int ic = intobj.getcode();
				Pointd ip = intobj.getp();
				if ( ic > 0 && ip.getx() > px )
					numCross++;
			}

		}

		if ( numCross%2 == 1 )
			inside = true;

		return inside;
	}

	private ArrayList setnextOn(ArrayList al)
	{
		ppcNode[] pp = new ppcNode[2];
		int oncount = 0;

		for ( int i = 0; i < al.size(); i++)
		{
			ppcNode ppn = (ppcNode)al.get(i);
			if ( ppn.isIntersection() )
				pp[oncount++] = ppn;

			if ( oncount == 2 )
			{
				pp[0].setnextOn(pp[1]);
				pp[0] = pp[1];
				oncount--;
			}
		}
		return al;
	}

	private ArrayList preprocessChain(RPolyLine c)
	{
		Pointd start, end;
		Pointd ev1, ev2;
		REdge re;
		ppcNode prevppcn = null;
		int num;
		int[] eil;		/* Edge index list from hash table */

		boolean flag = false;
		ArrayList ppcl = new ArrayList();

		/* Loop through segments finding intersections */
		for (int iv = 0; iv < c.size()-1; iv++)
		{
			num = 0;
			start = c.get(iv);
			end = c.get(iv+1);
//			System.out.println("\nstart: " + start + ", end: " + end);

			/* Get set of unique edges the segment may intersect */
			/* Loop through edges, checking for intersections    */
			ArrayList partial_ppcl = new ArrayList();
			eil = ht.findUnique(start, end);

//			if (eil.length == 0)
//				System.out.println("no intersections");

//			for (int ie = 0; ie < eil.length; ie++)
//				System.out.println("eil[" + ie + "]= " + eil[ie]);

			for ( int ie = 0; ie < eil.length; ie++ )
			{
//				System.out.println("\nel.get(" + eil[ie] + ")= " + el.get(eil[ie]));
				re = (REdge)el.get(eil[ie]);
				ev1 = re.getv1();
				ev2 = re.getv2();
//				System.out.println("ev1: " + ev1 + ", ev2: " + ev2);
				if ( !re.checked() )
				{
					re.setchecked(true);

					/* Case 1: start is on polygon edge & not ev2 */
					if ( Geometry.between(ev1, ev2, start)
						 && !start.equals(ev2) )
					{
						if ( iv == 0 || num == 0 )
						{
//							System.out.println("Case 1 add: " + start);
							partial_ppcl.add(
								new ppcNode(start, re.getid(), true));
						}
						else
						{
//							System.out.println("Case 1 update");
							prevppcn.setisIntersection(true);
							prevppcn.setedgeId(re.getid());
						}
						num++;
					}

					/* Case 2: ev1 is on segment */
					else if ( Geometry.between(start, end, ev1)
							  && !ev1.equals(start) && !ev1.equals(end) )
					{
//						System.out.println("Case 2 add: " + ev1);
						partial_ppcl.add(
							new ppcNode(ev1, re.getid(), true));
						num++;
					}

					/* Case 3: Check if last vertex of chain is an intersection point */
					else if ( iv == c.size()-2
							  && Geometry.between(ev1, ev2, end) )
					{
//						System.out.println("Case 3 add: " + end);
						partial_ppcl.add(
							new ppcNode(end, re.getid(), true));
						num++;
						flag = true;
					}

					/* Case 4: Segment and current edge intersect */
					else
					{
						IntersectNew intobj = Geometry.SegSegIntNew(start, end, ev1, ev2);
//						System.out.println("intobj: " + intobj);
						int intcode = intobj.getcode();
						if ( intcode == 1 )
						{
//							System.out.println("Case 4 add: " + intobj.getp());
							partial_ppcl.add(
								new ppcNode(intobj.getp(), re.getid(), true));
							num++;
						}
					}

				}	/* End of edge checked if */


			}	/* End of hash possible edge loop */


			/* Insert last point of chain segment */
//			if ( num != 0 && !flag )
			if ( !flag )
			{
				prevppcn = new ppcNode(end, Integer.MIN_VALUE, false);
				partial_ppcl.add(prevppcn);
//				System.out.println("added end - num = " + num + ", flag= " + flag + ", "+ prevppcn);
			}
//			else
//				System.out.println("!added end - num = " + num + ", flag= " + flag + ", "+ prevppcn);

			/* Loop through edges resetting checked flag */
			for ( int ie = 0; ie < eil.length; ie++ )
			{
				re = (REdge)el.get(eil[ie]);
				re.setchecked(false);
			}

			/* Order segment points and concatenate partial chain to full chain */
			if (partial_ppcl.size() > 1)
			{
				Collections.sort(partial_ppcl);
				if (end.equals(((ppcNode)partial_ppcl.get(0)).getp()))
				{
//					System.out.println("reverse list - end= " + end);
					partial_ppcl = reverseVertices(partial_ppcl);
				}
				else
				{
//					System.out.println("no reverse list - end= " + end);
				}
			}

//			System.out.println("\n");
//			for (int i = 0; i < partial_ppcl.size(); i++)
//				System.out.println("partial_ppcl[" + i + "]= " + (ppcNode)partial_ppcl.get(i));
			ppcl.addAll(partial_ppcl);

		}	/* End segment loop */

		return ppcl;
	}

	private ArrayList reverseVertices(ArrayList al)
	{
		int n = al.size();
		ArrayList nal = new ArrayList();
		for (int i = 0; i < n; i++)
			nal.add(al.get(n-i-1));
		return nal;
	}

    class ppcNode implements Comparable
	{
		Pointd p;
		int edgeId;
		ppcNode nextOn;
		boolean isIntersection;

		public ppcNode(Pointd p, int id, boolean isIntersection)
		{
			this.p = p;
			this.edgeId = id;
			this.isIntersection = isIntersection;
			this.nextOn = null;
		}

		public void setedgeId(int i)
		{
			this.edgeId = i;
		}

		public void setnextOn(ppcNode ppn)
		{
			this.nextOn = ppn;
		}

		public void setisIntersection(boolean isIntersection)
		{
			this.isIntersection = isIntersection;
		}

		public Pointd getp()
		{
			return p;
		}

		public int getid()
		{
			return edgeId;
		}

		public ppcNode getnextOn()
		{
			return nextOn;
		}

		public boolean isIntersection()
		{
			return isIntersection;
		}

		public String toString()
		{
			Pointd nextp = null;
			if ( nextOn != null )
				nextp = nextOn.getp();
			return ("p: " + p + " id: " + edgeId + " isIntersection: " + isIntersection + " nextOn: " + nextp);
		}

		public int compareTo(Object o)
		{
			Pointd ip = ((ppcNode)o).getp();

			if ( p.equals(ip) )
				return 0;
			else if ( p.vbefore(ip) )
				return -1;
			else
				return 1;
		}

		private void setupTest()
		{
			ArrayList al;

			al = new ArrayList();		/* Temporary list of polygon edges */

			REdge r0  = new REdge(new Pointd(0.00, 0.00), new Pointd(1.00, 0.00),
				1);
			REdge r1  = new REdge(new Pointd(1.00, 0.00), new Pointd(1.00, 0.40),
				1);
			REdge r2  = new REdge(new Pointd(1.00, 1.00), new Pointd(0.00, 1.00),
				0);
			REdge r3  = new REdge(new Pointd(0.00, 1.00), new Pointd(0.00, 0.60),
				0);
			REdge r4  = new REdge(new Pointd(0.00, 0.60), new Pointd(0.00, 0.00),
				1);
			REdge r5  = new REdge(new Pointd(1.00, 0.40), new Pointd(1.00, 1.00),
				0);
	//		REdge r6  = new REdge(new Pointd(0.00, 0.60), new Pointd(0.25, 0.50),
	//			countEdge++, 0, 1);
	//		REdge r7  = new REdge(new Pointd(0.25, 0.50), new Pointd(0.45, 0.80),
	//			countEdge++, 0, 1);
			REdge r6  = new REdge(new Pointd(0.00, 0.60), new Pointd(0.22, 0.20),
				0, 1);
			REdge r7  = new REdge(new Pointd(0.22, 0.20), new Pointd(0.45, 0.80),
				0, 1);
			REdge r8  = new REdge(new Pointd(0.45, 0.80), new Pointd(1.00, 0.40),
				0, 1);

			al.add(r0);
			al.add(r1);
			al.add(r2);
			al.add(r3);
			al.add(r4);
			al.add(r5);
			al.add(r6);
			al.add(r7);
			al.add(r8);

			el.addAll(al);
			System.out.println("\nREdge list");
			for (int i = 0; i < el.size(); i++)
				System.out.println(((REdge)el.get(i)).toString());
			System.out.println();

			ht.addAll(al);
			System.out.println("\nRHashtable\n" + ht);

			pl = new ArrayList();
			al = new ArrayList();
			al.add(new Integer(2));
			al.add(new Integer(3));
			al.add(new Integer(6));
			al.add(new Integer(7));
			al.add(new Integer(8));
			al.add(new Integer(5));
			pl.add(new RPolygonE(al));

			al = new ArrayList();
	//		al.add(new Integer(0));
	//		al.add(new Integer(1));
	//		al.add(new Integer(8));
	//		al.add(new Integer(7));
	//		al.add(new Integer(6));
	//		al.add(new Integer(4));
			al.add(new Integer(4));
			al.add(new Integer(6));
			al.add(new Integer(7));
			al.add(new Integer(8));
			al.add(new Integer(1));
			al.add(new Integer(0));
			pl.add(new RPolygonE(al));

			System.out.println("\nRPolygonE list");
			for (int i = 0; i < pl.size(); i++)
				System.out.println(pl.get(i));
			Pointd[] mcz =	{new Pointd(0.00, 0.40),
							 new Pointd(0.0862745098039216, 0.4431372549019608)};
			add(new RPolyLine(mcz));

	//		Pointd[] mcy =	{new Pointd(0.5061224489795918, 0.7591836734693878),
	//						 new Pointd(0.60, 0.90),
	//						 new Pointd(0.75, 0.75),
	//						 new Pointd(0.8044444444444444, 0.5422222222222223)};
	//		add(new RPolyLine(mcy));

	//		Pointd[] mcx = 	{new Pointd(0.22, 0.20),
	//						 new Pointd(0.50, 0.25),
	//						 new Pointd(0.50, 0.50),
	//						 new Pointd(0.3670103092783505, 0.5835051546391752)};
	//		add(new RPolyLine(mcx));

	//		Pointd[] mcw =	{new Pointd(0.00, 0.40),
	//						 new Pointd(0.3670103092783505, 0.5835051546391752)};
	//		add(new RPolyLine(mcw));

			Pointd[] mca =	{new Pointd(0.00, 0.40),
							 new Pointd(0.40, 0.60),
							 new Pointd(0.60, 0.90),
							 new Pointd(1.00, 0.20)};
			add(new RPolyLine(mca));

			System.out.println("==============================================================");

	//		Pointd[] mcb =	{new Pointd(0.00, 0.25),
	//						 new Pointd(0.45, 0.75),
	//					 new Pointd(1.00, 0.25)};
	//		add(new RPolyLine(mcb));

		}
	}
    
	public void writeData(boolean draw3d, PrintWriter out) throws IOException
	{
//		System.out.println("RPolyMapPolyLines writeData - type: " + type);
		
		PolygonA pl;
		String lineOut = "";
		String parmsOut = "";
		
		if (ri != null) {
			if (ri.random)
				parmsOut += "Randomize number of lines, ";
			
			parmsOut += "Max.-number-of-lines = " + ri.Snum_items;	
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
	
	public lmInfo getRInfo() {
		return ri;
	}
}