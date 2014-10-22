/* An actor that delays the input by the amount specified through another port.

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
package ptolemy.domains.de.lib;

import ptolemy.actor.lib.Transformer;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.util.CalendarQueue;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// VariableDelay

/**
 This actor delays its inputs by a variable delay.
 It works in a similar way as the TimedDelay actor except that the
 amount of time delayed is specified by an incoming token through
 the delay port (a parameter port). If the delay is zero, then this
 actor only increments the microstep, not the model time.
 If a negative delay is specified, then an exception is thrown.

 @deprecated Use ptolemy.actor.lib.TimeDelay.
 @see ptolemy.domains.de.lib.TimedDelay
 @author Jie Liu, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Yellow (hyzheng)
 */
@Deprecated
public class VariableDelay extends Transformer {

    // NOTE: This actor has alot copies from TimeDelay, but because it has
    // a PortParameter named delay and TimeDelay has just a parameter,
    // subclassing does not work well.

    /** Construct an actor with the specified container and name.
     *  @param container The composite entity to contain this one.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public VariableDelay(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        delay = new PortParameter(this, "delay");
        delay.setExpression("1.0");
        delay.setTypeEquals(BaseType.DOUBLE);

        output.setTypeSameAs(input);

        _delay = 1.0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       ports and parameters                ////

    // FIXME: VariableDelay.delay overrides TimedDelay.delay.
    /** The amount specifying delay. Its default value is 1.0.
     */
    public PortParameter delay;

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
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == delay) {
            double newDelay = ((DoubleToken) delay.getToken()).doubleValue();

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
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        VariableDelay newObject = (VariableDelay) super.clone(workspace);
        newObject.output.setTypeSameAs(newObject.input);
        return newObject;
    }

    /** Declare that the <i>output</i>
     *  does not depend on the <i>input</i> and <i>delay</i> in a firing.
     *  @exception IllegalActionException If the causality interface
     *  cannot be computed.
     *  @see #getCausalityInterface()
     */
    @Override
    public void declareDelayDependency() throws IllegalActionException {
        // Declare that output does not immediately depend on the delay input
        // and the input port,
        // though there is no lower bound on the time delay.
        _declareDelayDependency(delay.getPort(), output, 0.0);
        _declareDelayDependency(input, output, 0.0);
    }

    /** Update the delay parameter from the delay port and ensure the delay
     *  is not negative. Call the fire method of super class to consume
     *  inputs and generate outputs.
     *  @exception IllegalActionException If the super class throws it,
     *  or a negative delay is received.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        delay.update();
        _delay = ((DoubleToken) delay.getToken()).doubleValue();

        if (_delay < 0) {
            throw new IllegalActionException(this,
                    "Cannot have negative delay: " + _delay);
        }

        // produce output
        // NOTE: The amount of delay may be zero.
        // In this case, if there is already some token scheduled to
        // be produced at the current time before the current input
        // arrives, that token is produced. While the current input
        // is delayed to the next available firing at the current time.
        //
        // If we observe events in the queue that have expired,
        // discard them here.
        Time currentTime = getDirector().getModelTime();
        _currentOutput = null;

        if (_delayedOutputTokens.size() == 0) {
            output.send(0, null);
            return;
        }

        while (_delayedOutputTokens.size() > 0) {
            TimedEvent earliestEvent = (TimedEvent) _delayedOutputTokens.get();
            Time eventTime = earliestEvent.timeStamp;

            int comparison = eventTime.compareTo(currentTime);
            if (comparison == 0) {
                _currentOutput = (Token) earliestEvent.contents;
                output.send(0, _currentOutput);
                break;
            } else if (comparison > 0) {
                // It is not yet time to produce an output.
                output.send(0, null);
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
    @Override
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
    @Override
    public boolean isStrict() {
        return false;
    }

    /** Process the current input if it has not been processed. Schedule
     *  a firing to produce the earliest output token.
     *  @exception IllegalActionException If scheduling to refire cannot
     *  be performed or the superclass throws it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
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

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Current output. */
    protected Token _currentOutput;

    /** The amount of delay. */
    protected double _delay;

    /** A local event queue to store the delayed output tokens. */
    protected CalendarQueue _delayedOutputTokens;

}
