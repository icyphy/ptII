/* An HDFFSMController is an actor that contains the FSM states and
   transitions for an FSM that refines  a heterochronous
   dataflow actor.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.actor.sched.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.graph.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.domains.fsm.*;
import ptolemy.domains.fsm.kernel.*;
import ptolemy.domains.fsm.kernel.util.VariableList;
import ptolemy.domains.sdf.kernel.*;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// HDFFSMController
/**
An HDFFSMController should be used instead of an FSMController when the
FSM refines a heterochronous dataflow (HDF) actor. The HDFFSMController
contains the FSM model (i.e., the states and transitions). The states
are instances of HDFFSMState and and transitions are instances of
HDFFSMTransition. It is required that each state (HDFFSMState) refine
to another FSM or to an SDF or HDF diagram. It is important to note that
the refining diagram is not placed inside an HDFFSMState, but is
instead placed inside an opaque composite actor with the same
container as the HDFFSMController. Since the state (HDFFSMState) and
its refinement are distinct actors, an association between them must
be created. This is accomplished by calling the setRefinement()
method of each instance of HDFFSMState. Note also that neither the
HDFFSMController actor nor the HDFFSMState actors should contain any
ports. It is only necessary that each of the refining opaque
composite actors contain the same number and type of ports (with
the same names) as its container, and that each of the refining
actor's ports be linked to the corresponding port of its container.

@version $Id$
@author Brian K. Vogel
*/
//FIXME: This controller does too much work. The director should handle
//most of this work.
//FIXME: Why is this controller even needed at all?
//FIXME: Why have two distinct actors (and HDFFSMState and its refinement)
//to represent each state?
public class HDFFSMController  extends FSMController implements TypedActor {



    public HDFFSMController() {
        super();
    }


    public HDFFSMController(Workspace workspace) {
        super(workspace);
    }


