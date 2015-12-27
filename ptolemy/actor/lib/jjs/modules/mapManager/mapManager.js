// Below is the copyright agreement for the Ptolemy II system.
//
// Copyright (c) 2015 The Regents of the University of California.
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
// Ptolemy II includes the work of others, to see those copyrights, follow
// the copyright link on the splash page or see copyright.htm.


/**  Map Manager
 *  @author Matt Weber
 *  @version $$Id$$
 */

// Stop extra messages from jslint and jshint.  Note that there should
// be no space between the / and the * and global. See
// https://chess.eecs.berkeley.edu/ptexternal/wiki/Main/JSHint */
/*globals addInputHandler, console, error, exports, get, input, output, parameter, require, send */
/*jshint globalstrict: true*/
'use strict';

//******************************************************************************************
// Global MapManager Variables
//******************************************************************************************

//Todo: Perhaps add a collection of MapSources to iterate through for consistancy checking?

//Hash mapping "map._key()" strings to "map objects"
//This is a simple hash
var maps = {};

//Todo: delete this? I made the decision to maintain each alias's entities with the entity.
//Hash mapping source entities with their destination alter ego. he pairs are stored as parameters with the value true.
//This is a keyed hash
//var entityAliases = {};

//Todo: This isn't a structure you could iterate through if you wanted to make sure
// all transitive coordinate transformation functions could be determined. Make it a
// two level hash perhaps? Or attach the available transformations to a coordinate system
// object

//Hash mapping (domainName, codomainName) pairs as keys to functions
//This is a keyed hash
var coordinateTransformations= {};

//Hash mapping entity names to its entity
//this is a simple hash
var entities = {};


//******************************************************************************************
//Private helper functions
//******************************************************************************************


//Assumes the key for the hash is an ordinary string
function _simpleHashToString(hash) {

    //This closure exists to protect the scope of "text" and i and hashNames when there is a recursive call
    //from the toString method below.
    return function() {

	var hashNames = Object.keys(hash);
	var text = "{ ";
	for (var i = 0; i < hashNames.length; i++  ) {
	    text += hashNames[i] +=": " + hash[hashNames[i]].toString();
	    if (i != (hashNames.length -1)) {
		text += ", ";
	    }
	}
	text += " }";

	return text;
    }();
}

//Assumes the key for the hash is a specially formatted string
function _keyedHashToString(hash) {

    //This closure exists to protect the scope of "text" and i and hashNames when there is a recursive call
    //from the toString method below.
    return function() {
	var text = "{ ";
	var hashNames = Object.keys(hash);

	for (var i = 0; i < hashNames.length; i++  ) {
	    text += _keyToStrings(hashNames[i]) + ": " + hash[hashNames[i]].toString();
	    if (i != hashNames.length -1) {
		text += ", ";
	    }
	}

	text += " }";
	return text;
    }();
}


//argument must be an array of at least two strings
//returns a string formatted with "x_" for arguments and "  " as a delimiter
function _stringsToKey( stringArray) {
    var x;
    if (!  (stringArray instanceof Array)  ) {
	throw "Incorrect arguments to _stringsToKey. Input is not an array";
    }
    for ( x in stringArray) {
	if ( typeof x !== "string") {
	    throw "Incorrect arguments to _stringsToKey. Input is not an array of strings.";
	}
    }
    if ( stringArray.length < 2) {
	throw "Incorrect arguments to _stringsToKey.";
    }

    var key = "";
    var arg;
    var c;

    for (arg = 0; arg < stringArray.length; arg++) {

	var word = stringArray[arg];
	for (c = 0; c < word.length; c++) {
	    key += ('_' + word.charAt(c)  );
	}
	if (arg != stringArray.length -1) {
	    key += "  ";
	}
    }

    return key;
}

