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

/** JavaScript functions for the Nashorn host, which uses
 *  Java's Nashorn JavaScript engine.
 *  This host supports version 1 accessors.
 *  
 *  This host is almost entirely independent of Ptolemy II,
 *  except for some utility functions in 
 *  FileUtilities (which could easily be factored out).
 *  Also, modules that are required by accessors are loaded
 *  from $PTII/ptolemy/actor/lib/jjs, and some of those modules
 *  may have dependencies on Ptolemy II.
 *
 *  @module nashornHost
 *  @author Edward A. Lee, Contributor: Christopher Brooks
 *  @version $$Id$$
 *  @since Ptolemy II 11.0
 */

// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals Java, commonHost, getAccessorCode, load */
/*jshint globalstrict: true */
/*jslint nomen: true */
"use strict";

////////////////////////////////////////////////////////////////////////////
//// Java dependencies.

// Java classes that define some static functions to call from JS.
var FileUtilities
        = Java.type('ptolemy.util.FileUtilities');
var System
    = Java.type('java.lang.System');

////////////////////////////////////////////////////////////////////////////
//// Global variables.

// Flag that will cause debug output to the console if set to true.
var debug = false;

////////////////////////////////////////////////////////////////////////////
//// Variables supporting module loading and accessor loading.

var __moduleFile = FileUtilities.nameToFile(
        '$CLASSPATH/ptolemy/actor/lib/jjs/',
        null
);

/** A string giving the full path to the root directory for installed modules. */
var _moduleRoot = __moduleFile.getAbsolutePath();

//Check to see if _moduleFile is a Jar URL like
if (_moduleRoot.indexOf("!/") !== -1) {
    _moduleRoot = "jar:" + __moduleFile.toString();
}

var __accessorFile = FileUtilities.nameToFile(
        '$CLASSPATH/org/terraswarm/accessor/accessors/web/',
        null
);

var _accessorRoot = __accessorFile.getAbsolutePath();

//Check to see if _accessorRoot is a Jar URL like
if (_accessorRoot.indexOf("!/") !== -1) {
_accessorRoot = "jar:" + __accessorFile.toString();
}

/** An array that gives the search path for modules to be required. */
var _modulePath = [_moduleRoot + '/',
      _moduleRoot + '/modules/',
      _moduleRoot + '/node/',
      _moduleRoot + '/node_modules/',
      _accessorRoot + '/hosts/',
      _accessorRoot + '/'];

/** An array that gives the search path for modules to be required relative to the classpath. */
var _moduleClasspath = ['$CLASSPATH/ptolemy/actor/lib/jjs/modules/',
          '$CLASSPATH/ptolemy/actor/lib/jjs/node/',
          '$CLASSPATH/ptolemy/actor/lib/jjs/node_modules/',
          '$CLASSPATH/org/terraswarm/accessor/accessors/web/hosts/',
          '$CLASSPATH/org/terraswarm/accessor/accessors/web/'];

/** A string giving the full path to the root directory for installed accessors. */
var _accessorRoot = FileUtilities.nameToFile(
        '$CLASSPATH/org/terraswarm/accessor/accessors/web/',
        null
).getAbsolutePath();

/** A string giving the full path to the root directory for test accessors. */
var _testAccessors = FileUtilities.nameToFile(
        '$CLASSPATH/org/terraswarm/accessor/test/auto/accessors/',
        null
).getAbsolutePath();

/** An array that gives the search path for accessors to be extended. */
var _accessorPath = [_accessorRoot + '/', _testAccessors + '/'].concat(_modulePath);

/** An array that gives the search path for accessors to be extended. */
var _accessorClasspath = ['$CLASSPATH/org/terraswarm/accessor/test/auto/accessors/'].concat(_moduleClasspath);

////////////////////////////////////////////////////////////////////////////
//// Function definitions.

