/* A CT actor that detects zero crossing of input signal.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.lib;
import ptolemy.domains.ct.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.*;


//////////////////////////////////////////////////////////////////////////
//// ZeroCrossingDetector
/**
This is an event detector that monitors the signal coming in from the
"trigger" input. If the trigger is zero, then output the token from
the "input." port.
This actor controls the integration step size to accurately resolve
the time that the zero crossing happens.
It has a parameter "ErrorTolerance," which controls how accurate the
zero crossing is defined.
@author Jie Liu
@version $Id$
*/
public class ZeroCrossingDetector extends CTActor
    implements  CTStepSizeControlActor, CTEventGenerator {

    /** Construct an actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  The actor has two input, "trigger" and "input", and one output,
     *  "output." Both of them are single ports. All the ports has type
     *  DoubleToken.
     *
     *  @param container The subsystem that this actor is lived in
     *  @param name The actor's name
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If name coincides with
     *   an entity already in the container.
     */
    public ZeroCrossingDetector(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input");
        input.setMultiport(false);
        input.setInput(true);
        input.setOutput(false);
        input.setTypeEquals(BaseType.DOUBLE);
        trigger = new TypedIOPort(this, "trigger");
        trigger.setMultiport(false);
        trigger.setInput(true);
        trigger.setOutput(false);
        trigger.setTypeEquals(BaseType.DOUBLE);
        output = new TypedIOPort(this, "output");
        output.setMultiport(false);
        output.setInput(false);
        output.setOutput(true);
        output.setTypeEquals(BaseType.DOUBLE);
        _errorTolerance = (double)1e-4;
        ErrorTolerance = new Parameter(this, "ErrorTolerance",
                new DoubleToken(_errorTolerance));

    }


    ////////////////////////////////////////////////////////////////////////
    ////                         public variables                       ////

    /** The input port. Single port with type DoubleToken.
     */
    public TypedIOPort input;

    /** The output port. Single port with type DoubleToken.
     */
    public TypedIOPort output;

    /** The trigger port. Single port with type DoubleToken.
     */
    public TypedIOPort trigger;

    /** The parameter of error tolerance
     */
    public Parameter ErrorTolerance;

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Emit the event at current time if there is any. There will be no
     *  current event after emitting it. If there is no current event,
     *  do nothing.
     *  @exception IllegalActionException If the event cannot be broadcasted.
     */
    public void emitCurrentEvents() throws IllegalActionException{
        if(_debugging)
            _debug(this.getFullName() + " checking for current event...");

        if(_eventNow) {
            if(_debugging) _debug(getFullName() + " Emitting event: " +
                    _inputToken.toString());
            output.broadcast(_inputToken);
            _eventNow = false;
        }
    }

    /** Consume the input token and the trigger token. The trigger token
     *  will be used for finding the zero crossing in isThisStepSuccessful()
     *  method.
     *  @exception IllegalActionException If no token is available.
     */
    public void fire() throws IllegalActionException {
        _thisTrg = ((DoubleToken) trigger.get(0)).doubleValue();
        if(_debugging)
            _debug(getFullName() + " consuming trigger Token" +  _thisTrg);
        _inputToken = input.get(0);
    }

    /** Return true if there is an event at the current time.
     *  @return True if there is an event at the current time.
     */
    public boolean hasCurrentEvent() {
        return _eventNow;
    }

    /** Set up parameters and internal state, so that it has no history
     *  before the first firing.
     *
     *  @exception IllegalActionException If thrown by the super class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        updateParameters();
        _first = true;
        if(_debugging) _debug(getFullName() + "initialize");
    }

    /** Return true if this step does not cross zero. The current trigger
     *  token will be compared to the history trigger token. If they
     *  cross the zero threshold, this step is not successful.
     *  A special case is taken care such that if the history trigger
     *  and the current trigger are both zero, then no new event is
     *  triggered. If this step crosses zero, then the refined integration
     *  step size is computed.
     */
    public boolean isThisStepSuccessful() {
        if (_first) {
            _first = false;
            return true;
        }
        if(_debugging) {
            _debug(this.getFullName() + " This trigger " + _thisTrg);
            _debug(this.getFullName() + " The last trigger " + _lastTrg);
        }
        if (Math.abs(_thisTrg) < _errorTolerance) {
            if (_enabled) {
                _eventNow = true;
                if(_debugging)
                    _debug(getFullName() + " detected event at "
                            + getDirector().getCurrentTime());
                _enabled = false;
            }
            _eventMissed = false;
            return true;
        } else {
            if(!_enabled) {  // if last step is a zero, always successful.
                _enabled = true;
            } else {
                if ((_lastTrg * _thisTrg) < 0.0) {

                    CTDirector dir = (CTDirector)getDirector();
                    _eventMissed = true;
                    _refineStep = (-_lastTrg*dir.getCurrentStepSize())/
                        (_thisTrg-_lastTrg);
                    if(_debugging) _debug(getFullName() +
                            " Event Missed: refined step at" +  _refineStep);
                    return false;
                }
            }
            _eventMissed = false;
            return true;
        }
    }

    /** Make the current trigger token the history trigger token. Prepare
     *  for the next iteration.
     *  @return True always.
     */
    public boolean postfire() {
        if(!_eventMissed) {
            _lastTrg = _thisTrg;
        }
        return true;
    }

    /** Return the maximum Double, since this actor does not predict
     *  step size.
     *  @return java.Double.MAX_VALUE.
     */
    public double predictedStepSize() {
        return java.lang.Double.MAX_VALUE;
    }

    /** Return the refined step size if there is a missed event,
     *  otherwise return the current step size.
     *  @return The refined step size.
     */
    public double refinedStepSize() {
        if(_eventMissed) {
            return _refineStep;
        }
        return ((CTDirector)getDirector()).getCurrentStepSize();
    }

    /** Update the parameter if it has been changed.
     *  The new parameter will be used only after this method is called.
     */
    public void updateParameters() throws IllegalActionException{
        double p = ((DoubleToken)ErrorTolerance.getToken()
                    ).doubleValue();
        if(p <= 0) {
            throw new IllegalActionException(this,
                    "Error tolerance must be greater than 0.");
        }
        _errorTolerance = p;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // Parameter, the error tolerance, local copy
    private double _errorTolerance;

    // flag for indicating a missed event
    private boolean _eventMissed = false;

    // refined step size.
    private double _refineStep;

    // last trigger input.
    private double _lastTrg;

    // this trigger input.
    private double _thisTrg;

    // flag indicating if the event detection is enable for this step
    private boolean _enabled;

    // flag indicating if there is an event at the current time.
    private boolean _eventNow = false;

    // flag indicating if this is the first iteration in the execution,
    private boolean _first = true;

    // the current input token.
    private  Token _inputToken;
}
