/*
 ProxyModelInfrastructure sets up infrastructure for executing models
 in a distributed environment between client and server.

 Copyright (c) 2011-2014 The Regents of the University of California.
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
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import ptserver.data.RemoteEventToken;
import ptserver.util.ProxyModelBuilder;
import ptserver.util.ProxyModelBuilder.ProxyModelType;
import ptserver.util.ServerUtility;
import ptserver.util.TypeParser;

import com.ibm.mqtt.IMqttClient;
import com.ibm.mqtt.MqttClient;
import com.ibm.mqtt.MqttException;

///////////////////////////////////////////////////////////////////
//// ProxyModel

/** ProxyModelInfrastructure set ups infrastructure for executing models
 *  in a distributed mode between client and server.
 *
 *  @author Anar Huseynov
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (ahuseyno)
 *  @Pt.AcceptedRating Red (ahuseyno)
 */
public class ProxyModelInfrastructure {

    /** The listener that notifies about events happening in the RemoteModel.
     */
    public interface ProxyModelListener {

        /** Notify listener about the expiration of the model connection to another remote model.
         *  @param proxyModelInfrastructure The infrastructure whose connection expired.
         */
        void modelConnectionExpired(
                ProxyModelInfrastructure proxyModelInfrastructure);

        /** Notify all model listeners that the simulation has experienced an exception.
         *  @param proxyModelInfrastructure The infrastructure where the exception happened.
         *  @param message The message explaining what has happened.
         *  @param exception The exception that triggered this event.
         */
        void modelException(ProxyModelInfrastructure proxyModelInfrastructure,
                String message, Throwable exception);

        /**
         * Notify the listener about server event received from the remote ProxyModelInfrastructure.
         * @param proxyModelInfrastructure The proxyModelInfrastructure that received the event
         * @param event The remote event
         */
        void onRemoteEvent(ProxyModelInfrastructure proxyModelInfrastructure,
                RemoteEventToken event);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The quality of service that would be required from the MQTT broker.
     *  All messages must be send or received only once.
     */
    public static final int QOS_LEVEL = 2;

    /** Create a new instance of the proxy model infrastructure for the specified model type
     *  from the plain model. The model must contain markings indicating which sinks,
     *  sources or attributes are remote. The infrastructure would use ProxyModelBuilder to
     *  convert the plain model to the one that supports distributed execution.
     *  @param modelType the type of the model which must be either client or server
     *  @param plainTopLevelActor The topLevelActor that has attributes indicating remote objects
     *  but that was not yet converted to the model supporting distributed execution.
     *  @exception IllegalActionException if there is a problem setting a manager or
     *  capturing certain type information.
     *  @exception TypeConflictException if there is a problem resolving types on the model.
     *  @exception NameDuplicationException if there is a problem creating proxy sinks or sources.
     *  @exception CloneNotSupportedException if there is a problem cloning ports or attributes.
     */
    public ProxyModelInfrastructure(ProxyModelType modelType,
            CompositeActor plainTopLevelActor) throws IllegalActionException,
            TypeConflictException, NameDuplicationException,
            CloneNotSupportedException {
        _tokenPublisher = new TokenPublisher(_PERIOD, this);
        _executor = Executors.newFixedThreadPool(_POOL_SIZE);
        _modelType = modelType;
        _topLevelActor = plainTopLevelActor;
        _loadPlainModel();
    }

