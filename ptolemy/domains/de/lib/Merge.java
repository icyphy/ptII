/* A merge actor for DE.

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

@ProposedRating Yellow (mudit@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.de.lib;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// Merge
/**
This is a timed merge actor, which merges a set of input signals into
a single output signal.  It has an input port
(a multiport) and an output port (a single port).
The types of the ports are undeclared and will be resolved by the type
resolution mechanism, with the constraint that the output type must be
greater than or equal to the input type. On each call to the fire method, the
actor reads all available tokens from each input channel and sends
them to the output port. In DE, all tokens read in one invocation of the
fire method have the same time stamp.  Thus, the output is a chronological
merging of input events.

@author Edward A. Lee
@version $Id$
*/
public class Merge extends DETransformer  {

    /** Construct an actor in the specified container with the specified
     *  name. Create ports and make the input port a multiport.
     *  @param container The container.
     *  @param name The name.
     *  @exception NameDuplicationException If an actor
     *   with an identical name already exists in the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     */
    public Merge(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input.setMultiport(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read all available tokens from each input channel and
     *  send them to the output port.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        for (int i = 0; i < input.getWidth(); i++) {
            while (input.hasToken(i)) {
                output.send(0, input.get(i));
            }
        }
    }
}
