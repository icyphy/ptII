/* A representative concept in the unitSystem ontology for a set of units for
 * a specific physical dimension that is one of the 7 SI specified base dimensions.

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

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// SIBaseDimensionRepresentativeConcept

/** A representative concept in the unitSystem ontology for a set of units for
 *  a specific physical dimension that is one of the 7 SI specified base dimensions.
 *
 *  There are 7 base dimensions from which all other unit dimensions are
 *  derived. These can be specified in a units system ontology by 7
 *  BaseUnitRepresentativeConcepts:
 *  <ul>
 *  <li>Mass - SI unit kilograms (kg)
 *  <li>Position - SI unit meters (m)
 *  <li>Time - SI unit seconds (sec)
 *  <li>Current - SI unit amperes (amp)
 *  <li>Temperature - SI unit Kelvin (K)
 *  <li>Amount of substance - SI unit moles (mol)
 *  <li>Intensity of light - SI unit candela (cd)
 *  </ul>
@author Charles Shelton
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (cshelton)
@Pt.AcceptedRating Red (cshelton)
 */
public class SIBaseDimensionRepresentativeConcept extends
BaseDimensionRepresentativeConcept {

    /** Create a new SIBaseUnitRepresentativeConcept with the specified name and
     *  ontology.
     *
     *  @param ontology The specified ontology where this concept resides.
     *  @param name The specified name for the concept.
     *  @exception NameDuplicationException If the ontology already contains a
     *   concept with the specified name.
     *  @exception IllegalActionException If the base class throws it.
     */
    public SIBaseDimensionRepresentativeConcept(CompositeEntity ontology,
            String name) throws NameDuplicationException,
            IllegalActionException {
        super(ontology, name);
        unitFullName = new StringAttribute(this, "unitFullName");
        unitAbbreviation = new StringAttribute(this, "unitAbbreviation");
        useAbbreviation = new Parameter(this, "useAbbreviation");
        useAbbreviation.setTypeEquals(BaseType.BOOLEAN);

        // Default to using the unit abbreviation rather than the full name.
        useAbbreviation.setToken(BooleanToken.TRUE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The name of the base SI unit for this dimension. */
    public StringAttribute unitFullName;

    /** The abbreviated symbol for the base SI unit for this dimension. */
    public StringAttribute unitAbbreviation;

    /** Boolean parameter that indicates whether the unit names for this
     *  dimension should use the abbreviated symbols for the name and prefixes
     *  or the full unit name and prefixes.
     */
    public Parameter useAbbreviation;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in the unit name and abbreviation parameters. Update
     *  the SI unit conversion parameters based on the values of these
     *  parameters.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException Thrown if there is a problem creating
     *   the SIPrefixUnitConversionInfo parameters.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {

        boolean useUnitAbbreviation = ((BooleanToken) useAbbreviation
                .getToken()).booleanValue();

        // If not using unit abbreviations, react to a change in the
        // unitFullName parameter.
        if ((attribute.equals(useAbbreviation) || attribute
                .equals(unitFullName)) && !useUnitAbbreviation) {

            SIPrefixUnitConversionInfo.createAllSIPrefixConversionParameters(
                    this, unitFullName.getValueAsString(), useUnitAbbreviation,
                    1.0, null);

            // If using unit abbreviations, react to a change in the
            // unitAbbreviation parameter.
        } else if ((attribute.equals(useAbbreviation) || attribute
                .equals(unitAbbreviation)) && useUnitAbbreviation) {

            SIPrefixUnitConversionInfo.createAllSIPrefixConversionParameters(
                    this, unitAbbreviation.getValueAsString(),
                    useUnitAbbreviation, 1.0, null);
        } else {
            super.attributeChanged(attribute);
        }
    }
}
