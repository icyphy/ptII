/* A named object that represents a graphical view of another ptolemy model.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.gui.MessageHandler;
import ptolemy.gui.CancelException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import javax.swing.JFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.Iterator;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// ModelProxy
/**
A named object that represents an object that can be viewed in an application.
This is intended to be used in a user interface model.  

@author Steve Neuendorffer
@version $Id$
*/
public class ModelProxy extends CompositeEntity {

    /** Construct a view with the given name contained by the specified
     *  ModelDirectory. The view will act on the given model.  
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This entity will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  This constructor write-synchronizes on the workspace.
     *  @param container The container entity.
     *  @param name The name of the entity.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public ModelProxy(ModelDirectory container,
		String name)
            throws IllegalActionException, NameDuplicationException {
	super(container, name);
    }

    /** Override the base class so that views contained within this object
     *  are removed before the model is removed from the ModelDirectory.
     *  This should trigger the frames associated with those views to be 
     *  closed.
     *  @param container The container to attach this attribute to..
     *  @exception IllegalActionException If the proposed container is not
     *  an instance of ModelDirectory, if this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
	if (container == null) {
	    // Remove all views.
	    CompositeEntity oldcontainer = (CompositeEntity)getContainer();
	    super.setContainer(container);
	    List viewList = oldcontainer.entityList();
	    Iterator views = viewList.iterator();
	    while (views.hasNext()) {
		ComponentEntity view = (ComponentEntity)views.next();
		view.setContainer(null);
	    }
	} else if(container instanceof ModelDirectory) {
	    super.setContainer(container);
	} else {
	    throw new IllegalActionException(this, container, 
		"The container can only be set to an " + 
		"instance of ModelDirectory");
	}
    }

    /** Remove the specified entity. This method should not be used
     *  directly.  Call the setContainer() method of the entity instead with
     *  a null argument.
     *  The entity is assumed to be contained by this composite (otherwise,
     *  nothing happens). This does not alter the entity in any way.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *  This class overrides the superclass to check if this composite is
     *  empty, and if so, removes this object from its container.
     *  @param entity The entity to remove.
     */
    protected void _removeEntity(ComponentEntity entity) {
	super._removeEntity(entity);
       	if(entityList().size() == 0) {
	    try {
		setContainer(null);
	    } catch (Exception ex) {
		// Ignore.
	    }
	}
    }
}

