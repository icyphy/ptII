/* Constraint used to restrict pattern matching in model transformations.

@Copyright (c) 2007-2014 The Regents of the University of California.
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

package ptolemy.actor.gt;

import ptolemy.actor.gt.data.MatchResult;
import ptolemy.data.BooleanToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.toolbox.VisibleParameterEditorFactory;

///////////////////////////////////////////////////////////////////
////Constraint

/**
 Constraint used to restrict pattern matching in model transformations. A
 constraint contains an expression that must be evaluable into a boolean at the
 time the transformation rule is applied. When the expression is specified at
 design time, it is parsed and a syntax tree is generated. If the parsing is not
 successful, a design time exception occurs. If the parse is successful, the
 constraint is evaluated every time a match to the pattern is found. No
 exception will be shown at the time of pattern matching even if the constraint
 cannot be successfully evaluated. In that case, the constraint will only be
 considered <tt>false</tt>. A constraint is satisfied if and only if it can be
 successfully evaluated and the result is boolean <tt>true</tt>.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Constraint extends GTParameter {

    /** Constraint a constraint.
     *
     *  @param container The container of the constraint, which is usually the
     *   pattern of a {@link TransformationRule}.
     *  @param name The name of the constraint.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public Constraint(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        Variable variable = new Variable(this, "_textHeightHint");
        variable.setExpression("5");
        variable.setPersistent(false);

        editorFactory = new VisibleParameterEditorFactory(this, "editorFactory");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Check whether this constraint is satisfied with the given match in
     *  <tt>matchResult</tt> to the given pattern. A constraint is satisfied if
     *  and only if it can be successfully evaluated and the result is boolean
     *  <tt>true</tt>.
     *
     *  @param pattern The pattern.
     *  @param matchResult The match result.
     *  @return <tt>true</tt> if the constraint is satisfied; <tt>false</tt> if
     *   it cannot be evaluated or the evaluation result is <tt>false</tt>.
     */
    public boolean check(Pattern pattern, MatchResult matchResult) {
        try {
            _evaluate(pattern, matchResult);
            return ((BooleanToken) getToken()).booleanValue();
        } catch (IllegalActionException e) {
            return false;
        }
    }

    /** Set the container of this constraint. This method ensures that the
     *  container must be the pattern of a {@link TransformationRule}.
     *
     *  @param container The new container, or <tt>null</tt> if the current
     *   container of this constraint is to be removed.
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name, or the attribute
     *   and container are not in the same workspace, or the proposed container
     *   would result in recursive containment, or the container is not an
     *   instance of {@link Pattern}.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute.
     */
    @Override
    public void setContainer(NamedObj container) throws IllegalActionException,
    NameDuplicationException {
        super.setContainer(container);
        if (container != null) {
            GTTools.checkContainerClass(this, container, Pattern.class, false);
        }
    }

    /** The editor factory.
     */
    public VisibleParameterEditorFactory editorFactory;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Evaluate the current expression to a token with the given pattern and
     *  match result using {@link ptolemy.actor.gt.GTParameter.Evaluator}. If
     *  this variable
     *  was last set directly with a token, then do nothing. In other words,
     *  the expression is evaluated only if the value of the token was most
     *  recently given by an expression.  The expression is also evaluated
     *  if any of the variables it refers to have changed since the last
     *  evaluation.  If the value of this variable
     *  changes due to this evaluation, then notify all
     *  value dependents and notify the container (if there is one) by
     *  calling its attributeChanged() and attributeTypeChanged() methods,
     *  as appropriate. An exception is thrown
     *  if the expression is illegal, for example if a parse error occurs
     *  or if there is a dependency loop.
     *  <p>
     *  If evaluation results in a token that is not of the same type
     *  as the current type of the variable, then the type of the variable
     *  is changed, unless the new type is incompatible with statically
     *  specified types (setTypeEquals() and setTypeAtMost()).
     *  If the type is changed, the attributeTypeChanged() method of
     *  the container is called.  The container can reject the change
     *  by throwing an exception.
     *  <p>
     *  This method may trigger a model error, which is delegated up
     *  the container hierarchy until an error handler is found, and
     *  is ignored if no error handler is found.  A model error occurs
     *  if the expression cannot be parsed or cannot be evaluated.
     *  <p>
     *  Part of this method is read-synchronized on the workspace.
     *
     *  @param pattern The pattern.
     *  @param matchResult The match result for the match between the pattern
     *   and a host model.
     *  @exception IllegalActionException If the expression cannot
     *   be parsed or cannot be evaluated, or if a dependency loop is found.
     */
    @Override
    protected void _evaluate(Pattern pattern, MatchResult matchResult)
            throws IllegalActionException {
        setParseTreeEvaluator(new Evaluator(pattern, matchResult) {
            @Override
            protected ParserScope _createScope(Pattern pattern,
                    MatchResult matchResult, ParserScope superScope) {
                return new Scope(pattern, matchResult, superScope) {
                    @Override
                    public Token get(String name) throws IllegalActionException {
                        if (name.equals("this")) {
                            NamedObj container = Constraint.this.getContainer();
                            NamedObj match = (NamedObj) _matchResult
                                    .get(container);
                            if (match != null) {
                                return new ObjectToken(match, match.getClass());
                            }
                        }
                        return super.get(name);
                    }
                };
            }
        });
        try {
            super._evaluate();
        } finally {
            setParseTreeEvaluator(null);
            _parserScope = null;
        }
    }
}
