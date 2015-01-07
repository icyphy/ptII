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
package diva.canvas.interactor;

import java.util.Iterator;

import diva.canvas.Figure;
import diva.canvas.FigureDecorator;
import diva.canvas.Site;
import diva.canvas.event.LayerEvent;

/**
 * A manipulator which attaches grab handles to the sites
 * of the child figure.  It renders the grab handles and gives them a
 * chance to intercept picks.
 *
 * @author John Reekie
 * @author Michael Shilman
 * @version $Id$
 */
public class PathManipulator extends Manipulator {
    /** The path geometry
     */
    private PathGeometry _geometry;

    /**
     * Construct a new manipulator that uses rectangular grab-handles.
     */
    public PathManipulator() {
        this(new BasicGrabHandleFactory());
    }

    /**
     * Construct a new manipulator using the given grab-handle factory.
     */
    public PathManipulator(GrabHandleFactory f) {
        setGrabHandleFactory(f);
        setHandleInteractor(new Resizer());
    }

    /** Return the geometry of this manipulator
     */
    private PathGeometry getGeometry() {
        return _geometry;
    }

    /** Create a new instance of this manipulator. The new
     * instance will have the same grab handle, and interactor
     * for grab-handles, as this one.
     */
    @Override
    public FigureDecorator newInstance(Figure f) {
        PathManipulator m = new PathManipulator();
        m.setGrabHandleFactory(this.getGrabHandleFactory());
        m.setHandleInteractor(this.getHandleInteractor());
        return m;
    }

    /** Refresh the geometry.
     */
    @Override
    public void refresh() {
        if (_geometry != null) {
            _geometry.setShape(getChild().getShape());
        }
    }

    /** Set the child figure. If we have any grab-handles, lose them.
     * Then get a path geometry object set on this figure (the
     * manipulator, not the child) and create grab-handles on it.
     */
    @Override
    public void setChild(Figure f) {
        super.setChild(f);
        clearGrabHandles();
        _geometry = null;

        // Process new child
        Figure child = getChild();

        if (child != null) {
            // Check that we can mess with this figure
            if (!(child instanceof ShapedFigure)) {
                throw new IllegalArgumentException(
                        "PathManipulator can only decorate a ShapedFigure");
            }

            // Create the geometry defining the sites
            _geometry = new PathGeometry(this, getChild().getShape());

            Iterator i = _geometry.vertices();
            GrabHandle g = null;

            while (i.hasNext()) {
                // Create a grab handle and set up the interactor.
                // Unless it's a close segment, in which case we ignore it.
                Site site = (Site) i.next();

                if (!(site instanceof PathGeometry.CloseSegment)) {
                    g = getGrabHandleFactory().createGrabHandle(site);
                    g.setParent(this);
                    g.setInteractor(getHandleInteractor());
                    addGrabHandle(g);
                }
            }
        }

        // repaint();
    }

    ///////////////////////////////////////////////////////////////////
    //// Resizer

    /** An interactor class that changes a vertex of the child figure
     *  and triggers a repaint.
     */
    private static class Resizer extends DragInteractor {
        /** Translate the grab-handle
         */
        @Override
        public void translate(LayerEvent e, double x, double y) {
            // Translate the grab-handle, resizing the geometry
            GrabHandle g = (GrabHandle) e.getFigureSource();
            g.translate(x, y);

            // Transform the child -- could be made more efficient?...
            PathManipulator parent = (PathManipulator) g.getParent();
            PathGeometry geometry = parent.getGeometry();

            ((ShapedFigure) parent.getChild()).setShape(geometry.getShape());
        }
    }
}
