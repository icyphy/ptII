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

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import diva.canvas.Figure;
import diva.canvas.interactor.DragInteractor;
import diva.canvas.interactor.Interactor;
import diva.util.java2d.ShapeUtilities;
import diva.util.jester.TestCase;
import diva.util.jester.TestFailedException;
import diva.util.jester.TestHarness;
import diva.util.jester.TestSuite;
import diva.util.jester.TestUtilities;

/**
 * A test suite for Figure. Since Figure is an interface, this class
 * has no main() method. It defines a factory interface that
 * concrete factories must implement.
 *
 * @author John Reekie
 * @version $Id$
 */
public class FigureTest extends TestSuite {
    /** The figure factory interface
     */
    public interface FigureFactory {
        public Figure createFigure();
    }

    /**
     * The unit factory
     */
    private FigureFactory factory;

    /** Constructor
     */
    public FigureTest(TestHarness harness, FigureFactory factory) {
        setTestHarness(harness);
        setFactory(factory);
        this.factory = factory;
    }

    /**
     * runSuite()
     */
    @Override
    public void runSuite() {
        testHit();
        testIntersects();
        testPaint();
        testProperties();
        testTranslate();
        testTransform();
    }

    ///////////////////////////////////////////////////////////////////
    //// Test methods

    /** Perform the simple set/get tests.
     */
    public void testProperties() {
        runTestCase(new TestCase("Figure properties") {
            Figure figure = null;

            Interactor r = new DragInteractor();

            @Override
            public void init() throws Exception {
                figure = factory.createFigure();
            }

            @Override
            public void run() throws Exception {
                figure.setInteractor(r);
                figure.setVisible(false);
            }

            @Override
            public void check() throws TestFailedException {
                assertExpr(!figure.isVisible(), "Property visible");
                assertExpr(figure.getInteractor() == r,
                        "Property interactionRole");
            }
        });
    }

    /** Test hit. This doesn't actually do a hit test,
     * but it does check that the passed rectangle is not modified.
     */
    public void testHit() {
        runTestCase(new RegionTestCase("Figure hit") {
            @Override
            public void run() throws Exception {
                /*result =*/figure.hit(region);
            }
        });
    }

    /** Test intersection. This doesn't actually do an intersection test,
     * but it does check that the passed rectangle is not modified.
     */
    public void testIntersects() {
        runTestCase(new RegionTestCase("Figure intersects") {
            @Override
            public void run() throws Exception {
                /*result = */figure.intersects(region);
            }
        });
    }

