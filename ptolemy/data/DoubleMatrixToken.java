/* A token that contains a 2-D double array.

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

package ptolemy.data;
import ptolemy.math.Complex;

//////////////////////////////////////////////////////////////////////////
//// DoubleMatrixToken
/**
A token that contains a 2-D double array.

@author Yuhong Xiong
@version $Id$
*/
public class DoubleMatrixToken extends MatrixToken {

    /** Construct a DoubleMatrixToken with the specified 2-D array.
     *  This method makes a copy of the array and stores the copy, 
     *  so changes on the specified array after this token is
     *  constructed will not affect the content of this token.
     *  @exception NullPointerException If the specified array
     *   is null.
     */
    public DoubleMatrixToken(double[][] value) {
	_numRows = value.length;
	_numColumns = value[0].length;
	_value = new double[_numRows][_numColumns];
	for (int i = 0; i < _numRows; i++) {
	    for (int j = 0; j < _numColumns; j++) {
		_value[i][j] = value[i][j];
	    }
	}
    }

    // FIXME: finish this method after array is added to the 
    // 	      expression language.
    // Construct an DoubleMatrixToken from the specified string.
    // @param init A string expression of a 2-D int array.
    // @exception IllegalArgumentException If the string does
    //  not contain a parsable 2-D int array.
    //
    // public DoubleMatrixToken(String init) {
    // }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the content of this token as a 2-D Complex array.
     *  @return A 2-D Complex matrix
     */
    public Complex[][] complexMatrix() {
	Complex[][] array = new Complex[_numRows][_numColumns];
	for (int i = 0; i < _numRows; i++) {
	    for (int j = 0; j < _numColumns; j++) {
		array[i][j] = new Complex(_value[i][j]);
	    }
	}
	return array;
    }

    /** Return the content in the token as a 2-D double array.
     *  The returned array is a copy so the caller is free to
     *  modify it.
     *  @return A 2-D double array.
     */
    public double[][] doubleMatrix() {
	double[][] array = new double[_numRows][_numColumns];
	for (int i = 0; i < _numRows; i++) {
	    for (int j = 0; j < _numColumns; j++) {
	 	array[i][j] = _value[i][j];
	    }
	}
	return array;
    }

    // Return the content of this token as a 2-D Fix array.
    //
    // FIXME: finish this method after the Complex class is implemented.
    // public Fix[][] fixMatrix();

    /** Return the number of columns in the matrix.
     *  @return An integer.
     */
    public int numColumns() {
	return _numColumns;
    }

    /** Return the number of rows in the matrix.
     *  @return An intefer.
     */
    public int numRows() {
	return _numRows;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private double[][] _value = null;
    private int _numRows = 0;
    private int _numColumns = 0;
}

