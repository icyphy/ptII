/* A HDFFSMDirector governs the execution of the finite state
   machine in heterochronous dataflow model.

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

@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.domains.hdf.kernel;

import ptolemy.domains.fsm.kernel.*;
import ptolemy.actor.Director;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.sched.Scheduler;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.actor.Actor;
import ptolemy.actor.TypedActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.Port;
import ptolemy.data.Token;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.kernel.*;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// HDFFSMDirector
/**
<h1>HDF overview</h1>
Read [1].
<h1>Class comments</h1>
An HDFFSMDirector governs the execution of the finite state
machine in heterochronous dataflow (HDF) model. This director should be used
as the director of a finite state machine when the finite state
 machine refines a heterochronous dataflow (HDF) actor. When this
class is the director, it is necessary to also use HDFFSMActor instead
of HDFFSMActor. Note that there
is currently no HDF director. An SDFDirector should be used as the
director of an HDF modal, and an exception will be thrown if one
attempts to use a different director.
<p>
<h1>Usage</h1>
The toplevel modal must be HDF. Note that SDF is a special case of
HDF, so the toplevel modal is allowed to be SDF. The director of the
toplevel model must be an instance of SDFDirector, or else an exception
will occur. The toplevel HDF model is constructed just like an SDF
model, except that it may contain HDF actors as well as SDF actors.
All HDF actors must refine to an FSM with this class as the FSM's
local director. All states in the FSM must refine to either another
FSM, an HDF diagram or a SDF diagram. There is no constraint on the
number of levels in the hierarchy.
<p>
Currently, constructing the FSM is somewhat akward. To construct an FSM,
first create a TypedCompositeActor in the HDF diagram to contain
the FSM. This TypedCompositeActor will henceforth be refered to as
"the HDF actor." Create an HDFFSMDirector with the HDF actor as its
container. Create an HDFFSMActor actor with the HDF actor as
its container. Create one TypedComposite actor for each state
in the FSM, with the HDF actor as its container. This
TypedComposite actor is henceforth refered to as "the refinement."
Create the necessary ports on each refinement
such that each refining state actor contains the same number and
type of ports (typically input or output TypedIOPort) with the
same name as the corresponding ports of the HDF actor. Create a
relation for each port of the HDF actor. For each relation, link
all ports with the same name to the relation. (FIXME: consider
having the director do this automatically) (FIXME: Curently,
the ports of a refinement must be of type SDFIOPort)
<p>
The FSM diagram itself is constructed inside the HDFFSMActor.
To construct the FSM diagram, create an instance of State, for
each state in the FSM, with the HDFFSMActor as its
container. The parameter "refinementName" of each State should
be set to the name of the refinement.
Use the "initialStateName" parameter of HDFFSMActor to
set the initial state. Create an instance of Transition for
each transition in the FSM, with the HDFFSMActor as its container.
The guard expression of a transition is set by using the
setGuardExpression() method of Transition, with a guard expression
string as the parameter.
The guard expression is evaluated only after a "Type B firing" [1],
which is the last firing of the HDF actor in the current iteration
of the current HDF schedule.
A state transition (possibly back to the current state) will occurr
if the guard expression evaluates to true after a "Type B firing."
<p>
<h1>Guard expression syntax</h1>
The guard expressions use the Ptolemy II expression language. Currently,
the only variables allowed in the guard expressions are variables
containing tokens transfered through the input or output ports of
the HDF actor. Following the syntax of [1], if the HDF actor contains
an input port called dataIn, then use dataIn$0 in the guard
expression to reference the token most recently transfered through
port dataIn. Use dataIn$1 to reference the next most recent token,
dataIn$2 to reference the next most recent token, and so on. By
default, only the most recently transfered token is allowed.
In order to be able to reference up to the m'th most recently
transfered token (dataIn$m), call the setGuardTokenHistory()
method with m as the parameter. (FIXME: m is currently limited to 1)

<H1>References</H1>

<OL>
<LI>
A. Girault, B. Lee, and E. A. Lee, ``<A HREF="http://ptolemy.eecs.berkeley.edu/papers/98/starcharts">Hierarchical
Finite State Machines with Multiple Concurrency Models</A>,'' April 13,
1998.</LI>

@author Brian K. Vogel
@version: $Id$

*/
// FIXME: Throw exception if the executive director != SDFDirector or
// != HDFFSMDirector.
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

    /** Set the values of input variables in the mode controller. If the refinement of the current state of the mode controller
     *  is ready to fire, then fire the current refinement. If there is exactly one
     *  non-preemptive transition enabled then it is chosen and the choice
     *  actions contained by the transition are executed. Note that choice actions are not really needed (but are allowed) in HDF with FSMs, since a the current refinement can be used to produce output instead of a choice action.
     *
     *  @exception IllegalActionException If there is more than one
     *   transition enabled, or there is no controller, or thrown by any
     *   choice action.
     */
    public void fire() throws IllegalActionException {
        if(_debug_info) System.out.println(getName() + " fire() invoked.");
        HDFFSMActor ctrl = (HDFFSMActor)getController();
        ctrl.setInputVariables();
        State st = ctrl.currentState();
	Actor ref = ctrl.currentState().getRefinement();
        _fireRefinement = false;
        if (ref != null) {
	  if(_debug_info) System.out.println(getName() + " fire(): refinement is " + ((CompositeActor)ref).getName());
	  // Note that we do not call refinement.prefire() in 
	  // this.prefire(). This is because transferInputs() is
	  // called by the composite actor in fire(), and 
	  // refinement.prefire() should not be invoked until
	  // the tokens have been transfered to the input ports of
	  // the current refinement.
	  _fireRefinement = ref.prefire();
        }
	if(_debug_info) System.out.println(getName() + " fire(): refinement is ready to fire = " + _fireRefinement);
        if (_fireRefinement) {
            if(_debug_info) System.out.println(getName() + " firingrefinement");
	    // Fire the refinement.
            ref.fire();
            ctrl.setInputsFromRefinement();
        }
	// FIXME: what does this do?
        ctrl.chooseTransition(st.nonpreemptiveTransitionList());
        return;
    }

    /** Invoke the initialize() method of each deeply contained actor.
     *  Initialize the current firing count in the current
     *  iteration of the current schedule of the executive
     *  director. Note that the executive director will
     *  be an instance of SDFDirector.
     *  This method should be invoked once per execution, after the
     *  initialization phase, but before any iteration.  Since type
     *  resolution has been completed, the initialize() method of a contained
     *  actor may produce output or schedule events.
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
     *  @return A new SDFReceiver.
     */
    public Receiver newReceiver() {
        return new SDFReceiver();
    }

    /** Return true if the mode controller is ready to fire.
     *  If this model is not at the top level and the current
     *  time of this director lags behind that of the executive director,
     *  update the current time to that of the executive director. 
     *
     *  @exception IllegalActionException If there is no controller.
     */
    public boolean prefire() throws IllegalActionException {
	if(_debug_info) System.out.println(getName() + " prefire() invoked.");
        Nameable container = getContainer();
        if (!(container instanceof Actor)) return false;
        Actor cont = (Actor)container;
        Director exeDir = cont.getExecutiveDirector();
        HDFFSMActor ctrl = (HDFFSMActor)getController();
        Actor ref = ctrl.currentState().getRefinement();
	// Note: current time stuff is not needed for HDF.
        if (exeDir != null) {
            double outTime = exeDir.getCurrentTime();
            if (getCurrentTime() < outTime) {
                setCurrentTime(outTime);
            }
        }
        // Otherwise there's no notion of time.

	// FIXME: move this code into the fire() method.
        //_fireRefinement = false;
        //if (ref != null) {
	//  if(_debug_info) System.out.println(getName() + " prefire(): refinement is " + ((CompositeActor)ref).getName());
	//  _fireRefinement = ref.prefire();
        //}
	//if(_debug_info) System.out.println(getName() + " prefire(): refinement is ready to fire = " + _fireRefinement);
        return getController().prefire();
    }


    /** Return true if the mode controller wishes to be scheduled for
     *  another iteration. Postfire the refinement of the current state
     *  of the mode controller.
     *  If a type B firing has occured and exactly
     *  one transition is enabled, then change state to the destination
     *  state of the enabled trasition. Note that a type B firing is
     *  the last firing of an actor in an iteration of the graph in
     *  which it is embedded.

 If a state change occurs, change
     *  the port rates of the HDF actor (the container of this director)
     *  to be consistant with the port rates of the new state and
     *  tell the executive director (an SDFDirector) to invalidate
     *  its current schedule and compute a new schedule.
     *  @return True if the mode controller wishes to be scheduled for
     *   another iteration.
     *  @exception IllegalActionException If thrown by any commit action
     *   or there is no controller.
     */
    // FIXME: This method is WAY too long. Consider breaking it
    // into several smaller methods to make the code more
    // readable.
    public boolean postfire() throws IllegalActionException {

        HDFFSMActor ctrl = (HDFFSMActor)getController();
	State curState = ctrl.currentState();
	// Get the current refinement actor.
	TypedActor currentRefinement = 
	    curState.getRefinement();

	if (currentRefinement == null) {
	    throw new IllegalActionException(this,
                    "Can't postfire because current refinement is null.");
	}
	// For debugging.
	if(_debug_info) {
	    TypedCompositeActor ta = (TypedCompositeActor)curState.getRefinement();
	    System.out.println(getName() + " :  postfire(): firing " +
                    "current refinment: " +
                    ta.getFullName());
	    // Get all of its input ports.
	    Iterator refineInPorts = ta.inputPortList().iterator();
	    while (refineInPorts.hasNext()) {
		IOPort refineInPort =
			(IOPort)refineInPorts.next();
		if (_debug_info) System.out.println(getName() + " :  postfire(): Current port of refining actor " +
                        refineInPort.getFullName());
		if (_debug_info) System.out.println(getName() + " :  postfire(): token consumption rate = " +
                        _getTokenConsumptionRate(refineInPort));
	    }
	    System.out.println(getName() + " :  postfire(): firing " +
                    "current refinment right now: " +
                    ((CompositeActor)(curState.getRefinement())).getFullName());
	}
	// Postfire the current refinement.
	boolean postfireReturn = currentRefinement.postfire();

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
	    // of times speified by the current static schedule.
	    // A state transition can now occur.

	    // Set firing count back to zero for next iteration.
	    _firingsSoFar = 0;

	    ///////////////////////////////////////////////////////////////
	    // FIXME: 
	    Transition lastChosenTr = ctrl._getLastChosenTransition();
	    //Transition lastChosenTr = ctrl._lastChosenTransition;
	    
	    if (lastChosenTr  == null) {
		// There is no enabled transition, so remain in the
		// current state.
		if (_debug_info) System.out.println(getName() + " :  postfire(): Making a state transition back " +
                        "to the current state. ");
	    } else {
		// Make a state transition (possibly back to the current
		// state).

		// The HDF/SDF graph in which this FSM is embedded has
		// just finished one iteration, so make a state 
		// transition.

		if (_debug_info) System.out.println(getName() + " :  postfire(): Making a state transition. ");
		// Update the current refinement to point to the destination
		// state of the (enabled) transition.
		// FIXME:
		State newState = lastChosenTr.destinationState();
		ctrl.setCurrentState(newState);
		if (_debug_info) System.out.println(getName() + " : postfire(): making state transition to: " + newState.getFullName());
		curState = newState;
		// Since a state change has occured, recompute the
		// Mapping from input ports of the modal model to
		// their corresponding receivers of the new current
		// state and the mode controller.
		ctrl.setCurrentConnectionMap();

		// Update the map from an input port of the modal model
		// to the receivers of the current state.
		_currentLocalReceiverMap =
		    (Map)_localReceiverMaps.get(ctrl.currentState());

		// execute the transition actions
		//_takenTransition.executeTransitionActions();

		// Now update the token consumption/production rates of
		// the ports of the HDF actor refining to the FSM (this
		// actor's container). I.e., queue a mutation with the
		// mangaer.
		// This will cause the SDF scheduler to compute a new
		// schedule based in the new port rates.

		// Get the new current refinement acotr.
		TypedCompositeActor actor =
		    (TypedCompositeActor)curState.getRefinement();
		// Extract the token consumption/production rates from the
		// ports of the current refinement and update the
		// rates of the ports of the HDF actor containing
		// the refinment.
		_updateInputTokenConsumptionRates(actor);
		_updateOutputTokenProductionRates(actor);
		// Tell the scheduler that the current schedule is no
		// longer valid.
		if (_debug_info) System.out.println(getName() + " : invalidating " +
                        "current schedule.");
		// FIXME: remove this after hdf director is complete?
		_invalidateSchedule();
		// Get the firing count for the HDF actor (the container
		// of this director) in the current schedule.
		_firingsPerScheduleIteration = 
		    _getFiringsPerSchedulIteration();
	    }
	} 
	if (_debug_info) System.out.println(getName() + " :  postfire(): returning now.");
        return postfireReturn;
    }

    /** Create receivers and invoke the preinitialize() methods of all
     *  actors deeply contained by the container of this director.
     *  Propagate the consumption and production rates of the current state
     *  out to the corresponding ports of the container of this director.
     *  Note that in HDF, distinct current refinements may have distinct
     *  type signatures (port rates).
     *  This method is invoked once per execution, before any iteration,
     *  and before the initialize() method.
     *  @exception IllegalActionException If the preinitialize() method of
     *  one of the associated actors throws it, or there is no controller.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
	// The above will preinitialize all actors under the control of
	// this director. The ports of all refining actors (which contain
	// an HDF diagram) will now have the correct type signatures.
        //_buildLocalReceiverMaps();

	// Now propagate the type signature of the current refinment (the
	// refinement of the initial state) out to the corresponding ports
	// of the container of this director.
        // Update port consumption/production rates and invalidate
	// current SDF schedule.
	HDFFSMActor ctrl = (HDFFSMActor)getController();
	// Attempt to get the initial state from the mode controller.
	State initialState = ctrl.getInitialState();

	if (_debug_info) {
	    System.out.println(getName() + " : preinitialize(): " +
		"initialState is " + initialState.getName());
	}
	// Get the current refinement.
	TypedCompositeActor curRefinement =
	    (TypedCompositeActor)initialState.getRefinement();
	if (curRefinement != null) {
	    // FIXME: should not cast to SDFDirector.
	    // Should also allow HDFDirector and HDFFSMDirector.
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
		refinmentSched.schedule();
		if (_debug_info) System.out.println(getName() + " : preinitialize(): refinement's director : " + refinementDir.getFullName());
		
		if (_debug_info) {
		    CompositeActor container = (CompositeActor)getContainer();
		    System.out.println(getName() + " : preinitialize(): Name of HDF composite acotr: " +
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
	    if (_debug_info) System.out.println(getName() + " : preinitialize(): invalidating " +
                    "current schedule.");
	    _invalidateSchedule();
	} else {
	    throw new IllegalActionException(this,
                    "current refinement is null.");
	}
    }

    /** Return true if data are transferred from the input port of
     *  the container to the ports connected to the inside of the input
     *  port
     // FIXME: NO.
     and on the mode controller or the refinement of its current
     *  state. This method will transfer all of the available tokens on each
     *  input channel. The port
     *  argument must be an opaque input port. If any channel of the
     *  input port has no data, then that channel is ignored. Any token
     *  left not consumed in the ports to which data are transferred is
     *  discarded.
     *  @param port The input port to transfer tokens from.
     *  @return True if data are tranferred.
     *  @exception IllegalActionException If the port is not an opaque
     *   input port.
     */
    public boolean transferInputs(IOPort port) throws IllegalActionException {
	if(_debug_info) System.out.println(getName() +
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
	    if(_debug_info) System.out.println(getName() + " : transferInputs(): " + getFullName() +
                    ": transferInputs(): Rate of port: " +
                    port.getFullName() + " is " + rate);
	    for(int k = 0; k < rate; k++) {
		try {
                    ptolemy.data.Token t = port.get(i);
                    if (insiderecs != null && insiderecs[i] != null) {
                        if(_debug_info) {
			    System.out.println(getName() + " : transferInputs() " + getFullName() +
					       ": transfering input from port: " + port.getFullName() + "---");
			    System.out.println(getName() + " : transferInputs(): token value =  " + t.toString());
			}
                        for (int j = 0; j < insiderecs[i].length; j++) {
                            insiderecs[i][j].put(t);
			    // begin debug:
			    //Receiver rec1 = insiderecs[i][j];
			    //IOPort rec1Container = rec1.getContainer();
			    //if(_debug_info) System.out.println("HDFFSMDirector: " + getFullName() +
			    //" : transferInputs(): Just put token in port: " +
				//	rec1Container.getFullName());
			    // end debug.
                        }
			// Sucessfully transfered data, so return true.
                        trans = true;
                    }
		} catch (NoTokenException ex) {
		    // this shouldn't happen.
		    throw new InternalErrorException(
                            "Director.transferInputs: Internal error: " +
                            ex.getMessage());
		}
	    }
        }
        return trans;
    }

    /** Return true if transfers data from an output port of the
     *  container to the ports it is connected to on the outside.
     *  This method differs from the base class method in that this
     *  method will transfer all available tokens in the receivers,
     *  while the base class method will transfer at most one token.
     *  This behavior is required to handle the case of non-homogeneous
     *  opaque composite actors. The port argument must be an opaque
     *  output port.  If any channel of the output port has no data,
     *  then that channel is ignored.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     */
    public boolean transferOutputs(IOPort port)
            throws IllegalActionException {
	if(_debug_info) System.out.println(getName() + " : transferOutputs() invoked on port: "
                + port.getFullName());

        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "HDFFSMDirector: transferOutputs(): port argument is not " +
                    "an opaque output port.");
        }
        boolean trans = false;
        Receiver[][] insiderecs = port.getInsideReceivers();
        if (insiderecs != null) {
            for (int i = 0; i < insiderecs.length; i++) {
                if (insiderecs[i] != null) {
		    if(_debug_info) System.out.println(getName() + " : transferOutputs(): insiderecs[0].length: " +
                            insiderecs[0].length);
                    for (int j = 0; j < insiderecs[i].length; j++) {
			while (insiderecs[i][j].hasToken()) {
                            try {
                                ptolemy.data.Token t = insiderecs[i][j].get();
				if(_debug_info) System.out.println(getName() + 
                                        "transferOutputs(): sending token.");
                                port.send(i, t);
                                trans = true;
                            } catch (NoTokenException ex) {
                                throw new InternalErrorException(
                                        "Director.transferOutputs: " +
                                        "Internal error: " +
                                        ex.getMessage());
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
	if (_debug_info) System.out.println(getName() + " :  _getFiringsPerSchedulIteration(): just got sdf schedule.");
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
	// FIXME: replace this with cleaner/faster code.
	Scheduler scheduler = 
	    ((StaticSchedulingDirector)director).getScheduler();
	if (scheduler == null) {
	    throw new IllegalActionException(this, "Unable to get " + 
					 "the SDF or HDF scheduler.");
	}
	Enumeration actors = scheduler.schedule();
	int occurrence = 0;
	while (actors.hasMoreElements()) {
	    Actor actor = (Actor)actors.nextElement();
	    String scheduleName = ((Nameable)actor).getName();
	    String actorName = ((Nameable)container).getName();
	    if (scheduleName.equals(actorName)) {
		// Current actor in the static schedule is
		// the HDF compisite actor containing this FSM.
		// Increment the occurence count of this actor.
		occurrence++;
	    }

	    if (_debug_info) { 
		System.out.println(getName() + " :  _getFiringsPerSchedulIteration(): Actor in static schedule: " +
				   ((Nameable)actor).getName());
		System.out.println(getName() + " :  _getFiringsPerSchedulIteration(): Actors in static schedule: **** " +
				   occurrence);
		System.out.println(getName() + " :  _getFiringsPerSchedulIteration(): current fire count: **** " +
		   _firingsSoFar);
		
	    }
	}
	return occurrence;
    }

    /** Get the number of tokens that are produced or consumed
     *  on the designated port of this Actor, as supplied by
     *  by the port's "tokenConsumptionRate" Parameter.   If the parameter
     *  does not exist, then assume the actor is homogeneous and return a
     *  rate of 1.
     *  @exception IllegalActionException If the TokenConsumptionRate
     *   parameter has an invalid expression.
     */
    protected int _getTokenConsumptionRate(IOPort p)
            throws IllegalActionException {
        Parameter param = (Parameter)p.getAttribute("tokenConsumptionRate");
        if(param == null) {
            if(p.isInput())
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
    protected int _getTokenProductionRate(IOPort p)
            throws IllegalActionException {
        Parameter param = (Parameter)p.getAttribute("tokenProductionRate");
        if(param == null) {
            if(p.isOutput())
                return 1;
            else
                return 0;
        }
        return ((ptolemy.data.IntToken)param.getToken()).intValue();
    }

    /** Invalidate the current dataflow schedule. This needs to
     *  be done when we make a state transition to a new state
     *  such that the type signature of the new state is 
     *  different from the type signature of the old state.
     *
     *  @exception IllegalActionException If there is a
     *   problem invalidating the schedule. This should
     *   not happen.
     */
    private void _invalidateSchedule() 
	throws IllegalActionException {
	Scheduler scheduler = _getDataflowScheduler();
	scheduler.setValid(false);
    }

    protected void _setTokenConsumptionRate(Entity e, IOPort port, int rate)
            throws NotSchedulableException {
        if(rate < 0) throw new NotSchedulableException(
                "Rate must be >= 0");
        if(!port.isInput()) throw new NotSchedulableException("IOPort " +
                port.getName() + " is not an Input Port.");
        Port pp = e.getPort(port.getName());
        if(!port.equals(pp)) throw new NotSchedulableException("IOPort " +
                port.getName() + " is not contained in Entity " +
                e.getName());
        Parameter param = (Parameter)
            port.getAttribute("tokenConsumptionRate");
        try {
            if(param != null) {
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

    protected void _setTokenProductionRate(Entity e, IOPort port, int rate)
            throws NotSchedulableException {
        if(rate < 0) throw new NotSchedulableException(
                "Rate must be >= 0");
        if(!port.isOutput()) throw new NotSchedulableException("IOPort " +
                port.getName() + " is not an Output Port.");
        Port pp = e.getPort(port.getName());
        if(!port.equals(pp)) throw new NotSchedulableException("IOPort " +
                port.getName() + " is not contained in Entity " +
                e.getName());
        Parameter param = (Parameter)
            port.getAttribute("tokenProductionRate");
        try {
            if(param != null) {
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
    // FIXME: queue the mutation with the director?
    // FIXME: Need to check that preinitialize has already been invoked
    // on parameter "actor".
    protected void _updateInputTokenConsumptionRates(TypedCompositeActor actor)
            throws IllegalActionException {
	if (_debug_info) System.out.println(getName() + " : " +
                "_updateInputTokenConsumptionRates() invoked on actor: " +
                actor.getFullName());

	// Get all of its input ports.
	Iterator refineInPorts = actor.inputPortList().iterator();
	// Get the current refinement's container.
	ComponentEntity refineInPortContainer =
	    (ComponentEntity) actor.getContainer();

	while (refineInPorts.hasNext()) {
	    IOPort refineInPort =
		(IOPort)refineInPorts.next();
	    if (_debug_info) System.out.println(getName() + " : _updateInputTokenConsumptionRates(): Current port of refining actor " +
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
		if (_debug_info) System.out.println(getName() + " : _updateInputTokenConsumptionRates(): Current outisde port connected " +
                        "to port of refining actor " +
                        inputPortOutside.getFullName());
		// Check if the current port is contained by the
		// container of the current refinment.
		ComponentEntity thisPortContainer =
		    (ComponentEntity)inputPortOutside.getContainer();
		if (thisPortContainer.getFullName() == refineInPortContainer.getFullName()) {
		    // The current port  is contained by the
		    // container of the current refinment.
		    // Update its consumption rate.
		    if (_debug_info) System.out.println(getName() + " : _updateInputTokenConsumptionRates(): Updating consumption " +
                            "rate of port: " +
                            inputPortOutside.getFullName());
		    // Get the port to which "refineInPort" is
		    // connected on the inside.
		    // FIXME: ???
		    List listOfPorts = refineInPort.insidePortList();
		    // Just get the first port from the list
		    // since they all must have the same rate.
		    int refineInPortRate;
		    if (listOfPorts.isEmpty()) {
			// Just assume the rate is 1. This could happen
			// if a Source actor is inside (no input ports).
			refineInPortRate = 1;
		    } else {
			IOPort portWithRateInfo =
			    (IOPort)listOfPorts.get(0);

			refineInPortRate =
			    _getTokenConsumptionRate(portWithRateInfo);		    }

		    if (_debug_info) System.out.println(getName() + " : _updateInputTokenConsumptionRates(): Port: " + refineInPort.getFullName() + " rate = " + _getTokenConsumptionRate(refineInPort));
		    //if (_debug_info) System.out.println("Port: " + refineInPort.getFullName() + " rate = " + ((SDFIOPort)refineInPort).getTokenConsumptionRate());
		    if (_debug_info) System.out.println(getName() + " : _updateInputTokenConsumptionRates(): New consumption rate is " +
                            refineInPortRate);
				// FIXME: call requestChange in Manager for this?
                    _setTokenConsumptionRate(refineInPortContainer,
                            inputPortOutside,
                            refineInPortRate);
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
    // FIXME: queue the mutation with the director?
    protected void _updateOutputTokenProductionRates(TypedCompositeActor actor)
            throws IllegalActionException {
	// Get all of its input ports.
	Iterator refineOutPorts = actor.outputPortList().iterator();
	// Get the current refinement's container.
	ComponentEntity refineOutPortContainer =
	    (ComponentEntity) actor.getContainer();

	while (refineOutPorts.hasNext()) {
	    IOPort refineOutPort =
		(IOPort)refineOutPorts.next();
	    if (_debug_info) System.out.println(getName() + " : _updateOutputTokenProductionRates(): Current port of refining actor " +
                    refineOutPort.getFullName());


	    // Get all of the output ports this port is
	    // linked to on the outside (should only consist
	    // of 1 port).
	    Iterator outPortsOutside =
		refineOutPort.deepConnectedOutPortList().iterator();
	    if (!outPortsOutside.hasNext()) {
		throw new IllegalActionException("Current " +
                        "state's refining actor has an output " +
                        "port not connected to an output port " +
                        "of its container.");
	    }
	    while (outPortsOutside.hasNext()) {
		IOPort outputPortOutside =
		    (IOPort)outPortsOutside.next();
		if (_debug_info) System.out.println(getName() + " : _updateOutputTokenProductionRates(): Current outisde port connected " +
                        "to port of refining actor " +
                        outputPortOutside.getFullName());
		// Check if the current port is contained by the
		// container of the current refinment.
		ComponentEntity thisPortContainer =
		    (ComponentEntity)outputPortOutside.getContainer();
		if (thisPortContainer.getFullName() == refineOutPortContainer.getFullName()) {
		    // The current port  is contained by the
		    // container of the current refinment.
		    // Update its consumption rate.
		    if (_debug_info) System.out.println(getName() + " : _updateOutputTokenProductionRates(): Updating production " +
                            "rate of port: " +
                            outputPortOutside.getFullName());
		    // Get the port to which "refineOutPort" is
		    // connected on the inside.
		    List listOfPorts = refineOutPort.insidePortList();
		    // Just get the first port from the list
		    // since they all must have the same rate.
		    int refineOutPortRate;
		    if (listOfPorts.isEmpty()) {
			// Just assume the rate is 1. This could happen
			// if a Sink actor is inside (no ouput ports).
			refineOutPortRate = 1;
		    } else {
			IOPort portWithRateInfo =
			    (IOPort)listOfPorts.get(0);

			refineOutPortRate =
			    _getTokenProductionRate(portWithRateInfo);
		    }
		    if (_debug_info) System.out.println(getName() + " : _updateOutputTokenProductionRates(): New production rate is " +
                            refineOutPortRate);
				// FIXME: call requestChange in Manager for this?
                    _setTokenProductionRate(refineOutPortContainer,
                            outputPortOutside,
                            refineOutPortRate);
		}
	    }
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The number of times fire() has been called in the current
    // iteration of the SDF graph containing this FSM.
    private int _firingsSoFar;
    // Set to true to enable debuging.
    //private boolean _debug_info = true;
    private boolean _debug_info = false;
    // The firing count for the HDF actor (the container
    // of this director) in the current schedule.
    private int _firingsPerScheduleIteration = -1;

}
