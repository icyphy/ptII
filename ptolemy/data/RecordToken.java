/* A token that contains a set of label/token pairs.

 Copyright (c) 1997-2003 The Regents of the University of California.
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

@ProposedRating Green (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)

*/

package ptolemy.data;

import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.PtParser;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

//////////////////////////////////////////////////////////////////////////
//// RecordToken
/**
A token that contains a set of label/token pairs. Operations on record
tokens result in new record tokens containing only the common fields,
where the operation specifies how to combine the data in the common
fields.  Thus, for example, if two record tokens
are added or subtracted, then common records
(those with the same labels) will be added or subtracted,
and the disjoint records will not appear in the result.

@author Yuhong Xiong, Steve Neuendorffer, Elaine Cheong, Edward Lee
@version $Id$
@since Ptolemy II 1.0
*/

public class RecordToken extends AbstractNotConvertibleToken {

    /** Construct a RecordToken with the specified labels and values.
     *  The labels and values arrays must have the same length, and have one
     *  to one correspondence with each other.  That is, the i'th entry in
     *  the labels array is the label for the i'th value in the values array.
     *  Both arrays must be non-empty.
     *  @param labels An array of labels.
     *  @param values An array of Tokens.
     *  @exception IllegalActionException If the labels or the values array
     *   do not have the same length, or is empty, or contains null element,
     *   or the labels array contains duplicate elements.
     */
    public RecordToken(String[] labels, Token[] values)
            throws IllegalActionException {
        _initialize(labels, values);
    }

