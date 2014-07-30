/* Top-level window containing a plotter.

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
package ptolemy.actor.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import ptolemy.actor.injection.PortablePlaceable;
import ptolemy.data.expr.StringParameter;
import ptolemy.gui.ExtensionFilenameFilter;
import ptolemy.gui.ImageExportable;
import ptolemy.gui.JFileChooserBugFix;
import ptolemy.gui.Top;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.plot.Plot;
import ptolemy.plot.PlotBox;
import ptolemy.plot.PlotFormatter;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
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
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Yellow (cxh)
 */
@SuppressWarnings("serial")
public class PlotTableauFrame extends TableauFrame implements Printable,
ImageExportable {
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
        this(tableau, plotBox, (Placeable) null);
    }

    /** Construct a plot frame with the specified title and by default
     *  contains an instance of Plot. After constructing this, it is necessary
     *  to call setVisible(true) to make the plot appear.
     *  @param tableau The tableau where the window is placed.
     *  @param plotBox the plot object to put in the frame, or null to create
     *   an instance of Plot.
     *  @param placeable The associated plot actor, or null if none.
     */
    public PlotTableauFrame(Tableau tableau, PlotBox plotBox,
            Placeable placeable) {
        super(tableau, null, placeable);
        plot = plotBox;

        // We don't define a file name filter here because we are
        // only supporting PlotML files.  .plt files are not supported
        // for opening here.

        // Background color is a light grey.
        plot.setBackground(new Color(0xe5e5e5));
        getContentPane().add(plot, BorderLayout.CENTER);
        _initialSaveAsFileName = "plot.plt";
    }

    /** Construct a plot frame with the specified title and by default
     *  contains an instance of Plot. After constructing this, it is necessary
     *  to call setVisible(true) to make the plot appear.
     *  @param tableau The tableau where the window is placed.
     *  @param plotBox the plot object to put in the frame, or null to create
     *   an instance of Plot.
     *  @param placeable The associated plot actor, or null if none.
     */
    public PlotTableauFrame(Tableau tableau, PlotBox plotBox,
            PortablePlaceable placeable) {
        super(tableau, null, placeable);
        plot = plotBox;

        // We don't define a file name filter here because we are
        // only supporting PlotML files.  .plt files are not supported
        // for opening here.

        // Background color is a light grey.
        plot.setBackground(new Color(0xe5e5e5));
        getContentPane().add(plot, BorderLayout.CENTER);
        _initialSaveAsFileName = "plot.plt";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Print the plot to a printer,
     *  which is represented by the specified graphics object.
     *  @param graphics The context into which the page is drawn.
     *  @param format The size and orientation of the page being drawn.
     *  @param index The zero based index of the page to be drawn.
     *  @return PAGE_EXISTS if the page is rendered successfully, or
     *   NO_SUCH_PAGE if pageIndex specifies a non-existent page.
     *  @exception PrinterException If the print job is terminated.
     */
    @Override
    public int print(Graphics graphics, PageFormat format, int index)
            throws PrinterException {
        // Note that the Plot print menu does not directly call
        // this method, instead it calls _print().  This method is
        // included so that this class implement printable and the
        // print menu choice is enabled.
        return plot.print(graphics, format, index);
    }

    /** Create a sample plot.
     */
    public void samplePlot() {
        _file = null;
        _directory = null;
        plot.samplePlot();
    }

    /** Dispose of this frame.
     *
     *  <p>Override this dispose() method to unattach any listeners
     *  that may keep this model from getting garbage collected.  This
     *  method invokes the dispose() method of the superclass,
     *  {@link ptolemy.gui.Top}.</p>
     */
    @Override
    public void dispose() {
        if (_debugClosing) {
            System.out.println("TableauFrame.dispose() : " + this.getName());
        }
        super.dispose();
    }

    /** Write an image to the specified output stream in the specified format.
     *  Supported formats include at least "gif" and "png", standard image file formats.
     *  The image is a rendition of the current view of the model.
     *  @param stream The output stream to write to.
     *  @param format The image format to generate.
     *  @exception IOException If writing to the stream fails.
     *  @exception PrinterException  If the specified format is not supported.
     */
    @Override
    public void writeImage(OutputStream stream, String format)
            throws PrinterException, IOException {
        if (plot == null) {
            throw new IOException("No plot to write image from!");
        }
        plot.exportImage(stream, format);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The plot object held by this frame. */
    public final PlotBox plot;

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Directory that contains the input file. */
    protected File _directory = null;

    /** The export to PDF action. */
    protected Action _exportPDFAction;

    /** Edit menu for this frame. */
    protected JMenu _editMenu;

    /** The export to GIF action. */
    protected Action _exportGIFAction;

    /** The export to PNG action. */
    protected Action _exportPNGAction;

    /** The input file. */
    protected File _file = null;

    /** Special menu for this frame. */
    protected JMenu _specialMenu;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the menus that are used by this frame.
     */
    @Override
    protected void _addMenus() {
        super._addMenus();

        // Edit menu
        _editMenu = new JMenu("Edit");
        _editMenu.setMnemonic(KeyEvent.VK_E);
        _menubar.add(_editMenu);

        FormatAction formatAction = new FormatAction();
        // Avoid a dependency on diva/gui/GUIUtilities.java here, but
        // add some code duplication.
        JMenuItem item = _editMenu.add(formatAction);
        item.setText((String) formatAction.getValue(Action.NAME));
        item.setMnemonic((Integer) formatAction.getValue(Action.MNEMONIC_KEY));
        item.setToolTipText((String) formatAction.getValue("tooltip"));

        KeyStroke key = (KeyStroke) formatAction
                .getValue(Action.ACCELERATOR_KEY);
        item.setAccelerator(key);
        formatAction.putValue("menuItem", item);
        // End of duplicated code from diva GUIUtilities.

        // Special menu
        _specialMenu = new JMenu("Special");
        _specialMenu.setMnemonic(KeyEvent.VK_S);
        _menubar.add(_specialMenu);

        JMenuItem[] specialMenuItems = { new JMenuItem("Clear", KeyEvent.VK_C),
                new JMenuItem("Export", KeyEvent.VK_E),
                new JMenuItem("Fill", KeyEvent.VK_F),
                new JMenuItem("Reset axes", KeyEvent.VK_R),
                new JMenuItem("Sample plot", KeyEvent.VK_S), };
        SpecialMenuListener sml = new SpecialMenuListener();

        // Set the action command and listener for each menu item.
        for (JMenuItem specialMenuItem : specialMenuItems) {
            specialMenuItem.setActionCommand(specialMenuItem.getText());
            specialMenuItem.addActionListener(sml);
            _specialMenu.add(specialMenuItem);
        }
    }

    /** Clear the current plot.  This class checks to see whether
     *  the contents have been modified, and if so, then prompts the user
     *  to save them.  A return value of false
     *  indicates that the user has canceled the action.
     *  @return False if the user cancels the clear.
     */
    @Override
    protected boolean _clear() {
        boolean result = super._clear();

        // The false argument prevents clearing decorations.
        plot.clear(false);
        return result;
    }

    /** Create the items in the File menu's Export section
     *  This method adds a menu items to export images of the plot
     *  in GIF, PNG, and possibly PDF.
     *  @return The items in the File menu.
     */
    @Override
    protected JMenuItem[] _createFileMenuItems() {
        JMenuItem[] fileMenuItems = super._createFileMenuItems();

        JMenu exportMenu = (JMenu) fileMenuItems[_EXPORT_MENU_INDEX];
        exportMenu.setEnabled(true);

        try {
            // Get the "export PDF" action classname from the configuration.
            // This may or many not be included because it depends on GPL'd code,
            // and hence cannot be included included in any pure BSD distribution.
            // NOTE: Cannot use getConfiguration() because the configuration is
            // not set when this method is called. Hence, we assume that there
            // is only one configuration, or that if there are multiple configurations
            // in this execution, that the first one will determine whether PDF
            // export is provided.
            Configuration configuration = (Configuration) Configuration
                    .configurations().get(0);
            // NOTE: Configuration should not be null, but just in case:
            if (configuration != null) {
                // Deal with the PDF Action first.
                StringParameter exportPDFActionClassNameParameter = (StringParameter) configuration
                        .getAttribute("_exportPDFActionClassName",
                                StringParameter.class);

                if (exportPDFActionClassNameParameter != null) {
                    if (_exportPDFAction == null) {
                        String exportPDFActionClassName = exportPDFActionClassNameParameter
                                .stringValue();
                        try {
                            Class exportPDFActionClass = Class
                                    .forName(exportPDFActionClassName);
                            Constructor exportPDFActionConstructor = exportPDFActionClass
                                    .getDeclaredConstructor(Top.class);
                            _exportPDFAction = (AbstractAction) exportPDFActionConstructor
                                    .newInstance(this);
                        } catch (Throwable throwable) {
                            throw new InternalErrorException(
                                    null,
                                    throwable,
                                    "Failed to construct export PDF class \""
                                            + exportPDFActionClassName
                                            + "\", which was read from the configuration.");
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // We do not want to abort at this point because the worst
            // case is that we will have no Export PDF in the menu.
            // That is better than preventing the user from opening a model.
            System.err
            .println("Warning: Tried to create Export PDF menu item, but failed: "
                    + ex);
        }

        // Uncomment the next block to have Export PDF *ALWAYS* enabled.
        // We don't want it always enabled because ptiny, the applets and
        // Web Start should not included this AGPL'd piece of software

        // NOTE: Comment out the entire block with lines that begin with //
        // so that the test in adm notices that the block is commented out.

        //         if (_exportPDFAction == null) {
        //             //String exportPDFActionClassName = exportPDFActionClassNameParameter.stringValue();
        //             String exportPDFActionClassName = "ptolemy.vergil.basic.export.itextpdf.ExportPDFAction";
        //             try {
        //                 Class exportPDFActionClass = Class
        //                         .forName(exportPDFActionClassName);
        //                 Constructor exportPDFActionConstructor = exportPDFActionClass
        //                         .getDeclaredConstructor(Top.class);
        //                 _exportPDFAction = (AbstractAction) exportPDFActionConstructor
        //                         .newInstance(this);
        //             } catch (Throwable throwable) {
        //                 new InternalErrorException(null, throwable,
        //                         "Failed to construct export PDF class \""
        //                                 + exportPDFActionClassName
        //                                 + "\", which was read from the configuration.");
        //             }
        //         }
        // End of block to uncomment.

        if (_exportPDFAction != null) {
            // Insert the Export PDF item.
            JMenuItem exportItem = new JMenuItem(_exportPDFAction);
            exportMenu.add(exportItem);
        }

        // Next do the export GIF action.
        if (_exportGIFAction == null) {
            _exportGIFAction = new ExportImageAction("GIF");
        }
        JMenuItem exportItem = new JMenuItem(_exportGIFAction);
        exportMenu.add(exportItem);

        // Next do the export PNG action.
        if (_exportPNGAction == null) {
            _exportPNGAction = new ExportImageAction("PNG");
        }
        exportItem = new JMenuItem(_exportPNGAction);
        exportMenu.add(exportItem);

        return fileMenuItems;
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
                JOptionPane.showMessageDialog(this, "Error exporting plot to '"
                        + file + "': " + ex, "Ptolemy II Error",
                        JOptionPane.WARNING_MESSAGE);
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
    @Override
    protected void _help() {
        JOptionPane.showMessageDialog(this,
                "PlotTableauFrame is a plot in a top-level window.\n"
                        + "  File formats understood: Ptplot ASCII.\n"
                        + "  Left mouse button: Zooming.",
                        "About Ptolemy Plot", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Write the plot to the specified file in PlotML syntax.
     *  @param file The file to which to write.
     *  @exception IOException If the write fails.
     */
    @Override
    protected void _writeFile(File file) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            plot.write(out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    //// ExportImageAction

    /** Export an image of a plot. */
    public class ExportImageAction extends AbstractAction {

        /** Create a new action to export an image.
         *  @param formatName The name of the format, currently PNG and
         *  GIF are supported.
         */
        public ExportImageAction(String formatName) {
            super("Export " + formatName);
            _formatName = formatName.toLowerCase(Locale.getDefault());
            putValue("tooltip", "Export " + formatName + " image to a file.");
            // putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_G));
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                   ////

        /** Export an image.
         *  @param e The ActionEvent that invoked this action.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooserBugFix jFileChooserBugFix = new JFileChooserBugFix();
            Color background = null;
            try {
                background = jFileChooserBugFix.saveBackground();

                JFileChooser fileDialog = new JFileChooser();
                fileDialog.setDialogTitle("Specify a file to write to.");
                LinkedList extensions = new LinkedList();
                extensions.add(_formatName);
                fileDialog.addChoosableFileFilter(new ExtensionFilenameFilter(
                        extensions));

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

                int returnVal = fileDialog.showDialog(
                        PlotTableauFrame.this,
                        "Export "
                                + _formatName.toUpperCase(Locale.getDefault()));

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    _directory = fileDialog.getCurrentDirectory();
                    File file = fileDialog.getSelectedFile().getCanonicalFile();

                    if (file.getName().indexOf(".") == -1) {
                        // If the user has not given the file an extension, add it
                        file = new File(file.getAbsolutePath() + "."
                                + _formatName);
                    }
                    if (file.exists()) {
                        if (!MessageHandler.yesNoQuestion("Overwrite "
                                + file.getName() + "?")) {
                            return;
                        }
                    }
                    OutputStream out = null;
                    try {
                        out = new FileOutputStream(file);
                        plot.exportImage(out, _formatName);
                    } finally {
                        if (out != null) {
                            out.close();
                        }
                    }

                    // Open the PNG file.
                    // FIXME: We don't do the right thing with PNG files.
                    // It just opens in a text editor.
                    // _read(file.toURI().toURL());
                    MessageHandler.message("Image file exported to "
                            + file.getName());
                }
            } catch (Exception ex) {
                MessageHandler.error(
                        "Export to "
                                + _formatName.toUpperCase(Locale.getDefault())
                                + " failed", ex);
            } finally {
                jFileChooserBugFix.restoreBackground(background);
            }
        }

        private String _formatName;
    }

    /** Format the plot. */
    private class FormatAction extends AbstractAction {
        /** Create a new action to format the plot. */
        public FormatAction() {
            super("Format");
            putValue("tooltip", "Open a dialog to format the plot.");
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_F));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                _editFormat();
            } catch (Exception exception) {
                // If we do not catch exceptions here, then they
                // disappear to stdout, which is bad if we launched
                // where there is no stdout visible.
                JOptionPane.showMessageDialog(null, "Format Exception:\n"
                        + exception.toString(), "Ptolemy Plot Error",
                        JOptionPane.WARNING_MESSAGE);
            }

            // NOTE: The following should not be needed, but there jdk1.3beta
            // appears to have a bug in swing where repainting doesn't
            // properly occur.
            repaint();
        }
    }

    class SpecialMenuListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JMenuItem target = (JMenuItem) e.getSource();
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
                JOptionPane.showMessageDialog(null, "Special Menu Exception:\n"
                        + exception.toString(), "Ptolemy Plot Error",
                        JOptionPane.WARNING_MESSAGE);
            }

            // NOTE: The following should not be needed, but there jdk1.3beta
            // appears to have a bug in swing where repainting doesn't
            // properly occur.
            repaint();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Display only .eps files. */
    static class EPSFileFilter extends FileFilter {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        /** Accept only .eps files.
         *  @param fileOrDirectory The file to be checked.
         *  @return true if the file is a directory, a .eps file
         */
        @Override
        public boolean accept(File fileOrDirectory) {
            if (fileOrDirectory.isDirectory()) {
                return true;
            }

            String fileOrDirectoryName = fileOrDirectory.getName();
            int dotIndex = fileOrDirectoryName.lastIndexOf('.');

            if (dotIndex == -1) {
                return false;
            }

            String extension = fileOrDirectoryName.substring(dotIndex);

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
        @Override
        public String getDescription() {
            return "Encapsulated PostScript (.eps) files";
        }
    }
}
