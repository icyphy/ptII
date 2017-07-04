// Below is the copyright agreement for the Ptolemy II system.
//
// Copyright (c) 2015-2016 The Regents of the University of California.
// All rights reserved.
//
// Permission is hereby granted, without written agreement and without
// license or royalty fees, to use, copy, modify, and distribute this
// software and its documentation for any purpose, provided that the above
// copyright notice and the following two paragraphs appear in all copies
// of this software.
//
// IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
// FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.
//
// THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
// ENHANCEMENTS, OR MODIFICATIONS.
//
//
// Ptolemy II includes the work of others, to see those copyrights, follow
// the copyright link on the splash page or see copyright.htm.

/**
 * Module supporting interaction through a browser. The Server object starts
 * a server that accepts web socket connections and HTTP GET and POST requests.
 *
 * This module defines one class, Browser.  After constructing an instance of
 * Browser (using new), you can request that the browser display HTML that
 * you provide using the display() function. Whenever display() is called,
 * whatever was previously displayed is replaced in the browser.
 * 
 * You can also specify resources that the HTML references (such as images)
 * by using the addResource() function. You should call this before providing
 * HTML that requires those resources.
 * 
 * You can also add listeners for data sent by the browser using POST using
 * the addListener() function. So your HTML can include forms and buttons,
 * for example, that will POST JSON data.
 * 
 * The way this module works is that it starts a web server that accepts
 * websocket connections and HTTP GET and POST requests, and then it invokes
 * the system's default browser and points it at that web server. The
 * server provides HTML that contains a script to connect to the server
 * using a websocket. It then listens for messages on that websocket, and
 * when it receives them, it displays their contents (HTML) on the web page,
 * replacing whatever was there before. Hence, a highly dynamic stream of
 * pages can be provided.
 * 
 * The websocket can also accept incoming JSON data, in which case the Browser
 * object will emit a 'message' event.
 * 
 * @module browser
 * @author Edward A. Lee
 * @version $$Id$$
 */

// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals Java, exports, module */
/*jshint globalstrict: true*/
"use strict";

var EventEmitter = require('events').EventEmitter;
var WebSocketHelper = Java.type('ptolemy.actor.lib.jjs.modules.webSocket.WebSocketHelper');
var WebSocketServerHelper = Java.type('ptolemy.actor.lib.jjs.modules.webSocket.WebSocketServerHelper');

/** Constructor for a Browser server.
 * 
 *  The options argument is a JSON object containing the following optional fields:
 *  * hostInterface: The IP address or name of the local interface for the server
 *    to listen on.  This defaults to "localhost", but if the host machine has more
 *    than one network interface, e.g. an Ethernet and WiFi interface, then you may
 *    need to specifically specify the IP address of that interface here.
 *  * port: The port on which to listen for connections (the default is 80,
 *    which is the default HTTP port).
 *    
 *    
 *  The optional header argument specifies any text to include in the header
 *  of the web page that is created.
 *  
 *  The web page is opened the first time display() is called.
 *
 *  This subclasses EventEmitter, emitting events 'listening' and 'connection'.
 *  A typical usage pattern looks like this:
 *
 *  <pre>
 *     var browser = new require('browser').Browser({'port':8082});
 *     browser.display('<h1>Hello</h1>');
 *     setTimeout(function() {
 *        browser.display('<h1>World!</h1>');
 *     }, 5000);
 *     browser.stop();
 *  </pre>
 *  
 *  This displays "Hello" and then changes it to "World!" after 5 seconds.
 *  The Browser is an event emitter, emitting events 'listening' and 'connection'.
 *  
 *  @param options The port and hostInterface options.
 *  @param header HTML to include in the page header.
 */
exports.Browser = function (options, header) {
    this.server = null;
    this.listeners = {};
    this.header = '';
    if (header) {
        this.header = header;
    }
    this.open = false;
    
    options = options || {};
    
    if (typeof options.port === 'undefined' || options.port === null) {
        this.port = 80;
    } else {
        this.port = options.port;
    }
    
    if (typeof options.hostInterface === 'undefined' || options.hostInterface === null) {
        this.hostInterface = "localhost";
    } else {
        this.hostInterface = options.hostInterface;
    }

    // NOTE: I assume we don't need SSL because the server/browser interaction is only local.
    // Also, we are currently not using the back path through the websocket from the browser
    // to the server, but it's stype is set to JSON in case we later use it.
    this.server = WebSocketServerHelper.createServer(actor,
            this, this.hostInterface, false, '', '',
            this.port, 'application/json', 'text/html');
    this.server.startServer();
    
    this.browserLauncher = Java.type('ptolemy.actor.gui.BrowserLauncher');
};
util.inherits(exports.Browser, EventEmitter);

