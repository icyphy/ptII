/* A HDFFSMDirector governs the execution of the finite state
   machine in heterochronous dataflow model.

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
package ptolemy.domains.hdf.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.MultirateFSMDirector;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;


//////////////////////////////////////////////////////////////////////////
//// HDFFSMDirector

/**
   This director extends MultirateFSMDirector by supporting production and
   consumption of multiple tokens on a port in a firing and by restricting
   that state transitions could only occur on each global iteration.
   
   FIXME: Refactor into two directors, with the base class
   MultirateFSMDirector supporting multirate.
   FIXME: fix all the comments of the method.

   An HDFFSMDirector governs the execution of a finite state machine
   (FSM) in a heterochronous dataflow (HDF) or synchronous dataflow
   (SDF) model according to the *charts [1] semantics. *charts is a
   family of models of computation that specifies an operational
   semantics for composing hierarchical FSMs with various concurrency
   models.
   <p>
   This director is used with a modal model that consumes and produces
   any number of tokens on its ports. The number of tokens consumed
   and produced is determined by the refinement of the current state.
   FIXME: Figure out what it actually does and what it should do.

   The subset of *charts that this class supports is HDF inside FSM
   inside HDF, SDF inside FSM inside HDF, and SDF inside FSM inside SDF.
   This class must be used as the director of an FSM or ModalModel
   when the FSM refines
   an HDF or SDF composite actor, unless all the ports rates are always 1,
   in which case, the base class FSMDirector can be used. This director
   can also be used in an FSM or ModalModel in DDF.
   <p>
   This director assumes that every state has exactly one refinement, with one
   exception. A state may have no refinement if upon being entered, it has
   an outgoing transition with a guard that is true. This will be treated as a
   "transient state," in that the FSM will progress through that state
   to the next state until it encounters a state with a refinement.
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

   @author Rachel Zhou and Brian K. Vogel
   @version $Id$
   @Pt.ProposedRating Red (zhouye)
   @Pt.AcceptedRating Red (cxh)
   @see HDFDirector
*/
public class HDFFSMDirector extends MultirateFSMDirector {
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
     *  Choose a transition if this FSM is embedded in SDF, otherwise
     *  request to choose a transition to the manager.
     *  @exception IllegalActionException If there is no controller.
     */
    public void fire() throws IllegalActionException {
        CompositeActor container = (CompositeActor) getContainer();
        FSMActor controller = getController();
        controller.setNewIteration(_sendRequest);
        _readInputs();

        Transition transition;
        State currentState = controller.currentState();

        //_lastIntransientState = currentState;
        Actor[] actors = currentState.getRefinement();

        // NOTE: Paranoid coding.
        if ((actors == null) || (actors.length != 1)) {
            throw new IllegalActionException(this,
                    "Current state is required to have exactly one refinement: "
                    + currentState.getName());
        }

        for (int i = 0; i < actors.length; ++i) {
            if (_stopRequested) {
                break;
            }

            if (actors[i].prefire()) {
                actors[i].fire();
                //_refinementPostfire = actors[i].postfire();
                _refinementPostfire = actors[i].postfire();
            }
        }

        _readOutputsFromRefinement();

        if (_sendRequest) {
            ChangeRequest request = new ChangeRequest(this,
                    "choose a transition") {
                    protected void _execute()
                            throws KernelException, IllegalActionException {
                        FSMActor controller = getController();
                        State currentState = controller.currentState();
                        chooseNonTransientTransition(currentState);
                    }
                };

            request.setPersistent(false);
            container.requestChange(request);
        }

        return;
    }

    /** Return the change context being made explicit.  This class
     *  overrides the implementation in the FSMDirector base class to
     *  report that HDF models only make state transitions between
     *  toplevel iterations.
     */
    public Entity getContext() {
        // Set the flag indicating whether we're in an SDF model or
        // not.
        try {
            _getEnclosingDomainActor();
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex);
        }

        return (Entity) toplevel();
    }

    /** If this method is called immediately after preinitialize(),
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
        State currentState;
        FSMActor controller = getController();
        _sendRequest = true;
        controller.setNewIteration(_sendRequest);
        super.initialize();
    }


    /** Make a state transition if this FSM is embedded in SDF.
     *  Otherwise, request a change of state transition to the manager.
     *  <p>
     *  @return True if the FSM is inside SDF and the super class
     *  method returns true; otherwise return true if the postfire of
     *  the current state refinement returns true.
     *  @exception IllegalActionException If a refinement throws it,
     *  if there is no controller.
     */
    public boolean postfire() throws IllegalActionException {
        FSMActor controller = getController();
        CompositeActor container = (CompositeActor) getContainer();

        if (_sendRequest) {
            _sendRequest = false;

            ChangeRequest request = new ChangeRequest(this, "make a transition") {
                    protected void _execute() throws KernelException {
                        _sendRequest = true;
                        makeStateTransition();
                    }
                };

            request.setPersistent(false);
            container.requestChange(request);
        }

        return _refinementPostfire;
    }

    /** Preinitialize() methods of all actors deeply contained by the
     *  container of this director. The HDF/SDF preinitialize method
     *  will compute the initial schedule. Propagate the consumption
     *  and production rates of the current state out to the
     *  corresponding ports of the container of this director.
     *  @exception IllegalActionException If the preinitialize()
     *  method of one of the associated actors throws it, or there
     *  is no controller.
     */
    public void preinitialize() throws IllegalActionException {
        _sendRequest = true;
        super.preinitialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // A flag indicating whether the FSM can send a change request.
    // The controller in HDFFSMDirector can only send one request per 
    // global iteration.
    private boolean _sendRequest;

}