//argument is a string formatted with "x_" for arguments and "  " as a delimiter
//returns an array of strings
function _keyToStrings( key) {
    if ( typeof key !== "string") {
	throw "Incorrect arguments to _keyToStrings.";
    }

    if ( key.indexOf("  ") == -1) {
	throw "key in _keyToStrings does not have a delimiter.";
    }

    var c;
    var c_last = "";
    var args = [];
    var arg = "";


    var state = "parse";
    var nextState;

    for ( var i =0; i < key.length; i++) {
	c = key.charAt(i);
	if (state === "parse") {
	    if (c === "_") {
		nextState = "append";
	    } else if (c === " ") {
		nextState = "push";
	    }
	}

	if (state === "append") {
	    arg += c;
	    nextState = "parse";
	}

	if (state === "push") {
	    args.push(arg);
	    arg = "";
	    nextState = "parse";
	    //Note that the current value of c is ignored.
	}
    }
    args.push(arg);
    return args;
}

//REMOVED due to change of entity aliases as being stored with the entity

// function _entityAliasKey(sourceEntity, destinationEntity ) {
//  	if (! ((sourceEntity instanceof Entity) && (destinationEntity instanceof Entity))  ) {
//  		throw "Incorrect arguments to _EntityAliasKey.";
//  	}

//  	var sourceString = sourceEntity.toString();
//  	var destinationString = destinationEntity.toString();
//  	return _stringsToKey([sourceString, destinationString ]);
// }

function __coordinateTransformationKey( domainName, codomainName ) {
    if (! ( typeof domainName === "string" && typeof codomainName === "string" )) {
	throw "Incorrect arguments to _coordinateTransformationKey";
    }

    return _stringsToKey([domainName, codomainName ]);

}

//******************************************************************************************
//Public Enums
//******************************************************************************************

//
/**
 * @enum
 */

exports.SpaceTypeEnum = {
    EUCLIDEAN : "Euclidean",
    METRIC : "Metric",
    TOPOLOGICAL : "Topological",
    SET : "Set"
};




//******************************************************************************************
//Public toJSONString functions for MapManager hashes
//******************************************************************************************

/**
 * Returns the current state of the global in-memory maps storage as a JSON string.
 * The object has properties: maps, coordinateTransformations, and entities.
 * @function
 * @returns {string}
 */
exports.localRepoToJSONString = function() {
    var data = {
	"maps": maps,
	"coordinateTransformations": coordinateTransformations,
	"entities": entities
    };

    return JSON.stringify(data);
};


/**
 * Returns the current state of the global maps storage as a JSON string.
 * @function
 * @returns {string}
 */
exports.mapsToJSONString = function() {
    return JSON.stringify(maps);
};

/**
 * Returns the current state of the global coordinate transformations storage as a JSON string.
 * @function
 * @returns {string}
 */
exports.coordinateTransformationsToJSONString = function() {
    return JSON.stringify(coordinateTransformations);
};


/**
 * Returns the current state of the global entities storage as a JSON string.
 * @function
 * @returns {string}
 */
exports.entitiesToJSONString = function() {
    return JSON.stringify(entities);
};

//******************************************************************************************
//Public toString functions for MapManager hashes
//******************************************************************************************

/**
 * Returns the current state of the global maps storage as a string.
 * @function
 * @returns {string}
 */
exports.mapsToString = function() {
    return _simpleHashToString(maps);
};

/**
 * Returns the current state of the global coordinate transformations storage as a string.
 * @function
 * @returns {string}
 */
exports.coordinateTransformationsToString = function() {
    return _keyedHashToString(coordinateTransformations);
};


/**
 * Returns the current state of the global entities storage as a string.
 * @function
 * @returns {string}
 */
exports.entitiesToString = function() {
    return _simpleHashToString(entities);
};

//******************************************************************************************
//Data Control Functions
//******************************************************************************************


/**
 * Deletes all maps, entities, and coordinate transformations in the repo.
 * @function
 * @returns {void} - Clearing the repo always works.
 */
exports.clearRepo = function() {
    maps = {};
    coordinateTransformations= {};
    entities = {};
    return;
};


