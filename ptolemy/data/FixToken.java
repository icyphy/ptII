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

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Construct a FixToken with value 0.0 and a precision of (16/16)
     */
    public FixToken() {
	_value = new FixPoint("16/16","0.0");
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
    public FixToken(String precision, String init) 
	throws IllegalArgumentException {
	try {
	    _value = new FixPoint(precision,init);
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
    public FixToken(String precision, double value)  
	throws IllegalArgumentException {
	try {
	    _value = new FixPoint(precision, value);
	} catch (NumberFormatException e) {
	    throw new IllegalArgumentException(e.getMessage());
	}
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


    /** Return the value of this token as a Fixpoint.
     *  @return A Fixpoint
     */
    public FixPoint fixpointValue() {
        // FixToken is immutable, so we can just return the value.
        return _value;
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
        return new FixToken("(4.0)", 1.0);
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
    /** Get the value contained in this Token as a String in the form
     *  of "<i>integerbits . fractionbits</i>". This giving the same
     *  string representation on all possible platforms, facilitating a
     *  more robust testing of FixToken.  
     *  @return String in <i>integerbits . fractionbits</i> format
     */
    public String stringValue() {
	return _value.toString();
    }

    /** Return a description of the token as a string.
     *  In this base class, we return the fully qualified class name.
     *  @return A String representation of this token.
     */
    public String toString() {
        String str =  getClass().getName() + "(" + _value.doubleValue() + ")";
        return str;
    }

    /** Returns a new token representing the additive identity with a
     *  default precision of 1 bit for the integer part and 0 bits for
     *  the fractional part.  
     *  @return A new Token containing the additive identity.  
     */
    public Token zero()
    {
        return new FixToken("(1/1)",0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private FixPoint _value;

}

