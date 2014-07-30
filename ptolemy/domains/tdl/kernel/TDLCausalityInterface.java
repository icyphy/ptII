/* Interface representing a dependency in the TDL domain.

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
package ptolemy.domains.tdl.kernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.CausalityInterfaceForComposites;
import ptolemy.actor.util.Dependency;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// TDLCausalityInterface

/**
 This class gives a specialized causality interface for TDL.

 @author Patricia Derler, Contributor: Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class TDLCausalityInterface extends CausalityInterfaceForComposites {

    /** Construct a causality interface for the specified actor.
     *  @param actor The actor for which this is a causality interface.
     *   This is required to be an instance of CompositeEntity.
     *  @param defaultDependency The default dependency of an output
     *   port on an input port.
     *  @exception IllegalArgumentException If the actor parameter is not
     *  an instance of CompositeEntity.
     */
    public TDLCausalityInterface(Actor actor, Dependency defaultDependency)
            throws IllegalArgumentException {
        super(actor, defaultDependency);
        if (!(actor instanceof CompositeEntity)) {
            throw new IllegalArgumentException("Cannot create an instance of "
                    + "CausalityInterfaceForComposites for "
                    + actor.getFullName() + ", which is not a CompositeEntity.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the minimum delay for this port. The minimum delay is
     *  the minimum model time delay between this port or any
     *  equivalent port and a source actor.
     *  @param port Port for which the minimum delay should be computed.
     *  @return the minimum delay for the specified port.
     *  @exception IllegalActionException Thrown if minimum delay
     *  cannot be computed, because e.g. equivalent ports cannot be
     *  computed.
     */
    public Dependency getMinimumDelay(IOPort port)
            throws IllegalActionException {
        if (_minimumDelays.get(port) == null) {
            _getMinimumDelay(port, new ArrayList());
        }
        return _minimumDelays.get(port);
    }

    /** Override the base class
     * Clear local variables.
     */
    public void wrapup() {
        _minimumDelays.clear();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Recursively compute the minimum delay. To avoid loops, remember visited ports.
     * @param port Port to compute minimum delay for.
     * @param visitedPorts Ports that have already been considered in the recursive computation.
     * @return Dependency describing the minimum Delay.
     * @exception IllegalActionException Thrown if minimum delay cannot be computed.
     */
    private Dependency _getMinimumDelay(IOPort port,
            Collection<IOPort> visitedPorts) throws IllegalActionException {
        if (visitedPorts.contains(port)) {
            return getDefaultDependency();
        } else {
            visitedPorts.add(port);
        }
        if (_minimumDelays.get(port) != null) {
            return _minimumDelays.get(port);
        }

        Dependency minimumDelay = getDefaultDependency().oPlusIdentity();
        if (port.isInput()) {
            // if port is input port of this actor
            if (this._actor.inputPortList().contains(port)) {
                // compute minimum delay in the container if not null
                NamedObj container = _actor.getContainer();
                if (container instanceof Actor) {
                    Director executiveDirector = _actor.getExecutiveDirector();
                    if (executiveDirector instanceof TDLModuleDirector) {
                        minimumDelay = ((TDLCausalityInterface) ((Actor) container)
                                .getCausalityInterface())._getMinimumDelay(
                                        port, visitedPorts);
                    } else {
                        minimumDelay = getDefaultDependency();
                    }
                } else {
                    minimumDelay = getDefaultDependency();
                }
            } else {
                // else if port is input port of any actor in this actor
                Collection<IOPort> equivalentPorts = ((Actor) port
                        .getContainer()).getCausalityInterface()
                        .equivalentPorts(port);
                for (IOPort equivalentPort : equivalentPorts) {
                    if (equivalentPort.isInput()) {
                        Collection<IOPort> sourcePorts = equivalentPort
                                .sourcePortList(); // contains only one item (?)
                        for (IOPort sourcePort : sourcePorts) {
                            Dependency dependency = _getMinimumDelay(
                                    sourcePort, visitedPorts);
                            // Coverity and FindBugs report: RV: Bad use of return value
                            // the results of Comparator.compareTo(Object) should not be compared
                            // with a specific value such as -1 or 1.
                            if (dependency.compareTo(minimumDelay) <= Dependency.LESS_THAN) {
                                minimumDelay = dependency;
                            }
                        }
                        if (sourcePorts.size() == 0) {
                            minimumDelay = getDefaultDependency()
                                    .oTimesIdentity();
                        }
                    }
                }
                // set minimum delay for all ports in this equivalence class
                for (IOPort equivalentPort : equivalentPorts) {
                    _minimumDelays.put(equivalentPort, minimumDelay);
                    Collection<IOPort> sourcePorts = equivalentPort
                            .sourcePortList(); // contains only one item (?)
                    for (IOPort sourcePort : sourcePorts) {
                        _minimumDelays.put(sourcePort, minimumDelay);
                    }
                }
            }
        } else if (port.isOutput()) {
            // if port is output port of this actor
            if (this._actor.outputPortList().contains(port)) {
                Collection<IOPort> sourcePorts = port.sourcePortList(); // contains only one item (?)
                for (IOPort actorOutputPort : sourcePorts) {
                    Dependency dependency = _getMinimumDelay(actorOutputPort,
                            visitedPorts);
                    if (dependency.compareTo(minimumDelay) <= Dependency.LESS_THAN) {
                        minimumDelay = dependency;
                    }
                }
                if (sourcePorts.size() == 0) {
                    minimumDelay = getDefaultDependency();
                }
            }
            // else if port is output port of any actor in this actor
            else {
                if (port.getContainer() instanceof CompositeActor) {
                    if (((CompositeActor) port.getContainer()).getDirector() != this._actor
                            .getDirector()
                            && ((CompositeActor) port.getContainer())
                            .getDirector()
                            .defaultDependency()
                            .equals(this._actor.getDirector()
                                    .defaultDependency())) {
                        Collection<IOPort> deepInputPorts = port
                                .deepInsidePortList();
                        for (IOPort inputPort : deepInputPorts) {
                            Dependency delay = _getMinimumDelay(inputPort,
                                    visitedPorts);
                            if (delay.compareTo(minimumDelay) <= Dependency.LESS_THAN) {
                                minimumDelay = delay;
                            }
                        }
                    } else {
                        this._actor.getDirector().defaultDependency();
                    }
                } else {
                    CausalityInterface causalityInterface = ((Actor) port
                            .getContainer()).getCausalityInterface();
                    Collection<IOPort> inputPorts = causalityInterface
                            .dependentPorts(port);
                    for (IOPort inputPort : inputPorts) {
                        Dependency delay = _getMinimumDelay(inputPort,
                                visitedPorts);
                        delay = delay.oTimes(causalityInterface.getDependency(
                                inputPort, port));
                        if (delay.compareTo(minimumDelay) <= Dependency.LESS_THAN) {
                            minimumDelay = delay;
                        }
                    }
                    if (inputPorts.size() == 0) {
                        minimumDelay = getDefaultDependency();
                    }
                }
            }
        }
        _minimumDelays.put(port, minimumDelay);
        return minimumDelay;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Cache of minimum delays that were already computed. */
    private Map<IOPort, Dependency> _minimumDelays = new HashMap<IOPort, Dependency>();
}
