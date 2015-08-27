/*
Copyright (c) 2013 by the Regents of the University of Michigan.
Developed by the APRIL robotics lab under the direction of Edwin Olson (ebolson@umich.edu).

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the
   distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation
are those of the authors and should not be interpreted as representing
official policies, either expressed or implied, of the FreeBSD
Project.

This implementation of the AprilTags detector is provided for
convenience as a demonstration.  It is an older version implemented in
Java that has been supplanted by a much better performing C version.
If your application demands better performance, you will need to
replace this implementation with the newer C version and using JNI or
JNA to interface the C version to Java.

For details about the C version, see
https://april.eecs.umich.edu/wiki/index.php/AprilTags-C

 */

package edu.umich.eecs.april.jmat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/** Matrix class supporting dense and sparse matrices. **/
public class Matrix {
    static final public int DENSE = 0, SPARSE = 1;

    int m, n;
    int options;

    Vec rows[];
    static Random rand = new Random();

    final Vec makeVec(int length) {
        if ((options & SPARSE) == 0)
            return new DenseVec(length);

        return new CSRVec(length);
        //	return new HashVec(length);
    }

    ////////////////////////////////////////////
    // Constructors
    public Matrix(double v) {
        this(new double[][] { { v } });
    }

    public Matrix(int m, int n) {
        this(m, n, 0);
    }

    public Matrix(int m, int n, int options) {
        this.options = options;
        this.m = m;
        this.n = n;

        rows = new Vec[m];
        for (int i = 0; i < m; i++)
            rows[i] = makeVec(n);
    }

    public Matrix(double A[][]) {
        m = A.length;
        n = A[0].length;

        rows = new Vec[m];
        for (int i = 0; i < m; i++)
            rows[i] = new DenseVec(A[i]);
    }

    // create a matrix whose diagonal elements are given
    public static Matrix diag(double v[]) {
        Matrix X = new Matrix(v.length, v.length);
        for (int i = 0; i < v.length; i++)
            X.set(i, i, v[i]);
        return X;
    }

    public static Matrix rowMatrix(double v[]) {
        return new Matrix(new double[][] { v });
    }

    public static Matrix columnMatrix(double v[]) {
        Matrix M = new Matrix(v.length, 1);
        for (int i = 0; i < v.length; i++)
            M.set(i, 0, v[i]);
        return M;
    }

