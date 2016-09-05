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

var buffer = require('buffer');

// Message types
var msgType = {
    AUTH_HELLO: 0,
    AUTH_SESSION_KEY_REQ: 10,
    AUTH_SESSION_KEY_RESP: 11,
    SESSION_KEY_REQ_IN_PUB_ENC: 20,
    /** Includes distribution message (session keys) */
    SESSION_KEY_RESP_WITH_DIST_KEY: 21,
    /** Distribution message */
    SESSION_KEY_REQ: 22,
    /** Distribution message */
    SESSION_KEY_RESP: 23,
    SKEY_HANDSHAKE_1: 30,
    SKEY_HANDSHAKE_2: 31,
    SKEY_HANDSHAKE_3: 32,
    SECURE_COMM_MSG: 33,
    FIN_SECURE_COMM: 34,
    SECURE_PUB: 40,
    AUTH_ALERT: 100
};

var authAlertCode = {
    INVALID_DISTRIBUTION_KEY: 0,
    INVALID_SESSION_KEY_REQ_TARGET: 1
};

exports.msgType = msgType;

var AUTH_NONCE_SIZE = 8;					// used in parseAuthHello
var SESSION_KEY_ID_SIZE = 8;
var ABS_VALIDITY_SIZE = 6;
var REL_VALIDITY_SIZE = 6;
var DIST_CIPHER_KEY_SIZE = 16;               // 256 bit key = 32 bytes
var SESSION_CIPHER_KEY_SIZE = 16;            // 128 bit key = 16 bytes
var AUTH_ALERT_CODE_SIZE = 1;


var HANDSHAKE_NONCE_SIZE = 8;            // handshake nonce size
var SEQ_NUM_SIZE = 8;

exports.SESSION_KEY_ID_SIZE = SESSION_KEY_ID_SIZE;
exports.HANDSHAKE_NONCE_SIZE = HANDSHAKE_NONCE_SIZE;

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


///////////////////////////////////////////////////////////////////
////            Functions for Auth packet handling             ////

exports.parseAuthHello = function(buf) {
    var authId = buf.readUInt32BE(0);
    var nonce = buf.slice(4, 4 + AUTH_NONCE_SIZE);
    return {authId: authId, nonce: nonce};
};

var serializeSessionKeyReq = function(obj) {
    if (obj.nonce == undefined || obj.replyNonce == undefined || obj.sender == undefined
        || obj.purpose == undefined || obj.numKeys == undefined) {
        console.log('Error: SessionKeyReq nonce or replyNonce '
            + 'or purpose or numKeys is missing.');
        return;
    }
    var buf = new buffer.Buffer(AUTH_NONCE_SIZE * 2 + 5);
    obj.nonce.copy(buf, 0);
    obj.replyNonce.copy(buf, AUTH_NONCE_SIZE);
    buf.writeUInt32BE(obj.numKeys, AUTH_NONCE_SIZE * 2);
    buf.writeUInt8(obj.sender.length, AUTH_NONCE_SIZE * 2 + 4);

    var senderBuf = new buffer.Buffer(obj.sender);
    var purposeBuf = new buffer.Buffer(JSON.stringify(obj.purpose));
    return buffer.concat([buf, senderBuf, purposeBuf]);
};

var serializeSessionKeyReqWithDistributionKey = function(senderName,
    encryptedSessionKeyReqBuf) {
    var senderBuf = new buffer.Buffer(senderName);
    var lengthBuf = new buffer.Buffer(1);
    lengthBuf.writeUInt8(senderBuf.length);
    return buffer.concat([lengthBuf, senderBuf, encryptedSessionKeyReqBuf]);
};

var parseDistributionKey = function(buf) {
    var absValidity = new Date(buf.readUIntBE(0, ABS_VALIDITY_SIZE));
    var keyVal = buf.slice(ABS_VALIDITY_SIZE, ABS_VALIDITY_SIZE + DIST_CIPHER_KEY_SIZE);
    return {val: keyVal, absValidity: absValidity};
};

