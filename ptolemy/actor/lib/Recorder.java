/* Record all input tokens for later querying.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.StringToken;

// FIXME: Test suites can't use jdk 1.2 yet, so we have to use the
// old collections package.
// import java.util.LinkedList;
// import java.util.ListIterator;
import collections.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// Recorder
/**
Record all input tokens for later querying.  This actor can be used for
testing configurations of actors.  It can also be used in programs that
invoke Ptolemy models and wish to query the results after the model
is run.  The input tokens are read in the postfire() method so that
in domains with fixed-point semantics, only the final, settled value
is recorded.

@author Edward A. Lee
@version $Id$
*/

// FIXME: This class does not support mutations.

public class Recorder extends Sink {

    /** Construct an actor with an input multiport that can accept any
     *  StringToken.  Since almost all tokens can be converted to StringToken,
     *  the input type implies minimal constraints on the use of this actor.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Recorder(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input.setTypeEquals(StringToken.class);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the <code>input</code>
     *  variable to equal the new port.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        Recorder newobj = (Recorder)super.clone(ws);
        newobj.input.setTypeEquals(StringToken.class);
        return newobj;
    }

    /** Get the record for the specified channel.
     *  @exception NoSuchItemException If the channel is out of range.
     */

     // FIXME: This should return ListIterator when tests can use jdk 1.2.
    public Enumeration getRecord(int channel) throws NoSuchItemException {
        if (records == null || channel < 0 || channel >= records.length) {
            throw new NoSuchItemException(this,
                   "No such channel: " + channel);
        }
        // FIXME:
        // return records[channel].listIterator(0);
        return records[channel].elements();
    }

    /** Initialize the lists used to record input data.
     */
    public void initialize() {
        int width = input.getWidth();
        records = new LinkedList[width];
        for (int i = 0; i < width; i++) {
            records[i] = new LinkedList();
        }
    }

    /** Read at most one token from each input channel and record its value.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean postfire() throws IllegalActionException {
        int width = input.getWidth();
        for (int i = 0; i < width; i++) {
            if (input.hasToken(i)) {
                StringToken token = (StringToken)input.get(i);
                // FIXME
                // records[i].add(token);
                records[i].insertLast(token);
            }
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Array of linked lists.
    private LinkedList[] records;
}
