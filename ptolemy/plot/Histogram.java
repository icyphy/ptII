/* A histogram plotter.

@Copyright (c) 1997-2000 The Regents of the University of California.
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
@ProposedRating Yellow (cxh@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/
package ptolemy.plot;

import java.awt.Graphics;
import java.io.*;
import java.util.*;
import javax.swing.SwingUtilities;

//////////////////////////////////////////////////////////////////////////
//// Histogram
/**
 * A histogram plotter.  The plot can be configured and data can
 * be provided either through a file with commands or through direct
 * invocation of the public methods of the class.  To read a file or a
 * URL, use the read() method.
 * <p>
 * When calling the public methods, in most cases the changes will not
 * be visible until paint() has been called.  To request that this
 * be done, call repaint().  One exception is addPoint(), which
 * makes the affect of the new point visible immediately if the
 * plot is visible on the screen.
 * <p>
 * The ASCII format for the file
 * file contains any number commands,
 * one per line.  Unrecognized commands and commands with syntax
 * errors are ignored.  Comments are denoted by a line starting with a
 * pound sign "#".  The recognized commands include those supported by
 * the base class, plus a few more.  The commands are case
 * insensitive, but are usually capitalized.  The number of data sets
 * to be plotted does not need to be specified.  Data sets are added as needed.
 * Each dataset is identified with a color (see the base class).
 * <P>
 * The appearance of the histogram can be altered by the following commands:
 * <pre>
 * Bars: <i>width</i>
 * Bars: <i>width, offset</i>
 * </pre>
 * The <i>width</i> is a real number specifying the width of the bars
 * as a fraction of the bin width.  It usually has a value less than
 * or equal to one,
 * and defaults to 0.5.  The <i>offset</i> is a real number
 * specifying how much the bar of the <i>i < /i><sup>th</sup> data set
 * is offset from the previous one.  This allows bars to "peek out"
 * from behind the ones in front.  It defaults to 0.15.
 * Note that the frontmost data set will be the first one.
 * <p>
 * The width of each bin of the histogram can be specified using:
 * <pre>
 * BinWidth: <i>width</i>
 * </pre>
 * This is given in whatever units the data has.
 * By default, each bin is centered at <i>x</i> = <i>nw</i>,
 * where <i>w</i> is the width of the bin and <i>n</i> is an integer.
 * That bin represents values in the range (<i>x - w/2, x + w/2</i>).
 * The alignment of the bins can be changed with the following command:
 * <pre>
 * BinOffset: <i>offset</i>
 * </pre>
 * If this method is used with argument <i>o</i>, then each bin is
 * centered at <i>x = nw + o</i>, and represents values in the range
 * (<i>x - w/2 + o, x + w/2 + o</i>).  So for example, if <i>o = w/2</i>,
 * then each bin represents values from <i>nw</i> to
 * (<i>n</i> + 1)<i>w</i>) for some integer <i>n</i>.
 * The default offset is 0.5, half the default bin width.
 * <p>
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
 * New datasets are plotted <i>behind</i> the previous ones.
 * The data itself is given by a sequence of numbers, one per line.
 * The numbers are specified as
 * strings that can be parsed by the Double parser in Java.
 * It is also possible to specify the numbers using all the formats
 * accepted by the Plot class, so that the same data may be plotted by
 * both classes.  The <i>x</i> data is ignored, and only the <i>y</i>
 * data is used to calculate the histogram.
 *
 * @author Edward A. Lee
 * @version $Id$
 */
public class Histogram extends PlotBox {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a legend (displayed at the upper right) for the specified
     *  data set with the specified string.  Short strings generally
     *  fit better than long strings.
     *  @param dataset The dataset index.
     *  @param legend The label for the dataset.
     */
    public void addLegend(int dataset, String legend) {
        _checkDatasetIndex(dataset);
        super.addLegend(dataset, legend);
    }

