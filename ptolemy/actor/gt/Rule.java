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

    public abstract Object getAttributeValue(int index);

    public abstract RuleAttribute[] getAttributes();

    public abstract String getValues();

    public abstract void setAttributeValue(int index, Object value);

    public abstract void setValues(String values);

    public String toString() {
        return getValues();
    }

    public static final String FIELD_SEPARATOR = "/";

    protected String _getFirstField(String values) {
        _currentValues = values;
        _currentPosition = _currentValues.indexOf(FIELD_SEPARATOR);
        if (_currentPosition < 0) {
            return _currentValues;
        } else {
            return _currentValues.substring(0, _currentPosition++);
        }
    }

    protected String _getLastField() {
        if (_currentPosition < 0) {
            return "";
        } else {
            return _currentValues.substring(_currentPosition);
        }
    }

    protected String _getNextField() {
        if (_currentPosition < 0) {
            return "";
        } else {
            int newPosition =
                _currentValues.indexOf(FIELD_SEPARATOR, _currentPosition);
            if (newPosition < 0) {
                return _currentValues.substring(_currentPosition);
            } else {
                String result =
                    _currentValues.substring(_currentPosition, newPosition);
                _currentPosition = newPosition + 1;
                return result;
            }
        }
    }

    private int _currentPosition;

    private String _currentValues;
}
