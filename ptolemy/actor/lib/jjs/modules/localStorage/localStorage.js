// Module supporting persistent local key-value storage.
// Has the same interface as the node-persist (https://github.com/simonlast/node-persist),
// except for values(callback) and fine-grained control functions (e.g. persist()).
// Authors: Hokeun Kim
// Copyright: http://terraswarm.org/accessors/copyright.txt
//
var LocalStorageHelper = Java.type('ptolemy.actor.lib.jjs.modules.localStorage.LocalStorageHelper');

////////////////////
// Create a Java instance of LocalStorageHelper, using the specific name (container + actor)
// for the directory name.

var storage;
////////////////////
// Set up a new persistent storage in the file system.
// This must be called before calling any other functions in localStorage.
module.exports.initSync = function(opts) {
    var persistenceDir;
    if (!opts || !opts['dir']) {
        persistenceDir = 'persist';
    }
    else {
        persistenceDir = opts['dir'];
    }
    storage = new LocalStorageHelper(persistenceDir, actor.getContainer().getName() + '-' + actor.getName());
}

////////////////////
// Wrappers of the function in the java helper.

////////////////////
// Take a key and return its value from the local storage if the key exists,
// otherwise, return null.
module.exports.getItem = function(key) {
    return storage.getItem(key);
}

////////////////////
// Take a key-value pair and store the pair into the local storage.
module.exports.setItem = function(key, value) {
    storage.setItem(key, value);
}

////////////////////
// Take a key and remove it from the local storage.
module.exports.removeItem = function(key) {
    storage.removeItem(key);
}

////////////////////
// Remove all keys in the local storage.
module.exports.clear = function() {
    storage.clear();
}

////////////////////
// Return a key with index n, or null if it is not present.
module.exports.key = function(n) {
    return storage.key(n);
}

////////////////////
// Return the number of keys stored in the local storage.
module.exports.length = function() {
    return storage.length();
}
