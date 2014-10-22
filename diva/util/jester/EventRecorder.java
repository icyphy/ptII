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
package diva.util.jester;

import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

/**
 * EventRecorder listens to all of the mouse and keyboard events
 * on a given component and records these into a sequence which
 * can then be played back using an EventPlayer object.
 *
 * @see EventPlayer
 * @author Michael Shilman
 * @version $Id$
 * @Pt.AcceptedRating Red
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
        @Override
        public void mouseClicked(MouseEvent e) {
            record(e);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            record(e);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            record(e);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            record(e);
        }

        @Override
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
        @Override
        public void mouseDragged(MouseEvent e) {
            record(e);
        }

        @Override
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
        @Override
        public void keyPressed(KeyEvent e) {
            record(e);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            record(e);
        }

        @Override
        public void keyTyped(KeyEvent e) {
            record(e);
        }

        private void record(KeyEvent e) {
            _events.add(e);
        }
    }
}
