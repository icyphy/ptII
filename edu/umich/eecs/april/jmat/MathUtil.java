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

import java.util.Random;

import edu.umich.eecs.april.util.Tic;

/** Miscellaneous math utilities like mod2pi and fast exp functions. **/
public class MathUtil {
    static double epsilon = 0.000000001;
    static double twopi_inv = 0.5 / Math.PI;
    static double twopi = 2.0 * Math.PI;

    // only good for positive numbers.
    static private double mod2pi_pos(double vin) {
        double q = vin * twopi_inv + 0.5;
        int qi = (int) q;

        return vin - qi * twopi;
    }

    /** Ensure that v is [-PI, PI] **/
    static public double mod2pi(double vin) {
        double v;

        if (vin < 0)
            v = -mod2pi_pos(-vin);
        else
            v = mod2pi_pos(vin);

        // Validation test:
        //	if (v < -Math.PI || v > Math.PI)
        //		System.out.printf("%10.3f -> %10.3f\n", vin, v);

        return v;
    }

    /** Returns a value of v wrapped such that ref and v differ by no
     * more +/-PI
     **/
    static public double mod2pi(double ref, double v) {
        return ref + mod2pi(v - ref);
    }

    /** Returns true if the two doubles are within a small epsilon of
     * each other.
     **/
    static public boolean doubleEquals(double a, double b) {
        return Math.abs(a - b) < epsilon;
    }

    static public int clamp(int v, int min, int max) {
        if (v < min)
            v = min;
        if (v > max)
            v = max;
        return v;
    }

    static public double clamp(double v, double min, double max) {
        if (v < min)
            v = min;
        if (v > max)
            v = max;
        return v;
    }

    public static final double square(double x) {
        return x * x;
    }

    public static final int sign(double v) {
        if (v >= 0)
            return 1;
        return -1;
    }

    /** Quickly compute e^x for all x.

        Accuracy for x>0:
        x<0.5, absolute error < .0099
        x>0.5, relative error 0.36%

        For x<0, we internally compute the reciprocal form; error is
        magnified.

        This approximation is also monotonic.

     **/
    public static final double exp(double xin) {
        if (xin >= 0)
            return exp_pos(xin);

        return 1 / (exp_pos(-xin));
    }

    /** Quickly compute e^x for positive x.
     **/
    protected static final double exp_pos(double xin) {
        // our algorithm: compute 2^(x/log(2)) by breaking exponent
        // into integer and fractional parts.  The integer part can be
        // done with a bit shift operation. The fractional part, which
        // has bounded magnitude, can be computed with a polynomial
        // approximation. We then multiply together the two parts.

        // prevent deep recursion that would just return INF anyway...
        // e^709 > Double.MAX_VALUE;
        if (xin > 709)
            return Double.MAX_VALUE;

        if (xin > 43) // recursively handle values which would otherwise blow up.
        {
            // the value 43 was determined emperically
            return 4727839468229346561.4744575 * exp_pos(xin - 43);
        }

        double x = 1.44269504088896 * xin; // now we compute 2^x
        int wx = (int) x; // integer part
        double rx = x - wx; // fractional part

        rx *= 0.69314718055995; // scale fractional part by log(2)

        double b = 1L << wx; // 2^integer part
        double rx2 = rx * rx;
        double rx3 = rx2 * rx;
        double rx4 = rx3 * rx;

        double r = 1 + rx + rx2 / 2 + rx3 / 6 + rx4 / 24; // polynomial approximation for bounded rx.

        return b * r;
    }

    // returns [-PI,PI]
    // accurate within ~0.25 degrees
    public static final double atan2(double y, double x) {
        double atn = atan(y / x);

        if (y >= 0) {
            if (x >= 0)
                return atn;
            return Math.PI + atn;
        }
        if (x >= 0) {
            return atn;
        }
        return -Math.PI + atn;
    }

    /** returns [-PI/2, PI/2]
        accurate within 0.014 degrees
     **/
    public static final double atan(double x) {
        if (Math.abs(x) <= 1)
            return atan_mag1(x);
        if (x < 0)
            return -Math.PI / 2 - atan_mag1(1 / x);
        else
            return Math.PI / 2 - atan_mag1(1 / x);
    }

    // returns reasonable answers for |x|<=1.
    protected static final double atan_mag1(double x) {
        // accuracy = 0.26814 degrees
        //	return x/(1+0.28087207802773*x*x);

        if (true) {
            if (Math.abs(x) > 1)
                System.out.printf("ATAN_MAG1: %15f\n", x);

            final double p0 = -0.000158023363661;
            final double p1 = 1.003839939589617;
            final double p2 = -0.016224975245612;
            final double p3 = -0.343317496147292;
            final double p4 = 0.141501628812858;

            double a = Math.abs(x);
            double a2 = a * a;

            double y = p0 + p1 * a + p2 * a2 + p3 * (a2 * a) + p4 * (a2 * a2);

            if (x < 0)
                return -y;
            return y;
        } else {
            double xx = x * x;

            // accuracy = 0.10550 degrees (according to matlab)
            return (0.00182789418543 + 0.97687229491851 * x + 0.00087659977713 * xx)
                    / (0.99499024627366 + 0.00228262896304 * x + 0.25288677429562 * xx);
        }
    }

