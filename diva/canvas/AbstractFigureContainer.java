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
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import diva.util.Filter;

/** AbstractFigureContainer is an abstract class that roots the tree
 * of figure-containing classes.
 *
 * @version $Id$
 * @author John Reekie
 * @Pt.AcceptedRating Yellow
 */
public abstract class AbstractFigureContainer extends AbstractFigure implements
FigureContainer {
    /** Decorate a child figure, replacing the reference to the
     * child figure with the decorator.
     */
    @Override
    public void decorate(Figure child, FigureDecorator decorator) {
        if (child.getParent() != this) {
            throw new IllegalArgumentException("The object " + child
                    + " is not a child of " + this);
        }

        child.repaint();
        decorator.setParent(this);
        decorator.setChild(child);
        replaceChild(child, decorator);
        decorator.repaint();
    }

    /** Test if the given figure is a child of this composite.
     */
    @Override
    public abstract boolean contains(Figure f);

    /** Return an iteration of the children, in an undefined order.
     */
    @Override
    public abstract Iterator figures();

    /** Return an iteration of the children, from
     * back to front. This is the order
     * in which the children are painted.
     */
    @Override
    public abstract Iterator figuresFromBack();

    /** Return an iteration of the children, from
     * front to back. This is the order in which
     * events are intercepted.
     */
    @Override
    public abstract Iterator figuresFromFront();

    /** Return the number of child figures in this container.
     */
    @Override
    public abstract int getFigureCount();

    /** Paint this composite figure onto a 2D graphics object. If the layer
     * is not visible, return immediately. Otherwise paint all children
     * from back to front.
     */
    @Override
    public void paint(Graphics2D g) {
        if (!isVisible()) {
            return;
        }

        Figure f;
        Iterator i = figuresFromBack();

        while (i.hasNext()) {
            f = (Figure) i.next();
            f.paint(g);
        }
    }

    /** Given a rectangle, return the top-most descendent figure
     * that it hits. If none does, return null.
     */
    @Override
    public Figure pick(Rectangle2D region) {
        return CanvasUtilities.pick(figuresFromFront(), region);
    }

    /** Given a rectangle, return the top-most descendent figure
     * that it hits that is accepted by the given filter.
     * If none does, return null.
     */
    @Override
    public Figure pick(Rectangle2D region, Filter filter) {
        return CanvasUtilities.pick(figuresFromFront(), region, filter);
    }

    /** Accept notification that a repaint has occurred somewhere
     * in the hierarchy below this container. This default implementation
     * simply forwards the notification to its parent.
     */
    @Override
    public void repaint(DamageRegion d) {
        if (getParent() != null) {
            getParent().repaint(d);
        }
    }

    /** Replace the first figure with the second. This is a hook
     * method for the decorate() and undecorate() methods, and should
     * not be called by other methods. Implementors can assume that
     * the first figure is a child of this container, and that the
     * second is not.
     * @param child The figure to be replaced.
     * @param replacement The replacement figure.
     */
    protected abstract void replaceChild(Figure child, Figure replacement);

    /** Transform this figure with the supplied transform.
     * This default implementation simply forwards the transform
     * call to each child.
     */
    @Override
    public void transform(AffineTransform at) {
        repaint();

        Iterator i = figures();

        while (i.hasNext()) {
            Figure f = (Figure) i.next();
            f.transform(at);
        }

        repaint();
    }

    /** Translate this figure by the given distance.
     * This default implementation simply forwards the translate
     * call to each child.
     */
    @Override
    public void translate(double x, double y) {
        repaint();

        Iterator i = figures();

        while (i.hasNext()) {
            Figure f = (Figure) i.next();
            f.translate(x, y);
        }

        repaint();
    }

    /** Remove a figure from the given decorator and add
     * it back into this container.
     */
    @Override
    public void undecorate(FigureDecorator decorator) {
        if (decorator.getParent() != this) {
            throw new IllegalArgumentException("The object " + decorator
                    + " is not a child of " + this);
        }

        decorator.repaint();

        Figure child = decorator.getChild();
        replaceChild(decorator, child);
        decorator.setChild(null);
        decorator.setParent(null);
        child.setParent(this); // This is needed
        repaint();
    }
}
