// JavaScript functions to be shared among accessor hosts.
//
// Copyright (c) 2015-2017 The Regents of the University of California.
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


///////////////////////////////////////////////////////////////////
////                NOTE:               ////
////  This file has an exact copy in    ////
////    accessors/web/hosts/common      ////
////    and in the Ptolemy tree at      ////
////   $PTII/ptolemy/actor/lib/jjs      ////
//// If you update here, please update  ////
////   both places and run the tests    ////
///////////////////////////////////////////////////////////////////

/** This module provides host-independent functions for swarmlet hosts.
 *  A specific host (such as the Node.js host, the browser host, or the Ptolemy II
 *  host) can use this module to implement common functionality that is realizable
 *  in pure JavaScript.
 *
 *  Instantiating Accessors
 *  -----------------------
 *
 *  Specifically, this module provides a constructor for instantiating accessors,
 *  and a convenience function (**instantiateAccessor**) that takes as an argument
 *  the fully qualified accessor class name, such as 'net/REST'.
 *
 *  For these functions to be able to instantiate an accessor given only its class name,
 *  the host needs to provide as an argument to new Accessor() or
 *  instantiateAccessor() a function, here designated getAccessorCode(), that
 *  will retrieve the accessor source code given the class name. This cannot be done
 *  in a host-independent way. For example, ```getAccessorCode('net/REST')```
 *  should return the JavaScript  code defining the REST accessor.
 *  This function must also be provided if the host is to support accessor
 *  subclassing (extend()) or interface implementation (implement()).
 *
 *  The constructor and instantiate functions return an object that is an instance of an
 *  accessor. A specific host will typically use this by invoking the following
 *  instance's functions, perhaps in this order:
 *
 *  * **setParameter**(name, value): Set a parameter value.
 *  * **initialize**(): Initialize the accessor.
 *  * **provideInput**(name, value): Provide an input value.
 *  * **react()** React to input values and fire the accessor.
 *  * **latestOutput**(name): Retrieve an output value produced in react().
 *  * **wrapup**(): Wrap up the accessor.
 *
 *
 *  The react() function first invokes all input handlers that have been registered
 *  to handle provided inputs. It then invokes all input handlers that have been
 *  registered to handle any input.  Finally, it invokes the fire() function of the
 *  accessor, if one is defined.
 *
 *  Events Emitted
 *  --------------
 *
 *  An instantiated accessor is an event emitter that emits the following events:
 *
 *  * **initialize**: Emitted when the accessor has been initialized.
 *  * **output**(*name*, *value*): Emitted when an output with the specified name
 *    and value are sent.
 *  * **reactStart** Emitted when a reaction is starting.
 *  * **reactEnd**: Emitted when the accessor has reacted.
 *  * **wrapup**: Emitted when the accessor has wrapped up.
 *
 *
 *  Extend and Implement
 *  --------------------
 *
 *  If a getAccessorCode() function is provided, then this implementation supports
 *  the **extend**() and **implement**() functions. An accessor A extends an accessor
 *  B if it includes in its setup() function a statement like:
 *
 *      this.extend(B);
 *
 *  In this case, there will be two accessor instances created, one for A and one for
 *  B. Somewhat confusingly, A will be the prototype of B. This may seem backwards,
 *  but there is a good reason for it. B omits several key properties, such as its
 *  lists of inputs, outputs, and parameters. And thus, when the setup() function of
 *  B creates an input, that input becomes an input of A.  Accessor A can reference
 *  the instance of B as A.ssuper.  The exported functions of B are accessible via
 *  as A.ssuper.exports or A.exports.ssuper.
 *
 *  Accessor A **implements** accessor B if its setup function includes:
 *
 *      this.implement(B);
 *
 *  This case is similar, in that A becomes the prototype of B, but the instance of
 *  B is not available to A. The only interaction between the two is that when the
 *  setup() function of B is invoked, it will again create inputs, outputs, and
 *  parameters in A rather than in its own instance.
 *
 *  If A extends B, then the exports property of B becomes the prototype of the
 *  exports property of A.  This is the reverse of the prototype chain for the
 *  accessor instance itself. The reason for this is that B may define 'base-class'
 *  functions and properties that may or may not be overridden by A. These should
 *  be declared as exported properties.  For example, if B has a property:
 *
 *      exports.foo = 10;
 *
 *  Then A can refer to foo as `exports.foo`. A can also override the value of
 *  foo by just writing to it.
 *
 *  Composite Accessors
 *  -------------------
 *
 *  If a getAccessorCode() function is provided, then this implementation supports
 *  composite accessors, which can instantiate other accessors (instantiate()) and
 *  connect them (connect()).  That is, an accessor may be defined as a hierarchical
 *  composition of other accessors.
 *
 *  If C is a composite accessor and you invoke C.provideInput(name, value) to
 *  provide an input to C, the same input will be automatically provided to any
 *  contained accessor that is connected to the input with the specified name.
 *
 *  Moreover, if C does not provide its own fire() function, then a default fire()
 *  function will invoke react() on any contained accessors that are provided with
 *  an input. If those contained accessors produce outputs, those will be provided
 *  as inputs to any connected accessors and then those will react as well.
 *  Finally, a contained accessor may send an output to an output of C, in which
 *  case the composite accessor will produce an output.
 *
 *  The reactions of contained accessors in a composite will occur in topological
 *  sort order (upstream accessors are assured of reacting first). This ensures that
 *  reactions of a composite are deterministic. Specifically, suppose that A, B, and
 *  D are all accessors contained by a composite C. Suppose that the output of A goes
 *  to both B and D, and that the output of B also goes to D. Then B will always react
 *  before D, since it is upstream of D.
 *
 *  A composition of accessors can have cycles, but at least one output in any
 *  directed cycle must have the option 'spontaneous' set to 'true'. This means that
 *  the output is not immediately produced as a reaction to an input, but rather will
 *  be produced some time later as a consequence of a callback. If there is no such
 *  marked output, then the cycle is a causality loop. There is no way to determine
 *  the order in which accessors should react.
 *
 *  Mutable Accessors
 *  -----------------
 *
 *  Mutable accessors are a particular type of composite accessors. They have the ability
 *  to dynamically change their behavior by substituting a contained accessor X by another
 *  accessor X'. The condition is that X, as well as X', both are type refinement of the
 *  mutable accessor. By calling 'reifiableBy', accessors are tested for the condition.
 *
 *  When instantiated, a mutableAcessor is equivalent to an interface definition (but with
 *  a bunch of additional/configuration options). Every accessor tested for reification will
 *  be stored. When calling 'reify' function on a particular accessor X, the mutableAccessor
 *  will connect to X, making it equivalent to X itself. This is enabled by the composition
 *  mechanism that is done on runtime.
 *  this.emit
 *  The choice of the accessor to be used for reification can be forced (by giving the accessor
 *  as parameter) or by giving a sorting function. Depending on a bunch of options provided
 *  by the swarmlet designer, 'sort' function will select the best match.
 *
 *  At any time, a previous reification can be removed. This will make the mutableAccessor
 *  equivalent to an interface again. Consequently, another reification may be performed,
 *  enabling thus dynamic substitution.
 *
 *  Spontaneous Accessors
 *  ---------------------
 *
 *  An accessor need not have any inputs. In this case, an invocation of react()
 *  will not invoke any input handlers (there cannot be any) and will only invoke the
 *  fire() function.  Even with no fire() function, such an accessor can produce outputs
 *  if it sets up callbacks in its initialize() function, for example using
 *  setTimeout() or setInterval(). We call such an accessor a **spontaneous accessor**,
 *  because it spontaneously produces outputs without being triggered by any input.
 *
 *  A composite accessor can contain spontaneous accessors. When these produce outputs,
 *  the composite accessor will automatically react. Hence, the contained spontaneous
 *  accessor can trigger reactions of other contained accessors.
 *
 *  Overriding Function Bindings
 *  ----------------------------
 *
 *  A particular accessor host (such as the browser host or Node.js host) can
 *  provide an accessor instance with specific implementations of various functions
 *  that the accessor invokes. Such custom function
 *  bindings will override the defaults in this implementation.
 *  The most useful ones to override are probably these:
 *
 *  * **require**: The host's implementation of the require function.
 *     The default implementation throws an exception indicating that the host
 *     does not support any external modules.
 *  * **get**: A function to retrieve the value of an input. The default
 *     implementation returns the the value specified by a provideInput() call, or
 *     if there has been no provideInput() call, then the value provided in the
 *     options argument of the this.input() call, or null if there is no value.
 *  * **getParameter**: A function to retrieve the value of a parameter. The default
 *     implementation returns the the value specified by a this.setParameter() call, or
 *     if there has been no this.setParameter() call, then the value provided in the
 *     options argument of the this.parameter() call, or null if there is no value.
 *  * **send**: A function to send an output. The default implementation produces
 *     the output using console.log().
 *
 *
 *  @module @accessors-hosts/commonHost
 *  @author Edward A. Lee and Chris Shaver.  Contributor: Christopher Brooks
 *  @version $$Id$$
 */

// Stop extra messages from jslint and jshint.
// See https://chess.eecs.berkeley.edu/ptexternal/wiki/Main/JSHint
/*globals actor, alert, console, Duktape, exports, instance, java, Packages, process, require, setInterval, setIntervalDet, setTimeout, setTimeoutDet, clearInterval, clearIntervalDet, clearTimeout, clearTimeoutDet, window, Map, getResource */
/*jshint globalstrict: true, multistr: true */
'use strict';

// All the accessors that have been instantiated (at any level of the hierarchy,
// and also including interfaces and accessors that are extended).
var allAccessors = [];

// If set to true, then accessors whose class name begins with 'trusted/'
// will have a binding for the getTopLevelAccessors() function.
var trustedAccessorsAllowed = false;

// Determine which accessor host is in use.
// See https://www.icyphy.org/accessors/wiki/Main/ResourcesForHostAuthors#Differentiating
// See https://stijndewitt.com/2014/01/26/enums-in-javascript/
var accessorHostsEnum = {
    BROWSER: 1,
    CAPECODE: 2,
    DEFAULT: 3,
    DUKTAPE: 4,
    NODE: 5,
    NASHORN: 6,
    CORDOVA:7
};

var accessorHost = accessorHostsEnum.DEFAULT;

// Be sure to export these variables before any require()s of the util module because util.js
// require()s commonHost.  Do not put these at the end of the file.
// To test, run "ant tests.duk"
exports.accessorHostsEnum = accessorHostsEnum;
exports.accessorHost = accessorHost;

// In alphabetical order.
// FIXME: This should not be needed!!! Remove it. 
if (typeof window !== 'undefined') {
    if (typeof cordova !== 'undefined') {
        accessorHost = accessorHostsEnum.CORDOVA;
    } else if (window.hasOwnProperty('browserJSLoaded')) {
        accessorHost = accessorHostsEnum.BROWSER;
    }
} else if (typeof actor !== 'undefined' && typeof Packages !== 'undefined' && typeof Packages.java.util.Vector === 'function') {
    accessorHost = accessorHostsEnum.CAPECODE;
} else if (typeof Duktape === 'object') {
    accessorHost = accessorHostsEnum.DUKTAPE;
} else if (typeof Packages !== 'undefined' && typeof Packages.java.util.Vector === 'function') {
    accessorHost = accessorHostsEnum.NASHORN;
} else if (typeof process !== 'undefined' && typeof process.version === 'string') {
    accessorHost = accessorHostsEnum.NODE;
}

if (accessorHost === accessorHostsEnum.DUKTAPE) {
    var util = require('../common/modules/util.js');
    var EventEmitter = require('../common/modules/events.js').EventEmitter;
} else if(accessorHost === accessorHostsEnum.CORDOVA) {
    var util = require('cordova/utils');
} else {
    // The node host will load the core util module here and not access the file system.
    var util = require('util');
    var EventEmitter = require('events').EventEmitter;
}

if (accessorHost === accessorHostsEnum.CAPECODE || accessorHost === accessorHostsEnum.NASHORN || accessorHost === accessorHostsEnum.DUKTAPE) {
    ; // Then no deterministic temporal semantics
} else if (accessorHost === accessorHostsEnum.BROWSER) {
    var deterministicTemporalSemantics = require('/accessors/node_modules/@accessors-hosts/common/modules/deterministicTemporalSemantics');
} else if (accessorHost === accessorHostsEnum.CORDOVA) {
    // The module should be already loaded. But for more more convenience when binding to the deterministic behavior,
    // then we add the following
    var deterministicTemporalSemantics = {};
    deterministicTemporalSemantics.setTimeoutDet = setTimeoutDet;
    deterministicTemporalSemantics.setIntervalDet = setIntervalDet;
    deterministicTemporalSemantics.clearTimeoutDet = clearTimeoutDet;
    deterministicTemporalSemantics.clearIntervalDet = clearIntervalDet;
} else {    
    var deterministicTemporalSemantics = require('../common/modules/deterministicTemporalSemantics.js');
}

