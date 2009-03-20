/* Superclass of criteria and operations that can be associated with entities in
   a transformation rule.

@Copyright (c) 2007-2008 The Regents of the University of California.
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
package ptolemy.actor.gt;

import java.util.Arrays;
import java.util.Iterator;

import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// GTIngredient

/**
 Superclass of criteria and operations that can be associated with entities in a
 transformation rule. Each GTIngredient has a number of {\em elements}. An
 element is essentially an entry that can hold a value and can be disabled if
 its value is ignored. This superclass provides common methods for defining and
 accessing those elements.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public abstract class GTIngredient {

    /** Disable all elements.
     */
    public void disableAll() {
        Arrays.fill(_enablements, false);
    }

    /** Enable all elements.
     */
    public void enableAll() {
        Arrays.fill(_enablements, true);
    }

    /** Get the array of elements defined in this GTIngredient.
     *
     *  @return The array of elements.
     */
    public abstract GTIngredientElement[] getElements();

    /** Get the list that contains this GTIngredient.
     *
     *  @return The list.
     */
    public GTIngredientList getOwner() {
        return _owner;
    }

    /** Get the value of the index-th elements.
     *
     *  @param index The index.
     *  @return The value.
     *  @see #setValue(int, Object)
     */
    public abstract Object getValue(int index);

    /** Get a string that describes the values of all the elements.
     *
     *  @return A string that describes the values of all the elements.
     *  @see #setValues(String)
     */
    public abstract String getValues();

    /** Check whether this GTIngredient is applicable to the object.
     *
     *  @param object The object.
     *  @return true if this GTIngredient is applicable; false otherwise.
     */
    public boolean isApplicable(NamedObj object) {
        return true;
    }

    /** Check whether the index-th element is enabled.
     *
     *  @param index The index.
     *  @return true if the index-th element is enabled; false otherwise.
     *  @see #setEnabled(int, boolean)
     */
    public boolean isEnabled(int index) {
        if (!getElements()[index].canDisable()) {
            return true;
        } else {
            return _enablements[index];
        }
    }

    /** Set the enablement of the index-th element.
     *
     *  @param index The index.
     *  @param isEnabled true if the element is set to be enabled; false if it
     *   is disabled.
     *  @see #isEnabled(int)
     */
    public void setEnabled(int index, boolean isEnabled) {
        _enablements[index] = isEnabled;
    }

    /** Set the value of the index-th element.
     *
     *  @param index The index.
     *  @param value The value.
     */
    public abstract void setValue(int index, Object value);

    /** Set the values of all the elements with a string that describes them.
     *
     *  @param values A string that describes the new values of all the
     *   elements.
     *  @see #getValues()
     */
    public abstract void setValues(String values);

    /** Return a readable string about this GTIngredient.
     * 
     *  @return A string.
     */
    public String toString() {
        return getValues();
    }

    /** Validate the enablements and values of all the elements.
     * 
     *  @exception ValidationException If some elements are invalid.
     */
    public abstract void validate() throws ValidationException;

    /** The string to separate elements in a string that describes their values.
     */
    public static final String FIELD_SEPARATOR = "/";

    /** Construct a GTIngredient within the given list as its owner containing a
     *  given number of elements. All elements are enabled at the beginning.
     * 
     *  @param owner The list as the owner of the constructed GTIngredientList.
     *  @param elementCount The number of elements that the GTIngredient has.
     */
    protected GTIngredient(GTIngredientList owner, int elementCount) {
        _owner = owner;
        _enablements = new boolean[elementCount];
        enableAll();
    }

    /** Decode a Boolean field and store the value into the index-th element.
     * 
     *  @param index The index.
     *  @param iterator The iterator used to iterate over all fields in a string
     *   describing all the values.
     *  @return The value of the field.
     */
    protected boolean _decodeBooleanField(int index, FieldIterator iterator) {
        if (iterator.hasNext()) {
            _enablements[index] = Boolean.parseBoolean(iterator.next());
        } else {
            _enablements[index] = false;
        }
        return iterator.hasNext() ? Boolean.parseBoolean(iterator.next())
                : false;
    }

    /** Decode a string field and store the value into the index-th element.
     * 
     *  @param index The index.
     *  @param iterator The iterator used to iterate over all fields in a string
     *   describing all the values.
     *  @return The value of the field.
     */
    protected String _decodeStringField(int index, FieldIterator iterator) {
        if (iterator.hasNext()) {
            _enablements[index] = Boolean.parseBoolean(iterator.next());
        } else {
            _enablements[index] = false;
        }
        return iterator.hasNext() ? _unescapeElementString(iterator.next())
                : "";
    }

    /** Encode a Boolean field with the given value using the enablement of the
     *  index-th element, and append the encoded string to the end of the
     *  buffer.
     * 
     *  @param buffer The buffer.
     *  @param index The index.
     *  @param value The value to be encoded.
     */
    protected void _encodeBooleanField(StringBuffer buffer, int index,
            boolean value) {
        if (buffer.length() > 0) {
            buffer.append(FIELD_SEPARATOR);
        }
        buffer.append(_enablements[index]);
        buffer.append(FIELD_SEPARATOR);
        buffer.append(value);
    }

    /** Encode a string field with the given value using the enablement of the
     *  index-th element, and append the encoded string to the end of the
     *  buffer.
     * 
     *  @param buffer The buffer.
     *  @param index The index.
     *  @param value The value to be encoded.
     */
    protected void _encodeStringField(StringBuffer buffer, int index,
            String value) {
        if (buffer.length() > 0) {
            buffer.append(FIELD_SEPARATOR);
        }
        buffer.append(_enablements[index]);
        buffer.append(FIELD_SEPARATOR);
        buffer.append(_escapeElementString(value));
    }

    /** Escape a string that describes the value of a single element, so that it
     *  is enclosed in quotes and between the quotes, there are no quotes
     *  (single or double) or backslashes.
     * 
     *  @param elementString The string to be escaped.
     *  @return The escaped string.
     */
    protected static String _escapeElementString(String elementString) {
        if (elementString.equals("")) {
            return "";
        }

        elementString = elementString.replace("\\", "\\\\");
        elementString = elementString.replace("\"", "\\\"");
        elementString = elementString.replace("\'", "\\\'");
        return "\"" + elementString + "\"";
    }

    /** Find the closing parenthesis that matches the the open parenthesis at
     *  startPos position in string s.
     * 
     *  @param s The string.
     *  @param startPos The position of the open parenthesis to be matched.
     *  @return The position of the matching close parenthesis, or -1 if either
     *   the character at position startPos is not an open parenthesis or it is
     *   an open parenthesis but is not matched.
     */
    protected static int _findMatchingParen(String s, int startPos) {
        if (s.charAt(startPos) == '(') {
            int parenNum = 1;
            boolean inDblQuote = false;
            boolean inSngQuote = false;
            boolean escaped = false;
            for (int i = startPos + 1; i < s.length(); i++) {
                char c = s.charAt(i);

                if (c == '\\' && (inDblQuote || inSngQuote)) {
                    escaped = !escaped;
                } else if (c == '\"' && !escaped) {
                    inDblQuote = !inDblQuote;
                } else if (c == '\'' && !inDblQuote && !escaped) {
                    inSngQuote = !inSngQuote;
                } else if (c == ')' && !inDblQuote && !inSngQuote) {
                    parenNum--;
                }

                if (c != '\\') {
                    escaped = false;
                }

                if (parenNum == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    protected static int _findSeparator(String s, int startPos,
            char separator) {
        boolean inDblQuote = false;
        boolean inSngQuote = false;
        boolean escaped = false;
        for (int i = startPos; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c == '\\' && (inDblQuote || inSngQuote)) {
                escaped = !escaped;
            } else if (c == '\"' && !escaped) {
                inDblQuote = !inDblQuote;
            } else if (c == '\'' && !inDblQuote && !escaped) {
                inSngQuote = !inSngQuote;
            } else if (c == separator && !inDblQuote && !inSngQuote) {
                return i;
            }

            if (c != '\\') {
                escaped = false;
            }
        }
        return -1;
    }

    protected static String _unescapeElementString(String elementString) {
        if (elementString.equals("")) {
            return "";
        }

        StringBuffer buffer = new StringBuffer(elementString);
        buffer.deleteCharAt(0);
        buffer.deleteCharAt(buffer.length() - 1);
        for (int i = 0; i < buffer.length(); i++) {
            if (buffer.charAt(i) == '\\') {
                buffer.deleteCharAt(i);
            }
        }
        return buffer.toString();
    }

    protected static class FieldIterator implements Iterator<String> {

        public FieldIterator(String values) {
            _values = values;
        }

        public boolean hasNext() {
            return _values != null && _values.length() > 0;
        }

        public String next() {
            int position = _values.indexOf(FIELD_SEPARATOR);
            String next;
            if (position < 0) {
                next = _values;
                _values = null;
            } else {
                next = _values.substring(0, position);
                _values = _values.substring(position + 1);
            }
            return next;
        }

        public void remove() {
            throw new KernelRuntimeException();
        }

        private String _values;
    }

    private boolean[] _enablements;

    private GTIngredientList _owner;
}
