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
package ptolemy.data.ontologies.lattice.unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
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
    public DerivedDimensionRepresentativeConcept(CompositeEntity ontology, String name)
            throws NameDuplicationException, IllegalActionException {
        super(ontology, name);        
        dimensionArray = new Parameter(this, "dimensionArray");
        dimensionArray.setTypeEquals(new ArrayType(BaseType.RECORD));
        
        _componentDimensions = new HashMap<DimensionRepresentativeConcept,
                                            Integer>();
        _componentBaseDimensions = new HashMap<BaseDimensionRepresentativeConcept,
                                            Integer>();
        _dimensionNameToReferenceName = new HashMap<String, String>();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
    /** An array of records that specifies the dimensions and exponents that
     *  comprise this dimension.
     */
    public Parameter dimensionArray;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Return the component dimensions map for this derived unit dimension.
     *  @return The map of component units and their exponents for this
     *   derived dimension.
     *  @throws IllegalActionException Thrown if there is a problem getting
     *   the component dimensions map.
     */
    public Map<DimensionRepresentativeConcept, Integer> getComponentDimensions()
            throws IllegalActionException {
        _updateDimensionInformation();
        return new HashMap<DimensionRepresentativeConcept, Integer>(_componentDimensions);
    }
    
    /** Return a list of all the possible units contained in this derived
     *  dimension.
     *  @return The list of all DerivedUnitConcepts that have this
     *   DerivedDimensionRepresentativeConcept as a representative.
     *  @throws IllegalActionException Thrown if there is a problem getting any
     *   unit concepts from the ontology.
     */
    public List<DerivedUnitConcept> getAllUnits() throws IllegalActionException {
        List<DerivedUnitConcept> result = _getAllUserDefinedUnits();
        
        // Find the given unitName in the list of pre-specified parameters.
        List<Parameter> unitParameterList = attributeList(Parameter.class);
        for (Parameter unitParameter : unitParameterList) {
            Token unitConversionInfo = unitParameter.getToken();
            if (unitConversionInfo instanceof RecordToken) {
                try {
                    String unitConceptString = getName() + "_" + unitParameter.getName();
                    Concept unitConcept = getOntology().getConceptByString(unitConceptString);
                    if (unitConcept instanceof DerivedUnitConcept) {
                        result.add((DerivedUnitConcept) unitConcept);
                    }                    
                } catch (IllegalActionException ex) {
                    throw new IllegalActionException(this, ex, "Error getting unit concepts.");
                    // Do nothing since it was an invalid concept that should
                    // not be added to the list.
                }
            }
        }        
        return result;
    }
    
    /** Return the reference name used by the unit specifications in this
     *  concept for the given dimension name. The reference name allows us
     *  to specify derived dimension representative concepts in MoML that may
     *  depend on other dimension concepts without have to specify those
     *  dependencies until after the ontology model is created. 
     *  @param dimensionName The name of the dimension being referenced.
     *  @return The reference name used in the unit specifications to
     *   refer to the given dimension.
     */
    public String getReferenceNameByDimensionName(String dimensionName) {
        return _dimensionNameToReferenceName.get(dimensionName);
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
        _updateDimensionInformation();
        
        if (containsThisInfiniteConceptString(infiniteConceptString)) {
            String unitName = infiniteConceptString.substring(getName()
                    .length() + 1);
            
            return DerivedUnitConcept.createDerivedUnitConcept(getOntology(),
                    this, _findUnitRecordByName(unitName));           
        } else {
            throw new IllegalActionException(this, "The given string " +
                        infiniteConceptString + " cannot " +
                        "be used to derive a valid derived unit concept contained " +
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
    private void _deriveComponentBaseDimensionsMap() throws IllegalActionException {
        _componentBaseDimensions.clear();
        
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
    
    /** Return the unit info record token with the given Name field. First
     *  look in the array of user defined record tokens, and if it is not
     *  found there then look in the list of pre-specified unit
     *  parameters.
     *  @param unitName The value of the Name field of the unit record token to
     *   be found.
     *  @return The unit info RecordToken with the given Name field.
     *  @throws IllegalActionException Thrown if the unit cannot be found, or
     *   if the unit specification parameter is invalid.
     */
    private RecordToken _findUnitRecordByName(String unitName)
            throws IllegalActionException {
        RecordToken userDefinedRecord = _findUserDefinedUnitRecordByName(unitName);
        if (userDefinedRecord == null) {
            
            // Find the given unitName in the list of pre-specified parameters.
            List<Parameter> unitParameterList = attributeList(Parameter.class);
            for (Parameter unitParameter : unitParameterList) {
                if (unitName.equals(unitParameter.getName())) {
                    Token unitConversionInfo = unitParameter.getToken();
                    if (unitConversionInfo instanceof RecordToken) {
                        RecordToken unitNameRecord = new RecordToken(
                                new String[]{UnitConcept.unitNameLabel},
                                new Token[]{new StringToken(unitName)});
                        return RecordToken.merge(unitNameRecord,
                                (RecordToken) unitConversionInfo);
                        
                    } else {
                        throw new IllegalActionException(this,
                                "Invalid unit specification parameter: " +
                                unitParameter);
                    }
                }
            }
            throw new IllegalActionException(this, "No unit named " + unitName
                    + " for the " + this + " dimension.");
        } else {
            return userDefinedRecord;
        }
    }
    
    /** Return the list of user defined unit concepts within this
     *  DimensionRepresentativeConcept
     *  @return The list of user defined unit concepts.
     *  @throws IllegalActionException Thrown if there is a problem getting the
     *   list of units.
     */
    private List<DerivedUnitConcept> _getAllUserDefinedUnits() throws IllegalActionException {
        List<DerivedUnitConcept> result = new ArrayList<DerivedUnitConcept>();
        
        // The array of user defined unit records is null, return an empty list.
        if (_userDefinedUnitRecords == null) {
            return result;
        } else {
            for (RecordToken unitRecordToken : _userDefinedUnitRecords) {
                Token unitNameToken = unitRecordToken.get(UnitConcept.unitNameLabel);
                if (unitNameToken instanceof StringToken) {
                    String unitName = ((StringToken) unitNameToken).stringValue();
                    Concept unit = getOntology().getConceptByString(getName() + "_" + unitName);
                    if (unit instanceof DerivedUnitConcept) {
                        result.add((DerivedUnitConcept) unit);
                    }
                }
            }
            
            return result;
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
        Token dimensionNameToken = dimensionRecord.get(_dimensionLabel);
        if (dimensionNameToken instanceof StringToken) {
            String dimensionName = ((StringToken) dimensionNameToken).stringValue();
            
            // First see if the name is a reference name that is mapped to a
            // dimension parameter specified in this concept.
            Attribute dimensionAttribute = getAttribute(dimensionName);
            if (dimensionAttribute instanceof Parameter &&
                    ((Parameter) dimensionAttribute).getToken() instanceof ObjectToken) {
                ObjectToken dimensionConceptToken = (ObjectToken) ((Parameter) dimensionAttribute).getToken();
                Object dimensionConceptObject = dimensionConceptToken.getValue();
                if (dimensionConceptObject instanceof DimensionRepresentativeConcept) {
                    _dimensionNameToReferenceName.put(
                            ((DimensionRepresentativeConcept)
                                    dimensionConceptObject).getName(),
                                    dimensionName);
                    return (DimensionRepresentativeConcept) dimensionConceptObject;
                } else {
                    throw new IllegalActionException(this, "Invalid dimension " +
                            "concept: " + dimensionConceptObject);
                }
                
            // Next see if the name refers to a dimension name in the ontology.
            } else {
                Concept dimensionConcept = getOntology().getConceptByString(dimensionName);
                if (dimensionConcept instanceof DimensionRepresentativeConcept) {
                    return (DimensionRepresentativeConcept) dimensionConcept;
                } else {
                    throw new IllegalActionException(this, "Invalid dimension " +
                            "specification: " + dimensionName);
                }
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
        Integer currentExponent = _componentBaseDimensions.get(dimension);
        if (currentExponent == null) {
            _componentBaseDimensions.put(dimension, new Integer(exponentValue));
        } else {
            int newExponentValue = currentExponent.intValue() + exponentValue;
            _componentBaseDimensions.put(dimension, new Integer(newExponentValue));
        }   
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
        _dimensionNameToReferenceName.clear();
        
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
        
        _deriveComponentBaseDimensionsMap();
    }
    
    /** Update the component dimension information for this concept if the
     *  ontology model has changed. Otherwise do nothing. This method is
     *  called every time a new DerivedUnitConcept is created to ensure that
     *  those unit concepts are based on the most recent specification
     *  information in this dimension concept.
     *  @throws IllegalActionException Thrown if there is a problem setting
     *   the dimension information.
     */
    private void _updateDimensionInformation() throws IllegalActionException {
        if (workspace().getVersion() != _dimensionVersion) {
            ArrayToken dimensionArrayToken = (ArrayToken) dimensionArray.getToken();
            if (dimensionArrayToken != null) {
                _setUnitDimensions(dimensionArrayToken);
                _dimensionVersion = workspace().getVersion();
            }
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The map of component base dimensions to the exponents that comprises
     *  this derived dimension.
     */
    private Map<BaseDimensionRepresentativeConcept, Integer> _componentBaseDimensions;
    
    /** The map of component dimensions to the exponents that comprises
     *  this derived dimension.
     */
    private Map<DimensionRepresentativeConcept, Integer> _componentDimensions;
    
    /** The label for the Dimension field for the dimension record token. */
    private static final String _dimensionLabel = "Dimension";
    
    /** The label for the Exponent field for the dimension record token. */
    private static final String _exponentLabel = "Exponent";
    
    /** The map of component dimension names to their reference names in
     *  this dimension concept.
     */
    private Map<String, String> _dimensionNameToReferenceName;
    
    /** The last workspace version at which the cached dimension information
     *  was valid.
     */
    private long _dimensionVersion = -1L;
}
