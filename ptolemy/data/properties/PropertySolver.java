package ptolemy.data.properties;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ptolemy.actor.IOPort;
import ptolemy.actor.parameters.SharedParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.Node;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.Variable;
import ptolemy.data.properties.lattice.PropertyConstraintAttribute;
import ptolemy.data.properties.token.PropertyTokenAttribute;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.VersionAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

public abstract class PropertySolver extends Attribute {

    public PropertySolver(NamedObj container, String name) throws IllegalActionException, NameDuplicationException {
        super(container, name);

        action = new SharedParameter(
                this, "action", PropertySolver.class, TRAINING);
        action.setStringMode(true);
        _addActions(action);

        sharedUtilitiesWrapper = new SharedParameter(
                this, "sharedUtilitiesWrapper", PropertySolver.class);
        sharedUtilitiesWrapper.setPersistent(false);
        sharedUtilitiesWrapper.setVisibility(Settable.NONE);

        // **We can only create a new shared utilities object
        // only once per model.
        if (sharedUtilitiesWrapper.getExpression().length() == 0) {
            sharedUtilitiesWrapper.setToken(new ObjectToken(new SharedUtilities()));
        }

        Collection<SharedParameter> parameters = sharedUtilitiesWrapper.sharedParameterSet();
        for (SharedParameter parameter : parameters) {
            parameters = parameter.sharedParameterSet();
        }


        _sharedUtilities = (SharedUtilities) ((ObjectToken) 
                sharedUtilitiesWrapper.getToken()).getValue();

        _highlighter = new PropertyHighlighter(this, "PropertyHighlighter");

        manualAnnotation = new Parameter(this, "manualAnnotation", BooleanToken.FALSE);
        manualAnnotation.setTypeEquals(BaseType.BOOLEAN);

        all = new SharedParameter(this, "all", PropertySolver.class, "false");
        all.setTypeEquals(BaseType.BOOLEAN);
    }

    public Parameter action;
    public Parameter all;
    public Parameter manualAnnotation;

    public SharedParameter sharedUtilitiesWrapper;

    public void addErrors(String error) {
        _sharedUtilities.addErrors(error);
    }

    /** React to a change in an attribute. 
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this class).
     */
    public void attributeChanged(Attribute attribute)
    throws IllegalActionException {

        super.attributeChanged(attribute);
    }

