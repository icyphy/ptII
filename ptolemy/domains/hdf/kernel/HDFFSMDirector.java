/* A HDFFSMDirector governs the execution of the finite state
   machine in heterochronous dataflow model.

 Copyright (c) 1999-2003 The Regents of the University of California.
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

@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.domains.hdf.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Scheduler;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.fsm.kernel.Action;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.FSMDirector;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.domains.sdf.kernel.SDFScheduler;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.lang.String;

//////////////////////////////////////////////////////////////////////////
//// HDFFSMDirector
/**
An HDFFSMDirector governs the execution of a finite state machine
(FSM) in a heterochronous dataflow (HDF) or synchronous dataflow
(SDF) model according to the *charts [1] semantics. *charts is a
family of models of computation that specifies an operational
semantics for composing hierarchical FSMs with various concurrency
models.
<p>
The subset of *charts that this class supports is HDF inside FSM
inside HDF, and SDF inside FSM inside SDF.
This class must be used as the director of an FSM when the FSM
refines an HDF composite actor. This class may also be used as the
director of an FSM for an FSM when the FSM refines an SDF actor.
<p>
<b>Usage</b>
<p>
Hierarchical compositions of HDF with FSMs can be quite complex to
represent textually, even for simple models. It is therefore
recommended that a graphical model editor like Vergil be used to
construct the model.
<p>
The executive director must be HDF, SDF, or HDFFSMDirector.
Otherwise an exception will occur. An HDF or SDF composite actor that
refines to an FSM will use this class as the FSM's local director.
All states in the FSM must refine to either another FSM, an HDF model
or a SDF model. That is, all refinement actors must be opaque and must
externally have HDF or SDF semantics. There is no constraint on
the number of levels in the hierarchy.
<p>
To use this director, create a ModalModel and specify this director
as its director.  Then look inside to populate the controller
with states. Create one TypedComposite actor as a refinement
for each state in the FSM.
<p>
You must explicitly specify the initial state of the controller FSM.
The guard expression on each transition is evaluated only after a
"Type B firing" [1], which is the last firing of the HDF actor
in the current iteration of the current HDF schedule. A state
transition will occur if the guard expression evaluates to true
after a "Type B firing."
<p>
<b>References</b>
<p>
<OL>
<LI>
A. Girault, B. Lee, and E. A. Lee, ``<A HREF="http://ptolemy.eecs.berkeley.edu/papers/98/starcharts">Hierarchical
Finite State Machines with Multiple Concurrency Models</A>, '' April 13,
1998.</LI>
</ol>

@author Brian K. Vogel and Rachel Zhou
@version $Id$
@see HDFDirector
*/
public class HDFFSMDirector extends FSMDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public HDFFSMDirector() {
        super();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     */
    public HDFFSMDirector(Workspace workspace) {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public HDFFSMDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the values of input variables and the input array in the
     *  mode controller. If the refinement of the current state of
     *  the mode controller is ready to fire, then fire the current
     *  refinement.
     *
     *  @exception IllegalActionException If there is no controller.
     */
    public void fire() throws IllegalActionException {

        if (_debug_info) System.out.println(getName() +
                                           " fire() invoked.");
        _setInputVariables();
        FSMActor ctrl = getController();
        State st = ctrl.currentState();
        // FIXME
        //Actor ref = ctrl.currentState().getRefinement();
        Actor[] ref = ctrl.currentState().getRefinement();
        _fireRefinement = false;
        if (ref != null) {
          if (_debug_info) System.out.println(getName() +
                             " fire(): refinement is " +
                             // FIXME
                             //((CompositeActor)ref).getName());
                             ((CompositeActor)ref[0]).getName());
          // Note that we do not call refinement.prefire() in
          // this.prefire(). This is because transferInputs() is
          // called by the composite actor in fire(), and
          // refinement.prefire() should not be invoked until
          // the tokens have been transferred to the input ports of
          // the current refinement.
          // FIXME
          //_fireRefinement = ref.prefire();
          _fireRefinement = ref[0].prefire();
        }
        if (_debug_info) System.out.println(getName() +
               " fire(): refinement is ready to fire = " +
                                           _fireRefinement);
        if (_fireRefinement) {
            if (_debug_info) System.out.println(getName() +
                                        " firing refinement");
            // Fire the refinement.
            // FIXME
            //ref.fire();
            ref[0].fire();
            _setInputsFromRefinement();
        }
        if (_firingsSoFar == _firingsPerScheduleIteration - 1) {
            // The HDFFSM Director has fired
            // "_firingsPerScheduleIteration" times.
            // Note _firingsSoFar starts at zero and is updated in postfire.
            _chooseTransition(st.nonpreemptiveTransitionList());
        }
        return;
    }

    /** Get the number of firings per iteration of this director.
     * @return The number of firings per iteration of this director.
     */
    public int getFiringsPerScheduleIteration() {
        return _firingsPerScheduleIteration;
    }

    /** Get the number of firings so far of this director.
     * @return The number of firings so far.
     */
    public int getFiringsSoFar() {
        return _firingsSoFar;
    }

    /** Invoke the initialize() method of each deeply contained actor.
     *  Since type resolution has been completed, the initialize() method
     *  of a contained actor may produce output or schedule events.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the initialize() method of
     *   one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (!_resetCurrentHDFFSM) {
            _firingsSoFar = 0;
            FSMActor controller = getController();
            State initialState = controller.getInitialState();
            // Get the current refinement.
            TypedCompositeActor curRefinement =
                // FIXME
                //(TypedCompositeActor)initialState.getRefinement();
                (TypedCompositeActor)(initialState.getRefinement())[0];
            _setCurrentConnectionMap();
            // Update the map from an input port of the modal model
            // to the receivers of the current state.
            _currentLocalReceiverMap =
                (Map)_localReceiverMaps.get(initialState);
            if (curRefinement != null) {
                Director refinementDir = curRefinement.getDirector();
                if (_debug_info) {
                    System.out.println(getName() + " : initialize()" +
                        "initial refinement is "
                        + curRefinement.getFullName());
                    System.out.println(getName() + " : initialize(): " +
                        "initial director is " + refinementDir.getFullName());
                }
                if (refinementDir instanceof HDFFSMDirector) {
                    refinementDir.initialize();
                } else if (refinementDir instanceof HDFDirector) {
                    Scheduler refinmentSched = ((StaticSchedulingDirector)
                    refinementDir).getScheduler();
                    refinmentSched.setValid(false);
                    //refinmentSched.getSchedule();
                    //System.out.println("getSchedule in HDFFSM initialize");
                    ((HDFDirector)refinementDir).getSchedule();
                    if (_debug_info) System.out.println(getName() +
                        " : initialize(): refinement's director : " +
                            refinementDir.getFullName());

                    if (_debug_info) {
                        CompositeActor container =
                            (CompositeActor)getContainer();
                        System.out.println(getName() +
                            " : initialize(): Name of HDF composite actor: "
                                  + ((Nameable)container).getName());
                    }
                } else if (refinementDir instanceof SDFDirector) {
                    Scheduler refinmentSched = ((StaticSchedulingDirector)
                        refinementDir).getScheduler();
                    refinmentSched.setValid(false);
                    ((SDFScheduler)refinmentSched).getSchedule();
                } else {
                    // Invalid director.
                    throw new IllegalActionException(this,
                      "The current refinement has an invalid director. " +
                      "Allowed directors are SDF, HDF, or HDFFSMDirector.");
                }
                _updateInputTokenConsumptionRates(curRefinement);
                _updateOutputTokenProductionRates(curRefinement);
                // Tell the upper level scheduler that the current schedule
                // is no longer valid.
                if (_debug_info) System.out.println(getName() +
                        " : initialize(): invalidating " +
                                          "current schedule.");
               CompositeActor hdfActor = _getHighestFSM();
               Director director = hdfActor.getExecutiveDirector();
               ((StaticSchedulingDirector)director).invalidateSchedule();
            } else {
                throw new IllegalActionException(this,
                    "current refinement is null.");
            }
        }
    }

    /** Return a new receiver of a type compatible with this director.
     *  This returns an instance of SDFReceiver.
     *
     *  @return A new SDFReceiver.
     */
    public Receiver newReceiver() {
        return new SDFReceiver();
    }

    /** Return true if the mode controller wishes to be scheduled for
     *  another iteration. Postfire the refinement of the current state
     *  of the mode controller. If a type B firing has occurred and exactly
     *  one transition is enabled, then change state to the destination
     *  state of the enabled transition. Note that a type B firing is
     *  the last firing of an actor in an iteration of the HDF graph in
     *  which it is embedded.
     *  <p>
     *  If a state transition to a refinement with different port
     *  rates from the previous refinement occurs, then the port rates of
     *  the container of this director are updated to be consistent
     *  with the port rates of the new state's refinement. The HDF
     *  director will be notified of the change in port rates. If
     *  a change in port rates occurs and this FSM is governed by
     *  an SDF director, an exception will occur.
     *
     *  @return True if the mode controller wishes to be scheduled for
     *   another iteration.
     *  @exception IllegalActionException If a refinement throws it,
     *   if there is no controller, or if an inconsistency in port
     *   rates is detected between refinement actors.
     */
    public boolean postfire() throws IllegalActionException {
        FSMActor ctrl = getController();
        State curState = ctrl.currentState();
        // Get the current refinement actor.
        // FIXME
        //TypedActor currentRefinement =
        TypedActor[] currentRefinement =
            curState.getRefinement();
        if (currentRefinement == null) {
            throw new IllegalActionException(this,
             "Can't postfire because current refinement is null.");
        }

        // Postfire the current refinement.
        // FIXME
        //boolean postfireReturn = currentRefinement.postfire();
        boolean postfireReturn = currentRefinement[0].postfire();
        // Increment current firing count. This is the number of
        // times the current refining HDF actor has been fired
        // in the current iteration of the current static schedule
        // of the SDF/HDF graph containing this FSM.
        _firingsSoFar++;

        if (_debug_info) {
            System.out.println(getFullName() + " :  postfire(): " +
               "_firingsSoFar = " + _firingsSoFar +
               ", _firingsPerScheduleIteration = " +
                               _firingsPerScheduleIteration);
        }

        // Check if the fire() has been called the number of
        // times specified in the static schedule.
        if (_firingsSoFar == _firingsPerScheduleIteration) {
            // The current refinement has been fired the number
            // of times specified by the current static schedule.
            // A state transition can now occur.
            // Set firing count back to zero for next iteration.
            _firingsSoFar = 0;
            Transition lastChosenTr = _getLastChosenTransition();
            if (lastChosenTr  == null) {
                // There is no enabled transition, so remain in the
                // current state.
                if (_debug_info) System.out.println(getFullName() +
                  " :  postfire(): Making a state transition back " +
                                             "to the current state. "
                                             + curState.getFullName());
                TypedCompositeActor actor =
                // FIXME
                //(TypedCompositeActor)curState.getRefinement();
                    (TypedCompositeActor)(curState.getRefinement())[0];
                // Extract the token consumption/production rates from the
                // ports of the new refinement and update the
                // rates of the ports of the FSM actor.
                Director refinementDir = actor.getDirector();
                _updateInputTokenConsumptionRates(actor);
                _updateOutputTokenProductionRates(actor);
                CompositeActor hdfActor = _getHighestFSM();
                Director director = hdfActor.getExecutiveDirector();
                if (director instanceof HDFDirector) {
                    ((HDFDirector)director).invalidateSchedule();
                }

            } else {
                // The HDF/SDF graph in which this FSM is embedded has
                // just finished one iteration, so make a state
                // transition.
                if (_debug_info) System.out.println(getName() +
                     " :  postfire(): Making a state transition. ");
                // Update the current refinement to point to the destination
                // state of the (enabled) transition.
                // FIXME:
                State newState = lastChosenTr.destinationState();

                _setCurrentState(newState);
                if (_debug_info) System.out.println(getName() +
                   " : postfire(): making state transition to: " +
                                           newState.getFullName());
                Iterator actions = lastChosenTr.commitActionList().iterator();
                while (actions.hasNext() && !_stopRequested) {
                    //System.out.println("commit action in HDFFSM");
                    Action action = (Action)actions.next();
                    action.execute();
                }
                BooleanToken resetToken =
                            (BooleanToken)lastChosenTr.reset.getToken();
                if (resetToken.booleanValue()) {
                    setCurrentHDFFSMReset(true);
                    initialize();
                    //FSMActor controller = getController();
                    //State initialState = controller.getInitialState();
                }
                setCurrentHDFFSMReset(false);
                curState = newState;
                // Since a state change has occurred, recompute the
                // Mapping from input ports of the modal model to
                // their corresponding receivers of the new current
                // state and the mode controller.
                _setCurrentConnectionMap();
                // Update the map from an input port of the modal model
                // to the receivers of the current state.
                _currentLocalReceiverMap =
                    (Map)_localReceiverMaps.get(ctrl.currentState());

                // Get the new current refinement actor.
                TypedCompositeActor actor =
                    // FIXME
                    //(TypedCompositeActor)curState.getRefinement();
                    (TypedCompositeActor)(curState.getRefinement())[0];
                Director refinementDir = actor.getDirector();
                if (refinementDir instanceof HDFFSMDirector) {
                    refinementDir.postfire();
                } else if (refinementDir instanceof HDFDirector) {
                    Scheduler refinmentSched = ((StaticSchedulingDirector)
                        refinementDir).getScheduler();
                    refinmentSched.setValid(false);
                    //refinmentSched.getSchedule();
                    //System.out.println("getSchedule in HDFFSM postfire");
                    ((HDFDirector)refinementDir).getSchedule();
                } else if (refinementDir instanceof SDFDirector) {
                    Scheduler refinmentSched = ((StaticSchedulingDirector)
                        refinementDir).getScheduler();
                    refinmentSched.setValid(true);
                    ((SDFScheduler)refinmentSched).getSchedule();
                }

                // Extract the token consumption/production rates from the
                // ports of the new refinement and update the
                // rates of the ports of the FSM actor.
                _updateInputTokenConsumptionRates(actor);
                _updateOutputTokenProductionRates(actor);

                CompositeActor hdfActor = _getHighestFSM();
                Director director = hdfActor.getExecutiveDirector();
                if (director instanceof HDFDirector) {
                    ((HDFDirector)director).invalidateSchedule();
                }

                // Tell the scheduler that the current schedule is no
                // longer valid.
                if (_debug_info) System.out.println(getName() +
                                           " : invalidating " +
                                    "current schedule.");
            }
            return super.postfire();
        }
        if (_debug_info) System.out.println(getName() +
                             " :  postfire(): returning now.");

        return postfireReturn;
    }

    /** Return true if the mode controller is ready to fire.
     *
     *  @exception IllegalActionException If there is no controller.
     */
    public boolean prefire() throws IllegalActionException {
        if (_debug_info) System.out.println(getName() +
                                  " prefire() invoked.");
        return getController().prefire();
    }

    /** Create receivers and invoke the preinitialize() methods
     *  of all actors deeply contained by the container of this
     *  director. Propagate the consumption and production rates
     *  of the current state out to the corresponding ports of
     *  the container of this director. This method is invoked
     *  once per execution, before any iteration, and before the
     *  initialize() method.
     *
     *  @exception IllegalActionException If the preinitialize() method of
     *  one of the associated actors throws it, or there is no controller.
     */
    public void preinitialize() throws IllegalActionException {
        _firingsPerScheduleIteration = 1;
        _firingsSoFar = 0;
        _resetCurrentHDFFSM = true;
        super.preinitialize();

        FSMActor ctrl = getController();
        // Attempt to get the initial state from the mode controller.
        State initialState = ctrl.getInitialState();
        if (_debug_info) {
            System.out.println(getName() + " : preinitialize(): " +
                "initialState is " + initialState.getName());
        }
        // Get the current refinement.
        TypedCompositeActor curRefinement =
            // FIXME
            //(TypedCompositeActor)initialState.getRefinement();
            (TypedCompositeActor)(initialState.getRefinement())[0];
        if (curRefinement != null) {
            Director refinementDir = curRefinement.getDirector();
            if (_debug_info) {
                System.out.println(getName() + " : preinitialize()"
                    + "initial refinement is "
                    + curRefinement.getFullName());
                System.out.println(getName() + " : preinitialize(): "
                    + "initial director is " + refinementDir.getFullName());
            }

            if (refinementDir instanceof HDFFSMDirector) {
                refinementDir.preinitialize();
            } else if (refinementDir instanceof HDFDirector) {
                Scheduler refinmentSched = ((StaticSchedulingDirector)
                    refinementDir).getScheduler();
                refinmentSched.setValid(false);
                ((HDFDirector)refinementDir).getSchedule();
                if (_debug_info) System.out.println(getName() +
                    " : preinitialize(): refinement's director : " +
                        refinementDir.getFullName());

                if (_debug_info) {
                    CompositeActor container =
                        (CompositeActor)getContainer();
                    System.out.println(getName() +
                        " : preinitialize(): Name of HDF composite actor: " +
                                  ((Nameable)container).getName());
                }
            } else if (refinementDir instanceof SDFDirector) {
                Scheduler refinmentSched = ((StaticSchedulingDirector)
                    refinementDir).getScheduler();
                refinmentSched.setValid(false);
                ((SDFScheduler)refinmentSched).getSchedule();
            } else {
                // Invalid director.
                throw new IllegalActionException(this,
                  "The current refinement has an invalid director. " +
                  "Allowed directors are SDF, HDF, or HDFFSMDirector.");
            }

            _updateInputTokenConsumptionRates(curRefinement);
            _updateOutputTokenProductionRates(curRefinement);

            // Tell the scheduler that the current schedule is no
            // longer valid.
            if (_debug_info) System.out.println(getName() +
                        " : preinitialize(): invalidating " +
                                          "current schedule.");
            CompositeActor hdfActor = _getHighestFSM();
            Director director = hdfActor.getExecutiveDirector();
            ((StaticSchedulingDirector)director).invalidateSchedule();
        } else {
            throw new IllegalActionException(this,
                    "current refinement is null.");
        }
    }

    public void setCurrentHDFFSMReset(boolean resetCurrentHDFFSM) {
        _resetCurrentHDFFSM = resetCurrentHDFFSM;
    }

    /** Set the number of firings per iteration of this director.
     *  @param firings Number of firings per iteration of this director.
     */
    public void setFiringsPerScheduleIteration(int firings) {
        _firingsPerScheduleIteration = firings;
    }

    /** Return true if data are transferred from the input port of
     *  the container to the connected ports of the controller and
     *  of the current refinement actor.
     *  <p>
     *  This method will transfer all of the available tokens on each
     *  input channel. The port argument must be an opaque input port.
     *  If any channel of the input port has no data, then that
     *  channel is ignored. Any token left not consumed in the ports
     *  to which data are transferred is discarded.
     *
     *  @param port The input port to transfer tokens from.
     *  @return True if data are transferred.
     *  @exception IllegalActionException If the port is not an opaque
     *   input port.
     */
    public boolean transferInputs(IOPort port)
                   throws IllegalActionException {
        if (_debug_info) System.out.println(getName() +
                " : transferInputs() invoked");
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferInputs: port argument is not an opaque" +
                    "input port.");
        }
        boolean transferred = false;
        // The receivers of the current refinement that receive data
        // from "port."
        Receiver[][] insideReceivers = _currentLocalReceivers(port);
        // For each channel.
        for (int i = 0; i < port.getWidth(); i++) {
            int rate = SDFScheduler.getTokenConsumptionRate(port);
            if (_debug_info) System.out.println(getName() +
                                 " : transferInputs(): " +
                                           getFullName() +
                    ": transferInputs(): Rate of port: " +
                        port.getFullName() + " is " + rate);
            for (int k = 0; k < rate; k++) {
                try {
                    ptolemy.data.Token t = port.get(i);
                    if (insideReceivers != null && insideReceivers[i] != null) {
                        if (_debug_info) {
                            System.out.println(getName() +
                           " : transferInputs() " + getFullName() +
                                ": transferring input from port: " +
                                         port.getFullName() + "---");
                            System.out.println(getName() +
                            " : transferInputs(): token value =  " +
                                                       t.toString());
                        }
                        for (int j = 0; j < insideReceivers[i].length; j++) {
                            insideReceivers[i][j].put(t);
                        }
                        // Successfully transferred data, so return true.
                        transferred = true;
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(
                      "Director.transferInputs: Internal error: " +
                                                    ex);
                }
            }
        }
        return transferred;
    }