    /** Construct a RecordToken from the specified string.
     *  @param init A string expression of a record.
     *  @exception IllegalActionException If the string does not
     *  contain a parsable record.
     */
    public RecordToken(String init) throws IllegalActionException {
        PtParser parser = new PtParser();
        ASTPtRootNode tree = parser.generateParseTree(init);

        ParseTreeEvaluator evaluator = new ParseTreeEvaluator();
        Token token = evaluator.evaluateParseTree(tree);

        if (token instanceof RecordToken) {
            RecordToken recordToken = (RecordToken)token;
            Object[] labelObjects = recordToken.labelSet().toArray();
            String[] labels = new String[labelObjects.length];
            Token[] values = new Token[labelObjects.length];
            for (int i = 0; i < labelObjects.length; i++) {
                labels[i] = (String)labelObjects[i];
                values[i] = recordToken.get(labels[i]);
            }
            _initialize(labels, values);
        } else {
            throw new IllegalActionException("A record token cannot be"
                    + " created from the expression '" + init + "'");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the class of the argument is RecordToken, and
     *  the argument has the same set of labels as this token and the
     *  corresponding fields are equal, as determined by the equals
     *  method of the contained tokens.
     *  @param object An instance of Object.
     *  @return True if the argument is equal to this token.
     *  @see #hashCode()
     */
    public boolean equals(Object object) {
        // This test rules out instances of a subclass.
        if (object.getClass() != getClass()) {
            return false;
        }

        RecordToken recordToken = (RecordToken)object;

        Set myLabelSet = _fields.keySet();
        Set argLabelSet = recordToken._fields.keySet();
        if ( !myLabelSet.equals(argLabelSet)) {
            return false;
        }

        Iterator iterator = myLabelSet.iterator();
        while (iterator.hasNext()) {
            String label = (String)iterator.next();
            Token token1 = get(label);
            Token token2 = recordToken.get(label);
            if ( !token1.equals(token2)) {
                return false;
            }
        }

        return true;
    }

    /** Return the token with the specified label. If this token does not
     *  contain the specified label, return null.
     *  @param label A String label.
     *  @return A Token.
     */
    public Token get(String label) {
        return (Token)_fields.get(label);
    }

    /** Return the type of this token.
     *  @return An instance of RecordType.
     */
    public Type getType() {
        Object[] labelsObjects = _fields.keySet().toArray();
        int size = labelsObjects.length;
        String[] labels = new String[size];
        Type[] types = new Type[size];

        for (int i = 0; i < size; i++) {
            labels[i] = (String)labelsObjects[i];
            types[i] = get(labels[i]).getType();
        }

        return new RecordType(labels, types);
    }

    /** Return a hash code value for this token. This method returns the sum
     *  of the hash codes of the element tokens.
     *  @return A hash code value for this token.
     */
    public int hashCode() {
        int code = 0;
        Set labelSet = _fields.keySet();
        Iterator iterator = labelSet.iterator();
        while (iterator.hasNext()) {
            String label = (String)iterator.next();
            Token token = get(label);
            code += token.hashCode();
        }

        return code;
    }

    /** Return the labels of this token as a Set.
     *  @return A Set containing labels.
     */
    public Set labelSet() {
        return _fields.keySet();
    }

    /** Return the length of this token.
     *  @return The length of this token, which is greater than or equal
     *   to zero.
     */
    public int length() {
        return _fields.size();
    }

    /** Return a new token created by merging the two specified tokens,
     *  where preference is given to the first token when field labels
     *  are the same.
     *  @param token1 The higher priority record token.
     *  @param token2 The lower priority record token.
     *  @return A new RecordToken.
     */
    public static RecordToken merge(RecordToken token1, RecordToken token2) {
        Set unionSet = new HashSet();
        Set labelSet1 = token1._fields.keySet();
        Set labelSet2 = token2._fields.keySet();
        unionSet.addAll(labelSet1);
        unionSet.addAll(labelSet2);

        Object[] labelsObjects = unionSet.toArray();
        int size = labelsObjects.length;
        String[] labels = new String[size];
        Token[] values = new Token[size];
        for (int i = 0; i < size; i++) {
            labels[i] = (String)labelsObjects[i];
            Token value1 = token1.get(labels[i]);
            if (value1 != null) {
                values[i] = value1;
            } else {
                values[i] = token2.get(labels[i]);
            }
        }
        try {
            return new RecordToken(labels, values);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex);
        }
    }

    /** Return the (exact) return type of the merge function above.
     *  If the arguments are both record type, then return a record
     *  type that contains all of the fields (and types) of the first
     *  record, and all of the fields of the second record that are
     *  not in the first record,  otherwise return BaseType.UNKNOWN.
     *  @param type1 The type of the first argument to the
     *  corresponding function.
     *  @param type2 The type of the second argument to the
     *  corresponding function.
     *  @return The type of the value returned from the corresponding function.
     */
    public static Type mergeReturnType(Type type1, Type type2) {
        if (type1 instanceof RecordType &&
                type2 instanceof RecordType) {
            RecordType recordType1 = (RecordType)type1;
            RecordType recordType2 = (RecordType)type2;

            Set unionSet = new HashSet();
            Set labelSet1 = recordType1.labelSet();
            Set labelSet2 = recordType2.labelSet();
            unionSet.addAll(labelSet1);
            unionSet.addAll(labelSet2);

            Object[] labelsObjects = unionSet.toArray();
            int size = labelsObjects.length;
            String[] labels = new String[size];
            Type[] types = new Type[size];
            for (int i = 0; i < size; i++) {
                labels[i] = (String)labelsObjects[i];
                Type fieldType = recordType1.get(labels[i]);
                if (fieldType != null) {
                    types[i] = fieldType;
                } else {
                    types[i] = recordType2.get(labels[i]);
                }
            }
            return new RecordType(labels, types);
        } else {
            return BaseType.UNKNOWN;
        }
    }

    /** Returns a new RecordToken representing the multiplicative identity.
     *  The returned token has the same set of labels as this one, and
     *  each field contains the multiplicative identity of the corresponding
     *  field of this token.
     *  @return A RecordToken.
     *  @exception IllegalActionException If multiplicative identity is not
     *   supported by any element token.
     */
    public Token one() throws IllegalActionException {
        Object[] labelsObjects = _fields.keySet().toArray();
        int size = labelsObjects.length;
        String[] labels = new String[size];
        Token[] values = new Token[size];

        for (int i = 0; i < size; i++) {
            labels[i] = (String)labelsObjects[i];
            values[i] = get(labels[i]).one();
        }
        return new RecordToken(labels, values);
    }

    /** Return the value of this token as a string.
     *  The syntax is similar to the ML record:
     *  {<label> = <value>, <label> = <value>, ...}
     *  The record fields are listed in the lexicographical order of the
     *  labels determined by the java.lang.String.compareTo() method.
     *  @return A String beginning with "{" that contains label and value
     *  pairs separated by commas, ending with "}".
     */
    public String toString() {
        Object[] labelsObjects = _fields.keySet().toArray();
        // order the labels
        int size = labelsObjects.length;
        for (int i = 0; i < size-1; i++) {
            for (int j = i + 1; j < size; j++) {
                String labeli = (String)labelsObjects[i];
                String labelj = (String)labelsObjects[j];
                if (labeli.compareTo(labelj) >= 0) {
                    Object temp = labelsObjects[i];
                    labelsObjects[i] = labelsObjects[j];
                    labelsObjects[j] = temp;
                }
            }
        }

        // construct the string representation of this token.
        String stringRepresentation = "{";
        for (int i = 0; i < size; i++) {
            String label = (String)labelsObjects[i];
            String value = get(label).toString();
            if (i != 0) {
                stringRepresentation += ", ";
            }
            stringRepresentation += label + "=" + value;
        }
        return stringRepresentation + "}";
    }

    /** Returns a new RecordToken representing the additive identity.
     *  The returned token has the same set of labels as this one, and
     *  each field contains the additive identity of the corresponding
     *  field of this token.
     *  @return A RecordToken.
     *  @exception IllegalActionException If additive identity is not
     *   supported by any element token.
     */
    public Token zero() throws IllegalActionException {
        Object[] labelsObjects = _fields.keySet().toArray();
        int size = labelsObjects.length;
        String[] labels = new String[size];
        Token[] values = new Token[size];

        for (int i = 0; i < size; i++) {
            labels[i] = (String)labelsObjects[i];
            values[i] = get(labels[i]).zero();
        }
        return new RecordToken(labels, values);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a new token whose value is the field-wise addition of
     *  this token and the argument. It is assumed that the class of
     *  the argument is RecordToken.
     *  @param rightArgument The token to add to this token.
     *  @return A new RecordToken.
     *  @exception IllegalActionException If calling the add method on
     *  one of the record fields throws it.
     */
    protected Token _add(Token rightArgument)
            throws IllegalActionException {
        RecordToken recordToken = (RecordToken)rightArgument;

        Set intersectionSet = new HashSet();
        intersectionSet.addAll(_fields.keySet());
        intersectionSet.retainAll(recordToken._fields.keySet());

        Iterator labels = intersectionSet.iterator();
        int size = intersectionSet.size();
        String[] newLabels = new String[size];
        Token[] newValues = new Token[size];
        int i = 0;
        while (labels.hasNext()) {
            String label = (String)labels.next();
            Token token1 = get(label);
            Token token2 = recordToken.get(label);

            newLabels[i] = label;
            newValues[i] = token1.add(token2);

            i++;
        }
        return new RecordToken(newLabels, newValues);
    }

    /** Return a new token whose value is the field-wise division of
     *  this token and the argument. It is assumed that the class of
     *  the argument is RecordToken.
     *  @param rightArgument The token to divide this token by.
     *  @return A new RecordToken.
     *  @exception IllegalActionException If calling the divide method on
     *  one of the record fields throws it.
     */
    protected Token _divide(Token rightArgument)
            throws IllegalActionException {
        RecordToken recordToken = (RecordToken)rightArgument;

        Set intersectionSet = new HashSet();
        intersectionSet.addAll(_fields.keySet());
        intersectionSet.retainAll(recordToken._fields.keySet());

        Iterator labels = intersectionSet.iterator();
        int size = intersectionSet.size();
        String[] newLabels = new String[size];
        Token[] newValues = new Token[size];
        int i = 0;
        while (labels.hasNext()) {
            String label = (String)labels.next();
            Token token1 = get(label);
            Token token2 = recordToken.get(label);

            newLabels[i] = label;
            newValues[i] = token1.divide(token2);

            i++;
        }
        return new RecordToken(newLabels, newValues);
    }

    /** Test whether the value of this token is close to the first argument,
     *  where "close" means that the distance between them is less than
     *  or equal to the second argument.  This method only makes sense
     *  for tokens where the distance between them is reasonably
     *  represented as a double. It is assumed that the argument is
     *         an RecordToken, and the isCloseTo() method of the fields is
     *         is used.  If the fields do not match, then the return value is
     *         false.
     *  @param rightArgument The token to compare to this token.
     *  @param epsilon The value that we use to determine whether two
     *   tokens are close.
     *  @return A token containing true if the value of the first
     *   argument is close to the value of this token.
     */
    protected BooleanToken _isCloseTo(Token rightArgument, double epsilon)
            throws IllegalActionException {
        RecordToken recordToken = (RecordToken)rightArgument;

        Set myLabelSet = _fields.keySet();
        Set argLabelSet = recordToken._fields.keySet();
        if ( !myLabelSet.equals(argLabelSet)) {
            return BooleanToken.FALSE;
        }
        // Loop through all of the fields, checking each one for closeness.
        Iterator iterator = myLabelSet.iterator();
        while (iterator.hasNext()) {
            String label = (String)iterator.next();
            Token token1 = get(label);
            Token token2 = recordToken.get(label);
            BooleanToken result = token1.isCloseTo(token2, epsilon);
            if (result.booleanValue() == false) {
                return BooleanToken.FALSE;
            }
        }
        return BooleanToken.TRUE;
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
    protected BooleanToken _isEqualTo(Token rightArgument)
            throws IllegalActionException {
        RecordToken recordToken = (RecordToken)rightArgument;

        Set myLabelSet = _fields.keySet();
        Set argLabelSet = recordToken._fields.keySet();
        if ( !myLabelSet.equals(argLabelSet) ) {
            return BooleanToken.FALSE;
        }
        Iterator iterator = myLabelSet.iterator();
        while (iterator.hasNext()) {
            String label = (String)iterator.next();
            Token token1 = get(label);
            Token token2 = recordToken.get(label);
            BooleanToken result = token1.isEqualTo(token2);
            if (result.booleanValue() == false) {
                return BooleanToken.FALSE;
            }
        }

        return BooleanToken.TRUE;
    }

    /** Return a new token whose value is the field-wise modulo of
     *  this token and the argument. It is assumed
     *  that the class of the argument is RecordToken.
     *  @param rightArgument The token to modulo this token by.
     *  @return A new RecordToken.
     *  @exception IllegalActionException If calling the modulo method on
     *  one of the record fields throws it.
     */
    protected Token _modulo(Token rightArgument)
            throws IllegalActionException {
        RecordToken recordToken = (RecordToken)rightArgument;

        Set intersectionSet = new HashSet();
        intersectionSet.addAll(_fields.keySet());
        intersectionSet.retainAll(recordToken._fields.keySet());

        Iterator labels = intersectionSet.iterator();
        int size = intersectionSet.size();
        String[] newLabels = new String[size];
        Token[] newValues = new Token[size];
        int i = 0;
        while (labels.hasNext()) {
            String label = (String)labels.next();
            Token token1 = get(label);
            Token token2 = recordToken.get(label);

            newLabels[i] = label;
            newValues[i] = token1.modulo(token2);

            i++;
        }
        return new RecordToken(newLabels, newValues);
    }

    /** Return a new token whose value is the field-wise
     *  multiplication of this token and the argument. It is assumed
     *  that the class of the argument is RecordToken.
     *  @param rightArgument The token to multiply this token by.
     *  @return A new RecordToken.
     *  @exception IllegalActionException If calling the multiply method on
     *  one of the record fields throws it.
     */
    protected Token _multiply(Token rightArgument)
            throws IllegalActionException {
        RecordToken recordToken = (RecordToken)rightArgument;

        Set intersectionSet = new HashSet();
        intersectionSet.addAll(_fields.keySet());
        intersectionSet.retainAll(recordToken._fields.keySet());

        Iterator labels = intersectionSet.iterator();
        int size = intersectionSet.size();
        String[] newLabels = new String[size];
        Token[] newValues = new Token[size];
        int i = 0;
        while (labels.hasNext()) {
            String label = (String)labels.next();
            Token token1 = get(label);
            Token token2 = recordToken.get(label);

            newLabels[i] = label;
            newValues[i] = token1.multiply(token2);

            i++;
        }
        return new RecordToken(newLabels, newValues);
    }

    /** Return a new token whose value is the field-wise subtraction
     *  of this token and the argument. It is assumed that the class
     *  of the argument is RecordToken.
     *  @param rightArgument The token to subtract from this token.
     *  @return A new RecordToken.
     *  @exception IllegalActionException If calling the subtract
     *  method on one of the record fields throws it.
     */
    protected Token _subtract(Token rightArgument)
            throws IllegalActionException {
        RecordToken recordToken = (RecordToken)rightArgument;

        Set intersectionSet = new HashSet();
        intersectionSet.addAll(_fields.keySet());
        intersectionSet.retainAll(recordToken._fields.keySet());

        Iterator labels = intersectionSet.iterator();
        int size = intersectionSet.size();
        String[] newLabels = new String[size];
        Token[] newValues = new Token[size];
        int i = 0;
        while (labels.hasNext()) {
            String label = (String)labels.next();
            Token token1 = get(label);
            Token token2 = recordToken.get(label);

            newLabels[i] = label;
            newValues[i] = token1.subtract(token2);

            i++;
        }
        return new RecordToken(newLabels, newValues);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // initialize this token using the specified labels and values.
    // This method is called by the constructor.
    private void _initialize(String[] labels, Token[] values)
            throws IllegalActionException {
        if (labels == null || values == null ||
                labels.length != values.length) {
            throw new IllegalActionException("RecordToken: the labels or " +
                    "the values array do not have the same length, " +
                    "or is null.");
        }

        for (int i = 0; i<labels.length; i++) {
            if (labels[i] == null || values[i] == null) {
                throw new IllegalActionException("RecordToken: the " + i +
                        "'th element of the labels or values array is null");
            }
            if ( !_fields.containsKey(labels[i])) {
                _fields.put(labels[i], values[i]);
            } else {
                throw new IllegalActionException("RecordToken: The " +
                        "labels array contain duplicate element: " +
                        labels[i]);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Map _fields = new TreeMap();
}
