/* Code generator helper for FSMActor.

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
import ptolemy.actor.IOPort;
import ptolemy.codegen.c.actor.lib.ParseTreeCodeGenerator;
import ptolemy.codegen.kernel.ActorCodeGenerator;
import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.Variable;
import ptolemy.domains.fsm.kernel.AbstractActionsAttribute;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// FSMActor

/**
 Code generator helper for FSMActor.
 
 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (zgang)
 @Pt.AcceptedRating Red (zgang)
 */
public class FSMActor extends CCodeGeneratorHelper {

    /** Construct the code generator helper associated with the given FSMActor.
     *  @param component The associated component.
     */
    public FSMActor(ptolemy.domains.fsm.kernel.FSMActor component) {
        super(component);
    }
    
    /////////////////////////////////////////////////////////////////////
    ////                           public methods                    ////
    
    /** Generate the fire code of the associated FSMActor.
     *  @param code
     *  @exception IllegalActionException 
     */  
    public void generateFireCode(StringBuffer code)
            throws IllegalActionException {
        
        super.generateFireCode(code);
        
        _generateFireCode(code, new TransitionRetriever() {
            public Iterator retrieveTransitions(State state) {
                return state.outgoingPort.linkedRelationList().iterator();    
            }
        });
        
    } 
    