var parseSessionKey = function(buf) {
    var keyId = buf.readUIntBE(0, SESSION_KEY_ID_SIZE);
    var absValidityValue = buf.readUIntBE(SESSION_KEY_ID_SIZE, ABS_VALIDITY_SIZE);
    var absValidity = new Date(absValidityValue);
    var relValidity = buf.readUIntBE(SESSION_KEY_ID_SIZE + ABS_VALIDITY_SIZE, REL_VALIDITY_SIZE);
    var curIndex =  SESSION_KEY_ID_SIZE + ABS_VALIDITY_SIZE + REL_VALIDITY_SIZE;
    var keyVal = buf.slice(curIndex, curIndex + SESSION_CIPHER_KEY_SIZE);
    return {id: keyId, val: keyVal, absValidity: absValidity, relValidity: relValidity};
};

var parseSessionKeyResp = function(buf) {
    var replyNonce = buf.slice(0, AUTH_NONCE_SIZE);
    var bufIdx = AUTH_NONCE_SIZE;
    
	var cryptoSpecLen = buf.readUInt8(bufIdx);
	bufIdx += 1;
	var cryptoSpecStr = buf.toString(bufIdx, bufIdx + cryptoSpecLen);
	bufIdx += cryptoSpecLen;
	
    var sessionKeyCount = buf.readUInt32BE(bufIdx);

    bufIdx += 4;
    var sessionKeyList = [];

    var SESSION_KEY_BUF_SIZE = SESSION_KEY_ID_SIZE + ABS_VALIDITY_SIZE + REL_VALIDITY_SIZE + SESSION_CIPHER_KEY_SIZE;
    for (var i = 0; i < sessionKeyCount; i++) {
        var sessionKey = parseSessionKey(buf.slice(bufIdx));
        sessionKeyList.push(sessionKey);
        bufIdx += SESSION_KEY_BUF_SIZE;
    }
    return {replyNonce: replyNonce, sessionKeyList: sessionKeyList};
};

var parseAuthAlert = function(buf) {
    var code = buf.readUIntBE(0, AUTH_ALERT_CODE_SIZE);
    return {code: code};
}

///////////////////////////////////////////////////////////////////
////           Functions for Entity packet handling            ////
/*
    Handshake Format
    {
        nonce: /Buffer/, // encrypted, may be undefined
        replyNonce: /Buffer/, // encrypted, may be undefined
    }
*/
exports.serializeHandshake = function(obj) {
    if (obj.nonce == undefined && obj.replyNonce == undefined) {
        console.log('Error: handshake should include at least on nonce.');
        return;
    }
    var buf = new buffer.Buffer(1 + HANDSHAKE_NONCE_SIZE * 2);

    // indicates existance of nonces
    var indicator = 0;
    if (obj.nonce != undefined) {
        indicator += 1;
        obj.nonce.copy(buf, 1);
    }
    if (obj.replyNonce != undefined) {
        indicator += 2;
        obj.replyNonce.copy(buf, 1 + HANDSHAKE_NONCE_SIZE);
    }
    buf.writeUInt8(indicator, 0);

    return buf;
};

// buf should be just the unencrypted part
exports.parseHandshake = function(buf) {
    var obj = {};
    var indicator = buf.readUInt8(0);
    if ((indicator & 1) != 0) {
        // nonce exists
        obj.nonce = buf.slice(1, 1 + HANDSHAKE_NONCE_SIZE);
    }
    if ((indicator & 2) != 0) {
        // replayNonce exists
        obj.replyNonce = buf.slice(1 + HANDSHAKE_NONCE_SIZE, 1 + HANDSHAKE_NONCE_SIZE * 2);
    }
    return obj;
};

