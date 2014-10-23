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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.ConceptGraph;
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
@since Ptolemy II 10.0
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
     *  @exception IllegalActionException If the base class throws it.
     */
    public static DerivedUnitConcept createDerivedUnitConcept(
            Ontology ontology,
            DerivedDimensionRepresentativeConcept representative,
            RecordToken unitInfo) throws IllegalActionException {
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
    ////                         public methods                    ////

    /** Find the DerivedUnitConcept that contains the given dimension and
     *  component unit maps and the given unit conversion factor, or null if the
     *  unit doesn't exist in the ontology.
     *  @param dimensionMap The map of component dimensions to their exponents
     *   for this unit dimension.
     *  @param componentUnitsMap The map that links the component dimensions
     *   to a list component units for that dimension.
     *  @param newUnitFactor The unit factor for the UnitConcept to be found.
     *  @param unitOntology The ontology for the unit dimensions.
     *  @return The DerivedUnitConcept that contains the dimension and component
     *   maps, or the top of the lattice if no matching unit is found.
     *  @exception IllegalActionException Thrown if there is a problem getting
     *   the unit dimension.
     */
    public static Concept findUnitByComponentMapsAndUnitFactor(
            Map<DimensionRepresentativeConcept, Integer> dimensionMap,
            Map<DimensionRepresentativeConcept, List<UnitConcept>> componentUnitsMap,
            ScalarToken newUnitFactor, Ontology unitOntology)
                    throws IllegalActionException {

        if (_isDimensionMapEmpty(dimensionMap)) {
            return _getDimensionlessConcept(unitOntology);
        }

        // If the dimension map has only one dimension with an exponent of
        // one, just return the corresponding unit in the component units map.
        if (_hasSingleDimensionWithExponentOne(dimensionMap)) {
            return _getSingleUnitConceptInComponentUnitsMap(componentUnitsMap,
                    newUnitFactor);
        }
        // If any of the component units have a non-zero offset value,
        // we cannot derive a new unit from the map.
        if (_anyUnitHasANonZeroOffset(componentUnitsMap)) {
            return null;
        }

        Map<BaseDimensionRepresentativeConcept, Integer> baseDimensionMap = DerivedDimensionRepresentativeConcept
                .deriveComponentBaseDimensionsMap(dimensionMap);
        Map<BaseDimensionRepresentativeConcept, List<BaseUnitConcept>> baseUnitsMap = _deriveComponentBaseUnitsMap(
                componentUnitsMap, dimensionMap, baseDimensionMap);

        if (_isDimensionMapEmpty(baseDimensionMap)) {
            return _getDimensionlessConcept(unitOntology);
        }
        if (_hasSingleDimensionWithExponentOne(baseDimensionMap)) {
            return _getSingleUnitConceptInComponentUnitsMap(baseUnitsMap,
                    newUnitFactor);
        }
        if (_anyUnitHasANonZeroOffset(baseUnitsMap)) {
            return null;
        }

        List<DerivedDimensionRepresentativeConcept> candidateDimensions = _findMatchingDimensions(
                dimensionMap, baseDimensionMap, unitOntology);
        if (candidateDimensions.isEmpty()) {
            return null;
        } else {
            List<UnitConcept> candidateUnits = new ArrayList<UnitConcept>();
            for (DimensionRepresentativeConcept candidateDimension : candidateDimensions) {
                candidateUnits.addAll(_findEquivalentUnitConcepts(
                        candidateDimension, newUnitFactor));
            }
            return _getResultUnitConceptFromList(candidateUnits);
        }
    }

    /** Get the base component units map for this DerivedUnitConcept. This map links
     *  the base component dimensions with the list of base units for each dimension.
     *  @return The component units map.
     */
    public Map<BaseDimensionRepresentativeConcept, List<BaseUnitConcept>> getComponentBaseUnits() {
        return new HashMap<BaseDimensionRepresentativeConcept, List<BaseUnitConcept>>(
                _componentBaseUnits);
    }

    /** Get the component units map for this DerivedUnitConcept. This map links
     *  the component dimensions with the list of units for each dimension.
     *  @return The component units map.
     */
    public Map<DimensionRepresentativeConcept, List<UnitConcept>> getComponentUnits() {
        return new HashMap<DimensionRepresentativeConcept, List<UnitConcept>>(
                _componentUnits);
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
    protected DerivedUnitConcept(Ontology ontology,
            DerivedDimensionRepresentativeConcept representative,
            RecordToken unitInfo) throws IllegalActionException,
            NameDuplicationException {
        super(ontology, representative, unitInfo);
        _componentUnits = new HashMap<DimensionRepresentativeConcept, List<UnitConcept>>();
        _componentBaseUnits = null;
        _setComponentUnitsMap(unitInfo, representative);
        _setConversionFactors(unitInfo);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Apply the individual unit conversion factors and offsets for each
     *  component unit to the conversion factor and offset for the derived unit.
     *  @exception IllegalActionException Thrown if there is a problem getting
     *   the component dimensions.
     */
    private void _applyComponentUnitConversionFactors()
            throws IllegalActionException {
        Map<DimensionRepresentativeConcept, Integer> componentDimensions = ((DerivedDimensionRepresentativeConcept) _representative)
                .getComponentDimensions();

        for (Map.Entry<DimensionRepresentativeConcept, Integer> componentDimensionsMapEntry : componentDimensions
                .entrySet()) {
            DimensionRepresentativeConcept dimension = componentDimensionsMapEntry
                    .getKey();
            int dimensionExponent = componentDimensionsMapEntry.getValue()
                    .intValue();
            List<UnitConcept> unitsList = _componentUnits.get(dimension);
            for (UnitConcept unit : unitsList) {
                if (dimensionExponent > 0) {
                    _unitFactor = (ScalarToken) _unitFactor
                            .multiply(unit._unitFactor);
                } else if (dimensionExponent < 0) {
                    _unitFactor = (ScalarToken) _unitFactor
                            .divide(unit._unitFactor);
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
     *  @exception IllegalActionException Thrown if the units array cannot be found
     *   or it is invalid.
     */
    private Token[] _getUnitsArray(RecordToken derivedUnitRecord,
            String dimensionName) throws IllegalActionException {
        // First check to see if the record token has a label that matches
        // the given dimensionName.
        Token unitsArrayToken = derivedUnitRecord.get(dimensionName);

        // If the unitsArrayToken is not found, the dimensionName might be
        // specified by a reference name used in the parent
        // DerivedDimensionRepresentativeConcept. Try to find the unitsArrayToken
        // based on that reference name.
        if (unitsArrayToken == null) {
            String referenceName = ((DerivedDimensionRepresentativeConcept) _representative)
                    .getReferenceNameByDimensionName(dimensionName);
            if (referenceName != null) {
                unitsArrayToken = derivedUnitRecord.get(referenceName);
            }
        }

        if (unitsArrayToken == null) {
            throw new IllegalActionException(this, "Could not find the units "
                    + "information for the " + dimensionName + " dimension.");
        } else {
            if (unitsArrayToken instanceof ArrayToken
                    && ((ArrayToken) unitsArrayToken).getElementType().equals(
                            BaseType.STRING)) {
                return ((ArrayToken) unitsArrayToken).arrayValue();
            } else {
                throw new IllegalActionException(this, "Invalid units array "
                        + "for the " + dimensionName + " dimension: "
                        + unitsArrayToken);
            }
        }
    }

    /** Set the component units for the derived unit based on the given
     *  record token that specifies the component units for this derived unit.
     *  @param derivedUnitRecord The record token that contains the specified
     *   unit conversion information.
     *  @param unitDimensionRepresentative The dimension representative concept
     *   for this derived unit concept.
     *  @exception IllegalActionException Thrown if the record token has invalid
     *   unit conversion specifications.
     */
    private void _setComponentUnitsMap(RecordToken derivedUnitRecord,
            DerivedDimensionRepresentativeConcept unitDimensionRepresentative)
                    throws IllegalActionException {
        Map<DimensionRepresentativeConcept, Integer> componentDimensions = unitDimensionRepresentative
                .getComponentDimensions();

        for (Map.Entry<DimensionRepresentativeConcept, Integer> componentDimensionsMapEntry : componentDimensions
                .entrySet()) {
            DimensionRepresentativeConcept dimension = componentDimensionsMapEntry
                    .getKey();
            String dimensionName = dimension.getName();
            Token[] unitsStringTokens = _getUnitsArray(derivedUnitRecord,
                    dimensionName);
            int dimensionExponent = componentDimensionsMapEntry.getValue()
                    .intValue();
            int dimensionExponentAbsValue = Math.abs(dimensionExponent);

            if (unitsStringTokens.length == dimensionExponentAbsValue) {
                List<UnitConcept> unitsList = new ArrayList<UnitConcept>();

                for (Token unitStringToken : unitsStringTokens) {
                    String unitName = ((StringToken) unitStringToken)
                            .stringValue();
                    Concept unit = getOntology().getConceptByString(
                            dimensionName + "_" + unitName);
                    if (unit instanceof UnitConcept) {
                        unitsList.add((UnitConcept) unit);
                    } else {
                        throw new IllegalActionException(this, "Invalid "
                                + "unit concept: " + unit);
                    }
                }
                _componentUnits.put(dimension, unitsList);
            } else {
                throw new IllegalActionException(this, "The component "
                        + "dimension " + dimension + " has an exponent of "
                        + dimensionExponent + " so its units array "
                        + "should have " + dimensionExponentAbsValue
                        + " elements but it does not.");
            }
        }

        _componentBaseUnits = _deriveComponentBaseUnitsMap(_componentUnits,
                componentDimensions,
                unitDimensionRepresentative.getComponentBaseDimensions());
    }

    /** Set the unit conversion factor and offset for the derived unit based on
     *  the specified factor and offset and the component unit conversion
     *  factors that comprise this derived unit.
     *  @param derivedUnitRecord The record token that contains the specified
     *   unit conversion information.
     *  @exception IllegalActionException Thrown if the unit factor or unit
     *   offset in the record token is invalid.
     */
    private void _setConversionFactors(RecordToken derivedUnitRecord)
            throws IllegalActionException {
        Token unitFactorToken = derivedUnitRecord
                .get(UnitConversionInfo.unitFactorLabel);
        if (unitFactorToken == null) {
            _unitFactor = DoubleToken.ONE;
        } else if (unitFactorToken instanceof ScalarToken) {
            _unitFactor = (ScalarToken) unitFactorToken;
        } else {
            throw new IllegalActionException(this, "Invalid unit conversion "
                    + "factor: " + unitFactorToken);
        }

        Token unitOffsetToken = derivedUnitRecord
                .get(UnitConversionInfo.unitOffsetLabel);
        if (unitOffsetToken == null) {
            _unitOffset = DoubleToken.ZERO;
        } else if (unitOffsetToken instanceof DoubleToken) {
            _unitOffset = (ScalarToken) unitOffsetToken;
        } else {
            throw new IllegalActionException(this, "Invalid unit conversion "
                    + "offset: " + unitOffsetToken);
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
     *  @exception IllegalActionException Thrown if the exponentValue passed in is
     *   zero, because it should be either positive or negative.
     */
    private static void _addBaseUnit(
            Map<BaseDimensionRepresentativeConcept, List<BaseUnitConcept>[]> baseComponentUnits,
            BaseUnitConcept baseUnit, int exponentValue)
                    throws IllegalActionException {

        BaseDimensionRepresentativeConcept baseDimension = (BaseDimensionRepresentativeConcept) baseUnit
                .getDimension();
        List<BaseUnitConcept>[] arrayOfBaseUnitLists = baseComponentUnits
                .get(baseDimension);

        if (arrayOfBaseUnitLists == null) {
            arrayOfBaseUnitLists = new ArrayList[] {
                    new ArrayList<BaseUnitConcept>(),
                    new ArrayList<BaseUnitConcept>() };
        }

        if (exponentValue > 0) {
            arrayOfBaseUnitLists[POSITIVE_EXPONENT_INDEX].add(baseUnit);
        } else if (exponentValue < 0) {
            arrayOfBaseUnitLists[NEGATIVE_EXPONENT_INDEX].add(baseUnit);
        } else {
            throw new IllegalActionException("Exponent value should not "
                    + "be zero since it was taken from a "
                    + "dimension in the map.");
        }

        baseComponentUnits.put(baseDimension, arrayOfBaseUnitLists);
    }

    /** Update the base component units map with the set of base units from
     *  another derived units' base component units map.
     *  @param baseUnitsMap The base component units map to be updated.
     *  @param baseUnitsMapFromDerivedUnit The base component units map from another
     *   derived unit concept to be added to the baseUnitsMap.
     *  @param derivedDimensionExponent The exponent of the derived dimension.
     *  @exception IllegalActionException Thrown if the derivedDimensionExponent
     *   is zero, which should never be the case if this method is called.
     */
    private static void _addDerivedUnit(
            Map<BaseDimensionRepresentativeConcept, List<BaseUnitConcept>[]> baseUnitsMap,
            Map<BaseDimensionRepresentativeConcept, List<BaseUnitConcept>[]> baseUnitsMapFromDerivedUnit,
            int derivedDimensionExponent) throws IllegalActionException {

        for (Map.Entry<BaseDimensionRepresentativeConcept, List<BaseUnitConcept>[]> baseUnitsMapEntry : baseUnitsMapFromDerivedUnit
                .entrySet()) {
            BaseDimensionRepresentativeConcept baseDimension = baseUnitsMapEntry
                    .getKey();
            List<BaseUnitConcept>[] arrayOfBaseUnitsListsFromDerivedUnit = baseUnitsMapEntry
                    .getValue();
            List<BaseUnitConcept>[] arrayOfBaseUnitsLists = baseUnitsMap
                    .get(baseDimension);

            if (derivedDimensionExponent > 0) {
                if (arrayOfBaseUnitsLists == null) {
                    arrayOfBaseUnitsLists = arrayOfBaseUnitsListsFromDerivedUnit;
                } else {
                    arrayOfBaseUnitsLists[POSITIVE_EXPONENT_INDEX]
                            .addAll(arrayOfBaseUnitsListsFromDerivedUnit[POSITIVE_EXPONENT_INDEX]);
                    arrayOfBaseUnitsLists[NEGATIVE_EXPONENT_INDEX]
                            .addAll(arrayOfBaseUnitsListsFromDerivedUnit[NEGATIVE_EXPONENT_INDEX]);
                }
                // If the derived dimension's exponent is negative, then the array of units lists must swap
                // the positive and negative units lists arrays.
            } else if (derivedDimensionExponent < 0) {
                if (arrayOfBaseUnitsLists == null) {
                    arrayOfBaseUnitsLists = arrayOfBaseUnitsListsFromDerivedUnit;
                    List<BaseUnitConcept> tempList = new ArrayList<BaseUnitConcept>(
                            arrayOfBaseUnitsLists[NEGATIVE_EXPONENT_INDEX]);
                    arrayOfBaseUnitsLists[NEGATIVE_EXPONENT_INDEX] = arrayOfBaseUnitsLists[POSITIVE_EXPONENT_INDEX];
                    arrayOfBaseUnitsLists[POSITIVE_EXPONENT_INDEX] = tempList;
                } else {
                    arrayOfBaseUnitsLists[NEGATIVE_EXPONENT_INDEX]
                            .addAll(arrayOfBaseUnitsListsFromDerivedUnit[POSITIVE_EXPONENT_INDEX]);
                    arrayOfBaseUnitsLists[POSITIVE_EXPONENT_INDEX]
                            .addAll(arrayOfBaseUnitsListsFromDerivedUnit[NEGATIVE_EXPONENT_INDEX]);
                }
            } else {
                throw new IllegalActionException("Dimension exponent value "
                        + "should never be zero because then it would "
                        + "not have an entry in the dimension map.");
            }
            baseUnitsMap.put(baseDimension, arrayOfBaseUnitsLists);
        }

    }

    /** Return true if any unit in the given component units map has a non-zero
     *  offset value.
     *  @param componentUnitsMap The map of component UnitConcepts.
     *  @return true if none of the component UnitConcepts has a non-zero
     *   offset value, and false otherwise.
     *  @exception IllegalActionException Thrown if there is a problem comparing
     *   the UnitConcept unit offset scalar token values.
     */
    private static boolean _anyUnitHasANonZeroOffset(
            Map<? extends DimensionRepresentativeConcept, ? extends List<? extends UnitConcept>> componentUnitsMap)
                    throws IllegalActionException {

        for (List<? extends UnitConcept> unitList : componentUnitsMap.values()) {
            for (UnitConcept unit : unitList) {
                ScalarToken unitOffset = unit.getUnitOffset();
                if (!unitOffset.isEqualTo(unitOffset.zero()).booleanValue()) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Derive a map of base dimensions to lists of units that represents the
     *  given component units map and dimension map.
     *  @param componentUnitsMap The map of dimensions to lists of units from
     *   which the base component units map will be derived.
     *  @param dimensionMap The map of dimensions to exponents from which
     *   base dimension map will be derived.
     *  @param baseDimensionMap The map of base dimensions to exponents needed
     *   for creating the base component units map.
     *  @return The map of base dimensions to lists of base units that composes the given
     *   component units map.
     *  @exception IllegalActionException Thrown if an invalid dimension concept
     *   is found.
     */
    private static Map<BaseDimensionRepresentativeConcept, List<BaseUnitConcept>> _deriveComponentBaseUnitsMap(
            Map<DimensionRepresentativeConcept, List<UnitConcept>> componentUnitsMap,
            Map<DimensionRepresentativeConcept, Integer> dimensionMap,
            Map<BaseDimensionRepresentativeConcept, Integer> baseDimensionMap)
                    throws IllegalActionException {

        Map<BaseDimensionRepresentativeConcept, List<BaseUnitConcept>> baseComponentUnits = new HashMap<BaseDimensionRepresentativeConcept, List<BaseUnitConcept>>();

        Map<BaseDimensionRepresentativeConcept, List<BaseUnitConcept>[]> baseComponentUnitsSeparateExponents = _deriveComponentBaseUnitsSeparateExponentsMap(
                componentUnitsMap, dimensionMap, baseDimensionMap);

        for (Map.Entry<BaseDimensionRepresentativeConcept, Integer> baseDimensionMapEntry : baseDimensionMap
                .entrySet()) {
            BaseDimensionRepresentativeConcept baseDimension = baseDimensionMapEntry
                    .getKey();
            int exponent = baseDimensionMapEntry.getValue().intValue();
            List<BaseUnitConcept> positiveExponentUnitList = baseComponentUnitsSeparateExponents
                    .get(baseDimension)[POSITIVE_EXPONENT_INDEX];
            List<BaseUnitConcept> negativeExponentUnitList = baseComponentUnitsSeparateExponents
                    .get(baseDimension)[NEGATIVE_EXPONENT_INDEX];
            List<BaseUnitConcept> composedUnitList = null;

            if (exponent > 0) {
                composedUnitList = _removeMatchingListElements(
                        positiveExponentUnitList, negativeExponentUnitList);
            } else if (exponent < 0) {
                composedUnitList = _removeMatchingListElements(
                        negativeExponentUnitList, positiveExponentUnitList);
            } else {
                throw new IllegalActionException(
                        "Exponent value should never be "
                                + "zero because then it would not have an entry "
                                + "in the map.");
            }
            if (composedUnitList.size() == Math.abs(exponent)) {
                baseComponentUnits.put(baseDimension, composedUnitList);
            } else {
                throw new IllegalActionException("Base component unit list "
                        + "for the base dimension " + baseDimension
                        + " must be the same length as the absolute "
                        + "value of the dimension map exponent: list "
                        + "size: " + composedUnitList.size()
                        + ", exponent value: " + exponent);
            }
        }

        return baseComponentUnits;
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
     *  @exception IllegalActionException Thrown if unit concepts that are
     *   neither BaseUnitConcepts or DerivedUnitConcepts are found.
     */
    private static Map<BaseDimensionRepresentativeConcept, List<BaseUnitConcept>[]> _deriveComponentBaseUnitsSeparateExponentsMap(
            Map<DimensionRepresentativeConcept, List<UnitConcept>> componentUnitsMap,
            Map<DimensionRepresentativeConcept, Integer> dimensionMap,
            Map<BaseDimensionRepresentativeConcept, Integer> baseDimensionMap)
                    throws IllegalActionException {

        Map<BaseDimensionRepresentativeConcept, List<BaseUnitConcept>[]> baseComponentUnitsSeparateExponents = new HashMap<BaseDimensionRepresentativeConcept, List<BaseUnitConcept>[]>();

        for (Map.Entry<DimensionRepresentativeConcept, List<UnitConcept>> componentUnitsMapEntry : componentUnitsMap
                .entrySet()) {
            DimensionRepresentativeConcept dimension = componentUnitsMapEntry
                    .getKey();
            List<UnitConcept> unitsList = componentUnitsMapEntry.getValue();
            int exponent = dimensionMap.get(dimension).intValue();
            for (UnitConcept unit : unitsList) {
                if (unit instanceof BaseUnitConcept) {
                    _addBaseUnit(baseComponentUnitsSeparateExponents,
                            (BaseUnitConcept) unit, exponent);
                } else if (unit instanceof DerivedUnitConcept) {
                    DerivedDimensionRepresentativeConcept unitDimension = (DerivedDimensionRepresentativeConcept) unit
                            .getDimension();
                    Map<DimensionRepresentativeConcept, Integer> unitDimensionMap = unitDimension
                            .getComponentDimensions();
                    Map<BaseDimensionRepresentativeConcept, List<BaseUnitConcept>[]> derivedUnitBaseComponentSeparateExponents = _deriveComponentBaseUnitsSeparateExponentsMap(
                            ((DerivedUnitConcept) unit).getComponentUnits(),
                            unitDimensionMap,
                            DerivedDimensionRepresentativeConcept
                            .deriveComponentBaseDimensionsMap(unitDimensionMap));
                    _addDerivedUnit(baseComponentUnitsSeparateExponents,
                            derivedUnitBaseComponentSeparateExponents, exponent);
                } else {
                    throw new IllegalActionException("A unit concept must be "
                            + "either a BaseUnitConcept "
                            + "or a DerivedUnitConcept.");
                }
            }
        }
        return baseComponentUnitsSeparateExponents;
    }

    /** Return a list of UnitConcepts in the given unit dimension with the
     *  matching unit factor. If there are no matching units, return an
     *  empty list.
     *  @param dimension The DimensionRepresentativeConcept that represents
     *   the dimension from which to draw matching UnitConcepts.
     *  @param newUnitFactor The unit conversion factor to match for the
     *   UnitConcepts.
     *  @return The list of matching UnitConcepts.
     *  @exception IllegalActionException Thrown if there is a problem computing
     *   the unit factor scalar token values.
     */
    private static List<UnitConcept> _findEquivalentUnitConcepts(
            DimensionRepresentativeConcept dimension, ScalarToken newUnitFactor)
                    throws IllegalActionException {

        // Create an epsilon for testing unit factor closeness that is small
        // relative to the value of the unit factor.
        // It must be scaled to account for precision errors for very large
        // and very small unit scale factors.
        double unitFactorEpsilon = newUnitFactor.doubleValue();
        double base10Exponent = Math.log(unitFactorEpsilon) / Math.log(10.0);
        unitFactorEpsilon = Math.pow(10.0, base10Exponent - 9.0);

        List<UnitConcept> matchingUnits = new ArrayList<UnitConcept>();
        for (UnitConcept unit : dimension.getAllUnits()) {
            ScalarToken unitFactor = unit.getUnitFactor();
            ScalarToken unitOffset = unit.getUnitOffset();
            boolean noUnitOffsets = unitOffset.isEqualTo(unitOffset.zero())
                    .booleanValue();
            if (unit instanceof DerivedUnitConcept) {
                DerivedUnitConcept derivedUnit = (DerivedUnitConcept) unit;
                noUnitOffsets = noUnitOffsets
                        && !_anyUnitHasANonZeroOffset(derivedUnit
                                .getComponentUnits());
                noUnitOffsets = noUnitOffsets
                        && !_anyUnitHasANonZeroOffset(derivedUnit
                                .getComponentBaseUnits());
            }
            if (noUnitOffsets
                    && newUnitFactor.isCloseTo(unitFactor, unitFactorEpsilon)
                    .booleanValue()) {
                matchingUnits.add(unit);
            }
        }
        return matchingUnits;
    }

    /** Find all the matching dimension concepts with the given dimension map.
     *  @param dimensionMap The map of component dimensions to their exponents
     *   for this unit dimension.
     *  @param baseDimensionMap The already calculated base dimension map for
     *   the dimension map input.
     *  @param unitOntology The ontology for the unit dimensions.
     *  @return The List of found matching DerivedDimensionRepresentativeConcepts.
     *  @exception IllegalActionException Thrown if there is a problem getting the
     *   dimension concept.
     */
    private static List<DerivedDimensionRepresentativeConcept> _findMatchingDimensions(
            Map<DimensionRepresentativeConcept, Integer> dimensionMap,
            Map<BaseDimensionRepresentativeConcept, Integer> baseDimensionMap,
            Ontology unitOntology) throws IllegalActionException {

        List<DerivedDimensionRepresentativeConcept> foundDimensions = new ArrayList<DerivedDimensionRepresentativeConcept>();

        List<DerivedDimensionRepresentativeConcept> allDimensions = unitOntology
                .entityList(DerivedDimensionRepresentativeConcept.class);
        for (DerivedDimensionRepresentativeConcept dimension : allDimensions) {
            Map<DimensionRepresentativeConcept, Integer> componentDimensions = dimension
                    .getComponentDimensions();

            if (dimensionMap.equals(componentDimensions)
                    || baseDimensionMap.equals(dimension
                            .getComponentBaseDimensions())) {
                foundDimensions.add(dimension);
            }
        }
        return foundDimensions;
    }

    /** Find a BaseUnitConcept in the given unit list that is from the same
     *  dimension as that of the specified BaseUnitConcept, or null if no such
     *  BaseUnitConcept exists.
     *  @param unitList The list of BaseUnitConcepts to be searched.
     *  @param baseUnitToFind The BaseUnitConcept to look for matching
     *   BaseUnitConcepts in the unitList.
     *  @return A BaseUnitConcept from the list that is from the same
     *   dimension as that of the specified BaseUnitConcept, or null if no such
     *   BaseUnitConcept exists.
     */
    private static BaseUnitConcept _findSameUnitFromDimension(
            List<BaseUnitConcept> unitList, BaseUnitConcept baseUnitToFind) {
        BaseDimensionRepresentativeConcept baseDimension = (BaseDimensionRepresentativeConcept) baseUnitToFind
                .getDimension();

        for (BaseUnitConcept unit : unitList) {
            BaseDimensionRepresentativeConcept dimension = (BaseDimensionRepresentativeConcept) unit
                    .getDimension();
            if (dimension.equals(baseDimension)) {
                return unit;
            }
        }
        return null;
    }

    /** Return the least upper bound of all the dimensionless concepts in the
     *  ontology, or null if there are no dimensionless
     *  concepts in the ontology.
     *  @param unitOntology The ontology from which to get the Dimensionless
     *   concept.
     *  @return The dimensionless concept, or the top of the lattice if none
     *   are in the ontology.
     *  @exception IllegalActionException Thrown if the ontology's concept graph
     *   is null.
     */
    private static Concept _getDimensionlessConcept(Ontology unitOntology)
            throws IllegalActionException {
        List<DimensionlessConcept> allDimensionlessConcepts = unitOntology
                .entityList(DimensionlessConcept.class);
        if (allDimensionlessConcepts.isEmpty()) {
            return null;
        } else {
            ConceptGraph conceptGraph = unitOntology.getConceptGraph();
            if (conceptGraph == null) {
                throw new IllegalActionException("The ontology " + unitOntology
                        + " has a null concept graph.");
            }
            return conceptGraph.leastUpperBound(new HashSet<Concept>(
                    allDimensionlessConcepts));
        }
    }

    /** Given a component units map that is known to have one entry in the map
     *  with a list of unit concepts that has a single element, return that unit
     *  concept if its unit factor matches the given unit factor, or another
     *  unit concept in the same dimension with the matching unit factor.
     *  @param componentUnitsMap The given component units map.
     *  @param newUnitFactor The unit conversion factor to match for the
     *   UnitConcepts.
     *  @return The matching unit concept with the given unit factor.
     *  @exception IllegalActionException Thrown if the component units map does
     *   not have exactly one entry or the unit list has more than one element.
     */
    private static Concept _getSingleUnitConceptInComponentUnitsMap(
            Map<? extends DimensionRepresentativeConcept, ? extends List<? extends UnitConcept>> componentUnitsMap,
                    ScalarToken newUnitFactor) throws IllegalActionException {

        if (componentUnitsMap.values().size() != 1) {
            throw new IllegalActionException("The component units map does "
                    + "not have exactly one entry in the map. Number of "
                    + "entries: " + componentUnitsMap.values().size());
        }

        for (List<? extends UnitConcept> unitList : componentUnitsMap.values()) {
            if (unitList == null || unitList.size() != 1) {
                throw new IllegalActionException("There is one "
                        + "dimension entry in the dimension map, "
                        + "but the unit list entry for that "
                        + "dimension in the component units map "
                        + "is null or has more than 1 element.");
            } else {
                UnitConcept unit = unitList.get(0);
                if (newUnitFactor.isCloseTo(unit.getUnitFactor())
                        .booleanValue()) {
                    return unit;
                } else {
                    List<UnitConcept> matchingUnits = _findEquivalentUnitConcepts(
                            unit.getDimension(), newUnitFactor);
                    return _getResultUnitConceptFromList(matchingUnits);
                }
            }
        }

        // This code should be unreachable but is required by the java compiler.
        throw new IllegalActionException("The component units map was empty "
                + "even though there is supposed to be exactly one "
                + "entry in the map.");
    }

    /** Return a single Concept from the given list of candidate UnitConcepts.
     *  If the list is null or empty, return null.
     *  @param candidateUnits The list of candidate UnitConcepts.
     *  @return Return a single Concept from the given list of candidate
     *   UnitConcepts. If the list is null or empty, return null. If the list
     *   has a single concept, return that concept. If the list has more than
     *   one concept, return the least upper bound of all the UnitConcepts.
     */
    private static Concept _getResultUnitConceptFromList(
            List<UnitConcept> candidateUnits) {
        if (candidateUnits == null || candidateUnits.isEmpty()) {
            return null;
        } else if (candidateUnits.size() == 1) {
            return candidateUnits.get(0);
        } else {
            Ontology unitOntology = candidateUnits.get(0).getOntology();
            return unitOntology.getConceptGraph().leastUpperBound(
                    new HashSet<Concept>(candidateUnits));
        }
    }

    /** Return true if the input dimension map has a single dimension with
     *  an exponent value of 1, or false otherwise.
     *  @param dimensionMap The dimension map to be tested.
     *  @return True if the map has one entry with an exponent of 1, false
     *   otherwise.
     */
    private static boolean _hasSingleDimensionWithExponentOne(
            Map<? extends DimensionRepresentativeConcept, Integer> dimensionMap) {
        if (dimensionMap.size() == 1) {
            for (Integer exponent : dimensionMap.values()) {
                if (exponent.intValue() == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Return true if the input dimension map is empty, and false otherwise.
     *  @param dimensionMap The input dimension map.
     *  @return true if the input dimension map is empty, and false otherwise.
     */
    private static boolean _isDimensionMapEmpty(
            Map<? extends DimensionRepresentativeConcept, Integer> dimensionMap) {
        if (dimensionMap.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    /** Return a new list of BaseUnitConcepts that removes one
     *  BaseUnitConcept from the originalList for each BaseUnitConcept element
     *  of the elementsToBeRemoved.  The BaseUnitConcept that is removed must
     *  either be the same unit concept or a unit concept belonging to that
     *  dimension.
     *  @param originalList The original list of BaseUnitConcepts.
     *  @param elementsToBeRemoved The list of BaseUnitConcepts to be removed
     *   from the originalList.
     *  @return A new list that contains all the elements of the original list
     *   minus the elements from the elementsToBeRemoved list.
     *  @exception IllegalActionException Thrown if the original list is null,
     *   or the original list has fewer elements than the elementsToBeRemoved
     *   list.
     */
    private static List<BaseUnitConcept> _removeMatchingListElements(
            List<BaseUnitConcept> originalList,
            List<BaseUnitConcept> elementsToBeRemoved)
                    throws IllegalActionException {
        if (originalList == null) {
            throw new IllegalActionException("Original list is null so no "
                    + "elements can be removed from it.");
        } else if (elementsToBeRemoved == null || elementsToBeRemoved.isEmpty()) {
            return new ArrayList<BaseUnitConcept>(originalList);
        } else if (originalList.size() < elementsToBeRemoved.size()) {
            throw new IllegalActionException("Original list has fewer "
                    + "elements that the number of elements to be removed, "
                    + "so all elements cannot be successfully removed.");
        } else {
            List<BaseUnitConcept> resultList = new ArrayList<BaseUnitConcept>(
                    originalList);
            for (BaseUnitConcept unitToBeRemoved : elementsToBeRemoved) {
                if (resultList.contains(unitToBeRemoved)) {
                    resultList.remove(unitToBeRemoved);
                } else {

                    // If the unit is not found, but a unit from
                    // the same dimension is found, we still must remove
                    // it so that the final dimensions match.
                    // The conversion factor has already been calculated by
                    // the newUnitFactor variable passed into
                    // findUnitByComponentMapsAndUnitFactor().
                    BaseUnitConcept unitFromSameDimension = _findSameUnitFromDimension(
                            originalList, unitToBeRemoved);
                    if (unitFromSameDimension != null) {
                        resultList.remove(unitFromSameDimension);
                    }
                }
            }
            return resultList;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

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
}
