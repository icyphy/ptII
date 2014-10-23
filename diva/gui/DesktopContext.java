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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;
import javax.swing.event.EventListenerList;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import diva.gui.toolbox.JPseudoFrame;
import diva.gui.toolbox.JShadePane;
import diva.gui.toolbox.JStatusBar;

/**
 * DesktopContext is an application context that displays multiple
 * content panes in an instance of a JDesktopPane. It also contains a
 * menubar, toolbar, and a status bar. When created, it is not
 * visible, so the caller must call setVisible(true) to make it appear
 * on the screen.
 *
 * <p> The implementation of JDesktopPane in Swing is not really quite
 * what we want in a frame of this type, so there are various listeners
 * attached to the JDesktopPane and the internal components that
 * resize and reparent components in a more useful way.
 *
 * <p> Each "content pane" that is added to this frame is added
 * by default to a JInternalFrame. However, the pane may be removed
 * from the pane, either by the JDesktopPane when it is iconified,
 * or by this class when the internal frame is maximized.  Hence, users of
 * this class should not rely on the content pane actually being contained
 * by the internal frame that it is associated with.
 *
 * <p> MDI applications often contain multiple toolbars.  This class
 * implements a toolbar pane that contains the basic toolbar, along with
 * any other toolbars in the application.
 *
 * @author Steve Neuendorffer
 * @author Michael Shilman
 * @author John Reekie
 * @version $Id$
 */
public class DesktopContext extends AppContextDelegate implements MDIContext {
    /** Store the view listeners.
     */
    private transient EventListenerList _listeners = new EventListenerList();

    /** The desktop pane
     */
    private transient JDesktopPane _desktopPane;

    /** The split pane
     */
    private transient JSplitPane _splitPane;

    /** The shade pane
     */
    private transient JComponent _palettePane;

    /** The toolbar pane
     */
    private transient JPanel _toolBarPane;

    /** The status bar
     */
    private transient JStatusBar _statusBar;

    /** The toolbar
     */
    private transient JToolBar _toolBar;

    /** The internal frame manager/listener object
     */
    private transient FrameManager _frameManager;

    /** The current content pane
     */
    private transient JComponent _currentPane;

    /** The icon that is given to the internal frames.
     */
    private transient Icon _frameIcon;

    /** The psuedo-frame
     */
    private transient JPseudoFrame _pseudoFrame;

    /** The current layout mode of this pane. This indicates whether
     * the internal frames are placed by the user, tiled, or cascaded.
     */
    private transient int _layoutMode = LAYOUT_PLACED;

    /** The maximize mode. If true, the current window is fully
     * maximized in the display.
     */
    private transient boolean _maximizeMode = false;

    /** The layout mode in which internal frames are placed by the user.
     */
    public final static int LAYOUT_PLACED = 0;

    /** The layout mode in which internal frames are tiled horizontally.
     */
    public final static int LAYOUT_HORIZONTAL = LAYOUT_PLACED + 1;

    /** The layout mode in which internal frames are tiled vertically.
     */
    public final static int LAYOUT_VERTICAL = LAYOUT_HORIZONTAL + 1;

    /** The layout mode in which internal frames are cascaded one above another.
     */
    public final static int LAYOUT_CASCADED = LAYOUT_VERTICAL + 1;

    /** A mapping from content panes to internal frames. All
     * panes except the current pane, if it is maximized, will
     * be in this mapping.
     */
    private transient HashMap _frames = new HashMap();

    /** Create an instance of this Frame with the application.  The
     * title will be obtained from the application. It will not be
     * visible by default.  The palette pane is set to be an instance of
     * JShadePane.
     * Create a tool bar pane, a toolbar in that pane, and a menu bar for
     * the given context.
     */
    public DesktopContext(AppContext context) {
        this(context, new JShadePane());
    }

    /** Create an instance of this Frame with the application.  The
     * title will be obtained from the application. It will not be
     * visible by default.  The palette pane is set to be the given pane.
     * Create a tool bar pane, a toolbar in that pane, and a menu bar for
     * the given context.
     */
    public DesktopContext(AppContext context, JComponent palette) {
        super(context);
        setSize(800, 600);
        setJMenuBar(new JMenuBar());

        JPanel contentPane = (JPanel) getContentPane();
        _toolBarPane = new JPanel();
        _toolBar = new JToolBar();
        _statusBar = new JStatusBar();
        _desktopPane = new JDesktopPane();
        _desktopPane.setBackground(contentPane.getBackground());
        _palettePane = palette;
        _splitPane = new JSplitPane();
        _splitPane.setLeftComponent(_palettePane);
        _splitPane.setRightComponent(_desktopPane);
        _splitPane.setContinuousLayout(true);

        // Add components
        contentPane.add(_toolBarPane, BorderLayout.NORTH);
        _toolBarPane.add(_toolBar);
        contentPane.add(_splitPane, BorderLayout.CENTER);
        contentPane.add(_statusBar, BorderLayout.SOUTH);

        // Create the psuedo-frame
        _pseudoFrame = new DesktopPseudoFrame(_desktopPane, getJMenuBar());

        // Create the internal manager/listener. This will listen to
        // every frame, as well as the pseudo-frame
        _frameManager = new FrameManager();
        _pseudoFrame.addActionListener(_frameManager);
    }

