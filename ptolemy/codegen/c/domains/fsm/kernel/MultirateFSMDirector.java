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
import ptolemy.codegen.c.domains.fsm.kernel.FSMActor.TransitionRetriever;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.domains.fsm.kernel.State;
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

    /** Construct the code generator helper associated with the given MultirateFSMDirector.
     *  @param director The associated ptolemy.domains.fsm.kernel.MultirateFSMDirector
     */
    public MultirateFSMDirector(ptolemy.domains.fsm.kernel.MultirateFSMDirector director) {
        super(director);
    }
    
    public String createOffsetVariablesIfNeeded() 
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();  
        code.append(_createOffsetVariablesIfNeeded());
        code.append(super.createOffsetVariablesIfNeeded());
        return code.toString();
    }
    
    /** Generate the code for the firing of actors.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating fire code for the actor.
     */
    public void generateFireCode(StringBuffer code)
            throws IllegalActionException {
        
        // generate code for refinements
        _generateRefinementCode(code);
        
        ptolemy.domains.fsm.kernel.FSMActor controller = 
                ((ptolemy.domains.fsm.kernel.FSMDirector)
                getComponent()).getController();
        FSMActor controllerHelper = (FSMActor) _getHelper(controller);
        
        // generate code for non-preemptive transition
        code.append("\n/* Nonpreepmtive Transition */\n\n");
        controllerHelper.generateFireCode(code, new TransitionRetriever() {
            public Iterator retrieveTransitions(State state) {
                return state.nonpreemptiveTransitionList().iterator();  
            }
        });
        
    }   
    
    /** Generate the preinitialize code for this director.
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If getting the helper fails,
     *   or if generating the preinitialize code for a helper fails,
     *   or if there is a problem getting the buffer size of a port.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generatePreinitializeCode());
        
        ptolemy.domains.fsm.kernel.MultirateFSMDirector director = 
                (ptolemy.domains.fsm.kernel.MultirateFSMDirector)
                getComponent();
        CompositeActor container = (CompositeActor) director.getContainer();
        ptolemy.domains.fsm.kernel.FSMActor controller = director.getController();
        ptolemy.codegen.c.actor.TypedCompositeActor containerHelper 
                = (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(container);
        
        code.append(containerHelper.processCode("static int $actorSymbol(currentConfiguration);\n"));
        
        int numberOfConfigurationsOfContainer = 0;
        
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
                        int[] portRates = rates[i];
                        
                        _updatePortBufferSize(actors[0], portRates);
                        
                        containerRates[configurationNumber] = portRates;
                        configurationNumber++;
                    } 
                } else {                    
                    List ports = ((Entity) actors[0]).portList();
                    int[] portRates = new int[ports.size()];
                    int k = 0;
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
                    
                    _updatePortBufferSize(actors[0], portRates);
                    
                    containerRates[configurationNumber] = portRates;
                    configurationNumber++;
                }                
            }
        }
        
        containerHelper.setRates(containerRates);
                
        return code.toString();
    }
    
    /** Generate code for transferring enough tokens to complete an internal 
     *  iteration.
     *  @param inputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException
     */
    public void generateTransferInputsCode(IOPort inputPort, StringBuffer code) 
            throws IllegalActionException {
     
        code.append("\n/* Transfer tokens to the inside */\n\n");
    
        CompositeActor container = (CompositeActor) getComponent().getContainer();
        
        ptolemy.codegen.c.actor.TypedCompositeActor containerHelper
                = (ptolemy.codegen.c.actor.TypedCompositeActor)
                _getHelper(container);
        
        ptolemy.domains.fsm.kernel.MultirateFSMDirector director = 
                (ptolemy.domains.fsm.kernel.MultirateFSMDirector) getComponent();
        
        ptolemy.domains.fsm.kernel.FSMActor controller = 
                director.getController();
        
        // find the port number corresponding to the given input port
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

        for (int configurationNumber = 0; configurationNumber < rates.length; configurationNumber++) {
            
            // find the state corresponding to the given configuration number
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
                        
                        // we should only send data to controller and refinement
                        // correspnding to current state.
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

                                Object offsetObject = helper.getWriteOffset(sinkPort, i);

                                if (offsetObject instanceof Integer) {
                                
                                    int offset = ((Integer) offsetObject).intValue() + k;
                                    offset %= helper.getBufferSize(sinkPort, i);
                                    temp = new Integer(offset).toString();
                                
                                } else {
                                    int modulo = helper.getBufferSize(sinkPort, i) - 1;
                                    temp = "(" + (String) helper.getWriteOffset(sinkPort, i)
                                            + " + " + k + ")&" + modulo;
                                }

                                code.append("[" + temp + "]");
                            }
                            
                            code.append(" = ");
                        }

                        code.append(containerHelper
                                 .getReference(name + "," + k));
                        code.append(";\n");
                    }
                    
                    // Only update the port write offsets of the controller and the current refinement.
                    // The offset of the input port itself is updated by outside director.
                    for (int m = 0; m < 2; m++) {
                        IOPort sinkPort = sinkPorts[m];
                        CodeGeneratorHelper helper = (CodeGeneratorHelper) 
                                _getHelper(sinkPort.getContainer());
                        Object offsetObject = helper.getWriteOffset(sinkPort, i);
                        if (offsetObject instanceof Integer) {
                            int offset = ((Integer) offsetObject).intValue();
                            offset = (offset + rate)% helper.getBufferSize(sinkPort, i);
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
     *  @exception IllegalActionException
     */
    public void generateTransferOutputsCode(IOPort outputPort, StringBuffer code)
            throws IllegalActionException {
     
        code.append("\n/* Transfer tokens to the outside */\n\n");

        CompositeActor container = (CompositeActor) getComponent().getContainer();
        
        ptolemy.codegen.c.actor.TypedCompositeActor containerHelper
                = (ptolemy.codegen.c.actor.TypedCompositeActor)
                _getHelper(container);
        
        // find the port number corresponding to the given output port
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
        for (int configurationNumber = 0; configurationNumber < rates.length; configurationNumber++) {
                       
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
                    // more compressed C code. It requires storing the rate of each
                    // configuration in the generated C code. We will pursue this
                    // approach later.
                    // Note the above approach of using for loop in the generated
                    // C code applies to all directors supporting multi-rate.
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
                    
            // The offset of the ports connected to the output port is 
            // updated by outside director.
            _updatePortOffset(outputPort, code, rate);
            
            code.append("break;\n");
        }    
        code.append("}\n");
    }
    
    /** Check to see if variables are needed to represent read and
     *  write offsets.
     */    
    protected String _createOffsetVariablesIfNeeded() 
               throws IllegalActionException {
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
            // If a refinement has only one rate, then there is no
            // need to uses variables.
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
     */      
    // FIXME: we could record total number of tokens transferred in each port 
    // for each configuration and then check if a variale is needed for each 
    // offset. For now we always use variables.
    protected String _createOffsetVariablesIfNeeded(IOPort port) 
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        CodeGeneratorHelper actorHelper = 
            (CodeGeneratorHelper) _getHelper(port.getContainer());   
     
        int length = 0;
        if (port.isInput()) {
            length = port.getWidth();
        } else {
            length = port.getWidthInside();
        } 
        for (int channel = 0; channel < length; channel++) {
            
            // Increase the buffer size of that channel to the power of two.
            int bufferSize = _ceilToPowerOfTwo(actorHelper
                    .getBufferSize(port, channel));
            actorHelper.setBufferSize(port, channel, bufferSize);
             
            StringBuffer channelReadOffset = new StringBuffer();
            StringBuffer channelWriteOffset = new StringBuffer();
            channelReadOffset.append(port.getFullName().replace('.', '_'));
            channelWriteOffset.append(port.getFullName().replace('.', '_'));

            if (port.getWidth() > 1) {
                channelReadOffset.append("_" + channel);
                channelWriteOffset.append("_" + channel);
            }

            channelReadOffset.append("_readoffset");
            channelWriteOffset.append("_writeoffset");

            String channelReadOffsetVariable = channelReadOffset.toString();
            String channelWriteOffsetVariable = channelWriteOffset.toString();

            // At this point, all offsets are 0 or the number of
            // initial tokens of SampleDelay.
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
    
    /** Check to see if the buffer size needed in current state  
     *  is smaller than in previous states. If so, set the buffer 
     *  size to the current buffer size. 
     */
    // FIXME: controller needs to get the maximal buffer size in one 
    // global iteration, which is potentially large. But we can infer
    // the number of tokens needed in one global iteration from guard
    // expression and only keep that many spaces.
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
            
            // update the corresponding controller port buffer size.
            // notice that all channels have same buffer size.
            int oldSize = controllerHelper.getBufferSize(controllerPort, 0);   
            if (oldSize < portRates[i]) {
                for (int j = 0; j < controllerPort.getWidth(); j++) {
                    controllerHelper.setBufferSize(controllerPort, j, portRates[i]);   
                }
            }
            
            if (refinementPort.isInput()) {
            
                // update the input port buffer size
                oldSize = refinementHelper.getBufferSize(refinementPort, 0);
                if  (oldSize < portRates[i]) {
                    for (int j = 0; j < refinementPort.getWidth(); j++) {    
                        refinementHelper.setBufferSize(refinementPort, j, portRates[i]);           
                    }
                }                        
            } else {
              
                // update the corresponding container's output port buffer size
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
