/* Top-level window containing a plotter.

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
@ProposedRating Yellow (cxh@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.plot;

import ptolemy.gui.*;

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
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.PrintJob;
import java.awt.event.*;
import java.awt.print.PrinterJob;
import java.io.*;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;

// TO DO:
//   - Add a mechanism for combining two plots into one

//////////////////////////////////////////////////////////////////////////
//// PlotFrame
/**

PlotFrame is a versatile two-dimensional data plotter that runs as
part of an application, but in its own window. It can read files
compatible with the old Ptolemy plot file format (currently only ASCII).
It is extended with the capability to read PlotML files in PlotMLFrame.
An application can also interact directly with the contained Plot
object, which is visible as a public member, by invoking its methods.
<p>
An application that uses this class should set up the handling of
window-closing events.  Presumably, the application will exit when
all windows have been closed. This is done with code something like:
<pre>
    plotFrameInstance.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
            // Handle the event
        }
    });
</pre>
<p>
PlotFrame contains an instance of PlotBox. PlotBox is the base class for
classes with drawing capability, e.g. Plot, LogicAnalyzer. If not
specified in the constructor, the default is to contain a Plot object. This
field is set once in the constructor and immutable afterwards.

@see Plot
@see PlotBox
@author Christopher Hylands and Edward A. Lee
@version $Id$
*/
public class PlotFrame extends JFrame {

    /** Construct a plot frame with a default title and by default contains
     *  an instance of Plot. After constructing this, it is necessary
     *  to call setVisible(true) to make the plot appear.
     */
    public PlotFrame() {
        this("Ptolemy Plot Frame");
    }

    /** Construct a plot frame with the specified title and by default
     *  contains an instance of Plot. After constructing this, it is necessary
     *  to call setVisible(true) to make the plot appear.
     *  @param title The title to put on the window.
     */
    public PlotFrame(String title) {
        this(title, null);
    }

    /** Construct a plot frame with the specified title and the specified
     *  instance of PlotBox.  After constructing this, it is necessary
     *  to call setVisible(true) to make the plot appear.
     *  @param title The title to put on the window.
     *  @param plotArg the plot object to put in the frame, or null to create
     *   an instance of Plot.
     */
    public PlotFrame(String title, PlotBox plotArg) {
        super(title);

        // Background color is a light grey.
        setBackground(new Color(0xe5e5e5));

        if (plotArg == null) {
            plot = new Plot();
        } else {
            plot = plotArg;
        }

        _fileMenu.setMnemonic(KeyEvent.VK_F);
        _editMenu.setMnemonic(KeyEvent.VK_E);
        _specialMenu.setMnemonic(KeyEvent.VK_S);

        // File menu
        JMenuItem[] fileMenuItems = {
            new JMenuItem("Open", KeyEvent.VK_O),
            new JMenuItem("Save", KeyEvent.VK_S),
            new JMenuItem("SaveAs", KeyEvent.VK_A),
            new JMenuItem("Export", KeyEvent.VK_E),
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
        fileMenuItems[4].setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK));

