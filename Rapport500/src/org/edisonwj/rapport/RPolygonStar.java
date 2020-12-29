package org.edisonwj.rapport;


/**
 * RPolygonStar defines the class of star polygons
 */

import java.io.*;
import java.text.*;
import java.util.*;

class RPolygonStar extends PolygonA implements RapportDefaults
{
	protected psInfo ri;				/* Link to RInfo */

	public RPolygonStar(psInfo ri, Rand mr)	/* Generate star polygon */
	{
		super(ri.nv);
		this.ri = ri;
		
		if (!genPolygon(ri, mr))
		{
			v = null;
			nv = 0;
		}
		if (ri.color)
			setColor();
	}

	private boolean genPolygon(psInfo cg, Rand mr)	/* Generate polygon */
	{
		int n = cg.nv;

		/* Randomize n */
		if (cg.random && n > 10)
			n = mr.uniform(10,n);

		boolean angle_unif = cg.angle_unif;
		boolean angle_exp = cg.angle_exp;
		boolean angle_mmpp = cg.angle_mmpp;

		boolean radial_unif = cg.radial_unif;
		boolean radial_exp = cg.radial_exp;

		boolean angsum = cg.angsum;
		boolean bounded = cg.bounded;
		boolean smooth1 = cg.smooth1;
		boolean smooth2 = cg.smooth2;

   		double smooth_angle = Math.PI / cg.smooth_factor;
  		double radial_exp_rate = cg.radial_exp_rate;
		double density = cg.density;
		double markov_param = cg.markov_param;

  	  	double[] angle = new double[n + 1];
  		double[] radius = new double[n + 1];
  		double angle_sum = 0.0;
  		double beta = 0.0;

 	  	int i;

 	  	if (cg.points)
 	  	{
			/* Method from Knight and Epstein */
			for (i=0; i < n; i++)
				v[i] = new Pointd(mr.uniform(),mr.uniform());
			reSize(n);

			/* Find point inside triangle (center of gravity) formed by first three points */
			Pointd q = Geometry.centroid3(v[0],v[1],v[2]);

			/* Sort points radially about qc */
			sortAroundQ(v,q);
		}

		else if (cg.polar)
		{
			/* Generate n angular coordinates */

			if ( angle_unif || angle_exp )		/* uniform or exponential */
			{
				if (angsum)
				{
					angle[0] = 0.0;
					for (i=1; i<=n; i++)
						angle[i] = angle[i-1] + random_angle(mr, angle_unif);
					angle_sum = angle[n] + random_angle(mr, angle_unif);
					for (i=1; i<=n; i++)
						angle[i] = (angle[i] / angle_sum) * (2.0 * Math.PI);
				}
				else
				{
					angle[0] = 0.0;
					for (i=1; i<=n; i++)
						angle[i] = random_angle(mr, angle_unif, 0.0, 2.0*Math.PI);
					Arrays.sort(angle);
				}
			}

			else if ( angle_mmpp)				/* markov */
			{
				/* MAXCLUSTERS is the absolute maximum for MMPP */
				   long max_xdiv = (long)Math.ceil( Math.sqrt ( (double) MAXCLUSTERS_MMPP) );
				   long max_ydiv = (long)Math.ceil ( (double) MAXCLUSTERS_MMPP / max_xdiv);
				/* Note: the number of intervals is still random */
				   int xdivisions = (int)mr.uniform (1, max_xdiv);
				   int ydivisions = (int)mr.uniform (1, max_ydiv);
				   double alpha_A = 1.0;
				/* Randomize size of B-intervals */
				   double alpha_B = mr.uniform (0.5, 2.0);
				   double lambda_A = 1.0;
				/* Linearly pick a high lambda based on density specified */
					double lambda_B = 1.0 + (HIGHLAMBDA - 1.0) * (density - 1.0) / 9.0;
				/* */
					double upper_bound = 2 * Math.PI;

				/* Generate n angular coordinates */
					angle = mr.mmpp( n, xdivisions, alpha_A, alpha_B,
						lambda_A, lambda_B, upper_bound);
			}

			else
			{
				System.out.println("RPolygonCircle.genPoly: invalid angle distribution");
				return false;
			}


			/* Now make sure that no sector is larger than Math.PI */
			for (i=2; i<=n; i++)
			{
				if (angle[i] - angle[i-1] >= Math.PI)
				{
					System.out.println("RPolygonCircle.genPoly: sector larger than PI");
					return false;
				}
			}

			/* Generate n radial coordinates */

			NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMaximumFractionDigits(4);
			nf.setMinimumFractionDigits(4);
			nf.setMinimumIntegerDigits(1);

			radius[1] = random_radius(mr, radial_unif, bounded, radial_exp_rate);
			for (i=2; i<=n; i++)
			{
				if ( smooth1 )
				{
					beta = (angle[i] - angle[i-1]) / smooth_angle;
					if ( beta > 1.0) beta = 1.0;
					radius[i] = (1.0 - beta) * radius[i-1]
						+ beta * random_radius(mr, radial_unif, bounded, radial_exp_rate);
//					System.out.println(
//									  "angle[" + i + "]= " + nf.format(angle[i]*180.0/Math.PI)
//									+ ", angle[" + (i-1) + "]= " + nf.format(angle[i-1]*180.0/Math.PI)
//									+ ", angle[" + i + "]= " + nf.format(angle[i])
//									+ ", angle[" + (i-1) + "]= " + nf.format(angle[i-1])
//									+ "\nbeta= " + beta
//									+ "\nradius[" + i + "]= " + radius[i]
//									+ ", radius[" + (i-1) + "]= " + radius[i-1]);
				}
				else if ( smooth2 )
				{
					beta = markov_param;
					radius[i] = beta * radius[i-1]
							+ (1 - beta) * random_radius(mr, radial_unif, bounded, radial_exp_rate);
//					System.out.println("beta= " + beta
//									+ ", angle[" + i + "]= " + nf.format(angle[i]*180.0/Math.PI)
//									+ ", angle[" + (i-1) + "]= " + nf.format(angle[i-1]*180.0/Math.PI)
//									+ "\nradius[" + i + "]= " + radius[i]
//									+ ", radius[" + (i-1) + "]= " + radius[i-1]);
				}
				else
					radius[i] = random_radius(mr, radial_unif, bounded, radial_exp_rate);
			}

			if ( smooth1 )
			{
				beta = (2.0 * Math.PI + angle[1] - angle[n]) / smooth_angle;
				if ( beta > 1.0) beta = 1.0;
					radius[1] = (1.0 - beta) * radius[n]
						+ beta * random_radius(mr, radial_unif, bounded, radial_exp_rate);
			}
			else if ( smooth2 )
			{
				beta = markov_param;
				radius[1] = beta * radius[n]
						+ (1 - beta) * random_radius(mr, radial_unif, bounded, radial_exp_rate);
			}

			for (i = 2; i < .1*n; i++)
			{
				if ( smooth1)
				{
					beta = (angle[i] - angle[i-1]) / smooth_angle;
					if ( beta > 1.0) beta = 1.0;
						radius[i] = (1.0 - beta) * radius[i-1]
							+ beta * random_radius(mr, radial_unif, bounded, radial_exp_rate);
				}
				else if ( smooth2 )
				{
					beta = markov_param;
					radius[i] = beta * radius[i-1]
						+ (1 - beta) * random_radius(mr, radial_unif, bounded, radial_exp_rate);
				}
			}

			/* Finally, convert to Cartesian coordinates and assign to polygon */
			for (i = 1; i <= n; i++)
				v[i-1] = new Pointd(radius[i] * Math.cos(angle[i]), radius[i] * Math.sin(angle[i]));
			reSize(n);
			scale(0.5);
			translate(0.5, 0.5);
		}

		/* Check, scale, and move to first quadrant unit square */
 		if (checkPolygon())
 		{
  			computeMetrics();
  			return true;
		}
  		else
			return false;
	}

//	Utility methods

//  Radial sort

