/*

 Copyright (c) 2003-2007 The Regents of the University of California.
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
import ptolemy.actor.gt.util.VariableScope;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.Constants;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.PtParser;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

//////////////////////////////////////////////////////////////////////////
//// AttributeCriterion

/**

@author Thomas Huining Feng
@version $Id$
@since Ptolemy II 6.1
@Pt.ProposedRating Red (tfeng)
@Pt.AcceptedRating Red (tfeng)
*/
public class AttributeCriterion extends Criterion {

    public AttributeCriterion(GTIngredientList owner) {
        this(owner, "");
    }

    public AttributeCriterion(GTIngredientList owner, String values) {
        this(owner, null, null, null);
        setValues(values);
    }

    public AttributeCriterion(GTIngredientList owner, String attributeName,
            String attributeType, String attributeValue) {
        super(owner, 3);

        NamedObj container = owner.getOwner().getContainer();
        _attributeName = new RegularExpressionString(attributeName);
        _attributeType = new PtolemyExpressionString(container, attributeType);
        _attributeValue = new PtolemyExpressionString(container, attributeValue);
    }

    public GTIngredientElement[] getElements() {
        return _ELEMENTS;
    }

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

    public String getValues() {
        StringBuffer buffer = new StringBuffer();
        _encodeStringField(buffer, 0, _attributeName.get());
        _encodeStringField(buffer, 1, _attributeType.get());
        _encodeStringField(buffer, 2, _attributeValue.get());
        return buffer.toString();
    }

    public boolean isAttributeNameEnabled() {
        return isEnabled(0);
    }

    public boolean isAttributeTypeEnabled() {
        return isEnabled(1);
    }

    public boolean isAttributeValueEnabled() {
        return isEnabled(2);
    }

    public NamedObjMatchResult match(NamedObj object) {
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
                        ASTPtRootNode tree = _TYPE_PARSER
                                .generateParseTree(expression);
                        Token token = _TYPE_EVALUATOR.evaluateParseTree(tree,
                                scope);

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

            return NamedObjMatchResult.MATCH;
        }
        return NamedObjMatchResult.NOT_MATCH;
    }

    public void setAttributeNameEnabled(boolean enabled) {
        setEnabled(0, enabled);
    }

    public void setAttributeTypeEnabled(boolean enabled) {
        setEnabled(1, enabled);
    }

    public void setAttributeValueEnabled(boolean enabled) {
        setEnabled(2, enabled);
    }

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

    public void setValues(String values) {
        FieldIterator fieldIterator = new FieldIterator(values);
        _attributeName.set(_decodeStringField(0, fieldIterator));
        _attributeType.set(_decodeStringField(1, fieldIterator));
        _attributeValue.set(_decodeStringField(2, fieldIterator));
    }

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

    private static final CriterionElement[] _ELEMENTS = {
            new StringCriterionElement("name", false, false, false),
            new ChoiceCriterionElement("type", true, false, true, true),
            new StringCriterionElement("value", true, false, true) };

    private static final ParseTreeEvaluator _TYPE_EVALUATOR = new ParseTreeEvaluator();

    private static final PtParser _TYPE_PARSER = new PtParser();

    private RegularExpressionString _attributeName;

    private PtolemyExpressionString _attributeType;

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
