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
 *
 */
package diva.canvas.interactor;

import diva.canvas.event.LayerEvent;
import diva.canvas.event.LayerListener;
import diva.canvas.event.LayerMotionListener;
import diva.canvas.event.MouseFilter;

/**
 * An object that encapsulate the interaction that a figure plays in
 * an interactive application. Typically, all objects of a certain
 * type (nodes in a graph viewer, for example) all have the same
 * interactor given to them, so that they behave the same. Each
 * interactor is thus attached to one or more figures in a canvas.
 *
 * When a mouse event occurs on the figure canvas, the event dispatch
 * code in the figure canvas searches for the top-most figure
 * underneath the mouse, and then searches up the tree of figures
 * looking for an interactor that can handle that event.
 *
 * @version $Id$
 * @author John Reekie
 */
public interface Interactor extends LayerListener, LayerMotionListener {
    /** Test is the interactor is prepared to accept this event.  If
     * so, the event is passed to the interactor for handling.  This
     * method is called only for the mouse-pressed and mouse-entered
     * events. If that event is accepted, other events of the same
     * series (dragged and released, or moved and exited,
     * respectively) are passed to the interactor without calling
     * this method to check. The mouse-entered event is called only
     * if isMotionEnabled() returns true.
     */
    public boolean accept(LayerEvent event);

    /** Get the mouse filter used by this interactor to
     * decide whether to accept an event. The result may
     * be null.
     */
    public MouseFilter getMouseFilter();

    /** Test the consuming flag of this interactor. If this flag is
     * set, the interactor expects is indicating that all events
     * that it will accept should be consumed.
     */
    public boolean isConsuming();

    /** Test the enabled flag of this interactor. If true, the
     * interactor is prepared to handle layer events of all kinds.
     */
    public boolean isEnabled();

    /** Test the motion enabled flag of this interactor. If true, the
     * interactor is prepared to handle layer motion events.
     */
    public boolean isMotionEnabled();

    /** Set the enabled flag of this interactor.  If true, the
     * interactor is prepared to handle layer events. The default
     * setting of this flag should be <b>true</b>.
     */
    public void setEnabled(boolean flag);

    /** Set the mouse filter of this interactor. If a filter is
     * set with this method, the interactor is expected to use the
     * filter within its accept() method.
     */
    public void setMouseFilter(MouseFilter filter);
}
