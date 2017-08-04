// Below is the copyright agreement for the Ptolemy II system.
//
// Copyright (c) 2015-2017 The Regents of the University of California.
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
 * Implementation for Nashorn and CapeCode of the module supporting accessor
 * interaction through a user interface. The Server object starts
 * a server that accepts web socket connections and HTTP GET and POST requests.
 *
 * This module defines one class, UserInterface.  After constructing an instance of
 * UserInterface (using new), you can request that the user interface display HTML that
 * you provide using the display() function. Whenever display() is called,
 * whatever was previously displayed is replaced with your content in the user interface.
 * 
 * You can also specify resources that the HTML references (such as images)
 * by using the addResource() function. You should call this before providing
 * HTML that requires those resources.
 * Note that updating a resource with the same name will not normally result
 * in the web page being updated because user interfaces normally cache such resources.
 * If HTML content refers to a resource that has already been loaded (or more
 * precisely, that has the same name as a resource that has already been loaded),
 * then the user interface will not load the resource again, but rather will use the
 * previous version.  You can force the user interface to reload a resource by augmenting
 * the name with parameters (which will be ignored). For example, if you have
 * a resource named "image.jpg" that you wish to update it, then you can
 * specify HTML like this:
 * 
 *     &lt;img src="image.jpg?count=n"/&gt;
 * 
 * where **n** is a unique integer not previously seen by the user interface.
 * This will force the user interface to go back to the server to retrieve the resource.
 * 
 * You can also add listeners for data sent by the user interface using POST using
 * the addListener() function. So your HTML can include forms and buttons,
 * for example, that will POST JSON data.
 * 
 * You can specify optional header content when you construct the UserInterface object.
 * This is a good place to include scripts.  The HTML content provided to
 * UserInterface.display() can also include scripts. Those scripts can invoke functions
 * defined in the header or reference variables defined in the header.
 * 
 * Scripts in the header can invoke a require(**module**) function to use any
 * module supported by the browser host.  The 'util.js' module is automatically
 * included, so there is no need to explicitly require it.  For example, to use
 * the web-socket-client module, your script could include:
 * 
 *   var ws = require('web-socket-client.js');
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
 * The websocket can also accept incoming JSON data, in which case the UserInterface
 * object will emit a 'message' event.
 * 
 * @module userInterface
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

/** Constructor for a UserInterface server.
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
 *     var browser = new require('browser').UserInterface({'port':8082});
 *     browser.display('<h1>Hello</h1>');
 *     setTimeout(function() {
 *        browser.display('<h1>World!</h1>');
 *     }, 5000);
 *     browser.stop();
 *  </pre>
 *  
 *  This displays "Hello" and then changes it to "World!" after 5 seconds.
 *  The UserInterface is an event emitter, emitting events 'listening' and 'connection'.
 *  
 *  @param options The port and hostInterface options.
 *  @param header HTML to include in the page header.
 *  @param content HTML content to include in the page.
 */
