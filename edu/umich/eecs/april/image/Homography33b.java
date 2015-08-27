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

package edu.umich.eecs.april.image;

import java.util.ArrayList;

import edu.umich.eecs.april.jmat.LinAlg;
import edu.umich.eecs.april.jmat.Matrix;
import edu.umich.eecs.april.jmat.SingularValueDecomposition;
import edu.umich.eecs.april.util.Normalize2D;

/** Compute 3x3 homography using Direct Linear Transform

    y = Hx (y = image coordinates in homogeneous coordinates, H = 3x3
    homography matrix, x = homogeneous 2D world coordinates)

    For each point correspondence, constrain y x Hx = 0 (where x is
    cross product). This means that they have the same direction, and
    ignores scale factor.

    We rewrite this as Ah = 0, where h is the 9x1 column vector of the
    elements of H. Each point correspondence gives us 3 equations of
    rank 2. The solution h is the minimum right eigenvector of A,
    which we can obtain via SVD, USV' = A. h is the right-most column
    of V'.

    We will actually maintain A'A internally, which is 9x9 regardless
    of how many correspondences we're given, and has the same
    eigenvectors as A.
 **/
public class Homography33b {
    double A[][] = new double[9][9];
    double H[][]; // homography, once computed.

    // worldx, worldy, imagex, imagey
    public ArrayList<double[]> correspondences = new ArrayList<double[]>();

    Normalize2D normWorld = new Normalize2D();
    Normalize2D normImage = new Normalize2D();

    public Homography33b() {
    }

    public double[][] getH() {
        compute();
        return H;
    }

    public void addCorrespondences(double worldxy[][], double imagexy[][]) {
        for (int i = 0; i < worldxy.length; i++) {
            addCorrespondence(worldxy[i][0], worldxy[i][1], imagexy[i][0],
                    imagexy[i][1]);
        }
    }

    public void addCorrespondence(double worldx, double worldy, double imagex,
            double imagey) {
        correspondences.add(new double[] { worldx, worldy, imagex, imagey });
        normWorld.add(worldx, worldy);
        normImage.add(imagex, imagey);
        H = null;
    }

