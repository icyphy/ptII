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

var EventEmitter = require('events').EventEmitter;
var util = require('util');

var bridges = {};	// An object to hold a collection of bridges.
//Used as an associative array, e.g. bridges['Bridge1'].

////////////////////////////////////////////////////////////
////Functions provided in this module.

/** Return an array of bridge names for currently instantiated mock bridges.
*  Can be empty.
*  
*  @return An array of mock bridge names.  Can be empty.
*/
exports.bridges = function() {
	return Object.keys(bridges);
};

// Default transition time 400 ms
var defaultTime = 400; 

//Sample lights for the bridge.  From the Hue docs with a few changes.
var defaultLights = 
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

/** Construct an instance of a MockHueBridge object.  
 * 
 *  To create a bridge, connect, and send commands, you can do this:
 *  
 *  <pre>
 *  	var bridge = new mockHueBridges.MockHueBridge();
 *		var connection = bridge.connect(get('bridgeID'));
 *		var connection.initializeToDefault();  // Optional
 *	    var output = connection.command(method, uri); // If no request body
 *      var output = connection.command(method, uri, body);  // Request with body 
 *  </pre>
 * 
 *  An instance of this object type implements the following functions:
 */
exports.MockHueBridge = function() {

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
	
	this.bridgeID = bridgeID;
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
 */

exports.MockHueBridgeConnection.prototype.command = function(method, URIpath, body) {
	
	  // Match most-specific first
	  // GET, PUT /api/<username>/lights/<id>/state/
	  var expression1 = new RegExp('/api/.+/lights/.+/state/');
	  
	  // GET /api/<username>/lights/<id>/
	  var expression2 = new RegExp('/api/.+/lights/.+'); 
		  
	  // GET /api/<username>/lights/
	  var expression3 = new RegExp('/api/.+/lights/');
	  
	  // GET /api/<username>/
	  var expression4 = new RegExp('/api/.+');
	  
	  // GET /
	  // Used by accessor to determine if bridge is reachable
	  var expression5 = new RegExp('/');
	  
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
		  return JSON.stringify(bridges[this.bridgeID].lights);
		  
	  } else if (method == "GET" && URIpath.match(expression4)) {
		// GET /api/<username>/
		  return JSON.stringify({lights : bridges[this.bridgeID].lights});
		  
	  } else if (method == "GET" && URIpath.match(expression5)) {
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