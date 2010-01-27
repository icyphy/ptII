/* A visitor for parse trees of the expression language that infers properties.

 Copyright (c) 1998-2010 The Regents of the University of California.
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
package ptolemy.data.properties;

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
 @since Ptolemy II 8.0
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
    public void evaluate(ASTPtRootNode node, PropertyHelper adapter)
            throws IllegalActionException {
        _adapter = adapter;
        node.visit(this);
    }

    /**
     *
     */
    public void visitAssignmentNode(ASTPtAssignmentNode node)
            throws IllegalActionException {
        ((ASTPtRootNode) node.jjtGetChild(0)).visit(this);
        Object object = _evaluatedObject;

        node.getExpressionTree().visit(this);

        if (_evaluatedObject instanceof Property) {
            _adapter.setEquals(object, (Property) _evaluatedObject);
        } else {
            throw _unsupportedVisitException("Unknown assignment object: "
                    + _evaluatedObject);
        }
    }

    /**
     *
     */
    public void visitLeafNode(ASTPtLeafNode node) throws IllegalActionException {
        _evaluatedObject = _resolveLabel(_getNodeLabel(node), _adapter
                .getComponent());

        if (_evaluatedObject == null) {
            throw _unsupportedVisitException("Cannot resolve label: "
                    + node.getName());
        }

        // FIXME: Not handling AST constraint yet.
    }

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

    /** Return an exception that describes an unsupported node type.
     *  @param name The name of the node type.
     *  @return An exception that describes an unsupported node type.
     */
    protected IllegalActionException _unsupportedVisitException(String name) {
        return new IllegalActionException("Nodes of type " + name
                + " cannot be visited by a " + getClass().getName() + ".");
    }

    protected String _getNodeLabel(ASTPtLeafNode node) {
        if (node.isConstant()) {
            return node.getToken().toString();
        } else {
            return node.getName();
        }
    }

    protected PropertyHelper _adapter;

    protected Object _evaluatedObject;

}
