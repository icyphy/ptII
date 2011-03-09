/* A representative concept in the unitSystem ontology for a set of units for
 * a specific physical dimension that is one of the 7 base dimensions.

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

import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.ontologies.Ontology;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// BaseDimensionRepresentativeConcept

/** A representative concept in the unitSystem ontology for a set of units for
 *  a specific physical dimension that is one of the 7 base dimensions.
 *  
 *  There are 7 base dimensions from which all other unit dimensions are derived. These are
 *  specified in the unitSystem ontology by 7 BaseUnitRepresentativeConcepts: 
 *  <ul>
 *  <li>Mass - SI unit kilograms (kg)
 *  <li>Position - SI unit meters (m)
 *  <li>Time - SI unit seconds (sec)
 *  <li>Current - SI unit amperes (amp)
 *  <li>Temperature - SI unit Kelvin (K)
 *  <li>Amount of substance - SI unit moles (mol)
 *  <li>Intensity of light - SI unit candela (cd)
 *  </ul>
@see BaseUnitConcept
@author Charles Shelton
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating Red (cshelton)
@Pt.AcceptedRating Red (cshelton)
*/
public class BaseDimensionRepresentativeConcept extends DimensionRepresentativeConcept {

    /** Create a new BaseUnitRepresentativeConcept with the specified name and
     *  ontology.
     *  
     *  @param ontology The specified ontology where this concept resides.
     *  @param name The specified name for the concept.
     *  @exception NameDuplicationException If the ontology already contains a
     *   concept with the specified name.
     *  @exception IllegalActionException If the base class throws it.
     */
    public BaseDimensionRepresentativeConcept(Ontology ontology, String name)
            throws NameDuplicationException, IllegalActionException {
        super(ontology, name);
        unitFactors = new Parameter(this, "unitFactors");
        unitOffsets = new Parameter(this, "unitOffsets");
        unitFactors.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        unitOffsets.setTypeEquals(new ArrayType(BaseType.DOUBLE));
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
    /** The array of multiplication factors for the units for this dimension. */
    public Parameter unitFactors;
    
    /** The array of offsets for the units for this dimension. */
    public Parameter unitOffsets;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Return a BaseUnitConcept instance from the given concept string
     *  representation. The string must represent one of the units specified
     *  for this physical dimension.
     *  @param infiniteConceptString The string that represents the unit concept
     *   to be returned.
     *  @return The BaseUnitConcept represented by the given string.
     *  @throws IllegalActionException Thrown if there is a problem creating
     *   the unit concept.
     */
    protected BaseUnitConcept _createInfiniteConceptInstance(
            String infiniteConceptString) throws IllegalActionException {
        
        if (containsThisInfiniteConceptString(infiniteConceptString)) {
            String unitName = infiniteConceptString.substring(getName()
                    .length() + 1);
            
            Token[] unitNamesArray = ((ArrayToken) unitNames.getToken()).arrayValue();            
            int index = 0;
            for (Token unitNameToken : unitNamesArray) {
                if (unitName.equals(((StringToken) unitNameToken).stringValue())) {
                    try {
                        DoubleToken unitFactorToken = (DoubleToken)
                            ((ArrayToken) unitFactors.getToken()).
                                getElement(index);
                        
                        DoubleToken unitOffsetToken = null;
                        if (unitOffsets.getToken() == null ||
                                unitOffsets.getToken().equals(Token.NIL)) {
                            unitOffsetToken = DoubleToken.ZERO;
                        } else {
                            unitOffsetToken = (DoubleToken) ((ArrayToken)
                                    unitOffsets.getToken()).getElement(index);
                        }
                        
                        Token[] valuesArray = new Token[]{unitNameToken,
                                                            unitFactorToken,
                                                            unitOffsetToken};
                        
                        RecordToken unitRecord = new RecordToken(
                                BaseUnitConcept.unitRecordLabelArray, valuesArray);
                        return BaseUnitConcept.createBaseUnitConcept(
                                getOntology(), this, unitRecord);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        throw new IllegalActionException(this,
                                "No matching unit factor or offset for the " +
                                "unit named: " +
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
}
