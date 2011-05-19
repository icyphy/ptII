/* A code manager that manages the extra complexity of dealing with nested actors.

 Copyright (c) 2006-2007 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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
package ptolemy.apps.ptalon.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Stack;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.actor.ptalon.PtalonTokenTypes;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.PtParser;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.util.StringUtilities;
import antlr.collections.AST;

/**
 * This class has a single static method, evaluate, which takes a typed
 * composite actor and a PtalonAST as arguments and attempts to populate the
 * actor with the specified model.
 * 
 * @author Adam Cataldo
 * 
 */
public final class PtalonModelEvaluator {

    public TypedCompositeActor getActor() {
        return _actor;
    }

    public String getActualName(String ptalonName) {
        return _nameMap.get(ptalonName);
    }

    /**
     * Create a new PtalonEvaluator.
     * 
     * @param actor
     *            The ptalon actor for this manager.
     */
    public boolean evaluate(TypedCompositeActor actor, AST ast)
            throws IllegalActionException {
        _actor = actor;
        Hashtable<String, Parameter> parameters = new Hashtable<String, Parameter>();
        for (Parameter p : PtalonModel.parameterList(actor)) {
            if (_nameMap.containsValue(p.getName())) {
                parameters.put(_nameMap.getKey(p.getName()), p);
            }
        }
        _scope = new PtalonModelScope(parameters);
        if (ast.getType() == PtalonTokenTypes.ACTOR_DEFINITION) {
            try {
                boolean changed = false;
                String desiredName = ast.getText();
                if (!_nameMap.containsKey(desiredName)) {
                    String actualName = actor.uniqueName(desiredName);
                    _nameMap.put(desiredName, actualName);
                    actor.setName(actualName);
                    changed = true;
                }
                AST child = ast.getFirstChild();
                while (child != null) {
                    changed |= evaluateStatement(child);
                    child = child.getNextSibling();
                }
                return changed;
            } catch (NameDuplicationException e) {
                throw new IllegalActionException(actor, e, e.getMessage());
            }
        } else {
            throw new IllegalActionException("Expecting an ACTOR tree.");
        }
    }

    private boolean evaluateStatement(AST ast) throws IllegalActionException,
            NameDuplicationException {
        switch (ast.getType()) {
        case PtalonTokenTypes.DANGLING_PORTS_OKAY:
        case PtalonTokenTypes.ATTACH_DANGLING_PORTS:
        case PtalonTokenTypes.PARAM_EQUALS:
        case PtalonTokenTypes.ACTOR:
        case PtalonTokenTypes.IF:
            break;
        case PtalonTokenTypes.PORT:
            return addPort(ast);
        case PtalonTokenTypes.MULTIPORT:
            return addMultiPort(ast);
        case PtalonTokenTypes.INPORT:
            return addInPort(ast);
        case PtalonTokenTypes.MULTIINPORT:
            return addMultiInPort(ast);
        case PtalonTokenTypes.OUTPORT:
            return addOutPort(ast);
        case PtalonTokenTypes.MULTIOUTPORT:
            return addMultiOutPort(ast);
        case PtalonTokenTypes.PARAMETER:
            return addParameter(ast);
        case PtalonTokenTypes.ACTOR_EQUALS:
            return addActorDefinition(ast);
        case PtalonTokenTypes.RELATION:
            return addRelation(ast);
        case PtalonTokenTypes.TRANSPARENT:
            return addTransparentRelation(ast);
        case PtalonTokenTypes.ACTOR_DECLARATION:
            return addActorDeclaration(ast);
        case PtalonTokenTypes.FOR:
            return addForLoop(ast);
        }
        return false;
    }

