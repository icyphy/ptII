/** Accessor for teleoperation of a ROS robot through a ROS bridge.
 *
 *  @accessor ROSTeleoperation
 *  @author Matt Weber
 *  @input control A JSON object FIXME.
 *  @output data A JSON object FIXME.
 *  @output schema Upon initialization, this output port produces a JSON object that
 *   is a schema for the JSON expected on the control input.
 *  @version $$Id: SoundActuator.js 76814 2017-09-15 00:56:16Z eal $$
 */
// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals console, error, exports, require */
/*jshint globalstrict: true*/
"use strict";

var RosPublisher;

exports.setup = function() {
	this.implement('ControllableSensor');
	RosPublisher = this.instantiate('RosPublisher.js', 'robotics/RosPublisher');
	// FIXME: This should be a parameter?
	RosPublisher.input('server', {
	    'value': 'localhost'
	});
	RosPublisher.input('port', {
	    'value': '9090'
	});
    RosPublisher.setParameter('topic', '/cmd_vel');
    //this.connect(RosPublisher, 'received', 'data');
};

exports.initialize = function() {
    this.addInputHandler('control', function (){
        var control = this.get('control');
        var msgJSON;

        var stop = control['stop'];
        if(stop){
            msgJSON = {
                "linear": {
                    "y": 0.0,
                    "x": 0.0,
                    "z": 0.0
                },
                "angular": {
                    "y": 0.0,
                    "x": 0.0,
                    "z": 0.0
                }
            };
        } else {
            var linearY = 0.0;
            var linearX = 0.0;
            var angularY = 0.0;
            var angularX = 0.0;
            
            if(control['linearY']){
                linearY = control['linearY'];
            }
            if(control['linearX']){
                linearX = control['linearX'];
            }
            if(control['angularY']){
                angularY = control['angularY'];
            }
            if(control['angularX']){
                angularX = control['angularX'];
            }

            msgJSON = {
                "linear": {
                    "y": linearY,
                    "x": linearX,
                    "z": 0.0
                },
                "angular": {
                    "y": angularY,
                    "x": angularX,
                    "z": 0.0
                }
            };
        }
        var msg = JSON.stringify(msgJSON);
        RosPublisher.send('toSend', msg);
    });
	// At initialize, send the schema;
	this.send('schema', schema);
	// Also send null data.
	this.send('data', null);
};

var schema = {
  "type": "object",
  "properties": {
    "linearY": {
      	"type": 'number',
      	"title": "Y Linear Velocity",
      	"description": "Linear Velocity in Y direction."
    },
    "linearX": {
        "type": 'number',
        "title": "X Linear Velocity",
        "description": "Linear Velocity in X direction."
    },
    "angularY": {
        "type": 'number',
        "title": "Y Angular Velocity",
        "description": "Linear Velocity in Y direction."
    },
    "angularX": {
        "type": 'number',
        "title": "X Angular Velocity",
        "description": "Linear Velocity in X direction."
    },
    "stop":{
        "type": 'string',
        "title": "Stop",
        "description": "If set to a non-falsy string, stop the robot."
    }
  }
};
