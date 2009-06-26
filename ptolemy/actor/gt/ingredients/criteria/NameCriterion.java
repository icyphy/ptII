/*
Below is the copyright agreement for the Ptolemy II system.
Version: $Id$

Copyright (c) 2008-2009 The Regents of the University of California.
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
*/
package ptolemy.actor.gt.ingredients.criteria;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import ptolemy.actor.gt.GTIngredientElement;
import ptolemy.actor.gt.GTIngredientList;
import ptolemy.actor.gt.ValidationException;
import ptolemy.actor.gt.util.RegularExpressionString;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// NameCriterion

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class NameCriterion extends Criterion {

    public NameCriterion(GTIngredientList owner) {
        this(owner, "");
    }

    public NameCriterion(GTIngredientList owner, String values) {
        super(owner, 1);
        setValues(values);
    }

    public GTIngredientElement[] getElements() {
        return _ELEMENTS;
    }

    public Object getValue(int index) {
        switch (index) {
        case 0:
            return _name;
        default:
            return null;
        }
    }

    public String getValues() {
        StringBuffer buffer = new StringBuffer();
        _encodeStringField(buffer, 0, _name.get());
        return buffer.toString();
    }

    public boolean match(NamedObj object) {
        Pattern pattern = _name.getPattern();
        Matcher matcher = pattern.matcher(object.getName());
        return matcher.matches();
    }

    public void setValue(int index, Object value) {
        switch (index) {
        case 0:
            _name.set((String) value);
            break;
        }
    }

    public void setValues(String values) {
        FieldIterator fieldIterator = new FieldIterator(values);
        _name.set(_decodeStringField(0, fieldIterator));
    }

    public void validate() throws ValidationException {
        if (_name.get().equals("")) {
            throw new ValidationException("Name must not be empty.");
        }

        try {
            _name.getPattern();
        } catch (PatternSyntaxException e) {
            throw new ValidationException("Regular expression \"" + _name +
                    "\" cannot be compiled.", e);
        }
    }

    private static final CriterionElement[] _ELEMENTS = {
        new StringCriterionElement("name", false, true, false)
    };

    private RegularExpressionString _name = new RegularExpressionString();
}
