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

@ProposedRating Red (davisj@eecs.berkeley.edu)

*/

package ptolemy.domains.csp.lib;

import ptolemy.actor.*;
import ptolemy.domains.csp.kernel.*;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.BooleanToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


//////////////////////////////////////////////////////////////////////////
//// CSPProcessor
/**

@author John S. Davis II
@version $Id$

*/

public class CSPProcessor extends CSPActor {

    /**
     */
    public CSPProcessor(CompositeActor cont, String name, int code) 
            throws IllegalActionException, NameDuplicationException {
         super(cont, name);
         
         _requestOut = new IOPort(this, "requestOut", false, true);
         _requestIn = new IOPort(this, "requestIn", true, false);
         _memoryOut = new IOPort(this, "memoryOut", false, true);
         _memoryIn = new IOPort(this, "memoryIn", true, false);
         
         _code = code;
         
    }
         
    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /**
     */
    public void accessMemory(boolean read) throws IllegalActionException {
        
        // State 1 
        if( getName().equals("proc1") ) {
	    System.out.println("STATE 1: " +getName());
        } else if( getName().equals("proc2") ) {
	    System.out.println("\tSTATE 1: " +getName());
        } else {
	    System.out.println("\t\tSTATE 1: " +getName());
        }
        double delayTime = java.lang.Math.random();
        if( delayTime < 0.25 ) {
            delayTime = 2.5;
        } else if ( delayTime >= 0.25 && delayTime < 0.5 ) {
            delayTime = 5.0;
        } else if ( delayTime >= 0.5 && delayTime < 0.75 ) {
            delayTime = 7.5;
        } else {
            delayTime = 10.0;
        }
        System.out.println(getName()+" delaying for "+delayTime+" seconds.");
        delay( delayTime ); 
        IntToken iToken = new IntToken( _code ); 
        _requestOut.broadcast(iToken); 
        
        // State 2 
        if( getName().equals("proc1") ) {
	    System.out.println("STATE 2: " +getName());
        } else if( getName().equals("proc2") ) {
	    System.out.println("\tSTATE 2: " +getName());
        } else {
	    System.out.println("\t\tSTATE 2: " +getName());
        }
        BooleanToken bToken = (BooleanToken)_requestIn.get(0); 
        
        // State 3 
        if( getName().equals("proc1") ) {
	    System.out.println("STATE 3: " +getName());
        } else if( getName().equals("proc2") ) {
	    System.out.println("\tSTATE 3: " +getName());
        } else {
	    System.out.println("\t\tSTATE 3: " +getName());
        }
        if( bToken.booleanValue() ) {
            // State 4
            if( getName().equals("proc1") ) {
	        System.out.println("STATE 4: " +getName());
            } else if( getName().equals("proc2") ) {
                System.out.println("\tSTATE 4: " +getName());
            } else {
                System.out.println("\t\tSTATE 4: " +getName());
            }
            if( read ) {
                _memoryIn.get(0);
            }
            else {
                StringToken strToken = new StringToken( getName() );
                _memoryOut.broadcast(strToken);
            }
            return;
        }
        
        // System.out.println(getName()+ ": Negative Ack!!!");
        accessMemory(read);
    }

    /**
     */
    public boolean endYet() {
        double time = _dir.getCurrentTime();
        if( time > 50.0 ) {
            System.out.println(getName() + " is ending because of time.");
            return true;
        }
        return false;
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
            if( endYet() ) {
                return;
            }
        }
    }
        
    /**
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        CompositeActor ca = (CompositeActor)getContainer();
        _dir = (CSPDirector)ca.getDirector();
    }
    
    /**
     */
    public boolean performReadNext() {
        if( java.lang.Math.random() < 0.5 ) {
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
    
    private CSPDirector _dir;
}
