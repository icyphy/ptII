/* A library for mathematical operations on matrices of ints.

Some algorithms are from

[1] Embree, Paul M. and Bruce Kimble. "C Language Algorithms for Digital
    Signal Processing". Prentice Hall. Englewood Cliffs, NJ, 1991.

This file was automatically generated with a preprocessor, so that 
similar matrix operations are supported on ints, longs, floats, and doubles. 

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
@AcceptedRating Yellow (ctsay@eecs.berkeley.edu)
*/

package ptolemy.math;

//////////////////////////////////////////////////////////////////////////
//// IntegerMatrixMath

/**
 * This class provides a library for mathematical operations on
 * matrices of ints.
 * <p>
 * Rows and column numbers of matrices are specified with zero-based indices.
 *
 * All calls expect matrix arguments to be non-null. In addition, all
 * rows of the matrix are expected to have the same number of columns.
 *
 * @author Jeff Tsay
 */

public class IntegerMatrixMath {

    // Private constructor prevents construction of this class.
    private IntegerMatrixMath() {}

    /** Return a new matrix that is constructed from the argument by
     *  adding the second argument to every element.
     *  @param matrix A matrix of ints.
     *  @param z The int number to add.
     *  @return A new matrix of ints.
     */
    public static final int[][] add(int[][] matrix, int z) {
        int[][] result = new int[_rows(matrix)][_columns(matrix)];
        for (int i = 0; i < _rows(matrix); i++) {
            for (int j = 0; j < _columns(matrix); j++) {
                result[i][j] = matrix[i][j] + z;
            }
        }
        return result;
    }

    /** Return a new matrix that is constructed from the argument by
     *  adding the second matrix to the first one. The matrices must be
     *  of the same size.
     *  @param matrix1 The first matrix of ints.
     *  @param matrix2 The second matrix of ints.
     *  @return A new matrix of ints.
     */
    public static final int[][] add(int[][] matrix1, int[][] matrix2) {
        _checkSameDimension("add", matrix1, matrix2);

        int[][] result = new int[_rows(matrix1)][_columns(matrix1)];
        for (int i = 0; i < _rows(matrix1); i++) {
            for (int j = 0; j < _columns(matrix1); j++) {
                result[i][j] = matrix1[i][j] + matrix2[i][j];
            }
        }
        return result;
    }

    /** Return a new matrix that is a copy of the matrix argument.
     *  @param matrix A matrix of ints.
     *  @return A new matrix of ints.
     */
    public static final int[][] allocCopy(int[][] matrix) {
        return crop(matrix, 0, 0, _rows(matrix), _columns(matrix)) ;
    }

    /** Return a new matrix that is a sub-matrix of the input
     *  matrix argument. The row and column from which to start
     *  and the number of rows and columns to span are specified.
     *  @param matrix A matrix of ints.
     *  @param rowStart An int specifying which row to start on.
     *  @param colStart An int specifying which column to start on.
     *  @param rowSpan An int specifying how many rows to copy.
     *  @param colSpan An int specifying how many columns to copy.
     */
    public static final int[][] crop(int[][] matrix,
            int rowStart, int colStart,
            int rowSpan, int colSpan) {
        int[][] retval = new int[rowSpan][colSpan];
        for (int i = 0; i < rowSpan; i++) {
            System.arraycopy(matrix[rowStart + i], colStart,
                    retval[i], 0, colSpan);
        }
        return retval;
    }


    /** Return a new matrix that is constructed by element by element
     *  division of the two matrix arguments. Each element of the
     *  first matrix is divided by the corresponding element of the
     *  second matrix.  The matrices must be of the same size.
     *  @param matrix1 A matrix of ints.
     *  @param matrix2 A matrix of ints.
     *  @return A new matrix of ints.
     */
    public static final int[][] divideElements(int[][] matrix1,
            int[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("divideElements", matrix1, matrix2);

        int[][] result = new int[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[i][j] = matrix1[i][j] / matrix2[i][j];
            }
        }
        return result;
    }

