/* An ODActor that can send out tokens without being provoked by other actors.

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

package ptolemy.domains.od.kernel;

import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// ODSourceActor
/** 
An ODActor that can produce and send out tokens by self invocation without 
requiring provokation by other actors. To invoke activity which may involve 
the future production of tokens, the reinvokeAfterDelay(int delay) method 
is used. This method causes invokation of activity to occur at time equal
to the current time of the actor plus the specified delay.


@author John S. Davis II
@version @(#)ODSourceActor.java	1.2	11/16/98
*/

public class ODSourceActor extends ODActor {

    /** Construct an ODSourceActor in the default workspace with an 
     *  empty string as its name. Increment the version number of the
     *  workspace. The object is added to the workspace directory.
     */
    public ODSourceActor() {
        super();
    }
    
    /** Construct an ODSourceActor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     * @param workspace The workspace that will list this actor.
     */
    public ODSourceActor(Workspace workspace) {
	super(workspace);
    }

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     * @param container The container.
     * @param name The name of this actor within the container.
     * @exception IllegalActionException If the entity cannot be contained
     *  by the proposed container (see the setContainer() method).
     * @exception NameDuplicationException If the name coincides with
     *  an entity already in the container.
     */
    public ODSourceActor(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        _reinvokeInPort = new ODIOPort( this, "reinvokeIn", true, false );
        _reinvokeOutPort = new ODIOPort( this, "reinvokeOut", false, true );
        _reinvokeRelation = new IORelation( container, name + "_innerRel" );
        
        _reinvokeInPort.link( _reinvokeRelation );
        _reinvokeOutPort.link( _reinvokeRelation );
        // System.out.println("Finished linking source actor");
    }
 
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Invoke this actor after the specified delay. The actor is
     *  invoked at time equal to the current time of the actor plus 
     *  the specified delay.
     * @param delay The delay from this actor's current time.
     * @exception IllegalActionException If the delay is negative.
     */        
    public void reinvokeAfterDelay(double delay) 
            throws IllegalActionException {
	if( delay < 0 ) {
	      throw new IllegalActionException( this, "Negative delays "
                      + "are prohibited.");
	}
        Token token = new Token();
        _reinvokeOutPort.send( 0, token, delay );
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////
    
    private ODIOPort _reinvokeInPort;
    private ODIOPort _reinvokeOutPort;
    private IORelation _reinvokeRelation;

}




















