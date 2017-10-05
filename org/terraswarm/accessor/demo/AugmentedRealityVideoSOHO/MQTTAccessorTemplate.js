"use strict";

exports.setup = function() {	
    this.output('data', {'type' : 'JSON'});
    
    var MQTTSubscriber = this.instantiate('MQTTSubscriber', 'net/MQTTSubscriber'); 
    MQTTSubscriber.input('subscribe', {
	/* 'value':'#'*/
    /* The GenerateBleeAccessors.xml model substitutes in the topic here.*/
        'value': '@topic@'
    });
    
    /* The GenerateBleeAccessors.xml model substitutes in the BrokerHost here.*/
    var brokerHost = '@brokerHost@';

    /* Hint: Use the lab11 Summon app to find local SwarmBoxes with BLE. */
    MQTTSubscriber.setParameter('brokerHost', brokerHost);

    var code = "\
	exports.setup = function() {\n\
	    this.input('in');\n\
	    this.output('out');\n\
    }\n\
    exports.initialize = function() {\n\
	    var thiz = this;\n\
	    this.addInputHandler('in', function() {\n\
	    	var inValue = thiz.get('in');\n\
	        console.log('MQTTAccessor:' + inValue);\n\
		    thiz.send('out', JSON.parse(inValue));\n\
	    });\n\
    }";
    var JavaScript = this.instantiateFromCode('JavaScript', code, false);

    this.connect(JavaScript, 'out', 'data');
    this.connect(MQTTSubscriber, 'received', JavaScript, 'in');
};