    /** Return a new array that is filled with the contents of the matrix.
     *  The ints are stored row by row, i.e. using the notation
     *  (row, column), the entries of the array are in the following order
     *  for a (m, n) matrix :
     *  (0, 0), (0, 1), (0, 2), ... , (0, n-1), (1, 0), (1, 1), ..., (m-1)(n-1)
     *  @param A matrix of ints.
     *  @return A new array of ints.
     */
    public static final int[] fromMatrixToArray(int[][] matrix) {
        return fromMatrixToArray(matrix, _rows(matrix), _columns(matrix));
    }

    /** Return a new array that is filled with the contents of the matrix.
     *  The maximum numbers of rows and columns to copy are specified so
     *  that entries lying outside of this range can be ignored. The
     *  maximum rows to copy cannot exceed the number of rows in the matrix,
     *  and the maximum columns to copy cannot exceed the number of columns
     *  in the matrix.
     *  The ints are stored row by row, i.e. using the notation
     *  (row, column), the entries of the array are in the following order
     *  for a matrix, limited to m rows and n columns :
     *  (0, 0), (0, 1), (0, 2), ... , (0, n-1), (1, 0), (1, 1), ..., (m-1)(n-1)
     *  @param matrix A matrix of ints.
     *  @return A new array of ints.
     */
    public static final int[] fromMatrixToArray(int[][] matrix,
            int maxRow, int maxCol) {
        int[] retval = new int[maxRow * maxCol];
        for (int i = 0; i < maxRow; i++) {
            System.arraycopy(matrix[i], 0, retval, i * maxCol, maxCol);
        }
        return retval;
    }


    /** Return an identity matrix with the specified dimension. The
     *  matrix is square, so only one dimension specifier is needed.
     *  @param dim An int
     *  @return A new identity matrix of ints
     */
    public static final int[][] identity(int dim) {
        int[][] retval = new int[dim][dim];
        // we rely on the fact Java fills the allocated matrix with 0's
        for (int i = 0; i < dim; i++) {
            retval[i][i] = 1;
        }
        return retval;
    }


    /** Replace the first matrix argument elements with the values of
     *  the second matrix argument. The second matrix argument must be
     *  large enough to hold all the values of second matrix argument.
     *  @param destMatrix A matrix of ints, used as the destination.
     *  @param srcMatrix A matrix of ints, used as the source.
     */
    public static final void matrixCopy(int[][] srcMatrix,
            int[][] destMatrix) {
        matrixCopy(srcMatrix, 0, 0, destMatrix, 0, 0, _rows(srcMatrix),
                _columns(srcMatrix));
    }

    /** Replace the first matrix argument's values, in the specified row
     *  and column range, with the second matrix argument's values, starting
     *  from specified row and column of the second matrix.
     *  @param srcMatrix A matrix of ints, used as the destination.
     *  @param srcRowStart An int specifying the starting row of the source.
     *  @param srcColStart An int specifying the starting column of the
     *  source.
     *  @param destMatrix A matrix of ints, used as the destination.
     *  @param destRowStart An int specifying the starting row of the dest.
     *  @param destColStart An int specifying the starting column of the
     *         dest.
     *  @param rowSpan An int specifying how many rows to copy.
     *  @param colSpan An int specifying how many columns to copy.
     */
    public static final void matrixCopy(int[][] srcMatrix,
            int srcRowStart, int srcColStart,
            int[][] destMatrix,
            int destRowStart, int destColStart,
            int rowSpan, int colSpan) {
        // We should verify the parameters here
        for (int i = 0; i < rowSpan; i++) {
            System.arraycopy(srcMatrix[srcRowStart + i], srcColStart,
                    destMatrix[destRowStart + i], destColStart,
                    colSpan);
        }
    }

    /** Return a new matrix that is constructed by computing the remainders between
     *  each element in the matrix and the second argument.
     *  @param matrix A matrix of ints.
     *  @param z The int number to .
     *  @return A new matrix of ints.
     */
    public static final int[][] moduloElements(int[][] matrix, int z) {
        int[][] result = new int[_rows(matrix)][_columns(matrix)];
        for (int i = 0; i < _rows(matrix); i++) {
            for (int j = 0; j < _columns(matrix); j++) {
                result[i][j] = matrix[i][j] % z;
            }
        }
        return result;
    }

