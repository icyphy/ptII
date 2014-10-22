/* A timer that produces an event with a time delay specified by the input.

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

import ptolemy.actor.util.CalendarQueue;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// Timer

/**
 A timer actor delays an event with a time delay specified by its input.
 <p>
 When a timer actor receives an input, if the input value is
 bigger than 0.0, the timer schedules itself to fire again some time
 later to generate an output. The amount of delay is specified by the
 input value. The value of output is specified by the <i>value</i>
 parameter of this actor. If the input value is 0.0, an output is
 produced in the next firing with a bigger microstep. If the input is
 less than 0.0, an exception will be thrown.

 <p> This actor is different from the {@link
 ptolemy.domains.de.lib.NonInterruptibleTimer} actor because the
 NonInterruptibleTimer actor delays the processing of a new input if
 it has not finished processing a previous input, while the
 Timer actor begins processing inputs immediately upon their arrival.

 @author Jie Liu, Edward A. Lee, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Yellow (hyzheng)
 @deprecated Use VariableDelay instead, which is essentially identical,
  or ResettableTimer for a more reasonable timer behavior.
 */
@Deprecated
public class Timer extends DETransformer {
    /** Construct an actor with the specified container and name.
     *  Declare that the input can only receive double tokens and the output
     *  has a data type the same as the value parameter.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Timer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        value = new Parameter(this, "value", new BooleanToken(true));
        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeSameAs(value);
    }

    ///////////////////////////////////////////////////////////////////
    ////                      ports and parameters                 ////

    /** The value produced at the output.  This can have any type,
     *  and it defaults to a boolean token with value <i>true</i>.
     */
    public Parameter value;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class, links the type of the <i>value</i> parameter
     *  to the output and sets the data type of the input to be double.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Timer newObject = (Timer) super.clone(workspace);
        newObject.output.setTypeSameAs(newObject.value);
        newObject.input.setTypeEquals(BaseType.DOUBLE);
        newObject._causalityInterface = null;
        return newObject;
    }

    /** Declare that the <i>output</i>
     *  does not depend on the <i>input</i> in a firing.
     *  @exception IllegalActionException If the causality interface
     *  cannot be computed.
     *  @see #getCausalityInterface()
     */
    @Override
    public void declareDelayDependency() throws IllegalActionException {
        _declareDelayDependency(input, output, 0.0);
    }

    /** Read one token from the input. Send out a token that is scheduled
     *  to produce at the current time to the output.
     *
     *  @exception IllegalActionException If there is no director, or can not
     *  send or get tokens from ports.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        _delay = -1.0;

        if (input.hasToken(0)) {
            _currentInput = input.get(0);

            double delayValue = ((DoubleToken) _currentInput).doubleValue();

            if (delayValue < 0) {
                throw new IllegalActionException("Delay can not be negative.");
            } else {
                _delay = delayValue;
            }
        } else {
            _currentInput = null;
        }

        Time currentTime = getDirector().getModelTime();
        _currentOutput = null;

        if (_delayedOutputTokens.size() > 0) {
            TimedEvent earliestEvent = (TimedEvent) _delayedOutputTokens.get();
            Time eventTime = earliestEvent.timeStamp;

            if (eventTime.equals(currentTime)) {
                _currentOutput = (Token) earliestEvent.contents;
                output.send(0, value.getToken());
                return;
            } else {
                // no tokens to be produced at the current time.
            }
        }
    }

    /** Initialize the internal states of this actor.
     *  @exception IllegalActionException If a derived class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _currentInput = null;
        _currentOutput = null;
        _delayedOutputTokens = new CalendarQueue(
                new TimedEvent.TimeComparator());
    }

    /** Update the internal states of this actor. If the current input
     *  is not processed in the fire method, schedule a refiring of this
     *  actor to produce an output in a future time,
     * (the current model time + delay specified by the input value).
     *
     *  @exception IllegalActionException If scheduling to refire cannot
     *  be performed or the superclass throws it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        Time delayToTime = currentTime.add(_delay);

        // Remove the token that is already sent at the current time.
        if (_delayedOutputTokens.size() > 0) {
            if (_currentOutput != null) {
                _delayedOutputTokens.take();
            }
        }

        // handle the refiring of the multiple tokens
        // that are scheduled to produce at the same time.
        if (_delayedOutputTokens.size() > 0) {
            TimedEvent earliestEvent = (TimedEvent) _delayedOutputTokens.get();
            Time eventTime = earliestEvent.timeStamp;

            if (eventTime.equals(currentTime)) {
                _fireAt(currentTime);
            }
        }

        // Schedule a future firing to process the current input.
        if (_currentInput != null) {
            _delayedOutputTokens.put(new TimedEvent(delayToTime, value
                    .getToken()));
            _fireAt(delayToTime);
        }

        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The causality interface, if it has been created. */
    protected CausalityInterface _causalityInterface;

    /** The amount of delay. */
    protected double _delay;

    /** A local queue to store the delayed tokens. */
    protected CalendarQueue _delayedOutputTokens;

    /** Current input. */
    protected Token _currentInput;

    /** Current output. */
    protected Token _currentOutput;
}
