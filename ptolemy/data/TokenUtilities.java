/* Assistance methods for token classes.

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
package ptolemy.data;

import java.text.DecimalFormat;

//////////////////////////////////////////////////////////////////////////
//// TokenUtilities

/**
 Various methods and fields that are used from within the various token
 classes.  This code is factored out here into a separate class to allow
 for simple use by the code generator.

 @author  Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Green (neuendor)
 @Pt.AcceptedRating Green (cxh)
 */
public class TokenUtilities {
    ///////////////////////////////////////////////////////////////////
    ////                         public fields                     ////
    // Note that these fields are public, since they are sometimes
    // useful to have in other places, like the code generator.

    /** The format that is used to print floating point numbers that
     *  are not very large, or very small.  The number of fractional
     *  digits here is determined by the place at which common
     *  numbers, such as one half, will get rounded to display nicely.
     */
    public static final DecimalFormat regularFormat = new DecimalFormat(
            "####0.0############");

    // Note: This used to be new DecimalFormat("0.0############E0##"),
    // but compiling with gcj resulted in the following error:
    // 'Exception in thread "main" class
    // java.lang.ExceptionInInitializerError:
    // java.lang.IllegalArgumentException: digit mark following zero
    // in exponent - index: 17'

    /** The format that is used to print floating point numbers that
     *  are very large, or very small.
     */
    public static final DecimalFormat exponentialFormat = new DecimalFormat(
            "0.0############E0");
}