    /** Create a new instance of the ProxyModelInfrastructure of the specified type from the model
     *  that was previously converted to the one supporting distributed execution by replacing certain
     *  named objects with proxy counterparts.
     *  @param modelType The type of the model which must be either client or server
     *  @param preprocessedTopLevelActor the model that was previosly processed with ProxyModelBuider
     *  and converted to the one supporting distributed execution.
     *  @param modelTypes The map from the Typeable's full name to its type.
     *  @exception IllegalActionException if there is a problem parsing model types or
     *  setting types on Typeable objects.
     */
    public ProxyModelInfrastructure(ProxyModelType modelType,
            CompositeActor preprocessedTopLevelActor,
            HashMap<String, String> modelTypes) throws IllegalActionException {
        _tokenPublisher = new TokenPublisher(_PERIOD, this);
        _executor = Executors.newFixedThreadPool(_POOL_SIZE);
        _modelType = modelType;
        _modelTypes.putAll(modelTypes);
        _topLevelActor = preprocessedTopLevelActor;
        _loadPreprocessedModel();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add ProxyModelListener in order to listen for the model events such as
     *  model expiration or exception events.
     *  @param listener The listener to add.
     *  @see #removeProxyModelListener(ProxyModelListener)
     */
    public void addProxyModelListener(ProxyModelListener listener) {
        if (_modelListeners.contains(listener)) {
            return;
        } else {
            _modelListeners.add(listener);
        }
    }

    /** Close the model along with all its connection.
     */
    public void close() {
        _pingPongExecutor.shutdownNow();
        if (_pingPongFuture != null) {
            _pingPongFuture.cancel(true);
        }
        _executor.shutdownNow();
        _tokenPublisher.cancel();
        if (_mqttClient != null) {
            try {
                _mqttClient.disconnect();
            } catch (MqttException e) {
                fireModelException(null, e);
            } finally {
                _mqttClient.terminate();
            }
        }
    }

    /** Notify the model listeners about the model connection expiration.
     */
    public void fireModelConnectionExpired() {
        for (ProxyModelListener listener : _modelListeners) {
            listener.modelConnectionExpired(this);
        }
    }

    /** Notify all model listeners that the simulation has experienced an exception.
     *  @param message The message explaining what has happened.
     *  @param e The exception (if any) that should be propagated.
     */
    public void fireModelException(String message, Throwable e) {
        if (_firedExceptionEvent) {
            _LOGGER.log(
                    Level.INFO,
                    "Trying to fire model exception from the proxy model that has fired the same event before. Ignoring to prevent recursive model exceptions",
                    e);
            return;
        }
        _firedExceptionEvent = true;
        for (ProxyModelListener listener : _modelListeners) {
            listener.modelException(this, message, e);
        }
    }

    /**
     * Notify all model listeners that a server event has occurred.
     * @param event The remote event
     */
    public void fireServerEvent(RemoteEventToken event) {
        for (ProxyModelListener listener : _modelListeners) {
            listener.onRemoteEvent(this, event);
        }
    }

    /** Return the executor to schedule short lived tasks.
     *  <p>It's used to send PongTokens outside of the MQTT listener thread
     *  since MQTTClient disallows that. </p>
     *  @return the executor to schedule short lived tasks.
     */
    public Executor getExecutor() {
        return _executor;
    }

    /** Return the manager controlling this model.
     *  @return the Manager controlling this model
     */
    public Manager getManager() {
        return _topLevelActor.getManager();
    }

    /** Get maximum latency before the proxy sink threads are forced to sleep.
     *  @return the maximum latency.
     *  @see #setMaxLatency(int)
     */
    public int getMaxlatency() {
        return _maxLatency;
    }

    /** Return the model types for certain typeable object's.
     *  The mapping is from the named object's full name to its Type's string representation.
     *  @return the _modelTypes
     */
    public HashMap<String, String> getModelTypes() {
        return _modelTypes;
    }

    /** Return the roundtrip latency of sending ping/echo requests.
     *  @return the roundtrip latency of sending ping/echo requests.
     */
    public long getPingPongLatency() {
        return _pingPonglatency;
    }

    /** Return the mappings from remote source full names to their RemoteSourceData
     *  data-structure.
     *  @return the remoteSourceMap the mappings between full name and
     *  RemoteSourceData.
     */
    public HashMap<String, ProxySourceData> getProxySourceMap() {
        return _proxySourceMap;
    }

    /** Return the map with RemoteValueListeners of the model's remote attributes.
     *  @return the map with RemoteValueListeners of the model's remote attributes.
     */
    public HashMap<String, ProxyValueListener> getRemoteAttributeListenersMap() {
        return _remoteAttributeListenersMap;
    }

    /** Return the mappings from remote attribute full names to their
     *  remote Settable instance.
     *  @return the settableAttributesMap the mappings from remote
     *  attribute full names to their remote Settable instance.
     */
    public HashMap<String, Settable> getRemoteAttributesMap() {
        return _remoteAttributesMap;
    }

    /** Return the subscription topic of the current model.
     *  @return the subscriptionTopic used to listen for tokens from
     *  other remote model.
     */
    public String getSubscriptionTopic() {
        return _subscriptionTopic;
    }

    /** Return the ticket that uniquely identifies the model.
     *  @return the ticket identifying the model.
     */
    public Ticket getTicket() {
        return _ticket;
    }

    /** Return the model's timeout period in milliseconds. if the
     *  period is less or equal to 0, the model would never timeout.
     *  @return the timeoutPeriod of the model.
     *  @see #setTimeoutPeriod(int)
     */
    public int getTimeoutPeriod() {
        return _timeoutPeriod;
    }

    /** Return the tokenPublisher used to send and batch tokens.
     *  @return The token publisher used for sending tokens.
     */
    public TokenPublisher getTokenPublisher() {
        return _tokenPublisher;
    }

    /** Return the top level actor used for running distributed simulation.
     *  @return the topLevelActor of the model.
     */
    public CompositeActor getTopLevelActor() {
        return _topLevelActor;
    }

    /** Return true if the model is stopped, otherwise return false.
     *  @return the stopped state of the model.
     *  @see #setStopped(boolean)
     */
    public boolean isStopped() {
        // TODO: Figure out if there is a difference for this class
        // between stopped and paused state.
        return _stopped;
    }

    /** Unsubscribe the listener from the model events.
     *  @param listener The listener to remove.
     *  @see #addProxyModelListener(ProxyModelListener)
     */
    public void removeProxyModelListener(ProxyModelListener listener) {
        _modelListeners.remove(listener);
    }

    /** Set the PongToken instance that was received the last.
     *  @param lastPongToken The last PongToken.
     */
    public synchronized void setLastPongToken(PongToken lastPongToken) {
        _lastPongToken = lastPongToken;
        long previousPingPongLatency = _pingPonglatency;
        _pingPonglatency = System.currentTimeMillis()
                - lastPongToken.getTimestamp();
        if (previousPingPongLatency > getMaxlatency()
                && _pingPonglatency < getMaxlatency()) {
            // Latency became acceptable, notify sinks to stop waiting.
            for (ProxySink sink : _proxySinkMap.values()) {
                synchronized (sink) {
                    sink.notifyAll();
                }
            }
        }
    }

    /** Set the maximum latency before the proxy sink threads are forced to sleep.
     *  @param maxLatency the maximum latency.
     *  @see #getMaxlatency()
     */
    public void setMaxLatency(int maxLatency) {
        _maxLatency = maxLatency;
    }

    /** Set the stopped state of the model.
     *  @param stopped indicates if the model is stopped or not.
     *  @see #isStopped()
     */
    public void setStopped(boolean stopped) {
        _stopped = stopped;
    }

    /** Set the model's timeout period in milliseconds. If the period is set
     *  to 0 or less, the model would never timeout.
     *  @param timeoutPeriod the timeout period of the model.
     *  @see #getTimeoutPeriod()
     */
    public void setTimeoutPeriod(int timeoutPeriod) {
        _timeoutPeriod = timeoutPeriod;
    }

    /** Set up the communication infrastructure.
     *  @param ticket The ticket associated with this remote model.
     *  @param brokerHostname The hostname of the MQTT broker.
     *  @return The manager of the model
     *  @exception MqttException If there is a problem connecting to the broker.
     *  @exception IllegalActionException If there is problem creating the manager.
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

    /** Close the model before finalizing.
     *  @see java.lang.Object#finalize()
     *  @exception Throwable If finalizing fails to close model and/or connections.
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the last pong token.
     *  @return the last pong token.
     */
    private synchronized PongToken _getLastPongToken() {
        return _lastPongToken;
    }

    /** Load plain model into the infrastructure by first converting it using ProxyModelBuilder.
     *  @exception IllegalActionException if there is a problem setting a manager or
     *  capturing certain type information.
     *  @exception TypeConflictException if there is a problem resolving types on the model.
     *  @exception NameDuplicationException if there is a problem creating proxy sinks or sources.
     *  @exception CloneNotSupportedException if there is a problem cloning ports or attributes.
     */
    private void _loadPlainModel() throws IllegalActionException,
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
            proxySink.setProxyModelInfrastructure(this);
        }
    }

