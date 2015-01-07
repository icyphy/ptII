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

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/** A basic implementation of the figure z-list, provided for
 * initial implementations of figure containers. This implementation
 * uses <b>java.util.ArrayList</b> internally. In the internal
 * implementation, the order of indexes is reversed, so that low-index
 * elements in the external interface are high-index elements in
 * the internal ArrayList. This is done on grounds of "performance":
 * the redraw iterator is more important than the event-handling
 * iterator.
 *
 * @version        $Id$
 * @author John Reekie
 * @Pt.AcceptedRating Yellow
 */
public class BasicZList implements ZList {
    /* The list of elements.
     */
    private ArrayList _elements = new ArrayList();

    /** Add a figure to the container.  See the implemented method for
     * a detailed description.
     */
    @Override
    public void add(Figure f) {
        _elements.add(f);
    }

    /** Insert a figure at the given position.  See the implemented
     * method for a detailed description.
     */
    @Override
    public void add(int index, Figure f) {
        _elements.add(_elements.size() - index, f);
    }

    /** Removes all of the figures from this list.
     */
    @Override
    public void clear() {
        _elements.clear();
    }

    /** Get the bounding box of all the figures in this list.
     */
    @Override
    public Rectangle2D getBounds() {
        return CanvasUtilities.computeCompositeBounds(figures());
    }

    /** Test if the z-list contains the given figure.  See the
     * implemented method for a detailed description.
     *
     * <p>Clients should note that, in general, a much better way of
     * making this same test is to check if the parent of the figure
     * is the same object as this container.
     */
    @Override
    public boolean contains(Figure f) {
        return _elements.contains(f);
    }

    /** Return an iteration of the figures in this container. In this
     * particular implementation, the figures are in redraw order -- that
     * is, from front to back. Clients must not rely on this, though,
     * and use figuresFromBack() if they require that order.
     */
    @Override
    public Iterator figures() {
        return _elements.iterator();
    }

    /** Return an iteration of the figures in this container, from
     * highest index to lowest index. This is the order in which
     * figures should normally be painted, so that figures at lower
     * indexes are painted over the top of figures at higher indexes.
     */
    @Override
    public Iterator figuresFromBack() {
        return _elements.iterator();
    }

    /** Return an iteration of the figures in this container, from
     * lowest index to highest index. This is the order in which
     * events should normally be intercepted.
     */
    @Override
    public Iterator figuresFromFront() {
        return new Iterator() {
            int cursor = _elements.size();

            @Override
            public boolean hasNext() {
                return cursor > 0;
            }

            @Override
            public Object next() throws NoSuchElementException {
                if (cursor <= 0) {
                    throw new NoSuchElementException("Can't get " + cursor
                            + "'th element from BasicZList of size "
                            + _elements.size());
                }
                cursor--;
                return _elements.get(cursor);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException(
                        "Cannot delete figure from zlist");
            }
        };
    }

    /** Get the figure at the given index.
     */
    @Override
    public Figure get(int index) {
        return (Figure) _elements.get(_elements.size() - 1 - index);
    }

    /** Return the number of figures in this list.
     */
    @Override
    public int getFigureCount() {
        return _elements.size();
    }

    /** Get the figures that are entirely contained by the given
     * region.
     */
    @Override
    public GeometricSet getContainedFigures(Rectangle2D region) {
        return new EnclosedGSet(region);
    }

    /** Get the figures with bounding boxes that intersect the given
     * region. Note that the returned set may contained figures
     * that do not intersect the region -- this method only
     * looks at the bounding boxes.
     */
    @Override
    public GeometricSet getIntersectedFigures(Rectangle2D region) {
        return new IntersectedGSet(region);
    }

    /** Return the index of the given figure in the Z-list.
     * See the implemented method for a detailed description.
     */
    @Override
    public int indexOf(Figure f) {
        int i = _elements.indexOf(f);

        if (i == -1) {
            return -1;
        } else {
            return _elements.size() - 1 - i;
        }
    }

    /** Remove the given figure from this container.
     * See the implemented method for a detailed description.
     */
    @Override
    public void remove(Figure f) {
        _elements.remove(f);
    }

