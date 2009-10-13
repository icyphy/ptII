package ptolemy.domains.ptales.kernel;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.util.CausalityInterfaceForComposites;
import ptolemy.actor.util.DFUtilities;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.domains.sdf.kernel.SDFScheduler;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** 
 *  FIXME: To do:
 *  - The tokenInitProduction parameter is not being used. ArrayOL equivalent?
 *  
 * @author eal
 *
 */
public class PtalesScheduler extends SDFScheduler {

    public PtalesScheduler(Director container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    protected Schedule _getSchedule() throws IllegalActionException,
            NotSchedulableException {
        // Context of this scheduler.
        PtalesDirector director = (PtalesDirector) getContainer();
        CompositeActor compositeActor = (CompositeActor) (director
                .getContainer());
        List<Actor> actors = compositeActor.deepEntityList();
        
        // Before overwriting them, collect the production and
        // consumption rates of the actors, if they are declared.
        // The will be declared if the actor is an SDF actor (either atomic
        // or an opaque composite actor). These will need to be restored later.
        Map<IOPort,Integer> originalDeclaredPortRates = new HashMap<IOPort,Integer>();
        for (Actor actor : actors) {
            List<IOPort> ports = actor.inputPortList();
            for (IOPort port : ports) {
                Integer rate = _getDeclaredPortRate(port, "tokenConsumptionRate");
                if (rate != null) {
                    originalDeclaredPortRates.put(port, rate);
                }
            }
            ports = actor.outputPortList();
            for (IOPort port : ports) {
                Integer rate = _getDeclaredPortRate(port, "tokenProductionRate");
                if (rate != null) {
                    originalDeclaredPortRates.put(port, rate);
                }
            }
        }
        
        // Next, collect all the multidimensional production/consumption specs.
        // These are stored in Map data structures so that they can be looked
        // up by port name and dimension name.
        // NOTE: We use a LinkedHashMap because we
        // need to keep track of the order dimensions are given
        // in each port. 4 | x, 2 | y is not the same spec
        // as 2 | y, 4 | x, but our data structures below do not
        // distinguish between the two.
        Map<IOPort,LinkedHashMap<String,Integer>> inputSpecs = new HashMap<IOPort,LinkedHashMap<String,Integer>>();
        Map<IOPort,LinkedHashMap<String,Integer>> outputSpecs = new HashMap<IOPort,LinkedHashMap<String,Integer>>();
        LinkedHashSet<String> dimensions = new LinkedHashSet<String>();
        Map<IOPort,Variable> portRates = new HashMap<IOPort,Variable>();
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
            // Since confusing debug output about intermediate schedules
            // will be produced here, we temporarily disable debugging.
            boolean isDebugging = _debugging;
            _debugging = false;
            try {
                super._getSchedule();
            } catch (IllegalActionException ex) {
                // Before doing anything further, restore the original rate parameters.
                // Otherwise, the model ends up in an inconsistent
                // state, and re-running it becomes difficult.
                _restoreOriginalPortRates(actors, originalDeclaredPortRates, portRates);
                throw ex;
            } catch (NotSchedulableException ex) {
                // Before doing anything further, restore the original rate parameters.
                // Otherwise, the model ends up in an inconsistent
                // state, and re-running it becomes difficult.
                _restoreOriginalPortRates(actors, originalDeclaredPortRates, portRates);
                throw ex;
            } finally {
                _debugging = isDebugging;
            }
            
            // Store the resulting firing counts for this dimension for each actor.
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

        // Before doing anything further, restore the original rate parameters.
        // Otherwise, if an exception occurs, the model ends up in an inconsistent
        // state, and re-running it becomes difficult.
        _restoreOriginalPortRates(actors, originalDeclaredPortRates, portRates);

        // Populate the schedule with a subclass of Firing
        // that keeps track of the dimensions for the firing.
        // FIXME: Brute force technique here assumes an acyclic graph.
        Schedule schedule = new Schedule();
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
            // Start with the input ports.
            List<IOPort> ports = actor.inputPortList();
            for (IOPort port : ports) {
                // Calculate the total amount of data (blockSize) produced by
                // a firing on this port.
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

                // Check that the multidimensional declared production rate
                // is consistent with the SDF declared rate, if
                // there originally was one in the port, and throw an exception
                // if not.
                Integer declaredRate = originalDeclaredPortRates.get(port);
                if (declaredRate != null && declaredRate.intValue() != blockSize) {
                    Attribute rateSpec = port.getAttribute(_RATE_SPEC_NAME);
                    if (rateSpec instanceof StringParameter) {
                        String multidimensionalSpec = ((StringParameter)rateSpec).stringValue();
                        throw new IllegalActionException(port,
                                "The declared rate of this port "
                                + declaredRate
                                + " is not equal to the declared multidimensional rate "
                                + multidimensionalSpec);
                    }
                }
                
                // Notify the receivers of the read pattern.
                // This will have the side effect of setting the capacity of the receivers.
                Receiver[][] receivers = port.getReceivers();
                if (receivers != null && receivers.length > 0) {
                    for (Receiver[] receiverss : receivers) {
                        if (receiverss != null && receiverss.length > 0) {
                            for (Receiver receiver : receiverss) {
                                ((PtalesReceiver)receiver).setReadPattern(spec, counts, dimensions);
                            }
                        }
                    }
                }
            }
            // Set production pattern of each of the receivers.
            ports = actor.outputPortList();
            for (IOPort port : ports) {
                LinkedHashMap<String,Integer> spec = outputSpecs.get(port);
                // Notify the destination receivers of the write pattern.
                Receiver[][] receivers = port.getRemoteReceivers();
                if (receivers != null && receivers.length > 0) {
                    for (Receiver[] receiverss : receivers) {
                        if (receiverss != null && receiverss.length > 0) {
                            for (Receiver receiver : receiverss) {
                                ((PtalesReceiver)receiver).setWritePattern(spec);
                            }
                        }
                    }
                }

                int blockSize = 1;
                if (spec != null) {
                    for (String dimension : spec.keySet()) {
                        Integer dimensionSpec = spec.get(dimension);
                        if (dimensionSpec != null) {
                            blockSize *= dimensionSpec.intValue();
                        }
                    }
                }
                
                // Check that the multidimensional declared production rate
                // is consistent with the SDF declared rate, if
                // there originally was one in the port, and throw an exception
                // if not.
                Integer declaredRate = originalDeclaredPortRates.get(port);
                if (declaredRate != null && declaredRate.intValue() != blockSize) {
                    Attribute rateSpec = port.getAttribute(_RATE_SPEC_NAME);
                    if (rateSpec instanceof StringParameter) {
                        String multidimensionalSpec = ((StringParameter)rateSpec).stringValue();
                        throw new IllegalActionException(port,
                                "The declared rate "
                                + declaredRate
                                + " of port "
                                + port.getName()
                                + " is not equal to the declared multidimensional rate "
                                + multidimensionalSpec);
                    }
                }
            }
        }
        return schedule;
    }

