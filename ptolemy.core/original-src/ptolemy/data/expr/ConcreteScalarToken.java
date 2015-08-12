/* A concrete scalar token.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.data.expr;

import ptolemy.data.BooleanToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ConcreteScalarToken

/**
 A token that represents a scalar of any type.
 This is instantiated by the Constants class with name "scalar".

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (yuhong)
 @Pt.AcceptedRating Yellow (wbwu)
 */
public class ConcreteScalarToken extends ScalarToken {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the type of this token.
     *  @return BaseType.SCALAR.
     */
    @Override
    public Type getType() {
        return BaseType.SCALAR;
    }

    /** Return this token.
     *  @return This token.
     */
    @Override
    protected ScalarToken _absolute() {
        return this;
    }

    /** Throw an exception.
     */
    @Override
    protected ScalarToken _add(ScalarToken rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(notSupportedMessage("add", this,
                rightArgument));
    }

    /** Throw an exception.
     */
    @Override
    protected ScalarToken _bitwiseAnd(ScalarToken rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(notSupportedMessage("bitwiseAnd",
                this, rightArgument));
    }

    /** Throw an exception.
     */
    @Override
    protected ScalarToken _bitwiseNot() throws IllegalActionException {
        throw new IllegalActionException(notSupportedMessage("bitwiseNot",
                this, this));
    }

    /** Throw an exception.
     */
    @Override
    protected ScalarToken _bitwiseOr(ScalarToken rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(notSupportedMessage("bitwiseOr", this,
                rightArgument));
    }

    /** Throw an exception.
     */
    @Override
    protected ScalarToken _bitwiseXor(ScalarToken rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(notSupportedMessage("bitwiseXor",
                this, rightArgument));
    }

    /** Throw an exception.
     */
    @Override
    protected ScalarToken _divide(ScalarToken rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(notSupportedMessage("divide", this,
                rightArgument));
    }

    /** Throw an exception.
     */
    @Override
    protected BooleanToken _isCloseTo(ScalarToken rightArgument, double epsilon)
            throws IllegalActionException {
        throw new IllegalActionException(notSupportedMessage("isCloseTo", this,
                rightArgument));
    }

    /** Throw an exception.
     */
    @Override
    protected BooleanToken _isLessThan(ScalarToken rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(notSupportedMessage("isLessThan",
                this, rightArgument));
    }

    /** Throw an exception.
     */
    @Override
    protected ScalarToken _modulo(ScalarToken rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(notSupportedMessage("modulo", this,
                rightArgument));
    }

    /** Throw an exception.
     */
    @Override
    protected ScalarToken _multiply(ScalarToken rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(notSupportedMessage("multiply", this,
                rightArgument));
    }

    /** Throw an exception.
     */
    @Override
    protected ScalarToken _subtract(ScalarToken rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(notSupportedMessage("subtract", this,
                rightArgument));
    }
}
