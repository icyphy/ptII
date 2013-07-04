// Based on http://bl.ocks.org/e-/5244131 Self-Organizing Map example

// tranData is used here, but defined in sensorandrew.js
// Please make sure sensorandrew.js is included first in the html page

// Room:  {name, [vertices {x, y pairs} ], id, temperature}.  
// Rooms are irregular-shaped polygons defined by a set of vertices. 
// The id is the id number of the sensor data feed, the temperature is the 
// latest data sample, and the timestamp is the time of the last sample 
// (used for animating the map)
// An SVG path is drawn connecting these vertices
// Vertices should be specified in order - a path is drawn between adjacent 
// vertices

// Create a Javascrpt object (in the style of an associative array) to hold data
// http://blog.xkoder.com/2008/07/10/javascript-associative-arrays-demystified/
// Use the sensor feed id as the key

var rooms = {
		"1052": {
			"name": "202", 
		    "vertices" : [ {"x":233, "y":45} , {"x":319, "y":45}, 
		                   {"x":319, "y":174} , {"x":233, "y":174}, 
		                   {"x":233, "y":45}],
		    "temperature" : 21.2
		},
		"1053": {
			"name": "203", 
		    "vertices" : [ {"x":324, "y":45} , {"x":456, "y":45}, 
		                   {"x":456, "y":174} , {"x":324, "y":174}, 
		                   {"x":324, "y":45}],
		    "temperature" : 21.2
		},
		"1054": {
			"name": "205", 
		    "vertices" : [ {"x":462, "y":45} , {"x":597, "y":45}, 
		                   {"x":597, "y":226} , {"x":462, "y":226}, 
		                   {"x":462, "y":45}],
		    "temperature" : 21.2
		},
		"1055": {
			"name": "206", 
		    "vertices" : [ {"x":420, "y":230} , {"x":597, "y":230}, 
		                   {"x":597, "y":362} , {"x":420, "y":362}, 
		                   {"x":420, "y":230}],
		    "temperature" : 21.2
		},
		"1056": {
			"name": "208", 
		    "vertices" : [ {"x":420, "y":368} , {"x":597, "y":368}, 
		                   {"x":597, "y":637} , {"x":420, "y":637}, 
		                   {"x":420, "y":368}],
		    "temperature" : 21.2
		},
		"1057": {
			"name": "212", 
		    "vertices" : [ {"x":468, "y":642} , {"x":597, "y":642}, 
		                   {"x":597, "y":912} , {"x":468, "y":912}, 
		                   {"x":468, "y":642}],
		    "temperature" : 21.2
		},
		"1058": {
			"name": "214", 
		    "vertices" : [ {"x":324, "y":921} , {"x":419, "y":921}, 
		                   {"x":419, "y":916} , {"x":597, "y":916}, 
		                   {"x":597, "y":1099} , {"x":324, "y":1099},
		                   {"x":324, "y":921}],
		    "temperature" : 21.2
		},
		"1059": {
			"name": "219", 
		    "vertices" : [ {"x":46, "y":916} , {"x":224, "y":916}, 
		                   {"x":224, "y":920} , {"x":320, "y":920}, 
		                   {"x":320, "y":1099} , {"x":46, "y":1099},
		                   {"x":46, "y":916}],
		    "temperature" : 21.2
		},
		"1060": {
			"name": "220", 
		    "vertices" : [ {"x":46, "y":642} , {"x":224, "y":642}, 
		                   {"x":224, "y":912} , {"x":46, "y":912}, 
		                   {"x":46, "y":642}],
		    "temperature" : 21.2
		},
		"1061": {
			"name": "222", 
		    "vertices" : [ {"x":46, "y":367} , {"x":224, "y":367}, 
		                   {"x":224, "y":638} , {"x":46, "y":638}, 
		                   {"x":46, "y":367}],
		    "temperature" : 21.2
		},
		"1062": {
			"name": "224", 
		    "vertices" : [ {"x":46, "y":45} , {"x":228, "y":45}, 
		                   {"x":228, "y":222} , {"x":224, "y":222}, 
		                   {"x":224, "y":331} , {"x":234, "y":331},
		                   {"x":234, "y":358} , {"x":224, "y":358},
		                   {"x":224, "y":363} , {"x":46, "y":363},
		                   {"x":46, "y":47}],
		    "temperature" : 21.2
		 }
};

var newArray = [21.0, 21.4, 21.6, 20.6, 20.8, 21.5, 21.8, 20.3, 22.4, 19.4 ];


// The SVG HTML element 
var svg;

// SVG temperature overlay
var roomShapes,
    roomInfoGroup;

