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

   @ProposedRating Red (zhouye@eecs.berkeley.edu)
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
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.FSMDirector;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.domains.sdf.kernel.SDFScheduler;
import ptolemy.domains.sdf.kernel.SDFUtilities;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
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
   inside HDF, SDF inside FSM inside HDF, and SDF inside FSM inside SDF.
   This class must be used as the director of an FSM when the FSM refines
   an HDF or SDF composite actor, unless all the ports rates are always 1.
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
   in the current global iteration of the current HDF schedule. A state
   transition will occur if the guard expression evaluates to true
   after a "Type B firing."
   <p>
   <b>References</b>
   <p>
   <OL>
   <LI>
   A. Girault, B. Lee, and E. A. Lee,
   ``<A HREF="http://ptolemy.eecs.berkeley.edu/papers/98/starcharts">
   Hierarchical Finite State Machines with Multiple Concurrency Models</A>,
   '' April 13, 1998.</LI>
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
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *  with the specified container.
     *  @exception NameDuplicationException If the container is not a
     *  CompositeActor and the name collides with an entity in the container.
     */
    public HDFFSMDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the values of input variables in the mode controller.
     *  If the refinement of the current state of the mode controller
     *  is ready to fire, then fire the current refinement.
     *  @exception IllegalActionException If there is no controller.
     */
    public void fire() throws IllegalActionException {
        CompositeActor container = (CompositeActor)getContainer();
        FSMActor ctrl = getController();
        ctrl.setFiringsSoFar(_firingsSoFar);
        //ctrl.setFiringsPerIteration(_firingsPerIteration);
        _setInputVariables();
        State st = ctrl.currentState();
        // FIXME
        //Actor ref = ctrl.currentState().getRefinement();
        Actor[] ref = ctrl.currentState().getRefinement();
        _fireRefinement = false;
        if (ref != null) {
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
        if (_fireRefinement) {
            // Fire the refinement.
            // FIXME
            //ref.fire();
            ref[0].fire();
            //ref[0].postfire();
            _setInputsFromRefinement();
        }
        if (_firingsSoFar == _firingsPerIteration - 1) {
            _chooseTransition(st.nonpreemptiveTransitionList());
        }
        ChangeRequest request =
            new ChangeRequest(this, "choose transition") {
            protected void _execute() throws KernelException {
                
            }
        };
        request.setPersistent(false);
        container.requestChange(request);
        return;
    }

    /** If this method is called immediately after preinitialze(),
     *  initialize the mode controller and all the refinements.
     *  If this is a reinitialization, it typically means this
     *  is a sub-layer HDFFSMDirector and a "reset" has been called
     *  at the upper-level HDFFSMDirector. This method will then
     *  reinitialize all the refinements in the sub-layer, recompute
     *  the schedule of the initial state in the sub-layer, and notify
     *  update of port rates to the upper level director.
     *  @exception IllegalActionException If the initialize() method of
     *  one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
        if (!_reinitialize){
            super.initialize();
            _reinitialize = true;
        } else {
            // This is a sub-layer HDFFSMDirector.
            // Reinitialize all the refinements in the sub-layer
            // HDFFSMDirector and recompute the schedule.
            super.initialize();
            _firingsSoFar = 0;
            FSMActor controller = getController();
            State initialState = controller.getInitialState();
            TypedCompositeActor curRefinement =
                // FIXME
                //(TypedCompositeActor)initialState.getRefinement();
                (TypedCompositeActor)(initialState.getRefinement())[0];
            if (curRefinement != null) {
                Director refinementDir = curRefinement.getDirector();
                if (refinementDir instanceof HDFFSMDirector) {
                    refinementDir.initialize();
                } else if (refinementDir instanceof HDFDirector) {
                    Scheduler refinmentSched = ((StaticSchedulingDirector)
                            refinementDir).getScheduler();
                    refinmentSched.setValid(false);
                    //refinmentSched.getSchedule();
                    ((HDFDirector)refinementDir).getSchedule();
                } else if (refinementDir instanceof SDFDirector) {
                    Scheduler refinmentSched = ((StaticSchedulingDirector)
                            refinementDir).getScheduler();
                    refinmentSched.setValid(true);
                    ((SDFScheduler)refinmentSched).getSchedule();
                } else {
                    // Invalid director.
                    throw new IllegalActionException(this,
                            "Only HDF, SDF, or HDFFSM director is " +
                            "allowed in the refinement");
                }
                _updateInputTokenConsumptionRates(curRefinement);
                _updateOutputTokenProductionRates(curRefinement);
                // Tell the upper level scheduler that the current schedule
                // is no longer valid.
                CompositeActor hdfActor = _getHighestFSM();
                Director director = hdfActor.getExecutiveDirector();
                ((StaticSchedulingDirector)director).invalidateSchedule();
            } else {
                throw new IllegalActionException(this,
                        "current refinement is null.");
            }
        }
    }

    /** 
     * Return the change context being made explicit.  In this case,
     * the change context returned is the context in which HDF makes
     * state transitions.
     */
    public Entity getContext() {
        return (Entity)toplevel();  // FIXME!
    }

    /** Return a list of variables that are modified in a modal model.
     * The variables are assumed to have a change context of the
     * container of this director.  This class returns all variables
     * that are assigned in the actions of transitions, and all rate
     * variables of the container.
     * @return A list of variables.
     */
    public List getModifiedVariables() throws IllegalActionException {
        List list = super.getModifiedVariables();

        if(!_allRefinementsHaveSameRate()) {
            CompositeActor container = (CompositeActor)getContainer();
            for(Iterator ports = container.inputPortList().iterator();
                ports.hasNext();) {
                IOPort port = (IOPort)ports.next();
                Parameter param = (Parameter)
                    SDFUtilities._getRateVariable(port, "tokenConsumptionRate");
                list.add(param);
            }
            for(Iterator ports = container.outputPortList().iterator();
                ports.hasNext();) {
                IOPort port = (IOPort)ports.next();
                Parameter param = (Parameter)
                    SDFUtilities._getRateVariable(port, "tokenProductionRate");
                list.add(param);
            }
        }
        return list;
    }
    
    /** Return a new receiver of a type compatible with this director.
     *  This returns an instance of SDFReceiver.
     *  @return A new SDFReceiver.
     */
    public Receiver newReceiver() {
        return new SDFReceiver();
    }

    /** If a type B firing has not occurred (i.e., one global iteration
     *  has not finished), return true if the postfire of the current
     *  refinement returns true.
     *  <p>
     *  Otherwise, if a type B firing has occured, a transition is
     *  allowed to occur. Set up new state and connection map if exactly
     *  one transition is enabled. Get the schedule of the current
     *  refinement and propagate its port rates to the outside.
     *  The upper-level director will be notified of the new connection
     *  and new port rates from the refinement. Return true if the
     *  super class method returns true.
     *  @return True if a type B firings has occured and the super
     *  class method returns true; or if a type B firing has not occured
     *  and the postfire of current refinement returns true.
     *  @exception IllegalActionException If a refinement throws it,
     *  if there is no controller, or if an inconsistency in port
     *  rates is detected between refinement actors.
     */
    public boolean postfire() throws IllegalActionException {
        FSMActor ctrl = getController();
        State curState = ctrl.currentState();
        CompositeActor container = (CompositeActor)getContainer();
        // FIXME
        //TypedActor currentRefinement =
        TypedActor[] currentRefinement = curState.getRefinement();
        if (currentRefinement == null) {
            throw new IllegalActionException(this,
                    "Can't postfire because current refinement is null.");
        }

        // FIXME
        //boolean postfireReturn = currentRefinement.postfire();
        boolean postfireReturn = currentRefinement[0].postfire();
        boolean superPostfire;
        _firingsSoFar++;

        ChangeRequest request =
            new ChangeRequest(this, "commit transition") {
            protected void _execute() throws KernelException {   
            }
        };
        request.setPersistent(false);
        //container.requestChange(request);
        container.requestChange(request);
        if (_firingsSoFar == _firingsPerIteration) {
            // One global iteration has finished.
            // A state transition can now occur.
            // Set firing count back to zero for next iteration.
            _firingsSoFar = 0;
            Transition lastChosenTr = _getLastChosenTransition();
            TypedCompositeActor actor;
            Director refinementDir;
            if (lastChosenTr  == null) {
                // No transition enabled. Remain in the current state.
                // FIXME
                //actor = (TypedCompositeActor)curState.getRefinement();
                actor = (TypedCompositeActor)(curState.getRefinement())[0];
                refinementDir = actor.getDirector();
                superPostfire = super.postfire();
            } else {
                // Make a state transition.
                State newState = lastChosenTr.destinationState();
                _setCurrentState(newState);

                superPostfire = super.postfire();
                curState = newState;

                // Get the new current refinement actor.
                // FIXME
                // actor = (TypedCompositeActor)curState.getRefinement();
                actor = (TypedCompositeActor)(curState.getRefinement())[0];
                refinementDir = actor.getDirector();
                if (refinementDir instanceof HDFFSMDirector) {
                    refinementDir.postfire();
                } else if (refinementDir instanceof HDFDirector) {
                    Scheduler refinmentSched = ((StaticSchedulingDirector)
                            refinementDir).getScheduler();
                    refinmentSched.setValid(false);
                    //refinmentSched.getSchedule();
                    ((HDFDirector)refinementDir).getSchedule();
                } else if (refinementDir instanceof SDFDirector) {
                    Scheduler refinmentSched = ((StaticSchedulingDirector)
                            refinementDir).getScheduler();
                    refinmentSched.setValid(true);
                    ((SDFScheduler)refinmentSched).getSchedule();
                } else {
                    throw new IllegalActionException(this,
                            "Only HDF, SDF, or HDFFSM director is "
                            + "allowed in the refinement.");
                }
            }
            // Even when the finite state machine remains in the
            // current state, the schedule may change. This occurs
            // in cases of multi-level HDFFSM model. The sup-mode
            // remains the same but the sub-mode has changed.
            _updateInputTokenConsumptionRates(actor);
            _updateOutputTokenProductionRates(actor);

            CompositeActor hdfActor = _getHighestFSM();
            Director director = hdfActor.getExecutiveDirector();
            if (director instanceof HDFDirector) {
                ((HDFDirector)director).invalidateSchedule();
            }
            //return super.postfire();
            return superPostfire;
        }

        return postfireReturn;
    }

    /** Return true if the mode controller is ready to fire.
     *  @exception IllegalActionException If there is no controller.
     */
    public boolean prefire() throws IllegalActionException {
        return getController().prefire();
    }

    /** Preinitialize() methods of all actors deeply contained by the
     *  container of this director. The HDF/SDF preinitialize method
     *  will compute the initial shedule. Propagate the consumption
     *  and production rates of the current state out to the
     *  corresponding ports of the container of this director.
     *  @exception IllegalActionException If the preinitialize()
     *  method of one of the associated actors throws it, or there
     *  is no controller.
     */
    public void preinitialize() throws IllegalActionException {
        _firingsPerIteration = 1;
        _firingsSoFar = 0;
        _reinitialize = false;
        super.preinitialize();

        FSMActor ctrl = getController();
        State initialState = ctrl.getInitialState();
        _setCurrentState(initialState);
        // Get the current refinement.
        TypedCompositeActor curRefinement =
            // FIXME
            //(TypedCompositeActor)initialState.getRefinement();
            (TypedCompositeActor)(initialState.getRefinement())[0];
        if (curRefinement != null) {
            Director refinementDir = curRefinement.getDirector();

            if (!(refinementDir instanceof HDFFSMDirector)
                    && !(refinementDir instanceof SDFDirector)) {
                // Invalid director.
                throw new IllegalActionException(this,
                        "Only HDF, SDF, or HDFFSM director is "
                        + "allowed in the refinement.");
            }

            _updateInputTokenConsumptionRates(curRefinement);
            _updateOutputTokenProductionRates(curRefinement);

        } else {
            throw new IllegalActionException(this,
                    "current refinement is null.");
        }
    }

    /** Set the number of firings per iteration of this director.
     *  @param firings Number of firings per iteration of this director.
     */
    public void setFiringsPerIteration(int firings) {
        _firingsPerIteration = firings;
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
     *  @param port The input port to transfer tokens from.
     *  @return True if data are transferred.
     *  @exception IllegalActionException If the port is not an opaque
     *  input port.
     */
    public boolean transferInputs(IOPort port)
            throws IllegalActionException {
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
            int rate = SDFUtilities.getTokenConsumptionRate(port);
            for (int k = 0; k < rate; k++) {
                try {
                    ptolemy.data.Token t = port.get(i);
                    if (insideReceivers != null && insideReceivers[i] != null) {
                        for (int j = 0; j < insideReceivers[i].length; j++) {
                            insideReceivers[i][j].put(t);
                        }
                        // Successfully transferred data, so return true.
                        transferred = true;
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(
                            "Director.transferInputs: Internal error: " + ex);
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
     *  @exception IllegalActionException If the port is not an opaque
     *  output port.
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     */
    public boolean transferOutputs(IOPort port)
            throws IllegalActionException {

        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "HDFFSMDirector: transferOutputs():" +
                    "  port argument is not an opaque output port.");
        }
        boolean trans = false;
        Receiver[][] insideReceivers = port.getInsideReceivers();
        if (insideReceivers != null) {
            for (int i = 0; i < insideReceivers.length; i++) {
                // "i" is port index.
                // "j" is that port's channel index.
                if (insideReceivers[i] != null) {
                    for (int j = 0; j < insideReceivers[i].length; j++) {
                        while (insideReceivers[i][j].hasToken()) {
                            try {
                                ptolemy.data.Token t =
                                    insideReceivers[i][j].get();
                                port.send(i, t);
                                trans = true;
                            } catch (NoTokenException ex) {
                                throw new InternalErrorException(
                                        "Director.transferOutputs: " +
                                        "Internal error: " + ex);
                            }
                        }
                    }
                }
            }
        }
        return trans;
    }

    /** Update the number of firings per global iteration of
     *  each actor in the current refinment.
     *  @param directorFiringsPerIteration Number of firings per global
     *  iteration of the current director. It is also the number of
     *  firings per global iteration of the current refinement.
     *  @exception IllegalActionException If no controller or current
     *  refinement can be found, or if the HDFDirector
     *  updateFiringsPerIteration method throws it.
     */
    public void updateFiringsPerIteration (int directorFiringsPerIteration)
            throws IllegalActionException {
        FSMActor ctrl = getController();
        State currentState;

        TypedCompositeActor currentRefinement;
        
        currentState = ctrl.currentState();
        currentRefinement =
            // FIXME
            //(TypedCompositeActor)currentState.getRefinement();
        (TypedCompositeActor)(currentState.getRefinement())[0];
        if (currentRefinement != null) {
            Director refinementDir = currentRefinement.getDirector();
            if (refinementDir instanceof HDFFSMDirector) {
                ((HDFFSMDirector)refinementDir).updateFiringsPerIteration(
                        directorFiringsPerIteration);
            } else if (refinementDir instanceof HDFDirector) {
                ((HDFDirector)refinementDir).
                    setDirectorFiringsPerIteration(
                            directorFiringsPerIteration);
                ((HDFDirector)refinementDir).updateFiringsPerIteration(
                        directorFiringsPerIteration);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return true if every state of the finite state machine has a
     * refinement and every refinement has ports with the same name
     * and direction and each port of each refinement with the same
     * name has the same rate parameter.
     */
    private boolean _allRefinementsHaveSameRate() 
            throws IllegalActionException {
        CompositeActor container = (CompositeActor)getContainer();
        FSMActor controller = getController();
        for(Iterator ports = container.portList().iterator();
            ports.hasNext();) {
            IOPort port = (IOPort)ports.next();
            String name = port.getName();
            int previousTokenProductionRate = 0;
            int previousTokenConsumptionRate = 0;
            boolean firstTime = true;
            for(Iterator states = controller.entityList().iterator();
                states.hasNext();) {
                State state = (State)states.next();
                CompositeActor refinement = 
                    (CompositeActor)state.getRefinement()[0];
                IOPort refinementPort = (IOPort)refinement.getPort(name);
                int tokenProductionRate = 0;
                int tokenConsumptionRate = 0;
                if(refinementPort == null) {
                    // Check directionality
                    if(port.isInput() != refinementPort.isInput()) {
                        return false;
                    }
                    if(port.isOutput() != refinementPort.isOutput()) {
                        return false;
                    }
                    tokenConsumptionRate = 
                        SDFUtilities._getRateVariableValue(
                                refinementPort, "tokenConsumptionRate", 0);
                    tokenProductionRate = 
                        SDFUtilities._getRateVariableValue(
                                refinementPort, "tokenProductionRate", 0);
                }
                if(previousTokenConsumptionRate != tokenConsumptionRate) {
                    return false;
                }
                if(previousTokenProductionRate != tokenProductionRate) {
                    return false;
                }
            }                    
        }
        return true;
    }
       

    /** If the container of this director does not have an
     *  HDFFSMDirector as its executive director, then return it.
     *  Otherwise, move up the hierarchy until we reach a container
     *  actor that does not have an HDFFSMDirector director for its
     *  executive director.
     *  @exception IllegalActionException If the top-level director
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

    /** Extract the token consumption rates from the input
     *  ports of the current refinement and update the
     *  rates of the input ports of the HDF opaque composite actor
     *  containing the refinment. The resulting mutation will cause
     *  the SDF scheduler to compute a new schedule using the
     *  updated rate information.
     *  @param actor The current refinement.
     */
    private void _updateInputTokenConsumptionRates(
            TypedCompositeActor actor) throws IllegalActionException {

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
            SDFUtilities.setTokenConsumptionRate(
                    containerPort, 0);
        }
        // Get all of its input ports of the current refinement actor.
        Iterator refineInPorts = actor.inputPortList().iterator();
        while (refineInPorts.hasNext()) {
            IOPort refineInPort =
                (IOPort)refineInPorts.next();
            // Get all of the input ports this port is
            // linked to on the outside (should only consist
            // of 1 port).
            // Iterator inPorts = inputPortList().iterator();
            // while (inPorts.hasNext()) {
            //     TypedIOPort inPort = (TypedIOPort)inPorts.next();
            Iterator inPortsOutside =
                refineInPort.deepConnectedInPortList().iterator();
            if (!inPortsOutside.hasNext()) {
                throw new IllegalActionException("Current " +
                        "state's refining actor has an input port not" +
                        "connected to an input port of its container.");
            }
            while (inPortsOutside.hasNext()) {
                IOPort inputPortOutside =
                    (IOPort)inPortsOutside.next();
                // Check if the current port is contained by the
                // container of the current refinement.
                ComponentEntity thisPortContainer =
                    (ComponentEntity)inputPortOutside.getContainer();
                String temp = refineInPortContainer.getFullName()
                    + "._Controller";
                if (thisPortContainer.getFullName() ==
                        refineInPortContainer.getFullName() ||
                        temp.equals(thisPortContainer.getFullName())) {
                    // set the outside port rate equal to the port rate
                    // of the refinement.
                    int portRateToSet = SDFUtilities
                        .getTokenConsumptionRate(refineInPort);
                    SDFUtilities.setTokenConsumptionRate
                        (inputPortOutside, portRateToSet);
                }
            }
        }
    }

    /** Extract the token production rates from the output
     *  ports of the current refinement and update the
     *  production and initial production rates of the output
     *  ports of the HDF opaque composite actor
     *  containing the refinment. The resulting mutation will cause
     *  the SDF scheduler to compute a new schedule using the
     *  updated rate information.
     *  @param actor The current refinement.
     */
    private void _updateOutputTokenProductionRates(
            TypedCompositeActor actor) throws IllegalActionException {

        // Get the current refinement's container.
        CompositeActor refineOutPortContainer =
            (CompositeActor) actor.getContainer();
        // Get all of the output ports of the container of this director.
        List containerPortList = refineOutPortContainer.outputPortList();
        // Set all of the external port rates to zero.
        Iterator containerPorts = containerPortList.iterator();
        while (containerPorts.hasNext()) {
            IOPort containerPort = (IOPort)containerPorts.next();
            SDFUtilities.setTokenProductionRate(
                    containerPort, 0);
        }
        // Get all of the current refinement's output ports.
        Iterator refineOutPorts = actor.outputPortList().iterator();
        while (refineOutPorts.hasNext()) {
            IOPort refineOutPort =
                (IOPort)refineOutPorts.next();
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
                // Check if the current port is contained by the
                // container of the current refinment.
                ComponentEntity thisPortContainer =
                    (ComponentEntity)outputPortOutside.getContainer();
                String temp = refineOutPortContainer.getFullName()
                    + "._Controller";
                if (thisPortContainer.getFullName() ==
                        refineOutPortContainer.getFullName()) {
                    // set the outside port rate equal to the port rate
                    // of the refinement.
                    int portRateToSet = SDFUtilities
                        .getTokenProductionRate(refineOutPort);
                    int portInitRateToSet = SDFUtilities
                        .getTokenInitProduction(refineOutPort);
                    SDFUtilities.setTokenProductionRate
                        (outputPortOutside, portRateToSet);
                    SDFUtilities.setTokenInitProduction
                        (outputPortOutside, portInitRateToSet);
                } else if (temp.equals(thisPortContainer.getFullName())) {
                    // set the outside port rate equal to the port rate of
                    // the refinement.
                    int portRateToSet = SDFUtilities
                        .getTokenProductionRate(refineOutPort);
                    SDFUtilities.setTokenConsumptionRate
                        (outputPortOutside, portRateToSet);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The number of times fire() has been called in the global
    // iteration of the SDF graph containing this FSM.
    private int _firingsSoFar;

    // The number of firings of this HDFFSM controller per
    // global iteration.
    private int _firingsPerIteration = 1;

    // A flag indicating whether the initialize method is
    // called due to reinitialization.
    private boolean _reinitialize;
}
