// Copyright (c) 2016-2016 The Regents of the University of California.
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

/**
 * Module supporting web servers.
 * This module defines one class, HttpServer, which runs a web server on the 
 * given port and host interface (e.g. localhost).  
 * 
 * HttpServer generates two events:
 * A listening event after the server has been started and is ready, and
 * Request events for each incoming request.  The request event includes
 * the request data plus a requestID number.
 * 
 * Accessors should provide a complete response back to HttpServer and include
 * the matching requestID.  
 *
 * @module httpServer
 * @author Edward A. Lee amd Elizabeth Osyk
 */

// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals exports, Java, require, util */
/*jslint nomen: true */
/*jshint globalstrict: true */
"use strict";

//Stop extra messages from jslint.  Note that there should be no
//space between the / and the * and global.
/*globals actor */

var HttpServerHelper = Java.type('ptolemy.actor.lib.jjs.modules.httpServer.HttpServerHelper');
var EventEmitter = require('events').EventEmitter;

///////////////////////////////////////////////////////////////////////////////
//// HttpServer

/** Construct an instance of HttpServer.
 *  After invoking this constructor (using new), the user script should set up listeners
 *  and then invoke the start() function on this HttpServer.
 *  This will create an HTTP server on the local host.
 *
 *  The options argument is a JSON object containing the following optional fields:
 *  * **hostInterface**: The IP address or name of the local interface for the server
 *    to listen on.  This defaults to "localhost", but if the host machine has more
 *    than one network interface, e.g. an Ethernet and WiFi interface, then you may
 *    need to specifically specify the IP address of that interface here.
 *  * **port**: The port on which to listen for requests (the default is 80,
 *    which is the default HTTP port).
 *  * **timeout**: The time in milliseconds to wait after emitting a request
 *    event for a response to be provided by invoking the respond() function.
 *   This is a long that defaults to 10,000.
 *   If this time expires before respond() is invoked, then this module
 *   will issue a generic timeout response to the HTTP request.
 *
 *
 *  This subclasses EventEmitter, emitting events:
 *  * **listening**: Emitted when the server is listening.
 *  * **request**: Emitted when an HTTP request has been received.
 *
 *	A request event contains an object with fields for the requestID, method, 
 *  path, and body (if any).  If there is no body, the body field is absent.
 *
 *  A typical usage pattern looks like this:
 *
 *     var httpServer = require('httpServer');
 *     var server = new httpServer.HttpServer({'port':8082});
 *     server.on('listening', function () {
 *         console.log('Server is listening.');
 *     });
 *     server.on('request', function (request) {
 *         console.log('Server received request: ' + util.inspect(request));
 *         server.respond(request.requestID, 'Hello World');
 *     });
 *     server.start();
 *
 *  where onListening is a handler for an event that this HttpServer emits
 *  when it is listening for requests.
 *
 *  @param options The options.
 */
exports.HttpServer = function (options) {
    if (options.port === undefined || options.port === null) {
        this.port = 80;
    } else {
        this.port = options.port;
    }
    this.hostInterface = options.hostInterface || 'localhost';
    this.helper = HttpServerHelper.getOrCreateServer(
        actor,
        this,
        this.hostInterface,
        this.port
    );
};
util.inherits(exports.HttpServer, EventEmitter);

/** Respond to a request. The provided response will be matched to the request 
 *  with the specified requestID.
 *  @param requestID An object that uniquely identifies the request.
 *   This should be the value of the requestID property of the object
 *   that was emitted as a 'request' event.
 *  @param response A complete text/html response.
 */
exports.HttpServer.prototype.respond = function (requestID, response, responseCode) {
	if (responseCode !== null && typeof responseCode !== 'undefined') {
		this.helper.respond(requestID, response, responseCode);
	} else {
		this.helper.respond(requestID, response, 200);
	}
    
};

/** Start the server. */
exports.HttpServer.prototype.start = function () {
    this.helper.startServer();
};

/** Stop the server. Note that this closing happens
 *  asynchronously. The server may not be closed when this returns.
 */
exports.HttpServer.prototype.stop = function () {
    this.helper.closeServer();
};

/** Notify that a request has come in.
 *  This is called by the helper class is not meant to be called by the JavaScript
 *  programmer. When this is called, the HttpServer will FIXME.
 *  @param requestID An object that uniquely identifies the request.
 *   This object must be passed back to the respond() function within the timeout
 *   specified in the options when this HttpServer object was created, or else
 *   a default response will be issued.
 *  @param method The HTTP method of the request.
 *  @param path The path of the request.
 *  @param body The body of the request, if any.
 */
exports.HttpServer.prototype._request = 
	function (requestID, method, path, body, headers, params) {
	var headersObject = {};
	var paramsObject = {};
	// headers is an array of name=value.
	if (headers !== null && typeof headers !== 'undefined') {
		for (var i = 0; i < headers.length; i++) {
			var header = headers[i].toString();
			var sign = header.indexOf('=');
                        // http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html
                        // section 4.2 says header keys are
                        // case-insensitive and Node returns lower
                        // case header keys, so CapeCode should as
                        // well.
			if (sign > 0) {
		            headersObject[header.substring(0, sign).toLowerCase()] = 
			    	header.substring(sign+1, headers[i].length);

			}
		}
	}
	
	// params is an array of name : value.
	if (params !== null && typeof params !== 'undefined') {
		for (var i = 0; i < params.length; i++) {
			var param = params[i].toString();
			var sign = param.indexOf(':');
			if (sign > 0) {
				paramsObject[param.substring(0, sign)] = 
					param.substring(sign+2, params[i].length);
			}
		}
	}
	
    // If you change what fields are in request, then change
    // $PTII/org/terraswarm/accessor/accessors/web/hosts/node/node_modules/@accessors-modules/http-server/http-server.js

    var request = {
    	'headers' : headersObject,	
        'requestID': requestID,
        'method': method,
        'path': path,
        'params' : paramsObject
    };
    
    if (body !== null) {
    	request.body = body;
    	
    	// Handle images.
    	if (request.headers['Content-Type'] !== null && 
    			typeof request.headers['Content-Type'] !== 'undefined') {
    		if (request.headers['Content-Type'].indexOf('image') > -1) {
    			var newBody = this.helper.convertImageBody(body);
    			request.body = newBody;
    		}
    	} else if (request.headers['content-type'] !== null && 
    		   typeof request.headers['content-type'] !== 'undefined') {
    		if (request.headers['content-type'].indexOf('image') > -1) {
    			var newBody = this.helper.convertImageBody(body);
    			request.body = newBody;
    		}
        }
    }
    this.emit('request', request);
};
