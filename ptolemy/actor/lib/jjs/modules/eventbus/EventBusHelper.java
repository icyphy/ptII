/* A Javascript helper for Vert.x.

@Copyright (c) 2015-2016 The Regents of the University of California.
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
package ptolemy.actor.lib.jjs.modules.eventbus;

import java.util.HashMap;
import java.util.Map;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.actor.lib.jjs.VertxHelperBase;

///////////////////////////////////////////////////////////////////
//// EventBusHelper

/** A helper class for the Vert.x event bus API. An instance of this
 *  class is associated with a VertxBus JavaScript object, defined
 *  in the eventbus module, that can publish or subscribe
 *  to events on the event bus event.  The VertxBus object is
 *  passed in as a constructor argument and is expected to implement the
 *  event emitter pattern, for example by inheriting from EventEmitter
 *  class of the events module using util.inherits().
 *  <p>
 *  For information about the Vert.x event bus, see
 *  <a href="http://vertx.io/core_manual_java.html#the-event-bus">http://vertx.io/core_manual_java.html#the-event-bus</a>.</p>
 *
 *  @author Patricia Derler and Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 11.0
 *  @Pt.ProposedRating Yellow (pd)
 *  @Pt.AcceptedRating Red (pd)
 */
public class EventBusHelper extends VertxHelperBase {

    /** Create an EventBusHelper for the specified actor and
     *  VertxBus JavaScript object for the event bus at the
     *  specified network interface (port and hostname).
     *  <p>
     *  If the clusterHostname is null, then this will use an
     *  unclustered instance of Vertx whose event bus will not
     *  communicate with any other instances of Vertx.  The
     *  clusterHostname is something like "localhost" or "10.0.0.4",
     *  and it specifies a local network device over which to listen
     *  for cluster connections (e.g. WiFi or Ethernet).</p>
     *  <p>
     *  The clusterPort is the tcp/ip port to which to listen for
     *  cluster connections.  The default for clusterPort for Vertx is
     *  25500.  If clusterHostname is null, then the default
     *  clusterPort value is used.
     *
     *  @param actor The actor that will publish or subscribe to the
     *   event bus.
     *  @param vertxBusJS The VertxBus JavaScript object that will
     *   publish and subscribe to the event bus.
     *  @param clusterPort The port over which to listen for cluster
     *   connections.
     *  @param clusterHostname The host interface over which to listen
     *   for cluster connections, or null to create an unclustered
     *   EventBusHelper.
     */
    public EventBusHelper(
                    Object actor,
                    ScriptObjectMirror vertxBusJS,
                    int clusterPort,
            String clusterHostname) {
            super(actor, vertxBusJS);
        _vertxBusJS = vertxBusJS;
        if (clusterHostname == null) {
            if (_unclusteredVertxInstance == null) {
                _unclusteredVertxInstance = Vertx.vertx();
            }
            _vertx = _unclusteredVertxInstance;
        } else {
            if (_vertxInstances == null) {
                _vertxInstances = new HashMap<String, Map<Integer, Vertx>>();
            }
            // FIXME: How to ensure that "localhost" and "127.0.0.1" return
            // the same helper? localhost could resolve to something else,
            // e.g. under IPv6.
            Map<Integer, Vertx> instances = _vertxInstances
                    .get(clusterHostname);
            if (instances == null) {
                _createVertx(clusterPort, clusterHostname);
                instances = new HashMap<Integer, Vertx>();
                instances.put(clusterPort, _vertx);
                _vertxInstances.put(clusterHostname, instances);
            } else {
                Vertx instance = instances.get(clusterPort);
                if (instance == null) {
                    _createVertx(clusterPort, clusterHostname);
                    instances.put(clusterPort, _vertx);
                } else {
                    _vertx = instance;
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
     *  @param replyHandler The handler for the reply.
     */
    public void send(String address, String message, final Object replyHandler) {
        EventBus bus = _vertx.eventBus();
        Handler<AsyncResult<Message<String>>> newHandler = new Handler<AsyncResult<Message<String>>>() {
            public void handle(AsyncResult<Message<String>> event) {
                _vertxBusJS.callMember("notifyReply", replyHandler,
                        event.result().body());
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
            _subscriptions = new HashMap<String, MessageConsumer<String>>();
        }
        if (_subscriptions.get(address) != null) {
            return;
        }
        Handler<Message<String>> newHandler = new Handler<Message<String>>() {
            public void handle(Message<String> message) {
                _vertxBusJS.callMember("notify", address, message.body());
                if (_reply != null) {
                    message.reply(_reply);
                }
            }
        };
        EventBus bus = _vertx.eventBus();
        MessageConsumer<String> messageConsumer = bus.consumer(address, newHandler);
        _subscriptions.put(address, messageConsumer);
    }

    /** Unsubscribe the associated JavaScript object as a subscriber to the
     *  specified address on the event bus.
     *  @param address The address on the bus to unsubscribe to, or null to
     *   unsubscribe to all addresses.
     *  @see #subscribe(String)
     */
    public void unsubscribe(final String address) {
        if (_subscriptions != null) {
            if (address == null) {
                for (String toUnsubscribe : _subscriptions.keySet()) {
                    MessageConsumer<String> messageConsumer = _subscriptions
                            .get(toUnsubscribe);
                    messageConsumer.unregister();
                }
                _subscriptions.clear();
            } else {
                MessageConsumer<String> messageConsumer = _subscriptions.get(address);
                if (messageConsumer != null) {
                    messageConsumer.unregister();
                    _subscriptions.remove(address);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create the Vertx instance we will use.
     *  @param clusterPort The port to listen for cluster information.
     *  @param clusterHostname The network interface to use for the cluster.
     */
    private void _createVertx(int clusterPort, String clusterHostname) {
        VertxOptions vertxOptions = new VertxOptions();
        vertxOptions.setClusterPort(clusterPort);
        vertxOptions.setClusterHost(clusterHostname);
        _vertx = Vertx.vertx(vertxOptions);

        /** FIXME: Some example code includes the following, but I can't find VertxOptions:
        VertxOptions options = new VertxOptions();
        Vertx.clusteredVertx(options, res -> {
          if (res.succeeded()) {
            Vertx vertx = res.result();
            EventBus eventBus = vertx.eventBus();
            System.out.println("We now have a clustered event bus: " + eventBus);
          } else {
            System.out.println("Failed: " + res.cause());
          }
        });
        */
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////

    /** The current instance of the JavaScript module. */
    private ScriptObjectMirror _vertxBusJS;

    /** The reply to send in response to received messages, or null to send no reply. */
    private String _reply = null;

    /** Map from addresses to which the associated JavaScript object is
     *  subscribed to the MessageConsumer associated with the handler.
     */
    private Map<String, MessageConsumer<String>> _subscriptions;

    /** An unclustered Vertx instance, if it has been created. */
    private static Vertx _unclusteredVertxInstance;

    /** The Vertx instance for this helper instance.
     *  There is at most one of these for each hostname/port pair
     *  over which clusters are formed for the event bus.
     */
    private Vertx _vertx;

    /** The platform manager instances, indexed by hostname and port. */
    private static Map<String, Map<Integer, Vertx>> _vertxInstances;
}
