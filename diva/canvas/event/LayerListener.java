/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas.event;

/** The interface for listeners that respond to mouse clicks and
 * drags.  Unlike the AWT MouseListener interface, this interface does
 * not include the enter and leave events, but does include the drag
 * event, for performance reasons.
 *
 * @version        $Revision$
 * @author         John Reekie
 */
public interface LayerListener extends java.util.EventListener {

    /** Invoked when the mouse moves while the button is still held
     * down.
     */
    void mouseDragged (LayerEvent e);

    /** Invoked when the mouse is pressed on a layer or figure.
     */
    void mousePressed (LayerEvent e);

    /** Invoked when the mouse is released on a layer or figure.
     */
    void mouseReleased (LayerEvent e);

    /** Invoked when the mouse is clicked on a layer or figure.
     */
    void mouseClicked (LayerEvent e);
}



