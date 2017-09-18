// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals console, error, exports, require */
/*jshint globalstrict: true*/
"use strict";

exports.setup = function() {	
    this.output('data', {'type' : 'JSON'});
    
    var MqttSubscriber = this.instantiate('MqttSubscriber', 'net/MqttSubscriber'); 
    MqttSubscriber.input('subscribe', {
	'value':'#'
    });
    
    // Hint: Use the lab11 Summon app to find local SwarmBoxes with BLE
    MqttSubscriber.setParameter('brokerHost', '192.168.2.15');

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

    //var TestDisplay = this.instantiate('TestDisplay', 'test/TestDisplay.js');

    this.connect(JavaScript, 'out', 'data');
    this.connect(MqttSubscriber, 'received', JavaScript, 'in');
    //this.connect(MqttSubscriber, 'error', TestDisplay, 'input');
};
