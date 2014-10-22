/* A concept function that returns the UnitConcept result of a multiplication
 * or division operation between two UnitConcepts in a unit system ontology.
 *
 * Copyright (c) 1998-2014 The Regents of the University of California. All
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import ptolemy.data.ScalarToken;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.ConceptFunction;
import ptolemy.data.ontologies.ConceptGraph;
import ptolemy.data.ontologies.Ontology;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// MultiplyOrDivideUnitConcepts

/** A concept function that returns the UnitConcept result of a multiplication
 *  or division operation between two UnitConcepts in a unit system ontology.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Green (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class MultiplyOrDivideUnitConcepts extends ConceptFunction {

    /** Create a new MultiplyOrDivideUnitConcepts concept function.
     *  @param ontology The domain and range unit system ontology for this
     *   concept function.
     *  @param isMultiply Indicates whether this concept function will perform
     *   multiplication or division for the unit system concepts.
     *  @exception IllegalActionException Thrown if the concept function cannot be created.
     */
    public MultiplyOrDivideUnitConcepts(Ontology ontology, boolean isMultiply)
            throws IllegalActionException {
        super((isMultiply ? "multiply_" : "divide_") + "UnitConcepts", 2,
                ontology);
        _isMultiply = isMultiply;
        _unitOntology = ontology;
        ConceptGraph ontologyGraph = _unitOntology.getConceptGraph();
        if (ontologyGraph == null) {
            throw new IllegalActionException("The Ontology " + _unitOntology
                    + " has a null concept graph.");
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
     *  @exception IllegalActionException Thrown if there is a problem creating
     *   the output UnitConcept.
     */
    @Override
    protected Concept _evaluateFunction(List<Concept> argValues)
            throws IllegalActionException {

        Concept arg1 = argValues.get(0);
        Concept arg2 = argValues.get(1);

        // If either concept is the bottom of the lattice, return bottom.
        if (arg1.equals(_bottomOfTheLattice)
                || arg2.equals(_bottomOfTheLattice)) {
            return _bottomOfTheLattice;

            // If either concept is the top of the lattice, return top.
        } else if (arg1.equals(_topOfTheLattice)
                || arg2.equals(_topOfTheLattice)) {
            return _topOfTheLattice;

            // If both concepts are dimensionless, return their least upper bound.
        } else if (arg1 instanceof DimensionlessConcept
                && arg2 instanceof DimensionlessConcept) {
            return _unitOntology.getConceptGraph().leastUpperBound(arg1, arg2);

            // If arg2 is dimensionless, we are multiplying or dividing the arg1 UnitConcept
            // by one, so just return arg1.
        } else if (arg1 instanceof UnitConcept
                && arg2 instanceof DimensionlessConcept) {
            return arg1;

            // If arg1 is dimensionless and the operation is multiplication, just
            // return arg2. If the operation is division, return the UnitConcept
            // that represents the inverse of arg2.
        } else if (arg1 instanceof DimensionlessConcept
                && arg2 instanceof UnitConcept) {
            if (_isMultiply) {
                return arg2;
            } else {
                return _findInverseUnitConcept((UnitConcept) arg2);
            }
        } else if (arg1 instanceof UnitConcept && arg2 instanceof UnitConcept) {
            return _findComposedUnitConcept((UnitConcept) arg1,
                    (UnitConcept) arg2);
        } else {
            throw new IllegalActionException("Concept inputs must be"
                    + " UnitConcepts or DimensionlessConcepts. Input"
                    + " Concepts were: " + arg1 + " and " + arg2 + ".");
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
    private Map<DimensionRepresentativeConcept, Integer> _createNewDimensionMap(
            DimensionRepresentativeConcept dimension, int exponentValue) {

        Map<DimensionRepresentativeConcept, Integer> dimensionMap = new HashMap<DimensionRepresentativeConcept, Integer>();
        dimensionMap.put(dimension, Integer.valueOf(exponentValue));
        return dimensionMap;
    }

    /** Create and return a new component units map initialized with the given
     *  UnitConcept.
     *  @param unit The UnitConcept to be added to the component units map.
     *  @return The new component units Map object.
     */
    private Map<DimensionRepresentativeConcept, List<UnitConcept>> _createNewComponentUnitsMap(
            UnitConcept unit) {

        Map<DimensionRepresentativeConcept, List<UnitConcept>> componentUnitsMap = new HashMap<DimensionRepresentativeConcept, List<UnitConcept>>();
        List<UnitConcept> unitList = new ArrayList<UnitConcept>();
        unitList.add(unit);

        componentUnitsMap.put(unit.getDimension(), unitList);
        return componentUnitsMap;
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
     *  @exception IllegalActionException Thrown if either of the UnitConcep inputs
     *   is invalid.
     */
    private Concept _findComposedUnitConcept(UnitConcept unit1,
            UnitConcept unit2) throws IllegalActionException {
        int exponentValue = 0;
        ScalarToken newUnitFactor = null;
        if (_isMultiply) {
            exponentValue = 1;
            newUnitFactor = (ScalarToken) unit1.getUnitFactor().multiply(
                    unit2.getUnitFactor());
        } else {
            exponentValue = -1;
            newUnitFactor = (ScalarToken) unit1.getUnitFactor().divide(
                    unit2.getUnitFactor());
        }

        DimensionRepresentativeConcept unit1Dimension = unit1.getDimension();
        DimensionRepresentativeConcept unit2Dimension = unit2.getDimension();
        Map<DimensionRepresentativeConcept, Integer> dimensionMap = null;
        Map<DimensionRepresentativeConcept, List<UnitConcept>> componentUnitsMap = null;

        // Special case: If both units are from the same dimension.
        if (unit1Dimension.equals(unit2Dimension)) {

            // If the operation is multiplication, then the dimension map has
            // only a single dimension with exponent value 2.
            if (_isMultiply) {
                dimensionMap = new HashMap<DimensionRepresentativeConcept, Integer>();
                componentUnitsMap = new HashMap<DimensionRepresentativeConcept, List<UnitConcept>>();
                dimensionMap.put(unit1Dimension, Integer.valueOf(2));
                List<UnitConcept> unitsList = new ArrayList<UnitConcept>();
                unitsList.add(unit1);
                unitsList.add(unit2);
                componentUnitsMap.put(unit1Dimension, unitsList);

                // If the operation is division, then the result is either the
                // Dimensionless concept (if specified in the ontology) if the
                // units are the same or the top of the lattice if the units are
                // different.
            } else {
                if (unit1.equals(unit2)) {
                    return _getDimensionlessConceptOrTop();

                    // If they are different units from the same dimension, there
                    // is a missing conversion factor, so output the top of
                    // the unit system lattice which indicates a conflict.
                } else {
                    return _topOfTheLattice;
                }
            }
        } else {
            dimensionMap = _createNewDimensionMap(unit2.getDimension(),
                    exponentValue);
            componentUnitsMap = _createNewComponentUnitsMap(unit2);
            dimensionMap.put(unit1Dimension, Integer.valueOf(1));
            List<UnitConcept> unit1List = new ArrayList<UnitConcept>();
            unit1List.add(unit1);
            componentUnitsMap.put(unit1Dimension, unit1List);
        }

        Concept result = DerivedUnitConcept
                .findUnitByComponentMapsAndUnitFactor(dimensionMap,
                        componentUnitsMap, newUnitFactor, _unitOntology);
        if (result == null) {
            return _topOfTheLattice;
        } else {
            return result;
        }
    }

    /** Find the UnitConcept that represents the multiplicative inverse of
     *  the given UnitConcept. If none exists, return the top of the lattice.
     *  @param unit The input UnitConcept.
     *  @return The UnitConcept that is the multiplicative inverse of the
     *   input UnitConcept.
     *  @exception IllegalActionException Thrown if there is a problem getting
     *   the unit concept.
     */
    private Concept _findInverseUnitConcept(UnitConcept unit)
            throws IllegalActionException {
        Map<DimensionRepresentativeConcept, Integer> inverseDimensionMap = _createNewDimensionMap(
                unit.getDimension(), -1);
        Map<DimensionRepresentativeConcept, List<UnitConcept>> inverseComponentUnitsMap = _createNewComponentUnitsMap(unit);

        ScalarToken inverseFactor = (ScalarToken) unit.getUnitFactor().one()
                .divide(unit.getUnitFactor());
        return DerivedUnitConcept.findUnitByComponentMapsAndUnitFactor(
                inverseDimensionMap, inverseComponentUnitsMap, inverseFactor,
                _unitOntology);
    }

    /** Return the least upper bound of all the dimensionless concepts in the
     *  ontology, or the top of the lattice if there are no dimensionless
     *  concepts in the ontology.
     *  @return The dimensionless concept, or the top of the lattice if none
     *   are in the ontology.
     *  @exception IllegalActionException Thrown if the ontology's concept graph
     *   is null.
     */
    private Concept _getDimensionlessConceptOrTop()
            throws IllegalActionException {
        List<DimensionlessConcept> allDimensionlessConcepts = _unitOntology
                .entityList(DimensionlessConcept.class);
        if (allDimensionlessConcepts.isEmpty()) {
            return _topOfTheLattice;
        } else {
            ConceptGraph conceptGraph = _unitOntology.getConceptGraph();
            if (conceptGraph == null) {
                throw new IllegalActionException("The ontology "
                        + _unitOntology + " has a null concept graph.");
            }
            return conceptGraph.leastUpperBound(new HashSet<Concept>(
                    allDimensionlessConcepts));
        }
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
