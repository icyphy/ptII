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
