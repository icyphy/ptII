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
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
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
refines an HDF actor. This class may also be used as the director
of an FSM for an FSM when the FSM refines an SDF actor.
<p>
<b>Usage</b>
<p>
Hierarchical compositions of HDF with FSMs can be quite complex to
represent textually, even for simple models. It is therefore
recommended that a graphical model editor like Vergil be used to
construct the model.
<p>
The executive director must be HDF, SDF, or HDFFSMDirector.
Otherwise an exception will occur. An HDF actor that refines to
an FSM will use this class as the FSM's local director. All states
in the FSM must refine to either another FSM, an HDF model or a SDF
model. That is, all refinement actors must be opaque and must
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
Finite State Machines with Multiple Concurrency Models</A>,'' April 13,
1998.</LI>
</ol>

@author Brian K. Vogel
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
        _chooseTransition(st.nonpreemptiveTransitionList());
        return;
    }

    /** Invoke the initialize() method of each deeply contained actor.
     *  This method should be invoked once per execution, after the
     *  initialization phase, but before any iteration.  Since type
     *  resolution has been completed, the initialize() method of
     *  a contained actor may produce output or schedule events.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the initialize() method of
     *   one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        // Initialize the current firing count in the current
        // iteration of the current schedule of the executive
        // director.
        _firingsSoFar = 0;
    }

    /** Return a new receiver of a type compatible with this director.
     *  This returns an instance of SDFReceiver.
     *
     *  @return A new SDFReceiver.
     */
    public Receiver newReceiver() {
        return new SDFReceiver();
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
     *  the container of this director are updated to be consistant
     *  with the port rates of the new state's refinement. The HDF
     *  director will be notified of the change in port rates. If
     *  a change in port rates occurs and this FSM is governed by
     *  an SDF director, an exception will occur.
     *
     *  @return True if the mode controller wishes to be scheduled for
     *   another iteration.
     *  @exception IllegalActionException If a refinement throws it,
     *   if there is no controller, or if an inconsistancy in port
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
        if (_debug_info) {
            _debugPostfire(curState);
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
        // Check if the current iteration of the HDF/SDF graph in
        // which this FSM refines has completed yet. The
        // iteration is complete iff the current refinement has
        // been fired the number of times specified by the current
        // static schedule of the HDF/SDF graph in which this FSM
        // refines.
        if (_firingsPerScheduleIteration == -1) {
            // Get the firing count for the HDF actor (the container
            // of this director) in the current schedule.
            _firingsPerScheduleIteration =
                _getFiringsPerSchedulIteration();
        }
        if (_debug_info) {
            System.out.println(getName() + " :  postfire(): " +
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
                if (_debug_info) System.out.println(getName() +
                  " :  postfire(): Making a state transition back " +
                                             "to the current state. ");
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
                // Now update the token consumption/production rates of
                // the ports of the HDF actor refining to the FSM (this
                // actor's container). I.e., queue a mutation with the
                // manager.
                // This will cause the SDF scheduler to compute a new
                // schedule based in the new port rates.

                // Get the new current refinement actor.
                TypedCompositeActor actor =
                    // FIXME
                    //(TypedCompositeActor)curState.getRefinement();
                    (TypedCompositeActor)(curState.getRefinement())[0];
                // Extract the token consumption/production rates from the
                // ports of the current refinement and update the
                // rates of the ports of the HDF actor containing
                // the refinment.
                _updateInputTokenConsumptionRates(actor);
                _updateOutputTokenProductionRates(actor);
                // Tell the scheduler that the current schedule is no
                // longer valid.
                if (_debug_info) System.out.println(getName() +
                                           " : invalidating " +
                                            "current schedule.");
                // should only invalidate schedule if director is HDF.
                CompositeActor hdfActor = _getHighestFSM();
                Director director = hdfActor.getExecutiveDirector();
                if (director instanceof HDFDirector) {
                    ((HDFDirector)director).invalidateSchedule();
                }
                // Get the firing count for the HDF actor (the container
                // of this director) in the current schedule.
                _firingsPerScheduleIteration =
                    _getFiringsPerSchedulIteration();
            }
        }
        if (_debug_info) System.out.println(getName() +
                             " :  postfire(): returning now.");
        return postfireReturn;
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
        super.preinitialize();
        // Now propagate the type signature of the current refinment (the
        // refinement of the initial state) out to the corresponding ports
        // of the container of this director.
        // Update port consumption/production rates and invalidate
        // current SDF schedule.
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
                System.out.println(getName() + " : preinitialize(): " +
                         "refinementDir1 is " + refinementDir.getName());
            }
            if (refinementDir instanceof HDFFSMDirector) {
                refinementDir.preinitialize();
            } else if ((refinementDir instanceof SDFDirector) ||
                       (refinementDir instanceof HDFDirector)) {
                Scheduler refinmentSched =
                    ((StaticSchedulingDirector)refinementDir).getScheduler();
                refinmentSched.setValid(false);
                refinmentSched.getSchedule();
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

    /** Override the base class to create array variables if the controller
     *  is an instance of FSMActor.
     *  @see ptolemy.domains.fsm.kernel.FSMDirector#setInputVariables()
     */
    protected void _setInputVariables() throws IllegalActionException {
        FSMActor ctrl = getController();
        if (ctrl instanceof HDFFSMActor) {
            ((HDFFSMActor)ctrl)._setInputVariables(_firingsSoFar, _getFiringsPerSchedulIteration());
        } else {
           super._setInputVariables();
        }
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
        boolean trans = false;
        // The receivers of the current refinement that receive data
        // from "port."
        Receiver[][] insiderecs = _currentLocalReceivers(port);
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
                    if (insiderecs != null && insiderecs[i] != null) {
                        if (_debug_info) {
                            System.out.println(getName() +
                           " : transferInputs() " + getFullName() +
                                ": transfering input from port: " +
                                         port.getFullName() + "---");
                            System.out.println(getName() +
                            " : transferInputs(): token value =  " +
                                                       t.toString());
                        }
                        for (int j = 0; j < insiderecs[i].length; j++) {
                            insiderecs[i][j].put(t);
                        }
                        // Successfully transfered data, so return true.
                        trans = true;
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(
                      "Director.transferInputs: Internal error: " +
                                                    ex);
                }
            }
        }
        return trans;
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
        Receiver[][] insiderecs = port.getInsideReceivers();
        if (insiderecs != null) {
            for (int i = 0; i < insiderecs.length; i++) {
                if (insiderecs[i] != null) {
                    if (_debug_info) System.out.println(getName() +
                    " : transferOutputs(): insiderecs[0].length: " +
                                               insiderecs[0].length);
                    for (int j = 0; j < insiderecs[i].length; j++) {
                        while (insiderecs[i][j].hasToken()) {
                            try {
                                ptolemy.data.Token t =
                                    insiderecs[i][j].get();
                                if (_debug_info) System.out.println(getName() +
                                      " : transferOutputs(): sending token.");
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

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    /** Debug.
     */
    private void _debugPostfire(State curState)
        throws IllegalActionException {
            TypedCompositeActor ta =
                // FIXME
                //(TypedCompositeActor)curState.getRefinement();
                (TypedCompositeActor)(curState.getRefinement())[0];
            System.out.println(getName() + " :  postfire(): firing " +
                    "current refinment: " +
                    ta.getFullName());
            // Get all of its input ports.
            Iterator refineInPorts = ta.inputPortList().iterator();
            while (refineInPorts.hasNext()) {
                IOPort refineInPort =
                        (IOPort)refineInPorts.next();
                if (_debug_info) System.out.println(getName() +
                 " :  postfire(): Current port of refining actor " +
                        refineInPort.getFullName());
                if (_debug_info) System.out.println(getName() +
                       " :  postfire(): token consumption rate = " +
                        _getTokenConsumptionRate(refineInPort));
            }
            System.out.println(getName() + " :  postfire(): firing " +
                    "current refinment right now: " +
                    // FIXME
                    //((CompositeActor)(curState.getRefinement())).getFullName());
                    ((CompositeActor)(curState.getRefinement())[0]).getFullName());
    }

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

    /** Get the SDF or HDF scheduler associated with the container of
     *  this director.
     *
     *  @return The SDF or HDF scheduler.
     *  @exception IllegalActionException If there is no scheduler or
     *   if the container of this director is not directly or indirectly
     *   contained by a model governed by an SDF or HDF director.
     */
    private Scheduler _getDataflowScheduler()
        throws IllegalActionException {
        CompositeActor container =   _getHighestFSM();
        Director director = container.getExecutiveDirector();
        Scheduler scheduler =
            ((StaticSchedulingDirector)director).getScheduler();
        if (scheduler == null) {
            throw new IllegalActionException(this, "Unable to get " +
                                         "the SDF or HDF scheduler.");
        }
        return scheduler;
    }

    /** Return the firing count for the current refinement actor
     *  in the current dataflow schedule.
     *
     *  @return The firing count for the current refinement actor
     *  in the current schedule.
     *  @exception IllegalActionException If FIXME.
     */
    private int _getFiringsPerSchedulIteration()
        throws IllegalActionException {
        if (_debug_info) System.out.println(getName() +
      " :  _getFiringsPerSchedulIteration(): just got sdf schedule.");
        // Move up towards the top level of the hierarchy until we
        // reach an actor that is directly contained by either an
        // SDF or an HDF model.
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
        // Now, "container" is directly contained by either an
        // SDF or an HDF model.
        // Get the firing count of "container" in the schedule.
        if ((((StaticSchedulingDirector)director).isScheduleValid()) && (_cachedFiringCount  > -1)) {
            return _cachedFiringCount;
        } else if (director instanceof HDFDirector) {
            return ((HDFDirector)director).getFiringCount(container);
        } else if (director instanceof SDFDirector) {
            Scheduler scheduler =
            ((StaticSchedulingDirector)director).getScheduler();
            if (scheduler == null) {
                throw new IllegalActionException(this, "Unable to get " +
                                         "the SDF or HDF scheduler.");
            }
            Schedule schedule = scheduler.getSchedule();
            Iterator firings = schedule.firingIterator();
            int occurrence = 0;
            while (firings.hasNext()) {
                Firing firing = (Firing)firings.next();
                Actor actorInSchedule = (Actor)(firing.getActor());
                String actorInScheduleName =
                    ((Nameable)actorInSchedule).getName();
                String actorName = ((Nameable)container).getName();
                if (actorInScheduleName.equals(actorName)) {
                    // Current actor in the static schedule is
                    // the HDF composite actor containing this FSM.
                    // Increment the occurrence count of this actor.
                    occurrence += firing.getIterationCount();
                }

                if (_debug_info) {
                    System.out.println(getName() +
                                       " :  _getFiringsPerSchedulIteration(): Actor in static schedule: " +
                                       ((Nameable)container).getName());
                    System.out.println(getName() +
                                       " : _getFiringsPerSchedulIteration(): Actors in static schedule:" +
                                       occurrence);
                }
            }
            _cachedFiringCount = occurrence;
            return _cachedFiringCount;
        } else {
            throw new IllegalActionException(this, "The executive " +
                "director is invalid. The executive director " +
                "should be either an SDFDirector or an HDFDirector.");
        }
    }

    /** Get the number of tokens that are produced or consumed
     *  on the designated port of this Actor, as supplied by
     *  by the port's "tokenConsumptionRate" Parameter.   If the parameter
     *  does not exist, then assume the actor is homogeneous and return a
     *  rate of 1.
     *  @exception IllegalActionException If the TokenConsumptionRate
     *   parameter has an invalid expression.
     */
    private int _getTokenConsumptionRate(IOPort p)
            throws IllegalActionException {
        Parameter param =
            (Parameter)p.getAttribute("tokenConsumptionRate");
        if (param == null) {
            if (p.isInput())
                return 1;
            else
                return 0;
        } else
            return ((ptolemy.data.IntToken)param.getToken()).intValue();
    }

    /** Get the number of tokens that are produced or consumed
     *  on the designated port of this Actor during each firing,
     *  as supplied by
     *  by the port's "tokenProductionRate" Parameter.   If the parameter
     *  does not exist, then assume the actor is homogeneous and return a
     *  rate of 1.
     *  @exception IllegalActionException If the tokenProductionRate
     *   parameter has an invalid expression.
     */
    private int _getTokenProductionRate(IOPort p)
            throws IllegalActionException {
        Parameter param =
            (Parameter)p.getAttribute("tokenProductionRate");
        if (param == null) {
            if (p.isOutput())
                return 1;
            else
                return 0;
        }
        return ((ptolemy.data.IntToken)param.getToken()).intValue();
    }


    private void _setTokenConsumptionRate(Entity e, IOPort port, int rate)
            throws NotSchedulableException {
        if (rate < 0) throw new NotSchedulableException(
                "Rate must be >= 0");
        if (!port.isInput()) throw new NotSchedulableException("IOPort " +
                port.getName() + " is not an Input Port.");
        Port pp = e.getPort(port.getName());
        if (!port.equals(pp)) throw new NotSchedulableException("IOPort " +
                port.getName() + " is not contained in Entity " +
                e.getName());
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
                                          refineInPort.getFullName());
            // Get all of the input ports this port is
            // linked to on the outside (should only consist
            // of 1 port).
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
                if (thisPortContainer.getFullName() ==
                    refineInPortContainer.getFullName()) {
                    // The current port  is contained by the
                    // container of the current refinment.
                    // Update its consumption rate.
                    if (_debug_info) System.out.println(getName() +
                      " : _updateInputTokenConsumptionRates(): " +
                                         "Updating consumption " +
                                                "rate of port: " +
                                   inputPortOutside.getFullName());


                    int portRateToSet = SDFScheduler.getTokenConsumptionRate(refineInPort);
                    SDFScheduler.setTokenConsumptionRate(inputPortOutside, portRateToSet);
                }
            }
        }
    }

    /* Extract the token production rates from the output
     * ports of the current refinement and update the
     * rates of the output ports of the HDF opaque composite actor
     * containing the refinment. The resulting mutation will cause
     * the SDF scheduler to compute a new schedule using the
     * updated rate information.
     *
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
        // Get all of the ouput ports of the container of this director.
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

                    int portRateToSet = SDFScheduler.getTokenProductionRate(refineOutPort);
                    SDFScheduler.setTokenProductionRate(outputPortOutside, portRateToSet);
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
    // The firing count for the HDF actor (the container
    // of this director) in the current schedule.
    private int _firingsPerScheduleIteration = -1;
    private int _cachedFiringCount = -1;
}
