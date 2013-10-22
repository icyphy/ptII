// Based on http://bl.ocks.org/e-/5244131 Self-Organizing Map example

// Timer for page refresh.  Declared here so we can cancel it if Ptolemy
// model stops running
var intervalHandler;

// Javascript objects to store data
var power = true;
var rooms = {
		"elevator1": { 
		    "vertices" : [ {"x":150, "y":198} , {"x":198, "y":198}, 
		                   {"x":198, "y":269} , {"x":150, "y":269}, 
		                   {"x":150, "y":198}],
		    "occupied" : false
		},
		"elevator2": { 
		    "vertices" : [ {"x":429, "y":163} , {"x":511, "y":173}, 
		                   {"x":511, "y":230} , {"x":429, "y":230}, 
		                   {"x":429, "y":163}],
		    "occupied" : false
		},
		"elevator3": { 
		    "vertices" : [ {"x":641, "y":103} , {"x":688, "y":111}, 
		                   {"x":680, "y":171} , {"x":633, "y":166}, 
		                   {"x":641, "y":103}],
		    "occupied" : false
		}
};

// SVG temperature overlay
var image,
    roomShapes;

// Opacity of temperature overlay.  Between one (opaque) and zero (transparent)
var opacity = 0.7;

// Duration of the room map animated transition, in ms
var duration = 2000;

// Image dimensions.  Assumes a fixed-size image
// Scaife floor 2 dimensions 648 x 1147
var graphPadding = 30,			// Padding around heat map
	imageHeight = 342,
    imageHeightPadding = 100,	// Enlarge SVG image so extra info can be added
	imageWidth = 746,
	imageWidthPadding = 500,	// Enlarge SVG image so extra info can be added
	sourceImageHeight = 342,	// Used to compute the room scaling factor
	sourceImageWidth = 746,
	roomScalingFactor = 1.0,	// Scaling factor for room overlay
	startX = 0,					// X, Y origin coordinates.  Set in initialize()
	startY = 0,
	tempAdjustment = 50;		// Full range of RGB (0-255) is too bright

// Function for drawing the room shapes. See:
// http://www.dashingd3js.com/svg-paths-and-d3js 
// https://github.com/mbostock/d3/wiki/SVG-Shapes
var lineFunction = d3.svg.line()
			.x(function(d) {return Math.floor(d.x*roomScalingFactor); })
			.y(function(d) {return Math.floor(d.y*roomScalingFactor); })
			.interpolate("linear");

// Add image to svg element
function addImage(svg) {
	image = svg.append("image")
		.attr("xlink:href", "DOPCenter.gif")
		.attr("width", imageWidth)
		.attr("height", imageHeight)
		.attr("transform", "translate(" + startX + "," + startY + ")");
}

// Draw room temperature overlay
function drawRooms(svg) {
	
	// Assumes room array and temperature array are in the same order
	// If not, modify this section to select the temperature with the matching
	// room id
	// Animations:  See http://www.valhead.com/2013/01/04/tutorial-css-animation-fill-mode/
	// http://blogs.adobe.com/webplatform/2012/03/30/svg-animations-css-animations-css-transitions/
	
	roomShapes = svg.selectAll(".roomShape")
		.data(d3.entries(rooms))
		.enter()
		.append("path")
			// Use d.value.vertices here instead of d.vertices because we are 
			// using d3.entries().  This returns "key" : "", "value" : {}
			.attr("d", function(d) {return lineFunction(d.value.vertices)})
			.attr("stroke", "black")
			.attr("stroke-width", 2)
			.attr("fill", function(d) {return rgb(d.value.occupied, power)})
			.attr("fill-opacity", opacity)
			.attr("class", "roomShape")
			.attr("transform", "translate(" + startX + "," + startY + ")");
}

// Initialize size and location of map and animation duration
function initializeMap(height, width, x, y, time) {
	imageHeight = height;
	imageWidth = width;
	startX = x;
	startY = y;
	duration = time;
	
	// Assumes aspect ratio is preserved
	roomScalingFactor = imageHeight / sourceImageHeight; 
}

// Color rooms according to power and occupancy status
function rgb(occupied, power) {
	
	// Colors are arbitrary - could pick different ones
	// Blue for unoccupied
	// Green for occupied, power
	// Red for occupied, no power
	if (!occupied) {
		return "rgb(0,0,200)";
	} else if (occupied && power) {
		return "rgb(0,200,0)";
	} else {
		return "rgb(200,0,0)";
	}
}
	
// Set the status of the elevator with the given name.  If room does not exist,
// do nothing.
function setStatus(name, occupied) {
	for (var key in rooms) {
		if (rooms[key] == name) {
			rooms[key].occupied = occupied;
			break;
		}
	}
}

// Get data from Ptolemy
function getData(){
	$.ajax({
		url: 'simulator/data',
		type: 'GET',
		dataType: 'json',
		success: function(result) {
			// Set rooms
			for (var key in result) {
				  if (result.hasOwnProperty(key) && rooms.hasOwnProperty(key)) {
				    rooms[key].occupied = result[key];
				  }
			}
			
			// Set power
			power = result["power"];
		},
		error: function(e) {
			// Cancel page refresh if Ptolemy model has stopped running
			clearInterval(intervalHandler);
			// alert("Error retrieving data from simulator: " + JSON.stringify(e));
		}
	});
}

// Update map visualization
function updateMap(svg) {
	// Get latest data
	getData();
		
	// Update visualization
	// http://stackoverflow.com/questions/9589768/using-an-associative-array-as-data-for-d3
	
	svg.selectAll(".roomShape")
		.data(d3.entries(rooms))
		.transition()
		.duration(duration)
		// Use d.value.temperature here instead of d.vertices because we are 
		// using d3.entries().  This returns "key" : "", "value" : {}
		.attr("fill", function(d) {return rgb(d.value.occupied, power)});
}


