/**
 * Module to simulate selected functions of a Hue bridge.
 * 
 * The module creates a virtual bridge for each unique bridge ID (a string
 * provided by an accessor).  The module remembers configuration and state 
 * information for the bridge.  
 *
 * @module mockHueBridge
 * @authors Elizabeth Osyk
 * @copyright http://terraswarm.org/accessors/copyright.txt
 */

"use strict";

var EventEmitter = require('events').EventEmitter;
var util = require('util');

// An object to hold a collection of bridges.
// Used as an associative array, e.g. bridges['Bridge1'].
var bridges = {};	

// An array to hold registered usernames
var usernames = [];

// Default transition time 400 ms
var defaultTime = 400; 

// Error message for unauthorized user.  Address will be updated.
var authorizationError = 
	[{ "error" : 
	   {"type" : 1, "address" : "/", "description" : "unauthorized user"}}];

// Sample lights for the bridge.  From the Hue docs with a few changes.
var defaultLights;

////////////////////////////////////////////////////////////
////Classes provided in this module.

/** Construct an instance of a MockHueBridge object with 'ptolemyuser' as a
 *  registered user.
 * 
 *  To create a bridge, connect, and send commands, you can do this:
 *  
 *  <pre>
 *  	var bridge = new mockHueBridges.MockHueBridge();
 *		var connection = bridge.connect(get('bridgeID'));
 *		var connection.initializeToDefault();  // Optional
 *	    var response;
 *      response = connection.command(method, uri); // If no request body
 *      response = connection.command(method, uri, body);  // Request with body 
 *  </pre>
 * 
 *  An instance of this object type implements the following functions:
 *  <ul>
 *  <li> bridges(): Return a list of bridge names. </li>
 *  <li> connect(bridgeID): Connect to the bridge with the given ID string. 
 *   Remembers the bridgeID so the caller doesn't have to specify it for every 
 *   command. </li>
 *  <li> MockHueBridgeConnection(bridgeID): Create a connection with the given 
 *   bridgeID string. </li>
 *  <li> MockHueBridgeConnection.command(method, URIpath, body): Issue a command
 *   to the bridge in the form of an HTTP request with the specified method
 *   (GET, POST, PUT), path and body (for POST and PUT). </li>
 *  <li> MockHueBridgeConnection.initializeToDefault(): Initialize bridge to 
 *   default configuration of two lights.
 *  </ul>
 */