    /** If the specified port has the specified rate parameter, then
     *  record that parameter in the portRates data structure. Otherwise,
     *  create a new rate parameter with value 1 and record that in the
     *  the portRates data structure.
     *  @param portRates The data structure into which to record.
     *  @param port The port.
     *  @param description The name of the parameter.
     *  @throws IllegalActionException
     */
    private void _createPortRateParameter(Map<IOPort, Variable> portRates,
            IOPort port, String description) throws IllegalActionException {
        Variable portRate = DFUtilities.getRateVariable(port, description);
        if (portRate == null) {
            portRate = DFUtilities.setRate(port, description, 1);
        }
        portRates.put(port, portRate);
    }

    /** Get the port rate parameter for the specified port, if it
     *  exists, and return its value, or null if it does not exist.
     *  @param port The port.
     *  @param description The name of the parameter.
     *  @return The port rate parameter.
     *  @throws IllegalActionException If evaluating the rate parameter
     *   fails (e.g. due to a malformed expression).
     */
    private Integer _getDeclaredPortRate(IOPort port, String description) 
            throws IllegalActionException {
        Variable portRate = DFUtilities.getRateVariable(port, description);
        Integer result = null;
        if (portRate != null) {
            Token value = portRate.getToken();
            if (value instanceof IntToken) {
                result = new Integer(((IntToken)value).intValue());
            }
        }
        return result;
    }

    /**
     * 
     * @param port
     * @return
     * @throws IllegalActionException
     */
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
            LinkedHashSet<String> dimensions, IOPort port) throws IllegalActionException {
        LinkedHashMap<String,Integer> spec = _getRates(port);
        if (spec.size() > 0) {
            inputSpecs.put(port, spec);
            dimensions.addAll(spec.keySet());
            if (_debugging) {
                for (String dimension : spec.keySet()) {
                    String direction = " consumes ";
                    if (port.isOutput()) {
                        direction = " produces ";
                    }
                    _debug("--- " + port.getFullName() 
                            + direction
                            + spec.get(dimension)
                            + " in dimension "
                            + dimension);
                }
            }
        }
    }
    
    /** Restore the original port rates for the specifies actors from the
     *  values given in the table using the specified port rate parameters.
     *  @param actors The actors.
     *  @param originalDeclaredPortRates The table of original rates, by port.
     *  @param portRates A table of rate parameters, by port.
     *  @throws IllegalActionException If setting the rate parameter fails.
     */
    private void _restoreOriginalPortRates(List<Actor> actors,
            Map<IOPort, Integer> originalDeclaredPortRates,
            Map<IOPort, Variable> portRates) throws IllegalActionException {
        for (Actor actor : actors) {
            List<IOPort> ports = ((Entity)actor).portList();
            for (IOPort port : ports) {
                Integer declaredRate = originalDeclaredPortRates.get(port);
                Variable rateParameter = portRates.get(port);
                if (rateParameter != null) {
                    rateParameter.setToken(new IntToken(declaredRate.intValue()));
                }
            }
        }
    }

    /** Temporarily set the port rate for the specified port so that
     *  the SDF scheduler can solve the balance equations for the specified dimension.
     *  @param portRates A table of rate parameters by port.
     *  @param dimension The dimension for which to set the rate.
     *  @param port The port.
     *  @param spec A table of rates for the port by dimension.
     *  @throws IllegalActionException If setting the rate parameter fails.
     */
    private void _setPortRateForSDFScheduler(Map<IOPort, Variable> portRates,
            String dimension, IOPort port, Map<String, Integer> spec)
            throws IllegalActionException {
        Integer rate = null;
        if (spec != null) {
            rate = spec.get(dimension);
        }
        if (rate == null) {
            rate = _ONE;
        }
        Variable portRate = portRates.get(port);
        portRate.setToken(new IntToken(rate.intValue()));
    }

    private static String _RATE_SPEC_NAME = "_ptalesRateSpec";
    
    private static Integer _ONE = new Integer(1);
}
