/* A signal plotter.

@Copyright (c) 1997- The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptolemy.plot;

// TO DO:
//   - support for oscilloscope-like plots (where x axis wraps around).
//   - steps between points rather than connected lines.
//   - cubic spline interpolation
//   - fix missing fill button under MacOS8.x/Netscape4.04
//   - create a binary file format that includes plot configuration.
//   - define a binary file format with formatting info.
//
// NOTE: The XOR drawing mode is needed in order to be able to erase
// plotted points and restore the grid line, tick marks, and boundary
// rectangle.  Another alternative would be to put the tick marks
// outside the rectangle, disallow grid marks, and adjust drawing so
// it never overlaps the boundary rectangle.  Then erasing could be
// done by redrawing in white. This would be better.

// NOTE: There are quite a few subjective spacing parameters, all
// given, unfortunately, in pixels.  This means that as resolutions
// get better, this program may need to be adjusted.

import java.awt.*;
import java.io.*;
import java.util.*;
import java.net.*;

//////////////////////////////////////////////////////////////////////////
//// Plot
/**
 * A flexible signal plotter.  The plot can be configured and data can
 * be provided either through a file with commands or through direct
 * invocation of the public methods of the class.  To read a file or a
 * URL, use the read() method.
 * <p>
 * When calling the public methods, in most cases the changes will not
 * be visible until paint() has been called.  To request that this
 * be done, call repaint().  One exception is addPoint(), which
 * makes the new point visible immediately if the plot is visible on
 * the screen.
 * <p>
 * For backwards compatibility with an older program, pxgraph, the
 * readPxgraph() method reads pxgraph-compatible
 * binary files.  Since those binary
 * files have no format information, and format information in pxgraph is
 * provided by command line arguments, a method parsePxgraphargs() is
 * provided to interpret the pxgraph-style command-line arguments.
 * <p>
 * The ASCII format for the file
 * file contains any number commands,
 * one per line.  Unrecognized commands and commands with syntax
 * errors are ignored.  Comments are denoted by a line starting with a
 * pound sign "#".  The recognized commands include those supported by
 * the base class, plus a few more.  The commands are case
 * insensitive, but are usually capitalized.  The number of data sets
 * to be plotted does not need to be specified.  Data sets are added as needed.
 * Each dataset can be optionally identified with
 * color (see the base class) or with unique marks.  The style of
 * marks used to denote a data point is defined by one of the following
 * commands:
 * <pre>
 * Marks: none
 * Marks: points
 * Marks: dots
 * Marks: various
 * </pre>
 * Here, "points" are small dots, while "dots" are larger.  If "various"
 * is specified, then unique marks are used for the first ten data sets,
 * and then recycled.
 * Using no marks is useful when lines connect the points in a plot,
 * which is done by default.  To disable connecting lines, use:
 * <pre>
 * Lines: off
 * </pre>
 * To reenable them, use
 * <pre>
 * Lines: on
 * </pre>
 * You can also specify "impulses", which are lines drawn from a plotted point
 * down to the x axis.  Plots with impulses are often called "stem plots."
 * These are off by default, but can be turned on with the
 * command:
 * <pre>
 * Impulses: on
 * </pre>
 * or back off with the command
 * <pre>
 * Impulses: off
 * </pre>
 * To create a bar graph, turn off lines and use any of the following commands:
 * <pre>
 * Bars: on
 * Bars: <i>width</i>
 * Bars: <i>width, offset</i>
 * </pre>
 * The <i>width</i> is a real number specifying the width of the bars
 * in the units of the x axis.  The <i>offset</i> is a real number
 * specifying how much the bar of the <i>i</i><sup>th</sup> data set
 * is offset from the previous one.  This allows bars to "peek out"
 * from behind the ones in front.  Note that the frontmost data set
 * will be the last one.  To turn off bars, use
 * <pre>
 * Bars: off
 * </pre>
 * To specify data to be plotted, start a data set with the following command:
 * <pre>
 * DataSet: <i>string</i>
 * </pre>
 * Here, <i>string</i> is a label that will appear in the legend.
 * It is not necessary to enclose the string in quotation marks.
 * To start a new dataset without giving it a name, use:
 * <pre>
 * DataSet:
 * </pre>
 * In this case, no item will appear in the legend.
 * If the following directive occurs:
 * <pre>
 * ReuseDataSets: on
 * </pre>
 * Then datasets with the same name will be merged.  This makes it
 * easier to combine multiple datafiles that contain the same datasets
 * into one file.  By default, this capability is turned off, so
 * datasets with the same name are not merged.
 * The data itself is given by a sequence of commands with one of the
 * following forms:
 * <pre>
 * <i>x</i>, <i>y</i>
 * draw: <i>x</i>, <i>y</i>
 * move: <i>x</i>, <i>y</i>
 * <i>x</i>, <i>y</i>, <i>yLowErrorBar</i>, <i>yHighErrorBar</i>
 * draw: <i>x</i>, <i>y</i>, <i>yLowErrorBar</i>, <i>yHighErrorBar</i>
 * move: <i>x</i>, <i>y</i>, <i>yLowErrorBar</i>, <i>yHighErrorBar</i>
 * </pre>
 * The "draw" command is optional, so the first two forms are equivalent.
 * The "move" command causes a break in connected points, if lines are
 * being drawn between points. The numbers <i>x</i> and <i>y</i> are
 * arbitrary numbers as supported by the Double parser in Java.
 * If there are four numbers, then the last two numbers are assumed to
 * be the lower and upper values for error bars.
 * The numbers can be separated by commas, spaces or tabs.
 * <p>
 * This plotter has some <A NAME="ptplot limitations">limitations</a>:
 * <ul>
 * <li> Marks, impulses, and bars are assumed to apply to the entire
 *      plot, i.e. to all data sets.  Although it is possible to change
 *      these styles for different data sets, the graph will not be
 *      correctly redrawn when it gets redrawn due to zooming in or out
 *      or due to a window exposure event.
 * <li> If you zoom in far enough, the plot becomes unreliable.
 *      In particular, if the total extent of the plot is more than
 *      2<sup>32</sup> times extent of the visible area, quantization
 *      errors can result in displaying points or lines.
 *      Note that 2<sup>32</sup> is over 4 billion.
 * <li> The limitations of the log axis facility are listed in
 *      the <code>_gridInit()</code> method in the PlotBox class.
 * <li> The compatibility issues of the <code>pxgraph</code> script are
 *      list in the
 *<a href="pt.plot.Pxgraph.html#pxgraph script compatibility issues">Pxgraph</a>
 *      class.
 * </ul>
 *
 * @author Edward A. Lee, Christopher Hylands
 * @version $Id$
 */
public class Plot extends PlotBox {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** In the specified data set, add the specified x, y point to the
     *  plot.  Data set indices begin with zero.  If the data set
     *  does not exist, create it.  The fourth argument indicates
     *  whether the point should be connected by a line to the previous
     *  point.  The new point will be made visible if the plot is visible
     *  on the screen.  Otherwise, it will be drawn the next time the plot
     *  is drawn on the screen.
     *  @param dataset The data set index.
     *  @param x The X position of the new point.
     *  @param y The Y position of the new point.
     *  @param connected If true, a line is drawn to connect to the previous
     *   point.
     */
    public synchronized void addPoint(int dataset, double x, double y,
            boolean connected) {
        if (_xlog) {
            if (x <= 0.0) {
                System.err.println("Can't plot non-positive X values "+
                        "when the logarithmic X axis value is specified: " +
                        x);
                return;
            }
            x = Math.log(x)*_LOG10SCALE;
        }
        if (_ylog) {
            if (y <= 0.0) {
                System.err.println("Can't plot non-positive Y values "+
                        "when the logarithmic Y axis value is specified: " +
                        y);
                return;
            }
            y = Math.log(y)*_LOG10SCALE;
        }
        // This point is not an error bar so we set yLowEB
        // and yHighEB to 0
        _addPoint(dataset, x, y, 0, 0, connected, false);
    }

