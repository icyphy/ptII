/* A token that contains a long integer.

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

package pt.data;
import pt.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// LongToken
/** 
A token that contains a long integer.

@author Yuhong Xiong
$Id$
*/
public class LongToken extends ScalarToken {

    /** Construct a token with long integer 0.
     */
    public LongToken() {
	_value = 0L;
    }

    /** Construct a token with the specified value.
     */
    public LongToken(long value) {
	_value = value;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Set the value of the token to be the specified value.
     */
    public void setValue(long value) {
	_value = value;
    }

    /** Return the value in the token as a long integer.
     */
    public long longValue() {
	return _value;
    }

    /** Set the value in the token to the value represented by the
        specified string.
        @exception IllegalArgumentException The string does not contain
         a parsable number.
     */
    public void fromString(String init)
	    throws IllegalArgumentException {
	try {
	    _value = (Long.valueOf(init)).longValue();
	} catch (NumberFormatException e) {
	    throw new IllegalArgumentException(e.getMessage());
	}
    }

    /** Create a string representation of the value in the token.
     */
    public String toString() {
	return Long.toString(_value);
    }

    /** Add the value of the argument Token to the current Token.
     *  @param The token whose value we add to this Token
     *  @return A token of the appropriate type.
     */
    public Token add(Token a)
            throws IllegalActionException {
        if (a instanceof StringToken) {
            return new StringToken(this.toString() +
                                        ((StringToken)a).toString());
        } else if (a instanceof ByteToken) {
            return new LongToken(_value + ((ByteToken)a).longValue());
        } else if (a instanceof IntToken) {
            return new LongToken(_value + ((IntToken)a).longValue());
        } else if (a instanceof LongToken) {
            return new LongToken(_value + ((LongToken)a).longValue());
        } else {
            throw new IllegalActionException("Can't add " +
                getClass().getName() + "to " + a.getClass().getName());
        }
    }

    /** Subtract the value of the argument Token from the current Token.
     *  @param The token whose value we substract from this Token.
     *  @return A token of the appropriate type.
     */
    public Token subtract(Token a)
            throws IllegalActionException {
        if (a instanceof ByteToken) {
            return new LongToken(_value - ((ByteToken)a).longValue());
        } else if (a instanceof IntToken) {
            return new LongToken(_value - ((IntToken)a).longValue());
        } else if (a instanceof LongToken) {
            return new LongToken(_value - ((LongToken)a).longValue());
        } else {
            throw new IllegalActionException("Can't subtract " +
                a.getClass().getName() + "from " + getClass().getName());
        }
    }
 
    /** Multiply the value of the argument Token to the current Token.
     *  @param The token whose value we multiply to this Token.
     *  @return A token of the appropriate type.
     */
    public Token multiply(Token a)
            throws IllegalActionException {
        if (a instanceof ByteToken) {
            return new LongToken(_value * ((ByteToken)a).longValue());
        } else if (a instanceof IntToken) {
            return new LongToken(_value * ((IntToken)a).longValue());
        } else if (a instanceof LongToken) {
            return new LongToken(_value * ((LongToken)a).longValue());
        } else {
            throw new IllegalActionException("Can't multiply " +
                getClass().getName() + "to " + a.getClass().getName());
        }
    }

    /** Divide the value of the argument Token with the current Token.
     *  @param The token whose value we divide with this Token.
     *  @return A token of the appropriate type.
     */
    public Token divide(Token a)
            throws IllegalActionException {
        if (a instanceof ByteToken) {
            return new LongToken(_value / ((ByteToken)a).longValue());
        } else if (a instanceof IntToken) {
            return new LongToken(_value / ((IntToken)a).longValue());
        } else if (a instanceof LongToken) {
            return new LongToken(_value / ((LongToken)a).longValue());
        } else {
            throw new IllegalActionException("Can't divide " +
                getClass().getName() + "by " + a.getClass().getName());
        }
    }
 
    /** Find the result of the value of this Token modulo the value of the
     *  argument Token.
     *  @param The token whose value we do modulo with.
     *  @return A token of the appropriate type.
     */
    public Token modulo(Token a)
            throws IllegalActionException {
        if (a instanceof ByteToken) {
            return new LongToken(_value % ((ByteToken)a).longValue());
        } else if (a instanceof IntToken) {
            return new LongToken(_value % ((IntToken)a).longValue());
        } else if (a instanceof LongToken) {
            return new LongToken(_value % ((LongToken)a).longValue());
        } else {
            throw new IllegalActionException("Can't do modulo on " +
                getClass().getName() + "and " + a.getClass().getName());
        }
    }

    /** Test for equality of the values of this Token and the argument Token.
     *  @param The token with which to test equality.
     */
    public BooleanToken equality(Token a)
            throws  IllegalActionException {
        if (a instanceof ByteToken) {
            return new BooleanToken(_value == ((ByteToken)a).longValue());
        } else if (a instanceof IntToken) {
            return new BooleanToken(_value == ((IntToken)a).longValue());
        } else if (a instanceof LongToken) {
            return new BooleanToken(_value == ((LongToken)a).longValue());
        } else {
            throw new IllegalActionException("Can't compare equality between "
                + getClass().getName() + "and " + a.getClass().getName());
        }
    }
 
    //////////////////////////////////////////////////////////////////////////
    ////                        private variables                         ////
    private long _value;
}

