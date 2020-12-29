package org.edisonwj.rapport;

/**
 * Defines an Iterator interface --
 * prior to the current Java provided capability
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
 */
 
 public interface Iterator
{
    // move to first element
    void goFirst();

    // move to last element
    void goLast();

    // move to next (greater) element
    void next();

    // move to previous (lesser) element
    void prev();

    // true if this iterator points to a valid object
    boolean isValid();

    // return reference to current object
    Object getObject();
}

