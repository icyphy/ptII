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
var RosSubscriber;

exports.setup = function() {

    this.input( "velocities" );

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
    
    RosSubscriber = this.instantiate('RosSubscriber.js', 'robotics/RosSubscriber');
    // FIXME: This should be a parameter?
    RosSubscriber.input('server', {
        //'value': '192.168.0.103'
        'value': 'localhost'
    });
    RosSubscriber.input('port', {
        'value': '9090'
    });
    RosSubscriber.setParameter('topic', '/cmd_vel');
    
    //Workaround so this composite can set an input handler for ros data
    this.connect(RosSubscriber, "received", this, "velocities");
};

exports.initialize = function() {
    this.addInputHandler('control', function(){
        var control = this.get('control');
        var msgJSON;

        var command = control['action'];
        if(command == "forward"){
            RosPublisher.send('toSend', {"data": 1 } );
        }
        if(command == "spin"){
            RosPublisher.send('toSend', {"data": 2 } );
        }
        if(command == "circle"){
            RosPublisher.send('toSend', {"data": 3 } );
        }
        if(command == "square"){
            RosPublisher.send('toSend', {"data": 4 } );
        }
        var msg = msgJSON;
    });
    this.addInputHandler('velocities', function (){
        var vels = this.get('velocities');
        var data = {
            "angular": {
                "name": "Angular Velocity",
                "units": "radians/second",
                "Angular Velocity": vels.msg.angular.z
            },
            "linear": {
                "name": "Linear Velocity",
                "units": "meters/second",
                "Linear Velocity": vels.msg.linear.x
            }
        };  
        this.send('data', data);
    });
       
    // At initialize, send the schema;
    this.send('schema', schema);
    // Also send null data.
    this.send('data', null);
};

exports.wrapup =function(){
    //Reset the data display
    this.send('data', null);
    RosPublisher.wrapup();
    RosSubscriber.wrapup();
};
/*
var robotControl = function(){

};
*/

var schema = {
  "type": "object",
  "properties": {
    "action": {
          "type": "string",
          "title": "Robot Action: 'forward', 'spin', 'circle', 'square'",
          "description": "Command for the robot to perform."
    }
  }
};