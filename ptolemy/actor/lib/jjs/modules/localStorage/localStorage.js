// Below is the copyright agreement for the Ptolemy II system.
//
// Copyright (c) 2014-2016 The Regents of the University of California.
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
 * Module supporting persistent local key-value storage.
 * Has the same interface as the node-persist (https://github.com/simonlast/node-persist),
 * except for values(callback) and fine-grained control functions (e.g. persist()).
 * @module localStorage
 * @author Hokeun Kim
 * @version $$Id$$
 */

// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals actor, exports, Java, module */
/*jshint globalstrict: true */
"use strict";

////////////////////
// Create a Java instance of LocalStorageHelper, using the specific name (container + actor)
// for the directory name.
var LocalStorageHelper = Java.type('ptolemy.actor.lib.jjs.modules.localStorage.LocalStorageHelper');
var storage;

/** Set up a new persistent storage in the file system.
 * This must be called before calling any other functions in localStorage.
 */
module.exports.initSync = function (opts) {
    var persistenceDir;
    if (!opts || !opts.dir) {
        persistenceDir = 'persist';
    } else {
        persistenceDir = opts.dir;
    }
    storage = new LocalStorageHelper(persistenceDir, this.accessorName);
};

////////////////////
// Wrappers of the function in the java helper.

/**  Take a key and return its value from the local storage if the key exists,
 * otherwise, return null.
 */
module.exports.getItem = function (key) {
    return storage.getItem(key);
};


/** Take a key-value pair and store the pair into the local storage. */
module.exports.setItem = function (key, value) {
    storage.setItem(key, value);
};

/** Take a key and remove it from the local storage. */
module.exports.remove = function (key) {
    storage.removeItem(key);
};

/** Remove all keys in the local storage. */
module.exports.clear = function () {
    storage.clear();
};

/** Return a key with index n, or null if it is not present. */
module.exports.key = function (n) {
    return storage.key(n);
};

/** Return the number of keys stored in the local storage. */
module.exports.length = function () {
    return storage.length();
};