    /** In the specified data set, add the specified value to the
     *  histogram.  Data set indices begin with zero.  If the data set
     *  does not exist, create it.
     *  The new point will visibly alter the histogram if the plot is visible
     *  on the screen.  Otherwise, it will be drawn the next time the histogram
     *  is drawn on the screen.
     *  @param dataset The data set index.
     *  @param value The new value.
     */
    public synchronized void addPoint(int dataset, double value) {
        _checkDatasetIndex(dataset);

        // Calculate the bin number.
        int bin = (int)(Math.round((value-_binOffset)/_binWidth));
        Integer binobj = new Integer(bin);

        // Add to the appropriate bin
        Hashtable bins = (Hashtable)_histogram.elementAt(dataset);
        int count;
        if (bins.containsKey(binobj)) {
            // increase the count
            count = 1 + ((Integer)bins.get(binobj)).intValue();
            bins.put(binobj, new Integer(count));
        } else {
            // start a new entry.
            count = 1;
            bins.put(binobj, new Integer(count));
        }

        // For auto-ranging, keep track of min and max.
        double x = bin*_binWidth + _binOffset;
        if (x < _xBottom) _xBottom = x;
        double xtop = x + _binWidth/2.0;
        if (xtop > _xTop) _xTop = xtop;
        if ((double)count > _yTop) _yTop = (double)count;
        _yBottom = 0.0;
        Vector pts = (Vector)_points.elementAt(dataset);
        pts.addElement(new Double(value));

        // Draw the point on the screen only if the plot is showing.
        if (_showing) {
            // In swing, updates to showing graphics must be done in the
            // event thread, not here.  Thus, we have to queue the request.
            final int pendingDataset = dataset;
            final int pendingBin = bin;
            final int pendingCount = count;
            Runnable doPlotPoint = new Runnable() {
                public void run() {
                    _drawPlotPoint(getGraphics(), pendingDataset, pendingBin,
                           pendingCount);
                }
            };
            try {
                // Have to use invokeAndWait() here, not invokeLater()
                // because the "final" variables above, the semantics
                // of which are nowhere specified, do not seem to remain
                // constant if this is invoked later.
                SwingUtilities.invokeAndWait(doPlotPoint);
            } catch (Exception ex) {
                // Lots of useless exceptions get invoked here.
                System.err.println(ex.toString());
            }
        }
    }

    /** In the specified data set, add the specified y value to the
     *  histogram.  The x value and the <i>connected</i> arguments are
     *  ignored.  Data set indices begin with zero.  If the data set
     *  does not exist, create it.
     *  @param dataset The data set index.
     *  @param x Ignored.
     *  @param y The Y position of the new point.
     *  @param connected Ignored
     */
    public synchronized void addPoint(int dataset, double x, double y,
            boolean connected) {
        addPoint(dataset, y);
    }

    /** Clear the plot of all data points.  If the argument is true, then
     *  reset all parameters to their initial conditions, including
     *  the persistence, plotting format, and axes formats.
     *  For the change to take effect, you must call repaint().
     *  @param format If true, clear the format controls as well.
     */
    public synchronized void clear(boolean format) {
        super.clear(format);
        _currentdataset = -1;
        _points = new Vector();
        _histogram = new Vector();
        _filename = null;
        _showing = false;

        if (format) {
            // Reset format controls
            _barwidth = 0.5;
            _baroffset = 0.15;
            _binWidth = 1.0;
            _binOffset = 0.5;
        }
    }

    /** Create a sample plot.
     */
    public void samplePlot() {

        // Create a sample plot.
        clear(true);

        setTitle("Sample histogram");
        setXLabel("values");
        setYLabel("count");

        Random random = new Random();
        for (int i = 0; i <= 1000; i++) {
            this.addPoint(0, 5.0 * Math.cos(Math.PI * ((double)i)/500.0));
            this.addPoint(1, 10.0 * random.nextDouble() - 5.0);
            this.addPoint(2, 2.0 * random.nextGaussian());
        }
        this.repaint();
    }

    /** Set the width and offset of the bars.  Both are specified
     *  as a fraction of a bin width.  The offset is the amount by which the
     *  i < sup>th</sup> data set is shifted to the right, so that it
     *  peeks out from behind the earlier data sets.
     *  @param width The width of the bars.
     *  @param offset The offset per data set.
     */
    public void setBars(double width, double offset) {
        _barwidth = width;
        _baroffset = offset;
    }