//Todo - Validate the input map before replacing the repo.
/**
 * Replaces the current values of maps, coordinateTransformations,
 * and entities with the input. Currently does NOT check the input besides
 * making sure it has "maps", "coordinateTransformations", and "entities"
 * properties
 * @function
 * @param {String} repo - A JSON string of the format you would get calling
 * localRepoToJSONString()
 * @returns {boolean} if the input was an acceptably formatted repo.
 */
exports.replaceRepo = function(repo) {
    if (typeof name !== "string") {
	throw "Incorrect arguments to replaceRepo.";
    }

    var receivedRepo;
    try {
	receivedRepo = JSON.parse(repo);
    } catch (e) {
	if (e instanceof SyntaxError) {
	    return false;
	} else {
	    throw "JSON.parse threw a non-SyntaxError exception";
	}

    }

    if (! (receivedRepo.hasOwnProperty("maps") && receivedRepo.hasOwnProperty("coordinateTransformations") && receivedRepo.hasOwnProperty("entities") ) ) {
	return false;
    } else {
	maps = receivedRepo.maps;
	coordinateTransformations= receivedRepo.coordinateTransformations;
	entities = receivedRepo.entities;
	return true;
    }
};

//******************************************************************************************
//Entity Specific functions
//******************************************************************************************

//Todo what about other ontological metadata?
/**
 * @constructor
 * @param {string} name - The name of the entity
 */

function Entity(name) {
    if (typeof name !== "string") {
	throw "Incorrect arguments to Entity constructor.";
    }

    this.name = name;
    this.containingMap = null;
    this.aliases = {};
    this.placements = {};
    this.occupancies = {};

    this._key = function() {
	return this.name;
    };

    /**
     * @function
     * @returns {string} string representation of this object.
     */
    this.toString = function() {
	return "{ " + "name: " + this.name + " }";
    };

    /**
     * @function
     * @returns {string} string representation of this entity's aliases.
     */
    this.aliasesToString = function() {
	return _simpleHashToString(this.aliases);
    };

    //Todo should this be registerAlias instead? What counts as registering or adding?
    /**
     * Indicate that an entity has another name on another map.
     * @function
     * @param {Entity} alias - The Entity on the different map
     * @returns {boolean} if the entity alias was successfully added returns true, false otherwise.
     */
    this.addAlias = function( alias ) {
	if (! ( alias instanceof Entity )) {
	    throw "Incorrect arguments to addAlias.";
	}

	//Check to see if this entity has been registered.
	if (! entities.hasOwnProperty(alias._key()) ) {
	    throw "This entity is unregistered." + this.toString() + " Cannot give an unregistered entity an alias.";
	}

	//Check to see if alias for this entity has been registered. If not, throw exception.
	if (! entities.hasOwnProperty(alias._key()) ) {
	    throw "Attempt to add an unregistered entity as an alias to " + this.toString();
	}

	//Todo, check to make sure this logic is correct, I realize aliases can exist in code too
	if ((alias === this) || this.aliases.hasOwnProperty(alias._key())) {
	    return false;
	} else {
	    this.aliases[alias._key()] = true;
	    return true;
	}
    };

    /**
     * Specify the location of an entity that is on a map
     * @function
     * @param {Placement} placement - The location of the entity with respect to its map
     * @returns {boolean} true if the location was successfully set, false otherwise.
     */
    this.setPlacement = function(placement) {
	if (! (placement instanceof Placement)) {
	    throw "Incorrect arguments to setPosition.";
	}

	//Check to see if this entity has been registered.
	if (! entities.hasOwnProperty(this._key()) ) {
	    throw "This entity is unregistered." + this.toString() + " Cannot give an unregistered entity a position.";
	}

	if (this.containingMap === null) {
	    throw "This entity has not been placed on a map." + this.toString();
	}

	if (this.placements.hasOwnProperty(placement._key())) {
	    return false;
	} else {
	    this.placements[placement._key()] = placement;
	    return true;
	}
    };

    /**
     * Getter for placements
     * @function
     * @returns {object} Hash mapping placement keys to placements.
     */
    this.getPlacements = function(){
    	return this.placements;
    };

    /**
     * Getter for name
     * @function
     * @returns {string} Name of this entity.
     */
    this.getName = function(){
    	return this.name;
    };

    /**
     * Specify the location of an entity that is on a map
     * @function
     * @param {Occupancy} occupancy - The location of the entity with respect to its map
     * @returns {boolean} true if the location was successfully set, false otherwise.
     */
    this.setOccupancy = function(occupancy) {
	if (! (occupancy instanceof Occupancy)) {
	    throw "Incorrect arguments to setPosition.";
	}

	//Check to see if this entity has been registered.
	if (! entities.hasOwnProperty(this._key()) ) {
	    throw "This entity is unregistered." + this.toString() + " Cannot give an unregistered entity a position.";
	}

	if (this.containingMap === null) {
	    throw "This entity has not been placed on a map." + this.toString();
	}

	if (this.occupancies.hasOwnProperty(occupancy._key())) {
	    return false;
	} else {
	    this.occupancies[occupancy._key()] = occupancy;
	    return true;
	}
    };
}

