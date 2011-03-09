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
package ptolemy.data.ontologies.lattice.adapters.unitSystem;

import ptolemy.data.expr.Parameter;
import ptolemy.data.ontologies.FlatTokenRepresentativeConcept;
import ptolemy.data.ontologies.Ontology;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
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
    public DimensionRepresentativeConcept(Ontology ontology, String name)
            throws NameDuplicationException, IllegalActionException {
        super(ontology, name);
        unitNames = new Parameter(this, "unitNames");
        unitNames.setTypeEquals(new ArrayType(BaseType.STRING));
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
    /** The array of names for the units for this dimension. */
    public Parameter unitNames;
    
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
    
    ///////////////////////////////////////////////////////////////////
    ////                         public inner classes              ////
    
    /** The enumeration type that represents the 7 base dimensions for the
     *  unit system.
     */
    public enum BaseDimensionType {
        /** The Mass Dimension. */
        MASS (0, "Mass"),
        
        /** The Position Dimension. */
        POSITION (1, "Position"),
        
        /** The Time Dimension. */
        TIME (2, "Time"),
        
        /** The Current Dimension. */
        CURRENT (3, "Current"),
        
        /** The Temperature Dimension. */
        TEMPERATURE (4, "Temperature"),
        
        /** The Substance Dimension. */
        SUBSTANCE (5, "Substance"),
        
        /** The Light Intensity Dimension. */
        LIGHTINTENSITY (6, "LightIntensity");        
        
        /** Instantiate a BaseDimensionType enum with the specified index
         *  and name.
         *  @param index The index for the base dimension.
         *  @param name The name of the base dimension concept.
         */
        BaseDimensionType(int index, String name) {
            _index = index;
            _dimensionConceptName = name;
        }
        
        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////
        
        /** Get the index value for the BaseDimensionType.
         *  @return The integer index value.
         */
        public int getIndex() {
            return _index;
        }
        
        /** Get the BaseDimensionType enum that has the given string name.
         *  @param baseDimensionName The name of the base dimension.
         *  @return The BaseDimensionType enum with the given name.
         */
        public static BaseDimensionType getBaseDimensionTypeByName(
                String baseDimensionName) {
            for (BaseDimensionType dimension : BaseDimensionType.values()) {
                if (dimension._dimensionConceptName.equals(baseDimensionName)) {
                    return dimension;
                }
            }            
            return null;
        }
        
        /** Return the number of base dimensions specified in the
         *  BaseDimensionType enum type.
         *  @return The number of base dimensions.
         */
        public static int numBaseDimensions() {
            return BaseDimensionType.values().length;
        }
        
        ///////////////////////////////////////////////////////////////////
        ////                         private variables                 ////
        
        /** The index for the base dimension type enum. */
        private final int _index;
        
        /** The name of the base dimension type enum. */
        private final String _dimensionConceptName;
    }
}
