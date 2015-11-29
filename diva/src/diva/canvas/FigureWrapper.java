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
package diva.canvas;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/** A FigureWrapper is a figure container that contains
 * a single child figure. The purpose of a FigureWrapper is to provide
 * an abstract superclass for application-specific figures, that
 * need to implement certain behaviour but don't particularly
 * care about their appearance.
 *
 * @version        $Id$
 * @author John Reekie
 * @Pt.AcceptedRating Yellow
 */
public abstract class FigureWrapper extends AbstractFigure {
    /** The child
     */
    private Figure _child = null;

    /** Construct a new figure with the given child figure.
     */
    public FigureWrapper(Figure f) {
        setChild(f);
    }

    /** Get the bounds of the child figure.
     */
    @Override
    public Rectangle2D getBounds() {
        if (_child == null) {
            return new Rectangle2D.Double();
        } else {
            return _child.getBounds();
        }
    }

    /** Get the child figure, or null if there isn't one.
     */
    public Figure getChild() {
        return _child;
    }

    /** Get the outline shape of the child figure.
     */
    @Override
    public Shape getShape() {
        if (_child == null) {
            return new Rectangle2D.Double();
        } else {
            return _child.getShape();
        }
    }

    /** Paint the child if this figure is visible.
     */
    @Override
    public void paint(Graphics2D g) {
        if (_child != null && isVisible()) {
            _child.paint(g);
        }
    }

    /** Set the child figure. If there is already a child
     * figure, remove it from this container.
     */
    public void setChild(Figure f) {
        if (_child != null) {
            _child.repaint();
            _child.setParent(null);
        }

        _child = f;

        if (_child != null) {
            _child.setParent(this);

            //XXX            _child.repaint();
        }
    }

    /** Transform the child figure with the supplied transform.
     */
    @Override
    public void transform(AffineTransform at) {
        if (_child != null) {
            _child.transform(at);
        }
    }
}
