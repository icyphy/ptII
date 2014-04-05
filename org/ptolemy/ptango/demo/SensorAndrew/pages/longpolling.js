// Javascript for Sensor Andrew - Ptolemy web service

// An object to hold data for each room
// Create graphics for index.html file
// Uses tempmap.js

// Interval at which this page samples data - here, 2 seconds (2000 ms)
var interval = 2000;

// The svg element
var svg;

// Create the graphic elements once the DOM is loaded
$(document).ready(function() {
	// Delete all rooms with a negative number for a key
	// These only have simulation data
	// NOTE:  Key here is now the room number, vs. the id in 
	// TemperatureSimulation.xml
	for (key in rooms){
		if (key < 0) {
			delete rooms[key];
		} else {
			getData(key);
		}
	}
	
	svg = createSVG();
	initializeMap(imageHeight, imageWidth, 0, 0, interval, minTemp, maxTemp);
	
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
	
// Send a GET request which the server will respond to once sensor data is
// available.  This strategy is called long-polling
function getData(roomNumber) {
	$.ajax({
			url: 'scaife/' + roomNumber,
			type: 'GET',
			success: function(result) {
					// Store room temperature to 1 decimal place
					// 0 indicates a timeout.  In this case, store "Off" in
				 	// temperature array
					if (result <= 0) {
					   rooms[roomNumber].temperature = "Off";
					} else {
					   rooms[roomNumber].temperature = Math.round(result*10)/10;
					}	
					getData(roomNumber);
					
					// This will update all of the labels
					// TODO:  Any way to update labels individually?
					// Would probably have to change the way the group
					// is structured
					updateLabels();
				},
			error: function(e) {
					// Do not produce any error message, since the last
					// GET request will always fail (after server shuts down)
					// Uncomment for testing
					// alert("Error retrieving sensor data for room " +roomNumber);
				}
			});
}

// Calls updateRoomInfoGroup in tempmap.js.  updateLabels is defined here so 
// that svg variable can be accessed 
function updateLabels() {
	updateRoomInfoGroup(svg);
}