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
 * The authorization service is provided by a local authorization entity, Auth,
 * whose open-source Java implementation can be found on a GitHub repository 
 * (https://github.com/iotauth/iotauth).
 *
 * @module iotAuth
 * @author Hokeun Kim
 * @version $$Id$$
 */

"use strict";

var buffer = require('buffer');
var crypto = require('crypto');
var socket = require('socket');

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

var AUTH_NONCE_SIZE = 8;                                        // used in parseAuthHello
var SESSION_KEY_ID_SIZE = 8;
var DIST_KEY_EXPIRATION_TIME_SIZE = 6;
var SESSION_KEY_EXPIRATION_TIME_SIZE = 6;
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

/*
    IoTSP (IoT Secure Protocol) Message
    {
        msgType: /UInt8/,
        payloadLen: /variable-length integer encoding/
        payload: /Buffer/
    }
*/
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

var parseAuthHello = function(buf) {
    var authId = buf.readUInt32BE(0);
    var nonce = buf.slice(4, 4 + AUTH_NONCE_SIZE);
    return {authId: authId, nonce: nonce};
};

var serializeSessionKeyReq = function(obj) {
    if (obj.nonce == undefined || obj.replyNonce == undefined || obj.sender == undefined
        || obj.purpose == undefined || obj.numKeysPerRequest == undefined) {
        console.log('Error: SessionKeyReq nonce or replyNonce '
            + 'or purpose or numKeysPerRequest is missing.');
        return;
    }
    var buf = new buffer.Buffer(AUTH_NONCE_SIZE * 2 + 5);
    obj.nonce.copy(buf, 0);
    obj.replyNonce.copy(buf, AUTH_NONCE_SIZE);
    buf.writeUInt32BE(obj.numKeysPerRequest, AUTH_NONCE_SIZE * 2);
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
    var absValidity = new Date(buf.readUIntBE(0, DIST_KEY_EXPIRATION_TIME_SIZE));
    var curIndex = DIST_KEY_EXPIRATION_TIME_SIZE;
    var cipherKeySize = buf.readUInt8(curIndex);
    curIndex += 1;
    var cipherKeyVal = buf.slice(curIndex, curIndex + cipherKeySize);
    curIndex += cipherKeySize;
    var macKeySize = buf.readUInt8(curIndex);
    curIndex += 1;
    var macKeyVal = buf.slice(curIndex, curIndex + macKeySize);
    return {
        cipherKeyVal: cipherKeyVal,
        macKeyVal: macKeyVal,
        absValidity: absValidity
    };
};

var parseSessionKey = function(buf) {
    var keyId = buf.readUIntBE(0, SESSION_KEY_ID_SIZE);
    var curIndex = SESSION_KEY_ID_SIZE;
    var absValidityValue = buf.readUIntBE(curIndex, SESSION_KEY_EXPIRATION_TIME_SIZE);
    curIndex += SESSION_KEY_EXPIRATION_TIME_SIZE;
    var absValidity = new Date(absValidityValue);
    var relValidity = buf.readUIntBE(curIndex, REL_VALIDITY_SIZE);
    curIndex += REL_VALIDITY_SIZE;
    var cipherKeySize = buf.readUInt8(curIndex);
    curIndex += 1;
    var cipherKeyVal = buf.slice(curIndex, curIndex + cipherKeySize);
    curIndex += cipherKeySize;
    var macKeySize = buf.readUInt8(curIndex);
    curIndex += 1;
    var macKeyVal = buf.slice(curIndex, curIndex + macKeySize);
    curIndex += macKeySize;
    var sessionKey = {
        id: keyId,
        cipherKeyVal: cipherKeyVal,
        macKeyVal: macKeyVal,
        absValidity: absValidity,
        relValidity: relValidity
    };
    return {sessionKey: sessionKey, totalLen: curIndex};
};

var parseSessionKeyResp = function(buf) {
    var replyNonce = buf.slice(0, AUTH_NONCE_SIZE);
    var curIndex = AUTH_NONCE_SIZE;
    
        var cryptoSpecLen = buf.readUInt8(curIndex);
        curIndex += 1;
        var cryptoSpecStr = buf.toString(curIndex, curIndex + cryptoSpecLen);
        curIndex += cryptoSpecLen;
        
    var sessionKeyCount = buf.readUInt32BE(curIndex);

    curIndex += 4;
    var sessionKeyList = [];

    for (var i = 0; i < sessionKeyCount; i++) {
        var ret = parseSessionKey(buf.slice(curIndex));
        var sessionKey = ret.sessionKey;
        sessionKeyList.push(sessionKey);
        curIndex += ret.totalLen;
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
var serializeHandshake = function(obj) {
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
var parseHandshake = function(buf) {
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
var serializeSessionMessage = function(obj) {
    if (obj.seqNum == undefined || obj.data == undefined) {
        console.log('Error: Secure session message seqNum or data is missing.');
        return;
    }
    var seqNumBuf = new buffer.Buffer(SEQ_NUM_SIZE);
    seqNumBuf.writeUIntBE(obj.seqNum, 0, SEQ_NUM_SIZE);
    return buffer.concat([seqNumBuf, obj.data]);
};
var parseSessionMessage = function(buf) {
    var seqNum = buf.readUIntBE(0, SEQ_NUM_SIZE);
    var data = buf.slice(SEQ_NUM_SIZE);
    return {seqNum: seqNum, data: data};
};

///////////////////////////////////////////////////////////////////
////            Wrapper functions for crypto module            ////

exports.loadPublicKey = function(path) {
    return crypto.loadPublicKey(path);
};

exports.loadPrivateKey = function(path) {
    return crypto.loadPrivateKey(path);
};

function symmetricEncryptAuthenticate(buf, symmetricKey, cryptoSpec) {
    var cryptoSpec = crypto.parseSymmetricCryptoSpec(cryptoSpec);
    return crypto.symmetricEncryptWithHash(buf.getArray(), 
        symmetricKey.cipherKeyVal.getArray(), symmetricKey.macKeyVal.getArray(),
        cryptoSpec.cipher, cryptoSpec.mac);
};

function symmetricDecryptVerify(buf, symmetricKey, cryptoSpec) {
    var cryptoSpec = crypto.parseSymmetricCryptoSpec(cryptoSpec);
    return crypto.symmetricDecryptWithHash(buf.getArray(), 
        symmetricKey.cipherKeyVal.getArray(), symmetricKey.macKeyVal.getArray(),
        cryptoSpec.cipher, cryptoSpec.mac);

};

///////////////////////////////////////////////////////////////////
////           Functions for accessing Auth service            ////

/*
options = {
    authHost,
    authPort,
    entityName,
    numKeysPerRequest,
    purpose,
    distributionKey = {val, absValidity},
    distributionCryptoSpec,
    publicKeyCryptoSpec,
    authPublicKey,
    entityPrivateKey
}
*/
// Helper function for common code in handing session key response
function processSessionKeyResponse(options, sessionKeyRespBuf, distributionKey, myNonce) {
    var ret = symmetricDecryptVerify(sessionKeyRespBuf, distributionKey, options.distributionCryptoSpec);
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
};

function handleSessionKeyResponse(options, obj, myNonce, sessionKeyResponseCallback, callbackParameters) {
    if (obj.msgType == msgType.SESSION_KEY_RESP_WITH_DIST_KEY) {
        console.log('received session key response with distribution key attached!');
        var distKeyBuf = obj.payload.slice(0, 512);
        var sessionKeyRespBuf = obj.payload.slice(512);
        var pubEncData = distKeyBuf.slice(0, 256).getArray();
        var signature = distKeyBuf.slice(256).getArray();
        var verified = crypto.verifySignature(pubEncData, signature, options.authPublicKey, options.publicKeyCryptoSpec.sign);
        if (!verified) {
            sessionKeyResponseCallback({error: 'Auth signature NOT verified'});
            return;
        }
        console.log('Auth signature verified');
        distKeyBuf = new buffer.Buffer(
            crypto.privateDecrypt(pubEncData, options.entityPrivateKey, options.publicKeyCryptoSpec.cipher));
        var receivedDistKey = parseDistributionKey(distKeyBuf);
        var ret = processSessionKeyResponse(options, sessionKeyRespBuf, receivedDistKey, myNonce);
        if (ret.error) {
            sessionKeyResponseCallback({error: ret.error});
            return;
        }
        sessionKeyResponseCallback({success: true}, receivedDistKey, ret.sessionKeyList, callbackParameters);
    }
    else if (obj.msgType == msgType.SESSION_KEY_RESP) {
        console.log('Received session key response encrypted with distribution key');
        var ret = processSessionKeyResponse(options, obj.payload, options.distributionKey, myNonce);
        if (ret.error) {
            sessionKeyResponseCallback({error: ret.error});
            return;
        }
        sessionKeyResponseCallback({success: true}, null, ret.sessionKeyList, callbackParameters);
    }
    else if (obj.msgType == msgType.AUTH_ALERT) {
        console.log('Received Auth alert!');
        var authAlert = parseAuthAlert(obj.payload);
        if (authAlert.code == authAlertCode.INVALID_DISTRIBUTION_KEY) {
            sessionKeyResponseCallback({error: 'Received Auth alert due to invalid distribution key'});
            return;
        }
        else if (authAlert.code == authAlertCode.INVALID_SESSION_KEY_REQ_TARGET) {
            sessionKeyResponseCallback({error: 'Received Auth alert due to invalid session key request target'});
            return;
        }
        else {
            sessionKeyResponseCallback({error: 'Received Auth alert with unknown code :' + authAlert.code});
            return;
        }
    }
    else {
        sessionKeyResponseCallback({error: 'Unknown message type :' + obj.msgType});
        return;
    }
};

exports.sendSessionKeyRequest = function(options, sessionKeyResponseCallback, callbackParameters) {
        options.publicKeyCryptoSpec = crypto.parsePublicKeyCryptoSpec(options.publicKeyCryptoSpec);
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
                        var authHello = parseAuthHello(obj.payload);
                        myNonce = new buffer.Buffer(crypto.randomBytes(AUTH_NONCE_SIZE));
                
            var sessionKeyReq = {
                nonce: myNonce,
                replyNonce: authHello.nonce,
                numKeysPerRequest: options.numKeysPerRequest,
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
                            options.publicKeyCryptoSpec.cipher, options.publicKeyCryptoSpec.sign));
            }
            else {
                console.log('distribution key available! ');
                reqMsgType = msgType.SESSION_KEY_REQ;
                var sessionKeyReqBuf = serializeSessionKeyReq(sessionKeyReq);
                                   var encryptedSessionKeyReqBuf = new buffer.Buffer(
                                           symmetricEncryptAuthenticate(sessionKeyReqBuf, options.distributionKey, options.distributionCryptoSpec));
                reqPayload = serializeSessionKeyReqWithDistributionKey(options.entityName,
                                encryptedSessionKeyReqBuf)
            }
            var toSend = exports.serializeIoTSP({msgType: reqMsgType, payload: reqPayload}).getArray();
            authClientSocket.send(toSend);
                }
                else {
                    handleSessionKeyResponse(options, obj, myNonce, sessionKeyResponseCallback, callbackParameters);
                    authClientSocket.close();
                }
    });
    authClientSocket.on('close', function() {
            console.log('disconnected from auth');
    });
    authClientSocket.on('error', function(message) {
        sessionKeyResponseCallback({error: 'an error occurred in socket during session key request, details: ' + message});
        authClientSocket.close();
    });
        authClientSocket.open();
};

