/* An actor that delays the input for a certain amount of real time.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.domains.ci.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// BinarySwitch

/**
 An actor that calls Thread.sleep() on the current thread the first
 time fire() is called.  The sleep delays the inputs for a certain
 amount of real time, specified by the <i>sleepTime</i> parameter.

 <p>Note that one way to slow down the execution of a model while running
 inside vergil is to turn on animation.

 <p>If the width of the output port is less than that of the input port,
 the tokens in the extra channels are lost.

 @author Yang Zhao, based on Sleep by Jie Liu, Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.2
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class BinarySwitch extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public BinarySwitch(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        q1_len = new TypedIOPort(this, "q1_len", true, false);
        q1_len.setTypeEquals(BaseType.INT);
        q2_len = new TypedIOPort(this, "q2_len", true, false);
        q2_len.setTypeEquals(BaseType.INT);
        pkt_in = new TypedIOPort(this, "pkt_in", true, false);

        //pkt_in.setTypeEquals(BaseType.UNKNOWN);
        q1_out = new TypedIOPort(this, "q1_out", false, true);
        q1_out.setTypeEquals(pkt_in.getType());
        q2_out = new TypedIOPort(this, "q2_out", false, true);
        q2_out.setTypeEquals(pkt_in.getType());
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    // FIXME: What is the description of these ports
    // FIXME: ptk_in should be packet or packetIn
    // FIXME: q1_len should be q1Length, ditto with q2_len
    // FIXME: None of these should have underscores in them
    public TypedIOPort q1_len;

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    // FIXME: What is the description of these ports
    // FIXME: ptk_in should be packet or packetIn
    // FIXME: q1_len should be q1Length, ditto with q2_len
    // FIXME: None of these should have underscores in them
    public TypedIOPort q2_len;

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    // FIXME: What is the description of these ports
    // FIXME: ptk_in should be packet or packetIn
    // FIXME: q1_len should be q1Length, ditto with q2_len
    // FIXME: None of these should have underscores in them
    public TypedIOPort pkt_in;

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    // FIXME: What is the description of these ports
    // FIXME: ptk_in should be packet or packetIn
    // FIXME: q1_len should be q1Length, ditto with q2_len
    // FIXME: None of these should have underscores in them
    public TypedIOPort q1_out;

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    // FIXME: What is the description of these ports
    // FIXME: ptk_in should be packet or packetIn
    // FIXME: q1_len should be q1Length, ditto with q2_len
    // FIXME: None of these should have underscores in them
    public TypedIOPort q2_out;

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
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (q1_len.hasToken(0)) {
            _q1len = ((IntToken) q1_len.get(0)).intValue();
        }

        if (q2_len.hasToken(0)) {
            _q2len = ((IntToken) q2_len.get(0)).intValue();
        }

        if (pkt_in.hasToken(0)) {
            Token pkt = pkt_in.get(0);

            if (_q1len > _q2len) {
                q2_out.broadcast(pkt);
            } else {
                q1_out.broadcast(pkt);
            }
        }
    }

    /** Reset the flag that fire() checks so that fire() only sleeps once.
     *  @exception IllegalActionException If the parent class throws it.
     *  @return Whatever the superclass returns (probably true).
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        return super.prefire();
    }

    @Override
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
