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

/** Sparse vector implementation using a column-sorted row. **/
public class CSRVec extends Vec {
    public int length; // logical length of the vector

    public int indices[];
    public double values[];
    public int nz; // how many elements of indices/values are valid?

    int lastGetIdx, lastSetIdx;

    static final int MIN_SIZE = 16;

    public CSRVec(int length) {
        this(length, MIN_SIZE);
    }

    public CSRVec(int length, int capacity) {
        this.length = length;
        nz = 0;
        indices = new int[capacity];
        values = new double[capacity];
    }

    public final Vec copy() {
        CSRVec X = new CSRVec(length, indices.length);
        System.arraycopy(indices, 0, X.indices, 0, nz);
        System.arraycopy(values, 0, X.values, 0, nz);
        X.nz = nz;

        return X;
    }

    public final double[] copyArray() {
        return getDoubles();
    }

    public final double[] getDoubles() {
        double v[] = new double[length];
        for (int i = 0; i < nz; i++)
            v[indices[i]] = values[i];
        return v;
    }

    /** Remove any zeros that have snuck in. **/
    public final void filterZeros() {
        int outpos = 0;

        for (int inpos = 0; inpos < nz; inpos++) {
            if (values[inpos] != 0) {
                indices[outpos] = indices[inpos];
                values[outpos] = values[inpos];
                outpos++;
            }
        }

        nz = outpos;
    }

    /** Remove any values &lt; eps that have snuck in. **/
    public final void filterZeros(double eps) {
        int outpos = 0;

        for (int inpos = 0; inpos < nz; inpos++) {
            if (Math.abs(values[inpos]) > eps) {
                indices[outpos] = indices[inpos];
                values[outpos] = values[inpos];
                outpos++;
            }
        }

        nz = outpos;
    }

    // make the ith element correspond to the (idx,v) tuple, moving
    // anything after it as necessary. Position 'i' must be the
    // correct position for idx. 'idx' must not already be in the vec.
    final void insert(int i, int idx, double v) {
        if (v == 0)
            return;

        if (nz == indices.length)
            grow();

        System.arraycopy(indices, i, indices, i + 1, nz - i);
        System.arraycopy(values, i, values, i + 1, nz - i);

        indices[i] = idx;
        values[i] = v;
        nz++;
    }

    public final void resize(int newlength) {
        if (length <= newlength) {
            length = newlength;
            return;
        }

        while (nz > 0 && indices[nz - 1] >= newlength) {
            indices[nz - 1] = 0;
            values[nz - 1] = 0;
            nz--;
        }
    }

    final void sort() {
        // this is fairly inefficient if there are many changes (but
        // pretty reasonable if there's just one change, as in the
        // common case of an insert.
        for (int i = 1; i < nz; i++) {
            if (indices[i] < indices[i - 1]) {

                // do an insertion of i back somewhere.
                int itmp = indices[i];
                double vtmp = values[i];

                // find the insertion point
                int ipt = i;
                while (ipt >= 1 && itmp < indices[ipt - 1]) {
                    indices[ipt] = indices[ipt - 1];
                    values[ipt] = values[ipt - 1];
                    ipt--;
                }

                indices[ipt] = itmp;
                values[ipt] = vtmp;
            }
        }
    }

    public final void ensureCapacity(int mincapacity) {
        if (mincapacity > indices.length)
            grow(mincapacity);
    }

    final void grow() {
        grow(0);
    }

    final void grow(int mincapacity) {
        int newcapacity = indices.length * 2;
        while (newcapacity < mincapacity)
            newcapacity *= 2;

        int newindices[] = new int[newcapacity];
        double newvalues[] = new double[newcapacity];

        System.arraycopy(indices, 0, newindices, 0, nz);
        System.arraycopy(values, 0, newvalues, 0, nz);

        indices = newindices;
        values = newvalues;
    }

    public final CSRVec copy(int i0, int i1) {
        CSRVec X = new CSRVec(i1 - i0 + 1);

        int low = 0;
        while (low < nz && indices[low] < i0)
            low++;

        // XXX could be done with arraycopy by finding high.
        for (int i = low; i < nz && indices[i] <= i1; i++) {

            if (X.nz == X.indices.length)
                X.grow();

            X.indices[X.nz] = indices[i] - i0;
            X.values[X.nz] = values[i];
            X.nz++;
        }
        return X;
    }

