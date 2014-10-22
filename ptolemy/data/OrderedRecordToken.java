/* A token that contains a set of label/token pairs - maintaining the original order.

 Copyright (c) 2009-2014 The Regents of the University of California.
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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// OrderedRecordToken

/**
 A token that contains a set of label/token pairs. Record labels may be
 arbitrary strings. Operations on record tokens result in new record tokens
 containing only the common fields, where the operation specifies how to
 combine the data in the common fields.  Thus, for example, if two record
 tokens are added or subtracted, then common records (those with the same
 labels) will be added or subtracted, and the disjoint records will not
 appear in the result.

 <p>This implementation maintains the order of the entries as they were added.

 @author Ben Leinfelder
 @version $Id$
 @since Ptolemy II 8.0
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
     *
     *  @param fieldMap A Map that has keys of type String and
     *  values of type Token.
     *  @exception IllegalActionException If the map contains null
     *  keys or values, or if it contains non-String keys or non-Token
     *  values.
     */
    public OrderedRecordToken(Map<String, Token> fieldMap)
            throws IllegalActionException {
        super(fieldMap);
    }

    /** Construct a RecordToken from the specified string.
     *  <p>Record labels that contain any non-Java identifier characters
     *  must be presented as a string i.e., surrounded with single or double
     *  quotes. Quotes within label strings must be escaped using a backslash.
     *  </p>
     *
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
     *
     *  @param labels An array of labels.
     *  @param values An array of Tokens.
     *  @exception IllegalActionException If the labels or the values array
     *   do not have the same length, or contains null element,
     *   or the labels array contains duplicate elements.
     */
    public OrderedRecordToken(String[] labels, Token[] values)
            throws IllegalActionException {
        super(labels, values);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the class of the argument is RecordToken, and
     *  the argument has the same set of labels as this token and the
     *  corresponding fields are equal, as determined by the equals
     *  method of the contained tokens. Order matters
     *  @param object An instance of Object.
     *  @return True if the argument is equal to this token.
     *  @see #hashCode()
     */
    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        // This test rules out instances of a subclass.
        if (object.getClass() != getClass()) {
            return false;
        }

        RecordToken recordToken = (RecordToken) object;

        try {
            if (_isEqualTo(recordToken) == BooleanToken.TRUE) {
                return true;
            }
        } catch (IllegalActionException ex) {
            return false;
        }

        return false;
    }

    /** Return a hash code value for this token. This method returns the xor
     *  of the hash codes of the labels and the element tokens.
     *  @return A hash code value for this token.
     */
    @Override
    public int hashCode() {
        int code = 0;
        Set<String> labelSet = _fields.keySet();
        Iterator<String> iterator = labelSet.iterator();

        while (iterator.hasNext()) {
            String label = iterator.next();
            Token token = get(label);
            code ^= label.hashCode();
            code ^= token.hashCode();
        }

        return code;
    }

    /** Return the value of this token as a string.
     *  The syntax is similar to that of a record, but using square braces
     *  instead of curly braces,
     *  <code>[<i>label</i> = <i>value</i>, <i>label</i> = <i>value</i>, ...]</code>
     *  The record fields are listed in the their original order
     *  <p>Record labels that contain any non-Java identifier characters
     *  are surrounded with double quotes. Quotes within label strings are
     *  escaped using a backslash.
     *  </p>
     *
     *  @return A String beginning with "[" that contains label and value
     *  pairs separated by commas, ending with "]".
     */
    @Override
    public String toString() {
        Object[] labelsObjects = _fields.keySet().toArray();

        // construct the string representation of this token.
        StringBuffer stringRepresentation = new StringBuffer("[");

        int size = labelsObjects.length;
        for (int i = 0; i < size; i++) {
            String label = (String) labelsObjects[i];
            String value = get(label).toString();

            if (i != 0) {
                stringRepresentation.append(", ");
            }

            // quote and escape labels that are not valid Java identifiers
            if (!StringUtilities.isValidIdentifier(label)) {
                label = "\"" + StringUtilities.escapeString(label) + "\"";
            }
            stringRepresentation.append(label + " = " + value);
        }

        return stringRepresentation.toString() + "]";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * @see RecordToken
     */
    @Override
    protected RecordToken _createRecordToken(String[] labels, Token[] values)
            throws IllegalActionException {
        return new OrderedRecordToken(labels, values);
    }

    /**  Initialize the storage used by this token.  OrderedRecordToken
     *   uses a LinkedHashMap so that the original order of the record
     *   is maintained.
     */
    @Override
    protected void _initializeStorage() {
        _fields = new LinkedHashMap<String, Token>();
    }

    /**
     * Create a Set implementation appropriate for operations on this RecordToken.
     * Here we are using an ordered set.
     * @return a new Set.
     */
    @Override
    protected Set<String> _createSet() {
        return new LinkedHashSet<String>();
    }

    /** Return true if the specified token is equal to this one.
     *  Equal means that both tokens have the same labels with the
     *  same values.  This method is different from equals() in that
     *  _isEqualTo() looks for equalities of values irrespective of
     *  their types.  It is assumed that the type of the argument is
     *  RecordToken.
     *  @param rightArgument The token to compare to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return True if the argument is equal to this.
     */
    @Override
    protected BooleanToken _isEqualTo(Token rightArgument)
            throws IllegalActionException {
        RecordToken recordToken = (RecordToken) rightArgument;

        Set<String> myLabelSet = _fields.keySet();
        Set<String> argLabelSet = recordToken._fields.keySet();

        if (!myLabelSet.equals(argLabelSet)) {
            return BooleanToken.FALSE;
        }

        Iterator<String> iterator = myLabelSet.iterator();
        Iterator<String> argIterator = argLabelSet.iterator();

        while (iterator.hasNext()) {
            String label = iterator.next();
            String argLabel = argIterator.next();

            // labels match
            if (!label.equals(argLabel)) {
                return BooleanToken.FALSE;
            }

            Token token1 = get(label);
            Token token2 = recordToken.get(argLabel);

            // tokens match
            BooleanToken result = token1.isEqualTo(token2);
            if (result.booleanValue() == false) {
                return BooleanToken.FALSE;
            }
        }

        return BooleanToken.TRUE;
    }

}
