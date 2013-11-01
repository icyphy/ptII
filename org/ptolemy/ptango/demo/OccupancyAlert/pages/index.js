// Create graphics for index.html file
// Uses simulator.js

// The svg element
var svg;

// Time interval data is sampled at, in milliseconds
var interval = 2000;

// Create the graphic elements once the DOM is loaded
$(document).ready(function() {	
	svg = createSVG();
	
	// Update temperature map periodically
	var counter = 0; 
	intervalHandler = setInterval(function() {
		
		getData();

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