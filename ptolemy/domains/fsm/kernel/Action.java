/* Abstract base class of transition action.

 Copyright (c) 1999-2000 The Regents of the University of California.
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

import ptolemy.kernel.util.Attribute;

import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// Action
/**
An Action is contained by a Transition in an FSMActor.
<p>
When the FSMActor is fired, a transition among the outgoing transitions
of the current state is chosen if it is enabled. The choice actions
contained by the chosen transition are executed. An action is a choice
action if it implements the ChoiceAction marker interface. A choice
action may be executed more than once during an iteration in domains
with fixed-point semantics.
<p>
When the FSMActor is postfired, the chosen transition of the latest firing
of the actor is commited. The commit actions contained by the transition
are executed and the current state of the actor is set to the destination
state of the transition. An action is a commit action if it implements the
CommitAction marker interface.

@author Xiaojun Liu
@version $Id$
@see ChoiceAction
@see CommitAction
@see Transition
@see FSMActor
*/
public abstract class Action extends Attribute {

    /** Construct an action with the given name contained by the specified
     *  transition. The transition argument must not be null, or a
     *  NullPointerException will be thrown.  This action will use the
     *  workspace of the transition for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is added to the directory of the workspace
     *  if the container is null.
     *  Increment the version of the workspace.
     *  @param transition The transition.
     *  @param name The name of this action.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public Action(Transition transition, String name)
            throws IllegalActionException, NameDuplicationException {
        super(transition, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute the action.
     *  @exception IllegalActionException When the action cannot be
     *   successfully completed.
     */
    abstract public void execute() throws IllegalActionException;

    /** Override the base class to ensure that the proposed container
     *  is an instance of Transition or null. If it is, call the
     *  base class setContainer() method. A null argument will remove
     *  the action from its container.
     *
     *  @param entity The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this action and container are not in the same workspace, or
     *   if the argument is not a Transition or null.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the name of this action.
     */
    public void setContainer(NamedObj container)
            throws IllegalActionException, NameDuplicationException {
        if (!(container instanceof Transition) &&
                (container != null)) {
            throw new IllegalActionException(container, this,
                    "Action can only be contained by instances of " +
                    "Transition.");
        }
        super.setContainer(container);
    }

}
