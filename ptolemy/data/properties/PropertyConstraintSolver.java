/**
 * 
 */
package ptolemy.data.properties;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypeConflictException;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.properties.gui.PropertyConstraintSolverGUIFactory;
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
        propertyLattice = new StringParameter(this, "propertyLattice");
        propertyLattice.setExpression("staticDynamic");
                
        solvingFixedPoint = new StringParameter(this, "solvingFixedPoint");
        solvingFixedPoint.setExpression("least");

        _addChoices();

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"115\" height=\"40\" "
                + "style=\"fill:blue\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\nResolve Property.</text></svg>");

        new PropertyConstraintSolverGUIFactory(
                this, "_propertyConstraintSolverGUIFactory");
        
    }

    

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    //public StringParameter latticeClassName;    

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
            if (lattices[i].isDirectory() && !latticeName.equals("CVS")) {
                propertyLattice.addChoice(latticeName);
            }
        }
        
        solvingFixedPoint.addChoice("least");
        solvingFixedPoint.addChoice("greatest");
    }



    /** The file parameter for the lattice description file.
     */
    public StringParameter propertyLattice;
    
    /** Indicate whether to compute the least or greatest 
     *  fixed point solution.
     */
    public StringParameter solvingFixedPoint;
    

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
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
        
        if (attribute.equals(propertyLattice)) {
            _helperStore.clear();
        }
    }
    
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
        
        PropertyLattice lattice = PropertyLattice.getPropertyLattice(
                propertyLattice.stringValue());
        
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
                    // Find the greatest solution (most general type)
                    if (solvingFixedPoint.stringValue().equals("Greatest")) {
                        solver.solveGreatest();
                    } else {
                        solver.solveLeast();
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
    public PropertyConstraintHelper _getHelper(NamedObj component)
            throws IllegalActionException {
        if (_helperStore.containsKey(component)) {
            return (PropertyConstraintHelper) _helperStore.get(component);
        }
        
        PropertyLattice lattice = PropertyLattice
                .getPropertyLattice(propertyLattice.stringValue());
        
        String packageName = "ptolemy.data.properties.lattice." 
            + propertyLattice.stringValue();        

        Class componentClass = component.getClass();

        Class helperClass = null;
        while (helperClass == null) {
            try {
                
                // FIXME: Is this the right error message?
                if (!componentClass.getName().contains("ptolemy")) {
                    throw new IllegalActionException("There is no property helper "
                            + " for " + component.getClass());
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
        Method initializeMethod = null;
        
        try {
            constructor = helperClass.getConstructor(
                    new Class[] { componentClass, PropertyLattice.class });
            
        } catch (NoSuchMethodException e) {
            throw new IllegalActionException(null, e,
                    "Cannot find constructor method in " 
                    + helperClass.getName());
        }

        Object helperObject = null;

        try {
            helperObject = constructor.newInstance(new Object[] { component, lattice });
            
        } catch (Exception ex) {
            throw new IllegalActionException(component, ex,
                    "Failed to create the helper class for property constraints.");
        }

        if (!(helperObject instanceof PropertyConstraintHelper)) {
            throw new IllegalActionException(
                    "Cannot resolve property for this component: "
                    + component + ". Its helper class does not"
                    + " implement PropertyConstraintHelper.");
        }

        PropertyConstraintHelper castHelperObject = 
            (PropertyConstraintHelper) helperObject;

        castHelperObject.setSolver(this);
        
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
