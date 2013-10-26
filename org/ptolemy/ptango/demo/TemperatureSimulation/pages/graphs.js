// Multi-line temperature graph based on http://bl.ocks.org/mbostock/3884955

// Room temperature data
// Imported as an array of objects with fields CoolSetpoint and temperatures 
// for each of the rooms and the hallway

// Declare the variables with global scope so they appear in Firefox's debugger
var data,
    formattedData,
    names;

// Graph scales and axes.  Extend range of y axis a bit so lines don't hit max
// and min values (looks better if that doesn't happen)
var xScale, 
	yScale,
	xAxis,
	yAxis,
	minMaxPadding = 0.5;

// Colors for the data series.  Specified by the name used in the export. 
var colorData = { "CoolSetpoint" : {color: "lightblue"},
                  "THallway" : {color: "darkgray"},
                  "TRoom202" : {color: "coral"},
                  "TRoom203" : {color: "darkorange"},
                  "TRoom205" : {color: "gold"},
                  "TRoom206" : {color: "yellowgreen"},
                  "TRoom208" : {color: "limegreen"},
                  "TRoom212" : {color: "mediumaquamarine"},
                  "TRoom214" : {color: "mediumturquoise"},
                  "TRoom219" : {color: "steelblue"},
                  "TRoom220" : {color: "slateblue"},
                  "TRoom222" : {color: "chocolate"},
                  "TRoom224" : {color: "sienna"},
};

// Colors and text for the legend. 
// Uses CSS named colors:  http://www.w3.org/TR/SVG/types.html#ColorKeywords
// Could alternatively use RGB for custom colors
var legendData = [ {label: "Cooling Setpoint", 
			        color: "lightblue"},
				   {label: "Hallway Temperature", 
					color: "darkgray"},
				   {label: "Outside Temperature", 
					color: "crimson"},
				   {label: "Room 202 Temperature", 
					color: "coral"},
				   {label: "Room 203 Temperature", 
					color: "darkorange"},
				   {label: "Room 205 Temperature", 
					color: "gold"},
				   {label: "Room 206 Temperature", 
				    color: "yellowgreen"},
				   {label: "Room 208 Temperature", 
					color: "limegreen"},
				   {label: "Room 212 Temperature", 
					color: "mediumaquamarine"},
				   {label: "Room 214 Temperature", 
					color: "mediumturquoise"},
				   {label: "Room 219 Temperature", 
					color: "steelblue"},
				   {label: "Room 220 Temperature", 
					color: "slateblue"},
				   {label: "Room 222 Temperature", 
					color: "chocolate"},
				   {label: "Room 224 Temperature", 
					color: "sienna"}	
					];

// Average hourly outside temperature for July, starting at midnight 00:00
// Copied from USA_PA_Pittsburgh.Intl.AP.725700_TMY3.stat
// In Celsius
var outsideTemp = [{name: "outsideTemp", 
					values: [19.8, 19.7, 19.2, 18.9, 18.4, 18.6, 19.8, 21.4, 
					 22.9, 24.0, 25.0, 26.1, 26.6, 26.9, 27.0, 27.3, 26.9, 26.3, 
                  25.7, 24.6, 23.4, 22.2, 21.3, 20.5]
				    }];

// The SVG element in the HTML page
var svg;

// Chart dimensions.  Assumes a fixed-size object
var chartHeight = 500,
	chartWidth = 700;

var floorplanWidth = 500;

// Whitespace padding around graph
var padding = 30; 

// True if it is the first time page is viewed. Used for create graph vs. update
var firstView = true;

// Functions for computing the lines on the graph and the lines themselves
var lines,
    lineFunction,
    outsideTempLine,
    outsideTempLineFunction,
    showFloorplan = false,
    verticalLine,
    verticalLineFunction,
    verticalLineGroup;

// Endpoints for the vertical line selector

var verticalLineData = [{"x": 3*padding + 1, "y" : padding}, 
          				{"x": 3*padding + 1, "y" : chartHeight - 3*padding}];

var verticalLineGroupData = [{"x" : 0, "y" : 0}];

var verticalLineIndex = 0;

// Draggability for vertical line
// Based on https://github.com/mbostock/d3/wiki/Drag-Behavior
// and http://bl.ocks.org/mbostock/1557377
var dragLine = d3.behavior.drag()
			 .origin(Object)	// Preserves the offset between the mouse
			 					// position and the object's position
			 					// Useful for larger objects
			 .on("drag", function(d) {
					d.x += d3.event.dx;	 // Only update the x direction 
										 // (drag left and right only)
					if (d.x < 0) {d.x = 0};
					if (d.x > chartWidth - 7*padding) 
						{d.x = chartWidth - 7*padding};
					verticalLineGroupData[0].x = d.x;
					
					// Remember index in case user changes setpoint
					verticalLineIndex = Math.floor(d.x * 1.465);
					
					// Update temperature map
					// d.x from 0 to 490
					// data from 0 to 718
					setTemperatures(verticalLineIndex);
					
					d3.select(this).attr("transform", function(d,i) {
						return "translate(" + [d.x, d.y] + ")"
				    
					});
			 });

