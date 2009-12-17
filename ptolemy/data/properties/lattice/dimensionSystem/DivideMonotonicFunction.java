/* A monotonic function that returns the correct property for the quotient of
   division of a dividend and divisor in the dimensionSystem lattice.

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
//// DivideMonotonicFunction
//// This class implements a monotonic function for the quotient
//// of a division of a dividend and divisor.  It returns the correct
//// dimensionSystem property for the quotient based on the properties
//// of the dividend and divisor.

/**
 A Monotonic function class for division quotients in the dimensionSystem lattice.

 @author Charles Shelton
 @version $Id$
 @since Ptolemy II 8.1
 @Pt.ProposedRating Red (cshelton)
 @Pt.AcceptedRating Red (cshelton)
*/
public class DivideMonotonicFunction extends MonotonicFunction {

    /**
     * Construct a DivideMonotonicFunction for the dimensionSystem lattice.
     * @param dividend The given dividend of the division.
     * @param divisor The given divisor of the division.
     * @param dimensionSystemLattice The given dimensionSystem property lattice
     * @param adapter The given PropertyConstraintHelper using this monotonic function
     */
    public DivideMonotonicFunction(Object dividend, Object divisor,
            PropertyLattice dimensionSystemLattice, PropertyConstraintHelper adapter) {
        _dividend = dividend;
        _divisor = divisor;
        _lattice = dimensionSystemLattice;
        _adapter = adapter;
    }

    ///////////////////////////////////////////////////////////////
    ////                       public methods                  ////

    /** Return the monotonic function result which is the property of the quotient
     *  based on the properties of the dividend and the divisor.
     *  @return A Property.
     *  @exception IllegalActionException
     */
  
    public Object getValue() throws IllegalActionException {

        // Get the properties of the dividend and divisor
        Property dividendProperty = _adapter.getSolver().getProperty(_dividend);
        Property divisorProperty = _adapter.getSolver().getProperty(_divisor);

        Property time = _lattice.getElement("TIME");
        Property position = _lattice.getElement("POSITION");
        Property speed = _lattice.getElement("SPEED");
        Property acceleration = _lattice.getElement("ACCELERATION");
        Property unitless = _lattice.getElement("UNITLESS");
        Property unknown = _lattice.getElement("UNKNOWN");
        Property top = _lattice.getElement("TOP");

        // Speed / Time = Acceleration
        if (dividendProperty == speed && divisorProperty == time) {
            return acceleration;
            
        // Position / Time = Speed
        } else if (dividendProperty == position && divisorProperty == time) {
            return speed;
            
        // Position / Speed = Time
        } else if (dividendProperty == position && divisorProperty == speed) {
            return time;
            
        // Speed / Acceleration = Time    
        } else if (dividendProperty == speed && divisorProperty == acceleration) {
            return time;
            
        // If divisor is Unitless, then the quotient = dividend property
        } else if (divisorProperty == unitless) {
            return dividendProperty;
            
        // If either the dividend or divisor is Unknown then the quotient is Unknown
        } else if (dividendProperty == unknown || divisorProperty == unknown) {
            return unknown;
            
        // If the dividend property == the divisor property, the quotient is Unitless
        } else if (dividendProperty == divisorProperty) {
            return unitless;
            
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
        return new InequalityTerm[] { _adapter.getPropertyTerm(_dividend),
                _adapter.getPropertyTerm(_divisor) };
    }
    
    ///////////////////////////////////////////////////////////////
    ////                       private methods                 ////
    
    // _dividend and _divisor are Objects because they can be any propertyable object
    // which might be IOPorts, Attributes, ASTPtNodes, etc.
    private Object _dividend;
    private Object _divisor;
    private PropertyLattice _lattice;    
    private PropertyConstraintHelper _adapter;
}

