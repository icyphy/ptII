/* A transition in an FSMActor.

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
@ProposedRating Yellow (liuxj@eecs.berkeley.edu)
@AcceptedRating Yellow (liuxj@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.kernel;

import ptolemy.actor.TypedActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.UnknownResultException;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StreamListener;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

// FIXME: Replace StringAttribute with lazy variables, and remove _guard.
// Other places where this could be done?

//////////////////////////////////////////////////////////////////////////
//// Transition
/**
A Transition has a source state and a destination state. A
transition has a guard expression and a trigger expression. Both
expressions should evaluate to a boolean value. The trigger of a
transition must be true whenever the guard is true. A transition is
enabled and can be taken when its guard is true.  A transition is
triggered and must be taken when its trigger is true.  

<p> A transition can contain actions. The way to specify actions is
to give value to the <i>outputActions</i> parameter and the
<i>setActions</i> parameter.

The value of these parameters is a string of the form:
<pre>
     <i>command</i>; <i>command</i>; ...
</pre>
where each <i>command</i> has the form:
<pre>
     <i>destination</i> = <i>expression</i>
</pre>
For the <i>outputActions</i> parameter, <i>destination</i> is either
<pre>
     <i>portName</i>
</pre>
or
<pre>
   <i>portName</i>(<i>channelNumber</i>)
   </pre>
   Here, <i>portName</i> is the name of a port of the FSM actor,
   If no <i>channelNumber</i> is given, then the value
   is broadcast to all channels of the port.
   <p>
   For the <i>setActions</i> parameter, <i>destination</i> is
<pre>
     <i>variableName</i>
</pre>
<i>variableName</i> identifies either a variable or parameter of
the FSM actor, or a variable or parameter of the refinement of the
destination state of the transition. To give a variable of the
refinement, use a dotted name, as follows:
<pre>
     <i>refinementName</i>.<i>variableName</i>
</pre>
The <i>expression</i> is a string giving an expression in the usual
Ptolemy II expression language. The expression may include references
to variables and parameters contained by the FSM actor.
<p>
The <i>outputActions</i> and <i>setActions</i> parameters are not the only
ways to specify actions. In fact, you can add action attributes that are
instances of anything that inherits from Action.
(Use the Add button in the Edit Parameters dialog).
<p>
An action is either a ChoiceAction or a CommitAction. The <i>setActions</i>
parameter is a CommitAction, whereas the <i>outputActions</i> parameter is a
ChoiceAction. A commit action is executed when the transition is taken to
change the state of the FSM, in the postfire() method of FSMActor.
A choice action, by contrast, is executed in the fire() method
of the FSMActor when the transition is chosen, but not yet taken.
The difference is subtle, and for most domains, irrelevant.
A few domains, however, such as CT, which have fixed point semantics,
where the fire() method may be invoked several times before the
transition is taken (committed). For such domains, it is useful
to have actions that fulfill the ChoiceAction interface.
Such actions participate in the search for a fixed point, but
do not change the state of the FSM.
<p>
A transition can be preemptive or non-preemptive. When a preemptive transition
is chosen, the refinement of its source state is not fired. A non-preemptive
transition is only chosen after the refinement of its source state is fired.
<p>
The <i>reset</i> parameter specifies whether the refinement of the destination
state is reset when the transition is taken. There is no reset() method in the
Actor interface, so the initialize() method of the refinement is called. Please
note that this feature is still under development.

@author Xiaojun Liu and Edward A. Lee
@version $Id$
@since Ptolemy II 0.4
@see State
@see Action
@see ChoiceAction
@see CommitAction
@see CommitActionsAttribute
@see FSMActor
@see OutputActionsAttribute
*/
public class Transition extends ComponentRelation {

