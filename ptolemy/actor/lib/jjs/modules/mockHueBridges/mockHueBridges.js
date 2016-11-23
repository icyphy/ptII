// Copyright (c) 2015-2016 The Regents of the University of California.
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

/**
 * Module to simulate selected functions of a Hue bridge.
 *
 * The module creates a virtual bridge for each unique bridge ID (a string
 * provided by an accessor).  The module remembers configuration and state
 * information for the bridge.
 *
 * @module mockHueBridge
 * @author Elizabeth Osyk
 * @version $$Id$$
 */

// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals console, exports, Java, require */
/*jshint globalstrict: true */
"use strict";

var EventEmitter = require('events').EventEmitter;
var util = require('util');

var MockHueBridgeHelper = Java.type('ptolemy.actor.lib.jjs.modules.mockHueBridges.MockHueBridgeHelper');

///////////////////////////////////////////////////////////////////
////Classes provided in this module.

/** Construct an instance of a MockHueBridge object.
 *
 *  To create a bridge, connect, and send commands, you can do this:
 *
 *  <pre>
 *      var MockHueBridgeHelper = Java.type('ptolemy.actor.lib.jjs.modules.mockHueBridges.MockHueBridgeHelper');
 *          var helper = MockHueBridgeHelper.getInstance();
 *                var connection = bridge.connect(get('bridgeID'));
 *                connection.initializeToDefault();  // Optional
 *
 *            var response;
 *      response = connection.command(method, uri); // If no request body
 *      response = connection.command(method, uri, body);  // Request with body
 *  </pre>
 *
 *  An instance of this object type implements the following functions:
 *  <ul>
 *  <li> connect(bridgeID): Connect to the bridge with the given ID string.
 *   Remembers the bridgeID so the caller doesn't have to specify it for every
 *   command. </li>
 *  <li> MockHueBridgeConnection(bridgeID): Create a connection with the given
 *   bridgeID string. </li>
 *  <li> MockHueBridgeConnection.command(method, URIpath, body): Issue a command
 *   to the bridge in the form of an HTTP request with the specified method
 *   (GET, POST, PUT), path and body (for POST and PUT). </li>
 *  <li> MockHueBridgeConnection.initializeToDefault(): Initialize bridge to
 *   default configuration of two lights. </li>
 *  </ul>
 */

