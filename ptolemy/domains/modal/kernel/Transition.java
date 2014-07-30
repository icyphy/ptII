/* A transition in an FSMActor.

   Copyright (c) 1999-2014 The Regents of the University of California.
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
package ptolemy.domains.modal.kernel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.TypedActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.attributes.VersionAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StreamListener;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLChangeRequest;

///////////////////////////////////////////////////////////////////
//// Transition

/**
   A Transition has a source state and a destination state. A
   transition has a guard expression, which is evaluated to a boolean value.
   Whenever a transition out of the current state
   is enabled, it must be taken in the current firing.
   That is, unlike some state machines formalisms, our guard is not just
   an enabler for the transition but rather a trigger for the transition.

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
   The <i>history</i> parameter specifies whether the refinement of the destination
   state refinement is initialized when the transition is taken. By default, this
   is false, which means that the destination refinement is initialized.
   If you change this to true, then the destination refinement will not be
   initialized, so when the state is re-entered, the refinement will
   continue executing where it left off.
   <p>
   The <i>nondeterministic</i> parameter specifies whether this transition is
   nondeterministic. Here nondeterministic means that this transition may not
   be the only enabled transition at a time. The default value is a boolean
   token with value as false, meaning that if this transition is enabled, it
   must be the only enabled transition.
   <p>
   The <i>immediateTransition</i> parameter, if given a value true, specifies
   that this transition is may be taken as soon as its source state is entered,
   in the same iteration. This may lead to transient states, where a state is
   passed through without ever becoming the current state.
   <p>
   The <i>defaultTransition</i> parameter, if given a value true, specifies
   that this transition is enabled if no other non-default
   transition is enabled and if its guard evaluates to true.
   <p>
   The <i>error</i> parameter, if given a value true, specifies
   that this transition is enabled if the refinement of the source state of
   the transition throws a model error or an exception
   while executing. The default value is a boolean
   token with value false. When such an exception or model error
   occurs, two variables are set that may be used in the guard
   or the output or set actions of this transition:
   <ul>
   <li> <i>errorMessage</i>: The error message (a string).
   <li> <i>errorClass</i>: The class of the exception thrown.
   </ul>
   In addition, if the exception is an instance of KernelException
   or a subclass (such as IllegalActionException), then a third
   variable is set:
   <ul>
   <li> <i>errorCause</i>: The Ptolemy object that caused the exception.
   </ul>
   The <i>errorCause</i> is made available as an ObjectToken on which
   you can invoke methods such as getName() in the guard or output
   or set actions of this transition.

   @author Xiaojun Liu, Edward A. Lee, Haiyang Zheng, Christian Motika
   @version $Id$
   @since Ptolemy II 8.0
   @Pt.ProposedRating Yellow (hyzheng)
   @Pt.AcceptedRating Red (hyzheng)
   @see State
   @see Action
   @see ChoiceAction
   @see CommitAction
   @see CommitActionsAttribute
   @see FSMActor
   @see OutputActionsAttribute
 */
public class Transition extends ComponentRelation {
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

