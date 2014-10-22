/* An operation to add or change an attribute.

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
package ptolemy.actor.gt.ingredients.operations;

import ptolemy.actor.gt.GTIngredientElement;
import ptolemy.actor.gt.GTIngredientList;
import ptolemy.actor.gt.GTParameter;
import ptolemy.actor.gt.NamedObjVariable;
import ptolemy.actor.gt.Pattern;
import ptolemy.actor.gt.Replacement;
import ptolemy.actor.gt.ValidationException;
import ptolemy.actor.gt.data.MatchResult;
import ptolemy.actor.gt.util.PtolemyExpressionString;
import ptolemy.data.StringToken;
import ptolemy.data.expr.ASTPtLeafNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ASTPtSumNode;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.PtParser;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// AttributeOperation

/**
 An operation to add or change an attribute.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class AttributeOperation extends Operation {

    /** Construct an operation within the given list as its owner. All elements
     *  are enabled and are initialized to empty at the beginning.
     *
     *  @param owner The list as the owner of the constructed GTIngredientList.
     */
    public AttributeOperation(GTIngredientList owner) {
        this(owner, "");
    }

    /** Construct an operation within the given list as its owner and initialize
     *  all the elements with the given values, which are a string encoding of
     *  those elements. All elements are enabled at the beginning.
     *
     *  @param owner The list as the owner of the constructed GTIngredientList.
     *  @param values The string encoding of the values of the elements.
     */
    public AttributeOperation(GTIngredientList owner, String values) {
        this(owner, null, null, null);
        setValues(values);
    }

    /** Construct an operation within the given list as its owner and with the
     *  given value to each of its elements..
     *
     *  @param owner The list as the owner of the constructed GTIngredientList.
     *  @param attributeName Value of the attributeName element.
     *  @param attributeClass Value of the attributeClass element.
     *  @param attributeValue Value of the attributeValue element.
     */
    public AttributeOperation(GTIngredientList owner, String attributeName,
            String attributeClass, String attributeValue) {
        super(owner, 3);

        NamedObj container = owner.getOwner().getContainer();
        _attributeName = attributeName;
        _attributeClass = attributeClass;
        _attributeValue = new PtolemyExpressionString(container, attributeValue);
    }

    /** Get the change request to update the object in the host model.
     *
     *  @param pattern The pattern of the transformation rule.
     *  @param replacement The replacement of the transformation rule.
     *  @param matchResult The match result.
     *  @param patternObject The object in the pattern, or null.
     *  @param replacementObject The object in the replacement that corresponds
     *   to the object in the pattern.
     *  @param hostObject The object in the host model corresponding to the
     *   object in the replacement.
     *  @return The change request.
     *  @exception IllegalActionException If error occurs in generating the
     *   change request.
     */
    @Override
    public ChangeRequest getChangeRequest(Pattern pattern,
            Replacement replacement, MatchResult matchResult,
            NamedObj patternObject, NamedObj replacementObject,
            NamedObj hostObject) throws IllegalActionException {
        if (_valueParseTree == null) {
            _reparse();
        }

        String attributeClass;
        if (isAttributeClassEnabled()) {
            attributeClass = _attributeClass;
        } else {
            Attribute oldAttribute = hostObject.getAttribute(_attributeName);
            if (oldAttribute == null) {
                throw new IllegalActionException(
                        "Unable to determine the class" + " of attribute "
                                + _attributeName + " for entity " + hostObject
                                + ".");
            }
            attributeClass = oldAttribute.getClassName();
        }

        ParserScope scope = NamedObjVariable.getNamedObjVariable(hostObject,
                true).getParserScope();
        GTParameter.Evaluator evaluator = new GTParameter.Evaluator(pattern,
                matchResult);
        String expression;
        if (_valueParseTree instanceof ASTPtSumNode) {
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < _valueParseTree.jjtGetNumChildren(); i++) {
                ASTPtRootNode child = (ASTPtRootNode) _valueParseTree
                        .jjtGetChild(i);
                if (!(child.isConstant() && child.getToken() instanceof StringToken)) {
                    ASTPtLeafNode newNode = _evaluate(child, evaluator, scope);
                    buffer.append(_parseTreeWriter
                            .parseTreeToExpression(newNode));
                } else {
                    buffer.append(((StringToken) child.getToken())
                            .stringValue());
                }
            }
            expression = buffer.toString();
        } else if (!(_valueParseTree.isConstant() && _valueParseTree.getToken() instanceof StringToken)) {
            ASTPtRootNode newRoot = _evaluate(_valueParseTree, evaluator, scope);
            expression = _parseTreeWriter.parseTreeToExpression(newRoot);
        } else {
            expression = _attributeValue.get();
        }
        String moml = "<property name=\"" + _attributeName + "\" class=\""
                + attributeClass + "\" value=\""
                + StringUtilities.escapeForXML(expression) + "\"/>";
        return new MoMLChangeRequest(this, hostObject, moml, null);
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
            return _attributeClass;
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
        _encodeStringField(buffer, 0, _attributeName);
        _encodeStringField(buffer, 1, _attributeClass);
        _encodeStringField(buffer, 2, _attributeValue.get());
        return buffer.toString();
    }

    /** Return whether the attributeClass element is enabled.
     *
     *  @return true if the attributeClass element is enabled.
     */
    public boolean isAttributeClassEnabled() {
        return isEnabled(1);
    }

    /** Return whether the attributeName element is enabled.
     *
     *  @return true if the attributeName element is enabled.
     */
    public boolean isAttributeNameEnabled() {
        return isEnabled(0);
    }

    /** Return whether the attributeValue element is enabled.
     *
     *  @return true if the attributeValue element is enabled.
     */
    public boolean isAttributeValueEnabled() {
        return isEnabled(2);
    }

    /** Set the value of the attributeClass element.
     *
     *  @param attributeClass The value of the attributeClass element.
     */
    public void setAttributeClass(String attributeClass) {
        _attributeClass = attributeClass;
    }

    /** Set the value of the attributeName element.
     *
     *  @param attributeName The value of the attributeName element.
     */
    public void setAttributeName(String attributeName) {
        _attributeName = attributeName;
    }

    /** Set the value of the attributeValue element.
     *
     *  @param attributeValue The value of the attributeValue element.
     */
    public void setAttributeValue(String attributeValue) {
        _attributeValue.set(attributeValue);
        _valueParseTree = null;
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
            _attributeName = (String) value;
            break;
        case 1:
            _attributeClass = (String) value;
            break;
        case 2:
            setAttributeValue((String) value);
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
        _attributeName = _decodeStringField(0, fieldIterator);
        _attributeClass = _decodeStringField(1, fieldIterator);
        setAttributeValue(_decodeStringField(2, fieldIterator));
    }

    /** Validate the enablements and values of all the elements.
     *
     *  @exception ValidationException If some elements are invalid.
     */
    @Override
    public void validate() throws ValidationException {
        if (_attributeName.equals("")) {
            throw new ValidationException("Name must not be empty.");
        }
        if (_attributeName.contains(".")) {
            throw new ValidationException("Name must not have period (\".\") "
                    + "in it.");
        }

        if (isAttributeClassEnabled()) {
            if (_attributeClass.equals("")) {
                throw new ValidationException("Class must not be empty.");
            }

            try {
                Class.forName(_attributeClass);
            } catch (Throwable t) {
                throw new ValidationException("Cannot load class \""
                        + _attributeClass + "\".", t);
            }
        }

        if (_valueParseTree == null) {
            try {
                _reparse();
            } catch (IllegalActionException e) {
                throw new ValidationException(
                        "Unable to parse attribute value.");
            }
        }
    }

    /** Generate the parse tree for the attributeValue element again.
     *
     *  @exception IllegalActionException If error occurs in the parse tree
     *   generation.
     */
    protected void _reparse() throws IllegalActionException {
        _valueParseTree = new PtParser()
                .generateStringParseTree(_attributeValue.get());
    }

    /** The elements.
     */
    private static final OperationElement[] _ELEMENTS = {
            new StringOperationElement("name", false, false),
            new StringOperationElement("type", true, false),
            new StringOperationElement("value", false, true) };

    /** Value of the attributeClass element.
     */
    private String _attributeClass;

    /** Value of the attributeName element.
     */
    private String _attributeName;

    /** Value of the attributeValue element.
     */
    private PtolemyExpressionString _attributeValue;

    /** Parse tree of the attributeValue element.
     */
    private ASTPtRootNode _valueParseTree;
}
