/* Top-level window containing a plotter.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

import java.awt.*;
import javax.swing.JPanel;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;

// TO DO:
//   - Add a mechanism for combining two plots into one
//   - Convert to use swing, especially for the menu.

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
author Christopher Hylands and Edward A. Lee
@version $Id$
*/
public class PlotFrame extends Frame {

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

        if (plotArg == null) {
            plot = new Plot();
        } else {
            plot = plotArg;
        }

        // File menu
        MenuItem[] fileMenuItems = {
            // FIXME: These shortcuts are not right.
            new MenuItem("Open", new MenuShortcut(KeyEvent.VK_O)),
            new MenuItem("Save", new MenuShortcut(KeyEvent.VK_S)),
            new MenuItem("SaveAs", new MenuShortcut(KeyEvent.VK_A)),
            new MenuItem("Export", new MenuShortcut(KeyEvent.VK_E)),
            new MenuItem("Print", new MenuShortcut(KeyEvent.VK_P)),
            new MenuItem("Close", new MenuShortcut(KeyEvent.VK_W)),
        };
        FileMenuListener fml = new FileMenuListener();
        // Set the action command and listener for each menu item.
        for(int i = 0; i < fileMenuItems.length; i++) {
            fileMenuItems[i].setActionCommand(fileMenuItems[i].getLabel());
            fileMenuItems[i].addActionListener(fml);
            _fileMenu.add(fileMenuItems[i]);
        }
        _menubar.add(_fileMenu);

        // Edit menu
        MenuItem format = new MenuItem("Format");
        FormatListener formatListener = new FormatListener();
        format.addActionListener(formatListener);
        _editMenu.add(format);
        _menubar.add(_editMenu);

        // Special menu
        MenuItem[] specialMenuItems = {
            // FIXME: These shortcuts are not right.
            new MenuItem("About", null),
            new MenuItem("Help", new MenuShortcut(KeyEvent.VK_H)),
            new MenuItem("Clear", new MenuShortcut(KeyEvent.VK_R)),
            new MenuItem("Fill", new MenuShortcut(KeyEvent.VK_F)),
            new MenuItem("Sample plot", null),
        };
        SpecialMenuListener sml = new SpecialMenuListener();
        // Set the action command and listener for each menu item.
        for(int i = 0; i < specialMenuItems.length; i++) {
            specialMenuItems[i].setActionCommand(
                    specialMenuItems[i].getLabel());
            specialMenuItems[i].addActionListener(sml);
            _specialMenu.add(specialMenuItems[i]);
        }
        _menubar.add(_specialMenu);

        setMenuBar(_menubar);

