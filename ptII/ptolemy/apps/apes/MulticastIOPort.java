package ptolemy.apps.apes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// MulticastIOPort

/**
 * TODO dependent on the parameters determine if port is an input/output/io port
 * 
 * This port communicates without wired connections. The port references the
 * destinations and sources by names as specified by the <i>destinationActors</i>
 * or <i>sourceActors</i> parameter.
 * <p>
 * A port is outside multicast if at least one destination actor is given and a
 * NamedDestinationActor with the given name is contained by the container of
 * the port's container (transparent hierarchy is not supported). A port is
 * inside multicast if at least one destination actor is given and a
 * NamedDestinationActor with the given name is contained by the container of
 * this port. If no destination actor is given, then the behavior of the port
 * reverts to that of the base class. Specifically, it will only communicate if
 * it is wired. If at least one destination is given and the actor exists, then
 * all the wired connections to this port are ignored.
 * <p>
 * The width of this port on either side that is using multicast communication
 * is fixed at one. Otherwise, it depends on the number of links to the port.
 * <p>
 * When this port is used for multicast communications, nothing is connected to
 * it. Consequently, methods that access the topology such as
 * connectedPortList() and deepConnectedInPortList() return an empty list. There
 * are no deeply connected ports. 
 * 
 * TODO: How about sinkPortList()?
 * 
 * @author Patricia Derler, Stefan Resmerita
 */
public class MulticastIOPort extends TypedIOPort {
    
