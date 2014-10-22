/* A token that contains a set of label/token pairs.

 Copyright (c) 2003-2014 The Regents of the University of California.
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

import org.w3c.dom.Document;

import ptolemy.data.expr.XMLParser;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// XmlToken

/**
 A token that contains a xml document.

 Currently, no operations between function tokens (add, multiply, etc.)
 are supported.

 @author Yang Zhao
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (cxh)
 */
public class XMLToken extends Token {
    /** Construct an empty token.
     */
    public XMLToken() {
        super();
        _toString = "";
    }

    /** Construct an XmlToken from the specified string.
     *  @param init The initialization string.
     *  @exception Exception If the string is not parsable.
     */
    public XMLToken(String init) throws Exception {
        XMLParser parser = new XMLParser();
        _doc = parser.parser(init);
        _toString = init;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Convert the specified token into an instance of XMLToken.
     *  If the specified token is not an instance of XMLToken,
     *  an exception is thrown.
     *  @param token The token to be converted to a XMLToken.
     *  @return A XMLToken.
     *  @exception IllegalActionException If the conversion
     *   cannot be carried out.
     */
    public static XMLToken convert(Token token) throws IllegalActionException {
        if (token instanceof XMLToken) {
            return (XMLToken) token;
        }

        throw new IllegalActionException(notSupportedConversionMessage(token,
                "xmltoken"));
    }

    /** Return the dom document parsed from the xml string.
     *  @return A Document.
     */
    public Document getDomTree() {
        return _doc;
    }

    /** Return true if the argument is an instance of XMLToken with the
     *  same value.
     *  @param object An instance of Object.
     *  @return True if the argument is an IntToken with the same
     *  value. If either this object or the argument is nil, return
     *  false.
     */
    @Override
    public boolean equals(Object object) {
        // See http://www.technofundo.com/tech/java/equalhash.html
        if (object == this) {
            return true;
        }
        if (object == null) {
            return false;
        }
        // This test rules out subclasses.
        if (object.getClass() != getClass()) {
            return false;
        }

        if (isNil() || ((XMLToken) object).isNil()) {
            return false;
        }

        if (((XMLToken) object).toString().equals(_toString)) {
            return true;
        }

        return false;
    }

    /** Return the hash code for the XMLToken object. If two XMLToken
     *  objects contains the same String then they have the same
     *  hashcode.
     *  @return The hash code for this XMLToken object.
     */
    @Override
    public int hashCode() {
        // See http://www.technofundo.com/tech/java/equalhash.html
        int hashCode = 27;
        if (_toString != null) {
            hashCode = 31 * hashCode + _toString.hashCode();
        }
        return hashCode;
    }

    /** Test that the value of this token is close to the first argument,
     *  where "close" means that the distance between them is less than
     *  or equal to the second argument.  This method only makes sense
     *  for tokens where the distance between them is reasonably
     *  represented as a double. If the argument token is not of
     *  the same type as this token, then either this token or the
     *  argument will be converted, if possible, to the type of the other.
     *  <p>
     *  Subclasses should not
     *  generally override this method, but override the protected
     *  _isCloseTo() method to ensure that type conversion is performed
     *  consistently.
     *  @param token The token to test closeness of this token with.
     *  @param epsilon The value that we use to determine whether two
     *   tokens are close.
     *  @return A boolean token that contains the value true if the
     *   value and units of this token are close to those of the
     *   argument token.
     *  @exception IllegalActionException If the argument token and
     *   this token are of incomparable types, or the operation does
     *   not make sense for the given types.
     */
    @Override
    public final BooleanToken isCloseTo(Token token, double epsilon)
            throws IllegalActionException {
        // FIXME: This is copied from AbstractConvertibleToken.

        // Note that if we had absolute(), subtraction() and islessThan()
        // we could perhaps define this method for all tokens.
        int typeInfo = TypeLattice.compare(getType(), token);

        if (typeInfo == CPO.SAME) {
            return _isCloseTo(token, epsilon);
        } else if (typeInfo == CPO.HIGHER) {
            AbstractConvertibleToken convertedArgument = (AbstractConvertibleToken) getType()
                    .convert(token);

            try {
                BooleanToken result = _isCloseTo(convertedArgument, epsilon);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex, notSupportedMessage(
                        "isCloseTo", this, token));
            }
        } else if (typeInfo == CPO.LOWER) {
            return token.isCloseTo(this, epsilon);
        } else {
            throw new IllegalActionException(notSupportedIncomparableMessage(
                    "isCloseTo", this, token));
        }
    }

    /** Return the type of this token.
     *  @return BaseType.XMLTOKEN.
     */
    @Override
    public Type getType() {
        return BaseType.XMLTOKEN;
    }

    /** Return the value of this Token as a string.
     *  @return A String.
     */
    @Override
    public String toString() {
        return _toString;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Test for closeness of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is
     *  an XMLToken.
     *  @param rightArgument The token to add to this token.
     *  @param epsilon The value that we use to determine whether two
     *  tokens are close.  This parameter is ignored by this class.
     *  @return A BooleanToken containing the result.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     */
    protected BooleanToken _isCloseTo(Token rightArgument, double epsilon)
            throws IllegalActionException {
        return _isEqualTo(rightArgument);
    }

    /** Test for equality of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is
     *  XMLToken.
     *  @param rightArgument The token to add to this token.
     *  @return A BooleanToken containing the result.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     */
    protected BooleanToken _isEqualTo(Token rightArgument)
            throws IllegalActionException {
        XMLToken convertedArgument = (XMLToken) rightArgument;
        return BooleanToken.getInstance(toString().compareTo(
                convertedArgument.toString()) == 0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Document _doc;

    private String _toString;
}
