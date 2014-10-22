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

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.SwingConstants;

import diva.canvas.AbstractFigure;
import diva.canvas.CanvasUtilities;
import diva.canvas.Figure;

/** A LabelWrapper is a figure that attaches a label to some other figure.
 * The location at which the label is attached can (in this class) be
 * set to the center or to any of the four edges or corners of the
 * bounding box. (Maybe later we'll figure out a way to have the label
 * locate at a site.) In addition, the anchor and padding attributes
 * of the figure itself can be used to adjust the label location relative
 * to the anchoring point on the main figure.
 *
 * <P> Note that this class is intended for use in simple applications
 * where a simple label is attached to something. For more complex
 * applications, such as attaching multiple labels, you will need
 * to implement your own class.
 *
 * @version        $Id$
 * @author John Reekie
 */
public class LabelWrapper extends AbstractFigure {
    /** The child
     */
    private Figure _child = null;

    /** The label
     */
    private LabelFigure _label = null;

    /** The label anchor
     */
    private int _anchor = SwingConstants.CENTER;

    /** Construct a new figure with the given child figure and
     * the given string.
     */
    public LabelWrapper(Figure f, String label) {
        _child = f;
        f.setParent(this);

        _label = new LabelFigure(label);

        Point2D pt = CanvasUtilities.getLocation(_child.getBounds(), _anchor);
        _label.translateTo(pt);
    }

    /** Get the bounds of this figure.
     */
    @Override
    public Rectangle2D getBounds() {
        Rectangle2D bounds = _child.getBounds();
        Rectangle2D.union(bounds, _label.getBounds(), bounds);
        return bounds;
    }

    /** Get the child figure
     */
    public Figure getChild() {
        return _child;
    }

    /** Get the label. This can be used to adjust the label
     * appearance, anchor, and so on.
     */
    public LabelFigure getLabel() {
        return _label;
    }

    /** Get the shape of this figure. This is the shape
     * of the child figure only -- the label is not included
     * in the shape.
     */
    @Override
    public Shape getShape() {
        return _child.getShape();
    }

    /** We are hit if either the child or the figure is hit.
     */
    @Override
    public boolean hit(Rectangle2D r) {
        return _child.hit(r) || _label.hit(r);
    }

    /** Paint this figure
     */
    @Override
    public void paint(Graphics2D g) {
        if (_child != null && isVisible()) {
            _child.paint(g);
            _label.paint(g);
        }
    }

    /** Set the anchor of the label. The anchor is the position on
     * the child figure at which the label will be located.
     * It can be any of the positioning constants defined
     * in SwingConstants.
     */
    public void setAnchor(int anchor) {
        this._anchor = anchor;

        Point2D pt = CanvasUtilities.getLocation(_child.getBounds(), anchor);
        repaint();
        _label.translateTo(pt);
        repaint();
    }

    /** Transform the figure with the supplied transform.
     */
    @Override
    public void transform(AffineTransform at) {
        repaint();
        _child.transform(at);

        Point2D pt = CanvasUtilities.getLocation(_child.getBounds(), _anchor);
        _label.translateTo(pt);
        repaint();
    }
}
