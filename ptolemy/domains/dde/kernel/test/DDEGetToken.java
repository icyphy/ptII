/* DDEGetToken

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
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.Token;
import ptolemy.domains.dde.kernel.DDEReceiver;
import ptolemy.domains.dde.kernel.DDEThread;
import ptolemy.domains.dde.kernel.TimeKeeper;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// DDEGetToken

/**

 @author John S. Davis II
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Red (davisj)
 @Pt.AcceptedRating Red (cxh)
 */
public class DDEGetToken extends DDEGet {
    /**
     */
    public DDEGetToken(TypedCompositeActor cont, String name, int numTokens)
            throws IllegalActionException, NameDuplicationException {
        super(cont, name);

        _numTokens = numTokens;
        _tokens = new Token[_numTokens];
        _threadTimes = new double[_numTokens];
        _receiverTimes = new double[_numTokens];
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public double getReceiverTime(int cntr) {
        return _receiverTimes[cntr];
    }

    /**
     */
    public double getThreadTime(int cntr) {
        return _threadTimes[cntr];
    }

    /**
     */
    public Token getToken(int cntr) {
        return _tokens[cntr];
    }

    /**
     */
    @Override
    public void fire() throws IllegalActionException {
        //        int aCntr = 0;
        //        Receiver[][] theReceivers = input.getReceivers();
        //
        //        for (int i = 0; i < theReceivers.length; i++) {
        //            for (int j = 0; j < theReceivers[i].length; j++) {
        //                aCntr++;
        //            }
        //        }

        int cnt = 0;

        while (cnt < _numTokens) {
            Receiver[][] receivers = input.getReceivers();

            for (Receiver[] receiver2 : receivers) {
                for (int j = 0; j < receiver2.length; j++) {
                    DDEReceiver receiver = (DDEReceiver) receiver2[j];

                    if (receiver.hasToken()) {
                        _receiverTimes[cnt] = receiver.getReceiverTime()
                                .getDoubleValue();
                        _tokens[cnt] = receiver.get();

                        Thread thread = Thread.currentThread();

                        if (thread instanceof DDEThread) {
                            TimeKeeper timeKeeper = ((DDEThread) thread)
                                    .getTimeKeeper();
                            _threadTimes[cnt] = timeKeeper.getModelTime()
                                    .getDoubleValue();
                        }
                    }
                }
            }

            cnt++;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private int _numTokens;

    private Token[] _tokens = null;

    private double[] _threadTimes = null;

    private double[] _receiverTimes = null;
}
