/* Base class for plotters.

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

import java.awt.Container;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.SwingUtilities;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.gui.Placeable;
import ptolemy.actor.gui.PlotEffigy;
import ptolemy.actor.gui.PlotTableau;
import ptolemy.actor.gui.PlotTableauFrame;
import ptolemy.actor.gui.SizeAttribute;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.WindowPropertiesAttribute;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Configurable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.plot.Plot;
import ptolemy.plot.PlotBox;
import ptolemy.plot.plotml.PlotMLParser;

//////////////////////////////////////////////////////////////////////////
//// PlotterBase
/**
Base class for plotters.  This class contains an instance of the
PlotBox class from the Ptolemy plot package as a public member,
although which subclass of PlotBox is created is left to derived classes.
It provides a parameter that determines whether to fill the plot
when wrapup is invoked. It also has a <i>legend</i> parameter,
which gives a comma-separated list of labels to attach to
each dataset.  Normally, the number of elements in this list
should equal the number of input channels, although this
is not enforced.

@see ptolemy.plot.PlotBox

@author  Edward A. Lee
@version $Id$
@since Ptolemy II 2.1
*/
public class PlotterBase extends TypedAtomicActor
    implements Configurable, Placeable {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public PlotterBase(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        fillOnWrapup = new Parameter(this, "fillOnWrapup",
                new BooleanToken(true));
        fillOnWrapup.setTypeEquals(BaseType.BOOLEAN);

        legend = new StringAttribute(this, "legend");

        _windowProperties = new WindowPropertiesAttribute(
                this, "_windowProperties");

        _plotSize = new SizeAttribute(this, "_plotSize");

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
                + "<polyline points=\"-10,0, -5,-8, 5,8, 10,0\" "
                + "style=\"stroke:red\"/>\n"
                + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The plot object. */
    public transient PlotBox plot;

    /** If true, fill the plot when wrapup is called.
     *  This parameter has type BooleanToken, and default value true.
     */
    public Parameter fillOnWrapup;

    /** A comma-separated list of labels to attach to each data set.
     *  This is always a string, with no enclosing quotation marks.
     */
    public StringAttribute legend;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is <i>legend</i>, then parse the string
     *  and set the legend.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        // NOTE: Do not react to changes in _windowProperties.
        // Those properties are only used when originally opening a window.
        if (attribute == legend) {
            if (plot != null) {
                plot.clearLegends();
                String value = legend.getExpression();
                if (value != null && !value.trim().equals("")) {
                    StringTokenizer tokenizer = new StringTokenizer(value, ",");
                    int channel = 0;
                    while (tokenizer.hasMoreTokens()) {
                        plot.addLegend(channel++, tokenizer.nextToken().trim());
                    }
                }
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has an
     *   attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        PlotterBase newObject = (PlotterBase)super.clone(workspace);
        newObject.plot = null;
        newObject._container = null;
        newObject._frame = null;
        return newObject;
    }

    /** Configure the object with data from the specified input source
     *  (a URL) and/or textual data, assumed to be in PlotML format.
     *  If this is called before the plotter has been created
     *  (by calling place() or initialize()), then the configuration
     *  is deferred until the plotter is created.
     *  @param base The base relative to which references within the input
     *   are found, or null if this is not known, or there is none.
     *  @param source The input source, which specifies a URL.
     *  @param text Configuration information given as text.
     *  @exception Exception If the configuration source cannot be read
     *   or if the configuration information is incorrect.
     */
    public void configure(URL base, String source, String text)
            throws Exception {
        if (plot instanceof Plot) {
            PlotMLParser parser = new PlotMLParser((Plot)plot);
            if (source != null && !source.trim().equals("")) {
                URL xmlFile = new URL(base, source);
                InputStream stream = xmlFile.openStream();
                parser.parse(base, stream);
                stream.close();
                _configureSource = source;
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
            // Defer until plot has been placed.
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
    public String getConfigureSource() {
        return _configureSource;
    }

    /** Return the text string that represents the current configuration of
     *  this object.  Note that any configuration that was previously
     *  specified using the source attribute need not be returned here.
     *  This returns a null string if there is no associated plot.
     */
    public String getConfigureText() {
        if (plot == null) {
            // NOTE: Is this the right thing to do?
            return "";
        } else {
            // NOTE: Cannot include xml spec in the header because processing
            // instructions cannot be nested in XML (lame, isn't it?).
            //String header
            //    = "<!DOCTYPE plot PUBLIC \"-//UC Berkeley//DTD PlotML 1//EN\"\n"                + "\"http://ptolemy.eecs.berkeley.edu/xml/dtd/PlotML_1.dtd\">";
            StringWriter writer = new StringWriter();
            PrintWriter print = new PrintWriter(writer);
            // NOTE: Cannot include xml spec in the header because processing
            // instructions cannot be nested in XML (lame, isn't it?).
            //print.write(header);
            print.write("\n<plot>\n");
            // The second (null) argument indicates that PlotML PUBLIC DTD
            // should be referenced.
            plot.writeFormat(print);
            print.write("</plot>\n");
            return writer.toString();
        }
    }

    /** Specify the container into which this plot should be placed.
     *  This method needs to be called before the first call to initialize().
     *  Otherwise, the plot will be placed in its own frame.
     *  The size of the plot, unfortunately, cannot be effectively
     *  determined from the size of the container because the
     *  container may not yet be laid out (its size will be zero).
     *  Thus, you will have to explicitly
     *  set the size of the plot by calling plot.setSize().
     *  The background of the plot is set equal to that of the container
     *  (unless it is null).
     *  <p>
     *  If configure() has been called (prior to the plot getting created),
     *  then the configurations that it specified have been deferred. Those
     *  configurations are performed at this time.
     *
     *  @param container The container into which to place the plot, or
     *   null to specify that a new plot should be created.
     */
    public void place(Container container) {
        _container = container;
        // NOTE: This actor always shows the plot buttons, even if
        // the plot is in a separate frame.  They are very useful.
        if (_container == null) {
            // Dissociate with any container.
            // NOTE: _remove() doesn't work here.  Why?
            if (_frame != null) _frame.dispose();
            _frame = null;
            // If we forget the plot, then its properties get lost.
            // Also, if the window is deleted during a run, the data
            // will be lost. So do not forget the plot.
            // plot = null;
            return;
        }
        if (_container instanceof PlotBox) {
            plot = (PlotBox)_container;
            plot.setButtons(true);
        } else {
            if (plot == null) {
                plot = _newPlot();
                plot.setTitle(getName());
            }
            plot.setButtons(true);
            _container.add(plot);
            // java.awt.Component.setBackground(color) says that
            // if the color "parameter is null then this component
            // will inherit the  background color of its parent."
            plot.setBackground(null);
        }
        // If configurations have been deferred, implement them now.
        _implementDeferredConfigurations();
    }

    /** Clear the plot, if there is one.  Notice that unlike
     *  initialize(), this clears the entire plot.
     *  @see #initialize()
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        if (plot != null) {
            // Do not clear the axes.
            plot.clear(false);
            plot.repaint();
        }
    }

    /** Override the base class to remove the plot from its graphical
     *  container if the argument is null.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the base class throws it.
     *  @exception NameDuplicationException If the base class throws it.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        Nameable previousContainer = getContainer();
        super.setContainer(container);
        if (container != previousContainer && previousContainer != null) {
            _remove();
        }
    }

    /** If the <i>fillOnWrapup</i> parameter is true, rescale the
     *  plot so that all the data is visible.
     *  @throws IllegalActionException If the superclass throws it.
     */
    public void wrapup() throws IllegalActionException {
        if (((BooleanToken)fillOnWrapup.getToken()).booleanValue()) {
            if (plot != null) {
                plot.fillPlot();
            }
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
        // Make sure that the current position of the frame, if any,
        // is up to date.
        if (_frame != null) {
            _windowProperties.recordProperties(_frame);
        }
        if (plot != null) {
            _plotSize.recordSize(plot);
        }
        super._exportMoMLContents(output, depth);
        // NOTE: Cannot include xml spec in the header because processing
        // instructions cannot be nested in XML (lame, isn't it?).
        String header =
            "<!DOCTYPE plot PUBLIC \"-//UC Berkeley//DTD PlotML 1//EN\"\n"
            + "\"http://ptolemy.eecs.berkeley.edu/xml/dtd/PlotML_1.dtd\">";
        if (plot != null) {
            output.write(_getIndentPrefix(depth) + "<configure>\n<?plotml "
                    + header + "\n<plot>\n");
            PrintWriter print = new PrintWriter(output);
            // The second (null) argument indicates that PlotML PUBLIC DTD
            // should be referenced.
            plot.writeFormat(print);
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
                    if (text != null) {
                        output.write("<![CDATA[\n");
                    }
                } else {
                    output.write(_getIndentPrefix(depth) + "<configure>\n");
                }
                if (text != null) {
                    output.write(text.trim() + "\n");
                    if (source != null && !source.trim().equals("")) {
                        output.write(_getIndentPrefix(depth) + "]]>\n");
                    }
                }
                output.write(_getIndentPrefix(depth) + "</configure>\n");
            }
        }
    }

    /** If configurations have been deferred, implement them now.
     *  Also, configure the plot legends, if appropriate.
     */
    protected void _implementDeferredConfigurations() {
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
        // Configure the new plot with legends, if appropriate.
        try {
            attributeChanged(legend);
        } catch (IllegalActionException ex) {
            // Safe to ignore because user would
            // have already been alerted.
        }
    }

    /** Create a new plot. In this base class, it is an instance of Plot.
     *  In derived classes, it can be classes derived from Plot.
     *  @return A new plot object.
     */
    protected PlotBox _newPlot() {
        return new Plot();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** Container into which this plot should be placed */
    protected Container _container;

    /** Frame into which plot is placed, if any. */
    protected transient PlotTableauFrame _frame;

    /** A specification of the size of the plot if it's in its own window. */
    protected SizeAttribute _plotSize;

    /** A specification for the window properties of the frame.
     */
    protected WindowPropertiesAttribute _windowProperties;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Remove the plot from the current container, if there is one.
     */
    private void _remove() {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (plot != null) {
                        if (_container != null) {
                            _container.remove(plot);
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

    // The bases and input streams given to the configure() method.
    private List _configureBases = null;
    private List _configureSources = null;
    private List _configureTexts = null;

    private String _configureSource;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Version of Plot class that removes its association with the
     *  plot upon closing, and also records the size of the plot window.
     */
    protected class PlotterPlotFrame extends PlotTableauFrame {

        /** Construct a plot frame in the corresponding Tableau with the
         *  specified instance of PlotBox.
         *  After constructing this, it is necessary
         *  to call setVisible(true) to make the plot appear.
         *  @param tableau The tableau.
         *  @param plotBox the plot object to put in the frame,
         *   or null to create an instance of Plot.
         */
        public PlotterPlotFrame(Tableau tableau, PlotBox plotBox) {
            super(tableau, plotBox);
        }

        /** Close the window.  This overrides the base class to remove
         *  the association with the Display and to record window properties.
         */
        protected boolean _close() {
            // Record the window properties before closing.
            if (_frame != null) {
                _windowProperties.recordProperties(_frame);
            }
            if (PlotterBase.this.plot != null) {
                _plotSize.recordSize(PlotterBase.this.plot);
            }
            boolean result = super._close();
            place(null);
            return result;
        }
    }

    /** Tableau that creates a PlotterPlotFrame.
     */
    protected class PlotWindowTableau extends PlotTableau {

        /** Construct a new tableau for the model represented by the
         *  given effigy.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container does not accept
         *   this entity (this should not occur).
         *  @exception NameDuplicationException If the name coincides with an
         *   attribute already in the container.
         */
        public PlotWindowTableau(PlotEffigy container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
            frame = new PlotterPlotFrame(this, plot);
            setFrame(frame);
        }

        public PlotterPlotFrame frame;
    }
}