        // Close button = ctrl-w.
        fileMenuItems[5].setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.CTRL_MASK));

        FileMenuListener fml = new FileMenuListener();
        // Set the action command and listener for each menu item.
        for(int i = 0; i < fileMenuItems.length; i++) {
            fileMenuItems[i].setActionCommand(fileMenuItems[i].getText());
            fileMenuItems[i].addActionListener(fml);
            _fileMenu.add(fileMenuItems[i]);
        }
        _menubar.add(_fileMenu);

        // Edit menu
        JMenuItem format = new JMenuItem("Format", KeyEvent.VK_F);
        FormatListener formatListener = new FormatListener();
        format.addActionListener(formatListener);
        _editMenu.add(format);
        _menubar.add(_editMenu);

        // Special menu
        JMenuItem[] specialMenuItems = {
            new JMenuItem("About", KeyEvent.VK_A),
            new JMenuItem("Help", KeyEvent.VK_H),
            new JMenuItem("Clear", KeyEvent.VK_C),
            new JMenuItem("Fill", KeyEvent.VK_F),
            new JMenuItem("Sample plot", KeyEvent.VK_S),
        };
        SpecialMenuListener sml = new SpecialMenuListener();
        // Set the action command and listener for each menu item.
        for(int i = 0; i < specialMenuItems.length; i++) {
            specialMenuItems[i].setActionCommand(
                    specialMenuItems[i].getText());
            specialMenuItems[i].addActionListener(sml);
            _specialMenu.add(specialMenuItems[i]);
        }
        _menubar.add(_specialMenu);

        setJMenuBar(_menubar);

        getContentPane().add(plot, BorderLayout.CENTER);
        // FIXME: This should not be hardwired in here.
        setSize(500, 300);

        // Center.
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = getSize();
        int x = (screenSize.width - frameSize.width) / 2;
        int y = (screenSize.height - frameSize.height) / 2;
        setLocation(x, y);
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

    /** @serial The plot object held by this frame. */
    // FIXME: uncomment final when we upgrade to jdk1.2
    public /*final*/ PlotBox plot;

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////


    /** @serial Menubar for this frame. */
    protected JMenuBar _menubar = new JMenuBar();

    /** @serial Edit menu for this frame. */
    protected JMenu _editMenu = new JMenu("Edit");

    /** @serial File menu for this frame. */
    protected JMenu _fileMenu = new JMenu("File");

    /** @serial Special menu for this frame. */
    protected JMenu _specialMenu = new JMenu("Special");

    /** @serial Directory that contains the input file. */
    protected File _directory = null;

    /** @serial The input file. */
    protected File _file = null;


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    protected void _about() {
        JOptionPane.showMessageDialog(this,
                "PlotFrame class\n" +
                "By: Edward A. Lee, eal@eecs.berkeley.edu " +
                "and Christopher Hylands, cxh@eecs.berkeley.edu\n" +
                "Version 3.1p1, Build: $Id$\n\n"+
                "For more information, see\n" +
                "http://ptolemy.eecs.berkeley.edu/java/ptplot\n\n" +
                "Copyright (c) 1997-2000, " +
                "The Regents of the University of California.",
                "About Ptolemy Plot", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Close the window.
     */
    protected void _close() {
        dispose();
    }

    /** Interactively edit the file format.
     */
    protected void _editFormat() {
        JPanel panel = new JPanel(new BorderLayout());
        final Query wideQuery = new Query();
        panel.add(wideQuery, BorderLayout.WEST);
        final Query narrowQuery = new Query();
        panel.add(narrowQuery, BorderLayout.EAST);

        // Populate the wide query.
        wideQuery.setTextWidth(20);
        String originalTitle = plot.getTitle();
        wideQuery.addLine("title", "Title", originalTitle);
        String originalXLabel = plot.getXLabel();
        wideQuery.addLine("xlabel", "X Label", originalXLabel);
        String originalYLabel = plot.getYLabel();
        wideQuery.addLine("ylabel", "Y Label", originalYLabel);
        double[] originalXRange = plot.getXRange();
        wideQuery.addLine("xrange", "X Range",
                "" + originalXRange[0] + ", " + originalXRange[1]);
        double[] originalYRange = plot.getYRange();
        wideQuery.addLine("yrange", "Y Range",
                "" + originalYRange[0] + ", " + originalYRange[1]);
        String[] marks = {"none", "points", "dots", "various"};
        String originalMarks = "none";
        if (plot instanceof Plot) {
            originalMarks = ((Plot)plot).getMarksStyle();
            wideQuery.addRadioButtons("marks", "Marks", marks, originalMarks);
        }
        Vector[] originalXTicks = plot.getXTicks();
        String originalXTicksSpec = "";
        if (originalXTicks != null) {
            StringBuffer buffer = new StringBuffer();
            Vector positions = originalXTicks[0];
            Vector labels = originalXTicks[1];
            for(int i = 0; i < labels.size(); i++) {
                if(buffer.length() > 0) {
                    buffer.append(", ");
                }
                buffer.append(labels.elementAt(i).toString());
                buffer.append(" ");
                buffer.append(positions.elementAt(i).toString());
            }
            originalXTicksSpec = buffer.toString();
        }
        wideQuery.addLine("xticks", "X Ticks", originalXTicksSpec);

        Vector[] originalYTicks = plot.getYTicks();
        String originalYTicksSpec = "";
        if (originalYTicks != null) {
            StringBuffer buffer = new StringBuffer();
            Vector positions = originalYTicks[0];
            Vector labels = originalYTicks[1];
            for(int i = 0; i < labels.size(); i++) {
                if(buffer.length() > 0) {
                    buffer.append(", ");
                }
                buffer.append(labels.elementAt(i).toString());
                buffer.append(" ");
                buffer.append(positions.elementAt(i).toString());
            }
            originalYTicksSpec = buffer.toString();
        }
        wideQuery.addLine("yticks", "Y Ticks", originalYTicksSpec);

        boolean originalGrid = plot.getGrid();
        narrowQuery.addCheckBox("grid", "Grid", originalGrid);
        boolean originalStems = false;
        boolean[][] originalConnected = null;
        if (plot instanceof Plot) {
            originalStems = ((Plot)plot).getImpulses();
            narrowQuery.addCheckBox("stems", "Stems", originalStems);
            originalConnected = _getConnected();
            narrowQuery.addCheckBox("connected", "Connect",
                    ((Plot)plot).getConnected());
        }
        boolean originalColor = plot.getColor();
        narrowQuery.addCheckBox("color", "Use Color", originalColor);

        // FIXME: setXLog() and setYLog() cause problems with
        // dropped data if they are toggled after data is read in.
        // This is because the log axis facility modifies the datasets
        // in addPlotPoint() in Plot.java.  When this is fixed
        // we can add the XLog and YLog facility to the Format menu
        //
        //boolean originalXLog = plot.getXLog();
        //narrowQuery.addCheckBox("xlog", "X Log", originalXLog);
        //if (originalXTicks != null) {
        //    narrowQuery.setBoolean("xlog", false);
        //    narrowQuery.setEnabled("xlog", false);
        //}
        //boolean originalYLog = plot.getYLog();
        //narrowQuery.addCheckBox("ylog", "Y Log", originalYLog);
        //if (originalYTicks != null) {
        //    narrowQuery.setBoolean("ylog", false);
        //    narrowQuery.setEnabled("ylog", false);
        //}

        // Attach listeners.
        wideQuery.addQueryListener(new QueryListener() {
            public void changed(String name) {
                if (name.equals("title")) {
                    plot.setTitle(wideQuery.stringValue("title"));
                } else if (name.equals("xlabel")) {
                    plot.setXLabel(wideQuery.stringValue("xlabel"));
                } else if (name.equals("ylabel")) {
                    plot.setYLabel(wideQuery.stringValue("ylabel"));
                } else if (name.equals("xrange")) {
                    plot.read("XRange: " + wideQuery.stringValue("xrange"));
                } else if (name.equals("xticks")) {
                    String spec = wideQuery.stringValue("xticks").trim();
                    plot.read("XTicks: " + spec);
                    // FIXME: log axis format temporarily disable, see above.
                    // if(spec.equals("")) {
                    //    narrowQuery.setEnabled("xlog", true);
                    // } else {
                    //    narrowQuery.setBoolean("xlog", false);
                    //    narrowQuery.setEnabled("xlog", false);
                    // }
                } else if (name.equals("yticks")) {
                    String spec = wideQuery.stringValue("yticks").trim();
                    plot.read("YTicks: " + spec);
                    // FIXME: log axis format temporarily disable, see above.
                    // if(spec.equals("")) {
                    //    narrowQuery.setEnabled("ylog", true);
                    // } else {
                    //    narrowQuery.setBoolean("ylog", false);
                    //    narrowQuery.setEnabled("ylog", false);
                    // }
                } else if (name.equals("yrange")) {
                    plot.read("YRange: " + wideQuery.stringValue("yrange"));
                } else if (name.equals("marks")) {
                    ((Plot)plot).setMarksStyle(wideQuery.stringValue("marks"));
                }
                plot.repaint();
            }
        });

        narrowQuery.addQueryListener(new QueryListener() {
            public void changed(String name) {
                if (name.equals("grid")) {
                    plot.setGrid(narrowQuery.booleanValue("grid"));
                } else if (name.equals("stems")) {
                    ((Plot)plot).setImpulses(narrowQuery.booleanValue("stems"));
                    plot.repaint();
                } else if (name.equals("color")) {
                    plot.setColor(narrowQuery.booleanValue("color"));
                // FIXME: log axis format temporarily disable, see above.
                // } else if (name.equals("xlog")) {
                //    plot.setXLog(narrowQuery.booleanValue("xlog"));
                // } else if (name.equals("ylog")) {
                //    plot.setYLog(narrowQuery.booleanValue("ylog"));
                } else if (name.equals("connected")) {
                    _setConnected(narrowQuery.booleanValue("connected"));
                }
                plot.repaint();
            }
        });

        // Open the dialog.
        String[] buttons = {"Apply", "Cancel"};
        ComponentDialog dialog =
            new ComponentDialog(this, "Set plot format", panel, buttons);

        if (dialog.buttonPressed().equals("Apply")) {
            // Apply current values.
            plot.setTitle(wideQuery.stringValue("title"));
            plot.setXLabel(wideQuery.stringValue("xlabel"));
            plot.setYLabel(wideQuery.stringValue("ylabel"));
            plot.read("XRange: " + wideQuery.stringValue("xrange"));
            plot.read("YRange: " + wideQuery.stringValue("yrange"));
            plot.setGrid(narrowQuery.booleanValue("grid"));
            plot.setColor(narrowQuery.booleanValue("color"));
            // FIXME: log axis format temporarily disable, see above.
            // plot.setXLog(narrowQuery.booleanValue("xlog"));
            // plot.setYLog(narrowQuery.booleanValue("ylog"));
            if (plot instanceof Plot) {
                Plot cplot = (Plot)plot;
                cplot.setMarksStyle(wideQuery.stringValue("marks"));
                cplot.setImpulses(narrowQuery.booleanValue("stems"));
                _setConnected(narrowQuery.booleanValue("connected"));
            }
            // FIXME: log axis format temporarily disable, see above.
            // String spec = wideQuery.stringValue("xticks").trim();
            // plot.read("XTicks: " + spec);
            // if(spec.equals("")) {
            //    narrowQuery.setEnabled("xlog", true);
            // } else {
            //    narrowQuery.setBoolean("xlog", false);
            //    narrowQuery.setEnabled("xlog", false);
            // }
            // spec = wideQuery.stringValue("yticks").trim();
            // plot.read("YTicks: " + spec);
            // if(spec.equals("")) {
            //    narrowQuery.setEnabled("ylog", true);
            // } else {
            //    narrowQuery.setBoolean("ylog", false);
            //    narrowQuery.setEnabled("ylog", false);
            // }
        } else {
            // Restore original values.
            plot.setTitle(originalTitle);
            plot.setXLabel(originalXLabel);
            plot.setYLabel(originalYLabel);
            plot.setXRange(originalXRange[0], originalXRange[1]);
            plot.setYRange(originalYRange[0], originalYRange[1]);
            plot.setGrid(originalGrid);
            plot.setColor(originalColor);
            // FIXME: log axis format temporarily disable, see above.
            // plot.setXLog(originalXLog);
            // plot.setYLog(originalYLog);
            if (plot instanceof Plot) {
                Plot cplot = (Plot)plot;
                cplot.setMarksStyle(originalMarks);
                cplot.setImpulses(originalStems);
                _restoreConnected(originalConnected);
            }
            // FIXME: log axis format temporarily disable, see above.
            // plot.read("XTicks: " + originalXTicksSpec);
            // if(originalXTicksSpec.equals("")) {
            //    narrowQuery.setEnabled("xlog", true);
            // } else {
            //   narrowQuery.setBoolean("xlog", false);
            //    narrowQuery.setEnabled("xlog", false);
            // }
            // plot.read("YTicks: " + originalYTicksSpec);
            // if(originalYTicksSpec.equals("")) {
            //    narrowQuery.setEnabled("ylog", true);
            // } else {
            //    narrowQuery.setBoolean("ylog", false);
            //    narrowQuery.setEnabled("ylog", false);
            // }
        }
        plot.repaint();
    }

    /** Query the user for a filename and export the plot to that file.
     *  Currently, the only supported format is EPS.
     */
    protected void _export() {
        JFileChooser fileDialog = new JFileChooser();
        fileDialog.setDialogTitle("Export EPS to...");
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
        // FIXME: Can't seem to suggest a filename... Following fails
        // compile:
        // fileDialog.setSelectedFile("plot.eps");
        int returnVal = fileDialog.showDialog(this, "Export");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileDialog.getSelectedFile();
            try {
                FileOutputStream fout = new FileOutputStream(file);
                plot.export(fout);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                "Error exporting plot: " + ex,
                "Ptolemy II Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /** Display more detailed information than given by _about().
     */
    protected void _help() {
        JOptionPane.showMessageDialog(this,
                "PlotFrame is a plot in a top-level window.\n" +
                "  File formats understood: Ptplot ASCII.\n" +
                "  Left mouse button: Zooming.",
                "About Ptolemy Plot", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Open a new file and plot its data.
     */
    protected void _open() {
        JFileChooser fileDialog = new JFileChooser();
        fileDialog.setDialogTitle("Select a plot file");

        // Filter file names.
        // NOTE: This filter is not built in yet... add this when it is.
        // It is not worth the trouble to design our own filter.
        // ExtensionFileFilter filter = new ExtensionFileFilter();
        // filter.addExtension("plt");
        // filter.addExtension("xml");
        // filter.setDescription("Plot files");
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
                plot.clear(true);
                _read(new URL("file", null, _directory.getAbsolutePath()),
                         new FileInputStream(_file));
                plot.repaint();
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(this,
                "File not found:\n" + ex.toString(),
                "Ptolemy Plot Error", JOptionPane.WARNING_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                "Error reading input:\n" + ex.toString(),
                "Ptolemy Plot Error", JOptionPane.WARNING_MESSAGE);
            }
        }
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

    /** Read the specified stream.  Derived classes may override this
     *  to support other file formats.
     *  @param base The base for relative file references, or null if
     *   there are not relative file references.
     *  @param in The input stream.
     *  @exception IOException If the stream cannot be read.
     */
    protected void _read(URL base, InputStream in) throws IOException {
        plot.read(in);
    }

    /** Save the plot to the current file, determined by the
     *  and _file protected variable.
     */
    protected void _save() {
        if (_file != null) {
            try {
                FileOutputStream fout = new FileOutputStream(_file);
                plot.write(fout);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                "Error writing file:\n" + ex.toString(),
                "Ptolemy Plot Error", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            _saveAs();
        }
    }

    /** Query the user for a filename and save the plot to that file.
     */
    protected void _saveAs() {

        JFileChooser fileDialog = new JFileChooser();
        fileDialog.setDialogTitle("Save plot as...");
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
        // fileDialog.setCurrentFile("plot.xml");
        int returnVal = fileDialog.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            _file = fileDialog.getSelectedFile();
            setTitle(_file.getName());
            _directory = fileDialog.getCurrentDirectory();
            _save();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Get the current connected state of all the point in the
    // plot.  NOTE: This method reaches into the protected members of
    // the Plot class, taking advantage of the fact that this class is
    // in the same package.
    private boolean[][] _getConnected() {
        Vector points = ((Plot)plot)._points;
        boolean[][] result = new boolean[points.size()][];
        for (int dataset = 0; dataset < points.size(); dataset++) {
            Vector pts = (Vector)points.elementAt(dataset);
            result[dataset] = new boolean[pts.size()];
            for (int i = 0; i < pts.size(); i++) {
                PlotPoint pt = (PlotPoint)pts.elementAt(i);
                result[dataset][i] = pt.connected;
            }
        }
        return result;
    }

    // Set the current connected state of all the point in the
    // plot.  NOTE: This method reaches into the protected members of
    // the Plot class, taking advantage of the fact that this class is
    // in the same package.
    private void _setConnected(boolean value) {
        Vector points = ((Plot)plot)._points;
        // Make sure the default matches.
        ((Plot)plot).setConnected(value);
        boolean[][] result = new boolean[points.size()][];
        for (int dataset = 0; dataset < points.size(); dataset++) {
            Vector pts = (Vector)points.elementAt(dataset);
            result[dataset] = new boolean[pts.size()];
            boolean first = true;
            for (int i = 0; i < pts.size(); i++) {
                PlotPoint pt = (PlotPoint)pts.elementAt(i);
                pt.connected = value && !first;
                first = false;
            }
        }
    }

    // Set the current connected state of all the point in the
    // plot.  NOTE: This method reaches into the protected members of
    // the plot class, taking advantage of the fact that this class is
    // in the same package.
    private void _restoreConnected(boolean[][] original) {
        Vector points = ((Plot)plot)._points;
        boolean[][] result = new boolean[points.size()][];
        for (int dataset = 0; dataset < points.size(); dataset++) {
            Vector pts = (Vector)points.elementAt(dataset);
            result[dataset] = new boolean[pts.size()];
            for (int i = 0; i < pts.size(); i++) {
                PlotPoint pt = (PlotPoint)pts.elementAt(i);
                pt.connected = original[dataset][i];
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    class FileMenuListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JMenuItem target = (JMenuItem)e.getSource();
            String actionCommand = target.getActionCommand();
            if (actionCommand.equals("Open")) _open();
            else if (actionCommand.equals("Save")) _save();
            else if (actionCommand.equals("SaveAs")) _saveAs();
            else if (actionCommand.equals("Export")) _export();
            else if (actionCommand.equals("Print")) _print();
            else if (actionCommand.equals("Close")) _close();

            // NOTE: The following should not be needed, but there jdk1.3beta
            // appears to have a bug in swing where repainting doesn't
            // properly occur.
            repaint();
        }
    }

    class FormatListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            _editFormat();

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
            if (actionCommand.equals("About")) {
                _about();
            } else if (actionCommand.equals("Help")) {
                _help();
            } else if (actionCommand.equals("Fill")) {
                plot.fillPlot();
            } else if (actionCommand.equals("Clear")) {
                plot.clear(false);
                plot.repaint();
            } else if (actionCommand.equals("Sample plot")) {
                plot.clear(true);
                samplePlot();
            }

            // NOTE: The following should not be needed, but there jdk1.3beta
            // appears to have a bug in swing where repainting doesn't
            // properly occur.
            repaint();
        }
    }
}
