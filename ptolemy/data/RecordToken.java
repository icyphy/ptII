/* A token that contains a set of label/token pairs.

 Copyright (c) 1997-2001 The Regents of the University of California.
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

@ProposedRating Yellow (yuhong@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)

*/

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.type.Type;
import ptolemy.data.type.RecordType;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.PtParser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//////////////////////////////////////////////////////////////////////////
//// RecordToken
/**
A token that contains a set of label/token pairs.

@author Yuhong Xiong
@version $Id$
*/

public class RecordToken extends Token {

    /** Construct a RecordToken with the specified labels and values.
     *  The labels and values arrays must have the same length, and have one
     *  to one correspondence with each other. That is, the i'th entry in
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
     *  @exception IllegalActionException If the string does
     *   not contain a parsable record.
     */
    public RecordToken(String init) throws IllegalActionException {
        PtParser parser = new PtParser();
        ASTPtRootNode tree = parser.generateParseTree(init);
	RecordToken token = (RecordToken)tree.evaluateParseTree();

        Object[] labelObjects = token.labelSet().toArray();
        String[] labels = new String[labelObjects.length];
        Token[] values = new Token[labelObjects.length];
	for (int i=0; i<labelObjects.length; i++) {
	    labels[i] = (String)labelObjects[i];
	    values[i] = token.get(labels[i]);
	}
        _initialize(labels, values);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new token whose value is the field-wise addition of
     *  this token and the argument. The argument must be a RecordToken.
     *  The result is a RecordToken whose label set is the union
     *  of the label sets of this token and the argument.
     *  @param token The token to add to this token.
     *  @return A new RecordToken.
     *  @exception IllegalActionException If the argument is not a
     *   RecordToken, or calling the add method of the element token
     *   throws it.
     */
    public Token add(Token token) throws IllegalActionException {
        if ( !(token instanceof RecordToken)) {
            throw new IllegalActionException("RecordToken.add: The argument "
                    + "is not a RecordToken.");
        }

        RecordToken recordToken = (RecordToken)token;

        Set unionSet = new HashSet();
        Set myLabelSet = _fields.keySet();
        Set argLabelSet = recordToken._fields.keySet();
        unionSet.addAll(myLabelSet);
        unionSet.addAll(argLabelSet);

        Object[] labelsObjects = unionSet.toArray();
        int size = labelsObjects.length;
        String[] labels = new String[size];
        Token[] values = new Token[size];
        for (int i = 0; i < size; i++) {
            labels[i] = (String)labelsObjects[i];
            Token value1 = this.get(labels[i]);
            Token value2 = recordToken.get(labels[i]);
	    if (value1 == null) {
	        values[i] = value2;
	    } else if (value2 == null) {
	        values[i] = value1;
	    } else {
                values[i] = value1.add(value2);
	    }
        }

        return new RecordToken(labels, values);
    }

    /** Convert the argument token into an instance of RecordToken. Since
     *  this method is static, there is not an instance of RecordToken from
     *  which the resulting type can be obtained. So this method always
     *  throws an exception.  Use the convert method in RecordType instead.
     *  @exception IllegalActionException Always thrown.
     */
    public static Token convert(Token token) throws IllegalActionException {
        throw new IllegalActionException("RecordToken.convert: " +
                "This method cannot be used, use the convert method " +
                "in RecordType.");
    }

    /** Return true if the argument is an instnace of RecordToken with the
     *  same set of labels and the corresponding fields are equal.
     *  @param object An instance of Object.
     *  @return True if the argument is an instance of RecordToken with the
     *   same set of labels and the corresponding fields are equal.
     */
    public boolean equals(Object object) {
	// This test rules out instances of a subclass.
	if (object.getClass() != RecordToken.class) {
	    return false;
	}

        RecordToken recordToken = (RecordToken)object;

        Set myLabelSet = _fields.keySet();
        Set argLabelSet = recordToken._fields.keySet();
        if ( !myLabelSet.equals(argLabelSet)) {
            return false;
        }

        Iterator iterator = myLabelSet.iterator();
        while(iterator.hasNext()) {
            String label = (String)iterator.next();
            Token token1 = this.get(label);
            Token token2 = recordToken.get(label);
            if ( !token1.equals(token2)) {
                return false;
            }
        }

        return true;
    }

    /** Return the Token with the specified label. If this token does not
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
            types[i] = this.get(labels[i]).getType();
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
        while(iterator.hasNext()) {
            String label = (String)iterator.next();
            Token token = this.get(label);
	    code += token.hashCode();
        }

	return code;
    }

    /** Test that the value of this Token is close to the argument
     *  Token.  The value of the ptolemy.math.Complex epsilon field is
     *  used to determine whether the two Tokens are close.
     *
     *  <p>If A and B are the values of elements of the tokens, and if
     *  the following is true:
     *  <pre>
     *  abs(A-B) < epsilon
     *  </pre>
     *  then A and B are considered close.
     *
     *  @see ptolemy.math.Complex#epsilon
     *  @see #isEqualTo
     *  @param token The token to test closeness of this token with.
     *  @return a boolean token that contains the value true if the
     *   value and units of this token are close to those of the argument
     *   token.
     *  @exception IllegalActionException If the argument token is
     *   not of a type that can be compared with this token.
     */
    public BooleanToken isCloseTo(Token token) throws IllegalActionException{
        return isCloseTo(token, ptolemy.math.Complex.epsilon);
    }

    /** Test that the value of this Token is close to the argument
     *  Token.  The value of the ptolemy.math.Complex epsilon field is
     *  used to determine whether the two Tokens are close.
     *
     *  <p>If A and B are the values of elements of the tokens, and if
     *  the following is true:
     *  <pre>
     *  abs(A-B) < epsilon
     *  </pre>
     *  then A and B are considered close.
     *
     *  @see #isEqualTo
     *  @param token The token to test closeness of this token with.
     *  @param epsilon The value that we use to determine whether two
     *  tokens are close.
     *  @return a boolean token that contains the value true if the
     *   value and units of this token are close to those of the argument
     *   token.
     *  @exception IllegalActionException If the argument token is
     *   not of a type that can be compared with this token.
     */
    public BooleanToken isCloseTo(Token token,
				  double epsilon)
	throws IllegalActionException {
        if ( !(token instanceof RecordToken)) {
            throw new IllegalActionException("RecordToken.isEqualTo: The " +
                    "argument is not a RecordToken.");
        }

        RecordToken recordToken = (RecordToken)token;

        Set myLabelSet = _fields.keySet();
        Set argLabelSet = recordToken._fields.keySet();
        if ( !myLabelSet.equals(argLabelSet)) {
            return new BooleanToken(false);
        }

        Iterator iterator = myLabelSet.iterator();
        while(iterator.hasNext()) {
            String label = (String)iterator.next();
            Token token1 = this.get(label);
            Token token2 = recordToken.get(label);
            BooleanToken result = token1.isCloseTo(token2, epsilon);
            if (result.booleanValue() == false) {
                return new BooleanToken(false);
            }
        }

        return new BooleanToken(true);
    }

    /** Test for equality of the values of this Token and the argument.
     *  @param token The token with which to test equality.
     *  @return A new BooleanToken which contains the result of the test.
     *  @exception IllegalActionException If the argument is not an
     *   RecordToken, or calling the isEqualTo method of the element token
     *   throws it.
     */
    public BooleanToken isEqualTo(Token token) throws IllegalActionException {
        if ( !(token instanceof RecordToken)) {
            throw new IllegalActionException("RecordToken.isEqualTo: The " +
                    "argument is not a RecordToken.");
        }

        RecordToken recordToken = (RecordToken)token;

        Set myLabelSet = _fields.keySet();
        Set argLabelSet = recordToken._fields.keySet();
        if ( !myLabelSet.equals(argLabelSet)) {
            return new BooleanToken(false);
        }

        Iterator iterator = myLabelSet.iterator();
        while(iterator.hasNext()) {
            String label = (String)iterator.next();
            Token token1 = this.get(label);
            Token token2 = recordToken.get(label);
            BooleanToken result = token1.isEqualTo(token2);
            if (result.booleanValue() == false) {
                return new BooleanToken(false);
            }
        }

        return new BooleanToken(true);
    }

    /** Return the labels of this token as a Set.
     *  @return A Set containing labels.
     */
    public Set labelSet() {
        return _fields.keySet();
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
            values[i] = this.get(labels[i]).one();
        }
        return new RecordToken(labels, values);
    }

