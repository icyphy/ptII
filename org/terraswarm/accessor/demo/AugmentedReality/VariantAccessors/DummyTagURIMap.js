//Dummy Accessor for an ontology map between tagURIs and accessors


var httpClient = require('@accessors-modules/http-client');

//A non-dummy implementation of this accessor would obtain this information
//from some external source and then cache it locally.
var IdToResource = {
    'ARDemo://Berkeley/CoryHall/DOPCenter/127': 'PressureSensor.js',
    'ARDemo://Berkeley/CoryHall/DOPCenter/435': 'VibrationSensor.js',
    'ARDemo://Berkeley/CoryHall/DOPCenter/105': 'TemperatureSensor.js'  
};

exports.setup = function() {
    // Inputs
    this.input('tagURI', {
        'type': 'string'
    });
    
    // Outputs
    this.output('accessor', {
        'type': 'string',
        'value': ''
    });
}

exports.initialize = function() {
    var thiz = this;
    
    this.addInputHandler('tagURI', function() {
        var uri = thiz.get('tagURI');
        
        var accessor = getResource(IdToResource[uri], 2000);
        if (accessor) {
            thiz.send('accessor', accessor);
        }
    });
}

