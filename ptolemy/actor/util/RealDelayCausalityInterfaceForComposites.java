package ptolemy.actor.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// RealDelayCausalityInterfaceForComposites

/**
 * This class extends the causality interface for composites to compute minimum
 * delays for ports. A minimum delay describes the minimum model time delay between
 * all ports in an equivalence class and source actors. Minimum delays can only be
 * computed for RealDependencies.
 * @author Patricia Derler
 *
 */
public class RealDelayCausalityInterfaceForComposites extends
        CausalityInterfaceForComposites {

    /** Construct a causality interface for the specified actor.
     *  @param actor The actor for which this is a causality interface.
     *   This is required to be an instance of CompositeEntity.
     *  @param defaultDependency The default dependency of an output
     *   port on an input port.
     */    
    public RealDelayCausalityInterfaceForComposites(
            Actor actor, Dependency defaultDependency) 
            throws IllegalArgumentException {
        super(actor, defaultDependency);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Return the minimum delay for this port. The minimum delay is the minimum
     * model time delay between this port or any equivalent port and a source actor. 
     *  @param port Port for which the minimum delay should be computed.
     *  @return 
     *  @exception IllegalActionException Thrown if minimum delay cannot be computed,
     *   because e.g. equivalent ports cannot be computed. 
     */
    public RealDependency getMinimumDelay(IOPort port) throws IllegalActionException {
        if (_minimumDelays.get(port) == null) {
            _getMinimumDelay(port, null);
        } 
        return _minimumDelays.get(port); 
    }
    
    /**
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
    private RealDependency _getMinimumDelay(IOPort port, Collection<IOPort> visitedPorts) throws IllegalActionException {
        if (visitedPorts == null)
            visitedPorts = new ArrayList<IOPort>();
        else if (visitedPorts.contains(port))
            return RealDependency.OPLUS_IDENTITY;
        visitedPorts.add(port);
        RealDependency minimumDelay = RealDependency.OPLUS_IDENTITY;
        if (port.isInput()) {
            // if port is input port of this actor
            if (this._actor.inputPortList().contains(port)) {
                minimumDelay = ((RealDelayCausalityInterfaceForComposites)((CompositeActor)
                        this._actor.getContainer()).getCausalityInterface())._getMinimumDelay(port, visitedPorts);
            }
            // else if port is input port of any actor in this actor
            else {
                if (port.getContainer() instanceof CompositeActor) {
                    Collection<IOPort> equivalentPorts = (((CompositeActor)port.getContainer()).getCausalityInterface()).equivalentPorts(port);
                    for (IOPort equivalentPort : equivalentPorts) {
                        Collection<IOPort> sourcePorts = equivalentPort.sourcePortList(); // contains only one item (?)
                        for (IOPort sourcePort : sourcePorts) {
                            RealDependency dependency = _getMinimumDelay(sourcePort, visitedPorts);
                            if (dependency.compareTo(minimumDelay) == RealDependency.LESS_THAN)
                                minimumDelay = dependency;
                        } 
                    }
                } else {
                    Collection<IOPort> sourcePorts = port.sourcePortList(); // contains only one item (?)
                    for (IOPort actorOutputPort : sourcePorts) {
                        RealDependency dependency = _getMinimumDelay(actorOutputPort, visitedPorts);
                        if (dependency.compareTo(minimumDelay) == RealDependency.LESS_THAN)
                            minimumDelay = dependency;
                    }
                    if (sourcePorts.size() == 0) {
                        minimumDelay = RealDependency.OTIMES_IDENTITY;
                    }
                }
            }
        } else if (port.isOutput()) {
            // if port is output port of this actor
            if (this._actor.outputPortList().contains(port)) {
                Collection<IOPort> sourcePorts = port.sourcePortList(); // contains only one item (?)
                for (IOPort actorOutputPort : sourcePorts) {
                    RealDependency dependency = _getMinimumDelay(actorOutputPort, visitedPorts);
                    if (dependency.compareTo(minimumDelay) == RealDependency.LESS_THAN)
                        minimumDelay = dependency;
                }
                if (sourcePorts.size() == 0) {
                    minimumDelay = RealDependency.OTIMES_IDENTITY;
                }
            }
            // else if port is output port of any actor in this actor
            else { 
                if (port.getContainer() instanceof CompositeActor) {
                    Collection<IOPort> deepInputPorts = port.deepInsidePortList();
                    for (IOPort inputPort : deepInputPorts) { 
                        RealDependency delay = _getMinimumDelay(inputPort, visitedPorts);
                        if (delay.compareTo(minimumDelay) == RealDependency.LESS_THAN)
                            minimumDelay = delay;
                    }
                } else {
                    CausalityInterface causalityInterface = ((Actor)port.getContainer()).getCausalityInterface();
                    Collection<IOPort> inputPorts = causalityInterface.dependentPorts(port);
                    for (IOPort inputPort : inputPorts) {
                        RealDependency delay = _getMinimumDelay(inputPort, visitedPorts);
                        delay = (RealDependency) delay.oTimes(causalityInterface.getDependency(inputPort, port));
                        if (delay.compareTo(minimumDelay) == RealDependency.LESS_THAN)
                            minimumDelay = delay;
                    }
                    if (inputPorts.size() == 0)
                        minimumDelay = RealDependency.OTIMES_IDENTITY;
                }
            }
        }
        _minimumDelays.put(port, minimumDelay); 
        return minimumDelay;
    } 
    
    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////
    
    /** Buffer for minimum delays that were already computed.  
     */
    private Map<IOPort, RealDependency> _minimumDelays = new HashMap<IOPort, RealDependency>();
    
}
