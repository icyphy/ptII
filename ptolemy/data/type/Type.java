/** An Interface representing the Type of a Token.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.data.type;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.Token;

//////////////////////////////////////////////////////////////////////////
//// Type
/**
An interface representing the type of a Token.
All instances of Type must be immutable to avoid circular containment
in structured types.

@author Yuhong Xiong, Steve Neuendorffer
@version $Id$
*/

public interface Type 
{
    /** Convert the specified token into a token having the type
     *  represented by this object.
     *  @param t a token.
     *  @return a token.
     *  @exception IllegalActionExceptin If lossless conversion
     *   cannot be done.
     */
    public Token convert(Token t) throws IllegalActionException;
 
    /** Test if the argument token is compatible with this type.
     *  Compatible is defined as follows: If this type is a constant, the
     *  argument is compatible if it can be converted losslessly to a token
     *  of this type; If this type is a variable, the argument is compatible
     *  if its type is a substitution instance of this type, or if it can
     *  be converted losslessly to a substitution instance of this type.
     *  @param t A Token.
     *  @return True if the argument is compatible with this type.
     */
    public boolean isCompatible(Token t);

    /** Test if this Type is a constant. A Type is a constant if it
     *  does not contain BaseType.NAT in any level within it.
     *  @return True if this type is a constant.
     */
    public boolean isConstant();

    /** Determine if the argument represents the same type as this object.
     *  @param t A Type.
     *  @return True if the argument represents the same type as this
     *   object; false otherwise.
     */
    public boolean isEqualTo(Type t);

    /** Determine if this Type corresponds to an instantiable token
     *  class.
     *  @return True if this type corresponds to an instantiable
     *   token class.
     */
    public boolean isInstantiable();

    /** Return true if the specified type is a substitution instance of this
     *  type. For the argument to be a substitution instance, this type must
     *  be a variable, and the argument must be a type that can be obtained
     *  by replacing the BaseType.NAT component of this type by another type.
     *  @parameter type A Type.
     *  @return True is the argument is a substitution instance of this type.
     */
    public boolean isSubstitutionInstance(Type type);

    /** Return the string representation of this type.
     *  @return A String.
     */
    public String toString();
}

