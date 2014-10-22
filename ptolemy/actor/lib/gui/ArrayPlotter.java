/* Plot arrays of doubles.

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

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.SequenceActor;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.plot.PlotInterface;

///////////////////////////////////////////////////////////////////
//// ArrayPlotter

/**
 <p>A plotter that plots a sequence of arrays of doubles.
 This plotter contains an instance of the Plot
 class from the Ptolemy plot package as a public member. Data at
 the input, which can consist of any number of channels, are plotted
 on this instance.  Each input channel is plotted as a separate data set.
 Each input token is an array of doubles.</p>
 <p>
 The <i>iterationsPerUpdate</i> parameter can be used to fine tune
 the display.  It can be quite expensive to generate the display, and
 by default, this actor generates it on every firing.  If
 <i>iterationsPerUpdate</i> is set to some integer greater than
 one, then it specifies how many iterations should be executed
 between updates. Thus, if <i>iterationsPerUpdate</i> = 2, then every
 second time this actor fires, it will update the display. That is,
 it will update its display on the first firing, the third, the
 fifth, etc. It will, however, consume its inputs on every firing.
 The plot is always updated in the wrapup() method.</p>
 <p>
 Note that this can be used to generate live plots, like SequenceScope,
 but it has fewer drawing artifacts than SequenceScope since it does
 not use XOR drawing mode.</p>

 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 @see SequenceScope
 */
public class ArrayPlotter extends Plotter implements SequenceActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ArrayPlotter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Create the input port and make it a multiport.
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.DOUBLE);
        input.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        iterationsPerUpdate = new Parameter(this, "iterationsPerUpdate");
        iterationsPerUpdate.setExpression("1");

        // set the parameters
        xInit = new Parameter(this, "xInit", new DoubleToken(0.0));
        xInit.setTypeEquals(BaseType.DOUBLE);
        xUnit = new Parameter(this, "xUnit", new DoubleToken(1.0));
        xUnit.setTypeEquals(BaseType.DOUBLE);

        // initialize the parameters
        attributeChanged(xInit);
        attributeChanged(xUnit);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input port, which receives an array of doubles. */
    public TypedIOPort input;

    /** The number of iterations between updates of the display
     *  on the screen.
     *  This parameter has type IntToken, with default value 1.
     *  Its value must be non-negative.
     */
    public Parameter iterationsPerUpdate;

    /** The increment of the X axis. */
    public Parameter xUnit;

    /** The start point of the X axis. */
    public Parameter xInit;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Notification that an attribute has changed.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the expression of the
     *   attribute cannot be parsed or cannot be evaluated.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == xInit) {
            _xInit = ((DoubleToken) xInit.getToken()).doubleValue();
        } else {
            if (attribute == xUnit) {
                _xUnit = ((DoubleToken) xUnit.getToken()).doubleValue();
            } else {
                super.attributeChanged(attribute);
            }
        }
    }

    /** If the plot has not already been created, create it.
     *  If configurations specified by a call to configure() have not yet
     *  been processed, process them.  This overrides the base class to
     *  also start counting iterations, so that the
     *  <i>iterationsPerUpdate</i> parameter works.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _iteration = 0;
        // If the model is run, changed and run again but this actor is
        // not fired, then be sure not to plot the old data
        _tokens = null;
    }

    /** Read at most one token from each input channel and plot it as
     *  a function of the iteration number, scaled by <i>xUnit</i>.
     *  The first point is plotted at the horizontal position given by
     *  <i>xInit</i>. The increments on the position are given by
     *  <i>xUnit</i>. The input data are plotted in postfire() to
     *  ensure that the data have settled.
     *  @exception IllegalActionException If there is no director,
     *   or if the base class throws it.
     *  @return True if it is OK to continue.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        int width = input.getWidth();
        _offset = ((IntToken) startingDataset.getToken()).intValue();

        if (_tokens == null || _tokens.length != width) {
            _tokens = new ArrayToken[width];
        }

        for (int i = width - 1; i >= 0; i--) {
            double xValue = _xInit;

            if (input.hasToken(i)) {
                _tokens[i] = (ArrayToken) input.get(i);

                if (_iteration == 0) {
                    Token[] currentArray = _tokens[i].arrayValue();

                    // NOTE: We assume the superclass ensures this cast is safe.
                    ((PlotInterface) plot).clear(i + _offset);

                    for (Token element : currentArray) {
                        double currentValue = ((DoubleToken) element)
                                .doubleValue();
                        ((PlotInterface) plot).addPoint(i + _offset, xValue,
                                currentValue, true);
                        xValue += _xUnit;
                    }
                }
            }
        }

        _iteration++;

        if (_iteration == ((IntToken) iterationsPerUpdate.getToken())
                .intValue()) {
            _iteration = 0;
        }

        return super.postfire();
    }

    /** Update the plot with the most recently read data.
     *  If the <i>fillOnWrapup</i> parameter is true, rescale the
     *  plot so that all the data is visible.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        if (_tokens != null) {
            for (int i = _tokens.length - 1; i >= 0; i--) {
                double xValue = _xInit;

                if (_tokens[i] != null) {
                    Token[] currentArray = _tokens[i].arrayValue();

                    // NOTE: We assume the superclass ensures this cast is safe.
                    ((PlotInterface) plot).clear(i + _offset);

                    for (Token element : currentArray) {
                        double currentValue = ((DoubleToken) element)
                                .doubleValue();
                        ((PlotInterface) plot).addPoint(i + _offset, xValue,
                                currentValue, true);
                        xValue += _xUnit;
                    }
                }
            }
        }

        super.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** Start of the X axis counter. */
    protected double _xInit;

    /** Increment of the X axis counter. */
    protected double _xUnit;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Iteration count, modulo the iterationsPerUpdate.
    private int _iteration = 0;

    // The value of the startingDataset parameter.
    private int _offset;

    // The most recently read tokens in the fire() method.
    private ArrayToken[] _tokens;
}
