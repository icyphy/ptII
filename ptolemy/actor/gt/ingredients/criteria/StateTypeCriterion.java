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
import ptolemy.domains.fsm.kernel.State;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

public class StateTypeCriterion extends Criterion {

    public StateTypeCriterion(GTIngredientList owner) {
        this(owner, "");
    }

    public StateTypeCriterion(GTIngredientList owner, String values) {
        this(owner,  false, false);
        setValues(values);
    }

    public StateTypeCriterion(GTIngredientList owner, boolean isInit, boolean isFinal) {
        super(owner, 2);

        _isInit = isInit;
        _isFinal = isFinal;
    }

    public GTIngredientElement[] getElements() {
        return _ELEMENTS;
    }

    public Object getValue(int index) {
        switch (index) {
        case 0:
            return _isInit;
        case 1:
            return _isFinal;
        default:
            return null;
        }
    }

    public String getValues() {
        StringBuffer buffer = new StringBuffer();

        _encodeBooleanField(buffer, 0, _isInit);
        _encodeBooleanField(buffer, 1, _isFinal);

        return buffer.toString();
    }

    public boolean isApplicable(NamedObj object) {
         return super.isApplicable(object) && object instanceof State;
    }

    public boolean isFinal() {
        return _isFinal;
    }

    public boolean isFinalEnabled() {
        return isEnabled(1);
    }

    public boolean isInit() {
        return _isInit;
    }

    public boolean isInitEnabled() {
        return isEnabled(0);
    }

    public boolean match(NamedObj object) {
        State state = (State) object;
        try {
            if(isInitEnabled() && isFinalEnabled()) {
                if( _isInit == ((BooleanToken)state.isInitialState.getToken())
                        .booleanValue() &&_isFinal == ((BooleanToken)state
                                .isFinalState.getToken()).booleanValue()) {
                    return true;
                } else {
                    return false;
                }
            }
            else if(isInitEnabled()) {
                if( _isInit == ((BooleanToken)state.isInitialState.getToken())
                        .booleanValue()) {
                    return true;
                } else {
                    return false;
                }
            }

            else if(isFinalEnabled()) {
                if( _isFinal == ((BooleanToken)state.isFinalState.getToken())
                        .booleanValue()) {
                    return true;
                } else {
                    return false;
                }
            } else if(!isFinalEnabled() && !isInitEnabled() ) {
                return true;
            } else {
                return false;
            }
        } catch (IllegalActionException e) {
            return false;
        }
    }

    public void setValue(int index, Object value) {
        switch (index) {
        case 0:
            _isInit = ((Boolean) value).booleanValue();
            break;
        case 1:
            _isFinal = ((Boolean) value).booleanValue();
        }
    }

    public void setValues(String values) {
        FieldIterator fieldIterator = new FieldIterator(values);

        _isInit = _decodeBooleanField(0, fieldIterator);
        _isFinal = _decodeBooleanField(1, fieldIterator);
    }


    public void setisFinalEnabled(boolean enabled) {
        setEnabled(1, enabled);
    }

    public void setisInitEnabled(boolean enabled) {
        setEnabled(0, enabled);
    }

    public void validate() throws ValidationException {
    }

    private static final CriterionElement[] _ELEMENTS = {
        new BooleanCriterionElement("isInit", true),
        new BooleanCriterionElement("isFinal", true)
    };

    private boolean _isFinal;

    private boolean _isInit;

}
