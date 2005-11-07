/* Code generator helper class associated with the HDFDirector class.

Copyright (c) 2005 The Regents of the University of California.
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

package ptolemy.codegen.c.domains.hdf.kernel;

import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.util.DFUtilities;
import ptolemy.codegen.c.actor.TypedCompositeActor;
import ptolemy.codegen.c.domains.sdf.kernel.SDFDirector;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.data.BooleanToken;
import ptolemy.domains.sdf.kernel.CachedSDFScheduler;
import ptolemy.domains.sdf.kernel.SDFScheduler;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// HDFDirector

/** 
 Code generator helper class associated with the HDFDirector class.
 
 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (zgang)
 @Pt.AcceptedRating Red (zgang)
 */

public class HDFDirector extends SDFDirector {

    /** Construct the code generator helper associated with the given HDFDirector.
     *  @param director The associated ptolemy.domains.hdf.kernel.HDFDirector
     */
    public HDFDirector(ptolemy.domains.hdf.kernel.HDFDirector director) {
        super(director);
    }
    
    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////
    
    /** Generate code for declaring read and write offset variables if needed.
     *  First relay the information of firings per global iteration of the 
     *  container actor to the contained actors if necessary. Then call the same 
     *  method in its super class to create read and write offset variables 
     *  if needed.
     * 
     *  @return The generated code.
     *  @exception IllegalActionException If thrown while creating offset variables.
     */
    public String createOffsetVariablesIfNeeded() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        
        ptolemy.domains.hdf.kernel.HDFDirector director = 
                (ptolemy.domains.hdf.kernel.HDFDirector) getComponent();
        CompositeActor container = (CompositeActor) director.getContainer();
        ptolemy.codegen.c.actor.TypedCompositeActor containerHelper = 
                (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(container);

        List actors = container.deepEntityList();
        int numberOfActors = actors.size();
        int numberOfConfigurationsOfContainer = _schedules.length;
        
        int[] actorConfigurations = new int[numberOfActors];
        for (int configurationNumber = 0; 
                configurationNumber < numberOfConfigurationsOfContainer;
                configurationNumber++) {
            
            // Find the configuration number of each contained actor
            // given the configuration number of the container actor.
            int remainder = configurationNumber;
            for (int j = 0; j < numberOfActors - 1; j++) {
                actorConfigurations[j] = remainder / _divisors[j+1];
                remainder = remainder % _divisors[j+1]; 
            }    
            actorConfigurations[numberOfActors - 1] = remainder;
            
            // Find the firings per global iteration for the current 
            // configuration of the container actor.
            int[] arrayOfFiringsPerGlobalIterationOfContainer = 
                    containerHelper.getFiringsPerGlobalIteration();
            int firingsPerGlobalIterationOfContainer = 1;
            if (arrayOfFiringsPerGlobalIterationOfContainer != null) {
                firingsPerGlobalIterationOfContainer =
                        arrayOfFiringsPerGlobalIterationOfContainer
                        [configurationNumber];
            }
            
            Iterator actorsIterator = actors.iterator();
            int actorNumber = 0;
            while (actorsIterator.hasNext()) {
                Actor actor = (Actor) actorsIterator.next();
                if (actor instanceof CompositeActor) {
                    ptolemy.actor.Director localDirector = actor.getDirector();
                    // If the actor's local director is HDFDirector
                    // or HDFFSMDirector, set the firings per global iteration
                    // of the actor to be the product of firings per global
                    // iteration of the container actor and firings per local 
                    // iteration of the actor. This way we can relay the 
                    // information of firings per global iteration to the inside.
                    if(localDirector instanceof 
                            ptolemy.domains.hdf.kernel.HDFDirector ||
                            localDirector instanceof 
                            ptolemy.domains.hdf.kernel.HDFFSMDirector) {
                        int firingsPerLocalIteration = 0;
                        Iterator firings = _schedules[configurationNumber]
                                .firingIterator();
                        while (firings.hasNext()) {
                             Firing firing = (Firing) firings.next();
                             if (firing.getActor() == actor) {
                                 firingsPerLocalIteration += 
                                        firing.getIterationCount();  
                             }
                        }
                        
                        // key statement here
                        int firingsPerGlobalIterationOfActor
                                = firingsPerLocalIteration 
                                * firingsPerGlobalIterationOfContainer;
                        
                        TypedCompositeActor actorHelper
                                = (TypedCompositeActor) _getHelper((NamedObj) actor);
                        int[] arrayOfFiringsPerGlobalIterationOfActor 
                                = actorHelper.getFiringsPerGlobalIteration();
                        if (arrayOfFiringsPerGlobalIterationOfActor != null) {
                            int temp = arrayOfFiringsPerGlobalIterationOfActor
                                    [actorConfigurations[actorNumber]];
                            // only the maximum matters
                            if (temp < firingsPerGlobalIterationOfActor) {
                                arrayOfFiringsPerGlobalIterationOfActor
                                        [actorConfigurations[actorNumber]]
                                         = firingsPerGlobalIterationOfActor;
                            } 
                        } else {
                            arrayOfFiringsPerGlobalIterationOfActor 
                                    = new int[actorHelper.getRates().length];
                            actorHelper.setFiringsPerGlobalIteration
                                    (arrayOfFiringsPerGlobalIterationOfActor);
                            arrayOfFiringsPerGlobalIterationOfActor
                                    [actorConfigurations[actorNumber]] 
                                     = firingsPerGlobalIterationOfActor;                                                      
                        }
                    }
                }
                actorNumber++;
            }
        }

        code.append(super.createOffsetVariablesIfNeeded());
        return code.toString();
    }
    
