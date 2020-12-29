package org.edisonwj.rapport;

/**
 * Miscellaneous methods for geometry processing,
 * most based on or using code from:
 * Computational Geometry in C Second Edition, Joseph O'Rourke
 */

import java.util.*;

public class Geometry implements RapportDefaults
{
	private static final double epsilon = DEFAULTEPSILON;
	private static final boolean exact_comp = EXACT_COMP;

/************************************************************/
/* USEFUL GEOMETRY FUNCTIONS */
/************************************************************/

/* Return next vertex index in the circular sequence 0,...,n-1 */

public static int next (int i, int n)
{
  if (i < n-1) return i+1;
  else return 0;
}

/* Return previous index */

public static int prev (int i, int n)
{
  if (i==0) return n-1;
  else return i-1;
}

/* Compute twice the area of the triangle abc .
   Note: if abc are rational or integers, the result stays
   the same */

public static double twice_area (Pointd a, Pointd b, Pointd c)
{
  return  (a.x * b.y - a.y * b.x
          +  b.x * c.y - b.y * c.x
          +  c.x * a.y - c.y * a.x);
}

public static double area (Pointd a, Pointd b, Pointd c)
{
  return 0.5 * twice_area ( a, b, c);
}

/* Sometimes just the sign of the area is useful */

public static int area_sign (Pointd a, Pointd b, Pointd c)
{
  double area2;
  area2 = (a.x * b.y - a.y * b.x
          +  b.x * c.y - b.y * c.x
          +  c.x * a.y - c.y * a.x); /*twice_area (a, b, c);*/
  if (exact_comp)
  {
    if (area2 < 0) return -1;
    else if (area2 > 0) return 1;
    else return 0;
  }
  else
  {
    if (area2 < -epsilon) return -1;
    else if (area2 > epsilon) return 1;
    else return 0;
  }
}

/* Centroid of triangle is just an average of vertices */

public static Pointd centroid3( Pointd a, Pointd b, Pointd c)
{
	Pointd ct = new Pointd();
	ct.setx((a.x + b.x + c.x)/3.0);
	ct.sety((a.y + b.y + c.y)/3.0);
	return ct;
}

public static Pointd Centroid3( Pointd a, Pointd b, Pointd c)
{
	Pointd ct = new Pointd();
//	System.out.println("Point: " + ct);
//	ct.x = a.x + b.x + c.x;
//	ct.y = a.y + b.y + c.y;
	ct.setx(a.x + b.x + c.x);
	ct.sety(a.y + b.y + c.y);
	return ct;
}

/* Compute square of distance between two points */
public static double distance2(Pointd a, Pointd b)
{
	double dx = a.getx() - b.getx();
	double dy = a.gety() - b.gety();
	return dx * dx + dy * dy;
}

/* Compute distance between two points */
public static double distance(Pointd a, Pointd b)
{
	return Math.sqrt(distance2(a, b));
}

/* Is point c strictly to the left of line ab? */

public static boolean left (Pointd a, Pointd b, Pointd c)
{
  double Two_Area;
  Two_Area = (a.x * b.y - a.y * b.x
          +  b.x * c.y - b.y * c.x
          +  c.x * a.y - c.y * a.x);
//  System.out.println( "Two_Area = " + Two_Area +
//  					" a= " + a +
//  					" b=" + b +
//  					" c=" + c );
  if (exact_comp)
  {
  	if (Two_Area > 0) return true;
  	else return false;
  }
  else
  {
    if (Two_Area > epsilon) return true;
    else return false;
  }

}

/* Is point c to the left of, or on line ab? */

public static boolean lefton (Pointd a, Pointd b, Pointd c)
{
  double x;
  double Two_Area;
  Two_Area =  (a.x * b.y - a.y * b.x
          +  b.x * c.y - b.y * c.x
          +  c.x * a.y - c.y * a.x);

  if (exact_comp) {
    if (Two_Area >= 0) return true;
    else return false;
  }
  else {
    x = Two_Area;
    if ( (x > epsilon) || (Math.abs(x) <= epsilon) ) return true;
    else return false;
  }
}

/* Is point c strictly to the right of line ab? */

public static boolean right (Pointd a, Pointd b, Pointd c)
{
  if (! lefton (a,b,c) ) return true;
  else return false;
}

/* Is point c to the right of, or on line ab? */

public static boolean righton (Pointd a, Pointd b, Pointd c)
{
  if (! left (a,b,c) ) return true;
  else return false;
}

/* Is point c collinear with line ab? */

public static boolean collinear (Pointd a, Pointd b, Pointd c)
{
  double Two_Area;
  Two_Area =  (a.x * b.y - a.y * b.x
          +  b.x * c.y - b.y * c.x
          +  c.x * a.y - c.y * a.x);
  if (exact_comp) {
    if (Two_Area == 0) return true;
    else return false;
  }
  else {
    if (Math.abs(Two_Area) <= epsilon) return true;
    else return false;
  }
}

/* Does the the line segment ab properly intersect the
   line segment cd? That is, do they cut each other? */

public static boolean proper_intersect (Pointd a, Pointd b, Pointd c, Pointd d)
{
  /* If the intersection is proper then points c and d should
     lie on opposite sides of segment ab. Also, a and b should
     lie on opposite sides of segment cd. In other words, their
     areas with cd should have opposite sign */
  if ( (area_sign(a,b,c) * area_sign(a,b,d) < 0) &&
       (area_sign(c,d,a) * area_sign(c,d,b) < 0) )
    return true;
  else return false;
}

/* Is point c collinear with and between a and b? */

public static boolean between (Pointd a, Pointd b, Pointd c)
{
  boolean Col;
  double Two_Area;

  if (a.equals(b)) {
  	if (a.equals(c))
  		return true;
  	else
  		return false;
  }

  Two_Area =  (a.x * b.y - a.y * b.x
          +  b.x * c.y - b.y * c.x
          +  c.x * a.y - c.y * a.x);
  if (exact_comp) {
    if (Two_Area == 0) Col = true;
    else Col = false;
  }
  else {
    if (Math.abs(Two_Area) <= epsilon) Col = true;
    else Col = false;
  }
  if (!Col ) return false;
  /* Now that the points are collinear, check using coordinates.
     To be between, the x-value of c has to be between the x values
     of a and b. However, the three points can be on a vertical line
     with c being above a and b. In this case, we have to make sure
     c's y-value is between that of a and b */
  if (exact_comp) {
    if (a.x != b.x)  /* Not on a vertical line */
      return  (   ( (a.x <= c.x) && (c.x <= b.x) )
	       || ( (b.x <= c.x) && (c.x <= a.x) )  );
    else
      return  (   ( (a.y <= c.y) && (c.y <= b.y) )
	       || ( (b.y <= c.y) && (c.y <= a.y) )  );
  }
  else {
    if ( Math.abs(a.x-b.x) > epsilon)  /* Not on a vertical line */
      return  (   ( (a.x <= c.x) && (c.x <= b.x) )
	       || ( (b.x <= c.x) && (c.x <= a.x) )  );
    else
      return  (   ( (a.y <= c.y) && (c.y <= b.y) )
	       || ( (b.y <= c.y) && (c.y <= a.y) )  );
  }

}

/* To allow for improper intersections (such as when the
   the endpoint c of segment cd lies on the segment ab) we
   need to be more careful */

public static boolean intersect (Pointd a, Pointd b, Pointd c, Pointd d)
{
  boolean Pro_Inter;
/*  areas with cd should have opposite sign */
  if ( (area_sign(a,b,c) * area_sign(a,b,d) < 0) &&
       (area_sign(c,d,a) * area_sign(c,d,b) < 0) )
    Pro_Inter = true;
  else Pro_Inter = false;

  if ( Pro_Inter )
    return true;
  else if ( (between(a,b,c)) || (between(a,b,d)) ||
            (between(c,d,a)) || (between(c,d,b)) )
    return true;
  else
    return false;
}

/* Suppose segments ab and cd are to be edges of a simple polygon.
   If they are non-consecutive edges, they had better not intersect.
   If they are consecutive we want to allow a single point of
   intersection that must be either a or b. This functions checks
   out whether ab and cd are valid edges. The caller must specify
   whether they are successive (succ==1) or not (succ==0) */

public static boolean valid_edges (Pointd a, Pointd b, Pointd c, Pointd d, boolean succ)
{
  double Delta_X;
  double Delta_Y;
  boolean Inter;              /*intersect */
  boolean Pro_Inter;
  if (exact_comp) {
    if (succ) {
      /* Edges(a,b) and (c,d) are successive, i.e., b==c. Check a != d */
      if ( (a.x == d.x)  && (a.y == d.y) ) return false;
      else return true;
    }
    else {
      /* Not successive edges. Check for intersection */

      /*  areas with cd should have opposite sign */
      if ( (area_sign(a,b,c) * area_sign(a,b,d) < 0) &&
	   (area_sign(c,d,a) * area_sign(c,d,b) < 0) )
	Pro_Inter = true;
      else Pro_Inter = false;

      if ( Pro_Inter )
	Inter =true;
      else if ( (between(a,b,c)) || (between(a,b,d)) ||
            (between(c,d,a)) || (between(c,d,b)) )
	Inter = true;
      else
	Inter = false;
      return(!Inter );
    }
  }
  else {
    if (succ) {
      /* Edges(a,b) and (c,d) are successive, i.e., b==c. Check a != d */
      Delta_X = a.x - d.x;
      Delta_Y = a.y - d.y;
      if ( (Math.abs(Delta_X) <= epsilon) &&
           (Math.abs(Delta_Y) <= epsilon) ) return false;
      else return true;
    }
    else {
      /* Not successive edges. Check for intersection */

      /*  areas with cd should have opposite sign */
      if ( (area_sign(a,b,c) * area_sign(a,b,d) < 0) &&
	   (area_sign(c,d,a) * area_sign(c,d,b) < 0) )
	Pro_Inter = true;
      else Pro_Inter = false;

      if ( Pro_Inter )
	Inter = true;
      else if ( (between(a,b,c)) || (between(a,b,d)) ||
		(between(c,d,a)) || (between(c,d,b)) )
	Inter = true;
      else
	Inter = false;
      return(!Inter );
    }
  }
}

public static boolean valid_edges (Pointd a, Pointd b, Pointd c, Pointd d)
{
	double Delta_X;
	double Delta_Y;

	if (exact_comp)
	{
		/* if edges(a,b) and (c,d) are successive, i.e. b==c, check a != d */
		/* or edges(c,d) and (a,b) are successive, i.e. d==a, check c != b */

		if ( (b.x == c.x) && (b.y == c.y) )
		{
			if ( (a.x == d.x) && (a.y == d.y) )
				return false;
	   	else
				return true;
		}

		if ( (d.x == a.x) && (d.y == a.y) )
		{
			if ( (c.x == b.x) && (c.y == b.y) )
				return false;
	   	else
				return true;
    	}

      /* Not successive edges. Check for intersection */
		if ( intersect(a, b, c, d) )
			return false;
		else
			return true;
   }
	else
	{
		/* if edges(a,b) and (c,d) are successive, i.e. b==c, check a != d */
		/* or edges(c,d) and (a,b) are successive, i.e. d==a, check c != b */

		Delta_X = b.x - c.x;
		Delta_Y = b.y - c.y;
		if ( (Math.abs(Delta_X) <= epsilon) && (Math.abs(Delta_Y) <= epsilon) )
		{
      	Delta_X = a.x - d.x;
      	Delta_Y = a.y - d.y;
      	if ( (Math.abs(Delta_X) <= epsilon) && (Math.abs(Delta_Y) <= epsilon) )
				return false;
			else
				return true;
		}

		Delta_X = d.x - a.x;
		Delta_Y = d.y - a.y;
		if ( (Math.abs(Delta_X) <= epsilon) && (Math.abs(Delta_Y) <= epsilon) )
		{
      	Delta_X = c.x - b.x;
      	Delta_Y = c.y - b.y;
      	if ( (Math.abs(Delta_X) <= epsilon) && (Math.abs(Delta_Y) <= epsilon) )
				return false;
			else
				return true;
		}

      /* Not successive edges. Check for intersection */
		if ( intersect(a, b, c, d) )
			return false;
		else
			return true;
	}
}

public static boolean valid_edges(EdgeR e1, EdgeR e2)
{
	Pointd a, b, c, d;

//	if (DEBUG >= 3)
//		System.out.println("Checking valid_edges:\n"
//									+ "e1= " + e1 + "\n"
//									+ "e2= " + e2);
	a = e1.getv1();
	b = e1.getv2();
	c = e2.getv1();
	d = e2.getv2();
	return valid_edges(a, b, c, d);
}

/* Given two lines each specified by two points (a,b) and (c,d),
   compute the intersection point if the slopes of the two lines are
   not identical. In this case return true, else if the slopes are
   identical, return false. The Caller must create the space for
   the intersection point *e.  */

public static boolean intersection_point(Pointd a, Pointd b, Pointd c, Pointd d, Pointd e)
{
  double m1, m2, c1, c2, x, y;

  if (exact_comp) {

    if (a.x == b.x)  {  /* If the first line is vertical */
      if (c.x == d.x)  /* Same slope - return false */
	return false;
      /* Otherwise, compute intersection point */
      m2 =(c.y - d.y) /(c.x - d.x);  /* Slope of line cd  */
      c2 = c.y - m2 * c.x;             /* Y-intercept of cd */
      e.x = a.x;   e.y = m2 * a.x + c2;
      return true;
    }
    else if (c.x == d.x) {  /* The second line is vertical */
      if (a.x == b.x)  /* Same slope - return false */
	return false;
      /* Otherwise, compute intersection point */
      m1 =(a.y - b.y) /(a.x - b.x);  /* Slope of line ab  */
      c1 = a.y - m1 * a.x;             /* Y-intercept of ab */
      e.x = c.x;  e.y = m1 * c.x + c1;
      return true;
    }
    /* Now both lines have non-infinite slopes(possibly zero).
       Compute slopes of both lines. */
    m1 =(a.y - b.y) /(a.x - b.x);
    m2 =(c.y - d.y) /(c.x - d.x);
    /* Compute both Y-intercepts */
    c1 = a.y - m1 * a.x;
    c2 = c.y - m2 * c.x;

    if (m1 == m2) return false;

    /* Compute point of intersection */
    e.x =(c2 - c1) /(m1 - m2);
    e.y = m1 * e.x + c1;
    return true;

  }
  else {  /* Inexact comparison */

    if (Math.abs(a.x-b.x) <= epsilon)  {  /* If the first line is vertical */
      if (Math.abs(c.x-d.x) <= epsilon)  /* Same slope - return false */
	return false;
      /* Otherwise, compute intersection point */
      m2 =(c.y - d.y) /(c.x - d.x);  /* Slope of line cd  */
      c2 = c.y - m2 * c.x;             /* Y-intercept of cd */
      e.x = a.x;   e.y = m2 * a.x + c2;
      return true;
    }
    else if (Math.abs(c.x-d.x) <= epsilon) {  /* The second line is vertical */
      if (Math.abs(a.x-b.x) <= epsilon)  /* Same slope - return false */
	return false;
      /* Otherwise, compute intersection point */
      m1 =(a.y - b.y) /(a.x - b.x);  /* Slope of line ab  */
      c1 = a.y - m1 * a.x;             /* Y-intercept of ab */
      e.x = c.x;  e.y = m1 * c.x + c1;
      return true;
    }
    /* Now both lines have non-infinite slopes(possibly zero).
       Compute slopes of both lines. */
    m1 =(a.y - b.y) /(a.x - b.x);
    m2 =(c.y - d.y) /(c.x - d.x);
    /* Compute both Y-intercepts */
    c1 = a.y - m1 * a.x;
    c2 = c.y - m2 * c.x;

    if (Math.abs(m1-m2) <= epsilon) return false;

    /* Compute point of intersection */
    e.x =(c2 - c1) /(m1 - m2);
    e.y = m1 * e.x + c1;
    return true;
  }
}
/*
This code is described in "Computational Geometry in C" (Second Edition),
Chapter 7.  It is not written to be comprehensible without the
explanation in that book.

Compile:  gcc -o segseg segseg.c

Written by Joseph O'Rourke.
Last modified: November 1997
Questions to orourke@cs.smith.edu.
--------------------------------------------------------------------
This code is Copyright 1998 by Joseph O'Rourke.  It may be freely
redistributed in its entirety provided that this copyright notice is
not removed.
--------------------------------------------------------------------*/

/*---------------------------------------------------------------------
SegSegInt: Finds the point of intersection p between two closed
segments ab and cd.  Returns p and a char with the following meaning:
   'e': The segments collinearly overlap, sharing a point.
   'v': An endpoint (vertex) of one segment is on the other segment,
        but 'e' doesn't hold.
   '1': The segments intersect properly (i.e., they share a point and
        neither 'v' nor 'e' holds).
   '0': The segments do not intersect (i.e., they share no points).
Note that two collinear segments that share just one point, an endpoint
of each, returns 'e' rather than 'v' as one might expect.
---------------------------------------------------------------------*/
public static intersect SegSegInt( Pointd a, Pointd b, Pointd c, Pointd d )
{
   double  s, t;       /* The two parameters of the parametric eqns. */
   double num, denom;  /* Numerator and denoninator of equations. */
   double px, py;	   /* Coordinates of intersection point. */
   char code = '?';    /* Return char characterizing intersection. */

   denom = a.getx() * (double)( d.gety() - c.gety() ) +
           b.getx() * (double)( c.gety() - d.gety() ) +
           d.getx() * (double)( b.gety() - a.gety() ) +
           c.getx() * (double)( a.gety() - b.gety() );

   /* If denom is zero, then segments are parallel: handle separately. */
   if (denom == 0.0)
      return  ParallelInt(a, b, c, d);

   num =    a.getx() * (double)( d.gety() - c.gety() ) +
            c.getx() * (double)( a.gety() - d.gety() ) +
            d.getx() * (double)( c.gety() - a.gety() );
   if ( (num == 0.0) || (num == denom) )
   {
	   code = 'v';
//	   System.out.println("SegSegInt endpoint overlap 1");
   }
   s = num / denom;
// System.out.println("SegSegInt num= " + num + ", denom= " + denom + ", s= " + s);

   num = -( a.getx() * (double)( c.gety() - b.gety() ) +
            b.getx() * (double)( a.gety() - c.gety() ) +
            c.getx() * (double)( b.gety() - a.gety() ) );
   if ( (num == 0.0) || (num == denom) )
   {
	   code = 'v';
//	   System.out.println("SegSegInt endpoint overlap 2");
   }
   t = num / denom;
// System.out.println("SegSegInt num= " + num + ", denom= " + denom + ", t= " + t);;

   if      ( (0.0 < s) && (s < 1.0) &&
             (0.0 < t) && (t < 1.0) )
   {
		code = '1';
//		System.out.println("SegSegInt proper intersection s:t " + s + ":" + t);
   }
   else if ( (0.0 > s) || (s > 1.0) ||
             (0.0 > t) || (t > 1.0) )
   {
		code = '0';
//		System.out.println("SegSegInt no intersection s:t " + s + ":" + t);
   }

   px = a.getx() + s * ( b.getx() - a.getx() );
   py = a.gety() + s * ( b.gety() - a.gety() );

   return (new intersect(new Pointd(px, py), code) );
}


public static intersect ParallelInt( Pointd a, Pointd b, Pointd c, Pointd d )
{
	Pointd p;
	char code;

   if ( !collinear( a, b, c) ) {
	  p = null;
      code = '0';
   }
   else if ( between( a, b, c ) ) {
      p = c;
      code = 'e';
   }
   else if ( between( a, b, d ) ) {
      p = d;
      code = 'e';
   }
   else if ( between( c, d, a ) ) {
      p = a;
      code = 'e';
   }
   else if ( between( c, d, b ) ) {
      p = b;
      code = 'e';
   }
   else {
	  p = null;
      code = '0';
   }

  return (new intersect(p, code) );
}

/*
This code is described in "Computational Geometry in C" (Second Edition),
Chapter 7.  It is not written to be comprehensible without the
explanation in that book.

Compile:  gcc -o segseg segseg.c

Written by Joseph O'Rourke.
Last modified: November 1997
Questions to orourke@cs.smith.edu.
--------------------------------------------------------------------
This code is Copyright 1998 by Joseph O'Rourke.  It may be freely
redistributed in its entirety provided that this copyright notice is
not removed.
--------------------------------------------------------------------*/

/*---------------------------------------------------------------------
SegSegInt: Finds the point of intersection p between two closed
segments ab and cd.  Returns p and a char with the following meaning:
   '10+': The segments collinearly overlap, sharing a point.
   '2': An endpoint (vertex) of one segment is on the other segment,
        but 'e' doesn't hold.
   '1': The segments intersect properly (i.e., they share a point and
        neither 'v' nor 'e' holds).
   '0': The segments do not intersect (i.e., they share no points).
Note that two collinear segments that share just one point, an endpoint
of each, returns '10+' rather than '2' as one might expect.
---------------------------------------------------------------------*/
public static IntersectNew SegSegIntNew( Pointd a, Pointd b, Pointd c, Pointd d )
{
//	System.out.println("SegSegIntNew a= " + a + ", b= " + b + ", c= " + c + ", d= " + d);
	double  s, t;       /* The two parameters of the parametric eqns. */
	double num, denom;  /* Numerator and denoninator of equations. */
	double px, py;	   /* Coordinates of intersection point. */
	int code = 0;		/* Return code characterizing intersection. */

	denom = a.getx() * (double)( d.gety() - c.gety() ) +
	        b.getx() * (double)( c.gety() - d.gety() ) +
	        d.getx() * (double)( b.gety() - a.gety() ) +
	        c.getx() * (double)( a.gety() - b.gety() );
//	System.out.println("denom= " + denom);

	/* If denom is zero, then segments are parallel: handle separately. */
	if (denom == 0.0)
		return  ParallelIntNew(a, b, c, d);

	num =    a.getx() * (double)( d.gety() - c.gety() ) +
	         c.getx() * (double)( a.gety() - d.gety() ) +
	         d.getx() * (double)( c.gety() - a.gety() );
//	System.out.println("num= " + num);

	if ( (num == 0.0) || (num == denom) )
	{
		code = 2;
//		System.out.println("SegSegInt endpoint overlap 1");
	}
	s = num / denom;
//	System.out.println("SegSegInt num= " + num + ", denom= " + denom + ", s= " + s);

	num = -( a.getx() * (double)( c.gety() - b.gety() ) +
	         b.getx() * (double)( a.gety() - c.gety() ) +
	         c.getx() * (double)( b.gety() - a.gety() ) );
//	System.out.println("num= " + num);

	if ( (num == 0.0) || (num == denom) )
	{
		code = 2;
//		System.out.println("SegSegInt endpoint overlap 2");
	}
	t = num / denom;
//	System.out.println( "SegSegInt num= " + num + ", denom= " + denom +
//						", s= " + s + ". t= " + t);

	if (    (0.0 < s) && (s < 1.0) &&
			(0.0 < t) && (t < 1.0) )
	{
		code = 1;
//		System.out.println("SegSegInt proper intersection s:t " + s + " : " + t);
	}
	else if ( (0.0 > s) || (s > 1.0) ||
			  (0.0 > t) || (t > 1.0) )
	{
		code = 0;
//		System.out.println("SegSegInt no intersection s:t " + s + " : " + t);
	}

	px = a.getx() + s * ( b.getx() - a.getx() );
	py = a.gety() + s * ( b.gety() - a.gety() );
	Pointd p = new Pointd(px, py);
	Pointd q = null;

	return (new IntersectNew(p, q, code));
}

public static IntersectNew ParallelIntNew( Pointd a, Pointd b, Pointd c, Pointd d )
{
	Pointd p;
	Pointd q;
	int code;

	if ( !collinear( a, b, c) ) {
		p = null;
		q = null;
		code = 0;
	}
	else if ( between( a, b, c ) && between ( a, b, d ) ) {
		p = c;
		q = d;
		code = 10;
	}
	else if ( between( c, d, a ) && between ( c, d, b ) ) {
		p = a;
		q = b;
		code = 11;
	}
	else if ( between( a, b, c ) && between ( c, d, b ) ) {
		p = c;
		q = b;
		code = 12;
	}
	else if ( between( a, b, c ) && between ( c, d, a ) ) {
		p = c;
		q = a;
		code = 13;
	}
	else if ( between( a, b, d ) && between ( c, d, b )) {
		p = d;
		q = b;
		code = 14;
	}
	else if ( between( a, b, d ) && between ( c, d, a )) {
		p = d;
		q = a;
		code = 15;
	}
	else {
		p = null;
		q = null;
		code = 16;
	}
	return (new IntersectNew(p, q, code) );
}

/*---------------------------------------------------------------------
RaySegInt: Finds the point of intersection p between ray with origin a
and containing b and a closed segment cd. Returns an object containing
p and an int with the following meaning:
   '10+': The ray and segment collinearly overlap sharing points.
   '2': An endpoint (vertex) of the segment is on the ray, but not collinear.
   '1': The ray and segment intersect properly (i.e., they share a point and
        neither of the above are the case).
   '0': The ray and segment do not intersect (i.e., they share no points).
Note that a ray and collinear segment that share just one point, an endpoint
of each, returns '10+' rather than '2' as one might expect.
---------------------------------------------------------------------*/
public static IntersectNew RaySegIntNew( Pointd a, Pointd b, Pointd c, Pointd d )
{
//	System.out.println("RaySegIntNew a= " + a + ", b= " + b + ", c= " + c + ", d= " + d);
	double s = 0.0;		/* The two parameters of the parametric eqns. */
	double t = 0.0;
	double num, denom;  /* Numerator and denoninator of equations. */
	double px, py;	    /* Coordinates of intersection point. */
	int code = 0;		/* Return code characterizing intersection. */

	denom = a.getx() * (double)( d.gety() - c.gety() ) +
	        b.getx() * (double)( c.gety() - d.gety() ) +
	        d.getx() * (double)( b.gety() - a.gety() ) +
	        c.getx() * (double)( a.gety() - b.gety() );
//	System.out.println("denom= " + denom);

	/* If denom is zero, then segments are parallel: handle separately. */
	if (denom == 0.0)
	{
//		System.out.println("RaySegIntNew: parallel ray and segment");
		return  ParallelIntNew(a, b, c, d);
	}

	num =    a.getx() * (double)( d.gety() - c.gety() ) +
	         c.getx() * (double)( a.gety() - d.gety() ) +
	         d.getx() * (double)( c.gety() - a.gety() );
//	System.out.println("num= " + num);
	if ( (num == 0.0) || (num == denom) )
	{
		code = 2;
//		System.out.println("RaySegInt endpoint overlap 1");
	}
	s = num / denom;
//	System.out.println("RaySegInt num= " + num + ", denom= " + denom + ", s= " + s);

	num = -( a.getx() * (double)( c.gety() - b.gety() ) +
			 b.getx() * (double)( a.gety() - c.gety() ) +
			 c.getx() * (double)( b.gety() - a.gety() ) );
//	System.out.println("num= " + num);
	if ( (num == 0.0) || (num == denom) )
	{
		code = 2;
//		System.out.println("RaySegInt endpoint overlap 2");
	}
	t = num / denom;
//	System.out.println( "RaySegInt num= " + num + ", denom= " + denom + ". t= " + t);

	if ( s > 0.0 &&
		 t > 0.0 && t < 1.0 )
	{
		code = 1;
//		System.out.println("RaySegInt proper intersection s:t " + s + " : " + t);
	}

	px = a.getx() + s * ( b.getx() - a.getx() );
	py = a.gety() + s * ( b.gety() - a.gety() );
	Pointd p = new Pointd(px, py);
	Pointd q = null;

	return (new IntersectNew(p, q, code));
}

/*---------------------------------------------------------------------
RayRayInt: Finds the point of intersection p between a ray with origin a
and containing b and a ray with origin c containing d. Returns an object
containing p and an int with the following meaning:
   '10+': The rays collinearly overlap sharing points.
     '1': The rays intersect properly (i.e., they share a point and
        neither of the above are the case).
   '0': The rays do not intersect (i.e., they share no points).
---------------------------------------------------------------------*/
public static IntersectNew RayRayIntNew( Pointd a, Pointd b, Pointd c, Pointd d )
{
//	System.out.println("RayRayIntNew a= " + a + ", b= " + b + ", c= " + c + ", d= " + d);
	double s = 0.0;		/* The two parameters of the parametric eqns. */
	double t = 0.0;
	double num, denom;  /* Numerator and denoninator of equations. */
	double px, py;	    /* Coordinates of intersection point. */
	int code = 0;		/* Return code characterizing intersection. */

	denom = a.getx() * ( d.gety() - c.gety() ) +
	        b.getx() * ( c.gety() - d.gety() ) +
	        d.getx() * ( b.gety() - a.gety() ) +
	        c.getx() * ( a.gety() - b.gety() );
//	System.out.println("denom= " + denom);

	/* If denom is zero, then segments are parallel: handle separately. */
	if (denom == 0.0)
	{
//		System.out.println("RayRayIntNew: parallel rays");
		return  ParallelIntNew(a, b, c, d);
	}

	num =    a.getx() * ( d.gety() - c.gety() ) +
	         c.getx() * ( a.gety() - d.gety() ) +
	         d.getx() * ( c.gety() - a.gety() );
//	System.out.println("num= " + num);
	if ( (num == 0.0) || (num == denom) )
	{
		code = 2;
//		System.out.println("RayRayInt endpoint overlap 1");
	}
	s = num / denom;
//	System.out.println("RayRayInt num= " + num + ", denom= " + denom + ", s= " + s);

	num = -( a.getx() * ( c.gety() - b.gety() ) +
			 b.getx() * ( a.gety() - c.gety() ) +
			 c.getx() * ( b.gety() - a.gety() ) );
//	System.out.println("num= " + num);
	if ( (num == 0.0) || (num == denom) )
	{
		code = 2;
//		System.out.println("RayRayInt endpoint overlap 2");
	}
	t = num / denom;
//	System.out.println( "RayRayInt num= " + num + ", denom= " + denom + ". t= " + t);

	if (s > 0.0 && t > 0.0)
	{
		code = 1;
//		System.out.println("RayRayInt proper intersection s:t " + s + " : " + t);
	}

	px = a.getx() + s * ( b.getx() - a.getx() );
	py = a.gety() + s * ( b.gety() - a.gety() );
	Pointd p = new Pointd(px, py);
	Pointd q = null;

	return (new IntersectNew(p, q, code));
}

/* Return index of point making smallest angle */
	public static Pointd minAngle(Pointd p1, Pointd p2, ArrayList al)
	{
//		System.out.println("\nEnter minAngle p1: " + p1 + ", p2: " + p2);
		Pointd ptemp;
		ArrayList ltemp = new ArrayList();

		for (int i=0; i<al.size(); i++)
		{
			ptemp = (Pointd)al.get(i);
//			System.out.println("al(" + i + "): " + ptemp);
			if ( left(p1, p2, ptemp) )
			{
				ltemp.add(ptemp);
//				System.out.println("Adding to ltemp: " + ptemp + ", size: " + ltemp.size());
			}
		}
//		System.out.println("ltemp.size: " + ltemp.size());

		if ( ltemp.size() == 0 )
		{
			System.out.println("Geometry: invalid state for minangle");
			return null;
		}
		else if ( ltemp.size() == 1 )
		{
			return (Pointd)ltemp.get(0);
		}
		else
		{
			Pointd[] v = Qsort(p2, ltemp);
			return v[v.length-1];
		}
	}