///////////////////////////////////////////////////////////////////
////       List of available cryptography specifications       ////
exports.symmetricCryptoSpecs = ['AES-128-CBC:HmacSHA256',
'AES-192-CBC:HmacSHA256',
'AES-128-GCM'
];

exports.publicKeyCryptoSpecs = ['RSA/ECB/PKCS1PADDING:SHA256withRSA'];


///////////////////////////////////////////////////////////////////
////     Common socket class for entity server and client      ////

var IoTSecureSocket = function(socket, sessionKey, sessionCryptoSpec) {
    this.socket = socket;
    this.sessionKey = sessionKey;
    this.sessionCryptoSpec = sessionCryptoSpec;
    this.writeSeqNum = 0;
    this.readSeqNum = 0;
};

IoTSecureSocket.prototype.close = function() {
    if (this.socket) {
        this.socket.close();
    }
    this.socket = null;
};

IoTSecureSocket.prototype.checkSessionKeyValidity = function() {
    if (this.sessionKey.absValidity > new Date()) {
        return true;
    }
    return false;
};

// to be called from outside of iotAuth module
IoTSecureSocket.prototype.send = function(data) {
    if (!this.socket) {
        console.log('Internal socket is not available');
        return false;
    }
    if (!this.checkSessionKeyValidity()) {
        console.log('Session key expired!');
        return false;
    }
    var buf = serializeSessionMessage({seqNum: this.writeSeqNum, data: new buffer.Buffer(data)});
    var encBuf = new buffer.Buffer(symmetricEncryptAuthenticate(buf, this.sessionKey, this.sessionCryptoSpec));
    this.writeSeqNum++;
    var msg = {
        msgType: msgType.SECURE_COMM_MSG,
        payload: encBuf
    };
    var toSend = exports.serializeIoTSP(msg).getArray();
    this.socket.send(toSend);
    return true;
};

