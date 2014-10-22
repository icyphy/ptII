/* Plot arrays of doubles in an XY plot.

 @Copyright (c) 2007-2014 The Regents of the University of California.
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
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.plot.Plot;

///////////////////////////////////////////////////////////////////
//// ArrayPlotterXY

/**
 <p>A plotter that plots a sequence of pairs of arrays of doubles
 as an XY plot.  This plotter contains an instance of the Plot
 class from the Ptolemy plot package as a public member. Data at
 the inputs, which can have any number of channels, are plotted
 on this instance.  Each pair of input channels is plotted as a separate data set.
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
 Note that this can be used to generate live plots, like XYScope,
 but it has fewer drawing artifacts than XYScope since it does
 not use XOR drawing mode.</p>

 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 @see XYScope
 */
public class ArrayPlotterXY extends Plotter implements SequenceActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ArrayPlotterXY(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Create the input port and make it a multiport.
        x = new TypedIOPort(this, "x", true, false);
        x.setMultiport(true);
        x.setTypeEquals(BaseType.DOUBLE);
        x.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        new Parameter(x, "_showName", BooleanToken.TRUE);

        y = new TypedIOPort(this, "y", true, false);
        y.setMultiport(true);
        y.setTypeEquals(BaseType.DOUBLE);
        y.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        new Parameter(y, "_showName", BooleanToken.TRUE);

        iterationsPerUpdate = new Parameter(this, "iterationsPerUpdate");
        iterationsPerUpdate.setExpression("1");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The number of iterations between updates of the display
     *  on the screen.
     *  This parameter has type IntToken, with default value 1.
     *  Its value must be non-negative.
     */
    public Parameter iterationsPerUpdate;

    /** Input port for the horizontal axis, which receives an array of doubles. */
    public TypedIOPort x;

    /** Input port for the vertical axis, which receives an array of doubles. */
    public TypedIOPort y;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
        _xtokens = null;
        _ytokens = null;
    }

    /** Read at most one token from each input channel on <i>x</i> and
     *  <i>y</i> inputs, and if there is a token on both, plot the data
     *  as an XY plot. The input data are plotted in postfire() to
     *  ensure that the data have settled.
     *  @exception IllegalActionException If there is no director,
     *   or if the base class throws it.
     *  @return True if it is OK to continue.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        int xwidth = x.getWidth();
        int ywidth = y.getWidth();
        _offset = ((IntToken) startingDataset.getToken()).intValue();

        int jointWidth = xwidth;
        if (jointWidth > ywidth) {
            jointWidth = ywidth;
        }
        if (_xtokens == null || _xtokens.length != jointWidth) {
            _xtokens = new ArrayToken[jointWidth];
        }
        if (_ytokens == null || _ytokens.length != jointWidth) {
            _ytokens = new ArrayToken[jointWidth];
        }

        for (int i = xwidth - 1; i >= 0; i--) {
            if (x.hasToken(i)) {
                _xtokens[i] = (ArrayToken) x.get(i);
                if (ywidth > i && y.hasToken(i)) {
                    _ytokens[i] = (ArrayToken) y.get(i);
                    if (_iteration == 0) {
                        Token[] xArray = _xtokens[i].arrayValue();
                        Token[] yArray = _ytokens[i].arrayValue();

                        // NOTE: We assume the superclass ensures this cast is safe.
                        ((Plot) plot).clear(i + _offset);

                        for (int j = 0; j < xArray.length; j++) {
                            double xValue = ((DoubleToken) xArray[j])
                                    .doubleValue();
                            double yValue = ((DoubleToken) yArray[j])
                                    .doubleValue();
                            ((Plot) plot).addPoint(i + _offset, xValue, yValue,
                                    true);
                        }
                    }
                }
            }
        }
        // If y is wider than x, read its inputs and discard them.
        for (int j = ywidth - 1; j >= xwidth; j--) {
            y.get(j);
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
        if (_xtokens != null) {
            for (int i = _xtokens.length - 1; i >= 0; i--) {
                if (_xtokens[i] != null && _ytokens[i] != null) {
                    Token[] xArray = _xtokens[i].arrayValue();
                    Token[] yArray = _ytokens[i].arrayValue();

                    // NOTE: We assume the superclass ensures this cast is safe.
                    ((Plot) plot).clear(i + _offset);

                    for (int j = 0; j < xArray.length; j++) {
                        double xValue = ((DoubleToken) xArray[j]).doubleValue();
                        double yValue = ((DoubleToken) yArray[j]).doubleValue();
                        ((Plot) plot).addPoint(i + _offset, xValue, yValue,
                                true);
                    }
                }
            }
        }
        super.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Iteration count, modulo the iterationsPerUpdate.
    private int _iteration = 0;

    // The value of the startingDataset parameter.
    private int _offset;

    // The most recently read tokens from the x input in the fire() method.
    private ArrayToken[] _xtokens;

    // The most recently read tokens from the y input in the fire() method.
    private ArrayToken[] _ytokens;
}
