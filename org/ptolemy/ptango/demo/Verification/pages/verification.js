// Based on http://bl.ocks.org/e-/5244131 Self-Organizing Map example
var values = [],
	lights = [{"name":"","x":170,"y":130}],
	sensors = [{"name":"s1","x":240,"y":70,"value":0}];
    // lights = [{"name":"","y":0,"x":0},{"name":"","y":0,"x":0},{"name":"","y":0,"x":0},{"name":"","y":0,"x":0},{"name":"","y":0,"x":0}],
	// sensors = [{"x":55,"y":23,"name":"S1", "value"=800}];

// SVG markers
var blocks,
    lightMarkers,
    sensorMarkers;

var imageWidth = 584,
    imageHeight = 488;

var svg;

//Draggability
//Based on https://github.com/mbostock/d3/wiki/Drag-Behavior
//and http://bl.ocks.org/mbostock/1557377
//TODO:  Way to make this a function, so we can pass in the dataset we 
//would like to edit?  Or, way to get dataset name from graphical element?
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
			 })
			 .on("dragend", postMarkers);
			 

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
			})
			.on("dragend", postMarkers);
					


// Create the graphic elements once the DOM is loaded
$(document).ready(function() {
	// TODO:  Instead of buttons, use start drag / stop drag
	createSVG();
	
	// Register click handlers
	$("#submit").click(postThresholds);
	
	// Request light status.  This stores an HTTP request at the server which
	// is responded to as the light status changes (or, with current value 
	// within timeout)
	getLight();
	
	// Post light and sensor locations
	postMarkers();
});

// Create SVG element showing the room image plus markers for lights, sensors
function createSVG() {
	firstView = true;
	
	svg = d3.select('#map')
			.append('svg')
			.attr('width', imageWidth)
			.attr('height', imageHeight);

	svg.append("image")
		.attr("xlink:href", "livingroom.png")
		.attr("width", 584)
		.attr("height", 488);
	
	var text = "";
	
	drawMarkers();	
}

//Draw markers on the graph for the lights and sensors
function drawMarkers() {
	
	// Create circle and square markers, using a g group to link text and marker 
	// http://stackoverflow.com/questions/11350553/nested-svg-node-creation-in-d3-js
	
	for (var i = 0; i < lights.length; i++){
		var marker = svg.append("g");	// "g" stands for group
		
		marker.append("circle")
				.attr("class", "light") // So we can select by class later
				.attr("r", 23)
				.style("fill", "rgb(100, 100, 100)")
				.style("stroke", "black")
				.style("stroke-width", 3)
				.attr("x", 0)
				.attr("y", 0);
		
		marker.append("text")
			 	.attr("class", "lightLabel")
			 	.attr("x", -8)
			 	.attr("y", 5)
			 	.attr("font-family", "sans-serif")
				.attr("font-weight", "bold");
		
		marker.attr("class", "lightMarker");
	}
		
	lightMarkers = 
		d3.selectAll(".lightMarker")
				.data(lights)
				.attr("transform", function(d,i) {
					return "translate(" + [d.x, d.y] + ")"
				})
				.call(dragLights);
	
	// Data must be associated to text labels, too, in addition to markers
	// .text() must be done after .data() 
	d3.selectAll(".lightLabel")
		.data(lights)
		.text(function(d) {return d.name});
		
	for (var i = 0; i < sensors.length; i++) {
		var marker = svg.append("g");	// "g" stands for group
		
		marker.append("rect")
				.attr("class", "sensor") // So we can select by class later, 
										 // since heat map also uses rectangles,
										 // can't use selectAll("rect")
				.attr("width", 40)
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
			.attr("width", 116)
			.attr("height", 40)
			.attr("x", 40)
			.attr("y", 0)
			.style("fill", "rgb(255, 255, 255)")
			.style("stroke", "black")
			.style("stroke-width", 3);
		
		marker.append("text")
			 	.attr("class", "sensorReading")
			 	.attr("x", 55)
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
	
	// TODO:  Allow to add, remove markers using d3 enter() and exit()
	// http://mbostock.github.io/d3/tutorial/circle.html
}

// Get the current status of the light (on or off); update graphic
function getLight() {
	$.ajax({
		url: 'room/light',
		type: 'GET',
		dataType : "json", // Type of data expected from the server
		success: function(result) {
			// TODO:  Store on/off status in light array
			// Update the circle color (black for off, yellow for on)
			if (result.lightIsOn == true) {
				d3.selectAll(".light")
					.style("fill", "rgb(248, 198, 78)");
			} else {
				d3.selectAll(".light")
					.style("fill", "rgb(100, 100, 100)");
			}
			
			// Update the sensor reading
			sensors[0].value = result.lightAmount;
			d3.selectAll(".sensorReading")
			 .data(sensors)
			 .text(function(d) {return Math.floor(d.value) + " lumens"});
			
			// Issue another request
			getLight();	
		},
		error: function(e) {
			// Error message for debugging.  Otherwise, don't generate alert,
			// since an error will occur due to the last outstanding GET
			// request when the web server is stopped
			// alert("error " + JSON.stringify(e));
		}
	});
}

// Post light and sensor locations to the Ptolemy model; calculate sensor value
// Called upon completion of a drag event 
function postMarkers(){
	
	// Post the current light locations to the Ptolemy model
	// Dragging the markers around changes the values in this array
	$.ajax({
		url: 'room/markers',
		type: 'POST',
		dataType: 'json',	// Data type expected from the server
		data: {lightsList: JSON.stringify(lights), 
				sensorsList: JSON.stringify(sensors) },
		success: function(result) {
			// Set value of sensor
			// Assuming only one sensor here 
			sensors[0].value = result.lightAmount;
			d3.selectAll(".sensorReading")
			 .data(sensors)
			 .text(function(d) {return Math.floor(d.value) + " lumens"});
		},
		error: function(e) {
			alert("error " + JSON.stringify(e));
		}
	});
}

// Post thresholds to Ptolemy model
function postThresholds() {
	
	$.ajax({
		url: 'room',
		type: 'POST',
		data: {onThreshold: $("#onThreshold").val(),
				offThreshold: $("#offThreshold").val()},
		success: function(result) {
		},
		error: function(e) {
			alert("error " + JSON.stringify(e));
		}
	});
}


