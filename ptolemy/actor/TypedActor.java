/* Interface for actors whose ports have types.

Copyright (c) 1997-2005 The Regents of the University of California.
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
package ptolemy.actor;

import java.util.List;

import ptolemy.kernel.util.IllegalActionException;


//////////////////////////////////////////////////////////////////////////
//// TypedActor

/**
   A TypedActor is an actor whose ports have types.
   This interface defines the method to get type constraints from
   an actor.  The ports on a TypedActor are constrainted to be TypedIOPorts.

   @author Yuhong Xiong
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Green (yuhong)
   @Pt.AcceptedRating Green (lmuliadi)
   @see ptolemy.actor.TypedCompositeActor
   @see ptolemy.actor.TypedAtomicActor
   @see ptolemy.actor.TypedIOPort
*/
public interface TypedActor extends Actor {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the type constraints of this actor.
     *  The constraints is a list of inequalities.
     *  @return a list of Inequality.
     *  @exception IllegalActionException If type conflict is detected
     *   during static type checking.
     *  @see ptolemy.graph.Inequality
     */
    public List typeConstraintList() throws IllegalActionException;
}
