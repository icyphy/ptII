/* A library for mathematical operations on matrices.

Copyright (c) 1998 The Regents of the University of California.
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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.math;

import java.lang.*;

//////////////////////////////////////////////////////////////////////////
//// MatrixMath
/**
 * This class provides library for mathematical operations on
 * double matrices.
 * <p>
 * The suffix "R" on method names means "Replace."  Any method with
 * that suffix modifies the array argument rather than constructing a new
 * array.
 * Rows and column numbers of matrices are specified with zero-based indices.
 *
 * @author Jeff Tsay
 * @version @(#)MatrixMath.java	1.1   11/10/98
 */

public final class MatrixMath {

    // Private constructor prevents construction of this class.
    private MatrixMath() {}

    /** Return the number of rows of a matrix.
     *  @param matrix A matrix of doubles.
     *  @retval An int.
     */
    public static final int rows(double[][] matrix) {
        return matrix.length;
    }

    /** Return the number of columns of a matrix.
     *  @param matrix A matrix of doubles.
     *  @retval An int.
     */
    public static final int columns(double[][] matrix) {
        return matrix[0].length;
    }

    /** Return a new matrix (zero-filled by Java) with the same
     *  dimensions as the input matrix argument.
     *  @param matrix A matrix of doubles.
     *  @retval A new matrix of doubles.
     */
    public static final double[][] alloc(double[][] matrix) {
        return new double[matrix.length][matrix[0].length];
    }

    /** Return a new matrix (zero-filed by Java) with the specified
     *  rows and columns.
     *  @param rows An int.
     *  @param columns An int.
     */
    public static final double[][] alloc(int rows, int columns) {
        return new double[rows][columns];
    }

    /** Return a new array that is filled with the contents of the matrix.
     *  The doubles are stored row by row, i.e. using the notation
     *  (row, column), the entries of the array are in the following order
     *  for a (m,n) matrix :
     *  (0,0), (0,1), (0,2), ... , (0,n-1), (1,0), (1,1), ... , (m-1)(n-1)
     *  @param A matrix of doubles.
     *  @retval A new array of doubles.
     */
    public static final double[] toArray(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[] retval = new double[rows * cols];
        for (int i = 0; i < rows; i++) {
            int offset = i * cols;
            for (int j = 0; j < cols; j++) {
                retval[offset + j] = matrix[i][j];
            }
        }
        return retval;
    }

    /** Return a new array that is filled with the contents of the matrix.
     *  The maximum numbers of rows and columns to copy are specified so
     *  that entries lying outside of this range can be ignored. The
     *  maximum rows to copy cannot exceed the number of rows in the matrix,
     *  and the maximum columns to copy cannot exceed the number of columns
     *  in the matrix.
     *  The doubles are stored row by row, i.e. using the notation
     *  (row, column), the entries of the array are in the following order
     *  for a matrix, limited to m rows and n columns :
     *  (0,0), (0,1), (0,2), ... , (0,n-1), (1,0), (1,1), ... , (m-1)(n-1)
     *  @param matrix A matrix of doubles.
     *  @retval A new array of doubles.
     */
    public static final double[] toArray(double[][] matrix, int maxRow,
                                         int maxCol) {
        double[] retval = new double[maxRow * maxCol];
        for (int i = 0; i < maxRow; i++) {
            int offset = i * maxCol;
            for (int j = 0; j < maxCol; j++) {
                retval[offset + j] = matrix[i][j];
            }
        }
        return retval;
    }

    /** Replace the array elements with those in the matrix argument.
     *  The doubles are stored row by row, i.e. using the notation
     *  (row, column), the entries of the array are in the following order
     *  for a (m,n) matrix :
     *  (0,0), (0,1), (0,2), ... , (0,n-1), (1,0), (1,1), ... , (m-1,n-1)
     *  @param array An array of doubles
     *  @param matrix A matrix of doubles
     */
    public static final void fillArray(double[] array, double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        for (int i = 0; i < rows; i++) {
            int offset = i * cols;
            for (int j = 0; j < cols; j++) {
                array[offset + j] = matrix[i][j];
            }
        }
    }

