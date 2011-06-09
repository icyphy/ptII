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
 */
package diva.gui;

/**
 * A listener interface for receiving events on views. A _view_ is any
 * JComponent that contains a view into a Document. Typically, views
 * might be opened, closed, iconified, and so on. Although there are
 * various listeners in AWT and Swing that capture these events, they
 * are all specific to certain widgets, whereas the ViewListener
 * interface abstracts from specific widgets.
 *
 * <p> This interface is partly based on the Swing InternalFrameListener
 * interface.
 *
 * @author Heloise Hse
 * @author John Reekie
 * @version $Id$
 */
public interface ViewListener extends java.util.EventListener {
    /** Invoked when the view has been closed.
     */
    public void viewClosed(ViewEvent e);

    /** Invoked when the view is about to be closed
     */
    public void viewClosing(ViewEvent e);

    /** Invoked when the view has been de-selected. Typically, this
     * happens when the view no longer is the front-most view or when
     * it loses the focus.
     */
    public void viewDeselected(ViewEvent e);

    /** Invoked when the view has been hidden. This might happen
     * because another view is placed over the top of it, or because
     * an internal frame is iconified.
     */
    public void viewHidden(ViewEvent e);

    /** Invoked when the view moves
     */
    public void viewMoved(ViewEvent e);

    /** Invoked when the view was resized
     */
    public void viewResized(ViewEvent e);

    /** Invoked when the view has been selected. Typically, this
     * happens when the view becomes the front-most view or when it
     * gets the focus.
     */
    public void viewSelected(ViewEvent e);

    /** Invoked when the view has been shown. This might happen
     * because an internal frame is deiconified, for example.
     */
    public void viewShown(ViewEvent e);
}