exports.Entity = Entity;

//Note that for now entities cannot be unregistered.
/**
 * Before any entity can be used by mapManager, it must first be registered so it can be placed in the global entity storage.
 * @function
 * @param {Entity} entity - The entity object to be registered by the map manager.
 * @returns {boolean} if the entity was successfully registered returns true, false otherwise.
 */
exports.registerEntity = function(entity) {
    if (! ( entity instanceof Entity) ) {
	throw "Incorrect arguments to registerEntity.";
    }

    if (entities.hasOwnProperty(entity._key()) ) {
	return false;
    } else {
	entities[entity._key()] = entity;
	return true;
    }
};



//Removed because EntityAliases no longer exists

// exports.removeEntityAlias = function(sourceEntity, destinationEntity ) {
//  	if (! ((sourceEntity instanceof Entity) && (destinationEntity instanceof Entity))  ) {
//  		throw "Incorrect arguments to EntityAlias constructor.";
//  	}

// 	var key = _entityAliasKey(sourceEntity, destinationEntity)

// 	if (entityAliases.hasOwnProperty(key)) {
// 		delete entityAliases[key];
// 		return true;
// 	} else {
// 		return false;
// 	}
// }

//Removed because EntityAliases no longer exists

// exports.entityAliasExists = function(sourceEntity, destinationEntity ) {
//  	if (! ((sourceEntity instanceof Entity) && (destinationEntity instanceof Entity))  ) {
//  		throw "Incorrect arguments to entityAliasExists.";
//  	}

//  	var key = _entityAliasKey(sourceEntity, destinationEntity)

//  	if (entityAliases.hasOwnProperty(key)) {
//  		return true;
//  	} else {
//  		return false;
//  	}
// }

//******************************************************************************************
//CoordinateTransformation Specific functions
//******************************************************************************************


//Todo: Remove this class and just treat coordinate systems as names. Or find
//a reason to make this a full-blown object.
/**
 * @constructor
 * @param {name} name - The name of the coordinate system.
 */
function CoordinateSystem(name ) {
    if (typeof name !== "string") {
	throw "Incorrect arguments to CoordinateSystem constructor.";
    }

    this.name = name;

    /**
     * @function
     * @returns {string} string representation of this object.
     */
    this.toString = function() {
	return name;
    };
}

exports.CoordinateSystem =  CoordinateSystem;

//Todo should the domainName and codomainName be strings or actual coordinate system objects?
//Todo break the assumption that there only has to be one transformation from a particular domain to codomain.
/**
 * Before any coordinate transformation can be used by mapManager,
 * it must first be registered so it can be placed in the global coordinate transformation storage.
 * @function
 * @param {String} domainName - The name of domain coordinate system.
 * @param {String} codomainName - The name of codomain coordinate system.
 * @param {function} codomainName - A function that takes one argument,
 * a point in the domain, and returns a point in the codomain
 * @returns {boolean} if the coordinate transformation was successfully registered returns true, false otherwise.
 */