        add("Center", plot);
        // FIXME: This should not be hardwired in here.
        setSize(500, 300);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a sample plot.
     */
    public void samplePlot() {
        _filename = null;
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
    protected MenuBar _menubar = new MenuBar();

    /** @serial Edit menu for this frame. */
    protected Menu _editMenu = new Menu("Edit");

    /** @serial File menu for this frame. */
    protected Menu _fileMenu = new Menu("File");

    /** @serial Special menu for this frame. */
    protected Menu _specialMenu = new Menu("Special");

    /** @serial directory that contains the input file. */
    protected String _directory = null;

    /** @serial name of the input file. */
    protected String _filename = null;


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Display basic information about the application.
     */
    protected void _about() {
        Message message = new Message(
                "Ptolemy plot frame\n" +
                "By: Edward A. Lee, eal@eecs.berkeley.edu\n" +
                "and Christopher Hylands, cxh@eecs.berkeley.edu\n" +
                "Version 3.1, Build: $Id$"+
                "For more information, see\n" +
                "http://ptolemy.eecs.berkeley.edu/java/ptplot\n");
        message.setTitle("About Ptolemy Plot");
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
        boolean originalXLog = plot.getXLog();
        narrowQuery.addCheckBox("xlog", "X Log", originalXLog);
        if (originalXTicks != null) {
            narrowQuery.setBoolean("xlog", false);            
            narrowQuery.setEnabled("xlog", false);
        }
        boolean originalYLog = plot.getYLog();
        narrowQuery.addCheckBox("ylog", "Y Log", originalYLog);
        if (originalYTicks != null) {
            narrowQuery.setBoolean("ylog", false);            
            narrowQuery.setEnabled("ylog", false);
        }

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
                    if(spec.equals("")) {
                        narrowQuery.setEnabled("xlog", true);
                    } else {
                        narrowQuery.setBoolean("xlog", false);
                        narrowQuery.setEnabled("xlog", false);
                    }
                } else if (name.equals("yticks")) {
                    String spec = wideQuery.stringValue("yticks").trim();
                    plot.read("YTicks: " + spec);
                    if(spec.equals("")) {
                        narrowQuery.setEnabled("ylog", true);
                    } else {
                        narrowQuery.setBoolean("ylog", false);
                        narrowQuery.setEnabled("ylog", false);
                    }
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
                } else if (name.equals("xlog")) {
                    plot.setXLog(narrowQuery.booleanValue("xlog"));
                } else if (name.equals("ylog")) {
                    plot.setYLog(narrowQuery.booleanValue("ylog"));
                } else if (name.equals("connected")) {
                    _setConnected(narrowQuery.booleanValue("connected"));
                }
                plot.repaint();
            }
        });

        // Open the dialog.
        String[] buttons = {"Apply", "Cancel"};
        PanelDialog dialog =
            new PanelDialog(this, "Set plot format", panel, buttons);

        if (dialog.buttonPressed().equals("Cancel")) {
            // Restore original values.
            plot.setTitle(originalTitle);
            plot.setXLabel(originalXLabel);
            plot.setYLabel(originalYLabel);
            plot.setXRange(originalXRange[0], originalXRange[1]);
            plot.setYRange(originalYRange[0], originalYRange[1]);
            plot.setGrid(originalGrid);
            plot.setColor(originalColor);
            plot.setXLog(originalXLog);
            plot.setYLog(originalYLog);
            if (plot instanceof Plot) {
                Plot cplot = (Plot)plot;
                cplot.setMarksStyle(originalMarks);
                cplot.setImpulses(originalStems);
                _restoreConnected(originalConnected);
            }
            plot.read("XTicks: " + originalXTicksSpec);
            if(originalXTicksSpec.equals("")) {
                narrowQuery.setEnabled("xlog", true);
            } else {
                narrowQuery.setBoolean("xlog", false);
                narrowQuery.setEnabled("xlog", false);
            }
            plot.read("YTicks: " + originalYTicksSpec);
            if(originalYTicksSpec.equals("")) {
                narrowQuery.setEnabled("ylog", true);
            } else {
                narrowQuery.setBoolean("ylog", false);
                narrowQuery.setEnabled("ylog", false);
            }
        } else {
            // Apply current values.
            plot.setTitle(wideQuery.stringValue("title"));
            plot.setXLabel(wideQuery.stringValue("xlabel"));
            plot.setYLabel(wideQuery.stringValue("ylabel"));
            plot.read("XRange: " + wideQuery.stringValue("xrange"));
            plot.read("YRange: " + wideQuery.stringValue("yrange"));
            plot.setGrid(narrowQuery.booleanValue("grid"));
            plot.setColor(narrowQuery.booleanValue("color"));
            plot.setXLog(narrowQuery.booleanValue("xlog"));
            plot.setYLog(narrowQuery.booleanValue("ylog"));
            if (plot instanceof Plot) {
                Plot cplot = (Plot)plot;
                cplot.setMarksStyle(wideQuery.stringValue("marks"));
                cplot.setImpulses(narrowQuery.booleanValue("stems"));
                _setConnected(narrowQuery.booleanValue("connected"));
            }
            String spec = wideQuery.stringValue("xticks").trim();
            plot.read("XTicks: " + spec);
            if(spec.equals("")) {
                narrowQuery.setEnabled("xlog", true);
            } else {
                narrowQuery.setBoolean("xlog", false);
                narrowQuery.setEnabled("xlog", false);
            }
            spec = wideQuery.stringValue("yticks").trim();
            plot.read("YTicks: " + spec);
            if(spec.equals("")) {
                narrowQuery.setEnabled("ylog", true);
            } else {
                narrowQuery.setBoolean("ylog", false);
                narrowQuery.setEnabled("ylog", false);
            }
        }
        plot.repaint();
    }

    /** Query the user for a filename and export the plot to that file.
     *  Currently, the only supported format is EPS.
     */
    protected void _export() {
        FileDialog filedialog = new FileDialog(this, "Export EPS to...");
        if (_directory != null) {
            filedialog.setDirectory(_directory);
        }
        filedialog.setFile("plot.eps");
        filedialog.setVisible(true);
        String filename = filedialog.getFile();
        if (filename == null) return;
        String directory = filedialog.getDirectory();
        File file = new File(directory, filename);
        try {
            FileOutputStream fout = new FileOutputStream(file);
            plot.export(fout);
        } catch (IOException ex) {
            Message msg = new Message("Error exporting plot: " + ex);
        }
    }

    /** Display more detailed information than given by _about().
     */
    protected void _help() {
        Message message = new Message(
                "This is a versatile two-dimensional data plotter " +
                "that runs as part of an application, but in its own " +
                "window. It can read files compatible with the Ptolemy " +
                "plot file format (currently only ASCII), or the " +
                "application can interact directly with the contained " +
                "Plot object, which is visible as a public member. " +
                "For a description of the file format, see the Plot " +
                "and PlotBox classes.",
                null, null, 8, 60, TextArea.SCROLLBARS_NONE);
        message.setTitle("Plot frame");
    }

    /** Open a new file and plot its data.
     */
    protected void _open() {
        FileDialog filedialog = new FileDialog(this, "Select a plot file");
        if (_directory != null) {
            filedialog.setDirectory(_directory);
        }
        filedialog.setVisible(true);
        String filename = filedialog.getFile();
        if (filename == null) return;
        _directory = filedialog.getDirectory();
        File file = new File(_directory, filename);
        String dir = file.getParent();
        if (dir != null) {
            // NOTE: It's not clear why the file separator is needed
            // here on the end, but it is...
            _directory = dir + File.separator;
        }
        _filename = null;
        try {
            plot.clear(true);
            _read(new URL("file", null, _directory), new FileInputStream(file));
            plot.repaint();
        } catch (FileNotFoundException ex) {
            Message msg = new Message("File not found: " + ex);
        } catch (IOException ex) {
            Message msg = new Message("Error reading input: " + ex);
        }
        _filename = filename;
    }

    /** Print the plot.
     */
    protected void _print() {
        // The awt uses properties to set the defaults:
        // awt.print.destination   - can be "printer" or "file"
        // awt.print.printer       - print command
        // awt.print.fileName      - name of the file to print
        // awt.print.numCopies     - obvious
        // awt.print.options       - options to pass to the print command
        // awt.print.orientation   - can be "portrait" or "landscape"
        // awt.print.paperSize     - can be "letter", "legal", "executive"
        //                           or "a4"

        // Accept the defaults... But if you want to change them,
        // do something like this...
        // Properties newprops = new Properties();
        // newprops.put("awt.print.destination", "file");
        // newprops.put("awt.print.fileName", _outputFile);
        // PrintJob printjob = getToolkit().getPrintJob(this,
        //      getTitle(), newprops);
        PrintJob printjob = getToolkit().getPrintJob(this,
                getTitle(), null);
        if (printjob != null) {
            try {
                Graphics printgraphics = printjob.getGraphics();
                if (printgraphics != null) {
                    // Print only the plot frame.
                    try {
                        plot.printAll(printgraphics);
                    } finally {
                        printgraphics.dispose();
                    }
                }
            } finally {
                printjob.end();
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

    /** Save the plot to the current file, determined by the _directory
     *  and _filename protected variables.
     */
    protected void _save() {
        if (_filename != null) {
            File file = new File(_directory, _filename);
            try {
                FileOutputStream fout = new FileOutputStream(file);
                plot.write(fout);
            } catch (IOException ex) {
                Message msg = new Message("Error writing file: " + ex);
            }
        } else {
            _saveAs();
        }
    }

    /** Query the user for a filename and save the plot to that file.
     */
    protected void _saveAs() {
        FileDialog filedialog = new FileDialog(this, "Save plot as...");
        if (_directory != null) {
            filedialog.setDirectory(_directory);
        }
        filedialog.setFile("plot.xml");
        filedialog.setVisible(true);
        _filename = filedialog.getFile();
        if (_filename == null) return;
        _directory = filedialog.getDirectory();
        _save();
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
            MenuItem target = (MenuItem)e.getSource();
            String actionCommand = target.getActionCommand();
            if (actionCommand.equals("Open")) _open();
            else if (actionCommand.equals("Save")) _save();
            else if (actionCommand.equals("SaveAs")) _saveAs();
            else if (actionCommand.equals("Export")) _export();
            else if (actionCommand.equals("Print")) _print();
            else if (actionCommand.equals("Close")) _close();
        }
    }

    class FormatListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            _editFormat();
        }
    }

    class SpecialMenuListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            MenuItem target = (MenuItem)e.getSource();
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
        }
    }
}
