/** A base class representing a property constraint helper.

 Copyright (c) 1997-2006 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY


 */
package ptolemy.data.properties.lattice;

import java.util.ArrayList;
import java.util.List;

import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.properties.Property;
import ptolemy.data.properties.PropertySolver;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

//////////////////////////////////////////////////////////////////////////
//// PropertyConstraintHelper

/**
 A base class representing a property constraint helper.

 @author Man-Kit Leung, Thomas Mandl, Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class PropertyConstraintASTNodeHelper extends PropertyConstraintHelper {

    /** 
     * Construct the property constraint helper associated
     * with the given component.
     * @param component The associated component.
     * @exception IllegalActionException Thrown if 
     *  PropertyConstraintHelper(NamedObj, PropertyLattice, boolean)
     *  throws it. 
     */
    public PropertyConstraintASTNodeHelper(PropertySolver solver,
            ASTPtRootNode node) throws IllegalActionException {
        this(solver, node, true);
    }

    /**
     * Construct the property constraint helper for the given
     * component and property lattice.
     * @param component The given component.
     * @param lattice The given property lattice.
     * @param useDefaultConstraints Indicate whether this helper
     *  uses the default actor constraints. 
     * @exception IllegalActionException Thrown if the helper cannot
     *  be initialized.
     */
    public PropertyConstraintASTNodeHelper(PropertySolver solver,
            ASTPtRootNode node, boolean useDefaultConstraints)
            throws IllegalActionException {

        super(solver, node, useDefaultConstraints);
    }

    /** Return the constraints of this component.  The constraints is
     *  a list of inequalities. This base class returns a empty list.
     *  @return A list of Inequality.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public List constraintList() throws IllegalActionException {
        List constraints = new ArrayList();
        return constraints;
    }

    /**
     * Return the property value associated with the given property lattice
     * and the given port.  
     * @param astNode The given port.
     * @param lattice The given lattice.
     * @return The property value of the given port. 
     */
    public Property getProperty(Object astNode) {
        if (astNode == _component) {
            return (Property) _resolvedProperties.get(astNode);
        } else {
            try {
                return getSolver().getHelper((ASTPtRootNode) astNode)
                        .getProperty(astNode);
            } catch (IllegalActionException e) {

                throw new InternalErrorException("This should happen!");
            }
        }
    }

    /**
     * Return the property term from the given port and lattice.
     * @param port The given port.
     * @param lattice The given property lattice.
     * @return The property term of the given port.
     * @exception IllegalActionException 
     */
    public InequalityTerm getPropertyTerm(Object object)
            throws IllegalActionException {

        if (object instanceof InequalityTerm) {
            return (InequalityTerm) object;
        }

        if (object == _component) {

            return super.getPropertyTerm(object);
        } else {
            return getSolver().getHelper(object).getPropertyTerm(object);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Return a list of property-able NamedObj contained by
     * the component. All ports and parameters are considered
     * property-able.
     * @return The list of property-able named object.
     */
    protected List _getPropertyables() {
        List list = new ArrayList();
        list.add(_component);
        return list;
    }
}
