/* A plotter that is also a source of sketched signals.

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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (vogel@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.gui;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.plot.EditablePlot;
import ptolemy.plot.Plot;
import ptolemy.plot.PlotBox;

import java.awt.Container;

//////////////////////////////////////////////////////////////////////////
//// SketchedSource
/**
This actor is a plotter that also produces as its output a
signal that has been sketched by the user on the screen.
The <i>length</i> parameter specifies the
number of samples in the sketched signal.  The <i>period</i>
parameter, if greater than zero, specifies the period with which the
signal should be repeated (in samples).  If the period is longer than
the length, then zeros will be inserted. If the period is less than
the length, then the last few values will not be used.  If this
parameter is zero or negative, then the sketched signal is produced
exactly once, at the beginning of the execution of the model.  If the
period is greater than zero, and the sketch is modified during
execution of the model, then the modification appears in the next
cycle of the period after the modification has been completed.  In
other words, the change does not appear mid-cycle.
<p>
This actor is also a plotter, and will plot the input signals
on the same plot as the sketched signal.  It can be used in a
feedback loop where the output affects the input.

@author  Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/
public class SketchedSource extends SequencePlotter {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SketchedSource(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

    	output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

        // Create the parameters.
        length = new Parameter(this, "length", new IntToken(100));
        period = new Parameter(this, "period", new IntToken(0));
        yBottom = new Parameter(this, "yBottom", new DoubleToken(-1.0));
        yTop = new Parameter(this, "yTop", new DoubleToken(1.0));

        // Fill on wrapup no longer makes sense.
        fillOnWrapup.setToken(BooleanToken.FALSE);
        fillOnWrapup.setVisibility(Settable.NONE);

        // Starting data set for producing plots is now always 1.
        startingDataset.setToken(_one);
        startingDataset.setVisibility(Settable.NONE);    
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The length of the output signal that will be generated.
     *  This parameter must contain an IntToken.  By default, it has
     *  value 100.
     */
    public Parameter length;

    /** The output port.  The type of this port is double.
     */
    public TypedIOPort output = null;

    /** An indicator of whether the signal should be periodically
     *  repeated, and if so, at what period.  If the value is negative
     *  or zero, it is not repeated.  Otherwise, it is repeated with
     *  the specified period.  This parameter must contain an IntToken.
     *  By default, it has value 0.
     */
    public Parameter period;

    /** The bottom of the Y range. This is a double, with default value -1.0.
     */
    public Parameter yBottom;

    /** The top of the Y range. This is a double, with default value 1.0.
     */
    public Parameter yTop;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>length</i>,
     *  then set the trace to its initial value.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>length</i> and its value is not positive.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == length) {
            if (((IntToken)length.getToken()).intValue() < 0) {
                throw new IllegalActionException(this,
                        "length: value is required to be positive.");
            }
            _setInitialTrace();
        } else if (attribute == yBottom || attribute == yTop) {
            _setRanges();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has an
     *   attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        SketchedSource newObject = (SketchedSource)super.clone(workspace);
        // FIXME: Anything needed here?
        _count = 0;
        return newObject;
    }

    /** Produce one data sample from the sketched signal on the output
     *  port.
     *  @exception IllegalActionException If there is no director, or
     *   if the base class throws it.
     */
    public void fire() throws IllegalActionException {
        // Read the trigger input, if there is one.
        super.fire();
        int periodValue = ((IntToken)period.getToken()).intValue();
        if (_count < _data[1].length) {
            // NOTE: X value ignored.
            output.send(0, new DoubleToken(_data[1][_count]));
            _count++;
        } else {
            output.send(0, _zero);
            if (_count < periodValue) {
                _count++;
            }
        }
        if (periodValue > 0 && _count >= periodValue) {
            // Reread the data in case it has changed.
            _count = 0;
            _data = ((EditablePlot)plot).getData(0);
        }
    }

    /** Override the base class to read data from the plot.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (!_initialTraceSet) {
            _setInitialTrace();
        }
        _data = ((EditablePlot)plot).getData(0);
        _count = 0;
    }

    /** Override the base class to create an initial trace.
     *  @param container The container into which to place the plot.
     */
    public void place(Container container) {
        super.place(container);
        // Set the default signal value in the plot.
        try {
            _setInitialTrace();
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create a new plot. In this class, it is an instance of EditablePlot.
     *  @return A new editable plot object.
     */
    protected PlotBox _newPlot() {
        return new EditablePlot();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Set the initial value of the plot and fill the plot.
    // If the plot is null, return without doing anything.
    private void _setInitialTrace() throws IllegalActionException {
        if (plot == null) return;
        _initialTraceSet = true;
        int lengthValue = ((IntToken)length.getToken()).intValue();
        // If the values haven't changed, return.
        if (lengthValue == _previousLengthValue) {
            return;
        }
        _previousLengthValue = lengthValue;
        ((Plot)plot).clear(0);
        boolean connected = false;
        for (int i = 0; i < lengthValue; i++) {
            ((Plot)plot).addPoint(0, (double)i, 0.0, connected);
            connected = true;
        }
        _setRanges();
        plot.repaint();
    }

    // Set the X and Y ranges of the plot.
    private void _setRanges() throws IllegalActionException {
        if (plot == null) return;
        double xInitValue = ((DoubleToken)xInit.getToken()).doubleValue();
        double xUnitValue = ((DoubleToken)xUnit.getToken()).doubleValue();
        int lengthValue = ((IntToken)length.getToken()).intValue();
        plot.setXRange(xInitValue, xUnitValue * lengthValue);

        double yBottomValue = ((DoubleToken)yBottom.getToken()).doubleValue();
        double yTopValue = ((DoubleToken)yTop.getToken()).doubleValue();
        plot.setYRange(yBottomValue, yTopValue);
    }        

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** Current position in the signal. */
    private int _count;

    /** Sketched data. */
    private double[][] _data;

    /** Indicator that initial trace has been supplied. */
    private boolean _initialTraceSet = false;

    // Constant one.
    private static IntToken _one = new IntToken(1);

    // Previous value of length parameter.
    private int _previousLengthValue;

    /** Zero token. */
    private static DoubleToken _zero = new DoubleToken(0.0);
}