    /** Construct a transition in the given workspace with an empty string
     *  as a name.
     *  If the workspace argument is null, use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version of the workspace.
     *  @param workspace The workspace for synchronization and version
     *  tracking.
     *  @exception IllegalActionException If the container is incompatible
     *   with this transition.
     *  @exception NameDuplicationException If the name coincides with
     *   any relation already in the container.
     */
    public Transition(Workspace workspace) throws IllegalActionException,
    NameDuplicationException {
        super(workspace);
        _init();
    }

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
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == preemptive) {
            // evaluate the parameter to make sure it is given a valid
            // expression
            preemptive.getToken();
            workspace().incrVersion();
        } else if (attribute == immediate) {
            _immediate = ((BooleanToken) immediate.getToken()).booleanValue();
        } else if (attribute == nondeterministic) {
            _nondeterministic = ((BooleanToken) nondeterministic.getToken())
                    .booleanValue();
        } else if (attribute == guardExpression) {
            // The guard expression can only be evaluated at run
            // time, because the input variables it can reference are created
            // at run time. The guardExpression is a string
            // attribute used to convey expressions without being evaluated.
            // _guard is the variable that does the evaluation.
            _guardParseTree = null;
            _guardParseTreeVersion = -1;
            _parseTreeEvaluatorVersion = -1;
        } else if (attribute == refinementName) {
            _refinementVersion = -1;
        } else if (attribute == outputActions || attribute == setActions) {
            _actionListsVersion = -1;
        } else {
            super.attributeChanged(attribute);
        }

        if (attribute == outputActions && _debugging) {
            outputActions.addDebugListener(new StreamListener());
        } else if (attribute == setActions && _debugging) {
            setActions.addDebugListener(new StreamListener());
        } else if (attribute == fsmTransitionParameterName) {
            if (((BooleanToken) showFSMTransitionParameter.getToken())
                    .booleanValue()) {
                _getFSMTransitionParameter();
                try {
                    _fsmTransitionParameter
                            .setName(((StringToken) fsmTransitionParameterName
                                    .getToken()).stringValue());
                } catch (NameDuplicationException e) {
                    throw new IllegalActionException(this, e.getCause(),
                            e.getLocalizedMessage());
                }
            }
        } else if (attribute == showFSMTransitionParameter) {
            if (((BooleanToken) showFSMTransitionParameter.getToken())
                    .booleanValue()) {
                _getFSMTransitionParameter();
                if (_fsmTransitionParameter != null) {
                    _fsmTransitionParameter.hide(false);
                    _fsmTransitionParameter.setPersistent(true);
                }
                showFSMTransitionParameter.setPersistent(true);
                fsmTransitionParameterName.setPersistent(true);
            } else {
                if (_fsmTransitionParameter != null) {
                    _fsmTransitionParameter.setPersistent(false);
                }
                showFSMTransitionParameter.setPersistent(false);
                fsmTransitionParameterName.setPersistent(false);
            }
        }
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
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Transition newObject = (Transition) super.clone(workspace);
        /*
          newObject.guardExpression = (StringAttribute) newObject
          .getAttribute("guardExpression");
          newObject.preemptive = (Parameter) newObject.getAttribute("preemptive");
          newObject.immediate = (Parameter) newObject.getAttribute("immediate");
          newObject.refinementName = (StringAttribute) newObject.getAttribute("refinementName");
         */
        newObject._actionListsVersion = -1;
        newObject._choiceActionList = new LinkedList();
        newObject._commitActionList = new LinkedList();
        newObject._destinationState = null;
        newObject._fsmTransitionParameter = null;
        newObject._guardParseTree = null;
        newObject._guardParseTreeVersion = -1;
        // newObject._historySet = false;
        newObject._parseTreeEvaluatorVersion = -1;
        newObject._parseTreeEvaluator = null;
        newObject._refinementVersion = -1;
        newObject._refinement = null;
        newObject._sourceState = null;
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

    /** Return the full label, which may include the guard expression,
     *  the output expression and the set actions.
     *  @return The full label
     */
    public String getFullLabel() {
        StringBuffer buffer = new StringBuffer("");
        boolean hasAnnotation = false;
        String text;
        try {
            text = annotation.stringValue();
        } catch (IllegalActionException e) {
            text = "Exception evaluating annotation: " + e.getMessage();
        }
        if (!text.trim().equals("")) {
            hasAnnotation = true;
            buffer.append(text);
        }

        String guard = getGuardExpression();
        if (guard != null && !guard.trim().equals("")) {
            if (hasAnnotation) {
                buffer.append("\n");
            }
            buffer.append("guard: ");
            buffer.append(guard);
        }

        String expression = outputActions.getExpression();
        if (expression != null && !expression.trim().equals("")) {
            buffer.append("\n");
            buffer.append("output: ");
            buffer.append(expression);
        }

        expression = setActions.getExpression();
        if (expression != null && !expression.trim().equals("")) {
            buffer.append("\n");
            buffer.append("set: ");
            buffer.append(expression);
        }
        return buffer.toString();
    }

    /** Return the guard expression. The guard expression should evaluate
     *  to a boolean value.
     *  @return The guard expression.
     *  @see #setGuardExpression
     */
    public String getGuardExpression() {
        return guardExpression.getExpression();
    }

    /** Return a string describing this transition. The string has up to
     *  three lines. The first line is the guard expression, preceded
     *  by "guard: ".  The second line is the <i>outputActions</i> preceded
     *  by the string "output: ". The third line is the
     *  <i>setActions</i> preceded by the string "set: ". If any of these
     *  is missing, then the corresponding line is omitted.
     *  @return A string describing this transition.
     */
    public String getLabel() {
        try {
            if (((BooleanToken) showFSMTransitionParameter.getToken())
                    .booleanValue()) {
                return ((StringToken) fsmTransitionParameterName.getToken())
                        .stringValue();
            } else {
                return getFullLabel();
            }
        } catch (IllegalActionException e) {
            return "Exception evaluating annotation: " + e.getMessage();
        }
    }

    /** Return the parse tree evaluator used by this transition to evaluate
     *  the guard expression.
     *  @return ParseTreeEvaluator for evaluating the guard expression.
     */
    public ParseTreeEvaluator getParseTreeEvaluator() {
        if (_parseTreeEvaluatorVersion != workspace().getVersion()) {
            // If there is no current parse tree evaluator,
            // then create one. If this transition is under the control
            // of an FSMDirector, then delegate creation to that director.
            // Otherwise, create a default instance of ParseTreeEvaluator.
            FSMDirector director = _getDirector();
            if (director != null) {
                _parseTreeEvaluator = director.getParseTreeEvaluator();
            } else {
                // When this transition is used inside an FSMActor.
                if (_parseTreeEvaluator == null) {
                    _parseTreeEvaluator = new ParseTreeEvaluator();
                }
            }
            _parseTreeEvaluatorVersion = workspace().getVersion();
        }
        return _parseTreeEvaluator;
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
            TypedCompositeActor containerContainer = (TypedCompositeActor) container
                    .getContainer();
            int index = 0;

            while (tokenizer.hasMoreTokens()) {
                String name = tokenizer.nextToken().trim();

                if (name.equals("")) {
                    throw new IllegalActionException(this,
                            "Malformed list of refinements: " + names);
                }

                TypedActor element = (TypedActor) containerContainer
                        .getEntity(name);

                if (element == null) {
                    throw new IllegalActionException(this, "Cannot find "
                            + "refinement with name \"" + name + "\" in "
                            + containerContainer.getFullName());
                }

                _refinement[index++] = element;
            }

            _refinementVersion = workspace().getVersion();
            return _refinement;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return true if this transition is a default transition. Return false
     *  otherwise.
     *  @return True if this transition is a default transition.
     *  @exception IllegalActionException If the default parameter
     *   cannot be evaluated.
     */
    public boolean isDefault() throws IllegalActionException {
        return ((BooleanToken) defaultTransition.getToken()).booleanValue();
    }

    /** Return true if the transition is enabled, that is the guard is true,
     *  and false if the guard evaluates to false.
     *  @return True If the transition is enabled and some event is detected.
     *  @exception IllegalActionException If the guard cannot be evaluated.
     */
    public boolean isEnabled() throws IllegalActionException {
        NamedObj container = getContainer();
        if (container instanceof FSMActor) {
            return isEnabled(((FSMActor) container).getPortScope());
        } else {
            return false;
        }
    }

    /** Return true if the transition is enabled, that is the guard is true,
     *  and false if the guard evaluates to false.
     *  @param scope The parser scope in which the guard is to be evaluated.
     *  @return True If the transition is enabled and some event is detected.
     *  @exception IllegalActionException If the guard cannot be evaluated.
     */
    public boolean isEnabled(ParserScope scope) throws IllegalActionException {
        ParseTreeEvaluator parseTreeEvaluator = getParseTreeEvaluator();
        if (_guardParseTree == null
                || _guardParseTreeVersion != _workspace.getVersion()) {
            String expr = getGuardExpression();
            // If the expression is empty, interpret this as true.
            if (expr.trim().equals("")) {
                return true;
            }
            // Parse the guard expression.
            PtParser parser = new PtParser();
            try {
                _guardParseTree = parser.generateParseTree(expr);
                _guardParseTreeVersion = _workspace.getVersion();
            } catch (IllegalActionException ex) {
                throw new IllegalActionException(this, ex,
                        "Failed to parse guard expression \"" + expr + "\"");
            }
        }
        Token token = parseTreeEvaluator.evaluateParseTree(_guardParseTree,
                scope);
        if (!(token instanceof BooleanToken)) {
            throw new IllegalActionException(this,
                    "Guard expression does not evaluate to a boolean!"
                            + " The gaurd expression is: \""
                            + guardExpression.getExpression()
                            + "\", which evaluates to " + token);
        }
        boolean result = ((BooleanToken) token).booleanValue();
        return result;
    }

    /** Return true if this transition is an error transition. Whether this
     *  transition an error transition is specified by the <i>error</i> parameter.
     *  @return True if this transition is an error transition.
     *  @exception IllegalActionException If the parameter cannot be evaluated.
     */
    public boolean isErrorTransition() throws IllegalActionException {
        return ((BooleanToken) error.getToken()).booleanValue();
    }

    /** Return true if this transition is a history transition.
     *  If the <i>history</i> parameter has been set to true, then return true.
     *  Otherwise, check to see whether the model was created by a Ptolemy version
     *  earlier than 9.1.devel. If it was not, return false, the new default.
     *  If it was, then look for a parameter named
     *  "reset", and set the history parameter to the complement of it
     *  and return that value.
     *  If there is no such parameter, then we have to assume the default
     *  behavior that prevailed before 9.1.devel, and set history to true.
     *  @return true if this transition is a history transition.
     *  @exception IllegalActionException If the value of the history parameter
     *   cannot be read.
     */
    public boolean isHistory() throws IllegalActionException {
        // Ensure that the corrections below for older version compatibility
        // are performed only once.
        if (_historySet) {
            return ((BooleanToken) history.getToken()).booleanValue();
        }
        _historySet = true;
        // History has not been explicitly set true. Should use either new
        // or old default depending on the version of Ptolemy that created the model.
        try {
            VersionAttribute version = (VersionAttribute) toplevel()
                    .getAttribute("_createdBy", VersionAttribute.class);
            if (version == null) {
                // No version attribute. Return whatever the value
                // of the history parameter is.
                return ((BooleanToken) history.getToken()).booleanValue();
            }
            if (_REFERENCE_VERSION == null) {
                _REFERENCE_VERSION = new VersionAttribute("9.0.devel");
            }
            if (version.compareTo(_REFERENCE_VERSION) <= 0) {
                // Model was created under old defaults. Look for a reset parameter.
                final Parameter reset = (Parameter) getAttribute("reset",
                        Parameter.class);
                if (reset != null) {
                    Token resetValue = reset.getToken();
                    // Remove the reset parameter.
                    // If the model is subsequently saved, then the history
                    // parameter will take over, and it will be confusing to
                    // also have a reset parameter that doesn't do anything.
                    try {
                        reset.setContainer(null);
                    } catch (NameDuplicationException e) {
                        // Should not happen. Ignore.
                    }
                    // If the value of the reset parameter is false,
                    // and the destination states have refinements,
                    // then set the history parameter to true.
                    // Otherwise, leave the history parameter at its default.
                    if (resetValue instanceof BooleanToken) {
                        boolean resetValueBoolean = ((BooleanToken) resetValue)
                                .booleanValue();
                        if (!resetValueBoolean) {
                            // reset parameter exists and has value false, but if the destination
                            // state has no refinement, we nonetheless change this to make
                            // the history parameter false.
                            State destinationState = destinationState();
                            if (destinationState != null) {
                                TypedActor[] refinements = destinationState
                                        .getRefinement();
                                if (refinements == null
                                        || refinements.length == 0) {
                                    // No need to make history true. Stick with the default.
                                    return false;
                                }
                            }
                            // Set the new parameter to its non-default value.
                            history.setExpression("true");
                            // Force this to be exported because this might be getting set
                            // during construction of the model, in which case, true will be
                            // assumed to be the default value.
                            history.setPersistent(true);
                            return true;
                        } else {
                            // No need to set the new parameter, since this is the default.
                            return false;
                        }
                    } else {
                        // Parameter reset exists, but is not a boolean. Old default is
                        // that history is true.
                        history.setExpression("true");
                        // Force this to be exported because this might be getting set
                        // during construction of the model, in which case, true will be
                        // assumed to be the default value.
                        history.setPersistent(true);
                        return true;
                    }
                } else {
                    // Parameter reset does not exist. Old default is
                    // that history is true. But this is only really required
                    // if the destination state has a refinement.
                    State destinationState = destinationState();
                    if (destinationState != null) {
                        TypedActor[] refinements = destinationState
                                .getRefinement();
                        if (refinements == null || refinements.length == 0) {
                            // No need to make history true. Stick with the default.
                            return false;
                        }
                    }
                    history.setExpression("true");
                    // Force this to be exported because this might be getting set
                    // during construction of the model, in which case, true will be
                    // assumed to be the default value.
                    history.setPersistent(true);
                    return true;
                }
            } else {
                // Version is recent. Return the current value of the history parameter.
                return ((BooleanToken) history.getToken()).booleanValue();
            }
        } catch (IllegalActionException e) {
            // Can't access version attribute. Return default.
            return ((BooleanToken) history.getToken()).booleanValue();
        }
    }

    /** Return true if this transition is immediate. Whether this transition
     *  is immediate is specified by the <i>immediateTransition</i> parameter.
     *  @return True if this transition is immediate.
     */
    public boolean isImmediate() {
        return _immediate;
    }

    /** Return true if this transition is nondeterministic. Return false
     *  otherwise.
     *  @return True if this transition is nondeterministic.
     */
    public boolean isNondeterministic() {
        return _nondeterministic;
    }

    /** Return true if this transition is a termination transition. Whether this
     *  transition a termination transition is specified by the <i>termination</i> parameter.
     *  @return True if this transition is a termination transition.
     *  @exception IllegalActionException If the parameter cannot be evaluated.
     */
    public boolean isTermination() throws IllegalActionException {
        return ((BooleanToken) termination.getToken()).booleanValue();
    }

    /** Return true if this transition is preemptive. Whether this transition
     *  is preemptive is specified by the <i>preemptive</i> parameter.
     *  @return True if this transition is preemptive.
     *  @exception IllegalActionException If the parameter cannot be evaluated.
     */
    public boolean isPreemptive() throws IllegalActionException {
        return ((BooleanToken) preemptive.getToken()).booleanValue();
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
    @Override
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (container != null) {
            if (!(container instanceof FSMActor)) {
                throw new IllegalActionException(container, this,
                        "Transition can only be contained by instances of "
                                + "FSMActor.");
            }
        } else {
            if (_fsmTransitionParameter != null) {
                _fsmTransitionParameter.setContainer(null);
            }
        }

        super.setContainer(container);
    }

    /** Set the guard expression. The guard expression should evaluate
     *  to a boolean value.
     *  @param expression The guard expression.
     *  @see #getGuardExpression
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

    /** Set the FSMTransitionParameter.
     * @param parameter The parameter.
     */
    public void setFsmTransitionParameter(FSMTransitionParameter parameter) {
        _fsmTransitionParameter = parameter;
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
    ////                         public variables                  ////

    /** An annotation that describes the transition. If this is non-empty,
     *  then a visual editor will be expected to put this annotation on
     *  or near the transition to document its function. This is a string
     *  that defaults to the empty string. Note that it can reference
     *  variables in scope using the notation $name.
     */
    public StringParameter annotation;

    /** Indicator that this transition is a default transition. A
     *  default transition is enabled only if no other non-default
     *  transition is enabled.  This is a boolean with default value
     *  false. If the value is true, then the guard expression is
     *  ignored.
     */
    public Parameter defaultTransition = null;

    /** Parameter specifying whether this transition should be treated
     *  as an error transition.  The default value is a boolean with
     *  the value false, which indicates that this transition is not
     *  an error transition.  If the value is true, that this transition
     *  is enabled if and only if the refinement of the source state of
     *  the transition throws a model error while executing.
     */
    public Parameter error = null;

    /** Attribute the exit angle of a visual rendition.
     *  This parameter contains a DoubleToken, initially with value PI/5.
     *  It must lie between -PI and PI.  Otherwise, it will be truncated
     *  to lie within this range.
     */
    public Parameter exitAngle;

    /** The name of the transition, which defaults to the name of
     *  the transition followed by the string "Parameter".
     */
    public Parameter fsmTransitionParameterName;

    /** Attribute giving the orientation of a self-loop. This is equal to
     * the tangent at the midpoint (more or less).
     *  This parameter contains a DoubleToken, initially with value 0.0.
     */
    public Parameter gamma;

    /** Attribute specifying the guard expression.
     */
    public StringAttribute guardExpression = null;

    /** Parameter specifying whether the refinements of the destination
     *  state are initialized when the transition is taken.
     *  This is a boolean that defaults to false.
     */
    public Parameter history;

    /** Parameter specifying whether this transition is immediate.
     */
    public Parameter immediate = null;

    /** Parameter specifying whether this transition is nondeterministic.
     *  Here nondeterministic means that this transition may not be the only
     *  enabled transition at a time. The default value is a boolean token
     *  with value as false, meaning that if this transition is enabled, it
     *  must be the only enabled transition.
     */
    public Parameter nondeterministic = null;

    /** The action commands that produce outputs when the transition is taken.
     */
    public OutputActionsAttribute outputActions;

    /** Parameter specifying whether this transition is preemptive.
     */
    public Parameter preemptive = null;

    /** Attribute specifying one or more names of refinements. The
     *  refinements must be instances of TypedActor and have the same
     *  container as the FSMActor containing this state, otherwise
     *  an exception will be thrown when getRefinement() is called.
     *  Usually, the refinement is a single name. However, if a
     *  comma-separated list of names is provided, then all the specified
     *  refinements will be executed.
     *  This attribute has a null expression or a null string as
     *  expression when the state is not refined.
     *  @deprecated Use immediate transitions.
     */
    @Deprecated
    public StringAttribute refinementName;

    /** The action commands that set parameters when the transition is taken.
     *  By default, this is empty.
     */
    public CommitActionsAttribute setActions;

    /** True of the the value of the {@link #fsmTransitionParameterName} parameter
     *  should be returned by {@link #getLabel()}.
     */
    public Parameter showFSMTransitionParameter;

    /** Parameter specifying whether the refinements of the origin
     *  state must have terminated (postfire has returned false)
     *  for the transition to be enabled.
     */
    public Parameter termination;

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
    @Override
    protected void _checkPort(Port port) throws IllegalActionException {
        super._checkPort(port);

        if (!(port.getContainer() instanceof State)) {
            throw new IllegalActionException(this, port.getContainer(),
                    "Transition can only connect to instances of State.");
        }

        State st = (State) port.getContainer();

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
        Port pt = (Port) ports.next();
        State s = (State) pt.getContainer();

        if (pt == s.incomingPort && port == st.incomingPort
                || pt == s.outgoingPort && port == st.outgoingPort) {
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
                Port p = (Port) ports.next();
                State s = (State) p.getContainer();

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

    /** Return the FSMDirector in charge of this transition,
     *  or null if there is none.
     *  @return The director in charge of this transition.
     */
    private FSMDirector _getDirector() {
        // Get the containing FSMActor.
        NamedObj container = getContainer();
        if (container != null) {
            // Get the containing modal model.
            CompositeActor modalModel = (CompositeActor) container
                    .getContainer();
            if (modalModel != null) {
                // Get the director for the modal model.
                Director director = modalModel.getDirector();
                if (director instanceof FSMDirector) {
                    return (FSMDirector) director;
                }
            }
        }
        return null;
    }

    private void _getFSMTransitionParameter() throws IllegalActionException {
        if (getContainer() != null) {
            if (_fsmTransitionParameter == null) {
                _fsmTransitionParameter = (FSMTransitionParameter) getContainer()
                        .getAttribute(
                                ((StringToken) fsmTransitionParameterName
                                        .getToken()).stringValue());
                if (_fsmTransitionParameter != null) {
                    try {
                        _fsmTransitionParameter.setTransition(this);
                    } catch (NameDuplicationException e) {
                        throw new IllegalActionException(this, e.getCause(),
                                e.getLocalizedMessage());
                    }
                }
            }
            if (_fsmTransitionParameter == null) {
                Location sourceStateLocation = (Location) sourceState()
                        .getAttribute("_location");
                Location destinationStateLocation = (Location) destinationState()
                        .getAttribute("_location");
                String moml = "<property name=\""
                        + ((StringToken) fsmTransitionParameterName.getToken())
                                .stringValue()
                        + "\" class=\"ptolemy.domains.modal.kernel.FSMTransitionParameter\">\n"
                        + "    <property name=\"_hideName\" class=\"ptolemy.kernel.util.SingletonAttribute\"/>\n"
                        + "    <property name=\"_icon\" class=\"ptolemy.vergil.icon.ValueIcon\">\n"
                        + "        <property name=\"_color\" class=\"ptolemy.actor.gui.ColorAttribute\" value=\"{0.0, 0.0, 1.0, 1.0}\"/>\n"
                        + "        <property name=\"displayWidth\" value=\"1000\"/>\n"
                        + "        <property name=\"numberOfLines\" value=\"100\"/>\n"
                        + "    </property>\n"
                        + "    <property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"["
                        + (int) (destinationStateLocation.getLocation()[0] + (sourceStateLocation
                                .getLocation()[0] - destinationStateLocation
                                .getLocation()[0]) / 2)
                        + ", "
                        + (int) (destinationStateLocation.getLocation()[1] + (sourceStateLocation
                                .getLocation()[1] - destinationStateLocation
                                .getLocation()[1]) / 2)
                        + "]\"/>\n"
                        + "    <property name=\"_smallIconDescription\" class=\"ptolemy.kernel.util.SingletonConfigurableAttribute\">\n"
                        + "        <configure><svg><text x=\"20\" style=\"font-size:14; font-family:SansSerif; fill:blue\" y=\"20\">-P-</text></svg></configure>\n"
                        + "    </property>\n"
                        + "    <property name=\"_editorFactory\" class=\"ptolemy.vergil.toolbox.VisibleParameterEditorFactory\"/>\n"
                        + "    <property name=\"_configurer\" class=\"ptolemy.actor.gui.TransitionEditorPaneFactory\"/>\n"
                        + "</property>";

                MoMLChangeRequest request = new MoMLChangeRequest(this,
                        getContainer(), moml);
                getContainer().requestChange(request);
            }
        }
    }

    // Initialize the variables of this transition.
    private void _init() throws IllegalActionException,
    NameDuplicationException {
        fsmTransitionParameterName = new StringParameter(this,
                "fsmTransitionParameterName");
        fsmTransitionParameterName.setExpression(this.getName() + "Parameter");
        fsmTransitionParameterName.setVisibility(Settable.FULL);

        showFSMTransitionParameter = new Parameter(this,
                "showFSMTransitionParameter");
        showFSMTransitionParameter.setTypeEquals(BaseType.BOOLEAN);
        showFSMTransitionParameter.setToken(BooleanToken.FALSE);

        annotation = new StringParameter(this, "annotation");
        annotation.setExpression("");
        // Add a hint to indicate to the PtolemyQuery class to open with a text style.
        Variable variable = new Variable(annotation, "_textHeightHint");
        variable.setExpression(_TEXT_HEIGHT);
        variable.setPersistent(false);

        guardExpression = new StringAttribute(this, "guardExpression");
        // Add a hint to indicate to the PtolemyQuery class to open with a text style.
        variable = new Variable(guardExpression, "_textHeightHint");
        variable.setExpression(_TEXT_HEIGHT);
        variable.setPersistent(false);

        outputActions = new OutputActionsAttribute(this, "outputActions");
        // Add a hint to indicate to the PtolemyQuery class to open with a text style.
        variable = new Variable(outputActions, "_textHeightHint");
        variable.setExpression(_TEXT_HEIGHT);
        variable.setPersistent(false);

        setActions = new CommitActionsAttribute(this, "setActions");
        // Add a hint to indicate to the PtolemyQuery class to open with a text style.
        variable = new Variable(setActions, "_textHeightHint");
        variable.setExpression(_TEXT_HEIGHT);
        variable.setPersistent(false);

        exitAngle = new Parameter(this, "exitAngle");
        exitAngle.setVisibility(Settable.NONE);
        exitAngle.setExpression("PI/5.0");
        exitAngle.setTypeEquals(BaseType.DOUBLE);

        gamma = new Parameter(this, "gamma");
        gamma.setVisibility(Settable.NONE);
        gamma.setExpression("0.0");
        gamma.setTypeEquals(BaseType.DOUBLE);

        // default attributes.
        defaultTransition = new Parameter(this, "defaultTransition");
        defaultTransition.setTypeEquals(BaseType.BOOLEAN);
        defaultTransition.setToken(BooleanToken.FALSE);
        // We would like to call this parameter "default" but
        // can't because this is a Java keyword.
        defaultTransition.setDisplayName("default");

        // Nondeterministic attributes.
        nondeterministic = new Parameter(this, "nondeterministic");
        nondeterministic.setTypeEquals(BaseType.BOOLEAN);
        nondeterministic.setToken(BooleanToken.FALSE);

        immediate = new Parameter(this, "immediate");
        immediate.setTypeEquals(BaseType.BOOLEAN);
        immediate.setToken(BooleanToken.FALSE);

        preemptive = new Parameter(this, "preemptive");
        preemptive.setTypeEquals(BaseType.BOOLEAN);
        preemptive.setToken(BooleanToken.FALSE);

        // From version 9.0.devel to 9.1.devel, the parameter
        // reset was replaced by history and the default behavior
        // was changed so that by default transitions are not
        // history transitions (are reset transitions).
        history = new Parameter(this, "history");
        history.setTypeEquals(BaseType.BOOLEAN);
        history.setToken(BooleanToken.FALSE);

        error = new Parameter(this, "error");
        error.setTypeEquals(BaseType.BOOLEAN);
        error.setToken(BooleanToken.FALSE);

        termination = new Parameter(this, "termination");
        termination.setTypeEquals(BaseType.BOOLEAN);
        termination.setToken(BooleanToken.FALSE);

        // Add refinement name parameter
        refinementName = new StringAttribute(this, "refinementName");
        refinementName.setVisibility(Settable.EXPERT);

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
                Action action = (Action) actions.next();

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

    private FSMTransitionParameter _fsmTransitionParameter;

    // The parse tree for the guard expression.
    private ASTPtRootNode _guardParseTree;

    // Version of the cached guard parse tree
    private long _guardParseTreeVersion = -1;

    // Flag to ensure that the corrections below for older version compatibility
    // are performed only once.
    private boolean _historySet = false;

    // Set to true if the transition should be checked
    // as soon as the source state is entered. This may lead
    // to transient states.
    private boolean _immediate = false;

    private boolean _nondeterministic = false;

    // The parse tree evaluator for the transition.
    // Note that this variable should not be accessed directly even inside
    // this class. Instead, always use the getParseTreeEvaluator() method.
    private ParseTreeEvaluator _parseTreeEvaluator;

    // Version of the cached parse tree evaluator
    private long _parseTreeEvaluatorVersion = -1;

    // Latest version before default history behavior changed.
    private static VersionAttribute _REFERENCE_VERSION;

    // Cached reference to the refinement of this state.
    private TypedActor[] _refinement = null;

    // Version of the cached reference to the refinement.
    private long _refinementVersion = -1;

    // Cached source state of this transition.
    private State _sourceState = null;

    // Version of cached source/destination state.
    private long _stateVersion = -1;

    // Default text height in the dialog box.
    private static String _TEXT_HEIGHT = "4";
}