    /** Set the offset of the bins, in whatever units the data are given.
     *  Without calling this, each bin is centered at <i>x</i> = <i>nw</i>,
     *  where <i>w</i> is the width of the bin and <i>n</i> is an integer.
     *  That bin represents values in the range (<i>x - w/2, x + w/2</i>).
     *  If this method is called with argument <i>o</i>, then each bin is
     *  centered at <i>x = nw + o</i>, and represents values in the range
     *  (<i>x - w/2 + o, x + w/2 + o</i>).  So for example, if <i>o = w/2</i>,
     *  then each bin represents values from <i>nw</i> to
     *  (<i>n</i> + 1)<i>w</i>) for some integer <i>n</i>.
     *  @param offset The bin offset.
     */
    public void setBinOffset(double offset) {
        _binOffset = offset;
    }

    /** Set the width of the bins, in whatever units the data are given.
     *  @param width The width of the bins.
     */
    public void setBinWidth(double width) {
        _binWidth = width;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////


    /** Check the argument to ensure that it is a valid data set index.
     *  If it is less than zero, throw an IllegalArgumentException (which
     *  is a runtime exception).  If it does not refer to an existing
     *  data set, then fill out the _points and _histogram
     *  Vectors so that they refer
     *  to all existing data sets.
     *  @param dataset The data set index.
     */
    protected void _checkDatasetIndex(int dataset) {
        if (dataset < 0) {
            throw new IllegalArgumentException("Plot._checkDatasetIndex: Cannot"
                    + " give a negative number for the data set index.");
        }
        while (dataset >= _points.size()) {
            _points.addElement(new Vector());
            _histogram.addElement(new Hashtable());
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
    protected void _drawBar(Graphics graphics, int dataset,
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
            int barlx = (int)(xpos - _barwidth *_binWidth * _xscale/2 +
                    dataset * _baroffset *_binWidth * _xscale);
            // right x position of bar
            int barrx = (int)(barlx + _barwidth *_binWidth * _xscale);
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

    /** Draw the axes and then plot the histogram. If the second
     *  argument is true, clear the display first.
     *  This method is called by paint().  To cause it to be called you
     *  would normally call repaint(), which eventually causes paint() to
     *  be called.
     *  @param graphics The graphics context.
     *  @param clearfirst If true, clear the plot before proceeding.
     */
    protected void _drawPlot(Graphics graphics,
            boolean clearfirst) {
        // We must call PlotBox._drawPlot() before calling _drawPlotPoint
        // so that _xscale and _yscale are set.
        super._drawPlot(graphics, clearfirst);

        _showing = true;

        // Plot the histograms in reverse order so that the first colors
        // appear on top.
        for (int dataset = _points.size() - 1; dataset >= 0 ; dataset--) {
            Hashtable data = (Hashtable)_histogram.elementAt(dataset);
            Enumeration keys = data.keys();
            while (keys.hasMoreElements()) {
                Integer bin = (Integer)keys.nextElement();
                Integer count = (Integer)data.get(bin);
                _drawPlotPoint(graphics, dataset,
                        bin.intValue(), count.intValue());
            }
        }
    }

    /** Parse a line that gives plotting information. Return true if
     *  the line is recognized.  Lines with syntax errors are ignored.
     *  @param line A command line.
     *  @return True if the line is recognized.
     */
    protected boolean _parseLine(String line) {
        // parse only if the super class does not recognize the line.
        if (super._parseLine(line)) {
            return true;
        } else {
            // We convert the line to lower case so that the command
            // names are case insensitive
            String lcLine = new String(line.toLowerCase());
            if (lcLine.startsWith("dataset:")) {
                // new data set
                _currentdataset++;
                if (lcLine.length() > 0) {
                    String legend = (line.substring(8)).trim();
                    if (legend != null && legend.length() > 0) {
                        addLegend(_currentdataset, legend);
                    }
                }
                return true;
            } else if (lcLine.startsWith("bars:") || 
                    lcLine.startsWith("bargraph:")) {
                // The PlotML code uses barGraph, but the older style
                // uses bars 
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
                        boffset = (new Double(baroffset)).doubleValue();
                    }
                    setBars(bwidth.doubleValue(), boffset);
                } catch (NumberFormatException e) {
                    // ignore if format is bogus.
                }
                return true;
            } else if (lcLine.startsWith("binwidth:")) {
                String binwidth = (line.substring(9)).trim();
                try {
                    Double bwidth = new Double(binwidth);
                    setBinWidth(bwidth.doubleValue());
                } catch (NumberFormatException e) {
                    // ignore if format is bogus.
                }
                return true;
            } else if (lcLine.startsWith("binoffset:")) {
                String binoffset = (line.substring(10)).trim();
                try {
                    Double boffset = new Double(binoffset);
                    setBinOffset(boffset.doubleValue());
                } catch (NumberFormatException e) {
                    // ignore if format is bogus.
                }
                return true;
            } else if (lcLine.startsWith("numsets:")) {
                // Obsolete field... ignore.
                return true;
            } else if (line.startsWith("move:")) {
                // deal with 'move: 1 2' and 'move:2 2'
                line = line.substring(5, line.length()).trim();
            } else if (line.startsWith("move")) {
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

            // Handle Plot formats
            int fieldsplit = line.indexOf(",");
            if (fieldsplit == -1) {
                fieldsplit = line.indexOf(" ");
            }
            if (fieldsplit == -1) {
                fieldsplit = line.indexOf("\t");  // a tab
            }

            if (fieldsplit == -1) {
                // Have just one number per line
                try {
                    Double xpt = new Double(line);
                    addPoint(_currentdataset, xpt.doubleValue());
                    return true;
                } catch (NumberFormatException e) {
                    // ignore if format is bogus.
                }
            } else {
                String y = (line.substring(fieldsplit+1)).trim();
                try {
                    Double ypt = new Double(y);
                    addPoint(_currentdataset, ypt.doubleValue());
                    return true;
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

        output.println(
                "<barGraph width=\"" + _barwidth
                + "\" offset=\"" + _baroffset + "\"/>");

        output.println("<bin width=\"" + _binWidth
                + " offset=\"" + _binOffset + "\">");

        for (int dataset = 0; dataset < _points.size(); dataset++) {
            // Write the dataset directive
            String legend = getLegend(dataset);
            if (legend != null) {
                output.println("<dataset name=\"" + legend + "\">");
            } else {
                output.println("<dataset>");
            }
            // Write the data
            Vector pts = (Vector)_points.elementAt(dataset);
            for (int pointnum = 0; pointnum < pts.size(); pointnum++) {
                Double pt = (Double)pts.elementAt(pointnum);
                output.println("<p y=\"" + pt.doubleValue() + "\"/");
            }
            output.println("</dataset>");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** @serial The current dataset. */
    protected int _currentdataset = -1;

    /** @serial A vector of datasets. */
    protected Vector _points = new Vector();

    /** @serial A vector of histogram data. */
    protected Vector _histogram = new Vector();

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /* Draw the specified histogram bar.
     * Note that paint() should be called before
     * calling this method so that it calls _drawPlot(), which sets
     * _xscale and _yscale. Note that this does not check the dataset
     * index.  It is up to the caller to do that.
     */
    private void _drawPlotPoint(Graphics graphics,
            int dataset, int bin, int count) {
        // Set the color
        if (_usecolor) {
            int color = dataset % _colors.length;
            graphics.setColor(_colors[color]);
        } else {
            graphics.setColor(_foreground);
        }

        double y = (double)count;
        double x = _binWidth*bin + _binOffset;

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

        // Use long here because these numbers can be quite large
        // (when we are zoomed out a lot).
        long ypos = _lry - (long)((y - _yMin) * _yscale);
        long xpos = _ulx + (long)((x - _xMin) * _xscale);

        _drawBar(graphics, dataset, xpos, ypos, true);

        // Restore the color, in case the box gets redrawn.
        graphics.setColor(_foreground);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** @serial The width of a bar. */
    private double _barwidth = 0.5;

    /** @serial The offset between bars. */
    private double _baroffset = 0.15;

    /** @serial The width of a bin. */
    private double _binWidth = 1.0;

    /** @serial The offset between bins. */
    private double _binOffset = 0.5;

    /** @serial Last filename seen in command-line arguments. */
    private String _filename = null;

    /** @serial  Set by _drawPlot(), and reset by clear(). */
    private boolean _showing = false;
}