    /** Add a content pane to this frame. This pane will be put
     * into a JInternalFrame inside the internal desktop pane.
     * The name of the pane will be its title. Generally, a client
     * should call setCurrentPane() after calling this method.
     * The default close operation of frame will be set to DO_NOTHING_ON_CLOSE,
     * and closing operations should be handled by adding a view
     * listener instead.
     */
    @Override
    public void addContentPane(String title, JComponent pane) {
        JInternalFrame iframe = new DesktopInternalFrame(title, true, true,
                true, true);

        Icon icon = getFrameIcon();

        if (icon != null) {
            iframe.setFrameIcon(icon);
        }

        _frames.put(pane, iframe);

        // We want to handle the closing ourselves.
        iframe.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        iframe.getContentPane().add(pane);
        iframe.setBounds(10, 10, 300, 200);
        iframe.setVisible(true);

        // Add the frame manager to it
        iframe.addComponentListener(_frameManager);
        iframe.addInternalFrameListener(_frameManager);
        iframe.addPropertyChangeListener(_frameManager);

        _desktopPane.add(iframe);

        refreshLayout();
    }

    /**
     * Add a view listener to this frame.
     */
    @Override
    public void addViewListener(ViewListener listener) {
        _listeners.add(ViewListener.class, listener);
    }

    /** Get the content pane that is displayed as the current content
     * pane. In this class, this will return the content of the
     * topmost internal frame.
     */
    @Override
    public JComponent getCurrentContentPane() {
        return _currentPane;
    }

    /**
     * Return the icon that is displayed in the frame.
     */
    @Override
    public Icon getFrameIcon() {
        return _frameIcon;
    }

    /** Return the internal frame for a given component.
     */
    public JInternalFrame getInternalFrame(JComponent pane) {
        return (JInternalFrame) _frames.get(pane);
    }

    /** Get the layout mode
     */
    public int getLayoutMode() {
        return _layoutMode;
    }

    /** Get the component used for palettes and the like.
     */
    public JComponent getPalettePane() {
        return _palettePane;
    }

    /** Get the status bar.
     */
    public JStatusBar getStatusBar() {
        return _statusBar;
    }

    /** Get the split pane separating the shade pane and the desktop
     * pane.  This will be null if there is no shade pane.
     */
    public JSplitPane getSplitPane() {
        return _splitPane;
    }

    /** Get the tool bar.
     */
    public JToolBar getJToolBar() {
        return _toolBar;
    }

    /** Get the tool bar pane.
     */
    public JPanel getToolBarPane() {
        return _toolBarPane;
    }

    /** Test if we are in maximize mode
     */
    public boolean isMaximizeMode() {
        return _maximizeMode;
    }

    /** Refresh the layout of the internal frames. This method
     * attempts to be smart about how the internal frames are laid
     * out. It attempts to place iconified frames along the bottom of
     * the desktop pane, and places the other frames within the
     * remaining space.
     */
    public void refreshLayout() {
        // Most of these are not implemented
        switch (_layoutMode) {
        case LAYOUT_HORIZONTAL:
        case LAYOUT_VERTICAL:
        case LAYOUT_CASCADED:
        case LAYOUT_PLACED:

            // FIXME
            break;
        }
    }

    /** Remove the given content pane from the display and close.
     *  This assumes a subsequent call to setCurrentContentPane.
     */
    @Override
    public void removeContentPane(JComponent pane) {
        // Watch out if we are removing the pane that is currently maximized.
        if (pane == _currentPane) {
            _pseudoFrame.hideFrame();
        }

        JInternalFrame iframe = (JInternalFrame) _frames.remove(pane);

        try {
            iframe.setClosed(true);
        } catch (PropertyVetoException e) {
        }

        iframe.dispose();
    }

    /**
     * Remove a view listener from this frame.
     */
    @Override
    public void removeViewListener(ViewListener listener) {
        _listeners.remove(ViewListener.class, listener);
    }

