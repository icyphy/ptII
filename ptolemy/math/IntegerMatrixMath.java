/* A library for mathematical operations on matrices of ints.

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
@AcceptedRating Yellow (ctsay@eecs.berkeley.edu)
*/

package ptolemy.math;


//////////////////////////////////////////////////////////////////////////
//// IntegerMatrixMath
/**
This class provides a library for mathematical operations on
matrices of ints.

Rows and column numbers of matrices are specified with zero-based indices.
All calls expect matrix arguments to be non-null. In addition, all
rows of the matrix are expected to have the same number of columns.

@author Jeff Tsay
@version $Id$
@since Ptolemy II 1.0
*/
public class IntegerMatrixMath {

    // private constructor prevents construction of this class.
    private IntegerMatrixMath() {}

    /** Return a new matrix that is constructed from the argument by
     *  adding the second argument to every element.
     *  @param matrix A matrix of ints.
     *  @param z The int number to add.
     *  @return A new matrix of ints.
     */
    public static final int[][] add(int[][] matrix, int z) {
        int[][] returnValue = new int[_rows(matrix)][_columns(matrix)];
        for (int i = 0; i < _rows(matrix); i++) {
            for (int j = 0; j < _columns(matrix); j++) {
                returnValue[i][j] = matrix[i][j] + z;
            }
        }
        return returnValue;
    }

    /** Return a new matrix that is constructed from the argument by
     *  adding the second matrix to the first one.  If the two
     *  matrices are not the same size, throw an
     *  IllegalArgumentException.
     *  @param matrix1 The first matrix of ints.
     *  @param matrix2 The second matrix of ints.
     *  @return A new matrix of ints.  */
    public static final int[][] add(final int[][] matrix1,
            final int[][] matrix2) {
        _checkSameDimension("add", matrix1, matrix2);

        int[][] returnValue = new int[_rows(matrix1)][_columns(matrix1)];
        for (int i = 0; i < _rows(matrix1); i++) {
            for (int j = 0; j < _columns(matrix1); j++) {
                returnValue[i][j] = matrix1[i][j] + matrix2[i][j];
            }
        }
        return returnValue;
    }

    /** Return a new matrix that is a copy of the matrix argument.
     *  @param matrix A matrix of ints.
     *  @return A new matrix of ints.
     */
    public static final int[][] allocCopy(final int[][] matrix) {
        return crop(matrix, 0, 0, _rows(matrix), _columns(matrix)) ;
    }

    /** Return a new array that is formed by applying an instance of a
     *  IntegerBinaryOperation to each element in the input matrix,
     *  using z as the left operand in all cases and the matrix elements
     *  as the right operands (op.operate(z, matrix[i][j])).
     */
    public static final int[][] applyBinaryOperation(
            IntegerBinaryOperation op, final int z, final int[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        int[][] returnValue = new int[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = op.operate(z, matrix[i][j]);
            }
        }
        return returnValue;
    }