/** Print a message to the console.
 *  NOTE: This function is not required by the accessor specification, so accessors
 *  should not rely on it being present.
 *  @param message The message
 */
function alert(message) {
    console.log(message);
}

/** Report an error by printing using console.error().
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

    console.error(message);
}

//////// NOTE: This host does not support getResource(). How to restrict
// file system access?

/**
 * Require the named module. This function imports modules formatted
 * according to the CommonJS standard.
 *
 * <p>If the name begins with './' or '/', then it is assumed to
 * specify a file or directory on the local disk. If it is a file, the
 * '.js' suffix may be optionally omitted. If it is a directory, then
 * this function will look for a package.json file in that directory
 * and load the file specified by the 'main' property the JSON object
 * defined in that file. If there is no package.json file, then it
 * will load an 'index.js' file, if there is one.</p>
 *
 * <p>If the name does not begin with './' or '/', then it is assumed
 * to specify a module installed in this accessor host.</p>
 *
 * <p>In both cases, this function returns an object that includes as
 * properties any properties that have been added to the 'exports'
 * property. For example, to export a function, the module JavaScript
 * file could define the function as follows:</p>
 *
 * <pre>
 *   exports.myFunction = function() {...};
 * </pre>
 *
 * <p>Alternatively, the module JavaScript file can explicitly define
 * the exports object as follows:<p>
 *
 * <pre>
 *   var myFunction = function() {...};
 *   module.exports = {
 *       myFunction : myFunction
 *   };
 * </pre>
 *
 * <p>This implementation uses the requires() function implemented by Walter Higgins,
 * found here: <a href="https://github.com/walterhiggins/commonjs-modules-javax-script.">https://github.com/walterhiggins/commonjs-modules-javax-script</a>.</p>
 *
 * @see http://nodejs.org/api/modules.html#modules_the_module_object
 * @see also: http://wiki.commonjs.org/wiki/Modules
 */
var require = load(_moduleRoot + '/external/require.js')(
    // Invoke the function returned by 'load' immediately with the following arguments.
    //    - a root directory in which to look for modules.
    //    - an array of paths in which to look for modules.
    //    - an optional hook object that includes two callback functions for notification.
    _moduleRoot,
    _modulePath
);

/**
 * Require the named accessor. This is a version of require() that looks
 * in a different place for accessors.
 * @see #require()
 */
/* FIXME: requireAccessor is not used anywhere.
var requireAccessor = load(_moduleRoot + '/external/require.js')(
    // Invoke the function returned by 'load' immediately with the following arguments.
    //    - a root directory in which to look for accessors.
    //    - an array of paths in which to look for accessors.
    //    - an optional hook object that includes two callback functions for notification.
    _accessorRoot,
    _accessorPath
);
*/

////////////////////
// Pull in the util and console modules, now that require is defined.
// NOTE: If require() is defined using "function require" rather than
// "var require = function", then this could be done at the top of the file,
// because the function keyword binds function definitions in this script
// before evaluating the script. However, then loading a subsequent script
// like capeCodeHost.js will not override the definition of require in any
// of its uses here. So we stick with this style.

var util = require('util');
var console = require('console');

// Locally defined modules.
var commonHost = require('commonHost.js');

// This Nashorn allows trusted accessors, which means that any
// accessor whose class name begins with 'trusted/' can invoke the
// function getTopLevelAccessors().
commonHost.allowTrustedAccessors(true);
////////////////////

/** Return the source code for an accessor from its fully qualified name.
 *  This will throw an exception if there is no such accessor on the accessor
 *  search path.
 *  @param name Fully qualified accessor name, e.g. 'net/REST'.
 */
