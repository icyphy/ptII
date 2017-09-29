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
        //'value': '192.168.0.103'
        'value': 'localhost'
    });
    RosPublisher.input('port', {
        'value': '9090'
    });
    RosPublisher.setParameter('topic', '/finite');
    //this.connect(RosPublisher, 'received', 'data');
};

exports.initialize = function() {
    this.addInputHandler('control', function (){
        var control =this.get('control');
        var msgJSON;

        var command = control['action'];
        if(command == "forward"){
            RosPublisher.send('toSend', {"data": 1 } );
        }
        if(command == "spin"){
            RosPublisher.send('toSend', {"data": 2 } );
        }
        var msg = msgJSON;

    });
    // At initialize, send the schema;
    this.send('schema', schema);
    // Also send null data.
    this.send('data', null);
};

var schema = {
  "type": "object",
  "properties": {
    "action": {
          "type": "string",
          "title": "Robot Action",
          "description": "Command for the robot to perform."
    }
  }
};