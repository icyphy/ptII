/* Abstract base class for tokens that contain a 2-D array.

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

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.math.Complex;

//////////////////////////////////////////////////////////////////////////
//// MatrixToken
/**
Abstract base class for tokens that contain a 2-D array.
The derived classes should override the clone() method to do a deep copy
of the token.

@author Yuhong Xiong
$Id$
*/
public abstract class MatrixToken extends Token {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the number of rows of the containted matrix.
     *  In this base class, we just throw an exception.
     *  @return An integer
     *  @exception IllegalActionException alway thrown
     */
    public int numRows()
	    throws IllegalActionException {
	throw new IllegalActionException("MatrixToken.numRow: This base "
		+ "class does not contain an array.");
    }

    /** Return the number of columns of the containt matrix.
     *  In this base class, we just throw an exception.
     *  @return An integer
     *  @exception IllegalActionException alway thrown
     */
    public int numColumns()
	    throws IllegalActionException {
	throw new IllegalActionException("MatrixToken.numColumn: This base "
		+ "class does not contain an array.");
    }

    /** Return the content of this token as a 2-D double array.
     *  In this base class, we just throw an exception.
     *  @return A 2-D double array
     *  @exception IllegalActionException alway thrown
     */
    public double[][] doubleMatrix()
	    throws IllegalActionException {
	throw new IllegalActionException("MatrixToken.doubleMatrix: This base "
		+ "class does not contain an array.");
    }

    /** Return the content of this token as a 2-D Complex array.
     *  In this base class, we just throw an exception.
     *  @return A 2-D Complex array
     *  @exception IllegalActionException alway thrown
     */
    public Complex[][] complexMatrix()
	    throws IllegalActionException {
	throw new IllegalActionException("MatrixToken.complexMatrix: "
		+ "This base class does not contain an array.");
    }

    /** Return the content in the token as a 2-D Fix array.
     */
    // FIXME: uncomment this method after the Complex class is implemented.
    // public Fix[][] fixMatrix();

    /** Return the content of this token as a 2-D integer array.
     *  In this base class, we just throw an exception.
     *  @return A 2-D integer array
     *  @exception IllegalActionException alway thrown
     */
    public int[][] intMatrix()
	    throws IllegalActionException {
	throw new IllegalActionException("MatrixToken.intMatrix: This base "
		+ "class does not contain an array.");
    }

    /** Return the content of this token as a 2-D long array.
     *  In this base class, we just throw an exception.
     *  @return A 2-D long array
     *  @exception IllegalActionException alway thrown
     */
    public long[][] longMatrix()
	    throws IllegalActionException {
	throw new IllegalActionException("MatrixToken.longMatrix: This base "
		+ "class does not contain an array.");
    }
}

