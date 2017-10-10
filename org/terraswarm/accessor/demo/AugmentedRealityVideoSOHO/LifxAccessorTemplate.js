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

/** Control a Philips Lifx light bulb.
 *
 *  @accessor LifxAccessorTemplate
 *  @author Chadlia Jerad, based on HueAccessorTemplate by Christopher Brooks
 *  @input {JSON} control JSON control for the Lifx, for example,
 *                {"on" : true, "hue" : 120}
 *  @output data A JSON object.
 *  @version $$Id: LifxAccessorTemplate.js 77016 2017-10-05 23:47:36Z cxh $$
 */
// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals console, error, exports, require */
/*jshint globalstrict: true*/

"use strict";

var handle = null;
exports.setup = function() {
    this.implement('ControllableSensor');

    this.output('data', {'type' : 'JSON'});

    var Lifx = this.instantiate('Lifx', 'devices/Lifx'); 

    Lifx.send('manualBulbSetup', {
        'ipAddress': '@ipAddress@',
        'port': '@port@',
        'macAddress': '@macAddress@'});

    
    this.connect('control', Lifx, 'control');
    this.connect(Lifx, 'data', 'data');
};

exports.initialize = function() {

    // At initialize, send the schema;
    this.send('schema', schema);
    // Also send null data.
    this.send('data', null);
};

var schema = {
  "type": "object",
  "properties": {
    "on": {
      	"type": "string",
      	"title": "Turn the light on or off",
      	"description": "Turn the light on or off",
      	"choices": ["on", "off"]
    },
    "color": {
        "type": "string",
        "title": "Choose the light color",
        "description": "Select the color",
        "choices": ["red", "blue", "green", "yellow"]
    }
  }
};
