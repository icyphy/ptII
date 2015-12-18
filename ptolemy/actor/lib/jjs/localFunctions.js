// JavaScript functions for a Ptolemy II (Nashorn) accessor host.
// Copyright (c) 2015 The Regents of the University of California.
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

/** JavaScript functions for a Ptolemy II (Nashorn) accessor host.
 *
 * <p> This file includes local functions that are specific to the
 * Nashorn implementation.</p>
 *
 * <p> The First section is utility functions that accessors are not meant
 * to use directly, but that the accessor support functions below
 * use. These all have names beginning with underscore.</p>
 *
 * <p>Set a global variable _debug to true to see console outputs.</p>
 *
 * <h2>References</h2>
 *
 * <p><name="VisionOfSwarmLets">Elizabeth Latronico, Edward A. Lee,
 * Marten Lohstroh, Chris Shaver, Armin Wasicek, Matt Weber.</a>
 * <a href="http://www.terraswarm.org/pubs/332.html">A Vision of Swarmlets</a>,
 * <i>IEEE Internet Computing, Special Issue on Building Internet
 * of Things Software</i>, 19(2):20-29, March 2015.</p>
 *
 * @module localFunctions
 * @author Edward A. Lee, Contributor: Christopher Brooks, Chris Shaver
 * @version $$Id$$
 * @since Ptolemy II 11.0
 */

// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals Java, actor, channel, error, java, requireAccessor */
/*jshint globalstrict: true*/
"use strict";

////////////////////
// Set a prototype for the exports object with default functions.
var exports = {};
Object.setPrototypeOf(exports, {
    fire: function () {return undefined; },
    initialize: function () {return undefined; },
    setup: function () {return undefined; },
    wrapup: function () {return undefined; }
});

/** Default empty function to use if the function argument to
 *  addInputHandler is null.
 */
function nullHandlerFunction() {}

/** Add a handler function to call when the specified input receives new data.
 *  If the name argument is null, or if there is no name argument and the first
 *  argument is a function, then call the handler when any input receives data.
 *  Return a handle to use in removeInputHandler(). If there are additional arguments
 *  beyond the function, then those arguments will be passed to the function
 *  when it is invoked. The handler can retrieve the input input value by invoking get().
 *  Note that with this implementation, it is not necessary to
 *  call removeInputHandler() in the actor's wrapup() function.
 *  Nevertheless, it is a good idea to do that in an accessor
 *  since other accessor hosts may not work the same way.
 *  The handler function will be invoked in the context of the exports object
 *  defined in the accessor script (i.e., 'this' will resolve to that exports object).
 *  @param name The input name (a string), or null to react to any input.
 *  @param func The function to invoke when the input receives data.
 *  @param arguments Additional arguments, if any, to pass to the callback function.
 */
function addInputHandler(name, func) {
    var argCount = 2, callback, id, proxy = null, tail;
    if (name && typeof name !== 'string') {
        // Tolerate a single argument, a function.
        if (typeof name === 'function') {
            func = name;
            name = null;
            argCount = 1;
        } else {
            throw ('name argument is required to be a string. Got: ' + (typeof name));
        }
    }
    if (!func) {
        func = nullHandlerFunction;
    } else if (typeof func !== 'function') {
        throw ('Argument of addInputHandler is not a function. It is: ' + func);
    }

    if (name) {
        proxy = actor.getPortOrParameterProxy(name);
    }
    if (!proxy && name) {
        throw ('No such input: ' + name);
    }

    // If there are arguments to the callback, create a new function.
    // Get an array of arguments excluding the first two.
    tail = Array.prototype.slice.call(arguments, argCount);
    if (tail.length !== 0) {
        callback = function() {
            func.apply(exports, tail);
        };
    } else {
        callback = function() {
            func.apply(exports);
        };
    }
    if (proxy) {
        id = proxy.addInputHandler(callback);
        return id;
    } else {
        // Add generic input handler.
        id = actor.addInputHandler(callback);
        return id;
    }
}

