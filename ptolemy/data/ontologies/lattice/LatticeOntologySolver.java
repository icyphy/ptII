/* A solver for lattice-based ontologies.
 * 
 * Copyright (c) 2007-2010 The Regents of the University of California. All
 * rights reserved.
 * 
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
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
 */
package ptolemy.data.ontologies.lattice;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypeConflictException;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.OntologyAdapter;
import ptolemy.data.ontologies.OntologyResolutionException;
import ptolemy.data.ontologies.OntologySolver;
import ptolemy.data.ontologies.gui.OntologySolverGUIFactory;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.graph.CPO;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// LatticeOntologySolver

/**
 * An instance of this solver contains an <i>ontology</i>, which itself
 * contains a {@linkplain ptolemy.data.ontologies.ConceptGraph ConceptGraph}
 * and default constraints. The LatticeOntologySolver
 * contains an {@linkplain ptolemy.data.ontologies.Ontology Ontology} whose
 * ConceptGraph must be a lattice.  It uses the
 * Rehof-Mogensen algorithm to resolve which {@linkplain ptolemy.data.ontologies.Concept Concepts}
 * are assigned to model components.
 * <p>
 * This class is based on the PropertyConstraintSolver in the properties package
 * by Man-Kit Leung.
 * 
 * @author Man-Kit Leung, Edward A. Lee, Charles Shelton, Ben Lickly, Dai Bui, Beth Latronico
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class LatticeOntologySolver extends OntologySolver {

    /**
     * Constructor for the OntologySolver.
     * 
     * @param container The model that contains the OntologySolver
     * @param name The name of the OntologySolver
     * @exception IllegalActionException If there is any problem creating the
     * OntologySolver object.
     * @exception NameDuplicationException If there is already a component
     * in the container with the same name
     */
    public LatticeOntologySolver(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Provide a default model so that this is never empty.
        _model = new CompositeEntity(workspace());

        solvingFixedPoint = new StringParameter(this, "solvingFixedPoint");
        solvingFixedPoint.setExpression("least");

        actorConstraintType = new StringParameter(this, "actorConstraintType");
        actorConstraintType.setExpression("out >= in");

        connectionConstraintType = new StringParameter(this,
                "connectionConstraintType");
        connectionConstraintType.setExpression("sink >= src");

        compositeConnectionConstraintType = new StringParameter(this,
                "compositeConnectionConstraintType");
        compositeConnectionConstraintType.setExpression("sink >= src");

        fsmConstraintType = new StringParameter(this, "fsmConstraintType");
        fsmConstraintType.setExpression("sink >= src");

        expressionASTNodeConstraintType = new StringParameter(this,
                "expressionASTNodeConstraintType");
        expressionASTNodeConstraintType.setExpression("parent >= child");
        
        _addChoices();

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"120\" height=\"40\" "
                + "style=\"fill:blue\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\nResolve Properties</text></svg>");

        new OntologySolverGUIFactory(this, "_LatticeOntologySolverGUIFactory");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** 
     * String that represents the setting for default actor constraints.
     * <ul>
     * <li>"in >= out" The actor inputs must be >= the actor outputs
     * <li>"out >= in" The actor outputs must be >= the actor inputs
     * <li>"out == in" The actor outputs must == the actor inputs
     * <li>"out == meet(in1, in2, ...)" The actor outputs must == the
     * least upper bound of the outputs
     * <li>"in == meet(out1, out2, ...)" The actor inputs must == the
     * least upper bound of the outputs
     * <li>"NONE" No constraints between the actor inputs and outputs
     * </ul>
     */
    public StringParameter actorConstraintType;

    /**
     * String that represents the setting for default composite connection constraints.
     * <ul>
     * <li>"src >= sink" The source must be >= the sink of each composite connection
     * <li>"sink >= src" The sink must be >= the source of each composite connection
     * <li>"sink == src" The sink must == the source of each composite connection
     * <li>"src == meet(sink1, sink2, ...)" The source must == the least upper bound
     * of the sink of each composite connection
     * <li>"sink == meet(src1, src2, ...)" The sink must == the least upper bound
     * of the source of each composite connection
     * <li>"NONE" No constraints between the sources and sinks of composite connections
     * </ul>
     */
    public StringParameter compositeConnectionConstraintType;

    /**
     * String that represents the setting for default connection constraints.
     * <ul>
     * <li>"src >= sink" The source must be >= the sink of each connection
     * <li>"sink >= src" The sink must be >= the source of each connection
     * <li>"sink == src" The sink must == the source of each connection
     * <li>"src == meet(sink1, sink2, ...)" The source must == the least upper bound
     * of the sink of each connection
     * <li>"sink == meet(src1, src2, ...)" The sink must == the least upper bound
     * of the source of each connection
     * <li>"NONE" No constraints between the sources and sinks of connections
     * </ul>
     */
    public StringParameter connectionConstraintType;

    /**
     * String that represents the setting for default AST expression constraints.
     * <ul>
     * <li>"child >= parent" The child node must be >= the parent node
     * <li>"parent >= child" The parent node must be >= the child node
     * <li>"parent == child" The parent node must == the child node
     * <li>"parent == meet(child1, child2, ...)" The parent node must == the least
     * upper bound of the child nodes
     * <li>"NONE"
     * </ul>
     */
    public StringParameter expressionASTNodeConstraintType;

    /**
     * String that represents the setting for default finite state machine constraints.
     * <ul>
     * <li>"src >= sink" The source must be >= the sink of each state transition
     * <li>"sink >= src" The sink must be >= the source of each state transition
     * <li>"sink == src" The sink must == the source of each state transition
     * <li>"src == meet(sink1, sink2, ...)" The source must == the least upper bound
     * of the sink of each state transition
     * <li>"sink == meet(src1, src2, ...)" The sink must == the least upper bound
     * of the source of each state transition
     * <li>"NONE" No constraints between the sources and sinks of state transitions
     * </ul>
     */
    public StringParameter fsmConstraintType;

    /**
     * Indicate whether to compute the least or greatest fixed point solution.
     * <ul>
     * <li> "least" Solve for least fixed point
     * <li> "greatest" Solve for greatest fixed point
     * </ul>
     */
    public StringParameter solvingFixedPoint;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Get the list of affected InequalityTerms from the OntologySolver's
     * PropertyTermManager.
     * FIXME: 01/28/10 Charles Shelton - Not really sure what this method is used for. The call to
     * _propertyTermManager.getAffectedTerms() appears to always return
     * an empty ArrayList.
     * 
     * @param updateTerm This parameter doesn't appear to be used
     * @return The list of inequality terms that are affected by the OntologySolver
     * @exception IllegalActionException If an exception is thrown
     */
    public List<ptolemy.graph.InequalityTerm> getAffectedTerms(
            ptolemy.graph.InequalityTerm updateTerm)
            throws IllegalActionException {
        return _conceptTermManager.getAffectedTerms(updateTerm);
    }

    /**
     * Get the ConstraintManager that collects and maintains all the inequality
     * constraints for the OntologySolver.
     * 
     * @return the constraintManager
     */
    public ConstraintManager getConstraintManager() {
        return _constraintManager;
    }

    /** Initialize the solver and get the initial statistics for the Lattice OntologySolver.
     *  This will return information about the number of constraints and concept terms generated
     *  before the solver executes its algorithm.
     * 
     *  @author Charles Shelton
     *  @return A hash table containing string representations of the solver statistics and
     *   constraints information, separated by tabs.
     *  @exception IllegalActionException If an exception occurs when collecting the constraints.
     */
    public Hashtable getInitialSolverInformation()
            throws IllegalActionException {
        NamedObj toplevel = _toplevel();
        LatticeOntologyAdapter toplevelAdapter = (LatticeOntologyAdapter) getAdapter(toplevel);

        // FIXME: The code from here to constraintList() doesn't really
        // belong here. The constraintList() method of the Adapter should
        // ensure that the constraint list it returns is valid.
        toplevelAdapter.reinitialize();

        toplevelAdapter._addDefaultConstraints(
                _getConstraintType(actorConstraintType.stringValue()));

        // FIXME: have to generate the connection every time
        // because the model structure can changed.
        // (i.e. adding or removing connections.)
        toplevelAdapter._setConnectionConstraintType(
                _getConstraintType(connectionConstraintType.stringValue()),
                _getConstraintType(compositeConnectionConstraintType
                        .stringValue()), _getConstraintType(fsmConstraintType
                        .stringValue()),
                _getConstraintType(expressionASTNodeConstraintType
                        .stringValue()));
        // Collect the constraints in a list

        List<Inequality> constraintList = toplevelAdapter.constraintList();

        String initialSolverConstraints = _getConstraintsAsLogFileString(constraintList);

        Hashtable initialSolverInfo = new Hashtable();
        initialSolverInfo.put("initialSolverConstraints",
                initialSolverConstraints);

        return initialSolverInfo;
    }

    /** Get the statistics for the Lattice OntologySolver after the model has been resolved.
     *  This will return information about the number of constraints and concept terms generated
     *  after the solver executes its algorithm.
     * 
     *  @author Charles Shelton
     *  @return A hash table containing string representations of the solver statistics and
     *   constraints information, separated by tabs.
     *  @exception IllegalActionException If an exception occurs when collecting the constraints.
     */
    public Hashtable getResolvedSolverInformation()
            throws IllegalActionException {
        if (_resolvedConstraintList == null) {
            try {
                resolveProperties();
            } catch (KernelException kernelEx) {
                throw new IllegalActionException(this, kernelEx,
                        "Error while trying to execute LatticeOntologySolver "
                                + getName() + " resolution algorithm: "
                                + kernelEx);
            }
        }

        String resolvedSolverConstraints = _getConstraintsAsLogFileString(_resolvedConstraintList);

        Hashtable resolvedSolverInfo = new Hashtable();
        resolvedSolverInfo.put("resolvedSolverConstraints",
                resolvedSolverConstraints);

        // Reset the resolved constraint list for the next time it is tested.
        _resolvedConstraintList = null;

        return resolvedSolverInfo;
    }

    /**
     * Returns the adapter that contains property information for the given AST
     * node.
     * @param node The given ASTPtRootNode.
     * @return The associated property constraint adapter.
     * @exception IllegalActionException If an exception is thrown in the private
     * _getHelper method
     */
    public LatticeOntologyASTNodeAdapter getAdapter(ASTPtRootNode node)
            throws IllegalActionException {

        return (LatticeOntologyASTNodeAdapter) _getAdapter(node);
    }

    /**
     * Returns the adapter that contains property information for the given
     * component.
     * @param component The given component
     * @return The associated property constraint adapter.
     * @exception IllegalActionException If an exception is thrown in the private
     * _getHelper method
     */
    public OntologyAdapter getAdapter(NamedObj component)
            throws IllegalActionException {

        return _getAdapter(component);
    }

    /**
     * Return the property constraint adapter associated with the given object.
     * @param object The given object.
     * @return The associated property constraint adapter.
     * @exception IllegalActionException If an exception is thrown in the private
     * _getHelper method
     */
    public OntologyAdapter getAdapter(Object object)
            throws IllegalActionException {

        return _getAdapter(object);
    }

    /**
     * Return the property value associated with the specified object.
     * @param object The specified object.
     * @return The property of the specified object.
     */
    public Concept getProperty(Object object) {
        try {
            return (Concept) getConceptTerm(object).getValue();
        } catch (IllegalActionException ex) {
            return null;
        }
    }

    /**
     * Return the concept term from the given object.
     * @param object The given object.
     * @return The concept term of the given object.
     */
    public ptolemy.graph.InequalityTerm getConceptTerm(Object object) {
        return getPropertyTermManager().getConceptTerm(object);
    }

    /**
     * Return the property term manager that collects and maintains a hash map
     * that maps all model objects to their the inequality terms for the OntologySolver.
     * 
     * @return The property term manager for the OntologySolver
     */
    public ConceptTermFactory getPropertyTermManager() {
        if (_conceptTermManager == null) {
            _conceptTermManager = _getPropertyTermManager();
        }
        return _conceptTermManager;
    }

    /**
     * Return true if the given model object has been annotated with a manual annotation
     * constraint.
     * 
     * @param object The model object to be checked to see if it is annotated
     * @return true if the model object is annotated, false otherwise
     */
    public boolean isAnnotatedTerm(Object object) {
        return _annotatedObjects.contains(object);
    }

    /**
     * Reset the solver. This removes the internal states of the solver (e.g.
     * previously recorded properties, statistics, etc.). Also resets the
     * {@linkplain ConceptTermManager} to null and clears the trained constraints.
     */
    public void reset() {
        super.reset();
        _conceptTermManager = null;
    }

    /** Run a test. This invokes the solver in TEST mode.
     *  @exception IllegalActionException If the test fails.
     */
    public void test() throws IllegalActionException {
        invokeSolver();
        resetAll();
    }

    /** Train a test. This invokes the solver in TRAINING mode.
     */
    public void train() {
        // Training is not supported yet.
        try {
            workspace().getWriteAccess();
            invokeSolver();
            resetAll();
        } finally {
            workspace().doneWriting();
        }
    }

    /**
     * Return the LatticeOntologyAdapter for the specified component. This
     * instantiates a new OntologyAdapter if it does not already exist
     * for the specified component.  This returns specific LatticeOntologyAdapters
     * for the LatticeOntologySolver.
     * 
     * @param component The specified component.
     * @return The LatticeOntologyAdapter for the specified component.
     * @exception IllegalActionException Thrown if the LatticeOntologyAdapter
     * cannot be instantiated.
     */
    protected OntologyAdapter _getAdapter(Object component)
            throws IllegalActionException {
        OntologyAdapter adapter = null;

        try {
            adapter = super._getAdapter(component);
        } catch (IllegalActionException ex) {
        }

        if (adapter == null) {
            if (component instanceof FSMActor) {
                /* Removed to make compile
                 * --Ben on 12/04/2009
                adapter = new PropertyConstraintFSMHelper(this,
                        (FSMActor) component);
                } else if (component instanceof ptolemy.domains.modal.kernel.FSMActor) {
                adapter = new PropertyConstraintModalFSMHelper(this,
                        (ptolemy.domains.modal.kernel.FSMActor) component);
                 */
            } else if (component instanceof CompositeEntity) {
                adapter = new LatticeOntologyCompositeAdapter(this,
                        (CompositeEntity) component);
            } else if (component instanceof ASTPtRootNode) {
                adapter = new LatticeOntologyASTNodeAdapter(this,
                        (ASTPtRootNode) component);
            } else {
                adapter = new LatticeOntologyAdapter(this, component);
            }
        }
        _adapterStore.put(component, adapter);
        return adapter;
    }

    /**
     * Return a new property term manager. Subclass can implements a its own
     * manager and override this method to instantiate a instance of the new
     * class.
     * @return A property term manager.
     */
    protected ConceptTermManager _getPropertyTermManager() {
        //      FIXME: doesn't work for other use-cases!
        //      return new StaticDynamicTermManager(this);
        return new ConceptTermManager(this);
    }

    /**
     * Resolve the property values for the toplevel entity that contains this
     * solver, given the model analyzer that invokes this.
     * @exception KernelException If there is an exception thrown during the OntologySolver
     * resolution
     */
    protected void _resolveProperties() throws KernelException {
        // Reset the list of resolved constraints before executing the ontology solver resolution. 
        _resolvedConstraintList = null;

        // FIXME: The code from here to constraintList() doesn't really
        // belong here. The constraintList() method of the Adapter should
        // ensure that the constraint list it returns is valid.
        NamedObj toplevel = _toplevel();
        LatticeOntologyAdapter toplevelAdapter = (LatticeOntologyAdapter) getAdapter(toplevel);

        toplevelAdapter.reinitialize();

        toplevelAdapter
                ._addDefaultConstraints(_getConstraintType(actorConstraintType
                        .stringValue()));

        // FIXME: have to generate the connection every time
        // because the model structure can changed.
        // (i.e. adding or removing connections.)
        toplevelAdapter._setConnectionConstraintType(
                _getConstraintType(connectionConstraintType.stringValue()),
                _getConstraintType(compositeConnectionConstraintType
                        .stringValue()), _getConstraintType(fsmConstraintType
                        .stringValue()),
                _getConstraintType(expressionASTNodeConstraintType
                        .stringValue()));

        // Collect and solve type constraints.
        List<Inequality> constraintList = toplevelAdapter.constraintList();

        _resolveProperties(toplevel, toplevelAdapter, constraintList);
    }

    /** Resolve the properties of the given top-level container,
     *  subject to the given constraint list.
     * @param toplevel The top-level container
     * @param toplevelAdapter Must be toplevel.getAdapter()
     * @param constraintList The constraint list that we are solving
     * @exception TypeConflictException If an unacceptable solution is reached
     * @exception OntologyResolutionException If constraints are unsatisfiable
     */
    protected void _resolveProperties(NamedObj toplevel,
            LatticeOntologyAdapter toplevelAdapter,
            List<Inequality> constraintList) throws TypeConflictException,
            OntologyResolutionException {

        List<Inequality> conflicts = new LinkedList<Inequality>();
        List<Inequality> unacceptable = new LinkedList<Inequality>();

        try {
            // Check declared properties across all connections.
            //List propertyConflicts = topLevel._checkDeclaredProperties();
            //conflicts.addAll(propertyConflicts);

            /*
             * // FIXME: this is the iterative approach. List constraintList =
             * new ArrayList(); Iterator adapters =
             * _adapterStore.values().iterator(); while (adapters.hasNext()) {
             * PropertyConstraintHelper adapter = (PropertyConstraintHelper)
             * adapters.next();
             * 
             * constraintList.addAll(adapter.constraintList()); } //
             */

            // NOTE: To view all property constraints, uncomment these.
            /*
             * Iterator constraintsIterator = constraintList.iterator(); while
             * (constraintsIterator.hasNext()) {
             * System.out.println(constraintsIterator.next().toString()); }
             */

            if (constraintList.size() > 0) {
                CPO lattice = null;

                // Added by Charles Shelton 12/17/09
                // Check to see if the ontology is a lattice
                // and throw an exception if it isn't.                
                if (getOntology().isLattice()) {
                    lattice = getOntology().getGraph();
                } else {
                    throw new IllegalActionException(
                            this,
                            "This Ontology is not a lattice, "
                                    + "and therefore we cannot resolve the model using the least fixed point algorithm.");
                }

                // Instantiate our own customized version of InequalitySolver.
                ptolemy.graph.InequalitySolver solver = new ptolemy.graph.InequalitySolver(
                        lattice);
                //InequalitySolver solver = new InequalitySolver(cpo, this);

                solver.addInequalities(constraintList.iterator());
                _constraintManager.setConstraints(constraintList);

                // Find the greatest solution (most general type)
                if (solvingFixedPoint.stringValue().equals("greatest")) {
                    solver.solveGreatest();
                } else {
                    solver.solveLeast();
                }

                _resolvedConstraintList = constraintList;

                // If some inequalities are not satisfied, or type variables
                // are resolved to unacceptable types, such as
                // BaseType.UNKNOWN, add the inequalities to the list of
                // property conflicts.
                for (Inequality inequality : constraintList) {

                    if (!inequality.isSatisfied(lattice)) {
                        conflicts.add(inequality);

                    } else {
                        // Check if there exist an unacceptable term value.
                        boolean isAcceptable = true;

                        InequalityTerm[] lesserVariables = inequality
                                .getLesserTerm().getVariables();

                        InequalityTerm[] greaterVariables = inequality
                                .getGreaterTerm().getVariables();

                        for (InequalityTerm variable : lesserVariables) {
                            if (!variable.isValueAcceptable()) {
                                unacceptable.add(inequality);
                                isAcceptable = false;
                                break;
                            }
                        }

                        if (isAcceptable) {
                            // Continue checking for unacceptable terms.
                            for (InequalityTerm variable : greaterVariables) {
                                if (!variable.isValueAcceptable()) {
                                    unacceptable.add(inequality);
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            // Check for solution correctness.
            if (conflicts.size() > 0) {
                throw new OntologyResolutionException(this, toplevel(),
                        "Properties conflicts occurred in "
                                + toplevel().getFullName()
                                + " on the following inequalities:\n"
                                + conflicts);
            }
            if (unacceptable.size() > 0) {
                throw new TypeConflictException(unacceptable,
                        "Properties resolved to unacceptable types in "
                                + toplevel.getFullName()
                                + " due to the following inequalities:");
            }

        } catch (IllegalActionException ex) {
            // This should not happen. The exception means that
            // _checkDeclaredProperty or constraintList is called on a
            // transparent actor.
            throw new OntologyResolutionException(this, toplevel, ex,
                    "Concept resolution failed because of an error "
                            + "during property inference");
        }

    }

    /**
     * Add choices to the parameters.
     * @exception IllegalActionException If there is a problem accessing files
     * or parameters.
     */
    private void _addChoices() throws IllegalActionException {
        solvingFixedPoint.addChoice("least");
        solvingFixedPoint.addChoice("greatest");

        actorConstraintType.addChoice("in >= out");
        actorConstraintType.addChoice("out >= in");
        actorConstraintType.addChoice("out == in");
        actorConstraintType.addChoice("out == meet(in1, in2, ...)");
        actorConstraintType.addChoice("in == meet(out1, out2, ...)");
        actorConstraintType.addChoice("NONE");
        //      actorConstraintType.addChoice("in > out");
        //      actorConstraintType.addChoice("out > in");
        //      actorConstraintType.addChoice("out != in");

        connectionConstraintType.addChoice("src >= sink");
        connectionConstraintType.addChoice("sink >= src");
        connectionConstraintType.addChoice("sink == src");
        connectionConstraintType.addChoice("src == meet(sink1, sink2, ...)");
        connectionConstraintType.addChoice("sink == meet(src1, src2, ...)");
        connectionConstraintType.addChoice("NONE");
        //      connectionConstraintType.addChoice("src > sink");
        //      connectionConstraintType.addChoice("sink > src");
        //      connectionConstraintType.addChoice("sink != src");

        compositeConnectionConstraintType.addChoice("src >= sink");
        compositeConnectionConstraintType.addChoice("sink >= src");
        compositeConnectionConstraintType.addChoice("sink == src");
        compositeConnectionConstraintType
                .addChoice("src == meet(sink1, sink2, ...)");
        compositeConnectionConstraintType
                .addChoice("sink == meet(src1, src2, ...)");
        compositeConnectionConstraintType.addChoice("NONE");
        //      compositeConnectionConstraintType.addChoice("src > sink");
        //      compositeConnectionConstraintType.addChoice("sink > src");
        //      compositeConnectionConstraintType.addChoice("sink != src");

        expressionASTNodeConstraintType.addChoice("child >= parent");
        expressionASTNodeConstraintType.addChoice("parent >= child");
        expressionASTNodeConstraintType.addChoice("parent == child");
        //expressionASTNodeConstraintType.addChoice("child == meet(parent1, parent2, ...)");
        expressionASTNodeConstraintType
                .addChoice("parent == meet(child1, child2, ...)");
        expressionASTNodeConstraintType.addChoice("NONE");
        //      expressionASTNodeConstraintType.addChoice("child > parent");
        //      expressionASTNodeConstraintType.addChoice("parent > child");
        //      expressionASTNodeConstraintType.addChoice("parent != child");

        fsmConstraintType.addChoice("src >= sink");
        fsmConstraintType.addChoice("sink >= src");
        fsmConstraintType.addChoice("sink == src");
        fsmConstraintType.addChoice("src == meet(sink1, sink2, ...)");
        fsmConstraintType.addChoice("sink == meet(src1, src2, ...)");
        fsmConstraintType.addChoice("NONE");
        //      fsmConstraintType.addChoice("src > sink");
        //      fsmConstraintType.addChoice("sink > src");
        //      fsmConstraintType.addChoice("sink != src");

    }
    
    /** Return a string representing the list of inequality constraints specified
     *  that can be written to a log file.
     * 
     *  @param constraintList The list of inequality constraints to be parsed into a string.
     *  @return A string representing the list of inequality constraints that can be written to a log file.
     *  @throws IllegalActionException If the string cannot be formed from the list of inequality constraints.
     */
    private String _getConstraintsAsLogFileString(
            List<Inequality> constraintList)
            throws IllegalActionException {

        StringBuffer output = new StringBuffer();
        for (Inequality inequality : constraintList) {
            output.append(inequality.toString() + _eol);
        }

        return output.toString();
    }

    /**
     * Return the constraint type based on the string parsed from the OntologySolver
     * dialog box.
     * 
     * @param typeValue The string representing the constraint type to be parsed
     * @return The constraint type as one of the enumerated ConstraintType values.
     * @exception IllegalActionException If an exception is thrown.
     */
    protected static ConstraintType _getConstraintType(String typeValue)
            throws IllegalActionException {
        boolean isEquals = typeValue.contains("==");
        boolean isSrc = typeValue.startsWith("src")
                || typeValue.startsWith("in") || typeValue.startsWith("child");
        boolean isNotEquals = typeValue.contains("!=");

        boolean hasMeet = typeValue.contains("meet");

        if (typeValue.equals("NONE")) {
            return ConstraintType.NONE;
        }
        if (hasMeet) {
            return isSrc ? ConstraintType.SRC_EQUALS_MEET
                    : ConstraintType.SINK_EQUALS_MEET;

        } else if (isEquals) {
            return ConstraintType.EQUALS;

        } else if (isNotEquals) {
            return ConstraintType.NOT_EQUALS;

        } else {
            return isSrc ? ConstraintType.SRC_EQUALS_GREATER
                    : ConstraintType.SINK_EQUALS_GREATER;
        }
    }

    /**
     * An enumeration type to represent the types of constraints for
     * default constraint settings for actor inputs and outputs, connections
     * and finite state machine transitions.
     */
    public static enum ConstraintType {
        /** Represents that the two sides must be equal. */
        EQUALS,

        /** Represents that there is no constraint between the two sides. */
        NONE,

        /** Represents that the two sides must be unequal. */
        NOT_EQUALS,

        /** Represents that the sink must be >= the source. */
        SINK_EQUALS_GREATER,

        /** Represents that the sink must == the least upper bound of all sources. */
        SINK_EQUALS_MEET,

        /** Represents that the source must be >= the sink. */
        SRC_EQUALS_GREATER,

        /** Represents that the source must == the least upper bound of all sinks. */
        SRC_EQUALS_MEET
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The set of Objects that have been manually annotated. */
    private final HashSet<Object> _annotatedObjects = new HashSet<Object>();

    /** The constraint manager that keeps track of all the constraints in the model for the LatticeOntologySolver. */
    private final ConstraintManager _constraintManager = new ConstraintManager(
            this);

    /** The concept term manager that keeps track of all the concept terms in the model for the LatticeOntologySolver. */
    private ConceptTermManager _conceptTermManager;

    /** The list of constraints after the ontology resolution algorithm has executed. */
    private List<Inequality> _resolvedConstraintList;

}