// Remove once room shapes are working
var startX = chartWidth + 6*padding, startY = 0;

// True if the page is served from Ptolemy; false if loaded offline
var isOnline;

// Function executed once page is loaded
$(document).ready(function() {
	// Check if page is being served online, or loaded offline
	if (typeof(online) == 'undefined') {
		isOnline = false;
	} else { 
		isOnline = true;
	}
	
	// Register click handlers
	if (isOnline) {
		$("#calculate").click(readDataOnline);
		
		// Hide loading message
		$("#loading").hide(); 
	}
	else {
		$("#radio21Label").click(readDataOffline);
		$("#radio23Label").click(readDataOffline);
		$("#radio25Label").click(readDataOffline);
	}
	$("#checkboxLabel").click(showHideFloorplan);
	
	// Formatting for buttons.  From jQueryUI
	// Ensure offline page starts with middle button (23 degrees C) checked
	// Trigger a click event to check this button and kick off data import
	if (isOnline) {
		$("#calculate").button();
		$("#calculate").trigger("click");	// Simulate a click to call readData()
	}
	else {
		$("#radio-group").buttonset();
		$("#radio23Label").trigger("click"); // Simulate a click to call readData()
	}
	
	$("#checkbox").prop("checked", false);
	$("#checkbox").button();
	
	// Create the progress bar (in hidden div loading) 
	// http://jqueryui.com/progressbar/#indeterminate
	 $( "#progressBar" ).progressbar({ value: false });
	 $("#progressBar").progressbar("option", "value", false);
});

