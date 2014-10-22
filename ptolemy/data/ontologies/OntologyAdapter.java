/*  The base abstract class for an ontology adapter.

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

package ptolemy.data.ontologies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ptolemy.actor.IOPort;
import ptolemy.actor.lib.Expression;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// OntologyAdapter

/**
Constraints for a component in the model.

<p>The model component can be an object of any Ptolemy class
(e.g. ASTPtRootNode, Sink, Entity, and FSMActor). A model component,
in turn, may have one or multiple property-able objects. Each
constraint is relevant to a property-able object. For example,
the PropertyAdapter associated with an actor may have each of its
IOPorts as property-able.

<p>A property-able object is an object that can be annotated with
a Property object. Users can define different Property classes as
part of their use-case definition.

<p>Every PropertyAdapter is associated a property
solver. PropertyAdapters support hierarchical structuring. They may
have downward links to sub-adapters. This is helpful to construct
PropertyAdapter for hierarchical component in the model. For example, a
PropertyAdapter for the CompositeActor have all the contained actors'
adapters as its sub-adapters.

<p>A PropertyAdapter supports manual annotation. Users can define their
own annotation evaluator to evaluate property expressions and/or
constraints.

@author Man-Kit Leung
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (mankit)
@Pt.AcceptedRating Red (mankit)
 */
public abstract class OntologyAdapter {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the associated component object.
     *
     * @return The associated component.
     * @see #setComponent
     */
    public Object getComponent() {
        return _component;
    }

    /**
     * Return the container entity for the specified ASTPtRootNode.
     *
     * @param node The specified ASTPtRootNode.
     *
     * @return The container entity for the specified ASTPtRootNode.
     */
    public Entity getContainerEntity(ASTPtRootNode node) {
        Attribute attribute = _solver.getAttribute(node);
        NamedObj container = attribute.getContainer();

        while (!(container instanceof Entity)) {
            container = container.getContainer();
        }
        return (Entity) container;
    }

    /**
     * Return the name of the PropertyAdapter. In this base class,
     * return the concatenation of the prefix "Adapter_" and the string
     * representation of the component object. This method does not
     * guarantee uniqueness.
     *
     * @return The name of the PropertyAdapter.
     */
    public String getName() {
        return "Adapter_" + _component.toString();
    }

    /**
     * Return a list of property-able NamedObj contained by the component. All
     * ports and parameters are considered property-able.
     * @return The list of property-able named object.
     */
    public List<Object> getPropertyables() {
        List<Object> list = new ArrayList<Object>();

        // Add all ports.
        list.addAll(((Entity) getComponent()).portList());

        // Add attributes.
        list.addAll(_getPropertyableAttributes());

        return list;
    }

    /**
     * Return The PropertySolver that uses this adapter.
     *
     * @return The PropertySolver that uses this adapter.
     */
    public OntologySolver getSolver() {
        return _solver;
    }

    /**
     * Reset and initialize the PropertyAdapter. This clears any
     * cached states and the resolved properties of the property-able
     * objects. This call is recursive, so every sub-adapter will be
     * reset and initialized after the call.
     *
     * @exception IllegalActionException Thrown if
     * {@link #getPropertyables()} throws it.
     */
    public void reinitialize() throws IllegalActionException {

        for (OntologyAnnotationAttribute annotation : _getAnnotationAttributes()) {
            _evaluateAnnotation(annotation);
        }

        for (Object propertyable : getPropertyables()) {

            // Remove all ConceptAttributes.
            if (propertyable instanceof NamedObj) {
                if (_solver.isSettable(propertyable)) {
                    _solver.clearResolvedConcept(propertyable);
                }
            }
        }

        // The recursive case.
        for (OntologyAdapter adapter : _getSubAdapters()) {
            adapter.reinitialize();
        }
    }

    /**
     * Associate this PropertyAdapter with the specified component.
     *
     * @param component The specified component.
     * @see #getComponent
     */
    public void setComponent(Object component) {
        _component = component;
    }

    /**
     * Set the property of specified object equal to the specified property.
     *
     * @param object The specified object.
     *
     * @param property The specified property.
     */
    public void setEquals(Object object, Concept property) {
        _solver.setConcept(object, property);
        _solver.markAsNonSettable(object);
    }

