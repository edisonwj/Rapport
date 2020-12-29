package org.edisonwj.rapport;

/**
 * This class implements a binary tree.
 *
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
 *  This software is sold "as is" without warranty of any kind.*
 *
 *  package coyote.tools;
 * 
 * BinaryTree
 *
 */

public class BinaryTree
    implements Iterable
{
    // class-global constant
    protected Node SENTINEL = new Node();

    //-------------------------
    // inner classes
    //-------------------------
    protected class Node
    {
        //-------------------------
        // fields
        //-------------------------
        public Node prev;
        public Node next;
        public Node parent;

        public Object content;

        //-------------------------
        // constructors
        //-------------------------

        // construct sentinel node
        protected Node()
        {
            prev    = this;
            next    = this;
            parent  = null;

            content = null;
        }

        // construct new node
        public Node
            (
            Object obj
            )
        {
            prev   = SENTINEL;
            next   = SENTINEL;
            parent = SENTINEL;

            content = obj;
        }
    }

    protected class BinaryTreeIterator
        implements Iterator
    {
        //-------------------------
        // fields
        //-------------------------
        protected Node current;

        //-------------------------
        // constructors
        //-------------------------
        public BinaryTreeIterator()
        {
//            ++locks;
            goFirst();
        }

        protected void finalize()
        {
//            --locks;
        }

        //-------------------------
        // methods
        //-------------------------

        // move to first element
        public void goFirst()
        {
            current = minimum(root);
        }

        // move to last element
        public void goLast()
        {
            current = maximum(root);
        }

        // move to next (greater) element
        public void next()
        {
            current = successor(current);
        }

        // move to previous (lesser) element
        public void prev()
        {
            current = predecessor(current);
        }

        // true if this iterator points to a valid object
        public boolean isValid()
        {
            return current != SENTINEL;
        }

        // return reference to current object
        public Object getObject()
        {
            return current.content;
        }
    }

    //-------------------------
    // fields
    //-------------------------

    // root node
    protected Node root;

    // tool for sorting
    protected SortTool tool;

    // number of elements resident
    protected int count;

    // number of iterator locks
    protected int locks;

    //-------------------------
    // exceptions
    //-------------------------

    protected BinaryTreeException err_not_found = new BinaryTreeException("item not found in binary tree");
    protected BinaryTreeException err_locked    = new BinaryTreeException("binary tree locked");

    //-------------------------
    // constructors
    //-------------------------
    public BinaryTree
        (
        SortTool tool
        )
    {
        this.tool = tool;

        root  = SENTINEL;
        count = 0;
        locks = 0;
    }

    //-------------------------
    // properties
    //-------------------------
    public int getLocks()
    {
        return locks;
    }

    public int getCount()
    {
        return count;
    }

    //-------------------------
    // internal utility methods
    //-------------------------
    protected Node minimum
        (
        Node n
        )
    {
        if (n != SENTINEL)
            while (n.prev != SENTINEL)
                n = n.prev;

        return n;
    }

    protected Node maximum
        (
        Node n
        )
    {
        if (n != SENTINEL)
            while (n.next != SENTINEL)
                n = n.next;

        return n;
    }

    protected Node predecessor
        (
        Node n
        )
    {
        Node x, y;

        if (n.prev != SENTINEL)
            return maximum(n.prev);
        else
        {
            x = n;
            y = n.parent;

            while ((y != SENTINEL) && (x == y.prev))
            {
                x = y;
                y = y.parent;
            }
        }

        return y;
    }

    protected Node successor
        (
        Node n
        )
    {
        Node x, y;

        if (n.next != SENTINEL)
            return minimum(n.next);
        else
        {
            x = n;
            y = n.parent;

            while ((y != SENTINEL) && (x == y.next))
            {
                x = y;
                y = y.parent;
            }
        }

        return y;
    }

    protected Node search
        (
        Object item
        )
    {
        Node n = root;

        while (n != SENTINEL)
        {
            if (n.content == item)
                return n;

            if (tool.compare(item,n.content) == SortTool.COMP_LESS)
                n = n.prev;
            else
                n = n.next;
        }

        throw err_not_found;
    }

    protected Node insert
        (
        Object item
        )
    {
        if (locks > 0)
            throw err_locked;

        Node y = SENTINEL;
        Node x = root;

        while (x != SENTINEL)
        {
            y = x;

            if (item.equals(x.content))
                return null;
            else
            {
                if (tool.compare(item,x.content) == SortTool.COMP_LESS)
                    x = x.prev;
                else
                    x = x.next;
            }
        }

        Node z = new Node(item);
        z.parent = y;

        if (y == SENTINEL)
            root = z;
        else
        {
            if (tool.compare(z.content,y.content) == SortTool.COMP_LESS)
                y.prev = z;
            else
                y.next = z;
        }

        ++count;

        return z;
    }

    //-------------------------
    // methods
    //-------------------------
    public void add
        (
        Object item
        )
    {
        insert(item);
    }

    public void remove
        (
        Object item
        )
    {
        if (locks > 0)
            throw err_locked;

        // find node
        Node z = search(item);

        // locate node to be sliced out
        Node x, y;

        if ((z.prev == SENTINEL) || (z.next == SENTINEL))
            y = z;
        else
            y = successor(z);

        // find child to replace with y
        if (y.prev == SENTINEL)
            x = y.next;
        else
            x = y.prev;

        // splice child onto parent
        if (x != SENTINEL)
            x.parent = y.parent;

        if (y.parent == SENTINEL)
            root = x;
        else
        {
            // splice child node
            if (y == y.parent.prev)
                y.parent.prev = x;
            else
                y.parent.next = x;
        }

        // do we need to save y?
        if (y != z)
            z.content = y.content;

        --count;
    }

    public Iterator makeIterator()
    {
        return new BinaryTreeIterator();
    }
}