    /** In the specified data set, add the specified x, y point to the
     *  plot with error bars.  Data set indices begin with zero.  If
     *  the dataset does not exist, create it.  yLowEB and
     *  yHighEB are the lower and upper error bars.  The sixth argument
     *  indicates whether the point should be connected by a line to
     *  the previous point.
     *  The new point will be made visible if the plot is visible
     *  on the screen.  Otherwise, it will be drawn the next time the plot
     *  is drawn on the screen.
     *  This method is based on a suggestion by
     *  Michael Altmann <michael@email.labmed.umn.edu>.
     *
     *  @param dataset The data set index.
     *  @param x The X position of the new point.
     *  @param y The Y position of the new point.
     *  @param yLowEB The low point of the error bar.
     *  @param yHighEB The high point of the error bar.
     *  @param connected If true, a line is drawn to connect to the previous
     *   point.
     */
    public synchronized void addPointWithErrorBars(int dataset,
            double x, double y, double yLowEB, double yHighEB,
            boolean connected) {
        if (_xlog) {
            if (x <= 0.0) {
                System.err.println("Can't plot non-positive X values "+
                        "when the logarithmic X axis value is specified: " +
                        x);
                return;
            }
            x = Math.log(x)*_LOG10SCALE;
        }
        if (_ylog) {
            if (y <= 0.0 || yLowEB <= 0.0 || yHighEB <= 0.0) {
                System.err.println("Can't plot non-positive Y values "+
                        "when the logarithmic Y axis value is specified: " +
                        y);
                return;
            }
            y = Math.log(y)*_LOG10SCALE;
            yLowEB = Math.log(yLowEB)*_LOG10SCALE;
            yHighEB = Math.log(yHighEB)*_LOG10SCALE;
        }
        _addPoint(dataset, x, y,
                yLowEB, yHighEB, connected, true);
    }

    /** Clear the plot of all data points.  This resets all parameters
     *  to their initial conditions, including the persistence and plotting
     *  format.  If the argument is false, then
     *  the axes display is kept the same.  Otherwise, all parameters
     *  controlling the axis displays are set to their initial condition.
     *  For the change to take effect, you must call repaint().
     *  @param axes If true, clear the axes too.
     */
    public synchronized void clear (boolean axes) {
        super.clear(axes);
        _currentdataset = -1;
        int size = _points.size();
        _points = new Vector();
        _prevx = new Vector();
        _prevy = new Vector();
        _marks = 0;
        _painted = false;

        // Reset the private variables to their initial state.
        _pointsPersistence = 0;
        _sweepsPersistence = 0;
        _bars = false;
        _barwidth = 0.5;
        _baroffset = 0.05;
        _connected = true;
        _impulses = false;
        _maxdataset = -1;
        _reusedatasets = false;
        _firstinset = true;
        _sawfirstdataset = false;
        _pxgraphBlankLineMode = true;
        _endian = _NATIVE_ENDIAN;
        _xyInvalid = false;
        _filename = null;
        _showing = false;
    }

    /** Erase the point at the given index in the given dataset.  If
     *  lines are being drawn, also erase the line to the next points
     *  (note: not to the previous point).  The point is not checked to
     *  see whether it is in range, so care must be taken by the caller
     *  to ensure that it is.
     *  The change will be made visible if the plot is visible
     *  on the screen.  Otherwise, it will take effect the next time the plot
     *  is drawn on the screen.
     *
     *  @param dataset The data set index.
     *  @param index The index of the point to erase.
     */
    public synchronized void erasePoint(int dataset, int index) {
        if (isShowing()) {
            _erasePoint(getGraphics(), dataset, index);
        }
        Vector points = (Vector)_points.elementAt(dataset);
        if (points != null) {
            // If this point is at the maximum or minimum x or y boundary,
            // then flag that boundary needs to be recalculated next time
            // fillPlot() is called.
            PlotPoint pt = (PlotPoint)points.elementAt(index);
            if (pt != null) {
                if (pt.x == _xBottom || pt.x == _xTop ||
                        pt.y == _yBottom || pt.y == _yTop) {
                    _xyInvalid = true;
                }

                points.removeElementAt(index);
            }
        }
    }

    /** Rescale so that the data that is currently plotted just fits.
     *  This overrides the base class method to ensure that the protected
     *  variables _xBottom, _xTop, _yBottom, and _yTop are valid.
     *  This method calls repaint(), which eventually causes the display
     *  to be updated.
     */
    public synchronized void fillPlot () {
        if (_xyInvalid) {
            // Recalculate the boundaries based on currently visible data
            _xBottom = Double.MAX_VALUE;
            _xTop = - Double.MIN_VALUE;
            _yBottom = Double.MAX_VALUE;
            _yTop = - Double.MIN_VALUE;
            for (int dataset=0; dataset < _points.size(); dataset++) {
                Vector points = (Vector)_points.elementAt(dataset);
                for (int index=0; index < points.size(); index++) {
                    PlotPoint pt = (PlotPoint)points.elementAt(index);
                    if (pt.x < _xBottom) _xBottom = pt.x;
                    if (pt.x > _xTop) _xTop = pt.x;
                    if (pt.y < _yBottom) _yBottom = pt.y;
                    if (pt.y > _yTop) _yTop = pt.y;
                }
            }
        }
        _xyInvalid = false;
        super.fillPlot();
    }

    /** Return the last file name seen on the command-line arguments parsed
     *  by parseArgs().  If there was none, return null.
     *  @return A file name, or null if there is none.
     */
    public String getCmdLineFilename() {
        return _filename;
    }

    /** Return the maximum number of data sets.
     *  This method is deprecated, since there is no longer an upper bound.
     *  @deprecated
     */
    public int getMaxDataSets() {
        return Integer.MAX_VALUE;
    }

    /** Start a new thread to paint the component contents.
     *  This is done in a new thread so that large data sets can
     *  can be displayed without freezing the user interface.
     *  When repainting is completed, the protected variable _painted
     *  is set to true, and notifyAll() is called.
     *  @param graphics The graphics context.
     */
    public void paint(Graphics graphics) {
        _drawPlot(graphics, true);

        // NOTE: jdk 1.1.4 cannot compile the following preferred method:
        //         Thread paintthread = new Thread() {
        //             public void run() {
        //                 _drawPlot(graphics, true);
        //             }
        //         };
        //        paintthread.start();
        // NOTE: This strategy fails due to a bug in jdk 1.1
        // Anything drawn to the graphics object in another thread
        // never appears.
        //        _painted = false;
        //         if (_painter == null) {
        //             _painter = new Painter(graphics);
        //         }
        //         // Stop the thread if still drawing from previous call.
        //         if (_painting) _painter.stop();
        //         _painter.start();
    }

