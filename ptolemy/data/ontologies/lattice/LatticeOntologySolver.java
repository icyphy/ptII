/* A solver for lattice-based ontologies.
 *
 * Copyright (c) 2007-2013 The Regents of the University of California. All
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
import java.util.Set;

import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.OntologyAdapter;
import ptolemy.data.ontologies.OntologyResolutionException;
import ptolemy.data.ontologies.OntologySolver;
import ptolemy.data.ontologies.OntologySolverBase;
import ptolemy.data.ontologies.OntologySolverModel;
import ptolemy.data.ontologies.gui.OntologySolverGUIFactory;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.graph.CPO;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

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
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class LatticeOntologySolver extends OntologySolver {

    /** Constructor for the OntologySolver.
     *  @param container The model that contains the OntologySolver
     *  @param name The name of the OntologySolver
     *  @exception IllegalActionException If there is any problem creating the
     *   OntologySolver object.
     *  @exception NameDuplicationException If there is already a component
     *   in the container with the same name
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

        _trainedConceptRecordArray = new Parameter(this,
                "_trainedConceptRecordArray");
        _trainedConceptRecordArray.setVisibility(Settable.NONE);
        _trainedConceptRecordArray.setPersistent(true);
        _setTrainedConceptsParameterType();

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

    /** The string name of the attribute that defines the arithmetic add
     *  concept function for this ontology solver.
     */
    public static final String ADD_FUNCTION_NAME = "addFunction";

    /** The string name of the attribute that defines the arithmetic subtract
     *  concept function for this ontology solver.
     */
    public static final String SUBTRACT_FUNCTION_NAME = "subtractFunction";

    /** The string name of the attribute that defines the arithmetic multiply
     *  concept function for this ontology solver.
     */
    public static final String MULTIPLY_FUNCTION_NAME = "multiplyFunction";

    /** The string name of the attribute that defines the arithmetic divide
     *  concept function for this ontology solver.
     */
    public static final String DIVIDE_FUNCTION_NAME = "divideFunction";

    /** The string name of the attribute that defines the arithmetic negation
     *  concept function for this ontology solver.
     */
    public static final String NEGATE_FUNCTION_NAME = "negateFunction";

    /** The string name of the attribute that defines the arithmetic negation
     *  concept function for this ontology solver.
     */
    public static final String RECIPROCAL_FUNCTION_NAME = "reciprocalFunction";

    /** The string name of the attribute that defines the logical not
     *  concept function for this ontology solver.
     */
    public static final String NOT_FUNCTION_NAME = "notFunction";

    /** The string name of the attribute that defines the logical and
     *  concept function for this ontology solver.
     */
    public static final String AND_FUNCTION_NAME = "andFunction";

    /** The string name of the attribute that defines the logical or
     *  concept function for this ontology solver.
     */
    public static final String OR_FUNCTION_NAME = "orFunction";

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an object with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new object.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        LatticeOntologySolver newObject = (LatticeOntologySolver) super
                .clone(workspace);
        newObject._annotatedObjects = new HashSet<Object>();
        newObject._constraintManager = new ConstraintManager(newObject);
        newObject._trainedConceptRecordArray = (Parameter) newObject
                .getAttribute("_trainedConceptRecordArray");
        return newObject;
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
    public Hashtable<String, String> getInitialSolverInformation()
            throws IllegalActionException {
        initialize();

        String initialSolverConstraints = _getConstraintsAsString(_initialConstraintList);

        Hashtable<String, String> initialSolverInfo = new Hashtable<String, String>();
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
    public Hashtable<String, String> getResolvedSolverInformation()
            throws IllegalActionException {
        if (_resolvedConstraintList == null) {
            invokeSolver(false);
        }

        String resolvedSolverConstraints = _getConstraintsAsString(_resolvedConstraintList);

        Hashtable<String, String> resolvedSolverInfo = new Hashtable<String, String>();
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
     * _getAdapter method
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
     * _getAdapter method
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
     * _getAdapter method
     */
    @Override
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

    /** Returns true if one or more terms resolved to unacceptable concepts,
     *  false otherwise (if the solver has not run, or if all terms have
     *  acceptable resolved concepts.
     *
     * @return  True if one or more terms resolved to unacceptable concepts,
     *          false otherwise.
     */
    public boolean hasUnacceptableTerms() {
        if (_resolvedUnacceptableList != null
                && !_resolvedUnacceptableList.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Initialize the solver:  Reset the solver (superclass) and then collect
     * all of the initial constraints from the model.
     *  @exception IllegalActionException If an exception occurs when
     *  collecting the constraints.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        NamedObj toplevel = _toplevel();
        LatticeOntologyAdapter toplevelAdapter = (LatticeOntologyAdapter) getAdapter(toplevel);

        // FIXME: The code from here to constraintList() doesn't really
        // belong here. The constraintList() method of the Adapter should
        // ensure that the constraint list it returns is valid.
        toplevelAdapter.reinitialize();

        // FIXME: have to generate the connection every time
        // because the model structure can changed.
        // (i.e. adding or removing connections.)
        toplevelAdapter._setConnectionConstraintType(_getConstraintType());

        toplevelAdapter._addDefaultConstraints(_getConstraintType());

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

    /** Return true if the solver is finding a least fixed point, and
     *  false if the solver is finding a greatest fixed point.
     *
     * @return True, if solver is solving a least fixed point,
     *    false, if it is solving a greatest fixed point.
     * @exception IllegalActionException If the solvingFixedPoint parameter
     *   contains an invalid value.
     */
    public boolean isLeastFixedPoint() throws IllegalActionException {
        if (solvingFixedPoint.stringValue().equals("least")) {
            return true;
        } else if (solvingFixedPoint.stringValue().equals("greatest")) {
            return false;
        } else {
            throw new IllegalActionException("Invalid fixed point type.\n"
                    + "Must be one of 'least' or 'greatest'.");
        }
    }

    /**
     * Reset the solver. This removes the internal states of the
     * solver (e.g.  previously recorded properties, statistics,
     * etc.). Also resets the {@linkplain ConceptTermManager} to null
     * and clears the trained constraints.
     */
    @Override
    public void reset() {
        super.reset();
        _clearLists();
        _conceptTermManager = null;
    }

    /** Resolve the concept values for the toplevel entity that contains this
     *  solver, given the model analyzer that invokes this.
     *  @exception IllegalActionException If there is an exception thrown during the OntologySolver
     *   resolution.
     */
    @Override
    public void resolveConcepts() throws IllegalActionException {
        NamedObj toplevel = _toplevel();

        _resolveConcepts(toplevel, _initialConstraintList);
    }

    /** Run concept inference and check the values match those trained.
     *
     *  This simply looks through the conceptable objects and
     *  checks that their resolved concepts match the values
     *  contained in the <i>__trainedConceptRecordArray</i> attribute.
     *
     *  @exception IllegalActionException If inference fails or the test
     *   resolves to the wrong values.
     */
    @Override
    public void test() throws IllegalActionException {
        try {
            workspace().getWriteAccess();
            invokeSolver();
        } finally {
            workspace().doneWriting();
        }

        ArrayToken trainedConceptsArrayToken = (ArrayToken) _trainedConceptRecordArray
                .getToken();
        if (trainedConceptsArrayToken == null) {
            throw new IllegalActionException("The " + getName()
                    + " ontology solver has not been trained for ontology "
                    + "concept resolution, so its analysis cannot be tested.");
        } else {
            Token[] trainedConceptRecordsArray = trainedConceptsArrayToken
                    .arrayValue();
            Set<NamedObj> allNamedObjs = getAllConceptableNamedObjs();

            for (Token trainedConceptToken : trainedConceptRecordsArray) {
                RecordToken conceptRecord = (RecordToken) trainedConceptToken;

                String conceptableFullName = ((StringToken) conceptRecord
                        .get(_namedObjLabel)).stringValue();
                NamedObj conceptable = _getConceptableFromFullName(
                        conceptableFullName, allNamedObjs);
                if (conceptable == null) {
                    throw new IllegalActionException(this, "The full name "
                            + conceptableFullName
                            + " does not refer to a valid model object that "
                            + "can be resolved to an ontology concept.");
                }
                String trainedConceptString = ((StringToken) conceptRecord
                        .get(_conceptLabel)).stringValue();

                Concept inferredConcept = getConcept(conceptable);
                if (inferredConcept == null) {
                    if (trainedConceptString != null
                            && !trainedConceptString.equals("")) {
                        throw new IllegalActionException(conceptable,
                                "Testing failure at " + conceptable.toString()
                                        + '\n' + "Expected '"
                                        + trainedConceptString
                                        + "' but did not infer anything.");
                    }
                } else if (!inferredConcept.toString().equals(
                        trainedConceptString)) {
                    throw new IllegalActionException(conceptable,
                            "Testing failure at " + conceptable.toString()
                                    + '\n' + "Expected '"
                                    + trainedConceptString + "' but got '"
                                    + inferredConcept.toString() + "' instead.");
                }
            }

            if (!allNamedObjs.isEmpty()) {
                throw new IllegalActionException(this, "Some of the "
                        + "conceptable model elements do not have "
                        + "trained concept values. They are: " + allNamedObjs);
            }
        }
    }

    /** Run concept inference and save the inferred concept values.
     *
     *  For all conceptable model elements, an array of records that maps the
     *  full name of the Ptolemy element to the name of the concept to which it
     *  was resolved is generated and stored as a parameter of the ontology
     *  solver.
     *
     *  @exception IllegalActionException If inference fails..
     */
    @Override
    public void train() throws IllegalActionException {
        try {
            workspace().getWriteAccess();
            invokeSolver();
            Set<NamedObj> allNamedObjs = getAllConceptableNamedObjs();

            RecordToken[] trainedConcepts = new RecordToken[allNamedObjs.size()];
            int index = 0;
            for (NamedObj conceptable : allNamedObjs) {
                Concept inferredConcept = getConcept(conceptable);

                Token[] recordArray = new Token[2];
                recordArray[0] = new StringToken(conceptable.getFullName());
                if (inferredConcept == null) {
                    recordArray[1] = new StringToken(null);
                } else {
                    recordArray[1] = new StringToken(inferredConcept.toString());
                }
                RecordToken conceptRecord = new RecordToken(
                        _trainedConceptRecordLabels, recordArray);

                trainedConcepts[index++] = conceptRecord;
            }
            _trainedConceptRecordArray
                    .setToken(new ArrayToken(trainedConcepts));
        } finally {
            workspace().doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public inner classes              ////

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

        /** Represents that the sink must be &ge; the source. */
        SINK_GE_SOURCE,

        /** Represents that the source must be &ge; the sink. */
        SOURCE_GE_SINK
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a string representing the list of terms that resolved to
     *  unacceptable concepts.  Returns an empty string if the solver has not
     *  been run, or if all terms were acceptable.
     *
     *  See {@linkplain ptolemy.data.ontologies.lattice.ConceptTermManager ConceptTermManager}
     *  for a definition of acceptable.
     *
     *  @return A string representing the list of terms that resolved to
     *          unacceptable concepts.  Can be the empty string.
     *  @exception IllegalActionException If the string cannot be formed from the
     *          list of inequality terms
     */
    protected String getUnacceptableTermsAsString()
            throws IllegalActionException {

        StringBuffer output = new StringBuffer();

        if (_resolvedUnacceptableList != null
                && !_resolvedUnacceptableList.isEmpty()) {
            for (InequalityTerm term : _resolvedUnacceptableList) {
                output.append(term.toString() + _eol);
            }
        }

        return output.toString();
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
     * Set the resolved constraint list and the list of unacceptable
     * inequality terms to null.  Implemented as a protected method so that
     * subclasses may call it.
     */
    protected void _clearLists() {
        _initialConstraintList = null;
        _resolvedConstraintList = null;
        _resolvedUnacceptableList = new ArrayList<InequalityTerm>();
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
            List<ActorConstraintsDefinitionAttribute> modelDefinedAdapters = ((OntologySolverModel) _model)
                    .attributeList(ActorConstraintsDefinitionAttribute.class);
            for (ActorConstraintsDefinitionAttribute adapterDefinitionAttribute : modelDefinedAdapters) {
                if (((StringToken) adapterDefinitionAttribute.actorClassName
                        .getToken()).stringValue().equals(
                        component.getClass().getName())) {
                    adapter = adapterDefinitionAttribute.createAdapter(
                            (ComponentEntity) component, this);
                    break;
                }
            }
        }

        if (adapter == null) {
            try {
                adapter = OntologySolverBase._getAdapter(component, this);
            } catch (IllegalActionException ex) {
            }
        }

        if (adapter == null) {
            if (component instanceof FSMActor) {
                adapter = new LatticeOntologyModalFSMAdapter(this,
                        (FSMActor) component);
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

    /** Resolve the concepts for the given top-level container,
     *  subject to the given constraint list.
     * @param toplevel The top-level container
     * @param constraintList The constraint list that we are solving
     * @exception OntologyResolutionException If constraints are unsatisfiable
     */
    protected void _resolveConcepts(NamedObj toplevel,
            List<Inequality> constraintList) throws OntologyResolutionException {

        List<Inequality> conflicts = new ArrayList<Inequality>();

        try {
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
                    lattice = getOntology().getConceptGraph();
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

    /** Return the constraint type based on the solver strategy and the
     *  type of the fixed point.
     *  Least fixed points with forward inference give constraints
     *  that the sinks be greater that the sources, bidirectional
     *  inference requires sources and sinks to be equal, etc.
     *
     *  @return The enumeration from ConstraintType corresponding to the
     *      given solver strategy.
     *  @exception IllegalActionException If solver strategy or fixed point
     *      type cannot be understood.
     */
    protected ConstraintType _getConstraintType() throws IllegalActionException {
        String strategy = solverStrategy.stringValue();
        String fixedPoint = solvingFixedPoint.stringValue();
        if (strategy.equals("forward") && fixedPoint.equals("least")
                || strategy.equals("backward") && fixedPoint.equals("greatest")) {
            return ConstraintType.SINK_GE_SOURCE;
        } else if (strategy.equals("backward") && fixedPoint.equals("least")
                || strategy.equals("forward") && fixedPoint.equals("greatest")) {
            return ConstraintType.SOURCE_GE_SINK;
        } else if (strategy.equals("bidirectional")) {
            return ConstraintType.EQUALS;
        } else if (strategy.equals("none")) {
            return ConstraintType.NONE;
        } else {
            throw new IllegalActionException(
                    "Cannot understand solver strategy.\n" + "Strategy: \""
                            + strategy + "\"\n" + "Fixed Point: \""
                            + fixedPoint + '"');
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The list of constraints collected from the model, before resolution.  */
    protected List<Inequality> _initialConstraintList;

    /** The list of constraints after the ontology resolution algorithm has executed. */
    protected List<Inequality> _resolvedConstraintList;

    /** The list of InequalityTerms that have resolved to an unacceptable concept value.
     *  See {@linkplain ptolemy.data.ontologies.lattice.ConceptTermManager ConceptTermManager}
     *  for a definition of unacceptable.  */
    protected List<InequalityTerm> _resolvedUnacceptableList;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Add choices to the parameters.
     * @exception IllegalActionException If there is a problem accessing files
     * or parameters.
     */
    private void _addChoices() throws IllegalActionException {
        solverStrategy.addChoice("forward");
        solverStrategy.addChoice("backward");
        solverStrategy.addChoice("bidirectional");
        solverStrategy.addChoice("none");

        solvingFixedPoint.addChoice("least");
        solvingFixedPoint.addChoice("greatest");
    }

    /** Return the conceptable model element NamedObj that has the given
     *  full name string. Also remove that NamedObj from the given set of
     *  all conceptable NamedObj elements.
     *  @param fullName The full name of the model element.
     *  @param allConceptableNamedObjs The set of all conceptable NamedObj
     *   elements in the model.
     *  @return The NamedObj element in the set with the given full name, or
     *   null if it does not exist.
     */
    private NamedObj _getConceptableFromFullName(String fullName,
            Set<NamedObj> allConceptableNamedObjs) {
        for (NamedObj modelElement : allConceptableNamedObjs) {
            if (modelElement.getFullName().equals(fullName)) {
                allConceptableNamedObjs.remove(modelElement);
                return modelElement;
            }
        }
        return null;
    }

    /** Return a string representing the list of inequality constraints specified
     *  that can be written to a log file.
     *
     *  @param constraintList The list of inequality constraints to be parsed into a string.
     *  @return A string representing the list of inequality constraints that can be written to a log file.
     *  @exception IllegalActionException If the string cannot be formed from the list of inequality constraints.
     */
    private String _getConstraintsAsString(List<Inequality> constraintList)
            throws IllegalActionException {

        StringBuffer output = new StringBuffer();
        for (Inequality inequality : constraintList) {
            output.append(inequality.toString() + _eol);
        }

        return output.toString();
    }

    /** Set the type constraint for the _trainedConceptRecordArray parameter.
     *  @exception IllegalActionException Thrown if there is a problem setting
     *   the type constraint.
     */
    private void _setTrainedConceptsParameterType()
            throws IllegalActionException {
        Type[] typeArray = new Type[2];
        typeArray[0] = BaseType.STRING;
        typeArray[1] = BaseType.STRING;
        RecordType conceptRecordType = new RecordType(
                _trainedConceptRecordLabels, typeArray);
        ArrayType conceptRecordArrayType = new ArrayType(conceptRecordType);
        _trainedConceptRecordArray.setTypeEquals(conceptRecordArrayType);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The set of Objects that have been manually annotated. */
    private/*final*/HashSet<Object> _annotatedObjects = new HashSet<Object>();

    /** The constraint manager that keeps track of all the constraints in the model for the LatticeOntologySolver. */
    private/*final*/ConstraintManager _constraintManager = new ConstraintManager(
            this);

    /** The concept term manager that keeps track of all the concept terms in the model for the LatticeOntologySolver. */
    private ConceptTermManager _conceptTermManager;

    /** The parameter that contains the array of trained concept values for
     *  the model that contains this solver.
     */
    private Parameter _trainedConceptRecordArray;

    /** Label for the NamedObj field of the trained concept record tokens. */
    private static final String _namedObjLabel = "NamedObj";

    /** Label for the Concept field of the trained concept record tokens. */
    private static final String _conceptLabel = "Concept";

    /** The array of labels for the trained concept records. */
    private static final String[] _trainedConceptRecordLabels = new String[] {
            _namedObjLabel, _conceptLabel };
}
