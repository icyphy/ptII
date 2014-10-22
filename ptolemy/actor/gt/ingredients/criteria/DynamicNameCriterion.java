/* A criterion to constrain the name of an object in the host model.

Below is the copyright agreement for the Ptolemy II system.

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
 */
package ptolemy.actor.gt.ingredients.criteria;

import ptolemy.actor.gt.GTIngredientElement;
import ptolemy.actor.gt.GTIngredientList;
import ptolemy.actor.gt.ValidationException;
import ptolemy.actor.gt.util.PtolemyExpressionString;
import ptolemy.data.StringToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// NameCriterion

/**
 A criterion to constrain the name of an object in the host model.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class DynamicNameCriterion extends Criterion {

    /** Construct a criterion within the given list as its owner. All elements
     *  are enabled and are initialized to empty at the beginning.
     *
     *  @param owner The list as the owner of the constructed GTIngredientList.
     */
    public DynamicNameCriterion(GTIngredientList owner) {
        this(owner, "");
    }

    /** Construct a criterion within the given list as its owner and initialize
     *  all the elements with the given values, which are a string encoding of
     *  those elements. All elements are enabled at the beginning.
     *
     *  @param owner The list as the owner of the constructed GTIngredientList.
     *  @param values The string encoding of the values of the elements.
     */
    public DynamicNameCriterion(GTIngredientList owner, String values) {
        super(owner, 1);

        NamedObj container = owner.getOwner().getContainer();
        _name = new PtolemyExpressionString(container);
        setValues(values);
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
            return _name;
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
        _encodeStringField(buffer, 0, _name.get());
        return buffer.toString();
    }

    /** Test whether the given object in the host model matches the object in
     *  the pattern that has this criterion.
     *
     *  @param object The object.
     *  @return true if the object matches.
     */
    @Override
    public boolean match(NamedObj object) {
        try {
            String name = ((StringToken) _name.getToken()).stringValue();
            return name.equals(object.getName());
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
            _name.set((String) value);
            break;
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
        _name.set(_decodeStringField(0, fieldIterator));
    }

    /** Validate the enablements and values of all the elements.
     *
     *  @exception ValidationException If some elements are invalid.
     */
    @Override
    public void validate() throws ValidationException {
        if (_name.get().equals("")) {
            throw new ValidationException("Name must not be empty.");
        }
    }

    /** The elements.
     */
    private static final CriterionElement[] _ELEMENTS = { new StringCriterionElement(
            "name", false, false, true) };

    /** Value of the name element.
     */
    private PtolemyExpressionString _name;
}
