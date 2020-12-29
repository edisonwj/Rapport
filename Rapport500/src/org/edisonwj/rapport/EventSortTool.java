package org.edisonwj.rapport;

/**
 * EventSortTool supports event sorting.
 * 
 * Adapted from StringSortTool in
 *
 *  JAVA ALGORITHMS
 *  ---------------
 *  Copyright 1997 Scott Robert Ladd
 *  All rights reserved
 */

public class EventSortTool
    implements SortTool
{

    // compare two values
    public int compare
        (
        Object x1,
        Object x2
        )
    {
        if ((x1 instanceof EventPoint)
        &&  (x2 instanceof EventPoint))
        {
            int c = ((EventPoint)x1).compareTo((EventPoint)x2);

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
        if ((x1 instanceof EventPoint)
        &&  (x2 instanceof EventPoint))
        {
//			System.out.println("mergedata x1: " + x1.toString());
//			System.out.println("mergedata x2: " + x2.toString());
			EventPoint x3 = ((EventPoint)x1).merge((EventPoint)x2);
//			System.out.println("mergedata x3: " + x3.toString());
			return x3;
        }
        else
            throw SortTool.err1;
    }

}

