// Copyright (c) 2014-2015 The Regents of the University of California.
// All rights reserved.

// Permission is hereby granted, without written agreement and without
// license or royalty fees, to use, copy, modify, and distribute this
// software and its documentation for any purpose, provided that the above
// copyright notice and the following two paragraphs appear in all copies
// of this software.

// IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
// FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.

// THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
// ENHANCEMENTS, OR MODIFICATIONS.

/** Produce outputs periodically, at one second intervals,
 *  as long as the most recently received suppress input is not true.
 *  The output is a count of the periods, starting with 1.
 *
 *  @accessor HandlerWithSuppress
 *  @input suppress If true, then suppress the output triggered by a callback.
 *  @input produce An event here is produced as an output, unless suppressed.
 *  @output output The count of the firing.
 *  @author Edward A. Lee
 *  @version $$Id$$
 */

exports.setup = function() {
    this.input("suppress", {'value':false, 'type': "boolean"});
    this.input("produce");
    this.output("output", {'type':"number"});
}
var count;
var handleTimeout = function() {
    this.send('produce', true);
};

var inputHandler = function() {
    count = count + 1;
    if (!this.get('suppress')) {
        this.send('output', count);
    }
};

exports.initialize = function() {
	this.addInputHandler('produce', inputHandler.bind(this));
    count = 0;
    setInterval(handleTimeout.bind(this), 1000);
}