exports.MockHueBridge = function() {
	
	// Sample lights for the bridge.  From the Hue docs with a few changes.
	defaultLights = 
	{	 
		    "1": {
		        "state": {
		            "on": false,
		            "bri": 0,
		            "hue": 0,
		            "sat": 0,
		            "xy": [0.5128,0.4147],
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
		            "xy": [0,0],
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
};


/** Return an array of bridge names for currently instantiated mock bridges.
*  Can be empty.
*  
*  @return An array of mock bridge names.  Can be empty.
*/
exports.MockHueBridge.prototype.bridges = function() {
	return Object.keys(bridges);
};

/** Construct an instance of a MockHueBridgeConnection object. 
 * 
 *  This object remembers the bridgeID for interacting with the MockHueBridge
 *  object.  The MockHueBridge object stores state for all bridges.  
 *  
 *  @param bridgeID A unique string identifying the bridge.
 */
exports.MockHueBridgeConnection = function(bridgeID) {
	// Call the super constructor
	EventEmitter.call(this);
	
	/** Given a URI, check if the username in the URI is an authorized user.
	 * 
	 * @param URIpath The URI potentially containing a username.
	 * @returns True if the user is authorized to access the URI or if authorization
	 * is not required; false otherwise.
	 */
	this.authorized = function(URIpath) {
		// Check if a username is present.  If URI does not have a username, this
		// operation is valid for all users, so return true.
		var expression = new RegExp('/api/.+');
		if (URIpath.match(expression)){
			URIpath = URIpath.substring(5, URIpath.length);
			var slash = URIpath.indexOf('/');
			if (slash === -1){
				slash = URIpath.length;
			}
			var username = URIpath.substring(0, slash);
			console.log('username : ' + username);
			  for (var i = 0; i < usernames.length; i++) {
				  console.log('username i : ' + usernames[i]);
				  if (usernames[i] === username) {
					  return true;
				  }
			  }
			return false;
		} else {
			return true;
		}
	};
	
	this.bridgeID = bridgeID;
	usernames = [];
	usernames.push('ptolemyuser');
};
util.inherits(exports.MockHueBridgeConnection, EventEmitter);

/** Connect to the bridge, remembering the bridge ID.
 * 
 * @param bridgeID A unique string identifying the bridge.
 * @returns {MockHueBridgeConnection} A connection to the bridge that remembers
 *  the bridgeID.
 */
exports.MockHueBridge.prototype.connect = function(bridgeID) {
	if (typeof bridges[bridgeID] != 'object') {
		bridges[bridgeID] = {};
	}
	
	return new exports.MockHueBridgeConnection(bridgeID);
};

/** Issue a command to the bridge.
 * 
 * @param method The HTTP request method.
 * @param URIpath The path part of the HTTP request URI, e.g. /api/username
 * @param body The HTTP request body, if any.  Optional. 
 * @returns The information requested (for GET) or information on whether the
 *  command was successful or not (for POST and PUT). 
 */

exports.MockHueBridgeConnection.prototype.command = 
	function(method, URIpath, body) {
	
	  // Match most-specific first
	  // GET, PUT /api/<username>/lights/<id>/state/
	  var expression1 = new RegExp('/api/.+/lights/.+/state/');
	  
	  // GET /api/<username>/lights/<id>/
	  var expression2 = new RegExp('/api/.+/lights/.+'); 
		  
	  // GET /api/<username>/lights/
	  var expression3 = new RegExp('/api/.+/lights/');
	  
	  // GET /api/<username>/
	  var expression4 = new RegExp('/api/.+');
	  
	  // POST /api
	  var expression5 = new RegExp('/api/');
	  
	  // GET /
	  // Used by accessor to determine if bridge is reachable
	  var expression6 = new RegExp('/');
	  
	  console.log("Executing method " + method + ", command " + URIpath);
	  
	  if (method == "GET" && URIpath.match(expression1)) {
		// GET /api/<username>/lights/<id>/state/
		  var lightID = findLightID(URIpath);
		  return JSON.stringify(bridges[this.bridgeID].lights[lightID]);
		  
	  } else if ((method == "POST" || method == "PUT") 
			  && URIpath.match(expression1)){
		  
		// POST or PUT /api/<username>/lights/<id>/state/
		  var lightID = findLightID(URIpath);
		  
		  // Parse body.  Contains {on, bri, hue, sat, transitiontime}
		  // transitiontime will change global transition time
		  // Future enhancement:  Generate event when transition is complete?		  
		  var lights = bridges[this.bridgeID].lights;
		  
		  if (typeof lights[lightID] != 'undefined') {
			  if (typeof body != undefined) {
				  for (var prop in body) {
					  if (prop != 'transitiontime') {
						  lights[lightID].state[prop] = body[prop];
					  }
				  }
			  }
		  }		  
		  
		  // TODO:  Implement or find a setTimeout() for Nashorn.
		  // In future, changes should occur after the specified transition time
		  // The HTTP response is generated immediately (as it should be)
		  
		  /*
		  var transitionTime;
		  if (body.hasOwnProperty('transitiontime')) {
			  transitionTime = body.transitiontime;
		  } else if (bridges[this.bridgeID].hasOwnProperty(transitionTime)) {
			  transitionTime = bridges[this.bridgeID].transitionTime;
		  } else {
			  transitionTime = defaultTime;
		  }	
		  */  
		  
		  bridges[this.bridgeID].lights = lights;
		  if (body.hasOwnProperty('transitiontime')) {
			  bridges[this.bridgeID].transitionTime = transitionTime;
		  }
		  
		  this.emit('change', bridges[this.bridgeID]);
		  
		  // Respond with list of properties changed
		  // E.g. // {"success":{"/lights/1/name":"Bedroom Light"}}
		  // The actual string is constructed here - doesn't need JSONified
		  var changeList;
		  changeList = "{\"success\" : {" ;
		  
		  for (var prop in body) {
			  changeList = changeList + "\"/lights/" + lightID + "/" + prop 
			    + "\":\"" + body[prop] + "\",";
		  }
		  
		  // Remove last comma
		  changeList = changeList.substring(0, changeList.length - 1);
		  changeList = changeList + "}}";
		  return changeList;
		  
	  } else if (method == "GET" && URIpath.match(expression2)) {
		// GET /api/<username>/lights/<id>/
		  var lightID = findLightID(URIpath);
		  return JSON.stringify(bridges[this.bridgeID].lights[lightID]);
		  
	  } else if (method == "GET" && URIpath.match(expression3)) {
		// GET /api/<username>/lights/
		// TODO: Check authorization
		  return JSON.stringify(bridges[this.bridgeID].lights);
  
	  } else if (method == "GET" && URIpath.match(expression4)) {
		// GET /api/<username>/
		// TODO:  Check authorization
		// Check for authorized user
		  return JSON.stringify({lights : bridges[this.bridgeID].lights}); 	  
	  } else if (method == "POST" && URIpath.match(expression5)) {
		// POST /api/
		// Register a new user.  Check if a username is specified.
		  var username;
		  
		  for (var prop in body) {
			  if (prop === "username") {
				  username = body[prop];
				  for (var i = 0; i < usernames.length; i++) {
					  if (usernames[i] === username) {
						  // If already registered, return success
						  return JSON.stringify([{success: {username: username}}]);
					  }
				  }
				  
				  // If not registered, add and return success
				  usernames.push(username);
				  return JSON.stringify([{success : {username : username}}]);
			  }
		  }
		  
		  // If username not specified in request, generate a new one
		  username = username + usernames.length;
		  usernames.push(username);
		  return JSON.stringify([{success : {username: username}}]);

		  //{"devicetype": "my_hue_app#iphone peter", "username": "peter"}
		  //[{"success":{"username": "83b7780291a6ceffbe0bd049104df"}}]
		
	  } else if (method == "GET" && URIpath.match(expression6)) {
		// GET /api/
		  // Note: Hue API does not specify what to return here
		  var ok = {available : true};
		  return JSON.stringify(ok);
		  
	  } else {
		  var unsupported = {supported : false};
		  return JSON.stringify(unsupported);
	  }
};

/** Initialize bridge with the ID corresponding to this connection.
 *  The default configuration is a set of two lights.
 */
exports.MockHueBridgeConnection.prototype.initializeToDefault = function() {
	bridges[this.bridgeID] = 
		{lights: defaultLights, transitionTime :  defaultTime };
}; 

/** Given a URI, find the lightID.
 * 
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
};