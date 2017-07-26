// This script defines an interface for a ControllableSensor.
exports.setup = function() {
	// Indicate the ontology of accessors that implement this interface.
	// The argument is an arbitrary string.
	// FIXME: Why not just rely on the name of this interface?
	this.realize('controllableSensor');
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
