/* An SCController is an FSM controller.

 Copyright (c) 1997-1998 The Regents of the University of California.
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

*/

package ptolemy.domains.sc.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.actor.util.*;
import ptolemy.data.*;
import ptolemy.automata.util.*;
import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// SCController
/**
An SCController is an FSM controller. 

FIXME: Clean handling of the refinement of the state left.

@authors Xiaojun Liu
@version $Id$
*/
public class SCController extends CompositeEntity implements Actor {


    public SCController() {
        super();
    }


    public SCController(Workspace workspace) {
        super(workspace);
    }


    public SCController(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }


    public void addLocalVariable(String name, Token initialValue) 
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


    public Object clone(Workspace ws) throws CloneNotSupportedException {
        // FIXME
        // This method needs careful refinement.
        SCController newobj = (SCController)super.clone();
        if (_initialState != null) {
            newobj._initialState = 
                    (SCState)newobj.getEntity(_initialState.getName());
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
        if (_initTrans != null) {
            newobj._initTrans = new LinkedList();
            Enumeration trans = _initTrans.elements();
            while (trans.hasMoreElements()) {
                SCTransition tr = (SCTransition)trans.nextElement();
                newobj._initTrans.insertLast(newobj.getRelation(tr.getName()));
            }
        }
        newobj._currentState = null;
        newobj._takenTransition = null;
        if (_localVariables != null) {
            newobj._localVariables = (VariableList)newobj.getAttribute("LocalVariables");
        }
        // From AtomicActor.
        newobj._inputPortsVersion = -1;
        newobj._outputPortsVersion = -1;
        return newobj;
    }


    /** Create receivers for each input port.
     *  @exception IllegalActionException If any port throws it.
     */
    public void createReceivers() throws IllegalActionException {
        Enumeration inports = inputPorts();
        while (inports.hasMoreElements()) {
            IOPort inport = (IOPort)inports.nextElement();
            inport.createReceivers();
        }
    }


    public SCTransition createTransition(SCState source, SCState dest) {
        return source.createTransitionTo(dest);
    }


    public SCState currentState() {
        return _currentState;
    }


    public Actor currentRefinement() {
        if (_currentState != null) {
            return _currentState.getRefinement();
        } else {
            return null;
        }
    }


// When an SCController fires, its behavior is the parallel composition of
// its own sequential controll logic and its current refinement. 
// Preemptive transitions of controll logic take precedence of refinement.
// Question: what to do to the refinement when a transition is taken?
    public void fire() throws IllegalActionException {
        _takenTransition = null;
        _setInputVars();
        // Evaluate the preemptive transitions.
        SCTransition trans;
        Enumeration preTrans = _currentState.getPreemptiveTrans();
        while (preTrans.hasMoreElements()) {
            trans = (SCTransition)preTrans.nextElement();
            if (trans.isEnabled()) {
                if (_takenTransition != null) {
                    // Nondeterminate transition!
                    System.out.println("Nondeterminate transition!");
                } else {
                    _takenTransition = trans;
                }
            }
        }

        if (_takenTransition == null) {
            // The local input of an SCState is the output of its refinement.
            _currentState.resetLocalInputStatus();
   
            // Invoke refinement.
            // Delegate to the executive director.
            // FIXME!
            if (currentRefinement() != null) {
                currentRefinement().fire();
            }

            // Evaluate the nonpreemptive transitions.
            Enumeration nonPreTrans = _currentState.getNonPreemptiveTrans();
            while (nonPreTrans.hasMoreElements()) {
                trans = (SCTransition)nonPreTrans.nextElement();
                if (trans.isEnabled()) {
                    if (_takenTransition != null) {
                        // Nondeterminate transition!
                        System.out.println("Nondeterminate transition!");
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
            // do not change state, that's done in postfire()
        }

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


    public void initialize() throws IllegalActionException {
        try {
            setupScope();
        } catch (NameDuplicationException ex) {
            // FIXME!!
            // ignore for now
        }
        // Set local variables to their initial value.
        if (_localVariables != null) {
            Enumeration localVars = _localVariables.getVariables();
            Variable var = null;
            while (localVars.hasMoreElements()) {
                var = (Variable)localVars.nextElement();
                var.reset();
            }
        }
        // Evaluate initial transitions, determine initial state.
        _setInputVars();
        _takenTransition = null;
        if (_initTrans != null) { 
            Enumeration trs = _initTrans.elements();
            SCTransition trans;
            while (trs.hasMoreElements()) {
                trans = (SCTransition)trs.nextElement();
                if (trans.isEnabled()) {
                    if (_takenTransition != null) {
                        // Nondeterminate initial transition!
                        System.out.println("Multiple initial transitions "
                                + "enabled!");
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
            System.out.println("Initialization error: no initial state!");
        }
        if (currentRefinement() != null) {
            // FIXME!!
            // Initialize the refinement.
            // Delegate to the director or just call initialize() on
            // the refinement?
            // Now we are doing initialization in SC system.
        }
    }


    /** Return an enumeration of the input ports.
     *  This method is read-synchronized on the workspace.
     *  @return An enumeration of IOPort objects.
     */
    public Enumeration inputPorts() {
        try {
            workspace().getReadAccess();
            if(_inputPortsVersion != workspace().getVersion()) {
                // Update the cache.
                LinkedList inports = new LinkedList();
                Enumeration ports = getPorts();
                while(ports.hasMoreElements()) {
                    IOPort p = (IOPort)ports.nextElement();
                    if( p.isInput()) {
                        inports.insertLast(p);
                    }
                }
                _cachedInputPorts = inports;
                _inputPortsVersion = workspace().getVersion();
            }
            return _cachedInputPorts.elements();
        } finally {
            workspace().doneReading();
        }
    }


    // SCController is a standalone sequential logic controller.
    public boolean isOpaque() {
        return true;
    }


    /** Create a new IOPort with the specified name.
     *  The container of the port is set to this actor.
     *  This method is write-synchronized on the workspace.
     *
     *  @param name The name for the new port.
     *  @return The new port.
     *  @exception NameDuplicationException If the actor already has a port
     *   with the specified name.
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


    // newRelation
    // Should return an SCTransition?


    /** Return a new receiver of a type compatible with the director.
     *  Derived classes may further specialize this to return a reciever
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


    /** Return an enumeration of the output ports.
     *  This method is read-synchronized on the workspace.
     *  @return An enumeration of IOPort objects.
     */
    public Enumeration outputPorts() {
        try {
            workspace().getReadAccess();
            if(_outputPortsVersion != workspace().getVersion()) {
                _cachedOutputPorts = new LinkedList();
                Enumeration ports = getPorts();
                while(ports.hasMoreElements()) {
                    IOPort p = (IOPort)ports.nextElement();
                    if( p.isOutput()) {
                        _cachedOutputPorts.insertLast(p);
                    }
                }
                _outputPortsVersion = workspace().getVersion();
            }
            return _cachedOutputPorts.elements();
        } finally {
            workspace().doneReading();
        }
    }


    public boolean prefire() {
        // Anything useful to do?
        return true;
    }


    /** Change state according to the enabled transition determined 
     *  from last fire.
     *  @return True, the execution can continue into the next iteration.
     *  @exception IllegalActionException If the refinement of the state
     *   transitioned into cannot be initialized.
     */
    public boolean postfire() throws IllegalActionException {
        if (_takenTransition == null) {
            // No transition is enabled when last fire. SCController does not
            // change state. Note this is different from when a transition
            // back to the current state is taken.
            return true;
        }

// What to do to the refinement of the state left?

        _currentState = _takenTransition.destinationState();

        // execute the transition actions
        _takenTransition.executeTransitionActions();

        if (_takenTransition.isInitEntry() || _currentState.isInitEntry()) {   
            // Initialize the refinement.
            Actor actor = currentRefinement();
            if (actor == null) {
                return true;
            }
// If the refinement is an SCController or an SC system, then the trigger 
// actions of the taken transition should be input to the actor to enable
// initial transitions.
// ADD THIS!
//            if (actor instanceof SCController) {
//                // Do what's needed.
//            } else {
//                // Do what's needed.
//            }
            // FIXME!
            actor.initialize();
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
                    "SCController can only be contained by instances of " +
                    "CompositeActor.");
        }
        super.setContainer(container);
    }        


    public void setInitialState(SCState initialState) 
            throws IllegalActionException {
        if (initialState != null && initialState.getContainer() != this) {
            throw new IllegalActionException(this, initialState,
                    "Initial state is not contained by SCController.");
        }
        _initialState = initialState;
    }


    public void setInitialTransition(SCTransition initialTransition)
            throws IllegalActionException {
        if (initialTransition != null && initialTransition.getContainer() != this) {
            throw new IllegalActionException(this, initialTransition,
                    "Initial transition is not contained by SCController.");
        }
        if (_initTrans == null) {
            _initTrans = new LinkedList();
            _initTrans.insertFirst(initialTransition);
        } else if (_initTrans.includes(initialTransition)) {
            return;
        } else {
            _initTrans.insertFirst(initialTransition);
        }
    }            


    public void setupScope() 
            throws NameDuplicationException {
        try {
            // remove old variable lists
            if (_inputStatusVars != null) {
                _inputStatusVars.setContainer(null);
                _inputValueVars.setContainer(null);
            }
            // create new variable lists
            _inputStatusVars = new VariableList(this, INPUT_STATUS_VAR_LIST);
            _inputValueVars = new VariableList(this, INPUT_VALUE_VAR_LIST);
            _inputStatusVars.createVariables(inputPorts());
            _inputValueVars.createVariables(inputPorts());
        } catch (IllegalActionException ex) {
        } catch (NameDuplicationException ex) {
        }
        Enumeration states = getEntities();
        SCState state;
        while (states.hasMoreElements()) {
            state = (SCState)states.nextElement();
            state.setupScope();
        }
        Enumeration transitions = getRelations();
        SCTransition trans;
        while (transitions.hasMoreElements()) {
            trans = (SCTransition)transitions.nextElement();
            trans.setupScope();
        }
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
     *  operations to be performed excatly once at the end of a complete
     *  execution of an application.  It typically closes
     *  files, displays final results, etc.
     *
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void wrapup() throws IllegalActionException {
    }


    /** Add a state to this controller with minimal error checking.
     *  This overrides the base-class method to make sure the argument
     *  is an instance of SCState.
     *  It is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @param entity State to add.
     *  @exception IllegalActionException If the actor has no name, or the
     *   action would result in a recursive containment structure, or the
     *   argument is not an instance of SCState.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the states list.
     */
    protected void _addEntity(ComponentEntity entity)
            throws IllegalActionException, NameDuplicationException {
        if (!(entity instanceof SCState)) {
            throw new IllegalActionException(this, entity,
                    "SCController can only contain entities that are instances"
                    + " of SCState");
        }
        // SCState is not an Actor
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
     *   not an instance of SCTransition.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the contained relations list.
     */
    protected void _addRelation(ComponentRelation relation)
            throws IllegalActionException, NameDuplicationException {
        if (!(relation instanceof SCTransition)) {
            throw new IllegalActionException(this, relation,
                    "SCController can only contain instances of SCTransition.");
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
            var.evaluate();
            // If this controller is a refinement, set the corresponding
            // state's local input variable.
            // This cannot be done in the SCDirector.transferOutput() because
            // there is no inside receiver at SCController's output ports.
            // FIXME!!
            if (getDirector() instanceof SCDirector) {
                SCDirector dir = (SCDirector)getDirector();
                if (dir.currentRefinement() == this) {

SCState state = dir.currentState();
System.out.println("SCController " + this.getFullName() + " setting " +
        "local variable " + var.getName() + " of state " + state.getFullName());

                    dir.currentState().setLocalInputVar(var.getName(), var.getToken());
                }
            }            
            port = (IOPort)getPort(var.getName());
            if (port == null || !port.isOutput()) {
                continue;
            }
            if (port.numLinks() > 0) {

System.out.println("Sending trigger action to "+port.getFullName());

                port.send(0, var.getToken());
            } else {

                // This branch will not be executed, because now all ports
                // in SC system are connected.

System.out.println("Port " + port.getFullName() + " is floating.");

                // Here we must be careful, port does not have inside
                // receivers created!
                port = (IOPort)((CompositeActor)getContainer()).getPort(port.getName());
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
        Enumeration inports = inputPorts();
        IOPort port;
        while (inports.hasMoreElements()) { 
            port = (IOPort)inports.nextElement();
            if (port.numLinks() > 0) {
                if (port.hasToken(0)) {

System.out.println("Port " + port.getFullName() + " has token.");

                    _inputStatusVars.setVarValue(port.getName(), PRESENT);
                    _inputValueVars.setVarValue(port.getName(), port.get(0));
                } else {

System.out.println("Port " + port.getFullName() + " has no token.");

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

   
    protected synchronized String _uniqueStateName() {
        return super._uniqueEntityName();
    }


    protected synchronized String _uniqueTransitionName() {
        return super._uniqueRelationName();
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
            var.evaluate();
            _localVariables.setVarValue(var.getName(), var.getToken());
        }
    } 


    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    // The name of the local variable list.
    public static final String LOCAL_VARIABLE_LIST = "LocalVariables";

    /** The name of the input status variable list.
     */
    public static final String INPUT_STATUS_VAR_LIST = "InputStatusVars";

    /** The name of the input value variable list.
     */
    public static final String INPUT_VALUE_VAR_LIST = "InputValueVars";

    public static final BooleanToken PRESENT = new BooleanToken(true);
    public static final BooleanToken ABSENT = new BooleanToken(false);


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The initial state.
    SCState _initialState = null;

    // The list of initial transitions.
    LinkedList _initTrans = null;

    // The current state.
    SCState _currentState = null;

    // The input status variable list.
    VariableList _inputStatusVars = null;

    // The input value variable list.
    VariableList _inputValueVars = null;

    // The local variable list.
    VariableList _localVariables = null;

    // The transition to be taken when change state.
    SCTransition _takenTransition = null;

    // From AtomicActor, should keep consistent.
    // Cached lists of input and output ports.
    private transient long _inputPortsVersion = -1;
    private transient LinkedList _cachedInputPorts;
    private transient long _outputPortsVersion = -1;
    private transient LinkedList _cachedOutputPorts;


}    








