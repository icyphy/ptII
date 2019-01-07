exports.setup = function() {
	this.input('control2', {
		'type': 'JSON',
		'value': {}
	});
	this.output('data2', {
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
		var whatIsGoingOn = 'I am the reified accessor ' + thiz.accessorName + ' and I send with a period of 250';
		thiz.send('whatIsGoingOn', whatIsGoingOn);
		console.log(whatIsGoingOn);
	}, 250);
};

