/* Causality interface for FSM actors.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.domains.modal.modal;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.CausalityInterfaceForComposites;
import ptolemy.actor.util.Dependency;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// MirrorCausalityInterface

/**
This class infers a causality interface from causality interfaces provided
in the constructor and in the {@link #composeWith} method. For each of these
interfaces, this interface finds ports in its own actor that match the names
of those for the specified interfaces, and constructs dependencies that
are oPlus compositions of the dependencies in the specified interfaces for
ports with the same names. For equivalence classes, it merges the equivalence
classes so that if two ports are equivalent in any of the provided causality
interfaces, then the corresponding (same named) ports in the actor are also
equivalent.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating Yellow (eal)
@Pt.AcceptedRating Red (eal)
 */
public class MirrorCausalityInterface extends CausalityInterfaceForComposites {

    /** Construct a causality interface that mirrors the specified causality
     *  interface.
     *  @param actor The actor for which this is an interface.
     *  @param causality The interface to mirror.
     * @exception IllegalArgumentException If the actor parameter is not
     * an instance of CompositeEntity.
     */
    public MirrorCausalityInterface(Actor actor, CausalityInterface causality)
            throws IllegalArgumentException {
        super(actor, causality.getDefaultDependency());
        _composedInterfaces.add(causality);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add the specified causality interface for the specified actor.
     *  @param causality The interface to compose with the one specified
     *   in the constructor.
     */
    public void composeWith(CausalityInterface causality) {
        _composedInterfaces.add(causality);
    }

    /** Return the dependency between the specified input port
     *  and the specified output port.  This is done by checking
     *  the guards and actions of all the transitions.
     *  When called for the first time since a change in the model
     *  structure, this method performs the complete analysis of
     *  the FSM and caches the result. Subsequent calls just
     *  look up the result.
     *  @param input The input port.
     *  @param output The output port, or null to update the
     *   dependencies (and record equivalence classes) without
     *   requiring there to be an output port.
     *  @return The dependency between the specified input port
     *   and the specified output port, or null if a null output
     *   is port specified.
     *  @exception IllegalActionException If a guard expression cannot be parsed.
     */
    @Override
    public Dependency getDependency(IOPort input, IOPort output)
            throws IllegalActionException {
        // If the dependency is not up-to-date, then update it.
        long workspaceVersion = ((NamedObj) _actor).workspace().getVersion();
        if (_dependencyVersion != workspaceVersion) {
            // Need to update dependencies. The cached version
            // is obsolete.
            try {
                ((NamedObj) _actor).workspace().getReadAccess();
                _reverseDependencies = new HashMap<IOPort, Map<IOPort, Dependency>>();
                _forwardDependencies = new HashMap<IOPort, Map<IOPort, Dependency>>();

                // Iterate over all the associated interfaces.
                for (CausalityInterface causality : _composedInterfaces) {
                    List<IOPort> mirrorInputs = causality.getActor()
                            .inputPortList();
                    for (IOPort mirrorInput : mirrorInputs) {
                        Port localInput = ((Entity) _actor).getPort(mirrorInput
                                .getName());
                        if (!(localInput instanceof IOPort)) {
                            throw new IllegalActionException(_actor,
                                    mirrorInput.getContainer(),
                                    "No matching port with name "
                                            + mirrorInput.getName());
                        }
                        // The localInput may not be an input port...
                        // It may have been an output port that the FSMActor controller
                        // also has as an input port. But we don't want to set a forward
                        // depedency in this case.
                        if (!((IOPort) localInput).isInput()) {
                            continue;
                        }
                        Map<IOPort, Dependency> forwardMap = _forwardDependencies
                                .get(localInput);
                        if (forwardMap == null) {
                            forwardMap = new HashMap<IOPort, Dependency>();
                            _forwardDependencies.put((IOPort) localInput,
                                    forwardMap);
                        }
                        for (IOPort dependentOutput : causality
                                .dependentPorts(mirrorInput)) {
                            Port localOutput = ((Entity) _actor)
                                    .getPort(dependentOutput.getName());
                            if (!(localOutput instanceof IOPort)) {
                                throw new IllegalActionException(_actor,
                                        mirrorInput.getContainer(),
                                        "No matching port with name "
                                                + mirrorInput.getName());
                            }
                            Dependency dependency = causality.getDependency(
                                    mirrorInput, dependentOutput);
                            forwardMap.put((IOPort) localOutput, dependency);
                            // Now handle the reverse dependencies.
                            Map<IOPort, Dependency> backwardMap = _reverseDependencies
                                    .get(localOutput);
                            if (backwardMap == null) {
                                backwardMap = new HashMap<IOPort, Dependency>();
                                _reverseDependencies.put((IOPort) localOutput,
                                        backwardMap);
                            }
                            backwardMap.put((IOPort) localInput, dependency);
                        }
                    }
                }
                // Next do equivalence classes.
                // We iterate over the input ports, and for each one,
                // find the ports for which it has equivalents in any
                // associated causality.
                _equivalenceClasses = new HashMap<IOPort, Collection<IOPort>>();
                Collection<IOPort> localInputs = _actor.inputPortList();
                for (IOPort localInput : localInputs) {
                    Collection<IOPort> equivalences = _equivalenceClasses
                            .get(localInput);
                    if (equivalences != null) {
                        // This input port is done.
                        continue;
                    }
                    equivalences = new HashSet<IOPort>();
                    // Iterate over all the associated interfaces.
                    for (CausalityInterface causality : _composedInterfaces) {
                        IOPort mirrorInput = (IOPort) ((Entity) causality
                                .getActor()).getPort(localInput.getName());
                        if (mirrorInput == null) {
                            throw new IllegalActionException(_actor,
                                    localInput, "Expected matching port in "
                                            + causality.getActor()
                                                    .getFullName());
                        }
                        equivalences.addAll(_localMirrors(causality
                                .equivalentPorts(mirrorInput)));
                    }
                    // Set the equivalence class for all ports in the set.
                    for (IOPort equivalentPort : equivalences) {
                        _equivalenceClasses.put(equivalentPort, equivalences);
                    }
                }
            } finally {
                ((NamedObj) _actor).workspace().doneReading();
            }
            _dependencyVersion = workspaceVersion;
        }
        if (output == null) {
            return null;
        }
        Map<IOPort, Dependency> inputMap = _forwardDependencies.get(input);
        if (inputMap != null) {
            Dependency result = inputMap.get(output);
            if (result != null) {
                return result;
            }
        }
        // If there is no recorded dependency, then reply
        // with the additive identity (which indicates no
        // dependency).
        return _defaultDependency.oPlusIdentity();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the local ports whose names match the ports in the
     *  specified collection.
     *  @param ports A collection of ports.
     *  @exception IllegalActionException If no matching port is found.
     */
    private Collection<IOPort> _localMirrors(Collection<IOPort> ports)
            throws IllegalActionException {
        Set<IOPort> result = new HashSet<IOPort>();
        for (IOPort port : ports) {
            IOPort localPort = (IOPort) ((Entity) _actor).getPort(port
                    .getName());
            if (localPort == null) {
                throw new IllegalActionException(port.getContainer(), port,
                        "Expected matching port in " + _actor.getFullName());
            }
            result.add(localPort);
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** The set of causality interfaces that this one composes. */
    private Set<CausalityInterface> _composedInterfaces = new HashSet<CausalityInterface>();
}