    /**
     * Construct a port in the specified workspace with an empty string as a
     * name. You can then change the name with setName(). If the workspace
     * argument is null, then use the default workspace. The object is added to
     * the workspace directory. Increment the version number of the workspace.
     * 
     * @param workspace
     *            The workspace that will list the port.
     * @exception IllegalActionException
     *                If creating the parameters of this port throws it.
     * @exception NameDuplicationException
     *                If creating the parameters of this port throws it.
     */
    public MulticastIOPort(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);
        _initialize();
    }

    /**
     * Construct a port with the specified container and name that is neither an
     * input nor an output. The specified container must implement the Actor
     * interface, or an exception will be thrown.
     * 
     * @param container
     *            The container actor.
     * @param name
     *            The name of the port.
     * @exception IllegalActionException
     *                If the port is not of an acceptable class for the
     *                container, or if the container does not implement the
     *                Actor interface.
     * @exception NameDuplicationException
     *                If the name coincides with a port already in the
     *                container.
     */
    public MulticastIOPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        this(container, name, false, false);
        _initialize();
    }

    /**
     * Construct a port with a container and a name that is either an input, an
     * output, or both, depending on the third and fourth arguments. The
     * specified container must implement the Actor interface or an exception
     * will be thrown.
     * 
     * @param container
     *            The container actor.
     * @param name
     *            The name of the port.
     * @param isInput
     *            True if this is to be an input port.
     * @param isOutput
     *            True if this is to be an output port.
     * @exception IllegalActionException
     *                If the port is not of an acceptable class for the
     *                container, or if the container does not implement the
     *                Actor interface.
     * @exception NameDuplicationException
     *                If the name coincides with a port already in the
     *                container.
     */
    public MulticastIOPort(ComponentEntity container, String name,
            boolean isInput, boolean isOutput) throws IllegalActionException,
            NameDuplicationException {
        super(container, name, isInput, isOutput);
        _initialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /**
     * A comma-separated list of actors that can send tokens to this port
     * through multicast ports. This is a string that defaults to the * string,
     * indicating that all actors are accepted.
     */
    public StringParameter sourceActors;

    /**
     * A comma-separated list of actors tokens are sent to by this port. 
     * This is a string that defaults to the * string,
     * indicating that all actors are accepted.
     */
    public StringParameter destinationActors;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * If the attribute is one of the properties attributes, make sure its value
     * is a record token.
     * 
     * @param attribute
     *            The attribute that changed.
     * @exception IllegalActionException
     *                If the change is not acceptable to this container.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {

        if ((attribute == destinationActors) || (attribute == sourceActors)) {
            // Since the source/destination parameters affect connectivity, we should
            // treat changes to their values as changes to the topology.
            // To do that, we listen for changes and increment the version
            // number of the workspace.
            workspace().incrVersion();
            //TODO: tokenizer for comma separated string
            _destinationActorNames.add(destinationActors.stringValue());
        } else {
            super.attributeChanged(attribute);
        }
    }

    /**
     * Send token to every actor with a multicastIOPort 
     * in the same hierarchy in the model.
     * 
     * @param token
     *            The token to send.
     */
    public void broadcast(Token token) throws IllegalActionException {

        Collection<IOPort> destinationActorPorts = _getAllDestinationActors();

        if (destinationActorPorts != null) {

            for (IOPort port : destinationActorPorts) {
                Receiver[][] receivers = port.getReceivers();
                for (int i = 0; i < receivers.length; i++) {
                    for (int j = 0; j < receivers[0].length; j++) {
                        Receiver receiver = receivers[i][j];
                        receiver.put(token);
                    }
                }
            }

        } else {
            super.broadcast(token);
        }
    }

    /**
     * Send the specified portion of a token array to all receivers connected to
     * this port. The first <i>vectorLength</i> tokens of the token array are
     * sent.
     * 
     * @param tokenArray
     *            The token array to send
     * @param vectorLength
     *            The number of elements of the token array to send.
     * @exception NoRoomException
     *                If there is no room in the receiver.
     * @exception IllegalActionException
     *                If the tokens to be sent cannot be converted to the type
     *                of this port
     */
    public void broadcast(Token[] tokenArray, int vectorLength)
            throws IllegalActionException, NoRoomException {
        // TODO change to take into account the destination list
        super.broadcast(tokenArray, vectorLength); 
    }

    /**
     * Clone the object into the specified workspace. The new object is <i>not</i>
     * added to the directory of that workspace (you must do this yourself if
     * you want it there).
     * 
     * @param workspace
     *            The workspace for the cloned object.
     * @exception CloneNotSupportedException
     *                Not thrown in this base class
     * @return The new Attribute.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        MulticastIOPort newObject = (MulticastIOPort) super.clone(workspace);
        newObject._receivers = null;
        newObject._insideReceivers = null;
        return newObject;
    }

    /**
     * Override the base class to create receivers for MulticastIOPort. 
     * 
     * @exception IllegalActionException
     *                If this port is not an opaque input port or if there is no
     *                director.
     */
    public void createReceivers() throws IllegalActionException {
        // This call will create receivers based on relations that
        // are linked to the port.
        super.createReceivers();

        _receivers = new Receiver[1][1];
        _receivers[0][0] = _newReceiver();

        if (getContainer() instanceof CompositeActor) {
            _insideReceivers = new Receiver[1][1];
            _insideReceivers[0][0] = _newInsideReceiver();
        }
    }

    /**
     * Override the base class to return the inside receiver for wireless
     * communication if wireless communication is being used. Otherwise, defer
     * to the base class.
     * 
     * @return The local inside receivers, or an empty array if there are none.
     */
    public Receiver[][] getInsideReceivers() {
        if (getContainer() instanceof CompositeActor) {
            return _insideReceivers;
        } else {
            return super.getInsideReceivers();
        }
    }

    /**
     * Override the base class to return the outside receiver for wireless
     * communication if wireless communication is being used. Otherwise, defer
     * to the base class.
     * 
     * @return The local receivers, or an empty array if there are none.
     */
    public Receiver[][] getReceivers() {
        //          if (getOutsideChannel() != null) {
        if (_receivers == null) {
            return _EMPTY_RECEIVERS;
        }

        return _receivers;
        //          } else {
        //              return super.getReceivers();
        //          }
    }

    /**
     * Send Token to specified destination actors. Note that 
     * the channelIndex is ignored as there is just one channel.
     */
    public void send(int channelIndex, Token token)
            throws IllegalActionException, NoRoomException {

        Collection<IOPort> destinationActorPorts = _getDestinationActors();

        if (destinationActorPorts != null && destinationActorPorts.size() > 0) {

            for (IOPort port : destinationActorPorts) {
                Receiver[][] receivers = port.getReceivers();
                for (int i = 0; i < receivers.length; i++) {
                    for (int j = 0; j < receivers[0].length; j++) {
                        Receiver receiver = receivers[i][j];
                        receiver.put(token);
                    }
                }
            }
        } else {
            super.send(channelIndex, token);
        }
    }

    /**
     * Send to actors in the intersection of the list given by the port
     * parameter and the list given in the argument of the method.
     */
    public void send(List<String> destinations, Token token)
            throws IllegalActionException, NoRoomException {

        if (!_destinationActorNames.isEmpty()) {
            for (String actorName : destinations) {
                if (_destinationActorNames.contains("*")
                        || _destinationActorNames.contains(actorName)) {

                    if (!_destinationActorPorts.containsKey(actorName)
                            || workspace().getVersion() != _destinationActorsVersion) {
                        CompositeActor compositeActor = (CompositeActor) getContainer()
                                .getContainer();
                        Object entity = compositeActor.getEntity(actorName);
                        if (entity instanceof Actor) {
                            Actor actor = (Actor) entity;
                            List<IOPort> inputPorts = actor.inputPortList();
                            MulticastIOPort sinkPort = null;
                            for (IOPort port : inputPorts) {
                                if (port instanceof MulticastIOPort) {
                                    sinkPort = (MulticastIOPort) port;
                                }
                            }
                            _destinationActorPorts.put(actorName, sinkPort);
                        }
                        _destinationActorsVersion = workspace().getVersion();
                    }
                    IOPort port = _destinationActorPorts.get(actorName);
                    Receiver[][] receivers = port.getReceivers();
                    for (int i = 0; i < receivers.length; i++) {
                        for (int j = 0; j < receivers[0].length; j++) {
                            Receiver receiver = receivers[i][j];
                            receiver.put(token);
                        }
                    }
                }
            }
        }
    }

    /**
     * Send token to actor given by actorName.
     */
    public void send(String actorName, Token token)
            throws IllegalActionException, NoRoomException {

        if (!_destinationActorNames.isEmpty()) {
            if (_destinationActorNames.contains("*")
                    || _destinationActorNames.contains(actorName)) {

                if (!_destinationActorPorts.containsKey(actorName)
                        || workspace().getVersion() != _destinationActorsVersion) {
                    CompositeActor compositeActor = (CompositeActor) getContainer()
                            .getContainer();
                    Object entity = compositeActor.getEntity(actorName);
                    if (entity instanceof Actor) {
                        Actor actor = (Actor) entity;
                        List<IOPort> inputPorts = actor.inputPortList();
                        MulticastIOPort sinkPort = null;
                        for (IOPort port : inputPorts) {
                            if (port instanceof MulticastIOPort) {
                                sinkPort = (MulticastIOPort) port;
                            }
                        }
                        _destinationActorPorts.put(actorName, sinkPort);
                    }
                    _destinationActorsVersion = workspace().getVersion();
                }
                IOPort port = _destinationActorPorts.get(actorName);
                if (port != null) {
                    Receiver[][] receivers = port.getReceivers();
                    for (int i = 0; i < receivers.length; i++) {
                        for (int j = 0; j < receivers[0].length; j++) {
                            Receiver receiver = receivers[i][j];
                            receiver.put(token);
                        }
                    }
                }
            }
        }
    }

    /**
     * Send token to actor.
     */
    public void send(Actor actor, Token token) throws IllegalActionException,
            NoRoomException {

        if (!_destinationActorNames.isEmpty()) {
            if (_destinationActorNames.contains("*")
                    || _destinationActorNames.contains(actor.getName())) {
                if (!_destinationActorPorts.containsKey(actor.getName())
                        || workspace().getVersion() != _destinationActorsVersion) {
                    List<IOPort> inputPorts = actor.inputPortList();
                    MulticastIOPort sinkPort = null;
                    for (IOPort port : inputPorts) {
                        if (port instanceof MulticastIOPort) {
                            sinkPort = (MulticastIOPort) port;
                        }
                    }
                    _destinationActorPorts.put(actor.getName(), sinkPort);
                }
                _destinationActorsVersion = workspace().getVersion();
            }
            IOPort port = _destinationActorPorts.get(actor.getName());
            if (port != null) {
                Receiver[][] receivers = port.getReceivers();
                for (int i = 0; i < receivers.length; i++) {
                    for (int j = 0; j < receivers[0].length; j++) {
                        Receiver receiver = receivers[i][j];
                        receiver.put(token);
                    }
                }
            }
        }
    }

    public void send(Token token) throws IllegalActionException,
            NoRoomException {
        send(0, token);
    }

    private Collection<IOPort> _getDestinationActors() {
        if (workspace().getVersion() != _destinationActorsVersion) {
            // TODO catch exceptions if getContainer() fails
            CompositeActor compositeActor = (CompositeActor) getContainer()
                    .getContainer();
            List entities = compositeActor.entityList();
            for (Iterator it = entities.iterator(); it.hasNext();) {
                Object entity = it.next();
                if (entity instanceof Actor) {
                    Actor actor = (Actor) entity;
                    if (_destinationActorNames.equals("*")
                            || _destinationActorNames.contains(actor.getName())) {
                        List<IOPort> inputPorts = actor.inputPortList();
                        MulticastIOPort sinkPort = null;
                        for (IOPort port : inputPorts) {
                            if (port instanceof MulticastIOPort) {
                                sinkPort = (MulticastIOPort) port;
                            }
                        }
                        _destinationActorPorts.put(actor.getName(), sinkPort);
                    }
                }
            }
        }
        _destinationActorsVersion = workspace().getVersion();
        return _destinationActorPorts.values();
    }

    private Collection<IOPort> _getAllDestinationActors() {
        if (workspace().getVersion() != _destinationActorsVersion) {
            // TODO catch exceptions if getContainer() fails
            CompositeActor compositeActor = (CompositeActor) getContainer()
                    .getContainer();
            List entities = compositeActor.entityList();
            for (Iterator it = entities.iterator(); it.hasNext();) {
                Object entity = it.next();
                if (entity instanceof Actor) {
                    Actor actor = (Actor) entity;
                    List<IOPort> inputPorts = actor.inputPortList();
                    MulticastIOPort sinkPort = null;
                    for (IOPort port : inputPorts) {
                        if (port instanceof MulticastIOPort) {
                            sinkPort = (MulticastIOPort) port;
                        }
                    }
                    _destinationActorPorts.put(actor.getName(), sinkPort);
                }
            }
        }
    
        _destinationActorsVersion = workspace().getVersion();
        return _destinationActorPorts.values();
    }

    private void _initialize() throws IllegalActionException,
            NameDuplicationException {
        destinationActors = new StringParameter(this, "destinationActors");
        destinationActors.setExpression("");
        _destinationActorNames = new ArrayList<String>();
        _destinationActorPorts = new HashMap<String, IOPort>();
    
        sourceActors = new StringParameter(this, "sourceActors");
        sourceActors.setExpression("*");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // To ensure that getReceivers() and variants never return null.
    private static Receiver[][] _EMPTY_RECEIVERS = new Receiver[0][0];

    // Receivers for this port for outside wireless connections.
    private Receiver[][] _receivers;

    // Receivers for this port for inside wireless connections.
    private Receiver[][] _insideReceivers;

    /**
     */
    private long _destinationActorsVersion;

    private HashMap<String, IOPort> _destinationActorPorts;
    private List<String> _destinationActorNames;

}
