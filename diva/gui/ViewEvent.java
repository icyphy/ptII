/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.gui;

import javax.swing.JComponent;

/**
 * A view event is generated when a view of a Document is
 * iconified, selected, closed, and so on.
 *
 * @author John Reekie  (johnr@eecs.berkeley.edu)
 * @author Heloise Hse  (hwawen@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public class ViewEvent extends java.util.EventObject {

    /**  The view closing event type
     */
    public static final int VIEW_CLOSING = 0;

    /**  The view closed event type
     */
    public static final int VIEW_CLOSED = VIEW_CLOSING+1;

    /**  The view deselected event type
     */
    public static final int VIEW_DESELECTED = VIEW_CLOSED+1;

    /**  The view hidden event type
     */
    public static final int VIEW_HIDDEN = VIEW_DESELECTED+1;

    /**  The view moved event type
     */
    public static final int VIEW_MOVED = VIEW_HIDDEN+1;

    /**  The view resized event type
     */
    public static final int VIEW_RESIZED = VIEW_MOVED+1;

    /**  The view selected event type
     */
    public static final int VIEW_SELECTED = VIEW_RESIZED+1;

    /**  The view shown event type
     */
    public static final int VIEW_SHOWN = VIEW_SELECTED+1;

    /** The event type.
     * @serial
     */
    private int _id;

    /** Create a view event with the specified component and type.
     * The component is accessible either with the getSource() method or
     * the more type-specific getView() method.
     */
    public ViewEvent (JComponent view, int id) {
        super(view);
        _id = id;
    }

    /** Return the event type.
     */
    public int getID () {
        return _id;
    }

    /** Return the component/view that the event refers to
     */
    public JComponent getView () {
        return (JComponent) getSource();
    }
}


