/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.util.jester;

import java.awt.event.*;
import java.awt.Component;
import java.util.ArrayList;

/**
 * EventRecorder listens to all of the mouse and keyboard events
 * on a given component and records these into a sequence which
 * can then be played back using an EventPlayer object.
 *
 * @see EventPlayer
 * @author Michael Shilman      (michaels@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public class EventRecorder {
    /**
     * The component that we are recording from.
     */
    private Component _component;

    /**
     * Store the event sequence here.
     */
    private ArrayList _events = new ArrayList();

    /**
     * Record mouse events.
     */
    private MouseRecorder _mouseRecorder = new MouseRecorder();

    /**
     * Record mouse motion events.
     */
    private MouseMotionRecorder _mouseMotionRecorder = new MouseMotionRecorder();

    /**
     * Record keyboard events.
     */
    private KeyRecorder _keyRecorder = new KeyRecorder();

    /**
     * Record events from the given component.
     */
    public EventRecorder(Component component) {
        _component = component;
    }

    /**
     * Start recording events on the constructor-specified
     * component.
     */
    public void record() {
        _component.addMouseListener(_mouseRecorder);
        _component.addMouseMotionListener(_mouseMotionRecorder);
        _component.addKeyListener(_keyRecorder);
    }

    /**
     * Stop recording events on the constructor-specified
     * component and return the recorded events from this
     * session as an array.
     */
    public synchronized InputEvent[] stop() {
        _component.removeMouseListener(_mouseRecorder);
        _component.removeMouseMotionListener(_mouseMotionRecorder);
        _component.removeKeyListener(_keyRecorder);
        InputEvent[] out = new InputEvent[_events.size()];
        _events.toArray(out);
        return out;
    }

    /**
     * Record all mouse events.
     */
    private class MouseRecorder implements MouseListener {
        public void mouseClicked(MouseEvent e) {
            record(e);
        }
        public void mouseEntered(MouseEvent e) {
            record(e);
        }
        public void mouseExited(MouseEvent e) {
            record(e);
        }
        public void mousePressed(MouseEvent e) {
            record(e);
        }
        public void mouseReleased(MouseEvent e) {
            record(e);
        }
        private void record(MouseEvent e) {
            _events.add(e);
        }
    }

    /**
     * Record all mouse motion events.
     */
    private class MouseMotionRecorder implements MouseMotionListener {
        public void mouseDragged(MouseEvent e) {
            record(e);
        }
        public void mouseMoved(MouseEvent e) {
            record(e);
        }
        private void record(MouseEvent e) {
            _events.add(e);
        }
    }

    /**
     * Record all keyboard events.
     */
    private class KeyRecorder implements KeyListener {
        public void keyPressed(KeyEvent e) {
            record(e);
        }
        public void keyReleased(KeyEvent e) {
            record(e);
        }
        public void keyTyped(KeyEvent e) {
            record(e);
        }
        private void record(KeyEvent e) {
            _events.add(e);
        }
    }
}

