/* An action that resets a refinement.

 Copyright (c) 2000 The Regents of the University of California.
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
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.kernel;

import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.actor.Actor;

//////////////////////////////////////////////////////////////////////////
//// ResetRefinement
/**
A ResetRefinement action resets the refinement of the destination state
of the transition containing the action. This action is a commit action
contained by a transition in an FSMActor, which will be called the
associated FSMActor of this action.
<p>
There is no reset() method in the Actor interface. This action just calls
the initialize() method of the refinement.
<p>
Note: this class is still under development.

@author Xiaojun Liu
@version $Id$
@see Transition
@see FSMActor
*/
public class ResetRefinement extends Action implements CommitAction {

    /** Construct a ResetRefinement action with the given name contained
     *  by the specified transition. The transition argument must not be
     *  null, or a NullPointerException will be thrown. This action will
     *  use the workspace of the transition for synchronization and
     *  version counts. If the name argument is null, then the name is
     *  set to the empty string. A variable for expression evaluation is
     *  created in the transition. The name of the variable is obtained
     *  by prepending an underscore to the name of this action.
     *  Increment the version of the workspace.
     *  @param transition The transition.
     *  @param name The name of this action.
     *  @exception IllegalActionException If the action is not of an
     *   acceptable class for the transition, or if the name contains
     *   a period.
     *  @exception NameDuplicationException If the transition already
     *   has an attribute with the name or that obtained by prepending
     *   an underscore to the name.
     */
    public ResetRefinement(Transition transition, String name)
            throws IllegalActionException, NameDuplicationException {
        super(transition, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Reset the refinement of the destination state of the transition
     *  containing this action by calling its initialize() method. An
     *  exception is thrown if the destination state has no refinement.
     *  @exception IllegalActionException If the destination state has
     *   no refinement, or if thrown by the initialize() method of the
     *   refinement.
     */
    public void execute() throws IllegalActionException {
        State dest = ((Transition)getContainer()).destinationState();
        Actor ref = dest.getRefinement();
        if (ref == null) {
            throw new IllegalActionException(this, dest,
                    "The destination state has no refinement.");
        }
        ref.initialize();
    }

}
