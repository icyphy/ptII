/* A representative concept in the unitSystem ontology for a set of units for
 * a specific physical dimension.

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

import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
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
@since Ptolemy II 8.0
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
        unitInfoRecords.setTypeEquals(new ArrayType(BaseType.RECORD));
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
     *  @throws IllegalActionException Thrown if there is a problem getting the
     *   token from the unitInfoRecords parameter.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute.equals(unitInfoRecords)) {
            Token unitTokenArrayToken = unitInfoRecords.getToken();
            if (unitTokenArrayToken != null) {
                Token[] unitTokenArray = ((ArrayToken) unitTokenArrayToken).arrayValue();
                _userDefinedUnitRecords = new RecordToken[unitTokenArray.length];
                for (int i = 0; i < unitTokenArray.length; i++) {
                    _userDefinedUnitRecords[i] = (RecordToken) unitTokenArray[i];
                }
            }
        } else {
            super.attributeChanged(attribute);
        }
    }   
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    /** Return a UnitConcept instance from the given concept string
     *  representation. The string must represent one of the units specified
     *  for this physical dimension.
     *  @param infiniteConceptString The string that represents the unit concept
     *   to be returned.
     *  @return The BaseUnitConcept represented by the given string.
     *  @throws IllegalActionException Thrown if there is a problem creating
     *   the unit concept.
     */
    protected abstract UnitConcept _createInfiniteConceptInstance(
            String infiniteConceptString) throws IllegalActionException;
    
    /** Return the user defined unit record with the given Name field that was
     *  specified by the user in the unitInfoRecords parameter. 
     *  @param unitName The value of the Name field of the unit record token to
     *   be found.
     *  @return The unit info RecordToken with the given Name field, or null if
     *   it is not found.
     */
    protected RecordToken _findUserDefinedUnitRecordByName(String unitName) {
        if (_userDefinedUnitRecords == null) {
            return null;
        } else {
            for (RecordToken unitRecordToken : _userDefinedUnitRecords) {
                Token unitNameToken = ((RecordToken) unitRecordToken).
                    get(UnitConcept.unitNameLabel);
                if (unitNameToken instanceof StringToken &&
                        unitName.equals(((StringToken) unitNameToken).stringValue())) {
                    return unitRecordToken;
                }
            }
            return null;
        }
    }    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The array of record tokens specified by the user in the unitInfoRecords
     *  parameter.
     */
    private RecordToken[] _userDefinedUnitRecords = null;
}