    /** Replace the array elements with those in the matrix argument.
     *  Values from the matrix are copied to the array up to the
     *  specified row and column.
     *  The doubles are stored row by row, i.e. using the notation
     *  (row, column), the entries of the array are in the following order
     *  for a matrix limited to m rows and n columns :
     *  (0,0), (0,1), (0,2), ... , (0,n-1), (1,0), (1,1), ... , (m-1,n-1)
     *  @param array An array of doubles.
     *  @param matrix A matrix of doubles.
     *  @param maxRows An int, specifying the maximum row of the matrix.
     *  @param maxCols An int, specifying the maximum column of the matrix.
     */
    public static final void fillArray(double[] array, double[][] matrix,
                                       int maxRows, int maxCols) {
        for (int i = 0; i < maxRows; i++) {
            int offset = i * maxCols;
            for (int j = 0; j < maxCols; j++) {
                array[offset + j] = matrix[i][j];
            }
        }
    }

    /** Return a new matrix of doubles that is initialized from a 1-D array.
     *  The format of the array must be (0,0), (0,1), ..., (0, n-1), (1,0),
     *  (1,1), ..., (m-1, n-1) where the output matrix is to be m x n and
     *  entries are denoted by (row, column).
     *  @param array An array of doubles.
     *  @param rows An int.
     *  @param cols An int.
     *  @retval A new matrix of doubles.
     */
    public static final double[][] initFromArray(double[] array, int rows,
                                                 int cols)
    {
        double[][] retval = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            int offset = i * cols;
            for (int j = 0; j < cols; j++) {
                retval[i][j] = array[offset + j];
            }
        }
        return retval;
    }


    /** Replace the first matrix argument elements with the values of
     *  the second matrix argument. The second matrix argument must be
     *  large enough to hold all the values of second matrix argument.
     *  @param destMatrix A matrix of doubles, used as the destination.
     *  @param srcMatrix A matrix of doubles, used as the source.
     */
    public static final void copy(double[][] destMatrix, double[][] srcMatrix) {
        for (int i = 0; i < srcMatrix.length; i++) {
            for (int j = 0; j < srcMatrix[0].length; j++) {
                srcMatrix[i][j] = destMatrix[i][j];
            }
        }
    }

    /** Return a new matrix that is a copy of the matrix argument.
     *  @param matrix A matrix of doubles.
     *  @return A new matrix of doubles.
     */
    public static final double[][] allocCopy(double[][] matrix) {
        double[][] retval = new double[matrix.length][matrix[0].length];
        copy(retval, matrix);
        return retval;
    }

    /** Replace the first matrix argument's values, in the specified row
     *  and column range, with the second matrix argument's values, starting
     *  from specified row and column of the second matrix.
     *  @param destMatrix A matrix of doubles, used as the destination.
     *  @param destRowStart An int specifying the starting row of the dest.
     *  @param rowSpan An int specifying how many rows to copy.
     *  @param destColStart An int specifying the starting column of the
     *         dest.
     *  @param colSpan An int specifying how many columns to copy.
     *  @param srcMatrix A matrix of doubles, used as the destination.
     *  @param srcRowStart An int specifying the starting row of the source.
     *  @param srcColStart An int specifying the starting column of the
     *  source.
     */
    public static final void partialCopy(double[][] destMatrix,
                                         int destRowStart, int rowSpan,
                                         int destColStart, int colSpan,
                                         double[][] srcMatrix,
                                         int srcRowStart, int srcColStart) {
        // We should verify the parameters here
        for (int i = 0; i < rowSpan; i++) {
            for (int j = 0; j < colSpan; j++) {
                destMatrix[destRowStart + i][destColStart + j] =
                 srcMatrix[srcRowStart + i][srcColStart + j];
            }
        }
    }

    /** Return a new matrix that is a sub-matrix of the input
     *  matrix argument. The row and column from which to start
     *  and the number of rows and columns to span are specified.
     *  @param matrix A matrix of doubles.
     *  @param rowStart An int specifying which row to start on.
     *  @param colStart An int specifying which column to start on.
     *  @param rowSpan An int specifying how many rows to copy.
     *  @param colSpan An int specifying how man columns to copy.
     */
    public static final double[][] crop(double[][] matrix,
                                        int rowStart, int colStart,
                                        int rowSpan, int colSpan) {
        double[][] retval = new double[rowSpan][colSpan];
        for (int i = 0; i < rowSpan; i++) {
            for (int j = 0; j < colSpan; j++) {
                retval[i][j] = matrix[rowStart + i][colStart + j];
            }
        }
        return retval;
    }

    /** Return an identity matrix with the specified dimension. The
     *  matrix is square, so only one dimension specifier is needed.
     *  @param dim An int
     *  @retval A new identity matrix of doubles
     */
    public static final double[][] identity(int dim) {
        double[][] retval = new double[dim][dim];
        // we rely on the fact Java fills the allocated matrix with 0's
        for (int i = 0; i < dim; i++) {
            retval[i][i] = 1.0;
        }
        return retval;
    }

    /** Return a new matrix, which is defined by Aij = 1/(i+j+1),
     *  the Hilbert matrix. The matrix is square with one
     *  dimension specifier required.
     *  @param dim An int
     *  @retval A new Hilbert matrix of doubles
     */
    public static final double[][] hilbert(int dim) {
        double[][] retval = new double[dim][dim];
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                retval[i][j] = 1.0 / (double) (i + j + 1);
            }
        }
        return retval;
    }

    // Are these add a scalar methods really useful?

    /** Return a new matrix that is constructed from the argument by
     *  adding the second argument to every element.
     *  @param matrix An array of doubles.
     *  @param z The double number to add.
     *  @return A new matrix of doubles.
     */
    public static final double[][] add(double[][] matrix, double z) {
        double[][] result = new double[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                result[i][j] = matrix[i][j] + z;
            }
        }
        return result;
    }

    /** Modify the matrix argument by
     *  adding the second argument to every element.
     *  @param matrix An matrix of doubles.
     *  @param z The double to add.
     */
    public final void addR(double[][] matrix, double z) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                matrix[i][j] += z;
            }
        }
    }

    /** Return a new matrix that is constructed from the argument by
     *  adding the second matrix to the first one.
     *  @param matrix1 The first matrix of doubles.
     *  @param matrix2 The second matrix of doubles.
     *  @return A new matrix of doubles.
     */
    public static final double[][] add(double[][] matrix1, double[][] matrix2) {
        double[][] result = new double[matrix1.length][matrix1[0].length];
        for (int i = 0; i < matrix1.length; i++) {
            for (int j = 0; j < matrix1[0].length; j++) {
                result[i][j] = matrix1[i][j] + matrix2[i][j];
            }
        }
        return result;
    }

    /** Modify the first matrix argument by
     *  adding the second matrix argument
     *  @param matrix1 The first (modified) matrix of doubles.
     *  @param matrix2 The second matrix of doubles.
     */
    public static final void addR(double[][] matrix1, double[][] matrix2) {
        for (int i = 0; i < matrix1.length-1; i++) {
            for (int j = 0; j < matrix1[0].length; j++) {
                matrix1[i][j] += matrix2[i][j];
            }
        }
    }

    /** Return a new matrix that is constructed from the argument by
     *  subtracting the second matrix from the first one.
     *  @param matrix1 The first matrix of doubles.
     *  @param matrix2 The second matrix of doubles.
     *  @return A new matrix of doubles.
     */
    public static final double[][] subtract(double[][] matrix1,
                                            double[][] matrix2) {
        double[][] result = new double[matrix1.length][matrix1[0].length];
        for (int i = 0; i < matrix1.length; i++) {
            for (int j = 0; j < matrix1[0].length; j++) {
                result[i][j] = matrix1[i][j] - matrix2[i][j];
            }
        }
        return result;
    }

    /** Modify the first matrix argument by subtracting the second matrix
     *  argument from the first one.
     *  @param matrix1 The first (modified) matrix of doubles.
     *  @param matrix2 The second matrix of doubles.
     */
    public static final void subtractR(double[][] matrix1, double[][] matrix2) {
        for (int i = 0; i < matrix1.length-1; i++) {
            for (int j = 0; j < matrix1[0].length; j++) {
                matrix1[i][j] -= matrix2[i][j];
            }
        }
    }

    /** Return a new matrix that is the additive inverse of the
     *  argument matrix.
     *  @param matrix A matrix of doubles.
     *  @return A new matrix of doubles.
     */
    public static final double[][] negative(double[][] matrix) {
        double[][] result = new double[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                result[i][j] = -matrix[i][j];
            }
        }
        return result;
    }

    /** Modify the matrix argument by replacing it with its additive
     *  inverse.
     *  @param matrix A matrix of doubles.
     */
    public static final void negativeR(double[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                matrix[i][j] = -matrix[i][j];
            }
        }
    }

    /** Return a new matrix that is constructed from the argument by
     *  multiplying the first matrix by the second one.
     *  Note this operation is not commutative,
     *  so care must be taken in the ordering of the arguments.
     *  The number of columns of matrix1
     *  must equal the number of rows of matrix2. If matrix1 is of
     *  size m x n, and matrix2 is of size n x p, the returned matrix
     *  will have size m x p.
     *  @param matrix1 The first matrix of doubles.
     *  @param matrix2 The second matrix of doubles.
     *  @return A new matrix of doubles.
     */
    public static final double[][] multiply(double[][] matrix1,
                                            double[][] matrix2) {
        double[][] result = new double[matrix1.length][matrix2[0].length];
        for (int i = 0; i < matrix1.length; i++) {
            for (int j = 0; j < matrix2[0].length; j++) {
                double sum = 0.0;
                for (int k = 0; k < matrix2.length; k++) {
                    sum += matrix1[i][k] * matrix2[k][j];
                }
                result[i][j] = sum;
            }
        }
        return result;
    }

    /** Return a new vector that is constructed from the argument by
     *  pre-multiplying the vector by a matrix. The number of columns of
     *  the matrix must equal the number of elements in the vector.
     *  The returned vector will have the same size as the input
     *  vector. The array is treated as a column vector.
     *  @param matrix A matrix of doubles.
     *  @param array An array of doubles.
     *  @return A new array of doubles.
     */
    public static final double[] preMultiply(double[][] matrix,
                                             double[] array) {
        double[] result = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            double sum = 0.0;
            for (int j = 0; j < array.length; j++) {
                sum += matrix[i][j] * array[j];
            }
            result[i] = sum;
        }
        return result;
    }

    /** Modify the argument vector by pre-multiplying it by a matrix.
     *  The number of columns of the matrix must equal the number of elements
     *  in the vector. The array is treated as a column vector.
     *  @param matrix A matrix of doubles.
     *  @param array An array of doubles.
     */
    public static final void preMultiplyR(double[][] matrix,
                                          double[] array) {
        // Allocate some temporary storage because we can't do this inline.
        double[] result = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            double sum = 0.0;
            for (int j = 0; j < array.length; j++) {
                sum += matrix[i][j] * array[j];
            }
            result[i] = sum;
        }
        // Copy temporary back to input vector
        for (int k = 0; k < array.length; k++) {
            array[k] = result[k];
        }
    }

    /** Return a new vector that is constructed from the argument by
     *  post-multiplying the vector by a matrix. The number of rows of 
     *  the matrix must equal the number of elements in the vector.
     *  The returned vector will have the same size as the input
     *  vector. The array is treated as a row vector.
     *  @param array An array of doubles.
     *  @param matrix A matrix of doubles.
     *  @return A new array of doubles.
     */
    public static final double[] postMultiply(double[] array,
                                              double[][] matrix) {
        double[] result = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            double sum = 0.0;
            for (int j = 0; j < array.length; j++) {
                sum += matrix[j][i] * array[j];
            }
            result[i] = sum;
        }
        return result;
    }

    /** Modify the argument vector by post-multiplying it by a matrix.
     *  The number of rows of the matrix must equal the number of elements in
     *  the vector. The returned vector will have the same size as the input
     *  vector. The array is treated as a row vector.
     *  @param array An array of doubles.
     *  @param matrix A matrix of doubles.
     */
    public static final void postMultiplyR(double[] array,
                                           double[][] matrix) {
        // Allocate some temporary storage because we can't do this inline.
        double[] result = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            double sum = 0.0;
            for (int j = 0; j < array.length; j++) {
                sum += matrix[j][i] * array[j];
            }
            result[i] = sum;
        }

        // Copy temporary back to input vector
        for (int k = 0; k < array.length; k++) {
            array[k] = result[k];
        }
    }

    /** Return a new matrix that is constructed by element by element
     *  multiplication of the two matrix arguments.
     *  @param matrix1 A matrix of doubles.
     *  @param matrix2 A matrix of doubles.
     *  @retval A new matrix of doubles.
     */
    public static final double[][] pwiseMultiply(double[][] matrix1,
                                                 double[][] matrix2) {
        double[][] result = new double[matrix1.length][matrix1[0].length];
        for (int i = 0; i < matrix1.length; i++) {
            for (int j = 0; j < matrix1[0].length; j++) {
                result[i][j] = matrix1[i][j] * matrix2[i][j];
            }
        }
        return result;
    }

    /** Modify the first matrix argument by element by element
     *  multiplication by the second matrix argument.
     *  @param matrix1 A matrix of doubles.
     *  @param matrix2 A matrix of doubles.
     */
    public static final void pwiseMultiplyR(double[][] matrix1,
                                            double[][] matrix2) {
        for (int i = 0; i < matrix1.length; i++) {
            for (int j = 0; j < matrix1[0].length; j++) {
                matrix1[i][j] *= matrix2[i][j];
            }
        }
    }

    /** Return a new matrix that is constructed by element by element
     *  division of the two matrix arguments. Each element of the
     *  first matrix is divided by the corresponding element of the
     *  second matrix.
     *  @param matrix1 A matrix of doubles.
     *  @param matrix2 A matrix of doubles.
     *  @retval A new matrix of doubles.
     */
    public static final double[][] pwiseDivide(double[][] matrix1,
                                               double[][] matrix2) {
        double[][] result = new double[matrix1.length][matrix1[0].length];
        for (int i = 0; i < matrix1.length; i++) {
            for (int j = 0; j < matrix1[0].length; j++) {
                result[i][j] = matrix1[i][j] / matrix2[i][j];
            }
        }
        return result;
    }

    /** Modify the first matrix argument by element by element
     *  division by the second matrix argument.
     *  @param matrix1 A matrix of doubles.
     *  @param matrix2 A matrix of doubles.
     */
    public static final void pwiseDivideR(double[][] matrix1,
                                          double[][] matrix2) {
        for (int i = 0; i < matrix1.length; i++) {
            for (int j = 0; j < matrix1[0].length; j++) {
                matrix1[i][j] /= matrix2[i][j];
            }
        }
    }

    /** Return a new matrix that is constructed by multiplying the matrix
     *  by a scalefactor.
     *  @param matrix A matrix of doubles.
     *  @scalefactor The constant to multiply the matrix by.
     */
    public static final double[][] scale(double[][] matrix,
                                   double scalefactor) {
        double[][] result = new double[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                result[i][j] = matrix[i][j] * scalefactor;
            }
        }
        return result;
    }

    /** Modify the argument matrix by multiplying the matrix by a scalefactor.
     *  @param matrix A matrix of doubles.
     *  @scalefactor The constant to multiply the matrix by.
     */
    public static final void scaleR(double[][] matrix,
                              double scalefactor) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                matrix[i][j] *= scalefactor;
            }
        }
    }

    /** Return a new matrix that is constructed by transposing the input
     *  matrix.
     *  @param matrix A matrix of doubles.
     *  @return A new matrix of doubles.
     */
    public static final double[][] transpose(double[][] matrix) {
        double[][] result = new double[matrix[0].length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                result[j][i] = matrix[i][j];
            }
        }
        return result;
    }

    // The following methods are valid only on square matrices.

    /** Overwrite the input matrix argument (must be a square matrix)
     *  with its transpose.
     *  @param matrix A matrix of doubles.
     */
    public static final void transposeR(double[][] matrix) {
        for (int i = 1; i < matrix.length; i++) {
            for (int j = 0; j < i; j++) {
                double temp = matrix[i][j];
                matrix[i][j] = matrix[j][i];
                matrix[j][i] = temp;
            }
        }
    }

    /** Return a new matrix that is constructed by inverting the input
     *  matrix. If the input matrix is singular, null is returned.
     *  This method is from "C Language Algorithms for Digital Signal
     *  Processing" by Embree and Kimble.
     *  @param matrix A matrix of doubles
     *  @return A new matrix of doubles, or null if no inverse exists
     */
    public static final double[][] inverse(double[][] A) {
        // We should check that A is a square matrix here

        double[][] Ai = allocCopy(A);

        int n = A.length;
        // We depend on each of the elements being initialized to 0.0
        int[] pivot_flag = new int[n];
        int[] swap_col = new int[n];
        int[] swapRow = new int[n];

        int irow = 0, icol = 0;

        for (int i = 0; i < n; i++) { // n iterations of pivoting
            /* find the biggest pivot element */
            double big = 0.0;
            for (int row = 0; row < n; row++) {
                if (pivot_flag[row] == 0) {
                    for (int col = 0; col < n; col++) {
                        if (pivot_flag[col] == 0) {
                            double abs_element = Math.abs(Ai[row][col]); 
                            if (abs_element >= big) {
                                big = abs_element;
                                irow = row;
                                icol = col;
                            }
                        }
                    }
                }
            }
            pivot_flag[icol]++;

            // swap rows to make this diagonal the biggest absolute pivot
            if (irow != icol) {
                for (int col = 0; col < n; col++) {
                    double temp = Ai[irow][icol];
                    Ai[irow][col] = Ai[icol][col];
                    Ai[icol][col] = temp;
                }
            }

            // store what we swapped
            swapRow[i] = irow;
            swap_col[i] = icol;

            // if the pivot is zero, the matrix is singular
            if (Ai[icol][icol] == 0.0) {
               return null;
            }

            // divide the row by the pivot
            double pivot_inverse = 1.0 / A[icol][icol];
            Ai[icol][icol] = 1.0; // pivot = 1 to avoid round off
            for (int col = 0; col < n; col++) {
                Ai[icol][col] *= pivot_inverse;
            }

            // fix the other rows by subtracting
            for (int row = 0; row < n; row++) {
                if (row != icol) {
                   double temp = Ai[row][icol];
                   Ai[row][icol] = 0.0;
                   for (int col = 0; col < n; col++) {
                       Ai[row][col] -= Ai[icol][col] * temp;
                   }
                }
            }
        }

        // fix the effect of all the swaps for final answer
        for (int swap = n - 1; swap >= 0; swap--) {
            if (swapRow[swap] != swap_col[swap]) {
                for (int row = 0; row < n; row++) {
                    double temp = Ai[row][swapRow[swap]];
                    Ai[row][swapRow[swap]] = Ai[row][swap_col[swap]];
                    Ai[row][swap_col[swap]] = temp;
                }
            }
        }

        return Ai;
    }

    /** Return the determinate of a square matrix.
     *  This algorithm uses LU decomposition, and is taken from
     *  "C Language Algorithms for Digital Signal Processing" by
     *  Embree and Kimble.
     *  @param matrix A matrix of doubles.
     *  @retval The determinate of the matrix.
     */
    public static final double determinate(double[][] matrix) {
        // check that the matrix is square

        double[][] a = allocCopy(matrix);
        double det = 1.0;
        int n = matrix.length;

        for (int pivot = 0; pivot < n-1; pivot++) {
            // find the biggest absolute pivot
            double big = Math.abs(a[pivot][pivot]);
            int swapRow = 0; // initialize for no swap
            for (int row = pivot + 1; row < n; row++) {
                double abs_element = Math.abs(a[row][pivot]);
                if (abs_element > big) {
                   swapRow = row;
                   big = abs_element;
                }
            }

            // unless swapRow is still zero we must swap two rows
            if (swapRow != 0) {
               double[] a_ptr = a[pivot];
               a[pivot] = a[swapRow];
               a[swapRow] = a_ptr;

               // change sign of determinate because of swap
               det *= -a[pivot][pivot];
            } else {
               // calculate the determinate by the product of the pivots
               det *= a[pivot][pivot];
            }

            // if almost singular matrix, give up now

            // FIXME use epsilon instead of this ugly constant
            if (Math.abs(det) < 1.0e-50)
               return det;

            double pivot_inverse = 1.0 / a[pivot][pivot];
            for (int col = pivot + 1; col < n; col++) {
                a[pivot][col] *= pivot_inverse;
            }

            for (int row = pivot + 1; row < n; row++) {
                double temp = a[row][pivot];
                for (int col = pivot + 1; col < n; col++) {
                    a[row][col] -= a[pivot][col] * temp;
                }
            }
        }

        // last pivot, no reduction required
        det *= a[n-1][n-1];

        return det;
    }

    /** Convert the matrix into a string representation, which is to
     *  be readable by human beings.
     *  @param M A matrix of doubles
     */
    public static final String toString(double[][] M) {
        StringBuffer sb = new StringBuffer();
        sb.append('{');

        for (int i = 0; i < MatrixMath.rows(M); i++) {
            
            // Replace with ArrayMath.toString(M[i]) when it gets in line

            sb.append('{');
            for (int j = 0; j < MatrixMath.columns(M); j++) {
               sb.append(Double.toString(M[i][j]));
               
               if (j < (MatrixMath.columns(M) - 1)) {
                  sb.append(',');
               }
            }
            
            sb.append('}'); 

            if (i < (MatrixMath.rows(M) - 1)) {
               sb.append(',');
            }
        }

        sb.append('}');     

        return new String(sb);
    }
}





















