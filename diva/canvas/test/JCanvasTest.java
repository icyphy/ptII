/*
 Copyright (c) 1998-2014 The Regents of the University of California
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */
package diva.canvas.test;

import javax.swing.JFrame;

import diva.canvas.CanvasPane;
import diva.canvas.JCanvas;
import diva.canvas.toolbox.BasicCanvasPane;
import diva.util.jester.TestCase;
import diva.util.jester.TestFailedException;
import diva.util.jester.TestHarness;
import diva.util.jester.TestSuite;

/**
 * A test suite for JCanvas. This is fairly basic, and is
 * really just a few simple confidence tests.
 *
 * @author John Reekie
 * @version $Id$
 */
public class JCanvasTest extends TestSuite {
    /**
     * The unit factory
     */
    private CanvasFactory factory;

    /** Constructor
     */
    public JCanvasTest(TestHarness harness, CanvasFactory factory) {
        setTestHarness(harness);
        this.factory = factory;
    }

    /**
     * runSuite()
     */
    @Override
    public void runSuite() {
        testConstructor1();
        testConstructor2();
    }

    ///////////////////////////////////////////////////////////////////
    //// Test methods

    /** Test the nullary constructor
     */
    public void testConstructor1() {
        runTestCase(new TestCase("JCanvas constructor 1") {
            JCanvas canvas = null;

            @Override
            public void run() throws Exception {
                canvas = factory.createJCanvas();
            }

            @Override
            public void check() throws TestFailedException {
                CanvasPane pane = canvas.getCanvasPane();
                assertExpr(pane != null, "pane != null");
                assertExpr(
                        pane.getClass().getName()
                                .equals("diva.canvas.GraphicsPane"), pane
                                .getClass().getName()
                                + " != diva.canvas.GraphicsPane");
            }
        });
    }

    /** Test the constructor that takes a pane
     */
    public void testConstructor2() {
        runTestCase(new TestCase("JCanvas constructor 2") {
            JCanvas canvas = null;

            CanvasPane pane = new BasicCanvasPane();

            @Override
            public void run() throws Exception {
                canvas = factory.createJCanvas(pane);
            }

            @Override
            public void check() throws TestFailedException {
                CanvasPane pane = canvas.getCanvasPane();
                assertExpr(pane != null, "pane != null");
                assertEquals(pane, this.pane, "pane == this.pane");
            }
        });
    }

    ///////////////////////////////////////////////////////////////////
    ////  main

    /** Create a default test harness and a canvas factory and
     * run all tests on it.
     */
    public static void main(String[] argv) {
        new JCanvasTest(new TestHarness(), new CanvasFactory()).run();
    }

    ///////////////////////////////////////////////////////////////////
    //// Test cases

    /**
     * FramedCanvas is a test case that accepts a JCanvas and places
     * it into a pane. This ensures that methods like paint() get called.
     */
    public static class FramedCanvas {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        public FramedCanvas(JCanvas canvas) {
            JFrame frame = new JFrame();
            canvas.setSize(600, 400);
            frame.getContentPane().add(canvas);
            frame.pack();
            frame.setVisible(true);
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// Factories

    /**
     * The canvas factory provides methods for default creation
     * of canvas objects.
     */
    public static class CanvasFactory {
        /** Create a canvas with the default pane
         */
        public JCanvas createJCanvas() {
            return new JCanvas();
        }

        /** Create a canvas with the given pane.
         */
        public JCanvas createJCanvas(CanvasPane pane) {
            return new JCanvas(pane);
        }
    }
}
