/* A token that contains a reference to an arbitrary object.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.graph.CPO;
import ptolemy.data.type.*;

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
     *   cannot be carried out in a lossless fashion.
     */
    public static Token convert(Token token)
	    throws IllegalActionException {

	int compare = TypeLattice.compare(new ObjectToken(), token);
	if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
	    throw new IllegalActionException("ObjectToken.convert: " +
                    "type of argument: " + token.getClass().getName() +
                    "is higher or incomparable with ObjectToken in the type " +
                    "hierarchy.");
	}

	if (token instanceof ObjectToken) {
	    return token;
	}

	// Argument type is lower, but don't know how to convert it
	// to ObjectToken.
	throw new IllegalActionException("cannot convert from token " +
		"type: " + token.getClass().getName() + " to a ObjectToken");
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

    /** Return the string description of the object.  If there is no such
     *  object, then return a description of the token.
     *  @return A String.
     *  @deprecated Use toString() instead.
     */
    public String stringValue() {
	return toString();
    }

    /** Return the string description of the object.  If there is no such
     *  object, then return a description of the token.
     *  @return A String.
     */
    public String toString() {
        if (_value != null) {
            return _value.toString();
        } else {
            return super.toString();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // This is protected to allow access in derived classes only.
    protected Object _value = null;
}
