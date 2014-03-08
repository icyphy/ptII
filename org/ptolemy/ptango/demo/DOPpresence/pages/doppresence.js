// Javascript for DOP Center door entry page

// An idle timer for resetting the page once a user has entered
var timer;

// Function called on page load
$(document).ready(function() {
	
	// Register onclick handler for submit button
	// Submit the form data and communicate authorization status
	$("#submit").click(function(e) {
		e.preventDefault();  // Prevent default post action
		
		// Clear the idle timer since a new request has arrived
		clearInterval(timer);
		
	    var postData = $("#form").serializeArray();
	    var formURL = $("#form").attr("action");
	    $.ajax(
	    {
	        url : formURL,
	        type: "POST",
	        data : postData,
	        dataType : "json",	// Type of data expected from the server
	        success:function(data, textStatus, jqXHR)
	        {
	        	$("#message").html(data.message);
	        	
	        	// If unsuccessful, clear the door code field
	        	if (!data.authorized){
	        		$("#doorcode").val("");
	        	}
	        	
	           	// After ten seconds, clear username and door code and reset the 
	        	// welcome message. This timeout is canceled if a new request
	        	// is submitted
	        	timer = setTimeout("resetFields()", "10000");
	        	
	        },
	        error: function(jqXHR, textStatus, errorThrown)
	        {
	            alert("Error communicating with server.");
	        }
	    });
	    
	    return false; 
	});
});

// Clear the username and door code and reset the welcome message
function resetFields() {
	$("#username").val("");
	$("#doorcode").val("");
	$("#message").html("Please enter your username and door code.");
}