function getAccessorCode(name) {
    var code,
        i,
        location;
    // Append a '.js' to the name, if needed.
    if (name.indexOf('.js') !== name.length - 3) {
        name += '.js';
    }

    // Handle absolute pathnames.
    if (name[0] === '/' || name[0] === '\\') {
        code = FileUtilities.getFileAsString(name);
        return code;
    }

    // _accessorPath is defined in basicFunctions.js.
    for (i = 0; i < _accessorPath.length; i++) {
        location = _accessorPath[i].concat(name);
        try {
            code = FileUtilities.getFileAsString(location);
            break;
        } catch (err) {
            continue;
        }
    }
    if (!code) {
	for (i = 0; i < _accessorClasspath.length; i++) {
		location = _accessorClasspath[i].concat(name);
		try {
			code = FileUtilities.getFileFromClasspathAsString(location);
			break;
		} catch (err) {
			continue;
		}
	}
    }
    if (!code) {
        throw ('Accessor ' + name + ' not found on path: ' + _accessorPath + ' or relative path: ' + _accessorClasspath);
    }
    return code;
}

/** Instantiate and return an accessor.
 *  This will throw an exception if there is no such accessor class on the accessor
 *  search path.
 *  @param accessorName The name to give to the instance.
 *  @param accessorClass Fully qualified accessor class name, e.g. 'net/REST'.
 */
function instantiate(accessorName, accessorClass) {
    // NOTE: The definition of the require var in this file may be overridden if
    // capeCodeHost.js is evaluated after this file is evaluated.
    var bindings = {
        'require': require,
    };
    var instance = new commonHost.instantiateAccessor(
        accessorName, accessorClass, getAccessorCode, bindings);
    console.log('Instantiated accessor ' + accessorName + ' with class ' + accessorClass);

    return instance;
}

////FIXME: Remove this.

/** Instantiate and initialize the accessors named by the
 *  accessorNames argument
 *
 * See invoke() for how this method is used.
 *
 * @param accessorNames An array of accessor names in a format suitable
 * for getAccessorCode(name).
 */
function instantiateAndInitialize(accessorNames) {
    var length = accessorNames.length;
    var index;
    for (index = 0; index < length; ++index) {
        // The name of the accessor is basename of the accessorClass.
        var accessorClass = accessorNames[index];

        // For example, if the accessorClass is
        // test/TestComposite, then the accessorName will be
        // TestComposite.

        var startIndex = (accessorClass.indexOf('\\') >= 0 ? accessorClass.lastIndexOf('\\') : accessorClass.lastIndexOf('/'));
        var accessorName = accessorClass.substring(startIndex);
        if (accessorName.indexOf('\\') === 0 || accessorName.indexOf('/') === 0) {
            accessorName = accessorName.substring(1);
        }
        // If the same accessorClass appears more than once in the
        // list of arguments, then use different names.
        // To replicate: node nodeHostInvoke.js test/TestComposite test/TestComposite
        if (index > 0) {
            accessorName += "_" + (index - 1);
        }
        var accessor = instantiate(accessorName, accessorClass);
        // Push the top level accessor so that we can call wrapup later.
        accessors.push(accessor);
    }
}

/** Evaluate command-line arguments by first converting the arguments
 *  from a Java array to a JavaScript array, and then invoking main()
 *  in commonHost.js.
 *  @param argv Command-line arguments.
 */
function processCommandLineArguments(argv) {
	return commonHost.processCommandLineArguments(
	        // Command-line arguments.
	        Java.from(argv),
	        // Function to read a file and return a string.
	        FileUtilities.getFileAsString,
	        // Function to instantiate accessors.
	        instantiate,
	        // Function to call upon termination.
	        function() {
	            // FIXME: Should first call wrapup() on all accessors.
	            System.exit(0);
	        }
	);
}

///////////////////////////////////////////////////////////////////////
// Make commonHost functions visible when this file is evaluated directly.

var Accessor = commonHost.Accessor;
var getTopLevelAccessors = commonHost.getTopLevelAccessors;
var stopAllAccessors = commonHost.stopAllAccessors;
var uniqueName = commonHost.uniqueName;

// FIXME: Handle exit calls like how we do in nodeHost?
