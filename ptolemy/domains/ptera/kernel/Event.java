/* Events in the Ptera controller.

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

package ptolemy.domains.ptera.kernel;

import java.util.List;

import ptolemy.actor.TypedActor;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Constants;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

/**
 A Ptera event is contained by a Ptera controller in a Ptera modal model.
 Each event can be placed in the Ptera controller's event queue when it is
 scheduled. When the model time reaches the time of the scheduled event, the
 event is removed from the event queue and is processed. Actions can be
 associated with each event, so that processing an event causes those actions to
 be executed.
 <p>
 An event may schedule another event after a 0 or greater delay of model time.
 This scheduling relation may be guarded by a Boolean expression. When an event
 schedules another, processing the first one causes the second one to be placed
 in the same Ptera controller's event queue. An event may also cancel another
 event that was previously placed in the same Ptera controller's event queue but
 has not been processed yet.
 <p>
 If the <code>fireOnInput</code> parameter of an event is set to true, then the
 event is also placed in its Ptera controller's input event queue when
 scheduled. When the Ptera controller receives an input at any of its input
 ports, all the events in its input event queue are removed and processed. Those
 events can use the value of that input in their guards or actions.
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
 This means, if the event is scheduled in the Ptera controller's event queue and
 a change is made on the variable (e.g., by the user or by actors such as {@link
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
public class Event extends State {

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
    }

    /** React to a change in an attribute. If the changed attribute is
     *  the <code>parameters</code> attribute, update the parser scope for the
     *  actions so that the parameters' names can be referred to in those
     *  actions. If it is <code>monitoredVariables</code>, register this event
     *  as a value listener of all the monitored variables. If the changed
     *  attribute is <code>isInitialState</code>, do nothing. This is because
     *  the Ptera controller need not be updated with this attribute is set. If
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

        if (attribute == parameters) {
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
     *  @return A refiring data structure that contains a non-negative double
     *   number if refire() should be called after that amount of model time, or
     *   null if refire() need not be called.
     *  @exception IllegalActionException If the number of the arguments or
     *   their types do not match, the actions cannot be executed, or any
     *   expression (such as guards and arguments to the next events) cannot be
     *   evaluated.
     *  @see #refire(ArrayToken, RefiringData)
     */
    public RefiringData fire(ArrayToken arguments)
            throws IllegalActionException {
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

        return null;
    }

    /** Return the Ptera controller that contains this event.
     *
     *  @return The Ptera controller.
     */
    public PteraController getController() {
        NamedObj container = getContainer();
        if (container instanceof PteraController) {
            return (PteraController) container;
        } else {
            return null;
        }
    }

    /** Return whether this event is a final event, so that its execution causes
     *  the event queue of the Ptera director to be cleared.
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
     *  automatically scheduled at model time 0 in the Ptera director's event
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

    /** Continue the processing of this event with the given arguments from the
     *  previous fire() or refire(). The number of arguments
     *  provided must be equal to the number of formal parameters defined for
     *  this event, and their types must match. The actions of this event are
     *  executed.
     *
     *  @param arguments The arguments used to process this event.
     *  @param data The refiring data structure returned by the previous fire()
     *   or refire().
     *  @return A refiring data structure that contains a non-negative double
     *   number if refire() should be called after that amount of model time, or
     *   null if refire() need not be called.
     *  @exception IllegalActionException If the number of the arguments or
     *   their types do not match, the actions cannot be executed, or any
     *   expression (such as guards and arguments to the next events) cannot be
     *   evaluated.
     *  @see #fire(ArrayToken)
     */
    public RefiringData refire(ArrayToken arguments, RefiringData data)
            throws IllegalActionException {
        return null;
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
     *  in the containing Ptera controller's event queue, if it is in it.
     *  <p>
     *  All the scheduling relations from this events are tested with their
     *  guards. If a scheduling relation's guard returns true, then the event
     *  that it points to is scheduled to occur after the amount of model time
     *  specified by the scheduling relation's delay parameter.
     *
     *  @exception IllegalActionException If the scheduling relations cannot be
     *   evaluated.
     */
    public void scheduleEvents() throws IllegalActionException {
        for (SchedulingRelation relation :
                (List<SchedulingRelation>) preemptiveTransitionList()) {
            _scheduleEvent(relation);
        }
        for (SchedulingRelation relation :
                (List<SchedulingRelation>) nonpreemptiveTransitionList()) {
            _scheduleEvent(relation);
        }
    }

    /** Request that the event cease execution altogether.
     */
    public void stop() {
    }

    /** The actions for this event. */
    public ActionsAttribute actions;

    /** A Boolean parameter to specify whether this event is a final event. */
    public Parameter isFinalEvent;

    /** A Boolean parameter to specify whether this event is an initial
    event. */
    public Parameter isInitialEvent;

    /** A list of formal parameters. */
    public ParametersAttribute parameters;

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

    //////////////////////////////////////////////////////////////////////////
    //// RefiringData

    /**
     A data structure to store the model time advance for the refire() method to
     be called. This data structure is returned by fire() and refire().

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 7.1
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    public static class RefiringData {

        /** Construct a refiring data structure.
         *
         *  @param timeAdvance The time advance for the next refire() call.
         */
        public RefiringData(double timeAdvance) {
            _timeAdvance = timeAdvance;
        }

        /** Return the time advance.
         *
         *  @return The time advance.
         */
        public double getTimeAdvance() {
            return _timeAdvance;
        }

        /** The time advance. */
        private double _timeAdvance;
    }

    //////////////////////////////////////////////////////////////////////////
    //// EventParameter

    /** Return the parser scope used to evaluate the actions and values
     *  associated with scheduling relations. The parser scope can evaluate
     *  names in the parameter list of this event. The values for those names
     *  are obtained from the given array of arguments.
     *
     *  @return The parser scope.
     *  arguments do not match those of the defined parameters.
     */
    protected ParserScope _getParserScope() {
        if (_parserScope == null || _parserScopeVersion != _workspace
                .getVersion()) {
            _parserScope = new VariableScope(this, getController()
                    .getPortScope());
            TypedActor[] refinements;
            try {
                refinements = getRefinement();
            } catch (IllegalActionException e) {
                throw new InternalErrorException(this, e,
                        "Unable to get refinements.");
            }
            if (refinements != null) {
                for (int i = refinements.length - 1; i >= 0; i--) {
                    _parserScope = new VariableScope((NamedObj) refinements[i],
                            _parserScope);
                }
            }
            _parserScopeVersion = _workspace.getVersion();
        }
        return _parserScope;
    }

    /** Return whether the refinement is active. In this base class, any
     *  refinement is active, so true is returned all the time. Subclasses may
     *  override this method to ignore the automatic firing of some refinements.
     *
     *  @param refinement The refinement to be tested.
     *  @return True if the refinement is active; false otherwise.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    protected boolean _isActiveRefinement(TypedActor refinement)
            throws IllegalActionException {
        return true;
    }

    /** Schedule the event that is pointed to by the given relation, if the
     *  guard returns true.
     *
     *  @param relation The scheduling relation.
     *  @exception IllegalActionException If the scheduling relation cannot be
     *   evaluated.
     */
    protected void _scheduleEvent(SchedulingRelation relation)
            throws IllegalActionException {
        PteraController controller = getController();
        if (controller == null) {
            throw new IllegalActionException(this, "To schedule events, the " +
                    "container must be a PteraController.");
        }
        PteraDirector director = controller.director;
        ParserScope scope = _getParserScope();
        if (relation.isEnabled(scope)) {
            double delay = relation.getDelay(scope);
            Event nextEvent = (Event) relation.destinationState();
            if (relation.isCanceling()) {
                director.cancel(nextEvent);
            } else {
                int priority = ((IntToken) relation.priority.getToken())
                        .intValue();
                boolean reset = ((BooleanToken) relation.reset.getToken())
                        .booleanValue();
                ArrayToken edgeArguments = relation.getArguments(scope);
                director.fireAt(nextEvent, director.getModelTime().add(delay),
                        edgeArguments, relation.getTriggers(), priority, reset);
            }
        }
    }

    /** The parser scope used to execute actions and evaluate arguments.
     */
    private VariableScope _parserScope;

    /** Version of _parserScope.
     */
    private long _parserScopeVersion = -1;
}