// Create an SVG element showing the temperature graph
function createGraph() {

	// Original array is array of objects: 
	// [ { CoolSetpoint="16.0", THallway="22.6976701179367", TOutside="22.3546702723403", more...}, ...]
	// Map to an array of format:  [ {name: "CoolSetpoint", values: [16.0, 18.0,...] ...} ]
	// Assumes data is in same order as legend
	names = d3.keys(data[0]);
	formattedData = names.map(function(name, i) {
		return {
			color: colorData[name].color,
			name: name,
			values: data.map(function(d) {
				return +d[name];
			})
		};
	});
	
	// Data is for July 15th from 12 midnight to 11:59 pm, every two minutes
	// This is specified in the EPlusScaife.idf file
	var startDate = new Date("July 1, 2013 00:00:00");
	var endDate = new Date("July 1, 2013 23:58:00");
	
	// X axis is a time-scale axis showing hours of one day
	xScale = d3.time.scale()
		.domain([startDate, endDate])
		.range([3*padding, chartWidth - 4*padding]);
	
	// Y axis is a linear axis with range auto-scaled to max and min of outside
	// temperature, plus a little extra
	yScale = d3.scale.linear()
		.domain([
			 d3.max(outsideTemp, function(d) {return d3.max(d.values); })
			 	+ minMaxPadding,
		     d3.min(outsideTemp, function(d) {return d3.min(d.values); })
		     	- minMaxPadding]) 
		.range([1 + padding, chartHeight - 3*padding]);
	
	// Uncomment to scale to max and min of room data (vs outside temp)
	/*
	yScale = d3.scale.linear()
		.domain([
			 d3.max(formattedData, function(d) {return d3.max(d.values); })
			 	+ minMaxPadding,
		     d3.min(formattedData, function(d) {return d3.min(d.values); })
		     	- minMaxPadding]) 
		.range([1 + padding, chartHeight - 3*padding]);
		*/
	
	// Create the SVG element to hold the graph
	svg = d3.select('#temperature-graph')
				.append('svg')
				.attr('width', chartWidth + 5*padding + floorplanWidth)
				.attr('height', chartHeight);
	
    // Create floorplan map, but hide it to start
	// Uses functions from tempmap.js
	// Do this first since room overlay doesn't seem to work if done
	// at the end?
	initializeMap(498, 282, chartWidth + 6*padding, 0, 500, 21.0, 25.0);
	addImage(svg);
	drawRooms(svg);
	drawGradient(svg);
	hideMap();
	
	// Draw a line for setpoint, outside temperature, each room and hallway
	// Samples are every 2 minutes
	
	lineFunction = d3.svg.line()
		.x(function(d, i) {return xScale(new Date(startDate.getTime() + 2*i*60000)); })
		.y(function(d) {return yScale(d); })
	
	lines = svg.selectAll(".datafeed")
		.data(formattedData)
		.enter().append("g")
			.attr("class", "datafeed");
	
	// Treat the CoolingSetpoint line specially
	lines.append("path")
		 .attr("class", "datafeedLine")
		 .attr("d", function(d) {return lineFunction(d.values); })
		 .attr("stroke-width", function(d,i){ 
			 if (i == 0) {return 12;} else {return 2;}
		 })
		 .attr("stroke", function(d) {return d.color;})
		 .attr("fill", "none")
		 .attr("stroke-opacity", function(d,i){
			 if (i == 0){return 0.6;} else {return 1;}
		 });
	
	// Draw a line for the outside temperature
	// Samples are every hour
	
	outsideTempLineFunction = d3.svg.line()
	.x(function(d, i) {return xScale(new Date(startDate.getTime() + 60*i*60000)); })
	.y(function(d) {return yScale(d); })
	
	outsideTempLine = svg.selectAll(".outsideTemp")
		.data(outsideTemp)
		.enter().append("g")
			.attr("class", "outsideTemp");
	
	outsideTempLine.append("path")
		 .attr("class", "outsideLine")
		 .attr("d", function(d) {return outsideTempLineFunction(d.values); })
		 .attr("stroke-width", 2)
		 .attr("stroke", "crimson")
		 .attr("fill", "none");
	
	// Draw axes
	// http://alignedleft.com/tutorials/d3/axes/

	xAxis = d3.svg.axis()
		.scale(xScale)
		.orient("bottom");
	
	svg.append("g")
		.attr("class", "axis")
		.attr("transform", "translate(0, " + (chartHeight - 3*padding) + ")")
		.call(xAxis);
	
	// Add axis label.  Use unicode representation for degree symbol in text
	// http://alignedleft.com/tutorials/d3/axes/
	// http://www.d3noob.org/2012/12/adding-axis-labels-to-d3js-graph.html
	
	svg.append("text")
		.attr("class", "axisLabel")
		.attr("x", chartWidth / 2)
		.attr("y", chartHeight - padding)
		.style("text-anchor", "middle")
		.text("Time");
	
	yAxis = d3.svg.axis()
		.scale(yScale)
		.orient("left")
		.ticks(8);
	
	svg.append("g")
		.attr("class", "axis")
		.attr("transform", "translate(" + (3*padding) + ", 0)")
		.call(yAxis);

	
	svg.append("text")
		.attr("class", "axisLabel")
		.attr("x", -(chartHeight / 2) + padding)
		.attr("y", padding)
		.attr("transform", "rotate(-90)")
		.style("text-anchor", "middle")
		.text("Temperature (\u00B0C)");
	
	// Draw legend
	// http://jsbin.com/ubafur/3
	   
	var squareSize = 16;
	
	var legend = svg.append("g")
	  .attr("class", "legend")
	  .attr("transform", "translate(" + (chartWidth - 2*padding) + ", " + padding + ")")
	  .attr("height", 100)
	  .attr("width", 100);  
   
    legend.selectAll('rect')
      .data(legendData)
      .enter()
      .append("rect")
	  .attr("x", 0)
      .attr("y", function(d, i){ return i *  20;})
	  .attr("width", squareSize)
	  .attr("height", squareSize)
	  .style("fill", function(d) {return d.color;} );
      
    legend.selectAll('text')
      .data(legendData)
      .enter()
      .append("text")
      .attr("class", "legendLabel")
	  .attr("x", padding)
      .attr("y", function(d, i){ return i *  20 + squareSize - 2;})
      .text(function(d) {return d.label;});
		
    // Add vertical line for synching with floorplan temperature map
    verticalLineFunction = d3.svg.line()
		.x(function(d) {return d.x; })
		.y(function(d) {return d.y; })
		.interpolate("linear");
    
    // Create the vertical line and hide it
	// Create the vertical line
	verticalLineGroup = svg.selectAll(".verticalLineGroup")
		.data(verticalLineGroupData)
		.enter()
		.append("svg:g")
		.attr("class", "verticalLineGroup")
		.attr("opacity", 0.6)
		.call(dragLine);
	
	verticalLine = verticalLineGroup.selectAll(".verticalLine")
		.data(verticalLineData)
		.enter()
		.append("path")
		 .attr("class", "verticalLine")
		 .attr("d", verticalLineFunction(verticalLineData))
		 .attr("stroke-width", 16)
		 .attr("stroke", "yellow");
	
	verticalLineGroup.style("display", "none");
	
	// Set room temperatues on the overlay to array index 1 of data
	// First data point (index 0) is abnormally high, so don't use it
	setTemperatures(0);
}

