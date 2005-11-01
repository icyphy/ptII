/* Code generator helper class associated with the HDFFSMDirector class.

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

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedActor;
import ptolemy.codegen.c.actor.TypedCompositeActor;
import ptolemy.codegen.c.domains.fsm.kernel.FSMActor;
import ptolemy.codegen.c.domains.fsm.kernel.MultirateFSMDirector;
import ptolemy.codegen.c.domains.fsm.kernel.FSMActor.TransitionRetriever;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// HDFDirector

/**
 Code generator helper class associated with the HDFFSMDirector class.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (zgang)
 @Pt.AcceptedRating Red (zgang)
 */
public class HDFFSMDirector extends MultirateFSMDirector {
    /** Construct the code generator helper associated with the given HDFFSMDirector.
     *  @param director The associated ptolemy.domains.hdf.kernel.HDFFSMDirector
     */
    public HDFFSMDirector(ptolemy.domains.hdf.kernel.HDFFSMDirector director) {
        super(director);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////
    
    public String createOffsetVariablesIfNeeded() 
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();  
        
        ptolemy.domains.fsm.kernel.MultirateFSMDirector director = 
                (ptolemy.domains.fsm.kernel.MultirateFSMDirector) getComponent();
        CompositeActor container = (CompositeActor) director.getContainer();
        ptolemy.domains.fsm.kernel.FSMActor controller = director.getController();
        ptolemy.codegen.c.actor.TypedCompositeActor containerHelper 
                = (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(container);
        CodeGeneratorHelper controllerHelper 
                = (CodeGeneratorHelper) _getHelper(controller);
        int[] firingsPerGlobalIterationArrayOfContainer = 
                containerHelper.getFiringsPerGlobalIteration();
        int[][] containerRates = containerHelper.getRates();
        
        Iterator states = controller.entityList().iterator();
        int configurationNumber = 0;
        while (states.hasNext()) {
            State state = (State) states.next();
            TypedActor[] actors = state.getRefinement();
            if (actors != null) {
                CodeGeneratorHelper refinementHelper = 
                        (CodeGeneratorHelper) _getHelper((NamedObj) actors[0]);
                int[][] rates = refinementHelper.getRates();
                int length = 1;
                if (rates != null) {
                    length = rates.length;
                }    
                for (int i = 0; i < length; i++) {
                    int firingsPerGlobalIterationOfContainer
                            = firingsPerGlobalIterationArrayOfContainer
                            [configurationNumber]; 
                    List ports = controller.portList();
                    Iterator portsIterator = ports.iterator();
                    int portNumber = 0;
                    while (portsIterator.hasNext()) {
                        IOPort port = (IOPort) portsIterator.next();
                        int newSize = firingsPerGlobalIterationOfContainer * 
                                containerRates[configurationNumber][portNumber];
                        int oldSize = controllerHelper.getBufferSize(port, 0);
                        if (oldSize < newSize) {
                            for (int j = 0; j < port.getWidth(); j++) {
                                controllerHelper.setBufferSize(port, j, newSize);
                            }    
                        }
                        portNumber++;
                    }
                    if (actors[0] instanceof CompositeActor) {
                        ptolemy.actor.Director localDirector = actors[0].getDirector();
                        if(localDirector instanceof 
                                ptolemy.domains.hdf.kernel.HDFDirector ||
                                localDirector instanceof 
                                ptolemy.domains.hdf.kernel.HDFFSMDirector) {
                            TypedCompositeActor actorHelper 
                                    = (TypedCompositeActor) _getHelper((NamedObj) actors[0]);
                            int[] firingsPerGlobalIterationArrayOfActor 
                                    = actorHelper.getFiringsPerGlobalIteration();
                            if (firingsPerGlobalIterationArrayOfActor == null) {
                                firingsPerGlobalIterationArrayOfActor  
                                        = new int[length];
                                actorHelper.setFiringsPerGlobalIteration
                                        (firingsPerGlobalIterationArrayOfActor);
                            }
                            firingsPerGlobalIterationArrayOfActor[i] 
                                    = firingsPerGlobalIterationOfContainer;                               
                        }
                    }
                    configurationNumber++;
                }               
            }    
        }

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
        
        ptolemy.domains.hdf.kernel.HDFFSMDirector director = 
                (ptolemy.domains.hdf.kernel.HDFFSMDirector) getComponent();
        CompositeActor container = (CompositeActor) director.getContainer();
        ptolemy.codegen.c.actor.TypedCompositeActor containerHelper 
                = (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(container);
        
        code.append(containerHelper.processCode("$actorSymbol(fired) = 1;\n"));
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
        
        ptolemy.domains.hdf.kernel.HDFFSMDirector director = 
                (ptolemy.domains.hdf.kernel.HDFFSMDirector)
                getComponent();
        CompositeActor container = (CompositeActor) director.getContainer();
        ptolemy.codegen.c.actor.TypedCompositeActor containerHelper 
                = (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(container);
        
        code.append(containerHelper.processCode
                ("static unsigned char $actorSymbol(fired) = 0;\n"));
        
        return code.toString();
    }    
        
    public void generateSwitchModeCode(StringBuffer code) 
            throws IllegalActionException {

        super.generateSwitchModeCode(code);
        
        ptolemy.domains.fsm.kernel.FSMActor controller = 
            ((ptolemy.domains.fsm.kernel.FSMDirector)
            getComponent()).getController();
        FSMActor controllerHelper = (FSMActor) _getHelper(controller);
        
        ptolemy.domains.hdf.kernel.HDFFSMDirector director = 
            (ptolemy.domains.hdf.kernel.HDFFSMDirector) getComponent();
        CompositeActor container = (CompositeActor) director.getContainer();        
        ptolemy.codegen.c.actor.TypedCompositeActor containerHelper = 
            (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(container);
    
        code.append(containerHelper.processCode("if ($actorSymbol(fired)) {\n"));
         // generate code for non-preemptive transition
        code.append("\n/* Nonpreepmtive Transition */\n\n");
        controllerHelper.generateFireCode(code, new TransitionRetriever() {
            public Iterator retrieveTransitions(State state) {
                return state.nonpreemptiveTransitionList().iterator();  
            }
        });
        code.append(containerHelper.processCode("$actorSymbol(fired) = 0;\n"));
        code.append("}\n");
        
    }
}
