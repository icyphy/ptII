/* A base class for vectorizable actors that transform an input stream into an output stream.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// VectorizableTransformer
/**
This is a base class for vectorizable actors that transform
an input stream into an output stream. It provides a
setVectorLength() method to set the vector length. The
vector length defines the number of tokens that an actor
consumes and/or produces on each firing. Vectorizable actors
should use the vectorized versions of the IOPort.get() and
IOPort.send() methods.

@author Brian K. Vogel.
@version $Id$
*/

public class VectorizableTransformer extends Transformer implements Vectorizable {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public VectorizableTransformer(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
	// Set the default vector length.
	_vectorLength = 1;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
	throws IllegalActionException {
	if (vectorLength > 0) {
	    _vectorLength = vectorLength;
	} else {
	    throw new IllegalActionException(this,
		     "Attemp to set an invalid vector length. The " +
		     "vector length must be a positive integer. " +
                     "Attempted to set vector length = " + vectorLength);
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables                 ////

    protected int _vectorLength;

}
