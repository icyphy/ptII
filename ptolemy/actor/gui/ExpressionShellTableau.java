/* A tableau for evaluating expressions interactively.

 Copyright (c) 2003 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtAssignmentNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.Variable;
import ptolemy.graph.InequalityTerm;
import ptolemy.gui.ShellInterpreter;
import ptolemy.gui.ShellTextArea;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

import java.util.Set;

//////////////////////////////////////////////////////////////////////////
//// ExpressionShellTableau
/**
A tableau that provides an interactive shell for evaluating expressions.

@author Christopher Hylands and Edward A. Lee
@version $Id$
@since Ptolemy II 3.0
@see ShellTextArea
@see ExpressionShellEffigy
*/
public class ExpressionShellTableau extends Tableau
    implements ShellInterpreter {

    /** Create a new tableau.
     *  The tableau is itself an entity contained by the effigy
     *  and having the specified name.  The frame is not made visible
     *  automatically.  You must call show() to make it visible.
     *  @param container The containing effigy.
     *  @param name The name of this tableau within the specified effigy.
     *  @exception IllegalActionException If the tableau is not acceptable
     *   to the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public ExpressionShellTableau(ExpressionShellEffigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        frame = new ExpressionShellFrame(this);
        setFrame(frame);
        frame.setTableau(this);
        _evaluator = new ParseTreeEvaluator();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Evaluate the specified command.
     *  @param command The command.
     *  @return The return value of the command, or null if there is none.
     *  @exception Exception If something goes wrong processing the command.
     */
    public String evaluateCommand(String command) throws Exception {
        if (command.trim().equals("")) {
            return "";
        }
        PtParser parser = new PtParser();
        ASTPtRootNode node = parser.generateSimpleAssignmentParseTree(command);
        String targetName = null;

        // Figure out if we got an assignment... if so, then get the
        // identifier name and only evaluated the expression part.
        if (node instanceof ASTPtAssignmentNode) {
            ASTPtAssignmentNode assignmentNode = (ASTPtAssignmentNode) node;
            targetName = assignmentNode.getIdentifier();
            node = assignmentNode.getExpressionTree();
        }

        final NamedObj model =
            ((ExpressionShellEffigy)getContainer()).getModel();
        ParserScope scope = new ModelScope() {
                public ptolemy.data.Token get(String name)
                        throws IllegalActionException {
                    Variable result = getScopedVariable(
                            null, model, name);
                    if (result != null) {
                        return result.getToken();
                    } else {
                        return null;
                    }
                }
                public ptolemy.data.type.Type getType(String name)
                        throws IllegalActionException {
                    Variable result = getScopedVariable(
                            null, model, name);
                    if (result != null) {
                        return result.getType();
                    } else {
                        return null;
                    }
                }
                public InequalityTerm getTypeTerm(String name)
                        throws IllegalActionException {
                    Variable result = getScopedVariable(
                            null, model, name);
                    if (result != null) {
                        return result.getTypeTerm();
                    } else {
                        return null;
                    }
                }
                public Set identifierSet() {
                    return getAllScopedVariableNames(null, model);
                }
            };

        Token result = _evaluator.evaluateParseTree(node, scope);

        // If a target was specified, instantiate a new token.
        if (targetName != null) {
            Attribute attribute = model.getAttribute(targetName);
            if (attribute != null && !(attribute instanceof Parameter)) {
                attribute.setContainer(null);
                attribute = null;
            }
            if (attribute == null) {
                attribute = new Parameter(model, targetName);
            }
            ((Parameter)attribute).setToken(result);
        }

        if (result == null) {
            return "";
        } else {
            return result.toString();
        }
    }

    /** Return true if the specified command is complete (ready
     *  to be interpreted).
     *  @param command The command.
     *  @return True.
     */
    public boolean isCommandComplete(String command) {
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The associated frame. */
    public ExpressionShellFrame frame;

    /** The contained shell. */
    public ShellTextArea shell;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The parameter used for evaluation.
    private ParseTreeEvaluator _evaluator;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A factory that creates a control panel to display a Tcl Shell
     */
    public static class Factory extends TableauFactory {

        /** Create a factory with the given name and container.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container is incompatible
         *   with this attribute.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         */
        public Factory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Create a new instance of ExpressionShellTableau in the specified
         *  effigy. It is the responsibility of callers of
         *  this method to check the return value and call show().
         *  @param effigy The model effigy.
         *  @return A new control panel tableau if the effigy is
         *    a PtolemyEffigy, or null otherwise.
         *  @exception Exception If the factory should be able to create a
         *   tableau for the effigy, but something goes wrong.
         */
        public Tableau createTableau(Effigy effigy) throws Exception {
            // NOTE: Can create any number of tableaux within the same
            // effigy.  Is this what we want?
            if (effigy instanceof ExpressionShellEffigy) {
                return new ExpressionShellTableau(
                        (ExpressionShellEffigy)effigy,
                        "ExpressionShellTableau");
            } else {
                return null;
            }
        }
    }
}
