/* Constraint used to restrict pattern matching in model transformations.

@Copyright (c) 2007-2008 The Regents of the University of California.
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
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

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
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
*/
public class Constraint extends ParameterAttribute {

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
    }

    /** Construct a constraint.
     *
     *  @param workspace The workspace that will list the attribute.
     */
    public Constraint(Workspace workspace) {
        super(workspace);
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
            ((GTParameter) parameter)._evaluate(pattern, matchResult);
            return ((BooleanToken) parameter.getToken()).booleanValue();
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
    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        super.setContainer(container);
        if (container != null) {
            _checkContainerClass(container, Pattern.class, false);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Initialize the parameter used to specify the expression of this
     *  constraint.
     *
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the parameter's name
     *   ("constraint") coincides with a parameter already in the container.
     */
    protected void _initParameter() throws IllegalActionException,
            NameDuplicationException {
        parameter = new GTParameter(this, "constraint");
        parameter.setTypeEquals(BaseType.BOOLEAN);
        Variable variable = new Variable(parameter, "_textHeightHint");
        variable.setExpression("5");
        variable.setPersistent(false);
    }
}
