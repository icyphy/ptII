/* An actor that delays the input by the exact amount specified through another port.

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

import java.util.Set;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.TimeDelay;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

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

public class AbsoluteDelay extends TimeDelay {

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

        outputTime = new TypedIOPort(this, "outputTime", true, false);
        _zero = new Time(getDirector());

        Set<Port> dependentPorts = _causalityMarker.causalityMarker.get(0);
        dependentPorts.add(input);
        dependentPorts.add(outputTime);
        _causalityMarker.addDependentPortSet(dependentPorts);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       ports and parameters                ////

    /** The amount specifying delay. Its default value is 0.0.
     */
    public TypedIOPort outputTime;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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

    /** Process the current input if it has not been processed. Schedule
     *  a firing to produce the earliest output token.
     *  @exception IllegalActionException If scheduling to refire cannot
     *  be performed or the superclass throws it.
     */
    public boolean postfire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        Time time = currentTime.subtract(_outputTime);
        if (time.compareTo(_zero) < 0) {
            _delay = -time.getDoubleValue();
        } else {
            _delay = 0;
        }
        return super.postfire();
    }

    /** Declare _outputTime parameter to be of Time with default value 0.0
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _outputTime = new Time(getDirector(), 0.0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the method of the super class to initialize the
     *  parameter values.
     *  @exception IllegalActionException Not thrown in this class.
     *  @exception NameDuplicationException Not thrown in this class.
     */
    protected void _init() throws NameDuplicationException,
            IllegalActionException {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The amount of delay. */
    protected Time _outputTime;

    /** Zero time.
     */
    protected Time _zero;

}
