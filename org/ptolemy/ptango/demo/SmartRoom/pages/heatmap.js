// Based on http://bl.ocks.org/e-/5244131 Self-Organizing Map example
var values = [],
	lights = [],
	sensors = [];
    // lights = [{"name":"","y":0,"x":0},{"name":"","y":0,"x":0},{"name":"","y":0,"x":0},{"name":"","y":0,"x":0},{"name":"","y":0,"x":0}],
	// sensors = [{"x":55,"y":23,"name":"S1", "value"=800}];

// SVG markers
var blocks,
    lightMarkers,
    sensorMarkers;

//FIXME:  Should use the width of the container. 
var imageWidth = 584,
    imageHeight = 488;

// FIXME:  Eventually, support a variable size grid
var blocksHigh = 25,  // Number of squares in grid top to bottom 58
	blocksWide = 30,  // Number of squares left to right in grid 48
	blockHeight = 20, // Height of each block
	blockWidth = 20;  // Width of each block
					  // Actual grid size in pixels will be scaled to container width

var svg;

var firstView = true;

//Draggability
//Based on https://github.com/mbostock/d3/wiki/Drag-Behavior
//and http://bl.ocks.org/mbostock/1557377
// TODO:  Way to make this a function, so we can pass in the dataset we 
// would like to edit?  Or, way to get dataset name from graphical element?
var dragLights = d3.behavior.drag()
			 .origin(Object)	// Preserves the offset between the mouse
			 					// position and the object's position
			 					// Useful for larger objects
			 .on("drag", function(d,i) {
				 	// Write changes to dataset, and copy changes to array
					d.x += d3.event.dx;	 
					d.y += d3.event.dy; 
					lights[i] = {x: d.x, y: d.y, name: d.name, value: d.value};
					d3.select(this).attr("transform", function(d,i) {
						return "translate(" + [d.x, d.y] + ")"
					});
			 });

var dragSensors = d3.behavior.drag()
			.origin(Object)	// Preserves the offset between the mouse
								// position and the object's position
								// Useful for larger objects
			.on("drag", function(d,i) {
			 		// Write changes to dataset, and copy changes to array
					d.x += d3.event.dx;	
					d.y += d3.event.dy; 
					sensors[i] = {x: d.x, y: d.y, name: d.name, value: d.value};
					d3.select(this).attr("transform", function(d,i) {
						return "translate(" + [d.x, d.y] + ")"
					});			
			});

// Create the graphic elements once the DOM is loaded
$(document).ready(function() {
	// Formatting for radio buttons.  From jQueryUI
	$("#radio-group").buttonset();
	createSVG();
});

// Delete the heat map graphic element
function clearHeatMap() {
	d3.selectAll(".value").remove();
}

// Create SVG element showing the room image plus markers for lights, sensors
function createSVG() {
	firstView = true;
	
	svg = d3.select('#room-map')
			.append('svg')
			.attr('width', imageWidth)
			.attr('height', imageHeight);

	svg.append("image")
		.attr("xlink:href", "livingroom.png")
		.attr("width", 584)
		.attr("height", 488);
	
	var text = "";
	
	// Create blank array to input to Ptolemy
	// TODO:  Figure out how to do this in Ptolemy
	/*
	text += "{";
	for (var i = 15; i < blocksWide; i++) {
		for (var j = 0; j < blocksHigh; j++) {
			text += "{x=" + i*blockHeight + ",y=" + j*blockWidth + ",value=0},"; 
		}	
	} 
	
	// There will be one extra comma.  Manually delete it.
	text +="}";
	//alert(text);
	*/
	
	getMarkers();	// Calls getHeatMap()
}

//Draw the heat map superimposed on the photo
function drawHeatMap() {

	var opacity = 0.4;   // A value between one (opaque) and zero (transparent)

	// TODO:  Learn how to use enter() and exit() d3 functions
	for (var i = 0; i < values.length; i++) {
		svg.append("rect")
			.attr("class", "value") 
			.attr("height", blockHeight)
			.attr("width", blockWidth)
			.style("fill-opacity", opacity);
	}
	
	blocks = svg.selectAll(".value");
	blocks.data(values);
	
	blocks.attr("x", function(d) {return d.x - blockWidth/2;})
	   	  .attr("y", function(d) {return d.y - blockHeight/2;})
	   	  .attr("fill", function(d){return rgb([d.value, d.value, 0]);});
	
	// Need to re-map dataset with .data(), then update labels
	d3.selectAll(".sensorReading")
		 .data(sensors)
		 .text(function(d) {return Math.floor(d.value) + " lumens"});
	
	// Optional:  Print sensor location instead of reading
	/*
	d3.selectAll(".sensorReading")
	 	.text(function(d) {return Math.floor(d.x) + " , " + Math.floor(d.y)});
	 */	
	 
}

