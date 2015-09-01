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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Simple linear algebra and geometry functions designed to operate
 * on doubles[].
 *
 * Conventions: We assume points are projected via right multiplication. E.g.,
 * p' = Mp.
 *
 * Roll: rotation around X. Pitch: rotation around Y. Yaw: rotation around Z.
 *
 * Roll Pitch Yaw are evaluated in the order: roll, pitch, then yaw. I.e.,
 *   rollPitchYawToMatrix(rpy) = rotateZ(rpy[2]) * rotateY(rpy[1]) * rotateX(rpy[0])
 **/
public final class LinAlg {
    /** Returns manhatten distance. **/
    public static double manhattenDistance(double a[], double b[]) {
        assert (a.length == b.length);

        double dist = 0;
        for (int i = 0; i < a.length; i++)
            dist += Math.abs(b[i] - a[i]);

        return dist;
    }

    /** Returns the square of v **/
    public static final double sq(double v) {
        return v * v;
    }

    public static final double[] resize(double v[], int newlength) {
        double r[] = new double[newlength];
        for (int i = 0; i < Math.min(newlength, v.length); i++)
            r[i] = v[i];
        return r;
    }

    /** Squared Euclidean distance **/
    public static int squaredDistance(int a[], int b[]) {
        assert (a.length == b.length);
        return squaredDistance(a, b, a.length);
    }

    /** Squared Euclidean distance using first 'len' elements **/
    public static int squaredDistance(int a[], int b[], int len) {
        int mag = 0;

        for (int i = 0; i < len; i++)
            mag += sq(b[i] - a[i]);

        return mag;
    }

    public static double distance(int a[], int b[]) {
        return Math.sqrt(squaredDistance(a, b));
    }

    /** Squared Euclidean distance **/
    public static double squaredDistance(double a[], double b[]) {
        assert (a.length == b.length);
        return squaredDistance(a, b, a.length);
    }

    /** Squared Euclidean distance using first 'len' elements **/
    public static double squaredDistance(double a[], double b[], int len) {
        double mag = 0;

        for (int i = 0; i < len; i++)
            mag += sq(b[i] - a[i]);

        return mag;
    }

    /** Euclidean distance **/
    public static double distance(double a[], double b[]) {
        assert (a.length == b.length);
        return Math.sqrt(squaredDistance(a, b, a.length));
    }

    /** Euclidean distance using first 'len' elements **/
    public static double distance(double a[], double b[], int len) {
        return Math.sqrt(squaredDistance(a, b, len));
    }

    /** Return absolute value of each element. **/
    public static double[][] abs(double A[][]) {
        if (A.length == 0)
            return new double[0][0];

        double B[][] = new double[A.length][A[0].length];
        for (int i = 0; i < A.length; i++)
            for (int j = 0; j < A[i].length; j++)
                B[i][j] = Math.abs(A[i][j]);
        return B;
    }

    /** Return absolute value of each element. **/
    public static double[] abs(double a[]) {
        double b[] = new double[a.length];
        for (int i = 0; i < a.length; i++)
            b[i] = Math.abs(a[i]);
        return b;
    }

    /** Return absolute value of each element. **/
    public static float[] abs(float a[]) {
        float b[] = new float[a.length];
        for (int i = 0; i < a.length; i++)
            b[i] = Math.abs(a[i]);
        return b;
    }

    /** sum of squared elements **/
    public static double normF(double a[]) {
        double mag = 0;

        for (int i = 0; i < a.length; i++)
            mag += sq(a[i]);

        return mag;
    }

    /** length of the vector **/
    public static double magnitude(int a[]) {
        double mag = 0;

        for (int i = 0; i < a.length; i++)
            mag += sq(a[i]);

        return Math.sqrt(mag);
    }

    /** length of the vector **/
    public static float magnitude(float a[]) {
        double mag = 0;

        for (int i = 0; i < a.length; i++)
            mag += sq(a[i]);

        return (float) Math.sqrt(mag);
    }

    /** length of the vector **/
    public static double magnitude(double a[]) {
        double mag = 0;

        for (int i = 0; i < a.length; i++)
            mag += sq(a[i]);

        return Math.sqrt(mag);
    }

    /** average of elements in a vector **/
    public static float average(float a[]) {
        float sum = 0;
        for (int i = 0; i < a.length; i++)
            sum += a[i];
        return sum / a.length;
    }

    /** average of elements in a vector **/
    public static double average(double a[]) {
        double sum = 0;
        for (int i = 0; i < a.length; i++)
            sum += a[i];
        return sum / a.length;
    }

    public static float normL1(float a[]) {
        float mag = 0;
        for (int i = 0; i < a.length; i++)
            mag += Math.abs(a[i]);
        return mag;
    }

    public static double normL1(double a[]) {
        double mag = 0;
        for (int i = 0; i < a.length; i++)
            mag += Math.abs(a[i]);
        return mag;
    }

    public static float[] normalizeL1(float a[]) {
        return scale(a, 1.0 / normL1(a));
    }

    public static double[] normalizeL1(double a[]) {
        return scale(a, 1.0 / normL1(a));
    }

    /** Rescale so magnitude is 1. **/
    public static float[] normalize(float a[]) {
        float b[] = new float[a.length];
        float mag = magnitude(a);

        for (int i = 0; i < a.length; i++)
            b[i] = a[i] / mag;

        return b;
    }

    public static double[] normalize(double a[]) {
        double b[] = new double[a.length];
        double mag = magnitude(a);

        for (int i = 0; i < a.length; i++)
            b[i] = a[i] / mag;

        return b;
    }

    public static void normalizeEquals(double a[]) {
        double mag = magnitude(a);

        for (int i = 0; i < a.length; i++)
            a[i] = a[i] / mag;
    }

    public static double[] add(double a[], double b[]) {
        return add(a, b, null);
    }

    public static double[] add(double a[], double b[], double r[]) {
        assert (a.length == b.length);
        if (r == null)
            r = new double[a.length];
        assert (r.length == a.length);

        for (int i = 0; i < a.length; i++)
            r[i] = a[i] + b[i];

        return r;
    }

    public static double[][] addMany(double a[][], double[][]... bs) {
        double X[][] = copy(a);

        for (double b[][] : bs)
            LinAlg.plusEquals(X, b);

        return X;
    }

    public static double[][] add(double a[][], double b[][]) {
        return add(a, b, null);
    }

    public static double[][] add(double a[][], double b[][], double X[][]) {
        assert (a.length == b.length);
        assert (a[0].length == b[0].length);

        if (X == null)
            X = new double[a.length][a[0].length];

        for (int i = 0; i < a.length; i++)
            for (int j = 0; j < a[0].length; j++)
                X[i][j] = a[i][j] + b[i][j];
        return X;
    }

    public static double[][] subtract(double a[][], double b[][]) {
        return subtract(a, b, null);
    }

    public static double[][] subtract(double a[][], double b[][], double X[][]) {
        assert (a.length == b.length);
        assert (a[0].length == b[0].length);

        if (X == null)
            X = new double[a.length][a[0].length];

        for (int i = 0; i < a.length; i++)
            for (int j = 0; j < a[0].length; j++)
                X[i][j] = a[i][j] - b[i][j];
        return X;
    }

    // optimized for 4x4 case.
    public static void timesEquals(double a[][], double b[][]) {
        assert (a[0].length == b.length);
        assert (a.length == b[0].length);

        if (a.length == 4 && a[0].length == 4) {
            double t0, t1, t2, t3;

            for (int i = 0; i < a.length; i++) {
                t0 = a[i][0];
                t1 = a[i][1];
                t2 = a[i][2];
                t3 = a[i][3];

                for (int j = 0; j < 4; j++)
                    a[i][j] = t0 * b[0][j] + t1 * b[1][j] + t2 * b[2][j] + t3
                            * b[3][j];
            }
            return;
        }

        // dumb version: multiply, then copy.
        double x[][] = matrixAB(a, b);
        for (int i = 0; i < x.length; i++)
            for (int j = 0; j < x[0].length; j++)
                a[i][j] = x[i][j];
    }

    public static void plusEquals(double a[][], double b[][]) {
        assert (a.length == b.length);
        assert (a[0].length == b[0].length);

        for (int i = 0; i < a.length; i++)
            for (int j = 0; j < a[0].length; j++)
                a[i][j] += b[i][j];
    }

    public static double[][] inverse(double A[][]) {
        return inverse(A, null);
    }

