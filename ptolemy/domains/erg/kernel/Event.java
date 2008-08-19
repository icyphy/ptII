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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.Executable;
import ptolemy.actor.Initializable;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
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
 @since Ptolemy II 6.1
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
        isFinalState.setVisibility(Settable.NONE);

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
            actions._updateParserScope();
        }
    }

    /** Clone the state into the specified workspace. This calls the
     *  base class and then sets the attribute and port public members
     *  to refer to the attributes and ports of the new state.
     *  @param workspace The workspace for the new event.
     *  @return A new event.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Event newObject = (Event) super.clone(workspace);
        newObject._monitoredVariables = null;
        return newObject;
    }

    /** Process this event with the given arguments. The number of arguments
     *  provided must be equal to the number of formal parameters defined for
     *  this event, and their types must match. This method first executes the
     *  actions, then searches for all the events that are scheduled or
     *  cancelled by this event. For each scheduling relation from this event,
     *  the guard is tested. If it is true, the ending event (which could be the
     *  same as this event) is scheduled to occur after the specified amount of
     *  delay. Arguments to that event, if any, are also computed at this time.
     *  For each cancelling edge from this event, the ending event is cancelled
     *  in the containing ERG controller's event queue, if it is in it.
     *
     *  @param arguments The arguments used to process this event.
     *  @exception IllegalActionException If the number of the arguments or
     *   their types do not match, the actions cannot be executed, or any
     *   expression (such as guards and arguments to the next events) cannot be
     *   evaluated.
     */
    public void fire(ArrayToken arguments) throws IllegalActionException {
        ParserScope scope = _getParserScope(arguments);
        actions.execute(scope);
        _fireRefinements();
        _scheduleEvents(scope);
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

    /** Begin execution of the actor and start to monitor the variables whose
     *  values this event listens to. Also invoke the initialize methods of
     *  objects that have been added using {@link #addInitializable()}.
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

    /** Do nothing except invoke the preinitialize methods
     *  of objects that have been added using {@link #addInitializable()}.
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
                ERGDirector.TimedEvent timedEvent = director.findFirst(this);
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
     *  objects that have been added using {@link #addInitializable()}.
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
    //// ParameterParserScope

    /**
     The parser scope that resolves the parameters for an ERG event with their
     actual values supplied when the event is processed.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 7.1
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    public static class ParameterParserScope implements ParserScope {

        /** Look up and return the value with the specified name in the
         *  scope. Return null if the name is not defined in this scope.
         *
         *  @param name The name of the variable to be looked up.
         *  @return The token associated with the given name in the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public Token get(String name) throws IllegalActionException {
            if (_parameterMap.containsKey(name)) {
                return _parameterMap.get(name);
            } else {
                return _superscope.get(name);
            }
        }

        /** Look up and return the type of the value with the specified
         *  name in the scope. Return null if the name is not defined in
         *  this scope.
         *
         *  @param name The name of the variable to be looked up.
         *  @return The token associated with the given name in the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public Type getType(String name) throws IllegalActionException {
            return get(name).getType();
        }

        /** Look up and return the type term for the specified name
         *  in the scope. Return null if the name is not defined in this
         *  scope, or is a constant type.
         *
         *  @param name The name of the variable to be looked up.
         *  @return The InequalityTerm associated with the given name in
         *  the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public InequalityTerm getTypeTerm(String name)
                throws IllegalActionException {
            return _superscope.getTypeTerm(name);
        }

        /** Return a list of names corresponding to the identifiers
         *  defined by this scope.  If an identifier is returned in this
         *  list, then get() and getType() will return a value for the
         *  identifier.  Note that generally speaking, this list is
         *  extremely expensive to compute, and users should avoid calling
         *  it.  It is primarily used for debugging purposes.
         *
         *  @return A list of names corresponding to the identifiers
         *  defined by this scope.
         *  @exception IllegalActionException If constructing the list causes
         *  it.
         */
        public Set<?> identifierSet() throws IllegalActionException {
            Set<Object> set = new HashSet<Object>(_parameterMap.keySet());
            set.addAll(_superscope.identifierSet());
            return set;
        }

        /** Construct a parser scope with the given map from parameter names to
         *  their values, and a given scope used to look up names if they cannot
         *  be resolved as parameters.
         *
         *  @param parameterMap The parameter map.
         *  @param superscope The scope used to look up names if they cannot
         *  be resolved as parameters.
         */
        ParameterParserScope(Map<String, Token> parameterMap,
                ParserScope superscope) {
            _parameterMap = parameterMap;
            _superscope = superscope;
        }

        /** The parameter map. */
        private Map<String, Token> _parameterMap;

        /** The scope used to look up names if they cannot be resolved as
            parameters. */
        private ParserScope _superscope;

    }

    /** Fire all the refinements in this event once, if there is any. The
     *  refinements can be either ERG models or actor models. For ERG models,
     *  the initial events in them are scheduled for immediate processing.
     *
     *  @exception IllegalActionException If the refinements cannot be obtained,
     *  or the refinements cannot be fired.
     */
    protected void _fireRefinements() throws IllegalActionException {
        ERGController controller = (ERGController) getContainer();
        ERGDirector director = controller.director;

        Actor[] refinements = getRefinement();
        if (refinements != null) {
            for (Actor refinement : refinements) {
                if (refinement instanceof ERGController) {
                    ((ERGController) refinement).director._initializeSchedule();
                    director.fireAt(refinement, director.getModelTime());
                } else {
                    if (refinement.prefire()) {
                        refinement.fire();
                        refinement.postfire();
                    }
                }
            }
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
    protected ParserScope _getParserScope(ArrayToken arguments)
            throws IllegalActionException {
        ERGController controller = (ERGController) getContainer();
        ParserScope scope = controller.getPortScope();

        List<?> names = parameters.getParameterNames();
        int paramCount = names == null ? 0 : names.size();
        int argCount = arguments == null ? 0 : arguments.length();
        if (paramCount > argCount) {
            throw new IllegalActionException(this, "The number of arguments to "
                    + "this event must be greater than or equal to the number "
                    + "of declared parameters, which is " + paramCount + ".");
        }

        if (paramCount > 0) {
            Map<String, Token> parameterMap = new HashMap<String, Token>();
            Iterator<?> namesIter = names.iterator();
            Type[] types = parameters.getParameterTypes();
            for (int i = 0; namesIter.hasNext(); i++) {
                String name = (String) namesIter.next();
                Token argument = arguments.getElement(i);
                if (!types[i].isCompatible(argument.getType())) {
                    throw new IllegalActionException(this, "Argument " + (i + 1)
                            + "must have type " + types[i]);
                }
                parameterMap.put(name, argument);
            }
            scope = new ParameterParserScope(parameterMap, scope);
        }
        return scope;
    }

    /** Schedule the next events by evaluating all scheduling relations from
     *  this event.
     *
     *  @param scope The parser scope used in the evaluation.
     *  @exception IllegalActionException If the scheduling relations cannot be
     *  evaluated.
     */
    protected void _scheduleEvents(ParserScope scope)
            throws IllegalActionException {
        ERGController controller = (ERGController) getContainer();
        ERGDirector director = controller.director;

        List<?>[] schedulesArray = new List<?>[2];
        schedulesArray[0] = preemptiveTransitionList();
        schedulesArray[1] = nonpreemptiveTransitionList();
        for (List<?> schedules : schedulesArray) {
            for (Object scheduleObject : schedules) {
                SchedulingRelation schedule =
                    (SchedulingRelation) scheduleObject;
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

    /** List of objects whose (pre)initialize() and wrapup() methods should be
     *  slaved to these.
     */
    private transient List<Initializable> _initializables;

    /** The variables that this event has been monitoring. */
    private List<Variable> _monitoredVariables;

    /** Whether this event is monitoring the change of values of the monitored
    variables. */
    private boolean _monitoring = false;
}
