/* A CompositeActor is a computational unit which contains Actors and
operates on and/or produces data.

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
//// CompositeActor
/** 
A CompositeActor is a computational unit which contains Actors and
operates on and/or produces data. The Ports of a CompositeActor are
constrained to be IOPorts.
@author Mudit Goel
@version $Id$
@see Actor
@see IOPort
@see full-classname
*/
public abstract class CompositeActor extends CompositeEntity {
    /** Construct an actor in the default workspace with an empty string
     *  as its name.
     *  Increment the version number of the workspace.
     */
    public CompositeActor() {
	super();
    }

    /** Construct an actor in the specified workspace with an empty
     *  string as a name (you can then change the name with setName()).
     *  If the workspace argument is null, then use the default workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     */
    public CompositeActor(Workspace workspace) {
        super(workspace);
    }

    /** Create an object with a name and a container. 
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This entity will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The parent actor.
     *  @param name The name of the actor.
     *  @exception IllegalActionException Name argument is null.
     *  @exception NameDuplicationException Name coincides with
     *   an actor already in the container.
     */
    public CompositeActor(CompositeActor container, String name) 
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Sets the director for execution of this CompositeActor
     * @param director is the Director responsible for the execution of this
     *  CompositeActor
     */
    public void setDirector(Director director) {
	_director = director;
        return;
    }

    /** Returns the director responsible for execution
     * @return the director
     */
    public Director getDirector() {
	return _director;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    private Director _director;
}