    public static double[][] inverse(double A[][], double X[][]) {
        if (X == null)
            X = new double[A.length][A.length];

        if (A.length == 0)
            return new double[0][0];

        assert (X.length == A.length && X[0].length == A[0].length);

        if (A.length == 1 && A[0].length == 1) {
            return new double[][] { { 1.0 / A[0][0] } };
        }

        if (A.length == 2 && A[0].length == 2) {
            double a = A[0][0], b = A[0][1];
            double c = A[1][0], d = A[1][1];

            double det = (a * d - b * c);
            if (det == 0)
                return null;

            det = 1.0 / det;
            X[0][0] = det * d;
            X[0][1] = -det * b;
            X[1][0] = -det * c;
            X[1][1] = det * a;

            return X;
        }

        if (A.length == 3 && A[0].length == 3) {
            double a = A[0][0], b = A[0][1], c = A[0][2];
            double d = A[1][0], e = A[1][1], f = A[1][2];
            double g = A[2][0], h = A[2][1], i = A[2][2];

            double det = (a * e * i - a * f * h - d * b * i + d * c * h + g * b
                    * f - g * c * e);
            if (det == 0)
                return null;
            det = 1.0 / det;

            X[0][0] = det * (e * i - f * h);
            X[0][1] = det * (-b * i + c * h);
            X[0][2] = det * (b * f - c * e);
            X[1][0] = det * (-d * i + f * g);
            X[1][1] = det * (a * i - c * g);
            X[1][2] = det * (-a * f + c * d);
            X[2][0] = det * (d * h - e * g);
            X[2][1] = det * (-a * h + b * g);
            X[2][2] = det * (a * e - b * d);

            return X;
        }

        if (A.length == 4 && A[0].length == 4) {
            double m00 = A[0][0], m01 = A[0][1], m02 = A[0][2], m03 = A[0][3];
            double m10 = A[1][0], m11 = A[1][1], m12 = A[1][2], m13 = A[1][3];
            double m20 = A[2][0], m21 = A[2][1], m22 = A[2][2], m23 = A[2][3];
            double m30 = A[3][0], m31 = A[3][1], m32 = A[3][2], m33 = A[3][3];

            double det = m00 * m11 * m22 * m33 - m00 * m11 * m23 * m32 - m00
                    * m21 * m12 * m33 + m00 * m21 * m13 * m32 + m00 * m31 * m12
                    * m23 - m00 * m31 * m13 * m22 - m10 * m01 * m22 * m33 + m10
                    * m01 * m23 * m32 + m10 * m21 * m02 * m33 - m10 * m21 * m03
                    * m32 - m10 * m31 * m02 * m23 + m10 * m31 * m03 * m22 + m20
                    * m01 * m12 * m33 - m20 * m01 * m13 * m32 - m20 * m11 * m02
                    * m33 + m20 * m11 * m03 * m32 + m20 * m31 * m02 * m13 - m20
                    * m31 * m03 * m12 - m30 * m01 * m12 * m23 + m30 * m01 * m13
                    * m22 + m30 * m11 * m02 * m23 - m30 * m11 * m03 * m22 - m30
                    * m21 * m02 * m13 + m30 * m21 * m03 * m12;

            if (det == 0)
                return null;

            X[0][0] = m11 * m22 * m33 - m11 * m23 * m32 - m21 * m12 * m33 + m21
                    * m13 * m32 + m31 * m12 * m23 - m31 * m13 * m22;
            X[1][0] = -m10 * m22 * m33 + m10 * m23 * m32 + m20 * m12 * m33
                    - m20 * m13 * m32 - m30 * m12 * m23 + m30 * m13 * m22;
            X[2][0] = m10 * m21 * m33 - m10 * m23 * m31 - m20 * m11 * m33 + m20
                    * m13 * m31 + m30 * m11 * m23 - m30 * m13 * m21;
            X[3][0] = -m10 * m21 * m32 + m10 * m22 * m31 + m20 * m11 * m32
                    - m20 * m12 * m31 - m30 * m11 * m22 + m30 * m12 * m21;
            X[0][1] = -m01 * m22 * m33 + m01 * m23 * m32 + m21 * m02 * m33
                    - m21 * m03 * m32 - m31 * m02 * m23 + m31 * m03 * m22;
            X[1][1] = m00 * m22 * m33 - m00 * m23 * m32 - m20 * m02 * m33 + m20
                    * m03 * m32 + m30 * m02 * m23 - m30 * m03 * m22;
            X[2][1] = -m00 * m21 * m33 + m00 * m23 * m31 + m20 * m01 * m33
                    - m20 * m03 * m31 - m30 * m01 * m23 + m30 * m03 * m21;
            X[3][1] = m00 * m21 * m32 - m00 * m22 * m31 - m20 * m01 * m32 + m20
                    * m02 * m31 + m30 * m01 * m22 - m30 * m02 * m21;
            X[0][2] = m01 * m12 * m33 - m01 * m13 * m32 - m11 * m02 * m33 + m11
                    * m03 * m32 + m31 * m02 * m13 - m31 * m03 * m12;
            X[1][2] = -m00 * m12 * m33 + m00 * m13 * m32 + m10 * m02 * m33
                    - m10 * m03 * m32 - m30 * m02 * m13 + m30 * m03 * m12;
            X[2][2] = m00 * m11 * m33 - m00 * m13 * m31 - m10 * m01 * m33 + m10
                    * m03 * m31 + m30 * m01 * m13 - m30 * m03 * m11;
            X[3][2] = -m00 * m11 * m32 + m00 * m12 * m31 + m10 * m01 * m32
                    - m10 * m02 * m31 - m30 * m01 * m12 + m30 * m02 * m11;
            X[0][3] = -m01 * m12 * m23 + m01 * m13 * m22 + m11 * m02 * m23
                    - m11 * m03 * m22 - m21 * m02 * m13 + m21 * m03 * m12;
            X[1][3] = m00 * m12 * m23 - m00 * m13 * m22 - m10 * m02 * m23 + m10
                    * m03 * m22 + m20 * m02 * m13 - m20 * m03 * m12;
            X[2][3] = -m00 * m11 * m23 + m00 * m13 * m21 + m10 * m01 * m23
                    - m10 * m03 * m21 - m20 * m01 * m13 + m20 * m03 * m11;
            X[3][3] = m00 * m11 * m22 - m00 * m12 * m21 - m10 * m01 * m22 + m10
                    * m02 * m21 + m20 * m01 * m12 - m20 * m02 * m11;

            for (int i = 0; i < 4; i++)
                for (int j = 0; j < 4; j++)
                    X[i][j] /= det;

            return X;
        }

        // use the generic (slow) method.
        Matrix M = new Matrix(A).inverse();
        M.copyToArray(X);

        return X;
    }

    public static double min(double a[][]) {
        double v = Double.MAX_VALUE;
        for (int i = 0; i < a.length; i++)
            for (int j = 0; j < a[0].length; j++)
                v = Math.min(v, a[i][j]);

        return v;
    }

    public static double max(double a[][]) {
        double v = -Double.MAX_VALUE;
        for (int i = 0; i < a.length; i++)
            for (int j = 0; j < a[0].length; j++)
                v = Math.max(v, a[i][j]);

        return v;
    }

    public static double[] max(double a[], double b[]) {
        double v[] = new double[a.length];
        for (int i = 0; i < v.length; i++)
            v[i] = Math.max(a[i], b[i]);
        return v;
    }

    /** a += b **/
    public static void plusEquals(double a[], double b[]) {
        assert (a.length == b.length);

        for (int i = 0; i < a.length; i++)
            a[i] += b[i];
    }

    /** a -= b **/
    public static void minusEquals(double a[], double b[]) {
        assert (a.length == b.length);

        for (int i = 0; i < a.length; i++)
            a[i] -= b[i];
    }

    // a -= b
    public static void minusEquals(double a[], double b[], double scale) {
        assert (a.length == b.length);

        for (int i = 0; i < a.length; i++)
            a[i] -= b[i] * scale;
    }

    // a += scale*b
    public static void plusEquals(double a[], double b[], double scale) {
        assert (a.length == b.length);

        for (int i = 0; i < a.length; i++)
            a[i] += b[i] * scale;
    }

    public static double[] subtract(double a[], double b[]) {
        return subtract(a, b, null);
    }

    public static double[] subtract(double a[], double b[], double r[]) {
        assert (a.length == b.length);
        if (r == null)
            r = new double[a.length];
        assert (r.length == a.length);

        for (int i = 0; i < a.length; i++)
            r[i] = a[i] - b[i];

        return r;
    }

    public static double[] crossProduct(double a[], double b[], double r[]) {
        if (r == null)
            r = new double[a.length];
        r[0] = a[1] * b[2] - a[2] * b[1];
        r[1] = a[2] * b[0] - a[0] * b[2];
        r[2] = a[0] * b[1] - a[1] * b[0];
        return r;
    }

    public static double[] crossProduct(double a[], double b[]) {
        return crossProduct(a, b, null);
    }

    public static float[] crossProduct(float a[], float b[], float r[]) {
        if (r == null)
            r = new float[a.length];
        r[0] = a[1] * b[2] - a[2] * b[1];
        r[1] = a[2] * b[0] - a[0] * b[2];
        r[2] = a[0] * b[1] - a[1] * b[0];
        return r;
    }

    public static float[] crossProduct(float a[], float b[]) {
        return crossProduct(a, b, null);
    }

    /** Consider the directed 2D line that travels from p0 to p1. is q
     * on the left (or on the line)?
     **/
    public static final boolean pointLeftOf(double p0[], double p1[],
            double q[]) {
        double a0 = p1[0] - p0[0];
        double a1 = p1[1] - p0[1];
        double b0 = q[0] - p0[0];
        double b1 = q[1] - p0[1];

        return (a0 * b1 - a1 * b0) >= 0;
    }

    /** Given the 2D triangle (p0,p1,p2), where the points are given
        such that we orbit the triangle clockwise (the interior of the
        triangle is on the left of each segment), is point q inside
        the triangle?
     **/
    public static final boolean pointInsideTriangle(double p0[], double p1[],
            double p2[], double q[]) {
        return LinAlg.pointLeftOf(p0, p1, q) && pointLeftOf(p1, p2, q)
                && pointLeftOf(p2, p0, q);
    }

    public static float[] copyFloats(double a[]) {
        float r[] = new float[a.length];
        for (int i = 0; i < a.length; i++)
            r[i] = (float) a[i];
        return r;
    }

    public static double[] copyDoubles(float a[]) {
        double r[] = new double[a.length];
        for (int i = 0; i < a.length; i++)
            r[i] = a[i];
        return r;
    }

    public static double[] copyDoubles(int a[]) {
        double r[] = new double[a.length];
        for (int i = 0; i < a.length; i++)
            r[i] = a[i];
        return r;
    }

    public static float[] copy(float a[]) {
        float r[] = new float[a.length];
        for (int i = 0; i < a.length; i++)
            r[i] = a[i];
        return r;
    }

    public static int[] copy(int a[]) {
        int r[] = new int[a.length];
        for (int i = 0; i < a.length; i++)
            r[i] = a[i];
        return r;
    }

    public static double[] copy(double a[], int len) {
        double r[] = new double[len];

        for (int i = 0; i < Math.min(len, a.length); i++)
            r[i] = a[i];

        return r;
    }

    public static double[] copy(double a[], int a0, int len) {
        assert (a.length >= a0 + len);
        double r[] = new double[len];
        for (int i = 0; i < len; i++)
            r[i] = a[i + a0];
        return r;
    }

    public static double[] copy(double a[]) {
        double r[] = new double[a.length];

        for (int i = 0; i < a.length; i++)
            r[i] = a[i];

        return r;
    }

    public static void copy(double a[], double r[]) {
        assert (r.length == a.length);

        for (int i = 0; i < a.length; i++)
            r[i] = a[i];
    }

    public static double[][] copy(double a[][]) {
        if (a.length == 0)
            return new double[0][0];

        double X[][] = new double[a.length][a[0].length];
        for (int i = 0; i < a.length; i++)
            for (int j = 0; j < a[0].length; j++)
                X[i][j] = a[i][j];
        return X;
    }

    /** Copy the submatrix beginning at (i0,j0) with 'nrows' rows and 'ncols' columns **/
    public static double[][] copy(double a[][], int i0, int j0, int nrows,
            int ncols) {
        double X[][] = new double[nrows][ncols];
        for (int i = 0; i < nrows; i++)
            for (int j = 0; j < ncols; j++)
                X[i][j] = a[i0 + i][j0 + j];
        return X;
    }