    /** Set the given content pane to be displayed as the current
     * content pane. If we are in maximize mode, set that pane to
     * be displayed maximized. Otherwise, deiconify it if it is
     * iconified, and raise it to the front.
     */
    @Override
    public void setCurrentContentPane(JComponent pane) {
        JInternalFrame iframe = getInternalFrame(pane);

        if (_maximizeMode) {
            // If this is not already the current pane, maximize it
            if (pane != _currentPane) {
                JInternalFrame currentFrame = getInternalFrame(_currentPane);
                _pseudoFrame.hideFrame();

                // If we set the current content pane, then we have
                // to be sure that we select the right internal frame,
                // because that frame is responsible for creating
                // the view event.
                try {
                    iframe.setSelected(true);
                    _pseudoFrame.showFrame(iframe);
                } catch (PropertyVetoException ex) {
                    // FIXME:  the selected pane is going to get out of
                    // sync with the selected document if this ever happens.
                    // back out the change
                    _pseudoFrame.showFrame(currentFrame);
                    return;
                }
            }
        } else {
            // In internal frame mode. Be sure to avoid recursion
            if (!iframe.isSelected()) {
                iframe.grabFocus();
                iframe.show();
            }

            refreshLayout();
        }

        _currentPane = pane;
        pane.requestFocus();
    }

    /** Set the icon that is displayed in
     * internal frames.
     */
    @Override
    public void setFrameIcon(Icon icon) {
        _frameIcon = icon;
    }

    /** Set the layout mode. As long as we are not in maximize
     * mode, refresh the layout accordingly.
     */
    public void setLayoutMode(int mode) {
        _layoutMode = mode;

        if (!isMaximizeMode()) {
            refreshLayout();
        }
    }

    /** Set the maximize mode. If changed to true, the current
     * pane is removed from its internal frame and placed into the
     * split pane. If changed to false, the reverse happens and the
     * desktop pane layout is refreshed.
     */
    public void setMaximizeMode(boolean mode) {
        if (mode == _maximizeMode) {
            return;
        }

        JInternalFrame iframe = getInternalFrame(_currentPane);

        if (mode) {
            _pseudoFrame.showFrame(iframe);
        } else {
            _pseudoFrame.hideFrame();
            refreshLayout();
        }

        _maximizeMode = mode;
    }

    /**
     * Set the palette pane of the context to the given component.
     */
    public void setPalettePane(JComponent pane) {
        _palettePane = pane;
        _splitPane.setLeftComponent(pane);
    }

    /**
     * Show the given status string in the context.
     * In this class, the status is shown in the status bar at the bottom
     * of the frame.
     */
    @Override
    public void showStatus(String status) {
        _statusBar.setMessage(status);
    }

    /** A workaround for the busted JInternalFrame closing event handling in
     * jdk1.2. This should go away when we switch to 1.3 completely.
     */
    @SuppressWarnings("serial")
    private static class DesktopInternalFrame extends JInternalFrame {

        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        /**
         * Creates a <code>JInternalFrame</code> with the specified title and
         * with resizability, closability, maximizability, and iconifiability
         * specified.  All constructors defer to this one.
         *
         * @param title       the <code>String</code> to display in
         *                    the title bar
         * @param resizable   if true, the frame can be resized
         * @param closable    if true, the frame can be closed
         * @param maximizable if true, the frame can be maximized
         * @param iconifiable if true, the frame can be iconified
         */
        public DesktopInternalFrame(String title, boolean resizable,
                boolean closable, boolean maximizable, boolean iconifiable) {
            super(title, resizable, closable, maximizable, iconifiable);
        }

        // This method is implemented in jdk1.3, but not in 1.2.  so we
        // use this class and provide the method so that 1.2 and 1.3 try to
        // look the same.
        @Override
        public void doDefaultCloseAction() {
            fireInternalFrameEvent(InternalFrameEvent.INTERNAL_FRAME_CLOSING);
        }
    }

    @SuppressWarnings("serial")
    private class DesktopPseudoFrame extends JPseudoFrame {
        public DesktopPseudoFrame(JDesktopPane desktopPane, JMenuBar menuBar) {
            super(desktopPane, menuBar);
        }

        @Override
        protected void removeComponent(JComponent c) {
            _splitPane.setRightComponent(null);
        }

        @Override
        protected void setComponent(JComponent c) {
            _splitPane.setRightComponent(c);
        }

        @Override
        public void setClosed(boolean flag) throws PropertyVetoException {
            // To close the pseudoFrame, we don't call hideFrame. This confuses
            // the maximizeMode.  Instead rely on removeContentPane to hide
            // the frame if the close actually succeeds.
            if (flag) {
                DesktopInternalFrame frame = (DesktopInternalFrame) getInternalFrame(getCurrentContentPane());
                frame.doDefaultCloseAction();
            }
        }