exports.UserInterface = function (options, header, content) {
    this.server = null;
    this.count = 0;
    this.pendingSends = [];
    this.listeners = {};
    this.header = '';
    this.listening = false;
    if (header) {
        this.header = header;
    }
    this.content = '';
    if (content) {
        this.content = content;
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
    var self = this;
    this.on('listening', function() {
        this.listening = true;
        // If there is content to display already, then open the page.
        // Otherwise, wait until there is content.
        if (self.content) {
            createTemplatePage.call(self);
            self.open = true;
        }        
    });
    this.server.startServer();
    
    this.browserLauncher = Java.type('ptolemy.actor.gui.BrowserLauncher');
};
util.inherits(exports.UserInterface, EventEmitter);

/** Add a listener for data posted on a specific path. Only one
 *  listener can be active for a given path. The last-added listener
 *  will be used.
 *  @param path The path to listen for POST requests on.
 *  @param listener A callback function with one argument.
 */
exports.UserInterface.prototype.addListener = function (path, listener) {
    this.listeners[path] = listener;
};

/** Add a resource to be served by the server.
 *  @param path The path to the resource.
 *  @param resource The resource to serve.
 *  @param contentType The content type of the resource.
 */
exports.UserInterface.prototype.addResource = function (path, resource, contentType) {
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
exports.UserInterface.prototype.post = function (path, data) {
  if (this.listeners[path]) {
    this.listeners[path].apply(null, [data]);
  } else {
    console.log("No listener registered for path: " + path);
  }
}

/** Display the specified HTML text.
 *  This replaces the main body of the page.
 *  @param html The HTML to display.
 */
exports.UserInterface.prototype.display = function (html) {
    var self = this;
    if (!this.open) {
        // Make sure the initial HTML is put in right from the start in case
        // the header or content includes onload callbacks.
        this.content = html;
        if (this.listening) {
            createTemplatePage.call(this);
            this.open = true;
        } else {
            this.on('listening', function() {
                createTemplatePage.call(self);
                self.open = true;
            });
        }
        // No need to send the HTML over the websocket.
        return;
    }
    // If there is a websocket connection, send the HTML over the
    // websocket. Otherwise, save the HTML to send it when the socket is
    // opened.
    var toSend = html.toString();
    // The string we send may have a prefix of the form <<id<<type<<
    // (see update() below), so if the html starts with '<<' we escape it.
    // Normally, it will not start that way since that is invalid HTML.
    if (toSend.startsWith('<<')) {
        toSend = toSend.replace('<<', 'REMOVEME<<');
    }
    if (this.helper && this.helper.isOpen()) {
        this.helper.send(toSend);
    } else {
        this.pendingSends.push(toSend);
    }
};

/** Stop the server. Note that this closing happens
 *  asynchronously. The server may not be closed when this returns.
 */
exports.UserInterface.prototype.stop = function () {
    if (this.helper && this.helper.isOpen()) {
        this.helper.close();
    }
    if (this.server !== null) {
        this.server.closeServer();
        this.server = null;
    }
    this.listening = false;
};

/** Update a DOM object with the specified ID.
 *  @param id The ID.
 *  @param property The type of the update. If this is "html", then the
 *   DOM object is updated by invoking the jQuery html() function it
 *   with the specified content as an argument. Otherwise, the property
 *   with name *property* is assigned the value of the content.
 *   If *property* is 'src', then in addition, the content is augmented
 *   with a suffix of the form '?count=*n*', where *n* is a unique number.
 *   This is so that the browser will be forced to reload the src rather than
 *   using any cached version it may have. This can be used, for example,
 *   to force an update to an img tag where a new image has been provided
 *   using addResource().
 *  @param content The content of the update, typically HTML to insert or
 *   a property value like src to set.
 */
exports.UserInterface.prototype.update = function(id, property, content) {
    if (property.equals('src')) {
        content += '?count=' + (this.count++);
    }
    var toSend = '<<' + id + '<<' + property + '<<' + content;
    if (this.helper && this.helper.isOpen()) {
        this.helper.send(toSend);
    } else {
        this.pendingSends.push(toSend);
    }
}

/** Notify that a handshake was successful and a websocket has been created.
 *  This is called by the helper class is not meant to be called by the JavaScript
 *  programmer. This server supports only one socket connection at a time, so if
 *  there already is one open, it first closes it.
 *  @param serverWebSocket The Java ServerWebSocket object.
 *  @param helper The helper in charge of this socket.
 */
exports.UserInterface.prototype._socketCreated = function (serverWebSocket, helper) {
    if (this.helper && this.helper.isOpen()) {
        this.helper.close();
    }
    this.helper = WebSocketHelper.createServerSocket(actor,
            this, serverWebSocket, helper, 'application/json', 'text/html');
    if (this.pendingSends.length > 0) {
        for (var i = 0; i < this.pendingSends.length; i++) {
            this.helper.send(this.pendingSends[i]);
        }
        this.pendingSends = [];
    }
};

/** Notify this object of a received message from the socket.
 *  This function attempts to parse the message as JSON and then
 *  emits a "message" event with the message as an argument.
 *  This function is called by the helper and should not be called
 *  by the user of this module.
 *  @param message The incoming message.
 */
exports.UserInterface.prototype._notifyIncoming = function (message) {
    try {
        message = JSON.parse(message);
    } catch (error) {
        this.emit('error', error);
        return;
    }
    // Assume the helper has already provided the correct type.
    this.emit("message", message);
};

/** Create the template page, which pulls in jquery, defines the
 *  require() function, and includes the util module. It uses the
 *  browsers WebSocket to open a socket to the server to listen
 *  for updates to the content. And it constructs the default page
 *  using the specified header and content.
 *  Finally, it launches the browser.
 */
function createTemplatePage() {
    // Create the template page.
    // This uses jquery because of its html() function, which unlike
    // just setting innerHTML, evaluates any scripts that might be contained.
    // NOTE: Using jquery installed in the browser host. Could use a more modern version, e.g.
    // https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js
    // But should use a local copy.
    // This assumes the browser host is installed here.
    var script = '<script src="/accessors/hosts/browser/modules/jquery.js"></script>\n\
        <script src="/accessors/hosts/browser/modules/require.js"></script>\n\
        <script>\n\
        var util = require("util.js");\n\
        if ("WebSocket" in window) {\n\
            var socket = new WebSocket("ws://localhost:'
        + this.port
        + '");\n\
            // Listen for new HTML content\n\
            socket.onmessage = function(event) {\n\
                var fr = new FileReader();\n\
                fr.onloadend = function() {\n\
                    var result = fr.result;\n\
                    if (result.startsWith("<<")) {\n\
                        // Received an update instead of HTML\n\
                        var split = result.split("<<");\n\
                        // NOTE: first item is an empty string.\n\
                        var id = split[1];\n\
                        var property = split[2];\n\
                        var content = split[3];\n\
                        if (property == "html") {\n\
                            // Use jQuery html function here so scripts in content are evaluated.\n\
                            $("#" + id).html(content);\n\
                        } else {\n\
                            var element = document.getElementById(id);\n\
                            var dom = element[property];\n\
                            element[property] = content;\n\
                        }\n\
                    } else {\n\
                        if (result.startsWith("REMOVEME<<")) {\n\
                            result = result.replace("REMOVEME<<", "<<");\n\
                        }\n\
                        $("body").html(result);\n\
                    }\n\
                };\n\
                fr.readAsText(event.data);\n\
            };\n\
        } else {\n\
            document.getElementById("result").innerHTML = "Your browser does not support websockets.";\n\
        }\n\
        // To ensure that the current page is unchanged after a POST, override\n\
        // the default submit behavior for any form.\n\
        jQuery(document).ready(function() {\n\
            var form = jQuery("form");\n\
            form.submit(function() {\n\
                var data = jQuery(this).serialize();\n\
                jQuery.post("/", data);\n\
                return false; // Prevent default handler from going to a new page.\n\
            });\n\
        });\n\
        </script>\n';
    var template = '<!DOCTYPE html><html><head><meta charset="UTF-8"><title>UserInterface Swarmlet Interface</title>'
            + script
            + this.header
            + '</head><body>'
            + this.content
            + '</body></html>'
    this.server.setResponse(template);
    this.browserLauncher.openURL('http://localhost:' + this.port);
}
