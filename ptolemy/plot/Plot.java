/* A signal plotter.

@Author: Edward A. Lee
@Version: $Id$

@Copyright (c) 1997 The Regents of the University of California.
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
/* Confuses metrowerks java: package plot; */

// FIXME: To do
//   - support for oscilloscope-like plots (where x axis wraps around).
//   - crosshair cursor and zooming in.
//   - connected lines and steps
//   - support error bars (ydelta, or ylow yhigh) (adjust yrange to fit error bars)
//   - support impulses as a plot option
//   - support bars as a plot option
//   - support zooming in and out
//   - cubic spline interpolation
//   - log scale axes

// NOTE: There are quite a few subjective spacing parameters, all given, unfortunately,
// in pixels.  This means that as resolutions get better, this program may need to be
// adjusted.

import java.awt.*;
import java.util.*;
import java.applet.Applet;

//////////////////////////////////////////////////////////////////////////
//// Plot
/** 
 * A flexible signal plotter.  The plot can be configured and data can be
 * provided either through a file with commands or
 * through direct invocation of the public methods of the class.
 * If a file is used, the file can be given as a URL through the
 * applet parameter called "dataurl". The file contains any number
 * commands, one per line.  Unrecognized commands and commands with
 * syntax errors are ignored.  Comments are denoted by a line starting
 * with a pound sign "#".  The recognized commands include those
 * supported by the base class, plus a few more.
 * The following command defines the number of data sets to be plotted.
 * <pre>
 * NumSets: <i>positiveInteger</i>
 * <pre>
 * If data is provided for more data sets than this number, those
 * data are ignored.  Each dataset can be optionally identified with
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
 * Using no marks is useful only when lines connect the points in a plot,
 * which is done by default.  To disable connecting lines, use:
 * <pre>
 * Lines: off
 * </pre>
 * To specify data to be plotted, start a data set with the following command:
 * <pre>
 * DataSet: <i>string</i>
 * <pre>
 * Here, <i>string</i> is a label that will appear in the legend.
 * It is not necessary to enclose the string in quotation marks.
 * The data itself is given by a sequence of commands with one of the
 * following forms:
 * <pre>
 * <i>x</i>,<i>y</i>
 * draw: <i>x</i>,<i>y</i>
 * move: <i>x</i>,<i>y</i>
 * </pre>
 * The "draw" command is optional, so the first two forms are equivalent.
 * The "move" command causes a break in connected points, if lines are
 * being drawn between points. The numbers <i>x</i> and <i>y</i> are arbitrary numbers
 * as supported by the Double parser in Java.
 */
public class Plot extends PlotBox {

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////
   
    /**
     * In the specified data set, add the specified x,y point to the plot.
     * Data set indices begin with zero.  If the dataset argument is out of
     * range, ignore.  The number of data sets is given by calling *setNumSets()*.
     * The fourth argument indicates whether the point should be connected by a line
     * to the previous point.
     */
    public synchronized void addPoint(int dataset, double x, double y, boolean connected) {
        if (dataset >= numsets || dataset < 0) return;
        
        // For auto-ranging, keep track of min and max.
        if (x < xBottom) xBottom = x;
        if (x > xTop) xTop = x;
        if (y < yBottom) yBottom = y;
        if (y > yTop) yTop = y;

        // FIXME: Ignoring sweeps for now.
        PlotPoint pt = new PlotPoint();
        pt.x = x;
        pt.y = y;
        pt.connected = connected;
        Vector pts = points[dataset];
        pts.addElement(pt);
        if (_pointsPersistence > 0) {
            if (pts.size() > _pointsPersistence) erasePoint(dataset,0);
        }
        _drawPlotPoint(dataset, pt);
    }
    
    /**
     * Draw the axes and then plot all points.  This is synchronized to prevent
     * multiple threads from drawing the plot at the same time.  It calls
     * <code>notify()</code> at the end so that a thread can use <code>wait()</code>
     * to prevent it plotting points before the axes have been first drawn.
     * If the argument is true, clear the display first.
     */
	public synchronized void drawPlot(boolean clearfirst) {
    	// Draw the axes
    	drawAxes(clearfirst);
    	// Plot the points
    	for (int dataset = 0; dataset < numsets; dataset++) {
    	    Vector data = points[dataset];
    	    for (int pointnum = 0; pointnum < data.size(); pointnum++) {
    	        _drawPlotPoint(dataset, (PlotPoint)data.elementAt(pointnum));
    	    }
    	}
    	notify();
    }
    