    /**
     * Check if there is any regression testing errors after resolving
     * properties. If so, throw a new PropertyFailedRegressionTestException
     * with an error message that includes all the properties that does not
     * match the regression test values. 
     * @throws PropertyFailedRegressionTestException Thrown if there is any
     *  errors in the regression test.
     */
    public void checkErrors() 
    throws PropertyResolutionException {

        // FIXME: remove the errors as well.

        List errors = _sharedUtilities.removeErrors();

        if (!errors.isEmpty()) {
            String errorMessage = errors.toString();

            if (isTesting()) {
                throw new PropertyFailedRegressionTestException(
                        this, errorMessage);
            } else {
                throw new PropertyResolutionException(
                        this, errorMessage);
            }
        }
    }

    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        PropertySolver newObject = (PropertySolver) super.clone(workspace);
        return newObject;
    }

    public void checkResolutionErrors() throws IllegalActionException {
        for (Object propertyable : getAllPropertyables()) {
            _recordUnacceptableSolution(
                    propertyable, getProperty(propertyable));
        }
        checkErrors();
    }

    /**
     * Clear the manual annotation constraints assoicated with
     * this solver use-case.
     * @exception IllegalActionException Not Thrown.
     */
    public void clearAnnotations() throws IllegalActionException {

        for (PropertyHelper helper : getAllHelpers()) {
            if (helper.getComponent() instanceof NamedObj) {
                NamedObj namedObj = (NamedObj) helper.getComponent();

                for (AnnotationAttribute attribute : (List<AnnotationAttribute>)
                        namedObj.attributeList(AnnotationAttribute.class)) {

                    if (isIdentifiable(attribute.getUseCaseIdentifier())) {

                        try {
                            attribute.setContainer(null);
                        } catch (NameDuplicationException e) {
                            assert false;
                        }
                    }
                }
            }
        }
        _repaintGUI();
    }

    /**
     * Clear the display properties.
     */
    public void clearDisplay() {
        _highlighter.clearDisplay();
    }

    /**
     * Clear the resolved properties for this solver. This goes through
     * all property-able objects and removes the property attributes 
     * that has the extended use-case name of this solver. 
     * @exception IllegalActionException Not Thrown.
     */
    public void clearProperties() throws IllegalActionException {
        _resolvedProperties.clear();

        try {
            for (Object propertyable : getAllPropertyables()) {
                if (propertyable instanceof NamedObj) {
                    NamedObj namedObj = (NamedObj) propertyable;

                    PropertyAttribute attribute = (PropertyAttribute)
                    namedObj.getAttribute(getExtendedUseCaseName());

                    if (attribute != null) {
                        attribute.setContainer(null);
                    }
                }
            }

            Attribute trainedException = 
                getTrainedExceptionAttribute();

            if (trainedException != null) {
                trainedException.setContainer(null);
            }

        } catch (NameDuplicationException e1) {
            assert false;
        }
        _repaintGUI();
    }

    public void displayProperties() throws IllegalActionException {
        // Do nothing if we are not in a mode that allows display.
        if (!(isResolve() || isView())) {
            return;
        }
        Iterator propertyables = getAllPropertyables().iterator();
        while (propertyables.hasNext()) {
            Object propertyableObject = propertyables.next();

            if (propertyableObject instanceof NamedObj) {
                NamedObj namedObj = (NamedObj) propertyableObject;

                Property property = getResolvedProperty(namedObj, false);

                _highlighter.showProperty(namedObj, property);
                _highlighter.highlightProperty(namedObj, property);
            }
        }
        _repaintGUI();
    }

    /**
     * Find a solver that is associated with the given label. There 
     * can be more than one solvers with the label. This method returns 
     * whichever it finds first.
     * @param identifier The given label. 
     * @return The property solver associated with the given label.
     * @throws IllegalActionException Thrown if no matched solver
     *  is found.
     */
    public PropertySolver findSolver(String identifier) 
    throws PropertyResolutionException {

        for (PropertySolver solver : getAllSolvers(sharedUtilitiesWrapper)) {
            if (solver.getUseCaseName().equals(identifier)) {
                return solver;
            }
            if (solver.getClass().getSimpleName().equals(identifier)) {
                return solver;
            }
            if (solver.getName().equals(identifier)) {
                return solver;
            }
        }

        throw new PropertyResolutionException(this,
                "Cannot find \"" + identifier + "\" solver.");
    }

    public abstract String getExtendedUseCaseName();

    /**
     * Return the property helper for the given object. 
     * @param object The given object.
     * @return The property helper for the object.
     * @throws IllegalActionException Thrown if the helper cannot
     *  be found or instantiated.
     */
    public abstract PropertyHelper getHelper(Object object) 
    throws IllegalActionException;

    /**
     * @param attribute The given attribute.
     * @return The root ast node associated with the given attribute.
     * @throws IllegalActionException
     */
    public ASTPtRootNode getParseTree(Attribute attribute) throws IllegalActionException {
        Map<Attribute, ASTPtRootNode> parseTrees = 
            getSharedUtilities().getParseTrees();

        if (!parseTrees.containsKey(attribute)) {

            String expression = ((Settable) attribute).getExpression().trim();

            if (expression.length() == 0) {
                return null;
            }

            ASTPtRootNode parseTree;
            //          if ((attribute instanceof StringAttribute) || 
            //          ((attribute instanceof Variable 
            //          && ((Variable) attribute).isStringMode()))) {
            if ((attribute instanceof Variable) 
                    && ((Variable) attribute).isStringMode()) {

                parseTree = getParser().generateStringParseTree(expression);

            } else {
                parseTree = getParser().generateParseTree(expression);
            }

            parseTrees.put(attribute, parseTree);
            getSharedUtilities().putAttribute(parseTree, attribute);
        }
        return parseTrees.get(attribute);
    }

    public abstract String getUseCaseName();

    public void highlightProperties() throws IllegalActionException {
        _highlighter.highlightProperties();        
        _repaintGUI();
    }

    public static void main(String[] args) throws Exception {
        //testProperties(args);
        testPropertiesAndGenerateReports(args[0]);
    }

    /** Parse a command-line argument. This method recognized -help
     *  and -version command-line arguments, and prints usage or
     *  version information. No other command-line arguments are
     *  recognized.
     *  @param arg The command-line argument to be parsed.
     *  @return True if the argument is understood, false otherwise.
     *  @exception Exception If something goes wrong.
     */
    public static boolean parseArg(String arg) throws Exception {
        if (arg.equals("-help")) {
            // TODO: _usage()??
            //System.out.println(_usage());

            StringUtilities.exit(0);
            // If we are testing, and ptolemy.ptII.exitAfterWrapup is set
            // then StringUtilities.exit(0) might not actually exit.
            return true;
        } else if (arg.equals("-version")) {
            System.out.println("Version "
                    + VersionAttribute.CURRENT_VERSION.getExpression()
                    + ", Build $Id$");

            StringUtilities.exit(0);
            // If we are testing, and ptolemy.ptII.exitAfterWrapup is set
            // then StringUtilities.exit(0) might not actually exit.
            return true;
        } 
        // Argument not recognized.
        return false;
    }

    public boolean isClear() {
        return action.getExpression().equals(PropertySolver.CLEAR);
    }

    public void reset() {
        _declaredProperties = new HashMap<Object, Property>();
        _resolvedProperties = new HashMap<Object, Property>();
        _nonSettables = new HashSet<Object>();
        _previousProperties = new HashMap<Object, Property>();
        _helperStore = new HashMap<Object, PropertyHelper>();
        _stats = new TreeMap<Object, Object>();

    }

    /**
     * Resolve the property values for the top-level entity that contains
     * the solver.
     * @param analyzer The model analyzer that invokes the solver. However, 
     *  this is null if the solver is invoked directly from its GUI.
     * @param isInvoked Whether the solver is directly invoked or activated
     *  through solver dependencies.
     * @return True if resolution succeeds as expected; Otherwise, false.
     * @throws IllegalActionException TODO
     */
    public boolean resolveProperties(ModelAnalyzer analyzer, boolean isInvoked)
    throws KernelException {

        boolean success = true;

        boolean noException = true;

        try {

            getSharedUtilities().addRanSolvers(this);

            _analyzer = analyzer;
            _isInvoked = isInvoked;        

            // Clear the resolved properties for the chosen solver.
            String actionValue = action.getExpression();
            if (actionValue.equals(CLEAR_ANNOTATION)) {
                if (isInvoked) {
                    clearAnnotations();
                }
                return true;
            } else if (actionValue.equals(CLEAR)) {
                if (isInvoked) {
                    clearProperties();
                    clearDisplay();
                }
                return true;

            } else if (actionValue.equals(VIEW)) {
                if (isInvoked) {
                    clearDisplay();
                    showProperties();
                    highlightProperties();        
                }
                return true;

            } 


            // If this is not an intermediate (invoked) solver, 
            // we need to clear the display.
            if (isInvoked && isResolve()) {
                PropertySolver previousSolver = 
                    _sharedUtilities._previousInvokedSolver;

                // Clear the display properties of the previous invoked solver.
                // If no solver is invoked previously, at least clear
                // the previous highlighting for this solver.
                if (previousSolver == null) {
                    previousSolver = this;
                }
                previousSolver.clearDisplay();

                _sharedUtilities._previousInvokedSolver = this;

                // If we are in TRAINING mode, then keep
                // all the intermediate results.
                boolean keepIntermediates = 
                    //actionValue.equals(ANNOTATE_ALL) || 
                    ((BooleanToken)all.getToken()).booleanValue() ||
                    actionValue.equals(TRAINING);

                for (String solverName : _dependentSolvers) {
                    PropertySolver dependentSolver = findSolver(solverName);

                    dependentSolver.resolveProperties(
                            analyzer, keepIntermediates);

                    dependentSolver.updateProperties();
                }
            } else if (isInvoked && isTesting()) {
                for (String solverName : _dependentSolvers) {
                    PropertySolver dependentSolver = findSolver(solverName);

                    dependentSolver.resolveProperties(analyzer, false);
                }
            }
            _resolveProperties(analyzer);

            checkResolutionErrors();

        } catch (PropertyResolutionException ex) {
            noException = false;
            //     resolution exceptions. that means resolution ended prematurely.
            //     But that may not means that this is an improper behavior
            //     Check whether we are expecting an exception,
            //     if in testing mode, then add a RegressionTestErrorException
            PropertySolver failedSolver = ex.getSolver();

            // Remove '\r' characters to make Windows-Linux comparable strings.
            String trainedException = failedSolver.getTrainedException().replaceAll("\r", "");
            String exception = ex.getMessage().replaceAll("\r", "");
            if (isTesting()) {
                if (!exception.equals(trainedException)) {
                    addErrors(PropertySolver.getTrainedExceptionMismatchMessage(
                            exception, trainedException));
                }
            } else if (isResolve()) {
                if (!exception.equals(trainedException)) {

                    // ask the user if this is expected, 
                    boolean doRecord = MessageHandler.yesNoQuestion(
                            PropertySolver.getTrainedExceptionMismatchMessage(
                                    exception, trainedException) + 
                    "Do you want to record it?");

                    if (doRecord) {
                        // If so, record the exception in ex.solver.
                        failedSolver.recordTrainedException(exception);
                    } else {
                        if (isTraining()) {
                            // Don't set mode to TEST because the user
                            // did not train (record) this exception.
                            success = false;
                        }
                    }
                }
            }
        }

        if (isTesting() && noException && getTrainedException().length() > 0) {
            // if in TEST mode, if there is a previously trained 
            // RegressionTestErrorExceptionException
            // and we do not get one in the resolution, 
            // then we throw an exception.
            addErrors(PropertySolver.getTrainedExceptionMismatchMessage(
                    "", getTrainedException()));
        }			

        return success;
    }

    public String setAction (String actionString) {
        String oldAction = action.getExpression();
        action.setExpression(actionString);
        return oldAction;
    }

    public void showProperties() throws IllegalActionException {
        _highlighter.showProperties();
        _repaintGUI();
    }

    /**
     * Resolve the property values for the given top-level entity.
     * Print out the name of the this solver. Sub-classes should
     * overrides this method.
     * @param analyzer The given model analyzer.
     * @throws KernelException Not thrown in this base class.
     */
    protected void _resolveProperties(ModelAnalyzer analyzer) 
    throws KernelException {
        System.out.println("Invoking \"" + getName() + "\" (" 
                + getExtendedUseCaseName() + "):");
    }

    private void _repaintGUI() {
        requestChange(new ChangeRequest(this,
        "Repaint the GUI.") {
            protected void _execute() throws Exception {
            }
        });
    }

    /**
     * 
     * @param actionParameter
     */
    protected static void _addActions(Parameter actionParameter) {
        actionParameter.addChoice(ANNOTATE);
        actionParameter.addChoice(CLEAR);
        actionParameter.addChoice(TEST);
        actionParameter.addChoice(TRAINING);
        actionParameter.addChoice(VIEW);
        actionParameter.addChoice(CLEAR_ANNOTATION);
    }

    private void _recordUnacceptableSolution(
            Object propertyable, Property property) {

        // Check for unacceptable solution.
        if ((property != null) && 
                (!property.isAcceptableSolution())) {
            addErrors("Property \"" 
                    + property + "\" is not an acceptable solution for "
                    + propertyable + "." + _eol);
        }
    }

    /**
     * @param attribute
     * @param property
     * @throws IllegalActionException
     */
    private void _updatePropertyAttribute(PropertyAttribute attribute, Property property) 
    throws IllegalActionException {
        if (property != null) {
            // Write results to attribute
            attribute.setExpression(property.toString());

        } else {
            attribute.setExpression("");                            
        }
    }

    /**
     * Get the propertyable attribute contained by the given propertyable.
     * @param propertyable The given propertyable object.
     * @return The property attribute contained by the given propertyable.
     * @throws IllegalActionException
     */
    protected PropertyAttribute _getPropertyAttribute(NamedObj propertyable) 
    throws IllegalActionException {
        PropertyAttribute attribute = null;

        // write results to attribute
        if (getExtendedUseCaseName().startsWith("lattice")) {
            attribute = (PropertyConstraintAttribute) 
            propertyable.getAttribute(getExtendedUseCaseName());

            if (attribute == null) {
                try {
                    attribute = new PropertyConstraintAttribute(propertyable, getExtendedUseCaseName());
                } catch (NameDuplicationException e) {
                    // This shouldn't happen. If another attribute 
                    // has the same name, we should find it before.
                    assert false;
                }
            } 
        } else if (getExtendedUseCaseName().startsWith("token")) {
            attribute = (PropertyTokenAttribute) propertyable.getAttribute(getExtendedUseCaseName());
            if (attribute == null) {
                try {
                    attribute = new PropertyTokenAttribute(propertyable, getExtendedUseCaseName());
                } catch (NameDuplicationException e) {
                    // This shouldn't happen. See reason above.
                    assert false;
                }
            }
        } else {
            //FIXME: Error checking?
            throw new PropertyResolutionException(this, propertyable, 
            "Failed to get the PropertyAttribute.");
        }
        return attribute;
    }

    /**
     * Check the given property against the trained property recorded
     * on the given NamedObj. It also restore the trained property that
     * is temporarily cleared for regression testing.
     * @param namedObj The given NamedObj.
     * @param property The given resolved property.
     * @throws PropertyResolutionException Thrown if there are errors
     *  restoring the trained property.
     */
    protected void _regressionTest(NamedObj namedObj, Property property) 
    throws PropertyResolutionException {

        Property previousProperty = 
            getPreviousProperty(namedObj);

        // Restore the previous resolved property, if there exists one.
        if (previousProperty != null) {
            try {
                PropertyAttribute attribute = _getPropertyAttribute(namedObj);
                _updatePropertyAttribute(attribute, previousProperty);

            } catch (IllegalActionException ex) {
                throw new PropertyResolutionException(this, ex);
            }
        }

        // The first check is for singleton elements, and the equals()
        // comparison is necessary for "equivalent" elements, such as
        // those in the SetLattice usecase.
        if ((previousProperty == null && previousProperty != property) ||
            (previousProperty != null && !previousProperty.equals(property))) {

            addErrors(_eol + "Property \"" + getUseCaseName() + 
                    "\" resolution failed for " + namedObj.getFullName() + 
                    "." + _eol + "    Trained value: \"" +
                    previousProperty +
                    "\"; Resolved value: \"" +
                    property + "\".");
        }
    }

    /**
     * Get the name of the package that contains the class of this solver.
     * @return A string that contains the package name. 
     */
    protected String _getPackageName() {
        return getClass().getPackage().getName() + "." + getUseCaseName();
    }

    protected PropertyHelper _getHelper(Object object) throws IllegalActionException {
        if (_helperStore.containsKey(object)) {
            return (PropertyHelper) _helperStore.get(object);
        }

        if ((object instanceof IOPort) || (object instanceof Attribute)) {
            return _getHelper(((NamedObj) object).getContainer());
        }

        String packageName = _getPackageName();

        Class componentClass = object.getClass();

        Class helperClass = null;
        while (helperClass == null) {
            try {

                // FIXME: Is this the right error message?
                if (!componentClass.getName().contains("ptolemy")) {
                    throw new IllegalActionException("There is no property helper "
                            + " for " + object.getClass());
                }

                helperClass = Class.forName((componentClass.getName()
                        .replaceFirst("ptolemy", packageName))
                        .replaceFirst(".configuredSolvers.", "."));

            } catch (ClassNotFoundException e) {
                // If helper class cannot be found, search the helper class
                // for parent class instead.
                componentClass = componentClass.getSuperclass();
            }
        }

        Constructor constructor = null;
        Class solverClass = getClass();
        while (constructor == null && solverClass != null) {
            try {
                constructor = helperClass.getConstructor(
                        new Class[] { solverClass, componentClass });                

            } catch (NoSuchMethodException ex) {            
                solverClass = solverClass.getSuperclass();
            }
        }

        if (constructor == null) {
            throw new IllegalActionException(
                    "Cannot find constructor method in " 
                    + helperClass.getName());
        }

        Object helperObject = null;

        try {
            helperObject = constructor.newInstance(new Object[] { this, object });

        } catch (Exception ex) {
            throw new IllegalActionException(null, ex,
            "Failed to create the helper class for property constraints.");
        }

        if (!(helperObject instanceof PropertyHelper)) {
            throw new IllegalActionException(
                    "Cannot resolve property for this component: "
                    + object + ". Its helper class does not"
                    + " implement PropertyHelper.");
        }        
        _helperStore.put(object, (PropertyHelper)helperObject);

        return (PropertyHelper) helperObject;
    }


    public static void testPropertiesAndGenerateReports(String directoryPath) {
        try {

            Map[] stats;
            stats = _testPropertiesDirectory(directoryPath);
            _printGlobalStats(stats[0]);
            _printLocalStats(stats[1]);

        } catch (Exception ex) {
            // Force the error to show up on console.
            // We may want to direct this an error file.
            ex.printStackTrace(System.out);
        }
    }

    /**
     * 
     * @param directoryPath
     * @return
     * @throws IOException
     */
    private static Map[] _testPropertiesDirectory(String directoryPath) throws IOException {
        // Create the log directories.
        new File(_statsDirectory).mkdirs();
        new File(_exceptionLogsDirectory).mkdirs();

        // See MoMLSimpleApplication for similar code
        MoMLParser parser = new MoMLParser();
        MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());
        MoMLParser.addMoMLFilter(new RemoveGraphicalClasses());

        // FIXME: get "stdout", "nondeep" options

        HashMap<Object, Map> localStats = new LinkedHashMap<Object, Map>();
        HashMap globalStats = new LinkedHashMap();

        Map[] summary = new Map[] { globalStats, localStats };

        File directory = new File(directoryPath);

        for (File file : directory.listFiles()) {

            if (file.isDirectory()) {
                Map[] directoryOutputs = _testPropertiesDirectory(file.getAbsolutePath());

                _composeOutputs(summary[0], directoryOutputs[0]);
                _composeOutputs(summary[1], directoryOutputs[1]);

            } else if (_isTestableFile(file)) {
                System.out.println("***isTestable: " + file.getAbsolutePath());

                // Redirect System.err to a byteArrayStream.
                ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
                PrintStream errorStream = new PrintStream(byteArrayStream);
                System.setErr(errorStream);

                // FIXME: Implement option to redirect System.out to a file.
                //                String outputFilename = 
                //                    StringUtilities.getProperty("ptolemy.ptII.dir") 
                //                    + "/BOSCH_Logfiles/" + file.getName();
                //                
                //                outputFilename = outputFilename.replace(".xml", "_output.txt");
                //                File outputFile = new File(outputFilename);
                //                outputFile.createNewFile();
                //                
                //                FileOutputStream fos2 = new FileOutputStream(outputFilename);
                //                PrintStream ps2 = new PrintStream(fos2);
                //                System.setOut(ps2);

                boolean failed = false;

                String filePath = file.getAbsolutePath();

                //==========================================================
                // Record timestamp and memory usage (per testcase).
                System.gc();
                long startTime = System.currentTimeMillis();

                // Format the current time.
                SimpleDateFormat formatter
                = new SimpleDateFormat ("yyyy.MM.dd G 'at' hh:mm:ss a zzz");
                Date currentTime_1 = new Date(startTime);
                String startTimeString = formatter.format(currentTime_1);

                _addLocalStatsEntry(localStats, _createKey(filePath, null, null), "Start time ", startTimeString);
                _addLocalStatsEntry(localStats, _createKey(filePath, null, null), "Memory usage before", Runtime.getRuntime().totalMemory());

                //==========================================================

                try {
                    parser.reset();
                    MoMLParser.purgeModelRecord(filePath);
                    CompositeEntity toplevel = _getModel(filePath, parser);		

                    // Get all instances of PropertySolver contained in the model.
                    // FIXME: This only gets solvers in the top-level.
                    List<PropertySolver> solvers = 
                        toplevel.attributeList(PropertySolver.class);

                    // There is no PropertySolver in the model. 
                    if (solvers.size() == 0) {
                        System.err.println("The model does not contain a solver.");
                    } 

                    for (PropertySolver solver : solvers) {
                        if (solver.isTesting()) {
                            // FIXME:
                            //solver._prepareForTesting(options);                                
                            failed &= solver.invokeSolver();

                            localStats.put(_createKey(filePath, solver, solver), solver._stats); 
                            _solverStatsHeaders.addAll(solver._stats.keySet());

                            for (String solverName : solver.getDependentSolvers()) {
                                PropertySolver dependentSolver = solver.findSolver(solverName);

                                localStats.put(_createKey(filePath, 
                                        dependentSolver, solver), dependentSolver._stats); 
                                _solverStatsHeaders.addAll(dependentSolver._stats.keySet());
                            }

                            solver.resetAll();

                        } else {
                            System.err.println("Warning: regression test not performed. " + 
                                    solver.getDisplayName() + " in " + filePath + 
                                    " is set to [" + solver.action.getExpression() + "] mode.");

                            failed = true;
                        }
                    }
                } catch (Exception ex) {
                    failed = true;
                    ex.printStackTrace(System.err);
                }

                //==========================================================
                // Record timestamp and memory usage (per testcase).
                long finishTime = System.currentTimeMillis();
                System.gc();
                _addLocalStatsEntry(localStats, _createKey(filePath, null, null), "Time used (ms)", finishTime - startTime);
                _addLocalStatsEntry(localStats, _createKey(filePath, null, null), "Memory usage after", Runtime.getRuntime().totalMemory());
                _addLocalStatsEntry(localStats, _createKey(filePath, null, null), "Failed?", failed);

                _incrementStats(globalStats, "#Total tests", 1);

                String errors = byteArrayStream.toString();

                if (!failed) {
                    // Should not succeed with errors.
                    assert errors.length() == 0;

                    _incrementStats(globalStats, "#Passed", 1);

                } else {
                    // Should not have a failure without error message.
                    assert errors.length() > 0;

                    _incrementStats(globalStats, "#Failed", 1);

                    File errorFile = _getExceptionLogFile(file, failed);
                    BufferedWriter writer = new BufferedWriter(new FileWriter(errorFile));
                    writer.write(errors);
                    writer.close();
                }
                //==========================================================
            }


            //		statistics log (global, **this is the compare file)
            //		    comprehensive overview (#failure, #success, #knownFailure, global)
            //		        - on top
            //		    stats field identifier (one headline)
            //		    testcase id (name, no timestamp, per model)
            //		        - each stats info (separated by [tab])

            //		    multiple invocations of (intermediate) solver are mapped to the same id
            //		        - create separate entries for these invocations

        }
        return summary;
    }

    /**
     * 
     * @param stats
     * @param key
     * @param entryHeader
     * @param entryValue
     */
    private static void _addLocalStatsEntry(
            Map<Object, Map> stats, Object key, String entryHeader, Object entryValue) {
        _modelStatsHeaders.add(entryHeader);

        Map entry;
        if (stats.containsKey(key)) {
            entry = stats.get(key);
        } else {
            entry = new HashMap();
            stats.put(key, entry);
        }
        entry.put(entryHeader, entryValue);        
    }

    private static Object _createKey(String filePath, PropertySolver solver, PropertySolver invokedSolver) {
        String key = filePath + _separator;

        if (solver != null) {
            key += solver.getName();
        }

        key += _separator;

        if (solver == null && invokedSolver == null) {
            // no solver is invoked.
        } else if (solver == invokedSolver || invokedSolver == null) {
            key += "directly invoked";
        } else {
            key += "dependent for (" + invokedSolver + ")";
        }
        return key;
    }

    /**
     * Get the exception log file for the given test model. The exception log
     * filename reflects whether the test has failed or not. For example,
     * a test model named "model.xml" may have a corresponding exception
     * file named "Failed_errors_model.log". If a file with the same name
     * already existed, a suffix number is attached. This occurs when 
     * logs generated by previous runs of the test script are not removed,
     * or there are multiple model files with the same name with under
     * different directories. 
     * @param modelFile The given test model file.
     * @param failed Indicate whether the test had failed or not.
     * @return The exception log file that did not previously exist. 
     */
    private static File _getExceptionLogFile(File modelFile, boolean failed) {
        int suffixId = 0;

        File errorFile;

        do {
            String exceptionLogFilename = _exceptionLogsDirectory + "/" +
            (failed ? "Failed_errors_" : "Passed_errors_") + modelFile.getName();

            // Replace the extension.
            exceptionLogFilename = exceptionLogFilename.substring(0, exceptionLogFilename.length() - 4);

            // Duplicate filenames (under different directories) are handled by
            // appending a serial suffix. We assume the file content would
            // specify the path to the model file.
            if (suffixId == 0) {
                errorFile = new File(exceptionLogFilename + ".log");
            } else {
                errorFile = new File(exceptionLogFilename + suffixId + ".log");
            }

            suffixId++;
        } while (errorFile.exists());

        return errorFile;
    }

    /** The directory path to store the test statistics reports. */
    private static String _statsDirectory = StringUtilities.getProperty("ptolemy.ptII.dir") + "/propertiesLogfiles";

    /** The directory path to store the exception log files. */
    private static String _exceptionLogsDirectory = _statsDirectory + "/exceptionLogs";

    /** The file path for the overview report file. */ 
    private static String _statsFilename = _statsDirectory + "/propertyTestReports.tsv";

    private static void _printLocalStats(Map<Object, Map> stats) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(_statsFilename), true));

        // Give ordering to the header fields.
        List headers = new LinkedList(_modelStatsHeaders);
        headers.addAll(_solverStatsHeaders);

        // Print the header row.
        writer.append("Filename" + _separator);
        writer.append("Solver" + _separator);
        writer.append("Invocation");
        for (Object header : headers) {
            writer.append(_separator + header.toString());
        }
        writer.newLine();

        // Iterate using triplet keys {testFile, solver, isInvoked}.
        for (Object key : stats.keySet()) {
            Map entry = stats.get(key);
            writer.append(key.toString());                    

            for (Object header : headers) {
                writer.append(_separator);
                if (entry.containsKey(header)) {
                    writer.append(entry.get(header).toString());                    
                }
            }
            writer.newLine();
        }
        writer.close();
    }

    private static void _printGlobalStats(Map stats) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(_statsFilename)));
        for (Object field : stats.keySet()) {
            writer.append(field + _separator + stats.get(field));
            writer.newLine();
        }
        writer.close();
    }

    private static LinkedHashSet _solverStatsHeaders = new LinkedHashSet();
    private static LinkedHashSet _modelStatsHeaders = new LinkedHashSet();

    private static void _composeOutputs(Map summary, Map intermediateOutputs) {
        for (Object field : intermediateOutputs.keySet()) {
            Object value = intermediateOutputs.get(field);
            if (value instanceof Number) {
                _incrementStats(summary, field, (Number) value);
            } else if (value instanceof Map) {
                summary.put(field, value);
            }
        }
    }

    private static boolean _isTestableFile(File file) {
        if (!file.getName().endsWith(".xml")) {
            return false;
        }
        return _isTestableDirectory(file.getParentFile());
    }

    private static boolean _isTestableDirectory(File file) {
        if (!file.isDirectory()) {
            return false;
        }

        List directoryPath = Arrays.asList(
                file.getAbsolutePath().split(File.separator.replace("\\", "\\\\")));

        return 
        directoryPath.contains("test") ||
        directoryPath.contains("demo");
    }

    /** Resolve properties for a model.
     *  @param args An array of Strings, each element names a MoML file
     *  containing a model.
     *  @return The return value of the last subprocess that was run
     *  to compile or run the model.  Return -1 if called  with no arguments.
     *  @exception Exception If any error occurs.
     */
    public static int testProperties(String[] args) throws Exception {

        HashMap options = new HashMap();

        if (args.length == 0) {
            System.err.println("Usage: java -classpath $PTII "
                    + "ptolemy.data.properties.PropertySolver model.xml "
                    + "[model.xml . . .]" + _eol
                    + "  The arguments name MoML files containing models");
            return -1;
        }

        // See MoMLSimpleApplication for similar code
        MoMLParser parser = new MoMLParser();
        MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());
        MoMLParser.addMoMLFilter(new RemoveGraphicalClasses());

        for (int i = 0; i < args.length; i++) {

            if (parseArg(args[i])) {
                continue;
            }

            if (args[i].equals(NONDEEP_TEST_OPTION)) {
                options.put(NONDEEP_TEST_OPTION, true);
                continue;
            }
            if (args[i].trim().startsWith("-")) {
                if (i >= (args.length - 1)) {
                    throw new IllegalActionException("Cannot set "
                            + "parameter " + args[i] + " when no value is "
                            + "given.");
                }

                // Save in case this is a parameter name and value.
                //_parameterNames.add(args[i].substring(1));
                //_parameterValues.add(args[i + 1]);
                //i++;
                continue;
            }

            CompositeEntity toplevel = null;
            boolean isDone = false;
            int numberOfSolverTested = 0;

            while (!isDone) {
                long memStart, memEnd;
                PropertySolver solver = null;

                System.gc();
                memStart = Runtime.getRuntime().totalMemory();
                parser.reset();
                MoMLParser.purgeModelRecord(args[i]);
                toplevel = _getModel(args[i], parser);

                // Get all instances of PropertySolver contained in the model.
                // FIXME: This only gets solvers in the top-level.
                List solvers = toplevel.attributeList(PropertySolver.class);

                if (solvers.size() == 0) {
                    // There is no PropertySolver in the model. 
                    System.err.println("The model does not contain a solver.");

                } else if (numberOfSolverTested < solvers.size()) {
                    // Get the last PropertySolver in the list, maybe
                    // it was added last?
                    solver = (PropertySolver) solvers.get(numberOfSolverTested++);

                    if (solver.isTesting()) {
                        solver._prepareForTesting(options);                                
                        solver.invokeSolver();
                        solver.resetAll();

                    } else {
                        System.err.println("Warning: regression test not performed. " + 
                                solver.getDisplayName() + " in " + args[i] + 
                                " is set to [" + solver.action.getExpression() + "] mode.");
                    }
                } else {
                    isDone = true;
                }

                // Destroy the top level so that we avoid
                // problems with running the model after generating code
                if (toplevel != null) {
                    toplevel.setContainer(null);
                    toplevel = null;
                }

                //==========================================================
                System.gc();
                memEnd = Runtime.getRuntime().totalMemory();
                if ((memEnd - memStart) != 0) {
                    // FIXME: throw some sort of memory leak exception?
                    //                  System.out.println("Memory Usage Before PS: " + memStart);                    
                    //                  System.out.println("Memory Usage After PS: " + memEnd);
                    //                  System.out.println("Memory diff = : " + (memEnd - memStart));
                    //==========================================================

                }
            }

        }
        return 0;
    }

    /**
     * Prepare for automatic testing. In this base class, do nothing.
     */
    protected void _prepareForTesting(Map options) {
        return;
    }

    public boolean invokeSolver() {
        return invokeSolver(null);
    }

    public boolean invokeSolver(ModelAnalyzer analyzer) {
        boolean success = false;

        try {
            success = resolveProperties(analyzer, true);	            

            updateProperties();

            checkErrors();

            displayProperties();

            if (isTraining() && success) {
                setTesting();
            }

        } catch (KernelException e) {
            resetAll();
            throw new InternalErrorException(e);
        } 

        return success;
    }

    public boolean isManualAnnotate() {
        return manualAnnotation.getExpression().equals("true");
    }

    public boolean isTesting() {
        return action.getExpression().equals(PropertySolver.TEST);
    }

    public boolean isResolve() {
        return ((action.getExpression().equals(ANNOTATE)) ||
                //(action.getExpression().equals(ANNOTATE_ALL)) ||
                //(action.getExpression().equals(MANUAL_ANNOTATE)) ||
                (action.getExpression().equals(TRAINING)));
    }

    public boolean isTraining() {
        return action.getExpression().equals(TRAINING);
    }

    public boolean isView() {
        return action.getExpression().equals(PropertySolver.VIEW);
    }

    /**
     * Return true if the solver can be identified by the given
     * use-case string; otherwise, false.
     * @param usecase The given use-case label.
     * @return True if the solver can be identified by the given
     * use-case string; otherwise, false.
     */
    public boolean isIdentifiable(String usecase) {
        return usecase.equals(getName()) ||
        usecase.equals(getUseCaseName()) ||
        usecase.equals(getExtendedUseCaseName());
    }

    public boolean isSettable(Object object) {
        return !_nonSettables.contains(object);
    }

    /**
     * Increment the given field the solver statistics by a
     * given number. This is used for incrementing integer
     * type statistics. If the given field does not exist,
     * it starts the count of the field at zero.
     * @param field The given field of the solver statistics.
     * @param increment The given number to increment by.
     */
    public void incrementStats(Object field, long increment) {
        _incrementStats(_stats, field, increment);
    }

    private static void _incrementStats(Map map, Object field, Number increment) {
        Number current = (Number) map.get(field);
        if (current == null) {
            current = 0;
        }
        map.put(field, current.longValue() + increment.longValue());
    }

    /**
     * @param path
     * @param parser
     * @return
     * @throws IllegalActionException 
     */
    private static CompositeEntity _getModel(String path, MoMLParser parser) 
    throws IllegalActionException {
        // Note: the code below uses explicit try catch blocks
        // so we can provide very clear error messages about what
        // failed to the end user.  The alternative is to wrap the
        // entire body in one try/catch block and say
        // "Code generation failed for foo", which is not clear.
        URL modelURL;

        try {
            modelURL = new File(path).toURI().toURL();
        } catch (Exception ex) {
            throw new IllegalActionException(null, ex, 
                    "Could not open \"" + path + "\"");
        }

        CompositeEntity toplevel = null;

        try {
            toplevel = (CompositeEntity) parser.parse(null, modelURL);
        } catch (Exception ex) {
            throw new IllegalActionException(null, ex, 
                    "Failed to parse \"" + path + "\"");
        }
        return toplevel;
    }


    /**
     * Resolve the properties (invoked from a ModelAnalyzer).
     * @throws KernelException
     */
    public void resolveProperties(ModelAnalyzer analyzer) 
    throws KernelException {
        resolveProperties(analyzer, false);
    }

    /**
     * Resolve the properties.
     * @throws KernelException
     */
    public boolean resolveProperties(boolean isInvoked) throws KernelException {
        return resolveProperties(null, isInvoked);
    }

    public void addNonSettable(Object object) {
        _nonSettables.add(object);
    }

    /**
     * Add the given unique solver identifier to the dependency list. 
     * A dependent solver is one whose analysis result is required
     * for this solver's analysis. The dependent solvers are run in
     * order before invoking this solver.
     * @param solverName The 
     */
    public void addDependentSolver(String solverName) {
        _dependentSolvers.add(solverName);
    }

    public void clearResolvedProperty (Object object) {
        _resolvedProperties.remove(object);
    }

    /**
     * Return the resolved property for the given object. 
     * @param object The given object
     * @return The resolved property for the given object.
     */
    public Property getResolvedProperty(Object object) {
        return getResolvedProperty(object, true);
    }


    public Property getResolvedProperty(Object object, boolean resolve) {
        Property property = (Property) _resolvedProperties.get(object);

        // See if it is already resolved.
        if (property != null) {
            return property;
        }

        // Get from the PropertyAttribute in the model.
        if (object instanceof NamedObj) {
            PropertyAttribute attribute = 
                (PropertyAttribute) ((NamedObj) object)
                .getAttribute(getExtendedUseCaseName());

            if ((attribute != null) && (attribute.getProperty() != null)) {
                return attribute.getProperty();
            }            
        }

        // Try resolve the property.
        try {
            if (resolve && !getSharedUtilities().getRanSolvers().contains(this)) {
                resolveProperties(_analyzer);
                //getSharedUtilities().addRanSolvers(this);
            }
        } catch (KernelException ex) {
            throw new InternalErrorException(
                    KernelException.stackTraceToString(ex));
        }

        return (Property) _resolvedProperties.get(object);
    }

    /**
     * Return the declared property for the given object. 
     * @param object The given object
     * @return The declared property for the given object.
     */
    public Property getDeclaredProperty(Object object) {
        return (Property) _declaredProperties.get(object);
    }

    /**
     * Return the previous resolved property for the given object. 
     * @param object The given object.
     * @return The previous resolved property for the given object.
     */
    public Property getPreviousProperty(Object object) {
        return (Property) _previousProperties.get(object);
    }

    /**
     * Get the attribute that corresponds to the given root ast node.
     * This assumes that the correspondence is recorded previously
     * through calling PropertyHelper.putAttribute(ASTPtRootNode, Attribute).
     * @param node The given ast node.
     * @return The attribute associated with the given ast node.
     * @throws IllegalActionException 
     */
    public Attribute getAttribute(ASTPtRootNode node) {
        Node root = node;
        Map<ASTPtRootNode, Attribute> attributes = 
            getSharedUtilities().getAttributes();

        while (root.jjtGetParent() != null) {
            if (attributes.containsKey(root)) {
                return attributes.get(root);
            }
            root = root.jjtGetParent();
        }

        if (!attributes.containsKey(root)) {
            throw new AssertionError(node.toString() +
            " does not have a corresponding attribute.");
        }

        return attributes.get(root);
    }

    /**
     * Return the property value associated with the given object.  
     * @param object The given object.
     * @return The property of the given object. 
     */
    public Property getProperty(Object object) {
        return getResolvedProperty(object);
    }

    /**
     * The PropertyHighlighter that controls the property visualization.
     */
    protected PropertyHighlighter _highlighter;

    /** 
     * The mapping between property-able objects and their
     * declare property. 
     */
    private HashMap<Object, Property> _declaredProperties = new HashMap<Object, Property>();

    /** 
     * The mapping between ports and their property values.
     * Each mapping is of the form (IOPort, Property). 
     */
    private HashMap<Object, Property> _resolvedProperties = new HashMap<Object, Property>();

    /**
     * The set of propertyable objects that are marked non-settable.
     * A non-settable propertyable object results from setting an object
     * to a fixed property through PropertyHelper.setEquals().
     */
    private HashSet<Object> _nonSettables = new HashSet<Object>();

    private HashMap<Object, Property> _previousProperties = new HashMap<Object, Property>();

    /** A hash map that stores the code generator helpers associated
     *  with the actors.
     */
    protected HashMap<Object, PropertyHelper> _helperStore = new HashMap<Object, PropertyHelper>();

    protected Map<Object, Object> _stats = new LinkedHashMap<Object, Object>();

    /**
     * @return The expression parser shared by all solvers.
     */
    public static PtParser getParser() {
        if (_parser == null) {
            _parser = new PtParser();
        }
        return _parser;
    }

    /**
     * @return The SharedUtilities object. 
     */
    public SharedUtilities getSharedUtilities() {
        return _sharedUtilities; 
    }

    /**
     * Record tracing statistics.
     * @throws IllegalActionException 
     */
    protected void _addStatistics() throws IllegalActionException {
        _stats.put("# of helpers", _helperStore.size());
        _stats.put("# of propertyables", getAllPropertyables().size());
        _stats.put("# of resolved properties", _resolvedProperties.size());
        _stats.put("# of resolution errors", _sharedUtilities.getErrors().size());
        _stats.put("has trained resolution errors", getTrainedException().length() > 0);
    }

    /**
     * Return the string representation of the recorded statistics.
     * @param separator The delimiter to separate the statistics fields.
     * @return The string representation of the recorded statistics.
     */
    protected String _getStatsAsString(String separator) {
        StringBuffer result = new StringBuffer();
        for (Object field : _stats.keySet()) {
            result.append(field + separator + _stats.get(field) + _eol);
        }
        return result.toString();
    }

    private static PtParser _parser;

    /**
     * Return the list of all PropertyHelpers.
     * @return The list of all PropertyHelper.
     */
    public List<PropertyHelper> getAllHelpers() {
        NamedObj topLevel = toplevel();
        List<PropertyHelper> result = new LinkedList<PropertyHelper>();
        List<PropertyHelper> subHelpers = new LinkedList<PropertyHelper>();

        try {
            result.add(getHelper(topLevel));
            subHelpers.add(getHelper(topLevel));

            while (!subHelpers.isEmpty()) {
                PropertyHelper helper = subHelpers.remove(0);
                subHelpers.addAll(helper._getSubHelpers());
                result.add(helper);
            }
        } catch (IllegalActionException e) {
            assert false;
        }

        return result;
    }

    /**
     * Return the list of all propertyable objects.
     * @return The list of all propertyable objects.
     * @throws IllegalActionException 
     */
    public List getAllPropertyables() throws IllegalActionException {
        List result = new LinkedList();

        for (PropertyHelper helper : getAllHelpers()) {
            result.addAll(helper.getPropertyables());
        }
        return result;
    }

    /**
     * The list of all solvers that shared the same utility object 
     * wrapped by the given SharedParameter (e.g. all solvers from
     * the same model).
     * @param sharedParameter The given SharedParameter that wraps a
     *  shared utility object.
     * @return A list of solvers.
     */
    public static List<PropertySolver> getAllSolvers(SharedParameter sharedParameter) {
        List<NamedObj> parameters = new ArrayList<NamedObj>(sharedParameter.sharedParameterSet());
        List<PropertySolver>  solvers= new LinkedList<PropertySolver>();
        for (NamedObj parameter : parameters) {
            Object container = parameter.getContainer();
            if (container instanceof PropertySolver) {
                solvers.add((PropertySolver) container);
            }
        }
        return solvers;        
    }

    protected static String _eol = StringUtilities.getProperty("line.separator");

    protected static String _separator = "\t";

    protected boolean _isInvoked;

    protected ModelAnalyzer _analyzer = null;

    /**
     * The list that keeps track of the dependencies on other solvers.
     * Circular dependencies are not allowed but it is up to the user
     * to enforce this requirement. This means that there should not
     * be a case where two solvers exist in each other's dependency list. 
     */
    private List<String> _dependentSolvers = new LinkedList<String>();

    private SharedUtilities _sharedUtilities;

    private static String _TRAINED_EXCEPTION_ATTRIBUTE_NAME = "PropertyResolutionExceptionMessage";

    /** The display label for "annotate" in the action choices */
    protected static final String ANNOTATE = "ANNOTATE";

    /** The display label for "clear" in the action choices */
    protected static final String CLEAR = "CLEAR";

    /** The display label for "clear annotation" in the action choices */
    protected static final String CLEAR_ANNOTATION = "CLEAR_ANNOTATION";

    /** The display label for "test" in the action choices */
    protected static final String TEST = "TEST";

    /** The display label for "training" in the action choices */
    protected static final String TRAINING = "TRAINING";

    /** The display label for "view" in the action choices */
    protected static final String VIEW = "VIEW";

    public static final String NONDEEP_TEST_OPTION = "-nondeep"; 

    /**
     * Return the list of dependent solvers. The list contains the
     * unique name of the solvers.
     * @return The list of dependent solvers.
     */
    public List<String> getDependentSolvers() {
        return _dependentSolvers;
    }

    /**
     * Return the trained exception message string. If there is no
     * trained exception, an empty string is return.
     * @return The trained exception message string.
     */
    public String getTrainedException() {
        StringAttribute attribute = (StringAttribute)
        getAttribute(_TRAINED_EXCEPTION_ATTRIBUTE_NAME);

        if (attribute == null) {
            return "";
        } else {
            return attribute.getExpression();
        }
    }

    /**
     * Reset all internal states including utilities shared across
     * solvers in the same model. To release used memory, maps of 
     * the resolved and declared properties are cleared. The shared
     * parser used particularly by PropertySolvers is set to null. 
     * 
     */
    public void resetAll() {     
        _parser = null;
        for (PropertySolver solver : getAllSolvers(sharedUtilitiesWrapper)) {
            solver.reset();
        }
        getSharedUtilities().resetAll();        
    }

    /**
     * Record the previous property of the given object.
     * @param object The given object.
     * @param property The given property.
     */
    public void recordPreviousProperty(Object object, Property property)  {
        _previousProperties.put(object, property);
    }

    /**
     * @param exceptionMessage
     * @throws IllegalActionException
     */
    public void recordTrainedException(String exceptionMessage) throws IllegalActionException {
        StringAttribute attribute = (StringAttribute)
        getAttribute(_TRAINED_EXCEPTION_ATTRIBUTE_NAME);
        if (attribute == null) {

            try {
                attribute = new StringAttribute(
                        this, _TRAINED_EXCEPTION_ATTRIBUTE_NAME);

            } catch (NameDuplicationException e) {
                assert false;
            }
        } 
        attribute.setExpression(exceptionMessage);
    }

    /**
     * Record the declared property of the given object.
     * @param object The given object.
     * @param property The given property.
     */
    public void setDeclaredProperty(Object object, Property property)  {
        _declaredProperties.put(object, property);
    }

    /**
     * Record the resolved property of the given object.
     * @param object The given object.
     * @param property The given property.
     */
    public void setResolvedProperty(Object object, Property property) {
        _resolvedProperties.put(object, property);
    }

    /**
     * Update the property. This method is called from both invoked
     * and auxiliary solvers.
     * @throws IllegalActionException 
     * @throws IllegalActionException
     */
    public void updateProperties() throws IllegalActionException {
        if (isView() || isClear()) {
            return;
        }

        boolean hasDecided = false;
        boolean userDecision = true;

        // Only test the invoked solver.
        boolean doTest = isTesting() && _isInvoked;
        boolean doUpdate = isResolve();

        _addStatistics();

        for (Object propertyable : getAllPropertyables()) {

            if (!NamedObj.class.isInstance(propertyable)) {
                // FIXME: This happens when the propertyable is an ASTNodes,
                // or any non-Ptolemy objects. We are not updating their
                // property values, nor doing regression test for them.
                continue;
            }

            NamedObj namedObj = (NamedObj) propertyable;

            // Get the value resolved by the solver.
            Property property = getProperty(namedObj);

            if (doTest) {    // Regression testing.
                _regressionTest(namedObj, property);

            } else if (doUpdate) {
                Property previous = getPreviousProperty(namedObj);

                if (!_isInvoked && !hasDecided) {

                    // Check if the previous and resolved properties are different.
                    if ((previous == null && property != null) ||
                            previous != null && !previous.equals(property)) {

                        if (_analyzer == null) {
                            // Get user's decision.
                            userDecision = MessageHandler.yesNoQuestion(
                                    "Resolved auxilary property for \"" + 
                                    getExtendedUseCaseName() + 
                                    "\" is different from previous. " +
                            "Update this property?");
                        } else {
                            // Suppress the dialog.
                            userDecision = _analyzer.overwriteDependentProperties
                            .getToken() == BooleanToken.TRUE;

                        }
                        // Remember that we have made a decision.
                        hasDecided = true;
                    }
                }

                // Do nothing only if the previous resolved property 
                // did not exist AND the user did not want to update.
                if (userDecision || previous != null) {

                    // Get the property attribute so we can either update
                    // its value or compare its value against the resolved
                    // value (regression testing).
                    PropertyAttribute attribute = _getPropertyAttribute(namedObj);                            
                    _updatePropertyAttribute(attribute, userDecision ? property : previous);

                }
            } 
        }

        System.out.println(_getStatsAsString(": "));        
    }

    /**
     * Set the solver to testing mode.
     */
    public void setTesting() {
        action.setPersistent(true);
        setAction(TEST);
        _repaintGUI();
    }


    /**
     * Return the error message string that shows the mismatch between
     * the two given exception strings. This method does not compare
     * the content between the input strings. It merely wraps the input 
     * strings into a larger error message that says there is a mismatch
     * between the two. This is used to generate the error message for 
     * failed regression test that detects a mismatch between the 
     * expected (trained) exception and the generate exception.
     * @param exception The first input error message.
     * @param trainedException The second input error message.
     * @return The exception message string.
     */
    public static String getTrainedExceptionMismatchMessage(
            String exception, String trainedException) {
        return "The generated exception:" + _eol + 
        "-------------------------------------------------------" + _eol + 
        exception + _eol + 
        "-------------------------------------------------------" + _eol + 
        " does not match the trained exception:" + _eol + 
        "-------------------------------------------------------" + _eol + 
        trainedException + _eol +
        "-------------------------------------------------------" + _eol;
    }

    /**
     * Return the name of the trained exception attribute.
     * @return The name of the trained exception attribute.
     */
    public Attribute getTrainedExceptionAttribute() {
        return getAttribute(_TRAINED_EXCEPTION_ATTRIBUTE_NAME);
    }

}
