package org.edisonwj.rapport;

/**
 * RCellSet defines the cell polymap structure
 */

import java.awt.*;
import java.io.*;
import java.util.*;

class RCellSet extends RObject implements RapportDefaults
{
	private int np;					/* Number of polygons */
	private int nc;					/* Number of cells in each row and col */
	private int method;				/* Distribution 1 = unif; 2 = exp; 3 = mixture; */
	private RCell[][] cell;			/* Cell matrix */
	private ArrayList[] active;		/* List of active cells for each polygon */
	private ArrayList[] inactive;	/* List of inactive cells for each polygon */

	private Rand mr;				/* Random number generator */
	private int tc;					/* total count */
	private Color[] pcolor;			/* Array of random colors for cell display */

	public RCellSet(pcInfo ri, Rand mr)		/* Cell polygon */
	{
//		System.out.println("RCellSet constructor, parms: pcInfo, Rand: " + this.hashCode() +
//							", pcInfo: " + ri.hashCode());
		this.mr = mr;
		this.np = ri.num_poly;
		this.nc = ri.num_cells;
		if (ri.uniform)
			this.method = 1;
		else if (ri.exponential)
			this.method = 2;
		else if (ri.mixed)
			this.method = 3;

//		Create matrix of cells
		cell = new RCell[nc][nc];

//		Initialize cells
		for (int i=0; i<nc; i++)
			for (int j=0; j<nc; j++)
				cell[i][j] = new RCell(i,j);

//		Set no-neighbor indicators for edge cells
		for (int i=0; i<nc; i++)
		{
//			0=below, 1=right, 2=above, 3=left
			cell[0][i].setNeighbors(0);			/* bottom row */
			cell[nc-1][i].setNeighbors(2);		/* top row */
			cell[i][0].setNeighbors(3);			/* left col */
			cell[i][nc-1].setNeighbors(1);		/* right col */
		}

//		Setup polygon arrays
//		cn = new int[np];
		active = new ArrayList[np];
		inactive = new ArrayList[np];
		for (int i=0; i<np; i++)
		{
			active[i] = new ArrayList();
			inactive[i] = new ArrayList();
//			cn[i] = 0;
		}

		this.genRCellSet(ri);

//		Generate random colors
//		seedColor = Math.abs((int)(new Date().getTime()));
//		Rand cr = new Rand(seedColor);
		Rand cr = new Rand(mr.getSeed());
		pcolor = new Color[np];

		for (int i = 0; i < np; i++)
			pcolor[i] = new Color(	cr.uniform(0, 255),
									cr.uniform(0, 255),
									cr.uniform(0, 255));
	}

	public RCellSet(cmInfo ri, Rand mr)		/* Cell PolyMap */
	{
//		System.out.println("RCellSet constructor, parms: cmInfo, Rand: " + this.hashCode() +
//							", cmInfo: " + ri.hashCode());
		this.mr = mr;
		this.np = ri.num_poly;
		this.nc = ri.num_cells;

		if (ri.uniform)
			this.method = 1;
		else if (ri.exponential)
			this.method = 2;
		else
			this.method = 3;

//		Create matrix of cells
		cell = new RCell[nc][nc];

//		Initialize cells
		for (int i=0; i<nc; i++)
			for (int j=0; j<nc; j++)
				cell[i][j] = new RCell(i,j);

//		Set no-neighbor indicators for edge cells
		for (int i=0; i<nc; i++)
		{
//			0=below, 1=right, 2=above, 3=left
			cell[0][i].setNeighbors(0);			/* bottom row */
			cell[nc-1][i].setNeighbors(2);		/* top row */
			cell[i][0].setNeighbors(3);			/* left col */
			cell[i][nc-1].setNeighbors(1);		/* right col */
		}

//		Setup polygon arrays
//		cn = new int[np];
		active = new ArrayList[np];
		inactive = new ArrayList[np];
		for (int i=0; i<np; i++)
		{
			active[i] = new ArrayList();
			inactive[i] = new ArrayList();
//			cn[i] = 0;
		}

		this.genRCellSet(ri);

//		Generate random colors
//		seedColor = Math.abs((int)(new Date().getTime()));
//		Rand cr = new Rand(seedColor);
		Rand cr = new Rand(mr.getSeed());
		pcolor = new Color[np];

		for (int i = 0; i < np; i++)
			pcolor[i] = new Color(	cr.uniform(0, 255),
									cr.uniform(0, 255),
									cr.uniform(0, 255));
	}

