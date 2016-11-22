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

function unsigendByteArrayEquals(a, b) {
    if (a.length != b.length) {
        return false;
    }
    for (var i = 0; i < a.length; i++) {
        // to compare unsigned byte values
        if ((a[i] & 0xff) != (b[i] & 0xff)) {
            return false;
        }
    }
    return true;
}


///////////////////////////////////////////////////////////////////
////                        Secure Hash                        ////

exports.getHashLength = function (hashAlgorithm) {
    return this.helper.getHashLength(hashAlgorithm);
}

exports.getKeySize = function (key) {
    return this.helper.getKeySize(key);
}

/** Calculate hash value using a secure hash function.
 */
exports.hash = function (input, hashAlgorithm) {
    return this.helper.hash(input, hashAlgorithm);
};

exports.hmac = function (input, key, hashAlgorithm) {
    return this.helper.hmac(input, key, hashAlgorithm);
};

///////////////////////////////////////////////////////////////////
////                 Public key cryptography                   ////

exports.parsePublicKeyCryptoSpec = function (cryptoSpec) {
    var cryptoSpecTokens = cryptoSpec.split(':');
    return {
        cipher: cryptoSpecTokens[0],
        sign: cryptoSpecTokens[1]
    };
};

exports.loadPrivateKey = function (filePath) {
    return this.helper.loadPrivateKey(filePath);
}

exports.loadPublicKey = function (filePath) {
    return this.helper.loadPublicKey(filePath);
}

exports.privateDecrypt = function (input, privateKey, cipherAlgorithm) {
    return this.helper.privateDecrypt(input, privateKey, cipherAlgorithm);
}

exports.publicEncrypt = function (input, publicKey, cipherAlgorithm) {
    return this.helper.publicEncrypt(input, publicKey, cipherAlgorithm);
}

exports.publicEncryptAndSign = function (input, publicKey, privateKey, cipherAlgorithm, signAlgorithm) {
    var encryptedData = exports.publicEncrypt(input, publicKey, cipherAlgorithm);
    var signature = exports.signWithPrivateKey(encryptedData, privateKey, signAlgorithm);
    return encryptedData.concat(signature);
};

/** Return a random byte array.
 */
exports.randomBytes = function (size) {
    return this.helper.randomBytes(size);
};

exports.signWithPrivateKey = function (input, privateKey, signAlgorithm) {
    return this.helper.signWithPrivateKey(input, privateKey, signAlgorithm);
}

exports.verifySignature = function (data, signature, publicKey, signAlgorithm) {
    return this.helper.verifySignature(data, signature, publicKey, signAlgorithm);
}

///////////////////////////////////////////////////////////////////
////                   Symmetric Cryptography                  ////

exports.parseSymmetricCryptoSpec = function (cryptoSpec) {
    var cryptoSpecTokens = cryptoSpec.split(':');
    return {
        cipher: cryptoSpecTokens[0],
        mac: cryptoSpecTokens[1]
    };
};

/** Return a symmetric decrypted bytes.
 */
exports.symmetricDecrypt = function (input, key, cipherAlgorithm) {
    return this.helper.symmetricDecrypt(input, key, cipherAlgorithm);
};

exports.symmetricDecryptWithHash = function (input, cipherKeyVal, macKeyVal,
    cipherAlgorithm, macAlgorithm) {
    var hashLength = this.helper.getMacLength(macAlgorithm);
    if (input.length < hashLength) {
        return {
            data: null,
            hashOk: false
        };
    }
    var enc = input.slice(0, input.length - hashLength);
    var receivedTag = input.slice(input.length - hashLength);
    var computedTag = this.helper.hmac(enc, macKeyVal, macAlgorithm);
    var hashOk = unsigendByteArrayEquals(receivedTag, computedTag);
    if (!hashOk) {
        return {
            data: null,
            hashOk: false
        };
    }
    var data = this.helper.symmetricDecrypt(enc, cipherKeyVal, cipherAlgorithm);
    return {
        data: data,
        hashOk: hashOk
    };
};

/** Return a symmetric encrypted bytes.
 */
exports.symmetricEncrypt = function (input, key, cipherAlgorithm) {
    return this.helper.symmetricEncrypt(input, key, cipherAlgorithm);
};

exports.symmetricEncryptWithHash = function (input, cipherKeyVal, macKeyVal,
    cipherAlgorithm, macAlgorithm) {
    if (typeof input === 'string') {
        var temp = [];
        if (input.startsWith('0x')) {
            for (var i = 2; i < (input.length - 1); i += 2) {
                temp.push(parseInt(input.substring(i, i + 2), 16));
            }
        } else {
            for (var i = 0; i < input.length; i++) {
                temp.push(input.charCodeAt(i));
            }
        }
        input = temp;
    }
    var enc = this.helper.symmetricEncrypt(input, cipherKeyVal, cipherAlgorithm);
    var tag = this.helper.hmac(enc, macKeyVal, macAlgorithm);
    return enc.concat(tag);
};
