/* A representative concept in the unitSystem ontology for a set of units for
 * a specific physical dimension that is derived from other dimensions.

 Copyright (c) 1998-2011 The Regents of the University of California.
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
package ptolemy.data.ontologies.lattice.adapters.unitSystem;

import java.util.HashMap;
import java.util.Map;

import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.ontologies.Ontology;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// DerivedDimensionRepresentativeConcept

/** <p>A representative concept in the unitSystem ontology for a set of units for
 *  a specific physical dimension that is derived from other dimensions. For
 *  example, the Force dimension is derived from:<BR>
 *  <code>Force = Mass * Position / (Time ^ 2)</code>
 *  </p>
 *  <p>And its SI unit Newtons (N) is derived from:<BR>
 *  <code>N = kg * m / (s ^ 2)</code>
 *  </p>
 *  Dimensions can be composed of other derived dimensions hierarchically. For
 *  example, the force dimension can also be derived as:<BR>
 *  <code>Velocity = Position / Time<BR>
 *  Acceleration = Velocity / Time<BR>
 *  Force = Mass * Acceleration
 *  </code>
 *  </p>
@see DerivedUnitConcept
@see BaseUnitConcept
@author Charles Shelton
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating Red (cshelton)
@Pt.AcceptedRating Red (cshelton)
*/
public class DerivedDimensionRepresentativeConcept extends DimensionRepresentativeConcept {

