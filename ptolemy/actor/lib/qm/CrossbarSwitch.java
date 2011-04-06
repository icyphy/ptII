package ptolemy.actor.lib.qm;

import java.util.HashMap;
import java.util.TreeSet;

import ptolemy.actor.Actor;
import ptolemy.actor.IntermediateReceiver;
import ptolemy.actor.QuantityManager;
import ptolemy.actor.Receiver;
import ptolemy.actor.util.FIFOQueue;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** This actor is an {@link QuantityManager} that, when its
 *  {@link #sendToken(Receiver, Token)} method is called, delays
 *  the delivery of the specified token to the specified receiver
 *  according to a service rule. 
 *  
 *  <p>This quantity manager implements a crossbar switch. 
 *  
 *  <p> FIXME: add explanation
 *  
 *  @author Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Yellow (derler)
 *  @Pt.AcceptedRating Red (derler)
 */
public class CrossbarSwitch extends BasicSwitch {

    /** Construct a Bus with a name and a container.
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

    /** Initialize actor variables.
     */
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

    /** Move tokens from the input queue to the switch fabric, move tokens
     *  from the switch fabric to the output queues and send tokens from the 
     *  output queues to the target receivers. When moving tokens between 
     *  queues the appropriate delays are considered. 
     *  @exception IllegalActionException Thrown if token cannot be sent to
     *  target receiver.
     */
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
                    int actorPort = _getActorPortId(receiver);
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

                    int actorPort = _getActorPortId(receiver);
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
                                    .add(_outputBufferDelay),
                                    event.contents));
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
                    if (receiver instanceof IntermediateReceiver) {
                        ((IntermediateReceiver) receiver).source = this;
                    }
                    receiver.put(token);
                    _outputTokens.get(i).remove(event);
                }
            }

            if (_debugging) {
                _debug("At time " + currentTime + ", completing send");
            }
        }
    }

    /** Schedule a refiring of the actor at the current or a future time. 
     *  The actor is refired at current time if there are tokens waiting
     *  to be processed by the switch fabric and the state of the crossbar
     *  switch indicates that the connection is free. It is refired at a 
     *  future time if any of the queues contains tokens.
     *  @exeption IllegalActionException If actor cannot be refired at 
     *  the computed time.
     */
    protected void _scheduleRefire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        _nextFireTime = Time.POSITIVE_INFINITY;
        for (int i = 0; i < _numberOfPorts; i++) {
            if (_waitingOnSwitchFabricQueue.get(i).size() > 0) {
                Object[] output = (Object[]) _waitingOnSwitchFabricQueue.get(i)
                        .get(0);
                Receiver receiver = (Receiver) output[0];

                int actorPort = _getActorPortId(receiver);
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

    
    /** Return the actor that contains this receiver. If the receiver is an
     *  IntermediateReceiver the actor is the quantity manager which manages
     *  this receiver, otherwise it is the actor containing this receiver.
     *  @param receiver The receiver.
     *  @return The actor containing the receiver.
     */
    private int _getActorPortId(Receiver receiver) {
        Actor actor;
        if (receiver instanceof IntermediateReceiver) {
            actor = (Actor) ((IntermediateReceiver) receiver).quantityManager;
        } else {
            actor = (Actor) receiver.getContainer().getContainer();
        }
        return _actorPorts.get(actor);
    }

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
    
    /** Status of the crossbar switches */
    private boolean[][] _crossbarSwitchStates;
}
