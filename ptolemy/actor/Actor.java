/* An Actor is a non-hierarchical computational unit which operates on 
and/or produces data.
   
 Copyright (c) 1997- The Regents of the University of California.
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

package pt.actors;
import pt.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// Actor
/** 
An Actor is a non-hierarchical computational unit which operates on 
and/or produces data. The Ports of Actors are constrained to be IOPorts
and the Relations are constrained to be IORelations.
@author Mudit Goel
@version $Id$
@see CompositeActor
@see IOPort
@see full-classname
*/
public abstract class Actor extends ComponentEntity implements Executable {
    /** Constructor
     */	
    public Actor() {
        super();
    }

    public Actor(Workspace workspace) {
        super(workspace);
    }
     
    public Actor(CompositeActor container, String name) 
            throws NameDuplicationException {
        super((CompositeEntity)container, name);
    }
    
    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////
    
    public Director getDirector() {
        return ((CompositeActor)getContainer()).getDirector();            
    }

    public void fire() throws IllegalActionException {
    }

    public void initialize() {
    }

    public void postfire() {
    }

    //FIXME::
    public boolean prefire() {
        return true;
    }
  
    public void wrapup() {
        return;
    }
        
    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    ////////////////////////////////////////////////////////////////////////
    ////                         protected variables                    ////

    ////////////////////////////////////////////////////////////////////////
    ////                         private methods                        ////

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

}




