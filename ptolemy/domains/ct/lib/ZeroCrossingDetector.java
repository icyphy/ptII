/* A CT actor that detects zero crossings of its trigger input signal.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Yellow (liuj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.lib;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.CTEventGenerator;
import ptolemy.domains.ct.kernel.CTStepSizeControlActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// ZeroCrossingDetector
/**
A event detector that converts continuous signals to discrete events.
When the <i>trigger</i> is zero (within the specified
<i>errorTolerance</i>), this actor outputs the value from the
<i>input</i> port as a discrete event. This actor controls
the integration step size to accurately resolve the time
at which the zero crossing occurs.

@author Jie Liu
@version $Id$
*/
public class ZeroCrossingDetector extends Transformer
    implements CTStepSizeControlActor, CTEventGenerator {

    /** Construct an actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The subsystem that this actor is lived in
     *  @param name The actor's name
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If name coincides with
     *   an entity already in the container.
     */
    public ZeroCrossingDetector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.DOUBLE);
        Parameter inputType = new Parameter(input, "signalType",
                new StringToken("CONTINUOUS"));
        output.setTypeEquals(BaseType.DOUBLE);
        Parameter outputType = new Parameter(output, "signalType",
                new StringToken("DISCRETE"));
        trigger = new TypedIOPort(this, "trigger", true, false);
        trigger.setMultiport(false);
        trigger.setTypeEquals(BaseType.DOUBLE);
        Parameter triggerType = new Parameter(trigger, "signalType",
                new StringToken("CONTINUOUS"));
        _errorTolerance = (double)1e-4;
        errorTolerance = new Parameter(this, "errorTolerance",
                new DoubleToken(_errorTolerance));

    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public variables                       ////

    /** The trigger port. Single port with type double.
     */
    public TypedIOPort trigger;

    /** The parameter of error tolerance of type double. By default,
     *  it contains a DoubleToken of 1e-4.
     */
    public Parameter errorTolerance;

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Update the attribute if it has been changed. If the attribute
     *  is <i>errorTolerance<i> then update the local cache.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the attribute change failed.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException{
        if (attribute == errorTolerance) {
            double p = ((DoubleToken)errorTolerance.getToken()
                        ).doubleValue();
            if(p <= 0) {
                throw new IllegalActionException(this,
                        "Error tolerance must be greater than 0.");
            }
            _errorTolerance = p;
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Emit the event at current time if there is any. There will be no
     *  current event after emitting it. If there is no current event,
     *  do nothing.
     *  @exception IllegalActionException If the event cannot be broadcasted.
     *
    public void emitCurrentEvents() throws IllegalActionException{
        if(_debugging)
            _debug(this.getFullName() + " checking for current event...");

        if(_eventNow) {
            if(_debugging) _debug(getFullName() + " Emitting event: " +
                    _inputToken.toString());
            if (input.getWidth() != 0) {
                output.broadcast(_inputToken);
            } else {
                output.broadcast(new DoubleToken(0.0));
            }
            _eventNow = false;
        }
        }*/

    /** Consume the input token and the trigger token. The trigger token
     *  will be used for finding the zero crossing in the isThisStepAccurate()
     *  method to control the step size. The input token will be
     *  used in emitCurrentEvent() if the trigger is zero (within the
     *  given error tolerance). Notice that this method does not
     *  produce any output.
     *  @exception IllegalActionException If no token is available.
     */
    public void fire() throws IllegalActionException {
        CTDirector director = (CTDirector)getDirector();
        if (director.isDiscretePhase()) {
            if (hasCurrentEvent()) {
                // Emit event.
                if(_debugging) _debug(getFullName() + " Emitting event: " +
                        _inputToken.toString());
                if (_inputToken != null) {
                    output.broadcast(_inputToken);
                } else {
                    output.broadcast(new DoubleToken(0.0));
                }
                _eventNow = false;
            }
        } else {
            //consume the input.
            _thisTrigger = ((DoubleToken) trigger.get(0)).doubleValue();
            if(_debugging)
                _debug(getFullName() + " consuming trigger Token" +
                        _thisTrigger);
            if((input.getWidth() != 0) && input.hasToken(0)) {
                _inputToken = input.get(0);
            } else {
                _inputToken = null;
            }
        }
    }

    /** Return true if there is an event at the current time.
     *  @return True if there is an event at the current time.
     */
    public boolean hasCurrentEvent() {
        return _eventNow;
    }

    /** Initialize the execution.
     *
     *  @exception IllegalActionException If thrown by the super class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _first = true;
	_eventNow = false;
        if(_debugging) _debug(getFullName() + "initialize");
    }

    /** Return true if this step does not cross zero. The current trigger
     *  token will be compared to the previous trigger token. If they
     *  cross the zero threshold, this step is not accurate.
     *  A special case is taken care so that if the previous trigger
     *  and the current trigger are both zero, then no new event is
     *  detected. If this step crosses zero, then the refined integration
     *  step size is computed by linear interpolation.
     *  If this is the first iteration after initialize() is called,
     *  then always return false, since there is no history to compare with.
     *  @return True if the trigger input in this integration step
     *          does not cross zero.
     */
    public boolean isThisStepAccurate() {
        if (_first) {
            _first = false;
            _eventNow = false;
            return true;
        }
        if(_debugging) {
            _debug(this.getFullName() + " This trigger " + _thisTrigger);
            _debug(this.getFullName() + " The last trigger " + _lastTrigger);
        }
        if (Math.abs(_thisTrigger) < _errorTolerance) {
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
            if(!_enabled) {  // if last step is a zero, always accurate.
                _enabled = true;
            } else {
                if ((_lastTrigger * _thisTrigger) < 0.0) {

                    CTDirector dir = (CTDirector)getDirector();
                    _eventMissed = true;
                    _refineStep = (-_lastTrigger*dir.getCurrentStepSize())/
                        (_thisTrigger-_lastTrigger);
                    if(_debugging) _debug(getFullName() +
                            " Event Missed: refined step at" +  _refineStep);
                    return false;
                }
            }
            _eventMissed = false;
            return true;
        }
    }

    /** Prepare for the next iteration, by making the current trigger
     *  token to be the history trigger token.
     *  @return True always.
     */
    public boolean postfire() {
        _lastTrigger = _thisTrigger;

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


    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // Parameter, the error tolerance, local copy
    private double _errorTolerance;

    // flag for indicating a missed event
    private boolean _eventMissed = false;

    // refined step size.
    private double _refineStep;

    // last trigger input.
    private double _lastTrigger;

    // this trigger input.
    private double _thisTrigger;

    // flag indicating if the event detection is enable for this step
    private boolean _enabled;

    // flag indicating if there is an event at the current time.
    private boolean _eventNow = false;

    // flag indicating if this is the first iteration in the execution,
    private boolean _first = true;

    // the current input token.
    private  Token _inputToken;
}
