/* Abstract base class for tokens that contain a reference to a 2-D array.

 Copyright (c) 1997-1998 The Regents of the University of California.
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

import pt.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// MatrixToken
/**
Abstract base class for tokens that contain a reference to a 2-D array.
The derived classes should override the clone() method to do a deep copy
of the token.

@author Yuhong Xiong
@version $Id$
*/
public abstract class MatrixToken extends Token {

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Return the number of rows in the matrix.
     */
    public abstract int numRows();

    /** Return the number of columns in the matrix.
     */
    public abstract int numColumns();

    /** Return the content in the token as a 2-D byte array.
     */
    public abstract byte[][] byteMatrix();

    /** Return the content in the token as a 2-D double array.
     */
    public abstract double[][] doubleMatrix();

    /** Return the content in the token as a 2-D Complex array.
     */
    // FIXME: uncomment this method after the Complex class is available.
    // public abstract Complex[][] complexMatrix();

    /** Return the content in the token as a 2-D Fix array.
     */
    // FIXME: uncomment this method after the Complex class is implemented.
    // public abstract Fix[][] fixMatrix();

    /** Return the content in the token as a 2-D integer array.
     */
    public abstract int[][] intMatrix();

    /** Return the content in the token as a 2-D long array.
     */
    public abstract long[][] longMatrix();
}