	public void genRCellSet(RInfo ri)
	{
//		System.out.println("RCellSet genRCellSet RInfo: " + ri.hashCode());
		int px;		/* polygon index */
		int cx;		/* cell index */
		int r;		/* row index */
		int c;		/* col index */
		int newr=0;	/* row index for new cell */
		int newc=0;	/* col index for new cell */
		int ps=0;		/* current polygon size */
		RCell curcell;	/* current cell */
		RCell addcell;	/* added cell */

//		Set polygon seeds
		tc = 0;
		int pcount = -1;
		while ( pcount < np-1 )
		{
			if ( np == 1)
			{
				r = (nc/2)-1;
				c = (nc/2)-1;
			}
			else
			{
				r = mr.uniform(0, nc-1);
				c = mr.uniform(0, nc-1);
			}
			curcell = cell[r][c];
			if (curcell.getPn() == -1)
			{
				pcount++;
				curcell.setPn(pcount);
				active[pcount].add(curcell);
//				cn[pcount] = 1;
				tc++;
				doNeighbors(cell[r][c]);
//				System.out.println("Seed for poly " + pcount + " " + cell[r][c]);
			}
		}

//		Grow polygons
		int total = nc*nc;
		if ( np == 1 )
			total = mr.uniform((int)(0.3*total), (int)(0.5*total));
//			total = mr.uniform((int)(0.3*total), (int)(0.7*total));
//		System.out.println("total: " + total);

		while (tc < total)
		{
			int newi;
//			Randomly select a polygon
			px = mr.uniform(0,np-1);		/* get polygon index */
			ps = active[px].size();			/* get polygon active cell count */
//			System.out.println("px= " + px + ", ps=" + ps);
			if ( ps == 0 )
				continue;

//			Randomly select a cell to grow
			cx = 0;
			if (ps-1 > 0)
			{
				switch (method)
				{
					case 1:	/* Uniform */
					cx = mr.uniform(0,ps-1);	/* get cell index */
					break;

					case 2: /* Exponential */
					cx = (int)mr.exponential((double)0,(double)(ps));	/* get cell index */
					break;

					case 3: /* Mixture */
					if (mr.uniform() > .5)
						cx = mr.uniform(0,ps-1);	/* get cell index */
					else
						cx = (int)mr.exponential((double)0,(double)(ps));	/* get cell index */
					break;
				}
//				System.out.println("RCellSet cx= " + cx + ", ps-1= " + (ps-1));
			}

//			System.out.println("RCellSet - px, ps, cx: " + px + " " + ps + " " +cx);

//			Get cell and its coordinates
			curcell = (RCell)active[px].get(cx);
			r = curcell.getRow();
			c = curcell.getCol();
//			System.out.println("RCellSet - curcell: " + curcell);

//			Randomly select cell growth
//			0=below, 1=right, 2=above, 3=left
			newi = curcell.select(mr,method);
//			System.out.println("RCellSet - newi: " + newi);

			switch (newi)
			{
				case 0: /* Add cell below */
					newr = r - 1;
					newc = c;
				break;

				case 1: /* Add cell to right */
					newr = r;
					newc = c + 1;
				break;

				case 2: /* Add cell above */
					newr = r + 1;
					newc = c;
				break;

				case 3: /* Add cell to left */
					newr = r;
					newc = c - 1;
				break;
			}
//			cn[px]++;
			tc++;

			addcell = cell[newr][newc];
			addcell.setPn(px);
			doNeighbors(addcell);

			if ( addcell.available() )
			{
//				System.out.println("addcell available result");
				active[px].add(addcell);
			}
			else
			{
//				System.out.println("addcell not available result");
				inactive[px].add(addcell);
			}
		}
		nv = tc;

//		System.out.println();
//		for (int i = 0; i < np; i++)
//		{
//			System.out.println("RCellSet Polygon: " + i
//								+ " " + active[i].size()
//								+ " " + inactive[i].size() );
//			for ( int j=0; j<active[i].size(); j++)
//				System.out.println("RCellSet - active[" + i + "](" + j + ")= "
//								+ (RCell)active[i].get(j));
//			for ( int j=0; j<inactive[i].size(); j++)
//				System.out.println("RCellSet - inactive[" + i + "](" + j + ")= "
//								+ (RCell)inactive[i].get(j));
//			System.out.println();
//		}

	}

