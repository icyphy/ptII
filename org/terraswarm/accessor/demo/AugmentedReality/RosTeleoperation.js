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
        //'value': '192.168.0.104'
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
        var control =this.get('control');
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
            var linear = 0.0;
            var angular = 0.0;
            
            if(control['linear']){
                linear = parseFloat(control['linear']);
            }
            if(control['angular']){
                angular = parseFloat(control['angular']);
            }
            msgJSON = {
                "linear": {
                    "y": 0.0,
                    "x": linear,
                    "z": 0.0
                },
                "angular": {
                    "y": 0.0,
                    "x": 0.0,
                    "z": angular
                }
            };
        }
        var msg = msgJSON;
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
    "linear": {
      	"type": 'number',
      	"title": "Linear Velocity",
      	"description": "Linear Velocity in X direction."
    },
    "angular": {
        "type": 'number',
        "title": "Angular Velocity",
        "description": "Anugular Velocity around Z axis."
    },
    "stop":{
        "type": 'string',
        "title": "Stop",
        "description": "If set to a non-falsy string, stop the robot."
    }
  }
};
