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

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedActor;
import ptolemy.actor.util.DFUtilities;
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
        while(states.hasNext()) {
            State state = (State) states.next();
            TypedActor[] actors = state.getRefinement();
            if (actors != null) {
                CodeGeneratorHelper refinementHelper = 
                        (CodeGeneratorHelper) _getHelper((NamedObj) actors[0]);
                int[][] rates = refinementHelper.getRates();
                if (rates != null) {
                    for (int i = 0; i < rates.length; i++) {
                        int[] portRates = rates[i];
                        containerRates[containerConfigurationIndex] = portRates;
                        containerConfigurationIndex++;
                    } 
                } else {
                    List ports = ((Entity) actors[0]).portList();
                    int[] portRates = new int[ports.size()];
                    for (int k = 0; k < portRates.length; k++) {
                        IOPort port = (IOPort) ports.get(k);
                        if (port.isInput()) {
                            portRates[k] = DFUtilities.getTokenConsumptionRate(port);
                        } else {
                            portRates[k] = DFUtilities.getTokenProductionRate(port);                     
                        }                        
                    }
                    containerRates[containerConfigurationIndex] = portRates;
                    containerConfigurationIndex++;
                }                
            }
        }
        
        ptolemy.codegen.c.actor.TypedCompositeActor containerHelper = 
            (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(container);
        containerHelper.setRates(containerRates);
                
        return code.toString();
    }
    
    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    public void generateTransferInputsCode(IOPort inputPort, StringBuffer code) 
            throws IllegalActionException {

        code.append("\n/* Transfer tokens to the inside */\n\n");

        ptolemy.codegen.c.actor.TypedCompositeActor _compositeActorHelper
                = (ptolemy.codegen.c.actor.TypedCompositeActor)
                _getHelper(getComponent().getContainer());

        for (int i = 0; i < inputPort.getWidth(); i++) {
            if (i < inputPort.getWidthInside()) {   
                String name = inputPort.getName();
                if (inputPort.isMultiport()) {
                    name = name + '#' + i;   
                }               
         
                code.append(_compositeActorHelper.getReference("@" + name));
                code.append(" = ");
                code.append(_compositeActorHelper.getReference(name));
                code.append(";\n");
            }
        }      
    }

    public void generateTransferOutputsCode(IOPort outputPort, StringBuffer code)
            throws IllegalActionException {

        code.append("\n/* Transfer tokens to the outside */\n\n");

        ptolemy.codegen.c.actor.TypedCompositeActor _compositeActorHelper
                = (ptolemy.codegen.c.actor.TypedCompositeActor)
                _getHelper(getComponent().getContainer());

        for (int i = 0; i < outputPort.getWidthInside(); i++) {
            if (i < outputPort.getWidth()) {   
                String name = outputPort.getName();
                if (outputPort.isMultiport()) {
                    name = name + '#' + i;   
                }             
        
                code.append(_compositeActorHelper.getReference(name));
                code.append(" = ");
                code.append(_compositeActorHelper.getReference("@" + name));
                code.append(";\n");
            }
        }          
    }

}
