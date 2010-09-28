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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import ptolemy.data.StringToken;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.ontologies.FiniteConcept;
import ptolemy.data.ontologies.OntologyAdapter;
import ptolemy.data.ontologies.OntologyResolutionException;
import ptolemy.data.ontologies.OntologySolver;
import ptolemy.data.ontologies.OntologySolverModel;
import ptolemy.data.ontologies.gui.OntologySolverGUIFactory;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.graph.CPO;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// LatticeOntologySolver

/**
 * An instance of this solver contains an <i>ontology</i>, which itself
 * contains a {@linkplain ptolemy.data.ontologies.ConceptGraph ConceptGraph}
 * and default constraints. The LatticeOntologySolver
 * contains an {@linkplain ptolemy.data.ontologies.Ontology Ontology} whose
 * ConceptGraph must be a lattice.  It uses the
 * Rehof-Mogensen algorithm to resolve which {@linkplain ptolemy.data.ontologies.FiniteConcept Concepts}
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
        _model = new OntologySolverModel(this, workspace());

        // Provide a single parameter to set the others, plus a "custom" option
        // However, for the other parameters, still call setExpression here
        // in case the solverStrategy was set to custom (for example in 
        // a saved model) and in case no parameter values were specified.
        // The "custom" option makes no changes to the other parameters.
        solverStrategy = new StringParameter(this, "solverStrategy");
        solverStrategy.setExpression("forward");
        
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

        _applySolverStrategy();
        
        _addChoices();

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"40\" "
                + "style=\"fill:blue\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\nApply Ontology</text></svg>");

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
    
    /**
     * A single parameter for quickly selecting common solving strategies, 
     * plus a "custom" option to allow a user-defined strategy.
     */
    public StringParameter solverStrategy;
    

    /** The string name of the attribute that defines the arithmetic multiply
     *  concept function for this ontology solver.
     */
    public static final String MULTIPLY_FUNCTION_NAME = "multiplyFunction";

    /** The string name of the attribute that defines the arithmetic divide
     *  concept function for this ontology solver.
     */
    public static final String DIVIDE_FUNCTION_NAME = "divideFunction";

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the attributeChanged method so that if the solver
     *  strategy changes, the other related parameters are updated.
     *  @param attribute The attribute that has been changed.
     *  @throws IllegalActionException If there is a problem changing the attribute.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute.equals(solverStrategy)) {
            _applySolverStrategy();
        }
        super.attributeChanged(attribute);
    }
    
    /**
     * Get the list of affected InequalityTerms from the OntologySolver's
     * ConceptTermManager.
     * FIXME: 01/28/10 Charles Shelton - Not really sure what this method is used for. The call to
     * _conceptTermManager.getAffectedTerms() appears to always return
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

    /** Initialize the solver and get the initial statistics for the
     *  Lattice OntologySolver.  This will return information about
     *  the number of constraints and concept terms generated before
     *  the solver executes its algorithm.
     * 
     *  @return A hash table containing string representations of the
     *   solver statistics and constraints information, separated by
     *   tabs.
     *  @exception IllegalActionException If an exception occurs when
     *  collecting the constraints.
     */
    public Hashtable getInitialSolverInformation()
            throws IllegalActionException {
        resetAll();

        initialize();

        String initialSolverConstraints = 
            _getConstraintsAsString(_initialConstraintList);

        Hashtable initialSolverInfo = new Hashtable();
        initialSolverInfo.put("initialSolverConstraints",
                initialSolverConstraints);

        return initialSolverInfo;
    }

    /** Get the statistics for the Lattice OntologySolver after the
     *  model has been resolved.  This will return information about
     *  the number of constraints and concept terms generated after
     *  the solver executes its algorithm.
     * 
     *  @return A hash table containing string representations of the
     *   solver statistics and constraints information, separated by
     *   tabs.
     *  @exception IllegalActionException If an exception occurs when
     *  collecting the constraints.
     */
    public Hashtable getResolvedSolverInformation()
            throws IllegalActionException {
        if (_resolvedConstraintList == null) {
            try {
                resolveConcepts();
            } catch (KernelException kernelEx) {
                throw new IllegalActionException(this, kernelEx,
                        "Error while trying to execute LatticeOntologySolver "
                                + getName() + " resolution algorithm: "
                                + kernelEx);
            }
        }

        String resolvedSolverConstraints = _getConstraintsAsString(_resolvedConstraintList);

        Hashtable resolvedSolverInfo = new Hashtable();
        resolvedSolverInfo.put("resolvedSolverConstraints",
                resolvedSolverConstraints);

        // Reset the resolved constraint list for the next time it is tested.
        _resolvedConstraintList = null;

        return resolvedSolverInfo;
    }

    /**
     * Returns the adapter that contains concept information for the given AST
     * node.
     * @param node The given ASTPtRootNode.
     * @return The associated concept constraint adapter.
     * @exception IllegalActionException If an exception is thrown in the private
     * _getHelper method
     */
    public LatticeOntologyASTNodeAdapter getAdapter(ASTPtRootNode node)
            throws IllegalActionException {

        return (LatticeOntologyASTNodeAdapter) _getAdapter(node);
    }

    /**
     * Returns the adapter that contains concept information for the given
     * component.
     * @param component The given component
     * @return The associated concept constraint adapter.
     * @exception IllegalActionException If an exception is thrown in the private
     * _getHelper method
     */
    public OntologyAdapter getAdapter(NamedObj component)
            throws IllegalActionException {

        return _getAdapter(component);
    }

    /**
     * Return the concept constraint adapter associated with the given object.
     * @param object The given object.
     * @return The associated concept constraint adapter.
     * @exception IllegalActionException If an exception is thrown in the private
     * _getHelper method
     */
    public OntologyAdapter getAdapter(Object object)
            throws IllegalActionException {

        return _getAdapter(object);
    }

    
    /**
     * Return the concept value associated with the specified object.
     * @param object The specified object.
     * @return The concept of the specified object.
     */
    /*
    public Concept getConcept(Object object) {
        try {
            return (Concept) getConceptTerm(object).getValue();
        } catch (IllegalActionException ex) {
            return null;
        }
    }
    */

    /**
     * Return the concept term from the given object.
     * @param object The given object.
     * @return The concept term of the given object.
     */
    public ptolemy.graph.InequalityTerm getConceptTerm(Object object) {
        return getConceptTermManager().getConceptTerm(object);
    }

    /**
     * Return the concept term manager that collects and maintains a hash map
     * that maps all model objects to their the inequality terms for the OntologySolver.
     * 
     * @return The concept term manager for the OntologySolver
     */
    public ConceptTermFactory getConceptTermManager() {
        if (_conceptTermManager == null) {
            _conceptTermManager = _getConceptTermManager();
        }
        return _conceptTermManager;
    }
    
    /**
     * Initialize the solver:  Reset the solver (superclass) and then collect 
     * all of the initial constraints from the model.
     *  @exception IllegalActionException If an exception occurs when
     *  collecting the constraints.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        
        NamedObj toplevel = _toplevel();
        LatticeOntologyAdapter toplevelAdapter = 
            (LatticeOntologyAdapter) getAdapter(toplevel);

        // FIXME: The code from here to constraintList() doesn't really
        // belong here. The constraintList() method of the Adapter should
        // ensure that the constraint list it returns is valid.
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
        
        _initialConstraintList = toplevelAdapter.constraintList();
    }

    /**
     * Return true if the given model object has been annotated with a
     * manual annotation constraint.
     * 
     * @param object The model object to be checked to see if it is annotated
     * @return true if the model object is annotated, false otherwise
     */
    public boolean isAnnotatedTerm(Object object) {
        return _annotatedObjects.contains(object);
    }
    
    /**
     * Reset the solver. This removes the internal states of the
     * solver (e.g.  previously recorded properties, statistics,
     * etc.). Also resets the {@linkplain ConceptTermManager} to null
     * and clears the trained constraints.
     */
    public void reset() {
        super.reset();
        _clearLists();
        _conceptTermManager = null;
    }

    /** Run concept inference and check the values match those trained.
     * 
     *  This simply looks through the conceptable objects and
     *  checks that their resolved concepts match the value
     *  contained in the <i>_trainedConcept</i> attribute.  Conceptables
     *  without a <i>_trainedConcept</i> attribute are just ignored, and
     *  do not cause the test to fail.
     * 
     *  @exception IllegalActionException If inference fails or the test
     *   resolves to the wrong values.
     */
    public void test() throws IllegalActionException {
        try {
            workspace().getWriteAccess();
            resetAll();
            invokeSolver();
        } finally {
            workspace().doneWriting();
        }
        for (NamedObj conceptable : getAllConceptableNamedObjs()) {
            StringParameter trained = (StringParameter) conceptable
                    .getAttribute("_trainedConcept");
            if (trained == null) {
                // Testing a conceptable that is not trained should not
                // cause the test to fail.
                continue;
            }
            FiniteConcept inferredConcept = getConcept(conceptable);
            if (inferredConcept == null) {
                throw new IllegalActionException(conceptable,
                        "Testing failure at " + conceptable.toString() + '\n'
                                + "Expected '" + trained.stringValue()
                                + "' but did not infer anything.");
            }
            String inferred = inferredConcept.toString();
            if (!inferred.equals(trained.stringValue())) {
                throw new IllegalActionException(conceptable,
                        "Testing failure at " + conceptable.toString() + '\n'
                                + "Expected '" + trained.stringValue()
                                + "' but got '" + inferred + "' instead.");
            }
        }
    }

    /** Run concept inference and save the inferred concept values.
     * 
     *  For values that are correctly resolved to a non-null concept,
     *  a string representation of the concept is stored in the
     *  <i>_trainedConcept</i> attribute of the NamedObj.  For values
     *  that resolve to null, nothing is recorded.
     *  
     *  @exception IllegalActionException If inference fails..
     */
    public void train() throws IllegalActionException {
        try {
            workspace().getWriteAccess();
            resetAll();
            invokeSolver();
            for (NamedObj conceptable : getAllConceptableNamedObjs()) {
                FiniteConcept inferred = getConcept(conceptable);
                if (inferred == null) {
                    // If we have conceptables that do not resolve to concepts,
                    // simply skip them.
                    continue;
                }
                StringParameter trained;
                try {
                    trained = new StringParameter(conceptable,
                            "_trainedConcept");
                } catch (NameDuplicationException e) {
                    trained = (StringParameter) conceptable
                            .getAttribute("_trainedConcept");
                }
                trained.setExpression(inferred.toString());
            }
        } finally {
            workspace().doneWriting();
        }
    }

    
    /** Return a string representing the list of terms that resolved to
     *  unacceptable concepts.  Returns an empty string if the solver has not
     *  been run, or if all terms were acceptable. 
     *  
     *  See {@linkplain ptolemy.data.ontologies.lattice.ConceptTermManager ConceptTermManager} 
     *  for a definition of acceptable.
     * 
     *  @return A string representing the list of terms that resolved to 
     *          unacceptable concepts.  Can be the empty string.
     *  @throws IllegalActionException If the string cannot be formed from the 
     *          list of inequality terms
     */
    protected String getUnacceptableTermsAsString() throws IllegalActionException {

        StringBuffer output = new StringBuffer();
        
        if (_resolvedUnacceptableList != null 
                && !_resolvedUnacceptableList.isEmpty()) {
            for (InequalityTerm term : _resolvedUnacceptableList) {
            output.append(term.toString() + _eol);
            }
        }

        return output.toString();
    }
    
    /** Returns true if one or more terms resolved to unacceptable concepts, 
     *  false otherwise (if the solver has not run, or if all terms have 
     *  acceptable resolved concepts.   
     * 
     * @return  True if one or more terms resolved to unacceptable concepts,
     *          false otherwise.
     */
    protected boolean hasUnacceptableTerms()
    {
        if (_resolvedUnacceptableList != null 
                && !_resolvedUnacceptableList.isEmpty()) {
            return true;
        }
        return false;
    }
    
    /** Return the list of inequality terms that resolved to unacceptable
     *  concepts.  Returns null if the solver has not been run first, or if all 
     *  terms were acceptable.  
     *  See {@linkplain ptolemy.data.ontologies.lattice.ConceptTermManager ConceptTermManager} 
     *  for a definition of acceptable.
     *  
     *  @return  The list of inequality terms that resolved to unacceptable 
     *           concepts.  Can be null.
     */
    protected List<InequalityTerm> getUnacceptableTerms() {
        return _resolvedUnacceptableList;
    }
    
    /**
     *  Set the solver's options and the visibility of those options according
     *  to the value of the solverStrategy parameter.  This should be called 
     *  whenever the solverStrategy parameter changes.
     */
    protected void _applySolverStrategy() {
        // If solver strategy is not set, do not make any changes
        if (solverStrategy != null 
                && solverStrategy.getValueAsString().length() != 0) {
            String strategy = solverStrategy.getValueAsString();
            
            // For the first three strategies, set parameters to certain
            // values and set visibility of parameters to not editable
            if(strategy.contains("forward") || strategy.contains("backward") 
                    || strategy.contains("bidirectional")) {
                
                // Common settings and set visibility to NOT_EDITABLE.               
                solvingFixedPoint.setExpression("least");
                
                actorConstraintType.setVisibility(Settable.NOT_EDITABLE);
                compositeConnectionConstraintType
                    .setVisibility(Settable.NOT_EDITABLE);
                connectionConstraintType.setVisibility(Settable.NOT_EDITABLE);
                expressionASTNodeConstraintType
                    .setVisibility(Settable.NOT_EDITABLE);
                fsmConstraintType.setVisibility(Settable.NOT_EDITABLE);
                solvingFixedPoint.setVisibility(Settable.NOT_EDITABLE);
                
                
                // Specific settings
                if (strategy.contains("forward")) {
                    actorConstraintType.setExpression("out >= in");
                    compositeConnectionConstraintType
                        .setExpression("sink >= src");
                    
                    connectionConstraintType.setExpression("sink >= src");
                    expressionASTNodeConstraintType
                        .setExpression("parent >= child");
                    fsmConstraintType.setExpression("sink >= src");
                }
                else if (strategy.contains("backward")) {
                    actorConstraintType.setExpression("in >= out");
                    compositeConnectionConstraintType
                        .setExpression("src >= sink");
                    connectionConstraintType.setExpression("src >= sink");
                    expressionASTNodeConstraintType
                        .setExpression("child >= parent");
                    fsmConstraintType.setExpression("src >= sink");
                }
                else {
                    actorConstraintType.setExpression("out == in");
                    compositeConnectionConstraintType
                        .setExpression("sink == src");
                    connectionConstraintType.setExpression("sink == src");
                    // FIXME:  Test old solver expressions - this was 
                    // parent >= child previously
                    expressionASTNodeConstraintType
                        .setExpression("parent == child");
                    fsmConstraintType.setExpression("sink == src");
                }
                
                // All of these expressions should be valid
                try {
                    actorConstraintType.validate();
                    compositeConnectionConstraintType.validate();
                    expressionASTNodeConstraintType.validate();
                    fsmConstraintType.validate();
                } catch(IllegalActionException e){};
            }

            // For the custom strategy, set visibility of parameters to 
            // editable.  Values are not changed.
            else if (strategy.contains("custom")) {
                actorConstraintType.setVisibility(Settable.FULL);
                compositeConnectionConstraintType.setVisibility(Settable.FULL);
                connectionConstraintType.setVisibility(Settable.FULL);
                expressionASTNodeConstraintType.setVisibility(Settable.FULL);
                fsmConstraintType.setVisibility(Settable.FULL);
                solvingFixedPoint.setVisibility(Settable.FULL);
            }
            // Do nothing if the strategy is none of the above
        }
    }
    
    /** 
     * Set the resolved constraint list and the list of unacceptable 
     * inequality terms to null.  Implemented as a protected method so that 
     * subclasses may call it.  
     */
    protected void _clearLists() {
        _initialConstraintList = null;
        _resolvedConstraintList = null;
        _resolvedUnacceptableList = new ArrayList<InequalityTerm>();
    }
    
    protected void _isTermAcceptable() {
        // null is acceptable
        
       
        
    }
    
    // Illegal action exception thrown from getOntology() 
    
    // Collect all information related to whether or not terms are unacceptable
    
    // For each term, check if its concept is acceptable for that term
    // The notion of acceptable can currently be defined at three levels:
    // Per-lattice:  The concept is not acceptable for any term that uses that lattice
    // Per-container:  The concept is not acceptable for any term in the specified 
    //  container.  The container can be a whole model.
    // Per-term:  The concept is not acceptable for the specified term
    //  (acceptance criteria translate to one or more of these)
    
    // Return a list/table of terms, true/false?
    // Allow checking of an individual term?
    
    // Per-lattice
    
    
    // Per-container.  Not implemented currently.
    
    // Per-term
    
    
    // collectUnacceptableConcepts(Ontology?)
    // A concept is deemed acceptable unless explicitly specified otherwise
    // DO this way, or should ontology provide a list?  Perhaps ontology should provide a list?
    
    
    // collectUnacceptableConcepts(Container?)
    // collectUnacceptableConcepts(Term?)
    // call lattice - getUnacceptableConcepts() - for per-lattice - save list
    // make it a sorted set for easy access
    // nothing implemented per-container yet
    // collect all acceptance criteria constraints and determine list for these
    
    /**
     * Return the LatticeOntologyAdapter for the specified
     * component. This instantiates a new OntologyAdapter if it does
     * not already exist for the specified component.  This returns
     * specific LatticeOntologyAdapters for the LatticeOntologySolver.
     * 
     * @param component The specified component.
     * @return The LatticeOntologyAdapter for the specified component.
     * @exception IllegalActionException Thrown if the LatticeOntologyAdapter
     * cannot be instantiated.
     */
    protected OntologyAdapter _getAdapter(Object component)
            throws IllegalActionException {
        OntologyAdapter adapter = null;

        // First see if the adapter has already been cached
        // Must check the adapter store first because additional state
        // is stored in the adapter that will be lost if we generate a new adapter from
        // a model defined adapter definition after it was already instantiated before.
        // This fixes the bug where backwards and bidirectional ontology solvers
        // were not working because the adapter's interconnectConstraintType member variable
        // would be reset to null because a new adapter object was created from the model
        // definition.
        if (_adapterStore.containsKey(component)) {
            return _adapterStore.get(component);
        } else {
            // Next look for the adapter in the LatticeOntologySolver model.
            List modelDefinedAdapters = ((OntologySolverModel) _model)
                    .attributeList(ActorConstraintsDefinitionAttribute.class);
            for (Object adapterDefinitionAttribute : modelDefinedAdapters) {
                if (((StringToken) ((ActorConstraintsDefinitionAttribute) adapterDefinitionAttribute).actorClassName
                        .getToken()).stringValue().equals(component.getClass().getName())) {
                    adapter = ((ActorConstraintsDefinitionAttribute) adapterDefinitionAttribute)
                            .createAdapter((ComponentEntity) component);
                    break;
                }
            }
        }

        if (adapter == null) {
            try {
                adapter = super._getAdapter(component);
            } catch (IllegalActionException ex) {
            }
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
     * Return a new concept term manager. Subclass can implements a its own
     * manager and override this method to instantiate a instance of the new
     * class.
     * @return A concept term manager.
     */
    protected ConceptTermManager _getConceptTermManager() {
        //      FIXME: doesn't work for other use-cases!
        //      return new StaticDynamicTermManager(this);
        return new ConceptTermManager(this);
    }    
    
    /**
     * Resolve the concept values for the toplevel entity that contains this
     * solver, given the model analyzer that invokes this.
     * @exception KernelException If there is an exception thrown during the OntologySolver
     * resolution
     */
    protected void _resolveConcepts() throws KernelException {
        // Reset the list of resolved constraints and list of acceptable 
        // inequality terms before executing the ontology solver resolution. 
        _clearLists();
        initialize();
        
        NamedObj toplevel = _toplevel();
        LatticeOntologyAdapter toplevelAdapter = 
            (LatticeOntologyAdapter) getAdapter(toplevel);

        _resolveConcepts(toplevel, toplevelAdapter, _initialConstraintList);
    }

    /** Resolve the concepts for the given top-level container,
     *  subject to the given constraint list.
     * @param toplevel The top-level container
     * @param toplevelAdapter Must be toplevel.getAdapter()
     * @param constraintList The constraint list that we are solving
     * @exception OntologyResolutionException If constraints are unsatisfiable
     */
    protected void _resolveConcepts(NamedObj toplevel,
            LatticeOntologyAdapter toplevelAdapter,
            List<Inequality> constraintList) 
        throws OntologyResolutionException {

        List<Inequality> conflicts = new ArrayList<Inequality>();        

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
                // concept conflicts.
                for (Inequality inequality : constraintList) {

                    if (!inequality.isSatisfied(lattice)) {
                        conflicts.add(inequality);

                    } else {
                        // Check if there exist an unacceptable term value.
                        InequalityTerm[] lesserVariables = inequality
                                .getLesserTerm().getVariables();

                        InequalityTerm[] greaterVariables = inequality
                                .getGreaterTerm().getVariables();

                        // Collect all unacceptable terms from the inequalities
                        for (InequalityTerm variable : lesserVariables) {
                            if (!variable.isValueAcceptable()) {
                                _resolvedUnacceptableList.add(variable);
                            }
                        }

                        // Continue checking for unacceptable terms.
                        for (InequalityTerm variable : greaterVariables) {
                            if (!variable.isValueAcceptable()) {
                                _resolvedUnacceptableList.add(variable);
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
            /*
            if (unacceptable.size() > 0) {
                throw new TypeConflictException(unacceptable,
                        "Properties resolved to unacceptable types in "
                                + toplevel.getFullName()
                                + " due to the following inequalities:");
            }
            */

        } catch (IllegalActionException ex) {
            // This should not happen. The exception means that
            // _checkDeclaredProperty or constraintList is called on a
            // transparent actor.
            throw new OntologyResolutionException(this, toplevel, ex,
                    "Concept resolution failed because of an error "
                            + "during concept inference");
        }

    }

    /**
     * Add choices to the parameters.
     * @exception IllegalActionException If there is a problem accessing files
     * or parameters.
     */
    private void _addChoices() throws IllegalActionException {
        solverStrategy.addChoice("forward");
        solverStrategy.addChoice("backward");
        solverStrategy.addChoice("bidirectional");
        solverStrategy.addChoice("custom");
        
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
    private String _getConstraintsAsString(
            List<Inequality> constraintList) throws IllegalActionException {

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

    /** The list of constraints collected from the model, before resolution.  */
    private List<Inequality> _initialConstraintList;
    
    /** The list of constraints after the ontology resolution algorithm has executed. */
    private List<Inequality> _resolvedConstraintList;
    
    /** The list of InequalityTerms that have resolved to an unacceptable concept value. 
     *  See {@linkplain ptolemy.data.ontologies.lattice.ConceptTermManager ConceptTermManager}
     *  for a definition of unacceptable.  */
    private List<InequalityTerm> _resolvedUnacceptableList;

}
