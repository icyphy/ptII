/* An actor that detects level crossings of its trigger input signal.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.domains.continuous.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.continuous.ContinuousStepSizeController;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.continuous.kernel.ContinuousDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// LevelCrossingDetector

/**
 An event detector that converts continuous signals to discrete events when
 the input <i>trigger</i> signal crosses a threshold specified by the <i>level</i>
 parameter. The <i>direction</i> parameter
 can constrain the actor to detect only rising or falling transitions.
 It has three possible values, "rising", "falling", and "both", where
 "both" is the default. This actor will produce an output whether the
 input is continuous or not. That is, if a discontinuity crosses the
 threshold in the right direction, it produces an output at the time
 of the discontinuity.  If the input is continuous,
 then the output is generated when the input is
 within <i>errorTolerance</i> of the level.
 The value of the output is given by the <i>value</i> parameter,
 which by default has the value of the <i>level</i> parameter.
  <p>
 This actor has a one microstep delay before it will produce an
 output. That is, when a level crossing is detected, the actor
 requests a refiring in the next microstep at the current time,
 and only in that refiring produces the output.
 This ensures that the output satisfies the piecewise
 continuity constraint. It is always absent at microstep 0.
<p>
 This actor will not produce an event at the time of the first firing
 unless there is a level crossing discontinuity at that time.

 @author Edward A. Lee, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Red (hyzheng)
 */
