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
// Put your JavaScript program here.
// Add ports and parameters.
// Define JavaScript functions initialize(), fire(), and/or wrapup().
// Refer to parameters in scope using dollar-sign{parameterName}.
// In the fire() function, use this.get(parameterName, channel) to read inputs.
// Send to output ports using this.send(value, portName, channel).


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


