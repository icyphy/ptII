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
package diva.canvas.event;

import java.awt.AWTEvent;

import diva.canvas.CanvasComponent;

/** An event acceptor is a canvas component object that is prepared to accept
 *  an AWT event, and then possibly process and consume it. This
 *  interface is also used as a tagging interface so that event-processing
 *  routines can tell whether certain objects are smart enough
 *  to handle events for themselves.
 *
 * @version        $Id$
 * @author         John Reekie
 */
public interface EventAcceptor extends CanvasComponent {

    /** Test the enabled flag of this object. If true, the object
     *  is prepared to handle events with processEvent().
     *  @return true if the object is prepared to handle events
     *  with processEvent()
     */
    public boolean isEnabled();

    /** Set the enabled flag of this object. If true, the object
     *  is prepared to handle events with processEvent().
     *  @param flag True if the object is prepared to handle
     *  events with processEvent();
     */
    public void setEnabled(boolean flag);

    /** Dispatch an AWT event within this component.  The implementing
     *  object should test if it is enabled first, and return if not.
     *  Otherwise, it should handle the event in whatever way it thinks
     *  appropriate.
     *  @param the event.
     */
    public void dispatchEvent(AWTEvent event);
}
