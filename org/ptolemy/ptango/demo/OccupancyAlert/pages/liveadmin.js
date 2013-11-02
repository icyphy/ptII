/* Javascript for Swarm Lab Simulator admin panel */

var elevator = "false",
	power306 = "true",
	power307 = "true",
	power308 = "true";

var elevatorLive = "false",
	power306Live = "false",
	power307Live = "false",
	power308Live = "false";

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
	$("#power306").val(power306);
	$("#power307").val(power307);
	$("#power308").val(power308);
	
	$("#elevatorLive").val(elevatorLive);
	$("#power306Live").val(power306Live);
	$("#power307Live").val(power307Live);
	$("#power308Live").val(power308Live);
	
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
			
			if (result.power306) {
				power306 = "true";
			} else {
				power306 = "false";
			}
			
			if (result.power307) {
				power307 = "true";
			} else {
				power307 = "false";
			}
			
			if (result.power308) {
				power308 = "true";
			} else {
				power308 = "false";
			}
			
			if (result.elevatorLive) {
				elevatorLive = "true";
			} else {
				elevatorLive = "false";
			}
			
			if (result.power306Live) {
				power306Live = "true";
			} else {
				power306Live = "false";
			}
			
			if (result.power307Live) {
				power307Live = "true";
			} else {
				power307Live = "false";
			}
			
			if (result.power308Live) {
				power308Live = "true";
			} else {
				power308Live = "false";
			}
			
			$("#elevator").val(elevator);
			$("#power306").val(power306);
			$("#power307").val(power307);
			$("#power308").val(power308);
			
			$("#elevatorLive").val(elevatorLive);
			$("#power306Live").val(power306Live);
			$("#power307Live").val(power307Live);
			$("#power308Live").val(power308Live);
			
			// Update display.  This should be done in the callback to ensure
			// that the data is received before the display is refreshed
			// (since ajax calls are asynchronous - execution will proceed
			// in parallel while waiting for result)
			refresh();
			setSliders();
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
			   power306: power306,
			   power307: power307,
			   power308: power308,
			   elevatorLive: elevatorLive,
			   power306Live: power306Live,
			   power307Live: power307Live,
			   power308Live: power308Live},
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
	$("#power306").slider("refresh");
	$("#power307").slider("refresh");
	$("#power308").slider("refresh");
	
	$("#elevatorLive").slider("refresh");
	$("#power306Live").slider("refresh");
	$("#power307Live").slider("refresh");
	$("#power308Live").slider("refresh");
}

// POST settings to Ptolemy
function submit() {
	// Store new values
	elevator = $("#elevator").val();
	power306 = $("#power306").val();
	power307 = $("#power307").val();
	power308 = $("#power308").val();
	
	elevatorLive = $("#elevatorLive").val();
	power306Live = $("#power306Live").val();
	power307Live = $("#power307Live").val();
	power308Live = $("#power308Live").val();
	
	setSliders();
	
	// POST to Ptolemy
	postSettings();
}

// Enable / Disable sliders 
// FIXME:  Would be better to do onchange for sliders, but this is not
// working
function setSliders() {

	if (elevatorLive == "false") {
		$('#elevator').slider('enable');
	} else {
		$('#elevator').slider('disable');
	}
	
	if (power306Live == "false") {
		$('#power306').slider('enable');
	} else {
		$('#power306').slider('disable');
	}
	
	if (power307Live == "false") {
		$('#power307').slider('enable');
	} else {
		$('#power307').slider('disable');
	}
	
	if (power308Live == "false") {
		$('#power308').slider('enable');
	} else {
		$('#power308').slider('disable');
	}
}