    /**
     * Erase the point at the given index in the given dataset.  If lines are being
     * drawn, also erase the line to the next points (note: not to the previous point).
     * The point is not checked to see whether it is in range, so care must be taken
     * by the caller to ensure that it is.
     */
    public synchronized void erasePoint(int dataset, int index) {
        Vector pts = points[dataset];
        // Erase the line to the next point rather than the previous point.
        int saveprevx = _prevx[dataset];
        int saveprevy = _prevy[dataset];
        PlotPoint pp = (PlotPoint)pts.elementAt(index);
        if (index < pts.size() - 1) {
            PlotPoint nextp = (PlotPoint)pts.elementAt(index+1);
            pp.connected = nextp.connected;
            _prevx[dataset] = ulx + (int) ((nextp.x - xMin) * xscale);
            _prevy[dataset] = lry - (int) ((nextp.y - yMin) * yscale);
        }
        _drawPlotPoint(dataset, pp);
        _prevx[dataset] = saveprevx;
        _prevy[dataset] = saveprevy;
        pts.removeElementAt(index);
        pp = (PlotPoint)pts.elementAt(index);
        pp.connected = false;
    }

    /**
     * Return a string describing this applet.
     */
    public String getAppletInfo() {
        return "Plot 1.0: A flexible data plotter. By: Edward A. Lee, eal@eecs.berkeley.edu";
    }

    /**
     * Initialize the applet.  If a dataurl parameter has been specified,
     * read the file given by the URL and parse the commands in it.
     */
    public void init() {
        setNumSets(numsets);
        currentdataset = -1;
        super.init();
    }

	/**
	 * Draw the axes and the accumulated points.
	 */
	public void paint(Graphics g) {
	    drawPlot(true);
    }

    /**
     * Set the marks style to "none", "points", "dots", or "various".  In the last
     * case, unique marks are used for the first ten data sets, then recycled.
     */
    public void setMarksStyle (String style) {
        if (style.equalsIgnoreCase("none")) {
            marks = 0;
        } else if (style.equalsIgnoreCase("points")) {
            marks = 1;
        } else if (style.equalsIgnoreCase("dots")) {
            marks = 2;
        } else if (style.equalsIgnoreCase("various")) {
            marks = 3;
        }
    }

    /**
     * Specify the number of data sets to be plotted together.  Allocate a Vector
     * to store each data set.  Note that calling this causes any previously
     * plotted points to be forgotten.  This method should be called before
     * <code>setPointsPersistence</code>.
     */
    public void setNumSets (int numsets) {
        this.numsets = numsets;
        points = new Vector[numsets];
        _prevx = new int[numsets];
        _prevy = new int[numsets];
        for (int i=0; i<numsets; i++) {
            points[i] = new Vector();
        }
    }
    
    /**
     * Calling this method with a positive argument sets the
     * persistence of the plot to the given number of points.  Calling with a zero
     * argument turns off this feature, reverting to infinite memory (unless sweeps
     * persistence is set).  If both sweeps and points persistence are set
     * then sweeps take precedence.  This method should be called after
     * <code>setNumSets()</code>.
     * FIXME: No file format yet.
     */
    public void setPointsPersistence (int persistence) {
        _pointsPersistence = persistence;
        if (persistence > 0) {
            for (int i = 0; i < numsets; i++) {
                points[i].setSize(persistence);
            }
        }
    }
    
