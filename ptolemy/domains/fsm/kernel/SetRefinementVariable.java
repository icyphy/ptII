/* An action setting a variable of a refinement.

 Copyright (c) 2000 The Regents of the University of California.
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

import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Variable;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;

//////////////////////////////////////////////////////////////////////////
//// SetRefinementVariable
/**
A SetRefinementVariable action takes the token from evaluating the
expression specified by the <i>expression</i> parameter and sets the
value of the instance of Variable specified by the <i>variableName</i>
parameter with the token. This action is a commit action contained by
a transition in an FSMActor, which will be called the associated
FSMActor of this action. The variable set by this action must be
contained by the refinement of the destination state of the transition
containing this action or an entity deeply contained by the refinement,
otherwise an exception is thrown when this action is executed. The
<i>variableName</i> parameter specifies the name of the variable
relative to the refinement. The scope of the specified expression
includes all the variables and parameters contained by the associated
FSMActor.

@author Xiaojun Liu
@version $Id$
@see Transition
@see FSMActor
*/
public class SetRefinementVariable extends SetVariable {

    /** Construct a SetRefinementVariable action with the given name
     *  contained by the specified transition. The transition argument
     *  must not be null, or a NullPointerException will be thrown.
     *  This action will use the workspace of the transition for
     *  synchronization and version counts. If the name argument is
     *  null, then the name is set to the empty string. A variable for
     *  expression evaluation is created in the transition. The name
     *  of the variable is obtained by prepending an underscore to the
     *  name of this action.
     *  Increment the version of the workspace.
     *  @param transition The transition.
     *  @param name The name of this action.
     *  @exception IllegalActionException If the action is not of an
     *   acceptable class for the transition, or if the name contains
     *   a period.
     *  @exception NameDuplicationException If the transition already
     *   has an attribute with the name or that obtained by prepending
     *   an underscore to the name.
     */
    public SetRefinementVariable(Transition transition, String name)
            throws IllegalActionException, NameDuplicationException {
        super(transition, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the variable specified by the <i>variableName</i> parameter.
     *  This method is read-synchronized on the workspace.
     *  @return The specified variable.
     *  @exception IllegalActionException If there is no variable with the
     *   specified name in the refinement of the destination state of the
     *   transition containing this action or an entity deeply contained
     *   by the refinement.
     */
    protected Variable _getVariable() throws IllegalActionException {
        if (_variableVersion == workspace().getVersion()) {
            return _variable;
        }
        try {
            workspace().getReadAccess();
            Transition tr = (Transition)getContainer();
            Entity ref = (Entity)tr.destinationState().getRefinement();
            if (ref == null) {
                throw new IllegalActionException(this, "The destination "
                        + "state of the containing transition has no "
                        + "refinement.");
            }
            StringToken tok = (StringToken)variableName.getToken();
            Attribute var = ref.getAttribute(tok.toString());
            if (var == null) {
                throw new IllegalActionException(ref, this, "Cannot find "
                        + "variable with name: " + tok.toString());
            }
            if (!(var instanceof Variable)) {
                throw new IllegalActionException(ref, this, "The attribute "
                        + "with name \"" + tok.toString() + "\" is not an "
                        + "instance of Variable.");
            }
            _variable = (Variable)var;
            _variableVersion = workspace().getVersion();
            return _variable;
        } finally {
            workspace().doneReading();
        }
    }

}
