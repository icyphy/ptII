// Below is the copyright agreement for the Ptolemy II system.
//
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
//
// Ptolemy II includes the work of others, to see those copyrights, follow
// the copyright link on the splash page or see copyright.htm.

/**
 * Module to access audio hardware on the host.
 * FIXME: This is very incomplete! Just a placeholder for now.
 * @module audio
 * @author Edward A. Lee
 */

// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals Java, error, exports */
/*jshint globalstrict: true*/
"use strict";

var AudioHelper = Java.type('ptolemy.actor.lib.jjs.modules.audio.AudioHelper');
var EventEmitter = require('events').EventEmitter;

// Reference to the Java class documented at:
//    http://terra.eecs.berkeley.edu:8080/job/ptII/javadoc/ptolemy/media/javasound/LiveSound.html
var LiveSound = Java.type('ptolemy.media.javasound.LiveSound');
// Clip playback uses javafx instead of Ptolemy SoundReader since javafx supports mp3
var AudioClip = Java.type('javafx.scene.media.AudioClip');

/** Return an array of capture formats currently available
 *  on the current host. This includes at least 'raw'.
 *  @return An array of capture formats.
 */
exports.outputFormats = function () {
    // The Java.from() Nashorn extension converts a Java array into a JavaScript array.
    return Java.from(AudioHelper.outputFormats());
};


/** Construct an instance of an Player object type. This should be instantiated in your
 *  JavaScript code as
 *  <pre>
 *     var audio = require("audio");
 *     var player = new audio.Player();
 *  </pre>
 *  An instance of this object type implements the following functions:
 *  (FIXME: replace with your design)
 *  <ul>
 *  <li> play(data): Play the specified array.
 *  <li> stop(): Stop playback and free the audio resources.
 *  </ul>
 *  @param options A JSON object with fields 'FIXME' and 'FIXME' that give the
 *   FIXME properties of the audio such as sample rate, etc. Provide reasonable
 *   defaults.
 */
exports.Player = function (options) {
    // Provide default values for options using the following common JavaScript idiom.
    options = options || {};
    this.foo = options.foo || 80;

    LiveSound.setSampleRate(8000);
    // Start playback.
    LiveSound.startPlayback(this);
};

/** Play audio data.
 *  @param data An array of numbers in the range -1 to 1 to be played.
 */
exports.Player.prototype.play = function (data) {
    // NOTE: Convert array into 2-D array required by LiveSound.
    LiveSound.putSamples(this, [data]);
};

/** Stop the player and free audio resources. */
exports.Player.prototype.stop = function () {
    LiveSound.stopPlayback(this);
};


/** Construct an instance of a ClipPlayer object type.  A ClipPlayer plays
 * audio from a URL source. This should be instantiated in your JavaScript code as:
 *  <pre>
 *     var audio = require("audio");
 *     var player = new audio.ClipPlayer();
 *  </pre>
 *  An instance of this object type implements the following functions:
 *  <ul>
 *  <li> load(url) : Load audio from the specified url.
 *  <li> play(): Play the audio from the previously loaded url.
 *  <li> stop(): Stop playback.
 *  </ul>
 */

/** Create a ClipPlayer.
 */
exports.ClipPlayer = function () {
    this.clip = null;
};

/** Load audio from the specified URL.
 * @param url  The URL to load audio from.
 */
exports.ClipPlayer.prototype.load = function (url) {
    try {
        this.clip = new AudioClip(url);
    } catch (err) {
        error("Error connecting to audio URL " + url);
    }
};

/** Play the currently loaded audio clip.
 */
exports.ClipPlayer.prototype.play = function () {
    if (this.clip !== null) {
        this.clip.play();
    } else {
        error("No audio clip to play.  Please load a url first.");
    }
};

/** Stop playback.
 */
exports.ClipPlayer.prototype.stop = function () {
    if (this.clip !== null) {
        this.clip.stop();
    }
};