    /** Return a new token whose value is the field-wise subtraction of
     *  this token and the argument. The argument must be a RecordToken.
     *  The result is a RecordToken whose label set is the intersection
     *  of the label sets of this token and the argument. The type of
     *  the result token is greater than or equal to the types of this
     *  token and the argument.
     *  @param token The token to subtract from this token.
     *  @return A new RecordToken.
     *  @exception IllegalActionException If the argument is not a
     *   RecordToken, or calling the subtract method of the element token
     *   throws it.
     */
    public Token subtract(Token token) throws IllegalActionException {
        if ( !(token instanceof RecordToken)) {
            throw new IllegalActionException("RecordToken.subtract: The "
	            + "argument is not a RecordToken.");
        }

        RecordToken recordToken = (RecordToken)token;

        Set intersectionSet = new HashSet();
        Set myLabelSet = _fields.keySet();
        Set argLabelSet = recordToken._fields.keySet();
        intersectionSet.addAll(myLabelSet);
        intersectionSet.retainAll(argLabelSet);

        Object[] labelsObjects = intersectionSet.toArray();
        int size = labelsObjects.length;
        String[] labels = new String[size];
        Token[] values = new Token[size];
        for (int i = 0; i < size; i++) {
            labels[i] = (String)labelsObjects[i];
            Token value1 = this.get(labels[i]);
            Token value2 = recordToken.get(labels[i]);
            values[i] = value1.subtract(value2);
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
            values[i] = this.get(labels[i]).zero();
        }
        return new RecordToken(labels, values);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

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
                        "labels array contain duplicate element: " + labels[i]);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    private Map _fields = new HashMap();
}

