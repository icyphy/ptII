/* A token that contains an array of tokens.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.data;

import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

///////////////////////////////////////////////////////////////////
//// UpdatedArrayToken

/**
 A token that contains an array of tokens that is equal to another
 specified ArrayToken except at one location, where it has a new
 value. This implementation keeps a reference to the other specified
 token and records only the diff between this modified array and the
 original one.
 <p>
 This technique is inspired by a similar technique of storing diffs
 described by Michael Isard in a talk at Berkeley in February, 2012.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Red (cxh) nil token code
 */
public class UpdatedArrayToken extends ArrayToken {
    /** Construct an UpdatedArrayToken that is equal to the specified
     *  <i>baseToken</i>, except at <i>index</i>, where its value is
     *  <i>newValue</i>.
     *  The type of the resulting array type is the least upper bound
     *  of the types of the elements, which is not necessarily the same
     *  as the type of the base token.
     *  @param baseToken The base array.
     *  @param index The index of the new value.
     *  @param newValue The updated value.
     *  @exception IllegalActionException If the index is out of range
     *   for the base token.
     */
    public UpdatedArrayToken(ArrayToken baseToken, int index, Token newValue)
            throws IllegalActionException {
        super(baseToken.getElementType());
        if (index >= baseToken.length()) {
            throw new IllegalActionException("Index " + index + " is out of "
                    + "range for the array of length " + baseToken.length());

        }
        _baseToken = baseToken;
        _index = index;
        _newValue = newValue;

        Type newValueType = newValue.getType();
        Type baseType = baseToken.getElementType();
        _elementType = TypeLattice.leastUpperBound(newValueType, baseType);
        _newValue = _elementType.convert(_newValue);

        _depth = _baseToken._depth + 1;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    // FindBugs says "Class doesn't override equals in superclass," but
    // that's ok here.  See the tests in test/UpdatedArrayToken.tcl

    /** Return the element at the specified index.
     *  @param index The index of the desired element.
     *  @return The token contained in this array token at the
     *  specified index.
     *  @exception ArrayIndexOutOfBoundsException If the specified index is
     *   outside the range of the token array.
     */
    @Override
    public Token getElement(int index) {
        if (index == _index) {
            return _newValue;
        }
        try {
            return _elementType.convert(_baseToken.getElement(index));
        } catch (IllegalActionException e) {
            // This should not happen because _elementType is an upper bound.
            throw new InternalErrorException(e);
        }
    }

    /** Return the length of the contained token array.
     *  @return The length of the contained token array.
     */
    @Override
    public int length() {
        return _baseToken.length();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The base array. */
    private ArrayToken _baseToken;

    /** The index of the updated value. */
    private int _index;

    /** The one updated value. */
    private Token _newValue;
}