exports.registerCoordinateTransformation = function registerCoordinateTransformation( domainName, codomainName, transformation ) {
    if (! ( typeof domainName === "string" && typeof codomainName === "string" &&
            typeof transformation === "function" )  ) {
	throw "Incorrect arguments to addCoordinateTransformation constructor.";
    }

    var key = __coordinateTransformationKey( domainName, codomainName );

    if (coordinateTransformations.hasOwnProperty(key)) {
	return false;
    } else {
	coordinateTransformations[key] = transformation;
	return true;
    }
};

/**
 * Retrieve a previously registered coordinate transformation from the global storage.
 * @function
 * @param {String} domainName - The name of domain coordinate system.
 * @param {String} codomainName - The name of codomain coordinate system.
 * @returns {boolean} A function that takes one argument,
 * a point in the domain, and returns a point in the codomain
 */
exports.getCoordinateTransformation = function getCoordinateTransformation( domainName, codomainName) {
    if (! ( typeof domainName === "string" && typeof codomainName === "string" )) {
	throw "Incorrect arguments to getCoordinateTransformation constructor.";
    }

    var key = __coordinateTransformationKey( domainName, codomainName );

    return coordinateTransformations[ key ];
};

//Todo, think through the possible messy complications of unregistering a coordinate transformation.
//This might be impossible.
//exports.unregisterCoordinateTransformation

//******************************************************************************************
//Maps Specific functions
//******************************************************************************************



/**

 * @typedef {Map}
 * @constructor
 * @param {mapName} string - The name of the map.
 * @param {SpaceTypeEnum} spaceTypeEnum - The variety of mathematical space this map considers.
 * @param {CoordinateSystem} coordinateSystem - The coordinate system with respect to which this map gives position.
 */
