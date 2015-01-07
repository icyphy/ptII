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

import diva.canvas.event.LayerEvent;
import diva.canvas.event.MouseFilter;

/**
 * An abstract class that implements Interactor. This class provides
 * simple implementations of most of the required methods of
 * interactors. The listener methods are all implemented,
 * to make it easier to implement interactor subclasses.
 *
 * @version $Id$
 * @author John Reekie
 */
public abstract class AbstractInteractor implements Interactor {
    /** The consuming flag.
     */
    private boolean _consuming = true;

    /** The flag that says that layer events are enabled
     */
    private boolean _enabled = true;

    /** The flag that says that layer motion events are enabled
     */
    private boolean _motionEnabled = false;

    /** The mouse filter
     */
    private MouseFilter _mouseFilter = MouseFilter.defaultFilter;

    /** Test if the interactor accepts the given event. This default
     * implementation returns true if the interactor is enabled
     * and the mouse filter accepts the event.
     */
    @Override
    public boolean accept(LayerEvent event) {
        return isEnabled() && getMouseFilter().accept(event);
    }

    /** Get the mouse filter used by this interactor to
     * decide whether to accept an event. The result may
     * be null.
     */
    @Override
    public MouseFilter getMouseFilter() {
        return _mouseFilter;
    }

    /** Test the consuming flag of this interactor. If this flag is
     * set, the interactor consumes all input events that get past its
     * mouse filter.
     */
    @Override
    public boolean isConsuming() {
        return _consuming;
    }

    /** Test the enabled flag of this interactor. If true, the interactor
     * is prepared to handle layer events.
     */
    @Override
    public boolean isEnabled() {
        return _enabled;
    }

    /** Test the motion enabled flag of this interactor. If true, the
     * interactor is prepared to handle layer motion events.
     */
    @Override
    public boolean isMotionEnabled() {
        return _motionEnabled;
    }

    /** Do nothing.
     */
    @Override
    public void mouseDragged(LayerEvent layerEvent) {
        // empty
    }

    /** Do nothing.
     */
    @Override
    public void mouseEntered(LayerEvent layerEvent) {
        // empty
    }

    /** Do nothing.
     */
    @Override
    public void mouseExited(LayerEvent layerEvent) {
        // empty
    }

    /** Do nothing.
     */
    @Override
    public void mouseMoved(LayerEvent layerEvent) {
        // empty
    }

    /** Do nothing.
     */
    @Override
    public void mousePressed(LayerEvent layerEvent) {
        // empty
    }

    /** Do nothing.
     */
    @Override
    public void mouseReleased(LayerEvent layerEvent) {
        // empty
    }

    /** Do nothing.
     */
    @Override
    public void mouseClicked(LayerEvent layerEvent) {
        //empty
    }

    /** Set the consuming flag of this layer.  If this flag is
     * set, the interactor consumes all events that get past its mouse
     * filter. By default, the flag is true.
     */
    public void setConsuming(boolean flag) {
        _consuming = flag;
    }

    /** Set the enabled flag of this interactor.  If true, the interactor
     * is prepared to handle layer events. The default setting
     * of this flag is <b>true</b>.
     */
    @Override
    public void setEnabled(boolean flag) {
        _enabled = flag;
    }

    /** Set the motion enabled flag of this interactor.  If true, the
     * interactor is prepared to handle layer motion events. The default
     * setting of this flag is <b>false</b>.
     */
    protected void setMotionEnabled(boolean flag) {
        _motionEnabled = flag;
    }

    /** Set the mouse filter of this interactor.
     */
    @Override
    public void setMouseFilter(MouseFilter filter) {
        _mouseFilter = filter;
    }
}
