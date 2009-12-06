/**
 * The base class for a property constraint adapter.
 * 
 * Copyright (c) 2007-2009 The Regents of the University of California. All
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
package ptolemy.data.ontologies;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.ontologies.OntologySolver.ConstraintType;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.modal.ModalModel;
import ptolemy.graph.CPO;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// PropertyConstraintHelper

/**
 * The base class for a property constraint adapter.
 * 
 * @author Man-Kit Leung, Thomas Mandl, Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class PropertyConstraintHelper extends PropertyHelper {

    /**
     * Construct the property constraint adapter associated with the given
     * component and solver. The constructed adapter implicitly uses the default
     * constraints set by the solver.
     * @param solver The specified property solver.
     * @param component The associated component.
     * @exception IllegalActionException Thrown if the adapter cannot be
     * initialized.
     */
    public PropertyConstraintHelper(OntologySolver solver,
            Object component) throws IllegalActionException {
        this(solver, component, true);
    }

    /**
     * Construct the property constraint adapter for the given component and
     * property lattice.
     * @param solver The given property lattice.
     * @param component The given component.
     * @param useDefaultConstraints Indicate whether this adapter uses the
     * default actor constraints.
     * @exception IllegalActionException Thrown if the adapter cannot be
     * initialized.
     */
    public PropertyConstraintHelper(OntologySolver solver,
            Object component, boolean useDefaultConstraints)
            throws IllegalActionException {

        setComponent(component);
        _useDefaultConstraints = useDefaultConstraints;
        _solver = solver;
        _lattice = solver.getLattice();
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
        _setEffectiveTerms();

        _constraintAttributes();

        _addSubHelperConstraints();

        return _union(_ownConstraints, _subHelperConstraints);
    }

    /**
     * Return the list of constraining terms for a given object. It delegates to
     * the constraint manager of the solver linked with this adapter.
     * @param object The given object.
     * @return The list of constrainting terms.
     */
    public List<InequalityTerm> getConstraintingTerms(Object object) {
        return getSolver().getConstraintManager().getConstraintingTerms(object);
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
     * Return the associated InequalityTerm.
     * @param object
     * @return The InequalityTerm.
     */
    public InequalityTerm getPropertyTerm(Object object) {
        return getSolver().getPropertyTerm(object);
    }

    /**
     * Return the associated property solver.
     * @return The associated property solver.
     */
    public OntologySolver getSolver() {
        return (OntologySolver) _solver;
    }

    public boolean isAnnotated(Object object) {
        return ((OntologySolver) _solver).isAnnotatedTerm(object);
    }

    public boolean isConstraintSource() {
        boolean constraintSource = interconnectConstraintType == ConstraintType.SRC_EQUALS_MEET
                || interconnectConstraintType == ConstraintType.SRC_EQUALS_GREATER;
        return constraintSource;
    }

    /**
     * Set an inequality constraint between the two specified objects, such that
     * the property value of object1 is at least as great as the property value
     * of object2.
     * @param object1 The first object.
     * @param object2 The second object.
     */
    public void setAtLeast(Object object1, Object object2) {
        _setAtLeast(getPropertyTerm(object1), getPropertyTerm(object2), true);
    }

    public void setAtLeast(Object object1, Object object2, boolean isBase) {
        _setAtLeast(getPropertyTerm(object1), getPropertyTerm(object2), isBase);
    }

    public void setAtLeastByDefault(Object term1, Object term2) {
        setAtLeast(term1, term2);

        if (term1 != null && term2 != null) {
            _solver.incrementStats("# of default constraints", 1);
            _solver.incrementStats("# of atomic actor default constraints", 1);
        }
    }

    public void setAtLeastManualAnnotation(Object term1, Object term2) {
        setAtLeast(term1, term2);

        if (term1 != null && term2 != null) {
            getSolver().addAnnotated(term1);
            getSolver().addAnnotated(term2);
            _solver.incrementStats("# of manual annotations", 1);
        }
    }

    /**
     * Set an inequality constraint between the two specified objects, such that
     * the property value of object1 is at most the property value of object2.
     * @param object1 The first object.
     * @param object2 The second object.
     */
    public void setAtMost(Object object1, Object object2) {
        _setAtLeast(getPropertyTerm(object2), getPropertyTerm(object1), true);
    }

    public void setAtMost(Object object1, Object object2, boolean isBase) {
        _setAtLeast(getPropertyTerm(object2), getPropertyTerm(object1), isBase);
    }

    /**
     * Create constraints that set the property of the given objects same as
     * each other. It creates two inequalities that constraint the first object
     * to be at least the second, and then constraint the second to be at least
     * the first.
     * @param object1 The given port.
     * @param object2 The given function term.
     */
    public void setSameAs(Object object1, Object object2) {
        setAtLeast(object1, object2);
        setAtLeast(object2, object1);
    }

    public void setSameAsByDefault(Object term1, Object term2) {
        setSameAs(term1, term2);

        if (term1 != null && term2 != null) {
            _solver.incrementStats("# of default constraints", 2);
            _solver.incrementStats("# of atomic actor default constraints", 2);
        }
    }

    public void setSameAsManualAnnotation(Object term1, Object term2) {
        setSameAs(term1, term2);

        if (term1 != null && term2 != null) {
            getSolver().addAnnotated(term1);
            getSolver().addAnnotated(term2);
            _solver.incrementStats("# of manual annotations", 2);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                           public variable                 ////

    /**
     * 
     */
    public ConstraintType interconnectConstraintType;

    ///////////////////////////////////////////////////////////////////
    ////                        public inner class                 ////

    public class Inequality extends ptolemy.graph.Inequality {

        public Inequality(InequalityTerm lesserTerm, InequalityTerm greaterTerm,
                boolean isBase) {
            super(lesserTerm, greaterTerm);

            _isBase = isBase;
            _adapter = PropertyConstraintHelper.this;
        }

        public PropertyHelper getHelper() {
            return _adapter;
        }

        /**
         * Return true if this inequality is composable; otherwise, false.
         * @return Whether this inequality is composable.
         */
        public boolean isBase() {
            return _isBase;
        }

        /**
         * Test if this inequality is satisfied with the current value of
         * variables.
         * @param cpo A CPO over which this inequality is defined.
         * @return True if this inequality is satisfied; false otherwise.
         * @exception IllegalActionException If thrown while getting the value
         * of the terms.
         */
        public boolean isSatisfied(CPO cpo) throws IllegalActionException {
            InequalityTerm lesserTerm = (InequalityTerm) getLesserTerm();
            InequalityTerm greaterTerm = (InequalityTerm) getGreaterTerm();

            if (lesserTerm.getValue() == null) {
                return true;
            } else if (greaterTerm.getValue() == null) {
                return false;
            }

            return super.isSatisfied(cpo);
        }

        private final boolean _isBase;

        private final PropertyHelper _adapter;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * 
     * @param actorConstraintType
     * @exception IllegalActionException
     */
    protected void _addDefaultConstraints(ConstraintType actorConstraintType)
            throws IllegalActionException {
        if (!_useDefaultConstraints
                || !AtomicActor.class.isInstance(getComponent())) {
            return;
        }

        boolean constraintSource = actorConstraintType == ConstraintType.SRC_EQUALS_MEET
                || actorConstraintType == ConstraintType.SRC_EQUALS_GREATER;

        List<Object> portList1 = constraintSource ? ((AtomicActor) getComponent())
                .inputPortList()
                : ((AtomicActor) getComponent()).outputPortList();

        List<Object> portList2 = constraintSource ? ((AtomicActor) getComponent())
                .outputPortList()
                : ((AtomicActor) getComponent()).inputPortList();

        Iterator ports = portList1.iterator();

        while (ports.hasNext()) {
            IOPort port = (IOPort) ports.next();
            _constraintObject(actorConstraintType, port, portList2);
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
    protected void _addSubHelperConstraints() throws IllegalActionException {
        Iterator adapters = _getSubHelpers().iterator();

        while (adapters.hasNext()) {
            PropertyConstraintHelper adapter = (PropertyConstraintHelper) adapters
                    .next();
            _subHelperConstraints.addAll(adapter.constraintList());
        }
    }

    protected ParseTreeAnnotationEvaluator _annotationEvaluator() {
        return new ParseTreeConstraintAnnotationEvaluator();
    }

    protected void _constraintAttributes() {

        for (Attribute attribute : _getPropertyableAttributes()) {

            try {
                ASTPtRootNode node = getParseTree(attribute);

                // Take care of actors without nodes, e.g. MonitorValue actors without previous execution
                if (node != null) {
                    PropertyConstraintASTNodeHelper astHelper = ((OntologySolver) _solver)
                            .getHelper(node);

                    List list = new ArrayList();
                    list.add(node);

                    _constraintObject(astHelper.interconnectConstraintType,
                            attribute, list);
                    //setSameAs(attribute, getParseTree(attribute));
                    //setAtLeast(attribute, getParseTree(attribute));
                }

            } catch (IllegalActionException ex) {
                // This means the expression is not parse-able.
                assert false;
            }
        }
    }

    /**
     * @param constraintType
     * @param object
     * @param objectList
     * @exception IllegalActionException
     */
    protected void _constraintObject(ConstraintType constraintType,
            Object object, List objectList) throws IllegalActionException {

        boolean isEquals = constraintType == ConstraintType.EQUALS
                || constraintType == ConstraintType.SINK_EQUALS_MEET
                || constraintType == ConstraintType.SRC_EQUALS_MEET;

        boolean useMeetFunction = constraintType == ConstraintType.SRC_EQUALS_MEET
                || constraintType == ConstraintType.SINK_EQUALS_MEET;

        if (constraintType != ConstraintType.NONE) {
            if (!useMeetFunction) {

                for (Object object2 : objectList) {

                    if (isEquals) {
                        setSameAsByDefault(object, object2);

                    } else {
                        if (object2 instanceof ASTPtRootNode) {
                            if (constraintType == ConstraintType.SINK_EQUALS_GREATER) {
                                setAtLeastByDefault(object, object2);
                            } else {
                                setAtLeastByDefault(object2, object);
                            }
                        } else {
                            setAtLeastByDefault(object, object2);
                        }
                    }
                }
            } else {
                if (objectList.size() > 0) {
                    /* Removed to make compile
                     * --Ben on 12/04/2009
                    InequalityTerm term2 = new MeetFunction(getSolver(),
                            objectList);
                    setSameAsByDefault(object, term2);
                     */
                }
            }
        }
    }

    protected void _constraintObject(ConstraintType constraintType,
            Object object, Set<Object> objectList)
            throws IllegalActionException {
        _constraintObject(constraintType, object, new ArrayList<Object>(
                objectList));
    }

    /**
     * Return the list of constrained ports given the flag whether source or
     * sink ports should be constrainted. If source ports are constrained, it
     * returns the list of input ports of the assoicated actor; otherwise, it
     * returns the list of output ports.
     * @param constraintSource The flag that indicates whether source or sink
     * ports are constrainted.
     * @return The list of constrainted ports.
     */
    protected List _getConstraintedPorts(boolean constraintSource) {
        Actor actor = (Actor) getComponent();
        return constraintSource ? actor.outputPortList() : actor
                .inputPortList();
    }

    /**
     * Return the list of constraining ports on a given port, given whether
     * source or sink ports should be constrainted.
     * @param constraintSource The flag that indicates whether source or sink
     * ports are constrainted.
     * @param port The given port.
     * @return The list of constrainting ports.
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
    protected List<PropertyHelper> _getSubHelpers()
            throws IllegalActionException {
        OntologySolver solver = getSolver();
        if (solver.expressionASTNodeConstraintType.getExpression().equals(
                "NONE")) {
            return new LinkedList();
        }
        return _getASTNodeHelpers();
    }

    /**
     * Create a constraint that set the first term to be at least the second
     * term.
     * @param term1 The greater term.
     * @param term2 The lesser term.
     */
    protected void _setAtLeast(InequalityTerm term1, InequalityTerm term2,
            boolean isBase) {
        if (term1 != null && term2 != null) {
            _ownConstraints.add(new Inequality(term2, term1, isBase));
        }

        // FIXME: Why are we setting the value here?
        if (term2 instanceof Concept) {
            try {
                term1.setValue(term2);
            } catch (IllegalActionException e) {
                assert false;
            }
        }
    }

    /**
     * 
     * @param constraintType
     * @exception IllegalActionException
     */
    protected void _setConnectionConstraintType(ConstraintType constraintType,
            ConstraintType compositeConstraintType,
            ConstraintType fsmConstraintType,
            ConstraintType expressionASTNodeConstraintType)
            throws IllegalActionException {

        Iterator adapters = _getSubHelpers().iterator();

        while (adapters.hasNext()) {
            PropertyConstraintHelper adapter = (PropertyConstraintHelper) adapters
                    .next();

            adapter._setConnectionConstraintType(constraintType,
                    compositeConstraintType, fsmConstraintType,
                    expressionASTNodeConstraintType);
        }

        if (getComponent() instanceof ASTPtRootNode) {

            interconnectConstraintType = expressionASTNodeConstraintType;

        } else if (getComponent() instanceof ModalModel
                || getComponent() instanceof FSMActor) {

            interconnectConstraintType = fsmConstraintType;

        } else if (getComponent() instanceof CompositeEntity) {

            interconnectConstraintType = compositeConstraintType;

        } else {
            interconnectConstraintType = constraintType;
        }
    }

    /*
     * private void _removeConstraints() { Set<Inequality> removeConstraints =
     * new HashSet<Inequality>();
     * 
     * Iterator inequalities = _constraints.iterator(); while
     * (inequalities.hasNext()) { Inequality inequality = (Inequality)
     * inequalities.next(); List<InequalityTerm> variables =
     * _deepGetVariables(inequality.getGreaterTerm().getVariables());
     * 
     * variables.addAll(
     * _deepGetVariables(inequality.getLesserTerm().getVariables()));
     * 
     * Iterator iterator = variables.iterator();
     * 
     * while (iterator.hasNext()) { InequalityTerm term = (InequalityTerm)
     * iterator.next(); if
     * (nonConstraintings.contains(term.getAssociatedObject())) {
     * removeConstraints.add(inequality); } } }
     * _constraints.removeAll(removeConstraints); }
     */
    protected void _setEffectiveTerms() {
        // do nothing in here, overwrite use-case specific!

    }

    /** The list of permanent property constraints. */
    protected List<Inequality> _subHelperConstraints = new LinkedList<Inequality>();

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
    ////                       protected variables                 ////

    protected List<Inequality> _ownConstraints = new LinkedList<Inequality>();

    /** The property lattice. */
    protected ConceptLattice _lattice;

    /** Indicate whether this adapter uses the default actor constraints. */
    protected boolean _useDefaultConstraints;

}
