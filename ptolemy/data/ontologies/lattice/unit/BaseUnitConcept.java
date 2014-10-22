/* A concept in the unitSystem ontology for a specific unit for
 * a specific physical dimension.

 Copyright (c) 2011-2014 The Regents of the University of California.
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
package ptolemy.data.ontologies.lattice.unit;

import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.ontologies.Ontology;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// BaseUnitConcept

/** A concept in the unitSystem ontology for a specific unit for
 *  a specific physical dimension.
 *
 *  A unit for a base dimension is defined by the physical dimension it
 *  measures and the multiplication factor and offset values required to convert
 *  a value in its unit measurement to a unit measurement in the SI unit for this
 *  dimension.
 *
 *  For example, to represent the units for measuring temperature in degrees
 *  Fahrenheit, the multiplication factor and offset are specified as what is
 *  needed to convert Fahrenheit to Kelvin:
 *  <ul>
 *  <li>unitFactor = 5.0/9.0
 *  <li>unitOffset = 459.67
 *  </ul>
 *
 *  <p>To convert a temperature measurement value in degrees F to degrees K, apply
 *  the formula:<br>
 *  <code>value in K = (value in F + unitOffset) * unitFactor</code>
 *  </p>
 *  <p>Inversely, to convert a measurement from the SI unit (K) to this unit (F),
 *  apply this formula:<br>
 *  <code>value in F = (value in K) / unitFactor - unitOffset</code>
 *  </p>
 *  <p>So far temperature is the only dimension that requires an offset. All
 *  the other dimensions only require a multiplication
 *  factor, so their offset is always zero. For example,
 *  To represent the position dimension units in kilometers (km):</p>
 *  <ul>
 *  <li>unitFactor = 1000.0
 *  <li>unitOffset = 0.0
 *  </ul>
 *  <p>The SI unit for position is meters (m) so the kilometers unitFactor is 1000.0
 *  and there is zero offset.<p>
 *  <p>This class is an infinite concept so that an arbitrary number of different
 *  unit measurements can be represented for any physical dimension. All that is
 *  required is specify the name of units and the multiplication factor and offset
 *  needed to convert the unit to the SI unit for that dimension.</p>
@see BaseDimensionRepresentativeConcept
@author Charles Shelton
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (cshelton)
@Pt.AcceptedRating Red (cshelton)
 */
public class BaseUnitConcept extends UnitConcept {

    ///////////////////////////////////////////////////////////////////
    ////             public constructors/factories                 ////

    /** Create a new base unit concept, belonging to the given
     *  ontology, with an automatically generated name.
     *
     *  @param ontology The ontology to which this concept belongs.
     *  @param representative The finite concept that represents where the infinite
     *   token concepts belong in the ontology lattice.
     *  @param unitInfo The token value for this BaseUnitConcept.
     *  @return The newly created RecordConcept.
     *  @exception IllegalActionException If the base class throws it.
     */
    public static BaseUnitConcept createBaseUnitConcept(Ontology ontology,
            BaseDimensionRepresentativeConcept representative,
            RecordToken unitInfo) throws IllegalActionException {
        try {
            return new BaseUnitConcept(ontology, representative, unitInfo);
        } catch (NameDuplicationException e) {
            throw new IllegalActionException(
                    "Name conflict with automatically generated infinite concept name.\n"
                            + "This should never happen."
                            + "Original exception:" + e.toString());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                    protected constructors                 ////

    /** Create a new BaseUnitConcept, belonging to the given
     *  ontology.
     *
     *  @param ontology The ontology to which this concept belongs.
     *  @param representative The finite concept that represents the physical
     *   dimension for the set infinite concepts that represent units for
     *   this dimension in the ontology lattice.
     *  @param unitInfo The record token value that has the name and scale
     *   factor information for this unit.
     *  @exception NameDuplicationException Should never be thrown.
     *  @exception IllegalActionException If the base class throws it.
     */
    protected BaseUnitConcept(Ontology ontology,
            BaseDimensionRepresentativeConcept representative,
            RecordToken unitInfo) throws IllegalActionException,
            NameDuplicationException {
        super(ontology, representative, unitInfo);

        Token unitFactor = unitInfo.get(UnitConversionInfo.unitFactorLabel);
        if (unitFactor instanceof ScalarToken) {
            _unitFactor = (ScalarToken) unitFactor;
        } else {
            throw new IllegalActionException(this,
                    "Invalid unit factor value (must be a scalar value): "
                            + unitFactor);
        }

        Token unitOffset = unitInfo.get(UnitConversionInfo.unitOffsetLabel);
        if (unitOffset == null) {
            _unitOffset = DoubleToken.ZERO;
        } else if (unitOffset instanceof ScalarToken) {
            _unitOffset = (ScalarToken) unitOffset;
        } else {
            throw new IllegalActionException(this,
                    "Invalid unit offset value (must be a scalar value): "
                            + unitOffset);
        }
    }
}