        @Override
        public void setIcon(boolean flag) throws PropertyVetoException {
            // to iconify, unset the maximize mode, instead of just
            // hiding the frame.
            if (flag) {
                JInternalFrame frame = getInternalFrame(getCurrentContentPane());
                setMaximizeMode(false);
                frame.setIcon(true);
            }
        }

        @Override
        public void setMaximum(boolean flag) throws PropertyVetoException {
            // unset the maximize mode, instead of just hiding the frame.
            setMaximizeMode(flag);
        }
    }

    /**
     * FrameManager is a class that responds to UI events on the
     * internal frames and generates other events and does useful stuff.
     */
    private class FrameManager extends InternalFrameAdapter implements
    ComponentListener, PropertyChangeListener, ActionListener {
        // Update when the pseudo-frame has things done to it
        @Override
        public void actionPerformed(ActionEvent e) {
            // We could listen to the pseudoframe here, but it works
            // better to override the default behavior in the
            // DesktopPseudoFrame
        }

        // Propagate frame moves as view events
        @Override
        public void componentMoved(ComponentEvent e) {
            fire(e, ViewEvent.VIEW_MOVED);
        }

        @Override
        public void componentResized(ComponentEvent e) {
            fire(e, ViewEvent.VIEW_RESIZED);
        }

        @Override
        public void componentShown(ComponentEvent e) {
            fire(e, ViewEvent.VIEW_SHOWN);
        }

        @Override
        public void componentHidden(ComponentEvent e) {
            fire(e, ViewEvent.VIEW_HIDDEN);
        }

        // Some internal frame events get propagated as view events
        @Override
        public void internalFrameActivated(InternalFrameEvent e) {
            fire(e, ViewEvent.VIEW_SELECTED);
        }

        @Override
        public void internalFrameClosed(InternalFrameEvent e) {
            fire(e, ViewEvent.VIEW_CLOSED);
        }

        @Override
        public void internalFrameClosing(InternalFrameEvent e) {
            fire(e, ViewEvent.VIEW_CLOSING);
        }

        @Override
        public void internalFrameDeactivated(InternalFrameEvent e) {
            fire(e, ViewEvent.VIEW_DESELECTED);
        }

        @Override
        public void internalFrameDeiconified(InternalFrameEvent e) {
            fire(e, ViewEvent.VIEW_SHOWN);
        }

        @Override
        public void internalFrameIconified(InternalFrameEvent e) {
            fire(e, ViewEvent.VIEW_HIDDEN);
        }

        // If the window is maximized, do it properly
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            String property = e.getPropertyName();

            if (property.equals("maximum")) {
                if (((Boolean) e.getNewValue()).booleanValue()) {
                    setMaximizeMode(true);
                }
            }
        }

        private void fire(AWTEvent e, int id) {
            JInternalFrame f = (JInternalFrame) e.getSource();
            JComponent c;

            if (f.getContentPane().getComponentCount() == 1) {
                c = (JComponent) f.getContentPane().getComponent(0);
            } else if (f.getContentPane().getComponentCount() == 0
                    && isMaximizeMode()) {
                c = getCurrentContentPane();
            } else {
                throw new RuntimeException("Could not find content "
                        + "for frame " + f);
            }

            ViewEvent event = new ViewEvent(c, id);
            Object[] listeners = _listeners.getListenerList();

            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == ViewListener.class) {
                    switch (id) {
                    case ViewEvent.VIEW_CLOSED:
                        ((ViewListener) listeners[i + 1]).viewClosed(event);
                        break;

                    case ViewEvent.VIEW_CLOSING:
                        ((ViewListener) listeners[i + 1]).viewClosing(event);
                        break;

                    case ViewEvent.VIEW_DESELECTED:
                        ((ViewListener) listeners[i + 1]).viewDeselected(event);
                        break;

                    case ViewEvent.VIEW_HIDDEN:
                        ((ViewListener) listeners[i + 1]).viewHidden(event);
                        break;

                    case ViewEvent.VIEW_MOVED:
                        ((ViewListener) listeners[i + 1]).viewMoved(event);
                        break;

                    case ViewEvent.VIEW_RESIZED:
                        ((ViewListener) listeners[i + 1]).viewResized(event);
                        break;

                    case ViewEvent.VIEW_SELECTED:
                        ((ViewListener) listeners[i + 1]).viewSelected(event);
                        break;

                    case ViewEvent.VIEW_SHOWN:
                        ((ViewListener) listeners[i + 1]).viewShown(event);
                        break;
                    }
                }
            }
        }
    }
}
