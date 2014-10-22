/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 1995-2014 The Regents of the University of California.
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

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
*/
package ptolemy.actor.lib.vertx;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSServer;
import org.vertx.java.platform.Verticle;

public class EventBusTestServer extends Verticle {
    @Override
    public void start() {
        HttpServer server = vertx.createHttpServer();
        /* Register HTTP handler */
        server.requestHandler(new Handler<HttpServerRequest>() {
            public void handle(HttpServerRequest request) {
                System.out.println("request");
            }
        });

        /* Set security permission to let everything go through */
        JsonArray permitted = new JsonArray();
        permitted.add(new JsonObject());

        /* Create SockJS and bridge it to the Event Bus */
        SockJSServer sockJSServer = vertx.createSockJSServer(server);
        sockJSServer.bridge(new JsonObject().putString("prefix", "/eventbus")
                .putNumber("heartbeat_period", 400)
                .putNumber("session_timeout", 50000)
                .putNumber("ping_interval", 100000)
                .putNumber("vertxbus_ping_interval", 100000),
                permitted, permitted);

        EventBus eb = vertx.eventBus();
        /* Register Handler 1 */
        eb.registerLocalHandler("data.comm", new Handler<Message>() {
                    @Override
                    public void handle(Message message) {
                        System.out.println("data.comm");
                    }
                });

        /* Register Handler 2 */
        eb.registerHandler("data.comm.return", new Handler<Message>() {
            @Override
            public void handle(Message message) {
                System.out.println("data.comm.return");
            }
        });
        /* Start the server */
        server.listen(7379);
    }
}
