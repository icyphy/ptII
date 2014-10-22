/* Utilities for User Actor Libraries

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.LibraryBuilder;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// UserActorLibrary

/**
 Access the User Actor Library.

 @author Christopher Brooks, based on work by Steve Neuendorffer, Edward A. Lee, Contributor: Chad Berkeley (Kepler)
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (neuendor)
 */
public class UserActorLibrary {
    /**
     *  Open the user actor library as a new library in the actor
     *  library for this application.
     *
     *  <p>The name of the user actor library consists of the
     *  values of {@link ptolemy.util.StringUtilities#preferencesDirectory()}
     *  and {@link #USER_LIBRARY_NAME} and ".xml" concatenated.
     *
     *  <p>An alternate class can be used to build the library if reading the
     *  MoML is not desired.  The class must extend ptolemy.moml.LibraryBuilder
     *  and the _alternateLibraryBuilder property must be set with the 'value'
     *  set to the class that extends LibraryBuilder.
     *
     *  @param configuration The configuration where we look for the
     *  actor library.
     *  @exception Exception If there is a problem opening the configuration,
     *  opening the MoML file, or opening the MoML file as a new library.
     */
    public static void openUserLibrary(Configuration configuration)
            throws Exception {

        // FIXME: If the name is something like
        // "vergilUserLibrary.xml" then when we save an actor in the
        // library and then save the window that comes up the name of
        // entity gets set to vergilUserLibrary instead of the value
        // of USER_LIBRARY_NAME. This causes problems when we
        // try to save another file. The name of the entity gets
        // changed by the saveAs code.

        String libraryName = null;

        try {
            libraryName = StringUtilities.preferencesDirectory()
                    + USER_LIBRARY_NAME + ".xml";
        } catch (Exception ex) {
            System.out.println("Warning: Failed to get the preferences "
                    + "directory (-sandbox always causes this): " + ex);
        }

        if (libraryName != null) {
            File file = new File(libraryName);

            if (!file.isFile() || !file.exists()) {
                // File might exist under an old name.
                // Try to read it.
                String oldLibraryName = StringUtilities.preferencesDirectory()
                        + "user library.xml";
                File oldFile = new File(oldLibraryName);

                if (oldFile.isFile() && oldFile.exists()) {
                    if (!oldFile.renameTo(file)) {
                        throw new IOException("Failed to rename \"" + oldFile
                                + "\" to \"" + file + "\".");
                    }
                }
            }

            if (!file.isFile() || !file.exists()) {
                FileWriter writer = null;

                try {
                    if (!file.createNewFile()) {
                        throw new Exception(file + "already exists?");
                    }

                    writer = new FileWriter(file);
                    writer.write("<entity name=\"" + USER_LIBRARY_NAME
                            + "\" class=\"ptolemy.moml.EntityLibrary\"/>");
                    writer.close();
                } catch (Exception ex) {
                    throw new Exception("Failed to create an empty user "
                            + "library: " + libraryName, ex);
                } finally {
                    if (writer != null) {
                        writer.close();
                    }
                }
            }
            openLibrary(configuration, file);
        }
    }

    /**
     *  Open the MoML file at the given location as a new library in
     *  the actor library for this application.
     *
     *  <p>An alternate class can be used to build the library if reading
     *  the MoML is not desired.  The class must extend
     *  ptolemy.moml.LibraryBuilder and the _alternateLibraryBuilder
     *  property must be set with the 'value' set to the class that
     *  extends LibraryBuilder.</p>
     *
     *  <p>A library of components is a .xml file that defines a MoML Class
     *  that extends ptolemy.moml.EntityLibrary, for example, the following
     *  file creates a library called "MyActors" that has an XYPlotter
     *  in the library:</p>
     *  <pre>
     *  &lt;?xml version="1.0" standalone="no"?&gt;
     *  &lt;!DOCTYPE class PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
     *      "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd"&gt;
     *  &lt;class name="MyActors" extends="ptolemy.moml.EntityLibrary"&gt;
     *    &lt;configure&gt;
     *      &lt;group&gt;
     *         &lt;entity name="XY Plotter" class="ptolemy.actor.lib.gui.XYPlotter"/&gt;
     *      &lt;/group&gt;
     *    &lt;/configure&gt;
     *  &lt;/class&gt;
     *  </pre>
     *
     *  <p>Note that one restriction is to see the new library, one must
     *  open a new Graph viewer (New -&gt; Graph).</p>
     *
     *  @param configuration The configuration where we look for the
     *  actor library.
     *  @param file The MoML file to open.
     *  @exception Exception If there is a problem opening the
     *  configuration, opening the MoML file, or opening the MoML file
     *  as a new library.
     */
    public static void openLibrary(Configuration configuration, File file)
            throws Exception {
        CompositeEntity library = null;
        final CompositeEntity libraryContainer = (CompositeEntity) configuration
                .getEntity("actor library");

        final ModelDirectory directory = (ModelDirectory) configuration
                .getEntity(Configuration._DIRECTORY_NAME);

        if (directory == null) {
            return;
        }

        if (libraryContainer == null) {
            return;
        }

        StringAttribute alternateLibraryBuilderAttribute = (StringAttribute) libraryContainer
                .getAttribute("_alternateLibraryBuilder");

        // If the _alternateLibraryBuilder attribute is present,
        // then we use the specified class to build the library
        // instead of just reading the moml.

        if (alternateLibraryBuilderAttribute != null) {
            // Get the class that will build the library from the plugins
            String libraryBuilderClassName = alternateLibraryBuilderAttribute
                    .getExpression();

            // Dynamically load the library builder and build the library
            Class libraryBuilderClass = Class.forName(libraryBuilderClassName);
            LibraryBuilder libraryBuilder = (LibraryBuilder) libraryBuilderClass
                    .newInstance();

            // Set the attributes defined in the moml to the attributes of the
            // LibraryBuilder
            libraryBuilder.addAttributes(alternateLibraryBuilderAttribute
                    .attributeList());

            try {
                library = libraryBuilder.buildLibrary(libraryContainer
                        .workspace());
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new Exception("Cannot create library with "
                        + "LibraryBuilder: ", ex);
            }
        }

        // If we have a jar URL, convert spaces to %20
        URL fileURL = JNLPUtilities.canonicalizeJarURL(file.toURI().toURL());

        String identifier = fileURL.toExternalForm();

        // Check to see whether the library is already open.
        Effigy libraryEffigy = directory.getEffigy(identifier);

        if (libraryEffigy == null) {
            if (library == null) {
                // Only do this if the library hasn't been set above
                // by a LibraryBuilder
                // No previous libraryEffigy exists that is identified
                // by this URL. Parse the user library into the
                // workspace of the actor library.
                MoMLParser parser = new MoMLParser(libraryContainer.workspace());

                // Set the ErrorHandler so that if we have
                // compatibility problems between devel and production
                // versions, we can skip that element.
                // MoMLParser.setErrorHandler(new VergilErrorHandler());
                parser.parse(fileURL, fileURL);

                library = (CompositeEntity) parser.getToplevel();
            }

            // library.setContainer(libraryContainer); //i don't know if this is
            // needed
            // Now create the effigy with no tableau.
            final PtolemyEffigy finalLibraryEffigy = new PtolemyEffigy(
                    directory.workspace());
            finalLibraryEffigy.setSystemEffigy(true);

            // Correct old library name, if the loaded library happens
            // to be user library.
            if (library == null) {
                throw new NullPointerException("library == null?");
            }
            if (library.getName().equals("user library")) {
                library.setName(USER_LIBRARY_NAME);
            }

            finalLibraryEffigy.setName(directory.uniqueName(library.getName()));

            _instantiateLibrary(library, directory, configuration, file,
                    libraryContainer, finalLibraryEffigy);

            finalLibraryEffigy.setModel(library);

            // Identify the URL from which the model was read
            // by inserting an attribute into both the model
            // and the effigy.
            URIAttribute uri = new URIAttribute(library, "_uri");
            uri.setURL(fileURL);

            // This is used by TableauFrame in its _save() method.
            finalLibraryEffigy.uri.setURL(fileURL);

            finalLibraryEffigy.identifier.setExpression(identifier);
        }
    }

