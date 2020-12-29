package org.edisonwj.rapport;

/** 
 * RHashtable defines a hash mapping struture utilized in
 * various line sweep operations. 
 */

import java.util.*;

public class RHashtable implements RapportDefaults
{
	double regionXmin = RXMIN;
	double regionXmax = RXMAX;
	double regionYmin = RYMIN;
	double regionYmax = RYMAX;

	int gran;
	double cellx;
	double celly;

	protected RapportException illegal_input = new RapportException("illegal input");

	RHashtableNode[][] ht;

	public RHashtable()
	{
		gran = 4;
		cellx = (regionXmax - regionXmin)/gran;
		celly = (regionYmax - regionYmin)/gran;

		ht = new RHashtableNode[gran+1][gran+1];
		for (int i = 0; i < gran+1; i++)
			for (int j = 0; j < gran+1; j++)
				ht[i][j] = new RHashtableNode(i, j);
	}

	public void addAll(RSegmentSet rs)
	{
		for (int i = 0; i < rs.size(); i++)
			add(new REdge((RSegment)rs.get(i)));
	}

	public void addAll(ArrayList al)
	{
		for (int i = 0; i < al.size(); i++)
			add((REdge)al.get(i));
	}

	public void add(REdge re)
	{
		ArrayList cl;
		CellListItem ci;

//		System.out.println("Add " + re);

		cl = findCells(re.getv1(), re.getv2());

//		System.out.println("found cells");

		for ( int k = 0; k < cl.size(); k++ )
		{
			ci = (CellListItem)cl.get(k);
			ht[ci.geti()][ci.getj()].add(re.getid());
		}
	}

	public void remove(REdge re)
	{
		ArrayList cl;
		CellListItem ci;

//		System.out.println("Remove " + re);

		cl = findCells(re.getv1(), re.getv2());
		for ( int k = 0; k < cl.size(); k++ )
		{
			ci = (CellListItem)cl.get(k);
			ht[ci.geti()][ci.getj()].remove(re.getid());
		}
	}

	/**
	 * Find the set of unique edges in cells intersected by segment
	 */
	public int[] findUnique(Pointd v1, Pointd v2)
	{
		ArrayList cl;
		CellListItem ci;
		ArrayList pel = new ArrayList();
		ArrayList tel = new ArrayList();
		int[] el = new int[0];

		cl = findCells(v1, v2);
		for (int i = 0; i < cl.size(); i++)
		{
			ci = (CellListItem)cl.get(i);
			pel.addAll(getel(ci.geti(),ci.getj()));
		}

		if ( pel.size() > 0 )
		{
			Collections.sort(pel);
			tel.add(pel.get(0));
			for (int i = 1; i < pel.size(); i++)
				if ( !pel.get(i).equals(pel.get(i-1)) )
					tel.add(pel.get(i));
			el = new int[tel.size()];
			for (int i = 0; i < el.length; i++)
				el[i] = ((Integer)tel.get(i)).intValue();
		}

		return el;
	}

