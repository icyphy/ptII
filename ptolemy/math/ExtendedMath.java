/* A library of additional mathematical operations beyond those provided
   by the Java Math class.

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

@ProposedRating Yellow (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.math;

import java.lang.*;
import java.util.*;
import java.lang.reflect.*;


//////////////////////////////////////////////////////////////////////////
//// ExtendedMath
/**
 * ExtendedMath is a library of additional mathematical operations
 * beyond those provided by the Java Math class.
 *
 * @author Albert Chen, William Wu, Edward A. Lee, Jeff Tsay
 * @version $Id$
 */

public class ExtendedMath {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the inverse hyperbolic cosine of the argument.
     *  The argument is required to be greater than one, or an
     *  IllegalArgumentException is thrown (this is a runtime
     *  exception, so it need not be declared).
     *  The returned value is positive.
     *  FIXME: Is the range of the argument correct?
     */
    public static final double acosh(final double x) {
        if (x < 1) {
            throw new IllegalArgumentException("ExtendedMath.acosh: Argument "
                    + "is required to be greater than 1.  Got " + x);
        }
        return Math.log( x+Math.sqrt(x*x-1) );
    }

    /** Return the inverse hyperbolic sine of the argument.
     *  FIXME: What is the assumed range of the argument?
     */
    public static final double asinh(final double x) {
        double result;
        if (x<0) {
            result = -Math.log( -x+Math.sqrt(x*x+1) );
        }
        else {
            result = Math.log( x+Math.sqrt(x*x+1) );
        }
        return result;
    }

    /** Return the hyperbolic cosine of the argument.
     *  FIXME: What is the assumed range of the argument?
     */
    public static final double cosh(final double x) {
        return (Math.exp(x) + Math.exp(-x))/2;
    }

    /** Return the base-10 logarithm of the argument. */
    public static final double log10(final double x) {
        return Math.log(x) * _ONEOVERLN10;
    }

    /** Return the base-2 logarithm of the argument. */
    public static final double log2(final double x) {
        return Math.log(x) * _ONEOVERLN2;
    }

    /** Return the hyperbolic sine of the argument.
     *  FIXME: What is the assumed range of the argument?
     */
    public static final double sinh(final double x) {
        return (Math.exp(x) - Math.exp(-x))/2;
    }

    /** If the argument is less than zero, return -1, otherwise
     *  return 1.
     */
    public static final int sgn(final double x) {
        if (x<0) return -1;
        else return 1;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** sqrt(2) */
    public static final double SQRT_2 = Math.sqrt(2.0);

    /** 1 / sqrt(2) */
    public static final double ONE_OVER_SQRT_2 = 1.0 / SQRT_2;

    /** PI / 2 */
    public static final double PI_OVER_2 = Math.PI * 0.5;

    /** PI / 4 */
    public static final double PI_OVER_4 = Math.PI * 0.25;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private static final double _ONEOVERLN2  = 1.0 / Math.log(2.0);
    private static final double _ONEOVERLN10 = 1.0 / Math.log(10.0);
}
