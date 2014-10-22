/* An actor that distribute its input data to different output ports.

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
package ptolemy.domains.ci.demo.Router;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// QueueControl

/**
 An actor that distribute its input data to different outputs.
 Its output ports <i>queue1<i> and <i>queue2<i> is connected
 to two <i>queue<i> actors respectively. The lengh of the two
 queue are fed back to its input ports <i>q1Length<i> and
 <i>q2Length<i>. The input token at <i>input<i> is distributed
 according to the following policy: if the total length of queue1
 and queue2 is less than threshold1, specified by the <i>minMark</i>
 parameter, the input token is send to <i>queue1<i> if queue1's
 length is less than queue2's, otherwise send to <i>queue2<i>; if
 the total length is greater than threshold1 but less than
 threshold2, specified by the <i>minMark</i> parameter, the
 input token may be dropped randomly(with a probability
 proportional to the amount larger than threshold1) or
 send to queues shorter; if the total length is greater than
 threshold2, then drop the input token. If the input token is
 dropped, it is send to the <i>dropped<i> output so that it can
 be catched or monitored when necessary.

 @author Xiaojun Liu
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Yellow (cxh)
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
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        q1Length = new TypedIOPort(this, "q1Length", true, false);
        q1Length.setTypeEquals(BaseType.INT);

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.GENERAL);

        q2Length = new TypedIOPort(this, "q2Length", true, false);
        q2Length.setTypeEquals(BaseType.INT);

        queue1 = new TypedIOPort(this, "queue1", false, true);
        queue1.setTypeEquals(BaseType.GENERAL);

        dropped = new TypedIOPort(this, "dropped", false, true);
        dropped.setTypeEquals(BaseType.GENERAL);

        queue2 = new TypedIOPort(this, "queue2", false, true);
        queue2.setTypeEquals(BaseType.GENERAL);

        minMark = new Parameter(this, "minMark");
        minMark.setTypeEquals(BaseType.INT);
        minMark.setExpression("10");

        maxMark = new Parameter(this, "maxMark");
        maxMark.setTypeEquals(BaseType.INT);
        maxMark.setExpression("20");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    public TypedIOPort q1Length;

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    public TypedIOPort q2Length;

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    public TypedIOPort queue1;

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    public TypedIOPort dropped;

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    public TypedIOPort queue2;

    //the thresholds used for control queue1 and queue2's length.
    public Parameter minMark;

    //the thresholds used for control queue1 and queue2's length.
    public Parameter maxMark;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** update _q1Length if <i>q1Length<i> has token,
     *  or update _q2Length if <i>q2Length<i> has token.
     *  No output it produced if <i>input<i> doesn't has
     *  token, otherwise, distribute the input data to
     *  corresponding output.
     *  @exception IllegalActionException Not thrown in this base class */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (q1Length.hasToken(0)) {
            _q1Length = ((IntToken) q1Length.get(0)).intValue();
        }

        if (q2Length.hasToken(0)) {
            _q2Length = ((IntToken) q2Length.get(0)).intValue();
        }

        if (input.hasToken(0)) {
            Token pkt = input.get(0);
            double r = Math.random();
            int min = ((IntToken) minMark.getToken()).intValue();
            int max = ((IntToken) maxMark.getToken()).intValue();
            double l = 0.0;

            if (_q1Length + _q2Length > min) {
                l = (0.0 + _q1Length + _q2Length - min) / (max - min);
            }

            if (l > r) {
                dropped.broadcast(pkt);
            } else {
                if (_q1Length > _q2Length) {
                    queue2.broadcast(pkt);
                } else {
                    queue1.broadcast(pkt);
                }
            }
        }
    }

    /** Preinitialize the private variables _q1Length
     *  and _q2Length to zero.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _q1Length = 0;
        _q2Length = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    //length of the queue that <i>queue1<i> connected to.
    private int _q1Length;

    //length of the queue that <i>queue2<i> connected to.
    private int _q2Length;
}
