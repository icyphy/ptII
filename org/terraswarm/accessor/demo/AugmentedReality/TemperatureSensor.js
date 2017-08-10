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

/** A dummy accessor for a temperature sensor that outputs a range of temperatures
 *  varying from a low of 0 degrees centigrade up to 100 degrees in steps that default
 *  to 10 degrees every second.
 *  
 *  The control input can be used to vary the period of sampling.
 *  To change the period, provide an input object with property "period"
 *  that specifies the period in milliseconds.
 *  The period defaults to 1 sample per second.
 *  A period of 0 will stop the sampling.
 *  
 *  The control input can also be used to vary the increment added to each
 *  sample. To change the increment, provide an input object with property
 *  "step" that specifies the increment in degrees.
 *
 *  The output is a JSON object with the following properties:
 *  * name: "Temperature sensor"
 *  * units: "Degree centigrade"
 *  * temperature: a number.
 *
 *  @accessor TemperatureSensor
 *  @author Edward A. Lee (eal@eecs.berkeley.edu)
 *  @input control A JSON object that can have two properties,
 *   "step", a number and "period", also a number.
 *  @output data A JSON object.
 *  @version $$Id$$
 */
// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals console, error, exports, require */
/*jshint globalstrict: true*/
"use strict";

exports.setup = function() {
	// this.implement('ControllableSensor');
	this.realize('controllableSensor');
	this.input('control', {
		'type': 'JSON',
		'value': null
	});

	// Defining outputs
	this.output('data', { 'spontaneous':true,
		'type': 'JSON',
		'value': null		
	});
	this.output('schema', { 'spontaneous':true,
		'type': 'JSON',
		'value': null
	});
};

var handle = null;
var step = 10;
var period = 1000;
var temperature = 0;

function sendData() {
	this.send('data', {
		'name': "Temperature sensor",
    	'units': "Degrees centigrade",
  	 	'temperature': temperature
	});
	temperature += step;
}

function updateControl() {
	var control = this.get('control');
	
	var s = control['step'];
	if (s || s === 0) {
		if (typeof s === 'string') {
	    	s = JSON.parse(s);
		}
		step = s;
	}
	
	var p = control['period'];
	if (p || p === 0) {
		if (typeof p === 'string') {
	    	p = JSON.parse(p);
		}
		if (handle) {
	    	clearInterval(handle);
	    	handle = null;
	    }
		period = p;
	}
	if (period && !handle) {
        // Nothing running. Start the sampling.
        // Send an initial sample, then periodically.
        sendData.call(this);
        handle = setInterval(sendData.bind(this), period);
    }
}

exports.initialize = function() {	
	// At initialize, send the schema;
	this.send('schema', schema);
	updateControl.call(this);
	this.addInputHandler('control', updateControl.bind(this));
};

exports.wrapup = function() {
    if (handle) {
        clearInterval(handle);
    }
};

var schema = {
  "type": "object",
  "properties": {
    "step": {
      	"type": "number",
      	"title": "step size",
      	"description": "The increment by which data is increased each second"
    },
    "period": {
      	"type": "number",
      	"title": "sampling period",
      	"description": "The time between samples in milliseconds"    	
    }
  }
};
