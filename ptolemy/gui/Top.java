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
// FIXME: Trim this.
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
import java.awt.Container;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.PrintJob;
import java.awt.event.*;
import java.awt.print.PrinterJob;
import java.io.*;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;

//////////////////////////////////////////////////////////////////////////
//// Top
/**
This is a top-level window with a menubar and status bar.
Derived classes should add components to the content pane using a
line like:
<pre>
    getContentPane().add(component, BorderLayout.CENTER);
</pre>

@author Edward A. Lee
@version $Id$
*/
public abstract class Top extends JFrame {

    /** Construct an empty top-level frame. The window is centered on the
     *  screen, and is separately iconified and deiconified by the window
     *  manager. After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     */
    public Top() {
        super();

        getContentPane().setLayout(new BorderLayout());

        // Set up the menus.
        _fileMenu.setMnemonic(KeyEvent.VK_F);
        _helpMenu.setMnemonic(KeyEvent.VK_H);

        // File menu
        JMenuItem[] fileMenuItems = {
            new JMenuItem("Open", KeyEvent.VK_O),
            new JMenuItem("Save", KeyEvent.VK_S),
            new JMenuItem("SaveAs", KeyEvent.VK_A),
            new JMenuItem("Print", KeyEvent.VK_P),
            new JMenuItem("Close", KeyEvent.VK_C),
        };
        // Open button = ctrl-o.
        fileMenuItems[0].setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK));

        // Save button = ctrl-s.
        fileMenuItems[1].setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));

        // Print button = ctrl-p.
        fileMenuItems[3].setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK));

        // Close button = ctrl-w.
        fileMenuItems[4].setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.CTRL_MASK));

        FileMenuListener fml = new FileMenuListener();
        // Set the action command and listener for each menu item.
        for(int i = 0; i < fileMenuItems.length; i++) {
            fileMenuItems[i].setActionCommand(fileMenuItems[i].getText());
            fileMenuItems[i].addActionListener(fml);
            _fileMenu.add(fileMenuItems[i]);
        }
        _menubar.add(_fileMenu);

        // Help menu
        JMenuItem[] helpMenuItems = {
            new JMenuItem("About", KeyEvent.VK_A),
            new JMenuItem("Help", KeyEvent.VK_H),
        };
        HelpMenuListener sml = new HelpMenuListener();
        // Set the action command and listener for each menu item.
        for(int i = 0; i < helpMenuItems.length; i++) {
            helpMenuItems[i].setActionCommand(
                    helpMenuItems[i].getText());
            helpMenuItems[i].addActionListener(sml);
            _helpMenu.add(helpMenuItems[i]);
        }
        _menubar.add(_helpMenu);

        setJMenuBar(_menubar);

        // Add a status bar.
        getContentPane().add(_statusBar, BorderLayout.SOUTH);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the key used to identify this window.
     *  @return The key identifying the model.
     */
    public Object getKey() {
        return _key;
    }

    /** Set the key used to identify this window, and display a string
     *  representation of this key in the titlebar.
     *  @param key The key identifying the model.
     */
    public void setKey(Object key) {
        _key = key;
        setTitle(key.toString());
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

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** @serial Directory that contains the input file. */
    protected File _directory = null;

    /** @serial The input file. */
    protected File _file = null;

    /** @serial File menu for this frame. */
    protected JMenu _fileMenu = new JMenu("File");

    /** @serial Help menu for this frame. */
    protected JMenu _helpMenu = new JMenu("Help");

    /** @serial Menubar for this frame. */
    protected JMenuBar _menubar = new JMenuBar();

    /** The status bar. */
    protected StatusBar _statusBar = new StatusBar();

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Open a dialog with basic information about this window.
     */
    protected void _about() {
        JOptionPane.showMessageDialog(this,
                "Ptolemy II.\n" +
                "By: Claudius Ptolemeus, ptolemy@eecs.berkeley.edu\n" +
                "For more information, see\n" +
                "http://ptolemy.eecs.berkeley.edu/ptolemyII\n\n" +
                "Copyright (c) 1997-2000, " +
                "The Regents of the University of California.",
                "About Ptolemy II", JOptionPane.INFORMATION_MESSAGE);
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
        // FIXME: Check to see whether the data has changed.
        return true;
    }

    /** Close the window.
     */
    protected void _close() {
        // FIXME: Check to see whether the data has changed.
        dispose();
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
     *  @exception IOException If the URL cannot be read.
     */
    protected abstract void _read(URL url) throws IOException;

    /** Open a file dialog to identify a file to be opened, and then call
     *  _read() to open the file.
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
            File file = fileDialog.getSelectedFile();
            try {
                _read(file.toURL());
            } catch (IOException ex) {
                report("Error reading input", ex);
            }
        }
    }

    /** Print the contents.
     */
    protected void _print() {
        PrinterJob job = PrinterJob.getPrinterJob();
        if (job.printDialog()) {
            try {
                job.print();
            } catch (Exception ex) {
                report("Printing failed", ex);
            }
        }
    }

    /** Save the model to the current file, determined by the
     *  and _file protected variable.  This calls _writeFile().
     */
    protected void _save() {
        if (_file != null) {
            try {
                _writeFile(_file);
            } catch (IOException ex) {
                report("Error writing file", ex);
            }
        } else {
            _saveAs();
        }
    }

    /** Query the user for a filename and save the model to that file.
     */
    protected void _saveAs() {
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
            _save();
        }
    }

    /** Write the model to the specified file.
     *  @param file The file to write to.
     *  @exception IOException If the write fails.
     */
    protected abstract void _writeFile(File file) throws IOException;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The key used to identify the model.
    private Object _key;

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

            // NOTE: The following should not be needed, but there jdk1.3beta
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
            if (actionCommand.equals("About")) {
                _about();
            } else if (actionCommand.equals("Help")) {
                _help();
            }

            // NOTE: The following should not be needed, but there jdk1.3beta
            // appears to have a bug in swing where repainting doesn't
            // properly occur.
            repaint();
        }
    }
}
