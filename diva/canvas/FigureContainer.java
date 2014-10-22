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

import java.awt.geom.Rectangle2D;

import diva.util.Filter;

/** The FigureContainer interface is implemented by any
 * visible component that can contain figures. It includes
 * all of the methods in VisibleComponent and FigureSet,
 * and adds methods related to containment of a known
 * and finite set of figures.
 *
 * @version        $Id$
 * @author John Reekie
 * @Pt.AcceptedRating Yellow
 */
public interface FigureContainer extends FigureSet, VisibleComponent {
    /** Add a figure to this container. The figure should be added so
     * that it always displays above existing figures.
     */
    public void add(Figure f);

    /** Test if this container contains the given figure. As a general
     * rule, the implementation of this method is not required to be
     * efficient -- O(n) in the length of the list is acceptable.
     * Clients should note that, in general, a much better way
     * of making this same test is to check if the parent of the figure
     * is the same object as this container.
     */
    @Override
    public boolean contains(Figure f);

    /** Decorate a child figure, replacing the
     * child figure with the decorator.
     */
    public void decorate(Figure f, FigureDecorator d);

    /** Return the number of figures in this container.
     */
    public int getFigureCount();

    /** Given a rectangle, return the top-most descendent figure
     * that hits it. Otherwise, return null. Implementors
     * should not call their own hit() method, but only
     * those of their children.
     *
     * <P>Note that a region is given instead of a point so
     * that "pick halo" can be implemented. The region should
     * not have zero size, or no figure will be hit.
     */
    public Figure pick(Rectangle2D region);

    /** Given a rectangle, return the top-most descendent figure
     * that hits it, and is accepted by the given filter.
     * Otherwise, return null. Implementors
     * should not call their own hit() method, but only
     * those of their children.
     *
     * <P>Note that a region is given instead of a point so
     * that "pick halo" can be implemented. The region should
     * not have zero size, or no figure will be hit.
     */
    public Figure pick(Rectangle2D region, Filter f);

    /** Remove the given figure from this container.
     */
    public void remove(Figure f);

    /** Remove a figure from the given decorator and add
     * it back into this container.
     */
    public void undecorate(FigureDecorator d);
}
