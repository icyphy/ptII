/* Top-level window containing a plotter.

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
@ProposedRating Yellow (cxh@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import diva.gui.GUIUtilities;

import ptolemy.plot.Plot;
import ptolemy.plot.PlotBox;
import ptolemy.plot.PlotFormatter;
import ptolemy.util.StringUtilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

//////////////////////////////////////////////////////////////////////////
//// PlotTableauFrame
/**

PlotTableauFrame is a version of PlotFrame in the plot package that
works more closely with the Ptolemy actor.gui infrastructure.
In particular, the File menu commands will open Ptolemy models
and HTML files, not just PlotML files. It contains an instance
of PlotBox. If not specified in the constructor, the default
is to contain a Plot object, where Plot extends PlotBox. This
field is set once in the constructor and immutable afterwards.

@see Plot
@see PlotBox
@author Edward A. Lee
@version $Id$
@since Ptolemy II 2.1
*/
public class PlotTableauFrame extends TableauFrame {

    /** Construct a plot frame with a default title and by default contains
     *  an instance of Plot. After constructing this, it is necessary
     *  to call setVisible(true) to make the plot appear.
     */
    public PlotTableauFrame() {
        this(null);
    }

    /** Construct a plot frame in the corresponding Tableau with the
     *  specified instance of PlotBox.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the plot appear.
     *  @param tableau The tableau where the window is placed
     */
    public PlotTableauFrame(Tableau tableau) {
        this(tableau, new Plot());
    }

    /** Construct a plot frame with the specified title and by default
     *  contains an instance of Plot. After constructing this, it is necessary
     *  to call setVisible(true) to make the plot appear.
     *  @param tableau The tableau where the window is placed.
     *  @param plotBox the plot object to put in the frame, or null to create
     *   an instance of Plot.
     */
    public PlotTableauFrame(Tableau tableau, PlotBox plotBox) {
        super(tableau, null);
        plot = plotBox;

        // Create a file filter that accepts .xml and .moml files.
        LinkedList extensions = new LinkedList();
        extensions.add("plt");
        _fileFilter = new ExtensionFileFilter(extensions);

        // Background color is a light grey.
        plot.setBackground(new Color(0xe5e5e5));
        getContentPane().add(plot, BorderLayout.CENTER);
        _initialSaveAsFileName = "plot.plt";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a sample plot.
     */
    public void samplePlot() {
        _file = null;
        _directory = null;
        plot.samplePlot();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The plot object held by this frame. */
    public final PlotBox plot;

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Edit menu for this frame. */
    protected JMenu _editMenu;

    /** Special menu for this frame. */
    protected JMenu _specialMenu;

    /** Directory that contains the input file. */
    protected File _directory = null;

    /** The input file. */
    protected File _file = null;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the menus that are used by this frame.
     */
    protected void _addMenus() {
        super._addMenus();

        // Edit menu
        _editMenu = new JMenu("Edit");
        _editMenu.setMnemonic(KeyEvent.VK_E);
        _menubar.add(_editMenu);

        FormatAction formatAction = new FormatAction();
        GUIUtilities.addMenuItem(_editMenu, formatAction);

        // Special menu
        _specialMenu = new JMenu("Special");
        _specialMenu.setMnemonic(KeyEvent.VK_S);
        _menubar.add(_specialMenu);

        JMenuItem[] specialMenuItems = {
            new JMenuItem("Clear", KeyEvent.VK_C),
            new JMenuItem("Export", KeyEvent.VK_E),
            new JMenuItem("Fill", KeyEvent.VK_F),
            new JMenuItem("Reset axes", KeyEvent.VK_R),
            new JMenuItem("Sample plot", KeyEvent.VK_S),
        };
        SpecialMenuListener sml = new SpecialMenuListener();
        // Set the action command and listener for each menu item.
        for (int i = 0; i < specialMenuItems.length; i++) {
            specialMenuItems[i].setActionCommand(
                    specialMenuItems[i].getText());
            specialMenuItems[i].addActionListener(sml);
            _specialMenu.add(specialMenuItems[i]);
        }
    }

    /** Clear the current plot.  This class checks to see whether
     *  the contents have been modified, and if so, then prompts the user
     *  to save them.  A return value of false
     *  indicates that the user has canceled the action.
     *  @return False if the user cancels the clear.
     */
    protected boolean _clear() {
        boolean result = super._clear();
        // The false argument prevents clearing decorations.
        plot.clear(false);
        return result;
    }

    /** Interactively edit the file format in a modal dialog.
     */
    protected void _editFormat() {
        PlotFormatter formatter = new PlotFormatter(plot);
        formatter.openModal();
    }

    /** Query the user for a filename and export the plot to that file.
     *  Currently, the only supported format is EPS.
     */
    protected void _export() {
        JFileChooser fileDialog = new JFileChooser();
        fileDialog.addChoosableFileFilter(new EPSFileFilter());
        fileDialog.setDialogTitle("Export EPS to...");
        if (_directory != null) {
            fileDialog.setCurrentDirectory(_directory);
        } else {
            // The default on Windows is to open at user.home, which is
            // typically an absurd directory inside the O/S installation.
            // So we use the current directory instead.
            String cwd = StringUtilities.getProperty("user.dir");
            if (cwd != null) {
                fileDialog.setCurrentDirectory(new File(cwd));
            }
        }

        fileDialog.setSelectedFile(new File(fileDialog.getCurrentDirectory(),
                "plot.eps"));

        int returnVal = fileDialog.showDialog(this, "Export");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileDialog.getSelectedFile();
            FileOutputStream fout = null;
            try {
                fout = new FileOutputStream(file);
                plot.export(fout);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error exporting plot to '" + file + "': " + ex,
                        "Ptolemy II Error", JOptionPane.WARNING_MESSAGE);
            } finally {
                if (fout != null) {
                    try {
                        fout.close();
                    } catch (Throwable throwable) {
                        System.out.println("Ignoring failure to close stream "
                                + "on " + file);
                        throwable.printStackTrace();
                    }
                }
            }
        }
    }

