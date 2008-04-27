/*

 Copyright (c) 2003-2008 The Regents of the University of California.
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
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;

//////////////////////////////////////////////////////////////////////////
//// SubclassRule

/**

@author Thomas Huining Feng
@version $Id$
@since Ptolemy II 6.1
@Pt.ProposedRating Red (tfeng)
@Pt.AcceptedRating Red (tfeng)
*/
public class RenameOperation extends Operation {

    public RenameOperation(GTIngredientList owner) {
        this(owner, "");
    }

    public RenameOperation(GTIngredientList owner, String values) {
        super(owner, 1);

        NamedObj container = owner.getOwner().getContainer();
        _name = new PtolemyExpressionString(container, "");
        setValues(values);
    }

    public ChangeRequest getChangeRequest(Pattern pattern,
            Replacement replacement, MatchResult matchResult,
            Entity patternEntity, Entity replacementEntity, Entity hostEntity)
    throws IllegalActionException {
        if (_valueParseTree == null) {
            _reparse();
        }

        ParserScope scope = NamedObjVariable.getNamedObjVariable(hostEntity,
                true).getParserScope();
        GTParameter.Evaluator evaluator = new GTParameter.Evaluator(pattern,
                matchResult);

        String name;
        if (_valueParseTree instanceof ASTPtSumNode) {
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < _valueParseTree.jjtGetNumChildren(); i++) {
                ASTPtRootNode child = (ASTPtRootNode) _valueParseTree
                        .jjtGetChild(i);
                if (!(child.isConstant()
                        && child.getToken() instanceof StringToken)) {
                    ASTPtLeafNode newNode = _evaluate(child, evaluator, scope);
                    buffer.append(_parseTreeWriter
                            .parseTreeToExpression(newNode));
                } else {
                    buffer.append(((StringToken) child.getToken())
                            .stringValue());
                }
            }
            name = buffer.toString();
        } else if (!(_valueParseTree.isConstant()
                && _valueParseTree.getToken() instanceof StringToken)) {
            ASTPtRootNode newRoot = _evaluate(_valueParseTree, evaluator,
                    scope);
            name = _parseTreeWriter.parseTreeToExpression(newRoot);
        } else {
            name = _name.get();
        }

        NamedObj parent = hostEntity.getContainer();
        String moml = "<entity name=\"" + hostEntity.getName()
                + "\"><rename name=\"" + name + "\"/></entity>";
        return new MoMLChangeRequest(this, parent, moml, null);
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

    public void setName(String name) {
        _name.set(name);
        _valueParseTree = null;
    }

    public void setValue(int index, Object value) {
        switch (index) {
        case 0:
            setName((String) value);
            break;
        }
    }

    public void setValues(String values) {
        FieldIterator fieldIterator = new FieldIterator(values);
        setName(_decodeStringField(0, fieldIterator));
    }

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

    protected void _reparse() throws IllegalActionException {
        _valueParseTree = _parser.generateStringParseTree(_name.get());
    }

    private static final OperationElement[] _ELEMENTS = {
        new StringOperationElement("name", false, true)
    };

    private PtolemyExpressionString _name;

    private PtParser _parser = new PtParser();

    private ASTPtRootNode _valueParseTree;
}
