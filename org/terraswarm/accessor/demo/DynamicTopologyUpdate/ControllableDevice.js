exports.setup = function() {
	this.input('control', {
		'type': 'JSON',
		'value': {}
	});
	this.output('data', {
		'type': 'JSON',
		'value': {}
	});
	this.output('whatIsGoingOn', {
		'type': 'string',
		'value': ''
	});
};

exports.initialize = function() {
	var thiz = this;

	setInterval(function() {
		var whatIsGoingOn = 'I am the reified accessor ' + thiz.accessorName + ' and I send with a period of 300'
		thiz.send('whatIsGoingOn', whatIsGoingOn);
		console.log(whatIsGoingOn);
	}, 300);
};

