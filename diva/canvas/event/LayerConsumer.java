/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas.event;

/** An adapter for layer listeners that consumes events. This
 * should be used instead of LayerAdapter in situations in which
 * a subclass overrides just one or two methods, but consumes the
 * event in those methods, and require that they be consumed in
 * the other methods as well.
 *
 * @version        $Revision$
 * @author         John Reekie
 */
public class LayerConsumer implements LayerListener {

    /** Invoked when the mouse moves while the button is still held
     * down.
     */
    public void mouseDragged (LayerEvent e) {
        e.consume();
    }

    /** Invoked when the mouse is pressed on a layer or figure.
     */
    public void mousePressed (LayerEvent e) {
        e.consume();
    }

    /** Invoked when the mouse is released on a layer or figure.
     */
    public void mouseReleased (LayerEvent e) {
        e.consume();
    }

    /** Invoked when the mouse is clicked on a layer or figure.
     */
    public void mouseClicked (LayerEvent e) {
        e.consume();
    }
}