/** Specify that a derived accessor extends a specified base accessor.
 *  Call this in the setup() function of the derived accessor as:
 *  ```javascript
 *     extend('MyBaseAccessor');
 *  ```
 *  This will cause the setup() function of the base accessor to be invoked,
 *  which means that this accessor will acquire all inputs, outputs, and parameters
 *  of the base accessor.
 *
 *  In addition, the derived accessor inherits all fields of the exports
 *  field of the base accessor, including its initialize(), wrapup(), and any other
 *  exported function.  To override these, simply define new functions. The override
 *  can invoke the base accessor's function as in the following example:
 *  ```javascript
 *     exports.initialize = function() {
 *        Object.getPrototypeOf(exports).initialize.apply(this);
 *        ... code specific to the current accessor ...
 *     };
 *  ```
 *  or more simply
 *  ```javascript
 *     exports.initialize = function() {
 *        ssuper.initialize.apply(this);
 *        ... code specific to the current accessor ...
 *     };
 *  ```
 *
 *  In this implementation, the accessor definition is searched for in
 *  $PTII/org/terraswarm/accessor/accessors/web, which is expected to be a clone
 *  of the repository at http://terraswarm.org/accessors.
 *
 *  FIXME: Need a way to specify a different search location for the accessor,
 *  including online.
 *
 *  @param accessorName The name of the accessor to extend.
 *  @see #implement()
 */
var extend = function(exportsExtending) {
    return function(accessorName) {
        var exportsPrototype = requireAccessor(accessorName);
        // Make sure the prototype has default methods defined.
        if (!exportsPrototype.fire ||
                !exportsPrototype.initialize ||
                !exportsPrototype.setup ||
                !exportsPrototype.wrapup) {
            Object.setPrototypeOf(exportsPrototype, {
                fire: function() {},
                initialize: function() {},
                setup: function() {},
                wrapup: function() {}
            });
        }
        Object.setPrototypeOf(exportsExtending, exportsPrototype);
        
        // NOTE: The super keyword is built in to ECMA 6.
        // Since it isn't available in earlier versions, we provide an alternate:
        exportsExtending.ssuper = exportsPrototype;
        
        // OK, this is mind-blowing difficult to understand, but the setup() method
        // below may itself include an invocation of extend(), realizing multi-level
        // inheritance. To make sure to not overwrite the prototype at this level
        // of the prototype chain, change the value of the exportsExtending argument
        // to be the new object whose prototype is to be set by the nested call to
        // extend().
        exportsExtending = exportsPrototype;
        // Now invoke the setup method.
        exportsPrototype.setup();
    };
}(exports);

/** Get data from an input.
 *  @param name The name of the input (a string).
 *  @param channel The (optional) channel number, where null is equivalent to 0.
 *  @return The value received on the input, or null if no value is received.
 */
function get(name, channel) {
    if (typeof name !== 'string') {
        throw ('name argument is required to be a string. Got: ' + (typeof name));
    }
    var proxy = actor.getPortOrParameterProxy(name);
    if (!proxy) {
        throw ('No such input: ' + name);
    }
    // Give channel a default value of 0.
    channel = (typeof channel !== 'undefined') ? channel : 0;
    var result = proxy.get(channel);
    return convertFromToken(result, proxy.isJSON());
}

/** Get data from a parameter.
 *  @param name The name of the parameter (a string).
 *  @return The value of the parameter, or null if it has no value.
 */
function getParameter(name) {
    if (typeof name !== 'string') {
        throw ('name argument is required to be a string. Got: ' + (typeof name));
    }
    var proxy = actor.getPortOrParameterProxy(name);
    if (!proxy) {
        throw ('No such parameter: ' + name);
    }
    // Give channel a default value of 0.
    //if (typeof channel === 'undefined') {
    //    channel = 0;
    //}
    var result = proxy.get(0 /*channel*/);
    return convertFromToken(result, proxy.isJSON());
}

/** Specify that a derived accessor implements a specified base accessor interface.
 *  Call this in the setup() function of the derived accessor as:
 *  ```javascript
 *     implement('MyInterface');
 *  ```
 *  This will cause the setup() function of the interface to be invoked,
 *  which means that this accessor will acquire all inputs, outputs, and parameters
 *  of the interface.
 *
 *  In this implementation, the interface is searched for in
 *  $PTII/org/terraswarm/accessor/accessors/web, which is expected to be a clone
 *  of the repository at http://terraswarm.org/accessors.
 *
 *  FIXME: Need a way to specify a different search location for the interface,
 *  including online.
 *
 *  @param interfaceName The name of the interface to implement.
 *  @see #extend()
 */
function implement(interfaceName) {
    var interfaceExports = requireAccessor(interfaceName);
    interfaceExports.setup();
}

