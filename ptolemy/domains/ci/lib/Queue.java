/* An actor that delays the input for a certain amount of real time.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Yellow (cxh@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.ci.lib;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.LongToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// Sleep
/**
An actor that calls Thread.sleep() on the current thread the first
time fire() is called.  The sleep delays the inputs for a certain
amount of real time, specified by the <i>sleepTime</i> parameter.

<p>Note that one way to slow down the execution of a model while running
inside vergil is to turn on animation.

<p>If the width of the output port is less than that of the input port,
the tokens in the extra channels are lost.

@author Yang Zhao, based on Queue by Jie Liu, Christopher Hylands
@version $Id$
@since Ptolemy II 2.2
*/
public class Queue extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Queue(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input.setMultiport(false);
        output.setMultiport(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Call Thread.sleep() the first time fire is called and then
     *  transfer tokens from inputs to outputs, one token from each
     *  channel.  If fire() is called twice in a row without an
     *  intervening call to either postfire() or prefire(), then no
     *  output is produced.
     *  <p>If the width of the output port is less than
     *  that of the input port, the tokens in the extra channels
     *  are lost.
     *  @exception IllegalActionException Not thrown in this base class */
    public void fire() throws IllegalActionException {
        if (input.hasToken(0)) {
            _queue.add(input.get(0));
        } else {
            output.broadcast((Token)_queue.removeFirst());
        }
    }

    /** Reset the flag that fire() checks so that fire() only sleeps once.
     *  @exception IllegalActionException If the parent class throws it.
     *  @return Whatever the superclass returns (probably true).
     */
    public boolean prefire() throws IllegalActionException {
        super.prefire();
        return (input.hasToken(0) || _queue.size() > 0);
    }

    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _queue = new LinkedList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // True if sleep was called in fire().  Sleep should only
    // be called once in fire().
    private LinkedList _queue = null;
}
