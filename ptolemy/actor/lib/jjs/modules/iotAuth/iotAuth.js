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
exports.msgType = {
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

var AUTH_NONCE_SIZE = 8;					// used in parseAuthHello
var SESSION_KEY_ID_SIZE = 8;
var ABS_VALIDITY_SIZE = 6;
var REL_VALIDITY_SIZE = 6;
var DIST_CIPHER_KEY_SIZE = 16;               // 256 bit key = 32 bytes
var SESSION_CIPHER_KEY_SIZE = 16;            // 128 bit key = 16 bytes


var HANDSHAKE_NONCE_SIZE = 8;            // handshake nonce size
var SEQ_NUM_SIZE = 8;

exports.AUTH_NONCE_SIZE = AUTH_NONCE_SIZE;
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

exports.serializeSessionKeyReq = function(obj) {
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

exports.serializeSessionKeyReqWithDistributionKey = function(senderName,
    encryptedSessionKeyReqBuf) {
    var senderBuf = new buffer.Buffer(senderName);
    var lengthBuf = new buffer.Buffer(1);
    lengthBuf.writeUInt8(senderBuf.length);
    return buffer.concat([lengthBuf, senderBuf, encryptedSessionKeyReqBuf]);
};

exports.parseDistributionKey = function(buf) {
    var absValidity = new Date(buf.readUIntBE(0, ABS_VALIDITY_SIZE));
    var keyVal = buf.slice(ABS_VALIDITY_SIZE, ABS_VALIDITY_SIZE + DIST_CIPHER_KEY_SIZE);
    return {val: keyVal, absValidity: absValidity};
};

exports.parseSessionKey = function(buf) {
    var keyId = buf.readUIntBE(0, SESSION_KEY_ID_SIZE);
    var absValidityValue = buf.readUIntBE(SESSION_KEY_ID_SIZE, ABS_VALIDITY_SIZE);
    var absValidity = new Date(absValidityValue);
    var relValidity = buf.readUIntBE(SESSION_KEY_ID_SIZE + ABS_VALIDITY_SIZE, REL_VALIDITY_SIZE);
    var curIndex =  SESSION_KEY_ID_SIZE + ABS_VALIDITY_SIZE + REL_VALIDITY_SIZE;
    var keyVal = buf.slice(curIndex, curIndex + SESSION_CIPHER_KEY_SIZE);
    return {id: keyId, val: keyVal, absValidity: absValidity, relValidity: relValidity};
};

exports.parseSessionKeyResp = function(buf) {
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
        var sessionKey = exports.parseSessionKey(buf.slice(bufIdx));
        sessionKeyList.push(sessionKey);
        bufIdx += SESSION_KEY_BUF_SIZE;
    }
    return {replyNonce: replyNonce, sessionKeyList: sessionKeyList};
};

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

