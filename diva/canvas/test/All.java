/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.canvas.test;
import diva.util.jester.TestHarness;
import diva.util.jester.TestSuite;


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
        // Test concrete figures
        new ConcreteFigures(getTestHarness()).run();

        // Canvas tests
        new JCanvasTest(
                getTestHarness(),
                new JCanvasTest.CanvasFactory()).run();
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




