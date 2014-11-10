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
// Set debug to true to see console outputs.
var _debug = false;

////////////////////
// Default fire function, which does nothing.
function fire() {}

////////////////////
// Function to get data from an input port.
function get(port, channel) {
    // Give channel a default value of 0.
    channel = (typeof channel !== 'undefined') ? channel : 0;
    var result = port.get(channel);
    return convertFromToken(result);
}

////////////////////
// Default initialize function, which does nothing.
function initialize() {}

////////////////////
// Function to send data to an output port.
function send(value, port, channel) {
    // Give channel a default value of 0.
    channel = (typeof channel !== 'undefined') ? channel : 0;
    var token = convertToToken(value);
    port.send(channel, token);
}

////////////////////
// Default wrapup function, which does nothing.
function wrapup() {}

//--------------------------- Exposed Java Types -----------------------------
var ArrayToken = Java.type('ptolemy.data.ArrayToken');
var BooleanToken = Java.type('ptolemy.data.BooleanToken');
var DateToken = Java.type('ptolemy.data.DateToken');
var DoubleToken = Java.type('ptolemy.data.DoubleToken');
var IntToken = Java.type('ptolemy.data.IntToken');
var ObjectToken = Java.type('ptolemy.data.ObjectToken');
var RecordToken = Java.type('ptolemy.data.RecordToken');
var StringToken = Java.type('ptolemy.data.StringToken');
var Token = Java.type('ptolemy.data.Token');

//---------------------------- Utility functions -----------------------------
////////////////////
// Convert the specified argument from a Ptolemy II Token
// to a JavaScript type if there is a lossless conversion.
// Otherwise, just return the value.
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
    }
    // FIXME: Handle DateToken and ActorToken
    // If all else fails, just return the token object.
    return value;
}

////////////////////
// Convert the specified argument to a Ptolemy II Token.
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
            // Integer.
            return new IntToken(value);
        }
        return new DoubleToken(value);
    } else if (type === 'string') {
        return new StringToken(value);
    } else if (type === 'boolean') {
        return new BooleanToken(value);
    } else if (type === 'object') {
        if (Array.isArray(value)) {
            // Using Nashorn-specific extension here to create Java array.
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
            // Have to go through a string representation, unfortunately.
            // FIXME: Doesn't work. Format doesn't match.
            return new DateToken(value.toString());
        } else {
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
    // FIXME: Handle ActorToken
    // If all else fails, wrap the value in an ObjectToken.
    return new ObjectToken(value);
}
        

