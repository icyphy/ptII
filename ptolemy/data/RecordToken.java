/* A token that contains a set of label/token pairs.

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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.PtParser;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// RecordToken

/**
 A token that contains a set of label/token pairs. Record labels may be
 arbitrary strings. Operations on record tokens result in new record tokens
 containing only the common fields, where the operation specifies how to
 combine the data in the common fields.  Thus, for example, if two record
 tokens are added or subtracted, then common records (those with the same
 labels) will be added or subtracted, and the disjoint records will not
 appear in the result.

 @author Yuhong Xiong, Steve Neuendorffer, Elaine Cheong, Edward Lee; contributors: J. S. Senecal, Marten Lohstroh
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (neuendor)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class RecordToken extends AbstractNotConvertibleToken {

    /** Construct a RecordToken with no fields.
     */
    public RecordToken() {
        _initializeStorage();
        String[] labels = new String[0];
        Token[] values = new Token[0];
        try {
            _initialize(labels, values);
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }
    }

    /** Construct a RecordToken with the specified labels and values.
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
    public RecordToken(String[] labels, Token[] values)
            throws IllegalActionException {
        _initializeStorage();
        _initialize(labels, values);
    }

    /** Construct a RecordToken from the specified string.
     *
     * <p>Record labels that contain any non-Java identifier characters
     * must be presented as a string i.e., surrounded with single or double
     * quotes. Quotes within label strings must be escaped using a backslash.
     * </p>
     *
     *  @param init A string expression of a record.
     *  @exception IllegalActionException If the string does not
     *  contain a parsable record.
     */
    public RecordToken(String init) throws IllegalActionException {
        _initializeStorage();
        PtParser parser = new PtParser();
        ASTPtRootNode tree = parser.generateParseTree(init);

        ParseTreeEvaluator evaluator = new ParseTreeEvaluator();
        Token token = evaluator.evaluateParseTree(tree);

        if (token instanceof RecordToken) {
            RecordToken recordToken = (RecordToken) token;
            Object[] labelObjects = recordToken.labelSet().toArray();
            String[] labels = new String[labelObjects.length];
            Token[] values = new Token[labelObjects.length];

            for (int i = 0; i < labelObjects.length; i++) {
                labels[i] = (String) labelObjects[i];
                values[i] = recordToken.get(labels[i]);
            }

            _initialize(labels, values);
        } else {
            throw new IllegalActionException("A record token cannot be"
                    + " created from the expression '" + init + "'");
        }
    }

    /** Construct a RecordToken with the labels and values specified
     *  by a given Map object. The object cannot contain any null keys
     *  or values.
     *  @param fieldMap A Map that has keys of type String and
     *  values of type Token.
     *  @exception IllegalActionException If the map contains null
     *  keys or values, or if it contains non-String keys or non-Token
     *  values.
     */
    public RecordToken(Map<String, Token> fieldMap)
            throws IllegalActionException {
        _initializeStorage();

        // iterate through map and put values under key in local map
        for (Map.Entry<String, Token> entry : fieldMap.entrySet()) {
            String key = entry.getKey();
            Token val = entry.getValue();

            if (key == null || val == null) {
                throw new IllegalActionException("RecordToken: given "
                        + "map contains either null keys " + "or null values.");
            }

            _fields.put(key, val);
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

        Set<String> myLabelSet = _fields.keySet();
        Set<String> argLabelSet = recordToken._fields.keySet();

        if (!myLabelSet.equals(argLabelSet)) {
            return false;
        }

        Iterator<String> iterator = myLabelSet.iterator();

        while (iterator.hasNext()) {
            String label = iterator.next();
            Token token1 = get(label);
            Token token2 = recordToken.get(label);

            if (!token1.equals(token2)) {
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
        return _fields.get(label);
    }

    /** Return the type of this token.
     *  @return An instance of RecordType.
     */
    @Override
    public Type getType() {
        Object[] labelsObjects = _fields.keySet().toArray();
        int size = labelsObjects.length;
        String[] labels = new String[size];
        Type[] types = new Type[size];

        for (int i = 0; i < size; i++) {
            labels[i] = (String) labelsObjects[i];
            types[i] = get(labels[i]).getType();
        }

        return new RecordType(labels, types);
    }

    /** Return a hash code value for this token. This method returns the sum
     *  of the hash codes of the element tokens.
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
            code += token.hashCode();
        }

        return code;
    }

    /** Return the labels of this token as a Set.
     *  @return A Set containing labels.
     */
    public Set<String> labelSet() {
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
        Set<String> unionSet = new HashSet<String>();
        Set<String> labelSet1 = token1._fields.keySet();
        Set<String> labelSet2 = token2._fields.keySet();
        unionSet.addAll(labelSet1);
        unionSet.addAll(labelSet2);

        Object[] labelsObjects = unionSet.toArray();
        int size = labelsObjects.length;
        String[] labels = new String[size];
        Token[] values = new Token[size];

        for (int i = 0; i < size; i++) {
            labels[i] = (String) labelsObjects[i];

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
        if (type1 instanceof RecordType && type2 instanceof RecordType) {
            RecordType recordType1 = (RecordType) type1;
            RecordType recordType2 = (RecordType) type2;

            Set<String> unionSet = new HashSet<String>();
            Set<String> labelSet1 = recordType1.labelSet();
            Set<String> labelSet2 = recordType2.labelSet();
            unionSet.addAll(labelSet1);
            unionSet.addAll(labelSet2);

            Object[] labelsObjects = unionSet.toArray();
            int size = labelsObjects.length;
            String[] labels = new String[size];
            Type[] types = new Type[size];

            for (int i = 0; i < size; i++) {
                labels[i] = (String) labelsObjects[i];

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
    @Override
    public Token one() throws IllegalActionException {
        Object[] labelsObjects = _fields.keySet().toArray();
        int size = labelsObjects.length;
        String[] labels = new String[size];
        Token[] values = new Token[size];

        for (int i = 0; i < size; i++) {
            labels[i] = (String) labelsObjects[i];
            values[i] = get(labels[i]).one();
        }

        return _createRecordToken(labels, values);
    }

    /** Return the value of this token as a string.
     *  The syntax is similar to the ML record:
     *  <code>{<i>label</i> = <i>value</i>, <i>label</i> = <i>value</i>, ...}</code>
     *  The record fields are listed in the lexicographical order of the
     *  labels determined by the java.lang.String.compareTo() method.
     *
     *  <p>Record labels that contain any non-Java identifier characters
     *  are surrounded with double quotes. Quotes within label strings are
     *  escaped using a backslash.
     *  </p>
     *
     *  @return A String beginning with "{" that contains label and value
     *  pairs separated by commas, ending with "}".
     */
    @Override
    public String toString() {
        Object[] labelsObjects = _fields.keySet().toArray();

        // order the labels
        int size = labelsObjects.length;

        for (int i = 0; i < size - 1; i++) {
            for (int j = i + 1; j < size; j++) {
                String labeli = (String) labelsObjects[i];
                String labelj = (String) labelsObjects[j];

                if (labeli.compareTo(labelj) >= 0) {
                    Object temp = labelsObjects[i];
                    labelsObjects[i] = labelsObjects[j];
                    labelsObjects[j] = temp;
                }
            }
        }

        // construct the string representation of this token.
        StringBuffer stringRepresentation = new StringBuffer("{");

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

        return stringRepresentation.toString() + "}";
    }

    /** Returns a new RecordToken representing the additive identity.
     *  The returned token has the same set of labels as this one, and
     *  each field contains the additive identity of the corresponding
     *  field of this token.
     *  @return A RecordToken.
     *  @exception IllegalActionException If additive identity is not
     *   supported by any element token.
     */
    @Override
    public Token zero() throws IllegalActionException {
        Object[] labelsObjects = _fields.keySet().toArray();
        int size = labelsObjects.length;
        String[] labels = new String[size];
        Token[] values = new Token[size];

        for (int i = 0; i < size; i++) {
            labels[i] = (String) labelsObjects[i];
            values[i] = get(labels[i]).zero();
        }

        return _createRecordToken(labels, values);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Empty Record. */
    public static final RecordToken EMPTY_RECORD = new RecordToken();

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
    @Override
    protected Token _add(Token rightArgument) throws IllegalActionException {
        RecordToken recordToken = (RecordToken) rightArgument;

        Set<String> intersectionSet = _createSet();
        intersectionSet.addAll(_fields.keySet());
        intersectionSet.retainAll(recordToken._fields.keySet());

        Iterator<String> labels = intersectionSet.iterator();
        int size = intersectionSet.size();
        String[] newLabels = new String[size];
        Token[] newValues = new Token[size];
        int i = 0;

        while (labels.hasNext()) {
            String label = labels.next();
            Token token1 = get(label);
            Token token2 = recordToken.get(label);

            newLabels[i] = label;
            newValues[i] = token1.add(token2);

            i++;
        }

        return _createRecordToken(newLabels, newValues);
    }

    /** Return a new token whose value is the field-wise division of
     *  this token and the argument. It is assumed that the class of
     *  the argument is RecordToken.
     *  @param rightArgument The token to divide this token by.
     *  @return A new RecordToken.
     *  @exception IllegalActionException If calling the divide method on
     *  one of the record fields throws it.
     */
    @Override
    protected Token _divide(Token rightArgument) throws IllegalActionException {
        RecordToken recordToken = (RecordToken) rightArgument;

        Set<String> intersectionSet = _createSet();
        intersectionSet.addAll(_fields.keySet());
        intersectionSet.retainAll(recordToken._fields.keySet());

        Iterator<String> labels = intersectionSet.iterator();
        int size = intersectionSet.size();
        String[] newLabels = new String[size];
        Token[] newValues = new Token[size];
        int i = 0;

        while (labels.hasNext()) {
            String label = labels.next();
            Token token1 = get(label);
            Token token2 = recordToken.get(label);

            newLabels[i] = label;
            newValues[i] = token1.divide(token2);

            i++;
        }

        return _createRecordToken(newLabels, newValues);
    }

    /** Test whether the value of this token is close to the first
     *  argument, where "close" means that the distance between them
     *  is less than or equal to the second argument.  This method
     *  only makes sense for tokens where the distance between them is
     *  reasonably represented as a double. It is assumed that the
     *  argument is an RecordToken, and the isCloseTo() method of the
     *  fields is used.  If the fields do not match, then the
     *  return value is false.
     *  @param rightArgument The token to compare to this token.
     *  @param epsilon The value that we use to determine whether two
     *   tokens are close.
     *  @return A token containing true if the value of the first
     *   argument is close to the value of this token.
     *  @exception IllegalActionException If throw while checking
     *  the closeness of an element of the record.
     */
    @Override
    protected BooleanToken _isCloseTo(Token rightArgument, double epsilon)
            throws IllegalActionException {
        RecordToken recordToken = (RecordToken) rightArgument;

        Set<String> myLabelSet = _fields.keySet();
        Set<String> argLabelSet = recordToken._fields.keySet();

        if (!myLabelSet.equals(argLabelSet)) {
            return BooleanToken.FALSE;
        }

        // Loop through all of the fields, checking each one for closeness.
        Iterator<String> iterator = myLabelSet.iterator();

        while (iterator.hasNext()) {
            String label = iterator.next();
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

        while (iterator.hasNext()) {
            String label = iterator.next();
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
     *  this token and the argument. It is assumed that the class of
     *  the argument is RecordToken.
     *  @param rightArgument The token to modulo this token by.
     *  @return A new RecordToken.
     *  @exception IllegalActionException If calling the modulo method on
     *  one of the record fields throws it.
     */
    @Override
    protected Token _modulo(Token rightArgument) throws IllegalActionException {
        RecordToken recordToken = (RecordToken) rightArgument;

        Set<String> intersectionSet = _createSet();
        intersectionSet.addAll(_fields.keySet());
        intersectionSet.retainAll(recordToken._fields.keySet());

        Iterator<String> labels = intersectionSet.iterator();
        int size = intersectionSet.size();
        String[] newLabels = new String[size];
        Token[] newValues = new Token[size];
        int i = 0;

        while (labels.hasNext()) {
            String label = labels.next();
            Token token1 = get(label);
            Token token2 = recordToken.get(label);

            newLabels[i] = label;
            newValues[i] = token1.modulo(token2);

            i++;
        }

        return _createRecordToken(newLabels, newValues);
    }

    /** Return a new token whose value is the field-wise
     *  multiplication of this token and the argument. It is assumed
     *  that the class of the argument is RecordToken.
     *  @param rightArgument The token to multiply this token by.
     *  @return A new RecordToken.
     *  @exception IllegalActionException If calling the multiply method on
     *  one of the record fields throws it.
     */
    @Override
    protected Token _multiply(Token rightArgument)
            throws IllegalActionException {
        RecordToken recordToken = (RecordToken) rightArgument;

        Set<String> intersectionSet = _createSet();
        intersectionSet.addAll(_fields.keySet());
        intersectionSet.retainAll(recordToken._fields.keySet());

        Iterator<String> labels = intersectionSet.iterator();
        int size = intersectionSet.size();
        String[] newLabels = new String[size];
        Token[] newValues = new Token[size];
        int i = 0;

        while (labels.hasNext()) {
            String label = labels.next();
            Token token1 = get(label);
            Token token2 = recordToken.get(label);

            newLabels[i] = label;
            newValues[i] = token1.multiply(token2);

            i++;
        }

        return _createRecordToken(newLabels, newValues);
    }

    /** Return a new token whose value is the field-wise subtraction
     *  of this token and the argument. It is assumed that the class
     *  of the argument is RecordToken.
     *  @param rightArgument The token to subtract from this token.
     *  @return A new RecordToken.
     *  @exception IllegalActionException If calling the subtract
     *  method on one of the record fields throws it.
     */
    @Override
    protected Token _subtract(Token rightArgument)
            throws IllegalActionException {
        RecordToken recordToken = (RecordToken) rightArgument;

        Set<String> intersectionSet = _createSet();
        intersectionSet.addAll(_fields.keySet());
        intersectionSet.retainAll(recordToken._fields.keySet());

        Iterator<String> labels = intersectionSet.iterator();
        int size = intersectionSet.size();
        String[] newLabels = new String[size];
        Token[] newValues = new Token[size];
        int i = 0;

        while (labels.hasNext()) {
            String label = labels.next();
            Token token1 = get(label);
            Token token2 = recordToken.get(label);

            newLabels[i] = label;
            newValues[i] = token1.subtract(token2);

            i++;
        }

        return _createRecordToken(newLabels, newValues);
    }

    /**
     * Subclasses of RecordToken may choose a different Map implementation
     * TreeMap is used in the base class to provide naturally-ordered labels
     * This may not be desired in some applications.
     */
    protected void _initializeStorage() {
        _fields = new TreeMap<String, Token>();
    }

    /**
     * Create a new RecordToken.
     * Subclasses of RecordToken may return a different subclass instance.
     * @param labels An array of String labels for the RecordToken to be created.
     * @param values An array of Token values for the RecordToken to be created.
     * @return a new RecordToken.
     * @exception IllegalActionException If thrown while constructing the RecordToken
     */
    protected RecordToken _createRecordToken(String[] labels, Token[] values)
            throws IllegalActionException {
        return new RecordToken(labels, values);
    }

    /**
     * Create a Set implementation appropriate for operations on this RecordToken
     * Subclasses of RecordToken may return a different implementation.
     * @return a new Set.
     */
    protected Set<String> _createSet() {
        return new HashSet<String>();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // initialize this token using the specified labels and values.
    // This method is called by the constructor.
    private void _initialize(String[] labels, Token[] values)
            throws IllegalActionException {
        if (labels == null || values == null || labels.length != values.length) {
            throw new IllegalActionException("RecordToken: the labels or "
                    + "the values array do not have the same length, "
                    + "or is null.");
        }

        for (int i = 0; i < labels.length; i++) {
            if (labels[i] == null || values[i] == null) {
                throw new IllegalActionException("RecordToken: the " + i
                        + "'th element of the labels or values array is null");
            }

            labels[i] = labels[i];
            if (!_fields.containsKey(labels[i])) {
                _fields.put(labels[i], values[i]);
            } else {
                throw new IllegalActionException("RecordToken: The "
                        + "labels array contain duplicate element: "
                        + labels[i]);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The map of fields that has keys of type String and values of
     *  type token.
     *  Subclasses can use alternative Map implementations (for ordering).
     */
    protected Map<String, Token> _fields = null;
}
