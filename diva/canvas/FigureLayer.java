/*
 Copyright (c) 1998-2001 The Regents of the University of California
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

import java.awt.AWTEvent;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import diva.canvas.event.EventAcceptor;
import diva.canvas.event.LayerEvent;
import diva.canvas.interactor.Interactor;
import diva.util.Filter;

/** A figure layer is a layer on which Figures can be drawn.  It
 * contains a z-list in which all the contained figures are held, and
 * implements wrappers for most of the z-list methods to provide
 * flexible access to the contained figures.   Figures are also
 * stored at contiguous integer indexes, with index zero being the "topmost"
 * figure and the highest index being the "lowest" figure.
 *
 * <p> FigureLayer responds to events on
 * figures themselves by forwarding the events to interactors attached
 * to a hit figure. It does not support events on the layer itself
 * (see EventLayer for that).
 *
 * @version        $Id$
 * @author John Reekie
 * @rating Yellow
 */
public class FigureLayer extends CanvasLayer implements FigureContainer, EventAcceptor {

    /** The figures contained in this layer.
     */
    private ZList _zlist = null;

    /** The size of the halo to use for "picking."
     */
    private double _pickHalo = 0.5;

    /** The visibility flag.
     */
    private boolean _visible = true;

    /** The enabled flag.
     */
    private boolean _enabled = true;

    /** The figure that the pointer is currently over.
     */
    private Figure _pointerOver = null;

    /** The figure that has currently grabbed the pointer.
     */
    private Figure _pointerGrabber = null;

    /** The last layer event processed by this layer.
     */
    private LayerEvent _lastLayerEvent = null;

    /** Create a new figure layer that is not in a pane. The layer will
     * not be displayed, and its coordinate transformation will be
     * as though it were a one-to-one mapping. Use of this constructor
     * is strongly discouraged, as many of the geometry-related methods
     * expect to see a pane.
     */
    public FigureLayer () {
        super();
        _zlist = new BasicZList();
    }

    /** Create a new figure layer within the given pane.
     */
    public FigureLayer (CanvasPane pane) {
        super(pane);
        _zlist = new BasicZList();
    }

    /** Create a new figure layer within the given pane and with
     * the given ZList to hold the figures it contains.
     */
    public FigureLayer (CanvasPane pane, ZList zlist) {
        super(pane);
        this._zlist = zlist;
    }

    /** Create a new figure layer with
     * the given ZList to hold its figures. This can be used
     * to create a more efficient z-list than the default,
     * which is an instance of BasicZList.
     */
    public FigureLayer (ZList zlist) {
        super();
        this._zlist = zlist;
    }

    /** Add a figure to the layer. The figure is added to the
     * ZList, and the figure's layer and parent fields
     * set appropriately. The figure will be painted over
     * the top of all existing figures. It does <i>not</i>
     * check whether the figure is already in the layer -- clients
     * clients are therefore responsible for being bug-free.
     */
    public void add (Figure f) {
        _zlist.add(0,f);
        f.setParent(this);
        repaint(f.getBounds());
    }

    /** Insert a figure into the layer at the given position.  To
     *  insert the figure just in front of some other figure, use
     *  getIndex() to get the other figure's index, and pass
     *  <i>index</i> as the first argument. To insert the figure just
     *  behind some other figure, pass <i>index+1</i> as the first
     *  argument. To insert so the figure displays over the top of
     *  other figures, insert at zero.
     *
     *  <p>Clients should assume that an implementation of this method
     *  does <i>not</i> check if the figure is already contained --
     *  clients are therefore responsible for being bug-free.
     */
    public void add (int index, Figure f) {
        _zlist.add(index, f);
        f.setParent(this);
        repaint(f.getBounds());
    }

    /** Removes all of the figures from this layer.
     */
    public void clear() {
        _zlist.clear();
        repaint();
    }

    /** Test if the layer contains the given figure.  Note that, in
     * general, a much better way of making this same test is to check
     * if the parent of the figure is the same object as this
     * layer.
     */
    public boolean contains (Figure f) {
        return _zlist.contains(f);
    }

    /** Decorate a child figure, replacing the child figure with the
     * decorator.
     */
    public void decorate (Figure child, FigureDecorator decorator) {
        if (child.getParent() != this) {
            throw new IllegalArgumentException(
                    "The object " +
                    child +
                    " is not a child of " +
                    this);
        }
        child.repaint();
        decorator.setParent(this);
        decorator.setChild(child);
        _zlist.set(_zlist.indexOf(child), decorator);
        decorator.repaint();
    }

