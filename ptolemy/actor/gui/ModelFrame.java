/* Top-level window containing a Ptolemy II model.

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

// FIXME: To do:
//  - Fix printing.
//  - Handle file changes (warn when discarding modified models).

package ptolemy.actor.gui;

// Ptolemy imports
import ptolemy.actor.CompositeActor;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Manager;
import ptolemy.gui.StatusBar;
import ptolemy.kernel.CompositeEntity;

// Java imports
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
//// ModelFrame
/**

ModelFrame is a top-level window containing a Ptolemy II model.
If contains a ModelPane, but adds a menu bar and a status bar for
message reporting.
<p>
An application that uses this class should set up the handling of
window-closing events.  Presumably, the application will exit when
all windows have been closed. This is done with code something like:
<pre>
    modelFrameInstance.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
            // Handle the event
        }
    });
</pre>

@author Edward A. Lee
@version $Id$
*/
public class ModelFrame extends JFrame implements ExecutionListener {

    /** Construct a frame to control the specified Ptolemy II model.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  A controlling application must be specified; this class
     *  delegates opening of new models to that application.
     *  @param model The model to put in this frame, or null if none.
     *  @param application The controlling application.
     */
    public ModelFrame(CompositeActor model, PtolemyApplication application) {
        super();
        _model = model;
        _application = application;

        // Create first with no model to avoid duplicating work when
        // we next call setModel().
        _pane = new ModelPane(null);
        setModel(model);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(_pane, BorderLayout.CENTER);

        // Make the go button the default.
        _pane.setDefaultButton();

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
        // FIXME: Need to do something with the progress bar in the status bar.
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Report that an execution error has occurred.  This method
     *  is called by the specified manager.
     *  @param manager The manager calling this method.
     *  @param ex The exception being reported.
     */
    public void executionError(Manager manager, Exception ex) {
        report(ex);
    }

    /** Report that execution of the model has finished.
     *  @param manager The manager calling this method.
     */
    public synchronized void executionFinished(Manager manager) {
        report("execution finished.");
    }

    /** Get the associated model.
     *  @return The associated model.
     */
    public CompositeActor getModel() {
        return _model;
    }

    /** Report that a manager state has changed.
     *  This is method is called by the specified manager.
     *  @param manager The manager calling this method.
     */
    public void managerStateChanged(Manager manager) {
        Manager.State newstate = manager.getState();
        if (newstate != _previousState) {
            report(manager.getState().getDescription());
            _previousState = newstate;
        }
    }

    /** Return the container into which to place placeable objects.
     *  @return A container for graphical displays.
     */
    public ModelPane modelPane() {
        return _pane;
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

    /** Report an exception.  This displays a message in a dialog and
     *  prints the stack trace to the standard error stream.
     *  @param message The message.
     *  @param ex The exception to report.
     */
    public void report(String message, Exception ex) {
	_statusBar.setMessage("Exception. " + message);
        if (message != null) {
            System.err.println(message);
        } else {
            System.err.println("Exception thrown.");
        }
        System.err.println(ex.getMessage());
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, message + "\n" + ex.toString(),
                "Ptolemy II error", JOptionPane.ERROR_MESSAGE);
    }

    /** Set background color.  This overrides the base class to set the
     *  background of contained ModelPane.
     *  @param background The background color.
     */
    public void setBackground(Color background) {
        super.setBackground(background);
        // This seems to be called in a base class constructor, before
        // this variables has been set.
        if (_pane != null) _pane.setBackground(background);
        if (_statusBar != null) _statusBar.setBackground(background);
    }

    /** Set the associated model.
     *  @param model The associated model.
     */
    public void setModel(CompositeActor model) {
        _model = model;
        if (model != null) {
            _pane.setModel(model);
            setTitle(model.getName());
            Manager manager = model.getManager();
            if (manager != null) {
                manager.addExecutionListener(this);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** @serial Menubar for this frame. */
    protected JMenuBar _menubar = new JMenuBar();

    /** @serial File menu for this frame. */
    protected JMenu _fileMenu = new JMenu("File");

    /** @serial Help menu for this frame. */
    protected JMenu _helpMenu = new JMenu("Help");

    /** @serial Directory that contains the input file. */
    protected File _directory = null;

    /** @serial The input file. */
    protected File _file = null;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Open a dialog with basic information about this window.
     */
    protected void _about() {
        JOptionPane.showMessageDialog(this,
                "This is a control panel for a Ptolemy II model.\n" +
                "By: Claudius Ptolemeus, ptolemy@eecs.berkeley.edu\n" +
                "Version 1.0, Build: $Id$\n\n"+
                "For more information, see\n" +
                "http://ptolemy.eecs.berkeley.edu/ptolemyII\n\n" +
                "Copyright (c) 1997-2000, " +
                "The Regents of the University of California.",
                "About Ptolemy II", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Close the window.
     */
    protected void _close() {
        dispose();
    }

    /** Display more detailed information than given by _about().
     */
    protected void _help() {
        String message = "Ptolemy II model.";
        if (_model != null) {
            String tip = Documentation.consolidate(_model);
            if (tip != null) {
                message = "Ptolemy II model:\n" + tip;
            }
        }
        JOptionPane.showMessageDialog(this, message,
                "About " + getTitle(), JOptionPane.INFORMATION_MESSAGE);
    }

    /** Open a new file.
     */
    protected void _open() {
        JFileChooser fileDialog = new JFileChooser();
        fileDialog.setDialogTitle("Select a model file");

        // Filter file names.
        // NOTE: This filter is not built in yet... add this when it is.
        // It is not worth the trouble to design our own filter.
        // ExtensionFileFilter filter = new ExtensionFileFilter();
        // filter.addExtension("xml");
        // filter.setDescription("Ptolemy files");
        // fileDialog.addChoosableFileFilter(filter);

        if (_directory != null) {
            fileDialog.setCurrentDirectory(_directory);
        } else {
            // The default on Windows is to open at user.home, which is
            // typically an absurd directory inside the O/S installation.
            // So we use the current directory instead.
            String cwd = System.getProperty("user.dir");
            if (cwd != null) {
                fileDialog.setCurrentDirectory(new File(cwd));
            }
        }
        int returnVal = fileDialog.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            _file = fileDialog.getSelectedFile();
            setTitle(_file.getName());
            _directory = fileDialog.getCurrentDirectory();
            try {
                _application._read(new URL("file", null,
                        _directory.getAbsolutePath()),
                        new FileInputStream(_file));
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(this,
                        "File not found:\n" + ex.toString(),
                        "Ptolemy II Error", JOptionPane.WARNING_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error reading input:\n" + ex.toString(),
                        "Ptolemy II Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /** Print the contents.
     */
    protected void _print() {
        PrinterJob job = PrinterJob.getPrinterJob();
        // FIXME: What classes implement Printable? This one doesn't...
        // job.setPrintable(_pane);
        if (job.printDialog()) {
            try {
                job.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Printing failed:\n" + ex.toString(),
                        "Print Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /** Save the model to the current file, determined by the
     *  and _file protected variable.
     */
    protected void _save() {
        if (_file != null) {
            try {
                java.io.FileWriter fout = new java.io.FileWriter(_file);
                _model.exportMoML(fout);
                fout.close();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error writing file:\n" + ex.toString(),
                        "Ptolemy II Error", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            _saveAs();
        }
    }

    /** Query the user for a filename and save the model to that file.
     */
    protected void _saveAs() {

        JFileChooser fileDialog = new JFileChooser();
        fileDialog.setDialogTitle("Save model as...");
        if (_directory != null) {
            fileDialog.setCurrentDirectory(_directory);
        } else {
            // The default on Windows is to open at user.home, which is
            // typically an absurd directory inside the O/S installation.
            // So we use the current directory instead.
            String cwd = System.getProperty("user.dir");
            if (cwd != null) {
                fileDialog.setCurrentDirectory(new File(cwd));
            }
        }
        // FIXME: Can't seem to suggest a file name:
        // fileDialog.setCurrentFile("model.xml");
        int returnVal = fileDialog.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            _file = fileDialog.getSelectedFile();
            setTitle(_file.getName());
            _directory = fileDialog.getCurrentDirectory();
            _save();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The controlling application. This class delegates opening new models
    // to that application.
    private PtolemyApplication _application;

    // The model that this window controls, if any.
    private CompositeActor _model;

    // The pane in which the model data is displayed.
    private ModelPane _pane;

    // The previous state of the manager, to avoid reporting it if it hasn't
    // changed.
    private Manager.State _previousState;

    // The status bar.
    private StatusBar _statusBar = new StatusBar();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

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
