/* CSPMemory

 Copyright (c) 1998 The Regents of the University of California.
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

@ProposedRating Red (nsmyth@eecs.berkeley.edu)

*/

package ptolemy.domains.csp.lib;

import ptolemy.domains.csp.kernel.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.Token;
import ptolemy.data.StringToken;


//////////////////////////////////////////////////////////////////////////
//// CSPMemory
/**

@author John S. Davis II
@version $Id$

*/

public class CSPMemory extends CSPActor {

    /**
     */
    public CSPMemory(CompositeActor cont, String name) 
            throws IllegalActionException, NameDuplicationException {
         super(cont, name);
         
         _input = new IOPort(this, "input", true, false);
         _output = new IOPort(this, "output", false, true);
         
         _input.setMultiport(true);
         _output.setMultiport(true);
         
         _strValue = "initialValue";
    }
         
    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /**
     */
    public void fire() throws IllegalActionException {
        
        boolean beginning = true; 
        StringToken token;
        
        while(true) {
            token = new StringToken( _strValue );
            
            ConditionalBranch[] branches = new ConditionalBranch[2];
            
            // Receive Branch
            branches[0] = new ConditionalReceive(true, _input, 0, 0);
            
            // Send Branch
            branches[1] = new ConditionalSend(true, _output, 0, 1, token);
            
            int br = chooseBranch( branches );
            
            if( br == 0 ) {
                token = (StringToken)branches[0].getToken();
                _strValue = token.stringValue();
            } else if( br == -1 ) {
                return;
            } 
        }
    }
    
    public synchronized String getString() {
        return _strValue;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public variables                       ////

    private IOPort _input;
    private IOPort _output;
    
    private String _strValue = null;
}
