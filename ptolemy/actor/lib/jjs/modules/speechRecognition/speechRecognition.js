// Copyright (c) 2017 The Regents of the University of California.
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
 * A module to support speech recognition
 * @module speechRecognition
 * @author Christopher Brooks, based on speech-recognition.js by Beth Osyk
 * @version $$Id: udpSocket.js 75575 2016-12-29 05:17:30Z eal $$
 */

// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals Java, actor, exports, require, util */
/*jshint globalstrict: true */
"use strict";

// Java types used
var SpeechRecognitionHelper = Java.type('ptolemy.actor.lib.jjs.modules.speechRecognition.SpeechRecognitionHelper');

// TODO:  Should this be a singleton, since probably only one instance can 
// can access the microphone at a time?
var EventEmitter = require('events').EventEmitter;

/** Create a SpeechRecognition object.  
 * @param options.continuous true to operate in continuous mode, false to stop
 * recognition automatically after one phrase has been detected.
 */
exports.SpeechRecognition = function(options) {
	
    this.recognition = new SpeechRecognitionHelper(actor, this, options); 
    
        
	// TODO:  Implement options.  Right now this only works in continuous mode
	// for English.
};

util.inherits(exports.SpeechRecognition, EventEmitter);

/** Set options.
 * @param options.continuous true to operate in continuous mode, false to stop
 */
// TODO:  Set options.
exports.SpeechRecognition.prototype.setOptions = function(options) {
};

/** Start recognizing speech.
 * 
 */
exports.SpeechRecognition.prototype.start = function() {
    this.recognition.start();
};

/** Stop recognizing speech.  Note that speech recognition stops automatically 
 * after the first phrase in non-continuous mode.
 */
exports.SpeechRecognition.prototype.stop = function() {
    this.recognition.stop();
};
