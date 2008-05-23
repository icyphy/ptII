package ptolemy.data.properties;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import ptolemy.actor.CompositeActor;
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
import ptolemy.kernel.attributes.VersionAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;
import ptolemy.util.FileUtilities;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

public abstract class PropertySolver extends Attribute {

    public PropertySolver(NamedObj container, String name) throws IllegalActionException, NameDuplicationException {
        super(container, name);

        action = new SharedParameter(
                this, "action", PropertySolver.class, "true");
        action.setStringMode(true);
        _addActions(action);

        sharedUtilitiesWrapper = new SharedParameter(
                this, "sharedUtilitiesWrapper", PropertySolver.class);
        sharedUtilitiesWrapper.setPersistent(false);
        sharedUtilitiesWrapper.setVisibility(Settable.NONE);

        // **We can only create a new shared utilities object
        // only once per model.
        if (sharedUtilitiesWrapper.getExpression().length() == 0) {
            sharedUtilitiesWrapper.setToken(new ObjectToken(new SharedUtilities(sharedUtilitiesWrapper)));
        }

        _sharedUtilities = (SharedUtilities) ((ObjectToken) 
                sharedUtilitiesWrapper.getToken()).getValue();

        _highlighter = new PropertyHighlighter(this, "PropertyHighlighter");
    }

    public Parameter action;

    public SharedParameter sharedUtilitiesWrapper;