    public static double[] quatToRollPitchYaw(double q[]) {
        assert (q.length == 4);

        // XXX: suspicious!
        double rpy[] = new double[3];

        double roll_a = 2 * (q[0] * q[1] + q[2] * q[3]);
        double roll_b = 1 - 2 * (q[1] * q[1] + q[2] * q[2]);
        rpy[0] = Math.atan2(roll_a, roll_b);

        double pitch_sin = 2 * (q[0] * q[2] - q[3] * q[1]);
        rpy[1] = Math.asin(pitch_sin);

        double yaw_a = 2 * (q[0] * q[3] + q[1] * q[2]);
        double yaw_b = 1 - 2 * (q[2] * q[2] + q[3] * q[3]);
        rpy[2] = Math.atan2(yaw_a, yaw_b);

        return rpy;
    }

    public static double[] rollPitchYawToQuat(double rpy[]) {
        assert (rpy.length == 3);

        double q[] = new double[4];
        double roll = rpy[0], pitch = rpy[1], yaw = rpy[2];

        double halfroll = roll / 2;
        double halfpitch = pitch / 2;
        double halfyaw = yaw / 2;

        double sin_r2 = Math.sin(halfroll);
        double sin_p2 = Math.sin(halfpitch);
        double sin_y2 = Math.sin(halfyaw);

        double cos_r2 = Math.cos(halfroll);
        double cos_p2 = Math.cos(halfpitch);
        double cos_y2 = Math.cos(halfyaw);

        q[0] = cos_r2 * cos_p2 * cos_y2 + sin_r2 * sin_p2 * sin_y2;
        q[1] = sin_r2 * cos_p2 * cos_y2 - cos_r2 * sin_p2 * sin_y2;
        q[2] = cos_r2 * sin_p2 * cos_y2 + sin_r2 * cos_p2 * sin_y2;
        q[3] = cos_r2 * cos_p2 * sin_y2 - sin_r2 * sin_p2 * cos_y2;

        return q;
    }

    /** Returns the equivalent 4x4 transformation matrix. **/
    public static double[][] rollPitchYawToMatrix(double rpy[]) {
        return quatToMatrix(rollPitchYawToQuat(rpy));
    }

    public static double[][] scale(double sxyz) {
        return scale(sxyz, sxyz, sxyz);
    }

    /** Returns the 4x4 transformation matrix corresponding to scaling the dimensions **/
    public static double[][] scale(double sx, double sy, double sz) {
        double T[][] = identity(4);
        T[0][0] = sx;
        T[1][1] = sy;
        T[2][2] = sz;
        return T;
    }

    /** Return the 4x4 transformation matrix corresponding to translations in x, y, and z **/
    public static double[][] translate(double xyz[]) {
        double T[][] = identity(4);
        T[0][3] = xyz[0];
        T[1][3] = xyz[1];
        if (xyz.length > 2)
            T[2][3] = xyz[2];

        return T;
    }

    /** Return the 4x4 transformation matrix corresponding to translations in x, y, and z **/
    public static double[][] translate(double x, double y, double z) {
        double T[][] = identity(4);
        T[0][3] = x;
        T[1][3] = y;
        T[2][3] = z;

        return T;
    }

    public static double[][] translate(double x, double y) {
        return translate(x, y, 0);
    }

    /** Returns the 4x4 transformation matrix due to rotate theta degrees around the X axis **/
    public static double[][] rotateX(double theta) {
        double c = Math.cos(theta), s = Math.sin(theta);

        return new double[][] { { 1, 0, 0, 0 }, { 0, c, -s, 0 },
                { 0, s, c, 0 }, { 0, 0, 0, 1 } };
    }

    /** Returns the 4x4 transformation matrix due to rotate theta degrees around the Y axis **/
    public static double[][] rotateY(double theta) {
        double c = Math.cos(theta), s = Math.sin(theta);

        return new double[][] { { c, 0, s, 0 }, { 0, 1, 0, 0 },
                { -s, 0, c, 0 }, { 0, 0, 0, 1 } };
    }

    /** Returns the 4x4 transformation matrix due to rotate theta degrees around the Z axis **/
    public static double[][] rotateZ(double theta) {
        double c = Math.cos(theta), s = Math.sin(theta);

        return new double[][] { { c, -s, 0, 0 }, { s, c, 0, 0 },
                { 0, 0, 1, 0 }, { 0, 0, 0, 1 } };
    }

    /** Compose the rotations of quaternion a and quaternion b **/
    public static double[] quatMultiply(double a[], double b[]) {
        assert (a.length == 4);
        assert (b.length == 4);

        double c[] = new double[4];

        c[0] = a[0] * b[0] - a[1] * b[1] - a[2] * b[2] - a[3] * b[3];
        c[1] = a[0] * b[1] + a[1] * b[0] + a[2] * b[3] - a[3] * b[2];
        c[2] = a[0] * b[2] - a[1] * b[3] + a[2] * b[0] + a[3] * b[1];
        c[3] = a[0] * b[3] + a[1] * b[2] - a[2] * b[1] + a[3] * b[0];

        return c;
    }

    public static double[] quatInverse(double q[]) {
        double mag = LinAlg.magnitude(q);
        double r[] = new double[4];
        r[0] = q[0] / mag;
        r[1] = -q[1] / mag;
        r[2] = -q[2] / mag;
        r[3] = -q[3] / mag;
        return r;
    }

    /** Interpolate quaternions from q0 (w=0) to q1 (w=1). **/
    public static double[] slerp(double q0[], double q1[], double w) {
        double dot = LinAlg.dotProduct(q0, q1);

        if (dot < 0) {
            // flip sign on one of them so we don't spin the "wrong
            // way" around. This doesn't change the rotation that the
            // quaternion represents.
            dot = -dot;
            q1 = LinAlg.scale(q1, -1);
        }

        // if large dot product (1), slerp will scale both q0 and q1
        // by 0, and normalization will blow up.
        if (dot > 0.95) {
            return LinAlg.add(LinAlg.scale(q0, 1 - w), LinAlg.scale(q1, w));
        }

        // normal slerp interpolation.
        double angle = Math.acos(dot);

        // they're the same... don't blow up with divide by zero.
        if (angle == 0)
            return LinAlg.copy(q0);

        // SLERP.
        return LinAlg.normalize(LinAlg.add(
                LinAlg.scale(q0, Math.sin(angle * (1 - w))),
                LinAlg.scale(q1, Math.sin(angle * w))));

        /* Test code template
           Try -45,-45 ; -45 +45 ; -45 134; -45 136 ; 0 90

        double q0[] = LinAlg.rollPitchYawToQuat(new double[] {0, 0, Math.toRadians(-45)});
        double q1[] = LinAlg.rollPitchYawToQuat(new double[] {0, 0, Math.toRadians(-45)});
        for (double w = 0; w <= 1; w+= 0.1)
            System.out.printf("%15f\n", Math.toDegrees(LinAlg.quatToRollPitchYaw(LinAlg.slerp(q0, q1, w))[2]));
         */
    }

    /** Rotate the vector v by the quaternion q **/
    public static double[] quatRotate(double q[], double v[]) {
        assert (q.length == 4);
        assert (v.length == 3);

        double t2, t3, t4, t5, t6, t7, t8, t9, t10;

        t2 = q[0] * q[1];
        t3 = q[0] * q[2];
        t4 = q[0] * q[3];
        t5 = -q[1] * q[1];
        t6 = q[1] * q[2];
        t7 = q[1] * q[3];
        t8 = -q[2] * q[2];
        t9 = q[2] * q[3];
        t10 = -q[3] * q[3];

        return new double[] {
                2 * ((t8 + t10) * v[0] + (t6 - t4) * v[1] + (t3 + t7) * v[2])
                        + v[0],
                2 * ((t4 + t6) * v[0] + (t5 + t10) * v[1] + (t9 - t2) * v[2])
                        + v[1],
                2 * ((t7 - t3) * v[0] + (t2 + t9) * v[1] + (t5 + t8) * v[2])
                        + v[2] };
    }

    /** Given two unit vectors 'a' and 'b', what is the
     * minimum-rotation quaternion that projects 'a' onto 'b'?
     *
     **/
    public static double[] quatCompute(double a[], double b[]) {
        a = LinAlg.normalize(a);
        b = LinAlg.normalize(b);

        // rotate around the axis perpendicular to both of the vectors.
        double cross[] = LinAlg.crossProduct(a, b);

        // the sine of the angle between them is the magnitude of the
        // cross product. But there are two angles that have that
        // sine.  Just try both and return the better one.
        double theta = Math.asin(LinAlg.magnitude(cross));

        double q0[] = LinAlg.angleAxisToQuat(theta, cross);
        double q1[] = LinAlg.angleAxisToQuat(Math.PI - theta, cross);

        double b0[] = LinAlg.quatRotate(q0, a);
        double b1[] = LinAlg.quatRotate(q1, a);

        if (LinAlg.distance(b0, b) < LinAlg.distance(b1, b))
            return q0;

        return q1;
    }

    public static double[] matrixToXyzrpy(double M[][]) {
        double tx = M[0][3];
        double ty = M[1][3];
        double tz = M[2][3];
        double rx = Math.atan2(M[2][1], M[2][2]);
        double ry = Math.atan2(-M[2][0],
                Math.sqrt(M[0][0] * M[0][0] + M[1][0] * M[1][0]));
        double rz = Math.atan2(M[1][0], M[0][0]);

        return new double[] { tx, ty, tz, rx, ry, rz };
    }

    public static double[] matrixToRollPitchYaw(double M[][]) {
        double q[] = matrixToQuat(M);
        return quatToRollPitchYaw(q);
    }

    public static double[] matrixToRollPitchYaw(Matrix M) {
        double q[] = matrixToQuat(M);
        return quatToRollPitchYaw(q);
    }

    public static double[] matrixToQuat(double R[][]) {
        return matrixToQuat(new Matrix(R));
    }

    /** Convert the 3x3 rotation matrix to a quaternion. (A 4x4
        transformation matrix may be passed in: only the upper-left
        3x3 block will be considered.)
     **/
    public static double[] matrixToQuat(Matrix R) {
        // see: "from quaternion to matrix and back"
        // trace: get the same result if R is 4x4 or 3x3:
        double T = R.get(0, 0) + R.get(1, 1) + R.get(2, 2) + 1;
        double S;

        double m0 = R.get(0, 0), m1 = R.get(1, 0), m2 = R.get(2, 0);
        double m4 = R.get(0, 1), m5 = R.get(1, 1), m6 = R.get(2, 1);
        double m8 = R.get(0, 2), m9 = R.get(1, 2), m10 = R.get(2, 2);

        double q[] = new double[4];

        if (T > 0.0000001) {
            S = Math.sqrt(T) * 2;
            q[1] = -(m9 - m6) / S;
            q[2] = -(m2 - m8) / S;
            q[3] = -(m4 - m1) / S;
            q[0] = 0.25 * S;
        } else if (m0 > m5 && m0 > m10) { // Column 0:
            S = Math.sqrt(1.0 + m0 - m5 - m10) * 2;
            q[1] = -0.25 * S;
            q[2] = -(m4 + m1) / S;
            q[3] = -(m2 + m8) / S;
            q[0] = (m9 - m6) / S;
        } else if (m5 > m10) { // Column 1:
            S = Math.sqrt(1.0 + m5 - m0 - m10) * 2;
            q[1] = -(m4 + m1) / S;
            q[2] = -0.25 * S;
            q[3] = -(m9 + m6) / S;
            q[0] = (m2 - m8) / S;
        } else {
            // Column 2:
            S = Math.sqrt(1.0 + m10 - m0 - m5) * 2;
            q[1] = -(m2 + m8) / S;
            q[2] = -(m9 + m6) / S;
            q[3] = -0.25 * S;
            q[0] = (m4 - m1) / S;
        }

        return normalize(q);
    }

