/* A token that contains an integer number.

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
import pt.graph.Cpo;

//////////////////////////////////////////////////////////////////////////
//// IntegerToken
/** 
 * A token that contains an integer number.
 * 
 * @author Neil Smyth, Yuhong Xiong
 * $Id$
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


    /** Add the value of the argument Token to this Token. Type resolution
     *  also occurs here, with the returned Token type chosen to achieve
     *  a lossless conversion.
     *  @param tok The token to add to this Token.
     *  @exception IllegalActionException Thrown if the passed token 
     *   is not of a type that can be added to this Tokens value in
     *   a lossless fashion.
     */
    public Token add(pt.data.Token tok) throws IllegalActionException {
        int typeInfo = TypeCpo.compare(this, tok);
        try {
            if (typeInfo == Cpo.STRICT_GREATER) {
                return tok.addR(this);
            } else if (tok instanceof IntToken) {
                int result = _value + ((IntToken)tok).getValue();
                return new IntToken(result);
            } else if (typeInfo == Cpo.STRICT_LESS) {
                IntToken tmp = this.convert(tok);
                int result = _value + tmp.getValue();
                return new IntToken(result);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            String str = "add method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + tok.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }
         
    /** Add the value of this Token to the argument Token. Type resolution
     *  also occurs here, with the returned Token type chosen to achieve
     *  a lossless conversion.
     *  @param tok The token to add this Token to.
     *  @exception IllegalActionException Thrown if the passed token 
     *   is not of a type that can be added to this Tokens value in
     *   a lossless fashion.
     */
    public Token addR(pt.data.Token tok) throws IllegalActionException {
        IntToken tmp = this.convert(tok);
        int result = tmp.getValue() + _value;
        return new IntToken(result);
    }
   
    // Return a reference to a Complex. The real part of the Complex
    // is the value in the token, the imaginary part is set to 0.
    // FIXME: finish after the Complex class is moved to this package.

    //    public Complex complexValue() {
    //    }

    /** Used to convert Token types further down the type hierarchy to
     *  the type of this Token. No types below IntToken in lossless 
     *  type hierarchy, so throw an exception if reach here.
     *  @param tok The token to be converted to a IntToken.
     *  @exception IllegalActionException Thrown if the conversion
     *  cannot be carried out in a lossless fashion.
     */
    static public IntToken convert(Token tok) throws IllegalActionException{
        String str = "cannot convert from token type: ";
        str = str + tok.getClass().getName() + " to a ";
        throw new IllegalActionException(str + "IntToken");
    }

    /** Divide the value of this Token with the value of the argument Token.
     *  Type resolution also occurs here, with the returned Token type 
     *  chosen to achieve a lossless conversion. If two integers are divided,
     *  the result may or may not be an integer.
     *  @param tok The token to divide this Token by
     *  @exception IllegalActionException Thrown if the passed token is 
     *  not of a type that can be divide this Tokens value by in a 
     *  lossless fashion.
     */
    public Token divide(Token tok) throws IllegalActionException {
        int typeInfo = TypeCpo.compare(this, tok);
        try {
            if (typeInfo == Cpo.STRICT_GREATER) {
                return tok.divideR(this);
            } else if (tok instanceof IntToken) {
                double result = _value / ((IntToken)tok).doubleValue();
                if ((result - (int)result) == 0) {
                    return new IntToken((int)result);
                } else {
                    return new DoubleToken(result);
                }
            } else if (typeInfo == Cpo.STRICT_LESS) {
                IntToken tmp = this.convert(tok);
                double result = _value / tmp.doubleValue();
                if ((result - (int)result) == 0) {
                    return new IntToken((int)result);
                } else {
                    return new DoubleToken(result);
                }
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            String str = "divide method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + tok.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }

    /** Divide the value of the argument Token by this Token. Type resolution
     *  also occurs here, with the returned Token type chosen to achieve
     *  a lossless conversion.
     *  @param tok The token to be divided by the value of this Token.
     *  @exception IllegalActionException Thrown if the passed token 
     *   is not of a type that can be divided by this Tokens value in
     *   a lossless fashion.
     */
    public Token divideR(pt.data.Token tok) throws IllegalActionException {
        IntToken tmp = this.convert(tok);
        double result = tmp.getValue() / _value;
        if (result == (int)result) {
            return new IntToken((int)result);
        } else {
            return new DoubleToken(result);
        }
    }

    /** Return the value in the token as a double
     */
    public double doubleValue() {
        return _value;
    }
  
    /** Test the values of this Token and the argument Token for equality.
     *  Type resolution also occurs here, with the returned Token type 
     *  chosen to achieve a lossless conversion. 
     *  @param tok The token to divide this Token by
     *  @exception IllegalActionException Thrown if the passed token is 
     *  not of a type that can be compared with this Tokens value.
     */
    public BooleanToken equality(Token tok) throws IllegalActionException {
        int typeInfo = TypeCpo.compare(this, tok);
        try {
            if (typeInfo == Cpo.STRICT_GREATER) {
                return tok.equality(this);
            } else if (tok instanceof IntToken) {
                if ( _value == ((IntToken)tok).getValue()) {
                    return new BooleanToken(true);
                }
                return new BooleanToken(false);
            } else if (typeInfo == Cpo.STRICT_LESS) {
                IntToken tmp = this.convert(tok);
                if ( _value == tmp.getValue()) {
                    return new BooleanToken(true);
                }
                return new BooleanToken(false);
            } else {
                throw new Exception();            
            }
        } catch (Exception ex) {
            String str = "equality method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + tok.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }

    /** Set the value in the token to the value represented by the
     *  specified string.
     *  @exception IllegalArgumentException The string does not contain
     *  a parsable number.
     */
    public void fromString(String init)
	    throws IllegalArgumentException {
	try {
	    _value = (Integer.valueOf(init)).intValue();
	} catch (NumberFormatException e) {
	    throw new IllegalArgumentException(e.getMessage());
	}
    }
    
    /** Get the int value contained by this token.
     */
    public int getValue() {
        return _value;
    }

    /** Return the value in the token as a int.
     */
    public int intValue() {
	return _value;
    }

    /** Return the value in the token as a int.
     */
    public long longValue() {
	return (long)_value;
    }

    /** Get the value of this Token modulo the value of the argument Token.
     *  Type resolution also occurs here, with the returned Token type 
     *  chosen to achieve a lossless conversion. 
     *  @param tok The token to modulo this Token by
     *  @exception IllegalActionException Thrown if the passed token is
     *  not of a type that can be  used with modulo in a lossless fashion.
     */
    public Token modulo(Token tok) throws IllegalActionException {
        int typeInfo = TypeCpo.compare(this, tok);
        try {
            if (typeInfo == Cpo.STRICT_GREATER) {
                return tok.moduloR(this);
            } else if (tok instanceof IntToken) {
                int result = _value % ((IntToken)tok).getValue();
                return new IntToken(result);
            } else if (typeInfo == Cpo.STRICT_LESS) {
                IntToken tmp = this.convert(tok);
                int result = _value % tmp.getValue();
                return new IntToken(result);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            String str = "modulo method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + tok.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }
    /** Modulo the value of the argument Token by this Token. 
     *  Type resolution also occurs here, with the returned Token 
     *  type chosen to achieve a lossless conversion.
     *  @param tok The token to apply modulo to by the value of this Token.
     *  @exception IllegalActionException Thrown if the passed token 
     *   is not of a type that can apply modulo by this Tokens value in
     *   a lossless fashion.
     */
    public Token moduloR(pt.data.Token tok) throws IllegalActionException {
        IntToken tmp = this.convert(tok);
        int result = tmp.getValue() %  _value;
        return new IntToken(result);
    }
   

    /** Multiply the value of this Token with the value of the argument Token.
     *  Type resolution also occurs here, with the returned Token type 
     *  chosen to achieve a lossless conversion. 
     *  @param tok The token to multiply this Token by.
     *  @exception IllegalActionException Thrown if the passed token is 
     *  not of a type that can be multiplied by this Tokens value in 
     *  a lossless fashion.
     */
    public Token multiply(Token tok) throws IllegalActionException {
        int typeInfo = TypeCpo.compare(this, tok);
        try {
            if (typeInfo == Cpo.STRICT_GREATER) {
                return tok.multiplyR(this);
            } else if (tok instanceof IntToken) {
                int result = _value * ((IntToken)tok).getValue();
                return new IntToken(result);
            } else if (typeInfo == Cpo.STRICT_LESS){
                IntToken tmp = this.convert(tok);
                int result = _value * tmp.getValue();
                return new IntToken(result);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            String str = "multiply method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + tok.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        } 
    }
   
    /** Multiply the value of the argument Token by this Token. 
     *  Type resolution also occurs here, with the returned Token 
     *  type chosen to achieve a lossless conversion.
     *  @param tok The token to be multiplied by the value of this Token.
     *  @exception IllegalActionException Thrown if the passed token 
     *   is not of a type that can be multiplied by this Tokens value in
     *   a lossless fashion.
     */
    public Token multiplyR(pt.data.Token tok) throws IllegalActionException {
        IntToken tmp = this.convert(tok);
        int result = tmp.getValue() * _value;
        return new IntToken(result);
    }
    
    /** Returns the multiplicative identity. 
     */
    public Token one() {
        return new IntToken(1);
    }

    /** Set the value in the token 
     *  @param d The new value for the token
     */
    public void setValue(int d) {
        _value = d;
    }

    /** Get the value contained in this Token as a String.
     */
    public String stringValue() {
        return Integer.toString(_value);
    }

    /** Subtract the value of the argument Token from this Token. Type 
     *  resolution also occurs here, with the returned Token type chosen to 
     *  achieve a lossless conversion. 
     *  @param tok The token to subtract to this Token.
     *  @exception IllegalActionException Thrown if the passed token is 
     *   not of a type that can be subtracted from this Tokens value in
     *   a lossless fashion.
     */
    public Token subtract(pt.data.Token tok) throws IllegalActionException {
        int typeInfo = TypeCpo.compare(this, tok);
        try {
            if (typeInfo == Cpo.STRICT_GREATER) {
                return tok.addR(this);
            } else if (tok instanceof IntToken) {
                int result = _value -  ((IntToken)tok).getValue();
                return new IntToken(result);
            } else if (typeInfo == Cpo.STRICT_LESS){
                IntToken tmp = this.convert(tok);
                int result = _value - tmp.getValue();
                return new IntToken(result);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            String str = "subtract method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + tok.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }

    /** Subtract the value of this Token from the argument Token. Type 
     *  resolution also occurs here, with the returned Token type 
     *  chosen to achieve a lossless conversion.
     *  @param tok The token to add this Token to.
     *  @exception IllegalActionException Thrown if the passed token 
     *   is not of a type that can be added to this Tokens value in
     *   a lossless fashion.
     */
    public Token subtractR(pt.data.Token tok) throws IllegalActionException {
        IntToken tmp = this.convert(tok);
        int result = _value - tmp.getValue();
        return new IntToken(result);
    }
   
    /** Return a representation of the token as a String.
     */
    public String toString() {
        String str = getClass().getName() + "(" + stringValue() + ")";
        return str;
    }

    /** Returns the additive identity. 
     */
    public Token zero() {
        return new IntToken(0);
    }
        

    //////////////////////////////////////////////////////////////////////////
    ////                        private variables                         ////
    private int _value;
}

