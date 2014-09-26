/* A crossbar switch with a service rule.

@Copyright (c) 2011-2013 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.domains.de.lib.aspect;

import java.util.HashMap;
import java.util.TreeSet;

import ptolemy.actor.CommunicationAspect;
import ptolemy.actor.Receiver;
import ptolemy.actor.util.FIFOQueue;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/** This actor is an {@link CommunicationAspect} that, when its
 *  {@link #sendToken(Receiver, Receiver, Token)} method is called, delays
 *  the delivery of the specified token to the specified receiver
 *  according to a delays on input buffers, delays on output buffers
 *  and delays in the switch fabric of a crossbar switch. This actor is
 *  used the same way as the {@link BasicSwitch}; just the switch fabric
 *  implemented here is different. I.e. this switch can potentially exhibit
 *  better throughput performance as some tokens can be processed in parallel
 *  by the switch fabric. For more information on how a crossbar switch works
 *  please refer to the
 *  <a href="http://en.wikipedia.org/wiki/Crossbar_switch#in_browser">http://en.wikipedia.org/wiki/Crossbar_switch</a>
 *  General Properties.
 *
 *  @author Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Yellow (derler)
 *  @Pt.AcceptedRating Red (derler)
 */
public class CrossbarSwitch extends BasicSwitch {

