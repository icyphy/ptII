/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.canvas.test;
import diva.util.jester.*;

import diva.canvas.*;
import diva.canvas.toolbox.*;

import javax.swing.JFrame;

/**
 * A test suite for JCanvas. This is fairly basic, and is
 * really just a few simple confidence tests.
 *
 * @author John Reekie      (johnr@eecs.berkeley.edu)
 * @version $Revision$
 */
public class JCanvasTest extends TestSuite {

    /**
     * The unit factory
     */
    private CanvasFactory factory;

    /** Constructor
     */
    public JCanvasTest (TestHarness harness, CanvasFactory factory) {
        setTestHarness(harness);
        this.factory = factory;
    }

    /**
     * runSuite()
     */
    public void runSuite () {
        testConstructor1();
        testConstructor2();
    }

    ///////////////////////////////////////////////////////////////////
    //// Test methods

    /** Test the nullary constructor
     */
    public void testConstructor1 () {
        runTestCase(new TestCase("JCanvas constructor 1") {
                JCanvas canvas;
                public void run () throws Exception {
                    canvas = factory.createJCanvas();
                }
                public void check () throws TestFailedException {
                    CanvasPane pane = canvas.getCanvasPane();
                    assertExpr(pane != null, "pane != null");
                    assertExpr(pane.getClass().getName().equals("diva.canvas.GraphicsPane"),
                            pane.getClass().getName() + " != diva.canvas.GraphicsPane");
                }
            });
    }

    /** Test the constructor that takes a pane
     */
    public void testConstructor2 () {
        runTestCase(new TestCase("JCanvas constructor 2") {
                JCanvas canvas;
                CanvasPane pane = new BasicCanvasPane();
                public void run () throws Exception {
                    canvas = factory.createJCanvas(pane);
                }
                public void check () throws TestFailedException {
                    CanvasPane pane = canvas.getCanvasPane();
                    assertExpr(pane != null, "pane != null");
                    assertEquals(pane,this.pane,"pane == this.pane");
                }
            });
    }

    ////////////////////////////////////////////////////////////
    ////  main

    /** Create a default test harness and a canvas factory and
     * run all tests on it.
     */
    public static void main (String argv[]) {
        new JCanvasTest(new TestHarness(), new CanvasFactory()).run();
    }

    ////////////////////////////////////////////////////////////
    //// Test cases

    /**
     * FramedCanvas is a test case that accepts a JCanvas and places
     * it into a pane. This ensures that methods like paint() get called.
     */
    public class FramedCanvas {
        public FramedCanvas (JCanvas canvas) {
            JFrame frame = new JFrame();
            canvas.setSize(600,400);
            frame.getContentPane().add(canvas);
            frame.pack();
            frame.show();
        }
    }

    ////////////////////////////////////////////////////////////
    //// Factories

    /**
     * The canvas factory provides methods for default creation
     * of canvas objects.
     */
    public static class CanvasFactory {
        /** Create a canvas with the default pane
         */
        public JCanvas createJCanvas () {
            return new JCanvas();
        }
        /** Create a canvas with the given pane.
         */
        public JCanvas createJCanvas (CanvasPane pane) {
            return new JCanvas(pane);
        }
    }
}



