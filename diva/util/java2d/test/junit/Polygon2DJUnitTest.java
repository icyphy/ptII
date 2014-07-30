/* JUnit test the diva.util.java2d.Polygon2DJUnitTest

 Copyright (c) 2011-2014 The Regents of the University of California.
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
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */

package diva.util.java2d.test.junit;

import org.junit.Assert;

import diva.util.java2d.Polygon2D;

///////////////////////////////////////////////////////////////////
//// Polygon2DJUnitTest
/**
 * Test out diva.util.java2d.Polygon2D class.
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class Polygon2DJUnitTest {

    @org.junit.Before
    public void before() {
        unitDiamond();
        unitSquare();
        unitSquareCentered();
    }

    /** Instantiate a 1x1 diamond with a centered on 0.0, 0.0. */
    public void unitDiamond() {
        // A square
        // {x0, y0, x1, y1, ... }
        double[] coordinates = new double[] { -0.5, 0.0, 0.0, 0.5, 0.5, 0.5,
                0.0, -0.5 };
        unitDiamond = new Polygon2D.Double(coordinates);
        unitDiamond.closePath();
        //System.out.println("unitDiamond: " + unitDiamond);
    }

    /** Instantiate a 1x1 square with a corner at 0.0, 0.0. */
    public void unitSquare() {
        // A square
        // {x0, y0, x1, y1, ... }
        double[] coordinates = new double[] { 0.0, 0.0, 1.0, 0.0, 1.0, 1.0,
                0.0, 1.0 };
        unitSquare = new Polygon2D.Double(coordinates);
        unitSquare.closePath();
        //System.out.println("unitSquare: " + unitSquare);
    }

    /** Instantiate a 1x1 square centered at 0.0, 0.0. */
    public void unitSquareCentered() {
        // A square
        // {x0, y0, x1, y1, ... }
        double[] coordinates = new double[] { -0.5, -0.5, -0.5, 0.5, 0.5, 0.5,
                0.5, -0.5 };
        unitSquareCentered = new Polygon2D.Double(coordinates);
        unitSquareCentered.closePath();
        //System.out.println("unitSquareCentered: " + unitSquareCentered);
    }

    /**
     *  Test Polygon2D.contains() on a unit diamond centered on 0.0.
     */
    @org.junit.Test
    public void unitDiamondContains() throws Exception {
        Assert.assertTrue("unitDiamond contains(0.0, 0.0)",
                unitDiamond.contains(0.0, 0.0));

        Assert.assertFalse("unitDiamond contains(0.0, 0.5)",
                unitDiamond.contains(0.0, 0.5));
        Assert.assertFalse("unitDiamond contains(0.5, 0.5)",
                unitDiamond.contains(0.5, 0.5));
        Assert.assertFalse("unitDiamond contains(0.0, -0.5)",
                unitDiamond.contains(0.0, -0.5));

        Assert.assertFalse("unitDiamond contains(-0.6, 0.0)",
                unitDiamond.contains(-0.6, 0.0));
        Assert.assertFalse("unitDiamond contains(0.1, 0.5)",
                unitDiamond.contains(0.1, 0.5));
        Assert.assertFalse("unitDiamond contains(0.6, 0.5)",
                unitDiamond.contains(0.6, 0.5));
        Assert.assertFalse("unitDiamond contains(0.1, -0.5)",
                unitDiamond.contains(0.1, -0.5));

        Assert.assertFalse("unitDiamond contains(2.0, 2.0)",
                unitDiamond.contains(2.0, 2.0));
        Assert.assertFalse("unitDiamond contains(2.0, -2.0)",
                unitDiamond.contains(2.0, -2.0));
        Assert.assertFalse("unitDiamond contains(-2.0, -2.0)",
                unitDiamond.contains(-2.0, -2.0));
        Assert.assertFalse("unitDiamond contains(-2.0, 2.0)",
                unitDiamond.contains(-2.0, 2.0));
    }

    /** Corner case that fails in old implementation of contains(). */
    @org.junit.Test
    public void unitDiamondContains2() {
        // This test is questionable because it is at one of the vertices.
        Assert.assertTrue("unitDiamond contains(-0.5, 0.0)",
                unitDiamond.contains(-0.5, 0.0));
    }

    /**
     *  Test Polygon2D.contains() on a unit square with a corner at
     *  0.0, 0.0.
     */
    @org.junit.Test
    public void unitSquareContains() throws Exception {
        Assert.assertTrue("unitSquare contains(0.5, 0.5)",
                unitSquare.contains(0.5, 0.5));

        Assert.assertFalse("unitSquare contains(1.0, 1.0)",
                unitSquare.contains(1.0, 1.0));
        Assert.assertFalse("unitSquare contains(1.0, 0.0)",
                unitSquare.contains(1.0, 0.0));

        Assert.assertFalse("unitSquare contains(1.1, 1.0)",
                unitSquare.contains(1.1, 1.0));
        Assert.assertFalse("unitSquare contains(1.1, 0.0)",
                unitSquare.contains(1.1, 0.0));

        Assert.assertFalse("unitSquare contains(2.0, 2.0)",
                unitSquare.contains(2.0, 2.0));
        Assert.assertFalse("unitSquare contains(2.0, -2.0)",
                unitSquare.contains(2.0, -2.0));
        Assert.assertFalse("unitSquare contains(-2.0, -2.0)",
                unitSquare.contains(-2.0, -2.0));
        Assert.assertFalse("unitSquare contains(-2.0, 2.0)",
                unitSquare.contains(-2.0, 2.0));
    }

    /** Corner case that fails in old implementation of contains(). */
    @org.junit.Test
    public void unitSquareContains2() {
        // This test is questionable because it is at one of the vertices.
        Assert.assertTrue("unitSquare contains(0.0, 0.0)",
                unitSquare.contains(0.0, 0.0));
    }

    /** Corner case that fails in old implementation of contains(). */
    @org.junit.Test
    public void unitSquareContains3() {
        // This test definitely should pass.
        Assert.assertFalse("unitSquare contains(-0.1, 0.0)",
                unitSquare.contains(-0.1, 0.0));
    }

    /** Corner case that fails in old implementation of contains(). */
    @org.junit.Test
    public void unitSquareContains4() {
        // This test definitely should pass.
        Assert.assertFalse("unitSquare contains(-0.1, 1.0)",
                unitSquare.contains(-0.1, 1.0));
    }

    /** Corner case that fails in old implementation of contains(). */
    @org.junit.Test
    public void unitSquareContains5() {
        // This test is questionable because it is at one of the vertices.
        Assert.assertFalse("unitSquare contains(0.0, 1.0)",
                unitSquare.contains(0.0, 1.0));
    }

    /**
     *  Test Polygon2D.contains() on a unit square centered at 0.0, 0.0.
     */
    @org.junit.Test
    public void unitSquareCenteredContains() throws Exception {
        Assert.assertTrue("unitSquareCentered contains(0.0, 0.0)",
                unitSquareCentered.contains(0.0, 0.0));

        // Things get odd when the point to be checked is the same as a vertice.
        Assert.assertTrue("unitSquareCentered contains(-0.5, -0.5)",
                unitSquareCentered.contains(-0.5, -0.5));
        Assert.assertFalse("unitSquareCentered contains(-0.5, 0.5)",
                unitSquareCentered.contains(-0.5, 0.5));
        Assert.assertFalse("unitSquareCentered contains(0.5, 0.5)",
                unitSquareCentered.contains(0.5, 0.5));
        Assert.assertFalse("unitSquareCentered contains(0.5, -0.5)",
                unitSquareCentered.contains(0.5, -0.5));

        Assert.assertFalse("unitSquareCentered contains(0.6, 0.5)",
                unitSquareCentered.contains(0.6, 0.5));
        Assert.assertFalse("unitSquareCentered contains(0.6, -0.5)",
                unitSquareCentered.contains(0.6, -0.5));

        Assert.assertFalse("unitSquareCentered contains(-0.5, -0.6)",
                unitSquareCentered.contains(-0.5, -0.6));
        Assert.assertFalse("unitSquareCentered contains(-0.5, 0.6)",
                unitSquareCentered.contains(-0.5, 0.6));
        Assert.assertFalse("unitSquareCentered contains(0.5, 0.6)",
                unitSquareCentered.contains(0.5, 0.6));
        Assert.assertFalse("unitSquareCentered contains(0.5, -0.6)",
                unitSquareCentered.contains(0.5, -0.6));

        Assert.assertTrue("unitSquareCentered contains(-0.4, -0.5)",
                unitSquareCentered.contains(-0.4, -0.5));

        // Things get odd when the y value of the point to be checked is the same as the horizontal line.
        Assert.assertFalse("unitSquareCentered contains(-0.4, 0.501)",
                unitSquareCentered.contains(-0.4, 0.501));
        Assert.assertFalse("unitSquareCentered contains(-0.4, 0.5)",
                unitSquareCentered.contains(-0.4, 0.5));
        Assert.assertTrue("unitSquareCentered contains(-0.4, 0.4999)",
                unitSquareCentered.contains(-0.4, 0.499));

        // Things get odd when the y value of the point to be checked is the same as the horizontal line.
        Assert.assertFalse("unitSquareCentered contains(0.4, 0.501)",
                unitSquareCentered.contains(0.4, 0.501));
        Assert.assertFalse("unitSquareCentered contains(0.4, 0.5)",
                unitSquareCentered.contains(0.4, 0.5));
        Assert.assertTrue("unitSquareCentered contains(0.4, 0.499)",
                unitSquareCentered.contains(0.4, 0.499));

        Assert.assertTrue("unitSquareCentered contains(0.4, -0.5)",
                unitSquareCentered.contains(0.4, -0.5));

        Assert.assertTrue("unitSquareCentered contains(-0.5, -0.4)",
                unitSquareCentered.contains(-0.5, -0.4));
        Assert.assertTrue("unitSquareCentered contains(-0.5, 0.4)",
                unitSquareCentered.contains(-0.5, 0.4));

        // Things get odd when the x value of the point to be checked is the same as the vertical line.
        Assert.assertFalse("unitSquareCentered contains(0.501, 0.4)",
                unitSquareCentered.contains(0.501, 0.4));
        Assert.assertFalse("unitSquareCentered contains(0.5, 0.4)",
                unitSquareCentered.contains(0.5, 0.4));
        Assert.assertTrue("unitSquareCentered contains(0.499, 0.4)",
                unitSquareCentered.contains(0.499, 0.4));

        // Things get odd when the x value of the point to be checked is the same as the vertical line.
        Assert.assertFalse("unitSquareCentered contains(0.501, -0.4)",
                unitSquareCentered.contains(0.501, -0.4));
        Assert.assertFalse("unitSquareCentered contains(0.5, -0.4)",
                unitSquareCentered.contains(0.5, -0.4));
        Assert.assertTrue("unitSquareCentered contains(0.499, -0.4)",
                unitSquareCentered.contains(0.499, -0.4));

        Assert.assertFalse("unitSquareCentered contains(2.0, 2.0)",
                unitSquareCentered.contains(2.0, 2.0));
        Assert.assertFalse("unitSquareCentered contains(2.0, -2.0)",
                unitSquareCentered.contains(2.0, -2.0));
        Assert.assertFalse("unitSquareCentered contains(-2.0, -2.0)",
                unitSquareCentered.contains(-2.0, -2.0));
        Assert.assertFalse("unitSquareCentered contains(-2.0, 2.0)",
                unitSquareCentered.contains(-2.0, 2.0));
    }

    /** Corner case that fails in old implementation of contains(). */
    @org.junit.Test
    public void unitSquareCenteredContains1() {
        Assert.assertFalse("unitSquareCentered contains(-0.6, -0.5)",
                unitSquareCentered.contains(-0.6, -0.5));
    }

    /** Corner case that fails in old implementation of contains(). */
    @org.junit.Test
    public void unitSquareCenteredContains2() {
        Assert.assertFalse("unitSquareCentered contains(-0.6, 0.5)",
                unitSquareCentered.contains(-0.6, 0.5));
    }

    /** Test the diva.util.java2d.Polygon2D class
     *
     *  <p>To run, use:</p>
     *
     *  <pre>
     *   $PTII/bin/ptinvoke diva.util.java2d.Polygon2DJUnitTest
     *  </pre>
     *  or
     *  <pre>
     *   java -classpath $PTII:${PTII}/lib/junit-4.8.2.jar diva.util.java2d.test.junit.Polygon2DJUnitTest
     *  </pre>
     *  We use ptinvoke so that the classpath is set to include all the packages
     *  used by Ptolemy II.
     *
     *  @param args Ignored.
     */
    public static void main(String args[]) {
        org.junit.runner.JUnitCore
        .main("diva.util.java2d.test.junit.Polygon2DJUnitTest");
    }

    /** A 1.0 x 1.0 diamond centered on 0.0, 0.0. */
    public Polygon2D.Double unitDiamond;

    /** A 1.0 x 1.0 square with a corner at 0.0, 0.0. */
    public Polygon2D.Double unitSquare;

    /** A 1.0 x 1.0 square centered at 0.0, 0.0. */
    public Polygon2D.Double unitSquareCentered;
}
