/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
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
 * @version        $Revision$
 * @author John Reekie
 * @rating Yellow
 */
public abstract class FigureWrapper extends AbstractFigure {

    /** The child
     */
    private Figure _child = null;

    /** Construct a new figure with the given child figure.
     */
    public FigureWrapper (Figure f) {
        setChild(f);
    }

    /** Get the bounds of the child figure.
     */
    public Rectangle2D getBounds () {
        if (_child == null) {
            return new Rectangle2D.Double();
        } else {
            return _child.getBounds();
        }
    }

    /** Get the child figure, or null if there isn't one.
     */
    public Figure getChild () {
        return _child;
    }

    /** Get the outline shape of the child figure.
     */
    public Shape getShape () {
        if (_child == null) {
            return new Rectangle2D.Double();
        } else {
            return _child.getShape();
        }
    }

    /** Paint the child if this figure is visible.
     */
    public void paint (Graphics2D g) {
        if (_child != null && isVisible()) {
            _child.paint(g);
        }
    }

    /** Set the child figure. If there is already a child
     * figure, remove it from this container.
     */
    public void setChild (Figure f) {
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
    public void transform (AffineTransform at) {
        if (_child != null) {
            _child.transform(at);
        }
    }
}