    double[][] compute() {
        if (H != null) // already computed?
            return H;

        double normWorldT[][] = normWorld.getTransform();
        double normImageT[][] = normWorld.getTransform();

        // Would it be better to compute the Nx9 matrix and compute
        // the economy SVD of that? We'd have a smaller condition
        // number, but a slower SVD.
        for (double corr[] : correspondences) {

            // XXX this is wasteful. We could inline the
            // multiplications, exploit the special form of the
            // matrix.
            double worldxy[] = LinAlg.matrixAB(normWorldT, new double[] {
                    corr[0], corr[1], 1 });
            double imagexy[] = LinAlg.matrixAB(normImageT, new double[] {
                    corr[2], corr[3], 1 });
            double worldx = worldxy[0];
            double worldy = worldxy[1];
            double imagex = imagexy[0];
            double imagey = imagexy[1];

            // only update upper-right. A'A is symmetric, we'll finish the lower left later.
            double a03 = -worldx;
            double a04 = -worldy;
            double a05 = -1;
            double a06 = worldx * imagey;
            double a07 = worldy * imagey;
            double a08 = imagey;

            A[3][3] += a03 * a03;
            A[3][4] += a03 * a04;
            A[3][5] += a03 * a05;
            A[3][6] += a03 * a06;
            A[3][7] += a03 * a07;
            A[3][8] += a03 * a08;
            A[4][4] += a04 * a04;
            A[4][5] += a04 * a05;
            A[4][6] += a04 * a06;
            A[4][7] += a04 * a07;
            A[4][8] += a04 * a08;
            A[5][5] += a05 * a05;
            A[5][6] += a05 * a06;
            A[5][7] += a05 * a07;
            A[5][8] += a05 * a08;
            A[6][6] += a06 * a06;
            A[6][7] += a06 * a07;
            A[6][8] += a06 * a08;
            A[7][7] += a07 * a07;
            A[7][8] += a07 * a08;
            A[8][8] += a08 * a08;

            double a10 = worldx;
            double a11 = worldy;
            double a12 = 1;
            double a16 = -worldx * imagex;
            double a17 = -worldy * imagex;
            double a18 = -imagex;

            A[0][0] += a10 * a10;
            A[0][1] += a10 * a11;
            A[0][2] += a10 * a12;
            A[0][6] += a10 * a16;
            A[0][7] += a10 * a17;
            A[0][8] += a10 * a18;
            A[1][1] += a11 * a11;
            A[1][2] += a11 * a12;
            A[1][6] += a11 * a16;
            A[1][7] += a11 * a17;
            A[1][8] += a11 * a18;
            A[2][2] += a12 * a12;
            A[2][6] += a12 * a16;
            A[2][7] += a12 * a17;
            A[2][8] += a12 * a18;
            A[6][6] += a16 * a16;
            A[6][7] += a16 * a17;
            A[6][8] += a16 * a18;
            A[7][7] += a17 * a17;
            A[7][8] += a17 * a18;
            A[8][8] += a18 * a18;

            double a20 = -worldx * imagey;
            double a21 = -worldy * imagey;
            double a22 = -imagey;
            double a23 = worldx * imagex;
            double a24 = worldy * imagex;
            double a25 = imagex;

            A[0][0] += a20 * a20;
            A[0][1] += a20 * a21;
            A[0][2] += a20 * a22;
            A[0][3] += a20 * a23;
            A[0][4] += a20 * a24;
            A[0][5] += a20 * a25;
            A[1][1] += a21 * a21;
            A[1][2] += a21 * a22;
            A[1][3] += a21 * a23;
            A[1][4] += a21 * a24;
            A[1][5] += a21 * a25;
            A[2][2] += a22 * a22;
            A[2][3] += a22 * a23;
            A[2][4] += a22 * a24;
            A[2][5] += a22 * a25;
            A[3][3] += a23 * a23;
            A[3][4] += a23 * a24;
            A[3][5] += a23 * a25;
            A[4][4] += a24 * a24;
            A[4][5] += a24 * a25;
            A[5][5] += a25 * a25;
        }

        // make symmetric
        for (int i = 0; i < A.length; i++)
            for (int j = i + 1; j < A[0].length; j++)
                A[j][i] = A[i][j];

        H = new double[3][3];

        // compute using singular value decomposition
        SingularValueDecomposition svd = new SingularValueDecomposition(
                new Matrix(A));

        Matrix V = svd.getV();
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                H[i][j] = V.get(i * 3 + j, V.getColumnDimension() - 1);

        H = LinAlg.matrixABC(LinAlg.inverse(normImageT), H, normWorldT);

        if (false) {
            // test the homography
            for (double corr[] : correspondences) {
                double ixy[] = project(corr[0], corr[1]);

                System.out.printf("%15f %15f : %15f %15f => %15f %15f\n",
                        corr[0], corr[1], corr[2], corr[3], ixy[0], ixy[1]);
            }
        }

        return H;
    }

    public double[] project(double worldx, double worldy) {
        compute();

        double ixy[] = new double[2];
        ixy[0] = H[0][0] * worldx + H[0][1] * worldy + H[0][2];
        ixy[1] = H[1][0] * worldx + H[1][1] * worldy + H[1][2];
        double z = H[2][0] * worldx + H[2][1] * worldy + H[2][2];
        ixy[0] = ixy[0] / z;
        ixy[1] = ixy[1] / z;
        return ixy;
    }

    public static void main(String args[]) {
        // z is implicitly 0
        double worldxy[][] = new double[][] { { 0, 0 }, { 6, 0 }, { 6, 6 },
                { 0, 6 } };

        double imagexy[][] = new double[][] { { 0, 5 }, { 100, 10 },
                { 130, 110 }, { 20, 90 } };

        Homography33b homography = new Homography33b();
        homography.addCorrespondences(worldxy, imagexy);
        double h[][] = homography.compute();
        LinAlg.print(h);

        for (int j = 0; j < worldxy.length; j++) {
            System.out.printf("\n%15f %15f -> %15f %15f\n", worldxy[j][0],
                    worldxy[j][1], imagexy[j][0], imagexy[j][1]);
            double xyw[] = homography.project(worldxy[j][0], worldxy[j][1]);
            System.out.printf("                                -> %15f %15f\n",
                    xyw[0], xyw[1]);
        }
    }
}