	public ArrayList findCells(Pointd v1, Pointd v2)
	{
//		System.out.println("findCells v1= " + v1 + ", v2= " + v2);
//		System.out.println("adjust edge direction if necessary");
		if (!v1.vbefore(v2))
		{
			Pointd vtmp = v1;
			v1 = v2;
			v2 = vtmp;
		}
//		System.out.println("findCells v1= " + v1 + ", v2= " + v2);

		double v1x = v1.getx();
		double v1y = v1.gety();
		double v2x = v2.getx();
		double v2y = v2.gety();

		/* Verify data is in range */
		if (   v1x < regionXmin || v1x > regionXmax
			|| v2x < regionXmin || v2x > regionXmax
			|| v1y < regionYmin || v1y > regionYmax
			|| v2y < regionYmin || v2y > regionYmax )
			throw illegal_input;

		int frsti = (int)Math.floor((Math.abs(regionXmin) + v1x)/cellx);
		int frstj = (int)Math.floor((Math.abs(regionYmin) + v1y)/celly);
		int lasti = (int)Math.floor((Math.abs(regionXmin) + v2x)/cellx);
		int lastj = (int)Math.floor((Math.abs(regionYmin) + v2y)/celly);

//		System.out.println("regionXmin= " + regionXmin + " regionYmin= " + regionYmin
//						 + "\nregionXmax= " + regionXmax + " regionYmax= " + regionYmax
//						 + "\ncellx= " + cellx + " celly= " + celly
//						 + "\ngran= " + gran);

//		System.out.println("frsti:frstj= "
//					+ frsti + ":" + frstj
//					+ " lasti:lastj= "
//					+ lasti + ":" + lastj);

		int i = frsti, j = frstj;

		ArrayList cellList = new ArrayList();
		int state = 0;

		/* Check for horizontal perimeter segment */
		if (    v1y == v2y
			&& (v1y == regionYmin || v1y == regionYmax) )
		{
			if ( v1x <= v2x )
			{
//				System.out.println("Horizontal perimeter segment ==>");

				if ( j == gran )
					j--;

				if ( i > 0 && v1x == ht[i][j].lwlt.getx() )
					cellList.add(new CellListItem(i-1, j));

				do
				{
					cellList.add(new CellListItem(i, j));
					i++;
				} while ( i <= lasti && i < gran );
			}
			else
			{
//				System.out.println("Horizontal perimeter segment <==");

				if ( i == gran )
					i--;
				if ( j == gran )
					j--;

				do
				{
					cellList.add(new CellListItem(i, j));
					i--;
				} while ( i >= lasti );

				if ( i >= 0 && v2x == ht[i+1][j].lwlt.getx() )
				{
					cellList.add(new CellListItem(i, j));
				}
			}
		}

		/* Check for vertical perimeter segment */
		else if (   v1x == v2x
				&& (v1x == regionXmin || v1x == regionXmax) )
		{
			if ( v1y <= v2y )
			{
//				System.out.println("Vertical perimeter segment ^");

				if ( i == gran )
					i--;

				if ( j > 0 && v1y == ht[i][j].lwlt.gety() )
					cellList.add(new CellListItem(i, j-1));

				do
				{
					cellList.add(new CellListItem(i, j));
					j++;
				} while ( j <= lastj && j < gran );
			}
			else
			{
//				System.out.println("Vertical perimeter segment .");
				if ( i == gran )
					i--;
				if ( j == gran )
					j--;
				do
				{
					cellList.add(new CellListItem(i, j));
					j--;
				} while ( j >= lastj );

				if ( j >= 0 && v2y == ht[i][j+1].lwlt.gety() )
				{
					cellList.add(new CellListItem(i, j));
				}
			}
		}

		/* Check for horizontal non-perimeter gridline segment */
		else if (    v1y == v2y
			 	&& ( v1y == ht[i][j].lwlt.gety() ) )
		{
			if ( v1x <= v2x )
			{
//				if (DEBUG >= 3)
//					System.out.println("Horizontal line case ==>");

				if ( i > 0 && v1x == ht[i][j].lwlt.getx() )
				{
					cellList.add(new CellListItem(i-1, j));
					cellList.add(new CellListItem(i-1, j-1));
				}

				do
				{
					cellList.add(new CellListItem(i, j));
					cellList.add(new CellListItem(i, j-1));
					i++;
				} while ( i <= lasti && i < gran );
			}
			else
			{
//				if (DEBUG >= 3)
//					System.out.println("Horizontal line case <==");

				if ( i >= gran )
					i = gran - 1;

				do
				{
					cellList.add(new CellListItem(i, j));
					cellList.add(new CellListItem(i, j-1));
					i--;
				} while ( i >= lasti );

				if ( i >= 0 && v2x == ht[i+1][j].lwlt.getx() )
				{
					cellList.add(new CellListItem(i, j));
					cellList.add(new CellListItem(i, j-1));
				}
			}
		}

		/* Check for vertical non-perimeter gridline segment */
		else if (    v1x == v2x
				&& ( v1x == ht[i][j].lwlt.getx() ) )
		{
			if ( v1y <= v2y )
			{
//				if (DEBUG >= 3)
//					System.out.println("Vertical line case ^");

				if ( j > 0 && v1y == ht[i][j].lwlt.gety() )
				{
					cellList.add(new CellListItem(i, j-1));
					cellList.add(new CellListItem(i-1, j-1));
				}

				do
				{
					cellList.add(new CellListItem(i, j));
					cellList.add(new CellListItem(i-1,j));
					j++;
				} while ( j <= lastj && j < gran );
			}

			else
			{
//				if (DEBUG >= 3)
//					System.out.println("Vertical line case .");

				if ( j >= gran )
					j = gran - 1;

				do
				{
					cellList.add(new CellListItem(i, j));
					cellList.add(new CellListItem(i-1, j));
					j--;
				} while ( j >= lastj );

				if ( j >= 0 && v2y == ht[i][j+1].lwlt.gety() )
				{
					cellList.add(new CellListItem(i, j));
					cellList.add(new CellListItem(i-1, j));
				}
			}
		}

		else
		{
//			System.out.println("Non-horizontal and non-vertical");

			/* Check for start at a lower left cell corner */
			if ( ( i < gran && j < gran && v1.equals(ht[i][j].lwlt) )
				|| ( i == gran && v1y == ht[i-1][j].lwlt.gety() )
				|| ( j == gran && v1x == ht[i][j-1].lwlt.getx() ) )
			{
//				System.out.println("Lower left corner start");
				if ( i < gran && j < gran )
					cellList.add(new CellListItem(i, j));
				if ( i > 0 && j < gran )
					cellList.add(new CellListItem(i-1, j));
				if ( i < gran && j > 0 )
					cellList.add(new CellListItem(i, j-1));
				if ( i > 0 && j > 0 )
					cellList.add(new CellListItem(i-1, j-1));

				if      ( v2x < v1x && v2y < v1y )
				{
					state = 5;
					i -= 1;
					j -= 1;
//					System.out.println("i, j, state: " + i + " " + j + " " + state);
				}
				else if ( v2x > v1x && v2y < v1y )
				{
					state = 6;
					j -= 1;
//					System.out.println("i, j, state: " + i + " " + j + " " + state);
				}
				else if ( v2x > v1x && v2y > v1y )
				{
					state = 7;
//					System.out.println("i, j, state: " + i + " " + j + " " + state);
				}
				else if ( v2x < v1x && v2y > v1y )
				{
					state = 8;
					i -= 1;
//					System.out.println("i, j, state: " + i + " " + j + " " + state);
				}
			}

			/* Check for start on lower grid boundary */
			else if (  Geometry.between(ht[i][j].lwlt, ht[i][j].lwrt, v1)
					&& v2y < v1y)
			{
//				System.out.println("Lower edge start: i=" + i + ",j= " + j);
				j -=1;
				state = 1;
				cellList.add(new CellListItem(i, j));
//				System.out.println("add i, j, state: " + i + " " + j + " " + state);
			}

			else if ( i < gran && j < gran )
			{
//				System.out.println("Other start");
				cellList.add(new CellListItem(i, j));
			}

			while ( (i != lasti || j != lastj)
				&& !Geometry.between(ht[i][j].lwlt, ht[i][j].lwrt, v2)
				&& !Geometry.between(ht[i][j].lwrt, ht[i][j].hirt, v2)
				&& !Geometry.between(ht[i][j].hirt, ht[i][j].hilt, v2)
				&& !Geometry.between(ht[i][j].hilt, ht[i][j].lwlt, v2) )
			{
//				if (DEBUG >= 3)
//					System.out.println("i,j " + i + " " + j +
//							" " + ht[i][j].lwlt +
//							" " + ht[i][j].lwrt +
//							" " + ht[i][j].hirt +
//							" " + ht[i][j].hilt +
//							" " + state);

				if		(  state != 3
						&& Geometry.proper_intersect(v1, v2, ht[i][j].lwlt, ht[i][j].lwrt))
				{
					j -= 1;
					state = 1;
//					System.out.println("i, j, state: " + i + " " + j + " " + state);
				}

				else if (  state != 4
						&& Geometry.proper_intersect(v1, v2, ht[i][j].lwrt, ht[i][j].hirt))
				{
					i +=1;
					state = 2;
//					System.out.println("i, j, state: " + i + " " + j + " " + state);
				}

				else if (  state != 1
						&& Geometry.proper_intersect(v1, v2, ht[i][j].hirt, ht[i][j].hilt))
				{
					j +=1;
					state = 3;
//					System.out.println("i, j, state: " + i + " " + j + " " + state);
				}

				else if (  state != 2
						&& Geometry.proper_intersect(v1, v2, ht[i][j].hilt, ht[i][j].lwlt))
				{
					i -=1;
					state = 4;
//					System.out.println("i, j, state: " + i + " " + j + " " + state);
				}

				else if ( state != 7
						&& !v2.equals(ht[i][j].lwlt)
						&& Geometry.between(v1, v2, ht[i][j].lwlt) )
				{
					cellList.add(new CellListItem(i-1, j));
					cellList.add(new CellListItem(i, j-1));
					i -=1;
					j -=1;
					state = 5;
//					System.out.println("i, j, state: " + i + " " + j + " " + state);
				}

				else if ( state != 8
						&& !v2.equals(ht[i][j].lwrt)
						&& Geometry.between(v1, v2, ht[i][j].lwrt) )
				{
					cellList.add(new CellListItem(i+1, j));
					cellList.add(new CellListItem(i, j-1));
					i +=1;
					j -=1;
					state = 6;
//					System.out.println("i, j, state: " + i + " " + j + " " + state);
				}

				else if ( state != 5
						&& !v2.equals(ht[i][j].hirt)
						&& Geometry.between(v1, v2, ht[i][j].hirt) )
				{
					cellList.add(new CellListItem(i+1, j));
					cellList.add(new CellListItem(i, j+1));
					i +=1;
					j +=1;
					state = 7;
//					System.out.println("i, j, state: " + i + " " + j + " " + state);
				}

				else if ( state != 6
						&& !v2.equals(ht[i][j].hilt)
						&& Geometry.between(v1, v2, ht[i][j].hilt) )
				{
					cellList.add(new CellListItem(i, j+1));
					cellList.add(new CellListItem(i-1, j));
					i -=1;
					j +=1;
					state = 8;
//					System.out.println("i, j, state: " + i + " " + j + " " + state);
				}

				cellList.add(new CellListItem(i, j));

			} /* End of while loop */

			if ( Geometry.collinear(v2, ht[i][j].lwlt, ht[i][j].lwrt) )
			{
				if ( j > 0 )
					cellList.add(new CellListItem(i, j-1));

				if ( v2.equals(ht[i][j].lwlt) )
				{
					if ( i > 0 )
					{
						cellList.add(new CellListItem(i-1, j));
						if ( j > 0 )
							cellList.add(new CellListItem(i-1, j-1));
					}
				}
				else if ( v2.equals(ht[i][j].lwrt) )
				{
					if ( i < gran-1 )
					{
						cellList.add(new CellListItem(i+1, j));
						if ( j > 0 )
							cellList.add(new CellListItem(i+1, j-1));
					}
				}
				state = 9;
//				System.out.println("i, j, state: " + i + " " + j + " " + state);
			}

			else if ( Geometry.collinear(v2, ht[i][j].lwrt, ht[i][j].hirt) )
			{
				if ( i < gran-1 )
					cellList.add(new CellListItem(i+1, j));

				if ( v2.equals(ht[i][j].lwrt) )
				{
					if ( j > 0 )
					{
						cellList.add(new CellListItem(i, j-1));
						if ( i < gran-1 )
							cellList.add(new CellListItem(i+1, j-1));
					}
				}
				else if ( v2.equals(ht[i][j].hirt) )
				{
					if ( j < gran-1 )
					{
						cellList.add(new CellListItem(i, j+1));
						if ( i < gran-1 )
							cellList.add(new CellListItem(i+1, j+1));
					}
				}
				state = 10;
//				System.out.println("i, j, state: " + i + " " + j + " " + state);
			}

			else if ( Geometry.collinear(v2, ht[i][j].hirt, ht[i][j].hilt) )
			{
				if (j < gran-1)
					cellList.add(new CellListItem(i, j+1));

				if ( v2.equals(ht[i][j].hilt) )
				{
					if ( i > 0 )
					{
						cellList.add(new CellListItem(i-1, j));
						if ( j < gran-1 )
							cellList.add(new CellListItem(i-1, j+1));
					}
				}
				else if ( v2.equals(ht[i][j].hirt) )
				{
					if ( i < gran-1 )
					{
						cellList.add(new CellListItem(i+1, j));
						if ( j < gran-1 )
							cellList.add(new CellListItem(i+1, j+1));
					}
				}
				state = 11;
//				System.out.println("i, j, state: " + i + " " + j + " " + state);
			}

			else if ( Geometry.collinear(v2, ht[i][j].hilt, ht[i][j].lwlt) )
			{
				if ( i > 0 )
					cellList.add(new CellListItem(i-1, j));

				if ( v2.equals(ht[i][j].hilt) )
				{
					if ( j < gran-1 )
					{
						cellList.add(new CellListItem(i, j+1));
						if ( i > 0 )
							cellList.add(new CellListItem(i-1, j+1));
					}
				}
				else if ( v2.equals(ht[i][j].lwlt) )
				{
					if ( j > 0 )
					{
						cellList.add(new CellListItem(i, j-1));
						if ( i > 0 )
							cellList.add(new CellListItem(i-1, j-1));
					}
				}
				state = 12;
//				System.out.println("i, j, state: " + i + " " + j + " " + state);
			}
		}




//		if ( DEBUG >= 3 )
//			for (i = 0; i < cellList.size(); i++)
//				System.out.println("Segment: " + v1 + "->" + v2 + " cellList[" + i + "]: " + cellList.get(i));

		return cellList;
	}

