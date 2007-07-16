/** An Interface representing a property.

 Copyright (c) 1997-2006 The Regents of the University of California.
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
package ptolemy.data.properties;



//////////////////////////////////////////////////////////////////////////
//// Property

/**
 An interface representing a property.

 @author Thomas Mandl, Man-Kit Leung, Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (cxh)
 */
public class Property {
    
    /** Test if the argument property is compatible with this property.
     *  Compatible is defined as follows: If this property is a constant, the
     *  argument is compatible if it is the same or less than this property in
     *  the property lattice; If this property is a variable, the argument is
     *  compatible if it is a substitution instance of this property.
     *  @param property An instance of Property.
     *  @return True if the argument is compatible with this property.
     */
    public boolean isCompatible(Property property) {
        throw new AssertionError("Not supported in Base class.");
    }
    /** Test if this property is a constant. A property is a constant if it
     *  does not contain the bottom of the property lattice in any level within it.
     *  @return True if this property is a constant.
     */
    public boolean isConstant() {
        throw new AssertionError("Not supported in Base class.");
    }

    /** Determine if this Type corresponds to an instantiable token
     *  class.
     *  @return True if this type corresponds to an instantiable
     *   token class.
     */
    public boolean isInstantiable() {
        throw new AssertionError("Not supported in Base class.");
    }
    /** Return true if the specified property is a substitution instance of this
     *  property. For the argument to be a substitution instance, it must be
     *  either the same as this property, or it must be a property that can be
     *  obtained by replacing the Baseproperty.UNKNOWN component of this property by
     *  another property.
     *  @param property A property.
     *  @return True if the argument is a substitution instance of this property.
     */
    public boolean isSubstitutionInstance(Property property) {
        throw new AssertionError("Not supported in Base class.");
    }
}