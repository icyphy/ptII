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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypeConflictException;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.ASTPtAssignmentNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.OntologyAdapter;
import ptolemy.data.ontologies.OntologyResolutionException;
import ptolemy.data.ontologies.OntologySolver;
import ptolemy.data.ontologies.gui.OntologySolverGUIFactory;
import ptolemy.data.ontologies.lattice.LatticeOntologyAdapter.Inequality;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.MonotonicFunction;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.graph.CPO;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.FileUtilities;

///////////////////////////////////////////////////////////////////
//// LatticeOntologySolver

/**
 * An instance of this solver contains an <i>ontology</i>, which itself
 * contains a {@linkplain ptolemy.data.ontologies.ConceptGraph ConceptGraph}
 * and default constraints. The LatticeOntologySolver
 * contains an {@linkplain ptolemy.data.ontologies.Ontology Ontology} whose
 * ConceptGraph must be a lattice.  It uses the
 * Reihof-Mogensen algorithm to resolve which {@linkplain ptolemy.data.ontologies.Concept Concepts}
 * are assigned to model components.
 * <p>
 * This class is based on the PropertyConstraintSolver in the properties package
 * by Man-Kit Leung.
 * 
 * @author Man-Kit Leung, Edward A. Lee
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

        logMode = new Parameter(this, "logMode");
        logMode.setTypeEquals(BaseType.BOOLEAN);
        logMode.setExpression("false");

        // Set to path of the model.
        logDirectory = new FileParameter(this, "Log directory");
        // In Windows, this should map to C:\temp\, /home/tmp/ in Linux.
        logDirectory.setExpression("$HOME/temp/ConstraintFiles");

        trainedConstraintDirectory = new FileParameter(this,
                "Trained constraint directory");
        trainedConstraintDirectory
                .setExpression("$CLASSPATH/trainedConstraints");

        _addChoices();

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"120\" height=\"40\" "
                + "style=\"fill:blue\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\nResolve Properties</text></svg>");

        /* Removed to make compile
         * --Ben on 12/04/2009
         * 
         * Added back in to provide double-click property resolution
         * GUI operation for OntologySolver.
         * 12/21/09 Charles Shelton
         */
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

    /** Holds the value of the directory location for the log file for the OntologySolver. */
    public FileParameter logDirectory;

    /** 
     * Holds the value of the logMode. "true" to enable logging for the
     * OntologySolver and "false" to disable logging.
     */
    public Parameter logMode;

    /**
     * Indicate whether to compute the least or greatest fixed point solution.
     * <ul>
     * <li> "least" Solve for least fixed point
     * <li> "greatest" Solve for greatest fixed point
     * </ul>
     */
    public StringParameter solvingFixedPoint;

    /**
     * Holds the value of the directory locatino for the trained constraints
     * for regression tests for the OntologySolver resolution.
     */
    public FileParameter trainedConstraintDirectory;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Add the specified object to the hash table of manually annotated
     * objects in the model.
     * 
     * @param object The java Object to be added to the hash table
     */
    public void addAnnotated(Object object) {
        _annotatedObjects.add(object);
    }

    /**
     * React to a change in an attribute.
     * @param attribute The attribute that changed.
     * @exception IllegalActionException If the change is not acceptable to this
     * container (not thrown in this class).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == logMode) {
            _logMode = logMode.getToken() == BooleanToken.TRUE;
        }
        super.attributeChanged(attribute);
    }

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
        LatticeOntologyAdapter toplevelHelper = (LatticeOntologyAdapter) getAdapter(toplevel);

        toplevelHelper.reinitialize();

        toplevelHelper
                ._addDefaultConstraints(_getConstraintType(actorConstraintType
                        .stringValue()));

        // FIXME: have to generate the connection every time
        // because the model structure can changed.
        // (i.e. adding or removing connections.)
        toplevelHelper._setConnectionConstraintType(
                _getConstraintType(connectionConstraintType.stringValue()),
                _getConstraintType(compositeConnectionConstraintType
                        .stringValue()), _getConstraintType(fsmConstraintType
                        .stringValue()),
                _getConstraintType(expressionASTNodeConstraintType
                        .stringValue()));
        // Collect the constraints in a list

        List<Inequality> constraintList = toplevelHelper.constraintList();

        getStats().put("# of generated constraints", constraintList.size());
        getStats()
                .put("# of concept terms", _conceptTermManager.terms().size());

        String initialSolverStats = _getStatsAsString("\t");
        String initialSolverConstraints = _getConstraintsAsLogFileString(
                constraintList, "I");

        Hashtable initialSolverInfo = new Hashtable();
        initialSolverInfo.put("initialSolverStats", initialSolverStats);
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
                resolveProperties(null, true);
            } catch (KernelException kernelEx) {
                throw new IllegalActionException(this, kernelEx,
                        "Error while trying to execute LatticeOntologySolver "
                                + getName() + " resolution algorithm: "
                                + kernelEx);
            }
        }

        String resolvedSolverStats = _getStatsAsString("\t");
        String resolvedSolverConstraints = _getConstraintsAsLogFileString(
                _resolvedConstraintList, "R");

        Hashtable resolvedSolverInfo = new Hashtable();
        resolvedSolverInfo.put("resolvedSolverStats", resolvedSolverStats);
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
     * Return true if the OntologySolver log mode is enabled, false otherwise.
     * 
     * @return true if the OntologySolver log mode is enabled, false otherwise
     */
    public Boolean isLogMode() {
        return _logMode;
    }

    /**
     * Reset the solver. This removes the internal states of the solver (e.g.
     * previously recorded properties, statistics, etc.). Also resets the
     * {@linkplain ConceptTermManager} to null and clears the trained constraints.
     */
    public void reset() {
        super.reset();
        _conceptTermManager = null;
        _trainedConstraints.clear();
    }

    /**
     * Set the log mode for the OntologySolver. True enables the log mode
     * and false disables the log mode.
     * 
     * @param isLogMode Boolean value to set the log mode
     */
    public void setLogMode(boolean isLogMode) {
        _logMode = isLogMode;
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
     * Update the model objects with their associated concepts.
     * 
     * @exception IllegalActionException If the model objects cannot be updated with their concepts.
     */
    public void updateProperties() throws IllegalActionException {
        super.updateProperties();

        // Only need to look at the constraints of the top level adapter.
        OntologyAdapter adapter;
        adapter = getAdapter(_toplevel());

        if (isLogMode()) {
            String constraintFilename = _getTrainedConstraintFilename()
                    + "_resolved.txt";

            // Populate the _trainedConstraints list.
            _readConstraintFile(constraintFilename);

            // Match and remove from the list.
            _regressionTestConstraints((LatticeOntologyAdapter) adapter);

            // Check if there are unmatched constraints.
            _checkMissingConstraints();
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
     * Initialize solver algorithm execution statistics Map for OntologySolver.
     * Adds constraint statistics for LatticeOntologySolver in addition to the
     * default set of OntologySolver statistics.
     */
    protected void _initializeStatistics() {
        super._initializeStatistics();
        getStats().put("# of default constraints", 0);
        getStats().put("# of composite default constraints", 0);
        getStats().put("# of atomic actor default constraints", 0);
        getStats().put("# of AST default constraints", 0);
        getStats().put("# of generated constraints", 0);
        getStats().put("# of trained constraints", 0);
    }

    /**
     * Resolve the property values for the toplevel entity that contains this
     * solver, given the model analyzer that invokes this.
     * @param analyzer The given model analyzer.
     * @exception KernelException If there is an exception thrown during the OntologySolver
     * resolution
     */
    protected void _resolveProperties(NamedObj analyzer) throws KernelException {
        super._resolveProperties(analyzer);

        // Reset the list of resolved constraints before executing the ontology solver resolution. 
        _resolvedConstraintList = null;

        NamedObj toplevel = _toplevel();
        LatticeOntologyAdapter toplevelHelper = (LatticeOntologyAdapter) getAdapter(toplevel);

        toplevelHelper.reinitialize();

        toplevelHelper
                ._addDefaultConstraints(_getConstraintType(actorConstraintType
                        .stringValue()));

        // FIXME: have to generate the connection every time
        // because the model structure can changed.
        // (i.e. adding or removing connections.)
        toplevelHelper._setConnectionConstraintType(
                _getConstraintType(connectionConstraintType.stringValue()),
                _getConstraintType(compositeConnectionConstraintType
                        .stringValue()), _getConstraintType(fsmConstraintType
                        .stringValue()),
                _getConstraintType(expressionASTNodeConstraintType
                        .stringValue()));

        // Collect and solve type constraints.
        List<Inequality> constraintList = toplevelHelper.constraintList();

        _resolveProperties(toplevel, toplevelHelper, constraintList);
    }

    /** Resolve the properties of the given top-level container,
     * subject to the given constraint list.
     * 
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
        Writer writer = null;

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

                //              BEGIN CHANGE Thomas, 04/10/2008
                // Collect statistics.
                getStats().put("# of generated constraints",
                        constraintList.size());
                getStats().put("# of property terms",
                        _conceptTermManager.terms().size());

                // log initial constraints to file
                File file = null;
                Date date = new Date();
                String timestamp = date.toString().replace(":", "_");
                String logFilename = getContainer().getName() + "__"
                        + getName() + "__" + timestamp.replace(" ", "_")
                        + ".txt";

                if (!logDirectory.asFile().exists()) {
                    if (!logDirectory.asFile().mkdirs()) {
                        throw new IllegalActionException(this,
                                "Failed to create \""
                                        + logDirectory.asFile()
                                                .getAbsolutePath()
                                        + "\" directory.");
                    }
                    file = FileUtilities.nameToFile(logFilename, logDirectory
                            .asFile().toURI());

                    try {
                        if (!file.exists()) {
                            if (!file.getParentFile().exists()) {
                                if (!file.getParentFile().mkdirs()) {
                                    throw new IllegalActionException(this,
                                            "Failed to create \""
                                                    + file.getParentFile()
                                                            .getAbsolutePath()
                                                    + "\" directory.");
                                }

                            }
                            if (!file.createNewFile()) {
                                throw new IllegalActionException(this,
                                        "Failed to create \""
                                                + file.getAbsolutePath()
                                                + "\".");
                            }
                        }

                        writer = new FileWriter(file);

                        writer.write(_getStatsAsString("\t"));
                        writer.write(_getConstraintsAsLogFileString(
                                constraintList, "I"));

                    } catch (IOException ex) {
                        throw new OntologyResolutionException(this, ex,
                                "Error writing to constraint log file \""
                                        + file.getAbsolutePath() + "\".");
                    }
                }

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
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                    throw new OntologyResolutionException(this, toplevel(), ex,
                            "Failed to close a file");
                }
            }
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

    /**
     * 
     */
    private void _checkMissingConstraints() {
        StringBuffer errorMessage = new StringBuffer(_eol + "Concept \""
                + getName() + "\" resolution failed." + _eol);

        boolean hasError = false;

        for (String trainedValue : _trainedConstraints) {
            errorMessage.append("    Missing constraint: \"" + trainedValue
                    + "\"." + _eol);

            hasError = true;
        }

        if (hasError) {
            getOntologySolverUtilities().addErrors(errorMessage.toString());
        }
    }

    //    /**
    //     * Return the property value associated with the given property lattice
    //     * and the given port.
    //     * @param object The given port.
    //     * @param lattice The given lattice.
    //     * @return The property value of the given port.
    //     * @exception IllegalActionException
    //     */
    //  public Concept getProperty(Object object) {
    //  ptolemy.graph.InequalityTerm term = (ptolemy.graph.InequalityTerm) getPropertyTerm(object);
    //  return (Concept) term.getValue();
    //  }

    /** Return the specified inequality constraints as a string for the log file.
     * 
     *  @param inequality The inequality constraint to be returned as a string.
     *  @param annotation FIXME: 01/28/10 Charles Shelton - This is not used anywhere in the function so I don't know why it's here.
     *  @return A string representing the inequality constraint that can be written to a log file.
     *  @exception IllegalActionException If the string cannot be formed from the inequality constraint.
     */
    private List<String> _getConstraintAsLogFileString(Inequality inequality,
            String annotation) throws IllegalActionException {
        List<String> logConstraints = new LinkedList<String>();

        String output = "";
        ptolemy.graph.InequalityTerm lesserTerm = (ptolemy.graph.InequalityTerm) inequality
                .getLesserTerm();
        ptolemy.graph.InequalityTerm greaterTerm = (ptolemy.graph.InequalityTerm) inequality
                .getGreaterTerm();

        output = inequality.getHelper().getClass().getPackage().toString()
                .replace("package ", "")
                + "\t"
                + inequality.getHelper().getClass().getSimpleName()
                + "\t"
                + _getReducedFullName(inequality.getHelper().getComponent())
                + "\t"
                + (inequality.isBase() ? "base" : "not base")
                + "\t"
                + _getConstraintLogString(lesserTerm, "")
                + "\t"
                + "<="
                + "\t"
                + _getConstraintLogString(greaterTerm, "");
        logConstraints.add(output);

        // also write variables of FunctionTerms to log-Files
        if (lesserTerm instanceof MonotonicFunction) {
            for (InequalityTerm variable : lesserTerm.getVariables()) {
                output = inequality.getHelper().getClass().getPackage()
                        .toString().replace("package ", "")
                        + "\t"
                        + inequality.getHelper().getClass().getSimpleName()
                        + "\t"
                        + _getReducedFullName(inequality.getHelper()
                                .getComponent())
                        + "\t"
                        + (inequality.isBase() ? "base" : "not base")
                        + "\t"
                        + _getConstraintLogString(
                                (ptolemy.graph.InequalityTerm) variable, "")
                        + "\t"
                        + "MFV"
                        + "\t"
                        + _getConstraintLogString(lesserTerm,
                                _getReducedFullName(inequality.getHelper()
                                        .getComponent()));
                logConstraints.add(output);
            }
            /*  FIXME: Removing chunks of code wholesale now.
             * --Ben 12/04/2009
            for (InequalityTerm constant : lesserTerm.getConstants()) {
                output = inequality.getHelper().getClass().getPackage()
                        .toString().replace("package ", "")
                        + "\t"
                        + inequality.getHelper().getClass().getSimpleName()
                        + "\t"
                        + _getReducedFullName(inequality.getHelper()
                                .getComponent())
                        + "\t"
                        + (inequality.isBase() ? "base" : "not base")
                        + "\t"
                        + _getConstraintLogString((ptolemy.graph.InequalityTerm) constant, "")
                        + "\t"
                        + "MFC"
                        + "\t"
                        + _getConstraintLogString(lesserTerm,
                                _getReducedFullName(inequality.getHelper()
                                        .getComponent()));
                logConstraints.add(output);
            }
             */
        }

        return logConstraints;
    }

    /** Return the string representation of the inequality term for one side of
     *  an inequality constraint.
     * 
     *  @param propertyTerm The inequality term for which to construct a string.
     *  @param actorName A string containing the name of the actor from which the constraint is derived.
     *  @return A string representing the inequality term.
     *  @throws IllegalActionException If the string cannot be formed from the inequality term.
     */
    private String _getConstraintLogString(
            ptolemy.graph.InequalityTerm propertyTerm, String actorName)
            throws IllegalActionException {
        if (propertyTerm instanceof Concept) {
            // FIXME: This is bogus unreadable syntax. "eff" means "effective"
            // (whatever that means).
            return "eff" + "\t" + "\t"
                    + propertyTerm.getClass().getSuperclass().getSimpleName()
                    + "\t" + propertyTerm.toString() + "\t"
                    + propertyTerm.getValue();
        } else if (propertyTerm instanceof MonotonicFunction) {
            return "eff"
                    + "\t"
                    + actorName
                    + "\t"
                    + propertyTerm.getClass().getSuperclass().getSimpleName()
                    + "\t"
                    + propertyTerm.getClass().toString()
                            .substring(
                                    propertyTerm.getClass().toString()
                                            .lastIndexOf(".")) + "\t"
                    + propertyTerm.getValue();
        } else {
            Object object = propertyTerm.getAssociatedObject();
            String containerName = "";

            if (object != null) {
                if (object instanceof ASTPtRootNode) {
                    try {
                        if (((ASTPtRootNode) object).jjtGetParent() != null
                                && !(((ASTPtRootNode) object).jjtGetParent() instanceof ASTPtAssignmentNode)) {
                            containerName = _getReducedFullName(((ASTPtRootNode) object)
                                    .jjtGetParent());
                        } else {
                            containerName = _getReducedFullName(getAdapter(
                                    object).getContainerEntity(
                                    (ASTPtRootNode) object));
                        }
                    } catch (IllegalActionException e) {
                        assert false;
                    }
                }
                // FIXME: effective is not implemented.
                return "eff" + "\t" + containerName + "\t"
                        + object.getClass().getSimpleName() + "\t"
                        + _getReducedFullName(object) + "\t"
                        + propertyTerm.getValue();
            } else {
                return "NO" + "\t" + "ASSOCIATED" + "\t" + "OBJECT";
            }
        }

    }
    
    /** Return a string representing the list of inequality constraints specified
     *  that can be written to a log file.
     * 
     *  @param constraintList The list of inequality constraints to be parsed into a string.
     *  @param annotation FIXME: 01/28/10 Charles Shelton - This is not used anywhere in the function so I don't know why it's here.
     *  @return A string representing the list of inequality constraints that can be written to a log file.
     *  @throws IllegalActionException If the string cannot be formed from the list of inequality constraints.
     */
    private String _getConstraintsAsLogFileString(
            List<Inequality> constraintList, String annotation)
            throws IllegalActionException {

        StringBuffer output = new StringBuffer();
        for (Inequality inequality : constraintList) {
            output.append(_getConstraintAsLogFileString(inequality, annotation)
                    + _eol);
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

    /** Get the reduced full name of the model object in the Ptolemy model.
     * 
     *  @param object The model object whose reduced full name we want to get.
     *  @return A string representing the reduced full name.
     */
    private String _getReducedFullName(Object object) {
        if (object instanceof NamedObj) {
            String name = ((NamedObj) object).getFullName();
            if (name.indexOf(".", 2) > 0) {
                name = name.substring(name.indexOf(".", 2));
            } else {
                name = "topLevelComposite";
            }
            return name;
        } else {
            return object.toString();
        }
    }

    /**
     * Return the trained constraint filename.
     * @return The trained constraint filename.
     * @exception IllegalActionException If there is a problem getting the name
     * of the top level or the value of the <i>trainedConstraintDirectory</i>
     * parameter.
     */
    private String _getTrainedConstraintFilename()
            throws IllegalActionException {
        // Make an unique file name from the toplevel container.
        // FIXME: don't use __, they make the filenames too long.
        String constraintFilename = _toplevel().getName() + "__" + getName();

        String directoryPath = trainedConstraintDirectory.getExpression();
        directoryPath = directoryPath.replace("\\", "/");
        directoryPath += directoryPath.endsWith("/")
                || directoryPath.endsWith("\\") ? "" : "/";

        File constraintFile;
        if (directoryPath.startsWith("$CLASSPATH")) {

            // FIXME: for a cloned URIAttribute, the URI is not set.
            URI directory = new File(URIAttribute.getModelURI(this))
                    .getParentFile().toURI();

            constraintFile = FileUtilities.nameToFile(directoryPath
                    .substring(11)
                    + constraintFilename, directory);

        } else {
            if (!trainedConstraintDirectory.asFile().exists()) {
                if (!trainedConstraintDirectory.asFile().mkdirs()) {
                    throw new IllegalActionException(this,
                            "Failed to create \""
                                    + trainedConstraintDirectory.asFile()
                                            .getAbsolutePath()
                                    + "\" directory.");
                }
            }
            constraintFile = FileUtilities.nameToFile(constraintFilename,
                    trainedConstraintDirectory.asFile().toURI());
        }
        return constraintFile.getAbsolutePath().replace("\\", "/").replaceAll(
                "%5c", "/");
    }

    
    /** Read in the constraint file that contains the list of trained constraints
     *  for the model for this LatticeOntologySolver.
     * 
     *  @param filename The filename referring to the constraint file.
     */
    private void _readConstraintFile(String filename) {

        File file = new File(filename);

        try {
            BufferedReader reader = null;

            try {
                reader = new BufferedReader(new FileReader(file));

                String line = reader.readLine();
                while (line != null) {
                    _trainedConstraints.add(line);
                    line = reader.readLine();
                }

                getStats().put("# of trained constraints",
                        _trainedConstraints.size());
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        } catch (IOException ex) {
            /* FIXME: Removed to make compile
             * --Ben 12/04/2009
            throw new PropertyFailedRegressionTestException(this,
                    "Failed to open or read the constraint file \"" + filename
                            + "\".");
            */
        }
    }

    
    /** FIXME: 01/27/10 Charles Shelton - I don't know what this method does or is used for.
     * 
     * @param adapter The LatticeOntologyAdapter from which to get the constraints.
     * @throws IllegalActionException If an exception is thrown when getting the component
     *  referred to from the adapter.
     */
    private void _regressionTestConstraints(LatticeOntologyAdapter adapter)
            throws IllegalActionException {
        Object object = adapter.getComponent();
        if (!(object instanceof NamedObj)) {
            return;
        }
        //NamedObj namedObj = (NamedObj) object;
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

        /** Represents that the sink must be > the source. */
        SINK_GREATER,

        /** Represents that the source must be >= the sink. */
        SRC_EQUALS_GREATER,

        /** Represents that the source must == the least upper bound of all sinks. */
        SRC_EQUALS_MEET,

        /** Represents that the source must be > the sink. */
        SRC_GREATER
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

    /** Boolean value that determines whether or not the LatticeOnologySolver is in log mode. */
    private boolean _logMode;

    /** The concept term manager that keeps track of all the concept terms in the model for the LatticeOntologySolver. */
    private ConceptTermManager _conceptTermManager;

    /** The list of constraints after the ontology resolution algorithm has executed. */
    private List<Inequality> _resolvedConstraintList;

    /**
     * The set of trained constraints. This set is populated from parsing the
     * constraint file when training mode is off.
     */
    private final List<String> _trainedConstraints = new LinkedList<String>();

    /**
     * The string that identifies whether the OntologySolver should use
     * a user-defined lattice.  This is obsolete and should be removed.
     */
    protected static final String _USER_DEFINED_LATTICE = "Attribute::";

}
