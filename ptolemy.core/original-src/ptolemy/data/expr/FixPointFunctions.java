/* Class providing static functions for PtolemyII expression language
 that operate exclusively on the fixed point data type.

 Copyright (c) 1998-2013 The Regents of the University of California.
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
package ptolemy.data.expr;

import ptolemy.math.FixPoint;
import ptolemy.math.Precision;
import ptolemy.math.Quantizer;

///////////////////////////////////////////////////////////////////
//// FixPointFunctions

/**
 This class provides static functions for operating on Fixpoint numbers
 in the Ptolemy II expression language. The added functionality is
 <ul>
 <li> Create a FixPoint for a double value with a particular
 precision. The result is an instance of <i>FixPoint</i>.
 For example:
 <pre>
 fix(5.34, 10, 4)
 </pre>
 creates a fixed point number with a total of 10 bits, 4 of which are
 integer bits, representing the number 5.34.
</li>
 <li> Create a FixPointMatrix with entries that consist of instances of
 FixPoint. Each entry in the fixed point matrix has
 the same precision. For example,
 <pre>
 fix([ -.040609, -.001628, .17853, .37665, .37665, .17853,
 -.001628, -.040609 ], 10, 2)
 </pre>
 creates a matrix where each entry has 10 bits, two of which are
 integer bits.
</li>
 <li> Create a DoubleToken whose value is the quantized version of the
 given double value. The value is quantized by converting it into a
 fixed point value with a particular precision and then back again to
 a double value.
 For example,
 <pre>
 quantize(5.34, 10, 4)
 </pre>
 quantizes the number 5.34 to 10 bits of precision, 4 of which
 are integer bits.
 </li>
 <li>
 Create a matrix whose entries are the quantized version of the
 values of the given matrix. The values are quantized by converting
 them into a fixed point value with a particular precision and then
 back again into a double value. Each entry is quantized using the same
 precision. The result is an instance of <i>DoubleMatrixToken</i>.
 For example:
 <pre>
 quantize([ -.040609, -.001628, .17853, .37665, .37665, .17853,
 -.001628, -.040609 ], 10, 2)
 </pre>
 creates a new instance of DoubleMatrixToken containing the specified
 values with 10 bits of precision, two of which are integer bits.
 </li>
 </ul>
 In all cases, rounding is used when quantization errors occur,
 and saturation is used when overflow occurs.

 @author Bart Kienhuis, Contributor: Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Yellow (kienhuis)
 @Pt.AcceptedRating Red (kienhuis)
 @see PtParser
 @see ptolemy.math.FixPoint
 @see ptolemy.math.Quantizer
 */
public class FixPointFunctions {
    // The only constructor is private so that this class cannot
    // be instantiated.
    private FixPointFunctions() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a FixPoint representing the specified integer.
     *  For example,
     *  <pre>
     *      fix(5, 10, 4)
     *  </pre>
     *  creates a fixed point representation of the integer 5 with
     *  10 bits of precision, 4 of which are integer bits.
     *
     *  @param value The value to represent.
     *  @param numberOfBits The total number of bits.
     *  @param integerBits The number of bits used for the integer part.
     *  @return A fixed point representation of the value.
     */
    public static FixPoint fix(int value, int numberOfBits, int integerBits) {
        Precision precision = new Precision(numberOfBits, integerBits);
        return Quantizer.round(value, precision);
    }

    /** Create a FixPoint representing the specified double.
     *  For example,
     *  <pre>
     *      fix(5.34, 10, 4)
     *  </pre>
     *  creates a fixed point representation of the numer 5.34 with
     *  10 bits of precision, 4 of which are integer bits.
     *
     *  @param value The value to represent.
     *  @param numberOfBits The total number of bits.
     *  @param integerBits The number of bits used for the integer part.
     *  @return A fixed point representation of the value.
     */
    public static FixPoint fix(double value, int numberOfBits, int integerBits) {
        Precision precision = new Precision(numberOfBits, integerBits);
        return Quantizer.round(value, precision);
    }

    /** Create a double whose value is the quantized version of
     *  the given double value. The value is quantized by converting
     *  it into a fixed point value with a particular precision and
     *  then back again into a double value. For example,
     *  <pre>
     *     quantize(5.34, 10, 4)
     *  </pre>
     *  yields a double representing 5.34 quantized to 10 bits of
     *  precision, of which 4 bits are used
     *  for the integer part and 6 bits are used for the fractional
     *  part.
     *
     *  @param value The value to quantize.
     *  @param numberOfBits The total number of bits.
     *  @param integerBits The number of bits used for the integer part.
     *  @return a double with value that is quantized.
     */
    public static double quantize(double value, int numberOfBits,
            int integerBits) {
        FixPoint fixValue = Quantizer.round(value, new Precision(numberOfBits,
                integerBits));
        return fixValue.doubleValue();
    }
}
