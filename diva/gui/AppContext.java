/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
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
 * @author Steve Neuendorffer (neuendor@eecs.berkeley.edu)
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Revision$
 */
public interface AppContext extends RootPaneContainer {

    /**
     * Return the action that is called back when the user
     * exits the app.
     */
    public Action getExitAction();

    /** Get the image that represents this frame.
     */
    public Image getIconImage ();

    /**
     * Return the menu bar that the container uses.
     */
    public JMenuBar getJMenuBar();

    /**
     * Return the title of the context.
     */
    public String getTitle ();

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
    public void setIconImage (Image image);

    /**
     * Set the menu bar that the container uses.
     */
    public void setJMenuBar(JMenuBar menu);

    /**
     *  Set the title of the context.
     */
    public void setTitle (String title);

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



