/* ODFGetToken

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

@ProposedRating Red (davisj@eecs.berkeley.edu)

*/

package ptolemy.domains.odf.kernel.test;

import ptolemy.domains.odf.kernel.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.Token;
import ptolemy.data.StringToken;


//////////////////////////////////////////////////////////////////////////
//// ODFGetToken
/**

@author John S. Davis II
@version $Id$

*/

public class ODFGetToken extends ODFGet {

    /**
     */
    public ODFGetToken(CompositeActor cont, String name, int numTokens)
            throws IllegalActionException, NameDuplicationException {
         super(cont, name);

	 _numTokens = numTokens;
	 _tokens = new Token[_numTokens];
	 _threadTimes = new double[_numTokens];
	 _rcvrTimes = new double[_numTokens];
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /**
     */
    public double getRcvrTime(int cntr) {
	return _rcvrTimes[cntr];
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
    public void fire() throws IllegalActionException {
	int aCntr = 0;
	Receiver[][] theRcvrs = inputPort.getReceivers();
	for( int i = 0; i < theRcvrs.length; i++ ) {
	    for( int j = 0; j < theRcvrs[i].length; j++ ) {
		aCntr++;
	    }
	}
	System.out.println("There are "+aCntr+" receivers in "+getName());
	int cnt = 0; 
	while(cnt < _numTokens) {
	    Receiver[][] rcvrs = inputPort.getReceivers();
	    for( int i = 0; i < rcvrs.length; i++ ) {
		for( int j = 0; j < rcvrs[i].length; j++ ) {
		    ODFReceiver rcvr = (ODFReceiver)rcvrs[i][j]; 
		    System.out.println("ODFGetToken receiver["+i+"]["+j+"]; cnt = "+cnt);
		    if( rcvr.hasToken() ) {
			_rcvrTimes[cnt] = rcvr.getRcvrTime();
	                _tokens[cnt] = rcvr.get();
	                TimeKeeper timeKeeper = rcvr.getReceivingTimeKeeper();
			_threadTimes[cnt] = timeKeeper.getCurrentTime();
		    } else {
			System.out.println("No token for cnt "+cnt);
		    }
		}
	    }
	    cnt++;
	}
	System.out.println("ODFGetToken() ends with cnt = "+cnt);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                        private variables                       ////

    private int _numTokens;
    private Token[] _tokens = null;
    private double[] _threadTimes = null;
    private double[] _rcvrTimes = null;
    
}
