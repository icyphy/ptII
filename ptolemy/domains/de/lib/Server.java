/* A server with a fixed or variable service time.

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

import java.util.LinkedList;

import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

//////////////////////////////////////////////////////////////////////////
//// Server

/**
 This actor models a server with a fixed or variable service time.
 A server is either busy (serving a customer) or not busy at any given time.
 If an input arrives when the server is not busy, then the input token is
 produced on the output with a delay given by the <i>newServiceTime</i>
 parameter.
 If an input arrives while the server is busy, then that input is
 queued until the server becomes free, at which point it is produced
 on the output with a delay given by the <i>newServiceTime</i> parameter.
 If several inputs arrive while the server is busy, then they are
 served on a first-come, first-served basis.
 <p>
 If the <i>newServiceTime</i> parameter is not set, it defaults to 1.0.
 The value of the parameter can be changed at any time during execution
 of the model by providing an input event at the <i>newServiceTime</i>
 input port.  The token read at that port replaces the value of the
 <i>newServiceTime</i> parameter.
 <p>
 This actor declares that there is delay between the <i>input</i>
 and the <i>output</i> ports and between <i>newServiceTime</i>
 and <i>output</i>.  The director uses this information for
 assigning priorities to firings.
 <p>
 Like the TimedDelay actor, the output is produced with a future
 time stamp (larger than current time by <i>newServiceTime</i>).  If
 the service time is always zero and several events arrive at the
 same time, the server will output the first available input and
 queue the other inputs to process in the future microsteps. A
 service time of zero can be usefully viewed as an infinitesimal
 service time. See {@link TimedDelay}.
 <p>
 The key difference between the NonInterruptibleTimer actor and the Server
 actor is how the service time is specified.  In the NonInterruptibleTimer
 actor, whenever an input arrives, the value of the input token specifies
 the service time. This actor will guarantee that much service time to be
 given to that input.  In the Server actor, service times for inputs ar
 decided by the ServiceTime parameter, which may change any time during an
 execution. In particular, how much service time an input actually gets is
 decided the value of the ServiceTime parameter at the time the server is
 ready to serve that input.

 @see ptolemy.domains.de.lib.TimedDelay
 @see ptolemy.domains.de.lib.VariableDelay

 @author Lukito Muliadi, Edward A. Lee, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Yellow (hyzheng)
 */
public class Server extends VariableDelay {
    /** Construct an actor with the specified container and name.
     *  @param container The composite entity to contain this one.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Server(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read the available input token. If the server is not busy,
     *  begin servicing it. If the delay is 0, output is immediately available.
     *  Otherwise, the output available time is delayed by the amount of the
     *  <i>newServiceTime></i> parameter. If the server is busy, check
     *  whether the current service finishes. If so, generate output. Otherwise,
     *  do nothing.
     *  @exception IllegalActionException If can not update the serviceTime
     *  parameter, read inputs, or send outputs.
     */
    public void fire() throws IllegalActionException {
        // Don't call "super.fire();" here, the parent class is an actor.
        // update delay value
        delay.update();
        _delay = ((DoubleToken) delay.getToken()).doubleValue();

        Time currentTime = getDirector().getModelTime();

        // consume input and put it into the _delayedInputTokensList
        // NOTE: this list is different from the _delayedOutputTokens defined 
        // in the TimedDelay class.
        if (input.hasToken(0)) {
            _currentInput = input.get(0);
            _delayedInputTokensList.addLast(_currentInput);
        } else {
            _currentInput = null;
        }

        // produce output
        _currentOutput = null;

        if (_delayedOutputTokens.size() > 0) {
            if (currentTime.compareTo(_nextTimeFree) == 0) {
                TimedEvent earliestEvent = (TimedEvent) _delayedOutputTokens
                        .get();
                Time eventTime = earliestEvent.timeStamp;

                if (!eventTime.equals(currentTime)) {
                    throw new InternalErrorException("Service time is "
                            + "reached, but output is not available.");
                }

                _currentOutput = (Token) earliestEvent.contents;
                output.send(0, _currentOutput);
            } else {
                // no tokens to be produced at the current time.
            }
        }
    }

    /** Reset the states of the server to indicate that the server is ready
     *  to serve.
     *  @exception IllegalActionException If the base class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _nextTimeFree = Time.NEGATIVE_INFINITY;
        _delayedInputTokensList = new LinkedList();
    }

    /** If there are delayed input events that are not processed and the
     *  server is ready, begin process the earliest input event and schedule
     *  future firings to produce them.
     *  @exception IllegalActionException If there is no director or can not
     *  schedule future firings to handle delayed input events.
     *  @return True if the stop is not requested.
     */
    public boolean postfire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();

        // Remove the current output token from _delayedTokens.
        // NOTE: In this server class, the _delayedTokens can have
        // at most one token inside (like a processor can execute
        // at most one process at any time.)
        if (_currentOutput != null) {
            _delayedOutputTokens.take();
        }

        // If the delayedInputTokensList is not empty, and the delayedTokens
        // is empty (ready for a new service), get the first token
        // and put it into service. Schedule a refiring to wave up
        // after the service finishes.
        if ((_delayedInputTokensList.size() != 0)
                && _delayedOutputTokens.isEmpty()) {
            _nextTimeFree = currentTime.add(_delay);
            _delayedOutputTokens.put(new TimedEvent(_nextTimeFree,
                    _delayedInputTokensList.removeFirst()));
            getDirector().fireAt(this, _nextTimeFree);
        }

        return !_stopRequested;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected method                    ////

    /** Override the method of the super class to initialize parameters.
     */
    protected void _init() throws NameDuplicationException,
            IllegalActionException {
        super._init();
        delay.getPort().setName("newServiceTime");

        // Put the delay port at the bottom of the icon by default.
        StringAttribute cardinality = new StringAttribute(delay.getPort(),
                "_cardinal");
        cardinality.setExpression("SOUTH");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Next time the server becomes free.
     */
    private Time _nextTimeFree;

    /** List of delayed input tokens, whose finishing times can not be decided.
     */
    private LinkedList _delayedInputTokensList;
}