    /** Test painting. This method calls both versions of the paint
     * method. It doesn't actually test what the paint method does.
     * In either case, it verifies that the transform context of the
     * Graphics2D is not changed.  In the case of the paint method
     * that takes a region, it also verifies that the region is not
     * changed by the call.  Note that we transform the figure first
     * as this is sometimes needed to make this bug show up.
     */
    public void testPaint() {
        final BufferedImage buffer = new BufferedImage(100, 100,
                BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = buffer.createGraphics();

        runTestCase(new TestCase("Figure paint") {
            Figure figure = null;

            AffineTransform at1 = null;

            AffineTransform at2 = null;

            AffineTransform at3 = null;

            Rectangle2D region = new Rectangle2D.Double(10, 20, 30, 40);

            @Override
            public void init() throws Exception {
                figure = factory.createFigure();

                AffineTransform at = new AffineTransform();
                at.translate(10, 20);
                at.scale(0.5, 2.0);
                figure.transform(at);
            }

            @Override
            public void run() throws Exception {
                at1 = new AffineTransform(g.getTransform());
                figure.paint(g);
                at2 = new AffineTransform(g.getTransform());
                figure.paint(g, region);
                at3 = new AffineTransform(g.getTransform());
            }

            @Override
            public void check() throws TestFailedException {
                assertExpr(at1.equals(at2),
                        "Graphics2D transform changed from:\n    " + at1
                                + " \nto:\n    " + at2);
                assertExpr(at2.equals(at3),
                        "Graphics2D transform changed from:\n    " + at2
                                + " \nto:\n    " + at3);
            }
        });

        runTestCase(new RegionTestCase("Figure paint region test") {
            @Override
            public void run() throws Exception {
                figure.paint(g, region);
            }
        });
    }

    /** Test how transforms affect the figure
     */
    public void testTransform() {
        runTestCase(new TestCase("Figure transform") {
            Figure figure = null;

            AffineTransform at = null;

            Shape shape = null;

            Rectangle2D bounds = null;

            @Override
            public void init() throws Exception {
                figure = factory.createFigure();
                at = new AffineTransform();
                at.translate(40, -20);
                at.scale(2.0, 0.5);
            }

            @Override
            public void run() throws Exception {
                shape = figure.getShape();
                shape = ShapeUtilities.transformModify(shape, at);
                bounds = figure.getBounds();
                bounds = (Rectangle2D) bounds.clone();
                ShapeUtilities.transformModify(bounds, at);

                figure.transform(at);
            }

            @Override
            public void check() throws TestFailedException {
                assertExpr(TestUtilities.shapeEquals(shape, figure.getShape(),
                        0.01), "Shape not transformed: " + shape + " != "
                        + figure.getShape());

                // For the bounds, we need to allow a large error,
                // because bounds don't necessarily transform correctly!
                // So this test is only useful for catching the most
                // gross errors
                assertExpr(TestUtilities.shapeEquals(bounds,
                        figure.getBounds(), 2.0), "Bounds not transformed: "
                        + bounds + " != " + figure.getBounds());
            }
        });
    }

    /** Test how translates affect the figure
     */
    public void testTranslate() {
        runTestCase(new TestCase("Figure translate") {
            Figure figure = null;

            Shape shape = null;

            Rectangle2D bounds = null;

            @Override
            public void init() throws Exception {
                figure = factory.createFigure();
            }

            @Override
            public void run() throws Exception {
                shape = figure.getShape();
                shape = new GeneralPath(shape);
                shape = ShapeUtilities.translateModify(shape, 10.0, -20.0);
                bounds = figure.getBounds();
                bounds = (Rectangle2D) bounds.clone();
                bounds = (Rectangle2D) ShapeUtilities.translateModify(bounds,
                        10.0, -20.0);

                figure.translate(10.0, -20.0);
            }

            @Override
            public void check() throws TestFailedException {
                assertExpr(TestUtilities.shapeEquals(shape, figure.getShape(),
                        0.01), "Shape not translated: " + shape + " != "
                        + figure.getShape());

                assertExpr(TestUtilities.shapeEquals(bounds,
                        figure.getBounds(), 0.01), "Bounds not translated: "
                        + bounds + " != " + figure.getBounds());
            }
        });
    }

    ///////////////////////////////////////////////////////////////////
    //// Inner classes

    /** Region testing test case. This test case can be used for
     * methods that take a region argument, to verify that they don't
     * change that region. The run method must be overridden.
     */
    public abstract class RegionTestCase extends TestCase {
        Figure figure = null;

        //boolean result;

        Rectangle2D region = null;

        Rectangle2D copy = null;

        public RegionTestCase(String str) {
            super(str);
        }

        @Override
        public void init() throws Exception {
            figure = factory.createFigure();

            AffineTransform at = new AffineTransform();
            at.translate(10, 20);
            at.scale(0.5, 2.0);
            figure.transform(at);

            region = (Rectangle2D) figure.getBounds().clone();
            copy = (Rectangle2D) region.clone();
        }

        // public void run () throws Exception {
        //     result = figure.intersects(region);
        //}
        @Override
        public void check() throws TestFailedException {
            assertExpr(TestUtilities.shapeEquals(region, copy, 0.01),
                    "The region was changed from:\n    " + copy
                            + " \nto:\n    " + region);
        }
    }
}
