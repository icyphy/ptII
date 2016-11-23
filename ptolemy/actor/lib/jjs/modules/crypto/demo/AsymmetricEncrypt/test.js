// Copyright (c) 2016 The Regents of the University of California.
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
"use strict"

var crypto = require('crypto');
var fs = require('fs');
var constants = require('constants');

var publicKey = fs.readFileSync('DemoCert.pem');
var privateKey = fs.readFileSync('DemoKey.pem');

var array = [-66, 8, -115, 73, -49, 42, 23, -36, 43, -108, 22, -28, -84, 57, -21, -9, 38, -18, -47, -91, 113, -30, 81, -56, 55, 19, -40, -66, 75, 126, 13, -6, 34, -10, -111, 40, -54, -41, 103, -18, -84, 54, 98, -10, 110, 6, -85, 54, 13, 35, -46, -108, -57, 75, 109, -11, -44, 127, -72, -14, 32, 77, -33, 61, 116, -17, -13, -39, 105, -104, 114, -47, -112, -27, -43, 46, 119, 121, 17, 35, -83, 38, -11, 61, 6, -110, -78, 113, -110, -72, -74, -3, -101, 49, -78, -65, 92, 108, -36, 114, -60, 6, 124, -90, -93, -57, 11, -27, 24, 118, -66, -106, 88, -74, 68, -11, -103, 76, 14, 69, -128, -11, 29, -44, -26, 24, -83, -90, 10, -116, 9, 75, -115, -60, 57, -57, 89, 111, 11, 66, 32, 5, -115, -97, -29, 50, -118, 15, 59, 83, -35, 15, 101, -80, -111, -12, 101, 108, 75, -18, 65, -124, 120, -8, -100, 16, 107, 127, -61, 83, -25, -40, 106, -128, 93, 106, 116, -16, -111, 96, 127, 73, 74, -6, -83, 14, -14, 107, -56, -69, -21, -36, -81, 69, 70, 10, -122, -62, 39, -86, 83, -102, 12, 115, 80, -52, 66, 36, -60, 5, -124, -102, -6, 22, 14, -111, -76, 49, -51, -58, -61, 37, 4, 1, -15, 98, 23, 43, -96, 79, -84, -79, -28, -128, 69, -107, -118, -59, -108, -50, -51, 70, 68, -28, 57, -94, 102, 15, 39, 16, 87, -14, -124, 64, -123, 14];
var buf = new Buffer(array);
console.log(buf.length);

var origin = crypto.privateDecrypt({
    key: privateKey,
    padding: constants.RSA_PKCS1_PADDING
}, buf);
console.log('origin');
console.log(origin);


var strBuf = new Buffer('hello world!');

var enc = crypto.publicEncrypt(publicKey, strBuf);
console.log('enc buf');
console.log(enc);

var dec = crypto.privateDecrypt(privateKey, enc);
console.log(dec);

var sign = crypto.createSign('RSA-SHA256');
sign.update(new Buffer('bye world!'));
var signature = sign.sign(privateKey);
console.log(signature.length);
console.log(signature);

var hmacKey = new Buffer('16611efd3f469b2d4b14e0a55926cece', 'hex');
var hmac = crypto.createHmac('sha1', hmacKey);
hmac.update('hello world!');
var hmacHash = hmac.digest();
console.log('hmacHash');
console.log(hmacHash);
