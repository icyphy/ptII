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

//////////////////////////////////////////////////////////////////////////
//// DoubleToken
/** 
A token that contains a double precision number.

@author Yuhong Xiong
$Id$
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

    /** Set the value of the token to be the specified value.
     */
    public void setValue(double value) {
	_value = value;
    }

    /** Return the value in the token as a byte.
     */
    public byte byteValue() {
	return (byte)_value;
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

    // Return a reference to a Fix.
    // FIXME: finish after the Fix class is available.

//    public Fix fixValue() {
//    }

    /** Return the value in the token as an int.
     */
    public int intValue() {
	return (int)_value;
    }

    /** Return the value in the token as a long integer.
     */
    public long longValue() {
	return (long)_value;
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
	    throw new IllegalArgumentException(e.getMessage());
	}
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

