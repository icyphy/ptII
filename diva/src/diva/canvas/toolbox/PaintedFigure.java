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
 *
 */
package diva.canvas.toolbox;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import diva.canvas.AbstractFigure;
import diva.util.java2d.PaintedList;
import diva.util.java2d.PaintedObject;
import diva.util.java2d.ShapeUtilities;

/**
 * A PaintedFigure is contains an arbitrary set of PaintedObjects
 * in a list. This figure can be used for constructing more complex
 * figures than classes such as BasicFigure. It contains a transform
 * that is used for scaling the contained painted objects. (Note
 * that, if using this class to construct a figure from an external
 * source, the initial transform should operate on the coordinates
 * only.)
 *
 * @version        $Id$
 * @author         John Reekie
 * @author      Nick Zamora
 */
public class PaintedFigure extends AbstractFigure {
    // Note that this class was deprecated becase we were to use
    // diva.compat.canvas instead.  However, the Ptolemy sources
    // do not include diva.compat.canvas, so I'm making this class
    // undeprecated. -cxh 7/05

    /** The color compositing operator
     */
    private Composite _composite = AlphaComposite.SrcOver; // opaque

    /** The transform for the internals
     */
    private AffineTransform _transform = new AffineTransform();

    /** The list containing the objects that we paint
     */
    private PaintedList _paintedList;

    /** Create a new blank figure.
     */
    public PaintedFigure() {
        super();
        _paintedList = new PaintedList();
    }

    /** Create a new figure that paints itself using the given PaintedList.
     */
    public PaintedFigure(PaintedList objects) {
        super();
        _paintedList = objects;
    }

    /** Add a new painted object to the objects displayed
     * by this figure.
     */
    public void add(PaintedObject po) {
        _paintedList.add(po);
        repaint();
    }

    /** Get the painted list of painted objects of this figure.
     */
    public PaintedList getPaintedList() {
        return _paintedList;
    }

    /** Get the bounding box of this figure.
     */
    @Override
    public Rectangle2D getBounds() {
        Rectangle2D bounds = (Rectangle2D) ShapeUtilities.transformModify(
                _paintedList.getBounds(), _transform);
        return bounds;
    }

    /** Return the origin, which is the point relative to which all of the
     *  contained objects are drawn.
     *  @return The origin.
     */
    @Override
    public Point2D getOrigin() {
        return new Point2D.Double(_transform.getTranslateX(),
                _transform.getTranslateY());
    }

    /** Get the shape of this figure. This is the same as the
     * bounding box.
     */
    @Override
    public Shape getShape() {
        return getBounds();
    }

    /** Get the color composition operator of this figure.
     */
    public Composite getComposite() {
        return _composite;
    }

    /** Paint the figure.
     */
    @Override
    public void paint(Graphics2D g) {
        if (!isVisible()) {
            return;
        }

        if (_composite != null) {
            g.setComposite(_composite);
        }

        AffineTransform savedTransform = g.getTransform();
        g.transform(_transform);
        _paintedList.paint(g);
        g.setTransform(savedTransform);
    }

    /** Set the color composition operator of this figure. If the
     * composite is set to null, then the composite will not be
     * changed when the figure is painted. By default, the composite
     * is set to opaque.
     */
    public void setComposite(Composite c) {
        _composite = c;
        repaint();
    }

    /** Transform the figure with the supplied transform. This can be
     * used to perform arbitrary translation, scaling, shearing, and
     * rotation operations.
     */
    @Override
    public void transform(AffineTransform at) {
        repaint();
        _transform.preConcatenate(at);
        repaint();
    }
}
