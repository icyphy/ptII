/* An FSMController is an FSM controller.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Red (liuxj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.domains.fsm.lib;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.actor.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Variable;
import ptolemy.domains.fsm.kernel.util.VariableList;
import ptolemy.domains.fsm.kernel.*;
import ptolemy.domains.ct.kernel.*;
import java.util.Enumeration;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// FSMController
/**
An FSMController is an FSM controller.

FIXME: Clean handling of the refinement of the state left.

@authors Xiaojun Liu
@version $Id$
*/
public class HSController extends FSMController {


    public HSController() {
        super();
    }


    public HSController(Workspace workspace) {
        super(workspace);
    }


    public HSController(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    public boolean prefire() {
        _currentState.resetLocalInputStatus();
        return super.prefire();
    }

    /** Change state according to the enabled transition determined
     *  from last fire.
     *  @return True, the execution can continue into the next iteration.
     *  @exception IllegalActionException If the refinement of the state
     *   transitioned into cannot be initialized.
     */
    public boolean postfire() throws IllegalActionException {

        _takenTransition = null;
        _setInputVars();
        // Evaluate the preemptive transitions.
        FSMTransition trans;
        Enumeration preTrans = _currentState.getPreemptiveTrans();
        while (preTrans.hasMoreElements()) {
            trans = (FSMTransition)preTrans.nextElement();
            if (trans.isEnabled()) {
                if (_takenTransition != null) {
                    // Nondeterminate transition!
                    //System.out.println("Nondeterminate transition!");
                } else {
                    _takenTransition = trans;
                }
            }
        }

        if (_takenTransition == null) {
            // The local input of an FSMState is the output of its refinement.
            //_currentState.resetLocalInputStatus();

            // Evaluate the nonpreemptive transitions.
            Enumeration nonPreTrans = _currentState.getNonPreemptiveTrans();
            while (nonPreTrans.hasMoreElements()) {
                trans = (FSMTransition)nonPreTrans.nextElement();
                if (trans.isEnabled()) {
                    if (_takenTransition != null) {
                        // Nondeterminate transition!
                        //System.out.println("Nondeterminate transition!");
                    } else {
                        _takenTransition = trans;
                    }
                }
            }
        }

        if (_takenTransition != null) {
            _outputTriggerActions(_takenTransition.getTriggerActions());
            _updateLocalVariables(_takenTransition.getLocalVariableUpdates());
            // _takenTransition.executeTransitionActions();
            // do not change state, that's done in postfire()
        }

        if (_takenTransition == null) {
            // No transition is enabled when last fire. FSMController does not
            // change state. Note this is different from when a transition
            // back to the current state is taken.
            return true;
        }

        // What to do to the refinement of the state left?

        _currentState = _takenTransition.destinationState();

        // execute the transition actions
        _takenTransition.executeTransitionActions();
	CTCompositeActor actor = (CTCompositeActor)currentRefinement();
	if (actor != null) {
	    actor.postfire();
        }

        CTDirector emb = (CTDirector)actor.getDirector();
        CTCompositeActor hs = (CTCompositeActor)actor.getContainer();
        CTDirector out = (CTDirector)hs.getExecutiveDirector();

        emb.fireAt(null, out.getCurrentTime());
        out.fireAt(null, out.getCurrentTime());

        if (_takenTransition.isInitEntry() || _currentState.isInitEntry()) {
            // Initialize the refinement.
            // Actor actor = currentRefinement();
            if (actor == null) {
                return true;
            }
            // If the refinement is an FSMController or an FSM system, then the trigger
            // actions of the taken transition should be input to the actor to enable
            // initial transitions.
            // ADD THIS!
            //            if (actor instanceof FSMController) {
            //                // Do what's needed.
            //            } else {
            //                // Do what's needed.
            //            }
            // FIXME!
            actor.initialize();
        }
        return true;
    }

}
