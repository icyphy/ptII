/* A CT actor that detects level crossings of its trigger input signal.

Copyright (c) 1998-2005 The Regents of the University of California.
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
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.CTEventGenerator;
import ptolemy.domains.ct.kernel.CTExecutionPhase;
import ptolemy.domains.ct.kernel.CTStepSizeControlActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;


//////////////////////////////////////////////////////////////////////////
//// LevelCrossingDetector

/**
   An event detector that converts continuous signals to discrete events when
   the continuous signal crosses a level threshold.
   <p>
   The <i>direction</i> parameter
   can constrain the actor to detect only rising or falling transitions.
   It has three possible values, "rising", "falling", and "both", where
   "both" is the default.
   <p>
   When the <i>trigger</i> equals the level threshold (within the specified
   <i>errorTolerance</i>), this actor outputs a discrete event with value
   <i>defaultEventValue</i> if <i>useEventValue</i> is selected. Otherwise, the
   actor outputs a discrete event with the value as the level threshold.
   <p>
   This actor controls the step size such that level crossings never
   occur during an integration. So, this actor is only used in Continuous-Time
   domain.

   @author Jie Liu, Haiyang Zheng
   @version $Id$
   @since Ptolemy II 2.0
   @Pt.ProposedRating Yellow (hyzheng)
   @Pt.AcceptedRating Red (hyzheng)
*/
public class LevelCrossingDetector extends TypedAtomicActor
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
    public LevelCrossingDetector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        output = new TypedIOPort(this, "output", false, true);
        new Parameter(output, "signalType", new StringToken("DISCRETE"));

        trigger = new TypedIOPort(this, "trigger", true, false);
        trigger.setMultiport(false);
        trigger.setTypeEquals(BaseType.DOUBLE);
        new Parameter(trigger, "signalType", new StringToken("CONTINUOUS"));

        level = new Parameter(this, "level", new DoubleToken(0.0));
        level.setTypeEquals(BaseType.DOUBLE);

        // By default, this director detects both directions of leve crossings.
        direction = new StringParameter(this, "direction");
        direction.setExpression("both");
        _detectRisingCrossing = true;
        _detectFallingCrossing = true;
        
        direction.addChoice("both");
        direction.addChoice("falling");
        direction.addChoice("rising");

        defaultEventValue = new Parameter(this, "defaultEventValue",
                new DoubleToken(0.0));
        
        // FIXME: If usingDefaultEventValue is false, the output
        // type should be constrained to match the trigger input type.
        output.setTypeAtLeast(defaultEventValue);

        useDefaultEventValue = new Parameter(this, "useDefaultEventValue");
        useDefaultEventValue.setTypeEquals(BaseType.BOOLEAN);
        useDefaultEventValue.setToken(BooleanToken.FALSE);

        _errorTolerance = (double) 1e-4;
        errorTolerance = new Parameter(this, "errorTolerance",
                new DoubleToken(_errorTolerance));
        errorTolerance.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** A parameter that specifies the value of output events
     *  if the <i>useEventValue</i> parameter is checked. By default,
     *  it contains a DoubleToken of 0.0.
     */
    public Parameter defaultEventValue;

    /** A parameter that can be used to limit the detected level crossings
     *  to rising or falling. There are three choices: "falling", "rising", and 
     *  "both". The default value is "both".
     */
    public StringParameter direction;

    /** The parameter of error tolerance of type double. By default,
     *  it contains a DoubleToken of 1e-4.
     */
    public Parameter errorTolerance;

    /** The parameter that specifies the level threshold. By default, it
     *  contains a DoubleToken of value 0.0. Note, a change of this
     *  parameter at run time will not be applied until the next
     *  iteration.
     */
    public Parameter level;

    /** The output port. The type is at least the type of the
     *  <i>defaultEventValue</i> parameter.
     */
    public TypedIOPort output;

    /** The trigger port. Single port with type double.
     */
    public TypedIOPort trigger;

    /** The parameter that indicates whether to use the default event
     *  value.
     */
    public Parameter useDefaultEventValue;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Update the attribute if it has been changed. If the attribute
     *  is <i>errorTolerance</i> or <i>level</i>, then update the local cache.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the attribute change failed.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == errorTolerance) {
            double tolerance = 
                ((DoubleToken) errorTolerance.getToken()).doubleValue();
            if (tolerance <= 0.0) {
                throw new IllegalActionException(this,
                        "Error tolerance must be greater than 0.");
            }
            _errorTolerance = tolerance;            
        } else if (attribute == direction) {
            String crossingDirections = direction.stringValue();
            if (crossingDirections.equalsIgnoreCase("falling")) {
                _detectFallingCrossing = true;
                _detectRisingCrossing = false;
            } else if (crossingDirections.equalsIgnoreCase("rising")) {
                _detectFallingCrossing = false;
                _detectRisingCrossing = true;
            } else if (crossingDirections.equalsIgnoreCase("both")) {
                _detectFallingCrossing = true;
                _detectRisingCrossing = true;
            } else {
                throw new IllegalActionException("Unknown direction: "
                        + crossingDirections);
            }
        } else if (attribute == level) {
            _level = ((DoubleToken) level.getToken()).doubleValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the value public variable in the new
     *  object to equal the cloned parameter in that new object.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *  an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        LevelCrossingDetector newObject = (LevelCrossingDetector) super.clone(workspace);

        // Set the type constraints.
        newObject.output.setTypeAtLeast(newObject.defaultEventValue);
        return newObject;
    }

    /** Produce a discrete event if level crossing happens. If the current
     *  execution is in a continuous phase, the current trigger is recorded but
     *  no event can be produced. If the current execution is in a discrete
     *  phase, the current and previous trigger tokens are compared to find
     *  whether a level crossing happens. If there is a crossing, a discrete
     *  event is generated.
     *  <p>
     *  The value of this event may be the specified level, or the default
     *  event value if the usingDefaultEventValue is configured true (checked).
     *  @exception IllegalActionException If can not get token from the trigger
     *  port or can not send token through the output port.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        //record the input.
        _thisTrigger = ((DoubleToken) trigger.get(0)).doubleValue();

        if (_debugging) {
            _debug("Consuming a trigger token: " + _thisTrigger);
        }

        CTDirector director = (CTDirector) getDirector();

        if (director.getExecutionPhase() == CTExecutionPhase.GENERATING_EVENTS_PHASE) {
            if (_debugging && _verbose) {
                _debug("This is a discrete phase execution.");
            }

            // There are two conditions when an event is generated.
            // 1. By linear interpolation, an event is located at the current
            // time; OR,
            // 2. There is a discontinuity at the current time.
            boolean hasEvent = _eventNow;
            if ((_lastTrigger - _level) * (_thisTrigger - _level) < 0.0) {
                boolean inputIsIncreasing = _thisTrigger > _lastTrigger;
                if ((_detectFallingCrossing && !inputIsIncreasing) ||
                        (_detectRisingCrossing && inputIsIncreasing)) {
                    hasEvent = true;
                }
            }
            if (hasEvent) {
                // Emit an event.
                if (((BooleanToken) useDefaultEventValue.getToken())
                        .booleanValue()) {
                    output.send(0, defaultEventValue.getToken());
                    
                    if (_debugging) {
                        _debug("Emitting an event with a default value: "
                                + defaultEventValue.getToken());
                    }
                } else {
                    output.send(0, new DoubleToken(_level));
                    
                    if (_debugging) {
                        _debug("Emitting an event with the level value: "
                                + _level);
                    }
                }
                // Event has been emitted. Clear the internal states.
                _eventNow = false;
                _eventMissed = false;
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
     *  @exception IllegalActionException If thrown by the super class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _eventMissed = false;
        _eventNow = false;
        _level = ((DoubleToken) level.getToken()).doubleValue();
        _thisTrigger = _lastTrigger = 0.0;
    }

    /** Return true if there is no event detected during the current step size.
     *  @return True if there is no event detected in the current iteration.
     */
    public boolean isOutputAccurate() {
        if (_debugging && _verbose) {
            _debug("The last trigger is " + _lastTrigger);
            _debug("The current trigger is " + _thisTrigger);
        }

        // If the level is crossed and the current trigger is very close
        // to the level, the current step size is accurate.
        // If the current trigger is equal to the level threshold, the current
        // step size is accurate.
        // Otherwise, the current step size is too big.
        // NOTE that the level crossing must happen to avoid the possibility
        // of detecting duplicate level crossings.
        boolean inputIsIncreasing = _thisTrigger > _lastTrigger;
        if (((_lastTrigger - _level) * (_thisTrigger - _level)) < 0.0) {
            // Preinitialize method ensures the cast to be safe.
            CTDirector director = (CTDirector) getDirector();

            if (Math.abs(_thisTrigger - _level) < _errorTolerance) {
                if ((_detectFallingCrossing && !inputIsIncreasing) ||
                        (_detectRisingCrossing && inputIsIncreasing)) {
                    // The current time is close enough to when the event 
                    // happens.
                    if (_debugging) {
                        _debug("Event is detected at "
                                + getDirector().getModelTime());
                    }
                    _eventNow = true;
                    _eventMissed = false;
                } else {
                    // Although the current trigger is close to the level,
                    // the direction is not right. Ignore the event.
                    _eventNow = false;
                    _eventMissed = false;
                }
            } else {
                // Level crossing happens and the direction is right,
                // report a missing event.
                if ((_detectFallingCrossing && !inputIsIncreasing) ||
                        (_detectRisingCrossing && inputIsIncreasing)) {
                    _eventNow = false;
                    _eventMissed = true;
                } else {
                    // Although level crossing happens, 
                    // the direction is not right. Ignore the event.
                    _eventNow = false;
                    _eventMissed = false;
                }
            }
        } else if (_thisTrigger == _level) {
            if ((_detectFallingCrossing && !inputIsIncreasing) ||
                    (_detectRisingCrossing && inputIsIncreasing)) {
                // The current time is exactly when the event happens.
                if (_debugging) {
                    _debug("Event is detected at "
                            + getDirector().getModelTime());
                }
                _eventNow = true;
                _eventMissed = false;
            } else {
                // Although the current trigger is equal to the level,
                // the direction is not right. Ignore the event.
                _eventNow = false;
                _eventMissed = false;
            }
        } else {
            // No level crossing happens, apperantly no events.
            _eventNow = false;
            _eventMissed = false;
        }

        return !_eventMissed;
    }

    /** Always return true because this actor is not involved
     *  in resolving states.
     *  @return true.
     */
    public boolean isStateAccurate() {
        return true;
    }

    /** Prepare for the next iteration, by making the current trigger
     *  token to be the history trigger token.
     *  @return True always.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public boolean postfire() throws IllegalActionException {
        _lastTrigger = _thisTrigger;
        CTDirector director = (CTDirector) getDirector();
        return super.postfire();
    }

    /** Return the maximum Double, since this actor does not predict
     *  step size.
     *  @return java.Double.MAX_VALUE.
     */
    public double predictedStepSize() {
        return java.lang.Double.MAX_VALUE;
    }

    /** Make sure the actor runs inside a CT domain.
     *  @exception IllegalActionException If the director is not
     *  a CTDirector or the parent class throws it.
     */
    public void preinitialize() throws IllegalActionException {
        if (!(getDirector() instanceof CTDirector)) {
            throw new IllegalActionException("LevelCrossingDetector can only"
                    + " be used inside CT domain.");
        }

        super.preinitialize();
    }

    /** Return the refined step size if there is a missed event,
     *  otherwise return the current step size.
     *  @return The refined step size.
     */
    public double refinedStepSize() {
        CTDirector dir = (CTDirector) getDirector();
        double refinedStep = dir.getCurrentStepSize();

        if (_eventMissed) {
            // The refined step size is a linear interpolation.
            // NOTE: we always to get a little overshoot to make sure the
            // level crossing happens. The little overshoot chosen here
            // is half of the error toelrance.
            refinedStep = 
                ((Math.abs(_lastTrigger - _level) + _errorTolerance/2) 
                        * dir.getCurrentStepSize())
                        / Math.abs(_thisTrigger - _lastTrigger);

            if (_debugging) {
                _debug(getFullName() + " Event Missed: refined step to "
                        + refinedStep + " at " + dir.getModelTime() );
            }
        }

        return refinedStep;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The level threshold this actor detects.
     */
    // The variable is proetected because ZeroCrossingDetector needs access
    // to this variable.
    protected double _level;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    // flag indicating whether this actor detects the level crossing 
    // when the input value is rising.
    private boolean _detectRisingCrossing;

    // flag indicating whether this actor detects the level crossing 
    // when the input value is falling.
    private boolean _detectFallingCrossing;
    
    // Parameter, the error tolerance, local copy
    private double _errorTolerance;

    // flag for indicating a missed event
    private boolean _eventMissed = false;

    // flag indicating if there is an event at the current time.
    private boolean _eventNow = false;

    // last trigger input.
    private double _lastTrigger;

    // this trigger input.
    private double _thisTrigger;
}
