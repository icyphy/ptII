/*
 @Copyright (c) 2003-2013 The Regents of the University of California.
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

import ptolemy.data.ArrayToken;
import ptolemy.data.Token;

///////////////////////////////////////////////////////////////////
//// PtArrayList

/**
 A List representation of an ArrayToken.
 @author J&#246;rn W. Janneck
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class PtArrayList extends AbstractList<Token> {
    /** Construct a PtArrayList from an ArrayToken.
     *  @param arrayToken The arrayToken with which to construct the List.
     */
    public PtArrayList(ArrayToken arrayToken) {
        _arrayToken = arrayToken;
    }

    /** Get the token at the given index.  The type of the return
     *  @param index The index of the desired element.
     *  @return The token contained in this array token at the
     *  specified index.
     *  @exception ArrayIndexOutOfBoundException If the specified index is
     *   outside the range of the token array.
     */
    public Token get(int index) {
        return _arrayToken.getElement(index);
    }

    /** Return the size of the array.
     *  @return size The length of the ArrayToken.
     */
    public int size() {
        return _arrayToken.length();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private ArrayToken _arrayToken;
}