    /** Generate the code for the firing of actors according to the HDF
     *  schedules.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If the actor to be fired cannot 
     *   find its associated helper or fire code generation fails.
     */
    public void generateFireCode(StringBuffer code) throws IllegalActionException {
        
        ptolemy.domains.hdf.kernel.HDFDirector director = 
            (ptolemy.domains.hdf.kernel.HDFDirector) getComponent();
        CompositeActor container = (CompositeActor) director.getContainer();
        ptolemy.codegen.c.actor.TypedCompositeActor containerHelper = 
                (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(container);
        
        int numberOfActors = container.deepEntityList().size();
        int[] actorConfigurations = new int[numberOfActors];
        boolean inline = 
                ((BooleanToken) _codeGenerator.inline.getToken()).booleanValue();
        
        if (!inline) {
            StringBuffer functionCode = new StringBuffer();
            Iterator actors = container.deepEntityList().iterator();
            while (actors.hasNext()) {
                Actor actor = (Actor) actors.next();
                functionCode.append("\nvoid " + 
                        actor.getFullName().replace('.' , '_') + "() {\n");
                CodeGeneratorHelper actorHelper = 
                        (CodeGeneratorHelper) _getHelper((NamedObj) actor);
                actorHelper.generateFireCode(functionCode);
                functionCode.append("}\n");
            }
            code.insert(0, functionCode);
            
            code.append("int i;\n");
        }   
        
        code.append("switch (" 
                + containerHelper.processCode("$actorSymbol(currentConfiguration)")
                + ") {\n");
        for (int configurationNumber = 0; configurationNumber < _schedules.length; 
                configurationNumber++) {
            
            // Find the configuration number of each contained actor
            // given the configuration number of the container actor.
            int remainder = configurationNumber;
            for (int i = 0; i < numberOfActors - 1; i++) {
                actorConfigurations[i] = remainder / _divisors[i+1];
                remainder = remainder % _divisors[i+1]; 
            }    
            actorConfigurations[numberOfActors - 1] = remainder;
            
            code.append("case " + configurationNumber + ":\n");

            Iterator actorsToFire = _schedules[configurationNumber].firingIterator();
            while (actorsToFire.hasNext()) {
                Firing firing = (Firing) actorsToFire.next();
                Actor actor = firing.getActor();
                
                // Find the actor number for the given actor.
                // Actors are numbered in the order as in the list returned 
                // by deepEntityList().
                int actorNumber = 0;
                Iterator actors = container.deepEntityList().iterator();
                while(actors.hasNext()) {
                    if (actors.next() == actor) {
                        break;   
                    }
                    actorNumber++;
                }

                CodeGeneratorHelper helper = 
                        (CodeGeneratorHelper) _getHelper((NamedObj) actor);
                int[][] rates = helper.getRates();
                
                if (inline) {
                    for (int i = 0; i < firing.getIterationCount(); i++) {

                        // generate fire code for the actor
                        helper.generateFireCode(code);

                        // update buffer offset after firing each actor once
                        Iterator ports = ((Entity) actor).portList().iterator();
                        int j = 0; // j is the port number
                        while (ports.hasNext()) {
                            IOPort port = (IOPort) ports.next();
                            int rate;
                            if (rates != null) {
                                rate = rates[actorConfigurations[actorNumber]][j];    
                            } else {
                                rate = DFUtilities.getRate(port);
                            }
                            if (port.isInput()) {
                                _updatePortOffset(port, code, rate);
                            } else {
                                _updateConnectedPortsOffset(port, code, rate);
                            } 
                            j++;
                        }
                    }
                } else {
                    
                    int count = firing.getIterationCount();
                    if (count > 1) {
                        code.append("for (i = 0; i < " + count + " ; i++) {\n");
                    }   
                        
                    code.append(actor.getFullName().replace('.' , '_') + "();\n");
                        
                    // update buffer offset after firing each actor once
                    Iterator ports = ((Entity) actor).portList().iterator();
                    int j = 0; // j is the port number
                    while (ports.hasNext()) {
                        IOPort port = (IOPort) ports.next();
                        int rate;
                        if (rates != null) {
                            rate = rates[actorConfigurations[actorNumber]][j];    
                        } else {
                            rate = DFUtilities.getRate(port);
                        }
                        if (port.isInput()) {
                            _updatePortOffset(port, code, rate);
                        } else {
                            _updateConnectedPortsOffset(port, code, rate);
                        } 
                        j++;
                    }
                        
                    if (count > 1) {
                        code.append("}\n");
                    } 
                }
            } 
            code.append("break;\n");
        }
        code.append("}\n");
        
        // A variable is set to record the firing of the director.
        // This variable is used when doing mode transition after
        // one global iteration.
        code.append(containerHelper.processCode("$actorSymbol(fired) = 1;\n"));   
    }
    
    /** Generate the initialize code for the associated HDF director. Generate
     *  code for initializing the configuration number of the container actor.
     * 
     *  @return The generated initialize code.
     *  @exception IllegalActionException If thrown while calling the same
     *   method in its super class or updating the configuration number.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer initializeCode = new StringBuffer();
        initializeCode.append(super.generateInitializeCode());

        _updateConfigurationNumber(initializeCode);
        
        return initializeCode.toString();
    }
    
    /** Generate mode transition code. It generates code for updating configuration
     *  number of the container actor. The code generated in this method is executed 
     *  after each global iteration.
     * 
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If getting helper fails, processing 
     *   code fails, or updating configuration number fails. 
     */
    public void generateModeTransitionCode(StringBuffer code) 
            throws IllegalActionException {

        super.generateModeTransitionCode(code);

        ptolemy.domains.hdf.kernel.HDFDirector director = 
                (ptolemy.domains.hdf.kernel.HDFDirector) getComponent();
        CompositeActor container = (CompositeActor) director.getContainer();  
        ptolemy.codegen.c.actor.TypedCompositeActor containerHelper = 
                (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(container);

        code.append(containerHelper.processCode("if ($actorSymbol(fired)) {\n"));
        _updateConfigurationNumber(code);
        code.append(containerHelper.processCode("$actorSymbol(fired) = 0;\n"));
        code.append("}\n");
    }

    
    /** Generate the preinitialize code for this director. It computes and records
     *  schedules for all configurations of the container actor, also records 
     *  external port rates of the container actor under all configurations.
     * 
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If getting the helper fails,
     *   or if generating the preinitialize code for a helper fails,
     *   or if there is a problem getting the buffer size of a port.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generatePreinitializeCode());
        
        ptolemy.domains.hdf.kernel.HDFDirector director = 
                (ptolemy.domains.hdf.kernel.HDFDirector) getComponent();
        CompositeActor container = (CompositeActor) director.getContainer();
        ptolemy.codegen.c.actor.TypedCompositeActor containerHelper = 
                (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(container);
        code.append(containerHelper.processCode
                ("static int $actorSymbol(currentConfiguration);\n"));
        code.append(containerHelper.processCode
                ("static unsigned char $actorSymbol(fired) = 0;\n"));
        List actors = container.deepEntityList();
        int numberOfActors = actors.size();
        _divisors = new int[numberOfActors];
        int numberOfConfigurationsOfContainer = 1;
        
        // Initialize _divisors for later use and find the total number 
        // of configurations for the container actor, which is the product
        // of the numbers of configurations of contained actors.
        for (int i = numberOfActors - 1; i >= 0; i--) {
            Actor actor = (Actor) actors.get(i);
            CodeGeneratorHelper helper = 
                    (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            int[][] rates = helper.getRates(); 
            _divisors[i] = numberOfConfigurationsOfContainer;
            if (rates != null) {
                numberOfConfigurationsOfContainer *= rates.length;
                _divisors[i] = numberOfConfigurationsOfContainer;
            }    
        }
        
        // Set constrainBufferSizes to true so that buffer capacity
        // will be recorded while computing schedule.
        ((CachedSDFScheduler) director.getScheduler())
                .constrainBufferSizes.setExpression("true");
        
        _schedules = new Schedule[numberOfConfigurationsOfContainer];  
        int[][] containerRates = new int[numberOfConfigurationsOfContainer][];
        int[] actorConfigurations = new int[numberOfActors];
        for (int configurationNumber = 0; 
                configurationNumber < numberOfConfigurationsOfContainer; 
                configurationNumber++) {
            
            // Find the configuration number of each contained actor
            // given the configuration number of the container actor.
            int remainder = configurationNumber;
            for (int j = 0; j < numberOfActors - 1; j++) {
                actorConfigurations[j] = remainder / _divisors[j+1];
                remainder = remainder % _divisors[j+1]; 
            }    
            actorConfigurations[numberOfActors - 1] = remainder;
            
            // Set port rates of all actors for current configuration.
            int j = 0; // j is the actor number
            Iterator actorsIterator = actors.iterator();
            while (actorsIterator.hasNext()) {
                Actor actor = (Actor) actorsIterator.next();
                CodeGeneratorHelper actorHelper = 
                        (CodeGeneratorHelper) _getHelper((NamedObj) actor);
                int[][] rates = actorHelper.getRates(); 
                // If rates is null, then the current actor has only one 
                // configuration and the port rates have already been set.
                if (rates != null) { 
                    int[] portRates = rates[actorConfigurations[j]];
                    Iterator ports = ((Entity) actor).portList().iterator();
                    int k = 0; // k is the port number
                    while (ports.hasNext()) {
                        IOPort port = (IOPort) ports.next();
                        if (port.isInput()) {
                            DFUtilities.setTokenConsumptionRate(port, portRates[k]);
                        } else {
                            DFUtilities.setTokenProductionRate(port, portRates[k]);                     
                        }
                        k++;
                    }
                }
                j++;
            }
            
            // Each schedule must be computed from scratch, including updating
            // buffer capacity for each actor, determining external port capacity.
            director.invalidateSchedule();
            ((CachedSDFScheduler) director.getScheduler()).clearCaches();
            
            // The following code clears all receivers under the control 
            // of the director, essentially setting the field _waitingTokens
            // in each receiver to zero. This should be done in SDFScheduler.
            actorsIterator = actors.iterator(); 
            while (actorsIterator.hasNext()) {
                Actor actor = (Actor) actorsIterator.next(); 
                Iterator inputPorts = actor.inputPortList().iterator();
                while (inputPorts.hasNext()) {
                    IOPort inputPort = (IOPort) inputPorts.next();
                    Receiver[][] receivers = inputPort.getReceivers();
                    if (receivers != null) {
                        for (int m = 0; m < receivers.length; m++) {
                            for (int n = 0; n < receivers[m].length; n++) {
                                receivers[m][n].clear();   
                            }
                        }
                    }
                }
            }
            Iterator outputPorts = container.outputPortList().iterator();
            while (outputPorts.hasNext()) {
                IOPort outputPort = (IOPort) outputPorts.next();
                Receiver[][] receivers = outputPort.getInsideReceivers();
                if (receivers != null) {
                    for (int m = 0; m < receivers.length; m++) {
                        for (int n = 0; n < receivers[m].length; n++) {
                            receivers[m][n].clear();   
                        }
                    }
                }
            }
            
            // Compute the schedule.
            _schedules[configurationNumber] = director.getScheduler().getSchedule();
            
            // Check to see if the buffer size needed in current configuration  
            // is greater than in previous configurations. If so, set the buffer 
            // size to the current buffer size needed. 
            _updatePortBufferSize();
            
            // Record external port rates for current configuration.
            List externalPorts = container.portList();
            int[] externalPortRates = new int[externalPorts.size()];
            Iterator externalPortsIterator = externalPorts.iterator();
            int portNumber = 0;
            while (externalPortsIterator.hasNext()) {
                IOPort externalPort = (IOPort) externalPortsIterator.next();
                externalPortRates[portNumber] = DFUtilities.getRate(externalPort);
                portNumber++;
            }
            containerRates[configurationNumber] = externalPortRates;              
        }
        
        containerHelper.setRates(containerRates);
        
        // Set constrainBufferSizes back to false so that it won't interfere
        // with running the model in ptolemy environment.
        ((SDFScheduler) director.getScheduler())
                .constrainBufferSizes.setExpression("false");
                    
        return code.toString();
    }

	/** Generate code for transferring enough tokens to complete an internal 
     *  iteration.
     *  @param inputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    public void generateTransferInputsCode(IOPort inputPort, StringBuffer code) 
            throws IllegalActionException {
     
        code.append("\n/* Transfer tokens to the inside */\n\n");
    
        CompositeActor container = (CompositeActor) getComponent().getContainer();
        ptolemy.codegen.c.actor.TypedCompositeActor containerHelper
                = (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(container);
        
        // Find the port number for the given input port.
        Iterator containerPorts = container.portList().iterator();
        int portNumber = 0;
        while (containerPorts.hasNext()) {
            if (containerPorts.next() == inputPort) {
                break;
            }
            portNumber++;
        }
        
        code.append("switch (" 
                + containerHelper.processCode("$actorSymbol(currentConfiguration)")
                + ") {\n");
        // Each configuration has a schedule, therefore the number of configurations
        // is equal to the number of schedules.
        for (int configurationNumber = 0; configurationNumber < _schedules.length; 
                configurationNumber++) {
            
            int[][] rates = containerHelper.getRates();
            int rate = rates[configurationNumber][portNumber];
            
            code.append("case " + configurationNumber + ":\n");

            for (int i = 0; i < inputPort.getWidth(); i++) {
                if (i < inputPort.getWidthInside()) {
             
                    String name = inputPort.getName();
                    if (inputPort.isMultiport()) {
                        name = name + '#' + i;   
                    }
  
                    for (int k = 0; k < rate; k++) { 
                        code.append(containerHelper
                                .getReference("@" + name + "," + k));
                        code.append(" = ");
                        code.append(containerHelper
                                 .getReference(name + "," + k));
                        code.append(";\n");
                    }    
                }
            }
            
            // The offset of the input port itself is updated by outside director.
            _updateConnectedPortsOffset(inputPort, code, rate);
            
            code.append("break;\n");
        }
        code.append("}\n");        
    }

    /** Generate code for transferring enough tokens to fulfill the output 
     *  production rate.
     *  @param outputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    public void generateTransferOutputsCode(IOPort outputPort, StringBuffer code)
            throws IllegalActionException {
     
        code.append("\n/* Transfer tokens to the outside */\n\n");

        CompositeActor container = (CompositeActor) getComponent().getContainer();
        ptolemy.codegen.c.actor.TypedCompositeActor containerHelper
                = (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(container);
        
        // Find the port number for the given output port.
        Iterator containerPorts = container.portList().iterator();
        int portNumber = 0;
        while (containerPorts.hasNext()) {
            if (containerPorts.next() == outputPort) {
                break;
            }
            portNumber++;
        }
        
        code.append("switch (" 
                + containerHelper.processCode("$actorSymbol(currentConfiguration)")
                + ") {\n");
        // Each configuration has a schedule, therefore the number of configurations
        // is equal to the number of schedules.
        for (int configurationNumber = 0; configurationNumber < _schedules.length; 
                configurationNumber++) {
            
            int[][] rates = containerHelper.getRates();
            int rate = rates[configurationNumber][portNumber];
            
            code.append("case " + configurationNumber + ":\n");
            
            for (int i = 0; i < outputPort.getWidthInside(); i++) {
                if (i < outputPort.getWidth()) {   
                    String name = outputPort.getName();
                    if (outputPort.isMultiport()) {
                        name = name + '#' + i;   
                    }
      
                    for (int k = 0; k < rate; k++) {    
                        code.append(containerHelper
                                .getReference(name + "," + k));
                        code.append(" = ");
                        code.append(containerHelper
                                .getReference("@" + name + "," + k));
                        code.append(";\n");
                    }    
                }
            } 
                    
            // The offset of the ports connected to the output port in its downstream 
            // is updated by outside director.
            _updatePortOffset(outputPort, code, rate);
            
            code.append("break;\n");
        }    
        code.append("}\n");
    }
    
    
    /** Create read and write offset variables if needed for any output port
     *  of the container actor and any input port of contained actors.
     *  @return A string containing declared read and write offset variables.
     *  @exception IllegalActionException If thrown while creating offset variables.
     */    
    protected String _createOffsetVariablesIfNeeded() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        CompositeActor container 
	            = (CompositeActor) getComponent().getContainer();

        Iterator outputPorts = container.outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort) outputPorts.next();
            code.append(_createOffsetVariablesIfNeeded(outputPort));
        }
    
