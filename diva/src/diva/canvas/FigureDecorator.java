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
import java.util.Iterator;

import diva.util.NullIterator;
import diva.util.UnitIterator;

/** A FigureDecorator is a figure container that contains a single
 * child figure. The purpose of a FigureDecorator is to change or
 * affect the way in which the child is rendered, and so this
 * class behaves somewhat differently to other figures.
 *
 * <p> This class is a reasonable example of the Decorator design
 * pattern, hence its name.
 *
 * @version        $Id$
 * @author John Reekie
 * @Pt.AcceptedRating Red
 */
public abstract class FigureDecorator extends AbstractFigureContainer {
    /** The child
     */
    private Figure _child = null;

    /** Add a figure. This method does not make too much sense
     * for Decorators, but has to be here anyway. This method is
     * set same as calling setChild(f).
     */
    @Override
    public void add(Figure f) {
        setChild(f);
    }

    /** Test if the given figure is the one contained by this decorator.
     */
    @Override
    public boolean contains(Figure f) {
        return f == _child;
    }

    /** Return an iteration containing the one child.
     */
    @Override
    public Iterator figures() {
        if (_child == null) {
            return new NullIterator();
        } else {
            return new UnitIterator(_child);
        }
    }

    /** Return an iteration containing the one child.
     */
    @Override
    public Iterator figuresFromBack() {
        return figures();
    }

    /** Return an iteration containing the one child.
     */
    @Override
    public Iterator figuresFromFront() {
        return figures();
    }

    /** Get the bounds of this figure, which is by default the
     * same as the child figure, if there is one, or a very small
     * rectangle if there isn't.
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

    /** Get the container, which is defined as the lowest
     * ancestor that is not a FigureDecorator.
     */
    public FigureContainer getContainer() {
        if (getParent() instanceof FigureDecorator) {
            return ((FigureDecorator) getParent()).getContainer();
        } else {
            return (FigureContainer) getParent();
        }
    }

    /** Get the decorated figure, which is defined as the highest
     * descendent that is not a decorator.
     */
    public Figure getDecoratedFigure() {
        if (_child instanceof FigureDecorator) {
            return ((FigureDecorator) _child).getDecoratedFigure();
        } else {
            return _child;
        }
    }

    /** Return zero if there is no child, or one if there is.
     */
    @Override
    public int getFigureCount() {
        if (_child == null) {
            return 0;
        } else {
            return 1;
        }
    }

    /** Get the outline shape of this figure, which is by default the
     * same as the child figure, if there is one, or a very small
     * rectangle if there isn't.
     */
    @Override
    public Shape getShape() {
        if (_child == null) {
            return new Rectangle2D.Double();
        } else {
            return _child.getShape();
        }
    }

    /** Test if the child is hit.
     */
    @Override
    public boolean hit(Rectangle2D r) {
        return _child.hit(r);
    }

    /** Create a new instance of this figure decorator, modeled
     * on this one. This is used by interaction code that needs to
     * dynamically create new manipulators. The figure argument can
     * be used by this method to initialize the new instance; however,
     * the new instance must <i>not</i> be wrapped around the figure,
     * since that should be done by the caller.
     */
    public abstract FigureDecorator newInstance(Figure f);

    /** Paint the figure. By default, this method simply forwards the
     * paint request to the contained figure.
     */
    @Override
    public void paint(Graphics2D g) {
        if (_child != null) {
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
            _child.repaint();
        }
    }

    /** Remove a figure. This method does not make too much sense
     * for Decorators, but has to be here anyway. If the passed
     * figure is the same as the child figure, then this method
     * is the same as calling setChild(null). Otherwise, it does
     * nothing.
     */
    @Override
    public void remove(Figure f) {
        if (_child == f) {
            setChild(null);
        }
    }

    /** Replace the first figure, which must be a child, with the
     * second, which must not be a child.
     */
    @Override
    protected void replaceChild(Figure child, Figure replacement) {
        _child = replacement;
    }

    /** Transform the figure with the supplied transform. By default,
     * this method simply forwards the paint request to the child
     * figure.
     */
    @Override
    public void transform(AffineTransform at) {
        if (_child != null) {
            _child.transform(at);
        }
    }

    /** Translate the figure by the given distance.  By default, this
     * method simply forwards the paint request to the child figure.
     */
    @Override
    public void translate(double x, double y) {
        if (_child != null) {
            _child.translate(x, y);
        }
    }
}
