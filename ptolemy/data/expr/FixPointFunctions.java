/* Class providing additional FixPoint functions to ptolemyII expression
language.

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

package ptolemy.data.expr;

import ptolemy.data.Token;      // This should not be necessary, but it is.
import ptolemy.data.*;
import ptolemy.math.*;
import ptolemy.data.FixToken;   // For javadoc
import ptolemy.math.Quantizer;  // For javadoc

//////////////////////////////////////////////////////////////////////////
//// FixPointFunctions

/** Class providing additional functions for fix point number to
ptolemyII expression language. The added functionality is
<ul>
<li> Creating a fix point token
        <p><pre>fix( 5.34, 10, 2)</pre><p>

<li> Creating a matrix of fix point tokens
        <p><pre>fix( [ -.040609, -.001628, .17853, .37665, .37665, .17853, -.001628, -.040609 ], 10,  2)</pre><p>

<li> Quantizing doubles to a specific precision
        <p><pre>quantize( [ -.040609, -.001628, .17853, .37665, .37665,
        .17853, -.001628, -.040609 ], 10,  2)</pre><p>

</ul>

@author Bart Kienhuis
@version $Id$
@see PtParser
@see ptolemy.data.FixToken
@see ptolemy.math.Quantizer

*/

public class FixPointFunctions {


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**

        Create a Token with a fix point value of a given precision. This
        function is used by the expression parser to create a FixToken. A
        FixToken is created as

        <p><pre>fix( 5.34, 10, 2)</pre><p>

        This generates a FixToken with the value 5.34 using 10 bits of
        which 2 are used to represent the integer part and 8 for the
        fractional part.

        @param value The value of the FixToken
        @param numberOfBits The total number of bits.
        @param integerBits The number of bits used for the integer part.
    */
    public static Token fix( double value,
            int numberOfBits, int integerBits) {
        return new FixToken( value, numberOfBits, integerBits );
    }

    /** Create a matrix with fix point values of a given
        precision. This method is used by the expression parser to create
        a matrix of fix point tokens. It is created as

        <p><pre>fix( [ -.040609, -.001628, .17853, .37665, .37665, .17853, -.001628, -.040609 ], 10,  2)</pre><p>

        This generates a 1x8 FixMatrixToken. The precision of each fix
        point value is 10 bits of which 2 are used for the integer part
        and 8 for the fraction part.
        @param values The matrix with double values.
        @param numberOfBits The total number of bits.
        @param integerBits The number of bits used for the integer part.

    */
    public static Token fix(  DoubleMatrixToken values,
            int numberOfBits, int integerBits) {
        FixPoint [][] fxa = new FixPoint[1][values.getColumnCount()];
        Precision precision = new Precision( numberOfBits, integerBits );
        for( int i=0; i<values.getColumnCount(); i++) {
            fxa[0][i] = Quantizer.round( values.getElementAt(0,i), precision);
        }
        return new FixMatrixToken( fxa );
    }

    /** Create a matrix of double values that are quantized to a given
        precision. This function is used by the expression parser to
        create a matrix of quantized doubles. The matrix of quantized
        doubles is created as

        <p><pre>quantize( [ -.040609, -.001628, .17853, .37665, .37665,
        .17853, -.001628, -.040609 ], 10,  2)</pre><p>

        This generates a 1x8 matrix of double values, of which each value
        is quantized using a given precision. In this case 10 bits are
        used of which 2 for the integer part and 8 for the fraction part.
        @param values The matrix with quantized double values.
        @param numberOfBits The total number of bits.
        @param integerBits The number of bits used for the integer part.
    */
    public static Token quantize( DoubleMatrixToken values, int numberOfBits,
            int integerBits) {
        double [][] fxa = new double[1][values.getColumnCount()];
        for( int i=0; i<values.getColumnCount(); i++) {
            Precision precision = new Precision( numberOfBits, integerBits );
            fxa[0][i] =
                (Quantizer.round(values.getElementAt(0,i), precision)).doubleValue();;
        }
        return new DoubleMatrixToken( fxa );
    }


}