    public final Vec copyPart(int i0, int i1) {
        CSRVec X = new CSRVec(length);

        int low = 0;
        while (low < nz && indices[low] < i0)
            low++;

        // XXX could be done with arraycopy by finding high.
        for (int i = low; i < nz && indices[i] <= i1; i++) {

            if (X.nz == X.indices.length)
                X.grow();

            X.indices[X.nz] = indices[i];
            X.values[X.nz] = values[i];
            X.nz++;
        }
        return X;
    }

    public final int size() {
        return length;
    }

    public final int getNz() {
        return nz;
    }

    // not thread safe. Maintain a cursor to help consecutive
    // accesses.
    public final double get(int idx) {
        if (nz == 0)
            return 0;

        if (lastGetIdx >= nz || lastGetIdx < 0)
            lastGetIdx = nz / 2;

        if (indices[lastGetIdx] < idx) {
            // search up
            while (lastGetIdx + 1 < nz && indices[lastGetIdx + 1] <= idx)
                lastGetIdx++;

            if (indices[lastGetIdx] == idx)
                return values[lastGetIdx];

            return 0;
        } else {
            // search down
            while (lastGetIdx - 1 >= 0 && indices[lastGetIdx - 1] >= idx)
                lastGetIdx--;

            if (indices[lastGetIdx] == idx)
                return values[lastGetIdx];

            return 0;
        }
    }

    // reference version of "get", without the cursor caching.
    public final double getRef(int idx) {
        for (int i = 0; i < nz && indices[i] <= idx; i++)
            if (indices[i] == idx)
                return values[i];
        return 0;
    }

    public final void set(int idx, double v) {
        if (lastSetIdx < 0 || lastSetIdx >= nz)
            lastSetIdx = nz / 2;

        if (nz == 0) {
            if (v == 0)
                return;
            indices[0] = idx;
            values[0] = v;
            nz = 1;
            return;
        }

        if (indices[lastSetIdx] == idx) {
            values[lastSetIdx] = v;
            return;
        }

        // search.
        if (indices[lastSetIdx] < idx) {
            // search up
            while (lastSetIdx + 1 < nz && indices[lastSetIdx + 1] <= idx)
                lastSetIdx++;

            if (indices[lastSetIdx] == idx) {
                values[lastSetIdx] = v;
                return;
            }
            insert(lastSetIdx + 1, idx, v);

        } else {
            // search down
            while (lastSetIdx - 1 >= 0 && indices[lastSetIdx - 1] >= idx)
                lastSetIdx--;

            if (indices[lastSetIdx] == idx) {
                values[lastSetIdx] = v;
                return;
            }

            insert(lastSetIdx, idx, v);
        }
    }

    public final void setRef(int idx, double v) {
        for (int i = 0; i < nz && indices[i] <= idx; i++) {
            if (indices[i] == idx) {
                values[i] = v;
                return;
            }
        }

        if (v == 0)
            return;

        if (nz == indices.length)
            grow();

        indices[nz] = idx;
        values[nz] = v;
        nz++;
        sort();
    }

    public final double dotProduct(Vec r) {
        double acc = 0;

        if (r instanceof CSRVec) {
            CSRVec a = this;
            CSRVec b = (CSRVec) r;

            int aidx = 0, bidx = 0;

            while (aidx < a.nz && bidx < b.nz) {
                int ai = a.indices[aidx], bi = b.indices[bidx];

                if (ai == bi) {
                    acc += a.values[aidx] * b.values[bidx];
                    aidx++;
                    bidx++;
                    continue;
                }

                if (ai < bi)
                    aidx++;
                else
                    bidx++;
            }

            return acc;
        }

        // default method
        for (int i = 0; i < nz; i++)
            acc += values[i] * r.get(indices[i]);

        return acc;
    }

    public final double dotProduct(Vec r, int i0, int i1) {
        double acc = 0;

        if (r instanceof CSRVec) {
            CSRVec a = this;
            CSRVec b = (CSRVec) r;

            int aidx = 0, bidx = 0;
            while (aidx < a.nz && a.indices[aidx] < i0)
                aidx++;
            while (bidx < b.nz && b.indices[bidx] < i0)
                bidx++;

            while (aidx < a.nz && bidx < b.nz) {
                int ai = a.indices[aidx], bi = b.indices[bidx];

                if (ai > i1 || bi > i1)
                    break;

                if (ai == bi) {
                    acc += a.values[aidx] * b.values[bidx];
                    aidx++;
                    bidx++;
                    continue;
                }

                if (ai < bi)
                    aidx++;
                else
                    bidx++;
            }

            return acc;
        }

        // default
        int low = 0;
        while (low < nz && indices[low] < i0)
            low++;

        for (int i = low; i < nz && indices[i] <= i1; i++)
            acc += values[i] * r.get(indices[i]);

        return acc;
    }

