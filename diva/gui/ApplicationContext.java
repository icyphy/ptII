/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.gui;

import java.awt.Component;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.UIManager;

/**
 * A context for an application in the diva.gui infrastructure.  This class
 * represents an AppContext as a JFrame.  Generally, all this class does is
 * to pipe the AppContext method through to the appropriate method in the
 * JFrame class.  In addition, a listener is attached to the frame that
 * fires the context's exit action when a WINDOW_CLOSING event is created.
 * In this class, the default exit action kills the JVM.
 * This class does not contain a menu bar or a toolbar by default.
 *
 * @author Steve Neuendorffer (neuendor@eecs.berkeley.edu)
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @author John Reekie (johnr@eecs.berkeley.edu)
 * @version $Revision$
 */
public class ApplicationContext extends JFrame implements AppContext {
    /**
     * The action that is called when this exits.
     */
    private transient Action _exitAction;

    /**
     * The application that owns this frame
     */
    private transient Application _application;

    /**
     * The window listener for this frame.
     */
    private transient WindowListener _windowListener;

    /**
     * Create a new ApplicationContext.  Add a hook to the frame that will
     * cause the exit action to be fired when a user attempts to close the
     * window.  Set the default close operation of the frame to be
     * DO_NOTHING_ON_CLOSE, since this is handled by the default exit action.
     */
    public ApplicationContext() {
        // Set the system look and feel.
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {}

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // hook the exit action into the window closing.
        addWindowListener(new WindowAdapter () {
                public void windowClosing(WindowEvent e) {
                    if (_exitAction != null) {
                        _exitAction.actionPerformed(null);
                    }
                }
            });
    }

    /**
     * Return the action that is called back when the user
     * exits the app.
     */
    public Action getExitAction() {
        return _exitAction;
    }

    /**
     * Get the image that represents this frame.
     */
    public Image getIconImage () {
        return super.getIconImage();
    }

    /**
     * Return the title of the context.
     */
    public String getTitle () {
        return super.getTitle();
    }

    /**
     * Return the menu bar that the container uses.
     */
    public JMenuBar getJMenuBar() {
        return super.getJMenuBar();
    }

    /**
     * Return whether or not the frame is visible.
     */
    public boolean isVisible() {
        return super.isVisible();
    }

    /**
     * Return "this" as a component.
     */
    public Component makeComponent() {
        return this;
    }

    /**
     * Show the given status string at the bottom of
     * the context.  This base class does nothing.
     */
    public void showStatus(String status) {
    }

    /**
     * Set the frame's visibility.
     */
    public void setVisible(boolean visible) {
        super.setVisible(visible);
    }

    /**
     * Set the action that is called back when the user
     * exits the app.
     */
    public void setExitAction(Action exitAction) {
        _exitAction = exitAction;
    }

    /**
     * Set the title of the context.
     */
    public void setTitle (String title) {
        super.setTitle(title);
    }

    /**
     * Set the icon that represents this frame.
     */
    public void setIconImage (Image image) {
        super.setIconImage(image);
    }

    /**
     * Set the menu bar that the container uses.
     */
    public void setJMenuBar(JMenuBar menu) {
        super.setJMenuBar(menu);
    }
}



