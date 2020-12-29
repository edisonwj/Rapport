package org.edisonwj.rapport;

import java.util.*;

/*
 * Generate a collection of convex polygons
 * using the convex hull method
 */

class RPolySetHull implements RapportDefaults
{
	private static final double epsilon = DEFAULTEPSILON;
	private static final boolean exact_comp = EXACT_COMP;

	// RInfo
	private phInfo ri;
	private int n;
	private Pointd[] randomPoints;	/* Random points */

	// Random number source
	private long seed;
	private Rand mr;
	private Rand mr2;

	// Result
	private Vector pv;				/* Polygon vector */

	public RPolySetHull(phInfo iri, Rand imr, Rand imr2)
	{
		ri = iri;
		mr = imr;
		mr2 = imr2;
	}

	public Vector genRPolySetHull()
	{
		pv = new Vector();

		// Generate a random set of  n points
		//	int n = mr.uniform(3, ri.nv);
		//	int n = (int)Math.ceil(mr.exponential(3.0, (double)ri.nv));
			int n = ri.nv;

			Pointd[] randomPoints = new Pointd[n];
			for ( int i=0; i < n; i++ )
				randomPoints[i] = new Pointd(mr.uniform(), mr.uniform());
		//		randomPoints[i] = new Pointd(mr.exponential(0.0,1.0), mr.uniform());

		// Create HashMap to track used polygon patterns
			HashMap hm = new HashMap();
			String key;
			int numv[] = new int[n+1];

			for ( int i = 0; i < ri.numb_objects; i++ )			/* Generate polygons */
			{
				for ( int j = 0; j < ri.max_attempts; j++ )	/* Make multiple tries */
				{
				// Use random circle to constrain points
				//*	double r = mr.uniform(.05, .95);
				//	double r = mr.unifomr(.05, .45);
				//	double r = mr.uniform(.05,.90);
				//	double r = mr.uniform(.1,.9);
				//	double r = mr.exponential(.05, .95);
					double r = mr.Normal(.50,.20);
					Pointd pc = new Pointd(mr.uniform(), mr.uniform());

				// Find the points in the circle
					int c, d;
					Pointd[] selectedPoints = new Pointd[n];
					for ( c = 0, d = 0; c < n; c++)
					{
						Pointd sp = randomPoints[c];
						if ( sp.inCircle(r, pc) )
							selectedPoints[d++] = sp;
					}

				// Must have at least 3 points
					if ( d < 3)
					{
//						System.out.println("Attempt " + i + ", " + j + " failed. Less than 3 points");
						continue;
					}

					Pointd[] finalPoints = new Pointd[d];
					System.arraycopy(selectedPoints, 0, finalPoints, 0, d);

					// Find convex hull
					Hull h = new Hull(finalPoints);

					// Save hull_points as polygon */
					PolygonA pa = new PolygonA(h.getPoints());

					// Check whether already used
					key = pa.toString();
//					System.out.println("Key: " + i + ", " + j + "\n" + key);
					if (hm.containsKey(key))
					{
//						System.out.println("Attempt " + i + ", " + j + " failed. Polygon already used:\n" + key);
						continue;
					}
					else
					{
						hm.put(key,null);
						numv[pa.size()]++;
						pv.add(pa);
						break;
					}

	//				if (pa.isGend())
	//				{
	//					if (j > 10)
	//						System.out.println("Add polygon: " + i + " , try: " + j);
	//					pv.add(pa);
	//					break;
	//				}
	//					else
	//						System.out.println("RGen phGen: attempt failed # " + i + " " + j);

				} /* End of try loop */
			} /* End of polygon generation loop */
			System.out.println("RGen phGen complete");
			for ( int i = 0; i < n; i++)
				System.out.println("numv[" + i + "]= " + numv[i]);
			return pv;
		}
	}