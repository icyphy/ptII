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

package diva.canvas.event;

import diva.canvas.CanvasLayer;
import diva.canvas.interactor.Interactor;

import java.awt.AWTEvent;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.Iterator;

/** An event layer is a canvas layer that accepts mouse events.
 * It is designed to be layered over or under other layers, such
 * as FigureLayer, and accepts a number of options that control
 * whether it consumes events and so on. Event layers can have event
 * listeners attached to them, which will be notified when
 * events occur.
 *
 * <p>Sample uses of the event layer
 * <ul>
 * <li>On top of a figure layer, events consumed: trap all events
 * before they get to the figure layer.
 * <li>On top of a figure layer, events not consumed: monitor all
 * events but still allow the figure layer to respond.
 * <li> Behind of a figure layer: catch events on the "background"
 * of the pane, which didn't hit a figure in the figure layer.
 * </ul>
 *
 * @version        $Id$
 * @author         John Reekie
 */
public class EventLayer extends CanvasLayer implements EventAcceptor {

    /** The enabled flag.
     */
    private boolean _enabled = true;

    /** The consuming flag.
     */
    private boolean _consuming = false;

    /** The current interactor
     */
    private Interactor _currentInteractor;

    /** The list of attached interactors
     */
    private ArrayList _interactors = new ArrayList();

    /** The layer listeners
     */
    transient private LayerListener layerListener = null;

    /** The layer motion listeners
     */
    transient private LayerMotionListener layerMotionListener = null;

    ///////////////////////////////////////////////////////////////////
    //// Public methods

    /**
     * Add an interactor to this interactor. The added interactor
     * will have events forwarded to it if it accepts the event.
     */
    public void addInteractor (Interactor i) {
        _interactors.add(i);
    }

    /** Add the given layer listener to this dispatcher.
     */
    public void addLayerListener(LayerListener l) {
        layerListener = LayerEventMulticaster.add(layerListener,l);
    }

    /** Add the given layer motion listener to this dispatcher.
     */
    public void addLayerMotionListener(LayerMotionListener l) {
        layerMotionListener = LayerEventMulticaster.add(layerMotionListener,l);
    }

    /** Dispatch an AWT event on this layer. Currently only
     * layer events are handled.
     */
    public void dispatchEvent (AWTEvent event) {
        if (event instanceof LayerEvent) {
            processLayerEvent((LayerEvent)event);
        } else {
            // FIXME
            System.out.println("Bad event: " + event);
        }
    }

    /**
     * Return an iteractor over the attached interactors.
     */
    public Iterator interactors () {
        return _interactors.iterator();
    }

    /** Test the consuming flag of this layer. If this flag is
     * set, the layer consumes all input events.
     */
    public boolean isConsuming () {
        return _consuming;
    }

    /** Test the enabled flag of this layer. Note that this flag
     *  does not indicate whether the layer is actually enabled,
     * as its pane or one if its ancestors may not be enabled.
     */
    public boolean isEnabled () {
        return _enabled;
    }

    /** Process a layer event. This method dispatches
     * the event to any registered layer listeners or
     * layer motion listeners, and then to any interactors. If the
     * "consuming" flag is set, it consumes the event before returning.
     */
    protected void processLayerEvent(LayerEvent event) {
        if (!isEnabled()) {
            return;
        }

        // Process layer events
        if (layerListener != null) {
            int id = event.getID();
            switch(id) {
            case MouseEvent.MOUSE_PRESSED:
                layerListener.mousePressed(event);
                break;
            case MouseEvent.MOUSE_DRAGGED:
                layerListener.mouseDragged(event);
                break;
            case MouseEvent.MOUSE_RELEASED:
                layerListener.mouseReleased(event);
                break;
            case MouseEvent.MOUSE_CLICKED:
                layerListener.mouseClicked(event);
                break;
            }
        }


        // Process layer motion events
        if (layerMotionListener != null) {
            int id = event.getID();
            switch(id) {
            case MouseEvent.MOUSE_MOVED:
                layerMotionListener.mouseMoved(event);
                break;
            case MouseEvent.MOUSE_EXITED:
                layerMotionListener.mouseExited(event);
                break;
            case MouseEvent.MOUSE_ENTERED:
                layerMotionListener.mouseEntered(event);
                break;
            }
        }

        int id = event.getID();
        // Pass the event to attached interactors
        switch(id) {
        case MouseEvent.MOUSE_PRESSED:
            // Find what interactor will grab the event.
            for (Iterator i = _interactors.iterator(); i.hasNext(); ) {
                Interactor interactor = (Interactor) i.next();

                if (interactor.accept(event)) {
                    _currentInteractor = interactor;
                    _currentInteractor.mousePressed(event);
                    break;
                }
            }
            break;
        case MouseEvent.MOUSE_DRAGGED:
            if (_currentInteractor != null) {
                _currentInteractor.mouseDragged(event);
            }
            break;

        case MouseEvent.MOUSE_RELEASED:
            if (_currentInteractor != null) {
                _currentInteractor.mouseReleased(event);
            }
            _currentInteractor = null;
            break;
        case MouseEvent.MOUSE_CLICKED:
            if (_currentInteractor != null) {
                _currentInteractor.mouseClicked(event);
            }
            _currentInteractor = null;
            break;
        }
        if (_consuming) {
            event.consume();
        }
    }

    /**
     * Remove the given interactor from this interactor.
     */
    public void removeInteractor (Interactor i) {
        _interactors.remove(i);
    }

    /** Remove the given layer listener from this dispatcher.
     */
    public void removeLayerListener(LayerListener l) {
        layerListener = LayerEventMulticaster.remove(layerListener, l);
    }

    /** Remove the given layer motion listener from this dispatcher.
     */
    public void removeLayerMotionListener(LayerMotionListener l) {
        layerMotionListener = LayerEventMulticaster.remove(layerMotionListener, l);
    }

    /** Set the consuming flag of this layer.  If this flag is
     * set, the layer consumes all input events.
     */
    public void setConsuming (boolean flag) {
        _consuming = flag;
    }

    /** Set the enabled flag of this layer. If the flag is false,
     * then the layer will not respond to user input events.
     */
    public void setEnabled (boolean flag) {
        _enabled = flag;
    }
}



