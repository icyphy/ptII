/** A base class representing a property constraint adapter.

 Copyright (c) 2007-2014 The Regents of the University of California.
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
package ptolemy.data.ontologies.lattice;

import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.ASTPtLeafNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Variable;
import ptolemy.data.ontologies.OntologyAdapter;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver.ConstraintType;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// LatticeOntoloyASTNodeAdapter

/**
 A base class representing a property constraint adapter.

 @author Man-Kit Leung, Thomas Mandl, Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class LatticeOntologyASTNodeAdapter extends LatticeOntologyAdapter {

    /**
     * Construct the property constraint adapter associated
     * with the given AST node.
     * @param solver  The lattice-based ontology solver for this adapter
     * @param node The given AST node
     * @exception IllegalActionException Thrown if
     *  LatticeOntologyASTNodeAdapter(NamedObj, ASTPtRootNode, boolean)
     *  throws it.
     */
    public LatticeOntologyASTNodeAdapter(LatticeOntologySolver solver,
            ASTPtRootNode node) throws IllegalActionException {
        this(solver, node, true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Construct the property constraint adapter for the given
     * property solver and AST node.
     * @param solver The lattice-based ontology solver for this adapter
     * @param node The given AST node
     * @param useDefaultConstraints Indicate whether this adapter
     *  uses the default actor constraints
     * @exception IllegalActionException If the adapter cannot
     *  be initialized.
     */
    public LatticeOntologyASTNodeAdapter(LatticeOntologySolver solver,
            ASTPtRootNode node, boolean useDefaultConstraints)
            throws IllegalActionException {

        super(solver, node, useDefaultConstraints);
    }

    /** Return the constraints of this component.  The constraints is
     *  a list of inequalities.
     *  @return A list of Inequalities.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public List<Inequality> constraintList() throws IllegalActionException {
        boolean constrainParent = interconnectConstraintType == ConstraintType.SINK_GE_SOURCE;
        boolean isEqualConstraint = interconnectConstraintType == ConstraintType.EQUALS;

        if (getComponent() instanceof ASTPtLeafNode) {
            ASTPtLeafNode node = (ASTPtLeafNode) getComponent();

            if (node.isConstant() && node.isEvaluated()) {
                //FIXME: Should be handled by use-case specific adapters.
                // We should make a (abstract) method that forces use-case
                // to implement this.
            } else {

                NamedObj namedObj = getNamedObject(getContainerEntity(node),
                        node.getName());

                if (namedObj != null) {
                    // Set up bidirectional constraint when the constraint type
                    // is equal.
                    if (isEqualConstraint) {
                        setSameAs(node, namedObj);

                        // Set up one-direction constraint.
                    } else if (constrainParent) {
                        setAtLeast(node, namedObj);
                    } else {
                        setAtLeast(namedObj, node);
                    }

                } else {
                    // Cannot resolve the property of a label.
                    assert false;
                }
            }
        }

        if (_useDefaultConstraints) {
            ASTPtRootNode node = (ASTPtRootNode) getComponent();
            List<Object> children = new ArrayList<Object>();

            boolean isNone = interconnectConstraintType == ConstraintType.NONE;

            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                if (!constrainParent && !isNone) {
                    // Set child >= parent.
                    setAtLeast(node.jjtGetChild(i), node);
                }
                children.add(node.jjtGetChild(i));
            }

            if (constrainParent || isEqualConstraint) {
                _constrainObject(interconnectConstraintType, node, children);
            }
        }

        return _union(_ownConstraints, _subAdapterConstraints);
    }

    /**
     * Returns the component referenced by the given name in the given
     * container.
     *
     * @param container The container in which to find the component
     * @param name The name of the component
     * @return The NamedObj component referred to by the name found in the
     * container, or null if it is not found
     */
    public static NamedObj getNamedObject(Entity container, String name) {
        // Check the port names.
        TypedIOPort port = (TypedIOPort) container.getPort(name);

        if (port != null) {
            return port;
        }

        Variable result = ModelScope.getScopedVariable(null, container, name);

        if (result != null) {
            return result;
        }
        return null;
    }

    /**
     * Return a list of property-able NamedObj contained by
     * the component. All ports and parameters are considered
     * property-able.
     * @return The list of property-able named object.
     */
    @Override
    public List<Object> getPropertyables() {
        List<Object> list = new ArrayList<Object>();
        list.add(getComponent());
        return list;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return an array of all the inequality terms for the
     *  child nodes to this product node.
     * @return The array of inequality terms for the child nodes.
     */
    protected InequalityTerm[] _getChildNodeTerms() {
        List<InequalityTerm> terms = new ArrayList<InequalityTerm>();
        try {
            for (int i = 0; i < _getNode().jjtGetNumChildren(); i++) {
                Object child = _getNode().jjtGetChild(i);

                LatticeOntologyASTNodeAdapter adapter = (LatticeOntologyASTNodeAdapter) getSolver()
                        .getAdapter(child);

                InequalityTerm term = adapter.getPropertyTerm(child);
                terms.add(term);
            }
        } catch (IllegalActionException e) {
            throw new AssertionError(
                    "Unable to get the children property term(s).");
        }
        return terms.toArray(new InequalityTerm[terms.size()]);
    }

    /**
     * Return the node this adapter references.
     *
     * @return The node referred to by this adapter
     */
    protected ASTPtRootNode _getNode() {
        return (ASTPtRootNode) getComponent();
    }

    /**
     * Return the list of sub-adapters. In this base class,
     * return an empty list.
     * @return The list of sub-adapters.
     */
    @Override
    protected List<OntologyAdapter> _getSubAdapters() {
        return new ArrayList<OntologyAdapter>();
    }
}