// Opacity of temperature overlay.  Between one (opaque) and zero (transparent)
var opacity = 0.7;

// Image dimensions.  Assumes a fixed-size image
// Scaife floor 2 dimensions 648 x 1147
var graphPadding = 30,			// Padding around heat map
	gradientWidth = 30,			// Width of temperature gradient scale
	imageHeight = 1147,
    imageHeightPadding = 100,	// Enlarge SVG image so extra info can be added
	imageWidth = 648,
	imageWidthPadding = 500,	// Enlarge SVG image so extra info can be added
	labelPadding = 60,			// Padding around room temperature readings
	rectHeight = 40,			// Height and width of room temperature readings
	rectWidth = 60,
	tempAdjustment = 50;		// Full range of RGB (0-255) is too bright

// Function for drawing the room shapes. See:
// http://www.dashingd3js.com/svg-paths-and-d3js 
// https://github.com/mbostock/d3/wiki/SVG-Shapes
var lineFunction = d3.svg.line()
						.x(function(d) {return d.x; })
						.y(function(d) {return d.y; })
						.interpolate("linear");

// Interval at which this page samples data - here, 2 seconds (2000 ms)
var interval = 2000;

var minTemp = 19.3,
maxTemp = 22.4;

// Create the graphic elements once the DOM is loaded
$(document).ready(function() {
	createSVG();
	
	// For first demo - update temperatures according to sampling interval
	// Stop when we are out of data
	var counter = 0; 
	var intervalHandler = setInterval(function() {
		// Update temperature dataset.  

		// Comment out this sample data once Sensor Andrew connection is working
		for (key in rooms){
			rooms[key].temperature = newArray[counter];
		}
		
		
		// Update visualization
		// http://stackoverflow.com/questions/9589768/using-an-associative-array-as-data-for-d3
		
		svg.selectAll(".roomShape")
			.data(d3.entries(rooms))
			.transition()
			.duration(interval)
			// Use d.value.temperature here instead of d.vertices because we are 
			// using d3.entries().  This returns "key" : "", "value" : {}
			.attr("fill", function(d) {return rgb(d.value.temperature)});
		

		// TODO:  Update map instantaneously also?  Right now, updates every 
		// 2 seconds.  Need timestamping then since animation is smoothed.
		
		// Comment out once Sensor Andrew connection is working
		// Login seems to be down?  07/03/2013 4:55 pm EST
		roomInfoGroup.selectAll(".roomTempRect")
			.data(d3.entries(rooms))
			.transition()
			.duration(interval)
			.attr("fill", function(d) {return rgb(d.value.temperature)});
	
		roomInfoGroup.selectAll(".roomTempLabel")
			.data(d3.entries(rooms))
			.text(function (d) {return d.value.temperature});
		
		// Stop after 10 iterations
		counter +=1;
		if (counter >= 10) {
			clearInterval(intervalHandler);
		}
	}, interval);
});


// Create SVG element showing the room image plus markers for sensors
function createSVG() {
	firstView = true;
	
	svg = d3.select('#room-map')
			.append('svg')
			.attr('width', imageWidth + imageWidthPadding)
			.attr('height', imageHeight);

	// Comment out to remove the background image
	
	svg.append("image")
		.attr("xlink:href", "scaifefloor2.png")
		.attr("width", imageWidth)
		.attr("height", imageHeight);
	
	// Create gradient as a legend for the temperature scale
	// http://bl.ocks.org/mbostock/1086421	
	
	var gradientGroup = svg.append("g")
		.attr("transform", "translate(" + 
				(imageWidth + graphPadding + 4*labelPadding) + 
				"," + 1.5*labelPadding + ")");
	
	var gradient = gradientGroup.append("svg:defs")
		.append("svg:linearGradient")
			.attr("id", "gradient")
			.attr("x1", "0%")
			.attr("x2", "0%")
			.attr("y1", "0%")
			.attr("y2", "100%")
			.attr("spreadMethod", "pad");
			
	gradient.append("svg:stop")
	    .attr("offset", "0%")
	    .attr("stop-color", "rgb(" + (255 - tempAdjustment) + ",0,0")
	    .attr("stop-opacity", opacity);
	
	gradient.append("svg:stop")
	    .attr("offset", "100%")
	    .attr("stop-color", "rgb(0,0," + (255 - tempAdjustment) + ")")
	    .attr("stop-opacity", opacity);
	
	gradientGroup.append("svg:rect")
	    .attr("width", gradientWidth)
	    .attr("height", 11*labelPadding)
	    .style("fill", "url(#gradient)");	
	
	// Draw an axis on the temperature scale
	// http://alignedleft.com/tutorials/d3/axes/
	
	var tempScale = d3.scale.linear()
		.domain([maxTemp, minTemp])
		.range([0, 11*labelPadding]);

	var tempAxis = d3.svg.axis()
		.scale(tempScale)
		.orient("right")
		.ticks(10);
		//.tickFormat(function(d) {return d + " &degC"});
	
	// Vertical temperature scale and labels
	
	gradientGroup.append("g")
		.attr("class", "axis")
		.attr("transform", "translate(" + gradientWidth + ",0)")
		.call(tempAxis);
	
	// Add axis label.  Use unicode representation for degree symbol in text
	// http://www.d3noob.org/2012/12/adding-axis-labels-to-d3js-graph.html
	// Note x and y are reversed due to 90 degree rotation
	
	svg.append("text")
		.attr("class", "axisLabel")
		.attr("x", imageHeight/2.5)
		.attr("y", -(imageWidth + graphPadding + 3*gradientWidth + 4*labelPadding))
		.attr("transform", "rotate(90)")
		.style("text-anchor", "middle")
		.text("Temperature (\u00B0C)");
		
	
	drawRooms();
}

