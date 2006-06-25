/* Utilities for Actor Libraries

 Copyright (c) 2006 The Regents of the University of California.
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

import java.io.IOException;
import java.io.StringWriter;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.KernelException;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// ActorLibraryUtilities

/**
 Utilities for Actor Libraries

 @author Christopher Brooks, based on work by Steve Neuendorffer, Edward A. Lee, Contributor: Chad Berkeley (Kepler)
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (neuendor)
 */
public class ActorLibraryUtilities {
    /** Save the given entity in the user library in the given
     *  configuration.
     *  @param configuration The configuration.
     *  @param entity The entity to save.
     *  @since Ptolemy 2.1
     */
    public static void saveComponentInLibrary(Configuration configuration,
            Entity entity) {
        try {
            CompositeEntity libraryInstance = (CompositeEntity) configuration
                    .getEntity("actor library." + VERGIL_USER_LIBRARY_NAME);

            if (libraryInstance == null) {
                MessageHandler.error("Save In Library failed: "
                        + "Could not find user library with name \""
                        + VERGIL_USER_LIBRARY_NAME + "\".");
                return;
            }

            // Note that the library in the configuration is an
            // instance of another model.  We have to go get the
            // original model to make sure that the change propagates
            // back to the file from which the library is loaded from.
            Tableau libraryTableau = configuration.openModel(libraryInstance);
            PtolemyEffigy libraryEffigy = (PtolemyEffigy) libraryTableau
                    .getContainer();
            CompositeEntity library = (CompositeEntity) libraryEffigy
                    .getModel();

            StringWriter buffer = new StringWriter();

            // Check whether there is already something existing in the
            // user library with this name.
            if (library.getEntity(entity.getName()) != null) {
                MessageHandler.error("Save In Library failed: An object"
                        + " already exists in the user library with name "
                        + "\"" + entity.getName() + "\".");
                return;
            }

            entity.exportMoML(buffer, 1);

            ChangeRequest request = new MoMLChangeRequest(entity, library,
                    buffer.toString());
            library.requestChange(request);
        } catch (IOException ex) {
            // Ignore.
        } catch (KernelException ex) {
            // Ignore.
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The name of the user library.  The default value is
     *  "UserLibrary".  The value of this variable is what appears
     *  in the Vergil left hand tree menu.
     */
    public static String VERGIL_USER_LIBRARY_NAME = "UserLibrary";

}
