/* Top-level window with a menubar and status bar.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */
package ptolemy.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.print.PrintService;
import javax.print.attribute.Attribute;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Destination;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// Top

/**
 This is a top-level window with a menubar and an optional status bar.
 Derived classes should add components to the content pane using a
 line like:
 <pre>
 getContentPane().add(component, BorderLayout.CENTER);
 </pre>
 Derived classes may wish to modify the menus.  The File
 and Help menus are exposed as protected members.
 The File menu items in the _fileMenuItems protected array are,
 in order, Open File, Open URL, New, Save, Save As, Print, Close, and Exit.
 The Help menu items in the _helpMenuItems protected array are,
 in order, About and Help.
 <p>
 A derived class can use the insert() methods of JMenu
 to insert a menu item defined by an Action or a JMenuItem
 into a specified position in the menu.
 Derived classes can also insert separators using the
 insertSeparator() method of JMenu.
 In principle, derived classes can also remove menu items
 using the remove() methods of JMenu; however, we discourage this.
 A basic principle of user interface design is habituation, where
 there is considerable value in having menus that have consistent
 contents and layout throughout the application (Microsoft, for
 example, violates this principle with adaptive menus).
 <p>
 Instead of removing items from the menu, they can be disabled.
 For example, to disable the "Save" item in the File menu, do
 <pre>
 _fileMenuItems[3].setEnabled(false);
 </pre>
 <p>
 Some menu items are provided, but are disabled by default.
 The "New" item, for example, can be enabled with
 <pre>
 _fileMenuItems[2].setEnabled(true);
 </pre>
 A derived class that enables this menu item can populate the menu with
 submenu items.  This particular entry in the _fileMenuItems[2]
 is a JMenu, not just a JMenuItem, so it can have menu items
 added to it.
 <p>
 A derived class can add an entirely new menu (many do that).
 However, at this time, the JMenuBar interface does not support
 putting a new menu into an arbitrary position.  For this reason,
 derived classes should insert new menus into the menu bar only
 in the _addMenus() protected method.  This ensures that the File
 menu is always the rightmost menu, and the Help menu is always
 the leftmost menu.  The _addMenus() method is called when the window
 is first packed.

 @author Edward A. Lee and Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (janneck)
 */
@SuppressWarnings("serial")
public abstract class Top extends JFrame {
    /** Construct an empty top-level frame with the default status
     *  bar.  After constructing this, it is necessary to call
     *  pack() to have the menus added, and then setVisible(true)
     *  to make the frame appear.  It may also be desirable to
     *  call centerOnScreen().  This can be done after
     *  pack() and before setVisible().
     */
    public Top() {
        this(new StatusBar());
    }

    /** Construct an empty top-level frame with the specified status
     *  bar.  After constructing this,
     *  it is necessary to call pack() to have the menus added, and
     *  then setVisible(true) to make the frame appear.  It may also
     *  be desirable to call centerOnScreen().  This can be done after
     *  pack() and before setVisible().
     *  @param statusBar A status bar, or null to not insert one.
     */
    public Top(StatusBar statusBar) {
        super();

        _statusBar = statusBar;

        // Ensure that user is prompted before closing if the data
        // has been modified.
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new CloseWindowAdapter());

        getContentPane().setLayout(new BorderLayout());

        _fileMenuListener = new FileMenuListener();
        _helpMenuListener = new HelpMenuListener();
        _historyMenuListener = new HistoryMenuListener();

        // Make this the default context for modal messages.
        UndeferredGraphicalMessageHandler.setContext(this);