    /** Generate the initialize code of the associated FSMActor. 
     *  @return The processed code string.
     *  @exception IllegalActionException
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateInitializeCode());
        
        ptolemy.domains.fsm.kernel.FSMActor fsmActor 
                = (ptolemy.domains.fsm.kernel.FSMActor) getComponent();
        State initialState = fsmActor.getInitialState(); 
        Iterator allStates = fsmActor.entityList().iterator();
        int counter = 0;
        while (allStates.hasNext()) {
            if(allStates.next() == initialState) {
                code.append("$actorSymbol(currentState) = " + counter + ";\n");
                break;
            }
            counter++;
        }
        return processCode(code.toString());
    }
    
    /** Generate the preinitialize code of the associated FSMActor. 
     *  @return The processed code string.
     *  @exception IllegalActionException
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        _scope = new HelperScope();
        code.append(super.generatePreinitializeCode());
        code.append("int $actorSymbol(currentState);\n");
        code.append("unsigned char $actorSymbol(transitionFlag);\n");
        return processCode(code.toString());
    }
          
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    protected void _generateFireCode(StringBuffer code, 
            TransitionRetriever transitionRetriever) 
            throws IllegalActionException {
                
        StringBuffer codeBuffer = new StringBuffer();
        
        ptolemy.domains.fsm.kernel.FSMActor fsmActor 
                = (ptolemy.domains.fsm.kernel.FSMActor) getComponent();

        int depth = 1;
        codeBuffer.append(_getIndentPrefix(depth));    
        codeBuffer.append("$actorSymbol(transitionFlag) = 1;\n");
        codeBuffer.append(_getIndentPrefix(depth));
        codeBuffer.append("switch ($actorSymbol(currentState)) {\n");
       
        Iterator states = fsmActor.entityList().iterator();
        int stateCount = 0;
        depth++;
        while (states.hasNext()) {
    
            codeBuffer.append(_getIndentPrefix(depth));           
            codeBuffer.append("case " + stateCount + ":\n");
            stateCount++;
      
            State state = (State) states.next();
            Iterator transitions = transitionRetriever.retrieveTransitions(state);
            int transitionCount = 0;            
            depth++;
            while (transitions.hasNext()) {
                if (transitionCount == 0) {
                    codeBuffer.append(_getIndentPrefix(depth));                   
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
                ParseTreeCodeGenerator parseTreeCodeGenerator 
                        = new ParseTreeCodeGenerator();
                parseTreeCodeGenerator.evaluateParseTree(guardParseTree, _scope);
                codeBuffer.append(parseTreeCodeGenerator.generateFireCode());
                codeBuffer.append(") {\n");
        
                depth++;
                // generate code for choice action
                Iterator actions = transition.choiceActionList().iterator();
                while (actions.hasNext()) {
                    AbstractActionsAttribute action 
                            = (AbstractActionsAttribute) actions.next();
                    Iterator destinationNameList 
                            = action.getDestinationNameList().iterator();
                    while (destinationNameList.hasNext()) {
                        String destinationName = (String) destinationNameList.next();
                        NamedObj destination = (NamedObj) 
                                action.getDestination(destinationName);
                        int channel = -1;
                        if (action.isChannelSpecified(destinationName)) {
                            channel = action.getChannel(destinationName);
                        }
                        ASTPtRootNode parseTree = action.getParseTree(destinationName);
                  
                        codeBuffer.append(_getIndentPrefix(depth));
                        if (channel >= 0) {
                            codeBuffer.append("$ref(" + 
                                    destinationName + "#" + channel + ") = ");
                            // During choice action, an output port receives tokens
                            // sent by itself if it is also an input port
                            if (((IOPort) destination).isInput()) {
                                codeBuffer.append(destination.getFullName().replace('.', '_'));
                                if (((IOPort) destination).isMultiport()) {
                                    codeBuffer.append( "[" + channel + "]");
                                }
                                codeBuffer.append( " = ");
                            }
                        } else {// broadcast
                            int width = ((IOPort) action.getDestination
                                    (destinationName)).getWidth();;
                            for (int i = 0; i < width; i++) {
                                codeBuffer.append("$ref(" + 
                                        destinationName + "#" + i + ") = ");
                                // During choice action, an output port receives tokens
                                // sent by itself if it is also an input port
                                if (((IOPort) destination).isInput()) {
                                    codeBuffer.append(destination.getFullName().replace('.', '_'));
                                    if (((IOPort) destination).isMultiport()) {
                                        codeBuffer.append( "[" + i + "]");
                                    }
                                    codeBuffer.append( " = ");
                                }                               
                            }
                        }
                
                        parseTreeCodeGenerator = new ParseTreeCodeGenerator();
                        parseTreeCodeGenerator.evaluateParseTree(parseTree, _scope);
                        codeBuffer.append(parseTreeCodeGenerator.generateFireCode());
                        codeBuffer.append(";\n");                       
                    }
                }
                
                // generate code for transition refinement
                Actor[] actors = transition.getRefinement();
                if (actors != null) {
                    for (int i = 0; i < actors.length; ++i) {
                        ActorCodeGenerator helper = 
                                (ActorCodeGenerator) _getHelper((NamedObj) actors[i]);
                        helper.generateFireCode(codeBuffer);
                    }
                }
        
                // generate code for commit action
                actions = transition.commitActionList().iterator();
                while (actions.hasNext()) {
                    AbstractActionsAttribute action 
                            = (AbstractActionsAttribute) actions.next();
                    Iterator destinationNameList 
                            = action.getDestinationNameList().iterator();
                    while (destinationNameList.hasNext()) {
                        String destinationName = (String) destinationNameList.next();
                        NamedObj destination = (NamedObj) 
                                action.getDestination(destinationName);
                        int channel = -1;
                        if (action.isChannelSpecified(destinationName)) {
                            channel = action.getChannel(destinationName);
                        }
                        ASTPtRootNode parseTree = action.getParseTree(destinationName);
                
                        codeBuffer.append(_getIndentPrefix(depth));
                        if (destination instanceof IOPort) {
                            if (channel >= 0) {
                                codeBuffer.append("$ref(" + 
                                        destinationName + "#" + channel + ") = ");       
                            } else {// broadcast
                                int width = ((IOPort) action.getDestination
                                        (destinationName)).getWidth();;
                                for (int i = 0; i < width; i++) {
                                    codeBuffer.append("$ref(" + 
                                            destinationName + "#" + i + ") = ");    
                       
                                }
                            }
                        } else if(destination instanceof Variable) {
                            codeBuffer.append(destination.getFullName().replace('.', '_') + " = ");
                        }
                
                        parseTreeCodeGenerator = new ParseTreeCodeGenerator();
                        parseTreeCodeGenerator.evaluateParseTree(parseTree, _scope);
                        codeBuffer.append(parseTreeCodeGenerator.generateFireCode());
                        codeBuffer.append(";\n");                       
                    }
                }
                        
                // generate code for updating new state
                State destinationState = transition.destinationState();
                Iterator allStates = fsmActor.entityList().iterator();
                int counter = 0;
                while (allStates.hasNext()) {
                    if(allStates.next() == destinationState) {
                        codeBuffer.append(_getIndentPrefix(depth));
                        codeBuffer.append("$actorSymbol(currentState) = " 
                                + counter + ";\n");
                        break;
                    }
                    counter++;
                }
                
                // generate code for reinitialization if reset is true
                BooleanToken resetToken = (BooleanToken) transition.reset.getToken();
                if (resetToken.booleanValue()) {
                    actors = destinationState.getRefinement();
                    if (actors != null) {
                        for (int i = 0; i < actors.length; ++i) {
                            ActorCodeGenerator helper = 
                                    (ActorCodeGenerator) _getHelper((NamedObj) actors[i]);
                            codeBuffer.append(helper.generateInitializeCode());
                        }
                    }
                }
                
                depth--;
                codeBuffer.append(_getIndentPrefix(depth));
                codeBuffer.append("} ");
            }
            if (transitionCount > 0) {
                codeBuffer.append("else {\n");   
            } else {
                codeBuffer.append("\n");
            }
            depth++;
            codeBuffer.append(_getIndentPrefix(depth));
            codeBuffer.append("$actorSymbol(transitionFlag) = 0;\n");
            depth--;
            if (transitionCount > 0) {
                codeBuffer.append(_getIndentPrefix(depth));
                codeBuffer.append("} \n");//end of if statement
            }
            codeBuffer.append(_getIndentPrefix(depth));
            codeBuffer.append("break;\n");//end of case statement 
            depth--;
        }
        depth--;
        codeBuffer.append(_getIndentPrefix(depth));
        codeBuffer.append("}\n");//end of switch statement   
        code.append(processCode(codeBuffer.toString()));
    }
    
    protected static interface TransitionRetriever {
        
        public Iterator retrieveTransitions(State state);
   
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    private HelperScope _scope = null;

    
}    