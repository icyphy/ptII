// Below is the copyright agreement for the Ptolemy II system.
//
// Copyright (c) 2017 The Regents of the University of California.
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

/** A Nashorn operating system module that implements a subset of the
 *  Node os module.
 *
 *  @module os
 *  @author Christopher Broosk
 *  @version $$Id$$
 */

// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals Java, exports, require */
/*jshint globalstrict: true*/
"use strict";

/** Get the hostname.
 *  @return The hostname
 */
exports.hostname = function() {
    var InetAddress = Java.type('java.net.InetAddress');
    try {
        var result = InetAddress.getLocalHost().getHostName();
        if (result.length() !== 0) {
            return result;
        }
    } catch (e) {
    }

    var System = Java.type('java.lang.System');
    host = System.getenv("HOSTNAME");
    if (host !== null) {
        return host;
    }
    var host = System.getenv("COMPUTERNAME");
    if (host !== null) {
        return host;
    }
    return null;
};

