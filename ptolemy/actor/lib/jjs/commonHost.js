// JavaScript functions to be shared among accessor hosts.
//
// Copyright (c) 2015 The Regents of the University of California.
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


                ////////////////////////////////////////////
                ////                NOTE:               ////
                ////  This file has an exact copy in    ////
                ////    accessors/web/hosts/common      ////
                ////    and in the Ptolemy tree at      ////
                ////   $PTII/ptolemy/actor/lib/jjs      ////
                //// If you update here, please update  ////
                ////   both places and run the tests    ////
                ////////////////////////////////////////////

/** This module provides host-independent functions for swarmlet hosts.
 *  A specific host (such as the Node.js host, the browser host, or the Ptolemy II
 *  host) can use this module to implement common functionality that is realizable
 *  in pure JavaScript.
 *
 *  Specifically, this module provides a constructor for instantiating accessors,
 *  and a convenience function that takes as an argument the fully qualified accessor
 *  class name ('''instantiateAccessor'''), such as 'net/REST'.
 *
 *  For these functions to be able to instantiate an accessor
 *  given only its class, the host needs to provide as an argument to new Accessor() or
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
 *  * '''setParameter'''(name, value): Set a parameter value.
 *  * '''initialize'''(): Initialize the accessor.
 *  * '''provideInput'''(name, value): Provide an input value.
 *  * '''react()''' React to input values and fire the accessor.
 *  * '''latestOutput'''(name): Retrieve an output value produced in react().
 *  * '''wrapup'''(): Wrap up the accessor.
 *
 *
 *  The react() function first invokes all input handlers that have been registered
 *  to handle provided inputs. It then invokes all input handlers that have been
 *  registered to handle any input.  Finally, it invokes the fire() function of the
 *  accessor, if one is defined.
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
 *  An accessor need not have any inputs. In this case, an invocation of react()
 *  will not invoke any input handlers (there cannot be any) and will only invoke the
 *  fire() function.  Even with no fire() function, such an accessor can produce outputs
 *  if it sets up callbacks in its initialize() function, for example using
 *  setTimeout() or setInterval(). We call such an accessor a '''spontaneous accessor''',
 *  because it spontaneously produces outputs without being triggered by any input.
 *
 *  A composite accessor can contain spontaneous accessors. When these produce outputs,
 *  the composite accessor will automatically react. Hence, the contained spontaneous
 *  accessor can trigger reactions of other contained accessors.
 *
 *  The two instantiate functions can also be provided a set of function
 *  bindings that will override the defaults in this implementation.
 *  The most useful ones to override are probably these:
 *
 *  * '''require''': The host's implementation of the require function.
 *     The default implementation throws an exception indicating that the host
 *     does not support any external modules.
 *  * '''get''': A function to retrieve the value of an input. The default
 *     implementation returns the the value specified by a provideInput() call, or
 *     if there has been no provideInput() call, then the value provided in the
 *     options argument of the input() call, or null if there is no value.
 *  * '''getParameter''': A function to retrieve the value of a parameter. The default
 *     implementation returns the the value specified by a setParameter() call, or
 *     if there has been no setParameter() call, then the value provided in the
 *     options argument of the parameter() call, or null if there is no value.
 *  * '''send''': A function to send an output. The default implementation produces
 *     the output using console.log().
 *
 *
 *  @module commonHost
 *  @authors: Edward A. Lee and Chris Shaver
 */

'use strict';

/** Create (using new) an accessor instance whose interface and functionality is given by
 *  the specified code. Specifically, the created object includes at least the following
 *  properties:
 *
 *  * '''exports''': An object that includes any properties that have have been
 *    explicitly added to the exports property in the specified code.
 *  * '''inputList''': An array of input names (see below).
 *  * '''inputs''': An object with one property per input (see below).
 *  * '''outputList''': An array of output names (see below).
 *  * '''outputs''': An object with one property per output (see below).
 *  * '''parameterList''': An array of parameter names (see below).
 *  * '''parameters''': An object with one property per parameter (see below).
 *  * '''inputHandlers''': An object indexed by input name with
 *    an array of input handlers, each of which is a function.
 *  * '''anyInputHandlers''': An array of input handlers to be invoked
 *    when any input arrives (the name argument of addInputHandler is null).
 *  * '''inputHandlersIndex''': An object indexed by handler id (returned
 *    by addInputHandler()) that contains objects of the form
 *    {'name': nameOfInput, 'index': arrayIndexOfHandler}.
 *    This is used by removeInputHandler(). If the handler is one
 *    for any input, then nameOfInput is null and arrayIndexOfHandler
 *    specifies the position of the handler in the anyInputHandlers array.
 *
 *
 *  Inputs, outputs, and parameters in an accessor have a defined order.
 *  The ```inputList``` property is an array giving the name of each input
 *  in the order in which it is defined in the setup() function.
 *  For each entry in that array, there is a property by that name in the
 *  ```inputs``` object, indexed by the name. The value of that property is the
 *  options object given to the ```input()``` function, possibly with additional
 *  properties such as 'destination', which is used for composite accessors.
 *  Similarly, parameters and outputs are represented in the
 *  data structure by an array of names and an object with the options values.
 *
 *  The bindings parameter provides function bindings for functions that are used by
 *  accessors.  Any that are not provided will be provided with defaults.
 *  Any that are provided will override the defaults.
 *
 *  @param accessorName A name to give to the accessor.
 *  @param code The accessor source code.
 *  @param getAccessorCode A function that will retrieve the source code of a specified
 *   accessor (used to implement the extend() and implement() functions), or null if
 *   the host does not support accessors that extend other accessors.
 *  @param bindings The function bindings to be used by the accessor.
 *  @param extendedBy An optional argument specifying what accessor is extending or
 *   implementing this new instance. Pass null or no argument if it is not being extended.
 *   This should always be the top-level object doing the extending, not the immediate
 *   parent.
 */