function Map(mapName, spaceType, coordinateSystem) {

    //Todo find a way to type check spaceType against SpaceTypeEnum
    if (! ((coordinateSystem instanceof CoordinateSystem) &&( typeof mapName === "string" )) ) {
	throw "Incorrect arguments to map constructor";
    }
    this.spaceType = spaceType;
    this.coordinateSystem = coordinateSystem;
    this.mapName = mapName;
    this.mapEntities = {}; //set of keys for entities this map contains

    this._key = function() {
	return mapName;
    };

    this.mapEntitiesToString = function() {
	return _simpleHashToString(this.mapEntities);
    };

    /**
     * @function
     * @returns {string} string representation of this object.
     */
    this.toString = function() {
	return "{ mapName: " + mapName + ", spaceType: " + this.spaceType + ", coordinateSystem: " + this.coordinateSystem + " }";
    };

    /**
     * Attach an entity to this map. This does not entail giving it a position.
     * @function
     * @param {Entity} entity - The entity to be attached to this map
     * @returns {boolean} if successful returns true, false otherwise.
     */
    this.addEntity = function(entity) {
	if ( ! (entity instanceof Entity )  ) {
	    throw "Incorrect arguments to Map.addEntity.";
	}

	if (! entities.hasOwnProperty(entity._key()) ) {
	    throw "Cannot add unregistered entity to map" + entity.toString();
	}

	if (! maps.hasOwnProperty(this._key()) ) {
	    throw "Cannot add an entity to an unregistred map" + this.toString();
	}

	if (this.mapEntities.hasOwnProperty(entity._key()) || entity.containingMap !== null ) {
	    //The entity is already on this map or has already been assigned to a different map.
	    return false;

	} else {
	    this.mapEntities[entity._key()] = true;
	    entity.containingMap = this._key();
	    return true;
	}
    };


    //Todo: should it be allowed to remove an entity?
    //Todo: what happens if an entity is in an invalid state of not thinking it's on a map it is on?
    //right now this function will just return false.
    /**
     * Unattach an entity from this map.
     * @function
     * @param {Entity} entity - The entity to be unattached from this map
     * @returns {boolean} if successful returns true, false otherwise.
     */
    this.removeEntity = function(entity) {
	if ( ! (entity instanceof Entity )  ) {
	    throw "Incorrect arguments to Map.addEntity.";
	}

	if (! entities.hasOwnProperty(entity._key()) ) {
	    throw "Cannot remove an unregistered entity from the map" + entity.toString();
	}

	if (! maps.hasOwnProperty(this._key()) ) {
	    throw "Cannot remove an entity from an unregistred map" + this.toString();
	}

	if ((! this.mapEntities.hasOwnProperty(entity._key())) || (entity.containingMap !== this._key()) ) {
	    //Entity had not been previously assigned to this map.
	    return false;

	} else {
	    delete this.mapEntities[entity._key()];
	    entity.containingMap = null;
	    return true;
	}
    };


    /**
     * Create an SVG format image of the entities on the map, from placements having
     * 2D center coordinates.
     * @function
     * @param {number} width - The pixel width of the image.
     * @param {number} width - The pixel height of the image.
     * @returns {string} An SVG image displaying the contents of the map.
     */
    this.mapEntitiesToSVG = function(width, height){
        if(  ! ((typeof width === "number") && (typeof height === "number") ) ){
    	    throw "Incorrect arguments to mapEntitiesToSVG";
        }

        console.log("function is called");

        var svgString = "";

        //Set xml version
        svgString += '<?xml version="1.0"?>\n';

        //Add a comment to show this program created the image
        svgString += '<!-- Created by mapManager -->\n';

        //Set width and height
        svgString +='<svg viewbox="0 0 100 100" preserveAspectRatio="xMidYMid meet" ';
        svgString += 'width="' + width.toString() +  '" height="' + height.toString() + '">\n';

        //Map boarder
        svgString += '<rect width="100" height="100" x="0" y="0" ';
        svgString += 'style="fill:rgb(0,0,0);stroke-width:1;stroke:rgb(0,0,0)" fill-opacity="0.1" />\n';

        console.log(this.mapEntities);

        //SVG content
        for( var eKey in this.mapEntities){
    	    var e = entities[eKey];
    	    var placements = e.getPlacements();

    	    console.log("eKey is:" + eKey);
    	    console.log("e is:" + e);
    	    console.log("placements is:" + placements);
    	    console.log("placement properties:" + Object.getOwnPropertyNames(e.getPlacements));
    	    for( var pKey in placements){
    		var p = placements[pKey];
    		console.log("P is:" + p);
    		var center = p.getCenter();
    		//Only consider 2D coordinates
    		console.log("center is" + center);
    		if(center.length == 2 ){

    		    //TODO fix this! This is wildly incorrect because of different coord systems for svg
    		    //I'm just doing it now for testing.
    		    var cx = center[0];
    		    var cy = center[1];
   		    svgString += '<circle cx="' + cx.toString() +'" cy="' + cy.toString() + '" r=".5"/>\n';
   		    svgString += '<text x="' + cx.toString() +'" y="' + (cy-1).toString() +
   			'" font-family="Verdana" font-size="2">\n'; //note the -1 y
   			
   		    svgString += e.getName() + '\n';
    		    svgString += '</text>\n';
    		}
    	    }
        }
        
        //close SVG
        svgString +='</svg>';
        
        return svgString;
    };
}

exports.Map = Map;


/**
 * Before any map can be used by mapManager, it must first be registered so it can be placed in the global map storage.
 * @function
 * @param {Map} map - The map object to be registered by the map manager.
 * @returns {boolean} if the map was successfully registered returns true, false otherwise.
 */
exports.registerMap = function(map) {
    if (! (map instanceof Map) ) {
	throw "Incorrect arguments to registerMap.";
    }

    if (map === undefined) {
	throw "undefined argument to registerMap.";
    }

    if (maps.hasOwnProperty( map._key()) ) {
	return false;
    } else {
	maps[map._key()] = map;
	return true;
    }
};


//******************************************************************************************
//Metadata classes
//******************************************************************************************

//Todo, perhaps add a cyrptopgrahic signiture to this. And maybe more information.
/**
 * @typedef {MapSource}
 * @constructor
 * @param {string} name - The name of this location information provider.
 */
function MapSource(source) {

    if (typeof source !== "string") {
	throw "Incorrect arguments to MapSource constructor";
    }

    this.name = source;

    this._key = function() {
	return this.name;
    };
}

