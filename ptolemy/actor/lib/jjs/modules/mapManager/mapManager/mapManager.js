//Author: Matt Weber


//******************************************************************************************
//Global MapManager Variables
//******************************************************************************************


//Hash mapping "mapName" strings to "map objects"
//This is a simple hash
var maps = {};

//A set of EntityAlias (source, destination) pairs. The pairs are stored as parameters with the value true.
//This is a keyed hash
var entityAliases = {};

//Hash mapping (domainName, codomainName) pairs as keys to functions
//This is a keyed hash
var coordinateTransformations= {};

//Todo: create a global set of all entities across all maps?
//var entities = {};


//******************************************************************************************
//Private helper functions
//******************************************************************************************


//Assumes the key for the hash is an ordinary string
function _simpleHashToString(hash){

	var text = "{ ";
	hashNames = Object.keys(hash);

	for(var i = 0; i < hashNames.length; i++  ){
		text += hashNames[i] + ": " + hash[hashNames[i]].toString();
		if(i != hashNames.length -1){
			text += ", ";
		}
	}

	text += " }"

	return text;
}

//Assumes the key for the hash is a specially formatted string
function _keyedHashToString(hash){
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

function _entityAliasKey(sourceEntity, destinationEntity ){
 	if( ! ((sourceEntity instanceof Entity) && (destinationEntity instanceof Entity))  ){
 		throw "Incorrect arguments to _EntityAliasKey.";
 	}

 	var sourceString = sourceEntity.toString();
 	var destinationString = destinationEntity.toString();
 	return _stringsToKey([sourceString, destinationString ]);
}

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

exports.entityAliasesToString = function(){
	return _keyedHashToString(entityAliases);
}

//Removed because there is currently no global entities hash

// exports.entitiesToString = function(){
// 	return _hashToString(entities);
// }


//******************************************************************************************
//EntityAlias Specific functions
//******************************************************************************************

//Todo what about other ontological metadata?
function Entity(name){
	if(! (typeof name === "string") ) {
		throw "Incorrect arguments to Entity constructor.";
	}

	this.name = name;
	this.containingMaps = {};

	this._key = function(){
		return this.name;
	}

	this.toString = function(){
		return "{ " + "name: " + this.name + ", " + "containingMaps: " + _simpleHashToString(this.containingMaps) + " }";
	}
}

exports.Entity = Entity;

exports.addEntityAlias = function(sourceEntity, destinationEntity ){
 	if( ! ((sourceEntity instanceof Entity) && (destinationEntity instanceof Entity))  ){
 		throw "Incorrect arguments to EntityAlias constructor.";
 	}

	var key = _entityAliasKey(sourceEntity, destinationEntity)

	if(entityAliases.hasOwnProperty(key)){
		return false;
	} else {
		entityAliases[key] = true;
		return true;
	}
}

exports.removeEntityAlias = function(sourceEntity, destinationEntity ){
 	if( ! ((sourceEntity instanceof Entity) && (destinationEntity instanceof Entity))  ){
 		throw "Incorrect arguments to EntityAlias constructor.";
 	}

	var key = _entityAliasKey(sourceEntity, destinationEntity)

	if(entityAliases.hasOwnProperty(key)){
		delete entityAliases[key];
		return true;
	} else {
		return false;
	}
}

exports.entityAliasExists = function(sourceEntity, destinationEntity ){
 	if( ! ((sourceEntity instanceof Entity) && (destinationEntity instanceof Entity))  ){
 		throw "Incorrect arguments to entityAliasExists.";
 	}

 	var key = _entityAliasKey(sourceEntity, destinationEntity)

 	if(entityAliases.hasOwnProperty(key)){
 		return true;
 	} else {
 		return false;
 	}
}

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


//Function to add a coordinate transform.
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
	this.entities = {}; //set of entities this map contains

	this.toString = function() {
		return "{mapName: " + mapName + ", spaceType: " + this.spaceType + ", coordinateSystem: " + this.coordinateSystem + "}";
	}

	this._key = function(){
		return mapName;
	}

	this.addEntity = function(entity) {
		if ( ! (entity instanceof Entity )  ){
			throw "Incorrect arguments to Map.addEntity."
		}

		if( entities.hasOwnProperty(entity.toString()) || entity.containingMaps.hasOwnProperty(this.toString()) ){
			return false;
		} else {
			this.entities[entity._key()] = true;
			entity.containingMaps[this._key()] = true;
			return true;
		}
	}
}

exports.Map = Map;

exports.addMap = function(map){
	if( ! (map instanceof Map) ){
		throw "Incorrect arguments to addMap.";
	}

	if(map == undefined){
		throw "undefined argument to addMap."
	}

	if(maps.hasOwnProperty( map.mapName) ){
		return false;
	} else {
		maps[map.mapName] = map;
		return true;
	}
}

exports.getMap = function(mapName){
	if( ! (typeof mapName === "string")  ){
		throw "Incorrect arguments to getMap";
	}

	return maps[mapName];

}

exports.removeMap = function(mapName ){
 	if( typeof mapName !== "string" ){
 		throw "Incorrect arguments to removeMap";
 	}

	if(maps.hasOwnProperty(mapName)){
		delete maps[mapName];
		return true;
	} else {
		return false;
	}
}