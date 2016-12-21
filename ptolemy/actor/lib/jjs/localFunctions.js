// JavaScript functions for a Ptolemy II (Nashorn) accessor host.
// Copyright (c) 2014-2016 The Regents of the University of California.
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

/** JavaScript functions for a Ptolemy II (Nashorn) accessor host.
 *
 * @module localFunctions
 * @author Edward A. Lee, Contributor: Christopher Brooks, Chris Shaver
 * @version $$Id$$
 * @since Ptolemy II 11.0
 */

/*jslint nomen: true */

// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals Java, actor, channel, clearInterval, clearTimeout, error, getAccessorCode, getParameter, httpRequest, input, java, output, parameter, readURL, require, requireAccessor, send, setDefault, setInterval, setAccessor, setParameter, setTimeout, superSend, _accessorPath */
/*jshint globalstrict: true*/
"use strict";

// The commonHost is defined in the accessors repo, but we have a local copy
// in this same directory.
var commonHost = require('commonHost.js');

////////////////////
// Set a prototype for the exports object with default functions.
var exports = {};
Object.setPrototypeOf(exports, {
    fire: function () {
        return undefined;
    },
    initialize: function () {
        return undefined;
    },
    setup: function () {
        return undefined;
    },
    wrapup: function () {
        return undefined;
    }
});

/** Evaluate the specified code in the current context.
 *  @param accessorName The name to give to the accessor.
 *  @param code The code to evaluate.
 */
function evaluateCode(accessorName, code) {
    var bindings = {
        'clearInterval': clearInterval,
        'clearTimeout': clearTimeout,
        'error': error,
        'getParameter': getParameter,
        'httpRequest': httpRequest,
        'input': input,
        'output': output,
        'parameter': parameter,
        'readURL': readURL,
        'require': require,
        'send': send,
        'setDefault': setDefault,
        'setInterval': setInterval,
        'setParameter': setParameter,
        'setTimeout': setTimeout,
        'superSend': superSend
    };
    return new commonHost.Accessor(accessorName, code, getAccessorCode, bindings);
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
    var result = proxy.get(0 /*channel*/ );
    return convertFromToken(result, proxy.isJSON());
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
    // Invoke the basic input() functionality of commonHost.
    // Make sure the context is this, not the prototype.
    commonHost.Accessor.prototype.input.call(this, name, options);

    // Then invoke the Ptolemy functionality, which will create the input if it doesn't
    // already exist.
    // Nashorn bug if options is undefined, where it says:
    // Cannot cast jdk.nashorn.internal.runtime.Undefined to java.util.Map.
    // Replace with null.
    if (typeof options === 'undefined') {
        options = null;
    }
    // The following will return a Token if there was a previous value
    // stored in the input port-parameter that overrides the defaults.
    // The value of that token should become the default value of this input,
    // regardless of what the options state.
    var previousValue = actor.input(name, options);
    if (previousValue) {
        this.inputs[name].value = convertFromToken(previousValue);
    }
}

/** Specify an output for the accessor.
 *  The name argument is a required string, recommended to be camelCase with a leading
 *  lower-case character). The options argument can have the following fields:
 *  * type: The data type of the output (a string). If this is not specified, then any valid JavaScript value may be sent as an output. If it is specified, it must be one of the valid data types.
 *  @param name The name of the output.
 *  @param options The options, or null or omitted to accept the defaults.
 */
