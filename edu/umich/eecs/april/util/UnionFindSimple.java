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

package edu.umich.eecs.april.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/** Implementation of disjoint set data structure that packs each
 * entry into a single array of 'int' for performance. *
 */
public final class UnionFindSimple {
    int data[]; // alternating arent ids, rank, size.

    static final int SZ = 2;

    /** @param maxid The maximum node id that will be referenced. **/
    public UnionFindSimple(int maxid) {
        data = new int[maxid * SZ];

        reset();
    }

    // initializes each node to it's cluster
    public void reset() {

        for (int i = 0; i < data.length / SZ; i++) {
            // everyone is their own cluster of size 1
            data[SZ * i + 0] = i;
            data[SZ * i + 1] = 1;
        }
    }

    public int size() {
        return data.length / SZ;
    }

    public int getSetSize(int id) {
        return data[SZ * getRepresentative(id) + 1];
    }

    public int getRepresentative(int id) {
        // terminal case: a node is its own parent.
        if (data[SZ * id] == id)
            return id;

        // otherwise, recurse...
        int root = getRepresentative(data[SZ * id]);

        // short circuit the path.
        data[SZ * id] = root;

        return root;
    }

    /** returns the id of the merged node. **/
    public int connectNodes(int aid, int bid) {
        int aroot = getRepresentative(aid);
        int broot = getRepresentative(bid);

        if (aroot == broot)
            return aroot;

        int asz = data[SZ * aroot + 1];
        int bsz = data[SZ * broot + 1];

        if (asz > bsz) {
            data[SZ * broot] = aroot;
            data[SZ * aroot + 1] += bsz;
            return aroot;
        } else {
            data[SZ * aroot] = broot;
            data[SZ * broot + 1] += asz;
            return broot;
        }
    }

    public static void main(String args[]) {
        int nedges = 100000;
        int nnodes = 1000;

        UnionFindSimple uf = new UnionFindSimple(nnodes);

        ArrayList<int[]> edges = new ArrayList<int[]>();
        Random r = new Random();

        for (int i = 0; i < nedges; i++) {
            int a = r.nextInt(nnodes);
            int b = r.nextInt(nnodes);

            edges.add(new int[] { a, b });

            uf.connectNodes(a, b);
        }

        System.out.println("");

        for (int a = 0; a < nnodes; a++) {

            // construct set of all reachable nodes.
            HashSet<Integer> reachable = new HashSet<Integer>();
            reachable.add(a);

            while (true) {
                int size0 = reachable.size();

                for (int edge[] : edges) {
                    if (reachable.contains(edge[0])) {
                        reachable.add(edge[1]);
                    }
                    if (reachable.contains(edge[1])) {
                        reachable.add(edge[0]);
                    }
                }

                if (reachable.size() == size0)
                    break;
            }

            for (int b = 0; b < nnodes; b++) {
                if (reachable.contains(b))
                    assert (uf.getRepresentative(a) == uf.getRepresentative(b));
                else
                    assert (uf.getRepresentative(a) != uf.getRepresentative(b));
            }

            assert (reachable.size() == uf.getSetSize(a));
        }
    }
}
