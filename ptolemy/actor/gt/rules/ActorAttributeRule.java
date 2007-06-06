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

import ptolemy.actor.gt.Rule;
import ptolemy.actor.gt.RuleAttribute;

//////////////////////////////////////////////////////////////////////////
//// ActorAttributeRule

/**

@author Thomas Huining Feng
@version $Id$
@since Ptolemy II 6.1
@Pt.ProposedRating Red (tfeng)
@Pt.AcceptedRating Red (tfeng)
*/
public class ActorAttributeRule extends Rule {

    public ActorAttributeRule() {
        this("");
    }

    public ActorAttributeRule(String values) {
        setValues(values);
    }

    public ActorAttributeRule(String name, String type, String value) {
        _attributeName = name;
        _attributeType = type;
        _attributeValue = value;
    }

    public Object getAttributeValue(int index) {
        switch (index) {
        case 0: return _attributeName;
        case 1: return _attributeType;
        case 2: return _attributeValue;
        default: return null;
        }
    }

    public RuleAttribute[] getAttributes() {
        return _ATTRIBUTES;
    }

    public String getValues() {
        return _attributeName + FIELD_SEPARATOR + _attributeType +
                FIELD_SEPARATOR + _attributeValue;
    }

    public void setAttributeValue(int index, Object value) {
        switch (index) {
        case 0: _attributeName = (String) value;
        case 1: _attributeType = (String) value;
        case 2: _attributeValue = (String) value;
        }
    }

    public void setValues(String values) {
        int pos1 = values.indexOf(FIELD_SEPARATOR);
        if (pos1 >= 0) {
            _attributeName = values.substring(0, pos1);
            int pos2 = values.indexOf(FIELD_SEPARATOR, pos1 + 1);
            if (pos2 >= 0) {
                _attributeType = values.substring(pos1 + 1, pos2);
                _attributeValue = values.substring(pos2 + 1);
            } else {
                _attributeType = values.substring(pos1 + 1);
                _attributeValue = "";
            }
        } else {
            _attributeName = values;
            _attributeType = "";
            _attributeValue = "";
        }
    }

    private static final RuleAttribute[] _ATTRIBUTES = {
        new RuleAttribute(RuleAttribute.STRING, "Attribute Name"),
        new RuleAttribute(RuleAttribute.STRING, "Attribute Type"),
        new RuleAttribute(RuleAttribute.STRING, "Attribute Value")
    };

    private String _attributeName;

    private String _attributeType;

    private String _attributeValue;
}
