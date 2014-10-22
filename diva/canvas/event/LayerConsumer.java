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

/** An adapter for layer listeners that consumes events. This
 * should be used instead of LayerAdapter in situations in which
 * a subclass overrides just one or two methods, but consumes the
 * event in those methods, and require that they be consumed in
 * the other methods as well.
 *
 * @version        $Id$
 * @author         John Reekie
 */
public class LayerConsumer implements LayerListener {
    /** Invoked when the mouse moves while the button is still held
     * down.
     */
    @Override
    public void mouseDragged(LayerEvent e) {
        e.consume();
    }

    /** Invoked when the mouse is pressed on a layer or figure.
     */
    @Override
    public void mousePressed(LayerEvent e) {
        e.consume();
    }

    /** Invoked when the mouse is released on a layer or figure.
     */
    @Override
    public void mouseReleased(LayerEvent e) {
        e.consume();
    }

    /** Invoked when the mouse is clicked on a layer or figure.
     */
    @Override
    public void mouseClicked(LayerEvent e) {
        e.consume();
    }
}
