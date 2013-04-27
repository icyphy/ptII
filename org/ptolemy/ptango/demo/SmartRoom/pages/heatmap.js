// Based on http://bl.ocks.org/e-/5244131 Self-Organizing Map example
// var values = [];
var values = [{"x":0,"y":0,"value":74.5}],
	lights = [],
	sensors = [];
    // lights = [{"name":"","y":0,"x":0},{"name":"","y":0,"x":0},{"name":"","y":0,"x":0},{"name":"","y":0,"x":0},{"name":"","y":0,"x":0}],
    //sensors = [{"x":55,"y":23,"name":"S1"}];

var temp = [];
var text = "";

// SVG markers
var circles,
    squares,
    labels,
    blocks;

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

// Draggability - Not working yet
// Based on https://gist.github.com/enjalot/1378144
var drag = d3.behavior.drag()
	.on("drag", function(d,i) {
		d.x += d3.event.dx
		d.y += d3.event.dy
		d3.select(this).attr("transform", function(d,i){
		return "translate(" + [ d.x,d.y ] + ")"
	})
});


// Create the graphic elements once the DOM is loaded
$(document).ready(function() {
	createSVG();
});

// TODO: Implement heat map toggle on/off
// Delete the heat map graphic element
function clearHeatMap() {
	
}

// Create SVG element showing the room image plus markers for lights, sensors
function createSVG() {
	
	// Create the SVG element and append to the body
	// FIXME: Next, have fixed place on page
	svg = d3.select('#room-map')
			.append('svg')
			.attr('width', imageWidth)
			.attr('height', imageHeight);

	svg.append("image")
		.attr("xlink:href", "livingroom.png")
		.attr("width", 584)
		.attr("height", 488);
	
	// Create blank array to input to Ptolemy
	// TODO:  Figure out how to do this in Ptolemy
	/*
	text += "{";
	for (var i = 15; i < blocksWide; i++) {
		for (var j = 0; j < blocksHigh; j++) {
			temp.push({
				x: i*blockWidth,
			    y: j*blockHeight,
			    value: 0
			})
			text += "{x=" + i*blockHeight + ",y=" + j*blockWidth + ",value=0},"; 
		}	
	} 
	
	// There will be one extra comma.  Manually delete it.
	text +="}";
	//alert(text);
	*/
	
	getMarkers();
	getData();
}

// Draw markers on the graph for the lights and sensors
function drawMarkers() {
	
	// Create circles and squares 
	// FIXME:  Use a g grouping, e.g. 
	// http://stackoverflow.com/questions/11350553/nested-svg-node-creation-in-d3-js
	
	for (var i = 0; i < lights.length; i++){
		svg.append("circle")
				.attr("class", "light") // So we can select by class later
										 // (circles don't need it, but keeps
										 // syntax consistent with rectangles)
				.attr("r", 23)
				.style("fill", "rgb(248, 198, 78)")
				.style("stroke", "black")
				.style("stroke-width", 3);
		
		svg.append("text")
			.attr("class", "lightLabel");
	}
	
	for (var i = 0; i < sensors.length; i++) {
		svg.append("rect")
			.attr("class", "sensor") // So we can select by class later, 
									 // since heat map also uses rectangles,
									 // can't use selectAll("rect")
			.attr("width", 40)
			.attr("height", 40)		
			.style("fill", "rgb(161, 189, 213)")
			.style("stroke", "black")
			.style("stroke-width", 3);
		
		svg.append("text")
			.attr("class", "sensorLabel");
	}
	
	circles = svg.selectAll(".light");
	squares = svg.selectAll(".sensor");
	circleLabels = svg.selectAll(".lightLabel");
	squareLabels = svg.selectAll(".sensorLabel");
	
	// Map the data to the visual representation 
	circles.data(lights);
	squares.data(sensors);
	circleLabels.data(lights);
	squareLabels.data(sensors);
	
	
	// TODO:  Allow dynamic markers.  This isn't working...
	// Add any new markers
	// circles.enter().append("circle")
	//	.attr("class", "light") 
	//	.attr("r", 23)
	//	.style("fill", "rgb(248, 198, 78)")
	//	.style("stroke", "black")
	//	.style("stroke-width", 3);	

	// circleLabels.enter().append("text")
	//		 .attr("class", "lightLabel");
	
	//
	// squares.enter().append("rect")
	// 		.attr("width", 40)
	//		.attr("height", 40);
	
	// Delete any extra markers
	// circles.exit().remove();
	// squares.exit().remove();

	// Set marker locations and labels
	// The function(d) refers to the dataset mapped to the visual elements
	// FIXME:  Refactor this stuff into CSS file?  Should be possible?
	circles.attr("cx", function(d) {return d.x;})
	       .attr("cy", function(d) {return d.y;});
	       
	
	squares.attr("x", function(d) {return d.x;})
		   .attr("y", function(d) {return d.y;});
		   
	
	// Add labels. The label location is a bit different for circles vs. squares
	circleLabels.attr("x", function(d) {return d.x - 8;})
				.attr("y", function(d) {return d.y + 5;})
				.attr("font-family", "sans-serif")
				.attr("font-weight", "bold")
				.text(function(d) {return d.name;});
	
	squareLabels.attr("x", function(d) {return d.x + 11;})
	            .attr("y", function(d) {return d.y + 25;})
	            .attr("font-family", "sans-serif")
	            .attr("font-weight", "bold")
	            .text(function(d) {return d.name;});
	
	// Make draggable
	// How to get the labels to follow?  Use g grouping?
	// Based on https://gist.github.com/enjalot/1378144
	
}

// Draw the heat map superimposed on the photo
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
	
	/*
	var graphNodes = svg.append('g').attr('class','nodes');

	graphNodes
		.selectAll('rect')
		.data(values)
		.enter().append('rect')
			.attr('x', function(node){return node.x})
			.attr('y', function(node){return node.y})
			.attr('height', blockHeight)
			.attr('width', blockWidth)
			.style('fill', function(node){return rgb([node.value, node.value, 0]);})
			.style('fill-opacity', opacity);
	*/
}

// Submit a request to the Ptolemy model to get the heat map data
function getData(){
	$.get('/room/data', function(data) {
		// TODO:  JSON is being returned from Ptolemy as a string.  In future, 
		// allow setting response type to application/json so we don't need
		// $.parseJSON(data)
		values = $.parseJSON(data);
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
		
		drawMarkers();
	})
	.fail(function() {alert("Failed to get marker data from server."); });
}

// Creates an rgb color string from an array of three numbers each 0 to 255
// TODO:  Auto-scale graphic according to max and min luminosity
// Luminosity in example roughly ranges from 0 - 1000, so divide by 10 first
function rgb(array){
	  return 'rgb('+ array.map(function(r){	
		  			if ((r / 3) > 255) {
		  				return 255;
		  			} else {
		  				return Math.round((r / 2));}
		  			}).join(',') +')';	  			
}


