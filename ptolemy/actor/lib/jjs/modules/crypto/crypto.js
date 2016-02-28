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

///////////////////////////////////////////////////////////////////////////////
//// supportedReceiveTypes

/** Return a random byte array.
 */
exports.randomBytes = function (size) {
    return this.helper.randomBytes(size);
};

/** Return a symmetric encrypted bytes.
 */
exports.symmetricEncrypt = function(input, key, cipherAlgorithm, messageDigestAlgorithm) {
    return this.helper.symmetricEncrypt(input, key, cipherAlgorithm, messageDigestAlgorithm);
};

/** Return a symmetric decrypted bytes.
 */
exports.symmetricDecrypt = function(input, key, cipherAlgorithm, messageDigestAlgorithm) {
    return this.helper.symmetricDecrypt(input, key, cipherAlgorithm, messageDigestAlgorithm);
};
