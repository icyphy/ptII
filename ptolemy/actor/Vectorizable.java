/* Interface for actors that can be vectorized.

 Copyright (c) 1997-2000 The Regents of the University of California.
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
@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating Red (vogel@eecs.berkeley.edu)
*/

package ptolemy.actor;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// Vectorizable
/**
This interface defines the setVectorLength() method. It should be 
implemented by actors that can be vectorized. The 
setVectorLength() method is used to set the vector length, and is
typically invoked by a director. The vector length is a nonnegative
integer that defines the number of tokens that the actor consumes 
and/or produces when fire() or postfire() is invoked. Many domains
do not support vectorized actors, so a vectorizable actor should 
use a vector length of 1 by default to promote domain polymorphism. 
A director can then change the vector length via setVectorLength() 
if the actor is used in a domain that supports vectorized actors.

@author Brian K. Vogel
@version $Id$
*/
public interface Vectorizable {

    /** Set the vector length. The vector length of an actor is
     *  defined as the number of tokens that are consumed and/or
     *  produced when the actor is fired. If a vectorizable actor
     *  is used in a domain that supports vectorized actors, then
     *  the director may invoke this method to set the vector length.
     *  This method is not guaranteed to be invoked, so a 
     *  vectorizable actor should use a default vector length of 1.
     *  An exception will occur if the requested vector length is
     *  less than 1.
     *  @param vectorLength The requested vector length to use for
     *   the actor.
     *
     *  @exception IllegalActionException If the requested vector length
     *   is invalid or cannot be set.
     */
    public void setVectorLength(int vectorLength) 
	throws IllegalActionException;

}
