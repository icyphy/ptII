/* Base class for Ptolemy configurations.

 Copyright (c) 2000-2003 The Regents of the University of California.
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
@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (celaine@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// Configuration
/**
This is a base class for a composite entity that defines the
configuration of an application that uses Ptolemy II classes.
An instance of this class is in charge of the user interface,
and coordinates multiple views of multiple models. One of its
functions, for example, is to manage the opening of new models,
ensuring that an appropriate view is used. It also makes sure that
if a model is opened that is already open, then existing views are
shown rather than creating new views.
<p>
The applications <i>vergil</i> and <i>moml</i> (at least) use
configurations defined in MoML files, typically located in
ptII/ptolemy/configs. The <i>moml</i> application takes as
command line arguments a list of MoML files, the first of which
is expected to define an instance of Configuration and its contents.
That configuration is then used to open subsequent MoML files on the
command line, and to manage the user interface.
<p>
Rather than performing all these functions itself, this class
is a container for a model directory, effigy factories, and tableau
factories that actually realize these functions. An application
is configured by populating an instance of this class with
a suitable set of these other classes. A minimal configuration
defined in MoML is shown below:
<pre>
&lt;?xml version="1.0" standalone="no"?&gt;
&lt;!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd"&gt;
&lt;entity name="configuration" class="ptolemy.actor.gui.Configuration"&gt;
  &lt;doc&gt;Configuration to run but not edit Ptolemy II models&lt;/doc&gt;
  &lt;entity name="directory" class="ptolemy.actor.gui.ModelDirectory"/&gt;
  &lt;entity name="effigyFactory" class="ptolemy.actor.gui.PtolemyEffigy$Factory"/&gt;
  &lt;property name="tableauFactory" class="ptolemy.actor.gui.RunTableau$Factory"/&gt;
&lt;/entity&gt;
</pre>
<p>
It must contain, at a minimum, an instance of ModelDirectory, named
"directory", and an instance of EffigyFactory, named "effigyFactory".
The openModel() method delegates to the effigy factory the opening of a model.
It may also contain an instance of TextEditorTableauFactory, named "tableauFactory".
A tableau is a visual representation of the model in a top-level window.
The above minimal configuration can be used to run Ptolemy II models
by opening a run panel only.
<p>
When the directory becomes empty (all models have been closed),
it removes itself from the configuration. When this happens, the
configuration calls System.exit() to exit the application.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
@see EffigyFactory
@see ModelDirectory
@see Tableau
@see TextEditorTableau
*/
public class Configuration extends CompositeEntity {

