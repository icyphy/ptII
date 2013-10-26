// Create graphics for index.html file
// Uses map.js

// Interval at which this page samples data - here, 2 seconds (2000 ms)
var interval = 2000;

// The svg element
var svg;

// Create the graphic elements once the DOM is loaded
$(document).ready(function() {	
	svg = createSVG();
	initializeMap(342, 746, 0, 0, interval);
	addImage(svg);
	drawRooms(svg);
	
	// Update temperature map periodically
	var counter = 0; 
	intervalHandler = setInterval(function() {
		
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