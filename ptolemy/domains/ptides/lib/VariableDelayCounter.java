/* An actor that delays the input by the amount specified through another port, while counting.

 Copyright (c) 1998-2010 The Regents of the University of California.
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
import ptolemy.actor.Director;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.lib.VariableDelay;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
////VariableDelayCounter

/**
This actor delays its inputs by a variable delay, while producing
outputs in timestamp order.
It works in a similar way as the TimedDelay actor except that the
amount of time delayed is specified by an incoming token through
the delay port (a parameter port).

@see ptolemy.domains.de.lib.VariableDelay
@author Jia Zou, Slobodan Matic
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating Yellow (jiazou)
@Pt.AcceptedRating Red (jiazou)
*/

public class VariableDelayCounter extends VariableDelay {
    /** Construct an actor with the specified container and name.
     *  @param container The composite entity to contain this one.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public VariableDelayCounter(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       ports and parameters                ////

    /** Keeps the current counting state. */
    private int _count;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read one token from the input. Send out a token that is scheduled
     *  to produce at the current time to the output.
     *  @exception IllegalActionException If there is no director, or the
     *  input can not be read, or the output can not be sent.
     */
    public void fire() throws IllegalActionException {
        delay.update();
        _delay = ((DoubleToken) delay.getToken()).doubleValue();

        if (_delay < 0) {
            throw new IllegalActionException("Can not have a "
                    + "negative delay: " + _delay + ". "
                    + "Check whether overflow happens.");
        }

        // consume input
        if (input.hasToken(0)) {
            _currentOutput = input.get(0);
        } else {
            _currentOutput = null;
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
                _currentOutput = new IntToken(_count);
                output.send(0, _currentOutput);
            }
        }
    }

    /** Override the base class to zero the counter.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _count = 0;
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
                // if we sent out some token, then increment the count.
                _count++;
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

        // Process the current input.
        if (_currentOutput != null) {
            _delayedOutputTokens
                    .put(new TimedEvent(delayToTime, _currentOutput));
            _fireAt(delayToTime);
        }

        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Request a firing of this actor at the specified time
     *  and throw an exception if the director does not agree to
     *  do it at the requested time. This is a convenience method
     *  provided because many actors need it.
     *  <p>
     *  If the executive director is a Ptides director, use
     *  fireAt(Actor, Time, IOPort) method because the pure event this
     *  actor generates is always safe to process.
     *  </p>
     *  @param time The requested time.
     *  @exception IllegalActionException If the director does not
     *   agree to fire the actor at the specified time, or if there
     *   is no director.
     */
    protected void _fireAt(Time time) throws IllegalActionException {
        Director director = getDirector();
        if (director == null) {
            throw new IllegalActionException(this, "No director.");
        }
        Time result = director.fireAt(this, time);
        if (!result.equals(time)) {
            throw new IllegalActionException(this,
                    "Director is unable to fire the actor at the requested time: "
                            + time + ". It responds it will fire it at: "
                            + result);
        }
    }

    /** Override the method of the super class to initialize the
     *  parameter values.
     */
    protected void _init() throws NameDuplicationException,
            IllegalActionException {
        delay = new PortParameter(this, "delay");
        delay.setExpression("1.0");
        delay.setTypeEquals(BaseType.DOUBLE);

        Set<Port> dependentPorts = new HashSet<Port>();
        dependentPorts.add(input);
        dependentPorts.add(delay.getPort());
        _causalityMarker = new CausalityMarker(this, "causalityMarker");
        _causalityMarker.addDependentPortSet(dependentPorts);

    }
}
