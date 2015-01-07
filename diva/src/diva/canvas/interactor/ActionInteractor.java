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

import java.awt.event.ActionEvent;

import javax.swing.Action;

import diva.canvas.event.LayerEvent;
import diva.canvas.event.MouseFilter;

/**
 * An interactor that fires an Action when a mouse pressed event occurs.
 *
 * @version $Id$
 * @author Steve Neuendorffer
 */
public class ActionInteractor extends AbstractInteractor {
    // The associated action.
    Action _action = null;

    /** Create a new interactor that will throw a NullPointerException
     *  when a mouse button is pressed.  (In some cases we have to set
     *  the action after creating it.)
     */
    public ActionInteractor() {
        setAction(null);
        setMouseFilter(MouseFilter.defaultFilter);
    }

    /** Create a new interactor that will activate the given action.
     */
    public ActionInteractor(Action action) {
        setAction(action);
        setMouseFilter(MouseFilter.defaultFilter);
    }

    /** Return the action associated with this interactor.
     */
    public Action getAction() {
        return _action;
    }

    /** Activate the action referenced by this interactor.  The source of
     *  the ActionEvent is the layer event.
     */
    @Override
    public void mousePressed(LayerEvent layerEvent) {
        ActionEvent event = new ActionEvent(layerEvent, layerEvent.getID(),
                "Pressed", layerEvent.getModifiers());
        _action.actionPerformed(event);
    }

    /** Set the action associated with this interactor.
     */
    public void setAction(Action action) {
        _action = action;
    }
}
