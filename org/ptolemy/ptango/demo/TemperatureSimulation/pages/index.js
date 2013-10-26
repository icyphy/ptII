// Create graphics for index.html file
// Uses tempmap.js

// Interval at which this page samples data - here, 2 seconds (2000 ms)
var interval = 2000;

// Test data
// var newArray = [21.0, 21.4, 21.6, 20.6, 20.8, 21.5, 21.8, 20.3, 22.4, 19.4 ];

// The svg element
var svg;

// Create the graphic elements once the DOM is loaded
$(document).ready(function() {
	// Delete all rooms with a negative number for a key
	// These only have simulation data
	for (key in rooms){
		if (key < 0) {
			delete rooms[key];
		}
	}
	
	svg = createSVG();
	initializeMap(767, 432, 0, 0, interval, minTemp, maxTemp);
	addImage(svg);
	drawRooms(svg);
	drawRoomInfo(svg);
	drawGradient(svg);
	
	// Update temperature map periodically
	var counter = 0; 
	var intervalHandler = setInterval(function() {
		
		updateMap(svg);

		// Uncomment to stop after 10 iterations
		/*
		counter +=1;
		if (counter >= 10) {
			clearInterval(intervalHandler);
		}
		*/
		
	}, interval);
});

// Create SVG element to hold the temperature map
function createSVG() {
	firstView = true;
	
	var svg = d3.select('#room-map')
			.append('svg')
			.attr('width', imageWidth + imageWidthPadding)
			.attr('height', imageHeight);

	return svg;
}

// Called by sensorandrew.js.  Need function defined here to access svg variable 
function updateLabels() {
	updateRoomInfoGroup(svg);
}