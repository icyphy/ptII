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
 */
package diva.gui;

/**
 * An adapter for view listener, containing empty method
 * implementations.
 *
 * @author John Reekie
 * @version $Id$
 */
public class ViewAdapter implements ViewListener {
    /** Invoked when the view is about to be closed.
     */
    @Override
    public void viewClosing(ViewEvent e) {
    }

    /** Invoked when the view has been closed.
     */
    @Override
    public void viewClosed(ViewEvent e) {
    }

    /** Invoked when the view has been de-selected. Typically, this
     * happens when the view no longer is the front-most view or when
     * it loses the focus.
     */
    @Override
    public void viewDeselected(ViewEvent e) {
    }

    /** Invoked when the view has been hidden. This might happen
     * because another view is placed over the top of it, or because
     * an internal frame is iconified.
     */
    @Override
    public void viewHidden(ViewEvent e) {
    }

    /** Invoked when the view moves
     */
    @Override
    public void viewMoved(ViewEvent e) {
    }

    /** Invoked when the view was resized
     */
    @Override
    public void viewResized(ViewEvent e) {
    }

    /** Invoked when the view has been selected. Typically, this
     * happens when the view becomes the front-most view or when it
     * gets the focus.
     */
    @Override
    public void viewSelected(ViewEvent e) {
    }

    /** Invoked when the view has been shown. This might happen
     * because an internal frame is deiconified, for example.
     */
    @Override
    public void viewShown(ViewEvent e) {
    }
}
