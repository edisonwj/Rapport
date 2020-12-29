package org.edisonwj.rapport;

/**
 * Kernel List implements a doubly-linked,
 * circular list for finding a polygon kernel.
 * 
 * D. T. Lee and F. P. Preparata, An optimal algorithm for finding the kernel 
 * 		of a polygon, Journal of the ACM 26, 415-421 (1979). 
 *
 */

public class KernelList
	implements RapportDefaults
{
	// Class variables providing for inexact comparison.

	private static final double epsilon = DEFAULTEPSILON;
	private static final boolean exact_comp = EXACT_COMP;
	private static final int debug = 1;
	private static final boolean LEFT = true;
	private static final boolean RIGHT = false;

	// Inner Classes

	public class KernelNode
	{
		// KernelNode Variables
		public KernelNode pred;		/* Backward list link */
		public KernelNode succ;		/* Forward list link */
		public Pointd kv;			/* Vertex */

		// KernelNode Constructors
		public KernelNode(Pointd iv, KernelNode pr, KernelNode sc)
		{
			kv = iv;
			pred = pr;
			succ = sc;
		}

		public KernelNode(Pointd iv)
		{
			this(iv, null, null);
		}

		public void show()
		{
			System.out.println( "KernelNode"	+
								"\nthis= "  + this +
								"\npred= "	+ pred +
								"\nsucc= "	+ succ +
								"\nv= "		+ kv);
		}
		
		public String toString()
		{
			String ts = "kv: " + kv.toString();
//						+ ", pr.kv: " + pred.kv.toString()
//						+ ", sc.kv: " + succ.kv.toString();
			return ts;
		}
	}

	public class KernelListException
		extends RuntimeException
	{
		public KernelListException()
		{
			super();
		}

		public KernelListException(String s)
		{
			super(s);
		}
	}

	// List Variables
	protected KernelNode head;
	protected KernelNode tail;
	protected KernelNode current;
	protected KernelNode F;
	protected KernelNode L;
	protected int size;
	protected boolean circular;
	protected boolean star;

	protected KernelNode k1n, k2n;
	protected Pointd k1, k2, i1;

	protected KernelNode wt1n, wt2n, ws1n, ws2n, wr1n, wr2n, wun;
	protected KernelNode w1n, w2n, brn;
	protected Pointd w1, w2;

	// Polygon bounding region
	protected Pointd[] rb;
	protected double minx, miny, maxx, maxy;

	// List Exceptions
	protected KernelListException NoSuchElementException =
		new KernelListException("NoSuchElementException");
	protected KernelListException CircularListAddHeadException =
		new KernelListException("CircularListAddHeadException");
	protected KernelListException CircularListAddTailException =
		new KernelListException("CircularListAddTailException");
	protected KernelListException InvalidStateException1 =
		new KernelListException("InvalidStateException1");
	protected KernelListException InvalidStateException2 =
		new KernelListException("InvalidStateException2");
	protected KernelListException InvalidStateException3 =
		new KernelListException("InvalidStateException3");
	protected KernelListException InvalidStateException4 =
		new KernelListException("InvalidStateException4");
	protected KernelListException InvalidStateException5 =
		new KernelListException("InvalidStateException5");
	protected KernelListException InvalidStateException6 =
		new KernelListException("InvalidStateException6");
	protected KernelListException InvalidStateException7 =
		new KernelListException("InvalidStateException7");
	protected KernelListException InvalidStateException8 =
		new KernelListException("InvalidStateException8");
	protected KernelListException InvalidIntcntException =
		new KernelListException("InvalidIntcntException");

	// List Constructors
	public KernelList()
	{
//		System.out.println("KernelList() constructor");
		head = null;
		tail = null;
		current = null;
		F = null;
		L = null;
		size = 0;
		circular = false;
		star = true;
	}

	public KernelList(Pointd[] inv)
	{
		this();
//		System.out.println("KernelList(v) constructor");
		
		// Remove collinear vertices
		Pointd[] v = Geometry.removeCollinearVertices(inv);
		int n = v.length;
//		System.out.println("KernelList v.length: " + v.length);
 
		// KernelList takes the polygon vertex
		// list and finds the kernel.

		// Reorder vertices so first vertex, v0, is reflex
		v = reflexFirst(v);
//		System.out.println("\nreflexFirst - Output point set");
//		for (int i = 0; i < v.length; i++)
//			System.out.println("v[" + i + "]= " + v[i]);

		// Compute boundary intersections
		setBoundary(v);
		Pointd[] bf = new Pointd[n];
		Pointd[] br = new Pointd[n];
		for (int i = 0; i < n; i++)
		{
			bf[i] = findBoundaryInt(v[prev(i,n)], v[i], i); /* forward boundary intersection */
			diag(3,"bf[" + i + "]= " + bf[i]);
			br[i] = findBoundaryInt(v[next(i,n)], v[i], i); /* backward boundary intersection */
			diag(3,"br[" + i + "]= " + br[i]);
		}

//		Initialize list (K1) and F = Ie1v0 and L = v0e0I
		F = addTail(br[0]);
		diag(3,"Added F " + F.toString());
		addTail(v[0]);
		L = addTail(bf[0]);
		diag(3,"Added L " + L.toString());
//		show();

		int maxsize = size;
		boolean[] path = new boolean[50];
		for (int i = 0; i < path.length; i++)
			path[i] = false;

		for (int i = 1; i < n-1; i++)
		{
			path[0] = true;
			int intcnt = 0;
			w1n = w2n = wun = brn = wt1n = wt2n = ws1n = ws2n = wr1n = wr2n = null;

			diag(3,"Start iteration i = " + i + " **************************\n");
			if (isReflex(i, v))
			/* if reflex vertex */
			{
				path[1] = true;
				diag(3,"reflex vertex");
				diag(3,"check righton " + br[i] + "-" + v[next(i,n)] + " " + F.kv);
				if (Geometry.righton(br[i], v[next(i,n)], F.kv))
				/* if F lies on or right of */
				/* 		Infinity e[i+1] v[i+1] */
				{
					path[2] = true;
					diag(3,"righton");
					if (scanForwardI(F, L, br[i], v[next(i,n)]))
					/* Scan from F to L for kernel intersect */
					/* Sets:  */
					/*	w1  = intersection */
					/*  wt1 = kernel vertex node before intersection  */
					/*  wt2 = kernel vertex node after intersection   */
					{
						path[3] = true;
						intcnt++;
						w1   = i1;
						wt1n = k1n;
						wt2n = k2n;
						diag(3,"found intersection one = " + i1);
						if (scanBackwardI(F, head, br[i], v[next(i,n)]))
						/* Scan from F to list head for kernel intersect */
						/* Sets: */
						/*  w2  = intersection */
						/*  ws1 = kernel vertex node before intersection */
						/*  ws2 = kernel vertex node after intersection  */
						{
							path[4] = true;
							intcnt++;
							w2   = i1;
							ws1n = k1n;
							ws2n = k2n;
							diag(3,"found intersection two = " + i1);
							/* Replace ws2 through wt1 with w2 and w1 */
							remove(ws2n, wt1n);
							current = wt2n;
							w1n = addBefore(w1);
							w2n = addBefore(w2);
						}
						else /* Reached list head without intersection */
						{
							path[5] = true;
							diag(3,"no intersection two yet" ) ;

							/* Look for bounding intersection - scan backward from tail */
							path[7] = true;
							if (scanBackwardI(tail, L, br[i], v[next(i,n)]) && !i1.equals(w1))
							/* Scan back from tail to find kernel intersect */
							/* Sets: */
							/* w2  = intersection */
							/* wr1 = kernel vertex node before intersection */
							/* wr2 = kernel vertex node after intersection  */
							{
								path[8] = true;
								intcnt++;
								w2 = i1;
								wr1n = k1n;
								wr2n = k2n;
								diag(3,"found intersection w2 = " + i1);
								/* delete from head through wt1 */
								/* delete from wr2 through tail */
								/* add at end w2 and w1 (set list to circular) */
								remove(head, wt1n);
								remove(wr2n, tail);
								w2n = addTail(w2);
								w1n = addTail(w1);
								setCircular();
							}
							else
							{
								diag(3,"unbounded kernel");
								/* Replace head through wt1 with br[i] and w1 */
								remove(head, wt1n);
								current = wt2n;
								w1n = addBefore(w1);
								brn = addBefore(br[i]);
							}
						}

						/* Update F */
						if (intcnt == 2)
						{
							/* Two intersections */
							diag(3,"set F");
//							w2n.show();
							F = w2n;
						}
						else if (intcnt == 1)
						{
							/* One intersection */
							diag(3,"set F");
//							brn.show();
							F = brn;
						}
						else
							throw InvalidIntcntException;

						/* Update L */
						if (scanForwardLR(L, L, v[next(i,n)], LEFT))
						{
							path[9] = true;
							/* Found wu that wu+1 left of v[next(i,n)]-wu */
							diag(3,"set L");
//							wun.show();
							L = wun;
						}
						else /* Kernel must still be unbounded */
							 /* No change in L */
						{
							path[10] = true;
							diag(3,"no change in L");
						}
					}
					else /* Reached L without intersection */
						 /*   not star */
					{
						path[11] = true;
						diag(2,"Not star");
						star = false;
						break;
					}
				}

				else
				/* F lies left of       */
				/*	Infinity e[i+1] v[i+1] */
				/*  Kernel boundary unchanged */
				{
					path[12] = true;
					diag(3,"left");
					diag(3,"kernel boundary unchanged");
					/* Update F */
					if (scanForwardLR(F, F, v[next(i,n)], RIGHT))
					{
						path[13] = true;
						/* Found wu such that wu+1 right of v[next(i,n)]-wu */
						diag(3,"set F");
//						wun.show();
						F = wun;
					}
					else
					{
						showVertices(v);
						show();
						throw InvalidStateException2;
					}

					/* Update L */
					if (scanForwardLR(L, L, v[next(i,n)], LEFT))
					{
						path[14] = true;
						/* Found wu that wu+1 left of v[next(i,n)]-wu */
						diag(3,"set L");
//						wun.show();
						L = wun;
					}
					else /* Kernel still unbounded */
					     /* No change in L */
					{
						path[15] = true;
						diag(3,"no change in L");
					}
				}
			}

/*-----------------------------------------------------------------*/

			else
			/* else convex vertex */
			{
				path[16] = true;
				diag(3,"convex vertex");
				diag(3,"check rigton " + v[i] + "-" + bf[next(i,n)] + " " + L.kv);
				if (Geometry.righton(v[i], bf[next(i,n)], L.kv))
				/* if L lies on or right of */
				/* 	v[i] e[i+1]	Infinity */
				{
					path[17] = true;
					diag(3,"righton");
					if (scanBackwardI(L, F, v[i], bf[next(i,n)]))
					/* Scan from L to F for kernel intersect */
					/* Sets:  */
					/*	w1  = intersection */
					/*  wt1 = kernel vertex before intersection  */
					/*  wt2 = kernel vertex after intersection   */
					{
						path[18] = true;
						intcnt++;
						w1   = i1;
						wt1n = k1n;
						wt2n = k2n;
						diag(3,"found intersection one = " + w1 + " " + wt1n + " " + wt2n);
						if (scanForwardI(L, tail, v[i], bf[next(i,n)]))
						/* Scan from L to tail for kernel intersect */
						/* Sets: */
						/*  w2  = intersection */
						/*  ws1 = kernel vertex before intersection */
						/*  ws2 = kernel vertex after intersection  */
						{
							path[19] = true;
							intcnt++;
							w2   = i1;
							ws1n = k1n;
							ws2n = k2n;
							diag(3,"found intersection two = " + i1);
							/* Replace wt2 through ws1 with w1 and w2 */
							remove(wt2n, ws1n);
							current = wt1n;
							w1n = addAfter(w1);
							w2n = addAfter(w2);
						}
						else
						/* Reached list tail without intersection */
						{
							path[27] = true;
							diag(3,"no intersection two found yet");

							/* Look for bounding intersection - scan forward from head */
							path[29] = true;
							if (scanForwardI(head, F, v[i], bf[next(i,n)]) && !i1.equals(w1))
							/* Scan from head for kernel intersect */
							/* Sets: */
							/*  w2  = intersection */
							/*  wr1 = kernel vertex before intersection */
							/*  wr2 = kernel vertex after intersection  */
							{
								path[30] = true;
								intcnt++;
								w2   = i1;
								wr1n = k1n;
								wr2n = k2n;
								diag(3,"kernel bounded");
								diag(3,"found intersection w2 = " + i1);
								/* delete from head through wr1 */
								/* delete from wt2 through tail */
								/* add at end w1 and w2 (set list to circular) */
								remove(head, wr1n);
								remove(wt2n, tail);
								w1n = addTail(w1);
								w2n = addTail(w2);
								setCircular();
							}
							else
							{
								/* Kernel(i+1) still unbounded */
								/* Replace wt2 through tail with w1 and bf[next(i,n)] */
								diag(3,"kernel unbounded");
								remove(wt2n, tail);
								current = wt1n;
								w1n = addAfter(w1);
								brn = addAfter(bf[next(i,n)]);
							}
						}

						if (intcnt == 2)
						{
							/* Two intersections */
							/* Update F */
							if (Geometry.between(v[i], w1, v[next(i,n)]))
							{
								path[20] = true;
								diag(3,"between");
								if (scanForwardLR(F, F, v[next(i,n)], RIGHT))
								{
									path[21] = true;
									/* Found wu such that wu+1 right of v[next(i,n)]-wu */
									diag(3,"set F");
//									wun.show();
									F = wun;
								}
								else
								{
									showVertices(v);
									show();
									throw InvalidStateException3;
								}
							}
							else
							{
								path[22] = true;
								diag(3,"not between");
								diag(3,"set F");
//								w1n.show();
								F = w1n;
							}

							/* Update L */
							if (Geometry.between(v[i], w2, v[next(i,n)]))
							{
								path[23] = true;
								diag(3,"between");
								diag(3,"set L");
//								w2n.show();
								L = w2n;
							}
							else
							{
								path[24] = true;
								diag(3,"not between");
								if (scanForwardLR(w2n, w2n, v[next(i,n)], LEFT))
								{
									path[25] = true;
									/* Found wu that wu+1 left of v[next(i,n)]-wu */
									diag(3,"set L");
//									wun.show();
									L = wun;
								}
								else /* Kernel must still be unbounded */
								     /* No change in L */
								{
									path[26] = true;
									diag(3,"no change in L");
								}
							}
						}

						else if (intcnt == 1)
						{
							/* One intersection */
							/* Update F */
//							System.out.println("One intersection - update F");
							if (Geometry.between(v[i], w1, v[next(i,n)]))
							{
								path[31] = true;
								diag(3,"between " + v[i] + " " + w1 + " " + v[next(i,n)]);
//								System.out.println("v[" + i + "]= " + v[i] + "  w1= " + w1 + "  v[" + next(i,n) + "]= " + v[next(i,n)]);
								if (scanForwardLR(F, F, v[next(i,n)], RIGHT))
								{
									path[32] = true;
									/* Found wu such that wu+1 right of v[next(i,n)]-wu */
									diag(3,"set F");
//									wun.show();
									F = wun;
								}
								else
								{
									System.out.println("Did not find suitable wu");
									showVertices(v);
									show();
									throw InvalidStateException5;
								}
							}
							else
							{
								path[33] = true;
								diag(3,"not between");
								diag(3,"set F");
//								w1n.show();
								F = w1n;
							}

							/* Update L */
							if (circular)
							{
								path[34] = true;
								diag(3,"set  L (circular");
//								w2n.show();
								L = w2n;
							}
							else
							{
								path[35] = true;
								diag(3,"set L (non-circular)");
//								brn.show();
								L = brn;
							}
						}
						else
							throw InvalidIntcntException;

					}
					else /* Reached F without intersection */
						 /*	not star */
					{
						path[36] = true;
						diag(2,"Not star");
						star = false;
						break;
					}
				}

				else
				/* 		else L lies left of      */
				/* 		v[i] e[i+1] Infinity     */
				{
					/* Kernel boundary unchanged */
					path[37] = true;
					diag(3,"left");
					diag(3,"kernel boundary unchanged");
					/* Update F] */
					if (scanForwardLR(F, F, (v[next(i,n)]), RIGHT))
					{
						path[38] = true;
						/* Found wu that wu+1 right of v[next(i,n)]-wu */
						diag(3,"set F");
//						wun.show();
						F = wun;
					}
					else
					{
						showVertices(v);
						show();
						throw InvalidStateException6;
					}

					/* Update L */
					if (circular)
					{
						diag(3,"circular");
						path[39] = true;
						if (scanForwardLR(L, L, v[next(i,n)], LEFT))
						{
							/* Found wu that wu+1 left of v[next(i,n)]-wu */
							diag(3,"set L");
//							wun.show();
							L = wun;
						}
						else
						{
							showVertices(v);
							show();
							throw InvalidStateException7;
						}
					}
					else /* Kernel unbounded */
					     /* No change in L */
					{
						path[40] = true;
						diag(3,"no change in L");
					}
				}

			} /* End else reflex */

			diag(3,"End of iteration\n");
//			show();
			if (size > maxsize)
				maxsize = size;

		}	/* End for */

		diag(3,"KernelList completed - star " + star + "\n");
		diag(3,"maxsize = " + maxsize);
//		for (int i = 0; i < path.length; i++)
//			System.out.println("path[" + i + "]= " + path[i]);
	}

	private boolean isReflex(int i, Pointd[] v)
	{
		int i1, i2, i3;

		i1 = prev(i, v.length);
		i2 = i;
		i3 = next(i, v.length);
		if ( Geometry.left( v[i1], v[i2], v[i3]) )
			return false;
		else
			return true;
	}

	private Pointd[] reflexFirst(Pointd[] v)
	{
		Pointd[] u = new  Pointd[v.length];
		int k = -1;
		for (int i = 0; i < v.length; i++)
		{
			if (isReflex(i, v))
			{
				k = i;
				break;
			}
		}
		for (int i = 0; i < v.length; i++)
		{
			u[i] = v[k];
			k = next(k, v.length);
		}

		return u;
	}

	private void setBoundary(Pointd[] v)
	{
		minx = Math.floor(findMinX(v));
		miny = Math.floor(findMinY(v));
		maxx = Math.ceil(findMaxX(v));
		maxy = Math.ceil(findMaxY(v));

		/* Region Bounding Vertices */
		rb    = new Pointd[4];
//		rb[0] = new Pointd(minx, miny);
//		rb[1] = new Pointd(maxx, miny);
//		rb[2] = new Pointd(maxx, maxy);
//		rb[3] = new Pointd(minx, maxy);

		rb[0] = new Pointd(minx-.5, miny-.5);
		rb[1] = new Pointd(maxx+.5, miny-.5);
		rb[2] = new Pointd(maxx+.5, maxy+.5);
		rb[3] = new Pointd(minx-.5, maxy+.5);

//		for (int i = 0; i < rb.length; i++)
//			System.out.println("rb[" + i + "]= " + rb[i]);
	}

	private Pointd findBoundaryInt(Pointd v0, Pointd v1, int ix)
	{
//		System.out.println("Start findBoundaryInt");
//		System.out.println( 	"v0= " + v0 +
//								", v1= " + v1 +
//								"ix= " + ix);

		IntersectNew[] it = new IntersectNew[2];
		Pointd[] itp = new Pointd[2];
		int[] itc = new int[2];
		int id;

		id = findDir(v0, v1);
		for (int i = 0; i < 2; i++)
		{
			it[i] = Geometry.RaySegIntNew(v0, v1, rb[id], rb[next(id,4)]);
			itc[i] = it[i].getcode();
			id = next(id,4);
		}

		if ((itc[0] == 1 || itc[0] == 2) && (itc[1] == 1 || itc[1] == 2))
		{
			itp[0] = it[0].getp();
			itp[1] = it[1].getp();
			if (Geometry.distance(itp[0], v1) < Geometry.distance(itp[1], v1))
				return itp[0];
			else
				return itp[1];
		}
		else if (itc[0] == 1 || itc[0] == 2)
		{
			return it[0].getp();
		}
		else if (itc[1] == 1 || itc[1] == 2)
		{
			return it[1].getp();
		}
		else
			return null;
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

		return code;
	}

	private double slope(Pointd v1, Pointd v2)
	{
		return (v2.gety() - v1.gety())/(v2.getx() - v1.getx());
	}

	private KernelNode addHead(Pointd v)
	{
		KernelNode kn = new KernelNode(v);

		if (!circular)
		{
			if (head == null)
			{
				head = tail = kn;
			}
			else
			{
				kn.pred = null;
				kn.succ = head;
				head.pred = kn;
				head = kn;
			}
			current = kn;
			size++;
//			System.out.println("addHead");
//			kn.show();
			return kn;
		}
		else
			throw CircularListAddHeadException;
	}

	private KernelNode addTail(Pointd v)
	{
		KernelNode kn = new KernelNode(v);

		if (!circular)
		{
			if (head == null)
			{
				head = tail = kn;
			}
			else
			{
				kn.pred = tail;
				kn.succ = null;
				tail.succ = kn;
				tail = kn;
			}
			current = kn;
			size++;
//			System.out.println("addTail");
//			kn.show();
			return kn;
		}
		else
			throw CircularListAddTailException;
	}

	private KernelNode addBefore(Pointd v)
	{
		KernelNode kn = new KernelNode(v);

		if (current != null)
		{
			kn.succ = current;
			kn.pred = current.pred;
			if (current != head)
				(current.pred).succ = kn;
			else
				head = kn;
			current.pred = kn;
			current = kn;
			size++;
//			System.out.println("addBefore");
//			kn.show();
			return kn;
		}
		else
			return addHead(v);
	}

	private KernelNode addAfter(Pointd v)
	{
		KernelNode kn = new KernelNode(v);

		if (current != null)
		{

			kn.succ = current.succ;
			kn.pred = current;
			if (current != tail)
				(current.succ).pred = kn;
			else
				tail = kn;
			current.succ = kn;
			current = kn;
			size++;
//			System.out.println("addAfter");
//			kn.show();
			return kn;
		}
		else
			return addTail(v);
	}

	private boolean scanForwardI(KernelNode str, KernelNode end, Pointd v1, Pointd v2)
	{
		IntersectNew it;

		/* Scan forward for intersection */
		k1n = str;
		while (k1n.succ != null)
		{
			k2n = k1n.succ;
			k1 = k1n.kv;
			k2 = k2n.kv;

//			System.out.println("check intersection " + k1 + "-" + k2 + " " + v1 + "-" + v2);
			if (Geometry.between(k1, k2, v1))
			{
//				System.out.println("set intersection to vertex");
				i1 = v1;
				return true;
			}
			else
			{
				it = Geometry.SegSegIntNew(k1, k2, v1, v2);
//				System.out.println("itcode= " + it.getcode());
				if (it.getcode() == 1 || it.getcode() == 2)
				{
					i1  = it.getp();
					return true;
				}
			}

			if (k2n == end)
				break;
			else
				k1n = k2n;
		}
		return false;
	}

	private boolean scanBackwardI(KernelNode str, KernelNode end, Pointd v1, Pointd v2)
	{
		IntersectNew it;

		/* Scan backward for intersection */
		k2n = str;
		while (k2n.pred != null)
		{
			k1n = k2n.pred;
			k1 = k1n.kv;
			k2 = k2n.kv;

//			System.out.println("check intersection " + k1 + "-" + k2 + " " + v1 + "-" + v2);
			if (Geometry.between(k1, k2, v1))
			{
//				System.out.println("set intersection to vertex");
				i1 = v1;
				return true;
			}
			else
			{
				it = Geometry.SegSegIntNew(k1, k2, v1, v2);
//				System.out.println("itcode= " + it.getcode());
				if (it.getcode() == 1 || it.getcode() == 2)
				{
					i1  = it.getp();
					return true;
				}
			}

			if (k1n == end)
				break;
			else
				k2n = k1n;
		}
		return false;
	}

	private boolean scanForwardLR(KernelNode str, KernelNode end, Pointd v1, boolean left)
	{
		/* Scan forward for point to left or right */
		k1n = str;
		while (k1n.succ != null)
		{
			k2n = k1n.succ;
			k1 = k1n.kv;
			k2 = k2n.kv;

//			System.out.println("check forward left/right " + v1 + "-" + k1 + " " + k2 + " left: " + left);
			if (left)
			{
				if (Geometry.left(v1, k1, k2))
				{
//					System.out.println("left");
					wun = k1n;
					return true;
				}
			}
			else
			{
				if (Geometry.righton(v1, k1, k2))
				{
//					System.out.println("right");
					wun = k1n;
					return true;
				}
			}
			if (k2n == end)
				break;
			else
				k1n = k2n;
		}
		return false;
	}

	private boolean scanBackwardLR(KernelNode str, KernelNode end, Pointd v1, boolean left)
	{
		/* Scan backward for point to left or right */
		k2n = str;
		while (k2n.pred != null)
		{
			k1n = k2n.pred;
			k1 = k1n.kv;
			k2 = k2n.kv;

			System.out.println("check backward left/right " + v1 + "-" + k1 + " " + k2);
			if (left)
			{
				if (Geometry.left(v1, k1, k2))
				{
					System.out.println("left");
					wun = k1n;
					return true;
				}
			}
			else
			{
				if (Geometry.righton(v1, k1, k2))
				{
					System.out.println("right");
					wun = k1n;
					return true;
				}
			}
			if (k1n == end)
				break;
			else
				k2n = k1n;
		}
		return false;
	}

	private void remove()
	{
//		System.out.println("remove");
//		current.show();

		if (current == null)
			throw NoSuchElementException;

		if (circular ||
			(!circular && current != tail && current != head))
		{
			(current.pred).succ = current.succ;
			(current.succ).pred = current.pred;
			current = current.succ;
		}
		else if (current == tail)
		{
			if (current.pred != null)
				(current.pred).succ = null;
			tail = current.pred;
			current = current.pred;
		}
		else if (current == head)
		{
			if (current.succ != null)
				(current.succ).pred = null;
			head = current.succ;
			current = current.succ;
		}
		size--;
	}

	private void remove(KernelNode k)
	{
		current = k;
		remove();
	}

	private void remove(KernelNode k1, KernelNode k2)
	{
		KernelNode cur, nxt;
		nxt = k1;

		do {
			cur = nxt;
			nxt = cur.succ;
			remove(cur);
		} while (cur != k2);
	}

	private void setCircular()
	{
		head.pred = tail;
		tail.succ = head;
		head = null;
		tail = null;
		circular = true;
	}

	private void diag(int i, String s)
	{
		if (debug >= i)
			System.out.println(s);
	}

	private void showVertices(Pointd[] v)
	{
		System.out.println();
		for (int i = 0; i < v.length; i++)
			System.out.println("v[" + i + "]= " + v[i]);
		System.out.println();
	}

	private double findMinX(Pointd[] v)
	{
		double x;
		double min = Double.MAX_VALUE;

		for (int i = 0; i < v.length; i++)
		{
			x = v[i].getx();
			if (x < min)
				min = x;
		}
		return min;
	}

	private double findMaxX(Pointd[] v)
	{
		double x;
		double max = Double.MIN_VALUE;

		for (int i = 0; i < v.length; i++)
		{
			x = v[i].getx();
			if (x > max)
				max = x;
		}
		return max;
	}


	private double findMinY(Pointd[] v)
	{
		double y;
		double min = Double.MAX_VALUE;

		for (int i = 0; i < v.length; i++)
		{
			y = v[i].gety();
			if (y < min)
				min = y;
		}
		return min;
	}

	private double findMaxY(Pointd[] v)
	{
		double y;
		double max = Double.MIN_VALUE;

		for (int i = 0; i < v.length; i++)
		{
			y = v[i].gety();
			if (y > max)
				max = y;
		}
		return max;
	}

	// Public Methods

	public boolean isStar()
	{
		return star;
	}

	public int size()
	{
		return size;
	}

	public Pointd[] getKernel()
	{
		KernelNode cur = current;
		Pointd[] kv = new Pointd[size];
		for (int i = 0; i < size; i++)
		{
			kv[i] = cur.kv;
			cur = cur.succ;
		}
		return kv;
	}

	public void show()
	{
		System.out.println("KernelList");
		if (!circular)
		{
			System.out.println("head=    " + head.toString());
			System.out.println("tail=    " + tail.toString());
		}
		System.out.println("current= " + current.toString());
		System.out.println("F=       " + F.toString());
		System.out.println("L=       " + L.toString());
		System.out.println("circular= " + circular + ", star= " + star);
		System.out.println("size=    " + size);
		KernelNode kn;
		if (head != null)
			kn = head;
		else
			kn = current;

		for (int i = 0; i < size; i++)
		{
			kn.show();
			kn = kn.succ;
		}
		System.out.println();
	}

	private int next (int i, int n)
	{
	  if (i < n-1) return i+1;
	  else return 0;
	}

	private int prev(int i, int n)
	{
		if (i==0) return n-1;
		else return i-1;
	}

	public static void main(String[] args)
	{
		PolygonA pa;
		Pointd[] v = new Pointd[9];

		v[0] = new Pointd(0.1288288288288288,0.2527027027027027);
		v[1] = new Pointd(0.7506756756756757,0.2527027027027027);
		v[2] = new Pointd(0.7085585585585586,0.3914414414414415);
		v[3] = new Pointd(0.9067567567567568,0.6590090090090090);
		v[4] = new Pointd(0.7927927927927929,0.6020270270270272);
		v[5] = new Pointd(0.5945945945945946,0.7531531531531532);
		v[6] = new Pointd(0.5128378378378379,0.6689189189189190);
		v[7] = new Pointd(0.4261261261261262,0.6713963963963965);
		v[8] = new Pointd(0.3468468468468469,0.4855855855855856);
		
		pa = new PolygonA(v);

	}
}