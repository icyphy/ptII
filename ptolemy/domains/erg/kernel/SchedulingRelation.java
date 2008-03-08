/*

 Copyright (c) 1997-2005 The Regents of the University of California.
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

package ptolemy.domains.erg.kernel;

import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.PtParser;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
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
        if (attribute == delay) {
            _delayParseTree = null;
        }
    }

    public double getDelay() throws IllegalActionException {
        ERGController controller = (ERGController) getContainer();
        String expr = delay.getExpression();
        if (_delayParseTree == null) {
            // Parse the delay expression.
            PtParser parser = new PtParser();
            try {
                _delayParseTree = parser.generateParseTree(expr);
            } catch (IllegalActionException ex) {
                throw new IllegalActionException(this, ex,
                        "Failed to parse delay expression \"" + expr + "\"");
            }
        }
        Token token = _parseTreeEvaluator.evaluateParseTree(_delayParseTree,
                controller.getPortScope());
        if (token == null) {
            throw new IllegalActionException(this, "Unable to evaluate delay" +
                    "expression \"" + expr + "\"");
        }
        double result = ((DoubleToken) token).doubleValue();
        return result;
    }

    public String getLabel() {
        StringBuffer buffer = new StringBuffer(super.getLabel());

        String delayExpression = delay.getExpression();
        if ((delayExpression != null) && !_isZeroDelay(delayExpression)) {
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

    public boolean isEnabled() throws IllegalActionException {
        String guard = getGuardExpression();
        if (guard.trim().equals("")) {
            return true;
        } else {
            return super.isEnabled();
        }
    }

    public Parameter arguments;

    public Parameter delay;

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

        delay = new Parameter(this, "delay");
        delay.setDisplayName("delay (\u03B4)");
        delay.setTypeEquals(BaseType.DOUBLE);
        delay.setExpression("0.0");

        arguments = new Parameter(this, "arguments");
        arguments.setTypeEquals(new ArrayType(BaseType.GENERAL));
        arguments.setExpression("{}");
    }

    private static boolean _isEmptyArguments(String argumentsExpression) {
        return argumentsExpression.trim().equals("{}");
    }

    private static boolean _isZeroDelay(String delayExpression) {
        try {
            double delay = Double.parseDouble(delayExpression);
            return DoubleToken.ZERO.equals(new DoubleToken(delay));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private ASTPtRootNode _delayParseTree;

    private ParseTreeEvaluator _parseTreeEvaluator = new ParseTreeEvaluator();
}
