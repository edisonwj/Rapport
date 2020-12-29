package org.edisonwj.rapport;

/**
 * Defines a jpgFilter for use in saving jpg images
 */

import java.io.*;
import javax.swing.filechooser.FileFilter;

public class jpgFilter extends FileFilter
{
	public boolean accept(File f)
	{
		return f.getName().toLowerCase().endsWith(".jpg")
					|| f.isDirectory();
	}

	public String getDescription()
	{
		return "jpg File";
	}
}