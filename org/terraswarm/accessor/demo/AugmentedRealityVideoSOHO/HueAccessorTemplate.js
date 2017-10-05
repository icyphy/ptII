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

/** Control a Philips Hue light bulb.
 *
 *  @accessor HueAccessorTemplate
 *  @author Christopher Brooks, based on TemperatureSensor by Edward A. Lee
 *  @input {JSON} commands JSON commands for the Hue, for example,
 *                {"id" : 1, "on" : true, "hue" : 120}
 *  @output data A JSON object.
 *  @version $$Id$$
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
    
    var Hue = this.instantiate('Hue', 'devices/Hue'); 

    /* The GenerateHueAccessors.xml model substitutes in the bridgeIP here.*/
    Hue.setParameter('bridgeIP','@bridgeIP@');

    /* The GenerateHueAccessors.xml model substitutes in the userName here.*/
    Hue.setParameter('userName', '@userName@');

    var codeIn = "\
        exports.setup = function() {\n\
           this.input('in');\n\
           this.output('out');\n\
        }\n\
        exports.initialize = function() {\n\
           var thiz = this;\n\
           this.addInputHandler('in', function() {\n\
               var inValue = thiz.get('in');\n\
               console.log('HueAccessorTemplate.JavaScriptOut: value of in: ' + inValue);\n\
               var util = require('util');\n\
               console.log(util.inspect(inValue));\n\
               console.log('HueAccessorTemplate.JavaScriptOut: inValue.on: ' + inValue.on);\n\
               var on = true;\n\
               if (inValue.on !== 'on') {;\n\
                   on = false; \n\
               };\n\
               inValue = '{ \"id\": \"@id@\", \"on\": ' + on + '}';\n\
               thiz.send('out', JSON.parse(inValue));\n\
           });\n\
        }";
    var JavaScriptIn = this.instantiateFromCode('JavaScriptIn', codeIn, false);

    var codeOut = "\
        exports.setup = function() {\n\
           this.input('in');\n\
           this.output('out');\n\
        }\n\
        exports.initialize = function() {\n\
           var thiz = this;\n\
           this.addInputHandler('in', function() {\n\
               /* var inValue = thiz.get('in'); */\n\
               /* FIXME: Need to get real data from the Hue. */\n\
               var inValue = '{ \"bridgeIP\": \"@bridgeIP@\", \"id\": \"@id@\", \"userName\": \"@userName@\" }';\n\
               thiz.send('out', JSON.parse(inValue));\n\
           });\n\
        }";

    var JavaScriptOut = this.instantiateFromCode('JavaScriptOut', codeOut, false);
    
    this.connect('control', JavaScriptIn, 'in');
    this.connect(JavaScriptIn, 'out', Hue, 'commands');
    this.connect(Hue, 'lights', JavaScriptOut, 'in');
    this.connect(JavaScriptOut, 'out', 'data');

};

exports.initialize = function() {	
    // At initialize, send the schema;
    this.send('schema', schema);
    // Also send null data.
    this.send('data', null);
};


function sendData() {
    this.send('data', {
	"id": @id@,
        "on": onValue,
    });
}

var schema = {
  "type": "object",
  "properties": {
    "on": {
      	"type": "string",
      	"title": "Turn the light on or off",
      	"description": "Turn the light on or off",
      	"choices": ["on", "off"]
    }
  }
};
