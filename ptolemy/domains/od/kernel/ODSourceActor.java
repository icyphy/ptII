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

package ptolemy.domains.od.kernel;

import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// ODSourceActor
/** 


@author John S. Davis II
@version @(#)ODSourceActor.java	1.2	11/16/98
*/
public class ODSourceActor extends ODActor {

    /** 
     */
    public ODSourceActor() {
        super();
    }
    
    /** 
     */
    public ODSourceActor(Workspace workspace) {
	super(workspace);
    }

    /** 
     */
    public ODSourceActor(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        _refireInPort = new ODIOPort( this, "refireIn", true, false );
        _refireOutPort = new ODIOPort( this, "refireOut", false, true );
        _refireRelation = new IORelation( container, name + "_innerRel" );
        
        _refireInPort.link( _refireRelation );
        _refireOutPort.link( _refireRelation );
        // JFIXME 
        // System.out.println("Finished linking source actor");
    }
 
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** FIXME: Should we check for negative delays?
     */
    public void refireAfterDelay(double delay) 
            throws IllegalActionException {
        Token token = new Token();
        _refireOutPort.send( 0, token, delay );
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                      protected variables                  ////
    
    protected ODIOPort _refireInPort;
    protected ODIOPort _refireOutPort;
    protected IORelation _refireRelation;

    ///////////////////////////////////////////////////////////////////
    ////                        private methods			   ////

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////
    
    


}




