    /** Return a new matrix that is constructed by computing the remainders between
     *  each element in the first matrix argument and the corresponding element in the 
     *  second matrix argument. The matrices must be of the same size.
     *  @param matrix1 The first matrix of ints.
     *  @param matrix2 The second matrix of ints.
     *  @return A new matrix of ints.
     */
    public static final int[][] modulo(int[][] matrix1, int[][] matrix2) {
        _checkSameDimension("moduloElements", matrix1, matrix2);
       
        int[][] result = new int[_rows(matrix1)][_columns(matrix1)];
        for (int i = 0; i < _rows(matrix1); i++) {
            for (int j = 0; j < _columns(matrix1); j++) {
                result[i][j] = matrix1[i][j] % matrix2[i][j];
            }
        }
        return result;
    }
    

    /** Return a new matrix that is constructed by multiplying the matrix
     *  by a scalefactor.
     *  @param matrix A matrix of ints.
     *  @scalefactor The constant by which to multiply the matrix.
     */
    public static final int[][] multiply(int[][] matrix,
            int scalefactor) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        int[][] result = new int[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[i][j] = matrix[i][j] * scalefactor;
            }
        }
        return result;
    }

    /** Return a new array that is constructed from the argument by
     *  pre-multiplying the array (treated as a row vector) by a matrix.
     *  The number of rows of the matrix must equal the number of elements
     *  in the array. The returned array will have a length equal to the number
     *  of columns of the matrix.
     *  @param matrix A matrix of ints.
     *  @param array An array of ints.
     *  @return A new array of ints.
     */
    public static final int[] multiply(int[][] matrix,
            int[] array) {

        int rows = _rows(matrix);
        int columns = _columns(matrix);

        if (rows != array.length) {
            throw new IllegalArgumentException(
                    "preMultiply : array does not have the same number of " +
                    "elements (" + array.length + ") as the number of rows " +
                    "of the matrix (" + rows + ")");
        }

        int[] result = new int[columns];
        for (int i = 0; i < columns; i++) {
            int sum = 0;
            for (int j = 0; j < rows; j++) {
                sum += matrix[j][i] * array[j];
            }
            result[i] = sum;
        }
        return result;
    }

    /** Return a new array that is constructed from the argument by
     *  post-multiplying the matrix by an array (treated as a row vector).
     *  The number of columns of the matrix must equal the number of elements
     *  in the array. The returned array will have a length equal to the number
     *  of rows of the matrix.
     *  @param array An array of ints.
     *  @param matrix A matrix of ints.
     *  @return A new array of ints.
     */
    public static final int[] multiply(int[] array,
            int[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        if (columns != array.length) {
            throw new IllegalArgumentException(
                    "postMultiply() : array does not have the same number " +
                    "of elements (" + array.length + ") as the number of " +
                    "columns of the matrix (" + columns + ")");
        }

        int[] result = new int[rows];
        for (int i = 0; i < rows; i++) {
            int sum = 0;
            for (int j = 0; j < columns; j++) {
                sum += matrix[i][j] * array[j];
            }
            result[i] = sum;
        }
        return result;
    }

    /** Return a new matrix that is constructed from the argument by
     *  multiplying the first matrix by the second one.
     *  Note this operation is not commutative,
     *  so care must be taken in the ordering of the arguments.
     *  The number of columns of matrix1
     *  must equal the number of rows of matrix2. If matrix1 is of
     *  size m x n, and matrix2 is of size n x p, the returned matrix
     *  will have size m x p.
     *  @param matrix1 The first matrix of ints.
     *  @param matrix2 The second matrix of ints.
     *  @return A new matrix of ints.
     */
    public static final int[][] multiply(int[][] matrix1,
            int[][] matrix2) {
        int[][] result = new int[_rows(matrix1)][matrix2[0].length];
        for (int i = 0; i < _rows(matrix1); i++) {
            for (int j = 0; j < matrix2[0].length; j++) {
                int sum = 0;
                for (int k = 0; k < matrix2.length; k++) {
                    sum += matrix1[i][k] * matrix2[k][j];
                }
                result[i][j] = sum;
            }
        }
        return result;
    }

    /** Return a new matrix that is constructed by element by element
     *  multiplication of the two matrix arguments. The matrices must be
     *  of the same size.
     *  @param matrix1 A matrix of ints.
     *  @param matrix2 A matrix of ints.
     *  @return A new matrix of ints.
     */
    public static final int[][] multiplyElements(int[][] matrix1,
            int[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("multiplyElements", matrix1, matrix2);

        int[][] result = new int[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[i][j] = matrix1[i][j] * matrix2[i][j];
            }
        }
        return result;
    }

    /** Return a new matrix that is the additive inverse of the
     *  argument matrix.
     *  @param matrix A matrix of ints.
     *  @return A new matrix of ints.
     */
    public static final int[][] negative(int[][] matrix) {
        int[][] result = new int[_rows(matrix)][_columns(matrix)];
        for (int i = 0; i < _rows(matrix); i++) {
            for (int j = 0; j < _columns(matrix); j++) {
                result[i][j] = -matrix[i][j];
            }
        }
        return result;
    }

    /** Return a new matrix that is constructed from the argument by
     *  arithmetically shifting the elements in the matrix by the second argument.
     *  If the second argument is positive, the elements are shifted left by
     *  the second argument. If the second argument is negative, the elements
     *  are shifted right (arithmetically, with the >>> operator) by the absolute 
     *  value of the second argument. If the second argument is 0, no operation is 
     *  performed (the matrix is just copied).
     *  @param matrix A first matrix of ints.
     *  @param shiftAmount The amount to shift by, positive for left shift, 
     *  negative for right shift.
     *  @return A new matrix of ints.
     */
    public static final int[][] shiftArithmetic(int[][] matrix, int shiftAmount) {
        int[][] result = new int[_rows(matrix)][_columns(matrix)];
        
        if (shiftAmount >= 0) {        
           for (int i = 0; i < _rows(matrix); i++) {
               for (int j = 0; j < _columns(matrix); j++) {
                   result[i][j] = matrix[i][j] << shiftAmount;
               }
           }
        } else if (shiftAmount < 0) {
           for (int i = 0; i < _rows(matrix); i++) {
               for (int j = 0; j < _columns(matrix); j++) {
                   result[i][j] = matrix[i][j] >>> -shiftAmount;
               }
           }
        }
        
        return result;
    }
       
    /** Return a new matrix that is constructed from the argument by
     *  logically shifting the elements in the matrix by the second argument.
     *  If the second argument is positive, the elements are shifted left by
     *  the second argument. If the second argument is negative, the elements
     *  are shifted right (logically, with the >> operator) by the absolute value 
     *  of the second  argument. If the second argument is 0, no operation is performed
     *  (the matrix is just copied).
     *  @param matrix A first matrix of ints.
     *  @param shiftAmount The amount to shift by, positive for left shift, 
     *  negative for right shift.
     *  @return A new matrix of ints.
     */
    public static final int[][] shiftLogical(int[][] matrix, int shiftAmount) {
        int[][] result = new int[_rows(matrix)][_columns(matrix)];
        
        if (shiftAmount >= 0) {        
           for (int i = 0; i < _rows(matrix); i++) {
               for (int j = 0; j < _columns(matrix); j++) {
                   result[i][j] = matrix[i][j] << shiftAmount;
               }
           }
        } else if (shiftAmount < 0) {
           for (int i = 0; i < _rows(matrix); i++) {
               for (int j = 0; j < _columns(matrix); j++) {
                   result[i][j] = matrix[i][j] >> -shiftAmount;
               }
           }
        }
        
        return result;
    }
   

    /** Return a new matrix that is constructed from the argument by
     *  subtracting the second matrix from the first one.  The matrices must be
     *  of the same size.
     *  @param matrix1 The first matrix of ints.
     *  @param matrix2 The second matrix of ints.
     *  @return A new matrix of ints.
     */
    public static final int[][] subtract(int[][] matrix1,
            int[][] matrix2) {
        _checkSameDimension("subtract", matrix1, matrix2);

        int[][] result = new int[_rows(matrix1)][_columns(matrix1)];
        for (int i = 0; i < _rows(matrix1); i++) {
            for (int j = 0; j < _columns(matrix1); j++) {
                result[i][j] = matrix1[i][j] - matrix2[i][j];
            }
        }
        return result;
    }

    /** Return a new matrix that is formed by converting the ints in
     *  the argument matrix to doubles.
     *  @param array An matrix of int.
     *  @return A new matrix of doubles.
     */
    public static final double[][] toDoubleMatrix(final int[][] matrix) {
        double[][] retval = new double[_rows(matrix)][_columns(matrix)];
        
        for (int i = 0; i < _rows(matrix); i++) {
            for (int j = 0; j < _columns(matrix); j++) {
                retval[i][j] = (double) matrix[i][j];
            }
        }
        return retval;
    }
    

    /** Return a new matrix that is formed by converting the ints in
     *  the argument matrix to floats.
     *  @param array An matrix of int.
     *  @return A new matrix of floats.
     */
    public static final float[][] toFloatMatrix(final int[][] matrix) {
        float[][] retval = new float[_rows(matrix)][_columns(matrix)];
        
        for (int i = 0; i < _rows(matrix); i++) {
            for (int j = 0; j < _columns(matrix); j++) {
                retval[i][j] = (float) matrix[i][j];
            }
        }
        return retval;
    }        
    


    /** Return a new matrix that is formed by converting the ints in
     *  the argument matrix to longs.
     *  @param array An matrix of int.
     *  @return A new matrix of longs.
     */
    public static final long[][] toLongMatrix(final int[][] matrix) {
        long[][] retval = new long[_rows(matrix)][_columns(matrix)];
        
        for (int i = 0; i < _rows(matrix); i++) {
            for (int j = 0; j < _columns(matrix); j++) {
                retval[i][j] = (long) matrix[i][j];
            }
        }
        return retval;
    }        
    

    /** Return a new matrix of ints that is initialized from a 1-D array.
     *  The format of the array must be (0, 0), (0, 1), ..., (0, n-1), (1, 0),
     *  (1, 1), ..., (m-1, n-1) where the output matrix is to be m x n and
     *  entries are denoted by (row, column).
     *  @param array An array of ints.
     *  @param rows An int.
     *  @param cols An int.
     *  @return A new matrix of ints.
     */
    public static final int[][] toMatrixFromArray(int[] array, int rows,
            int cols) {
        int[][] retval = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(array, i * cols, retval[i], 0, cols);
        }
        return retval;
    }

    /** Return a new String representing the matrix, formatted as
     *  in Java array initializers.
     */
    public static final String toString(int[][] matrix) {
        return toString(matrix, ArrayStringFormat.javaASFormat);
    }

    /** Return a new String representing the matrix, formatted as
     *  specified by the ArrayStringFormat argument.
     *  To get a String in the Ptolemy expression language format,
     *  call this method with ArrayStringFormat.exprASFormat as the
     *  format argument.
     */
    public static final String toString(int[][] matrix,
            ArrayStringFormat asf) {
        StringBuffer sb = new StringBuffer();
        sb.append(asf.matrixBeginString());

        for (int i = 0; i < _rows(matrix); i++) {

            sb.append(asf.vectorBeginString());
            for (int j = 0; j < _columns(matrix); j++) {
                sb.append(asf.intString(matrix[i][j]));

                if (j < (_columns(matrix) - 1)) {
                    sb.append(asf.elementDelimiterString());
                }
            }

            sb.append(asf.vectorEndString());

            if (i < (_rows(matrix) - 1)) {
                sb.append(asf.vectorDelimiterString());
            }
        }

        sb.append(asf.matrixEndString());

        return new String(sb);
    }


    /** Return the trace of a square matrix, which is the sum of the
     *  diagonal entries A<sub>11</sub> + A<sub>22</sub> + ... + A<sub>nn</sub>
     *  Throw an IllegalArgumentException if the matrix is not square.
     *  Note that the trace of a matrix is equal to the sum of its eigenvalues.
     *  @param matrix A matrix of ints.
     *  @return The trace of the matrix.
     */
    public static final int trace(int[][] matrix) {
        int dim = _checkSquare("trace", matrix);
        int sum = 0;

        for (int i = 0; i < dim; i++) {
            sum += matrix[i][i];
        }
        return sum;
    }

    /** Return a new matrix that is constructed by transposing the input
     *  matrix.
     *  @param matrix A matrix of ints.
     *  @return A new matrix of ints.
     */
    public static final int[][] transpose(int[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        int[][] result = new int[columns][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[j][i] = matrix[i][j];
            }
        }
        return result;
    }

    /** Returns true iff the differences of all corresponding elements of
     *  2 matrices, that are of the same size, are all within a constant range,
     *  [-R, R], where R is the allowed error. The specified absolute
     *  difference must be non-negative.
     *  More concisely, abs(M1[i, j] - M2[i, j]) must be within [R, R]
     *  for 0 <= i < m and 0 <= j <n where M1 and M2 are both m x n matrices.
     *  @param matrix1 A matrix of ints.
     *  @param matrix2 A matrix of ints.
     *  @param absoluteError A int indicating the absolute value of the
     *  allowed error.
     *  @return A boolean condition.
     */
    public static final boolean within(int[][] matrix1, int[][] matrix2,
            int absoluteError) {
        if (absoluteError < 0) {
            throw new IllegalArgumentException(
                    "within(): absoluteError (" + absoluteError +
                    " must be non-negative.");
        }

        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("within", matrix1, matrix2);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (Math.abs(matrix1[i][j] - matrix2[i][j]) > absoluteError) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Returns true iff the differences of all corresponding elements of
     *  2 matrices, that are of the same size, are all within the range
     *  specificed by the corresponding values of the error matrix. The
     *  error matrix may contain negative entries; the absolute value
     *  is used.
     *  More concisely, abs(M1[i, j] - M2[i, j]) must be
     *  within [-E[i,j], E[i,j]], for 0 <= i < m and 0 <= j < n
     *  where M1, M2, and E are all m x n matrices.
     *  @param matrix1 A matrix of ints.
     *  @param matrix2 A matrix of ints.
     *  @param errorMatrix A matrix of ints.
     *  @return A boolean condition.
     */
    public static final boolean within(int[][] matrix1, int[][] matrix2,
            int[][] errorMatrix) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("within", matrix1, matrix2);
        _checkSameDimension("within", matrix1, errorMatrix);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (Math.abs(matrix1[i][j] - matrix2[i][j]) >
                        Math.abs(errorMatrix[i][j])) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Return the number of columns of a matrix.
     *  @param matrix A matrix of ints.
     *  @return An int.
     */
    private static final int _columns(int[][] matrix) {
        return matrix[0].length;
    }

    /** Check that the two matrix arguments are of the same dimension.
     *  If they are not, an IllegalArgumentException is thrown.
     *  @param caller A string representing the caller method name.
     *  @param matrix1 A matrix of ints.
     *  @param matrix2 A matrix of ints.
     */
    private static final void _checkSameDimension(String caller,
            int[][] matrix1,
            int[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        if ((rows != _rows(matrix2)) || (columns != _columns(matrix2))) {
            throw new IllegalArgumentException(
                    "ptolemy.math.intMatrixMath." + caller + "() : one matrix " +
                    _dimensionString(matrix1) +
                    " is not the same size as another matrix " +
                    _dimensionString(matrix2) + ".");
        }
    }

    /** Check that the argument matrix is a square matrix. If the matrix is not
     *  square, an IllegalArgumentException is thrown.
     *  @param caller A string representing the caller method name.
     *  @param matrix A matrix of ints.
     *  @return The dimension of the square matrix.
     */
    private static final int _checkSquare(String caller, int[][] matrix) {
        if (_rows(matrix) != _columns(matrix)) {
            throw new IllegalArgumentException(
                    "ptolemy.math.intMatrixMath." + caller +
                    "() : matrix argument " + _dimensionString(matrix) +
                    " is not a square matrix.");
        }
        return _rows(matrix);
    }

    private static final String _dimensionString(int[][] matrix) {
        return ("[" + _rows(matrix) + " x " + _columns(matrix) + "]");
    }

    /** Return the number of rows of a matrix.
     *  @param matrix A matrix of ints.
     *  @return An int.
     */
    private static final int _rows(int[][] matrix) {
        return matrix.length;
    }
}
