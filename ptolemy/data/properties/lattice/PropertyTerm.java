/** A class representing a property term factory.

 Copyright (c) 1997-2009 The Regents of the University of California.
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

package ptolemy.data.properties.lattice;

import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;


//////////////////////////////////////////////////////////////////////////
//// PropertyTerm

/**
 An interface for a property term.
 A term is either a constant, a variable, or a function. 

 @author Man-kit Leung
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public interface PropertyTerm extends InequalityTerm {

    /** Return an array of constants contained in this term.
     *  If this term is a variable, return an array of size zero;
     *  if this term is a constant, return an array of size one that
     *  contains this constants; if this term is a function, return an
     *  array containing all the constants in the function.
     *  @return An array of InequalityTerms
     */
    public InequalityTerm[] getConstants();

    /** Get the value of this PropertyTerm.
     *  @return the value of this PropertyTerm.   
     */
    public Object getValue() throws IllegalActionException;

    /**
     * Return true if this property term is effective.
     * @return true if this property term is effective.
     * @see #setEffective(boolean)
     */
    public boolean isEffective();

    /**
     * Set the effectiveness of this property term to the specified value.
     * @param isEffective The specified effective value.
     * @see #isEffective()
     */
    public void setEffective(boolean isEffective);
}
