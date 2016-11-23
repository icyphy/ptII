// Below is the copyright agreement for the Ptolemy II system.
//
// Copyright (c) 2015-2016 The Regents of the University of California.
// All rights reserved.
//
// Permission is hereby granted, without written agreement and without
// license or royalty fees, to use, copy, modify, and distribute this
// software and its documentation for any purpose, provided that the above
// copyright notice and the following two paragraphs appear in all copies
// of this software.
//
// IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
// FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.
//
// THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
// ENHANCEMENTS, OR MODIFICATIONS.
//
//
// Ptolemy II includes the work of others, to see those copyrights, follow
// the copyright link on the splash page or see copyright.htm.
/**
 * Module to access some signal processing methods.
 *
 * The static methods defined in ptolemy.math.SignalProcessing are targeted.
 * This module is under development. Only the FFT method has been implemented.
 * @module dsp
 * @author Ilge Akkaya
 */

// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals Java, exports */
/*jshint globalstrict: true*/
"use strict";

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

exports.Signal = function (options) {
    // Provide default values for options using the following common JavaScript idiom.
};

/** Return an FFT.
 *  @param data An array of numbers.
 */
exports.Signal.prototype.fft = function (data) {
    var outputReal = [];
    outputReal = Java.type('ptolemy.math.SignalProcessing')["FFTRealOut(double[])"](data);
    var outputImag = [];
    outputImag = Java.type('ptolemy.math.SignalProcessing')["FFTImagOut(double[])"](data);

    var output = {};
    output.real = outputReal;
    output.imag = outputImag;

    return output;
};
