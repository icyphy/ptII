/* Top-level window with a menubar and status bar.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.gui;

// Java imports
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.JPanel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Event;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.Pageable;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.io.*;
import java.net.URL;

//////////////////////////////////////////////////////////////////////////
//// Top
/**
This is a top-level window with a menubar and status bar.
Derived classes should add components to the content pane using a
line like:
<pre>
    getContentPane().add(component, BorderLayout.CENTER);
</pre>
Derived classes may wish to modify the menus.  The File
and Help menus are exposed as protected members.
The File menu items in the _fileMenuItems protected array are,
in order, Open, New, Save, SaveAs, Print, and Close.
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
    _fileMenuItems[2].setEnabled(false);
</pre>
<p>
Some menu items are provided, but are disabled by default.
The "New" item, for example, can be enabled with
<pre>
    _fileMenuItems[1].setEnabled(true);
</pre>
A derived class that enables this menu item can populate the menu with
submenu items.  This particular entry in the _fileMenuItems[1]
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
is first made visible.

@author Edward A. Lee and Steve Neuendorffer
@version $Id$
*/
public abstract class Top extends JFrame {

    /** Construct an empty top-level frame.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  It may also be desirable to call centerOnScreen().
     */
    public Top() {
        super();

        // Ensure that user is prompted before closing if the data
        // has been modified.
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
                _close();
	    }
	});

        getContentPane().setLayout(new BorderLayout());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Center the window on the screen.  This must be called after the
     *  window is populated with its contents, since it depends on the size
     *  being known.
     */
    public void centerOnScreen() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        setLocation((tk.getScreenSize().width - getSize().width)/2,
               (tk.getScreenSize().height - getSize().height)/2);
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

    /** Report an exception.  This displays a message in a dialog and
     *  prints the stack trace to the standard error stream.
     *  @param ex The exception to report.
     */
    public void report(Exception ex) {
	report("", ex);
    }

    /** Report a message to the user by displaying it in a status bar.
     *  @param message The message to report.
     */
    public void report(String message) {
	_statusBar.setMessage(message);
    }

    /** Report an exception.  This pops up a window with the option
     *  of examining the stack trace.
     *  @param message The message.
     *  @param ex The exception to report.
     */
    public void report(String message, Exception ex) {
        _statusBar.setMessage("Exception. " + message);
        MessageHandler.error("Exception thrown.", ex);
    }

    /** Set background color.  This overrides the base class to set the
     *  background of contained ModelPane.
     *  @param background The background color.
     */
    public void setBackground(Color background) {
        super.setBackground(background);
        // This seems to be called in a base class constructor, before
        // this variable has been set. Hence the test against null.
        if (_statusBar != null) _statusBar.setBackground(background);
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

    /** If the argument is true, then make the window visible; otherwise,
     *  make it invisible.  This
     *  overrides the base class to populate the menu bar if the argument
     *  is true and they have not already been populated.  This
     *  is done here rather than in the constructor so that derived
     *  classes are assured that their constructors have been fully
     *  executed when _addMenus() is called.
     *  @param flag True to make the window visible.
     */
    public void setVisible(boolean flag) {
        if (flag && !_menuPopulated) {
            _menuPopulated = true;

            // Set up the menus.
            _fileMenu.setMnemonic(KeyEvent.VK_F);
            _helpMenu.setMnemonic(KeyEvent.VK_H);
            
            // Open button = ctrl-o.
            _fileMenuItems[0].setAccelerator(
                    KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK));

            // The mnemonic isn't set in the static initializer because
            // JMenu doesn't have an appropriate constructor.
            _fileMenuItems[1].setMnemonic(KeyEvent.VK_N);
            // New button disabled by default.
            _fileMenuItems[1].setEnabled(false);

            // Save button = ctrl-s.
            _fileMenuItems[2].setAccelerator(
                    KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));

            // Print button = ctrl-p.
            _fileMenuItems[4].setAccelerator(
                    KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK));
            // Print button disabled by default, unless this class implements
	    // one of the JDK1.2 printing interfaces.
            if(this instanceof Printable ||
	       this instanceof Pageable) {
		_fileMenuItems[4].setEnabled(true);
	    } else {
		_fileMenuItems[4].setEnabled(false);
	    }

            // Close button = ctrl-w.
            _fileMenuItems[5].setAccelerator(
                    KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.CTRL_MASK));

            FileMenuListener fml = new FileMenuListener();
            // Set the action command and listener for each menu item.
            for(int i = 0; i < _fileMenuItems.length; i++) {
                _fileMenuItems[i].setActionCommand(_fileMenuItems[i].getText());
                _fileMenuItems[i].addActionListener(fml);
                _fileMenu.add(_fileMenuItems[i]);
            }
            _menubar.add(_fileMenu);

            HelpMenuListener sml = new HelpMenuListener();
            // Set the action command and listener for each menu item.
            for(int i = 0; i < _helpMenuItems.length; i++) {
                _helpMenuItems[i].setActionCommand(
                        _helpMenuItems[i].getText());
                _helpMenuItems[i].addActionListener(sml);
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
            
            // Add a status bar.
            getContentPane().add(_statusBar, BorderLayout.SOUTH);
        }
        super.setVisible(flag);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The most recent directory used in a file dialog. */
    protected static File _directory = null;

    /** File menu for this frame. */
    protected JMenu _fileMenu = new JMenu("File");

    /** Items in the file menu. */
    protected JMenuItem[] _fileMenuItems = {
        new JMenuItem("Open", KeyEvent.VK_O),
        new JMenu("New"),
        new JMenuItem("Save", KeyEvent.VK_S),
        new JMenuItem("SaveAs", KeyEvent.VK_A),
        new JMenuItem("Print", KeyEvent.VK_P),
        new JMenuItem("Close", KeyEvent.VK_C),
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
    protected StatusBar _statusBar = new StatusBar();

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
                "Copyright (c) 1997-2000, " +
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
     *  if the data associated with this window has been modified,
     *  as indicated by isModified(), then ask the user whether to
     *  save the data before closing.
     */
    protected void _close() {
        // NOTE: We use dispose() here rather than just hiding the
        // window.  This ensures that derived classes can react to
        // windowClosed events rather than overriding the
        // windowClosing behavior given here.
        if (isModified()) {
            if (_queryForSave()) {
                dispose();
            }
        } else {
            // Window is not modified, so just dispose.
            dispose();
        }
    }

    /** Display the same information given by _about().
     *  Derived classes should override this to give information
     *  about the particular window and its role.
     */
    protected void _help() {
        _about();
    }

    /** Read the specified URL.
     *  @param url The URL to read.
     *  @exception Exception If the URL cannot be read.
     */
    protected abstract void _read(URL url) throws Exception;

    /** Open a file dialog to identify a file to be opened, and then call
     *  _read() to open the file.x
     */
    protected void _open() {
        JFileChooser fileDialog = new JFileChooser();
        fileDialog.setDialogTitle("Select a model file");

        if (_directory != null) {
            fileDialog.setCurrentDirectory(_directory);
        } else {
            // The default on Windows is to open at user.home, which is
            // typically an absurd directory inside the O/S installation.
            // So we use the current directory instead.
            // FIXME: Could this throw a security exception in an applet?
            String cwd = System.getProperty("user.dir");
            if (cwd != null) {
                fileDialog.setCurrentDirectory(new File(cwd));
            }
        }
        int returnVal = fileDialog.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
	    _directory = fileDialog.getCurrentDirectory();
            try {
                File file = fileDialog.getSelectedFile().getCanonicalFile();
                _read(file.toURL());
            } catch (Exception ex) {
                report("Error reading input", ex);
            }
        }
    }

    /** Print the contents.  If this frame implements either the
     *  Printable or Pageable then those interfaces are used to print
     *  it.
     */
    protected void _print() {
	PrinterJob job = PrinterJob.getPrinterJob();
	if(this instanceof Pageable) {
	    job.setPageable((Pageable)this);
	} else if(this instanceof Printable) {
	    PageFormat format = job.pageDialog(job.defaultPage());
	    job.setPrintable((Printable)this, format);
	} else {
	    // can't print it.
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

    /** Save the model to the current file, determined by the
     *  and _file protected variable.  This calls _writeFile().
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
        fileDialog.setDialogTitle("Save as...");
        if (_directory != null) {
            fileDialog.setCurrentDirectory(_directory);
        } else {
            // The default on Windows is to open at user.home, which is
            // typically an absurd directory inside the O/S installation.
            // So we use the current directory instead.
            // FIXME: This will probably fail with a security exception in
            // applets.
            String cwd = System.getProperty("user.dir");
            if (cwd != null) {
                fileDialog.setCurrentDirectory(new File(cwd));
            }
        }
        int returnVal = fileDialog.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            _file = fileDialog.getSelectedFile();
	    
            setTitle(_file.getName());
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
    ////                         private variables                 ////

    // The input file.
    private File _file = null;

    // Indicator that the data represented in the window has been modified.
    private boolean _modified = false;

    // Indicator that the menu has been populated.
    private boolean _menuPopulated = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Open a dialog to prompt the user to save the data.
    // Return false if the user clicks "cancel", and otherwise return true.
    private boolean _queryForSave() {
        Object[] options = {"Save", "Discard changes", "Cancel"};

        String query = "Save changes?";
        if (_file != null) {
            query = "Save changes to " + _file.getName() + "?";
        }
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
        } else if(selected == 1) {
            return true;
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Listener for file menu commands. */
    class FileMenuListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JMenuItem target = (JMenuItem)e.getSource();
            String actionCommand = target.getActionCommand();
            if (actionCommand.equals("Open")) _open();
            else if (actionCommand.equals("Save")) _save();
            else if (actionCommand.equals("SaveAs")) _saveAs();
            else if (actionCommand.equals("Print")) _print();
            else if (actionCommand.equals("Close")) _close();

            // NOTE: The following should not be needed, but jdk1.3beta
            // appears to have a bug in swing where repainting doesn't
            // properly occur.
            repaint();
        }
    }

    /** Listener for help menu commands. */
    class HelpMenuListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JMenuItem target = (JMenuItem)e.getSource();
            String actionCommand = target.getActionCommand();
            if (actionCommand.equals("About")) _about();
            else if (actionCommand.equals("Help")) _help();

            // NOTE: The following should not be needed, but there jdk1.3beta
            // appears to have a bug in swing where repainting doesn't
            // properly occur.
            repaint();
        }
    }
}
