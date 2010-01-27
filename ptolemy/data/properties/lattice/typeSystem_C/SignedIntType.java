/** A lattice node representing SystemC signed int type.

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

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY


 */
package ptolemy.data.properties.lattice.typeSystem_C;

import ptolemy.data.LongToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.properties.lattice.LatticeProperty;
import ptolemy.data.properties.lattice.PropertyLattice;
import ptolemy.data.properties.lattice.TypeProperty;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// SignedInt

/**
 A lattice node representing SystemC signed int type.

 @author Thomas Mandl
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public abstract class SignedIntType extends LatticeProperty implements
        TypeProperty {

    /** Construct a node named "SignedIntType" in the lattice.
     *  @param lattice The lattice in which the node is to be constructed.   
     */   
    public SignedIntType(PropertyLattice lattice) {
        // FIXME: why is this called SignedIntType and not SignedInt?
        // In SystemC, a SignedIt ia a "sc_int".
        // See http://en.wikipedia.org/wiki/SystemC
        super(lattice, "SignedIntType");
    }
    
    /** Construct a node named "SignedIntType" in the lattice.
     *  @param lattice The lattice in which the node is to be constructed.   
     *  @param name The name, used by subclasses.
     */   
    public SignedIntType(PropertyLattice lattice, String name) {
        // 09/21/09 - Charles Shelton: Additional constructor needed
        // for subclasses that inherit from SignedIntType so that they
        // can also set their name member when declared.

        super(lattice, name);
    }

    /** Return the number of bits.
     *  Derived classes should declare this method to provide different
     *  bit widths.
     *  @return the number of bits   
     */
    public abstract short getNumberBits();

    /** Return true if this element has signed.
     *  @return Always return true.
     */
    public boolean isSigned() {
        return true;
    }

    /** Maximum value of a signed int in System C.
     *  @return The maximum value of an signed int in SystemC,
     *  which is dependent on the number of bits defined by derived
     *  classes.
     */
    public Token getMaxValue() {
        return new LongToken((long) Math.pow(2, getNumberBits() - 1) - 1);
    }

    /** Minimum value of a signed int in System C.
     *  @return The minimum value of an signed int in SystemC,
     *  which is dependent on the number of bits defined by derived
     *  classes.
     */
    public Token getMinValue() {
        return new LongToken((long) -Math.pow(2, getNumberBits() - 1));
    }

    /** Return true if this element has minimum and maximum values.
     *  @return Always return true.
     */
    public boolean hasMinMaxValue() {
        return true;
    }

//    public boolean isInRange(Token token) throws IllegalActionException {
//         // FIXME: Findbugs: Unchecked/unconfirmed cast.
//         // The problem here is that token might not be a ScalarToken.
//         // Is this method used?  Perhaps it can be removed.
//         if ((((ScalarToken) token).longValue() < ((ScalarToken) getMinValue())
//                 .longValue())
//                 || (((ScalarToken) token).longValue() > ((ScalarToken) getMaxValue())
//                         .longValue())) {
//
//             return false;
//         } else {
//             return true;
//         }
//     }
}
