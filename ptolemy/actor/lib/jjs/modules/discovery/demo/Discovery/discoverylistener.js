/**  A device discovery listener for Node.js.  
 * 
 *   This program should be executed on the terra server.
 * 
 *   This program starts a web server and listens for POSTs containing info 
 *   about devices connected to a particular host.  It prints the info to the 
 *   screen and stores the info in a variable.  It does not currently persist
 *   the data.
 *   
 *   To test locally, see instructions at the bottom of the file.
 *   
 *   Accessors: https://www.terraswarm.org/accessors/
 *   Discovery:  https://www.terraswarm.org/accessors/wiki/Version0/Discovery
 *  
 *   Author: Elizabeth Latronico
 */

var express = require('express');
var path = require('path');
var bodyParser = require('body-parser');

var app = express();
// Use the body-parser package to parse JSON-encoded bodies of incoming HTTP requests
app.use(bodyParser.json());     
 
var port = 8088;

app.listen(port);
console.log('Listening on port ' + port);

var hostMap = {};

// GET /hosts/:hostname/devices  Print a list of devices connected to swarmbox 
// ":hostname"
app.get('/hosts/:hostname/devices', function (req, res) {
	// console.log("Looking up devices for " + req.params.hostname);
	var hostname = req.params.hostname;
	if (hostname in hostMap) {
	    res.send(hostMap[hostname]);
	} else {
		res.send([]);
	}
});

// POST /hosts/(hostname)/devices  Submit a list of devices 
// {IPAddress, MAC address, name}} connected to swarmbox ":hostname"
app.post('/hosts/:hostname/devices', function (req, res) {
	// The Express body-parser will automatically parse the JSON into a Javascript object
	hostMap[req.params.hostname] = req.body;
	res.send('Received device list for ' + req.params.hostname + ' \n');
});

// To test locally, uncomment the section below, and do the following:
// Copy this file (discoverylistener.js) to a location you wish to run node from
// Make a directory named static
// Copy swarmboxes.html to /static
// Copy the content of https://terra.eecs.berkeley.edu:8088/hosts to a file
// named /static/hosts.html
// Make a directory /static/images
// Pick your favorite computer picture and save as /static/images/computers.png
// npm install express
// npm install body-parser
// node discoverylistener.js

/*
// GET /hosts :  List swarmbox names
// For serving hosts file locally
app.get('/hosts', function (req, res) {
 res.sendfile('./static/hosts.html');
});

// Get /map : Return the swarmbox map
// For serving swarmbox map locally
app.get('/map', function (req, res) {
 res.sendfile('./static/swarmboxes.html');
});
*/
