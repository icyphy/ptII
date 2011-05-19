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

import java.awt.Component;
import java.awt.Image;

import javax.swing.Action;
import javax.swing.JMenuBar;
import javax.swing.RootPaneContainer;

/**
 * A context for either an application or an applet
 * that wants to use the diva.gui infrastructure.
 *
 * @author Steve Neuendorffer
 * @author Michael Shilman
 * @version $Id$
 */
public interface AppContext extends RootPaneContainer {
    /**
     * Return the action that is called back when the user
     * exits the app.
     */
    public Action getExitAction();

    /** Get the image that represents this frame.
     */
    public Image getIconImage();

    /**
     * Return the menu bar that the container uses.
     */
    public JMenuBar getJMenuBar();

    /**
     * Return the title of the context.
     */
    public String getTitle();

    /**
     * Return whether the context is visible.
     */
    public boolean isVisible();

    /**
     * Make this into a component (since Component is not an
     * interface.
     */
    public Component makeComponent();

    /**
     * Set the action that is called back when the user
     * exits the app.
     */
    public void setExitAction(Action exitAction);

    /** Set the icon that represents this frame.
     */
    public void setIconImage(Image image);

    /**
     * Set the menu bar that the container uses.
     */
    public void setJMenuBar(JMenuBar menu);

    /**
     *  Set the title of the context.
     */
    public void setTitle(String title);

    /**
     * Set the size of the context.  Won't do
     * anything for an applet.
     */
    public void setSize(int w, int h);

    /**
     * Set whether the context is visible.  May be
     * meaningless if the context is always visible.
     */
    public void setVisible(boolean visible);

    /**
     * Show the given status string at the bottom of
     * the context.  Note: application implementation
     * may implement this as an empty method.
     */
    public void showStatus(String status);
}
