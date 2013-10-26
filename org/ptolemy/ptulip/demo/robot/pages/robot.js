//  Grid size 
// FIXME:  Eventually, support a variable size grid instead of fixed=pixel width
var blocksHigh = 2,  
	blocksWide = 3,  
	blockHeight = 150, // Height of each block in pixels
	blockWidth = 150;  // Width of each block in pixels


// Array of marker locations
var markerLocations = [ {name: "robot", "x": blockWidth/4, "y" : blockHeight/4}, 
                        {name: "treasure", "x" : blockWidth + blockWidth/4, 
								"y" : blockHeight/4}, 
                        {name: "park", "x" : 2*blockWidth + blockWidth/4, 
								"y" : blockHeight/4}];

// Array of regions markers are in.  Calculated by post request
var markerRegions = ["X0", "X1", "X2"];

// SVG markers
var blocks,
    markers;

// SVG element 
var svg;

// Draggability
// Based on https://github.com/mbostock/d3/wiki/Drag-Behavior
// and http://bl.ocks.org/mbostock/1557377
//TODO:  Way to make this a function, so we can pass in the dataset we 
//would like to edit?  Or, way to get dataset name from graphical element?
var drag = d3.behavior.drag()
			 .origin(Object)	// Preserves the offset between the mouse
			 					// position and the object's position
			 					// Useful for larger objects
			 .on("drag", function(d,i) {
				 	// Write changes to d3 dataset and copy to array
					d.x += d3.event.dx;	 
					d.y += d3.event.dy; 
					markerLocations[i] = {x: d.x, y: d.y, name: d.name};
					d3.select(this).attr("x", function(d,i) {return d.x})
								   .attr("y", function(d,i) {return d.y});
				
			 });

//Create the graphic elements once the DOM is loaded
$(document).ready(function() {
	// Clear result textbox
	$("#result").val("");
	// Hide loading message
	$("#loading").hide(); 
	// TODO:  Add jQuery UI button formatting
	createSVG();
});

//Create SVG element showing the room image plus markers for lights, sensors
function createSVG() {
	firstView = true;
	
	svg = d3.select('#map')
			.append('svg')
			.attr('width', blocksWide * blockWidth)
			.attr('height', blocksHigh * blockHeight);
	
	// Draw grid
	// Use a group so we can label rectangles 
	for (var i = 0; i < blocksWide; i++) {
		for (var j = 0; j < blocksHigh; j++) {
			var labelNumber = i + j*blocksWide;
			
			var square = svg.append("g");	// "g" stands for group
			
			square.append("rect")
				.attr("class", "gridSquare")
				.attr("width", blockWidth)
				.attr("height", blockHeight)
				.attr("x", 0)
				.attr("y", 0)
				.attr("rx", 8)// Rounded corners
				.attr("ry", 8)
				//.style("fill", "rgb(184, 200, 220)")
				.style("fill", "rgb(218, 227, 237)")
				.style("stroke", "rgb(94,122,169")
				.style("stroke-width", 2);
			
			
			square.append("text")
				 	.attr("class", "gridLabel")
				 	.attr("x", 20 + blockWidth/4)
				 	.attr("y", 40 + blockHeight/4)
				 	.attr("font-family", "sans-serif")
					.attr("font-weight", "bold")
					.attr("color", "rgba(0, 0, 0, 0.5)")
					.text(function() {return "X" + labelNumber});
			
			square.attr("transform", function() {
				return "translate(" +[i*blockWidth, j*blockHeight] +")"
				});
		}	
	}
	
	// Add markers
	svg.append("image")	
			.attr("xlink:href", "robot.png")
			.attr("class", "marker")
			.attr("width", 69)
			.attr("height", 63);
	
	svg.append("image")
			.attr("xlink:href", "star.png")
			.attr("class", "marker")
			.attr("width", 59)
			.attr("height", 52);
	
	svg.append("image")
			.attr("xlink:href", "power.png")
			.attr("class", "marker")
			.attr("width", 58)
			.attr("height", 51);
	
	markers = d3.selectAll(".marker")
					.data(markerLocations)
					.attr("x", function(d,i) {return d.x})
					.attr("y", function(d,i) {return d.y})
					.call(drag);
}

// Calculate which region each marker is in.  Print info to page.  
// TODO:  In future, info will be posted to server, and server will
// return controller specification
function calculate(){
	var regionNumber;
	var infoText = "";
	
	// Figure out which node each marker is in
	for (var i = 0; i < markerLocations.length; i++){
		regionNumber = (Math.floor(markerLocations[i].x / blockWidth)) 
				+ (Math.floor(markerLocations[i].y / blockHeight)) * blocksWide;
		markerRegions[i] = "X" + regionNumber;
	}
		
	// Animated marker transition to middle of region.  Useful if someone has 
	// placed a marker near the edge
	
	var newX, newY;
	
	markers.each(function(d,i) {
		newX = Math.floor(d.x/blockWidth)*blockWidth + blockWidth/4;
		newY = Math.floor(d.y/blockHeight)*blockHeight + blockHeight/4;
		d3.select(this)
			.transition()
				.attr("x", function(d,i) {return newX})
				.attr("y", function(d,i) {return newY});
		
		// Update underlying array
		markerLocations[i].x = newX;
		markerLocations[i].y = newY;
	});			
	
	// TODO:  In future, this won't be needed
	infoText = "Calculating for Treasure: " + markerRegions[1] + ", Park: " 
				+ markerRegions[2]; 
	
	$("#loadingMessage").html(infoText); 
	
	// Post region info to Ptolemy server, which will calculate and 
	// return an automaton
	postRegions();
}

// Post the treasure and park regions to the Ptolemy model
function postRegions(){
	// The ajax function is asynchronous, so set the result text
	// in the success callback to ensure that a response has 
	// been received from the Ptolemy model
	$.ajax({
		url: 'robot',
		type: 'POST',
		dataType: 'text',	// The type of data expected from the server
							// Use type 'text' so we can display easily
		data: {treasureRegion: markerRegions[1],
				parkRegion: markerRegions[2]},
		success: function(result) {
			$("#result").val(result);
		},
		error: function(jqXHR, textStatus, errorThrown) {
			alert("Error posting data to the server.");
			//alert("Error code: " + jqXHR);
			//alert("Error text status: " + textStatus);
			//alert("Error thrown: " + errorThrown);
		}
	});
}

// Grey out page while loading.  See:
// http://stackoverflow.com/questions/1964839/jquery-please-wait-loading-animation

$(document).ajaxStart(function() { 
	 $("#loading").show();
});

$(document).ajaxStop(function() { 
     $("#loading").hide(); 
});
