/* Plot histograms.

@Copyright (c) 1998-2002 The Regents of the University of California.
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

						PT_COPYRIGHT_VERSION 2
						COPYRIGHTENDKEY
@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.gui;

import ptolemy.actor.Manager;
import ptolemy.actor.gui.Placeable;
import ptolemy.actor.lib.Sink;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.*;
import ptolemy.plot.Histogram;
import ptolemy.plot.PlotFrame;
import ptolemy.plot.plotml.HistogramMLParser;

import javax.swing.SwingUtilities;
import java.awt.Container;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

//////////////////////////////////////////////////////////////////////////
//// HistogramPlotter
/**
A histogram plotter.  This plotter contains an instance of the Histogram
class from the Ptolemy plot package as a public member.  A histogram
of data at the input port, which can consist of any number of channels,
is plotted on this instance. The input data type is double.
<p>
The output plot consists of a set of vertical bars, each representing
a histogram bin.  The height of the bar is the count of the number
of inputs that have been observed that fall within that bin.
The <i>n</i>-th bin represents values in the range
(<i>x</i> - <i>w</i>/2 + <i>o</i>, <i>x</i> + <i>w</i>/2 + <i>o</i>),
where <i>w</i> is the value of the <i>binWidth</i> parameter,
and <i>o</i> is the value of the <i>binOffset</i> parameter.
So for example, if <i>o = w/2</i>,
then each bin represents values from <i>nw</i> to
(<i>n</i> + 1)<i>w</i>) for some integer <i>n</i>.
The default offset is 0.5, half the default bin width, which is 1.0.
<p>
This actor has a <i>legend</i> parameter,
which gives a comma-separated list of labels to attach to
each dataset.  Normally, the number of elements in this list
should equal the number of input channels, although this
is not enforced.

@see ptolemy.plot.Histogram

@author  Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/
public class HistogramPlotter extends Sink implements Configurable, Placeable {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public HistogramPlotter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.DOUBLE);
        fillOnWrapup = new Parameter(this, "fillOnWrapup",
                new BooleanToken(true));
        binWidth = new Parameter(this, "binWidth",
                new DoubleToken(1.0));
        binOffset = new Parameter(this, "binOffset",
                new DoubleToken(0.5));
        legend = new StringAttribute(this, "legend");

	_attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-20\" y=\"-20\" "
                + "width=\"40\" height=\"40\" "
                + "style=\"fill:lightGrey\"/>\n"
                + "<rect x=\"-12\" y=\"-12\" "
                + "width=\"24\" height=\"24\" "
                + "style=\"fill:white\"/>\n"
                + "<rect x=\"2\" y=\"-18\" "
                + "width=\"4\" height=\"4\" "
                + "style=\"fill:grey\"/>\n"
                + "<rect x=\"8\" y=\"-18\" "
                + "width=\"4\" height=\"4\" "
                + "style=\"fill:grey\"/>\n"
                + "<rect x=\"14\" y=\"-18\" "
                + "width=\"4\" height=\"4\" "
                + "style=\"fill:grey\"/>\n"
                + "<rect x=\"-8\" y=\"2\" "
                + "width=\"4\" height=\"10\" "
                + "style=\"fill:red\"/>\n"
                + "<rect x=\"-2\" y=\"-8\" "
                + "width=\"4\" height=\"20\" "
                + "style=\"fill:red\"/>\n"
                + "<rect x=\"4\" y=\"-5\" "
                + "width=\"4\" height=\"17\" "
                + "style=\"fill:red\"/>\n"
                + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** If true, fill the histogram when wrapup is called.
     *  This parameter has type boolean and default value true.
     */
    public Parameter fillOnWrapup;

    /** The width of the bin of the histogram.
     *  This parameter has type double, with default value 1.0.
     */
    public Parameter binWidth;

    /** The offset for bins of the histogram.
     *  This parameter has type double, with default value 0.5.
     */
    public Parameter binOffset;

    /** A comma-separated list of labels to attach to each data set.
     *  This is always a string, with no enclosing quotation marks.
     */
    public StringAttribute legend;

    /** The histogram object. */
    public transient Histogram histogram;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the parameter is <i>binWidth</i> or <i>binOffset</i>, then
     *  configure the histogram with the specified bin width or offset.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the bin width is not positive.
     */
    public void attributeChanged(Attribute attribute)
           throws IllegalActionException {
       if (attribute == binWidth) {
           double width = ((DoubleToken)binWidth.getToken()).doubleValue();
           if (width <= 0.0) {
               throw new IllegalActionException(this,
                       "Invalid bin width (must be positive): " + width);
           }
           if (histogram != null) {
               histogram.setBinWidth(width);
           }
       } else if (attribute == binOffset) {
           double offset = ((DoubleToken)binOffset.getToken()).doubleValue();
           if (histogram != null) {
               histogram.setBinOffset(offset);
           }
       } else if (attribute == legend) {
            if (histogram != null) {
                histogram.clearLegends();
                String value = legend.getExpression();
                if (value != null && !value.trim().equals("")) {
                    StringTokenizer tokenizer = new StringTokenizer(value, ",");
                    int channel = 0;
                    while (tokenizer.hasMoreTokens()) {
                        histogram.addLegend(channel++,
                               tokenizer.nextToken().trim());
                    }
                }
            }
       } else {
           super.attributeChanged(attribute);
       }
   }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the public variables.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has an
     *   attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        HistogramPlotter newObject = (HistogramPlotter)super.clone(workspace);
        newObject.histogram = null;
        newObject._container = null;
        newObject._frame = null;
        return newObject;
    }

    /** Configure the histogram with data from the specified input source
     *  (a URL) and/or textual data, assumed to be in PlotML format.
     *  If this is called before the histogram has been created
     *  (by calling place() or initialize()), then reading of the input
     *  stream is deferred until the histogram is created.
     *  @param base The base relative to which references within the input
     *   stream are found, or null if this is not known.
     *  @param source The input source, which specifies a URL.
     *  @param text Configuration information given as text.
     *  @exception Exception If the configuration source cannot be read
     *   or if the configuration information is incorrect.
     */
    public void configure(URL base, String source, String text)
            throws Exception {
        if (histogram != null) {
            HistogramMLParser parser = new HistogramMLParser(histogram);
            if (source != null && !source.equals("")) {
                URL xmlFile = new URL(base, source);
                InputStream stream = xmlFile.openStream();
                parser.parse(base, stream);
                stream.close();
            }
            if (text != null && !text.equals("")) {
                // NOTE: Regrettably, the XML parser we are using cannot
                // deal with having a single processing instruction at the
                // outer level.  Thus, we have to strip it.
                String trimmed = text.trim();
                if (trimmed.startsWith("<?") && trimmed.endsWith("?>")) {
                    trimmed = trimmed.substring(2, trimmed.length() - 2).trim();
                    if (trimmed.startsWith("plotml")) {
                        trimmed = trimmed.substring(6).trim();
                        parser.parse(base, trimmed);
                    }
                    // If it's not a plotml processing instruction, ignore.
                } else {
                    // Data is not enclosed in a processing instruction.
                    // Must have been given in a CDATA section.
                    parser.parse(base, text);
                }
            }
        } else {
            // Defer until histogram has been placed.
            if (_configureBases == null) {
                _configureBases = new LinkedList();
                _configureSources = new LinkedList();
                _configureTexts = new LinkedList();
            }
            _configureBases.add(base);
            _configureSources.add(source);
            _configureTexts.add(text);
        }
    }

    /** Return the input source that was specified the last time the configure
     *  method was called.
     *  @return The string representation of the input URL.
     */
    public String getSource() {
        return null;
    }

    /** Return the text string that represents the current configuration of
     *  this object.  Note that any configuration that was previously
     *  specified using the source attribute need not be returned here.
     */
    public String getText() {
        if (histogram == null) {
            // FIXME
            return "";
        } else {
            // NOTE: Cannot include xml spec in the header because processing
            // instructions cannot be nested in XML (lame, isn't it?).
            String header
            = "<!DOCTYPE plot PUBLIC \"-//UC Berkeley//DTD PlotML 1//EN\"\n"                + "\"http://ptolemy.eecs.berkeley.edu/xml/dtd/PlotML_1.dtd\">";
            StringBuffer buffer = new StringBuffer();
            buffer.append(header);
            buffer.append("\n<plot>\n");
	    PrintWriter print = new PrintWriter(new StringWriter());
	    // The second (null) argument indicates that PlotML PUBLIC DTD
	    // should be referenced.
	    histogram.writeFormat(print);
            buffer.append(print.toString());
            return buffer.toString();
        }
    }

    /** If the histogram has not already been created, create it using
     *  place().
     *  If configurations specified by a call to configure() have not yet
     *  been processed, process them.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (histogram == null || !_placeCalled) {
            place(_container);
        } else {
            // Clear the histogram without clearing the axes.
            histogram.clear(false);
        }
        if (_frame != null) {
	    _frame.setVisible(true);
        }
        histogram.repaint();
    }

    /** Specify the graphical container into which this histogram should be
     *  placed. This method needs to be called before the first call to
     *  initialize(). Otherwise, the histogram will be placed in its own frame.
     *  The histogram is also placed in its own frame if this method
     *  is called with the argument null. If the argument is an instance
     *  of Histogram, then plot data to that instance.  If a container has been
     *  specified but it is not an instance of Histogram, then create a new
     *  instance of Histogram and place it in that container
     *  using its add() method.
     *  <p>
     *  If configure() has been called (prior to the plot getting created),
     *  then the configurations that it specified have been deferred. Those
     *  configurations are performed at this time.
     *
     *  @param container The container into which to place the histogram.
     */
    public void place(Container container) {
        _container = container;
        _placeCalled = true;
        if (_container == null) {
            // Place the histogram in its own frame.
            histogram = new Histogram();
            histogram.setTitle(getName());
            _frame = new PlotFrame(getFullName(), histogram);
	    _frame.setVisible(true);
        } else {
            if (_container instanceof Histogram) {
                histogram = (Histogram)_container;
            } else {
                if (histogram == null) {
                    histogram = new Histogram();
                    histogram.setTitle(getName());
                    histogram.setButtons(true);
                }
                _container.add(histogram);
		// java.awt.Component.setBackground(color) says that
		// if the color "parameter is null then this component
		// will inherit the  background color of its parent."
                //histogram.setBackground(_container.getBackground());
		histogram.setBackground(null);
            }
        }
        // If configurations have been deferred, implement them now.
        if (_configureSources != null) {
            Iterator sources = _configureSources.iterator();
            Iterator texts = _configureTexts.iterator();
            Iterator bases = _configureBases.iterator();
            while (sources.hasNext()) {
                URL base = (URL)bases.next();
                String source = (String)sources.next();
                String text = (String)texts.next();
                try {
                    configure(base, source, text);
                } catch (Exception ex) {
                    getManager().notifyListenersOfException(ex);
                }
            }
            _configureSources = null;
            _configureTexts = null;
            _configureBases = null;
        }
        // Configure the new histogram with parameter values, possibly
        // overriding those set in evaluating the deferred configure.
        try {
            attributeChanged(binWidth);
            attributeChanged(binOffset);
            attributeChanged(legend);
        } catch (IllegalActionException ex) {
            // Safe to ignore because user would
            // have already been alerted.
        }
    }

    /** Read at most one input token from each input channel
     *  and update the histogram.
     *  This is done in postfire to ensure that data has settled.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean postfire() throws IllegalActionException {
        int width = input.getWidth();
        for (int i = width - 1; i >= 0; i--) {
            if (input.hasToken(i)) {
                DoubleToken curToken = (DoubleToken)input.get(i);
                double curValue = curToken.doubleValue();
                histogram.addPoint(i, curValue);
            }
        }
        return super.postfire();
    }

    /** Override the base class to remove the plot from its graphical
     *  container if the argument is null.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the base class throws it.
     *  @exception NameDuplicationException If the base class throws it.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(container);
        if (container == null) {
            _remove();
        }
    }

    /** If the <i>fillOnWrapup</i> parameter is true, rescale the
     *  plot so that all the data is visible.
     *
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void wrapup() throws IllegalActionException {
        if (((BooleanToken)fillOnWrapup.getToken()).booleanValue()) {
            histogram.fillPlot();
        }
        super.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Write a MoML description of the contents of this object, which
     *  in this class is the configuration information. This method is called
     *  by exportMoML().  Each description is indented according to the
     *  specified depth and terminated with a newline character.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     */
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        super._exportMoMLContents(output, depth);
        // NOTE: Cannot include xml spec in the header because processing
        // instructions cannot be nested in XML (lame, isn't it?).
        String header
            = "<!DOCTYPE plot PUBLIC \"-//UC Berkeley//DTD PlotML 1//EN\"\n"                + "\"http://ptolemy.eecs.berkeley.edu/xml/dtd/PlotML_1.dtd\">";
	if (histogram != null) {
	    output.write(_getIndentPrefix(depth) + "<configure>\n<?plotml"
                    + header + "\n<plot>\n");
	    PrintWriter print = new PrintWriter(output);
	    // The second (null) argument indicates that PlotML PUBLIC DTD
	    // should be referenced.
	    histogram.writeFormat(print);
	    output.write("</plot>?>\n"
                    + _getIndentPrefix(depth) + "</configure>\n");
        } else if (_configureSources != null) {
            // Configuration has been specified, but not yet evaluated.
            // Save the configuration just as specified.
            // Note that the bases are lost, since those are presumably
            // the URL of the file containing the XML.
            Iterator sources = _configureSources.iterator();
            Iterator texts = _configureTexts.iterator();
            while (sources.hasNext()) {
                String source = (String)sources.next();
                String text = (String)texts.next();
                if (source != null && !source.trim().equals("")) {
                    output.write(_getIndentPrefix(depth)
                            + "<configure source=\""
                            + source
                            + "\">");
                } else {
                    output.write(_getIndentPrefix(depth) + "<configure>\n");
                }
                if (text != null) {
		    output.write(text.trim() + "\n");
		}
                output.write(_getIndentPrefix(depth) + "</configure>\n");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Remove the histogram from the current container, if there is one.
     */
    private void _remove() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (histogram != null) {
                    if (_container != null) {
                        _container.remove(histogram);
                        _container.invalidate();
                        _container.repaint();
                    } else if (_frame != null) {
                        _frame.dispose();
                    }
                }
            }
        });
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** Container into which this histogram should be placed */
    private transient Container _container;

    // Frame into which plot is placed, if any.
    private transient PlotFrame _frame;

    // The bases and input streams given to the configure() method.
    private List _configureBases = null;
    private List _configureSources = null;
    private List _configureTexts = null;

    // Flag indicating that the place() method has been called at least once.
    private boolean _placeCalled = false;
}