    /** Create a new DerivedUnitRepresentativeConcept with the specified name and
     *  ontology.
     *  
     *  @param ontology The specified ontology where this concept resides.
     *  @param name The specified name for the concept.
     *  @exception NameDuplicationException If the ontology already contains a
     *   concept with the specified name.
     *  @exception IllegalActionException If the base class throws it.
     */
    public DerivedDimensionRepresentativeConcept(Ontology ontology, String name)
            throws NameDuplicationException, IllegalActionException {
        super(ontology, name);        
        unitConversionInfo = new Parameter(this, "unitConversionInfo");
        dimensionArray = new Parameter(this, "dimensionArray");
        unitConversionInfo.setTypeEquals(new ArrayType(BaseType.RECORD));
        dimensionArray.setTypeEquals(new ArrayType(BaseType.RECORD));
        
        _componentDimensions = new HashMap<DimensionRepresentativeConcept,
                                            Integer>();
        _baseDimensionExponentsArray = new int[BaseDimensionType.numBaseDimensions()];
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
    /** An array of records that specifies the dimensions and exponents that
     *  comprise this dimension.
     */
    public Parameter dimensionArray;
    
    /** The array of units specifications for each unit name in this dimension. */
    public Parameter unitConversionInfo;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** React to a change in the dimensionArray parameter. Update the
     *  component dimensions based on the new value of the dimensionArray.
     *  @param attribute The attribute that has changed.
     *  @throws IllegalActionException Thrown if there is a problem creating
     *   the component dimensions map from the dimension array parameter.
     */
    public void attributeChanged(Attribute attribute)
        throws IllegalActionException {
        if (attribute.equals(dimensionArray)) {
            ArrayToken dimensionArrayToken = (ArrayToken) dimensionArray.getToken();
            if (dimensionArrayToken != null) {
                _setUnitDimensions(dimensionArrayToken);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }
    
    /** Return the component dimensions map for this derived unit dimension.
     *  @return The map of component units and their exponents for this
     *   derived dimension.
     */
    public Map<DimensionRepresentativeConcept, Integer> getComponentDimensions() {
        return _componentDimensions;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    /** Return a BaseUnitConcept instance from the given concept string
     *  representation. The string must represent one of the units specified
     *  for this physical dimension.
     *  @param infiniteConceptString The string that represents the unit concept
     *   to be returned.
     *  @return The BaseUnitConcept represented by the given string.
     *  @throws IllegalActionException Thrown if there is a problem creating
     *   the unit concept.
     */
    protected DerivedUnitConcept _createInfiniteConceptInstance(
            String infiniteConceptString) throws IllegalActionException {
        if (containsThisInfiniteConceptString(infiniteConceptString)) {
            String unitName = infiniteConceptString.substring(getName()
                    .length() + 1);
            
            Token[] unitNamesArray = ((ArrayToken) unitNames.getToken()).arrayValue();            
            int index = 0;
            for (Token unitNameToken : unitNamesArray) {
                if (unitName.equals(((StringToken) unitNameToken).stringValue())) {
                    try {
                        RecordToken unitConversionToken = (RecordToken)
                            ((ArrayToken) unitConversionInfo.getToken()).
                                getElement(index);                        
                        Token[] valuesArray = new Token[]{unitNameToken,
                                                          unitConversionToken};                        
                        RecordToken unitRecord = new RecordToken(
                                DerivedUnitConcept.derivedUnitRecordLabelArray,
                                valuesArray);
                        return DerivedUnitConcept.createDerivedUnitConcept(
                                getOntology(), this, unitRecord);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        throw new IllegalActionException(this,
                                "No matching unit conversion info record for " +
                                "the unit named: " +
                                ((StringToken) unitNameToken).stringValue());
                    }
                }
                index++;
            }
            throw new IllegalActionException(this, "No unit named " + unitName
                    + " for the " + this + " dimension.");            
        } else {
            throw new IllegalActionException(this, "The given string cannot " +
                        "be used to derive a valid infinite concept contained " +
                        "by this representative.");
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /** Derive the array of base dimension exponents that represent this
     *  derived dimension.
     *  @throws IllegalActionException Thrown if an unrecognized dimension
     *   concept is found.
     */
    private void _deriveBaseDimensionExponentsArray() throws IllegalActionException {
        // Clear the base dimension exponents array values to all zeros.
        for (int i = 0; i < _baseDimensionExponentsArray.length; i++) {
            _baseDimensionExponentsArray[i] = 0;
        }
        
        for (DimensionRepresentativeConcept dimension : _componentDimensions.keySet()) {
            int exponentValue = _getExponentValueForComponentDimension(_componentDimensions, dimension);
            if (dimension instanceof BaseDimensionRepresentativeConcept) {
                _incrementBaseDimensionExponent((BaseDimensionRepresentativeConcept)
                        dimension, exponentValue);
            } else if (dimension instanceof DerivedDimensionRepresentativeConcept) {
                _incrementDerivedDimensionExponents(
                        ((DerivedDimensionRepresentativeConcept) dimension).
                            _componentDimensions, exponentValue);
            } else {
                throw new IllegalActionException(this,
                        "A unit dimension must be either a " +
                        "BaseDimensionRepresentativeConcept or a " +
                        "DerivedDimensionRepresentativeConcept.");
            }
        }
    }
    
    /** Get the dimension concept from a dimensionRecord record token.
     *  @param dimensionRecord The record token that specifies the dimension
     *   and its exponent.
     *  @return The dimension concept contained in the record token.
     *  @throws IllegalActionException Thrown if the dimension record token
     *   or the concept object contained in the record token is invalid.
     */
    private DimensionRepresentativeConcept _getDimensionValue(RecordToken dimensionRecord)
        throws IllegalActionException {
        Token dimensionObjectToken = dimensionRecord.get(_dimensionLabel);
        if (dimensionObjectToken instanceof ObjectToken) {
            Object dimensionObject = ((ObjectToken) dimensionObjectToken).getValue();
            if (dimensionObject instanceof DimensionRepresentativeConcept) {
                return (DimensionRepresentativeConcept) dimensionObject;
            } else {
                throw new IllegalActionException(this, "Invalid dimension " +
                                "specification: " + dimensionObject);
            }
        } else {
            throw new IllegalActionException(this, "Invalid dimension record " +
                        "token: " + dimensionRecord);
        }
    }
    
    /** Get the integer exponent value from a dimensionRecord record token.
     *  @param dimensionRecord The record token that specifies the dimension
     *   and its exponent.
     *  @return The integer exponent for the dimension contained in the record token.
     *  @throws IllegalActionException Thrown if the dimension record token
     *   or the exponent value in the record token is zero or invalid.
     */
    private Integer _getExponentValue(RecordToken dimensionRecord) throws IllegalActionException {
        Token exponentToken = dimensionRecord.get(_exponentLabel);
        if (exponentToken instanceof IntToken) {
            int exponentValue = (((IntToken) exponentToken).intValue());
            if (exponentValue == 0) {
                throw new IllegalActionException(this, "Dimension exponent " +
                                "cannot be zero because that means the derived " +
                                "dimension is not derived from it.");
            } else {
                return new Integer(exponentValue);
            }
        } else {
            throw new IllegalActionException(this, "Invalid dimension record " +
                        "token: " + dimensionRecord);
        }
    }
    
    /** Get the exponent value for the given component dimension for this
     *  derived dimension.
     *  @param dimensionMap The map that associates the component dimensions
     *   to their exponents for this derived dimension.
     *  @param dimension The specified component dimension.
     *  @return The integer exponent value for the given component dimension.
     *  @throws IllegalActionException Thrown if the returned exponent value is
     *   invalid
     */
    private int _getExponentValueForComponentDimension(Map<DimensionRepresentativeConcept, Integer>
        dimensionMap, DimensionRepresentativeConcept dimension)
            throws IllegalActionException {
        Integer exponent = dimensionMap.get(dimension);
        if (exponent == null) {
            throw new IllegalActionException(this, "Exponent value for " +
                            "dimension " + dimension + " was null.");
        } else {
            return exponent.intValue();
        }
    }
    
    /** Increment the base dimension exponent by the given exponent value.
     *  @param dimension The specified base dimension to be incremented.
     *  @param exponentValue The exponent value by which the base dimension
     *   exponent will be incremented.
     *  @throws IllegalActionException Thrown if the base dimension concept is
     *   invalid.
     */
    private void _incrementBaseDimensionExponent(
            BaseDimensionRepresentativeConcept dimension, int exponentValue)
                throws IllegalActionException {
        
        BaseDimensionType baseDimension = BaseDimensionType.
            getBaseDimensionTypeByName(dimension.getName());
        if (baseDimension == null) {
            throw new IllegalActionException(this, "Invalid base unit " +
            		"dimension concept: " + dimension);
        }        
        int baseDimensionIndex = baseDimension.getIndex();
        _baseDimensionExponentsArray[baseDimensionIndex] += exponentValue;     
    }
    
    /** Increment the derived dimension exponents by the given exponent value.
     *  @param dimensionMap The map of dimension exponents to be incremented.
     *  @param exponentValue The exponent value by which the derived dimension
     *   exponents will be incremented.
     *  @throws IllegalActionException Thrown if any of the derived dimension
     *   concepts are invalid.
     */
    private void _incrementDerivedDimensionExponents(
            Map<DimensionRepresentativeConcept, Integer> dimensionMap,
            int exponentValue) throws IllegalActionException {
        
        for (DimensionRepresentativeConcept dimension : dimensionMap.keySet()) {
            int subDimensionExponentValue = exponentValue *
                    _getExponentValueForComponentDimension(dimensionMap, dimension);
            if (dimension instanceof BaseDimensionRepresentativeConcept) {
                _incrementBaseDimensionExponent((BaseDimensionRepresentativeConcept)
                        dimension, subDimensionExponentValue);                
            } else if (dimension instanceof DerivedDimensionRepresentativeConcept) {
                _incrementDerivedDimensionExponents(
                        ((DerivedDimensionRepresentativeConcept) dimension).
                        _componentDimensions,
                        subDimensionExponentValue);
            } else {
                throw new IllegalActionException(this,
                        "A unit dimension must be either a " +
                        "BaseDimensionRepresentativeConcept or a " +
                        "DerivedDimensionRepresentativeConcept.");
            }
        }
    }
    
    /** Set the component dimensions map based on the given dimensionArrayToken
     *  ArrayToken of RecordTokens.
     *  @param dimensionArrayToken The given dimensionArrayToken
     *  @throws IllegalActionException Thrown if the dimensionArrayToken is not
     *   an array of record tokens.
     */
    private void _setUnitDimensions(ArrayToken dimensionArrayToken)
        throws IllegalActionException {
        _componentDimensions.clear();
        
        Token[] dimensions = dimensionArrayToken.arrayValue();
        for (Token dimensionRecord : dimensions) {
            if (dimensionRecord instanceof RecordToken) {
                DimensionRepresentativeConcept dimension =
                    _getDimensionValue((RecordToken) dimensionRecord);
                Integer exponent = _getExponentValue((RecordToken) dimensionRecord);
                _componentDimensions.put(dimension, exponent);                
            } else {
                throw new IllegalActionException(this, "Dimension array token " +
                                "must be an array of record tokens.");
            }
        }
        
        _deriveBaseDimensionExponentsArray();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The map of component dimensions to the exponents that comprises
     *  this derived dimension.
     */
    private Map<DimensionRepresentativeConcept, Integer> _componentDimensions;
    
    /** The array of base dimension exponents that defines this derived
     *  dimension.
     */
    private int[] _baseDimensionExponentsArray;
    
    /** The label for the Dimension field for the dimension record token. */
    private static final String _dimensionLabel = "Dimension";
    
    /** The label for the Exponent field for the dimension record token. */
    private static final String _exponentLabel = "Exponent";
}
