/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */
package diva.canvas.interactor;

import java.util.ArrayList;
import java.util.Iterator;

import diva.canvas.event.LayerEvent;

/**
 * An interactor that forwards events to other interactors.
 *
 * @version $Revision$
 * @author John Reekie
 */
public class CompositeInteractor extends AbstractInteractor {

    /** The list of attached interactors
     */
    private ArrayList _interactors = new ArrayList();

    /** The current interactor
     */
    private Interactor _currentInteractor;

    /** Create a new composite interactor. [The following
     * is currently not true: By default, composite
     * interactors do not consume events.]
     */
    public CompositeInteractor () {
        //// FIXME: why was this set false? SelectionInteraction
        //// needs is to be true, shouldn't it always be true?
        //// setConsuming(false);
    }

    /**
     * Accept an event if any attached interactor will accept it.
     */
    public boolean accept (LayerEvent e) {
        for (Iterator i = interactors(); i.hasNext(); ) {
            if (((Interactor) i.next()).accept(e)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add an interactor to this interactor. The added interactor
     * will have events forwarded to it if it accepts the event.
     */
    public void addInteractor (Interactor i) {
        _interactors.add(i);
    }

    /**
     * Return an iteractor over the attached interactors.
     */
    public Iterator interactors () {
        return _interactors.iterator();
    }

    /**
     * Return true if any contained interactor is motion enabled.
     */
    public boolean isMotionEnabled () {
        for (Iterator i = interactors(); i.hasNext(); ) {
            if (((Interactor) i.next()).isMotionEnabled()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handle a mouse drag event. If there's a current interactor
     * receiving events, pass the event to it.
     */
    public void mouseDragged (LayerEvent event) {
        if (!isEnabled()) {
            return;
        }
        if (_currentInteractor != null) {
            _currentInteractor.mouseDragged(event);
        }
        if (isConsuming()) {
            event.consume();
        }
    }

    /**
     * Handle a mouse entered event. If this interactor
     * is not enabled, return immediately. For each interactor, see if
     * it will accept the event, and if it will, pass this event and
     * the subsequent motion and exited events to it.
     */
    public void mouseEntered (LayerEvent event) {
        if (!isMotionEnabled()) {
            return;
        }
        Iterator i = _interactors.iterator();
        while (i.hasNext()) {
            Interactor interactor = (Interactor) i.next();
            if (interactor.accept(event)) {
                _currentInteractor = interactor;
                _currentInteractor.mouseEntered(event);
                break;
            }
        }
        if (isConsuming()) {
            event.consume();
        }
    }

    /**
     * Handle a mouse exited event. If there is a current
     * interactor receiving motion events, pass the event
     * to it.
     */
    public void mouseExited (LayerEvent event) {
        if (!isMotionEnabled()) {
            return;
        }
        if (_currentInteractor != null) {
            _currentInteractor.mouseExited(event);
        }
        if (isConsuming()) {
            event.consume();
        }
        _currentInteractor = null;
    }

    /**
     * Handle a mouse moved event. If there is a current
     * interactor receiving motion events, pass the event
     * to it.
     */
    public void mouseMoved (LayerEvent event) {
        if (!isMotionEnabled()) {
            return;
        }
        if (_currentInteractor != null) {
            _currentInteractor.mouseMoved(event);
        }
        if (isConsuming()) {
            event.consume();
        }
    }

    /**
     * Handle a mouse press event. If this interactor is
     * enabled, call each attached interactor's accept()
     * event.  Pass the event to the first one that accepts
     * the event, and remember it to forward subsequent events
     * to.  Continue this process for all attached interactors until the
     * event is consumed.
     */
    public void mousePressed (LayerEvent event) {
        if (!isEnabled()) {
            return;
        }
        Iterator i = _interactors.iterator();
        Interactor interactor;
        while (i.hasNext()) {
            interactor = (Interactor) i.next();
            if (interactor.accept(event)) {
                _currentInteractor = interactor;
                _currentInteractor.mousePressed(event);
                if (event.isConsumed()) {
                    break;
                }
            }
        }
        if (isConsuming()) {
            event.consume();
        }
    }

    /**
     * Handle a mouse released event. If there's a current interactor
     * receiving events, pass the event to it.
     */
    public void mouseReleased (LayerEvent event) {
        if (!isEnabled()) {
            return;
        }
        if (_currentInteractor != null) {
            _currentInteractor.mouseReleased(event);
        }
        if (isConsuming()) {
            event.consume();
        }
        _currentInteractor = null;
    }

    /**
     * Remove the given interactor from this interactor.
     */
    public void removeInteractor (Interactor i) {
        _interactors.remove(i);
    }
}