	private static Pointd[] Qsort(Pointd p, ArrayList al)
	{
		Pointd[] v = new Pointd[1+al.size()];
		v[0] = p;
		for (int i=0; i<al.size(); i++)
			v[i+1] = (Pointd)al.get(i);
		Sort(v, 1, v.length-1);
		return v;
	}

	private static void Sort(Pointd[] v, int first, int last)
	{
		int lo, hi;

		if (first >= last)
			return;

		Pointd piv = v[(first + last)/2];
		lo = first;
		hi = last;

		do
		{
			while (lo <= last  && Compare(v, lo, piv) < 0)
				lo++;

			while (hi >= first && Compare(v, hi, piv) > 0)
				hi--;

			if (lo <= hi)
			{
				if (Compare(v, lo, hi) == 0)
				{
					lo++;
					hi--;
				}
				else
					Swap(v, lo, hi);
			}
		} while (lo <= hi);
		Sort(v, first, hi);
		Sort(v, lo, last);
	}

	public static void Swap(Pointd[] v, int i, int j)
	{
		Pointd pt = v[i];
		v[i] = v[j];
		v[j] = pt;
	}

	private static int Compare(Pointd[] p, int i, int j)
	{
		int as = Geometry.area_sign(p[0], p[i], p[j]);
		if ( as > 0)		/* left turn  */
			return -1;
		else if (as < 0)	/* right turn */
			return 1;
		else 					/* collinear  */
		{
			double x = Math.abs(p[i].getx() - p[0].getx()) - Math.abs(p[j].getx() - p[0].getx());
			double y = Math.abs(p[i].gety() - p[0].gety()) - Math.abs(p[j].gety() - p[0].gety());
			if ( (x < 0) || (y < 0) )
				return -1;
			else if ( (x > 0) || (y > 0) )
				return 1;
			else		// points are coincident
				return 0;
		}
	}

