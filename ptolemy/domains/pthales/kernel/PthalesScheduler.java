/* A scheduler for multidimensional dataflow.

 Copyright (c) 1998-2009 The Regents of the University of California.
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

package ptolemy.domains.pthales.kernel;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/** 
 *  FIXME: To do:
 *  - The tokenInitProduction parameter is not being used. ArrayOL equivalent?
 *  
 * @author eal
 *
 */
public class PthalesScheduler extends SDFScheduler {

    public PthalesScheduler(Director container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                    ////

    protected Schedule _getSchedule() throws IllegalActionException,
            NotSchedulableException {
        // Context of this scheduler.
        PthalesDirector director = (PthalesDirector) getContainer();
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
        // in each port. x = 4, y = 2 is not the same spec
        // as y = 2, x = 4.
        ////////////// The repetitions parameter, by Actor.
        Map<Actor,LinkedHashMap<String,Integer[]>> repetitionsSpecs
                = new HashMap<Actor,LinkedHashMap<String,Integer[]>>();
        ////////////// The size specification, by port.
        Map<IOPort,LinkedHashMap<String,Integer[]>> sizeSpecs
                = new HashMap<IOPort,LinkedHashMap<String,Integer[]>>();
        ////////////// The pattern specification, by port.
        Map<IOPort,LinkedHashMap<String,Integer[]>> patternSpecs
                = new HashMap<IOPort,LinkedHashMap<String,Integer[]>>();
        ////////////// The tiling specification, by port.
        Map<IOPort,LinkedHashMap<String,Integer[]>> tilingSpecs
                = new HashMap<IOPort,LinkedHashMap<String,Integer[]>>();
        ////////////// The SDF rate variable, by port.
        Map<IOPort,Variable> portRates = new HashMap<IOPort,Variable>();
        
        // Iterate over the actors.
        for (Actor actor : actors) {
            // First get the repetitions parameter.
            LinkedHashMap<String,Integer[]> repetitionsSpec
                    = _recordActorSpec(repetitionsSpecs, _REPETITIONS, actor);
            if (repetitionsSpec == null) {
                throw new IllegalActionException(actor,
                        actor.getFullName()
                        + " does not have a "
                        + _REPETITIONS
                        + " parameter.");
            }
            // Create an ordered list of the dimensions communicated on this link.
            List<String> dimensions = new LinkedList<String>();
            for (Entry<String,Integer[]> entry : repetitionsSpec.entrySet()) {
                dimensions.add(entry.getKey());
            }
            // Next do the output ports.
            List<IOPort> ports = actor.outputPortList();
            for (IOPort port : ports) {
                LinkedHashMap<String,Integer[]> sizeSpec
                        = _recordPortSpec(sizeSpecs, _SIZE, port);
                if (sizeSpec == null) {
                    throw new IllegalActionException(port,
                            port.getFullName()
                            + " does not have a "
                            + _SIZE
                            + " spec.");
                }
                LinkedHashMap<String,Integer[]> patternSpec
                        = _recordPortSpec(patternSpecs, _PATTERN, port);
                // FIXME: The following method looks for a stride as well,
                // which does not make sense for a tiling spec.
                LinkedHashMap<String,Integer[]> tilingSpec
                        = _recordPortSpec(tilingSpecs, _TILING, port);
                _createPortRateParameter(portRates, port, "tokenProductionRate");
                
                // Now we need to set capacities of each of the receivers.
                // Notify the destination receivers of the write pattern.
                Receiver[][] receivers = port.getRemoteReceivers();
                if (receivers != null && receivers.length > 0) {
                    for (Receiver[] receiverss : receivers) {
                        if (receiverss != null && receiverss.length > 0) {
                            for (Receiver receiver : receiverss) {
                                ((PthalesReceiver)receiver).setWritePattern(
                                        patternSpec, tilingSpec, sizeSpec, dimensions);
                            }
                        }
                    }
                }

                // Calculate the total amount of data produced by this output port.
                int blockSize = 1;
                if (patternSpec != null) {
                    for (Entry<String,Integer[]> entry : patternSpec.entrySet()) {
                        Integer[] dimensionSpec = entry.getValue();
                        if (dimensionSpec != null) {
                            blockSize *= dimensionSpec[0].intValue();
                        }
                    }
                }
                
                // Check that the multidimensional declared production rate
                // is consistent with the SDF declared rate, if
                // there originally was one in the port, and throw an exception
                // if not.
                Integer declaredRate = originalDeclaredPortRates.get(port);
                if (declaredRate != null && declaredRate.intValue() != blockSize) {
                    Attribute rateSpec = port.getAttribute(_PATTERN);
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
            // FIXME: Need to do the input ports of the container of this director,
            // treating them as output ports, in order to support hierarchical nestings
            // of this director inside other models.
            
            // Next do the input ports.
            ports = actor.inputPortList();
            for (IOPort port : ports) {
                LinkedHashMap<String,Integer[]> patternSpec 
                        = _recordPortSpec(patternSpecs, _PATTERN, port);
                // FIXME: The following method looks for a stride as well,
                // which does not make sense for a tiling spec.
                LinkedHashMap<String,Integer[]> tilingSpec
                        = _recordPortSpec(tilingSpecs, _TILING, port);
                _createPortRateParameter(portRates, port, "tokenConsumptionRate");
                
                // Calculate the total amount of data (blockSize) produced by
                // a firing on this port.
                int blockSize = 1;
                if (patternSpec != null) {
                    for (Entry<String,Integer[]> entry : patternSpec.entrySet()) {
                        Integer[] dimensionSpec = entry.getValue();
                        if (dimensionSpec != null) {
                            blockSize *= dimensionSpec[0].intValue();
                        }
                    }
                }

                // Check that the multidimensional declared production rate
                // is consistent with the SDF declared rate, if
                // there originally was one in the port, and throw an exception
                // if not.
                Integer declaredRate = originalDeclaredPortRates.get(port);
                if (declaredRate != null && declaredRate.intValue() != blockSize) {
                    Attribute rateSpec = port.getAttribute(_PATTERN);
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
                                // FIXME: Is the cast to LinkedHashSet safe?
                                // Depends on the Java implementation of LinkedHashMap.
                                ((PthalesReceiver)receiver).setReadPattern(patternSpec, tilingSpec);
                            }
                        }
                    }
                }
            }
        }
        
        // Populate the schedule with a subclass of Firing
        // that keeps track of the dimensions for the firing.
        // FIXME: Brute force technique here assumes an acyclic graph.
        // It executes all firings of upstream actors before any firing
        // of a downstream actor.
        Schedule schedule = new Schedule();
        CausalityInterfaceForComposites causality 
                = (CausalityInterfaceForComposites) compositeActor
                .getCausalityInterface();
        List<Actor> sortedActors = causality.topologicalSort();
        for (Actor actor : sortedActors) {
            Firing firing = new Firing(actor);
            int iterationCount = 1;
            LinkedHashMap<String,Integer[]> counts = repetitionsSpecs.get(actor);
            for (String dimension : counts.keySet()) {
                Integer[] count = counts.get(dimension);
                // This should never be null, but just in case, check...
                if (count != null && count.length > 0 && count[0] != null) {
                    iterationCount *= count[0].intValue();
                }
            }
            firing.setIterationCount(iterationCount);
            schedule.add(firing);
        }
        return schedule;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

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

    /** Return a data structure giving the dimension data contained by a
     *  parameter with the specified name in the specified port or actor.
     *  The dimension data is indexed by dimension name and contains two
     *  integers, a value and a stride, in that order.
     *  @param name The name of the parameter
     *  @param object The port or actor.
     *  @return The dimension data, or null if the parameter does not exist.
     *  @throws IllegalActionException If the parameter cannot be evaluated.
     */
    private LinkedHashMap<String,Integer[]> _parseSpec(
            String name, NamedObj object) throws IllegalActionException {
        LinkedHashMap<String,Integer[]> result = new LinkedHashMap<String,Integer[]>();
        Attribute attribute = object.getAttribute(name);
        if (attribute instanceof StringParameter) {
            String spec = ((StringParameter)attribute).stringValue();
            // FIXME: Handle syntax errors.
            String[] subSpecs = spec.split(",");
            for (String subSpec : subSpecs) {
                if (subSpec.trim().length() > 0) {
                    String[] subSubSpecs = subSpec.split("=");
                    Integer[] values = new Integer[2];
                    if (subSubSpecs[1].contains(".")) {
                        // A stride is given.
                        String[] strideSpec = subSubSpecs[1].split(".");
                        values[0] = new Integer(strideSpec[0].trim());
                        values[1] = new Integer(strideSpec[1].trim());
                    } else {
                        values[0] = new Integer(subSubSpecs[1].trim());
                        values[1] = _ONE;
                    }
                    result.put(subSubSpecs[0].trim(), values);
                }
            }
        }
        return result;
    }
    
    /** Populate the specified data structure with the dimension data obtained from
     *  a parameter with the specified name in the specified port
     *  @param returnedSpec The data structure to populate.
     *  @param name The name of the specification.
     *  @param actor The actor.
     *  @return The entry that is put in the data structure, or null if none is found.
     *  @throws IllegalActionException If the parameter cannot be evaluated.
     */
    private LinkedHashMap<String,Integer[]> _recordActorSpec(
            Map<Actor, LinkedHashMap<String, Integer[]>> returnedSpec,
            String name,
            Actor actor)
            throws IllegalActionException {
        LinkedHashMap<String,Integer[]> spec = _parseSpec(name, (NamedObj)actor);
        if (spec != null && spec.size() > 0) {
            returnedSpec.put(actor, spec);
            if (_debugging) {
                for (String dimension : spec.keySet()) {
                    _debug("--- " + actor.getFullName() 
                            + " has "
                            + name
                            + " with value "
                            + spec.get(dimension)
                            + " in dimension "
                            + dimension);
                }
            }
            return spec;
        }
        return null;
    }

    /** Populate the specified data structure with the dimension data obtained from
     *  a parameter with the specified name in the specified port.
     *  The resulting data structure is indexed by dimension, and has for each
     *  dimension two integers, stored as an array of Integer.
     *  If the named parameter has the string "x = n.m", where n and m are integers,
     *  then "x" becomes the dimension name and n and m are the two integers.
     *  @param returnedSpec The data structure to populate.
     *  @param name The name of the specification.
     *  @param port The port
     *  @return The entry that is put in the data structure, or null if none is found.
     *  @throws IllegalActionException If the parameter cannot be evaluated.
     */
    private LinkedHashMap<String,Integer[]> _recordPortSpec(
            Map<IOPort, LinkedHashMap<String, Integer[]>> returnedSpec,
            String name,
            IOPort port)
            throws IllegalActionException {
        LinkedHashMap<String,Integer[]> spec = _parseSpec(name, port);
        if (spec != null && spec.size() > 0) {
            returnedSpec.put(port, spec);
            if (_debugging) {
                for (String dimension : spec.keySet()) {
                    _debug("--- " + port.getFullName() 
                            + " has "
                            + name
                            + " with value "
                            + spec.get(dimension)
                            + " in dimension "
                            + dimension);
                }
            }
            return spec;
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////

    /** The name of the pattern parameter. */
    private static String _PATTERN = "pattern";
    
    /** The name of the repetitions parameter. */
    private static String _REPETITIONS = "repetitions";

    /** The name of the size parameter. */
    private static String _SIZE = "size";
    
    /** The name of the tiling parameter. */
    private static String _TILING = "tiling";


    private static Integer _ONE = new Integer(1);
}