	private void sortAroundQ(Pointd[] u, Pointd qi)
	{
		final Pointd q = qi;
		Arrays.sort(u, new Comparator() {
			public int compare(Object a, Object b)
			{
				double a1, a2;

				a1 = computeAngle(q, (Pointd)a);
				a2 = computeAngle(q, (Pointd)b);
				if ( a1 > a2)
					return 1;
				else if (a1 < a2)
					return -1;
				else 					/* collinear  */
				{
					a1 = Geometry.distance(q, (Pointd)a);
					a2 = Geometry.distance(q, (Pointd)b);
					if ( a1 > a2 )
						return 1;
					else if ( a1 < a2 )
						return -1;
					else		// points are coincident
						return 0;
				}
			}
		});
	}

// Compute polar angle between a and b
	private double computeAngle(Pointd a, Pointd b)
	{
		double angle;
		double dx = b.getx() - a.getx();
		double dy = b.gety() - a.gety();
		double atan = Math.atan(dy/dx);
		if ( dx >= 0.0 && dy >= 0.0 )
			angle = atan;
		else if ( dx < 0.0 && dy >= 0.0 )
			angle =  Math.PI + atan;
		else if ( dx < 0.0 && dy < 0.0 )
			angle = Math.PI + atan;
		else
			angle = 2*Math.PI + atan;
		return angle;
	}

//  Create random angle for circle method using uniform or exponential dist.

