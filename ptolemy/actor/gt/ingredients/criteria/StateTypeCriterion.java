/* A criterion to constrain the type of a state in an FSM or Ptera controller.

 Copyright (c) 2008-2014 The Regents of the University of California.
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
import ptolemy.domains.modal.kernel.State;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// StateTypeCriterion

/**
 A criterion to constrain the type of a state in an FSM or Ptera controller.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class StateTypeCriterion extends Criterion {

    /** Construct a criterion within the given list as its owner. All elements
     *  are enabled and are initialized to empty at the beginning.
     *
     *  @param owner The list as the owner of the constructed GTIngredientList.
     */
    public StateTypeCriterion(GTIngredientList owner) {
        this(owner, "");
    }

    /** Construct a criterion within the given list as its owner and initialize
     *  all the elements with the given values, which are a string encoding of
     *  those elements. All elements are enabled at the beginning.
     *
     *  @param owner The list as the owner of the constructed GTIngredientList.
     *  @param values The string encoding of the values of the elements.
     */
    public StateTypeCriterion(GTIngredientList owner, String values) {
        this(owner, false, false);
        setValues(values);
    }

    /** Construct a criterion within the given list as its owner and with the
     *  given value to each of its elements..
     *
     *  @param owner The list as the owner of the constructed GTIngredientList.
     *  @param isInit Value of the isInit element.
     *  @param isFinal Value of the isFinal element.
     */
    public StateTypeCriterion(GTIngredientList owner, boolean isInit,
            boolean isFinal) {
        super(owner, 2);

        _isInit = isInit;
        _isFinal = isFinal;
    }

    /** Get the array of elements defined in this GTIngredient.
     *
     *  @return The array of elements.
     */
    @Override
    public GTIngredientElement[] getElements() {
        return _ELEMENTS;
    }

    /** Get the value of the index-th elements.
     *
     *  @param index The index.
     *  @return The value.
     *  @see #setValue(int, Object)
     */
    @Override
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

    /** Get a string that describes the values of all the elements.
     *
     *  @return A string that describes the values of all the elements.
     *  @see #setValues(String)
     */
    @Override
    public String getValues() {
        StringBuffer buffer = new StringBuffer();

        _encodeBooleanField(buffer, 0, _isInit);
        _encodeBooleanField(buffer, 1, _isFinal);

        return buffer.toString();
    }

    /** Check whether this GTIngredient is applicable to the object.
     *
     *  @param object The object.
     *  @return true if this GTIngredient is applicable; false otherwise.
     */
    @Override
    public boolean isApplicable(NamedObj object) {
        return super.isApplicable(object) && object instanceof State;
    }

    /** Get the isFinal element.
     *
     *  @return The isFinal element.
     */
    public boolean isFinal() {
        return _isFinal;
    }

    /** Return whether the isFinal element is enabled.
     *
     *  @return true if the isFinal element is enabled.
     */
    public boolean isFinalEnabled() {
        return isEnabled(1);
    }

    /** Get the isInit element.
     *
     *  @return The isInit element.
     */
    public boolean isInit() {
        return _isInit;
    }

    /** Return whether the isInit element is enabled.
     *
     *  @return true if the isInit element is enabled.
     */
    public boolean isInitEnabled() {
        return isEnabled(0);
    }

    /** Test whether the given object in the host model matches the object in
     *  the pattern that has this criterion.
     *
     *  @param object The object.
     *  @return true if the object matches.
     */
    @Override
    public boolean match(NamedObj object) {
        State state = (State) object;
        try {
            if (isInitEnabled() && isFinalEnabled()) {
                if (_isInit == ((BooleanToken) state.isInitialState.getToken())
                        .booleanValue()
                        && _isFinal == ((BooleanToken) state.isFinalState
                                .getToken()).booleanValue()) {
                    return true;
                } else {
                    return false;
                }
            } else if (isInitEnabled()) {
                if (_isInit == ((BooleanToken) state.isInitialState.getToken())
                        .booleanValue()) {
                    return true;
                } else {
                    return false;
                }
            }

            else if (isFinalEnabled()) {
                if (_isFinal == ((BooleanToken) state.isFinalState.getToken())
                        .booleanValue()) {
                    return true;
                } else {
                    return false;
                }
            } else if (!isFinalEnabled() && !isInitEnabled()) {
                return true;
            } else {
                return false;
            }
        } catch (IllegalActionException e) {
            return false;
        }
    }

    /** Set the value of the index-th element.
     *
     *  @param index The index.
     *  @param value The value.
     *  @see #getValue(int)
     */
    @Override
    public void setValue(int index, Object value) {
        switch (index) {
        case 0:
            _isInit = ((Boolean) value).booleanValue();
            break;
        case 1:
            _isFinal = ((Boolean) value).booleanValue();
        }
    }

    /** Set the values of all the elements with a string that describes them.
     *
     *  @param values A string that describes the new values of all the
     *   elements.
     *  @see #getValues()
     */
    @Override
    public void setValues(String values) {
        FieldIterator fieldIterator = new FieldIterator(values);

        _isInit = _decodeBooleanField(0, fieldIterator);
        _isFinal = _decodeBooleanField(1, fieldIterator);
    }

    /** Validate the enablements and values of all the elements.
     *
     *  @exception ValidationException If some elements are invalid.
     */
    @Override
    public void validate() throws ValidationException {
    }

    /** The elements.
     */
    private static final CriterionElement[] _ELEMENTS = {
            new BooleanCriterionElement("isInit", true),
            new BooleanCriterionElement("isFinal", true) };

    /** Value of the isFinal element.
     */
    private boolean _isFinal;

    /** Value of the isInit element.
     */
    private boolean _isInit;

}
