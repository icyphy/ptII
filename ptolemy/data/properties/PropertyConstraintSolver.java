/**
 * 
 */
package ptolemy.data.properties;

import java.io.File;
import java.io.IOException;
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

        actorConstraintType = new StringParameter(this, "actorConstraintType");
        actorConstraintType.setExpression("out == meet(in1, in2, ...)");

        connectionConstraintType = new StringParameter(this, "connectionConstraintType");
        connectionConstraintType.setExpression("sink == meet(src1, src2, ...)");

        compositeConnectionConstraintType = new StringParameter(this, "compositeConnectionConstraintType");
        compositeConnectionConstraintType.setExpression("sink == meet(src1, src2, ...)");
        
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
        compositeConnectionConstraintType.addChoice("src == meet(sink1, sink2, ...)");
        compositeConnectionConstraintType.addChoice("sink == meet(src1, src2, ...)");
        compositeConnectionConstraintType.addChoice("NONE");
                
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
        
        if (attribute == propertyLattice) {

            if (_lattice == null || 
                    !propertyLattice.stringValue().equals(_lattice.getName())) {

                if (_lattice != null) {
                    PropertyLattice.releasePropertyLattice(_lattice);
                }

                _lattice = PropertyLattice.getPropertyLattice(
                        propertyLattice.stringValue());                
            }
        } else if (attribute == actorConstraintType) {
            actorContraintTypeChanged = true;
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

        PropertyConstraintCompositeHelper compositeHelper = 
            (PropertyConstraintCompositeHelper) _lattice.getHelper(topLevel);

        if (actorContraintTypeChanged) {
            actorContraintTypeChanged = false;
            compositeHelper._changeDefaultConstraints(
                    _getConstraintType(actorConstraintType.stringValue()));
        }
        
        // FIXME: have to generate the connection every time
        // because the model structure can changed.
        // (i.e. adding or removing connections.)
        compositeHelper._setConnectionConstraintType( 
                _getConstraintType(connectionConstraintType.stringValue()), 
                _getConstraintType(compositeConnectionConstraintType.stringValue()));        
        
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

                compositeHelper.updatePortProperty();
                
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

    private ConstraintType _getConstraintType(String typeValue) throws IllegalActionException {
        boolean isEquals = typeValue.contains("==");
        boolean isSrc = typeValue.startsWith("src") || typeValue.startsWith("in");
        boolean hasMeet = typeValue.contains("meet");

        if (typeValue.equals("NONE")) {
            return ConstraintType.NONE;
        }
        if (hasMeet) {
            return (isSrc) ? ConstraintType.SRC_EQUALS_MEET : 
                ConstraintType.SINK_EQUALS_MEET;
            
        } else if (isEquals) {
            return ConstraintType.EQUALS;
            
        } else { 
            return (isSrc) ? ConstraintType.SRC_LESS :
                ConstraintType.SINK_LESS;
        }            
    }



    private boolean actorContraintTypeChanged = false;
    
    private PropertyLattice _lattice = null;
    
    /**
    *
    */
   public static enum ConstraintType { 
       SRC_LESS, 
       SINK_LESS, 
       SRC_EQUALS_MEET, 
       SINK_EQUALS_MEET, 
       EQUALS,
       NONE };

        

}

