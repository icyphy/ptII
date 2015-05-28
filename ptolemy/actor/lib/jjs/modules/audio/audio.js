/**
 * Module to access audio hardware on the host.
 * @module audio
 * @author FIXME
 * @copyright http://terraswarm.org/accessors/copyright.txt
 */
 
// Reference to the Java class documented at:
//    http://terra.eecs.berkeley.edu:8080/job/ptII/javadoc/ptolemy/media/javasound/LiveSound.html
var LiveSound = Java.type('ptolemy.media.javasound.LiveSound');

// This file contains a template for a CommonJS module.
// It is to be completed by you.

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
exports.Player = function(options) {
    // Provide default values for options using the following common JavaScript idiom.
    options = options || {};
    this.foo = options['foo'] || 80;
    
    LiveSound.setSampleRate(8000);
    // Start playback.
    LiveSound.startPlayback(this);
}

/** Play audio data.
 *  @param data An array of numbers in the range -1 to 1 to be played.
 */
exports.Player.prototype.play = function(data) {
    // NOTE: Convert array into 2-D array required by LiveSound.
    LiveSound.putSamples(this, [data]);
}

/** Stop the player and free audio resources. */
exports.Player.prototype.stop = function() {
    LiveSound.stopPlayback(this);
}
