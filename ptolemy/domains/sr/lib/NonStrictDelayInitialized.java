/* A nonstrict actor that delays tokens by one iteration and outputs
 * an intial token.

 Copyright (c) 1997-2001 The Regents of the University of California.
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

@ProposedRating Red (celaine@eecs.berkeley.edu)
@AcceptedRating Red (celaine@eecs.berkeley.edu)
*/

package ptolemy.domains.sr.lib;

import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.CompositeEntity;

//////////////////////////////////////////////////////////////////////////
//// NonstrictDelayInitialized
/**
This actor implements a token delay with an initial value.  It has one
input port and one output port, both of which are single ports.  A
token that is received on the input port is sent on the output port on
the next iteration.  If more than one token is received on the input
port in a given iteration, only the final token is output on the next
iteration.  If no tokens are received in a given iteration, no token
is output on the next iteration.  An initial token with the value of
the <i>initialValue</i> parameter is emitted in the first iteration.

@author Elaine Cheong
@version $Id$
*/

public class NonStrictDelayInitialized extends NonStrictDelay {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public NonStrictDelayInitialized(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        initialValue = new Parameter(this, "initialValue");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Initial token value.
     */
    public Parameter initialValue;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the buffer variables (set intial token).
     *  @exception IllegalActionException If there is no director.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        _previousToken = initialValue.getToken();
    }
}


