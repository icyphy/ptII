package ptserver.communication;

import com.ibm.mqtt.IMqttClient;
import com.ibm.mqtt.MqttClient;
import com.ibm.mqtt.MqttException;
import com.ibm.mqtt.MqttPersistence;
import com.ibm.mqtt.MqttPersistenceException;
import com.ibm.mqtt.MqttSimpleCallback;

/* 
 * PushService that does all of the work.
 * Most of the logic is borrowed from KeepAliveService.
 * http://code.google.com/p/android-random/source/browse/trunk/TestKeepAlive/src/org/devtcg/demo/keepalive/KeepAliveService.java?r=219
 */
public class MQTTPublisher {

	// the port at which the broker is running.
	private static int MQTT_BROKER_PORT_NUM = 1883;
	// Let's not use the MQTT persistence.
	private static MqttPersistence MQTT_PERSISTENCE = null;
	// We don't need to remember any state between the connections, so we use a
	// clean start.
	// Set quality of services to 0 (at most once delivery), since we don't want
	// push notifications
	// arrive more than once. However, this means that some messages might get
	// lost (delivery is not guaranteed)
	private static int[] MQTT_QUALITIES_OF_SERVICE = { 2 };
	private static int MQTT_QUALITY_OF_SERVICE = 2;
	// The broker should not retain any messages.
	private static boolean MQTT_RETAINED_PUBLISH = false;

	// log helper function
	private void log(String message) {
		log(message, null);
	}

	private void log(String message, Throwable e) {
		if (e != null) {
			System.out.println(message + " " + e);

		} else {
			System.out.println(message);
		}
	}

	IMqttClient mqttClient = null;

	// Creates a new connection given the broker address and initial topic
	public MQTTPublisher(String brokerHostName, String deviceId) throws MqttException {
		// Create connection spec
		String mqttConnSpec = "tcp://" + brokerHostName + "@"
				+ MQTT_BROKER_PORT_NUM;
		// Create the client and connect
		mqttClient = MqttClient
				.createMqttClient(mqttConnSpec, MQTT_PERSISTENCE);
		mqttClient.connect(deviceId, true, (short) 60);
		mqttClient.ping();
		System.out.println(mqttClient.isConnected());

		// register this client app has being able to receive messages

	}

	public void addHandler(MqttSimpleCallback callback) {
		mqttClient.registerSimpleHandler(callback);
	}

	// Disconnect
	public void disconnect() {
		try {
			mqttClient.disconnect();
		} catch (MqttPersistenceException e) {
			log("MqttException"
					+ (e.getMessage() != null ? e.getMessage() : " NULL"), e);
		}
	}

	/*
	 * Send a request to the message broker to be sent messages published with
	 * the specified topic name. Wildcards are allowed.
	 */
	public void subscribeToTopic(String topicName) throws MqttException {

		if ((mqttClient == null) || (mqttClient.isConnected() == false)) {
			// quick sanity check - don't try and subscribe if we don't have
			// a connection
			log("Connection error" + "No connection");
		} else {
			String[] topics = { topicName };
			mqttClient.subscribe(topics, MQTT_QUALITIES_OF_SERVICE);
		}
	}

	/*
	 * Sends a message to the message broker, requesting that it be published to
	 * the specified topic.
	 */
	public void publishToTopic(String topicName, byte[] message)
			throws MqttException {
		if ((mqttClient == null) || (mqttClient.isConnected() == false)) {
			// quick sanity check - don't try and publish if we don't have
			// a connection
			log("No connection to public");
		} else {
			mqttClient.publish(topicName, message, MQTT_QUALITY_OF_SERVICE,
					MQTT_RETAINED_PUBLISH);
		}
	}

}