/* Top-level window with a menubar and status bar.

 Copyright (c) 1998-2003 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (janneck@eecs.berkeley.edu)
*/

package ptolemy.gui;

import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

//////////////////////////////////////////////////////////////////////////
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
in order, Open File, Open URL, New, Save, SaveAs, Print, Close, and Exit.
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
*/
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
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    _close();
                }
            });

        getContentPane().setLayout(new BorderLayout());

        // Make this the default context for modal messages.
        GraphicalMessageHandler.setContext(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Center the window on the screen.  This must be called after the
     *  window is populated with its contents, since it depends on the size
     *  being known. If this method is called from a thread that is not
     *  the AWT event dispatch thread, then its execution is deferred
     *  and performed in that thread.
     */
    public void centerOnScreen() {
        Runnable doCenter = new Runnable() {
            public void run() {
                Toolkit tk = Toolkit.getDefaultToolkit();
                setLocation((tk.getScreenSize().width - getSize().width)/2,
                        (tk.getScreenSize().height - getSize().height)/2);
                // Make this the default context for modal messages.
                GraphicalMessageHandler.setContext(Top.this);
            }
        };
        deferIfNecessary(doCenter);
    }
    
    /** Close the window, prompting the user to save changes if there
     *  have been any.  Derived classes should override the protected
     *  method _close(), not this one. This method returns immediately
     *  if it is called outside the swing UI thread, deferring the action
     *  so that it is executed in the swing thread.
     */
    public final void close() {
        Runnable doClose = new Runnable() {
            public void run() {
                _close();
            }
        };
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
            synchronized(_deferredActions) {
                // Add the specified action to the list of actions to perform.
                _deferredActions.add(action);

                // If it hasn't already been requested, request that actions
                // be performed in the event dispatch thread.
                if (!_actionsDeferred) {
                    Runnable doActions = new Runnable() {
                            public void run() {
                                _executeDeferredActions();
                            }
                        };
                    try {
                        // NOTE: Using invokeAndWait() here risks causing
                        // deadlock.  Don't do it!
                        SwingUtilities.invokeLater(doActions);
                    } catch (Exception ex) {
                        // Ignore InterruptedException.
                        // Other exceptions should not occur.
                    }
                    _actionsDeferred = true;
                }
            }
        }
    }


    /** Return true if the window is set to be centered when pack() is called.
     *  @return True if the window will be centered when pack is called.
     */
    public boolean getCentering() {
        return _centering;
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

    /** Report a message to the user by displaying it in a status bar,
     *  if there is one. If this method is called outside the AWT event
     *  thread, then its execution is deferred and performed in that thread.
     *  @param message The message to report.
     */
    public void report(final String message) {
        Runnable doReport = new Runnable() {
            public void run() {
                if (_statusBar != null) {
                    _statusBar.setMessage(message);
                }
            }
        };
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
        Runnable doReport = new Runnable() {
            public void run() {
                if (_statusBar != null) {
                    _statusBar.setMessage(MessageHandler.shortDescription(throwable)
                            + ". " + message);
                }
                MessageHandler.error(message, throwable);
            }
        };
        deferIfNecessary(doReport);
    }

    /** Set background color.  This overrides the base class to set the
     *  background of the status bar. If this method is called outside
     *  the AWT event thread, then its execution is deferred and
     *  performed in that thread.
     *  @param background The background color.
     */
    public void setBackground(final Color background) {
        Runnable doSet = new Runnable() {
            public void run() {
                Top.super.setBackground(background);
                // This seems to be called in a base class constructor, before
                // this variable has been set. Hence the test against null.
                if (_statusBar != null) {
                    _statusBar.setBackground(background);
                }
            }
        };
        deferIfNecessary(doSet);
    }

    /** Specify whether or not to center the window on the screen when
     *  packing it.  The default is true.
     *  @param centering Set to false to disable centering.
     */
    public void setCentering(boolean centering) {
        _centering = centering;
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
    public void pack() {
        Runnable doPack = new Runnable() {
            public void run() {
                if (!_menuPopulated) {
                    _menuPopulated = true;

                    // Set up the menus.
                    _fileMenu.setMnemonic(KeyEvent.VK_F);
                    _helpMenu.setMnemonic(KeyEvent.VK_H);

                    // Open button = ctrl-o.
                    _fileMenuItems[0].setAccelerator(
                            KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK));

                    // The mnemonic isn't set in the static initializer because
                    // JMenu doesn't have an appropriate constructor.
                    _fileMenuItems[2].setMnemonic(KeyEvent.VK_N);
                    // New button disabled by default.
                    _fileMenuItems[2].setEnabled(false);

                    // Save button = ctrl-s.
                    _fileMenuItems[3].setAccelerator(
                            KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));

                    // Print button = ctrl-p.
                    _fileMenuItems[5].setAccelerator(
                            KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK));
                    // Print button disabled by default, unless this class implements
                    // one of the JDK1.2 printing interfaces.
                    if (Top.this instanceof Printable ||
                            Top.this instanceof Pageable) {
                        _fileMenuItems[5].setEnabled(true);
                    } else {
                        _fileMenuItems[5].setEnabled(false);
                    }

                    // Close button = ctrl-w.
                    _fileMenuItems[6].setAccelerator(
                            KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.CTRL_MASK));

                    // Construct the File menu by adding action commands
                    // and action listeners.
                    FileMenuListener fileMenuListener = new FileMenuListener();
                    // Set the action command and listener for each menu item.
                    for (int i = 0; i < _fileMenuItems.length; i++) {
                        _fileMenuItems[i].setActionCommand(_fileMenuItems[i].getText());
                        _fileMenuItems[i].addActionListener(fileMenuListener);
                        _fileMenu.add(_fileMenuItems[i]);
                    }
                    _menubar.add(_fileMenu);

                    // Construct the Help menu by adding action commands
                    // and action listeners.
                    HelpMenuListener helpMenuListener = new HelpMenuListener();
                    // Set the action command and listener for each menu item.
                    for (int i = 0; i < _helpMenuItems.length; i++) {
                        _helpMenuItems[i].setActionCommand(
                                _helpMenuItems[i].getText());
                        _helpMenuItems[i].addActionListener(helpMenuListener);
                        _helpMenu.add(_helpMenuItems[i]);
                    }

                    // Unfortunately, at this time, Java provides no mechanism for
                    // derived classes to insert menus at arbitrary points in the
                    // menu bar.  Also, the menubar ignores the alignment property
                    // of the JMenu.  By convention, however, we want the help menu to
                    // be the rightmost menu.  Thus, we use a strategy pattern here,
                    // and call a protected method that derived classes can use to
                    // add menus.
                    _addMenus();

                    _menubar.add(_helpMenu);

                    setJMenuBar(_menubar);

                    // Add the status bar, if there is one.
                    if (_statusBar != null) {
                        getContentPane().add(_statusBar, BorderLayout.SOUTH);
                    }
                }
                Top.super.pack();
                if (_centering) {
                    centerOnScreen();
                }
            }
        };
        deferIfNecessary(doPack);
    }

    /** Override the base class to deiconify
     *  the window, if necessary. If this method is called
     *  outside the AWT event thread, then its execution is deferred and
     *  performed in that thread.
     */
    public void show() {
        Runnable doShow = new Runnable() {
            public void run() {
                // NOTE: We used to call pack() here, but this would override any manual
                // changes in sizing that had been made.
                setState(Frame.NORMAL);
                Top.super.show();
            }
        };
        deferIfNecessary(doShow);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Open a dialog with basic information about this window.
     */
    protected void _about() {
        JOptionPane.showMessageDialog(this,
                "Ptolemy II " + getClass().getName() + "\n" +
                "By: Claudius Ptolemaeus, ptolemy@eecs.berkeley.edu\n" +
                "For more information, see\n" +
                "http://ptolemy.eecs.berkeley.edu/ptolemyII\n\n" +
                "Copyright (c) 1997-2003, " +
                "The Regents of the University of California.",
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
     *  @return False if the user cancels the clear.
     */
    protected boolean _clear() {
        return _queryForSave();
    }

    /** Close the window.  Derived classes should override this to
     *  release any resources or remove any listeners.  In this class,
     *  if the data associated with this window has been modified, as
     *  indicated by isModified(), then ask the user whether to save
     *  the data before closing.
     *  @return False if the user cancels on a save query.
     */
    protected boolean _close() {
        // NOTE: We use dispose() here rather than just hiding the
        // window.  This ensures that derived classes can react to
        // windowClosed events rather than overriding the
        // windowClosing behavior given here.
        if (isModified()) {
            if (_queryForSave()) {
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

    /** Exit the application after querying the user to save data.
     *  Derived classes should override this to do something more
     *  reasonable, so that user data is not discarded.
     */
    protected void _exit() {
        if (isModified()) {
            if (_queryForSave()) {
                System.exit(0);
            }
        } else {
            // Window is not modified, so just exit.
            System.exit(0);
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
     *  _read() to open the file.
     */
    protected void _open() {
        JFileChooser fileDialog = new JFileChooser();
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
            String cwd = StringUtilities.getProperty("user.dir");
            if (cwd != null) {
                fileDialog.setCurrentDirectory(new File(cwd));
            }
        }
        if (fileDialog.showOpenDialog(this)
                == JFileChooser.APPROVE_OPTION) {
            _directory = fileDialog.getCurrentDirectory();
            try {
                // NOTE: It would be nice if it were possible to enter
                // a URL in the file chooser, but Java's file chooser does
                // not permit this, regrettably.  So we have a separate
                // menu item for this.
                File file = fileDialog.getSelectedFile().getCanonicalFile();
                _read(file.toURL());
            } catch (Error error) {
                // Be sure to catch Error here so that if we throw an
                // Error, then we will report it to with a window.
                // FIXME: The report() methods and
                // the MessageHandler class should take Throwable
                // as an argument instead of taking an vException.
                try {
                    throw new RuntimeException(error);
                } catch (Exception ex2) {
                    report("Error while reading input:", ex2);
                }
            } catch (Exception ex) {
                // NOTE: The XML parser can only throw an XmlException.
                // It signals that it is a user cancellation with the special
                // string pattern "*** Canceled." in the message.
                if (ex.getMessage() != null
                        && !ex.getMessage().startsWith("*** Canceled.")) {
                    // No need to report a CancelException, since it results
                    // from the user clicking a "Cancel" button.
                    report("Error reading input", ex);
                }
            }
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
        PrinterJob job = PrinterJob.getPrinterJob();
        if (this instanceof Pageable) {
            job.setPageable((Pageable)this);
        } else if (this instanceof Printable) {
            PageFormat format = job.pageDialog(job.defaultPage());
            job.setPrintable((Printable)this, format);
        } else {
            // Can't print it.
            return;
        }
        if (job.printDialog()) {
            try {
                job.print();
            } catch (Exception ex) {
                MessageHandler.error("Printing Failed", ex);
            }
        }
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
     *  @return True if the save succeeds.
     */
    protected boolean _saveAs() {
        JFileChooser fileDialog = new JFileChooser();
        if (_fileFilter != null) {
            fileDialog.addChoosableFileFilter(_fileFilter);
        }
        fileDialog.setDialogTitle("Save as...");
        if (_directory != null) {
            fileDialog.setCurrentDirectory(_directory);
        } else {
            // The default on Windows is to open at user.home, which is
            // typically an absurd directory inside the O/S installation.
            // So we use the current directory instead.
            // This will fail with a security exception in applets.
            // FIXME: we should support users under applets opening files
            // on the server.
            String cwd = StringUtilities.getProperty("user.dir");
            if (cwd != null) {
                fileDialog.setCurrentDirectory(new File(cwd));
            }
        }
        int returnVal = fileDialog.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            _file = fileDialog.getSelectedFile();
            if (_file.exists()) {
                // Ask for confirmation before overwriting a file.
                String query = "Overwrite " + _file.getName() + "?";
                // Show a MODAL dialog
                int selected = JOptionPane.showOptionDialog(
                        this,
                        query,
                        "Save Changes?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        null,
                        null);

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
    }

    /** Write the model to the specified file.
     *  @param file The file to write to.
     *  @exception IOException If the write fails.
     */
    protected abstract void _writeFile(File file) throws IOException;

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The most recent directory used in a file dialog. */
    protected static File _directory = null;

    /** The FileFilter that determines what files are displayed by
     *  the Open dialog and the Save As dialog
     *  The initial default is null, which causes no FileFilter to be
     *  applied, which results in all files being displayed.
     */
    protected FileFilter _fileFilter = null;

    /** File menu for this frame. */
    protected JMenu _fileMenu = new JMenu("File");

    /** Items in the file menu. */
    protected JMenuItem[] _fileMenuItems = {
        new JMenuItem("Open File", KeyEvent.VK_O),
        new JMenuItem("Open URL", KeyEvent.VK_U),
        new JMenu("New"),
        new JMenuItem("Save", KeyEvent.VK_S),
        new JMenuItem("SaveAs", KeyEvent.VK_A),
        new JMenuItem("Print", KeyEvent.VK_P),
        new JMenuItem("Close", KeyEvent.VK_C),
        new JMenuItem("Exit", KeyEvent.VK_X),
    };

    /** Help menu for this frame. */
    protected JMenu _helpMenu = new JMenu("Help");

    /** Help menu items. */
    protected JMenuItem[] _helpMenuItems = {
        new JMenuItem("About", KeyEvent.VK_A),
        new JMenuItem("Help", KeyEvent.VK_H),
    };

    /** Menubar for this frame. */
    protected JMenuBar _menubar = new JMenuBar();

    /** The status bar. */
    protected StatusBar _statusBar = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Indicator of whether actions are deferred. */
    private static boolean _actionsDeferred = false;

    /** List of deferred actions. */
    private static List _deferredActions = new LinkedList();
    
    // The input file.
    private File _file = null;

    // The most recently entered URL in Open URL.
    private String _lastURL = "http://ptolemy.eecs.berkeley.edu/xml/models/";

    // Indicator that the menu has been populated.
    private boolean _menuPopulated = false;

    // Indicator that the data represented in the window has been modified.
    private boolean _modified = false;

    // A flag indicating whether or not to center the window.
    private boolean _centering = true;

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
        synchronized(_deferredActions) {
            try {
                Iterator actions = _deferredActions.iterator();
                while (actions.hasNext()) {
                    Runnable action = (Runnable)actions.next();
                    action.run();
                }
            } finally {
                _actionsDeferred = false;
                _deferredActions.clear();
            }
        }
    }
    
    // Open a dialog to prompt the user to save the data.
    // Return false if the user clicks "cancel", and otherwise return true.
    private boolean _queryForSave() {
        Object[] options = {"Save", "Discard changes", "Cancel"};


        String query = "Save changes to "
            + StringUtilities.split(_getName()) + "?";


        // Show the MODAL dialog
        int selected = JOptionPane.showOptionDialog(
                this,
                query,
                "Save Changes?",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (selected == 0) {
            return _save();
        } else if (selected == 1) {
            return true;
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Listener for file menu commands. */
    class FileMenuListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Make this the default context for modal messages.
            GraphicalMessageHandler.setContext(Top.this);

            JMenuItem target = (JMenuItem)e.getSource();
            String actionCommand = target.getActionCommand();
            try {
                if (actionCommand.equals("Open File")) {
                    _open();
                } else if (actionCommand.equals("Open URL")) {
                    _openURL();
                } else if (actionCommand.equals("Save")) {
                    _save();
                } else if (actionCommand.equals("SaveAs")) {
                    _saveAs();
                } else if (actionCommand.equals("Print")) {
                    _print();
                } else if (actionCommand.equals("Close")) {
                    _close();
                } else if (actionCommand.equals("Exit")) {
                    _exit();
                }
            } catch (Exception exception) {
                // If we do not catch exceptions here, then they
                // disappear to stdout, which is bad if we launched
                // where there is no stdout visible.
                MessageHandler.error("File Menu Exception:", exception);
            }
            // NOTE: The following should not be needed, but jdk1.3beta
            // appears to have a bug in swing where repainting doesn't
            // properly occur.
            repaint();
        }
    }

    /** Listener for help menu commands. */
    class HelpMenuListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Make this the default context for modal messages.
            GraphicalMessageHandler.setContext(Top.this);

            JMenuItem target = (JMenuItem)e.getSource();
            String actionCommand = target.getActionCommand();
            try {
                if (actionCommand.equals("About")){
                    _about();
                } else if (actionCommand.equals("Help")) {
                    _help();
                }
            } catch (Exception exception) {
                // If we do not catch exceptions here, then they
                // disappear to stdout, which is bad if we launched
                // where there is no stdout visible.
                MessageHandler.error("Help Menu Exception:", exception);
            }
            // NOTE: The following should not be needed, but there jdk1.3beta
            // appears to have a bug in swing where repainting doesn't
            // properly occur.
            repaint();
        }
    }
}
