/* A library for mathematical operations on matrices of complex numbers.

Some algorithms are from

 [1] Embree, Paul M. and Bruce Kimble. "C Language Algorithms for Digital
    Signal Processing". Prentice Hall. Englewood Cliffs, NJ, 1991.

Copyright (c) 1998-2003 The Regents of the University of California.
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
This class provides a library for mathematical operations on
matrices of complex numbers.
<p>
Rows and column numbers of matrices are specified with zero-based indices.
All calls expect matrix arguments to be non-null. In addition, all
rows of the matrix are expected to have the same number of columns.
@author Jeff Tsay
@version $Id$
@since Ptolemy II 1.0
*/
public class ComplexMatrixMath {

    // Private constructor prevents construction of this class.
    private ComplexMatrixMath() {}

    /** Return a new matrix that is constructed from the argument by
     *  adding the second argument to every element.
     *
     *  @param matrix A matrix of complex numbers.
     *  @param z The complex number to add.
     *  @return A new matrix of complex numbers formed by adding <i>z</i>
     *  to every element of <i>matrix</i>.
     */
    public static final Complex[][] add(Complex[][] matrix, Complex z) {
        Complex[][] returnValue = new Complex[_rows(matrix)][_columns(matrix)];
        for (int i = 0; i < _rows(matrix); i++) {
            for (int j = 0; j < _columns(matrix); j++) {
                returnValue[i][j] = matrix[i][j].add(z);
            }
        }
        return returnValue;
    }

    /** Return a new matrix that is constructed from the argument by
     *  adding the second matrix to the first one.
     *
     *  @param matrix1 The first matrix of complex numbers.
     *  @param matrix2 The second matrix of complex numbers.
     *  @return A new matrix of complex numbers formed by adding <i>matrix2</i>
     *  to <i>matrix1</i>.
     *  @exception IllegalArgumentException If the matrices do not have the same
     *   dimensions.
     */
    public static final Complex[][] add(final Complex[][] matrix1,
            final Complex[][] matrix2) {
        _checkSameDimension("add", matrix1, matrix2);

        Complex[][] returnValue =
            new Complex[_rows(matrix1)][_columns(matrix1)];
        for (int i = 0; i < _rows(matrix1); i++) {
            for (int j = 0; j < _columns(matrix1); j++) {
                returnValue[i][j] = matrix1[i][j].add(matrix2[i][j]);
            }
        }
        return returnValue;
    }

    /** Return a new matrix that is a copy of the matrix argument.
     *
     *  @param matrix A matrix of complex numbers.
     *  @return A new matrix of complex numbers that is a copy of <i>matrix</i>.
     */
    public static final Complex[][] allocCopy(final Complex[][] matrix) {
        return crop(matrix, 0, 0, _rows(matrix), _columns(matrix));
    }

    /** Return a new matrix that is formed by applying an instance of
     *  a ComplexBinaryOperation to each element in the input matrix,
     *  using <i>z</i> as the left argument in all cases and the
     *  matrix elements as the right arguments (z,
     *  op.operate(matrix[i][j])).
     *
     *  @param op A complex binary operation.
     *  @param z A complex number.
     *  @param matrix A matrix of complex numbers.
     *  @return A new matrix formed by applying (z, op.operate(matrix[i][j]))
     *  to each element of <i>matrix</i>.
     */
    public static final Complex[][] applyBinaryOperation(
            ComplexBinaryOperation op, final Complex z,
            final Complex[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        Complex[][] returnValue = new Complex[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = op.operate(z, matrix[i][j]);
            }
        }
        return returnValue;
    }

