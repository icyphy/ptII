//Author: Matt Weber


//******************************************************************************************
//Global MapManager Variables
//******************************************************************************************


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
function _simpleHashToString(hash){

	//This closure exists to protect the scope of "text" and i and hashNames when there is a recursive call
	//from the toString method below.
	return function(){

		var hashNames = Object.keys(hash);
		var text = "{ ";
		for(var i = 0; i < hashNames.length; i++  ){
			text += hashNames[i] +=": " + hash[hashNames[i]].toString();
			if(i != (hashNames.length -1)){
				text += ", ";
			}
		}
		text += " }"

		return text;
	}();
}

//Assumes the key for the hash is a specially formatted string
function _keyedHashToString(hash){


	//This closure exists to protect the scope of "text" and i and hashNames when there is a recursive call
	//from the toString method below.
	return function(){
		var text = "{ ";
		hashNames = Object.keys(hash);

		for(var i = 0; i < hashNames.length; i++  ){
			text += _keyToStrings(hashNames[i]) + ": " + hash[hashNames[i]].toString();
			if(i != hashNames.length -1){
				text += ", ";
			}
		}

		text += " }"
		return text;
	}();
}


//argument must be an array of at least two strings
//returns a string formatted with "x_" for arguments and "  " as a delimiter
function _stringsToKey( stringArray){
	var x;
	if( !  (typeof stringArray === "array")  ){
		throw "Incorrect arguments to _stringsToKey.";
	}
	for ( x in stringArray){
		if( ! (typeof x === "string")){
			throw "Incorrect arguments to _stringsToKey."
		}
	}
	if ( stringArray.length < 2){
		throw "Incorrect arguments to _stringsToKey."
	}

	var key = "";
	var arg;
	var c;

	for(arg in stringArray){
		for( c in arg ){
			key += (c + '_' );
		}
		key += "  ";
	}
	return key;
}

//argument is a string formatted with "x_" for arguments and "  " as a delimiter
//returns an array of strings
function _keyToStrings( key){
	if( ! ( typeof key === "string" ) ){
		throw "Incorrect arguments to _keyToStrings.";
	}

	if( ! key.includes("  ")){
		throw "key in _keyToStrings does not have a delimiter.";
	}

	var c;
	var c_last = "";
	var args = [];
	var arg = "";

	var realChar = false;


	for( c in key){
		if(c_last + c === "  "){
			args.push(arg);
		}
		if (realChar === false){
			realChar = true;
		} else {
			arg += c;
			realChar = false;
		}
		c_last = c;
	}

	return args;
}

//REMOVED due to change of entity aliases as being stored with the entity

// function _entityAliasKey(sourceEntity, destinationEntity ){
//  	if( ! ((sourceEntity instanceof Entity) && (destinationEntity instanceof Entity))  ){
//  		throw "Incorrect arguments to _EntityAliasKey.";
//  	}

//  	var sourceString = sourceEntity.toString();
//  	var destinationString = destinationEntity.toString();
//  	return _stringsToKey([sourceString, destinationString ]);
// }

function __coordinateTransformationKey( domainName, codomainName ){
	if( ! ( typeof domainName === "string" && typeof codomainName === "string" )){
		throw "Incorrect arguments to _coordinateTransformationKey"
	}

	return _stringsToKey([domainName, codomainName ]);

}

//******************************************************************************************
//Public Enums
//******************************************************************************************

//enum definition for spaceType
exports.SpaceTypeEnum = {
	EUCLIDEAN : "Euclidean",
	METRIC : "Metric",
	TOPOLOGICAL : "Topological",
	SET : "Set"
}


//******************************************************************************************
//Public toString functions for MapManager hashes
//******************************************************************************************


exports.mapsToString = function(){
	return _simpleHashToString(maps);
}

exports.coordinateTransformationsToString = function(){
	return _keyedHashToString(coordinateTransformations);
}

//Removed due to change of entity aliases as being stored with the entity

// exports.entityAliasesToString = function(){
// 	return _keyedHashToString(entityAliases);
// }

exports.entitiesToString = function(){
	return _simpleHashToString(entities);
}


//******************************************************************************************
//Entity Specific functions
//******************************************************************************************

