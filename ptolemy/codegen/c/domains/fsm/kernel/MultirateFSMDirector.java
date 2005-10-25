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
    
    /** Generate the code for the firing of actors.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating fire code for the actor.
     */
    public void generateFireCode(StringBuffer code)
            throws IllegalActionException {
        
        ptolemy.domains.fsm.kernel.FSMActor controller = 
                ((ptolemy.domains.fsm.kernel.FSMDirector)
                getComponent()).getController();
        
        FSMActor controllerHelper = (FSMActor) _getHelper(controller);
        
        // generate code for refinements
        _generateRefinementCode(code);
        
        // generate code for non-preemptive transition
        code.append("\n/* Nonpreepmtive Transition */\n\n");
        controllerHelper._generateFireCode(code, new TransitionRetriever() {
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
    
        //FSMActor controllerHelper = (FSMActor) _getHelper(controller);
        
        ptolemy.codegen.c.actor.TypedCompositeActor containerHelper 
                = (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(container);
        
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
        int containerConfigurationIndex = 0;
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
                        
                        containerRates[containerConfigurationIndex] = portRates;
                        containerConfigurationIndex++;
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
                    
                    containerRates[containerConfigurationIndex] = portRates;
                    containerConfigurationIndex++;
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
        for (int order = 0; order < rates.length; order++) {
                        
            int rate = rates[order][portNumber];
            
            code.append("case " + order + ":\n");

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
     *  @exception IllegalActionException
     */
    public void generateTransferOutputsCode(IOPort outputPort, StringBuffer code)
            throws IllegalActionException {
     
        code.append("\n/* Transfer tokens to the outside */\n\n");

        CompositeActor container = (CompositeActor) getComponent().getContainer();
        
        ptolemy.codegen.c.actor.TypedCompositeActor containerHelper
                = (ptolemy.codegen.c.actor.TypedCompositeActor)
                _getHelper(container);
        
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
        for (int order = 0; order < rates.length; order++) {
                       
            int rate = rates[order][portNumber];
            
            code.append("case " + order + ":\n");
            
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
                    
            // The offset of the ports connected to the output port is 
            // updated by outside director.
            _updatePortOffset(outputPort, code, rate);
            
            code.append("break;\n");
        }    
        code.append("}\n");
    }
    
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
            
            // update the corresponding controller port buffer size
            int oldSize = controllerHelper.getBufferSize(controllerPort, 0);   
            if (oldSize < portRates[i]) {
                for (int j = 0; j < controllerPort.getWidth(); j++) {
                    controllerHelper.setBufferSize(controllerPort, j, portRates[i]);   
                }
            }
            
            if (refinementPort.isInput()) {
            
                // update the input port buffer size
                for (int channel = 0; channel < refinementPort.getWidth(); channel++) {
                    refinementHelper.setBufferSize(refinementPort, channel, portRates[i]);           
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
