/* Monitor integration steps so that a threshold is not crossed in one step.

Copyright (c) 1999-2005 The Regents of the University of California.
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

*/
package ptolemy.domains.ct.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.CTStepSizeControlActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


//////////////////////////////////////////////////////////////////////////
//// ThresholdMonitor

/**
   Output <i>true</i> if the input value is in the interval
   [<i>a</i>, <i>b</i>], which is centered at <i>thresholdCenter</i>
   and has width <i>thresholdWidth</i>.  This actor controls
   the integration step size so that the input does
   not cross the threshold without producing at least one
   <i>true</i> output. The output can be used as a pure event
   to trigger other events or state transitions.
   When the input crosses the interval in
   one step, this actor will report that the integration step is
   not accurate and refines the new step size by bisecting the
   old step size.

   @author  Jie Liu
   @version $Id$
   @since Ptolemy II 0.4
   @Pt.ProposedRating Red (liuj)
   @Pt.AcceptedRating Red (cxh)
*/
public class ThresholdMonitor extends TypedAtomicActor
    implements CTStepSizeControlActor {
    /** Construct an actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The subsystem that this actor is lived in
     *  @param name The name of the actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If name coincides with
     *   an entity already in the container.
     */
    public ThresholdMonitor(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(false);
        input.setTypeEquals(BaseType.DOUBLE);
        new Parameter(input, "signalType", new StringToken("CONTINUOUS"));

        output = new TypedIOPort(this, "output", false, true);
        output.setMultiport(false);
        output.setTypeEquals(BaseType.BOOLEAN);
        new Parameter(output, "signalType", new StringToken("DISCRETE"));

        _thWidth = (double) 1e-2;
        thresholdWidth = new Parameter(this, "thresholdWidth",
                new DoubleToken(_thWidth));

        _thCenter = (double) 0.0;
        thresholdCenter = new Parameter(this, "thresholdCenter",
                new DoubleToken(_thCenter));

        _lowerBound = -5e-3;
        _upperBound = 5e-3;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port, single port with type double.
     */
    public TypedIOPort input;

    /** The output port, single port with type boolean.
     */
    public TypedIOPort output;

    /** The parameter for the width of the threshold.
     */
    public Parameter thresholdWidth;

    /** The parameter for the center of the threshold.
     */
    public Parameter thresholdCenter;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Update local caches if the attributes have been changed.
     *  @exception IllegalActionException If there is no token in the
     *  the attribute.
     */
    public void attributeChanged(Attribute attribute)
        throws IllegalActionException {
        if ((attribute == thresholdCenter) || (attribute == thresholdWidth)) {
            _thCenter = ((DoubleToken) thresholdCenter.getToken()).doubleValue();
            _thWidth = Math.abs(((DoubleToken) thresholdWidth.getToken())
                                .doubleValue());

            _lowerBound = _thCenter - (_thWidth / (double) 2.0);
            _upperBound = _thCenter + (_thWidth / (double) 2.0);
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Consume the current input. If the input is in the threshold,
     *  then output true, otherwise output false.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        _debug("Monitor" + getFullName() + " fired.");
        _thisInput = ((DoubleToken) input.get(0)).doubleValue();

        if ((_thisInput <= _upperBound) && (_thisInput >= _lowerBound)) {
            output.send(0, new BooleanToken(true));
        } else {
            output.send(0, new BooleanToken(false));
        }
    }

    /** Initialize the execution.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _first = true;
    }

    /** Make this input to be the history input and return true.
     *  @return True.
     *  @exception IllegalActionException If token can not be read from or
     *  sent to ports, or thrown by the super class.
     */
    public boolean postfire() throws IllegalActionException {
        super.postfire();

        if (input.hasToken(0)) {
            _thisInput = ((DoubleToken) input.get(0)).doubleValue();
        }

        if ((_thisInput <= _upperBound) && (_thisInput >= _lowerBound)) {
            output.send(0, new BooleanToken(true));
        } else {
            output.send(0, new BooleanToken(false));
        }

        _lastInput = _thisInput;
        _first = false;
        return true;
    }

    /** Return java.lang.Double.MAX_VALUE, since this actor does not predict
     *  step sizes.
     *  @return java.lang.Double.MAX_VALUE.
     */
    public double predictedStepSize() {
        return java.lang.Double.MAX_VALUE;
    }

    /** Return half the current step size if the step crosses the threshold.
     *  Otherwise, return the current step size.
     *  @return Half of the current step size if the step is not accurate.
     */
    public double refinedStepSize() {
        CTDirector dir = (CTDirector) getDirector();

        if (!_accurate) {
            return 0.5 * dir.getCurrentStepSize();
        }

        return dir.getCurrentStepSize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // local copy of the threshold width
    private double _thWidth;

    // local copy of the threshold center.
    private double _thCenter;

    // flag indicting if this is the first iteration in an execution.
    private boolean _first;

    // flag indicating if the current step is accurate
    private boolean _accurate;

    // upper bound of the input value, = thCenter + thWidth/2
    private double _upperBound;

    // lower bound of the input value, = thCenter - thWidth/2
    private double _lowerBound;

    // last input token value
    private double _lastInput;

    // this input token value.
    private double _thisInput;

    /* (non-Javadoc)
     * @see ptolemy.domains.ct.kernel.CTStepSizeControlActor#isStateAccurate()
     */
    public boolean isStateAccurate() {
        return true;
    }

    /* (non-Javadoc)
     * @see ptolemy.domains.ct.kernel.CTStepSizeControlActor#isOutputAccurate()
     */
    public boolean isOutputAccurate() {
        if (!_first) {
            if (((_lastInput >= _upperBound) && (_thisInput <= _lowerBound))
                            || ((_lastInput <= _lowerBound)
                            && (_thisInput >= _upperBound))) {
                _debug(getFullName() + "one step crosses the threshold"
                    + "cutting the step size in half.");
                _accurate = false;
                return false;
            }
        }

        _accurate = true;
        return true;
    }
}
