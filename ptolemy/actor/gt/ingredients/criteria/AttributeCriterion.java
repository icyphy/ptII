/* A criterion to constrain an attribute of an object in the host model.

 Copyright (c) 2003-2014 The Regents of the University of California.
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import ptolemy.actor.gt.GTIngredientElement;
import ptolemy.actor.gt.GTIngredientList;
import ptolemy.actor.gt.ValidationException;
import ptolemy.actor.gt.util.PtolemyExpressionString;
import ptolemy.actor.gt.util.RegularExpressionString;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.Constants;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.PtParser;
import ptolemy.data.type.Type;
import ptolemy.domains.ptera.kernel.VariableScope;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

//////////////////////////////////////////////////////////////////////////
//// AttributeCriterion

/**
 A criterion to constrain an attribute of an object in the host model.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class AttributeCriterion extends Criterion {

    /** Construct a criterion within the given list as its owner. All elements
     *  are enabled and are initialized to empty at the beginning.
     *
     *  @param owner The list as the owner of the constructed GTIngredientList.
     */
    public AttributeCriterion(GTIngredientList owner) {
        this(owner, "");
    }

    /** Construct a criterion within the given list as its owner and initialize
     *  all the elements with the given values, which are a string encoding of
     *  those elements. All elements are enabled at the beginning.
     *
     *  @param owner The list as the owner of the constructed GTIngredientList.
     *  @param values The string encoding of the values of the elements.
     */
    public AttributeCriterion(GTIngredientList owner, String values) {
        this(owner, null, null, null);
        setValues(values);
    }

    /** Construct a criterion within the given list as its owner and with the
     *  given value to each of its elements..
     *
     *  @param owner The list as the owner of the constructed GTIngredientList.
     *  @param attributeName Value of the attributeName element.
     *  @param attributeType Value of the attributeType element.
     *  @param attributeValue Value of the attributeValue element.
     */
    public AttributeCriterion(GTIngredientList owner, String attributeName,
            String attributeType, String attributeValue) {
        super(owner, 3);

        NamedObj container = owner.getOwner().getContainer();
        _attributeName = new RegularExpressionString(attributeName);
        _attributeType = new PtolemyExpressionString(container, attributeType);
        _attributeValue = new PtolemyExpressionString(container, attributeValue);
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
            return _attributeName;
        case 1:
            return _attributeType;
        case 2:
            return _attributeValue;
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
        _encodeStringField(buffer, 0, _attributeName.get());
        _encodeStringField(buffer, 1, _attributeType.get());
        _encodeStringField(buffer, 2, _attributeValue.get());
        return buffer.toString();
    }

    /** Return whether the attributeName element is enabled.
     *
     *  @return true if the attributeName element is enabled.
     */
    public boolean isAttributeNameEnabled() {
        return isEnabled(0);
    }

    /** Return whether the attributeType element is enabled.
     *
     *  @return true if the attributeType element is enabled.
     */
    public boolean isAttributeTypeEnabled() {
        return isEnabled(1);
    }

    /** Return whether the attributeValue element is enabled.
     *
     *  @return true if the attributeValue element is enabled.
     */
    public boolean isAttributeValueEnabled() {
        return isEnabled(2);
    }

    /** Test whether the given object in the host model matches the object in
     *  the pattern that has this criterion.
     *
     *  @param object The object.
     *  @return true if the object matches.
     */
    @Override
    public boolean match(NamedObj object) {
        for (Object attributeObject : object.attributeList()) {
            Attribute attribute = (Attribute) attributeObject;

            if (isAttributeNameEnabled()) {
                Pattern pattern = _attributeName.getPattern();
                Matcher matcher = pattern.matcher(attribute.getName());
                if (!matcher.matches()) {
                    continue;
                }
            }

            if (isAttributeTypeEnabled() || isAttributeValueEnabled()) {
                if (attribute instanceof Settable) {
                    Settable settable = (Settable) attribute;
                    String expression = settable.getExpression();
                    VariableScope scope = new VariableScope(object);
                    try {
                        ASTPtRootNode tree = new PtParser()
                        .generateParseTree(expression);
                        Token token = new ParseTreeEvaluator()
                        .evaluateParseTree(tree, scope);

                        if (isAttributeTypeEnabled()) {
                            Type ruleType = _attributeType.getToken().getType();
                            Type tokenType = token.getType();
                            if (!ruleType.isCompatible(tokenType)) {
                                continue;
                            }
                        }

                        if (isAttributeValueEnabled()) {
                            BooleanToken equality = token
                                    .isEqualTo(_attributeValue.getToken());
                            if (!equality.booleanValue()) {
                                continue;
                            }
                        }
                    } catch (IllegalActionException e) {
                        continue;
                    }
                } else {
                    continue;
                }
            }

            return true;
        }
        return false;
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
            _attributeName.set((String) value);
            break;
        case 1:
            _attributeType.set((String) value);
            break;
        case 2:
            _attributeValue.set((String) value);
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
        _attributeName.set(_decodeStringField(0, fieldIterator));
        _attributeType.set(_decodeStringField(1, fieldIterator));
        _attributeValue.set(_decodeStringField(2, fieldIterator));
    }

    /** Validate the enablements and values of all the elements.
     *
     *  @exception ValidationException If some elements are invalid.
     */
    @Override
    public void validate() throws ValidationException {
        if (isAttributeNameEnabled()) {
            if (_attributeName.get().equals("")) {
                throw new ValidationException(
                        "Attribute name must not be empty.");
            }

            try {
                _attributeName.getPattern();
            } catch (PatternSyntaxException e) {
                throw new ValidationException("Regular expression \""
                        + _attributeName + "\" cannot be compiled.", e);
            }
        }

        if (isAttributeTypeEnabled()) {
            if (_attributeType.get().equals("")) {
                throw new ValidationException("Port type must not be empty.");
            }

            try {
                _attributeType.getToken().getType();
            } catch (IllegalActionException e) {
                throw new ValidationException("Type expression \""
                        + _attributeType + "\" cannot be parsed.", e);
            }
        }

        if (isAttributeValueEnabled()) {
            try {
                _attributeValue.getToken();
            } catch (IllegalActionException e) {
                throw new ValidationException("Value expression \""
                        + _attributeValue + "\" cannot be parsed.", e);
            }
        }
    }

    /** The elements.
     */
    private static final CriterionElement[] _ELEMENTS = {
        new StringCriterionElement("name", false, false, false),
        new ChoiceCriterionElement("type", true, false, true, true),
        new StringCriterionElement("value", true, false, true) };

    /** Value of the attributeName element.
     */
    private RegularExpressionString _attributeName;

    /** Value of the attributeType element.
     */
    private PtolemyExpressionString _attributeType;

    /** Value of the attributeValue element.
     */
    private PtolemyExpressionString _attributeValue;

    static {
        ChoiceCriterionElement attributeTypes = (ChoiceCriterionElement) _ELEMENTS[1];
        attributeTypes.addChoices(Constants.types().keySet());
        attributeTypes.addChoice("arrayType(int)");
        attributeTypes.addChoice("arrayType(int,5)");
        attributeTypes.addChoice("[double]");
        attributeTypes.addChoice("{x=double, y=double}");
    }
}
