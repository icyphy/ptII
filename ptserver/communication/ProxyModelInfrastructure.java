/*
 ProxyModel initializes by making needed replacement for sinks and sources.

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

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Executable;
import ptolemy.actor.Initializable;
import ptolemy.actor.Manager;
import ptolemy.actor.TypeConflictException;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.Type;
import ptolemy.data.type.Typeable;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptserver.actor.ProxySink;
import ptserver.actor.ProxySource;
import ptserver.control.Ticket;
import ptserver.data.PingToken;
import ptserver.data.PongToken;
import ptserver.util.ProxyModelBuilder;
import ptserver.util.ProxyModelBuilder.ProxyModelType;
import ptserver.util.TypeParser;

import com.ibm.mqtt.IMqttClient;
import com.ibm.mqtt.MqttClient;
import com.ibm.mqtt.MqttException;

///////////////////////////////////////////////////////////////////
//// ProxyModel
/**
 * Initialize the Ptolemy model by making needed replacement for sinks
 * and sources with appropriate proxy actors and set up infrastructure
 * for sending and receiving MQTT messages.
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
public class ProxyModelInfrastructure {

    private static final int PING_PONG_PERIOD = 1000;

    /**
     * Create a new instance of the remoteModel with the specified parameters.
     * @param modelType the type of the model which must be either client or server
     * @throws CloneNotSupportedException 
     * @throws NameDuplicationException 
     * @throws TypeConflictException 
     * @throws IllegalActionException 
     */
    public ProxyModelInfrastructure(ProxyModelType modelType,
            CompositeActor topLevelActor) throws IllegalActionException,
            TypeConflictException, NameDuplicationException,
            CloneNotSupportedException {
        _tokenPublisher = new TokenPublisher(100, 1000);
        _executor = Executors.newFixedThreadPool(3);
        _modelType = modelType;
        _topLevelActor = topLevelActor;
        loadPlainModel();
    }

    /**
     * Create a new instance of the remoteModel with the specified parameters.
     * @param modelType the type of the model which must be either client or server
     * @throws IllegalActionException 
     */
    public ProxyModelInfrastructure(ProxyModelType modelType,
            CompositeActor topLevelActor, HashMap<String, String> modelTypes)
            throws IllegalActionException {
        _tokenPublisher = new TokenPublisher(100, 1000);
        _executor = Executors.newFixedThreadPool(3);
        _modelType = modelType;
        _modelTypes.putAll(modelTypes);
        _topLevelActor = topLevelActor;
        loadPreprocessedModel();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /**
     * The listener that notifies about events happening in the RemoteModel.
     *
     */
    public interface ProxyModelListener {
        /**
         * Notify listener about the expiration of the model connection to another remote model.
         * @param remoteModel The remote model whose connection expired.
         */
        public void modelConnectionExpired(ProxyModelInfrastructure remoteModel);

        /**
         * Notify listener the exception in the given model
         * @param remoteModel The model where the exception happened.
         * @param exception The exception that triggered this event.
         */
        public void modelException(ProxyModelInfrastructure remoteModel,
                Throwable exception);
    }

    /**
     * The quality of service that would be required from the MQTT broker.
     * All messages must be send or received only once.
     */
    public static final int QOS_LEVEL = 2;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Add RemoteModelListener for notifications about the model events.
     * @param listener The listener to add.
     */
    public void addProxyModelListener(ProxyModelListener listener) {
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
        _executor.shutdown();
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
     * Return the manager controlling this model.
     * @return the Manager controlling this model
     */
    public Manager getManager() {
        return _topLevelActor.getManager();
    }

    /**
     * Return the roundtrip latency of sending ping/echo requests.
     * @return the roundtrip latency of sending ping/echo requests.
     */
    public long getPingPongLatency() {
        return _pingPonglatency;
    }

    /**
     * Return the mappings from remote source full names to their RemoteSourceData
     * data-structure.
     * @return the remoteSourceMap the mappings between full name and
     * RemoteSourceData.
     */
    public HashMap<String, ProxySourceData> getRemoteSourceMap() {
        return _proxySourceMap;
    }

    /**
     * Return the map with RemoteValueListeners of the model's remote attributes.
     * @return the map with RemoteValueListeners of the model's remote attributes.
     */
    public HashMap<String, RemoteValueListener> getSettableAttributeListenersMap() {
        return _remoteAttributeListenersMap;
    }

    /**
     * Return the mappings from remote attribute full names to their
     * remote Settable instance.
     * @return the settableAttributesMap the mappings from remote
     * attribute full names to their remote Settable instance.
     */
    public HashMap<String, Settable> getSettableAttributesMap() {
        return _remoteAttributesMap;
    }

    /**
     * Return the subscription topic of the current model.
     * @return the subscriptionTopic used to listen for tokens from
     * other remote model.
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
     * Return the model's timeout period in milliseconds. if the
     * period is less or equal to 0, the model would never timeout.
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
     * Load 
     * @param modelXML
     * @throws CloneNotSupportedException 
     * @throws NameDuplicationException 
     * @throws TypeConflictException 
     * @throws IllegalActionException 
     */
    private void loadPlainModel() throws IllegalActionException,
            TypeConflictException, NameDuplicationException,
            CloneNotSupportedException {
        ProxyModelBuilder builder = new ProxyModelBuilder(_modelType,
                _topLevelActor);
        builder.build();
        _proxySinkMap.putAll(builder.getProxySinkMap());
        _proxySourceMap.putAll(builder.getProxySourceMap());
        _remoteAttributesMap.putAll(builder.getRemoteAttributesMap());
        _modelTypes.putAll(builder.getModelTypes());
        for (ProxySourceData data : _proxySourceMap.values()) {
            data.getProxySource().setProxyModelInfrastructure(this);
        }
        for (ProxySink proxySink : _proxySinkMap.values()) {
            proxySink.setTokenPublisher(_tokenPublisher);
        }
    }

    /**
     * Load the model that already has RemoteSinks/Sources from
     * the supplied xml string and set appropriate model types from
     * the inferred model mapping.
     *
     * <p>This method is indented to be used on the Android to avoid
     * loading unneeded actors.</p>
     *
     * @param modelXML The modelXML file containing or
     * @param modelTypes The map of ports and their resolved types
     * @throws IllegalActionException if there is a problem parsing model types or 
     * setting types on Typeable objects.
     */
    private void loadPreprocessedModel() throws IllegalActionException {
        for (Object obj : getTopLevelActor().deepEntityList()) {
            ComponentEntity actor = (ComponentEntity) obj;
            if (actor instanceof ProxySink) {
                ProxySink remoteSink = (ProxySink) actor;
                remoteSink.setTokenPublisher(_tokenPublisher);
                _proxySinkMap.put(remoteSink.getTargetEntityName(), remoteSink);
            } else if (actor instanceof ProxySource) {
                ProxySource remoteSource = (ProxySource) actor;
                ProxySourceData remoteSourceData = new ProxySourceData(
                        remoteSource);
                remoteSource.setProxySourceData(remoteSourceData);
                remoteSource.setProxyModelInfrastructure(this);
                getRemoteSourceMap().put(remoteSource.getTargetEntityName(),
                        remoteSourceData);

            }
            Type type;
            for (Object portObject : actor.portList()) {
                if (portObject instanceof TypedIOPort) {
                    TypedIOPort port = (TypedIOPort) portObject;
                    if (port.deepConnectedPortList().isEmpty()) {
                        continue;
                    }
                    StringAttribute targetPortName = (StringAttribute) port
                            .getAttribute("targetPortName");

                    if (targetPortName != null) {
                        type = TypeParser.parse(_modelTypes.get(targetPortName
                                .getExpression()));
                        if (type != null) {
                            port.setTypeEquals(type);
                        }
                        port.typeConstraints().clear();
                    } else if ((type = TypeParser.parse(_modelTypes.get(port
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
                if ((type = TypeParser.parse(_modelTypes
                        .get(((Nameable) attribute).getFullName()))) != null) {
                    attribute.setTypeEquals(type);
                    attribute.typeConstraints().clear();
                }
            }
        }
        ProxyModelBuilder.findRemoteAttributes(
                ProxyModelBuilder.deepAttributeList(_topLevelActor),
                _remoteAttributesMap);
    }

    /**
     * Return true if the model is stopped, otherwise return false.
     * @return the stopped state of the model.
     * @see #setStopped(boolean)
     */
    public boolean isStopped() {
        // TODO: Figure out if there is a difference for this class
        // between stopped and paused state.
        return _stopped;
    }

    /**
     * Unsubscribe the listener from the model events.
     * @param listener The listener to remove.
     */
    public void removeProxyModelListener(ProxyModelListener listener) {
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
     * Set the stopped state of the model.
     * @param stopped indicates if the model is stopped or not.
     * @see #isStopped()
     */
    public void setStopped(boolean stopped) {
        _stopped = stopped;
    }

    /**
     * Set the model's timeout period in milliseconds. If the period is set
     * to 0 or less, the model would never timeout.
     * @param timeoutPeriod the timeout period of the model.
     * @see #getTimeoutPeriod()
     */
    public void setTimeoutPeriod(int timeoutPeriod) {
        this.timeoutPeriod = timeoutPeriod;
    }

    /**
     * Set up the communication infrastructure.
     * @param ticket The ticket associated with this remote model.
     * @param brokerHostname The hostname of the MQTT broker.
     * @return The manager of the model
     * @exception MqttException If there is a problem connecting to the broker.
     * @exception IllegalActionException If there is problem creating the manager.
     */
    public Manager setUpInfrastructure(Ticket ticket, String brokerHostname)
            throws MqttException, IllegalActionException {
        _ticket = ticket;
        switch (_modelType) {
        case CLIENT:
            _subscriptionTopic = ticket.getTicketID() + ProxyModelType.SERVER;
            _publishingTopic = ticket.getTicketID() + ProxyModelType.CLIENT;
            break;
        case SERVER:
            _subscriptionTopic = ticket.getTicketID() + ProxyModelType.CLIENT;
            _publishingTopic = ticket.getTicketID() + ProxyModelType.SERVER;
            break;
        default:
            // This should never happen.
            throw new IllegalStateException("Unhandled model type");
        }

        _tokenPublisher.startTimer(ticket);
        _setUpMQTT(brokerHostname);
        _setUpRemoteAttributes();
        _setUpMonitoring();
        _setUpManager();
        return _topLevelActor.getManager();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Close the model before finalizing
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    /**
     * Notify the model listeners about the model connection experiation.
     */
    protected final void _fireModelConnectionExpired() {
        for (ProxyModelListener listener : _modelListeners) {
            listener.modelConnectionExpired(this);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Return the last pong token.
     * @return the last pong token.
     */
    private synchronized PongToken _getLastPongToken() {
        return _lastPongToken;
    }

    /**
     * Set up MQTT connection.
     * @exception MqttException if there is a problem subscribing to topic.
     */
    private void _setUpMQTT(String address) throws MqttException {
        _mqttClient = MqttClient.createMqttClient(address, null);
        _mqttClient.connect(getTicket().getTicketID() + _modelType, true,
                (short) 10);
        _tokenPublisher.setMqttClient(_mqttClient);
        _tokenPublisher.setTopic(_publishingTopic);
        _mqttClient.registerSimpleHandler(new TokenListener(this));
        _mqttClient.subscribe(new String[] { getSubscriptionTopic() },
                new int[] { QOS_LEVEL });
    }

    /**
     * Initialize the manager of the model.
     * @exception IllegalActionException If there is a problem setting the
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
                for (ProxySourceData data : getRemoteSourceMap().values()) {
                    synchronized (data.getProxySource()) {
                        data.getProxySource().notifyAll();
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
        _pingTimer = new Timer("Ping-pong Timer " + getTicket());
        _pingTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                long msTime = System.currentTimeMillis();
                try {
                    _tokenPublisher.sendToken(new PingToken(msTime));
                } catch (IllegalActionException e) {
                    //TODO handle this
                }
                long latency = msTime - _getLastPongToken().getTimestamp();
                // update ping pong latency if the token was not received roughly within last 2 periods.
                if (latency > PING_PONG_PERIOD * 2) {
                    _pingPonglatency = latency;
                }
                if (timeoutPeriod > 0) {
                    if (latency > timeoutPeriod) {
                        _fireModelConnectionExpired();
                    }
                }
            }
        }, 0, PING_PONG_PERIOD);
    }

    /**
     * Set up remote attribute listeners.
     */
    private void _setUpRemoteAttributes() {
        for (Settable settable : _remoteAttributesMap.values()) {
            RemoteValueListener listener = new RemoteValueListener(
                    _tokenPublisher);
            settable.addValueListener(listener);
            _remoteAttributeListenersMap.put(settable.getFullName(), listener);
        }
    }

    /**
     * @return the _modelTypes
     */
    public HashMap<String, String> getModelTypes() {
        return _modelTypes;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     * The type of the remote model.
     */
    private final ProxyModelType _modelType;
    /**
     * Indicator if the model is stopped.
     */
    private volatile boolean _stopped;
    /**
     * The map from the Typeable's full name to its type
     */
    private final HashMap<String, String> _modelTypes = new HashMap<String, String>();

    /**
     * The mapping from the original source actor name to its remote
     * source actor and queue.
     */
    private final HashMap<String, ProxySourceData> _proxySourceMap = new HashMap<String, ProxySourceData>();

    /**
     * The mapping from the original sink actor name to its remote sink.
     */
    private final HashMap<String, ProxySink> _proxySinkMap = new HashMap<String, ProxySink>();

    /**
     * The mapping from the original settable object name to the
     * remote representation.
     */
    private final HashMap<String, Settable> _remoteAttributesMap = new HashMap<String, Settable>();

    /**
     * The mapping from the original settable object name to its
     * attribute listener.
     */
    private final HashMap<String, RemoteValueListener> _remoteAttributeListenersMap = new HashMap<String, RemoteValueListener>();

    /**
     * The token publisher used to batch tokens sent by the remote sink.
     */
    private final TokenPublisher _tokenPublisher;

    /**
     * The top level actor of the loaded model.
     */
    private final CompositeActor _topLevelActor;

    /**
     * The mqtt client connection.
     */
    private IMqttClient _mqttClient;

    /**
     * The topic used to listen for incoming mqtt messages.
     */
    private String _subscriptionTopic;

    /**
     * The topic used to publish outgoing mqtt messages.
     */
    private String _publishingTopic;

    /**
     * The timer used to send periodical pings.
     */
    private Timer _pingTimer;

    /**
     * The last pong token received from the remoteModel.
     */
    private volatile PongToken _lastPongToken;

    /**
     * The model listeners.
     */
    private final List<ProxyModelListener> _modelListeners = new CopyOnWriteArrayList<ProxyModelListener>();

    /**
     * The ticket identifying the model.
     */
    private Ticket _ticket;
    /**
     * The roundtrip latency for sending ping token and receiving pong token back.
     */
    private volatile long _pingPonglatency;

    /**
     * The executor used to schedule short lived tasks.
     */
    private final ExecutorService _executor;

    /**
     * Model time out period.
     */
    private int timeoutPeriod = 30000;

}
