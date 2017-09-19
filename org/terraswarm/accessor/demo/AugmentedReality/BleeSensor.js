// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals console, error, exports, require */
/*jshint globalstrict: true*/
"use strict";

exports.setup = function() {	
    this.output('data', {'type' : 'JSON'});
    
    var MQTTSubscriber = this.instantiate('MQTTSubscriber', 'net/MQTTSubscriber'); 
    MQTTSubscriber.input('subscribe', {
	// 'value':'#'
        'value': 'device/BLEES/c098e530005b'
    });
    
    // Hint: Use the lab11 Summon app to find local SwarmBoxes with BLE
    MQTTSubscriber.setParameter('brokerHost', '192.168.2.15');

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
    this.connect(MQTTSubscriber, 'received', JavaScript, 'in');
};
