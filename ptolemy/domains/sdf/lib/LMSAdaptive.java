/* A type polymorphic LMS adaptive filter.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import ptolemy.actor.TypedIOPort;
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
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// LMSAdaptive
/**
An adaptive filter using the Least-Mean Square (LMS) algorithm, also
known as the stochastic gradient algorithm.
The initial filter coefficients are given by the <i>initialTaps</i>
parameter.  The tap values can be observed on the
<i>tapValues</i> output. The default initial taps <i>initialTaps</i>
are {1, 0, 0, 0}. This actor supports decimation, but not interpolation.
<p>
When used correctly, this LMS adaptive filter will adapt to try to minimize
the mean-squared error of the signal at its <i>error</i> input.
In order for this to be possible, the output of the filter should
be compared (subtracted from) some reference signal to produce
an error signal.
That error signal should be fed back to the <i>error</i> input.
<p>
The <i>stepSize</i> parameter determines the rate of adaptation.
If its magnitude is too large, or if it has the wrong sign, then
the adaptation algorithm will be unstable.
<p>
The <i>errorDelay</i> parameter must equal the total number of delays
in the path from the output of the filter back to the error input.
This ensures correct alignment of the adaptation algorithm.
The number of delays must be greater than zero.
<p>
This actor is type polymorphic, supporting any data type that
supports multiplication by a scalar (the <i>stepSize</i>) and
addition.
<p>
The algorithm is simple.  Prior to each invocation of the parent
class (an FIR filter), which computes the output given the input,
this actor updates the coefficients according to the following
formula,
<pre>
   newTapValue = oldTapValue + error * stepSize * tapData
</pre>
where <i>tapData</i> is the contents of the delay line at
the tap in question.
This assumes that the <i>decimation</i> parameter is set
to 1 (the default).  If it has a value different from 1,
the algorithm is slightly more involved.  Similarly, this
assumes that the <i>errorDelay</i> is 1.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/
public class LMSAdaptive extends FIR {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public LMSAdaptive(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        interpolation.setVisibility(Settable.NONE);
        taps.setVisibility(Settable.NONE);

        error = new TypedIOPort(this, "error", true, false);
        tapValues = new TypedIOPort(this, "tapValues", false, true);

        // FIXME: Do we need to override attributeTypeChanged
        // to allow this to be a non double?
        stepSize = new Parameter(this, "stepSize", new DoubleToken(0.01));

        errorDelay = new Parameter(this, "errorDelay", new IntToken(1));

        // NOTE: This parameter is really just a renaming of the
        // taps parameter of the base class.  Setting it will just
        // cause the base class to be set.
        initialTaps = new Parameter(this, "initialTaps");
        ArrayType tapTypes = new ArrayType(BaseType.UNKNOWN);
        initialTaps.setTypeEquals(tapTypes);
        initialTaps.setExpression("{1.0, 0.0, 0.0, 0.0}");

        // set type constraints.
        error.setTypeSameAs(input);

        tapValues.setTypeSameAs(taps);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The error input port. The type of this port must match that
     *  of the input port.
     */
    public TypedIOPort error;

    /** The number of samples of delay in the feedback loop that
     *  brings the error back.  This has a type integer, and
     *  defaults to 1.
     */
    public Parameter errorDelay;

    /** The initial taps of the filter. This has a type of ArrayToken.
     *  By default, it contains the array {1.0, 0.0, 0.0, 0.0},
     *  meaning that the output of the filter is initially
     *  the same as the input, and that the adaptive filter has
     *  four taps.
     */
    public Parameter initialTaps;

    /** The adaptation step size.  This must have a type that can
     *  be multiplied by the input.  It defaults to 0.01, a double.
     */
    public Parameter stepSize;

    /** The output of tap values.  This has the same type as the
     *  initialTaps.
     */
    public TypedIOPort tapValues;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to set the <i>taps</i> parameter if the
     *  <i>initialTaps</i> parameter is changed.
     *  that are used in execution on the next invocation of fire().
     *  @param attribute The attribute that changed.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == initialTaps) {
            taps.setToken(initialTaps.getToken());
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then resets the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        LMSAdaptive newObject = (LMSAdaptive)(super.clone(workspace));

        // set the type constraints
        newObject.error.setTypeSameAs(newObject.input);
        newObject.tapValues.setTypeSameAs(newObject.taps);
        return newObject;
    }

    // FIXME: State update should occur in postfire.

    /** Consume the inputs, update the taps, and produce the outputs.
     *  @exception IllegalActionException If parameter values are invalid,
     *   or if there is no director, or if runtime type conflicts occur.
     */
    public void fire() throws IllegalActionException {
        // First update the taps
        int errorDelayValue = ((IntToken)errorDelay.getToken()).intValue();
        int decimationValue = ((IntToken)decimation.getToken()).intValue();
        int decimationPhaseValue = ((IntToken)decimationPhase.getToken())
            .intValue();
        int index = errorDelayValue * decimationValue + decimationPhaseValue;
        Token factor = error.get(0).multiply(stepSize.getToken());
        for (int i = 0; i < _taps.length; i++) {
            // The data item to use here should be "index" in the past,
            // where an index of zero would be the current input.
            Token datum = _data[(_mostRecent + index - 1) % _data.length];
            _taps[i] = _taps[i].add(factor.multiply(datum));
            index++;
        }
        // Update the tapValues output.
        // NOTE: This may be a relatively costly operation to be doing here.
        tapValues.send(0, new ArrayToken(_taps));

        // Then run FIR filter
        super.fire();
    }

    /** Return false if the error input does not have enough tokens to fire.
     *  Otherwise, return what the superclass returns.
     *  @return False if the number of input tokens available is not at least
     *   equal to the decimation parameter.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public boolean prefire() throws IllegalActionException {
        if (error.hasToken(0)) {
            return super.prefire();
        } else {
            if (_debugging) {
                _debug("Called prefire(), which returns false.");
            }
            return false;
        }
    }
}