// Draw markers on the graph for the lights and sensors
function drawMarkers() {
	
	// Create circle and square markers, using a g group to link text and marker 
	// http://stackoverflow.com/questions/11350553/nested-svg-node-creation-in-d3-js
	
	// Use d3 enter() and exit()
	// http://mbostock.github.io/d3/tutorial/circle.html
	
	var svg = d3.select("svg");
	
	var lightMarkers = svg.selectAll("lightMarker")
	                      	.data(lights)
	                      	.enter().append("g")
	                      			  .attr("class", "lightMarker");
	
	lightMarkers.append("circle")
			.attr("class", "light") // To select by class later
			.attr("r", 23)
			.style("fill", "rgb(248, 198, 78)")
			.style("stroke", "black")
			.style("stroke-width", 3)
			.attr("x", 0)
			.attr("y", 0);
	
	lightMarkers.append("text")
		 	.attr("class", "lightLabel")
		 	.attr("x", -8)
		 	.attr("y", 5)
		 	.attr("font-family", "sans-serif")
			.attr("font-weight", "bold");
	
	lightMarkers.attr("transform", function(d,i) {
					return "translate(" + [d.x, d.y] + ")"
				})
				.call(dragLights);
		
	// Data must be associated to text labels, too, in addition to markers
	// .text() must be done after .data() 
	d3.selectAll(".lightLabel")
		.data(lights)
		.text(function(d) {return d.name});
	
	var sensorMarkers = svg.selectAll("sensorMarker")
							.data(sensors)
							.enter().append("g")
							        	.attr("class", "sensorMarker");
	
	sensorMarkers.append("rect")
					.attr("class", "sensor") // So we can select by class later, 
					 // since heat map also uses rectangles,
					 // can't use selectAll("rect")
					.attr("width", 40)
					.attr("height", 40)		
					.style("fill", "rgb(161, 189, 213)")
					.style("stroke", "black")
					.style("stroke-width", 3);
	
	// Name label
	sensorMarkers.append("text")
				 	.attr("class", "sensorLabel")
				 	.attr("x", 11)
				 	.attr("y", 25)
				 	.attr("font-family", "sans-serif")
					.attr("font-weight", "bold");
	
	// Sensor reading
	sensorMarkers.append("rect")
					.attr("width", 116)
					.attr("height", 40)
					.attr("x", 40)
					.attr("y", 0)
					.style("fill", "rgb(255, 255, 255)")
					.style("stroke", "black")
					.style("stroke-width", 3);
	
	sensorMarkers.append("text")
				 	.attr("class", "sensorReading")
				 	.attr("x", 55)
				 	.attr("y", 25)
				 	.attr("font-family", "sans-serif")
					.attr("font-weight", "bold");
	
	sensorMarkers.attr("transform", function(d,i) {
					return "translate(" + [d.x, d.y] + ")"
				})
				.call(dragSensors);
	
	// Data must be associated to text labels, too, in addition to markers
	// .text() must be done after .data() 
	d3.selectAll(".sensorLabel")
		.data(sensors)
		.text(function(d) {return d.name});
	
	
	d3.selectAll(".sensorReading")
		 .data(sensors)
		 .text(function(d) {return Math.floor(d.value) + " lumens";});
	
	// Optional - Print sensor location instead of reading	
	
	/*
	d3.selectAll(".sensorReading")
		 .data(sensors)
		 .text(function(d) {return Math.floor(d.x) + " , " + Math.floor(d.y)});
		*/ 
}

// Submit a request to the Ptolemy model to get the heat map data
function getHeatMap(){
	$.get('/room/map', function(data) {
		// TODO:  JSON is being returned from Ptolemy as a string.  In future, 
		// allow setting response type to application/json so we don't need
		// $.parseJSON(data)
		var jsonData = $.parseJSON(data);
		values = jsonData.values;
		sensors = jsonData.sensors;
		
		drawHeatMap();
	})
	.fail(function() {alert("Failed to get map data from server."); });
}

// Submit a request to the Ptolemy model to get the marker locations
function getMarkers(){
	$.get('/room/markers', function(data) {
		// TODO:  JSON is being returned from Ptolemy as a string.  In future, 
		// allow setting response type to application/json so we don't need
		// $.parseJSON(data)
		var json = $.parseJSON(data);
		lights = json.lights;
		sensors = json.sensors;
		
		// If first time, draw markers
		// Called from this function since the get request is asynchronous
		// (so caller does not wait for it to complete)
		if (firstView) {
			drawMarkers();
			firstView = false;
			getHeatMap();	// Called here so that map is drawn on top of 
							// markers, so that markers can't be moved unless
							// Move Markers tab is selected
		}
	})
	.fail(function() {alert("Failed to get marker data from server."); });
}

// Post new light and sensor locations to the Ptolemy model

function postMarkers(){
	
	// Post the current light locations to the Ptolemy model
	// Dragging the markers around changes the values in this array
	$.ajax({
		url: 'room/markers',
		type: 'POST',
		dataType: 'json',
		data: {lightsList: JSON.stringify(lights), 
			sensorsList: JSON.stringify(sensors) },
		success: function(result) {
			d3.selectAll(".sensorReading")
			 .text(function(d) {return Math.floor(d.value) + " lumens"});
			getHeatMap();
		},
		error: function(e) {
			alert("error " + JSON.stringify(e));
		}
	});
}


// Creates an rgb color string from an array of three numbers each 0 to 255
// TODO:  Auto-scale graphic according to max and min luminosity
// Luminosity in example roughly ranges from 0 - 1000, so divide by 3 first
function rgb(array){
	  return 'rgb('+ array.map(function(r){	
		  			if ((r / 3) > 255) {
		  				return 255;
		  			} else {
		  				return Math.round((r / 2));}
		  			}).join(',') +')';	  			
}


