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

<<<<<<< Type.java
=======
import ptolemy.graph.InequalityTerm;
import ptolemy.graph.Inequality;	
import ptolemy.kernel.util.InternalErrorException;
>>>>>>> 1.7
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.Token;

//////////////////////////////////////////////////////////////////////////
//// Type
/**
An interface representing the type of a Token.
All instances of Type must be immutable to avoid circular containment
in structured types.

<<<<<<< Type.java
@author Yuhong Xiong, Steve Neuendorffer
$Id$
=======
@author Steve Neuendorffer
$Id$
>>>>>>> 1.7

*/

<<<<<<< Type.java
public interface Type 
=======
public interface Type extends InequalityTerm
>>>>>>> 1.7
{
<<<<<<< Type.java
    /** Convert the specified token into a token having the type
     *  represented by this object.
     *  @param t a token.
     *  @return a token.
     *  @exception IllegalActionExceptin If lossless conversion
     *   cannot be done.
=======
    /** Given a constraint on this Type, return an enumeration of constraints
     *  on other types, on which this type depends.
     *  In this base class, we assume there is nothing to expand, so return
     *  an enumeration with a single element of the given constraint.
>>>>>>> 1.7
     */
<<<<<<< Type.java
    public Token convert(Token t) throws IllegalActionException;
 
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
=======
    public Enumeration expandConstraint(Inequality constraint);
    
    /** Resolve the given constraints on objects of this type.
     *  Set the values of all the mentioned variables, such that the
     *  values are consistent with all of the constraints.
     *  @param constraints An Enumeration of Inequality objects.
     *  @return An enumeration of objects in which conflict occured.
     */
    //    public Enumeration resolveConstraints(Enumeration constraints);

>>>>>>> 1.7
}

