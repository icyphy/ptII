// Copyright (c) 2015-2016 The Regents of the University of California.
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
 * Module supporting authorization (Auth) service for the Internet of Things (IoT).
 * This module includes common functions for communication with IoT Auth service
 * and communication with other registered entities.
 *
 * @module iotAuth
 * @author Hokeun Kim
 * @version $$Id$$
 */

"use strict";

function numToVarLenInt(num) {
    var buf = new buffer.Buffer(0);
    while (num > 127) {
        var extraBuf = new buffer.Buffer(1);
        extraBuf.writeUInt8(128 | num & 127);
        buf = buffer.concat([buf, extraBuf]);
        num >>= 7;
    }
    var extraBuf = new buffer.Buffer(1);
    extraBuf.writeUInt8(num);
    buf = buffer.concat([buf, extraBuf]);
    return buf;
};

function varLenIntToNum(buf, offset) {
    var num = 0;
    for (var i = 0; i < buf.length && i < 5; i++) {
        num |= (buf.get(offset + i) & 127) << (7 * i);
        if ((buf.get(offset + i) & 128) == 0) {
            return {num: num, bufLen: i + 1};
            break;
        }
    }
    return null;
};

exports.serializeIoTSP = function(obj) {
    if (obj.msgType == undefined || obj.payload == undefined) {
        console.log('Error: IoTSP msgType or payload is missing.');
        return;
    }
    var msgTypeBuf = new buffer.Buffer(1);
    msgTypeBuf.writeUInt8(obj.msgType, 0);
    var payLoadLenBuf = numToVarLenInt(obj.payload.length);
    return buffer.concat([msgTypeBuf, payLoadLenBuf, obj.payload]);
};

exports.parseIoTSP = function(buf) {
    var msgTypeVal = buf.readUInt8(0);
    var ret = varLenIntToNum(buf, 1);
    var payloadVal = buf.slice(1 + ret.bufLen);
    return {msgType: msgTypeVal, payloadLen: ret.num, payload: payloadVal};
};

exports.test = function(str) {
    console.log(str);
}

/*
exports.Buffer = function(param) {
	if (typeof param === 'number') {
		this.array = new Array(param);
		for (var i = 0; i < this.array.length; i++) {
			this.array[i] = 0;
		}
	}
	else if (typeof param === 'string') {
		this.array = new Array(param.length);
		for (var i = 0; i < this.array.length; i++) {
			this.array[i] = (param.charCodeAt(i) & 255);
		}
	}
	else if (Array.isArray(param)) {
		this.array = new Array(param.length);
		for (var i = 0; i < this.array.length; i++) {
			if (typeof param[i] !== 'number') {
				throw 'Unsupported type of array for initializing Buffer!';
			}
			this.array[i] = (param[i] & 255);
		}
	}
	else {
		throw 'Unsupported type for initializing Buffer!';
	}
	Object.defineProperty(this, 'length', {
		get: function() { return this.array.length; }
	});
};

exports.Buffer.prototype.inspect = function() {
	var ret = '[';
	for (var i = 0; i < this.array.length; i++) {
		if (i != 0) {
			ret += ', ';
		}
		ret += this.array[i];
	}
	ret += ']';
	return ret;
};
*/
