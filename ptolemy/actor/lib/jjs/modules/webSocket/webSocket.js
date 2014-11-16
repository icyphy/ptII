// Module supporting web sockets.
// Authors: Hokeun Kim and Edward A. Lee
// Copyright: http://terraswarm.org/accessors/copyright.txt
//
// FIXME: Need a closure mechanism to avoid polluting the global namespace.
module.exports = {
    hello: function(){alert("Hello World")},
    
    // Open a web socket at the specified URL.
    open: function(url, onOpen) {
        var WebSocketHelper = Java.type('ptolemy.actor.lib.jjs.modules.webSocket.WebSocketHelper');
        var socket = WebSocketHelper.open(actor.getEngine(), "foo", {
            onOpen: function() {onOpen();}
        });
    }
};