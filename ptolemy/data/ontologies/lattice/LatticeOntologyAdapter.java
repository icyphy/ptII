/**
 * The base class for a lattice-based ontology adapter.
 *
 * Copyright (c) 2007-2013 The Regents of the University of California. All
 * rights reserved. Permission is hereby granted, without written agreement and
 * without license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies of
 * this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * PT_COPYRIGHT_VERSION_2 COPYRIGHTENDKEY
 *
 *
 */
package ptolemy.data.ontologies.lattice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.ontologies.OntologyAdapter;
import ptolemy.data.ontologies.ParseTreeAnnotationEvaluator;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver.ConstraintType;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// LatticeOntologyAdapter

/**
 * The base class for a lattice-based ontology adapter.
 *
 * @author Man-Kit Leung, Thomas Mandl, Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class LatticeOntologyAdapter extends OntologyAdapter {

    /**
     * Construct the lattice ontology adapter associated with the given
     * component and solver. The constructed adapter implicitly uses the default
     * constraints set by the solver.
     * @param solver The specified lattice-based ontology solver.
     * @param component The associated component.
     * @exception IllegalActionException Thrown if the adapter cannot be
     * initialized.
     */
    public LatticeOntologyAdapter(LatticeOntologySolver solver, Object component)
            throws IllegalActionException {
        this(solver, component, true);
    }

    /**
     * Construct the lattice ontology adapter for the given component and
     * property lattice.
     * @param solver The specified lattice-based ontology solver.
     * @param component The given component.
     * @param useDefaultConstraints Indicate whether this adapter uses the
     * default actor constraints.
     * @exception IllegalActionException Thrown if the adapter cannot be
     * initialized.
     */
    public LatticeOntologyAdapter(LatticeOntologySolver solver,
            Object component, boolean useDefaultConstraints)
            throws IllegalActionException {

        setComponent(component);
        _useDefaultConstraints = useDefaultConstraints;
        _solver = solver;
        _ownConstraints = new LinkedList<Inequality>();
        _subAdapterConstraints = new LinkedList<Inequality>();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the constraints of this component. The constraints is a list of
     * inequalities. This base class returns the union of the constraints
     * of this component and the constraints for the subcomponents
     * @return The constraints of this component.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public List<Inequality> constraintList() throws IllegalActionException {

        _constrainAttributes();

        _addSubAdapterConstraints();

        return _union(_ownConstraints, _subAdapterConstraints);
    }

    /**
     * Return the InequalityTerm associated with the given model object.
     *
     * @param object The given model object for which to find the InequalityTerm in
     * the OntologySolver
     * @return The InequalityTerm associated with the model object
     */
    public InequalityTerm getPropertyTerm(Object object) {
        return getSolver().getConceptTerm(object);
    }

    /**
     * Return the associated property solver.
     * @return The associated property solver.
     */
    @Override
    public LatticeOntologySolver getSolver() {
        return (LatticeOntologySolver) _solver;
    }

    /**
     * Returns true if the interconnectConstraintType is for sources, false otherwise.
     * It will return only true if {@linkplain #interconnectConstraintType
     * interconnectConstraintType} is set to
     * {@linkplain ConstraintType#SOURCE_GE_SINK SOURCE_GE_SINK} or
     * {@linkplain ConstraintType#EQUALS EQUALS}.
     *
     * @return true if the interconnectConstraintType is for sources, false otherwise
     */
    public boolean isConstraintSource() {
        boolean constraintSource = interconnectConstraintType == ConstraintType.SOURCE_GE_SINK
                || interconnectConstraintType == ConstraintType.EQUALS;
        return constraintSource;
    }

    /** Reset and initialize the LatticeOntologyAdapter. This clears all
     *  the cached constraints and the states of their inequality terms.
     *  @exception IllegalActionException Thrown if
     *   {@link #getPropertyables()} throws it.
     */
    @Override
    public void reinitialize() throws IllegalActionException {
        _ownConstraints = new LinkedList<Inequality>();
        _subAdapterConstraints = new LinkedList<Inequality>();
        super.reinitialize();
    }

    /** Set an inequality constraint between the two specified objects, such that
     *  the concept value of the greater term is greater than or equal to the
     *  concept value of the lesser term.
     *  @param greater The model object on the LHS of the &ge; inequality
     *  @param lesser The model object on the RHS of the &ge; inequality
     */
    public void setAtLeast(Object greater, Object lesser) {
        if (greater != null && lesser != null) {
            _ownConstraints.add(new Inequality(getPropertyTerm(lesser),
                    getPropertyTerm(greater)));
        }
    }

    /** Set an inequality constraint between the two specified objects, such that
     *  the concept value of the greater term is greater than or equal to the
     *  concept value of the lesser term.
     *  @param lesser The model object on the RHS of the &ge; inequality
     *  @param greater The model object on the LHS of the &ge; inequality
     */
    public void setAtMost(Object lesser, Object greater) {
        setAtLeast(greater, lesser);
    }

    /** Set two inequality constraints between the specified objects,
     *  such that the Concept value of object1 is equal to the Concept value
     *  of object2.
     *  @param object1 The model object on the LHS of the equality
     *  @param object2 The model object on the RHS of the equality
     */
    public void setSameAs(Object object1, Object object2) {
        setAtLeast(object1, object2);
        setAtLeast(object2, object1);
    }

    ///////////////////////////////////////////////////////////////////
    ////                           public variable                 ////

    /**
     * The default constraint type for connections between actors.
     */
    public ConstraintType interconnectConstraintType;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Add default constraints for the actor referred to by this OntologyAdapter
     * based on the given ConstraintType.
     *
     * @see ConstraintType
     * @param actorConstraintType The given ConstraintType for the default constraints
     * for the actor referred to by this OntologyAdapter
     * @exception IllegalActionException If an exception is thrown
     */
    protected void _addDefaultConstraints(ConstraintType actorConstraintType)
            throws IllegalActionException {
        if (!_useDefaultConstraints
                || !AtomicActor.class.isInstance(getComponent())) {
            return;
        }

        if (interconnectConstraintType == null) {
            interconnectConstraintType = actorConstraintType;
        }

        List<IOPort> inputPorts = ((AtomicActor) getComponent())
                .inputPortList();
        List<IOPort> outputPorts = ((AtomicActor) getComponent())
                .outputPortList();

        _constrainObjectLists(actorConstraintType, inputPorts, outputPorts);

        for (IOPort inputSink : inputPorts) {
            List<IOPort> previousOutputs = _getSourcePortList(inputSink);
            for (IOPort source : previousOutputs) {
                _constrainSingleObject(interconnectConstraintType, source,
                        inputSink);
            }
        }
    }

    /**
     * Iterate through the list of sub adapters and gather the constraints for
     * each one. Note that the adapter stores a new set of constraints each time
     * this is invoked. Therefore, multiple invocations will generate excessive
     * constraints and result in inefficiency during resolution.
     * @exception IllegalActionException Thrown if there is any errors in
     * getting the sub adapters and gathering the constraints for each one.
     */
    protected void _addSubAdapterConstraints() throws IllegalActionException {
        Iterator adapters = _getSubAdapters().iterator();

        while (adapters.hasNext()) {
            LatticeOntologyAdapter adapter = (LatticeOntologyAdapter) adapters
                    .next();
            _subAdapterConstraints.addAll(adapter.constraintList());
        }
    }

    /**
     * Create a new ParseTreeAnnotationEvaluator that is tailored for the
     * ontology. This class parses the user-defined ontology constraint
     * annotations in the model containing the LatticeOntologySolver.
     *
     * @return a new ParseTreeConstraintAnnotationEvaluator object
     */
    @Override
    protected ParseTreeAnnotationEvaluator _annotationEvaluator() {
        return new ParseTreeConstraintAnnotationEvaluator();
    }

    /**
     * Set default Inequality constraints for all attributes that can
     * be evaluated to a Concept in the Ontology. This method parses each
     * attribute and sets a default constraint between the root AST node and
     * its attribute, if the attribute was parseable.
     */
    protected void _constrainAttributes() {

        for (Attribute attribute : _getPropertyableAttributes()) {

            try {
                ASTPtRootNode node = getParseTree(attribute);

                // Take care of actors without nodes, e.g. MonitorValue actors without previous execution
                if (node != null) {
                    setSameAs(attribute, node);
                }

            } catch (IllegalActionException ex) {
                // This means the expression is not parse-able.
                assert false;
            }
        }
    }

    /**
     * Set default constraints between the given object and a list of objects based
     * on the given constraintType.  The given object is the sink and the list of objects
     * are the sources.
     *
     * @see ConstraintType
     * @param constraintType The given ConstraintType to be used for the default constraints
     * @param sink The given object that represents the sink for the default constraints
     * @param sourceList The list of objects passed in as a {@linkplain List} that
     * represents the sources for the default constraints
     * @exception IllegalActionException If an exception is thrown
     */
    protected void _constrainObject(ConstraintType constraintType, Object sink,
            List sourceList) throws IllegalActionException {

        // Not sure why this next line is needed, but there are test cases
        // that depend on this behavior.             FIXME: Investigate why
        if (constraintType == null) {
            constraintType = ConstraintType.SOURCE_GE_SINK;
        }

        for (Object source : sourceList) {
            switch (constraintType) {
            case NONE:
                break;
            case EQUALS:
                setSameAs(source, sink);
                break;
            case SINK_GE_SOURCE: //setAtLeast(source, sink); break;
            case SOURCE_GE_SINK:
                setAtLeast(sink, source);
                break;
            }
        }
    }

    /** Set the default constraint between the given list of source objects
     *  and list of sink objects based on the given constraintType.
     *
     * @see ConstraintType
     * @param constraintType The given ConstraintType to be used for the default constraint
     * @param sourceList A list of source objects
     * @param sinkList A list of sink objects
     * @exception IllegalActionException If an exception is thrown
     */
    protected void _constrainObjectLists(ConstraintType constraintType,
            List sourceList, List sinkList) throws IllegalActionException {
        for (Object source : sourceList) {
            for (Object sink : sinkList) {
                _constrainSingleObject(constraintType, source, sink);
            }
        }

    }

    /** Set the default constraint between the given source and sink object
     *  based on the given constraintType.
     *
     * @see ConstraintType
     * @param constraintType The given ConstraintType to be used for the default constraint
     * @param source The source object for the default constraints
     * @param sink The sink object for the default constraints
     * @exception IllegalActionException If an exception is thrown
     */
    protected void _constrainSingleObject(ConstraintType constraintType,
            Object source, Object sink) throws IllegalActionException {
        switch (constraintType) {
        case NONE:
            break;
        case EQUALS:
            setSameAs(source, sink);
            break;
        case SINK_GE_SOURCE:
            setAtLeast(sink, source);
            break;
        case SOURCE_GE_SINK:
            setAtLeast(source, sink);
            break;
        }
    }

    /**
     * Return the list of constrained ports given the flag whether source or
     * sink ports should be constrained. If source ports are constrained, it
     * returns the list of input ports of the associated actor; otherwise, it
     * returns the list of output ports.
     *
     * @param constraintSource The flag that indicates whether source or sink
     * ports are constrained.
     * @return The list of constrained ports.
     */
    protected List _getConstrainedPorts(boolean constraintSource) {
        Actor actor = (Actor) getComponent();
        return constraintSource ? actor.outputPortList() : actor
                .inputPortList();
    }

    /**
     * Return the list of constraining ports on a given port, given whether
     * source or sink ports should be constrained.
     * @param constraintSource The flag that indicates whether source or sink
     * ports are constrained.
     * @param port The given port.
     * @return The list of constraining ports.
     */
    protected static List _getConstraintingPorts(boolean constraintSource,
            TypedIOPort port) {

        return constraintSource ? _getSinkPortList(port)
                : _getSourcePortList(port);
    }

    /**
     * Return the list of sub-adapters. By default, this returns the list of
     * ASTNode adapters that are associated with the expressions of the
     * propertyable attributes.
     * @return The list of sub-adapters.
     * @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    protected List<OntologyAdapter> _getSubAdapters()
            throws IllegalActionException {
        LatticeOntologySolver solver = getSolver();
        if (solver.solverStrategy.getExpression().equals("none")) {
            return new LinkedList();
        }
        return _getASTNodeAdapters();
    }

    /**
     * Set the default constraint type for connections for the model component referred
     * to by this OntologyAdapter for the given OntologySolver, depending on what type of
     * component it is.  This method is recursive and
     * will set the default constraints for the OntologyAdapters for all subcomponents
     * of this model component as well.
     *
     * @see ConstraintType
     * @param constraintType The default ConstraintType for generic model component
     * connections; will be used if the model component is not one of the following
     * types
     * for Ptolemy expression language AST nodes; will be used it the model component
     * is an AST node
     * @exception IllegalActionException If an exception is thrown
     */
    protected void _setConnectionConstraintType(ConstraintType constraintType)
            throws IllegalActionException {

        Iterator adapters = _getSubAdapters().iterator();

        while (adapters.hasNext()) {
            LatticeOntologyAdapter adapter = (LatticeOntologyAdapter) adapters
                    .next();

            adapter._setConnectionConstraintType(constraintType);
        }

        interconnectConstraintType = constraintType;
    }

    /**
     * Return the union of the two specified lists of inequality constraints by
     * appending the second list to the end of the first list.
     * @param list1 The first list.
     * @param list2 The second list.
     * @return The union of the two lists.
     */
    protected static List<Inequality> _union(List<Inequality> list1,
            List<Inequality> list2) {

        List<Inequality> result = new ArrayList<Inequality>(list1);

        result.addAll(list2);
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The list of Inequality constraints contained by this LatticeOntologyAdapter. */
    protected List<Inequality> _ownConstraints = null;

    /** Indicate whether this adapter uses the default actor constraints. */
    protected boolean _useDefaultConstraints;

    /** The list of permanent property constraints. */
    protected List<Inequality> _subAdapterConstraints = null;

}
