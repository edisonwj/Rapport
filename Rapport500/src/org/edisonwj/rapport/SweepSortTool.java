package org.edisonwj.rapport;

/**
 * SweepSortTool provides a sort method for line sweep processing
 *
 *  Adapted from StringSortTool in
 *
 *  JAVA ALGORITHMS
 *  ---------------
 *  Copyright 1997 Scott Robert Ladd
 *  All rights reserved
 */

public class SweepSortTool
    implements SortTool
{
    // compare two values
    public int compare
        (
        Object x1,
        Object x2
        )
    {
        if ((x1 instanceof SweepItem)
        &&  (x2 instanceof SweepItem))
        {
            int c = ((SweepItem)x1).compareTo((SweepItem)x2);

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

    // merge values
    public Object mergedata
        (
        Object x1,
        Object x2
        )
    {
		return null;
	}
}