    /** returns xytMultiply(xytInverse(a), b) **/
    public static final double[] xytInvMul31(double a[], double b[]) {
        return xytInvMul31(a, b, null);
    }

    /** compute:  X = xytMultiply(xytInverse(a), b) **/
    public static final double[] xytInvMul31(double a[], double b[], double X[]) {
        assert (a.length == 3 && b.length == 3);

        double theta = a[2];
        double ca = Math.cos(theta), sa = Math.sin(theta);
        double dx = b[0] - a[0];
        double dy = b[1] - a[1];

        if (X == null)
            X = new double[3];

        X[0] = ca * dx + sa * dy;
        X[1] = -sa * dx + ca * dy;
        X[2] = b[2] - a[2];

        return X;
    }

    /** multiply two (x,y,theta) 2D rigid-body transformations **/
    public static double[] xytMultiply(double a[], double b[]) {
        return xytMultiply(a, b, null);
    }

    /** multiply two (x,y,theta) 2D rigid-body transformations **/
    public static double[] xytMultiply(double a[], double b[], double r[]) {
        assert (a.length == 3 && b.length == 3);

        double s = Math.sin(a[2]), c = Math.cos(a[2]);
        if (r == null)
            r = new double[3];
        assert (r.length == 3);

        r[0] = c * b[0] - s * b[1] + a[0];
        r[1] = s * b[0] + c * b[1] + a[1];
        r[2] = a[2] + b[2];

        return r;
    }

    /** compute the inverse of a (x,y,theta) transformation **/
    public static double[] xytInverse(double a[]) {
        assert (a.length == 3);

        double s = Math.sin(a[2]), c = Math.cos(a[2]);
        double r[] = new double[3];

        r[0] = -s * a[1] - c * a[0];
        r[1] = -c * a[1] + s * a[0];
        r[2] = -a[2];

        return r;
    }

    public static double[][] xyzrpyToMatrix(double xyzrpy[]) {
        assert (xyzrpy.length == 6);

        return quatPosToMatrix(rollPitchYawToQuat(new double[] { xyzrpy[3],
                xyzrpy[4], xyzrpy[5] }), new double[] { xyzrpy[0], xyzrpy[1],
                xyzrpy[2] });
    }

    public static double[][] xyzrpyToMatrix2(double xyzrpy[]) {
        assert (xyzrpy.length == 6 || xyzrpy.length == 3);

        double tx, ty, tz, rx, ry, rz;
        if (xyzrpy.length == 3) {
            tx = 0;
            ty = 0;
            tz = 0;
            rx = xyzrpy[0];
            ry = xyzrpy[1];
            rz = xyzrpy[2];
        } else {
            tx = xyzrpy[0];
            ty = xyzrpy[1];
            tz = xyzrpy[2];
            rx = xyzrpy[3];
            ry = xyzrpy[4];
            rz = xyzrpy[5];
        }

        double cx = Math.cos(rx), sx = Math.sin(rx);
        double cy = Math.cos(ry), sy = Math.sin(ry);
        double cz = Math.cos(rz), sz = Math.sin(rz);

        return new double[][] {
                { cy * cz, cz * sx * sy - cx * sz, sx * sz + cx * cz * sy, tx },
                { cy * sz, cx * cz + sx * sy * sz, cx * sy * sz - cz * sx, ty },
                { -sy, cy * sx, cx * cy, tz }, { 0, 0, 0, 1 } };
    }

    public static double[] xyzrpyInverse(double xyzrpy[]) {
        double tx, ty, tz, rx, ry, rz;
        if (xyzrpy.length == 3) {
            tx = 0;
            ty = 0;
            tz = 0;
            rx = xyzrpy[0];
            ry = xyzrpy[1];
            rz = xyzrpy[2];
        } else {
            tx = xyzrpy[0];
            ty = xyzrpy[1];
            tz = xyzrpy[2];
            rx = xyzrpy[3];
            ry = xyzrpy[4];
            rz = xyzrpy[5];
        }

        double cx = Math.cos(rx), sx = Math.sin(rx);
        double cy = Math.cos(ry), sy = Math.sin(ry);
        double cz = Math.cos(rz), sz = Math.sin(rz);

        double M[][] = inverse(xyzrpyToMatrix(xyzrpy));
        return matrixToXyzrpy(M);
    }

    /** convert x,y,theta vector to a 4x4 matrix **/
    public static double[][] xytToMatrix(double xyt[]) {
        assert (xyt.length == 3);

        double M[][] = identity(4);
        double s = Math.sin(xyt[2]), c = Math.cos(xyt[2]);
        M[0][0] = c;
        M[0][1] = -s;
        M[0][3] = xyt[0];

        M[1][0] = s;
        M[1][1] = c;
        M[1][3] = xyt[1];

        return M;
    }

    /** convert 4x4 matrix to XYT notation. Obviously, lossy. **/
    public static double[] matrixToXYT(double M[][]) {
        double xyt[] = new double[3];
        xyt[0] = M[0][3];
        xyt[1] = M[1][3];

        double rpy[] = matrixToRollPitchYaw(M);
        xyt[2] = rpy[2];
        return xyt;
    }

    public static double[] angleAxisToQuat(double angleAxis[]) {
        return angleAxisToQuat(angleAxis[0], new double[] { angleAxis[1],
                angleAxis[2], angleAxis[3] });
    }

    public static double[] angleAxisToQuat(double theta, double axis[]) {
        axis = LinAlg.normalize(axis);
        double q[] = new double[4];
        q[0] = Math.cos(theta / 2);
        double s = Math.sin(theta / 2);
        q[1] = axis[0] * s;
        q[2] = axis[1] * s;
        q[3] = axis[2] * s;

        return q;
    }

    /** return vector: [rad, x, y, z] **/
    public static double[] quatToAngleAxis(double q[]) {
        q = LinAlg.normalize(q);

        double aa[] = new double[4];

        // be polite: return an angle from [-pi, pi]
        // use atan2 to be 4-quadrant safe
        double mag = Math.sqrt(q[1] * q[1] + q[2] * q[2] + q[3] * q[3]);
        aa[0] = MathUtil.mod2pi(2 * Math.atan2(mag, q[0]));
        if (mag != 0) {
            aa[1] = q[1] / mag;
            aa[2] = q[2] / mag;
            aa[3] = q[3] / mag;
        } else {
            aa[1] = 1;
            aa[2] = 0;
            aa[3] = 0;
        }
        return aa;
    }

    public static double[][] quatToMatrix(double q[]) {
        return quatPosToMatrix(q, null);
    }

    public static double[][] quatPosToMatrix(double q[], double pos[]) {
        double M[][] = new double[4][4];
        double w = q[0], x = q[1], y = q[2], z = q[3];

        M[0][0] = w * w + x * x - y * y - z * z;
        M[0][1] = 2 * x * y - 2 * w * z;
        M[0][2] = 2 * x * z + 2 * w * y;

        M[1][0] = 2 * x * y + 2 * w * z;
        M[1][1] = w * w - x * x + y * y - z * z;
        M[1][2] = 2 * y * z - 2 * w * x;

        M[2][0] = 2 * x * z - 2 * w * y;
        M[2][1] = 2 * y * z + 2 * w * x;
        M[2][2] = w * w - x * x - y * y + z * z;

        if (pos != null) {
            M[0][3] = pos[0];
            M[1][3] = pos[1];
            M[2][3] = pos[2];
        }

        M[3][3] = 1;

        return M;
    }

    public static double[] quatPosToXYT(double q[], double pos[]) {
        double xyt[] = new double[3];
        xyt[0] = pos[0];
        xyt[1] = pos[1];
        double rpy[] = quatToRollPitchYaw(q);
        xyt[2] = rpy[2];
        return xyt;
    }

    public static double[] quatPosToXyzrpy(double q[], double pos[]) {
        double xyzrpy[] = new double[6];
        xyzrpy[0] = pos[0];
        xyzrpy[1] = pos[1];
        xyzrpy[2] = pos[2];
        double rpy[] = quatToRollPitchYaw(q);
        xyzrpy[3] = rpy[0];
        xyzrpy[4] = rpy[1];
        xyzrpy[5] = rpy[2];
        return xyzrpy;
    }

    /** v = v*a, storing result back into v **/
    public static void scaleEquals(double v[], double a) {
        for (int i = 0; i < v.length; i++)
            v[i] *= a;
    }

    public static float[] scale(float v[], double a) {
        return scale(v, a, null);
    }

    /** returns v*a, allocating a new result **/
    public static double[] scale(double v[], double a) {
        return scale(v, a, null);
    }

    public static double[] scale(double v[], double a, double nv[]) {
        if (nv == null)
            nv = new double[v.length];

        for (int i = 0; i < v.length; i++)
            nv[i] = v[i] * a;
        return nv;
    }

    public static float[] scale(float v[], double a, float nv[]) {
        if (nv == null)
            nv = new float[v.length];

        for (int i = 0; i < v.length; i++)
            nv[i] = (float) (v[i] * a);
        return nv;
    }

    public static double[] scale(double v[], double s[]) {
        return scale(v, s, null);
    }

    public static double[] scale(double v[], double s[], double nv[]) {
        assert (v.length == s.length);
        if (nv == null)
            nv = new double[v.length];

        for (int i = 0; i < v.length; i++)
            nv[i] = v[i] * s[i];
        return nv;
    }

    public static double[][] scale(double v[][], double a) {
        double X[][] = new double[v.length][v[0].length];
        for (int i = 0; i < v.length; i++)
            for (int j = 0; j < v[0].length; j++)
                X[i][j] = v[i][j] * a;
        return X;
    }

    public static void scaleEquals(double v[][], double a) {
        for (int i = 0; i < v.length; i++)
            for (int j = 0; j < v[0].length; j++)
                v[i][j] *= a;
    }