    /**
     * Return the string representation of the PropertyAdapter.
     *
     * @return The string representation of the PropertyAdapter.
     */
    @Override
    public String toString() {
        return getName() + " " + super.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Create a new ParseTreeAnnotationEvaluator that is tailored for the
     * ontology. This class parses the user-defined ontology annotations
     * in the model containing the OntologySolver.
     *
     * @return A new ParseTreeAnnotationEvaluator.
     */
    protected abstract ParseTreeAnnotationEvaluator _annotationEvaluator();

    /**
     * Return the ParseTreeAnnotationEvaluator. This will creates a new
     * ParseTreeAnnotationEvaluator if it does not already exists.
     *
     * @return The ParseTreeAnnotationEvaluator.
     */
    private ParseTreeAnnotationEvaluator _getAnnotationEvaluator() {
        if (_annotationEvaluator == null) {
            _annotationEvaluator = _annotationEvaluator();
        }
        return _annotationEvaluator;
    }

    /**
     * Return the list of PropertyAdapters for ASTPtRootNodes. These
     * ASTPtRootNodes are nodes of the parse tree constructed from
     * parsing the expression of every property-able Attribute.
     *
     * @return The list of PropertyAdapters for ASTPtRootNodes.
     * @exception IllegalActionException If the AST expression is not parseable
     */
    protected List<OntologyAdapter> _getASTNodeAdapters()
            throws IllegalActionException {
        List<OntologyAdapter> astAdapters = new ArrayList<OntologyAdapter>();
        ParseTreeASTNodeAdapterCollector collector = new ParseTreeASTNodeAdapterCollector();

        for (ASTPtRootNode root : _getAttributeParseTrees()) {
            if (root != null) {
                //                try {
                List<OntologyAdapter> adapters = collector.collectAdapters(
                        root, getSolver());
                astAdapters.addAll(adapters);
                //                } catch (IllegalActionException ex) {
                //                    // This means the expression is not parse-able.
                //                    // FIXME: So, we will discard it for now.
                //                    throw new AssertionError(ex.stackTraceToString(ex));
                //                }
            }
        }
        return astAdapters;
    }

    /** Return the list of parse trees for all settable Attributes
     *  of the component.
     *  @return The list of ASTPtRootNodes.
     *  @exception IllegalActionException Thrown if there is a problem getting
     *   the parse trees for the attributes.
     */
    protected List<ASTPtRootNode> _getAttributeParseTrees()
            throws IllegalActionException {
        List<ASTPtRootNode> result = new ArrayList<ASTPtRootNode>();

        Iterator attributes = null;

        attributes = _getPropertyableAttributes().iterator();

        while (attributes.hasNext()) {
            Attribute attribute = (Attribute) attributes.next();

            //            try {
            ASTPtRootNode pt = getParseTree(attribute);
            if (pt != null) {
                result.add(pt);
            }
            //            } catch (IllegalActionException ex) {
            //                // This means the expression is not parse-able.
            //                // FIXME: So, we will discard it for now.
            //                // FIXME: Breaks the regression test. Need to figure out a better
            //                // way to deal with the problem.
            //                System.out.println(KernelException.stackTraceToString(ex));
            //                //throw new AssertionError(ex.stackTraceToString(ex));
            //            }
        }
        return result;
    }

    /**
     * Return the list of property-able Attributes.
     * A property-able Attribute is a StringAttribute with the name
     * "guardTransition", a StringAttribute in an Expression actor,
     * a StringAttribute with the name "expression" or a Variable
     * with full visibility.  However, Variables with certain names
     * are excluded.
     * @return The list of property-able Attributes.
     */
    protected List<Attribute> _getPropertyableAttributes() {
        List<Attribute> result = new LinkedList<Attribute>();
        Iterator attributes = ((Entity) getComponent()).attributeList()
                .iterator();

        while (attributes.hasNext()) {
            Attribute attribute = (Attribute) attributes.next();

            // only consider StringAttributes, ignore all subclasses
            if (attribute.getClass().equals(
                    ptolemy.kernel.util.StringAttribute.class)) {
                if (((StringAttribute) attribute).getName().equalsIgnoreCase(
                        "guardTransition")
                        || ((StringAttribute) attribute).getContainer() instanceof Expression
                        && ((StringAttribute) attribute).getName()
                                .equalsIgnoreCase("expression")) {

                    result.add(attribute);
                }
            } else if (attribute instanceof Variable) {
                if (((Variable) attribute).getVisibility() == Settable.FULL) {

                    // filter Parameters with certain names; ignore all
                    // subclasses
                    if (attribute instanceof PortParameter) {
                        result.add(attribute);
                    } else if (attribute.getClass().equals(
                            ptolemy.data.expr.Parameter.class)
                            || attribute.getClass().equals(
                                    ptolemy.data.expr.StringParameter.class)) {

                        // FIXME: implement filter interface, so that adapter
                        // classes can specify which attributes
                        // need to be filtered (either by name or by class)
                        // Currently all filtered attributes need to be
                        // specified here
                        if (((Parameter) attribute).getName().equals(
                                "firingCountLimit")
                                || ((Parameter) attribute).getName().equals(
                                        "NONE")
                                || ((Parameter) attribute).getName().equals(
                                        "_hideName")
                                || ((Parameter) attribute).getName().equals(
                                        "_showName")
                                || ((Parameter) attribute).getName().equals(
                                        "conservativeAnalysis")
                                || ((Parameter) attribute).getName().equals(
                                        "directorClass")
                                || ((Parameter) attribute).getName().equals(
                                        "stateDependentCausality")
                                || ((Parameter) attribute).getName().equals(
                                        "delayed")
                                || ((Parameter) attribute).getName().equals(
                                        "displayWidth")) {

                            // do nothing, ignore the parameter
                        } else {
                            result.add(attribute);
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Return the list of receiving (down-stream) ports that are
     * connected to the specified port. This treats every port as an
     * opaque port.
     *
     * @param port The specified port.
     *
     * @return The list of receiving ports.
     */
    protected static List<IOPort> _getSinkPortList(IOPort port) {
        List<IOPort> result = new ArrayList<IOPort>();

        for (IOPort connectedPort : (List<IOPort>) port.connectedPortList()) {
            boolean isInput = connectedPort.isInput();
            boolean isCompositeOutput = connectedPort.getContainer() instanceof CompositeEntity
                    && !isInput
                    && port.depthInHierarchy() > connectedPort
                            .depthInHierarchy();

            if (isInput || isCompositeOutput) {
                result.add(connectedPort);
            }
        }
        return result;
    }

    /**
     * Return the list of sending (up-stream) ports that are connected
     * to the specified port. This treats every port as an opaque
     * port.
     *
     * @param port The specified port.
     *
     * @return The list of sending ports.
     */
    protected static List<IOPort> _getSourcePortList(IOPort port) {
        List<IOPort> result = new ArrayList<IOPort>();

        for (IOPort connectedPort : (List<IOPort>) port.connectedPortList()) {
            boolean isInput = connectedPort.isInput();
            boolean isCompositeInput = connectedPort.getContainer() instanceof CompositeEntity
                    && isInput
                    && port.depthInHierarchy() > connectedPort
                            .depthInHierarchy();

            if (!isInput || isCompositeInput) {
                result.add(connectedPort);
            }
        }
        return result;
    }

    /**
     * Return the list of sub-adapters. By default, this returns the list of
     * ASTNode adapters that are associated with the expressions of the
     * propertyable attributes.
     * @return The list of sub-adapters.
     * @exception IllegalActionException Not thrown in this base class.
     */
    protected List<OntologyAdapter> _getSubAdapters()
            throws IllegalActionException {
        getSolver();
        /* FIXME: Haven't included these options yet on default constraints.
        if (solver.expressionASTNodeConstraintType.getExpression().equals(
                "NONE")) {
            return new LinkedList();
        }
         */
        return _getASTNodeAdapters();
    }

    /** Return the ASTPtRootNode for the specified Attribute.
     *
     *  @param attribute The specified attribute.
     *  @return The ASTPtRootNode for the specified Attribute.
     *  @exception IllegalActionException Thrown if
     *   {@link ptolemy.data.ontologies.OntologySolver#getParseTree(Attribute)} throws it.
     */
    protected ASTPtRootNode getParseTree(Attribute attribute)
            throws IllegalActionException {
        return _solver.getParseTree(attribute);
    }

    /** Record the association between the specified ASTPtRootNode and the
     *  specified Attribute.
     *
     *  @param node The specified ASTPtRootNode.
     *  @param attribute The specified Attribute.
     */
    protected void putAttribute(ASTPtRootNode node, Attribute attribute) {
        _solver.getOntologySolverUtilities().putAttribute(node, attribute);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The annotation evaluator. */
    protected ParseTreeAnnotationEvaluator _annotationEvaluator;

    /** The associated ontology solver. */
    protected OntologySolver _solver;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Evaluate the expression of the specified AnnotationAttribute.
     *
     * @param annotation The specified AnnotationAttribute.
     * @exception IllegalActionException Thrown there is an error
     * parsing or evaluating the annotation.
     */
    private void _evaluateAnnotation(OntologyAnnotationAttribute annotation)
            throws IllegalActionException {
        Map map;
        try {
            ASTPtRootNode parseTree = getSolver().getParser()
                    .generateParseTree(annotation.getExpression());

            map = new HashMap();
            map.put(parseTree, parseTree);
        } catch (IllegalActionException ex) {
            map = getSolver().getParser().generateAssignmentMap(
                    annotation.getExpression());

        }

        // Evaluate each assignment constraints.
        for (Object assignment : map.values()) {
            ASTPtRootNode parseTree = (ASTPtRootNode) assignment;
            _getAnnotationEvaluator().evaluate(parseTree, this);
        }
    }

    /**
     * Return the list of AnnotationAttributes specific to this use-case.
     *
     * @return The list of AnnotationAttributes.
     *
     * @exception IllegalActionException Thrown if there is a problem obtaining
     * the use-case identifier for an annotation attribute.
     */
    private List<OntologyAnnotationAttribute> _getAnnotationAttributes()
            throws IllegalActionException {
        List<OntologyAnnotationAttribute> result = new LinkedList<OntologyAnnotationAttribute>();
        if (_component instanceof NamedObj) {

            for (Object attribute : ((NamedObj) _component).attributeList()) {

                if (attribute instanceof OntologyAnnotationAttribute) {
                    String ontologySolverName = ((OntologyAnnotationAttribute) attribute)
                            .getOntologySolverIdentifier();

                    // Check to make sure the use case name of the
                    // AnnotationAttribute matches the name of the ontology
                    // solver.
                    if (_solver.getName().equals(ontologySolverName)) {
                        result.add((OntologyAnnotationAttribute) attribute);
                    }
                }
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The associated component. */
    private Object _component;
}
