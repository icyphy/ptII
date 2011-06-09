/* A ModalDirector governs the execution of a modal model.

 Copyright (c) 2006-2010 The Regents of the University of California.
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ModalDirector

/**
 An ModalDirector governs the execution of a modal model. A modal model is
 a TypedCompositeActor with a ModalDirector as local director. The mode
 control logic is captured by a mode controller, an instance of FSMActor
 contained by the composite actor. Each state of the mode controller
 represents a mode of operation and can be refined by an opaque CompositeActor
 contained by the same composite actor.
 <p>
 This class differs from its base class in that it strictly follows the
 actor semantics. It does not invoke the postfire() method of any actors
 under its control until its own postfire() method is called. Thus,
 if those actors also follow the actor semantics, then no persistent
 changes are made in the prefire() or fire() methods.
 <p>
 When a modal model is fired, this director first transfers the input tokens
 from the outside domain to the mode controller and the refinement of its
 current state. The preemptive transitions from the current state of the mode
 controller are examined. If there is more than one transition enabled, and
 any of the enabled transitions is not marked nondeterministic, an
 exception is thrown. If there is exactly one preemptive transition enabled
 then it is chosen. The choice actions (outputActions) contained by the transition are
 executed. Any output token produced by the mode controller is transferred to
 both the output ports of the modal model and the input ports of the mode
 controller. Then the refinements associated with the enabled transition are
 fired. Any output token produced by the refinements is transferred to
 both the output ports of the modal model and the input ports of the mode
 controller. The refinements of the current state will not be fired.
 <p>
 If no preemptive transition is enabled, the refinements of the current state
 are fired. Any output token produced by the refinements is transferred to
 both the output ports of the modal model and the input ports of the mode
 controller. After this, the non-preemptive transitions from the current
 state of the mode controller are examined. If there is more than one
 transition enabled, and any of the enabled transitions is not marked
 nondeterministic, an exception is thrown. If there is exactly one
 non-preemptive transition enabled then it is chosen and the choice actions
 contained by the transition are executed. Any output token produced by the
 mode controller is transferred to the output ports of the modal model and
 the input ports of the mode controller. Then, the refinements of the
 enabled transition are executed. Any output token produced by the refinements
 is transferred to both the output ports of the modal model and the input
 ports of the mode controller.
 <p>
 At the end of one firing, the modal model transfers its outputs to the outside
 model. The mode controller does not change state during successive firings
 in one iteration of the top level in order to support upper level domains
 that iterate to a fixed point.
 <p>
 When the modal model is postfired, the chosen transition of the latest
 firing is committed. The commit actions contained by the transition are
 executed and the current state of the mode controller is set to the
 destination state of the transition.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Red (hyzheng)
 @see FSMActor
 */