    /** Display more detailed information than given by _about().
     */
    protected void _help() {
        JOptionPane.showMessageDialog(this,
                "PlotTableauFrame is a plot in a top-level window.\n" +
                "  File formats understood: Ptplot ASCII.\n" +
                "  Left mouse button: Zooming.",
                "About Ptolemy Plot", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Print the plot.
     */
    protected void _print() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(plot);
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

    /** Write the plot to the specified file in PlotML syntax
     *  @param file The file to write to.
     *  @exception IOException If the write fails.
     */
    protected void _writeFile(File file) throws IOException {
        FileOutputStream fout = new FileOutputStream(file);
        plot.write(fout);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Action to format the plot. */
    private class FormatAction extends AbstractAction {

        /** Create a new action to format the plot. */
        public FormatAction() {
            super("Format");
            putValue("tooltip",
                    "Open a dialog to format the plot.");
            putValue(GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_F));
        }

        public void actionPerformed(ActionEvent e) {
            try {
                _editFormat();
            } catch (Exception exception) {
                // If we do not catch exceptions here, then they
                // disappear to stdout, which is bad if we launched
                // where there is no stdout visible.
                JOptionPane.showMessageDialog(null,
                        "Format Exception:\n" + exception.toString(),
                        "Ptolemy Plot Error", JOptionPane.WARNING_MESSAGE);
            }
            // NOTE: The following should not be needed, but there jdk1.3beta
            // appears to have a bug in swing where repainting doesn't
            // properly occur.
            repaint();
        }
    }

    class SpecialMenuListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JMenuItem target = (JMenuItem)e.getSource();
            String actionCommand = target.getActionCommand();
            try {
                if (actionCommand.equals("Fill")) {
                    plot.fillPlot();
                } else if (actionCommand.equals("Reset axes")) {
                    plot.resetAxes();
                } else if (actionCommand.equals("Clear")) {
                    plot.clear(false);
                    plot.repaint();
                } else if (actionCommand.equals("Export")) {
                    _export();
                } else if (actionCommand.equals("Sample plot")) {
                    plot.clear(true);
                    samplePlot();
                }
            } catch (Exception exception) {
                // If we do not catch exceptions here, then they
                // disappear to stdout, which is bad if we launched
                // where there is no stdout visible.
                JOptionPane.showMessageDialog(null,
                        "Special Menu Exception:\n" + exception.toString(),
                        "Ptolemy Plot Error", JOptionPane.WARNING_MESSAGE);
            }
            // NOTE: The following should not be needed, but there jdk1.3beta
            // appears to have a bug in swing where repainting doesn't
            // properly occur.
            repaint();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Display only .eps files */
    class EPSFileFilter extends FileFilter {

        /** Accept only .eps files.
         *  @param file The file to be checked.
         *  @return true if the file is a directory, a .eps file
         */
        public boolean accept(File fileOrDirectory) {
            if (fileOrDirectory.isDirectory()) {
                return true;
            }

            String fileOrDirectoryName = fileOrDirectory.getName();
            int dotIndex = fileOrDirectoryName.lastIndexOf('.');
            if (dotIndex == -1) {
                return false;
            }
            String extension =
                fileOrDirectoryName
                .substring(dotIndex);

            if (extension != null) {
                if (extension.equalsIgnoreCase(".eps")) {
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        }

        /**  The description of this filter */
        public String getDescription() {
            return "Encapsulated PostScript (.eps) files";
        }
    }
}