    public static void printTranspose(byte v[]) {
        for (int i = 0; i < v.length; i++)
            System.out.printf("%15d\t", v[i]);
        System.out.printf("\n");
    }

    public static void printTranspose(int v[]) {
        for (int i = 0; i < v.length; i++)
            System.out.printf("%15d\t", v[i]);
        System.out.printf("\n");
    }

    public static void printTranspose(long v[]) {
        for (int i = 0; i < v.length; i++)
            System.out.printf("%15d\t", v[i]);
        System.out.printf("\n");
    }

    public static void printTranspose(float v[]) {
        for (int i = 0; i < v.length; i++)
            System.out.printf("%15f\t", v[i]);
        System.out.printf("\n");
    }

    public static void printTranspose(double v[]) {
        for (int i = 0; i < v.length; i++)
            System.out.printf("%15f\t", v[i]);
        System.out.printf("\n");
    }

    public static void printTranspose(double v[][]) {
        if (v.length == 0)
            return;
        for (int j = 0; j < v[0].length; j++) {
            for (int i = 0; i < v.length; i++) {
                System.out.printf("%14f ", v[i][j]);
            }
            System.out.printf("\n");
        }
    }

    public static void print(byte v[]) {
        for (int i = 0; i < v.length; i++)
            System.out.printf("%15d\n", v[i]);
        System.out.printf("\n");
    }

    public static void print(int v[]) {
        for (int i = 0; i < v.length; i++)
            System.out.printf("%15d\n", v[i]);
        System.out.printf("\n");
    }

    public static void print(long v[]) {
        for (int i = 0; i < v.length; i++)
            System.out.printf("%15dl\n", v[i]);
        System.out.printf("\n");
    }

    public static void print(float v[]) {
        for (int i = 0; i < v.length; i++)
            System.out.printf("%15f\n", v[i]);
        System.out.printf("\n");
    }

    public static void print(double v[]) {
        for (int i = 0; i < v.length; i++)
            System.out.printf("%15f\n", v[i]);
        System.out.printf("\n");
    }

    public static void print(int v[][]) {
        for (int i = 0; i < v.length; i++) {
            printTranspose(v[i]);
        }
    }

    public static void print(long v[][]) {
        for (int i = 0; i < v.length; i++) {
            printTranspose(v[i]);
        }
    }

    public static void print(float v[][]) {
        for (int i = 0; i < v.length; i++) {
            printTranspose(v[i]);
        }
    }

    public static void print(double v[][]) {
        for (int i = 0; i < v.length; i++) {
            printTranspose(v[i]);
        }
    }

