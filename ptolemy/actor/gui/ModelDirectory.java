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

import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.ComponentEntity;
import ptolemy.actor.CompositeActor;

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

    /** Construct a model directory
     *  in the default workspace with an empty string
     *  as its name. Add the entity to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public ModelDirectory() {
        super();
    }

    /** Construct a model directory in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  Add the entity to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     */
    public ModelDirectory(Workspace workspace) {
	super(workspace);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the singleton instance of this class.
     */
    // FIXME this probably shouldn't be a singleton, but how else do we 
    // get it?
    public static ModelDirectory getInstance() {
	return _instance;
    }

    /** If a view on a model with the specified name is present in this 
     *  object, then find all the views of that model and make them 
     *  visible; otherwise, read a model from the specified stream
     *  and create a default view for the model and add the view 
     *  to this directory.
     *  If no model reader has been registered, then do nothing.
     *  @param base The base for relative file references, or null if
     *   there are no relative file references.
     *  @param in The input stream.
     *  @param name The name to use to uniquely identify the new model.
     *  @exception IOException If the stream cannot be read.
     */
    // FIXME: consider adding policy for what the names are.
    public static void openModel(URL base, URL in, String name)
            throws IOException {
        ModelProxy model = (ModelProxy)getInstance().getEntity(name);
        if (model == null) {
            if (_modelReader != null) {
                _modelReader.read(base, in, name);
            }
        } else {
	    // FIXME this logic should be abstracted.
            // Model already exists.
            Iterator entities =
                   model.entityList().iterator();
            while(entities.hasNext()) {
		ModelProxy proxy = (ModelProxy)entities.next();
		Iterator views = 
		    model.entityList().iterator();
		while(views.hasNext()) {
		    View view = (View)views.next();
		    // FIXME: Is this the right thing to do?
		    view.getFrame().toFront();
		}
            }
        }
    }

    /** Specify the object to which reading of a model will be delegated.
     *  @param reader The object that will handle reading a model.
     */
     public static void setModelReader(ModelReader reader) {
        _modelReader = reader;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Remove the specified entity. This method should not be used
     *  directly.  Call the setContainer() method of the entity instead with
     *  a null argument.
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
	System.exit(0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The model reader, if one has been registered.
    private static ModelReader _modelReader = null;

    // The singleton model directory
    private static ModelDirectory _instance = new ModelDirectory();
}
