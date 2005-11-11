/* Code generator helper class associated with the MultirateFSMDirector class.

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

package ptolemy.codegen.c.domains.fsm.kernel;

import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedActor;
import ptolemy.actor.util.DFUtilities;
import ptolemy.codegen.c.actor.TypedCompositeActor;
import ptolemy.codegen.c.actor.lib.ParseTreeCodeGenerator;
import ptolemy.codegen.c.domains.fsm.kernel.FSMActor.TransitionRetriever;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.Variable;
import ptolemy.domains.fsm.kernel.AbstractActionsAttribute;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.domains.fsm.modal.Refinement;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;


//////////////////////////////////////////////////////////////////////////
//// MultirateFSMDirector

/** 
 Code generator helper class associated with the MultirateFSMDirector class.
 
 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (zgang)
 @Pt.AcceptedRating Red (zgang)
 */

public class MultirateFSMDirector extends FSMDirector {

    /** Construct the code generator helper associated with the 
     *  given MultirateFSMDirector.
     *  @param director The associated component.
     */
    public MultirateFSMDirector(
            ptolemy.domains.fsm.kernel.MultirateFSMDirector director) {
        super(director);
    }
    
    /** Generate code for declaring read and write offset variables if needed. 
     *  @return The generated code.
     *  @exception IllegalActionException If thrown while creating offset variables.
     */
    public String createOffsetVariablesIfNeeded() throws IllegalActionException {
        StringBuffer code = new StringBuffer();  
        code.append(_createOffsetVariablesIfNeeded());
        code.append(super.createOffsetVariablesIfNeeded());
        return code.toString();
    }
    
    /** Generate the code for the firing of actors controlled by this director.
     *  It generates code for firing refinements and making non-preemptive
     *  transition.
     * 
     *  @return The generated fire code.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating fire code for the actor.
     */
    public String generateFireCode() throws IllegalActionException {
        
        StringBuffer code = new StringBuffer();
        // Note unlike FSMDirector, no preemptive transition is taken
        // under the control of this director.
        
        // generate code for refinements
        _generateRefinementCode(code);
        
        ptolemy.domains.fsm.kernel.FSMActor controller = 
                ((ptolemy.domains.fsm.kernel.FSMDirector)
                getComponent()).getController();
        FSMActor controllerHelper = (FSMActor) _getHelper(controller);
        
        // generate code for non-preemptive transition
        code.append("\n/* Nonpreepmtive Transition */\n\n");
        controllerHelper.generateTransitionCode(code, new TransitionRetriever() {
            public Iterator retrieveTransitions(State state) {
                return state.nonpreemptiveTransitionList().iterator();  
            }
        });
        return code.toString();
    }   
    
