/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.util.test;
import diva.util.jester.*;

import java.awt.*;


/**
 * All the tests in this directory.
 *
 * @author John Reekie (johnr@eecs.berkeley.edu)
 * @version $Revision$
 */
public class All extends TestSuite {

    /** Constructor
     */
    public All (TestHarness harness) {
        setTestHarness(harness);
    }

    /**
     * runSuite()
     */
    public void runSuite () {
        // Test Shape utilities
        new ShapeUtilitiesTest(getTestHarness()).run();

        // Test XML
        new XMLElementTest(getTestHarness()).run();
        new XMLParserTest(getTestHarness()).run();
    }

    ////////////////////////////////////////////////////////////
    ////  main

    /** Create a default test harness and
     * run all tests on it.
     */
    public static void main (String argv[]) {
        new All(new TestHarness()).run();
    }
}