/** Specify an input for the accessor.
 *  The name argument is a required string, recommended to be camelCase with a leading
 *  lower-case character). The options argument can have the following fields:
 *  * options: An array of possible values for this input.
 *  * type: The data type of the input (a string). If this is not specified,
 *    then any valid JavaScript value may be provided as an input.
 *    If it is specified, it must be one of the valid data types.
 *  * value: A default value for the input.
 *  * visibility: A hint to restrict the visibility that a user has of the input
 *    This can have one of the values "none" (no visibility), "expert" (expert
 *    visibility), "noteditable" (full visibility, but not modifiable), or
 *    "full" (full visibility, the default).
 *  @param name The name of the input.
 *  @param options The options, or null or omitted to accept the defaults.
 */
function input(name, options) {
    // Nashorn bug if options is undefined, where it says:
    // Cannot cast jdk.nashorn.internal.runtime.Undefined to java.util.Map.
    // Replace with null.
    if (options === undefined) {
        options = null;
    }
    actor.input(name, options);
}

/** Specify an output for the accessor.
 *  The name argument is a required string, recommended to be camelCase with a leading
 *  lower-case character). The options argument can have the following fields:
 *  * type: The data type of the output (a string). If this is not specified, then any valid JavaScript value may be sent as an output. If it is specified, it must be one of the valid data types.
 *  @param name The name of the output.
 *  @param options The options, or null or omitted to accept the defaults.
 */
function output(name, options) {
    // Nashorn bug if options is undefined, where it says:
    // Cannot cast jdk.nashorn.internal.runtime.Undefined to java.util.Map.
    // Replace with null.
    if (options === undefined) {
        options = null;
    }
    actor.output(name, options);
}

/** Specify a parameter for the accessor.
 *  The name argument is a required string, recommended to be camelCase with a leading
 *  lower-case character). The options argument can have the following fields:
 *  * options: An array of possible values for this input.
 *  * type: The data type of the parameter (a string). If this is not specified,
 *    then any valid JavaScript value may be provided for the value.
 *    If it is specified, it must be one of the valid data types.
 *  * value: A default value for the parameter.
 *  * visibility: A hint to limit the visibility that a user has of this parameter.
 *    This can be one of "full" (the default), "expert", "noteditable", or "none",
 *    meaning full visibility, expert visibility, full visibility but without being
 *    able to change the value, and no visibility.
 *  @param name The name of the parameter.
 *  @param options The options, or null or omitted to accept the defaults.
 */
function parameter(name, options) {
    // Nashorn bug if options is undefined, where it says:
    // Cannot cast jdk.nashorn.internal.runtime.Undefined to java.util.Map.
    // Replace with null.
    if (options === undefined) {
        options = null;
    }
    actor.parameter(name, options);
}

/** Remove the input handler with the specified handle.
 *  @param handle The handle.
 *  @see #addInputHandler()
 */
function removeInputHandler(handle) {
    actor.removeInputHandler(handle);
}

/** Send data to an output or an input.
 *  If the type of the output or input is JSON, then the value
 *  is converted to a JSON string using JSON.stringify(value) before sending.
 *  If you are sending to an input, the value of that input will not be changed
 *  immediately, but instead, after conclusion of the function calling send(),
 *  any input handlers registered with that input and any fire() method defined
 *  will be invoked.
 *  @param name The name of the output or input (a string).
 *  @param value The value to send.
 *  @param channel The (optional) channel number, where null is equivalent to 0.
 */
function send(name, value, channel) {
    if (typeof name !== 'string') {
        throw ('name argument is required to be a string. Got: ' + (typeof name));
    }
    var proxy = actor.getPortOrParameterProxy(name), token;
    if (!proxy) {
        error('No such port: ' + name);
    } else {
        // Give channel a default value of 0.
        channel = (typeof channel !== 'undefined') ? channel : 0;
        if (proxy.isJSON()) {
            token = new StringToken(JSON.stringify(value));
        } else {
            token = convertToToken(value);
        }
        proxy.send(channel, token);
    }
}

/** Set the value of a parameter.
 *  Note that this can also be used to set the value of an input that has a
 *  default value, instead of using send(), but no input handler will be triggered.
 *  @param parameter The parameter name (a string).
 *  @param value The value to set.
 *  @deprecated Use setParameter() or setDefault().
 */
function set(parameter, value) {
    setParameter(parameter, value);
}

/** Set the default value of an input. Note that unlike
 *  using send(), no input handler will be triggered.
 *  Also, unlike send(), the provided value will be persistent,
 *  in that once it is set, the host will store the new value along with the model.
 *  @param input The input name (a string).
 *  @param value The value to set.
 */
function setDefault(input, value) {
    if (typeof input !== 'string') {
        throw ('input argument is required to be a string. Got: ' + (typeof input));
    }
    var proxy = actor.getPortOrParameterProxy(input);
    if (!proxy) {
        error('No such input: ' + input);
    } else {
        var token;
        if (proxy.isJSON()) {
            token = new StringToken(JSON.stringify(value));
        } else {
            token = convertToToken(value);
        }
        proxy.set(token);
    }
}

