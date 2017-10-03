// This script defines an interface for a ControllableSensor.
exports.setup = function() {
	this.input('control', {
		'type': 'JSON',
		'value':{}
	});
	this.output('data', {
		'spontaneous':true,
		'type': 'JSON'
	});
	this.output('schema', {
		'spontaneous':true,
		'type': 'JSON'
	});
};
