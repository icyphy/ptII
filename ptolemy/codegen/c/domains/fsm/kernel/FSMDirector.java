/* Code generator helper class associated with the FSMDirector class.

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

import ptolemy.actor.Actor;
import ptolemy.codegen.c.domains.fsm.kernel.FSMActor.TransitionRetriever;
import ptolemy.codegen.kernel.ActorCodeGenerator;
import ptolemy.codegen.kernel.Director;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// FSMDirector

/** 
 Code generator helper class associated with the FSMDirector class.
 
 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (zgang)
 @Pt.AcceptedRating Red (zgang)
 */

public class FSMDirector extends Director {

    /** Construct the code generator helper associated with the given FSMDirector.
     *  @param director The associated ptolemy.domains.fsm.kernel.FSMDirector
     */
    public FSMDirector(ptolemy.domains.fsm.kernel.FSMDirector director) {
        super(director);
    }
    
    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

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
        
        // generate code for preemptive transition
        code.append("\n/* Preepmtive Transition */\n\n");
        controllerHelper._generateFireCode(code, new TransitionRetriever() {
            public Iterator retrieveTransitions(State state) {
                return state.preemptiveTransitionList().iterator();  
            }
        });
        
        code.append("\n");
        
        // check to see if a preemptive transition is taken
        code.append("if ("
                + controllerHelper.processCode("$actorSymbol(transitionFlag)")
                + " == 0) {\n");
        
        // generate code for refinements
        _generateRefinementCode(code);
        
        // generate code for non-preemptive transition
        code.append("\n/* Nonpreepmtive Transition */\n\n");
        controllerHelper._generateFireCode(code, new TransitionRetriever() {
            public Iterator retrieveTransitions(State state) {
                return state.nonpreemptiveTransitionList().iterator();  
            }
        });
        
        code.append("}");
    }  
    
    /** Generate code for the firing of refinements.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException
     */
    protected void _generateRefinementCode(StringBuffer code) 
            throws IllegalActionException {
       
        ptolemy.domains.fsm.kernel.FSMActor controller = 
            ((ptolemy.domains.fsm.kernel.FSMDirector)
            getComponent()).getController();
    
        FSMActor controllerHelper = (FSMActor) _getHelper(controller);
        
        int depth = 1; 
        code.append(_getIndentPrefix(depth));
        code.append("switch (" 
                + controllerHelper.processCode("$actorSymbol(currentState)")
                + ") {\n");
        Iterator states = controller.entityList().iterator();
        int stateCount = 0;
        depth++;
        while (states.hasNext()) {
    
            code.append(_getIndentPrefix(depth));           
            code.append("case " + stateCount + ":\n");
            stateCount++;
      
            depth++;
            State state = (State) states.next();
            Actor[] actors = state.getRefinement();
            for (int i = 0; i < actors.length; i++) {
            
                ActorCodeGenerator actorHelper = 
                        (ActorCodeGenerator) _getHelper((NamedObj) actors[i]); 
                actorHelper.generateFireCode(code);                
            }
            
            code.append(_getIndentPrefix(depth));
            code.append("break;\n");//end of case statement
            depth--;
        }
        depth--;
        code.append(_getIndentPrefix(depth));
        code.append("}\n");//end of switch statemen
    }    
}
