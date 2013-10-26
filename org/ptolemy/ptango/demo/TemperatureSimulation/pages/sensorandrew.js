var BOSH_SERVICE = 'http://sensor.andrew.cmu.edu:5280/http-bind';

// NOTE:  A Sensor Andrew ID and password are needed to connect
// Please enter this information here
var JID = 'id';
var PASS = 'password';

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
// Nodes of interest:
// Room 202 only has lightswitch

// ID   Nickname 			Device name				Event node								Serial number
// 320 	208 Projector 		FireFly3 Environmental 	679a64b8d78ab1d599fd55b73b8e5cbe_data 	0xA
// 1581 	Thermometer Digital 	BMP085 	0000-00-00 00:00:00
// Use ID 1581

// 322 	212 Environmental 	FireFly3 Environmental 	f51a3e3ae15bd09ab36345ce832b398f_data 	0x7
// 1607 	Thermometer Digital 	BMP085 	0000-00-00 00:00:00
// Use ID 1607

// 323 	214 Projector 		FireFly3 Environmental 	b1f7eee1ccecf0af4f7434cac974f072_data 	0x8
// 1620 	Thermometer Digital 	BMP085
// Use ID 1620

// Room 219
// 334 		FireFly3 Environmental 	7495920b39d01f774e0782cf65031532_data 	0x18
// 1719 	Thermometer Digital 	BMP085 	0000-00-00 00:00:00
// Use ID 1719

// Room 220
// 335 		FireFly3 Environmental 	b0b0b76749275629cbe9d9c7bf8847f9_data 	0x1b
// 1732 	Thermometer Digital 	BMP085 	0000-00-00 00:00:00
// Use ID 1732

// Room 222
// 336 		FireFly3 Environmental 	a5ed2cdcc39a52b0aa94e724a0adbc79_data 	0x1a
// 1745 	Thermometer Digital 	BMP085 	0000-00-00 00:00:00
// Use ID 1745

function onMessage(msg) {

	// alert("message");
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
		if (rooms.hasOwnProperty(id)) {
			// Show number to one decimal place
			var temperature = 
				Number(transducerVals[i].getAttribute('typedValue'))
				.toFixed(1);
			rooms[id].temperature =  temperature;
		}
	}		
	
	// Update labels immediately
	updateLabels();

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
