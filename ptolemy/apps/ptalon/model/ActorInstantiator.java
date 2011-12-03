package ptolemy.apps.ptalon.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashSet;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.actor.ptalon.PtalonTokenTypes;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.AbstractSettableAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import antlr.collections.AST;

public class ActorInstantiator {
    public ActorInstantiator(PtalonModelEvaluator evaluator, URI ptalonUrl) {
        this(evaluator);
        _ptalonUrl = ptalonUrl;
    }

    public ActorInstantiator(PtalonModelEvaluator evaluator,
            TypedCompositeActor thisReference) {
        this(evaluator);
        _entity = thisReference;
    }

    public ActorInstantiator(PtalonModelEvaluator evaluator,
            Class<? extends ComponentEntity> actorClass) {
        this(evaluator);
        _actorClass = actorClass;
    }

    private ActorInstantiator(PtalonModelEvaluator evaluator) {
        _evaluator = evaluator;
        _actor = evaluator.getActor();
    }

    public boolean evaluate(AST ast) throws IllegalActionException,
            NameDuplicationException {
        boolean changed = false;
        changed |= createEntity(ast);
        AST assignmentAst = ast.getFirstChild();
        while (assignmentAst != null) {
            changed |= processAssignment(assignmentAst);
            assignmentAst = assignmentAst.getNextSibling();
        }
        return changed;
    }