function Accessor(accessorName, code, getAccessorCode, bindings, extendedBy) {
    if (!code) {
        throw 'No accessor code specified.';
    }
    this.accessorName = accessorName;
    this.getAccessorCode = getAccessorCode;
    this.bindings = bindings;
    this.extendedBy = extendedBy;
    
    // If no extendedBy is given, then initialize the data structures to be used
    // by this instance.
    if (!extendedBy) {
    
        // Indicate that the top-level object is this.
        this.extendedBy = this;
            
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
        //// Support for composite accessors.

        // List of contained accessors.
        this.containedAccessors = [];
    }
    
    ////////////////////////////////////////////////////////////////////
    //// Override using specified bindings.

    // Any property defined in the bindings argument overrides prototype functions
    // for this instance.
    for (var binding in bindings) {
        this[binding] = bindings[binding];
    }

    // Define the exports object to be populated by the accessor code.
    // Even if there is an object extending this one, this one has its own
    // exports property. If this object is being extended (rather than
    // implemented), then this new exports object will become the prototype
    // of the exports object of the immediate parent (see the extend() function
    // below).
    this.exports = {};
    
    // To keep track of implemented interfaces.
    this.implementedInterfaces = [];
    
    ////////////////////////////////////////////////////////////////////
    //// Evaluate the accessor code.

    // In strict mode, eval() cannot modify the scope of this function.
    // Hence, we wrap the code in the function, and will pass in the
    // exports object that we want the code to modify.
    // Inside that function, references to top-level accessor functions
    // such as input() will need to be bound to the particular implementation
    // of input() within this accessor function, so that that function
    // updates the proper property of this function.
    
    // NOTE: This only needs to include exports and the top-level functions
    // that the accessor can invoke. It need not include local functions, nor
    // should include functions that the host uses but accessors do not, such
    // as latestOutput(), provideInput(), and react().
    // It should also include the module object.
    
    // FIXME: Use the Function constructor for this:
        
    var wrapper = eval('(function( \
            addInputHandler, \
            connect, \
            exports, \
            extend, \
            get, \
            getParameter, \
            implement, \
            input, \
            instantiate, \
            module, \
            output, \
            parameter, \
            removeInputHandler, \
            require, \
            send, \
            setDefault, \
            setParameter) {'
            + code
            + '})');
               
    // Populate the exports property by invoking the function defined above.
    // These should only the top-level functions that an accessor can invoke.
    wrapper(
            this.addInputHandler.bind(this),
            this.connect.bind(this),
            this.exports,
            this.extend.bind(this),
            this.get.bind(this),
            this.getParameter.bind(this),
            this.implement.bind(this),
            this.input.bind(this),
            this.instantiate.bind(this),
            this.module,
            this.output.bind(this),
            this.parameter.bind(this),
            this.removeInputHandler.bind(this),
            this.require.bind(this),
            this.send.bind(this.extendedBy),
            this.setDefault.bind(this),
            this.setParameter.bind(this));
        
    ////////////////////////////////////////////////////////////////////
    //// Evaluate the setup() function to populate the structure.
        
    if (typeof this.exports.setup === 'function') {
        this.exports.setup();
    }
        
    ////////////////////////////////////////////////////////////////////
    //// Provide wrapper functions for initialize() and wrapup().
    
    // The instance versions of initialize() and wrapup() perform
    // functions that every accessor should perform, including handling
    // scheduling of any contained accessors.  They will also invoke
    // exports.initialize() and exports.wrapup(), if those are defined.
    
    this.initialize = function() {
        if (this.extendedBy.containedAccessors && this.extendedBy.containedAccessors.length > 0) {
            this.extendedBy.assignPriorities();
            this.extendedBy.eventQueue = [];
            for (var i = 0; i < this.extendedBy.containedAccessors.length; i++) {
                if (this.extendedBy.containedAccessors[i].initialize) {
                    this.extendedBy.containedAccessors[i].initialize();
                }
            }
        }
        if (typeof this.exports.initialize === 'function') {
            this.exports.initialize();
        }
    };

    this.wrapup = function() {
        // Mark that this accessor has not been initialized.
        this.extendedBy.initialized = false;
        
        // Remove all input handlers.
        this.inputHandlers = {};
        this.anyInputHandlers = [];
        this.inputHandlersIndex = {};
        // Counter used to assign unique IDs to each input handler.
        this.inputHandlerID = 0;
                
        // Invoke wrapup on contained accessors.
        if (this.extendedBy.containedAccessors && this.extendedBy.containedAccessors.length > 0) {
            for (var i = 0; i < this.extendedBy.containedAccessors.length; i++) {
                if (this.extendedBy.containedAccessors[i].wrapup) {
                    this.extendedBy.containedAccessors[i].wrapup();
                }
            }
        }
        if (typeof this.exports.wrapup === 'function') {
            this.exports.wrapup();
        }
    };

    // Record the instance indexed by its exports property.
    _accessorInstanceTable[this.exports] = this;
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
 *  @return An ID that can be passed to removeInputHandler().
 */
Accessor.prototype.addInputHandler = function(name, func) {
    var argCount = 2, callback, id, tail;
    if (name && typeof name !== 'string') {
        // Tolerate a single argument, a function.
        if (typeof name === 'function') {
            func = name;
            name = null;
            argCount = 1;
        } else {
            throw ('name argument is required to be a string. Got: ' + (typeof name));
        }
    }
    if (!func) {
        func = nullHandlerFunction;
    } else if (typeof func !== 'function') {
        throw ('Argument of addInputHandler is not a function. It is: ' + func);
    }

    // Check that the input exists.
    if (name && !this.extendedBy.inputs[name]) {
        throw 'Cannot add an input handler to a non-existent input: ' + name;
    }
    
    // Bind the callback function so that it is always invoked in the context
    // of the exports object of the master accessor object (no the base classes).
    // If there are arguments to the callback, create a new function.
    // Get an array of arguments excluding the first two.
    // When that function is invoked, 'this' will be the exports data structure
    // of the top-level instance (which by prototype chain, can reach down
    // the inheritance hierarchy).
    tail = Array.prototype.slice.call(arguments, argCount);
    var context = this.extendedBy.exports;
    if (tail.length !== 0) {
        callback = function() {
            func.apply(context, tail);
        };
    } else {
        callback = func.bind(context);
    }
    // Need to allow more than one handler and need to return a handle
    // that can be used by removeInputHandler.
    var index;
    if (name) {
        if (! this.extendedBy.inputHandlers[name]) {
            this.extendedBy.inputHandlers[name] = [];
        }
        index = this.extendedBy.inputHandlers[name].length;
        this.extendedBy.inputHandlers[name].push(callback);
    } else {
        index = this.extendedBy.anyInputHandlers.length;
        this.extendedBy.anyInputHandlers.push(callback);
    }
    var result = this.extendedBy.inputHandlerID;
    this.extendedBy.inputHandlersIndex[this.extendedBy.inputHandlerID++] = {
        'name': name,
        'index': index
    };
    // Record the handle in the callback function so that it can be
    // removed if it throws an exception.
    callback.handle = result;
    return result;
}

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
Accessor.prototype.assignPriorities = function() {
    var accessors = this.extendedBy.containedAccessors;
    
    // First, initialize the contained accessors with a null priority.
    for (var i = 0; i < accessors.length; i++) {
        accessors[i].priority = null;
    }
    // Next, assign the first accessor an arbitrary priority and follow its
    // connections to assign priorities implied by those connections.
    var startingPriority = 0;
    for (var i = 0; i < accessors.length; i++) {
        // If the instance already has a priority, skip it.
        if (accessors[i].priority != null) {
            continue;
        }
        accessors[i].priority = startingPriority;
        // console.log('Assigned priority to ' + accessors[i].accessorName + ' of ' + startingPriority);
        // Follow connections to get implied priorities.
        this.extendedBy.assignImpliedPrioritiesUpstream(accessors[i], startingPriority);
        this.extendedBy.assignImpliedPrioritiesDownstream(accessors[i], startingPriority);

        // Any remaining accessors without priorities are in one or more independent
        // connected subgraphs. To ensure that the next set of priorities does not
        // overlap those already assigned, we start with a sufficiently higher number.
        startingPriority = 2*(accessors.length - i);
    }
}

/** Assuming that the specified accessor has an assigned priority, follow its
 *  connections downstream and assign priorities to connected accessors.
 *  @param accessor The contained accessor with a priority.
 *  @param cyclePriority If we encounter an accessor with this priority, then
 *   there is a causality loop.
 */
Accessor.prototype.assignImpliedPrioritiesDownstream
        = function(accessor, cyclePriority) {
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
                    throw('Causality loop found including at least: '
                            + destinationAccessor.accessorName);
                }
                if (theirPriority === null) {
                    // Destination has no previously assigned priority. Give it one,
                    // and follow the implications.
                    destinationAccessor.priority = myPriority + 1;
                    // console.log('Assigned downstream priority to ' + destinationAccessor.accessorName + ' of ' + (myPriority + 1));
                    this.assignImpliedPrioritiesDownstream(
                            destinationAccessor, cyclePriority);
                } else {
                    if (theirPriority > myPriority) {
                        // Priority is OK. Continue.
                        continue;
                    }
                    // Priority has to be adjusted.
                    destinationAccessor.priority = myPriority + 1;
                    // console.log('Assigned downstream priority to ' + destinationAccessor.accessorName + ' of ' + (myPriority + 1));
                    this.assignImpliedPrioritiesDownstream(
                            destinationAccessor, cyclePriority);
                }
            }
        }
    }        
}

