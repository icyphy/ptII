/* A token that contains a boolean variable.

 Copyright (c) 1997 The Regents of the University of California.
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

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// BooleanToken
/**
 * A token that contains a boolean variable.
 *
 * @author Neil Smyth
 * @version $Id$
*/

public class BooleanToken extends Token {

    /** Construct a token with value false
     */
    public BooleanToken() {
	_value = false;
    }

    /** Construct a token with the specified value.
     */
    public BooleanToken(boolean b) {
	_value = b;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Test to see if the argument Token has the same truth value as this
     *  Token.
     *  @param the Token to compare truth values against.
     *  @exception IllegalActionException Thrown if the argument Token 
     *  is not a BooleanToken
     */
    public BooleanToken equality(Token a) throws IllegalActionException {
        if ( !(a instanceof BooleanToken)) {
            String str = "Cannot compare a BooleanToken with a ";
            throw new IllegalActionException(str + "non-BooleanToken");
        }
        boolean arg = ((BooleanToken)a).getValue();
        if ((_value && arg) || !(_value || arg)) {
            return new BooleanToken(true);
        }
        return new BooleanToken(false);
    }

    /** Set the value in the token to the value represented by the
     *  specified string. If string is true, then set to true. Else false.
     *  @exception IllegalArgumentException The string does not contain
     *  a parsable number.
     */
    public void fromString(String init)
	    throws IllegalArgumentException {
                _value = (Boolean.valueOf(init)).booleanValue();
    }

    /** Returns the value currently stored in this BooleanToken
     */
    public boolean getValue() {
        return _value;
    }

    /** Negate the value stored by this BooleanToken */
    public void negate() {
        if (_value) {
            _value = false;
        } else {
            _value = true;
        }
    }


    /** Set the value of the token to be the specified value.
     *  @param The boolean value to be contained in this token
     */
    public void setValue(boolean b) {
	_value = b;
    }

    /** Create a string representation of the value in the token.
     */
    public String toString() {
        String str = getClass().getName() + "(" + _value + ")";
        return str;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                        private variables                         ////
    private boolean _value;
}