	private void doNeighbors(RCell newc)
	{
//		System.out.println("doNeighbors: " + newc);
		RCell curc;
		int   curp;
		int r = newc.getRow();
		int c = newc.getCol();
		int p = newc.getPn();
		int wr;
		int wc;

//		Set neighbor indicators
//		0=below, 1=right, 2=above, 3=left

		if (r > 0)							/* Neighbor below */
		{
			wr = r-1;
			wc = c;
			curc = cell[wr][wc];
			curc.setNeighbors(2);
			curp = curc.getPn();
//			System.out.println("0 " + curc);
			if ( p == curp)
			{
				newc.setInterior(0);
				curc.setInterior(2);
			}
			if (!curc.available())			/* If no edges available and */
			{
				if (curp == -1)				/*   If not assigned, we have loop */
				{
					if (np == 1 && wr != 0 && wr != nc-1 && wc != 0 && wc != nc-1)
					{
	//					System.out.println("RCellSet Loop 0 : " + newc + " " + curc);
						curc.setPn(p);
						curc.setInterior(0);
						curc.setInterior(1);
						curc.setInterior(2);
						curc.setInterior(3);
						if (wr-1 >= 0)
							cell[wr-1][wc].setInterior(2);
						if (wr+1 < nc)
							cell[wr+1][wc].setInterior(0);
						if (wc-1 >= 0)
							cell[wr][wc-1].setInterior(1);
						if (wc+1 < nc)
							cell[wr][wc+1].setInterior(3);
						inactive[p].add(curc);
						tc++;
					}
				}
				else /* No edges available, but cell assigned - deactivate */
				{
//					System.out.println("curc inactive: " + curc);
//					for ( int i=0; i<active[curp].size(); i++)
//						System.out.println("RCellSet - active[" + curp + "](" + i + ")= "
//										+ (RCell)active[curp].get(i));
					active[curp].remove(active[curp].indexOf(curc));
					inactive[curp].add(curc);
				} /* End if assignment check */
			} /* End if no edges available */
		} /* End if neighbor below */

		if (r < nc-1)						/* Neighbor above */
		{
			wr = r+1;
			wc = c;
			curc = cell[wr][wc];
			curc.setNeighbors(0);
			curp = curc.getPn();
//			System.out.println("1 " + curc);
			if ( p == curp)
			{
				newc.setInterior(2);
				curc.setInterior(0);
			}
			if (!curc.available())		/* If no edges available and */
			{
				if (curp == -1)			/*   If not assigned, we have loop */
				{
					if (np == 1 && wr != 0 && wr != nc-1 && wc != 0 && wc != nc-1)
					{
//					System.out.println("RCellSet Loop 1 : " + newc + " " + curc);
					curc.setPn(p);
					curc.setInterior(0);
					curc.setInterior(1);
					curc.setInterior(2);
					curc.setInterior(3);
					if (wr-1 >= 0)
						cell[wr-1][wc].setInterior(2);
					if (wr+1 < nc)
						cell[wr+1][wc].setInterior(0);
					if (wc-1 >= 0)
						cell[wr][wc-1].setInterior(1);
					if (wc+1 < nc)
						cell[wr][wc+1].setInterior(3);
					inactive[p].add(curc);
					tc++;
					}
				}
				else /* No edges available, but cell assigned - deactivate */
				{
//					System.out.println("curc inactive: " + curc);
//					for ( int i=0; i<active[curp].size(); i++)
//						System.out.println("RCellSet - active[" + curp + "](" + i + ")= "
//										+ (RCell)active[curp].get(i));
					active[curp].remove(active[curp].indexOf(curc));
					inactive[curp].add(curc);
				} /* End if assignment check */
			} /* End if no edges available */
		} /* End if neighbor above */

		if (c > 0)							/* Neighbor left */
		{
			wr = r;
			wc = c-1;
			curc = cell[wr][wc];
			curc.setNeighbors(1);
			curp = curc.getPn();
//			System.out.println("2 " + curc);
			if ( p == curp)
			{
				newc.setInterior(3);
				curc.setInterior(1);
			}
			if (!curc.available())		/* If no edges available and */
			{
				if (curp == -1)			/*   If not assigned, we have loop */
				{
					if (np == 1 && wr != 0 && wr != nc-1 && wc != 0 && wc != nc-1)
					{
//					System.out.println("RCellSet Loop 2 : " + newc + " " + curc);
					curc.setPn(p);
					curc.setInterior(0);
					curc.setInterior(1);
					curc.setInterior(2);
					curc.setInterior(3);
					if (wr-1 >= 0)
						cell[wr-1][wc].setInterior(2);
					if (wr+1 < nc)
						cell[wr+1][wc].setInterior(0);
					if (wc-1 >= 0)
						cell[wr][wc-1].setInterior(1);
					if (wc+1 < nc)
						cell[wr][wc+1].setInterior(3);
					inactive[p].add(curc);
					tc++;
					}
				}
				else /* No edges available, but cell assigned - deactivate */
				{
//					System.out.println("curc inactive: " + curc);
//					for ( int i=0; i<active[curp].size(); i++)
//						System.out.println("RCellSet - active[" + curp + "](" + i + ")= "
//										+ (RCell)active[curp].get(i));
					active[curp].remove(active[curp].indexOf(curc));
					inactive[curp].add(curc);
				} /* End if assignment check */
			} /* End if no edges available */
		} /* End if neighbor left */

		if (c < nc-1)						/* Neighbor right */
		{
			wr = r;
			wc = c+1;
			curc = cell[wr][wc];
			curc.setNeighbors(3);
			curp = curc.getPn();
//			System.out.println("3 " + curc);
			if ( p == curp)
			{
				newc.setInterior(1);
				curc.setInterior(3);
			}
			if (!curc.available())		/* If no edges available and */
			{
				if (curp == -1)			/*   If not assigned, we have loop */
				{
					if (np == 1 && wr != 0 && wr != nc-1 && wc != 0 && wc != nc-1)
					{
//					System.out.println("RCellSet Loop 3 : " + newc + " " + curc);
					curc.setPn(p);
					curc.setInterior(0);
					curc.setInterior(1);
					curc.setInterior(2);
					curc.setInterior(3);
					if (wr-1 >= 0)
						cell[wr-1][wc].setInterior(2);
					if (wr+1 < nc)
						cell[wr+1][wc].setInterior(0);
					if (wc-1 >= 0)
						cell[wr][wc-1].setInterior(1);
					if (wc+1 < nc)
						cell[wr][wc+1].setInterior(3);
					inactive[p].add(curc);
					tc++;
					}
				}
				else /* No edges available, but cell assigned - deactivate */
				{
//					System.out.println("curc inactive: " + curc);
//					for ( int i=0; i<active[curp].size(); i++)
//						System.out.println("RCellSet - active[" + curp + "](" + i + ")= "
//										+ (RCell)active[curp].get(i));
					active[curp].remove(active[curp].indexOf(curc));
					inactive[curp].add(curc);
				}
			} /* End if assignment check */
		} /* End if no edges available */
	} /* End if neibhbor right */

