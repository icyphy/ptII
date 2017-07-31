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

/** A dummy accessor for a pressure sensor that outputs a range of pressures
 *  varying from a low of 100 kPa (kilopascals, roughly the air pressure at sea level)
 *  up to 1000 kPa in steps that default to 10 kPa every second.
 *  The control input can be used to vary the step size.
 *  The output is a JSON object with the following properties:
 *  * name: "Pressure sensor"
 *  * units: "kPa"
 *  * pressure: a number.
 *
 *  @accessor PressureSensor
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
	this.implement('ControllableSensor');
};

var handle = null;
var valve = 0.1;

function updateValve() {
	var control = this.get('control');
	var v = control['valve'];
	if (v === 0 || v) {
		if (typeof v === 'string') {
			v = JSON.parse(v);
		}
		valve = v;
	}
}

exports.initialize = function() {
	var thiz = this;
	
	var pressure = 100;
	
	updateValve.call(this);

	// At initialize, send the schema;
	this.send('schema', schema);

	handle = setInterval(function() {
		thiz.send('data', {
		    'name': "Pressure sensor",
            'units': "kPa",
            'pressure': pressure
		});
		pressure += valve * 100;
	}, 1000);

	this.addInputHandler('control', function() {
	    updateValve.call(thiz);
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
    "valve": {
      "type": "text",
      "title": "valve",
      "description": "The valve opening, which determins by how much the pressure is increased each second"
    }
  }
};
