mapManager = require('mapManager');

var outMessage = "";

var myCoords = new CoordinateSystem("myCoordsName");
var myMap = new mapManager.Map("myMapName" , mapManager.SpaceTypeEnum.EUCLIDEAN, myCoords);


var cat = new mapManager.Entity("cat");
var dog = new mapManager.Entity("dog");

outMessage += "Map with no entities.\n";
outMessage += myMap.toString() + "\n";
outMessage += "Entities without map.\n"
outMessage += cat.toString() + "\n";
outMessage += dog.toString() + "\n";

myMap.addEntity(cat);
myMap.addEntity(dog);

outMessage += 