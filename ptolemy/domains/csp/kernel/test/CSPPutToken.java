/* CSPPutToken

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
package ptolemy.domains.csp.kernel.test;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// CSPPutToken

/**

 @author John S. Davis II
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Red (davisj)
 @Pt.AcceptedRating Red (cxh)

 */
public class CSPPutToken extends CSPPut {
    /**
     */
    public CSPPutToken(TypedCompositeActor cont, String name, int numTokens)
            throws IllegalActionException, NameDuplicationException {
        super(cont, name);

        _numTokens = numTokens;
        _tokens = new Token[_numTokens];
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    @Override
    public void fire() throws IllegalActionException {
        int cnt = 0;

        while (cnt < _numTokens) {
            outputPort.send(0, _tokens[cnt]);
            cnt++;
        }
    }

    /**
     */
    public void setToken(Token token, int cntr) {
        _tokens[cntr] = token;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private int _numTokens;

    private Token[] _tokens = null;
}
