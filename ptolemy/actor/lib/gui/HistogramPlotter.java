/* Plot histograms.

@Copyright (c) 1998-2003 The Regents of the University of California.
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

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Placeable;
import ptolemy.actor.gui.PlotEffigy;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Configurable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.plot.Histogram;
import ptolemy.plot.PlotBox;
import ptolemy.plot.plotml.HistogramMLParser;

import java.io.InputStream;
import java.net.URL;

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
(<i>n</i> + 1)<i>w</i> for some integer <i>n</i>.
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
public class HistogramPlotter extends PlotterBase
    implements Configurable, Placeable {

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

        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.DOUBLE);

        binWidth = new Parameter(this, "binWidth");
        binWidth.setExpression("1.0");
        binWidth.setTypeEquals(BaseType.DOUBLE);

        binOffset = new Parameter(this, "binOffset");
        binOffset.setExpression("0.5");
        binOffset.setTypeEquals(BaseType.DOUBLE);

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

    /** The width of the bin of the histogram.
     *  This parameter has type double, with default value 1.0.
     */
    public Parameter binWidth;

    /** The offset for bins of the histogram.
     *  This parameter has type double, with default value 0.5.
     */
    public Parameter binOffset;

    /** The input port, which is a multiport. */
    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the parameter is <i>binWidth</i> or <i>binOffset</i>, then
     *  configure the histogram with the specified bin width or offset.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the bin width is not positive.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        // NOTE: Do not react to changes in _windowProperties.
        // Those properties are only used when originally opening a window.
        if (attribute == binWidth) {
            double width = ((DoubleToken)binWidth.getToken()).doubleValue();
            if (width <= 0.0) {
                throw new IllegalActionException(this,
                        "Invalid bin width (must be positive): " + width);
            }
            if (plot instanceof Histogram) {
                ((Histogram)plot).setBinWidth(width);
            }
        } else if (attribute == binOffset) {
            double offset = ((DoubleToken)binOffset.getToken()).doubleValue();
            if (plot instanceof Histogram) {
                ((Histogram)plot).setBinOffset(offset);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Configure the plot with data from the specified input source
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
                if (plot instanceof Histogram) {
                    HistogramMLParser parser = new HistogramMLParser((Histogram)plot);
                    if (source != null && !source.trim().equals("")) {
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
                    super.configure(base, source, text);
                }
            }

    /** Return the input source that was specified the last time the configure
     *  method was called.
     *  @return The string representation of the input URL.
     */
    public String getSource() {
        return null;
    }

    /** If the histogram has not already been created, create it using
     *  place().
     *  If configurations specified by a call to configure() have not yet
     *  been processed, process them.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (plot == null) {
            // Place the histogram in its own frame.
            plot = _newPlot();
            plot.setTitle(getName());
            plot.setButtons(true);
        }
        if (_frame == null && _container == null) {
            // Need an effigy and a tableau so that menu ops work properly.
            Effigy containerEffigy = Configuration.findEffigy(toplevel());
            if (containerEffigy == null) {
                throw new IllegalActionException(this,
                        "Cannot find effigy for top level: "
                        + toplevel().getFullName());
            }
            try {
                PlotEffigy plotEffigy = new PlotEffigy(
                        containerEffigy, containerEffigy.uniqueName("plot"));
                // The default identifier is "Unnamed", which is no good for
                // two reasons: Wrong title bar label, and it causes a save-as
                // to destroy the original window.
                plotEffigy.identifier.setExpression(getFullName());

                PlotWindowTableau tableau = new PlotWindowTableau(
                        plotEffigy, "tableau");
                _frame = tableau.frame;
            } catch (Exception ex) {
                throw new IllegalActionException(this, null, ex,
                        "Error creating effigy and tableau");
            }

            _windowProperties.setProperties(_frame);
            _implementDeferredConfigurations();

            // The SizeAttribute property is used to specify the size
            // of the Plot component. Unfortunately, with Swing's
            // mysterious and undocumented handling of component sizes,
            // there appears to be no way to control the size of the
            // Plot from the size of the Frame, which is specified
            // by the WindowPropertiesAttribute.
            if (_plotSize != null) {
                _plotSize.setSize(plot);
            }
            _frame.pack();
        } else {
            // Clear the histogram without clearing the axes.
            plot.clear(false);
            plot.repaint();
        }
        if (_frame != null) {
            // show() used to override manual placement by calling pack.
            // No more.
            _frame.show();
            _frame.toFront();
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
                // NOTE: Should we test before this cast?
                ((Histogram)plot).addPoint(i, curValue);
            }
        }
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** If configurations have been deferred, implement them now.
     *  Also, configure the histogram parameters, if appropriate.
     */
    protected void _implementDeferredConfigurations() {
        super._implementDeferredConfigurations();
        // Configure the new histogram with parameter values, possibly
        // overriding those set in evaluating the deferred configure.
        try {
            attributeChanged(binWidth);
            attributeChanged(binOffset);
        } catch (IllegalActionException ex) {
            // Safe to ignore because user would
            // have already been alerted.
        }
    }

    /** Create a new Histogram plot.
     *  @return A new plot object.
     */
    protected PlotBox _newPlot() {
        return new Histogram();
    }
}
