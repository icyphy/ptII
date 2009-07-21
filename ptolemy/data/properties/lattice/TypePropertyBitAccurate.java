/* An interface for classes that are bit accurate at the type propertly level.
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2009 The Regents of the University of California.
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
*/
package ptolemy.data.properties.lattice;

//////////////////////////////////////////////////////////////////////////
//// TypePropertyBitAccurate

/**
 An interface for classes that are bit accurate at the type propertly level.


 @author Charles Shelton (Bosch)
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public interface TypePropertyBitAccurate extends TypeProperty {

    /** Return true if the property is signed.
     *  @return true if the property is signed.
     */
    public boolean isSigned();

    /** Return the number of bits.
     * @return The number of bits.
     */
    public short getNumberBits();
}