    /** Dispatch an AWT event on this layer.  If the layer
     * is not enabled, return immediately. Otherwise process the event
     * according to its type. If the event represents a mouse click, drag,
     * or release, call the protected method <b>processLayerEvent</b>;
     * if it represents a mouse movement, call the protected method
     * </b>processLayerMotionEvent</b>. Currently other events types
     * are not handled.
     */
    public void dispatchEvent (AWTEvent event) {
        if (!isEnabled()) {
            return;
        }
        switch(event.getID()) {
        case MouseEvent.MOUSE_CLICKED:
        case MouseEvent.MOUSE_PRESSED:
        case MouseEvent.MOUSE_RELEASED:
        case MouseEvent.MOUSE_DRAGGED:
            processLayerEvent((LayerEvent)event);
            break;

        case MouseEvent.MOUSE_ENTERED:
        case MouseEvent.MOUSE_EXITED:
        case MouseEvent.MOUSE_MOVED:
            processLayerMotionEvent((LayerEvent)event);
            break;

        default:
            throw new IllegalArgumentException(
                    "Unrecognized event type: " + event);
        }
    }

    /** Return an iteration of the figures in this container. The order
     * in which figures are iterated is <i>undefined</i>.
     */
    public Iterator figures () {
        return _zlist.figures();
    }

    /** Return an iteration of the figures in this container, from
     * back to front. This is the order in which figures should
     * normally be painted.
     */
    public Iterator figuresFromBack () {
        return _zlist.figuresFromBack();
    }

    /** Return an iteration of the figures in this container, from
     * front to back. This is the order in which events should normally
     * be intercepted.
     */
    public Iterator figuresFromFront () {
        return _zlist.figuresFromFront();
    }

    /** Get the figure at the given index. Indexes are contiguous from
     * zero to getFigureCount()-1, with figures at lower indexes
     * being displayed "on top of" figures with higher indexes.
     */
    public Figure get (int index) {
        return _zlist.get(index);
    }

    /** Return the number of figures in this layer.
     */
    public int getFigureCount () {
        return _zlist.getFigureCount();
    }

    /** Return the figure that the mouse pointer is currently over,
     *  or null if none.
     *  @return The figure the mouse is currently over.
     */
    public Figure getCurrentFigure() {
        LayerEvent e = _lastLayerEvent;
        if(e != null && e.getID() != MouseEvent.MOUSE_EXITED) {
            return getFigure(e);
        } else {
            return null;
        }
    }

    /** Get the internal z-list. Clients must <i>not</i> modify
     * the z-list, but can use it for making queries on its contents.
     */
    public ZList getFigures () {
        return _zlist;
    }

    /** Get the bounds of the shapes draw in this layer.  In this class,
     *  we return the bounds of all the figures in the z-list.
     */
    public Rectangle2D getLayerBounds () {
        return _zlist.getBounds();
    }

    /** Get the "pick halo". This is the distance in either axis
     * that an object can be from the mouse to be considered
     * hit.
     */
    public final double getPickHalo () {
        return _pickHalo;
    }

    /** "Grab" the pointer. Typically, this method will be called from
     * an Interactor class in response to a mousePressed() event. The
     * effect of calling this method is to make the series of mouse
     * events up to and including the mouseReleased() event appear to
     * be originating from the figure <i>f</i> rather than the
     * actual figure that was clicked on. For example, if clicking
     * and dragging on a graph node creates and the drags a new edge,
     * this method will be called after constructing the edge to make
     * the edge itself handle the mouse drag  events instead of the node.
     */
    public void grabPointer(LayerEvent e, Figure f) {
        if (e.getID() != MouseEvent.MOUSE_PRESSED) {
            throw new IllegalArgumentException(
                    "The event " +
                    e +
                    " is not a mouse pressed event.\n" +
                    "Only mouse pressed events can grab the pointer" +
                    " (for now)");
        }

        // Remember the grab
        _pointerGrabber = f;

        // If the figure is prepared to handle events itself, let it
        if (f instanceof EventAcceptor) {
            ((EventAcceptor) f).dispatchEvent(e);
        }
        // If the event isn't consumed yet, scan up the tree to dispatch it
        if (!e.isConsumed()) {
            dispatchEventUpTree(f, e);
        }
    }

    /** Get the toolTipText for the point in the given LayerEvent.
     * This method starts with the figure that is set by the mouse motion
     * events when the pointer moves over a figure. Starting with that
     * figure, it walks up the tree until it finds a figure that
     * returns a tool tip, or until it reaches a root figure. If it
     * finds a tool tip, it returns it, otherwise it returns null.
     */
    public String getToolTipText(LayerEvent e) {
        String tip = null;

        // Scan up the tree try to dispatch the event
        Figure f = getFigure(e);
        while (f != null) {
            tip = f.getToolTipText();
            if (tip != null) break;

            // Move up to the parent
            CanvasComponent p = f.getParent();
            if ( !(p instanceof Figure)) {
                break;
            }
            f = (Figure) p;
        }
        return tip;
    }