exports.MockHueBridge = (function () {
    var helper = MockHueBridgeHelper.getInstance();

    // Error message for unauthorized user.  Address will be updated.
    var authorizationError = [{
        "error": {
            "type": 1,
            "address": "/",
            "description": "unauthorized user"
        }
    }];

    // Default transition time 400 ms.
    var defaultTime = 400;

    // Sample lights for the bridge.  From the Hue docs with a few changes.
    var defaultLights = {
        "1": {
            "state": {
                "on": false,
                "bri": 0,
                "hue": 0,
                "sat": 0,
                "xy": [0.5128, 0.4147],
                "ct": 467,
                "alert": "none",
                "effect": "none",
                "colormode": "xy",
                "reachable": true
            },
            "type": "Test light",
            "name": "Light 1",
            "modelid": "TESTMODEL",
            "swversion": "1",
            "pointsymbol": {
                "1": "none",
                "2": "none",
                "3": "none",
                "4": "none",
                "5": "none",
                "6": "none",
                "7": "none",
                "8": "none"
            }
        },
        "2": {
            "state": {
                "on": false,
                "bri": 0,
                "hue": 0,
                "sat": 0,
                "xy": [0, 0],
                "ct": 0,
                "alert": "none",
                "effect": "none",
                "colormode": "hs",
                "reachable": true
            },
            "type": "Test light",
            "name": "Light 2",
            "modelid": "TESTMODEL",
            "swversion": "1",
            "pointsymbol": {
                "1": "none",
                "2": "none",
                "3": "none",
                "4": "none",
                "5": "none",
                "6": "none",
                "7": "none",
                "8": "none"
            }
        }
    };

    ///////////////////////////////////////////////////////////////////
    //// Public functions of MockHueBridge

    /** Connect to the bridge, remembering the bridge ID.
     * @param bridgeID A unique string identifying the bridge.
     * @returns {MockHueBridgeConnection} A connection to the bridge that remembers
     *  the bridgeID.
     */
    function connect(bridgeID) {
        if (!helper.hasBridge(bridgeID)) {
            helper.addBridge(bridgeID);
        }

        return new MockHueBridgeConnection(bridgeID);
    }

    /** Construct an instance of a MockHueBridgeConnection object.
     *  This object remembers the bridgeID so the caller does not have to
     *  provide it for every command.
     *  @param bridgeID A unique string identifying the bridge.
     */
    function MockHueBridgeConnection(theBridgeID) {
        // Call the super constructor for EventEmitter
        EventEmitter.call(this);

        // Store the bridgeID.  This variable is available to functions
        // contained by MockHueBridgeConnection.
        var bridgeID = theBridgeID;


        ////////////////////////////////////////////////////////////
        //// Public functions of MockHueBridgeConnector

        /** Issue a command to the bridge.
         * @param method The HTTP request method.
         * @param URIpath The path part of the HTTP request URI,
         * e.g. /api/username
         * @param body The HTTP request body, if any.  Optional.
         * @returns The information requested (for GET) or information on
         * whether the command was successful or not (for POST and PUT).
         */
        this.command = function (method, URIpath, body) {
            var prop; // loop counter
            var lightID, json;

            // Match most-specific first
            // GET, PUT /api/<username>/lights/<id>/state/
            var expression1 = new RegExp('/api/.+/lights/.+/state/');

            // GET /api/<username>/lights/<id>/
            var expression2 = new RegExp('/api/.+/lights/.+');

            // GET /api/<username>/lights/
            var expression3 = new RegExp('/api/.+/lights/');

            // GET /api/<username>/
            var expression4 = new RegExp('/api/.+');

            // GET or POST /api
            var expression5 = new RegExp('/api/');

            // GET /
            // Used by accessor to determine if bridge is reachable
            var expression6 = new RegExp('/');

            console.log("Executing method " + method + ", command " + URIpath);

            if ((method == "POST" || method == "PUT") &&
                URIpath.match(expression1)) {
                // POST or PUT /api/<username>/lights/<id>/state/
                lightID = findLightID(URIpath);

                // Parse body.  Contains {on, bri, hue, sat, transitiontime}
                // Future enhancement:  Generate event when transition is complete?

                var state = JSON.parse(helper.getState(bridgeID));

                if (typeof state.lights[lightID] !== 'undefined') {
                    if (typeof body !== 'undefined') {
                        for (prop in body) {
                            if (prop != 'transitiontime') {
                                state.lights[lightID].state[prop] = body[prop];
                            }
                        }
                    }
                }

                // TODO:  Implement or find a setTimeout() for Nashorn.
                // In future, changes should occur after the transition time
                // The HTTP response is generated immediately (as it should be)

                helper.setState(bridgeID, JSON.stringify({
                    lights: state.lights
                }));
                if (body.hasOwnProperty('transitiontime')) {
                    helper.setTransitionTime(bridgeID, body.transitiontime);
                }

                // Emit an event on change.  The listening accessor will then
                // produce an output.
                this.emit('change', state);

                // Respond with list of properties changed
                // E.g. // {"success":{"/lights/1/name":"Bedroom Light"}}
                // The actual string is constructed here - doesn't need JSONified
                var changeList;
                changeList = "{\"success\" : {";

                for (prop in body) {
                    changeList = changeList + "\"/lights/" + lightID + "/" + prop +
                        "\":\"" + body[prop] + "\",";
                }

                // Remove last comma
                changeList = changeList.substring(0, changeList.length - 1);
                changeList = changeList + "}}";
                return changeList;

            } else if (method == "GET" && URIpath.match(expression2)) {
                // GET /api/<username>/lights/<id>/
                lightID = findLightID(URIpath);

                if (authorized(URIpath)) {
                    json = JSON.parse(helper.getState(bridgeID));
                    return JSON.stringify(json.lights[lightID]);
                } else {
                    authorizationError[0].error.address = URIpath;
                    return JSON.stringify(authorizationError);
                }

            } else if (method == "GET" && URIpath.match(expression3)) {
                // GET /api/<username>/lights/
                if (authorized(URIpath)) {
                    json = JSON.parse(helper.getState(bridgeID));
                    return JSON.stringify(json.lights);
                } else {
                    authorizationError[0].error.address = URIpath;
                    return JSON.stringify(authorizationError);
                }

            } else if (method == "GET" && URIpath.match(expression4)) {
                // GET /api/<username>/
                if (authorized(URIpath)) {
                    return helper.getState(bridgeID);
                } else {
                    authorizationError[0].error.address = URIpath;
                    return JSON.stringify(authorizationError);
                }

            } else if (method == "POST" && URIpath.match(expression5)) {
                // POST /api/
                // Register a new user.  Check if a username is specified.
                var username;
                var usernames = helper.getUsernames(bridgeID);

                for (prop in body) {
                    if (prop === "username") {
                        username = body[prop];
                        for (var i = 0; i < usernames.length; i++) {
                            if (usernames[i] === username) {
                                // If already registered, return success
                                return JSON.stringify([{
                                    success: {
                                        username: username
                                    }
                                }]);
                            }
                        }

                        // If not registered, add and return success
                        helper.addUsername(bridgeID, username);
                        return JSON.stringify([{
                            success: {
                                username: username
                            }
                        }]);
                    }
                }

                // If username not specified in request, generate a new one
                username = username + usernames.length;
                helper.addUsername(bridgeID, username);
                return JSON.stringify([{
                    success: {
                        username: username
                    }
                }]);

            } else if (method == "GET" && URIpath.match(expression6)) {
                // GET /api/
                // Note: Hue API does not specify what to return here
                var ok = {
                    available: true
                };
                return JSON.stringify(ok);

            } else {
                var unsupported = {
                    supported: false
                };
                return JSON.stringify(unsupported);
            }
        };

        /** Initialize bridge with the ID corresponding to this connection.
         *  The default configuration is a set of two lights.
         */

        this.initializeToDefault = function () {
            helper.setState(bridgeID, JSON.stringify({
                lights: defaultLights
            }));
            helper.setTransitionTime(bridgeID, defaultTime);
        };

        ////////////////////////////////////////////////////////////
        //// Private functions of MockHueBridgeConnector

        /** Given a URI, check if the username in the URI is an authorized user.
         * @param URIpath The URI potentially containing a username.
         * @returns True if the user is authorized to access the URI or if
         * authorization is not required; false otherwise.
         */

        function authorized(URIpath) {
            // Check if a username is present.  If URI does not have a username, this
            // operation is valid for all users, so return true.
            var expression = new RegExp('/api/.+');
            if (URIpath.match(expression)) {
                URIpath = URIpath.substring(5, URIpath.length);
                var slash = URIpath.indexOf('/');
                if (slash === -1) {
                    slash = URIpath.length;
                }
                var username = URIpath.substring(0, slash);
                var usernames = helper.getUsernames(bridgeID);
                for (var i = 0; i < usernames.length; i++) {
                    if (usernames[i] === username) {
                        return true;
                    }
                }
                return false;
            } else {
                return true;
            }
        }

        /** Given a URI, find the lightID.
         * @param URIpath The path portion of the URI, e.g. /api/username/lights/1/
         * @returns {String} The light ID, e.g. 1 for /api/username/lights/1/
         */

        function findLightID(URIpath) {
            var index = -1;
            var lightID = "-1";

            if (URIpath.length > 0) {
                index = URIpath.indexOf('lights');
                if (index != -1) {
                    URIpath = URIpath.substring(index + 7, URIpath.length);
                    index = URIpath.indexOf('/');
                    if (index != -1) {
                        lightID = URIpath.substring(0, index);
                    } else {
                        lightID = URIpath;
                    }
                }
            }

            return lightID;
        }
    }
    util.inherits(MockHueBridgeConnection, EventEmitter);

    // Return public functions for MockHueBridge
    return {
        connect: connect
    };

})();
