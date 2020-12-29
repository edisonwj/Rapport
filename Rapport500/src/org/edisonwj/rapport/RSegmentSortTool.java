package org.edisonwj.rapport;

/**
 * RSegmentSortTool defines the sort method for line segments
 * as adapted from StringSortTool in:
 *
 *  JAVA ALGORITHMS
 *  ---------------
 *  Copyright 1997 Scott Robert Ladd
 *  All rights reserved
 */
 
public class RSegmentSortTool
    implements SortTool
{

    // compare two values
    public int compare
        (
        Object x1,
        Object x2
        )
    {
        if ((x1 instanceof RSegment)
        &&  (x2 instanceof RSegment))
        {
            int c = ((RSegment)x1).compareTo((RSegment)x2);

            if (c < 0)
                return COMP_LESS;
            else
                if (c > 0)
                    return COMP_GRTR;
                else
                    return COMP_EQUAL;
        }
        else
            throw SortTool.err1;
    }

    // merge segments
    public Object mergedata
        (
        Object x1,
        Object x2
        )
    {
		return x1;
	}
}

