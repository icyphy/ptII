/* An application for editing ptolemy models visually.

 Copyright (c) 1999-2002 The Regents of the University of California.
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
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.vergil;

// Ptolemy imports
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.MoMLApplication;
import ptolemy.actor.gui.ModelDirectory;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.*;
import ptolemy.moml.ErrorHandler;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.util.StringUtilities;

import javax.swing.SwingUtilities;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;

// Java imports

//////////////////////////////////////////////////////////////////////////
//// VergilApplication
/**
This application opens run control panels for models specified on the
command line.  The exact facilities that are available are determined
by the configuration file ptolemy/configs/vergilConfiguration.xml,
which is loaded before any command-line arguments are processed.
If there are no command-line arguments at all, then the configuration
file is augmented by the MoML file ptolemy/configs/vergilWelcomeWindow.xml.

@author Edward A. Lee and Steve Neuendorffer
@version $Id$
@since Ptolemy II 1.0
@see ptolemy.actor.gui.ModelFrame
@see ptolemy.actor.gui.RunTableau
*/
public class VergilApplication extends MoMLApplication {

    /** Parse the specified command-line arguments, creating models
     *  and frames to interact with them.
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public VergilApplication(String args[]) throws Exception {
	super(args);

        // Create register an error handler with the parser so that
        // MoML errors are tolerated more than the default.
        _parser.setErrorHandler(new VergilErrorHandler());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new instance of this application, passing it the
     *  command-line arguments.
     *  @param args The command-line arguments.
     */
    public static void main(String args[]) {
	try {
	    new VergilApplication(args);
        } catch (Exception ex) {
            MessageHandler.error("Command failed", ex);
            System.exit(0);
        }

        // If the -test arg was set, then exit after 2 seconds.
        if (_test) {
            try {
                Thread.currentThread().sleep(2000);
            } catch (InterruptedException e) {
            }
            System.exit(0);
        }
    }

    /**
     *  Open the MoML file at the given location as a new library in the 
     *  actor library for this application.
     */
    public static void openLibrary(Configuration configuration, 
            File file) throws Exception {       
        final CompositeEntity libraryContainer = (CompositeEntity)
            configuration.getEntity("actor library");
        if (libraryContainer == null) {
            return;
        }
               
        final ModelDirectory directory = (ModelDirectory)
            configuration.getEntity(Configuration._DIRECTORY_NAME);
        if (directory == null) {
            return;
        }
        
        //FIXME: why do we have problems with spaces?
        //URL fileURL = file.toURL();
        URL fileURL =
            new URL( StringUtilities.substitute(file.toURL().toExternalForm(),
                             " ", "%20"));
        String identifier = fileURL.toExternalForm();

        // Check to see whether the library is already open.
        Effigy libraryEffigy = directory.getEffigy(identifier);
        if (libraryEffigy == null) {
            // No previous libraryEffigy exists that is identified by this URL.
            // Parse the user library into the workspace of the actor library.
            MoMLParser parser = new MoMLParser(libraryContainer.workspace());
            parser.parse(fileURL, fileURL);

            // Now create the effigy with no tableau.
            final PtolemyEffigy finalLibraryEffigy = 
                new PtolemyEffigy(directory.workspace());
            finalLibraryEffigy.setSystemEffigy(true);

            final ComponentEntity userLibrary = 
                (ComponentEntity)parser.getToplevel();

            finalLibraryEffigy.setName(
                    directory.uniqueName(userLibrary.getName()));
            
            ChangeRequest request =
                new ChangeRequest(configuration, file.toURL().toString()) {
                    protected void _execute() throws Exception {
                        userLibrary.setContainer(libraryContainer);
                        finalLibraryEffigy.setContainer(directory);
                    }
                };

            libraryContainer.requestChange(request);
            request.waitForCompletion();
      
            finalLibraryEffigy.setModel(userLibrary);
    
            // Identify the URL from which the model was read
            // by inserting an attribute into both the model
            // and the effigy.
            URIAttribute uri =
                new URIAttribute(userLibrary, "_uri");
            uri.setURL(fileURL);
           
            // This is used by TableauFrame in its
            //_save() method.
            finalLibraryEffigy.uri.setURL(fileURL);

            finalLibraryEffigy.identifier.setExpression(identifier);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a default Configuration, which in this case is given by
     *  the MoML file ptolemy/configs/vergilConfiguration.xml.
     *  @return A default configuration.
     *  @exception Exception If the configuration cannot be opened.
     */
    protected Configuration _createDefaultConfiguration() throws Exception {
        Configuration configuration = 
            _readConfiguration("ptolemy/configs/vergilConfiguration.xml");

        String libraryName = System.getProperty("user.home") + 
            System.getProperty("file.separator") + "vergilUserLibrary.xml";
        System.out.println("Attempting to open user library from " +
                libraryName);
        File file = new File(libraryName);
        if(!file.isFile() || !file.exists()) {
            try {
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.write("<entity name=\"vergilUserLibrary\" " +
                        "class=\"ptolemy.moml.EntityLibrary\"/>");
                writer.close();
            } catch (Exception ex) {
                MessageHandler.error("Failed to create an empty user library:"
                        + libraryName, ex);
            }
        }

        // Load the user library.
        try {
            openLibrary(configuration, file);
        } catch (Exception ex) {
            MessageHandler.error("Failed to display user library.", ex);
        }
        return configuration;
    }

    /** Return a default Configuration to use when there are no command-line
     *  arguments, which in this case is given by the default configuration
     *  augmented by the MoML file ptolemy/configs/vergilWelcomeWindow.xml.
     *  @return A configuration for when there no command-line arguments.
     *  @exception Exception If the configuration cannot be opened.
     */
    protected Configuration _createEmptyConfiguration() throws Exception {
        Configuration configuration = _createDefaultConfiguration();

        // FIXME: This code is Dog slow for some reason.
        URL inurl = specToURL("ptolemy/configs/vergilWelcomeWindow.xml");
        _parser.reset();
        _parser.setContext(configuration);
        _parser.parse(inurl, inurl.openStream());
        Effigy doc = (Effigy)configuration.getEntity("directory.doc");
        URL idurl = specToURL("ptolemy/configs/intro.htm");
        doc.identifier.setExpression(idurl.toExternalForm());
        return configuration;
    }

    /** Parse the command-line arguments. This overrides the base class
     *  only to set the usage information.
     *  @exception Exception If an argument is not understood or triggers
     *   an error.
     */
    protected void _parseArgs(final String args[]) throws Exception {
        _commandTemplate = "vergil [ options ] [file ...]";
        // NOTE: Java superstition dictates that if you want something
        // to work, you should invoke it in event thread.  Otherwise,
        // weird things happens at the user interface level.  This
        // seems to prevent occasional errors rending HTML.
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        VergilApplication.super._parseArgs(args);
                    } catch (Exception ex) {
                        MessageHandler.error("Command failed", ex);
                        System.exit(0);
                    }
                }
            });
    }
}
