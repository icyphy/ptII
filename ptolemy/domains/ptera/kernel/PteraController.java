/* Controller for Ptera modal models.

@Copyright (c) 2008-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.domains.ptera.kernel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.util.BooleanDependency;
import ptolemy.actor.util.BreakCausalityInterface;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.HasTypeConstraints;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.modal.ModalController;
import ptolemy.domains.ptera.lib.SynchronizeToRealtime;
import ptolemy.graph.Inequality;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.kernel.util.Workspace;

/**
 This controller is used in every Ptera modal model. It contains an {@link
 PteraDirector} to execute the events in it.
 <p>
 Each Ptera modal model has one or more Ptera controllers. One of those
 controllers is the top level controller, which can be obtained with {@link
 PteraModalModel#getController()}. The other controllers are refinements of
 events.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class PteraController extends ModalController {

    /** Construct an Ptera controller with a name and a container.
     *
     *  @param container The container, which must be an {@link PteraModalModel}.
     *  @param name The name.
     *  @exception IllegalActionException If the container is incompatible with
     *   this actor.
     *  @exception NameDuplicationException If the name coincides with an actor
     *   already in the container.
     */
    public PteraController(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /** Construct an Ptera controller in the specified workspace with
     *  no container and an empty string as a name. If the workspace argument is
     *  null, then use the default workspace.
     *
     *  @param workspace The workspace that will list the controller.
     *  @exception IllegalActionException If the container is incompatible with
     *   this actor.
     *  @exception NameDuplicationException If the name coincides with an actor
     *   already in the container.
     */
    public PteraController(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);
        _init();
    }

    /** React to a change in an attribute.
     *
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If thrown by the superclass
     *   attributeChanged() method, or if the value of the LIFO parameter cannot
     *   be read.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == LIFO) {
            director.LIFO.setToken(LIFO.getToken());
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the controller into the specified workspace. This calls the
     *  base class and then sets the attribute public members to refer
     *  to the attributes of the new actor.
     *
     *  @param workspace The workspace for the new controller.
     *  @return A new PteraController.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        PteraController controller = (PteraController) super.clone(workspace);
        controller._executiveDirector = null;
        controller._executiveDirectorVersion = -1;
        controller.director = (PteraDirector) controller
                .getAttribute("_Director");
        return controller;
    }

    /** Invoke the fire() method of the enclosed {@link PteraDirector}.
     *
     *  @exception IllegalActionException If the PteraDirector throws it.
     *  @see PteraDirector#fire()
     */
    @Override
    public void fire() throws IllegalActionException {
        director.fire();
    }

    /** Return a causality interface for this actor.  This method returns the
     *  causality interface where no output port depends on the input ports.  It
     *  is an instance of {@link BreakCausalityInterface}.
     *
     *  FIXME: A causality interface special for Ptera should be returned
     *  instead.
     *
     *  @return A representation of the dependencies between input ports
     *   and output ports.
     */
    @Override
    public CausalityInterface getCausalityInterface() {
        return new BreakCausalityInterface(this,
                BooleanDependency.OPLUS_IDENTITY);
    }

    /** Return the director responsible for the execution of this actor.
     *
     *  @return The director responsible for the execution of this actor.
     */
    @Override
    public Director getDirector() {
        return director;
    }

    /** Return the executive director. If the current controller is the
     *  top-level controller of an Ptera modal model, then the executive
     *  director is its director (returned by {@link #getDirector()}).
     *  Otherwise, the executive director is the director of the Ptera
     *  controller at a higher level in the refinement hierarchy.
     *
     *  @return The executive director.
     */
    @Override
    public Director getExecutiveDirector() {
        Workspace workspace = workspace();
        try {
            workspace.getReadAccess();
            if (_executiveDirectorVersion != workspace.getVersion()) {
                NamedObj container = getContainer();
                if (!(container instanceof PteraModalModel)) {
                    _executiveDirector = null;
                    _executiveDirectorVersion = workspace.getVersion();
                    return _executiveDirector;
                }

                PteraModalModel modalModel = (PteraModalModel) getContainer();
                if (modalModel.getController() == this) {
                    _executiveDirector = super.getDirector();
                } else {
                    _executiveDirector = null;
                    for (Object atomicEntity : modalModel.allAtomicEntityList()) {
                        if (atomicEntity instanceof Event) {
                            Event event = (Event) atomicEntity;
                            Actor[] refinements;
                            try {
                                refinements = event.getRefinement();
                            } catch (IllegalActionException e) {
                                throw new InternalErrorException(e);
                            }
                            if (refinements != null) {
                                for (Actor refinement : refinements) {
                                    if (refinement == this) {
                                        // Return the director of the Ptera
                                        // controller that has an event with the
                                        // current controller as its refinement.
                                        _executiveDirector = ((PteraController) event
                                                .getContainer()).director;
                                        break;
                                    }
                                }
                                if (_executiveDirector != null) {
                                    break;
                                }
                            }
                        }
                    }
                }
                _executiveDirectorVersion = workspace.getVersion();
            }
        } finally {
            workspace.doneReading();
        }
        return _executiveDirector;
    }

    /** Return null as the initial state. In Ptera there are multiple initial
     *  events. This method is overridden to return null. To get the list of
     *  initial events, check the isInitialState parameter of the events
     *  contained in this controller.
     *
     *  @return null
     */
    @Override
    public State getInitialState() {
        return null;
    }

    /** Initialize this controller by initializing the director that it
     *  contains, which sets the initial events, and initializing all the
     *  refinements.
     *
     *  @exception IllegalActionException If the director or initialize() of the
     *  superclass throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        director.initialize();
        super.initialize();
    }

    /** Return the result of isFireFunctional() from the director.
     *
     *  @return The result of isFireFunctional() from the director.
     */
    @Override
    public boolean isFireFunctional() {
        return director.isFireFunctional();
    }

    /** Return the result of isStrict() from the director.
     *
     *  @return The result of isStrict() from the director.
     * @exception IllegalActionException Thrown if causality interface
     *  cannot be computed.
     */
    @Override
    public boolean isStrict() throws IllegalActionException {
        return director.isStrict();
    }

    /** Invoke a specified number of iterations of the actor by calling
     *  iterate() of the director. An
     *  iteration is equivalent to invoking prefire(), fire(), and
     *  postfire(), in that order. In an iteration, if prefire()
     *  returns true, then fire() will be called once, followed by
     *  postfire(). Otherwise, if prefire() returns false, fire()
     *  and postfire() are not invoked, and this method returns
     *  NOT_READY. If postfire() returns false, then no more
     *  iterations are invoked, and this method returns STOP_ITERATING.
     *  Otherwise, it returns COMPLETED. If stop() is called while
     *  this is executing, then cease executing and return STOP_ITERATING.
     *
     *  @param count The number of iterations to perform.
     *  @return NOT_READY, STOP_ITERATING, or COMPLETED.
     *  @exception IllegalActionException If iterating is not
     *   permitted, or if prefire(), fire(), or postfire() throw it.
     */
    @Override
    public int iterate(int count) throws IllegalActionException {
        return director.iterate(count);
    }

    /** Create a new instance of {@link SchedulingRelation} with the specified
     *  name in this actor, and return it. This method is write-synchronized on
     *  the workspace.
     *
     *  @param name The name of the new scheduling relation.
     *  @return A scheduling relation with the given name.
     *  @exception IllegalActionException If the name argument is null.
     *  @exception NameDuplicationException If name collides with that
     *   of a scheduling relation already in this actor.
     */
    @Override
    public ComponentRelation newRelation(String name)
            throws IllegalActionException, NameDuplicationException {
        try {
            workspace().getWriteAccess();

            SchedulingRelation relation = new SchedulingRelation(this, name);
            return relation;
        } finally {
            workspace().doneWriting();
        }
    }

    /** Invoke postfire() of the director.
     *
     *  @return Return value of postfire() of the director.
     *  @exception IllegalActionException If postfire() of the director throws
     *   it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        return director.postfire();
    }

    /** Invoke prefire() of the director.
     *
     *  @return Return value of prefire() of the director.
     *  @exception IllegalActionException If prefire() of the director throws
     *   it.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        return director.prefire();
    }

    /** Preinitialize the controller by invoking preinitialize() of the
     *  director and that of the refinements.
     *
     *  @exception IllegalActionException If preinitialize() of the director or
     *   refinements throws it.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        director.preinitialize();
        super.preinitialize();
    }

    /** Set the value of the shadow variables for input ports of this actor.
     *  This method skips over ports that connected to outputs of a refinement.
     *  @exception IllegalActionException If a shadow variable cannot take
     *   the token read from its corresponding channel (should not occur).
     */
    @Override
    public void readInputs() throws IllegalActionException {
        _setCurrentConnectionMap();
        super.readInputs();
    }

    /** Stop execution by invoking stop() of the director.
     */
    @Override
    public void stop() {
        director.stop();
    }

    /** Request that execution of the current iteration stop as soon
     *  as possible by invoking stopFire() of the director.
     */
    @Override
    public void stopFire() {
        director.stopFire();
    }

    /** Return whether the synchronizeToRealtime attribute of this controller is
     *  set or not.
     *
     *  @return True if synchronizedToRealtime is set; false otherwise.
     */
    public boolean synchronizeToRealtime() {
        List<?> synchronizeAttributes = attributeList(SynchronizeToRealtime.class);
        boolean synchronize = false;
        if (synchronizeAttributes.size() > 0) {
            SynchronizeToRealtime attribute = (SynchronizeToRealtime) synchronizeAttributes
                    .get(0);
            try {
                synchronize = ((BooleanToken) attribute.getToken())
                        .booleanValue();
            } catch (IllegalActionException e) {
                return false;
            }
        }
        return synchronize;
    }

    /** Invoke terminate() of the director.
     */
    @Override
    public void terminate() {
        director.terminate();
    }

    /** Return the type constraints of this actor. The constraints
     *  have the form of a set of inequalities. This method first
     *  creates constraints such that the type of any input port that
     *  does not have its type declared must be less than or equal to
     *  the type of any output port that does not have its type
     *  declared. Type constraints from the contained Typeables
     *  (ports, variables, and parameters) are collected. In addition,
     *  type constraints from all the transitions are added. These
     *  constraints are determined by the guard and trigger expressions
     *  of transitions, and actions contained by the transitions.
     *  This method is read-synchronized on the workspace.
     *
     *  @return A list of inequalities.
     *  @see ptolemy.graph.Inequality
     */
    @Override
    public Set<Inequality> typeConstraints() {
        Set<Inequality> constraintList = new HashSet<Inequality>(
                super.typeConstraints());
        List<?> events = entityList(Event.class);
        for (Object eventObject : events) {
            Event event = (Event) eventObject;
            List<?> attributes = event.attributeList(HasTypeConstraints.class);
            for (Object attributeObject : attributes) {
                HasTypeConstraints attribute = (HasTypeConstraints) attributeObject;
                constraintList.addAll(attribute.typeConstraints());
            }
        }
        return constraintList;
    }

    /** Wrap up the controller by invoking wrapup() of the director and that of
     *  the refinements.
     *
     *  @exception IllegalActionException If wrapup() of the director or
     *   refinements throws it.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        director.wrapup();
        super.wrapup();
    }

    /** A Boolean parameter that decides whether simultaneous events should be
     *  placed in the event queue in the last-in-first-out (LIFO) fashion or
     *  not.
     */
    public Parameter LIFO;

    /** The Ptera director contained by this controller. */
    public PteraDirector director;

    /** Return a map from the classes of the entities to be dropped into a state
     *  and the class names of the refinements that can be used to contain those
     *  entities.
     *
     *  @return The map.
     */
    @Override
    protected TreeMap<Class<? extends Entity>, String> _getRefinementClasses() {
        TreeMap<Class<? extends Entity>, String> map = super
                ._getRefinementClasses();
        map.put(Event.class, PteraController.class.getName());
        map.put(PteraController.class, PteraController.class.getName());
        return map;
    }

    /** Set the event currently being executed.
     *
     *  @param event The current event.
     */
    protected void _setCurrentEvent(Event event) {
        _currentState = event;
    }

    /** Create director for this controller.
     *
     *  @exception IllegalActionException If the controller is incompatible
     *  with the director.
     *  @exception NameDuplicationException If the name of the director
     *  coincides with a director already in the controller.
     */
    private void _init() throws IllegalActionException,
            NameDuplicationException {
        director = new PteraDirector(this, "_Director");
        new SingletonAttribute(director, "_hide");

        LIFO = new Parameter(this, "LIFO");
        LIFO.setTypeEquals(BaseType.BOOLEAN);
        LIFO.setToken(BooleanToken.TRUE);
        director.LIFO.setToken(BooleanToken.TRUE);
    }

    /** The last updated executive director. */
    private Director _executiveDirector;

    /** The version of the workspace when _executiveDirector was updated. */
    private long _executiveDirectorVersion = -1;
}
