/* 

 Copyright (c) 1997-1999 The Regents of the University of California.
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

package ptolemy.domains.dde.lib;

import ptolemy.domains.dde.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// DDEStringMerge
/** 


@author John S. Davis II
@version %W%	%G%
*/
public class DDEStringMerge extends DDEActor {

    /** 
     */
    public DDEStringMerge(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

	_stringOutput = new DDEIOPort( this, "stringOut", false, true ); 
	_nullOutput =  new DDEIOPort( this, "nullOut", false, true );
	_input =  new DDEIOPort( this, "input", true, false );

	_stringOutput.setMultiport(true);
	_nullOutput.setMultiport(true);
	_input.setMultiport(true);
        
    }
 
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** 
     */
    public void fire() throws IllegalActionException {
	boolean notFinishedYet = true;
	Token token = null;
        
	// FIXME: This is valid because DDESourceActor.initialize()
        // calls getNextToken();
        while( notFinishedYet ) {
	    token = getNextToken(); 
	    _stringOutput.send( 0, token, getCurrentTime() );
        }
        
        // System.out.println(getName()+" is finished with fire()");
	// System.out.println(getName()+" returns "+postfire()+" for postfire()");
        
            /*
            System.out.println(getName() + " fired \"" 
                    + strTime.getString() + "\" at time = " + fireTime );
            */
        // ((DDEDirector)getDirector()).addWriteBlock();
        // System.out.println("#####"+getName()+" is finished executing");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                        private methods			   ////

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    private DDEIOPort _stringOutput;
    private DDEIOPort _nullOutput;
    private DDEIOPort _input;

}




















