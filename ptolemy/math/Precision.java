/** Precision indicates the precision of a FixPoint number.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Yellow (kienhuis@eecs.berkeley.edu)
@AcceptedRating Red (kienhuis@eecs.berkeley.edu)

*/

package ptolemy.math;

import ptolemy.kernel.util.IllegalActionException;
import java.text.NumberFormat;
import java.util.StringTokenizer;

/** 

This class describes the precision of a Fixpoint. A Fixpoint consists
of two finite bit strings; an integer part and a fractional part. The
total length of the FixPoint is determined by the combined length of
the integer part and the fractional part. The precision determines the
length of the integer and fractional part of a Fixpoint using either a
particular string representation, or by supping a particular length
for the integer and fractional part. <p>

The precision of a Fixpoint can be noted using different string formats

<ul>

<li> <b>m/n</b> <br> The total bit length of the Fixpoint is equal to
<i>m</i> bits and the integer part is equal to <i>n</i> bits. The
fractional part it thus equal to <i>m-n</i> bits.

<li> <b>m.n</b> <br> The total length of the Fixpoint is equal to
<i>n+m</i> bits. The integer part is <i>m</i> bits long and the
fractional part is <i>n</i> bits long.

<li> <b>m^e</b> <br> The Fixpoint can represent all numbers between
-2^m < number < 2^m with a resolution of <i>e</i> bits. This is
equivalent to saying that the the total number of bits available is
<i>e</i> and that <i>m</i> bits are used to describe the integer part.

</ul>

In describing the precision, one can place optionally brackets around
the precision description. Thus "(16/4)" and "16/4" represent the same
precision.


@author Bart Kienhuis
@version $Id$ 
@see FixPoint
*/

public class Precision {

    /**
       Construct a Precision object based on the provided string. The
       string can described the precision in three different modes
       namely; (m/n), (m.n), or (m^e) as explained in the description
       of this class.
       @param precision The string representing the precision.       
       @exception IllegalArgumentException If the precision string
       supplied does not match one of the known formats.  */
    public Precision(String precision) throws IllegalArgumentException
    {
	// Check which format is used
	boolean done = false;
	int type = 0;
	StringTokenizer st = new StringTokenizer(precision);
	if ( precision.indexOf('/', 0) != -1 ) {
	    done = true;
	    type = 1;
	    st = new StringTokenizer(precision,"/()\"");
	}
	if ( precision.indexOf('.', 0) != -1 ) {
	    done = true;
	    type = 2;
	    st = new StringTokenizer(precision,".()\"");
	}
	if ( precision.indexOf('^', 0) != -1 ) {
	    done = true;
	    type = 3;
	    st = new StringTokenizer(precision,"^()\"");
	}

	// throw an exception
	if (( done == false ) || ( st.countTokens() <1) ||
	    (st.countTokens() > 2 )) {
	    throw new IllegalArgumentException("The precision string " + 
                    precision + " uses an incorrect " +
                    "precision format" );
	}

        int  first = 0;
        int second = 0;

        // The string might contain only a single number...
        try { 
            first = (new Integer(st.nextToken())).intValue();
            second = (new Integer(st.nextToken())).intValue();
        } catch ( Exception e ) {
            throw new IllegalArgumentException("A precision string " +
                    " consists of two integers separated " +
                    " by a '/', '^', or '.' token" );
        }

	// Depending on the type, interpret the two values
	if ( type == 1 ) {
	    _length = first;
	    _integerBits = second;
	}

	if ( type == 2 ) {
	    _length = first + second;
	    _integerBits = first;
	}

	if ( type == 3 ) {
	    _length = second;
	    _integerBits = first;
	}
	_fraction = _length - _integerBits;

	if (_length <= 0 || _integerBits < 0 || _integerBits > _length) {
	    throw new IllegalArgumentException("Incorrect definition of " +
                    "Precision. Do not use negative values or have an " +
                    "integer part larger than the total length ");
	}

    }

    /** Construct a Precision object based on the provided
        numbers. These number define the precision in the (m/n)
        format. Thus the precision is given by the total number of
        bits used (m) and the number of bits used to represent the
        integer part (n).  
        @param length The total number of bits.
        @param integerBits Total number of integer bits.          
        @exception IllegalArgumentException If the given values are
        negative or when the integer number of bits is larger than the
        total number of bits.
    */
    public Precision(int length, int integerBits)
            throws IllegalArgumentException {
	if (length <= 0 || integerBits < 0 || integerBits > length) {
	    throw new IllegalArgumentException("Incorrect definition of " +
                    "Precision. Do not use negative values or have an " +
                    "integer part larger than the total length ");
	}
	_length   = length;
	_integerBits  = integerBits;
	_fraction = length - integerBits;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the number of bit representing the
        fractional part of a Fixpoint
	@return length of Fractional part.
    */
    public int getFractionBitLength() {
	return _fraction;
    }

    /** Return the number of bits representing the
        integer part of a Fixpoint
	@return length of Integer part.
    */
    public int getIntegerBitLength() {
	return _integerBits;
    }
    /** Return the total number of bits representing a Fixpoint
	@return Total number of bits.
     */
    public int getNumberOfBits() {
	return _length;
    }


    /** Return the precision
	@return The precision
    */
    public Precision getPrecision() {
	return this;
    }

    /** Return the precision that is the maximum of the two supplied
        precision in both the integer and fractional part. This method
        is used to allign Fixpoints onto a single precision
        representation.  
        @param precisionA a Precision 
        @param precisionB a Precision 
        @return Maximum Precision
    */
    public Precision matchThePoint(Precision precisionA,
				   Precision precisionB)
    {
	int bitright   = Math.max(precisionA.getFractionBitLength(),
				  precisionB.getFractionBitLength());
	int newIntLength = Math.max(precisionA.getIntegerBitLength(),
				    precisionB.getIntegerBitLength());
	int newLength  = newIntLength+bitright;
	return new Precision(newLength, newIntLength);
    }

    /** Return a string representing the Precision. The string is
	expressed using the <i>m.n</i> notation, where <i>m</i>
	indicates the number of bits used to represent the integer
	part and <i>n</i> represents the fractional part of a Fixpoint.
        @return string representing the Precision
    */
    public String toString() {
	String x = "(" + _integerBits + "." + (_length - _integerBits) + ")";
	return x;
    }

    /** Returns the maximal obtainable value for the given
        precision. When <i>m</i> represents the total number of bits
        and <i>n</i> the number of integer bits, this is equal to<p>
        <pre> 2^(n-1) - 1/(2^(m-n) </pre> 
        @return The maximal value obtainable for the given precision
    */
    public double findMax() {
        int ln = getNumberOfBits();
        int ib = getIntegerBitLength();
        double tmp = Math.pow(2,ib-1) - ( 1.0 / Math.pow(2, (ln - ib)) );
        return tmp;
    }

    /** Returns the minimal obtainable value for the given
        precision. When <i>m</i> represents the total number of bits
        and <i>n</i> the number of integer bits, this is equal to<p>
        <pre> -2^(n-1) </pre> 
        @return The minimal value obtainable for the given precision
    */
    public double findMin() {
        int ib = getIntegerBitLength();
        double tmp = -1*Math.pow(2,ib-1);
        return tmp;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** total numer of bits. */
    private int _length   = 0;
    
    /** number of bits in the integer part. */
    private int _integerBits  = 0;

    /** number of bits in the fractional part. */
    private int _fraction = 0;

}
