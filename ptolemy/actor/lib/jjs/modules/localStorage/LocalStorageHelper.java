/* Execute a script in JavaScript.

   Copyright (c) 2014-2015 The Regents of the University of California.
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
package ptolemy.actor.lib.jjs.modules.localStorage;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.internal.MqttPersistentData;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

///////////////////////////////////////////////////////////////////
//// LocalStorageHelper

/**
 * A helper class for the LocalStorage module in JavaScript.
 * 
 * @author Hokeun Kim
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Yellow (eal)
 * @Pt.AcceptedRating Red (bilung)
 */
public class LocalStorageHelper {

	/**
	 * Construct a LocalStorageHelper using the container's name and the actors'
	 * display name for the directory name. Also use current time to make the
	 * directory name unique.
	 * 
	 * @param persistenceDirectory
	 *            The default Mqtt file persistance
	 * @param containerActorName
	 *            Container's name plus the actors' display name
	 * @throws MqttPersistenceException
	 */
	public LocalStorageHelper(String persistenceDirectory,
			String containerActorName) throws MqttPersistenceException {
		// FIXME if you can find a better directory.
		_mqttLocalStorage = new MqttDefaultFilePersistence(persistenceDirectory);
		try {
			_mqttLocalStorage.close();
		} catch (MqttPersistenceException e) {
			// Just ignore
		}

		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
		Date date = new Date();

		// FIXME Change the information for the file name here, per mqtt
		// connection.
		// _mqttLocalStorage.open("clientId", "theConnection");
		_mqttLocalStorage.open("mqls-" + containerActorName,
				dateFormat.format(date));
	}

	// /////////////////////////////////////////////////////////////////
	// // public methods ////

	/**
	 * Take a key and return its value from the local storage if the key exists,
	 * otherwise, return null.
	 * 
	 * @param key
	 *            The key for the value to be returned.
	 * @return The string value associated with the key.
	 * @throws MqttPersistenceException
	 * @see #setItem(String, String)
	 */
	public String getItem(String key) throws MqttPersistenceException {
		if (_mqttLocalStorage.containsKey(key)) {
			MqttPersistentData message = (MqttPersistentData) _mqttLocalStorage
					.get(key);
			return new String(message.getHeaderBytes());
		} else {
			return null;
		}
	}

	/**
	 * Take a key-value pair and stores the pair into the local storage.
	 * 
	 * @param key
	 *            The key to be stored.
	 * @param value
	 *            The string value associated with the key.
	 * @throws MqttPersistenceException
	 * @throws IOException
	 * @see #getItem(String)
	 */
	public void setItem(String key, String value)
			throws MqttPersistenceException, IOException {
		byte[] bytes = value.getBytes();
		MqttPersistentData message = new MqttPersistentData(value, new byte[0],
				0, 0, bytes, 0, bytes.length);
		_mqttLocalStorage.put(key, message);
	}

	/**
	 * Take a key and remove it from the local storage.
	 * 
	 * @param key
	 *            The key to be removed.
	 * @throws MqttPersistenceException
	 */
	public void removeItem(String key) throws MqttPersistenceException {
		if (_mqttLocalStorage.containsKey(key)) {
			_mqttLocalStorage.remove(key);
		}
	}

	/**
	 * Remove all keys in the local storage.
	 * 
	 * @throws MqttPersistenceException
	 */
	public void clear() throws MqttPersistenceException {
		_mqttLocalStorage.clear();
	}

	/**
	 * Return a key with index n, or null if it is not present.
	 * 
	 * @param n
	 *            Index for the key to be returned.
	 * @return The key with index n.
	 * @throws MqttException
	 */
	public String key(Integer n) throws MqttException {
		Enumeration keys = _mqttLocalStorage.keys();

		int cnt = 0;

		while (keys.hasMoreElements()) {
			if (cnt == n) {
				Object obj = keys.nextElement();
				String message = (String) obj;
				return message;
			}
			cnt++;
			keys.nextElement();
		}

		return null;

	}

	/**
	 * Return the number of keys stored in the local storage.
	 * 
	 * @return The number of keys stored.
	 * @throws MqttPersistenceException
	 */
	public Integer length() throws MqttPersistenceException {
		Enumeration keys = _mqttLocalStorage.keys();
		int cnt = 0;

		while (keys.hasMoreElements()) {
			cnt++;
			keys.nextElement();
		}

		return cnt;
	}

	// /////////////////////////////////////////////////////////////////
	// // private fields ////

	/** Instance of MqttDefaultFilePersistence imported from Paho MQTT */
	private MqttDefaultFilePersistence _mqttLocalStorage;
}
