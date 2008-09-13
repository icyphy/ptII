/* Events in the ERG controller.

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

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Executable;
import ptolemy.actor.Initializable;
import ptolemy.actor.TypedActor;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Constants;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.kernel.util.Workspace;

/**
 An ERG event is contained by an ERG controller in an ERG modal model. Each
 event can be placed in the ERG controller's event queue when it is scheduled.
 When the model time reaches the time of the scheduled event, the event is
 removed from the event queue and is processed. Actions can be associated with
 each event, so that processing an event causes those actions to be executed.
 <p>
 An event may schedule another event after a 0 or greater delay of model time.
 This scheduling relation may be guarded by a Boolean expression. When an event
 schedules another, processing the first one causes the second one to be placed
 in the same ERG controller's event queue. An event may also cancel another
 event that was previously placed in the same ERG controller's event queue but
 has not been processed yet.
 <p>
 If the <code>fireOnInput</code> parameter of an event is set to true, then the
 event is also placed in its ERG controller's input event queue when scheduled.
 When the ERG controller receives an input at any of its input ports, all the
 events in its input event queue are removed and processed. Those events can use
 the value of that input in their guards or actions.
 <p>
 An event may also define 0 or more formal parameters. When it is scheduled, on
 the scheduling relation, values to those parameters must be provided.
 <p>
 When an event is processed, if there are actions defined for it, then those
 actions are executed. This happens before the refinements of the event is
 fired, if any.
 <p>
 One or more variable names can be listed in the <code>monitoredVariables</code>
 parameter, separated by commas. If any such variable names are specified, when
 the model is executed, changes on those variables are captured by the event.
 This means, if the event is scheduled in the ERG controller's event queue and a
 change is made on the variable (e.g., by the user or by actors such as {@link
 ptolemy.actor.lib.SetVariable}), then the event is processed even though its
 scheduled time has not arrive yet.
 <p>
 This class extends the {@link State} class in FSM, but it implements a model of
 computation whose semantics is completely different from FSM. Extending from
 FSM only because this makes it possible to reuse the user interface implemented
 for FSM.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Event extends State implements Initializable, ValueListener {

    /** Construct an event with the given name contained by the specified
     *  composite entity. The container argument must not be null, or a
     *  NullPointerException will be thrown. This event will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string.
     *  Increment the version of the workspace.
     *  This constructor write-synchronizes on the workspace.
     *
     *  @param container The container.
     *  @param name The name of the state.
     *  @exception IllegalActionException If the state cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   that of an entity already in the container.
     */
    public Event(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        refinementName.setVisibility(Settable.NONE);

        //isInitialState.setDisplayName("isInitialEvent");
        //isFinalState.setDisplayName("isFinalEvent");
        isInitialState.setVisibility(Settable.NONE);
        isInitialState.setPersistent(false);
        isFinalState.setVisibility(Settable.NONE);
        isFinalState.setPersistent(false);

        isInitialEvent = new Parameter(this, "isInitialEvent");
        isInitialEvent.setTypeEquals(BaseType.BOOLEAN);
        isInitialEvent.setExpression("false");
        isFinalEvent = new Parameter(this, "isFinalEvent");
        isFinalEvent.setTypeEquals(BaseType.BOOLEAN);
        isFinalEvent.setExpression("false");

        parameters = new ParametersAttribute(this, "parameters");

        actions = new ActionsAttribute(this, "actions");
        Variable variable = new Variable(actions, "_textHeightHint");
        variable.setExpression("5");
        variable.setPersistent(false);

        parameters.setExpression("()");

        fireOnInput = new Parameter(this, "fireOnInput");
        fireOnInput.setToken(BooleanToken.FALSE);
        fireOnInput.setTypeEquals(BaseType.BOOLEAN);

        monitoredVariables = new StringParameter(this, "monitoredVariables");
    }

    /** Add the specified object to the list of objects whose
     *  preinitialize(), intialize(), and wrapup()
     *  methods should be invoked upon invocation of the corresponding
     *  methods of this object.
     *
     *  @param initializable The object whose methods should be invoked.
     *  @see #removeInitializable(Initializable)
     *  @see ptolemy.actor.CompositeActor#addPiggyback(Executable)
     */
    public void addInitializable(Initializable initializable) {
        if (_initializables == null) {
            _initializables = new LinkedList<Initializable>();
        }
        _initializables.add(initializable);
    }

    /** React to a change in an attribute. If the changed attribute is
     *  the <code>parameters</code> attribute, update the parser scope for the
     *  actions so that the parameters' names can be referred to in those
     *  actions. If it is <code>monitoredVariables</code>, register this event
     *  as a value listener of all the monitored variables. If the changed
     *  attribute is <code>isInitialState</code>, do nothing. This is because
     *  the ERG controller need not be updated with this attribute is set. If
     *  the changed attribute is among the other attributes, then the superclass
     *  is called.
     *
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If thrown by the superclass
     *   attributeChanged() method, or the parser scope cannot be updated.
     */
    public void attributeChanged(Attribute attribute)
    throws IllegalActionException {
        if (attribute != isInitialState) {
            super.attributeChanged(attribute);
        }

        if (attribute == monitoredVariables) {
            _addValueListener();
        } else if (attribute == parameters) {
            List<EventParameter> eventParameters = attributeList(
                    EventParameter.class);
            for (EventParameter parameter : eventParameters) {
                try {
                    parameter.setContainer(null);
                } catch (NameDuplicationException e) {
                    // This should not happen. Ignore.
                }
            }

            List<String> names = parameters.getParameterNames();
            int paramCount = names == null ? 0 : names.size();
            if (paramCount > 0) {
                Type[] types = parameters.getParameterTypes();
                int i = 0;
                for (String name : names) {
                    Type type = types[i++];
                    Attribute existingAttribute = getAttribute(name);
                    if (existingAttribute != null) {
                        boolean compatible = false;
                        if (existingAttribute instanceof Variable) {
                            Variable variable = (Variable) existingAttribute;
                            Type declaredType = variable.getDeclaredType();
                            if (type.isCompatible(declaredType)) {
                                compatible = true;
                            }
                        }
                        if (!compatible) {
                            throw new IllegalActionException(this, "An " +
                                    "attribute named \"" + name + "\" is " +
                                    "found, but either it is not a variable, " +
                                    "or its type is not compatible with the " +
                                    "declared type of the parameter, which " +
                                    "is " + type + ".");
                        }
                    } else {
                        try {
                            EventParameter parameter = new EventParameter(this,
                                    name);
                            parameter.setTypeEquals(type);
                            Token defaultToken = Constants.get(type.toString());
                            if (defaultToken != null) {
                                parameter.setToken(defaultToken);
                            }
                        } catch (NameDuplicationException e) {
                            throw new IllegalActionException(this, "Unable " +
                                    "to create a parameter named \"" + name +
                                    "\".");
                        }
                    }
                }
            }
        } else if (attribute == isInitialEvent) {
            isInitialState.setToken(isInitialEvent.getToken());
        }
    }

    /** Clone the state into the specified workspace. This calls the
     *  base class and then sets the attribute and port public members
     *  to refer to the attributes and ports of the new state.
     *
     *  @param workspace The workspace for the new event.
     *  @return A new event.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Event newObject = (Event) super.clone(workspace);
        newObject._monitoredVariables = null;
        newObject._parserScope = null;
        newObject._parserScopeVersion = -1;
        return newObject;
    }

    /** Process this event with the given arguments. The number of arguments
     *  provided must be equal to the number of formal parameters defined for
     *  this event, and their types must match. The actions of this event are
     *  executed.
     *
     *  @param arguments The arguments used to process this event.
     *  @exception IllegalActionException If the number of the arguments or
     *   their types do not match, the actions cannot be executed, or any
     *   expression (such as guards and arguments to the next events) cannot be
     *   evaluated.
     */
    public void fire(ArrayToken arguments) throws IllegalActionException {
        List<String> names = parameters.getParameterNames();
        int paramCount = names == null ? 0 : names.size();
        int argCount = arguments == null ? 0 : arguments.length();
        if (paramCount > argCount) {
            throw new IllegalActionException(this, "The number of arguments to "
                    + "this event must be greater than or equal to the number "
                    + "of declared parameters, which is " + paramCount + ".");
        }

        if (paramCount > 0) {
            int i = 0;
            for (String name : names) {
                ((Variable) getAttribute(name)).setToken(arguments.getElement(
                        i++));
            }
        }

        actions.execute();
    }

    /** Return whether this event is an input event, which is processed when any
     *  input is received by the containing ERG controller.
     *
     *  @return True if this event is an input event.
     */
    public boolean fireOnInput() {
        try {
            return ((BooleanToken) fireOnInput.getToken()).booleanValue();
        } catch (IllegalActionException e) {
            return false;
        }
    }

    /** Return the ERG controller that contains this event.
     *
     *  @return The ERG controller.
     */
    public ERGController getController() {
        return (ERGController) getContainer();
    }

    /** Begin execution of the actor and start to monitor the variables whose
     *  values this event listens to. Also invoke the initialize methods of
     *  objects that have been added using {@link
     *  #addInitializable(Initializable)}.
     *
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void initialize() throws IllegalActionException {
        _monitoring = true;
        // Invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.initialize();
            }
        }
    }

    /** Return whether this event is a final event, so that its execution causes
     *  the event queue of the ERG director to be cleared.
     *
     *  @return true if this event is a final event, or false otherwise.
     *  @exception IllegalActionException If the expression of the isFinalEvent
     *   parameter cannot be parsed or cannot be evaluated, or if the result of
     *   evaluation violates type constraints, or if the result of evaluation is
     *   null and there are variables that depend on this one.
     */
    public boolean isFinalEvent() throws IllegalActionException {
        return ((BooleanToken) isFinalEvent.getToken()).booleanValue();
    }

    /** Return whether this event is an initial event, so that it is
     *  automatically scheduled at model time 0 in the ERG director's event
     *  queue.
     *
     *  @return true if this event is a final event, or false otherwise.
     *  @exception IllegalActionException If the expression of the
     *   isInitialEvent parameter cannot be parsed or cannot be evaluated, or if
     *   the result of evaluation violates type constraints, or if the result of
     *   evaluation is null and there are variables that depend on this one.
     */
    public boolean isInitialEvent() throws IllegalActionException {
        return ((BooleanToken) isInitialEvent.getToken()).booleanValue();
    }

    /** Do nothing except invoke the preinitialize methods
     *  of objects that have been added using {@link
     *  #addInitializable(Initializable)}.
     *  @exception IllegalActionException If one of the added objects
     *   throws it.
     */
    public void preinitialize() throws IllegalActionException {
        // Invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.preinitialize();
            }
        }
    }

    /** Remove the specified object from the list of objects whose
     *  preinitialize(), intialize(), and wrapup()
     *  methods should be invoked upon invocation of the corresponding
     *  methods of this object. If the specified object is not
     *  on the list, do nothing.
     *  @param initializable The object whose methods should no longer be invoked.
     *  @see #addInitializable(Initializable)
     *  @see ptolemy.actor.CompositeActor#removePiggyback(Executable)
     */
    public void removeInitializable(Initializable initializable) {
        if (_initializables != null) {
            _initializables.remove(initializable);
            if (_initializables.size() == 0) {
                _initializables = null;
            }
        }
    }

    /** Schedule the next events by evaluating all scheduling relations from
     *  this event. This method uses the argument values passed to this event by
     *  the previous invocation to {@link #fire(ArrayToken)}. If {@link
     *  #fire(ArrayToken)} has never been called, it uses a default scope in
     *  which no argument value has been given.
     *  <p>
     *  This method searches for all the events that are scheduled or
     *  cancelled by this event. For each scheduling relation from this event,
     *  the guard is tested. If it is true, the ending event (which could be the
     *  same as this event) is scheduled to occur after the specified amount of
     *  delay. Arguments to that event, if any, are also computed at this time.
     *  For each cancelling edge from this event, the ending event is cancelled
     *  in the containing ERG controller's event queue, if it is in it.
     *  <p>
     *  All the scheduling relations from this events are tested with their
     *  guards. If a scheduling relation's guard returns true, then the event
     *  that it points to is scheduled to occur after the amount of model time
     *  specified by the scheduling relation's delay parameter.
     *
     *  @exception IllegalActionException If the scheduling relations cannot be
     *  evaluated.
     */
    public void scheduleEvents() throws IllegalActionException {
        ERGController controller = (ERGController) getContainer();
        ERGDirector director = controller.director;
        ParserScope scope = _getParserScope();

        List<SchedulingRelation> schedules = new LinkedList<SchedulingRelation>(
                preemptiveTransitionList());
        schedules.addAll(nonpreemptiveTransitionList());
        boolean lifo = ((BooleanToken) getController().LIFO.getToken())
                .booleanValue();
        if (lifo) {
            Collections.sort(schedules, _LIFO_COMPARATOR);
        } else {
            Collections.sort(schedules, _FIFO_COMPARATOR);
        }
        for (SchedulingRelation schedule : schedules) {
            if (schedule.isEnabled(scope)) {
                double delay = schedule.getDelay(scope);
                Event nextEvent = (Event) schedule.destinationState();
                if (schedule.isCanceling()) {
                    director.cancel(nextEvent);
                } else {
                    ArrayToken edgeArguments = schedule.getArguments(scope);
                    director.fireAt(nextEvent, director.getModelTime().add(
                            delay), edgeArguments);
                }
            }
        }
    }

    /** Call the superclass to set the container (which should be an instance of
     *  {@link ERGController}) of this event, and then register this event as a
     *  value listener of all the variables listed in the
     *  <code>monitoredVariables</code> parameter. This method also adds the
     *  event to the container's initializable objects by calling its
     *  {@link Initializable#addInitializable(Initializable)} method.
     *
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace, or
     *   if the protected method _checkContainer() throws it, or if
     *   a contained Settable becomes invalid and the error handler
     *   throws it.
     *  @exception NameDuplicationException If the name of this entity
     *   collides with a name already in the container.
     */
    public void setContainer(CompositeEntity container)
    throws IllegalActionException, NameDuplicationException {
        CompositeEntity oldContainer = (CompositeEntity) getContainer();
        if (oldContainer instanceof Initializable) {
            ((Initializable) oldContainer).removeInitializable(this);
        }

        super.setContainer(container);

        if (container instanceof Initializable) {
            ((Initializable) container).addInitializable(this);
        }
        if (container != null) {
            _addValueListener();
        }
    }

    /** Request that the event cease execution altogether.
     */
    public void stop() {
    }

    /** Monitor the change of a variable specified by the <code>settable</code>
     *  argument if the execution has started, and invokes fireAt() of the
     *  director to request to fire this event at the current model time.
     *
     *  @param settable The variable that has been changed.
     */
    public void valueChanged(Settable settable) {
        if (_monitoring) {
            ERGDirector director =
                (ERGDirector) ((ERGController) getContainer()).getDirector();
            try {
                ERGDirector.TimedEvent timedEvent = director.findFirst(this,
                        false);
                if (timedEvent != null) {
                    // This event has been scheduled at least once.
                    Time modelTime = director.getModelTime();
                    if (modelTime.compareTo(timedEvent.timeStamp) < 0) {
                        director.cancel(this);
                        director.fireAt(this, modelTime, timedEvent.arguments);
                    }
                }
            } catch (IllegalActionException e) {
                // This shouldn't happen because this event does not have any
                // refinement.
                throw new InternalErrorException(e);
            }
        }
    }

    /** Stop monitoring variables. Also invoke the wrapup methods of
     *  objects that have been added using {@link
     *  #addInitializable(Initializable)}.
     *
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void wrapup() throws IllegalActionException {
        // Invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.wrapup();
            }
        }
        _monitoring = false;
    }

    /** The actions for this event. */
    public ActionsAttribute actions;

    /** A Boolean value representing whether this event is an input event. */
    public Parameter fireOnInput;

    /** A Boolean parameter to specify whether this event is a final event. */
    public Parameter isFinalEvent;

    /** A Boolean parameter to specify whether this event is an initial
    event. */
    public Parameter isInitialEvent;

    /** A comma-separated list of variable names to be monitored. */
    public StringParameter monitoredVariables;

    /** A list of formal parameters. */
    public ParametersAttribute parameters;

    //////////////////////////////////////////////////////////////////////////
    //// EventParameter

    /**
     The parameter to store an argument passed on a scheduling relation to this
     event.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 7.1
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    public static class EventParameter extends Parameter {

        /** Construct a parameter with the given name contained by the specified
         *  entity. The container argument must not be null, or a
         *  NullPointerException will be thrown.  This parameter will use the
         *  workspace of the container for synchronization and version counts.
         *  If the name argument is null, then the name is set to the empty
         *  string. The object is not added to the list of objects in the
         *  workspace unless the container is null.
         *  Increment the version of the workspace.
         *  @param container The container.
         *  @param name The name of the parameter.
         *  @exception IllegalActionException If the parameter is not of an
         *   acceptable class for the container.
         *  @exception NameDuplicationException If the name coincides with
         *   a parameter already in the container.
         */
        public EventParameter(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);

            setVisibility(Settable.EXPERT);
            setPersistent(false);
        }
    }

    /** Return the parser scope used to evaluate the actions and values
     *  associated with scheduling relations. The parser scope can evaluate
     *  names in the parameter list of this event. The values for those names
     *  are obtained from the given array of arguments.
     *
     *  @param arguments The array of arguments.
     *  @return The parser scope.
     *  @exception IllegalActionException If the number or types of the given
     *  arguments do not match those of the defined parameters.
     */
    protected ParserScope _getParserScope() {
        if (_parserScope == null || _parserScopeVersion != _workspace
                .getVersion()) {
            _parserScope = new VariableScope(this, getController()
                    .getPortScope());
            _parserScopeVersion = _workspace.getVersion();
        }
        return _parserScope;
    }

    /** Schedule the given actor, which is a refinement of this event.
     *
     *  @param refinement The refinement to be scheduled to fire.
     *  @return true if the refinement is scheduled; false otherwise.
     *  @throws IllegalActionException If thrown when trying to initialize the
     *  schedule of an ERGController refinement.
     */
    protected boolean _scheduleRefinement(TypedActor refinement)
            throws IllegalActionException {
        ERGController controller = (ERGController) getContainer();
        ERGDirector director = controller.director;
        refinement.initialize();
        director.fireAt(refinement, director.getModelTime());
        return true;
    }

    /** Remove this event from the value listener lists of the previously
     *  monitored variables, and register in the value listener lists of the
     *  newly monitored variables specified by the
     *  <code>monitoredVariables</code> parameter.
     *
     *  @exception IllegalActionException If value of the
     *  <code>monitoredVariables</code> parameter cannot be obtained, or it is
     *  malformed.
     */
    private void _addValueListener() throws IllegalActionException {
        if (_monitoredVariables == null) {
            _monitoredVariables = new LinkedList<Variable>();
        } else {
            for (Variable variable : _monitoredVariables) {
                variable.removeValueListener(this);
            }
            _monitoredVariables.clear();
        }

        if (monitoredVariables == null) {
            return;
        }
        String[] names = monitoredVariables.stringValue().split(",");
        for (String name : names) {
            name = name.trim();
            if (name.equals("")) {
                continue;
            }
            Variable variable = ModelScope.getScopedVariable(null, this, name);
            if (variable == null) {
                throw new IllegalActionException(this, "Unable to find " +
                        "variable with name\"" + name + "\".");
            } else {
                _monitoredVariables.add(variable);
                variable.addValueListener(this);
            }
        }
    }

    /** The scheduling relation comparator to be used when LIFO is set to false.
     */
    private static final PriorityComparator _FIFO_COMPARATOR =
        new PriorityComparator(false);

    /** The scheduling relation comparator to be used when LIFO is set to true.
     */
    private static final PriorityComparator _LIFO_COMPARATOR =
        new PriorityComparator(true);

    /** List of objects whose (pre)initialize() and wrapup() methods should be
     *  slaved to these.
     */
    private transient List<Initializable> _initializables;

    /** The variables that this event has been monitoring. */
    private List<Variable> _monitoredVariables;

    /** Whether this event is monitoring the change of values of the monitored
    variables. */
    private boolean _monitoring = false;

    /** The parser scope used to execute actions and evaluate arguments.
     */
    private VariableScope _parserScope;

    /** Version of _parserScope.
     */
    private long _parserScopeVersion = -1;

    //////////////////////////////////////////////////////////////////////////
    //// PriorityComparator

    /**
     The comparator to compare the priority of two scheduling relations from
     this event.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 7.1
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private static class PriorityComparator
            implements Comparator<SchedulingRelation> {

        /** Compare two scheduling relations from the same event.
         *
         *  @param schedule1 The first scheduling relation.
         *  @param schedule2 The second scheduling relation.
         *  @return 1 if the first scheduling relation is greater; -1 if the
         *  second scheduling relation is greater; 0 if the order is
         *  unspecified.
         */
        public int compare(SchedulingRelation schedule1,
                SchedulingRelation schedule2) {
            try {
                int priority1 = ((IntToken) schedule1.priority.getToken())
                        .intValue();
                int priority2 = ((IntToken) schedule2.priority.getToken())
                        .intValue();
                if (priority1 > priority2) {
                    return _lifo ? -1 : 1;
                } else if (priority1 < priority2) {
                    return _lifo ? 1 : -1;
                } else {
                    return 0;
                }
            } catch (IllegalActionException e) {
                throw new KernelRuntimeException(e, "Unable to obtain event " +
                        "priority.");
            }
        }

        /** Construct a comparator.
         *
         *  @param lifo Whether the current scheme of ordering events is LIFO or
         *  not.
         */
        private PriorityComparator(boolean lifo) {
            _lifo = lifo;
        }

        /** Whether the current scheme of ordering events is LIFO or not. */
        private boolean _lifo;
    }
}