    /** Load the model that already has ProxySinks/Sources and set appropriate model types from
     *  the inferred model mapping.
     *  <p>This method is indented to be used on the Android to avoid
     *  loading unneeded actors.</p>
     *
     * @exception IllegalActionException if there is a problem parsing model types or
     * setting types on Typeable objects.
     */
    private void _loadPreprocessedModel() throws IllegalActionException {
        for (Object obj : getTopLevelActor().deepEntityList()) {
            ComponentEntity actor = (ComponentEntity) obj;
            if (actor instanceof ProxySink) {
                ProxySink proxySink = (ProxySink) actor;
                proxySink.setTokenPublisher(_tokenPublisher);
                _proxySinkMap.put(proxySink.getTargetEntityName(), proxySink);
                proxySink.setProxyModelInfrastructure(this);
            } else if (actor instanceof ProxySource) {
                ProxySource proxySource = (ProxySource) actor;
                ProxySourceData remoteSourceData = new ProxySourceData(
                        proxySource);
                proxySource.setProxySourceData(remoteSourceData);
                proxySource.setProxyModelInfrastructure(this);
                getProxySourceMap().put(proxySource.getTargetEntityName(),
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
                        // Not sure if this is possible, but just in case.
                        throw new IllegalActionException(
                                port,
                                "Type constraint for the port was not found.\n"
                                        + "The port did not have an attribute named \"targetPortName\", which would be added by ProxyActor "
                                        + "if the port was typed and the full name of the original port saved within an attribute."
                                        + "The port name information is needed for setting port type information when the spliced-up model is recreated from the XML.\n"
                                        + "The _modelTypes map is of size "
                                        + "_modelTypes.size() and modelTypes contains: "
                                        + _modelTypes);
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
        ServerUtility.findRemoteAttributes(
                ServerUtility.deepAttributeList(_topLevelActor),
                _remoteAttributesMap);
    }

    /** Set up MQTT connection.
     *  @param address Address of the MQTT broker.
     *  @exception MqttException if there is a problem subscribing to topic.
     */
    private void _setUpMQTT(String address) throws MqttException {
        _mqttClient = MqttClient.createMqttClient(address, null);
        String topic = getTicket().getTicketID() + _modelType;
        try {
            _mqttClient.connect(topic, true, (short) 10);
        } catch (MqttException ex) {
            MqttException exception = new MqttException(
                    "Failed to connect to topic \""
                            + topic
                            + "\".  Perhaps the mosquitto daemon is not running? "
                            + "See $PTII/ptserver/control/PtolemyServer.java.");
            exception.initCause(ex);
            throw exception;
        }
        _tokenPublisher.setMqttClient(_mqttClient);
        _tokenPublisher.setTopic(_publishingTopic);
        _mqttClient.registerSimpleHandler(new TokenListener(this));
        _mqttClient.subscribe(new String[] { getSubscriptionTopic() },
                new int[] { QOS_LEVEL });
    }

    /** Initialize the manager of the model.
     *  @exception IllegalActionException If there is a problem setting the
     *  manager of the top level actor.
     */
    private void _setUpManager() throws IllegalActionException {
        Manager manager = new Manager(_topLevelActor.workspace(), null);
        _topLevelActor.setManager(manager);
        _topLevelActor.addPiggyback(new Executable() {

            @Override
            public void addInitializable(Initializable initializable) {
            }

            @Override
            public void fire() throws IllegalActionException {
            }

            @Override
            public void initialize() throws IllegalActionException {
                setStopped(false);
            }

            @Override
            public boolean isFireFunctional() {
                return false;
            }

            @Override
            public boolean isStrict() throws IllegalActionException {
                return false;
            }

            @Override
            public int iterate(int count) throws IllegalActionException {
                // FIXME: Not sure if this is correct
                throw new IllegalActionException("Iterating is not supported");
            }

            @Override
            public boolean postfire() throws IllegalActionException {
                return true;
            }

            @Override
            public boolean prefire() throws IllegalActionException {
                return true;
            }

            @Override
            public void preinitialize() throws IllegalActionException {
            }

            @Override
            public void removeInitializable(Initializable initializable) {
            }

            @Override
            public void stop() {
                _stopExecution();
            }

            @Override
            public void stopFire() {
                _stopExecution();
            }

            @Override
            public void terminate() {
            }

            @Override
            public void wrapup() throws IllegalActionException {
            }

            private void _stopExecution() {
                setStopped(true);
                for (ProxySourceData data : getProxySourceMap().values()) {
                    synchronized (data.getProxySource()) {
                        data.getProxySource().notifyAll();
                    }
                }
                for (ProxySink sink : _proxySinkMap.values()) {
                    synchronized (sink) {
                        sink.notifyAll();
                    }
                }
            }
        });
    }

    /** Set up model monitoring infrastructure.
     */
    private void _setUpMonitoring() {
        setLastPongToken(new PongToken(System.currentTimeMillis()));
        _pingPongExecutor = Executors.newSingleThreadScheduledExecutor();
        _pingPongFuture = _pingPongExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    long msTime = System.currentTimeMillis();
                    _tokenPublisher.sendToken(new PingToken(msTime), null);

                    _LOGGER.info("Sent ping token");
                    long latency = msTime - _getLastPongToken().getTimestamp();
                    // update ping pong latency if the token was not received roughly within last 2 periods.
                    if (latency > _PING_PERIOD * 2) {
                        _pingPonglatency = latency;
                    }
                    if (_timeoutPeriod > 0) {
                        if (latency > _timeoutPeriod) {
                            fireModelConnectionExpired();
                        }
                    }
                } catch (Throwable e) {
                    fireModelException("Exception in the monitoring system", e);
                }
            }
        }, 0, _PING_PERIOD, TimeUnit.MILLISECONDS);
    }

    /** Set up remote attribute listeners.
     */
    private void _setUpRemoteAttributes() {
        for (Settable settable : _remoteAttributesMap.values()) {
            ProxyValueListener listener = new ProxyValueListener(
                    _tokenPublisher);
            settable.addValueListener(listener);
            _remoteAttributeListenersMap.put(settable.getFullName(), listener);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The type of the remote model.
     */
    private final ProxyModelType _modelType;

    /** Indicator if the model is stopped.
     */
    private volatile boolean _stopped;

    /** The map from the Typeable's full name to its type.
     */
    private final HashMap<String, String> _modelTypes = new HashMap<String, String>();

    /** The mapping from the original source actor name to its remote
     *  source actor and queue.
     */
    private final HashMap<String, ProxySourceData> _proxySourceMap = new HashMap<String, ProxySourceData>();

    /** The mapping from the original sink actor name to its remote sink.
     */
    private final HashMap<String, ProxySink> _proxySinkMap = new HashMap<String, ProxySink>();

    /** The mapping from the original settable object name to the
     *  remote representation.
     */
    private final HashMap<String, Settable> _remoteAttributesMap = new HashMap<String, Settable>();

    /** The mapping from the original settable object name to its
     *  attribute listener.
     */
    private final HashMap<String, ProxyValueListener> _remoteAttributeListenersMap = new HashMap<String, ProxyValueListener>();

    /** The token publisher used to batch tokens sent by the remote sink.
     */
    private final TokenPublisher _tokenPublisher;

    /** The top level actor of the loaded model.
     */
    private final CompositeActor _topLevelActor;

    /** The mqtt client connection.
     */
    private IMqttClient _mqttClient;

    /** The topic used to listen for incoming mqtt messages.
     */
    private String _subscriptionTopic;

    /** The topic used to publish outgoing mqtt messages.
     */
    private String _publishingTopic;

    /** The last pong token received from the remoteModel.
     */
    private volatile PongToken _lastPongToken;

    /** The model listeners.
     */
    private final List<ProxyModelListener> _modelListeners = new CopyOnWriteArrayList<ProxyModelListener>();

    /** The ticket identifying the model.
     */
    private Ticket _ticket;

    /** The roundtrip latency for sending ping token and receiving pong token back.
     */
    private volatile long _pingPonglatency;

    /** The executor used to schedule short lived tasks.
     */
    private final ExecutorService _executor;

    /** Model time out period.
     */
    private int _timeoutPeriod = 60000;

    /** The period between sending ping tokens.
     */
    private static final int _PING_PERIOD;

    /** Size of the thread pool.
     */
    private static final int _POOL_SIZE = 3;

    /** Token publishing period in milliseconds.
     */
    private static final int _PERIOD;

    /** The maximum latency before forcing the proxy sinks to sleepl.
     */
    private int _maxLatency = 500;

    /**
     * The executor that sends periodical ping messages.
     */
    private ScheduledExecutorService _pingPongExecutor;
    /**
     * The future that sends ping pongs.
     */
    private ScheduledFuture<?> _pingPongFuture;

    /**
     * The logger used by the ptserver.
     */
    private static final Logger _LOGGER = Logger.getLogger("PtolemyServer");

    /**
     * Flag used to prevent recursive exception events.
     */
    private boolean _firedExceptionEvent = false;

    static {
        ResourceBundle config = ResourceBundle
                .getBundle("ptserver.PtolemyServerConfig");
        int val;
        try {
            val = Integer.parseInt(config.getString("PING_PERIOD"));
        } catch (Throwable e) {
            val = 1000;
        }
        _PING_PERIOD = val;

        try {
            val = Integer.parseInt(config.getString("PERIOD"));
        } catch (Throwable e) {
            val = 100;
        }
        _PERIOD = val;
    }
}
