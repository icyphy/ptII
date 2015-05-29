var dgram = require('dgram');

var d = new Date();
var time = (d.getMonth() + 1) + "/" + d.getDate() + "/" + d.getFullYear();
time = time + "-" + d.getHours() + ":" + d.getMinutes() + ":" + d.getSeconds();
time = time + ":" + d.getMilliseconds();

var message = new Buffer("Current time: " + time);
var client = dgram.createSocket("udp4");

client.send(message, 0, message.length, 8084, "localhost" 
    , function(err) { client.close(); }
);

