/* A token that contains a set of labeled Tokens.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Red (yuhong@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.type.Type;
import ptolemy.data.type.RecordType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//////////////////////////////////////////////////////////////////////////
//// RecordToken
/**
A token that contains a set of labeled Tokens.

@author Yuhong Xiong
@version $Id$
*/

public class RecordToken extends Token {

    /** Construct a RecordToken with the specified labeles and values. 
     *  The labels and values array must have the same length, and have one
     *  to correspondance. That is, the i'th entry in the labels array
     *  is the label for the i'th value in the values array. Both arrays
     *  must be non-empty.
     *  @param labels An array of labels.
     *  @param values An array of Tokens.
     *  @exception IllegalArgumentException If the labels or the values array
     *   do not have the same length; or is empty; or contains null element;
     *   or the labels array contains duplicate elements.
     */
    public RecordToken(String[] labels, Token[] values) {
        if (labels == null || values == null ||
            labels.length != values.length) {
            throw new IllegalArgumentException("RecordToken: the labels or " +
                "the values array do not have the same length, or is null.");
        }

        for (int i=0; i<labels.length; i++) {
            if (labels[i] == null || values[i] == null) {
                throw new IllegalArgumentException("RecordToken: the " + i +
                    "'th element of the labels or values array is null");
            }
            if ( !_fields.containsKey(labels[i])) {
                _fields.put(labels[i], values[i]);
            } else {
                throw new IllegalArgumentException("RecordToken: The " +
                    "labels array contain duplicate element: " + labels[i]);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new token whose value is the field-wise addition of
     *  this token and the argument. The argument must be a RecordToken.
     *  The result is a RecordToken whose label set is the intersection
     *  of the label sets of this token and the argument. The type of
     *  the result token is greater than or equal to the type of this
     *  token and the argument.
     *  @param t The token to add to this token.
     *  @return A new RecordToken.
     *  @exception IllegalActionException If the argument is not a
     *   RecordToken, or calling the add method of the element token
     *   throws it.
     */
    public Token add(Token t) throws IllegalActionException {
        if ( !(t instanceof RecordToken)) {
            throw new IllegalActionException("RecordToken.add: The argument "
                    + "is not a RecordToken.");
        }

        RecordToken argRecTok = (RecordToken)t;

        Set intersectionSet = new HashSet();
        Set myLabelSet = _fields.keySet();
        Set argLabelSet = argRecTok._fields.keySet();
        intersectionSet.addAll(myLabelSet);
        intersectionSet.retainAll(argLabelSet);

        int size = intersectionSet.size();
        String[] labels = new String[size];
        Token[] values = new Token[size];

        Iterator iter = intersectionSet.iterator();
        int i = 0;
        while (iter.hasNext()) {
            labels[i] = (String)iter.next();
            Token value1 = this.get(labels[i]);
            Token value2 = argRecTok.get(labels[i]);
            values[i] = value1.add(value2);
            i++;
        }

        return new RecordToken(labels, values);
    }

    /** Throw an exception. Use the convert method in RecordType.
     *  @exception IllegalActionException Always thrown.
     */
    public static Token convert(Token t) throws IllegalActionException {
        throw new IllegalActionException("RecordToken.convert: " +
                "This method cannot be used, use the convert method " +
                "in RecordType.");
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
        Iterator iter = _fields.keySet().iterator();
        int size = _fields.size();
        String[] labels = new String[size];
        Type[] types = new Type[size];

        int i = 0;
        while (iter.hasNext()) {
            labels[i] = (String)iter.next();
            types[i] = this.get(labels[i]).getType();
            i++;
        }

        return new RecordType(labels, types);
    }

    /** Test for equality of the values of this Token and the argument.
     *  @param t The token with which to test equality.
     *  @return A new BooleanToken which contains the result of the test.
     *  @exception IllegalActionException If the argument is not an
     *   RecordToken, or calling the isEqualTo method of the element token
     *   throws it.
     */
    public BooleanToken isEqualTo(Token t)
            throws IllegalActionException {
        if ( !(t instanceof RecordToken)) {
            throw new IllegalActionException("RecordToken.isEqualTo: The " +
                "argument is not a RecordToken.");
        }

        RecordToken argRecTok = (RecordToken)t;

        Set myLabelSet = _fields.keySet();
        Set argLabelSet = argRecTok._fields.keySet();
        if ( !myLabelSet.equals(argLabelSet)) {
            return new BooleanToken(false);
        }

        Iterator iter = myLabelSet.iterator();
        while(iter.hasNext()) {
            String label = (String)iter.next();
            Token token1 = this.get(label);
            Token token2 = argRecTok.get(label);
            BooleanToken result = token1.isEqualTo(token2);
            if (result.booleanValue() == false) {
                return new BooleanToken(false);
            }
        }

        return new BooleanToken(true);
    }

    /** Return the labels of this token as a Set.
     *  @return A Set containing strings.
     */
    public Set labelSet() {
        return _fields.keySet();
    }

    /** Returns a new RecordToken representing the multiplicative identity.
     *  The returned token has the same set of labels as this one, and
     *  each field contains the multiplicative identity of the corresponding
     *  field of this one.
     *  @return An RecordToken.
     *  @exception IllegalActionException If multiplicative identity is not
     *   supported by the element token.
     */
    public Token one() throws IllegalActionException {
        Object[] labelsObj = _fields.keySet().toArray();
        String[] labels = new String[labelsObj.length];
        Token[] values = new Token[labels.length];
        for (int i=0; i<labels.length; i++) {
            labels[i] = (String)labelsObj[i];
            values[i] = this.get(labels[i]).one();
        }
        return new RecordToken(labels, values);
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  The syntax is similar to the ML record:
     *  {<label>=<value>, <label>=<value>, ...}
     *  The record fields are listed in the lexicographical order of the
     *  labels determined by the java.lang.String.compareTo() method.
     *  @return A String beginning with "{" that contains label and value 
     *  pairs separated by commas, ending with "}".
     */
    public String toString() {
        Object[] labelsObj = _fields.keySet().toArray();
        // order the labels
        int size = labelsObj.length;
        for (int i=0; i<size-1; i++) {
            for (int j=i+1; j<size; j++) {
                String labeli = (String)labelsObj[i];
                String labelj = (String)labelsObj[j];
                if (labeli.compareTo(labelj) >= 0) {
                    Object temp = labelsObj[i];
                    labelsObj[i] = labelsObj[j];
                    labelsObj[j] = temp;
                }
            }
        }

        // construct the string representation of this token.
        String s = "{";
        for (int i=0; i<size; i++) {
            String label = (String)labelsObj[i];
            String value = get(label).toString();
            if (i != 0) {
                s += ", ";
            }
            s += label + "=" + value;
        }
        return s + "}";
    }

    /** Returns a new RecordToken representing the additive identity.
     *  The returned token has the same set of labels as this one, and
     *  each field contains the additive identity of the corresponding
     *  field of this one.
     *  @return An RecordToken.
     *  @exception IllegalActionException If additive identity is not
     *   supported by the element token.
     */
    public Token zero() throws IllegalActionException {
        Object[] labelsObj = _fields.keySet().toArray();
        String[] labels = new String[labelsObj.length];
        Token[] values = new Token[labels.length];
        for (int i=0; i<labels.length; i++) {
            labels[i] = (String)labelsObj[i];
            values[i] = this.get(labels[i]).zero();
        }
        return new RecordToken(labels, values);
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    private Map _fields = new HashMap();
}

