/* Controller for ERG modal models.

@Copyright (c) 2008 The Regents of the University of California.
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

package ptolemy.domains.erg.kernel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.util.BooleanDependency;
import ptolemy.actor.util.BreakCausalityInterface;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.data.BooleanToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.HasTypeConstraints;
import ptolemy.data.type.ObjectType;
import ptolemy.data.type.Type;
import ptolemy.domains.erg.lib.SynchronizeToRealtime;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.modal.ModalController;
import ptolemy.graph.Inequality;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.kernel.util.Workspace;

/**
 This controller is used in every ERG modal model. It contains an {@link
 ERGDirector} to execute the events in it.
 <p>
 Each ERG modal model has one or more ERG controllers. One of those controllers
 is the top level controller, which can be obtained with {@link
 ERGModalModel#getController()}. The other controllers are refinements of
 events.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ERGController extends ModalController {

    /** Construct an ERG controller with a name and a container.
     *
     *  @param container The container, which must be an {@link ERGModalModel}.
     *  @param name The name.
     *  @exception IllegalActionException If the container is incompatible with
     *   this actor.
     *  @exception NameDuplicationException If the name coincides with an actor
     *   already in the container.
     */
    public ERGController(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /** Construct an ERG controller in the specified workspace with
     *  no container and an empty string as a name. If the workspace argument is
     *  null, then use the default workspace.
     *
     *  @param workspace The workspace that will list the controller.
     *  @exception IllegalActionException If the container is incompatible with
     *   this actor.
     *  @exception NameDuplicationException If the name coincides with an actor
     *   already in the container.
     */
    public ERGController(Workspace workspace) throws IllegalActionException,
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
     *  @return A new ERGController.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ERGController controller = (ERGController) super.clone(workspace);
        controller._executiveDirector = null;
        controller._executiveDirectorVersion = -1;
        controller._objectScope = null;
        controller._objectScopeVersion = -1;
        return controller;
    }

    /** Invoke the fire() method of the enclosed {@link ERGDirector}.
     *
     *  @exception IllegalActionException If the ERGDirector throws it.
     *  @see ERGDirector#fire()
     */
    public void fire() throws IllegalActionException {
        director.fire();
    }

    /** Return a causality interface for this actor.  This method returns the
     *  causality interface where no output port depends on the input ports.  It
     *  is an instance of {@link BreakCausalityInterface}.
     *
     *  FIXME: A causality interface special for ERG should be returned instead.
     *
     *  @return A representation of the dependencies between input ports
     *   and output ports.
     */
    public CausalityInterface getCausalityInterface() {
        return new BreakCausalityInterface(this,
                BooleanDependency.OPLUS_IDENTITY);
    }

    /** Return the director responsible for the execution of this actor.
     *
     *  @return The director responsible for the execution of this actor.
     */
    public Director getDirector() {
        return director;
    }

    /** Return the executive director. If the current controller is the
     *  top-level controller of an ERG modal model, then the executive director
     *  is its director (returned by {@link #getDirector()}). Otherwise, the
     *  executive director is the director of the ERG controller at a higher
     *  level in the refinement hierarchy.
     *
     *  @return The executive director.
     */
    public Director getExecutiveDirector() {
        Workspace workspace = workspace();
        try {
            workspace.getReadAccess();
            if (_executiveDirectorVersion != workspace.getVersion()) {
                ERGModalModel modalModel = (ERGModalModel) getContainer();
                if (modalModel.getController() == this) {
                    _executiveDirector = super.getDirector();
                } else {
                    for (Object atomicEntity
                            : modalModel.allAtomicEntityList()) {
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
                                        // Return the director of the ERG
                                        // controller that has an event with the
                                        // current controller as its refinement.
                                        _executiveDirector = ((ERGController)
                                                event.getContainer()).director;
                                        break;
                                    }
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

    /** Return null as the initial state. In ERG there are multiple initial
     *  events. This method is overridden to return null. To get the list of
     *  initial events, check the isInitialState parameter of the events
     *  contained in this controller.
     *
     *  @return null
     */
    public State getInitialState() {
        return null;
    }

    /** Return a scope object that has current values from input ports
     *  of the containing ERG modal model in scope. This scope is used to
     *  evaluate guard expressions and actions. This scope is also specialized
     *  so that objects can be resolved and {@link ObjectToken} can be return
     *  for them.
     *
     *  @return A scope object that has current values from input ports of
     *  the containing ERG modal model in scope.
     */
    public ParserScope getPortScope() {
        if (_objectScope == null || _objectScopeVersion != _workspace
                .getVersion()) {
            _objectScope = new ERGObjectScope();
            _objectScopeVersion = _workspace.getVersion();
        }
        return _objectScope;
    }

    /** Test whether new input tokens have been received at the input ports.
     *
     *  @return true if new input tokens have been received.
     */
    public boolean hasInput() {
        Iterator<?> inPorts = ((ERGModalModel) getContainer()).inputPortList()
                .iterator();
        while (inPorts.hasNext() && !_stopRequested) {
            IOPort p = (IOPort) inPorts.next();
            Token token =
                (Token) _inputTokenMap.get(p.getName() + "_isPresent");
            if (token != null && BooleanToken.TRUE.equals(token)) {
                return true;
            }
        }
        return false;
    }

    /** Initialize this controller by initializing the director that it
     *  contains, which sets the initial events.
     *
     *  @exception IllegalActionException If the director or initialize() of the
     *  superclass throws it.
     */
    public void initialize() throws IllegalActionException {
        director.initialize();
        super.initialize();
    }

    /** Return the result of isFireFunctional() from the director.
     *
     *  @return The result of isFireFunctional() from the director.
     */
    public boolean isFireFunctional() {
        return director.isFireFunctional();
    }

    /** Return the result of isStrict() from the director.
    *
    *  @return The result of isStrict() from the director.
     * @exception IllegalActionException Thrown if causality interface 
     *  cannot be computed.
    */
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
    public boolean postfire() throws IllegalActionException {
        return director.postfire();
    }

    /** Invoke prefire() of the director.
     *
     *  @return Return value of prefire() of the director.
     *  @exception IllegalActionException If prefire() of the director throws
     *   it.
     */
    public boolean prefire() throws IllegalActionException {
        return director.prefire();
    }

    /** Preinitialize the controller by invoking preinitialize() of the
     *  director.
     *
     *  @exception IllegalActionException If preinitialize() of the director
     *   throws it.
     */
    public void preinitialize() throws IllegalActionException {
        director.preinitialize();
        super.preinitialize();
    }

    /** Stop execution by invoking stop() of the director.
     */
    public void stop() {
        director.stop();
    }

    /** Request that execution of the current iteration stop as soon
     *  as possible by invoking stopFire() of the director.
     */
    public void stopFire() {
        director.stopFire();
    }

    /** Return whether the synchronizeToRealtime attribute of this controller is
     *  set or not.
     *
     *  @return True if synchronizedToRealtime is set; false otherwise.
     */
    public boolean synchronizeToRealtime() {
        List<?> synchronizeAttributes =
            attributeList(SynchronizeToRealtime.class);
        boolean synchronize = false;
        if (synchronizeAttributes.size() > 0) {
            SynchronizeToRealtime attribute =
                (SynchronizeToRealtime) synchronizeAttributes.get(0);
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
    public Set<Inequality> typeConstraints() {
        Set<Inequality> constraintList = new HashSet<Inequality>(
                super.typeConstraints());
        List<?> events = entityList(Event.class);
        for (Object eventObject : events) {
            Event event = (Event) eventObject;
            List<?> attributes = event.attributeList(HasTypeConstraints.class);
            for (Object attributeObject : attributes) {
                HasTypeConstraints attribute =
                    (HasTypeConstraints) attributeObject;
                constraintList.addAll(attribute.typeConstraints());
            }
        }
        return constraintList;
    }

    /** A Boolean parameter that decides whether simultaneous events should be
     *  placed in the event queue in the last-in-first-out (LIFO) fashion or
     *  not.
     */
    public Parameter LIFO;

    /** The ERG director contained by this controller. */
    public ERGDirector director;

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
        director = new ERGDirector(this, "_Director");
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

    /** The scope used to evaluate actions. */
    private ERGObjectScope _objectScope = new ERGObjectScope();

    /** Version of _objectScope. */
    private long _objectScopeVersion = -1;

    /** This class implements a scope, which is used to evaluate the
     *  parsed expressions.  This class is currently rather simple,
     *  but in the future should allow the values of input ports to
     *  be referenced without having shadow variables.
     */
    private class ERGObjectScope extends PortScope {

        /** Look up and return the attribute with the specified name in the
         *  scope. Return null if such an attribute does not exist.
         *
         *  @param name The name of the variable to be looked up.
         *  @return The attribute with the specified name in the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public Token get(String name) throws IllegalActionException {
            Token token = super.get(name);
            if (token == null) {
                NamedObj object = ModelScope.getScopedObject(ERGController.this,
                        name);
                if (object instanceof Variable) {
                    token = ((Variable) object).getToken();
                } else if (object != null) {
                    token = new ObjectToken(object, object.getClass());
                }
            }
            return token;
        }

        /** Look up and return the type of the attribute with the
         *  specified name in the scope. Return null if such an
         *  attribute does not exist.
         *
         *  @param name The name of the variable to be looked up.
         *  @return The attribute with the specified name in the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public Type getType(String name) throws IllegalActionException {
            Type type = super.getType(name);
            if (type == null) {
                NamedObj object = ModelScope.getScopedObject(ERGController.this,
                        name);
                if (object instanceof Variable) {
                    type = ((Variable) object).getType();
                } else if (object != null) {
                    type = new ObjectType(object, object.getClass());
                }
            }
            return type;
        }

        /** Return the list of identifiers within the scope.
         *  @return The list of identifiers within the scope.
         */
        @SuppressWarnings("unchecked")
        public Set<?> identifierSet() {
            Set<Object> set = super.identifierSet();
            set.addAll(ModelScope.getAllScopedObjectNames(
                    ERGController.this));
            return set;
        }
    }
}
