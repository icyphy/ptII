exports.setup = function() {
	this.input('trigger');
	this.output('valueToSend', {
		'type': 'string',
		'value': ''
	});
	this.output('webComponent', {
		'type': 'JSON'
	});
    this.parameter('val', {
        'type': 'number',
        'value': 1
    });
};

exports.initialize = function() {
    var thiz = this;

    this.addInputHandler('trigger', function() {
        thiz.send('valueToSend', 'Accessor '+ thiz.accessorName + ' is sending value ' + thiz.getParameter('val'));
    });
}