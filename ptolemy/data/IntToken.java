/* A Particle that contains an integer

 Copyright (c) 1997- The Regents of the University of California.
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
//// IntToken
/** 
A token that contains an integer value.
The value is never null, the default being 0.

@author Mudit Goel, Yuhong Xiong
@version $Id$
*/
public class IntToken extends ScalarToken {

    /** Construct a token with integer 0.
     */	
    public IntToken() {
	_value = 0;
    }

    /** Construct a token with the specified value.
     */
    public IntToken(int value) {
        _value = value;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Set the value of the token to be the specified value.
     */
    public void setValue(int value) {
	_value = value;
    }

    // Return a reference to a Complex. The real part of the Complex
    // is the value in the token, the imaginary part is set to 0.
    // FIXME: finish after the Complex class is available.
 
//    public Complex complexValue() {
//    }

    /** Return the value in the token as a double.
     */
    public double doubleValue() {
        return (double)_value;
    }

    /** Return the value in the token as an int.
     */
    public int intValue() {
        return _value;
    }

    /** Return the value in the token as a long integer.
     */
    public long longValue() {
        return (long)_value;
    }
 
    /** Set the value in the token to the value represented by the
        specified string.
        @exception IllegalArgumentException The string does not contain
         a parsable integer.
     */
    public void fromString(String init)
            throws IllegalArgumentException {
        try {
            _value = (Integer.valueOf(init)).intValue();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /** Create a string representation of the value in the token.
     */
    public String toString() {
        return Integer.toString(_value);
    }

    /** Add the value of the argument Token to the current Token.
     *  @param The token whose value we add to this Token.
     *  @return A token of the appropriate type.
     */
    // FIXME: add handling of Complex and Fix tokens.
    public Token add(Token a)
	    throws IllegalActionException {
	if (a instanceof StringToken) {
	    return new StringToken(this.toString() +
					((StringToken)a).toString());
	} else if (a instanceof ByteToken) {
	    return new IntToken(_value + ((ByteToken)a).intValue());
//	} else if (a instanceof ComplexToken) {
	} else if (a instanceof DoubleToken) {
	    return new DoubleToken(this.doubleValue() +
				((DoubleToken)a).doubleValue());
//	} else if (a instanceof FixToken) {
	} else if (a instanceof IntToken) {
	    return new IntToken(_value + ((IntToken)a).intValue());
	} else if (a instanceof LongToken) {
	    return new LongToken(this.longValue() + ((LongToken)a).longValue());
	} else {
	    throw new IllegalActionException("Can't add " +
		getClass().getName() + "to " + a.getClass().getName());
	}
    }
 
    /** Subtract the value of the argument Token from the current Token.
     *  @param The token whose value we substract from this Token.
     *  @return A token of the appropriate type.
     */
    // FIXME: add handling of Complex and Fix tokens.
    public Token subtract(Token a)
	    throws IllegalActionException {
	if (a instanceof ByteToken) {
	    return new IntToken(_value - ((ByteToken)a).intValue());
//	} else if (a instanceof ComplexToken) {
	} else if (a instanceof DoubleToken) {
	    return new DoubleToken(this.doubleValue() -
					((DoubleToken)a).doubleValue());
//	} else if (a instanceof FixToken) {
	} else if (a instanceof IntToken) {
	    return new IntToken(_value - ((IntToken)a).intValue());
	} else if (a instanceof LongToken) {
	    return new LongToken(this.longValue() - ((LongToken)a).longValue());
	} else {
	    throw new IllegalActionException("Can't subtract " +
		a.getClass().getName() + "from " + getClass().getName());
	}
    }
 
    /** Multiply the value of the argument Token to the current Token.
     *  @param The token whose value we multiply to this Token.
     *  @return A token of the appropriate type.
     */
    // FIXME: add handling of Complex and Fix tokens.
    public Token multiply(Token a)
	    throws IllegalActionException {
	if (a instanceof ByteToken) {
	    return new IntToken(_value * ((ByteToken)a).intValue());
//	} else if (a instanceof ComplexToken) {
	} else if (a instanceof DoubleToken) {
	    return new DoubleToken(this.doubleValue() *
				((DoubleToken)a).doubleValue());
//	} else if (a instanceof FixToken) {
	} else if (a instanceof IntToken) {
	    return new IntToken(_value * ((IntToken)a).intValue());
	} else if (a instanceof LongToken) {
	    return new LongToken(this.longValue() * ((LongToken)a).longValue());
	} else {
	    throw new IllegalActionException("Can't multiply " +
		getClass().getName() + "to " + a.getClass().getName());
	}
    }
 
    /** Divide the value of the argument Token with the current Token.
     *  @param The token whose value we divide with this Token.
     *  @return A token of the appropriate type.
     */
    // FIXME: add handling of Complex and Fix tokens.
    public Token divide(Token a)
	    throws IllegalActionException {
	if (a instanceof ByteToken) {
	    return new IntToken(_value / ((ByteToken)a).intValue());
//	} else if (a instanceof ComplexToken) {
	} else if (a instanceof DoubleToken) {
	    return new DoubleToken(this.doubleValue() /
					((DoubleToken)a).doubleValue());
//	} else if (a instanceof FixToken) {
	} else if (a instanceof IntToken) {
	    return new IntToken(_value / ((IntToken)a).intValue());
	} else if (a instanceof LongToken) {
	    return new LongToken(this.longValue() / ((LongToken)a).longValue());
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
    // FIXME: add handling of Complex and Fix tokens.
    public Token modulo(Token a)
	    throws IllegalActionException {
	if (a instanceof ByteToken) {
	    return new IntToken(_value % ((ByteToken)a).intValue());
//	} else if (a instanceof ComplexToken) {
	} else if (a instanceof DoubleToken) {
	    return new DoubleToken(this.doubleValue() % 
				((DoubleToken)a).doubleValue());
//	} else if (a instanceof FixToken) {
	} else if (a instanceof IntToken) {
	    return new IntToken(_value % ((IntToken)a).intValue());
	} else if (a instanceof LongToken) {
	    return new LongToken(this.longValue() % ((LongToken)a).longValue());
	} else {
	    throw new IllegalActionException("Can't do modulo on " +
		getClass().getName() + "and " + a.getClass().getName());
	}
    }
 
    /** Test for equality of the values of this Token and the argument Token.
     *  @param The token with which to test equality.
     */
    // FIXME: add handling of Complex and Fix tokens.
    public BooleanToken equality(Token a)
	    throws  IllegalActionException {
	if (a instanceof ByteToken) {
	    return new BooleanToken(_value == ((ByteToken)a).intValue());
//	} else if (a instanceof ComplexToken) {
	} else if (a instanceof DoubleToken) {
	    return new BooleanToken(this.doubleValue() ==
				((DoubleToken)a).doubleValue());
//	} else if (a instanceof FixToken) {
	} else if (a instanceof IntToken) {
	    return new BooleanToken(_value == ((IntToken)a).intValue());
	} else if (a instanceof LongToken) {
	    return new BooleanToken(this.longValue() ==
					 ((LongToken)a).longValue());
	} else {
	    throw new IllegalActionException("Can't compare equality between "
		+ getClass().getName() + "and " + a.getClass().getName());
	}
    }
 
    /////////////////////////////////////////////////////////////////////////
    ////                        private variables                        ////
 
    private int _value = 0;
}

