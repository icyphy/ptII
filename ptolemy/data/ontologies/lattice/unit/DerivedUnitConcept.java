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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.ScalarToken;
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
        UnitConversionInfo.unitNameLabel, derivedUnitConversionLabel};
    
    ///////////////////////////////////////////////////////////////////
    ////                    public methods                         ////
    
    /** Derive a map of base dimensions to lists of units that represents the
     *  given component units map and dimension map.
     *  @param componentUnitsMap The map of dimensions to lists of units from
     *   which the base component units map will be derived.
     *  @param dimensionMap The map of dimensions to exponents from which
     *   base dimension map will be derived.
     *  @param baseDimensionMap The map of base dimensions to exponents needed
     *   for creating the base component units map.
     *  @return The map of base dimensions to exponents that composes the given
     *   dimension map.
     *  @throws IllegalActionException Thrown if an invalid dimension concept
     *   is found.
     */
    public static Map<BaseDimensionRepresentativeConcept, List<BaseUnitConcept>>
        deriveComponentBaseUnitsMap(Map<DimensionRepresentativeConcept,
            List<UnitConcept>> componentUnitsMap,
            Map<DimensionRepresentativeConcept, Integer> dimensionMap,
            Map<BaseDimensionRepresentativeConcept, Integer> baseDimensionMap)
                throws IllegalActionException {
        
        Map<BaseDimensionRepresentativeConcept, List<BaseUnitConcept>>
            baseComponentUnits =
                new HashMap<BaseDimensionRepresentativeConcept,
                    List<BaseUnitConcept>>();
        
        Map<BaseDimensionRepresentativeConcept, List<BaseUnitConcept>[]>
            baseComponentUnitsSeparateExponents =
                _deriveComponentBaseUnitsSeparateExponentsMap(
                        componentUnitsMap, dimensionMap, baseDimensionMap);
            
        for (BaseDimensionRepresentativeConcept baseDimension : baseDimensionMap.keySet()) {
            int exponent = baseDimensionMap.get(baseDimension).intValue();
            List<BaseUnitConcept> positiveExponentUnitList = baseComponentUnitsSeparateExponents.get(baseDimension)[POSITIVE_EXPONENT_INDEX];
            List<BaseUnitConcept> negativeExponentUnitList = baseComponentUnitsSeparateExponents.get(baseDimension)[NEGATIVE_EXPONENT_INDEX];
            List<BaseUnitConcept> composedUnitList = null;
            
            if (exponent > 0) {
                composedUnitList = _removeMatchingListElements(positiveExponentUnitList, negativeExponentUnitList);
            } else if (exponent < 0) {
                composedUnitList = _removeMatchingListElements(negativeExponentUnitList, positiveExponentUnitList);
            } else {
                throw new IllegalActionException("Exponent value should never be " +
                		"zero because then it would not have an entry " +
                		"in the map.");
            }
            if (composedUnitList.size() == Math.abs(exponent)) {
                // Sort the base component units lists so that we can compare the
                // lists for equality when trying to find the correct unit concepts.
                Collections.sort(composedUnitList, new BaseUnitComparator());
                baseComponentUnits.put(baseDimension, composedUnitList);
            } else {
                throw new IllegalActionException("Base component unit list " +
                                "for the base dimension " + baseDimension +
                		" must be the same length as the absolute " +
                		"value of the dimension map exponent: list " +
                		"size: " + composedUnitList.size() +
                		", exponent value: " + exponent);
            }
        }
        
        return baseComponentUnits;
    }
    
    /** Find the DerivedUnitConcept that contains the given dimension and
     *  component unit maps and the given unit conversion factor, or null if the
     *  unit doesn't exist.
     *  @param dimensionMap The map of component dimensions to their exponents
     *   for this unit dimension.
     *  @param componentUnitsMap The map that links the component dimensions
     *   to a list component units for that dimension.
     *  @param unitFactor The unit factor for the UnitConcept to be found.
     *  @param unitOntology The ontology for the unit dimensions.
     *  @return The DerivedUnitConcept that contains the dimension and component
     *   maps, or the top of the lattice if no matching unit is found.
     *  @throws IllegalActionException Thrown if there is a problem getting
     *   the unit dimension.
     */
    public static Concept findUnitByComponentMapsAndUnitFactor(
            Map<DimensionRepresentativeConcept, Integer> dimensionMap,
            Map<DimensionRepresentativeConcept, List<UnitConcept>> componentUnitsMap,
            ScalarToken unitFactor,
            Ontology unitOntology) throws IllegalActionException {
        
        // If the dimension map has only one dimension with an exponent of
        // one, just return the corresponding unit in the component units map.
        if (_hasSingleDimensionWithExponentOne(dimensionMap)) {
            return _getSingleUnitConceptInComponentUnitsMap(componentUnitsMap);
        }
        
        Map<BaseDimensionRepresentativeConcept, Integer> baseDimensionMap =
            DerivedDimensionRepresentativeConcept.deriveComponentBaseDimensionsMap(dimensionMap);
        Map<BaseDimensionRepresentativeConcept, List<BaseUnitConcept>> baseUnitsMap =
            deriveComponentBaseUnitsMap(componentUnitsMap, dimensionMap, baseDimensionMap);
        
        if (_hasSingleDimensionWithExponentOne(baseDimensionMap)) {
            return _getSingleUnitConceptInComponentUnitsMap(baseUnitsMap);
        }
        
        List<DerivedDimensionRepresentativeConcept> candidateDimensions =
            _findMatchingDimensions(dimensionMap, baseDimensionMap, unitOntology);
        if (candidateDimensions.isEmpty()) {
            return null;
        } else {
            List<UnitConcept> candidateUnits =
                _findMatchingUnits(componentUnitsMap, baseUnitsMap,
                        candidateDimensions);
            return _findUnitWithUnitFactor(candidateUnits, unitFactor,
                    unitOntology);
        }
    }
    
    /** Get the base component units map for this DerivedUnitConcept. This map links
     *  the base component dimensions with the list of base units for each dimension.
     *  @return The component units map.
     */
    public Map<BaseDimensionRepresentativeConcept, List<BaseUnitConcept>> getComponentBaseUnits() {
        return new HashMap<BaseDimensionRepresentativeConcept, List<BaseUnitConcept>>(_componentBaseUnits);
    }
    
    /** Get the component units map for this DerivedUnitConcept. This map links
     *  the component dimensions with the list of units for each dimension.
     *  @return The component units map.
     */
    public Map<DimensionRepresentativeConcept, List<UnitConcept>> getComponentUnits() {
        return new HashMap<DimensionRepresentativeConcept, List<UnitConcept>>(_componentUnits);
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
     *  @throws NameDuplicationException Should never be thrown.
     *  @throws IllegalActionException If the base class throws it.
     */
    protected DerivedUnitConcept(Ontology ontology,
            DerivedDimensionRepresentativeConcept representative,
            RecordToken unitInfo)
                throws IllegalActionException, NameDuplicationException {
        super(ontology, representative, unitInfo);
        _componentUnits = new HashMap<DimensionRepresentativeConcept, List<UnitConcept>>();
        _componentBaseUnits = null;
        _setComponentUnitsMap(unitInfo, representative);
        _setConversionFactors(unitInfo);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                    private methods                        ////
    
    /** Apply the individual unit conversion factors and offsets for each
     *  component unit to the conversion factor and offset for the derived unit.
     *  @throws IllegalActionException Thrown if there is a problem getting
     *   the component dimensions.
     */
    private void _applyComponentUnitConversionFactors() throws IllegalActionException {
        Map<DimensionRepresentativeConcept, Integer> componentDimensions =
            ((DerivedDimensionRepresentativeConcept) _representative).getComponentDimensions();
        
        for (DimensionRepresentativeConcept dimension : componentDimensions.keySet()) {
            int dimensionExponent = componentDimensions.get(dimension).intValue();
            List<UnitConcept> unitsList = _componentUnits.get(dimension);
            for (UnitConcept unit : unitsList) {
                if (dimensionExponent > 0) {
                    _unitFactor = (ScalarToken) _unitFactor.multiply(unit._unitFactor);
                } else if (dimensionExponent < 0) {
                    _unitFactor = (ScalarToken) _unitFactor.divide(unit._unitFactor);
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
                List<UnitConcept> unitsList = new ArrayList<UnitConcept>();

                for (Token unitStringToken : unitsStringTokens) {
                    String unitName = ((StringToken) unitStringToken).stringValue();
                    Concept unit = getOntology().getConceptByString(dimensionName + "_" + unitName);
                    if (unit instanceof UnitConcept) {
                        unitsList.add((UnitConcept) unit);
                    } else {
                        throw new IllegalActionException(this, "Invalid " +
                                "unit concept: " + unit);
                    }
                }
                _componentUnits.put(dimension, unitsList);
            } else {
                throw new IllegalActionException(this, "The component " +
                        "dimension " + dimension + " has an exponent of "
                        + dimensionExponent + " so its units array " +
                        "should have " + dimensionExponentAbsValue +
                        " elements but it does not.");
            }
        }
        
        _componentBaseUnits = deriveComponentBaseUnitsMap(_componentUnits,
                componentDimensions,
                unitDimensionRepresentative.getComponentBaseDimensions());
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                    private methods                        ////
    
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
        Token unitFactorToken = derivedUnitRecord.get(UnitConversionInfo.unitFactorLabel);
        if (unitFactorToken == null) {
            _unitFactor = DoubleToken.ONE;
        } else if (unitFactorToken instanceof ScalarToken) {
            _unitFactor = (ScalarToken) unitFactorToken;
        } else {
            throw new IllegalActionException (this, "Invalid unit conversion " +
            		"factor: " + unitFactorToken);
        }
        
        Token unitOffsetToken = derivedUnitRecord.get(UnitConversionInfo.unitOffsetLabel);
        if (unitOffsetToken == null) {
            _unitOffset = DoubleToken.ZERO;
        } else if (unitOffsetToken instanceof DoubleToken) {
            _unitOffset = (ScalarToken) unitOffsetToken;
        } else {
            throw new IllegalActionException (this, "Invalid unit conversion " +
            		"offset: " + unitOffsetToken);
        }
        
        _applyComponentUnitConversionFactors();
    }
    
    /** Update the base component units map with the given base unit and
     *  exponent value. For each base dimension in the map, two lists of base
     *  units are kept. One for all the units with positive exponents, and
     *  one for all the units with negative exponents.
     *  @param baseComponentUnits The base component units map that will be updated.
     *  @param baseUnit The base unit concept to be added to the map.
     *  @param exponentValue The exponent value for the base unit concept to be
     *   added to the map.
     *  @param baseDimensionMapExponentValue The exponent value of the base
     *   dimension for the base unit to be added to the map.
     *  @throws IllegalActionException Thrown if the exponentValue passed in is
     *   zero, because it should be either positive or negative.
     */
    private static void _addBaseUnit(Map<BaseDimensionRepresentativeConcept,
            List<BaseUnitConcept>[]> baseComponentUnits, BaseUnitConcept baseUnit,
            int exponentValue, int baseDimensionMapExponentValue)
        throws IllegalActionException {
        
        BaseDimensionRepresentativeConcept baseDimension =
            (BaseDimensionRepresentativeConcept) baseUnit.getDimension();
        List<BaseUnitConcept>[] arrayOfBaseUnitLists = baseComponentUnits.get(baseDimension);
        
        if (baseDimensionMapExponentValue != 0) {
            if (arrayOfBaseUnitLists == null) {
                arrayOfBaseUnitLists = (ArrayList<BaseUnitConcept>[])
                    new ArrayList[]{new ArrayList<BaseUnitConcept>(),
                                    new ArrayList<BaseUnitConcept>()};
            }
            
            if (exponentValue > 0) {
                arrayOfBaseUnitLists[POSITIVE_EXPONENT_INDEX].add(baseUnit);
            } else if (exponentValue < 0){
                arrayOfBaseUnitLists[NEGATIVE_EXPONENT_INDEX].add(baseUnit);
            } else {
                throw new IllegalActionException("Exponent value should not " +
                		"be zero since it was taken from a " +
                		"dimension in the map.");
            }
            
            baseComponentUnits.put(baseDimension, arrayOfBaseUnitLists);
        }
    }

    /** Update the base component units map with the set of base units from
     *  another derived units' base component units map.
     *  @param baseUnitsMap The base component units map to be updated.
     *  @param baseUnitsMapFromDerivedUnit The base component units map from another
     *   derived unit concept to be added to the baseUnitsMap.
     */
    private static void _addDerivedUnit(Map<BaseDimensionRepresentativeConcept,
            List<BaseUnitConcept>[]> baseUnitsMap,
            Map<BaseDimensionRepresentativeConcept,
            List<BaseUnitConcept>[]> baseUnitsMapFromDerivedUnit) {
        
        for (BaseDimensionRepresentativeConcept baseDimension : baseUnitsMapFromDerivedUnit.keySet()) {
            List<BaseUnitConcept>[] arrayOfBaseUnitsListsFromDerivedUnit =
                baseUnitsMapFromDerivedUnit.get(baseDimension);
            List<BaseUnitConcept>[] arrayOfBaseUnitsLists =
                baseUnitsMap.get(baseDimension);
            
            if (arrayOfBaseUnitsLists == null) {
                arrayOfBaseUnitsLists = arrayOfBaseUnitsListsFromDerivedUnit;
            } else {
                arrayOfBaseUnitsLists[POSITIVE_EXPONENT_INDEX].addAll(
                        arrayOfBaseUnitsListsFromDerivedUnit[POSITIVE_EXPONENT_INDEX]);
                arrayOfBaseUnitsLists[NEGATIVE_EXPONENT_INDEX].addAll(
                        arrayOfBaseUnitsListsFromDerivedUnit[NEGATIVE_EXPONENT_INDEX]);                
            }
            baseUnitsMap.put(baseDimension, arrayOfBaseUnitsLists);
        }
        
    }

    /** Recursively construct the base component units map for the given
     *  component units map and return it. Each value in the map is a
     *  two-element array of lists of BaseUnitConcepts that indicate the
     *  positive and negative exponent units for the base dimension.
     *  @param componentUnitsMap The component units map from which to derive
     *   the base component units map.
     *  @param dimensionMap The dimension map for the component units map.
     *  @param baseDimensionMap The already calculated base dimension map for
     *   the dimension map input.
     *  @return The base component units map with separate lists of
     *   positive and negative exponent units.
     *  @throws IllegalActionException Thrown if unit concepts that are
     *   neither BaseUnitConcepts or DerivedUnitConcepts are found.
     */
    private static Map<BaseDimensionRepresentativeConcept, List<BaseUnitConcept>[]>
        _deriveComponentBaseUnitsSeparateExponentsMap(Map<DimensionRepresentativeConcept,
                List<UnitConcept>> componentUnitsMap,
                Map<DimensionRepresentativeConcept, Integer> dimensionMap,
                Map<BaseDimensionRepresentativeConcept, Integer> baseDimensionMap)
                    throws IllegalActionException {
        
        Map<BaseDimensionRepresentativeConcept, List<BaseUnitConcept>[]>
            baseComponentUnitsSeparateExponents =
                new HashMap<BaseDimensionRepresentativeConcept,
                    List<BaseUnitConcept>[]>();
        
        for (DimensionRepresentativeConcept dimension : componentUnitsMap.keySet()) {
            List<UnitConcept> unitsList = componentUnitsMap.get(dimension);
            int exponent = dimensionMap.get(dimension).intValue();
            for (UnitConcept unit : unitsList) {
                if (unit instanceof BaseUnitConcept) {
                    Integer baseExponentInteger = baseDimensionMap.get(dimension);
                    int baseDimensionMapExponent = 0;
                    if (baseExponentInteger != null) {
                        baseDimensionMapExponent = baseExponentInteger;
                    }
                    _addBaseUnit(baseComponentUnitsSeparateExponents, (BaseUnitConcept) unit,
                            exponent, baseDimensionMapExponent);
                } else if (unit instanceof DerivedUnitConcept) {
                    DerivedDimensionRepresentativeConcept unitDimension =
                        (DerivedDimensionRepresentativeConcept) unit.getDimension();
                    Map<DimensionRepresentativeConcept, Integer> unitDimensionMap =
                        unitDimension.getComponentDimensions();
                    Map<BaseDimensionRepresentativeConcept, List<BaseUnitConcept>[]>
                        derivedUnitBaseComponentSeparateExponents =
                            _deriveComponentBaseUnitsSeparateExponentsMap(
                                    ((DerivedUnitConcept) unit).getComponentUnits(),
                                    unitDimensionMap,
                                    DerivedDimensionRepresentativeConcept.
                                        deriveComponentBaseDimensionsMap(unitDimensionMap));
                        _addDerivedUnit(baseComponentUnitsSeparateExponents,
                                derivedUnitBaseComponentSeparateExponents);
                } else {
                    throw new IllegalActionException("A unit concept must be " +
                            "either a BaseUnitConcept " +
                            "or a DerivedUnitConcept.");
                }
            }
        }        
        return baseComponentUnitsSeparateExponents;
    }

    /** Find all the matching dimension concepts with the given dimension map. 
     *  @param dimensionMap The map of component dimensions to their exponents
     *   for this unit dimension.
     *  @param baseDimensionMap The already calculated base dimension map for
     *   the dimension map input.
     *  @param unitOntology The ontology for the unit dimensions.
     *  @return The List of found matching DerivedDimensionRepresentativeConcepts.
     *  @throws IllegalActionException Thrown if there is a problem getting the
     *   dimension concept.
     */
    private static List<DerivedDimensionRepresentativeConcept>
        _findMatchingDimensions(Map<DimensionRepresentativeConcept, Integer>
            dimensionMap, Map<BaseDimensionRepresentativeConcept, Integer> baseDimensionMap,
            Ontology unitOntology) throws IllegalActionException {
        
        List<DerivedDimensionRepresentativeConcept> foundDimensions =
            new ArrayList<DerivedDimensionRepresentativeConcept>();
        
        List<DerivedDimensionRepresentativeConcept> allDimensions =
            unitOntology.entityList(DerivedDimensionRepresentativeConcept.class);        
        for (DerivedDimensionRepresentativeConcept dimension : allDimensions) {
            Map<DimensionRepresentativeConcept, Integer> componentDimensions =
                dimension.getComponentDimensions();
            
            if (dimensionMap.equals(componentDimensions) ||
                    baseDimensionMap.equals(dimension.getComponentBaseDimensions())) {
                foundDimensions.add(dimension);
            }
        }        
        return foundDimensions;
    }
    
    /** Find all the DerviedUnitConcepts in the given dimension that contains the
     *  given component units map.
     *  @param componentUnitsMap The map that links the component dimensions
     *   to a list component units for that dimension.
     *  @param baseComponentUnits The map of base component unit concepts for the
     *   input componentUnitsMap.
     *  @param dimensions The DerivedDimensionRepresentativeConcept from which
     *   the unit concept should be found.
     *  @return The list of UnitConcepts in this ontology that match the given
     *   componentUnitsMap or baseComponentUnits, or an empty list if there are none.
     *  @throws IllegalActionException Thrown if there is a problem getting the
     *   unit dimension concept.
     */
    private static List<UnitConcept>
        _findMatchingUnits(Map<DimensionRepresentativeConcept, List<UnitConcept>>
            componentUnitsMap, Map<BaseDimensionRepresentativeConcept, List<BaseUnitConcept>>
            baseComponentUnits, List<DerivedDimensionRepresentativeConcept> dimensions)
                throws IllegalActionException {
        
        List<UnitConcept> foundUnits = new ArrayList<UnitConcept>();
        
        for (DerivedDimensionRepresentativeConcept dimension : dimensions) {
            List<DerivedUnitConcept> allUnits = dimension.getAllUnits();
            for (DerivedUnitConcept unit : allUnits) {
                Map<DimensionRepresentativeConcept, List<UnitConcept>>
                    componentUnits = unit.getComponentUnits();
                if (componentUnitsMap.equals(componentUnits) ||
                        baseComponentUnits.equals(unit.getComponentBaseUnits())) {
                    foundUnits.add(unit);
                }
            }
        }
        
        return foundUnits;
    }
    
    /** From the list of UnitConcepts return the least upper bound of all the
     *  units that have the given unit conversion factor, or null if the list
     *  is null or empty. Normally there should only be
     *  one concept in the list that matches the conversion factor. If there is
     *  more than one concept that matches the conversion factor, return the
     *  least upper bound of these concepts.
     *  @param concepts The list of UnitConcepts to search.
     *  @param unitFactor The conversion unit factor that must match the
     *   UnitConcepts in the list.
     *  @param unitOntology The unit system ontology that contains these
     *   unit concepts.
     *  @return The least upper bound of all the concepts in the list that
     *   have the correct unit factor, or null if none are found or the list
     *   is empty or null.
     *  @throws IllegalActionException Thrown if there is a problem testing
     *   whether the unit factors are sufficiently close to be considered
     *   equal.
     */
    private static Concept _findUnitWithUnitFactor(List<UnitConcept> concepts,
            ScalarToken unitFactor, Ontology unitOntology) throws IllegalActionException {
        if (concepts == null || concepts.isEmpty()) {
            return null;
        } else {
            List<UnitConcept> resultConcepts = new ArrayList<UnitConcept>(concepts);
            for (UnitConcept concept : concepts) {
                if (!concept.getUnitFactor().isCloseTo(unitFactor).booleanValue()) {
                    resultConcepts.remove(concept);
                }
            }
            if (resultConcepts.isEmpty()) {
                return null;
            } else {
                return unitOntology.getConceptGraph().
                    leastUpperBound(resultConcepts.toArray());
            }
        }
    }
    
    /** Given a component units map that is know to have one entry in the map
     *  with a list of unit concepts that has a single element, return that unit
     *  concept.
     *  @param componentUnitsMap The given component units map.
     *  @return The single unit concept contained in the map.
     *  @throws IllegalActionException Thrown if the component units map does
     *   not have exactly one entry or the unit list has more than one element.
     */
    private static UnitConcept _getSingleUnitConceptInComponentUnitsMap(
            Map<? extends DimensionRepresentativeConcept,
                    ? extends List<? extends UnitConcept>> componentUnitsMap)
        throws IllegalActionException{
        
        if (componentUnitsMap.values().size() != 1) {
            throw new IllegalActionException("The component units map does " +
                    "not have exactly one entry in the map. Number of " +
                    "entries: " + componentUnitsMap.values().size());
        }
        
        for (List<? extends UnitConcept> unitList : componentUnitsMap.values()) {
            if (unitList == null || unitList.size() != 1) {
                throw new IllegalActionException("There is one " +
                            "dimension entry in the dimension map, " +
                            "but the unit list entry for that " +
                            "dimension in the component units map " +
                            "is null or has more than 1 element.");
            } else {
                return unitList.get(0);
            }
        }
        
        // This code should be unreachable but is required by the java compiler.
        throw new IllegalActionException("The component units map was empty " +
                    "even though there is supposed to be exactly one " +
                    "entry in the map.");
    }

    /** Return true if the input base dimension map has a single dimension with
     *  an exponent value of 1, or false otherwise.
     *  @param baseDimensionMap The base dimension map to be tested.
     *  @return True if the map has one entry with an exponent of 1, false
     *   otherwise.
     */    
    private static boolean _hasSingleDimensionWithExponentOne(
            Map<? extends DimensionRepresentativeConcept, Integer> baseDimensionMap) {
        if (baseDimensionMap.size() == 1) {
            for (Integer exponent : baseDimensionMap.values()) {
                if (exponent.intValue() == 1) {
                    return true;
                }
            }
        }        
        return false;
    }

    /** Return a new list of BaseUnitConcepts that removes all the elements
     *  of the elementsToBeRemoved list from the originalList.
     *  @param originalList The original list of BaseUnitConcepts.
     *  @param elementsToBeRemoved The list of BaseUnitConcepts to be removed
     *   from the originalList.
     *  @return A new list that contains all the elements of the original list
     *   minus the elements from the elementsToBeRemoved list.
     *  @throws IllegalActionException Thrown if the original list is null,
     *   or the original list has fewer elements than the elementsToBeRemoved
     *   list.
     */
    private static List<BaseUnitConcept> _removeMatchingListElements(
            List<BaseUnitConcept> originalList,
            List<BaseUnitConcept> elementsToBeRemoved)
                throws IllegalActionException {
        if (originalList == null) {
            throw new IllegalActionException("Original list is null so no " +
            		"elements can be removed from it.");
        } else if (originalList.size() < elementsToBeRemoved.size()) {
            throw new IllegalActionException("Original list has fewer " +
            		"elements that the number of elements to be removed, " +
            		"so all elements cannot be successfully removed.");
        } else if (elementsToBeRemoved == null || elementsToBeRemoved.isEmpty()) {
            return new ArrayList<BaseUnitConcept>(originalList);
        } else {
            List<BaseUnitConcept> resultList =
                new ArrayList<BaseUnitConcept>(originalList);
            for(BaseUnitConcept unitToBeRemoved : elementsToBeRemoved) {
                resultList.remove(unitToBeRemoved);
            }
            return resultList;
        }          
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                    private variables                      ////

    /** Map that links the component base dimensions to the list of component
     *  base units for this derived unit. The unit list is the size of the exponent
     *  for that component base dimension.
     */
    private Map<BaseDimensionRepresentativeConcept, List<BaseUnitConcept>> _componentBaseUnits;
    
    /** Map that links the component dimensions to the list of component
     *  units for this derived unit. The unit list is the size of the exponent
     *  for that component dimension.
     */
    private Map<DimensionRepresentativeConcept, List<UnitConcept>> _componentUnits;
    
    /** Index in the 2-element array of unit lists that contains all the units
     *  for positive exponents.
     */
    private static final int POSITIVE_EXPONENT_INDEX = 0;
    
    /** Index in the 2-element array of unit lists that contains all the units
     *  for negative exponents.
     */
    private static final int NEGATIVE_EXPONENT_INDEX = 1;
    
    ///////////////////////////////////////////////////////////////////
    ////                    private static inner classes           ////
    
    /** Comparator for the lists of BaseUnitConceps that will be sorted
     *  by their Concept string representations.
     * 
     */
    private static class BaseUnitComparator implements Comparator {
        
        /** Compare two BaseUnitConcept objects by their string
         *  representations.
         *  @param baseUnitConcept1 The first BaseUnitConcept object.
         *  @param baseUnitConcept2 The second BaseUnitConcept object.
         *  @return The result of the compare method on the string
         *   representations of the BaseUnitConcept objects.
         */
        public int compare(Object baseUnitConcept1, Object baseUnitConcept2) {
            return baseUnitConcept1.toString().compareTo(baseUnitConcept2.toString());
        }
    }
}
