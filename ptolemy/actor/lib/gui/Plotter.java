/* Base class for plotters.

@Copyright (c) 1998-2000 The Regents of the University of California.
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
import java.awt.BorderLayout;
import javax.swing.JPanel;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;
import ptolemy.actor.gui.Placeable;
import ptolemy.plot.*;
import ptolemy.plot.plotml.PlotMLParser;

/**
Base class for plotters.  This class contains an instance of the
Plot class from the Ptolemy plot package as a public member.
It provides a parameter that determines whether to fill the plot
when wrapup is invoked.  It also provides a parameter
<i>startingDataset</i>, which specifies the starting point
for the number of the dataset to use to create the plots.
This defaults to zero, but will typically be set to a positive
number when more than one instance of a plotter actor shares
the same plot object.

@author  Edward A. Lee
@version $Id$
 */
public class Plotter extends TypedAtomicActor
    implements Configurable, Placeable {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Plotter(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        fillOnWrapup = new Parameter(this, "fillOnWrapup",
                new BooleanToken(true));
        fillOnWrapup.setTypeEquals(BaseType.BOOLEAN);
        startingDataset = new Parameter(this, "startingDataset",
                new IntToken(0));
        startingDataset.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The plot object. */
    public transient Plot plot;

    /** If true, fill the plot when wrapup is called.
     *  This parameter has type BooleanToken, and default value true.
     */
    public Parameter fillOnWrapup;

    /** The starting dataset number to which data is plotted.
     *  This parameter has type IntToken, with default value 0.
     *  Its value must be non-negative.
     */
    public Parameter startingDataset;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Throw an exception if the specified new value for
     *  <i>startingDataset</i> is negative.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>startingDataset</i> and its value is negative.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == startingDataset) {
            if(((IntToken)startingDataset.getToken()).intValue() < 0) {
                throw new IllegalActionException(this,
                        "startingDataset: negative value is not allowed.");
            }
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has an
     *   attribute that cannot be cloned.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        Plotter newobj = (Plotter)super.clone(ws);
        newobj.plot = null;
        newobj._frame = null;
        newobj.fillOnWrapup
            = (Parameter)newobj.getAttribute("fillOnWrapup");
        newobj.startingDataset
            = (Parameter)newobj.getAttribute("startingDataset");
        return newobj;
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
        if (plot != null) {
            PlotMLParser parser = new PlotMLParser(plot);
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

    /** If the plot has not already been created, create it.
     *  If configurations specified by a call to configure() have not yet
     *  been processed, process them.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (plot == null) {
            place(_container);
        }
        if (_frame != null) {
	    _frame.setVisible(true);
        }
        int width = plot.getNumDataSets();
        int offset = ((IntToken)startingDataset.getToken()).intValue();
        for (int i = width - 1; i >= 0; i--) {
            plot.clear(i + offset);
        }
        plot.repaint();
    }

    /** Specify the container into which this plot should be placed.
     *  This method needs to be called before the first call to initialize().
     *  Otherwise, the plot will be placed in its own frame.
     *  The plot is also placed in its own frame if this method
     *  is called with a null argument.  The size of the plot,
     *  unfortunately, cannot be effectively determined from the size
     *  of the container because the container may not yet be laid out
     *  (its size will be zero).  Thus, you will have to explicitly
     *  set the size of the plot by calling plot.setSize().
     *  The background of the plot is set equal to that of the container
     *  (unless it is null).
     *  <p>
     *  If configure() has been called (prior to the plot getting created),
     *  then the configurations that it specified have been deferred. Those
     *  configurations are performed at this time.
     *
     *  @param container The container into which to place the plot.
     */
    public void place(Container container) {
        _container = container;
        if (_container == null) {
            // place the plot in its own frame.
            plot = new Plot();
            _frame = new PlotFrame(getFullName(), plot);
	    _frame.setVisible(true);
        } else {
            if (_container instanceof Plot) {
                plot = (Plot)_container;
            } else {
                if (plot == null) {
                    plot = new Plot();
                    plot.setButtons(true);
                }
                _container.add(plot);
                plot.setBackground(_container.getBackground());
            }
        }
        // If configurations have been deferred, implement them now.
        if (_configureSources != null) {
            Iterator sources = _configureSources.iterator();
            Iterator texts = _configureTexts.iterator();
            Iterator bases = _configureBases.iterator();
            while(sources.hasNext()) {
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
    }

    /** If the <i>fillOnWrapup</i> parameter is true, rescale the
     *  plot so that all the data is visible.
     */
    public void wrapup() {
        try {
            if(((BooleanToken)fillOnWrapup.getToken()).booleanValue()) {
                if (plot != null) {
                    plot.fillPlot();
                }
            }
        } catch (IllegalActionException ex) {
            // fillOnWrapup does not evaluate to a valid token,
            // skip fillPlot()
        }
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
        output.write(_getIndentPrefix(depth) + "<configure><?plotml\n"
                + header + "\n<plot>\n");
        PrintWriter print = new PrintWriter(output);
        // The second (null) argument indicates that PlotML PUBLIC DTD
        // should be referenced.
        plot.writeFormat(print);
        output.write("</plot>?>\n"
               + _getIndentPrefix(depth) + "</configure>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** Container into which this plot should be placed */
    protected Container _container;

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // Frame into which plot is placed, if any.
    private transient PlotFrame _frame;

    // The bases and input streams given to the configure() method.
    private List _configureBases = null;
    private List _configureSources = null;
    private List _configureTexts = null;
}