    strictfp public static void main(String args[]) {
        Random r = new Random();

        double err = 0;
        double M = 100;

        System.out.println("Max_double: " + Double.MAX_VALUE);

        System.out.println("Checking atan");

        for (int i = 0; i < 10000000; i++) {
            double x = M * r.nextDouble() - M / 2;
            double y = M * r.nextDouble() - M / 2;

            if (r.nextInt(100) == 0)
                x = 0;
            else if (r.nextInt(100) == 0)
                y = 0;

            double v1 = Math.atan2(y, x);
            double v2 = atan2(y, x);

            //		System.out.println(x+" "+y);
            double thiserr = Math.abs(v1 - v2);
            if (thiserr > .1)
                System.out.println(x + "\t" + y + "\t" + v1 + "\t" + v2);
            if (thiserr > err)
                err = thiserr;
        }
        System.out.println("err: " + err);
        System.out.println("err deg: " + Math.toDegrees(err));

        err = 0;
        M = 500;
        System.out.println("Checking exp");
        for (int i = 0; i < 10000000; i++) {
            double x = r.nextDouble() * M - M / 2;
            double v1 = Math.exp(x);
            double v2 = exp(x);

            double abserr = Math.abs(v1 - v2);
            double relerr = Math.abs((v2 - v1) / v1);

            if ((x < .5 && abserr > 0.01) || (x > .5 && relerr > 0.004))
                System.out.println(x + "\t" + v1 + "\t" + v2);
        }

        System.out.println("Benchmarking exp");
        //        benchexp();

        ///////////////////////////////////////////////
        System.out.println("Benchmarking atan");

        if (true) {
            double d[] = new double[10000];

            for (int i = 0; i < d.length; i++) {
                d[i] = r.nextDouble();
            }

            if (true) {
                Tic tic = new Tic();
                for (int i = 0; i < d.length; i++) {
                    for (int j = 0; j < d.length; j++) {
                        Math.atan2(d[i], d[j]);
                    }
                }
                System.out.printf("native: %15f\n", tic.toc());
            }

            if (true) {
                Tic tic = new Tic();
                for (int i = 0; i < d.length; i++) {
                    for (int j = 0; j < d.length; j++) {
                        atan2(d[i], d[j]);
                    }
                }
                System.out.printf("our version: %15f\n", tic.toc());
            }
        }

    }

    public static void benchexp() {
        Random r = new Random();
        long startTime, endTime;
        double elapsedTime;
        int iter = 100000000;
        startTime = System.currentTimeMillis();
        for (int i = 0; i < iter; i++)
            Math.exp(r.nextDouble() * 30);
        endTime = System.currentTimeMillis();
        elapsedTime = (endTime - startTime) / 1000f;
        System.out.println("Native: " + iter / elapsedTime);
        double nativeSpeed = iter / elapsedTime;

        startTime = System.currentTimeMillis();
        for (int i = 0; i < iter; i++)
            exp(r.nextDouble() * 30);
        endTime = System.currentTimeMillis();
        elapsedTime = (endTime - startTime) / 1000f;
        System.out.println("Fast: " + iter / elapsedTime);
        double fastSpeed = iter / elapsedTime;

        System.out.println("ratio: " + fastSpeed / nativeSpeed);

    }

    public static void benchatan() {
        Random r = new Random();
        long startTime, endTime;
        double elapsedTime;
        int iter = 100000000;
        startTime = System.currentTimeMillis();
        for (int i = 0; i < iter; i++)
            Math.atan2(r.nextDouble() * 30, r.nextDouble() * 30);
        endTime = System.currentTimeMillis();
        elapsedTime = (endTime - startTime) / 1000f;
        System.out.println("Native: " + iter / elapsedTime);
        double nativeSpeed = iter / elapsedTime;

        startTime = System.currentTimeMillis();
        for (int i = 0; i < iter; i++)
            atan2(r.nextDouble() * 30, r.nextDouble() * 30);
        endTime = System.currentTimeMillis();
        elapsedTime = (endTime - startTime) / 1000f;
        System.out.println("Fast: " + iter / elapsedTime);
        double fastSpeed = iter / elapsedTime;

        System.out.println("ratio: " + fastSpeed / nativeSpeed);
    }

    public static void inverse22(Matrix A) {
        double a = A.get(0, 0), b = A.get(0, 1);
        double c = A.get(1, 0), d = A.get(1, 1);

        double det = 1 / (a * d - b * c);
        A.set(0, 0, det * d);
        A.set(0, 1, -det * b);
        A.set(1, 0, -det * c);
        A.set(1, 1, det * a);
    }

    public static void inverse33(Matrix A) {
        double a = A.get(0, 0), b = A.get(0, 1), c = A.get(0, 2);
        double d = A.get(1, 0), e = A.get(1, 1), f = A.get(1, 2);
        double g = A.get(2, 0), h = A.get(2, 1), i = A.get(2, 2);

        double det = 1 / (a * e * i - a * f * h - d * b * i + d * c * h + g * b
                * f - g * c * e);

        A.set(0, 0, det * (e * i - f * h));
        A.set(0, 1, det * (-b * i + c * h));
        A.set(0, 2, det * (b * f - c * e));
        A.set(1, 0, det * (-d * i + f * g));
        A.set(1, 1, det * (a * i - c * g));
        A.set(1, 2, det * (-a * f + c * d));
        A.set(2, 0, det * (d * h - e * g));
        A.set(2, 1, det * (-a * h + b * g));
        A.set(2, 2, det * (a * e - b * d));
    }
}
