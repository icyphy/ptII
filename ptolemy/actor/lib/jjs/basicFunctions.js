// JavaScript functions for a Ptolemy II (Nashorn) accessor host.
// This file includes basic utility functions assumed by version 0 accessors.
//
// Author: Edward A. Lee
// Copyright: http://terraswarm.org/accessors/copyright.txt
//
////////////////////
// Pop up a dialog with the specified message.
function alert(message) {
    var MessageHandler = Java.type('ptolemy.util.MessageHandler');
    MessageHandler.message(message);
}

////////////////////
// Clear a timeout with the specified handle.
function clearTimeout(timeout) {
    error("FIXME: clearTimeout is not yet implemented.");
}

////////////////////
// Method for handling an error. This just throws an exception.
// Deprecated: Just use throw(message).
function error(message) {
    throw message;
}

////////////////////
// Method for performing a synchronous HTTP request.
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
    var request = new XMLHttpRequest();
    // The third argument specifies a synchronous read.
    request.open(method, url, false);
    // Null argument says there is no body.
    request.send(body);
    // readyState === 4 is the same as readyState === request.DONE.
    if (request.readyState === request.DONE) {
        if (request.status <= 400) {
            return request.responseText;
        } else {
            throw "httpRequest failed with code " + request.status + " at URL: " + url;
        }
    } else {
        throw "httpRequest did not complete: " + url;
    }
}

////////////////////
// Print a message to the console.
function print(message) {
    console.log(message);
}

////////////////////
// Method for synchronously reading a URL.
function readURL(url) {
	if (_debug) console.log("readURL(" + url + ")");
    var request = new XMLHttpRequest();
    // The third argument specifies a synchronous read.
    request.open("GET", url, false);
    // Null argument says there is no body.
    request.send(null);
    // readyState === 4 is the same as readyState === request.DONE.
    if (request.readyState === request.DONE) {
        if (request.status <= 400) {
            return request.responseText;
        } else {
            throw "readURL failed with code " + request.status + " at URL: " + url;
        }
    } else {
        throw "readURL did not complete: " + url;
    }
}

////////////////////
// Set a timeout to call the specified function after the specified time.
// Return a handle to use in clearTimeout().
// The setTimeout(callback, timeout) function is already built in to the Window object.
