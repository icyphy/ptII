/** A token that contains a FixPoint number.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Red (kienhuis@eecs.berkeley.edu)
@AcceptedRating Red (kienhuis@eecs.berkeley.edu)

*/

package ptolemy.data;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.math.FixPoint;
import ptolemy.graph.CPO;
import ptolemy.data.type.*;

//////////////////////////////////////////////////////////////////////////
//// FixToken
/**
A token that contains a FixPoint.
<p>
@author Bart Kienhuis
@see ptolemy.data.Token
@see ptolemy.math.FixPoint
@see ptolemy.math.Precision
@version $Id$

*/
public class FixToken extends ScalarToken {

    /** Construct a FixToken with value 0.0 and a precision of (16/16)
     */
    public FixToken() {
	_value = new FixPoint("16/16","0.0", FixPoint.SATURATE );
    }

    /** Construct a FixToken with for the supplied FixPoint value
     *  @param value a FixPoint value
     */
    public FixToken(FixPoint value) {
	_value = value;
    }

    /** Construct a FixToken with a value given as a String and a
	precision given as a String. Since FixToken has a finite
	number uses a finite number of bits to represent a value the
	supplied value is rounded to the nearest value possible given the
	precision, thereby introducing quantization errors.
	@param precision String representing the precision of the FixToken 
	@param init String representing	the value of the FixToken 
	@exception IllegalArgumentException If the format of the precision 
	string is incorrect
    */
    public FixToken(String precision, String init, String mode ) 
	throws IllegalArgumentException {
	try {
	    _value = new FixPoint(precision, init, _setRounding( mode ) );
	} catch (NumberFormatException e) {
	    throw new IllegalArgumentException(e.getMessage());
	}
    }

    /** Construct a FixToken with a value given as a String and a
	precision given as a String. Since FixToken has a finite
	number uses a finite number of bits to represent a value, the
	supplied value is rounded to the nearest value possible given
	the precision, thereby introducing quantization errors.  
	@param precision String giving the precision of the FixToken 
	@param value Double value of the FixToken
	@exception IllegalArgumentException If the format of the
	precision string is incorrect */
    public FixToken(String precision, double value, String mode )  
	throws IllegalArgumentException {
	try {
	    _value = new FixPoint(precision, value, _setRounding( mode ) );
	} catch (NumberFormatException e) {
	    throw new IllegalArgumentException(e.getMessage());
	}
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a FixToken containing the absolute value of the
     *  value of this token.
     *  @return An FixToken. 
     */
    public ScalarToken absolute() {
	// FIXME: implement this method after the FixPoint class supports it
	throw new UnsupportedOperationException("FixToken.absolute: method" +
	    "not implemented yet.");
    }

    /** Return a new FixToken with value equal to the sum of this
     *  FixToken number and the argument. 
     *  @param arg A FixToken.
     *  @return A new FixToken.  
     */
    public FixToken add(FixToken arg) {
	FixPoint result = _value.add( arg.fixpointValue() );
	return new FixToken(result);
    }

    /** Return a new FixToken with value equal to the division of this
     *  FixToken number and the argument. 
     *  @param arg A FixToken.
     *  @return A new FixToken.  
     */
    public FixToken divide(FixToken arg) {
	FixPoint result = _value.divide( arg.fixpointValue() );
	return new FixToken(result);
    }

    /** Return the value of this token as a double.
     *  @return A double
     */
    public double doubleValue() {
	return _value.doubleValue();
    }

    /** Return the value of this token as a Fixpoint.
     *  @return A Fixpoint
     */
    public FixPoint fixpointValue() {
        // FixToken is immutable, so we can just return the value.
        return _value;
    }

    /** Return the type of this token.
     *  @return BaseType.FIX
     */
    public Type getType() {
	return BaseType.FIX;
    }

    /** Check if the value of this token is strictly less than that of the
     *  argument token.
     *  @param arg A ScalarToken.
     *  @return A BooleanToken with value true if this token is strictly
     *   less than the argument.
     *  @exception IllegalActionException If the type of the argument token
     *   is incomparable with the type of this token.
     */
    public BooleanToken isLessThan(ScalarToken arg)
	    throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, arg);
        if (typeInfo == CPO.INCOMPARABLE) {
            throw new IllegalActionException("FixToken.isLessThan: The type" +
		" of the argument token is incomparable with the type of " +
		"this token. argType: " + arg.getType());
	}