    private boolean addForLoop(AST ast) throws IllegalActionException,
            NameDuplicationException {
        AST variableAst = ast.getFirstChild().getFirstChild();
        AST initialAst = ast.getFirstChild().getNextSibling().getFirstChild();
        AST conditionalAst = ast.getFirstChild().getNextSibling()
                .getNextSibling().getFirstChild();
        AST nextAst = ast.getFirstChild();
        while (nextAst.getNextSibling() != null) {
            nextAst = nextAst.getNextSibling();
        }
        Token currentToken = evaluateExpression(initialAst.getText());
        if (currentToken == null) {
            return false;
        }
        String varName = _scope.uniqueName(variableAst.getText());
        _scope.addVariable(varName, currentToken);
        BooleanToken execute = (BooleanToken) evaluateExpression(conditionalAst
                .getText());
        if (execute == null) {
            _scope.removeName(varName);
            return false;
        }
        AST firstStatementAst = ast.getFirstChild().getNextSibling()
                .getNextSibling().getNextSibling();
        boolean changed = false;
        while (execute.booleanValue()) {
            _forTokens.push(currentToken);
            AST statementAst = firstStatementAst;
            while (statementAst != nextAst) {
                changed |= evaluateStatement(statementAst);
                statementAst = statementAst.getNextSibling();
            }
            currentToken = evaluateExpression(nextAst.getFirstChild().getText());
            _scope.removeName(varName);
            _scope.addVariable(varName, currentToken);
            execute = (BooleanToken) evaluateExpression(conditionalAst
                    .getText());
            _forTokens.pop();
        }
        _scope.removeName(varName);
        return changed;
    }

    private PtalonModelScope _scope;

    public Token evaluateExpression(String expression) {
        try {
            PtParser parser = new PtParser();
            ParseTreeEvaluator evaluator = new ParseTreeEvaluator();
            ASTPtRootNode node = parser.generateParseTree(expression);
            return evaluator.evaluateParseTree(node, _scope);
        } catch (Exception ex) {
            return null;
        }
    }

    private String tryGetActualName(AST idAst) {
        String name = tryGetExpressionPtalonName(idAst);
        if (name == null) {
            return null;
        }
        if (_nameMap.containsKey(name)) {
            return null;
        }
        String actualName = _actor.uniqueName(name);
        _nameMap.put(name, actualName);
        return actualName;
    }

    public String tryGetExpressionPtalonName(AST idAst) {
        String name = null;
        if (idAst.getType() == PtalonTokenTypes.ID) {
            name = idAst.getText();
        } else if (idAst.getType() == PtalonTokenTypes.DYNAMIC_NAME) {
            idAst = idAst.getFirstChild();
            AST expr = idAst.getNextSibling();
            Token t = evaluateExpression(expr.getText());
            if (t != null) {
                name = idAst.getText() + t.toString();
            } else {
                return null;
            }
        } else {
            return null;
        }
        return name;
    }

    private boolean addActorDeclaration(AST ast) throws IllegalActionException,
            NameDuplicationException {
        LoopedAstReference reference = new LoopedAstReference(ast, _forTokens);
        if (!_actorInstances.containsKey(reference)) {
            String name = ast.getText();
            if (name.equals("this")) {
                ActorInstantiator instantiator = new ActorInstantiator(this,
                        _actor);
                _actorInstances.put(reference, instantiator);
            } else if (_actorClasses.containsKey(name)) {
                ActorInstantiator instantiator = new ActorInstantiator(this,
                        _actorClasses.get(name));
                _actorInstances.put(reference, instantiator);
            } else if (_ptalonActors.containsKey(name)) {
                ActorInstantiator instantiator = new ActorInstantiator(this,
                        _ptalonActors.get(name));
                _actorInstances.put(reference, instantiator);
            } else {
                return false;
            }
        }
        return _actorInstances.get(reference).evaluate(ast);
    }

    private Stack<Token> _forTokens = new Stack<Token>();

