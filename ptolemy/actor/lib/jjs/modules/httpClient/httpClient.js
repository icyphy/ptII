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
 * Module for HTTP clients.
 * A simple use of this module is to request a web page and print its contents, as
 * illustrated by the following example:
 * <pre>
 *    var httpClient = require('@accessors-modules/http-client');
 *    httpClient.get('http://accessors.org', function(message) {
 *        print(message.body);
 *    });
 * </pre>
 * Both http and https are supported.
 *
 * @module httpClient
 * @author Marten Lohstroh and Edward A. Lee
 * @version $$Id$$
 */

// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals Java, actor, error, exports, IncomingMessage, require, util */

// FIXME: Setting "use strict" causes a warning about the IncomingMessage function declaration being Read Only
// and then opening the camera library fails.  The error is:
//   Error: Error executing module net/REST line #237 : Error executing module httpClient line #356 : "IncomingMessage" is not defined
//   In file: /Users/cxh/ptII/ptolemy/actor/lib/jjs/modules/httpClient/httpClient.js
// "use strict";

// Java types used.
var HttpClientHelper = Java.type('ptolemy.actor.lib.jjs.modules.httpClient.HttpClientHelper');
var URL = Java.type('java.net.URL'); // FIXME: eventually, have a url module for this

var EventEmitter = require('events').EventEmitter;

/** Issue an HTTP request and provide a callback function for responses.
 *  The callback is a function that is passed an instance of IncomingMessage,
 *  defined here. This function returns an instance of ClientRequest, also defined here.
 *  The HTTP request will not actually be issued until you call the end() function on
 *  the returned ClientRequest.
 *
 *  This implementation ensures that for any accessor that calls this function,
 *  the callback functions are called in the same order as
 *  invocations of this request() function that triggered the request.
 *  If you call this function from the same accessor before the previous
 *  request has been completed (the callback function has been called or it has
 *  timed out), then the request will be queued to be issued only after the previous
 *  request has been satisfied.
 *
 *  The options argument can be a string URL
 *  or a map with the following fields (this helper class assumes
 *  all fields are present, so please be sure they are):
 *  <ul>
 *  <li> body: The request body, if any.  This supports at least strings and image data.
 *  <li> headers: An object containing request headers. By default this
 *       is an empty object. Items may have a value that is an array of values,
 *       for headers with more than one value.
 *  <li> keepAlive: A boolean that specified whether to keep sockets around
 *       in a pool to be used by other requests in the future. This defaults to false.
 *  <li> method: A string specifying the HTTP request method.
 *       This defaults to 'GET', but can also be 'PUT', 'POST', 'DELETE', etc.
 *  <li> outputCompleteResponseOnly: If false, then the multiple invocations of the
 *       callback may be invoked for each request. This defaults to true, in which case
 *       there will be only one invocation of the callback.
 *  <li> timeout: The amount of time (in milliseconds) to wait for a response
 *       before triggering a null response and an error. This defaults to 5000.
 *  <li> url: A string that can be parsed as a URL, or an object containing
 *       the following fields:
 *       <ul>
 *       <li> host: A string giving the domain name or IP address of
 *            the server to issue the request to. This defaults to 'localhost'.
 *       <li> path: Request path as a string. This defaults to '/'. This can
 *            include a query string, e.g. '/index.html?page=12', or the query
 *            string can be specified as a separate field (see below).
 *            An exception is thrown if the request path contains illegal characters.
 *       <li> protocol: The protocol. This is a string that defaults to 'http'.
 *       <li> port: Port of remote server. This defaults to 80.
 *       <li> query: A query string to be appended to the path, such as '?page=12'.
 *       </ul>
 *  </ul>
 *  @param options The options or URL.
 *  @param responseCallback The callback function to call with an instance of IncomingMessage,
 *   or with a null argument to signal an error.
 *  @return An instance of ClientRequest.
 */
exports.request = function (options, responseCallback) {
    return new ClientRequest(options, responseCallback);
};

