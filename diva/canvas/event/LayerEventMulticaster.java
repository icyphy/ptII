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
package diva.canvas.event;

import java.awt.AWTEventMulticaster;
import java.util.EventListener;

/** A subclass of the AWT event multi-caster, which adds support
 * for layer events.
 *
 * @version        $Id$
 * @author         John Reekie
 */
public class LayerEventMulticaster extends AWTEventMulticaster implements
        LayerListener, LayerMotionListener {
    /**
     * Create an event multicaster from two listeners.
     */
    protected LayerEventMulticaster(EventListener a, EventListener b) {
        super(a, b);
    }

    /** Invoked when the mouse moves while the button is still held
     * down.
     */
    @Override
    public void mouseDragged(LayerEvent e) {
        ((LayerListener) a).mouseDragged(e);
        ((LayerListener) b).mouseDragged(e);
    }

    /** Invoked when the mouse enters a layer or figure.
     */
    @Override
    public void mouseEntered(LayerEvent e) {
        ((LayerMotionListener) a).mouseEntered(e);
        ((LayerMotionListener) b).mouseEntered(e);
    }

    /** Invoked when the mouse exits a layer or figure.
     */
    @Override
    public void mouseExited(LayerEvent e) {
        ((LayerMotionListener) a).mouseExited(e);
        ((LayerMotionListener) b).mouseExited(e);
    }

    /** Invoked when the mouse moves while over a layer or figure.
     */
    @Override
    public void mouseMoved(LayerEvent e) {
        ((LayerMotionListener) a).mouseExited(e);
        ((LayerMotionListener) b).mouseExited(e);
    }

    /** Invoked when the mouse is pressed on a layer or figure.
     */
    @Override
    public void mousePressed(LayerEvent e) {
        ((LayerListener) a).mousePressed(e);
        ((LayerListener) b).mousePressed(e);
    }

    /** Invoked when the mouse is released on a layer or figure.
     */
    @Override
    public void mouseReleased(LayerEvent e) {
        ((LayerListener) a).mouseReleased(e);
        ((LayerListener) b).mouseReleased(e);
    }

    /** Invoked when the mouse is clicked on a layer or figure.
     */
    @Override
    public void mouseClicked(LayerEvent e) {
        ((LayerListener) a).mouseClicked(e);
        ((LayerListener) b).mouseClicked(e);
    }

    /**
     * Adds layer-listener-a with layer-listener-b and
     * returns the resulting multicast listener.
     */
    public static LayerListener add(LayerListener a, LayerListener b) {
        return (LayerListener) addInternal(a, b);
    }

    /**
     * Returns the resulting multicast listener from adding listener-a
     * and listener-b together.
     * If listener-a is null, it returns listener-b;
     * If listener-b is null, it returns listener-a
     * If neither are null, then it creates and returns
     * a new AWTEventMulticaster instance which chains a with b.
     * @param a event listener-a
     * @param b event listener-b
     */
    protected static EventListener addInternal(EventListener a, EventListener b) {
        if (a == null) {
            return b;
        }

        if (b == null) {
            return a;
        }

        return new LayerEventMulticaster(a, b);
    }

    /**
     * Adds layer-motion-listener-a with layer-motion-listener-b and
     * returns the resulting multicast listener.
     */
    public static LayerMotionListener add(LayerMotionListener a,
            LayerMotionListener b) {
        return (LayerMotionListener) addInternal(a, b);
    }

    /**
     * Removes the old layer-listener from layer-listener-l and
     * returns the resulting multicast listener.
     */
    public static LayerListener remove(LayerListener l, LayerListener oldl) {
        return (LayerListener) removeInternal(l, oldl);
    }

    /**
     * Removes the old layer-motion-listener from layer-motion-listener-l and
     * returns the resulting multicast listener.
     */
    public static LayerMotionListener remove(LayerMotionListener l,
            LayerMotionListener oldl) {
        return (LayerMotionListener) removeInternal(l, oldl);
    }
}
