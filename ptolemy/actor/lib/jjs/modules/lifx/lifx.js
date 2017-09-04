// Copyright (c) 2015-2017 The Regents of the University of California.
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

/** This module provides needed mechanisms to discover and control an available
 *  Lifx light bulbs in a local area network using Wifi. 
 *  While it is possible to interact with Lifx bulbs over the Internet by
 *  sending HTTP requests to the Cloud account, this module does not provide 
 *  such mechanism.
 *
 *  The communication with Lifx light bulbs is done over UDP/IP. Messages are 
 *  arrays of numeric bytes ordered in little-endian. The packets construction
 *  can be found is this link:
 *  https://lan.developer.lifx.com/docs/introduction 
 */

/** Create usiong new a Lifx light bulb object. The created object includes the following
 *  properties:
 *  * **ipAddress**: The IP address of the bulb in the Local Area Network.
 *  * **port**: The port number. This usually defaults to 56700.
 *  * **label**: The bulb's label, that was set by the user oduring the configuration.
 *  * **color**: The 
 *  * **power**: Boolean. Says if the light is on or off
 *
 */
exports.LifxLight = function (ipAddress, port, macAddress, label) {
	if (ipAddress && typeof ipAddress === 'string') {
		this.ipAddress = ipAddress;	
	} else {
		this.ipAddress = '';
	}

	if (port && typeof port === 'int') {
		this.port = port;	
	} else {
		this.port = 56700;
	}	

	if (macAddress && typeof macAddress === 'string') {
		this.macAddress = macAddress;	
	} else {
		this.macAddress = '';
	}

	if (label && typeof label === 'string') {
		this.label = label;	
	} else {
		this.label = '';
	}

	this.color = {};
	this.power = false;
}

/** Switch the light on. First, the packet options are set. Then, the
 *  packet is build as a string of hexadecimal numbers. Finally, the packet
 *  is converted to a byte array format and sent via the socket.
 *
 *  @param socket The socket used for sending the udp message
 */
exports.LifxLight.prototype.switchOn = function(socket) {
	// Set the options for switching the light on
	var options = {};
	options.size = '2a';
	options.ackRequired = 1; options.resRequired = 1;
	options.setPower = {}; options.setPower.on = true;

	// Build the hexadecimal packet and then convert it to an array of bytes
	var hexPacket = buildPacket.call(this, options);
	console.log('prePacket = ' + hexPacket);
	var packet = convertHexStringToByteArray(hexPacket);

	// Send the packet over the provided socket
	socket.send(packet, this.port, this.ipAddress, function () {
		console.log('Switch light on ' + this.macAddress + ' at ' + this.ipAddress + ':' + this.port + ' msg = ' + message);
	});
};

/** Switch the light off. First, the packet options are set. Then, the
 *  packet is build as a string of hexadecimal numbers. Finally, the packet
 *  is converted to a byte array format and sent via the socket.
 *
 *  @param socket The socket used for sending the udp message
 */
exports.LifxLight.prototype.switchOff = function(socket) {
	// Set the options for switching the light off
	var options = {};
	options.size = '2a';
	options.ackRequired = 1; options.resRequired = 1;
	options.setPower = {}; options.setPower.on = false;

	// Build the hexadecimal packet and then convert it to an array of bytes
	var hexPacket = buildPacket.call(this, options);
	var packet = convertHexStringToByteArray(hexPacket);

	// Send the packet over the provided socket
	socket.send(packet, this.port, this.ipAddress, function () {
		console.log('Switch light off ' + this.macAddress + ' at ' + this.ipAddress + ':' + this.port + ' msg = ' + message);
	});
};

/** Broadcasts UPD discovery messages. If Lifx bubls are in the network, they will 
 *  send there state.  
 * 
 *  @param socket The sicket instance to use for sending the discovery message
 */
exports.discoverLifx = function (socket) {
	// needs more elaboration
	var hexPacket = '240000341111111100000000000000000000000000000000000000000000000002000000';
	var packet = convertHexStringToByteArray(hexPacket);

	socket.send(packet, 56700, '255.255.255.255', function () {
		console.log('Start discovery: Broadcast at 255.255.255.255:56700... ');
	});
}

/** Returns a JSON object that describes the received message.
 *
 *  @param message The received message during listening.
 *  @return JSON object describing the device and the message features.
 */