    /** Construct a CrossbarSwitch  with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this actor.f
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CrossbarSwitch(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _switchFabricQueue = new HashMap<Integer, TreeSet<TimedEvent>>();
        _waitingOnSwitchFabricQueue = new HashMap<Integer, FIFOQueue>();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone this actor into the specified workspace. The new actor is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new actor with the same ports as the original, but
     *  no connections and no container.  A container must be set before
     *  much can be done with this actor.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return A new Bus.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        CrossbarSwitch newObject = (CrossbarSwitch) super.clone(workspace);
        // This is confusing.  The parent class has a private variable named _switchFabricQueue?
        newObject._switchFabricQueue = new HashMap<Integer, TreeSet<TimedEvent>>();
        newObject._waitingOnSwitchFabricQueue = new HashMap<Integer, FIFOQueue>();
        newObject._crossbarSwitchStates = new boolean[_numberOfPorts][_numberOfPorts];
        return newObject;
    }

    /** Move tokens from the input queue to the switch fabric, move tokens
     *  from the switch fabric to the output queues and send tokens from the
     *  output queues to the target receivers. When moving tokens between
     *  queues the appropriate delays are considered.
     *  @exception IllegalActionException Thrown if token cannot be sent to
     *  target receiver.
     */
    @Override
    public void fire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        // In a continuous domain this actor could be fired before any token has
        // been received; _nextTimeFree could be null.
        if (_nextFireTime != null && currentTime.compareTo(_nextFireTime) == 0) {

            // move tokens from input queue to switch fabric

            TimedEvent event;
            for (int i = 0; i < _numberOfPorts; i++) {
                event = _getEventForCurrentTime(_inputTokens.get(i));
                if (event != null) {
                    Object[] output = (Object[]) event.contents;
                    Receiver receiver = (Receiver) output[0];
                    int actorPort = _ioPortToSwitchInPort.get(receiver
                            .getContainer());
                    boolean free = true;
                    for (int j = 0; j < _numberOfPorts; j++) {
                        free = free & _crossbarSwitchStates[j][actorPort]
                                & _crossbarSwitchStates[i][j];
                    }
                    if (free) {
                        _switchFabricQueue.get(actorPort).add(
                                new TimedEvent(currentTime
                                        .add(_switchFabricDelay),
                                        event.contents));
                        _crossbarSwitchStates[i][actorPort] = false;
                    } else {
                        _waitingOnSwitchFabricQueue.get(i).put(event.contents);
                    }
                    _inputTokens.get(i).remove(event);
                }
            }

            // move waiting tokens into switch fabric

            for (int i = 0; i < _numberOfPorts; i++) {
                if (_waitingOnSwitchFabricQueue.get(i).size() > 0) {
                    Object[] output = (Object[]) _waitingOnSwitchFabricQueue
                            .get(i).get(0);
                    Receiver receiver = (Receiver) output[0];

                    int actorPort = _ioPortToSwitchInPort.get(receiver
                            .getContainer());
                    Time lastTimeStamp = currentTime;
                    boolean free = true;
                    for (int j = 0; j < _numberOfPorts; j++) {
                        free = free & _crossbarSwitchStates[j][actorPort]
                                & _crossbarSwitchStates[i][j];
                    }
                    if (free) {
                        _switchFabricQueue.get(actorPort).add(
                                new TimedEvent(lastTimeStamp
                                        .add(_switchFabricDelay), output));
                        _crossbarSwitchStates[i][actorPort] = false;
                        _waitingOnSwitchFabricQueue.get(i).take();
                    }
                }
            }

            // move tokens from switch fabric to output queues

            for (int i = 0; i < _numberOfPorts; i++) {
                event = _getEventForCurrentTime(_switchFabricQueue.get(i));
                if (event != null) {
                    Time lastTimeStamp = currentTime;
                    if (_switchFabricQueue.get(i).size() > 0) {
                        lastTimeStamp = _switchFabricQueue.get(i).last().timeStamp;
                    }
                    _outputTokens.get(i).add(
                            new TimedEvent(lastTimeStamp
                                    .add(_outputBufferDelay), event.contents));
                    _switchFabricQueue.get(i).remove(event);
                    // Reset crossbar state.
                    for (int j = 0; j < _numberOfPorts; j++) {
                        _crossbarSwitchStates[j][i] = true;
                    }
                }
            }

            // send tokens to target receiver

            for (int i = 0; i < _numberOfPorts; i++) {
                event = _getEventForCurrentTime(_outputTokens.get(i));
                if (event != null) {
                    Object[] output = (Object[]) event.contents;
                    Receiver receiver = (Receiver) output[0];
                    Token token = (Token) output[1];
                    _sendToReceiver(receiver, token);
                    _outputTokens.get(i).remove(event);
                }
            }

            if (_debugging) {
                _debug("At time " + currentTime + ", completing send");
            }
        }
    }

    /** Initialize actor variables.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        for (int i = 0; i < _numberOfPorts; i++) {
            _switchFabricQueue.put(i, new TreeSet<TimedEvent>());
            _waitingOnSwitchFabricQueue.put(i, new FIFOQueue());
        }
        _crossbarSwitchStates = new boolean[_numberOfPorts][_numberOfPorts];
        for (int i = 0; i < _numberOfPorts; i++) {
            for (int j = 0; j < _numberOfPorts; j++) {
                _crossbarSwitchStates[i][j] = true;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Schedule a refiring of the actor at the current or a future time.
     *  The actor is refired at current time if there are tokens waiting
     *  to be processed by the switch fabric and the state of the crossbar
     *  switch indicates that the connection is free. It is refired at a
     *  future time if any of the queues contains tokens.
     *  @exception IllegalActionException If actor cannot be refired at
     *  the computed time.
     */
    @Override
    protected void _scheduleRefire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        _nextFireTime = Time.POSITIVE_INFINITY;
        for (int i = 0; i < _numberOfPorts; i++) {
            if (_waitingOnSwitchFabricQueue.get(i).size() > 0) {
                Object[] output = (Object[]) _waitingOnSwitchFabricQueue.get(i)
                        .get(0);
                Receiver receiver = (Receiver) output[0];

                int actorPort = _ioPortToSwitchOutPort.get(receiver
                        .getContainer());
                boolean free = true;
                for (int j = 0; j < _numberOfPorts; j++) {
                    free = free & _crossbarSwitchStates[j][actorPort]
                            & _crossbarSwitchStates[i][j];
                }
                if (free) {
                    _nextFireTime = currentTime;
                }
            }
        }
        for (int i = 0; i < _numberOfPorts; i++) {
            _nextFireTime = _getNextFireTime(_nextFireTime, _inputTokens.get(i));
            _nextFireTime = _getNextFireTime(_nextFireTime,
                    _outputTokens.get(i));
            _nextFireTime = _getNextFireTime(_nextFireTime,
                    _switchFabricQueue.get(i));
        }
        _fireAt(_nextFireTime);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Switch fabric queues for every output port. A token is in the
     *  switch fabric queue if it can be processed and for the amount of time
     *  defined by the delay.
     */
    protected HashMap<Integer, TreeSet<TimedEvent>> _switchFabricQueue;

    /** Queue that stores tokens that have been put into the input queue but
     *  cannot be processed by the switch fabric because the crossbar switch
     *  for this connection is busy.
     */
    protected HashMap<Integer, FIFOQueue> _waitingOnSwitchFabricQueue;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the event in a set of TimedEvents which should be processed at
     *  the current time, i.e. where the time stamp of the TimedEvent equals
     *  current time.
     *  @param tokens The set of TimedEvents.
     *  @return The event that should be processed at the current time.
     */
    private TimedEvent _getEventForCurrentTime(TreeSet<TimedEvent> tokens) {
        Time currentTime = getDirector().getModelTime();
        if (tokens.size() > 0) {
            TimedEvent event = tokens.first();
            if (event.timeStamp.compareTo(currentTime) <= 0) {
                return event;
            }
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        privateVariables                   ////

    /** Status of the crossbar switches */
    private boolean[][] _crossbarSwitchStates;
}
