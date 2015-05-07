var WebSocketServer = require('ws').Server
  , wss = new WebSocketServer({port: 8080});

var replyCnt = 0;

wss.on('connection', function(ws) {
    ws.on('message', function(message) {
        console.log('received: %s', message);
        replyCnt ++;
        ws.send('port: 8080, reply count: ' + replyCnt);
    });
});

