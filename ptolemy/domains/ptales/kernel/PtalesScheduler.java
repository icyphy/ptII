package ptolemy.domains.ptales.kernel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.util.CausalityInterfaceForComposites;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.domains.sdf.kernel.SDFScheduler;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class PtalesScheduler extends SDFScheduler {

    public PtalesScheduler(Director container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    protected Schedule _getSchedule() throws IllegalActionException,
            NotSchedulableException {
        PtalesDirector director = (PtalesDirector) getContainer();
        CompositeActor compositeActor = (CompositeActor) (director
                .getContainer());
        List<Actor> actors = compositeActor.deepEntityList();
        
        // FIXME: Need to keep track of the order dimensions are given
        // in each port. 4 | x, 2 | y is not the same spec
        // as 2 | y, 4 | x, but our data structures below do not
        // distinguish between the two.
        
        // First, collect all the production/consumption specs.
        Map<IOPort,LinkedHashMap<String,Integer>> inputSpecs = new HashMap<IOPort,LinkedHashMap<String,Integer>>();
        Map<IOPort,LinkedHashMap<String,Integer>> outputSpecs = new HashMap<IOPort,LinkedHashMap<String,Integer>>();
        Set<String> dimensions = new HashSet<String>();
        Map<IOPort,Parameter> portRates = new HashMap<IOPort,Parameter>();
        for (Actor actor : actors) {
            // First do the input ports.
            List<IOPort> ports = actor.inputPortList();
            for (IOPort port : ports) {
                _recordRates(inputSpecs, dimensions, port);
                _createPortRateParameter(portRates, port, "tokenConsumptionRate");
            }
            // Next do the output ports.
            ports = actor.outputPortList();
            for (IOPort port : ports) {
                _recordRates(outputSpecs, dimensions, port);
                _createPortRateParameter(portRates, port, "tokenProductionRate");
            }
        }
        if (_debugging) {
            _debug("Total set of dimensions is: " + dimensions);
        }
        // Calculate repetition rates by dimension for each actor.
        Map<Actor,Map<String,Integer>> firingCounts = new HashMap<Actor,Map<String,Integer>>();
        for (String dimension : dimensions) {
            for (Actor actor : actors) {
                // First do the input ports.
                List<IOPort> ports = actor.inputPortList();
                for (IOPort port : ports) {
                    Map<String,Integer> spec = inputSpecs.get(port);
                    _setPortRateForSDFScheduler(portRates, dimension, port, spec);
                }
                // Next do the output ports.
                ports = actor.outputPortList();
                for (IOPort port : ports) {
                    Map<String,Integer> spec = outputSpecs.get(port);
                    _setPortRateForSDFScheduler(portRates, dimension, port, spec);
                }
            }
            // Now we have set the production consumption rates for all port.
            // Call the scheduler. As a side effect, it will set repetition
            // rates for each actor.
            super._getSchedule();
            for (Actor actor : actors) {
                Map<String,Integer> dimensionToFiringCount = firingCounts.get(actor);
                if (dimensionToFiringCount == null) {
                    dimensionToFiringCount = new HashMap<String,Integer>();
                    firingCounts.put(actor, dimensionToFiringCount);
                }
                dimensionToFiringCount.put(dimension, _getFiringCount((Entity)actor));
            }
        }
        if (_debugging) {
            for (Actor actor : actors) {
                _debug("* Firing counts for actor "
                        + actor.getFullName()
                        + firingCounts.get(actor).toString());
            }
        }

        Schedule schedule = new Schedule();
        // Populate the schedule with a subclass of Firing
        // that keeps track of the dimensions for the firing.
        // FIXME: Brute force technique here assumes an acyclic graph.
        CausalityInterfaceForComposites causality 
                = (CausalityInterfaceForComposites) compositeActor
                .getCausalityInterface();
        List<Actor> sortedActors = causality.topologicalSort();
        for (Actor actor : sortedActors) {
            Firing firing = new Firing(actor);
            int iterationCount = 1;
            Map<String,Integer> counts = firingCounts.get(actor);
            for (String dimension : counts.keySet()) {
                Integer count = counts.get(dimension);
                // This should never be null, but just in case, check...
                if (count != null) {
                    iterationCount *= count.intValue();
                }
            }
            firing.setIterationCount(iterationCount);
            schedule.add(firing);
                        
            // Now we need to set capacities of each of the receivers.
            List<IOPort> ports = actor.inputPortList();
            for (IOPort port : ports) {
                LinkedHashMap<String,Integer> spec = inputSpecs.get(port);
                int blockSize = 1;
                if (spec != null) {
                    for (String dimension : spec.keySet()) {
                        Integer dimensionSpec = spec.get(dimension);
                        if (dimensionSpec != null) {
                            blockSize *= dimensionSpec.intValue();
                        }
                    }
                }
                // In case the actor is internally an SDF model,
                // we need to restore the SDF consumption rates
                // to what they should be. Here, we assume the Ptales
                // spec is consistent with what the SDF model inside
                // does. FIXME: This should be checked.
                Attribute rateParameter = port.getAttribute("tokenConsumptionRate");
                if (rateParameter != null) {
                    ((Parameter)rateParameter).setToken(new IntToken(blockSize));
                }
                Receiver[][] receivers = port.getReceivers();
                if (receivers != null && receivers.length > 0) {
                    for (Receiver[] receiverss : receivers) {
                        if (receiverss != null && receiverss.length > 0) {
                            for (Receiver receiver : receiverss) {
                                ((PtalesReceiver)receiver).setReadPattern(spec, counts);
                            }
                        }
                    }
                }
            }
            // In case the actor is internally an SDF model,
            // we need to restore the SDF consumption rates
            // to what they should be. Here, we assume the Ptales
            // spec is consistent with what the SDF model inside
            // does. FIXME: This should be checked.
            ports = actor.outputPortList();
            for (IOPort port : ports) {
                Map<String,Integer> spec = outputSpecs.get(port);
                // Notify the destination receivers of the write pattern.
                // FIXME: Receiver[][] receivers = port.getRemoteReceivers(relation);
                int blockSize = 1;
                if (spec != null) {
                    for (String dimension : spec.keySet()) {
                        Integer dimensionSpec = spec.get(dimension);
                        if (dimensionSpec != null) {
                            blockSize *= dimensionSpec.intValue();
                        }
                    }
                }
                Attribute rateParameter = port.getAttribute("tokenProductionRate");
                if (rateParameter != null) {
                    ((Parameter)rateParameter).setToken(new IntToken(blockSize));
                }
            }
        }
        return schedule;
    }

    /**
     * @param portRates
     * @param dimension
     * @param port
     * @param spec
     * @throws IllegalActionException
     */
    private void _setPortRateForSDFScheduler(Map<IOPort, Parameter> portRates,
            String dimension, IOPort port, Map<String, Integer> spec)
            throws IllegalActionException {
        Integer rate = null;
        if (spec != null) {
            rate = spec.get(dimension);
        }
        if (rate == null) {
            rate = _ONE;
        }
        Parameter portRate = portRates.get(port);
        portRate.setToken(new IntToken(rate.intValue()));
    }

    /**
     * @param portRates
     * @param port
     * @throws IllegalActionException
     */
    private void _createPortRateParameter(Map<IOPort, Parameter> portRates,
            IOPort port, String description) throws IllegalActionException {
        Attribute portRate = port.getAttribute(description);
        if (portRate == null) {
            try {
                portRate = new Parameter(port, description);
            } catch (NameDuplicationException e) {
                throw new NotSchedulableException(e.toString());
            }
        }
        portRates.put(port, (Parameter) portRate);
    }

    private LinkedHashMap<String,Integer> _getRates(IOPort port) throws IllegalActionException {
        LinkedHashMap<String,Integer> result = new LinkedHashMap<String,Integer>();
        Attribute rateSpec = port.getAttribute(_RATE_SPEC_NAME);
        if (rateSpec instanceof StringParameter) {
            String spec = ((StringParameter)rateSpec).stringValue();
            // FIXME: What exactly is the syntax to expect here?
            // I'm assuming this:
            //    rate1 | dimensionName2 , rate2 | dimensionName2
            // FIXME: Handle syntax errors.
            String[] subSpecs = spec.split(",");
            for (String subSpec : subSpecs) {
                if (subSpec.trim().length() > 0) {
                    String[] subSubSpecs = subSpec.split("\\|");
                    result.put(subSubSpecs[1].trim(), new Integer(subSubSpecs[0].trim()));
                }
            }
        }
        return result;
    }
    
    /**
     * @param inputSpecs
     * @param dimensions
     * @param port
     * @throws IllegalActionException
     */
    private void _recordRates(Map<IOPort, LinkedHashMap<String, Integer>> inputSpecs,
            Set<String> dimensions, IOPort port) throws IllegalActionException {
        LinkedHashMap<String,Integer> spec = _getRates(port);
        String direction = " consumes ";
        if (port.isOutput()) {
            direction = " produces ";
        }
        if (spec.size() > 0) {
            inputSpecs.put(port, spec);
            dimensions.addAll(spec.keySet());
            if (_debugging) {
                for (String dimension : spec.keySet()) {
                    _debug("--- " + port.getFullName() 
                            + direction
                            + spec.get(dimension)
                            + " in dimension "
                            + dimension);
                }
            }
        }
    }
    
    private static String _RATE_SPEC_NAME = "_ptalesRateSpec";
    
    private static Integer _ONE = new Integer(1);
}