        // If we are on a Mac, initialize the quit menu
        // so that Command-Q saves data.
        if (PtGUIUtilities.macOSLookAndFeel()) {
            _macInitializer();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Open a dialog with basic information about this window.
     *  Derived classes may override {@link #_about()}.
     */
    public final void about() {
        // Under Mac OS X, the "About" menu choice invokes this method.
        _about();
    }

    /** Center the window on the screen.  This must be called after the
     *  window is populated with its contents, since it depends on the size
     *  being known. If this method is called from a thread that is not
     *  the AWT event dispatch thread, then its execution is deferred
     *  and performed in that thread.
     */
    public void centerOnScreen() {
        Runnable doCenter = new CenterOnScreenRunnable();

        deferIfNecessary(doCenter);
    }

    /** Close the window, prompting the user to save changes if there
     *  have been any.  Derived classes should override the protected
     *  method _close(), not this one. This method returns immediately
     *  if it is called outside the swing UI thread, deferring the action
     *  so that it is executed in the swing thread.
     */
    public final void close() {
        if (_debugClosing) {
            System.out.println("Top.close() : " + this.getName());
        }

        Runnable doClose = new CloseWindowRunnable();

        deferIfNecessary(doClose);
    }

    /** If this method is called in the AWT event dispatch thread,
     *  then simply execute the specified action.  Otherwise,
     *  if there are already deferred actions, then add the specified
     *  one to the list.  Otherwise, create a list of deferred actions,
     *  if necessary, and request that the list be processed in the
     *  event dispatch thread.
     *  <p>
     *  Note that it does not work nearly as well to simply schedule
     *  the action yourself on the event thread because if there are a
     *  large number of actions, then the event thread will not be able
     *  to keep up.  By grouping these actions, we avoid this problem.
     *  @param action The Runnable object to execute.
     */
    public static void deferIfNecessary(Runnable action) {
        // NOTE: This is a static version of a method in PlotBox, but
        // we do not want to create cross dependencies between these
        // packages.
        // In swing, updates to showing graphics must be done in the
        // event thread.  If we are in the event thread, then proceed.
        // Otherwise, queue a request or add to a pending request.
        if (EventQueue.isDispatchThread()) {
            action.run();
        } else {
            synchronized (_deferredActions) {
                // Add the specified action to the list of actions to perform.
                _deferredActions.add(action);

                // If it hasn't already been requested, request that actions
                // be performed in the event dispatch thread.
                if (!_actionsDeferred) {
                    Runnable doActions = new DeferredActionsRunnable();

                    // NOTE: Using invokeAndWait() here risks causing
                    // deadlock.  Don't do it!
                    SwingUtilities.invokeLater(doActions);
                    _actionsDeferred = true;
                }
            }
        }
    }

    /** Dispose of this frame.
     *     Override this dispose() method to unattach any listeners that may keep
     *  this model from getting garbage collected.  This method invokes the
     *  dispose() method of the superclass,
     *  {@link javax.swing.JFrame}.
     */
    @Override
    public void dispose() {
        if (_debugClosing) {
            System.out.println("Top.dispose() : " + this.getName());
        }

        // Deal with help menu action listeners
        /*int c =*/MemoryCleaner.removeActionListeners(_historyMenu);
        //System.out.println("_historyMenu: "+c);
        if (_historyMenu != null) {
            _historyMenusAndListeners.remove(_historyMenu);
        }
        _historyMenuListener = null;

        // Don't call removeWindowListeners here because Tableau.setFrame()
        // adds a WindowListener to windowsClosed events and that listener
        // handles closing the plots associated with a model.
        //MemoryCleaner.removeWindowListeners(this);

        // Deal with file menu action listeners
        /*c =*/MemoryCleaner.removeActionListeners(_fileMenu);
        //System.out.println("_fileMenu: "+c);
        for (JMenuItem menuItem : _fileMenuItems) {
            /*c =*/MemoryCleaner.removeActionListeners(menuItem);
            //System.out.println("_fileMenuItems["+i+"]: "+c);
        }
        _fileMenuListener = null;

        // Deal with help menu action listeners
        /*c =*/MemoryCleaner.removeActionListeners(_helpMenu);
        //System.out.println("_helpMenu: "+c);
        for (JMenuItem menuItem : _helpMenuItems) {
            /*c =*/MemoryCleaner.removeActionListeners(menuItem);
            //System.out.println("_helpMenuItems["+i+"]: "+c);
        }
        _helpMenuListener = null;

        /*c =*/MemoryCleaner.removeActionListeners(_menubar);
        //System.out.println("_menubar: "+c);
        _menubar.removeAll();

        // ensure reference to this is removed
        UndeferredGraphicalMessageHandler.setContext(null);

        // I'm not sure exactly why this works but it does!
        // I think it has to do with the KeyboardFocusManager
        // holding onto the last focused component, so clearing and
        // cycling seems to free up the reference to this window.
        KeyboardFocusManager focusManager = KeyboardFocusManager
                .getCurrentKeyboardFocusManager();
        focusManager.clearGlobalFocusOwner();
        focusManager.downFocusCycle();

        // Set any AbstractActions to null.  This is not strictly necessary,
        // but it helps free up memory more quickly.  By doing this here,
        // we no longer require people to update dispose() by hand.
        Class myClass = getClass();
        // Loop through classes up to the parent class of Top.
        while (myClass != JFrame.class) {
            Field[] fields = myClass.getDeclaredFields();
            for (Field field : fields) {
                try {
                    // Loop through the class hierarchy of each field.
                    // If the class is assignable from AbstractAction, then
                    // set it to null.
                    Class superclass = field.getType();
                    while (superclass != null && superclass != Object.class) {
                        if (superclass.isAssignableFrom(AbstractAction.class)) {
                            try {
                                field.setAccessible(true);
                                field.set(this, null);
                            } catch (SecurityException ex) {
                                if (!_printedSecurityExceptionMessage) {
                                    _printedSecurityExceptionMessage = true;
                                    System.out
                                    .println("Warning: Failed set "
                                            + field
                                            + " accessible while disposing. "
                                            + "(applets and -sandbox always causes this)");
                                }
                            }
                            break;
                        }
                        superclass = superclass.getSuperclass();
                    }
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException("Failed to get or set field "
                            + field, ex);
                }
            }
            myClass = myClass.getSuperclass();
        }

        getContentPane().removeAll();
        _disposed = true;

        super.dispose();
    }

    /** Exit the application after querying the user to save data.
     *  Derived classes should override {@link #_exit()}.
     *  @return False if the use cancelled a saving a modified buffer,
     *  true otherwise.
     */
    public final boolean exit() {
        // Under Mac OS X, Command-q invokes this method.
        if (_debugClosing) {
            System.out.println("Top.exit() : " + this.getName());
        }
        _exit();
        if (_exitResult == _CANCELED) {
            return false;
        }
        return true;
    }

    /** Return true if the window is set to be centered when pack() is called.
     *  @return True if the window will be centered when pack is called.
     *  @see #setCentering(boolean)
     */
    public boolean getCentering() {
        return _centering;
    }

    /** Return the size of the contents of this window.
     *  @return The size of the contents.
     */
    public Dimension getContentSize() {
        return getContentPane().getSize();
    }

    /** If called before the first time pack() is called, this
     *  method will prevent the appearance of a menu bar. This is
     *  rarely desirable, but some subclasses of Top have to
     *  contain panels that are not swing components. Such
     *  components do not work with menus (the menu seems to
     *  appear behind the component instead of in front of it).
     *  Call this to prevent a menu bar.
     */
    public void hideMenuBar() {
        _hideMenuBar = true;
    }

    /** Return true if this frame has been disposed.
     *  It is possible for the dispose() method to be called directly.
     *  This may conflict with another dispose call
     *  in a window listener or somewhere else.  Before Top
     *  calls super.dispose() (and thus triggering listeners) this
     *  boolean is set to true so listeners can check and make sure
     *  they aren't calling dispose for a second (or third) time.
     *  @return true if this frame has been disposed.
     */
    public boolean isDisposed() {
        return _disposed;
    }

    /** Return true if the menu of this window has been populated.
     *  The menu is populated as a side effect of the first invocation to
     *  the pack() method.
     *  @return True if the menu bar has been populated.
     */
    public synchronized boolean isMenuPopulated() {
        return _menuPopulated;
    }

    /** Return true if the data associated with this window has been
     *  modified since it was first read or last saved.  This returns
     *  the value set by calls to setModified(), or false if that method
     *  has not been called.
     *  @return True if the data has been modified.
     */
    public boolean isModified() {
        return _modified;
    }

    /** Size this window to its preferred size and make it
     *  displayable, and override the base class to populate the menu
     *  bar if the menus have not already been populated.  If the
     *  window size has not been set (by some derived class), then
     *  this will center the window on the screen. This is
     *  done here rather than in the constructor so that derived
     *  classes are assured that their constructors have been fully
     *  executed when _addMenus() is called. If this method is called
     *  outside the AWT event thread, then its execution is deferred and
     *  performed in that thread.
     */
    @Override
    public void pack() {
        Runnable doPack = new DoPackRunnable();

        deferIfNecessary(doPack);
    }

    /** Report a message to the user by displaying it in a status bar,
     *  if there is one. If this method is called outside the AWT event
     *  thread, then its execution is deferred and performed in that thread.
     *  @param message The message to report.
     */
    public void report(final String message) {
        Runnable doReport = new StatusBarMessageRunnable(message);

        deferIfNecessary(doReport);
    }

    /** Report a Throwable, which is usually an Exception but can also
     *  be an Error. If this method is called outside the AWT event
     *  thread, then its execution is deferred and performed in that thread.
     *  This pops up a window with the option of examining the stack
     *  trace, and reports the specified message in the status bar, if
     *  there is one.
     *  @param message The message.
     *  @param throwable The Throwable to report.
     */
    public void report(final String message, final Throwable throwable) {
        Runnable doReport = new StatusBarMessageReportRunnable(message,
                throwable);

        deferIfNecessary(doReport);
    }

    /** Report a Throwable, which is usually an Exception but can also
     *        be an Error.  This displays a message in a dialog by
     *  calling the two-argument version with an empty string as the
     *  first argument.  If this method is called outside the AWT event
     *  thread, then its execution is deferred and performed in that thread.
     *  @param throwable The Throwable to report
     *  @see #report(String, Throwable)
     */
    public void report(Throwable throwable) {
        report("", throwable);
    }

    /** Set background color.  This overrides the base class to set the
     *  background of the status bar. If this method is called outside
     *  the AWT event thread, then its execution is deferred and
     *  performed in that thread.
     *  @param background The background color.
     */
    @Override
    public void setBackground(final Color background) {
        _statusBarBackground = background;
        Runnable doSet = new SetBackgroundRunnable();

        deferIfNecessary(doSet);
    }

    /** Specify whether or not to center the window on the screen when
     *  packing it.  The default is true.
     *  @param centering Set to false to disable centering.
     *  @see #getCentering()
     */
    public void setCentering(boolean centering) {
        _centering = centering;
    }

    /**
     * Set the initial default directory.  If this method is not
     * called, then the initial default directory will be the value of
     * the user.dir Java property, which is typically the current
     * working directory.  This method allows external configuration
     * to determine the initial/default opening/saving directory to
     * use for file dialogs.  (Used in Kepler)
     * @param dir the initial directory to use for file dialogs
     */
    public static void setDirectory(File dir) {
        _directory = dir;
    }

    /** Record whether the data associated with this window has been
     *  modified since it was first read or last saved.  If you call
     *  this with a true argument, then subsequent attempts to close
     *  the window will trigger a dialog box to confirm the closing.
     *  @param modified Indicator of whether the data has been modified.
     */
    public void setModified(boolean modified) {
        _modified = modified;
    }

    /** Override the base class to deiconify
     *  the window, if necessary. If this method is called
     *  outside the AWT event thread, then its execution is deferred and
     *  performed in that thread.
     */
    @Override
    public void show() {
        Runnable doShow = new ShowWindowRunnable();

        deferIfNecessary(doShow);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Open a dialog with basic information about this window.
     */
    protected void _about() {
        JOptionPane.showMessageDialog(this, "Ptolemy II "
                + getClass().getName() + "\n"
                + "By: Claudius Ptolemaeus, ptolemy@eecs.berkeley.edu\n"
                + "For more information, see\n"
                + "http://ptolemy.eecs.berkeley.edu/ptolemyII\n\n"
                + "Copyright (c) 1997-2014, "
                + "The Regents of the University of California.",
                "About Ptolemy II", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Add menus to the menu bar.  In this base class, this does nothing.
     *  In derived classes, however, it will add items with commands like
     *  <pre>
     *      JMenu newMenu = new JMenu("My Menu");
     *      _menubar.add(newMenu);
     *  </pre>
     *  The reason for doing this in a protected method rather than
     *  doing it directly in the constructor of the base class is subtle.
     *  Unfortunately, at this time, Java provides no mechanism for
     *  derived classes to insert menus at arbitrary points in the
     *  menu bar.  Also, the menubar ignores the alignment property
     *  of the JMenu.  By convention, however, we want the help menu to
     *  be the rightmost menu.  Thus, we use a strategy pattern here,
     *  and call a protected method that derived classes can use to
     *  add menus.  Thus, this method is called before the Help menu
     *  is added, and hence menus added in this method will appear to
     *  the left of the Help menu.
     */
    protected void _addMenus() {
    }

    /** Clear the current contents.  This base class checks to see whether
     *  the contents have been modified, and if so, then prompts the user
     *  to save them.  Derived classes should override this method to
     *  first call this parent class method, then clear the data,
     *  unless the return value is false.  A return value of false
     *  indicates that the user has canceled the action.
     *  @return True if the current contents are either saved or discarded
     *   with permission from the user.
     */
    protected boolean _clear() {
        int result = _queryForSave();
        return result == _SAVED || result == _DISCARDED;
    }

    /** Close the window.  Derived classes should override this to
     *  release any resources or remove any listeners.  In this class,
     *  if the data associated with this window has been modified, as
     *  indicated by isModified(), then ask the user whether to save
     *  the data before closing.
     *  @return False if the user cancels on a save query.
     */
    protected boolean _close() {
        if (_debugClosing) {
            System.out.println("Top._close() : " + this.getName());
        }

        // NOTE: We use dispose() here rather than just hiding the
        // window.  This ensures that derived classes can react to
        // windowClosed events rather than overriding the
        // windowClosing behavior given here.
        if (isModified()) {
            int result = _queryForSave();

            if (result == _SAVED || result == _DISCARDED) {
                dispose();
                return true;
            }

            return false;
        } else {
            // Window is not modified, so just dispose.
            dispose();
            return true;
        }
    }

    /** Create the items in the File menu. A null element in the array
     *  represents a separator in the menu.
     *
     *  @return The items in the File menu.
     */
    protected JMenuItem[] _createFileMenuItems() {
        JMenuItem[] fileMenuItems = new JMenuItem[13];

        fileMenuItems[0] = new JMenuItem("Open File", KeyEvent.VK_O);
        fileMenuItems[1] = new JMenuItem("Open URL", KeyEvent.VK_U);
        // The following assumes _NEW_MENU_INDEX = 2.
        fileMenuItems[_NEW_MENU_INDEX] = new JMenu("New");
        fileMenuItems[3] = new JMenuItem("Save", KeyEvent.VK_S);
        fileMenuItems[4] = new JMenuItem("Save As", KeyEvent.VK_A);
        // The following assumes _IMPORT_MENU_INDEX = 5.
        fileMenuItems[_IMPORT_MENU_INDEX] = new JMenu("Import");
        // The following assumes _EXPORT_MENU_INDEX = 6.
        fileMenuItems[_EXPORT_MENU_INDEX] = new JMenu("Export");
        fileMenuItems[7] = new JMenuItem("Print", KeyEvent.VK_P);
        fileMenuItems[8] = new JMenuItem("Close", KeyEvent.VK_C);

        // Separators
        fileMenuItems[9] = null;
        fileMenuItems[11] = null;

        // History submenu
        JMenu history = new JMenu("Recent Files");
        fileMenuItems[10] = history;

        // Exit
        fileMenuItems[12] = new JMenuItem("Exit", KeyEvent.VK_Q);

        if (StringUtilities.inApplet()) {
            JMenuItem[] appletFileMenuItems = new JMenuItem[8];
            System.arraycopy(fileMenuItems, 0, appletFileMenuItems, 0,
                    appletFileMenuItems.length);
            appletFileMenuItems[7] = fileMenuItems[10];
            fileMenuItems = appletFileMenuItems;
            // If we are in an applet, disable certain menu items.
            fileMenuItems[0].setEnabled(false);
            fileMenuItems[2].setEnabled(false);
            fileMenuItems[3].setEnabled(false);
            fileMenuItems[4].setEnabled(false);
            fileMenuItems[_IMPORT_MENU_INDEX].setEnabled(false);
            fileMenuItems[_EXPORT_MENU_INDEX].setEnabled(false);
            return fileMenuItems;
        }

        // Open button = ctrl-o.
        fileMenuItems[0].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        // The mnemonic isn't set in the static
        // initializer because JMenu doesn't have an
        // appropriate constructor.
        fileMenuItems[2].setMnemonic(KeyEvent.VK_N);
        fileMenuItems[_EXPORT_MENU_INDEX].setMnemonic(KeyEvent.VK_E);

        // New Import and Export buttons disabled by default.
        fileMenuItems[2].setEnabled(false);
        fileMenuItems[_IMPORT_MENU_INDEX].setEnabled(false);
        fileMenuItems[_EXPORT_MENU_INDEX].setEnabled(false);

        // Save button = ctrl-s.
        fileMenuItems[3].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        // Print button = ctrl-p.
        fileMenuItems[7].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        // Print button disabled by default, unless this class implements
        // one of the JDK1.2 printing interfaces.
        if (Top.this instanceof Printable || Top.this instanceof Pageable) {
            fileMenuItems[7].setEnabled(true);
        } else {
            fileMenuItems[7].setEnabled(false);
        }

        // Close button = ctrl-w.
        fileMenuItems[8].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        // Close button = ctrl-q.
        fileMenuItems[12].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        // Sadly, the above doesn't actually work. Command-Q is not caught.

        return fileMenuItems;
    }

    /** Exit the application after querying the user to save data.
     *  Derived classes should override this to do something more
     *  reasonable, so that user data is not discarded.
     */
    protected void _exit() {
        if (isModified()) {
            int result = _queryForSave();
            _exitResult = result;
            if (result == _SAVED || result == _DISCARDED) {
                StringUtilities.exit(0);
            }
        } else {
            // Window is not modified, so just exit.
            StringUtilities.exit(0);
        }
    }

    /** Return the current directory.
     * If {@link #setDirectory(File)} or
     * {@link #_open()}, then the value of the "user.dir"
     * property is returned.
     * @return The current directory.
     */
    protected File _getCurrentDirectory() {
        if (_directory != null) {
            return _directory;
        } else {
            // The default on Windows is to open at user.home, which is
            // typically not what we want.
            // So we use the current directory instead.
            // This will fail with a security exception in applets.
            String currentWorkingDirectory = StringUtilities
                    .getProperty("user.dir");
            if (currentWorkingDirectory == null) {
                return null;
            } else {
                return new File(currentWorkingDirectory);
            }
        }
    }

    /** Get the name of this object, which in this base class is
     *  either the name of the file that has been associated with this
     *  object, or the string "Unnamed" if none.
     *  @return The name.
     */
    protected String _getName() {
        if (_file == null) {
            return "Unnamed";
        }

        return _file.getName();
    }

    /** Display the same information given by _about().
     *  Derived classes should override this to give information
     *  about the particular window and its role.
     */
    protected void _help() {
        _about();
    }

    /** Open a file dialog to identify a file to be opened, and then call
     *  _read() to open the file.  If {@link ptolemy.gui.PtGUIUtilities#useFileDialog()}
     *  return true, then java.awt.FileDialog is used.
     *  Otherwise, javax.swing.JFileChooser is used.
     */
    protected void _open() {
        if (PtGUIUtilities.useFileDialog()) {
            _openFileDialog();
        } else {
            _openJFileChooser();
        }
    }

    /** Open a dialog to enter a URL, and then invoke
     *  _read() to open the URL.
     */
    protected void _openURL() {
        Query query = new Query();
        query.setTextWidth(60);
        query.addLine("url", "URL", _lastURL);

        ComponentDialog dialog = new ComponentDialog(this, "Open URL", query);

        if (dialog.buttonPressed().equals("OK")) {
            _lastURL = query.getStringValue("url");

            try {
                URL url = new URL(_lastURL);
                _read(url);
            } catch (Exception ex) {
                report("Error reading URL:\n" + _lastURL, ex);
            }
        }
    }

    /** Print the contents.  If this frame implements either the
     *  Printable or Pageable then those interfaces are used to print
     *  it.
     */
    protected void _print() {
        // If you are using $PTII/bin/vergil, under bash, set this property:
        // export JAVAFLAGS=-Dptolemy.ptII.print.platform=CrossPlatform
        // and then run $PTII/bin/vergil
        if (StringUtilities.getProperty("ptolemy.ptII.print.platform").equals(
                "CrossPlatform")) {
            _printCrossPlatform();
        } else {
            _printNative();
        }
    }

    /** Print using the cross platform dialog.
     *  Note that in java 1.6.0_05, the properties button is disabled,
     *  so using _printNative() is preferred.
     */
    protected void _printCrossPlatform() {
        // FIXME: Code duplication with PlotBox and PlotFrame.
        // See PlotFrame for notes.

        // Build a set of attributes
        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
        PrinterJob job = PrinterJob.getPrinterJob();

        _macCheck();

        if (this instanceof Pageable) {
            job.setPageable((Pageable) this);
        } else if (this instanceof Printable) {
            //PageFormat format = job.pageDialog(job.defaultPage());
            //job.setPrintable((Printable) this, format);
            job.setPrintable((Printable) this);
        } else {
            // Can't print it.
            return;
        }

        if (job.printDialog(aset)) {
            try {
                job.print(aset);
            } catch (Exception ex) {
                MessageHandler.error("Cross Platform Printing Failed", ex);
            }
        }
    }

    /** If a PDF printer is available print to it.
     *  @exception PrinterException If a printer with the string "PDF"
     * cannot be found or if the job cannot be set to the PDF print
     * service or if there is another problem printing.
     */
    protected void _printPDF() throws PrinterException {
        // Find something that will print to PDF
        boolean foundPDFPrinter = false;

        PrintService pdfPrintService = null;
        PrintService printServices[] = PrinterJob.lookupPrintServices();
        for (PrintService printService : printServices) {
            if (printService.getName().indexOf("PDF") != -1) {
                foundPDFPrinter = true;
                pdfPrintService = printService;
            }
        }

        if (pdfPrintService == null || foundPDFPrinter == false) {
            throw new PrinterException("Could not find a printer with the "
                    + "string \"PDF\" in its name.  Currently, the -printPDF "
                    + "facility requires a PDF printer such as the non-free "
                    + "full version of Adobe Acrobat.");
        }

        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat pageFormat = job.defaultPage();
        job.setPrintService(pdfPrintService);

        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();

        _macCheck();

        if (this instanceof Pageable) {
            // FIXME: what about the page format?
            job.setPageable((Pageable) this);
            job.validatePage(pageFormat);
        } else if (this instanceof Printable) {
            job.setPrintable((Printable) this, pageFormat);
        } else {
            System.out.println("Can't print a " + this
                    + ", it must be either Pageable or Printable");
            // Can't print it.
            return;
        }
        if (foundPDFPrinter) {
            // This gets ignored, but let's try it anyway
            Destination destination = new Destination(
                    new File("ptolemy.pdf").toURI());
            aset.add(destination);

            // On the Mac, calling job.setJobName() will set the file name,
            // but not the directory.
            System.out
            .println("Top._printPDF(): Print Job information, much of which is ignored?\n"
                    + "JobName: "
                    + job.getJobName()
                    + "\nUserName: "
                    + job.getUserName());
            javax.print.attribute.Attribute[] attributes = aset.toArray();
            for (Attribute attribute : attributes) {
                System.out.println(attribute.getName() + " "
                        + attribute.getCategory() + " " + attribute);
            }

            job.print(aset);
            System.out
            .println("Window printed from command line. "
                    + "Under MacOSX, look for "
                    + "~/Desktop/Java Printing.pdf");
        }
    }

    /** Print using the native dialog.
     */
    protected void _printNative() {
        // FIXME: Code duplication with PlotBox and PlotFrame.
        // See PlotFrame for notes.

        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat defaultFormat = job.defaultPage();
        PageFormat pageFormat = job.pageDialog(defaultFormat);

        // If the print dialog is cancelled, we do not handle the job any more.
        // -- tfeng (12/12/2008)
        if (defaultFormat == pageFormat) {
            return;
        }

        _macCheck();

        if (this instanceof Pageable) {
            // FIXME: what about the page format?
            job.setPageable((Pageable) this);
            job.validatePage(pageFormat);
        } else if (this instanceof Printable) {
            job.setPrintable((Printable) this, pageFormat);
        } else {
            // Can't print it.
            return;
        }

        if (job.printDialog()) {
            try {
                job.print();
            } catch (Exception ex) {
                MessageHandler.error("Native Printing Failed", ex);
            }
        }
    }

    /** Open a dialog to prompt the user to save the data.
     *  Return false if the user clicks "cancel", and otherwise return true.
     *  If the user clicks "Save", this also saves the data.
     *  @return _SAVED if the file is saved, _DISCARDED if the modifications are
     *   discarded, _CANCELED if the operation is canceled by the user, and
     *   _FAILED if the user selects save and the save fails.
     */
    protected int _queryForSave() {
        Object[] options = { "Save", "Discard changes", "Cancel" };

        String query = "Save changes to " + StringUtilities.split(_getName())
                + "?";

        // Show the MODAL dialog
        int selected = JOptionPane.showOptionDialog(this, query,
                "Save Changes?", JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (selected == 0) {
            if (_save()) {
                return _SAVED;
            } else {
                return _FAILED;
            }
        }

        if (selected == 1) {
            // To prevent the user from being asked again:
            setModified(false);
            return _DISCARDED;
        }

        return _CANCELED;
    }

    /** Read the specified URL.
     *  @param url The URL to read.
     *  @exception Exception If the URL cannot be read.
     */
    protected abstract void _read(URL url) throws Exception;

    /** Save the model to the current file, if there is one, and otherwise
     *  invoke _saveAs().  This calls _writeFile().
     *  @return True if the save succeeds.
     */
    protected boolean _save() {
        if (_file != null) {
            try {
                _writeFile(_file);
                setModified(false);
                return true;
            } catch (IOException ex) {
                report("Error writing file", ex);
                return false;
            }
        } else {
            return _saveAs();
        }
    }

    /** Query the user for a filename and save the model to that file.
     *  If {@link ptolemy.gui.PtGUIUtilities#useFileDialog()}
     *  return true, then java.awt.FileDialog is used.
     *  Otherwise, javax.swing.JFileChooser is used.
     *  @return True if the save succeeds.
     */
    protected boolean _saveAs() {
        if (PtGUIUtilities.useFileDialog()) {
            return _saveAsFileDialogImplementation();
        } else {
            return _saveAsJFileChooserImplementation();
        }
    }

    /** Create and return a file dialog for the "Save As" command.
     *  @return A file dialog for save as.
     *  @deprecated Use {@link #_saveAsJFileChooserComponent()}
     */
    @Deprecated
    protected JFileChooser _saveAsFileDialog() {
        // Too bad this method was incorrectly named.  This method
        // returns a JFileChooser, why on earth would its name
        // have "FileDialog" in it?
        return _saveAsJFileChooserComponent();
    }

    /** Create and return a FileDialog for the "Save As" command.
     *  If {@link ptolemy.gui.PtGUIUtilities#useFileDialog()} returns true,
     *  then {@link #_saveAs()} uses this method.  Otherwise,
     *  {@link #_saveAsJFileChooserComponent()} is used.
     *  @return A file dialog for save as.
     */
    protected FileDialog _saveAsFileDialogComponent() {
        // The name of this method ends in "Component" because
        // there already was a _saveAsFileDialog() method.
        FileDialog fileDialog = new FileDialog(this, "Save as...",
                FileDialog.SAVE);

        if (_filenameFilter != null) {
            fileDialog.setFilenameFilter(_filenameFilter);
        }

        fileDialog.setDirectory(_getCurrentDirectory().toString());
        return fileDialog;
    }

    /** Create and return a JFileChooser for the "Save As" command.
     *  If {@link ptolemy.gui.PtGUIUtilities#useFileDialog()} returns false,
     *  then {@link #_saveAs()} uses this method.  Otherwise,
     *  {@link #_saveAsFileDialogComponent()} is used.
     *  @return A JFileChooser for save as.
     */
    protected JFileChooser _saveAsJFileChooserComponent() {
        JFileChooser fileDialog = new JFileChooser();

        if (_fileFilter != null) {
            fileDialog.addChoosableFileFilter(_fileFilter);
        }

        fileDialog.setDialogTitle("Save as...");
        fileDialog.setCurrentDirectory(_getCurrentDirectory());
        return fileDialog;
    }

    /** Update the submenu with a history list
     * and add a listener to each line.
     * @param historyList The list of history items,
     * where each element is a String is the name of the
     * menu item.
     */
    protected void _populateHistory(List historyList) {
        //System.out.println("_populateHistory("+historyList.size()+")");

        // Make sure we have a reference to the Recent Files menu
        if (_historyMenu == null) {
            Component[] components = _fileMenu.getMenuComponents();
            for (Component component : components) {
                if (component instanceof JMenu) {
                    JMenu menu = (JMenu) component;
                    String menuText = menu.getText();
                    if (menuText.equals("Recent Files")) {
                        _historyMenu = (JMenu) component;
                    }
                }
            }
            // If we found it, add to set of history menus
            if (_historyMenu != null) {
                _historyMenusAndListeners.put(_historyMenu,
                        _historyMenuListener);
            }
        }

        //System.out.println("number of history menus: " + _historyMenus.size());

        // Update the history menu in each Top
        for (Map.Entry<JMenu, HistoryMenuListener> entry : _historyMenusAndListeners
                .entrySet()) {
            //System.out.println("updating menu " + historyMenu);

            JMenu historyMenu = entry.getKey();

            /*int c =*/MemoryCleaner.removeActionListeners(historyMenu);
            //System.out.println("_historyMenu: "+c);

            historyMenu.removeAll();

            for (int i = 0; i < historyList.size(); i++) {
                String recentFileString = (String) historyList.get(i);
                JMenuItem item = new JMenuItem(recentFileString);
                item.addActionListener(entry.getValue());
                historyMenu.add(item);
            }
        }
    }

    /** Add the name of the last file open or set the name to the
     * first position if already in the list.
     * @param file name of the file to add
     * @param delete If true, remove from the history list, otherwise
     * the file is added to the beginning.
     * @exception IOException If the history file cannot be created, written to,
     * or saved.
     */
    protected void _updateHistory(String file, boolean delete)
            throws IOException {
        List<String> historyList = _readHistory();

        // Remove if already present (then added to first position)
        for (int i = 0; i < historyList.size(); i++) {
            if (historyList.get(i).equals(file)) {
                historyList.remove(i);
            }
        }

        // Remove if depth > limit
        if (historyList.size() >= _historyDepth) {
            historyList.remove(historyList.size() - 1);
        }

        // Add to fist position
        if (!delete) {
            historyList.add(0, file);
        }

        // Serialize history
        _writeHistory(historyList);

        // Update submenu
        _populateHistory(historyList);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Write the model to the specified file.
     *  @param file The file to write to.
     *  @exception IOException If the write fails.
     */
    protected abstract void _writeFile(File file) throws IOException;

    /** Indicator that a close operation is canceled. */
    protected static final int _CANCELED = 2;

    /** Indicator that a file is discarded. */
    protected static final int _DISCARDED = 1;

    /** Indicator that a file save failed. */
    protected static final int _FAILED = 3;

    /** Indicator that a file is saved. */
    protected static final int _SAVED = 0;

    /** The most recent directory used in a file dialog. */
    protected static File _directory = null;

    /** The return value of the _exit() menu.
     *  We use a separate variable here for backward compatibility.
     *  The values of this variable are the values returned by _queryForSave.
     */
    protected int _exitResult = _CANCELED;

    /** The FileFilter that determines what files are displayed by
     *  the Open dialog and the Save As dialog
     *  The initial default is null, which causes no FileFilter to be
     *  applied, which results in all files being displayed.
     */
    protected FileFilter _fileFilter = null;

    /** The FileFilter used by the java.awt.FileDialog that determines
     *  what files are displayed by the Open dialog and the Save As
     *  dialog The initial default is null, which causes no FileFilter
     *  to be applied, which results in all files being displayed.
     *
     *  <p>Note that this class can use either java.awt.FileDialog
     *  or javax.swing.JFileChooser, so classes should set
     *  both _fileFilter and _filenameFilter. Note that it
     *  possible to define an inner class that extends
     *  javax.swing.filechooser.FileFilter and implements
     *  java.io.FilenameFilter and that has two separate accept()
     *  methods.  This inner class can then be set as the
     *  value for both _fileFilter and _filenameFilter:
     *  <pre>
     *  ...
     *      MyFileFilter myFileFilter = new MyFileFilter();
     *      _fileFilter = myFileFilter;
     *      _filenameFilter = myFileFilter;
     *  ...
     *  static MyFileFilter extends FileFilter implements FilenameFilter {
     *      public boolean accept(File file) {
     *         // For FileFilter
     *         return true;
     *      }
     *
     *      public boolean accept(File director, String name) {
     *         // For FilenameFilter
     *         return true;
     *      }
     *
     *      public String getDescription() {
     *         // For FileFilter
     *         return "My filter description.";
     *      }
     *  }
     *  </pre>
     */
    protected FilenameFilter _filenameFilter = null;

    /** File menu for this frame. */
    protected JMenu _fileMenu = new JMenu("File");

    /** Items in the file menu. A null element represents a separator. */
    protected JMenuItem[] _fileMenuItems = _createFileMenuItems();

    /** Index in the _fileMenuItems array of the New item. */
    protected static int _NEW_MENU_INDEX = 2;

    /** Index in the _fileMenuItems array of the Import item. */
    protected static int _IMPORT_MENU_INDEX = 5;

    /** Index in the _fileMenuItems array of the Export item. */
    protected static int _EXPORT_MENU_INDEX = 6;

    /** Help menu for this frame. */
    protected JMenu _helpMenu = new JMenu("Help");

    /** Help menu items. */
    protected JMenuItem[] _helpMenuItems = {
            new JMenuItem("About", KeyEvent.VK_A),
            new JMenuItem("Help", KeyEvent.VK_H), };

    /** Menubar for this frame. */
    protected JMenuBar _menubar = new JMenuBar();

    /** The status bar. */
    protected StatusBar _statusBar = null;

    /** Listener for file menu commands. */
    class FileMenuListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Make this the default context for modal messages.
            UndeferredGraphicalMessageHandler.setContext(Top.this);

            JMenuItem target = (JMenuItem) e.getSource();
            String actionCommand = target.getActionCommand();

            try {
                if (actionCommand.equals("Open File")) {
                    _open();
                } else if (actionCommand.equals("Open URL")) {
                    _openURL();
                } else if (actionCommand.equals("Save")) {
                    _save();
                } else if (actionCommand.equals("Save As")) {
                    _saveAs();
                } else if (actionCommand.equals("Print")) {
                    _print();
                } else if (actionCommand.equals("Close")) {
                    if (!isDisposed()) {
                        Window thisWindow = Top.this;
                        WindowEvent wev = new WindowEvent(thisWindow,
                                WindowEvent.WINDOW_CLOSING);
                        Toolkit toolkit = Toolkit.getDefaultToolkit();
                        EventQueue queue = toolkit.getSystemEventQueue();
                        queue.postEvent(wev);
                    }
                } else if (actionCommand.equals("Exit")) {
                    _exit();
                }
            } catch (Throwable throwable) {
                // If we do not catch exceptions here, then they
                // disappear to stdout, which is bad if we launched
                // where there is no stdout visible.
                MessageHandler.error("File Menu Exception:", throwable);
            }

            // NOTE: The following should not be needed, but jdk1.3beta
            // appears to have a bug in swing where repainting doesn't
            // properly occur.
            repaint();
        }
    }

    /** Listener for help menu commands. */
    class HelpMenuListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Make this the default context for modal messages.
            UndeferredGraphicalMessageHandler.setContext(Top.this);

            JMenuItem target = (JMenuItem) e.getSource();
            String actionCommand = target.getActionCommand();

            try {
                if (actionCommand.equals("About")) {
                    _about();
                } else if (actionCommand.equals("Help")) {
                    _help();
                }
            } catch (Throwable throwable) {
                // If we do not catch exceptions here, then they
                // disappear to stdout, which is bad if we launched
                // where there is no stdout visible.
                MessageHandler.error("Help Menu Exception:", throwable);
            }

            // NOTE: The following should not be needed, but there jdk1.3beta
            // appears to have a bug in swing where repainting doesn't
            // properly occur.
            repaint();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Execute all actions pending on the deferred action list.
    // The list is cleared and the _actionsDeferred variable is set
    // to false, even if one of the deferred actions fails.
    // This method should only be invoked in the event dispatch thread.
    // It is synchronized on the _deferredActions list, so the integrity
    // of that list is ensured, since modifications to that list occur
    // only in other places that are also synchronized on the list.
    private static void _executeDeferredActions() {
        synchronized (_deferredActions) {
            try {
                Iterator actions = _deferredActions.iterator();

                while (actions.hasNext()) {
                    Runnable action = (Runnable) actions.next();
                    action.run();
                }
            } finally {
                _actionsDeferred = false;
                _deferredActions.clear();
            }
        }
    }

    /** Return the value of the history file name.
     *  @return The value of the history file name, which is usually in
     *  the Ptolemy II preferences directory.  The value returned is usually.
     *  "~/.ptolemyII/history.txt".
     *  @exception IOException If thrown while reading the preferences directory.
     */
    private String _getHistoryFileName() throws IOException {
        return StringUtilities.preferencesDirectory() + "history.txt";
    }

    /** Open a file dialog using the java.awt.Dialog class to identify
     *  a file to be opened, and then call _read() to open the file.
     *  Under Mac OS X, this method is preferred over _openJFileChooser()
     *  because this method uses java.awt.Dialog, whereas _openJFileChooser()
     *  uses javax.swing.JFileChooser.
     */
    private void _openFileDialog() {
        FileDialog fileDialog = new FileDialog(this, "Select a model file",
                FileDialog.LOAD);

        if (_filenameFilter != null) {
            fileDialog.setFilenameFilter(_filenameFilter);
        }
        boolean fail = false;
        if (_directory != null) {
            try {
                fileDialog.setDirectory(_directory.getCanonicalPath());
            } catch (IOException ex) {
                fail = true;
            }
        }
        if (fail || _directory == null) {
            // The default on Windows is to open at user.home, which is
            // typically an absurd directory inside the O/S installation.
            // So we use the current directory instead.
            // This will throw a security exception in an applet.
            // FIXME: we should support users under applets opening files
            // on the server.
            String currentWorkingDirectory = StringUtilities
                    .getProperty("user.dir");

            if (currentWorkingDirectory != null) {
                fileDialog.setDirectory(currentWorkingDirectory);
            }
        }

        fileDialog.show();

        if (fileDialog.getDirectory() == null) {
            _directory = new File("");
        } else {
            _directory = new File(fileDialog.getDirectory());
        }

        try {
            // NOTE: It would be nice if it were possible to enter
            // a URL in the file chooser, but Java's file chooser does
            // not permit this, regrettably.  So we have a separate
            // menu item for this.
            File file = new File(_directory, fileDialog.getFile());
            // Report on the time it takes to open the model.
            long startTime = System.currentTimeMillis();
            _read(file.toURI().toURL());
            long endTime = System.currentTimeMillis();
            if (endTime > startTime + 10000) {
                // Only print the time if it is more than 10
                // seconds See also PtolemyEffigy.  Perhaps
                // this code should be in PtolemyEffigy, but
                // if it is here, we get the time it takes to
                // read any file, not just a Ptolemy model.
                System.out.println("Opened " + file + " in "
                        + (System.currentTimeMillis() - startTime) + " ms.");
            }
            // Only add file if no exception
            _updateHistory(file.getAbsolutePath(), false);

        } catch (Error error) {
            // Be sure to catch Error here so that if we throw an
            // Error, then we will report it to with a window.
            try {
                throw new RuntimeException(error);
            } catch (Exception ex2) {
                report("Error while reading input:", ex2);
            }
        } catch (Exception ex) {
            // NOTE: The XML parser can only throw an
            // XmlException.  It signals that it is a user
            // cancellation with the special string pattern
            // "*** Canceled." in the message.

            if (ex.getMessage() != null
                    && !ex.getMessage().startsWith("*** Canceled.")) {
                // No need to report a CancelException, since
                // it results from the user clicking a
                // "Cancel" button.
                report("Error reading input", ex);
            }
        }
    }

    /** Open a file dialog to identify a file to be opened, and then call
     *  _read() to open the file.
     */
    private void _openJFileChooser() {
        // Swap backgrounds and avoid white boxes in "common places" dialog
        JFileChooserBugFix jFileChooserBugFix = new JFileChooserBugFix();
        Color background = null;
        try {
            background = jFileChooserBugFix.saveBackground();

            JFileChooser fileDialog = new JFileChooser();

            // To disable the Windows places bar on the left, uncomment the
            // line below.
            // fileDialog.putClientProperty("FileChooser.useShellFolder", Boolean.FALSE);
            if (_fileFilter != null) {
                fileDialog.addChoosableFileFilter(_fileFilter);
            }

            fileDialog.setDialogTitle("Select a model file.");

            if (_directory != null) {
                fileDialog.setCurrentDirectory(_directory);
            } else {
                // The default on Windows is to open at user.home, which is
                // typically an absurd directory inside the O/S installation.
                // So we use the current directory instead.
                // This will throw a security exception in an applet.
                // FIXME: we should support users under applets opening files
                // on the server.
                String currentWorkingDirectory = StringUtilities
                        .getProperty("user.dir");

                if (currentWorkingDirectory != null) {
                    fileDialog.setCurrentDirectory(new File(
                            currentWorkingDirectory));
                }
            }

            if (fileDialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                _directory = fileDialog.getCurrentDirectory();

                try {
                    // NOTE: It would be nice if it were possible to enter
                    // a URL in the file chooser, but Java's file chooser does
                    // not permit this, regrettably.  So we have a separate
                    // menu item for this.
                    File file = fileDialog.getSelectedFile().getCanonicalFile();
                    // Report on the time it takes to open the model.
                    long startTime = System.currentTimeMillis();
                    _read(file.toURI().toURL());
                    long endTime = System.currentTimeMillis();
                    if (endTime > startTime + 10000) {
                        // Only print the time if it is more than 10
                        // seconds See also PtolemyEffigy.  Perhaps
                        // this code should be in PtolemyEffigy, but
                        // if it is here, we get the time it takes to
                        // read any file, not just a Ptolemy model.
                        System.out.println("Opened " + file + " in "
                                + (System.currentTimeMillis() - startTime)
                                + " ms.");
                    }
                    // Only add file if no exception
                    _updateHistory(file.getAbsolutePath(), false);

                } catch (Error error) {
                    // Be sure to catch Error here so that if we throw an
                    // Error, then we will report it to with a window.
                    try {
                        throw new RuntimeException(error);
                    } catch (Exception ex2) {
                        report("Error while reading input:", ex2);
                    }
                } catch (Exception ex) {
                    // NOTE: The XML parser can only throw an
                    // XmlException.  It signals that it is a user
                    // cancellation with the special string pattern
                    // "*** Canceled." in the message.

                    if (ex.getMessage() != null
                            && !ex.getMessage().startsWith("*** Canceled.")) {
                        // No need to report a CancelException, since
                        // it results from the user clicking a
                        // "Cancel" button.
                        report("Error reading input", ex);
                    }
                }
            }
        } finally {
            jFileChooserBugFix.restoreBackground(background);
        }
    }

    /** If we are running under Mac OS X and Java 1.5, the print a
     * message about printing problems.
     */
    private static void _macCheck() {
        if (PtGUIUtilities.macOSLookAndFeel()
                && System.getProperty("java.version").startsWith("1.5")) {
            System.out
            .println("Warning, under Mac OS X with Java 1.5, printing might "
                    + "not work.  Try recompiling with Java 1.6 or setting a property:\n"
                    + "export JAVAFLAGS=-Dptolemy.ptII.print.platform=CrossPlatform\n"
                    + "and restarting vergil: $PTII/bin/vergil");
        }
    }

    /** Initialize the menus and key bindings for Mac OS X. */
    private void _macInitializer() {
        try {
            // To set the about name, set the
            // com.apple.mrj.application.apple.menu.about.name
            // property in the main thread, not the event thread.  See
            // VergilApplication.java
            MacOSXAdapter.setAboutMethod(this,
                    getClass().getMethod("about", (Class[]) null));
            MacOSXAdapter.setQuitMethod(this,
                    getClass().getMethod("exit", (Class[]) null));
        } catch (NoSuchMethodException ex) {
            report("Mac OS X specific initializations failed.", ex);
        }
    }

    /** Get the history from the file that contains names
     * Always return a list, that can be empty
     * @return list of file history
     */
    private List<String> _readHistory() throws IOException {
        ArrayList<String> historyList = new ArrayList<String>();
        String historyFileName = _getHistoryFileName();
        if (!new File(historyFileName).exists()) {
            // No history file, so just return
            return historyList;
        }
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(historyFileName);
            bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                historyList.add(line);
            }
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }

        return historyList;
    }

    /** Query the user for a filename and save the model to that file.
     *  @return True if the save succeeds.
     */
    private boolean _saveAsFileDialogImplementation() {
        // This method name ends with "Implementation" because there
        // already was a _saveAsFileDialog() method

        // Use the strategy pattern here to create the actual
        // dialog so that subclasses can customize this dialog.
        FileDialog fileDialog = _saveAsFileDialogComponent();

        if (fileDialog == null) {
            // Action was canceled.
            return false;
        }

        fileDialog.show();

        _directory = new File(fileDialog.getDirectory());

        _file = new File(_directory, fileDialog.getFile());

        // If the file exists, then FileDialog will prompt the user
        // so we don't want to prompt them again.
        // See "saving xml to existing file asks twice on mac"
        // http://bugzilla.ecoinformatics.org/show_bug.cgi?id=5760
        //         if (_file.exists()) {
        //             // Ask for confirmation before overwriting a file.
        //             String query = "Overwrite " + _file.getName() + "?";
        //
        //             // Show a MODAL dialog
        //             int selected = JOptionPane.showOptionDialog(this, query,
        //                     "Save Changes?", JOptionPane.YES_NO_OPTION,
        //                     JOptionPane.QUESTION_MESSAGE, null, null, null);
        //
        //             if (selected == 1) {
        //                 return false;
        //             }
        //         }

        // Truncate the name so that dialogs under Web Start on the Mac
        // work better.
        setTitle(StringUtilities.abbreviate(_getName()));
        return _save();
    }

    /** Query the user for a filename and save the model to that file.
     *  @return True if the save succeeds.
     */
    private boolean _saveAsJFileChooserImplementation() {
        // Swap backgrounds and avoid white boxes in "common places" dialog
        JFileChooserBugFix jFileChooserBugFix = new JFileChooserBugFix();
        Color background = null;
        try {
            background = jFileChooserBugFix.saveBackground();

            // Use the strategy pattern here to create the actual
            // dialog so that subclasses can customize this dialog.
            JFileChooser fileDialog = _saveAsFileDialog();

            // Under Java 1.6 and Mac OS X, showSaveDialog() ignores the filter.
            if (fileDialog.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                _file = fileDialog.getSelectedFile();

                if (_file.exists()) {
                    // Ask for confirmation before overwriting a file.
                    String query = "Overwrite " + _file.getName() + "?";

                    // Show a MODAL dialog
                    int selected = JOptionPane.showOptionDialog(this, query,
                            "Save Changes?", JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE, null, null, null);

                    if (selected == 1) {
                        return false;
                    }
                }

                // Truncate the name so that dialogs under Web Start on the Mac
                // work better.
                setTitle(StringUtilities.abbreviate(_getName()));
                _directory = fileDialog.getCurrentDirectory();
                return _save();
            }

            // Action was canceled.
            return false;
        } finally {
            jFileChooserBugFix.restoreBackground(background);
        }
    }

    /** Write history to the file defined by _getHistoryFileName(). */
    private void _writeHistory(List<String> historyList) throws IOException {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(_getHistoryFileName());
            for (String line : historyList) {
                fileWriter.write(line + "\n");
            }
        } finally {
            if (fileWriter != null) {
                fileWriter.close();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Set to true to print closing sequence information to standard
     * out.
     */
    protected boolean _debugClosing = false;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A runnable for showing the window. */
    class ShowWindowRunnable implements Runnable {
        @Override
        public void run() {
            // NOTE: We used to call pack() here, but this would
            // override any manual changes in sizing that had been
            // made.
            setState(Frame.NORMAL);
            // FIXME: show() is deprecated, but calling setVisible()
            // here results in a loop.
            //Top.super.setVisible(true);
            Top.super.show();

        }
    }

    /** A runnable for setting the status bar message for a report. */
    class StatusBarMessageReportRunnable implements Runnable {
        public StatusBarMessageReportRunnable(String message,
                Throwable throwable) {
            _message = message;
            _throwable = throwable;
        }

        @Override
        public void run() {
            if (_statusBar != null) {
                _statusBar.setMessage(MessageHandler
                        .shortDescription(_throwable) + ". " + _message);
            }

            MessageHandler.error(_message, _throwable);
        }

        private String _message;
        private Throwable _throwable;
    }

    /** A runnable for setting the status bar message. */
    class StatusBarMessageRunnable implements Runnable {
        public StatusBarMessageRunnable(String message) {
            _message = message;
        }

        @Override
        public void run() {
            if (_statusBar != null) {
                _statusBar.setMessage(_message);
            }
        }

        private String _message;
    }

    /** A runnable for packing the Window. */
    class DoPackRunnable implements Runnable {
        @Override
        public void run() {
            // NOTE: This always runs in the swing thread,
            // so there is no need to synchronize.
            if (!_menuPopulated) {
                // Set up the menus.
                _fileMenu.setMnemonic(KeyEvent.VK_F);
                _helpMenu.setMnemonic(KeyEvent.VK_H);

                // Construct the File menu by adding action commands
                // and action listeners.

                // Set the action command and listener for each menu item.
                for (JMenuItem _fileMenuItem : _fileMenuItems) {
                    if (_fileMenuItem == null) {
                        _fileMenu.addSeparator();
                    } else {
                        _fileMenuItem.setActionCommand(_fileMenuItem.getText());
                        _fileMenuItem.addActionListener(_fileMenuListener);
                        _fileMenu.add(_fileMenuItem);
                    }
                }

                _menubar.add(_fileMenu);

                // History fill
                try {
                    _populateHistory(_readHistory());
                } catch (IOException ex) {
                    // Ignore
                } catch (SecurityException ex) {
                    // Ignore
                }

                // Construct the Help menu by adding action commands
                // and action listeners.

                // Set the action command and listener for each menu item.
                for (JMenuItem _helpMenuItem : _helpMenuItems) {
                    _helpMenuItem.setActionCommand(_helpMenuItem.getText());
                    _helpMenuItem.addActionListener(_helpMenuListener);
                    _helpMenu.add(_helpMenuItem);
                }

                // Unfortunately, at this time, Java provides no
                // mechanism for derived classes to insert menus
                // at arbitrary points in the menu bar.  Also, the
                // menubar ignores the alignment property of the
                // JMenu.  By convention, however, we want the
                // help menu to be the rightmost menu.  Thus, we
                // use a strategy pattern here, and call a
                // protected method that derived classes can use
                // to add menus.
                _addMenus();

                _menubar.add(_helpMenu);

                if (!_hideMenuBar) {
                    setJMenuBar(_menubar);
                }

                // Add the status bar, if there is one.
                if (_statusBar != null) {
                    getContentPane().add(_statusBar, BorderLayout.SOUTH);
                }
            }

            Top.super.pack();

            if (_centering) {
                centerOnScreen();
            }
            _menuPopulated = true;
        }
    }

    /** A runnable for executing deferred actions. */
    static class DeferredActionsRunnable implements Runnable {
        @Override
        public void run() {
            _executeDeferredActions();
        }
    }

    /** A runnable for closing the window. */
    class CloseWindowRunnable implements Runnable {
        @Override
        public void run() {
            _close();
        }
    }

    /** A runnable for centering the window on the screen. */
    class CenterOnScreenRunnable implements Runnable {
        @Override
        public void run() {
            Toolkit tk = Toolkit.getDefaultToolkit();
            setLocation((tk.getScreenSize().width - getSize().width) / 2,
                    (tk.getScreenSize().height - getSize().height) / 2);

            // Make this the default context for modal messages.
            UndeferredGraphicalMessageHandler.setContext(Top.this);
        }
    }

    /** A runnable for setting the background color of the status bar. */
    class SetBackgroundRunnable implements Runnable {
        @Override
        public void run() {
            Top.super.setBackground(_statusBarBackground);

            // This seems to be called in a base class
            // constructor, before this variable has been
            // set. Hence the test against null.
            if (_statusBar != null) {
                _statusBar.setBackground(_statusBarBackground);
            }
        }
    }

    /** Listener for windowClosing action. */
    class CloseWindowAdapter extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            if (_debugClosing) {
                System.out.println("Top$CloseWindowAdapter.windowClosing() : "
                        + Top.this.getName());
            }

            Window window = e.getWindow();
            if (window instanceof Top) {
                Top top = (Top) window;
                if (!top.isDisposed()) {
                    _close();
                }
            }
        }
    }

    /** Listener for history menu commands. */
    class HistoryMenuListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            // Make this the default context for modal messages.
            UndeferredGraphicalMessageHandler.setContext(Top.this);

            JMenuItem target = (JMenuItem) event.getSource();
            String path = target.getActionCommand();

            File file = new File(path);

            // See if the file is still there.
            if (!file.exists()) {
                MessageHandler.error("File no longer exists.");
                try {
                    _updateHistory(path, true);
                } catch (IOException ex2) {
                    // Ignore
                }
                return;
            }

            // Try to read the file.
            try {
                _read(file.toURI().toURL());
            } catch (Exception e) {
                MessageHandler.error("Error reading " + file.getAbsolutePath(),
                        e);
                try {
                    _updateHistory(path, true);
                } catch (IOException ex2) {
                    // Ignore
                }
                return;
            }

            // Try to move the file to the top of the history menu.
            try {
                _updateHistory(path, false);
            } catch (IOException e) {
                MessageHandler.error("Error updating history file.", e);
                return;
            }

            setDirectory(file.getParentFile());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** An ActionListener for the menu items in the file menu. */
    private FileMenuListener _fileMenuListener;

    /** An ActionListener for the menu items in the help menu. */
    private HelpMenuListener _helpMenuListener;

    /** An ActionListener for the menu items in the history menu. */
    private HistoryMenuListener _historyMenuListener;

    /** A reference to the history menu. */
    private JMenu _historyMenu;

    /** A mapping of history menu to history menu listener for all open Top
     *  windows.
     */
    private static Map<JMenu, HistoryMenuListener> _historyMenusAndListeners = Collections
            .synchronizedMap(new WeakHashMap<JMenu, HistoryMenuListener>());

    /** The background color of the status bar */
    private Color _statusBarBackground;

    /** Indicator of whether actions are deferred. */
    private static boolean _actionsDeferred = false;

    // A flag indicating whether or not to center the window.
    private boolean _centering = true;

    /** List of deferred actions. */
    private static List _deferredActions = new LinkedList();

    /** True if this frame has been disposed. */
    private boolean _disposed = false;

    /** The input file. */
    private File _file = null;

    /** Flag to hide the menu bar. */
    private boolean _hideMenuBar = false;

    /** The most recently entered URL in Open URL. */
    private String _lastURL = "http://ptolemy.eecs.berkeley.edu/xml/models/";

    /** History depth. */
    private int _historyDepth = 10;

    /** Indicator that the menu has been populated. */
    private boolean _menuPopulated = false;

    /** Indicator that the data represented in the window has been modified. */
    private boolean _modified = false;

    /** True if we have printed the securityException message. */
    private static boolean _printedSecurityExceptionMessage = false;

}
