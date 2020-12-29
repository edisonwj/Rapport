package org.edisonwj.rapport;

/**
 * RapportDefaults specifies default values for Rapport parameters. 
 */

import java.awt.*;

public interface RapportDefaults
{
// General Parameters
	public static final String VERSION = "5.00";
	public int NUM_ITEMS = 50;
	
// Data Save Parameters
	public boolean DRAW3D_FORMAT = true;
	public String DRAW3D_NUMBER_FORMAT = "%.8f";
	public int DEFAULT_MAX_FRACTION_DIGITS = 10;
	public int DEFAULT_MIN_FRACTION_DIGITS = 10;
	public int DEFAULT_MIN_INTEGER_DIGITS = 1;

// Debug level
	public static final int DEBUG = 1;

// Drawing Constants
	public Color DRAW_COLOR = Color.blue;
	public int DRAW_SCALE = 200;
	public int DRAW_PAUSE = 100;
	public int DRAW_HEIGHT = 600;
	public int DRAW_WIDTH= 600;

// Polygon Generation Constants
	public static final double UNIFORM_DIST = 1.0;
	public static final double EXPONENTIAL_DIST = 2.0;
	public static final double BOUNDED_EXP_DIST = 3.0;
// We will sometimes limit how large exponential values can be
//      by throwing out values larger than 10 times the mean
	public static final double BOUNDED_EXP_PARAM = 5.0;
	public static final double BOUNDED_NORMAL_PARAM = 10.0;
	public static final double SMOOTH_FACTOR = 16.0;	/* Divisor of Pi */
//	public static final double SMOOTH_ANGLE = 16.0;		/* Divisor of Pi */
	public static final double SMOOTH_DISTANCE = 0.05;
	public static final double DEFAULT_RATE = 50.0;
	public static final double DEFAULT_MEAN = 0.25;
	public static final double DEFAULT_VARIANCE = 0.20;
	public static final double RADIAL_EXP_RATE = 1.0;

	public static final boolean USE_EXP_SUM = false;
	public static final boolean USE_ANG_SUM = false;
	public static final boolean SMOOTH = true;
	
	public static final Color DEFAULT_POLYGON_COLOR = new Color(100,149, 237);
	public static final String DEFAULT_POLYGON_XCOLOR = "0x6495EDFF";
	

// Other Constants
//	public static final int	DEFAULTMRES = 8;			/* Metric resolution */
	public static final int	DEFAULTMRES = 10;			/* Metric resolution */
	public static final int MAX_ATTEMPTS = 100;
	public static final int NUM_CLUSTERS = 5;
//	public static final long   DEFAULTSEED = 123456789L;
	public static final long   DEFAULTSEED = Math.abs(System.currentTimeMillis());
//	public static final long   DEFAULTSEED = (long)(new Date().getTime());
	public static final double DENSITY = 1.5;
	public static final double MAXCLUSTERS = 100;		/* Max clusters used, if not specified */
	public static final double MAXCLUSTERS_MMPP = 400;	/* Same - for the Poisson case */
	public static final double MIDDEV = 0.1;			/* Std deviation used for middle-dense case */
	public static final double HIGHDEV = 0.5;			/* High deviation - low density */
	public static final double LOWDEV = 0.075;			/* Low deviation - high density */
	public static final double HIGHLAMBDA = 50.0;		/* Poisson rate of high interval in MMPP */
	public static final double MARKOV_PARAM = 0.5;
//	public static final double DEFAULTEPSILON = 0.0000000001;
	public static final double DEFAULTEPSILON = 0.0000000000001;
	public static final boolean EXACT_COMP = false;
	public static final int HASHGRAN = 10;				/* 2d Hash granularity */

//	Region Boundaries
	public static final double RXMIN = 0.0;
	public static final double RXMAX = 1.0;
	public static final double RYMIN = 0.0;
	public static final double RYMAX = 1.0;

// Rectangle Formats
	public static final int RC_CORNERS = 1;		/* low-left & upper-right corners */
	public static final int RC_CENTER = 2;		/* center & half side length extents */
	public static final int RC_CORNER_EXT = 3;	/* low-left corner and side length extents */

// Cell Constants
	public static final int NUM_NEIGHBORS = 4;		/* number of cell neighbors */
	public static final byte ALL_NEIGHBORS = 15;	/* mask with all 4 bits set */

	// Composites
	public static final int DEFAULT_COMPOSITE_SIZE = 16;
	
// Type Values
	public static final int TYPE_GROUP = 100;
	public static final int TYPE_POLYGON = 1;
	public static final int TYPE_POINT_SET = 2;
	public static final int TYPE_POLAR_POINT = 3;
	public static final int TYPE_SEGMENT_SET = 4;
	public static final int TYPE_POLYLINE = 5;
	public static final int TYPE_LINEFIELD = 6;
	public static final int TYPE_RANDOM_WALK = 7;
	public static final int TYPE_CIRCLE = 8;
	public static final int TYPE_ELLIPSE = 9;
	public static final int TYPE_POLYGONE = 12;
	public static final int TYPE_RECTANGLER2CE = 14;
	public static final int TYPE_RECTANGLER2CN = 15;
	public static final int TYPE_RECTANGLER2CR = 16;
	public static final int TYPE_POLYMAP = 20;
	public static final int TYPE_POLYGON_CELL = 11;
	public static final int TYPE_POLYGON_DRAW = 10;
	
// Title Values
	public static final String TITLE_POINT = "Point";
	public static final String TITLE_LINE = "Line";
	public static final String TITLE_POLYLINE = "PolyLine";
	public static final String TITLE_POISSON_LINE_FIELD = "Poisson-Line-Field";
	public static final String TITLE_RANDOM_WALK = "Random-Walk";
	public static final String TITLE_POLYGON_RANDOM_POINTS = "Polygon-Random-Points";
	public static final String TITLE_POLYGON_CONVEX_HULL = "Polygon-Convex-Hull";
	public static final String TITLE_POLYGON_CELL = "Polygon-Cell";
	public static final String TITLE_POLYGON_MONOTONE = "Polygon-Monotone";
	public static final String TITLE_POLYGON_STAR = "Polygon-Star";
	public static final String TITLE_POLYGON_SPIRAL = "Polygon-Spiral";
	public static final String TITLE_POLYGON_DRAW = "Polygon-Draw";
	public static final String TITLE_POLYGON_TEST = "Polygon-Test";
	public static final String TITLE_RECTANGLE = "Rectangle";
	public static final String TITLE_CIRCLE_ELLIPSE = "Circle-Ellipse";
	public static final String TITLE_POLYMAP_CELL = "PolyMap-Cell";
	public static final String TITLE_POLYMAP_POISSON = "PolyMap-Poisson";
	public static final String TITLE_POLYMAP_POLYLINE = "PolyMap-PolyLine";
	public static final String TITLE_POLYMAP_VORONOI = "PolyMap-Voronoi";
	
	
}