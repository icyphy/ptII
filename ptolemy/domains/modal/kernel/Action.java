/* Abstract base class of transition action.

 Copyright (c) 1999-2014 The Regents of the University of California.
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
package ptolemy.domains.modal.kernel;

import java.util.List;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// Action

/**
 An Action is contained by a Transition in an FSMActor or an Event in an
 ERGController.
 <p>
 When the FSMActor is fired, an enabled transition among the outgoing
 transitions of the current state is chosen. The choice actions
 contained by the chosen transition are executed. An action is a choice
 action if it implements the ChoiceAction marker interface. A choice
 action may be executed more than once during an iteration in domains
 with fixed-point semantics.
 <p>
 When the FSMActor is postfired, the chosen transition of the latest firing
 of the actor is committed. The commit actions contained by the transition
 are executed and the current state of the actor is set to the destination
 state of the transition. An action is a commit action if it implements the
 CommitAction marker interface.

 @author Xiaojun Liu
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (liuxj)
 @Pt.AcceptedRating Yellow (liuxj)
 @see ChoiceAction
 @see CommitAction
 @see Transition
 @see FSMActor
 @see ptolemy.data.expr.Variable
 */
public abstract class Action extends StringAttribute {
    /** Construct an action in the specified workspace with an empty
     *  string as a name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     */
    public Action(Workspace workspace) {
        super(workspace);
    }

    /** Construct an action with the given name contained by the
     *  specified container. The container argument must not be
     *  null, or a NullPointerException will be thrown. This action
     *  will use the workspace of the container for synchronization
     *  and version counts. If the name argument is null, then the
     *  name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this action.
     *  @exception IllegalActionException If the action is not of an
     *   acceptable class for the container, or if the name contains
     *   a period.
     *  @exception NameDuplicationException If the container already
     *   has an attribute with the name.
     */
    public Action(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute the action.
     *  @exception IllegalActionException If the action cannot be
     *   successfully completed.
     */
    abstract public void execute() throws IllegalActionException;

    /** Return the list of destinations of assignments in this action.
     *  @return A list of IOPort for output actions, and a list of parameters
     *   for set actions.
     *  @exception IllegalActionException If the destination list cannot be
     *   constructed.
     */
    abstract public List getDestinations() throws IllegalActionException;

    /** Set the container of this action. The proposed container must
     *  be an instance of Transition or Event or null, otherwise an
     *  IllegalActionException will be thrown. A null argument will
     *  remove the action from its container.
     *
     *  @param container The proposed container.
     *  @exception IllegalActionException If setting the container
     *   would result in a recursive containment structure, or if this
     *   action and container are not in the same workspace, or if the
     *   argument is not an instance of Transition or null.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this action.
     */
    @Override
    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        // An Action is used in a Transition in the FSM domain, or an Event in
        // the Ptera domain.

        // FSM should not depend on Ptera, so this check is removed.
        // In fact, this check is not necessary, because all instantiable
        // classes in FSM declares container to have type Transition.

        /*if (!(container instanceof Transition) && !(container instanceof Event)
                && (container != null)) {
            throw new IllegalActionException(container, this, "Action can only "
                    + "be contained by instances of Transition or Event.");
        }*/

        super.setContainer(container);
    }
}