    /** Parse pxgraph style command line arguments.
     *  This method exists only for backward compatibility with the X11 pxgraph
     *  program.
     *
     *  @param args A set of command-line arguments.
     *  @return The number of arguments read.
     *  @exception CmdLineArgException If there is a problem parsing
     *   the command line arguments.
     *  @exception FileNotFoundException If a file is specified that is not
     *   found.
     *  @exception IOException If an error occurs reading an input file.
     */
    public int parseArgs(String args[]) throws CmdLineArgException,
            FileNotFoundException, IOException {
        int i = 0, j, argsread = 0;

        // If we see both -nl and -bar, assume we do an impulse plot.
        boolean sawbararg = false; // Saw -bar arg.
        boolean sawnlarg = false;  // Saw -nl arg.
        int savedmarks = 0;        // Save _marks in case we have -P -bar -nl.
        boolean binary = false;    // Read a binary xgraph file.

        String arg;
        String unsupportedOptions[] = {
            "-bd", "-brb", "-bw", "-gw", "-lw", "-zg", "-zw"
        };

        while (i < args.length && (args[i].startsWith("-") ||
                args[i].startsWith("=")) ) {
            arg = args[i++];

            if (arg.startsWith("-")) {
                // Search for unsupported options that take arguments
                boolean badarg = false;
                for(j = 0; j < unsupportedOptions.length; j++) {
                    if (arg.equals(unsupportedOptions[j])) {
                        System.err.println("pxgraph: " + arg +
                                " is not yet supported");
                        i++;
                        badarg = true;
                    }
                }
                if (badarg) continue;

                if (arg.equals("-bb")) {
                    // We ignore -bb because the Java version of pxgraph plot
                    // region is a different color from the surrounding region.
                    continue;
                } else if (arg.equals("-bg")) {
                    setBackground(getColorByName(args[i++]));
                    continue;
                } else if (arg.equals("-brw")) {
                    // -brw <width> BarWidth Bars:
                    // We default the baroffset to 0 here if the value does
                    // not include a comma.
                    if (arg.indexOf(",") == -1) {
                        if (!_parseLine("Bars: " + args[i++]+",0")) {
                            throw new
                                CmdLineArgException("Failed to parse `"+
                                        arg+"'");
                        }
                    } else {
                        if (!_parseLine("Bars: " + args[i++])) {
                            throw new
                                CmdLineArgException("Failed to parse `"+
                                        arg+"'");
                        }
                    }
                    continue;
                } else if (arg.equals("-lf")) {
                    // -lf <labelfont>
                    setLabelFont(args[i++]);
                    continue;
                } else if (arg.equals("-lx")) {
                    // -lx <xl,xh> XLowLimit, XHighLimit  XRange:
                    if (!_parseLine("XRange: " + args[i++])) {
                        throw new
                            CmdLineArgException("Failed to parse `"+arg+"'");
                    }
                    continue;
                } else if (arg.equals("-ly")) {
                    // -ly <yl,yh> YLowLimit, YHighLimit  YRange:
                    if (!_parseLine("YRange: " + args[i++])) {
                        throw new
                            CmdLineArgException("Failed to parse `"+arg+"'");
                    }
                    continue;
                } else if (arg.equals("-t")) {
                    // -t <title> TitleText "An X Graph"
                    String title =  args[i++];
                    setTitle(title);
                    continue;
                } else if (arg.equals("-tf")) {
                    // -tf <titlefont>
                    setTitleFont(args[i++]);
                    continue;
                } else if (arg.equals("-x")) {
                    // -x <unitName> XUnitText XLabel:
                    setXLabel(args[i++]);
                    continue;
                } else if (arg.equals("-y")) {
                    // -y <unitName> YUnitText YLabel:
                    setYLabel(args[i++]);
                    continue;
                } else if (arg.equals("-bar")) {
                    //-bar BarGraph Bars: on Marks: none Lines: off
                    // If we saw the -nl arg, then assume impulses
                    sawbararg = true;
                    if (sawnlarg) {
                        setImpulses(true);
                    } else {
                        setBars(true);
                        // Save _marks in case we did -P -bar -nl.
                        savedmarks = _marks;
                        setMarksStyle("none");
                    }
                    setConnected(false);
                    continue;
                } else if (arg.equals("-binary")) {
                    binary = true;
                    _endian = _NATIVE_ENDIAN;
                    continue;
                } else if (arg.equals("-bigendian")) {
                    binary = true;
                    _endian = _BIG_ENDIAN;
                    continue;
                } else if (arg.equals("-littleendian")) {
                    binary = true;
                    _endian = _LITTLE_ENDIAN;
                    continue;
                } else if (arg.equals("-db")) {
                    // Ignore.  Debug flag.
                    continue;
                } else if (arg.equals("-debug")) {
                    // -debug is not in the original X11 pxgraph.
                    // _debug = (int)Integer.valueOf(args[i++]).intValue();
                    continue;
                } else if (arg.equals("-fg")) {
                    setForeground(getColorByName(args[i++]));
                    continue;
                } else if (arg.equals("-help")) {
                    // -help is not in the original X11 pxgraph.
                    //_help();
                    continue;
                } else if (arg.equals("-impulses")) {
                    // -impulses is not in the original X11 pxgraph.
                    setImpulses(true);
                    setConnected(false);
                    continue;
                } else if (arg.equals("-lnx")) {
                    setXLog(true);
                    continue;
                } else if (arg.equals("-lny")) {
                    setYLog(true);
                    continue;
                } else if (arg.equals("-m")) {
                    // -m Markers Marks: various
                    setMarksStyle("various");
                    continue;
                } else if (arg.equals("-M")) {
                    // -M StyleMarkers Marks: various
                    setMarksStyle("various");
                    continue;
                } else if (arg.equals("-nl")) {
                    // -nl NoLines Lines: off
                    // If we saw the -bar arg, then assume impulses
                    sawnlarg = true;
                    if (sawbararg) {
                        // Restore the _marks in case we did -P -bar -nl
                        _marks = savedmarks;
                        setBars(false);
                        setImpulses(true);
                    }
                    setConnected(false);
                    continue;
                } else if (arg.equals("-o")) {
                    // -o <output filename>
                    // _outputFile =  args[i++];
                    i++;
                    continue;
                } else if (arg.equals("-p")) {
                    // -p PixelMarkers Marks: points
                    setMarksStyle("points");
                    continue;
                } else if (arg.equals("-P")) {
                    // -P LargePixel Marks: dots\n
                    setMarksStyle("dots");
                    continue;
                } else if (arg.equals("-print")) {
                    // -print is not in the original X11 pxgraph.
                    continue;
                } else if (arg.equals("-rv")) {
                    setBackground(getColorByName("black"));
                    setForeground(getColorByName("white"));
                    continue;
                } else if (arg.equals("-test")) {
                    // -test is not in the original X11 pxgraph.
                    //_test = true;
                    continue;
                } else if (arg.equals("-tk")) {
                    setGrid(false);
                    continue;
                } else if (arg.equals("-v") || arg.equals("-version")) {
                    // -version is not in the original X11 pxgraph.
                    //_version();
                    continue;
                } else if (arg.equals("-m")) {

                } if (arg.length() > 1  && arg.charAt(0) == '-') {
                    // Process '-<digit> <datasetname>'
                    try {
                        Integer datasetnumberint = new
                            Integer(arg.substring(1));
                        int datasetnumber = datasetnumberint.intValue();
                        if (datasetnumber >= 0) {
                            addLegend(datasetnumber, args[i++]);
                            continue;
                        }
                    } catch (NumberFormatException e) {}
                }
            } else {
                if (arg.startsWith("=")) {
                    // Process =WxH+X+Y
                    _width = (int)Integer.valueOf(arg.substring(1,
                            arg.indexOf('x'))).intValue();
                    int plusIndex = arg.indexOf('+');
                    int minusIndex = arg.indexOf('-');
                    if (plusIndex != -1 || minusIndex != -1) {
                        // =WxH+X+Y, =WxH-X+Y, =WxH-X-Y, =WxH+X-Y
                        if ( plusIndex != -1 && minusIndex != -1) {
                            // =WxH-X+Y or =WxH+X-Y
                            int index = minusIndex;
                            if (plusIndex < minusIndex) {
                                index = plusIndex;
                            }
                            _height = Integer.valueOf(arg.substring(
                                        arg.indexOf('x')+1,
                                        index)).intValue();
                        } else {
                            if (plusIndex != -1) {
                                // =WxH+X+Y
                                _height = Integer.valueOf(arg.substring(
                                            arg.indexOf('x')+1,
                                            plusIndex)).intValue();
                            } else {
                                // =WxH-X-Y
                                _height = Integer.valueOf(arg.substring(
                                            arg.indexOf('x')+1,
                                            minusIndex)).intValue();
                            }
                        }
                    } else {
                        if (arg.length() > arg.indexOf('x')) {
                            // =WxH
                            _height = Integer.valueOf(arg.substring(
                                        arg.indexOf('x')+1,
                                        arg.length())).intValue();
                        }
                    }
                    // FIXME: it is unclear what X and Y in =WxH+X+Y mean
                    // in a non-toplevel window, so we don't process
                    // those here.  See Pxgraph.java for how to process
                    // X and Y for a toplevel window.
                    continue;
                }
            }
            // If we got to here, then we failed to parse the arg
            throw new
                CmdLineArgException("Failed to parse `" + arg + "'");
        }
        argsread = i++;

        setSize(_width, _height);

        for(i = argsread; i < args.length; i++) {
            // Have a filename.  First attempt to open it as a URL.
            InputStream instream;
            try {
                URL inurl = new URL(_documentBase, args[i]);
                instream = inurl.openStream();
            } catch (MalformedURLException ex) {
                instream = new FileInputStream(args[i]);
                _filename = args[i];
            }
            if (binary) {
                readPxgraph(instream);
            } else {
                read(instream);
            }
        }
        return argsread;
    }

    /** Override the base class to indicate that a new data set is being read.
     *  This method is deprecated.  Use read() or readPxgraph() instead.
     *  @deprecated
     */
    public void parseFile(String filespec, URL documentBase) {
        _firstinset = true;
        _sawfirstdataset = false;
        super.parseFile(filespec, documentBase);
    }

