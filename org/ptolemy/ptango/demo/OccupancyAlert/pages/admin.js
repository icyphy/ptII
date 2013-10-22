/* Javascript for Cory Hall Simulator admin panel */

var elevator1 = false,
	elevator2 = false,
	elevator3 = false,
	power = true;

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
	$("#elevator1").val(elevator1);
	$("#elevator2").val(elevator2);
	$("#elevator3").val(elevator3);
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
			// Store state
			elevator1 = result.elevator1;
			elevator2 = result.elevator2;
			elevator3 = result.elevator3;
			power = result.power;
			
			// Update display.  This should be done in the callback to ensure
			// that the data is received before the display is refreshed
			// (since ajax calls are asynchronous - execution will proceed
			// in parallel while waiting for result)
			$("#elevator1").val(elevator1);
			$("#elevator2").val(elevator2);
			$("#elevator3").val(elevator3);
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
		data: {elevator1: elevator1,
			   elevator2: elevator2,
			   elevator3: elevator3,
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
	$("#elevator1").slider("refresh");
	$("#elevator2").slider("refresh");
	$("#elevator3").slider("refresh");
	$("#power").slider("refresh");
}

// POST settings to Ptolemy
function submit() {
	// Store new values
	elevator1 = $("#elevator1").val();
	elevator2 = $("#elevator2").val();
	elevator3 = $("#elevator3").val();
	power = $("#power").val();
	
	// POST to Ptolemy
	postSettings();
}