///////////////////////////////////////////////////////////////////
//// Accessor class and its functions.

/** Create (using new) an accessor instance whose interface and functionality is given by
 *  the specified code. This will evaluate the specified code, and if it exports a
 *  setup() function, invoke that function. The created object includes at least the
 *  following properties:
 *
 *  * **accessorName**: The name of the instance provided to the constructor.
 *  * **exports**: An object that includes any properties that have have been
 *    explicitly added to the exports property in the specified code.
 *  * **inputList**: An array of input names (see below).
 *  * **inputs**: An object with one property per input (see below).
 *  * **outputList**: An array of output names (see below).
 *  * **outputs**: An object with one property per output (see below).
 *  * **parameterList**: An array of parameter names (see below).
 *  * **parameters**: An object with one property per parameter (see below).
 *  * **inputHandlers**: An object indexed by input name with
 *    an array of input handlers, each of which is a function.
 *  * **anyInputHandlers**: An array of input handlers to be invoked
 *    when any input arrives (the name argument of addInputHandler is null).
 *  * **inputHandlersIndex**: An object indexed by handler id (returned
 *    by this.addInputHandler()) that contains objects of the form
 *    {'name': nameOfInput, 'index': arrayIndexOfHandler}.
 *    This is used by this.removeInputHandler(). If the handler is one
 *    for any input, then nameOfInput is null and arrayIndexOfHandler
 *    specifies the position of the handler in the anyInputHandlers array.
 *  * **monitor**: An object that records the number of initializations, wrapups and
 *    reactions, as well as the dates of each first and last events.
 *  * **realizesList**: An array of realized features names (see below).
 *  * **realizes**: An object with one property per realized feature (see below).
 *
 *  Inputs, outputs, and parameters, in addition to realized features in an accessor
 *  have a defined order. The ```inputList``` property is an array giving the
 *  name of each input in the order in which it is defined in the setup() function.
 *  For each entry in that array, there is a property by that name in the
 *  ```inputs``` object, indexed by the name. The value of that property is the
 *  options object given to the ```input()``` function, possibly with additional
 *  properties such as 'destination', which is used for composite accessors.
 *  Similarly, parameters and outputs are represented in the
 *  data structure by an array of names and an object with the options values.
 *  ```realizes``` objects describe the added values of the accessor, for example
 *  'Light', 'Camera'... This information is particularly useful when an accessor
 *  is tested for reification of a mutableAccessor. Such test is better solved
 *  using ontologies, rather equality.
 *  FIXME: Ontologies are to be added.
 *
 *  The returned instance may also include the following properties:
 *
 *  * **accessorClass**: The class name of the accessor, if not anonymous.
 *  * **containedAccessors**: A list of contained accessors, if this is a composite
 *    accessor or a mutableAccessor.
 *  * **container**: A reference to the containing accessor, if this instance is
 *    instantiated by a composite accessor.
 *  * **extendedBy**: A reference to the accessor that this is extended by, if there is
 *    one.
 *  * **extending**: A reference to the accessor that this extends, if it extends one.
 *  * **implementedInterfaces**: An array of interfaces that this accessor implements.
 *  * **root**: The root of the extend/implement tree. This is the accessor instance
 *    being invoked in a swarmlet, and each of the instances it extends or implements
 *    will reference it in their root property.
 *  * **ssuper**: If this accessor extends another, this is a reference to the instance
 *    of the accessor it extends.
 *  * **mutable**: If this accessor is a mutableAccessor, this attribute will be set.
 *
 *  If the returned instance is a mutableAccessor, then it will include these additional
 *  properties:
 *  * **status**: indicates the current state among all the states of the lifecycle of a
 *    given mutableAccessor
 *  * **reifyingAccessorsList**: An array of the reifying accessors, together with some
 *    information (such as Location...) that will be relevant for sorting. This list should
 *    be ordered using a given sorting function.
 *  * **inputsMap**: An array that maps the mutableAccessor inputs to the reifying accessor
 *    inputs.
 *  * **outputsMap**: An array that maps the mutableAccessor outputs to the reifying accessor
 *    outputs.
 *  * **parametersMap**: An array that maps the non mapped mutableAccessor inputs to, possibly,
 *    the reifying accessor non connected inputs.
 *  Notes: (i) When a mutableAccessor is reified, the attribute containedAccessors will contain
 *  he selected accessor for reification (ii) A mutableAccessor cannot extend another accessor.
 *  FIXME: Check if a  mutableAccessor can implement another accessor.
 *
 *  The bindings parameter provides function bindings for functions that are used by
 *  accessors.  Any that are not provided will be provided with defaults.
 *  Any that are provided will override the defaults.
 *
 *  @param accessorName A name to give to the accessor.
 *  @param code The accessor source code.
 *  @param getAccessorCode A function that will retrieve the source code of a specified
 *   accessor (used to implement the this.extend() and this.implement() functions), or null if
 *   the host does not support accessors that extend other accessors.
 *  @param bindings The functhis.emittion bindings to be used by the accessor.
 *  @param extendedBy An optional argument specifying what accessor is extending
 *   this new instance. Pass null or no argument if this accessor is not being extended.
 *   If this argument is present, then the getAccessorCode and bindings arguments are
 *   ignored (the instance inherits those properties from the extender).
 *  @param implementedBy An optional argument specifying what accessor is implementing
 *   this new instance. Pass null or no argument if this accessor is not being
 *   implemented.
 *   If this argument is present, then the getAccessorCode and bindings arguments are
 *   ignored (the instance inherits those properties from the implementer).
 *
 */
function Accessor(accessorName, code, getAccessorCode, bindings, extendedBy, implementedBy) {
    if (!code) {
        throw new Error('No accessor code specified.');
    }
    var binding;
    // First, create all the properties that this instance will have as its 'own'
    // properties, even if it is being extended or implemented.
    this.accessorName = accessorName;
    this.extendedBy = extendedBy;
    this.implementedBy = implementedBy;

    this.code = code;

    this.bindings = bindings;

    ///////////////////////////////////////////////////////////////////
    //// Override using specified bindings.

    // Any property defined in the bindings argument overrides prototype functions
    // for this instance. Do this before creating other own properties in case
    // the caller accidentally tries to provide bindings whose names match key
    // properties of this instance.
    for (binding in bindings) {
        this[binding] = bindings[binding];
    }


    // If no extendedBy or implementedBy is given, then initialize the data structures
    // to be used by this accessor instance.  These data structures will be in the
    // prototype chain for any instance that this accessor implements or extends,
    // so that the setup() function of those instances can modify these data structures.
    if (!extendedBy && !implementedBy) {

        this.getAccessorCode = getAccessorCode;

        ////////////////////////////////////////////////////////////////////
        //// Define the properties that define inputs and input handlers

        // Inputs, outputs, and parameters need to be able to be accessed two ways,
        // by name and in the order they are defined. Hence, we define two data
        // structures for each, one of which is an ordered list of names, and one
        // of which is an object with a property for each input, output, or parameter.
        this.inputList = [];
        this.inputs = {};
        this.inputHandlers = {};
        this.anyInputHandlers = [];
        this.inputHandlersIndex = {};

        // Counter used to assign unique IDs to each input handler.
        this.inputHandlerID = 0;

        ////////////////////////////////////////////////////////////////////
        //// Define the properties that define outputs.

        this.outputList = [];
        this.outputs = {};

        ////////////////////////////////////////////////////////////////////
        //// Define the properties that define parameters.

        this.parameterList = [];
        this.parameters = {};

        ////////////////////////////////////////////////////////////////////
        //// Define the properties that define realized functions.

        this.realizesList = [];
        this.realizes = {};


        ////////////////////////////////////////////////////////////////////
        //// Support for composite accessors.

        // List of contained accessors.
        this.containedAccessors = [];

        ////////////////////////////////////////////////////////////////////
        //// Support for monitoring.
        this.monitor = {};

        // Monitoring initialize events
        this.monitor.initialize = {
            'count': 0,
            'firstOccurrenceDate': {},
            'latestOccurrenceDate': {}
        };

        // Monitoring react events
        this.monitor.reactStart = {
            'count': 0,
            'firstOccurrenceDate': {},
            'latestOccurrenceDate': {}
        };

        // Monitoring react events
        this.monitor.reactEnd = {
            'count': 0,
            'firstOccurrenceDate': {},
            'latestOccurrenceDate': {}
        };

        // Monitoring wrapup events
        this.monitor.wrapup = {
            'count': 0,
            'firstOccurrenceDate': {},
            'latestOccurrenceDate': {}
        };
    }

    // Define the exports object to be populated by the accessor code.
    // Even if there is an object extending this one, this one has its own
    // exports property. If this object is being extended (rather than
    // implemented), then this new exports object will become the prototype
    // of the exports object of the immediate parent (see the this.extend() function
    // below).
    this.exports = {};

    // To keep track of implemented interfaces.
    // Note that since this is an 'own' property, only the immediately
    // implemented interfaces are listed.
    this.implementedInterfaces = [];

    // To prevent ssuper from being inherited via the prototype chain,
    // define it explicitly to be null.
    this.ssuper = null;
    this.exports.ssuper = null;

    ///////////////////////////////////////////////////////////////////
    //// Evaluate the accessor code.

    // In strict mode, eval() cannot modify the scope of this function.
    // Hence, we wrap the code in the function, and will pass in the
    // exports object that we want the code to modify.

    // Need to provide all the functions that are allowed to be invoked
    // as top-level functions in the accessor specification.
    if (bindings && bindings.getResource) {
        this.getResource = bindings.getResource;
    } else if (typeof getResource !== 'undefined') {
        this.getResource = getResource;
    } else {
        throw new Error('Host does not define required getResource function.');
    }

    if (bindings && bindings.setInterval) {
        this.setInterval = bindings.setInterval;
    } else if (typeof setInterval !== 'undefined') {
        if (deterministicTemporalSemantics) {
            this.setInterval = deterministicTemporalSemantics.setIntervalDet;
        } else {
            this.setInterval = setInterval;
        }
    } else {
        throw new Error('Host does not define required setInterval function.');
    }

    if (bindings && bindings.setTimeout) {
        this.setTimeout = bindings.setTimeout;
    } else if (typeof setTimeout !== 'undefined') {
        if (deterministicTemporalSemantics) {
            this.setTimeout = deterministicTemporalSemantics.setTimeoutDet;
        } else {
            this.setTimeout = setTimeout;
        }
    } else {
        throw new Error('Host does not define required setTimeout function.');
    }

    if (bindings && bindings.clearInterval) {
        this.clearInterval = bindings.clearInterval;
    } else if (typeof clearInterval !== 'undefined') {
        if (deterministicTemporalSemantics) {
            this.clearInterval = deterministicTemporalSemantics.clearIntervalDet;
        } else {
            this.clearInterval = clearInterval;
        }
    } else {
        throw new Error('Host does not define required clearInterval function.');
    }

    if (bindings && bindings.clearTimeout) {
        this.clearTimeout = bindings.clearTimeout;
    } else if (typeof clearTimeout !== 'undefined') {
        if (deterministicTemporalSemantics) {
            this.clearTimeout = deterministicTemporalSemantics.clearTimeoutDet;
        } else {
            this.clearTimeout = clearTimeout;
        }
    } else {
        throw new Error('Host does not define required clearTimeout function.');
    }

    if (bindings && bindings.alert) {
        this.alert = bindings.alert;
    } else if (typeof alert !== 'undefined') {
        this.alert = alert;
    } else if (typeof console.log !== 'undefined') {
        this.alert = console.log;
    } else {
        throw new Error('Host does not define required alert function.');
    }

    // By default, the root property is this instance.
    this.root = this;

    var wrapper = new Function('\
alert, \
error, \
exports, \
getResource, \
httpRequest, \
readURL, \
require, \
setInterval, \
setTimeout, \
clearInterval, \
clearTimeout',
        code);
    wrapper.call(this,
        this.alert,
        this.error,
        this.exports,
        this.getResource,
        this.httpRequest,
        this.readURL,
        this.require,
        this.setInterval,
        this.setTimeout,
        this.clearInterval,
        this.clearTimeout);

    // Mark that the accessor has not been initialized
    this.initialized = false;

    // Record the instance indexed by its exports property.
    _accessorInstanceTable[this.exports] = this;

    ///////////////////////////////////////////////////////////////////
    //// Set up the prototype chain and ssuper properties.

    if (extendedBy) {
        // This accessor is being extended.

        // First argument is the object, second is the prototype.
        // Note the reverse arrangment for the accessor instance vs.
        // its exports property.
        Object.setPrototypeOf(this, extendedBy);
        Object.setPrototypeOf(extendedBy.exports, this.exports);

        if (extendedBy.ssuper) {
            throw new Error('Cannot extend more than one base accessor.');
        }

        extendedBy.ssuper = this;
        extendedBy.exports.ssuper = this.exports;

        this.root = extendedBy.root;

        extendedBy.extending = this;

    } else if (implementedBy) {
        // This accessor interface is being implemented.
        Object.setPrototypeOf(this, implementedBy);
        // Note that these now no relationship between the exports properties,
        // and there are no ssuper properties provided.

        this.root = implementedBy.root;

        implementedBy.implementedInterfaces.push(this);
    }

    ///////////////////////////////////////////////////////////////////
    //// Evaluate the setup() function to populate the data structures.

    if (typeof this.exports.setup === 'function') {
        this.exports.setup.call(this);
    }

    ///////////////////////////////////////////////////////////////////
    //// Provide wrapper functions for initialize(), fire(), and wrapup().

    if (!extendedBy && !implementedBy) {
        // The instance versions of initialize() and wrapup() perform
        // functions that every accessor should perform, including handling
        // scheduling of any contained accessors.  They will also invoke
        // exports.initialize() and exports.wrapup(), if those are defined.
        this.initialize = function () {
            if (this.containedAccessors && this.containedAccessors.length > 0) {
                this.assignPriorities();
                this.eventQueue = [];
                for (var i = 0; i < this.containedAccessors.length; i++) {
                    if (this.containedAccessors[i].initialize) {
                        this.containedAccessors[i].initialize();
                    }
                }
            }
            if (typeof this.exports.initialize === 'function') {
                // Call with 'this' being the accessor instance, not the exports
                // property.
                this.exports.initialize.call(this);
            }
            this.initialized = true;

            // Update monitoring information
            this.updateMonitoringInformation('initialize');

            this.emit('initialize');
        };

        this.fire = function () {
            if (typeof this.exports.fire === 'function') {
                // Call with 'this' being the accessor instance, not the exports
                // property.
                this.exports.fire.call(this);
            }

            // Update monitoring information
            // FIXME: Are react and fire semantically different?
        };

        this.wrapup = function () {
            // Mark that this accessor has not been initialized.
            this.initialized = false;

            // Remove all input handlers.
            this.inputHandlers = {};
            this.anyInputHandlers = [];
            this.inputHandlersIndex = {};
            // Reset counter used to assign unique IDs to each input handler.
            this.inputHandlerID = 0;

            // Invoke wrapup on contained accessors.
            if (this.containedAccessors && this.containedAccessors.length > 0) {
                for (var i = 0; i < this.containedAccessors.length; i++) {
                    if (this.containedAccessors[i].wrapup) {
                        this.containedAccessors[i].wrapup();
                    }
                }
            }
            if (typeof this.exports.wrapup === 'function') {
                // Call with 'this' being the accessor instance, not the exports
                // property.
                this.exports.wrapup.call(this);
            }

            // Update monitoring information
            this.updateMonitoringInformation('wrapup');

            this.emit('wrapup');
        };
    }
}
if (accessorHost === accessorHostsEnum.CORDOVA) {
    Accessor.prototype = Object.create(EventEmitter.prototype, {
        constructor: {
            value: Accessor,
            enumerable: false,
            writable: true,
            configurable: true
        }
    });
} else {
    util.inherits(Accessor, EventEmitter);
}

