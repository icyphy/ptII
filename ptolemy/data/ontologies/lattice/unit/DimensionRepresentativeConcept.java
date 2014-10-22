/* A representative concept in the unitSystem ontology for a set of units for
 * a specific physical dimension.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
import java.util.List;

import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.FlatTokenRepresentativeConcept;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// DimensionRepresentativeConcept

/** A representative concept in the unitSystem ontology for a set of units for
 *  a specific physical dimension. This is an abstract base class for base and
 *  derived unit dimensions.

@see BaseDimensionRepresentativeConcept
@see DerivedDimensionRepresentativeConcept
@author Charles Shelton
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (cshelton)
@Pt.AcceptedRating Red (cshelton)
 */
public abstract class DimensionRepresentativeConcept extends
        FlatTokenRepresentativeConcept {

    /** Create a new DimensionRepresentativeConcept with the specified name and
     *  ontology.
     *
     *  @param ontology The specified ontology where this concept resides.
     *  @param name The specified name for the concept.
     *  @exception NameDuplicationException If the ontology already contains a
     *   concept with the specified name.
     *  @exception IllegalActionException If the base class throws it.
     */
    public DimensionRepresentativeConcept(CompositeEntity ontology, String name)
            throws NameDuplicationException, IllegalActionException {
        super(ontology, name);
        unitInfoRecords = new Parameter(this, "unitInfoRecords");
        unitInfoRecords.setTypeAtMost(new ArrayType(BaseType.RECORD));
        _unitList = new ArrayList<UnitConcept>();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The array of information records for the units for this dimension. */
    public Parameter unitInfoRecords;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in the unitInfoRecords parameter. Update the array
     *  of user defined unit specification record tokens based on the value
     *  of the unitInfoRecords parameter.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException Thrown if there is a problem getting the
     *   token from the unitInfoRecords parameter.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute.equals(unitInfoRecords)) {
            Token unitTokenArrayToken = unitInfoRecords.getToken();
            if (unitTokenArrayToken != null) {
                Token[] unitTokenArray = ((ArrayToken) unitTokenArrayToken)
                        .arrayValue();
                _userDefinedUnitRecords = new RecordToken[unitTokenArray.length];
                for (int i = 0; i < unitTokenArray.length; i++) {
                    _userDefinedUnitRecords[i] = (RecordToken) unitTokenArray[i];
                }
            } else {
                _userDefinedUnitRecords = null;
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Return a list of all the possible units contained in this
     *  dimension representative concept.
     *  @return The list of all UnitConcepts that have this
     *   DimensionRepresentativeConcept as a representative.
     *  @exception IllegalActionException Thrown if there is a problem getting any
     *   unit concepts from the ontology.
     */
    public List<? extends UnitConcept> getAllUnits()
            throws IllegalActionException {
        if (_unitListVersion != workspace().getVersion()) {
            _unitList = _getAllUserDefinedUnits();

            // Find the given unitName in the list of pre-specified parameters.
            List<UnitConversionInfo> unitParameterList = attributeList(UnitConversionInfo.class);
            for (UnitConversionInfo unitParameter : unitParameterList) {
                try {
                    String unitConceptString = getName() + "_"
                            + unitParameter.getName();
                    Concept unitConcept = getOntology().getConceptByString(
                            unitConceptString);
                    if (unitConcept instanceof UnitConcept
                            && this.equals(((UnitConcept) unitConcept)
                                    .getDimension())) {
                        _unitList.add((UnitConcept) unitConcept);
                    }
                } catch (IllegalActionException ex) {
                    throw new IllegalActionException(this, ex,
                            "Error getting unit concepts.");
                }
            }
        }
        return _unitList;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a UnitConcept instance from the given concept string
     *  representation. The string must represent one of the units specified
     *  for this physical dimension.
     *  @param infiniteConceptString The string that represents the unit concept
     *   to be returned.
     *  @return The BaseUnitConcept represented by the given string.
     *  @exception IllegalActionException Thrown if there is a problem creating
     *   the unit concept.
     */
    @Override
    protected abstract UnitConcept _createInfiniteConceptInstance(
            String infiniteConceptString) throws IllegalActionException;

    /** Return the unit info record token with the given Name field. First
     *  look in the array of user defined record tokens, and if it is not
     *  found there then look in the list of pre-specified unit
     *  parameters.
     *  @param unitName The value of the Name field of the unit record token to
     *   be found.
     *  @return The unit info RecordToken with the given Name field.
     *  @exception IllegalActionException Thrown if the unit cannot be found, or
     *   if the unit specification parameter is invalid.
     */
    protected RecordToken _findUnitRecordByName(String unitName)
            throws IllegalActionException {
        RecordToken userDefinedRecord = _findUserDefinedUnitRecordByName(unitName);
        if (userDefinedRecord == null) {

            // Find the given unitName in the list of pre-specified parameters.
            List<UnitConversionInfo> unitParameterList = attributeList(UnitConversionInfo.class);
            for (UnitConversionInfo unitParameter : unitParameterList) {
                if (unitName.equals(unitParameter.getName())) {
                    RecordToken unitConversionInfoRecord = (RecordToken) unitParameter
                            .getToken();
                    if (unitConversionInfoRecord == null) {
                        throw new IllegalActionException(this,
                                "Invalid unit specification parameter: "
                                        + unitParameter);
                    } else {
                        RecordToken unitNameRecord = new RecordToken(
                                new String[] { UnitConversionInfo.unitNameLabel },
                                new Token[] { new StringToken(unitName) });
                        return RecordToken.merge(unitNameRecord,
                                unitConversionInfoRecord);
                    }
                }
            }
            throw new IllegalActionException(this, "No unit named " + unitName
                    + " for the " + this + " dimension.");
        } else {
            return userDefinedRecord;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the user defined unit record with the given Name field that was
     *  specified by the user in the unitInfoRecords parameter.
     *  @param unitName The value of the Name field of the unit record token to
     *   be found.
     *  @return The unit info RecordToken with the given Name field, or null if
     *   it is not found.
     */
    private RecordToken _findUserDefinedUnitRecordByName(String unitName) {
        if (_userDefinedUnitRecords == null) {
            return null;
        } else {
            for (RecordToken unitRecordToken : _userDefinedUnitRecords) {
                Token unitNameToken = unitRecordToken
                        .get(UnitConversionInfo.unitNameLabel);
                if (unitNameToken instanceof StringToken
                        && unitName.equals(((StringToken) unitNameToken)
                                .stringValue())) {
                    return unitRecordToken;
                }
            }
            return null;
        }
    }

    /** Return the list of user defined unit concepts within this
     *  DimensionRepresentativeConcept
     *  @return The list of user defined unit concepts.
     *  @exception IllegalActionException Thrown if there is a problem getting the
     *   list of units.
     */
    private List<UnitConcept> _getAllUserDefinedUnits()
            throws IllegalActionException {
        List<UnitConcept> result = new ArrayList<UnitConcept>();

        // The array of user defined unit records is null, return an empty list.
        if (_userDefinedUnitRecords == null) {
            return result;
        } else {
            for (RecordToken unitRecordToken : _userDefinedUnitRecords) {
                Token unitNameToken = unitRecordToken
                        .get(UnitConversionInfo.unitNameLabel);
                if (unitNameToken instanceof StringToken) {
                    String unitName = ((StringToken) unitNameToken)
                            .stringValue();
                    Concept unit = getOntology().getConceptByString(
                            getName() + "_" + unitName);
                    if (unit instanceof DerivedUnitConcept) {
                        result.add((DerivedUnitConcept) unit);
                    }
                }
            }

            return result;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The array of record tokens specified by the user in the unitInfoRecords
     *  parameter.
     */
    protected RecordToken[] _userDefinedUnitRecords = null;

    /** The list of valid units for this dimension representative concept. */
    private List<UnitConcept> _unitList;

    /** The last workspace version at which the cached list of unit concepts
     *  was valid.
     */
    private long _unitListVersion = -1L;
}