/** Set the value of a parameter.
 *  @param parameter The parameter name (a string).
 *  @param value The value to set.
 */
function setParameter(parameter, value) {
    if (typeof parameter !== 'string') {
        throw ('parameter argument is required to be a string. Got: ' + (typeof parameter));
    }
    var proxy = actor.getPortOrParameterProxy(parameter), token;
    if (!proxy) {
        error('No such parameter: ' + parameter);
    } else {
        if (proxy.isJSON()) {
            token = new StringToken(JSON.stringify(value));
        } else {
            token = convertToToken(value);
        }
        proxy.set(token);
    }
}

//------------------------ Functions Invoked by JavaScript.java ------------------------
// FIXME: Should these names have leading underscores?

// Default fire function, which invokes exports.fire().
// Note that if the script simply defines a top-level fire() function instead
// of exports.fire(), that function will overwrite this one and will still work
// as expected.
function fire() {exports.fire(); }

/** Initialize function used by the Ptolemy II/Nashorn host to provide the host
 *  context as an argument to the initialize() function of the accessor.
 *  This should not be called directly or overridden by the accessor.
 *  FIXME: Above comment not yet implemented.
 */
function initialize() {exports.initialize(); }

// Default setup function, which invokes exports.setup().
// Note that if the script simply defines a top-level setup() function instead
// of exports.setup(), that function will overwrite this one and will still work
// as expected.
function setup() {exports.setup(); }

// Default wrapup function, which invokes exports.wrapup().
// Note that if the script simply defines a top-level wrapup() function instead
// of exports.wrapup(), that function will overwrite this one and will still work
// as expected.
function wrapup() {exports.wrapup(); }

//--------------------------- Exposed Java Types -----------------------------
// FIXME: Attempting to debug ClassNotFoundException after loading 350 models.
try {
    var ArrayToken2 = Java.type('ptolemy.data.ArrayToken');
} catch (e) {
    var message = e.message;
    if (!message) {
        message = e.toString();
    }
    var javaClassPath = java.lang.System.getProperty("java.class.path");
    var userDir = java.lang.System.getProperty("user.dir");
    throw new Error("Error loading ptolemy.data.ArrayToken class" + message + " java.class.path property: " + javaClassPath + " user.dir property (current working directory): " + userDir);
}

var ArrayToken = Java.type('ptolemy.data.ArrayToken');
var ActorToken = Java.type('ptolemy.data.ActorToken');
var AWTImageToken = Java.type('ptolemy.data.AWTImageToken');
var BaseType = Java.type('ptolemy.data.type.BaseType');
var BooleanToken = Java.type('ptolemy.data.BooleanToken');
var DateToken = Java.type('ptolemy.data.DateToken');
var DoubleToken = Java.type('ptolemy.data.DoubleToken');
var Entity = Java.type('ptolemy.kernel.Entity');
var Image = Java.type('java.awt.Image');
var IntToken = Java.type('ptolemy.data.IntToken');
var LongToken = Java.type('ptolemy.data.LongToken');
var ObjectToken = Java.type('ptolemy.data.ObjectToken');
var RecordToken = Java.type('ptolemy.data.RecordToken');
var StringToken = Java.type('ptolemy.data.StringToken');
var Token = Java.type('ptolemy.data.Token');
var TokenArray = Java.type('ptolemy.data.Token[]');
// Converter class for JSON.  Converts to a RecordToken or ArrayToken.
var JSONToToken = Java.type('ptolemy.actor.lib.conversions.json.JSONToToken');


//---------------------------- Utility functions -----------------------------
/** Convert the specified argument from a Ptolemy II Token
 *  to a JavaScript type if there is a lossless conversion.
 *  This is a utility function, not intended for script writers to use.
 *  Otherwise, just return the value.
 *  @param value The token to convert.
 */
