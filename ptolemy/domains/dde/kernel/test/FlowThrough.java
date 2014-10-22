/* FlowThrough is a test class used to test token production
 AND consumption.

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
package ptolemy.domains.dde.kernel.test;

import ptolemy.actor.Receiver;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.domains.dde.kernel.DDEReceiver;
import ptolemy.domains.dde.kernel.DDEThread;
import ptolemy.domains.dde.kernel.TimeKeeper;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// FlowThrough

/**
 FlowThrough is a test class used to test token production AND consumption.
 It has a typed, input and output multiport. The fire() method of this
 class simply passes through "real" tokens. Use this class to test
 DDEReceiver and DDEThread.


 @author John S. Davis II
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Red (davisj)
 @Pt.AcceptedRating Red (cxh)

 */
public class FlowThrough extends TypedAtomicActor {
    /**
     * @param name The name of the actor.
     */
    public FlowThrough(TypedCompositeActor cont, String name)
            throws IllegalActionException, NameDuplicationException {
        super(cont, name);

        output = new TypedIOPort(this, "output", false, true);
        output.setMultiport(true);
        output.setTypeEquals(BaseType.GENERAL);
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.GENERAL);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _inRcvrs = input.getReceivers();
    }

    /**
     */
    @Override
    public void fire() throws IllegalActionException {
        Token token = null;

        if (_inRcvrs.length == 0) {
            _continueIterations = false;
        }

        for (Receiver[] _inRcvr : _inRcvrs) {
            for (int j = 0; j < _inRcvr.length; j++) {
                DDEReceiver inRcvr = (DDEReceiver) _inRcvr[j];

                if (inRcvr.hasToken()) {
                    token = inRcvr.get();

                    Receiver[][] outRcvrs = output.getRemoteReceivers();

                    for (Receiver[] outRcvr2 : outRcvrs) {
                        for (int l = 0; l < outRcvr2.length; l++) {
                            DDEReceiver outRcvr = (DDEReceiver) outRcvr2[l];
                            Thread thr = Thread.currentThread();

                            if (thr instanceof DDEThread) {
                                TimeKeeper kpr = ((DDEThread) thr)
                                        .getTimeKeeper();
                                outRcvr.put(token, kpr.getModelTime());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        return _continueIterations;
    }

    /**
     */
    public void setOutChan(int ch) throws IllegalActionException {
        //_outChannel = ch;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    public TypedIOPort output;

    public TypedIOPort input;

    //private int _outChannel = -1;

    private boolean _continueIterations = true;

    private Receiver[][] _inRcvrs;
}
