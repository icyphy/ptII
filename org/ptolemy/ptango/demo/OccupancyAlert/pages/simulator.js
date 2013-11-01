// Based on http://bl.ocks.org/e-/5244131 Self-Organizing Map example

// Timer for page refresh.  Declared here so we can cancel it if Ptolemy
// model stops running
var intervalHandler;

// Javascript objects to store data
var power = true;
var rooms = {
		"elevator": { 
		    "vertices" : [ {"x":50, "y":325} , {"x":78, "y":325}, 
		                   {"x":78, "y":355} , {"x":50, "y":355}, 
		                   {"x":50, "y":325}],
		    "occupied" : false
		},
};

// SVG temperature overlay
var image,
    roomShapes;

// Opacity of overlay.  Between one (opaque) and zero (transparent)
var opacity = 0.7

// Duration of the room map animated transition, in ms
var duration = 2000;

// True if this is the first view
var firstView = true;

// Image dimensions.  Assumes a fixed-size image
// Swarm Lab picture dimensions 54.5 in x 24.5 in or 5232 px x 2352 px
// Swarm Lab picture resized to 10.9 in x 4.9 in or 1046 px x 470 px
var sourceImageHeight = 470,	// Used to compute the room scaling factor
	sourceImageWidth = 1046;

var graphPadding = 30,			// Padding around heat map
	imageHeight = sourceImageHeight,
    imageHeightPadding = 0,	// Enlarge SVG image so extra info can be added
	imageWidth = sourceImageWidth,
	imageWidthPadding = 50,	// Enlarge SVG image so extra info can be added
	roomScalingFactor = 1.0,	// Scaling factor for room overlay
	startX = 0,					// X, Y origin coordinates.  Set in initialize()
	startY = 0;

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
		.attr("xlink:href", "Swarm Lab floorplan.svg")
		.attr("width", 1046)
		.attr("height", 470)
		.attr("transform", "translate(" + startX + "," + startY + ")");
}

// Draw room temperature overlay
function drawRooms(svg) {
	
	// Draw room overlay
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

// Get data from Ptolemy and update rooms.  Room-updating must be done in 
// callback since .ajax is asynchronous - wait for data before drawing
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
			
			// Draw or upate rooms
			if (firstView) {
				addImage(svg);
				drawRooms(svg);
				firstView = false;
			} else {
				updateMap(svg);
			}
		},
		error: function(e) {
			// Cancel page refresh if Ptolemy model has stopped running
			clearInterval(intervalHandler);
			// alert("Error retrieving data from simulator: " + JSON.stringify(e));
		}
	});
}

// Color rooms according to power and occupancy status
function rgb(occupied, power) {
	
	// Colors are arbitrary - could pick different ones
	// Clear for unoccupied - white with 0 opacity - underlying image is orange
	// Green for occupied, power
	// Red for occupied, no power
	if (!occupied) {
		return "rgb(0,200,350)";
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

// Update map visualization
function updateMap(svg) {
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


