/* An interface for the unitSystem ontology base and derived unit concepts.

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

///////////////////////////////////////////////////////////////////
//// UnitInformation

/** An interface for the unitSystem ontology base and derived unit concepts.

@author Charles Shelton
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating Red (cshelton)
@Pt.AcceptedRating Red (cshelton)
*/
public interface UnitInformation {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Return true if this unit can be converted to the specified unit. 
     *  @param unit The other unit concept to compare to this one.
     *  @return true if the units can be converted, false otherwise.
     */
    public boolean canBeConvertedTo(UnitInformation unit);
    
    /** Return the multiplication factor that converts a value in this unit to the
     *  SI unit for this dimension.
     *  @return The unit factor as a double value.
     */
    public double getUnitFactor();
    
    /** Return the name of the unit.
     *  @return The name of the unit.
     */
    public String getUnitName();
    
    /** Return the offset factor that converts a value in this unit to the SI
     *  unit for this dimension. Currently this is only used for temperature
     *  unit conversions.
     *  @return The unit offset as a double value.
     */
    public double getUnitOffset();
}
