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

/** An accessor for a message board that accepts and displays text.
 *  
 *  Text entered into the message box will be sent to the message board
 *  through http POST when the user presses submit. The server responds
 *  with the status of the message. 
 *
 *  @accessor MessageBoard.js
 *  @author Matt Weber (matt.weber@berkeley.edu)
 *
 *  FIXME: The control input should be an object according to the other accessors like
 *      TemperatureSensor.js
 *  @input control A string. 
 *  @output data A JSON object that is expected to have one property: "status", a string
 *      representing the status of the last message sent.
 *  @version $$Id: MessageBoard.js 76593 2017-08-10 21:25:23Z chadlia.jerad $$
 */
// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals console, error, exports, require */
/*jshint globalstrict: true*/
"use strict";

var httpClient = require('@accessors-modules/http-client');

var handle = null;

exports.setup = function() {
    //FIXME: Technically this is a controllable actuator, maybe we should change the interface?
    this.implement('ControllableSensor');

    this.parameter('url',{
        'type': 'string',
        'value': 'http://128.32.44.194:8082'
        //'value': 'http://terra.eecs.berkeley.edu:8091/track'
        //'value': 'http://127.0.0.1:8080/'
    });

    //FIXME: I think the best implementation of this accessor would extend the rest accessor,
    //but this would add a lot of input and output ports that would be problematic for reification.
    //this.extend('net/REST');
};


function sendData( serverResponse ) {
    console.log("serverResponse: " + JSON.stringify(serverResponse));
    if(serverResponse && serverResponse.body){
        this.send('data', {
            'name': "Message to post",
            'status': serverResponse.body,
        });
    } else {
        this.send('data', {
            'name': "Message to post",
            'status': "Null server status."
        });        
    }
}

function updateControl() {
    var control = this.get('control');
    console.log(control);
    
    var m = control['message'];
    console.log("m: " + m);
    if(m || m === ""){
        /*
        try{
            httpClient.get({
            'body': m,
            'method': "GET",
            'url': this.getParameter('url') + "?message=" + m
            }, sendData.bind(this));
        }
        catch (e){
            this.send('data', {
            'name': "Message to post",
            'status': "Error: " + e
            });
        }
        */
        
        try {
            httpClient.post({
            'body': m,
            'method': "POST",
            'url': this.getParameter('url')
            }, sendData.bind(this));
        } catch (e){
            this.send('data', {
            'name': "Message to post",
            'status': "Error: " + e
            });
        }
    }
}

function sendInitData(){
    this.send('data', {
        'name': "Message to post",
        'status': 'No message sent.'
    });
}

exports.initialize = function() {    
    // At initialize, send the schema;
    this.send('schema', schema);
    sendInitData.call(this);
    handle = this.addInputHandler('control', updateControl.bind(this));
};

exports.wrapup = function() {
    if (handle !== null) {
        this.removeInputHandler(handle); clearInterval(handle);
    }
};

var schema = {
  "type": "object",
  "properties": {
    "message": {
          "type": "string",
          "title": "message",
          "description": "The message to display on the message board."
    }
  }
};