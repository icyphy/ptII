/* Scheduling relations between Ptera events.

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

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import ptolemy.actor.parameters.Priority;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.RecordToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtArrayConstructNode;
import ptolemy.data.expr.ASTPtRecordConstructNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.domains.modal.kernel.Transition;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

/**
 A scheduling relation is an edge from one Ptera event to another. If it is not
 a cancelling edge, then processing the event at the starting point of the edge
 causes the one at the end point to be scheduled after a certain amount of
 model-time delay, if the guard of the scheduling relation is satisfied. If it
 is a cancelling edge, then processing the first event causes the second one to
 be cancelled if it is already scheduled in the containing Ptera controller's
 event queue.
 <p>
 A scheduling relation may carry argument values to be supplied to the event to
 be scheduled, who has parameters defined on it. The number and types of the
 evaluated arguments must match those of the parameters declared by the event.
 <p>
 If the guard of a scheduling relation is omitted, it is defaulted to true,
 which means the scheduling relation is unconditional.
 <p>
 A scheduling relation can be assigned a priority, which is an integer number.
 The default priority is 0. Priorities are used to order the scheduling
 relations from the same event. Scheduling relations with higher priorities
 (smaller priority numbers) are scheduled before those with lower priorities
 from the same event, if they are scheduled at exactly the same time in the
 future.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class SchedulingRelation extends Transition {

    /** Construct a scheduling relation with the given name contained by the
     *  specified entity. The container argument must not be null, or a
     *  NullPointerException will be thrown. This transition will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *
     *  @param container The container.
     *  @param name The name of the scheduling relation.
     *  @exception IllegalActionException If the container is incompatible
     *   with this scheduling relation.
     *  @exception NameDuplicationException If the name coincides with
     *   any relation already in the container.
     */
    public SchedulingRelation(PteraController container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        guardExpression.setDisplayName("condition");
        outputActions.setVisibility(Settable.NONE);
        setActions.setVisibility(Settable.NONE);
        preemptive.setVisibility(Settable.NONE);
        defaultTransition.setVisibility(Settable.NONE);
        nondeterministic.setVisibility(Settable.NONE);
        refinementName.setVisibility(Settable.NONE);

        delay = new StringAttribute(this, "delay") {
            @Override
            protected void _exportMoMLContents(Writer output, int depth)
                    throws IOException {
                String displayName = getDisplayName();
                setDisplayName(null);
                try {
                    super._exportMoMLContents(output, depth);
                } finally {
                    setDisplayName(displayName);
                }
            }
        };
        delay.setDisplayName("delay (\u03B4)");
        delay.setExpression("0.0");

        arguments = new StringAttribute(this, "arguments");
        arguments.setExpression("{}");

        canceling = new Parameter(this, "canceling");
        canceling.setTypeEquals(BaseType.BOOLEAN);
        canceling.setExpression("false");

        priority = new Priority(this, "priority");

        triggers = new StringParameter(this, "triggers");
        Variable variable = new Variable(triggers, "_textHeightHint");
        variable.setExpression("3");
        variable.setPersistent(false);
    }

    /** React to a change in an attribute. If the changed attribute is
     *  the <i>arguments</i> parameter, evaluate the arguments. If the changed
     *  attribute is <i>delay</i>, evaluate the delay. Then, check whether the
     *  combination of this scheduling relation's parameters is reasonable. If
     *  the scheduling relation is cancelling, then its <i>arguments</i>
     *  parameter must specify an empty list ("()"), and its delay must be
     *  evaluated to 0.0.
     *
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If thrown by the superclass
     *   attributeChanged() method, or the changed attribute is the
     *   <i>arguments</i> parameter or the <i>delay</i> parameter and is given
     *   an expression that does not evaluate to a boolean value, or this
     *   scheduling relation is set to cancelling but the values of
     *   <i>arguments</i> and <i>delay</i> are not acceptable.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == arguments) {
            _parseArguments();
        } else if (attribute == delay) {
            _parseDelay();
        } else if (attribute == triggers) {
            getTriggers();
        }

        if (canceling != null && delay != null && isCanceling()
                && getContainer() != null) {
            if (!_isZero(delay.getExpression())) {
                throw new IllegalActionException("For a canceling edge, the "
                        + "delay must be const 0.0.");
            } else {
                if (_argumentsTreeVersion != _workspace.getVersion()) {
                    _parseArguments();
                }
                if (_argumentsTree.jjtGetNumChildren() > 0) {
                    throw new IllegalActionException("For a canceling edge, "
                            + "the argument list must be empty.");
                }
            }
        }
        super.attributeChanged(attribute);
    }

    /** Clone the scheduling relation into the specified workspace. This calls
     *  the base class and then sets the attribute public members to refer to
     *  the attributes of the new scheduling relation.
     *
     *  @param workspace The workspace for the new scheduling relation.
     *  @return A new scheduling relation.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        SchedulingRelation relation = (SchedulingRelation) super
                .clone(workspace);
        relation._argumentsTree = null;
        relation._argumentsTreeVersion = -1;
        relation._delayTree = null;
        relation._delayTreeVersion = -1;
        relation._parseTreeEvaluator = new ParseTreeEvaluator();
        return relation;
    }

    /** Evaluate the parse tree of the arguments and return an ArrayToken or
     *  RecordToken that contains the values of those arguments in the given
     *  parser scope.
     *
     *  @param scope The parser scope in which the arguments are evaluated.
     *  @return The token containing the values of those arguments, which must
     *   be of type {@link ArrayToken} or {@link RecordToken}.
     *  @exception IllegalActionException If the evaluation is unsuccessful.
     */
    public Token getArguments(ParserScope scope) throws IllegalActionException {
        if (_argumentsTreeVersion != _workspace.getVersion()) {
            _parseArguments();
        }
        Token token = _parseTreeEvaluator.evaluateParseTree(_argumentsTree,
                scope);
        if (token == null
                || !(token instanceof ArrayToken || token instanceof RecordToken)) {
            throw new IllegalActionException(this, "Unable to evaluate "
                    + "arguments \"" + arguments.getExpression() + "\".");
        }
        return token;
    }

    /** Evaluate the delay parameter in the given parse scope and return its
     *  value.
     *
     *  @param scope The parser scope in which the delay is evaluated.
     *  @return The value of the model-time delay.
     *  @exception IllegalActionException If the evaluation is unsuccessful.
     */
    public double getDelay(ParserScope scope) throws IllegalActionException {
        if (_delayTreeVersion != _workspace.getVersion()) {
            _parseDelay();
        }
        Token token = _parseTreeEvaluator.evaluateParseTree(_delayTree, scope);
        if (token == null || !(token instanceof ScalarToken)) {
            throw new IllegalActionException(this, "Unable to evaluate delay"
                    + "expression \"" + delay.getExpression() + "\".");
        }
        double result = ((ScalarToken) token).doubleValue();
        return result;
    }

    /** Return a string describing this scheduling relation. The string has up
     *  to three lines. The first line is the guard expression, preceded
     *  by "guard: ". The second line is the delay expression, preceded by
     *  "\u03B4: " (unicode for the delta character). The third line is the
     *  list of arguments, preceded by "arguments: ". If any of these
     *  is missing, then the corresponding line is omitted.
     *
     *  @return A string describing this transition.
     */
    @Override
    public String getLabel() {
        StringBuffer buffer = new StringBuffer(super.getLabel());

        String delayExpression = delay.getExpression();
        if (delayExpression != null && !_isZero(delayExpression)) {
            if (buffer.length() > 0) {
                buffer.append("\n");
            }
            buffer.append("\u03B4: "); // Unicode for \delta
            buffer.append(delayExpression);
        }

        String argumentsExpression = arguments.getExpression();
        if (argumentsExpression != null) {
            String trimmedArguments = argumentsExpression.trim();
            boolean emptyArguments = trimmedArguments.startsWith("{")
                    && trimmedArguments.endsWith("}")
                    && trimmedArguments
                    .substring(1, trimmedArguments.length() - 1).trim()
                    .equals("");
            if (!emptyArguments) {
                if (buffer.length() > 0) {
                    buffer.append("\n");
                }
                buffer.append("arguments: ");
                buffer.append(argumentsExpression);
            }
        }

        String priorityExpression = priority.getExpression();
        if (priorityExpression != null && !_isZero(priorityExpression)) {
            if (buffer.length() > 0) {
                buffer.append("\n");
            }
            buffer.append("priority: "); // Unicode for \delta
            buffer.append(priorityExpression);
        }

        String triggersExpression = triggers.getExpression();
        if (!triggersExpression.trim().equals("")) {
            if (buffer.length() > 0) {
                buffer.append("\n");
            }
            buffer.append("triggers: " + triggersExpression);
        }

        return buffer.toString();
    }

    /** Get the list of ports or variables referred to in the triggers
     *  attributes. When a port receives a token or a variable's value is
     *  changed, the event that this scheduling relation points to is triggered
     *  (if that event is still in the event queue). Only ports belonging to the
     *  container of this scheduling relation (which is an PteraController) is
     *  searched for. Variables belonging to this scheduling relation, its
     *  container, and containers of the container are searched for. Variable
     *  names can contain dots.
     *
     *  @return A list of ports and variables.
     *  @exception IllegalActionException If the value of the triggers parameter
     *  cannot be obtained.
     */
    public List<NamedObj> getTriggers() throws IllegalActionException {
        CompositeEntity controller = (CompositeEntity) getContainer();
        if (controller == null) {
            return null;
        }
        String[] names = triggers.stringValue().split(",");
        List<NamedObj> list = null;
        for (String name : names) {
            name = name.trim();
            if (name.equals("")) {
                continue;
            }
            NamedObj object = getAttribute(name, Variable.class);
            if (object == null) {
                object = controller.getAttribute(name, Variable.class);
            }
            if (object == null) {
                object = controller.getPort(name);
            }
            if (object == null) {
                object = ModelScope.getScopedVariable(null, this, name);
            }
            if (object == null) {
                throw new IllegalActionException(this, "Unable to find "
                        + "a port of a variable with name\"" + name + "\".");
            } else {
                if (list == null) {
                    list = new LinkedList<NamedObj>();
                }
                list.add(object);
            }
        }
        return list;
    }

    /** Return whether this scheduling relation is cancelling.
     *
     *  @return True if this scheduling relation is cancelling.
     */
    public boolean isCanceling() {
        try {
            return ((BooleanToken) canceling.getToken()).booleanValue();
        } catch (IllegalActionException e) {
            return false; // Assume it is not canceling edge by default.
        }
    }

    /** Evaluate the guard in the given parser scope, and return whether this
     *  scheduling relation is enabled (with its guard evaluated to true).
     *
     *  @param scope The parser scope in which the guard is to be evaluated.
     *  @return True if the transition is enabled and some event is detected.
     *  @exception IllegalActionException If thrown when evaluating the guard.
     */
    @Override
    public boolean isEnabled(ParserScope scope) throws IllegalActionException {
        String guard = getGuardExpression();
        if (guard.trim().equals("")) {
            return true;
        } else {
            return super.isEnabled(scope);
        }
    }

    /** The attribute for arguments. Its value must be evaluated to an
        ArrayToken or RecordToken, though this evaluation is performed only when
        this scheduling relation is to be considered by the starting event but
        not when this attribute is set by the designer. */
    public StringAttribute arguments;

    /** A Boolean-valued parameter that defines whether this scheduling relation
        is cancelling. */
    public Parameter canceling;

    /** The attribute for the model-time delay. Its value must be evaluated to a
        ScalarToken, though this evaluation is performed only when this
        scheduling relation is to be considered by the starting event but not
        when this attribute is set by the designer. */
    public StringAttribute delay;

    /** The priority of this scheduling relation. */
    public Priority priority;

    /** A comma-separated list of port names and variable names to be
     *  monitored. */
    public StringParameter triggers;

    /** Return whether the given expression is statically equal to 0.0.
     *
     *  @param expression The expression.
     *  @return True if the expression is statically equal to 0.0.
     */
    private boolean _isZero(String expression) {
        String trimmedExpression = expression.trim().toLowerCase(
                Locale.getDefault());
        for (String zeroConst : _ZERO_CONSTS) {
            if (trimmedExpression.equals(zeroConst)) {
                return true;
            }
        }
        return false;
    }

    /** Parse the arguments.
     *
     *  @exception IllegalActionException If thrown when when parsing the
     *   arguments.
     */
    private void _parseArguments() throws IllegalActionException {
        _argumentsTree = new PtParser().generateParseTree(arguments
                .getExpression());
        _argumentsTreeVersion = _workspace.getVersion();
        if (!(_argumentsTree == null
                || _argumentsTree instanceof ASTPtArrayConstructNode || _argumentsTree instanceof ASTPtRecordConstructNode)) {
            throw new IllegalActionException(this, "The arguments for a "
                    + "scheduling edge must be in an array or a record in the "
                    + "form of {...}.");
        }
    }

    /** Parse the delay.
     *
     *  @exception IllegalActionException If thrown when when parsing the delay.
     */
    private void _parseDelay() throws IllegalActionException {
        _delayTree = new PtParser().generateParseTree(delay.getExpression());
        _delayTreeVersion = _workspace.getVersion();
    }

    /** An array of all recognizable constant values that equal to 0.0d. */
    private static final String[] _ZERO_CONSTS = new String[] { "0", "0.0",
        "0l", "0s", "0ub", "0.0d", "0.0f" };

    /** The parse tree of arguments. */
    private ASTPtRootNode _argumentsTree;

    /** Version of _argumentsTree. */
    private long _argumentsTreeVersion = -1;

    /** The parse tree of delay. */
    private ASTPtRootNode _delayTree;

    /** Version of _delayTree. */
    private long _delayTreeVersion = -1;

    /** The evaluated to evaluate all parse trees. */
    private ParseTreeEvaluator _parseTreeEvaluator = new ParseTreeEvaluator();
}
