/* DDEGetNToken is a test class used to test the consumption of tokens
and check relevant parameters.

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

package ptolemy.domains.dde.kernel.test;

import ptolemy.actor.*;
import ptolemy.domains.dde.kernel.*;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.Token;
import ptolemy.data.StringToken;


//////////////////////////////////////////////////////////////////////////
//// DDEGetNToken
/**
DDEGetNToken is a test class used to test the consumption of tokens
and check relevant parameters. DDEGetNToken can retrieve N tokens
where 'N' is set in the constructor. For each token retrieved, the
current time at the time of consumption and the actual consumed
token can be queried. The queries can take place after the completion
of Manager.run().


@author John S. Davis II
@version $Id$

*/

public class DDEGetNToken extends DDEGet {

    /**
     */
    public DDEGetNToken(TypedCompositeActor cont, String name, int numTokens)
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
	int cnt = 0; 
	Token token;
	while(cnt < _numTokens) {
            Thread thread = Thread.currentThread(); 
	    if( thread instanceof DDEThread ) {
		TimeKeeper timeKeeper = ((DDEThread)thread).getTimeKeeper();
	        _tokens[cnt] = getNextToken(); 
		_threadTimes[cnt] = timeKeeper.getCurrentTime(); 
		cnt++;
	    }
	}
    }

    ////////////////////////////////////////////////////////////////////////
    ////                        private variables                       ////

    private int _numTokens;
    private Token[] _tokens = null;
    private double[] _threadTimes = null;
    private double[] _rcvrTimes = null;
    
}
