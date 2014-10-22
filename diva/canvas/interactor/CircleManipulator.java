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

import diva.canvas.CanvasUtilities;
import diva.canvas.Figure;
import diva.canvas.FigureDecorator;
import diva.canvas.Site;
import diva.canvas.connector.CenterSite;
import diva.canvas.event.LayerEvent;

/**
 * A manipulator which attaches a grab handles to one of the bounds
 * of the child figure.  It renders the grab handle and gives it a
 * chance to intercept picks.
 *
 * @author Nick Zamora
 * @version $Id$
 */
public class CircleManipulator extends Manipulator {
    /** The geometry "helper"
     */
    private CircleGeometry _geometry;

    /** The interactor that is attached to a move handle.
     */
    private Interactor _dragInteractor = null;

    /**
     * Construct a new manipulator that uses rectangular grab-handles.
     */
    public CircleManipulator() {
        this(new BasicGrabHandleFactory());
    }

    /**
     * Construct a new manipulator using the given grab-handle factory.
     */
    public CircleManipulator(GrabHandleFactory f) {
        setGrabHandleFactory(f);
        setHandleInteractor(new Resizer());
    }

    /** Return the geometry of this manipulator
     */
    private CircleGeometry getGeometry() {
        return _geometry;
    }

    /** Create a new instance of this manipulator. The new
     * instance will have the same grab handle, and interactor
     * for grab-handles, as this one.
     */
    @Override
    public FigureDecorator newInstance(Figure f) {
        CircleManipulator m = new CircleManipulator();
        m.setGrabHandleFactory(this.getGrabHandleFactory());
        m.setHandleInteractor(this.getHandleInteractor());
        m.setDragInteractor(_dragInteractor);
        return m;
    }

    /** Refresh the geometry. This adjusts the bounds of the geometry
     * to match the bounds of the child figure.
     */
    @Override
    public void refresh() {
        if (_geometry != null) {
            _geometry.setBounds(getChild().getBounds());
        }
    }

    /** Set the child figure. If we have a grab-handle, lose it.
     * Then get a rectangle geometry object and create a grab-handle
     * on one of its sites.
     */
    @Override
    public void setChild(Figure child) {
        super.setChild(child);
        clearGrabHandles();

        // Process new child
        if (child != null) {
            // Create the geometry defining the sites
            _geometry = new CircleGeometry(this, getChild().getBounds());

            Iterator i = _geometry.sites();
            GrabHandle g = null;

            while (i.hasNext()) {
                // Create a grab handle and set up the interactor
                Site site = (Site) i.next();
                g = getGrabHandleFactory().createGrabHandle(site);
                g.setParent(this);
                g.setInteractor(getHandleInteractor());
                addGrabHandle(g);
            }

            // Add a center handle for dragging
            if (_dragInteractor != null) {
                CenterSite center = new CenterSite(getChild());
                GrabHandle mover = new MoveHandle(center);
                mover.setParent(this);
                mover.setInteractor(_dragInteractor);
                addGrabHandle(mover);
            }

            // Move them where they should be - ?
            relocateGrabHandles();

            // Set the minimum size
            // FIXME: this is bogus: set it in the interactor instead!
            _geometry.setMinimumSize(4 * g.getSize());
        }
    }

    /** Set the drag interactor for figures wrapped by this
     * manipulator. If set, the manipulator displays an additional
     * handle that can be used to drag the figure. This is useful
     * for certain types of figure that are outlines only.
     */
    public void setDragInteractor(Interactor dragger) {
        _dragInteractor = dragger;
    }

    ///////////////////////////////////////////////////////////////////
    //// Resizer

    /** An interactor class that changes the bounds of the child
     * figure and triggers a repaint.
     */
    private static class Resizer extends DragInteractor {
        /** Translate the grab-handle
         */
        @Override
        public void translate(LayerEvent e, double x, double y) {
            // Translate the grab-handle, resizing the geometry
            GrabHandle g = (GrabHandle) e.getFigureSource();
            g.translate(x, y);

            // Transform the child.
            CircleManipulator parent = (CircleManipulator) g.getParent();
            CircleGeometry geometry = parent.getGeometry();

            parent.getChild().transform(
                    CanvasUtilities.computeTransform(parent.getChild()
                            .getBounds(), geometry.getBounds()));
        }
    }
}
