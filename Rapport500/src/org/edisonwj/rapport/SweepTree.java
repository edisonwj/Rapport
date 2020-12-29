package org.edisonwj.rapport;

/* SweepTree defines the overall red-black tree
 * structure to support line sweep processing
 */

public class SweepTree
    extends RedBlackTree
{
	protected Pointd psweep;

	public SweepTree
	       (
	       SortTool tool
	       )
	{
		super(tool);
	}

	public void setPsweep(Pointd p)
	{
		psweep = p;
	}

	public Pointd getPsweep()
	{
		return psweep;
	}
}