function convertFromToken(value, isJSON) {
    // If the value is not a Token, just return it.
    if (!(value instanceof Token)) {
        return value;
    }
    if (value instanceof DoubleToken) {
        return value.doubleValue();
    }
    if (value instanceof StringToken) {
        if (isJSON) {
            var json = value.stringValue();
            if (json.trim() === '') {
                // Empty string.
                return null;
            }
            return JSON.parse(value.stringValue());
        }
        return value.stringValue();
    }
    if (value instanceof IntToken) {
        return value.intValue();
    }
    if (value instanceof BooleanToken) {
        return value.booleanValue();
    }
    if (value instanceof ArrayToken) {
        var result = [], i;
        for (i = 0; i < value.length(); i++) {
            result[i] = convertFromToken(value.getElement(i), false);
        }
        return result;
    }
    if (value instanceof RecordToken) {
        var resultRecord = {};
        //var labelSet = value.labelSet();
        // "for each" is a Nashorn extension for iterating over Java collections.
        // This is tested by ptolemy/actor/lib/jjs/test/auto/JavaScriptRecordToken.xml
        // The "for each" below causes problems with JSLint
        //for each (label in value.labelSet()) {
        //    result[label] = convertFromToken(value.get(label), false);
        // So, we use an iterator instead:
        var iterator = value.labelSet().iterator();
        while (iterator.hasNext()) {
            var label = iterator.next();
            resultRecord[label] = convertFromToken(value.get(label), false);
        }
        return resultRecord;
    }
    if (value instanceof DateToken) {
        return new Date(value.getValue());
    }
    if (value instanceof ActorToken) {
        return value.getEntity();
    }
    // If all else fails, just return the token object.
    return value;
}

/** Convert the specified object to a native JavaScript array.
 *  This is useful when you have constructed an Object[] in Java
 *  and you wish to pass it to the JavaScript world and have it
 *  treated as an array. Without this function, it will be treated
 *  as an ordinary object that happens to have fields '0', '1', etc.
 *  @param array The array to convert.
 */
function convertToJSArray(array) {
    return Java.from(array);
}
 
/** Convert the specified argument to a Ptolemy II Token.
 *  This is a utility function, not intended for script writers to use.
 *  @param value The JavaScript value to convert.
 */
function convertToToken(value) {
    // NOTE: This is better done in JavaScript than in Java
    // because while the Nashorn interface to Java objects
    // seems standardized, the access to JavaScript objects from
    // Java appears difficult without resorting to "internal" classes.
    // Supposedly, ScriptObjectMirror will solve this problem, but it's
    // API is currently poorly documented, and what documentation there is
    // disagrees with the implementation in Java 8.
    
    // If the value is already a Token, just return it.
    if (value instanceof Token) {
        return value;
    }
    var type = typeof value;
    if (type === 'number') {
        if ((value % 1) === 0) {
            if (value >= -2147483648 && value <= 2147483647) {
                // Integer.
                return new IntToken(value);
            }
            return new LongToken(value);
        }
        return new DoubleToken(value);
    }
    if (type === 'string') {
        return new StringToken(value);
    }
    if (type === 'boolean') {
        return new BooleanToken(value);
    }
    if (type === 'object') {
        if (Array.isArray(value)) {
            // Using Nashorn-specific extension here to create Java array.
            if (value.length < 1) {
                // FIXME:  Ptolemy requires a type to be specified for empty 
                // arrays.  Javascript does not, so there's no information as
                // to what the type should be.  Currently, use a string.
                return new ArrayToken(BaseType.STRING);
            }
            var result = new TokenArray(value.length);
            for (var i = 0; i < value.length; i++) {
                result[i] = convertToToken(value[i]);
            }
            return new ArrayToken(result);
        }
        if (value === null) {
            // Is this the right thing to do?
            return Token.NIL;
        }
        if (value instanceof Date) {
            // NOTE: DateToken constructor takes a long, which JavaScript doesn't support.
            // But the following seems to work. Consequences?
            return new DateToken(value.getTime());
        }
        if (value instanceof Entity) {
            return new ActorToken(value);
        }
        if (value instanceof Image) {
            return new AWTImageToken(value);
        } 
        // Create a RecordToken with the fields of the object.
        // Using Nashorn-specific extension here to create Java array.
        var StringArray = Java.type('java.lang.String[]');
        var length = value.length;
        // Sadly, I can't find any way to find out how many enumerable
        // properties a JS object has. So we count them.
        var count = 0;
        var field2;
        for (field2 in value) {
            count += 1;
        }
        // NOTE: If there are no properties, we will send an empty record.
        var valueArray = new TokenArray(count);
        var fieldNames = new StringArray(count);
        var j = 0;
        var field;
        for (field in value) {
            fieldNames[j] = field;
            valueArray[j] = convertToToken(value[field]);
            j++;
        }
        return new RecordToken(fieldNames, valueArray);
    }
    if (type === 'undefined') {
        // Is this the right thing to do?
        return Token.NIL;
    }
    // If all else fails, wrap the value in an ObjectToken.
    return new ObjectToken(value);
}
