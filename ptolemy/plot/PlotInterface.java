/*
 Copyright (c) 2011-2014 The Regents of the University of California.
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

package ptolemy.plot;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;

///////////////////////////////////////////////////////////////////
//// PlotInterface

/**
 * Definitions for an object that plots data.
 * @author ahuseyno
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public interface PlotInterface extends PlotBoxInterface {
    @Override
    public void addLegend(int dataset, String legend);

    /** In the specified data set, add the specified x, y point to the
     *  plot.  Data set indices begin with zero.  If the data set
     *  does not exist, create it.  The fourth argument indicates
     *  whether the point should be connected by a line to the previous
     *  point.  Regardless of the value of this argument, a line will not
     *  drawn if either there has been no previous point for this dataset
     *  or setConnected() has been called with a false argument.
     *  <p>
     *  In order to work well with swing and be thread safe, this method
     *  actually defers execution to the event dispatch thread, where
     *  all user interface actions are performed.  Thus, the point will
     *  not be added immediately (unless you call this method from within
     *  the event dispatch thread). All the methods that do this deferring
     *  coordinate so that they are executed in the order that you
     *  called them.
     *
     *  @param dataset The data set index.
     *  @param x The X position of the new point.
     *  @param y The Y position of the new point.
     *  @param connected If true, a line is drawn to connect to the previous
     *   point.
     */
    public void addPoint(final int dataset, final double x, final double y,
            final boolean connected);

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
     *  Michael Altmann (michael@email.labmed.umn.edu).
     *  <p>
     *  In order to work well with swing and be thread safe, this method
     *  actually defers execution to the event dispatch thread, where
     *  all user interface actions are performed.  Thus, the point will
     *  not be added immediately (unless you call this method from within
     *  the event dispatch thread).  All the methods that do this deferring
     *  coordinate so that they are executed in the order that you
     *  called them.
     *
     *  @param dataset The data set index.
     *  @param x The X position of the new point.
     *  @param y The Y position of the new point.
     *  @param yLowEB The low point of the error bar.
     *  @param yHighEB The high point of the error bar.
     *  @param connected If true, a line is drawn to connect to the previous
     *   point.
     */
    public void addPointWithErrorBars(final int dataset, final double x,
            final double y, final double yLowEB, final double yHighEB,
            final boolean connected);

    /** Clear the plot of all data points.  If the argument is true, then
     *  reset all parameters to their initial conditions, including
     *  the persistence, plotting format, and axes formats.
     *  For the change to take effect, you must call repaint().
     *  @param format If true, clear the format controls as well.
     *  <p>
     *  In order to work well with swing and be thread safe, this method
     *  actually defers execution to the event dispatch thread, where
     *  all user interface actions are performed.  Thus, the clear will
     *  not be executed immediately (unless you call this method from within
     *  the event dispatch thread).  All the methods that do this deferring
     *  coordinate so that they are executed in the order that you
     *  called them.
     */
    @Override
    public void clear(final boolean format);

    /** Clear the plot of data points in the specified dataset.
     *  This calls repaint() to request an update of the display.
     *  <p>
     *  In order to work well with swing and be thread safe, this method
     *  actually defers execution to the event dispatch thread, where
     *  all user interface actions are performed.  Thus, the point will
     *  not be added immediately (unless you call this method from within
     *  the event dispatch thread).  If you call this method, the addPoint()
     *  method, and the erasePoint() method in any order, they are assured
     *  of being processed in the order that you called them.
     *
     *  @param dataset The dataset to clear.
     */
    public void clear(final int dataset);

    /** Erase the point at the given index in the given dataset.  If
     * lines are being drawn, these lines are erased and if necessary new
     * ones will be drawn. The point is not checked to
     *  see whether it is in range, so care must be taken by the caller
     *  to ensure that it is.
     *  <p>
     *  In order to work well with swing and be thread safe, this method
     *  actually defers execution to the event dispatch thread, where
     *  all user interface actions are performed.  Thus, the point will
     *  not be erased immediately (unless you call this method from within
     *  the event dispatch thread).  All the methods that do this deferring
     *  coordinate so that they are executed in the order that you
     *  called them.
     *
     *  @param dataset The data set index.
     *  @param index The index of the point to erase.
     */
    public void erasePoint(final int dataset, final int index);

    /** Rescale so that the data that is currently plotted just fits.
     *  This overrides the base class method to ensure that the protected
     *  variables _xBottom, _xTop, _yBottom, and _yTop are valid.
     *  This method calls repaint(), which eventually causes the display
     *  to be updated.
     *  <p>
     *  In order to work well with swing and be thread safe, this method
     *  actually defers execution to the event dispatch thread, where
     *  all user interface actions are performed.  Thus, the fill will
     *  not occur immediately (unless you call this method from within
     *  the event dispatch thread).  All the methods that do this deferring
     *  coordinate so that they are executed in the order that you
     *  called them.
     */
    @Override
    public void fillPlot();

    /** Return whether the default is to connect
     *  subsequent points with a line.  If the result is false, then
     *  points are not connected.  When points are by default
     *  connected, individual points can be not connected by giving the
     *  appropriate argument to addPoint().  Also, a different default
     *  can be set for each dataset, overriding this global default.
     *  @return True if points will be connected by default
     * @see #setConnected
     */
    public boolean getConnected();

    /** Return whether a line will be drawn from any
     *  plotted point down to the x axis.
     *  A plot with such lines is also known as a stem plot.
     *  @return True if this is an impulse plot
     *  @see #setImpulses(boolean)
     */
    public boolean getImpulses();

    /** Return false if setLineStyles() has not yet been called or if
     *  setLineStyles(false) has been called, which signifies that
     *  different line styles are not to be used.  Otherwise, return true.
     *  @return True if line styles are to be used.
     *  @see #setLineStyles(boolean)
     */
    public boolean getLineStyles();

    /** Get the marks style, which is one of
     *  "none", "points", "dots", or "various".
     *  @return A string specifying the style for points.
     *  @see #setMarksStyle
     */
    public String getMarksStyle();

    /** Return the maximum number of data sets.
     *  This method is deprecated, since there is no longer an upper bound.
     *  @return The maximum number of data sets
     *  @deprecated
     */
    @Deprecated
    public int getMaxDataSets();

    /** Return the actual number of data sets.
     *  @return The number of data sets that have been created.
     */
    public int getNumDataSets();

    /** Return false if setReuseDatasets() has not yet been called
     *  or if setReuseDatasets(false) has been called.
     *  @return false if setReuseDatasets() has not yet been called
     *  or if setReuseDatasets(false) has been called.
     *  @since Ptolemy II 10.0
     *  @see #setReuseDatasets(boolean)
     */
    public boolean getReuseDatasets();

    /** Override the base class to indicate that a new data set is being read.
     *  This method is deprecated.  Use read() instead (to read the old
     *  file format) or one of the classes in the plotml package to read
     *  the new (XML) file format.
     *  @deprecated
     */
    @Override
    @Deprecated
    public void parseFile(String filespec, URL documentBase);

    /** Mark the disconnections with a Dot in case value equals true, otherwise these
     *  points are not marked.
     *  @param value True when disconnections should be marked.
     */
    public void markDisconnections(boolean value);

    /** Read a file with the old syntax (non-XML).
     *  Override the base class to register that we are reading a new
     *  data set.
     *  @param inputStream The input stream.
     *  @exception IOException If the stream cannot be read.
     */
    @Override
    public void read(InputStream inputStream) throws IOException;

    /** Create a sample plot.  This is not actually done immediately
     *  unless the calling thread is the event dispatch thread.
     *  Instead, it is deferred to the event dispatch thread.
     *  It is important that the calling thread not hold a synchronize
     *  lock on the Plot object, or deadlock will result (unless the
     *  calling thread is the event dispatch thread).
     */
    @Override
    public void samplePlot();

    /** Turn bars on or off (for bar charts).  Note that this is a global
     *  property, not per dataset.
     *  @param on If true, turn bars on.
     */
    public void setBars(boolean on);

    /** Turn bars on and set the width and offset.  Both are specified
     *  in units of the x axis.  The offset is the amount by which the
     *  i <sup>th</sup> data set is shifted to the right, so that it
     *  peeks out from behind the earlier data sets.
     *  @param width The width of the bars.
     *  @param offset The offset per data set.
     */
    public void setBars(double width, double offset);

    /** If the argument is true, then the default is to connect
     *  subsequent points with a line.  If the argument is false, then
     *  points are not connected.  When points are by default
     *  connected, individual points can be not connected by giving the
     *  appropriate argument to addPoint().  Also, a different default
     *  can be set for each dataset, overriding this global default.
     *  setConnected will also change the behavior of points that were
     *  already drawn if the graph is redrawn. If it isn't the points
     *  are not touched. If you change back the setConnected state,
     *  the again see what was visible before.
     *  @param on If true, draw lines between points.
     *  @see #setConnected(boolean, int)
     *  @see #getConnected
     */
    public void setConnected(boolean on);

    /** If the first argument is true, then by default for the specified
     *  dataset, points will be connected by a line.  Otherwise, the
     *  points will not be connected. When points are by default
     *  connected, individual points can be not connected by giving the
     *  appropriate argument to addPoint().
     *  Note that this method should be called before adding any points.
     *  Note further that this method should probably be called from
     *  the event thread.
     *  @param on If true, draw lines between points.
     *  @param dataset The dataset to which this should apply.
     *  @see #setConnected(boolean)
     *  @see #getConnected
     */
    public void setConnected(boolean on, int dataset);

    /** If the argument is true, then a line will be drawn from any
     *  plotted point down to the x axis.  Otherwise, this feature is
     *  disabled.  A plot with such lines is also known as a stem plot.
     *  @param on If true, draw a stem plot.
     *  @see #getImpulses()
     */
    public void setImpulses(boolean on);

    /** If the first argument is true, then a line will be drawn from any
     *  plotted point in the specified dataset down to the x axis.
     *  Otherwise, this feature is
     *  disabled.  A plot with such lines is also known as a stem plot.
     *  @param on If true, draw a stem plot.
     *  @param dataset The dataset to which this should apply.
     *  @see #getImpulses()
     */
    public void setImpulses(boolean on, int dataset);

    /** Set the style of the lines joining marks.
     *  @param styleString A string specifying the color for points.
     *  The following styles are permitted: "solid", "dotted",
     *  "dashed", "dotdashed", "dotdotdashed".
     *  @param dataset The data set index.
     */
    public void setLineStyle(String styleString, int dataset);

    /** If the argument is true, draw the data sets with different line
     *  styles.  Otherwise, use one line style.
     *  @param lineStyles True if the data sets are to be drawn in different
     *  line styles.
     *  @see #getLineStyles()
     */
    public void setLineStyles(boolean lineStyles);

    /** Set the marks style to "none", "points", "dots", or "various".
     *  In the last case, unique marks are used for the first ten data
     *  sets, then recycled.
     *  This method should be called only from the event dispatch thread.
     *  @param style A string specifying the style for points.
     *  @see #getMarksStyle
     */
    public void setMarksStyle(String style);

    /** Set the marks style to "none", "points", "dots", "various",
     *  or "pixels" for the specified dataset.
     *  In the last case, unique marks are used for the first ten data
     *  sets, then recycled.
     *  @param style A string specifying the style for points.
     *  @param dataset The dataset to which this should apply.
     *  @see #getMarksStyle
     */
    public void setMarksStyle(String style, int dataset);

    /** Specify the number of data sets to be plotted together.
     *  This method is deprecated, since it is no longer necessary to
     *  specify the number of data sets ahead of time.
     *  @param numSets The number of data sets.
     *  @deprecated
     */
    @Deprecated
    public void setNumSets(int numSets);

    /** Calling this method with a positive argument sets the
     *  persistence of the plot to the given number of points.  Calling
     *  with a zero argument turns off this feature, reverting to
     *  infinite memory (unless sweeps persistence is set).  If both
     *  sweeps and points persistence are set then sweeps take
     *  precedence.
     *  <p>
     *  Setting the persistence greater than zero forces the plot to
     *  be drawn in XOR mode, which allows points to be quickly and
     *  efficiently erased.  However, there is a bug in Java (as of
     *  version 1.3), where XOR mode does not work correctly with
     *  double buffering.  Thus, if you call this with an argument
     *  greater than zero, then we turn off double buffering for this
     *  panel <i>and all of its parents</i>.  This actually happens
     *  on the next call to addPoint().
     *  @param persistence Number of points to persist for.
     */
    public void setPointsPersistence(int persistence);

    /** If the argument is true, then datasets with the same name
     *  are merged into a single dataset.
     *  @param on If true, then merge datasets.
     *  @see #getReuseDatasets()
     */
    public void setReuseDatasets(boolean on);

    /** Calling this method with a positive argument sets the
     *  persistence of the plot to the given width in units of the
     *  horizontal axis. Calling
     *  with a zero argument turns off this feature, reverting to
     *  infinite memory (unless points persistence is set).  If both
     *  X and points persistence are set then both are applied,
     *  meaning that points that are old by either criterion will
     *  be erased.
     *  <p>
     *  Setting the X persistence greater than zero forces the plot to
     *  be drawn in XOR mode, which allows points to be quickly and
     *  efficiently erased.  However, there is a bug in Java (as of
     *  version 1.3), where XOR mode does not work correctly with
     *  double buffering.  Thus, if you call this with an argument
     *  greater than zero, then we turn off double buffering for this
     *  panel <i>and all of its parents</i>.  This actually happens
     *  on the next call to addPoint().
     *  @param persistence Persistence in units of the horizontal axis.
     */
    public void setXPersistence(double persistence);

    /** Write plot data information to the specified output stream in PlotML.
     *  @param output A buffered print writer.
     */
    @Override
    public void writeData(PrintWriter output);

    /** Write plot format information to the specified output stream in
     *  PlotML, an XML scheme.
     *  @param output A buffered print writer.
     */
    @Override
    public void writeFormat(PrintWriter output);
}