// to be called inside of iotAuth module
IoTSecureSocket.prototype.receive = function(payload) {
    if (!this.socket) {
        return {success: false, error: 'Internal socket is not available'};
    }
    if (!this.checkSessionKeyValidity()) {
        return {success: false, error: 'Session key expired!'};
    }
    var ret = symmetricDecryptVerify(payload, this.sessionKey, this.sessionCryptoSpec);
    if (!ret.hashOk) {
        return {success: false, error: 'Received hash for secure comm msg is NOT ok'};
    }
    console.log('Received hash for secure comm msg is ok');
    var buf = new buffer.Buffer(ret.data);
    ret = parseSessionMessage(buf);
    
    if (ret.seqNum != this.readSeqNum) {
        return {success: false, error: 'seqNum does not match! expected: ' + this.readSeqNum + ' received: ' + ret.seqNum};
    }
    this.readSeqNum++;
    console.log('Received seqNum: ' + ret.seqNum);
    return {success: true, data: ret.data};
};

IoTSecureSocket.prototype.inspect = function() {
    var ret = 'sessionKey: ' + this.sessionKey.toString();
    ret += ' writeSeqNum: '+ this.writeSeqNum;
    ret += ' readSeqNum: '+ this.readSeqNum;
    return ret;
};

///////////////////////////////////////////////////////////////////
////                Functions for entity client                ////

