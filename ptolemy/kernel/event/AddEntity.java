/* A request to add an entity to a container.

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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.kernel.event;

import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.Actor;
import ptolemy.actor.Director;

//////////////////////////////////////////////////////////////////////////
//// AddEntity
/** 
A request to add an entity to a specified container.
To use this, first create the entity in the same workspace as the container,
assign it a name, construct an instance of this object, and then queue
this object with the director or manager using the requestChange() method.
This can only be done with entities that have constructors that take
a workspace argument rather than a container.
Note that if the container is an instance of CompositeActor, this will
result in the schedule and type resolution being invalidated.  Thus,
at the next opportunity, both will be redone. Also, if the entity being
added implements the Actor interface, then a second change request
is posted with its director to have it initialized.

@author  Edward A. Lee
@version $Id$
@see ptolemy.kernel.ComponentEntity
@see InitializeActor
*/
public class AddEntity extends ChangeRequest {

    /** Construct a request with the specified originator, entity to
     *  be added, and proposed container.
     *  @param originator The source of the change request.
     *  @param entity The entity to add.
     *  @param container The proposed container.
     */	
    public AddEntity(Nameable originator, ComponentEntity entity,
            CompositeEntity container) {
        super(originator, "Add the entity "
        + entity.getName() + " to " + container.getFullName());
        _entity = entity;
        _container = container;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute the change by calling the setContainer() method of the
     *  entity. If the entity implements the Actor interface, then
     *  generate a new change request to initialize the entity.
     *  @exception ChangeFailedException If the container rejects the
     *   entity, for example because of name duplication.
     */	
    public void execute() throws ChangeFailedException {
        try {
            _entity.setContainer(_container);
        } catch (KernelException ex) {
            throw new ChangeFailedException(this, ex);
        }
        if (_entity instanceof Actor) {
            Actor actor = (Actor)_entity;
            Director director = actor.getDirector();
            if (director != null) {
                director.requestChange(
                    new InitializeActor(getOriginator(),actor));
            }
        }
    }

    /** Get the entity.
     *  @return The entity to be added.
     */
    public ComponentEntity getEntity() {
        return _entity;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The proposed container.
    private CompositeEntity _container;

    // The entity to add.
    private ComponentEntity _entity;
}
