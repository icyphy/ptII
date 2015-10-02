// Put your JavaScript program here.
// Add ports and parameters.
// Define JavaScript functions initialize(), fire(), and/or wrapup().
// Refer to parameters in scope using dollar-sign{parameterName}.
// In the fire() function, use get(parameterName, channel) to read inputs.
// Send to output ports using send(value, portName, channel).


mapManager = require('mapManager');
 
 
//Test of maps

console.log("****** Test of Maps ******");
myCoords = new mapManager.CoordinateSystem("Joey");
yourCoords = new mapManager.CoordinateSystem("Sally");
myMap = new mapManager.Map("myMapName" , mapManager.SpaceTypeEnum.EUCLIDEAN, myCoords);
yourMap = new mapManager.Map("yourMapName", mapManager.SpaceTypeEnum.TOPOLOGICAL, yourCoords);
badMap = new mapManager.Map("badMapName", mapManager.SpaceTypeEnum.EUCLIDEAN, myCoords);

console.log("Adding myMapName");
console.log(mapManager.addMap(myMap));
console.log("Adding myMap again, should print false");
console.log(mapManager.addMap(myMap))
mapManager.addMap(yourMap);
mapManager.addMap(badMap);

console.log(mapManager.mapsToString());
console.log(mapManager.removeMap("badMapName"));
console.log("printing maps with badMapName removed");
console.log(mapManager.mapsToString());


//Test of entityAliases
console.log("****** Test of entityAliases ******");
cat = new mapManager.Entity("cat");
dog = new mapManager.Entity("dog");

console.log(cat.toString());
//Big TODO!! Decide if an entity must be attached to a map when it is created.
//If so, does that it get associated in the entity constructor,
//in a mapManager function, or as a method of the map?