    /**
     * A sweep is a sequence of points where the value of X is increasing.
     * A point that is added with a smaller x than the previous point increments
     * the sweep count.  Calling this method with a non-zero argument sets the
     * persistence of the plot to the given number of sweeps.  Calling with a zero
     * argument turns off this feature.  If both sweeps and points persistence are set
     * then sweeps take precedence.
     * FIXME: No file format yet.
     * FIXME: Not implemented yet.
     */
    public void setSweepsPersistence (int persistence) {
        _sweepsPersistence = persistence;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                          protected methods                       ////
    
    /**
     * Put a mark corresponding to the specified dataset at the specified
     * x and y position. If the fourth argument is true, attempt to connect this
     * point with the previous point by drawing a line.  
     * If the fifth argument is true, then check the range and plot only points
     * and that portion of the connecting line that is in range.
     * If both the new point and the previous point are out of range, no line is drawn.
     */
    protected void drawPoint(int dataset, int xpos, int ypos, boolean connected, boolean clip) {
        // Points are only distinguished up to 10 data sets.
        dataset %= 10;
        if (_pointsPersistence > 0) {
            // To allow erasing to work by just redrawing the points.
            graphics.setXORMode(Color.red);
        }
        if (usecolor) {
            graphics.setColor(colors[dataset]);
        }
        // If the point is not out of range, draw it.
        boolean pointinside = ypos <= lry && ypos >= uly && xpos <= lrx && xpos >= ulx;
        boolean showsomething = pointinside || !clip;
        if (showsomething) {
            switch (marks) {
            case 1:
                // points -- use 3-pixel ovals.
                graphics.fillOval(xpos-1, ypos-1, 3, 3);
                break;
            case 2:
                // dots
                graphics.fillOval(xpos-_radius, ypos-_radius, _diameter, _diameter); 
                break;
            case 3:
                // marks
                int xpoints[], ypoints[];
                switch (dataset) {
                    case 0:
                        // filled circle
                        graphics.fillOval(xpos-_radius, ypos-_radius, _diameter, _diameter); 
                        break;
                    case 1:
                        // cross
                        graphics.drawLine(xpos-_radius, ypos-_radius, xpos+_radius, ypos+_radius); 
                        graphics.drawLine(xpos+_radius, ypos-_radius, xpos-_radius, ypos+_radius); 
                        break;
                    case 2:
                        // square
                        graphics.drawRect(xpos-_radius, ypos-_radius, _diameter, _diameter); 
                        break;
                    case 3:
                        // filled triangle
                        xpoints = new int[4];
                        ypoints = new int[4];
                        xpoints[0] = xpos; ypoints[0] = ypos-_radius;
                        xpoints[1] = xpos+_radius; ypoints[1] = ypos+_radius;
                        xpoints[2] = xpos-_radius; ypoints[2] = ypos+_radius;
                        xpoints[3] = xpos; ypoints[3] = ypos-_radius;
                        graphics.fillPolygon(xpoints, ypoints, 4);
                        break;
                    case 4:
                        // diamond
                        xpoints = new int[5];
                        ypoints = new int[5];
                        xpoints[0] = xpos; ypoints[0] = ypos-_radius;
                        xpoints[1] = xpos+_radius; ypoints[1] = ypos;
                        xpoints[2] = xpos; ypoints[2] = ypos+_radius;
                        xpoints[3] = xpos-_radius; ypoints[3] = ypos;
                        xpoints[4] = xpos; ypoints[4] = ypos-_radius;
                        graphics.drawPolygon(xpoints, ypoints, 5);
                        break;
                    case 5:
                        // circle
                        graphics.drawOval(xpos-_radius, ypos-_radius, _diameter, _diameter); 
                        break;
                    case 6:
                        // plus sign
                        graphics.drawLine(xpos, ypos-_radius, xpos, ypos+_radius); 
                        graphics.drawLine(xpos-_radius, ypos, xpos+_radius, ypos); 
                        break;
                    case 7:
                        // filled square
                        graphics.fillRect(xpos-_radius, ypos-_radius, _diameter, _diameter); 
                        break;
                    case 8:
                        // triangle
                        xpoints = new int[4];
                        ypoints = new int[4];
                        xpoints[0] = xpos; ypoints[0] = ypos-_radius;
                        xpoints[1] = xpos+_radius; ypoints[1] = ypos+_radius;
                        xpoints[2] = xpos-_radius; ypoints[2] = ypos+_radius;
                        xpoints[3] = xpos; ypoints[3] = ypos-_radius;
                        graphics.drawPolygon(xpoints, ypoints, 4);
                        break;
                    case 9:
                        // filled diamond
                        xpoints = new int[5];
                        ypoints = new int[5];
                        xpoints[0] = xpos; ypoints[0] = ypos-_radius;
                        xpoints[1] = xpos+_radius; ypoints[1] = ypos;
                        xpoints[2] = xpos; ypoints[2] = ypos+_radius;
                        xpoints[3] = xpos-_radius; ypoints[3] = ypos;
                        xpoints[4] = xpos; ypoints[4] = ypos-_radius;
                        graphics.fillPolygon(xpoints, ypoints, 5);
                        break;
                }
                break;
            default:
                // none
            }
        }
        
        int xstart = xpos;
        int ystart = ypos;
        int prevx = _prevx[dataset];
        int prevy = _prevy[dataset];

        // Draw a line to the previous point, if appropriate.
        if (connected) {
            if (clip) {
                 // Is the previous point in range?
                 boolean previnside = prevx >= ulx && prevx <= lrx &&
                         prevy >= uly && prevy <= lry;
                 // If the previous point is out of x range, adjust prev point to boundary.
                 int tmp;
                 if (pointinside) {
                     if (prevx < ulx) {
                         prevy = prevy + ((ypos - prevy) * (ulx - prevx))/(xpos - prevx);
                         prevx = ulx;
                     } else if (prevx > lrx) {
                         prevy = prevy + ((ypos - prevy) * (lrx - prevx))/(xpos - prevx);
                         prevx = lrx;
                     }

                     // If prev point is out of y range, adjust to boundary.
                     // Note that y increases downward
                     if (prevy < uly) {
                         prevx = prevx + ((xpos - prevx) * (uly - prevy))/(ypos - prevy);
                         prevy = uly;
                     } else if (prevy > lry) {
                         prevx = prevx + ((xpos - prevx) * (lry - prevy))/(ypos - prevy);
                         prevy = lry;
                     }
                 }

                 // If we are in line drawing mode, and the previous point was in range,
                 // but the current point is out of range, recompute the current point  
                 // position to lie on the boundary of the plot
                 if (!pointinside && previnside) {
                      showsomething = true;
                      // Adjust current point to lie on the boundary.
                      if (xpos < ulx) {
	                      ypos = ypos + ((prevy - ypos) * (ulx - xpos))/(prevx - xpos);
	                      xpos = ulx;
                      } else if (xpos > lrx) {
	                      ypos = ypos + ((prevy - ypos) * (lrx - xpos))/(prevx - xpos);
	                      xpos = lrx;
                      }
                      if (ypos < uly) {
	                      xpos = xpos + ((prevx - xpos) * (uly - ypos))/(prevy - ypos);
	                      ypos = uly;
                      } else if (ypos > lry) {
	                      xpos = xpos + ((prevx - xpos) * (lry - ypos))/(prevy - ypos);
	                      ypos = lry;
                      }
                  }
             }

             if (showsomething) {
                 graphics.drawLine(xpos, ypos, prevx, prevy);
             }
        }
        _prevx[dataset] = xstart;
        _prevy[dataset] = ystart;
        graphics.setColor(Color.black);
        if (_pointsPersistence > 0) {
            // Restore paint mode in case axes get redrawn.
            graphics.setPaintMode();
        }
    }
    
    /**
     * Parse a line that gives plotting information. Return true if the line is recognized.
     * Lines with syntax errors are ignored.
     */
    protected boolean parseLine (String line) {
        // parse only if the super class does not recognize the line.
        boolean connected = false;
        if (_connected) connected = true;
        if (!super.parseLine(line)) {
            int start = 0;
            if (line.startsWith("Marks:")) {
                String style = (line.substring(6)).trim();
                setMarksStyle(style);
                return true;
            } else if (line.startsWith("NumSets:")) {
                String num = (line.substring(8)).trim();
                try {
                    setNumSets(Integer.parseInt(num));
                }
                catch (NumberFormatException e) {
                    // ignore bogons
                }
            } else if (line.startsWith("DataSet:")) {
                // new data set
                _firstinset = true;
                currentdataset += 1;
                if (currentdataset >= 10) currentdataset = 0;
                String legend = (line.substring(8)).trim();
                addLegend(currentdataset, legend);
            } else if (line.startsWith("Lines:")) {
                if (line.indexOf("off",6) >= 0) {
                    _connected = false;
                }
            } else if (line.startsWith("move:")) {
                // a disconnected point
                connected = false;
                start = 5;
            } else if (line.startsWith("draw:")) {
                // a connected point, if connect is enabled.
                start = 5;
            }
            // See if an x,y point is given
         	int comma = line.indexOf(",", start);
        	if (comma > 0) {
                String x = (line.substring(start, comma)).trim();
                String y = (line.substring(comma+1)).trim();
        	    try {
        	        Double xpt = new Double(x);
        	        Double ypt = new Double(y);
        	        if (_firstinset) {
        	            connected = false;
        	            _firstinset = false;
        	        }
        	        addPoint(currentdataset, xpt.doubleValue(), ypt.doubleValue(), connected);
        	        return true;
        	    } catch (NumberFormatException e) {
        	        // ignore if format is bogus.
        	    }
        	}
        }
        return false;
    }
    
    //////////////////////////////////////////////////////////////////////////
    ////                       protected variables                        ////
    
    // The current dataset
    protected int currentdataset = -1;
    
    // A vector of datasets.
    protected Vector[] points;
    
    // An indicator of the marks style.  See parseLine method for interpretation.
    protected int marks;
    
    protected int numsets = 1;

    //////////////////////////////////////////////////////////////////////////
    ////                       private methods                            ////
    
    /**
     * Draw the specified point. If the point is out of range, do nothing.
     */
    private void _drawPlotPoint(int dataset, PlotPoint pt) {
        int ypos = lry - (int) ((pt.y - yMin) * yscale);
        int xpos = ulx + (int) ((pt.x - xMin) * xscale);
        drawPoint(dataset, xpos, ypos, pt.connected, true);
    }
    
    //////////////////////////////////////////////////////////////////////////
    ////                       private variables                          ////
    
    private int _pointsPersistence = 0;
    private int _sweepsPersistence = 0;
    private boolean _connected = true;
    private boolean _firstinset = true;
    
    // Give both radius and diameter of a point for efficiency.
    private int _radius = 3;
    private int _diameter = 6;
    
    // Information about the previously plotted point.
    private int _prevx[], _prevy[];
}
