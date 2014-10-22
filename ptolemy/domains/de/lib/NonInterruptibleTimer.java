/* A non-interruptible timer that produces an event with a time delay
 specified by the input.

 Copyright (c) 2004-2014 The Regents of the University of California.
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

import java.util.LinkedList;

import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// NonInterruptibleTimer

/**
 A NonInterruptibleTimer actor works similar to the {@link Timer} actor,
 except that if a NonInterruptibleTimer actor has not finished processing
 the previous input, a new input has to be delayed for processing.
 In other words, it can not be interrupted to respond new inputs. Instead,
 the new inputs will be queued and processed in a first come first serve
 (FCFS) fashion. This actor extends the Timer actor.
 <p>
 The key difference between the NonInterruptibleTimer actor and the Server
 actor is how the service time is specified.  In the NonInterruptibleTimer
 actor, whenever an input arrives, the value of the input token specifies
 the service time.  This actor will guarantee that much service time to be
 given to that input.  In the Server actor, service times for inputs ar
 decided by the ServiceTime parameter, which may change any time during an
 execution. In particular, how much service time an input actually gets is
 decided the value of the ServiceTime parameter at the time the server is
 ready to serve that input.

 @see Timer
 @author Haiyang Zheng
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Red (hyzheng)
 @deprecated Use ptolemy.actor.lib.ResettableTimer.
 */
@Deprecated
public class NonInterruptibleTimer extends Timer {
    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public NonInterruptibleTimer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read one token from the input. Send out a token that is scheduled
     *  to produce at the current time to the output.
     *
     *  @exception IllegalActionException If the delay value is negative, or
     *  this actor can not send tokens to ports, or this actor can not get
     *  tokens from ports.
     */
    @Override
    public void fire() throws IllegalActionException {
        // Don't call "super.fire();" here, the parent class is an actor.
        if (_debugging) {
            // We usually do this in the parent class hierarchy, but since
            // we are not calling super.fire(), we do it here.
            _debug("NonInterruptibleTimer: Called fire()");
        }
        _delay = -1.0;

        if (input.hasToken(0)) {
            _currentInput = input.get(0);
            _delayedInputTokensList.addLast(_currentInput);

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
            if (currentTime.compareTo(_nextTimeFree) == 0) {
                TimedEvent earliestEvent = (TimedEvent) _delayedOutputTokens
                        .get();
                Time eventTime = earliestEvent.timeStamp;

                if (!eventTime.equals(currentTime)) {
                    throw new InternalErrorException("Timer time is "
                            + "reached, but output is not available.");
                }

                _currentOutput = (Token) earliestEvent.contents;
                output.send(0, _currentOutput);
                return;
            } else {
                // no tokens to be produced at the current time.
            }
        }
    }

    /** Reset the states of the server to indicate that the timer is not
     *  processing any inputs.
     *  @exception IllegalActionException If the base class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _nextTimeFree = Time.NEGATIVE_INFINITY;
        _delayedInputTokensList = new LinkedList();
    }

    /** If there are delayed inputs that are not processed and the timer
     *  is not busy, begin processing the earliest input and schedule
     *  a future firing to produce it.
     *  @exception IllegalActionException If there is no director or can not
     *  schedule future firings to handle delayed input events.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();

        // Remove the current output token from _delayedTokens.
        if (_currentOutput != null) {
            _delayedOutputTokens.take();
        }

        // If the delayedInputTokensList is not empty and the
        // delayedOutputTokens is empty (meaning the timer is ready to process
        // a new input), get the first input in the delayedInputTokensList,
        // put it into the delayedOutputTokens, and begin processing it.
        // Schedule a refiring to produce the corresponding
        // output at the time: current time + delay specified by the input
        // being processed.
        if (_delayedInputTokensList.size() != 0
                && _delayedOutputTokens.isEmpty()) {
            // NOTE: the input has a fixed data type as double.
            DoubleToken delayToken = (DoubleToken) _delayedInputTokensList
                    .removeFirst();
            double delay = delayToken.doubleValue();
            _nextTimeFree = currentTime.add(delay);
            _delayedOutputTokens.put(new TimedEvent(_nextTimeFree, value
                    .getToken()));
            _fireAt(_nextTimeFree);
        }

        return !_stopRequested;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Next time the server becomes free.
    private Time _nextTimeFree;

    // List of delayed input tokens, whose finishing times can not be decided.
    private LinkedList _delayedInputTokensList;
}
