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
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

/**
 An ERG event is contained by an ERG controller in an ERG modal model. Each
 event can be placed in the ERG controller's event queue when it is scheduled.
 When the model time reaches the time of the scheduled event, the event is
 removed from the event queue and is processed. Actions can be associated with
 each event, so that processing an event causes those actions to be executed.
 <p>
 An event may schedule another event after a 0 or greater delay of model time.
 This scheduling relation may be guarded by a Boolean expression.
 When an event schedules another, processing the first one causes the second one
 to be placed in the same ERG controller's event queue. An event may also cancel
 another event that was previously placed in the same ERG controller's event
 queue but has not been processed yet.
 <p>
 If the <i>fireOnInput</i> parameter of an event is set to true, then the event
 is also placed in its ERG controller's input event queue when scheduled. When
 the ERG controller receives an input at any of its input ports, all the events
 in its input event queue are removed and processed. Those events can use the
 value of that input in their guards or actions.
 <p>
 An event may also define 0 or more formal parameters. When it is scheduled, on
 the scheduling relation, values to those parameters must be provided.
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
        isInitialState.setDisplayName("isInitialEvent");
        isFinalState.setDisplayName("isFinalEvent");

        parameters = new ParametersAttribute(this, "parameters");

        actions = new ActionsAttribute(this, "actions");
        Variable variable = new Variable(actions, "_textHeightHint");
        variable.setExpression("5");
        variable.setPersistent(false);

        parameters.setExpression("()");

        fireOnInput = new Parameter(this, "fireOnInput");
        fireOnInput.setToken(BooleanToken.FALSE);
        fireOnInput.setTypeEquals(BaseType.BOOLEAN);
    }

    /** React to a change in an attribute. If the changed attribute is
     *  the <i>parameters</i> attribute, update the parser scope for the actions
     *  so that the parameters' names can be referred to in those actions. If
     *  the changed attribute is <i>isInitialState</i>, do nothing. This is
     *  because the ERG controller need not be updated with this attribute is
     *  set. If the changed attribute is among the other attributes, then the
     *  superclass is called.
     *
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If thrown by the superclass
     *   attributeChanged() method, or the parser scope cannot be updated.
     */
    public void attributeChanged(Attribute attribute)
    throws IllegalActionException {
        if (attribute == parameters) {
            actions._updateParserScope();
        } else if (attribute != isInitialState) {
            super.attributeChanged(attribute);
        }
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

    /** The actions for this event. */
    public ActionsAttribute actions;

    /** A Boolean value representing whether this event is an input event. */
    public Parameter fireOnInput;

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
}
