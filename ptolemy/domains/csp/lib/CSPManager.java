/* CSPManager

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
import ptolemy.data.IntToken;
import ptolemy.data.BooleanToken;
import java.util.Random;
import java.util.Enumeration;
import collections.LinkedList;


//////////////////////////////////////////////////////////////////////////
//// CSPManager
/**
   FIXME: add description!!

@author John S. Davis II
@version $Id$

*/

public class CSPManager extends CSPActor {

    /**
     */
    public CSPManager(CompositeActor cont, String name) 
            throws IllegalActionException, NameDuplicationException {
         super(cont, name);
         
         requestOut = new IOPort(this, "requestOut", false, true);
         requestIn = new IOPort(this, "requestIn", true, false);
         contendOut = new IOPort(this, "contendOut", false, true);
         contendIn = new IOPort(this, "contendIn", true, false);
         
    }
         
    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /**
     */
    public void fire() throws IllegalActionException {
        
        int br; 
        int code;
        BooleanToken posAck = new BooleanToken( true ); 
        BooleanToken negAck = new BooleanToken( false );
        
        while(true) {
        
            //
            // State 1: Wait for 1st Request
            //
            ConditionalBranch[] reqBrchs = 
                    new ConditionalBranch[_numRequestInChannels];
            for( int i=0; i<_numRequestInChannels; i++ ) {
                reqBrchs[i] = new 
                        ConditionalReceive(true, requestIn, i, i);
            } 
            
            br = chooseBranch(reqBrchs);
            
            if( br != -1 ) {
                IntToken token = (IntToken)requestIn.get(br);
                code = token.intValue(); 
                _winningPortChannelCode = 
                        new PortChannelCode(requestIn, br, code);
            }
            
            
            //
            // State 2: Notify Contention Alarm of 1st Request
            //
            contendOut.send(0, null);
            
            
            //
            // State 3: Wait for Contenders and Send Ack's
            //
            _losingPortChannelCodes = new LinkedList(); 
            boolean continueCDO = true;
            while( continueCDO ) {
                reqBrchs = new ConditionalBranch[_numRequestInChannels+1];
                for( int i=0; i<_numRequestInChannels; i++ ) {
                    reqBrchs[i] = new ConditionalReceive(true, requestIn, i, i);
                } 
                int j = _numRequestInChannels;
                reqBrchs[j] = new ConditionalReceive(true, contendIn, 0, j);
                
                br = chooseBranch(reqBrchs);
                
                
                // Contention Occurred...and might happen again
                if( br >= 0 || br < _numRequestInChannels ) {
                    IntToken token = (IntToken)requestIn.get(br);
                    code = token.intValue(); 
                    if( code > _winningPortChannelCode.getCode() ) {
                        _losingPortChannelCodes.
                                insertFirst(_winningPortChannelCode);
                        _winningPortChannelCode = 
                                new PortChannelCode(requestIn, br, code);
                    } else {
                        _losingPortChannelCodes.insertFirst( new 
                            PortChannelCode(requestIn, br, code) );
                    }
                    
                }
                
                // Contention is Over
                else if( br == _numRequestInChannels ) {
                
                    // Send Positive Ack
                    int ch =  _winningPortChannelCode.getChannel();
                    requestOut.send(ch, posAck);
                    
                    // Send Negative Ack
                    Enumeration enum = _losingPortChannelCodes.elements();
                    PortChannelCode pcc = null;
                    while( enum.hasMoreElements() ) {
                        pcc = (PortChannelCode)enum.nextElement();
                        ch = pcc.getChannel();
                        requestOut.send(ch, negAck);
                    }
                    
                    // Prepare to Wait for New Requests...enter state 1
                    continueCDO = false;
                    _winningPortChannelCode = null;
                    _losingPortChannelCodes = null;
                    
                }
                    
                
                // All branches failed.
                else {
                    continueCDO = false;
                    return;
                }
            }
        }
    }
    
    
    

    public boolean postfire() {
        return false;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public variables                       ////

    public IOPort requestIn;
    public IOPort requestOut;
    public IOPort contendIn;
    public IOPort contendOut;
    
    private int _numRequestInChannels;
    
    private PortChannelCode _winningPortChannelCode;
    private LinkedList _losingPortChannelCodes;
}
