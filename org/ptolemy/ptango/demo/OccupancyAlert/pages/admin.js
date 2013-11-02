/* Javascript for Swarm Lab Simulator admin panel */

var elevator = "false",
	power = "true";

// Display default flip switch states; register click handlers
$(document).ready(function() {
	
	$("#submit").click(submit);
	$("#cancel").click(cancel);
	
	// Get values from Ptolemy, store, and refresh display, in case of a page 
	// refresh while Ptolemy is still running
	getSettings();
});

// If user cancels, reset values and refresh display
function cancel() {
	$("#elevator").val(elevator);
	$("#power").val(power);
	
	refresh();
}

// Get settings from Ptolemy
function getSettings(){
	$.ajax({
		url: 'data',
		type: 'GET',
		dataType: 'json',
		success: function(result) {
			// Store state.  For some reason, string is needed 
			// FIXME:  Figure out when when have time
			if (result.elevator) {
				elevator = "true";
			} else {
				elevator = "false";
			}
			
			if (result.power) {
				power = "true";
			} else {
				power = "false";
			}
			
			// Update display.  This should be done in the callback to ensure
			// that the data is received before the display is refreshed
			// (since ajax calls are asynchronous - execution will proceed
			// in parallel while waiting for result)
			$("#elevator").val(elevator);
			$("#power").val(power);
			
			refresh();
		},
		error: function(e) {
			alert("Error retrieving data from simulator: " + JSON.stringify(e));
		}
	});
}

//Post the elevator and power status to the Ptolemy model
function postSettings(){
	$.ajax({
		url: 'data',
		type: 'POST',
		//dataType: 'json',
		data: {elevator: elevator,
			   power: power},
		success: function(result) {
			alert("Data succesfully posted");
		},
		error: function(e) {
			alert("Error sending data to simulator: " + JSON.stringify(e));
		}
	});
}

// Refresh display
function refresh() {
	// For jQueryMobile, need to refresh display after changing values
	$("#elevator").slider("refresh");
	$("#power").slider("refresh");
}

// POST settings to Ptolemy
function submit() {
	// Store new values
	elevator = $("#elevator").val();
	power = $("#power").val();
	
	// POST to Ptolemy
	postSettings();
}
