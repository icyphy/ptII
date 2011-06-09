/*

 Copyright (c) 2005 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.backtrack.test.array1;

public class ArrayTest1 {
    public ArrayTest1() {
        init();
    }

    public void dump() {
        for (int i = 0; i < _length1; i++) {
            for (int j = 0; j < _length2; j++) {
                System.out.print(_formatInteger(_buf[i][j], 4));
            }

            System.out.println();
        }
    }

    public void init() {
        _buf = new int[_length1][_length2];

        for (int i = 0, k = 0; i < _length1; i++) {
            for (int j = 0; j < _length2; j++, k++) {
                _buf[i][j] = k;
            }
        }
    }

    public void modify() {
        int[][] newBuf = new int[_length1][_length2];

        for (int i = 0, k = 0; i < _length1; i++) {
            for (int j = 0; j < _length2; j++, k++) {
                newBuf[i][j] = (_length1 * _length2) - k - 1;
            }
        }

        System.arraycopy(newBuf, 0, _buf, 0, _length1);
    }

    private String _formatInteger(int i, int length) {
        StringBuffer buf = new StringBuffer();
        buf.append(Integer.toString(i));

        int len = buf.length();

        while (len++ < length) {
            buf.insert(0, ' ');
        }

        return buf.toString();
    }

    private int[][] _buf;

    private final int _length1 = 10;

    private final int _length2 = 20;
}