/** Add a listener for data posted on a specific path. Only one
 *  listener can be active for a given path. The last-added listener
 *  will be used.
 *  @param path The path to listen for POST requests on.
 *  @param listener A callback function with one argument.
 */
exports.Browser.prototype.addListener = function (path, listener) {
    this.listeners[path] = listener;
};

/** Add a resource to be served by the server.
 *  @param path The path to the resource.
 *  @param resource The resource to serve.
 *  @param contentType The content type of the resource.
 */
exports.Browser.prototype.addResource = function (path, resource, contentType) {
    if (!path.startsWith('/')) {
        path = '/' + path;
    }
    this.server.addResource(path, resource, contentType);
}

/** Invoke registered listeners upon the receipt of POST data.
 *  This is called by the helper when a POST request comes in.
 *  @param path The path where the POST was received.
 *  @param data The data that was transmitted.
 */
exports.Browser.prototype.post = function (path, data) {
  if (this.listeners[path]) {
    this.listeners[path].apply(null, [data]);
  } else {
    console.log("No listener registered for path: " + path);
  }
}

/** Display the specified HTML text.
 *  @param html The HTML to display.
 */
exports.Browser.prototype.display = function (html) {
    if (!this.open) {
        // Create the template page.
        // This uses jquery because of its html() function, which unlike
        // just setting innerHTML, evaluates any scripts that might be contained.
        var script = '<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>\n\
            <script>\n\
            if ("WebSocket" in window) {\n\
                var socket = new WebSocket("ws://localhost:'
            + this.port
            + '");\n\
                socket.onmessage = function(event) {\n\
                    var fr = new FileReader();\n\
                    fr.onloadend = function() {\n\
                        var result = fr.result;\n\
                        $("#contents").html(result);\n\
                    };\n\
                    fr.readAsText(event.data);\n\
                };\n\
            } else {\n\
                document.getElementById("result").innerHTML = "Your browser does not support websockets.";\n\
            }\n\
            </script>\n';
        var template = '<!DOCTYPE html><html><head><meta charset="UTF-8"><title>Browser Swarmlet Interface</title>'
                + script
                + this.header
                + '</head><body><div id="contents"></div></body></html>'
        this.server.setResponse(template);
        this.browserLauncher.openURL('http://localhost:' + this.port);
        this.open = true;
    }
    // If there is a websocket connection, send the HTML over the
    // websocket. Otherwise, save the HTML to send it when the socket is
    // opened.
    if (this.helper && this.helper.isOpen()) {
        this.helper.send(html.toString());
    } else {
        this.pendingHTML = html.toString();
    }
};

/** Stop the server. Note that this closing happens
 *  asynchronously. The server may not be closed when this returns.
 */
exports.Browser.prototype.stop = function () {
    if (this.helper && this.helper.isOpen()) {
        this.helper.close();
    }
    if (this.server !== null) {
        this.server.closeServer();
        this.server = null;
    }
};

/** Notify that a handshake was successful and a websocket has been created.
 *  This is called by the helper class is not meant to be called by the JavaScript
 *  programmer. This server supports only one socket connection at a time, so if
 *  there already is one open, it first closes it.
 *  @param serverWebSocket The Java ServerWebSocket object.
 *  @param helper The helper in charge of this socket.
 */
exports.Browser.prototype._socketCreated = function (serverWebSocket, helper) {
    if (this.helper && this.helper.isOpen()) {
        this.helper.close();
    }
    this.helper = WebSocketHelper.createServerSocket(actor,
            this, serverWebSocket, helper, 'application/json', 'text/html');
    if (this.pendingHTML) {
        this.helper.send(this.pendingHTML);
    }
};

/** Notify this object of a received message from the socket.
 *  This function attempts to parse the message as JSON and then
 *  emits a "message" event with the message as an argument.
 *  This function is called by the helper and should not be called
 *  by the user of this module.
 *  @param message The incoming message.
 */
exports.Browser.prototype._notifyIncoming = function (message) {
    try {
        message = JSON.parse(message);
    } catch (error) {
        this.emit('error', error);
        return;
    }
    // Assume the helper has already provided the correct type.
    this.emit("message", message);
};