/** Construct an instance of a Capture object type. This should be instantiated in your
 *  JavaScript code as
 *  <pre>
 *     var audio = require("audio");
 *     var capture = new audio.Capture();
 *  </pre>
 *  An instance of this object type implements the following functions:
 *  <ul>
 *  <li> capture.start(): Start capturing.
 *  <li> stop(): Stop capturing.
 *  </ul>
 *  This is an event emitter that will emit an event "capture"
 *  when audio data has been captured. The argument passed
 *  to a listener for that event will be audio data in a format
 *  given by the _outputFormat_ argument, which is one of the
 *  following strings:
 *  
 *  * "raw": The argument is a byte array representing audio data exactly as
 *    captured in the specified capture format.
 *  * "array": The audio data is converted into arrays of numbers (one per
 *    channel), where each number is in the range from -1.0 to 1.0.
 *    The argument passed to the "capture" event is an array of arrays,
 *    where the first index specifies the channel number.
 *  * "samples": The audio data is converted into numbers, where
 *    each number is in the range from -1.0 to 1.0, and each individual sample
 *    is emitted as a "capture" event with an argument that is
 *    a number (if there is only one channel) or
 *    as an array of numbers (if there is more than one channel).
 *    Note that this format introduces quite a lot of overhead, and using
 *    it may prevent you from capturing continuous audio.
 *  * "aiff": The audio data is converted into the AIFF file format historically
 *    associated with Apple computers.
 *  * "aifc": The audio data is converted into the AIFF-C, a compressed version
 *    of AIFF.
 *  * "au": The audio data is converted into the AU file format historically
 *    associated with Sun Microsystems and Unix computers.
 *  * "wav": The audio data is converted into the WAVE file format historically
 *    associated with Windows PCs.
 *    
 *  The optional _captureFormat_ argument is an object with the following properties,
 *  all of which are optional:
 *  
 *  * _bitsPerSample_: The number of bits per sample. This is an integer that
 *    defaults to 16.
 *  * _channels_: The number of channels. This defaults to 1.
 *  * _sampleRate_: The sample rate. This is an integer that defaults to 8000.
 *  
 *  @param captureTime The length of time for each audio capture (in milliseconds).
 *   This is an integer that defaults to 1000, capturing 1 second of audio at a time.
 *  @param outputFormat The format of the data passed to the "capture" event
 *   listeners as specified above.
 */
exports.Capture = function (captureTime, outputFormat, captureFormat) {
    this.helper = new AudioHelper(actor, this);

    captureFormat = captureFormat || {};
    captureFormat.bitsPerSample = captureFormat.bitsPerSample || 16;
    captureFormat.channels = captureFormat.channels || 1;
    captureFormat.sampleRate = captureFormat.sampleRate || 8000;

    this.helper.setCaptureParameters(captureFormat, captureTime, outputFormat);
    
    this.outputFormat = outputFormat;
};
util.inherits(exports.Capture, EventEmitter);

/** Start the capture. */
exports.Capture.prototype.start = function () {
    this.helper.startCapture();
};

/** Stop the capture and free audio resources. */
exports.Capture.prototype.stop = function () {
    this.helper.stopCapture();
};

/** Callback function used by the helper to deliver data.
 *  @param audioData The audio data.
 */
exports.Capture.prototype._captureData = function(audioData) {
    if (this.outputFormat == 'array') {
        // Use Nashorn-specific conversion to convert to a JavaScript array.
        var channels = Java.from(audioData);
        for (var i = 0; i < channels.length; i++) {
            channels[i] = Java.from(channels[i]);
        }
        this.emit('capture', channels);
    } else if (this.outputFormat == 'samples') {
        if (audioData.length === 1) {
            for (var i = 0; i < audioData[0].length; i++) {
                this.emit('capture', audioData[0][i]);
            }
        } else {
            // Assume all channels have the same length.
            for (var i = 0; i < audioData[0].length; i++) {
                var output = [];
                for (var j = 0; j < audioData.length; j++) {
                    output[j] = audioData[j][i];
                }
                this.emit('capture', output);
            }
        }
    } else {
        // Emit a byte array.
        this.emit('capture', Java.from(audioData));
    }
}
