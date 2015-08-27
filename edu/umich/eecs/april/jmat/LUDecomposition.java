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

/** LU Decomposition
    <P>
    For an m-by-n matrix A with m >= n, the LU decomposition is an m-by-n
    unit lower triangular matrix L, an n-by-n upper triangular matrix U,
    and a permutation vector piv of length m so that A(piv,:) = L*U.
    If m < n, then L is m-by-m and U is m-by-n.
    <P>
    The LU decompostion with pivoting always exists, even if the matrix is
    singular, so the constructor will never fail.  The primary use of the
    LU decomposition is in the solution of square systems of simultaneous
    linear equations.  This will fail if isNonsingular() returns false.
 **/
public class LUDecomposition {
    Matrix LU;

    int pivsign;
    int piv[];

    public LUDecomposition(Matrix A) {
        this(A, true);
    }

    public LUDecomposition(Matrix A, boolean autoPivot) {
        LU = A.copy();
        int m = LU.m, n = LU.n;

        pivsign = 1;
        piv = new int[m];
        for (int i = 0; i < m; i++)
            piv[i] = i;

        // Outer loop.
        for (int j = 0; j < n; j++) {

            if (n > 1000)
                System.out.printf("%d / %d\r", j, n);

            // Make a copy of the j-th column to localize references.
            Vec LUcolj = LU.getColumn(j);

            // Apply previous transformations.
            for (int i = 0; i < m; i++) {
                Vec LUrowi = LU.getRow(i);

                // Most of the time is spent in the following dot product.

                int kmax = Math.min(i, j);
                double s = LUrowi.dotProduct(LUcolj, 0, kmax - 1);

                LUcolj.plusEquals(i, -s);
                LUrowi.plusEquals(j, -s);
            }

            // Find pivot and exchange if necessary.
            int p = j;
            if (autoPivot) {
                for (int i = j + 1; i < m; i++) {
                    if (Math.abs(LUcolj.get(i)) > Math.abs(LUcolj.get(p))) {
                        p = i;
                    }
                }
            }

            if (p != j) {
                LU.swapRows(p, j);
                int k = piv[p];
                piv[p] = piv[j];
                piv[j] = k;
                pivsign = -pivsign;
            }

            // Compute multipliers.
            if (j < n && j < m && LU.get(j, j) != 0.0) {
                double LUjj = LU.get(j, j);
                for (int i = j + 1; i < m; i++) {
                    LU.timesEquals(i, j, 1.0 / LUjj);
                }
            }
        }
    }

    /*
      public LUDecomposition(Matrix A, boolean autoPivot)
      {
      LU = A.copy();
      int m = LU.m, n = LU.n;

      pivsign = 1;
      piv = new int[m];
      for (int i = 0; i < m; i++)
      piv[i] = i;

      // Main loop.
      for (int k = 0; k < n; k++) {

      if (n > 1000)
      System.out.printf("%d / %d\r", k, n);

      // Find pivot.
      int p = k;
      if (autoPivot) {
      for (int i = k+1; i < m; i++) {
      if (Math.abs(LU.get(i,k)) > Math.abs(LU.get(p,k))) {
      p = i;
      }
      }
      }

      // Exchange if necessary.
      if (p != k) {
      // swap rows p and k.
      LU.swapRows(p,k);
      int t = piv[p]; piv[p] = piv[k]; piv[k] = t;
      pivsign = -pivsign;
      }

      // Compute multipliers and eliminate k-th column.
      if (k<n && LU.get(k,k) != 0.0) {

      double scale = 1.0 / LU.get(k,k);

      Vec Lrowk = LU.getRow(k);

      for (int i = k+1; i < m; i++) {
      Vec Lrowi = LU.getRow(i);
      Lrowi.set(k, Lrowi.get(k) * scale);

      double Lik = Lrowi.get(k);

      Lrowk.addTo(Lrowi, -Lik, k+1, n-1);
      }
      }
      }
      }
     */

    public int[] getPermutation() {
        return piv;
    }

    public Matrix getLU() {
        return LU;
    }

    public Matrix getL() {
        int m = LU.m, n = LU.n;
        Matrix L = new Matrix(m, n, LU.getOptions());

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (i > j) {
                    L.set(i, j, LU.get(i, j));
                } else if (i == j) {
                    L.set(i, j, 1.0);
                }
            }
        }

        return L;
    }

    public Matrix getU() {
        int m = LU.m, n = LU.n;
        Matrix U = new Matrix(n, n, LU.getOptions());

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (i <= j) {
                    U.set(i, j, LU.get(i, j));
                }
            }
        }
        return U;
    }

    public double det() {
        if (LU.m != LU.n)
            throw new IllegalArgumentException("Matrix must be square.");

        double d = pivsign;
        for (int j = 0; j < LU.n; j++)
            d *= LU.get(j, j);

        return d;
    }

    public double logDet() {
        if (LU.m != LU.n)
            throw new IllegalArgumentException("Matrix must be square.");

        double sign = pivsign;
        double logD = 0;
        for (int j = 0; j < LU.n; j++) {
            final double v = LU.get(j, j);
            sign *= Math.signum(v);
            logD += Math.log(Math.abs(v));
        }

        if (sign <= 0)
            throw new IllegalArgumentException("logDet of a negative matrix.");

        return logD;
    }

    public boolean isSingular() {
        int n = LU.n;
        for (int j = 0; j < n; j++) {
            if (LU.get(j, j) == 0)
                return true;
        }

        return false;
    }

    public Matrix solve(Matrix B) {
        int m = LU.m, n = LU.n;
        if (B.m != m)
            throw new IllegalArgumentException(
                    "Matrix row dimensions must agree.");

        if (isSingular())
            throw new RuntimeException("Matrix is singular");

        // Copy right hand side with pivoting
        Matrix X = B.copy();
        X.permuteRows(piv);
        // Solve L*Y = B(piv,:)
        for (int k = 0; k < n; k++) {
            Vec Xrowk = X.getRow(k);

            for (int i = k + 1; i < n; i++) {
                Vec Xrowi = X.getRow(i);
                Xrowk.addTo(Xrowi, -LU.get(i, k));
            }
        }

        // Solve U*X = Y;
        for (int k = n - 1; k >= 0; k--) {
            Vec Xrowk = X.getRow(k);
            Xrowk.timesEquals(1.0 / LU.get(k, k));

            for (int i = 0; i < k; i++) {
                Vec Xrowi = X.getRow(i);
                Xrowk.addTo(Xrowi, -LU.get(i, k));
            }
        }

        return X;
    }

    public static void main(String args[]) {
        java.util.Random r = new java.util.Random();

        System.out.println("Testing factors");
        for (int iters = 0; iters < 1000; iters++) {
            Matrix A = Matrix.random(r.nextInt(3) + 3, r.nextInt(3) + 3);

            LUDecomposition lud = new LUDecomposition(A);

            Matrix A2 = lud.getL().times(lud.getU());
            A2.inversePermuteRows(lud.getPermutation());

            assert (A.equals(A2));
            System.out.println("ok " + iters);
        }

        System.out.println("Testing Solve");
        for (int iters = 0; iters < 1000; iters++) {
            Matrix A = Matrix.random(10, 10);
            Matrix X = Matrix.random(10, 1);
            Matrix B = A.times(X);

            LUDecomposition lud = new LUDecomposition(A);

            Matrix X2 = lud.solve(B);
            Matrix B2 = A.times(X2);

            assert (X.equals(X2) && B.equals(B2));
            System.out.println("ok " + iters);
        }
    }
}
