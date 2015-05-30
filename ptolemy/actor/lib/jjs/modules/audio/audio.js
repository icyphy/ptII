/**
 * Module to access audio hardware on the host.
 * NOTE: This is very incomplete! Just a placeholder for now.
 * @module audio
 * @authors Edward A. Lee
 * @copyright http://terraswarm.org/accessors/copyright.txt
 */
 
// Reference to the Java class documented at:
//    http://terra.eecs.berkeley.edu:8080/job/ptII/javadoc/ptolemy/media/javasound/LiveSound.html
var LiveSound = Java.type('ptolemy.media.javasound.LiveSound');

/** Construct an instance of an Player object type. This should be instantiated in your
 *  JavaScript code as
 *  <pre>
 *     var Audio = require("audio");
 *     var player = new Audio.Player();
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

// Below is code to be added by students.

/** Construct an instance of a Capture object type. This should be instantiated in your
 *  JavaScript code as
 *  <pre>
 *     var audio = require("audio");
 *     var capture = new audio.Capture();
 *  </pre>
 *  An instance of this object type implements the following functions:
 *  (FIXME: replace with your design)
 *  <ul>
 *  <li> get(): Return an array of audio data.
 *  <li> stop(): Stop capture and free the audio resources.
 *  </ul>
 *  @param options A JSON object with fields 'FIXME' and 'FIXME' that give the
 *   FIXME properties of the audio such as sample rate, etc. Provide reasonable
 *   defaults.
 */
exports.Capture = function(options) {
    LiveSound.setSampleRate(8000);
    // Start playback.
    LiveSound.startCapture(this);
}

/** Capture audio data.
 *  @return An array of numbers in the range -1 to 1 captured from the audio.
 */
exports.Capture.prototype.get = function(data) {
    // NOTE: 2-D double[][] array returned by LiveSound.
    var data = LiveSound.getSamples(this);
    // Could use Nashorn-specific conversion to convert to a JavaScript array,
    // as follows:
    // var channels = Java.from(data);
    // var sound = Java.from(channels[0]);

    // Note that we return only channel 0.
    if (data.length >= 1) {
        var channels = data[0];
        return channels;
    }
    throw("No audio data returned.");
}

/** Stop the capture and free audio resources. */
exports.Capture.prototype.stop = function() {
    LiveSound.stopCapture(this);
}
