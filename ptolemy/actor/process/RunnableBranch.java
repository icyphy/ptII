/* An interface used by receivers that receive data across
composite actor boundaries. 

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

@ProposedRating Green (mudit@eecs.berkeley.edu)
@AcceptedRating Yellow

*/

package ptolemy.actor.process;

import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;


//////////////////////////////////////////////////////////////////////////
//// RunnableBranch
/**
A RunnableBranch is an interface used by receivers that receive 
data across composite actor boundaries. 

@author John S. Davis II
@version $Id$

*/
public class RunnableBranch extends Branch implements Runnable {

    /**
     */
    public RunnableBranch(boolean guard, IOPort prodPort, IOPort consPort,
    	    int channel, int branchID, BranchController cntlr)
            throws IllegalActionException {
        super(guard, branchID, cntlr);
        
        Receiver[][] receivers;
        BoundaryReceiver receiver;
        
        receivers = prodPort.getReceivers();
        receiver = (BoundaryReceiver)receivers[channel][0];
        if( !receiver.isProducerReceiver() ) {
            throw new IllegalActionException(prodPort, "Not producer "
            	    + "receiver");
        }
        _prodRcvr = (BoundaryReceiver)receivers[channel][0];
        
        receivers = consPort.getRemoteReceivers();
        receiver = (BoundaryReceiver)receivers[channel][0];
        if( !receiver.isConsumerReceiver() ) {
            throw new IllegalActionException(consPort, "Not consumer "
            	    + "receiver");
        }
        _consRcvr = (BoundaryReceiver)receivers[channel][0];
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** 
     */
    public void run() {
    	try {
            while( true ) {
                try {
            	    Token token = null;
            	    token = _prodRcvr.get(this);
            	    _consRcvr.put(token, this);
                } catch( TerminateBranchException e ) {
                    while( _branchStopRequest ) {
			synchronized(this) {
                            wait();
			}
                    }
                }
            }
        } catch( TerminateProcessException e ) {
        } catch( InterruptedException e ) {
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    private BoundaryReceiver _prodRcvr;
    private BoundaryReceiver _consRcvr;

}