    /**
     * 
     * @param actionParameter
     */
    protected static void _addActions(Parameter actionParameter) {
        actionParameter.setExpression(PropertySolver.ANNOTATE);
        actionParameter.addChoice(PropertySolver.ANNOTATE);
        actionParameter.addChoice(PropertySolver.TEST);
        actionParameter.addChoice(PropertySolver.VIEW);
        actionParameter.addChoice(MANUAL_ANNOTATE);
        actionParameter.addChoice(PropertySolver.CLEAR);
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

// FIXME: Do we need the clone method?
/*
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
    	PropertySolver solver = (PropertySolver) super.clone(workspace);
    	try {
			solver.sharedUtilitiesWrapper.setToken(new ObjectToken(new SharedUtilities(sharedUtilitiesWrapper)));
	    	solver._sharedUtilities = (SharedUtilities) ((ObjectToken) 
	    			solver.sharedUtilitiesWrapper.getToken()).getValue();
	        
		} catch (IllegalActionException e) {
			assert false;
		}

    	return solver;
    }
*/
    
    public boolean isTraining() {
        return ((action.getExpression().equals(PropertySolver.ANNOTATE)) ||
                (action.getExpression().equals(PropertySolver.MANUAL_ANNOTATE)));
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

    public void reset() {
        _declaredProperties = new HashMap<Object, Property>();
        _resolvedProperties = new HashMap<Object, Property>();
        _nonSettables = new HashSet<Object>();
        _previousProperties = new HashMap<Object, Property>();
        _helperStore = new HashMap<Object, PropertyHelper>();
        _stats.clear();
    }

    /**
     * Resolve the property values for the given top-level entity.
     * @param topLevel The given top level entity.
     * @throws IllegalActionException TODO
     */
    public void resolveProperties(ModelAnalyzer analyzer, boolean isInvoked)
    throws KernelException {
        getSharedUtilities().addRanSolvers(this);

        _analyzer = analyzer;
        _isInvoked = isInvoked;        
        
        // Clear the resolved properties for the chosen solver.
        String actionValue = action.getExpression();
        if (actionValue.equals(PropertySolver.CLEAR)) {
            if (isInvoked) {
                clearProperties();
                clearDisplay();
            }
            return;

        } else if (actionValue.equals(PropertySolver.VIEW)) {
            if (isInvoked) {
                clearDisplay();
                showProperties();
                highlightProperties();        
            }
            return;
        } else {
            // If this is not an intermediate (invoked) solver, 
            // we need to clear the display.
            if (isInvoked && (isTraining() || isManualAnnotate())) {
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
            }
            
            _resolveProperties(analyzer);
            updateProperties();
        }
    }

    /**
     * Resolve the property values for the given top-level entity.
     * Do nothing in this base class.
     * @param topLevel The given top level entity.
     * @throws IllegalActionException TODO
     */
    protected void _resolveProperties(ModelAnalyzer analyzer) 
    throws KernelException {
    }

    /**
     * Return the property helper for the given object. 
     * @param object The given object.
     * @return The property helper for the object.
     * @throws IllegalActionException Thrown if the helper cannot
     *  be found or instantiated.
     */
    public abstract PropertyHelper getHelper(Object object) 
    throws IllegalActionException;

    /*
    public void addPropertyChangedListener(PropertyChangedListener listener) {
        _listeners.add(listener);
    }

    public void removePropertyChangedListener(
            PropertyChangedListener listener) {
        _listeners.remove(listener);
    }
     */    
    public abstract String getExtendedUseCaseName();

    public abstract String getUseCaseName();

    public void setAction (String actionString) {
        action.setExpression(actionString);
    }

    public void displayProperties() throws IllegalActionException {
        // Do nothing if we are not in a mode that allow display.
        if (!(isTraining() || isView() || isManualAnnotate())) {
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

        // Repaint the GUI.
        requestChange(new ChangeRequest(this,
        "Repaint the GUI.") {
            protected void _execute() throws Exception {
            }
        });        
    }

    public void showProperties() throws IllegalActionException {
        _highlighter.showProperties();

        // Repaint the GUI.
        requestChange(new ChangeRequest(this,
        "Repaint the GUI.") {
            protected void _execute() throws Exception {
            }
        });
    }

    public void highlightProperties() throws IllegalActionException {
        _highlighter.highlightProperties();        

        // Repaint the GUI.
        requestChange(new ChangeRequest(this,
        "Repaint the GUI.") {
            protected void _execute() throws Exception {
            }
        });
    }

    /**
     * Update the property.
     * @throws IllegalActionException
     */
    public void updateProperties() throws IllegalActionException {
        _addStatistics();

        boolean hasDecided = false;
        boolean userDecision = true;

        boolean doTest = isTesting() && _isInvoked;
        boolean doUpdate = isTraining() || isManualAnnotate();
        
        if (isView() || isClear()) {
            return;
        }
        //Iterator propertyables = _resolvedProperties.keySet().iterator();

        Iterator helpers = _helperStore.values().iterator();
        while (helpers.hasNext()) {
            PropertyHelper helper = (PropertyHelper) helpers.next();
            Iterator propertyables = helper.getPropertyables().iterator();

            while (propertyables.hasNext()) {
                Object propertyableObject = propertyables.next();

                if (propertyableObject instanceof NamedObj) {
                    NamedObj namedObj = (NamedObj) propertyableObject;

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
                                    userDecision = (_analyzer.overwriteDependentProperties
                                            .getToken() == BooleanToken.TRUE);

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
                } else {
                    // FIXME: This happens when the propertyable is an ASTNodes,
                    // or any non-Ptolemy objects. We are not updating their
                    // property values, nor doing regression test for them.
                }
            }
        }
    }

    /**
     * @param attribute
     * @param property
     * @throws IllegalActionException
     */
    private void _updatePropertyAttribute(PropertyAttribute attribute, Property property) throws IllegalActionException {
        if (property != null) {
            // Write results to attribute
            attribute.setExpression(property.toString());

        } else {
            attribute.setExpression("");                            
        }
    }

    /**
     * @param propertyable
     * @return
     * @throws IllegalActionException
     */
    private PropertyAttribute _getPropertyAttribute(NamedObj propertyable) 
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
            throw new PropertyResolutionException(propertyable, 
            "Failed to get the PropertyAttribute.");
        }
        return attribute;
    }

    /**
     * 
     * @param propertyableObject
     * @param namedObj
     * @param attribute
     * @param property
     * @throws IllegalActionException
     */
    protected void _regressionTest(NamedObj namedObj, Property property) 
    throws IllegalActionException {

        Property previousProperty = 
            getPreviousProperty(namedObj);

        // Restore the previous resolved property, if there exists one.
        if (previousProperty != null) {
            PropertyAttribute attribute = _getPropertyAttribute(namedObj);
            _updatePropertyAttribute(attribute, previousProperty);
        }

        if (previousProperty != property) {
            if (previousProperty == null ||
                    (previousProperty != null && !previousProperty.equals(property))) {

                _sharedUtilities.addErrors(_eol + "Property \"" + getUseCaseName() + 
                        "\" resolution failed for " + namedObj.getFullName() + 
                        "." + _eol + "    Trained value: \"" +
                        previousProperty +
                        "\"; Resolved value: \"" +
                        property + "\".");
            }
        }
    }

    /**
     * 
     * @param object
     * @return
     * @throws IllegalActionException
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

                helperClass = Class.forName(componentClass.getName()
                        .replaceFirst("ptolemy", packageName));

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

    /**
     * @param attribute
     * @return
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
            getSharedUtilities().putAttributes(parseTree, attribute);
        }
        return parseTrees.get(attribute);
    }

    /**
     * Find a constraint solver that is associated with the given 
     * property lattice name. There can be more than one solvers with
     * the same lattice. This method returns whichever it finds first. 
     * @param latticeName The given name of the property lattice. 
     * @return The property constraint solver associated with the
     *  given lattice name. 
     * @throws IllegalActionException Thrown if no matched solver
     *  is found.
     */
    public PropertySolver findSolver(String identifier) 
    throws IllegalActionException {

        Iterator iterator = _sharedUtilities.getAllSolvers().iterator();
        while (iterator.hasNext()) {
            PropertySolver solver = 
                (PropertySolver) iterator.next();

            if (solver.getUseCaseName().equals(identifier)) {
                return solver;
            }
        }

        throw new IllegalActionException(
                "Cannot find \"" + identifier + "\" solver.");
    }

    public static void main(String[] args) {
        try {
            File testDirectory = new File(FileUtilities.nameToURL("$CLASSPATH\\ptolemy\\data\\properties\\test\\auto\\", null, null).toURI());
            //File testDirectory = new File("C:\\eclipse\\workspace\\ptII\\ptolemy\\data\\properties\\test\\auto");

            File[] tests = testDirectory.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".xml");
                }
            });

            int i = 0;
            for (File file : tests) {
                //System.out.flush();
                System.out.println(++i + ".) -------Testing " + file.getName());
                testProperties(new String[]{file.getPath()});
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /** Resolve properties for a model.
     *  @param args An array of Strings, each element names a MoML file
     *  containing a model.
     *  @return The return value of the last subprocess that was run
     *  to compile or run the model.  Return -1 if called  with no arguments.
     *  @exception Exception If any error occurs.
     */
    public static int testProperties(String[] args) throws Exception {

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

            CompositeActor toplevel = null;
            boolean isDone = false;
            int numberOfSolverTested = 0;

            while (!isDone) {
                long memStart = 0, memEnd;
                PropertySolver solver = null;
                try {
                    System.gc();
                    memStart = Runtime.getRuntime().totalMemory();
                    parser.reset();    
                    parser.purgeModelRecord(args[i]);
                    toplevel = _getModel(args[i], parser);

                    //isDone = true;
                    ///*

                    // Get all instances of PropertySolver contained in the model.
                    // FIXME: This only gets solvers in the top-level.
                    List solvers = toplevel.attributeList(PropertySolver.class);

                    if (solvers.size() == 0) {
                        // There is no PropertySolver in the model. 
                        throw new PropertyResolutionException(
                        "The model does not contain a solver.");

                    } else if (numberOfSolverTested < solvers.size()) {
                        // Get the last PropertySolver in the list, maybe
                        // it was added last?
                        solver = (PropertySolver) solvers.get(numberOfSolverTested++);

                        try {
                            solver.setAction(PropertySolver.TEST);
                            solver.resolveProperties(true);
                            solver.checkRegressionTestErrors();
                        } catch (KernelException ex) {
                            throw new PropertyResolutionException(solver, ex,
                                    " Failed to resolve properties for \""
                                    + args[i] + "\"");

                        } finally {
                            solver.resetAll();
                        } 
                    } else {
                        isDone = true;
                    }

                    //*/

                } finally {
                    // Destroy the top level so that we avoid
                    // problems with running the model after generating code
                    if (toplevel != null) {
                        toplevel.setContainer(null);
                        toplevel = null;
                    }

                    System.gc();
                    memEnd = Runtime.getRuntime().totalMemory();
                    if ((memEnd - memStart) != 0) {
                        // FIXME: throw some sort of memory leak exception?
                        System.out.println("Memory Usage Before PS: " + memStart);                    
                        System.out.println("Memory Usage After PS: " + memEnd);
                        System.out.println("Memory diff = : " + (memEnd - memStart));
                    }
                }
            }

        }
        return 0;
    }

    /**
     * Clear the display properties.
     */
    public void clearDisplay() {
        _highlighter.clearDisplay();
    }

    public void clearProperties() throws IllegalActionException {
        // Get the PropertySolver.
        List propertyables = getAllPropertyables();
        reset();

        for (Object propertyable : propertyables) {
            if (propertyable instanceof NamedObj) {
                NamedObj namedObj = (NamedObj) propertyable;

                PropertyAttribute attribute = (PropertyAttribute)
                namedObj.getAttribute(getExtendedUseCaseName());

                try {
                    if (attribute != null) {
                        attribute.setContainer(null);
                    }
                } catch (NameDuplicationException e1) {
                    assert false;
                }
            }
        }

        // Repaint the GUI.
        requestChange(new ChangeRequest(this,
        "Repaint the GUI.") {
            protected void _execute() throws Exception {}
        });        
    }

    /**
     * Check if there is any regression testing errors after resolving
     * properties. If so, throw a new PropertyFailedRegressionTestException
     * with an error message that includes all the properties that does not
     * match the regression test values. 
     * @throws PropertyFailedRegressionTestException Thrown if there is any
     *  errors in the regression test.
     */
    public void checkRegressionTestErrors() 
    throws PropertyFailedRegressionTestException {

        List errors = _sharedUtilities.getErrors();

        if (isTesting() && !errors.isEmpty()) {
            String errorMessage = errors.toString();

            throw new PropertyFailedRegressionTestException(
                    this, errorMessage);
        }
    }

    public boolean isManualAnnotate() {
        return action.getExpression().
        equals(PropertySolver.MANUAL_ANNOTATE);
    }

    public boolean isTesting() {
        return action.getExpression().equals(PropertySolver.TEST);
    }

    public boolean isView() {
        return action.getExpression().equals(PropertySolver.VIEW);
    }

    public boolean isClear() {
        return action.getExpression().equals(PropertySolver.CLEAR);
    }

    /**
     * @param path
     * @param parser
     * @return
     * @throws PropertyResolutionException
     */
    private static CompositeActor _getModel(String path, MoMLParser parser) throws PropertyResolutionException {
        // Note: the code below uses explicit try catch blocks
        // so we can provide very clear error messages about what
        // failed to the end user.  The alternative is to wrap the
        // entire body in one try/catch block and say
        // "Code generation failed for foo", which is not clear.
        URL modelURL;

        try {
            modelURL = new File(path).toURL();
        } catch (Exception ex) {
            throw new PropertyResolutionException("Could not open \"" + path + "\"", ex);
        }

        CompositeActor toplevel = null;

        try {
            toplevel = (CompositeActor) parser.parse(null, modelURL);
        } catch (Exception ex) {
            throw new PropertyResolutionException("Failed to parse \"" + path + "\"",
                    ex);
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
    public void resolveProperties(boolean isInvoked) throws KernelException {
        resolveProperties(null, isInvoked);
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
        getSharedUtilities().resetAll();
    }

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
                _resolveProperties(_analyzer);
                getSharedUtilities().addRanSolvers(this);
            }
        } catch (KernelException ex) {
            throw new InternalErrorException(
                    KernelException.stackTraceToString(ex));
        }

        return (Property) _resolvedProperties.get(object);
    }

    public Property getDeclaredProperty(Object object) {
        return (Property) _declaredProperties.get(object);
    }

    public void setResolvedProperty(Object object, Property property) {
        _resolvedProperties.put(object, property);
    }

    public void recordPreviousProperty(Object object, Property property)  {
        _previousProperties.put(object, property);
    }

    public Property getPreviousProperty(Object object) {
        return (Property) _previousProperties.get(object);
    }

    public void setDeclaredProperty(Object object, Property property)  {
        _declaredProperties.put(object, property);
    }

    public void clearResolvedProperty (Object object) {
        _resolvedProperties.remove(object);
    }

    /**
     * 
     * @param node
     * @return
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

    public void addNonSettable(Object object) {
        _nonSettables.add(object);
    }

    public boolean isSettable(Object object) {
        return !_nonSettables.contains(object);
    }

    /**
     * Return the property value associated with the given property lattice
     * and the given port.  
     * @param object The given port.
     * @param lattice The given lattice.
     * @return The property value of the given port. 
     * @throws IllegalActionException 
     */
    public Property getProperty(Object object) {
        return getResolvedProperty(object);
    }

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
     * The set of property-able objects that??? 
     */
    private HashSet<Object> _nonSettables = new HashSet<Object>();

    private HashMap<Object, Property> _previousProperties = new HashMap<Object, Property>();

    /** A hash map that stores the code generator helpers associated
     *  with the actors.
     */
    protected HashMap<Object, PropertyHelper> _helperStore = new HashMap<Object, PropertyHelper>();

    protected Map<Object, Object> _stats = new TreeMap<Object, Object>();

    /**
     * @return the _parser
     */
    public static PtParser getParser() {
        if (_parser == null) {
            _parser = new PtParser();
        }
        return _parser;
    }

    /**
     * @return the _sharedUtilities
     */
    public SharedUtilities getSharedUtilities() {
        return _sharedUtilities; 
    }

    /**
     * 
     * @return
     */
    protected void _addStatistics() {
        _stats.put("# of helpers", _helperStore.size());
        _stats.put("# of propertyables", getAllPropertyables().size());
        _stats.put("# of resolved properties", _resolvedProperties.size());
    }

    public String getStatistics() {
        String result = "";
        for (Object key : _stats.keySet()) {
            result += key + ": " + _stats.get(key) + _eol;
        }
        return result;
    }

    private static PtParser _parser;

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

    public List getAllPropertyables() {
        List result = new LinkedList();

        for (PropertyHelper helper : getAllHelpers()) {
            result.addAll(helper.getPropertyables());
        }
        return result;
    }

    protected static String _eol = StringUtilities.getProperty("line.separator");

    protected boolean _isInvoked;

    protected ModelAnalyzer _analyzer = null;


    /** The display label for "clear" in the action choices */
    protected static final String CLEAR = "CLEAR";

    /** The display label for "test" in the action choices */
    protected static final String TEST = "TEST";

    /** The display label for "annotate" in the action choices */
    protected static final String ANNOTATE = "ANNOTATE";

    protected static final String VIEW = "VIEW";

    protected static final String MANUAL_ANNOTATE = "MANUAL ANNOTATE";

    private SharedUtilities _sharedUtilities;
}