// NOTE: Perhaps options should include a localAddress: A string giving a name or
// IP address for the local network interface to use
// for network connections. This defaults to 'localhost', but on machines with more than one
//  network interface (e.g. WiFi and Ethernet), you may need to specify which one to use.
// Marten, 05/21/2015: I'm not sure if I understand the problem (or solution) here.
// Depending on the IP address, the host's routing table will
// determine which device will be used. The name 'localhost' does not signify a
// network interface but a hostname.

// NOTE: Node has keepAliveMsecs, but I don't see anything like it in Vert.x
// When using HTTP KeepAlive, this is an integer that specifies
// how often (in milliseconds) to send a TCP KeepAlive packet over sockets being kept alive.
// This defaults 1000 and is only relevant if keepAlive is set to true.

// NOTE: The following options are supported by http.request() in Node.js, but not here,
// or at least not tested (yet):
// hostname: To support url.parse() hostname is preferred over host
// socketPath: Unix Domain Socket (use one of host:port or socketPath)
// auth: Basic authentication i.e. 'user:password' to compute an Authorization header.
// agent: Controls Agent behavior. When an Agent is used request will default to Connection: keep-alive. Possible values:
// undefined (default): use global Agent for this host and port.
// Agent object: explicitly use the passed in Agent.
// false: opts out of connection pooling with an Agent, defaults request to Connection: close.

// NOTE: This interface is attempting to follow principles in this module:
// https://github.com/request/request

/** Convenience method to issue an HTTP GET.  This just calls request() and then
 *  calls end() on the object returned by request(). It returns the object returned
 *  by request() (an instance of ClientRequest). See request() for documentation of
 *  the arguments.
 *
 *  This implementation ensures that for any accessor that calls this function,
 *  the callback functions are called in the same order as
 *  invocations of this request() function that triggered the request.
 *  If you call this function from the same accessor before the previous
 *  request has been completed (the callback function has been called or it has
 *  timed out), then the request will be queued to be issued only after the previous
 *  request has been satisfied.
 *
 *  @param options The options.
 *  @param responseCallback The callback function to call with an instance of IncomingMessage,
 *   or with a null argument to signal an error.
 */
exports.get = function (options, responseCallback) {
    var request = exports.request(options, responseCallback);
    request.end();
    return request;
};

/** Convenience method to issue an HTTP POST.  This just calls request() and then
 *  calls end() on the object returned by request(). It returns the object returned
 *  by request() (an instance of ClientRequest). See request() for documentation of
 *  the arguments.
 *
 *  This implementation ensures that for any accessor that calls this function,
 *  the callback functions are called in the same order as
 *  invocations of this request() function that triggered the request.
 *  If you call this function from the same accessor before the previous
 *  request has been completed (the callback function has been called or it has
 *  timed out), then the request will be queued to be issued only after the previous
 *  request has been satisfied.
 *
 *  @param options The options.
 *  @param responseCallback The callback function to call with an instance of IncomingMessage,
 *   or with a null argument to signal an error.
 */
exports.post = function (options, responseCallback) {
    options.method = "POST";
    var request = exports.request(options, responseCallback);
    request.end();
    return request;
};

/** Convenience method to issue an HTTP PUT.  This just calls request() and then
 *  calls end() on the object returned by request(). It returns the object returned
 *  by request() (an instance of ClientRequest). See request() for documentation of
 *  the arguments.
 *
 *  This implementation ensures that for any accessor that calls this function,
 *  the callback functions are called in the same order as
 *  invocations of this request() function that triggered the request.
 *  If you call this function from the same accessor before the previous
 *  request has been completed (the callback function has been called or it has
 *  timed out), then the request will be queued to be issued only after the previous
 *  request has been satisfied.
 *
 *  @param options The options.
 *  @param responseCallback The callback function to call with an instance of IncomingMessage,
 *   or with a null argument to signal an error.
 */
exports.put = function (options, responseCallback) {
    options.method = "PUT";
    var request = exports.request(options, responseCallback);
    request.end();
    return request;
};

