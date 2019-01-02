exports.setup = function() {
	this.input('control', {
		'type': 'JSON'
	});
	this.output('data', {
		'type': 'JSON'
	});
	this.output('whatIsGoingOn', {
		'type': 'string',
		'value': ''
	});
};

exports.initialize = function() {
	var thiz = this;

	setInterval(function() {
		thiz.send('whatIsGoingOn', 'I am the reified accessor ' + thiz.accessorName + ' and I send with a period of 300');
	}, 300);

	this.addInputHandler('control', function() {
	});
};
