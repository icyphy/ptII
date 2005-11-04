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
    
    /** Generate code for declaring read and write offset variables if needed. 
     *  First find out the maximum buffer sizes needed for controller ports
     *  in one global iteration. Then relay the information of firings per global
     *  iteration of the modal model (which is the container of this director)
     *  to the refinements if necessary. Finally call the same method in its 
     *  super class to create read and write offset variables if needed.
     * 
     *  @return The generated code.
     *  @exception IllegalActionException If thrown while creating offset variables.
     */
    // FIXME: The maximum controller port buffer size in one global iteration is 
    // potentially large. But we can infer the number of tokens needed in one 
    // global iteration from guard expression and only keep that many spaces. 
    // However this assumes the index into array in the expression is fixed.
    // Most of the time (or all of the time? )this should be true. If the index
    // can be dynamically changed, then this would not work.
    public String createOffsetVariablesIfNeeded() throws IllegalActionException {
        StringBuffer code = new StringBuffer();  
        
        ptolemy.domains.fsm.kernel.MultirateFSMDirector director = 
                (ptolemy.domains.fsm.kernel.MultirateFSMDirector) getComponent();
        CompositeActor container = (CompositeActor) director.getContainer();
        ptolemy.domains.fsm.kernel.FSMActor controller = director.getController();
        ptolemy.codegen.c.actor.TypedCompositeActor containerHelper = 
                (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(container);
        CodeGeneratorHelper controllerHelper =
                (CodeGeneratorHelper) _getHelper(controller);
        int[] arrayOfFiringsPerGlobalIterationOfContainer = 
                containerHelper.getFiringsPerGlobalIteration();
        int[][] containerRates = containerHelper.getRates();
        
        Iterator states = controller.entityList().iterator();
        int configurationNumber = 0;
        while (states.hasNext()) {
            State state = (State) states.next();
            TypedActor[] actors = state.getRefinement();
            if (actors != null) {
                // There should be at most one refinement for each state.
                CodeGeneratorHelper refinementHelper = 
                        (CodeGeneratorHelper) _getHelper((NamedObj) actors[0]);
                int[][] rates = refinementHelper.getRates();
                int length = 1;
                if (rates != null) {
                    // The length of rates represents the number of configurations
                    // of the refinement.
                    length = rates.length;
                }    
                for (int i = 0; i < length; i++) {
                    
                    int firingsPerGlobalIterationOfContainer
                            = arrayOfFiringsPerGlobalIterationOfContainer
                            [configurationNumber]; 
                    Iterator ports = controller.portList().iterator();
                    int portNumber = 0;
                    while (ports.hasNext()) {
                        IOPort port = (IOPort) ports.next();
                        // Find the new controller port buffer sizes needed in one 
                        // global iteration from container port rates and container's
                        // firings per global iteration.
                        int newSize = firingsPerGlobalIterationOfContainer * 
                                containerRates[configurationNumber][portNumber];
                        // All channels have same buffer size, so we use channel 0.
                        int oldSize = controllerHelper.getBufferSize(port, 0);
                        if (oldSize < newSize) {
                            for (int j = 0; j < port.getWidth(); j++) {
                                controllerHelper.setBufferSize(port, j, newSize);
                            }    
                        }
                        portNumber++;
                    }
                    
                    // If the refinement's local director is HDFDirector
                    // or HDFFSMDirector, set the firings per global iteration
                    // of the refinemenet the same as the firings per global 
                    // iteration of the container. This way we can relay the 
                    // information of firings per global iteration to the inside.
                    if (actors[0] instanceof CompositeActor) {
                        ptolemy.actor.Director localDirector = actors[0].getDirector();
                        if(localDirector instanceof 
                                ptolemy.domains.hdf.kernel.HDFDirector ||
                                localDirector instanceof 
                                ptolemy.domains.hdf.kernel.HDFFSMDirector) {
                            TypedCompositeActor actorHelper = (TypedCompositeActor) 
                                    _getHelper((NamedObj) actors[0]);
                            int[] arrayOfFiringsPerGlobalIterationOfActor =
                                    actorHelper.getFiringsPerGlobalIteration();
                            if (arrayOfFiringsPerGlobalIterationOfActor == null) {
                                arrayOfFiringsPerGlobalIterationOfActor = 
                                        new int[length];
                                actorHelper.setFiringsPerGlobalIteration
                                        (arrayOfFiringsPerGlobalIterationOfActor);
                            }
                            arrayOfFiringsPerGlobalIterationOfActor[i] =
                                    firingsPerGlobalIterationOfContainer;                               
                        }
                    }
                    configurationNumber++;
                }               
            }    
        }

        code.append(super.createOffsetVariablesIfNeeded());
        return code.toString();
    }
    
    /** Generate the code for the firing of actors controlled by this director.
     *  It generates code for firing refinements and setting a variable to record it.
     * 
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating fire code for the actor.
     */
    public void generateFireCode(StringBuffer code) throws IllegalActionException {       
        
        // Like MultirateFSMDirector, no preemptive transition is taken under 
        // the control of this director.
        
        // generate code for refinements
        _generateRefinementCode(code);
        
        ptolemy.domains.hdf.kernel.HDFFSMDirector director = 
                (ptolemy.domains.hdf.kernel.HDFFSMDirector) getComponent();
        CompositeActor container = (CompositeActor) director.getContainer();
        ptolemy.codegen.c.actor.TypedCompositeActor containerHelper = 
                (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(container);
        
        // Unlike MultirateFSMDirector, no non-preemptive transition is taken
        // at this point. Instead a variable is set to record the firing of the
        // modal model. This variable is used when doing mode transition after
        // one global iteration.
        code.append(containerHelper.processCode("$actorSymbol(fired) = 1;\n"));
    }   
    
    /** Generate mode transition code. The mode transition code generated in this 
     *  method is executed after each global iteration. 
     * 
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If the director helper throws it 
     *   while generating mode transition code. 
     */
    public void generateModeTransitionCode(StringBuffer code)
            throws IllegalActionException {

        super.generateModeTransitionCode(code);

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
        controllerHelper.generateTransitionCode(code, new TransitionRetriever() {
            public Iterator retrieveTransitions(State state) {
                return state.nonpreemptiveTransitionList().iterator();  
            }
        });
        // reset the variable 
        code.append(containerHelper.processCode("$actorSymbol(fired) = 0;\n"));
        code.append("}\n");
    }
    
    /** Generate the preinitialize code for this director. Declare a variable
     *  which is used to record the firing of this director.
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If getting the helper fails,
     *   or if generating the preinitialize code for a helper fails,
     *   or if there is a problem processing the code.
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
}
