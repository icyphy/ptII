/*

 Copyright (c) 2003-2008 The Regents of the University of California.
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
package ptolemy.actor.gt;

import java.util.Arrays;
import java.util.Iterator;

import ptolemy.kernel.util.KernelRuntimeException;

//////////////////////////////////////////////////////////////////////////
//// Rule

/**

@author Thomas Huining Feng
@version $Id$
@since Ptolemy II 6.1
@Pt.ProposedRating Red (tfeng)
@Pt.AcceptedRating Red (tfeng)
*/
public abstract class GTIngredient {

    public void disableAll() {
        Arrays.fill(_enablements, false);
    }

    public void enableAll() {
        Arrays.fill(_enablements, true);
    }

    public abstract GTIngredientElement[] getElements();

    public GTIngredientList getOwner() {
        return _owner;
    }

    public abstract Object getValue(int index);

    public abstract String getValues();

    public boolean isEnabled(int index) {
        if (!getElements()[index].canDisable()) {
            return true;
        } else {
            return _enablements[index];
        }
    }

    public void setEnabled(int index, boolean isEnabled) {
        _enablements[index] = isEnabled;
    }

    public abstract void setValue(int index, Object value);

    public abstract void setValues(String values);

    public String toString() {
        return getValues();
    }

    public abstract void validate() throws ValidationException;

    public static final String FIELD_SEPARATOR = "/";

    public enum NamedObjMatchResult {
        MATCH, NOT_MATCH, UNAPPLICABLE
    }

    protected GTIngredient(GTIngredientList owner) {
        _owner = owner;
    }

    protected GTIngredient(GTIngredientList owner, int elementCount) {
        this(owner);
        _enablements = new boolean[elementCount];
        enableAll();
    }

    protected boolean _decodeBooleanField(int index, FieldIterator iterator) {
        if (iterator.hasNext()) {
            _enablements[index] = Boolean.parseBoolean(iterator.next());
        } else {
            _enablements[index] = false;
        }
        return iterator.hasNext() ? Boolean.parseBoolean(iterator.next())
                : false;
    }

    protected String _decodeStringField(int index, FieldIterator iterator) {
        if (iterator.hasNext()) {
            _enablements[index] = Boolean.parseBoolean(iterator.next());
        } else {
            _enablements[index] = false;
        }
        return iterator.hasNext() ? _unescapeElementString(iterator.next())
                : "";
    }

    protected void _encodeBooleanField(StringBuffer buffer, int index,
            boolean value) {
        if (buffer.length() > 0) {
            buffer.append(FIELD_SEPARATOR);
        }
        buffer.append(_enablements[index]);
        buffer.append(FIELD_SEPARATOR);
        buffer.append(value);
    }

    protected void _encodeStringField(StringBuffer buffer, int index,
            String value) {
        if (buffer.length() > 0) {
            buffer.append(FIELD_SEPARATOR);
        }
        buffer.append(_enablements[index]);
        buffer.append(FIELD_SEPARATOR);
        buffer.append(_escapeElementString(value));
    }

    protected static String _escapeElementString(String elementString) {
        if (elementString.equals("")) {
            return "";
        }

        elementString = elementString.replace("\\", "\\\\");
        elementString = elementString.replace("\"", "\\\"");
        elementString = elementString.replace("\'", "\\\'");
        return "\"" + elementString + "\"";
    }

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

    protected static int _findSeparator(String s, int startPos, char separator) {
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