    /** Construct a transition in the given workspace with an empty string
     *  as a name.
     *  If the workspace argument is null, use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version of the workspace.
     *  @param workspace The workspace for synchronization and version
     *  tracking.
     */
    public Transition(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    /** Construct a transition with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown. This transition will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  @param container The container.
     *  @param name The name of the transition.
     *  @exception IllegalActionException If the container is incompatible
     *   with this transition.
     *  @exception NameDuplicationException If the name coincides with
     *   any relation already in the container.
     */
    public Transition(FSMActor container, String name) 
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
            }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    
    /** Attribute the exit angle of a visual rendition.
     *  This parameter contains a DoubleToken, initially with value PI/5.
     *  It must lie between -PI and PI.  Otherwise, it will be truncated
     *  to lie within this range.
     */
    public Parameter exitAngle;

    /** Attribute giving the orientation of a self-loop. This is equal to
     * the tangent at the midpoint (more or less).
     *  This parameter contains a DoubleToken, initially with value 0.0.
     */
    public Parameter gamma;

    /** Attribute specifying the guard expression.
     */
    public StringAttribute guardExpression = null;

    /** The action commands that produce outputs when the transition is taken.
     */
    public OutputActionsAttribute outputActions;

    /** Parameter specifying whether this transition is preemptive.
     */
    public Parameter preemptive = null;

    /** Parameter specifying whether the refinement of the destination
     *  state is reset when the transition is taken.
     */
    public Parameter reset = null;

    /** The action commands that set parameters when the transition is taken.
     */
    public CommitActionsAttribute setActions;

    /** Attribute specifying the trigger expression.
     */
    public StringAttribute triggerExpression = null;

