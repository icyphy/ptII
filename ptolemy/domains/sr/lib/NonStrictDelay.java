/* A nonstrict actor that delays tokens by one iteration.

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

@ProposedRating Red (pwhitake@eecs.berkeley.edu)
@AcceptedRating Red (pwhitake@eecs.berkeley.edu)
*/

package ptolemy.domains.sr.lib;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.Token;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.CompositeEntity;

//////////////////////////////////////////////////////////////////////////
//// NonstrictDelay
/**
This actor implements a token delay.  It has one input port and
one output port, both of which are single ports.  A token that is received
on the input port is sent on the output port on the next iteration.  If more
than one token is received on the input port in a given iteration, only
the final token is output on the next iteration.  If no tokens are received
in a given iteration, no token is output on the next iteration.

@author Paul Whitaker
@version $Id$
*/

public class NonStrictDelay extends Transformer {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public NonStrictDelay(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        new Attribute(this, "_nonStrictMarker");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If there is a token on the input port, consume exactly one token
     *  from the input port, and store it for output on the next iteration.
     *  If a token was received on the previous iteration, send it to the
     *  output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {

        if (input.isKnown(0)) {
            if (input.hasToken(0)) {
                _currentToken = input.get(0);
            } else {
                _currentToken = AbsentToken.ABSENT;
            }
        }
        
        if (_previousToken != null) {
            if (_previousToken == AbsentToken.ABSENT) {
                output.sendAbsent(0);
            } else {
                output.send(0, _previousToken);
            }
        }
    }

    /** Initialize the buffer variables.
     *  @exception IllegalActionException If there is no director.
     */
    public void initialize() throws IllegalActionException {
        _previousToken = null;
        _currentToken = null;
        super.initialize();
    }

    /** Update the buffer variables to allow the inputs received to be 
     *  sent as outputs.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean postfire() throws IllegalActionException {
        _previousToken = _currentToken;
        _currentToken = null;

        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The token received on the previous iteration to be output on the
    // current iteration.
    private Token _previousToken;

    // The most recent token received on the current iteration to be 
    // output on the next iteration.
    private Token _currentToken;

}


