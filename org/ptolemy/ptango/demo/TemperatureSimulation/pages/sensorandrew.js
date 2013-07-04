var BOSH_SERVICE = 'http://sensor.andrew.cmu.edu:5280/http-bind';

var JID = 'benzhang@sensor.andrew.cmu.edu';
var PASS = 'benzhang2012';

// The rooms object is defined in tempmap.js
// This object is a set of key, value pairs where the key is the sensor id
// var rooms = {};

var connection = null;
var logMsg = "";

// FIXME:  Need these?  No log field in HTML page.
function log(msg) 
{
   // $('#log').append('<div></div>').append(document.createTextNode(msg));
	logMsg += msg + "/n";
}

function rawInput(data)
{
    log('RECV: ' + data);
}

function rawOutput(data)
{
    log('SENT: ' + data);
}

function onConnect(status)
{
    if (status == Strophe.Status.CONNECTING) {
	log('Strophe is connecting.');
    } else if (status == Strophe.Status.CONNFAIL) {
	log('Strophe failed to connect.');
	//$('#connect').get(0).value = 'connect';
    } else if (status == Strophe.Status.DISCONNECTING) {
	log('Strophe is disconnecting.');
    } else if (status == Strophe.Status.DISCONNECTED) {
	log('Strophe is disconnected.');
	//$('#connect').get(0).value = 'connect';
    } else if (status == Strophe.Status.CONNECTED) {
	log('Strophe is connected.');
	
	// TODO:  In future, filter by node id?
	// addHandler: function (handler, ns, name, type, id, from)
	connection.addHandler(onMessage, null, 'message', null, null,  null); 
	connection.send($pres().tree());
    }
}

// Callback method when any node visible to this user account publishes data
// TODO:  Create account for demo?  Subscribe this user to only nodes we 
// want in demo?
function onMessage(msg) {

	alert("message");
	var i, j;
	
    // Sample data: (but this was for one specific node - looks like code is getting all nodes?  How to filter on the data event node id?)
    // <data><transducerValue id='voltage' rawValue='164' typedValue='16.400000' timestamp='2013-03-04_11:52:29'/></data>
	
	// TODO:  Make this into an event generator?  Update temperature text
	// as values come in.  Update visualization immediately too?  
	// Need timestamping then. 
	
	// 18554da7a16b3625f7a04d017a3abd45_data
	// e347bb86d9b492ffc9091bd3af2b0ae3_data
	// How to subscribe / filter to an individual node?  Scalability issue for future.
	var transducerVals = msg.getElementsByTagName('transducerValue');
	var id;
	
	for(i=0;i<transducerVals.length; i++){
		id = transducerVals[i].getAttribute('id');
		// alert(id);
		if (rooms.hasOwnProperty(id)) {
			// alert("found" + id);
			rooms[id].temperature =  
				transducerVals[i].getAttribute('typedValue');
			//  'time':(new Date()).getTime()
		}
	}		
	
	// Update temperature text
	// roomInfoGroup defined in tempmap.js
	roomInfoGroup.selectAll(".roomTempRect")
		.data(d3.entries(rooms))
		.transition()
		.duration(interval)
		.attr("fill", function(d) {return rgb(d.value.temperature)});

	roomInfoGroup.selectAll(".roomTempLabel")
		.data(d3.entries(rooms))
		.text(function (d) {return d.value.temperature});

    // we must return true to keep the handler alive.  
    // returning false would remove it after it finishes.
    return true;
}

$(document).ready(function () {
    connection = new Strophe.Connection(BOSH_SERVICE);
    connection.rawInput = rawInput;
    connection.rawOutput = rawOutput;

    // alert("connecting");
	connection.connect(JID, PASS, onConnect);
	// alert("connected");
});