function output(name, options) {
    // Invoke the basic output() functionality of commonHost.
    // Make sure the context is this, not the prototype.
    commonHost.Accessor.prototype.output.call(this, name, options);

    // Then invoke the Ptolemy functionality, which will create the input if it doesn't
    // already exist.
    // Nashorn bug if options is undefined, where it says:
    // Cannot cast jdk.nashorn.internal.runtime.Undefined to java.util.Map.
    // Replace with null.
    if (typeof options === 'undefined') {
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
    // Invoke the basic parameter() functionality of commonHost.
    // Make sure the context is this, not the prototype.
    commonHost.Accessor.prototype.parameter.call(this, name, options);

    // Then invoke the Ptolemy functionality, which will create the input if it doesn't
    // already exist.
    // Avoid this error:
    // Cannot cast jdk.nashorn.internal.runtime.Undefined to java.util.Map.
    // Replace with null.
    if (typeof options === 'undefined') {
        options = null;
    }
    var previousValue = actor.parameter(name, options);
    if (previousValue) {
        this.parameters[name].value = convertFromToken(previousValue);
    }
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
    var proxy = actor.getPortOrParameterProxy(name),
        token;
    if (!proxy) {
        error('No such port: ' + name);
    } else {
        /* The following used to be done here, but this send() function could be
         * be invoked in a Vert.x thread, and then there would be a race condition.
         * A send() could overtake another.
         * So I've moved this invocation to the place in the helper where the
         * send via the port actually occurs.

        this.superSend(name, value, channel);

         */

        // Give channel a default value of 0.
        channel = (typeof channel !== 'undefined') && (channel !== null) ? channel : 0;
        token = convertToToken(value, proxy.isJSON());
        proxy.send(channel, token, value);
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
    this.setParameter(parameter, value);
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
        token = convertToToken(value, proxy.isJSON());
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
    var proxy = actor.getPortOrParameterProxy(parameter),
        token;
    if (!proxy) {
        error('No such parameter: ' + parameter);
    } else {
        // Invoke the basic parameter() functionality of commonHost.
        // Make sure the context is this, not the prototype.
        commonHost.Accessor.prototype.setParameter.call(this, parameter, value);

        token = convertToToken(value, proxy.isJSON());
        proxy.set(token);
    }
}

/** Invoke send() of the commonHost accessor prototype to ensure that latestOutput()
 *  works.  This is a separate function so that the proxy can invoke it at the same
 *  time that it actually sends the data via the port. If the port is an input,
 *  then do nothing.
 *  @param name The name of the output (a string).
 *  @param value The value to send.
 *  @param channel The (optional) channel number, where null is equivalent to 0.
 */
function superSend(name, value, channel) {
    if (!this.outputs) {
        throw new Error(
            "No outputs property. Perhaps 'this' is not bound to the accessor?");
    }
    var output = this.outputs[name];
    if (output) {
        commonHost.Accessor.prototype.send.call(this, name, value, channel);
    }
}

//------------------------ Functions Invoked by JavaScript.java ------------------------

// Default fire function, which invokes exports.fire().
// Note that if the script simply defines a top-level fire() function instead
// of exports.fire(), that function will overwrite this one and will still work
// as expected.
function fire() {
    exports.fire();
}

// Default initialize function, which invokes exports.initialize().
// Note that if the script simply defines a top-level initialize() function instead
// of exports.initialize(), that function will overwrite this one and will still work
// as expected.
function initialize() {
    exports.initialize();
}

// Default setup function, which invokes exports.setup().
// Note that if the script simply defines a top-level setup() function instead
// of exports.setup(), that function will overwrite this one and will still work
// as expected.
function setup() {
    exports.setup();
}

// Stop execution of the model.  See ptolemy/actor/lib/Stop.java.
function stop() {
    actor.getDirector().finish();
    actor.getDirector().stopFire();
}

// Default wrapup function, which invokes exports.wrapup().
// Note that if the script simply defines a top-level wrapup() function instead
// of exports.wrapup(), that function will overwrite this one and will still work
// as expected.
function wrapup() {
    exports.wrapup();
}

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
 *  Otherwise, just return the value.
 *  A nil token results in returning null.
 *  This is a utility function, not intended for script writers to use.
 *  @param value The token to convert.
 *  @param isJSON True if the token being converted comes from a source with
 *   type JSON, in which case, if the value is a string, the string will be
 *   parsed.
 */
function convertFromToken(value, isJSON) {
    // If the value is not a Token, just return it.
    if (!(value instanceof Token)) {
        return value;
    }
    if (value.isNil()) {
        return null;
    }
    if (value instanceof DoubleToken) {
        return value.doubleValue();
    }
    if (value instanceof StringToken) {
        // NOTE: Used to always parse JSON here, but that is now handled in the common host
        // for most cases.
        if (isJSON) {
            // Attempt to parse the JSON.
            try {
                return JSON.parse(value.stringValue());
            } catch (err) {
                // Just return the string.
                return value.stringValue();
            }
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
        var result = [],
            i;
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
	// Under Mac OS X and Java 1.8.0_101, we need to add 0 to the
	// ms returned by DateToken.getValue() so that we avoid
	// getting an 'Invalid Date' from JavaScript date.
	// See $PTII/ptolemy/actor/lib/jjs/test/auto/JavaScriptReceiveDate.xml
	var ms = value.getValue() + 0;
        return new Date(ms);
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
 *  @param isJSON If the destination type is JSON, then return a StringToken
 *   containing the results of JSON.stringify().
 */
function convertToToken(value, isJSON) {
    // NOTE: This is better done in JavaScript than in Java
    // because while the Nashorn interface to Java objects
    // seems standardized, the access to JavaScript objects from
    // Java appears difficult without resorting to "internal" classes.
    // Supposedly, ScriptObjectMirror will solve this problem, but it's
    // API is currently poorly documented, and what documentation there is
    // disagrees with the implementation in Java 8.

    if (isJSON) {
        return new StringToken(JSON.stringify(value));
    }

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
	    console.log('localFunctions.js: convertToToken(' + value + ', ' + value.getTime() + ', ' + new Date(value.getTime()));
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
