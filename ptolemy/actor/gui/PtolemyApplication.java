/* An application that contains models and frames for interacting with them.

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
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

// Java imports
// FIXME: Trim this.
import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System;
import java.net.MalformedURLException;
import java.net.URL;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

// Ptolemy imports
import ptolemy.actor.Director;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.CompositeActor;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Variable;
import ptolemy.data.expr.Parameter;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;

// XML Imports
import com.microstar.xml.XmlException;

//////////////////////////////////////////////////////////////////////////
//// PtolemyApplication
/**
An application that opens a run control panel for each model that is 
created, instead of automatically executing it. 
Any number of models can be simultaneously running under
the same application.  An instance of RunView is created for each model and
added to the Model Directory.  When the frames displayed by this application
are all closed, then the application will automatically exit.
If no models are specified on the command line, then a default model is
opened.

@author Edward A. Lee and Steve Neuendorffer
@version $Id$
@see ModelFrame
@see RunView
*/
public class PtolemyApplication extends Application {

    /** Parse the specified command-line arguments, creating models
     *  and frames to interact with them.  If the size of the argument
     *  array is 0, then open a default model.
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public PtolemyApplication(String args[]) throws Exception {
        // Invoke the base class constructor with null arguments to prevent
        // the base class from running any specified models.
        super(null);
	if (args.length == 0) {
            // FIXME: We need a better initial default model,
            // perhaps something with a console that we can type
            // commands into?
            String temporaryArgs[] = {"ptolemy/moml/demo/modulation.xml"};
	    _parseArgs(temporaryArgs);
	} else { 
            _parseArgs(args);
        }

        // The Java look & feel is pretty lame, so we use the native
        // look and feel of the platform we are running on.
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Ignore exceptions, which only result in the wrong look and feel.
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new application with the specified command-line arguments.
     *  If the command-line arguments include the names of MoML files or
     *  URLs for MoML files, then one window is opened for each model.
     *  @param args The command-line arguments.
     */
    public static void main(String args[]) {
        try {
            PtolemyApplication app = new PtolemyApplication(args);
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

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Parse a command-line argument.
     *  @return True if the argument is understood, false otherwise.
     *  @exception Exception If something goes wrong.
     */
    protected boolean _parseArg(String arg) throws Exception {
        if (arg.equals("-class")) {
            _expectingClass = true;
        } else if (arg.equals("-help")) {
            System.out.println(_usage());
            // Don't call System.exit(0) here, it will break the test suites
        } else if (arg.equals("-test")) {
            _test = true;
        } else if (arg.equals("-version")) {
            System.out.println("Version 1.0, Build $Id$");
            // quit the program if the user asked for the version            
            // Don't call System.exit(0) here, it will break the test suites
        } else if (arg.equals("")) {
            // Ignore blank argument.
        } else {
            ModelDirectory directory = (ModelDirectory)getEntity("directory");
            if (directory == null) {
                throw new InternalErrorException("No model directory!");
            }
            if (_expectingClass) {
                _expectingClass = false;

                ModelProxy model = directory.getModel(arg);
                if (model == null) {
                    // No preexisting model.  Create class.
                    Class newClass = Class.forName(arg);

                    // Instantiate the specified class in a new workspace.
                    Workspace workspace = new Workspace();

                    // Get the constructor that takes a Workspace argument.
                    Class[] argTypes = new Class[1];
                    argTypes[0] = workspace.getClass();
                    Constructor constructor = newClass.getConstructor(argTypes);

                    Object args[] = new Object[1];
                    args[0] = workspace;
                    CompositeActor newModel
                             = (CompositeActor)constructor.newInstance(args);
		
                    // Create a proxy for the model.
                    PtolemyModelProxy proxy
                            = new PtolemyModelProxy(workspace());
                    proxy.setModel(newModel);
                } else {
                    // Model already exists.
                    model.showViews();
                }
	    } else {
                if (!arg.startsWith("-")) {
                    // Assume the argument is a file name.
                    // Attempt to read it.
                    URL inurl;
                    URL base;
                    // Default key is the argument itself.
                    String key = arg;
                    try {
                        // First argument is null because we are only
                        // processing absolute URLs this way.  Relative
                        // URLs are opened as ordinary files.
                        inurl = new URL(null, arg);
                        
                        // If URL was successfully constructed, use its external
                        // form as the key.
                        key = inurl.toExternalForm();

                        // Strangely, the XmlParser does not want as base the
                        // directory containing the file, but rather the
                        // file itself.
                        base = inurl;
                    } catch (MalformedURLException ex) {
                        try {
                            File file = new File(arg);
                            if(!file.exists()) {
                                // I hate communicating by exceptions
                                throw new MalformedURLException();
                            }
                            inurl = file.toURL();
                            
                            // Strangely, the XmlParser does not want as base
                            // the directory containing the file, but rather
                            // the file itself.
                            base = file.toURL();

                            // If the file was successfully constructed,
                            // use its URL as the key.
                            key = base.toExternalForm();

                        } catch (MalformedURLException ex2) {
                            // Try one last thing, using the classpath.
                            // FIXME: why not getClass().getClassLoader()....?
                            inurl = Class.forName(
                                    "ptolemy.kernel.util.NamedObj").
                            getClassLoader().getResource(arg);
                            if (inurl == null) {
                                throw new IOException("File not found: " + arg);
                            }
                            // If URL was successfully constructed, use its
                            // external form as the key.
                            key = inurl.toExternalForm();
                            
                            base = inurl;
                        }
                    }
                    // Now defer to the model reader.
                    openModel(base, inurl, key);
                } else {
                    // Argument not recognized.
                    return false;
                }
            }
        }
        return true;
    }

    /** Parse the command-line arguments.
     *  @exception Exception If an argument is not understood or triggers
     *   an error.
     */
    protected void _parseArgs(String args[]) throws Exception {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (_parseArg(arg) == false) {
		// FIXME: parameters are handled differently from classes
		// for no apparent reason.
                if (arg.startsWith("-") && i < args.length - 1) {
                    // Save in case this is a parameter name and value.
                    _parameterNames.add(arg.substring(1));
                    _parameterValues.add(args[i + 1]);
                    i++;
                } else {
                    // Unrecognized option.
                    throw new IllegalActionException("Unrecognized option: "
                            + arg);
                }
            }
        }
        if (_expectingClass) {
            throw new IllegalActionException("Missing classname.");
        }
        // Check saved options to see whether any is a parameter.
        Iterator names = _parameterNames.iterator();
        Iterator values = _parameterValues.iterator();
        while (names.hasNext() && values.hasNext()) {
            String name = (String)names.next();
            String value = (String)values.next();

            boolean match = false;
            ModelDirectory directory = (ModelDirectory)getEntity("directory");
            if (directory == null) {
                throw new InternalErrorException("No model directory!");
            }
            Iterator proxies
                    = directory.entityList(ModelProxy.class).iterator();
            while(proxies.hasNext()) {
		ModelProxy proxy = (ModelProxy)proxies.next();
		if(proxy instanceof PtolemyModelProxy) {
		    NamedObj model = ((PtolemyModelProxy)proxy).getModel();
		    Attribute attribute = model.getAttribute(name);
		    if (attribute instanceof Variable) {
			match = true;
			((Variable)attribute).setExpression(value);
			// Force evaluation so that listeners are notified.
			((Variable)attribute).getToken();
		    }
                    if (model instanceof CompositeActor) {
                        Director director
                                = ((CompositeActor)model).getDirector();
		        if (director != null) {
                            attribute = director.getAttribute(name);
                            if (attribute instanceof Variable) {
                                match = true;
                                ((Variable)attribute).setExpression(value);
                                // Force evaluation so that listeners
                                // are notified.
                                ((Variable)attribute).getToken();
                            }
			}
		    }
		}
            }
            if (!match) {
                // Unrecognized option.
                throw new IllegalActionException("Unrecognized option: "
                        + "-" + name);
            }
        }
    }

    /** Return a string summarizing the command-line arguments.
     *  @return A usage string.
     */
    protected String _usage() {
        String result = "Usage: " + _commandTemplate + "\n\n"
            + "Options that take values:\n";

        int i;
        for(i = 0; i < _commandOptions.length; i++) {
            result += " " + _commandOptions[i][0] +
                " " + _commandOptions[i][1] + "\n";
        }
        result += "\nBoolean flags:\n";
        for(i = 0; i < _commandFlags.length; i++) {
            result += " " + _commandFlags[i];
        }
        return result;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected variables                    ////

    /** The command-line options that are either present or not. */
    protected String _commandFlags[] = {
        "-help",
        "-test",
        "-version",
    };

    /** The command-line options that take arguments. */
    protected String _commandOptions[][] = {
        {"-class",  "<classname>"},
        {"-<parameter name>", "<parameter value>"},
    };

    /** The form of the command line. */
    protected String _commandTemplate = "ptolemy [ options ] [file ...]";

    /** If true, then auto exit after a few seconds. */
    protected static boolean _test = false;

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // Flag indicating that the previous argument was -class.
    private boolean _expectingClass = false;

    // List of parameter names seen on the command line.
    private List _parameterNames = new LinkedList();

    // List of parameter values seen on the command line.
    private List _parameterValues = new LinkedList();
}