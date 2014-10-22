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
package diva.canvas.interactor;

import java.util.ArrayList;
import java.util.Iterator;

import diva.canvas.event.LayerEvent;

/**
 * An interactor that forwards events to other interactors.
 *
 * @version $Id$
 * @author John Reekie
 */
public class CompositeInteractor extends AbstractInteractor {
    /** The list of attached interactors
     */
    private ArrayList<Interactor> _interactors = new ArrayList<Interactor>();

    /** The current interactor
     */
    private Interactor _currentInteractor;

    /** Create a new composite interactor. [The following
     * is currently not true: By default, composite
     * interactors do not consume events.]
     */
    public CompositeInteractor() {
        //// FIXME: why was this set false? SelectionInteraction
        //// needs is to be true, shouldn't it always be true?
        //// setConsuming(false);
    }

    /**
     * Accept an event if any attached interactor will accept it.
     */
    @Override
    public boolean accept(LayerEvent e) {
        for (Interactor interactor : _interactors) {
            if (interactor.accept(e)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Add an interactor to this interactor. The added interactor
     * will have events forwarded to it if it accepts the event.
     */
    public void addInteractor(Interactor i) {
        _interactors.add(i);
    }

    /**
     * Return an interactor over the attached interactors.
     */
    public Iterator interactors() {
        return _interactors.iterator();
    }

    /**
     * Return true if any contained interactor is motion enabled.
     */
    @Override
    public boolean isMotionEnabled() {
        for (Interactor interactor : _interactors) {
            if (interactor.isMotionEnabled()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Handle a mouse drag event. If there's a current interactor
     * receiving events, pass the event to it.
     */
    @Override
    public void mouseDragged(LayerEvent event) {
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
    @Override
    public void mouseEntered(LayerEvent event) {
        if (!isMotionEnabled()) {
            return;
        }

        for (Interactor interactor : _interactors) {
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
    @Override
    public void mouseExited(LayerEvent event) {
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
    @Override
    public void mouseMoved(LayerEvent event) {
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
    @Override
    public void mousePressed(LayerEvent event) {
        if (!isEnabled()) {
            return;
        }

        for (Interactor interactor : _interactors) {

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
    @Override
    public void mouseReleased(LayerEvent event) {
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
    public void removeInteractor(Interactor i) {
        _interactors.remove(i);
    }
}
