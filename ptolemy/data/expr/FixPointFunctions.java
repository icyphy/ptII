/* Class providing static functions for PtolemyII expression language
that operate exclusively on the fixed point data type.

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

package ptolemy.data.expr;

import ptolemy.data.Token;      // This should not be necessary, but it is.
import ptolemy.data.*;
import ptolemy.math.*;
import ptolemy.data.FixToken;   // For javadoc
import ptolemy.math.Quantizer;  // For javadoc

//////////////////////////////////////////////////////////////////////////
//// FixPointFunctions

/**

This class provides static functions for operating on Fixpoint numbers
in the Ptolemy II expression language. The added functionality is

<ul>

<li> Create a FixToken for a double value with a particular
precision. The result is an instance of <i>FixToken</i>.<<p>

<pre>fix(5.34,10, 4)</pre><p>

<li> Create a FixTokenMatrix which entries consist of instances of
FixToken for a given matrix. Each entry in the fixed point matrix has
the same precision. The result is an instance of <i>FixMatrixToken</i>
<p>

<pre>fix([ -.040609, -.001628, .17853, .37665, .37665, .17853,
-.001628, -.040609 ], 10, 2)</pre><p>

<li> Create a DoubleToken whose value is the quantized version of the
double value given. The value is quantized by converting it into a
fixed point value with a particular precision and then back again into
a double value. The result is an instance of <i>DoubleToken</i>.<<p>

<pre>quantize(5.34, 10, 4)</pre><p>

<li> Create a matrix whose entries are the quantized version of the
values of the given matrix. The values are quatnized by converting
them into a fixed point value with a particular precisoin and then
back again into a double value. Each entry is quantized using the same
precision. The result is an instance of <i>DoubleMatrixToken</i>.<p>

<pre>quantize([ -.040609, -.001628, .17853, .37665, .37665, .17853,
-.001628, -.040609 ], 10, 2)</pre><p>


</ul>

@author Bart Kienhuis
@version $Id$
@see PtParser
@see ptolemy.data.FixToken
@see ptolemy.math.Quantizer
*/

public class FixPointFunctions {

    // The only constructor is private so that this class cannot
    // be instantiated.
    private FixPointFunctions() {}

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**  Create a FixToken for a double value with a particular
     *   precision.
     *
     *  <p><pre>fix(5.34,10, 4)</pre><p>
     *
     *  In the example shown, the value of 5.34 is converted into a
     *  fixed point value that uses 10 bits; 4 bits are used for the
     *  integer part and 8 bits are used for the fractional part. The
     *  result is an instance of <i>FixToken</i>.
     *
     *  @param value The integer value of the FixToken
     *  @param numberOfBits The total number of bits.
     *  @param integerBits The number of bits used for the integer part.
     *  @return a FixToken.
     */
    public static Token fix(int value, int numberOfBits, int integerBits) {
        return new FixToken(value, numberOfBits, integerBits);
    }


    /** Create a FixTokenMatrix which entries consist of instances of
     *  FixToken for a given matrix. Each entry in the fixed point
     *  matrix has the same precision.
     *
     *  <p><pre>fix([ -.040609, -.001628, .17853, .37665, .37665,
     *  .17853, -.001628, -.040609 ], 10, 2)</pre><p>
     *
     *  In the example shown, the entries of the 8x1 matrix are
     *  converted into fixed point values with a precision of 10 bits
     *  of which 2 bit are used for the integer part and 8 bits are
     *  used for the fractional part. The result is an instance of
     *  <i>FixTokenMatrix</i>.
     *
     *  @param value The double value of the FixToken
     *  @param numberOfBits The total number of bits.
     *  @param integerBits The number of bits used for the integer part.
     *  @return a FixTokenMatrix.
     */
    public static Token fix(double value, int numberOfBits, int integerBits) {
        return new FixToken(value, numberOfBits, integerBits);
    }

    /** Create a matrix with fix point values of a given
     *  precision. This method is used by the expression parser to
     *  create a matrix of fix point tokens. It is created as
     *
     *  <p><pre>fix([ -.040609, -.001628, .17853, .37665, .37665,
     *  .17853, -.001628, -.040609 ], 10,  2)</pre><p>
     *
     *  This generates a 1x8 FixMatrixToken. The precision of each fix
     *  point value is 10 bits of which 2 are used for the integer
     *  part and 8 for the fraction part.
     *
     *  @param values The matrix with double values.
     *  @param numberOfBits The total number of bits.
     *  @param integerBits The number of bits used for the integer part.
     */
    public static Token fix(DoubleMatrixToken values,
            int numberOfBits, int integerBits) {
        FixPoint [][] fxa = new FixPoint[1][values.getColumnCount()];
        Precision precision = new Precision(numberOfBits, integerBits);
        for(int i = 0; i < values.getColumnCount(); i++) {
            fxa[0][i] = Quantizer.round(values.getElementAt(0,i), precision);
        }
        return new FixMatrixToken(fxa);
    }

    /** Create a DoubleToken whose value is the quantized version of
     *  the double value given. The value is quantized by converting
     *  it into a fixed point value with a particular precision and
     *  then back again into a double value.
     *
     *  <p><pre>quantize(5.34, 10, 4)</pre><p>
     *
     *  In the example shown, the value of 5.34 is converted into a
     *  fixed point value that uses 10 bits, of which 4 bits are used
     *  for the integer part and 6 bits are used for the fractional
     *  part. The result is an instance of <i>DoubleToken</i>.
     *
     *  @param value The value that needs to be quantized.
     *  @param numberOfBits The total number of bits.
     *  @param integerBits The number of bits used for the integer part.
     *  @return a DoubleToken.
     *  @see ptolemy.math.Quantizer
     */
    public static Token quantize(double value, int numberOfBits,
            int integerBits) {
        FixPoint fixValue = Quantizer.round(value,
                new Precision(numberOfBits, integerBits));
        return new DoubleToken( fixValue.doubleValue());
    }

    /** Create a matrix whose entries are the quantized version of the
     *  values of the given matrix. The values are quatnized by
     *  converting them into a fixed point value with a particular
     *  precisoin and then back again into a double value. Each entry
     *  is quantized using the same precision.
     *
     *  <p><pre>quantize([ -.040609, -.001628, .17853, .37665, .37665,
     *  .17853, -.001628, -.040609 ], 10, 2)</pre><p>
     *
     *  In the example shown, the matrix entries are converted into
     *  fixed point values with a precision of (10/2). Thus 10 bits
     *  are used of which 2 bits are used for the integer part and 8
     *  bits are used for the fractional part. These values are then
     *  converted back into doubles. The result is an instance of
     *  <i>DoubleTokenMatrix</i>.
     *
     *  @param values The matrix with quantized double values.
     *  @param numberOfBits The total number of bits.
     *  @param integerBits The number of bits used for the integer part.
     *  @return a DoubleMatrixToken.
     *  @see ptolemy.math.Quantizer
     */
    public static Token quantize(DoubleMatrixToken values, int numberOfBits,
            int integerBits) {
        double [][] fxa = new double[1][values.getColumnCount()];
        for(int i = 0; i < values.getColumnCount(); i++) {
            Precision precision = new Precision(numberOfBits, integerBits);
            fxa[0][i] =
                (Quantizer.round(
                        values.getElementAt(0, i), precision)).doubleValue();;
        }
        return new DoubleMatrixToken(fxa);
    }

}
