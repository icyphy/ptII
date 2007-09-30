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
package ptolemy.actor.gt.rules;

import ptolemy.actor.Actor;
import ptolemy.actor.gt.Rule;
import ptolemy.actor.gt.RuleAttribute;
import ptolemy.actor.gt.RuleValidationException;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// SubclassRule

/**

@author Thomas Huining Feng
@version $Id$
@since Ptolemy II 6.1
@Pt.ProposedRating Red (tfeng)
@Pt.AcceptedRating Red (tfeng)
*/
public class SubclassRule extends Rule {

    public SubclassRule() {
        this("");
    }

    public SubclassRule(String values) {
        super(1);
        setValues(values);
    }

    public RuleAttribute[] getRuleAttributes() {
        return _ATTRIBUTES;
    }

    public String getSuperclass() {
        return _superclass;
    }

    public Object getValue(int index) {
        switch (index) {
        case 0:
            return _superclass;
        default:
            return null;
        }
    }

    public String getValues() {
        StringBuffer buffer = new StringBuffer();
        _encodeStringField(buffer, 0, _superclass);
        return buffer.toString();
    }

    public boolean isSuperclassEnabled() {
        return isEnabled(0);
    }

    public NamedObjMatchResult match(NamedObj object) {
        if (object instanceof ComponentEntity) {
            if (isSuperclassEnabled()) {
                try {
                    Class<?> superclass = Class.forName(getSuperclass());
                    if (superclass.isInstance(object)) {
                        return NamedObjMatchResult.MATCH;
                    } else {
                        return NamedObjMatchResult.NOT_MATCH;
                    }
                } catch (ClassNotFoundException e) {
                    return NamedObjMatchResult.NOT_MATCH;
                }
            } else {
                return NamedObjMatchResult.MATCH;
            }
        } else {
            return NamedObjMatchResult.UNAPPLICABLE;
        }
    }

    public void setSuperclassEnabled(boolean enabled) {
        setEnabled(0, enabled);
    }

    public void setValue(int index, Object value) {
        switch (index) {
        case 0:
            _superclass = (String) value;
            break;
        }
    }

    public void setValues(String values) {
        FieldIterator fieldIterator = new FieldIterator(values);
        _superclass = _decodeStringField(0, fieldIterator);
    }

    public void validate() throws RuleValidationException {
        if (_superclass.equals("")) {
            throw new RuleValidationException("Superclass name must not be "
                    + "empty.");
        }
        Class<?> superclass;
        try {
            superclass = Class.forName(_superclass);
        } catch (ClassNotFoundException e) {
            throw new RuleValidationException("Cannot load class \""
                    + _superclass + "\".");
        }
        try {
            superclass.asSubclass(Actor.class);
        } catch (ClassCastException e) {
            throw new RuleValidationException("Superclass must be a subclass "
                    + "of \"" + Actor.class.getName() + "\".");
        }
    }

    private static final RuleAttribute[] _ATTRIBUTES = {
        new RuleAttribute(RuleAttribute.STRING, "superclass")
    };

    private String _superclass;
}
