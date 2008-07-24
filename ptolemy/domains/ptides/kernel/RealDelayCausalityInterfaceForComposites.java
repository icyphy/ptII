package ptolemy.domains.ptides.kernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.CausalityInterfaceForComposites;
import ptolemy.actor.util.Dependency;
import ptolemy.actor.util.RealDependency;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;

public class RealDelayCausalityInterfaceForComposites extends
        CausalityInterfaceForComposites {

    public RealDelayCausalityInterfaceForComposites(
            Actor actor, Dependency defaultDependency) 
            throws IllegalArgumentException {
        super(actor, defaultDependency);
    }
    
    public Dependency getMinimumDeadline(IOPort outputPort) throws IllegalActionException {
        return null;
    }
    
    public RealDependency getMinimumDelay(IOPort port) throws IllegalActionException {
        if (_minimumDelays.get(port) == null) {
            _getMinimumDelay(port, null);
        } 
        return _minimumDelays.get(port); 
    }
    
    public void wrapup() {
        _minimumDelays.clear();
    }

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
    
    
    
    private Map<IOPort, RealDependency> _minimumDelays = new HashMap<IOPort, RealDependency>();
    
}