//Todo what about other ontological metadata?
function Entity(name){
	if(! (typeof name === "string") ) {
		throw "Incorrect arguments to Entity constructor.";
	}

	this.name = name;
	this.containingMaps = {};
	this.aliases = {};

	this._key = function(){
		return this.name;
	}

	this.toString = function(){
		return "{ " + "name: " + this.name + " }";
	}

	this.containingMapsToString = function() {
		return _simpleHashToString(this.containingMaps);
	}

	this.aliasesToString = function() {
		return _simpleHashToString(this.aliases);
	}


	this.addAlias = function( alias ){
	 	if( ! ( alias instanceof Entity )){
	 		throw "Incorrect arguments to addAlias.";
	 	}

	 	//Check to see if this entity has been registered.
	 	if( ! entities.hasOwnProperty(alias._key()) ){
	 		throw "This entity is unregistered." + this.toString() + " Cannot give an unregistered entity an alias.";
	 	} 



	 	//Check to see if alias has been registered. If not, throw exception.
	 	if( ! entities.hasOwnProperty(alias._key()) ){
	 		throw "Attempt to add an unregistered entity as an alias to " + this.toString();
	 	} 

		//Todo, check to make sure this logic is correct, I realize aliases can exist in code too
	 	if( (alias === this) || this.aliases.hasOwnProperty(alias._key())){
	 		return false;
	 	} else {
	 		this.aliases[alias._key()] = alias;
	 		return true;
	 	}
	}
}

exports.Entity = Entity;

//Note that for now entities cannot be unregistered.
exports.registerEntity = function(entity){
	if( ! ( entity instanceof Entity) ){
		throw "Incorrect arguments to registerEntity.";
	}

	if(entities.hasOwnProperty(entity._key()) ){
		return false;
	} else {
		entities[entity._key()] = entity;
		return true;
	}
}


//Todo: is it the responsibility of the caller to remove the entity from all of its maps , and all of the entities that have it as an alias first?
//Todo: implement this better. The above makes me think this needs to be revised.

// exports.unregisterEntity = function(entity){
// 	if( ! ( entity instanceof Entity) ){
// 		throw "Incorrect arguments to unregisterEntity.";
// 	}

// 	if(entities.hasOwnProperty(entity.key()) ){
// 		delete entities[entity.key()];
// 		return true;
// 	} else {
// 		return false;
// 	}
// }



//Removed because EntityAliases no longer exists

// exports.removeEntityAlias = function(sourceEntity, destinationEntity ){
//  	if( ! ((sourceEntity instanceof Entity) && (destinationEntity instanceof Entity))  ){
//  		throw "Incorrect arguments to EntityAlias constructor.";
//  	}

// 	var key = _entityAliasKey(sourceEntity, destinationEntity)

// 	if(entityAliases.hasOwnProperty(key)){
// 		delete entityAliases[key];
// 		return true;
// 	} else {
// 		return false;
// 	}
// }

//Removed because EntityAliases no longer exists

// exports.entityAliasExists = function(sourceEntity, destinationEntity ){
//  	if( ! ((sourceEntity instanceof Entity) && (destinationEntity instanceof Entity))  ){
//  		throw "Incorrect arguments to entityAliasExists.";
//  	}

//  	var key = _entityAliasKey(sourceEntity, destinationEntity)

//  	if(entityAliases.hasOwnProperty(key)){
//  		return true;
//  	} else {
//  		return false;
//  	}
// }

//******************************************************************************************
//CoordinateTransformation Specific functions
//******************************************************************************************

//type definition for coordinateSystem? What exactly is a coordinate system? I think it has functions?
function CoordinateSystem(name ){
	if( typeof name !== "string"){
		throw "Incorrect arguments to CoordinateSystem constructor.";
	}

	this.name = name;
	this.toString = function() {
		return name;
	};
}

exports.CoordinateSystem =  CoordinateSystem;

//Todo consider changing the language to "registerCoordainteTransformation" for consistancy.
//Todo break the assumption that there only has to be one transformation from a particular domain to codomain.
exports.addCoordinateTransformation = function addCoordinateTransformation( domainName, codomainName, transformation ){
	if( ! ( typeof domainName === "string" && typeof codomainName === "string" && typeof transformation === "function" )  ){
		throw "Incorrect arguments to addCoordinateTransformation constructor."
	}
	
	var key = __coordinateTransformationKey( domainName, codomainName );

	if (coordinateTransformations.hasOwnProperty(key)){
		return false;
	} else {
		coordinateTransformations[key] = transformation;
		return true;
	}
}

