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
        setValues(values);
    }

    public Object getAttributeValue(int index) {
        switch (index) {
        case 0:
            return _superclass;
        default:
            return null;
        }
    }

    public RuleAttribute[] getAttributes() {
        return _ATTRIBUTES;
    }

    public String getValues() {
        return _superclass;
    }

    public void setAttributeValue(int index, Object value) {
        switch (index) {
        case 0:
            _superclass = (String) value;
            break;
        }
    }

    public void setValues(String values) {
        _superclass = values;
    }

    private static final RuleAttribute[] _ATTRIBUTES = {
        new RuleAttribute(RuleAttribute.STRING, "Superclass")
    };

    private String _superclass;
}
