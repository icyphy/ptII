exports.setup = function() {
	this.input('control2', {
		'type': 'JSON'
	});
	this.output('data2', {
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
		thiz.send('whatIsGoingOn', 'I am the reified accessor ' + thiz.accessorName + ' and I send with a period of 250');
	}, 250);

	this.addInputHandler('control2', function() {
	});
};

