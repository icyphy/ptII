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

/** Default "dense" vector implementation backed by a simple array. **/
public class DenseVec extends Vec {
    double v[];

    public DenseVec(int length) {
        v = new double[length];
    }

    public DenseVec(double v[]) {
        this.v = v;
    }

    public final Vec copy() {
        DenseVec X = new DenseVec(v.length);
        for (int i = 0; i < v.length; i++)
            X.v[i] = v[i];
        return X;
    }

    public final double[] copyArray() {
        return LinAlg.copy(v);
    }

    public final Vec copy(int i0, int i1) {
        DenseVec X = new DenseVec(i1 - i0 + 1);
        for (int i = 0; i < X.v.length; i++)
            X.v[i] = v[i0 + i];
        return X;
    }

    public final Vec copyPart(int i0, int i1) {
        DenseVec X = new DenseVec(v.length);
        for (int i = i0; i <= i1; i++)
            X.v[i] = v[i];
        return X;
    }

    public final void resize(int newlength) {
        double newv[] = new double[newlength];
        for (int i = 0; i < Math.min(newlength, v.length); i++)
            newv[i] = v[i];
        v = newv;
    }

    public final double[] getDoubles() {
        return v;
    }

    public final int size() {
        return v.length;
    }

    public final int getNz() {
        return v.length;
    }

    public final double get(int idx) {
        return v[idx];
    }

    public final void set(int idx, double value) {
        v[idx] = value;
    }

    public final double dotProduct(Vec r) {
        if (r.getNz() < getNz())
            return r.dotProduct(this);

        double acc = 0;
        for (int i = 0; i < v.length; i++) {
            acc += v[i] * r.get(i);
        }

        return acc;
    }

    public final double dotProduct(Vec r, int i0, int i1) {
        if (r.getNz() < getNz())
            return r.dotProduct(this, i0, i1);

        double acc = 0;
        for (int i = i0; i <= i1; i++) {
            acc += v[i] * r.get(i);
        }

        return acc;
    }

    public final void timesEquals(double scale) {
        for (int i = 0; i < v.length; i++)
            v[i] *= scale;
    }

    public final void timesEquals(double scale, int i0, int i1) {
        for (int i = i0; i <= i1; i++)
            v[i] *= scale;
    }

    public final void transposeAsColumn(Matrix A, int col) {
        for (int i = 0; i < v.length; i++)
            A.set(i, col, v[i]);
    }

    public final void transposeAsColumn(Matrix A, int col, int i0, int i1) {
        for (int i = i0; i <= i1; i++)
            A.set(i, col, v[i]);
    }

    public final void addTo(Vec r, double scale) {
        for (int i = 0; i < v.length; i++)
            r.set(i, r.get(i) + v[i] * scale);
    }

    public final void addTo(Vec r, double scale, int i0, int i1) {
        for (int i = i0; i <= i1; i++)
            r.set(i, r.get(i) + v[i] * scale);
    }

    public final void clear() {
        for (int i = 0; i < v.length; i++)
            v[i] = 0;
    }

    public final double normF() {
        double acc = 0;
        for (int i = 0; i < v.length; i++)
            acc += v[i] * v[i];

        return acc;
    }

    public final Vec copyPermuteColumns(Permutation p) {
        DenseVec X = new DenseVec(v.length);

        for (int i = 0; i < v.length; i++) {
            X.set(i, v[p.perm[i]]);
        }

        return X;
    }
}
