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

@ProposedRating Yellow (yuhong@eecs.berkeley.edu)
@AcceptedRating Yellow (wbwu@eecs.berkeley.edu)

*/

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.math.Complex;

//////////////////////////////////////////////////////////////////////////
//// MatrixToken
/**
Abstract base class for tokens that contain a 2-D array.
This class defines methods for type conversion among different matrix
tokens. The implementation in this base class just throws an exception.
Derived classes should override those methods where the corresponding
conversion can be achieved without loss of information.

@author Yuhong Xiong
@version $Id$
*/
public abstract class MatrixToken extends Token {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a copy of the content of this token as a 2-D Complex
     *  array.
     *  In this base class, we just throw an exception.
     *  @return A 2-D Complex array.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     */
    public Complex[][] complexMatrix()
	    throws IllegalActionException {
	String str = "complexMatrix method not supported on "
		+ this.getClass().getName() + " objects.";
	throw new IllegalActionException(str);
    }

    /** Return a copy of the content of this token as a 2-D double array.
     *  In this base class, we just throw an exception.
     *  @return A 2-D double array.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     */
    public double[][] doubleMatrix()
	    throws IllegalActionException {
	String str = "doubleMatrix method not supported on "
		+ this.getClass().getName() + " objects.";
	throw new IllegalActionException(str);
    }

    // Return the content in the token as a 2-D Fix array.
    // FIXME: uncomment this method after the Complex class is implemented.
    // public Fix[][] fixMatrix();

    /** Return a copy of the content of this token as a 2-D integer array.
     *  In this base class, we just throw an exception.
     *  @return A 2-D integer array.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     */
    public int[][] intMatrix()
	    throws IllegalActionException {
	String str = "intMatrix method not supported on "
		+ this.getClass().getName() + " objects.";
	throw new IllegalActionException(str);
    }

    /** Return a copy of the content of this token as a 2-D long array.
     *  In this base class, we just throw an exception.
     *  @return A 2-D long array.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     */
    public long[][] longMatrix()
	    throws IllegalActionException {
	String str = "longMatrix method not supported on "
		+ this.getClass().getName() + " objects.";
	throw new IllegalActionException(str);
    }

    /** Return the number of columns of the contained matrix.
     *  @return An integer representing the number of columns.
     */
    public abstract int getColumnCount();

    /** Return the number of rows of the contained matrix.
     *  @return An integer representing the number of rows.
     */
    public abstract int getRowCount();

    /** Return a new Token representing the right multiplicative
     *  identity. The returned token contains an identity matrix
     *  whose dimension is the same as the number of columns of
     *  the matrix contained in this token.
     *  The implementation in this base class just throws an
     *  exception. This method should be overriden in the subclass
     *  when the right multiplicative identity exists.
     *  @return A new Token containing the right multiplicative identity.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     */
    public Token oneRight()
	    throws IllegalActionException {
	throw new IllegalActionException("Right multiplicative identity " +
		"not supported on " + getClass().getName() + " objects.");
    }
}