	public void draw(Graphics g, RPanel rp)
	{
		System.out.println("RCellSet draw");
		int x, y, l;
		double hw;

		if ( nv > 0 )
		{
			Color saveC = g.getColor();

//			Compute cell size
			hw = 1.0/nc;
			l = rp.iL(hw);
			if (l <1 )
				l = 1;

//			Draw cells
			for (int i = 0; i < nc; i++)
			{
				for (int j = 0; j < nc; j++)
				{
					x = rp.iX(i*hw);
					y = rp.iY(j*hw + hw);
					g.setColor(pcolor[cell[j][i].getPn()]);
					g.fillRect(x, y, l, l);
//					System.out.println("draw: " + hw + " " + l +
//										" " + i + " " + j +
//										" " + i*hw + " " + j*hw +
//										" " + x + " " + y +
//										" " + cell[j][i].getPn());
				}
			}
			g.setColor(saveC);
		}
	}

	public ArrayList[] getActive()
	{
		return active;
	}

	public ArrayList[] getInactive()
	{
		return inactive;
	}

	public int getnp()
	{
		return np;
	}

	public int getnc()
	{
		return nc;
	}

	public void show()
	{
		System.out.println("\nActive List");
		for (int i = 0; i < active.length; i++)
			for (int j = 0; j < active[i].size(); j++)
				System.out.println("active[" + i + "](" + j + ")= " + ((RCell)active[i].get(j)).toString());

		System.out.println("\nInactive List");
		for (int i = 0; i < inactive.length; i++)
			for (int j = 0; j < inactive[i].size(); j++)
				System.out.println("inactive[" + i + "](" + j + ")= " + ((RCell)inactive[i].get(j)).toString());

		System.out.println();
	}

	public void setVertex(Pointd p, int i){}
	public Pointd getVertex(int i){return null;}
	public void add(Pointd p){}
	public void drawDecomp(Graphics g, RPanel rp){}
	public void drawHull(Graphics g, RPanel rp){}
	public void findHull(){}
	public Hull getHull(){return null;}
	public void reSize(){}
	public int size(){return np;}
	public int sizeDecomp(){return 0;}
	public int sizeHull(){return 0;}
	public void writeData(boolean draw3d, PrintWriter out) throws IOException{}
	public void readData(BufferedReader in) throws IOException{}
	public void scaleToUnitSquare(){}
	public void drawSpecial(Graphics g, RPanel rp){}
}
