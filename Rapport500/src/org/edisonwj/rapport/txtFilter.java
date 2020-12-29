package org.edisonwj.rapport;

/**
 * txtFilter defines the txtFilter object used during
 * save operations by RFrame.
 */

import java.io.*;
import javax.swing.filechooser.FileFilter;

public class txtFilter extends FileFilter
{
	public boolean accept(File f)
	{
		return f.getName().toLowerCase().endsWith(".txt")
					|| f.isDirectory();
	}
	
	public String getDescription()
	{
		return "txt File";
	}
}