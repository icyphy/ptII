// JavaScript functions for a Ptolemy II (Nashorn) accessor host.
// This file includes basic utility functions assumed by version 0 accessors.
//
// Author: Edward A. Lee
// Copyright: http://terraswarm.org/accessors/copyright.txt
//
// Flag that will cause debug output to the console if set to true.
var _debug = false

////////////////////
// Pop up a dialog with the specified message.
function alert(message) {
    var MessageHandler = Java.type('ptolemy.util.MessageHandler');
    MessageHandler.message(message);
}

////////////////////
// Clear an interval timer with the specified handle. See setInterval().
function clearInterval(handle) {
    actor.clearTimeout(handle);
}

////////////////////
// Clear a timeout with the specified handle. See setTimeout().
function clearTimeout(handle) {
    actor.clearTimeout(handle);
}

////////////////////
// Method for handling an error. This just throws an exception.
// Deprecated: Just use throw(message).
function error(message) {
    throw message;
}

////////////////////
// Method for performing a blocking HTTP request.
// DEPRECATED: Use the http module instead.
function httpRequest(url, method, properties, body, timeout) {
	if (_debug) {
	    console.log("httpRequest("
	        + (function(obj) {
	            result=[];
	            for(p in obj) {
	                result.push(JSON.stringify(obj[p]));
	            };
	            return result;
	        })(arguments)
	        + ")");
	}
	var theURL = new (Java.type('java.net.URL'))(url);
	if (actor.isRestricted && !theURL.getProtocol().toLowerCase().equals("http")) {
        throw "Actor is restricted. Only HTTP requests will be honored by httpRequest().";
    }
    var connection = theURL.openConnection();
    
    for (key in properties) {
        connection.setRequestProperty(key, properties[key]);
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
        var writer = new (Java.type('java.io.OutputStreamWriter'))(connection.getOutputStream());
        writer.write(body);
        writer.flush();
        writer.close();
    }

    // Wait for response.
    return Java.type('ptolemy.actor.lib.jjs.JavaScript').readFromInputStream(
            connection.getInputStream());
}

////////////////////
// Print a message to the console.
// print is built in to Nashorn.

////////////////////
// Method for synchronously reading a URL.
// DEPRECATED: Use the http module instead.
function readURL(url) {
	if (_debug) console.log("readURL('" + url + "')");
	var theURL = new (Java.type('java.net.URL'))(url);
	if (actor.isRestricted && !theURL.getProtocol().toLowerCase().equals("http")) {
        throw "Actor is restricted. Only HTTP requests will be honored by readURL().";
    }
	var request = new (Java.type('org.ptolemy.ptango.lib.HttpRequest'))();
	request.setUrl(theURL);
	var response = request.execute();
	if (!response.isSuccessful()) {
        throw "Failed to read URL: " + url
                + "\nResponse code: " + response.getResponseCode()
                + "\nResponse message: "
                + response.getResponseMessage();
    }
    return response.getBody();
}

////////////////////
// A string giving the full path to the root directory for installed modules.
var moduleRoot = Java.type('ptolemy.util.FileUtilities').nameToFile(
    '$CLASSPATH/ptolemy/actor/lib/jjs/', null)
    .getAbsolutePath();