	private double random_angle(Rand mr, boolean uniform)
	{
		if (uniform)					/* uniform angle distribution */
			return mr.uniform();
		else							/* exponential angle distribution */
			return mr.exponential(1.0);
	}

//	Create random angle for circle method using range uniform or exponential.

	private double random_angle(Rand mr, boolean uniform, double a, double b)
	{
		if (uniform)					/* uniform angle distribution */
			return mr.uniform(a, b);
		else							/* exponential angle distribution */
			return mr.exponential(a, b);
	}

//	Create random radius using uniform or bounded exponential.

	private double random_radius(Rand mr, boolean uniform, boolean bounded, double radial_exp_rate)
	{
		if (uniform)					/* uniform radial distribution */
			return mr.uniform();
		else							/* exponential radial distribution */
		{
			if (bounded)
				return mr.bounded_exponential(radial_exp_rate);
			else
				return mr.exponential(radial_exp_rate);
		}
	}
	
	public void writeData(boolean draw3d, PrintWriter out) throws IOException
	{
//		System.out.println("RPolygonStar writeData - type: " + type);
		
		String lineOut = "";
		String parmsOut = "";
		
		if (xcolor != null)
			parmsOut += "Color " + xcolor + " ";
		else
			parmsOut += "Color None ";
		
		if (ri != null) {		
			if (ri.polar)
				parmsOut += "Polar angle, ";
			if (ri.points)
				parmsOut += "Random points, ";

			if (ri.angle_unif)
				parmsOut += "Uniform angle, ";
			if (ri.angle_exp)
				parmsOut += "Exponential angle, ";
			if (ri.angle_mmpp)
				parmsOut += "Markov angle, ";

			if (ri.radial_unif)
				parmsOut += "Uniform radius, ";
			if (ri.radial_exp)
				parmsOut += "Exponential radius, ";
			
			if (ri.smooth1)
				parmsOut += "Smooth1 radius, ";
			if (ri.smooth2)
				parmsOut += "Smooth2 radius, ";
			if (ri.nosmooth)
				parmsOut += "No smooth, ";
			
			if (ri.angsum)
				parmsOut += "Angle sum, ";
			if (ri.bounded)
				parmsOut += "Bounded, ";
			if (ri.random)
				parmsOut += "Random number of vertices, ";	

			parmsOut += "Max.-number-of-vertices = " + ri.Snv;
		}
		else
			parmsOut += "Parameters unknown";
				
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
	
	public psInfo getRInfo() {
		return ri;
	}
}