// JavaScript functions for a Ptolemy II (Nashorn) accessor host.
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

/** JavaScript functions for the CapeCode host, which is based on
 *  Ptolemy II and Java's Nashorn JavaScript engine.
 *  This host supports version 1 accessors.
 *  To implement this host, first load the nashornHost.js file,
 *  which realizes functions that are independent of Ptolemy II.
 *  Then load this one, which overrides some of those function
 *  definitions.
 *
 *  @module capeCodeHost
 *  @author Edward A. Lee, Contributor: Christopher Brooks
 *  @version $$Id$$
 *  @since Ptolemy II 11.0
 */

// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals Java, actor, commonHost, getAccessorCode, load */
/*jshint globalstrict: true */
/*jslint nomen: true */
"use strict";

////////////////////////////////////////////////////////////////////////////
////Java dependencies.

//Java classes that define some static functions to call from JS.
var FileUtilities = Java.type('ptolemy.util.FileUtilities');

//////// NOTE: The following function overrides nashornHost.js.

/** Pop up a dialog with the specified message.
 *  NOTE: This function is not required by the accessor specification, so accessors
 *  should not rely on it being present.
 *  @param message The message
 */
function alert(message) {
    var MessageHandler = Java.type('ptolemy.util.MessageHandler');
    MessageHandler.message(message);
}

////////NOTE: The following function overrides nashornHost.js.

/** Report an error. This implementation delegates to the host actor to
 *  report the error. In this implementation, the host actor has an error output
 *  port. If that port is connected to something, then the error message is sent
 *  to that port. Otherwise, the error is reported via a dialog or on stderr (the
 *  latter if running headless).
 *  This function should be used only for non-fatal errors, where it is OK to
 *  continue executing. For fatal errors, throw an exception.
 *  @param message The message for the exception.
 */
function error(message) {
    // Print a stack trace to the console.
    /*
    console.error(message);
    console.error('------------------------- error stack trace:');
    var e = new Error('dummy');
    var stack = e.stack.replace(/^[^\(]+?[\n$]/gm, '')
            .replace(/^\s+at\s+/gm, '')
            .replace(/^Object.<anonymous>\s*\(/gm, '{anonymous}()@')
            .split('\n');
    console.error(stack);
    console.error('-------------------------');
    */

    actor.error(message);
}

/** Get a resource, which may be a relative file name or a URL, and return the
 *  value of the resource as a string.
 *  Implementations of this function may restrict the locations from which
 *  resources can be retrieved. This implementation restricts relative file
 *  names to be in the same directory where the swarmlet model is located or
 *  in a subdirectory, or if the resource begins with "$CLASSPATH/", to the
 *  classpath of the current Java process.
 *  @param uri A specification for the resource.
 *  @param timeout The timeout in milliseconds.
 */
function getResource(uri, timeout) {
    return actor.getResource(uri, timeout);
}

/////////// NOTE: The following function is deprecated, but provided for compatibility

/** Perform a blocking HTTP request.
 *  @param url The url for the request, method, properties, body, timeout) {
 *  @param method the request method for the url connection.
 *  @param properties An array of properties for the connection.
 *  @param body If non-empty, the body to be written to the output
 *  stream of the connection.
 *  @param timeout If specified, the connect and read timeout in milliseconds.
 *
 *  @deprecated: Use the http module instead, which provides non-blocking requests.
 */
function httpRequest(url, method, properties, body, timeout) {
    var theURL = new(Java.type('java.net.URL'))(url),
        protocol = theURL.getProtocol().toLowerCase(),
        connection,
        key,
        writer;

    if (debug) {
        console.log("httpRequest(" + (function (obj) {
            var result = [],
                p;
            for (p in obj) {
                if (obj.hasOwnProperty(p)) {
                    result.push(JSON.stringify(obj[p]));
                }
            }
            return result;
        })(arguments) + ")");
    }
    if (actor.isRestricted &&
        !(protocol.equals("http") ||
            protocol.equals("https"))) {
        throw "Actor is restricted. Only HTTP(S) requests will be honored by httpRequest().";
    }
    connection = theURL.openConnection();

    for (key in properties) {
        if (properties.hasOwnProperty(key)) {
            connection.setRequestProperty(key, properties[key]);
        }
    }

    connection.setRequestMethod(method);

    // If a timeout has been specified, set it.
    if (timeout && timeout >= 0) {
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
    }

    // Send body if applicable.
    if (body && !body.equals('')) {
        connection.setDoOutput(true);
        writer = new(Java.type('java.io.OutputStreamWriter'))(connection
            .getOutputStream());
        writer.write(body);
        writer.flush();
        writer.close();
    }

    // Wait for response.
    return FileUtilities.readFromInputStream(
        connection.getInputStream()
    );
}

/////////// NOTE: The following function is deprecated, but provided for compatibility

// Print a message to the console.
// print is built in to Nashorn, but is not required by the accessor specification,
// so accessors should not rely on it being present.

/** Synchronously read a URL.
 *  @param url The url.
 *  @param timeout The timeout in milliseconds
 *
 *  @deprecated: Use the http module, which provides non-blocking functions.
 */
function readURL(url, timeout) {
    if (!timeout) {
        timeout = 3000;
    }
    if (debug) {
        console.log("readURL('" + url + "')");
    }
    var theURL = new(Java.type('java.net.URL'))(url),
        request,
        response;
    if (actor.isRestricted &&
        !theURL.getProtocol().toLowerCase().equals("http") &&
        !theURL.getProtocol().toLowerCase().equals("https")) {
        throw "Actor is restricted. Only HTTP and HTTPS requests will be honored by readURL().";
    }
    request = new(Java.type('org.ptolemy.ptango.lib.HttpRequest'))();
    request.setUrl(theURL);
    request.setTimeout(timeout); // In milliseconds.
    response = request.execute();
    if (!response.isSuccessful()) {
        throw "Failed to read URL: " + url + "\nResponse code: " +
            response.getResponseCode() + "\nResponse message: " +
            response.getResponseMessage();
    }
    return response.getBody();
}
