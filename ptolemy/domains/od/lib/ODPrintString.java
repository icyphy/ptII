/* 

 Copyright (c) 1997-1998 The Regents of the University of California.
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

package ptolemy.domains.od.lib;

import ptolemy.domains.od.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// ODPrintString
/** 


@author John S. Davis II
@version %W%	%G%
*/
public class ODPrintString extends ODActor {

    /** 
     */
    public ODPrintString(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        _input = new ODIOPort( this, "input", true, false );
        _input.setMultiport(true);
    }
 
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** 
     */
    public void fire() throws IllegalActionException {
    // System.out.println("printer is beginning to fire()");
        StringToken token;
        double time;
        
        while( true ) {
            System.out.println("\nStarting ODPrintString.getNextToken()");
            token = (StringToken)getNextToken();
            System.out.println("Finished ODPrintString.getNextToken()");
            System.out.println("\tString is "+ token.stringValue() );
            // time = getCurrentTime();
            // System.out.println("\tTime is " + time);
            // System.out.print("\n");
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    private ODIOPort _input;
    
}




















