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

package ptolemy.domains.ci.demo.Router;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.LongToken;
import ptolemy.data.IntToken;
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

<p>Note that one way to slow dow the execution of a model while running
inside vergil is to turn on animation.

<p>If the width of the output port is less than that of the input port,
the tokens in the extra channels are lost.

@author Jie Liu, Christopher Hylands
@version $Id$
@since Ptolemy II 1.0
*/
public class QueueControl extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public QueueControl(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        q1_len = new TypedIOPort(this, "q1_len", true, false);
        q1_len.setTypeEquals(BaseType.INT);

        pkt_in = new TypedIOPort(this, "pkt_in", true, false);
        pkt_in.setTypeEquals(BaseType.GENERAL);

        q2_len = new TypedIOPort(this, "q2_len", true, false);
        q2_len.setTypeEquals(BaseType.INT);

        q1_out = new TypedIOPort(this, "q1_out", false, true);
        q1_out.setTypeEquals(BaseType.GENERAL);

        dropped = new TypedIOPort(this, "dropped", false, true);
        dropped.setTypeEquals(BaseType.GENERAL);

        q2_out = new TypedIOPort(this, "q2_out", false, true);
        q2_out.setTypeEquals(BaseType.GENERAL);

        minMark = new Parameter(this, "minMark");
        minMark.setTypeEquals(BaseType.INT);
        minMark.setExpression("10");

        maxMark = new Parameter(this, "maxMark");
        maxMark.setTypeEquals(BaseType.INT);
        maxMark.setExpression("20");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    public TypedIOPort q1_len, pkt_in, q2_len, q1_out, dropped, q2_out;
    public Parameter minMark, maxMark;

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
        if (q1_len.hasToken(0)) {
            _q1len = ((IntToken)q1_len.get(0)).intValue();
        }
        if (q2_len.hasToken(0)) {
            _q2len = ((IntToken)q2_len.get(0)).intValue();
        }
        if (pkt_in.hasToken(0)) {
            Token pkt = pkt_in.get(0);
            double r = Math.random();
            int min = ((IntToken)minMark.getToken()).intValue();
            int max = ((IntToken)maxMark.getToken()).intValue();
            double l = 0.0;
            if (_q1len + _q2len > min) {
                l = (0.0 + _q1len + _q2len - min)/(max - min);
            }
            if (l > r) {
                dropped.broadcast(pkt);
            } else {
                if (_q1len > _q2len) {
                    q2_out.broadcast(pkt);
                } else {
                    q1_out.broadcast(pkt);
                }
            }
        }
    }

    /** Reset the flag that fire() checks so that fire() only sleeps once.
     *  @exception IllegalActionException If the parent class throws it.
     *  @return Whatever the superclass returns (probably true).
     */
    public boolean prefire() throws IllegalActionException {
        return super.prefire();
    }

    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _q1len = 0;
        _q2len = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // True if sleep was called in fire().  Sleep should only
    // be called once in fire().
    private int _q1len;
    private int _q2len;
}
