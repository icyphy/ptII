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

/** Base type of all vectors (both sparse and dense). **/
public abstract class Vec {
    /** Return an array corresponding to the vector's elements. This
     * may or may not be the internal representation of the vector, so
     * callers should not modify this array.
     **/
    public abstract double[] getDoubles();

    /** How long is the vector? **/
    public abstract int size();

    /** How many non-zero entries are there? **/
    public abstract int getNz();

    /** Get the element at index idx **/
    public abstract double get(int idx);

    /** Set the element at index idx to v. */
    public abstract void set(int idx, double v);

    /** Compute the dot product with vector r **/
    public abstract double dotProduct(Vec r);

    // dot product from [i0, i1]
    public abstract double dotProduct(Vec r, int i0, int i1);

    /** Make a copy of the vector **/
    public abstract Vec copy();

    public abstract double[] copyArray();

    /** Resize the vector, truncating or adding zeros as appropriate. **/
    public abstract void resize(int newlength);

    /** create a new, smaller vector beginning at element i0, going
    through i1 (inclusive). The length of this vector will be i1-i0+1.
     **/
    public abstract Vec copy(int i0, int i1);

    /** create a same-sized vector containing only the spec'd elements. **/
    public abstract Vec copyPart(int i0, int i1);

    /** Multiply all elements in the vector by v **/
    public abstract void timesEquals(double v);

    /** Multiply the elements between indices [i0,i1] (inclusive) by
     * v **/
    public abstract void timesEquals(double v, int i0, int i1);

    /** Set all elements to zero. **/
    public abstract void clear();

    /** Add the value v to each element. **/
    public void plusEquals(int idx, double v) {
        set(idx, get(idx) + v);
    }

    /** Add the vector v to the elements beginning at index idx **/
    public void plusEquals(int idx, double v[]) {
        for (int i = 0; i < v.length; i++)
            set(idx + i, get(idx + i) + v[i]);
    }

    /** sum of squared elements. **/
    public abstract double normF();

    /** Insert this vector as column 'col' in matrix A. The column is
    initially all zero. The vector should iterate through its
    elements, calling the matrix's set method.
     **/
    public abstract void transposeAsColumn(Matrix A, int col);

    /** Transpose only the elements at indices [i0,i1] inclusive. **/
    public abstract void transposeAsColumn(Matrix A, int col, int i0, int i1);

    /** Add this vector (scaled) to another vector. This vector
    is unchanged.
     **/
    public abstract void addTo(Vec r, double scale);

    public abstract void addTo(Vec r, double scale, int i0, int i1);

    /** reorder the columns of this matrix so that they are:
    X' = [ X(perm[0]) X(perm[1]) X(perm[2])... ]
     **/
    public abstract Vec copyPermuteColumns(Permutation p);
}
