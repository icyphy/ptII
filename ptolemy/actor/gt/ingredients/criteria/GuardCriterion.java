/*

 Copyright (c) 2008 The Regents of the University of California.
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
package ptolemy.actor.gt.ingredients.criteria;

import ptolemy.actor.gt.GTIngredientElement;
import ptolemy.actor.gt.GTIngredientList;
import ptolemy.actor.gt.ValidationException;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Variable;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// GuardCriterion

/**

@author Anmol Khurana, Thomas Huining Feng
@version $Id$
@since Ptolemy II 7.1
@Pt.ProposedRating Red (tfeng)
@Pt.AcceptedRating Red (tfeng)
*/
public class GuardCriterion extends Criterion {

    public GuardCriterion(GTIngredientList owner) {
        this(owner, "");
    }

    public GuardCriterion(GTIngredientList owner, String values) {
        super(owner, 1);
        setValues(values);
    }

    public GTIngredientElement[] getElements() {
        return _ELEMENTS;
    }

    public Object getValue(int index) {
        switch (index) {
        case 0:
            return _guardValue;
        default:
            return null;
        }
    }

    public String getValues() {
        StringBuffer buffer = new StringBuffer();
        _encodeStringField(buffer, 0, _guardValue);
        return buffer.toString();
    }

    public boolean isApplicable(NamedObj object) {
        return super.isApplicable(object) && object instanceof Transition;
    }

    public boolean match(NamedObj object)  {
        Variable guardVariable = null;
        try {
            guardVariable = new Variable(object, object.uniqueName(
                            "guardVariable"));
            String guard = ((Transition) object).guardExpression
                            .getExpression();
            String guardTester = "(" + guard + ") == (" + _guardValue + ")";
            guardVariable.setExpression(guardTester);
            BooleanToken result = (BooleanToken) guardVariable.getToken();
            return result.booleanValue();
        } catch (Exception e) {
            return false;
        } finally {
                if (guardVariable != null) {
                        try {
                                        guardVariable.setContainer(null);
                                } catch (Exception e) {
                            throw new InternalErrorException("Failed to set container of "
                                                             + guardVariable + " to null");
                                }
                }
        }
    }

    public void setValue(int index, Object value) {
        switch (index) {
        case 0:
            _guardValue = (String) value;
            break;
        }
    }

    public void setValues(String values) {
        FieldIterator fieldIterator = new FieldIterator(values);
        _guardValue = _decodeStringField(0, fieldIterator);
    }

    public void validate() throws ValidationException {
        if (_guardValue.equals("")) {
            throw new ValidationException("guardvalue name must not be empty.");
        }
    }

    private static final CriterionElement[] _ELEMENTS = {
        new StringCriterionElement("GuardValue", false, false, true)
    };

    private String _guardValue;
}
