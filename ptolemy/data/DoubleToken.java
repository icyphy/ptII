/* A token that contains a double precision number.

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

import pt.kernel.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// DoubleToken
/** 
 * A token that contains a double precision number.
 * 
 * @author Yuhong Xiong, Neil Smyth
 * $Id$
 */
public class DoubleToken extends ScalarToken {

    /** Construct a token with double 0.0.
     */
    public DoubleToken() {
	_value = 0.0;
    }

    /** Construct a token with the specified value.
     */
    public DoubleToken(double value) {
	_value = value;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////


    /** Add the value of the argument Token to this Token. Type resolution
     *  also occurs here, with the returned Token type chosen to achieve
     *  a lossless conversion.
     * FIXME: what do do about long in the next six methods?
     *  @param a The token to add to this Token
     *  @exception Thrown if the passed token is not of a type that can be 
     *   added to this Tokens value in a lossless fashion.
     */
    public Token add(pt.data.Token a) throws IllegalActionException {
        if (a instanceof StringToken) {
            String result = toString() + a.toString();
            return new StringToken(result);
        } /*else if (a instanceof ComplexToken) {
            return a.add(this);
        }*/ else if (a instanceof ScalarToken) {
            double tmp = _value + ((ScalarToken)a).doubleValue();
            return new DoubleToken(tmp);
        } else {
            String str = " between " + this.getClass().getName() + " and ";
            str = str + a.getClass().getName();
            throw new IllegalActionException("add method not supported" + str);
        }
    }
     
    /** Subtract the value of the argument Token from this Token. Type 
     *  resolution also occurs here, with the returned Token type chosen to 
     *  achieve a lossless conversion. 
     *  @param a The token to subtract to this Token
     *  @exception Thrown if the passed token is not of a type that can be 
     *   subtracted from this Tokens value in a lossless fashion.
     */
    public Token subtract(Token a) throws IllegalActionException {
        if (a instanceof ScalarToken) {
            double tmp = _value - ((ScalarToken)a).doubleValue();
            return new DoubleToken(tmp);
        } /*else if (a instanceof ComplexToken) {
            return a.subtract(this);
        }*/  else {
            String str = "supported between " + this.getClass().getName();
            str = str + " and " + a.getClass().getName();
            throw new IllegalActionException("subtract method not " + str);
        }
    }
  
    /** Multiply the value of this Token with the value of the argument Token.
     *  Type resolution also occurs here, with the returned Token type 
     *  chosen to achieve a lossless conversion. 
     *  @param a The token to multiply this Token by
     *  @exception Thrown if the passed token is not of a type that can be 
     *   multiplied by this Tokens value in a lossless fashion.
     */
    public Token multiply(Token a) throws IllegalActionException {
         if (a instanceof ScalarToken) {
            double tmp = _value * ((ScalarToken)a).doubleValue();
            return new DoubleToken(tmp);
         } /*else if (a instanceof ComplexToken) {
            return a.multiply(this);
        }*/ else {
            String str = "supported between " + this.getClass().getName();
            str = str + " and " + a.getClass().getName();
            throw new IllegalActionException("multiply method not " + str);
        }
    }
   
    /** Divide the value of this Token with the value of the argument Token.
     *  Type resolution also occurs here, with the returned Token type 
     *  chosen to achieve a lossless conversion. 
     *  @param a The token to divide this Token by
     *  @exception Thrown if the passed token is not of a type that can be 
     *   divide this Tokens value by in a lossless fashion.
     */
    public Token divide(Token a) throws IllegalActionException {
        if (a instanceof ScalarToken) {
            double tmp = _value / ((ScalarToken)a).doubleValue();
            return new DoubleToken(tmp);
        /*} else if (a instanceof ComplexToken) {
             // buggyy, do what???
        }*/ 
        } else {
            String str = "supported between " + this.getClass().getName();
            str = str + " and " + a.getClass().getName();
            throw new IllegalActionException("divide method not " + str);
        }
    }

  
    /** Divide the value of this Token with the value of the argument Token.
     *  Type resolution also occurs here, with the returned Token type 
     *  chosen to achieve a lossless conversion. 
     *  @param a The token to divide this Token by
     *  @exception Thrown if the passed token is not of a type that can be 
     *   divide this Tokens value by in a lossless fashion.
     */
    public Token modulo(Token a) throws IllegalActionException {
        if (a instanceof ScalarToken) {
            double tmp = _value % ((ScalarToken)a).doubleValue();
            return new DoubleToken(tmp);
        } else {
            String str = "supported between " + this.getClass().getName();
            str = str + " and " + a.getClass().getName();
            throw new IllegalActionException("modulo method not " + str);
        }
    }

    /** Test the values of this Token and the argument Token for equality.
     *  Type resolution also occurs here, with the returned Token type 
     *  chosen to achieve a lossless conversion. 
     *  @param a The token to divide this Token by
     *  @exception Thrown if the passed token is not of a type that can be 
     *   compared this Tokens value.
     */
    public BooleanToken equality(Token a) throws IllegalActionException {
        if (a instanceof ScalarToken) {
            if (_value == ((ScalarToken)a).doubleValue()) {
                return new BooleanToken(true);
            } else {
                return new BooleanToken(false);
            } 
        }
        /*} else if (a instanceof ComplexToken) {
            return a.equality(this);
        }*/  else {
            String str = "supported between " + this.getClass().getName();
            str = str + " and " + a.getClass().getName();
            throw new IllegalActionException("equality method not " + str);
        }
    }

    

    // Return a reference to a Complex. The real part of the Complex
    // is the value in the token, the imaginary part is set to 0.
    // FIXME: finish after the Complex class is moved to this package.

//    public Complex complexValue() {
//    }

    /** Return the value in the token as a double.
     */
    public double doubleValue() {
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
	    _value = (Double.valueOf(init)).doubleValue();
	} catch (NumberFormatException e) {
	    throw new IllegalArgumentException(" here na na na " + e.getMessage());
	}
    }
    
    public double getValue() {
        return _value;
    }

    /** Set the value in the token 
     *  @param d The new value for the token
     */
    public void setValue(double d) {
        _value = d;
    }


    /** Create a string representation of the value in the token.
     */
    public String toString() {
	return Double.toString(_value);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                        private variables                         ////
    private double _value;
}