    /** Return a new array that is formed by applying an instance of a
     *  IntegerBinaryOperation to each element in the input matrix,
     *  using the matrix elements as the left operands and z as the right
     *  operand in all cases (op.operate(matrix[i][j], z)).
     */
    public static final int[][] applyBinaryOperation(
            IntegerBinaryOperation op, final int[][] matrix, final int z) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        int[][] returnValue = new int[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = op.operate(matrix[i][j], z);
            }
        }
        return returnValue;
    }

    /** Return a new array that is formed by applying an instance of a
     *  IntegerBinaryOperation to the two matrices, element by
     *  element, using the elements of the first matrix as the left
     *  operands and the elements of the second matrix as the right
     *  operands.  (op.operate(matrix1[i][j], matrix2[i][j])).  If the
     *  matrices are not the same size, throw an
     *  IllegalArgumentException.
     */
    public static final int[][] applyBinaryOperation(
            IntegerBinaryOperation op, final int[][] matrix1,
            final int[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("applyBinaryOperation", matrix1, matrix2);

        int[][] returnValue = new int[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = op.operate(matrix1[i][j], matrix2[i][j]);
            }
        }
        return returnValue;
    }

    /** Return a new array that is formed by applying an instance of a
     *  IntegerUnaryOperation to each element in the input matrix
     *  (op.operate(matrix[i][j])).
     */
    public static final int[][] applyUnaryOperation(
            final IntegerUnaryOperation op, final int[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        int[][] returnValue = new int[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = op.operate(matrix[i][j]);
            }
        }
        return returnValue;
    }

    /** Return a new matrix that is the formed by bitwise ANDing z
     *  with each element of the input matrix (matrix[i][j] & z).
     */
    public static final int[][] bitwiseAnd(final int[][] matrix, final int z) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        int[][] returnValue = new int[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix[i][j] & z;
            }
        }

        return returnValue;
    }

    /** Return a new array that is the element-by-element bitwise AND
     *  of the two input matrices (matrix1[i][j] & matrix2[i][j]).  If
     *  the two matrices are not the same size, throw an
     *  IllegalArgumentException.
     */
    public static final int[][] bitwiseAnd(final int[][] matrix1,
            final int[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("bitwiseAnd", matrix1, matrix2);

        int[][] returnValue = new int[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix1[i][j] & matrix2[i][j];
            }
        }
        return returnValue;
    }

    /** Return a new array that formed by the bitwise complement of
     *  each element in the input matrix (~matrix[i][j]).
     */
    public static final int[][] bitwiseComplement(final int[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        int[][] returnValue = new int[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = ~matrix[i][j];
            }
        }
        return returnValue;
    }

    /** Return a new matrix that is the formed by bitwise ORing z with
     *  each element of the input matrix (matrix[i][j] | z).

     */
    public static final int[][] bitwiseOr(final int[][] matrix, final int z) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        int[][] returnValue = new int[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix[i][j] | z;
            }
        }

        return returnValue;
    }

    /** Return a new array that is the element-by-element bitwise OR
     *  of the two input matrices (matrix1[i][j] | matrix2[i][j]).  If
     *  the two matrices are not the same size, throw an
     *  IllegalArgumentException.
     */
    public static final int[][] bitwiseOr(final int[][] matrix1,
            final int[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("bitwiseOr", matrix1, matrix2);

        int[][] returnValue = new int[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix1[i][j] | matrix2[i][j];
            }
        }
        return returnValue;
    }

    /** Return a new matrix that is the formed by bitwise XORing z
     *  with each element of the input matrix (matrix[i][j] ^ z).
     */
    public static final int[][] bitwiseXor(final int[][] matrix, final int z) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        int[][] returnValue = new int[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix[i][j] ^ z;
            }
        }

        return returnValue;
    }

    /** Return a new array that is the element-by-element bitwise XOR
     *  of the two input matrices (matrix1[i][j] & matrix2[i][j]).  If
     *  the two matrices are not the same size, throw an
     *  IllegalArgumentException.
     */
    public static final int[][] bitwiseXor(final int[][] matrix1,
            final int[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("bitwiseXor", matrix1, matrix2);

        int[][] returnValue = new int[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix1[i][j] ^ matrix2[i][j];
            }
        }
        return returnValue;
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
    public static final int[][] crop(final int[][] matrix,
            final int rowStart, final int colStart,
            final int rowSpan, final int colSpan) {
        int[][] returnValue = new int[rowSpan][colSpan];
        for (int i = 0; i < rowSpan; i++) {
            System.arraycopy(matrix[rowStart + i], colStart,
                    returnValue[i], 0, colSpan);
        }
        return returnValue;
    }

    /** Return a new matrix that is constructed by placing the
     *  elements of the input array on the diagonal of the square
     *  matrix, starting from the top left corner down to the bottom
     *  right corner. All other elements are zero. The size of of the
     *  matrix is n x n, where n is the length of the input array.
     */
    public static final int[][] diag(final int[] array) {
        int n = array.length;

        int[][] returnValue = new int[n][n];

        // Assume the matrix is zero-filled.

        for (int i = 0; i < n; i++) {
            returnValue[i][i] = array[i];
        }

        return returnValue;
    }

    /** Return a new matrix that is constructed from the argument by
     *  dividing the second argument to every element.
     *  @param matrix A matrix of ints.
     *  @param z The int number to divide.
     *  @return A new matrix of ints.
     */
    public static final int[][] divide(int[][] matrix, int z) {
        int[][] returnValue = new int[_rows(matrix)][_columns(matrix)];
        for (int i = 0; i < _rows(matrix); i++) {
            for (int j = 0; j < _columns(matrix); j++) {
                returnValue[i][j] = matrix[i][j]/z;
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
    public static final int[][] divideElements(final int[][] matrix1,
            final int[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("divideElements", matrix1, matrix2);

        int[][] returnValue = new int[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix1[i][j] / matrix2[i][j];
            }
        }
        return returnValue;
    }

    /** Return a new array that is filled with the contents of the matrix.
     *  The ints are stored row by row, i.e. using the notation
     *  (row, column), the entries of the array are in the following order
     *  for a (m, n) matrix :
     *  (0, 0), (0, 1), (0, 2), ... , (0, n-1), (1, 0), (1, 1), ..., (m-1)(n-1)
     *  @param matrix A matrix of ints.
     *  @return A new array of ints.
     */
    public static final int[] fromMatrixToArray(final int[][] matrix) {
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
    public static final int[] fromMatrixToArray(final int[][] matrix,
            int maxRow, int maxCol) {
        int[] returnValue = new int[maxRow * maxCol];
        for (int i = 0; i < maxRow; i++) {
            System.arraycopy(matrix[i], 0, returnValue, i * maxCol, maxCol);
        }
        return returnValue;
    }

    /** Return an new identity matrix with the specified dimension. The
     *  matrix is square, so only one dimension specifier is needed.
     */
    public static final int[][] identity(final int dim) {
        int[][] returnValue = new int[dim][dim];
        // we rely on the fact Java fills the allocated matrix with 0's
        for (int i = 0; i < dim; i++) {
            returnValue[i][i] = 1;
        }
        return returnValue;
    }

    /** Return an new identity matrix with the specified dimension. The
     *  matrix is square, so only one dimension specifier is needed.
     */
    public static final int[][] identityMatrixInt(final int dim) {
        return identity(dim);
    }

    /** Replace the first matrix argument elements with the values of
     *  the second matrix argument. The second matrix argument must be
     *  large enough to hold all the values of second matrix argument.
     *  @param destMatrix A matrix of ints, used as the destination.
     *  @param srcMatrix A matrix of ints, used as the source.
     */
    public static final void matrixCopy(final int[][] srcMatrix,
            final int[][] destMatrix) {
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
    public static final void matrixCopy(final int[][] srcMatrix,
            final int srcRowStart, final int srcColStart,
            final int[][] destMatrix,
            final int destRowStart, final int destColStart,
            final int rowSpan, final int colSpan) {
        // We should verify the parameters here
        for (int i = 0; i < rowSpan; i++) {
            System.arraycopy(srcMatrix[srcRowStart + i], srcColStart,
                    destMatrix[destRowStart + i], destColStart,
                    colSpan);
        }
    }

    /** Return a new matrix that is constructed by computing the
     *  remainders between each element in the matrix and z.
     */
    public static final int[][] modulo(final int[][] matrix,
            final int z) {
        int[][] returnValue = new int[_rows(matrix)][_columns(matrix)];
        for (int i = 0; i < _rows(matrix); i++) {
            for (int j = 0; j < _columns(matrix); j++) {
                returnValue[i][j] = matrix[i][j] % z;
            }
        }
        return returnValue;
    }

    /** Return a new matrix that is constructed by computing the
     *  remainders between each element in the first matrix argument
     *  and the corresponding element in the second matrix argument.
     *  If the two matrices are not the same size, throw an
     *  IllegalArgumentException.
     */
    public static final int[][] modulo(final int[][] matrix1,
            final int[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("modulo", matrix1, matrix2);

        int[][] returnValue = new int[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix1[i][j] % matrix2[i][j];
            }
        }
        return returnValue;
    }

    /** Return a new matrix that is constructed by multiplying the matrix
     *  by a scaleFactor.
     */
    public static final int[][] multiply(final int[][] matrix,
            final int scaleFactor) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        int[][] returnValue = new int[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix[i][j] * scaleFactor;
            }
        }
        return returnValue;
    }

    /** Return a new array that is constructed from the argument by
     *  pre-multiplying the array (treated as a row vector) by a matrix.
     *  The number of rows of the matrix must equal the number of elements
     *  in the array. The returned array will have a length equal to the number
     *  of columns of the matrix.
     */
    public static final int[] multiply(final int[][] matrix,
            final int[] array) {

        int rows = _rows(matrix);
        int columns = _columns(matrix);

        if (rows != array.length) {
            throw new IllegalArgumentException(
                    "preMultiply : array does not have the same number of " +
                    "elements (" + array.length + ") as the number of rows " +
                    "of the matrix (" + rows + ")");
        }

        int[] returnValue = new int[columns];
        for (int i = 0; i < columns; i++) {
            int sum = 0;
            for (int j = 0; j < rows; j++) {
                sum += matrix[j][i] * array[j];
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
     */
    public static final int[] multiply(final int[] array,
            final int[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        if (columns != array.length) {
            throw new IllegalArgumentException(
                    "postMultiply() : array does not have the same number " +
                    "of elements (" + array.length + ") as the number of " +
                    "columns of the matrix (" + columns + ")");
        }

        int[] returnValue = new int[rows];
        for (int i = 0; i < rows; i++) {
            int sum = 0;
            for (int j = 0; j < columns; j++) {
                sum += matrix[i][j] * array[j];
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
     *  @see #multiplyElements(int[][], int[][])
     *  @param matrix1 The first matrix of ints.
     *  @param matrix2 The second matrix of ints.
     *  @return A new matrix of ints.
     */
    public static final int[][] multiply(int[][] matrix1,
            int[][] matrix2) {
        int[][] returnValue = new int[_rows(matrix1)][matrix2[0].length];
        for (int i = 0; i < _rows(matrix1); i++) {
            for (int j = 0; j < matrix2[0].length; j++) {
                int sum = 0;
                for (int k = 0; k < matrix2.length; k++) {
                    sum += matrix1[i][k] * matrix2[k][j];
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
     *  See {@link #multiply(int[][], int[][])} for standard
     *  matrix multiplication.
     */
    public static final int[][] multiplyElements(final int[][] matrix1,
            final int[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("multiplyElements", matrix1, matrix2);

        int[][] returnValue = new int[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix1[i][j] * matrix2[i][j];
            }
        }
        return returnValue;
    }

    /** Return a new matrix that is the additive inverse of the
     *  argument matrix.
     */
    public static final int[][] negative(final int[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        int[][] returnValue = new int[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = -matrix[i][j];
            }
        }
        return returnValue;
    }

    /** Return a new matrix that is constructed from the argument by
     *  arithmetically shifting the elements in the matrix by the
     *  second argument.  If the second argument is positive, the
     *  elements are shifted left by the second argument. If the
     *  second argument is negative, the elements are shifted right
     *  (arithmetically, with the >>> operator) by the absolute value
     *  of the second argument. If the second argument is 0, no
     *  operation is performed (the matrix is just copied).
     *
     *  @param matrix A first matrix of ints.
     *  @param shiftAmount The amount to shift by, positive for left shift,
     *  negative for right shift.
     *  @return A new matrix of ints.
     */
    public static final int[][] shiftArithmetic(final int[][] matrix,
            final int shiftAmount) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        int[][] returnValue = new int[rows][columns];

        if (shiftAmount >= 0) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    returnValue[i][j] = matrix[i][j] << shiftAmount;
                }
            }
        } else if (shiftAmount < 0) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    returnValue[i][j] = matrix[i][j] >>> -shiftAmount;
                }
            }
        }

        return returnValue;
    }

    /** Return a new matrix that is constructed from the argument by
     *  logically shifting the elements in the matrix by the second
     *  argument.  If the second argument is positive, the elements
     *  are shifted left by the second argument. If the second
     *  argument is negative, the elements are shifted right
     *  (logically, with the >> operator) by the absolute value of the
     *  second argument. If the second argument is 0, no operation is
     *  performed (the matrix is just copied).
     *
     *  @param matrix A first matrix of ints.
     *  @param shiftAmount The amount to shift by, positive for left shift,
     *  negative for right shift.
     *  @return A new matrix of ints.
     */
    public static final int[][] shiftLogical(final int[][] matrix,
            final int shiftAmount) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        int[][] returnValue = new int[rows][columns];

        if (shiftAmount >= 0) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    returnValue[i][j] = matrix[i][j] << shiftAmount;
                }
            }
        } else if (shiftAmount < 0) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    returnValue[i][j] = matrix[i][j] >> -shiftAmount;
                }
            }
        }

        return returnValue;
    }

    /** Return a new matrix that is constructed from the argument by
     *  subtracting the second matrix from the first one.  If the two
     *  matrices are not the same size, throw an
     *  IllegalArgumentException.
     */
    public static final int[][] subtract(final int[][] matrix1,
            final int[][] matrix2) {
        _checkSameDimension("subtract", matrix1, matrix2);

        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        int[][] returnValue = new int[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = matrix1[i][j] - matrix2[i][j];
            }
        }
        return returnValue;
    }

    /** Return the sum of the elements of a matrix.
     *  @return The sum of the elements of the matrix.
     */
    public static final int sum(final int[][] matrix) {
        int sum = 0;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                sum += matrix[i][j];
            }
        }
        return sum;
    }

    /** Return a new matrix that is formed by converting the integers
     *  in the argument matrix to complex numbers. Each complex number
     *  has a real part equal to the value in the argument matrix and a
     *  zero imaginary part.
     *
     *  @param matrix A matrix of integers.
     *  @return A new matrix of complex numbers.
     */
    public static final Complex[][] toComplexMatrix(final int[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        Complex[][] returnValue = new Complex[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = new Complex((double)matrix[i][j], 0.0);
            }
        }
        return returnValue;
    }

    /** Return a new matrix that is formed by converting the ints in
     *  the argument matrix to doubles.
     *  @param matrix An matrix of int.
     *  @return A new matrix of doubles.
     */
    public static final double[][] toDoubleMatrix(final int[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        double[][] returnValue = new double[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = (double) matrix[i][j];
            }
        }
        return returnValue;
    }

    /** Return a new matrix that is formed by converting the ints in
     *  the argument matrix to floats.
     *  @param matrix An matrix of int.
     *  @return A new matrix of floats.
     */
    public static final float[][] toFloatMatrix(final int[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        float[][] returnValue = new float[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = (float) matrix[i][j];
            }
        }
        return returnValue;
    }

    /** Return a new matrix that is formed by converting the ints in
     *  the argument matrix to longs.
     *  @param matrix An matrix of int.
     *  @return A new matrix of longs.
     */
    public static final long[][] toLongMatrix(final int[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        long[][] returnValue = new long[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[i][j] = (long) matrix[i][j];
            }
        }
        return returnValue;
    }

    /** Return a new matrix of ints that is initialized from a 1-D array.
     *  The format of the array must be (0, 0), (0, 1), ..., (0, n-1), (1, 0),
     *  (1, 1), ..., (m-1, n-1) where the output matrix is to be m x n and
     *  entries are denoted by (row, column).
     *  @param array An array of ints.
     *  @param rows An integer representing the number of rows of the new
     *  matrix.
     *  @param cols An integer representing the number of columns of the new
     *  matrix.
     *  @return A new matrix of ints.
     */
    public static final int[][] toMatrixFromArray(int[] array, int rows,
            int cols) {
        int[][] returnValue = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(array, i * cols, returnValue[i], 0, cols);
        }
        return returnValue;
    }

    /** Return a new String representing the matrix, formatted as
     *  in Java array initializers.
     */
    public static final String toString(final int[][] matrix) {
        return toString(matrix, ", ", "{", "}", "{", ", ", "}");
    }

    /** Return a new String representing the matrix, formatted as
     *  specified by the ArrayStringFormat argument.
     *  To get a String in the Ptolemy expression language format,
     *  call this method with ArrayStringFormat.exprASFormat as the
     *  format argument.
     */
    public static final String toString(final int[][] matrix,
            String elementDelimiter, String matrixBegin, String matrixEnd, 
            String vectorBegin, String vectorDelimiter, String vectorEnd) {
        StringBuffer sb = new StringBuffer();
        sb.append(matrixBegin);

        for (int i = 0; i < _rows(matrix); i++) {

            sb.append(vectorBegin);
            for (int j = 0; j < _columns(matrix); j++) {
                sb.append(Integer.toString(matrix[i][j]));

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

        return new String(sb);
    }

    /** Return the trace of a square matrix, which is the sum of the
     *  diagonal entries A<sub>11</sub> + A<sub>22</sub> + ... + A<sub>nn</sub>
     *  Throw an IllegalArgumentException if the matrix is not square.
     *  Note that the trace of a matrix is equal to the sum of its eigenvalues.
     */
    public static final int trace(final int[][] matrix) {
        int dim = _checkSquare("trace", matrix);
        int sum = 0;

        for (int i = 0; i < dim; i++) {
            sum += matrix[i][i];
        }
        return sum;
    }

    /** Return a new matrix that is constructed by transposing the input
     *  matrix. If the input matrix is m x n, the output matrix will be
     *  n x m.
     */
    public static final int[][] transpose(final int[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        int[][] returnValue = new int[columns][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                returnValue[j][i] = matrix[i][j];
            }
        }
        return returnValue;
    }

    /** Return true if the elements of the two matrices differ by no
     *  more than the specified distance. If <i>distance</i> is
     *  negative, return false.
     *  @param matrix1 The first matrix.
     *  @param matrix2 The second matrix.
     *  @param distance The distance to use for comparison.
     *  @return True if the elements of the two matrices are within the
     *   specified distance.
     *  @exception IllegalArgumentException If the matrices do not
     *  have the same dimension.  This is a run-time exception, so it
     *  need not be declared explicitly.
     */
    public static final boolean within(final int[][] matrix1,
            final int[][] matrix2, int distance) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("within", matrix1, matrix2);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (matrix1[i][j] > matrix2[i][j] + distance ||
                        matrix1[i][j] < matrix2[i][j] - distance) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Return true if the elements of the two matrices differ by no more
     *  than the specified distances. If any element of <i>errorMatrix</i> is
     *  negative, return false.
     *  @param matrix1 The first matrix.
     *  @param matrix2 The second matrix.
     *  @param errorMatrix The distance to use for comparison.
     *  @return True if the elements of the two matrices are within the
     *   specified distance.
     *  @exception IllegalArgumentException If the matrices do not
     *  have the same dimension.  This is a run-time exception, so it
     *  need not be declared explicitly.
     */
    public static final boolean within(final int[][] matrix1,
            final int[][] matrix2, final int[][] errorMatrix) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("within", matrix1, matrix2);
        _checkSameDimension("within", matrix1, errorMatrix);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (matrix1[i][j] > matrix2[i][j] + errorMatrix[i][j] ||
                        matrix1[i][j] < matrix2[i][j] - errorMatrix[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Check that the two matrix arguments are of the same dimension.
     *  If they are not, an IllegalArgumentException is thrown.
     *  @param caller A string representing the caller method name.
     *  @param matrix1 A matrix of ints.
     *  @param matrix2 A matrix of ints.
     */
    protected static final void _checkSameDimension(final String caller,
            final int[][] matrix1, final int[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        if ((rows != _rows(matrix2)) || (columns != _columns(matrix2))) {
            throw new IllegalArgumentException(
                    "ptolemy.math.IntegerMatrixMath." + caller
                    + "() : one matrix "
                    + _dimensionString(matrix1)
                    + " is not the same size as another matrix "
                    + _dimensionString(matrix2) + ".");
        }
    }

    /** Check that the argument matrix is a square matrix. If the matrix is not
     *  square, an IllegalArgumentException is thrown.
     *  @param caller A string representing the caller method name.
     *  @param matrix A matrix of ints.
     *  @return The dimension of the square matrix.
     */
    protected static final int _checkSquare(final String caller,
            final int[][] matrix) {
        if (_rows(matrix) != _columns(matrix)) {
            throw new IllegalArgumentException(
                    "ptolemy.math.IntegerMatrixMath." + caller +
                    "() : matrix argument " + _dimensionString(matrix) +
                    " is not a square matrix.");
        }
        return _rows(matrix);
    }

    /** Return the number of columns of a matrix. */
    protected static final int _columns(final int[][] matrix) {
        return matrix[0].length;
    }

    protected static final String _dimensionString(final int[][] matrix) {
        return ("[" + _rows(matrix) + " x " + _columns(matrix) + "]");
    }

    /** Return the number of rows of a matrix. */
    protected static final int _rows(final int[][] matrix) {
        return matrix.length;
    }
}