// Request data from the Ptolemy server
function readDataOnline(){
	// If first view, create temperature graph
	// Import data that was exported from Ptolemy TemperatureSimulation. 
	// Use d3's XMLHttpRequest function:
	// https://github.com/mbostock/d3/wiki/Requests
	if (firstView) {
		// GET default temperature file from web server
		d3.csv("temperatures23setpoint.csv", function(error, csv) {
			if (error) {
				alert("Unable to import temperature data");
			} else {
				data = csv;
				firstView = false;
				createGraph();
			}
		});
	} else { 
		
		// For subsequent views, post the cooling setpoint to the server
		// The server will calculate the results and save the data in 
		// temperatures.csv (on the server)
		// Wait for a reply to the POST, indicating data is ready, then
		// GET the data file
		
		var setpoint = $("#setpoint").val();
		
		// Range checking - Range is current max/min shown on graph.  
		// In future, could check where simulation breaks down
		var minLimit = 18;
		var maxLimit = 27;
		
		if (setpoint < minLimit || setpoint > maxLimit) {
			alert("Please choose a setpoint between " + minLimit + " and " + maxLimit);
		} else {
			// Create wait dialog message including setpoint
			$("#loadingMessage").html("Calculating for setpoint of " + setpoint + "...");
		
			$.ajax({
				url: 'eplus',
				type: 'POST',
				dataType: 'text',
				data: {setpoint: setpoint},
				success: function(result) {
					d3.csv("temperatures.csv", function(error, csv) {
						if (error) {
							alert("Error retrieving results from server");
						} else {
							data = csv;
							updateGraph();
						}
					});
				},
				error: function(e) {
					alert("Error running server-side simulation");
				}
			});
		}
	}
}

// Read the appropriate data file according to the chosen cooling setpoint
function readDataOffline(){
	var filename = "temperatures21setpoint.csv";
	
	// If first view, create temperature graph
	// Import data that was exported from Ptolemy TemperatureSimulation. 
	// Use d3's XMLHttpRequest function:
	// https://github.com/mbostock/d3/wiki/Requests
	if (firstView) {
		d3.csv("temperatures23setpoint.csv", function(error, csv) {
			if (error) {
				alert("Unable to import temperature data");
			} else {
				data = csv;
				firstView = false;
				createGraph();
			}
		});
	} else { 
		
		// For subsequent views, update existing graph, so we can animate 
		// the transition
		
		if(this.id == ("radio21Label")) {
			filename = "temperatures21setpoint.csv";
		} else if (this.id == "radio23Label") {
			filename = "temperatures23setpoint.csv";
		} else if (this.id == "radio25Label") {
			filename = "temperatures25setpoint.csv";
		} else { alert("Please select a cooling setpoint.")}
		
		d3.csv(filename, function(error, csv) {
			if (error) {
				alert("Unable to import temperature data");
			} else {
				data = csv;
				updateGraph();
			}
		});
	}
		
}

/* Set temperatures of rooms data structure in tempmap.js to index i of data */
function setTemperatures(i) {
	setRoomTemperature(202, data[i].TRoom202); 
	setRoomTemperature(203, data[i].TRoom203); 
	setRoomTemperature(205, data[i].TRoom205); 
	setRoomTemperature(206, data[i].TRoom206); 
	setRoomTemperature(208, data[i].TRoom208); 
	setRoomTemperature(212, data[i].TRoom212); 
	setRoomTemperature(214, data[i].TRoom214);
	setRoomTemperature(219, data[i].TRoom219);
	setRoomTemperature(220, data[i].TRoom220); 
	setRoomTemperature(222, data[i].TRoom222); 
	setRoomTemperature(224, data[i].TRoom224); 
	
	updateMap(svg);
}

// Show or hide the vertical line to link line chart with floorpan map 
function showHideFloorplan() {
	if (showFloorplan) {
		verticalLineGroup.style("display", "none");
		hideMap();
		
		showFloorplan = false;
		
	} else {
		verticalLineGroup.style("display", "inline");
		showMap();
		
		showFloorplan = true;
	}
}


// Update the graph to show the new data
function updateGraph(){
	
	// Update data array
	names = d3.keys(data[0]);
	formattedData = names.map(function(name, i) {
		return {
			color: colorData[name].color,
			name: name,
			values: data.map(function(d) {
				return +d[name];
			})
		};
	});
	
	// Update room temperature lines.  Legend, axes, and outside temperature
	// are the same.
	d3.selectAll(".datafeedLine")
		.data(formattedData)
		.transition()
		.duration(1000)
		.attr("d", function(d) {return lineFunction(d.values); });
	
	// Update room map
	setTemperatures(verticalLineIndex);	
}

// Grey out page while loading.  See:
// http://stackoverflow.com/questions/1964839/jquery-please-wait-loading-animation

$(document).ajaxStart(function() { 
	 $("#loading").show();
});

$(document).ajaxStop(function() { 
     $("#loading").hide(); 
});

