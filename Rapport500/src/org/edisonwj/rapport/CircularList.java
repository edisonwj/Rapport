package org.edisonwj.rapport;

/**
 * Implements a doubly-linked, circular list.
 */

import java.util.*;

public class CircularList
	implements RapportDefaults
{
	// Class variables providing for inexact comparison.

	private static final double epsilon = DEFAULTEPSILON;
	private static final boolean exact_comp = EXACT_COMP;
	private static final int debug = 1;
	private static final boolean LEFT = true;
	private static final boolean RIGHT = false;

	// Inner Classes

	public class Node
	{
		// Node Variables
		public Node pred;		/* Backward list link */
		public Node succ;		/* Forward list link */
		public Object content;	/* Object */

		// Node Constructors
		public Node(Object obj, Node pr, Node sc)
		{
			content = obj;
			pred = pr;
			succ = sc;
		}

		public Node(Object obj)
		{
			this(obj, null, null);
		}

		public Node()
		{
			this(null, null, null);
		}

		public void swap()
		{
			Node ntmp;
			ntmp = pred;
			pred = succ;
			succ = ntmp;
		}

		public void show()
		{
			System.out.println( "Node"	+
								"\nthis= "  + this +
								"\npred= "	+ pred +
								"\nsucc= "	+ succ +
								"\ncontent= "	+ content);
		}
	}

	public class CircularListException
		extends RuntimeException
	{
		public CircularListException()
		{
			super();
		}

		public CircularListException(String s)
		{
			super(s);
		}
	}

	// List Variables
	protected Node head;
	protected Node tail;
	protected Node current;
	protected int count;

	// List Exceptions
	protected CircularListException NoSuchElementException =
		new CircularListException("NoSuchElementException");

	// List Constructors
	public CircularList()
	{
		head = null;
		tail = null;
		current = null;
		count = 0;
	}

	public CircularList(Object obj)
	{
		head = tail = current = new Node(obj);
		head.succ = head;
		head.pred = head;
		count++;
	}

	public CircularList(ArrayList al)
	{
		this();
		for (int i = 0; i < al.size(); i++)
			this.add(al.get(i));
	}

	/*
	 * Reverse order of entries
	 */
	 public void reverse()
	 {
		 Node nxt;
		 current = head;
		 nxt = current.succ;
		 for (int i = 0; i < count; i++)
		 {
			current.swap();
			current = nxt;
			nxt = current.succ;
		}
	 }

	/*
	 * Set current to object
	 */
	public void setCurrent(Object obj)
	{
		current = head;
		for (int i = 0; i < count; i++)
		{
			if (obj.equals(current.content))
				return;
			else
				current = current.succ;
		}
		throw NoSuchElementException;
	}

	/*
	 * Adds an item after the current entry.
	 */
	public void add(Object obj)
	{
		Node nn = new Node(obj);

		if (head == null)
		{
			head = tail = nn;
			nn.pred = nn;
			nn.succ = nn;
		}
		else
		{
			nn.succ = current.succ;
			nn.pred = current;
			(current.succ).pred = nn;
			current.succ = nn;
		}
		current = nn;
		count++;
	}

	/*
	 * Adds an item after entry ix.
	 */
	public void add(int ix, Object obj)
	{
		Node nn = new Node(obj);

		if (ix >= count)
			throw NoSuchElementException;
		else
		{
			current = head;
			for (int i = 0; i < ix; i++)
				current = current.succ;

			nn.succ = current.succ;
			nn.pred = current;
			(current.succ).pred = nn;
			current.succ = nn;
		}
		current = nn;
		count++;
	}

	/*
	 * Adds an item after the current entry, not advancing current..
	 */
	public void addNoAdvance(Object obj)
	{
		Node nn = new Node(obj);

		if (head == null)
		{
			head = tail = nn;
			nn.pred = nn;
			nn.succ = nn;
		}
		else
		{
			nn.succ = current.succ;
			nn.pred = current;
			(current.succ).pred = nn;
			current.succ = nn;
		}
		count++;
	}

	/*
	 * Adds an item as the first entry.
	 */
	public void addHead(Object obj)
	{
		Node nn = new Node(obj);

		if (head == null)
		{
			head = tail = nn;
			nn.pred = nn;
			nn.succ = nn;
		}
		else
		{
			nn.succ = head;
			nn.pred = head.pred;
			(head.pred).succ = nn;
			head.pred = nn;
			head = nn;
		}
		current = nn;
		count++;
	}

	/*
	 * Adds an item as the last entry.
	 */
	public void addTail(Object obj)
	{
		Node nn = new Node(obj);

		if (head == null)
		{
			head = tail = nn;
			nn.pred = nn;
			nn.succ = nn;
		}
		else
		{
			nn.pred = tail;
			nn.succ = tail.succ;
			(tail.succ).pred = nn;
			tail.succ = nn;
			tail = nn;
		}
		current = nn;
		count++;
	}

	/*
	 * Inserts an item before the current entry.
	 */
	public void insert(Object obj)
	{
		Node nn = new Node(obj);

		if (head == null)
		{
			head = tail = nn;
			nn.pred = nn;
			nn.succ = nn;
		}
		else
		{
			nn.succ = current;
			nn.pred = current.pred;
			(current.pred).succ = nn;
			current.pred = nn;
		}
		current = nn;
		count++;
	}

	/*
	 * Inserts an item before entry ix.
	 */
	public void insert(int ix, Object obj)
	{
		Node nn = new Node(obj);

		if (ix >= count)
			throw NoSuchElementException;
		else
		{
			current = head;
			for (int i = 0; i < ix; i++)
				current = current.succ;

			nn.succ = current;
			nn.pred = current.pred;
			(current.pred).succ = nn;
			current.pred = nn;
		}
		current = nn;
		count++;
	}

	/*
	 * Inserts an item before the current entry with no change of current.
	 */
	public void insertNoAdvance(Object obj)
	{
		Node nn = new Node(obj);

		if (head == null)
		{
			head = tail = nn;
			nn.pred = nn;
			nn.succ = nn;
		}
		else
		{
			nn.succ = current;
			nn.pred = current.pred;
			(current.pred).succ = nn;
			current.pred = nn;
		}
		count++;
	}

	/*
	 * Set entry ix.
	 */
	public void set(int ix, Object obj)
	{
		if (ix > count)
			throw NoSuchElementException;

		current = head;
		for (int i = 0; i < ix; i++)
			current = current.succ;

		current.content = obj;
	}

	/*
	 * Remove current entry.
	 */
	public Object remove()
	{
		if (head == null || current == null)
			throw NoSuchElementException;

		Object obj = current.content;

		if (current == head)
			head = current.succ;
		else if (current == tail)
			tail = current.pred;

		(current.pred).succ = current.succ;
		(current.succ).pred = current.pred;
		current = current.succ;
		count--;
		return obj;
	}

	/*
	 * Remove entry ix.
	 */
	public Object remove(int ix)
	{
		if (head == null || current == null)
			throw NoSuchElementException;

		current = head;
		for (int i = 0; i < ix; i++)
			current = current.succ;
		return remove();
	}

	/*
	 * Remove entry containing obj.
	 */
	public boolean remove(Object obj)
	{
		if (head == null || current == null)
			return false;

		current = head;
		for (int i = 0; i < count; i++)
		{
			if (obj.equals(current.content))
			{
				remove();
				return true;
			}
			else
				current = current.succ;
		}
		return false;
	}

	/*
	 * Find index of obj - sets current to obj.
	 */
	public int indexOf(Object obj)
	{
		if (head == null || current == null)
			throw NoSuchElementException;

		current = head;
		for (int i = 0; i < count; i++)
		{
			if (obj.equals(current.content))
				return i;
			else
				current = current.succ;
		}
		throw NoSuchElementException;
	}

	/*
	 * Get object from item ix.
	 */
	public Object get(int ix)
	{
		if (head == null || current == null)
			throw NoSuchElementException;

		current = head;
		for (int i = 0; i < ix; i++)
			current = current.succ;
		return current.content;
	}

	/*
	 * Get current item.
	 */
	public Object get()
	{
		return current.content;
	}

	/*
	 * Get next item.
	 */
	public Object next()
	{
		current = current.succ;
		return current.content;
	}

	/*
	 * Get previous item.
	 */
	public Object prev()
	{
		current = current.pred;
		return current.content;
	}

	/*
	 * Return list size.
	 */
	public int size()
	{
		return count;
	}

	/*
	 * Print diagnostic message.
	 */
	public void diag(int i, String s)
	{
		if (debug >= i)
			System.out.println(s);
	}

	public void show()
	{
		System.out.println("List");
		System.out.println("head=    " + head);
		System.out.println("tail=    " + tail);
		System.out.println("current= " + current);
		System.out.println("count=   " + count);
		Node nn;
		if (head != null)
			nn = head;
		else
			nn = current;

		for (int i = 0; i < count; i++)
		{
			nn.show();
			nn = nn.succ;
		}
		System.out.println();
	}

	public static void main(String[] args)
	{
	}
}