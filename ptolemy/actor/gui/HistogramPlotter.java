/* Plot histograms.

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

package ptolemy.actor.gui;

import ptolemy.actor.*;
import ptolemy.actor.lib.Sink;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.*;
import ptolemy.data.expr.*;
import ptolemy.plot.*;
import ptolemy.plot.plotml.HistogramMLParser;

// Java imports.
import java.awt.Container;
import java.io.InputStream;
import java.net.URL;

/**
A histogram plotter.  This plotter contains an instance of the Histogram
class from the Ptolemy plot package as a public member.  A histogram
of data at the input port, which can consist of any number of channels,
is plotted on this instance.

@author  Edward A. Lee
@version $Id$
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
    public HistogramPlotter(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.DOUBLE);
        fillOnWrapup = new Parameter(this, "fillOnWrapup",
                new BooleanToken(true));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** If true, fill the histogram when wrapup is called.
     *  This parameter has type BOOLEAN and default value true.
     */
    public Parameter fillOnWrapup;

    /** The histogram object. */
    public transient Histogram histogram;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the public variables.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has an
     *   attribute that cannot be cloned.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        HistogramPlotter newobj =
            (HistogramPlotter)super.clone(ws);
        newobj.histogram = null;
        newobj._container = null;
        newobj.fillOnWrapup
            = (Parameter)newobj.getAttribute("fillOnWrapup");
        return newobj;
    }

    /** Configure the histogram with data from the specified input stream,
     *  which is assumed to be in PlotML format.  If this is called before
     *  the plotter has been created (by calling place() or initialize()),
     *  then reading of the input stream is deferred until the plotter is
     *  created.
     *  @param base The base relative to which references within the input
     *   stream are found, or null if this is not known.
     *  @param in InputStream
     *  @exception Exception If the stream cannot be read or its syntax
     *   is incorrect.
     */
    public void configure(URL base, InputStream in) throws Exception {
        if (histogram != null) {
            HistogramMLParser parser = new HistogramMLParser(histogram);
            parser.parse(base, in);
            _configureBase = null;
            _configureIn = null;
        } else {
            // Defer until histogram has been placed.
            _configureBase = base;
            _configureIn = in;
        }
    }

    /** If the histogram has not already been created, create it using
     *  place().
     *  If configuration specified by a call to configure() has not yet
     *  been processed, process it.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (histogram == null) {
            place(_container);
        } else {
            // Clear the histogram without clearing the axes.
            histogram.clear(false);
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
     *  then the configuration that it specified has been deferred. That
     *  configuration is performed at this time.
     *
     *  @param container The container into which to place the histogram.
     */
    public void place(Container container) {
        _container = container;
        if (_container == null) {
            // Place the histogram in its own frame.
            histogram = new Histogram();
            PlotFrame frame = new PlotFrame(getFullName(), histogram);
        } else {
            if (_container instanceof Histogram) {
                histogram = (Histogram)_container;
            } else {
                histogram = new Histogram();
                _container.add(histogram);
                histogram.setButtons(true);
                histogram.setBackground(_container.getBackground());
            }
        }
        // If configure() has been deferred, implement it now.
        if (_configureIn != null) {
            try {
                configure(_configureBase, _configureIn);
            } catch (Exception ex) {
                getManager().notifyListenersOfException(ex);
            }
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

    /** If the <i>fillOnWrapup</i> parameter is true, rescale the
     *  plot so that all the data is visible.
     *
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void wrapup() throws IllegalActionException {
        if(((BooleanToken)fillOnWrapup.getToken()).booleanValue()) {
            histogram.fillPlot();
        }
        super.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** Container into which this histogram should be placed */
    private transient Container _container;

    // The base and input stream given to the configure() method.
    private URL _configureBase = null;
    private InputStream _configureIn = null;
}
