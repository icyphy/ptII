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

import javax.swing.Icon;
import javax.swing.JComponent;

/**
 * An app context that provides methods for multiple
 * documents and views.  [FIXME: more docs]
 *
 * @author John Reekie
 * @version $Id$
 */
public interface MDIContext extends AppContext {
    /** Add a new content pane to this frame. Generally, a client
     * should call setCurrentPane() after calling this method.
     */
    public void addContentPane(String title, JComponent pane);

    /** Add a view listener to this frame. These listeners will be
     * notified when views are modified. Generally, an application
     * will add itself to this listener list, as it is an easy way of
     * getting notification about operations on the content panes.
     */
    public void addViewListener(ViewListener listener);

    /** Get the content pane that is displayed as the current content
     * pane.
     */
    public JComponent getCurrentContentPane();

    /** Get the icon that is displayed in
     * internal frames.
     */
    public Icon getFrameIcon();

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
    public void removeContentPane(JComponent pane);

    /** Remove a view listener from this frame.
     */
    public void removeViewListener(ViewListener listener);

    /** Set the given content pane to be displayed as the current
     * content pane.
     */
    public void setCurrentContentPane(JComponent pane);

    /** Set the icon that is displayed in
     * internal frames.
     */
    public void setFrameIcon(Icon icon);
}
