/* An actor to put tokens in order.

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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

// FIXME: trim this.
import ptolemy.actor.*;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.*;
import ptolemy.data.type.*;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

import java.util.TreeMap;


//////////////////////////////////////////////////////////////////////////
//// Multiplexor
/**
This actor takes a sequence of inputs tagged with a sequence number
and produces them on the output port in the order given by the
sequence number.  The sequence numbers are integers starting
with zero.  On each firing, this actor consumes one token
from the <i>input</i> port and one token from the
<i>sequenceNumber</i> port. If the sequence number is the
next one in the sequence, then the token read from the <i>input</i>
port is produced on the <i>output</i> port.  Otherwise,
it is saved until its sequence number is the next one
in the sequence.  If an output is produced, then it may
be immediately followed by tokens that were previously
saved, if their sequence numbers are next.

@author Edward A. Lee
@version $Id$
*/

public class Sequencer extends Transformer {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Sequencer(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        sequenceNumber = new TypedIOPort(this, "sequenceNumber", true, false);
        sequenceNumber.setTypeEquals(BaseType.INT);

        output.setTypeSameAs(input);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input for the sequence number. The type is IntToken. */
    public TypedIOPort sequenceNumber;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read a token from the <i>sequenceNumber</i> port and from
     *  the <i>input</i> port, and output the next token(s) in the
     *  sequence, or none if the next token in the sequence has not
     *  yet been seen.  This method will throw a NoTokenException if
     *  <i>sequenceNumber</i> or <i>input</i> does not have a token.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        int number = ((IntToken) sequenceNumber.get(0)).intValue();
        Token token = input.get(0);
        if (number == _nextSequenceNumber) {
            output.send(0, token);
            _fireProducedOutput = true;
        } else {
            _pending.put(new Integer(number), token);
        }
    }

    /** Reset internal flags.
     */
    public void initialize() {
        _fireProducedOutput = false;
        _nextSequenceNumber = 0;
    }

    /** Return false if either the <i>input</i> port or the
     *  <i>sequenceNumber</i> port lacks an input token.
     *  Otherwise, return whatever the superclass returns.
     *  @return False if there are not enough tokens to fire.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean prefire() throws IllegalActionException {
        if (!sequenceNumber.hasToken(0)) return false;
        if (!input.hasToken(0)) return false;
        return super.prefire();
    }

    /** If the fire() method produced the input token then check to
     *  whether any pending tokens have subsequent sequence numbers.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean postfire() throws IllegalActionException {
        if (_fireProducedOutput) {
            _nextSequenceNumber++;
            if (_pending.size() > 0) {
                Integer nextKey = (Integer)_pending.firstKey();
                int next = nextKey.intValue();
                while (next == _nextSequenceNumber) {
                    _nextSequenceNumber++;
                    Token token = (Token)_pending.remove(nextKey);
                    output.send(0, token);
                    if (_pending.size() == 0) break;
                    nextKey = (Integer)_pending.firstKey();
                    next = nextKey.intValue();
                }
            }
            _fireProducedOutput = false;
        }
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Indicator that an output was produced by the fire() method.
    private boolean _fireProducedOutput = false;

    // Indicator of the next sequence number for the output.
    private int _nextSequenceNumber = 0;

    // The next sequence number.
    private int _next = 0;

    // The sorted pending data.
    private TreeMap _pending = new TreeMap();
}

