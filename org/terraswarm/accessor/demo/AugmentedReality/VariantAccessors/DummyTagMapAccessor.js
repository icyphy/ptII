//This is a dummy accessor (it already contains all the information it
//"accesses") that is supposed to be a placeholder for a domain accessor
//for a swarmbox maintained IdToResource mapping. The next step is to
//move the data to an actual swarmbox and perhaps obtain a different
//mapping if running the swarmlet in a different room 

var IdToResource = {
    '127': 'PressureSensor.js',
    '435': 'VibrationSensor.js',
    '105': 'TemperatureSensor.js'   
};

exports.setup = function() {
    //Inputs
    this.input('trigger');
    
    //Outputs
    this.output('IdToResource');
}

exports.initialize = function() {
    var thiz = this;
    
    this.addInputHandler('trigger' , function () {
        thiz.send( 'IdToResource', IdToResource);
    });
}