    /** Read a matrix; compatible with print method. Also strips out
     * empty lines as necessary. **/
    public static double[][] readMatrix(BufferedReader ins, int rows, int cols)
            throws IOException {
        double M[][] = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            while (true) {
                String line = ins.readLine();
                String toks[] = line.trim().split("\\s+");
                if (toks.length == 0)
                    continue;
                if (toks.length != cols) {
                    System.out.println("LinAlg.readMatrix: bad line " + line);
                    assert (false);
                }
                assert (toks.length == cols);
                for (int j = 0; j < cols; j++)
                    M[i][j] = Double.parseDouble(toks[j]);
                break;
            }
        }
        return M;
    }

    /** print out the sparsity patern of v using "." and "X" **/
    public static void printPattern(double v[][]) {
        for (int i = 0; i < v.length; i++) {
            for (int j = 0; j < v[0].length; j++) {
                if (v[i][j] == 0)
                    System.out.printf(".");
                else
                    System.out.printf("X");
            }
            System.out.printf("\n");
        }
    }

    /** Count the number of non-zero elements. **/
    public static int nz(double v[][]) {
        int nz = 0;

        for (int i = 0; i < v.length; i++) {
            for (int j = 0; j < v[0].length; j++) {
                if (v[i][j] != 0)
                    nz++;
            }
        }
        return nz;
    }

    /** return a vector close to a that is perpendicular to b. **/
    public static double[] makePerpendicular(double a[], double b[]) {
        double bdir[] = LinAlg.normalize(b);
        double dot = LinAlg.dotProduct(a, bdir);
        return LinAlg.subtract(a, LinAlg.scale(bdir, dot));
    }

    public static double dotProduct(double a[], double b[]) {
        assert (a.length == b.length);

        double mag = 0;
        for (int i = 0; i < a.length; i++)
            mag += a[i] * b[i];

        return mag;
    }

    public static double meanRadius(List<double[]> points, double center[]) {
        double acc = 0;
        for (double p[] : points) {
            acc += LinAlg.distance(p, center);
        }

        return acc / points.size();
    }

    public static double maxRadius(List<double[]> points, double center[]) {
        double acc = 0;
        for (double p[] : points) {
            acc = Math.max(acc, LinAlg.distance(p, center));
        }

        return acc;
    }

    public static double[] centroid(List<double[]> points) {
        if (points.size() == 0)
            return null;

        double acc[] = new double[points.get(0).length];
        for (double p[] : points)
            LinAlg.plusEquals(acc, p);

        LinAlg.scaleEquals(acc, 1.0 / points.size());

        return acc;
    }

    public static boolean equals(double a[], double b[], double thresh) {
        if (a.length != b.length)
            return false;

        for (int i = 0; i < a.length; i++) {
            if (Math.abs(a[i] - b[i]) > thresh)
                return false;
        }

        return true;
    }

    public static boolean equals(double a[][], double b[][], double thresh) {
        if (a.length != b.length || a[0].length != b[0].length)
            return false;

        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                if (Math.abs(a[i][j] - b[i][j]) > thresh)
                    return false;
            }
        }

        return true;
    }

    /** Transforms 2d or 3d points according the xytheta transform. **/
    public final static double[] transform(double xyt[], double p[]) {
        assert (xyt.length == 3);
        double c = Math.cos(xyt[2]), s = Math.sin(xyt[2]);

        if (p.length == 2)
            return new double[] { p[0] * c - p[1] * s + xyt[0],
                    p[0] * s + p[1] * c + xyt[1] };

        return new double[] { p[0] * c - p[1] * s + xyt[0],
                p[0] * s + p[1] * c + xyt[1], p[2] };
    }

    public static ArrayList<double[]> transform(double T[],
            List<double[]> points) {
        ArrayList<double[]> newpoints = new ArrayList<double[]>();

        for (double p[] : points) {
            double tp[] = transform(T, p);
            newpoints.add(tp);
        }

        return newpoints;
    }

    public final static double[] transformRotateOnly(double T[][], double p[]) {
        assert (T.length == 4);

        return new double[] { T[0][0] * p[0] + T[0][1] * p[1] + T[0][2] * p[2],
                T[1][0] * p[0] + T[1][1] * p[1] + T[1][2] * p[2],
                T[2][0] * p[0] + T[2][1] * p[1] + T[2][2] * p[2] };
    }

    public final static double[] transform(double T[][], double p[]) {
        if (T.length == 4) {
            if (p.length == 2) {
                return new double[] {
                        T[0][0] * p[0] + T[0][1] * p[1] + T[0][3],
                        T[1][0] * p[0] + T[1][1] * p[1] + T[1][3],
                        T[2][0] * p[0] + T[2][1] * p[1] + T[2][3] };
            } else if (p.length == 3) {
                return new double[] {
                        T[0][0] * p[0] + T[0][1] * p[1] + T[0][2] * p[2]
                                + T[0][3],
                        T[1][0] * p[0] + T[1][1] * p[1] + T[1][2] * p[2]
                                + T[1][3],
                        T[2][0] * p[0] + T[2][1] * p[1] + T[2][2] * p[2]
                                + T[2][3] };
            } else if (p.length == 4) {
                return new double[] {
                        T[0][0] * p[0] + T[0][1] * p[1] + T[0][2] * p[2]
                                + T[0][3],
                        T[1][0] * p[0] + T[1][1] * p[1] + T[1][2] * p[2]
                                + T[1][3],
                        T[2][0] * p[0] + T[2][1] * p[1] + T[2][2] * p[2]
                                + T[2][3], 1 };
            }
            assert (false);

        } else if (T.length == 3) {
            if (p.length == 2) {
                return new double[] {
                        T[0][0] * p[0] + T[0][1] * p[1] + T[0][2],
                        T[1][0] * p[0] + T[1][1] * p[1] + T[1][2],
                        T[2][0] * p[0] + T[2][1] * p[1] + T[2][2] };
            }
            assert (false);
        }

        assert (false);
        return null;
    }

    public final static double[] transform(Matrix T, double p[]) {
        if (T.getColumnDimension() == 4) {
            if (p.length == 2) {
                return new double[] {
                        T.get(0, 0) * p[0] + T.get(0, 1) * p[1] + T.get(0, 3),
                        T.get(1, 0) * p[0] + T.get(1, 1) * p[1] + T.get(1, 3),
                        T.get(2, 0) * p[0] + T.get(2, 1) * p[1] + T.get(2, 3) };
            } else if (p.length == 3) {
                return new double[] {
                        T.get(0, 0) * p[0] + T.get(0, 1) * p[1] + T.get(0, 2)
                                * p[2] + T.get(0, 3),
                        T.get(1, 0) * p[0] + T.get(1, 1) * p[1] + T.get(1, 2)
                                * p[2] + T.get(1, 3),
                        T.get(2, 0) * p[0] + T.get(2, 1) * p[1] + T.get(2, 2)
                                * p[2] + T.get(2, 3) };
            } else if (p.length == 4) {
                return new double[] {
                        T.get(0, 0) * p[0] + T.get(0, 1) * p[1] + T.get(0, 2)
                                * p[2] + T.get(0, 3),
                        T.get(1, 0) * p[0] + T.get(1, 1) * p[1] + T.get(1, 2)
                                * p[2] + T.get(1, 3),
                        T.get(2, 0) * p[0] + T.get(2, 1) * p[1] + T.get(2, 2)
                                * p[2] + T.get(2, 3), 1 };
            }
            assert (false);
        }

        assert (false);
        return null;
    }

    public static ArrayList<double[]> transform(Matrix T, List<double[]> points) {
        ArrayList<double[]> newpoints = new ArrayList<double[]>();

        for (double p[] : points) {
            double tp[] = transform(T, p);
            newpoints.add(tp);
        }

        return newpoints;
    }

    public static ArrayList<double[]> transform(double T[][],
            List<double[]> points) {
        ArrayList<double[]> newpoints = new ArrayList<double[]>();

        for (double p[] : points) {
            double tp[] = transform(T, p);
            newpoints.add(tp);
        }

        return newpoints;
    }

    // compute inv(T)*p, assuming that T is a scaled rigid-body matrix
    public static final double[] transformInverse(double T[][], double p[]) {
        double x[] = new double[3];

        // scale can be set to 1 if there's no scale transformation.
        double scale = 1.0 / Math.sqrt(LinAlg.sq(T[0][0]) + LinAlg.sq(T[1][0])
                + LinAlg.sq(T[2][0]));

        // undo translation
        double px = (p[0] - T[0][3]) * scale;
        double py = (p[1] - T[1][3]) * scale;
        double pz = (p[2] - T[2][3]) * scale;

        // undo rotation, noting that inv(R) = R'
        x[0] = scale * (T[0][0] * px + T[1][0] * py + T[2][0] * pz);
        x[1] = scale * (T[0][1] * px + T[1][1] * py + T[2][1] * pz);
        x[2] = scale * (T[0][2] * px + T[1][2] * py + T[2][2] * pz);

        return x;
    }

    // compute inv(T)*p, assuming that T is a scaled rigid-body matrix
    public static final double[] transformInverseRotateOnly(double T[][],
            double p[]) {
        double x[] = new double[3];

        // scale can be set to 1 if there's no scale transformation.
        double scale = 1.0 / Math.sqrt(LinAlg.sq(T[0][0]) + LinAlg.sq(T[1][0])
                + LinAlg.sq(T[2][0]));

        // undo translation
        double px = (p[0]) * scale;
        double py = (p[1]) * scale;
        double pz = (p[2]) * scale;

        // undo rotation, noting that inv(R) = R'
        x[0] = scale * (T[0][0] * px + T[1][0] * py + T[2][0] * pz);
        x[1] = scale * (T[0][1] * px + T[1][1] * py + T[2][1] * pz);
        x[2] = scale * (T[0][2] * px + T[1][2] * py + T[2][2] * pz);

        return x;
    }

    public static double clamp(double v, double min, double max) {
        if (v < min)
            return min;
        if (v > max)
            return max;
        return v;
    }

    public static int clamp(int v, int min, int max) {
        if (v < min)
            return min;
        if (v > max)
            return max;
        return v;
    }

    public static void clear(int v[]) {
        for (int i = 0; i < v.length; i++)
            v[i] = 0;
    }

    public static void clear(double v[]) {
        for (int i = 0; i < v.length; i++)
            v[i] = 0;
    }

    public static void clear(double v[][]) {
        for (int i = 0; i < v.length; i++)
            for (int j = 0; j < v[0].length; j++)
                v[i][j] = 0;
    }

    public static final double[][] transpose(double A[][]) {
        if (A.length == 0)
            return copy(A);

        double R[][] = new double[A[0].length][A.length];
        for (int row = 0; row < A.length; row++) {
            for (int col = 0; col < A[0].length; col++) {
                R[col][row] = A[row][col];
            }
        }
        return R;
    }

    public static final double[][] matrixABC(double A[][], double B[][],
            double C[][]) {
        return LinAlg.matrixAB(A, LinAlg.matrixAB(B, C));
    }

    public static final double[][] matrixABCt(double A[][], double B[][],
            double C[][]) {
        return LinAlg.matrixAB(A, LinAlg.matrixABt(B, C));
    }

    public static final double[][] matrixAtBC(double A[][], double B[][],
            double C[][]) {
        return LinAlg.matrixAB(LinAlg.matrixAtB(A, B), C);
    }

    // X = A * B
    public static final double[][] matrixAB(double A[][], double B[][]) {
        return matrixAB(A, B, null);
    }

    public static final double[][] matrixAB(double A[][], double B[][],
            double X[][]) {
        int m = A.length, n = B[0].length;
        int in = A[0].length;
        assert (A[0].length == B.length);
        if (X == null)
            X = new double[m][n];

        assert (X.length == m && X[0].length == n);

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                double acc = 0;
                for (int k = 0; k < in; k++)
                    acc += A[i][k] * B[k][j];
                X[i][j] = acc;
            }
        }

        return X;
    }

    /** X = A' * B **/
    public static final double[] matrixAB(double A[], double B[][]) {
        assert (A.length == B.length);

        double X[] = new double[B[0].length];

        for (int i = 0; i < X.length; i++) {
            double acc = 0;
            for (int k = 0; k < A.length; k++) {
                acc += A[k] * B[k][i];
            }
            X[i] = acc;
        }

        return X;
    }

    /** X = A * B **/
    public static final double[] matrixAB(double A[][], double B[]) {
        return matrixAB(A, B, null);
    }

    public static final double[] matrixAB(double A[][], double B[], double X[]) {
        assert (A[0].length == B.length);

        if (X == null)
            X = new double[A.length];

        assert (A.length == X.length);

        for (int i = 0; i < X.length; i++) {
            double acc = 0;
            for (int k = 0; k < A[0].length; k++)
                acc += A[i][k] * B[k];
            X[i] = acc;
        }

        return X;
    }

    public static final double[][] outerProduct(double A[], double B[]) {
        double X[][] = new double[A.length][B.length];

        for (int i = 0; i < A.length; i++)
            for (int j = 0; j < B.length; j++)
                X[i][j] = A[i] * B[j];
        return X;
    }

    // X = A' * B
    public static final double[][] matrixAtB(double A[][], double B[][]) {
        return matrixAtB(A, B, null);
    }

    public static final double[][] matrixAtB(double A[][], double B[][],
            double X[][]) {
        int m = A[0].length, n = B[0].length;
        int in = A.length;
        assert (A.length == B.length);
        if (X == null)
            X = new double[m][n];
        assert (X.length == m && X[0].length == n);

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                double acc = 0;
                for (int k = 0; k < in; k++)
                    acc += A[k][i] * B[k][j];
                X[i][j] = acc;
            }
        }

        return X;
    }

    public static final double[] matrixAtB(double A[][], double B[]) {
        return matrixAtB(A, B, null);
    }

    public static final double[] matrixAtB(double A[][], double B[], double X[]) {
        int m = A[0].length, n = B.length;

        assert (A.length == B.length);
        if (X == null)
            X = new double[m];
        assert (X.length == A[0].length);

        for (int i = 0; i < m; i++) {
            double acc = 0;
            for (int j = 0; j < n; j++) {
                acc += A[j][i] * B[j];
            }
            X[i] = acc;
        }

        return X;
    }

    public static final double[][] matrixABt(double A[][], double B[][]) {
        return matrixABt(A, B, null);
    }

    public static final double[][] matrixABt(double A[][], double B[][],
            double X[][]) {
        int m = A.length, n = B.length;
        int in = A[0].length;

        assert (A[0].length == B[0].length);
        if (X == null)
            X = new double[m][n];

        assert (X.length == m && X[0].length == n);

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                double acc = 0;
                for (int k = 0; k < in; k++)
                    acc += A[i][k] * B[j][k];
                X[i][j] = acc;
            }
        }

        return X;
    }

    // rotate the first two coordinates of v by theta radians
    public static double[] rotate2(double v[], double theta) {
        double X[] = LinAlg.copy(v);

        double sa = Math.sin(theta), ca = Math.cos(theta);
        double x = v[0], y = v[1];
        X[0] = ca * x - sa * y;
        X[1] = sa * x + ca * y;

        return X;
    }

    public static int min(int v[]) {
        return v[minIdx(v)];
    }

    public static int minIdx(int v[]) {
        int idx = -1;

        for (int i = 0; i < v.length; i++) {
            if (idx < 0 || v[i] < v[idx])
                idx = i;
        }

        return idx;
    }

    public static double min(double v[]) {
        return v[minIdx(v)];
    }

    public static int minIdx(double v[]) {
        int idx = -1;

        for (int i = 0; i < v.length; i++) {
            if (idx < 0 || v[i] < v[idx])
                idx = i;
        }

        return idx;
    }

    public static double max(double v[]) {
        return v[maxIdx(v)];
    }

    public static int maxIdx(double v[]) {
        int idx = -1;

        for (int i = 0; i < v.length; i++) {
            if (idx < 0 || v[i] > v[idx])
                idx = i;
        }

        return idx;
    }

    public static float min(float v[]) {
        return v[minIdx(v)];
    }

    public static int minIdx(float v[]) {
        int idx = -1;

        for (int i = 0; i < v.length; i++) {
            if (idx < 0 || v[i] < v[idx])
                idx = i;
        }

        return idx;
    }

    public static float max(float v[]) {
        return v[maxIdx(v)];
    }

    public static int maxIdx(float v[]) {
        int idx = -1;

        for (int i = 0; i < v.length; i++) {
            if (idx < 0 || v[i] > v[idx])
                idx = i;
        }

        return idx;
    }

    /** Given two (x,y) pairs, find the y coordinate for a third x point. **/
    public static double linearlyInterpolate(double xy0[], double xy1[],
            double x2) {
        return (xy0[1] + (xy1[1] - xy0[1]) * (x2 - xy0[0]) / (xy1[0] - xy0[0]));
    }

    /** Create a new matrix from the indices (inclusive) [row0, row1] and [col0,col1] **/
    public static double[][] select(double A[][], int row0, int row1, int col0,
            int col1) {
        double R[][] = new double[row1 - row0 + 1][col1 - col0 + 1];

        for (int r = row0; r <= row1; r++)
            for (int c = col0; c <= col1; c++)
                R[r - row0][c - col0] = A[r][c];
        return R;
    }

    /** Create a new vector from the indices (inclusive) [col0, col1] **/
    public static double[] select(double A[], int col0, int col1) {
        double R[] = new double[col1 - col0 + 1];

        for (int c = col0; c <= col1; c++)
            R[c - col0] = A[c];

        return R;
    }

    public static double[][] diag(double d[]) {
        double M[][] = new double[d.length][d.length];
        for (int i = 0; i < d.length; i++)
            M[i][i] = d[i];
        return M;
    }

    /** Return the identity matrix of size 'sz' **/
    public static double[][] identity(int sz) {
        double M[][] = new double[sz][sz];
        for (int i = 0; i < sz; i++)
            M[i][i] = 1;
        return M;
    }

    /** Generate a random matrix with each value drawn from uniform[0,1] **/
    public static double[][] randomMatrix(int rows, int cols, Random r) {
        double X[][] = new double[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                X[i][j] = r.nextDouble();

        return X;
    }

    public static double[][] multiplyMany(double a[][], double[][]... bs) {
        double X[][] = copy(a);

        for (double b[][] : bs)
            X = LinAlg.matrixAB(X, b);

        return X;
    }

    public static void main(String args[]) {
        Random r = new Random();

        // test matrix inversion
        /*
          for (int dim = 1; dim <= 4; dim++) {
          for (int iter = 0; iter < 1000; iter++) {
          double M[][] = randomMatrix(dim, dim, r);

          double qident[][] = matrixAB(M, inverse(M));
          double ident[][] = identity(dim);

          double err = max(abs(subtract(qident, ident)));
          if (err > 1E-10)
          System.out.println("WARNING: inverse dim="+dim+" err: "+err);
          }
          }
         */
        LinAlg.print(rollPitchYawToQuat(new double[] { 1 + 0, 0, 0 }));
        LinAlg.print(rollPitchYawToQuat(new double[] { 1 + 2 * Math.PI, 0, 0 }));

        for (int i = 0; i < 100; i++) {
            double q[] = angleAxisToQuat(
                    r.nextDouble() * 2 * Math.PI,
                    new double[] { (2 * r.nextDouble() - 1),
                            (2 * r.nextDouble() - 1), (2 * r.nextDouble() - 1) });

            double qinv[] = quatInverse(q);
            double M1[][] = quatToMatrix(q);
            double M2[][] = quatToMatrix(qinv);
            double I[][] = matrixAB(M1, M2);

            assert (equals(I, identity(4), 0.0001));
        }

        for (int i = 0; true && i < 1000000; i++) {
            double p[] = new double[] { 2 * r.nextDouble() - 1,
                    2 * r.nextDouble() - 1, 2 * r.nextDouble() - 1 };

            double q[] = angleAxisToQuat(
                    r.nextDouble() * 2 * Math.PI,
                    new double[] { (2 * r.nextDouble() - 1),
                            (2 * r.nextDouble() - 1), (2 * r.nextDouble() - 1) });

            double qp[] = quatRotate(q, p);

            double q1[] = matrixToQuat(new Matrix(quatToMatrix(q)));

            double q1p[] = quatRotate(q1, p);

            if (LinAlg.squaredDistance(qp, q1p) > 0.000001) {
                LinAlg.print(qp);
                LinAlg.print(q1p);
                System.out.println(i);

            }

            double Tp[] = matrixAB(quatToMatrix(q), new double[] { p[0], p[1],
                    p[2], 1 });
            if (LinAlg.squaredDistance(qp, LinAlg.copy(Tp, 3)) > 0.000001) {
                LinAlg.print(qp);
                LinAlg.print(Tp);
                System.out.println(i);
            }

            double rpy[] = quatToRollPitchYaw(q1);
            double Sp[] = matrixAB(rollPitchYawToMatrix(rpy), new double[] {
                    p[0], p[1], p[2], 1 });
            if (LinAlg.squaredDistance(qp, LinAlg.copy(Sp, 3)) > 0.000001) {
                LinAlg.print(qp);
                LinAlg.print(Sp);
                System.out.println(i);
            }

            double Qp[] = matrixAB(
                    rollPitchYawToMatrix(matrixToRollPitchYaw(quatToMatrix(q))),
                    new double[] { p[0], p[1], p[2], 1 });
            assert (LinAlg.squaredDistance(qp, LinAlg.copy(Qp, 3)) < 0.000001);

            double xyzrpy[] = new double[] { 0, 0, 0, rpy[0], rpy[1], rpy[2] };
            double Mp1[][] = xyzrpyToMatrix(xyzrpy);
            double Mp2[][] = xyzrpyToMatrix2(xyzrpy);
            double Mp3[][] = rollPitchYawToMatrix(rpy);

            assert (LinAlg.equals(LinAlg.matrixAB(
                    xyzrpyToMatrix2(xyzrpyInverse(xyzrpy)),
                    xyzrpyToMatrix2(xyzrpy)), identity(4), 0.00001));
            double Mrpy[] = matrixToRollPitchYaw(Mp2);
            assert (LinAlg.equals(Mrpy, rpy, 0.00001));

            double Mxyzrpy[] = matrixToXyzrpy(Mp2);
            assert (LinAlg.equals(Mxyzrpy, new double[] { 0, 0, 0, rpy[0],
                    rpy[1], rpy[2] }, 0.00001));

            /*	    LinAlg.print(Mp1);
                    System.out.println("\n");
                    LinAlg.print(Mp2); */
                    assert (LinAlg.equals(Mp1, Mp2, 0.00001));
                    assert (LinAlg.equals(Mp2, Mp3, 0.00001));
        }

        if (false) {
            double x[] = new double[] { 1, 0, 0, 1 };
            double y[] = new double[] { 0, 1, 0, 1 };
            double z[] = new double[] { 0, 0, 1, 1 };

            print(matrixAB(rotateX(Math.toRadians(90)), x)); // [1 0 0 1]
            print(matrixAB(rotateY(Math.toRadians(90)), x)); // [0 0 -1 1]
            print(matrixAB(rotateZ(Math.toRadians(90)), x)); // [0 1 0 1]

            print(matrixAB(rotateX(Math.toRadians(90)), y)); // [0 0 1 1]
            print(matrixAB(rotateY(Math.toRadians(90)), y)); // [0 1 0 1]
            print(matrixAB(rotateZ(Math.toRadians(90)), y)); // [-1 0 0 1]

            print(matrixAB(rotateX(Math.toRadians(90)), z)); // [0 -1 0 1]
            print(matrixAB(rotateY(Math.toRadians(90)), z)); // [1 0 0 1]
            print(matrixAB(rotateZ(Math.toRadians(90)), z)); // [0 0 1 1]
        }

        if (true) {
            double rpy[] = new double[] { 1, 2, 3 };
            double RR[][] = rotateX(rpy[0]);
            double RP[][] = rotateY(rpy[1]);
            double RY[][] = rotateZ(rpy[2]);

            print(matrixABC(RY, RP, RR));
            System.out.printf("\n");
            print(rollPitchYawToMatrix(rpy));

        }
    }

    public static double[] xyzrpyInvMul(double a[], double b[]) {
        double A[][] = xyzrpyToMatrix(a);
        double B[][] = xyzrpyToMatrix(b);
        double Ainv[][] = inverse(A);

        double AinvB[][] = matrixAB(Ainv, B);
        return matrixToXyzrpy(AinvB);
    }

    public static double[] xyzrpyMultiply(double a[], double b[]) {
        double A[][] = xyzrpyToMatrix(a);
        double B[][] = xyzrpyToMatrix(b);

        double AB[][] = matrixAB(A, B);
        return matrixToXyzrpy(AB);
    }

    public static double trace(double M[][]) {
        double t = 0;

        for (int i = 0; i < Math.min(M.length, M[0].length); i++)
            t += M[i][i];

        return t;
    }

    public static double det(double M[][]) {
        int n = M.length, m = M[0].length;
        assert (n == m);

        if (n == 2) {
            return M[0][0] * M[1][1] - M[0][1] * M[1][0];
        }

        if (n == 3) {
            double m00 = M[0][0], m01 = M[0][1], m02 = M[0][2];
            double m10 = M[1][0], m11 = M[1][1], m12 = M[1][2];
            double m20 = M[2][0], m21 = M[2][1], m22 = M[2][2];

            return m00 * m11 * m22 - m00 * m12 * m21 - m01 * m10 * m22 + m01
                    * m12 * m20 + m02 * m10 * m21 - m02 * m11 * m20;
        }

        if (n == 4) {
            double m00 = M[0][0], m01 = M[0][1], m02 = M[0][2], m03 = M[0][3];
            double m10 = M[1][0], m11 = M[1][1], m12 = M[1][2], m13 = M[1][3];
            double m20 = M[2][0], m21 = M[2][1], m22 = M[2][2], m23 = M[2][3];
            double m30 = M[3][0], m31 = M[3][1], m32 = M[3][2], m33 = M[3][3];

            return m00 * m11 * m22 * m33 - m00 * m11 * m23 * m32 - m00 * m12
                    * m21 * m33 + m00 * m12 * m23 * m31 + m00 * m13 * m21 * m32
                    - m00 * m13 * m22 * m31 - m01 * m10 * m22 * m33 + m01 * m10
                    * m23 * m32 + m01 * m12 * m20 * m33 - m01 * m12 * m23 * m30
                    - m01 * m13 * m20 * m32 + m01 * m13 * m22 * m30 + m02 * m10
                    * m21 * m33 - m02 * m10 * m23 * m31 - m02 * m11 * m20 * m33
                    + m02 * m11 * m23 * m30 + m02 * m13 * m20 * m31 - m02 * m13
                    * m21 * m30 - m03 * m10 * m21 * m32 + m03 * m10 * m22 * m31
                    + m03 * m11 * m20 * m32 - m03 * m11 * m22 * m30 - m03 * m12
                    * m20 * m31 + m03 * m12 * m21 * m30;
        }

        // use general version.
        Matrix MM = new Matrix(M);
        return MM.det();
    }

    /** Find the best plane fit to a set of points using SVD. **/
    public static double[] fitPlaneNormal(ArrayList<double[]> points) {
        double A[][] = new double[3][3];
        double M[] = new double[3];

        for (double p[] : points) {
            for (int i = 0; i < 3; i++)
                M[i] += p[i];
        }

        for (int i = 0; i < M.length; i++)
            M[i] /= points.size();

        for (double p[] : points) {
            double p0 = p[0] - M[0];
            double p1 = p[1] - M[1];
            double p2 = p[2] - M[2];

            A[0][0] += p0 * p0;
            A[0][1] += p0 * p1;
            A[0][2] += p0 * p2;
            A[1][1] += p1 * p1;
            A[1][2] += p1 * p2;
            A[2][2] += p2 * p2;
        }

        // make symmetric
        A[1][0] = A[0][1];
        A[2][0] = A[0][2];
        A[2][1] = A[1][2];

        if (Double.isNaN(A[0][0])) {
            return null;
        }

        Matrix V = new SingularValueDecomposition(new Matrix(A)).getV();
        return new double[] { V.get(0, 2), V.get(1, 2), V.get(2, 2) };
    }

    /** Fit y = Mx + b using weighted least-squares regression. Pass
     * in a list of {x, y, weight} (weight can be omitted).  Returns {
     * slope, offset, chi2. }
     *
     * Specifically, we solve the overdetermined system Jx = b, where
     * J = [ x1 1 ; x2 1; x3 1; ...], b = [ y1 ; y2; y3; ...], and x =
     * [M ; b] (the slope and offset of the line).
     *
     * Each row of J and b is weighted by that observation's value of
     * 'weight'. Weight corresponds to the standard deviation of the
     * uncertainty (square root of the variance).
     *
     * The least-squares solution is x = inv(J'J) J'b
     *
     * The chi^2 error is:
     *
     * chi^2 = (Jx-b)'(Jx-b) = x'J'Jx - 2x'J'b + b'b
     **/
    public static double[] fitLine(ArrayList<double[]> xyws) {
        double JtJ[][] = new double[2][2];
        double JtB[] = new double[2];

        double btb = 0;

        assert (xyws.size() >= 2);

        // For numerical stability, mean-shift all the
        // points. (Suppose p[0]s were large: we end up squaring
        // them.)
        double mx = 0, my = 0;

        for (double p[] : xyws) {
            mx += p[0];
            my += p[1];
        }
        mx /= xyws.size();
        my /= xyws.size();

        for (double p[] : xyws) {
            // weight
            double w2 = 1;
            if (p.length >= 3)
                w2 = p[2] * p[2];

            double px = p[0] - mx;
            double py = p[1] - my;

            JtJ[0][0] += w2 * px * px;
            JtJ[0][1] += w2 * px;
            JtJ[1][0] += w2 * px;
            JtJ[1][1] += w2;

            JtB[0] += w2 * px * py;
            JtB[1] += w2 * py;

            btb += w2 * py * py;
        }

        double JtJinv[][] = LinAlg.inverse(JtJ);
        double x[] = LinAlg.matrixAB(JtJinv, JtB);

        double chi2 = LinAlg.dotProduct(x, LinAlg.matrixAB(JtJ, x)) - 2
                * LinAlg.dotProduct(x, JtB) + btb;

        // fix up solution to account for mx and my. Only offset changes (slope stays the same).
        x[1] += my - x[0] * mx;

        /*
                if (false) {
                    // brute force chi2 computation. This can be more stable,
                    // but since we've introduced mx and my, we should be
                    // fine.
                    chi2 = 0;
                    for (double p[] : xyws) {
                        double ypred = p[0]*x[0] + x[1];
                        double w2 = 1;
                        if (p.length >= 3)
                            w2 = p[2]*p[2];

                        chi2 += w2*w2*(ypred-p[1])*(ypred-p[1]);
                    }
                    System.out.printf("%15f \n", chi2);
                }
        */

        return new double[] { x[0], x[1], chi2 };
    }

    /** compute the distance from xyz (in the direction dir) until it
     * collides with an axis-aligned box with dimensions sxyz,
     * centered at the origin. MAX_VALUE is returned if there is no collision. **/
    public static double rayCollisionBox(double xyz[], double dir[],
            double sxyz[]) {
        double u0 = -Double.MAX_VALUE;
        double u1 = Double.MAX_VALUE;

        // for what values of dist would each coordinate be in range?
        for (int i = 0; i < 3; i++) {
            if (dir[i] == 0) {
                if (Math.abs(xyz[i]) > sxyz[i] / 2)
                    return Double.MAX_VALUE;
                continue;
            }

            double a = (sxyz[i] / 2 - xyz[i]) / dir[i];
            double b = (-sxyz[i] / 2 - xyz[i]) / dir[i];

            u0 = Math.max(u0, Math.min(a, b));
            u1 = Math.min(u1, Math.max(a, b));
        }

        if (u1 < u0)
            return Double.MAX_VALUE; // no intersection

        if (u0 < 0) // intersection is in the opposite direction
            return Double.MAX_VALUE;

        return u0;
    }

    /** Let nd be the coefficients of the plane equation Ax + By + Cz
     * + D = 0. If the coordinate system is rotated by T, what is the
     * new plane equation?  **/
    public static double[] transformPlane(double T[][], double p[]) {
        // the normal of the plane is (A,B,C). Rotate that vector.
        // Then, add to D dotProduct(new-normal,translation)
        double newp[] = new double[4];

        for (int i = 0; i < 3; i++)
            newp[i] = T[i][0] * p[0] + T[i][1] * p[1] + T[i][2] * p[2];

        double dot = (newp[0] * T[0][3] + newp[1] * T[1][3] + newp[2] * T[2][3]);

        newp[3] = p[3] - dot;

        return newp;
    }

    public static ArrayList<double[]> transformPlanes(double T[][],
            ArrayList<double[]> planes) {
        ArrayList<double[]> newplanes = new ArrayList<double[]>();

        for (double p[] : planes)
            newplanes.add(transformPlane(T, p));

        return newplanes;
    }

    /** This function implements part of a convex hull collision test:
     * each face of the hull is described in terms of a plane equation:
     *
     * Ax + By + Cz + D = 0
     *
     * The sign of this equation must be configured so that the value
     * of the expression is negative on the inside of the hull and
     * positive on the other side.
     *
     * The plane and points must be in the same coordinate frame
     * @see #transformPlane(double[][], double[])

     *
     * We then test each of the points; if they are all on the
     * positive side of the plane, we return true.
     *
     * This is half of a convex hull collision test: for a full test,
     * you must try this in both directions.
     *
     * This method is a simplified version of
     * pointsOnWhichSideOfFace() method in order to speed up (by
     * returning early) when checking the planes of one hull to the
     * points of another.  In these cases, we know that any negative
     * value is a violation.
     * @param points an arraylist of 3D points (in coord frame A)
     * @param plane vector {A, B, C, D} (also in coord frame A)
     * @return boolean as true if all points are above plane.
     **/
    public static boolean pointsAbovePlane(ArrayList<double[]> points,
            double[] plane) {
        for (double p[] : points) {
            double v = plane[0] * p[0] + plane[1] * p[1] + plane[2] * p[2]
                    + plane[3];
            if (v <= 0)
                return false;
        }
        return true;
    }

    /** This function analyzes which side of a plane a set of points
     * resides where the plane is defined by the plane equation:
     *
     * Ax + By + Cz + D = 0
     *
     * The most common use case is for implementing part of a convex
     * hull collision test, where each face of the hull is described
     * in terms of a plane equation.
     *
     * An absence of a collision is defined as the presence of a plane
     * such that all the points of one hull are above the plane and
     * the points of the other hull are below this plane.  First check
     * the planes of the hull, with the simplified pointsAbovePlane()
     * function, but the full convex hull collision test requires also
     * checking additional planes (namely those with normals equal to
     * the cross-product of 2 edges, one from each hull).
     *
     * This function is a more general formulation of the
     * pointsAbovePlane() function, required for 6DOF hulls, and
     * returns one of three values {-1, 0, 1} depending on what side
     * of the face all the points are on.
     *
     * @param points an arraylist of 3D points (in coord frame A)
     * @param plane vector {A, B, C, D} (also in coord frame A)
     * @return {-1, 0, 1}: '1' when all points are above plane, '-1'
     * when all points are below the plane and '0' when points are
     * both above and below the plane.
     **/
    public static int pointsOnWhichSideOfPlane(ArrayList<double[]> points,
            double[] plane) {
        boolean pos = false;
        boolean neg = false;

        for (double p[] : points) {
            double v = plane[0] * p[0] + plane[1] * p[1] + plane[2] * p[2]
                    + plane[3];

            if (v > 0)
                pos = true;
            else if (v < 0)
                neg = true;
            if (pos && neg)
                return 0;
        }

        return (pos ? 1 : -1);
    }

    /** This function analyzes which side of a plane a set of points
     * resides, where the plane is defined by its normal and a point
     * on the plane.
     *
     * See @{link #pointsOnWhichSideOfPlane(ArrayList, double[])},
     * where the plane is defined by plane equation
     *
     * @param points an arraylist of 3D points (in coord. frame A)
     * @param normal vector for a surface (need not be unit length)
     * @param p is any point on the same surface as normal vector
     * comes from (in coord. frame A)
     * @return {-1, 0, 1}: '1' when all points are above plane, '-1'
     * when all points are below the plane and '0' when points are
     * both above and below the plane.
     **/
    public static int pointsOnWhichSideOfPlane(ArrayList<double[]> points,
            double[] normal, double[] p) {
        boolean pos = false;
        boolean neg = false;

        for (double[] point : points) {
            double v = dotProduct(subtract(point, p), normal);
            if (v > 0)
                pos = true;
            else if (v < 0)
                neg = true;
            if (pos && neg)
                return 0;
        }
        return (pos ? 1 : -1);
    }

    // factor 2x2 symmetric matrix S into L, such that S=L'L
    public static double[][] cholesky22(double S[][]) {
        double a = S[0][0];
        double b = S[0][1];
        double c = S[1][1];

        double sa = Math.sqrt(a);

        return new double[][] { { sa, b / sa }, { 0, Math.sqrt(c - b * b / a) } };
    }

    // compute determinant of small matrix. For large matrices an
    // alright method is to take product of diagonals of L and U
    public static double det33(double A[][]) {
        assert (A.length == 3 && A[0].length == 3);

        return -A[0][2] * A[1][1] * A[2][0] + A[0][1] * A[1][2] * A[2][0]
                + A[0][2] * A[1][0] * A[2][1] - A[0][0] * A[1][2] * A[2][1]
                - A[0][1] * A[1][0] * A[2][2] + A[0][0] * A[1][1] * A[2][2];
    }

    public static double det22(double A[][]) {
        assert (A.length == 2 && A[0].length == 2);

        return A[0][0] * A[1][1] - A[1][0] * A[0][1];
    }

    // computes A = U diag(sv) U^t in closed form. Derivation based on
    // computing minima/maxima wrt. t of v^tv where v = [a b; b d]* [cos(t), sin(t)]^t
    public static void svd22(double A[][],
    // Return vars:
            double sv[], double U[][]) {
        final double a = A[0][0];
        final double b = A[1][0];
        final double d = A[1][1];

        // Compute the theta which max/minimizes the multiplication against A
        double theta = Math.atan2(2 * b * (a + d), a * a - d * d) / 2;
        // note: There are only two distinct solutions for theta, one corresponding to the minimum eigenvalue
        // and another to the maximum. We don't care which one we get, since it is easy to compute one from the other.

        // Now we can compute the two perpendicular eigen vectors
        double eva[] = { Math.cos(theta), Math.sin(theta) };
        double evb[] = { -Math.sin(theta), Math.cos(theta) };

        // Compute the eigenvalues by multiplying the eigen vectors by A
        double evat[] = { a * eva[0] + b * eva[1], b * eva[0] + d * eva[1] };

        double evbt[] = { a * evb[0] + b * evb[1], b * evb[0] + d * evb[1] };
        double va = Math.sqrt((evat[0] * evat[0] + evat[1] * evat[1])
                / (eva[0] * eva[0] + eva[1] * eva[1]));
        double vb = Math.sqrt((evbt[0] * evbt[0] + evbt[1] * evbt[1])
                / (evb[0] * evb[0] + evb[1] * evb[1]));

        // sort the eigenvalues (& corresponding eigenvectors) by size
        if (va > vb) {
            sv[0] = va;
            sv[1] = vb;

            U[0][0] = eva[0];
            U[1][0] = eva[1];

            U[0][1] = evb[0];
            U[1][1] = evb[1];
        } else {
            sv[0] = vb;
            sv[1] = va;

            U[0][0] = evb[0];
            U[1][0] = evb[1];

            U[0][1] = eva[0];
            U[1][1] = eva[1];
        }
    }

}
