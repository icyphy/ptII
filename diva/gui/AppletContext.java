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
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JApplet;
import javax.swing.JMenuBar;
import javax.swing.UIManager;


/**
 * A context for an applet in the diva.gui infrastructure.
 * Generally, all this class does is
 * to pipe the AppContext method through to the appropriate method in the
 * JFrame class.  The exit action will be fired when the applet's
 * destroy method is executed.  The default exit action does nothing.
 *
 * @author Steve Neuendorffer
 * @author Michael Shilman
 * @version $Id$
 */
public class AppletContext extends JApplet implements AppContext {
    /**
     * The action that is called when this exits.
     */
    private transient Action _exitAction;

    /**
     * The application that owns this frame
     */
    private transient Application _application;

    /**
     * The icon that is displayed in internal frames
     */
    private transient Image _iconImage;

    /**
     * The title of the context.
     */
    private transient String _title;

    /**
     * Create a new context and set the exit action to do nothing.
     */
    public AppletContext() {
        _exitAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    //do nothing?
                }
            };

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
        }
    }

    /**
     * Override the superclass's destroy method
     * to call the user-specified exit action.
     */
    public void destroy() {
        _exitAction.actionPerformed(null);
        super.destroy();
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
    public Image getIconImage() {
        return _iconImage;
    }

    /**
     * Return the title of the context.
     */
    public String getTitle() {
        return _title;
    }

    /**
     * Return the menu bar that the container uses.
     */
    public JMenuBar getJMenuBar() {
        return super.getJMenuBar();
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
        super.showStatus(status);
    }

    /**
     * Set the action that is called back when the user
     * exits the app.
     */
    public void setExitAction(Action exitAction) {
        _exitAction = exitAction;
    }

    /**
     * Set the title of the context.  This has no significance in an
     * applet context.
     */
    public void setTitle(String title) {
        _title = title;
    }

    /**
     * Set the icon that represents this frame.
     */
    public void setIconImage(Image image) {
        _iconImage = image;
    }

    /**
     * Set the menu bar that the container uses.
     */
    public void setJMenuBar(JMenuBar menu) {
        super.setJMenuBar(menu);
    }

    /**
     * Do nothing.
     */
    public void setSize(int w, int h) {
    }

    /**
     * Do nothing.  Applets are always visible.
     */
    public void setVisible(boolean visible) {
    }

    /**
     * Return true.  Applets are always visible.
     */
    public boolean isVisible() {
        return true;
    }
}
