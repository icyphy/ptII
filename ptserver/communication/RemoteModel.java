/*
 RemoteModel initializes by making needed replacement for sinks and sources.

 Copyright (c) 2011 The Regents of the University of California.
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
package ptserver.communication;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Executable;
import ptolemy.actor.IOPort;
import ptolemy.actor.Initializable;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.Typeable;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptserver.actor.RemoteSink;
import ptserver.actor.RemoteSource;
import ptserver.control.Ticket;
import ptserver.data.PingToken;
import ptserver.data.PongToken;

import com.ibm.mqtt.IMqttClient;
import com.ibm.mqtt.MqttException;

///////////////////////////////////////////////////////////////////
//// RemoteModel
/**
 * Initialize the Ptolemy model by making needed replacement for sinks and sources with appropriate proxy actors and
 * set up infrastructure for sending and receiving MQTT messages.
 *
 * The model can set up the infrastructure for client or server
 * which differ slightly in actor replacement mechanisms.
 *
 * @author Anar Huseynov
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class RemoteModel {

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /**
     * The listener that notifies about events happening in the RemoteModel.
     *
     */
    public interface RemoteModelListener {
        /**
         * Notify listener about the expiration of the model connection to another remote model.
         * @param remoteModel The remote model whose connection expired.
         */
        public void modelConnectionExpired(RemoteModel remoteModel);

        /**
         * Notify listener the exception in the given model
         * @param remoteModel The model where the exception happened.
         * @param exception The exception that triggered this event.
         */
        public void modelException(RemoteModel remoteModel, Throwable exception);
    }

    /**
     * An enumerations that specifies the remote model type: client or server.
     *
     */
    public enum RemoteModelType {
        /**
         * Client remote model type.
         */
        CLIENT,
        /**
         * Server remote model type.
         */
        SERVER;
    }

    /**
     * The quality of service that would be required from the MQTT broker.  
     * All messages must be send or received only once.
     */
    public static final int QOS_LEVEL = 2;

    /**
     * Create a new instance of the remoteModel with the specified parameters.
     * @param subscriptionTopic the topic name that this model would subscribe to receive tokens from other remote model
     * @param publishingTopic the topic name that this model would publish its tokens to be received by other remote model
     * @param modelType the type of the model which must be either client or server
     */
    public RemoteModel(String subscriptionTopic, String publishingTopic,
            RemoteModelType modelType) {
        _tokenPublisher = new TokenPublisher(100, 1000);
        _subscriptionTopic = subscriptionTopic;
        _publishingTopic = publishingTopic;
        _modelType = modelType;
        _executor = Executors.newCachedThreadPool();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Add RemoteModelListener for notifications about the model events.
     * @param listener The listener to add.
     */
    public void addRemoteModelListener(RemoteModelListener listener) {
        if (_modelListeners.contains(listener)) {
            return;
        } else {
            _modelListeners.add(listener);
        }
    }

    /**
     * Close the model along with all its connection.
     */
    public void close() {
        _pingTimer.cancel();
        _tokenPublisher.cancelTimer();
        try {
            _mqttClient.unsubscribe(new String[] { getSubscriptionTopic() });
            _mqttClient.registerSimpleHandler(null);
            _mqttClient.disconnect();
        } catch (MqttException e) {
            // TODO handle exception
        }
    }

    /**
     * Return the executor to schedule short lived tasks.
     * 
     * <p>It's used to send PongTokens outside of the MQTT listener thread
     * since MQTTClient disallows that. </p> 
     * @return the executor to schedule short lived tasks. 
     */
    public Executor getExecutor() {
        return _executor;
    }

    /**
     * Return the last PongToken.
     * @return the last PongToken.
     * @set {@link #setLastPongToken(PongToken)}
     */
    public synchronized PongToken getLastPongToken() {
        return _lastPongToken;
    }

    /**
     * Return the manager controlling this model.
     * @return the Manager controlling this model
     */
    public Manager getManager() {
        return _topLevelActor.getManager();
    }

    /**
     * Return the roundtrip latency of sending ping/echo requests.
     * @return
     */
    public long getPingPongLatency() {
        return _pingPonglatency;
    }

    /**
     * Return the mappings from remote source full names to their RemoteSourceData
     * data-structure.
     * @return the remoteSourceMap the mappings between full name and RemoteSourceData.
     */
    public HashMap<String, RemoteSourceData> getRemoteSourceMap() {
        return _remoteSourceMap;
    }

    /**
     * Get the map from the Typeable's full name to its type.
     * @return The map from the Typeable's full name to its type.
     * @see #setResolvedTypes(HashMap)
     */
    public HashMap<String, String> getResolvedTypes() {
        return _resolvedTypes;
    }

    /**
     * Return the map with RemoteValueListeners of the model's remote attributes. 
     * @return the map with RemoteValueListeners of the model's remote attributes.
     */
    public HashMap<String, RemoteValueListener> getSettableAttributeListenersMap() {
        return _settableAttributeListenersMap;
    }

    /**
     * Return the mappings from remote attribute full names to their remote Settable instance.
     * @return the settableAttributesMap the mappings from remote attribute full names to their remote Settable instance.
     */
    public HashMap<String, Settable> getSettableAttributesMap() {
        return _settableAttributesMap;
    }

    /**
     * Return the subscription topic of the current model.
     * @return the subscriptionTopic used to listen for tokens from other remote model.
     */
    public String getSubscriptionTopic() {
        return _subscriptionTopic;
    }

    /**
     * Return the ticket that uniquely identifies the model.
     * @return the ticket identifying the model.
     */
    public Ticket getTicket() {
        return _ticket;
    }

    /**
     * Return the model's timeout period. if the period is less or equal to 0, 
     * the model would never timeout. 
     * @return the timeoutPeriod of the model.
     * @see #setTimeoutPeriod(int)
     */
    public int getTimeoutPeriod() {
        return timeoutPeriod;
    }

    /**
     * Return the tokenPublisher used to send and batch tokens.
     * @return The token publisher used for sending tokens.
     */
    public TokenPublisher getTokenPublisher() {
        return _tokenPublisher;
    }

    /**
     * Return the top level actor after the model was loaded.
     * @return the topLevelActor of the model
     */
    public CompositeActor getTopLevelActor() {
        return _topLevelActor;
    }

    /**
     * Initialize the model that already has RemoteSinks/Sources from the 
     * supplied xml string and set appropriate model types from the inferred model mapping.
     * 
     * <p>This method is indented to be used on the Android to avoid loading unneeded actors.</p>
     * @param modelXML The modelXML file containing or
     * @param modelTypes The map of ports and their resolved types
     * @exception Exception If there is a problem parsing the modelXML.
     */
    public void initModel(String modelXML, HashMap<String, String> modelTypes)
            throws Exception {
        MoMLParser parser = _createMoMLParser();
        _topLevelActor = (CompositeActor) parser.parse(modelXML);
        for (Object obj : getTopLevelActor().deepEntityList()) {
            ComponentEntity actor = (ComponentEntity) obj;
            if (actor instanceof RemoteSink) {
                RemoteSink remoteSink = (RemoteSink) actor;
                remoteSink.setTokenPublisher(_tokenPublisher);
                _remoteSinkMap
                        .put(remoteSink.getTargetEntityName(), remoteSink);
            } else if (actor instanceof RemoteSource) {
                RemoteSource remoteSource = (RemoteSource) actor;
                RemoteSourceData remoteSourceData = new RemoteSourceData(
                        remoteSource, this);
                remoteSource.setRemoteSourceData(remoteSourceData);
                getRemoteSourceMap().put(remoteSource.getTargetEntityName(),
                        remoteSourceData);

            }
            Type type;
            for (Object portObject : actor.portList()) {
                if (portObject instanceof TypedIOPort) {
                    TypedIOPort port = (TypedIOPort) portObject;
                    StringAttribute targetPortName = (StringAttribute) port
                            .getAttribute("targetPortName");

                    if (targetPortName != null) {
                        type = BaseType.forName(modelTypes.get(targetPortName
                                .getExpression()));
                        if (type != null) {
                            port.setTypeEquals(type);
                        }
                        port.typeConstraints().clear();
                    } else if ((type = BaseType.forName(modelTypes.get(port
                            .getFullName()))) != null) {
                        port.setTypeEquals(type);
                        port.typeConstraints().clear();
                    } else {
                        //Not sure if this is possible, but just in case.
                        throw new IllegalActionException(port,
                                "Type constraint for the port was not found");
                    }
                }
            }
            for (Typeable attribute : actor.attributeList(Typeable.class)) {
                //Cast to Nameable is safe because it's an attribute.
                if ((type = BaseType.forName(modelTypes
                        .get(((Nameable) attribute).getFullName()))) != null) {
                    attribute.setTypeEquals(type);
                    attribute.typeConstraints().clear();
                }
            }
        }
        _initRemoteAttributes(_topLevelActor);
    }

    /**
     * Return true if the model is stopped, otherwise return false.
     * TODO: Figure out if there is a difference for this class between stopped and paused state. 
     * @return the stopped state of the model.
     * @see #setStopped(boolean)
     */
    public boolean isStopped() {
        return _stopped;
    }

    /**
     * Load the model from the specified URL.
     *
     * @param modelURL the model URL to be loaded
     * @exception Exception if there is a problem parsing the model, connecting to the mqtt broker or replacing actors.
     */
    public void loadModel(URL modelURL) throws Exception {
        MoMLParser parser = _createMoMLParser();
        HashSet<ComponentEntity> unneededActors = new HashSet<ComponentEntity>();
        HashSet<ComponentEntity> sinks = new HashSet<ComponentEntity>();
        HashSet<ComponentEntity> sources = new HashSet<ComponentEntity>();
        setResolvedTypes(new HashMap<String, String>());
        _topLevelActor = (CompositeActor) parser.parse(null, modelURL);
        for (Object obj : getTopLevelActor().deepEntityList()) {
            ComponentEntity actor = (ComponentEntity) obj;
            Attribute remoteAttribute = actor.getAttribute("_remote");
            boolean isSinkOrSource = false;
            if (remoteAttribute instanceof Parameter) {
                Parameter parameter = (Parameter) remoteAttribute;
                if (parameter.getExpression().equals("source")) {
                    sources.add(actor);
                    isSinkOrSource = true;
                } else if (parameter.getExpression().equals("sink")) {
                    sinks.add(actor);
                    isSinkOrSource = true;
                }
            }
            if (!isSinkOrSource && _modelType == RemoteModelType.CLIENT) {
                unneededActors.add(actor);
            }
            _initRemoteAttributes(actor);
        }
        _initRemoteAttributes(_topLevelActor);
        if (_topLevelActor instanceof TypedCompositeActor) {
            TypedCompositeActor typedActor = (TypedCompositeActor) _topLevelActor;
            TypedCompositeActor.resolveTypes(typedActor);
        }
        _captureModelTypes(sources, getResolvedTypes());
        _captureModelTypes(sinks, getResolvedTypes());

        switch (_modelType) {
        case SERVER:
            for (ComponentEntity entity : sources) {
                _createSource(entity, true, getResolvedTypes());
            }
            for (ComponentEntity entity : sinks) {
                _createSink(entity, true, getResolvedTypes());
            }
            break;
        case CLIENT:
            for (ComponentEntity entity : sources) {
                _createSink(entity, false, getResolvedTypes());
            }
            for (ComponentEntity entity : sinks) {
                _createSource(entity, false, getResolvedTypes());
            }
            HashMap<NamedObj, StringAttribute> containerToDummyAttributeMap = new HashMap<NamedObj, StringAttribute>();
            for (Settable settable : _settableAttributesMap.values()) {
                Attribute attribute = (Attribute) settable;
                NamedObj container = attribute.getContainer();
                Attribute lastAttribute = attribute;
                while (container != _topLevelActor) {
                    StringAttribute dummyAttribute = containerToDummyAttributeMap
                            .get(container);
                    if (dummyAttribute == null) {
                        dummyAttribute = new StringAttribute(
                                container.getContainer(), container
                                        .getContainer().uniqueName("dummy"));
                        dummyAttribute.setPersistent(true);
                        containerToDummyAttributeMap.put(container,
                                dummyAttribute);
                    }
                    container = container.getContainer();
                    lastAttribute.setContainer(dummyAttribute);
                    lastAttribute = dummyAttribute;
                }
            }
            for (ComponentEntity componentEntity : unneededActors) {
                componentEntity.setContainer(null);
            }
            for (Entry<NamedObj, StringAttribute> entry : containerToDummyAttributeMap
                    .entrySet()) {
                entry.getValue().setName(entry.getKey().getName());
            }
            break;
        }
    }

    /**
     * Unsubscribe the listener from the model events.
     * @param listener The listener to remove.
     */
    public void removeRemoteModelListener(RemoteModelListener listener) {
        _modelListeners.remove(listener);
    }

    /**
     * Set the PongToken instance that was received the last. 
     * @param lastPongToken The last PongToken.
     * @see #getLastPongToken()
     */
    public synchronized void setLastPongToken(PongToken lastPongToken) {
        _lastPongToken = lastPongToken;
        _pingPonglatency = System.currentTimeMillis()
                - lastPongToken.getTimestamp();
    }

    /**
     * Set the MQTT client instance that is connected to the broker.
     * @param mqttClient the mqttClient that the model would use to send and receive MQTT messages
     * @exception MqttException if there is a problem connecting to the broker
     */
    public void setMqttClient(IMqttClient mqttClient) throws MqttException {
        _mqttClient = mqttClient;
        _tokenPublisher.setMqttClient(_mqttClient);
        _tokenPublisher.setTopic(_publishingTopic);
    }

    /**
     * Set the map from the Typeable's full name to its type.
     * @param portTypes the _portTypes to set
     * @see #getResolvedTypes()
     */
    public void setResolvedTypes(HashMap<String, String> portTypes) {
        _resolvedTypes = portTypes;
    }

    /**
     * Set the stopped state of the model.
     * @param stopped indicates if the model is stopped or not.
     * @see #isStopped()
     */
    public void setStopped(boolean stopped) {
        _stopped = stopped;
    }

    /**
     * Set the ticket uniquely identifying the model
     * @param ticket the ticket to set
     * @see #getTicket()
     */
    public void setTicket(Ticket ticket) {
        _ticket = ticket;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Set the model's timeout period.  If the period is set to 0 or less,
     * the model would never timeout.
     * @param the timeout period of the model.
     * @see #getTimeoutPeriod()
     */
    public void setTimeoutPeriod(int timeoutPeriod) {
        this.timeoutPeriod = timeoutPeriod;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Set up the communication infrastructure.
     * @return The manager of the model
     * @exception MqttException if there is a problem connecting to the broker.
     * @exception IllegalActionException If there is problem creating the manager.
     */
    public Manager setUpInfrastructure() throws MqttException,
            IllegalActionException {
        _setUpMQTT();
        _setUpRemoteAttributes();
        _setUpMonitoring();
        _setUpManager();
        return _topLevelActor.getManager();
    }

    /**
     * Notify the model listeners about the model connection experiation.
     */
    protected final void _fireModelConnectionExpired() {
        for (RemoteModelListener listener : _modelListeners) {
            listener.modelConnectionExpired(this);
        }
    }

    /**
     * Capture inferred types of the entities.
     * @param entities The entities whose inferred types are captured
     * @param portTypes The mapping that stores the types
     * @exception IllegalActionException If there is a problem inferring type of Typeable.
     */
    private void _captureModelTypes(HashSet<ComponentEntity> entities,
            HashMap<String, String> portTypes) throws IllegalActionException {
        for (ComponentEntity entity : entities) {
            for (Object portObject : entity.portList()) {
                Port port = (Port) portObject;
                if (port instanceof IOPort) {
                    // if it's TypedIOPort, capture its types.
                    if (port instanceof TypedIOPort) {
                        //FIXME using toString on Type is not elegant and could break.
                        portTypes.put(port.getFullName(), ((TypedIOPort) port)
                                .getType().toString());
                    }
                    // this port might be connected to other TypedIOPorts whose types are needed on the client.
                    IOPort ioPort = (IOPort) port;
                    for (Object relationObject : ioPort.linkedRelationList()) {
                        Relation relation = (Relation) relationObject;
                        List<Port> portList = relation.linkedPortList(port);
                        for (Port connectingPort : portList) {
                            // TODO: only the first port connection is used on the client, consider skipping the rest here.
                            if (connectingPort instanceof TypedIOPort) {
                                // FIXME using toString on Type is not elegant and could break.
                                portTypes.put(connectingPort.getFullName(),
                                        ((TypedIOPort) connectingPort)
                                                .getType().toString());
                            }
                        }
                    }
                }

            }
            for (Typeable attribute : entity.attributeList(Typeable.class)) {
                //FIXME using toString on Type is not elegant and could break
                portTypes.put(((Nameable) attribute).getFullName(), attribute
                        .getType().toString());
            }
        }
    }

    /**
     * Create and initialize a new MoMLParser.
     * @return new MoMLParser with BackwardCompatibility filters.
     */
    private MoMLParser _createMoMLParser() {
        MoMLParser parser = new MoMLParser(new Workspace());
        parser.resetAll();
        // TODO: is this thread safe?
        MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());
        return parser;
    }

    /**
     * Create a new instance of the RemoteSink either by replacing the targetEntity
     * or by replacing all entities connected to it.
     * @param targetEntity The target entity to be processed
     * @param replaceTargetEntity replaceTargetEntity true to replace the target entity with the proxy,
     * otherwise replace all entities connecting to it with one proxy
     * @param portTypes The map of ports and their resolved types
     * @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     * @exception NameDuplicationException If the container already has an
     *   actor with this name.
     * @exception CloneNotSupportedException If port cloning is not supported
     * @see ptserver.actor.RemoteSink
     */
    private void _createSink(ComponentEntity targetEntity,
            boolean replaceTargetEntity, HashMap<String, String> portTypes)
            throws IllegalActionException, NameDuplicationException,
            CloneNotSupportedException {
        RemoteSink remoteSink = new RemoteSink(
                (CompositeEntity) targetEntity.getContainer(), targetEntity,
                replaceTargetEntity, portTypes);
        remoteSink.setTokenPublisher(_tokenPublisher);
        _remoteSinkMap.put(remoteSink.getTargetEntityName(), remoteSink);
    }

    /**
     * Create a new instance of the RemoteSource either by replacing the targetEntity or by replacing all entities connected to it.
     * @param targetEntity The target entity to be processed
     * @param replaceTargetEntity replaceTargetEntity true to replace the target entity with the proxy,
     * otherwise replace all entities connecting to it with one proxy
     * @param portTypes The map of ports and their resolved types
     * @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     * @exception NameDuplicationException If the container already has an
     *   actor with this name.
     * @exception CloneNotSupportedException If port cloning is not supported
     * @see ptserver.actor.RemoteSource
     */
    private void _createSource(ComponentEntity targetEntity,
            boolean replaceTargetEntity, HashMap<String, String> portTypes)
            throws IllegalActionException, NameDuplicationException,
            CloneNotSupportedException {
        RemoteSource remoteSource = new RemoteSource(
                (CompositeEntity) targetEntity.getContainer(), targetEntity,
                replaceTargetEntity, portTypes);
        RemoteSourceData data = new RemoteSourceData(remoteSource, this);
        getRemoteSourceMap().put(remoteSource.getTargetEntityName(), data);
    }

    /**
     * Return the last pong token.
     * @return the last pong token.
     */
    private synchronized PongToken _getLastPongToken() {
        return _lastPongToken;
    }

    /**
     * Find all remote attributes of the model and add them to the _settableAttributesMap.
     * @param container
     */
    private void _initRemoteAttributes(NamedObj container) {
        for (Object attributeObject : container.attributeList()) {
            Attribute attribute = (Attribute) attributeObject;
            if (_isRemoteAttribute(attribute)) {
                _settableAttributesMap.put(attribute.getFullName(),
                        (Settable) attribute);
            } else {
                _initRemoteAttributes(attribute);
            }
        }
    }

    /**
     * Return true if the attribute is marked as remote attribute, false otherwise.
     * @param attribute the attribute to check.
     * @return true if the attribute is marked as remote attribute, false otherwise.
     */
    private boolean _isRemoteAttribute(Attribute attribute) {
        if (attribute instanceof Settable) {
            Attribute isRemoteAttribute = attribute.getAttribute("_remote");
            if (isRemoteAttribute instanceof Parameter) {
                if (((Parameter) isRemoteAttribute).getExpression().equals(
                        "attribute")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Initialize the manager of the model.
     * @throws IllegalActionException if there is a problem setting the 
     * manager of the top level actor.
     */
    private void _setUpManager() throws IllegalActionException {
        Manager manager = new Manager(_topLevelActor.workspace(), null);
        _topLevelActor.setManager(manager);
        _topLevelActor.addPiggyback(new Executable() {

            public void addInitializable(Initializable initializable) {
            }

            public void fire() throws IllegalActionException {
            }

            public void initialize() throws IllegalActionException {
                setStopped(false);
            }

            public boolean isFireFunctional() {
                return false;
            }

            public boolean isStrict() throws IllegalActionException {
                return false;
            }

            public int iterate(int count) throws IllegalActionException {
                //FIXME: Not sure if this is correct
                throw new IllegalActionException("Iterating is not supported");
            }

            public boolean postfire() throws IllegalActionException {
                return true;
            }

            public boolean prefire() throws IllegalActionException {
                return true;
            }

            public void preinitialize() throws IllegalActionException {
            }

            public void removeInitializable(Initializable initializable) {
            }

            public void stop() {
                setStopped(true);
                for (RemoteSourceData data : getRemoteSourceMap().values()) {
                    synchronized (data.getRemoteSource()) {
                        data.getRemoteSource().notifyAll();
                    }
                }
            }

            public void stopFire() {
            }

            public void terminate() {
            }

            public void wrapup() throws IllegalActionException {
            }
        });
    }

    /**
     * Set up model monitoring infrastructure.
     */
    private void _setUpMonitoring() {
        setLastPongToken(new PongToken(System.currentTimeMillis()));
        _pingTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                long msTime = System.currentTimeMillis();
                try {
                    _tokenPublisher.sendToken(new PingToken(msTime));
                } catch (IllegalActionException e) {
                    //TODO handle this
                }
                if (timeoutPeriod > 0) {
                    long lastPong = msTime - _getLastPongToken().getTimestamp();
                    long timeOut = 1000 * timeoutPeriod;
                    if (lastPong > timeOut) {
                        _fireModelConnectionExpired();
                    }
                }
            }
        }, 0, 1000);
    }

    /**
     * Set up MQTT connection.
     * @throws MqttException if there is a problem subscribing to topic.
     */
    private void _setUpMQTT() throws MqttException {
        _mqttClient.registerSimpleHandler(new TokenListener(this));
        _mqttClient.subscribe(new String[] { getSubscriptionTopic() },
                new int[] { QOS_LEVEL });
    }

    /**
     * Set up remote attribute listeners.
     */
    private void _setUpRemoteAttributes() {
        for (Settable settable : _settableAttributesMap.values()) {
            RemoteValueListener listener = new RemoteValueListener(
                    _tokenPublisher);
            settable.addValueListener(listener);
            _settableAttributeListenersMap
                    .put(settable.getFullName(), listener);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     * The type of the remote model.
     */
    private final RemoteModelType _modelType;

    /**
     * Indicator if the model is stopped.
     */
    private volatile boolean _stopped;

    /**
     * The map from the Typeable's full name to its type
     */
    private HashMap<String, String> _resolvedTypes;
    /**
     * The mapping from the original source actor name to its remote source actor and queue.
     */
    private final HashMap<String, RemoteSourceData> _remoteSourceMap = new HashMap<String, RemoteSourceData>();

    /**
     * The mapping from the original sink actor name to its remote sink.
     */
    private final HashMap<String, RemoteSink> _remoteSinkMap = new HashMap<String, RemoteSink>();

    /**
     * The mapping from the original settable object name to the remote representation.
     */
    private final HashMap<String, Settable> _settableAttributesMap = new HashMap<String, Settable>();

    /**
     * The mapping from the original settable object name to its attribute listener.
     */
    private final HashMap<String, RemoteValueListener> _settableAttributeListenersMap = new HashMap<String, RemoteValueListener>();

    /**
     * The token publisher used to batch tokens sent by the remote sink.
     */
    private final TokenPublisher _tokenPublisher;

    /**
     * The top level actor of the loaded model.
     */
    private CompositeActor _topLevelActor;

    /**
     * The mqtt client connection.
     */
    private IMqttClient _mqttClient;

    /**
     * The topic used to listen for incoming mqtt messages.
     */
    private final String _subscriptionTopic;

    /**
     * The topic used to publish outgoing mqtt messages.
     */
    private final String _publishingTopic;

    /**
     * The timer used to send periodical pings.
     */
    private final Timer _pingTimer = new Timer(true);

    /**
     * The last pong token received from the remoteModel.
     */
    private PongToken _lastPongToken;

    /**
     * The model listeners.
     */
    private final List<RemoteModelListener> _modelListeners = new CopyOnWriteArrayList<RemoteModelListener>();

    /**
     * The ticket identifying the model.
     */
    private Ticket _ticket;
    /**
     * The roundtrip latency for sending ping token and receiving pong token back.
     */
    private long _pingPonglatency;

    /**
     * The executor used to schedule short lived tasks.
     */
    private final Executor _executor;

    /**
     * Model time out period.
     */
    private int timeoutPeriod = 30;

}
