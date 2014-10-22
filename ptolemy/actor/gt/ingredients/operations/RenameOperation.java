/* An operation to rename an object.

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
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;

//////////////////////////////////////////////////////////////////////////
//// RenameOperation

/**
 An operation to rename an object.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class RenameOperation extends Operation {

    /** Construct an operation within the given list as its owner. All elements
     *  are enabled and are initialized to empty at the beginning.
     *
     *  @param owner The list as the owner of the constructed GTIngredientList.
     */
    public RenameOperation(GTIngredientList owner) {
        this(owner, "");
    }

    /** Construct an operation within the given list as its owner and initialize
     *  all the elements with the given values, which are a string encoding of
     *  those elements. All elements are enabled at the beginning.
     *
     *  @param owner The list as the owner of the constructed GTIngredientList.
     *  @param values The string encoding of the values of the elements.
     */
    public RenameOperation(GTIngredientList owner, String values) {
        super(owner, 1);

        NamedObj container = owner.getOwner().getContainer();
        _name = new PtolemyExpressionString(container, "");
        setValues(values);
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
     *    object in the replacement.
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

        ParserScope scope = NamedObjVariable.getNamedObjVariable(hostObject,
                true).getParserScope();
        GTParameter.Evaluator evaluator = new GTParameter.Evaluator(pattern,
                matchResult);

        String name;
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
            name = buffer.toString();
        } else if (!(_valueParseTree.isConstant() && _valueParseTree.getToken() instanceof StringToken)) {
            ASTPtRootNode newRoot = _evaluate(_valueParseTree, evaluator, scope);
            name = _parseTreeWriter.parseTreeToExpression(newRoot);
        } else {
            name = _name.get();
        }

        NamedObj parent = hostObject.getContainer();
        String moml = "<entity name=\"" + hostObject.getName()
                + "\"><rename name=\"" + name + "\"/></entity>";
        return new MoMLChangeRequest(this, parent, moml, null);
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

    /** Set value of the name element.
     *
     *  @param name Value of the name element.
     */
    public void setName(String name) {
        _name.set(name);
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
            setName((String) value);
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
        setName(_decodeStringField(0, fieldIterator));
    }

    /** Validate the enablements and values of all the elements.
     *
     *  @exception ValidationException If some elements are invalid.
     */
    @Override
    public void validate() throws ValidationException {
        if (_valueParseTree == null) {
            try {
                _reparse();
            } catch (IllegalActionException e) {
                throw new ValidationException(
                        "Unable to parse attribute value.");
            }
        }
    }

    /** Generate the parse tree for the name element again.
     *
     *  @exception IllegalActionException If error occurs in the parse tree
     *   generation.
     */
    protected void _reparse() throws IllegalActionException {
        _valueParseTree = new PtParser().generateStringParseTree(_name.get());
    }

    /** The elements.
     */
    private static final OperationElement[] _ELEMENTS = { new StringOperationElement(
            "name", false, true) };

    /** The name element.
     */
    private PtolemyExpressionString _name;

    /** Parse tree of the name element.
     */
    private ASTPtRootNode _valueParseTree;
}
