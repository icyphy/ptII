/**
 * Module to access some signal processing methods.
 * 
 * The static methods defined in ptolemy.math.SignalProcessing are targeted.
 * This module is under development. Only the FFT method has been implemented.
 * @module dsp
 * @author Ilge Akkaya
 * @copyright http://terraswarm.org/accessors/copyright.txt
 */
 
/** Construct an instance of a Signal object type. This should be instantiated in your
 *  JavaScript code as
 *  <pre> 
 *     var dsp = require("dsp");  
 *     var dspEngine = new dsp.Signal();  
 *  </pre>
 *  An instance of this object type implements the following functions: 
 *  <ul>
 *  <li> fft(signal): Return an object containing the real and imaginary parts of the Fast Fourier Transform ( FFT) of the input.
 *  <li> (TODO) ifft(coef): Return an object containing the real and imaginary parts of the inverse FFT of the input sequence.
 *  </ul>
 *  @param options A JSON object with fields 'FIXME' and 'FIXME' that give the
 *   TODO properties of the signal such as sample rate, etc. Provide reasonable
 *   defaults.
 */
var SignalProcessing = Java.type('ptolemy.math.SignalProcessing');
 
exports.Signal = function(options) {
    // Provide default values for options using the following common JavaScript idiom.
}
 
/** Return an FFT.
 *  @param data An array of numbers. 
 */
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