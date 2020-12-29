package org.edisonwj.rapport;

/**
 * Wedge List implements an ordered doubly-linked,
 * circular list for wedges.
 */

public class WedgeList
	implements RapportDefaults
{
	// Class variables providing for inexact comparison.

	private static final double epsilon = DEFAULTEPSILON;
	private static final boolean exact_comp = EXACT_COMP;
	private static final int debug = DEBUG;

	// Inner Classes

	public class WedgeNode
	{
		// WedgeNode Variables
		public WedgeNode pred;		/* Backward list link */
		public WedgeNode succ;		/* Forward list link */
		public int eid;				/* Edge identifier */
		public int forwardMult;		/* Forward wedge multiplicity */
		public int backwardMult;	/* Backward wedge multiplicity */
		public double angle;		/* Edge polar angle */

		// WedgeNode Constructors
		public WedgeNode(WedgeNode pr, WedgeNode sc, int id, int fm, int bm, double ang)
		{
			pred = pr;
			succ = sc;
			eid = id;
			forwardMult = fm;
			backwardMult = bm;
			angle = ang;
		}

		public WedgeNode(int id, double ang)
		{
			this(null, null, id, 0, 0, ang);
		}

		public boolean equals(WedgeNode b)
		{
			if (exact_comp)
			{
				if ( this.angle == b.angle )
					return true;
				else
					return false;
			}
			else
			{
				if ( Math.abs(this.angle - b.angle) < epsilon )
					return true;
				else
					return false;
			}
		}

		public boolean less(WedgeNode b)
		{
			if (exact_comp)
			{
				if ( this.angle < b.angle )
					return true;
				else
					return false;
			}
			else
			{
				if ( b.angle - this.angle > epsilon )
					return true;
				else
					return false;
			}
		}

		public void show()
		{
			System.out.println("WedgeNode"	+
								"\npred= "	+ pred +
								"\nsucc= "	+ succ +
								"\nthis= "	+ this +
								"\nid= "		+ eid +
								" fm= "		+ forwardMult +
								" bm= "		+ backwardMult +
								" ang= "	+ angle +
								" degrees= " + angle*180.0/Math.PI);
		}

		public void showshort()
		{
			System.out.println("WedgeNode"	+
								"id= "		+ eid +
								" fm= "		+ forwardMult +
								" bm= "		+ backwardMult +
								" degrees= " + angle*180.0/Math.PI);
		}
	}

	public class WedgeListException
		extends RuntimeException
	{
		public WedgeListException()
		{
			super();
		}

		public WedgeListException(String s)
		{
			super(s);
		}
	}

	// List Variables
	protected WedgeNode first;
	protected WedgeNode last;
	protected WedgeNode current;
	protected WedgeNode lastedge;
	protected int listsize;
	protected double[] rang;			/* Relative polar edge angles */
	protected WedgeNode[] wl;			/* Wedge node list entries */
	protected boolean wedge1;

	// List Exceptions
	protected WedgeListException erroneous_state1 =
		new WedgeListException("erroneous state1");
	protected WedgeListException erroneous_state2 =
		new WedgeListException("erroneous state2");
	protected WedgeListException erroneous_state3 =
		new WedgeListException("erroneous state3");
	protected WedgeListException erroneous_angle_order =
		new WedgeListException("erroneous angle order");
	protected WedgeListException erroneous_multiplicity =
		new WedgeListException("erroneous multiplicity");
	protected WedgeListException erroneous_multiplicity_order =
		new WedgeListException("erroneous multiplicity order");
	protected WedgeListException erroneous_single_entry =
		new WedgeListException("erroneous single entry");

	// List Constructors
	public WedgeList()
	{
		first = null;
		last = null;
		current = null;
		lastedge = null;
		listsize = 0;
	}

	public WedgeList(double[] rang)
	{
		// WedgeList takes the polar diagram as input.
		// Find edge with least polar angle.

//		j = findMin(pang);
//		double mina = pang[j];
//
//		// Relativize all edge angles to edge with least angle.
//		// Rotate all edges clockwise, so least angle is zero.
//		// Make the least angle (edge) the first.
//
//		rang = new double[pang.length];
//		for (i = 0; i < pang.length; i++)
//		{
//			rang[i] = pang[j] - mina;
//			j = next(j, pang.length);
//		}

		if (debug > 2)
		{
			System.out.println("\nInitial Angle List");
			for (int i = 0; i < rang.length; i++)
				System.out.println("rang[" + i + "]= " + rang[i] + " " + rang[i]*180.0/Math.PI);
			System.out.println();
		}

		// Create framework for wedge nodes
		wl = new WedgeNode[rang.length];

		// Initialize first wedge node

		wl[0] = new WedgeNode(0, rang[0]);
		first = wl[0];
		last = wl[0];
		current = wl[0];
		current.pred = current;
		current.succ = current;
		listsize = 1;
		if (debug > 2)
			show();

		// Build wedge list
		for (int i = 1; i < rang.length; i++)
		{
			wl[i] = new WedgeNode(i, rang[i]);
			if (debug > 2)
			{
				System.out.println("Inserting wl[" + i + "]");
				wl[i].show();
			}
			add(i);
			if (debug > 2)
				show();

			if (listsize == 1)
				break;
		}
		if (listsize > 1)
		{
			diag(3,"Final wedge processing");
			last();
		}

		if (debug > 1)
			showshort();
		else if (debug > 2)
			show();

		// Validate list
		validateList();
	}

	// List Methods

	private void validateList()
	{
		WedgeNode w1;
		wedge1 = false;

		if (listsize > 1)
		{
			/* Check for increasing order of angles in list */
			w1 = first.succ;
			for (int i = 1; i < listsize; i++)
			{
				if ( !(w1.angle > (w1.pred).angle) )
				{
					System.out.println("*****Invalid angle order - " +
						" id1= " + (w1.pred).eid +
						" id2= " + w1.eid +
						" angle1= " + (w1.pred).angle +
						" angle2= " + w1.angle);
					throw erroneous_angle_order;
				}
			}

			/* Check for proper multiplicities */
			w1 = first;
			for (int i = 1; i < listsize; i++)
			{
				if ( w1.forwardMult == w1.backwardMult ||
					 w1.forwardMult  < 1 || w1.forwardMult  > 2 ||
					 w1.backwardMult < 1 || w1.backwardMult > 2 )
				{
					System.out.println("*****Invalid multiplicity - " +
						" id= " + w1.eid + " fm= " + w1.forwardMult + " bm= " + w1.backwardMult);
						throw erroneous_multiplicity;
				}

				if ( !(w1.forwardMult  == (w1.succ).backwardMult &&
					   w1.backwardMult == (w1.pred).forwardMult) )
				{
					System.out.println("*****Invalid multiplicity order - " +
						" id= " + w1.eid + " fm= " + w1.forwardMult + " bm= " + w1.backwardMult +
						" succ= " + (w1.succ).eid + " succ.bm= " + (w1.succ).backwardMult +
						" pred= " + (w1.pred).eid + " pred.fm= " + (w1.pred).forwardMult);
					throw erroneous_multiplicity_order;
				}

				if (w1.forwardMult == 1)	/* Set indicator for presence of mult. 1 wedge */
					wedge1 = true;
			}
		}
		else /* listsize == 1 */
		{
			w1 = first;
			if (!((w1.forwardMult == 1 && w1.backwardMult == 1) ||
				  (w1.forwardMult == 2 && w1.backwardMult == 2)) ||
				  first != last )
			{
				System.out.println("*****Invalid single entry - " +
						" id= " + w1.eid + " fm= " + w1.forwardMult + " bm= " + w1.backwardMult);
				throw erroneous_single_entry;
			}
		}
	}

	public int size()
	{
		return listsize;
	}

	public int getFirstMult()
	{
		return first.forwardMult;
	}

	public boolean wedge1()
	{
		return wedge1;
	}

	private void diag(int i, String s)
	{
		if (debug >= i)
			System.out.println(s);
	}

	private void incrForward()
	{
		if (current.forwardMult < 2)
			current.forwardMult++;
	}

	private void incrBackward()
	{
		if (current.backwardMult < 2)
			current.backwardMult++;
	}

	private void delete()
	{
		/* Can't delete if only one entry, the first entry, or the last edge */
		if (listsize > 1 && current != first && current.eid != wl.length-1)
		{
			(current.pred).succ = current.succ;
			(current.succ).pred = current.pred;

			if (current == last)
				last = current.pred;

			current = current.pred;
			listsize--;
		}
	}

	/*
	 * Check multiplicities, delete current, and move to succeesor
	 */
	private void checkDeleteSucc()
	{
		if ( listsize > 1 &&
			((current.forwardMult == 2 && current.backwardMult == 2) ||
			 (current.forwardMult == 1 && current.backwardMult == 1)) )
		{
			(current.pred).succ = current.succ;
			(current.succ).pred = current.pred;

			if (current == last)
				last = current.pred;

			else if (current == first)
				first = current.succ;

			listsize--;
			diag(3,"checkDeleteSucc for: " + current.eid);
		}
		current = current.succ;
	}

	/*
	 * Check multiplicities, delete current, and move to predecessor
	 */
	private void checkDeletePred()
	{
		if ( listsize > 1 &&
			((current.forwardMult == 2 && current.backwardMult == 2) ||
			 (current.forwardMult == 1 && current.backwardMult == 1)) )
		{
			(current.pred).succ = current.succ;
			(current.succ).pred = current.pred;

			if (current == last)
				last = current.pred;

			else if (current == first)
				first = current.succ;

			listsize--;
			diag(3,"checkDeletePred for: " + current.eid);
		}
		current = current.pred;
	}

	private void insertAfter(WedgeNode wn)
	{
		/* Always insert last edge entry, so we can process final wedge */
		if ( wn.eid == wl.length-1 ||
			!(( current.forwardMult == 2 && (current.succ).backwardMult == 2 ) ||
			  ( current.forwardMult == 1 && (current.succ).backwardMult == 1 ) ) )
		{
			if (current == last)
				last = wn;

			wn.pred = current;
			wn.succ = current.succ;
			(current.succ).pred = wn;
			current.succ = wn;
			current = wn;
			current.forwardMult = (current.succ).backwardMult;
			current.backwardMult = (current.pred).forwardMult;
			listsize++;

			if (wn.eid == wl.length-1)
				lastedge = current;

//			if ((current.pred).forwardMult == 1 && (current.pred).backwardMult == 1)
//			{
//				diag(3,"Insert delete: " + (current.pred).eid);
//				WedgeNode temp = current;
//				current = current.pred;
//				delete();
//				current = temp;
//			}
		}
		else
		{
			diag(3,"Not inserted: " + wn.eid);
			diag(3,"Current: " + current.eid);
		}
	}

	private void insertBefore(WedgeNode wn)
	{
		/* Always insert last edge entry, so we can process final wedge */
		if ( wn.eid == wl.length-1 ||
			!(((current.pred).forwardMult == 2 && current.backwardMult == 2 ) ||
			  ((current.pred).forwardMult == 1 && current.backwardMult == 1 ) ) )
		{
			if (current == first)
				first = wn;

			wn.pred = current.pred;
			wn.succ = current;
			(current.pred).succ = wn;
			current.pred = wn;
			current = wn;
			current.forwardMult  = (current.succ).backwardMult;
			current.backwardMult = (current.pred).forwardMult;
			listsize++;

			if (wn.eid == wl.length-1)
				lastedge = current;
		}
		else
		{
			diag(3,"Not inserted: " + wn.eid);
			diag(3,"Current: " + current.eid);
		}
	}

	/*
	 * Process last wedge -- last edge to edge 0
	 */
	private void last()
	{
		if ( listsize > 1 )
		{
			/* Scan from last edge to take care of final wedge */
			current = lastedge;
			if (current.angle > Math. PI)
			{	/* Go forward */
				incrForward();
				checkDeleteSucc();
				while (current != first)
				{
					incrForward();
					incrBackward();
					checkDeleteSucc();
				}
				incrBackward();
				checkDeleteSucc();
			}
			else
			{	/* Go backward */
				incrBackward();
				checkDeletePred();
				while (current != first)
				{
					incrForward();
					incrBackward();
					checkDeletePred();
				}
				incrForward();
				checkDeletePred();
			}
		}
	}

	public void show()
	{
		System.out.println("WedgeList");
		System.out.println("first=   " + first);
		System.out.println("last=    " + last);
		System.out.println("current= " + current);
		System.out.println("listsize=   " + listsize);
		WedgeNode wn = first;
		for (int i = 0; i < listsize; i++)
		{
			wn.show();
			wn = wn.succ;
		}
		System.out.println();
	}

	public void showshort()
	{
		System.out.println("WedgeList" + " - listsize=   " + listsize);
		WedgeNode wn = first;
		for (int i = 0; i < listsize; i++)
		{
			wn.showshort();
			wn = wn.succ;
		}
		System.out.println();
	}

	private boolean isFirst()
	{
		if (current == first)
			return true;
		else
			return false;
	}

	private boolean isLast()
	{
		if (current == last)
			return true;
		else
			return false;
	}

	private void next()
	{
		current = current.succ;
	}

	private void prev()
	{
		current = current.pred;
	}

	private void add(int i)
	{
		int count = 0;
		boolean start, beforefirst;

		if (i < 1)
			throw erroneous_state1;

		WedgeNode pn = wl[i-1];		/* Previous edge processed */
		WedgeNode wn = wl[i];						/* Current edge to be processed */

		if (pn.angle - wn.angle > Math.PI)
		{
			diag(3,"Special forwards.");
			beforefirst = true;
			start = true;
			while (count < wl.length)
			{
				count++;

				if (start)
				{
					diag(3,"Start: " + current.eid);
					start = false;
					incrForward();
				}
				else
				{
					diag(3,"Increment: " + current.eid);
					incrForward();
					incrBackward();
				}

				if (!isFirst() &&
					((current.forwardMult == 2 && current.backwardMult == 2) ||
					 (current.forwardMult == 1 && current.backwardMult == 1)) )
				{
					diag(3,"Delete: " + current.eid);
					delete();
					diag(3,"After delete: " + current.eid);
				}

				current = current.succ;
				diag(3,"Successor: " + current.eid);

				if (isFirst())
				{
					diag(3,"First: " + current.eid);
					beforefirst = false;
				}

				if ((!beforefirst && wn.angle < current.angle) || listsize == 1)
				{
					diag(3,"Break: " + current.eid);
					break;
				}

			}
			if (count >= wl.length)			/* Should never loop this many times */
				throw erroneous_state2;

			if (wn.angle < current.angle)
			{
				diag(3,"InsertBefore: " + wn.eid);
				insertBefore(wn);
			}
			else	/* Case of only first entry remaining */
			{
				diag(3,"InsertAfter: " + wn.eid);
				incrForward();
				incrBackward();
				insertAfter(wn);
			}
		}

		else if (wn.angle - pn.angle > Math.PI)
		{
			diag(3,"Special backwards.");
			beforefirst = true;
			start = true;
			while (count < wl.length)
			{
				count++;

				if (start)
				{
//					if (current.eid != pn.eid)
//					{
//						diag(3,"Move to other boundary:");
//						current = current.pred;
//					}
					diag(3,"Start: " + current.eid);
					start = false;
					incrBackward();
				}
				else
				{
					diag(3,"Increment: " + current.eid);
					incrForward();
					incrBackward();
				}

				if (isFirst())
				{
					diag(3,"First: " + current.eid);
					beforefirst = false;
				}

				if (!isFirst() &&
					((current.forwardMult == 2 && current.backwardMult == 2) ||
					 (current.forwardMult == 1 && current.backwardMult == 1)) )
				{
					diag(3,"Delete: " + current.eid);
					delete();
					diag(3,"After delete: " + current.eid);
				}
				else
				{
					current = current.pred;
					diag(3,"Predecessor: " + current.eid);
				}

				if ((!beforefirst && wn.angle > current.angle) || listsize == 1)
				{
					diag(3,"Break: " + current.eid);
					break;
				}
			}
			if (count >= wl.length)			/* Should never loop this many times */
				throw erroneous_state2;

			if (wn.angle < current.angle)
			{
				diag(3,"InsertBefore: " + wn.eid);
				incrForward();
				insertBefore(wn);
			}
			else
			{
				diag(3,"InsertAfter: " + wn.eid);
				insertAfter(wn);
			}
		}

		else if (wn.angle > current.angle)
		{
			diag(3,"Normal forwards.");
			start = true;
			while (count < wl.length)
			{
				count++;
				if (start)
				{
					diag(3,"Start: " + current.eid);
					start = false;
					incrForward();
				}
				else
				{
					diag(3,"Increment: " + current.eid);
					incrForward();
					incrBackward();
				}

				if (!isFirst() &&
					((current.forwardMult == 2 && current.backwardMult == 2) ||
					 (current.forwardMult == 1 && current.backwardMult == 1)) )
				{
					diag(3,"Delete: " + current.eid);
					delete();
					diag(3,"After delete: " + current.eid);
				}

				if ((isLast() || wn.angle < (current.succ).angle) || listsize == 1)
				{
					diag(3,"Break: " + current.eid);
					break;
				}
				else
				{
					current = current.succ;
					diag(3,"Successor: " + current.eid);
				}
			}

			if (count >= wl.length)			/* Should never loop this many times */
				throw erroneous_state2;

			diag(3,"InsertAfter: " + wn.eid);
			insertAfter(wn);
		}
		else
		{
			diag(3,"Normal backwards.");
			start = true;
			while (count < wl.length)
			{
				count++;
				if (start)
				{
					diag(3,"Start: " + current.eid);
					start = false;
					incrBackward();
				}
				else
				{
					diag(3,"Increment: " + current.eid);
					incrForward();
					incrBackward();
				}

				if (!isFirst() &&
					((current.forwardMult == 2 && current.backwardMult == 2) ||
					 (current.forwardMult == 1 && current.backwardMult == 1)) )
				{
					diag(3,"Delete: " + current.eid);
					delete();
					diag(3,"After delete: " + current.eid);
				}
				else
				{
					current = current.pred;
					diag(3,"Predecessor: " + current.eid);
				}

				if (isFirst() || wn.angle > current.angle || listsize == 1)
				{
					diag(3,"Break: " + current.eid);
					break;
				}
			}
			if (count >= wl.length)			/* Should never loop this many times */
				throw erroneous_state2;

			if (wn.angle > current.angle)	/* See test mono21 */
			{
				diag(3,"InsertAfter: " + wn.eid);
				insertAfter(wn);
			}
			else
			{
				diag(3,"!!!!!!InsertBefore: " + wn.eid);
				insertBefore(wn);
			}
		}
	}

	public boolean findMonotone()
	{
		WedgeNode w1, w2;

		/* Check for big (> PI) wedges */
		w1 = first;
		for (int i = 0; i < listsize; i++)
		{
			diag(3,"Wedge(" + w1.eid + ") size= " + wedgeSize(w1)*180.0/Math.PI);
			if (wedgeSize(w1) > Math.PI)
			{
				if (w1.forwardMult == 1)
				{
					diag(2,"Normal to line of monotonicity within wedge " + w1.eid);
					return true;
				}
				else
				{
					diag(2,"No normal to line of monotonicity, large mult. 2 wedge " + w1.eid);
					return false;
				}
			}
			w1 = w1.succ;
		}

		/* Check for antipodal wedges with mult. 1 */
		w1 = first;
		/* Find first wedge with mult. 1 */
		while (w1.forwardMult != 1)
			w1 = w1.succ;
		/* Look for matching antipodal wedge */
		w2 = w1;
		while (true)
		{
			do
			{
				w2 = w2.succ;
			} while (w2.angle - w1.angle < Math.PI && w2 != first);

			diag(3,"Check 1 - w1: " + w1.eid + ", w2.pred: " + (w2.pred).eid);
			if (w1.forwardMult == 1 && (w2.pred).forwardMult == 1)
			{
				diag(2,"Normal to line of monotonicity: " +
							w1.eid + " " + (w2.pred).eid + "\n");
				return true;
			}

			if (w2 == first)
			{
				diag(2,"No normal to line of monotonicity");
				return false;
			}

			do
			{
				w1 = w1.succ;
			} while (w2.angle - w1.angle >= Math.PI && w1 != first);

			diag(3,"Check 2 - w1.pred: " + (w1.pred).eid + ", w2: " + w2.eid);
			if ((w1.pred).forwardMult == 1 && w2.forwardMult == 1)
			{
				diag(2,"Normal to line of monotonicity: " +
							w1.pred.eid + " " + w2.eid + "\n");
				return true;
			}

			if (w1 == first)
			{
				diag(2,"No normal to line of monotonicity");
				return false;
			}
		}
	}

	private double wedgeSize(WedgeNode wn)
	{
		if (wn != last)
			return (wn.succ).angle - wn.angle;
		else
			return 2.0*Math.PI - wn.angle + (wn.succ).angle;
	}

	private int findMin(double[] a)
	{
		int mini = 0;
		double min = Double.MAX_VALUE;

		for (int i = 0; i < a.length; i++)
		{
			if (a[i] < min)
			{
				min = a[i];
				mini = i;
			}
		}

		return mini;
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
		if (pa.isMonotone())
			System.out.println("Polygon is monotone.");
		else
			System.out.println("Polygon is not monotone.");
		System.out.println();

		v[0] = new Pointd(0.1114864864864865,0.2477477477477478);
		v[1] = new Pointd(0.7481981981981983,0.2502252252252253);
		v[2] = new Pointd(0.6887387387387388,0.3864864864864865);
		v[3] = new Pointd(0.8720720720720722,0.7011261261261262);
		v[4] = new Pointd(0.7680180180180181,0.6887387387387388);
		v[5] = new Pointd(0.5500000000000000,0.7903153153153154);
		v[6] = new Pointd(0.4558558558558559,0.6788288288288289);
		v[7] = new Pointd(0.3394144144144144,0.6218468468468469);
		v[8] = new Pointd(0.2774774774774775,0.4608108108108109);

		pa = new PolygonA(v);
		if (pa.isMonotone())
			System.out.println("Polygon is monotone.");
		else
			System.out.println("Polygon is not monotone.");
		System.out.println();

		v[0] = new Pointd(0.1337837837837838,0.2477477477477478);
		v[1] = new Pointd(0.7506756756756757,0.2551801801801802);
		v[2] = new Pointd(0.7060810810810811,0.3939189189189190);
		v[3] = new Pointd(0.8918918918918920,0.6440540540540541);
		v[4] = new Pointd(0.7729729729729731,0.6540540540540541);
		v[5] = new Pointd(0.5549549549549551,0.7828828828828830);
		v[6] = new Pointd(0.4657657657657658,0.7135135135135136);
		v[7] = new Pointd(0.3443693693693694,0.6837837837837839);
		v[8] = new Pointd(0.3096846846846847,0.5029279279279280);

		pa = new PolygonA(v);
		if (pa.isMonotone())
			System.out.println("Polygon is monotone.");
		else
			System.out.println("Polygon is not monotone.");
	}
}