/** Assuming that the specified accessor has an assigned priority, follow its
 *  connections upstream and assign priorities to connected accessors.
 *  @param accessor The contained accessor with a priority.
 *  @param cyclePriority If we encounter an accessor with this priority, then
 *   there is a causality loop.
 */
Accessor.prototype.assignImpliedPrioritiesUpstream = function(accessor, cyclePriority) {
    var myPriority = accessor.priority;
    // To get repeatable priorities, iterate over inputs in order.
    for (var i = 0; i < accessor.inputList.length; i++) {
        var input = accessor.inputs[accessor.inputList[i]];
        if (input.source && typeof input.source !== 'string') {
            // There is a source accessor.
            var source = input.source.accessor;
            var output = source.outputs[source.outputName];
            // If the output is marked 'spontaneous' then we can ignore it.
            if (output.spontaneous) {
                continue;
            }
            var theirPriority = source.priority;
            if (theirPriority === cyclePriority) {
                    throw('Causality loop found including at least: '
                            + accessor.accessorName);
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
}

/** Connect the specified inputs and outputs.
 *  There are four forms of this function:
 *
 *  1. connect(sourceAccessor, 'outputName', destinationAccessor, 'inputName');
 *  2. connect('myInputName', destinationAccessor, 'inputName');
 *  3. connect(sourceAccessor, 'outputName', 'myOutputName');
 *  4. connect('myInputName', 'myOutputName');
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
 *  '''accessor''' and '''inputName'''.
 *
 *  This method also sets a '''source''' property of the input or output that is
 *  the source of data on the connection. Again, that property is either a string
 *  name (to mean an input of the container accessor) or an object with two
 *  properties '''accessor''' and '''outputName'''.
 *
 *  @param a An accessor or a name.
 *  @param b An accessor or a name.
 *  @param c An accessor or a name.
 *  @param d A destination port name.
 */
Accessor.prototype.connect = function(a, b, c, d) {
    if (typeof a === 'string') {
        // form 2 or 4.
        var myInput = this.extendedBy.inputs[a];
        if (!myInput) {
            throw('connect(): No such input: ' + a);
        }
        if (!myInput.destinations) {
            myInput.destinations = [];
        }
        if (typeof b === 'string') {
            // form 4.
            if (!this.extendedBy.outputs[b]) {
                throw('connect(): No such output: ' + b);
            }
            myInput.destinations.push(b);
            this.extendedBy.outputs[b].source = a;
        } else {
            // form 2.
            if (!b.inputs[c]) {
                throw('connect(): Destination has no such input: ' + c);
            }
            myInput.destinations.push({'accessor': b, 'inputName': c});
            b.inputs[c].source = a;
        }
    } else {
        // form 1 or 3.
        var myOutput = a.outputs[b];
        if (!myOutput) {
            throw('connect(): Source has no such output: ' + b);
        }
        if (!myOutput.destinations) {
            myOutput.destinations = [];
        }
        if (typeof c === 'string') {
            // form 3.
            if (!this.extendedBy.outputs[c]) {
                throw('connect(): No such output: ' + b);
            }
            myOutput.destinations.push(c);
            this.extendedBy.outputs[c].source = {'accessor': a, 'outputName': b};
        } else {
            // form 1.
            if (!c.inputs[d]) {
                throw('connect(): Destination has no such input: ' + d);
            }
            myOutput.destinations.push({'accessor': c, 'inputName': d});
            c.inputs[d].source = {'accessor': a, 'outputName': b};
        }
    }
}
    
/** Convert the specified type to the type expected by the specified input,
 *  or throw an exception if no such conversion is possible.
 *  @param value The value to convert.
 *  @param destination The destination object, which may have a type property.
 *   This is an input, parameter, or output options object.
 *  @param name The name of the input, output, or parameter (for error reporting).
 */
function convertType(value, destination, name) {
    if (!destination.type || destination.type === typeof value  || value === null) {
        // Type is unspecified or a match, or value is null. Use value as given.
    } else if (destination.type === 'string') {
        if (typeof value !== 'string') {
            // Convert to string.
            try {
                value = JSON.stringify(value);
            } catch (error) {
                throw('Object provided to '
                        + name
                        + ' does not have a string representation: '
                        + error);
            }
        }
    } else if (typeof value === 'string') {
        // Provided value is a string, but 
        // destination type is boolean, number, int, or JSON.
        if (value === '') {
            // If the value is an empty string, then convert
            // to null, unless the destination type is JSON.
            if (!(destination.type === 'JSON')) {
                value = null;
            }
        } else {
            try {
                value = JSON.parse(value);
            } catch (error) {
                throw('Failed to convert value to destination type: '
                        + name
                        + ' expected a '
                        + destination.type
                        + ' but received: '
                        + value);
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
            throw(name + ' expected an int, but got a '
                    + (typeof value)
                    + ': '
                    + value);
        }
        // If type is int, need the value to be an integer.
        if (destination.type === 'int' && value % 1 !== 0) {
            throw(name + ' expected an int, but got ' + value);
        }
    } else {
        // Only remaining case: value is not a string
        // and destination type is JSON. Just check that the value has a
        // JSON representation.
        try {
            JSON.stringify(value);
        } catch(error) {
            throw('Object provided to '
                    + name
                    + ' does not have a JSON representation: '
                    + error);
        }
    }
    return value;
}

/** Extend the specified accessor, inheriting its interface as defined
 *  in its setup() function and making its exports object the prototype
 *  of the exports object of this accessor.
 *  This will throw an exception if no getAccessorCode() function
 *  has been specified.
 *  @param accessorClass Fully qualified accessor class name, e.g. 'net/REST'.
 */
Accessor.prototype.extend = function(accessorClass) {
    // NOTE: This function should not need to be overriden by any host.
    if (!this.getAccessorCode) {
        throw('extend() is not supported by this swarmlet host.');
    }

    this.extending = accessorClass;
    
    var baseName = this.accessorName + '_' + accessorClass;
    
    // Provide the instance with the top-level accessor extending this new instance.
    var extendedInstance = instantiateAccessor(
            baseName, accessorClass, this.getAccessorCode, this.bindings, this.extendedBy);
            
    if (extendedInstance.exports) {
        if (typeof extendedInstance.exports.setup === 'function') {
            // Now invoke the setup method in the context of this accessor.
            extendedInstance.exports.setup.call(this);
        }
        // Make the superclass functions available through 'this.ssuper'
        // regardless of whether 'this' is the exports object or the accessor
        // this.
        // NOTE: The super keyword is built in to ECMA 6.
        // Since it isn't available in earlier versions, we provide an alternate:
        this.exports.ssuper = extendedInstance.exports;
        this.ssuper = extendedInstance;
        
        // Set up the prototype chain for inheritance.
        Object.setPrototypeOf(this.exports, extendedInstance.exports);
        Object.setPrototypeOf(this, extendedInstance);
    }
}

/** Default implementation of get(), which reads the current value of the input
 *  provided by provideInput(), or the default value if none has been provided,
 *  or null if neither has been provided.
 *  @param name The name of the input.
 */
Accessor.prototype.get = function(name) {
    var input = this.extendedBy.inputs[name];
    
    if (!input) {
        // Tolerate using get() to retrieve a parameter instead of input,
        // since names are required to be unique anyway. This ensure backward
        // compatibility with earlier models for the Ptolemy host, which used
        // get() for both inputs and parameters.
        input = this.extendedBy.parameters[name];
        if (!input) {
            throw('get(name): No input named ' + name);
        }
    }
    var value;
    var currentValue = input['currentValue'];
    if (typeof currentValue !== 'undefined' && currentValue !== null) {
        // If provideInput() has been called, return that value.
        value = input['currentValue'];
    } else {
        // Note that if both currentValue and value are null or undefined,
        // then the correct response is null.
        value = input['value'];
        if (typeof value === 'undefined') {
            value = null;
        }
    }
    // If necessary, convert the value to the match the type.
    value = convertType(value, input, name);
    return value;
}

/** Default implementation of getParameter(), which reads the current value of the
 *  parameter provided by setParameter(), or the default value if none has been provided,
 *  or null if neither has been provided.
 *  @param name The name of the parameter.
 */
Accessor.prototype.getParameter = function(name) {
    var parameter = this.extendedBy.parameters[name];
    if (!parameter) {
        throw('getParameter(name): No parameter named ' + name);
    }
    // If setParameter() has been called, return that value.
    if (parameter['currentValue']) {
        return parameter['currentValue'];
    }
    // If necessary, convert the value to the match the type.
    var value = parameter['value'];
    value = convertType(value, parameter, name);
    return value;
}

/** Implement the specified accessor interface, inheriting its inputs, outputs,
 *  and parameters as defined in its setup() function.
 *  This will throw an exception if no getAccessorCode() function
 *  has been specified.
 *  @param accessorClass Fully qualified accessor class name, e.g. 'net/REST'.
 */
Accessor.prototype.implement = function(accessorClass) {
    // NOTE: This function should not need to be overriden by any host.
    if (!this.getAccessorCode) {
        throw('implement() is not supported by this swarmlet host.');
    }

    this.implementedInterfaces.push(accessorClass);

    var interfaceName = this.accessorName + '_' + accessorClass;
    // Provide this as the context in which to invoke top-level functions.
    var extendedInstance = instantiateAccessor(
            interfaceName, accessorClass, this.getAccessorCode, this.bindings, this);
    if (extendedInstance.exports) {            
        // Now invoke the setup method in the context of this object.
        extendedInstance.exports.setup.call(this);
    }
}

/** Default implementation of the function to define an accessor input.
 *  Accessors that override this should probably invoke this default explicitly
 *  by referencing the prototype.
 *  @param name The name of the input.
 *  @param options The options for the input.
 */
Accessor.prototype.input = function(name, options) {
     // The input may have been previously defined in a base accessor.
     pushIfNotPresent(name, this.extendedBy.inputList);
     this.extendedBy.inputs[name] = mergeObjects(this.extendedBy.inputs[name], options);
}

/** Instantiate the specified accessor as a contained accessor.
 *  This will throw an exception if no getAccessorCode() function
 *  has been specified.
 *  @param instanceName A name to give to this instance, which will be prepended
 *   with the container name, separated by a period.
 *  @param accessorClass Fully qualified accessor class name, e.g. 'net/REST'.
 */
Accessor.prototype.instantiate = function(instanceName, accessorClass) {
    if (!this.getAccessorCode) {
        throw('instantiate() is not supported by this swarmlet host.');
    }
    // The only binding we want to inherit from this is require.
    // For all other functions, we want the default implementation
    // when instantiating the contained accessor.
    var insideBindings = {
        'require': this.require
    };
    var instanceName = this.accessorName + '.' + instanceName;
    var containedInstance = instantiateAccessor(
            instanceName, accessorClass, this.getAccessorCode, insideBindings);
    containedInstance.container = this;
    this.extendedBy.containedAccessors.push(containedInstance);
    return containedInstance;
}

/** Instantiate an accessor given its fully qualified name, a function to retrieve
 *  the code, and a require function to retrieve modules.
 *  The returned object will have a property '''accessorClass''' with the value of the
 *  name parameter passed in here.
 *  @param accessorName A name to give to the accessor instance.
 *  @param accessorClass Fully qualified accessor class, e.g. 'net/REST'.
 *  @param getAccessorCode A function that will retrieve the source code of a specified
 *   accessor (used to implement the extend() and implement() functions), or null if
 *   the host does not support accessors that extend other accessors.
 *  @param bindings The function bindings to be used by the accessor.
 *  @param extendedBy An optional argument specifying what accessor is extending or
 *   implementing this new instance. Pass null or no argument if it is not being extended.
 *   This should always be the top-level object doing the extending, not the immediate
 *   parent. This is used by the extend() function and should not normally be used by
 *   a user.
 */
function instantiateAccessor(
        accessorName, accessorClass, getAccessorCode, bindings, extendedBy) {
    var code = getAccessorCode(accessorClass);
    var instance = new Accessor(
            accessorName, code, getAccessorCode, bindings, extendedBy);
    instance.accessorClass = accessorClass;
    return instance;
}

/** Return the latest value produced on this output, or null if no
 *  output has been produced.
 *  @param name The name of the output.
 *  @return The latest value produced on this output.
 */
Accessor.prototype.latestOutput = function(name) {
    if (!this.extendedBy.outputs[name]) {
        throw('lastestOutput(): No output named ' + name);
    }
    return this.extendedBy.outputs[name].latestOutput;
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
            for (var property in second) {
                result[property] = second[property];
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

/** Default module identifier for accessors.
 *  CommonJS specification requires a 'module' object with an 'id' property
 *  and an optional 'uri' property. The spec says that module.id should be
 *  a valid argument to require(). Here, we are just given the JavaScript
 *  code, so we don't have any information about where it came from.
 *  Hence, we set a default id to 'unspecified', with the expectation that the
 *  code passed in will override that, and possibly the uri property.
 */
Accessor.prototype.module = {'id': 'unspecified'};

/** Default empty function to use if the function argument to
 *  addInputHandler is null.
 */
function nullHandlerFunction() {}

/** Define an accessor output.
 *  @param name The name of the output.
 *  @param options The options.
 */
Accessor.prototype.output = function(name, options) {
    // The output may have been previously defined in a base accessor.
    pushIfNotPresent(name, this.extendedBy.outputList);
    this.extendedBy.outputs[name] = mergeObjects(this.extendedBy.outputs[name], options);
}

/** Define an accessor parameter.
 *  @param name The name of the parameter.
 *  @param options The options.
 */
Accessor.prototype.parameter = function(name, options) {
    // The parameter may have been previously defined in a base accessor.
    pushIfNotPresent(name, this.extendedBy.parameterList);
    this.extendedBy.parameters[name] = mergeObjects(this.extendedBy.parameters[name], options);
}
    
/** Set an input of this accessor to the specified value.
 *  This function will perform conversions to the destination port type, if possible.
 *  For example, if a number is expected, but a string is provided, then it will
 *  attempt to parse the string to create a number.
 *  @param name The name of the input to set.
 *  @param value The value to set the input to.
 */
Accessor.prototype.provideInput = function(name, value) {
    var input = this.extendedBy.inputs[name];
    if (!input) {
        throw('provideInput(): Accessor has no input named ' + name);
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
        if (this.extendedBy.container) {
            this.extendedBy.container.scheduleEvent(this);
        }
    
        // If the input is connected on the inside, then provide the same input
        // to the destination(s).
        if (input.destinations) {
            for (var i = 0; i < input.destinations.length; i++) {
                var destination = input.destinations[i];
                if (typeof destination === 'string') {
                    // The destination is output port of this accessor.
                    this.extendedBy.send(destination, value);
                } else {
                    // The destination is an input port of a contained accessor.
                    destination.accessor.provideInput(destination.inputName, value);
                }
            }
        }
    }
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

/** Invoke any registered handlers for all inputs or for a specified input.
 *  Also invoke any handlers that have been registered to respond to any input,
 *  if there are any such handlers.
 *  If no input name is given, or the name is null, then invoke handlers for
 *  all inputs that have been provided with input value using provideInput()
 *  since the last time input handlers were invoked.
 *  Also invoke the fire function of the accessor, if one has been defined.
 *  If a handler throws an exception, then remove it from the registered
 *  handlers before rethrowing the exception.
 *  @param name The name of the input.
 */
Accessor.prototype.react = function(name) {
    // Allow further reactions to be scheduled by this reaction.
    this.extendedBy.reactRequestedAlready = false;
    
    // To avoid code duplication, define a local function.
    var thiz = this.extendedBy;
    var invokeSpecificHandler = function(name) {
        if (thiz.inputHandlers[name] && thiz.inputHandlers[name].length > 0) {
            for (var i = 0; i < thiz.inputHandlers[name].length; i++) {
                if (typeof thiz.inputHandlers[name][i] === 'function') {
                    // Input handlers functions are bound to the exports object.
                    try {
                        thiz.inputHandlers[name][i]();
                    } catch (exception) {
                        // Remove the input handler.
                        thiz.extendedBy.removeInputHandler(
                                thiz.extendedBy.inputHandlers[name][i].handle);
                        error('Exception occurred in input handler.'
                                + ' Handler has been removed: '
                                + exception);
                    }
                }
            }
        }
    };

    if (name) {
        // Handling a specific input.
        invokeSpecificHandler(name);
    } else {
        // No specific input has been given.
        for (var i = 0; i < this.extendedBy.inputList.length; i++) {
            name = this.extendedBy.inputList[i];
            if (this.extendedBy.inputs[name].pendingHandler) {
                this.extendedBy.inputs[name].pendingHandler = false;
                invokeSpecificHandler(name);
            }
        }
    }
    // Next, invoke handlers registered to handle any input.
    if (this.extendedBy.anyInputHandlers.length > 0) {
        for (var i = 0; i < this.extendedBy.anyInputHandlers.length; i++) {
            if (typeof this.extendedBy.anyInputHandlers[i] === 'function') {
                // Call input handlers in the context of the exports object.
                try {
                    this.extendedBy.anyInputHandlers[i]();
                } catch (exception) {
                    // Remove the input handler.
                    this.extendedBy.removeInputHandler(
                            this.extendedBy.anyInputHandlers[i].handle);
                    error('Exception occurred in input handler.'
                            + ' Handler has been removed: '
                            + exception);
                }
            }
        }
    }
    
    // Next, invoke react() on any contained accessors.
    if (this.extendedBy.containedAccessors && this.extendedBy.containedAccessors.length > 0) {
        // console.log('Composite is reacting with ' + this.extendedBy.eventQueue.length + ' events.');
        while (this.extendedBy.eventQueue && this.extendedBy.eventQueue.length > 0) {
            // Remove from the event queue the first accessor, which will now react.
            // It may add itself back in, if it sends to its own input. But in that
            // case, it should fire again immediately, so that is correct.
            var removed = this.extendedBy.eventQueue.splice(0, 1);
            removed[0].react();        
        }
    }

    // Next, invoke the fire() function.
    if (typeof this.exports.fire === 'function') {
        this.exports.fire();
    }
}

/** Remove the input handler with the specified handle, if it exists.
 *  @param handle The handle.
 *  @see #addInputHandler()
 */
Accessor.prototype.removeInputHandler = function(handle) {
    var handler = this.extendedBy.inputHandlersIndex[handle];
    if (handler) {
        if (handler.name) {
            if (this.extendedBy.inputHandlers[handler.name]
                    && this.extendedBy.inputHandlers[handler.name][handler.index]) {
                this.extendedBy.inputHandlers[handler.name][handler.index] = null;
            }
        } else {
            // Handler is set up to handle any input.
            if (this.extendedBy.anyInputHandlers[handler.index]) {
                this.extendedBy.anyInputHandlers[handler.index] = null;
            }
        }
        this.extendedBy.inputHandlersIndex[handle] = null;
    }
}

/** Default implement of the require function, which throws an exception stating
 *  that require is not supported.
 */    
Accessor.prototype.require = function() {
    throw 'This swarmlet host does not support require().';
}

/** Schedule a reaction of the specified contained accessor.
 *  This puts the accessor onto the event queue in priority order.
 *  This assumes that priorities are unique to each accessor.
 *  It is necessary for the react() function to be invoked for this event
 *  to be handled, so this function uses setTimeout(function, time) with time
 *  argument 0 to schedule a reaction. This ensures that the reaction occurs
 *  after the currently executing function has completely executed.
 *  @param accessor The accessor.
 */
Accessor.prototype.scheduleEvent = function(accessor) {
    var queue = this.extendedBy.eventQueue;
    // If we haven't already scheduled a react() since the last react(),
    // schedule it now.
    if (!this.extendedBy.reactRequestedAlready) {
        this.extendedBy.reactRequestedAlready = true;
        var self = this.extendedBy;
        setTimeout(function() { self.react(); }, 0);
    }
    if (!queue || queue.length === 0) {
        // Use a simple array as an event queue because almost all
        // sorted insertions will be at the end, and all extractions
        // will be at the beginning.
        this.extendedBy.eventQueue = [accessor];
        return;
    }
    // There are already items in the event queue.
    var myPriority = accessor.priority;
    if (typeof myPriority !== 'number') {
        throw('Accessor does not have a priority: '
                + accessor.accessorName
                + '. Perhaps initialize() is overridden?');
    }
    // Recall that a higher priority number means a lower priority.
    var theirPriority = queue[queue.length - 1].priority;
    if (myPriority > theirPriority) {
        // Simple case. Append to the end of the queue.
        queue.push(instance);
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
            queue.splice(i+1, 0, accessor);
            return;
        }
        if (myPriority == theirPriority) {
            // Already on the queue.
            return;
        }
    }
    // Final case: My priority is less than all in the queue.
    queue.splice(0, 0, accessor);
}

/** Send via an output. This default implementation invokes provideInput() on any
 *  connected inputs if there are any. It also records the output for retrieval
 *  by latestOutput().
 *  @param name The output name.
 *  @param value The output value.
 */
Accessor.prototype.send = function(name, value) {
    var output = this.extendedBy.outputs[name];
    if (!output) {
        // May be sending to my own input.
        var input = this.extendedBy.inputs[name];
        if (!input) {
            throw('send(name, value): No output or input named ' + name);
        }
        // Make the input available in the _next_ reaction.
        var thiz = this.extendedBy;
        setTimeout(function() {
            thiz.provideInput(name, value);
        }, 0);
        return;
    }
    // If necessary, convert the value to the match the type.
    value = convertType(value, output, name);

    output.latestOutput = value;
    // console.log('Sending output through ' + name + ': ' + value);
    if (output.destinations && output.destinations.length > 0) {
        for (var i = 0; i < output.destinations.length; i++) {
            var destination = output.destinations[i];
            if (typeof destination === 'string') {
                // The destination is output port of this accessor.
                if (this.extendedBy.container) {
                    this.extendedBy.container.send(destination, value);
                } else {
                    // If no other implementation of send() has been provided and
                    // there is no container, this used to produce to standard output.
                    // But this is not a good idea, because hosts should invoke this
                    // superclass function in their own send(), and this will produce
                    // a lot of noise on the console.
                    // console.log('Output named "' + name + '" produced: ' + value);
                }
            } else {
                // The destination is an input port of a contained accessor.
                destination.accessor.provideInput(destination.inputName, value);
            }
        }
    } else {
        // If no other implementation of send() has been provided and
        // there are no destinations, this used to produce to standard output.
        // But this is not a good idea, because hosts should invoke this
        // superclass function in their own send(), and this will produce
        // a lot of noise on the console.
        // console.log('Output named "' + name + '" produced: ' + value);
    }
}

/** Set the default value of an input. Note that unlike
 *  using send(), no input handler will be triggered.
 *  Also, unlike send(), the provided value will be persistent,
 *  in that once it is set, the host will store the new value along with the model.
 *  @param name The input name (a string).
 *  @param value The value to set.
 */
Accessor.prototype.setDefault = function(name, value) {
    if (typeof name !== 'string') {
        throw ('input argument is required to be a string. Got: ' + (typeof name));
    }
    var input = this.extendedBy.inputs[name];
    if (!input) {
        throw('setDefault(): Accessor has no input named ' + name);
    }
    value = convertType(value, input, name);
    input.value = value;
}

/** Set a parameter of the specified accessor to the specified value.
 *  @param name The name of the parameter to set.
 * @param value The value to set the parameter to.
 */
Accessor.prototype.setParameter = function(name, value) {
    var parameter = this.extendedBy.parameters[name];
    if (!parameter) {
        throw('setParameter(): Accessor has no parameter named ' + name);
    }
    // If necessary, convert the value to the match the type.
    value = convertType(value, parameter, name);

    parameter.currentValue = value;
}

////////////////////////////////////////////////////////////////////
//// Module variables.

/** Table of accessor instances indexed by their exports property.
 *  This allows us to retrieve the full accessor data structure, but to only
 *  expose to the user of this module the exports property of the accessor.
 *  Note that this host does not support removing accessors, so the instance
 *  will be around as long as the process exists.
 */
var _accessorInstanceTable = {};

////////////////////////////////////////////////////////////////////
//// Exports

exports.Accessor = Accessor;
exports.instantiateAccessor = instantiateAccessor;





