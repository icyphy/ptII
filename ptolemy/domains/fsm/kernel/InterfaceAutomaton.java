/* An Interface Automaton.

 Copyright (c) 1999-2001 The Regents of the University of California.
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
@ProposedRating Yellow (liuxj@eecs.berkeley.edu)
@AcceptedRating Yellow (kienhuis@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.kernel;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// InterfaceAutomaton
/**
This class models an Interface Automaton. Interface automata is an automata
model defined by Luca de Alfaro in the paper "Interface Automata". 
An InterfaceAutomaton contains a set of states and
InterfaceAutomatonTransitions. There are three kinds transitions:
input transition, output transition, and internal transitions.
<p>
(FIXME: Are interface automata that are fired required to be deterministic?
or just randomly choose a transition.)

When an InterfaceAutomaton is fired, the outgoing transitions of the current
state are examined. An IllegalActionException is thrown if there is more than
one enabled transition. If there is exactly one enabled transition then it is
taken.
<p>
An InterfaceAutomaton enters its initial state during initialization. The
name of the initial state is specified by the <i>initialStateName</i> string
attribute.
<p>
An InterfaceAutomaton contains a set of variables for the input ports that
can be referenced in the labels of input transitions.
<p>

@author Yuhong Xiong, Xiaojun Liu and Edward A. Lee
@version $Id$
@see State
@see InterfaceAutomatonTransition
*/
public class InterfaceAutomaton extends FSMActor {

    /** Construct an InterfaceAutomaton in the default workspace with an
     *  empty string as its name. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public InterfaceAutomaton() {
        super();
    }

    /** Construct an InterfaceAutomaton in the specified workspace with an
     *  empty string as its name. The name can be changed later with
     *  setName().
     *  If the workspace argument is null, then use the default workspace.
     *  Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public InterfaceAutomaton(Workspace workspace) {
	super(workspace);
    }

    /** Create an InterfaceAutomaton in the specified container with the
     *  specified name. The name must be unique within the container or an
     *  exception is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this automaton within the container.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public InterfaceAutomaton(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new InterfaceAutomaton that is the composition of the
     *  specified InterfaceAutomaton and this one.
     *  @param automaton An InterfaceAutomaton to compose with this one.
     *  @return An InterfaceAutomaton that is the composition.
     */
    public InterfaceAutomaton compose(InterfaceAutomaton automaton) {
        // FIXME: implement this.
        return null;
    }

    // FIXME: do we allow non-determinism? how to handle it?
    /** Set the values of input variables. Choose the enabled transition
     *  among the outgoing transitions of the current state. Throw an
     *  exception if there is more than one transition enabled.
     *  @exception IllegalActionException If there is more than one
     *   transition enabled.
     */
    public void fire() throws IllegalActionException {
        super.fire();
    }

    /** Create a new instance of InterfaceAutomatonTransition with the
     *  specified name in this actor, and return it.
     *  This method is write-synchronized on the workspace.
     *  @param name The name of the new transition.
     *  @return An InterfaceAutomatonTransition with the given name.
     *  @exception IllegalActionException If the name argument is null.
     *  @exception NameDuplicationException If name collides with that
     *   of a transition already in this actor.
     */
    public ComponentRelation newRelation(String name)
            throws IllegalActionException, NameDuplicationException {
        try {
            workspace().getWriteAccess();
            InterfaceAutomatonTransition transition =
	            new InterfaceAutomatonTransition(this, name);
            return transition;
        } finally {
            workspace().doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add an InterfaceAutomatonTransition to this InterfaceAutomaton.
     *  This method should not be used directly.  Call the setContainer()
     *  method of the transition instead. This method does not set the
     *  container of the transition to refer to this container. This method
     *  is <i>not</i> synchronized on the workspace, so the caller should be.
     *
     *  @param relation The InterfaceAutomatonTransition to contain.
     *  @exception IllegalActionException If the transition has no name, or
     *   is not an instance of Transition.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the contained transitions list.
     */
    protected void _addRelation(ComponentRelation relation)
            throws IllegalActionException, NameDuplicationException {
        if (!(relation instanceof InterfaceAutomatonTransition)) {
            throw new IllegalActionException(this, relation,
                    "InterfaceAutomaton can only contain instances of "
		    + "InterfaceAutomatonTransition.");
        }
        super._addRelation(relation);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

}
