/*
@Copyright (c) 2003 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)


*/
package ptolemy.caltrop.util;

import caltrop.interpreter.Context;

import java.util.AbstractList;


//////////////////////////////////////////////////////////////////////////
//// IntegerList
/**
FIXME: What does this class do?
@author J&#246;rn W. Janneck <janneck@eecs.berkeley.edu>
@version $Id$
@since Ptolemy II 3.1
*/
public class IntegerList extends AbstractList {

    public IntegerList(Context context, int a, int b) {
        assert a <= b;

        _context = context;
        _a = a;
        _b = b;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public Object get(int n) {
        if (_a + n > _b) {
            throw new IndexOutOfBoundsException(
                _a + " + " +  n + " is greater than " + _b);
        }
        return _context.createInteger(_a + n);
    }

    public int  size() {
        return (_b - _a) + 1;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Context _context;
    private int  _a;
    private int  _b;
}