	if (typeInfo == CPO.LOWER) {
	    return arg.isLessThan(this);
	}

	// Argument type is lower or equal to this token.
	ScalarToken fixArg = arg;
	if (typeInfo == CPO.HIGHER) {
	    fixArg = (ScalarToken)convert(arg);
	}

	// FIXME: implement this method after the FixPoint class supports it
	throw new UnsupportedOperationException("FixToken.isLessThan: method" +
	    "not implemented yet.");

	// if (_value < intArg.fixValue()) {
	//    return new BooleanToken(true);
	// }
	// return new BooleanToken(false);
    }

    /** Return a new FixToken with value equal to the multiplication
	of this FixToken number and the argument.  
	@param arg A FixToken.  
	@return A new FixToken.  
    */
    public FixToken multiply(FixToken arg) {
	FixPoint result = _value.multiply( arg.fixpointValue() );
	return new FixToken(result);
    }

    /** Returns a new Token representing the multiplicative identity
     *  with a default precision of 1 bit for the integer part and 0
     *  bits for the fractional part.  
     *  @return A new Token containing the multiplicative identity.  
     */
    public Token one() {
        return new FixToken("(4.0)", 1.0, "Saturate" );
    }

    /** Return a new FixToken with value equal to the subtraction of this
     *  FixToken number and the argument. 
     *  @param arg A FixToken.
     *  @return A new FixToken.  
     */
    public FixToken subtract(FixToken arg) {
	FixPoint result = _value.subtract( arg.fixpointValue() );
	return new FixToken(result);
    }
    /** Return the value contained in this Token as a String in the form
     *  of "<i>integerbits . fractionbits</i>". This giving the same
     *  string representation on all possible platforms, facilitating a
     *  more robust testing of FixToken.  
     *  @return String in <i>integerbits . fractionbits</i> format
     *  @deprecated Use toString() instead.
     */
    public String stringValue() {
	return toString();
    }

    /** Return the value contained in this Token as a String in the form
     *  of "<i>integerbits . fractionbits</i>". This giving the same
     *  string representation on all possible platforms, facilitating a
     *  more robust testing of FixToken.  
     *  @return String in <i>integerbits . fractionbits</i> format
     */
    public String toString() {
	return _value.toString();
    }

    /** Returns a new token representing the additive identity with a
     *  default precision of 1 bit for the integer part and 0 bits for
     *  the fractional part.  
     *  @return A new Token containing the additive identity.  
     */
    public Token zero()
    {
        return new FixToken("(1/1)", 0, "Saturate" );
    }

    /** Set the Rounding mode of the FixPoint number. */
    // fixm: Currently it is a string, should be come a
    // type safe enumerated type
    public void setRoundingMode(String x) {
	_value.setRounding( (String) x );
    }

    /** Print the content of this FixToken: Debug Function */
    public void print() {
        _value.printFix();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private FixPoint _value;

    /** Set the overflow mode of the Fixpoint using a string. Added to
     *  make testing easier // FIXME still needed?
     @param name String describing the overflow mode */
    private ptolemy.math.FixPoint$Quantize _setRounding(String name) {
	if ( name.compareTo("Saturate")==0) {
	    return FixPoint.SATURATE;
	    //System.out.println(" -- SATURATE --");
	}
	if ( name.compareTo("Rounding")==0) {
	    return FixPoint.ROUND;
	    //System.out.println(" -- ZERO SATURATE --");
	}
	if ( name.compareTo("Truncate")==0) {
	    return FixPoint.TRUNCATE;
	    //System.out.println(" -- TRUNCATE --");
	}
        return FixPoint.TRUNCATE;
    }

}