/*
    SecureSessionMessage Format
    {
        SeqNum: /Buffer/, // UIntBE, SEQ_NUM_SIZE Bytes
        data: /Buffer/,
    }
*/
exports.serializeSessionMessage = function(obj) {
    if (obj.seqNum == undefined || obj.data == undefined) {
        console.log('Error: Secure session message seqNum or data is missing.');
        return;
    }
    var seqNumBuf = new buffer.Buffer(SEQ_NUM_SIZE);
    seqNumBuf.writeUIntBE(obj.seqNum, 0, SEQ_NUM_SIZE);
    return buffer.concat([seqNumBuf, obj.data]);
};
exports.parseSessionMessage = function(buf) {
    var seqNum = buf.readUIntBE(0, SEQ_NUM_SIZE);
    var data = buf.slice(SEQ_NUM_SIZE);
    return {seqNum: seqNum, data: data};
};

///////////////////////////////////////////////////////////////////
////           Functions for accessing Auth service            ////

var socket = require('socket');
var crypto = require('crypto');

/*
options = {
	authHost,
	authPort,
	entityName,
	numKeys,
	purpose,
	distributionKey = {val, absValidity},
	distCipher,
	distHash,
	publicCipher,
	signAlgorithm,
	authPublicKey,
	entityPrivateKey
}
*/
// Helper function for common code in handing session key response
function processSessionKeyResp(options, sessionKeyRespBuf, distributionKeyVal, myNonce) {
    var ret = crypto.symmetricDecryptWithHash(sessionKeyRespBuf.getArray(),
        distributionKeyVal.getArray(), options.distCipherAlgorithm, options.distHashAlgorithm);
    if (!ret.hashOk) {
        return {error: 'Received hash for session key resp is NOT ok'};
    }
    console.log('Received hash for session key resp is ok');
    sessionKeyRespBuf = new buffer.Buffer(ret.data);
    var sessionKeyResp = parseSessionKeyResp(sessionKeyRespBuf);
    if (!sessionKeyResp.replyNonce.equals(myNonce)) {
        return {error: 'Auth nonce NOT verified'};
    }
    console.log('Auth nonce verified');
    return {sessionKeyList: sessionKeyResp.sessionKeyList};
}

function handleSessionKeyResp(options, obj, myNonce, callback) {
    if (obj.msgType == msgType.SESSION_KEY_RESP_WITH_DIST_KEY) {
        console.log('received session key response with distribution key attached!');
        var distKeyBuf = obj.payload.slice(0, 512);
        var sessionKeyRespBuf = obj.payload.slice(512);
        var pubEncData = distKeyBuf.slice(0, 256).getArray();
        var signature = distKeyBuf.slice(256).getArray();
        var verified = crypto.verifySignature(pubEncData, signature, options.authPublicKey, options.signAlgorithm);
        if (!verified) {
            callback({error: 'Auth signature NOT verified'});
            return;
        }
        console.log('Auth signature verified');
        distKeyBuf = new buffer.Buffer(
            crypto.privateDecrypt(pubEncData, options.entityPrivateKey, options.publicCipherAlgorithm));
        var receivedDistKey = parseDistributionKey(distKeyBuf);
        var ret = processSessionKeyResp(options, sessionKeyRespBuf, receivedDistKey.val, myNonce);
        if (ret.error) {
            callback({error: ret.error});
            return;
        }
        callback({success:true}, receivedDistKey, ret.sessionKeyList);
    }
    else if (obj.msgType == msgType.SESSION_KEY_RESP) {
        console.log('Received session key response encrypted with distribution key');
        var ret = processSessionKeyResp(options, obj.payload, options.distributionKey.val, myNonce);
        if (ret.error) {
            callback({error: ret.error});
            return;
        }
        callback({success:true}, null, ret.sessionKeyList);
    }
    else if (obj.msgType == msgType.AUTH_ALERT) {
        console.log('Received Auth alert!');
        var authAlert = parseAuthAlert(obj.payload);
        if (authAlert.code == authAlertCode.INVALID_DISTRIBUTION_KEY) {
            callback({error: 'Received Auth alert due to invalid distribution key'});
            return;
        }
        else if (authAlert.code == authAlertCode.INVALID_SESSION_KEY_REQ_TARGET) {
            callback({error: 'Received Auth alert due to invalid session key request target'});
            return;
        }
        else {
            callback({error: 'Received Auth alert with unknown code :' + authAlert.code});
            return;
        }
    }
    else {
        callback({error: 'Unknown message type :' + obj.msgType});
        return;
    }
};

