// JavaScript functions for a Ptolemy II (Nashorn) accessor host.
// This file includes local functions that are specific to the Nashorn implementation.
//
// Author: Edward A. Lee
// Copyright: http://terraswarm.org/accessors/copyright.txt
//
/////////////////////////////////////////////////////////////////
// First section is utility functions that accessors are not meant
// to use directly, but that the accessor support functions below
// use. These all have names beginning with underscore.
//
// Set a global variable _debug to true to see console outputs.

////////////////////
// Set an exports global object with default functions.
var exports = {
    fire: function() {},
    initialize: function() {},
    setup: function() {},
    wrapup: function() {}
};

/** Add a handler function to call when the specified input receives new data.
 *  If the name argument is null, then call the handler when any input receives data.
 *  Return a handle to use in removeInputHandler(). If there are additional arguments
 *  beyond the first two, then those arguments will be passed to the function
 *  when it is invoked. The handler can retrieve the input input value by invoking get().
 *  Note that with this implementation, it is not necessary to
 *  call removeInputHandler() in the actor's wrapup() function.
 *  Nevertheless, it is a good idea to do that in an accessor
 *  since other accessor hosts may not work the same way.
 *  @param name The input name (a string), or null to react to any input.
 *  @param func The function to invoke when the input receives data.
 */
function addInputHandler(name, func) {
    if (name && typeof name !== 'string') {
        throw('name argument is required to be a string. Got: ' + (typeof name));
    }
    if (!func) {
        func = nullHandlerFunction;
    } else if (typeof func !== 'function') {
        throw('First argument of addInputHandler is required to be a function. Provided: ' + func);
    }
    var proxy = null;
    if (name) {
        proxy = actor.getPortOrParameterProxy(name);
    }
    if (!proxy && name) {
        throw('No such input: ' + name);
    }
    var callback = func;
    // If there are arguments to the callback, create a new function.
    // Get an array of arguments excluding the first two.
    var tail = Array.prototype.slice.call(arguments, 2);
    if (tail.length !== 0) {
        callback = function() {
            func.apply(this, tail);
        };
    }
    if (proxy) {
        var id = proxy.addInputHandler(callback);
        return id;
    } else {
        // Add generic input handler.
        var id = actor.addInputHandler(callback);
        return id;
    }
}

/** Default empty function to use if the function argument to
 *  addInputHandler is null.
 */
function nullHandlerFunction() {}

////////////////////
// Default fire function, which invokes exports.fire().
// Note that if the script simply defines a top-level fire() function instead
// of exports.fire(), that function will overwrite this one and will still work
// as expected.
function fire() {exports.fire();}

/** Get data from an input.
 *  @param name The name of the input (a string).
 *  @param channel The (optional) channel number, where null is equivalent to 0.
 */
function get(name, channel) {
    if (typeof name !== 'string') {
        throw('name argument is required to be a string. Got: ' + (typeof name));
    }
    var proxy = actor.getPortOrParameterProxy(name);
    if (!proxy) {
        throw('No such input: ' + name);
    }
    // Give channel a default value of 0.
    channel = (typeof channel !== 'undefined') ? channel : 0;
    var result = proxy.get(channel);
    return convertFromToken(result);
}

////////////////////
// Default initialize function, which invokes exports.initialize().
// Note that if the script simply defines a top-level initialize() function instead
// of exports.initialize(), that function will overwrite this one and will still work
// as expected.
function initialize() {exports.initialize();}

/** Remove the input handler with the specified handle.
 *  @param handle The handle.
 *  @see #addInputHandler()
 */
function removeInputHandler(handle) {
    actor.removeInputHandler(handle);
}

/** Send data to an output or an input.
 *  @param name The name of the output or input (a string).
 *  @param value The value to send.
 *  @param channel The (optional) channel number, where null is equivalent to 0.
 */
function send(name, value, channel) {
    if (typeof name !== 'string') {
        throw('name argument is required to be a string. Got: ' + (typeof name));
    }
    var proxy = actor.getPortOrParameterProxy(name);
    if (!proxy) {
        throw('No such output: ' + port);
    }
    // Give channel a default value of 0.
    channel = (typeof channel !== 'undefined') ? channel : 0;
    var token = convertToToken(value);
    proxy.send(channel, token);
}

/** Set the value of a parameter.
 *  @param value The value to set.
 *  @param parameter The parameter name (a string).
 */
function set(value, parameter) {
    // For backward compatibility, allow the port to be directly
    // a reference to the proxy.
    var proxy = parameter;
    if (typeof parameter === 'string') {
        proxy = actor.getPortOrParameterProxy(parameter);
    }
    if (!proxy) {
        throw('No such input: ' + parameter);
    }
    var token = convertToToken(value);
    proxy.set(token);
}

////////////////////
// Default setup function, which invokes exports.setup().
// Note that if the script simply defines a top-level setup() function instead
// of exports.setup(), that function will overwrite this one and will still work
// as expected.
function setup() {exports.setup();}

