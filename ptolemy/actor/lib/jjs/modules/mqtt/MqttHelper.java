/* AnExecute a script in JavaScript.

   Copyright (c) 2014 The Regents of the University of California.
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
package ptolemy.actor.lib.jjs.modules.mqtt;

import java.util.Random;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

///////////////////////////////////////////////////////////////////
//// WebSocketHelper

/**
   A helper class for the MQTT module in JavaScript.
   Creates one Paho MQTT client per MqttHelper.
   
   @author Hokeun Kim
   @version $Id$
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (bilung)
 */
public class MqttHelper {
    
    /**
     * This constructor creates one Paho MQTT client inside using given parameters
     * and connects the created client to the broker server.
     * 
     * @param engine The JavaScript engine of the JavaScript actor.
     * @param namespaceName The name of the JavaScript module namespace.
     * @param currentObj The JavaScript instance of the WebSocket.
     * @param port The port number of the broker server.
     * @param host The host name of the broker server.
     * @throws MqttException
     */
    public MqttHelper(ScriptEngine engine, String namespaceName, Object currentObj,
            int port, String host, String clientId) throws MqttException {
        _engine = engine;
        _namespaceName = namespaceName;
        _currentObj = currentObj;
        
        MemoryPersistence persistence = new MemoryPersistence();
        String hostUrl = "tcp://" + host + ":" + port;
        
        _mqttClient = new MqttAsyncClient(hostUrl, clientId, persistence);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        
        _mqttClient.connect(connOpts, null, new IMqttActionListener() {
            
            @Override
            public void onSuccess(IMqttToken arg0) {
                try {
                    Object obj = _engine.eval(_namespaceName);
                    
                    Object[] args = new Object[2];
                    args[0] = _currentObj;
                    
                    args[1] = "connect";
                    ((Invocable) _engine).invokeMethod(obj, "invokeCallback", args);
                }
                catch (NoSuchMethodException | ScriptException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
            @Override
            public void onFailure(IMqttToken arg0, Throwable arg1) {
                try {
                    Object obj = _engine.eval(_namespaceName);
                    
                    Object[] args = new Object[3];
                    args[0] = _currentObj;
                    
                    args[1] = "error";

                    Object[] jsArgs = new Object[1];
                    jsArgs[0] = _engine.eval("new Error('Connection refused')");
                    args[2] = jsArgs;
                    
                    ((Invocable) _engine).invokeMethod(obj, "invokeCallback", args);
                }
                catch (NoSuchMethodException | ScriptException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        
        _mqttClient.setCallback(new MqttCallback() {
            
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                try {
                    Object obj = _engine.eval(_namespaceName);
                    
                    Object[] args = new Object[3];
                    args[0] = _currentObj;
                    
                    args[1] = "message";

                    Object[] jsArgs = new Object[2];
                    jsArgs[0] = topic;
                    jsArgs[1] = message.getPayload();
                    args[2] = jsArgs;
                    
                    ((Invocable) _engine).invokeMethod(obj, "invokeCallback", args);
                }
                catch (NoSuchMethodException | ScriptException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
            @Override
            public void deliveryComplete(IMqttDeliveryToken arg0) {
                try {
                    Object obj = _engine.eval(_namespaceName);
                    
                    Object[] args = new Object[2];
                    args[0] = _currentObj;
                    
                    args[1] = "published";
                    ((Invocable) _engine).invokeMethod(obj, "invokeCallback", args);
                }
                catch (NoSuchMethodException | ScriptException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
            @Override
            public void connectionLost(Throwable arg0) {
                try {
                    Object obj = _engine.eval(_namespaceName);
                    
                    Object[] args = new Object[2];
                    args[0] = _currentObj;
                    
                    args[1] = "close";
                    ((Invocable) _engine).invokeMethod(obj, "invokeCallback", args);
                }
                catch (NoSuchMethodException | ScriptException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////
    /** 
     * Publish an MQTT message to subscribers listening to the topic.
     * 
     *  @param topic The topic to which to publish.
     *  @param message The message sent to subscribers.
     *  @param qos The QoS level of the message. (0: At most once, 1: At least once, 2: Exactly once)
     *  @param retain Whether the sever should hold on the message after it has been delivered to
     *  current subscribers so that a newly incoming subscriber can receive the message later.
     *  @throws MqttException If the publish fails.
     */
    public void publish(String topic, String message, Integer qos, boolean retain)
            throws MqttException {
        byte[] payload = message.getBytes();
        
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
     * @throws MqttException
     */
    public void subscribe(String topic, int qos) throws MqttException {
        _mqttClient.subscribe(topic, qos);
    }
    
    /**
     * Unsubscribe a topic. Stop getting messages on the topic.
     * 
     * @param topic The topic which the client will unsubscribe.
     * @throws MqttException
     */
    public void unsubscribe(String topic) throws MqttException {
        _mqttClient.unsubscribe(topic);
    }
    
    /**
     * Disconnect from the broker server and close (i.e. return all allocated resources) the client.
     * 
     * @throws MqttException
     */
    public void end() throws MqttException {
        if (_mqttClient.isConnected()) {
            _mqttClient.disconnect();
        }
        _mqttClient.close();
    }
    
    /**
     * Return whether the client is connected to a broker server.
     * 
     * @return if the client is connected.
     */
    public boolean isConnected() {
        return _mqttClient.isConnected();
    }

    /**
     * Generate a default client ID randomly.
     * 
     * @return generated client ID.
     */
    public static String getDefaultId() {
        byte[] idBytes = new byte[8];
        new Random().nextBytes(idBytes);
        String newId = "mqttpt_" + javax.xml.bind.DatatypeConverter.printHexBinary(idBytes);
        return newId;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     private methods                       ////
    
    
    /**
     * Private constructor for WebSocketHelper with a server-side web socket.
     * The server-side web socket is given from the web socket server.
     */

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////
      
    /** Instance of the current JavaScript engine. */
    private static ScriptEngine _engine;
    
    /** The name of the constructor of the JavaScript module. */
    private String _namespaceName;
    
    /** The current instance of the JavaScript module. */
    private Object _currentObj;

    /** The internal MQTT client created in Java */
    private MqttAsyncClient _mqttClient = null;
}
