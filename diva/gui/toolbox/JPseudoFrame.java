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
package diva.gui.toolbox;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;


/**
 * JPseudoFrame is not a Frame at all, but a widget that enables a
 * JInternalFrame to be maximized in the manner exhibited by Windows
 * applications. By default, if you use a JDesktopPane and maximize
 * one of the internal frames, the internal frame becomes the size of
 * the desktop pane, but it still has a full title bar. Not to put too
 * fine a point on it, this sucks, especially since Swing (as of
 * JDK1.2.1) completely screws up if the desktop pane happens to be
 * inside of a JSplitPane.
 *
 * <p> Instead, what you want is for the contents of the internal
 * frame to <i>replace</i> the desktop pane, and to have the window
 * iconify, minimize, and close buttons appear on the right-hand end
 * of the menu bar.  (Look at Word, Visio etc on Windows.)  So what
 * JPseudoFrame does is give you a means to do exactly that. The
 * actual setup is a little complicated because the pseudo-frame is
 * not part of the containment hierarchy, and the method setContent()
 * must be overridden to place a component in the correct place in the
 * hierarchy.
 *
 * <P> To be notified of events occurring in the frame, attach an
 * action listener. Pressing the buttons will cause action events to
 * be fired with the command strings "iconified", "minimized", or
 * "closed."  By the time that these events are generated, the
 * pseudo-frame will already have removed itself from the menu
 * bar. (Although we could perhaps generate InternalFrameEvents, this
 * is not really necessary and so we decided against on on the grounds
 * of getting this done....)
 *
 * @author John Reekie
 * @version $Id$
 */
public abstract class JPseudoFrame extends JComponent {
    /** Some glue
     */
    private transient Component _glue = Box.createGlue();

    /** The dimension is fixed
     */
    private transient Dimension _dim = new Dimension(52, 18);

    /** Three buttons
     */
    private transient JButton _iconifyButton;

    /** Three buttons
     */
    private transient JButton _minimizeButton;

    /** Three buttons
     */
    private transient JButton _closeButton;

    /** The menubar to draw in
     */
    private transient JMenuBar _menubar;

    /** The current content pane, if there is one
     */
    private transient JComponent _component = null;

    /** The desktop pane we are going to swap in and out.
     */
    private transient JDesktopPane _desktopPane;

    /** The internal frame that the current content pane belongs
     * to.
     */
    private transient JInternalFrame _internalFrame;

    /** The event listeners
     */
    private transient EventListenerList _listeners = new EventListenerList();

    /** Construct a new PseudoFrame which will steal layout from the
     * given desktop and draw itself in the given JMenuBar.
     */
    public JPseudoFrame(JDesktopPane desktopPane, JMenuBar menubar) {
        this._desktopPane = desktopPane;
        this._menubar = menubar;

        setLayout(null);

        Hashtable ui = UIManager.getLookAndFeelDefaults();
        _iconifyButton = new JButton((Icon) ui.get("InternalFrame.iconifyIcon"));
        _iconifyButton.setBounds(0, 2, 16, 14);
        _iconifyButton.setRequestFocusEnabled(false);
        _iconifyButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        setIcon(true);
                    } catch (PropertyVetoException ex) {
                    }

