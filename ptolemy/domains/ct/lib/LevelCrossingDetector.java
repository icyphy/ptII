/* A CT actor that detects level crossings of its trigger input signal.

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

@ProposedRating Yellow (liuj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.lib;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.CTEventGenerator;
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
When the <i>trigger</i> equals to the level threshold (within the specified
<i>errorTolerance</i>), this actor outputs a discrete event with the value as
<i>defaultEventValue</i> if <i>useEventValue</i> is selected. Otherwise, the actor
outputs a discrete event with the value as the level threshold.
This actor controls the integration step size to accurately resolve the time 
at which the level crossing occurs. So, this actor is only used in Continuous
Time domain.

@author Jie Liu, Haiyang Zheng
@version $Id$
@since Ptolemy II 2.0
*/
public class LevelCrossingDetector extends Transformer
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
        new Parameter(input, "signalType",
                new StringToken("CONTINUOUS"));
        output.setTypeAtLeast(input);
        new Parameter(output, "signalType",
                new StringToken("DISCRETE"));
        trigger = new TypedIOPort(this, "trigger", true, false);
        trigger.setMultiport(false);
        trigger.setTypeEquals(BaseType.DOUBLE);
        new Parameter(trigger, "signalType",
                new StringToken("CONTINUOUS"));
        _level = 0.0;
        level = new Parameter(this, "level", new DoubleToken(0.0));
        level.setTypeEquals(BaseType.DOUBLE);

        defaultEventValue = new Parameter(this, "defaultEventValue",
                new DoubleToken(0.0));
        output.setTypeAtLeast(defaultEventValue);

        useEventValue = new Parameter(this, "useEventValue");
        useEventValue.setTypeEquals(BaseType.BOOLEAN);
        useEventValue.setToken(BooleanToken.FALSE);

        _errorTolerance = (double)1e-4;
        errorTolerance = new Parameter(this, "errorTolerance",
                new DoubleToken(_errorTolerance));

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The trigger port. Single port with type double.
     */
    public TypedIOPort trigger;

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

    /** The parameter that specifies the default output event value
     *  if the input port is not connected to any thing. If the
     *  input is connected, this value is ignored.
     *  By default, it contains a DoubleToken of value 0.0.
     */
    public Parameter defaultEventValue;

    /** The parameter that indicates whether to use the default event
     *  value.
     */
    public Parameter useEventValue;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
            if (p <= 0) {
                throw new IllegalActionException(this,
                        "Error tolerance must be greater than 0.");
            }
            _errorTolerance = p;
        } else if (attribute == level) {
            _levelChanged = true;
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
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        LevelCrossingDetector newObject = (LevelCrossingDetector)
            super.clone(workspace);
        // Set the type constraint.
        newObject.output.setTypeAtLeast(newObject.input);
        newObject.output.setTypeAtLeast(newObject.defaultEventValue);
        return newObject;
    }

    /** Consume the trigger token. The trigger token will be used
     *  for finding the level crossing in the isThisStepAccurate()
     *  method to control the step size. If it is discrete phase,
     *  and if the trigger equals the level (within the
     *  given error tolerance), output a discrete event with the value
     *  of specified level.
     *  @exception IllegalActionException If no token is available.
     */
    public void fire() throws IllegalActionException {
        CTDirector director = (CTDirector)getDirector();

        //consume the input.
        _thisTrigger = ((DoubleToken) trigger.get(0)).doubleValue();
        if (_debugging) {
            _debug("Consuming trigger Token " + _thisTrigger);
        }
        if ((input.getWidth() != 0) && input.hasToken(0)) {
            _inputToken = input.get(0);
        } else {
            _inputToken = null;
        }

        if (director.isDiscretePhase()) {
            if (hasCurrentEvent()) {
                // Emit event.
                if (((BooleanToken)useEventValue.getToken()).booleanValue()) {
                    output.send(0, defaultEventValue.getToken());
                    if (_debugging) {
                        _debug("Emitting event: " + defaultEventValue.getToken());
                    }
                } else {
                    output.send(0, new DoubleToken(_level));
                    if (_debugging) {
                        _debug("Emitting event: " + _level);
                    }
                }
                _eventNow = false;
            }
        }
        super.fire();
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
        _eventMissed = false;
        _enabled = true;
        _eventNow = false;
        if (_levelChanged) {
            _level = ((DoubleToken)level.getToken()).doubleValue();
            _levelChanged = false;
        }
        if (_debugging) {
            _debug("Initialization finished.");
        }
    }

    /** Return true if this step does not cross the threshold.
     *  The current trigger
     *  token will be compared to the previous trigger token. If they
     *  cross the level threshold, this step is not accurate.
     *  A special case is taken care so that if the previous trigger
     *  and the current trigger both equal to the level value,
     *  then no new event is
     *  detected. If this step crosses the level threshold,
     *  then the refined integration
     *  step size is computed by linear interpolation.
     *  If this is the first iteration after initialize() is called,
     *  then always return true, since there is no history to compare with.
     *  @return True if the trigger input in this integration step
     *          does not cross the level threshold.
     */
    public boolean isThisStepAccurate() {
        if (_first) {
            if (_debugging) {
                _debug("It is the first iteration, the step size is " +
                    "assumed to be accurate.");
            }
            _first = false;
            _eventNow = false;
            return true;
        }

        if (_debugging) {
            _debug("The last trigger is " + _lastTrigger);
            _debug("The current trigger is " + _thisTrigger);
        }

        // If at breakpoints, no step size refinement is necessary.
        // The step size is 0.0, and it is always accurate.
        if (((CTDirector)getDirector()).isBreakpointIteration()) {
            if (_debugging) {
                _debug("This is a breakpoint iteration.");
            }
            // Check if the discontinuity generates events.
            if ((_lastTrigger - _level) * (_thisTrigger - _level)
                    < 0.0) {
                if (_enabled) {
                    _eventNow = true;
                    if (_debugging)
                        _debug("Event is detected at "
                                + getDirector().getCurrentTime());
                    _enabled = false;
                }
                _eventMissed = false;
            }
            return true;
        }

        if (Math.abs(_thisTrigger - _level) < _errorTolerance) {
            if (_enabled && _eventMissed) {
                _eventNow = true;
                if (_debugging)
                    _debug("Event is detected at "
                            + getDirector().getCurrentTime());
                _enabled = false;
            }
            _eventMissed = false;
            return true;
        } else {
            if (!_enabled) {  // if last step is a level, always accurate.
                _enabled = true;
            } else {
                if ((_lastTrigger - _level) * (_thisTrigger - _level)
                        < 0.0) {

                    CTDirector dir = (CTDirector)getDirector();
                    _eventMissed = true;
                    // The refined step size is a linear interpolation.
                    _refineStep = (Math.abs(_lastTrigger - _level)
                            *dir.getCurrentStepSize())
                        /Math.abs(_thisTrigger-_lastTrigger);
                    if (_debugging) _debug(getFullName() +
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
        if (_debugging) {
            _debug("Called postfire()");
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

    /** If the level has changed during the last iteration, update
     *  the parameter value. Note that only after the calling of this
     *  method, would the new value of level be used for detection.
     *  @return Same as that in the super class.
     *  @exception IllegalActionException If the getToken() method
     *  of the level parameter throws it.
     */
    public boolean prefire() throws IllegalActionException {
        if (_levelChanged) {
            _level = ((DoubleToken)level.getToken()).doubleValue();
            _levelChanged = false;
        }
        return super.prefire();
    }

    /** Make sure the actor runs inside a CT domain.
     *  @exception IllegalActionException If the director is not
     *  a CTDirector or the parent class throws it.
     */
    public void preinitialize() throws IllegalActionException {
        if (!(getDirector() instanceof CTDirector)) {
            throw new IllegalActionException("LevelCrossingDetector can only" +
                " be used inside CT domain.");
        }
        super.preinitialize();
    }

    /** Return the refined step size if there is a missed event,
     *  otherwise return the current step size.
     *  @return The refined step size.
     */
    public double refinedStepSize() {
        if (_eventMissed) {
            return _refineStep;
        }
        return ((CTDirector)getDirector()).getCurrentStepSize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // the level crossing threshold.
    protected double _level;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

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

    // flag indicating if the event detection is enabled for this step
    private boolean _enabled;

    // flag indicating if there is an event at the current time.
    private boolean _eventNow = false;

    // flag indicating if this is the first iteration in the execution,
    private boolean _first = true;

    // the current input token.
    private  Token _inputToken;

    // Indicating whether the 'level' value has changed.
    private boolean _levelChanged;
}
