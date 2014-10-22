/* Causality interface where all outputs depend on all inputs.

 Copyright (c) 2008-2014 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.actor.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// DefaultCausalityInterface

/**
 This class provides causality interfaces for actor networks as described
 in the paper "Causality Interfaces for Actor Networks" by Ye Zhou and
 Edward A. Lee, ACM Transactions on Embedded Computing Systems (TECS),
 April 2008, as available as <a href="http://www.eecs.berkeley.edu/Pubs/TechRpts/2006/EECS-2006-148.pdf">
 Technical Report No. UCB/EECS-2006-148</a>,
 November 16, 2006.  Specifically, this class represents a simple
 default causality interface where every output port depends on every
 input port, unless {@link #removeDependency(IOPort, IOPort)} has
 been called to prune the dependencies. In the latter case, the dependencies
 are pruned to not include the ones that were specified in that call.
 <p>
 In all cases, if there is any PortParameter in the actor, then all
 input ports become members of the same equivalence class, regardless
 of what dependencies have been pruned using
 {@link #removeDependency(IOPort, IOPort)}. The reason for this is
 that any output, present or future, may depend on the values at
 such port parameters. In particular, it is necessary for inputs
 on these port parameters to be present when any other input is
 processed because it affects the parameters of the actor.
 <p>
 Causality interfaces represent dependencies between input and output ports
 of an actor and can be used to perform scheduling or static analysis
 on actor models.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class DefaultCausalityInterface implements CausalityInterface {

    /** Construct a causality interface for the specified actor.
     *  @param actor The actor for which this is a causality interface.
     *  @param defaultDependency The default dependency of an output
     *   port on an input port.
     */
    public DefaultCausalityInterface(Actor actor, Dependency defaultDependency) {
        _actor = actor;
        _defaultDependency = defaultDependency;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the dependency that the specified output port has
     *  on the specified input port to represent a time
     *  delay with the specified value and superdense time index.
     *  This method is adaptive to the type
     *  of Dependency provided in the constructor. For example, if the
     *  constructor provides a BooleanDependency, then
     *  this method defines the dependency to be TRUE if
     *  the timeDelay is 0.0 and the index is 0, and otherwise it should
     *  be FALSE. If the Dependency is a RealDependency, then it uses
     *  the <i>timeDelay</i> argument to define the dependency.
     *  If it is a SuperdenseDependency, then it uses both the <i>timeDelay</i>
     *  and the <i>index</i>.
     *  @param input The input port.
     *  @param output The output port with a time delay dependency on the
     *   input port.
     *  @param timeDelay The time delay.
     *  @param index The superdense time index.
     */
    @Override
    public void declareDelayDependency(IOPort input, IOPort output,
            double timeDelay, int index) {
        if (_defaultDependency instanceof SuperdenseDependency) {
            if (timeDelay != 0.0 || index != 0) {
                _removeDependency(input, output);
            }
            // Store the time delay.
            Dependency dependency = SuperdenseDependency.valueOf(timeDelay,
                    index);
            if (_delayDependencies == null) {
                _delayDependencies = new HashMap<IOPort, Map<IOPort, Dependency>>();
            }
            Map<IOPort, Dependency> entry = new HashMap<IOPort, Dependency>();
            entry.put(output, dependency);
            _delayDependencies.put(input, entry);
        } else if (_defaultDependency instanceof RealDependency) {
            if (timeDelay != 0.0) {
                _removeDependency(input, output);
            }
            // Store the time delay.
            Dependency dependency = RealDependency.valueOf(timeDelay);
            if (_delayDependencies == null) {
                _delayDependencies = new HashMap<IOPort, Map<IOPort, Dependency>>();
            }
            Map<IOPort, Dependency> entry = new HashMap<IOPort, Dependency>();
            entry.put(output, dependency);
            _delayDependencies.put(input, entry);
        } else { // the dependency is a BooleanDependency.
            _removeDependency(input, output);
        }
    }

    /** Return a collection of the ports in this actor that depend on
     *  or are depended on by the specified port. A port X depends
     *  on a port Y if X is an output and Y is an input and
     *  getDependency(X,Y) returns oTimesIdentity()
     *  of the default dependency specified in the constructor.
     *  <p>
     *  This base class presumes (but does not check) that the
     *  argument is a port contained by the associated actor.
     *  By default,
     *  if the actor is an input, then it returns a collection of
     *  all the outputs. If the actor is output, then it returns
     *  a collection of all the inputs.
     *  However, if {@link #removeDependency(IOPort, IOPort)}
     *  has been called, then it prunes the results to not include
     *  ports where the dependency has been explicitly removed.
     *  <p>
     *  Derived classes may override this, but they may need to
     *  also override {@link #getDependency(IOPort, IOPort)}
     *  and {@link #equivalentPorts(IOPort)} to be consistent.
     *  @param port The port to find the dependents of.
     *  @return a collection of ports that depend on or are depended on
     *  by the specified port.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public Collection<IOPort> dependentPorts(IOPort port)
            throws IllegalActionException {
        if (_forwardPrunedDependencies == null) {
            // removeDependency() has not been called, so this is the simple case.
            if (port.isOutput()) {
                if (port.isInput()) {
                    // Port is both input and output.
                    HashSet<IOPort> result = new HashSet<IOPort>();
                    result.addAll(_actor.inputPortList());
                    result.addAll(_actor.outputPortList());
                    return result;
                }
                // Port is output and not input.
                return _actor.inputPortList();
            } else if (port.isInput()) {
                // Port is input and not output.
                return _actor.outputPortList();
            } else {
                // Port is neither input nor output.
                return _EMPTY_COLLECTION;
            }
        }
        // removeDependency() has been called.  Prune results.
        Collection<IOPort> result;
        if (port.isOutput()) {
            if (port.isInput()) {
                // Port is both input and output.
                result = new HashSet<IOPort>();
                result.addAll(_actor.inputPortList());
                result.addAll(_actor.outputPortList());
                Set<IOPort> inputPorts = _backwardPrunedDependencies.get(port);
                if (inputPorts != null) {
                    result.removeAll(inputPorts);
                }
                Set<IOPort> outputPorts = _forwardPrunedDependencies.get(port);
                if (outputPorts != null) {
                    result.removeAll(outputPorts);
                }
            } else {
                // Port is output and not input.
                Set<IOPort> inputPorts = _backwardPrunedDependencies.get(port);
                if (inputPorts == null) {
                    // No dependencies have been pruned for this output port.
                    result = _actor.inputPortList();
                } else {
                    result = new HashSet<IOPort>();
                    result.addAll(_actor.inputPortList());
                    result.removeAll(inputPorts);
                }
            }
        } else if (port.isInput()) {
            // Port is input and not output.
            Set<IOPort> outputPorts = _forwardPrunedDependencies.get(port);
            if (outputPorts == null) {
                // No dependencies have been pruned for this input port.
                result = _actor.outputPortList();
            } else {
                result = new HashSet<IOPort>();
                result.addAll(_actor.outputPortList());
                result.removeAll(outputPorts);
            }
        } else {
            result = new HashSet<IOPort>();
        }
        return result;
    }

    /** Return a collection of the input ports in this actor that are
     *  in the same equivalence class with the specified input
     *  port. This base class returns a collection of all
     *  the input ports of the associated actor, unless
     *  removeDependencies() has been called. In the latter
     *  case, it constructs the equivalence class based on
     *  the remaining dependencies on output ports, unless
     *  there is an instance of PortParameter in the actor.
     *  In that case, it again returns a collection of all
     *  the input ports.
     *  <p>
     *  If derived classes override this, they may also
     *  need to override {@link #getDependency(IOPort,IOPort)}
     *  and {@link #dependentPorts(IOPort)} to be consistent.
     *  The returned result should always include the specified input port.
     *  <p>
     *  An equivalence class is defined as follows.
     *  If input ports X and Y each have a dependency on any
     *  common port or on two equivalent ports, or they each can
     *  affect the state of the associated actor, then they
     *  are in an equivalence class. That is,
     *  there is a causal dependency. They are also in
     *  the same equivalence class if there is a port Z
     *  in an equivalence class with X and in an equivalence
     *  class with Y. If the actor has any instance of
     *  ParameterPort, then all input ports are in the same
     *  equivalence class. Otherwise, they are not in the same
     *  equivalence class.
     *  @param input The port to find the equivalence class of.
     *  @return set of the input ports in this actor that are
     *  in an equivalence class with the specified input.
     *  @exception IllegalArgumentException If the argument is not
     *   contained by the associated actor.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public Collection<IOPort> equivalentPorts(IOPort input)
            throws IllegalActionException {
        if (input.getContainer() != _actor || !input.isInput()) {
            throw new IllegalArgumentException(
                    "equivalentPort() called with argument "
                            + input.getFullName()
                            + " that is not an input port for "
                            + _actor.getFullName());
        }
        if (_forwardPrunedDependencies == null) {
            // removeDependencies() has not been called, so this is the
            // simple case.
            return _actor.inputPortList();
        }
        // FIXME: Should the result be cached?
        // If removeDependencies() has been called, finding the equivalent
        // ports is rather complicated. Presumably, this can only change
        // if ports are added or removed from the actor, or if
        // removeDependency() is called.
        List<IOPort> inputs = _actor.inputPortList();
        // If there is an instance of ParameterPort, then return the
        // whole collection of input ports.
        for (IOPort actorInput : inputs) {
            if (actorInput instanceof ParameterPort) {
                return _actor.inputPortList();
            }
        }
        List<IOPort> outputs = _actor.outputPortList();
        // Deal with simple cases first.
        if (inputs.size() == 1 || outputs.size() == 0) {
            // There is only one input port, or there are
            // no output ports, so we are done.
            return inputs;
        }
        HashSet<IOPort> result = new HashSet<IOPort>();
        HashSet<IOPort> dependents = new HashSet<IOPort>();
        _growDependencies(input, result, dependents);
        return result;
    }

    /** Return the actor for which this is a dependency.
     *  @return The actor for which this is a dependency.
     */
    @Override
    public Actor getActor() {
        return _actor;
    }

    /** Return the default dependency specified in the constructor.
     *  @return The default dependency.
     */
    @Override
    public Dependency getDefaultDependency() {
        return _defaultDependency;
    }

    /** Return the dependency between the specified input port
     *  and the specified output port.  This base class returns
     *  the default dependency if the first port is an input
     *  port owned by this actor and the second one is an output
     *  port owned by this actor. Otherwise, it returns the
     *  additive identity of the dependency. Also, if
     *  {@link #removeDependency(IOPort, IOPort)} has been
     *  called with the same two specified ports, then
     *  this method will return the additive identity.
     *  <p>
     *  Derived classes should override this method to provide
     *  actor-specific dependency information. If they do so,
     *  then they may also need to override {@link #equivalentPorts(IOPort)}
     *  and {@link #dependentPorts(IOPort)} to be consistent.
     *  @param input The input port.
     *  @param output The output port.
     *  @return The dependency between the specified input port
     *   and the specified output port.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public Dependency getDependency(IOPort input, IOPort output)
            throws IllegalActionException {
        // If there is a recorded delay dependency, return that.
        if (_delayDependencies != null) {
            Map<IOPort, Dependency> entry = _delayDependencies.get(input);
            if (entry != null) {
                Dependency result = entry.get(output);
                if (result != null) {
                    return result;
                }
            }
        }
        if (input.isInput() && input.getContainer() == _actor
                && output.isOutput() && output.getContainer() == _actor) {
            if (_forwardPrunedDependencies != null) {
                Set<IOPort> outputPorts = _forwardPrunedDependencies.get(input);
                if (outputPorts != null && outputPorts.contains(output)) {
                    // This dependency has been pruned.
                    return _defaultDependency.oPlusIdentity();
                }
            }
            return _defaultDependency;
        }
        return _defaultDependency.oPlusIdentity();
    }

    /** Return a description of the causality interfaces.
     *  @return A description of the causality interfaces.
     */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        Iterator inputPorts = _actor.inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            result.append(inputPort.getName());
            result.append(" has output dependencies as follows:\n");
            Iterator outputPorts = _actor.outputPortList().iterator();
            while (outputPorts.hasNext()) {
                IOPort outputPort = (IOPort) outputPorts.next();
                result.append("   ");
                result.append(outputPort.getName());
                result.append(": ");
                try {
                    result.append(getDependency(inputPort, outputPort));
                } catch (IllegalActionException e) {
                    result.append("EXCEPTION: ");
                    result.append(e);
                }
                result.append("\n");
            }
        }
        return result.toString();
    }

    /** Remove the dependency that the specified output port has
     *  on the specified input port. Specifically, calling this
     *  method ensures that getDependency(inputPort, outputPort)
     *  will return defaultDependency.oPlusIdentity() instead
     *  of the default defaultDependency.oTimesIdentity().
     *  It also adjusts what is returned by
     *  {@link #equivalentPorts(IOPort)} and
     *  {@link #dependentPorts(IOPort)}.
     *  @see #getDependency(IOPort, IOPort)
     *  @param inputPort The input port.
     *  @param outputPort The output port that does not depend on the
     *   input port.
     */
    @Override
    public void removeDependency(IOPort inputPort, IOPort outputPort) {
        if (_defaultDependency instanceof SuperdenseDependency) {
            // Set time delay to infinity.
            Dependency dependency = SuperdenseDependency.OPLUS_IDENTITY;
            if (_delayDependencies == null) {
                _delayDependencies = new HashMap<IOPort, Map<IOPort, Dependency>>();
            }
            Map<IOPort, Dependency> entry = new HashMap<IOPort, Dependency>();
            entry.put(outputPort, dependency);
            _delayDependencies.put(inputPort, entry);
        }
        if (_defaultDependency instanceof RealDependency) {
            // Set time delay to infinity.
            Dependency dependency = RealDependency.OPLUS_IDENTITY;
            if (_delayDependencies == null) {
                _delayDependencies = new HashMap<IOPort, Map<IOPort, Dependency>>();
            }
            Map<IOPort, Dependency> entry = new HashMap<IOPort, Dependency>();
            entry.put(outputPort, dependency);
            _delayDependencies.put(inputPort, entry);
        }
        _removeDependency(inputPort, outputPort);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** If the input port is already in the inputs set, do nothing
     *  and return. Otherwise, add the input port to the inputs set,
     *  and its output dependents to the outputs set. If any of those
     *  output dependents were not already in the outputs set,
     *  add them, and then recursively invoke this same method
     *  on all input ports that depend on those outputs.
     *  @param input The input port.
     *  @param inputs The set of inputs to which input is added if
     *  it is not already present.
     *  @param outputs The set of output dependents to which the output
     *  dependents are added if input was not in the inputs set.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    protected void _growDependencies(IOPort input, Set<IOPort> inputs,
            Set<IOPort> outputs) throws IllegalActionException {
        if (!inputs.contains(input)) {
            inputs.add(input);
            for (IOPort output : dependentPorts(input)) {
                // If the output has already been handled, skip it.
                if (!outputs.contains(output)) {
                    outputs.add(output);
                    for (IOPort anotherInput : dependentPorts(output)) {
                        _growDependencies(anotherInput, inputs, outputs);
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The associated actor. */
    protected Actor _actor;

    /** A record of removed dependencies from output to input, if any.
     *  In this case, if the dependency between the ports are anything
     *  other than the oTimesIdentity, then the dependency is removed. */
    protected Map<IOPort, Set<IOPort>> _backwardPrunedDependencies;

    /** Empty collection for use by dependentPort(). */
    protected static final Collection<IOPort> _EMPTY_COLLECTION = new LinkedList<IOPort>();

    /** The default dependency of an output port on an input port. */
    protected Dependency _defaultDependency;

    /** A record of delay dependencies from input to output, if any. */
    protected Map<IOPort, Map<IOPort, Dependency>> _delayDependencies;

    /** A record of removed dependencies from input to output, if any.
     *  In this case, if the dependency between the ports are anything
     *  other than the oTimesIdentity, then the dependency is removed. */
    protected Map<IOPort, Set<IOPort>> _forwardPrunedDependencies;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** For the benefit of the dependentPorts() and equivalentPorts()
     *  methods, remove the dependency that the input has on the output.
     *  @param inputPort The input.
     *  @param outputPort The output.
     */
    private void _removeDependency(IOPort inputPort, IOPort outputPort) {
        if (_forwardPrunedDependencies == null) {
            _forwardPrunedDependencies = new HashMap<IOPort, Set<IOPort>>();
            _backwardPrunedDependencies = new HashMap<IOPort, Set<IOPort>>();
        }
        Set<IOPort> outputPorts = _forwardPrunedDependencies.get(inputPort);
        if (outputPorts == null) {
            outputPorts = new HashSet<IOPort>();
            _forwardPrunedDependencies.put(inputPort, outputPorts);
        }
        outputPorts.add(outputPort);

        Set<IOPort> inputPorts = _backwardPrunedDependencies.get(outputPort);
        if (inputPorts == null) {
            inputPorts = new HashSet<IOPort>();
            _backwardPrunedDependencies.put(outputPort, inputPorts);
        }
        inputPorts.add(inputPort);
    }
}
