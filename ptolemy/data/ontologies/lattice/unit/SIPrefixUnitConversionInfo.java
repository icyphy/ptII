/* A parameter that holds a record describing unit conversion information for
 * a UnitConcept in a unit system ontology that specifies units with SI prefixes
 * (e.g. kilo-, centi-, milli-).

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

import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// SIPrefixUnitConversionInfo

/** A parameter that holds a record describing unit conversion information for
 *  a UnitConcept in a unit system ontology that specifies units with SI prefixes
 *  (e.g. kilo-, centi-, milli-).

@author Charles Shelton
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (cshelton)
@Pt.AcceptedRating Red (cshelton)
 */
public class SIPrefixUnitConversionInfo extends UnitConversionInfo {

    /** Create a new UnitConversionInfo parameter with the given name and
     *  container.
     *  @param container The DimensionRepresentativeConcept that contains this
     *   parameter.
     *  @param name The name of the parameter.
     *  @exception IllegalActionException Thrown if there is a problem creating
     *   the parameter.
     *  @exception NameDuplicationException Thrown if there is already a NamedObj
     *   in the DimensionRepresentativeConcept container with the same name.
     */
    public SIPrefixUnitConversionInfo(DimensionRepresentativeConcept container,
            String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public static methods             ////

    /** Create all the different unit info parameters that represent SI
     *  prefix-based units for the given dimension.
     *  @param dimension The given DimensionRepresentativeConcept.  It must
     *   be either an SIBaseDimensionRepresentativeConcept or an
     *   SIDerivedDimensionRepresentativeConcept.
     *  @param unitName The name of the base unit with no SI prefix.
     *  @param useSymbols true if the units should use the abbreviations for the
     *   unit prefixes. false if the units should use the full prefix names.
     *  @param baseUnitFactor The base unit factor for unit with no SI prefix.
     *  @param baseUnitRecord The base unit record that represents the component
     *   that make up the unit for an SIDerivedDimensionRepresentativeConcept.
     *   If the dimension is an SIBaseDimensionRepresentativeConcept then this parameter
     *   is ignored.
     *  @exception IllegalActionException Thrown if the input dimension is not an
     *   SIBaseDimensionRepresentativeConcept or an SIDerivedDimensionRepresentativeConcept.
     */
    public static void createAllSIPrefixConversionParameters(
            DimensionRepresentativeConcept dimension, String unitName,
            boolean useSymbols, double baseUnitFactor,
            RecordToken baseUnitRecord) throws IllegalActionException {

        if (dimension instanceof SIBaseDimensionRepresentativeConcept
                || dimension instanceof SIDerivedDimensionRepresentativeConcept) {
            removeAllSIPrefixConversionParameters(dimension);

            // Create the unit factor for the base unit with no prefix.
            createUnitConversionParameterForFactor(dimension, unitName,
                    baseUnitFactor, baseUnitRecord);

            for (SIUnitPrefixes prefix : SIUnitPrefixes.values()) {
                String completeUnitName = null;
                if (useSymbols) {
                    completeUnitName = prefix.prefixSymbol().concat(unitName);
                } else {
                    completeUnitName = prefix.prefixName().concat(unitName);
                }

                createUnitConversionParameterForFactor(dimension,
                        completeUnitName, baseUnitFactor * prefix.unitFactor(),
                        baseUnitRecord);
            }
        } else {
            throw new IllegalActionException(dimension, "The given dimension "
                    + dimension.getName() + "is not an "
                    + "SIBaseDimensionRepresentativeConcept or an "
                    + "SIDerivedDimensionRepresentativeConcept so no "
                    + "SI prefix units can be created.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private static methods            ////

    /** Create a new SIPrefixUnitConversionInfo parameter for the given
     *  DimensionRepresentativeConcept with the given unit name and
     *  conversion factor.
     *  @param dimension The given DimensionRepresentativeConcept.
     *  @param unitName The name of the new unit to be added.
     *  @param unitFactor The conversion factor for the new unit.
     *  @param unitRecord The base unit record for a derived dimension.  If the
     *   dimension is a base dimension, this parameter should be null.
     *  @exception IllegalActionException Thrown if there is a problem creating
     *   the parameter.
     */
    private static void createUnitConversionParameterForFactor(
            DimensionRepresentativeConcept dimension, String unitName,
            double unitFactor, RecordToken unitRecord)
                    throws IllegalActionException {
        SIPrefixUnitConversionInfo unitParameter = null;
        try {
            unitParameter = new SIPrefixUnitConversionInfo(dimension, unitName);
            unitParameter.setVisibility(NOT_EDITABLE);
        } catch (NameDuplicationException nameDup) {
            throw new IllegalActionException(dimension, nameDup,
                    "Error creating SI unit factor: factor parameter "
                            + unitName + " already exists.");
        }

        unitParameter.setToken(new RecordToken(
                new String[] { UnitConversionInfo.unitFactorLabel },
                new Token[] { new DoubleToken(unitFactor) }));
        if (dimension instanceof SIDerivedDimensionRepresentativeConcept) {
            if (unitRecord != null) {
                unitParameter.setToken(RecordToken.merge(unitRecord,
                        (RecordToken) unitParameter.getToken()));
            } else {
                throw new IllegalActionException(dimension, "Error creating "
                        + "SI unit factor: The dimension is an "
                        + "SIDerivedDimensionRepresentativeConcept "
                        + "but the base unitRecord is null.");
            }
        }
    }

    /** Remove all the existing SIPrefixUnitConversionInfo parameters
     *  from the given DimensionRepresentativeConcept.
     *  @param dimension The DimensionRepresentativeConcept from which to
     *   remove the parameters.
     *  @exception IllegalActionException Thrown if there is a problem deleting
     *   any of the parameters.
     */
    private static void removeAllSIPrefixConversionParameters(
            DimensionRepresentativeConcept dimension)
                    throws IllegalActionException {
        for (SIPrefixUnitConversionInfo prefixUnitParameter : dimension
                .attributeList(SIPrefixUnitConversionInfo.class)) {
            try {
                prefixUnitParameter.setContainer(null);
            } catch (NameDuplicationException nameDup) {
                throw new IllegalActionException(dimension, nameDup,
                        "Error removing SI unit factor "
                                + prefixUnitParameter.getName() + ".");
            }
        }
    }
}
