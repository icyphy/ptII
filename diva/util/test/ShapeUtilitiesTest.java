/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.util.test;

import diva.util.jester.*;
import diva.util.java2d.*;

import java.awt.*;
import java.awt.geom.*;

/**
 * A test suite for ShapeUtilities.
 *
 * @author John Reekie (johnr@eecs.berkeley.edu)
 * @version $Revision$
 */
public class ShapeUtilitiesTest extends TestSuite {

    /** Constructor
     */
    public ShapeUtilitiesTest (TestHarness harness) {
        setTestHarness(harness);
    }

    /**
     * runSuite()
     */
    public void runSuite () {
        testTransformBounds();
        testTransformModify();
        testTransformModifyRect();
        testTranslateModify();
    }

    ///////////////////////////////////////////////////////////////////
    //// Test methods

    /** Test bounds transformation
     */
    public void testTransformBounds () {
        runTestCase(new TestCase("TransformBounds") {
                Rectangle2D dr = new Rectangle2D.Double(10,20,30,40);
                Rectangle2D dr1 = (Rectangle2D) dr.clone();
                Rectangle2D dr2 = (Rectangle2D) dr.clone();
                Rectangle2D fr = new Rectangle2D.Float(60,20,30,40);
                Rectangle2D fr1 = (Rectangle2D) fr.clone();
                Rectangle2D fr2 = (Rectangle2D) fr.clone();
                AffineTransform at1, at2;
                Shape ds1, ds2, fs1, fs2;

                public void init () throws Exception {
                    at1 = new AffineTransform();     // This one is orthogonal
                    at1.translate(40,-20);
                    at1.scale(2.0,0.5);
                    at2 = new AffineTransform(at1);  // This one is not orthogonal
                    at2.rotate(-1.0);
                }
                public void run () throws Exception {
                    ds1 = at1.createTransformedShape(dr1);
                    ds2 = at2.createTransformedShape(dr2).getBounds2D();
                    fs1 = at1.createTransformedShape(fr1);
                    fs2 = at2.createTransformedShape(fr2).getBounds2D();
                    dr1 = ShapeUtilities.transformBounds(dr1,at1);
                    dr2 = ShapeUtilities.transformBounds(dr2,at2);
                    fr1 = ShapeUtilities.transformBounds(fr1,at1);
                    fr2 = ShapeUtilities.transformBounds(fr2,at2);
                }
                public void check () throws TestFailedException {
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
    public void testTransformModify () {
        runTestCase(new TestCase("TransformModify") {
                int n = 6;
                Shape shapes[] = new Shape[n];
                Shape modified[] = new Shape[n];
                Shape xforms[] = new Shape[n];
                AffineTransform at;

                public void init () throws Exception {
                    shapes[0] = new Rectangle2D.Double(10,20,30,40);
                    shapes[1] = new Ellipse2D.Double(10,20,30,40);
                    shapes[2] = new Area(ShapeUtilities.createSwatchShape());

                    Polyline2D polyline = new Polyline2D.Double(10,20,30,40);
                    polyline.lineTo(50,60);
                    polyline.lineTo(50,60);

                    Polygon2D polygon = new Polygon2D.Double(10,20);
                    polygon.lineTo(20,30);
                    polygon.lineTo(40,50);
                    polygon.lineTo(60,70);

                    shapes[3] = polyline;
                    shapes[4] = polygon;
                    shapes[5] = new GeneralPath(ShapeUtilities.createSwatchShape());

                    at = new AffineTransform();
                    at.translate(40,-20);
                    at.scale(2.0,0.5);
                    at.rotate(1.0); // make this a general transform
                }

                public void run () throws Exception {
                    for (int i = 0; i < n; i++) {
                        xforms[i] = at.createTransformedShape(shapes[i]);
                        modified[i] = ShapeUtilities.transformModify(shapes[i], at);
                    }
                }
                public void check () throws TestFailedException {
                    for (int i = 0; i < n; i++) {
                        assertExpr(TestUtilities.shapeEquals(modified[i], xforms[i], 0.01),
                                "Shape not transformed: " + modified[i] + " != " + xforms[i]);
                        if (i < 3) {
                            assertExpr(shapes[i] != modified[i],
                                    "Shapes must not be identical: "
                                    + shapes[i] + " != " + modified[i]);
                        } else {
                            assertExpr(shapes[i] == modified[i],
                                    "Shapes must be identical: "
                                    + shapes[i] + " != " + modified[i]);
                        }
                    }
                }
            });
    }

    /** Test rectangle transformation
     */
    public void testTransformModifyRect () {
        runTestCase(new TestCase("TransformModifyRect") {
                Rectangle2D dr = new Rectangle2D.Double(10,20,30,40);
                Rectangle2D fr = new Rectangle2D.Float(10,20,30,40);
                Ellipse2D er = new Ellipse2D.Double(10,20,30,40);
                AffineTransform at;
                Shape fs, ds, es;

                public void init () throws Exception {
                    at = new AffineTransform();
                    at.translate(40,-20);
                    at.scale(2.0,0.5);
                }
                public void run () throws Exception {
                    ds = at.createTransformedShape(dr);
                    fs = at.createTransformedShape(fr);
                    es = at.createTransformedShape(er);
                    ShapeUtilities.transformModifyRect(dr,at);
                    ShapeUtilities.transformModifyRect(fr,at);
                    ShapeUtilities.transformModifyRect(er,at);
                }
                public void check () throws TestFailedException {
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
    public void testTranslateModify () {
        runTestCase(new TestCase("TranslateModify") {
                int n = 6;
                Shape shapes[] = new Shape[n];
                Shape modified[] = new Shape[n];
                Shape xforms[] = new Shape[n];
                double x = 10.5;
                double y = -4.2;
                AffineTransform at = AffineTransform.getTranslateInstance(x,y);

                public void init () throws Exception {
                    shapes[0] = new Area(ShapeUtilities.createSwatchShape());
                    shapes[1] = new Rectangle2D.Double(10,20,30,40);
                    shapes[2] = new Ellipse2D.Double(10,20,30,40);

                    Polyline2D polyline = new Polyline2D.Double(10,20,30,40);
                    polyline.lineTo(50,60);
                    polyline.lineTo(50,60);

                    Polygon2D polygon = new Polygon2D.Double(10,20);
                    polygon.lineTo(20,30);
                    polygon.lineTo(40,50);
                    polygon.lineTo(60,70);

                    shapes[3] = polyline;
                    shapes[4] = polygon;
                    shapes[5] = new GeneralPath(ShapeUtilities.createSwatchShape());
                }

                public void run () throws Exception {
                    for (int i = 0; i < n; i++) {
                        xforms[i] = at.createTransformedShape(shapes[i]);
                        modified[i] = ShapeUtilities.translateModify(shapes[i], x, y);
                    }
                }
                public void check () throws TestFailedException {
                    for (int i = 0; i < n; i++) {
                        assertExpr(TestUtilities.shapeEquals(modified[i], xforms[i], 0.01),
                                "Shape not translated: " + modified[i] + " != " + xforms[i]);
                        if (i < 1) {
                            assertExpr(shapes[i] != modified[i],
                                    "Shapes must not be identical: "
                                    + shapes[i] + " != " + modified[i]);
                        } else {
                            assertExpr(shapes[i] == modified[i],
                                    "Shapes must be identical: "
                                    + shapes[i] + " != " + modified[i]);
                        }
                    }
                }
            });
    }

    ////////////////////////////////////////////////////////////
    ////  main

    /** Create a default test harness and
     * run all tests on it.
     */
    public static void main (String argv[]) {
        new ShapeUtilitiesTest(new TestHarness()).run();
    }
}


