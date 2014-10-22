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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Vector;

///////////////////////////////////////////////////////////////////
//// PlotBoxInterface

/**
 * Definitions for an object that contains a plotter.
 * @author ahuseyno
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public interface PlotBoxInterface {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a line to the caption (displayed at below graph) .
     * @param captionLine The string to be added.
     * @see #getCaptions()
     */
    public void addCaptionLine(String captionLine);

    /** Add a legend (displayed at the upper right) for the specified
     *  data set with the specified string.  Short strings generally
     *  fit better than long strings.  If the string is empty, or the
     *  argument is null, then no legend is added.
     *  @param dataset The dataset index.
     *  @param legend The label for the dataset.
     *  @see #renameLegend(int, String)
     */
    public void addLegend(int dataset, String legend);

    /** Specify a tick mark for the X axis.  The label given is placed
     *  on the axis at the position given by <i>position</i>. If this
     *  is called once or more, automatic generation of tick marks is
     *  disabled.  The tick mark will appear only if it is within the X
     *  range.
     *  <p>Note that if {@link #setXLog(boolean)} has been called, then
     *  the position value should be in log units.
     *  So, addXTick("1K", 3) will display the string <pre>1K</pre>
     *  at the 1000 mark.
     *  @param label The label for the tick mark.
     *  @param position The position on the X axis.
     */
    public void addXTick(String label, double position);

    /** Specify a tick mark for the Y axis.  The label given is placed
     *  on the axis at the position given by <i>position</i>. If this
     *  is called once or more, automatic generation of tick marks is
     *  disabled.  The tick mark will appear only if it is within the Y
     *  range.
     *  <p>Note that if {@link #setYLog(boolean)} has been called, then
     *  the position value should be in log units.
     *  So, addYTick("1K", 3) will display the string <pre>1K</pre>
     *  at the 1000 mark.
     *  @param label The label for the tick mark.
     *  @param position The position on the Y axis.
     */
    public void addYTick(String label, double position);

    /** If the argument is true, clear the axes.  I.e., set all parameters
     *  controlling the axes to their initial conditions.
     *  For the change to take effect, call repaint().  If the argument
     *  is false, do nothing.
     *  @param axes If true, clear the axes parameters.
     */
    public void clear(boolean axes);

    /** Clear all the captions.
     *  For the change to take effect, call repaint().
     *  @see #setCaptions(Vector)
     */
    public void clearCaptions();

    /** Clear all legends.  This will show up on the next redraw.
     */
    public void clearLegends();

    /** If this method is called in the event thread, then simply
     * execute the specified action.  Otherwise,
     * if there are already deferred actions, then add the specified
     * one to the list.  Otherwise, create a list of deferred actions,
     * if necessary, and request that the list be processed in the
     * event dispatch thread.
     *
     * Note that it does not work nearly as well to simply schedule
     * the action yourself on the event thread because if there are a
     * large number of actions, then the event thread will not be able
     * to keep up.  By grouping these actions, we avoid this problem.
     *
     * This method is not , so the caller should be.
     * @param action The Runnable object to execute.
     */
    public void deferIfNecessary(Runnable action);

    /** Destroy the plotter.  This method is usually
     *  called by PlotApplet.destroy().  It does
     *  various cleanups to reduce memory usage.
     */
    public void destroy();

    /** Rescale so that the data that is currently plotted just fits.
     *  This is done based on the protected variables _xBottom, _xTop,
     *  _yBottom, and _yTop.  It is up to derived classes to ensure that
     *  variables are valid.
     *  This method calls repaint(), which eventually causes the display
     *  to be updated.
     */
    public void fillPlot();

    /** Get the captions.
     *  @return the captions
     *  @see #addCaptionLine(String)
     *  @see #setCaptions(Vector)
     */
    public Vector getCaptions();

    /** Return whether the plot uses color.
     *  @return True if the plot uses color.
     *  @see #setColor(boolean)
     */
    public boolean getColor();

    /** Get the point colors.
     *  @return Array of colors
     *  @see #setColors(Object[])
     */
    public Object[] getColors();

    /** Get the file specification that was given by setDataurl().
     *  @return the file specification
     *  @see #setDataurl(String)
     *  @deprecated Use read() instead.
     */
    @Deprecated
    public String getDataurl();

    /** Get the document base that was set by setDocumentBase().
     *  @return the document base.
     *  @see #setDocumentBase(URL)
     *  @deprecated Use read() instead.
     */
    @Deprecated
    public URL getDocumentBase();

    /** Return whether the grid is drawn.
     *  @return True if a grid is drawn.
     *  @see #setGrid(boolean)
     */
    public boolean getGrid();

    /** Get the legend for a dataset, or null if there is none.
     *  The legend would have been set by addLegend().
     *  @param dataset The dataset index.
     *  @return The legend label, or null if there is none.
     */
    public String getLegend(int dataset);

    /** Given a legend string, return the corresponding dataset or -1 if no
     *  legend was added with that legend string
     *  The legend would have been set by addLegend().
     *  @param legend The String naming the legend
     *  @return The legend dataset, or -1 if not found.
     *  @since Ptolemy II 10.0
     */
    public int getLegendDataset(String legend);

    /** If the size of the plot has been set by setSize(),
     *  then return that size.  Otherwise, return what the superclass
     *  returns (which is undocumented, but apparently imposes no maximum size).
     *  Currently (JDK 1.3), only BoxLayout pays any attention to this.
     *  @return The maximum desired size.
     */

    //     public  Dimension getMaximumSize() {
    //         if (_sizeHasBeenSet) {
    //             return new Dimension(_preferredWidth, _preferredHeight);
    //         } else {
    //             return super.getMaximumSize();
    //         }
    //     }
    /** Get the minimum size of this component.
     *  This is simply the dimensions specified by setSize(),
     *  if this has been called.  Otherwise, return whatever the base
     *  class returns, which is undocumented.
     *  @return The minimum size.
     */

    //     public  Dimension getMinimumSize() {
    //         if (_sizeHasBeenSet) {
    //             return new Dimension(_preferredWidth, _preferredHeight);
    //         } else {
    //             return super.getMinimumSize();
    //         }
    //     }

    /** Get the current plot rectangle.  Note that in PlotBox, the
     *  Rectangle returned by this method is calculated from the
     *  values of _ulx _uly, _lrx and _lry.  The value passed in by
     *  setPlotRectangle() is not directly used, thus calling
     *  getPlotRectangle() may not return the same rectangle that was
     *  passed in with setPlotRectangle().
     *  @return Rectangle
     *  @see #setPlotRectangle(Object)
     */
    public Object getPlotRectangle();

    /** Get the title of the graph, or an empty string if there is none.
     *  @return The title.
     *  @see #setTitle(String)
     */
    public String getTitle();

    /** Get the range for X values of the data points registered so far.
     *  Usually, derived classes handle managing the range by checking
     *  each new point against the current range.
     *  @return An array of two doubles where the first element is the
     *  minimum and the second element is the maximum.
     *  @see #getXRange()
     */
    public double[] getXAutoRange();

    /** Get the label for the X (horizontal) axis, or null if none has
     *  been set.
     *  @return The X label.
     *  @see #setXLabel(String)
     */
    public String getXLabel();

    /** Return whether the X axis is drawn with a logarithmic scale.
     *  @return True if the X axis is logarithmic.
     *  @see #setXLog(boolean)
     */
    public boolean getXLog();

    /** Get the X range. If {@link #setXRange(double, double)} has been
     *  called, then this method returns the values passed in as
     *  arguments to setXRange(double, double).  If setXRange(double,
     *  double) has not been called, then this method returns the
     *  range of the data to be plotted, which might not be all of the
     *  data due to zooming.
     *  @return An array of two doubles where the first element is the
     *  minimum and the second element is the maximum.
     *  @see #getXAutoRange()
     *  @see #setXRange(double, double)
     */
    public double[] getXRange();

    /** Get the X ticks that have been specified, or null if none.
     *  The return value is an array with two vectors, the first of
     *  which specifies the X tick locations (as instances of Double),
     *  and the second of which specifies the corresponding labels.
     *  @return The X ticks.
     */
    public Vector[] getXTicks();

    /** Get the range for Y values of the data points registered so far.
     *  Usually, derived classes handle managing the range by checking
     *  each new point against the range.
     *  @return An array of two doubles where the first element is the
     *  minimum and the second element is the maximum.
     *  @see #getYRange()
     */
    public double[] getYAutoRange();

    /** Get the label for the Y (vertical) axis, or null if none has
     *  been set.
     *  @return The Y label.
     *  @see #setYLabel(String)
     */
    public String getYLabel();

    /** Return whether the Y axis is drawn with a logarithmic scale.
     *  @return True if the Y axis is logarithmic.
     *  @see #setYLog(boolean)
     */
    public boolean getYLog();

    /** Get the Y range. If {@link #setYRange(double, double)} has been
     *  called, then this method returns the values passed in as
     *  arguments to setYRange(double, double).  If setYRange(double,
     *  double) has not been called, then this method returns the
     *  range of the data to be plotted, which might not be all of the
     *  data due to zooming.
     *  @return An array of two doubles where the first element is the
     *  minimum and the second element is the maximum.
     *  @see #getYAutoRange()
     *  @see #setYRange(double, double)
     */
    public double[] getYRange();

    /** Get the Y ticks that have been specified, or null if none.
     *  The return value is an array with two vectors, the first of
     *  which specifies the Y tick locations (as instances of Double),
     *  and the second of which specifies the corresponding labels.
     *  @return The Y ticks.
     */
    public Vector[] getYTicks();

    /** Initialize the component, creating the fill button and parsing
     *  an input file, if one has been specified.  This is deprecated.
     *  Call setButtons() and read() instead.
     *  @deprecated
     */
    @Deprecated
    public void init();

    /** Syntactic sugar for parseFile(filespec, documentBase).
     *  @param filespec The file to be read.
     *  @deprecated  Use read() to read the old file
     *  format, or use one of the classes in the plotml package to
     *  read the XML-based file format.
     */
    @Deprecated
    public void parseFile(String filespec);

    /** Open up the input file, which could be stdin, a URL, or a file.
     *  @param filespec The file to be read.
     *  @param documentBase The base of the URL
     *  @deprecated Use read() instead.
     */
    @Deprecated
    public void parseFile(String filespec, URL documentBase);

    /** Read commands and/or plot data from an input stream in the old
     *  (non-XML) file syntax.
     *  To update the display, call repaint(), or make the plot visible with
     *  setVisible(true).
     *  <p>
     *  To read from standard input, use:
     *  <pre>
     *     read(System.in);
     *  </pre>
     *  To read from a url, use:
     *  <pre>
     *     read(url.openStream());
     *  </pre>
     *  To read a URL from within an applet, use:
     *  <pre>
     *     URL url = new URL(getDocumentBase(), urlSpec);
     *     read(url.openStream());
     *  </pre>
     *  Within an application, if you have an absolute URL, use:
     *  <pre>
     *     URL url = new URL(urlSpec);
     *     read(url.openStream());
     *  </pre>
     *  To read from a file, use:
     *  <pre>
     *     read(new FileInputStream(filename));
     *  </pre>
     *  @param in The input stream.
     *  @exception IOException If the stream cannot be read.
     */
    public void read(InputStream in) throws IOException;

    /** Read a single line command provided as a string.
     *  The commands can be any of those in the ASCII file format.
     *  @param command A command.
     */
    public void read(String command);

    /** Remove the legend (displayed at the upper right) for the specified
     *  data set. If the dataset is not found, nothing will occur.
     *  The PlotBox must be repainted in order for this to take effect.
     *  @param dataset The dataset index.
     */
    public void removeLegend(int dataset);

    /** Rename a legend.
     *  @param dataset The dataset of the legend to be renamed.
     *  If there is no dataset with this value, then nothing happens.
     *  @param newName  The new name of legend.
     *  @see #addLegend(int, String)
     */
    public void renameLegend(int dataset, String newName);

    /** Repaint the object. */
    public void repaint();

    /** Reset the X and Y axes to the ranges that were first specified
     *  using setXRange() and setYRange(). If these methods have not been
     *  called, then reset to the default ranges.
     *  This method calls repaint(), which eventually causes the display
     *  to be updated.
     */
    public void resetAxes();

    /** Do nothing in this base class. Derived classes might want to override
     *  this class to give an example of their use.
     */
    public void samplePlot();

    /**
     * Set automatic rescale. Automatic rescaling is enabled
     * when automaticRescale equals true and disabled when
     * automaticRescale equals false.
     * @param automaticRescale The boolean that specifies whether
     * plots should be automatic rescaled.
     */
    public void setAutomaticRescale(boolean automaticRescale);

    /** Set the background color.
     *  @param background The background color.
     */
    public void setBackground(Object background);

    /** If the argument is true, make a fill button visible at the upper
     *  right.  This button auto-scales the plot.
     *  NOTE: The button may infringe on the title space,
     *  if the title is long.  In an application, it is preferable to provide
     *  a menu with the fill command.  This way, when printing the plot,
     *  the printed plot will not have a spurious button.  Thus, this method
     *  should be used only by applets, which normally do not have menus.
     *  This method should only be called from within the event dispatch
     *  thread, since it interacts with swing.
     *  @param visible If true, make the fill button appear.
     *  @see #destroy()
     */
    public void setButtons(boolean visible);

    /** Set the strings of the caption.
     *  @param captionStrings A Vector where each element contains a String
     *  that is one line of the caption.
     *  @see #getCaptions()
     *  @see #clearCaptions()
     */
    public void setCaptions(Vector captionStrings);

    /** If the argument is false, draw the plot without using color
     *  (in black and white).  Otherwise, draw it in color (the default).
     *  @param useColor False to draw in back and white.
     *  @see #getColor()
     */
    public void setColor(boolean useColor);

    /** Set the point colors.  Note that the default colors have been
     *  carefully selected to maximize readability and that it is easy
     *  to use colors that result in a very ugly plot.
     *  @param colors Array of colors to use in succession for data sets.
     *  @see #getColors()
     */
    public void setColors(Object[] colors);

    /** Set the file to read when init() is called.
     *  @param filespec the file to be read
     *  @see #getDataurl()
     *  @deprecated Use read() instead.
     */
    @Deprecated
    public void setDataurl(String filespec);

    /** Set the document base to used when init() is called to read a URL.
     *  @param documentBase The document base to be used.
     *  @see #getDocumentBase()
     *  @deprecated   Use read() instead.
     */
    @Deprecated
    public void setDocumentBase(URL documentBase);

    /** Set the foreground color.
     *  @param foreground The foreground color.
     */
    public void setForeground(Object foreground);

    /** Control whether the grid is drawn.
     *  @param grid If true, a grid is drawn.
     *  @see #getGrid()
     */
    public void setGrid(boolean grid);

    /** Set the label font, which is used for axis labels and legend labels.
     *  The font names understood are those understood by
     *  java.awt.Font.decode().
     *  @param name A font name.
     */
    public void setLabelFont(String name);

    /** Set the plot rectangle inside the axes.  This method
     *  can be used to create two plots that share the same axes.
     *  @param rectangle Rectangle space inside axes.
     *  @see #getPlotRectangle()
     */
    public void setPlotRectangle(Object rectangle);

    /** Set the size of the plot.  This overrides the base class to make
     *  it work.  In particular, it records the specified size so that
     *  getMinimumSize() and getPreferredSize() return the specified value.
     *  However, it only works if the plot is placed in its own JPanel.
     *  This is because the JPanel asks the contained component for
     *  its preferred size before determining the size of the panel.
     *  If the plot is placed directly in the content pane of a JApplet,
     *  then, mysteriously, this method has no effect.
     *  @param width The width, in pixels.
     *  @param height The height, in pixels.
     */
    public void setSize(int width, int height);

    /**
     * Set repainting with a certain fixed refresh rate. This timed
     * repainting is enabled when timedRepaint equals true and
     * disabled when timedRepaint equals false.
     * @param timedRepaint The boolean that specifies whether
     * repainting should happen with a certain fixed refresh rate.
     */
    public void setTimedRepaint(boolean timedRepaint);

    /** Set the title of the graph.
     *  @param title The title.
     *  @see #getTitle()
     */
    public void setTitle(String title);

    /** Set the title font.
     *  The font names understood are those understood by
     *  java.awt.Font.decode().
     *  @param name A font name.
     */
    public void setTitleFont(String name);

    /** Specify whether the X axis is wrapped.
     *  If it is, then X values that are out of range are remapped
     *  to be in range using modulo arithmetic. The X range is determined
     *  by the most recent call to setXRange() (or the most recent zoom).
     *  If the X range has not been set, then use the default X range,
     *  or if data has been plotted, then the current fill range.
     *  @param wrap If true, wrapping of the X axis is enabled.
     */
    public void setWrap(boolean wrap);

    /** Set the label for the X (horizontal) axis.
     *  @param label The label.
     *  @see #getXLabel()
     */
    public void setXLabel(String label);

    /** Specify whether the X axis is drawn with a logarithmic scale.
     *  If you would like to have the X axis drawn with a
     *  logarithmic axis, then setXLog(true) should be called before
     *  adding any data points.
     *  @param xlog If true, logarithmic axis is used.
     *  @see #getXLog()
     */
    public void setXLog(boolean xlog);

    /** Set the X (horizontal) range of the plot.  If this is not done
     *  explicitly, then the range is computed automatically from data
     *  available when the plot is drawn.  If min and max
     *  are identical, then the range is arbitrarily spread by 1.
     *  <p>Note that if {@link #setXLog(boolean)} has been called, then
     *  the min and max values should be in log units.
     *  @param min The left extent of the range.
     *  @param max The right extent of the range.
     *  @see #getXRange()
     */
    public void setXRange(double min, double max);

    /** Set the label for the Y (vertical) axis.
     *  @param label The label.
     *  @see #getYLabel()
     */
    public void setYLabel(String label);

    /** Specify whether the Y axis is drawn with a logarithmic scale.
     *  If you would like to have the Y axis drawn with a
     *  logarithmic axis, then setYLog(true) should be called before
     *  adding any data points.
     *  @param ylog If true, logarithmic axis is used.
     *  @see #getYLog()
     */
    public void setYLog(boolean ylog);

    /** Set the Y (vertical) range of the plot.  If this is not done
     *  explicitly, then the range is computed automatically from data
     *  available when the plot is drawn.  If min and max are identical,
     *  then the range is arbitrarily spread by 0.1.
     *  <p>Note that if {@link #setYLog(boolean)} has been called, then
     *  the min and max values should be in log units.
     *  @param min The bottom extent of the range.
     *  @param max The top extent of the range.
     *  @see #getYRange()
     */
    public void setYRange(double min, double max);

    /** Write the current data and plot configuration to the
     *  specified stream in PlotML syntax.  PlotML is an XML
     *  extension for plot data.  The written information is
     *  standalone, in that it includes the DTD (document type
     *  definition).  This makes is somewhat verbose.  To get
     *  smaller files, use the two argument version of write().
     *  The output is buffered, and is flushed and
     *  closed before exiting.  Derived classes should override
     *  writeFormat and writeData rather than this method.
     *  @param out An output stream.
     */
    public void write(OutputStream out);

    /** Write the current data and plot configuration to the
     *  specified stream in PlotML syntax.  PlotML is an XML
     *  scheme for plot data. The URL (relative or absolute) for the DTD is
     *  given as the second argument.  If that argument is null,
     *  then the PlotML PUBLIC DTD is referenced, resulting in a file
     *  that can be read by a PlotML parser without any external file
     *  references, as long as that parser has local access to the DTD.
     *  The output is buffered, and is flushed and
     *  closed before exiting.  Derived classes should override
     *  writeFormat and writeData rather than this method.
     *  @param out An output stream.
     *  @param dtd The reference (URL) for the DTD, or null to use the
     *   PUBLIC DTD.
     */
    public void write(OutputStream out, String dtd);

    /** Write the current data and plot configuration to the
     *  specified stream in PlotML syntax.  PlotML is an XML
     *  scheme for plot data. The URL (relative or absolute) for the DTD is
     *  given as the second argument.  If that argument is null,
     *  then the PlotML PUBLIC DTD is referenced, resulting in a file
     *  that can be read by a PlotML parser without any external file
     *  references, as long as that parser has local access to the DTD.
     *  The output is buffered, and is flushed before exiting.
     *  @param out An output writer.
     *  @param dtd The reference (URL) for the DTD, or null to use the
     *   PUBLIC DTD.
     */
    public void write(Writer out, String dtd);

    /** Write plot data information to the specified output stream in PlotML.
     *  In this base class, there is no data to write, so this method
     *  returns without doing anything.
     *  @param output A buffered print writer.
     */
    public void writeData(PrintWriter output);

    /** Write plot format information to the specified output stream in PlotML.
     *  Derived classes should override this method to first call
     *  the parent class method, then add whatever additional format
     *  information they wish to add to the stream.
     *  @param output A buffered print writer.
     */
    public void writeFormat(PrintWriter output);

    /** Write the current data and plot configuration to the
     *  specified stream in the old PtPlot syntax.
     *  The output is buffered, and is flushed and
     *  closed before exiting.  Derived classes should override
     *  _writeOldSyntax() rather than this method.
     *  @param out An output stream.
     *  @deprecated
     */
    @Deprecated
    public void writeOldSyntax(OutputStream out);

    /** Zoom in or out to the specified rectangle.
     *  This method calls repaint().
     *  @param lowx The low end of the new X range.
     *  @param lowy The low end of the new Y range.
     *  @param highx The high end of the new X range.
     *  @param highy The high end of the new Y range.
     */
    public void zoom(double lowx, double lowy, double highx, double highy);
}