    public static Matrix outerProduct(double x[], double y[]) {
        int m = x.length;
        int n = y.length;

        Matrix X = new Matrix(m, n);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                X.set(i, j, x[i] * y[j]);
            }
        }

        return X;
    }

    private Matrix() { // used by copy
    }

    // If this matrix is already of the correct type, returns
    // itself. Otherwise, converts the matrix and returns the newly
    // copied matrix.
    public Matrix coerceOption(int options) {
        if (this.options == options)
            return this;

        Matrix X = new Matrix();
        X.m = m;
        X.n = n;
        X.options = options;

        X.rows = new Vec[m];
        for (int i = 0; i < m; i++) {
            X.rows[i] = X.makeVec(n);
            rows[i].addTo(X.rows[i], 1.0);
        }

        return X;
    }

    public Matrix copy() {
        Matrix X = new Matrix();
        X.m = m;
        X.n = n;
        X.options = options;

        X.rows = new Vec[m];
        for (int i = 0; i < m; i++)
            X.rows[i] = rows[i].copy();

        return X;
    }

    public double[][] copyArray() {
        double A[][] = new double[m][];
        for (int i = 0; i < m; i++)
            A[i] = rows[i].copyArray();

        return A;
    }

    public void copyToArray(double A[][]) {
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                A[i][j] = get(i, j);
    }

    /** Copy the submatrix beginning at (i0,j0) with m rows and n columns **/
    public double[][] copyArray(int i0, int j0, int tm, int tn) {
        double A[][] = new double[tm][tn];
        for (int i = 0; i < tm; i++)
            for (int j = 0; j < tn; j++)
                A[i][j] = get(i0 + i, j0 + j);
        return A;
    }

    /** Copy the submatrix beginning at (i0,j0) with m rows and n columns **/
    public void copyArray(int i0, int j0, int tm, int tn, double A[][]) {
        for (int i = 0; i < tm; i++)
            for (int j = 0; j < tn; j++)
                A[i][j] = get(i0 + i, j0 + j);
    }

    public double[] copyAsVector() {
        return getColumnPackedCopy();
    }

    public Matrix copy(int row0, int row1, int col0, int col1) {
        Matrix X = new Matrix();
        X.m = row1 - row0 + 1;
        X.n = col1 - col0 + 1;
        X.options = options;

        X.rows = new Vec[X.m];
        for (int i = 0; i < X.m; i++)
            X.rows[i] = rows[i + row0].copy(col0, col1);

        return X;
    }

    public static Matrix identity(int m, int n) {
        Matrix M = new Matrix(m, n);
        for (int i = 0; i < Math.min(m, n); i++)
            M.set(i, i, 1);

        return M;
    }

    public static Matrix random(int m, int n) {
        Matrix M = new Matrix(m, n);
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++) {
                M.set(i, j, rand.nextDouble());
            }

        return M;
    }

    ////////////////////////////////////////////
    // Basic Accessors
    public int getRowDimension() {
        return m;
    }

    public int getColumnDimension() {
        return n;
    }

    public int getOptions() {
        return options;
    }

    public double get(int pos) {
        int row = pos / n;
        int col = pos - row * n;

        return get(row, col);
    }

    public void set(int pos, double v) {
        int row = pos / n;
        int col = pos - row * n;

        set(row, col, v);
    }

    public double get(int row, int col) {
        Vec r = rows[row];
        return r.get(col);
    }

    public void set(int row, int col, double v) {
        Vec r = rows[row];
        r.set(col, v);
    }

    public void set(int row, int col, double v[][]) {
        for (int dr = 0; dr < v.length; dr++)
            for (int dc = 0; dc < v[dr].length; dc++)
                set(row + dr, col + dc, v[dr][dc]);
    }

    /** Create a new Vec containing a copy of the column. Changes to
        the Vec do NOT affect the Matrix. **/
    public Vec getColumn(int col) {
        Vec res = makeVec(m);

        for (int i = 0; i < m; i++) {
            Vec r = rows[i];
            res.set(i, r.get(col));
        }

        return res;
    }

    /** The Vec returned is a LIVE view of the matrix. Changes to it
        affect the matrix. (This is NOT the case for getColumn). **/
    public Vec getRow(int row) {
        return rows[row];
    }

    /** Use with extreme caution. **/
    public void setRow(int row, Vec v) {
        assert (v.size() == n);
        rows[row] = v;
    }

    public boolean isSparse() {
        return (options & SPARSE) != 0;
    }

    public int getNz() {
        int nz = 0;
        for (int i = 0; i < m; i++) {
            nz += rows[i].getNz();
        }
        return nz;
    }

    public double getNzFrac() {
        double mn = ((double) m) * ((double) n);
        return (getNz()) / mn;
    }

    ////////////////////////////////////////////
    // Operations
    public void clear() {
        for (int i = 0; i < m; i++)
            rows[i].clear();
    }

    public boolean equals(Matrix M) {
        return equals(M, 0.000001);
    }

    public boolean equals(Matrix M, double eps) {
        if (m != M.m || n != M.n)
            return false;

        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                if (Math.abs(M.get(i, j) - get(i, j)) > eps)
                    return false;
        return true;
    }

    public void print() {
        System.out.print(toString());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < m; i++) {
            sb.append("[");
            for (int j = 0; j < n; j++) {
                sb.append(String.format("%15f ", get(i, j)));
            }
            sb.append(" ]\n");
        }
        sb.append("\n");

        return sb.toString();
    }

    public Matrix times(double v) {
        Matrix X = copy();
        for (int i = 0; i < m; i++)
            X.rows[i].timesEquals(v);
        return X;
    }

    public Matrix times(Matrix B) {
        if (n != B.m)
            throw new IllegalArgumentException(
                    "Matrix inner dimensions must agree");

        int xm = m;
        int xn = B.n;

        Matrix X = null;
        if (isSparse() || B.isSparse())
            X = new Matrix(xm, xn, SPARSE);
        else
            X = new Matrix(xm, xn, DENSE);

        for (int col = 0; col < xn; col++) {
            Vec bcol = B.getColumn(col);

            for (int row = 0; row < xm; row++) {
                Vec arow = getRow(row);

                X.set(row, col, arow.dotProduct(bcol));
            }
        }

        return X;
    }

    // compute transpose(this)*B
    public double[] transposeTimes(double B[]) {
        double X[] = new double[n];

        for (int i = 0; i < rows.length; i++) {
            Vec row = getRow(i);

            if (row instanceof CSRVec) {
                CSRVec v = (CSRVec) row;
                for (int j = 0; j < v.nz; j++) {
                    int k = v.indices[j];
                    X[k] += v.values[j] * B[i];
                }
            } else {
                for (int j = 0; j < n; j++) {
                    X[j] += row.get(j) * B[i];
                }
            }
        }
        return X;
    }

    // compute this*BT'. This is often much faster than the ordinary
    // times() function since we don't have to process down columns.
    public Matrix timesTranspose(Matrix BT) {
        if (n != BT.n)
            throw new IllegalArgumentException(
                    "Matrix inner dimensions must agree");

        int xm = m;
        int xn = BT.m;

        Matrix X = null;
        if (isSparse() || BT.isSparse())
            X = new Matrix(xm, xn, SPARSE);
        else
            X = new Matrix(xm, xn, DENSE);

        for (int col = 0; col < xn; col++) {
            Vec bcol = BT.getRow(col);

            for (int row = 0; row < xm; row++) {
                Vec arow = getRow(row);

                X.set(row, col, arow.dotProduct(bcol));
            }
        }

        return X;

    }

    // times column vector
    public double[] times(double B[]) {
        if (n != B.length)
            throw new IllegalArgumentException(
                    "Matrix inner dimensions must agree");

        double v[] = new double[m];

        Vec col = new DenseVec(B);

        for (int i = 0; i < v.length; i++)
            v[i] = rows[i].dotProduct(col);

        return v;
    }

    public double det() {
        return new LUDecomposition(this).det();
    }

    public double logDet() {
        return new LUDecomposition(this).logDet();
    }

    public double[] getColumnPackedCopy() {
        double vals[] = new double[m * n];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                vals[i + j * m] = get(i, j);
        return vals;
    }

    /** Create a matrix from the column-packed vector. The matrix has
     * m rows and n columns. **/
    public static Matrix columnPackedMatrix(double v[], int m, int n) {
        assert (v.length == m * n);
        Matrix M = new Matrix(m, n);
        for (int c = 0; c < n; c++)
            for (int r = 0; r < m; r++)
                M.set(r, c, v[c * m + r]);
        return M;
    }

    public void timesEquals(double v) {
        for (Vec row : rows)
            row.timesEquals(v);
    }

    public void timesEquals(int i, int j, double v) {
        rows[i].timesEquals(v, j, j);
    }

    public Matrix plus(Matrix B) {
        if (m != B.m || n != B.n)
            throw new IllegalArgumentException("Matrix dimensions must agree");

        Matrix X = copy();
        for (int i = 0; i < m; i++)
            B.rows[i].addTo(X.rows[i], 1);
        return X;
    }

    public Matrix minus(Matrix B) {
        if (m != B.m || n != B.n)
            throw new IllegalArgumentException("Matrix dimensions must agree");

        Matrix X = copy();
        for (int i = 0; i < m; i++)
            B.rows[i].addTo(X.rows[i], -1);
        return X;
    }

    public void plusEquals(int i, int j, double v) {
        rows[i].plusEquals(j, v);
    }

    public void plusEquals(double v[][]) {
        plusEquals(0, 0, v);
    }

    public void plusEquals(int i0, int j0, double v[][]) {
        int nrows = v.length, ncols = v[0].length;

        for (int i = 0; i < nrows; i++) {
            for (int j = 0; j < ncols; j++) {
                rows[i0 + i].plusEquals(j0 + j, v[i][j]);
            }
        }
    }

    // Treat v as a column vector
    public void plusEqualsColumnVector(double v[]) {
        plusEqualsColumnVector(0, 0, v);
    }

    public void plusEqualsColumnVector(int i0, int j0, double v[]) {
        for (int i = 0; i < v.length; i++)
            rows[i0 + i].plusEquals(j0, v[i]);
    }

    public void plusEquals(Matrix M) {
        plusEquals(0, 0, M);
    }

    public void minusEquals(int i0, int j0, double v[][]) {
        int nrows = v.length, ncols = v[0].length;

        for (int i = 0; i < nrows; i++) {
            for (int j = 0; j < ncols; j++) {
                rows[i0 + i].plusEquals(j0 + j, -v[i][j]);
            }
        }
    }

    public void minusEquals(int i0, int j0, Matrix M) {
        for (int i = 0; i < M.m; i++) {
            for (int j = 0; j < M.n; j++) {
                rows[i + i0].plusEquals(j0 + j, -M.get(i, j));
            }
        }
    }

    public void minusEquals(Matrix M) {
        minusEquals(0, 0, M);
    }

    // Note: not optimized for sparse matrices M
    public void plusEquals(int i0, int j0, Matrix M) {
        for (int i = 0; i < M.m; i++) {
            for (int j = 0; j < M.n; j++) {
                rows[i + i0].plusEquals(j0 + j, M.get(i, j));
            }
        }
    }

    public double trace() {
        double acc = 0;

        for (int i = 0; i < Math.min(m, n); i++)
            acc += get(i, i);

        return acc;
    }

    public double normF() {
        double acc = 0;
        for (int i = 0; i < m; i++) {
            acc += rows[i].normF();
        }

        return acc;
    }

    public Matrix solve(Matrix B) {
        assert (m == n);

        LUDecomposition lu = new LUDecomposition(this);
        return lu.solve(B);
    }

    public void swapRows(int a, int b) {
        Vec t = rows[a];
        rows[a] = rows[b];
        rows[b] = t;
    }

    // Reorder the rows so that they are in the order given by pivot,
    // e.g., B[i,:]=A[pivot[i],:]
    public void permuteRows(int perm[]) {
        Vec newrows[] = new Vec[m];
        for (int i = 0; i < perm.length; i++)
            newrows[i] = rows[perm[i]];
        rows = newrows;
    }

    public Matrix copyPermuteColumns(int perm[]) {
        Permutation p = new Permutation(perm);

        Vec newrows[] = new Vec[m];

        for (int i = 0; i < m; i++)
            newrows[i] = rows[i].copyPermuteColumns(p);

        Matrix X = new Matrix();
        X.m = m;
        X.n = n;
        X.options = options;
        X.rows = newrows;

        return X;
    }

    // P'AP
    public Matrix copyPermuteRowsAndColumns(int perm[]) {
        Permutation p = new Permutation(perm);

        assert (m == n);
        Matrix X = new Matrix();
        X.m = m;
        X.n = n;
        X.options = options;
        X.rows = new Vec[m];

        for (int i = 0; i < perm.length; i++)
            X.rows[i] = getRow(perm[i]).copyPermuteColumns(p);

        return X;
    }

    // Reorder the rows so that they are in
    // e.g., B[pivot[i],:]=A[i,:]
    public void inversePermuteRows(int pivot[]) {
        Vec newrows[] = new Vec[m];
        for (int i = 0; i < pivot.length; i++)
            newrows[pivot[i]] = rows[i];
        rows = newrows;
    }

    public Matrix transpose() {
        Matrix X = new Matrix(n, m, options);
        for (int i = 0; i < m; i++) {
            Vec row = rows[i];
            row.transposeAsColumn(X, i);
        }

        return X;
    }

    // Return the transpose of the upper right triangular part of this matrix.
    public Matrix upperRightTranspose() {
        assert (m == n);
        Matrix X = new Matrix(n, n, options);
        for (int i = 0; i < n; i++) {
            Vec row = getRow(i);
            row.transposeAsColumn(X, i, i, n - 1);
        }

        return X;
    }

    public Matrix upperRight() {
        assert (m == n);
        Matrix X = new Matrix();
        X.m = m;
        X.n = n;
        X.options = options;

        X.rows = new Vec[m];
        for (int i = 0; i < m; i++)
            X.rows[i] = rows[i].copyPart(i, n - 1);

        return X;
    }

    public Matrix inverse() {
        if (m == 2 && n == 2) {
            double a = get(0, 0), b = get(0, 1);
            double c = get(1, 0), d = get(1, 1);

            double det = 1 / (a * d - b * c);

            Matrix X = new Matrix(2, 2);
            X.set(0, 0, det * d);
            X.set(0, 1, -det * b);
            X.set(1, 0, -det * c);
            X.set(1, 1, det * a);

            return X;
        }

        if (m == 3 && n == 3) {
            double a = get(0, 0), b = get(0, 1), c = get(0, 2);
            double d = get(1, 0), e = get(1, 1), f = get(1, 2);
            double g = get(2, 0), h = get(2, 1), i = get(2, 2);

            double det = 1 / (a * e * i - a * f * h - d * b * i + d * c * h + g
                    * b * f - g * c * e);

            Matrix X = new Matrix(3, 3);

            X.set(0, 0, det * (e * i - f * h));
            X.set(0, 1, det * (-b * i + c * h));
            X.set(0, 2, det * (b * f - c * e));
            X.set(1, 0, det * (-d * i + f * g));
            X.set(1, 1, det * (a * i - c * g));
            X.set(1, 2, det * (-a * f + c * d));
            X.set(2, 0, det * (d * h - e * g));
            X.set(2, 1, det * (-a * h + b * g));
            X.set(2, 2, det * (a * e - b * d));

            return X;
        }

        return solve(identity(m, m));
    }

    /** Truncate or add zeros as appropriate. **/
    public void resize(int newrows, int newcols) {
        // this could be inefficient if we're adding many rows.
        // alternative: allow rows.length to be oversized (and make
        // sure the code only touches the first m of them.) Then
        // we only have to reallocate the rows vector infrequently.

        ArrayList<Vec> nrows = new ArrayList<Vec>();

        for (int ridx = 0; ridx < Math.min(m, newrows); ridx++) {
            Vec v = rows[ridx];
            v.resize(newcols);
            nrows.add(v);
        }

        while (nrows.size() < newrows)
            nrows.add(makeVec(newcols));

        m = newrows;
        n = newcols;

        rows = nrows.toArray(new Vec[m]);
    }

    /** Writes matrix data in a format compatible with the read() method. **/
    public void write(BufferedWriter outs) throws IOException {
        outs.write(m + "\n");
        outs.write(n + "\n");
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                outs.write(get(i, j) + "\n");

    }

    /** Create a new matrix whose dimensions are given on the first
     * two lines (rows on the first line, cols on the second line),
     * followed by each element of the matrix on a line by itself (row
     * major ordering).
     **/
    public static Matrix read(BufferedReader ins) throws IOException {
        int m = Integer.parseInt(ins.readLine());
        int n = Integer.parseInt(ins.readLine());

        Matrix A = new Matrix(m, n);

        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                A.set(i, j, Double.parseDouble(ins.readLine()));

        return A;
    }

    /** Writes the matrix in a form that can be easily executed by
     * matlab in a way that is efficient for sparse matrices.
     **/
    public void writeMatlab(BufferedWriter outs, String matname)
            throws IOException {
        outs.write("rows_=[\n");
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                if (get(i, j) != 0)
                    outs.write("" + (i + 1) + "\n");
        outs.write("];\n");

        outs.write("cols_=[\n");
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                if (get(i, j) != 0)
                    outs.write("" + (j + 1) + "\n");
        outs.write("];\n");

        outs.write("vals_=[\n");
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                if (get(i, j) != 0)
                    outs.write("" + get(i, j) + "\n");
        outs.write("];\n");

        outs.write("" + matname + " = sparse(rows_,cols_,vals_," + m + "," + n
                + ");\n\n");
        outs.flush();
    }
}
