/**
 * Module to access some signal processing methods.
 * 
 * The static methods defined in ptolemy.math.SignalProcessing are targeted.
 * This module is under development. Only the FFT method has been implemented.
 * @module dsp
 * @author Ilge Akkaya
 * @copyright http://terraswarm.org/accessors/copyright.txt
 */
 

var SignalProcessing = Java.type('ptolemy.math.SignalProcessing');
 
exports.Signal = function(options) {
    // Provide default values for options using the following common JavaScript idiom.
}
 
exports.Signal.prototype.fft = function(data) {  
    var outputReal = [];
    outputReal = Java.type('ptolemy.math.SignalProcessing')["FFTRealOut(double[])"](data);
    var outputImag = [];
    outputImag = Java.type('ptolemy.math.SignalProcessing')["FFTImagOut(double[])"](data);
    
    var output = {};
    output.real = outputReal;
    output.imag = outputImag;
 
    return output; 
}