public class ModalDirector extends FSMDirector {

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of this director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public ModalDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the attribute public members to refer
     *  to the attributes of the new actor.
     *  @param workspace The workspace for the new actor.
     *  @return A new FSMActor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ModalDirector newObject = (ModalDirector) super.clone(workspace);
        newObject._actorsFired = new HashSet();
        newObject._disabledActors = new HashSet();
        return newObject;
    }

    /** Fire the modal model.
     *  If there is a preemptive transition enabled, execute its choice
     *  actions (outputActions) and fire its refinement. Otherwise,
     *  fire the refinement of the current state. After this firing,
     *  if there is a transition enabled, execute its choice actions
     *  and fire the refinement of the transition.
     *  If any tokens are produced during this firing, they are sent to
     *  both the output ports of the model model but also the input ports of
     *  the mode controller.
     *  @exception IllegalActionException If there is more than one
     *   transition enabled, or there is no controller, or thrown by any
     *   choice action.
     */
    public void fire() throws IllegalActionException {
        FSMActor controller = getController();
        if (_debugging) {
            _debug("Firing " + getFullName(), " at time " + getModelTime());
        }
        controller.readInputs();
        State st = controller.currentState();

        // Chose a preemptive transition, if there is one,
        // and execute its choice actions.
        // The choice actions are the outputActions, not the setActions.
        Transition tr = controller.chooseTransition(st
                .preemptiveTransitionList());
        _enabledTransition = tr;

        // If a preemptive transition was found, prefire and fire
        // the refinements of the transition, and then return.
        if (tr != null) {
            if (_debugging) {
                _debug("Preemptive transition is enabled.");
            }
            Actor[] actors = tr.getRefinement();
            if (actors != null) {
                for (int i = 0; i < actors.length; ++i) {
                    if (_stopRequested || _disabledActors.contains(actors[i])) {
                        break;
                    }
                    if (_debugging) {
                        _debug("Prefire and fire the refinement of the preemptive transition: "
                                + actors[i].getFullName());
                    }
                    if (actors[i].prefire()) {
                        actors[i].fire();
                        _actorsFired.add(actors[i]);
                    }
                }
            }
            controller.readOutputsFromRefinement();
            return;
        }

        // There was no preemptive transition, so we proceed
        // to the refinement of the current state.
        Actor[] actors = st.getRefinement();
        if (actors != null) {
            for (int i = 0; i < actors.length; ++i) {
                if (_stopRequested || _disabledActors.contains(actors[i])) {
                    break;
                }
                if (_debugging) {
                    _debug(
                            "Prefire and fire the refinement of the current state: ",
                            actors[i].getFullName());
                }
                if (actors[i].prefire()) {
                    actors[i].fire();
                    _actorsFired.add(actors[i]);
                }
            }
        }
        // Mark that this state has been visited.
        st.setVisited(true);

        // Read the outputs from the refinement.
        controller.readOutputsFromRefinement();

        // See whether there is an enabled transition.
        tr = controller.chooseTransition(st.nonpreemptiveTransitionList());
        _enabledTransition = tr;
        if (tr != null) {
            if (_debugging) {
                _debug("A transition is enabled.");
            }
            actors = tr.getRefinement();
            if (actors != null) {
                for (int i = 0; i < actors.length; ++i) {
                    if (_stopRequested || _disabledActors.contains(actors[i])) {
                        break;
                    }

                    if (actors[i].prefire()) {
                        if (_debugging) {
                            _debug("Prefire and fire the refinement of the transition: "
                                    + actors[i].getFullName());
                        }
                        actors[i].fire();
                        _actorsFired.add(actors[i]);
                    }
                }
                controller.readOutputsFromRefinement();
            }
        }
    }

    /** Return true if the mode controller wishes to be scheduled for
     *  another iteration. Postfire all the actors that were
     *  fired since the last call to initialize() or postfire().
     *  Then execute the commit actions contained by the last
     *  chosen transition of the mode controller and set its current
     *  state to the destination state of the transition.
     *  @return True if the mode controller wishes to be scheduled for
     *  another iteration.
     *  @exception IllegalActionException If thrown by any commit action
     *  or there is no controller.
     */
    public boolean postfire() throws IllegalActionException {
        if (_debugging) {
            _debug("Postfire called at time: " + getModelTime());
        }
        boolean postfireResult = true;
        // Postfire all the actors that were fired in this iteration.
        Iterator actors = _actorsFired.iterator();
        while (actors.hasNext()) {
            Actor actor = ((Actor) actors.next());
            if (!actor.postfire()) {
                _disabledActors.add(actor);
                postfireResult = false;
            }
        }

        // We cannot clear the _actorsFired list here because
        // the suggestedStepSize() method of the derived class,
        // HybridModalDirector needs this list to query the suggested step size.
        // Defer the following method to the prefire() method of
        // the next iteration.
        // _actorsFired.clear();

        // Postfire the controller.
        FSMActor controller = getController();
        State previousState = controller.currentState();
        postfireResult = controller.postfire() && postfireResult;
        State newState = controller.currentState();

        if (previousState != newState) {
            // Update the _currentLocalReceiverMap to the new state.
            _currentLocalReceiverMap = (Map) _localReceiverMaps.get(controller
                    .currentState());
        }

        // If a transition was taken, then request a refiring at the current time
        // in case the destination state is a transient state.
        if (_enabledTransition != null) {
            CompositeActor container = (CompositeActor) getContainer();
            Director executiveDirector = container.getExecutiveDirector();
            if (executiveDirector != null) {
                if (_debugging) {
                    _debug("ModalDirector: Request refiring by "
                            + executiveDirector.getFullName() + " at "
                            + getModelTime());
                }
                executiveDirector.fireAtCurrentTime(container);
            }
        }

        return postfireResult && !_stopRequested && !_finishRequested;
    }

    /** Override the prefire() method of the super class to clear
     *  local variables.
     *  @return Whatever super.prefire() returns (true if the director
     *  is ready to fire.
     *  @exception IllegalActionException If throw by the parent class.
     */
    public boolean prefire() throws IllegalActionException {
        _actorsFired.clear();
        return super.prefire();
    }

    /** Initialize this director.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _actorsFired.clear();
        _disabledActors.clear();
    }

    /** Indicate that a schedule for the model may no longer be valid.
     *  This method simply notifies the executive director.
     */
    public void invalidateSchedule() {
        CompositeActor container = (CompositeActor) getContainer();
        Director executiveDirector = container.getExecutiveDirector();
        if (executiveDirector != null && executiveDirector != this) {
            executiveDirector.invalidateSchedule();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Actors that were fired in the current iteration. */
    protected Set _actorsFired = new HashSet();

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Actors that have returned false in postfire(). */
    private Set _disabledActors = new HashSet();
}
