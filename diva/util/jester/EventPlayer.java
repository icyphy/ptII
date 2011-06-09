/*
 Copyright (c) 1998-2005 The Regents of the University of California
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
package diva.util.jester;

import java.awt.Component;
import java.awt.event.InputEvent;

/**
 * EventPlayer uses the java.awt.robot API to inject streams of events
 * into a component.  An event player is instantiated on a given
 * component and then passes events to that component every time its
 * <code>play()</code> method is called.
 *
 * @see EventRecorder
 * @author Michael Shilman
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public class EventPlayer {
    /**
     * The component that we are playing into.
     */
    private Component _component;

    /**
     * Play events into the given component.
     */
    public EventPlayer(Component component) {
        _component = component;
    }

    /**
     * Play the given sequence of events into
     * the component.
     */
    public void play(InputEvent[] events) {
        for (int i = 0; i < events.length; i++) {
            //            System.out.println("Dispatching: " + events[i]);
            _component.dispatchEvent(events[i]);
        }
    }
}
