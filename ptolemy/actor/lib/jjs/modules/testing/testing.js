// A module to load and run mocha test files.  
//Copyright (c) 2015 The Regents of the University of California.
//All rights reserved.

//Permission is hereby granted, without written agreement and without
//license or royalty fees, to use, copy, modify, and distribute this
//software and its documentation for any purpose, provided that the above
//copyright notice and the following two paragraphs appear in all copies
//of this software.

//IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
//FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
//ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
//THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
//SUCH DAMAGE.

//THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
//INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
//MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
//PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
//CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
//ENHANCEMENTS, OR MODIFICATIONS.

// FIXME:  Include chai and sinon here.  For some reason including chai here
// is not working?

var EventEmitter = require('events').EventEmitter;
var util = require('util');
var WindowMock = require('testing/window-mock/window-mock.js');

/** Create a Testing object for managing tests. 
 * 
 *  Testing implements the following functions:
 *  loadTestFile(filename): Load the given test file.
 *  run(): Execute the loaded test file.
 *  
 *  In the future, it would also be helpful to have:
 *  clearTests(): Clear all currently loaded tests.
 *  loadTests(string): Load the tests in the given string.
 *  setReporter(enum): Set the format of the output to one of the reporter choices. 
 *  
 *  Testing emits an 'end' event upon completion of all tests and supplies the
 *  results of the tests.
 */
exports.Testing = function() {
	EventEmitter.call(this);

	// This version of the mocha library was designed for the browser and 
	// expects and window object.  Create a mock window object.
	// The WindowMock class exports itself as 'default' which has a special 
	// meaning in ES6, but here, resolves to a property with the name 'default'.
    window = new WindowMock.default();
		
    // Requiring mocha.js creates a variable named mocha and loads content into it.
	require('testing/mocha/mocha.js');
	mocha.setup('bdd');
	
	// TODO:  Data structures to store the results
};
util.inherits(exports.Testing, EventEmitter);

/** Load the given mocha test file.
 */
exports.Testing.prototype.loadTestFile = function(filename) {
	require(filename);
};

/** Run the loaded test file. run() assumes that a file has been loaded already.
 *  Emit an 'end' event once all tests have completed and supply the test 
 *  results.
 */
exports.Testing.prototype.run = function() {
	var self = this;
	var results = "";
	
	// Register for mocha events and report test outcomes to the console.
	// TODO:  Refactor this into a separate reporter.  Report in JUnit format.
	// http://stackoverflow.com/questions/29050720/run-mocha-programatically-and-pass-results-to-variable-or-function
	mocha.run()
		.on('test', function(test) {
			results = results + '\nTest started: '+ test.title;
		    console.log('Test started: '+ test.title);
		})
		.on('test end', function(test) {
			results = results + '\nTest done: '+ test.title;
		    console.log('Test done: '+ test.title);
		})
		.on('pass', function(test) {
			results = results + '\nTest passed: '+ test.title;
		    console.log('Test passed: ' + test.title);
		})
		.on('fail', function(test, err) {
			results = results + '\nTest failed: ' + test.title;
		    console.log('Test failed: ' + test.title);
		    console.log(err);
		})
		.on('end', function() {
			results = results + "\nAll done.";
		    console.log('All done.');
		    self.emit('end', results);
		    // TODO:  Create a reporter to format results in Junit format.
		});
};