    private boolean createEntity(AST ast) throws IllegalActionException,
            NameDuplicationException {
        if (_entity != null) {
            return false;
        }
        String actorNameInPtalon = ast.getText();
        try {
            if (_actorClass != null) {
                Constructor<? extends ComponentEntity> constructor = _actorClass
                        .getConstructor(CompositeEntity.class, String.class);
                _entity = constructor.newInstance(_actor,
                        _actor.uniqueName(actorNameInPtalon));
                return true;
            }
        } catch (SecurityException e) {
            throw new IllegalActionException(_actor, e, e.getMessage());
        } catch (NoSuchMethodException e) {
            throw new IllegalActionException(_actor, e, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new IllegalActionException(_actor, e, e.getMessage());
        } catch (InstantiationException e) {
            throw new IllegalActionException(_actor, e, e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IllegalActionException(_actor, e, e.getMessage());
        } catch (InvocationTargetException e) {
            throw new IllegalActionException(_actor, e, e.getMessage());
        }
        try {
            if (_ptalonUrl != null) {
                PtalonModel model = new PtalonModel(_actor,
                        _actor.uniqueName(actorNameInPtalon));
                File file = new File(_ptalonUrl);
                FileReader reader = null;
                StringBuffer buffer = null;
                try {
                    reader = new FileReader(file);
                    buffer = new StringBuffer();
                    char[] c = new char[1024];
                    int i = 0;
                    while ((i = reader.read(c, 0, 1024)) > 0) {
                        buffer.append(c, 0, i);
                    }
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }

                String result = buffer.toString();
                model.setCode(result);
                model.updateModel();
                _entity = model;
                return true;
            }
        } catch (FileNotFoundException e) {
            throw new IllegalActionException(_actor, e, e.getMessage());
        } catch (IOException e) {
            throw new IllegalActionException(_actor, e, e.getMessage());
        }
        return false;
    }

    private boolean processAssignment(AST ast) throws IllegalActionException,
            NameDuplicationException {
        String lhs = _evaluator.tryGetExpressionPtalonName(ast.getFirstChild());
        if (lhs == null) {
            return false;
        }
        AST rightAst = ast.getFirstChild().getNextSibling();
        if (rightAst.getType() == PtalonTokenTypes.EXPRESSION) {
            Attribute att = _entity.getAttribute(lhs);
            if (att instanceof Variable) {
                return assignVariable((Variable) att, rightAst);
            } else if (att instanceof AbstractSettableAttribute) {
                return assignAbstractSettableAttribute(
                        (AbstractSettableAttribute) att, rightAst);
            }
            Parameter p = new Parameter(_entity, _entity.uniqueName(lhs));
            return assignVariable(p, rightAst);
        } else {
            TypedIOPort leftPort = (TypedIOPort) _entity.getPort(lhs);
            if (leftPort != null) {
                return attachPort(leftPort, rightAst);
            }
            String rhs = _evaluator.tryGetExpressionPtalonName(rightAst);
            if (rhs != null) {
                TypedIOPort rightPort = (TypedIOPort) _actor.getPort(rhs);
                if (rightPort != null) {
                    leftPort = createPort(lhs, rightPort);
                    return attachPort(leftPort, rightAst);
                }
                TypedIORelation rightRelation = (TypedIORelation) _actor
                        .getRelation(rhs);
                if (rightRelation != null) {
                    leftPort = createPort(lhs, rightRelation);
                    return attachPort(leftPort, rightAst);
                }
                TransparentRelation tr = new TransparentRelation(rhs);
                if (_evaluator.getTransparentRelations().contains(tr)) {
                    tr = _evaluator.getTransparentRelations().get(tr);
                    if (tr.hasInitialPortBeenSet()) {
                        leftPort = createPort(lhs, tr.getPort());
                        return attachPort(leftPort, rightAst);
                    }
                }
            }
        }
        return false;
    }

    private boolean assignVariable(Variable leftVariable, AST rightAst)
            throws IllegalActionException, NameDuplicationException {
        if (_setParameters.contains(leftVariable)) {
            return false;
        }
        String rhs = rightAst.getText();
        Token token = _evaluator.evaluateExpression(rhs);
        if (token == null) {
            return false;
        }
        leftVariable.setToken(token);
        _setParameters.add(leftVariable);
        return true;
    }

    private boolean assignAbstractSettableAttribute(
            AbstractSettableAttribute leftVariable, AST rightAst)
            throws IllegalActionException, NameDuplicationException {
        if (_setParameters.contains(leftVariable)) {
            return false;
        }
        String rhs = rightAst.getText();
        leftVariable.setExpression(rhs);
        _setParameters.add(leftVariable);
        return true;
    }

    private TypedIOPort createPort(String leftName,
            TypedIORelation rightRelation) throws IllegalActionException,
            NameDuplicationException {
        boolean isInput = false;
        boolean isOutput = false;
        for (Object x : rightRelation.linkedPortList()) {
            TypedIOPort p = (TypedIOPort) x;
            if ((p.isInput() && p.getContainer() == _actor)
                    || (p.isOutput() && p.getContainer() != _actor)) {
                isInput = true;
            }
            if ((p.isOutput() && p.getContainer() == _actor)
                    || (p.isInput() && p.getContainer() != _actor)) {
                isOutput = true;
            }
        }
        if (!isInput && !isOutput) {
            isInput = true;
            isOutput = true;
        }
        return new TypedIOPort(_entity, _entity.uniqueName(leftName), isInput,
                isOutput);
    }

    private TypedIOPort createPort(String leftName, TypedIOPort rightPort)
            throws IllegalActionException, NameDuplicationException {
        TypedIOPort leftPort;
        if (rightPort.getContainer().equals(_actor)) {
            leftPort = new TypedIOPort(_entity, _entity.uniqueName(leftName),
                    rightPort.isInput(), rightPort.isOutput());
        } else {
            leftPort = new TypedIOPort(_entity, _entity.uniqueName(leftName),
                    rightPort.isOutput(), rightPort.isInput());
        }
        leftPort.setMultiport(rightPort.isMultiport());
        return leftPort;
    }

    private boolean attachPort(TypedIOPort leftPort, AST rightAst)
            throws IllegalActionException, NameDuplicationException {
        if (_connectedPorts.contains(leftPort)) {
            return false;
        }
        String rhs = _evaluator.tryGetExpressionPtalonName(rightAst);
        if (rhs == null) {
            return false;
        }
        String rightName = _evaluator.getActualName(rhs);
        if (rightName == null) {
            TransparentRelation tr = new TransparentRelation(rhs);
            if (_evaluator.getTransparentRelations().containsKey(tr)) {
                tr = _evaluator.getTransparentRelations().get(tr);
                if (tr.hasInitialPortBeenSet()) {
                    _actor.connect(leftPort, tr.getPort());
                } else {
                    tr.setInitialPort(leftPort);
                }
                _connectedPorts.add(leftPort);
                return true;
            }
            return false;
        }
        TypedIOPort rightPort = (TypedIOPort) _actor.getPort(rightName);
        if (rightPort != null) {
            _actor.connect(leftPort, rightPort);
            _connectedPorts.add(leftPort);
            return true;
        }
        TypedIORelation rightRelation = (TypedIORelation) _actor
                .getRelation(rightName);
        if (rightRelation != null) {
            leftPort.link(rightRelation);
            _connectedPorts.add(leftPort);
            return true;
        }
        return false;
    }

    private TypedCompositeActor _actor;

    private Class<? extends ComponentEntity> _actorClass;

    private URI _ptalonUrl;

    private ComponentEntity _entity;

    private HashSet<TypedIOPort> _connectedPorts = new HashSet<TypedIOPort>();

    private HashSet<AbstractSettableAttribute> _setParameters = new HashSet<AbstractSettableAttribute>();

    private PtalonModelEvaluator _evaluator;

}
