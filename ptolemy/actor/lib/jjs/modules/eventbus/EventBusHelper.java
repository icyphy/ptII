/* A Javascript helper for Vert.x.

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
package ptolemy.actor.lib.jjs.modules.eventbus;

import java.util.HashMap;
import java.util.Map;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;

///////////////////////////////////////////////////////////////////
//// EventBusHelper

/** A helper class for the Vert.x event bus API. An instance of this
 *  class is associated with a JavaScript object that can publish or subscribe
 *  to events on the event bus event.  The associated JavaScript object is
 *  passed in as a constructor argument and is expected to implement the
 *  event emitter pattern, for example by inheriting from EventEmitter
 *  class of the events module using util.inherits().
 *  <p>
 *  This class follows the instructions for "Embedding Vert.x core"
 *  at <a href="http://vertx.io/embedding_manual.html">http://vertx.io/embedding_manual.html</a>.
 *  It states there that "Please note this feature is intended for power users only,"
 *  but we will not be using Vert.x to run verticles or modules, so this seems
 *  appropriate. We are using it only for the event bus and networking
 *  infrastructure.
   
   @author Patricia Derler and Edward A. Lee
   @version $Id$
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (pd)
   @Pt.AcceptedRating Red (pd)
 */
public class EventBusHelper {
        