////////////////////
// Require the named module. This function imports modules formatted
// according to the CommonJS standard.
// 
// If the name begins with './' or '/', then it is assumed
// to specify a file or directory on the local disk. If it is a file, the '.js' suffix
// may be optionally omitted. If it is a directory, then this function will look for
// a package.json file in that directory and load the file specified by the 'main'
// property the JSON object defined in that file. If there is no package.json file, then
// it will load an 'index.js' file, if there is one.
//
// If the name does not begin with './' or '/', then it is assumed to specify
// a module installed in this accessor host.
//
// In both cases, this function returns an object that includes as properties any
// properties that have been added to the 'exports' property. For example, to export
// a function, the module JavaScript file could define the function as follows:
//
//   exports.myFunction = function() {...};
//
// Alternatively, the module JavaScript file can explicitly define the exports object
// as follows:
//
//   var myFunction = function() {...};
//   module.exports = {
//       myFunction : myFunction
//   };
//
// This implementation uses the requires() function implemented by Walter Higgins,
// found here: https://github.com/walterhiggins/commonjs-modules-javax-script.
// See also: http://nodejs.org/api/modules.html#modules_the_module_object
// See also: http://wiki.commonjs.org/wiki/Modules
//
var require = load(moduleRoot + '/external/require.js')(
    // Invoke the function returned by 'load' immediately with the following arguments.
    //    - a root directory in which to look for modules.
    //    - an array of paths in which to look for modules.
    //    - an optional hook object that includes two callback functions for notification.
    moduleRoot,
    [ moduleRoot + '/', moduleRoot + '/modules/' , moduleRoot + '/node/' ]
);

////////////////////
// Pull in the util and console modules.
var util = require('util');
var console = require('console');

////////////////////
// Use a single Timer object for all timeout functions
// (since they all have to execute in the same thread anyway).
var _timer;

////////////////////
// Set a timeout to call the specified function after the specified time and
// repeatedly at multiples of that time.
// Return a handle to use in clearInterval(). If there are additional arguments
// beyond the first two, then those arguments will be passed to the function
// when it is invoked. This implementation uses fireAt() of the director
// in charge of the host JavaScript actor in Ptolemy II. Hence, actors
// that use this should be used with a director that respects fireAt(), such as DE.
// If the director has synchronizeToRealTime set to true, then it will approximate
// real-time behavior reasonably closely. Otherwise, the timeout will only be
// simulated. Either way, the timing is much more precise and well-defined than
// usual for JavaScript environments. If two actors specify the same timeout
// time in, say, their initialize() function, then they will be invoked at the
// same model time, and their outputs will be simultaneous. Any downstream actor
// will see them simultaneously.
// Note with this implementation, it is not necessary to
// call clearInterval() in the actor's wrapup() function.
// Nevertheless, it is a good idea to do that in an accessor
// since other accessor hosts may not work the same way.
function setInterval(func, milliseconds) {
    var callback = func;
    // If there are arguments to the callback, create a new function.
    // Get an array of arguments excluding the first two.
    var tail = Array.prototype.slice.call(arguments, 2);
    if (tail.length !== 0) {
        callback = function() {
            func.apply(this, tail);
        };
    }
    var id = actor.setInterval(callback, milliseconds);
    return id;
}

////////////////////
// Set a timeout to call the specified function after the specified time.
// Return a handle to use in clearTimeout(). If there are additional arguments
// beyond the first two, then those arguments will be passed to the function
// when it is invoked. This implementation uses fireAt() of the director
// in charge of the host JavaScript actor in Ptolemy II. Hence, actors
// that use this should be used with a director that respects fireAt(), such as DE.
// If the director has synchronizeToRealTime set to true, then it will approximate
// real-time behavior reasonably closely. Otherwise, the timeout will only be
// simulated. Either way, the timing is much more precise and well-defined than
// usual for JavaScript environments. If two actors specify the same timeout
// time in, say, their initialize() function, then they will be invoked at the
// same model time, and their outputs will be simultaneous. Any downstream actor
// will see them simultaneously.
// Note with this implementation, it is not necessary to
// call clearTimeout() in the actor's wrapup() function.
// Nevertheless, it is a good idea to do that in an accessor
// since other accessor hosts may not work the same way.
function setTimeout(func, milliseconds) {
    var callback = func;
    // If there are arguments to the callback, create a new function.
    // Get an array of arguments excluding the first two.
    var tail = Array.prototype.slice.call(arguments, 2);
    if (tail.length !== 0) {
        callback = function() {
            func.apply(this, tail);
        };
    }
    var id = actor.setTimeout(callback, milliseconds);
    return id;
}
