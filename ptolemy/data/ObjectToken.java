/* A token that contains a reference to an arbitrary object.

 Copyright (c) 1997-2003 The Regents of the University of California.
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

@ProposedRating Yellow (yuhong@eecs.berkeley.edu)
@AcceptedRating Yellow (wbwu@eecs.berkeley.edu)

*/

package ptolemy.data;

import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// ObjectToken
/**
A token that contains a reference to an arbitrary object.
Note that when this token constructed, the object passed to the constructor
is not cloned. Thus, care must be exercised to ensure that actors do
not modify that object in a nondeterministic way, unless such nondeterminism
is acceptable.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 0.2
*/
public class ObjectToken extends Token {

    /** Construct an empty token.
     */
    public ObjectToken() {
        super();
    }

    /** Construct a token with a reference to the specified object.
     *  @exception IllegalActionException If the argument is not of
     *  the appropriate type
     *  (may be thrown by derived classes, but is not thrown here).
     */
    public ObjectToken(Object value)
            throws IllegalActionException {
        _value = value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Convert the specified token into an instance of ObjectToken.
     *  This method does lossless conversion.
     *  If the argument is already an instance of ObjectToken,
     *  it is returned without any change. Otherwise, if the argument
     *  is below ObjectToken in the type hierarchy, it is converted to
     *  an instance of ObjectToken or one of the subclasses of
     *  ObjectToken and returned. If none of the above condition is
     *  met, an exception is thrown.
     *  @param token The token to be converted to an ObjectToken.
     *  @return An ObjectToken.
     *  @exception IllegalActionException If the conversion
     *   cannot be carried out.
     */
    public static ObjectToken convert(Token token)
            throws IllegalActionException {
        if (token instanceof ObjectToken) {
            return (ObjectToken)token;
        }

        throw new IllegalActionException(
                notSupportedConversionMessage(token, "object"));
    }

    /** Return true if the argument is an instance of ObjectToken and its
     *  contained object is equal to the object contained in this token,
     *  as tested by the equals() method of the contained object.
     *  @param object An instance of Object.
     *  @return True if the argument is an instance of ObjectToken and its
     *   contained object is equal to the object contained in this token.
     */
    public boolean equals(Object object) {
        // This test rules out subclasses.
        if (object.getClass() != getClass()) {
            return false;
        }

        if (((ObjectToken)object).getValue().equals(_value)) {
            return true;
        }
        return false;
    }

    /** Return the type of this token.
     *  @return BaseType.OBJECT
     */
    public Type getType() {
        return BaseType.OBJECT;
    }

    /** Return the value of the token, a reference to an object.
     *  @return The Object in this token.
     */
    public Object getValue() {
        return _value;
    }

    /** Return a hash code value for this token. This method returns the
     *  hash code of the contained object.
     *  @return A hash code value for this token.
     */
    public int hashCode() {
        return _value.hashCode();
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  The returned syntax looks like a function call to a one argument method
     *  named "object".  The argument is the string representation of the
     *  contained object, or the string "null" if the object is null.  Notice
     *  that this syntax is not currently parseable by the expression language.
     *  @return A String representing the object.
     */
    public String toString() {
        if (_value != null) {
            return "object(" + _value.toString() + ")";
        } else {
            return "object(null)";
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // This is protected to allow access in derived classes only.
    protected Object _value = null;
}