                    fireInternalAction("iconified");
                }
            });
        add(_iconifyButton);

        _minimizeButton = new JButton((Icon) ui.get(
                                              "InternalFrame.minimizeIcon"));
        _minimizeButton.setBounds(16, 2, 16, 14);
        _minimizeButton.setRequestFocusEnabled(false);
        _minimizeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        setMaximum(false);
                    } catch (PropertyVetoException ex) {
                    }

                    fireInternalAction("minimized");
                }
            });
        add(_minimizeButton);

        _closeButton = new JButton((Icon) ui.get("InternalFrame.closeIcon"));
        _closeButton.setBounds(34, 2, 16, 14);
        _closeButton.setRequestFocusEnabled(false);
        _closeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        setClosed(true);
                    } catch (PropertyVetoException ex) {
                    }

                    fireInternalAction("closed");
                }
            });
        add(_closeButton);
    }

    /** Add an action listener
     */
    public void addActionListener(ActionListener l) {
        _listeners.add(ActionListener.class, l);
    }

    /** Fire an action event
     */
    protected void fireInternalAction(String name) {
        ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, name);
        Object[] listeners = _listeners.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ActionListener.class) {
                ((ActionListener) listeners[i + 1]).actionPerformed(e);
            }
        }
    }

    /** Return a fixed size
     */
    public Dimension getMinimumSize() {
        return _dim;
    }

    /** Return a fixed size
     */
    public Dimension getPreferredSize() {
        return _dim;
    }

    /** Return a fixed size
     */
    public Dimension getMaximumSize() {
        return _dim;
    }

    /** Hide the pseudo-frame. This method removes the current content
     * pane from its container and puts it back into its internal
     * frame. Then it puts the desktop pane back to where its supposed
     * to go. If no component is already shown, do nothing.
     */
    public void hideFrame() {
        if (_component == null) {
            return;
        }

        removeComponent(_component);
        _internalFrame.getContentPane().add(_component);

        try {
            _internalFrame.setMaximum(false);
        } catch (PropertyVetoException e) {
        }

        setComponent(_desktopPane);
        _component = null;
        _internalFrame = null;
        _menubar.remove(this);
        _menubar.remove(_glue);
        _menubar.validate();
        _menubar.repaint();
    }

    /** Remove the given component from its position in the display
     */
    protected abstract void removeComponent(JComponent component);

    /** Place the given component into the display
     */
    protected abstract void setComponent(JComponent component);

    /** Show the pseudo-frame with the contents of the given internal
     * frame. This method removes the content pane from the internal
     * frame, removes the desktop pane from its container and then
     * puts the content pane where the desktop pane was. If a
     * component is already shown, do nothing.
     */
    public void showFrame(JInternalFrame frame) {
        if (_component != null) {
            return;
        }

        removeComponent(_desktopPane);
        _internalFrame = frame;
        _component = (JComponent) frame.getContentPane().getComponent(0);
        frame.getContentPane().remove(_component);
        setComponent(_component);
        _menubar.add(_glue);
        _menubar.add(this);
        _menubar.validate();
        _menubar.repaint();
    }

    /** Test if the frame is closed. Inverse of isMaximum().
     */
    public boolean isClosed() {
        return !isMaximum();
    }

    /** Test if the frame is iconified. Inverse of isMaximum().
     */
    public boolean isIconified() {
        return !isMaximum();
    }

    /** Test if the frame is maximized. Return true if the frame has
     * a content pane displayed.
     */
    public boolean isMaximum() {
        return (_component != null);
    }

    /** Remove an action listener
     */
    public void removeActionListener(ActionListener l) {
        _listeners.remove(ActionListener.class, l);
    }

    /** Close the frame if the argument is true.
     * In this base class, move the content
     * pane from the view and put it back into its internal
     * frame. Then close the internal frame. If the argument is false,
     * then do nothing.
     * Subclasses may override this method to do something different
     * to close the frame. This is often useful if further management is
     * necessary.
     * @see diva.gui.DesktopContext
     */
    public void setClosed(boolean flag) throws PropertyVetoException {
        if (flag) {
            if (_internalFrame != null) {
                // remember the internal frame.
                JInternalFrame frame = _internalFrame;
                hideFrame();
                frame.setClosed(true);
            }
        }
    }

    /** Iconify the frame if the argument is true.
     * In this base class, remove the content
     * pane from the view and put it back into its internal
     * frame. Then iconify the internal frame. If the argument is
     * false, do nothing.
     * Subclasses may override this method to do something different
     * to iconify the frame. This is often useful if further management is
     * necessary.
     * @see diva.gui.DesktopContext
     */
    public void setIcon(boolean flag) throws PropertyVetoException {
        if (flag) {
            if (_internalFrame != null) {
                // remember the internal frame.
                JInternalFrame frame = _internalFrame;
                hideFrame();
                frame.setIcon(true);
            }
        }
    }

    /** Minimize the frame if the argument is false.
     * In this base class, remove the
     * content pane from the view and put it back into its internal
     * frame. If the argument is true, do nothing.
     * Subclasses may override this method to do something different
     * to undo maximization of the frame.
     * This is often useful if further management is necessary.
     * @see diva.gui.DesktopContext
     */
    public void setMaximum(boolean flag) throws PropertyVetoException {
        if (!flag) {
            hideFrame();
        }
    }
}
