/* A request to place an object with another.

 Copyright (c) 2000 The Regents of the University of California.
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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.kernel.event;

import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;

//////////////////////////////////////////////////////////////////////////
//// PlaceEntity
/**
A request to place an object within a container.

@author  Steve Neuendorffer
@version $Id$
*/
public class PlaceEntity extends ChangeRequest {

    /** Construct a request with the specified originator, port, and relation.
     *  @param originator The source of the change request.
     *  @param port The port to link.
     *  @param relation The relation to link.
     */
    public PlaceEntity(Nameable originator, 
		     ComponentEntity entity, CompositeEntity container) {
        super(originator, "Place entity "
                + entity.getFullName() + " in container "
                + container.getFullName());
	_entity = entity;
	_container = container;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute the change by setting the container of the object to be
     *  the container.
     *  @exception ChangeFailedException If the object is rejected by
     *  the container.
     */
    public void execute() throws ChangeFailedException {
        try {
	    _entity.setContainer(_container);
        } catch (IllegalActionException ex) {
            throw new ChangeFailedException(this, ex);
        } catch (NameDuplicationException ex) {
            throw new ChangeFailedException(this, ex);
        }
    }

    /** Get the container.
     *  @return The object to set as the container.
     */
    public CompositeEntity getContainer() {
        return _container;
    }

    /** Get the object.
     *  @return The object to place.
     */
    public ComponentEntity getEntity() {
        return _entity;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The container.
    private CompositeEntity _container;

    // The entity.
    private ComponentEntity _entity;
}
