/*
 @Copyright (c) 2003-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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
package ptolemy.caltrop.util;

import java.util.AbstractList;

import ptolemy.data.IntToken;
import caltrop.interpreter.Context;

///////////////////////////////////////////////////////////////////
//// IntegerList

/**
 A sparse list of integers.
 @author J&#246;rn W. Janneck
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class IntegerList extends AbstractList<IntToken> {
    /** Construct a list of Integers IntegerList object.
     *
     * @param context  a Caltrop interpreter context.
     * @param a        The lower limit of the range of integers.
     * @param b        The upper limit of the range of integers.
     */
    public IntegerList(Context context, int a, int b) {
        assert a <= b;

        _context = context;
        _a = a;
        _b = b;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return an integer from the Caltrop interpreter context.
     *  @param n The integer that is added to the a value and returned
     *  n+a must be less than or equal to b.
     *  @return the integer at slot a+n.
     */
    @Override
    public IntToken get(int n) {
        if (_a + n > _b) {
            throw new IndexOutOfBoundsException(_a + " + " + n
                    + " is greater than " + _b);
        }

        return (IntToken) _context.createInteger(_a + n);
    }

    /** The size of the list.
     *  @return The size of the list. (b-a)+1
     */
    @Override
    public int size() {
        return _b - _a + 1;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Context _context;

    private int _a;

    private int _b;
}
