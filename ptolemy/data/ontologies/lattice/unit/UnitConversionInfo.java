/* A parameter that holds a record describing unit conversion information for
 * a UnitConcept in a unit system ontology.

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

import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// UnitConversionInfo

/** A parameter that holds a record describing unit conversion information for
 *  a UnitConcept in a unit system ontology. A DimensionRepresentativeConcept
 *  uses the record token contained in this parameter to generate a particular
 *  UnitConcept in its dimension.

@author Charles Shelton
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (cshelton)
@Pt.AcceptedRating Red (cshelton)
 */
public class UnitConversionInfo extends Parameter {

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
    public UnitConversionInfo(DimensionRepresentativeConcept container,
            String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);
        setTypeAtMost(BaseType.RECORD);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The name label for the unit record token information when constructing
     *  a new UnitConcept.
     */
    public static final String unitNameLabel = "Name";

    /** The factor label for the unit record token information when constructing
     *  a new UnitConcept.
     */
    public static final String unitFactorLabel = "Factor";

    /** The offset label for the unit record token information when constructing
     *  a new UnitConcept.
     */
    public static final String unitOffsetLabel = "Offset";

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the setContainer method to only allow a UnitConversionInfo
     *  parameter to be contained by DimensionRepresentativeConcepts.
     *  @param container The new container for this parameter, which must either
     *   be a DimensionRepresentativeConcept or null.
     *  @exception IllegalActionException Thrown if the new container is not
     *   a DimensionRepresentativeConcept.
     *  @exception NameDuplicationException Thrown if there is already a NamedObj
     *   in the DimensionRepresentativeConcept container with the same name.
     */
    @Override
    public void setContainer(NamedObj container) throws IllegalActionException,
    NameDuplicationException {
        if (container == null
                || container instanceof DimensionRepresentativeConcept) {
            super.setContainer(container);
        } else {
            throw new IllegalActionException(this, "A UnitConversionInfo "
                    + "parameter must be contained by a "
                    + "DimensionRepresentativeConcept.");
        }
    }
}
