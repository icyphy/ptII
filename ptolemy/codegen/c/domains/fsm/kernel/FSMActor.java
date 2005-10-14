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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.codegen.c.actor.lib.ParseTreeCodeGenerator;
import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.Variable;
import ptolemy.domains.fsm.kernel.AbstractActionsAttribute;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

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
        
        StringBuffer codeBuffer = new StringBuffer();
        codeBuffer.append("\n/* fire " + getComponent().getName() + " */\n\n");
        
        ptolemy.domains.fsm.kernel.FSMActor fsmActor 
                = (ptolemy.domains.fsm.kernel.FSMActor) getComponent();
        
        int depth = 1;
        codeBuffer.append(_getIndentPrefix(depth));        
        codeBuffer.append("switch($actorSymbol(currentState)) {\n");
               
        Iterator states = fsmActor.entityList().iterator();
        int stateCount = 0;
        depth++;
        while (states.hasNext()) {
            
            codeBuffer.append(_getIndentPrefix(depth));           
            codeBuffer.append("case " + stateCount + ":\n");
            stateCount++;
            
            State state = (State) states.next();
            Iterator transitions = state.outgoingPort.linkedRelationList().iterator();
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
                        int channel = -1;
                        if (action.isChannelSpecified(destinationName)) {
                            channel = action.getChannel(destinationName);
                        }
                        ASTPtRootNode parseTree = action.getParseTree(destinationName);
                        
                        codeBuffer.append(_getIndentPrefix(depth));
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
                        
                        parseTreeCodeGenerator = new ParseTreeCodeGenerator();
                        parseTreeCodeGenerator.evaluateParseTree(parseTree, _scope);
                        codeBuffer.append(parseTreeCodeGenerator.generateFireCode());
                        codeBuffer.append(";\n");                       
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
                            // FIXME: what if the variable belongs to one refinement
                            codeBuffer.append("$ref(" + destinationName + ") = ");
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
                depth--;
                codeBuffer.append(_getIndentPrefix(depth));
                codeBuffer.append("} ");//end of if statement
            }
            codeBuffer.append("\n");
            codeBuffer.append(_getIndentPrefix(depth));
            codeBuffer.append("break;\n");//end of case statement 
            depth--;
        }
        depth--;
        codeBuffer.append(_getIndentPrefix(depth));
        codeBuffer.append("}\n");//end of switch statement
        code.append(processCode(codeBuffer.toString()));
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
        _scope = new PortScope();
        code.append(super.generatePreinitializeCode());
        code.append("int $actorSymbol(currentState);\n");
        return processCode(code.toString());
    }
    
    /** Generate the shared code. 
     *  @return the shared code.
     *  @exception IllegalActionException
     */
     public Set generateSharedCode() throws IllegalActionException {
         Set codeBlocks = new HashSet();
         String codeBlock = "#define true 1\n#define false 0\n";
         codeBlocks.add(codeBlock);
         return codeBlocks;
     }
    
    /** Return a number of spaces that is proportional to the argument.
     *  If the argument is negative or zero, return an empty string.
     *  @param level The level of indenting represented by the spaces.
     *  @return A string with zero or more spaces.
     */
    protected static String _getIndentPrefix(int level) {
        return StringUtilities.getIndentPrefix(level);
    }
    
    /** This class implements a scope, which is used to generate the
     *  parsed expressions in target language.
     */
    private class PortScope extends ModelScope {
        /** Look up and return the macro corresponding to the specified 
         *  name in the scope. Return null if such a macro does not exist.
         *  @return The macro with the specified name in the scope.
         * @throws IllegalActionException
         *  @exception IllegalActionException Not thrown here.
         */
        public ptolemy.data.Token get(String name) 
            throws IllegalActionException {
            
            Actor actor = (Actor) FSMActor.this.getComponent();
            Iterator inputPorts = actor.inputPortList().iterator();
           
            // FIXME: need to consider multiport, multiple token consumption
            while (inputPorts.hasNext()) {
                IOPort inputPort = (IOPort)inputPorts.next();
                if (inputPort.getName().equals(name)) {
                    return new ObjectToken("$ref(" + name + ")");    
                }
            }
            
            // FIXME: if a parameter is never re-assigned in any commit actions,
            // then we should only use $val(name). This requires two parses.
            Attribute attribute = ((NamedObj) actor).getAttribute(name);
            if (attribute != null) {
                return new ObjectToken("$ref(" + name + ")") ;  
            } else { 
                attribute = getScopedVariable(null, (NamedObj) actor, name);
            }
            if (attribute != null) {
                return new ObjectToken("$val(" + name + ")") ;  
            } else {
                return null;
            }
        }

        /** This method should not be called.
         *  @exception IllegalActionException If it is called.
         */
        public ptolemy.data.type.Type getType(String name)
                throws IllegalActionException {
            throw new IllegalActionException("This method should not be called."); 
        }

        /** This method should not be called.
         *  @exception IllegalActionException If it is called.
         */
        public ptolemy.graph.InequalityTerm getTypeTerm(String name)
                throws IllegalActionException {
            throw new IllegalActionException("This method should not be called.");
        }

        /** This method should not be called.
         *  @throws IllegalActionException If it is called.
         */
        public Set identifierSet() throws IllegalActionException {
            throw new IllegalActionException("This method should not be called.");
        }
    }

    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    private PortScope _scope = null;

    
}    