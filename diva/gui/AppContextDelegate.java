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

import java.awt.Component;
import java.awt.Container;
import java.awt.Image;

import javax.swing.Action;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JRootPane;

/**
 * This class provides basic support for an instance of AppContext which
 * delegates its operation to one of the basic AppContexts (usually an
 * AppContext or an ApplicationContext.)  This is similar to an interface
 * adapter, except the default implementation of each method is to
 * call the identical method on the delegate context.
 *
 * @author Steve Neuendorffer
 * @version $Id$
 */
public class AppContextDelegate implements AppContext {
    /** The app-context that implements the windowing facilities.
     */
    private transient AppContext _delegate;

    /** Create a new app context that delegates to the given context.
     */
    public AppContextDelegate(AppContext context) {
        _delegate = context;
    }

    /**
     * Returns the contentPane of the delegate.
     */
    @Override
    public Container getContentPane() {
        return _delegate.getContentPane();
    }

    /**
     * Return the context delegate.
     */
    public AppContext getDelegate() {
        return _delegate;
    }

    /**
     * Returns the glassPane of the delegate.
     */
    @Override
    public Component getGlassPane() {
        return _delegate.getGlassPane();
    }

    /**
     * Returns the layeredPane of the delegate.
     */
    @Override
    public JLayeredPane getLayeredPane() {
        return _delegate.getLayeredPane();
    }

    /**
     * Returns the rootPane of the delegate.
     */
    @Override
    public JRootPane getRootPane() {
        return _delegate.getRootPane();
    }

    /**
     * Return the title of the context.
     */
    @Override
    public String getTitle() {
        return _delegate.getTitle();
    }

    /**
     * Set the content pane of the delegate.  The "contentPane" is the
     * primary container for application specific components.
     */
    @Override
    public void setContentPane(Container contentPane) {
        _delegate.setContentPane(contentPane);
    }

    /**
     * Set the glassPane of the delegate.  The glassPane is always the
     * first child of the rootPane and the rootPanes layout manager
     * ensures that it's always as big as the rootPane.
     */
    @Override
    public void setGlassPane(Component glassPane) {
        _delegate.setGlassPane(glassPane);
    }

    /**
     * Set the layered pane of the delegate.  A Container that manages
     * the contentPane and in some cases a menu bar
     */
    @Override
    public void setLayeredPane(JLayeredPane layeredPane) {
        _delegate.setLayeredPane(layeredPane);
    }

    /**
     * Set the exit action of the delegate.
     */
    @Override
    public void setExitAction(Action action) {
        _delegate.setExitAction(action);
    }

    /**
     * Return the exit action of the delegate.
     */
    @Override
    public Action getExitAction() {
        return _delegate.getExitAction();
    }

    /**
     * Set the image icon of the delegate.
     */
    @Override
    public void setIconImage(Image image) {
        _delegate.setIconImage(image);
    }

    /**
     * Return the image icon of the delegate.
     */
    @Override
    public Image getIconImage() {
        return _delegate.getIconImage();
    }

    /**
     * Return the menu bar of the delegate.
     */
    @Override
    public JMenuBar getJMenuBar() {
        return _delegate.getJMenuBar();
    }

    /**
     * Set the menu bar of the delegate.
     */
    @Override
    public void setJMenuBar(JMenuBar menu) {
        _delegate.setJMenuBar(menu);
    }

    /**
     * Show the status in the delegate.
     */
    @Override
    public void showStatus(String status) {
        _delegate.showStatus(status);
    }

    /**
     * Set the size in the delegate.
     */
    @Override
    public void setSize(int w, int h) {
        _delegate.setSize(w, h);
    }

    /**
     * Set the title of the context.  This has no significance in an
     * applet context.
     */
    @Override
    public void setTitle(String title) {
        _delegate.setTitle(title);
    }

    /**
     * Invoke the delegate's setvisible().
     */
    @Override
    public void setVisible(boolean visible) {
        _delegate.setVisible(visible);
    }

    /**
     * Invoke the delegate's isvisible().
     */
    @Override
    public boolean isVisible() {
        return _delegate.isVisible();
    }

    /**
     * Call makeComponent() on the delegate.
     */
    @Override
    public Component makeComponent() {
        return _delegate.makeComponent();
    }
}