exports.parseReceivedMessage = function (message) {
	var hexMessage = convertStringToHexString(message);
	var messageCode = hexMessage.substring(64, 2);
	var messageMacAddress = hexMessage.substring(16, 12);

	var response = {};

	// The received message is a State message (code 107)
	if (messageCode === '6b') { 
		//parsePayload();
	} else if (messageCode === '76') {
	// The received message is a State Power message (code 118)
		// parsePayload();	
		// under construction	
	} else if (messageCode === '79') {
	// The received message is a StateInfrared message (code 121)
		// parsePayload();	
		// under construction
	}
	// ...
	return hexMessage;
}

/** Convinience function for converting a string, which each character is an
 *  hexadecimal number to an array buffer of bytes. This latter will contain 
 *  unsigned bytes with the value of two consecutive characters from the provided
 *  string.
 *
 *  @param hexString String of hexadecimal values in each character
 *  @return converted hexString into ArrayBuffer
 */
var convertHexStringToByteArray = function (hexString) {
	var buffer = new ArrayBuffer();
	var i = 0;
	for (i = 0 ; i < hexString.length ; i=i+2 ) {
		var hs = hexString.slice(i, i+2);
		buffer[ i / 2] = (parseInt(hs, 16)) & 0xFF;
	}
	return buffer; 
}

/** Convinience function for converting a string str to another string hexString
 *  of hexadecimal characters. For this, each character of str is mapped into two
 *  characters that represent hexadecimal values.
 *
 *  @param str String of raw bytes.
 *  @return converted str to a string of hexa values.
 */
var convertStringToHexString = function (str) {
	var hexString = '';
	var i = 0;
	for (i = 0 ; i < str.length ; i++ ) {
		hex = str.charCodeAt(i).toString(16);
        hexString += ("000"+hex).slice(-2);
	}
	return hexString; 
}

/** Builds a UDP packet to be sent, based on the provided options.
 *  Please refer to https://lan.developer.lifx.com/docs/building-a-lifx-packet
 *  to know about the message format.
 *
 *  @param options A JSON object that describes the packet features
 *  @return UDP Packet to be sent 
 */
var buildPacket = function (options) {
	var packet = '';

	// ============================= Construct the header
    // ----------------------- Frame
    // -- size = 16bits
    packet += options.size + '00';
    // -- origin+tagged+addressable+protocol = 16bits
    if (options.toAll) {
    	packet += '0034';
    } else {
    	packet += '0014';
    }
    // -- source: set by the client (if all 0 then response broadcast) 32bits
    packet += '11111111';

	// ----------------------- Frame address
	// -- target mac address (48bits)+0000
	if (options.toAll) {
		packet += '000000000000' +'0000';
	} else {
		packet += this.macAddress + '0000';	
	}
	// -- reserved (48bits)
	packet += '000000000000';
	// -- reserved + ack_required + res_required (8bits);
	if (!options.ackRequired && !options.resRequired) {
		packet += '00';
	} else if (!options.resRequired) {
		packet += '02';
	} else {
		packet += '01';
	};
	// -- sequence (8bits): reference to the message
	if (options.sequence) {
		packet += ''+ options.sequence;
	} else {
		packet += '00';
	}

	// ----------------------- Protocol header
	// -- reserved (64bits)
	packet += '0000000000000000'; 
	// -- message type (16bits) + reserved (16bits)
	if (options.get) {
		packet += '6500' + '0000'; // Get --> 101
	} else if (options.setColor) { 
		packet += '6600' + '0000'; // SetColor --> 102
	} else 	if (options.getPower) {
		packet += '7400' + '0000'; // GetPower --> 116
	} else if (options.setPower) { 
		packet += '7500' + '0000'; // SetPower --> 117
	} else if (options.getInfrared) {
		packet += '7800' + '0000'; // GetInfrared --> 120
	} else if (options.setInfrared) { 
		packet += '7a00' + '0000'; // SetInfrared --> 122
	}

	// ============================= Construct the Payload
	if (options.setPower) {
		if (options.setPower.on) {
			packet += 'ffff00000000';
		} else {
			packet += '000000000000';
		}
	}

	if (options.setColor) {
		// under construction
	}

	if (options.setInfrared) {
		// under construction
	}

	return packet;
}