    /** Attribute specifying one or more names of refinements. The
     *  refinements must be instances of TypedActor and have the same
     *  container as the FSMActor containing this state, otherwise
     *  an exception will be thrown when getRefinement() is called.
     *  Usually, the refinement is a single name. However, if a
     *  comma-separated list of names is provided, then all the specified
     *  refinements will be executed.
     *  This attribute has a null expression or a null string as
     *  expression when the state is not refined.
     */
    public StringAttribute refinementName = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute is
     *  the <i>preemptive</i> parameter, evaluate the parameter. If the
     *  parameter is given an expression that does not evaluate to a
     *  boolean value, throw an exception; otherwise increment the
     *  version number of the workspace.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If thrown by the superclass
     *   attributeChanged() method, or the changed attribute is the
     *   <i>preemptive</i> parameter and is given an expression that
     *   does not evaluate to a boolean value.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);
        if (attribute == preemptive) {
            // evaluate the parameter to make sure it is given a valid
            // expression
            preemptive.getToken();
            workspace().incrVersion();
        }
        // The guard and trigger expressions can only be evaluated at run
        // time, because the input variables they can reference are created
        // at run time. guardExpression and triggerExpression are string
        // attributes used to convey expressions without being evaluated.
        // _guard and _trigger are the variables that do the evaluation.
        if (attribute == guardExpression) {
            String expr = guardExpression.getExpression();
            _guard.setExpression(expr);

            // If the executive director is HSDirector, 
            if (_exeDirectorIsHSDirector) {                
                // Invalid a relation list for the transition.
                _relationList.destroy();
                // Reconstruct the relation list.
                _parseTreeEvaluator.setEvaluationMode(true);
            }
        }
        if (attribute == triggerExpression) {
            String expr = triggerExpression.getExpression();
            _trigger.setExpression(expr);
        }
        if (attribute == refinementName) {
            _refinementVersion = -1;
        }

        if (attribute == outputActions && _debugging)
            outputActions.addDebugListener(new StreamListener());
        if (attribute == setActions && _debugging)
            setActions.addDebugListener(new StreamListener());
    }

    /** Return the list of choice actions contained by this transition.
     *  @return The list of choice actions contained by this transition.
     */
    public List choiceActionList() {
        if (_actionListsVersion != workspace().getVersion()) {
            _updateActionLists();
        }
        return _choiceActionList;
    }

    /** Clone the transition into the specified workspace. This calls the
     *  base class and then sets the attribute public members to refer to
     *  the attributes of the new transition.
     *  @param workspace The workspace for the new transition.
     *  @return A new transition.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
                Transition newObject = (Transition)super.clone(workspace);
                newObject.guardExpression =
                (StringAttribute)newObject.getAttribute("guardExpression");
                newObject.preemptive = (Parameter)newObject.getAttribute("preemptive");
                newObject.triggerExpression =
                (StringAttribute)newObject.getAttribute("triggerExpression");
                newObject.refinementName =
                (StringAttribute)newObject.getAttribute("refinementName");
                newObject._guard = (Variable)newObject.getAttribute("_guard");

                newObject._trigger = (Variable)newObject.getAttribute("_trigger");
                newObject._actionListsVersion = -1;
                newObject._choiceActionList = new LinkedList();
                newObject._commitActionList = new LinkedList();
                newObject._stateVersion = -1;
                return newObject;
            }

    /** Return the list of commit actions contained by this transition.
     *  @return The list of commit actions contained by this transition.
     */
    public List commitActionList() {
        if (_actionListsVersion != workspace().getVersion()) {
            _updateActionLists();
        }
        return _commitActionList;
    }

    /** Return the destination state of this transition.
     *  @return The destination state of this transition.
     */
    public State destinationState() {
        if (_stateVersion != workspace().getVersion()) {
            _checkConnectedStates();
        }
        return _destinationState;
    }

    /** Return the guard expression. The guard expression should evaluate
     *  to a boolean value.
     *  @return The guard expression.
     */
    public String getGuardExpression() {
        return _guard.getExpression();
    }

    /** Return a string describing this transition. The string has two lines.
     *  The first line is the guard expression. The second line is the
     *  concatenation of the expressions of <i>outputActions</i> and
     *  <i>setActions</i>.
     *  @return A string describing this transition.
     */
    public String getLabel() {
        StringBuffer buffer = new StringBuffer();
        boolean aLabel = false;
        String guard = getGuardExpression();
        if (guard != null) {
            buffer.append(guard);
            aLabel = true;
        }
        String action = null;
        String expression = outputActions.getExpression();
        if (expression != null && !expression.trim().equals("")) {
            action = expression;
        }
        expression = setActions.getExpression();
        if (expression != null && !expression.trim().equals("")) {
            if (action != null) {
                action = action + "; " + expression;
            } else {
                action = expression;
            }
        }
        if (action != null) {
            if (aLabel) buffer.append("\n");
            buffer.append(action);
            aLabel = true;
        }
        if (aLabel) {
            return buffer.toString();
        } else {
            return "";
        }
    }

    /** Return the refinements of this transition. The names of the refinements
     *  are specified by the <i>refinementName</i> attribute. The refinements
     *  must be instances of TypedActor and have the same container as
     *  the FSMActor containing this state, otherwise an exception is thrown.
     *  This method can also return null if there is no refinement.
     *  This method is read-synchronized on the workspace.
     *  @return The refinements of this state, or null if there are none.
     *  @exception IllegalActionException If the specified refinement
     *   cannot be found, or if a comma-separated list is malformed.
     */
    public TypedActor[] getRefinement() throws IllegalActionException {
        if (_refinementVersion == workspace().getVersion()) {
            return _refinement;
        }
        try {
            workspace().getReadAccess();
            String names = refinementName.getExpression();
            if (names == null || names.trim().equals("")) {
                _refinementVersion = workspace().getVersion();
                _refinement = null;
                return null;
            }
            StringTokenizer tokenizer = new StringTokenizer(names, ",");
            int size = tokenizer.countTokens();
            if (size <= 0) {
                _refinementVersion = workspace().getVersion();
                _refinement = null;
                return null;
            }
            _refinement = new TypedActor[size];
            Nameable container = getContainer();
            TypedCompositeActor containerContainer =
            (TypedCompositeActor)container.getContainer();
            int index = 0;
            while (tokenizer.hasMoreTokens()) {
                String name = tokenizer.nextToken().trim();
                if (name.equals("")) {
                    throw new IllegalActionException(this,
                            "Malformed list of refinements: " + names);
                }
                TypedActor element =
                    (TypedActor)containerContainer.getEntity(name);
                if (element == null) {
                    throw new IllegalActionException(this, "Cannot find "
                            + "refinement with name \"" + name
                            + "\" in " + containerContainer.getFullName());
                }
                _refinement[index++] = element;
            }
            _refinementVersion = workspace().getVersion();
            return _refinement;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return the trigger expression. The trigger expression should evaluate
     *  to a boolean value.
     *  @return The trigger expression.
     */
    public RelationList getRelationList() {
        return _relationList;
    }

    /** Return the trigger expression. The trigger expression should evaluate
     *  to a boolean value.
     *  @return The trigger expression.
     */
    public String getTriggerExpression() {
        return _trigger.getExpression();
    }

    /** Return true if the transition is enabled, that is the guard is true, or
     *  some event has been detected due to crossing some level.
     *  @return True if the transition is enabled and some event is detected.
     *  @exception IllegalActionException If thrown when evaluating the guard.
     */
    public boolean isEnabled() throws IllegalActionException {
        try {
            if (_exeDirectorIsHSDirector && !_relationList.isEmpty()) {
                _parseTreeEvaluator.setEvaluationMode(false);
            }
            Token token = _guard.getToken();
            if (token == null) {
                return false;
            }
            //FIXME: deal with continuous varialbes and discrete variables
            // using signalType.
            boolean result = ((BooleanToken)token).booleanValue();
            return result;
        } catch (UnknownResultException ex) {
            return false;
        }
    }

    /** Return true if this transition is preemptive. Whether this transition
     *  is preemptive is specified by the <i>preemptive</i> parameter.
     *  @return True if this transition is preemptive.
     */
    public boolean isPreemptive() {
        try {
            return ((BooleanToken)preemptive.getToken()).booleanValue();
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(preemptive.getFullName()
                    + ": The parameter does not have a valid value, \""
                    + preemptive.getExpression() + "\".");
        }
    }

    /** Return true if the transition is triggered.
     *  @return True if the transition is triggered.
     *  @exception IllegalActionException If thrown when evaluating the
     *   trigger, or the trigger is true but the guard is false.
     */
    public boolean isTriggered() throws IllegalActionException {
        Token token = _trigger.getToken();
        boolean result = ((BooleanToken)token).booleanValue();
        token = _guard.getToken();
        boolean guardValue = ((BooleanToken)token).booleanValue();
        if (result == true && guardValue == false) {
            throw new IllegalActionException(this, "The trigger: "
                    + getTriggerExpression() + " is true but the guard: "
                    + getGuardExpression() + " is false.");
        }
        return result;
    }

    /** Override the base class to ensure that the proposed container
     *  is an instance of FSMActor or null; if it is null, then
     *  remove it from the container, and also remove any refinement(s)
     *  that it references that are not referenced by some other
     *  transition or state.
     *
     *  @param container The proposed container.
     *  @exception IllegalActionException If the transition would result
     *   in a recursive containment structure, or if
     *   this transition and container are not in the same workspace, or
     *   if the argument is not a FSMActor or null.
     *  @exception NameDuplicationException If the container already has
     *   an relation with the name of this transition.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (container != null) {
            if (!(container instanceof FSMActor)) {
                throw new IllegalActionException(container, this,
                        "Transition can only be contained by instances of "
                        + "FSMActor.");
            }
        }
        super.setContainer(container);
    }

    /** Set the guard expression. The guard expression should evaluate
     *  to a boolean value.
     *  @param expression The guard expression.
     */
    public void setGuardExpression(String expression) {
        try {
            guardExpression.setExpression(expression);
            guardExpression.validate();
        } catch (IllegalActionException ex) {
            throw new InternalErrorException("Error in setting the "
                    + "guard expression of a transition.");
        }
    }

    /** Set the trigger expression. The trigger expression should evaluate
     *  to a boolean value.
     *  @param expression The trigger expression.
     */
    public void setTriggerExpression(String expression) {
        try {
            triggerExpression.setExpression(expression);
            triggerExpression.validate();
        } catch (IllegalActionException ex) {
            throw new InternalErrorException("Error in setting the "
                    + "trigger expression of a transition.");
        }
    }

    /** Return the source state of this transition.
     *  @return The source state of this transition.
     */
    public State sourceState() {
        if (_stateVersion != workspace().getVersion()) {
            _checkConnectedStates();
        }
        return _sourceState;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Throw an IllegalActionException if the port cannot be linked
     *  to this transition. A transition has a source state and a
     *  destination state. A transition is only linked to the outgoing
     *  port of its source state and the incoming port of its destination
     *  state.
     *  @exception IllegalActionException If the port cannot be linked
     *   to this transition.
     */
    protected void _checkPort(Port port) throws IllegalActionException {
        super._checkPort(port);
        if (!(port.getContainer() instanceof State)) {
            throw new IllegalActionException(this, port.getContainer(),
                    "Transition can only connect to instances of State.");
        }
        State st = (State)port.getContainer();
        if (port != st.incomingPort && port != st.outgoingPort) {
            throw new IllegalActionException(this, port.getContainer(),
                    "Transition can only be linked to incoming or outgoing "
                    + "port of State.");
        }
        if (numLinks() == 0) {
            return;
        }
        if (numLinks() >= 2) {
            throw new IllegalActionException(this,
                    "Transition can only connect two States.");
        }
        Iterator ports = linkedPortList().iterator();
        Port pt = (Port)ports.next();
        State s = (State)pt.getContainer();
        if ((pt == s.incomingPort && port == st.incomingPort) ||
                (pt == s.outgoingPort && port == st.outgoingPort)) {
            throw new IllegalActionException(this,
                    "Transition can only have one source and one destination.");
        }
        return;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Check the states connected by this transition, cache the result.
    // This method is read-synchronized on the workspace.
    private void _checkConnectedStates() {
        try {
            workspace().getReadAccess();
            Iterator ports = linkedPortList().iterator();
            _sourceState = null;
            _destinationState = null;
            while (ports.hasNext()) {
                Port p = (Port)ports.next();
                State s = (State)p.getContainer();
                if (p == s.incomingPort) {
                    _destinationState = s;
                } else {
                    _sourceState = s;
                }
            }
            _stateVersion = workspace().getVersion();
        } finally {
            workspace().doneReading();
        }
    }
    
    // Initialize the variables of this transition.
    private void _init()
            throws IllegalActionException, NameDuplicationException {
        guardExpression = new StringAttribute(this, "guardExpression");
        outputActions = new OutputActionsAttribute(this, "outputActions");
        setActions = new CommitActionsAttribute(this, "setActions");
        exitAngle = new Parameter(this, "exitAngle");
        exitAngle.setVisibility(Settable.NONE);
        exitAngle.setExpression("PI/5.0");
        exitAngle.setTypeEquals(BaseType.DOUBLE);
        gamma = new Parameter(this, "gamma");
        gamma.setVisibility(Settable.NONE);
        gamma.setExpression("0.0");
        gamma.setTypeEquals(BaseType.DOUBLE);
        reset = new Parameter(this, "reset");
        reset.setTypeEquals(BaseType.BOOLEAN);
        reset.setToken(BooleanToken.FALSE);
        preemptive = new Parameter(this, "preemptive");
        preemptive.setTypeEquals(BaseType.BOOLEAN);
        preemptive.setToken(BooleanToken.FALSE);
        triggerExpression = new StringAttribute(this, "triggerExpression");
        triggerExpression.setVisibility(Settable.NONE);
        _guard = new Variable(this, "_guard");
        // Make the variable lazy since it will often have
        // an expression that cannot be evaluated.
        _guard.setLazy(true);
        _guard.setTypeEquals(BaseType.BOOLEAN);
        
        _exeDirectorIsHSDirector = false;
        
        // Depending on whether the director is FSM director
        // or HSDirector, we configure the Transitions with 
        // different ParseTreeEvaluators.
        
        CompositeEntity container = (CompositeEntity) getContainer();
        
        // If the executive director is HSDirector, 
        if (container != null) {
            TypedCompositeActor modalModel =
                (TypedCompositeActor)container.getContainer();
            if (modalModel != null 
                    && modalModel.getDirector() instanceof HSDirector) {
            
                // FIXME: This is wrong...  what if the director changes?
                _exeDirectorIsHSDirector = true;
                
                // construct a relation list for the transition;
                _relationList = new RelationList(this, "relationList");
                
                // associate the relation list with the
                // ParseTreeEvaluatorForGuardExpression
                
                // FIXME: how to get the error tolerance
                // If we limite the HSDirector only works under CT model
                // or Modal Models, we can use the error tolerance from
                // the top level CT director.
                
                _parseTreeEvaluator = new ParseTreeEvaluatorForGuardExpression(
                        _relationList, 1e-4);
                
                // Register the guard expression with the above parse
                // tree evaluator
                _guard.setParseTreeEvaluator( 
                        (ParseTreeEvaluator) _parseTreeEvaluator);
            }
        }
        
        // If the executive director is FSMDirector, do nothing.
                
        _trigger = new Variable(this, "_trigger");
        // Make the variable lazy since it will often have
        // an expression that cannot be evaluated.
        _trigger.setLazy(true);
        _trigger.setTypeEquals(BaseType.BOOLEAN);
        // add refinement name parameter
        refinementName = new StringAttribute(this, "refinementName");
    }

    // Update the cached lists of actions.
    // This method is read-synchronized on the workspace.
    private void _updateActionLists() {
        try {
            workspace().getReadAccess();
            _choiceActionList.clear();
            _commitActionList.clear();
            Iterator actions = attributeList(Action.class).iterator();
            while (actions.hasNext()) {
                Action action = (Action)actions.next();
                if (action instanceof ChoiceAction) {
                    _choiceActionList.add(action);
                }
                if (action instanceof CommitAction) {
                    _commitActionList.add(action);
                }
            }
            _actionListsVersion = workspace().getVersion();
        } finally {
            workspace().doneReading();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Version of cached lists of actions.
    private long _actionListsVersion = -1;

    // Cached list of choice actions contained by this transition.
    private List _choiceActionList = new LinkedList();

    // Cached list of commit actions contained by this Transition.
    private List _commitActionList = new LinkedList();

    // Cached destination state of this transition.
    private State _destinationState = null;

    // Variable for evaluating guard.
    private Variable _guard = null;

    // Flag to indicate whether the executive director is HSDirector.
    private boolean _exeDirectorIsHSDirector = false;
    
    // Set to true when the transition is preemptive.
    private boolean _preemptive = false;

    // Cached source state of this transition.
    private State _sourceState = null;

    // Version of cached source/destination state.
    private long _stateVersion = -1;

    // Variable for evaluating trigger.
    private Variable _trigger = null;

    // Cached reference to the refinement of this state.
    private TypedActor[] _refinement = null;

    // Version of the cached reference to the refinement.
    private long _refinementVersion = -1;

    // List of the relation expressions of a gurad expression
    private RelationList _relationList;

    // The parse tree evaluator for the transition.
    private ParseTreeEvaluatorForGuardExpression _parseTreeEvaluator;

}
