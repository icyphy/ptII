/**
 * 
 */
package ptolemy.data.properties.lattice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypeConflictException;
import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.properties.ModelAnalyzer;
import ptolemy.data.properties.Property;
import ptolemy.data.properties.PropertyFailedRegressionTestException;
import ptolemy.data.properties.PropertyHelper;
import ptolemy.data.properties.PropertyResolutionException;
import ptolemy.data.properties.PropertySolver;
import ptolemy.data.properties.gui.PropertySolverGUIFactory;
import ptolemy.data.properties.lattice.PropertyConstraintHelper.Inequality;
import ptolemy.data.type.BaseType;
import ptolemy.graph.CPO;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.FileUtilities;
import ptolemy.util.MessageHandler;

/**
 @author Man-Kit Leung, Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.0.4
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class PropertyConstraintSolver extends PropertySolver {
    /**
     * @param container The given container.
     * @param name The given name
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public PropertyConstraintSolver(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        //latticeClassName = new StringParameter(this, "latticeClassName");
        //latticeClassName.setExpression("ptolemy.data.properties.PropertyLattice");

        // FIXME: What should we use for the extension of the 
        // lattice decription file?
        propertyLattice = new StringParameter(this, "propertyLattice");
        propertyLattice.setExpression("staticDynamic");

        solvingFixedPoint = new StringParameter(this, "solvingFixedPoint");
        solvingFixedPoint.setExpression("least");

        actorConstraintType = new StringParameter(this, "actorConstraintType");
        actorConstraintType.setExpression("out == meet(in1, in2, ...)");

        connectionConstraintType = new StringParameter(this, "connectionConstraintType");
        connectionConstraintType.setExpression("sink == meet(src1, src2, ...)");

        compositeConnectionConstraintType = new StringParameter(this, "compositeConnectionConstraintType");
        compositeConnectionConstraintType.setExpression("sink == meet(src1, src2, ...)");

        fsmConstraintType = new StringParameter(this, "fsmConstraintType");
        fsmConstraintType.setExpression("sink == meet(src1, src2, ...)");
        
        expressionASTNodeConstraintType = new StringParameter(this, "expressionASTNodeConstraintType");
        expressionASTNodeConstraintType.setExpression("parent == meet(child1, child2, ...)");

        logConstraints = new Parameter(this, "Log constraints to file");        
        logConstraints.setTypeEquals(BaseType.BOOLEAN);
        logConstraints.setExpression("false");    

        // FIXME: Change default directory of trained constraints to current working directory
        logDirectory = new FileParameter(this, "Log directory");
        logDirectory.setExpression("C:\\temp\\ConstraintFiles");

        trainedConstraintDirectory = new FileParameter(this, "Trained constraint directory");

trainedConstraintDirectory.setExpression(logDirectory.getExpression() + "\\trainedConstraints");
//        trainedConstraintDirectory.setExpression(getSource() + "\\trainedConstraints"); 
/*        
        if (getSource() != null) {
            trainedConstraintDirectory.setExpression(getSource() + "\\trainedConstraints"); 
        } else {
            trainedConstraintDirectory.setExpression(logDirectory.getExpression() + "\\trainedConstraints"); 
        }
*/
        _addChoices();

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"120\" height=\"40\" "
                + "style=\"fill:blue\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\nResolve Properties</text></svg>");

        new PropertySolverGUIFactory(
                this, "_propertyConstraintSolverGUIFactory");
        
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The file parameter for the lattice description file.
     */
    public StringParameter propertyLattice;
    
    /** Indicate whether to compute the least or greatest 
     *  fixed point solution.
     */
    public StringParameter solvingFixedPoint;
        
    public StringParameter connectionConstraintType;
    
    public StringParameter actorConstraintType;

    public StringParameter compositeConnectionConstraintType;

    public StringParameter expressionASTNodeConstraintType;
    
    public StringParameter fsmConstraintType;

    public Parameter logConstraints;
    
    public FileParameter logDirectory;

    public FileParameter trainedConstraintDirectory;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
        
    public List<PropertyTerm> getAffectedTerms(PropertyTerm updateTerm) 
            throws IllegalActionException {
        return _propertyTermManager.getAffectedTerms(updateTerm);
    }
    
    /**
    * @return the constraintManager
    */
    public ConstraintManager getConstraintManager() {
        return _constraintManager;
    }
    
    public String getExtendedUseCaseName() {
        return "lattice::" + getUseCaseName();
    }
    
    /**
     * Returns the helper that contains property information for
     * the given AST node.
     * @param node The given ASTPtRootNode.
     * @return The associated property constraint helper.
     */    
    public PropertyConstraintASTNodeHelper getHelper(
            ASTPtRootNode node) throws IllegalActionException {

        return (PropertyConstraintASTNodeHelper) _getHelper(node);
    }
    
    /**
     * Returns the helper that contains property information for
     * the given component.
     * @param component The given component
     * @return The associated property constraint helper.
     */    
    public PropertyHelper getHelper(NamedObj component)
            throws IllegalActionException {

        return (PropertyHelper) _getHelper(component);
    }


    /**
     * Return the property constraint helper associated with the
     * given object.
     * @param object The given object.
     */
    public PropertyHelper getHelper(Object object) 
            throws IllegalActionException {
        
        return (PropertyHelper) _getHelper(object);
    }
    
    /**
     * Return the property lattice for this constraint solver.
     * @return The property lattice for this constraint solver.
     */
    public PropertyLattice getLattice() {
        if (_lattice == null) {
            _lattice = PropertyLattice.getPropertyLattice(
                    propertyLattice.getExpression());                
        }
        return _lattice;
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
        PropertyTerm term = (PropertyTerm) getPropertyTerm(object);
        return (Property) term.getValue();
    }
    
    /**
     * Return the property term from the given object.
     * @param object The given object.
     * @return The property term of the given object.
     * @throws IllegalActionException 
     */
    public PropertyTerm getPropertyTerm(Object object) {
        return getPropertyTermManager().getPropertyTerm(object);
    }
    
    public PropertyTermFactory getPropertyTermManager() {
        if (_propertyTermManager == null) {
            _propertyTermManager = _getPropertyTermManager();
        }
        return _propertyTermManager;
    }
    

    public String getUseCaseName() {
        return getLattice().getName();
    }
    
    public Boolean isLogMode() throws IllegalActionException {
        return (isTraining() &&
                (logConstraints.getToken() == BooleanToken.TRUE));
    }

    public void reset() {
        super.reset();
        _propertyTermManager = null;
        _trainedConstraints.clear();
    }
    
    /**
     * Resolve the property values for the given top-level entity.
     * @param toplevel The given top level entity.
     */
    protected void _resolveProperties(ModelAnalyzer analyzer) 
            throws KernelException {
        super._resolveProperties(analyzer);

        PropertyConstraintCompositeHelper toplevelHelper = 
            (PropertyConstraintCompositeHelper) getHelper(toplevel());

        toplevelHelper.reinitialize();
        
        toplevelHelper._addDefaultConstraints(
                _getConstraintType(actorConstraintType.stringValue()));
        
        // FIXME: have to generate the connection every time
        // because the model structure can changed.
        // (i.e. adding or removing connections.)
        toplevelHelper._setConnectionConstraintType( 
                _getConstraintType(connectionConstraintType.stringValue()), 
                _getConstraintType(compositeConnectionConstraintType.stringValue()),
                _getConstraintType(fsmConstraintType.stringValue()),
                _getConstraintType(expressionASTNodeConstraintType.stringValue()));        
        
        try {
            List<Inequality> conflicts = new LinkedList<Inequality>();
            List<Inequality> unacceptable = new LinkedList<Inequality>();

            // Check declared properties across all connections.
            //List propertyConflicts = topLevel._checkDeclaredProperties();
            //conflicts.addAll(propertyConflicts);

            // Collect and solve type constraints.
            List<Inequality> constraintList = toplevelHelper.constraintList();
            
/*       // FIXME: this is the iterative approach.     
    List constraintList = new ArrayList();
    Iterator helpers = _helperStore.values().iterator();
    while (helpers.hasNext()) {
        PropertyConstraintHelper helper = 
            (PropertyConstraintHelper) helpers.next();
        
        constraintList.addAll(helper.constraintList());
    }
//*/            

            // NOTE: To view all property constraints, uncomment these.

            /*
             Iterator constraintsIterator = constraintList.iterator();
             while (constraintsIterator.hasNext()) {
                 System.out.println(constraintsIterator.next().toString());
             }
             */

            if (constraintList.size() > 0) {
                CPO cpo = getLattice().lattice();
                
                // Instantiate our own customized version of InequalitySolver.
                // ptolemy.graph.InequalitySolver solver = new ptolemy.graph.InequalitySolver(cpo);
                InequalitySolver solver = new InequalitySolver(cpo, this);
                
                Iterator constraints = constraintList.iterator();

                solver.addInequalities(constraints);
                _constraintManager.setConstraints(constraintList);

                
// BEGIN CHANGE Thomas, 04/10/2008                
                // Collect statistics.
                _stats.put("# of generated constraints", constraintList.size());
                _stats.put("# of property terms", _propertyTermManager.terms().size());
                
                // log initial constraints to file
                BufferedWriter writer = null;
                Date date = new Date();
                String timestamp = date.toString().replace(":", "_");
                File constraintDir = new File(logDirectory.getExpression());
                String logFilename = constraintDir + "\\" + getContainer().getName() + "__" + getUseCaseName() + "__" + timestamp + ".txt";
                
                if (isLogMode()) {
                    try {
                        if (!constraintDir.exists()) {
                            constraintDir.mkdir();
                        }
                        writer = new BufferedWriter(new FileWriter(logFilename));
                        writer.write(_getConstraintsAsString(constraintList, "I"));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } 

                // Record the initial constraints.
                if (isTraining()) {
                    String constraintFilename = 
                        _getTrainedConstraintFilename() + "_initial.txt";

                    // Populate the _trainedConstraints list.
                    _logHelperConstraints(toplevelHelper);  

                    // Write the list to file.
                    _updateConstraintFile(constraintFilename);
                }

// END CHANGE Thomas, 04/10/2008                

                
                try {
                    // Find the greatest solution (most general type)
                    if (solvingFixedPoint.stringValue().equals("greatest")) {
                        solver.solveGreatest();
                    } else {
                        solver.solveLeast();
                    }
                } catch (InvalidStateException ex) {
                    throw new InvalidStateException(toplevel(), ex,
                            "The basic property lattic was: "
                            + _lattice.basicLattice());
                }

                // log resolved constraints to file.
                if (isLogMode()) {
                    try {
                        writer.write(_getConstraintsAsString(constraintList, "R"));
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                
                // If some inequalities are not satisfied, or type variables
                // are resolved to unacceptable types, such as
                // BaseType.UNKNOWN, add the inequalities to the list of 
                // property conflicts.
                Iterator inequalities = constraintList.iterator();

                while (inequalities.hasNext()) {
                    Inequality inequality = (Inequality) inequalities.next();

                    if (!inequality.isSatisfied(_lattice.lattice())) {
                        inequality.isSatisfied(_lattice.lattice());
                        conflicts.add(inequality);
                    } else {
                        // Check if type variables are resolved to unacceptable
                        //types
                        InequalityTerm[] lesserVariables = inequality
                                .getLesserTerm().getVariables();
                        InequalityTerm[] greaterVariables = inequality
                                .getGreaterTerm().getVariables();
                        boolean added = false;

                        for (int i = 0; i < lesserVariables.length; i++) {
                            InequalityTerm variable = lesserVariables[i];

                            if (!variable.isValueAcceptable()) {
                                unacceptable.add(inequality);
                                added = true;
                                break;
                            }
                        }

                        if (added == false) {
                            for (int i = 0; i < greaterVariables.length; i++) {
                                InequalityTerm variable = greaterVariables[i];

                                if (!variable.isValueAcceptable()) {
                                    unacceptable.add(inequality);
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if (conflicts.size() > 0) {
                throw new TypeConflictException(conflicts,
                        "Properties conflicts occurred in "
                                + toplevel().getFullName()
                                + " on the following inequalities:");
            }
            if (unacceptable.size() > 0) {
                throw new TypeConflictException(unacceptable,
                        "Properties resolved to unacceptable types in "
                                + toplevel().getFullName()
                                + " due to the following inequalities:");
            }
                        
        } catch (IllegalActionException ex) {
            // This should not happen. The exception means that
            // _checkDeclaredProperty or constraintList is called on a
            // transparent actor.
            throw new PropertyResolutionException(toplevel(), ex,
                    "Property resolution failed because of an error "
                            + "during property inference");
        }        
        
    }


    /**
     * Update the property.
     * @throws IllegalActionException
     */
    public void updateProperties() throws IllegalActionException {
        super.updateProperties();

        // Only need to look at the constraints of the top level helper.
        PropertyHelper helper = getHelper(toplevel());
        
        String constraintFilename = _getTrainedConstraintFilename() + "_resolved.txt";

        if (isTraining()) {
            _trainedConstraints.clear();
            
            // Populate the _trainedConstraints list.
            _logHelperConstraints((PropertyConstraintHelper) helper);          

            // Write the list to file.
            _updateConstraintFile(constraintFilename);
            
        } else {
            // Populate the _trainedConstraints list.
            _readConstraintFile(constraintFilename);
            
            // Match and remove from the list.
            _regressionTestConstraints((PropertyConstraintHelper) helper);
            
            // Check if there are unmatched constraints.
            _checkMissingConstraints();
        }        
        
        System.out.println(getStatistics());
    }

    /**
     * @return
     */
    private String _getTrainedConstraintFilename() {
        File constraintDir;
        try {
            constraintDir = new File(FileUtilities.nameToURL(trainedConstraintDirectory.getExpression(),
                     null, null).getFile());
        } catch (IOException e) {
            constraintDir = new File(trainedConstraintDirectory.getExpression());            
        }
        
        // Make an unique file name from the toplevel container.
        String constraintFilename = constraintDir + "\\" 
            + toplevel().getName() + "__" + getUseCaseName();
        return constraintFilename;
    }



    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _addChoices() {
        File file = null;
        
        try {
            file = new File(FileUtilities.nameToURL(
                    "$CLASSPATH/ptolemy/data/properties/lattice", 
                    null, null).getFile());
        } catch (IOException ex) {
            // Should not happen.
            assert false;
        }

        File[] lattices = file.listFiles(); 
        for (int i = 0; i < lattices.length; i++) {
            String latticeName = lattices[i].getName();
            if (lattices[i].isDirectory() && !latticeName.equals("CVS") && !latticeName.equals(".svn")) {
                propertyLattice.addChoice(latticeName);
            }
        }
        
        solvingFixedPoint.addChoice("least");
        solvingFixedPoint.addChoice("greatest");
        
        actorConstraintType.addChoice("in >= out");
        actorConstraintType.addChoice("out >= in");
        actorConstraintType.addChoice("out == in");
        actorConstraintType.addChoice("out == meet(in1, in2, ...)");
        actorConstraintType.addChoice("in == meet(out1, out2, ...)");
        actorConstraintType.addChoice("NONE");
//        actorConstraintType.addChoice("in > out");
//        actorConstraintType.addChoice("out > in");
//        actorConstraintType.addChoice("out != in");
        
        connectionConstraintType.addChoice("src >= sink");
        connectionConstraintType.addChoice("sink >= src");
        connectionConstraintType.addChoice("sink == src");
        connectionConstraintType.addChoice("src == meet(sink1, sink2, ...)");
        connectionConstraintType.addChoice("sink == meet(src1, src2, ...)");
        connectionConstraintType.addChoice("NONE");
//        connectionConstraintType.addChoice("src > sink");
//        connectionConstraintType.addChoice("sink > src");
//        connectionConstraintType.addChoice("sink != src");

        compositeConnectionConstraintType.addChoice("src >= sink");
        compositeConnectionConstraintType.addChoice("sink >= src");
        compositeConnectionConstraintType.addChoice("sink == src");
        compositeConnectionConstraintType.addChoice("src == meet(sink1, sink2, ...)");
        compositeConnectionConstraintType.addChoice("sink == meet(src1, src2, ...)");
        compositeConnectionConstraintType.addChoice("NONE");
//        compositeConnectionConstraintType.addChoice("src > sink");
//        compositeConnectionConstraintType.addChoice("sink > src");
//        compositeConnectionConstraintType.addChoice("sink != src");
                
        expressionASTNodeConstraintType.addChoice("child >= parent");
        expressionASTNodeConstraintType.addChoice("parent >= child");
        expressionASTNodeConstraintType.addChoice("parent == child");
        //expressionASTNodeConstraintType.addChoice("child == meet(parent1, parent2, ...)");
        expressionASTNodeConstraintType.addChoice("parent == meet(child1, child2, ...)");
        expressionASTNodeConstraintType.addChoice("NONE");
//        expressionASTNodeConstraintType.addChoice("child > parent");
//        expressionASTNodeConstraintType.addChoice("parent > child");
//        expressionASTNodeConstraintType.addChoice("parent != child");

        fsmConstraintType.addChoice("src >= sink");
        fsmConstraintType.addChoice("sink >= src");
        fsmConstraintType.addChoice("sink == src");
        fsmConstraintType.addChoice("src == meet(sink1, sink2, ...)");
        fsmConstraintType.addChoice("sink == meet(src1, src2, ...)");
        fsmConstraintType.addChoice("NONE");
//        fsmConstraintType.addChoice("src > sink");
//        fsmConstraintType.addChoice("sink > src");
//        fsmConstraintType.addChoice("sink != src");
    }

    private void _checkMissingConstraints() {
        String errorMessage = _eol + "Property \"" + getUseCaseName() + 
        "\" resolution failed." + _eol;

        boolean hasError = false;

        for (String trainedValue : _trainedConstraints) {
            errorMessage += "    Missing constraint: \"" 
                + trainedValue + "\"." + _eol;
            
            hasError = true;
        }

        if (hasError) {
            getSharedUtilities().addErrors(errorMessage);
        }
    }

    /**
     * @param annotation
     * @param output
     * @param constraints
     * @return
     */
    private String _getConstraintAsString(Inequality inequality, String annotation) {
        String output = "";
        String lesserTermIsEffective = ((PropertyTerm)(inequality.getLesserTerm())).isEffective() ? "eff" : "ineff";
        String greaterTermIsEffective = ((PropertyTerm)(inequality.getGreaterTerm())).isEffective() ? "eff" : "ineff";

        String greaterTermContainer = "";
        String lesserTermContainer = "";
        if (inequality.getGreaterTerm().getAssociatedObject() instanceof ASTPtRootNode) {
            try {
                //FIXME: get through sharedUtility
                greaterTermContainer = getHelper(inequality.getGreaterTerm().getAssociatedObject()).getContainerEntity((ASTPtRootNode)inequality.getGreaterTerm().getAssociatedObject()).getFullName() + " ";
            } catch (IllegalActionException e) {
                assert false;
            }
        }
        if (inequality.getLesserTerm().getAssociatedObject() instanceof ASTPtRootNode) {
            try {
                lesserTermContainer = getHelper(inequality.getLesserTerm().getAssociatedObject()).getContainerEntity((ASTPtRootNode)inequality.getLesserTerm().getAssociatedObject()).getFullName() + " ";
            } catch (IllegalActionException e) {
                assert false;
            }
        }

        output += annotation + 
                "\t" + lesserTermIsEffective + 
                "\t" + greaterTermIsEffective + 
                // "\t" + inequality.hashCode() + 
                "\t" + lesserTermContainer + inequality.getLesserTerm() + 
                "\t" + "<=" + 
                "\t" + greaterTermContainer + inequality.getGreaterTerm(); 

        return output;
    }

    /**
     * Return null if the associated component of the helper is not
     * a NamedObj (e.g. if it is a PtASTRootNode); Otherwise, return
     * an array of two ConstraintAttribute's that store the list of
     * constraints as String's.
     * @param helper
     * @return
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    private ConstraintAttribute[] _getConstraintAttributes(
            PropertyConstraintHelper helper) throws IllegalActionException, NameDuplicationException {
        
        Object object = helper.getComponent();
        
        if (object instanceof NamedObj) {
            NamedObj namedObj = (NamedObj) object;

            String attributeName[] = new String[2];
            ConstraintAttribute attribute[] = new ConstraintAttribute[2];
            attributeName[0] = getExtendedUseCaseName() + "::OwnConstraints";
            attributeName[1] = getExtendedUseCaseName() + "::SubHelperConstraints";

            for (int i = 0; i < 2; i++) {
                attribute[i] = (ConstraintAttribute) 
                namedObj.getAttribute(attributeName[i]);
                
                if (attribute[i] == null) {
                    attribute[i] = new ConstraintAttribute(namedObj, attributeName[i]);                    
                }
// FIXME: Remove??
                TextStyle style = new TextStyle(((ConstraintAttribute)attribute[i]), "_style");
                style.height.setExpression("10");
                style.width.setExpression("60");
                
            }
            return attribute;
        } else {
            // FIXME: This happens for ASTNodeHelper.
            return null;
        }
    }



    private String _getConstraintsAsString(
            List<Inequality> constraintList, 
            String annotation) {
        
        String output = "";
        for (Inequality inequality : constraintList) {
            output += _getConstraintAsString(inequality, annotation) + _eol;
        }

        return output;
    }

    /**
     * 
     * @param typeValue
     * @return
     * @throws IllegalActionException
     */
    private ConstraintType _getConstraintType(String typeValue) throws IllegalActionException {
        boolean isEquals = typeValue.contains("==");
        boolean isSrc = 
            typeValue.startsWith("src") || 
            typeValue.startsWith("in") ||
            typeValue.startsWith("parent");
        boolean isNotEquals = typeValue.contains("!=");
        
        boolean hasMeet = typeValue.contains("meet");

        if (typeValue.equals("NONE")) {
            return ConstraintType.NONE;
        }
        if (hasMeet) {
            return (isSrc) ? ConstraintType.SRC_EQUALS_MEET : 
                ConstraintType.SINK_EQUALS_MEET;
            
        } else if (isEquals) {
            return ConstraintType.EQUALS;
            
        } else if (isNotEquals) {
            return ConstraintType.NOT_EQUALS;

        } else { 
            return (isSrc) ? ConstraintType.SRC_EQUALS_GREATER :
                ConstraintType.SINK_EQUALS_GREATER;
        }            
    }
    

    /**
     * @return
     */
    protected PropertyTermManager _getPropertyTermManager() {
// FIXME: doesn't work for other use-cases!
//        return new StaticDynamicTermManager(this);
        return new PropertyTermManager(this);
    }

    
    /**
     * 
     * @param helper
     */
    private void _logHelperConstraints(PropertyConstraintHelper helper) {
        List<Inequality>[] constraintSet = new List[2];
        constraintSet[0] = helper._ownConstraints;
        constraintSet[1] = helper._subHelperConstraints;
        
        for (int i = 0; i < 2; i++) {
            String whichSet = (i == 0) ? " own " : " subHelper's ";
            
            for (Inequality constraint : constraintSet[i]){
                _trainedConstraints.add(
                    _getConstraintAsString(constraint, whichSet));
            }
        }
    }

    private void _readConstraintFile(String filename) 
            throws PropertyFailedRegressionTestException {
        
        File file = new File(filename);
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
        
            String line = reader.readLine();
            while (line != null) {
                _trainedConstraints.add(line);
                line = reader.readLine();
            }
            
            _stats.put("# of trained constraints", _trainedConstraints.size());

            reader.close();
        } catch (Exception ex) {
            throw new PropertyFailedRegressionTestException(this,
                    "Failed to open or read the constraint file \"" +
                    filename + "\".");
        }
    }

    private void _regressionTestConstraints(PropertyConstraintHelper helper) {
        Object object = helper.getComponent();
        if (!(object instanceof NamedObj)) {
            return;
        }
        NamedObj namedObj = (NamedObj) object;
        
        String errorMessage = _eol + "Property \"" + getUseCaseName() + 
        "\" resolution failed for " + namedObj.getFullName() + 
        "'s helper." + _eol;

        List<Inequality>[] constraintSet = new List[2];
        constraintSet[0] = helper._ownConstraints;
        constraintSet[1] = helper._subHelperConstraints;
        
        boolean hasError = false;
    
        for (int i = 0; i < 2; i++) {
            String whichSet = (i == 0) ? " own " : " subHelper's ";
            
            for (Inequality constraint : constraintSet[i]){
                String constraintString = 
                    _getConstraintAsString(constraint, whichSet);
                
                // Remove from the trained set so we can test for duplicates.
                if (!_trainedConstraints.remove(constraintString)) {
                    errorMessage += "    Extra" + whichSet + 
                    "constraint generated: \"" + constraintString + "\"." + _eol;
                    
                    hasError = true;
                } else {
                    errorMessage += "";
                }
            }
        }
    
        if (hasError) {
            getSharedUtilities().addErrors(errorMessage);
        }
        
    }
    
    /**
     * Log the trained constraints in a subdirectory under the 
     * specified logDirectory. The contraint file has an unique
     * name consist of the name of toplevel container and this
     * solver. If the constraint file already exists, an overwrite
     * warning message is sent to the user.
     * @param filename
     * @throws PropertyResolutionException Thrown if there is a
     *  problem opening, writing, or closing the constraint file.
     */
    private void _updateConstraintFile(String filename) throws IllegalActionException {        
        if (!isTraining() || !isLogMode()) {
            return;
        }
        
        File constraintDir = 
            new File(logDirectory.getExpression() + "\\trainedConstraints");
        
        try {
            // Create the directory path to the constraint file.
            if (!constraintDir.exists()) {
                constraintDir.mkdirs();
            }
            
            File constraintFile = new File(filename);
            
            if (constraintFile.exists()) {
                if (_analyzer != null && _analyzer.overwriteConstraint
                        .getExpression().equals("false")) {
                    return;
                }
                // Ask user for a decision.
                if (_analyzer == null && 
                        !logConstraints.getExpression().equals("true")) { 
                        /*
                        !MessageHandler.yesNoQuestion(
                        "The constraint file \"" + filename + "\""
                        + " exists. OK to overwrite?")) {
                         */
                    // Don't overwrite, do nothing and return.
                    return;
                }
            }
            
            Writer writer = new FileWriter(filename);
            for (String constraint : _trainedConstraints) {
                writer.write(constraint + _eol);
            }
            writer.close();
            
        } catch (IOException ex) {
            throw new PropertyResolutionException(this, ex, 
                    "Failed to train the constraint log file \"" +
                    filename + "\".");
        }
    }


    /**
     *
     */
    public static enum ConstraintType { 
        SRC_GREATER, 
        SRC_EQUALS_GREATER, 
        SINK_GREATER, 
        SINK_EQUALS_GREATER, 
        SRC_EQUALS_MEET, 
        SINK_EQUALS_MEET, 
        EQUALS,
        NOT_EQUALS,
        NONE 
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private ConstraintManager _constraintManager = 
        new ConstraintManager(this);

    /**
     * The property lattice for this constraint solver.
     */
    private PropertyLattice _lattice = null;
    
    private PropertyTermManager _propertyTermManager;

    /**
     * The set of trained constraints. This set is populated from
     * parsing the constraint file when training mode is off. 
     */
    private List<String> _trainedConstraints = new LinkedList<String>(); 

}