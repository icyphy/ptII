/*

 Copyright (c) 2003-2006 The Regents of the University of California.
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

import java.util.Iterator;

import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// Rule

/**

@author Thomas Huining Feng
@version $Id$
@since Ptolemy II 6.1
@Pt.ProposedRating Red (tfeng)
@Pt.AcceptedRating Red (tfeng)
*/
public abstract class Rule {

    public void disableAllAttributes() {
        for (int i = 0; i < _enablements.length; i++) {
            _enablements[i] = false;
        }
    }

    public void enableAllAttributes() {
        for (int i = 0; i < _enablements.length; i++) {
            _enablements[i] = true;
        }
    }

    public abstract RuleAttribute[] getAttributes();

    public abstract Object getAttributeValue(int index);

    public abstract String getValues();

    public boolean isAttributeEnabled(int index) {
        return _enablements[index];
    }

    public abstract NamedObjMatchResult match(NamedObj object);

    public void setAttributeEnabled(int index, boolean isEnabled) {
        _enablements[index] = isEnabled;
    }

    public abstract void setAttributeValue(int index, Object value);

    public abstract void setValues(String values);

    public String toString() {
        return getValues();
    }

    public abstract void validate() throws RuleValidationException;

    public static final String FIELD_SEPARATOR = "/";

    public enum NamedObjMatchResult {
        MATCHING, UNAPPLICABLE, UNMATCHING
    }

    protected Rule(int attributeCount) {
        _enablements = new boolean[attributeCount];
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
        return iterator.hasNext() ? _unescapeStringAttribute(iterator.next())
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
        buffer.append(_escapeStringAttribute(value));
    }

    protected static String _escapeStringAttribute(String attribute) {
        if (attribute.equals("")) {
            return "";
        }

        attribute = attribute.replace("\\", "\\\\");
        attribute = attribute.replace("\"", "\\\"");
        attribute = attribute.replace("\'", "\\\'");
        return "\"" + attribute + "\"";
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

    protected static String _unescapeStringAttribute(String attribute) {
        if (attribute.equals("")) {
            return "";
        }

        StringBuffer buffer = new StringBuffer(attribute);
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
}
