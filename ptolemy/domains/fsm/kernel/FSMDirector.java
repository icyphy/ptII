/*An FSMDirector governs the execution of a modal model.

 Copyright (c) 1999-2000 The Regents of the University of California.
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
@ProposedRating Yellow (liuxj@eecs.berkeley.edu)
@AcceptedRating Yellow (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.kernel;

import ptolemy.actor.Director;
import ptolemy.actor.Actor;
import ptolemy.actor.TypedActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.Entity;
import ptolemy.data.Token;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;


//////////////////////////////////////////////////////////////////////////
//// FSMDirector
/**
An FSMDirector governs the execution of a modal model. A modal model is
a TypedCompositeActor with a FSMDirector as local director. The mode
control logic is captured by a mode controller, an instance of FSMActor
contained by the composite actor. Each state of the mode controller
represents a mode of operation and can be refined by a TypedActor contained
by the same composite actor.
<p>
When a modal model is fired, this director first transfers the input tokens
from the outside domain to the mode controller and the refinement of its
current state. The preemptive transitions from the current state of the mode
controller are examined. If there is more than one transition enabled, an
exception is thrown. If there is exactly one preemptive transition enabled
then it is chosen and the choice actions contained by the transition are
executed. The refinement of the current state is not fired. Any output token
produced by the mode controller is transferred to the outside domain. If no
preemptive transition is enabled, the refinement of the current state is
fired. The non-preemptive transitions from the current state of the mode
controller are examined. If there is more than one transition enabled, an
exception is thrown. If there is exactly one non-preemptive transition
enabled then it is chosen and the choice actions contained by the transition
are executed. Any output token produced by the mode controller or the
refinement is transferred to the outside domain.
<p>
The mode controller does not change state during successive firings in one
iteration in order to support outside domains that iterate to a fixed point.
When the modal model is postfired, the chosen transition of the latest firing
is committed. The commit actions contained by the transition are executed and
the current state of the mode controller is set to the destination state of
the transition.

@author Xiaojun Liu
@version $Id$
@see FSMActor
*/
public class FSMDirector extends Director {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public FSMDirector() {
        super();
        _createParameter();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this director.
     */
    public FSMDirector(Workspace workspace) {
        super(workspace);
        _createParameter();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of this director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     */
    public FSMDirector(CompositeActor container, String name)
            throws IllegalActionException {
        super(container, name);
        _createParameter();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Parameter specifying the name of the mode controller in the
     *  container of this director. This director must have a mode
     *  controller that has the same container as this director,
     *  otherwise an IllegalActionException will be thrown when action
     *  methods of this director are called.
     */
    public Parameter controllerName = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute is
     *  the <i>controllerName</i> parameter, record the change but do not
     *  check whether there is an FSMActor with the specified name in the
     *  container of this director.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If thrown by the superclass
     *   attributeChanged() method.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);
        if (attribute == controllerName) {
            _controllerVersion = -1;
        }
    }

    /** Clone the director into the specified workspace. This calls the
     *  base class and then sets the parameter public members to refer
     *  to the parameters of the new director.
     *  @param ws The workspace for the new director.
     *  @return A new director.
     *  @throws CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
            throws CloneNotSupportedException {
        FSMDirector newobj = (FSMDirector)super.clone(ws);
        newobj.controllerName =
                (Parameter)newobj.getAttribute("controllerName");
        newobj._controllerVersion = -1;
        newobj._localReceiverMapsVersion = -1;
        return newobj;
    }

    /** Set the values of input variables in the mode controller. Examine
     *  the preemptive outgoing transitions of its current state. Throw an
     *  exception if there is more than one transition enabled. If there
     *  is exactly one preemptive transition enabled then it is chosen and
     *  the choice actions contained by the transition are executed. The
     *  refinement of the current state of the mode controller is not fired.
     *  If no preemptive transition is enabled and the refinement is ready
     *  to fire in the current iteration, fire the refinement. The
     *  non-preemptive transitions from the current state of the mode
     *  controller are examined. If there is more than one transition
     *  enabled, an exception is thrown. If there is exactly one
     *  non-preemptive transition enabled then it is chosen and the choice
     *  actions contained by the transition are executed.
     *  @exception IllegalActionException If there is more than one
     *   transition enabled, or there is no controller, or thrown by any
     *   choice action.
     */
    public void fire() throws IllegalActionException {
        if(_debugging) _debug(getName(), " fire.");
        FSMActor ctrl = getController();
        ctrl._setInputVariables();
        if(_debugging) _debug(getName(), " find FSMActor " + ctrl.getName());
        State st = ctrl.currentState();
        Transition tr =
            ctrl._chooseTransition(st.preemptiveTransitionList());
        if (tr != null) {
            return;
        }
        if (_fireRefinement) {
            TypedActor ref = st.getRefinement();
            if(_debugging) _debug(getName(), " fire refinement",
                    ((ptolemy.kernel.util.NamedObj)ref).getName());
            ref.fire();
            ctrl._setInputsFromRefinement();
        }
        ctrl._chooseTransition(st.nonpreemptiveTransitionList());
        return;
    }

