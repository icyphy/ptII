/* An actor that reads expressions and parses them into tokens.

@Copyright (c) 1998-2003 The Regents of the University of California.
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

                                                PT_COPYRIGHT_VERSION 2
                                                COPYRIGHTENDKEY
@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (liuj@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.conversions;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// ExpressionToToken
/**
This actor reads a string expression from the input port and outputs
the token resulting from the evaluation.  The type of the output port
defaults to general, meaning that the only output will be a pure
event.  In order to usefully use this class, the type of the output
port must be set to the type of the expression that is expected.

@author  Steve Neuendorffer
@version $Id$
@since Ptolemy II 2.1
*/
public class ExpressionToToken extends Converter {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ExpressionToToken(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.STRING);
        output.setTypeEquals(BaseType.UNKNOWN);
        _expressionEvaluator = new Variable(this, "_expressionEvaluator");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Output the data read in the preinitialize() or postfire() if
     *  there is any.
     *  @exception IllegalActionException If there's no director, or
     *   if the expression read from the input cannot be parsed.
     */
    public void fire() throws IllegalActionException {
        String string = ((StringToken)input.get(0)).stringValue();
        _expressionEvaluator.setExpression(string);
        output.broadcast(_expressionEvaluator.getToken());
    }

    /** Return true if and only if an input is present.
     *  @exception IllegalActionException If there's no director, or
     *   if no connection has been made to the input.
     */
    public boolean prefire() throws IllegalActionException {
        if (input.hasToken(0)) {
            return super.prefire();
        } else {
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** Variable used to evaluate expressions. */
    private Variable _expressionEvaluator;
}
