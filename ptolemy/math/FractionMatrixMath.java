/* A library for mathematical operations on matrices of Fractions.

 Copyright (c) 2004-2014 The Regents of the University of California.
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

//////////////////////////////////////////////////////////////////////////
//// FractionMatrixMath

/**
 A library for mathematical operations on matrices of Fractions.

 <p>Rows and column numbers of matrices are specified with zero-based indices.
 All calls expect matrix arguments to be non-null. In addition, all
 rows of the matrix are expected to have the same number of columns.

 @author Adam Cataldo
 @version $Id$
 @since Ptolemy II 5.0
 @Pt.ProposedRating Red (acataldo)
 @Pt.AcceptedRating Red (cxh)
 */
public class FractionMatrixMath {
    // private constructor prevents construction of this class.
    private FractionMatrixMath() {
    }

    /** Return a new matrix that is constructed from the argument by
     *  adding the second argument to every element.
     *  @param matrix A matrix of Fractions.
     *  @param z The Fraction to add.
     *  @return A new matrix of Fractions.
     */
    public static final Fraction[][] add(Fraction[][] matrix, Fraction z) {
        Fraction[][] returnValue = new Fraction[_rows(matrix)][_columns(matrix)];

        for (int i = 0; i < _rows(matrix); i++) {
            for (int j = 0; j < _columns(matrix); j++) {
                returnValue[i][j] = matrix[i][j].add(z);
            }
        }

        return returnValue;
    }

    /** Return a new matrix that is constructed from the argument by
     *  adding the second matrix to the first one.  If the two
     *  matrices are not the same size, throw an
     *  IllegalArgumentException.
     *  @param matrix1 The first matrix of Fractions.
     *  @param matrix2 The second matrix of Fractions.
     *  @return A new matrix of Fractions.  */
    public static final Fraction[][] add(final Fraction[][] matrix1,
            final Fraction[][] matrix2) {
        _checkSameDimension("add", matrix1, matrix2);

        Fraction[][] returnValue = new Fraction[_rows(matrix1)][_columns(matrix1)];

        for (int i = 0; i < _rows(matrix1); i++) {
            for (int j = 0; j < _columns(matrix1); j++) {
                returnValue[i][j] = matrix1[i][j].add(matrix2[i][j]);
            }
        }

        return returnValue;
    }

    /** Return a new matrix that is a copy of the matrix argument.
     *  @param matrix A matrix of Fractions.
     *  @return A new matrix of Fractions.
     */
    public static final Fraction[][] allocCopy(final Fraction[][] matrix) {
        return crop(matrix, 0, 0, _rows(matrix), _columns(matrix));
    }

    /** Return a new matrix that is a sub-matrix of the input
     *  matrix argument. The row and column from which to start
     *  and the number of rows and columns to span are specified.
     *  @param matrix A matrix of Fractions.
     *  @param rowStart An int specifying which row to start on.
     *  @param colStart An int specifying which column to start on.
     *  @param rowSpan An int specifying how many rows to copy.
     *  @param colSpan An int specifying how many columns to copy.
     */
    public static final Fraction[][] crop(final Fraction[][] matrix,
            final int rowStart, final int colStart, final int rowSpan,
            final int colSpan) {
        Fraction[][] returnValue = new Fraction[rowSpan][colSpan];

        for (int i = 0; i < rowSpan; i++) {
            System.arraycopy(matrix[rowStart + i], colStart, returnValue[i], 0,
                    colSpan);
        }

        return returnValue;
    }