    /** Save the given entity in the user library in the given
     *  configuration.
     *  @param configuration The configuration.
     *  @param entity The entity to save.
     *  @exception IOException if the user library cannot be found.
     *  @exception IllegalActionException If there is a problem creating
     *  the entity in the library.
     *  @exception NameDuplicationException If a entity with the same
     *  name already exists in the library.
     *  @since Ptolemy II 5.2
     */
    public static void saveComponentInLibrary(Configuration configuration,
            Entity entity) throws IOException, IllegalActionException,
            NameDuplicationException {
        if (entity == null) {
            throw new NullPointerException("Save in library failed. "
                    + "entity was null, cannot save a null entity.");
        }
        CompositeEntity libraryInstance = (CompositeEntity) configuration
                .getEntity("actor library." + USER_LIBRARY_NAME);

        if (libraryInstance == null) {
            throw new IOException("Save In Library failed: "
                    + "Could not find user library with name \""
                    + USER_LIBRARY_NAME + "\".");
        }

        // Note that the library in the configuration is an
        // instance of another model. We have to go get the
        // original model to make sure that the change propagates
        // back to the file from which the library is loaded.
        Tableau libraryTableau = configuration.openModel(libraryInstance);
        PtolemyEffigy libraryEffigy = (PtolemyEffigy) libraryTableau
                .getContainer();
        CompositeEntity library = (CompositeEntity) libraryEffigy.getModel();

        StringWriter buffer = new StringWriter();

        // Check whether there is already something existing in the
        // user library with this name.
        if (library == null) {
            throw new InternalErrorException("Save in library failed. "
                    + "libraryEffigy.getModel() returned null.");
        }
        if (library.getEntity(entity.getName()) != null) {
            throw new NameDuplicationException(entity,
                    "Save In Library failed: An object"
                            + " already exists in the user library with name "
                            + "\"" + entity.getName() + "\".");
        }

        if (entity.getName().trim().equals("")) {
            entity.exportMoML(buffer, 1, "Unnamed");
        } else {
            entity.exportMoML(buffer, 1);
        }

        ChangeRequest request = new MoMLChangeRequest(entity, library,
                buffer.toString());
        library.requestChange(request);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The name of the user library.  The default value is
     *  "UserLibrary".  The value of this variable is what appears
     *  in the Vergil left hand tree menu.
     *  <p>This variable is not final so that users of this class
     *  may change it.
     */
    public static/* final */String USER_LIBRARY_NAME = "UserLibrary";

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * instantiate a ComponentEntity and create the changeRequest to
     * implement it in the model
     */
    private static void _instantiateLibrary(final CompositeEntity library,
            final ModelDirectory directory, Configuration configuration,
            File file, final CompositeEntity libraryContainer,
            final PtolemyEffigy finalLibraryEffigy) throws Exception {
        ChangeRequest request = new ChangeRequest(configuration, file.toURI()
                .toURL().toString()) {
            @Override
            protected void _execute() throws Exception {
                // The library is a class!
                library.setClassDefinition(true);
                library.instantiate(libraryContainer, library.getName());
                finalLibraryEffigy.setContainer(directory);
            }
        };

        libraryContainer.requestChange(request);
        request.waitForCompletion();
    }

}
