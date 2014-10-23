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
package diva.util.test;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import diva.util.java2d.Polygon2D;
import diva.util.java2d.Polyline2D;
import diva.util.java2d.ShapeUtilities;
import diva.util.jester.TestCase;
import diva.util.jester.TestFailedException;
import diva.util.jester.TestHarness;
import diva.util.jester.TestSuite;
import diva.util.jester.TestUtilities;

/**
 * A test suite for ShapeUtilities.
 *
 * @author John Reekie
 * @version $Id$
 */
public class ShapeUtilitiesTest extends TestSuite {
    /** Constructor
     */
    public ShapeUtilitiesTest(TestHarness harness) {
        setTestHarness(harness);
    }

    /**
     * runSuite()
     */
    @Override
    public void runSuite() {
        testTransformBounds();
        testTransformModify();
        testTransformModifyRect();
        testTranslateModify();
    }

    ///////////////////////////////////////////////////////////////////
    //// Test methods

    /** Test bounds transformation
     */
    public void testTransformBounds() {
        runTestCase(new TestCase("TransformBounds") {
            Rectangle2D dr = new Rectangle2D.Double(10, 20, 30, 40);

            Rectangle2D dr1 = (Rectangle2D) dr.clone();

            Rectangle2D dr2 = (Rectangle2D) dr.clone();

            Rectangle2D fr = new Rectangle2D.Float(60, 20, 30, 40);

            Rectangle2D fr1 = (Rectangle2D) fr.clone();

            Rectangle2D fr2 = (Rectangle2D) fr.clone();

            AffineTransform at1;

            AffineTransform at2;

            Shape ds1;

            Shape ds2;

            Shape fs1;

            Shape fs2;

            @Override
            public void init() throws Exception {
                at1 = new AffineTransform(); // This one is orthogonal
                at1.translate(40, -20);
                at1.scale(2.0, 0.5);
                at2 = new AffineTransform(at1); // This one is not orthogonal
                at2.rotate(-1.0);
            }

            @Override
            public void run() throws Exception {
                ds1 = at1.createTransformedShape(dr1);
                ds2 = at2.createTransformedShape(dr2).getBounds2D();
                fs1 = at1.createTransformedShape(fr1);
                fs2 = at2.createTransformedShape(fr2).getBounds2D();
                dr1 = ShapeUtilities.transformBounds(dr1, at1);
                dr2 = ShapeUtilities.transformBounds(dr2, at2);
                fr1 = ShapeUtilities.transformBounds(fr1, at1);
                fr2 = ShapeUtilities.transformBounds(fr2, at2);
            }

            @Override
            public void check() throws TestFailedException {
                assertExpr(TestUtilities.shapeEquals(ds1, dr1, 0.01),
                        "Bounds not transformed: " + ds1 + " != " + dr1);
                assertExpr(TestUtilities.shapeEquals(ds2, dr2, 0.01),
                        "Bounds not transformed: " + ds2 + " != " + dr2);
                assertExpr(TestUtilities.shapeEquals(fs1, fr1, 0.01),
                        "Bounds not transformed: " + fs1 + " != " + fr1);
                assertExpr(TestUtilities.shapeEquals(fs2, fr2, 0.01),
                        "Bounds not transformed: " + fs2 + " != " + fr2);
            }
        });
    }

    /** Test general transformation
     */
    public void testTransformModify() {
        runTestCase(new TestCase("TransformModify") {
            int n = 6;

            Shape[] shapes = new Shape[n];

            Shape[] modified = new Shape[n];

            Shape[] xforms = new Shape[n];

            AffineTransform at;

            @Override
            public void init() throws Exception {
                shapes[0] = new Rectangle2D.Double(10, 20, 30, 40);
                shapes[1] = new Ellipse2D.Double(10, 20, 30, 40);
                shapes[2] = new Area(ShapeUtilities.createSwatchShape());

                Polyline2D polyline = new Polyline2D.Double(10, 20, 30, 40);
                polyline.lineTo(50, 60);
                polyline.lineTo(50, 60);

                Polygon2D polygon = new Polygon2D.Double(10, 20);
                polygon.lineTo(20, 30);
                polygon.lineTo(40, 50);
                polygon.lineTo(60, 70);

                shapes[3] = polyline;
                shapes[4] = polygon;
                shapes[5] = new GeneralPath(ShapeUtilities.createSwatchShape());

                at = new AffineTransform();
                at.translate(40, -20);
                at.scale(2.0, 0.5);
                at.rotate(1.0); // make this a general transform
            }

            @Override
            public void run() throws Exception {
                for (int i = 0; i < n; i++) {
                    xforms[i] = at.createTransformedShape(shapes[i]);
                    modified[i] = ShapeUtilities.transformModify(shapes[i], at);
                }
            }

            @Override
            public void check() throws TestFailedException {
                for (int i = 0; i < n; i++) {
                    assertExpr(TestUtilities.shapeEquals(modified[i],
                            xforms[i], 0.01), "Shape not transformed: "
                                    + modified[i] + " != " + xforms[i]);

                    if (i < 3) {
                        assertExpr(shapes[i] != modified[i],
                                "Shapes must not be identical: " + shapes[i]
                                        + " != " + modified[i]);
                    } else {
                        assertExpr(shapes[i] == modified[i],
                                "Shapes must be identical: " + shapes[i]
                                        + " != " + modified[i]);
                    }
                }
            }
        });
    }

