/* A token that contains a label/token pairs in a union.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.data;

import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.PtParser;
import ptolemy.data.type.Type;
import ptolemy.data.type.UnionType;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// UnionToken

/**
 A token that contains a label/token pairs. This pair is one of the
 choices in a union type, but the exact type is not stored in this
 class. Operations on union tokens may require that they have the
 same field in among the possible choices.

 @author Yuhong Xiong, Steve Neuendorffer, Elaine Cheong, Edward Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (yuhong)
 @Pt.AcceptedRating Red (cxh)
 */
public class UnionToken extends AbstractNotConvertibleToken {
    /** Construct a UnionToken with the specified label and value.
     *  @param label The label.
     *  @param value The value of this token.
     *  @exception IllegalActionException If the label or the value is null.
     */
    public UnionToken(String label, Token value) throws IllegalActionException {
        if (label == null || value == null) {
            throw new IllegalActionException("UnionToken: The label or the "
                    + "value is null.");
        }

        _label = label;
        _value = value;
    }

    /** Construct a UnionToken from the specified string.
     *  @param init A string expression of a record.
     *  @exception IllegalActionException If the string does not
     *  contain a parsable record.
     */
    public UnionToken(String init) throws IllegalActionException {
        PtParser parser = new PtParser();
        ASTPtRootNode tree = parser.generateParseTree(init);

        ParseTreeEvaluator evaluator = new ParseTreeEvaluator();
        Token token = evaluator.evaluateParseTree(tree);

        if (token instanceof UnionToken) {
            UnionToken unionToken = (UnionToken) token;
            _label = unionToken.label();
            _value = unionToken.value();

            if (_label == null || _value == null) {
                throw new IllegalActionException("UnionToken: The label "
                        + "or the value is null.");
            }
        } else {
            throw new IllegalActionException("An union token cannot be"
                    + " created from the expression '" + init + "'");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the argument is an UnionToken with the same
     *  label and value.
     *  @param object An instance of Object.
     *  @return True if the argument is equal to this token.
     *  @see #hashCode()
     */
    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        // This test rules out instances of a subclass.
        if (object.getClass() != getClass()) {
            return false;
        }

        UnionToken unionToken = (UnionToken) object;

        return _label.equals(unionToken.label())
                && _value.equals(unionToken.value());
    }

    /** Return the type of this token.
     *  @return An instance of UnionType containing one field. The label
     *   of that field is the label of this token, and the type of that
     *   field is the type of the value of this token.
     */
    @Override
    public Type getType() {
        String[] labels = new String[1];
        labels[0] = _label;

        Type[] types = new Type[1];
        types[0] = _value.getType();
        return new UnionType(labels, types);
    }

    /** Return a hash code value for this token. This method returns the
     *  hash codes of the value token.
     *  @return A hash code value for this token.
     */
    @Override
    public int hashCode() {
        return _value.hashCode();
    }

    /** Return the label of this token.
     *  @return The label of this token.
     */
    public String label() {
        return _label;
    }

    /** Returns a new UnionToken representing the multiplicative identity.
     *  The returned token has the same label as this one, and contains
     *  the multiplicative identity of the value of this token.
     *  @return A UnionToken.
     *  @exception IllegalActionException If multiplicative identity is not
     *   supported by any value token.
     */
    @Override
    public Token one() throws IllegalActionException {
        return new UnionToken(_label, _value.one());
    }

    /** Return the value of this token as a string.
     *  The syntax is <code>{|<i>label</i> = <i>value</i>|}</code>
     *  @return A String beginning with "{|" that contains a label and value
     *  pair, ending with "|}".
     */
    @Override
    public String toString() {
        return "{|" + _label + " = " + _value.toString() + "|}";
    }

    /** Return the value of this token.
     * @return The value of this token.
     */
    public Token value() {
        return _value;
    }

    /** Returns a new UnionToken representing the additive identity.
     *  The returned token has the same label as this one, and
     *  contains the additive identity of the value token.
     *  @return A UnionToken.
     *  @exception IllegalActionException If additive identity is not
     *   supported by any value token.
     */
    @Override
    public Token zero() throws IllegalActionException {
        return new UnionToken(_label, _value.zero());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a new token whose value is the sum of this token and
     *  the argument. It is assumed that the class of the argument
     *  is UnionToken.
     *  @param rightArgument The token to add to this token.
     *  @return A new UnionToken.
     *  @exception IllegalActionException If the label of the argument
     *   and this token are different, or calling the add method on
     *   the value token throws it.
     */
    @Override
    protected Token _add(Token rightArgument) throws IllegalActionException {
        UnionToken unionToken = (UnionToken) rightArgument;

        if (!_label.equals(unionToken.label())) {
            throw new IllegalActionException("UnionToken._add: The label "
                    + "of the argument token (" + unionToken.label()
                    + ") is different from that of this token (" + _label + ".");
        }

        Token value = _value.add(unionToken.value());
        return new UnionToken(_label, value);
    }

    /** Return a new token whose value is the division of
     *  this token and the argument. It is assumed that the class of
     *  the argument is UnionToken.
     *  @param rightArgument The token to divide this token by.
     *  @return A new UnionToken.
     *  @exception IllegalActionException If the label of the argument
     *   and this token are different, or calling the divide method on
     *   the value token throws it.
     */
    @Override
    protected Token _divide(Token rightArgument) throws IllegalActionException {
        UnionToken unionToken = (UnionToken) rightArgument;

        if (!_label.equals(unionToken.label())) {
            throw new IllegalActionException("UnionToken._divide: The label "
                    + "of the argument token (" + unionToken.label()
                    + ") is different from that of this token (" + _label + ".");
        }

        Token value = _value.divide(unionToken.value());
        return new UnionToken(_label, value);
    }

    /** Test whether the value of this token is close to the first
     *  argument, where "close" means that the distance between them
     *  is less than or equal to the second argument.  This method
     *  only makes sense for tokens where the distance between them is
     *  reasonably represented as a double. It is assumed that the
     *  argument is an UnionToken, and the isCloseTo() method of the
     *  fields is used.  If the fields do not match, then the
     *  return value is false.
     *  @param rightArgument The token to compare to this token.
     *  @param epsilon The value that we use to determine whether two
     *   tokens are close.
     *  @return A token containing true if the value of the first
     *   argument is close to the value of this token.
     *  @exception IllegalActionException If thrown by calling isCloseTo()
     *  on the value of this Token.
     */
    @Override
    protected BooleanToken _isCloseTo(Token rightArgument, double epsilon)
            throws IllegalActionException {
        UnionToken unionToken = (UnionToken) rightArgument;

        if (!_label.equals(unionToken.label())) {
            return new BooleanToken(false);
        }

        return _value.isCloseTo(unionToken.value(), epsilon);
    }

    /** Return true if the specified token is equal to this one.
     *  Equal means that both tokens have the same label with the
     *  same value.  This method is different from equals() in that
     *  _isEqualTo() looks for equalities of values irrespective of
     *  their types.  It is assumed that the type of the argument is
     *  UnionToken.
     *  @param rightArgument The token to compare to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return True if the argument is equal to this.
     */
    @Override
    protected BooleanToken _isEqualTo(Token rightArgument)
            throws IllegalActionException {
        UnionToken unionToken = (UnionToken) rightArgument;

        if (!_label.equals(unionToken.label())) {
            return new BooleanToken(false);
        }

        return _value.isEqualTo(unionToken.value());
    }

    /** Return a new token whose value is the modulo of this token and
     *  the argument. It is assumed that the class of the argument is
     *  UnionToken.
     *  @param rightArgument The token to modulo this token by.
     *  @return A new UnionToken.
     *  @exception IllegalActionException If the label of the argument
     *   and this token are different, or calling the modulo method on
     *   the value token throws it.
     */
    @Override
    protected Token _modulo(Token rightArgument) throws IllegalActionException {
        UnionToken unionToken = (UnionToken) rightArgument;

        if (!_label.equals(unionToken.label())) {
            throw new IllegalActionException("UnionToken._modulo: The label "
                    + "of the argument token (" + unionToken.label()
                    + ") is different from that of this token (" + _label + ".");
        }

        Token value = _value.modulo(unionToken.value());
        return new UnionToken(_label, value);
    }

    /** Return a new token whose value is the multiplication of this token
     *  and the argument. It is assumed that the class of the argument is
     *  UnionToken.
     *  @param rightArgument The token to multiply this token by.
     *  @return A new UnionToken.
     *  @exception IllegalActionException If the label of the argument
     *   and this token are different, or calling the multiply method on
     *   the value token throws it.
     */
    @Override
    protected Token _multiply(Token rightArgument)
            throws IllegalActionException {
        UnionToken unionToken = (UnionToken) rightArgument;

        if (!_label.equals(unionToken.label())) {
            throw new IllegalActionException("UnionToken._multiply: The label "
                    + "of the argument token (" + unionToken.label()
                    + ") is different from that of this token (" + _label + ".");
        }

        Token value = _value.multiply(unionToken.value());
        return new UnionToken(_label, value);
    }

    /** Return a new token whose value is the subtraction of this token
     *  and the argument. It is assumed that the class of the argument
     *  is UnionToken.
     *  @param rightArgument The token to subtract from this token.
     *  @return A new UnionToken.
     *  @exception IllegalActionException If the label of the argument
     *   and this token are different, or calling the subtract method on
     *   the value token throws it.
     */
    @Override
    protected Token _subtract(Token rightArgument)
            throws IllegalActionException {
        UnionToken unionToken = (UnionToken) rightArgument;

        if (!_label.equals(unionToken.label())) {
            throw new IllegalActionException("UnionToken._subtract: The label "
                    + "of the argument token (" + unionToken.label()
                    + ") is different from that of this token (" + _label + ".");
        }

        Token value = _value.subtract(unionToken.value());
        return new UnionToken(_label, value);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private String _label;

    private Token _value;
}