    /** Construct an instance in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  Add the instance to the workspace directory.
     *  Increment the version number of the workspace.
     *  Note that there is no constructor that takes a container
     *  as an argument; a Configuration is always
     *  a top-level entity (this is enforced by the setContainer()
     *  method).
     *  @param workspace The workspace that will list the entity.
     */
    public Configuration(Workspace workspace) {
        super(workspace);
        _configurations.add(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create the first tableau for the given effigy, using the
     *  tableau factory.  This is called after an effigy is first opened,
     *  or when a new effigy is created.  If the method fails
     *  to create a tableau, then it removes the effigy from the directory.
     *  This prevents us from having lingering effigies that have no
     *  user interface.
     *  @return A tableau for the specified effigy, or null if none
     *   can be opened.
     *  @param effigy The effigy for which to create a tableau.
     */
    public Tableau createPrimaryTableau(final Effigy effigy) {
        // NOTE: It used to be that the body of this method was
        // actually executed later, in the event thread, so that it can
        // safely interact with the user interface.
        // However, this does not appear to be necessary, and it
        // makes it impossible to return the tableau.
        // So we no longer do this.

        // If the object referenced by the effigy contains
        // an attribute that is an instance of TextEditorTableauFactory,
        // then use that factory to create the tableau.
        // Otherwise, use the first factory encountered in the
        // configuration that agrees to represent this effigy.
        TableauFactory factory = null;
        if (effigy instanceof PtolemyEffigy) {
            NamedObj model = ((PtolemyEffigy) effigy).getModel();
            if (model != null) {
                Iterator factories =
                    model.attributeList(TableauFactory.class).iterator();
                // If there are more than one of these, use the first
                // one that agrees to open the model.
                while (factories.hasNext() && factory == null) {
                    factory = (TableauFactory) factories.next();
                    try {
                        Tableau tableau = factory.createTableau(effigy);
                        if (tableau != null) {
                            // The first tableau is a master.
                            tableau.setMaster(true);
                            tableau.setEditable(effigy.isModifiable());
                            tableau.show();
                            return tableau;
                        }
                    } catch (Exception ex) {
                        // Ignore so we keep trying.
                        // NOTE: Uncomment this line to detect bugs when
                        // you try to open a model and you get a text editor.
                        // ex.printStackTrace();
                        factory = null;
                    }
                }
            }
        }
        // Defer to the configuration.
        // Create a tableau if there is a tableau factory.
        factory = (TableauFactory) getAttribute("tableauFactory");
        if (factory != null) {
            // If this fails, we do not want the effigy to linger
            try {
                Tableau tableau = factory.createTableau(effigy);
                if (tableau == null) {
                    throw new Exception("Tableau factory returns null.");
                }
                // The first tableau is a master.
                tableau.setMaster(true);
                tableau.setEditable(effigy.isModifiable());
                tableau.show();
                return tableau;
            } catch (Exception ex) {
                // NOTE: Uncomment this line to detect bugs when
                // you try to open a model and you get a text editor.
                // ex.printStackTrace();

                // Remove the effigy.  We were unable to open a tableau for it.
                try {
                    effigy.setContainer(null);
                } catch (KernelException kernelException) {
                    throw new InternalErrorException(
                        this,
                        kernelException,
                        null);
                }

                // As a last resort, attempt to open source code
                // associated with the object.
                if (effigy instanceof PtolemyEffigy) {
                    NamedObj object = ((PtolemyEffigy) effigy).getModel();
                    // Source code is found by name.
                    String filename =
                        object.getClass().getName().replace('.', '/') + ".java";
                    try {
                        URL toRead =
                            getClass().getClassLoader().getResource(filename);
                        if (toRead != null) {
                            return openModel(null, toRead, toRead.toExternalForm());
                        } else {
                            MessageHandler.error(
                                "Cannot find a tableau or the source code for "
                                    + object.getFullName());
                        }
                    } catch (Exception exception) {
                        MessageHandler.error(
                            "Failed to open the source code for "
                                + object.getFullName(),
                            exception);
                    }
                }

                // Note that we can't rethrow the exception here
                // because removing the effigy may result in
                // the application exiting.
                MessageHandler.error(
                    "Failed to open tableau for "
                        + effigy.identifier.getExpression(),
                    ex);
            }
        }
        return null;
    }

    /** Find an effigy for the specified model by searching all the
     *  configurations that have been created. Although typically there is
     *  only one, in principle there may be more than one.  This can be used
     *  to find a configuration, which is typically the result of calling
     *  toplevel() on the effigy.
     *  @param model The model for which to find an effigy.
     *  @return An effigy, or null if none can be found.
     */
    public static Effigy findEffigy(NamedObj model) {
        Iterator configurations = _configurations.iterator();
        while (configurations.hasNext()) {
            Configuration configuration = (Configuration) configurations.next();
            Effigy effigy = configuration.getEffigy(model);
            if (effigy != null) {
                return effigy;
            }
        }
        return null;
    }

    /** Get the model directory.
     *  @return The model directory, or null if there isn't one.
     */
    public ModelDirectory getDirectory() {
        Entity directory = getEntity(_DIRECTORY_NAME);
        if (directory instanceof ModelDirectory) {
            return (ModelDirectory) directory;
        }
        return null;
    }

    /** Get the effigy for the specified Ptolemy model.
     *  This searches all instances of PtolemyEffigy deeply contained by
     *  the directory, and returns the first one it encounters
     *  that is an effigy for the specified model.
     *  @return The effigy for the model, or null if none is found.
     */
    public PtolemyEffigy getEffigy(NamedObj model) {
        Entity directory = getEntity(_DIRECTORY_NAME);
        if (directory instanceof ModelDirectory) {
            return _findEffigyForModel((ModelDirectory) directory, model);
        } else {
            return null;
        }
    }

    /** Open the specified URL.
     *  If a model with the specified identifier is present in the directory,
     *  then find all the tableaux of that model and make them
     *  visible; otherwise, read a model from the specified URL <i>in</i>
     *  and create a default tableau for the model and add the tableau
     *  to this directory.
     *  @param base The base for relative file references, or null if
     *   there are no relative file references.
     *  @param in The input URL.
     *  @param identifier The identifier that uniquely identifies the model.
     *  @return The tableau that is created, or null if none.
     *  @exception Exception If the URL cannot be read.
     */
    public Tableau openModel(URL base, URL in, String identifier)
        throws Exception {
        return openModel(base, in, identifier, null);
    }

    /** Open the specified URL using the specified effigy factory.
     *  If a model with the specified identifier is present in the directory,
     *  then find all the tableaux of that model and make them
     *  visible; otherwise, read a model from the specified URL <i>in</i>
     *  and create a default tableau for the model and add the tableau
     *  to this directory.
     *  @param base The base for relative file references, or null if
     *   there are no relative file references.
     *  @param in The input URL.
     *  @param identifier The identifier that uniquely identifies the model.
     *  @param factory The effigy factory to use.
     *  @return The tableau that is created, or null if none.
     *  @exception Exception If the URL cannot be read.
     */
    public Tableau openModel(
        URL base,
        URL in,
        String identifier,
        EffigyFactory factory)
        throws Exception {
        ModelDirectory directory = (ModelDirectory) getEntity(_DIRECTORY_NAME);
        if (directory == null) {
            throw new InternalErrorException("No model directory!");
        }
        // Check to see whether the model is already open.
        Effigy effigy = directory.getEffigy(identifier);
        if (effigy == null) {
            // No previous effigy exists that is identified by this URL.
            // Find an effigy factory to read it.
            if (factory == null) {
                factory = (EffigyFactory) getEntity("effigyFactory");
            }
            if (factory == null) {
                throw new InternalErrorException("No effigy factories in the configuration!");
            }

            effigy = factory.createEffigy(directory, base, in);

            if (effigy == null) {
                MessageHandler.error(
                    "Unsupported file type or connection not available: "
                        + in.toExternalForm());
                return null;
            }
            if (effigy.identifier.getExpression().compareTo("Unnamed") == 0) {
                // If the value identifier field of the effigy we just
                // created is "Unnamed", then set it to the value of
                // the identifier parameter.
                //
                // HSIFEffigyFactory sets effiigy.identifier because it
                // converts the file we specified from HSIF to MoML and then
                // opens up a file other than the one we specified.
                effigy.identifier.setExpression(identifier);
            }
            // Check the URL to see whether it is a file,
            // and if so, whether it is writable.
            if (in.getProtocol().equals("file")) {
                String filename = in.getFile();
                File file = new File(filename);
                try {
                    if (!file.canWrite()) {
                        effigy.setModifiable(false);
                    }
                } catch (java.security.AccessControlException accessControl) {
                    // If we are running in a sandbox, then canWrite()
                    // may throw an AccessControlException.
                    effigy.setModifiable(false);
                }
            } else {
                effigy.setModifiable(false);
            }
            return createPrimaryTableau(effigy);
        } else {
            // Model already exists.
            return effigy.showTableaux();
        }
    }

    /** Open the specified Ptolemy II model. If a model already has
     *  open tableaux, then put those in the foreground and
     *  return the first one.  Otherwise, create a new tableau and if
     *  necessary, a new effigy.  Unless there is a more natural container
     *  for the effigy (e.g. it is a hierarchical model), then if a new
     *  effigy is created, it is put into the directory of the configuration.
     *  Any new tableau created will be contained by that effigy.
     *  @param entity The model.
     *  @return The tableau that is created, or the first one found,
     *   or null if none is created or found.
     *  @exception IllegalActionException If constructing an effigy or tableau
     *   fails.
     *  @exception NameDuplicationException If a name conflict occurs (this
     *   should not be thrown).
     */
    public Tableau openModel(NamedObj entity)
        throws IllegalActionException, NameDuplicationException {
        
        return openModel(entity, null);
    }

    /** Open the specified Ptolemy II model. If a model already has
     *  open tableaux, then put those in the foreground and
     *  return the first one.  Otherwise, create a new tableau and,
     *  if necessary, a new effigy. Unless there is a more natural
     *  place for the effigy (e.g. it is a hierarchical model), then if a new
     *  effigy is created, it is put into the <i>container</i> argument,
     *  or if that is null, into the directory of the configuration.
     *  Any new tableau created will be contained by that effigy.
     *  @param entity The model.
     *  @param container The container for any new effigy.
     *  @return The tableau that is created, or the first one found,
     *   or null if none is created or found.
     *  @exception IllegalActionException If constructing an effigy or tableau
     *   fails.
     *  @exception NameDuplicationException If a name conflict occurs (this
     *   should not be thrown).
     */
    public Tableau openModel(NamedObj entity, CompositeEntity container)
        throws IllegalActionException, NameDuplicationException {

        // If the entity defers its MoML definition to another,
        // then open that other, unless this is a class extending another.
        // FIXME: Need a way to represent a base class in what is opened!
        NamedObj deferredTo = entity.getMoMLInfo().deferTo;
        String elementName = entity.getMoMLInfo().elementName;        
        if (deferredTo != null
                && (elementName == null || !elementName.equals("class"))) {
            entity = deferredTo;
        }

        // Search the model directory for an effigy that already
        // refers to this model.
        PtolemyEffigy effigy = getEffigy(entity);
        if (effigy != null) {
            // Found one.  Display all open tableaux.
            return effigy.showTableaux();
        } else {
            // There is no pre-existing effigy.  Create one.
            effigy = new PtolemyEffigy(workspace());
            effigy.setModel(entity);

            // Look to see whether the model has a URIAttribute.
            List attributes = entity.attributeList(URIAttribute.class);
            if (attributes.size() > 0) {
                // The entity has a URI, which was probably
                // inserted by MoMLParser.

                URI uri = ((URIAttribute) attributes.get(0)).getURI();

                // Set the URI and identifier of the effigy.
                effigy.uri.setURI(uri);
                effigy.identifier.setExpression(uri.toString());

                if (container == null) {
                    // Put the effigy into the directory
                    ModelDirectory directory = getDirectory();
                    effigy.setName(directory.uniqueName(entity.getName()));
                    effigy.setContainer(directory);
                } else {
                    effigy.setName(container.uniqueName(entity.getName()));
                    effigy.setContainer(container);
                }

                // Create a default tableau.
                return createPrimaryTableau(effigy);
            } else {
                // If we get here, then we are looking inside a model
                // that is defined within the same file as the parent,
                // probably.  Create a new PtolemyEffigy
                // and open a tableau for it.

                // Put the effigy inside the effigy of the parent,
                // rather than directly into the directory.
                NamedObj parent = (NamedObj) entity.getContainer();
                PtolemyEffigy parentEffigy = null;
                // Find the first container above in the hierarchy that
                // has an effigy.
                while (parent != null && parentEffigy == null) {
                    parentEffigy = getEffigy(parent);
                    parent = (NamedObj) parent.getContainer();
                }
                boolean isContainerSet = false;
                if (parentEffigy != null) {
                    // OK, we can put it into this other effigy.
                    effigy.setName(parentEffigy.uniqueName(entity.getName()));
                    effigy.setContainer(parentEffigy);

                    // Set the identifier of the effigy to be that
                    // of the parent with the model name appended.

                    // Note that we add a # the first time, and
                    // then add . after that.  So
                    // file:/c:/foo.xml#bar.bif is ok, but
                    // file:/c:/foo.xml#bar#bif is not
                    // If the title does not contain a legitimate
                    // way to reference the submodel, then the user
                    // is likely to look at the title and use the wrong
                    // value if they xml edit files by hand. (cxh-4/02)
                    String entityName = parentEffigy.identifier.getExpression();
                    String separator = "#";
                    if (entityName.indexOf("#") > 0) {
                        separator = ".";
                    }
                    effigy.identifier.setExpression(
                        entityName + separator + entity.getName());

                    // Set the uri of the effigy to that of
                    // the parent.
                    effigy.uri.setURI(parentEffigy.uri.getURI());

                    // Indicate success.
                    isContainerSet = true;
                }
                // If the above code did not find an effigy to put
                // the new effigy within, then put it into the
                // directory directly or the specified container.
                if (!isContainerSet) {
                    if (container == null) {
                        CompositeEntity directory = getDirectory();
                        effigy.setName(directory.uniqueName(entity.getName()));
                        effigy.setContainer(directory);
                    } else {
                        effigy.setName(container.uniqueName(entity.getName()));
                        effigy.setContainer(container);                        
                    }
                    effigy.identifier.setExpression(entity.getFullName());
                }

                return createPrimaryTableau(effigy);
            }
        }
    }

    /** If the argument is not null, then throw an exception.
     *  This ensures that the object is always at the top level of
     *  a hierarchy.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the argument is not null.
     */
    public void setContainer(CompositeEntity container)
        throws IllegalActionException {
        if (container != null) {
            throw new IllegalActionException(
                this,
                "Configuration can only be at the top level "
                    + "of a hierarchy.");
        }
    }

    /** Find all instances of Tableau deeply contained in the directory
     *  and call show() on them.  If there is no directory, then do nothing.
     */
    public void showAll() {
        final ModelDirectory directory =
            (ModelDirectory) getEntity(_DIRECTORY_NAME);
        if (directory == null)
            return;
        _showTableaux(directory);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The name of the model directory. */
    static public final String _DIRECTORY_NAME = "directory";

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Remove the specified entity; if that entity is the model directory,
     *  then exit the application.  This method should not be called
     *  directly.  Call the setContainer() method of the entity instead with
     *  a null argument.
     *  The entity is assumed to be contained by this composite (otherwise,
     *  nothing happens). This does not alter the entity in any way.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *  @param entity The entity to remove.
     */
    protected void _removeEntity(ComponentEntity entity) {
        super._removeEntity(entity);
        if (entity.getName().equals(_DIRECTORY_NAME)) {
            System.exit(0);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Recursively search the specified composite for an instance of
    // PtolemyEffigy that matches the specified model.
    private PtolemyEffigy _findEffigyForModel(
        CompositeEntity composite,
        NamedObj model) {

        if (composite != null) {
            Iterator effigies =
                composite.entityList(PtolemyEffigy.class).iterator();
            while (effigies.hasNext()) {
                PtolemyEffigy effigy = (PtolemyEffigy) effigies.next();

                // First see whether this effigy matches.
                if (effigy.getModel() == model) {
                    return effigy;
                }
                // Then see whether any effigy inside this one matches.
                PtolemyEffigy inside = _findEffigyForModel(effigy, model);
                if (inside != null) {
                    return inside;
                }
            }
        }
        return null;
    }

    // Call show() on all instances of Tableaux contained by the specified
    // container.
    private void _showTableaux(CompositeEntity container) {
        Iterator entities = container.entityList().iterator();
        while (entities.hasNext()) {
            Object entity = entities.next();
            if (entity instanceof Tableau) {
                ((Tableau) entity).show();
            } else if (entity instanceof CompositeEntity) {
                _showTableaux((CompositeEntity) entity);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The list of configurations that have been created.
    private static List _configurations = new LinkedList();
}
