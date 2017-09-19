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
        'value': 'device/PowerBlade/c098e570021e'
        // ["device/Coilcube/c098e5434ff39e2d","device/Coilcube (Splitcore)/c098e5494ddfd6bb","device/PowerBlade/c098e57000b0","device/Blink/c098e5900006","device/Blink/c098e5900097","device/PowerBlade/c098e57000a9","device/FirestormSensing/c57b6831352b","device/Blink/c098e5900008","device/Blink/c098e5900095","device/Blink/c098e590009f","device/BLEES/c098e5300096","device/Blink/c098e590000d","device/PowerBlade/c098e570002e","device/PowerBlade/c098e5700096","device/Blink/c098e5900099","device/BLEES/c098e5300094","device/BLEES/c098e5300091","device/BLEES/c098e5300092","device/Coilcube/c098e54347d38c05","device/Coilcube/c098e5434ffc9eed","device/Coilcube/c098e54b41f39532","device/Coilcube (Splitcore)/c098e5494defc05a","device/Coilcube/c098e5434fe1a889","device/Coilcube/c098e50645575b86","device/Coilcube/c098e5434ff870c1","device/Coilcube/c098e5434ff347c5","device/Impulse/c098e5494ddcb95a","device/Coilcube/c098e5434ff35b27","device/Coilcube/c098e5434ff3bc3b","device/Coilcube/c098e5404ff35720","device/Coilcube/c098e5434cf53609","device/Coilcube/c098e5424ff35201","device/Coilcube/c098e5434ff39720","device/Coilcube/c098e509bff3a88e","device/Coilcube/c098e5434ff98c26","device/Coilcube/c098e5634ff35201","device/Coilcube (Splitcore)/c098e5df7c5f71d9","device/Coilcube (Splitcore)/c098e5494cdff52c","device/Coilcube/c098e5434af399ae","device/Coilcube/c098e5450f535201","device/Coilcube (Splitcore)/c098e54feddfd6bb","device/Coilcube/c098e5d3fff360c0","device/Coilcube/c098e5434ff3ac6b","device/Coilcube/c098e55919f35202","device/Coilcube (Splitcore)/c098e5294633d038","device/Coilcube/c098e5f34ff35720","device/Coilcube (Splitcore)/c098e54949dff52c","device/Coilcube/c098e5414ff35720","device/Coilcube/c098e5434ff38c86","device/Coilcube (Splitcore)/c098e5e74ddff52c","device/Coilcube (Splitcore)/c098e5494ddfbb23","device/Coilcube/c098e5434fe19dc9","device/Coilcube (Splitcore)/c098e5494d6f372c","device/Coilcube (Splitcore)/c098e5494d3fe7d9","device/Coilcube/c098e54341f35720","device/PowerBlade/c098e5700045","device/Coilcube/c098e5435ff35891","device/Coilcube/c098e54322f3ac3b","device/Blink/c098e590008a","device/Blink/c098e59000c4","device/Blink/c098e5900084","device/Blink/c098e590008b","device/Blink/c098e590007f","device/Blink/c098e59000c2","device/Blink/c098e59000c3","device/BLEES/c098e5300011","device/BLEES/c098e53000bb","device/Coilcube/c098e54ef4f399ae","device/Coilcube (Splitcore)/c098e54f4dd5552c","device/Coilcube (Splitcore)/c098e5494ddff52d","device/Coilcube/c098e5434ff35895","device/Coilcube (Splitcore)/c098e5494ddf1d8e","device/Coilcube/c098e5434ff35740","device/Coilcube/c098e5434ff36ae0","device/Coilcube/c098e5434ff3a887","device/Coilcube/c098e5434ff35891","device/Coilcube/c098e5434ff381b5","device/Coilcube/c098e5434ff3a7c5","device/Coilcube/c098e5434ff35b22","device/Coilcube/c098e5434ff36ac0","device/Coilcube/c098e5434ff370c1","device/Coilcube/c098e5434ff35201","device/Coilcube (Splitcore)/c098e5494ddfc05a","device/Coilcube/c098e5434ff399ae","device/Coilcube/c098e5434ff33e0f","device/Coilcube/c098e5434fe1aac9","device/Coilcube/c098e5434ff39546","device/Impulse/c098e5494ddfd69d","device/Coilcube (Splitcore)/c098e5494ddfe7d9","device/Impulse/c098e5494ddfc2f4","device/Coilcube/c098e5434ff3ac3b","device/Impulse/c098e5494ddfbace","device/Coilcube/c098e5434ff35720","device/Coilcube/c098e5434ff3404e","device/Coilcube/c098e5434ff366bd","device/Coilcube (Splitcore)/c098e5494ddff52c","device/Coilcube/c098e5434ff3ab86","device/Coilcube/c098e5434ff3a88e","device/Impulse/c098e5494ddfb95a","device/Coilcube/c098e5434ff33609","device/Coilcube (Splitcore)/c098e5b94ddff52c","device/sEHnsor/c098e5434ff33609","device/Coilcube/c098e54342f35720","device/Coilcube (Splitcore)/c098e5594ddff52c","device/Coilcube (Splitcore)/c098e5494ddfea8f","device/Coilcube (Splitcore)/c098e5494d7ff1fa","device/Coilcube (Splitcore)/c098e5494ddff1fa","device/Coilcube/c098e5434ff38c26","device/Thermes/c098e5494ddff52c","device/Coilcube/c098e5434f5350fe","device/Coilcube (Splitcore)/c098e5495ddfe7d9","device/Coilcube (Splitcore)/c098e54941dfe7d9","device/Coilcube/c098e5434ff39f46","device/Coilcube/c098e5434f5e3699","device/Coilcube/c098e5434ff35338","device/Coilcube/c098e54345f39546","device/Coilcube/c098e54f4ff399ae","device/Ligeiro/c098e5d0001c","device/Ligeiro/c098e5d00048","device/Ligeiro/c098e5d00050","device/Ligeiro/c098e5d0004c","device/Ligeiro/c098e5d00053","device/Ligeiro/c098e5d0004e","device/Ligeiro/c098e5d00042","device/Ligeiro/c098e5d00029","device/Ligeiro/c098e5d00025","device/Ligeiro/c098e5d0004f","device/Ligeiro/c098e5d00047","device/Ligeiro/c098e5d0004d","device/Ligeiro/c098e5d00024","device/Ligeiro/c098e5d0004b","device/Ligeiro/c098e5d00044","device/Ligeiro/c098e5d0004a","device/Ligeiro/c098e5d00028","device/Ligeiro/c098e5d0003f","device/PowerBlade/c098e570014a","device/Ligeiro/c098e5d00036","device/BLEES/c098e530005a","device/PowerBlade/c098e570021e","device/BLEES/c098e530005b"]
    });
    
    var brokerHostFile = '$KEYSTORE/brokerHostFile';
    var brokerHost = '';
    try {
        brokerHost = getResource(brokerHostFile, 1000).trim();
    } catch (e) {
        console.log('BleeSensorr.js: Could not get ' + brokerHostFile + ":  " + e)
        brokerHost = 'ThisIsNotAPipeNorIsITheBrokerHostIPAddress.UseSummonToFindALocalBroker';
    }

    // Hint: Use the lab11 Summon app to find local SwarmBoxes with BLE
    MQTTSubscriber.setParameter('brokerHost', brokerHost);

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