    /** Remove the figure at the given position in the list.
     * See the implemented method for a detailed description.
     */
    @Override
    public void remove(int index) {
        _elements.remove(_elements.size() - 1 - index);
    }

    /** Replace the figure at the given index with the passed-in
     * figure.
     */
    @Override
    public void set(int index, Figure f) {
        _elements.set(_elements.size() - 1 - index, f);
    }

    /** Set the index of the given figure.
     * See the implemented method for a detailed description.
     */
    @Override
    public void setIndex(int index, Figure f) {
        _elements.remove(f);
        _elements.add(_elements.size() - index, f);
    }

    ///////////////////////////////////////////////////////////////////
    //// Inner classes

    /** The abstract class of Geometric sets on a zlist. Note
     * that if a figure covered by a geometric set is decorated,
     * the set will contain the decorator, not the figure directly.
     */
    private abstract class GSet implements GeometricSet {
        private Rectangle2D _region;

        private ArrayList _currentFigures;

        /** Create a new set
         */
        public GSet(Rectangle2D region) {
            _currentFigures = new ArrayList();
            setGeometry(region);
        }

        /** Test if the given figure is within the bounds of this
         * region.
         */
        @Override
        public boolean contains(Figure f) {
            return _contains(f, _region);
        }

        /** Return the figures in undefined order.
         */
        @Override
        public Iterator figures() {
            return _currentFigures.iterator();
        }

        /** Return the figures from highest index to lowest index.
         */
        @Override
        public Iterator figuresFromBack() {
            return _currentFigures.iterator();
        }

        /** Return the figures from lowest index to highest index.
         */
        @Override
        public Iterator figuresFromFront() {
            return new Iterator() {
                int cursor = _currentFigures.size();

                @Override
                public boolean hasNext() {
                    return cursor > 0;
                }

                @Override
                public Object next() throws NoSuchElementException {
                    if (cursor <= 0) {
                        throw new NoSuchElementException("Can't get " + cursor
                                + "'th element from BasicZList of size "
                                + _currentFigures.size());
                    }
                    cursor--;
                    return _currentFigures.get(cursor);
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException(
                            "Cannot delete figure from geometric set");
                }
            };
        }

        /** Get the geometry.
         */
        @Override
        public Shape getGeometry() {
            return _region;
        }

        /** Set the geometry. All previously-obtained iterators
         * are invalid and must be discarded.
         */
        @Override
        public void setGeometry(Shape geometry) {
            _region = (Rectangle2D) geometry;

            // Clear the figure collections.
            _currentFigures = new ArrayList(_currentFigures.size());

            // Refilter the figures
            _filter(_currentFigures, _elements.iterator(), _region);
        }

        /** Test if the given figure is within this set
         */
        public abstract boolean _contains(Figure f, Rectangle2D region);

        /* Filter the figures into the given ArrayList
         */
        protected abstract void _filter(ArrayList result, Iterator i,
                Rectangle2D region);
    }

    /** The figures with intersecting bounding boxes
     */
    private class IntersectedGSet extends GSet {
        public IntersectedGSet(Rectangle2D region) {
            super(region);
        }

        /** Test if the given figure is within this set
         */
        @Override
        public boolean _contains(Figure f, Rectangle2D region) {
            return f.getBounds().intersects(region);
        }

        /* Filter the figures into the given ArrayList
         */
        @Override
        public void _filter(ArrayList result, Iterator i, Rectangle2D region) {
            while (i.hasNext()) {
                Figure f = (Figure) i.next();

                if (f.getBounds().intersects(region)) {
                    result.add(f);
                }
            }
        }
    }

    /** The figures with enclosed bounding boxes
     */
    private class EnclosedGSet extends GSet {
        public EnclosedGSet(Rectangle2D region) {
            super(region);
        }

        /** Test if the given figure is within this set
         */
        @Override
        public boolean _contains(Figure f, Rectangle2D region) {
            return region.contains(f.getBounds());
        }

        /* Filter the figures into the given ArrayList
         */
        @Override
        public void _filter(ArrayList result, Iterator i, Rectangle2D region) {
            while (i.hasNext()) {
                Figure f = (Figure) i.next();

                if (region.contains(f.getBounds())) {
                    result.add(f);
                }
            }
        }
    }
}
