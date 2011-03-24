/* A concept function that returns the UnitConcept result of a multiplication
 * or division operation between two UnitConcepts in a unit system ontology.
 * 
 * Copyright (c) 1998-2010 The Regents of the University of California. All
 * rights reserved. Permission is hereby granted, without written agreement and
 * without license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies of
 * this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 * 
 * PT_COPYRIGHT_VERSION_2 COPYRIGHTENDKEY
 */
package ptolemy.data.ontologies.lattice.unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.ConceptFunction;
import ptolemy.data.ontologies.ConceptGraph;
import ptolemy.data.ontologies.Ontology;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// MultiplyOrDivideUnitConcepts

/** A concept function that returns the UnitConcept result of a multiplication
 * or division operation between two UnitConcepts in a unit system ontology.
 * 
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Green (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class MultiplyOrDivideUnitConcepts extends ConceptFunction {

    /** Create a new MultiplyOrDivideUnitConcepts concept function.
     *  @param ontology The domain and range unit system ontology for this
     *   concept function. 
     *  @param isMultiply Indicates whether this concept function will perform
     *   multiplication or division for the unit system concepts.
     *  @throws IllegalActionException Thrown if the concept function cannot be created.
     */
    public MultiplyOrDivideUnitConcepts(Ontology ontology, boolean isMultiply)
                throws IllegalActionException {
        super((isMultiply ? "multiply_" : "divide_") + "UnitConcepts", 2, ontology);
        _isMultiply = isMultiply;
        _unitOntology = ontology;
        ConceptGraph ontologyGraph = _unitOntology.getConceptGraph();
        if (ontologyGraph == null) {
            throw new IllegalActionException("The Ontology " + _unitOntology + " has a null concept graph.");
        } else {
            _topOfTheLattice = ontologyGraph.top();
            _bottomOfTheLattice = ontologyGraph.bottom();
        }        
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    /** Return the function output from the given input arguments. The output
     *  concept is a UnitConcept that is the result of multiplication or division
     *  of the two input UnitConcepts, or the top of the ontology lattice if there
     *  is no UnitConcept in the ontology that represents the product or quotient
     *  of the two input concepts.
     *  @param argValues The 2 UnitConcept input arguments.
     *  @return The output UnitConcept.
     *  @throws IllegalActionException Thrown if there is a problem creating
     *   the output RecordConcept.
     */
    protected Concept _evaluateFunction(List<Concept> argValues)
        throws IllegalActionException {
        
        Concept arg1 = argValues.get(0);
        Concept arg2 = argValues.get(1);
        
        // If either concept is the bottom of the lattice, return bottom.
        if (arg1.equals(_bottomOfTheLattice) || arg2.equals(_bottomOfTheLattice)) {
            return _bottomOfTheLattice;
            
        // If either concept is the top of the lattice, return top.
        } else if (arg1.equals(_topOfTheLattice) || arg2.equals(_topOfTheLattice)) {
            return _topOfTheLattice;
            
        // If both concepts are dimensionless, return their least upper bound.
        } else if (arg1 instanceof DimensionlessConcept && arg2 instanceof DimensionlessConcept) {
            return _unitOntology.getConceptGraph().leastUpperBound(arg1, arg2);
            
        // If arg2 is dimensionless, we are multiplying or dividing the arg1 UnitConcept
        // by one, so just return arg1.
        } else if (arg1 instanceof UnitConcept && arg2 instanceof DimensionlessConcept) {
            return arg1;
            
        // If arg1 is dimensionless and the operation is multiplication, just
        // return arg2. If the operation is division, return the UnitConcept
        // that represents the inverse of arg2.
        } else if (arg1 instanceof DimensionlessConcept && arg2 instanceof UnitConcept) {
            if (_isMultiply) {
                return arg2;
            } else {
                return _findInverseUnitConcept((UnitConcept) arg2);
            }            
        } else if (arg1 instanceof UnitConcept && arg2 instanceof UnitConcept) {
            
            // If the operation is division, and the if the first unit is from
            // the same dimension as the second unit.
            if (((UnitConcept) arg1).getDimension().equals(
                    ((UnitConcept) arg2).getDimension()) && !_isMultiply) {

                // If they are the same unit, return the dimensionless concept
                // if one is specified in the units ontology. Return the top
                // of the lattice if no dimensionless concept is specified.
                if (arg1.equals(arg2)) {
                    return _getDimensionlessConceptOrTop();
                    
                // If they are different units from the same dimension, there
                // is a missing conversion factor, so output the top of
                // the unit system lattice which indicates a conflict.
                } else {
                    return _topOfTheLattice;
                }
            } else {
                return _findComposedUnitConcept((UnitConcept) arg1, (UnitConcept) arg2);
            }            
        } else {
            throw new IllegalActionException("Concept inputs must be" +
            		" UnitConcepts or DimensionlessConcepts. Input" +
            		" Concepts were: " + arg1 + " and " + arg2 + ".");
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /** Create and return a new dimension map initialized with the given
     *  dimension and exponent.
     *  @param dimension The dimension to be added to the dimension map.
     *  @param exponentValue The exponent value to be assigned to the dimension
     *   in the map.
     *  @return The new dimension Map object.
     */
    private Map<DimensionRepresentativeConcept, Integer>
        _createNewDimensionMap(DimensionRepresentativeConcept dimension,
                int exponentValue) {        
        
        Map<DimensionRepresentativeConcept, Integer> dimensionMap =
            new HashMap<DimensionRepresentativeConcept, Integer>();
        dimensionMap.put(dimension, new Integer(exponentValue));
        return dimensionMap;
    }
    
    /** Create and return a new component units map initialized with the given
     *  UnitConcept.
     *  @param unit The UnitConcept to be added to the component units map.
     *  @return The new component units Map object.
     */
    private Map<DimensionRepresentativeConcept, List<UnitConcept>>
        _createNewComponentUnitsMap(UnitConcept unit) {
            
        Map<DimensionRepresentativeConcept, List<UnitConcept>> componentUnitsMap =
            new HashMap<DimensionRepresentativeConcept, List<UnitConcept>>();
        List<UnitConcept> unitList = new ArrayList<UnitConcept>();
        unitList.add(unit);
        
        componentUnitsMap.put(unit.getDimension(), unitList);
        return componentUnitsMap;
    }
    
    /** Find the UnitConcept that represents the multiplicative inverse of
     *  the given UnitConcept. If none exists, return the top of the lattice.
     *  @param unit The input UnitConcept.
     *  @return The UnitConcept that is the multiplicative inverse of the
     *   input UnitConcept. 
     *  @throws IllegalActionException Thrown if there is a problem getting
     *   the unit concept.
     */
    private Concept _findInverseUnitConcept(UnitConcept unit)
            throws IllegalActionException {
        Map<DimensionRepresentativeConcept, Integer> inverseDimensionMap =
            _createNewDimensionMap(unit.getDimension(), -1);
        Map<DimensionRepresentativeConcept, List<UnitConcept>> inverseComponentUnitsMap =
            _createNewComponentUnitsMap(unit);
        
        return DerivedUnitConcept.findUnitByComponentMaps(inverseDimensionMap,
                inverseComponentUnitsMap, _unitOntology);
    }
    
    /** Find the UnitConcept that represents the composition of the two
     *  input UnitConcepts which will be the result of either multiplication
     *  or division of the two units depending on the value of the _isMultiply
     *  class variable.
     *  @param unit1 The first unit concept.
     *  @param unit2 The second unit concept.
     *  @return The unit concept that represents the multiplication or division
     *   of the two input concepts, or the top of the lattice if that unit
     *   does not exist in the ontology.
     *  @throws IllegalActionException Thrown if either of the UnitConcep inputs
     *   is invalid.
     */
    private Concept _findComposedUnitConcept(UnitConcept unit1, UnitConcept unit2) throws IllegalActionException {
        int exponentValue = 0;
        if (_isMultiply) {
            exponentValue = 1;
        } else {
            exponentValue = -1;
        }
        
        DimensionRepresentativeConcept unit1Dimension = unit1.getDimension();
        DimensionRepresentativeConcept unit2Dimension = unit2.getDimension();        
        Map<DimensionRepresentativeConcept, Integer> dimensionMap = null;
        Map<DimensionRepresentativeConcept, List<UnitConcept>> componentUnitsMap = null;
        
        if (unit1 instanceof BaseUnitConcept) {
            dimensionMap = _createNewDimensionMap(unit2.getDimension(), exponentValue);
            dimensionMap.put(unit1Dimension, new Integer(1));
            componentUnitsMap = _createNewComponentUnitsMap(unit2);
            List<UnitConcept> unitList = new ArrayList<UnitConcept>();
            unitList.add(unit1);
            componentUnitsMap.put(unit1Dimension, unitList);            
        } else if (unit1 instanceof DerivedUnitConcept) {
            dimensionMap = _getNewDimensionMap(
                    (DerivedDimensionRepresentativeConcept) unit1Dimension,
                    unit2Dimension, exponentValue);
            componentUnitsMap = _getNewComponentUnitsMap(dimensionMap,
                    (DerivedUnitConcept) unit1, unit2, exponentValue);
            if (componentUnitsMap == null) {
                return _topOfTheLattice;
            }
        } else {
            throw new IllegalActionException("UnitConcept must be either a " +
            		"BaseUnitConcept or a DerivedUnitConcept. First input " +
            		"concept " + unit1 + " was of type " +
            		unit1.getClass() + ".'");
        }
        
        return DerivedUnitConcept.findUnitByComponentMaps(dimensionMap, componentUnitsMap, _unitOntology);
    }
    
    /** Return the least upper bound of all the dimensionless concepts in the
     *  ontology, or the top of the lattice if there are no dimensionless
     *  concepts in the ontology.
     *  @return The dimensionless concept, or the top of the lattice if none
     *   are in the ontology.
     *  @throws IllegalActionException Thrown if the ontology's concept graph
     *   is null.
     */
    private Concept _getDimensionlessConceptOrTop() throws IllegalActionException {        
        List<DimensionlessConcept> allDimensionlessConcepts =
            _unitOntology.entityList(DimensionlessConcept.class);
        if (allDimensionlessConcepts.isEmpty()) {
            return _topOfTheLattice;
        } else {
            ConceptGraph conceptGraph = _unitOntology.getConceptGraph();
            if (conceptGraph == null) {
                throw new IllegalActionException("The ontology " + _unitOntology +
                            " has a null concept graph.");
            }
            return conceptGraph.leastUpperBound(allDimensionlessConcepts.toArray());
        }
    }
    
    /** Return a new dimension map that contains the dimensions from the given
     *  input dimensions.
     *  @param unit1Dimension The dimension of the first unit concept.
     *  @param unit2Dimension The dimension of the second unit concept
     *  @param exponentValue The exponent value for the second unit concept
     *   (1 if the operation is multiplication, -1 if it is division)
     *  @return A new dimension map that contains the component dimensions
     *   of the first unit dimension, and the second unit dimension.
     *  @throws IllegalActionException Thrown if there is a problem getting
     *   the component dimensions from the unit dimension.
     */
    private Map<DimensionRepresentativeConcept, Integer> _getNewDimensionMap(
            DerivedDimensionRepresentativeConcept unit1Dimension,
            DimensionRepresentativeConcept unit2Dimension, int exponentValue) throws IllegalActionException {
        Map<DimensionRepresentativeConcept, Integer> newDimensionMap = unit1Dimension.getComponentDimensions();
        
        Integer currentExponent = newDimensionMap.get(unit2Dimension);
        if (currentExponent == null) {
            newDimensionMap.put(unit2Dimension, new Integer(exponentValue));
        } else {
            int newExponentValue = currentExponent.intValue() + exponentValue;
            if (newExponentValue == 0) {
                newDimensionMap.remove(unit2Dimension);
            } else {
                newDimensionMap.put(unit2Dimension, new Integer(newExponentValue));
            }
        }
        
        return newDimensionMap;
    }
    
    /** Return a new component units map that contains the component units from
     *  the given input dimensions.
     *  @param newDimensionMap The new dimension map for the composed result
     *   of the two input unit concepts.
     *  @param unit1 The first unit concept.
     *  @param unit2 The second unit concept.
     *  @param exponentValue The exponent value for the second unit concept
     *   (1 if the operation is multiplication, -1 if it is division)
     *  @return The new component units map that contains the component
     *   units of the first unit concept and the second unit concept.
     *  @throws IllegalActionException Thrown if the exponent for unit2's
     *   dimension in the newDimensionMap is null.
     */
    private Map<DimensionRepresentativeConcept, List<UnitConcept>> _getNewComponentUnitsMap(
            Map<DimensionRepresentativeConcept, Integer> newDimensionMap,
            DerivedUnitConcept unit1, UnitConcept unit2, int exponentValue)
                throws IllegalActionException {
        Map<DimensionRepresentativeConcept, List<UnitConcept>>
            newComponentUnitsMap = unit1.getComponentUnits();
            
        if (newDimensionMap.get(unit2.getDimension()) == null) {
            newComponentUnitsMap.remove(unit2.getDimension());
        } else {
            List<UnitConcept> oldUnitsList = newComponentUnitsMap.get(unit2.getDimension());
            if (oldUnitsList == null) {
                List<UnitConcept> newUnitList = new ArrayList<UnitConcept>();
                newUnitList.add(unit2);
                newComponentUnitsMap.put(unit2.getDimension(), newUnitList);
            } else {
                Integer newExponent = newDimensionMap.get(unit2.getDimension());
                if (newExponent == null) {
                    throw new IllegalActionException("The second input unit " +
                            "concept's dimension does not have a valid " +
                            "value in the dimension map for the composed " +
                    "unit concept result.");
                } else {
                    int newExponentValue = newExponent.intValue();
                    List<UnitConcept> newUnitsList = _getNewUnitsList(unit2,
                            oldUnitsList, newExponentValue, exponentValue);
                    if (newUnitsList == null) {
                        return null;
                    }
                    newComponentUnitsMap.put(unit2.getDimension(), newUnitsList);
                }
            }
        }
        
        return newComponentUnitsMap;
    }
    
    /** Return the new units list for the given unit based on whether the
     *  array should be increased or shortened based on the new exponent
     *  value.
     *  @param unitToBeModified The unit concept being added or removed from
     *   the units array
     *  @param oldUnitsList The old units list being modified.
     *  @param newExponentValue The new exponent value for the dimension
     *   represented by this units array.
     *  @param unitExponentValue The exponent value for the unit being modified.
     *   Should be either -1 or 1.
     *  @return The new units array after being modified.
     *  @throws IllegalActionException Thrown if the old units array should
     *   be shortened, but the unitToBeModified is not contained in the old
     *   units array.
     */
    public List<UnitConcept> _getNewUnitsList(UnitConcept unitToBeModified,
            List<UnitConcept> oldUnitsList, int newExponentValue,
            int unitExponentValue) throws IllegalActionException {
        List<UnitConcept> newUnitsList = new ArrayList<UnitConcept>(oldUnitsList);
        
        boolean _newArrayIsBigger = (newExponentValue > 0 &&
                unitExponentValue > 0) || (newExponentValue < 0 &&
                        unitExponentValue < 0);
        if (_newArrayIsBigger) {
            newUnitsList.add(unitToBeModified);
        } else {
            for (int i = newUnitsList.size() - 1; i >= 0; i--) {
                if (newUnitsList.get(i).equals(unitToBeModified)) {
                    newUnitsList.remove(i);
                    return newUnitsList;
                }
            }
            return null;
        }
        return newUnitsList;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** Determines whether the concept function represents multiplication
     *  or division of two unit concepts.
     */
    private boolean _isMultiply = false;
    
    /** The bottom of the lattice for the unit system ontology */
    private Concept _bottomOfTheLattice;
    
    /** The top of the lattice for the unit system ontology */
    private Concept _topOfTheLattice;
    
    /** The unit system ontology for this concept function. */
    private Ontology _unitOntology;
}
