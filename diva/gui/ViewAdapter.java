/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.gui;

/**
 * An adapter for view listener, containing empty method
 * implementations.
 *
 * @author John Reekie  (johnr@eecs.berkeley.edu)
 * @version $Revision$
 */
public class ViewAdapter implements ViewListener {

    /** Invoked when the view is about to be closed.
     */
    public void viewClosing (ViewEvent e) {}

    /** Invoked when the view has been closed.
     */
    public void viewClosed (ViewEvent e) {}

    /** Invoked when the view has been de-selected. Typically, this
     * happens when the view no longer is the front-most view or when
     * it loses the focus.
     */
    public void viewDeselected (ViewEvent e) {}

    /** Invoked when the view has been hidden. This might happend
     * because another view is placed over the top of it, or because
     * an internal frame is iconified.
     */
    public void viewHidden (ViewEvent e) {}

    /** Invoked when the view moves
     */
    public void viewMoved (ViewEvent e) {}

    /** Invoked when the view was resized
     */
    public void viewResized (ViewEvent e) {}

    /** Invoked when the view has been selected. Typically, this
     * happens when the view becomes the front-most view or when it
     * gets the focus.
     */
    public void viewSelected (ViewEvent e) {}

    /** Invoked when the view has been shown. This might happen
     * because an internal frame is deiconified, for example.
     */
    public void viewShown (ViewEvent e) {}
}


