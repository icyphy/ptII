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
@ProposedRating Yellow (liuxj@eecs.berkeley.edu)
@AcceptedRating Yellow (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.kernel;

import ptolemy.kernel.ComponentEntity;

import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.actor.TypedActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;

import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// State
/**
A State has two ports: one for linking incoming transitions, the other for
outgoing transitions. When the FSMActor containing a state is the mode
controller of a modal model, the state can be refined by a TypedActor. The
refinement must have the same container as the FSMActor. During execution
of a modal model, only the mode controller and the refinement of the current
state of the mode controller react to input to the modal model and produce
output. The outgoing transitions from a state are either preemptive or
non-preemptive. When a modal model is fired, if a preemptive transition
from the current state of the mode controller is chosen, the refinement of
the current state is not fired. Otherwise the refinement is fired before
choosing a non-preemptive transition.

@author Xiaojun Liu
@version $Id$
@see Transition
@see FSMActor
@see FSMDirector
*/
public class State extends ComponentEntity {

    /** Construct a state with the given name contained by the specified
     *  composite entity. The container argument must not be null, or a
     *  NullPointerException will be thrown. This state will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string.
     *  Increment the version of the workspace.
     *  This constructor write-synchronizes on the workspace.
     *  @param container The container.
     *  @param name The name of the state.
     *  @exception IllegalActionException If the state cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   that of an entity already in the container.
     */
    public State(FSMActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        incomingPort = new ComponentPort(this, "incomingPort");
        outgoingPort = new ComponentPort(this, "outgoingPort");
        refinementName = new Parameter(this, "refinementName");
        refinementName.setTypeEquals(BaseType.STRING);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The port linking incoming transitions.
     */
    public ComponentPort incomingPort = null;

    /** The port linking outgoing transitions.
     */
    public ComponentPort outgoingPort = null;

    /** Parameter specifying the name of the refinement. The refinement
     *  must be a TypedActor and have the same container as the FSMActor
     *  containing this state, otherwise an exception will be thrown
     *  when getRefinement() is called. This parameter contains a null
     *  token when the state is not refined.
     */
    public Parameter refinementName = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute is
     *  the <i>refinementName</i> parameter, record the change but do
     *  not check whether there is a TypedActor with the specified name
     *  and having the same container as the FSMActor containing this
     *  state.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If thrown by the superclass
     *   attributeChanged() method.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);
        if (attribute == refinementName) {
            _refinementVersion = -1;
        }
    }

    /** Clone the state into the specified workspace. This calls the
     *  base class and then sets the parameter and port public members
     *  to refer to the parameters and ports of the new state.
     *  @param ws The workspace for the new state.
     *  @return A new state.
     *  @throws CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
            throws CloneNotSupportedException {
        State newobj = (State)super.clone(ws);
        newobj.incomingPort = (ComponentPort)newobj.getPort("incomingPort");
        newobj.outgoingPort = (ComponentPort)newobj.getPort("outgoingPort");
        newobj.refinementName =
                (Parameter)newobj.getAttribute("refinementName");
        newobj._refinementVersion = -1;
        newobj._transitionListVersion = -1;
        return newobj;
    }

    /** Return the refinement of this state. The name of the refinement
     *  is specified by the <i>refinementName</i> parameter. The refinement
     *  must be a TypedActor and have the same container as the FSMActor
     *  containing this state, otherwise an exception is thrown.
     *  This method is read-synchronized on the workspace.
     *  @return The refinement of this state.
     *  @exception IllegalActionException If the specified refinement
     *   cannot be found.
     */
    public TypedActor getRefinement() throws IllegalActionException {
        if (_refinementVersion == workspace().getVersion()) {
            return _refinement;
        }
        try {
            workspace().getReadAccess();
            StringToken tok = (StringToken)refinementName.getToken();
            if (tok != null) {
                String refName = tok.stringValue();
                FSMActor cont = (FSMActor)getContainer();
                TypedCompositeActor contContainer =
                        (TypedCompositeActor)cont.getContainer();
                _refinement = (TypedActor)contContainer.getEntity(refName);
                if (_refinement == null) {
                    throw new IllegalActionException(this, "Cannot find "
                            + "refinement with name \"" + refName
                            + "\" in " + contContainer.getFullName());
                }
            } else {
                _refinement = null;
            }
            _refinementVersion = workspace().getVersion();
            return _refinement;
        } finally {
            workspace().doneReading();
        }            
    }

    /** Return the list of non-preemptive outgoing transitions from
     *  this state.
     *  @return The list of non-preemptive outgoing transitions from
     *   this state.
     */
    public List nonpreemptiveTransitionList() {
        if (_transitionListVersion != workspace().getVersion()) {
            _updateTransitionLists();
        }
        return _nonpreemptiveTransitionList;
    }

    /** Return the list of preemptive outgoing transitions from
     *  this state.
     *  @return The list of preemptive outgoing transitions from
     *   this state.
     */
    public List preemptiveTransitionList() {
        if (_transitionListVersion != workspace().getVersion()) {
            _updateTransitionLists();
        }
        return _preemptiveTransitionList;
    }

    /** Override the base class to ensure that the proposed container
     *  is an instance of FSMActor or null. If it is, call the base
     *  class setContainer() method. A null argument will remove this
     *  state from its container.
     *  @param container The proposed container.
     *  @exception IllegalActionException If this state and the container
     *   are not in the same workspace, or if the argument is not an
     *   instance of FSMActor or null.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the name of this state.
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

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Update the cached transition lists.
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

    // Cached reference to the refinement of this state.
    private TypedActor _refinement = null;

    // Version of the cached reference to the refinement.
    private long _refinementVersion = -1;

    // Version of cached transition lists.
    private long _transitionListVersion = -1;

}
