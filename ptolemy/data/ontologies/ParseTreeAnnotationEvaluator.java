/* A visitor for parse trees of the expression language that infers properties.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.data.ontologies;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.expr.ASTPtAssignmentNode;
import ptolemy.data.expr.ASTPtLeafNode;
import ptolemy.data.expr.ASTPtMethodCallNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.AbstractParseTreeVisitor;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// ParseTreePropertyInference

/**
 This class visits parse trees and infers a property for each node in the
 parse tree.  This property is stored in the parse tree.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 @see ptolemy.data.expr.ASTPtRootNode
 */
public class ParseTreeAnnotationEvaluator extends AbstractParseTreeVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Infer the property of the parse tree with the specified root node using
     *  the specified scope to resolve the values of variables.
     *  @param node The root of the parse tree.
     *  @param adapter The given property adapter.
     *  @exception IllegalActionException If an error occurs during
     *   evaluation.
     */
    public void evaluate(ASTPtRootNode node, OntologyAdapter adapter)
            throws IllegalActionException {
        _adapter = adapter;
        node.visit(this);
    }

    /**
     * Visit the assignment node when parsing a user-defined ontology annotation.
     * This is for a manual annotation that assigns a Concept to a specified
     * model component.
     *
     * @param node The assignment node to be visited.
     * @exception IllegalActionException If the assignment is not possible.
     */
    @Override
    public void visitAssignmentNode(ASTPtAssignmentNode node)
            throws IllegalActionException {
        ((ASTPtRootNode) node.jjtGetChild(0)).visit(this);
        Object object = _evaluatedObject;

        node.getExpressionTree().visit(this);

        if (_evaluatedObject instanceof FiniteConcept) {
            _adapter.setEquals(object, (FiniteConcept) _evaluatedObject);
        } else {
            throw _unsupportedVisitException("Unknown assignment object: "
                    + _evaluatedObject);
        }
    }

    /**
     *  visitLeafNode method is called when parsing an Annotation for a manual constraint.
     *  Uncommented to get ontology solver to work.
     *  12/16/09 Charles Shelton
     *
     *  This visitLeafNode method assumes the node will refer to a component
     *  in the model and _evaluatedObject will be set to that component.
     *  If it is not, then an exception is thrown.
     *
     *  In the derived class ParseTreeConstraintAnnotationEvaluator for constraint
     *  annotations, the node could also refer to a Concept in the Ontology.
     *
     *  The derived class will override this method and catch its exception, then
     *  check to see if the node refers to a Concept rather than a model Component.
     *
     *  @param node The leaf node to be visited
     *  @exception IllegalActionException If the node label cannot be resolved to a
     *  component in the model
     */
    @Override
    public void visitLeafNode(ASTPtLeafNode node) throws IllegalActionException {
        _evaluatedObject = _resolveLabel(_getNodeLabel(node),
                _adapter.getComponent());

        if (_evaluatedObject == null) {
            throw _unsupportedVisitException("Cannot resolve label: "
                    + node.getName());
        }
        // FIXME: Not handling AST constraint yet.
    }

    /**
     * Visit the method node when parsing a user-defined ontology annotation.
     *
     * @param node The method call node to be visited
     * @exception IllegalActionException If the method label cannot be resolved.
     */
    @Override
    public void visitMethodCallNode(ASTPtMethodCallNode node)
            throws IllegalActionException {
        String name = node.getMethodName();
        ((ASTPtRootNode) node.jjtGetChild(0)).visit(this);

        _evaluatedObject = _resolveLabel(name, _evaluatedObject);

        // (x.y.step).getPort()
        if (name.equals("getPort")) {
            if (_evaluatedObject instanceof PortParameter) {
                _evaluatedObject = ((PortParameter) _evaluatedObject).getPort();
            }

            if (!(_evaluatedObject instanceof Port)) {
                _evaluatedObject = null;
            }

        } else if (name.equals("getParameter")) {
            if (_evaluatedObject instanceof ParameterPort) {
                _evaluatedObject = ((ParameterPort) _evaluatedObject)
                        .getParameter();
            }

            if (!(_evaluatedObject instanceof Parameter)) {
                _evaluatedObject = null;
            }
        }

        if (_evaluatedObject == null) {
            throw _unsupportedVisitException("Cannot resolve label: " + name);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Return the label for the leaf node.
     *
     * @param node The given leaf node
     * @return The string label for the node; If the node
     * is constant this is the token contained in the node
     * as a string, if not then this is the name of the node.
     */
    protected String _getNodeLabel(ASTPtLeafNode node) {
        if (node.isConstant()) {
            return node.getToken().toString();
        } else {
            return node.getName();
        }
    }

    /**
     * Return an exception that describes an unsupported node type.
     *
     * @param name The name of the node type.
     * @return An exception that describes an unsupported node type.
     */
    @Override
    protected IllegalActionException _unsupportedVisitException(String name) {
        return new IllegalActionException("Nodes of type " + name
                + " cannot be visited by a " + getClass().getName() + ".");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variable                ////

    /** The property adapter that contains the top level model
     *  component containing actors that could be referenced
     *  by the node label.
     */
    protected OntologyAdapter _adapter;

    /** The model component that the parse tree node refers to. */
    protected Object _evaluatedObject;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Use the specified name to find an object contained in the container.
     *  @param name The specified name.
     *  @param container The specified NamedObj container.
     *  @return A NamedObj contained in the container with the given name.
     */
    private Object _resolveLabel(String name, Object container) {
        int dotIndex = name.indexOf('.');

        if (dotIndex >= 0) {
            String subContainerName = name.substring(0, dotIndex);
            if (container instanceof CompositeActor) {
                Object object = ((CompositeActor) container)
                        .getEntity(subContainerName);
                if (object != null) {
                    return _resolveLabel(name.substring(dotIndex + 1), object);
                }
            }
        } else {
            if (container instanceof CompositeActor) {
                Object object = ((CompositeActor) container).getEntity(name);
                if (object != null) {
                    return object;
                }
            }

            if (container instanceof Entity) {
                if (name.endsWith(".getPort")) {
                    name = name.replace(".getPort", "");
                }
                Object object = ((Entity) container).getPort(name);
                if (object != null) {
                    return object;
                }
            }

            if (container instanceof NamedObj) {
                if (name.endsWith(".getParameter")) {
                    name = name.replace(".getParameter", "");
                }
                Object object = ((NamedObj) container).getAttribute(name);
                if (object != null) {
                    return object;
                }
            }

            if (container instanceof Entity) {
                Object object = ((Entity) container).getPort(name);
                if (object != null) {
                    return object;
                }

                object = ((NamedObj) container).getAttribute(name);
                if (object != null) {
                    return object;
                }
            }

        }
        return null;
    }
}