    public final void timesEquals(double scale) {
        for (int i = 0; i < nz; i++)
            values[i] *= scale;
    }

    public final void timesEquals(double scale, int i0, int i1) {
        int low = 0;
        while (low < nz && indices[low] < i0)
            low++;

        for (int i = low; i < nz && indices[i] <= i1; i++)
            values[i] *= scale;
    }

    public final void transposeAsColumn(Matrix A, int col) {
        for (int i = 0; i < nz; i++)
            A.set(indices[i], col, values[i]);
    }

    public final void transposeAsColumn(Matrix A, int col, int i0, int i1) {
        int low = 0;
        while (low < nz && indices[low] < i0)
            low++;

        for (int i = low; i < nz && indices[i] <= i1; i++)
            A.set(indices[i], col, values[i]);
    }

    public static final void add(CSRVec csra, double ascale, CSRVec csrb,
            int i0, int i1, CSRVec res) {
        assert (csra.length == csrb.length);

        int apos = 0, bpos = 0;

        while (apos < csra.nz || bpos < csrb.nz) {
            int thisidx = Integer.MAX_VALUE;
            if (apos < csra.nz)
                thisidx = csra.indices[apos];
            if (bpos < csrb.nz)
                thisidx = Math.min(thisidx, csrb.indices[bpos]);

            double vala = 0;
            double valb = 0;

            if (apos < csra.nz && csra.indices[apos] == thisidx) {
                vala = csra.values[apos];
                apos++;
            }
            if (bpos < csrb.nz && csrb.indices[bpos] == thisidx) {
                valb = csrb.values[bpos];
                bpos++;
            }

            if (thisidx < i0 || thisidx > i1)
                continue;

            double thisvalue = ascale * vala + valb;
            res.indices[res.nz] = thisidx;
            res.values[res.nz] = thisvalue;
            res.nz++;
        }
    }

    public final void addTo(Vec _r, double scale) {
        if (_r instanceof CSRVec) {
            CSRVec r = (CSRVec) _r;

            int ridx = 0;

            for (int i = 0; i < nz; i++) {
                while (ridx < r.nz && r.indices[ridx] < indices[i])
                    ridx++;

                if (ridx < r.nz && r.indices[ridx] == indices[i]) {
                    r.values[ridx] += values[i] * scale;
                } else {
                    r.insert(ridx, indices[i], values[i] * scale);
                }
            }

        } else {

            for (int i = 0; i < nz; i++)
                _r.plusEquals(indices[i], values[i] * scale);
        }
    }

    public final void addTo(Vec _r, double scale, int i0, int i1) {
        if (_r instanceof CSRVec) {
            CSRVec r = (CSRVec) _r;

            int low = 0;
            while (low < nz && indices[low] < i0)
                low++;

            int ridx = 0;

            for (int i = low; i < nz && indices[i] <= i1; i++) {
                while (ridx < r.nz && r.indices[ridx] < indices[i])
                    ridx++;

                if (ridx < r.nz && r.indices[ridx] == indices[i]) {
                    r.values[ridx] += values[i] * scale;
                } else {
                    r.insert(ridx, indices[i], values[i] * scale);
                }
            }

        } else {

            int low = 0;
            while (low < nz && indices[low] < i0)
                low++;

            for (int i = low; i < nz && indices[i] <= i1; i++)
                _r.plusEquals(indices[i], values[i] * scale);
        }
    }

    public final void plusEquals(int idx, double v) {
        for (int i = 0; i < nz; i++) {
            if (indices[i] == idx) {
                values[i] += v;
                return;
            }
        }

        // it's a new element
        set(idx, v);
    }

    public final void clear() {
        nz = 0;
    }

    public final double normF() {
        double acc = 0;

        for (int i = 0; i < nz; i++)
            acc += values[i] * values[i];

        return acc;
    }

    public final Vec copyPermuteColumns(Permutation p) {
        // XXX
        CSRVec X = new CSRVec(length, indices.length);

        for (int i = 0; i < nz; i++) {
            X.indices[i] = p.invperm[indices[i]];
            X.values[i] = values[i];
        }
        X.nz = nz;
        X.sort();

        return X;
    }
}
