/* A library for mathematical operations on matrices of complex numbers.

Some algorithms are from

[1] Embree, Paul M. and Bruce Kimble. "C Language Algorithms for Digital
    Signal Processing". Prentice Hall. Englewood Cliffs, NJ, 1991.

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
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.math;

//////////////////////////////////////////////////////////////////////////
//// ComplexMatrixMath

/**
 * This class provides a library for mathematical operations on
 * matrices of complex numbers.
 * <p>
 * Rows and column numbers of matrices are specified with zero-based indices.
 *
 * All calls expect matrix arguments to be non-null. In addition, all
 * rows of the matrix are expected to have the same number of columns.
 *
 * @author Jeff Tsay
 */

public class ComplexMatrixMath {

    // Private constructor prevents construction of this class.
    private ComplexMatrixMath() {}

    /** Return a new matrix that is constructed from the argument by
     *  adding the second argument to every element.
     *  @param matrix A matrix of complex numbers.
     *  @param z The Complex number to add.
     *  @return A new matrix of complex numbers.
     */
    public static final Complex[][] add(Complex[][] matrix, Complex z) {
        Complex[][] retval = new Complex[_rows(matrix)][_columns(matrix)];
        for (int i = 0; i < _rows(matrix); i++) {
            for (int j = 0; j < _columns(matrix); j++) {
                retval[i][j] = matrix[i][j].add(z);
            }
        }
        return retval;
    }

    /** Return a new matrix that is constructed from the argument by
     *  adding the second matrix to the first one. 
     *  If the two matrices are not the same size, throw an IllegalArgumentException.     
     *  @param matrix1 The first matrix of complex numbers.
     *  @param matrix2 The second matrix of complex numbers.
     *  @return A new matrix of complex numbers.
     */
    public static final Complex[][] add(final Complex[][] matrix1, 
            final Complex[][] matrix2) {
        _checkSameDimension("add", matrix1, matrix2);

        Complex[][] retval = new Complex[_rows(matrix1)][_columns(matrix1)];
        for (int i = 0; i < _rows(matrix1); i++) {
            for (int j = 0; j < _columns(matrix1); j++) {
                retval[i][j] = matrix1[i][j].add(matrix2[i][j]);
            }
        }
        return retval;
    }

    /** Return a new matrix that is a copy of the matrix argument.
     *  @param matrix A matrix of complex numbers.
     *  @return A new matrix of complex numbers.
     */
    public static final Complex[][] allocCopy(final Complex[][] matrix) {
        return crop(matrix, 0, 0, _rows(matrix), _columns(matrix));
    }

