/* A directory of open models.

 Copyright (c) 1999-2000 The Regents of the University of California.
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

package ptolemy.actor.gui;

import ptolemy.actor.CompositeActor;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

//////////////////////////////////////////////////////////////////////////
//// ModelDirectory
/**
A directory of open models. This is a static class, with no instances
nor any way to create instances.  It is static so that any application
that opens Ptolemy models can use it to make sure that a model is not
opened more than once.  The key for each entry is any object, often an
instance of String.  Typical choices (which depend on the application)
are the fully qualified class name, or the canonical URL or file name
for a MoML file that describes the model.

@author Edward A. Lee and Steve Neuendorffer
@version $Id$
*/
public class ModelDirectory extends CompositeEntity {

    /** Construct a model directory with the specified container and name.
     *  @param container The application that contains this directory.
     *  @param name The name of the directory.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.  This should not be thrown.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public ModelDirectory(Application container, String name)
            throws IllegalActionException, NameDuplicationException {
	super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the model that corresponds to the specified identifier.
     *  @param identifier The identifier for the model, such as a URL.
     *  @return The model, or null if there is no such model in the directory.
     */
    public ModelProxy getModel(String identifier) {
        Iterator entities = entityList(ModelProxy.class).iterator();
        while (entities.hasNext()) {
            ModelProxy entity = (ModelProxy)entities.next();
            Parameter idParam = (Parameter)entity.getAttribute("identifier");
            if (idParam != null) {
                try {
                    String id = ((StringToken)idParam.getToken()).stringValue();
                    if (id.equals(identifier)) return entity;
                } catch (IllegalActionException ex) {
                    throw new InternalErrorException(
                    "Can't get string value of identifier! " + ex.toString());
                }
            }
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Remove the specified entity, and if there are no more models
     *  in the directory, then remove this directory from its container.
     *  This method should not be used directly.  Call the setContainer()
     *  method of the entity instead with a null argument.
     *  The entity is assumed to be contained by this composite (otherwise,
     *  nothing happens). This does not alter the entity in any way.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *  This class overrides the superclass to check if this composite is
     *  empty, and if so, calls system.exit
     *  @param entity The entity to remove.
     */
    protected void _removeEntity(ComponentEntity entity) {
	super._removeEntity(entity);
	if (entityList(ModelProxy.class).size() == 0) {
            try {
		setContainer(null);
            } catch (KernelException ex) {
                throw new InternalErrorException("Cannot remove directory!");
            }
        }
    }
}
