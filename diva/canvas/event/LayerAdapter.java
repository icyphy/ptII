/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas.event;

/** An adapter for layer listeners. The methods in this class
 * are empty -- the class is provided to make it easier to
 * produce anonymous LayerListeners.
 *
 * @version        $Revision$
 * @author         John Reekie
 */
public class LayerAdapter implements LayerListener {

    /** Invoked when the mouse moves while the button is still held
     * down.
     */
    public void mouseDragged (LayerEvent e) {}

    /** Invoked when the mouse is pressed on a layer or figure.
     */
    public void mousePressed (LayerEvent e) {}

    /** Invoked when the mouse is released on a layer or figure.
     */
    public void mouseReleased (LayerEvent e) {}

    /** Invoked when the mouse is clicked on a layer or figure.
     */
    public void mouseClicked (LayerEvent e) {}
}