    /** Split a string containing pxgraph-compatible command-line arguments
     *  into an array and call parseArgs() on the array.  This is used
     *  in the rare circumstance that you want to control the format
     *  of a plot from an applet HTML file rather than in the plot data
     *  file.
     *  @return The number of arguments read.
     *  @exception CmdLineArgException If there is a problem parsing
     *   the command line arguments.
     *  @exception FileNotFoundException If a file is specified that is not
     *   found.
     *  @exception IOException If an error occurs reading an input file.
     */
    public int parsePxgraphargs(String pxgraphargs) throws CmdLineArgException,
            FileNotFoundException, IOException {
        // We convert the String to a Stream and then use a StreamTokenizer
        // to parse the arguments into a Vector and then copy
        // the vector into an array of Strings.  We use a Vector
        // so that we can handle an arbitrary number of arguments

        Vector argvector = new Vector();
        boolean prependdash = false; // true if we need to add a -

        StringReader pin = new StringReader(pxgraphargs);

        try {
            StreamTokenizer stoken = new StreamTokenizer(pin);

            // We don't want to parse numbers specially, so we reset
            // the syntax and then add back what we want.
            stoken.resetSyntax();
            stoken.whitespaceChars(0, ' ');
            stoken.wordChars('(', '~');
            stoken.quoteChar('"');
            stoken.quoteChar('\'');
            int c;
            String partialarg = null;
        out:
            while (true) {
                c = stoken.nextToken();
                //System.out.print(c + " "+stoken.ttype+" "+stoken.sval+" ");
                switch (stoken.ttype) {        // same as value of 'c'
                case StreamTokenizer.TT_EOF:
                    break out;
                case StreamTokenizer.TT_WORD:
                    //System.out.println("Word: " + stoken.sval);
                    if (prependdash) {
                        prependdash = false;
                        if (partialarg == null)
                            argvector.addElement(new String("-"+stoken.sval));
                        else
                            argvector.addElement(new String("-" + partialarg +
                                    stoken.sval));
                    } else {
                        if (partialarg == null)
                            argvector.addElement(new String(stoken.sval));
                        else
                            argvector.addElement(new String(partialarg +
                                    stoken.sval));
                    }
                    partialarg = null;
                    break;
                case '-':
                    prependdash = true;
                    break;
                case '#':
                case '$':
                case '%':
                case '&':
                    // The above chars can be part of a URL.  For example
                    // perl scripts use &.  However, we cannot include
                    // them in the wordChars() range of chars, since
                    // the single quote is between them and the rest of the
                    // chars. So we have to process them by hand.
                    partialarg = ((String)argvector.lastElement()) + (char)c;
                    argvector.removeElementAt(argvector.size()-1);
                    break;
                case '"':
                case '\'':
                    //System.out.println("String: " + stoken.sval);
                    argvector.addElement(new String(stoken.sval));
                    break;
                default:
                    throw new IOException("Failed to parse: '"+ (char)c +
                            "' in `" + pxgraphargs + "'");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create a array
        String args[] = new String[argvector.size()];
        for(int i = 0; i<argvector.size(); i++) {
            args[i] = (String)argvector.elementAt(i);
        }
        return parseArgs(args);
    }

    /** Override the base class to register that we are reading a new
     *  data set.
     *  @param inputstream The input stream.
     */
    public void read(InputStream in)
            throws IOException {
        super.read(in);
        _firstinset = true;
        _sawfirstdataset = false;
    }

    /** Read a pxgraph-compatible binary encoded file.
     *  @param in The input stream.
     *  @exception java.io.IOException If an I/O error occurs.
     */
    public void readPxgraph(InputStream inputstream)
            throws IOException {
        // This method is similar to _parseLine(), except it parses
        // an entire file at a time.

        Cursor oldCursor = getCursor();
        setCursor(new Cursor(Cursor.WAIT_CURSOR));

        try {
            // Flag that we are starting a new data set.
            _firstinset = true;
            // Flag that we have not seen a DataSet line in this file.
            _sawfirstdataset = false;

            DataInputStream in = new DataInputStream(
                new BufferedInputStream(inputstream));
            int c;
            float x = 0, y = 0, pointCount = 0;
            boolean byteSwapped = false;
            boolean connected = false;
            byte input[] = new byte[4];

            if (_connected) connected = true;

            switch (_endian) {
                case _NATIVE_ENDIAN:
                try {
                    if ( System.getProperty("os.arch").equals("x86")) {
                        byteSwapped = true;
                    }
                } catch (SecurityException e) {}
                break;
                case _BIG_ENDIAN:
                break;
                case _LITTLE_ENDIAN:
                byteSwapped = true;
                break;
                default:
                throw new IOException("Internal Error: Don't know about '"+
                _endian + "' style of endian");
            }

            try {
                c = in.readByte();
                if ( c != 'd') {
                    // Assume that the data is one data set, consisting
                    // of 4 byte floats.  None of the Ptolemy pxgraph
                    // binary format extensions apply.
                    // Note that the binary format is bigendian, or network
                    // order.  Little-endian machines, like x86 will not
                    // be able to write binary data directly
                    // (However, they could use Java's mechanisms for
                    // writing binary files).

                    // Read 3 more bytes, create the x float.
                    int bits = c;
                    bits = bits << 8;
                    bits += in.readByte();
                    bits = bits << 8;
                    bits += in.readByte();
                    bits = bits << 8;
                    bits += in.readByte();

                    x = Float.intBitsToFloat(bits);
                    y = in.readFloat();
                    connected = _addLegendIfNecessary(connected);
                    addPoint(_currentdataset, x, y, connected);
                    if (_connected) connected = true;

                    while (true) {
                        x = in.readFloat();
                        y = in.readFloat();
                        connected = _addLegendIfNecessary(connected);
                        addPoint(_currentdataset, x, y, connected);
                        if (_connected) connected = true;
                    }
                } else {
                    // Assume that the data is in the pxgraph binary format.
                    while (true) {
                        // For speed reasons, the Ptolemy group extended
                        // pxgraph to read binary format data.
                        // The format consists of a command character,
                        // followed by optional arguments
                        // d <4byte float> <4byte float> - Draw a X, Y point
                        // e                             - End of a data set
                        // n <chars> \n                  - New set name, ends in \n
                        // m                             - Move to a point

                        switch (c) {
                        case 'd':
                            // Data point.
                            if (byteSwapped) {
                                in.readFully(input);
                                x = Float.intBitsToFloat(
                                    (( input[3] & 0xFF ) << 24) |
                                    (( input[2] & 0xFF ) << 16) |
                                    (( input[1] & 0xFF ) << 8) |
                                    ( input[0] & 0xFF ));
                                in.readFully(input);
                                y = Float.intBitsToFloat(
                                    (( input[3] & 0xFF ) << 24) |
                                    (( input[2] & 0xFF ) << 16) |
                                    (( input[1] & 0xFF ) << 8) |
                                    ( input[0] & 0xFF ));
                            } else {
                                x = in.readFloat();
                                y = in.readFloat();
                            }
                            pointCount++;
                            connected = _addLegendIfNecessary(connected);
                            addPoint(_currentdataset, x, y, connected);
                            if (_connected) connected = true;
                            break;
                        case 'e':
                            // End of set name.
                            connected = false;
                            break;
                        case 'n':
                            StringBuffer datasetname = new StringBuffer();
                            _firstinset = true;
                            _sawfirstdataset = true;
                            _currentdataset++;
                            if (_currentdataset >= _MAX_MARKS)
                            _currentdataset = 0;
                            // New set name, ends in \n.
                            while (c != '\n')
                            datasetname.append(in.readChar());
                            addLegend(_currentdataset, datasetname.toString());
                            setConnected(true);
                            break;
                        case 'm':
                            // a disconnected point
                            connected = false;
                            break;
                        default:
                            throw new IOException("Don't understand `" +
                                (char)c + "' character " +
                                "(decimal value = " + c +
                                ") in binary file.  Last point was (" + x +
                                "," + y + ").\nProcessed " + pointCount +
                                " points sucessfully");
                        }
                        c = in.readByte();
                    }
                }
            } catch (EOFException e) {}
        } finally {
            setCursor(oldCursor);
        }
    }

    /** Turn bars on or off (for bar charts).
     *  @param on If true, turn bars on.
     */
    public void setBars (boolean on) {
        _bars = on;
    }

    /** Turn bars on and set the width and offset.  Both are specified
     *  in units of the x axis.  The offset is the amount by which the
     *  i<sup>th</sup> data set is shifted to the right, so that it
     *  peeks out from behind the earlier data sets.
     *  @param width The width of the bars.
     *  @param offset The offset per data set.
     */
    public void setBars (double width, double offset) {
        _barwidth = width;
        _baroffset = offset;
        _bars = true;
    }

    /** If the argument is true, then the default is to connect
     *  subsequent points with a line.  If the argument is false, then
     *  points are not connected.  When points are by default
     *  connected, individual points can be not connected by giving the
     *  appropriate argument to addPoint().
     */
    public void setConnected (boolean on) {
        _connected = on;
    }

    /** If the argument is true, then a line will be drawn from any
     *  plotted point down to the x axis.  Otherwise, this feature is
     *  disabled.  A plot with such lines is also known as a stem plot.
     *  @param on If true, draw a stem plot.
     */
    public void setImpulses (boolean on) {
        _impulses = on;
    }

    /** Set the marks style to "none", "points", "dots", or "various".
     *  In the last case, unique marks are used for the first ten data
     *  sets, then recycled.
     *  @param style A string specifying the style for points.
     */
    public void setMarksStyle (String style) {
        if (style.equalsIgnoreCase("none")) {
            _marks = 0;
        } else if (style.equalsIgnoreCase("points")) {
            _marks = 1;
        } else if (style.equalsIgnoreCase("dots")) {
            _marks = 2;
        } else if (style.equalsIgnoreCase("various")) {
            _marks = 3;
        }
    }

    /** Specify the number of data sets to be plotted together.
     *  This method is deprecated, since it is no longer necessary to
     *  specify the number of data sets ahead of time.
     *  It has the effect of clearing all previously plotted points.
     *  This method should be called before setPointsPersistence().
     *  This method throws IllegalArgumentException if the number is less
     *  than 1.  This is a runtime exception, so it need not be declared.
     *  @param numsets The number of data sets.
     *  @deprecated
     */
    public void setNumSets (int numsets) {
        if (numsets < 1) {
            throw new IllegalArgumentException("Number of data sets ("+
                    numsets + ") must be greater than 0.");

        }
        _currentdataset = -1;
        _points.removeAllElements();
        _prevx.removeAllElements();
        _prevy.removeAllElements();
        for (int i=0; i<numsets; i++) {
            _points.addElement(new Vector());
            _prevx.addElement(new Long(0));
            _prevy.addElement(new Long(0));
        }
    }

    /** Calling this method with a positive argument sets the
     *  persistence of the plot to the given number of points.  Calling
     *  with a zero argument turns off this feature, reverting to
     *  infinite memory (unless sweeps persistence is set).  If both
     *  sweeps and points persistence are set then sweeps take
     *  precedence.
     */
    public void setPointsPersistence (int persistence) {
        //   FIXME: No file format yet.
        _pointsPersistence = persistence;
    }

    /** A sweep is a sequence of points where the value of X is
     *  increasing.  A point that is added with a smaller x than the
     *  previous point increments the sweep count.  Calling this method
     *  with a non-zero argument sets the persistence of the plot to
     *  the given number of sweeps.  Calling with a zero argument turns
     *  off this feature.  If both sweeps and points persistence are
     *  set then sweeps take precedence.
     *  <b> This feature is not implemented yet, so this method has no
     *  effect</b>.
     */
    public void setSweepsPersistence (int persistence) {
        //   * FIXME: No file format yet.
        _sweepsPersistence = persistence;
    }

    /** Override the base class to not clear the component first.
     *  @param graphics The graphics context.
     */
    public void update(Graphics g) {
        paint(g);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////


    /** Check the argument to ensure that it is a valid data set index.
     *  If it is less than zero, throw an IllegalArgumentException (which
     *  is a runtime exception).  If it does not refer to an existing
     *  data set, then fill out the _points Vector so that it does refer
     *  to an existing data set.
     *  @param dataset The data set index.
     */
    protected void _checkDatasetIndex(int dataset) {
        if (dataset < 0) {
            throw new IllegalArgumentException("Plot._addPoint: Cannot give "
            + "a negative number for the data set index.");
        }
        while (dataset >= _points.size()) {
            _points.addElement(new Vector());
            _prevx.addElement(new Long(0));
            _prevy.addElement(new Long(0));
        }
    }

    /** Draw bar from the specified point to the y axis.
     *  If the specified point is below the y axis or outside the
     *  x range, do nothing.  If the <i>clip</i> argument is true,
     *  then do not draw above the y range.
     *  Note that paint() should be called before
     *  calling this method so that _xscale and _yscale are properly set.
     *  @param graphics The graphics context.
     *  @param dataset The index of the dataset.
     *  @param xpos The x position.
     *  @param ypos The y position.
     *  @param clip If true, then do not draw outside the range.
     */
    protected void _drawBar (Graphics graphics, int dataset,
            long xpos, long ypos, boolean clip) {
        if (clip) {
            if (ypos < _uly) {
                ypos = _uly;
            } if (ypos > _lry) {
                ypos = _lry;
            }
        }
        if (ypos <= _lry && xpos <= _lrx && xpos >= _ulx) {
            // left x position of bar.
            int barlx = (int)(xpos - _barwidth * _xscale/2 +
                    (_currentdataset - dataset - 1) *
                    _baroffset * _xscale);
            // right x position of bar
            int barrx = (int)(barlx + _barwidth * _xscale);
            if (barlx < _ulx) barlx = _ulx;
            if (barrx > _lrx) barrx = _lrx;
            // Make sure that a bar is always at least one pixel wide.
            if (barlx >= barrx) barrx = barlx+1;
            // The y position of the zero line.
            long zeroypos = _lry - (long) ((0-_yMin) * _yscale);
            if (_lry < zeroypos) zeroypos = _lry;
            if (_uly > zeroypos) zeroypos = _uly;

            if (_yMin >= 0 || ypos <= zeroypos) {
                graphics.fillRect(barlx, (int)ypos,
                        barrx - barlx, (int)(zeroypos - ypos));
            } else {
                graphics.fillRect(barlx, (int)zeroypos,
                        barrx - barlx, (int)(ypos - zeroypos));
            }
        }
    }

    /** Draw an error bar for the specified yLowEB and yHighEB values.
     *  If the specified point is below the y axis or outside the
     *  x range, do nothing.  If the <i>clip</i> argument is true,
     *  then do not draw above the y range.
     *  @param graphics The graphics context.
     *  @param dataset The index of the dataset.
     *  @param xpos The x position.
     *  @param yLowEBPos The lower y position of the error bar.
     *  @param yHighEBPos The upper y position of the error bar.
     *  @param clip If true, then do not draw above the range.
     */
    protected void _drawErrorBar (Graphics graphics, int dataset,
            long xpos, long yLowEBPos, long yHighEBPos,
            boolean clip) {
        _drawLine(graphics, dataset, xpos - _ERRORBAR_LEG_LENGTH, yHighEBPos,
                xpos + _ERRORBAR_LEG_LENGTH, yHighEBPos, clip);
        _drawLine(graphics, dataset, xpos, yLowEBPos, xpos, yHighEBPos, clip);
        _drawLine(graphics, dataset, xpos - _ERRORBAR_LEG_LENGTH, yLowEBPos,
                xpos + _ERRORBAR_LEG_LENGTH, yLowEBPos, clip);
    }

    /** Draw a line from the specified point to the y axis.
     *  If the specified point is below the y axis or outside the
     *  x range, do nothing.  If the <i>clip</i> argument is true,
     *  then do not draw above the y range.
     *  @param graphics The graphics context.
     *  @param xpos The x position.
     *  @param ypos The y position.
     *  @param clip If true, then do not draw outside the range.
     */
    protected void _drawImpulse (Graphics graphics,
            long xpos, long ypos, boolean clip) {
        if (clip) {
            if (ypos < _uly) {
                ypos = _uly;
            } if (ypos > _lry) {
                ypos = _lry;
            }
        }
        if (ypos <= _lry && xpos <= _lrx && xpos >= _ulx) {
            // The y position of the zero line.
            double zeroypos = _lry - (long) ((0-_yMin) * _yscale);
            if (_lry < zeroypos) zeroypos = _lry;
            if (_uly > zeroypos) zeroypos = _uly;
            graphics.drawLine((int)xpos, (int)ypos, (int)xpos,
                    (int)zeroypos);
        }
    }

    /** Draw a line from the specified starting point to the specified
     *  ending point.  The current color is used.  If the <i>clip</i> argument
     *  is true, then draw only that portion of the line that lies within the
     *  plotting rectangle.
     *  @param graphics The graphics context.
     *  @param dataset The index of the dataset.
     *  @param startx The starting x position.
     *  @param starty The starting y position.
     *  @param endx The ending x position.
     *  @param endy The ending y position.
     *  @param clip If true, then do not draw outside the range.
     */
    protected void _drawLine (Graphics graphics,
            int dataset, long startx, long starty, long endx, long endy,
            boolean clip) {

        if (clip) {
            // Rule out impossible cases.
            if (!((endx <= _ulx && startx <= _ulx) ||
                    (endx >= _lrx && startx >= _lrx) ||
                    (endy <= _uly && starty <= _uly) ||
                    (endy >= _lry && starty >= _lry))) {
                // If the end point is out of x range, adjust
                // end point to boundary.
                // The integer arithmetic has to be done with longs so as
                // to not loose precision on extremely close zooms.
                if (startx != endx) {
                    if (endx < _ulx) {
                        endy = (int)(endy + ((long)(starty - endy) *
                                (_ulx - endx))/(startx - endx));
                        endx = _ulx;
                    } else if (endx > _lrx) {
                        endy = (int)(endy + ((long)(starty - endy) *
                                (_lrx - endx))/(startx - endx));
                        endx = _lrx;
                    }
                }

                // If end point is out of y range, adjust to boundary.
                // Note that y increases downward
                if (starty != endy) {
                    if (endy < _uly) {
                        endx = (int)(endx + ((long)(startx - endx) *
                                (_uly - endy))/(starty - endy));
                        endy = _uly;
                    } else if (endy > _lry) {
                        endx = (int)(endx + ((long)(startx - endx) *
                                (_lry - endy))/(starty - endy));
                        endy = _lry;
                    }
                }

                // Adjust current point to lie on the boundary.
                if (startx != endx) {
                    if (startx < _ulx) {
                        starty = (int)(starty + ((long)(endy - starty) *
                                (_ulx - startx))/(endx - startx));
                        startx = _ulx;
                    } else if (startx > _lrx) {
                        starty = (int)(starty + ((long)(endy - starty) *
                                (_lrx - startx))/(endx - startx));
                        startx = _lrx;
                    }
                }
                if (starty != endy) {
                    if (starty < _uly) {
                        startx = (int)(startx + ((long)(endx - startx) *
                                (_uly - starty))/(endy - starty));
                        starty = _uly;
                    } else if (starty > _lry) {
                        startx = (int)(startx + ((long)(endx - startx) *
                                (_lry - starty))/(endy - starty));
                        starty = _lry;
                    }
                }
            }

            // Are the new points in range?
            if (endx >= _ulx && endx <= _lrx &&
                    endy >= _uly && endy <= _lry &&
                    startx >= _ulx && startx <= _lrx &&
                    starty >= _uly && starty <= _lry) {
                graphics.drawLine((int)startx, (int)starty,
                        (int)endx, (int)endy);
            }
        } else {
            // draw unconditionally.
            graphics.drawLine((int)startx, (int)starty,
                    (int)endx, (int)endy);
        }
    }

    /** Draw the axes and then plot all points.  This is synchronized
     *  to prevent multiple threads from drawing the plot at the same
     *  time.  It sets _painted true and calls notifyAll() at the end so that a
     *  thread can use <code>wait()</code> to prevent it plotting
     *  points before the axes have been first drawn.  If the second
     *  argument is true, clear the display first.
     *  This method is called by paint().  To cause it to be called you
     *  would normally call repaint(), which eventually causes paint() to
     *  be called.
     *  @param graphics The graphics context.
     *  @param clearfirst If true, clear the plot before proceeding.
     */
    protected synchronized void _drawPlot(Graphics graphics,
            boolean clearfirst) {
        // We must call PlotBox._drawPlot() before calling _drawPlotPoint
        // so that _xscale and _yscale are set.
        super._drawPlot(graphics, clearfirst);

        _showing = true;

        // Plot the points
        for (int dataset = 0; dataset < _points.size(); dataset++) {
            Vector data = (Vector)_points.elementAt(dataset);
            for (int pointnum = 0; pointnum < data.size(); pointnum++) {
                _drawPlotPoint(graphics, dataset, pointnum);
            }
        }
        _painted = true;
        notifyAll();
    }

    /** Put a mark corresponding to the specified dataset at the
     *  specified x and y position. The mark is drawn in the current
     *  color. What kind of mark is drawn depends on the _marks
     *  variable and the dataset argument. If the fourth argument is
     *  true, then check the range and plot only points that
     *  are in range.
     *  @param graphics The graphics context.
     *  @param dataset The index of the dataset.
     *  @param xpos The x position.
     *  @param ypos The y position.
     *  @param clip If true, then do not draw outside the range.
     */
    protected void _drawPoint(Graphics graphics,
            int dataset, long xpos, long ypos,
            boolean clip) {

        // FIXME: If the point is outside of range, and being drawn,
        // then it is probably a legend point.  When printing, we probably
        // want to use a line rather than a point for the legend.
        // (So that line patterns are visible).  Should that be handled here?

        // If the point is not out of range, draw it.
        if (!clip || (ypos <= _lry && ypos >= _uly &&
                xpos <= _lrx && xpos >= _ulx)) {
            int xposi = (int)xpos;
            int yposi = (int)ypos;
            switch (_marks) {
            case 0:
                // If no mark style is given, draw a filled rectangle.
                // This is used, for example, to draw the legend.
                graphics.fillRect(xposi-6, yposi-6, 6, 6);
                break;
            case 1:
                // points -- use 3-pixel ovals.
                graphics.fillOval(xposi-1, yposi-1, 3, 3);
                break;
            case 2:
                // dots
                graphics.fillOval(xposi-_radius, yposi-_radius,
                        _diameter, _diameter);
                break;
            case 3:
                // marks
                int xpoints[], ypoints[];
                // Points are only distinguished up to _MAX_MARKS data sets.
                int mark = dataset % _MAX_MARKS;
                switch (mark) {
                case 0:
                    // filled circle
                    graphics.fillOval(xposi-_radius, yposi-_radius,
                            _diameter, _diameter);
                    break;
                case 1:
                    // cross
                    graphics.drawLine(xposi-_radius, yposi-_radius,
                            xposi+_radius, yposi+_radius);
                    graphics.drawLine(xposi+_radius, yposi-_radius,
                            xposi-_radius, yposi+_radius);
                    break;
                case 2:
                    // square
                    graphics.drawRect(xposi-_radius, yposi-_radius,
                            _diameter, _diameter);
                    break;
                case 3:
                    // filled triangle
                    xpoints = new int[4];
                    ypoints = new int[4];
                    xpoints[0] = xposi; ypoints[0] = yposi-_radius;
                    xpoints[1] = xposi+_radius; ypoints[1] = yposi+_radius;
                    xpoints[2] = xposi-_radius; ypoints[2] = yposi+_radius;
                    xpoints[3] = xposi; ypoints[3] = yposi-_radius;
                    graphics.fillPolygon(xpoints, ypoints, 4);
                    break;
                case 4:
                    // diamond
                    xpoints = new int[5];
                    ypoints = new int[5];
                    xpoints[0] = xposi; ypoints[0] = yposi-_radius;
                    xpoints[1] = xposi+_radius; ypoints[1] = yposi;
                    xpoints[2] = xposi; ypoints[2] = yposi+_radius;
                    xpoints[3] = xposi-_radius; ypoints[3] = yposi;
                    xpoints[4] = xposi; ypoints[4] = yposi-_radius;
                    graphics.drawPolygon(xpoints, ypoints, 5);
                    break;
                case 5:
                    // circle
                    graphics.drawOval(xposi-_radius, yposi-_radius,
                            _diameter, _diameter);
                    break;
                case 6:
                    // plus sign
                    graphics.drawLine(xposi, yposi-_radius, xposi,
                            yposi+_radius);
                    graphics.drawLine(xposi-_radius, yposi, xposi+_radius,
                            yposi);
                    break;
                case 7:
                    // filled square
                    graphics.fillRect(xposi-_radius, yposi-_radius,
                            _diameter, _diameter);
                    break;
                case 8:
                    // triangle
                    xpoints = new int[4];
                    ypoints = new int[4];
                    xpoints[0] = xposi; ypoints[0] = yposi-_radius;
                    xpoints[1] = xposi+_radius; ypoints[1] = yposi+_radius;
                    xpoints[2] = xposi-_radius; ypoints[2] = yposi+_radius;
                    xpoints[3] = xposi; ypoints[3] = yposi-_radius;
                    graphics.drawPolygon(xpoints, ypoints, 4);
                    break;
                case 9:
                    // filled diamond
                    xpoints = new int[5];
                    ypoints = new int[5];
                    xpoints[0] = xposi; ypoints[0] = yposi-_radius;
                    xpoints[1] = xposi+_radius; ypoints[1] = yposi;
                    xpoints[2] = xposi; ypoints[2] = yposi+_radius;
                    xpoints[3] = xposi-_radius; ypoints[3] = yposi;
                    xpoints[4] = xposi; ypoints[4] = yposi-_radius;
                    graphics.fillPolygon(xpoints, ypoints, 5);
                    break;
                }
                break;
            default:
                // none
            }
        }
    }

    /** Parse a line that gives plotting information. Return true if
     *  the line is recognized.  Lines with syntax errors are ignored.
     *  @param line A command line.
     *  @return True if the line is recognized.
     */
    protected boolean _parseLine (String line) {
        boolean connected = false;
        if (_connected) connected = true;
        // parse only if the super class does not recognize the line.
        if (super._parseLine(line)) {
            // We saw a non-pxgraph file directive, so blank lines
            // no longer mean new datasets.
            _pxgraphBlankLineMode = false;
            return true;
        } else {
            // We convert the line to lower case so that the command
            // names are case insensitive
            String lcLine = new String(line.toLowerCase());
            if (lcLine.startsWith("marks:")) {
                String style = (line.substring(6)).trim();
                setMarksStyle(style);
                _pxgraphBlankLineMode = false;
                return true;
            } else if (lcLine.startsWith("numsets:")) {
                // Ignore.  No longer relevant.
                _pxgraphBlankLineMode = false;
                return true;
            } else if (lcLine.startsWith("reusedatasets:")) {
                if (lcLine.indexOf("off", 16) >= 0) {
                    _reusedatasets = false;
                } else {
                    _reusedatasets = true;
                }
                return true;
            } else if (lcLine.startsWith("dataset:")
                    || (_pxgraphBlankLineMode && lcLine.length() == 0)) {
                if (_reusedatasets && lcLine.length() > 0) {
                    String tlegend = (line.substring(8)).trim();
                    _currentdataset = -1;
                    int i;
                    for ( i = 0; i <= _maxdataset; i++) {
                        if (getLegend(i).compareTo(tlegend) == 0) {
                            _currentdataset = i;
                        }
                    }
                    if (_currentdataset != -1) {
                        return true;
                    } else {
                        _currentdataset = _maxdataset;
                    }
                }

                // new data set
                // If we have not yet seen a non-pxgraph file directive,
                // and the line is blank, then this is a new data set.
                _firstinset = true;
                _sawfirstdataset = true;
                _currentdataset++;
                if (lcLine.length() > 0) {
                    String legend = (line.substring(8)).trim();
                    if (legend != null && legend.length() > 0) {
                        addLegend(_currentdataset, legend);
                    }
                    _pxgraphBlankLineMode = false;
                }
                _maxdataset = _currentdataset;
                return true;
            } else if (lcLine.startsWith("lines:")) {
                if (lcLine.indexOf("off", 6) >= 0) {
                    setConnected(false);
                } else {
                    setConnected(true);
                }
                _pxgraphBlankLineMode = false;
                return true;
            } else if (lcLine.startsWith("impulses:")) {
                if (lcLine.indexOf("off", 9) >= 0) {
                    setImpulses(false);
                } else {
                    setImpulses(true);
                }
                _pxgraphBlankLineMode = false;
                return true;
            } else if (lcLine.startsWith("bars:")) {
                if (lcLine.indexOf("off", 5) >= 0) {
                    setBars(false);
                } else {
                    setBars(true);
                    if (! _yRangeGiven) {
                        _yBottom = 0;
                    }
                    int comma = line.indexOf(",", 5);
                    String barwidth;
                    String baroffset = null;
                    if (comma > 0) {
                        barwidth = (line.substring(5, comma)).trim();
                        baroffset = (line.substring(comma+1)).trim();
                    } else {
                        barwidth = (line.substring(5)).trim();
                    }
                    try {
                        Double bwidth = new Double(barwidth);
                        double boffset = _baroffset;
                        if (baroffset != null) {
                            boffset = (new Double(baroffset)).
                                doubleValue();
                        }
                        setBars(bwidth.doubleValue(), boffset);
                    } catch (NumberFormatException e) {
                        // ignore if format is bogus.
                    }
                }
                _pxgraphBlankLineMode = false;
                return true;
            } else if (line.startsWith("move:")) {
                // a disconnected point
                connected = false;
                // deal with 'move: 1 2' and 'move:2 2'
                line = line.substring(5, line.length()).trim();
            } else if (line.startsWith("move")) {
                // a disconnected point
                connected = false;
                // deal with 'move 1 2' and 'move2 2'
                line = line.substring(4, line.length()).trim();
            } else if (line.startsWith("draw:")) {
                // a connected point, if connect is enabled.
                line = line.substring(5, line.length()).trim();
            } else if (line.startsWith("draw")) {
                // a connected point, if connect is enabled.
                line = line.substring(4, line.length()).trim();
            }
            line = line.trim();

            // We can't use StreamTokenizer here because it can't
            // process numbers like 1E-01.
            // This code is somewhat optimized for speed, since
            // most data consists of two data points, we want
            // to handle that case as efficiently as possible.

            int fieldsplit = line.indexOf(",");
            if (fieldsplit == -1) {
                fieldsplit = line.indexOf(" ");
            }
            if (fieldsplit == -1) {
                fieldsplit = line.indexOf(" ");  // a tab
            }

            if (fieldsplit > 0) {
                String x = (line.substring(0, fieldsplit)).trim();
                String y = (line.substring(fieldsplit+1)).trim();
                // Any more separators?
                int fieldsplit2 = y.indexOf(",");
                if (fieldsplit2 == -1) {
                    fieldsplit2 = y.indexOf(" ");
                }
                if (fieldsplit2 == -1) {
                    fieldsplit2 = y.indexOf(" ");  // a tab
                }
                if (fieldsplit2 > 0) {
                    line = (y.substring(fieldsplit2+1)).trim();
                    y = (y.substring(0, fieldsplit2)).trim();
                }
                try {
                    Double xpt = new Double(x);
                    Double ypt = new Double(y);
                    if (fieldsplit2 > 0) {
                        // There was one separator after the y value, now
                        // look for another separator.
                        int fieldsplit3 = line.indexOf(",");
                        if (fieldsplit3 == -1) {
                            fieldsplit3 = line.indexOf(" ");
                        }
                        if (fieldsplit3 == -1) {
                            fieldsplit2 = line.indexOf(" ");  // a tab
                        }

                        if (fieldsplit3 > 0) {
                            // We have more numbers, assume that this is
                            // an error bar
                            String yl = (line.substring(0,
                                    fieldsplit3)).trim();
                            String yh = (line.substring(fieldsplit3+1)).trim();
                            Double yLowEB = new Double(yl);
                            Double yHighEB = new Double(yh);
                            connected = _addLegendIfNecessary(connected);
                            addPointWithErrorBars(_currentdataset,
                                    xpt.doubleValue(),
                                    ypt.doubleValue(),
                                    yLowEB.doubleValue(),
                                    yHighEB.doubleValue(),
                                    connected);
                            return true;
                        } else {
                            // It is unlikely that we have a fieldsplit2 >0
                            // but not fieldsplit3 >0, but just in case:

                            connected = _addLegendIfNecessary(connected);
                            addPoint(_currentdataset, xpt.doubleValue(),
                                    ypt.doubleValue(), connected);
                            return true;
                        }
                    } else {
                        // There were no more fields, so this is
                        // a regular pt.
                        connected = _addLegendIfNecessary(connected);
                        addPoint(_currentdataset, xpt.doubleValue(),
                                ypt.doubleValue(), connected);
                        return true;
                    }
                } catch (NumberFormatException e) {
                    // ignore if format is bogus.
                }
            }
        }
        return false;
    }

    /** Write plot information to the specified output stream.
     *  Derived classes should override this method to first call
     *  the parent class method, then add whatever additional information
     *  they wish to add to the stream.
     *  @param output A buffered print writer.
     */
    protected void _write(PrintWriter output) {
        super._write(output);

        switch(_marks) {
        case 1:
            output.println("Marks: points");
        case 2:
            output.println("Marks: dots");
        case 3:
            output.println("Marks: various");
        }
        // NOTE: NumSets is obsolete, so we don't write it.
        if (_reusedatasets) output.println("ReuseDatasets: on");
        if (!_connected) output.println("Lines: off");
        if (_impulses) output.println("Impulses: on");
        if (_bars) output.println("Bars: " + _barwidth + ", " + _baroffset);

        for (int dataset = 0; dataset < _points.size(); dataset++) {
            String legend = getLegend(dataset);
            if (legend != null) {
                output.println("DataSet: " + getLegend(dataset));
            } else {
                output.println("DataSet:");
            }
            Vector pts = (Vector)_points.elementAt(dataset);
            for (int pointnum = 0; pointnum < pts.size(); pointnum++) {
                PlotPoint pt = (PlotPoint)pts.elementAt(pointnum);
                if (!pt.connected) output.print("move: ");
                if (pt.errorBar) {
                    output.println(pt.x + ", " + pt.y + ", "
                            + pt.yLowEB + ", " + pt.yHighEB);
                } else {
                    output.println(pt.x + ", " + pt.y);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // The current dataset.
    protected int _currentdataset = -1;

    // A vector of datasets.
    protected Vector _points = new Vector();

    // An indicator of the marks style.  See _parseLine method for
    // interpretation.
    protected int _marks;
    // Indicate that painting is complete.
    protected boolean _painted = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /* Add a legend if necessary, return the value of the connected flag.
     */
    private boolean _addLegendIfNecessary(boolean connected) {
        if (! _sawfirstdataset  || _currentdataset < 0) {
            // We did not set a DataSet line, but
            // we did get called with -<digit> args
            _sawfirstdataset = true;
            _currentdataset++;
        }
        if (getLegend(_currentdataset) == null) {
            // We did not see a "DataSet" string yet,
            // nor did we call addLegend().
            _firstinset = true;
            _sawfirstdataset = true;
            addLegend(_currentdataset,
                    new String("Set "+ _currentdataset));
        }
        if (_firstinset) {
            connected = false;
            _firstinset = false;
        }
        return connected;
    }

    /* In the specified data set, add the specified x, y point to the
     * plot.  Data set indices begin with zero.  If the dataset
     * argument is less than zero, throw an IllegalArgumentException
     * (a runtime exception).  If it refers to a data set that does
     * not exist, create the data set.  The fourth argument indicates
     * whether the point should be connected by a line to the previous
     * point.  The point is drawn on the screen only if is visible.
     * Otherwise, it is drawn the next time paint() is called.
     */
    private synchronized void _addPoint(
            int dataset, double x, double y, double yLowEB, double yHighEB,
            boolean connected, boolean errorBar) {
        _checkDatasetIndex(dataset);

        // For auto-ranging, keep track of min and max.
        if (x < _xBottom) _xBottom = x;
        if (x > _xTop) _xTop = x;
        if (y < _yBottom) _yBottom = y;
        if (y > _yTop) _yTop = y;

        // FIXME: Ignoring sweeps for now.
        PlotPoint pt = new PlotPoint();
        pt.x = x;
        pt.y = y;
        pt.connected = connected;

        if (errorBar) {
            if (yLowEB < _yBottom) _yBottom = yLowEB;
            if (yLowEB > _yTop) _yTop = yLowEB;
            if (yHighEB < _yBottom) _yBottom = yHighEB;
            if (yHighEB > _yTop) _yTop = yHighEB;
            pt.yLowEB = yLowEB;
            pt.yHighEB = yHighEB;
            pt.errorBar = true;
        }

        Vector pts = (Vector)_points.elementAt(dataset);
        pts.addElement(pt);
        if (_pointsPersistence > 0) {
            if (pts.size() > _pointsPersistence)
                erasePoint(dataset, 0);
        }
        // Draw the point on the screen only if the plot is showing.
        if (_showing) {
            _drawPlotPoint(getGraphics(), dataset, pts.size() - 1);
        }
    }

    /* Draw the specified point and associated lines, if any.
     * Note that paint() should be called before
     * calling this method so that it calls _drawPlot(), which sets
     * _xscale and _yscale. Note that this does not check the dataset
     * index.  It is up to the caller to do that.
     */
    private synchronized void _drawPlotPoint(Graphics graphics,
            int dataset, int index) {
        // Set the color
        if (_pointsPersistence > 0) {
            // To allow erasing to work by just redrawing the points.
            graphics.setXORMode(_background);
        }
        if (_usecolor) {
            int color = dataset % _colors.length;
            graphics.setColor(_colors[color]);
        } else {
            graphics.setColor(_foreground);
        }

        Vector pts = (Vector)_points.elementAt(dataset);
        PlotPoint pt = (PlotPoint)pts.elementAt(index);
        // Use long here because these numbers can be quite large
        // (when we are zoomed out a lot).
        long ypos = _lry - (long)((pt.y - _yMin) * _yscale);
        long xpos = _ulx + (long)((pt.x - _xMin) * _xscale);

        // Draw the line to the previous point.
        long prevx = ((Long)_prevx.elementAt(dataset)).longValue();
        long prevy = ((Long)_prevy.elementAt(dataset)).longValue();
        if (pt.connected) _drawLine(graphics, dataset, xpos, ypos,
                prevx, prevy, true);

        // Save the current point as the "previous" point for future
        // line drawing.
        _prevx.setElementAt(new Long(xpos),dataset);
        _prevy.setElementAt(new Long(ypos),dataset);

        // Draw the point & associated decorations, if appropriate.
        if (_marks != 0) _drawPoint(graphics, dataset, xpos, ypos, true);
        if (_impulses) _drawImpulse(graphics, xpos, ypos, true);
        if (_bars) _drawBar(graphics, dataset, xpos, ypos, true);
        if (pt.errorBar)
            _drawErrorBar(graphics, dataset, xpos,
                    _lry - (long)((pt.yLowEB - _yMin) * _yscale),
                    _lry - (long)((pt.yHighEB - _yMin) * _yscale), true);

        // Restore the color, in case the box gets redrawn.
        graphics.setColor(_foreground);
        if (_pointsPersistence > 0) {
            // Restore paint mode in case axes get redrawn.
            graphics.setPaintMode();
        }
    }

    /* Erase the point at the given index in the given dataset.  If
     * lines are being drawn, also erase the line to the next points
     * (note: not to the previous point).
     * Note that paint() should be called before
     * calling this method so that it calls _drawPlot(), which sets
     * _xscale and _yscale.  It should be adequate to check isShowing()
     * before calling this.
     */
    private synchronized void _erasePoint(Graphics graphics,
            int dataset, int index) {
        // Set the color
        if (_pointsPersistence > 0) {
            // To allow erasing to work by just redrawing the points.
            graphics.setXORMode(_background);
        }
        if (_usecolor) {
            int color = dataset % _colors.length;
            graphics.setColor(_colors[color]);
        } else {
            graphics.setColor(_foreground);
        }

        Vector pts = (Vector)_points.elementAt(dataset);
        PlotPoint pt = (PlotPoint)pts.elementAt(index);
        long ypos = _lry - (long) ((pt.y - _yMin) * _yscale);
        long xpos = _ulx + (long) ((pt.x - _xMin) * _xscale);

        // Erase line to the next point, if appropriate.
        if (index < pts.size() - 1) {
            PlotPoint nextp = (PlotPoint)pts.elementAt(index+1);
            int nextx = _ulx + (int) ((nextp.x - _xMin) * _xscale);
            int nexty = _lry - (int) ((nextp.y - _yMin) * _yscale);
            // NOTE: I have no idea why I have to give this point backwards.
            if (nextp.connected) _drawLine(graphics, dataset,
                    nextx, nexty,  xpos, ypos, true);
            nextp.connected = false;
        }

        // Draw the point & associated lines, if appropriate.
        if (_marks != 0) _drawPoint(graphics, dataset, xpos, ypos, true);
        if (_impulses) _drawImpulse(graphics, xpos, ypos, true);
        if (_bars) _drawBar(graphics, dataset, xpos, ypos, true);
        if (pt.errorBar)
            _drawErrorBar(graphics, dataset, xpos,
                    _lry - (long)((pt.yLowEB - _yMin) * _yscale),
                    _lry - (long)((pt.yHighEB - _yMin) * _yscale), true);

        // Restore the color, in case the box gets redrawn.
        graphics.setColor(_foreground);
        if (_pointsPersistence > 0) {
            // Restore paint mode in case axes get redrawn.
            graphics.setPaintMode();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _pointsPersistence = 0;
    private int _sweepsPersistence = 0;
    private boolean _bars = false;
    private double _barwidth = 0.5;
    private double _baroffset = 0.05;
    private boolean _connected = true;
    private boolean _impulses = false;

    // The highest data set used.
    private int _maxdataset = -1;

    // True if we saw 'reusedatasets: on' in the file.
    private boolean _reusedatasets = false;

    // Is this the first datapoint in a set?
    private boolean _firstinset = true;
    // Have we seen a DataSet line in the current data file?
    private boolean _sawfirstdataset = false;

    // Give both radius and diameter of a point for efficiency.
    private int _radius = 3;
    private int _diameter = 6;

    // If _pxgraphBlankLineMode is true, then we have not yet seen
    // a non-pxgraph file directive, so blank lines mean new datasets.
    private boolean _pxgraphBlankLineMode = true;

    // Check the osarch and use the appropriate endian
    private static final int _NATIVE_ENDIAN = 0;
    // Data is in big-endian
    private static final int _BIG_ENDIAN = 1;
    // Data is in little-endian
    private static final int _LITTLE_ENDIAN = 2;

    // Format to read data in.
    private int _endian = _NATIVE_ENDIAN;

    // Information about the previously plotted point.
    private Vector _prevx = new Vector(), _prevy = new Vector();

    // Half of the length of the error bar horizontal leg length;
    private static final int _ERRORBAR_LEG_LENGTH = 5;

    // Maximum number of different marks
    // NOTE: There are 11 colors in the base class.  Combined with 10
    // marks, that makes 110 unique signal identities.
    private static final int _MAX_MARKS = 10;

    // Flag indicating validity of _xBottom, _xTop, _yBottom, and _yTop.
    private boolean _xyInvalid = false;

    // Last filename seen in command-line arguments.
    private String _filename = null;

    // Set by _drawPlot(), and reset by clear()
    private boolean _showing = false;

// NOTE: This strategy fails due to a bug in jdk 1.1
//     // Support for Painter class which draws plot in the background.
//     private Painter _painter;
//     // FIXME: This has to be friendly or Netscape fails
//     boolean _painting = false;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

// NOTE: This strategy fails due to a bug in jdk 1.1
// Nothing drawn to the graphics object in this other thread ever appears.
//     // This class spawns a thread to load the sound file in the background.
//     // NOTE: This class has to be public or Netscape 4.0 fails.
//     public class Painter extends Thread {
//         public Painter(Graphics graphics) {
//             _graphics = graphics;
//         }
//         public void run() {
//             _painting = true;
// // FIXME: Well, damn... Can't draw to the graphics object from another
// // thread, apparently!!!  This never appears!!!!
// _graphics.drawRect(10, 10, 100, 100);
//             _drawPlot(_graphics, true);
//             _painting = false;
//         }
//         private Graphics _graphics;
//     }
}