exports.MapSource = MapSource;

//******************************************************************************************
//Position Specific Functions
//******************************************************************************************

/**
 * class describing positions and relations
 * @typedef {ObservationMetadata}
 * @constructor
 * @param {provenance} MapSource - The name of the source for this observation.
 * @param {timestamp} Date - The UTC timestamp for this observation (the time it happened, not when it was recorded).
 */
function ObservationMetadata(provenance, timestamp ) {

    if (! ( ( provenance instanceof MapSource ) && (typeof timestamp === "number")) ) {
	throw "Incorrect arguments to ObservationMetadata constructor";
    }

    this.provenance = provenance;
    this.timestamp = timestamp;

    this._key = function() {
	return _stringsToKey( [ provenance._key(), this.timestamp.toString() ]);
    };
}

exports.ObservationMetadata = ObservationMetadata;



var _globalPlacementID = 0;
//Todo: so far this placement only makes sense for 2D eculidean spaces because of shape,
// and only for euclidean spaces because of pose. Generalize it!
//Todo: DCEL shape input
//Todo: Probability. Particles?

/**
 * @typedef {Placement}
 * @constructor
 * @param {ObservationMetadata} metadata - Observation data for this map.
 * @param {array} center - The coordinate position describing the center of this entity.
 * @param {Quaternion} pose - The pose of the entity.
 * @param {array} shape - A counterclockwise array of coordinates around the entity's boundary.
 * Coordinates are given as they would be when the center is at the origin and pose is in the direction
 * of the positive y-axis and flat on the x-y plane.
 * This is done so uncertainty in the location of the center doesn't contaminate the shape of the entity.
 */
function Placement(metadata, center, pose, shape ) {

    if (! ( ( metadata instanceof ObservationMetadata ) && (center instanceof Array) &&
	    (pose instanceof Quaternion ) && (shape instanceof Array)) ) {
	throw "Incorrect arguments to Placment constructor";
    }

    this.metadata = metadata;
    this.center = center;
    this.pose = pose;
    this.shape = shape;
    this._placementID = _globalPlacementID++;

    this._key = function() {
	return _stringsToKey( [ this._placementID.toString(), this.metadata._key() ] );
    };

    /**
     * Getter for center
     * @function
     * @returns {array} The coordinate position describing the center of this entity.
     */
    this.getCenter = function(){
    	return this.center;
    };
}

exports.Placement = Placement;


/**
 * @typedef {Quaternion}
 * @constructor
 * @param {number} w
 * @param {number} x
 * @param {number} y
 * @param {number} z
 */
function Quaternion(w, x, y, z) {

    if (! ( ( typeof w === "number") && ( typeof x === "number") &&( typeof y === "number") &&
	    ( typeof z === "number") )) {
	throw "Incorrect arguments to Quaternion constructor";
    }

    this.w = w;
    this.x = x;
    this.y = y;
    this.z = z;

}

exports.Quaternion = Quaternion;

var _globalOccupancyID = 0;

/**
 * @typedef {Occupancy}
 * @constructor
 * @param {metadata} ObservationMetadata - Observation data for this map.
 * @param {grid} array - An occupancy grid of 1 to 3 dimensions. Values must be integers between 0 to 100.
 */
function Occupancy(metadata, grid ) {

    if (! ( ( metadata instanceof ObservationMetadata ) && (grid instanceof Array) ) ) {
	throw "Incorrect arguments to Occupancy constructor";
    }

    this.metadata = metadata;
    this.grid = grid;
    this.dimensions = [];
    this._occupancyID = _globalOccupancyID++;

    var dataTemp = grid;
    while (dataTemp instanceof Array) {
	this.dimensions.push( dataTemp.length );
	dataTemp = dataTemp[0];
    }

    this._key = function() {
	return _stringsToKey( [ this._occupancyID.toString(), this.metadata._key() ] );
    };

}

exports.Occupancy = Occupancy;

//******************************************************************************************
//Relation Specific Functions
//******************************************************************************************

