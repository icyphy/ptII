/* A request for a parameter value change.

 Copyright (c) 1999-2000 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.actor.event;

import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.event.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.Token;

//////////////////////////////////////////////////////////////////////////
//// SetParameter
/**
A request for a parameter value change.  The change is realized via
the setExpression() method of the Parameter class, followed by getToken()
(to force evaluation of the expression).

@author  Edward A. Lee
@version $Id$
@see ptolemy.data.expr.Parameter
*/
public class SetParameter extends ChangeRequest {

    /** Construct a request with the specified originator, parameter,
     *  and new expression for the parameter.
     *  @param originator The source of the change request.
     *  @param parameter The parameter to change.
     *  @param expression The expression giving the new value of the
     *   parameter.
     */
    public SetParameter(Nameable originator, Parameter parameter,
            String expression) {
        super(originator, "Change value of parameter "
                + parameter.getFullName() + " to " + expression);
        _parameter = parameter;
        _expression = expression;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute the change by calling the setExpression() method of the
     *  parameter.
     *  @see ptolemy.data.expr.Parameter#setExpression
     *  @exception ChangeFailedException If the container of the parameter
     *   rejects the expression.
     */
    public void execute() throws ChangeFailedException {
        String originalExpression;
        try {
            originalExpression = _parameter.getToken().toString();
        } catch (Exception ex) {
            originalExpression = _parameter.getExpression();
        }
        try {
            _parameter.setExpression(_expression);
            // Force evaluation of the expression to ensure validity,
            // and to ensure that the container is notified of the new
            // value.
            _parameter.getToken();
        } catch (IllegalActionException ex) {
            _parameter.setExpression(originalExpression);
            throw new ChangeFailedException(this, ex);
        }
    }

    /** Get the parameter that is to be changed.
     *  @return The parameter to change.
     */
    public Parameter getParameter() {
        return _parameter;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The expression giving the new value of the parameter.
    private String _expression;

    // The parameter to change.
    private Parameter _parameter;
}
