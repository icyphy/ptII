/* A logic analyzer.

Copyright (c) 1997-1998 The Regents of the University of California.
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

import java.awt.*;
import java.io.*;
import java.util.*;
import java.net.*;

//////////////////////////////////////////////////////////////////////////
//// LogicAnalyzer
/** 

@author Lukito Muliadi
@version $Id$
@see classname
@see full-classname
*/
public class LogicAnalyzer extends PlotBox {

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
    public synchronized void addPoint(int dataset, double time, int logicValue) {
        double yVal = 0;
        if (logicValue == ONE) {
            yVal = -dataset + _heightRatio*0.5;
        } else if (logicValue == ZERO) {
            yVal = -dataset - _heightRatio*0.5;
        }
        
        System.out.println("Set="+dataset + " X-value=" + time + " Y-value=" + yVal);
        _addPoint(dataset, time, yVal);
    }

    /** Clear the plot of all data points.  If the argument is true, then
     *  reset all parameters to their initial conditions, including
     *  the persistence, plotting format, and axes formats.
     *  For the change to take effect, you must call repaint().
     *  @param format If true, clear the format controls as well.
     */
    public synchronized void clear (boolean format) {
        super.clear(format);
        _currentdataset = -1;
        int size = _points.size();
        _points = new Vector();
        _prevx = new Vector();
        _prevy = new Vector();
        _painted = false;
        _maxdataset = -1;
        _firstinset = true;
        _sawfirstdataset = false;
        _pxgraphBlankLineMode = true;
        _endian = _NATIVE_ENDIAN;
        _xyInvalid = false;
        _filename = null;
        _showing = false;

        if (format) {
            // Reset format controls
            _reusedatasets = false;
        }
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
            for (int dataset = 0; dataset < _points.size(); dataset++) {
                Vector points = (Vector)_points.elementAt(dataset);
                for (int index = 0; index < points.size(); index++) {
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
        for (int i = 0; i<numsets; i++) {
            _points.addElement(new Vector());
            _prevx.addElement(new Long(0));
            _prevy.addElement(new Long(0));
        }
    }

    /** Override the base class to not clear the component first.
     *  @param graphics The graphics context.
     */
    public void update(Graphics g) {
        paint(g);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Logic values.
     */
    public static final int ZERO = 0;
    public static final int ONE = 1;
    public static final int DONTKNOW = 2;
    public static final int HIGHZ = 3;

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
            // draw the tick
            String legend = getLegend(dataset);
            if (legend == null) {
                legend = new String("Data "+dataset);
            }
            addYTick(legend,-dataset);
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
            // points -- use 3-pixel ovals.
            //graphics.fillOval(xposi-1, yposi-1, 3, 3);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // The current dataset.
    protected int _currentdataset = -1;

    // A vector of datasets.
    protected Vector _points = new Vector();

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
    private synchronized void _addPoint(int dataset, double x, double y) {
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

        Vector pts = (Vector)_points.elementAt(dataset);
        pts.addElement(pt);

        // Draw the point on the screen only if the plot is showing.
        if (_showing) {
            _drawPlotPoint(getGraphics(), dataset, pts.size() - 1);
        }
    }

    /* We don't wanna have a legend in the upper right hand corner.
     * 
     */
    protected int _drawLegend(Graphics graphics, int urx, int ury) {
        // no legend, so the width is equal to 0
        return 0;
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
        
        long prevx = 0;
        long prevy = 0;
        if (index>0) {
            PlotPoint prev = (PlotPoint)pts.elementAt(index-1);
            prevy = _lry - (long)((prev.y - _yMin) * _yscale);
            prevx = _ulx + (long)((prev.x - _xMin) * _xscale);
            // draw lines to the previous point.
            // a horizontal line
            _drawLine(graphics, dataset, xpos, prevy,prevx, prevy, true);
            // a vertical line
            _drawLine(graphics, dataset, xpos, ypos,xpos, prevy, true);
        } else {
            
        }
        
        /*       
        // Use long here because these numbers can be quite large
        // (when we are zoomed out a lot).
        long ypos = _lry - (long)((pt.y - _yMin) * _yscale);
        long xpos = _ulx + (long)((pt.x - _xMin) * _xscale);

        // Draw lines to the previous point.
        long prevx = ((Long)_prevx.elementAt(dataset)).longValue();
        long prevy = ((Long)_prevy.elementAt(dataset)).longValue();
        // a horizontal line
        _drawLine(graphics, dataset, xpos, prevy,prevx, prevy, true);
        // a vertical line
        // _drawLine(graphics, dataset, xpos, ypos,xpos, prevy, true);

        // Save the current point as the "previous" point for future
        // line drawing.
        _prevx.setElementAt(new Long(xpos), dataset);
        _prevy.setElementAt(new Long(ypos), dataset);
        */

        // Draw the point & associated decorations, if appropriate.
        _drawPoint(graphics, dataset, xpos, ypos, true);

        // Restore the color, in case the box gets redrawn.
        graphics.setColor(_foreground);
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
        //if (_marks != 0) _drawPoint(graphics, dataset, xpos, ypos, true);
        //if (_impulses) _drawImpulse(graphics, xpos, ypos, true);
        //if (_bars) _drawBar(graphics, dataset, xpos, ypos, true);
        //if (pt.errorBar)
        //    _drawErrorBar(graphics, dataset, xpos,
        //            _lry - (long)((pt.yLowEB - _yMin) * _yscale),
        //            _lry - (long)((pt.yHighEB - _yMin) * _yscale), true);

        // Restore the color, in case the box gets redrawn.
        graphics.setColor(_foreground);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////


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

    private double _heightRatio = 0.9;

}
