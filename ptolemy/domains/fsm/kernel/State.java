/* A state in an FSMActor.

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

import ptolemy.kernel.ComponentEntity;

import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.actor.TypedActor;

import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// State
/**
A State has two ports: one for linking incoming transitions, the other for
outgoing transitions. In a modal model, a State can be refined by a
TypedActor.

@author Xiaojun Liu
@version $Id$
@see Transition
@see FSMActor
*/
public class State extends ComponentEntity {

    /** Construct a state with the given name contained by the specified
     *  composite entity. The container argument must not be null, or a
     *  NullPointerException will be thrown. This state will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  This constructor write-synchronizes on the workspace.
     *  @param container The container entity.
     *  @param name The name of the entity.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an state already in the container.
     */
    public State(FSMActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        incomingPort = new ComponentPort(this, "IncomingPort");
        outgoingPort = new ComponentPort(this, "OutgoingPort");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the refinement of this state.
     *  @return A TypedActor refining this state.
     */
    public TypedActor getRefinement() {
        return _refinement;
    }

    /** Return the list of non-preemptive outgoing transitions from
     *  this state.
     *  @return A list of non-preemptive transitions.
     */
    public List nonpreemptiveTransitionList() {
        if (_transitionListVersion != workspace().getVersion()) {
            _updateTransitionLists();
        }
        return _nonpreemptiveTransitionList;
    }

    /** Return the list of preemptive outgoing transitions from
     *  this state.
     *  @return A list of preemptive transitions.
     */
    public List preemptiveTransitionList() {
        if (_transitionListVersion != workspace().getVersion()) {
            _updateTransitionLists();
        }
        return _preemptiveTransitionList;
    }

    /** Override the base class to ensure that the proposed container
     *  is an instance of FSMActor or null. If it is, call the
     *  base class setContainer() method. A null argument will remove
     *  the state from its container.
     *
     *  @param entity The proposed container.
     *  @exception IllegalActionException If the state would result
     *   in a recursive containment structure, or if
     *   this state and container are not in the same workspace, or
     *   if the argument is not an instance of FSMActor or null.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the name of this transition.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (!(container instanceof FSMActor) &&
                (container != null)) {
            throw new IllegalActionException(container, this,
                    "State can only be contained by instances of " +
                    "FSMActor.");
        }
        super.setContainer(container);
    }

    /** Set the refinement of this state. The refinement must implement
     *  the Nameable interface, otherwise an IllegalActionException is
     *  thrown. The refinement must have the same container as the
     *  FSMActor containing this state.
     *  @param refinement The refinement of this state.
     *  @exception IllegalActionException If the refinement does not
     *   implement the Nameable interface, or if the refinement does
     *   not have the same container as the FSMActor containing this
     *   state.
     */
    public void setRefinement(TypedActor refinement)
            throws IllegalActionException {
        if (refinement == null) {
            _refinement = null;
            return;
        }
        if (!(refinement instanceof Nameable)) {
            throw new IllegalActionException(this,
                    "The refinement of a state must implement the "
                    + "Nameable interface.");
        }
        if (getContainer() != ((Nameable)refinement).getContainer()) {
            throw new IllegalActionException(this,
                    "The refinement of a state must have the same container "
                    + "as the FSMActor containing the state.");
        }
        _refinement = refinement;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The port linking incoming transitions.
     */
    public ComponentPort incomingPort = null;

    /** The port linking outgoing transitions.
     */
    public ComponentPort outgoingPort = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Redo the cached transition lists.
    // This method is read-synchronized on the workspace.
    private void _updateTransitionLists() {
        try {
            workspace().getReadAccess();
            _nonpreemptiveTransitionList.clear();
            _preemptiveTransitionList.clear();
            Iterator trs = outgoingPort.linkedRelationList().iterator();
            while (trs.hasNext()) {
                Transition tr = (Transition)trs.next();
                if (tr.isPreemptive()) {
                    _preemptiveTransitionList.add(tr);
                } else {
                    _nonpreemptiveTransitionList.add(tr);
                }
            }
            _transitionListVersion = workspace().getVersion();
        } finally {
            workspace().doneReading();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Cached list of non-preemptive outgoing transitions from this state.
    private List _nonpreemptiveTransitionList = new LinkedList();

    // Cached list of preemptive outgoing transitions from this state.
    private List _preemptiveTransitionList = new LinkedList();

    // The refinement of this state.
    private TypedActor _refinement = null;

    // Version of cached transition lists.
    private long _transitionListVersion = -1;
}

