/* A concept in the unitSystem ontology for a specific unit for
 * a specific physical dimension.

 Copyright (c) 2011 The Regents of the University of California.
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

import java.util.HashMap;
import java.util.Map;

import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.Ontology;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// DerivedUnitConcept

/** A concept in the unitSystem ontology for a specific unit for
 *  a specific physical dimension.
 *  
 *  A unit for a derived dimension is defined by the physical dimension it
 *  measures and the multiplication factor and offset values required to convert
 *  a value in its unit measurement to a unit measurement in the SI unit for this
 *  dimension.
 *  
@see DerivedDimensionRepresentativeConcept
@author Charles Shelton
@version $Id$
@since Ptolemy II 8.1
@Pt.ProposedRating Red (cshelton)
@Pt.AcceptedRating Red (cshelton)
*/
public class DerivedUnitConcept extends UnitConcept {
    
    ///////////////////////////////////////////////////////////////////
    ////             public constructors/factories                 ////
    
    /** Create a new derived unit concept, belonging to the given
     *  ontology, with an automatically generated name.
     * 
     *  @param ontology The ontology to which this concept belongs.
     *  @param representative The finite concept that represents where the infinite
     *   token concepts belong in the ontology lattice.
     *  @param unitInfo The token value for this FlatTokenInfiniteConcept.
     *  @return The newly created RecordConcept.
     *  @throws IllegalActionException If the base class throws it.
     */
    public static DerivedUnitConcept createDerivedUnitConcept(
            Ontology ontology, DerivedDimensionRepresentativeConcept representative,
            RecordToken unitInfo)
                throws IllegalActionException {
        try {
            return new DerivedUnitConcept(ontology, representative, unitInfo);
        } catch (NameDuplicationException e) {
            throw new IllegalActionException(
                    "Name conflict with automatically generated infinite concept name.\n"
                  + "This should never happen."
                  + "Original exception:" + e.toString());
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                    public variables                       ////
    
    /** The conversion information label for the unit record token information
     *  when constructing a new DerivedUnitConcept.
     */
    private static final String derivedUnitConversionLabel = "DerivedConversion";
    
    /** The array of labels for the unit record token information when constructing
     *  a new DerivedUnitConcept.
     */
    public static final String[] derivedUnitRecordLabelArray = new String[]{
                            unitNameLabel, derivedUnitConversionLabel};

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
     *  @throws NameDuplicationException Should never be thrown.
     *  @throws IllegalActionException If the base class throws it.
     */
    protected DerivedUnitConcept(Ontology ontology,
            DerivedDimensionRepresentativeConcept representative,
            RecordToken unitInfo)
                throws IllegalActionException, NameDuplicationException {
        super(ontology, representative, unitInfo);
        _componentUnits = new HashMap<DimensionRepresentativeConcept, UnitConcept[]>();
        _setComponentUnitsMap(unitInfo, representative);
        _setConversionFactors(unitInfo);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                    private methods                        ////
    
    /** Apply the individual unit conversion factors and offsets for each
     *  component unit to the conversion factor and offset for the derived unit.
     */
    private void _applyComponentUnitConversionFactors() {
        Map<DimensionRepresentativeConcept, Integer> componentDimensions =
            ((DerivedDimensionRepresentativeConcept) _representative).getComponentDimensions();
        
        for (DimensionRepresentativeConcept dimension : componentDimensions.keySet()) {
            int dimensionExponent = componentDimensions.get(dimension).intValue();
            UnitConcept[] unitsArray = _componentUnits.get(dimension);
            for (UnitConcept unit : unitsArray) {
                if (dimensionExponent > 0) {
                    _unitFactor *= unit._unitFactor;
                } else if (dimensionExponent < 0) {
                    _unitFactor /= unit._unitFactor;
                }
                
                // FIXME: What do we do for units that have an offset as well as a factor?
            }
        }
    }
    
    /** Get the array of component unit names for the specified component
     *  dimension from the unit record token for this derived unit concept.
     *  @param derivedUnitRecord The RecordToken that specifies the component
     *   dimensions and units that make up this derived unit.
     *  @param dimensionName The specific component dimension name from which
     *   to get the array of unit names.
     *  @return The array of unit names for this component dimension as an
     *   array of StringTokens.
     *  @throws IllegalActionException Thrown if the units array cannot be found
     *   or it is invalid.
     */
    private Token[] _getUnitsArray(RecordToken derivedUnitRecord, String dimensionName) throws IllegalActionException {
        // First check to see if the record token has a label that matches
        // the given dimensionName.        
        Token unitsArrayToken = derivedUnitRecord.get(dimensionName);
        
        // If the unitsArrayToken is not found, the dimensionName might be
        // specified by a reference name used in the parent
        // DerivedDimensionRepresentativeConcept. Try to find the unitsArrayToken
        // based on that reference name.
        if (unitsArrayToken == null) {
            String referenceName = ((DerivedDimensionRepresentativeConcept)
                    _representative).getReferenceNameByDimensionName(dimensionName);
            if (referenceName != null) {
                unitsArrayToken = derivedUnitRecord.get(referenceName);
            }
        }
        
        if (unitsArrayToken == null) {
            throw new IllegalActionException(this, "Could not find the units " +
            		"information for the " + dimensionName + " dimension.");
        } else {
            if (unitsArrayToken instanceof ArrayToken &&
                    ((ArrayToken) unitsArrayToken).getElementType().
                        equals(BaseType.STRING)) {
                return ((ArrayToken) unitsArrayToken).arrayValue();
            } else {
                throw new IllegalActionException(this, "Invalid units array " +
                    "for the " + dimensionName + " dimension: " +
                    unitsArrayToken);
            }
        }
    }
    
    /** Set the component units for the derived unit based on the given
     *  record token that specifies the component units for this derived unit.
     *  @param derivedUnitRecord The record token that contains the specified
     *   unit conversion information.
     *  @param unitDimensionRepresentative The dimension representative concept
     *   for this derived unit concept.
     *  @throws IllegalActionException Thrown if the record token has invalid
     *   unit conversion specifications.
     */
    private void _setComponentUnitsMap(RecordToken derivedUnitRecord,
            DerivedDimensionRepresentativeConcept unitDimensionRepresentative)
                throws IllegalActionException {
        Map<DimensionRepresentativeConcept, Integer> componentDimensions =
            unitDimensionRepresentative.getComponentDimensions();
        
        for (DimensionRepresentativeConcept dimension : componentDimensions.keySet()) {
            String dimensionName = dimension.getName();
            Token[] unitsStringTokens = _getUnitsArray(derivedUnitRecord, dimensionName);
            int dimensionExponent = componentDimensions.get(dimension).intValue();
            int dimensionExponentAbsValue = Math.abs(dimensionExponent);

            if (unitsStringTokens.length == dimensionExponentAbsValue) {
                UnitConcept[] unitsArray = new UnitConcept[unitsStringTokens.length];

                int index = 0;
                for (Token unitStringToken : unitsStringTokens) {
                    String unitName = ((StringToken) unitStringToken).stringValue();
                    Concept unit = getOntology().getConceptByString(dimensionName + "_" + unitName);
                    if (unit instanceof UnitConcept) {
                        unitsArray[index++] = (UnitConcept) unit;
                    } else {
                        throw new IllegalActionException(this, "Invalid " +
                                "unit concept: " + unit);
                    }
                }
                _componentUnits.put(dimension, unitsArray);
            } else {
                throw new IllegalActionException(this, "The component " +
                        "dimension " + dimension + " has an exponent of "
                        + dimensionExponent + " so its units array " +
                        "should have " + dimensionExponentAbsValue +
                        " elements but it does not.");
            }
        }
    }
    
    /** Set the unit conversion factor and offset for the derived unit based on
     *  the specified factor and offset and the component unit conversion
     *  factors that comprise this derived unit.
     *  @param derivedUnitRecord The record token that contains the specified
     *   unit conversion information.
     *  @throws IllegalActionException Thrown if the unit factor or unit
     *   offset in the record token is invalid.
     */
    private void _setConversionFactors(RecordToken derivedUnitRecord)
            throws IllegalActionException {
        Token unitFactorToken = derivedUnitRecord.get(unitFactorLabel);
        if (unitFactorToken == null) {
            _unitFactor = 1.0;
        } else if (unitFactorToken instanceof DoubleToken) {
            _unitFactor = ((DoubleToken) unitFactorToken).doubleValue();
        } else {
            throw new IllegalActionException (this, "Invalid unit conversion " +
            		"factor: " + unitFactorToken);
        }
        
        Token unitOffsetToken = derivedUnitRecord.get(unitOffsetLabel);
        if (unitOffsetToken == null) {
            _unitOffset = 0.0;
        } else if (unitOffsetToken instanceof DoubleToken) {
            _unitOffset = ((DoubleToken) unitOffsetToken).doubleValue();
        } else {
            throw new IllegalActionException (this, "Invalid unit conversion " +
            		"offset: " + unitOffsetToken);
        }
        
        _applyComponentUnitConversionFactors();
    }

    ///////////////////////////////////////////////////////////////////
    ////                    private variables                      ////
    
    /** Map that links the component dimensions to the array of component
     *  units for this derived unit. The unit array is the size of the exponent
     *  for that component dimension.
     */
    private Map<DimensionRepresentativeConcept, UnitConcept[]> _componentUnits;
}
