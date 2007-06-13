/**
 * 
 */
package ptolemy.data.properties;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypeConflictException;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.properties.gui.PropertyConstraintSolverGUIFactory;
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

/**
 * @author eal
 *
 */
public class PropertyConstraintSolver extends Attribute {
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
        latticeFile = new FileParameter(this, "latticeFile");
        latticeFile.setExpression(
                "$PTII/ptolemy/data/properties/lattice/StaticDynamic.ldf");
        
        solveLeast = new Parameter(this, "solveLeast");
        solveLeast.setTypeEquals(BaseType.BOOLEAN);
        solveLeast.setExpression("true");

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"40\" "
                + "style=\"fill:blue\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\nresolve property.</text></svg>");

        new PropertyConstraintSolverGUIFactory(this, "_codeGeneratorGUIFactory");
    }

    

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    //public StringParameter latticeClassName;    

    /**
     * The file parameter for the lattice description file.
     */
    public FileParameter latticeFile;
    
    public Parameter solveLeast;
    

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Resolve the property values for the given top-level entity.
     * @param topLevel The given top level entity.
     */
    public void resolveProperties(CompositeEntity topLevel) 
            throws KernelException {
        if (topLevel.getContainer() != null) {
            throw new IllegalArgumentException(
                "TypedCompositeActor.resolveProperties:" +
                " The specified actor is not the top level container.");
        }

        PropertyConstraintHelper compositeHelper = _getHelper(topLevel);
        
        PropertyLattice lattice = PropertyLattices.getPropertyLattice(
                latticeFile.asFile());
        
        try {
            List conflicts = new LinkedList();
            List unacceptable = new LinkedList();

            // Check declared properties across all connections.
            //List propertyConflicts = topLevel._checkDeclaredProperties();
            //conflicts.addAll(propertyConflicts);

            // Collect and solve type constraints.
            List constraintList = compositeHelper.constraintList();

            // NOTE: To view all property constraints, uncomment these.

            /*
             Iterator constraintsIterator = constraintList.iterator();
             while (constraintsIterator.hasNext()) {
             System.out.println(constraintsIterator.next().toString());
             }
             */

            if (constraintList.size() > 0) {
                CPO cpo = lattice.lattice();
                InequalitySolver solver = new InequalitySolver(cpo);
                Iterator constraints = constraintList.iterator();

                solver.addInequalities(constraints);

                try {
                    // Find the least solution (most specific types)
                    if (((BooleanToken) solveLeast.getToken()).booleanValue()) {
                        solver.solveLeast();
                    } else {
                        solver.solveGreatest();
                    }
                } catch (InvalidStateException ex) {
                    throw new InvalidStateException(topLevel, ex,
                            "The basic property lattic was: "
                            + lattice.basicLattice());
                }

                // If some inequalities are not satisfied, or type variables
                // are resolved to unacceptable types, such as
                // BaseType.UNKNOWN, add the inequalities to the list of 
                // property conflicts.
                Iterator inequalities = constraintList.iterator();

                while (inequalities.hasNext()) {
                    Inequality inequality = (Inequality) inequalities.next();

                    if (!inequality.isSatisfied(lattice.lattice())) {
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
        } catch (IllegalActionException ex) {
            // This should not happen. The exception means that
            // _checkDeclaredProperty or constraintList is called on a
            // transparent actor.
            throw new InternalErrorException(topLevel, ex,
                    "Property resolution failed because of an error "
                            + "during property inference");
        }        
    }

    /**
     * Returns the helper that contains property information for
     * the given component.
     * @param component The given component
     * @return The associated property constraint helper.
     */    
    protected static PropertyConstraintHelper _getHelper(NamedObj component)
            throws IllegalActionException {
        if (_helperStore.containsKey(component)) {
            return (PropertyConstraintHelper) _helperStore.get(component);
        }

        String packageName = "ptolemy.data.properties";
        String componentClassName = component.getClass().getName();
        String helperClassName = componentClassName.replaceFirst("ptolemy",
                packageName);

        Class helperClass = null;

        try {
            helperClass = Class.forName(helperClassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalActionException(null, e,
                    "Cannot find helper class " + helperClassName);
        }

        Constructor constructor = null;

        try {
            constructor = helperClass.getConstructor(new Class[] { component
                                                                   .getClass() });
        } catch (NoSuchMethodException e) {
            throw new IllegalActionException(null, e,
                    "There is no constructor in " + helperClassName
                    + " which accepts an instance of "
                    + componentClassName + " as the argument.");
        }

        Object helperObject = null;

        try {
            helperObject = constructor.newInstance(new Object[] { component });
        } catch (Exception ex) {
            throw new IllegalActionException(component, ex,
                    "Failed to create the helper class for property constraints.");
        }

        if (!(helperObject instanceof PropertyConstraintHelper)) {
            throw new IllegalActionException(
                    "Cannot resolve property for this component: " + component
                    + ". Its helper class does not"
                    + " implement PropertyConstraintHelper.");
        }

        PropertyConstraintHelper castHelperObject = (PropertyConstraintHelper) helperObject;

        _helperStore.put(component, helperObject);

        return castHelperObject;
    }



    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A hash map that stores the code generator helpers associated
     *  with the actors.
     */
    private static HashMap _helperStore = new HashMap();

}
