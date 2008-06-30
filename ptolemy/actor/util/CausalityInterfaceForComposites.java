/* Interface representing a dependency between ports.

 Copyright (c) 2003-2006 The Regents of the University of California.
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
import java.util.Map;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.kernel.CompositeEntity;


//////////////////////////////////////////////////////////////////////////
//// CausalityInterfaceForComposites

/**
 This class elaborates its base class by providing an algorithm for inferring
 the causality interface of a composite actor from the causality interfaces
 of its component actors and their interconnection topology.

 @author Edward A. Lee
 @version $Id: CausalityInterfaceForComposites.java 47513 2007-12-07 06:32:21Z cxh $
 @since Ptolemy II 7.2
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class CausalityInterfaceForComposites extends CausalityInterface {
    
    /** Construct a causality interface for the specified actor.
     *  @param actor The actor for which this is a causality interface.
     *   This is required to be an instance of CompositeEntity.
     *  @param defaultDependency The default dependency of an output
     *   port on an input port.
     */
    public CausalityInterfaceForComposites(
            Actor actor, Dependency defaultDependency) 
            throws IllegalArgumentException {
        super(actor, defaultDependency);
        if (!(actor instanceof CompositeEntity)) {
            throw new IllegalArgumentException(
                    "Cannot create an instance of " +
                    "CausalityInterfaceForComposites for " +
                    actor.getFullName()
                    +", which is not a CompositeEntity.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the dependency between the specified input port
     *  and the specified output port.  This is done by traversing
     *  the network of actors from the input ports. For each output
     *  port reachable from an input port, its dependency on the
     *  input port is determined by composing the dependencies along
     *  all paths from the input port to the output port using
     *  oPlus() and oTimes() operators of the dependencies.
     *  For any output port that is not reachable from an input
     *  port, the dependency on that input port is set to
     *  the oPlusIdentity() of the default dependency given
     *  in the constructor.
     *  <p>
     *  When called for the first time since a change in the model
     *  structure, this method performs the complete analysis of
     *  the graph and caches the result. Subsequent calls just
     *  look up the result. Note that the complete analysis
     *  can be quite expensive. For each input port, it traverses
     *  the graph to find all ports reachable from that input port,
     *  and tracks the dependencies. In the worst case, the
     *  complexity can be N*M^2, where N is the number of
     *  input ports and M is the total number of ports in the
     *  composite (including the ports of all contained actors).
     *  The algorithm used, however, is optimized for typical
     *  Ptolemy II models, so in most cases the algorithm completes
     *  in time on the order of N*D, where D is the length of
     *  the longest chain of ports from an input port to an
     *  output port.
     *  @param input The input port.
     *  @param output The output port.
     *  @return The dependency between the specified input port
     *   and the specified output port.
     */
    public Dependency getDependency(IOPort input, IOPort output) {
        // Cast is safe because this is checked in the constructor
        CompositeEntity actor = (CompositeEntity)_actor;
        
        // If the dependency is not up-to-date, then update it.
        long workspaceVersion = actor.workspace().getVersion();
        if (_dependencyVersion != workspaceVersion) {
            // Need to update dependencies. The cached version
            // is obsolete.
            try {
                actor.workspace().getReadAccess();
                _dependencies = new HashMap<IOPort,Map<IOPort,Dependency>>();
                Iterator inputPorts = _actor.inputPortList().iterator();
                while (inputPorts.hasNext()) {
                    IOPort inputPort = (IOPort)inputPorts.next();
                    // Construct a map of dependencies from this inputPort
                    // to all reachable ports.
                    Map<IOPort,Dependency> map = new HashMap<IOPort,Dependency>();
                    _dependencies.put(inputPort, map);
                    Collection portsToProcess = inputPort.insideSinkPortList();
                    // Set the initial dependency of all the portsToProcess.
                    Iterator ports = portsToProcess.iterator();
                    while (ports.hasNext()) {
                        IOPort port = (IOPort)ports.next();
                        map.put(port, _defaultDependency.oTimesIdentity());
                    }
                    if (!portsToProcess.isEmpty()) {
                        _setDependency(
                                inputPort,
                                map,
                                portsToProcess);
                    }
                }
            } finally {
                actor.workspace().doneReading();
            }
            _dependencyVersion = workspaceVersion;
        }
        Map<IOPort,Dependency> inputMap = _dependencies.get(input);
        if (inputMap == null) {
            // This should not occur. Throw an exception?
            return _defaultDependency;
        }
        Dependency result = inputMap.get(output);
        if (result == null) {
            // If there is no entry for the output, then there
            // is no path from the input to this output port.
            return _defaultDependency.oPlusIdentity();
        }
        return result;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Set the dependency from the specified inputPort to all
     *  ports that are reachable via the portsToProcess ports.
     *  The results are stored in the specified map.
     *  @param inputPort An input port of this actor.
     *  @param map A map of dependencies from this input port to all reachable ports,
     *   built by this method. The map is required to contain all ports in portsToProcess
     *   on entry.
     *  @param portsToProcess Ports connected to the input port directly or indirectly.
     */
    private void _setDependency(
            IOPort inputPort, 
            Map<IOPort,Dependency> map, 
            Collection portsToProcess) {
        Set<IOPort> portsToProcessNext = new HashSet<IOPort>();
        Iterator ports = portsToProcess.iterator();
        while (ports.hasNext()) {
            IOPort port = (IOPort)ports.next();
            // The argument map is required to contain this dependency.
            Dependency dependency = map.get(port);
            // Next, check whether we have gotten to an output port of this actor.
            if (port.getContainer() == _actor) {
                // Port is owned by this actor. If it is
                // output port, then it is dependent on this
                // input port by the given dependency. It should
                // not normally be an input port, but we tolerate
                // that here in case some domain uses it someday.
                // In that latter case, there is no dependency.
                if (port.isOutput()) {
                    // We have a path from an input to an output.
                    // Record the dependency.
                    _recordDependency(port, map, dependency);
                }
            } else {
                // The port presumably belongs to an actor inside this actor.
                _recordDependency(port, map, dependency);
                // Next record the dependency that all output ports of
                // the actor containing the port have on the input port.
                Actor actor = (Actor)port.getContainer();
                CausalityInterface causality = actor.getCausalityInterface();
                Iterator outputPorts = actor.outputPortList().iterator();
                while (outputPorts.hasNext()) {
                    IOPort outputPort = (IOPort)outputPorts.next();
                    Dependency actorDependency = causality.getDependency(port, outputPort);
                    Dependency newDependency = dependency.oTimes(actorDependency);
                    if (_recordDependency(outputPort, map, newDependency)) {
                        // Dependency of this output port has been set or
                        // changed.  Add ports to the set of ports to be
                        // processed next.
                        Collection sinkPorts = outputPort.sinkPortList();
                        Iterator sinkPortsIterator = sinkPorts.iterator();
                        while (sinkPortsIterator.hasNext()) {
                            IOPort sinkPort = (IOPort)sinkPortsIterator.next();
                            _recordDependency(sinkPort, map, newDependency);
                            if (sinkPort.getContainer() != _actor) {
                                // Port is not owned by this actor.
                                // Further processing will be needed.
                                portsToProcessNext.add(sinkPort);
                            }
                        }
                    }
                }
            }
        }
        if (!portsToProcessNext.isEmpty()) {
            _setDependency(inputPort, map, portsToProcessNext);
        }
    }
    
    /** Record a dependency from the specified inputPort to the
     *  specified output port. If there was a prior dependency already
     *  that was less than this one, then update the dependency
     *  using its oPlus() method.
     *  Return true if the dependency was newly set or modified from
     *  a previously recorded dependency. Return false if no change
     *  was made to a previous dependency.
     *  @param inputPort The source port.
     *  @param port The destination port.
     *  @param dependency The dependency map for ports reachable from the input port.
     *  @return True if the dependency was changed.
     */
    private boolean _recordDependency(
            IOPort port,
            Map<IOPort,Dependency> map,
            Dependency dependency) {
        Dependency priorDependency = map.get(port);
        if (priorDependency == null) {
            map.put(port, dependency);
            return true;
        }
        // There is a prior dependency.
        Dependency newDependency = priorDependency.oPlus(dependency);
        if (!newDependency.equals(priorDependency)) {
            // Update the dependency.
            map.put(port, newDependency);
            return true;
        }
        // No change made to the dependency.
        return false;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** Computed dependencies. */
    private Map<IOPort,Map<IOPort,Dependency>> _dependencies;

    /** The workspace version where the dependency was last updated. */
    private long _dependencyVersion;
}
