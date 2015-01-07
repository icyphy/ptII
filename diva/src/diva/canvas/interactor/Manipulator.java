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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;

import diva.canvas.CanvasUtilities;
import diva.canvas.DamageRegion;
import diva.canvas.Figure;
import diva.canvas.FigureDecorator;
import diva.canvas.event.MouseFilter;

/**
 * A Manipulator is an object that decorates some figure, and generally
 * paint grab-handles or some other stuff to the figure to that
 * it can be resized and manipulated. This is an abstract class. Concrete
 * subclasses implement particular kinds of manipulator.
 *
 * @author John Reekie
 * @author Michael Shilman
 * @version        $Id$
 */
public abstract class Manipulator extends FigureDecorator {
    /** The factory that builds the grab handles.
     */
    private GrabHandleFactory _factory = new BasicGrabHandleFactory();

    /** The grab-handles that belong to this manipulator
     */
    private ArrayList _grabHandles = new ArrayList();

    /** The interactor for grab-handles
     */
    private DragInteractor _handleInteractor = new DragInteractor();

    /** A nasty little flag that turns off child repaints
     */
    private boolean _repainting = false;

    /** Add a grab-handle to this manipulator's collection of grab-handles.
     */
    public void addGrabHandle(GrabHandle gh) {
        _grabHandles.add(gh);
    }

    /** Clear all grab-handles.
     */
    public void clearGrabHandles() {
        _grabHandles.clear();
    }

    /** Get the bounds. This is the union of the child's bounding box
     * and the bounding boxes of all the grab-handles
     */
    @Override
    public Rectangle2D getBounds() {
        Rectangle2D bounds = getChild().getBounds();

        if (_grabHandles.size() > 0) {
            bounds = bounds.createUnion(CanvasUtilities
                    .computeCompositeBounds(_grabHandles.iterator()));
        }

        return bounds;
    }

    /** Get the grab handle factory.
     */
    public GrabHandleFactory getGrabHandleFactory() {
        return this._factory;
    }

    /** Get the grab handle interactor.
     */
    public DragInteractor getHandleInteractor() {
        return _handleInteractor;
    }

    /** Test if this manipulator is in the process of repainting
     * itself and its child.
     */
    public boolean isRepainting() {
        return _repainting;
    }

    /** Paint the manipulator. This default implementation first
     * paints the child figure, and then paints each of the
     * grab-handles.
     */
    @Override
    public void paint(Graphics2D g) {
        if (getChild() == null) {
            return;
        }

        getChild().paint(g);

        Iterator i = _grabHandles.iterator();

        while (i.hasNext()) {
            GrabHandle h = (GrabHandle) i.next();
            h.paint(g);
        }
    }

    /** Given a rectangle, return the top-most thing that hits
     * it. This could be one of the grab-handles, or the child.
     * If nothing hits, return null.
     */
    @Override
    public Figure pick(Rectangle2D r) {
        if (getChild() == null) {
            return null;
        }

        Iterator i = _grabHandles.iterator();

        while (i.hasNext()) {
            GrabHandle h = (GrabHandle) i.next();

            if (h.intersects(r)) {
                return h;
            }
        }

        if (getChild().hit(r)) {
            return getChild();
        } else {
            return null;
        }
    }

    /** Remove a grab-handle from this manipulator's
     * collection of grab-handles.
     */
    public void removeGrabHandle(GrabHandle gh) {
        _grabHandles.remove(gh);
    }

    /** Receive repaint notification. If the manipulator is already
     * in the middle of repainting, ignore this request. Otherwise,
     * generate another repaint call with the total region covered
     * by the child and its grab-handles both before and after
     * the grab-handles are moved to their new locations. Oh, and
     * move the grab-handles to their new locations.
     */
    @Override
    public void repaint(DamageRegion d) {
        if (isRepainting()) {
            return;
        }

        repaint();
    }

    /** Refresh the geometry. This means that the location of
     * geometry must be adjusted to match the current position
     * of the child figure.
     */
    public abstract void refresh();

    /** Relocate grab-handles to their correct positions. This is
     * a utility method to simplify notification callbacks.
     */
    public void relocateGrabHandles() {
        Iterator i = _grabHandles.iterator();

        while (i.hasNext()) {
            ((GrabHandle) i.next()).relocate();
        }
    }

    /** Request a repaint of the manipulator and child. This method
     * generates another repaint call with the total region covered
     * by the child and its grab-handles both before and after
     * the grab-handles are moved to their new locations. It also
     * adjusts the geometry to the current location of the child
     * moves the grab-handles to their new locations.
     */
    @Override
    public void repaint() {
        // Create the damage region
        DamageRegion d = DamageRegion.createDamageRegion(getTransformContext(),
                getBounds());

        // Change the geometry to match the current location
        refresh();

        // Move the grabhandles
        relocateGrabHandles();

        // Extend the damage region
        d.extend(getBounds());

        // Propagate the damage request
        if (getParent() != null) {
            getParent().repaint(d);
        }
    }

    /** Request a repaint of the manipulator and child. This method
     * generates another repaint call with the total region covered
     * by the child and its grab-handles both before and after
     * the grab-handles are moved to their new locations. It moves
     * moves the grab-handles to their new locations, but does not
     * adjust the geometry to the location of the child. This method
     * should therefore be used for repaints that are generated by
     * modifications to the geometry.
     */
    public void repaintAlready() {
        // Create the damage region
        DamageRegion d = DamageRegion.createDamageRegion(getTransformContext(),
                getBounds());

        // Move the grabhandles
        relocateGrabHandles();

        // Extend the damage region
        d.extend(getBounds());

        // Propagate the damage request
        if (getParent() != null) {
            getParent().repaint(d);
        }
    }

    /** Set the grab handle factory. This is set by default to
     * an instance of BasicGrabHandleFactory.
     */
    public void setGrabHandleFactory(GrabHandleFactory factory) {
        this._factory = factory;
    }

    /** Set the mouse filter that is set in the grab handle interactor.
     */
    public void setHandleFilter(MouseFilter filter) {
        _handleInteractor.setMouseFilter(filter);
    }

    /** Set the grab handle interactor. The interactor is set by default to
     * an instance of DragInteractor. Note that any previously-set mouse
     * filter will be lost.
     */
    public void setHandleInteractor(DragInteractor interactor) {
        _handleInteractor = interactor;
    }

    /** Set the repainting flag. This says that the manipulator
     * is in the process of repainting itself and its child, and
     * that it can therefore ignore repaint requests for the
     * children.
     */
    public void setRepainting(boolean repainting) {
        this._repainting = repainting;
    }
}
