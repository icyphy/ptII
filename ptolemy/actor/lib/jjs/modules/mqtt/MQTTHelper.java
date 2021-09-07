/* A helper class for the MQTT module in JavaScript.

Copyright (c) 2015-2018 The Regents of the University of California.
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
package ptolemy.actor.lib.jjs.modules.mqtt;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.actor.lib.jjs.HelperBase;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// WebSocketHelper

/**
   A helper class for the MQTT module in JavaScript.
   Creates one Paho MQTT client per MQTTHelper.

   @author Hokeun Kim
   @version $Id$
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (bilung)
 */
public class MQTTHelper extends HelperBase {

    /** Construct a MQTThelper for the specified JavaScript object.
     *  The argument can be a JavaScript actor or an instance of a
     *  JavaScript class.
     *  @param actor The actor that this is helping.
     *  @param helping The JS object that this is helping.
     */
    public MQTTHelper(Object actor, ScriptObjectMirror helping) {
        super(actor, helping);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get or create a helper for the specified actor.  If one has
     *  been created before and has not been garbage collected, return
     *  that one. Otherwise, create a new one.
     *  @param actor Either a JavaScript actor or a
     *  RestrictedJavaScriptInterface.
     *  @param helping The JavaScript object that this is helping.
     *  @return The MQTTHelper.
     */
    public static MQTTHelper getOrCreateHelper(Object actor,
            ScriptObjectMirror helping) {
        if (_mqttHelper == null) {
            // FIXME: This isn't right. Instead of a static
            // _mqttHelper, this should use a mechanism like in
            // VertHelperBase to associate helpers with
            // actors. Perhaps this should subclass VertxHelperBase?
            _mqttHelper = new MQTTHelper(actor, helping);
        }
        return _mqttHelper;
    }

    /**
     * Generate a default client ID randomly.
     *
     * @return generated client ID.
     */
    public static String getDefaultId() {
        byte[] idBytes = new byte[8];
        _random.nextBytes(idBytes);

        // Don't use
        // javax.xml.bind.DatatypeConverter.printHexBinary()
        // here because javax.xml.bind.DatatypeConverter is
        // not directly available in Java 9.  To use it
        // requires compiling with --add-modules
        // java.xml.bind, which seems to not be easily
        // supported in Eclipse.
        // An alternative would be to use Apache commons codec,
        // but this would introduce a compile and runtime dependency.

        String newId = "mqttpt_" + new BigInteger(idBytes);
        return newId;
    }
    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////

    /** Share the Random object. */
    private static Random _random = new Random();

    /** Share the MQTTHelper object. */
    private static MQTTHelper _mqttHelper = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public classes                    ////

    /** A wrapper for a Paho MQTT client. */
    public class MQTTClientWrapper {
        /**
         * This constructor creates one Paho MQTT client inside using given parameters.
         *
         * @param currentObj The JavaScript instance of the WebSocket.
         * @param port The port number of the broker server.
         * @param host The host name of the broker server.
         * @param clientId The id of the client, whiich is passed to MqttAsyncClient();
         * @param rawBytes True if the payload is raw bytes.
         * @exception MqttException
         */
        public MQTTClientWrapper(ScriptObjectMirror currentObj, int port,
                String host, String clientId, boolean rawBytes)
                throws MqttException {
            _currentObj = currentObj;

            MemoryPersistence persistence = new MemoryPersistence();
            String hostUrl = "tcp://" + host + ":" + port;

            _mqttClient = new MqttAsyncClient(hostUrl, clientId, persistence);
            _rawBytes = rawBytes;
            _connOpts = new MqttConnectOptions();
            _connOpts.setCleanSession(true);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /**
         * Start connection between the client and the broker server.
         * @exception MqttSecurityException
         * @exception MqttException
         */
        public void start() throws MqttSecurityException, MqttException {
            _mqttClient.setCallback(new MqttCallback() {

                @Override
                public void messageArrived(String topic, MqttMessage message)
                        throws Exception {
                    // TODO: Process differently depending on types
                    Object messageObject;
                    if (_rawBytes) {
                        messageObject = _toJSArray(message.getPayload());
                    } else {
                        messageObject = new String(message.getPayload(),
                                StandardCharsets.UTF_8);
                    }
                    _currentObj.callMember("emit", "message", topic,
                            messageObject);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken arg0) {
                    _currentObj.callMember("emit", "published");
                }

                @Override
                public void connectionLost(Throwable arg0) {
                    _currentObj.callMember("emit", "close");
                }
            });

            _mqttClient.connect(_connOpts, null, new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken arg0) {
                    _currentObj.callMember("emit", "connect");
                }

                @Override
                public void onFailure(IMqttToken arg0, Throwable arg1) {
                    _error(_currentObj, "Connection refused.", arg1);
                }
            });
        }

        /**
         * Publish an MQTT message to subscribers listening to the topic.
         *
         *  @param topic The topic to which to publish.
         *  @param message The message sent to subscribers.
         *  @param qos The QoS level of the message. (0: At most once, 1: At least once, 2: Exactly once)
         *  @param retain Whether the sever should hold on the message after it has been delivered to
         *  current subscribers so that a newly incoming subscriber can receive the message later.
         *  @exception MqttException If the publish fails.
         *  @exception IllegalActionException
         */
        public void publish(String topic, Object message, Integer qos,
                boolean retain) throws MqttException, IllegalActionException {
            byte[] payload;
            if (_rawBytes) {
                payload = _toJavaBytes(message);
            } else {
                payload = ((String) message).getBytes(StandardCharsets.UTF_8);
            }

            MqttMessage mqttMessage = new MqttMessage(payload);

            mqttMessage.setQos(qos);
            mqttMessage.setRetained(retain);
            _mqttClient.publish(topic, mqttMessage);
        }

        /**
         * Subscribe a topic using the given maximum QoS level. Start getting messages on the topic.
         *
         * @param topic The topic which the client will subscribe.
         * @param qos The maximum QoS at which to subscribe. Messages published at
         * a lower quality of service will be received at the published QoS. Messages published
         * at a higher quality of service will be received using the QoS specified on the subscribe.
         * @exception MqttException
         */
        public void subscribe(String topic, int qos) throws MqttException {
            _mqttClient.subscribe(topic, qos);
        }

        /**
         * Unsubscribe a topic. Stop getting messages on the topic.
         *
         * @param topic The topic which the client will unsubscribe.
         * @exception MqttException
         */
        public void unsubscribe(String topic) throws MqttException {
            _mqttClient.unsubscribe(topic);
        }

        /**
         * Disconnect from the broker server and close (i.e. return all allocated resources) the client.
         *
         * @exception MqttException
         */
        public void end() throws MqttException {
            if (_mqttClient.isConnected()) {
                _mqttClient.disconnect();
            }
            try {
                _mqttClient.close();
            } catch (Throwable ex) {
                System.err.println("Closing MQTT connection failed: " + ex);
            }
        }

        /**
         * Return whether the client is connected to a broker server.
         *
         * @return if the client is connected.
         */
        public boolean isConnected() {
            return _mqttClient.isConnected();
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private methods                   ////

        /**
         * Private constructor for WebSocketHelper with a server-side web socket.
         * The server-side web socket is given from the web socket server.
         */

        /** Whether to use raw bytes or string for data to be published. */
        private boolean _rawBytes;

        /** The current instance of the JavaScript module. */
        private ScriptObjectMirror _currentObj;

        /** The internal MQTT client created in Java */
        private MqttAsyncClient _mqttClient = null;

        /** Connection options for the current MQTT connection */
        MqttConnectOptions _connOpts = null;
    }
}