// Draw room temperature overlay
function drawRooms() {
	
	// Assumes room array and temperature array are in the same order
	// If not, modify this section to select the temperature with the matching
	// room id
	// Animations:  See http://www.valhead.com/2013/01/04/tutorial-css-animation-fill-mode/
	// http://blogs.adobe.com/webplatform/2012/03/30/svg-animations-css-animations-css-transitions/
	svg.selectAll(".roomShape")
		.data(d3.entries(rooms))
		.enter()
		.append("path")
			// Use d.value.vertices here instead of d.vertices because we are 
			// using d3.entries().  This returns "key" : "", "value" : {}
			.attr("d", function(d) {return lineFunction(d.value.vertices)})
			.attr("stroke", "black")
			.attr("stroke-width", 2)
			.attr("fill", function(d) {return rgb(d.value.temperature)})
			.attr("fill-opacity", opacity)
			.attr("class", "roomShape");
			
	
	// Add individual room info and temperatures
	// Shows change in temperature readings in case animation is too smooth
	
	roomInfoGroup = svg.append("g")
			.attr("transform", "translate(" + 
		 			(imageWidth + labelPadding) + "," + labelPadding + ")");
	
	roomInfoGroup.append("text")
		.attr("x", "0")
		.attr("y", "0")
		.attr("font-family", "sans-serif")
		.attr("font-size", 26)
		.text("Room Temperatures");
	
	roomInfoGroup.selectAll(".roomInfoLabel")
		.data(d3.entries(rooms))
		.enter()
		.append("text")
			.attr("class", "roomInfoLabel")
			.attr("x", "0")
			.attr("y", function(d,i) {return i*labelPadding + labelPadding})
			.attr("font-family", "sans-serif")
			.attr("font-size", 22)
			.style("text-anchor", "left")
			.text(function (d) {return d.value.name});
	
	roomInfoGroup.selectAll(".roomTempRect")
		.data(d3.entries(rooms))
		.enter()
		.append("rect")
			.attr("class", "roomTempRect")
			.attr("x", labelPadding)
			.attr("y", function(d,i) {return i*labelPadding - labelPadding/2.5 + labelPadding - 2})
			.attr("rx", 5)
			.attr("ry", 5)
			.attr("height", rectHeight)
			.attr("width", rectWidth)
			.attr("fill", function(d) {return rgb(d.value.temperature)})
			.attr("fill-opacity", opacity);
	
	roomInfoGroup.selectAll(".roomTempLabel")
		.data(d3.entries(rooms))
		.enter()
		.append("text")
			.attr("class", "roomTempLabel")
			.attr("x", 1.5*labelPadding)
			.attr("y", function(d,i) {return i*labelPadding + labelPadding})
			.attr("font-family", "sans-serif")
			.attr("font-size", 22)
			.attr("fill", "white")
			.style("text-anchor", "middle")
			.text(function (d) {return d.value.temperature}); 
}

// Create an rgb color string from an array of three numbers each 0 to 255
// Scale according to temperature range
function rgb(temp) {
	
	// Handle too low / too high values
	if (temp < minTemp){
		temp = minTemp;
	} else if (temp > maxTemp) {
		temp = maxTemp;
	}
	
	// Scale
	var red = Math.floor(((temp - minTemp) / (maxTemp - minTemp)) * 255) 
				- tempAdjustment;
	var yellow = 0;
	var blue = 255 - Math.floor(((temp - minTemp) / (maxTemp - minTemp)) * 255) 
				- tempAdjustment;
	
	return "rgb(" + red + "," + yellow + "," + blue + ")";
}


