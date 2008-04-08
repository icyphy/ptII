/*

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

import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtArrayConstructNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.PtParser;
import ptolemy.data.type.BaseType;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class SchedulingRelation extends Transition {

    /**
     * @param container
     * @param name
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public SchedulingRelation(ERGController container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /**
     * @param workspace
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public SchedulingRelation(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    public void attributeChanged(Attribute attribute)
    throws IllegalActionException {
        if (attribute == arguments) {
            try {
                _argumentsTree = (ASTPtArrayConstructNode)
                        _parser.generateParseTree(arguments.getExpression());
            } catch (ClassCastException e) {
                throw new IllegalActionException(this, "The arguments for a " +
                        "scheduling edge must be in an array in the form of " +
                        "{...}.");
            }
        } else if (attribute == delay) {
            _delayTree = _parser.generateParseTree(delay.getExpression());
        }

        if (canceling != null && delay != null && isCanceling()
                && getContainer() != null) {
            if (!_isZeroDelay()) {
                throw new IllegalActionException("For a canceling edge, the "
                        + "delay must be const 0.0.");
            } else if (_argumentsTree.jjtGetNumChildren() > 0) {
                throw new IllegalActionException("For a canceling edge, the "
                        + "argument list must be empty.");
            }
        }
    }

    public ArrayToken getArguments(ParserScope scope)
    throws IllegalActionException {
        Token token = _parseTreeEvaluator.evaluateParseTree(_argumentsTree,
                scope);
        if (token == null || !(token instanceof ArrayToken)) {
            throw new IllegalActionException(this, "Unable to evaluate " +
                    "arguments \"" + arguments.getExpression() + "\".");
        }
        return (ArrayToken) token;
    }

    public double getDelay(ParserScope scope) throws IllegalActionException {
        Token token = _parseTreeEvaluator.evaluateParseTree(_delayTree, scope);
        if (token == null || !(token instanceof ScalarToken)) {
            throw new IllegalActionException(this, "Unable to evaluate delay" +
                    "expression \"" + delay.getExpression() + "\".");
        }
        double result = ((ScalarToken) token).doubleValue();
        return result;
    }

    public String getLabel() {
        StringBuffer buffer = new StringBuffer(super.getLabel());

        String delayExpression = delay.getExpression();
        if ((delayExpression != null) && !_isZeroDelay()) {
            if (buffer.length() > 0) {
                buffer.append("\n");
            }
            buffer.append("\u03B4: "); // Unicode for \delta
            buffer.append(delayExpression);
        }

        String argumentsExpression = arguments.getExpression();
        if ((argumentsExpression != null)
                && !_isEmptyArguments(argumentsExpression)) {
            if (buffer.length() > 0) {
                buffer.append("\n");
            }
            buffer.append("arguments: ");
            buffer.append(argumentsExpression);
        }

        return buffer.toString();
    }

    public boolean isCanceling() {
        try {
            return ((BooleanToken) canceling.getToken()).booleanValue();
        } catch (IllegalActionException e) {
            return false; // Assume it is not canceling edge by default.
        }
    }

    public boolean isEnabled(ParserScope scope) throws IllegalActionException {
        String guard = getGuardExpression();
        if (guard.trim().equals("")) {
            return true;
        } else {
            return super.isEnabled(scope);
        }
    }

    public StringAttribute arguments;

    public Parameter canceling;

    public StringAttribute delay;

    private void _init() throws IllegalActionException,
    NameDuplicationException {
        guardExpression.setDisplayName("condition");
        outputActions.setVisibility(Settable.NONE);
        setActions.setVisibility(Settable.NONE);
        reset.setVisibility(Settable.NONE);
        preemptive.setVisibility(Settable.NONE);
        defaultTransition.setVisibility(Settable.NONE);
        nondeterministic.setVisibility(Settable.NONE);
        refinementName.setVisibility(Settable.NONE);

        delay = new StringAttribute(this, "delay");
        delay.setDisplayName("delay (\u03B4)");
        delay.setExpression("0.0");

        arguments = new StringAttribute(this, "arguments");
        arguments.setExpression("{}");

        canceling = new Parameter(this, "canceling");
        canceling.setTypeEquals(BaseType.BOOLEAN);
        canceling.setExpression("false");
    }

    private static boolean _isEmptyArguments(String argumentsExpression) {
        return argumentsExpression.trim().equals("{}");
    }

    private boolean _isZeroDelay() {
        try {
            double d =
                getDelay(((ERGController) getContainer()).getPortScope());
            if (d == 0.0) {
                return true;
            }
        } catch (IllegalActionException e) {
        }
        return false;
    }

    private ASTPtArrayConstructNode _argumentsTree;

    private ASTPtRootNode _delayTree;

    private ParseTreeEvaluator _parseTreeEvaluator = new ParseTreeEvaluator();

    private PtParser _parser = new PtParser();
}