    /** Return the index of the given figure. Figures
     *  with a higher index are drawn behind figures with a lower index.
     */
    public int indexOf (Figure f) {
        return _zlist.indexOf(f);
    }

    /** Test the enabled flag of this layer. Note that this flag
     *  does not indicate whether the layer is actually enabled,
     * as its pane or one if its ancestors may not be enabled.
     */
    public final boolean isEnabled () {
        return _enabled;
    }

    /** Test the visibility flag of this layer. Note that this flag
     *  does not indicate whether the layer is actually visible on
     *  the screen, as its pane or one if its ancestors may not be visible.
     */
    public final boolean isVisible () {
        return _visible;
    }

    /** Paint this layer onto a 2D graphics object. If the layer
     * is not visible, return immediately. Otherwise paint all figures
     * from back to front.
     */
    public void paint (Graphics2D g) {
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

    /** Paint this layer onto a 2D graphics object, within the given
     * region.  If the layer is not visible, return
     * immediately. Otherwise paint all figures that overlap the given
     * region, from back to front.
     */
    public void paint (Graphics2D g, Rectangle2D region) {
        if (!isVisible()) {
            return;
        }
        Figure f;
        Iterator i = _zlist.getIntersectedFigures(region).figuresFromBack();
        while (i.hasNext()) {
            f = (Figure) i.next();
            f.paint(g,region);
        }
    }

    /** Get the picked figure. This method recursively traverses the
     * tree until it finds a figure that is "hit" by the region. Note
     * that a region is given instead of a point so that "hysteresis"
     * can be implemented. If no figure is picked, return null.  The
     * region should not have zero size, or no figure will be hit.
     */
    public Figure pick (Rectangle2D region) {
        return CanvasUtilities.pick(
                _zlist.getIntersectedFigures(region).figuresFromFront(),
                region);
    }

    /** Get the picked figure. This method recursively traverses the
     * tree until it finds a figure that is "hit" by the region. Note
     * that a region is given instead of a point so that "hysteresis"
     * can be implemented. If no figure is picked, return null.  The
     * region should not have zero size, or no figure will be hit.
     */
    public Figure pick (Rectangle2D region, Filter filter) {
        Iterator iterator =
            _zlist.getIntersectedFigures(region).figuresFromFront();
        return CanvasUtilities.pick(iterator, region, filter);
    }

    /** Remove the given figure from this layer. The figure's
     * layer is set to null.
     */
    public void remove (Figure f) {
        _zlist.remove(f);
        f.setParent(null);
        repaint(f.getBounds());
    }

    /** Remove the figure at the given position in the list. The figure's
     * layer is set to null.
     */
    public void remove (int index) {
        Figure f = _zlist.get(index);
        _zlist.remove(index);
        f.setParent(null);
        repaint(f.getBounds());
    }

    /** Repaint all figures that intersect the given rectangle.
     */
    public void repaint (Rectangle2D region) {
        repaint(DamageRegion.createDamageRegion(
                getTransformContext(), region));
    }

    /** Set the enabled flag of this layer. If the flag is false,
     * then the layer will not respond to user input events.
     */
    public final void setEnabled (boolean flag) {
        _enabled = flag;
    }

    /** Set the "pick halo". This is the distance a figure
     * can be from the mouse in either axis to be considered
     * hit by the mouse. By default, it it set to 0.5, meaning
     * that the hit detection rectangle is 1.0 along each side.
     */
    public final void setPickHalo (double halo) {
        _pickHalo = halo;
    }

    /** Set the index of the given figure.  That is, move it in the
     * display list to the given position. To move the figure to just
     * in front of some other figure, use getIndex() to get the other
     * figure's index, and pass <i>index</i> as the first argument.
     * To move the figure to just behind some other figure, pass
     * <i>index+1</i> as the first argument. (Other figures will have
     * their indexes changed accordingly.)
     *
     * <p> Note that this method does <i>not</i> check if the figure
     * is already contained -- clients are therefore responsible for
     * being bug-free.
     *
     * @exception IndexOutOfBoundsException The new index is out of range.
     */
    public void setIndex (int index, Figure f) {
        _zlist.setIndex(index,f);
        repaint(f.getBounds());
    }

    /** Set the visibility flag of this layer. If the flag is false,
     * then the layer will not be drawn on the screen.
     */
    public final void setVisible (boolean flag) {
        _visible = flag;
        repaint();
    }

    /** Remove a figure from the given decorator and add
     * it back into this container.
     */
    public void undecorate (FigureDecorator decorator) {
        if (decorator.getParent() != this) {
            throw new IllegalArgumentException(
                    "The object " +
                    decorator +
                    "is not a child of " +
                    this);
        }
        decorator.repaint();
        Figure child = decorator.getChild();
        _zlist.set(_zlist.indexOf(decorator),child);
        decorator.setChild(null);
        decorator.setParent(null);
        child.setParent(this); // This is needed
        child.repaint();
    }

    ///////////////////////////////////////////////////////////////////////
    //// protected methods

    /** Dispatch a layer event up the tree. Proceed up the hierarchy
     * looking for a figure that has an interactor that is
     * enabled for layer events. Dispatch the event to the first
     * one found. If the event is not consumed, repeat.
     */
    private void dispatchEventUpTree(Figure f, LayerEvent e) {
        // Scan up the tree try to dispatch the event
        while (f != null) {
            Interactor interactor = f.getInteractor();
            if (interactor != null) {
                // Set the figure source
                e.setFigureSource(f);

                if (interactor.accept(e)) {
                    // Send the event to the interactor
                    switch(e.getID()) {
                    case MouseEvent.MOUSE_DRAGGED:
                        interactor.mouseDragged(e);
                        break;
                    case MouseEvent.MOUSE_PRESSED:
                        interactor.mousePressed(e);
                        break;
                    case MouseEvent.MOUSE_RELEASED:
                        interactor.mouseReleased(e);
                        break;
                    case MouseEvent.MOUSE_CLICKED:
                        interactor.mouseClicked(e);
                        break;
                    }
                }
            }
            // Break if the event was consumed
            if (e.isConsumed()) {
                break;
            }
            // Move up to the parent
            CanvasComponent p = f.getParent();
            if ( !(p instanceof Figure)) {
                break;
            }
            f = (Figure) p;
        }
    }

    /** Dispatch a layer motion event up the tree. Proceed up the hierarchy
     * looking for a figure that has an interactor that is enabled
     * for motion events. Dispatch the event to the first
     * one found. The event does not propagate up the tree any
     * further, regardless of whether the event was consumed or not.
     * (Is this the right behavior??)
     */
    private void dispatchMotionEventUpTree(Figure f, LayerEvent e) {
        // Scan up the tree try to dispatch the event
        while (f != null) {
            Interactor interactor = f.getInteractor();
            if (interactor != null && interactor.isMotionEnabled()) {
                // Set the figure source
                e.setFigureSource(f);

                if (interactor.accept(e)) {
                    // Send the event to the interactor
                    switch(e.getID()) {
                    case MouseEvent.MOUSE_MOVED:
                        interactor.mouseMoved(e);
                        break;
                    case MouseEvent.MOUSE_EXITED:
                        interactor.mouseExited(e);
                        break;
                    case MouseEvent.MOUSE_ENTERED:
                        interactor.mouseEntered(e);
                        break;
                    }
                }
            }
            // Move up to the parent
            CanvasComponent p = f.getParent();
            if ( !(p instanceof Figure)) {
                break;
            }
            f = (Figure) p;
        }
    }

    /** Return the figure pointed to by the given LayerEvent.  If
     * there is no figure, then return null.
     */
    protected final Figure getFigure(LayerEvent e) {
        // Get the figure that the mouse hit, if any
        double wh = _pickHalo * 2;
        Rectangle2D region = new Rectangle2D.Double (
                e.getLayerX() - _pickHalo,
                e.getLayerY() - _pickHalo,
                wh, wh);
        return pick(region);
    }
   
    /** Process a layer event. The behaviour of this method depends on
     * the action type. If it is MOUSE_PRESSED, then it recurses
     * down the figure tree searching for the top-most figure under
     * the cursor. (If a figure has been hit on its transparent part,
     * then it will not be considered to be above another figure --
     * the method Figure.hits() determines whether a figure gets the
     * event.) When it finds it, it remembers it, and then
     * proceeds back up the tree, passing the event to the event
     * dispatcher of any figures that have one. After each such
     * call, if the event has been consumed, then the upwards-traversal
     * stops. Finally, if this layer is reached, and the event has
     * not been consumed, then any registered LayerListeners are
     * called. (Or should they be notified in any case?)
     *
     * <p>If the event type is MOUSE_DRAGGED or MOUSE_RELEASED, then
     * the downwards recursion is skipped, and the upwards propagation
     * is begun from the figure remembered from the MOUSE_PRESSED
     * processing. Again, the propagation stops when the event is
     * consumed.
     *
     * <p><b>Note</b>: the above strategy will not work with more than
     * one input device. Is there anything in MouseEvent that allows
     * us to identify the input device?
     */
    protected void processLayerEvent(LayerEvent e) {
        Figure f;
        int id = e.getID();
        _lastLayerEvent = e;
        e.setLayerSource(this);

        switch(id) {
        case MouseEvent.MOUSE_PRESSED:
            f = getFigure(e);

            // If there's a figure, grab the pointer and process the event
            if (f != null) {
                grabPointer(e, f);
            } else {
                // If the pointer was grabbed, and the new mouse press
                // was on the background, then all we have to do is
                // forget the previous grab.
                _pointerGrabber = null;
            }
            break;

        case MouseEvent.MOUSE_DRAGGED:
            if (_pointerGrabber == null) {
                // Ignore the event if noone grabbed the pointer before
                return;
            }
            // If the figure is prepared to handle events itself, let it
            if (_pointerGrabber instanceof EventAcceptor) {
                ((EventAcceptor) _pointerGrabber).dispatchEvent(e);
            }

            // If the event isn't consumed yet, scan up
            // the tree and dispatch it
            if (!e.isConsumed()) {
                dispatchEventUpTree(_pointerGrabber, e);
            }
            break;

        case MouseEvent.MOUSE_RELEASED:
            if (_pointerGrabber == null) {
                // Ignore the event if noone grabbed the pointer before
                return;
            }
            // If the figure is prepared to handle events itself, let it
            if (_pointerGrabber instanceof EventAcceptor) {
                ((EventAcceptor) _pointerGrabber).dispatchEvent(e);
            }

            // If the event isn't consumed yet, scan up
            // the tree and dispatch it
            if (!e.isConsumed()) {
                dispatchEventUpTree(_pointerGrabber, e);
            }
            // Clear the pointer grab
            _pointerGrabber = null;
            break;

            // Process a click event only. This code ignores the
            // grab, as it should have already been cleared by a
            // preceding MOUSE_RELEASED event. I'm not entirely
            // sure if this is actually correct or not.
            //
        case MouseEvent.MOUSE_CLICKED:
            // Get the figure that the mouse hit, if any
            f = getFigure(e);

            // If there's no figure, we're done
            if (f == null) {
                return;
            }

            // If the figure is prepared to handle events itself, let it
            if (f instanceof EventAcceptor) {
                ((EventAcceptor) f).dispatchEvent(e);
            }
            // If the event isn't consumed yet, scan up the tree to dispatch it
            if (!e.isConsumed()) {
                dispatchEventUpTree(f, e);
            }
            break;
        }
    }

    /** Process a layer motion event. The behavior of this method
     * depends on the action type. If the action is MOUSE_ENTERED.
     * then the figure tree is scanned to find if the mouse is
     * now over a figure, and the event is dispatched to that
     * figure if so. If the action is MOUSE_MOVED, then the tree
     * is scanned again to find the figure currently under the mouse;
     * if it is different, then leave and enter events are generated
     * on the previous and current figures (either may not exist);
     * otherwise a motion event is generated on the current figure.
     * If the action is MOUSE_EXITED, then the current figure, if
     * there is one, has an exit event sent to it. In all of these cases
     * the event is propagated from the current figure up the hierarchy
     * until consumed.
     */
    protected void processLayerMotionEvent(LayerEvent e) {
        int id = e.getID();
        _lastLayerEvent = e;
        e.setLayerSource(this);

        if (id == MouseEvent.MOUSE_EXITED) {
            dispatchMotionEventUpTree(_pointerOver, e);
            _pointerOver = null;
        } else if (id == MouseEvent.MOUSE_ENTERED) {
            // Get the figure that the mouse hit, if any.
            _pointerOver = getFigure(e);
            dispatchMotionEventUpTree(_pointerOver, e);
        } else if (id == MouseEvent.MOUSE_MOVED) {
            // Get the figure that the mouse hit, if any.
            Figure figure = getFigure(e);
            if (figure != _pointerOver) {
                LayerEvent event;
                event = new LayerEvent(e,
                        MouseEvent.MOUSE_EXITED);
                dispatchMotionEventUpTree(_pointerOver, event);
                _pointerOver = figure;
                event = new LayerEvent(e,
                        MouseEvent.MOUSE_ENTERED);
                dispatchMotionEventUpTree(_pointerOver, event);
            }
        }
    }
}


