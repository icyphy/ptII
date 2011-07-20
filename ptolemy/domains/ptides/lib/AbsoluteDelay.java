/* An actor that delays the input by the exact amount specified through another port.

 Copyright (c) 1998-2011 The Regents of the University of California.
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

package ptolemy.domains.ptides.lib;

import java.util.HashSet;
import java.util.Set;

import ptolemy.actor.CausalityMarker;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.actor.util.CalendarQueue;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
////AbsoluteDelay

/**
This actor produces an output at model time equal to the value of outputTime.
the other port. This actor keeps track of all timestamps that are produced
previously, and would produce an output at timestamp of value max{t, outputTime},
where t is the current model time.

@author Jia Zou, Slobodan Matic
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating Yellow (jiazou)
@Pt.AcceptedRating Red (jiazou)
*/

public class AbsoluteDelay extends Transformer {

    /** Construct an actor with the specified container and name.
     *  @param container The composite entity to contain this one.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public AbsoluteDelay(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        
        delay = new Parameter(this, "delay");
        delay.setTypeEquals(BaseType.DOUBLE);
        delay.setExpression("1.0");
        _delay = 1.0;

        output.setTypeSameAs(input);
        outputTime = new TypedIOPort(this, "outputTime", true, false);

        // empty set of dependent ports.
        Set<Port> dependentPorts = new HashSet<Port>();
        dependentPorts.add(input);
        dependentPorts.add(outputTime);
        _causalityMarker = new CausalityMarker(this, "causalityMarker");
        _causalityMarker.addDependentPortSet(dependentPorts);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       ports and parameters                ////

    /** The amount of delay. The default for this parameter is 1.0.
     *  This parameter must contain a DoubleToken
     *  with a non-negative value, or an exception will be thrown when
     *  it is set.
     */
    public Parameter delay;


    /** The amount specifying delay. Its default value is 0.0.
     */
    public TypedIOPort outputTime;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is <i>delay</i>, then ensure that the value
     *  is non-negative.
     *  <p>NOTE: the newDelay may be 0.0, which may change the causality
     *  property of the model. We leave the model designers to decide
     *  whether the zero delay is really what they want.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the delay is negative.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == delay) {
            double newDelay = ((DoubleToken) (delay.getToken())).doubleValue();

            if (newDelay < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative delay: " + newDelay);
            } else {
                _delay = newDelay;
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. Set a type
     *  constraint that the output type is the same as the that of input.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        AbsoluteDelay newObject = (AbsoluteDelay) super.clone(workspace);
        newObject.output.setTypeSameAs(newObject.input);
        newObject._causalityMarker = (CausalityMarker) newObject
                .getAttribute("causalityMarker");
        return newObject;
    }

    /** Override the base class to declare that the <i>output</i>
     *  does not depend on the <i>outputTime</i> and <i>input</i>
     *  port in a firing.
     *  @exception IllegalActionException If the causality interface
     *  cannot be computed.
     *  @see #getCausalityInterface()
     */
    public void declareDelayDependency() throws IllegalActionException {
        // Declare that output does not immediately depend on the delay input
        // and the input port,
        // though there is no lower bound on the time delay.
        _declareDelayDependency(outputTime, output, 0.0);
        _declareDelayDependency(input, output, 0.0);
    }

    /** Update the delay parameter from the delay port and ensure the delay
     *  is not negative. Call the fire method of super class to consume
     *  inputs and generate outputs.
     *  @exception IllegalActionException If the super class throws it,
     *  or a negative delay is received.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        // FIXME: there's gotta be a better way to set a time in a Time object.
        for (int channelIndex = 0; channelIndex < outputTime.getWidth(); channelIndex++) {
            while (outputTime.hasToken(channelIndex)) {
                Time difference = _outputTime
                        .subtract(((DoubleToken) outputTime.get(channelIndex))
                                .doubleValue());
                _outputTime = _outputTime.subtract(difference);
            }
        }

        Time currentTime = getDirector().getModelTime();
        _currentOutput = null;

        while (_delayedOutputTokens.size() > 0) {
            TimedEvent earliestEvent = (TimedEvent) _delayedOutputTokens.get();
            Time eventTime = earliestEvent.timeStamp;

            int comparison = eventTime.compareTo(currentTime);
            if (comparison == 0) {
                _currentOutput = (Token) earliestEvent.contents;
                comparison = eventTime.compareTo(_outputTime);
                if (comparison >= 0) {
                    output.send(0, _currentOutput);
                } else {
                    _delayedOutputTokens.put(new TimedEvent(_outputTime,
                            earliestEvent.contents));
                    _fireAt(_outputTime);
                }
                break;
            } else if (comparison > 0) {
                // It is not yet time to produce an output.
                break;
            }
            // If we get here, then we have passed the time of the delayed
            // output. We simply discard it and check the next one in the queue.
            _delayedOutputTokens.take();
        }
    }
    
    /** Initialize the states of this actor.
     *  @exception IllegalActionException If a derived class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _currentOutput = null;
        _delayedOutputTokens = new CalendarQueue(
                new TimedEvent.TimeComparator());
    }
    
    /** Return false indicating that this actor can be fired even if
     *  the inputs are unknown.
     *  @return False.
     */
    public boolean isStrict() {
        return false;
    }

    /** Process the current input if it has not been processed. Schedule
     *  a firing to produce the earliest output token.
     *  @exception IllegalActionException If scheduling to refire cannot
     *  be performed or the superclass throws it.
     */
    public boolean postfire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        Time time = currentTime.subtract(_outputTime);
        if (time.isNegative()) {
            _delay = -time.getDoubleValue();
        } else {
            _delay = 0;
        }
        Time delayToTime = currentTime.add(_delay);

        // Remove the token that is sent at the current time.
        if (_delayedOutputTokens.size() > 0) {
            if (_currentOutput != null) {
                _delayedOutputTokens.take();
            }
        }

        // Handle the refiring of the multiple tokens
        // that are scheduled to be produced at the same time.
        if (_delayedOutputTokens.size() > 0) {
            TimedEvent earliestEvent = (TimedEvent) _delayedOutputTokens.get();
            Time eventTime = earliestEvent.timeStamp;

            if (eventTime.equals(currentTime)) {
                _fireAt(currentTime);
            }
        }

        // consume input
        if (input.hasToken(0)) {
            _delayedOutputTokens.put(new TimedEvent(delayToTime, input.get(0)));
            _fireAt(delayToTime);
        }
        return super.postfire();
    }

    /** Declare _outputTime parameter to be of Time with default value 0.0.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _outputTime = new Time(getDirector(), 0.0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Current output. */
    protected Token _currentOutput;

    /** The amount of delay. */
    protected double _delay;

    /** A local event queue to store the delayed output tokens. */
    protected CalendarQueue _delayedOutputTokens;

    /** A causality marker to store information about how pure events are causally
     *  related to trigger events.
     */
    protected CausalityMarker _causalityMarker;

    /** The amount of delay. */
    protected Time _outputTime;

    /** Zero time. */
    protected Time _zero;

}
