/**
 * 
 */
package ptolemy.data.properties.lattice;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypeConflictException;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.properties.PropertySolver;
import ptolemy.data.type.BaseType;
import ptolemy.graph.CPO;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalitySolver;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.FileUtilities;

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
     * @exception IllegalActionException
     * @exception NameDuplicationException
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

        connectionConstraintType = new StringParameter(this,
                "connectionConstraintType");
        connectionConstraintType.setExpression("sink == meet(src1, src2, ...)");

        compositeConnectionConstraintType = new StringParameter(this,
                "compositeConnectionConstraintType");
        compositeConnectionConstraintType
                .setExpression("sink == meet(src1, src2, ...)");

        expressionASTNodeConstraintType = new StringParameter(this,
                "expressionASTNodeConstraintType");
        expressionASTNodeConstraintType
                .setExpression("parent == meet(child1, child2, ...)");

        trainingMode = new Parameter(this, "trainingMode");
        trainingMode.setTypeEquals(BaseType.BOOLEAN);
        trainingMode.setExpression("true");

        _addChoices();

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"115\" height=\"40\" "
                + "style=\"fill:blue\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\nResolve Property.</text></svg>");

        //new PropertySolverGUIFactory(this,
        //        "_propertyConstraintSolverGUIFactory");

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    //public StringParameter latticeClassName;    

    private void _addChoices() {
        File file = null;

        try {
            file = new File(FileUtilities.nameToURL(
                    "$CLASSPATH/ptolemy/data/properties/lattice", null, null)
                    .getFile());
        } catch (IOException ex) {
            // Should not happen.
            assert false;
        }

        File[] lattices = file.listFiles();
        for (int i = 0; i < lattices.length; i++) {
            String latticeName = lattices[i].getName();
            if (lattices[i].isDirectory() && !latticeName.equals("CVS")) {
                propertyLattice.addChoice(latticeName);
            }
        }

        solvingFixedPoint.addChoice("least");
        solvingFixedPoint.addChoice("greatest");

        actorConstraintType.addChoice("in <= out");
        actorConstraintType.addChoice("out <= in");
        actorConstraintType.addChoice("out == in");
        actorConstraintType.addChoice("out == meet(in1, in2, ...)");
        actorConstraintType.addChoice("in == meet(out1, out2, ...)");
        actorConstraintType.addChoice("NONE");

        connectionConstraintType.addChoice("src <= sink");
        connectionConstraintType.addChoice("sink <= src");
        connectionConstraintType.addChoice("sink == src");
        connectionConstraintType.addChoice("src == meet(sink1, sink2, ...)");
        connectionConstraintType.addChoice("sink == meet(src1, src2, ...)");
        connectionConstraintType.addChoice("NONE");

        compositeConnectionConstraintType.addChoice("src <= sink");
        compositeConnectionConstraintType.addChoice("sink <= src");
        compositeConnectionConstraintType.addChoice("sink == src");
        compositeConnectionConstraintType
                .addChoice("src == meet(sink1, sink2, ...)");
        compositeConnectionConstraintType
                .addChoice("sink == meet(src1, src2, ...)");
        compositeConnectionConstraintType.addChoice("NONE");

        expressionASTNodeConstraintType.addChoice("child <= parent");
        expressionASTNodeConstraintType.addChoice("parent <= child");
        expressionASTNodeConstraintType.addChoice("parent == child");
        //expressionASTNodeConstraintType.addChoice("child == meet(parent1, parent2, ...)");
        expressionASTNodeConstraintType
                .addChoice("parent == meet(child1, child2, ...)");
        expressionASTNodeConstraintType.addChoice("NONE");
    }

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

    /** React to a change in an attribute. Clear the previous mappings
     *  for the helpers, so new helpers will be created for the new
     *  lattice.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this class).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {

        super.attributeChanged(attribute);

        if (attribute == propertyLattice) {
            if (_lattice == null
                    || !propertyLattice.stringValue()
                            .equals(_lattice.getName())) {

                _lattice = PropertyLattice.getPropertyLattice(propertyLattice
                        .stringValue());

                _helperStore.clear();
            }
        }
    }

    /**
     * Returns the helper that contains property information for
     * the given component.
     * @param component The given component
     * @return The associated property constraint helper.
     */
    public PropertyConstraintHelper getHelper(NamedObj component)
            throws IllegalActionException {

        return (PropertyConstraintHelper) _getHelper(component);
    }

    /**
     * Returns the helper that contains property information for
     * the given AST node.
     * @param node The given ASTPtRootNode.
     * @return The associated property constraint helper.
     */
    public PropertyConstraintASTNodeHelper getHelper(ASTPtRootNode node)
            throws IllegalActionException {

        return (PropertyConstraintASTNodeHelper) _getHelper(node);
    }

    /**
     * Return the property constraint helper associated with the
     * given object.
     * @param object The given object.
     */
    public PropertyConstraintHelper getHelper(Object object)
            throws IllegalActionException {

        return (PropertyConstraintHelper) _getHelper(object);
    }

    /**
     * Return the property lattice for this constraint solver.
     * @return The property lattice for this constraint solver.
     */
    public PropertyLattice getLattice() {
        return _lattice;
    }

    public String getUseCaseName() {
        return _lattice.getName();
    }

    public String getExtendedUseCaseName() {
        return "lattice::" + getUseCaseName();
    }

    /**
     * Resolve the property values for the given top-level entity.
     * @param topLevel The given top level entity.
     */
    public void resolveProperties(CompositeEntity topLevel)
            throws KernelException {
        if (topLevel.getContainer() != null) {
            throw new IllegalArgumentException(
                    "TypedCompositeActor.resolveProperties:"
                            + " The specified actor is not the top level container.");
        }

        PropertyConstraintCompositeHelper compositeHelper = (PropertyConstraintCompositeHelper) getHelper(topLevel);

        compositeHelper.reinitialize();

        compositeHelper
                ._changeDefaultConstraints(_getConstraintType(actorConstraintType
                        .stringValue()));

        // FIXME: have to generate the connection every time
        // because the model structure can changed.
        // (i.e. adding or removing connections.)
        compositeHelper._setConnectionConstraintType(
                _getConstraintType(connectionConstraintType.stringValue()),
                _getConstraintType(compositeConnectionConstraintType
                        .stringValue()),
                _getConstraintType(expressionASTNodeConstraintType
                        .stringValue()));

        try {
            List conflicts = new LinkedList();
            List unacceptable = new LinkedList();

            // Check declared properties across all connections.
            //List propertyConflicts = topLevel._checkDeclaredProperties();
            //conflicts.addAll(propertyConflicts);

            // Collect and solve type constraints.
            List constraintList = compositeHelper.constraintList();

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
                CPO cpo = _lattice.lattice();
                InequalitySolver solver = new InequalitySolver(cpo);
                Iterator constraints = constraintList.iterator();

                solver.addInequalities(constraints);

                try {
                    // Find the greatest solution (most general type)
                    if (solvingFixedPoint.stringValue().equals("greatest")) {
                        solver.solveGreatest();
                    } else {
                        solver.solveLeast();
                    }
                } catch (InvalidStateException ex) {
                    throw new InvalidStateException(topLevel, ex,
                            "The basic property lattic was: "
                                    + _lattice.basicLattice());
                }

                // If some inequalities are not satisfied, or type variables
                // are resolved to unacceptable types, such as
                // BaseType.UNKNOWN, add the inequalities to the list of 
                // property conflicts.
                Iterator inequalities = constraintList.iterator();

                while (inequalities.hasNext()) {
                    Inequality inequality = (Inequality) inequalities.next();

                    if (!inequality.isSatisfied(_lattice.lattice())) {
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
                                + topLevel.getFullName()
                                + " on the following inequalities:");
            }
            if (unacceptable.size() > 0) {
                throw new TypeConflictException(unacceptable,
                        "Properties resolved to unacceptable types in "
                                + topLevel.getFullName()
                                + " due to the following inequalities:");
            }

            BooleanToken isTraining = (BooleanToken) trainingMode.getToken();

            compositeHelper.updateProperty(isTraining.booleanValue());

            _cleanUp();

        } catch (IllegalActionException ex) {
            // This should not happen. The exception means that
            // _checkDeclaredProperty or constraintList is called on a
            // transparent actor.
            throw new InternalErrorException(topLevel, ex,
                    "Property resolution failed because of an error "
                            + "during property inference");
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * 
     */
    private void _cleanUp() {
        _helperStore.clear();
        PropertyLattice.cleanUp();
        PropertyConstraintHelper._parser = null;
    }

    /**
     * 
     * @param typeValue
     * @return
     * @exception IllegalActionException
     */
    private ConstraintType _getConstraintType(String typeValue)
            throws IllegalActionException {
        boolean isEquals = typeValue.contains("==");
        boolean isSrc = typeValue.startsWith("src")
                || typeValue.startsWith("in") || typeValue.startsWith("parent");

        boolean hasMeet = typeValue.contains("meet");

        if (typeValue.equals("NONE")) {
            return ConstraintType.NONE;
        }
        if (hasMeet) {
            return (isSrc) ? ConstraintType.SRC_EQUALS_MEET
                    : ConstraintType.SINK_EQUALS_MEET;

        } else if (isEquals) {
            return ConstraintType.EQUALS;

        } else {
            return (isSrc) ? ConstraintType.SRC_LESS : ConstraintType.SINK_LESS;
        }
    }

    /**
     * The property lattice for this constraint solver.
     */
    private PropertyLattice _lattice = null;

    /**
    *
    */
    public static enum ConstraintType {
        SRC_LESS, SINK_LESS, SRC_EQUALS_MEET, SINK_EQUALS_MEET, EQUALS, NONE
    };
}
