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
import ptolemy.actor.Director;
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

    /** Generate the fire code of the associated FSMActor.  It provies 
     *  generateTransitionCode(StringBuffer, TransitionRetriever) with an
     *  anonymous class implementing a method which returns an iterator of
     *  all outgoing transitions of the current state. 
     * 
     *  @return The generated fire code.
     *  @exception IllegalActionException If thrown while generating firing code.
     */
    public String generateFireCode() throws IllegalActionException {
        
        StringBuffer code = new StringBuffer();
        code.append(super.generateFireCode());

        generateTransitionCode(code, new TransitionRetriever() {
            public Iterator retrieveTransitions(State state) {
                return state.outgoingPort.linkedRelationList().iterator();
            }
        });
        return code.toString();
    }
    
    /** Generate the initialize code of the associated FSMActor. It generates
     *  code for initializing current state with initial state,  and initializing
     *  current configuration of the container when it applies (i.e., when
     *  this FSMActor works as a modal controller for a MultirateFSMDirector).  
     * 
     *  @return The initialize code of the associated FSMActor.
     *  @exception IllegalActionException If initial state cannot be found,
     *   configuraton number cannot be updated or code cannot be processed.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer codeBuffer = new StringBuffer();
        codeBuffer.append(super.generateInitializeCode());

        ptolemy.domains.fsm.kernel.FSMActor fsmActor = 
                (ptolemy.domains.fsm.kernel.FSMActor) getComponent();
        State initialState = fsmActor.getInitialState();
        
        _updateCurrentState(codeBuffer, initialState, 0);
        
        return processCode(codeBuffer.toString());
    }
    
    /** Generate the preinitialize code of the associated FSMActor. It declares
     *  two variables for this actor: currentState and transitionFlag. 
     *  currentState is an int representing this actor's current state.
     *  transitionFlag is an unsigned char to indicate if a preemptive
     *  transition is taken.
     * 
     *  @return The preinitialize code of the associated FSMActor.
     *  @exception IllegalActionException If thrown when creating buffer 
     *   size and offset map or processing code.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generatePreinitializeCode());
        code.append("static int $actorSymbol(currentState);\n");
        code.append("static unsigned char $actorSymbol(transitionFlag);\n");
        return processCode(code.toString());
    }
      
    /** Generate a set of shared codes of the associated FSMActor.
     *  It generates code for defining macro true and false.
     *  
     *  @return a set of shared codes.
     *  @exception IllegalActionException Not thrown here.
     */
    public Set generateSharedCode() throws IllegalActionException {
        Set set = new HashSet();
        set.addAll(super.generateSharedCode());
        set.add("#define true 1\n#define false 0\n");
        return set;
    }

    
    /** Generate code for making transition. It generates code for both choice 
     *  action and commit action.
     * 
     *  @param code The string buffer that the generated code is appended to.
     *  @param transitionRetriever An instance of a class implementing a method 
     *   which returns an iterator of all, preemptive or non-preemptive 
     *   transitions of the current state. 
     *  @throws IllegalActionException If thrown while generating transition code.
     */
    public void generateTransitionCode(StringBuffer code,
            TransitionRetriever transitionRetriever)
            throws IllegalActionException {
        StringBuffer codeBuffer = new StringBuffer();

        ptolemy.domains.fsm.kernel.FSMActor fsmActor 
                = (ptolemy.domains.fsm.kernel.FSMActor) getComponent();

        int depth = 1;
        codeBuffer.append(_getIndentPrefix(depth));
        // The default value 1 of transitionFlag means the transition 
        // will be taken. If no transition is actually taken, it will be
        // set to value 0.
        codeBuffer.append("$actorSymbol(transitionFlag) = 1;\n");
        codeBuffer.append(_getIndentPrefix(depth));
        // States are numbered according to the order they are created,
        // i.e., the same order as in list returned by the method entityList().
        codeBuffer.append("switch ($actorSymbol(currentState)) {\n");

        Iterator states = fsmActor.entityList().iterator();
        int stateCount = 0;
        depth++;

        while (states.hasNext()) {
            codeBuffer.append(_getIndentPrefix(depth));
            // For each state...
            codeBuffer.append("case " + stateCount + ":\n");
            stateCount++;

            State state = (State) states.next();
            
            // The transitions (all, preemptive or non-preemptive depending on the 
            // instance of TransitionRetriever given) that need to be tried.
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
                parseTreeCodeGenerator
                        .evaluateParseTree(guardParseTree, _scope);
                codeBuffer.append(parseTreeCodeGenerator.generateFireCode());
                codeBuffer.append(") {\n");

                depth++;

                // generate code for choice action
                Iterator actions = transition.choiceActionList().iterator();

                while (actions.hasNext()) {
                    AbstractActionsAttribute action 
                            = (AbstractActionsAttribute) actions.next();
                    Iterator destinationNameList = action
                            .getDestinationNameList().iterator();

                    while (destinationNameList.hasNext()) {
                        String destinationName = (String) destinationNameList.next();
                        NamedObj destination = (NamedObj) action
                                .getDestination(destinationName);
                        
                        int channel = -1;
                        if (action.isChannelSpecified(destinationName)) {
                            channel = action.getChannel(destinationName);
                        }

                        ASTPtRootNode parseTree 
                                = action.getParseTree(destinationName);

                        codeBuffer.append(_getIndentPrefix(depth));

                        // Note in choice action only output can be generated
                        // and no parameter be changed.
                        if (channel >= 0) {
                            codeBuffer.append("$ref(" + destinationName + "#"
                                    + channel + ") = ");

                            // During choice action, an output port receives token
                            // sent by itself when it is also an input port, i.e.,
                            // when this FSMActor is used as a modal controller.
                            if (((IOPort) destination).isInput()) {
                                codeBuffer.append(destination.getFullName()
                                        .replace('.', '_'));

                                if (((IOPort) destination).isMultiport()) {
                                    codeBuffer.append("[" + channel + "]");
                                }

                                codeBuffer.append(" = ");
                            }
                        } else { // broadcast

                            int width = ((IOPort) action.getDestination
                                    (destinationName)).getWidth();
                            
                            for (int i = 0; i < width; i++) {
                                codeBuffer.append("$ref(" + destinationName
                                        + "#" + i + ") = ");

                                // During choice action, an output port receives token
                                // sent by itself when it is also an input port, i.e.,
                                // when this FSMActor is used as a modal controller.
                                if (((IOPort) destination).isInput()) {
                                    codeBuffer.append(destination.getFullName()
                                            .replace('.', '_'));

                                    if (((IOPort) destination).isMultiport()) {
                                        codeBuffer.append("[" + i + "]");
                                    }

                                    codeBuffer.append(" = ");
                                }
                            }
                        }

                        parseTreeCodeGenerator = new ParseTreeCodeGenerator();
                        parseTreeCodeGenerator.evaluateParseTree(parseTree, _scope);
                        codeBuffer.append(parseTreeCodeGenerator.generateFireCode());
                        codeBuffer.append(";\n");
                    }
                }

                boolean inline = ((BooleanToken) 
                        _codeGenerator.inline.getToken()).booleanValue();
            
                // generate code for transition refinement
                Actor[] actors = transition.getRefinement();

                if (actors != null) {
                    for (int i = 0; i < actors.length; i++) {
                        ActorCodeGenerator helper = (ActorCodeGenerator) 
                                _getHelper((NamedObj) actors[i]);
                        // fire the actor
                        if (inline) {
                            codeBuffer.append(helper.generateFireCode());
                        } else {
                            codeBuffer.append(actors[i].getFullName().replace
                                    ('.' , '_') + "();\n");   
                        }
                    }
                }

                // generate code for commit action
                actions = transition.commitActionList().iterator();

                while (actions.hasNext()) {
                    AbstractActionsAttribute action 
                            = (AbstractActionsAttribute) actions.next();
                    Iterator destinationNameList = action
                            .getDestinationNameList().iterator();

                    while (destinationNameList.hasNext()) {
                        String destinationName = (String) destinationNameList.next();
                        NamedObj destination = (NamedObj) action
                                .getDestination(destinationName);
                        
                        int channel = -1;
                        if (action.isChannelSpecified(destinationName)) {
                            channel = action.getChannel(destinationName);
                        }

                        ASTPtRootNode parseTree = action
                                .getParseTree(destinationName);

                        codeBuffer.append(_getIndentPrefix(depth));

                        if (destination instanceof IOPort) {
                            if (channel >= 0) {
                                codeBuffer.append("$ref(" + destinationName
                                        + "#" + channel + ") = ");
                            } else { // broadcast

                                int width = ((IOPort) action.getDestination
                                        (destinationName)).getWidth();

                                for (int i = 0; i < width; i++) {
                                    codeBuffer.append("$ref(" + destinationName
                                            + "#" + i + ") = ");
                                }
                            }
                        } else if (destination instanceof Variable) {
                            codeBuffer.append(destination.getFullName()
                                    .replace('.', '_') + " = ");
                        }

                        parseTreeCodeGenerator = new ParseTreeCodeGenerator();
                        parseTreeCodeGenerator.evaluateParseTree(parseTree, _scope);
                        codeBuffer.append(parseTreeCodeGenerator.generateFireCode());
                        codeBuffer.append(";\n");
                    }
                }

                // generate code for updating current state
                State destinationState = transition.destinationState();
                _updateCurrentState(codeBuffer, destinationState, depth);

                // generate code for reinitialization if reset is true.             
                // we assume the value of reset itself cannot be changed dynamically
                BooleanToken resetToken = (BooleanToken) transition.reset.getToken();
                if (resetToken.booleanValue()) {
                    actors = destinationState.getRefinement();

                    if (actors != null) {
                        for (int i = 0; i < actors.length; ++i) {
                            ActorCodeGenerator helper = (ActorCodeGenerator) 
                                    _getHelper((NamedObj) actors[i]);
                            codeBuffer.append(helper.generateInitializeCode());
                        }
                    }
                }
                
                // Generate code for updating configuration number of this 
                // FSMActor's container.
                // The code is generated only when this FSMActor is used as a modal 
                // controller for an instance of MultirateFSMDirector.
                Director director = fsmActor.getExecutiveDirector();
                if (director instanceof 
                        ptolemy.domains.fsm.kernel.MultirateFSMDirector) {
                    MultirateFSMDirector directorHelper = 
                            (MultirateFSMDirector) _getHelper((NamedObj) director);
                    directorHelper._updateConfigurationNumber
                            (codeBuffer, destinationState);
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
            // indicates no transition is taken.
            codeBuffer.append("$actorSymbol(transitionFlag) = 0;\n");
            
            // Generate code for updating configuration number of this FSMActor's
            // container.
            // Note we need this because the configuration of the current refinement 
            // may have been changed even when there is no state transition.
            // The code is generated only when this FSMActor is used as a modal 
            // controller for an instance of MultirateFSMDirector.
            Director director = fsmActor.getExecutiveDirector();
            if (director instanceof 
                    ptolemy.domains.fsm.kernel.MultirateFSMDirector) {
                MultirateFSMDirector directorHelper = 
                        (MultirateFSMDirector) _getHelper((NamedObj) director);
                directorHelper._updateConfigurationNumber(codeBuffer, state);
            }
            depth--;

            if (transitionCount > 0) {
                codeBuffer.append(_getIndentPrefix(depth));
                codeBuffer.append("} \n"); // end of if statement
            }

            codeBuffer.append(_getIndentPrefix(depth));
            codeBuffer.append("break;\n"); // end of case statement 
            depth--;
        }

        depth--;
        codeBuffer.append(_getIndentPrefix(depth));
        codeBuffer.append("}\n"); // end of switch statement   
        code.append(processCode(codeBuffer.toString()));
    }
    
    /** A class implementing this interface implements a method to retrieve
     *  transitions of a given state. Depending on implementation, it could 
     *  return all transitions, only preemptive transtions or only non-preemptive
     *  transitions.
     */
    public static interface TransitionRetriever {
        /** Returns an iterator of (some or all) transitions from the given state.
         *  @param The given state.
         *  @return An iterator of (some or all) transitions from the given state. 
         */
        public Iterator retrieveTransitions(State state);
    }

    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate code for updating current state of this FSMActor. The states are
     *  numbered according to the order in the list returned by entityList().
     * 
     *  @param codeBuffer The string buffer that the generated code is appended to.
     *  @param state The current state.
     *  @param depth The depth in the hierarchy, to determine indenting.
     */ 
    protected void _updateCurrentState(StringBuffer codeBuffer, 
            State state, int depth) {
        ptolemy.domains.fsm.kernel.FSMActor fsmActor 
                = (ptolemy.domains.fsm.kernel.FSMActor) getComponent();
        Iterator states = fsmActor.entityList().iterator();
        int stateCounter = 0;

        while (states.hasNext()) {
            if (states.next() == state) {   
                codeBuffer.append("$actorSymbol(currentState) = " 
                        + stateCounter + ";\n");
                break;
            }
            stateCounter++;
        }    
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables                 ////
    
    /** The scope to generate code for guard expression, choice action 
     *  and commit action.
     */
    protected HelperScope _scope = new HelperScope();
}
