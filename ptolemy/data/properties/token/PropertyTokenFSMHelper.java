/* A helper class for ptolemy.actor.AtomicActor.

 Copyright (c) 2006 The Regents of the University of California.
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
package ptolemy.data.properties.token;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.properties.PropertyHelper;
import ptolemy.domains.fsm.kernel.AbstractActionsAttribute;
import ptolemy.domains.fsm.kernel.CommitActionsAttribute;
import ptolemy.domains.fsm.kernel.OutputActionsAttribute;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;

//////////////////////////////////////////////////////////////////////////
//// FSMActor

/**
 A helper class for ptolemy.actor.FSMActor.

 @author Man-Kit Leung, Thomas Mandl
 @version $Id$
 @since Ptolemy II 6.2
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class PropertyTokenFSMHelper extends PropertyTokenCompositeHelper {

    /**
     * Construct a helper for the given AtomicActor. This is the
     * helper class for any ActomicActor that does not have a
     * specific defined helper class. Default actor constraints
     * are set for this helper. 
     * @param actor The given ActomicActor.
     * @param lattice The staticDynamic lattice.
     * @throws IllegalActionException 
     */
    public PropertyTokenFSMHelper(PropertyTokenSolver solver, 
            ptolemy.domains.fsm.kernel.FSMActor actor)
            throws IllegalActionException {
        
        super(solver, actor);
    }

    public List<Object> getPropertyables() {
        List<Object> result = super.getPropertyables();

//        if (!getSolver().isListening()) {
            ptolemy.domains.fsm.kernel.FSMActor actor = 
                (ptolemy.domains.fsm.kernel.FSMActor) getComponent();

            Iterator states = actor.entityList(State.class).iterator();
            while (states.hasNext()) {
                State state = (State) states.next();
            
                Iterator transitions = 
                    state.outgoingPort.linkedRelationList().iterator();
    
                while (transitions.hasNext()) {
                    Transition transition = (Transition) transitions.next();
                    result.add(transition.guardExpression);
                }

//            }
        }

        return result;
    }    

    protected List<PropertyHelper> _getSubHelpers() throws IllegalActionException {
        List<PropertyHelper> helpers = new ArrayList<PropertyHelper>();
//        if (!getSolver().isListening()) {
            helpers.addAll(_getASTNodeHelpers());
//        }
        return helpers;
    }   
    
    protected List<ASTPtRootNode> _getAttributeParseTrees() {

        List<ASTPtRootNode> result = super._getAttributeParseTrees();

        ptolemy.domains.fsm.kernel.FSMActor actor = 
            (ptolemy.domains.fsm.kernel.FSMActor) getComponent();

        Iterator states = actor.entityList(State.class).iterator();
        while (states.hasNext()) {
            State state = (State) states.next();
        
            Iterator transitions = 
                state.outgoingPort.linkedRelationList().iterator();

            while (transitions.hasNext()) {
                Transition transition = (Transition) transitions.next();
                
                try {
                    result.add(getParseTree(transition.guardExpression));

                } catch (IllegalActionException ex) {
                    throw new AssertionError(
                            "Problem parsing the guard expression: " + 
                            transition.getGuardExpression() + "\n" + 
                            KernelException.stackTraceToString(ex));
                }
                
                OutputActionsAttribute outputActions = transition.outputActions;
                result.addAll(_getParseTrees(outputActions));

                CommitActionsAttribute setActions = transition.setActions;
                result.addAll(_getParseTrees(setActions));                
            }
        }
        
        return result;
    }

    /**
     * @param actions
     * @return
     */
    private List<ASTPtRootNode> _getParseTrees(AbstractActionsAttribute actions) {
        List<ASTPtRootNode> parseTrees = actions.getParseTreeList();
        
        Iterator iterator = parseTrees.iterator();
        while (iterator.hasNext()) {
            putAttributes((ASTPtRootNode) iterator.next(), actions);
        }
        return parseTrees;
    }

}
