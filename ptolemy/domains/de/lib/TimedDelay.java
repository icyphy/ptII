/* An actor that delays the input by the specified amount.

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
package ptolemy.domains.de.lib;

import ptolemy.actor.util.CalendarQueue;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;


//////////////////////////////////////////////////////////////////////////
//// TimedDelay

/**
   This actor delays the input by a specified amount of time. The amount
   of the time is required to be non-negative and has a default value 1.0.
   The input and output types are unconstrained, except that the output type
   must be the same as that of the input.
   <p>
   The behavior of this actor on each firing is to read a token from the input,
   if there is one, and schedule itself to fire again to produce that token
   on the corresponding output channel after the appropriate time delay.
   Note that if the value of delay is 0.0, and there is no output scheduled
   to produce at the same time the input arrives, the output is produced
   immediately. Otherwise, the input is produced in the next available firing
   at the same model time. If there is no input token, then no output token is
   produced.
   <p>
   Occasionally, this actor is used inside a feedback loop just for scheduling
   purpose, where the delay parameter is set to zero. This implies that no
   output token is produced earlier than the time its trigger input arrives.
   Therefore the actor declares that there is a delay between the input
   and the output, and the DE director will leverage this when
   determining the precedences of the actors. It is sometimes useful to think
   of this zero-valued delay as an infinitesimal delay.
   <p>
   The output may have the same microstep as the input, if there is
   no queued output scheduled to produce at the same time the input arrives.
   Otherwise, the output is produced one microstep later. This guarantees that
   a DE signal is functional in the sense that for any tag, there is
   at most one value.

   @see ptolemy.actor.util.FunctionDependencyOfAtomicActor
   @see ptolemy.domains.de.lib.VariableDelay
   @see ptolemy.domains.de.lib.Server

   @author Edward A. Lee, Lukito Muliadi, Haiyang Zheng
   @version $Id$
   @since Ptolemy II 1.0
   @Pt.ProposedRating Green (hyzheng)
   @Pt.AcceptedRating Yellow (hyzheng)
*/
public class TimedDelay extends DETransformer {
    /** Construct an actor with the specified container and name.
     *  Constrain that the output type to be the same as the input type.
     *  @param container The composite entity to contain this one.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TimedDelay(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // NOTE: The _init method is used to allow classes that extend
        // this class to reconfig their settings. This may not be a
        // good pattern.
        _init();
        output.setTypeSameAs(input);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       ports and parameters                ////

    /** The amount of delay. The default for this parameter is 1.0.
     *  This parameter must contain a DoubleToken
     *  with a non-negative value, or an exception will be thrown when
     *  it is set.
     */
    public Parameter delay;

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
        TimedDelay newObject = (TimedDelay) super.clone(workspace);
        newObject.output.setTypeSameAs(newObject.input);
        return newObject;
    }

    /** Read one token from the input. Send out a token that is scheduled
     *  to produce at the current time to the output.
     *  @exception IllegalActionException If there is no director, or the
     *  input can not be read, or the output can not be sent.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        // consume input
        if (input.hasToken(0)) {
            _currentInput = input.get(0);
        } else {
            _currentInput = null;
        }

        // produce output
        // NOTE: The amount of delay may be zero.
        // In this case, if there is already some token scheduled to
        // be produced at the current time before the current input
        // arrives, that token is produced. While the current input
        // is delayed to the next available firing at the current time.
        Time currentTime = getDirector().getModelTime();
        _currentOutput = null;

        if (_delayedOutputTokens.size() > 0) {
            TimedEvent earliestEvent = (TimedEvent) _delayedOutputTokens.get();
            Time eventTime = earliestEvent.timeStamp;

            if (eventTime.equals(currentTime)) {
                _currentOutput = (Token) earliestEvent.contents;
                output.send(0, _currentOutput);
            } else {
                // no tokens to be produced at the current time.
            }
        }

        if ((_delay == 0) && (_currentInput != null)
                && (_currentOutput == null)) {
            output.send(0, _currentInput);
            _currentInput = null;
        }
    }

    /** Initialize the states of this actor.
     *  @exception IllegalActionException If a derived class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _currentInput = null;
        _currentOutput = null;
        _delayedOutputTokens = new CalendarQueue(new TimedEvent.TimeComparator(
                                                         this.getDirector()));
    }

    /** Process the current input if it has not been processed. Schedule
     *  a firing to produce the earliest output token.
     *  @exception IllegalActionException If scheduling to refire cannot
     *  be performed or the superclass throws it.
     */
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
                getDirector().fireAt(this, currentTime);
            }
        }

        // Process the current input if it is not processed.
        if (_currentInput != null) {
            _delayedOutputTokens.put(new TimedEvent(delayToTime, _currentInput));
            getDirector().fireAt(this, delayToTime);
        }

        return super.postfire();
    }

    /** Override the base class to declare that the <i>output</i>
     *  does not depend on the <i>input</i> in a firing.
     */
    public void pruneDependencies() {
        super.pruneDependencies();
        removeDependency(input, output);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected method                    ////

    /** Initialize the delay parameter.
     *  @exception IllegalActionException If delay parameter cannot be set.
     *  @exception NameDuplicationException If there already is a parameter
     *  named "delay".
     */
    protected void _init()
            throws IllegalActionException, NameDuplicationException {
        delay = new Parameter(this, "delay", new DoubleToken(1.0));
        delay.setTypeEquals(BaseType.DOUBLE);
        _delay = ((DoubleToken) delay.getToken()).doubleValue();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Current input.
     */
    protected Token _currentInput;

    /** Current output.
     */
    protected Token _currentOutput;

    /** The amount of delay.
     */
    protected double _delay;

    /** A local event queue to store the delayed output tokens.
     */
    protected CalendarQueue _delayedOutputTokens;
}