    /** Schedule a firing of the given actor at the given time. This
     *  is delegated to the executive director by scheduling with it
     *  a firing of the container of this director at the given time.
     *  The actor should be either the mode controller or the refinement
     *  of one of its states. For both cases firing the actor happens
     *  when the container of this director is fired at the given time.
     *  In the second case, if the actor is not the refinement of the
     *  state of the mode controller when the execution reaches the
     *  given time, the firing is ignored.
     *  @param actor The actor scheduled to be fired.
     *  @param time The scheduled time.
     *  @exception IllegalActionException If thrown in scheduling
     *   a firing of the container of this director at the given
     *   time with the executive director.
     *  FIXME: Changed by liuj, not yet reviewed.
     */
    public void fireAt(Actor actor, double time)
            throws IllegalActionException {
        Actor cont = (Actor)getContainer();
        Director execdir = cont.getExecutiveDirector();
        if(execdir != null) {
            execdir.fireAt(cont, time);
        } else {
            setCurrentTime(time);
        }
    }

    /** Return the mode controller of this director. The name of the
     *  mode controller is specified by the <i>controllerName</i>
     *  parameter. The mode controller must have the same container as
     *  this director.
     *  This method is read-synchronized on the workspace.
     *  @return The mode controller of this director.
     *  @exception IllegalActionException If no controller is found.
     */
    public FSMActor getController() throws IllegalActionException {
        if (_controllerVersion == workspace().getVersion()) {
            return _controller;
        }
        try {
            workspace().getReadAccess();
            StringToken tok = (StringToken)controllerName.getToken();
            if (tok == null) {
                throw new IllegalActionException(this, "No name for mode "
                        + "controller is set.");
            }
            String ctrlName = tok.stringValue();
            CompositeActor cont = (CompositeActor)getContainer();
            Entity entity = cont.getEntity(ctrlName);
            if (entity == null) {
                throw new IllegalActionException(this, "No controller found "
                        + "with name " + ctrlName);
            }
            if (!(entity instanceof FSMActor)) {
                throw new IllegalActionException(this, entity,
                        "mode controller must be an instance of FSMActor.");
            }
            _controller = (FSMActor)entity;
            _controllerVersion = workspace().getVersion();
            return _controller;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return the next iteration time provided by the refinement of the
     *  current state of the mode controller. If the refinement does not
     *  provide this, return that given by the superclass.
     *  @return The time of the next iteration.
     */
    public double getNextIterationTime() {
        try {
            Actor ref = getController().currentState().getRefinement();
            if (ref != null && ref.getDirector() != this) {
                // The refinement has a local director.
                return ref.getDirector().getNextIterationTime();
            }
        } catch (IllegalActionException ex) {
            // No mode controller, return that given by the superclass.
        }
        return super.getNextIterationTime();
    }

    /** Return true if the mode controller wishes to be scheduled for
     *  another iteration. Postfire the refinement of the current state
     *  of the mode controller if it is ready to fire in the current
     *  iteration. Execute the commit actions contained by the last
     *  chosen transition of the mode controller and set its current
     *  state to the destination state of the transition.
     *  If this director is at the top level, it will return the 
     *  postfire() value of the current refinement.
     *  @return True if the mode controller wishes to be scheduled for
     *   another iteration.
     *  @exception IllegalActionException If thrown by any commit action
     *   or there is no controller.
     *  FIXME: Changed by liuj, not yet reviewed.
     */
    public boolean postfire() throws IllegalActionException {
        FSMActor ctrl = getController();
        
        boolean result = true;
        if (_fireRefinement) {
            result = ctrl.currentState().getRefinement().postfire();
            if (getContainer().getExecutiveDirector() != null) {
                // Not at the top level, ignore the return value from 
                // the current refinement.
                result = true;
            }
            // Otherwise, 'and' the refinement.postfire() with controller.postfire()
        }
        result = result && ctrl.postfire();
        _currentLocalReceiverMap =
                (Map)_localReceiverMaps.get(ctrl.currentState());
        return result;
    }

    /** Return true if the mode controller is ready to fire. 
     *  If this model is not at the top level and the current
     *  time of this director lags behind that of the executive director,
     *  update the current time to that of the executive director. Record
     *  whether the refinement of the current state of the mode controller
     *  is ready to fire.
     *  @exception IllegalActionException If there is no controller.
     *  FIXME: Changed by liuj, not yet reviewed.
     */
    public boolean prefire() throws IllegalActionException {
        Actor cont = (Actor)getContainer();
        Director exeDir = cont.getExecutiveDirector();
        FSMActor ctrl = getController();
        Actor ref = ctrl.currentState().getRefinement();
        if (exeDir != null) {
            double outTime = exeDir.getCurrentTime();
            if (getCurrentTime() < outTime) {
                setCurrentTime(outTime);
            }
        }
        // Otherwise there's no notion of time.
        _fireRefinement = false;
        if (ref != null) {
            _fireRefinement = ref.prefire();
        }
        return getController().prefire();
    }

    /** Create receivers and invoke the preinitialize() methods of all
     *  actors deeply contained by the container of this director.
     *  This method is invoked once per execution, before any iteration,
     *  and before the initialize() method.
     *  @exception IllegalActionException If the preinitialize() method of
     *  one of the associated actors throws it, or there is no controller.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _buildLocalReceiverMaps();
    }

    /** Set the current time of the model under this director.
     *  It allows the set time to be earlier than the current time.
     *  This fearure is needed when switching between timed and untimed
     *  models.
     *
     *  @exception IllegalActionException Never thrown
     *  @param newTime The new current simulation time.
     *  FIXME: Changed by liuj, not yet reviewed.
     */
    public void setCurrentTime(double newTime) throws IllegalActionException {
        _currentTime = newTime;
    }

    /** Return true if data are transferred from the input port of
     *  the container to the ports connected to the inside of the input
     *  port and on the mode controller or the refinement of its current
     *  state. This method will transfer exactly one token on each
     *  input channel that has at least one token available. The port
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
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferInputs: port argument is not an opaque" +
                    "input port.");
        }
        boolean trans = false;
        Receiver[][] insiderecs = _currentLocalReceivers(port);
        for (int i = 0; i < port.getWidth(); i++) {
            try {
                if (port.hasToken(i)) {
                    Token t = port.get(i);
                    if (insiderecs != null && insiderecs[i] != null) {
                        if(_debugging) _debug(getName(),
                                "transfering input from " + port.getName());
                        for (int j = 0; j < insiderecs[i].length; j++) {
                            if (insiderecs[i][j].hasToken()) {
                                insiderecs[i][j].get();
                            }
                            insiderecs[i][j].put(t);
                        }
                        trans = true;
                    }
                } else {
                    if (insiderecs != null && insiderecs[i] != null) {
                        for (int j = 0; j < insiderecs[i].length; j++) {
                            if (insiderecs[i][j].hasToken()) {
                                insiderecs[i][j].get();
                            }
                        }
                    }
                }
            } catch (NoTokenException ex) {
                // this shouldn't happen.
                throw new InternalErrorException(
                        "Director.transferInputs: Internal error: " +
                        ex.getMessage());
            }
        }
        return trans;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // Map from input ports of the modal model to the local receivers
    // for the current state.
    protected Map _currentLocalReceiverMap = null;

    // True if the refinement of the current state of the mode controller
    // is ready to fire in an iteration.
    protected boolean _fireRefinement = false;

    // Stores for each state of the mode controller the map from input
    // ports of the modal model to the local receivers when the mode
    // controller is in that state.
    protected Map _localReceiverMaps = new HashMap();

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    // Build for each state of the mode controller the map from input
    // ports of the modal model to the local receivers when the mode
    // controller is in that state.
    // This method is read-synchronized on the workspace.
    // @exception IllegalActionException If there is no mode controller.
    protected void _buildLocalReceiverMaps()
            throws IllegalActionException {
        try {
            workspace().getReadAccess();
            FSMActor ctrl = getController();
            // Remove any existing maps.
            _localReceiverMaps.clear();
            // Create a map for each state of the mode controller.
            Iterator states = ctrl.entityList().iterator();
            State st = null;
            while (states.hasNext()) {
                st = (State)states.next();
                _localReceiverMaps.put(st, new HashMap());
            }
            CompositeActor comp = (CompositeActor)getContainer();
            Iterator inports = comp.inputPortList().iterator();
            List rlist = new LinkedList();
            while (inports.hasNext()) {
                IOPort port = (IOPort)inports.next();
                Receiver[][] allRcvrs = port.deepGetReceivers();
                states = ctrl.entityList().iterator();
                while (states.hasNext()) {
                    st = (State)states.next();
                    TypedActor ref = st.getRefinement();
                    Receiver[][] rs = new Receiver[allRcvrs.length][0];
                    for (int i = 0; i < allRcvrs.length; ++i) {
                        rlist.clear();
                        for (int j = 0; j < allRcvrs[i].length; ++j) {
                            Receiver r = allRcvrs[i][j];
                            Nameable cont = r.getContainer().getContainer();
                            if (cont == ctrl || cont == ref) {
                                rlist.add(r);
                            }
                        }
                        rs[i] = new Receiver[rlist.size()];
                        Object[] rcvrs = rlist.toArray();
                        for (int j = 0; j < rcvrs.length; ++j) {
                            rs[i][j] = (Receiver)rcvrs[j];
                        }
                    }
                    Map m = (HashMap)_localReceiverMaps.get(st);
                    m.put(port, rs);
                }
            }
            _localReceiverMapsVersion = workspace().getVersion();
            _currentLocalReceiverMap =
                    (Map)_localReceiverMaps.get(ctrl.currentState());
        } finally {
            workspace().doneReading();
        }
    }

    /** Return the receivers contained by ports connected to the inside
     *  of the given input port and on the mode controller or the
     *  refinement of its current state.
     *  @param port An input port of the container of this director.
     *  @return The receivers that currently get inputs from the given
     *   port.
     *  @exception IllegalActionException If there is no controller.
     */
    protected Receiver[][] _currentLocalReceivers(IOPort port)
            throws IllegalActionException {
        if (_localReceiverMapsVersion != workspace().getVersion()) {
            _buildLocalReceiverMaps();
        }
        return (Receiver[][])_currentLocalReceiverMap.get(port);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Create the controllerName parameter.
    private void _createParameter() {
        try {
            Attribute a = getAttribute("controllerName");
            if (a != null) {
                a.setContainer(null);
            }
            controllerName = new Parameter(this, "controllerName");
            controllerName.setTypeEquals(BaseType.STRING);
        } catch (NameDuplicationException ex) {
            throw new InternalErrorException(getName() + "Cannot create "
                    + "controllerName parameter.");
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(getName() + "Cannot create "
                    + "controllerName parameter.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Cached reference to mode controller.
    private FSMActor _controller = null;

    // Version of cached reference to mode controller.
    private long _controllerVersion = -1;

    // Version of the local receiver maps.
    private long _localReceiverMapsVersion = -1;

}