    /** Test rectangle transformation
     */
    public void testTransformModifyRect() {
        runTestCase(new TestCase("TransformModifyRect") {
            Rectangle2D dr = new Rectangle2D.Double(10, 20, 30, 40);

            Rectangle2D fr = new Rectangle2D.Float(10, 20, 30, 40);

            Ellipse2D er = new Ellipse2D.Double(10, 20, 30, 40);

            AffineTransform at;

            Shape fs;

            Shape ds;

            Shape es;

            @Override
            public void init() throws Exception {
                at = new AffineTransform();
                at.translate(40, -20);
                at.scale(2.0, 0.5);
            }

            @Override
            public void run() throws Exception {
                ds = at.createTransformedShape(dr);
                fs = at.createTransformedShape(fr);
                es = at.createTransformedShape(er);
                ShapeUtilities.transformModifyRect(dr, at);
                ShapeUtilities.transformModifyRect(fr, at);
                ShapeUtilities.transformModifyRect(er, at);
            }

            @Override
            public void check() throws TestFailedException {
                assertExpr(TestUtilities.shapeEquals(ds, dr, 0.01),
                        "Rectangle not transformed: " + dr + " != " + ds);
                assertExpr(TestUtilities.shapeEquals(fs, fr, 0.01),
                        "Rectangle not transformed: " + fr + " != " + fs);
                assertExpr(TestUtilities.shapeEquals(es, er, 0.01),
                        "Ellipse not transformed: " + er + " != " + es);
            }
        });
    }

    /** Test translation
     */
    public void testTranslateModify() {
        runTestCase(new TestCase("TranslateModify") {
            int n = 6;

            Shape[] shapes = new Shape[n];

            Shape[] modified = new Shape[n];

            Shape[] xforms = new Shape[n];

            double x = 10.5;

            double y = -4.2;

            AffineTransform at = AffineTransform.getTranslateInstance(x, y);

            @Override
            public void init() throws Exception {
                shapes[0] = new Area(ShapeUtilities.createSwatchShape());
                shapes[1] = new Rectangle2D.Double(10, 20, 30, 40);
                shapes[2] = new Ellipse2D.Double(10, 20, 30, 40);

                Polyline2D polyline = new Polyline2D.Double(10, 20, 30, 40);
                polyline.lineTo(50, 60);
                polyline.lineTo(50, 60);

                Polygon2D polygon = new Polygon2D.Double(10, 20);
                polygon.lineTo(20, 30);
                polygon.lineTo(40, 50);
                polygon.lineTo(60, 70);

                shapes[3] = polyline;
                shapes[4] = polygon;
                shapes[5] = new GeneralPath(ShapeUtilities.createSwatchShape());
            }

            @Override
            public void run() throws Exception {
                for (int i = 0; i < n; i++) {
                    xforms[i] = at.createTransformedShape(shapes[i]);
                    modified[i] = ShapeUtilities.translateModify(shapes[i], x,
                            y);
                }
            }

            @Override
            public void check() throws TestFailedException {
                for (int i = 0; i < n; i++) {
                    assertExpr(TestUtilities.shapeEquals(modified[i],
                            xforms[i], 0.01), "Shape not translated: "
                                    + modified[i] + " != " + xforms[i]);

                    if (i < 1) {
                        assertExpr(shapes[i] != modified[i],
                                "Shapes must not be identical: " + shapes[i]
                                        + " != " + modified[i]);
                    } else {
                        assertExpr(shapes[i] == modified[i],
                                "Shapes must be identical: " + shapes[i]
                                        + " != " + modified[i]);
                    }
                }
            }
        });
    }

    ///////////////////////////////////////////////////////////////////
    ////  main

    /** Create a default test harness and
     * run all tests on it.
     */
    public static void main(String[] argv) {
        new ShapeUtilitiesTest(new TestHarness()).run();
    }
}
