/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.util.jester;

import java.awt.event.*;
import java.awt.Component;

/**
 * EventPlayer uses the java.awt.robot API to inject streams of events
 * into a component.  An event player is instantiated on a given
 * component and then passes events to that component every time its
 * <code>play()</code> method is called.
 *
 * @see EventRecorder
 * @author Michael Shilman      (michaels@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
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
        for(int i = 0; i < events.length; i++) {
            //            System.out.println("Dispatching: " + events[i]);
            _component.dispatchEvent(events[i]);
        }
    }
}











