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
import ptolemy.math.Quantizer;
import ptolemy.math.Precision;
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
@see ptolemy.math.Quantizer
@version $Id$

*/
public class FixToken extends ScalarToken {

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
	@param pre the precision of the FixToken.
	@param value the value of the FixToken.
	@exception IllegalArgumentException If the format of the precision 
	string is incorrect
    */
    public FixToken(double value, String pre) 
            throws IllegalArgumentException {
                try {
                    Precision precision = new Precision( pre );
                    _value = Quantizer.round(value, precision);
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
    public FixToken(double value, int numberOfBits, int integerBits)  
            throws IllegalArgumentException {
                try {                    
                    Precision precision = 
                        new Precision( numberOfBits, integerBits);
                    _value = Quantizer.round(value, precision);
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
	FixPoint result = _value.add( arg.fixValue() );
	return new FixToken(result);
    }

    /** Return a new FixToken with value equal to the division of this
     *  FixToken number and the argument. 
     *  @param arg A FixToken.
     *  @return A new FixToken.  
     */
    public FixToken divide(FixToken arg) {
	FixPoint result = _value.divide( arg.fixValue() );
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
    public FixPoint fixValue() {
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
	FixPoint result = _value.multiply( arg.fixValue() );
	return new FixToken(result);
    }

    /** Returns a new Token representing the multiplicative identity
     *  with the same precision as the current FixToken.  
     *  @return A new Token containing the multiplicative identity.
     */
    public Token one() {
        return new FixToken( 1.0, _value.getPrecision().toString() );
    }

    /** Return a new FixToken with value equal to the subtraction of this
     *  FixToken number and the argument. 
     *  @param arg A FixToken.
     *  @return A new FixToken.  
     */
    public FixToken subtract(FixToken arg) {
	FixPoint result = _value.subtract( arg.fixValue() );
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
        Precision precision = _value.getPrecision();
	return "fix(" + _value.toString() + 
            "," + precision.getNumberOfBits() + 
            "," + precision.getIntegerBitLength() + ")";
    }

    /** Returns a new token representing the additive identity with the
     *  same precision as the current FixToken.  
     *  @return A new Token containing the additive identity.  
     */
    public Token zero()
    {
        return new FixToken( 0.0, _value.getPrecision().toString() );
    }

    /** Set the Rounding mode of the FixPoint number. */
    // fixm: Currently it is a string, should be come a
    // type safe enumerated type
    public void setRoundingMode(String x) {
	// _value.setRounding( (String) x );
    }

    /** Print the content of this FixToken: Debug Function */
    public void print() {
        _value.printFix();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private FixPoint _value;


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    public static FixToken fix( double value, int numberOfBits, 
            int integerBits) {
        System.out.println(" Expression Language: received value,value,value");
        return new FixToken( value, numberOfBits, integerBits );
    }


   public static Token fix(  DoubleMatrixToken values, int numberOfBits, 
           int integerBits) {
       System.out.println(" Expression Language: received array of values");
       System.out.println(" Expression Language: return a Fix matrix ");
       System.out.println(" Precision: (" + numberOfBits + "/" + 
               integerBits + ")" );
       FixPoint [][] fxa = new FixPoint[1][values.getColumnCount()];
       Precision precision = new Precision( numberOfBits, integerBits );
       for( int i=0; i<values.getColumnCount(); i++) {
           fxa[0][i] = Quantizer.round( values.getElementAt(0,i), precision);
           System.out.println(" Result["+i+"] = " + fxa[0][i].toString() );
       }
       return new FixMatrixToken( fxa );
   }
    

   public static Token quantize(  DoubleMatrixToken values, int numberOfBits, 
           int integerBits) {
       System.out.println(" Expression Language: received array of values");
       System.out.println(" Expression Language: return a double matrix ");
       System.out.println(" Precision: (" + numberOfBits + "/" + 
               integerBits + ")" );
       double [][] fxa = new double[1][values.getColumnCount()];
       for( int i=0; i<values.getColumnCount(); i++) {
           // fxa[0][i] = 1.0;
           fxa[0][i] = (new FixToken( values.getElementAt(0,i), numberOfBits, integerBits ) ).doubleValue();
           System.out.println(" Result["+i+"] = " + fxa[0][i] );
       }
       return new DoubleMatrixToken( fxa );
   }


    /**
   public static FixToken fix( DoubleMatrixToken values, int numberOfBits, 
           int integerBits) {
       System.out.println(" Expression Language: received Matrix of values");
       System.out.println(" Precision: (" + numberOfBits + "/" + 
               integerBits + ")" );
       FixToken[] fxa = new FixToken[values.getColumnCount()];
       
       for( int i=0; i<values.getColumnCount(); i++) {
           fxa[i] = new FixToken( values.getElementAt(0,i), numberOfBits, integerBits );
           System.out.println(" Result["+i+"] = " + fxa[i].toString() );
       }      
       return new FixToken(10.0, 16,2);
       }
       */

    public static FixToken fix( double value, String precision ) {
        System.out.println(" Expression Language: received value,precision");
        return new FixToken(value, precision);
    }

}