/** Add an input handler for the specified input and return a handle that
 *  can be used to remove the input handler.
 *  If no name is given (the first argument is null or a function), then the
 *  function will be invoked when any input changes.
 *  If more arguments are given beyond the first two (or first, if the function
 *  is given first), then those arguments
 *  will be passed to the input handler function when it is invoked.
 *  @param name The name of the input (a string).
 *  @param func The function to be invoked.
 *  @param args Additional arguments to pass to the function.
 *  @return An ID that can be passed to this.removeInputHandler().
 */
Accessor.prototype.addInputHandler = function (name, func) {
    var argCount = 2,
        callback, id, tail;
    if (name && typeof name !== 'string') {
        // Tolerate a single argument, a function.
        if (typeof name === 'function') {
            func = name;
            name = null;
            argCount = 1;
        } else {
            throw new Error('name argument is required to be a string. Got: ' + (typeof name));
        }
    }
    if (!func) {
        func = nullHandlerFunction;
    } else if (typeof func !== 'function') {
        throw new Error('Argument of addInputHandler is not a function. It is: ' + func);
    }

    // Check that the input exists.
    if (name && !this.inputs[name]) {
        throw new Error('Cannot add an input handler to a non-existent input: ' + name);
    }

    // Bind the callback function so that it is always invoked in the context
    // of the root accessor object (not extended or implemented instances).
    // If there are arguments to the callback, create a new function.
    // Get an array of arguments excluding the first two.
    // When that function is invoked, 'this' will be the data structure
    // of the top-level instance (which by prototype chain, can reach down
    // the inheritance hierarchy).
    tail = Array.prototype.slice.call(arguments, argCount);
    var thiz = this.root;
    if (tail.length !== 0) {
        callback = function () {
            func.apply(thiz, tail);
        };
    } else {
        callback = func.bind(thiz);
    }
    // Need to allow more than one handler and need to return a handle
    // that can be used by removeInputHandler.
    var index;
    if (name) {
        if (!thiz.inputHandlers[name]) {
            thiz.inputHandlers[name] = [];
        }
        index = thiz.inputHandlers[name].length;
        thiz.inputHandlers[name].push(callback);
    } else {
        index = thiz.anyInputHandlers.length;
        thiz.anyInputHandlers.push(callback);
    }
    var result = thiz.inputHandlerID;
    thiz.inputHandlersIndex[thiz.inputHandlerID++] = {
        'name': name,
        'index': index
    };
    // Record the handle in the callback function so that it can be
    // removed if it throws an exception.
    callback.handle = result;
    return result;
};

/** Assign priorities to contained accessors based on a topological sort of the
 *  connectivity graph. Priorities are integers (positive or negative), where a lower
 *  number indicates a higher priority. The number for an accessor is assured of being
 *  higher than the number for any upstream accessor.  An accessor A is upstream of
 *  an accessor B if A has an output connected to an input of B and that output is not
 *  marked "spontaneous" (that is, it does not have an option with name "spontaneous"
 *  and value true). A spontaneous output is produced by an asynchronous callback
 *  rather than as a response to an input.  Every directed cycle in a connectivity
 *  graph must contain at least one spontaneous output or there will be a deadlock
 *  due to a causality loop.
 */
Accessor.prototype.assignPriorities = function () {
    // Note that we could just use this instead of this.root because of the
    // prototype chain, but in a deep hierarchy, this will be more efficient.
    var thiz = this.root;
    var accessors = thiz.containedAccessors;

    // First, initialize the contained accessors with a null priority.
    for (var i = 0; i < accessors.length; i++) {
        accessors[i].priority = null;
    }
    // Next, assign the first accessor an arbitrary priority and follow its
    // connections to assign priorities implied by those connections.
    var startingPriority = 0;
    for (i = 0; i < accessors.length; i++) {
        // If the instance already has a priority, skip it.
        if (accessors[i].priority !== null) {
            continue;
        }
        accessors[i].priority = startingPriority;
        // console.log('Assigned priority to ' + accessors[i].accessorName + ' of ' + startingPriority);
        // Follow connections
        thiz.assignImpliedPrioritiesUpstream(accessors[i], startingPriority);
        thiz.assignImpliedPrioritiesDownstream(accessors[i], startingPriority);

        // Any remaining accessors without priorities are in one or more independent
        // connected subgraphs. To ensure that the next set of priorities does not
        // overlap those already assigned, we start with a sufficiently higher number.
        startingPriority = 2 * (accessors.length - i);
    }
};

/** Assuming that the specified accessor has an assigned priority, follow its
 *  connections downstream and assign priorities to connected accessors.
 *  Each downstream accessor gets a priority at least one greater than the
 *  priority of the specified accessor, and each gets a unique priority.
 *  Return the largest priority assigned to a downstream accessor, or if
 *  no priorities are assigned to downstream accessor, then return the priority
 *  of the specified accessor.
 *  @param accessor The contained accessor with a priority.
 *  @param cyclePriority If we encounter an accessor with this priority, then
 *   there is a causality loop.
 */
Accessor.prototype.assignImpliedPrioritiesDownstream = function (accessor, cyclePriority) {
    var myPriority = accessor.priority;
    // To get repeatable priorities, iterate over outputs in order.
    for (var i = 0; i < accessor.outputList.length; i++) {
        var output = accessor.outputs[accessor.outputList[i]];
        if (output.spontaneous) {
            // Output is spontaneous, so my priority has no implications
            // for downstream accessors.
            continue;
        }
        if (output.destinations) {
            // There are destination accessors.
            for (var j = 0; j < output.destinations.length; j++) {
                var destination = output.destinations[j];
                if (typeof destination === 'string') {
                    // Destination is an output of the container.
                    continue;
                }
                var destinationAccessor = destination.accessor;
                var destinationInput = destinationAccessor.inputs[destination.inputName];
                var theirPriority = destinationAccessor.priority;
                if (theirPriority === cyclePriority) {
                    throw new Error('Causality loop found including at least: ' +
                        destinationAccessor.accessorName);
                }
                if (theirPriority === null) {
                    // Destination has no previously assigned priority. Give it one,
                    // and follow the implications.

                    // We increment myPriority before use so that if
                    // an output is connected to multiple inputs, the
                    // inputs do not have the same priority.
                    // To replicate this, run:

                    // (cd $PTII/org/terraswarm/accessor/accessors/web/hosts/node; node nodeHostInvoke -timeout 6000 test/auto/RampJSTestDisplay.js)

                    // If we don't increment the priority then either
                    // the Display or the TrainableTest fails to get
                    // inputs.
                    destinationAccessor.priority = ++myPriority;
                    // console.log('Assigned downstream priority to ' + destinationAccessor.accessorName + ' of ' + myPriority));

                    // In case there are further assigned priorities further downstream,
                    // update myPriority to the largest of those.
                    myPriority = this.assignImpliedPrioritiesDownstream(
                        destinationAccessor, cyclePriority);
                } else {
                    if (theirPriority > myPriority) {
                        // Priority is OK. Continue.
                        continue;
                    }
                    // Priority has to be adjusted.

                    // See comment above for why we increment myPriority.
                    destinationAccessor.priority = ++myPriority;
                    // console.log('Assigned downstream priority to ' + destinationAccessor.accessorName + ' of ' + (myPriority + 1));
                    myPriority = this.assignImpliedPrioritiesDownstream(
                        destinationAccessor, cyclePriority);
                }
            }
        }
    }
    return myPriority;
};

/** Assuming that the specified accessor has an assigned priority, follow its
 *  connections upstream and assign priorities to connected accessors.
 *  @param accessor The contained accessor with a priority.
 *  @param cyclePriority If we encounter an accessor with this priority, then
 *   there is a causality loop.
 */
Accessor.prototype.assignImpliedPrioritiesUpstream = function (accessor, cyclePriority) {
    var myPriority = accessor.priority;
    // To get repeatable priorities, iterate over inputs in order.
    for (var i = 0; i < accessor.inputList.length; i++) {
        var input = accessor.inputs[accessor.inputList[i]];
        if (input.source && typeof input.source !== 'string') {
            // There is a source accessor.
            var source = input.source.accessor;
            // There was a bug here where $PTII/ptolemy/actor/lib/jjs/test/auto/RampDisplay.xml
            // would fail because output was undefined.
            //var output = source.outputs[source.outputName];
            var output = source.outputs[input.source.outputName];
            if (typeof output === 'undefined') {
                throw new Error('In "' + source.accessorName + ', source.outputName was: "' + source.outputName + '", and source.outputs[source.outputName] is of type undefined? outputs:' + source.outputs.toString() + ' source:\n' + util.inspect(source));
            }
            // If the output is marked 'spontaneous' then we can ignore it.
            if (output.spontaneous) {
                continue;
            }
            var theirPriority = source.priority;
            if (theirPriority === cyclePriority) {
                throw new Error('Causality loop found including at least: ' +
                    accessor.accessorName);
            }
            if (theirPriority === null) {
                // Source has no previously assigned priority. Give it one,
                // and follow the implications.
                source.priority = myPriority - 1;
                // console.log('Assigned upstream priority to ' + accessors[i].accessorName + ' of ' + (myPriority - 1));
                this.assignImpliedPrioritiesUpstream(source, cyclePriority);
            } else {
                if (theirPriority < myPriority) {
                    // Priority is OK. Continue.
                    continue;
                }
                // Priority has to be adjusted.
                source.priority = myPriority - 1;
                // console.log('Assigned upstream priority to ' + accessors[i].accessorName + ' of ' + (myPriority - 1));
                this.assignImpliedPrioritiesUpstream(source, cyclePriority);
            }
        }
    }
};

