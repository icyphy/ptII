/* CSPPut

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

package ptolemy.domains.csp.kernel.test;

import ptolemy.domains.csp.kernel.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.Token;
import ptolemy.data.StringToken;


//////////////////////////////////////////////////////////////////////////
//// CSPPut
/**

@author John S. Davis II
@version $Id$

*/

public class CSPPut extends CSPActor {

    /**
     */
    public CSPPut(CompositeActor cont, String name, int count)
            throws IllegalActionException, NameDuplicationException {
         super(cont, name);

         outputPort = new IOPort(this, "output", false, true);
	 _count = count;
	 _tokens = new Token[_count];
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /**
     */
    public void fire() throws IllegalActionException {
	int cnt = 0; 
	Token token = new Token();
	while(cnt < _count) {
	    outputPort.send(0, _tokens[cnt]);
	    cnt++;
	}
    }

    /**
     */
    public void setToken(Token token, int cntr) {
	_tokens[cntr] = token;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                        private variables                       ////

    public IOPort outputPort;
    private int _count;
    private int _pauseCnt = -1;
    private Token[] _tokens = null;
}
