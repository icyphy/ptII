var WebSocketServer = require('ws').Server
  , wss = new WebSocketServer({port: 1984});

var replyCnt = 0;

wss.on('connection', function(ws) {
    ws.on('message', function(message) {
        console.log('received: %s', message);
        replyCnt ++;
        ws.send('port: 1984, reply count: ' + replyCnt);
    });
});

