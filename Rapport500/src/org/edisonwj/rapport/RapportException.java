package org.edisonwj.rapport;

/**
 * RapportException provides for general Rapport exception handling.
 */

import java.lang.RuntimeException;

public class RapportException
    extends RuntimeException
{

    public RapportException()
    {
        super();
    }

    public RapportException(String s)
    {
        super(s);
    }

}

