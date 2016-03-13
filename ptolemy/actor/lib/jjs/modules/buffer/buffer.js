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
 * Module supporting Buffer object.
 * This module's interface is similar to Buffer module in Node.js.
 *
 * @module buffer
 * @author Hokeun Kim
 * @version $$Id$$
 */

"use strict";

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

exports.Buffer.prototype.toString = function() {
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

exports.Buffer.prototype.get = function(offset) {
	return this.array[offset];
};

exports.Buffer.prototype.getArray = function() {
	return this.array;
};

exports.Buffer.prototype.slice = function(begin, end) {
	if (end == undefined) {
		end = this.array.length;
	}
	return new exports.Buffer(this.array.slice(begin, end));
};

exports.Buffer.prototype.equals = function(other) {
	if (this.array.length != other.length) {
		return false;
	}
	for (var i = 0; i < this.array.length; i++) {
		if (this.get(i) != other.get(i)) {
			return false;
		}
	}
	return true;
};

exports.Buffer.prototype.copy = function(target, targetStart, sourceStart, sourceEnd) {
	if (sourceEnd == undefined) {
		sourceEnd = this.array.length;
	}
	if (sourceStart == undefined) {
		sourceStart = 0;
	}
	if (targetStart == undefined) {
		targetStart = 0;
	}
	for (var i = 0; i < (sourceEnd - sourceStart); i++) {
		target.array[targetStart + i] = this.array[sourceStart + i];
	}
};

exports.Buffer.prototype.readUInt8 = function(offset) {
	return this.array[offset] & 255;
};

exports.Buffer.prototype.writeUInt8 = function(value, offset) {
	if (offset == undefined) {
		offset = 0;
	}
	this.array[offset] = (255 & value);
};

exports.Buffer.prototype.readUInt32BE = function(offset) {
	var value = 0;
	for (var i = offset; i < offset + 4; i++) {
		value *= 256;
		value += (this.array[i] & 255);
	}
	return value;
};

exports.Buffer.prototype.writeUInt32BE = function(value, offset) {
	if (offset == undefined) {
		offset = 0;
	}
	for (var i = offset + 3; i >= offset; i--) {
		this.array[i] = (255 & value);
		value >>= 8;
	}
};


exports.Buffer.prototype.readUIntBE = function(offset, size) {
	var value = 0;
	for (var i = offset; i < offset + size; i++) {
		value *= 256;
		value += (this.array[i] & 255);
	}
	return value;
};

exports.Buffer.prototype.writeUIntBE = function(value, offset, size) {
	if (offset == undefined) {
		offset = 0;
	}
	for (var i = offset + size - 1; i >= offset; i--) {
		this.array[i] = (255 & value);
		value >>= 8;
	}
};

exports.concat = function(bufList) {
	var totalLen = 0;
	for (var i = 0; i < bufList.length; i++) {
		totalLen += bufList[i].length;
	}
	var tempArray = new Array(totalLen);

	var k = 0;
	for (var i = 0; i < bufList.length; i++) {
		for (var j = 0; j < bufList[i].length; j++) {
			tempArray[k] = bufList[i].get(j);
			k++;
		}
	}
	return new exports.Buffer(tempArray);
}