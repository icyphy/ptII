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

/** A dummy accessor for a vibration sensor that outputs a range of vibrations
 *  varying from a low of 0 m/s^2 to 100 m/s^2 in steps that default to 5 m/s^2
 *  every second. The control input can be used to vary the step size.
 *  The output is a JSON object with the following properties:
 *  * name: "Vibration sensor"
 *  * units: "m/s^2"
 *  * vibration: a number.
 *
 *  @accessor VibrationSensor
 *  @author Edward A. Lee (eal@eecs.berkeley.edu)
 *  @input control A JSON object that is expected to have one property, "step", a number.
 *  @output data A JSON object.
 *  @version $$Id$$
 */
// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals console, error, exports, require */
/*jshint globalstrict: true*/
"use strict";

exports.setup = function() {
    // Indicate that this can reify a MutableAccessor that expects a controllableSensor.
    this.realize('controllableSensor');

	this.input('control', {
		'type': 'JSON',
		'value': {'step': 5}
	});
	this.output('data', {
		'type': 'JSON'
	});
	this.output('schema', {
		'type': 'JSON'
	});
};

var handle = null;

exports.initialize = function() {
	var thiz = this;
	
	var vibration = 0;
	var step = thiz.get('control')['step'];

	// At initialize, send the schema;
	this.send('schema', schema);

	handle = setInterval(function() {
		thiz.send('data', {
		    'name': "Vibration sensor",
            'units': "m/s^2",
            'vibration': vibration
		});
		vibration += step;
	}, 1000);

	this.addInputHandler('control', function() {
	    step = thiz.get('control')['step'];
        if (typeof step === undefined) {
            step = 0;
        }
	});
};

exports.wrapup = function() {
    if (handle) {
        clearInterval(handle);
    }
};

var schema = {
  "$schema": "http://json-schema.org/draft-03/schema#",
  "type": "object",
  "properties": {
    "step": {
      "type": "number",
      "title": "step size",
      "description": "The increment by which data is increased each second"
    }
  }
};
