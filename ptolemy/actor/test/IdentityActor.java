/* An identity actor for testing.

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
@ProposedRating Yellow (liuj@eecs.berkeley.edu)
*/

package ptolemy.actor.test;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// IdentityActor
/**
An IdentityActor is a simple atomic actor that transfers the input token
to the output. This actor is used in tests that needs to emulate the
fire method. This actor has a single input port and a single output port,
with the name "input" and "output" respectively.

@author  Jie Liu
@version $Id$
*/
public class IdentityActor extends AtomicActor {
    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public IdentityActor(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new IOPort(this, "input");
        input.setInput(true);
        input.setOutput(false);
        input.setMultiport(false);
        output = new IOPort(this, "output");
        output.setInput(false);
        output.setOutput(true);
        output.setMultiport(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire the actor; transfer the input to the output.
     *  @exception IllegalActionException If there's no token
     *      in the input port.
     */
    public void fire() throws IllegalActionException {
        try {
            Token in = input.get(0);
            output.broadcast(in);
        }catch(NoTokenException e) {
            throw new IllegalActionException( this,
                    " No token available when firing.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public  variables                 ////
    public IOPort input;
    public IOPort output;

}