// NOTE: The following events are produced by ClientRequest in Node.js
// From: http.ClientRequest
// Event: 'response'
// Event: 'socket'
// Event: 'connect'
// Event: 'upgrade'
// Event: 'continue'
// From stream.Writeable
// Event: 'finish'
// Event: 'pipe'
// Event: 'unpipe'
// Event: 'error'

/** Constructor for the object type returned by the request() function.
 *  This object type provides the following functions:
 *  <ul>
 *  <li> end(): Call this to end the request. </li>
 *  <li> write(''data'', ''encoding''): Write data (e.g. for a POST request). </li>
 *  </ul>
 *  The request will not be issued until you call end().
 *  See the documentation of the request function for an explanation of the arguments.
 *  This is an event emitter that emits the following events:
 *  <ul>
 *  <li> 'error': If an error occurs. The message is passed as an argument. </li>
 *  <li> 'response': A response is received from the server. This event is automatically
 *       handled by calling responseCallback, if responseCallback is not null.</li>
 *  </ul>
 *  @constructor
 *  @param options The options.
 *  @param responseCallback The callback function to call with an instance of IncomingMessage,
 *   or with a null argument to signal an error.
 */
function ClientRequest(options, responseCallback) {
    var self = this;

    var defaultOptions = {
        'headers': {},
        'keepAlive': false,
        'method': 'GET',
        'outputCompleteResponseOnly': true,
        'timeout': 5000,
        'trustAll': false,
    };
    var defaultURL = {
        'host': 'localhost',
        'path': '/',
        'port': 80,
        'protocol': 'http',
        'query': ''
    };

    var urlSpec;
    if (util.isString(options)) {
        urlSpec = options;
        options = {}; // If only URL is passed in, create new options object
    } else if (util.isString(options.url)) {
        urlSpec = options.url;
    }
    if (urlSpec) {
        var url = new URL(urlSpec);
        var port = url.getPort();
        if (port < 0) {
            port = url.getDefaultPort();
            if (port < 0) {
                port = 80;
            }
        }

        options.url = {
            'host': url.getHost(),
            'path': url.getPath(),
            'port': port,
            'protocol': url.getProtocol(),
            'query': url.getQuery()
        };
    } else {
        options.url = util._extend(defaultURL, options.url);
    }
    // Fill in default values.
    options = util._extend(defaultOptions, options);
    // Do not generate a query for null or undefined queries.
    if (options.url.query === null || typeof options.url.query === 'undefined') {
    	options.url.query = '';
    }

    // Attach the callback to be invoked when this object issues
    // a 'response' event.
    if (responseCallback) {
        if (options.outputCompleteResponseOnly) {
            self.once('response', responseCallback);
        } else {
            self.on('response', responseCallback);
        }
    }

    // Set the Content-Length header
    if (options.body !== null && options.body !== undefined) {
        var headers;
        if (typeof options.headers == "undefined") {
            headers = {};
        } else {
            headers = options.headers;
        }

        headers['Content-Length'] = options.body.length;
        options.headers = headers;
    }

    this.helper = HttpClientHelper.getOrCreateHelper(actor, this);
    this.options = options;
}
util.inherits(ClientRequest, EventEmitter);
exports.ClientRequest = ClientRequest;

/** Issue the request. */
ClientRequest.prototype.end = function () {
	console.log("Making an HTTP request.");
	// console.log("Making an HTTP request to " + util.inspect(this.options.url));
    this.helper.request(this, this.options);
};

/** Stop a response, if there is one active. This is useful if a streaming response
 *  needs to be stopped. The closes the socket connection.
 */
ClientRequest.prototype.stop = function () {
    if (this.helper) {
        this.helper.stop();
    }
};

/** Append the specified data to the body of the request.
 *  FIXME: Currently, this supports only strings and ignore the encoding.
 *  @param data The data to append.
 *  @param encoding The encoding of the data.
 */
