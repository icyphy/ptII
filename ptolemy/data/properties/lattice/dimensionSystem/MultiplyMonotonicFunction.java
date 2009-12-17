/* A monotonic function that returns the correct property for the product of
   multiplication of two factors in the dimensionSystem lattice.

 Copyright (c) 2006-2009 The Regents of the University of California.
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
package ptolemy.data.properties.lattice.dimensionSystem;

import ptolemy.data.properties.Property;
import ptolemy.data.properties.lattice.MonotonicFunction;
import ptolemy.data.properties.lattice.PropertyConstraintHelper;
import ptolemy.data.properties.lattice.PropertyLattice;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// MultiplyMonotonicFunction
//// This class implements a monotonic function for the product
//// of a multiplication of two factors.  It returns the correct
//// dimensionSystem property for the product based on the properties of the factors.

/**
 A Monotonic function class for multiplication products in the dimensionSystem lattice.

 @author Charles Shelton
 @version $Id$
 @since Ptolemy II 8.1
 @Pt.ProposedRating Red (cshelton)
 @Pt.AcceptedRating Red (cshelton)
*/
public class MultiplyMonotonicFunction extends MonotonicFunction {

    /**
     * Construct a MultiplyMonotonicFunction for the dimensionSystem lattice.
     * @param factor1 The given first factor of the multiplication.
     * @param factor2 The given second factor of the multiplication.
     * @param dimensionSystemLattice The given dimensionSystem property lattice
     * @param adapter The given PropertyConstraintHelper using this monotonic function
     */
    public MultiplyMonotonicFunction(Object factor1, Object factor2,
            PropertyLattice dimensionSystemLattice, PropertyConstraintHelper adapter) {
        _factor1 = factor1;
        _factor2 = factor2;
        _lattice = dimensionSystemLattice;
        _adapter = adapter;
    }

    ///////////////////////////////////////////////////////////////
    ////                       public methods                  ////

    /** Return the monotonic function result which is the property of the product
     *  based on the properties of the two factors.
     *  @return A Property.
     *  @exception IllegalActionException
     */
  
    public Object getValue() throws IllegalActionException {

        // Get the properties of the two factors
        Property factor1Property = _adapter.getSolver().getProperty(_factor1);
        Property factor2Property = _adapter.getSolver().getProperty(_factor2);

        Property time = _lattice.getElement("TIME");
        Property position = _lattice.getElement("POSITION");
        Property speed = _lattice.getElement("SPEED");
        Property acceleration = _lattice.getElement("ACCELERATION");
        Property unitless = _lattice.getElement("UNITLESS");
        Property unknown = _lattice.getElement("UNKNOWN");
        Property top = _lattice.getElement("TOP");

        // Speed * Time = Position
        if ((factor1Property == speed && factor2Property == time)
                || (factor1Property == time && factor2Property == speed)) {
            return position;
            
        // Acceleration * Time = Speed    
        } else if ((factor1Property == acceleration && factor2Property == time)
                || (factor1Property == time && factor2Property == acceleration)) {
            return speed;
            
        // If either factor is Unknown, then the product is Unknown
        } else if (factor1Property == unknown || factor2Property == unknown) {
            return unknown;
            
        // If either factor is Top, then the product is Top
        } else if (factor1Property == top || factor2Property == top) {
            return top;
            
        // If factor1 is Unitless, then the product is the same as factor2 
        } else if (factor1Property == unitless) {
            return factor2Property;
            
        // If factor2 is Unitless, then the product is the same as factor1
        } else if (factor2Property == unitless) {
            return factor1Property;
            
        // For all other cases return Top
        } else {
            return top;
        }
    }

    public boolean isEffective() {
        return true;
    }

    public void setEffective(boolean isEffective) {
    }
    
    protected InequalityTerm[] _getDependentTerms() {
        return new InequalityTerm[] { _adapter.getPropertyTerm(_factor1),
                _adapter.getPropertyTerm(_factor2) };
    }
    
    ///////////////////////////////////////////////////////////////
    ////                       private methods                 ////
    
    // _factor1 and _factor2 are Objects because they can be any propertyable object
    // which might be IOPorts, Attributes, ASTPtNodes, etc.
    private Object _factor1;
    private Object _factor2;
    private PropertyLattice _lattice;    
    private PropertyConstraintHelper _adapter;
}