        Iterator actors = container.deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            Iterator inputPorts = actor.inputPortList().iterator();
            while (inputPorts.hasNext()) {
                IOPort inputPort = (IOPort) inputPorts.next();
                code.append(_createOffsetVariablesIfNeeded(inputPort));               
            } 
        } 
        return code.toString();
    }
    
    /** Check to see if variables are needed to represent read and
     *  write offsets for the given port.
     *  @param port The given port.
     *  @return A string containing declared read and write offset variables.
     *  @exception IllegalActionException If thrown while creating offset variables.
     */      
    // FIXME: we could record total number of tokens transferred in each port 
    // for each schedule and then check if a variale is needed for each offset. 
    // For now we always use variables.
    protected String _createOffsetVariablesIfNeeded(IOPort port) 
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        
        CodeGeneratorHelper actorHelper = 
            (CodeGeneratorHelper) _getHelper(port.getContainer()); 
        
        // When buffer size is no greater than 1, there is no need for 
        // offset variable.
        if (actorHelper.getBufferSize(port) <= 1) {
             return code.toString();
        }
     
        int width = 0;
        if (port.isInput()) {
            width = port.getWidth();
        } else {
            width = port.getWidthInside();
        } 
        for (int channel = 0; channel < width; channel++) {
            
            // Increase the buffer size of that channel to the power of two.
            int bufferSize = _ceilToPowerOfTwo(actorHelper
                    .getBufferSize(port, channel));
            actorHelper.setBufferSize(port, channel, bufferSize);
             
            StringBuffer channelReadOffset = new StringBuffer();
            StringBuffer channelWriteOffset = new StringBuffer();
            channelReadOffset.append(port.getFullName().replace('.', '_'));
            channelWriteOffset.append(port.getFullName().replace('.', '_'));

            if (width > 1) {
                channelReadOffset.append("_" + channel);
                channelWriteOffset.append("_" + channel);
            }

            channelReadOffset.append("_readoffset");
            channelWriteOffset.append("_writeoffset");

            String channelReadOffsetVariable = channelReadOffset.toString();
            String channelWriteOffsetVariable = channelWriteOffset.toString();

            code.append("static int " + channelReadOffsetVariable + " = " 
                    + actorHelper.getReadOffset(port, channel) + ";\n");
            code.append("static int " + channelWriteOffsetVariable + " = " 
                    + actorHelper.getWriteOffset(port, channel) + ";\n");
             
            // Now replace these concrete offsets with the variables.
            actorHelper.setReadOffset(port, channel, channelReadOffsetVariable);
            actorHelper.setWriteOffset(port, channel, channelWriteOffsetVariable);
        }
        return code.toString();
    }

    
    /** Generate code for updating configuration number of the container actor
     *  as a function of the configuration numbers of contained actors.
     *  The total number of configurations of the container is the product
     *  of numbers of configurations of contained actors.
     *  
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while getting helper or
     *   processing code.
     */
    protected void _updateConfigurationNumber(StringBuffer code) 
            throws IllegalActionException {
        
        ptolemy.domains.hdf.kernel.HDFDirector director = 
                (ptolemy.domains.hdf.kernel.HDFDirector) getComponent();
        CompositeActor container = (CompositeActor) director.getContainer();
        ptolemy.codegen.c.actor.TypedCompositeActor containerHelper = 
                (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(container);
        
        int numberOfActors = container.deepEntityList().size();
        code.append(containerHelper.
                processCode("$actorSymbol(currentConfiguration) = "));
        Iterator actors = container.deepEntityList().iterator();
        int actorNumber = 0;
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper actorHelper = (CodeGeneratorHelper)
                    _getHelper((NamedObj) actor);
            int[][] rates = actorHelper.getRates();
            // From the following code one can find that the configuration number
            // of the first contained actor is the "most significant" number and
            // that of the last is the "least significant".
            if (actorNumber < numberOfActors - 1) {
                if (rates != null) {
                    code.append(actorHelper.processCode
                            ("$actorSymbol(currentConfiguration)") 
                            + " * " + _divisors[actorNumber + 1] + " + "); 
                } else {
                    code.append("0 + ");   
                }
            } else {
                if (rates != null) {
                    code.append(actorHelper.processCode
                            ("$actorSymbol(currentConfiguration);\n")); 
                } else {
                    code.append("0;\n");   
                }    
            }
            actorNumber++;
        }           
    }    
    
    /** Check to see if the buffer size needed in current configuration  
     *  is greater than in previous configurations. If so, set the buffer 
     *  size to the current buffer size needed. 
     *  @exception IllegalActionException If thrown while getting helper
     *   or buffer size.
     */
    protected void _updatePortBufferSize() throws IllegalActionException {
        
        ptolemy.domains.hdf.kernel.HDFDirector director = 
                (ptolemy.domains.hdf.kernel.HDFDirector) getComponent();
        CompositeActor container = (CompositeActor) director.getContainer();
        ptolemy.codegen.c.actor.TypedCompositeActor containerHelper = 
                (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(container);
        
        Iterator actors = container.deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper actorHelper = 
                    (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            Iterator inputPorts = actor.inputPortList().iterator();
            while (inputPorts.hasNext()) {
                IOPort inputPort = (IOPort) inputPorts.next();
                for (int k = 0; k < inputPort.getWidth(); k++) {
                    int newCapacity = getBufferSize(inputPort, k);
                    int oldCapacity = actorHelper.getBufferSize(inputPort, k);
                    if (newCapacity > oldCapacity) {
                        actorHelper.setBufferSize(inputPort, k, newCapacity);      
                    }
                }
            }
        }
        
        Iterator outputPorts = container.outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort) outputPorts.next();
            for (int k = 0; k < outputPort.getWidthInside(); k++) {
                int newCapacity = getBufferSize(outputPort, k);
                int oldCapacity = containerHelper.getBufferSize(outputPort, k);
                if (newCapacity > oldCapacity) {
                    containerHelper.setBufferSize(outputPort, k, newCapacity); 
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** An array of integers helping to establish the relationship between
     *  the configuration number of the container actor and the configuration
     *  numbers of contained actors.
     */
    private int[] _divisors;
    
    /** An array of schedules, each element corresponding to one configuration.
     */
    private Schedule[] _schedules;
}