    /** Transfer data from an output port of the current
     *  refinement actor to the ports it is connected to on
     *  the outside. This method differs from the base class
     *  method in that this method will transfer all available
     *  tokens in the receivers, while the base class method will
     *  transfer at most one token. This behavior is required to
     *  handle the case of non-homogeneous actors. The port
     *  argument must be an opaque output port.  If any channel
     *  of the output port has no data, then that channel is ignored.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     */
    public boolean transferOutputs(IOPort port)
            throws IllegalActionException {
        if (_debug_info) System.out.println(getName() +
                  " : transferOutputs() invoked on port: "
                                      + port.getFullName());

        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "HDFFSMDirector: transferOutputs():" +
                                "  port argument is not " +
                                   "an opaque output port.");
        }
        boolean trans = false;
        Receiver[][] insideReceivers = port.getInsideReceivers();
        if (insideReceivers != null) {
            for (int i = 0; i < insideReceivers.length; i++) {
                // "i" is port index.
                // "j" is that port's channel index.
                if (insideReceivers[i] != null) {
                    if (_debug_info) System.out.println(getName() +
                    " : transferOutputs(): insideReceivers[0].length: " +
                                               insideReceivers[0].length);
                    for (int j = 0; j < insideReceivers[i].length; j++) {
                        while (insideReceivers[i][j].hasToken()) {
                            try {
                                ptolemy.data.Token t =
                                    insideReceivers[i][j].get();
                                if (_debug_info) {
                                    System.out.println(getName() +
                                      " : transferOutputs(): sending token.");
                                    System.out.println(getName() +
                                     " : transferOutputs(): token value =  " +
                                     t.toString());
                                }
                                port.send(i, t);
                                trans = true;
                            } catch (NoTokenException ex) {
                                throw new InternalErrorException(
                                        "Director.transferOutputs: " +
                                        "Internal error: " +
                                        ex);
                            }
                        }
                    }
                }
            }
        }
        return trans;
    }

    /** Update the number of firings per top-level iteration of
     *  each actor in the current refinment.
     *  @param directorFiringCount Number of firings per top-level
     *  iteration of the current director. It is also the number of
     *  firings per top-level iteration of the current refinement.
     *  @param preinitializeFlag A flag indicating whether this method
     *  is called in the preinitialize method.
     *  @exception IllegalActionException If no controller or current
     *  refinement can be found, or if the HDFDirector updateFiringCount
     *  method throws it.
     */
    public void updateFiringCount
            (int directorFiringCount, boolean preinitializeFlag)
            throws IllegalActionException {
        FSMActor ctrl = getController();
        State currentState;

        TypedCompositeActor currentRefinement;
        // Get the current refinement.
        if (preinitializeFlag) {
            currentState = ctrl.getInitialState();
            currentRefinement =
                (TypedCompositeActor)(currentState.getRefinement())[0];
        } else {
            currentState = ctrl.currentState();
            if (_debug_info) {
                System.out.println(" : HDFFSM get current state : "
                    + currentState.getName());
            }
            currentRefinement =
                     // FIXME
                     //(TypedCompositeActor)initialState.getRefinement();
            (TypedCompositeActor)(currentState.getRefinement())[0];
        }
        if (currentRefinement != null) {
            Director refinementDir = currentRefinement.getDirector();
            if (_debug_info) {
                System.out.println(getName() +
                    "HDFFSM current refinment director "
                    + refinementDir.getName());
            }
            if (refinementDir instanceof HDFFSMDirector) {
                ((HDFFSMDirector)refinementDir).
                    updateFiringCount(directorFiringCount, preinitializeFlag);
            } else if (refinementDir instanceof HDFDirector) {
                ((HDFDirector)refinementDir).
                    setDirectorFiringsPerIteration(directorFiringCount);
                ((HDFDirector)refinementDir).
                    updateFiringCount(directorFiringCount, preinitializeFlag);
            }
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** If the container of this director does not have an
     *  HDFFSMDirector as its executive director, then return it.
     *  Otherwise, move up the hierarchy until we reach a container
     *  actor that does not have an HDFFSMDirector director for its
     *  executive director.
     *
     *  @exception IllegalActionException If the top level director
     *  is an HDFFSMDirector.
     */
    private CompositeActor _getHighestFSM()
        throws IllegalActionException {
            // Keep moving up towards the toplevel of the hierarchy until
            // we find either an SDF or HDF executive director or we reach
            // the toplevel composite actor.
            CompositeActor container = (CompositeActor)getContainer();
            Director director = container.getExecutiveDirector();
            boolean foundValidDirector = false;
            while (foundValidDirector == false) {
                if (director == null) {
                    // We have reached the toplevel without finding a
                    // valid director.
                    throw new IllegalActionException(this,
                        "This model is not a refinement of an SDF or " +
                        "an HDF model.");
                } else if (director instanceof SDFDirector) {
                    foundValidDirector = true;
                } else if (director instanceof HDFDirector) {
                    foundValidDirector = true;
                } else {
                    // Move up another level in the hierarchy.
                    container = (CompositeActor)(container.getContainer());
                    director = container.getExecutiveDirector();
                }
            }
            return container;
        }

    private void _setTokenConsumptionRate(Entity e, IOPort port, int rate)
            throws NotSchedulableException {
        if (rate < 0) {
            throw new NotSchedulableException("Rate must be >= 0");
        }

        if (!port.isInput()) {
            throw new NotSchedulableException("IOPort "
                    + port.getName() + " is not an Input Port.");
        }
        Port pp = e.getPort(port.getName());
        if (!port.equals(pp)) {
            throw new NotSchedulableException("IOPort "
                + port.getName() + " is not contained in Entity "
                + e.getName());
        }
        Parameter param = (Parameter)
            port.getAttribute("tokenConsumptionRate");
        try {
            if (param != null) {
                param.setToken(new IntToken(rate));
            } else {
                param = new Parameter(port,"tokenConsumptionRate",
                        new IntToken(rate));
            }
        } catch (Exception exception) {
            // This should never happen.
            // e might be NameDuplicationException, but we already
            // know it doesn't exist.
            // e might be IllegalActionException, but we've already
            // checked the error conditions
            throw new InternalErrorException(exception.getMessage());
        }
    }

    private void _setTokenProductionRate(Entity e, IOPort port, int rate)
            throws NotSchedulableException {
        if (rate < 0) throw new NotSchedulableException(
                "Rate must be >= 0");
        if (!port.isOutput()) throw new NotSchedulableException("IOPort " +
                port.getName() + " is not an Output Port.");
        Port pp = e.getPort(port.getName());
        if (!port.equals(pp)) throw new NotSchedulableException("IOPort " +
                port.getName() + " is not contained in Entity " +
                e.getName());
        Parameter param = (Parameter)
            port.getAttribute("tokenProductionRate");
        try {
            if (param != null) {
                param.setToken(new IntToken(rate));
            } else {
                param = new Parameter(port,"tokenProductionRate",
                        new IntToken(rate));
            }
        } catch (Exception exception) {
            // This should never happen.
            // e might be NameDuplicationException, but we already
            // know it doesn't exist.
            // e might be IllegalActionException, but we've already
            // checked the error conditions
            throw new InternalErrorException(exception.getMessage());
        }
    }

    /* Extract the token consumption rates from the input
     * ports of the current refinement and update the
     * rates of the input ports of the HDF opaque composite actor
     * containing the refinment. The resulting mutation will cause
     * the SDF scheduler to compute a new schedule using the
     * updated rate information.
     *
     * @param actor The current refinement.
     */
    private void _updateInputTokenConsumptionRates(TypedCompositeActor actor)
            throws IllegalActionException {
        if (_debug_info) System.out.println(getName() + " : " +
                "_updateInputTokenConsumptionRates() invoked on actor: " +
                actor.getFullName());
        FSMActor ctrl = getController();
        // Get the current refinement's container.
        CompositeActor refineInPortContainer =
            (CompositeActor) actor.getContainer();
        // Get all of the input ports of the container of this director.
        List containerPortList = refineInPortContainer.inputPortList();
        // Set all of the port rates to zero.
        Iterator containerPorts = containerPortList.iterator();
        while (containerPorts.hasNext()) {
            IOPort containerPort = (IOPort)containerPorts.next();
            _setTokenConsumptionRate(refineInPortContainer,
                            containerPort,
                            0);
        }
        // Get all of its input ports of the current refinement actor.
        Iterator refineInPorts = actor.inputPortList().iterator();
        while (refineInPorts.hasNext()) {
            IOPort refineInPort =
                (IOPort)refineInPorts.next();
            if (_debug_info) System.out.println(getName() +
                " : _updateInputTokenConsumptionRates(): Current " +
                                         "port of refining actor " +
                                          refineInPort.getFullName()
                                          + " with rate = " +
            SDFScheduler.getTokenConsumptionRate(refineInPort));
            // Get all of the input ports this port is
            // linked to on the outside (should only consist
            // of 1 port).
            // Iterator inPorts = inputPortList().iterator();
            //            while (inPorts.hasNext()) {
            //                TypedIOPort inPort = (TypedIOPort)inPorts.next();
            Iterator inPortsOutside =
                refineInPort.deepConnectedInPortList().iterator();
            if (!inPortsOutside.hasNext()) {
                throw new IllegalActionException("Current " +
                        "state's refining actor has an input " +
                        "port not connected to an input port " +
                        "of its container.");
            }
            while (inPortsOutside.hasNext()) {
                IOPort inputPortOutside =
                    (IOPort)inPortsOutside.next();
                if (_debug_info) System.out.println(getName() +
                    " : _updateInputTokenConsumptionRates(): " +
                            " Current outside port connected " +
                                  "to port of refining actor " +
                                  inputPortOutside.getFullName());
                // Check if the current port is contained by the
                // container of the current refinment.
                ComponentEntity thisPortContainer =
                    (ComponentEntity)inputPortOutside.getContainer();
                String temp = refineInPortContainer.getFullName()
                    + "._Controller";
                if (thisPortContainer.getFullName() ==
                    refineInPortContainer.getFullName() ||
                    temp.equals(thisPortContainer.getFullName())) {
                    // The current port  is contained by the
                    // container of the current refinment.
                    // Update its consumption rate.
                    if (_debug_info) System.out.println(getName() +
                      " : _updateInputTokenConsumptionRates(): " +
                                         "Updating consumption " +
                                                "rate of port: " +
                                   inputPortOutside.getFullName());

                    // set the outside port rate = port rate of the refinement.
                    int portRateToSet = SDFScheduler
                        .getTokenConsumptionRate(refineInPort);
                    SDFScheduler.setTokenConsumptionRate
                        (inputPortOutside, portRateToSet);
                }
            }
        }
    }

    /** Extract the token production rates from the output
     *  ports of the current refinement and update the
     *  rates of the output ports of the HDF opaque composite actor
     *  containing the refinment. The resulting mutation will cause
     *  the SDF scheduler to compute a new schedule using the
     *  updated rate information.
     * @param actor The current refinement.
     */
    private void _updateOutputTokenProductionRates(TypedCompositeActor actor)
            throws IllegalActionException {
        if (_debug_info) System.out.println(getName() +
                    " : _updateOutputTokenProductionRates invoked " +
                    " for actor = " + actor.getName());
        // Get the current refinement's container.
        CompositeActor refineOutPortContainer =
            (CompositeActor) actor.getContainer();
        // Get all of the output ports of the container of this director.
        List containerPortList = refineOutPortContainer.outputPortList();
        // Set all of the external port rates to zero.
        Iterator containerPorts = containerPortList.iterator();
        while (containerPorts.hasNext()) {
            IOPort containerPort = (IOPort)containerPorts.next();
            _setTokenProductionRate(refineOutPortContainer,
                            containerPort,
                            0);
        }
        // Get all of the current refinement's output ports.
        Iterator refineOutPorts = actor.outputPortList().iterator();
        while (refineOutPorts.hasNext()) {
            IOPort refineOutPort =
                (IOPort)refineOutPorts.next();
            if (_debug_info) {
                System.out.println(getName() +
               " : _updateOutputTokenProductionRates(): Current " +
                                        "port of refining actor = " +
                                       refineOutPort.getFullName() +
                                 " with rate = " +
                      SDFScheduler.getTokenProductionRate(refineOutPort));
            }
            // Get all of the output ports this port is
            // linked to on the outside (should only consist
            // of 1 port).
            Iterator outPortsOutside =
                refineOutPort.deepConnectedOutPortList().iterator();
            //if (!outPortsOutside.hasNext()) {
            //throw new IllegalActionException("Current " +
            //          "state's refining actor has an output " +
            //          "port not connected to an output port " +
            //          "of its container.");
            //}
            while (outPortsOutside.hasNext()) {
                IOPort outputPortOutside =
                    (IOPort)outPortsOutside.next();
                if (_debug_info) System.out.println(getName() +
                  " : _updateOutputTokenProductionRates(): " +
                           "Current outside port connected " +
                                "to port of refining actor " +
                               outputPortOutside.getFullName());
                // Check if the current port is contained by the
                // container of the current refinment.
                ComponentEntity thisPortContainer =
                    (ComponentEntity)outputPortOutside.getContainer();
                String temp = refineOutPortContainer.getFullName()
                                    + "._Controller";
                if (thisPortContainer.getFullName() ==
                    refineOutPortContainer.getFullName()) {
                    // The current port  is contained by the
                    // container of the current refinment.
                    // Update its consumption rate.
                    if (_debug_info) System.out.println(getName() +
                        " : _updateOutputTokenProductionRates(): " +
                                            "Updating production " +
                            "rate of port: " +
                            outputPortOutside.getFullName());

                    // set the outside port = port rate of the refinement.
                    int portRateToSet = SDFScheduler
                        .getTokenProductionRate(refineOutPort);
                    SDFScheduler.setTokenProductionRate
                        (outputPortOutside, portRateToSet);
                } else if (temp.equals(thisPortContainer.getFullName())) {
                    if (_debug_info) System.out.println(getName() +
                        " : _updateOutputTokenConsumptionRates(): " +
                        "Updating controller's consumption " +
                        "rate of port: " +
                        outputPortOutside.getFullName());

                    // set the outside port = port rate of the refinement.
                    int portRateToSet = SDFScheduler
                        .getTokenProductionRate(refineOutPort);
                    SDFScheduler.setTokenConsumptionRate
                        (outputPortOutside, portRateToSet);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The number of times fire() has been called in the current
    // iteration of the SDF graph containing this FSM.
    private int _firingsSoFar;

    // Set to true to enable debugging.
    //private boolean _debug_info = true;
    private boolean _debug_info = false;

    // A flag indicating whether the current director
    // has made a transition with "reset" set to be true.
    private boolean _resetCurrentHDFFSM = false;

    // The firing count for the HDF actor (the container
    // of this director) in the current schedule.
    private int _firingsPerScheduleIteration = 1;
    private int _cachedFiringCount = -1;
}