ClientRequest.prototype.write = function (data, encoding) {
    if (this.options.body) {
        this.options.body = this.options.body + data;
    } else {
        this.options.body = data;
    }
};

/** Internal function used to handle an error.
 *  @param message The error message.
 */
ClientRequest.prototype._handleError = function (message) {
    // There may be no registered error event handler.
    try {
        this.emit('error', message);
    } catch (err) {
        error(message);
    }
};

/** Internal method used to handle a response. The argument is an
 *  an instance of the Java class org.vertx.java.core.http.HttpClientResponse.
 *  This method uses the data therein to construct an IncomingMessage object
 *  and pass that as an argument to the 'response' event of the ClientRequest.
 *  @param response The response from the server, or null to signal an error.
 *  @param body The body of the response, or an error message for an error.
 */
ClientRequest.prototype._response = function (response, body) {
    var message = new IncomingMessage(response, body);
    this.emit('response', message);

    if (typeof response.statusCode === 'function') {
        var code = response.statusCode();
        if (code >= 400) {
            // An error occurred. Emit both an error event and a response event.
            this._handleError('Received response code ' + code + ". " + response.statusMessage());
        }
    } else {
        this._handleError('Received incorrect response: ' + response);
    }
};

/** Internal method used to handle an error response. The argument is an
 *  an instance of the Java class org.vertx.java.core.http.HttpClientResponse.
 *  This method emits the response and then handles the error.
 *  @param response The response from the server, or null to not emit a response.
 *  @param body The body of the response, or an error message for an error.
 */
ClientRequest.prototype._errorResponse = function (response, body) {
    if (response !== null) {
    	var message = new IncomingMessage(response, body);
        this.emit('response', message);
    }
    this._handleError(body);
};

// NOTE: The following events are produce by IncomingMessage in Node.js
// From stream.Readable
// Event: 'readable'
// Event: 'data'
// Event: 'end'
// Event: 'close'
// Event: 'error'

/** Incoming message object type.  This should not be constructed by the user,
 *  but rather is constructed by the _response function above.
 *
 *  An instance of this object type will be passed to the callback passed to the
 *  request() or this.get() functions. The instance contains:
 *  <ul>
 *  <li> body: a string with the body of the response. </li>
 *  <li> cookies: an array of strings with cookies returned. </li>
 *  <li> headers: message header names and values. Names are lower case. </li>
 *  <li> statusCode: an integer indicating the status of the response. </li>
 *  <li> statusMessage: a string with the status message of the response. </li>
 *  </ul>
 *
 *  This object should match the interface of the Node 
 *  [http.IncomingMessage](https://nodejs.org/api/http.html#http_class_http_incomingmessage).
 *  Do not add fields here, otherwise the node host will fail.
 *
 *  @constructor
 *  @param response An instance of the Java class org.vertx.java.core.http.HttpClientResponse.
 */
// IncomingMessage = function(response, body) {
function IncomingMessage(response, body) {
    this.body = body;
    if (typeof response.cookies === 'function') {
        this.cookies = response.cookies();
    }
    if (typeof response.getHeader === 'function') {
        // headersMap is of type org.vertx.java.core.MultiMap
        var headersMap = response.headers();
        var headers = {};
        for each (var name in headersMap.names()) {
            // Vert.x header keys are in lowercase.
            headers[name.toLowerCase()] = headersMap.get(name);
        }
        this.headers = headers;
    } else {
        this.headers = '';
    }
    if (typeof response.statusCode === 'function') {
        this.statusCode = response.statusCode();
    } else {
        this.statusCode = 400;
    }
    if (typeof response.statusMessage === 'function') {
        this.statusMessage = response.statusMessage();
    } else {
        this.statusMessage = body;
    }
}

// Each time this file is reloaded, reset the helper for this actor.
// This will start the sequence numbers at zero and discard any corrupted state
// that may have resulted from exceptions.
// FIXME: This may not work, because the actor may be associated with some other Vertx helper.
HttpClientHelper.reset(actor);
