/* A token that contains a set of label/token pairs - maintaining the original order.

 Copyright (c) 2009 The Regents of the University of California.
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

import java.util.LinkedHashMap;
import java.util.Map;

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// OrderedRecordToken

/**
 A token that contains a set of label/token pairs. Operations on record
 tokens result in new record tokens containing only the common fields,
 where the operation specifies how to combine the data in the common
 fields.  Thus, for example, if two record tokens
 are added or subtracted, then common records
 (those with the same labels) will be added or subtracted,
 and the disjoint records will not appear in the result.

 This implementation maintains the order of the entries as they were added

 @author Ben Leinfelder
@version $Id$
@since Ptolemy II 7.1
 @version $Id$
 @Pt.ProposedRating yellow (leinfelder)
 @Pt.AcceptedRating red (leinfelder)
 */
public class OrderedRecordToken extends RecordToken {

    /** Construct an OrderedRecordToke with now fields.
     * @see RecordToken
     */
    public OrderedRecordToken() {
        super();
    }

    /** Construct an OrderedRecordToken with the labels and values specified
     *  by a given Map object. The object cannot contain any null keys
     *  or values.
     *  @param fieldMap A Map that has keys of type String and
     *  values of type Token.
     *  @exception IllegalActionException If the map contains null
     *  keys or values, or if it contains non-String keys or non-Token
     *  values.
     */
     public OrderedRecordToken(Map fieldMap) throws IllegalActionException {
     super(fieldMap);
     }

    /** Construct a RecordToken from the specified string.
     *  @param init A string expression of a record.
     *  @exception IllegalActionException If the string does not
     *  contain a parsable record.
     */
    public OrderedRecordToken(String init) throws IllegalActionException {
        super(init);
    }

    /** Construct an OrderedRecordToken with the specified labels and values.
     *  The labels and values arrays must have the same length, and have one
     *  to one correspondence with each other.  That is, the i'th entry in
     *  the labels array is the label for the i'th value in the values array.
     *  If both arrays are empty, this creates an empty record token.
     *  @param labels An array of labels.
     *  @param values An array of Tokens.
     *  @exception IllegalActionException If the labels or the values array
     *   do not have the same length, or contains null element,
     *   or the labels array contains duplicate elements.
     */
    public OrderedRecordToken(String[] labels, Token[] values) throws IllegalActionException {
        super(labels, values);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**  Intialize the storage used by this token.  OrderedRecordToken
     *   uses a LinkedHashMap so that the original order of the record
     *   is maintained.
     */
    protected void _initializeStorage() {
            _fields = new LinkedHashMap();
    }

}
