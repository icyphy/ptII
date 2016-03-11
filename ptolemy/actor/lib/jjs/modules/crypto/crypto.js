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
 * Module supporting crypto operations.
 *
 * @module crypto
 * @author Hokeun Kim
 * @version $$Id$$
 */

// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals actor, console, exports, Java, require, util */
/*jshint globalstrict: true */
"use strict";

var CryptoHelper = Java.type('ptolemy.actor.lib.jjs.modules.crypto.CryptoHelper');
var EventEmitter = require('events').EventEmitter;

this.helper = new CryptoHelper(this);

function arrayEquals(a, b) {
    if (a.length != b.length) {
        return false;
    }
    for (var i = 0; i < a.length; i++) {
        if (a[i] != b[i]) {
            return false;
        }
    }
    return true;
}

///////////////////////////////////////////////////////////////////////////////
//// supportedReceiveTypes

exports.getHashLength = function(hashAlgorithm) {
    return this.helper.getHashLength(hashAlgorithm);
}

exports.getKeySize = function(key) {
    return this.helper.getKeySize(key);
}

/** Calculate hash value using a secure hash function.
 */
exports.hash = function(input, hashAlgorithm) {
    return this.helper.hash(input, hashAlgorithm);
};

exports.hmac = function(input, key, hashAlgorithm) {
    return this.helper.hmac(input, key, hashAlgorithm);
};

exports.loadPrivateKey = function(filePath) {
    return this.helper.loadPrivateKey(filePath);
}

exports.loadPublicKey = function(filePath) {
    return this.helper.loadPublicKey(filePath);
}

exports.privateDecrypt = function(input, privateKey, cipherAlgorithm) {
    return this.helper.privateDecrypt(input, privateKey, cipherAlgorithm);
}

exports.publicEncrypt = function(input, publicKey, cipherAlgorithm) {
    return this.helper.publicEncrypt(input, publicKey, cipherAlgorithm);
}

exports.publicEncryptAndSign = function(input, publicKey, privateKey, cipherAlgorithm, signAlgorithm) {
    var encryptedData = exports.publicEncrypt(input, publicKey, cipherAlgorithm);
    var signature = exports.signWithPrivateKey(encryptedData, privateKey, signAlgorithm);
    return encryptedData.concat(signature);
};

/** Return a random byte array.
 */
exports.randomBytes = function (size) {
    return this.helper.randomBytes(size);
};

exports.signWithPrivateKey = function(input, privateKey, signAlgorithm) {
    return this.helper.signWithPrivateKey(input, privateKey, signAlgorithm);
}

/** Return a symmetric decrypted bytes.
 */
exports.symmetricDecrypt = function(input, key, cipherAlgorithm) {
    return this.helper.symmetricDecrypt(input, key, cipherAlgorithm);
};

exports.symmetricDecryptWithHash = function(input, key, cipherAlgorithm, hashAlgorithm) {
    var decryptedInput = this.helper.symmetricDecrypt(input, key, cipherAlgorithm);
    var hashLength = this.helper.getHashLength(hashAlgorithm);
    if (decryptedInput.length < hashLength) {
        return {data: null, hashOk: false};
    }
    var data = decryptedInput.slice(0, decryptedInput.length - hashLength);
    var hash = decryptedInput.slice(decryptedInput.length - hashLength);
    var computedHash = this.helper.hash(data, hashAlgorithm);
    var hashOk = arrayEquals(hash, computedHash);
    return {data: data, hashOk: hashOk};
};

/** Return a symmetric encrypted bytes.
 */
exports.symmetricEncrypt = function(input, key, cipherAlgorithm) {
    return this.helper.symmetricEncrypt(input, key, cipherAlgorithm);
};

exports.symmetricEncryptWithHash = function(input, key, cipherAlgorithm, hashAlgorithm) {
    var hash = this.helper.hash(input, hashAlgorithm);
    return this.helper.symmetricEncrypt(input.concat(hash), key, cipherAlgorithm);
};

exports.verifySignature = function(data, signature, publicKey, signAlgorithm) {
    return this.helper.verifySignature(data, signature, publicKey, signAlgorithm);
}