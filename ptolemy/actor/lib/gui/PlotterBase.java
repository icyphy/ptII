/* Base class for plotters.

 @Copyright (c) 1998-2014 The Regents of the University of California.
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
 */
package ptolemy.actor.lib.gui;

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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.injection.ActorModuleInitializer;
import ptolemy.actor.injection.PortableContainer;
import ptolemy.actor.injection.PortablePlaceable;
import ptolemy.actor.injection.PtolemyInjector;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Configurable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.plot.PlotBoxInterface;
import ptolemy.plot.PlotInterface;
import ptolemy.plot.plotml.PlotMLParser;

///////////////////////////////////////////////////////////////////
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
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (cxh)
 */
public class PlotterBase extends TypedAtomicActor implements Configurable,
PortablePlaceable {
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

        fillOnWrapup = new Parameter(this, "fillOnWrapup", new BooleanToken(
                true));
        fillOnWrapup.setTypeEquals(BaseType.BOOLEAN);

        automaticRescale = new Parameter(this, "automaticRescale",
                new BooleanToken(false));
        automaticRescale.setTypeEquals(BaseType.BOOLEAN);

        legend = new StringAttribute(this, "legend");

        _getImplementation().initWindowAndSizeProperties();

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-20\" y=\"-20\" " + "width=\"40\" height=\"40\" "
                + "style=\"fill:lightGrey\"/>\n" + "<rect x=\"-12\" y=\"-12\" "
                + "width=\"24\" height=\"24\" " + "style=\"fill:white\"/>\n"
                + "<rect x=\"2\" y=\"-18\" " + "width=\"4\" height=\"4\" "
                + "style=\"fill:grey\"/>\n" + "<rect x=\"8\" y=\"-18\" "
                + "width=\"4\" height=\"4\" " + "style=\"fill:grey\"/>\n"
                + "<rect x=\"14\" y=\"-18\" " + "width=\"4\" height=\"4\" "
                + "style=\"fill:grey\"/>\n"
                + "<polyline points=\"-10,0, -5,-8, 5,8, 10,0\" "
                + "style=\"stroke:red\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** If true, the plot will automatically rescale if necessary.
     *  This parameter has type BooleanToken, and default value false.
     */
    public Parameter automaticRescale;

    /** The plot object. */
    public transient PlotBoxInterface plot;

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
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
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

    /** Free up memory when closing. */
    public void cleanUp() {
        setFrame(null);
        _getImplementation().cleanUp();
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has an
     *   attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        PlotterBase newObject = (PlotterBase) super.clone(workspace);

        newObject._configureBases = null;
        newObject._configureSources = null;
        newObject._configureTexts = null;
        newObject._implementation = null;
        newObject.plot = null;
        try {
            if (_base != null) {
                newObject._base = new URL(_base.toString());
            }
            newObject.configure(newObject._base, _source, _text);

            // See _getImplementation():
            if (PtolemyInjector.getInjector() == null) {
                System.err.println("Warning: main() did not call "
                        + "ActorModuleInitializer.initializeInjector(), "
                        + "so PlotterBase.clone() is calling it for you.");
                ActorModuleInitializer.initializeInjector();
            }
            newObject._implementation = PtolemyInjector.getInjector()
                    .getInstance(PlotterBaseInterface.class);
            newObject._implementation.init(newObject);

            newObject._implementation.initWindowAndSizeProperties();

        } catch (Exception e) {
            // This should not occur.
            throw new CloneNotSupportedException("Clone failed: " + e);
        }
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
    @Override
    public void configure(URL base, String source, String text)
            throws Exception {
        _base = base;
        _source = source;
        _text = text;

        if (plot instanceof PlotInterface) {
            PlotMLParser parser = new PlotMLParser((PlotInterface) plot);

            if (source != null && !source.trim().equals("")) {
                URL xmlFile = new URL(base, source);
                InputStream stream = xmlFile.openStream();
                parser.parse(base, stream);
                stream.close();
                _configureSource = source;
            }

            // Avoid trying to parse whitespace only text.
            if (text != null) {
                String trimmed = text.trim();
                if (trimmed != null && !trimmed.equals("")) {
                    // NOTE: Regrettably, the XML parser we are using cannot
                    // deal with having a single processing instruction at the
                    // outer level.  Thus, we have to strip it.

                    if (trimmed.startsWith("<?") && trimmed.endsWith("?>")) {
                        trimmed = trimmed.substring(2, trimmed.length() - 2)
                                .trim();

                        if (trimmed.startsWith("plotml")) {
                            trimmed = trimmed.substring(6).trim();
                            parser.parse(base, trimmed);
                        }

                        // If it's not a plotml processing instruction, ignore.
                    } else {
                        // Data is not enclosed in a processing instruction.
                        // Must have been given in a CDATA section.
                        parser.parse(base, trimmed);
                    }
                }
            }
        } else {
            // Defer until plot has been placed.
            if (_configureBases == null) {
                _configureBases = new LinkedList<URL>();
                _configureSources = new LinkedList<String>();
                _configureTexts = new LinkedList<String>();
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
    @Override
    public String getConfigureSource() {
        return _configureSource;
    }

    /** Return the text string that represents the current configuration of
     *  this object.  Note that any configuration that was previously
     *  specified using the source attribute need not be returned here.
     *  This returns a null string if there is no associated plot.
     *  @return The text string that represent the current configuration.
     */
    @Override
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
    @Override
    public void place(PortableContainer container) {
        _getImplementation().setPlatformContainer(
                container != null ? container.getPlatformContainer() : null);
        _getImplementation().removeNullContainer();

        if (container != null) {
            if (container.getPlatformContainer() instanceof PlotBoxInterface) {
                // According to FindBugs the cast is an error:
                //  [M D BC] Unchecked/unconfirmed cast [BC_UNCONFIRMED_CAST]
                // However it is checked that _container instanceof PlotBox,
                // so FindBugs is wrong.
                plot = (PlotBoxInterface) container.getPlatformContainer();
                plot.setButtons(true);
            } else {
                if (plot == null) {
                    plot = _newPlot();
                    plot.setTitle(getName());
                }

                plot.setButtons(true);
                container.add(plot);
                // java.awt.Component.setBackground(color) says that
                // if the color "parameter is null then this component
                // will inherit the  background color of its parent."
                plot.setBackground(null);
            }

            // If the container is non-null and configurations have
            // been deferred, implement them now.
            // There was a bug here where we got a ConcurrentModificationException
            // because ModelPane.setModel() called ModelPane._closeDisplays(),
            // which called PlotterBase.place(null) and then _implementDeferredConfigurations
            // iterated through the list of bases while it was being modified.
            // To replicate, do open Spectrum.xml and do View -> Run Window.
            _implementDeferredConfigurations();
        }
    }

    /** Clear the plot, if there is one.  Notice that unlike
     *  initialize(), this clears the entire plot.
     *  @see #initialize()
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        if (plot != null) {
            // Do not clear the axes.
            plot.clear(false);
            plot.repaint();

            if (((BooleanToken) automaticRescale.getToken()).booleanValue()) {
                plot.setAutomaticRescale(true);
            }
        }

    }

    /** Override the base class to remove the plot from its graphical
     *  container if the argument is null.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the base class throws it.
     *  @exception NameDuplicationException If the base class throws it.
     */
    @Override
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        Nameable previousContainer = getContainer();
        super.setContainer(container);

        if (container != previousContainer && previousContainer != null) {
            _remove();
        }
    }

    /** Specify the associated frame and set its properties (size, etc.)
     *  to match those stored in the _windowProperties attribute.
     *  @param frame The associated frame.
     */
    public void setFrame(Object frame) {
        _getImplementation().setFrame(frame);
    }

    /** Set a name to present to the user.
     *  <p>If the Plot window has been rendered, then the title of the
     *  Plot window will be updated to the value of the name parameter.</p>
     *  @param name A name to present to the user.
     *  @see #getDisplayName()
     */
    @Override
    public void setDisplayName(String name) {
        super.setDisplayName(name);
        // See http://bugzilla.ecoinformatics.org/show_bug.cgi?id=4302
        _getImplementation().setTableauTitle(name);
    }

    /** Set or change the name.  If a null argument is given the
     *  name is set to an empty string.
     *  Increment the version of the workspace.
     *  This method is write-synchronized on the workspace.
     *  <p>If the Plot window has been rendered, then the title of the
     *  Plot window will be updated to the value of the name parameter.</p>
     *  @param name The new name.
     *  @exception IllegalActionException If the name contains a period
     *   or if the object is a derived object and the name argument does
     *   not match the current name.
     *  @exception NameDuplicationException Not thrown in this base class.
     *   May be thrown by derived classes if the container already contains
     *   an object with this name.
     *  @see #getName()
     *  @see #getName(NamedObj)
     */
    @Override
    public void setName(String name) throws IllegalActionException,
    NameDuplicationException {
        super.setName(name);
        // See http://bugzilla.ecoinformatics.org/show_bug.cgi?id=4302
        _getImplementation().setTableauTitle(name);
    }

    /** If the <i>fillOnWrapup</i> parameter is true, rescale the
     *  plot so that all the data is visible.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        if (((BooleanToken) fillOnWrapup.getToken()).booleanValue()) {
            if (plot != null) {
                plot.fillPlot();
            }
        }

        if (plot != null) {
            // If we are generating code, then plot might be null;
            plot.setAutomaticRescale(false);
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
    @Override
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        // Make sure that the current position of the frame, if any,
        // is up to date.
        _getImplementation().updateWindowAndSizeAttributes();
        super._exportMoMLContents(output, depth);

        // NOTE: Cannot include xml spec in the header because processing
        // instructions cannot be nested in XML (lame, isn't it?).
        String header = "<!DOCTYPE plot PUBLIC \"-//UC Berkeley//DTD PlotML 1//EN\"\n"
                + "\"http://ptolemy.eecs.berkeley.edu/xml/dtd/PlotML_1.dtd\">";

        if (plot != null) {
            output.write(_getIndentPrefix(depth) + "<configure>\n<?plotml "
                    + header + "\n<plot>\n");

            PrintWriter print = new PrintWriter(output);

            // The second (null) argument indicates that PlotML PUBLIC DTD
            // should be referenced.
            plot.writeFormat(print);
            output.write("</plot>?>\n" + _getIndentPrefix(depth)
                    + "</configure>\n");
        } else if (_configureSources != null) {
            // Configuration has been specified, but not yet evaluated.
            // Save the configuration just as specified.
            // Note that the bases are lost, since those are presumably
            // the URL of the file containing the XML.
            Iterator<String> sources = _configureSources.iterator();
            Iterator<String> texts = _configureTexts.iterator();

            while (sources.hasNext()) {
                String source = sources.next();
                String text = texts.next();

                if (source != null && !source.trim().equals("")) {
                    output.write(_getIndentPrefix(depth)
                            + "<configure source=\"" + source + "\">");

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

    /** Get the right instance of the implementation depending upon the
     *  of the dependency specified through dependency injection.
     *  If the instance has not been created, then it is created.
     *  If the instance already exists then return the same.
     *
     *        <p>This code is used as part of the dependency injection needed for the
     *  HandSimDroid project, see $PTII/ptserver.  This code uses dependency
     *  inject to determine what implementation to use at runtime.
     *  This method eventually reads ptolemy/actor/ActorModule.properties.
     *  {@link ptolemy.actor.injection.ActorModuleInitializer#initializeInjector()}
     *  should be called before this method is called.  If it is not
     *  called, then a message is printed and initializeInjector() is called.</p>
     *  @return the implementation.
     */
    protected PlotterBaseInterface _getImplementation() {
        if (_implementation == null) {
            if (PtolemyInjector.getInjector() == null) {
                System.err.println("Warning: main() did not call "
                        + "ActorModuleInitializer.initializeInjector(), "
                        + "so PlotterBase is calling it for you.");
                ActorModuleInitializer.initializeInjector();
            }
            _implementation = PtolemyInjector.getInjector().getInstance(
                    PlotterBaseInterface.class);
            _implementation.init(this);
        }
        return _implementation;
    }

    /** If configurations have been deferred, implement them now.
     *  Also, configure the plot legends, if appropriate.
     */
    protected void _implementDeferredConfigurations() {
        if (_configureSources != null) {

            // Coverity indicates that configure() can modify
            // _configureSources, _configureTexts and _configureBases,
            // which would invalidate the iterators because the
            // underlying list would be modified.  However, the logic
            // of this program is such that configure() would only add
            // to these lists if plot was not yet a PlotInterface.
            // However, place() sets plot to a plotter and then calls
            // this method, so logically, the underlying lists cannot
            // be moderated.  However, derived classes could do
            // something different, so copying the lists is a good
            // idea.

            Iterator<String> sources = new LinkedList<String>(_configureSources)
                    .iterator();
            Iterator<String> texts = new LinkedList<String>(_configureTexts)
                    .iterator();
            Iterator<URL> bases = new LinkedList<URL>(_configureBases)
                    .iterator();

            while (sources.hasNext()) {
                URL base = bases.next();
                String source = sources.next();
                String text = texts.next();
                try {
                    configure(base, source, text);
                } catch (Exception ex) {
                    System.out.println("Failed to parse? base: \"" + base
                            + "\": source:\"" + source + "text:\"" + text
                            + "\"");
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

    /** Override the base class to ensure that MoML is produced
     *  if there is configuration information to export.
     *  @param depth The depth.
     *  @return True to export MoML.
     */
    @Override
    protected boolean _isMoMLSuppressed(int depth) {
        if (plot != null || _configureSources != null) {
            return false;
        }
        return super._isMoMLSuppressed(depth);
    }

    /** Create a new plot. In this base class, it is an instance of Plot.
     *  In derived classes, it can be classes derived from Plot.
     *  @return A new plot object.
     */
    protected PlotBoxInterface _newPlot() {
        return _getImplementation().newPlot();
    }

    /** Propagate the value of this object to the
     *  specified object. The specified object is required
     *  to be an instance of the same class as this one, or
     *  a ClassCastException will be thrown.
     *  @param destination Object to which to propagate the
     *   value.
     *  @exception IllegalActionException If the value cannot
     *   be propagated.
     */
    @Override
    protected void _propagateValue(NamedObj destination)
            throws IllegalActionException {
        try {
            ((Configurable) destination).configure(_base, _source, _text);
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex, "Propagation failed.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** The base specified in configure(). */
    protected URL _base;

    /** The source specified in configure(). */
    protected String _source;

    /** The text specified in configure(). */
    protected String _text;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Remove the plot from the current container, if there is one.
     */
    private void _remove() {
        _getImplementation().remove();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    // The bases and input streams given to the configure() method.
    private List<URL> _configureBases = null;

    private List<String> _configureSources = null;

    private List<String> _configureTexts = null;

    private String _configureSource;

    /** Implementation of the PlotterBaseInterface.  This code is used as part
     *  of the dependency injection needed for the HandSimDroid project, see
     *  $PTII/ptserver.
     */
    private PlotterBaseInterface _implementation;
}