/** Connect the specified inputs and outputs.
 *  There are four forms of this function:
 *
 *  1. this.connect(sourceAccessor, 'outputName', destinationAccessor, 'inputName');
 *  2. this.connect('myInputName', destinationAccessor, 'inputName');
 *  3. this.connect(sourceAccessor, 'outputName', 'myOutputName');
 *  4. this.connect('myInputName', 'myOutputName');
 *
 *  In all cases, this connects a data source to a destination.
 *  An input port of this accessor, with name 'myInputName', can be a source of data
 *  for a contained accessor or for an output port of this accessor, with name
 *  'myOutputName'.
 *
 *  This method appends a destination to the destination property of the input
 *  or output object in the inputs or outputs property of this accessor. The form
 *  of the destination is either a string (if the destination is an output
 *  of this accessor) or an object with two properties,
 *  **accessor** and **inputName**.
 *
 *  This method also sets a **source** property of the input or output that is
 *  the source of data on the connection. Again, that property is either a string
 *  name (to mean an input of the container accessor) or an object with two
 *  properties **accessor** and **outputName**.
 *
 *  @param a An accessor or a name.
 *  @param b An accessor or a name.
 *  @param c An accessor or a name.
 *  @param d A destination port name.
 */
Accessor.prototype.connect = function (a, b, c, d) {
    // Note that we could just use this instead of this.root because of the
    // prototype chain, but in a deep hierarchy, this will be more efficient.
    var thiz = this.root;
    if (typeof a === 'string') {
        // form 2 or 4.
        var myInput = thiz.inputs[a];
        if (!myInput) {
            throw new Error('connect(): No such input: ' + a);
        }
        if (!myInput.destinations) {
            myInput.destinations = [];
        }
        if (typeof b === 'string') {
            // form 4.
            if (!thiz.outputs[b]) {
                throw new Error('connect(): No such output: ' + b);
            }
            myInput.destinations.push(b);
            thiz.outputs[b].source = a;
        } else {
            // form 2.
            if (!b.inputs[c]) {
                throw new Error('connect(): Destination has no such input: ' + c);
            }
            myInput.destinations.push({
                'accessor': b,
                'inputName': c
            });
            b.inputs[c].source = a;
        }
    } else {
        // form 1 or 3.
        var myOutput = a.outputs[b];
        if (!myOutput) {
            throw new Error('connect(): Source has no such output: ' + b);
        }
        if (!myOutput.destinations) {
            myOutput.destinations = [];
        }
        if (typeof c === 'string') {
            // form 3.
            if (!thiz.outputs[c]) {
                throw new Error('connect(): No such output: ' + b);
            }
            myOutput.destinations.push(c);
            thiz.outputs[c].source = {
                'accessor': a,
                'outputName': b
            };
        } else {
            // form 1.
            if (!c.inputs[d]) {
                throw new Error('connect(): Destination has no such input: ' + d);
            }
            myOutput.destinations.push({
                'accessor': c,
                'inputName': d
            });
            c.inputs[d].source = {
                'accessor': a,
                'outputName': b
            };
        }
    }
};


/** Disconnects the specified inputs and outputs.
 *  This function is buit from connect() function. Therefore, it uses the same
 *  four forms, however it is used in order to produce the opposite effect:
 *  1. this.disconnect(sourceAccessor, 'outputName', destinationAccessor, 'inputName');
 *  2. this.disconnect('myInputName', destinationAccessor, 'inputName');
 *  3. this.disconnect(sourceAccessor, 'outputName', 'myOutputName');
 *  4. this.disconnect('myInputName', 'myOutputName');
 *
 *  In all cases, this disconnects a data source from a destination.
 *  An input port of this accessor, with name 'myInputName', can be a source of data
 *  for a contained accessor or for an output port of this accessor, with name
 *  'myOutputName'.
 *
 *  This method removes an already established connection. The form
 *  of the destination is either a string (if the destination is an output
 *  of this accessor) or an object with two properties,
 *  **accessor** and **inputName**.
 *
 *  This method also removes from previously constructed *source* property of the input
 *  or output the source of data on the connection. Again, that property is either a string
 *  name (to mean an input of the container accessor) or an object with two
 *  properties *accessor* and *outputName*.
 *
 *  @param a An accessor or a name.
 *  @param b An accessor or a name.
 *  @param c An accessor or a name.
 *  @param d A destination port name.
 */
Accessor.prototype.disconnect = function (a, b, c, d) {
    // Note that we could just use this instead of this.root because of the
    // prototype chain, but in a deep hierarchy, this will be more efficient.
    var thiz = this.root;
    if (typeof a === 'string') {
        // form 2 or 4.
        var myInput = thiz.inputs[a];
        if (!myInput) {
            throw new Error('disconnect(): No such input: ' + a);
        }
        if (typeof b === 'string') {
            // form 4.
            if (!thiz.outputs[b]) {
                throw new Error('disconnect(): No such output: ' + b);
            }
            myInput.destinations.splice(myInput.destinations.indexOf(b), 1);
            thiz.outputs[b].source = null;
        } else {
            // form 2.
            if (!b.inputs[c]) {
                throw new Error('disconnect(): Destination has no such input: ' + c);
            }
            myInput.destinations.splice(myInput.destinations.indexOf({
                'accessor': b,
                'inputName': c
            }), 1);
            b.inputs[c].source = null;
        }
    } else {
        // form 1 or 3.
        var myOutput = a.outputs[b];
        if (!myOutput) {
            throw new Error('disconnect(): Source has no such output: ' + b);
        }
        if (typeof c === 'string') {
            // form 3.
            if (!thiz.outputs[c]) {
                throw new Error('disconnect(): No such output: ' + b);
            }
            myOutput.destinations.splice(myOutput.destinations.indexOf(c), 1);
            thiz.outputs[c].source = null;
        } else {
            // form 1.
            if (!c.inputs[d]) {
                throw new Error('disconnect(): Destination has no such input: ' + d);
            }
            myOutput.destinations.splice(myOutput.destinations.indexOf({
                'accessor': c,
                'inputName': d
            }), 1);
            c.inputs[d].source = null;
        }
    }
};

/** Report an error using console.error().
 *  This should be used by an accessor to report non-fatal errors.
 *  For fatal errors, invoke "throw new Error('A Description');"
 *
 *  @param message The error message.
 */