    /** Create a EventBusHelper for the specified publish or subscribe
     *  client for the event bus using the specified port and hostname
     *  for cluster connections.
     *  If the hostname is null, then this will use
     *  an unclustered instance of Vertx whose event bus will not
     *  communicate with any other instances of Vertx.  The hostname
     *  is something like "localhost" or
     *  "10.0.0.4", and it specifies a local network device over
     *  which to listen for cluster connections (e.g. wifi or
     *  ethernet). The port is the port to listen over.
     *  The default for Vertx is 25500.
     *  @param jsObject The JavaScript object that will subscribe to the event bus.
     *  @param clusterHost The port over which to listen for cluster connections.
     *  @param clusterHost The host interface over which to listen for cluster connections,
     *   or null to create an unclustered EventBusHelper.
     */
    public EventBusHelper(ScriptObjectMirror jsObject, int clusterPort, String clusterHost) {
	_currentObj = jsObject;
	if (clusterHost == null) {
	    if (_unclusteredVertxInstance == null) {
		_unclusteredVertxInstance = VertxFactory.newVertx();
	    }
	    _vertx = _unclusteredVertxInstance;
	} else {
	    if (_vertxInstances == null) {
		_vertxInstances = new HashMap<String,Map<Integer,Vertx>>();
	    }
	    // FIXME: How to ensure that "localhost" and "127.0.0.1" return
	    // the same helper? localhost could resolve to something else,
	    // e.g. under IPv6.
	    Map<Integer,Vertx> instances = _vertxInstances.get(clusterHost);
	    if (instances == null) {
		_vertx = VertxFactory.newVertx(clusterPort, clusterHost);
		instances = new HashMap<Integer,Vertx>();
		instances.put(clusterPort, _vertx);
		_vertxInstances.put(clusterHost, instances);
	    } else {
		Vertx instance = instances.get(clusterPort);
		if (instance == null) {
		    _vertx = VertxFactory.newVertx(clusterPort, clusterHost);
		    instances.put(clusterPort, _vertx);
		} else {
		    _vertx = instance;
		}
	    }
	}
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////
    
    /** Publish text data onto the Vertx event bus. 
     *  @param address The address, (topic, channel name, stream ID,...) 
     *  @param message A message to be published, as a string.
     */
    public void publish(String address, String message) {
	EventBus bus = _vertx.eventBus();
	bus.publish(address, message);
    }
    
    /** Send text data to exactly one recipient on the Vertx event bus.
     *  According to the Vert.x documentation, the recipient is choosen
     *  in a loosely round-robin fashion. If a reply has been set via
     *  {@link #setReply(String)}, then if an when the reply is received,
     *  the associated JavaScript object's reply() function will be invoked.
     *  @param address The address, (topic, channel name, stream ID,...) 
     *  @param message A message to be published, as a string.
     */
    public void send(String address, String message) {
	EventBus bus = _vertx.eventBus();
	bus.send(address, message);
    }

    /** Send text data to exactly one recipient on the Vertx event bus
     *  and handle the reply by invoking the specified function.
     *  According to the Vert.x documentation, the recipient is choosen
     *  in a loosely round-robin fashion. If a reply has been set via
     *  {@link #setReply(String)}, then if an when the reply is received,
     *  the associated JavaScript object's reply() function will be invoked.
     *  @param address The address, (topic, channel name, stream ID,...) 
     *  @param message A message to be published, as a string.
     */
    public void send(String address, String message, final Object replyHandler) {
	EventBus bus = _vertx.eventBus();
	Handler<Message> newHandler = new Handler<Message>() {
	    public void handle(Message message) {
		_currentObj.callMember("notifyReply", replyHandler, message.body());
	    }
	};
	bus.send(address, message, newHandler);
    }

    /** Set the reply to send in response to any point-to-point
     *  events received in the future.
     *  @param reply A reply to send when an event is received, or null
     *   to not send any replies.
     */
    public void setReply(String reply) {
	_reply = reply;
    }

    /** Subscribe to the specified address on the event bus.
     *  Whenever an event is published to this address by some
     *  client in the event bus cluster, this
     *  method will cause an event with the same name as address to be
     *  emitted on the associated JavaScript object
     *  with the message body as the value of the event.
     *  If this object is already subscribed to this address, then
     *  do nothing.
     *  @param address The address on the bus to subscribe to.
     *  @see #unsubscribe(String)
     */
    public void subscribe(final String address) {
	if (_subscriptions == null) {
	    _subscriptions = new HashMap<String,Handler>();
	}
	if (_subscriptions.get(address) != null) {
	    return;
	}
	Handler<Message> newHandler = new Handler<Message>() {
	    public void handle(Message message) {
                _currentObj.callMember("notify", address, message.body());
                if (_reply != null) {
                    message.reply(_reply);
                }
	    }
	};
	_subscriptions.put(address, newHandler);
	EventBus bus = _vertx.eventBus();
	bus.registerHandler(address, newHandler);
    }

    /** Unsubscribe the associated JavaScript object as a subscriber to the
     *  specified address on the event bus.
     *  @param address The address on the bus to unsubscribe to, or null to
     *   unsubscribe to all addresses.
     *  @see #subscribe(String)
     *  @see #unsubscribe()
     */
    public void unsubscribe(final String address) {
	if (_subscriptions != null) {
	    EventBus bus = _vertx.eventBus();
	    if (address == null) {
		for (String toUnsubscribe : _subscriptions.keySet()) {
		    Handler<Message> previousHandler = _subscriptions.get(toUnsubscribe);
		    bus.unregisterHandler(toUnsubscribe, previousHandler);
		}
		_subscriptions.clear();
	    } else {
		Handler<Message> previousHandler = _subscriptions.get(address);
		if (previousHandler != null) {
		    bus.unregisterHandler(address, previousHandler);
		    _subscriptions.remove(address);
		}
	    }
	}
    }
   
    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////
    
    /** The current instance of the JavaScript module. */
    private ScriptObjectMirror _currentObj;
    
    /** The reply to send in response to received messages, or null to send no reply. */
    private String _reply = null;

    /** Map from addresses to which the associated JavaScript object is
     *  subscribed to the handler function.
     */
    private Map<String,Handler> _subscriptions;
    
    /** An unclustered Vertx instance, if it has been created. */
    private static Vertx _unclusteredVertxInstance;

    /** The Vertx instance for this helper instance.
     *  There is at most one of these for each hostname/port pair
     *  over which clusters are formed for the event bus.
     */
    private Vertx _vertx;
    
    /** The platform manager instances, indexed by hostname and port. */
    private static Map<String,Map<Integer,Vertx>> _vertxInstances;
}
