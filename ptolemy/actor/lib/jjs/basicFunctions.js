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
// print is built in to Nashorn.

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
// Set a timeout to call the specified function after the specified time.
// Return a handle to use in clearTimeout().
// The setTimeout(callback, timeout) function is already built in to the Window object.
// FIXME: Nashorn!!!