	public String toString()
	{
		String s = "";
		for (int i = 0; i < gran; i++)
			for (int j = 0; j < gran; j++)
				s += "ht[" + i +"][" + j + "]= " + ht[i][j].toString() + "\n";
		return s;
	}

	public List getel(int i, int j)
	{
		return ht[i][j].getel();
	}

	class RHashtableNode
	{
		private List el;				/* HashEdgelist */
		public Pointd lwlt;				/* Lower left corner */
		public Pointd lwrt;				/* Lower right corner */
		public Pointd hirt;				/* Upper right corner */
		public Pointd hilt;				/* Upper left corner */

		public RHashtableNode(int i, int j)
		{
			el = new LinkedList();
			lwlt = new Pointd(regionXmin + i*cellx,		regionYmin + j*celly);
			lwrt = new Pointd(regionXmin + (i+1)*cellx,	regionYmin + j*celly);
			hirt = new Pointd(regionXmin + (i+1)*cellx,	regionYmin + (j+1)*celly);
			hilt = new Pointd(regionXmin + i*cellx, 	regionYmin + (j+1)*celly);
		}

		public void add(int id)
		{
			el.add((Object)new Integer(id));
		}

		public void remove(int id)
		{
			el.remove((Object)new Integer(id));
		}

		public List getel()
		{
			return el;
		}

		public String toString()
		{
			return el.toString();
		}
	}
}

class CellListItem
{
	int i;
	int j;

	CellListItem(int i, int j)
	{
		this.i = i;
		this.j = j;
	}

	public int geti()
	{
		return i;
	}

	public int getj()
	{
		return j;
	}

	public String toString()
	{
		return "" + i + ", " + j;
	}
}

