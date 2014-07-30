// Based on http://bl.ocks.org/e-/5244131 Self-Organizing Map example

// Timer for page refresh.  Declared here so we can cancel it if Ptolemy
// model stops running
var intervalHandler;

// Javascript objects to store data
var power = true;

var rooms = {
		"elevator": {
		    "vertices" : [ {"x":40, "y":315} , {"x":88, "y":315}, 
		                   {"x":88, "y":355} , {"x":40, "y":355}, 
		                   {"x":40, "y":315}],
		    "occupied" : false
		},
};

var sensors = [{"name" : "PM306", "value" : 50.5, "x" : 715.0, "y" : 290.0, 
					"power" : true}, 
               {"name" : "PM307", "value" : 22.5, "x" : 870.0, "y" : 350.0,
					"power" : true}, 
               {"name" : "PM308", "value" : 72.0, "x" : 890.0, "y" : 270.0,
					"power" : true}];

// SVG temperature overlay
var image,
    roomShapes,
    sensorMarkers;

// Opacity of overlay.  Between one (opaque) and zero (transparent)
var opacity = 0.7;

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
		.attr("xlink:href", "SwarmLabFloorPlan.svg")
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

// Draw markers for sensors and their values
function drawMarkers(){
	for (var i = 0; i < sensors.length; i++) {
		var marker = svg.append("g");	// "g" stands for group
		
		marker.append("rect")
				.attr("class", "sensor") // So we can select by class later, 
										 // since heat map also uses rectangles,
										 // can't use selectAll("rect")
				.attr("width", 70)
				.attr("height", 40)		
				.style("fill", "rgb(161, 189, 213)")
				.style("stroke", "black")
				.style("stroke-width", 3);
		
		// Name label
		marker.append("text")
			 	.attr("class", "sensorLabel")
			 	.attr("x", 11)
			 	.attr("y", 25)
			 	.attr("font-family", "sans-serif")
				.attr("font-weight", "bold");
		
		// Sensor reading
		marker.append("rect")
			.attr("class", "sensorReadingBox")
			.attr("width", 66)
			.attr("height", 40)
			.attr("x", 70)
			.attr("y", 0)
			//.style("fill", "rgb(255, 255, 255)")
			.style("stroke", "black")
			.style("stroke-width", 3);
		
		marker.append("text")
			 	.attr("class", "sensorReading")
			 	.attr("x", 80)
			 	.attr("y", 25)
			 	.attr("font-family", "sans-serif")
				.attr("font-weight", "bold");
		
		marker.attr("class", "sensorMarker");
	}
	
	sensorMarkers = 
		d3.selectAll(".sensorMarker")
				.data(sensors)
				.attr("transform", function(d,i) {
					return "translate(" + [d.x, d.y] + ")"
				});
	
	updateMarkers();
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
			power = result.power;
			
			if (result.hasOwnProperty("power306")) {
				sensors[0].power = result.power306;
			} else {
				sensors[0].power = power;
			}
			
			if (result.hasOwnProperty("power307")) {
				sensors[1].power = result.power307;
			} else {
				sensors[1].power = power;
			}
			
			if (result.hasOwnProperty("power308")) {
				sensors[2].power = result.power308;
			} else {
				sensors[2].power = power;
			}		
			
			// Draw or upate rooms
			if (firstView) {
				addImage(svg);
				drawRooms(svg);
			} else {
				updateMap(svg);
			}
			getMarkerData();
		},
		error: function(e) {
			// Cancel page refresh if Ptolemy model has stopped running
			clearInterval(intervalHandler);
			// alert("Error retrieving data from simulator: " + JSON.stringify(e));
		}
	});
}

//Submit a request to the Ptolemy model to get the marker locations
function getMarkerData(){
	$.get('/simulator/markers', function(data) {
		// TODO:  JSON is being returned from Ptolemy as a string.  In future, 
		// allow setting response type to application/json so we don't need
		// $.parseJSON(data)
		var json = $.parseJSON(data);
		sensors[0].value = json.reading306;
		sensors[1].value = json.reading307;
		sensors[2].value = json.reading308;
		
		// If first time, draw markers
		// Called from this function since the get request is asynchronous
		// (so caller does not wait for it to complete)
		if (firstView) {
			drawMarkers();
			firstView = false;
		} else {
			updateMarkers();
		}
	})
	.fail(function() {alert("Failed to get marker data from server."); });
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

function rgbLive(power) {
	// White for operating
	// Gray for power outage
	if (power) {
		return "rgb(255,255,255)";
	} else {
		return "rgb(50,50,50)";
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

function updateMarkers(){
	// Data must be associated to text labels, too, in addition to markers
	// .text() must be done after .data() 
	d3.selectAll(".sensorLabel")
		.data(sensors)
		.text(function(d) {return d.name});
	
	
	d3.selectAll(".sensorReadingBox")
		.data(sensors)
		.transition()
		.duration(duration)
		.attr("fill", function(d) {return rgbLive(d.power)});
	
	d3.selectAll(".sensorReading")
		 .data(sensors)
		 .text(function(d) {return Math.floor(d.value) + " W";});
}



