/* An action setting the value of a variable.

 Copyright (c) 2000-2001 The Regents of the University of California.
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
@ProposedRating Red (liuxj@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.kernel;

import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Variable;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;

//////////////////////////////////////////////////////////////////////////
//// SetVariable
/**
A SetVariable action takes the token from evaluating the expression
specified by the <i>expression</i> attribute and sets the value of the
instance of Variable specified by the <i>variableName</i> attribute with
the token. This action is a commit action contained by a transition in
an FSMActor, which will be called the associated FSMActor of this action.
The variable with name specified by the <i>variableName</i> attribute
must be contained by the associated FSMActor, otherwise an exception will
be thrown when this action is executed. The scope of the specified
expression includes all the variables and parameters contained by the
associated FSMActor.

@author Xiaojun Liu
@version $Id$
@see Transition
@see FSMActor
*/
public class SetVariable extends Action implements CommitAction {

    /** Construct a SetVariable action with the given name contained
     *  by the specified transition. The transition argument must not be
     *  null, or a NullPointerException will be thrown. This action will
     *  use the workspace of the transition for synchronization and
     *  version counts. If the name argument is null, then the name is
     *  set to the empty string. A variable for expression evaluation is
     *  created in the transition. The name of the variable is obtained
     *  by prepending an underscore to the name of this action.
     *  Increment the version of the workspace.
     *  @param transition The transition.
     *  @param name The name of this action.
     *  @exception IllegalActionException If the action is not of an
     *   acceptable class for the container, or if the name contains
     *   a period.
     *  @exception NameDuplicationException If the transition already
     *   has an attribute with the name or that obtained by prepending
     *   an underscore to the name.
     */
    public SetVariable(Transition transition, String name)
            throws IllegalActionException, NameDuplicationException {
        super(transition, name);
        expression = new StringAttribute(this, "expression");
        variableName = new StringAttribute(this, "variableName");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Attribute specifying an expression. The value set by this action
     *  is obtained by evaluating the expression. The scope of the
     *  expression includes all the variables and parameters of the
     *  associated FSMActor of this action.
     */
    public StringAttribute expression = null;

    /** Attribute specifying the name of the variable. The variable
     *  must be contained by the associated FSMActor, otherwise an
     *  exception will be thrown when this action is executed.
     */
    public StringAttribute variableName = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute is
     *  the <i>variableName</i> attribute, record the change but do not
     *  check whether the associated FSMActor contains a variable with
     *  the specified name. If the changed attribute is the
     *  <i>expression</i> attribute, set the specified expression to the
     *  variable for expression evaluation.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If thrown by the superclass
     *   attributeChanged() method.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);
        if (attribute == variableName) {
            _variableVersion = -1;
        }
        if (attribute == expression) {
            String expr = expression.getExpression();
            _evaluationVariable().setExpression(expr);
        }
    }

    /** Clone the action into the specified workspace. This calls the
     *  base class and then sets the attribute public members to refer
     *  to the attributes of the new action.
     *  @param workspace The workspace for the new action.
     *  @return A new action.
     *  @throws CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        SetVariable newObject = (SetVariable)super.clone(workspace);
        newObject.expression =
            (StringAttribute)newObject.getAttribute("expression");
        newObject.variableName =
            (StringAttribute)newObject.getAttribute("variableName");
        newObject._variableVersion = -1;
        return newObject;
    }

    /** Take the token from evaluating the expression specified by the
     *  <i>expression</i> attribute and set the value of the variable
     *  specified by the <i>variableName</i> attribute with the token.
     *  @exception IllegalActionException If expression evaluation fails,
     *   or the specified variable cannot take the token from evaluating
     *   the expression as value.
     */
    public void execute() throws IllegalActionException {
        Variable var = _getVariable();
        var.setToken(_evaluationVariable().getToken());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the variable specified by the <i>variableName</i> attribute.
     *  This method is read-synchronized on the workspace.
     *  @return The specified variable.
     *  @exception IllegalActionException If the associated FSMActor does
     *   not contain a variable with the specified name.
     */
    protected Variable _getVariable() throws IllegalActionException {
        if (_variableVersion == workspace().getVersion()) {
            return _variable;
        }
        try {
            workspace().getReadAccess();
            FSMActor fsm = (FSMActor)getContainer().getContainer();
            String name = variableName.getExpression();
            Attribute var = fsm.getAttribute(name);
            if (var == null) {
                throw new IllegalActionException(fsm, this, "Cannot find "
                        + "variable with name: " + name);
            }
            if (!(var instanceof Variable)) {
                throw new IllegalActionException(fsm, this, "The attribute "
                        + "with name \"" + name + "\" is not an "
                        + "instance of Variable.");
            }
            _variable = (Variable)var;
            _variableVersion = workspace().getVersion();
            return _variable;
        } finally {
            workspace().doneReading();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // Cached reference to the specified variable.
    protected Variable _variable;

    // Version of reference to the specified variable.
    protected long _variableVersion = -1;

}