	private static int Compare(Pointd[] p, int i, Pointd m)
	{
		int as = Geometry.area_sign(p[0], p[i], m);
		if ( as > 0)		/* left turn  */
			return -1;
		else if (as < 0)	/* right turn */
			return 1;
		else 					/* collinear  */
		{
			double x = Math.abs(p[i].getx() - p[0].getx()) - Math.abs(m.getx() - p[0].getx());
			double y = Math.abs(p[i].gety() - p[0].gety()) - Math.abs(m.gety() - p[0].gety());
			if ( (x < 0) || (y < 0) )
				return -1;
			else if ( (x > 0) || (y > 0) )
				return 1;
			else		// points are coincident
				return 0;
		}
	}

	public static Pointd midpoint(Pointd v1, Pointd v2)
	{
		double mx, my;

		mx = (v1.getx() + v2.getx()) / 2.0;
		my = (v1.gety() + v2.gety()) / 2.0;
		return new Pointd(mx, my);
	}
	
	public static Pointd[] removeCollinearVertices(Pointd[] v)
	{
		int n = v.length;
		Vector<Pointd> vv = new Vector<Pointd>();
		int i = 0, j = 0;
		while (i < n)
		{
			for (j = i; j < n; j++)
			{
				if (!collinear(v[i], v[(j+2)%n], v[(j+1)%n]))
				{
					j++;
					break;
				}
			}
			vv.add(v[i]);
			i = j;
		}
		Pointd[] u = new Pointd[vv.size()];
		vv.toArray(u); 
		return u;
	}

}