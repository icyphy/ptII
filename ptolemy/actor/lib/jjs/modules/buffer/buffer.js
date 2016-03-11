"use strict";

exports.Buffer = function(param) {
	if (typeof param === 'number') {
		this.array = new Array(param);
		for (var i = 0; i < this.array.length; i++) {
			this.array[i] = 0;
		}
	}
	else if (Array.isArray(param)) {
		this.array = new Array(param.length);
		for (var i = 0; i < this.array.length; i++) {
			this.array[i] = param[i] & 127;
		}
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
	for (var i = 0; this.array.length; i++) {
		if (this.get(i) != other.get(i)) {
			return false;
		}
	}
	return true;
};

exports.Buffer.prototype.readUInt8 = function(offset) {
	return this.array[offset] & 127;
};

exports.Buffer.prototype.writeUInt8 = function(value, offset) {
	if (offset == undefined) {
		offset = 0;
	}
	this.array[offset] = (127 & value);
};

exports.Buffer.prototype.readUInt32BE = function(offset) {
	var value = 0;
	for (var i = offset; i < offset + 4; i++) {
		value |= this.array[offset] & 127;
		value <<= 8;
	}
	return value;
};

exports.Buffer.prototype.writeUInt32BE = function(value, offset) {
	if (offset == undefined) {
		offset = 0;
	}
	for (var i = offset + 4; i >= offset; i--) {
		this.array[offset] = (127 & value);
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