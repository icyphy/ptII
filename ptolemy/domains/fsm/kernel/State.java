/* A state in an FSMActor.

Copyright (c) 1999-2005 The Regents of the University of California.
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
package ptolemy.domains.fsm.kernel;

import ptolemy.actor.TypedActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;


//////////////////////////////////////////////////////////////////////////
//// State

/**
   A State has two ports: one for linking incoming transitions, the other for
   outgoing transitions. When the FSMActor containing a state is the mode
   controller of a modal model, the state can be refined by one or more
   instances of TypedActor. The refinements must have the same container
   as the FSMActor. During execution of a modal model, only the mode
   controller and the refinements of the current state of the mode
   controller react to input to the modal model and produce
   output. The outgoing transitions from a state are either preemptive or
   non-preemptive. When a modal model is fired, if a preemptive transition
   from the current state of the mode controller is chosen, the refinements of
   the current state are not fired. Otherwise the refinements are fired before
   choosing a non-preemptive transition.

   @author Xiaojun Liu
   @version $Id$
   @since Ptolemy II 0.4
   @Pt.ProposedRating Yellow (liuxj)
   @Pt.AcceptedRating Yellow (kienhuis)
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
    public State(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        incomingPort = new ComponentPort(this, "incomingPort");
        outgoingPort = new ComponentPort(this, "outgoingPort");
        refinementName = new StringAttribute(this, "refinementName");

        _attachText("_iconDescription",
                "<svg>\n"
                + "<circle cx=\"0\" cy=\"0\" r=\"20\" style=\"fill:white\"/>\n"
                + "</svg>\n");

        // Specify that the name should be centered in graphical displays.
        SingletonParameter center = new SingletonParameter(this, "_centerName");
        center.setExpression("true");
        center.setVisibility(Settable.EXPERT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The port linking incoming transitions.
     */
    public ComponentPort incomingPort = null;

    /** The port linking outgoing transitions.
     */
    public ComponentPort outgoingPort = null;

    /** Attribute specifying one or more names of refinements. The
     *  refinements must be instances of TypedActor and have the same
     *  container as the FSMActor containing this state, otherwise
     *  an exception will be thrown when getRefinement() is called.
     *  Usually, the refinement is a single name. However, if a
     *  comma-separated list of names is provided, then all the specified
     *  refinements will be executed.
     *  This attribute has a null expression or a null string as
     *  expression when the state is not refined.
     */
    public StringAttribute refinementName = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute is
     *  the <i>refinementName</i> attribute, record the change but do
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
     *  base class and then sets the attribute and port public members
     *  to refer to the attributes and ports of the new state.
     *  @param workspace The workspace for the new state.
     *  @return A new state.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        State newObject = (State) super.clone(workspace);
        newObject.incomingPort = (ComponentPort) newObject.getPort(
                "incomingPort");
        newObject.outgoingPort = (ComponentPort) newObject.getPort(
                "outgoingPort");
        newObject.refinementName = (StringAttribute) newObject.getAttribute(
                "refinementName");
        newObject._refinementVersion = -1;
        newObject._transitionListVersion = -1;
        newObject._nonpreemptiveTransitionList = new LinkedList();
        newObject._preemptiveTransitionList = new LinkedList();
        return newObject;
    }

    /** Return the refinements of this state. The names of the refinements
     *  are specified by the <i>refinementName</i> attribute. The refinements
     *  must be instances of TypedActor and have the same container as
     *  the FSMActor containing this state, otherwise an exception is thrown.
     *  This method can also return null if there is no refinement.
     *  This method is read-synchronized on the workspace.
     *  @return The refinements of this state, or null if there are none.
     *  @exception IllegalActionException If the specified refinement
     *   cannot be found, or if a comma-separated list is malformed.
     */
    public TypedActor[] getRefinement() throws IllegalActionException {
        if (_refinementVersion == workspace().getVersion()) {
            return _refinement;
        }

        try {
            workspace().getReadAccess();

            String names = refinementName.getExpression();

            if ((names == null) || names.trim().equals("")) {
                _refinementVersion = workspace().getVersion();
                _refinement = null;
                return null;
            }

            StringTokenizer tokenizer = new StringTokenizer(names, ",");
            int size = tokenizer.countTokens();

            if (size <= 0) {
                _refinementVersion = workspace().getVersion();
                _refinement = null;
                return null;
            }

            _refinement = new TypedActor[size];

            Nameable container = getContainer();
            TypedCompositeActor containerContainer = (TypedCompositeActor) container
                .getContainer();
            int index = 0;

            while (tokenizer.hasMoreTokens()) {
                String name = tokenizer.nextToken().trim();

                if (name.equals("")) {
                    throw new IllegalActionException(this,
                            "Malformed list of refinements: " + names);
                }

                TypedActor element = (TypedActor) containerContainer.getEntity(name);

                if (element == null) {
                    throw new IllegalActionException(this,
                            "Cannot find " + "refinement with name \"" + name
                            + "\" in " + containerContainer.getFullName());
                }

                _refinement[index++] = element;
            }

            _refinementVersion = workspace().getVersion();
            return _refinement;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return true if this state has been visited. Otherwise, return false.
     *  @return Returns true if this state has been visited.
     */
    public boolean isVisited() {
        return _visited;
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

    /** Set the flag that indicates whether this state has been visited with
     *  the given boolean value.
     *  @param visited The boolean flag indicating whether this state has been
     *  visited.
     */
    public void setVisited(boolean visited) {
        _visited = visited;
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

            Iterator transitions = outgoingPort.linkedRelationList().iterator();

            while (transitions.hasNext()) {
                Transition transition = (Transition) transitions.next();

                if (transition.isPreemptive()) {
                    _preemptiveTransitionList.add(transition);
                } else {
                    _nonpreemptiveTransitionList.add(transition);
                }
            }

            _transitionListVersion = workspace().getVersion();
        } finally {
            workspace().doneReading();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Flag indicating whether this state has been visited.
    private boolean _visited = false;

    // Cached list of non-preemptive outgoing transitions from this state.
    private List _nonpreemptiveTransitionList = new LinkedList();

    // Cached list of preemptive outgoing transitions from this state.
    private List _preemptiveTransitionList = new LinkedList();

    // Cached reference to the refinement of this state.
    private TypedActor[] _refinement = null;

    // Version of the cached reference to the refinement.
    private long _refinementVersion = -1;

    // Version of cached transition lists.
    private long _transitionListVersion = -1;
}