    /** Generate the initialize code for the associated MultirateFSMDirector. 
     *  Generate code for initializing the configuration number of the container
     *  actor. If the initial state does not have a refinement, which usually 
     *  happens when the purpose of making transition from the initial state is 
     *  to initialize model parameters, generate code for making transition and
     *  then initializing the configuration number of the container actor.
     * 
     *  @return The generated initialize code.
     *  @exception IllegalActionException If thrown while calling the same
     *   method in its super class or updating the configuration number.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer initializeCode = new StringBuffer();
        
        ptolemy.domains.fsm.kernel.MultirateFSMDirector director = 
                (ptolemy.domains.fsm.kernel.MultirateFSMDirector) getComponent();
        ptolemy.domains.fsm.kernel.FSMActor controller = director.getController();
        FSMActor controllerHelper = (FSMActor) _getHelper(controller);
        State initialState = controller.getInitialState();
        Actor[] actors = initialState.getRefinement();
        if (actors == null) { // no refinement for the initial state
            // We assume all transitions from the initial state are
            // nonpreemptive transitions.
            Iterator transitions =
                    initialState.nonpreemptiveTransitionList().iterator();
            initializeCode.append(_generateInitialTransitionCode(transitions));
        } else {
            initializeCode.append(super.generateInitializeCode());
            _updateConfigurationNumber(initializeCode, initialState);
        }
        return initializeCode.toString();
    }
    
    /** Generate the preinitialize code for this director. First find out
     *  the number of configurations of the modal model which is the container 
     *  actor of the director. For each configuration, find the port rates of
     *  the modal model from the port rates of the corresponding refinement and 
     *  update the maximum buffer sizes needed for the ports of the refinement.
     * 
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If getting the helper fails,
     *   or if generating the preinitialize code for a helper fails,
     *   or if there is a problem getting the buffer size of a port.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generatePreinitializeCode());
        
        ptolemy.domains.fsm.kernel.MultirateFSMDirector director = 
                (ptolemy.domains.fsm.kernel.MultirateFSMDirector) getComponent();
        CompositeActor container = (CompositeActor) director.getContainer();
        ptolemy.domains.fsm.kernel.FSMActor controller = director.getController();
        ptolemy.codegen.c.actor.TypedCompositeActor containerHelper =
                (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(container);
        
        code.append(containerHelper.processCode
                ("static int $actorSymbol(currentConfiguration);\n"));
        
        int numberOfConfigurationsOfContainer = 0;
        
        // Find the number of configurations of the container actor, which is
        // the sum of numbers of configurations of all the refinements.
        Iterator states = controller.entityList().iterator();
        while (states.hasNext()) {
            State state = (State) states.next();
            TypedActor[] actors = state.getRefinement();
            if (actors != null) {
                CodeGeneratorHelper refinementHelper = 
                        (CodeGeneratorHelper) _getHelper((NamedObj) actors[0]);
                int[][] rates = refinementHelper.getRates();
                if (rates != null) {
                    numberOfConfigurationsOfContainer += rates.length;   
                } else {
                    numberOfConfigurationsOfContainer += 1;
                }
            }    
        }
        
        int[][] containerRates = new int[numberOfConfigurationsOfContainer][];
        
        states = controller.entityList().iterator();
        int configurationNumber = 0;
        while (states.hasNext()) {
            State state = (State) states.next();
            TypedActor[] actors = state.getRefinement();
            if (actors != null) {
                CodeGeneratorHelper refinementHelper = 
                        (CodeGeneratorHelper) _getHelper((NamedObj) actors[0]);
                int[][] rates = refinementHelper.getRates();
                if (rates != null) {
                    for (int i = 0; i < rates.length; i++) {
                        // Extract the port rates from the current refinement 
                        // and set the port rates of the modal model containing 
                        // the refinement.
                        int[] portRates = rates[i];
                        containerRates[configurationNumber] = portRates;
                        configurationNumber++;
                        
                        // Update buffer sizes of current refinement with
                        // the extracted port rates.
                        _updatePortBufferSize(actors[0], portRates);
                    } 
                } else {
                    List ports = ((Entity) actors[0]).portList();
                    int[] portRates = new int[ports.size()];
                    int k = 0;
                    // Extract the port rates from the current refinement 
                    // and set the port rates of the modal model containing 
                    // the refinement.
                    Iterator portsIterator = ports.iterator();
                    while (portsIterator.hasNext()) {
                        IOPort port = (IOPort) portsIterator.next();

                        if (port.isInput()) {
                            portRates[k] = DFUtilities.getTokenConsumptionRate(port);
                        } else {
                            portRates[k] = DFUtilities.getTokenProductionRate(port);
                        }
                        k++;
                    }
                    containerRates[configurationNumber] = portRates;
                    configurationNumber++;
                    
                    // Update buffer sizes of current refinement with
                    // the extracted port rates.
                    _updatePortBufferSize(actors[0], portRates);  
                }                
            }
        }
        
        // Set the port rates of the modal model for all configurations.
        containerHelper.setRates(containerRates);
                
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
        
        ptolemy.codegen.c.actor.TypedCompositeActor containerHelper =
                (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(container);
        
        ptolemy.domains.fsm.kernel.MultirateFSMDirector director = 
                (ptolemy.domains.fsm.kernel.MultirateFSMDirector) getComponent();
        
        ptolemy.domains.fsm.kernel.FSMActor controller = 
                director.getController();
        
        // Find the port number corresponding to the given input port.
        // Ports are numbered in the order as in the list returned by portList().
        Iterator containerPorts = container.portList().iterator();
        int portNumber = 0;
        while(containerPorts.hasNext()) {
            if (containerPorts.next() == inputPort) {
                break;
            }
            portNumber++;
        }
        
        int[][] rates = containerHelper.getRates();
        
        code.append("switch (" 
                + containerHelper.processCode("$actorSymbol(currentConfiguration)")
                + ") {\n");

        for (int configurationNumber = 0; configurationNumber < rates.length; 
                configurationNumber++) {
            
            // Find the state corresponding to the given configuration number.
            Iterator states = controller.entityList().iterator();
            int number = configurationNumber;
            State currentState = null;
            while (states.hasNext()) {
                 State state = (State) states.next();
                 Actor[] actors = state.getRefinement();
                 if (actors != null) {
                     int[][] refinementRates = ((CodeGeneratorHelper)
                             _getHelper((NamedObj) actors[0])).getRates();  
                     if (refinementRates != null) {
                         number -= refinementRates.length;          
                     } else {
                         number -= 1;      
                     }
                     if (number < 0) {
                         currentState = state;
                         break;  
                     }
                 }
            }     
            
            IOPort controllerPort = (IOPort) controller.getPort(inputPort.getName());
            Entity refinement = (Entity) currentState.getRefinement()[0];
            IOPort refinementPort = (IOPort) refinement.getPort(inputPort.getName());
            IOPort[] sinkPorts = {controllerPort, refinementPort};
            
            int rate = rates[configurationNumber][portNumber];
            
            code.append("case " + configurationNumber + ":\n");

            for (int i = 0; i < inputPort.getWidth(); i++) {
                if (i < inputPort.getWidthInside()) {
             
                    String name = inputPort.getName();
                    if (inputPort.isMultiport()) {
                        name = name + '#' + i;   
                    }
  
                    for (int k = 0; k < rate; k++) { 
                        
                        // we should only send data to the controller and the 
                        // refinement corresponding to current state.
                        for (int m = 0; m < 2; m++) {
                        
                            IOPort sinkPort = sinkPorts[m];
                            CodeGeneratorHelper helper = (CodeGeneratorHelper) 
                                    _getHelper(sinkPort.getContainer());
                            code.append(sinkPort.getFullName().replace('.', '_'));

                            if (sinkPort.isMultiport()) {
                                code.append("[" + i + "]");
                            }

                            int sinkPortBufferSize = helper.getBufferSize(sinkPort);

                            if (sinkPortBufferSize > 1) {
                                String temp = "";
                                Object offsetObject = 
                                        helper.getWriteOffset(sinkPort, i);
                                if (offsetObject instanceof Integer) {                               
                                    int offset = 
                                            ((Integer) offsetObject).intValue() + k;
                                    offset %= helper.getBufferSize(sinkPort, i);
                                    temp = new Integer(offset).toString();                                
                                } else {
                                    int modulo = 
                                            helper.getBufferSize(sinkPort, i) - 1;
                                    temp = "(" + (String) helper.getWriteOffset
                                            (sinkPort, i) + " + " + k + ")&" + modulo;
                                }
                                code.append("[" + temp + "]");
                            }                            
                            code.append(" = ");
                        }
                        code.append(containerHelper.getReference(name + "," + k));
                        code.append(";\n");
                    }
                    
                    // Only update the port write offsets of the controller and 
                    // the current refinement. The offset of the input port itself 
                    // is updated by outside director.
                    for (int m = 0; m < 2; m++) {
                        IOPort sinkPort = sinkPorts[m];
                        CodeGeneratorHelper helper = (CodeGeneratorHelper) 
                                _getHelper(sinkPort.getContainer());
                        Object offsetObject = helper.getWriteOffset(sinkPort, i);
                        if (offsetObject instanceof Integer) {
                            int offset = ((Integer) offsetObject).intValue();
                            offset = (offset + rate)% 
                                    helper.getBufferSize(sinkPort, i);
                            helper.setWriteOffset(sinkPort, i, new Integer(offset));
                        } else {
                            int modulo = helper.getBufferSize(sinkPort, i) - 1;
                            String offsetVariable = (String) 
                                    helper.getWriteOffset(sinkPort, i);
                            code.append((String) offsetVariable + " = (" 
                                    + offsetVariable + " + " + rate 
                                    + ")&" + modulo + ";\n");
                        }
                    }
                }               
            }

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
        
        // Find the port number corresponding to the given output port.
        // Ports are numbered in the order as in the list returned by portList().
        Iterator containerPorts = container.portList().iterator();
        int portNumber = 0;
        while(containerPorts.hasNext()) {
            if (containerPorts.next() == outputPort) {
                break;
            }
            portNumber++;
        }
        
        int[][] rates = containerHelper.getRates();
        
        code.append("switch (" 
                + containerHelper.processCode("$actorSymbol(currentConfiguration)")
                + ") {\n");
        for (int configurationNumber = 0; configurationNumber < rates.length; 
                configurationNumber++) {
                       
            int rate = rates[configurationNumber][portNumber];
            
            code.append("case " + configurationNumber + ":\n");
            
            for (int i = 0; i < outputPort.getWidthInside(); i++) {
                if (i < outputPort.getWidth()) {   
                    String name = outputPort.getName();
                    if (outputPort.isMultiport()) {
                        name = name + '#' + i;   
                    }
                    
                    // FIXME: we can generate for loop in C instead of using for
                    // loop here. Thus we can parametarize the rate and don't need 
                    // switch statement in the generated C code. Thus we can generate 
                    // more compressed C code. It requires storing the rates of each
                    // configuration in the generated C code. We will pursue this
                    // approach later.
                    // Note the above approach of using for loop in the generated
                    // C code applies to all directors supporting multi-rate.
                    for (int k = 0; k < rate; k++) {     
                        code.append(containerHelper.getReference(name + "," + k));
                        code.append(" = ");
                        code.append(containerHelper.getReference
                                ("@" + name + "," + k));
                        code.append(";\n");
                    }    
                }
            } 
                    
            // The offset of the ports connected to the output port in its 
            // downstream is updated by outside director.
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
            CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) 
                    _getHelper((NamedObj) actor);
            int[][] rates = actorHelper.getRates();
            // If a refinement has only one configuration, then there is no
            // need to use variables.
            if (!(actor instanceof Refinement && rates == null)) {
                Iterator inputPorts = actor.inputPortList().iterator();
                while (inputPorts.hasNext()) {
                    IOPort inputPort = (IOPort) inputPorts.next();
                    code.append(_createOffsetVariablesIfNeeded(inputPort));               
                } 
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
    // for each configuration and then check if a variable is needed for the 
    // offset. For now we always use variables.
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
    
    /** Generate code for making initial transition. The purpose of this method
     *  is to initialize parameter values because they are persistent from each
     *  run of the model. If not properly initialized, parameters will retain 
     *  their accumulated values from previous model executions. A typical approach 
     *  is to build the model such that the initial state has an outgoing 
     *  transition with guard expression true, and use the set actions of this 
     *  transition for parameter initialization. However, this transition can 
     *  only be taken during initialization instead of after firing a refinement
     *  because the initial state does not have a refinement.
     *
     *  @param transitions The iterator of transitions from initial state.
     *  @return The generated initial transition code.
     *  @throws IllegalActionException If thrown while generating transition code.
     */
    protected String _generateInitialTransitionCode(Iterator transitions) 
            throws IllegalActionException {
        StringBuffer codeBuffer = new StringBuffer();       
        
        ptolemy.domains.fsm.kernel.MultirateFSMDirector director =
                (ptolemy.domains.fsm.kernel.MultirateFSMDirector) getComponent();
        ptolemy.domains.fsm.kernel.FSMActor controller = director.getController();
        FSMActor controllerHelper = (FSMActor) _getHelper(controller);

        int transitionCount = 0;
        while (transitions.hasNext()) {
            if (transitionCount == 0) {
                codeBuffer.append("if (");
            } else {
                codeBuffer.append("else if (");
            }
            transitionCount++;

            Transition transition = (Transition) transitions.next();

            // generate code for guard expression
            String guard = transition.getGuardExpression();
            PtParser parser = new PtParser();
            ASTPtRootNode guardParseTree = parser.generateParseTree(guard);
            ParseTreeCodeGenerator parseTreeCodeGenerator = 
                    new ParseTreeCodeGenerator();
            parseTreeCodeGenerator
                    .evaluateParseTree(guardParseTree, controllerHelper._scope);
            codeBuffer.append(parseTreeCodeGenerator.generateFireCode());
            codeBuffer.append(") {\n");

            // generate code for commit action
            Iterator actions = transition.commitActionList().iterator();
            while (actions.hasNext()) {
                AbstractActionsAttribute action =
                        (AbstractActionsAttribute) actions.next();
                Iterator destinationNameList =
                        action.getDestinationNameList().iterator();

                while (destinationNameList.hasNext()) {
                    String destinationName = (String) destinationNameList.next();
                    NamedObj destination = (NamedObj) action
                            .getDestination(destinationName);                       
                    ASTPtRootNode parseTree = action.getParseTree(destinationName);
                    if (destination instanceof Variable) {
                        codeBuffer.append(destination.getFullName()
                                .replace('.', '_') + " = ");
                    } else {
                        throw new IllegalActionException("No output can be" +
                                " produced in any action for MultirateFSMDirector.");   
                    }
                    parseTreeCodeGenerator = new ParseTreeCodeGenerator();
                    parseTreeCodeGenerator.evaluateParseTree
                            (parseTree, controllerHelper._scope);
                    codeBuffer.append(parseTreeCodeGenerator.generateFireCode());
                    codeBuffer.append(";\n");
                }
            }

            // All contained actors must be initialized before the configuration
            // number of the container actor can be updated.
            codeBuffer.append(super.generateInitializeCode());
            
            // Generate code for updating current state.
            // This overrides using initial state as the current state in
            // the initialize code of the controller.
            State destinationState = transition.destinationState();
            controllerHelper._updateCurrentState(codeBuffer, destinationState, 0);
            
            // Generate code for updating configuration number of this 
            // FSMActor's container actor.
            _updateConfigurationNumber(codeBuffer, destinationState);

            codeBuffer.append("} ");
        } 
        return controllerHelper.processCode(codeBuffer.toString());
    }

        
    /** Generate code for updating configuration number of the container actor
     *  according to the following equation:
     *  
     *  container's current configuration number = sum of numbers of configurations
     *  of refinements corresponding to states before the current state + 
     *  configuration number of refinement corresponding to the current state
     * 
     *  The order of states is the same as in the list returned by calling 
     *  entityList() on the controller.
     * 
     *  @param codeBuffer The string buffer that the generated code is appended to.
     *  @param state The current state.
     *  @exception IllegalActionException If helper cannot be found, refinement
     *   cannot be found or code cannot be processed.
     */
    protected void _updateConfigurationNumber(StringBuffer codeBuffer, State state) 
            throws IllegalActionException {
        
        ptolemy.domains.fsm.kernel.MultirateFSMDirector director = 
            (ptolemy.domains.fsm.kernel.MultirateFSMDirector) getComponent();
        ptolemy.domains.fsm.kernel.FSMActor controller = director.getController();
        TypedCompositeActor containerHelper = (TypedCompositeActor) 
                _getHelper(director.getContainer());
        Actor[] refinements = state.getRefinement();
        if (refinements == null) {
            return;        
        }
        TypedCompositeActor refinementHelper = (TypedCompositeActor) 
                _getHelper((NamedObj) refinements[0]);
        Iterator states = controller.entityList().iterator();
        int tempSum = 0;

        while (states.hasNext()) {
            State nextState = (State) states.next();
            Actor[] actors = nextState.getRefinement();
            if (actors != null) {
                TypedCompositeActor helper = (TypedCompositeActor)
                        _getHelper((NamedObj) actors[0]);
                int[][] rates = helper.getRates();
                    
                if (nextState == state) {
                    if (rates == null ) { // only one internal configuration
                        codeBuffer.append(containerHelper.processCode
                                ("$actorSymbol(currentConfiguration) = ")
                                + tempSum + ";\n");
                    } else {
                        codeBuffer.append(containerHelper.processCode
                                ("$actorSymbol(currentConfiguration) = ")
                                + refinementHelper.processCode
                                ("$actorSymbol(currentConfiguration)") 
                                + " + " + tempSum + ";\n");   
                    }
                    break;
                } else {
                    if (rates == null) { // only one internal configuration
                        tempSum += 1;       
                    } else {
                        tempSum += rates.length;   
                    }
                }    
            }    
        }                                  
    }
    
    
    /** Check to see if the buffer size needed in current configuration  
     *  is greater than in previous configurations. If so, set the buffer 
     *  size to the current buffer size needed. 
     *  @param refinement The refinement to be checked.
     *  @param portRates An int array of port rates of the refinement.
     *  @exception IllegalActionException If thrown while getting helper
     *   or buffer size.
     */
    protected void _updatePortBufferSize(Actor refinement, int[] portRates) 
            throws IllegalActionException {
        
        ptolemy.domains.fsm.kernel.MultirateFSMDirector director = 
                (ptolemy.domains.fsm.kernel.MultirateFSMDirector)
                getComponent();
    
        CompositeActor container = (CompositeActor) director.getContainer();
    
        ptolemy.domains.fsm.kernel.FSMActor controller = director.getController();
        
        CodeGeneratorHelper refinementHelper = (CodeGeneratorHelper)
                _getHelper((NamedObj) refinement);
        
        CodeGeneratorHelper controllerHelper = (CodeGeneratorHelper)
                _getHelper(controller);
        
        CodeGeneratorHelper containerHelper = (CodeGeneratorHelper)
                _getHelper(container);
        
        List refinementPorts = ((Entity) refinement).portList();
        List controllerPorts = controller.portList();
        List containerPorts = container.portList();
        
        for (int i = 0; i < refinementPorts.size(); i++) {
        
            IOPort refinementPort = (IOPort) refinementPorts.get(i);
            IOPort controllerPort = (IOPort) controllerPorts.get(i);
            IOPort containerPort = (IOPort) containerPorts.get(i);
            
            // Update the corresponding controller port buffer size.
            // Notice that all channels have same buffer size.
            int oldSize = controllerHelper.getBufferSize(controllerPort, 0);   
            if (oldSize < portRates[i]) {
                for (int j = 0; j < controllerPort.getWidth(); j++) {
                    controllerHelper.setBufferSize(controllerPort, j, portRates[i]);   
                }
            }
            
            if (refinementPort.isInput()) {           
                // Update the input port buffer size of the refinement.
                oldSize = refinementHelper.getBufferSize(refinementPort, 0);
                if  (oldSize < portRates[i]) {
                    for (int j = 0; j < refinementPort.getWidth(); j++) {    
                        refinementHelper.
                                setBufferSize(refinementPort, j, portRates[i]);           
                    }
                }                        
            } else {              
                // Update the output port buffer size of the container actor.
                oldSize = containerHelper.getBufferSize(containerPort, 0);   
                if (oldSize < portRates[i]) {
                    for (int j = 0; j < containerPort.getWidthInside(); j++) {
                        containerHelper.setBufferSize(containerPort, j, portRates[i]);   
                    }
                }
            }
        }                   
    }   
}    