/*
options = {
    serverHost,
    serverPort,
    sessionKey = {id, val, absValidity},
    sessionCryptoSpec
}
*/
/*
eventHandlers = {
    onClose,
    onError,
    onData,
    onConnection
}
*/
exports.initializeSecureCommunication = function(options, eventHandlers) {
    if (options.sessionKey == null) {
        eventHandlers.onError('Comm init failed: No available key');
        return;
    }
    // client communication state
    var entityClientCommState = {
        IDLE: 0,
        HANDSHAKE_1_SENT: 10,
        IN_COMM: 30                    // Session message
    };
    var entityClientState = entityClientCommState.IDLE;
    var entityClientSocket = new socket.SocketClient(options.serverPort, options.serverHost,
    {
        //'connectTimeout' : this.getParameter('connectTimeout'),
        'discardMessagesBeforeOpen' : false,
        'emitBatchDataAsAvailable' : true,
        //'idleTimeout' : this.getParameter('idleTimeout'),
        //'keepAlive' : false,
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
    
    var myNonce;
    entityClientSocket.on('open', function() {
        console.log('connected to server');
        myNonce = new buffer.Buffer(crypto.randomBytes(HANDSHAKE_NONCE_SIZE));
        var handshake1 = {nonce: myNonce};
        var buf = serializeHandshake(handshake1);
        var encBuf = new buffer.Buffer(symmetricEncryptAuthenticate(buf, options.sessionKey, options.sessionCryptoSpec));
        
        var keyIdBuf = new buffer.Buffer(SESSION_KEY_ID_SIZE);
        keyIdBuf.writeUIntBE(options.sessionKey.id, 0, SESSION_KEY_ID_SIZE);
        var msg = {
            msgType: msgType.SKEY_HANDSHAKE_1,
            payload: buffer.concat([keyIdBuf, encBuf])
        };
        var toSend = exports.serializeIoTSP(msg).getArray();
        entityClientSocket.send(toSend);
        console.log('switching to HANDSHAKE_1_SENT');
        entityClientState = entityClientCommState.HANDSHAKE_1_SENT;
    });
    var expectingMoreData = false;
    var obj;
    var iotSecureSocket = null;
    entityClientSocket.on('data', function(data) {
        console.log('data received from server');
        var buf = new buffer.Buffer(data);
        if (!expectingMoreData) {
            obj = exports.parseIoTSP(buf);
            if (obj.payload.length < obj.payloadLen) {
                expectingMoreData = true;
                console.log('more data will come. current: ' + obj.payload.length
                    + ' expected: ' + obj.payloadLen);
            }
        }
        else {
            obj.payload = buffer.concat([obj.payload, new buffer.Buffer(data)]);
            if (obj.payload.length ==  obj.payloadLen) {
                expectingMoreData = false;
            }
            else {
                console.log('more data will come. current: ' + obj.payload.length
                    + ' expected: ' + obj.payloadLen);
            }
        }
        
        if (expectingMoreData) {
            // do not process the packet yet
            return;
        }
        else if (obj.msgType == msgType.SKEY_HANDSHAKE_2) {
            console.log('received session key handshake2!');
            if (entityClientState != entityClientCommState.HANDSHAKE_1_SENT) {
                eventHandlers.onError('Comm init failed: Wrong sequence of handshake, disconnecting...');
                entityClientSocket.close();
                return;
            }
            var ret = symmetricDecryptVerify(obj.payload, options.sessionKey, options.sessionCryptoSpec);
            if (!ret.hashOk) {
                eventHandlers.onError('Comm init failed: Received hash for handshake2 is NOT ok');
                entityClientSocket.close();
                return;
            }
            console.log('Received hash for handshake2 is ok');
            var buf = new buffer.Buffer(ret.data);
            var handshake2 = parseHandshake(buf);
            if (!handshake2.replyNonce.equals(myNonce)) {
                eventHandlers.onError('Comm init failed: Server nonce NOT verified');
                return;
            }
            console.log('Server nonce verified');
            var theirNonce = handshake2.nonce;
            var handshake3 = {replyNonce: theirNonce};
            buf = serializeHandshake(handshake3);
            var encBuf = new buffer.Buffer(symmetricEncryptAuthenticate(buf, options.sessionKey, options.sessionCryptoSpec));
            var msg = {
                msgType: msgType.SKEY_HANDSHAKE_3,
                payload: encBuf
            };
            entityClientSocket.send(exports.serializeIoTSP(msg).getArray());

            console.log('switching to IN_COMM');
            entityClientState = entityClientCommState.IN_COMM;
            // socket, sessionKey, cipherAlgorithm, hashAlgorithm
            iotSecureSocket = new IoTSecureSocket(entityClientSocket, options.sessionKey,
                options.sessionCryptoSpec);
            eventHandlers.onConnection(iotSecureSocket);
        }
        else if (obj.msgType == msgType.SECURE_COMM_MSG) {
            console.log('received secure communication message!');
            if (entityClientState == entityClientCommState.IN_COMM) {
                var ret = iotSecureSocket.receive(obj.payload);
                if (!ret.success) {
                    eventHandlers.onError(ret.error);
                    return;
                }
                eventHandlers.onData(ret.data);
                return;
            }
            else {
                eventHandlers.onError('Comm init failed: it is not in IN_COMM state, disconnecting...');
                entityClientSocket.close();
                return;
            }
        }
    });
    entityClientSocket.on('close', function() {
        if (entityClientState == entityClientCommState.IN_COMM) {
            eventHandlers.onClose();
            return;
        }
        eventHandlers.onError('Comm init failed: disconnected from server during communication initialization.');
    });
    entityClientSocket.on('error', function(message) {
        if (entityClientState == entityClientCommState.IN_COMM) {
            eventHandlers.onError(message);
            return;
        }
        eventHandlers.onError('Comm init failed: Error in comm init - details: ' + message);
        entityClientSocket.close();
        return;
    });
    entityClientSocket.open();
};

///////////////////////////////////////////////////////////////////
////                Functions for entity server                ////

/*
options = {
    serverPort,
    sessionCryptoSpec
}
*/
/*
eventHandlers = {
    onServerError,      // for server
    onServerListening,
    onClientRequest,    // for client's communication initialization request

    onClose,            // for individual sockets
    onError,
    onData,
    onConnection
}
*/
exports.initializeSecureServer = function(options, eventHandlers) {
    var entityServer;

    entityServer = new socket.SocketServer(
        {
            //'clientAuth' : this.getParameter('clientAuth'),
            'emitBatchDataAsAvailable' : true,
            //'hostInterface' : this.getParameter('hostInterface'),
            //'idleTimeout' : this.getParameter('idleTimeout'),
            //'keepAlive' : false,
            //'noDelay' : this.getParameter('noDelay'),
            //'pfxKeyCertPassword' : this.getParameter('pfxKeyCertPassword'),
            //'pfxKeyCertPath' : this.getParameter('pfxKeyCertPath'),
            'port' : options.serverPort,
            'rawBytes' : true,
            //'receiveBufferSize' : this.getParameter('receiveBufferSize'),
            'receiveType' : 'byte',
            //'sendBufferSize' : this.getParameter('sendBufferSize'),
            'sendType' : 'byte',
            //'sslTls' : this.getParameter('sslTls'),
            //'trustedCACertPath' : this.getParameter('trustedCACertPath')
        }
    );

    entityServer.on('error', function(message) {
        eventHandlers.onServerError(message);
    });
        
    entityServer.on('listening', function(listeningPort) {
        eventHandlers.onServerListening(listeningPort);
    });

    // server communication state
    var entityServerCommState = {
        IDLE: 0,
        WAITING_SESSION_KEY: 20,
        HANDSHAKE_1_RECEIVED: 21,
        HANDSHAKE_2_SENT: 22,
        IN_COMM: 30                    // Session message
    };
    var connectionCount = 0;
    entityServer.on('connection', function(entityServerSocket) {
        console.log('client connected');
        // entityServerSocket is an instance of the Socket class defined
        // in the socket module.
        var entityServerState = entityServerCommState.IDLE;
        var myNonce;
        var entityServerSessionKey;
        var socketID = -1;

        function sendHandshake2(handshake1Payload, serverSocket, sessionKey) {
            if (entityServerState != entityServerCommState.HANDSHAKE_1_RECEIVED) {
                eventHandlers.onServerError('Error during comm init - in wrong state, expected: HANDSHAKE_1_RECEIVED, disconnecting...');
                serverSocket.close();
                return;
            }
            var enc = handshake1Payload.slice(SESSION_KEY_ID_SIZE);
            entityServerSessionKey = sessionKey;
            var ret = symmetricDecryptVerify(enc, entityServerSessionKey, options.sessionCryptoSpec);
            if (!ret.hashOk) {
                eventHandlers.onServerError('Error during comm init - received hash for handshake 1 is NOT ok');
                serverSocket.close();
                return;
            }
            console.log('received hash for handshake 1 is ok');
            var buf = new buffer.Buffer(ret.data);
            
            var handshake1 = parseHandshake(buf);
            
            myNonce = new buffer.Buffer(crypto.randomBytes(HANDSHAKE_NONCE_SIZE));
            
            var theirNonce = handshake1.nonce;
            
            var handshake2 = {nonce: myNonce, replyNonce: theirNonce};
            
            var encBuf = symmetricEncryptAuthenticate(serializeHandshake(handshake2),
                entityServerSessionKey, options.sessionCryptoSpec);
            var msg = {
                msgType: msgType.SKEY_HANDSHAKE_2,
                payload: new buffer.Buffer(encBuf)
            };
            var toSend = exports.serializeIoTSP(msg).getArray();

            console.log('switching to HANDSHAKE_2_SENT state.');
            entityServerState = entityServerCommState.HANDSHAKE_2_SENT;
            
            serverSocket.send(toSend);
        };

        console.log('A client connected from ' +
            entityServerSocket.remoteHost() + ':' + entityServerSocket.remotePort());
        
        entityServerSocket.on('close', function() {
            if (entityServerState == entityServerCommState.IN_COMM) {
                eventHandlers.onClose(socketID);
                return;
            }
            else {
                eventHandlers.onServerError('Closed during comm init from ' +
                    entityServerSocket.remoteHost() + ':' + entityServerSocket.remotePort());
                entityServerSocket.close();
                return;
            }
        });
        
        entityServerSocket.on('error', function(message) {
            if (entityServerState == entityServerCommState.IN_COMM) {
                eventHandlers.onError(message, socketID);
                return;
            }
            else {
                eventHandlers.onServerError('Error during comm init, details: ' + message);
                entityServerSocket.close();
                return;
            }
        });
        
        var expectingMoreData = false;
        var obj;
        var iotSecureSocket = null;
        entityServerSocket.on('data', function(data) {
            console.log('received data from client');
            var buf = new buffer.Buffer(data);
            if (!expectingMoreData) {
                obj = exports.parseIoTSP(buf);
                if (obj.payload.length < obj.payloadLen) {
                    expectingMoreData = true;
                    console.log('more data will come. current: ' + obj.payload.length
                        + ' expected: ' + obj.payloadLen);
                }
            }
            else {
                obj.payload = buffer.concat([obj.payload, new buffer.Buffer(data)]);
                if (obj.payload.length ==  obj.payloadLen) {
                    expectingMoreData = false;
                }
                else {
                    console.log('more data will come. current: ' + obj.payload.length
                        + ' expected: ' + obj.payloadLen);
                }
            }
            
            if (expectingMoreData) {
                // do not process the packet yet
                return;
            }
            else if (obj.msgType == msgType.SKEY_HANDSHAKE_1) {
                console.log('received session key handshake1');
                if (entityServerState != entityServerCommState.IDLE) {
                    eventHandlers.onServerError('Error during comm init - in wrong state, expected: IDLE, disconnecting...');
                    entityServerSocket.close();
                    return;
                }
                console.log('switching to HANDSHAKE_1_RECEIVED state.');
                entityServerState = entityServerCommState.HANDSHAKE_1_RECEIVED;
                eventHandlers.onClientRequest(obj.payload, entityServerSocket, sendHandshake2);

            }
            else if (obj.msgType == msgType.SKEY_HANDSHAKE_3) {
                console.log('received session key handshake3');
                if (entityServerState != entityServerCommState.HANDSHAKE_2_SENT) {
                    eventHandlers.onServerError('Error during comm init - in wrong state, expected: HANDSHAKE_2_SENT, disconnecting...');
                    entityServerSocket.close();
                    return;
                }
                var ret = symmetricDecryptVerify(obj.payload, entityServerSessionKey, options.sessionCryptoSpec);
                if (!ret.hashOk) {
                    eventHandlers.onServerError('Error during comm init - received hash for handshake 3 is NOT ok, disconnecting...');
                    entityServerSocket.close();
                    return;
                }
                console.log('received hash for handshake 3 is ok');
                var buf = new buffer.Buffer(ret.data);
                var handshake3 = parseHandshake(buf);
                if (!handshake3.replyNonce.equals(myNonce)) {
                    eventHandlers.onServerError('Error during comm init - client nonce NOT verified');
                    entityServerSocket.close();
                    return;
                }
                console.log('client nonce verified');
                
                console.log('switching to IN_COMM state.');
                entityServerState = entityServerCommState.IN_COMM;
                iotSecureSocket = new IoTSecureSocket(entityServerSocket, entityServerSessionKey,
                    options.sessionCryptoSpec);

                socketID = connectionCount++;
                var socketInstance = {
                    'id': socketID,
                    'remoteHost': entityServerSocket.remoteHost(),
                    'remotePort': entityServerSocket.remotePort(),
                    'status': 'open'
                };
                eventHandlers.onConnection(socketInstance, iotSecureSocket);
            }
            else if (obj.msgType == msgType.SECURE_COMM_MSG) {
                if (entityServerState == entityServerCommState.IN_COMM) {
                    var ret = iotSecureSocket.receive(obj.payload);
                    if (!ret.success) {
                        eventHandlers.onError(ret.error, socketID);
                        return;
                    }
                    eventHandlers.onData(ret.data, socketID);
                    return;
                }
                else {
                    eventHandlers.onServerError('Error: it is not in IN_COMM state, disconnecting...');
                    entityServerSocket.close();
                    return;
                }
            }
        });
    });
    
    // Open the server after setting up all the handlers.
    entityServer.start();
    return entityServer;
};


///////////////////////////////////////////////////////////////////
////          Functions for secure publish-subscribe           ////

/*
    SecurePublishedMessage Format
    {
        keyId: /UIntBE/, // S_KEY_ID_SIZE Bytes - in plain text
        seqNum: /UIntBE/, SEQ_NUM_SIZE Bytes - encrypted
        data: /Buffer/, // data - encrypted
    }
*/
/*
    cryptoSpec          // 'cipher:hash'
*/
/*
    message = {
        sequenceNum,    // Integer
        data            // Buffer
    }
*/
exports.encryptSecureMessageToPublish = function(message, cryptoSpec, sessionKey) {
    var buf = serializeSessionMessage(
        {seqNum: message.sequenceNum, data: new buffer.Buffer(message.data)});
    var encBuf = new buffer.Buffer(symmetricEncryptAuthenticate(buf, sessionKey, cryptoSpec));

    var keyIdBuf = new buffer.Buffer(SESSION_KEY_ID_SIZE);
    keyIdBuf.writeUIntBE(sessionKey.id, 0, SESSION_KEY_ID_SIZE);
    var msg = {
        msgType: msgType.SECURE_PUB,
        payload: buffer.concat([keyIdBuf, encBuf])
    };
    return exports.serializeIoTSP(msg).getArray();
};
/*
    ret = {
        success,
        error,           // only when success == false
        keyId,
        encryptedMessage // Buffer
    }
*/
exports.getKeyIdOfSecurePublishedMessage = function(rawData) {
    var buf = new buffer.Buffer(rawData);

    var obj = exports.parseIoTSP(buf);
    if (obj.msgType != msgType.SECURE_PUB) {
        return {success: false, error: 'Not a secure published message'};
    }
    var keyId = obj.payload.readUIntBE(0, SESSION_KEY_ID_SIZE);
    return {success: true, keyId: keyId, encryptedMessage: obj.payload.slice(SESSION_KEY_ID_SIZE)};
};
/*
    ret = {
        success,
        error,           // only when success == false
        sequenceNum,
        message         // JavaScript array
    }
*/
exports.decryptSecurePublishedMessage = function(encryptedMessage, cryptoSpec, sessionKey) {
    var ret = symmetricDecryptVerify(encryptedMessage, sessionKey, cryptoSpec);
    if (!ret.hashOk) {
        return {success: false, error: 'Received hash for secure comm msg is NOT ok'};
    }
    console.log('Received hash for secure published message is ok');
    var buf = new buffer.Buffer(ret.data);
    ret = parseSessionMessage(buf);
    return {success: true, sequenceNum: ret.seqNum, message: ret.data.getArray()};
};

