// This script defines an interface for a ControllableSensor.

exports.setup = function() {
	this.input('trigger');
	this.output('valueToSend', {
		'type': 'string',
		'value': ''
	});
	this.output('webComponent', {
		'type': 'JSON'
	});
};
