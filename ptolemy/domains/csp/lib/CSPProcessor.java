/* CSPProcessor

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

import ptolemy.actor.*;
import ptolemy.domains.csp.kernel.*;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.BooleanToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import java.util.Random;


//////////////////////////////////////////////////////////////////////////
//// CSPProcessor
/**

@author John S. Davis II
@version $Id$

*/

public class CSPProcessor extends CSPActor {

    /**
     */
    public CSPProcessor(CompositeActor cont, String name) 
            throws IllegalActionException, NameDuplicationException {
         super(cont, name);
         
         _requestOut = new IOPort(this, "requestOut", false, true);
         _requestIn = new IOPort(this, "requestIn", true, false);
         _memoryOut = new IOPort(this, "memoryOut", false, true);
         _memoryIn = new IOPort(this, "memoryIn", true, false);
         
    }
         
    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /**
     */
    public void accessMemory(boolean read) throws IllegalActionException {
        
        // State 1 
        Random rand = new Random(); 
        double delayTime = rand.nextDouble() * 10 ; 
        delay( delayTime ); 
        
        // State 2 
        IntToken iToken = new IntToken( _code ); 
        _requestOut.send(0, iToken); 
        
        // State 3 
        BooleanToken bToken = (BooleanToken)_requestIn.get(0); 
        if( bToken.booleanValue() ) {
            if( read ) {
                _memoryIn.get(0);
            }
            else {
                StringToken strToken = new StringToken( getName() );
                _memoryOut.send(0, strToken);
            }
            return;
        }
        
        // State 4
        accessMemory(read);
    }

    /**
     */
    public void fire() throws IllegalActionException {
        while(true) {
            if( performReadNext() ) {
                accessMemory(true);
            } else {
                accessMemory(false);
            }
        }
    }
        
    /**
     */
    public boolean performReadNext() {
        Random rand = new Random();
        if( rand.nextDouble() < 0.5 ) {
            return true;
        }
        return false;
    }
    
    ////////////////////////////////////////////////////////////////////////
    ////                        private methods                         ////

    private IOPort _requestIn;
    private IOPort _requestOut;
    private IOPort _memoryIn;
    private IOPort _memoryOut;
    
    private int _code;
}