////////////////////
// Default wrapup function, which invokes exports.wrapup().
// Note that if the script simply defines a top-level wrapup() function instead
// of exports.wrapup(), that function will overwrite this one and will still work
// as expected.
function wrapup() {exports.wrapup();}

//--------------------------- Exposed Java Types -----------------------------
var ActorToken = Java.type('ptolemy.data.ActorToken');
var ArrayToken = Java.type('ptolemy.data.ArrayToken');
var BaseType = Java.type('ptolemy.data.type.BaseType');
var BooleanToken = Java.type('ptolemy.data.BooleanToken');
var DateToken = Java.type('ptolemy.data.DateToken');
var DoubleToken = Java.type('ptolemy.data.DoubleToken');
var Entity = Java.type('ptolemy.kernel.Entity');
var IntToken = Java.type('ptolemy.data.IntToken');
var LongToken = Java.type('ptolemy.data.LongToken');
var ObjectToken = Java.type('ptolemy.data.ObjectToken');
var RecordToken = Java.type('ptolemy.data.RecordToken');
var StringToken = Java.type('ptolemy.data.StringToken');
var Token = Java.type('ptolemy.data.Token');


//---------------------------- Utility functions -----------------------------
/** Convert the specified argument from a Ptolemy II Token
 *  to a JavaScript type if there is a lossless conversion.
 *  This is a utility function, not intended for script writers to use.
 *  Otherwise, just return the value.
 *  @param value The token to convert.
 */
function convertFromToken(value) {
    // If the value is not a Token, just return it.
    if (!(value instanceof Token)) {
        return value;
    } else if (value instanceof DoubleToken) {
        return value.doubleValue();
    } else if (value instanceof StringToken) {
        return value.stringValue();
    } else if (value instanceof IntToken) {
        return value.intValue();
    } else if (value instanceof BooleanToken) {
        return value.booleanValue();
    } else if (value instanceof ArrayToken) {
        var result = [];
        for (var i = 0; i < value.length(); i++) {
            result[i] = convertFromToken(value.getElement(i));
        }
        return result;
    } else if (value instanceof RecordToken) {
        var result = {};
        var labelSet = value.labelSet();
        // "for each" is a Nashorn extension for iterating over Java collections.
        for each (label in value.labelSet()) {
            result[label] = convertFromToken(value.get(label));
        }
        return result;
    } else if (value instanceof DateToken) {
        return new Date(value.getValue());
    } else if (value instanceof ActorToken) {
        return value.getEntity();
    }
    // If all else fails, just return the token object.
    return value;
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
        if ((value%1) === 0) {
        	if (value >= -2147483648 && value <= 2147483647) {
        		// Integer.
        		return new IntToken(value);
        	}
        	return new LongToken(value);
        }
        return new DoubleToken(value);
    } else if (type === 'string') {
        return new StringToken(value);
    } else if (type === 'boolean') {
        return new BooleanToken(value);
    } else if (type === 'object') {
        if (Array.isArray(value)) {
            // Using Nashorn-specific extension here to create Java array.
        	if (value.length < 1) {
        		// FIXME:  Ptolemy requires a type to be specified for empty 
        		// arrays.  Javascript does not, so there's no information as
        		// to what the type should be.  Currently, use a string.
        		return new ArrayToken(BaseType.STRING);
        	}
            var TokenArray = Java.type('ptolemy.data.Token[]');
            var result = new TokenArray(value.length);
            for(var i = 0; i < value.length; i++) {
                result[i] = convertToToken(value[i]);
            }
            return new ArrayToken(result);
        } else if (value === null) {
            // Is this the right thing to do?
            return Token.NIL;
        } else if (value instanceof Date) {
            // NOTE: DateToken constructor takes a long, which JavaScript doesn't support.
            // But the following seems to work. Consequences?
            return new DateToken(value.getTime());
        } else if (value instanceof Entity) {
            return new ActorToken(value);
        } else {
            // Create a RecordToken with the fields of the object.
            // Using Nashorn-specific extension here to create Java array.
            var TokenArray = Java.type('ptolemy.data.Token[]');
            var StringArray = Java.type('java.lang.String[]');
            var length = value.length;
            // Sadly, I can't find any way to find out how many enumerable
            // properties a JS object has. So we count them.
            var count = 0;
            for (field in value) {
                count += 1;
            }
            // NOTE: If there are no properties, we will send an empty record.
            var valueArray = new TokenArray(count);
            var fieldNames = new StringArray(count);
            var i = 0;
            for (field in value) {
                fieldNames[i] = field;
                valueArray[i] = convertToToken(value[field]);
                i++;
            }
            return new RecordToken(fieldNames, valueArray);
        }
    } else if (type === 'undefined') {
        // FIXME: More information?
        throw "value is undefined";
    }
    // If all else fails, wrap the value in an ObjectToken.
    return new ObjectToken(value);
}