    /** Return a new matrix that is formed by applying an instance of
     *  a ComplexBinaryOperation to each element in the input matrix,
     *  using <i>z</i> as the right argument in all cases and the
     *  matrix elements as the left arguments
     *  (op.operate(matrix[i][j], z)).
     *
     *  @param op A complex binary operation.
     *  @param z A complex number.
     *  @param matrix A matrix of complex numbers.
     *  @return A new matrix formed by applying (op.operate(matrix[i][j], z))
     *  to each element of <i>matrix</i>.
     */
    public static final Complex[][] applyBinaryOperation(
            ComplexBinaryOperation op, final Complex[][] matrix,
            final Complex z) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        Complex[][] returnValue = new Complex[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = op.operate(matrix[i][j], z);
            }
        }
        return returnValue;
    }

    /** Return a new matrix that is formed by applying an instance of a
     *  ComplexBinaryOperation to the two matrices, element by element,
     *  using the elements of the first matrix as the left operands and the
     *  elements of the second matrix as the right operands.
     *  (op.operate(matrix1[i][j], matrix2[i][j])).
     *
     *  @param matrix1 The first matrix of complex numbers.
     *  @param matrix2 The second matrix of complex numbers.
     *  @return A new matrix of complex numbers with each element
     *  equal to (op.operate(matrix1[i][j], matrix2[i][j])).
     *  @exception IllegalArgumentException If the matrices do not have the same
     *   dimensions.
     */
    public static final Complex[][] applyBinaryOperation(
            ComplexBinaryOperation op, final Complex[][] matrix1,
            final Complex[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("applyBinaryOperation", matrix1, matrix2);

        Complex[][] returnValue = new Complex[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = op.operate(matrix1[i][j], matrix2[i][j]);
            }
        }
        return returnValue;
    }

    /** Return a new matrix that is formed by applying an instance of a
     *  ComplexUnaryOperation to each element in the input matrix
     *  (op.operate(matrix[i][j])).
     *
     *  @param matrix The matrix of complex numbers.
     *  @return A new matrix of complex numbers with each element
     *  equal to (op.operate(matrix1[i][j])).
     */
    public static final Complex[][] applyUnaryOperation(
            final ComplexUnaryOperation op, final Complex[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        Complex[][] returnValue = new Complex[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = op.operate(matrix[i][j]);
            }
        }
        return returnValue;
    }

    /** Return a new matrix that is constructed by conjugating the elements
     *  of the input matrix.
     *
     *  @param matrix The matrix of complex numbers.
     *  @return A new matrix of complex numbers formed
     *  by conjugating the elements of <i>matrix</i>.
     */
    public static final Complex[][] conjugate(final Complex[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        Complex[][] returnValue = new Complex[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix[i][j].conjugate();
            }
        }
        return returnValue;
    }


    /** Return a new matrix that is constructed by transposing the input
     *  matrix and conjugating the elements. If the input matrix is m x n,
     *  the output matrix will be n x m.
     *
     *  @param matrix The matrix of complex numbers.
     *  @return A new matrix of complex numbers formed by transposing
     *  the input matrix and conjugating the elements.
     */
    public static final Complex[][] conjugateTranspose(
            final Complex[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        Complex[][] returnValue = new Complex[columns][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[j][i] = matrix[i][j].conjugate();
            }
        }
        return returnValue;
    }

    /** Return a new matrix that is a sub-matrix of the input
     *  matrix argument. The row and column from which to start
     *  and the number of rows and columns to span are specified.
     *
     *  @param matrix A matrix of complex numbers.
     *  @param rowStart An int specifying which row to start on.
     *  @param colStart An int specifying which column to start on.
     *  @param rowSpan An int specifying how many rows to copy.
     *  @param colSpan An int specifying how many columns to copy.
     *  @return A new matrix that is a sub-matrix of <i>matrix</i>.
     */
    public static final Complex[][] crop(final Complex[][] matrix,
            final int rowStart, final int colStart,
            final int rowSpan, final int colSpan) {
        Complex[][] returnValue = new Complex[rowSpan][colSpan];
        for (int i = 0; i < rowSpan; i++) {
            System.arraycopy(matrix[rowStart + i], colStart,
                    returnValue[i], 0, colSpan);
        }
        return returnValue;
    }

    /** Return the determinant of a square matrix.
     *  If the matrix is not square, throw an IllegalArgumentException.
     *  This algorithm uses LU decomposition, and is taken from [1].
     *
     *  @param matrix The matrix for which to calculate the determinant.
     *  @return The determinant of the matrix.
     */
    public static final Complex determinant(final Complex[][] matrix) {
        _checkSquare("determinant", matrix);

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

                // Change sign of determinant because of swap.
                det = det.multiply(a[pivot][pivot].negate());
            } else {
                // Calculate the determinant by the product of the pivots.
                det = det.multiply(a[pivot][pivot]);
            }

            // If almost singular matrix, give up now.

            if (det.magnitudeSquared() <= 1.0e-9) {
                return Complex.ZERO;
            }

            Complex pivotInverse = a[pivot][pivot].reciprocal();
            for (int col = pivot + 1; col < n; col++) {
                a[pivot][col] = a[pivot][col].multiply(pivotInverse);
            }

            for (int row = pivot + 1; row < n; row++) {
                Complex temp = a[row][pivot];
                for (int col = pivot + 1; col < n; col++) {
                    a[row][col] =
                        a[row][col].subtract(a[pivot][col].multiply(temp));
                }
            }
        }

        // Last pivot, no reduction required.
        det = det.multiply(a[n-1][n-1]);

        return det;
    }

    /** Return a new matrix that is constructed by placing the
     *  elements of the input array on the diagonal of the square
     *  matrix, starting from the top left corner down to the bottom
     *  right corner. All other elements are zero. The size of of the
     *  matrix is n x n, where n is the length of the input array.
     *
     *  @param array The input array of complex numbers.
     *  @return A new matrix containing <i>array</i> as its diagonal.
     */
    public static final Complex[][] diag(final Complex[] array) {
        int n = array.length;

        Complex[][] returnValue = new Complex[n][n];

        _zeroMatrix(returnValue, n, n);

        for (int i = 0; i < n; i++) {
            returnValue[i][i] = array[i];
        }

        return returnValue;
    }

    /** Return a new matrix that is constructed from the argument by
     *  dividing the second argument to every element.
     *  @param matrix A matrix of complex numbers.
     *  @param z The complex number to divide.
     *  @return A new matrix of complex numbers.
     */
    public static final Complex[][] divide(Complex[][] matrix, Complex z) {
        Complex[][] returnValue = new Complex[_rows(matrix)][_columns(matrix)];
        for (int i = 0; i < _rows(matrix); i++) {
            for (int j = 0; j < _columns(matrix); j++) {
                returnValue[i][j] = matrix[i][j].divide(z);
            }
        }
        return returnValue;
    }


    /** Return a new matrix that is constructed by element-by-element
     *  division of the two matrix arguments. Each element of the
     *  first matrix is divided by the corresponding element of the
     *  second matrix.
     *
     *  @param matrix1 The first matrix of complex numbers.
     *  @param matrix2 The second matrix of complex numbers.
     *  @return A new matrix of complex numbers constructed by
     *  element-by-element division of the two matrix arguments.
     *  @exception IllegalArgumentException If the matrices do not
     *  have the same dimensions.
     */
    public static final Complex[][] divideElements(final Complex[][] matrix1,
            final Complex[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("divideElements", matrix1, matrix2);

        Complex[][] returnValue = new Complex[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix1[i][j].divide(matrix2[i][j]);
            }
        }
        return returnValue;
    }

    /** Return a new array that is filled with the contents of the matrix.
     *  The complex numbers are stored row by row, i.e. using the notation
     *  (row, column), the entries of the array are in the following order
     *  for a (m, n) matrix :
     *  (0, 0), (0, 1), (0, 2), ... , (0, n-1), (1, 0), (1, 1), ..., (m-1)(n-1)
     *
     *  @param matrix A matrix of complex numbers.
     *  @return A new array of complex numbers filled with
     *  the contents of the matrix.
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
     *
     *  @param matrix A matrix of complex numbers.
     *  @return A new array of complex numbers filled with
     *  the contents of the matrix.
     */
    public static final Complex[] fromMatrixToArray(final Complex[][] matrix,
            int maxRow, int maxCol) {
        Complex[] returnValue = new Complex[maxRow * maxCol];
        for (int i = 0; i < maxRow; i++) {
            System.arraycopy(matrix[i], 0, returnValue, i * maxCol, maxCol);
        }
        return returnValue;
    }

    /** Return an new identity matrix with the specified dimension. The
     *  matrix is square, so only one dimension specifier is needed.
     *
     *  @param dim An integer representing the dimension of the
     *  identity matrix to be returned.
     *  @return A new identity matrix of complex numbers with the
     *  specified dimension.
     */
    public static final Complex[][] identity(final int dim) {
        Complex[][] returnValue = new Complex[dim][dim];

        _zeroMatrix(returnValue, dim, dim);

        for (int i = 0; i < dim; i++) {
            returnValue[i][i] = Complex.ONE;
        }
        return returnValue;
    }

    /** Return an new identity matrix with the specified dimension. The
     *  matrix is square, so only one dimension specifier is needed.
     */
    public static final Complex[][] identityMatrixComplex(final int dim) {
        return identity(dim);
    }

    /** Return a new matrix that is formed by taking the imaginary parts of the
     *  complex numbers in the argument matrix.
     *
     *  @param matrix A matrix of complex numbers.
     *  @return A new matrix of doubles from the imaginary parts
     *  of <i>matrix</i>.
     */
    public static final double[][] imagParts(final Complex[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        double[][] returnValue = new double[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix[i][j].imag;
            }
        }
        return returnValue;
    }

    /** Return a new matrix that is constructed by inverting the input
     *  matrix. If the input matrix is singular, null is returned.
     *  This method is from [1]
     *
     *  @param A A matrix of complex numbers.
     *  @return the inverse of <i>matrix</i>.
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
                            double magSquaredElement =
                                Ai[row][col].magnitudeSquared();
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
                        Ai[row][col] =
                            Ai[row][col].subtract(Ai[icol][col].multiply(temp));
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
     *  the second matrix argument. The first matrix argument must be
     *  large enough to hold all the values of second matrix argument.
     *
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
     *
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

        for (int i = 0; i < rowSpan; i++) {
            System.arraycopy(srcMatrix[srcRowStart + i], srcColStart,
                    destMatrix[destRowStart + i], destColStart,
                    colSpan);
        }
    }

    /** Return a new matrix that is constructed by multiplying the matrix
     *  by a real scaleFactor.
     *
     *  @param matrix A matrix of complex numbers.
     *  @param scaleFactor A double used to multiply each element
     *  of the matrix by.
     *  @return A new matrix that is formed by multiplying the matrix by
     *  <i>scaleFactor</i>.
     */
    public static final Complex[][] multiply(final Complex[][] matrix,
            final double scaleFactor) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        Complex[][] returnValue = new Complex[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix[i][j].scale(scaleFactor);
            }
        }
        return returnValue;
    }

    /** Return a new matrix that is constructed by multiplying the matrix
     *  by a complex scaleFactor.
     *
     *  @param matrix A matrix of complex numbers.
     *  @param z A complex number used to multiply each element
     *  of the matrix by.
     *  @return A new matrix that is formed by multiplying the matrix by
     *  <i>z</i>.
     */
    public static final Complex[][] multiply(final Complex[][] matrix,
            final Complex z) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        Complex[][] returnValue = new Complex[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix[i][j].multiply(z);
            }
        }
        return returnValue;
    }



    /** Return a new array that is constructed from the argument by
     *  pre-multiplying the array (treated as a row vector) by a matrix.
     *  The number of rows of the matrix must equal the number of elements
     *  in the array. The returned array will have a length equal to the number
     *  of columns of the matrix.
     *
     *  @param matrix A matrix of complex numbers.
     *  @param array An array of complex numbers.
     *  @return A new matrix that is formed by multiplying <i>array</i> by
     *  <i>matrix</i>.
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

        Complex[] returnValue = new Complex[columns];
        for (int i = 0; i < columns; i++) {
            Complex sum = Complex.ZERO;
            for (int j = 0; j < rows; j++) {
                sum = sum.add(matrix[j][i].multiply(array[j]));
            }
            returnValue[i] = sum;
        }
        return returnValue;
    }

    /** Return a new array that is constructed from the argument by
     *  post-multiplying the matrix by an array (treated as a row vector).
     *  The number of columns of the matrix must equal the number of elements
     *  in the array. The returned array will have a length equal to the number
     *  of rows of the matrix.
     *
     *  @param array An array of complex numbers.
     *  @param matrix A matrix of complex numbers.
     *  @return A new matrix that is formed by multiplying <i>matrix</i> by
     *  <i>array</i>.
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

        Complex[] returnValue = new Complex[rows];
        for (int i = 0; i < rows; i++) {
            Complex sum = Complex.ZERO;
            for (int j = 0; j < columns; j++) {
                sum = sum.add(matrix[i][j].multiply(array[j]));
            }
            returnValue[i] = sum;
        }
        return returnValue;
    }

    /** Return a new matrix that is constructed from the argument by
     *  multiplying the first matrix by the second one.
     *  Note this operation is not commutative,
     *  so care must be taken in the ordering of the arguments.
     *  The number of columns of matrix1
     *  must equal the number of rows of matrix2. If matrix1 is of
     *  size m x n, and matrix2 is of size n x p, the returned matrix
     *  will have size m x p.
     *
     *  <p>Note that this method is different from the other multiply()
     *  methods in that this method does not do pointwise multiplication.
     *
     *  @see #multiplyElements(Complex[][], Complex[][])
     *  @param matrix1 The first matrix of complex numbers.
     *  @param matrix2 The second matrix of complex numbers.
     *  @return A new matrix of complex numbers equal to <i>matrix1</i>
     *  times <i>matrix2</i>.
     */
    public static final Complex[][] multiply(Complex[][] matrix1,
            Complex[][] matrix2) {

        Complex[][] returnValue =
            new Complex[_rows(matrix1)][matrix2[0].length];
        for (int i = 0; i < _rows(matrix1); i++) {
            for (int j = 0; j < matrix2[0].length; j++) {
                Complex sum = Complex.ZERO;
                for (int k = 0; k < matrix2.length; k++) {
                    sum = sum.add(matrix1[i][k].multiply(matrix2[k][j]));
                }
                returnValue[i][j] = sum;
            }
        }
        return returnValue;
    }

    /** Return a new matrix that is constructed by element by element
     *  multiplication of the two matrix arguments.
     *  If the two matrices are not the same size, throw an
     *  IllegalArgumentException.
     *
     *  <p>Note that this method does pointwise matrix multiplication.
     *  See {@link #multiply(Complex[][], Complex[][])} for standard
     *  matrix multiplication.
     *
     *  @param matrix1 The first matrix of complex numbers.
     *  @param matrix2 The second matrix of complex numbers.
     *  @return A new matrix constructed by element by element
     *  multiplication of the two matrix arguments.
     */
    public static final Complex[][] multiplyElements(final Complex[][] matrix1,
            final Complex[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("multiplyElements", matrix1, matrix2);

        Complex[][] returnValue = new Complex[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix1[i][j].multiply(matrix2[i][j]);
            }
        }
        return returnValue;
    }

    /** Return a new matrix that is the additive inverse of the
     *  argument matrix.
     * @param matrix A matrix of complex numbers.
     * @return A new matrix of complex numbers, which is the additive
     * inverse of the given matrix.
     */
    public static final Complex[][] negative(final Complex[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        Complex[][] returnValue = new Complex[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix[i][j].negate();
            }
        }
        return returnValue;
    }

    /** Return a new matrix that is formed by orthogonalizing the
     *  columns of the input matrix (the column vectors are
     *  orthogonal). If not all columns are linearly independent, the
     *  output matrix will contain a column of zeros for all redundant
     *  input columns.
     *
     *  @param matrix A matrix of complex numbers.
     *  @return A new matrix formed by orthogonalizing the
     *  columns of the input matrix.
     */
    public static final Complex[][] orthogonalizeColumns(Complex[][] matrix) {
        Object[] orthoInfo = _orthogonalizeRows(transpose(matrix));
        return transpose((Complex[][]) orthoInfo[0]);
    }

    /** Return a new matrix that is formed by orthogonalizing the rows of the
     *  input matrix (the row vectors are orthogonal). If not all rows are
     *  linearly independent, the output matrix will contain a row of zeros
     *  for all redundant input rows.
     *
     *  @param matrix A matrix of complex numbers.
     *  @return A new matrix formed by orthogonalizing the
     *  rows of the input matrix.
     */
    public static final Complex[][] orthogonalizeRows(Complex[][] matrix) {
        Object[] orthoInfo = _orthogonalizeRows(matrix);
        return (Complex[][]) orthoInfo[0];
    }

    /** Return a new matrix that is formed by orthonormalizing the
     *  columns of the input matrix (the column vectors are orthogonal
     *  and have norm 1). If not all columns are linearly independent,
     *  the output matrix will contain a column of zeros for all
     *  redundant input columns.
     *
     *  @param matrix A matrix of complex numbers.
     *  @return A new matrix formed by orthonormalizing the
     *  columns of the input matrix.
     */
    public static final Complex[][] orthonormalizeColumns(Complex[][] matrix) {
        return transpose(orthogonalizeRows(transpose(matrix)));
    }

    /** Return a new matrix that is formed by orthonormalizing the
     *  rows of the input matrix (the row vectors are orthogonal and
     *  have norm 1). If not all rows are linearly independent, the
     *  output matrix will contain a row of zeros for all redundant
     *  input rows.
     *
     *  @param matrix A matrix of complex numbers.
     *  @return A new matrix formed by orthonormalizing the
     *  rows of the input matrix.
     */
    public static final Complex[][] orthonormalizeRows(
            final Complex[][] matrix) {
        int rows = _rows(matrix);

        Object[] orthoInfo = _orthogonalizeRows(matrix);
        Complex[][] orthogonalMatrix = (Complex[][]) orthoInfo[0];
        Complex[] oneOverNormSquaredArray = (Complex[]) orthoInfo[2];

        for (int i = 0; i < rows; i++) {
            orthogonalMatrix[i] = ComplexArrayMath.scale(
                    orthogonalMatrix[i],
                    oneOverNormSquaredArray[i].sqrt());
        }

        return orthogonalMatrix;
    }

    /** Return a new matrix that is formed by taking the real parts of the
     *  complex numbers in the argument matrix.
     *
     *  @param matrix An matrix of complex numbers.
     *  @return A new matrix of the double coefficients of the complex
     *  numbers of <i>matrix</i>.
     */
    public static final double[][] realParts(final Complex[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        double[][] returnValue = new double[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix[i][j].real;
            }
        }
        return returnValue;
    }

    /** Return a new matrix that is constructed from the argument by
     *  subtracting the second matrix from the first one.
     *
     *  @param matrix1 The first matrix of complex numbers.
     *  @param matrix2 The second matrix of complex numbers.
     *  @return A new matrix of complex numbers constructed by
     *  subtracting the second matrix from the first one.
     *  @exception IllegalArgumentException If the matrices do not
     *  have the same dimensions.
     */
    public static final Complex[][] subtract(final Complex[][] matrix1,
            final Complex[][] matrix2) {
        _checkSameDimension("subtract", matrix1, matrix2);

        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        Complex[][] returnValue = new Complex[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix1[i][j].subtract(matrix2[i][j]);
            }
        }
        return returnValue;
    }

    /** Return the sum of the elements of a matrix.
     *  @return The sum of the elements of the matrix.
     */
    public static final Complex sum(final Complex[][] matrix) {
        Complex sum = Complex.ZERO;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                sum = sum.add(matrix[i][j]);
            }
        }
        return sum;
    }

    /** Return a new matrix of complex numbers that is initialized
     *  from a 1-D array.  The format of the array must be (0, 0), (0,
     *  1), ..., (0, n-1), (1, 0), (1, 1), ..., (m-1, n-1) where the
     *  output matrix is to be m x n and entries are denoted by (row,
     *  column).
     *
     *  @param array An array of complex numbers.
     *  @param rows An integer representing the number of rows of the
     *  new matrix.
     *  @param cols An integer representing the number of columns of the
     *  new matrix.
     *  @return A new matrix of complex numbers initialized from a 1-D array.
     */
    public static final Complex[][] toMatrixFromArray(Complex[] array,
            int rows,
            int cols) {
        Complex[][] returnValue = new Complex[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(array, i * cols, returnValue[i], 0, cols);
        }
        return returnValue;
    }

    /** Return a new String representing the matrix, formatted as
     *  in Java array initializers.
     *
     *  @param matrix A matrix of Complex numbers.
     *  @return A new String representing the matrix in Java array initializers.
     */
    public static final String toString(final Complex[][] matrix) {
        return toString(matrix, ", ", "{", "}", "{", ", ", "}");
    }

    /** Return a new String representing the matrix, formatted as
     *  specified by the ArrayStringFormat argument.
     *  To get a String in the Ptolemy expression language format,
     *  call this method with ArrayStringFormat.exprASFormat as the
     *  format argument.
     *
     *  @param matrix A matrix of Complex numbers.
     *  @param elementDelimiter The delimiter between elements,
     *  typically ", ".
     *  @param matrixBegin The start of the matrix, typically "{".
     *  @param matrixEnd The end of the matrix, typically "{".
     *  @param vectorBegin The start of the vector, typically "{".
     *  @param vectorDelimiter The delimiter between elements,
     *  typically ", ".
     *  @param vectorEnd The end of the vector, typically "}".
     *  @return A new String representing the matrix in the specified format.
     */
    public static final String toString(final Complex[][] matrix,
            String elementDelimiter, String matrixBegin, String matrixEnd, 
            String vectorBegin, String vectorDelimiter, String vectorEnd) {
        StringBuffer sb = new StringBuffer();
        sb.append(matrixBegin);

        for (int i = 0; i < _rows(matrix); i++) {

            sb.append(vectorBegin);
            for (int j = 0; j < _columns(matrix); j++) {
                sb.append(matrix[i][j].toString());

                if (j < (_columns(matrix) - 1)) {
                    sb.append(elementDelimiter);
                }
            }

            sb.append(vectorEnd);

            if (i < (_rows(matrix) - 1)) {
                sb.append(vectorDelimiter);
            }
        }

        sb.append(matrixEnd);

        return sb.toString();
    }

    /** Return the trace of a square matrix, which is the sum of the
     *  diagonal entries A<sub>11</sub> + A<sub>22</sub> + ... + A<sub>nn</sub>
     *  Throw an IllegalArgumentException if the matrix is not square.
     *  Note that the trace of a matrix is equal to the sum of its eigenvalues.
     *
     *  @param matrix A matrix of complex numbers.
     *  @return A complex number which is the trace of <i>matrix</i>.
     */
    public static final Complex trace(final Complex[][] matrix) {
        int dim = _checkSquare("trace", matrix);
        Complex sum = Complex.ZERO;

        for (int i = 0; i < dim; i++) {
            sum = sum.add(matrix[i][i]);
        }
        return sum;
    }

    /** Return a new matrix that is constructed by transposing the input
     *  matrix. If the input matrix is m x n, the output matrix will be
     *  n x m. Note that for complex matrices, the conjugate transpose
     *  is more commonly used.
     *
     *  @param matrix A matrix of complex numbers.
     *  @return A new matrix of complex numbers which is the
     *  transpose of <i>matrix</i>.
     */
    public static final Complex[][] transpose(final Complex[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        Complex[][] returnValue = new Complex[columns][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[j][i] = matrix[i][j];
            }
        }
        return returnValue;
    }

    /** Return true if all the distances between corresponding elements in
     *  <i>matrix1</i> and <i>matrix2</i> are all less than or equal to
     *  the magnitude of <i>maxError</i>. If both matrices are empty,
     *  return true.
     *  @param matrix1 The first matrix.
     *  @param matrix2 The second matrix.
     *  @param maxError A complex number whose magnitude is taken to
     *   be the distance threshold.
     *  @exception IllegalArgumentException If the matrices do not have the same
     *   dimensions. This is a run-time exception, so it need not be declared
     *   explicitly.
     *  @return True or false.
     */
    public static final boolean within(Complex[][] matrix1,
            Complex[][] matrix2, Complex maxError) {
        return within(matrix1, matrix2, maxError.magnitude());
    }

    /** Return true if all the distances between corresponding
     *  elements in <i>matrix1</i> and <i>matrix2</i> are all less
     *  than or equal to the magnitude of <i>maxError</i>. If both
     *  matrices are empty, return true. If <i>maxError</i> is negative,
     *  return false.
     *  @param matrix1 The first matrix.
     *  @param matrix2 The second matrix.
     *  @param maxError The threshold for the magnitude of the difference.
     *  @exception IllegalArgumentException If the matrices do not have the same
     *   dimensions.  This is a run-time exception, so it need not be declared explicitly.
     *  @return True or false.
     */
    public static final boolean within(Complex[][] matrix1,
            Complex[][] matrix2, double maxError) {
        _checkSameDimension("within", matrix1, matrix2);

        int rows = _rows (matrix1);
        int columns = _columns (matrix1);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                    if (!matrix1[i][j].isCloseTo(matrix2[i][j], maxError)) {
                    return false;
                    }
            }
        }
        return true;
    }

    /** Return true if all the distances between corresponding
     *  elements in <i>matrix1</i> and <i>matrix2</i> are all less
     *  than or equal to corresponding elements in <i>maxError</i>. If
     *  both matrices are empty, return true. If any element of
     *  <i>maxError</i> is negative, return false.
     *
     *  @param matrix1 The first matrix.
     *  @param matrix2 The second matrix.
     *  @param maxError The matrix of thresholds for the magnitudes of
     *  difference.
     *  @exception IllegalArgumentException If the matrices do not have the same
     *   dimensions.
     *  @return True or false.
     */
    public static final boolean within(Complex[][] matrix1,
            Complex[][] matrix2, double[][] maxError) {
        _checkSameDimension("within", matrix1, matrix2);

        int rows = _rows (matrix1);
        int columns = _columns (matrix1);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (!matrix1[i][j].isCloseTo(matrix2[i][j], maxError[i][j])) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Return true if all the distances between corresponding elements in
     *  <i>matrix1</i> and <i>matrix2</i> are all less than or equal to
     *  the magnitude of the corresponding element in <i>maxError</i>.
     *  If both matrices are empty, return true.
     *
     *  <p>Note that there is no notion of negative distance with
     *  complex numbers, so unlike the within() methods for other
     *  types, this method will not return false if an element of the
     *  maxError matrix is negative.
     *
     *  @param matrix1 The first matrix.
     *  @param matrix2 The second matrix.
     *  @param maxError A matrix of complex numbers whose magnitudes
     *  for each element are taken to be the distance thresholds.
     *  @exception IllegalArgumentException If the arrays are not of the same
     *   length.
     *  @return True or false.
     */
    public static final boolean within(Complex[][] matrix1,
            Complex[][] matrix2, Complex[][] maxError) {

        int rows = _rows (maxError);
        int columns = _columns (maxError);

        double[][] doubleError = new double[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                doubleError[i][j] = maxError[i][j].magnitude();
            }
        }

        return within(matrix1, matrix2, doubleError);
    }

    /** Return a new complex matrix whose entries are all zero.
     *  The size of the matrix is specified by the input arguments.
     *
     *  @param rows The number of rows of the zero matrix.
     *  @param columns The number of columns of the zero matrix.
     *  @return A new complex matrix whose entries are all zero.
     */
    public static final Complex[][] zero(int rows, int columns) {
        return _zeroMatrix(new Complex[rows][columns], rows, columns);
    }

    /////////////////////////////////////////////////////////////////////////
    ////                      protected methods                          ////

    /** Check that the two matrix arguments are of the same dimension.
     *  If they are not, an IllegalArgumentException is thrown.
     *
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
                    "ptolemy.math.ComplexMatrixMath." + caller +
                    "() : one matrix " +
                    _dimensionString(matrix1) +
                    " is not the same size as another matrix " +
                    _dimensionString(matrix2) + ".");
        }
    }

    /** Check that the argument matrix is a square matrix. If the matrix is not
     *  square, an IllegalArgumentException is thrown.
     *
     *  @param caller A string representing the caller method name.
     *  @param matrix A matrix of complex numbers.
     *  @return The dimension of the square matrix (an int).
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

    /** Return the number of columns of a matrix.
     *
     *  @param matrix A matrix of complex numbers.
     *  @return The number of columns of the given matrix.
     */
    protected static final int _columns(final Complex[][] matrix) {
        return matrix[0].length;
    }

    /** Print out the dimensions of the given matrix.
     *
     *  @param matrix A matrix of complex numbers.
     *  @return A string specifying the dimensions of the given matrix.
     */
    protected static final String _dimensionString(final Complex[][] matrix) {
        return ("[" + _rows(matrix) + " x " + _columns(matrix) + "]");
    }

    /** Given a set of row vectors rowArrays[0] ... rowArrays[n-1], compute:
     *  <ol>
     *  <li> A new set of row vectors out[0] ... out[n-1] which are the
     *       orthogonalized versions of each input row vector. If a row
     *       vector rowArray[i] is a linear combination of the last 0 .. i
     *       - 1 row vectors, set array[i] to an array of 0's (array[i]
     *       being the 0 vector is a special case of this). Put the result
     *       in returnValue[0].<br>
     *
     *  <li> An n x n matrix containing the dot products of the input
     *       row vectors and the output row vectors,
     *       dotProductMatrix[j][i] = <rowArray[i], outArray[j]>.  Put
     *       the result in returnValue[1].<br>
     *
     *  <li> An array containing 1 / (norm(outArray[i])<sup>2</sup>),
     *       with n entries.  Put the result in returnValue[2].<br>
     *
     *  <li> A count of the number of rows that were found to be linear
     *       combinations of previous rows. Replace those rows with rows
     *       of zeros. The count is equal to the nullity of the
     *       transpose of the input matrix. Wrap the count with an
     *       Integer, and put it in returnValue[3].
     *  </ol>
     *  Orthogonalization is done with the Gram-Schmidt process.
     *
     *  @param rowArrays A set of row vectors.
     *  @return An array of four objects, where the first is the
     *   orthogonal matrix, the second is a matrix containing the dot
     *   products of the input rows with the output rows, the third is
     *   an array of the reciprocals of the norms squared of the orthogonal
     *   rows, and the fourth is an Integer containing the number of
     *   linearly independent rows in the argument matrix.
     */
    protected static final Object[] _orthogonalizeRows(Complex[][] rowArrays) {
        int rows = rowArrays.length;
        int columns =  rowArrays[0].length;
        int nullity = 0;

        Complex[][] orthogonalMatrix = new Complex[rows][];

        Complex[] oneOverNormSquaredArray = new Complex[rows];

        // A matrix containing the dot products of the input row
        // vectors and output row vectors, dotProductMatrix[j][i] =
        // <rowArray[i], outArray[j]>
        Complex[][] dotProductMatrix = new Complex[rows][rows];

        for (int i = 0; i < rows; i++) {
            // Get a reference to the row vector.
            Complex[] refArray = rowArrays[i];

            // Initialize row vector.
            Complex[] rowArray = refArray;

            // Subtract projections onto all previous vectors.
            for (int j = 0; j < i; j++) {

                // Save the dot product for future use for QR decomposition.
                Complex dotProduct =
                    ComplexArrayMath.dotProduct(refArray, orthogonalMatrix[j]);

                dotProductMatrix[j][i] = dotProduct;

                rowArray = ComplexArrayMath.subtract(rowArray,
                        ComplexArrayMath.scale(orthogonalMatrix[j],
                                dotProduct.multiply(oneOverNormSquaredArray[j])));
            }

            // Compute the dot product between the input and output vector
            // for the diagonal entry of dotProductMatrix.
            dotProductMatrix[i][i] =
                ComplexArrayMath.dotProduct(refArray, rowArray);

            // Check the norm to find zero rows, and save the 1 /
            // norm^2 for later computation.
            double normSqrd = ComplexArrayMath.l2normSquared(rowArray);

            Complex normSquared = new Complex (normSqrd, 0.0);

            Complex Zero_Complex = new Complex(0.0, 0.0);
            if (normSquared == Zero_Complex) {
                if (i == 0) {
                    // The input row was the zero vector, we now have
                    // a reference to it.  Set the row to a new zero
                    // vector to ensure the output memory is entirely
                    // disjoint from the input memory.
                    orthogonalMatrix[i] = new Complex[columns];
                } else {
                    // Reuse the memory allocated by the last
                    // subtract() call -- the row is all zeros.
                    orthogonalMatrix[i] = rowArray;
                }

                // Set the normalizing factor to 0.0 to avoid division by 0,
                // it works because the projection onto the zero vector yields
                // zero.
                oneOverNormSquaredArray[i] = Zero_Complex;

                nullity++;
            } else {
                orthogonalMatrix[i] = rowArray;
                Complex One_Complex = new Complex (1.0, 0.0);
                oneOverNormSquaredArray[i] = One_Complex.divide(normSquared);
            }
        }
        return new Object[] {
            orthogonalMatrix,
            dotProductMatrix,
            oneOverNormSquaredArray,
            new Integer(nullity)
                };
    }

    /** Return the number of rows of a matrix.
     *
     *  @param matrix A matrix of complex numbers.
     *  @return The number of rows of the given matrix.
     */
    protected static final int _rows(final Complex[][] matrix) {
        return matrix.length;
    }

    /** Place zeroes in specific places of the given matrix.
     *
     *  @param matrix A matrix of complex numbers.
     *  @param rows The number of rows for the matrix.
     *  @param columns The number of columns for the matrix.
     *  @return The modified matrix with zeroes in the desired positions.
     */
    protected static final Complex[][] _zeroMatrix(Complex[][] matrix,
            int rows, int columns) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                matrix[i][j] = Complex.ZERO;
            }
        }
        return matrix;
    }
}
