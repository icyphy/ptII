/* DDEPutToken is a test class used for testing the production of tokens.

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
import ptolemy.actor.util.Time;
import ptolemy.data.Token;
import ptolemy.domains.dde.kernel.DDEReceiver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// DDEPutToken

/**
 DDEPutToken is a test class used for testing the production of tokens.
 DDEPutToken has a single, typed, output multiport. A DDEPutToken object
 can output N tokens where 'N' is specified in the constructor. The current
 time of DDEPutToken at the time a given token is output can be queried
 after Manager.run() is finished.


 @author John S. Davis II
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Red (davisj)
 @Pt.AcceptedRating Red (cxh)

 */
public class DDEPutToken extends DDEPut {
    /**
     */
    public DDEPutToken(TypedCompositeActor cont, String name, int numTokens)
            throws IllegalActionException, NameDuplicationException {
        super(cont, name);

        _numTokens = numTokens;
        _tokens = new Token[_numTokens];
        _times = new double[_numTokens];
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    @Override
    public void fire() throws IllegalActionException {
        int cnt = 0;

        while (cnt < _numTokens) {
            Receiver[][] rcvrs = outputPort.getRemoteReceivers();

            for (Receiver[] rcvr2 : rcvrs) {
                for (int j = 0; j < rcvr2.length; j++) {
                    DDEReceiver rcvr = (DDEReceiver) rcvr2[j];

                    if (_oneArg) {
                        rcvr.put(_tokens[cnt]);
                    } else {
                        rcvr.put(_tokens[cnt], new Time(getDirector(),
                                _times[cnt]));
                    }
                }
            }

            cnt++;
        }
    }

    /**
     */
    @Override
    public boolean postfire() {
        return false;
    }

    /**
     */
    public void setOneArgPut(boolean oneArg) {
        _oneArg = oneArg;
    }

    /**
     */
    public void setToken(Token token, double time, int cntr) {
        _tokens[cntr] = token;
        _times[cntr] = time;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private int _numTokens;

    private Token[] _tokens = null;

    private double[] _times = null;

    private boolean _oneArg = false;
}
