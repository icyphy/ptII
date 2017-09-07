// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals console, error, exports, require */
/*jshint globalstrict: true*/
"use strict";

exports.setup = function() {	
	this.output('data', {'type' : 'JSON'});
	
    var GDPLogSubscribe = this.instantiate('GDPLogSubscribe', 'gdp/GDPLogSubscribe'); 
    GDPLogSubscribe.input('logname', {
		'value':'edu.berkeley.eecs.testlog'
	});
	
	var code = "\
	exports.setup = function() {\n\
	    this.input('in');\n\
	    this.output('out');\n\
    }\n\
    exports.initialize = function() {\n\
	    var thiz = this;\n\
	    this.addInputHandler('in', function() {\n\
		    thiz.send('out', JSON.parse(thiz.get('in')));\n\
	    });\n\
    }";
    var JavaScript = this.instantiateFromCode('JavaScript', code, false);

    this.connect(JavaScript, 'out', 'data');
    this.connect(GDPLogSubscribe, 'data', JavaScript, 'in');
};
