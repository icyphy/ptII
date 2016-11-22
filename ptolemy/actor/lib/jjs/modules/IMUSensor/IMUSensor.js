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
// Ptolemy II includes the work of others, to see those copyrights, follow
// the copyright link on the splash page or see copyright.htm.

/**
 * Module to access bluetooth IMU sensor (MotionNet sensor from  UTDallas)
 * @module IMUSensor
 * @author Hunter Massey
 * @version $$Id$$
 */

// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals exports, Java */
/*jshint globalstrict: true */
"use strict";

// Reference to the Java class that controls the bluetooth serial stream
var Controller = Java.type('ptolemy.actor.lib.jjs.modules.IMUSensor.SerialPortController');
var SensorStream = null;

/** Construct an instance of a Controller object type.
 *  This starts a thread in the java code that constantly reads input on the serial port that
 *  was linked to the sensor.
 *  If the code stops unexpectedly it is possible this serial port will remain open and
 *  ptolemy will need to be restarted.
 *  This code may be 'dumb' as it simply reads the input buffer
 *  whenever the getSample method is called and returns it. Checking whether this sample is a new one
 *  is done in the accessor.
 *
 *  Example:
 *  var imuSensor = require('IMUSensor');
 *  var stream = imuSensor.Stream();
 *  initialize:
 *          stream.start(bluetooth serialport #);
 *        setInterval(getSample, this.getParameter(samplingRate));
 *  getSample:
 *          var sample = stream.getSample();
 *          if (prevSample != sample) {
 *                  prevSample = sample;
 *                  this.send('stream', sample);
 *          }
 */
exports.Stream = function () {
    // Does not take options!
    // Default is set by java code
    // Baud rate: 115200
    // Buffer window: 60 samples - @ 20HZ, this is 3 seconds
    SensorStream = new Controller();
};

/** Initializes connection and collection of data from sensor. */
exports.Stream.prototype.start = function (sensorPort) {
    SensorStream.start(sensorPort);
};

/** Return the sample buffer - the sample buffer size should be
 * modifiable in the future.
 */
exports.Stream.prototype.getSample = function () {
    var sample = Java.from(SensorStream.getSample());
    return [sample, []];
};

/** Stop the sensor stream from continuing to collect data. */
exports.Stream.prototype.stop = function () {
    SensorStream.stop();
};
