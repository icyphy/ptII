/* A token that contains a reference to a 2-D double array.

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

package pt.data;

//////////////////////////////////////////////////////////////////////////
//// DoubleMatrixToken
/**
A token that contains a reference to a 2-D double array.

@author Yuhong Xiong
@version $Id$
*/
public class DoubleMatrixToken extends MatrixToken {

    /** Construct a token with a null 2-D array.
     */
    public DoubleMatrixToken() {
    }

    /** Construct a token with the specified 2-D array.
     */
    public DoubleMatrixToken(double[][] value) {
	_value = value;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Make a deep copy of the token.
     *  @return An identical token.
     *  @exception CloneNotSupportedException May be thrown by derived classes.
     */
    public Object clone()
            throws CloneNotSupportedException {
        DoubleMatrixToken copy =  (DoubleMatrixToken)super.clone();
	double[][] array = null;
	if (_value != null) {
	    int row = _value.length;
	    int col = _value[0].length;
	    array = new double[row][col];
	    for (int i = 0; i < row; i++) {
	    	for (int j = 0; j < col; j++) {
		    array[i][j] = _value[i][j];
	    	}
	    }
	}
        copy.setValue(array);
	return copy;
    }

    /** Set the value of the token to be the specified 2-D array.
     */
    public void setValue(double[][] value) {
	_value = value;
    }

    /** Return the number of rows in the matrix.  If the matrix is not
     *  initialized, return 0.
     */
    public int numRows() {
	if (_value == null) {
	    return 0;
	} else {
	    return _value.length;
	}
    }

    /** Return the number of columns in the matrix.  If the matrix is not
     *  initialized, return 0.
     */
    public int numColumns() {
	if (_value == null) {
	    return 0;
	} else {
	    return _value[0].length;
	}
    }

    /** Return the content in the token as a 2-D byte array.
     */
    public byte[][] byteMatrix() {
	byte[][] array = null;
	if (_value != null) {
	    int row = _value.length;
	    int col = _value[0].length;
	    array = new byte[row][col];
	    for (int i = 0; i < row; i++) {
		for (int j = 0; j < col; j++) {
		    array[i][j] = (byte)_value[i][j];
		}
	    }
	}
	return array;
    }

    /** Return the content in the token as a 2-D double array.
     */
    public double[][] doubleMatrix() {
	return _value;
    }

    /** Return the content in the token as a 2-D Complex array.
     */
    // FIXME: uncomment this method after the Complex class is available.
    // public Complex[][] complexMatrix();

    /** Return the content in the token as a 2-D Fix array.
     */
    // FIXME: uncomment this method after the Complex class is implemented.
    // public Fix[][] fixMatrix();

    /** Return the content in the token as a 2-D integer array.
     */
    public int[][] intMatrix() {
	int[][] array = null;
	if (_value != null) {
	    int row = _value.length;
	    int col = _value[0].length;
	    array = new int[row][col];
	    for (int i = 0; i < row; i++) {
		for (int j = 0; j < col; j++) {
		    array[i][j] = (int)_value[i][j];
		}
	    }
	}
	return array;
    }

    /** Return the content in the token as a 2-D long array.
     */
    public long[][] longMatrix() {
	long[][] array = null;
	if (_value != null) {
	    int row = _value.length;
	    int col = _value[0].length;
	    array = new long[row][col];
	    for (int i = 0; i < row; i++) {
		for (int j = 0; j < col; j++) {
		    array[i][j] = (long)_value[i][j];
		}
	    }
	}
	return array;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                        private variables                         ////
    private double[][] _value = null;
}

