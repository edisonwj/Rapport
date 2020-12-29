package org.edisonwj.rapport;

/**
 * RPolygonHull defines the class of polygons defined by convex hulls
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.*;

class RPolygonHull extends PolygonA implements RapportDefaults
{
	private static final double epsilon = .0000000001;
	
	protected phInfo ri;				/* Link to RInfo */

	public class RPolygonHullException
		extends RuntimeException
	{
		public RPolygonHullException()
		{
			super();
		}

		public RPolygonHullException(String s)
		{
			super(s);
		}
	}

	protected RPolygonHullException invalidquad =
		new RPolygonHullException("invalid quad state");

	protected RPolygonHullException invalidsect =
		new RPolygonHullException("invalid sector state");

	protected RPolygonHullException unexpected =
		new RPolygonHullException("unexpected state");

	public RPolygonHull(phInfo ri, Rand mr, Rand mr2)	/* Generate polygon using hull method */
	{
		super(ri.nv);
		this.ri = ri;
//		System.out.println("RPolygonHull Constructor - title: " + ri.title);

		if (ri.a)
			genPolyHullA(ri, mr, mr2);	/* Basic hull method */
		else if (ri.b)
			genPolyHullB(ri, mr, mr2 );	/* Convex-stable method */
		else if (ri.c)
			genPolyHullC(ri, mr, mr2);	/* Convex hull of finite no. points - circle */
		else if (ri.d)
			genPolyHullD(ri, mr, mr2);	/* Alternate circle method */
		else if (ri.e)
			genPolyHullE(ri, mr, mr2);	/* Incremental hull method */

		if (this.size() > 0)
		{
			if (!this.Ccw())
				this.Reverse();
			this.computeMetrics();
			if (ri.color)
				setColor();
		}
	}

	/*
	 * Basic hull method. Generate a set of random points
	 * in the unit square and find the convex hull.
	 */
	private void genPolyHullA(phInfo ri, Rand mr, Rand mr2)
	{
		int i;
		Pointd p, pc;

  	/* Generate ri.nv random points */
//		int n = mr2.uniform(3, ri.nv);
//		int n = (int)Math.ceil(mr2.exponential(3.0, (double)ri.nv));
		int n = ri.nv;

		Pointd[] random_points = new Pointd[n];
		for (i=0; i<n; i++)
			random_points[i] = new Pointd(mr.uniform(), mr.uniform());

	/* Find convex hull */
		Hull h = new Hull(random_points);

	/* Save hull_points as polygon */
		this.set(h.getPoints());
		this.setHull(h);
	}

	/*
	 * Isotropic convex-stable sets. Based on Molchanov and Stoyan:
	 * "Statistical Models of Random Polyhedra."
	 * Commun. Statist.-Stochastic Models, 12(2) 199-214 (1996).
	 */
	private void genPolyHullB(phInfo ri, Rand mr, Rand mr2)
	{
		int n = ri.nv;
		double alpha = ri.alpha;

		RPointp pp;
		double e, g, t, ea, p0;
		Pointd[] rp = new Pointd[n];
		for (int i = 0; i < n; i++)
		{
			do
			{
				e = Math.tan(Math.PI*mr.uniform()/2.0);
				g = 2.0/(Math.PI*(1+e*e));
				t = mr.uniform(0.0,g);
				ea = 1.0 + Math.pow(e,2.0+alpha);
				p0 = e/(ea*Math.PI);
//				System.out.println("HullB: " + "i= " + i
//									+ ", e= " + e
//									+ ", g= " + g
//									+ ", t= " + t
//									+ ", ea= " + ea
//									+ ", p0= " + p0);
			} while (p0 < t);
			pp = new RPointp(e, mr.uniform(0.0, 2*Math.PI), 0);
//			System.out.println("pp= " + pp.toString());
			rp[i] = new Pointd(pp.getx(), pp.gety());
//			System.out.println("rp[" + i + "]= " + rp[i]);
		}

		/* Find convex hull */
		Hull h = new Hull(rp);

		/* Save hull_points as polygon */
		this.set(h.getPoints());
		this.scaleToUnitSquare();
//		this.setHull(h);
	}

	/*
	 * Circle hull method. Generate set of random points and
	 * generate random circle to select subset for forming hull.
	 * Variation of convex hulls of a finite number of points per
	 * Molchanov and Stoyan, "Statistical Models of Random Polyhedra."
	 * Commun. Statist.-Stochastic Models, 12(2) 199-214 (1996)
	 */
	private void genPolyHullC(phInfo ri, Rand mr, Rand mr2)
	{
		int i;
		Pointd p, pc;

  	/* Generate ri.nv random points */
//		int n = mr.uniform(3, ri.nv);
		int n = (int)Math.ceil(mr2.exponential(3.0, (double)ri.nv));
//		int n = ri.nv;

	/* Use random circle to constrain points */
		double r = mr.uniform(.05, .95);
//		double r = mr.uniform(.05,.90);
//		double r = mr.uniform(.1,.9);
//		double r = mr.exponential(.1, .9);
//		double r = mr.Normal(.35,.10);
		double rx = r/(2.0*Math.PI);
//		double rx = r/6;
//		double rx = r/7;
		pc = new Pointd(mr.uniform(0.0-rx, 1.0+rx), mr.uniform(0.0-rx, 1.0+rx));
//		pc = new Pointd(mr.uniform(0.0, 1.0), mr.uniform(0.0, 1.0));

		Pointd[] random_points = new Pointd[n];
		for (i=0; i<n; i++)
		{
			do
			{
				p = new Pointd(mr.uniform(), mr.uniform());
			} while (!p.inCircle(r, pc) );

			random_points[i] = p;
		}

	/* Find convex hull */
		Hull h = new Hull(random_points);

	/* Save hull_points as polygon */
		this.set(h.getPoints());
		this.setHull(h);
	}

	/*
	 * Alternate circle method.
	 */
	private void genPolyHullD(phInfo ri, Rand mr, Rand mr2)
	{
		int i;
		double r, a, x, y;

		/* Generate random points in the unit disc */
		Pointd[] random_points = new Pointd[ri.nv];
		for (i = 0; i < ri.nv; i++)
		{
			r = mr.uniform();
			a = mr.uniform(0,2*Math.PI);
			x = r*Math.cos(a);
			y = r*Math.sin(a);
			random_points[i] = new Pointd(x,y);
		}

		/* Find convex hull */
		Hull h = new Hull(random_points);
		//	System.out.println("Hull created: " + h.size());

		/* Save hull_points as polygon */
		this.set(h.getPoints());
		this.setHull(h);


//		Pointd p, pc;
//
//  	/* Generate circle radius and center */
//		double r = mr.uniform();
//		double r = mr.uniform(.05, .95);
//		double r = mr.uniform(.1,.9);
//		double r = mr.exponential(.1, .9);
//		double r = mr.Normal(.35,.10);

//		pc = new Pointd(mr.uniform(), mr.uniform());
//		double rx = r/(2.0*Math.PI);
//		pc = new Pointd(mr.uniform(0.0-rx, 1.0+rx), mr.uniform(0.0-rx, 1.0+rx));
//		double cx = pc.getx();
//		double cy = pc.gety();

//		double mx = Math.min(cx, 1.0-cx);
//		double my = Math.min(cy, 1.0-cy);
//		double md = Math.min(mx,my);
//		if (r > md)
//			r = md;

//		double minx = cx - r;
//		if (minx < 0.0) minx = 0.0;
//		double maxx = cx + r;
//		if (maxx > 1.0) maxx = 1.0;
//		double miny = cy - r;
//		if (miny < 0.0) miny = 0.0;
//		double maxy = cy + r;
//		if (maxy > 1.0) maxy = 1.0;
//		double prob = (maxx-minx)*(maxy-miny);
//		if (prob >= 1.0) prob = .99;
//		if (prob <= 0.0) prob = .01;
//
  	/* Generate ri.nv random points */
//		System.out.println("Get binomial " + (ri.nv-3) + " " + prob);
//		int n = mr.binomial(ri.nv-3, prob) + 3;
//		int n = mr.uniform(3, ri.nv);
//		int n = (int)Math.ceil(mr2.exponential(3.0, (double)ri.nv));
//		int n = ri.nv;
//		System.out.println(""+r+" "+pc+" "+
//				minx+ " " +maxx+" " +miny+" " +maxy+" " +prob+" "+n);

	/* Generate points within the box*/

//		Pointd[] random_points = new Pointd[n];
//		for (i=0; i<n; i++)
//		{
//			do
//			{
//				p = new Pointd(mr.uniform(minx, maxx), mr.uniform(miny, maxy));
//			} while (!p.inCircle(r, pc) );
//
//			random_points[i] = p;
//		}
//		System.out.println("Points created: " +i);

	/* Find convex hull */
//		Hull h = new Hull(random_points);
//		System.out.println("Hull created: " + h.size());

	/* Save hull_points as polygon */
//		this.set(h.getPoints());
//		this.setHull(h);
//		System.out.println("Polygon created: " + this.size());

	}

	/*
	 * Incremental hull method. Generate random triangle.
	 * Successivley pick a random edge and replace with a new
	 * convex vertex and edges until required size reached.
	 */
	private void genPolyHullE(phInfo ri, Rand mr, Rand mr2)
	{
		int itc, vi0, vi1, vi2, vi3;
		Pointd itp, v0, v1, v2, v3;
		Pointd rp = null;

		int n = ri.nv;
		/* Randomize n */
		if (n > 10)
			n = mr.uniform(10,n);
		ArrayList vll = new ArrayList();		/* Vertex list */
		ArrayList intp;
		Pointd[] vt = new Pointd[3];		/* Triangle of vertices */
		Pointd[] vq = new Pointd[4];		/* Quadrilateral of vertices */

	  	/* Generate random triangle */
		Pointd[] random_points = new Pointd[n];
		for (int i=0; i<3; i++)
			vll.add(new Pointd(mr.uniform(), mr.uniform()));
		if (Geometry.twice_area((Pointd)vll.get(0), (Pointd)vll.get(1), (Pointd)vll.get(2)) < 0)
			reverse(vll);

//		System.out.println("\nInitial triangle");
//		for (int i = 0; i < 3; i++)
//			System.out.println("vll(" + i + ")= " + (Pointd)vll.get(i));

		/* Pick random edge and add new vertex and edges */
		for (int i = 3; i < n; i++)
		{
			vi0 = mr.uniform(0,i-1);
			vi1 = next(vi0,i);
			vi2 = next(vi1,i);
			vi3 = next(vi2,i);
			v0 = (Pointd)vll.get(vi0);
			v1 = (Pointd)vll.get(vi1);
			v2 = (Pointd)vll.get(vi2);
			v3 = (Pointd)vll.get(vi3);

//			System.out.println(   "vi0= " + vi0 +
//								"\nvi1= " + vi1 +
//								"\nvi2= " + vi2 +
//								"\nvi3= " + vi3);
			if (i == 3)
			{
				intp = findBoundaryInt(v0, v1, v2, v3);
				vq = new Pointd[intp.size()+2];
				vq[0] = v1;
				for (int j = 0; j < intp.size(); j++)
					vq[j+1] = (Pointd)intp.get(j);
				vq[vq.length-1] = v2;

//				for (int j = 0; j < vq.length; j++)
//					System.out.println(   "vq[" + j + "]= " + vq[j]);
				rp = pointInD(mr, vq);
//				System.out.println("$$$$rp1= " + rp);
			}

			else
			{
				/* Find region and generate new random point */
				IntersectNew it = Geometry.RayRayIntNew(v0, v1, v3, v2);
				itc = it.getcode();
				itp = it.getp();
//				System.out.println("it.getcode()= " + itc + ", it.getp()= " + itp);
				if ((itc == 1 || itc == 2)&& inSquare(itp))
				{
//					System.out.println("ip right and insquare: " + itp);
					/* compute random point in triangle */
					vt[0] = v1;
					vt[1] = v2;
					vt[2] = itp;
					rp = pointInD(mr, vt);
//					System.out.println("$$$$rp2= " + rp);
				}
				else
				{
//					System.out.println("no intersection in square");
					intp = findBoundaryInt(v0, v1, v2, v3);
					vq = new Pointd[intp.size()+2];
					vq[0] = v1;
					for (int j = 0; j < intp.size(); j++)
						vq[j+1] = (Pointd)intp.get(j);
					vq[vq.length-1] = v2;

//					for (int j = 0; j < vq.length; j++)
//						System.out.println(   "vq[" + j + "]= " + vq[j]);
					rp = pointInD(mr, vq);
//					System.out.println("$$$$rp3= " + rp);
				}
			}

			vll.add(vi2,rp);
//			System.out.println("Polygon at i= " + i);
//			for (int k=0; k < vll.size(); k++)
//				System.out.println("vll[" + k + "]= " + (Pointd)vll.get(k));
		}

		Pointd[] va = new Pointd[vll.size()];
		for (int i = 0; i < va.length; i++)
			va[i] = (Pointd)vll.get(i);

		/* Find convex hull */
		Hull h = new Hull(va);

		/* Save hull_points as polygon */
		this.set(h.getPoints());
		this.setHull(h);
	}

	public Pointd[] boundaryIntR(Pointd inp, Pointd v0, Pointd v1, Pointd v2, Pointd v3)
	{
		int quad = inQuad(inp);
		IntersectNew it;
		Pointd[] ip = new Pointd[2];

		/* Region Bounding Vertices */
		Pointd[] rb = { new Pointd(1.0, 0.0),
						new Pointd(1.0, 1.0),
						new Pointd(0.0, 1.0),
						new Pointd(0.0, 0.0)};

		int bi1 = quad;
		int bi2 = next(bi1,4);
		int bi3 = next(bi2,4);
		it = Geometry.SegSegIntNew(v0, v1, rb[bi1], rb[bi2]);
		if (it.getcode() == 1)
		{
			ip[0] = it.getp();
			it = Geometry.SegSegIntNew(v2, v3, rb[bi1], rb[bi2]);
			if (it.getcode() == 1)
				ip[1] = it.getp();
			else
			{
				it = Geometry.SegSegIntNew(v2, v3, rb[bi2], rb[bi3]);
				if (it.getcode() == 1)
					ip[1] = it.getp();
				else
					throw invalidquad;
			}
		}

		else
		{
			it = Geometry.SegSegIntNew(v0, v1, rb[bi2], rb[bi3]);
			if (it.getcode() == 1)
			{
				ip[0] = it.getp();
				it = Geometry.SegSegIntNew(v2, v3, rb[bi2], rb[bi3]);
				if (it.getcode() == 1)
					ip[1] = it.getp();
				else
					throw invalidquad;
			}
			else
				throw invalidquad;
		}

		return ip;
	}

	public Pointd[] boundaryIntL(Pointd inp, Pointd v0, Pointd v1, Pointd v2, Pointd v3)
	{
		int bi1, bi2, bi3, bi4;
		IntersectNew it;
		Pointd[] ip = new Pointd[2];

		/* Region Bounding Vertices */
		Pointd[] rb = { new Pointd(1.0, 0.0),
						new Pointd(1.0, 1.0),
						new Pointd(0.0, 1.0),
						new Pointd(0.0, 0.0)};

		int sect = inSect(inp);
		if (sect%2 == 0)
		{
			bi1 = sect/2;
			bi2 = next(bi1,4);
			bi3 = next(bi2,4);

			it = Geometry.SegSegIntNew(v0, v1, rb[bi1], rb[bi2]);
			if (it.getcode() == 1)
			{
				ip[0] = it.getp();
				it = Geometry.SegSegIntNew(v2, v3, rb[bi1], rb[bi2]);
				if (it.getcode() == 1)
					ip[1] = it.getp();
				else
				{
					it = Geometry.SegSegIntNew(v2, v3, rb[bi2], rb[bi3]);
					if (it.getcode() == 1)
						ip[1] = it.getp();
					else
						throw invalidsect;
				}
			}

			else
			{
				it = Geometry.SegSegIntNew(v0, v1, rb[bi2], rb[bi3]);
				if (it.getcode() == 1)
				{
					ip[0] = it.getp();
					it = Geometry.SegSegIntNew(v2, v3, rb[bi2], rb[bi3]);
					if (it.getcode() == 1)
						ip[1] = it.getp();
					else
						throw invalidsect;
				}
				else
					throw invalidsect;
			}
		}
		else
		{
			bi1 = sect/2;
			bi2 = next(bi1,4);
			bi3 = next(bi2,4);
			bi4 = next(bi3,4);

			it = Geometry.SegSegIntNew(v0, v1, rb[bi1], rb[bi2]);
			if (it.getcode() == 1)
			{
				ip[0] = it.getp();
				it = Geometry.SegSegIntNew(v2, v3, rb[bi1], rb[bi2]);
				if (it.getcode() == 1)
					ip[1] = it.getp();
				else
				{
					it = Geometry.SegSegIntNew(v2, v3, rb[bi2], rb[bi3]);
					if (it.getcode() == 1)
						ip[1] = it.getp();
					else
					{
						it = Geometry.SegSegIntNew(v2, v3, rb[bi3], rb[bi4]);
						if (it.getcode() == 1)
							ip[1] = it.getp();
						else
							throw invalidsect;
					}
				}
			}
			else
			{
				it = Geometry.SegSegIntNew(v0, v1, rb[bi2], rb[bi3]);
				if (it.getcode() == 1)
				{
					ip[0] = it.getp();
					it = Geometry.SegSegIntNew(v2, v3, rb[bi2], rb[bi3]);
					if (it.getcode() == 1)
						ip[1] = it.getp();
					else
						throw invalidsect;
				}
				else
				{
					it = Geometry.SegSegIntNew(v0, v1, rb[bi3], rb[bi4]);
					if (it.getcode() == 1)
					{
						ip[0] = it.getp();
						it = Geometry.SegSegIntNew(v2, v3, rb[bi3], rb[bi4]);
						if (it.getcode() == 1)
							ip[1] = it.getp();
						else
							throw invalidsect;
					}
				}
			}
		}
		return ip;
	}

	public boolean inSquare(Pointd p)
	{
		double x = p.getx();
		double y = p.gety();
		if (0.0 <= x && x <= 1.0 && 0.0 <= y && y <= 1.0)
			return true;
		else
			return false;
	}

	public int inQuad(Pointd p)
	{
		int code = 0;
		double x = p.getx();
		double y = p.gety();

		if (	x >= 0.0 && y >= 0.0)
			code = 0;
		else if (x < 0.0 && y >= 0.0)
			code = 1;
		else if (x < 0.0 && y <  0.0)
			code = 2;
		else if (x > 0.0 && y <  0.0)
			code = 3;
		return code;
	}

	public int inSect(Pointd p)
	{
		int code = 0;
		double x = p.getx();
		double y = p.gety();

		if		(x > 1.0)
		{
			if		(y < 0.0)
				code = 0;
			else if (y > 0.0 && y < 1.0)
				code = 1;
			else if (y > 1.0)
				code = 2;
		}
		else if (x > 0.0 && x < 1.0)
		{
			if		(y < 0.0)
				code = 7;
			else if (y > 0.0 && y < 1.0)
				code = 8;
			else if (y > 1.0)
				code = 3;
		}
		else if (x < 0.0)
		{
			if		(y < 0.0)
				code = 6;
			else if (y > 0.0 && y < 1.0)
				code = 5;
			else if (y > 1.0)
				code = 4;
		}
		return code;
	}

	public Pointd pointInD(Rand mr, Pointd[] v)
	{
		int n = v.length;
		double[] ran = new double[n-1];
		double[] space = new double[n];
		double x = 0.0;
		double y = 0.0;

		for (int i = 0; i < n-1; i++)
			ran[i] = mr.uniform();
		Arrays.sort(ran);

		space[0] = ran[0];
		for (int i = 0; i < n-2; i++)
			space[i+1] = ran[i+1] - ran[i];
		space[n-1] = 1.0 - ran[n-2];

		for (int i = 0; i < n; i++)
		{
			x += space[i]*v[i].getx();
			y += space[i]*v[i].gety();
		}
		if (x < 0 || y < 0 )
		{
			System.out.println("\npointInD - p: " + new Pointd(x,y));
			for (int i = 0; i < v.length; i++)
				System.out.println("pointInD - v[" + i + "]: " + v[i]);
		}
		return new Pointd(x,y);
	}

	public ArrayList findBoundaryInt(Pointd v0, Pointd v1, Pointd v2, Pointd v3)
	{
//		System.out.println("Start findBoundaryInt");
//		System.out.println("v0= " + v0 +
//							", v1= " + v1 +
//							", v2= " + v2 +
//							", v3= " + v3 );

		int id, id1, id2, itc;
		IntersectNew it;
		Pointd itp;
		double itx, ity;
		ArrayList ip = new ArrayList();

		/* Region Bounding Vertices */
		Pointd[] rb = { new Pointd(0.0, 0.0),
						new Pointd(1.0, 0.0),
						new Pointd(1.0, 1.0),
						new Pointd(0.0, 1.0)};

		id1 = -1;
		id2 = -1;
		id = findDir(v0, v1);
		for (int i = 0; i < 2; i++)
		{
			it = Geometry.RaySegIntNew(v0, v1, rb[id], rb[next(id,4)]);
//			System.out.println("i= " + i + ", it= " + it);
			itc = it.getcode();
			itp = it.getp();
			itx = itp.getx();
			ity = itp.gety();
			if (itx < 0.0 && Math.abs(itx) < epsilon)
			{
//				System.out.println("+1+Adjust itx= " + itx);
				itx = 0.0;
			}
			if (itx > 1.0 && Math.abs(itx-1.0) < epsilon)
			{
//				System.out.println("-1-Adjust itx= " + itx);
				itx = 1.0;
			}
			if (ity < 0.0 && Math.abs(ity) < epsilon)
			{
//				System.out.println("+1+Adjust ity= " + ity);
				ity = 0.0;
			}
			if (ity > 1.0 && Math.abs(ity-1.0) < epsilon)
			{
//				System.out.println("-1-Adjust ity= " + ity);
				ity = 1.0;
			}
			if ((itc == 1 || itc == 2) &&
				 itx >= 0.0 && itx <= 1.0 &&
				 ity >= 0.0 && ity <= 1.0 &&
				 Geometry.right(v1, v2, itp))
			{
				ip.add(new Pointd(itx, ity));
				id1 = id;
				break;
			}
			else
			{
//				System.out.println("*1*itxd= " + itx + ", ityd= " + ity);
				id = next(id,4);
			}
		}

		id = findDir(v3, v2);
		for (int i = 0; i < 2; i++)
		{
			it = Geometry.RaySegIntNew(v3, v2, rb[id], rb[next(id,4)]);
//			System.out.println("i= " + i + ", it= " + it);
			itc = it.getcode();
			itp = it.getp();
			itx = itp.getx();
			ity = itp.gety();
			if (itx < 0.0 && Math.abs(itx) < epsilon)
			{
//				System.out.println("+2+Adjust itx= " + itx);
				itx = 0.0;
			}
			if (itx > 1.0 && Math.abs(itx-1.0) < epsilon)
			{
//				System.out.println("-2-Adjust itx= " + itx);
				itx = 1.0;
			}
			if (ity < 0.0 && Math.abs(ity) < epsilon)
			{
//				System.out.println("+2+Adjust ity= " + ity);
				ity = 0.0;
			}
			if (ity > 1.0 && Math.abs(ity-1.0) < epsilon)
			{
//				System.out.println("-2-Adjust ity= " + ity);
				ity = 1.0;
			}
			if ((itc == 1 || itc == 2) &&
				 itx >= 0.0 && itx <= 1.0 &&
				 ity >= 0.0 && ity <= 1.0 &&
				 Geometry.right(v1, v2, itp))
			{
				ip.add(new Pointd(itx,ity));
				id2 = id;
				break;
			}
			else
			{
//				System.out.println("*2*itxd= " + itx + ", ityd= " + ity);
				id = next(id,4);
			}
		}

		if (id1 >= 0 && id2 >= 0)
		{
			for (int i = id1, j = 1; i != id2; i = next(i,4))
			{
				ip.add(j,rb[next(i,4)]);
				j++;
			}

//			for (int i = 0; i < ip.size(); i++)
//				System.out.println("ip(i)= " + (Pointd)ip.get(i));

			return ip;
		}
		else
		{
			System.out.println("Did not find boundary intersections");
			return null;
		}
	}

	public int findDir(Pointd v1, Pointd v2)
	{
		double xdiff;
		double ydiff;
		int code = -1;

		xdiff = v2.getx() - v1.getx();
		ydiff = v2.gety() - v1.gety();

		if (xdiff >= 0.0 && ydiff >= 0.0)
			code = 1;
		else if (xdiff <  0.0 && ydiff >= 0.0)
			code = 2;
		else if (xdiff <  0.0 && ydiff <  0.0)
			code = 3;
		else if (xdiff >= 0.0 && ydiff <  0.0)
			code = 0;

//		System.out.println("findDir code= " + code);

		return code;
	}

	/* Reverses triangle vertex order to make it Ccw
	*/
	public void reverse(ArrayList al)
	{
		Pointd temp = (Pointd)al.get(1);
		al.set(1, (Pointd)al.get(2));
		al.set(2, temp);
	}
	
	public void writeData(boolean draw3d, PrintWriter out) throws IOException
	{
//		System.out.println("RPolygonHull writeData - type: " + type);
		
		String lineOut = "";
		String parmsOut = "";
		String method = "";
		
		if (xcolor != null)
			parmsOut += "Color " + xcolor + " ";
		else
			parmsOut += "Color None ";
		
		if (ri != null) {
			if (ri.a)
				method = "Unit Square";
			else if (ri.b)
				method = "Convex Stable Set";
			else if (ri.c)
				method = "Random Disc";
			else if (ri.d)
				method = "Unit Disc";
			else if (ri.e)
				method = "Incremental Convex";
			else
				method = "Unknown";
			
			parmsOut += "Method = " + method + 
						", Number-random-points = " + ri.Snv +
						", Alpha = " + ri.Salpha;
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
	
	public phInfo getRInfo() {
		return ri;
	}
}