public class LevelCrossingDetector extends TypedAtomicActor implements
ContinuousStepSizeController {
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

        trigger = new TypedIOPort(this, "trigger", true, false);
        trigger.setMultiport(false);
        trigger.setTypeEquals(BaseType.DOUBLE);

        level = new Parameter(this, "level", new DoubleToken(0.0));
        level.setTypeEquals(BaseType.DOUBLE);

        value = new Parameter(this, "value");
        value.setExpression("level");

        // By default, this director detects both directions of level crossings.
        direction = new StringParameter(this, "direction");
        direction.setExpression("both");
        _detectRisingCrossing = true;
        _detectFallingCrossing = true;

        direction.addChoice("both");
        direction.addChoice("falling");
        direction.addChoice("rising");

        output.setTypeAtLeast(value);

        _errorTolerance = 1e-4;
        errorTolerance = new Parameter(this, "errorTolerance", new DoubleToken(
                _errorTolerance));
        errorTolerance.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** A parameter that can be used to limit the detected level crossings
     *  to rising or falling. There are three choices: "falling", "rising", and
     *  "both". The default value is "both".
     */
    public StringParameter direction;

    /** The error tolerance specifying how close the value of a continuous
     *  input needs to be to the specified level to produce the output event.
     *  Note that this indirectly affects the accuracy of the time of the
     *  output since the output can be produced at any time after the
     *  level crossing occurs while it is still within the specified
     *  error tolerance of the level. This is a double with default 1e-4.
     */
    public Parameter errorTolerance;

    /** The parameter that specifies the level threshold. By default, it
     *  contains a double with value 0.0. Note, a change of this
     *  parameter at run time will not be applied until the next
     *  iteration.
     */
    public Parameter level;

    /** The output value to produce when a level-crossing is detected.
     *  This can be any data type. It defaults to the same value
     *  as the <i>level</i> parameter.
     */
    public Parameter value;

    /** The output port. The type is at least the type of the
     *  <i>value</i> parameter.
     */
    public TypedIOPort output;

    /** The trigger port. This is an input port with type double.
     */
    public TypedIOPort trigger;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Update the attribute if it has been changed. If the attribute
     *  is <i>errorTolerance</i> or <i>level</i>, then update the local cache.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the attribute change failed.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == errorTolerance) {
            double tolerance = ((DoubleToken) errorTolerance.getToken())
                    .doubleValue();

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

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *  an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        LevelCrossingDetector newObject = (LevelCrossingDetector) super
                .clone(workspace);

        // Set the type constraints.
        newObject.output.setTypeAtLeast(newObject.value);
        return newObject;
    }

    /** Declare that the output does not depend on the input in a firing.
     *  @exception IllegalActionException If the causality interface
     *  cannot be computed.
     *  @see #getCausalityInterface()
     */
    @Override
    public void declareDelayDependency() throws IllegalActionException {
        _declareDelayDependency(trigger, output, 0.0);
    }

    /** Detect whether the current input compared to the input
     *  on the last iteration indicates that a level crossing in the
     *  appropriate direction has occurred, if the time is within
     *  <i>errorTolerance</i> of the time at which the crossing occurs.
     *  If there is such a level crossing, then postfire will request
     *  a refiring at the current time, and the next invocation of fire()
     *  will produce the output event.
     *  @exception IllegalActionException If it cannot get a token from the trigger
     *   port or cannot send a token through the output port.
     */
    @Override
    public void fire() throws IllegalActionException {
        ContinuousDirector dir = (ContinuousDirector) getDirector();
        double currentStepSize = dir.getCurrentStepSize();
        int microstep = dir.getIndex();
        _postponedOutputProduced = false;

        if (_debugging) {
            _debug("Called fire() at time " + dir.getModelTime()
                    + " with microstep " + microstep + " and step size "
                    + currentStepSize);
        }

        // If there is a postponed output, then produce it.
        // Need to use <= rather than == here because a modal
        // model may have been suspended when the microstep matched.
        if (_postponed > 0 && _postponed <= microstep) {
            if (_debugging) {
                _debug("-- Produce postponed output.");
            }
            output.send(0, value.getToken());
            _postponedOutputProduced = true;
        } else {
            // There is no postponed output, so send clear.
            if (_debugging) {
                _debug("-- Output is absent.");
            }
            output.sendClear(0);
        }

        // If the trigger input is available, record it.
        if (trigger.getWidth() > 0 && trigger.isKnown(0) && trigger.hasToken(0)) {
            _thisTrigger = ((DoubleToken) trigger.get(0)).doubleValue();
            if (_debugging) {
                _debug("-- Consumed a trigger input: " + _thisTrigger);
                _debug("-- Last trigger is: " + _lastTrigger);
            }

            // If first firing, do not look for a level crossing.
            if (_lastTrigger == Double.NEGATIVE_INFINITY) {
                return;
            }

            boolean inputIsIncreasing = _thisTrigger > _lastTrigger;
            boolean inputIsDecreasing = _thisTrigger < _lastTrigger;

            // If a crossing has occurred, and either the current step
            // size is zero or the current input is within error tolerance
            // of the level, then request a refiring.
            // Check whether _lastTrigger and _thisTrigger are on opposite sides
            // of the level.
            // NOTE: The code below should not set _eventMissed = false because
            // an event may be missed during any stage of speculative execution.
            // This should be set to false only in postfire.
            if ((_lastTrigger - _level) * (_thisTrigger - _level) < 0.0
                    || _thisTrigger == _level) {
                // Crossing has occurred. Check whether the direction is right.
                // Note that we do not produce an output is the input is neither
                // increasing nor decreasing. Presumably, we already produced
                // an output in that case.
                if (_detectFallingCrossing && inputIsDecreasing
                        || _detectRisingCrossing && inputIsIncreasing) {
                    // If the step size is not 0.0, and the
                    // current input is not close enough, then we
                    // have missed an event and the step size will need
                    // to be adjusted.
                    if (currentStepSize != 0.0
                            && Math.abs(_thisTrigger - _level) >= _errorTolerance) {
                        // Step size is nonzero and the current input is not
                        // close enough. We have missed an event.
                        if (_debugging) {
                            _debug("-- Missed an event. Step size will be adjusted.");
                        }
                        _eventMissed = true;
                    } else {
                        // Request a refiring.
                        _postponed = microstep + 1;
                    }
                }
            }
        }
    }

    /** Initialize the execution.
     *  @exception IllegalActionException If thrown by the super class.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _eventMissed = false;
        _level = ((DoubleToken) level.getToken()).doubleValue();
        _lastTrigger = Double.NEGATIVE_INFINITY;
        _thisTrigger = _lastTrigger;
        _postponed = 0;
        _postponedOutputProduced = false;
    }

    /** Return false if with the current step size we miss a level crossing.
     *  @return False if the step size needs to be refined.
     */
    @Override
    public boolean isStepSizeAccurate() {
        if (_debugging) {
            _debug("Step size is accurate: " + !_eventMissed);
        }
        return !_eventMissed;
    }

    /** Return false. This actor can produce some outputs even the
     *  inputs are unknown. This actor is usable for breaking feedback
     *  loops.
     *  @return False.
     */
    @Override
    public boolean isStrict() {
        return false;
    }

    /** Prepare for the next iteration, by making the current trigger
     *  token to be the history trigger token.
     *  @return True always.
     *  @exception IllegalActionException If thrown by the super class.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called postfire().");
        }

        _lastTrigger = _thisTrigger;
        _eventMissed = false;
        if (_postponed > 0) {
            if (_debugging) {
                _debug("Requesting refiring at the current time.");
            }
            getDirector().fireAtCurrentTime(this);
        }
        if (_postponedOutputProduced) {
            _postponedOutputProduced = false;
            // There might be yet another postponed output requested.
            // If the current microstep matches _postponed, then there
            // there is not, and we can reset _postponed.
            ContinuousDirector dir = (ContinuousDirector) getDirector();
            int microstep = dir.getIndex();
            if (microstep >= _postponed) {
                _postponed = 0;
            }
        }
        return super.postfire();
    }

    /** Make sure the actor runs with a ContinuousDirector.
     *  @exception IllegalActionException If the director is not
     *  a ContinuousDirector or the parent class throws it.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        if (!(getDirector() instanceof ContinuousDirector)) {
            throw new IllegalActionException("LevelCrossingDetector can only"
                    + " be used inside Continuous domain.");
        }
        super.preinitialize();
    }

    /** Return the refined step size if there is a missed event,
     *  otherwise return the current step size.
     *  @return The refined step size.
     */
    @Override
    public double refinedStepSize() {
        ContinuousDirector dir = (ContinuousDirector) getDirector();
        double refinedStep = dir.getCurrentStepSize();

        if (_eventMissed) {
            // The refined step size is a linear interpolation.
            // NOTE: we always to get a little overshoot to make sure the
            // level crossing happens. The little overshoot chosen here
            // is half of the error tolerance.
            refinedStep = (Math.abs(_lastTrigger - _level) + _errorTolerance / 2)
                    * dir.getCurrentStepSize()
                    / Math.abs(_thisTrigger - _lastTrigger);

            if (_debugging) {
                _debug(getFullName() + "-- Event Missed: refine step to "
                        + refinedStep);
            }
            // Reset this because the iteration will be repeated with a new step size.
            // The new iteration may not miss the event.
            _eventMissed = false;
        }
        return refinedStep;
    }

    /** Return the maximum Double value. This actor does not suggest
     *  or constrain the step size for the next iteration.
     *  @return java.Double.MAX_VALUE.
     */
    @Override
    public double suggestedStepSize() {
        return java.lang.Double.MAX_VALUE;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The level threshold this actor detects. */
    protected double _level;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Flag indicating whether this actor detects the level crossing
    // when the input value is rising.
    private boolean _detectRisingCrossing;

    // Flag indicating whether this actor detects the level crossing
    // when the input value is falling.
    private boolean _detectFallingCrossing;

    // Cache of the value of errorTolerance.
    private double _errorTolerance;

    // Flag indicating a missed event.
    private boolean _eventMissed = false;

    // Last trigger input.
    private double _lastTrigger;

    // Indicator that the output is postponed to the specified microstep.
    private int _postponed;

    // Indicator that the postponed output was produced.
    private boolean _postponedOutputProduced;

    // This trigger input.
    private double _thisTrigger;
}
