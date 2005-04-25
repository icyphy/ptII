/* A utilities class for mathematics.

Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.math;


/** This class provides a utilities library that includes a set of
    mathematical functions.

    @author Haiyang Zheng
    @version $Id$
    @since Ptolemy II 4.1
    @Pt.ProposedRating Red (hyzheng)
    @Pt.AcceptedRating Red (hyzheng)
*/
public class Utilities {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Given a double value, return a new double with precision as
     *  given.
     *  @param value The double value.
     *  @param precision The precision.
     *  @return A double value with the given precision.
     */
    public static double round(double value, int precision) {
        // NOTE: when the value is too big, e.g. close to the
        // maximum double value, the following algorithm will
        // get overflow, which gives a wrong answer.
        double newValue = Math.round(value * Math.pow(10, precision)) / Math
            .pow(10, precision);
        return newValue;
    }
}
