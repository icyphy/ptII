var vertx = require('vertx');

var httpServer = vertx.createHttpServer();

var sockJSServer = vertx.createSockJSServer(httpServer);

sockJSServer.bridge({"prefix" : "/eventbus", "session_timeout": 10000, "ping_interval": 100000, "vertxbus_ping_interval": 100000, "heartbeat_period": 10000}, [{}], [{}] );

httpServer.listen(8080);