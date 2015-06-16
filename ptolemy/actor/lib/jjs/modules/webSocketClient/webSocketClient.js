// Copyright (c) 2014-2015 The Regents of the University of California.
// All rights reserved.

// Permission is hereby granted, without written agreement and without
// license or royalty fees, to use, copy, modify, and distribute this
// software and its documentation for any purpose, provided that the above
// copyright notice and the following two paragraphs appear in all copies
// of this software.

// IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
// FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.

// THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
// ENHANCEMENTS, OR MODIFICATIONS.

/** This accessor sends and/or receives messages from a web socket at
 *  the specified host and port. In `initialize()`, it
 *  begins connecting to the web socket server.
 *  Once the connection is established, a `true` boolean is sent to
 *  the `connected` output.
 *  `'ready'` is set to `true`. If a connection
 *  was not established during `initiazlize()`, this
 *  accessor will not try to connect again.
 *
 *  Whenever an input is received on the `'toSend'`
 *  input, the message is sent to the socket.
 *
 *  Whenever a message is received from the socket, that message is
 *  produced on the `'received'` output.
 *
 *  When `wrapup()` is invoked, this accessor closes the
 *  connection.
 *
 *  The data can be any type that has a JSON representation.
 *  For incomming messages, this accessor assumes that the message is
 *  a string in UTF-8 that encodes a JSON object.<br/>
 *  A copy of this accessor is also in the modules directory, which other accessors can use as 
 *  a generic implementation of a web socket. This accessor-module exports a sendToWebSocket(data) function 
 *  which other accessors can use, for example:
 *  <pre>var wsClient = require('webSocketClient');
 *       wsClient.sendToWebSocket(JSONDataToSend);
 *  </pre> 
 *
 *  This accessor-module also exports its inputHandler function on
 *  'toSend' which other accessors can override, for example:n
 *  <pre> var wsClient = require('webSocketClient');
 *       wsClient.toSendInputHandler = function() {...}
 *  </pre>
 *  See `RosPublisher.js` for an example.
 *  This accessor requires the 'webSocket' module.
 *
 *  @module webSocketClient
 *  @parameter {string} server The IP address or domain name of server.
 *  @parameter {int} port The port that the web socket listens to.
 *  @input {JSON} toSend The data to be sent over the socket.
 *  @output {boolean} connected Output `true` on connected and `false` on disconnected.
 *  @output {JSON} received The data received from the web socket server.
 *  @author Hokeun Kim, Marcus Pan, Edward A. Lee
 *  @version $Id: WebSocketClient.js 171 2015-06-15 12:55:47Z eal $
 */

var WebSocket = require('webSocket');
var client = null;
var handle;
var wrappedUp = false;

/** Set up the accessor by defining the parameters, inputs, and outputs. */
exports.setup = function() {
  accessor.parameter('server', {
    type: 'string',
    value: 'localhost',
  });
  accessor.parameter('port', {
    type: 'int',
    value: 8080,
  });
  accessor.input('toSend', {
    type: 'JSON', 
    description: 'The data to be send to the web socket server, in JSON format.'
  });
  accessor.output('connected', {
    type: 'boolean',
  });
  accessor.output('received');
}

/** Initializes accessor by attaching functions to inputs. */
exports.initialize = function() {
  client = new WebSocket.Client(
    {'host':getParameter('server'), 'port':getParameter('port')}
  );
  client.on('open', onOpen);
  client.on('message', onMessage);
  client.on('close', onClose);
  client.on('error', function(message) {
      error(message);
  });
  handle = addInputHandler('toSend', exports.toSendInputHandler);
  console.log('initialize() complete');
} 

/** Handles input on 'toSend'. */
exports.toSendInputHandler = function() {
  exports.sendToWebSocket(get('toSend'));
}

/** Sends JSON data to the web socket. */
exports.sendToWebSocket = function(data) {
  client.send(data);
  console.log("Sending to web socket: " + JSON.stringify(data));
}

/** Executues once  web socket establishes a connection.
 *   Sets 'connected' output to true.
 */
function onOpen() {
  console.log('Status: Connection established');
  send('connected', true);
  connected = true;
}
  
/** Executes once web socket closes.
 *  Sets 'connected' output to false if accessor hasn't wrapped up.
 */
function onClose(message) {
  console.log('Status: Connection closed: ' + message);
  
  if (!wrappedUp) {
    send('connected', false);
  }
}
  
/** Outputs message received from web socket. */
function onMessage(message) {
  console.log('Received from web socket: ' + JSON.stringify(message));
  send('received', message);
}
  
/** Closes web socket connection. */
exports.wrapup = function() {
  wrappingUp = true;
  if (handle !== null) {
    removeInputHandler(handle, 'toSend');
  }
  if (client) {
    client.close();
  }
  wrappedUp = true;
  console.log('wrapup() complete');
}

