/* 2-D plotter widget

 Copyright (c) 1998 The Regents of the University of California.
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
@ProposedRating Red
@AcceptedRating Red
*/

package ptolemy.plot;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.StringTokenizer;

// TO DO:
//   - Add a mechanism for combining two plots into one
//   - Convert to use swing, especially for the menu.
//   - Add a swing-based dialog for setting the plot format
//   - Add a swing-based dialog for adding points.
//   - Improve the help mechanism and separate from the usage message.
//   - Add an "export" mechanism.  Should create:
//        + and HTML file and a .plt plot file.
//        + a gif
//        + an MIF file
//        + what else?

//////////////////////////////////////////////////////////////////////////
//// PlotFrame
/**

PlotFrame is a versatile two-dimensional data plotter that runs as
part of an application, but in its own window. It can read files
compatible with the Ptolemy plot file format (currently only ASCII),
or the application can interact directly with the contained Plot
object, which is visible as a public member. For a description of
the file format, see the Plot and PlotBox classes.
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

@see Plot
@see PlotBox
@author Christopher Hylands and Edward A. Lee
@version $Id$
*/
public class PlotFrame extends Frame {

    /** Construct a plot frame with a default title.
     */
    public PlotFrame() {
        this("Ptolemy Plot Frame");
    }

    /** Construct a plot frame with the specified title.
     */
    public PlotFrame(String title) {
        super(title);
        // File menu
        MenuItem[] fileMenuItems = {
            // FIXME: These shortcuts are not right.
            new MenuItem("Open", new MenuShortcut(KeyEvent.VK_O)),
            new MenuItem("Save", new MenuShortcut(KeyEvent.VK_S)),
            new MenuItem("SaveAs", new MenuShortcut(KeyEvent.VK_A)),
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

        // Special menu
        MenuItem[] specialMenuItems = {
            new MenuItem("About", null),
            new MenuItem("Help", new MenuShortcut(KeyEvent.VK_H)),
            new MenuItem("Clear", new MenuShortcut(KeyEvent.VK_C)),
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

        add("Center",plot);
        // FIXME: This should not be hardwired in here.
        setSize(500, 300);
        setVisible(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a sample plot.
     */
    public void samplePlot() {
        // Create a sample plot.
        plot.clear(true);
        _filename = null;
        _directory = null;

        plot.setTitle("Sample plot");
        plot.setYRange(-4, 4);
        plot.setXRange(0, 100);
        plot.setXLabel("time");
        plot.setYLabel("value");
        plot.addYTick("-PI", -Math.PI);
        plot.addYTick("-PI/2", -Math.PI/2);
        plot.addYTick("0", 0);
        plot.addYTick("PI/2", Math.PI/2);
        plot.addYTick("PI", Math.PI);
        plot.setNumSets(10);
        plot.setMarksStyle("none");
        plot.setImpulses(true);

        boolean first = true;
        for (int i = 0; i <= 100; i++) {
            plot.addPoint(0, (double)i,
                    5 * Math.cos(Math.PI * i/20), !first);
            plot.addPoint(1, (double)i,
                    4.5 * Math.cos(Math.PI * i/25), !first);
            plot.addPoint(2, (double)i,
                    4 * Math.cos(Math.PI * i/30), !first);
            plot.addPoint(3, (double)i,
                    3.5* Math.cos(Math.PI * i/35), !first);
            plot.addPoint(4, (double)i,
                    3 * Math.cos(Math.PI * i/40), !first);
            plot.addPoint(5, (double)i,
                    2.5 * Math.cos(Math.PI * i/45), !first);
            plot.addPoint(6, (double)i,
                    2 * Math.cos(Math.PI * i/50), !first);
            plot.addPoint(7, (double)i,
                    1.5 * Math.cos(Math.PI * i/55), !first);
            plot.addPoint(8, (double)i,
                    1 * Math.cos(Math.PI * i/60), !first);
            plot.addPoint(9, (double)i,
                    0.5 * Math.cos(Math.PI * i/65), !first);
            first = false;
        }
        plot.repaint();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The plot object held by this frame.
     */
    public Plot plot = new Plot();

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    protected MenuBar _menubar = new MenuBar();
    protected Menu _fileMenu = new Menu("File");
    protected Menu _specialMenu = new Menu("Special");
    protected String _directory = null;
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
                "Version 2.0, Build: $Id$\n\n"+
                "For more information, see\n" +
                "http://ptolemy.eecs.berkeley.edu/java/ptplot\n");
        message.setTitle("About Ptolemy Plot");
    }

    /** Close the window.
     */
    protected void _close() {
        dispose();
    }

    /** Display more detailed information than given by _about().
     */
    protected void _help() {
        Message message = new Message(
                "PlotFrame is a versatile two-dimensional data plotter " + 
                "that runs as part of an application, but in its own " +
                "window. It can read files compatible with the Ptolemy " +
                "plot file format (currently only ASCII), or the " +
                "application can interact directly with the contained " +
                "Plot object, which is visible as a public member. " +
                "For a description of the file format, see the Plot " +
                "and PlotBox classes.");
        message.setTitle("Plot frame");
    }

    /** Open a new file and plot its data.
     */
    protected void _open() {
        FileDialog filedialog = new FileDialog(this, "Select a plot file");
        filedialog.setFilenameFilter(new PlotFilenameFilter());
        if (_directory != null) {
            filedialog.setDirectory(_directory);
        }
        filedialog.setVisible(true);
        String filename = filedialog.getFile();
        if (filename == null) return;
        _directory = filedialog.getDirectory();
        File file = new File(_directory, filename);
        _filename = null;
        try {
            plot.clear(true);
            plot.read(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            Message msg = new Message("File not found: " + ex);
        } catch (IOException ex) {
            Message msg = new Message("Error reading input: " + ex);
        }
        _filename = filename;
    }

    /** Print the plot.
     */
    protected void _print () {
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
        // Properties newprops= new Properties();
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
        filedialog.setFilenameFilter(new PlotFilenameFilter());
        if (_directory != null) {
            filedialog.setDirectory(_directory);
        }
        filedialog.setFile("plot.plt");
        filedialog.setVisible(true);
        _filename = filedialog.getFile();
        if (_filename == null) return;
        _directory = filedialog.getDirectory();
        _save();
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
            else if (actionCommand.equals("Print")) _print();
            else if (actionCommand.equals("Close")) _close();
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

    // FIXME: This filter doesn't work.  Why?
    class PlotFilenameFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            name = name.toLowerCase();
            if (name.endsWith(".plt")) return true;
            return false;
        }
    }
}
