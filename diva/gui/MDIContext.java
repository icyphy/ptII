/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.gui;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.event.EventListenerList;

/**
 * An app context that provides methods for multiple
 * documents and views.  [FIXME: more docs]
 *
 * @author John Reekie (johnr@eecs.berkeley.edu)
 * @version $Revision$
 */
public interface MDIContext extends AppContext {
   /** Add a new content pane to this frame. Generally, a client
     * should call setCurrentPane() after calling this method.
     */
    public void addContentPane (String title, JComponent pane);

    /** Add a view listener to this frame. These listeners will be
     * notified when views are modified. Generally, an application
     * will add itself to this listener list, as it is an easy way of
     * getting notification about operations on the content panes.
     */
    public void addViewListener (ViewListener listener);

    /** Get the content pane that is displayed as the current content
     * pane.
     */
    public JComponent getCurrentContentPane ();

    /** Get the icon that is displayed in
     * internal frames.
     */
    public Icon getFrameIcon ();

    /** Get the event listener list. Subclasses should use this to get
     * an event listener list to which they can add their own
     * listeners.
     *
     * FIXME: do we need this in the interface??
     *
    public EventListenerList getViewListenerList ();
    */

    /** Remove the given content pane from the display and close.
     */
    public void removeContentPane (JComponent pane);

    /** Remove a view listener from this frame.
     */
    public void removeViewListener (ViewListener listener);

    /** Set the given content pane to be displayed as the current
     * content pane.
     */
    public void setCurrentContentPane (JComponent pane);

    /** Set the icon that is displayed in
     * internal frames.
     */
    public void setFrameIcon (Icon icon);
}