    /** Return a new array that is formed by applying an instance of a 
     *  ComplexBinaryOperation to each element in the input matrix,     
     *  using z as the left operand in all cases and the matrix elements
     *  as the right operands (op.operate(z, matrix[i][j])).
     */
    public static final Complex[][] applyBinaryOperation(
            ComplexBinaryOperation op, final Complex z, final Complex[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);
            
        Complex[][] retval = new Complex[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; i < columns; j++) {
                retval[i][j] = op.operate(z, matrix[i][j]);
            }
        }
        return retval;
    }

    /** Return a new array that is formed by applying an instance of a 
     *  ComplexBinaryOperation to each element in the input matrix,     
     *  using the matrix elements as the left operands and z as the right 
     *  operand in all cases (op.operate(matrix[i][j], z)).
     */
    public static final Complex[][] applyBinaryOperation(
            ComplexBinaryOperation op, final Complex[][] matrix, final Complex z) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);
            
        Complex[][] retval = new Complex[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; i < columns; j++) {
                retval[i][j] = op.operate(matrix[i][j], z);
            }
        }
        return retval;
    }
            
    /** Return a new array that is formed by applying an instance of a 
     *  ComplexBinaryOperation to the two matrices, element by element,
     *  using the elements of the first matrix as the left operands and the 
     *  elements of the second matrix as the right operands.
     *  (op.operate(matrix1[i][j], matrix2[i][j])).
     *  If the matrices are not the same size, throw an IllegalArgumentException.
     */
    public static final Complex[][] applyBinaryOperation(
            ComplexBinaryOperation op, final Complex[][] matrix1, final Complex[][] matrix2) {     
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);
        
        _checkSameDimension("applyBinaryOperation", matrix1, matrix2);      
            
        Complex[][] retval = new Complex[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; i < columns; j++) {
                retval[i][j] = op.operate(matrix1[i][j], matrix2[i][j]);
            }
        }     
        return retval;
    }

    /** Return a new array that is formed by applying an instance of a 
     *  ComplexUnaryOperation to each element in the input matrix 
     *  (op.operate(matrix[i][j])).
     */
    public static final Complex[][] applyUnaryOperation(
            final ComplexUnaryOperation op, final Complex[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);
            
        Complex[][] retval = new Complex[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; i < columns; j++) {
                retval[i][j] = op.operate(matrix[i][j]);
            }
        }        
        return retval;
    }

    /** Returns true iff the differences of all the real and imaginary parts of 
     *  corresponding elements of 2 matrices, that are of the same size, are all 
     *  within a constant range, [-R, R], where R is the allowed error. 
     *  The specified absolute
     *  difference must be non-negative.
     *  More concisely, abs(Re{M1[i, j]} - Re{M2[i, j]}) 
     *  and abs(Re{M1[i, j]} - Re{M2[i, j]}) must both be within [-R, R]
     *  for 0 <= i < m and 0 <= j <n where M1 and M2 are both m x n matrices.
     *  @param matrix1 A matrix of complex numbers.
     *  @param matrix2 A matrix of complex numbers.
     *  @param absoluteError A Complex indicating the absolute value of the
     *  allowed error.
     *  @return A boolean condition.
     */
    public static final boolean arePartsWithin(final Complex[][] matrix1, 
            final Complex[][] matrix2, double absoluteError) {
        if (absoluteError < 0.0) {
            throw new IllegalArgumentException(
                    "within(): absoluteError (" + absoluteError +
                    " must be non-negative.");
        }

        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("arePartsWithin", matrix1, matrix2);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {                
                if ((Math.abs(matrix1[i][j].real - matrix2[i][j].real) > absoluteError) ||
                    (Math.abs(matrix1[i][j].imag - matrix2[i][j].imag) > absoluteError)) {
                   return false;
                }
            }
        }
        return true;
    }



    /** Return a new matrix that is constructed by conjugating the elements in the 
     *  input matrix. 
     */
    public static final Complex[][] conjugate(final Complex[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        Complex[][] retval = new Complex[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                retval[i][j] = matrix[i][j].conjugate();
            }
        }
        return retval;
    }


    /** Return a new matrix that is constructed by transposing the input
     *  matrix, and conjugating the elements. If the input matrix is m x n, 
     *  the output matrix will be n x m.
     */
    public static final Complex[][] conjugateTranspose(final Complex[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        Complex[][] retval = new Complex[columns][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                retval[j][i] = matrix[i][j].conjugate();
            }
        }
        return retval;
    }

    /** Return a new matrix that is a sub-matrix of the input
     *  matrix argument. The row and column from which to start
     *  and the number of rows and columns to span are specified.
     *  @param matrix A matrix of complex numbers.
     *  @param rowStart An int specifying which row to start on.
     *  @param colStart An int specifying which column to start on.
     *  @param rowSpan An int specifying how many rows to copy.
     *  @param colSpan An int specifying how many columns to copy.
     */
    public static final Complex[][] crop(final Complex[][] matrix,
            final int rowStart, final int colStart,
            final int rowSpan, final int colSpan) {
        Complex[][] retval = new Complex[rowSpan][colSpan];
        for (int i = 0; i < rowSpan; i++) {
            System.arraycopy(matrix[rowStart + i], colStart,
                    retval[i], 0, colSpan);
        }
        return retval;
    }

    /** Return the determinate of a square matrix.
     *  If the matrix is not square, throw an IllegalArgumentException.
     *  This algorithm uses LU decomposition, and is taken from [1]
     *  THIS IS NOT TESTED!     
     */
    public static final Complex determinate(final Complex[][] matrix) {
        _checkSquare("determinate", matrix);

        Complex[][] a;
        Complex det = Complex.ONE;
        int n = _rows(matrix);

        a = allocCopy(matrix);

        for (int pivot = 0; pivot < n-1; pivot++) {
            // find the biggest absolute pivot
            double big = a[pivot][pivot].magnitudeSquared();
            int swapRow = 0; // initialize for no swap
            for (int row = pivot + 1; row < n; row++) {
                double magSquaredElement = a[row][pivot].magnitudeSquared();
                if (magSquaredElement > big) {
                   swapRow = row;
                   big = magSquaredElement;
                }
            }

            // unless swapRow is still zero we must swap two rows
            if (swapRow != 0) {
                Complex[] aPtr = a[pivot];
                a[pivot] = a[swapRow];
                a[swapRow] = aPtr;

                // change sign of determinate because of swap
                det = det.multiply(a[pivot][pivot].negate());
            } else {
                // calculate the determinate by the product of the pivots
                det = det.multiply(a[pivot][pivot]);
            }

            // if almost singular matrix, give up now

            // FIXME use epsilon instead of this ugly constant
            if (det.magnitudeSquared() <= 1.0E-9) {
                return Complex.ZERO;
            }

            Complex pivotInverse = a[pivot][pivot].reciprocal();
            for (int col = pivot + 1; col < n; col++) {
                a[pivot][col] = a[pivot][col].multiply(pivotInverse);
            }

            for (int row = pivot + 1; row < n; row++) {
                Complex temp = a[row][pivot];
                for (int col = pivot + 1; col < n; col++) {
                    a[row][col] = a[row][col].subtract(a[pivot][col].multiply(temp));
                }
            }
        }

        // last pivot, no reduction required
        det = det.multiply(a[n-1][n-1]);

        return det;
    }
    
    /** Return a new matrix that is constructed by placing the elements of the input 
     *  array on the diagonal of the square matrix, starting from the top left corner 
     *  down to the bottom right corner. All other elements are zero. The size of of the 
     *  matrix is n x n, where n is the length of the input array. 
     */
    public static final Complex[][] diag(final Complex[] array) {
        int n = array.length;
        
        Complex[][] retval = new Complex[n][n];

        _zeroMatrix(retval, n, n);
        
        for (int i = 0; i < n; i++) {
            retval[i][i] = array[i];
        }
        
        return retval;            
    } 
             
    /** Return a new matrix that is constructed by element by element
     *  division of the two matrix arguments. Each element of the
     *  first matrix is divided by the corresponding element of the
     *  second matrix. 
     *  If the two matrices are not the same size, throw an IllegalArgumentException.     
     */
    public static final Complex[][] divideElements(final Complex[][] matrix1,
            final Complex[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("divideElements", matrix1, matrix2);

        Complex[][] retval = new Complex[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                retval[i][j] = matrix1[i][j].divide(matrix2[i][j]);
            }
        }
        return retval;
    }

    /** Return a new array that is filled with the contents of the matrix.
     *  The complex numbers are stored row by row, i.e. using the notation
     *  (row, column), the entries of the array are in the following order
     *  for a (m, n) matrix :
     *  (0, 0), (0, 1), (0, 2), ... , (0, n-1), (1, 0), (1, 1), ..., (m-1)(n-1)
     *  @param A matrix of complex numbers.
     *  @return A new array of complex numbers.
     */
    public static final Complex[] fromMatrixToArray(final Complex[][] matrix) {
        return fromMatrixToArray(matrix, _rows(matrix), _columns(matrix));
    }

    /** Return a new array that is filled with the contents of the matrix.
     *  The maximum numbers of rows and columns to copy are specified so
     *  that entries lying outside of this range can be ignored. The
     *  maximum rows to copy cannot exceed the number of rows in the matrix,
     *  and the maximum columns to copy cannot exceed the number of columns
     *  in the matrix.
     *  The complex numbers are stored row by row, i.e. using the notation
     *  (row, column), the entries of the array are in the following order
     *  for a matrix, limited to m rows and n columns :
     *  (0, 0), (0, 1), (0, 2), ... , (0, n-1), (1, 0), (1, 1), ..., (m-1)(n-1)
     *  @param matrix A matrix of complex numbers.
     *  @return A new array of complex numbers.
     */
    public static final Complex[] fromMatrixToArray(final Complex[][] matrix,
            int maxRow, int maxCol) {
        Complex[] retval = new Complex[maxRow * maxCol];
        for (int i = 0; i < maxRow; i++) {
            System.arraycopy(matrix[i], 0, retval, i * maxCol, maxCol);
        }
        return retval;
    }
    
    /** Return an new identity matrix with the specified dimension. The
     *  matrix is square, so only one dimension specifier is needed.
     */
    public static final Complex[][] identity(final int dim) {
        Complex[][] retval = new Complex[dim][dim];
        
        _zeroMatrix(retval, dim, dim);        
        
        for (int i = 0; i < dim; i++) {
            retval[i][i] = Complex.ONE;
        }
        return retval;
    }

    /** Return a new matrix that is formed by taking the imaginary parts of the
     *  complex numbers in the argument matrix.
     *  @param array An matrix of Complex.
     *  @return A new matrix of doubles.
     */
    public static final double[][] imagParts(final Complex[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);
 
        double[][] retval = new double[rows][columns];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                retval[i][j] = matrix[i][j].imag;
            }
        }
        return retval;
    }    


    /** Return a new matrix that is constructed by inverting the input
     *  matrix. If the input matrix is singular, null is returned.
     *  This method is from [1]
     *  THIS IS NOT TESTED!
     */
    public static final Complex[][] inverse(final Complex[][] A) {
        _checkSquare("inverse", A);

        int n = _rows(A);

        Complex[][] Ai = allocCopy(A);

        // We depend on each of the elements being initialized to 0
        int[] pivotFlag = new int[n];
        int[] swapCol = new int[n];
        int[] swapRow = new int[n];

        int irow = 0, icol = 0;

        for (int i = 0; i < n; i++) { // n iterations of pivoting
            // find the biggest pivot element
            double big = 0.0;
            for (int row = 0; row < n; row++) {
                if (pivotFlag[row] == 0) {
                    for (int col = 0; col < n; col++) {
                        if (pivotFlag[col] == 0) {
                            double magSquaredElement = Ai[row][col].magnitudeSquared();
                            if (magSquaredElement >= big) {
                                big = magSquaredElement;
                                irow = row;
                                icol = col;
                            }
                        }
                    }
                }
            }
            pivotFlag[icol]++;

            // swap rows to make this diagonal the biggest absolute pivot
            if (irow != icol) {
                for (int col = 0; col < n; col++) {
                    Complex temp = Ai[irow][col];
                    Ai[irow][col] = Ai[icol][col];
                    Ai[icol][col] = temp;
                }
            }

            // store what we swapped
            swapRow[i] = irow;
            swapCol[i] = icol;

            // if the pivot is zero, the matrix is singular
            if (Ai[icol][icol].equals(Complex.ZERO)) {
               return null;
            }

            // divide the row by the pivot
            Complex pivotInverse = Ai[icol][icol].reciprocal();
            Ai[icol][icol] = Complex.ONE; // pivot = 1 to avoid round off
            for (int col = 0; col < n; col++) {
                Ai[icol][col] = Ai[icol][col].multiply(pivotInverse);
            }

            // fix the other rows by subtracting
            for (int row = 0; row < n; row++) {
                if (row != icol) {
                    Complex temp = Ai[row][icol];
                    Ai[row][icol] = Complex.ZERO;
                    for (int col = 0; col < n; col++) {
                        Ai[row][col] = Ai[row][col].subtract(Ai[icol][col].multiply(temp));
                    }
                }
            }
        }

        // fix the effect of all the swaps for final answer
        for (int swap = n - 1; swap >= 0; swap--) {
            if (swapRow[swap] != swapCol[swap]) {
                for (int row = 0; row < n; row++) {
                    Complex temp = Ai[row][swapRow[swap]];
                    Ai[row][swapRow[swap]] = Ai[row][swapCol[swap]];
                    Ai[row][swapCol[swap]] = temp;
                }
            }
        }

        return Ai;
    }
    
    /** Replace the first matrix argument elements with the values of
     *  the second matrix argument. The second matrix argument must be
     *  large enough to hold all the values of second matrix argument.
     *  @param destMatrix A matrix of complex numbers, used as the destination.
     *  @param srcMatrix A matrix of complex numbers, used as the source.
     */
    public static final void matrixCopy(final Complex[][] srcMatrix,
            final Complex[][] destMatrix) {
        matrixCopy(srcMatrix, 0, 0, destMatrix, 0, 0, _rows(srcMatrix),
                _columns(srcMatrix));
    }

    /** Replace the first matrix argument's values, in the specified row
     *  and column range, with the second matrix argument's values, starting
     *  from specified row and column of the second matrix.
     *  @param srcMatrix A matrix of complex numbers, used as the destination.
     *  @param srcRowStart An int specifying the starting row of the source.
     *  @param srcColStart An int specifying the starting column of the
     *  source.
     *  @param destMatrix A matrix of complex numbers, used as the destination.
     *  @param destRowStart An int specifying the starting row of the dest.
     *  @param destColStart An int specifying the starting column of the
     *         dest.
     *  @param rowSpan An int specifying how many rows to copy.
     *  @param colSpan An int specifying how many columns to copy.
     */
    public static final void matrixCopy(final Complex[][] srcMatrix,
            final int srcRowStart, final int srcColStart,
            final Complex[][] destMatrix,
            final int destRowStart, final int destColStart,
            final int rowSpan, final int colSpan) {
        // We should verify the parameters here
        for (int i = 0; i < rowSpan; i++) {
            System.arraycopy(srcMatrix[srcRowStart + i], srcColStart,
                    destMatrix[destRowStart + i], destColStart,
                    colSpan);
        }
    }
    
    /** Return a new matrix that is constructed by multiplying the matrix
     *  by a real scalefactor.
     */
    public static final Complex[][] multiply(final Complex[][] matrix,
            final double scalefactor) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        Complex[][] retval = new Complex[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                retval[i][j] = matrix[i][j].scale(scalefactor);
            }
        }
        return retval;
    }

    /** Return a new matrix that is constructed by multiplying the matrix
     *  by a complex scalefactor.
     */
    public static final Complex[][] multiply(final Complex[][] matrix,
            final Complex z) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        Complex[][] retval = new Complex[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                retval[i][j] = matrix[i][j].multiply(z);
            }
        }
        return retval;
    }



    /** Return a new array that is constructed from the argument by
     *  pre-multiplying the array (treated as a row vector) by a matrix.
     *  The number of rows of the matrix must equal the number of elements
     *  in the array. The returned array will have a length equal to the number
     *  of columns of the matrix.
     */
    public static final Complex[] multiply(final Complex[][] matrix,
            final Complex[] array) {

        int rows = _rows(matrix);
        int columns = _columns(matrix);

        if (rows != array.length) {
            throw new IllegalArgumentException(
                    "preMultiply : array does not have the same number of " +
                    "elements (" + array.length + ") as the number of rows " +
                    "of the matrix (" + rows + ")");
        }

        Complex[] retval = new Complex[columns];
        for (int i = 0; i < columns; i++) {
            Complex sum = Complex.ZERO;
            for (int j = 0; j < rows; j++) {
                sum = sum.add(matrix[j][i].multiply(array[j]));
            }
            retval[i] = sum;
        }
        return retval;
    }

    /** Return a new array that is constructed from the argument by
     *  post-multiplying the matrix by an array (treated as a row vector).
     *  The number of columns of the matrix must equal the number of elements
     *  in the array. The returned array will have a length equal to the number
     *  of rows of the matrix.
     */
    public static final Complex[] multiply(final Complex[] array,
            final Complex[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        if (columns != array.length) {
            throw new IllegalArgumentException(
                    "postMultiply() : array does not have the same number " +
                    "of elements (" + array.length + ") as the number of " +
                    "columns of the matrix (" + columns + ")");
        }

        Complex[] retval = new Complex[rows];
        for (int i = 0; i < rows; i++) {
            Complex sum = Complex.ZERO;
            for (int j = 0; j < columns; j++) {
                sum = sum.add(matrix[i][j].multiply(array[j]));
            }
            retval[i] = sum;
        }
        return retval;
    }

    /** Return a new matrix that is constructed from the argument by
     *  multiplying the first matrix by the second one.
     *  Note this operation is not commutative,
     *  so care must be taken in the ordering of the arguments.
     *  The number of columns of matrix1
     *  must equal the number of rows of matrix2. If matrix1 is of
     *  size m x n, and matrix2 is of size n x p, the returned matrix
     *  will have size m x p.
     *  @param matrix1 The first matrix of complex numbers.
     *  @param matrix2 The second matrix of complex numbers.
     *  @return A new matrix of complex numbers.
     */
    public static final Complex[][] multiply(Complex[][] matrix1,
            Complex[][] matrix2) {
        Complex[][] retval = new Complex[_rows(matrix1)][matrix2[0].length];
        for (int i = 0; i < _rows(matrix1); i++) {
            for (int j = 0; j < matrix2[0].length; j++) {
                Complex sum = Complex.ZERO;
                for (int k = 0; k < matrix2.length; k++) {
                    sum = sum.add(matrix1[i][k].multiply(matrix2[k][j]));
                }
                retval[i][j] = sum;
            }
        }
        return retval;
    }

    /** Return a new matrix that is constructed by element by element
     *  multiplication of the two matrix arguments. 
     *  If the two matrices are not the same size, throw an IllegalArgumentException.     
     */
    public static final Complex[][] multiplyElements(final Complex[][] matrix1,
            final Complex[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("multiplyElements", matrix1, matrix2);

        Complex[][] retval = new Complex[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                retval[i][j] = matrix1[i][j].multiply(matrix2[i][j]);
            }
        }
        return retval;
    }

    /** Return a new matrix that is the additive inverse of the
     *  argument matrix.
     */
    public static final Complex[][] negative(final Complex[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);
    
        Complex[][] retval = new Complex[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                retval[i][j] = matrix[i][j].negate();
            }
        }
        return retval;
    }
                 
    /** Return a new matrix that is constructed from the argument by
     *  subtracting the second matrix from the first one.  
     *  If the two matrices are not the same size, throw an IllegalArgumentException.          
     */
    public static final Complex[][] subtract(final Complex[][] matrix1,
            final Complex[][] matrix2) {
        _checkSameDimension("subtract", matrix1, matrix2);

        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        Complex[][] retval = new Complex[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                retval[i][j] = matrix1[i][j].subtract(matrix2[i][j]);
            }
        }
        return retval;
    }

    /** Return a new matrix that is formed by taking the real parts of the
     *  complex numbers in the argument matrix.
     *  @param array An matrix of Complex.
     *  @return A new matrix of doubles.
     */
    public static final double[][] realParts(final Complex[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);
 
        double[][] retval = new double[rows][columns];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                retval[i][j] = matrix[i][j].real;
            }
        }
        return retval;
    }    
           
    /** Return a new matrix of complex numbers that is initialized from a 1-D array.
     *  The format of the array must be (0, 0), (0, 1), ..., (0, n-1), (1, 0),
     *  (1, 1), ..., (m-1, n-1) where the output matrix is to be m x n and
     *  entries are denoted by (row, column).
     *  @param array An array of complex numbers.
     *  @param rows An integer representing the number of rows of the new matrix.
     *  @param cols An integer representing the number of columns of the new matrix.
     *  @return A new matrix of complex numbers.
     */
    public static final Complex[][] toMatrixFromArray(Complex[] array, int rows,
            int cols) {
        Complex[][] retval = new Complex[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(array, i * cols, retval[i], 0, cols);
        }
        return retval;
    }

    /** Return a new String representing the matrix, formatted as
     *  in Java array initializers.
     */
    public static final String toString(final Complex[][] matrix) {
        return toString(matrix, ArrayStringFormat.javaASFormat);
    }

    /** Return a new String representing the matrix, formatted as
     *  specified by the ArrayStringFormat argument.
     *  To get a String in the Ptolemy expression language format,
     *  call this method with ArrayStringFormat.exprASFormat as the
     *  format argument.
     */
    public static final String toString(final Complex[][] matrix,
            final ArrayStringFormat asf) {
        StringBuffer sb = new StringBuffer();
        sb.append(asf.matrixBeginString());

        for (int i = 0; i < _rows(matrix); i++) {

            sb.append(asf.vectorBeginString());
            for (int j = 0; j < _columns(matrix); j++) {
                sb.append(asf.complexString(matrix[i][j]));

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

        return sb.toString();
    }


    /** Return the trace of a square matrix, which is the sum of the
     *  diagonal entries A<sub>11</sub> + A<sub>22</sub> + ... + A<sub>nn</sub>
     *  Throw an IllegalArgumentException if the matrix is not square.
     *  Note that the trace of a matrix is equal to the sum of its eigenvalues.
     */
    public static final Complex trace(final Complex[][] matrix) {
        int dim = _checkSquare("trace", matrix);
        Complex sum = Complex.ZERO;

        for (int i = 0; i < dim; i++) {
            sum.add(matrix[i][i]);
        }
        return sum;
    }

    /** Return a new matrix that is constructed by transposing the input
     *  matrix. If the input matrix is m x n, the output matrix will be 
     *  n x m. Note that for complex matrices, the conjugate transpose
     *  is more commonly used.
     */
    public static final Complex[][] transpose(final Complex[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        Complex[][] retval = new Complex[columns][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                retval[j][i] = matrix[i][j];
            }
        }
        return retval;
    }

    /** Return a new complex matrix whose entries are all zero.
     *  The size of the matrix is specified by the input arguments.
     */
    public static final Complex[][] zero(int rows, int columns) {
        return _zeroMatrix(new Complex[rows][columns], rows, columns);
    }

    /** Return the number of columns of a matrix. */
    protected static final int _columns(final Complex[][] matrix) {
        return matrix[0].length;
    }

    /** Check that the two matrix arguments are of the same dimension.
     *  If they are not, an IllegalArgumentException is thrown.
     *  @param caller A string representing the caller method name.
     *  @param matrix1 A matrix of complex numbers.
     *  @param matrix2 A matrix of complex numbers.
     */
    protected static final void _checkSameDimension(final String caller,
             final Complex[][] matrix1, final Complex[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        if ((rows != _rows(matrix2)) || (columns != _columns(matrix2))) {
            throw new IllegalArgumentException(
                    "ptolemy.math.ComplexMatrixMath." + caller + "() : one matrix " +
                    _dimensionString(matrix1) +
                    " is not the same size as another matrix " +
                    _dimensionString(matrix2) + ".");
        }
    }

    /** Check that the argument matrix is a square matrix. If the matrix is not
     *  square, an IllegalArgumentException is thrown.
     *  @param caller A string representing the caller method name.
     *  @param matrix A matrix of complex numbers.
     *  @return The dimension of the square matrix.
     */
    protected static final int _checkSquare(final String caller, 
             final Complex[][] matrix) {
        if (_rows(matrix) != _columns(matrix)) {
            throw new IllegalArgumentException(
                    "ptolemy.math.ComplexMatrixMath." + caller +
                    "() : matrix argument " + _dimensionString(matrix) +
                    " is not a square matrix.");
        }
        return _rows(matrix);
    }

    protected static final String _dimensionString(final Complex[][] matrix) {
        return ("[" + _rows(matrix) + " x " + _columns(matrix) + "]");
    }
    
    /** Return the number of rows of a matrix. */
    protected static final int _rows(final Complex[][] matrix) {
        return matrix.length;        
    }    
    
    protected static final Complex[][] _zeroMatrix(Complex[][] matrix, int rows, int columns) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                matrix[i][j] = Complex.ZERO;
            }
        }        
        return matrix;
    }    
}
