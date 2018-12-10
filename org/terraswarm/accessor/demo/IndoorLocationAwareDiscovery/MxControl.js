// Put your JavaScript program here.
// Refer to parameters in scope using dollar-sign{parameterName}.
// See: https://wiki.eecs.berkeley.edu/accessors/Version1/AccessorSpecification

// This accessor serves as a control test. If the destinationName parameter is
// equal to controlIn id attribute, then controlIn input will be redirected
// to controlOut. Otherwise, it is stopped.

exports.setup = function () {
    this.input('controlIn', {
        'type': 'JSON',
        'value':{}
    });
    this.output('controlOut', {
        'type': 'JSON',
        'value':{}
    });
    this.parameter('destinationName', {
        'type': 'string',
        'value': ''
    });
};

exports.initialize = function() {
    var self = this;
    
    this.addInputHandler('controlIn', function() {
        var controlIn = thiz.get('controlIn');
        var destinationName = thiz.getParameter('destinationName');

        // Redirect controlIn to controlOut if it is the destination
        if (controlIn[id]==destinationName) {
            thiz.send('controlOut', controlIn);
        }

    });
};
    
  