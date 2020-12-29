package org.edisonwj.rapport;

/**
 * Triangle defines a triangle object
 * Uses: Pointd
 *
 * Copied from Section 2.13 of:
 *    Ammeraal, L. (1998) Computer Graphics for Java Programmers,
 *       Chichester: John Wiley.
 */

import java.awt.Color;

class Triangle {
	Pointd A, B, C;
	Color pcolor;		/* Polygon color */
	String xcolor;	/* Hex color for Draw3D */
	
	
   Triangle(Pointd A, Pointd B, Pointd C) {
	   this.A = A; this.B = B; this.C = C;
   }
   
	public void setColor(Color pcolor) {
		this.pcolor = pcolor;
	}
	
	public Color getColor() {
		return pcolor;
	}
	
	public void setXColor(String xcolor) {
		this.xcolor = xcolor;
	}
	
	public String getXColor() {
		return xcolor;
	}
}