    /** Return a new matrix that is constructed by placing the
     *  elements of the input array on the diagonal of the square
     *  matrix, starting from the top left corner down to the bottom
     *  right corner. All other elements are zero. The size of of the
     *  matrix is n x n, where n is the length of the input array.
     */
    public static final Fraction[][] diag(final Fraction[] array) {
        int n = array.length;

        Fraction[][] returnValue = new Fraction[n][n];

        // Assume the matrix is zero-filled.
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    returnValue[i][j] = array[i];
                } else {
                    returnValue[i][j] = new Fraction(0, 1);
                }
            }
        }

        return returnValue;
    }

    /** Return a new matrix that is constructed from the argument by
     *  dividing the second argument to every element.
     *  @param matrix A matrix of Fractions.
     *  @param z The Fraction to divide by.
     *  @return A new matrix of Fractions.
     */
    public static final Fraction[][] divide(Fraction[][] matrix, Fraction z) {
        Fraction[][] returnValue = new Fraction[_rows(matrix)][_columns(matrix)];

        for (int i = 0; i < _rows(matrix); i++) {
            for (int j = 0; j < _columns(matrix); j++) {
                returnValue[i][j] = matrix[i][j].divide(z);
            }
        }

        return returnValue;
    }

    /** Return a new matrix that is constructed by element by element
     *  division of the two matrix arguments. Each element of the
     *  first matrix is divided by the corresponding element of the
     *  second matrix.  If the two matrices are not the same size,
     *  throw an IllegalArgumentException.
     */
    public static final Fraction[][] divideElements(final Fraction[][] matrix1,
            final Fraction[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("divideElements", matrix1, matrix2);

        Fraction[][] returnValue = new Fraction[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix1[i][j].divide(matrix2[i][j]);
            }
        }

        return returnValue;
    }

    /** Return a new array that is filled with the contents of the matrix.
     *  The Fractions are stored row by row, i.e. using the notation
     *  (row, column), the entries of the array are in the following order
     *  for a (m, n) matrix :
     *  (0, 0), (0, 1), (0, 2), ... , (0, n-1), (1, 0), (1, 1), ..., (m-1)(n-1)
     *  @param matrix A matrix of Fractions.
     *  @return A new array of Fractions.
     */
    public static final Fraction[] fromMatrixToArray(final Fraction[][] matrix) {
        return fromMatrixToArray(matrix, _rows(matrix), _columns(matrix));
    }

    /** Return a new array that is filled with the contents of the matrix.
     *  The maximum numbers of rows and columns to copy are specified so
     *  that entries lying outside of this range can be ignored. The
     *  maximum rows to copy cannot exceed the number of rows in the matrix,
     *  and the maximum columns to copy cannot exceed the number of columns
     *  in the matrix.
     *  The Fractions are stored row by row, i.e. using the notation
     *  (row, column), the entries of the array are in the following order
     *  for a matrix, limited to m rows and n columns :
     *  (0, 0), (0, 1), (0, 2), ... , (0, n-1), (1, 0), (1, 1), ..., (m-1)(n-1)
     *  @param matrix A matrix of Fractions.
     *  @return A new array of Fractions.
     */
    public static final Fraction[] fromMatrixToArray(final Fraction[][] matrix,
            int maxRow, int maxCol) {
        Fraction[] returnValue = new Fraction[maxRow * maxCol];

        for (int i = 0; i < maxRow; i++) {
            System.arraycopy(matrix[i], 0, returnValue, i * maxCol, maxCol);
        }

        return returnValue;
    }

    /** Return an new identity matrix with the specified dimension. The
     *  matrix is square, so only one dimension specifier is needed.
     *  @return Identity matrix in fractions
     */
    public static final Fraction[][] identity(final int dim) {
        Fraction[][] returnValue = new Fraction[dim][dim];

        // we rely on the fact Java fills the allocated matrix with 0's
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                if (i == j) {
                    returnValue[i][i] = new Fraction(1, 1);
                } else {
                    returnValue[i][j] = new Fraction(0, 1);
                }
            }
        }

        return returnValue;
    }

    /** Return a new matrix that is constructed by multiplying the matrix
     *  by a scaleFactor.
     *  @param matrix The matrix of Fractions.
     *  @param scaleFactor The Fraction to multiply by.
     *  @return The resulting matrix of Fractions.
     */
    public static final Fraction[][] multiply(final Fraction[][] matrix,
            final Fraction scaleFactor) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        Fraction[][] returnValue = new Fraction[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix[i][j].multiply(scaleFactor);
            }
        }

        return returnValue;
    }

    /** Return a new array that is constructed from the argument by
     *  pre-multiplying the matrix by an array (treated as a row vector).
     *  The number of rows of the matrix must equal the number of elements
     *  in the array. The returned array will have a length equal to the number
     *  of columns of the matrix.
     *  @param array The array of Fractions.
     *  @param matrix The matrix of Fractions.
     *  @return The resulting matrix of Fractions.
     */
    public static final Fraction[] multiply(final Fraction[] array,
            final Fraction[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        if (rows != array.length) {
            throw new IllegalArgumentException(
                    "preMultiply : array does not have the same number of "
                            + "elements (" + array.length
                            + ") as the number of rows " + "of the matrix ("
                            + rows + ")");
        }

        Fraction[] returnValue = new Fraction[columns];

        for (int i = 0; i < columns; i++) {
            Fraction sum = new Fraction(0, 1);

            for (int j = 0; j < rows; j++) {
                sum = sum.add(matrix[j][i].multiply(array[j]));
            }

            returnValue[i] = sum;
        }

        return returnValue;
    }

    /** Return a new array that is constructed from the argument by
     *  post-multiplying the matrix by an array (treated as a column vector).
     *  The number of columns of the matrix must equal the number of elements
     *  in the array. The returned array will have a length equal to the number
     *  of rows of the matrix.
     *  @param matrix The matrix of Fractions.
     *  @param array The array of Fractions.
     *  @return The resulting matrix of Fractions.
     */
    public static final Fraction[] multiply(final Fraction[][] matrix,
            final Fraction[] array) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        if (columns != array.length) {
            throw new IllegalArgumentException(
                    "postMultiply() : array does not have the same number "
                            + "of elements (" + array.length
                            + ") as the number of " + "columns of the matrix ("
                            + columns + ")");
        }

        Fraction[] returnValue = new Fraction[rows];

        for (int i = 0; i < rows; i++) {
            Fraction sum = new Fraction(0, 1);

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
     *  @see #multiplyElements(Fraction[][], Fraction[][])
     *  @param matrix1 The first matrix of Fractions.
     *  @param matrix2 The second matrix of Fractions.
     *  @return A new matrix of ints.
     *  @exception ArithmeticException If the matrix dimensions don't match up.
     */
    public static final Fraction[][] multiply(Fraction[][] matrix1,
            Fraction[][] matrix2) throws ArithmeticException {
        if (_columns(matrix1) != _rows(matrix2)) {
            throw new ArithmeticException("Number of columns ("
                    + _columns(matrix1)
                    + ") of matrix1 does note equal number of rows ("
                    + _rows(matrix2) + ") of matrix2.");
        }

        Fraction[][] returnValue = new Fraction[_rows(matrix1)][_columns(matrix2)];

        for (int i = 0; i < _rows(matrix1); i++) {
            for (int j = 0; j < _columns(matrix2); j++) {
                Fraction sum = new Fraction(0, 1);

                for (int k = 0; k < _columns(matrix1); k++) {
                    sum = sum.add(matrix1[i][k].multiply(matrix2[k][j]));
                }

                returnValue[i][j] = sum;
            }
        }

        return returnValue;
    }

    /** Return a new matrix that is constructed by element by element
     *  multiplication of the two matrix arguments.  If the two
     *  matrices are not the same size, throw an
     *  IllegalArgumentException.
     *  <p>Note that this method does pointwise matrix multiplication.
     * @param matrix1 The first matrix of Fractions.
     *  @param matrix2 The second matrix of Fractions.
     *  @return A new matrix of ints.
     */
    public static final Fraction[][] multiplyElements(
            final Fraction[][] matrix1, final Fraction[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("multiplyElements", matrix1, matrix2);

        Fraction[][] returnValue = new Fraction[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix1[i][j].multiply(matrix2[i][j]);
            }
        }

        return returnValue;
    }

    /** Return a new matrix that is the additive inverse of the
     *  argument matrix.
     *  @param matrix the input matrix of Fractions.
     *  @return the output matrix of Fractions.
     */
    public static final Fraction[][] negative(final Fraction[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        Fraction[][] returnValue = new Fraction[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix[i][j].negate();
            }
        }

        return returnValue;
    }

    /** Return a new matrix that is constructed from the argument by
     *  subtracting the second matrix from the first one.  If the two
     *  matrices are not the same size, throw an
     *  IllegalArgumentException.
     *  @param matrix1 The matrix to be subtracted from.
     *  @param matrix2 The matrix to subtract.
     *  @return The difference matrix.
     */
    public static final Fraction[][] subtract(final Fraction[][] matrix1,
            final Fraction[][] matrix2) {
        _checkSameDimension("subtract", matrix1, matrix2);

        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        Fraction[][] returnValue = new Fraction[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix1[i][j].subtract(matrix2[i][j]);
            }
        }

        return returnValue;
    }

    /** Return the sum of the elements of a matrix.
     *  @param matrix The input matrix.
     *  @return The sum of the elements of the matrix.
     */
    public static final Fraction sum(final Fraction[][] matrix) {
        Fraction sum = new Fraction(0, 1);

        for (Fraction[] element : matrix) {
            for (int j = 0; j < element.length; j++) {
                sum = sum.add(element[j]);
            }
        }

        return sum;
    }

    /** Return a new matrix that is formed by converting the Fractions in
     *  the argument matrix to doubles.
     *  @param matrix An matrix of Fractions.
     *  @return A new matrix of doubles.
     */
    public static final double[][] toDoubleMatrix(final Fraction[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        double[][] returnValue = new double[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix[i][j].toDouble();
            }
        }

        return returnValue;
    }

    /** Return a new matrix of Fractions that is initialized from a 1-D array.
     *  The format of the array must be (0, 0), (0, 1), ..., (0, n-1), (1, 0),
     *  (1, 1), ..., (m-1, n-1) where the output matrix is to be m x n and
     *  entries are denoted by (row, column).
     *  @param array An array of Fraction.
     *  @param rows An integer representing the number of rows of the new
     *  matrix.
     *  @param cols An integer representing the number of columns of the new
     *  matrix.
     *  @return A new matrix of Fractions.
     */
    public static final Fraction[][] toMatrixFromArray(Fraction[] array,
            int rows, int cols) {
        Fraction[][] returnValue = new Fraction[rows][cols];

        for (int i = 0; i < rows; i++) {
            System.arraycopy(array, i * cols, returnValue[i], 0, cols);
        }

        return returnValue;
    }

    /** Return a new String representing the matrix, formatted as
     *  in Java array initializers.
     */
    public static final String toString(final Fraction[][] matrix) {
        return toString(matrix, ", ", "{", "}", "{", ", ", "}");
    }

    /** Return a new String representing the matrix, formatted as
     *  specified by the ArrayStringFormat argument.
     *  To get a String in the Ptolemy expression language format,
     *  call this method with ArrayStringFormat.exprASFormat as the
     *  format argument.
     */
    public static final String toString(final Fraction[][] matrix,
            String elementDelimiter, String matrixBegin, String matrixEnd,
            String vectorBegin, String vectorDelimiter, String vectorEnd) {
        StringBuffer sb = new StringBuffer();
        sb.append(matrixBegin);

        for (int i = 0; i < _rows(matrix); i++) {
            sb.append(vectorBegin);

            for (int j = 0; j < _columns(matrix); j++) {
                sb.append(matrix[i][j].toString());

                if (j < _columns(matrix) - 1) {
                    sb.append(elementDelimiter);
                }
            }

            sb.append(vectorEnd);

            if (i < _rows(matrix) - 1) {
                sb.append(vectorDelimiter);
            }
        }

        sb.append(matrixEnd);

        return new String(sb);
    }

    /** Return the trace of a square matrix, which is the sum of the
     *  diagonal entries A<sub>11</sub> + A<sub>22</sub> + ... + A<sub>nn</sub>
     *  Throw an IllegalArgumentException if the matrix is not square.
     *  Note that the trace of a matrix is equal to the sum of its eigenvalues.
     *  @param matrix A square matrix.
     *  @return The trace of this matrix.
     */
    public static final Fraction trace(final Fraction[][] matrix) {
        int dim = _checkSquare("trace", matrix);
        Fraction sum = new Fraction(0, 1);

        for (int i = 0; i < dim; i++) {
            sum = sum.add(matrix[i][i]);
        }

        return sum;
    }

    /** Return a new matrix that is constructed by transposing the input
     *  matrix. If the input matrix is m x n, the output matrix will be
     *  n x m.
     *  @param matrix The input matrix.
     *  @return The matrix transpose.
     */
    public static final Fraction[][] transpose(final Fraction[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        Fraction[][] returnValue = new Fraction[columns][rows];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[j][i] = matrix[i][j];
            }
        }

        return returnValue;
    }

    /** Check that the two matrix arguments are of the same dimension.
     *  If they are not, an IllegalArgumentException is thrown.
     *  @param caller A string representing the caller method name.
     *  @param matrix1 A matrix of ints.
     *  @param matrix2 A matrix of ints.
     */
    protected static final void _checkSameDimension(final String caller,
            final Fraction[][] matrix1, final Fraction[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        if (rows != _rows(matrix2) || columns != _columns(matrix2)) {
            throw new IllegalArgumentException(
                    "ptolemy.math.FractionMatrixMath." + caller
                    + "() : one matrix " + _dimensionString(matrix1)
                    + " is not the same size as another matrix "
                    + _dimensionString(matrix2) + ".");
        }
    }

    /** Check that the argument matrix is a square matrix. If the matrix is not
     *  square, an IllegalArgumentException is thrown.
     *  @param caller A string representing the caller method name.
     *  @param matrix A matrix of Fractions.
     *  @return The dimension of the square matrix.
     */
    protected static final int _checkSquare(final String caller,
            final Fraction[][] matrix) {
        if (_rows(matrix) != _columns(matrix)) {
            throw new IllegalArgumentException(
                    "ptolemy.math.FractionMatrixMath." + caller
                    + "() : matrix argument "
                    + _dimensionString(matrix)
                    + " is not a square matrix.");
        }

        return _rows(matrix);
    }

    /** Return the number of columns of a matrix.
     *  @param matrix The matrix.
     *  @return The number of columns.
     */
    protected static final int _columns(final Fraction[][] matrix) {
        return matrix[0].length;
    }

    /** Return a string that describes the number of rows and columns.
     *  @param matrix The matrix that is to be described.
     *  @return a string describing the dimensions of this matrix.
     */
    protected static final String _dimensionString(final Fraction[][] matrix) {
        return "[" + _rows(matrix) + " x " + _columns(matrix) + "]";
    }

    /** Return the number of rows of a matrix. */
    protected static final int _rows(final Fraction[][] matrix) {
        return matrix.length;
    }
}