Accessor.prototype.error = function (message) {
    console.error(message);
    // Print a stack trace to the console.
    console.error('------------------------- error stack trace:');
    var e = new Error('dummy');
    var stack = e.stack.replace(/^[^\(]+?[\n$]/gm, '')
        .replace(/^\s+at\s+/gm, '')
        .replace(/^Object.<anonymous>\s*\(/gm, '{anonymous}()@')
        .split('\n');
    console.error(stack);
    console.error('-------------------------');
};

/** Extend the specified accessor, inheriting its interface as defined
 *  in its setup() function and making its exports object the prototype
 *  of the exports object of this accessor.
 *  This will throw an exception if no getAccessorCode() function
 *  has been specified.
 *  @param accessorClass Fully qualified accessor class name, e.g. 'net/REST'.
 */
Accessor.prototype.extend = function (accessorClass) {
    // NOTE: This function should not need to be overriden by any host.
    if (!this.getAccessorCode) {
        throw new Error('extend() is not supported by this swarmlet host.');
    }

    var baseName = this.accessorName + '_' + accessorClass;

    // Create an instance of the accessor this is extending.
    var extendedInstance = instantiateAccessor(
        baseName, accessorClass, this.getAccessorCode, this.bindings, this, null);
};

/** Default implementation of this.get(), which reads the current value of the input
 *  provided by provideInput(), or the default value if none has been provided,
 *  or null if neither has been provided.
 *  @param name The name of the input.
 */
Accessor.prototype.get = function (name) {
    // Note that we could just use this instead of this.root because of the
    // prototype chain, but in a deep hierarchy, this will be more efficient.
    var thiz = this.root;
    var input = thiz.inputs[name];

    if (!input) {
        // Tolerate using this.get() to retrieve a parameter instead of input,
        // since names are required to be unique anyway. This ensure backward
        // compatibility with earlier models for the Ptolemy host, which used
        // this.get() for both inputs and parameters.
        input = thiz.parameters[name];
        if (!input) {
            throw new Error('get(name): No input named ' + name);
        }
    }
    var value;
    var currentValue = input.currentValue;
    if (typeof currentValue !== 'undefined' && currentValue !== null) {
        // If provideInput() has been called, return that value.
        value = input.currentValue;
    } else {
        // Note that if both currentValue and value are null or undefined,
        // then the correct response is null.
        value = input.value;
        if (typeof value === 'undefined') {
            value = null;
        }
    }
    // If necessary, convert the value to the match the type.
    value = convertType(value, input, name);
    return value;
};

/** Default implementation of this.getParameter(), which reads the current value of the
 *  parameter provided by this.setParameter(), or the default value if none has been provided,
 *  or null if neither has been provided.
 *  @param name The name of the parameter.
 */
Accessor.prototype.getParameter = function (name) {
    var parameter = this.parameters[name];
    if (!parameter) {
        throw new Error('getParameter(name): No parameter named ' + name);
    }
    // If this.setParameter() has been called, return that value.
    if (parameter.currentValue) {
        return parameter.currentValue;
    }
    // If necessary, convert the value to the match the type.
    var value = parameter.value;
    value = convertType(value, parameter, name);
    return value;
};

/** Default implement of the this.getResource() function, which throws an exception stating
 *  that getResource is not supported.
 */
Accessor.prototype.getResource = function () {
    throw new Error('This swarmlet host does not support this.getResource().');
};

/** Default implement of the httpRequest() function, which throws an exception stating
 *  that httpRequest is not supported.
 *  Note that this function is deprecated in the Accessor Specification version 1,
 *  but we include it here anyway.
 */
Accessor.prototype.httpRequest = function () {
    throw new Error('This swarmlet host does not support httpRequest().');
};

/** Implement the specified accessor interface, inheriting its inputs, outputs,
 *  and parameters as defined in its setup() function.
 *  This will throw an exception if no getAccessorCode() function
 *  has not been specified.
 *  @param accessorClass Fully qualified accessor class name, e.g. 'net/REST'.
 */
Accessor.prototype.implement = function (accessorClass) {
    // NOTE: This function should not need to be overriden by any host.
    if (!this.getAccessorCode) {
        throw new Error('implement() is not supported by this swarmlet host.');
    }

    var interfaceName = this.accessorName + '_' + accessorClass;

    // Create an instance of the accessor this is implementing.
    var extendedInstance = instantiateAccessor(
        interfaceName, accessorClass, this.getAccessorCode, this.bindings, null, this);
};

/** Default implementation of the function to define an accessor input.
 *  Accessors that override this should probably invoke this default explicitly
 *  by referencing the prototype.
 *  @param name The name of the input.
 *  @param options The options for the input.
 */
Accessor.prototype.input = function (name, options) {
    if (!this || !this.inputList) {
        throw new Error('Function input() is being called without "this" being defined. ' +
            'Perhaps use "this.input(...)" instead of "input(...)".');
    }
    // The input may have been previously defined in a base accessor.
    pushIfNotPresent(name, this.inputList);
    this.inputs[name] = mergeObjects(this.inputs[name], options);
};

/** Instantiate the specified accessor as a contained accessor.
 *  This will throw an exception if no getAccessorCode() function
 *  has been specified.
 *  @param instanceName A name to give to this instance, which will be prepended
 *   with the container name, separated by a period. If the container already
 *   contains an object with that name, then an index will be appended to the name,
 *   starting with 2, to ensure that the name is unique in the container.
 *  @param accessorClass Fully qualified accessor class name, e.g. 'net/REST'.
 */
Accessor.prototype.instantiate = function (instanceName, accessorClass) {
    if (!this.getAccessorCode) {
        throw new Error('instantiate() is not supported by this swarmlet host.');
    }
    // For functions that access ports, etc., we want the default implementation
    // when instantiating the contained accessor.
    var insideBindings = {
        'error': this.error,
        'httpRequest': this.httpRequest,
        'readURL': this.readURL,
        'require': this.require,
    };
    // If 'this' defines setTimeout(), etc., use that instead of current definitions.
    if (this && this.clearInterval) {
        insideBindings.clearInterval = this.clearInterval;
    }
    if (this && this.clearTimeout) {
        insideBindings.clearTimeout = this.clearTimeout;
    }
    if (this && this.setInterval) {
        insideBindings.setInterval = this.setInterval;
    }
    if (this && this.setTimeout) {
        insideBindings.setTimeout = this.setTimeout;
    }

    // FIXME: Do we need to ensure that 'actor' or 'accessor' is bound here?
    instanceName = this.accessorName + '.' + instanceName;
    instanceName = uniqueName(instanceName, this);
    var containedInstance = instantiateAccessor(
        instanceName, accessorClass, this.getAccessorCode, insideBindings);
    containedInstance.container = this;
    this.containedAccessors.push(containedInstance);
    return containedInstance;
};

/** Return the latest value produced on this output, or null if no
 *  output has been produced.
 *  @param name The name of the output.
 *  @return The latest value produced on this output.
 */
Accessor.prototype.latestOutput = function (name) {
    if (!this.outputs[name]) {
        throw new Error('lastestOutput(): No output named ' + name);
    }
    // console.log('Retrieving latest output from ' + name + ': ' + this.outputs[name].latestOutput);
    return this.outputs[name].latestOutput;
};

/** Default module identifier for accessors.
 *  CommonJS specification requires a 'module' object with an 'id' property
 *  and an optional 'uri' property. The spec says that module.id should be
 *  a valid argument to require(). Here, we are just given the JavaScript
 *  code, so we don't have any information about where it came from.
 *  Hence, we set a default id to 'unspecified', with the expectation that the
 *  code passed in will override that, and possibly the uri property.
 */
Accessor.prototype.module = {
    'id': 'unspecified'
};

/** Default implementation of the function to define a mutableAccessor.
 *  If this is a mutableAccessor, then add the corresponding properties.
 *  @param value The value, which should be 'true'
 */
Accessor.prototype.mutable = function (value) {
    if (value === 'true') {
        this.mutable = true;

        ///////////////////////////////////////////////////////////////////
        //// Set up the mutableAccessor properties.
        /*if (extendedBy || implementedBy) {
                throw new Error('An extended or implementing Accessor cannot be a mutableAccessor.');
        }*/

        ////////////////////////////////////////////////////////////////////
        //// For a mutableAccessor, define additional attributes

        this.reifyingAccessorsList = [];

        this.inputsMap = new Map();
        this.outputsMap = new Map();
        this.parametersMap = new Map();

        this.status = 'instantiated';

        ////////////////////////////////////////////////////////////////////
        //// Support for additional monitoring information

        // Monitoring reify events
        this.monitor.reify = {
            'count': 0,
            'firstOccurrenceDate': {},
            'latestOccurrenceDate': {}
        };

    }

};

/** Define an accessor output.
 *  @param name The name of the output.
 *  @param options The options.
 */
Accessor.prototype.output = function (name, options) {
    if (!this || !this.outputList) {
        throw new Error('Function output() is being called without "this" being defined. ' +
            'Perhaps use "this.output(...)" instead of "output(...)".');
    }
    // The output may have been previously defined in a base accessor.
    pushIfNotPresent(name, this.outputList);
    this.outputs[name] = mergeObjects(this.outputs[name], options);
};

/** Define an accessor parameter.
 *  @param name The name of the parameter.
 *  @param options The options.
 */
Accessor.prototype.parameter = function (name, options) {
    if (!this || !this.parameterList) {
        throw new Error('Function parameter() is being called without "this" being defined. ' +
            'Perhaps use "this.parameter(...)" instead of "parameter(...)".');
    }
    // The parameter may have been previously defined in a base accessor.
    pushIfNotPresent(name, this.parameterList);
    this.parameters[name] = mergeObjects(this.parameters[name], options);
};

/** Set an input of this accessor to the specified value.
 *  If this accessor has a container, then schedule an event using scheduleEvent()
 *  so that a reaction of the container is requested and that reaction triggers
 *  a reaction of this accessor to respond to the input.
 *  This function will perform conversions to the destination port type, if possible.
 *  For example, if a number is expected, but a string is provided, then it will
 *  attempt to parse the string to create a number.
 *  @param name The name of the input to set.
 *  @param value The value to set the input to.
 */
Accessor.prototype.provideInput = function (name, value) {
    var input = this.inputs[name];
    if (!input) {
        throw new Error('provideInput(): Accessor has no input named ' + name);
    }

    value = convertType(value, input, name);
    input.currentValue = value;

    // Mark this input as requiring invocation of an input handler.
    // But be careful: If the value is null and the port has no default
    // value, then this is being called to indicate that there is _no_
    // input. The accessor specification says that if there is a default
    // value, then sending null to the port _will_ trigger an event handler.
    if (value !== null || (typeof input.value !== 'undefined' && input.value !== null)) {
        input.pendingHandler = true;
        // If there is a container accessor, then put this accessor in its
        // event queue for handling in its fire() function.
        if (this.container) {
            this.container.scheduleEvent(this);
        }

        // If the input is connected on the inside, then provide the same input
        // to the destination(s).
        if (input.destinations) {
            for (var i = 0; i < input.destinations.length; i++) {
                var destination = input.destinations[i];
                if (typeof destination === 'string') {
                    // The destination is output port of this accessor.
                    this.send(destination, value);
                } else {
                    // The destination is an input port of a contained accessor.
                    destination.accessor.provideInput(destination.inputName, value);
                }
            }
        }
    }
};

/** Invoke any registered handlers for all inputs or for a specified input.
 *  Also invoke any handlers that have been registered to respond to any input,
 *  if there are any such handlers.
 *  If no input name is given, or the name is null, then invoke handlers for
 *  all inputs that have been provided with input value using provideInput()
 *  since the last time input handlers were invoked.
 *  Also, if any contained accessors have events scheduled using scheduleEvent(),
 *  then invoke their react() functions in priority order.
 *  Also invoke the fire function of the accessor, if one has been defined.
 *  If a handler throws an exception, then remove it from the registered
 *  handlers before rethrowing the exception.
 *  @param name The name of the input.
 */
Accessor.prototype.react = function (name) {
    // console.log(this.accessorName + ": ================== react(" + name + ")");

    this.emit('reactStart');
    // Update monitoring information
    this.updateMonitoringInformation('reactStart');

    var thiz = this.root;

    // Mark that there is no pending reaction so that if this reaction has
    // actions that require further reactions, those will be scheduled.
    thiz.reactRequestedAlready = false;

    // To avoid code duplication, define a local function.
    var invokeSpecificHandler = function (name) {

        if (thiz.inputHandlers[name] && thiz.inputHandlers[name].length > 0) {
            // When calling stop, there is a chance that "removed[0].react()" below
            // will fail with 'TypeError: Cannot read property 'length' of undefined',
            // so we check to see if thiz.inputHandlers[name] is defined.
            for (var i = 0; thiz.inputHandlers[name] && i < thiz.inputHandlers[name].length; i++) {
                if (typeof thiz.inputHandlers[name][i] === 'function') {
                    // Input handlers functions are bound to the exports object.
                    try {
                        thiz.inputHandlers[name][i]();
                    } catch (exception) {
                        // Remove the input handler.
                        if (thiz.inputHandlers && thiz.inputHandlers[name]) {
                            thiz.removeInputHandler(
                                thiz.inputHandlers[name][i].handle);
                        }
                        // Throw an Error here instead of calling error() so that
                        // if TrainableTest.wrapup() throws an Error because
                        // the input does not match the training data, then we
                        // don't ignore the error in commonHost.error().

                        // If the exception was thrown because of
                        // Java, we should get the Java stacktrace.
                        var stacktrace = exception.stack;
                        if (typeof stacktrace === 'undefined' && accessorHost === accessorHostsEnum.CAPECODE || accessorHost === accessorHostsEnum.NASHORN) {
                            try {
                                // This code is Cape Code Host-specific because it uses Java.
                                var StringWriter = java.io.StringWriter,
                                    PrintWriter = java.io.PrintWriter;
                                var stringWriter = new StringWriter();
                                var printWriter = new PrintWriter(stringWriter);
                                exception.printStackTrace(printWriter);
                                stacktrace = "\n" + stringWriter.toString();
                            } catch (exception2) {
                                stacktrace = "undefined and the exception was not a Java exception so exception.printStackTrace() failed with: " + exception2;
                            }
                        }

                        throw new Error('commonHost.js, react(), invoking a specific handler for \"' +
                            name + '\": Exception occurred in input handler,' +
                            ' which has now has been removed.  Exception was: ' +
                            exception +
                            ' Stacktrace was: ' + stacktrace);
                    }
                }
            }
        }
    };

    if (name) {
        // Handling a specific input.
        //console.log("commonHost.js: react(" + name + "): Handling a specific input.");
        invokeSpecificHandler(name);
    } else {
        // No specific input has been given.
        // Invoke pending inputHandlers.  An accessor might send to its own
        // inputs, so repeat until there are no more pending handlers.
        //console.log("commonHost.js: react(" + name + "): no specific input has been given.");
        var moreInputsPossiblyAvailable = true;
        while (moreInputsPossiblyAvailable) {
            moreInputsPossiblyAvailable = false;
            for (var i = 0; i < thiz.inputList.length; i++) {
                name = thiz.inputList[i];
                if (thiz.inputs[name].pendingHandler) {
                    thiz.inputs[name].pendingHandler = false;
                    moreInputsPossiblyAvailable = true;
                    invokeSpecificHandler(name);
                }
            }
        }
    }
    // Next, invoke handlers registered to handle any input.
    //console.log("commonHost.js: react(" + name + "): invoke handlers registered to handle any input");
    if (thiz.anyInputHandlers.length > 0) {
        for (var j = 0; j < thiz.anyInputHandlers.length; j++) {
            if (typeof thiz.anyInputHandlers[j] === 'function') {
                // Call input handlers in the context of the exports object.
                try {
                    thiz.anyInputHandlers[j]();
                } catch (exception) {
                    // Remove the input handler.
                    thiz.removeInputHandler(
                        thiz.anyInputHandlers[j].handle);
                    thiz.error('commonHost.js, react() invoking handlers registered to handle any input: Exception occurred in input handler,' +
                        ' which has now has been removed.  Exception was: ' +
                        exception +
                        ' Stacktrace was: ' + exception.stack);
                }
            }
        }
    }

    // Next, invoke react() on any contained accessors.
    if (thiz.containedAccessors && thiz.containedAccessors.length > 0) {
        //console.log('commonHost.js react(' + name + '): Composite is reacting with ' + thiz.eventQueue.length + ' events.');
        while (thiz.eventQueue && thiz.eventQueue.length > 0) {
            // Remove from the event queue the first accessor, which will now react.
            // It may add itself back in, if it sends to its own input. But in that
            // case, it should fire again immediately, so that is correct.
            var removed = thiz.eventQueue.splice(0, 1);
            removed[0].react();
        }
    }

    // Next, invoke the fire() function.
    if (typeof this.exports.fire === 'function') {
        //console.log('commonHost.js react(' + name + '): invoking fire');
        this.exports.fire.call(this);
    }

    // Update monitoring information
    this.updateMonitoringInformation('reactEnd');

    this.emit('reactEnd');
};

/** Default implement of the readURL function, which throws an exception stating
 *  that readURL is not supported.
 *  Note that this function is deprecated in the Accessor Specification version 1,
 *  but we include it here anyway.
 */
Accessor.prototype.readURL = function () {
    throw new Error('This swarmlet host does not support readURL().');
};

/** Default implementation of the function to define an accessor realized feature.
 *  Accessors that override this should probably invoke this default explicitly
 *  by referencing the prototype.
 *  @param name The name of the realized feature.
 *  @param options The options for the realized feature.
 */
Accessor.prototype.realize = function (name, options) {
    if (!this || !this.realizesList) {
        throw new Error('Function realize() is being called without "this" being defined. ' +
            'Perhaps use "this.realize(...)" instead of "realize(...)".');
    }
    // The input may have been previously defined in a base accessor.
    pushIfNotPresent(name, this.realizesList);
    this.realizes[name] = mergeObjects(this.realizes[name], options);
};

/** Evaluates if the 'mutableAccessor' (this) is reifiable by the 'accessor' given
 *  as parameter, or, in other words, if the accessor's interface refines the
 *  mutableAccesor's interface.
 *  The accessor should be a top level one.
 *  This is a type refinement, hence the function returns true if
 *   *  the set of inputs of 'accessor' are included in the set of inputs of
 *           'mutableAccessor'
 *   *  and the set of outputs of 'mutableAccessor' are included in the set of
 *      outputs of 'accessor'.
 *
 *  For an inclusion to hold, the following conditions are checked:
 *   *  Same port name
 *   *  If the ports contain an attribute called 'type', then they need to be
 *      checked for equality.
 *  TODO: Type checking can be improved by using an ontology.
 *
 *  An alternative for dealing with the accessor parameters is to to consider
 *  them, from the mutableAccessor view, as inputs. If the parameter 'strict' is
 *  passed, then the function looks for mapping the non mapped mutableAcessor
 *  inputs to the accessor parameters.
 *
 *  The following objects are constructed: inputsMap, outputsMap and parametersMap.
 *
 *  In addition, if the result is true (i.e. the accessor can be used to reify
 *  the mutableAccessor), then an object containing: (i) the accessor, (ii) the mappings
 *  and (iii) the options, is pushed in the array of reifyingAccessorsList.
 *
 *  @param accessor An instantiated Accessor object
 *  @param options An optional parameter that describes additional information about the
 *   accessor, such as its location, IPAddress, ...
 *   FIXME: options is to be detailed!!!
 *  @param strict An optional parameter that selects if the accessor parameters can be
 *   mapped to available mutableAccessor inputs.
 */
Accessor.prototype.reifiableBy = function (accessor, options, strict) {
    // Note that we could just use this instead of this.root because of the
    // prototype chain, but in a deep hierarchy, this will be more efficient.
    var thiz = this.root, i;

    // Check that this is a mutableAccessor
    if (!thiz.mutable) {
        console.log('Only mutableAccessors can be tested for reification.');
        return false;
    }

    if (!accessor) {
        console.log('reifiableBy(): the paremeter should be an accessor.');
        return false;
    }

    // Check if the accessor if a top level one
    if (accessor.container) {
        console.log('reifiableBy(): the parameter should be a top level accessor.');
        return false;
    }

    // Check if the mutableAccessor and accessor realizes similar features
    if (thiz.realizesList.length === 0 || accessor.realizesList.length === 0) {
        console.log('reifiableBy(): Realized features are needed to check reification.');
        // For compatibility reasons with the previous accessors, the function
        // will continue executing.
        // return false;
    }
    // Check is realized features are compatible
    // As a first step, this is reduced to equality checking
    // The next step is to perform this tak using ontologies
    for (i = 0; i < thiz.realizesList.length; i++) {
        var myRealizesInList = thiz.realizesList[i];
        var myRealizes = thiz.realizes[myRealizesInList];

        var accRealizesInList = accessor.realizesList[accessor.realizesList.indexOf(myRealizesInList)];
        var accRealizes = accessor.realizes[accRealizesInList];

        // Check the realized feature name
        if (!accRealizesInList)
            return false;

        // FIXME: To be further improved
    }

    // Map objects for inputs, outputs and possibly parameters between the mutableAccessor
    // and the accessor
    var inputsMap = new Map();
    var outputsMap = new Map();
    var parametersMap = new Map();

    var myInputInList, myInput, accInputInList, accInput;
    // Look for mapping the accessor inputs to the mutableAccesssor inputs
    for (i = 0; i < accessor.inputList.length; i++) {
        accInputInList = accessor.inputList[i];
        accInput = accessor.inputs[accInputInList];

        myInputInList = thiz.inputList[thiz.inputList.indexOf(accInputInList)];
        myInput = thiz.inputs[myInputInList];

        // Check the input name
        if (!myInputInList)
            return false;

        // Then check the type, if such attribute exists
        if (accInput.type !== myInput.type)
            return false;

        inputsMap.set(myInputInList, accInputInList);
    }

    // If 'strict' is not passed, proceed with mapping the accessor parameters
    for (i = 0; i < thiz.inputList.length; i++) {
        myInputInList = thiz.inputList[i];
        myInput = thiz.inputs[myInputInList];

        if (inputsMap.has(myInputInList))
            continue;

        // If the input is not already mapped to an accessor input,
        // then check if it can be associated to a parameter
        var accParameterInList = accessor.parameterList[accessor.parameterList.indexOf(myInputInList)];
        var accParameter = accessor.parameters[accParameterInList];

        if (accParameterInList)
        // Then check the type, if such attribute exists
            if (myInput.type === accParameter.type)
            parametersMap.set(myInputInList, accParameterInList);
    }

    // Look for mapping the accessor inputs to the mutableAccesssor inputs
    for (i = 0; i < thiz.outputList.length; i++) {
        var myOutputInList = thiz.outputList[i];
        var myOutput = thiz.outputs[myOutputInList];

        var accOutputInList = accessor.outputList[accessor.outputList.indexOf(myOutputInList)];
        var accOutput = accessor.outputs[accOutputInList];

        // Check the output name
        if (!accOutputInList)
            return false;

        // Then check the type, if such attribute exists
        if (accOutput.type !== myOutput.type)
            return false;

        outputsMap.set(accOutputInList, myOutputInList);
    }

    // Create the object to be saved in the mutableAccessor reifyingAccessorsList
    var reifying = {};
    reifying.accessor = accessor;
    reifying.inputsMap = inputsMap;
    reifying.outputsMap = outputsMap;
    reifying.parametersMap = parametersMap;
    reifying.options = options;

    thiz.reifyingAccessorsList.push(reifying);

    return true;
};

/** If the accessor's interface refines the mutableAccessor interface, then the inputs
 *  and outputs Map Objects are used to establish the connections between both objects
 *  and between the inputs and outputs.
 *  *  If an accessor is passed as a parameter, then it is first checked if it is
 *     contained in the array of reifyingAccessorsList. If it is not, then recall
 *     reifiableBy function.
 *  *  If no parameter is passed, then reify the first element of
 *     reifyingAccessorsList array. Otherwise, return false.
 *
 *  The Map objects inputsMap and outputsMap are constructed.
 *
 *  @param accessor an instantiated Accessor object
 */
Accessor.prototype.reify = function (accessor) {
    // Note that we could just use this instead of this.root because of the
    // prototype chain, but in a deep hierarchy, this will be more efficient.
    var thiz = this.root;

    // Check that this is a mutableAccessor
    if (!thiz.mutable) {
        console.log('Only mutableAccessors can be reified.');
        return false;
    }

    // Check if the mutableAccessor is already reified or not
    if (thiz.containedAccessors.length == 1) {
        console.log('The mutableAccessor is already reified, please remove the reification before substitution');
        return false;
    }

    var reifying = {};
    reifying.accessor = accessor;
    var _reifyingIndexInList = -1;

    // Check if an accessor is passed
    if (!accessor) {
        if (thiz.reifyingAccessorsList.length === 0) {
            console.log('reify(): no found accessor to reify.');
            return false;
        }

        // If no accessor is passed and that there is already concretizable ones, pick up
        // the first element of the array
        _reifyingIndexInList = 0;

    } else {
        // When an accessor is passed, check that it was tested for refinement.

        thiz.reifyingAccessorsList.forEach(function (element) {
            if (element.accessor === accessor) {
                _reifyingIndexInList = thiz.reifyingAccessorsList.indexOf(element);
            }
        });

        if (_reifyingIndexInList == -1) {
            console.log('reify(): the passed accessor has not been tested for abtraction. Please call reifiableBy.');
            return false;
        }
    }

    // Retreive the element to reify from the mutableAccessor's reifyingAccessorsList attribute
    reifying = thiz.reifyingAccessorsList[_reifyingIndexInList];
    thiz.reifyingAccessorsList.splice(_reifyingIndexInList, 1);

    // Establish that the mutableAccessor is considered as a composite accessor with only one
    // element
    thiz.containedAccessors.push(reifying.accessor);
    reifying.accessor.container = thiz;

    thiz.inputsMap = reifying.inputsMap;
    thiz.outputsMap = reifying.outputsMap;
    thiz.parametersMap = reifying.parametersMap;

    // Establish the connections
    thiz.inputsMap.forEach(function (key, value) {
        thiz.connect(key, reifying.accessor, value);
    });

    thiz.outputsMap.forEach(function (key, value) {
        thiz.connect(reifying.accessor, key, value);
    });

    // Update the mutableAccessor status and history
    thiz.status = 'reified';
    thiz.updateMonitoringInformation('reify');

    return true;
};

/** Removes the reification of the mutableAccessor. Therefore, the connections between both objects
 *  and between their corresponding inputs and outputs are removed.
 */
Accessor.prototype.removeReification = function () {
    // Note that we could just use this instead of this.root because of the
    // prototype chain, but in a deep hierarchy, this will be more efficient.
    var thiz = this.root;

    // Check if there is already a reification
    if (thiz.containedAccessors.length != 1) {
        console.log('removeReification(): No contained accessor.');
        return false;
    }

    var acc = thiz.containedAccessors.pop();
    if (acc.container) acc.container = null;

    thiz.inputsMap.forEach(function (key, value) {
        thiz.disconnect(key, acc, value);
    });

    thiz.outputsMap.forEach(function (key, value) {
        thiz.disconnect(acc, key, value);
    });


    thiz.inputsMap.clear();
    thiz.outputsMap.clear();
    if (thiz.parametersMap) thiz.parametersMap.clear();

    // Update the mutableAccessor status and history
    thiz.status = 'instantiated';

    return true;

};

/** Remove the input handler with the specified handle, if it exists.
 *  @param handle The handle.
 *  @see #addInputHandler()
 */
Accessor.prototype.removeInputHandler = function (handle) {
    var thiz = this.root;
    var handler = thiz.inputHandlersIndex[handle];
    if (handler) {
        if (handler.name) {
            if (thiz.inputHandlers[handler.name] &&
                thiz.inputHandlers[handler.name][handler.index]) {
                thiz.inputHandlers[handler.name][handler.index] = null;
            }
        } else {
            // Handler is set up to handle any input.
            if (thiz.anyInputHandlers[handler.index]) {
                thiz.anyInputHandlers[handler.index] = null;
            }
        }
        thiz.inputHandlersIndex[handle] = null;
    }
};

/** Default implement of the require function, which throws an exception stating
 *  that require is not supported.
 */
Accessor.prototype.require = function () {
    // Print a stack trace.
    var e = new Error('This swarmlet host does not support require().');
    var stack = e.stack.replace(/^[^\(]+?[\n$]/gm, '')
        .replace(/^\s+at\s+/gm, '')
        .replace(/^Object.<anonymous>\s*\(/gm, '{anonymous}()@')
        .split('\n');
    console.log(stack);
    throw e;
};

/** Schedule a reaction of the specified contained accessor,
 *  unless such a reaction has already been scheduled and has not
 *  yet occurred.
 *  This puts the accessor onto the event queue in priority order.
 *  This assumes that priorities are unique to each accessor.
 *
 *  As a side effect, this function schedules a reaction of this
 *  container accessor using setTimeout with timeout 0 unless
 *  there is already such a pending reaction request.
 *
 *  @param accessor The accessor.
 */
Accessor.prototype.scheduleEvent = function (accessor) {
    var thiz = this.root;
    var queue = thiz.eventQueue;

    // If we have not already requested a reaction for this container
    // accessor, do so now.
    // If there is a container accessor, then put this accessor in its
    // event queue for handling in its react() function.
    if (thiz.container) {
        thiz.container.scheduleEvent(this);
    } else {
        // The container has no container, so request a reaction if one
        // has not already been requested.
        if (!thiz.reactRequestedAlready) {
            thiz.reactRequestedAlready = true;
            setTimeout(function () {
                thiz.react();
            }, 0);
        }
    }

    // In the Nashorn host, queue can be undefined.
    if (typeof queue === 'undefined' || !queue || queue.length === 0) {
        // Use a simple array as an event queue because almost all
        // sorted insertions will be at the end, and all extractions
        // will be at the beginning.
        thiz.eventQueue = [accessor];
        return;
    }
    // There are already items in the event queue.
    var myPriority = accessor.priority;
    if (typeof myPriority !== 'number') {
        throw new Error('Accessor does not have a priority: ' +
            accessor.accessorName +
            '. Perhaps initialize() is overridden?');
    }
    // Recall that a higher priority number means a lower priority.
    var theirPriority = queue[queue.length - 1].priority;
    if (myPriority > theirPriority) {
        // Simple case. Append to the end of the queue.
        queue.push(accessor);
        return;
    }
    if (myPriority == theirPriority) {
        // Already on the queue.
        return;
    }
    // More complicated case. Insert into the queue.
    // Here we just search from the end.
    // This is not efficient for random access, but these insertions are
    // expected to occur in priority order anyway.
    // Insertions are likely to be near the end.
    for (var i = queue.length - 2; i >= 0; i--) {
        theirPriority = queue[i].priority;
        if (myPriority > theirPriority) {
            // Insert at location i+1, removing 0 elements.
            queue.splice(i + 1, 0, accessor);
            return;
        }
        if (myPriority == theirPriority) {
            // Already on the queue.
            return;
        }
    }
    // Final case: My priority is less than all in the queue.
    queue.splice(0, 0, accessor);
};

/** Send via an output. This default implementation invokes provideInput() on any
 *  connected inputs if there are any. It also records the output for retrieval
 *  by latestOutput().
 *  @param name The output name.
 *  @param value The output value.
 */
Accessor.prototype.send = function (name, value) {
    var thiz = this.root;
    var output = thiz.outputs[name];
    if (!output) {
        // May be sending to my own input.
        var input = thiz.inputs[name];
        if (!input) {
            throw new Error('send(name, value): No output or input named ' + name);
        }
        // Make the input available in the _next_ reaction.
        setTimeout(function () {
            thiz.provideInput(name, value);
            // If this accessor has a container, then provideInput()
            // above will take care of scheduling a future reaction.
            // However, if it has no container, then no such reaction
            // will be requested. Request that reaction here.
            if (!thiz.container) {
                if (!thiz.reactRequestedAlready) {
                    thiz.reactRequestedAlready = true;
                    setTimeout(function () {
                        thiz.react();
                    }, 0);
                }
            }
        }, 0);
        return;
    }
    // If necessary, convert the value to the match the type.
    value = convertType(value, output, name);

    output.latestOutput = value;
    this.emit('output', name, value);
    // console.log('Sending output through ' + name + ': ' + value);
    if (output.destinations && output.destinations.length > 0) {
        for (var i = 0; i < output.destinations.length; i++) {
            var destination = output.destinations[i];
            if (typeof destination === 'string') {
                // The destination is output port of this accessor.
                if (thiz.container) {
                    thiz.container.send(destination, value);
                } else {
                    // If no other implementation of this.send() has been provided and
                    // there is no container, this used to produce to standard output.
                    // But this is not a good idea, because hosts should invoke this
                    // superclass function in their own this.send(), and this will produce
                    // a lot of noise on the console.
                    // console.log('Output named "' + name + '" produced: ' + value);
                }
            } else {
                // The destination is an input port of a contained accessor.
                destination.accessor.provideInput(destination.inputName, value);
            }
        }
    } else {
        // If no other implementation of this.send() has been provided and
        // there are no destinations, this used to produce to standard output.
        // But this is not a good idea, because hosts should invoke this
        // superclass function in their own this.send(), and this will produce
        // a lot of noise on the console.
        // console.log('Output named "' + name + '" produced: ' + value);
    }
};

/** Set the default value of an input. Note that unlike
 *  using this.send(), no input handler will be triggered.
 *  Also, unlike this.send(), the provided value will be persistent,
 *  in that once it is set, the host will store the new value along with the model.
 *  @param name The input name (a string).
 *  @param value The value to set.
 */
Accessor.prototype.setDefault = function (name, value) {
    if (typeof name !== 'string') {
        throw new Error('input argument is required to be a string. Got: ' + (typeof name));
    }
    var input = this.inputs[name];
    if (!input) {
        throw new Error('setDefault(): Accessor has no input named ' + name);
    }
    value = convertType(value, input, name);
    input.value = value;
};

/** When an accessor extends another accessor, this function can be used to set the default
 *  input values of the extended instance.
 *  This function is similar to this.input() but should not create a new one. It rises an
 *  error if the given input name does not already exist.
 *  It is important to note that unlike using this.setDefault, the value (second argument)
 *  is assigned as a default object value (using mergeObjects function rather than the
 *  attribute 'value'). And just like this.setDefault(), the new object value will be
 *  persistent.
 *
 *  This function is to be used to set
 *  @param name The input name (a string).
 *  @param value The value of the input object to set.
 */
Accessor.prototype.setDefaultInput = function (name, value) {
    if (typeof name !== 'string') {
        throw new Error('Input argument is required to be a string. Got: ' + (typeof name));
    }
    var input = this.inputs[name];
    if (!input) {
        throw new Error('setDefaultInput(): Accessor has no input named ' + name);
    }
    this.inputs[name] = mergeObjects(this.inputs[name], value);
};

/** Set a parameter of the specified accessor to the specified value.
 *  @param name The name of the parameter to set.
 *  @param value The value to set the parameter to.
 */
Accessor.prototype.setParameter = function (name, value) {
    var parameter = this.parameters[name];
    if (!parameter) {
        throw new Error('setParameter(): Accessor ' +
            this.accessorName +
            ' has no parameter named ' + name +
            ' Perhaps it is an input and you should use setDefault()?');
    }
    // If necessary, convert the value to the match the type.
    value = convertType(value, parameter, name);

    parameter.currentValue = value;
};

/** Stop execution of the enclosing swarmlet by finding the top-level
 *  accessor and invoking wrapup() on it.
 */
Accessor.prototype.stop = function () {
    // console.log("commonHost.js: stop");
    var container = this;
    // Find the top-level container.
    while (container.container) {
        container = container.container;
    }
    // console.log("commonHost.js: stop: container: " + container);
    container.wrapup();
};

/** Stop execution of the enclosing swarmlet by finding the top-level
 *  accessor and invoking wrapup() on it.
 *  @param timeout When this time is reached, stop() is called.
 */
Accessor.prototype.stopAt = function (timeout) {
    this.stopAtTime = timeout;
    var self = this;
    self.setTimeout(function() {
        self.stop();
    }, timeout);
};

/** Updates the monitoring information (count, date/time of the first event and date/time
 *  of the last event).
 *  @param: info The name of the monitored event to update. info is a string
 */
Accessor.prototype.updateMonitoringInformation = function (info) {
    // Note that we could just use this instead of this.root because of the
    // prototype chain, but in a deep hierarchy, this will be more efficient.
    var thiz = this.root;

    // Increment the events count
    var monitor = thiz.monitor[info];
    monitor.count = monitor.count + 1;

    // Update the first occurrence date, if not defined yet
    if (!(monitor.firstOccurrenceDate['Date&Time'])) {
        monitor.firstOccurrenceDate = {
            'Date&Time': new Date().toLocaleString()
        };
    }

    // Update the last occurrence date
    monitor.latestOccurrenceDate = {
        'Date&Time': new Date().toLocaleString()
    };
};

///////////////////////////////////////////////////////////////////
//// Module functions.

/** If called with argument true, then accessors that are subsequently
 *  instantiated whose class name begins with 'trusted/' will have a
 *  binding for the getTopLevelAccessors() function. Such accessors
 *  can see and manipulate peer accessors, so a host that allows such
 *  trusted accessors should ensure that all trusted accessors are
 *  locally defined rather than downloaded from untrusted sources.
 *  @param allow True to allow trusted accessors, false otherwise.
 */
function allowTrustedAccessors(allow) {
    trustedAccessorsAllowed = allow;
}

/** Convert the specified type to the type expected by the specified input,
 *  or throw an exception if no such conversion is possible.
 *  @param value The value to convert.
 *  @param destination The destination object, which may have a type property.
 *   This is an input, parameter, or output options object.
 *  @param name The name of the input, output, or parameter (for error reporting).
 */
function convertType(value, destination, name) {
    if (!destination.type || destination.type === typeof value || value === null) {
        // Type is unspecified or a match, or value is null. Use value as given.
    } else if (destination.type === 'string') {
        if (typeof value !== 'string') {
            // Convert to string.
            try {
                value = JSON.stringify(value);
            } catch (error) {
                throw new Error('Object provided to ' +
                    name +
                    ' does not have a string representation: ' +
                    error);
            }
        }
    } else if (typeof value === 'string') {
        // Provided value is a string, but
        // destination type is boolean, number, int, or JSON.
        if (value === '') {
            // If the value is an empty string, then convert
            // to null, unless the destination type is JSON.
            if (destination.type !== 'JSON') {
                value = null;
            }
        } else {
            try {
                value = JSON.parse(value);
            } catch (error) {
                throw new Error('Failed to convert value to destination type: ' +
                    name +
                    ' expected a ' +
                    destination.type +
                    ' but received: ' +
                    value);
            }
        }
    } else if (destination.type === 'boolean' && typeof value !== 'boolean') {
        // Liberally convert JavaScript data to boolean.
        if (value) {
            value = true;
        } else {
            value = false;
        }
    } else if (destination.type === 'int' || destination.type === 'number') {
        // value is not a string. Needs to be a number.
        if (typeof value !== 'number') {
            throw new Error(name + ' expected an int, but got a ' +
                (typeof value) +
                ': ' +
                value);
        }
        // If type is int, need the value to be an integer.
        if (destination.type === 'int' && value % 1 !== 0) {
            throw new Error(name + ' expected an int, but got ' + value);
        }
    } else {
        // Only remaining case: value is not a string
        // and destination type is JSON. Just check that the value has a
        // JSON representation.
        try {
            JSON.stringify(value);
        } catch (err) {
            throw new Error('Object provided to ' +
                name +
                ' does not have a JSON representation: ' +
                err);
        }
    }
    return value;
}

/** Return an object of objects. Each object (referenced by the accessor name)
 *  contains: the accessor type (mutable, top level, extended, implemented),
 *  and all the monitoring information.
 *
 *  @return an object of names the top level accessors that have been created thus far.
 */
function getMonitoringInformation() {
    var result = {};
    for (var i = 0; i < allAccessors.length; i++) {
        var type;
        var name = allAccessors[i].accessorName;

        // Get the type of the accessor
        if (allAccessors[i].mutable) {
            type = 'mutable';
        } else if (allAccessors[i].extendedBy) {
            type = 'extended';
        } else if (allAccessors[i].implementedBy) {
            type = 'implemented';
        } else if (!allAccessors[i].container) {
            type = 'topLevel';
        } else {
            type = 'composite';
        }

        result[name] = {
            'type': type,
            'monitoringInformation': allAccessors[i].monitor
        };
    }
    return result;
}

/** Return the top-level accessors that have been created thus far.
 *  @return an array that names the top level accessors that have been created thus far.
 */
function getTopLevelAccessors() {
    var result = [];
    for (var i = 0; i < allAccessors.length; i++) {
        if (!allAccessors[i].container && !allAccessors[i].extendedBy && !allAccessors[i].implementedBy) {
            result.push(allAccessors[i]);
        }
    }
    return result;
}

/** Instantiate an accessor given its fully qualified name, a function to retrieve
 *  the code, and a require function to retrieve modules.
 *  The returned object will have a property **accessorClass** with the value of the
 *  name parameter passed in here.
 *  @param accessorName A name to give to the accessor instance.
 *  @param accessorClass Fully qualified accessor class, e.g. 'net/REST'.
 *  @param getAccessorCode A function that will retrieve the source code of a specified
 *   accessor (used to implement the this.extend() and this.implement() functions), or null if
 *   the host does not support accessors that extend other accessors.
 *  @param bindings The function bindings to be used by the accessor.
 *  @param extendedBy An optional argument specifying what accessor is extending
 *   this new instance. Pass null or no argument if this accessor is not being extended.
 *   If this argument is present, then the getAccessorCode and bindings arguments are
 *   ignored (the instance inherits those properties from the extender).
 *  @param implementedBy An optional argument specifying what accessor is implementing
 *   this new instance. Pass null or no argument if this accessor is not being
 *   implemented.
 *   If this argument is present, then the getAccessorCode and bindings arguments are
 *   ignored (the instance inherits those properties from the implementer).
 */
function instantiateAccessor(
    accessorName, accessorClass, getAccessorCode, bindings, extendedBy, implementedBy, mutable) {
    var code = getAccessorCode(accessorClass);
    // In case bindings is not defined.
    bindings = bindings || {};
    if (trustedAccessorsAllowed && accessorClass.startsWith('trusted/')) {
        bindings.getTopLevelAccessors = getTopLevelAccessors;
    } else {
        bindings.getTopLevelAccessors = function () {
            throw new Error('getTopLevelAccessors(): Accessors are not permitted' +
                ' to access peer accessors in this host.');
        };
    }
    var instance = new Accessor(
        accessorName, code, getAccessorCode, bindings, extendedBy, implementedBy, mutable);
    instance.accessorClass = accessorClass;
    allAccessors.push(instance);
    return instance;
}

/** Merge the specified objects. If the two have common properties, the merged object
 *  will have the properties of the second argument. If the first argument is null,
 *  then the returned object will equal the second argument, unless it too is null,
 *  in which case the returned object will be an empty object.
 *  @param first The first argument, giving default properties.
 *  @param second The second argument.
 */
function mergeObjects(first, second) {
    if (first) {
        if (second) {
            // Both objects exist. Copy the first.
            var result = {};
            for (var property in first) {
                result[property] = first[property];
            }
            // Override with the second.
            for (var property2 in second) {
                result[property2] = second[property2];
            }
            return result;
        } else {
            // First but no second object.
            return first;
        }
    } else if (second) {
        // Second but no first object.
        return second;
    }
    // No first or second.
    return {};
}

/** Default empty function to use if the function argument to
 *  addInputHandler is null.
 */
function nullHandlerFunction() {}

/** Process command line arguments to instantiate and initialize
 *  accessors or to evaluate plain JavaScript within the context of an
 *  accessor host. This is provided here in commonHost so that all
 *  accessor hosts that provide a command-line usage have the same
 *  command-line argument structure.
 *
 *  This function takes up to four arguments,
 *  where only the first is required.
 *
 *  The first argument is an array of
 *  command-line arguments (as detailed below).
 *
 *  The second (optional)
 *  argument is a function that given a file name, returns the contents
 *  of the file as a string. The second argument is needed only if
 *  plain JavaScript files are to be evaluated using the -js command-line
 *  argument.
 *
 *  The third (optional) argument is an instantiate function that
 *  takes two arguments, an accessor name (an arbitrary string) and an
 *  accessor class name. This argument is needed only if accessors class
 *  names are given on the command line to instantiate and initialize.
 *
 *  The fourth (optional) argument is a callback function to
 *  invoke when either a specified timeout is reached (if a timeout
 *  is provided), or all instantiated accessors have wrapped up
 *  and all specified JavaScript files have been evaluated. This argument
 *  may, for example, cause the calling process to exit.
 *
 *  For example, if a host provides a executable command named
 *  'host', then it might be invoked as follows:
 *
 *      > host -t 1000 -js fileName.js test/TestAccessor
 *
 *  The above command will evaluate the JavaScript in fileName.js, then
 *  instantiate the accessor with class name test/TestAccessor and
 *  initialize it, then wait one second and then call the terminate
 *  callback function.  For the above command to work, this function
 *  has to be provided a fileReader argument to read fileName.js.
 *
 *  The order in which files and accessors are specified matters. They
 *  will be evaluated or instantiated and initialized in the order that
 *  they appear in the argument list.
 *
 *  The command-line arguments are file names, accessor class names (such as
 *  net/REST), or any of the following options:
 *
 *  * -e|--e|-echo|--echo: Echo the command-line arguments.
 *    This is helpful for use under Ant apply.
 *
 *  * -h|--h|-help|--help: Print a usage message.
 *
 *  * -j|--j|-js|--js: Interpret the next argument as the name of a regular
 *    JavaScript file to evaluate.
 *
 *  * -k|--k|-keepalive|--keepalive: Keep the calling process alive until either
 *    a timeout option expires or all instanted accessors have called wrapup.
 *
 *  * -t|--t|-timeout|--timeout milliseconds: The maximum amount of time the
 *    script can run. When this time is reached, stop() is called on all
 *    accessors that have been instantiated, and then
 *
 *  * -v|--v|-version|--version: Print out the version number
 *
 *  @param argv An array of command-line arguments, see above.
 *  @param fileReader A function that, given a file name, returns its contents as a string.
 *  @param instantiateTopLevel A function that, given a name and class, instantiates a
 *   top-level accessor and returns it.
 *  @param terminator A callback function to invoke when all work is done.
 *  @return True if any accessors were intantiated and initialized or if the keepalive
 *   option was specified. The caller can use the return value to indicate whether there
 *   is still work to be done, for example in active accessors. The caller may exit if
 *   the return value is false, since this indicates that all work is done.
 */
function processCommandLineArguments(argv, fileReader, instantiateTopLevel, terminator) {

    // Simplified usage message to just show the preferred form of the arguments,
    // not all possible variations.
    var usage = "Usage: [-help] [-echo] [-js filename] [-keepalive] [-timeout milliseconds]" +
        " [-version] [accessorClassName] [accessorClassName ...]";
    var timeout = -1;

    // If there is no other definition of the instantiate() function in scope,
    // then set it equal to the instantiateTopLevel argument.
    if (typeof instantiate === 'undefined') {
        var instantiate = instantiateTopLevel;
    }

    if (argv.length === 0) {
        console.error(usage);
        return false;
    }

    var accessorCount = 0;
    var accessorsWrappedUp = 0;
    var keepAlive = false;
    for (var i = 0; i < argv.length; i++) {
        switch (argv[i]) {

        case '-e':
        case '--e':
        case '-echo':
        case '--echo':
            console.log(argv);
            break;

        case '-h':
        case '--h':
        case '-help':
        case '--help':
            console.log(usage);
            return false;

        case '-j':
        case '--j':
        case '-js':
        case '--js':
            if (i + 1 >= argv.length) {
                console.error("Argument " + i + "  was " + argv[i] + " but there is no " +
                    "following filename argument.  Args were: " + argv);
                return false;
            }
            i += 1;
            if (!fileReader) {
                console.error('This host does not support evaluating arbitrary JavaScript files.');
                return false;
            }

            try {
                eval(fileReader(argv[i]));
            } catch (error) {
                console.error('Failed to eval "' + argv[i] + '": ' + error +
                    ":" + error.stack);
                return false;
            }

            break;

        case '-k':
        case '--k':
        case '-keepalive':
        case '--keepalive':
            keepAlive = true;
            break;

        case '-t':
        case '--t':
        case '-timeout':
        case '--timeout':
            if (i + 1 >= argv.length) {
                console.error("Argument " + i + "  was " + argv[i] + " but there is no " +
                    "following milliseconds argument.  Args were: " + argv);
                return false;
            }
            i += 1;
            if (typeof argv[i] === 'string') {
                argv[i] = JSON.parse(argv[i]);
            }
            timeout = argv[i];

            console.log("commonHost.js: processCommandLineArguments(): " +
                "Setting timeout to stop after " + timeout + " ms.");
            setTimeout(function () {
                // Under node, process.exit gets caught by exitHandler() in
                // nodeHost.js and invokes wrapup().
                console.log("commonHost.js: processCommandLineArguments(): Maximum time reached. Calling stopAllAccessors().");
                // If a keep-alive timer is active, stop it.
                if (timerHandle) {
                    clearInterval(timerHandle);
                }
                // If a terminator function is given, use it.
                // Otherwise, invoke wrapup on all accessors.
                if (terminator) {
                    terminator();
                } else {
                    stopAllAccessors();
                }
            }, timeout);
            break;

        case '-v':
        case '--v':
        case '-version':
        case '--version':
            console.log("Accessors 1.0, commonHost.js: $Id$");
            return false;

        default:
            // All other arguments are assumed to be accessor class names to be instantiated.
            if (!instantiateTopLevel) {
                console.error('This host does not support instanting accessors.');
                return false;
            }
            accessorCount++;
            // Name for the accessor.
            var name = uniqueName(argv[i]);
            var accessor = instantiateTopLevel(name, argv[i]);

            // Initialize the accessor.
            accessor.initialize();

            accessor.on('wrapup', function () {
                accessorsWrappedUp++;
                if (terminator && accessorsWrappedUp >= accessorCount) {
                    // All initialized accessors have wrapped up.
                    console.log("All initialized accessors have wrapped up. Terminating.");
                    if (timerHandle) {
                        clearInterval(timerHandle);
                    }
                    terminator();
                }
            });
        }
    }

    // All command-line arguments have been processed.
    var timerHandle;
    // If a keep-alive argument has been given or if
    // any accessors have been initialized and no timeout has been specified,
    // then set a timeout to keep the process from exiting.
    if (keepAlive || (accessorCount > 0 && timeout === -1)) {
        // Prevent the script from exiting by repeating the empty function
        // every ~25 days. This will be cancelled if the terminator argument
        // is specified and all accessors have wrapped up.
        timerHandle = setInterval(function () {}, 2147483647);
    }
    if (accessorCount > 0) {
        return true;
    }
    return keepAlive;
}

/** Push the specified item onto the specified list if it is not already there.
 *  @param item Item to push.
 *  @param list List onto which to push it.
 */
function pushIfNotPresent(item, list) {
    for (var j = 0; j < list.length; j++) {
        if (item === list[j]) {
            return;
        }
    }
    list.push(item);
}

/** Stop execution by invoking wrapup() on all top-level accessors
 *  that have been initialized and not wrapped up.
 */
function stopAllAccessors() {
    var accessors = getTopLevelAccessors();
    var initialThrowable = null;
    for (var i = 0; i < accessors.length; i++) {
        var composite = accessors[i];
        if (composite.initialized) {
            try {
                console.log('commonHost.js: invoking wrapup() for accessor: ' + composite.accessorName);
                composite.wrapup();
            } catch (error) {
                console.error('commonHost.js: wrapup failed for accessor: ' + composite.accessorName);
                if (initialThrowable === null) {
                    initialThrowable = error;
                }
            }
        }
    }
    if (initialThrowable !== null) {
        throw new Error("commonHost.js: stopAllAccessors(): while invoking wrapup() of all accessors," +
            " an exception was thrown: " + initialThrowable + ":" + initialThrowable.stack);
    }
}

/** Return a name that is unique in the specified container based on the specified
 *  seed. If no container is given, then return a name that is unique among top-level
 *  accessors. If the seed contains slashes, as in an accessor class name like net/REST,
 *  then everything before the last slash is removed. If the seed ends with an extension
 *  .js, then everything after and including the last period is removed. The remaining
 *  seed will be used as is if the name is unique. Otherwise, it will have a number, starting
 *  with 2, appended so as to ensure that it is unique.
 */
function uniqueName(seed, container) {
    var startIndex = (seed.indexOf('\\') >= 0 ? seed.lastIndexOf('\\') :
        seed.lastIndexOf('/'));
    if (startIndex >= 0) {
        seed = seed.substring(startIndex + 1);
    }
    // Don't use endsWith() here, duktape does not support ECMA6 endsWith().
    if (seed.indexOf('.js') === seed.length - 3) {
        seed = seed.substring(0, seed.length - 3);
    }
    var accessors = getTopLevelAccessors();
    if (container && container.containedAccessors) {
        accessors = container.containedAccessors;
    }
    // Convert the list into an object for quick lookup.
    var names = {};
    for (var i = 0; i < accessors.length; i++) {
        if (accessors[i].accessorName) {
            names[accessors[i].accessorName] = true;
        }
    }
    if (!names[seed]) {
        return seed;
    } else {
        var count = 2;
        while (names['' + seed + count]) {
            count++;
        }
        return '' + seed + count;
    }
}

///////////////////////////////////////////////////////////////////
//// Module variables.

/** Table of accessor instances indexed by their exports property.
 *  This allows us to retrieve the full accessor data structure, but to only
 *  expose to the user of this module the exports property of the accessor.
 *  Note that this host does not support removing accessors, so the instance
 *  will be around as long as the process exists.
 */
var _accessorInstanceTable = {};

///////////////////////////////////////////////////////////////////
//// Exports

// Note that there are some exports that occur earlier in this file.
// FIXME: Why?
exports.Accessor = Accessor;
exports.allowTrustedAccessors = allowTrustedAccessors;
exports.instantiateAccessor = instantiateAccessor;
exports.getMonitoringInformation = getMonitoringInformation;
exports.getTopLevelAccessors = getTopLevelAccessors;
exports.processCommandLineArguments = processCommandLineArguments;
exports.stopAllAccessors = stopAllAccessors;
exports.uniqueName = uniqueName;

