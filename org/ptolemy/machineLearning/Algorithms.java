/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 1995-2014 The Regents of the University of California.
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

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
*/
package org.ptolemy.machineLearning;

/**
 * Algorithms class.
 *
 * @author ilgea
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class Algorithms {

    private Algorithms() {
        // TODO Auto-generated constructor stub
    }
    /**
     * Do a binary interval search for the key in array A. The bin index in which
     * key is found is returned.
     * @param A The search array
     * @param key Key to be searched
     * @return the found key.
     */
    public static int _binaryIntervalSearch(double[] A, double key) {
        return _binaryIntervalSearch(A, key, 0, A.length-1);
    }
    public static int _binaryIntervalSearch(double[] A, double key, int imin, int imax) {
        if (imax < imin) {
            return -1;
        } else {
            int imid = imin + ((imax - imin) / 2);
            if (imid >= A.length - 1) {
                return -1;
            } else if (A[imid] <= key && A[imid + 1] > key) {
                return imid;
            } else if (A[imid] > key) {
                return _binaryIntervalSearch(A, key, imin, imid - 1);
            } else if (A[imid] < key) {
                return _binaryIntervalSearch(A, key, imid + 1, imax);
            } else {
                return imid;
            }
        }
    }

}
