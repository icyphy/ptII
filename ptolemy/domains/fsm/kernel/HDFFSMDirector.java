/* A HDFFSMDirector governs the execution of the finite state
   machine in heterochronous dataflow model.

 Copyright (c) 1999 The Regents of the University of California.
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
@AcceptedRating Red (vogel@eecs.berkeley.edu)

*/

package ptolemy.domains.fsm.kernel;

import ptolemy.actor.Director;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.sched.Scheduler;
import ptolemy.actor.Actor;
import ptolemy.actor.TypedActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.ComponentEntity;
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
of FSMActor. Note that there 
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
@see HDFFSMActor
@see State
@see Transition
@see Action
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
     *  @exception It may be thrown in derived classes if the
     *      director is not compatible with the specified container.
     */
    public HDFFSMDirector(CompositeActor container, String name)
            throws IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the number of tokens in the guard token history.
     *  This returns the value set by setGuardTokenHistory().
     *  The default is one.
     *
     *  @return The number of tokens in the history.
     */
    // FIXME: Either implement setGuardTokenHistory(), or get rid
    // of this.
    public int getGuardTokenHistory() {
	//return _guardLength;
	return 1;
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
	_currentIterFireCount = 0;
    }

    /** Return a new receiver of a type compatible with this director.
     *  This returns an instance of SDFReceiver.
     *  @return A new SDFReceiver.
     */
    public Receiver newReceiver() {
      return new SDFReceiver();
    }

    /** Return true if the mode controller wishes to be scheduled for
     *  another iteration. Postfire the refinement of the current state
     *  of the mode controller.
     *  If a type B firing has occured and exactly
     *  one transition is enabled, then change state to the destination
     *  state of the enabled trasition. If a state change occurs, change
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
	// FIXME: Is this check really necessary?
        if (_fireRefinement) {
	    // Postfire the current refinement.
            ctrl.currentState().getRefinement().postfire();
        }
        //boolean result = ctrl.postfire();

        Transition trans;
	State curState = ctrl.currentState();
	// Fire the refinement.
	if (curState.getRefinement() != null) {
	    // DEBUG:
	    TypedCompositeActor ta = (TypedCompositeActor)curState.getRefinement();
	    if (_debugging) _debug("HDFFSMDirector:  postfire(): firing " +
				   "current refinment: " + 
				   ta.getFullName());
	    // Get all of its input ports.
	    Enumeration refineInPorts = ta.inputPorts();
	    while (refineInPorts.hasMoreElements()) {
		IOPort refineInPort =
		    (IOPort)refineInPorts.nextElement();
		if (_debugging) _debug("Current port of refining actor " +
				       refineInPort.getFullName());
		if (_debugging) _debug("token consumption rate = " +
				       _getTokenConsumptionRate(refineInPort));
	    }

	    if (_debugging) _debug("HDFFSMDirector:  postfire(): firing " +
				   "current refinment right now: " + 
				   ((CompositeActor)(curState.getRefinement())).getFullName());

	    // Increment current firing count. This is the number of
	    // times the current refining HDF actor has been fired
	    // in the current iteration of the current static schedule
	    // of the SDF/HDF graph containing this FSM.
	    _currentIterFireCount++;
	} else {
	    throw new IllegalActionException(this,
		     "Can't fire because current refinement is null.");
	}
	// Code above formerly in fire().


	// Check if the current iteration of the HDF/SDF graph in
	// which this FSM refines has completed yet. The
	// iteration is complete iff the current refinement has
	// been fired the number of times specified by the current
	// static schedule of the HDF/SDF graph in which this FSM
	// refines.
	
	// Get the HDF/SDF schedule.
	CompositeActor container = (CompositeActor)getContainer();
	if (_debugging) _debug("Name of HDF composite acotr: " +
			       ((Nameable)container).getName());
	String hdfCompositeActName = ((Nameable)container).getName();
	// Get the SDF Director.
	SDFDirector sdfDir = (SDFDirector)(container.getExecutiveDirector());
	// Get the SDF Director's scheduler.
	Scheduler s = sdfDir.getScheduler();
	if (s == null)
	    throw new IllegalActionException("Attempted to postfire " +
					     "FSM system with no SDF scheduler");
	Enumeration allactors = s.schedule();
	if (_debugging) _debug("HDFFSMDirector:  postfire(): just got sdf schedule.");
	
	int hdfCompositeActOccurrence = 0;
	
	while (allactors.hasMoreElements()) {
	    Actor actor = (Actor)allactors.nextElement();
	    String schedActName = ((Nameable)actor).getName();
	    
	    if (schedActName.equals(hdfCompositeActName)) {
		// Current actor in the static schedule is
		// the HDF compisite actor containing this FSM.
		
		// Increment the occurence count of this actor.
		hdfCompositeActOccurrence++;
	    }
	    
	    if (_debugging) _debug("Actor in static schedule: " +
				   ((Nameable)actor).getName());
	}
	if (_debugging) _debug("Actors in static schedule: **** " +
			       hdfCompositeActOccurrence);
	if (_debugging) _debug("current fire count: **** " +
			       _currentIterFireCount);
	boolean okToMakeTransition;
	// Check if the fire() has been called the number of
	// times specified in the static schedule.
	if (_currentIterFireCount == hdfCompositeActOccurrence) {
	    // The current refinement has been fired the number
	    // of times speified by the current static schedule.
	    // A state transition can now occur.
	    okToMakeTransition = true;
	    // Set firing count back to zero for next iteration.
	    _currentIterFireCount = 0;
	} else {
	    okToMakeTransition = false;
	}
	
	Transition lastChosenTr = ctrl._getLastChosenTransition();

	if (okToMakeTransition) {

	    if (lastChosenTr  == null) {
		// Do not make a state transition (remain in the current
		// state).
		
		// No transition is enabled when last fire. FSMController does not
		// change state. Note this is different from when a transition
		// back to the current state is taken.
		if (_debugging) _debug("Making a state transition back " +
				       "to the current state. ");
	    } else {
		// Make a state transition (possibly back to the current
		// state).
		
		// The HDF/SDF graph in which this FSM is embedded has
		// just finished one iteration, so change state. Note
		// that this transition could simply
		// be a transition back to the current state.

		if (_debugging) _debug("Making a state transition. ");

		

		// Update the current refinement to point to the destination
		// state of the (enabled) transition.
		// FIXME: 
		State newState = lastChosenTr.destinationState();
		//curState = lastChosenTr.destinationState();
	        ctrl.currentStateSet(newState);
		if (_debugging) _debug("HDFFSMDirector: postfire(): making state transition to: " + newState.getFullName());
		curState = newState;
		// Since a state change has occured, recompute the
		// Mapping from input ports of the modal model to
		// their corresponding receivers of the new current
		// state and the mode controller.
		ctrl._setCurrentConnectionMap();

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
		if (_debugging) _debug("HDFFSDirector: invalidating " +
				       "current schedule.");
		s.setValid(false);
	    }
	
	}	

	if (_debugging) _debug("HDFFSMDirector:  postfire(): returning now.");
        return true;
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
        _buildLocalReceiverMaps();

	// Now propagate the type signature of the current refinment (the
	// refinement of the initial state) out to the corresponding ports
	// of the container of this director.
		// Update port consumption/production rates and invalidate
	// current SDF schedule.
	// FIXME: Make a method that performs code below, since same
	// code invoked from postfire. (to avoid code duplication)

	FSMActor ctrl = getController();
	// Attempt to get the initial state from the mode controller.
	State initialState = ctrl.getInitialState();

	// Get the current refinement.
	TypedCompositeActor curRefinement =
	    (TypedCompositeActor)initialState.getRefinement();
	if (curRefinement != null) {
	    SDFDirector refinementDir = (SDFDirector)curRefinement.getDirector();
	    Scheduler refinmentSched = refinementDir.getScheduler();
	    refinmentSched.setValid(false);
	    refinmentSched.schedule();
	    if (_debugging) _debug("HDFFSDirector: preinitialize(): refinement's director : " + refinementDir.getFullName());
	    ////////////////// end temp1

	    // Get the HDF/SDF schedule.
	    CompositeActor container = (CompositeActor)getContainer();
	    if (_debugging) _debug("HDFFSDirector: preinitialize(): Name of HDF composite acotr: " +
				   ((Nameable)container).getName());
	    // Get the (toplevel) SDF Director.
	    SDFDirector sdfDir = (SDFDirector)(container.getExecutiveDirector());
	    // Get the SDF Director's scheduler.
	    Scheduler s = sdfDir.getScheduler();
	    //s.setValid(false);
	    //s.schedule();
	    // Update the rate information of the HDF actor containing
	    // the current refinement.
	    _updateInputTokenConsumptionRates(curRefinement);
	    _updateOutputTokenProductionRates(curRefinement);
	    // Tell the scheduler that the current schedule is no
	    // longer valid.
	    if (_debugging) _debug("HDFFSDirector: preinitialize(): invalidating " +
				   "current schedule.");
	    s.setValid(false);
	} else {
	    throw new IllegalActionException(this,
			   "current refinement is null.");
	}
    }

   /** Set the number of guard tokens allowed (largest m in dataIn$m)
     *  for each port. This number is common to all port. The default
     *  value is 1.
     *  <p>
     *  As an example, suppose that the the opaque composite actor
     *  containing the FSM model has an input port named "dataIn."
     *  Then, in order to reference tokens read in the "dataIn"
     *  port in the state transition guard expression, the guard
     *  variable(s) associated with port "dataIn"  must be used.
     *  These guard variables have the names dataIn$0, dataIn$1,
     *  dataIn$2, .... Here, dataIn$0 references the token most
     *  recently read in port "dataIn", dataIn$1 references the
     *  next most recently read token, and so on. 
     *
     * @param history The number of tokens in the guard history.
     */
    // FIXME: Either implement this, or get rid of it.
    // FIXME: The token sytax above is inconsitant with the currently
    // implemented syntax.
    public void  setGuardTokenHistory(int histSize) {
	 throw new InternalErrorException(this.getName() +
                  " : setGuardTokenHistory(): Sorry. This" +
		  " method is not yet implemented. Only history" +
                  " size = 1 is currently allowed.");      
    }

    /** Return true if data are transferred from the input port of
     *  the container to the ports connected to the inside of the input
     *  port and on the mode controller or the refinement of its current
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
	if(_debugging) _debug("HDFFSMDirector: " + getFullName() +
			      " : transferInputs() invoked");
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferInputs: port argument is not an opaque" +
                    "input port.");
        }
        boolean trans = false;
	// This receivers of the current refinement that receive data
	// from "port."
        Receiver[][] insiderecs = _currentLocalReceivers(port);
	// For each channel.
        for (int i = 0; i < port.getWidth(); i++) {
	    int rate = SDFScheduler.getTokenConsumptionRate(port);
	    if(_debugging) _debug("HDFFSMDirector: " + getFullName() +
				  ": transferInputs(): Rate of port: " +
				  port.getFullName() + " is " + rate);
	    for(int k = 0; k < rate; k++) {
		try {
                    ptolemy.data.Token t = port.get(i);
                    if (insiderecs != null && insiderecs[i] != null) {
                        if(_debugging) _debug("HDFFSMDirector: " + getFullName() +
                                ": transfering input from port: " + port.getFullName() + "---");
                        for (int j = 0; j < insiderecs[i].length; j++) {
                            insiderecs[i][j].put(t);
			    // begin debug:
			    //Receiver rec1 = insiderecs[i][j];
			    //IOPort rec1Container = rec1.getContainer();
			    //if(_debugging) _debug("HDFFSMDirector: " + getFullName() +
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
	if(_debugging) _debug("HDFFSMDirector: transferOutputs() invoked on port: "
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
		    if(_debugging) _debug("HDFFSMDirector: transferOutputs(): insiderecs[0].length: " +
				       insiderecs[0].length);
                    for (int j = 0; j < insiderecs[i].length; j++) {
			while (insiderecs[i][j].hasToken()) {
                            try {
                                ptolemy.data.Token t = insiderecs[i][j].get();
				if(_debugging) _debug("HDFFSMDirector: " +
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

    protected void _setTokenConsumptionRate(Entity e, IOPort port, int rate)
            throws NotSchedulableException {
        if(rate <= 0) throw new NotSchedulableException(
                "Rate must be > 0");
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
        if(rate <= 0) throw new NotSchedulableException(
                "Rate must be > 0");
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
	if (_debugging) _debug("HDFFSMDirector: " +
	   "_updateInputTokenConsumptionRates() invoked on actor: " +
           actor.getFullName());

	// Get all of its input ports.
	Enumeration refineInPorts = actor.inputPorts();
	// Get the current refinement's container.
	ComponentEntity refineInPortContainer =
	    (ComponentEntity) actor.getContainer();
	
	while (refineInPorts.hasMoreElements()) {
	    IOPort refineInPort =
		(IOPort)refineInPorts.nextElement();
	    if (_debugging) _debug("Current port of refining actor " +
				   refineInPort.getFullName());
	    
	    
	    // Get all of the input ports this port is
	    // linked to on the outside (should only consist
	    // of 1 port).
	    Enumeration inPortsOutside = 
		refineInPort.deepConnectedInPorts();
	    if (!inPortsOutside.hasMoreElements()) {
		throw new IllegalActionException("Current " +
			  "state's refining actor has an input " +
			  "port not connected to an input port " +
			                        "of its container.");
	    }
	    while (inPortsOutside.hasMoreElements()) {
		IOPort inputPortOutside =
		    (IOPort)inPortsOutside.nextElement();
		if (_debugging) _debug("Current outisde port connected " +
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
		    if (_debugging) _debug("Updating consumption " +
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

		    if (_debugging) _debug("Port: " + refineInPort.getFullName() + " rate = " + _getTokenConsumptionRate(refineInPort));
		    //if (_debugging) _debug("Port: " + refineInPort.getFullName() + " rate = " + ((SDFIOPort)refineInPort).getTokenConsumptionRate());
		    if (_debugging) _debug("New consumption rate is " +
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
	Enumeration refineOutPorts = actor.outputPorts();
	// Get the current refinement's container.
	ComponentEntity refineOutPortContainer =
	    (ComponentEntity) actor.getContainer();
	
	while (refineOutPorts.hasMoreElements()) {
	    IOPort refineOutPort =
		(IOPort)refineOutPorts.nextElement();
	    if (_debugging) _debug("Current port of refining actor " +
				   refineOutPort.getFullName());
	    
	    
	    // Get all of the output ports this port is
	    // linked to on the outside (should only consist
	    // of 1 port).
	    Enumeration outPortsOutside = 
		refineOutPort.deepConnectedOutPorts();
	    if (!outPortsOutside.hasMoreElements()) {
		throw new IllegalActionException("Current " +
			  "state's refining actor has an output " +
			  "port not connected to an output port " +
			                        "of its container.");
	    }
	    while (outPortsOutside.hasMoreElements()) {
		IOPort outputPortOutside =
		    (IOPort)outPortsOutside.nextElement();
		if (_debugging) _debug("Current outisde port connected " +
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
		    if (_debugging) _debug("Updating production " +
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
		    if (_debugging) _debug("New consumption rate is " +
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

    /** The number of times fire() has been called in the current
     * iteration of the SDF graph containing this FSM.
     */
    private int _currentIterFireCount;

}
