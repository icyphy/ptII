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

import javax.swing.JComponent;

/**
 * A view event is generated when a view of a Document is
 * iconified, selected, closed, and so on.
 *
 * @author John Reekie
 * @author Heloise Hse
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public class ViewEvent extends java.util.EventObject {
    /**  The view closing event type
     */
    public static final int VIEW_CLOSING = 0;

    /**  The view closed event type
     */
    public static final int VIEW_CLOSED = VIEW_CLOSING + 1;

    /**  The view deselected event type
     */
    public static final int VIEW_DESELECTED = VIEW_CLOSED + 1;

    /**  The view hidden event type
     */
    public static final int VIEW_HIDDEN = VIEW_DESELECTED + 1;

    /**  The view moved event type
     */
    public static final int VIEW_MOVED = VIEW_HIDDEN + 1;

    /**  The view resized event type
     */
    public static final int VIEW_RESIZED = VIEW_MOVED + 1;

    /**  The view selected event type
     */
    public static final int VIEW_SELECTED = VIEW_RESIZED + 1;

    /**  The view shown event type
     */
    public static final int VIEW_SHOWN = VIEW_SELECTED + 1;

    /** The event type.
     * @serial
     */
    private int _id;

    /** Create a view event with the specified component and type.
     * The component is accessible either with the getSource() method or
     * the more type-specific getView() method.
     */
    public ViewEvent(JComponent view, int id) {
        super(view);
        _id = id;
    }

    /** Return the event type.
     */
    public int getID() {
        return _id;
    }

    /** Return the component/view that the event refers to
     */
    public JComponent getView() {
        return (JComponent) getSource();
    }
}