exports.sendSessionKeyReq = function(options, callback) {
    var authClientSocket = new socket.SocketClient(options.authPort, options.authHost,
    {
        //'connectTimeout' : this.getParameter('connectTimeout'),
        'discardMessagesBeforeOpen' : false,
        'emitBatchDataAsAvailable' : true,
        //'idleTimeout' : this.getParameter('idleTimeout'),
        'keepAlive' : false,
        //'maxUnsentMessages' : this.getParameter('maxUnsentMessages'),
        //'noDelay' : this.getParameter('noDelay'),
        //'pfxKeyCertPassword' : this.getParameter('pfxKeyCertPassword'),
        //'pfxKeyCertPath' : this.getParameter('pfxKeyCertPath'),
        'rawBytes' : true,
        //'receiveBufferSize' : this.getParameter('receiveBufferSize'),
        'receiveType' : 'byte',
        //'reconnectAttempts' : this.getParameter('reconnectAttempts'),
        //'reconnectInterval' : this.getParameter('reconnectInterval'),
        //'sendBufferSize' : this.getParameter('sendBufferSize'),
        'sendType' : 'byte',
        //'sslTls' : this.getParameter('sslTls'),
        //'trustAll' : this.getParameter('trustAll'),
        //'trustedCACertPath' : this.getParameter('trustedCACertPath')
    });
    authClientSocket.on('open', function() {
    	console.log('connected to auth');
    });
    var myNonce;
    authClientSocket.on('data', function(data) {
    	console.log('data received from auth');
		var buf = new buffer.Buffer(data);
		var obj = exports.parseIoTSP(buf);
		if (obj.msgType == msgType.AUTH_HELLO) {
			var authHello = exports.parseAuthHello(obj.payload);
			myNonce = new buffer.Buffer(crypto.randomBytes(AUTH_NONCE_SIZE));
		
            var sessionKeyReq = {
                nonce: myNonce,
                replyNonce: authHello.nonce,
                numKeys: options.numKeys,
                sender: options.entityName,
                purpose: options.purpose
            };
            var reqMsgType;
            var reqPayload;
            if (options.distributionKey == null || options.distributionKey.absValidity < new Date()) {
                if (options.distributionKey != null) {
                    console.log('Distribution key expired, requesting new distribution key as well...');
                }
                else {
                    console.log('No distribution key yet, requesting new distribution key as well...');
                }
                reqMsgType = msgType.SESSION_KEY_REQ_IN_PUB_ENC;
	            var sessionKeyReqBuf = serializeSessionKeyReq(sessionKeyReq);
	            reqPayload = new buffer.Buffer(
	            	crypto.publicEncryptAndSign(sessionKeyReqBuf.getArray(),
	            	options.authPublicKey, options.entityPrivateKey,
	            	options.publicCipherAlgorithm, options.signAlgorithm));
            }
            else {
                console.log('distribution key available! ');
                reqMsgType = msgType.SESSION_KEY_REQ;
                var sessionKeyReqBuf = serializeSessionKeyReq(sessionKeyReq);
   				var encryptedSessionKeyReqBuf = new buffer.Buffer(
   					crypto.symmetricEncryptWithHash(sessionKeyReqBuf.getArray(), 
   					options.distributionKey.val.getArray(), options.distCipherAlgorithm, options.distHashAlgorithm));
                reqPayload = serializeSessionKeyReqWithDistributionKey(options.entityName,
                		encryptedSessionKeyReqBuf)
            }
            var toSend = exports.serializeIoTSP({msgType: reqMsgType, payload: reqPayload}).getArray();
            authClientSocket.send(toSend);
		}
		else {
	    	handleSessionKeyResp(options, obj, myNonce, callback);
	    	authClientSocket.close();
		}
    });
    authClientSocket.on('close', function() {
    	console.log('disconnected from auth');
    });
    authClientSocket.on('error', function(message) {
    	console.log('an error occurred');
        self.error(message);
    });
	authClientSocket.open();
};