    private boolean addActorDefinition(AST ast) throws IllegalActionException,
            NameDuplicationException {
        String actorName = ast.getFirstChild().getFirstChild().getText();
        String className = ast.getFirstChild().getNextSibling().getText();
        if (_actorClasses.containsKey(actorName)) {
            return false;
        }
        if (_ptalonActors.containsKey(actorName)) {
            return false;
        }
        if (className.startsWith("ptalonActor:")) {
            String name = className.substring(12);
            name = name.replace(".", "/");
            name = name + ".ptln";
            name = StringUtilities.getProperty("ptolemy.ptII.dirAsURL") + name;
            try {
                URI location = new URI(name);
                _ptalonActors.put(actorName, location);
            } catch (URISyntaxException e) {
                throw new IllegalActionException(_actor, e, e.getMessage());
            }
        } else {
            ClassLoader loader = ClassLoader.getSystemClassLoader();
            try {
                Class<?> c = loader.loadClass(className);
                _actorClasses.put(actorName,
                        (Class<? extends ComponentEntity>) c);
            } catch (ClassNotFoundException e) {
                throw new IllegalActionException(_actor, e, e.getMessage());
            }
        }
        return true;
    }

    private boolean addTransparentRelation(AST ast)
            throws IllegalActionException, NameDuplicationException {
        String ptalonName = tryGetExpressionPtalonName(ast.getFirstChild());
        if (ptalonName == null) {
            return false;
        }
        TransparentRelation tr = new TransparentRelation(ptalonName);
        if (_transparentRelations.containsKey(tr)) {
            return false;
        }
        _transparentRelations.put(tr, tr);
        return true;
    }

    public Hashtable<TransparentRelation, TransparentRelation> getTransparentRelations() {
        return _transparentRelations;
    }

    private boolean addRelation(AST ast) throws IllegalActionException,
            NameDuplicationException {
        String actualName = tryGetActualName(ast.getFirstChild());
        if (actualName == null) {
            return false;
        }
        new TypedIORelation(_actor, actualName);
        return true;
    }

    private boolean addParameter(AST ast) throws IllegalActionException,
            NameDuplicationException {
        String actualName = tryGetActualName(ast.getFirstChild());
        if (actualName == null) {
            return false;
        }
        new Parameter(_actor, actualName);
        return true;
    }

    private boolean addPortBase(AST idAst, boolean isInput, boolean isOutput,
            boolean isMultiport) throws IllegalActionException,
            NameDuplicationException {
        String actualName = tryGetActualName(idAst);
        if (actualName == null) {
            return false;
        }
        TypedIOPort port = new TypedIOPort(_actor, actualName, isInput,
                isOutput);
        if (isMultiport) {
            port.setMultiport(true);
        }
        return true;
    }

    private boolean addPort(AST ast) throws IllegalActionException,
            NameDuplicationException {
        return addPortBase(ast.getFirstChild(), true, true, false);
    }

    private boolean addMultiPort(AST ast) throws IllegalActionException,
            NameDuplicationException {
        return addPortBase(ast.getFirstChild(), true, true, true);
    }

    private boolean addInPort(AST ast) throws IllegalActionException,
            NameDuplicationException {
        return addPortBase(ast.getFirstChild(), true, false, false);
    }

    private boolean addMultiInPort(AST ast) throws IllegalActionException,
            NameDuplicationException {
        return addPortBase(ast.getFirstChild(), true, false, true);
    }

    private boolean addOutPort(AST ast) throws IllegalActionException,
            NameDuplicationException {
        return addPortBase(ast.getFirstChild(), false, true, false);
    }

    private boolean addMultiOutPort(AST ast) throws IllegalActionException,
            NameDuplicationException {
        return addPortBase(ast.getFirstChild(), false, true, true);
    }

    private TypedCompositeActor _actor;

    private ReversableHashtable<String, String> _nameMap = new ReversableHashtable<String, String>();

    private Hashtable<String, Class<? extends ComponentEntity>> _actorClasses = new Hashtable<String, Class<? extends ComponentEntity>>();

    private Hashtable<String, URI> _ptalonActors = new Hashtable<String, URI>();

    private Hashtable<LoopedAstReference, ActorInstantiator> _actorInstances = new Hashtable<LoopedAstReference, ActorInstantiator>();

    private Hashtable<TransparentRelation, TransparentRelation> _transparentRelations = new Hashtable<TransparentRelation, TransparentRelation>();

};
