package org.edisonwj.rapport;

/**
 * Red black tree structure
 *
 *  JAVA ALGORITHMS
 *  ---------------
 *  Copyright 1997 Scott Robert Ladd
 *  All rights reserved
 *
 *  This software source code is sold as a component of the
 *  book JAVA ALGORITHMS, written by Scott Robert Ladd and
 *  published by McGraw-Hill, Inc. Please read the LICENSE
 *  AGREEMENT and DISCLAIMER OF WARRANTY printed in the book.
 *
 *  You may freely compile and link this source code into your
 *  non-commercial software programs, providing that you do
 *  not redistribute the source code or object code derived
 *  therefrom. If you want to use this source code in a
 *  commercial application, you must obtain written permission
 *  by contacting:
 *
 *      Scott Robert Ladd
 *      P.O. Box 617
 *      Silverton, Colorado
 *      81433-0617 USA
 *
 *  This software is sold "as is" without warranty of any kind.
 *
 * package coyote.tools;
 *
 */

import java.util.*;			/* wje addition */

public class RedBlackTree
    extends BinaryTree
{
    //-------------------------
    // inner classes
    //-------------------------

    protected class RBData
    {
        // fields
        public boolean red;
        public Object  content;

        // constructors
        public RBData
            (
            Object item
            )
        {
            red     = true;
            content = item;
        }

        public RBData
			(
			boolean red,
			Object item
			)
		{
			this.red = red;
			content = item;
        }

        public String toString()
        {
			String s = "RBData: red = " + red;

			if (content == null)
				return (s + " content = null");
			else
				return (s + " content = " + content.toString());
		}
    }

    protected class RBDataSortTool
        implements SortTool
    {
        // fields
        SortTool rbtool;

        // constructor
        public RBDataSortTool
            (
            SortTool tool
            )
        {
            rbtool = tool;
        }

        // comparison method
        public int compare
            (
            Object x1,
            Object x2
            )
        {
            return rbtool.compare(((RBData)x1).content,((RBData)x2).content);
        }

        // mergedata method
        public Object mergedata
            (
            Object x1,
            Object x2
            )
        {
            return rbtool.mergedata(((RBData)x1).content,((RBData)x2).content);
        }

    }

    protected class RBTreeIterator
        extends BinaryTreeIterator
    {
        // constructors
        public RBTreeIterator()
        {
            super();
        }

        // return reference to current object
        public Object getObject()
        {
            return ((RBData)(current.content)).content;
        }
    }

    //-------------------------
    // constructors
    //-------------------------
    public RedBlackTree
        (
        SortTool tool
        )
    {
        super(null);

        // use a new constructor
        this.tool = new RBDataSortTool(tool);

        // change SENTINEL for red-black algorithm
        RBData rbd = new RBData(null);
        rbd.red = false; // sentinel is black
        SENTINEL.content = rbd;
    }

    //-------------------------
    // internal utility methods
    //-------------------------
    protected void rotateLeft
        (
        Node n
        )
    {
        Node y = n.next;

        // turn y's left subtrree into n's right subtree
        n.next = y.prev;

        if (y.prev != SENTINEL)
            y.prev.parent = n;

        // link n's parent to y
        y.parent = n.parent;

        if (n.parent == SENTINEL)
            root = y;
        else
        {
            if (n == n.parent.prev)
                n.parent.prev = y;
            else
                n.parent.next = y;
        }

        // put n on y's left
        y.prev   = n;
        n.parent = y;
    }

    protected void rotateRight
        (
        Node n
        )
    {
        Node y = n.prev;

        // turn y's left subtrree into n's right subtree
        n.prev = y.next;

        if (y.next != SENTINEL)
            y.next.parent = n;

        // link n's parent to y
        y.parent = n.parent;

        if (n.parent == SENTINEL)
            root = y;
        else
        {
            if (n == n.parent.next)
                n.parent.next = y;
            else
                n.parent.prev = y;
        }

        // put n on y's left
        y.next   = n;
        n.parent = y;
    }

    protected void deleteFixup
        (
        Node n
        )
    {
        Node w, x = n;

        RBData rbx = (RBData)(x.content);

        while ((x != root) && ((RBData)(x.content)).red == false)
        {
            if (x == x.parent.prev)
            {
                w = x.parent.next;

                if (((RBData)(w.content)).red)
                {
                    ((RBData)(w.content)).red        = false;
                    ((RBData)(x.parent.content)).red = true;
                    rotateLeft(x.parent);
                    w = x.parent.next;
                }

                if ((!((RBData)(w.prev.content)).red)
                &&  (!((RBData)(w.next.content)).red))
                {
                    ((RBData)(w.content)).red = true;
                    x = x.parent;
                }
                else
                {
                    if (!((RBData)(w.next.content)).red)
                    {
                        ((RBData)(w.prev.content)).red = false;
                        ((RBData)(w.content)).red      = true;
                        rotateRight(w);
                        w = x.parent.next;
                    }

                    ((RBData)(w.content)).red = ((RBData)(x.parent.content)).red;
                    ((RBData)(x.parent.content)).red = false;
                    ((RBData)(w.next.content)).red   = false;
                    rotateLeft(x.parent);
                    x = root;
                }
            }
            else
            {
                w = x.parent.prev;

                if (((RBData)(w.content)).red)
                {
                    ((RBData)(w.content)).red        = false;
                    ((RBData)(x.parent.content)).red = true;
                    rotateRight(x.parent);
                    w = x.parent.prev;
                }

                if ((!((RBData)(w.next.content)).red)
                &&  (!((RBData)(w.prev.content)).red))
                {
                    ((RBData)(w.content)).red = true;
                    x = x.parent;
                }
                else
                {
                    if (!((RBData)(w.prev.content)).red)
                    {
                        ((RBData)(w.next.content)).red = false;
                        ((RBData)(w.content)).red      = true;
                        rotateLeft(w);
                        w = x.parent.prev;
                    }

                    ((RBData)(w.content)).red = ((RBData)(x.parent.content)).red;
                    ((RBData)(x.parent.content)).red = false;
                    ((RBData)(w.prev.content)).red   = false;
                    rotateRight(x.parent);
                    x = root;
                }
            }
        }

        ((RBData)(x.content)).red = false;
    }

    //-------------------------
    // internal utility methods
    //-------------------------
    protected Node search
        (
        RBData item
        )
    {
        Node n = root;

        while (n != SENTINEL)
        {
			int c = tool.compare(item,n.content);
			switch(c)
			{
				case -1:		/* < */
					n = n.prev;
				break;

				case 1:			/* > */
					n = n.next;
				break;

				case 0:			/* = */
					return n;
			}

			if (((RBData)(n.content)).content == item.content)
                return n;
//
//            if (tool.compare(item,n.content) == SortTool.COMP_LESS)
//                n = n.prev;
//            else
//                n = n.next;

        }

        throw err_not_found;

    }

    //-------------------------
    // methods
    //-------------------------
    public void add
        (
        Object item
        )
    {
		// see if item is already in the tree
		try
		{
			RBData rbd = new RBData(item);
			Node z = search(rbd);
//			System.out.println ( "Replace content-1: " +
//					((RBData)(z.content)).red + " " +
//					((RBData)(z.content)).content );

//			z.content = new RBData(
//				((RBData)(z.content)).red,
//				((RBData)(z.content)).content );

//			z.content = new RBData(
//				((RBData)(z.content)).red,
//				tool.mergedata(z.content, rbd) );

//			z = search(new RBData(item));
//			System.out.println ( "Replace content-2: " +
//					((RBData)(z.content)).red + " " +
//					((RBData)(z.content)).content );

			return;
		}
		catch (BinaryTreeException e) {}

        // insert the item into the tree (automatically red)
        Node y, x = insert(new RBData(item));

        RBData x_data = (RBData)(x.content);

//        String i_str = (String)item;
//        String x_str = (String)x_data.content;

        // adjust the tree
        while ((x != root) && ((RBData)(x.parent.content)).red)
        {
            if (x.parent == x.parent.parent.prev)
            {
                y = x.parent.parent.next;

                if (((RBData)(y.content)).red)
                {
                    ((RBData)(x.parent.content)).red        = false;
                    ((RBData)(y.content)).red               = false;
                    ((RBData)(x.parent.parent.content)).red = true;
                    x = x.parent.parent;
                }
                else
                {
                    if (x == x.parent.next)
                    {
                        x = x.parent;
                        rotateLeft(x);
                    }

                    ((RBData)(x.parent.content)).red        = false;
                    ((RBData)(x.parent.parent.content)).red = true;
                    rotateRight(x.parent.parent);
                }
            }
            else
            {
                y = x.parent.parent.prev;

                if (((RBData)(y.content)).red)
                {
                    ((RBData)(x.parent.content)).red        = false;
                    ((RBData)(y.content)).red               = false;
                    ((RBData)(x.parent.parent.content)).red = true;
                    x = x.parent.parent;
                }
                else
                {
                    if (x == x.parent.prev)
                    {
                        x = x.parent;
                        rotateRight(x);
                    }

                    ((RBData)(x.parent.content)).red        = false;
                    ((RBData)(x.parent.parent.content)).red = true;
                    rotateLeft(x.parent.parent);
                }
            }
        }

        ((RBData)(root.content)).red = false;
    }

    public void remove
        (
        Object item
        )
    {
        if (locks > 0)
            throw err_locked;

        // find node
        Node z = search(new RBData(item));

        // find node to splice out
        Node x, y;

        if ((z.prev == SENTINEL) || (z.next == SENTINEL))
            y = z;
        else
            y = successor(z);

        // find child to replace y
        if (y.prev != SENTINEL)
            x = y.prev;
        else
            x = y.next;

        // splice child to parent
        x.parent = y.parent;

        if (y.parent == SENTINEL)
            root = x;
        else
        {
            // splice
            if (y == y.parent.prev)
                y.parent.prev = x;
            else
                y.parent.next = x;
        }

        // save y if necessary
        if (y != z)
            z.content = y.content;

        // adjust tree for red-black rules
        if (((RBData)(y.content)).red == false)
            deleteFixup(x);

        --count;
    }

    public void reverse
    	(
		Object item1,
		Object item2
		)
	{
        if (locks > 0)
            throw err_locked;

//		System.out.println("reverse-1: " + item1);
//		System.out.println("reverse-1: " + item2);

        // find nodes
        Node a, b;

        a = search(new RBData(item1));
//		System.out.println ("reverse-2: " +
//				((RBData)(a.content)).red + " " +
//				((RBData)(a.content)).content );
        b = search(new RBData(item2));
//		System.out.println ("reverse-2: " +
//				((RBData)(b.content)).red + " " +
//				((RBData)(b.content)).content );

        // reverse nodes
 //       if ( ((RBData)(b.content)).content == item2 )
 //       {
			Object c = ((RBData)(a.content)).content;
//			System.out.println ("reverse-c: " + c);

			((RBData)(a.content)).content = ((RBData)(b.content)).content;
//			System.out.println ("reverse-3: " +
//					((RBData)(a.content)).red + " " +
//					((RBData)(a.content)).content );

			((RBData)(b.content)).content = c;
//			System.out.println ("reverse-3: " +
//					((RBData)(b.content)).red + " " +
//					((RBData)(b.content)).content );
//		}
//
//		else
//			throw err_not_found;
	}

//    public Object get
//    	(
//		Object item
//		)
//	{
//		if ( count <= 0)
//			throw err_not_found;
//
//		Node z = search(new RBData(item));
//		Object result = ((RBData)(z.content)).content;
//		return result;
//	}

    public Object removeFirst()
    {
		if (count <= 0)
			throw err_not_found;

		// find minimum node, remove it, and return content.
		Node z = minimum(root);
		Object item = ((RBData)(z.content)).content;
    	remove(item);
    	return item;
	}

    public Object findPredecessor
    	(
		Object item
		)
	{
		Object result;

		// find node

		try
		{
		    Node z = search(new RBData(item));

    	    // find predecessor

		   	result = ((RBData)(predecessor(z).content)).content;
//		   	System.out.println("findPredecessor: " + z + " " + result);
		}
		catch (BinaryTreeException e)
		{
			result = null;
//			System.out.println("findPredecessor - did not find node");
		}

		return result;
	}

    public Object findSuccessor
    	(
		Object item
		)
	{
		Object result;

		// find node

		try
		{
		    Node z = search(new RBData(item));

    	    // find successor

		   	result = ((RBData)(successor(z).content)).content;
		}
		catch (BinaryTreeException e)
		{
			result = null;
		}

		return result;
	}

    public Object[] findNeighbors
    	(
		Object item
		)
	{
		Object[] result = new Object[2];
		// find node

		try
		{
//			System.out.println("findNeighbors-1: " + item);
		    Node z = search(new RBData(item));
//			System.out.println ("findNeighbors-2: " +
//					((RBData)(z.content)).red + " " +
//					((RBData)(z.content)).content );

    	    // find left and right neighbors
		   	result[0] = ((RBData)(predecessor(z).content)).content;
//		   	System.out.println("Found predecessor");
   			result[1] = ((RBData)(successor(z).content)).content;
// 			System.out.println("Found successor");
		}
		catch (BinaryTreeException e)
		{
			result = null;
		}

		return result;
	}

    public Iterator makeIterator()
    {
        return new RBTreeIterator();
    }

	public void ShowTree()
    {
		Stack stnode = new Stack();
		Node n = root;
		System.out.println("RedBlackTree-ShowTree");
		System.out.println( "Root: " +
							"content: " + (RBData)n.content +
							"\n" );

		stnode.push(n);
		while (!stnode.empty())
		{
			n = (Node)stnode.pop();
			System.out.println( "Node: " + n +
								"\ncontent: " + (RBData)n.content +
								"\npNode: " + n.prev +
								"\nprev: " + (RBData)n.prev.content +
								"\nnNode: " + n.next +
								"\nnext: " + (RBData)n.next.content +
								"\naNode: " + n.parent +
								"\nparent: " + (RBData)n.parent.content +
								"\n" );

			if (n.next != SENTINEL)
				stnode.push(n.next);
			if (n.prev != SENTINEL)
				stnode.push(n.prev);
			System.out.println("Stack: " + stnode + "\n");
		}
	}
}