//Function to get coordinate transformations out of the coordinateTransformations hash.
exports.getCoordinateTransformation = function getCoordinateTransformation( domainName, codomainName){
	if( ! ( typeof domainName === "string" && typeof codomainName === "string" )){
		throw "Incorrect arguments to getCoordinateTransformation constructor."
	}
	
	var key = __coordinateTransformationKey( domainName, codomainName );

	return coordinateTransformations[ key ];
}

//Todo, think through the possible messy complications of unregistering a coordinate transformation.
//This might be impossible.
//exports.unregisterCoordinateTransformation

//******************************************************************************************
//Maps Specific functions
//******************************************************************************************

//constructor for Map
function Map(mapName, spaceType, coordinateSystem){

	//Todo find a way to type check spaceType against SpaceTypeEnum
	if( ! ((coordinateSystem instanceof CoordinateSystem) &&( typeof mapName === "string" )) ){
	 	throw "Incorrect arguments to map constructor";
	 }
	this.spaceType = spaceType;
	this.coordinateSystem = coordinateSystem;
	this.mapName = mapName;
	this.mapEntities = {}; //set of entities this map contains


	this.mapEntitiesToString = function(){
		return _simpleHashToString(this.mapEntities);
	}

	this.toString = function() {
		return "{ mapName: " + mapName + ", spaceType: " + this.spaceType + ", coordinateSystem: " + this.coordinateSystem + " }";
	}

	this._key = function(){
		return mapName;
	}

	this.addEntity = function(entity) {
		if ( ! (entity instanceof Entity )  ){
			throw "Incorrect arguments to Map.addEntity."
		}

		if (! entities.hasOwnProperty(entity._key()) ){
			throw "Cannot add unregistered entity to map" + entity.toString();
		}

		if (! maps.hasOwnProperty(this._key()) ){
			throw "Cannot add an entity to an unregistred map" + this.toString();
		}

		if( this.mapEntities.hasOwnProperty(entity._key()) || entity.containingMaps.hasOwnProperty(this._key()) ){
			return false;
		} else {
			this.mapEntities[entity._key()] = entity;
			entity.containingMaps[this._key()] = this;
			return true;
		}
	}

	this.removeEntity = function(entity) {
		if ( ! (entity instanceof Entity )  ){
			throw "Incorrect arguments to Map.addEntity."
		}


		if (! entities.hasOwnProperty(entity._key()) ){
			throw "Cannot remove an unregistered entity from the map" + entity.toString();
		}

		if (! maps.hasOwnProperty(this._key()) ){
			throw "Cannot remove an entity from an unregistred map" + this.toString();
		}

		if( ! (this.mapEntities.hasOwnProperty(entity._key()) && entity.containingMaps.hasOwnProperty(this._key())) ){
			return false;
		} else {
			delete this.mapEntities[entity._key()];
			delete entity.containingMaps[this._key()];
			return true;
		}
	}
}

exports.Map = Map;

exports.registerMap = function(map){
	if( ! (map instanceof Map) ){
		throw "Incorrect arguments to registerMap.";
	}

	if(map == undefined){
		throw "undefined argument to registerMap."
	}

	if(maps.hasOwnProperty( map._key()) ){
		return false;
	} else {
		maps[map._key()] = map;
		return true;
	}
}


//Removed this function. The problem is mapKeys are implemented as private functions.
//There is no situation (for now) when the user has a mapKey but no map.

// exports.getMapFromKey = function(mapKey){
// 	if( ! (typeof mapKey === "string")  ){
// 		throw "Incorrect arguments to getMap";
// 	}

// 	return maps[mapKey];

// }

//Todo think through the implications of unregistering a map.
//Should this even be allowed?

// exports.unregisterMap = function(map){
//  	if( !( map instanceof Map ) ){
//  		throw "Incorrect arguments to unregisterMap";
//  	}

// 	if(maps.hasOwnProperty(map._key())){
// 		delete maps[map._key()];
// 		return true;
// 	} else {
// 		return false;
// 	}
// }