    public HDFFSMController(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /**
     */
    public List typeConstraintList()  {
	try {
	    workspace().getReadAccess();

	    List result = new LinkedList();
	    Enumeration inPorts = inputPorts();
	    while (inPorts.hasMoreElements()) {
	        TypedIOPort inport = (TypedIOPort)inPorts.nextElement();
		boolean isUndeclared = inport.getTypeTerm().isSettable();
		if (isUndeclared) {
		    Enumeration outPorts = outputPorts();
	    	    while (outPorts.hasMoreElements()) {
		    	TypedIOPort outport =
                            (TypedIOPort)outPorts.nextElement();

			isUndeclared = outport.getTypeTerm().isSettable();
		    	if (isUndeclared && inport != outport) {
			    // output also undeclared, not bi-directional port,
		            Inequality ineq = new Inequality(
                                    inport.getTypeTerm(),
                                    outport.getTypeTerm());
			    result.add(ineq);
			}
		    }
		}
	    }
	    return result;

	}finally {
	    workspace().doneReading();
	}
    }

    // FIXME: is ptolemy.data.Token the right token?
    public void addLocalVariable(String name, ptolemy.data.Token initialValue)
            throws IllegalActionException, NameDuplicationException {
        if (_localVariables == null) {
            _localVariables = new VariableList(this, LOCAL_VARIABLE_LIST);
        }
        Variable var = new Variable(_localVariables, name);
        try {
            // var's type is determined by initialValue's type.
            var.setToken(initialValue);
        } catch (IllegalArgumentException ex) {
            // this should not happen
        }
    }

    public FSMTransition createTransition(HDFFSMState source,
            HDFFSMState dest) {
        return (FSMTransition)source.createTransitionTo(dest);
    }

    /** Initialize the controller. Initialize the number of
     *  times fire() has been called in the current iteration
     *  of the current HDF/SDF schedule of the graph containing
     *  this FSM to 0.
     *  FIXME: This code should be in the director.
     */
    public void initialize() throws IllegalActionException {
	// Initialize the current firing count in the current
	// iteration of the current static schedule to 0.
	_currentIterFireCount = 0;


    }

    public Object clone(Workspace ws) throws CloneNotSupportedException {
        // FIXME
        // This method needs careful refinement.
        HDFFSMController newobj = (HDFFSMController)super.clone();
        if (_initialState != null) {
            newobj._initialState =
                (FSMState)newobj.getEntity(_initialState.getName());
        }
        newobj._inputStatusVars = null;
        newobj._inputValueVars = null;
        try {
            VariableList vlist =
                (VariableList)newobj.getAttribute(INPUT_STATUS_VAR_LIST);
            if (vlist != null) {
                vlist.setContainer(null);
            }
            vlist = (VariableList)newobj.getAttribute(INPUT_VALUE_VAR_LIST);
            if (vlist != null) {
                vlist.setContainer(null);
            }
        } catch (IllegalActionException ex) {
            // this should not happen
        } catch (NameDuplicationException ex) {
            // this should not happen
        }
        if (_initialTransitions != null) {
            newobj._initialTransitions = new LinkedList();
            Iterator trans = _initialTransitions.iterator();
            while (trans.hasNext()) {
                FSMTransition tr = (FSMTransition)trans.next();
                newobj._initialTransitions.add(
                        newobj.getRelation(tr.getName()));
            }
        }
        newobj._currentState = null;
        newobj._takenTransition = null;
        if (_localVariables != null) {
            newobj._localVariables =
                (VariableList)newobj.getAttribute("LocalVariables");
        }
        // From AtomicActor.
        newobj._inputPortsVersion = -1;
        newobj._outputPortsVersion = -1;
        return newobj;
    }



    /** Return the current state of the FSM. The returned state is
     *  an instance of FSMState.
     */
    public FSMState currentState() {
        return _currentState;
    }


    /** Return the refining actor associated with the current state.
     *  If there is no current state (something is broken) then
     *  return null.
     *  FIXME: throw an exception instead of returning null.
     */
    public Actor currentRefinement() {
        if (_currentState != null) {
            return _currentState.getRefinement();
        } else {
            return null;
        }
    }

    /** See fire() comment of the director.
     *
     */
    public void fire() throws IllegalActionException {
	if (_debugging) _debug("FSMController: fire()");

        _takenTransition = null;

        FSMTransition trans;

	// Fire the refinement.
	if (currentRefinement() != null) {
	    if (_debugging) _debug("FSMController:  fire(): firing " +
                    "current refinment");
	    currentRefinement().fire();
	    // Increment current firing count. This is the number of
	    // times the current refining HDF actor has been fired
	    // in the current iteration of the current static schedule
	    // of the SDF/HDF graph containing this FSM.
	    _currentIterFireCount++;
	} else {
	    throw new IllegalActionException(this,
                    "Can't fire because current refinement is null.");
	}

	// FIXME: move this code to postfire().
	// Evaluate the transitions.
	Enumeration nonPreTrans = _currentState.getNonPreemptiveTrans();
	while (nonPreTrans.hasMoreElements()) {
	    trans = (FSMTransition)nonPreTrans.nextElement();
	    if (_debugging)
                _debug("FSMController:  fire(): transistion name: "
                        + trans.getFullName());
	    if (trans.isEnabled()) {
		if (_debugging) _debug("FSMController:  fire():" +
                        " transition enabled");
		if (_takenTransition != null) {
		    // Nondeterminate transition!
		    //System.out.println("Nondeterminate transition!");
		} else {
		    _takenTransition = trans;
		}
	    }
	}

	if (_debugging) _debug("FSMController:  fire(): finished");
    }



    /** Return the director responsible for the execution of this actor.
     *  In this class, this is always the executive director.
     *  Return null if either there is no container or the container has no
     *  director.
     *  @return The director that invokes this actor.
     */
    public Director getDirector() {
        CompositeActor container = (CompositeActor)getContainer();
        if (container != null) {
            return container.getDirector();
        }
        return null;
    }


    /** Return the executive director (same as getDirector()).
     *  @return The executive director.
     */
    public Director getExecutiveDirector() {
        return getDirector();
    }

    /** Return the Manager responsible for execution of this actor,
     *  if there is one. Otherwise, return null.
     *  @return The manager.
     */
    public Manager getManager() {
        try {
            workspace().getReadAccess();
            CompositeActor container = (CompositeActor)getContainer();
            if (container != null) {
                return container.getManager();
            }
            return null;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return a list of the input ports contained by the
     *  container of this controller.
     *  This method is read-synchronized on the workspace.
     *  @return A list of IOPort objects.
     */
    public List inputPortList() {
        try {
            workspace().getReadAccess();
            if(_inputPortsVersion != workspace().getVersion()) {
                // Update the cache.
                List inports = new LinkedList();
                //Enumeration ports = getPorts();
		// Is this right?
		//Enumeration ports = ((CompositeEntity)getContainer()).getPorts();
		Iterator ports = ((CompositeEntity)getContainer()).portList().iterator();
                while(ports.hasNext()) {
                    IOPort p = (IOPort)ports.next();
                    if( p.isInput()) {
                        inports.add(p);
                    }
                }
                _cachedInputPorts = inports;
                _inputPortsVersion = workspace().getVersion();
            }
            return _cachedInputPorts;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return an enumeration of the input ports contained by the
     *  container of this controller.
     *  This method is read-synchronized on the workspace.
     *  @deprecated Use inputPortList() instead.
     *  @return An enumeration of IOPort objects.
     */
    public Enumeration inputPorts() {
        return Collections.enumeration(inputPortList());
    }


    // FSMController is a standalone sequential logic controller.
    public boolean isOpaque() {
        return true;
    }

    /* FIXME: Throw exception since this actor should not contain
     * any ports.
     */
    public Port newPort(String name) throws NameDuplicationException {
        try {
            workspace().getWriteAccess();
            IOPort port = new IOPort(this, name);
            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(
                    "AtomicActor.newPort: Internal error: " + ex.getMessage());
        } finally {
            workspace().doneWriting();
        }
    }



    /** Return a new receiver of a type compatible with the director.
     *  Derived classes may further specialize this to return a receiver
     *  specialized to the particular actor.
     *
     *  @exception IllegalActionException If there is no director.
     *  @return A new object implementing the Receiver interface.
     */
    public Receiver newReceiver() throws IllegalActionException {
        Director dir = getDirector();
        if (dir == null) {
            throw new IllegalActionException(this,
                    "Cannot create a receiver without a director.");
        }
        return dir.newReceiver();
    }


    /** Return a list of the output ports.
     *  This method is read-synchronized on the workspace.
     *  @return A list of IOPort objects.
     */
    public List outputPortList() {
        try {
            workspace().getReadAccess();
            if(_outputPortsVersion != workspace().getVersion()) {
                _cachedOutputPorts = new LinkedList();
                Iterator ports = portList().iterator();
                while(ports.hasNext()) {
                    IOPort p = (IOPort)ports.next();
                    if( p.isOutput()) {
                        _cachedOutputPorts.add(p);
                    }
                }
                _outputPortsVersion = workspace().getVersion();
            }
            return _cachedOutputPorts;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return an enumeration of the output ports.
     *  This method is read-synchronized on the workspace.
     *  @deprecated Use outputPortList() instead.
     *  @return An enumeration of IOPort objects.
     */
    public Enumeration outputPorts() {
        return Collections.enumeration(outputPortList());
    }


    public boolean prefire() {
        // Anything useful to do?
        return true;
    }


    /** Determine the initial state. Progagate the
     *  consumption/production rates of the current refinement's
     *  HDF diagram out to the HDF actor and invalidate the
     *  current schedule of the diagram in which the HDF actor
     *  is contained. Set up the scope of variables allowed to be
     *  used in transition guard expressions.
     *
     */
    public void preinitialize() throws IllegalActionException {
        try {
            _createReceivers();
	    // Set up the scope of variables allowed to be used
	    // in transition guard expressions.
            _setupScope();
        } catch (NameDuplicationException ex) {
            // FIXME!!
            // ignore for now

            throw new InvalidStateException(this, "DEAL WITH IT" +
                    ex.getMessage());

        }

        // Evaluate initial transitions, determine initial state.
        _setInputVars();
        _takenTransition = null;
        if (_initialTransitions != null) {
            Iterator trs = _initialTransitions.iterator();
            FSMTransition trans;
            while (trs.hasNext()) {
                trans = (FSMTransition)trs.next();
                if (trans.isEnabled()) {
                    if (_takenTransition != null) {
                        // Nondeterminate initial transition!
                        //System.out.println("Multiple initial transitions "
                        //        + "enabled!");
                    } else {
                        _takenTransition = trans;
                    }
                }
            }
        }
        if (_takenTransition != null) {
            _outputTriggerActions(_takenTransition.getTriggerActions());
            _updateLocalVariables(_takenTransition.getLocalVariableUpdates());
            // _takenTransition.executeTransitionActions();
            _currentState = _takenTransition.destinationState();
        } else {
            _currentState = _initialState;
        }
        if (_currentState == null) {
            // FIXME!!
            // Throw exception!
            //System.out.println("Initialization error: no initial state!");
        }



	// Get the current refinement.
	TypedCompositeActor curRefinement =
	    (TypedCompositeActor)currentRefinement();
	if (curRefinement != null) {
	    // Get the HDF/SDF schedule.
	    CompositeActor container = (CompositeActor)getContainer();
	    if (_debugging) _debug("Name of HDF composite acotr: " +
                    ((Nameable)container).getName());
	    // Get the SDF Director.
	    SDFDirector sdfDir = (SDFDirector)(container.getExecutiveDirector());
	    // Get the SDF Director's scheduler.
	    Scheduler s = sdfDir.getScheduler();
	    // Update the rate information of the HDF actor containing
	    // the current refinement.
	    _updateInputTokenConsumptionRates(curRefinement);
	    _updateOutputTokenProductionRates(curRefinement);
	    // Tell the scheduler that the current schedule is no
	    // longer valid.
	    if (_debugging) _debug("HDFFSMController: invalidating " +
                    "current schedule.");
	    s.setValid(false);
	} else {
	    throw new IllegalActionException(this,
                    "current refinement is null.");
	}

    }

    /** Change state according to the enabled transition determined
     *  from last fire.
     *  FIXME: Move this code into the local director's postfire().
     *  @return True, the execution can continue into the next iteration.
     *  @exception IllegalActionException If the refinement of the state
     *   transitioned into cannot be initialized.
     */
    public boolean postfire() throws IllegalActionException {
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

	if (okToMakeTransition) {

	    if (_takenTransition == null) {
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
		_currentState = _takenTransition.destinationState();

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
		    (TypedCompositeActor)currentRefinement();
		// Extract the token consumption/production rates from the
		// ports of the current refinement and update the
		// rates of the ports of the HDF actor containing
		// the refinment.
		// FIXME: queue this as mutation request with dir/manager?
		_updateInputTokenConsumptionRates(actor);
		_updateOutputTokenProductionRates(actor);
		// Tell the scheduler that the current schedule is no
		// longer valid.
		if (_debugging) _debug("HDFFSMController: invalidating " +
                        "current schedule.");
		s.setValid(false);
	    }

	}

	return true;
    }


    /** Override the base class to ensure that the proposed container
     *  is an instance of CompositeActor or null. If it is, call the
     *  base class setContainer() method. A null argument will remove
     *  the actor from its container.
     *
     *  @param entity The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace, or
     *   if the argument is not a CompositeActor or null.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the name of this entity.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (!(container instanceof CompositeActor) && (container != null)) {
            throw new IllegalActionException(container, this,
                    "FSMController can only be contained by instances of " +
                    "CompositeActor.");
        }
        super.setContainer(container);
    }


    /** Set the initial state for the FSM. This method must be
     *  called exactly once to set the starting state.
     *  @param state The initial state, an instance of FSMState.
     *  @exception IllegalActionException If this method is called more
     *  than once.
     */
    public void setInitialState(FSMState initialState)
            throws IllegalActionException {
        if (initialState != null && initialState.getContainer() != this) {
            throw new IllegalActionException(this, initialState,
                    "Initial state is not contained by FSMController.");
        }
        _initialState = initialState;
    }


    public void setInitialTransition(FSMTransition initialTransition)
            throws IllegalActionException {
        if (initialTransition != null &&
                initialTransition.getContainer() != this) {
            throw new IllegalActionException(this, initialTransition,
                    "Initial transition is not contained by FSMController.");
        }
        if (_initialTransitions == null) {
            _initialTransitions = new LinkedList();
            _initialTransitions.add(0, initialTransition);
        } else if (_initialTransitions.contains(initialTransition)) {
            return;
        } else {
            _initialTransitions.add(0, initialTransition);
        }
    }



    /** FIXME: This should do something like calling stopfire on the contained
     *  actors.
     */
    public void stopFire() {
    }


    /** By default, an AtomicActor does nothing incredible in its
     *  terminate, it just wraps up.
     */
    public void terminate() {
        try {
            wrapup();
        }
        catch (IllegalActionException e) {
            // Do not pass go, do not collect $200.  Most importantly,
            // just ignore everything and terminate.
        }
    }


    /** Do nothing.  Derived classes override this method to define
     *  operations to be performed exactly once at the end of a complete
     *  execution of an application.  It typically closes
     *  files, displays final results, etc.
     *
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void wrapup() throws IllegalActionException {
    }


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add a state to this controller with minimal error checking.
     *  This overrides the base-class method to make sure the argument
     *  is an instance of FSMState.
     *  It is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @param entity State to add.
     *  @exception IllegalActionException If the actor has no name, or the
     *   action would result in a recursive containment structure, or the
     *   argument is not an instance of FSMState.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the states list.
     */
    protected void _addEntity(ComponentEntity entity)
            throws IllegalActionException, NameDuplicationException {
        if (!(entity instanceof FSMState)) {
            throw new IllegalActionException(this, entity,
                    "FSMController can only contain entities that are " +
                    "instances of FSMState");
        }
        // FSMState is not an Actor
        super._addEntity(entity);
    }


    /** Override the base class to throw an exception if the added port
     *  is not an instance of IOPort.  This method should not be used
     *  directly.  Call the setContainer() method of the port instead.
     *  This method does not set the container of the port to point to
     *  this entity. It assumes that the port is in the same workspace
     *  as this entity, but does not check.  The caller should check.
     *  Derived classes may override this method to further constrain to
     *  a subclass of IOPort. This method is <i>not</i> synchronized on
     *  the workspace, so the caller should be.
     *
     *  @param port The port to add to this entity.
     *  @exception IllegalActionException If the port class is not
     *   acceptable to this entity, or the port has no name.
     *  @exception NameDuplicationException If the port name coincides with a
     *   name already in the entity.
     */
    protected void _addPort(Port port)
            throws IllegalActionException, NameDuplicationException {
        if (!(port instanceof IOPort)) {
            throw new IllegalActionException(this, port,
                    "Incompatible port class for this entity.");
        }
        super._addPort(port);
    }


    /** Add a transition to this controller. This method should not be used
     *  directly.  Call the setContainer() method of the transition instead.
     *  This method does not set the container of the transition to refer
     *  to this container. This method is <i>not</i> synchronized on the
     *  workspace, so the caller should be.
     *
     *  @param relation Relation to contain.
     *  @exception IllegalActionException If the relation has no name, or is
     *   not an instance of FSMTransition.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the contained relations list.
     */
    protected void _addRelation(ComponentRelation relation)
            throws IllegalActionException, NameDuplicationException {
        if (!(relation instanceof FSMTransition)) {
            throw new IllegalActionException(this, relation,
                    "FSMController can only contain instances " +
                    "of FSMTransition.");
        }
        super._addRelation(relation);
    }


    protected void _outputTriggerActions(VariableList vlist)
            throws IllegalActionException, NoRoomException {
        if (vlist == null) {
            return;
        }
        Enumeration vars = vlist.getVariables();
        Variable var;
        IOPort port;
        while (vars.hasMoreElements()) {
            var = (Variable)vars.nextElement();
            var.getToken();
            // If this controller is a refinement, set the corresponding
            // state's local input variable.
            // This cannot be done in the FSMDirector.transferOutput() because
            // there is no inside receiver at FSMController's output ports.
            // FIXME!!
            if (getDirector() instanceof FSMDirector) {
                FSMDirector dir = (FSMDirector)getDirector();
                if (dir.currentRefinement() == this) {

                    FSMState state = dir.currentState();
                    //System.out.println("FSMController " +
                    //this.getFullName() + " setting " +
                    //        "local variable " + var.getName() +
                    //":" + var.getExpression() + " of state "
                    //+ state.getFullName());

                    dir.currentState().setLocalInputVar(var.getName(),
                            var.getToken());
                }
            }
            port = (IOPort)getPort(var.getName());
            if (port == null || !port.isOutput()) {
                continue;
            }
            if (port.numLinks() > 0) {

                //System.out.println("Sending trigger action to "+
                //port.getFullName());

                port.send(0, var.getToken());
            } else {

                // This branch will not be executed, because now all ports
                // in FSM system are connected.

                //System.out.println("Port " + port.getFullName()
                //+ " is floating.");

                // Here we must be careful, port does not have inside
                // receivers created!
                port = (IOPort)((CompositeActor)getContainer()).getPort(
                        port.getName());
                if (port == null || !port.isOutput()) {
                    continue;
                }
                Receiver[][] recs = port.getInsideReceivers();
                if (recs != null) {
                    recs[0][0].put(var.getToken());
                }
            }
        }
    }

    protected void _setInputVars()
            throws IllegalArgumentException, IllegalActionException {
        // Do not deal with multiport, multiple tokens now.
	// Get an enumeration of all the input ports contained by the
	// container of this controller.
        Enumeration inports = inputPorts();
	//Enumeration inports = ((CompositeEntity)getContainer()).inputPorts();
        IOPort port;
        while (inports.hasMoreElements()) {
            port = (IOPort)inports.nextElement();
            if (port.numLinks() > 0) {
                if (port.hasToken(0)) {

                    if (_debugging) _debug("HDFSMController: " +
                            "_setInputVars(): Port " +
                            port.getFullName() + " has token.");

                    _inputStatusVars.setVarValue(port.getName(), PRESENT);
                    _inputValueVars.setVarValue(port.getName(), port.get(0));
                } else {

                    if (_debugging) _debug("HDFSMController: " +
                            "_setInputVars(): Port " +
                            port.getFullName() + " has no token.");

                    _inputStatusVars.setVarValue(port.getName(), ABSENT);
                }
            } else {
                // FIXME!!
                // Outside receivers are always created, so this branch
                // is not necessary.
                Receiver[][] recs = port.getReceivers();
                if (recs != null) {
                    Receiver rec = recs[0][0];
                    if (rec.hasToken()) {
                        _inputStatusVars.setVarValue(port.getName(), PRESENT);
                        _inputValueVars.setVarValue(port.getName(), rec.get());
                    } else {
                        _inputStatusVars.setVarValue(port.getName(), ABSENT);
                    }
                } else {
                    // Port desired width is not set, same as no input.
                    _inputStatusVars.setVarValue(port.getName(), ABSENT);
                }
            }
        }
    }

    protected void _updateLocalVariables(VariableList vlist)
            throws IllegalActionException, IllegalArgumentException {
        if (vlist == null) {
            return;
        }
        Enumeration vars = vlist.getVariables();
        Variable var;
        while (vars.hasMoreElements()) {
            var = (Variable)vars.nextElement();
            var.getToken();
            _localVariables.setVarValue(var.getName(), var.getToken());
        }
    }


    /** Get the number of tokens that are produced or consumed
     *  on the designated port of this Actor, as supplied by
     *  by the port's "TokenConsumptionRate" Parameter.   If the parameter
     *  does not exist, then assume the actor is homogeneous and return a
     *  rate of 1.
     *  @exception IllegalActionException If the TokenConsumptionRate
     *   parameter has an invalid expression.
     */
    protected int _getTokenConsumptionRate(IOPort p)
            throws IllegalActionException {
        Parameter param = (Parameter)p.getAttribute("TokenConsumptionRate");
        if(param == null) {
            if(p.isInput())
                return 1;
            else
                return 0;
        } else
            return ((IntToken)param.getToken()).intValue();
    }


    /** Get the number of tokens that are produced or consumed
     *  on the designated port of this Actor during each firing,
     *  as supplied by
     *  by the port's "TokenProductionRate" Parameter.   If the parameter
     *  does not exist, then assume the actor is homogeneous and return a
     *  rate of 1.
     *  @exception IllegalActionException If the TokenProductionRate
     *   parameter has an invalid expression.
     */
    protected int _getTokenProductionRate(IOPort p)
            throws IllegalActionException {
        Parameter param = (Parameter)p.getAttribute("TokenProductionRate");
        if(param == null) {
            if(p.isOutput())
                return 1;
            else
                return 0;
        }
        return ((IntToken)param.getToken()).intValue();
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
            port.getAttribute("TokenConsumptionRate");
        try {
            if(param != null) {
                param.setToken(new IntToken(rate));
            } else {
                param = new Parameter(port,"TokenConsumptionRate",
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
            port.getAttribute("TokenProductionRate");
        try {
            if(param != null) {
                param.setToken(new IntToken(rate));
            } else {
                param = new Parameter(port,"TokenProductionRate",
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
    protected void _updateInputTokenConsumptionRates(TypedCompositeActor actor)
            throws IllegalActionException {
	// Get all of its input ports.
	Enumeration refineInPorts =
            Collections.enumeration(actor.inputPortList());
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
		Collections.enumeration(
                        refineInPort.deepConnectedInPortList());
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
		if (thisPortContainer.getFullName() ==
                        refineInPortContainer.getFullName()) {
		    // The current port  is contained by the
		    // container of the current refinment.
		    // Update its consumption rate.
		    if (_debugging) _debug("Updating consumption " +
                            "rate of port: " +
                            inputPortOutside.getFullName());
		    // Get the port to which "refineInPort" is
		    // connected on the inside.
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
			    _getTokenConsumptionRate(portWithRateInfo);
		    }
		    if (_debugging)
                        _debug("New consumption rate is " + refineInPortRate);
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
	Enumeration refineOutPorts =
            Collections.enumeration(actor.outputPortList());
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
		Collections.enumeration(
                        refineOutPort.deepConnectedOutPortList());
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
		if (thisPortContainer.getFullName() ==
                        refineOutPortContainer.getFullName()) {
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
		    if (_debugging)
                        _debug("New consumption rate is " + refineOutPortRate);
				// FIXME: call requestChange in Manager
                    //for this?
                    _setTokenProductionRate(refineOutPortContainer,
                            outputPortOutside,
                            refineOutPortRate);
		}
	    }
	}
    }

    ////////////////////////////////////////////////////////////
    /////                  private methods             /////////

    /* Add all ports contained by this controller and all ports contained
     * by the container of this controller to the scope.
     */
    private void _setupScope()
            throws NameDuplicationException, IllegalActionException {


        try {
            // remove old variable lists
            if (_inputStatusVars != null) {
                _inputStatusVars.setContainer(null);
                _inputValueVars.setContainer(null);
            }
            // create new variable lists
            _inputStatusVars = new VariableList(this, INPUT_STATUS_VAR_LIST);
            _inputValueVars = new VariableList(this, INPUT_VALUE_VAR_LIST);
	    // Add all the input ports of this Controller to the scope.
            _inputStatusVars.createVariables(inputPorts());
	    // Add all the input ports of this Controller to the scope.
            _inputValueVars.createVariables(inputPorts());
	    // Add all the input ports of the Controller's container
	    // to the scope.
	    _inputStatusVars.createVariables(
                    Collections.enumeration(
                            ((CompositeEntity)getContainer()).portList()));
	    _inputValueVars.createVariables(
                    Collections.enumeration(
                            ((CompositeEntity)getContainer()).portList()));

        } catch (IllegalActionException ex) {
        } catch (NameDuplicationException ex) {
        }

        Enumeration states = Collections.enumeration(entityList());
        FSMState state;
	// Setup the scope associated with each state. The scope
	// associated with a state consists of all of its refining
	// actor's input and output ports.
        while (states.hasMoreElements()) {
            state = (FSMState)states.nextElement();
            state.setupScope();
        }
        Enumeration transitions = Collections.enumeration(relationList());
        FSMTransition trans;
	// Setup the scope associated with each transition. The scope
	// associated with a transition consists of the union of the
	// scope of its source state and the scope of its controller.
        while (transitions.hasMoreElements()) {
            trans = (FSMTransition)transitions.nextElement();
            trans.setupScope();
        }
    }

    /** Create receivers for each input port.
     *  @exception IllegalActionException If any port throws it.
     */
    private void _createReceivers() throws IllegalActionException {
        Enumeration inports = inputPorts();
        while (inports.hasMoreElements()) {
            IOPort inport = (IOPort)inports.nextElement();
            inport.createReceivers();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The number of times fire() has been called in the current
    // iteration of the SDF graph containing this FSM.
    private int _currentIterFireCount;


}

