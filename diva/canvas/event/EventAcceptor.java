/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas.event;
import diva.canvas.CanvasComponent;
import java.awt.AWTEvent;

/** An event acceptor is a canvas component object that is prepared to accept
 *  an AWT event, and then possibly process and consume it. This
 *  interface is also used as a tagging interface so that event-processing
 *  routines can tell whether certain objects are smart enough
 *  to handle events for themselves.
 *
 * @version        $Revision$
 * @author         John Reekie
 */
public interface EventAcceptor extends CanvasComponent {

    /** Test the enabled flag of this object. If true, the object
     * is prepared to handle events with processEvent().
     */
    public boolean isEnabled ();

    /** Set the enabled flag of this object. If true, the object
     * is prepared to handle events with processEvent().
     */
    public void setEnabled (boolean flag);

    /** Dispatch an AWT event within this component.  The implementing
     * object should test if it is enabled first, and return if not.
     * Otherwise, it should handle the event in whatever way it thinks
     * appropriate.
     */
    public void dispatchEvent (AWTEvent event);
}


