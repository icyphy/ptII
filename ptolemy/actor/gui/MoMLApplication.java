/* An application that reads one or more files specified on the command line.

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
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

// Ptolemy imports
import ptolemy.actor.Director;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.CompositeActor;
import ptolemy.data.expr.Variable;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;

//////////////////////////////////////////////////////////////////////////
//// MoMLApplication
/**
This is an application that reads one or more
files specified on the command line, or instantiates one or
more Java classes specified by the -class option.
If one of these files is an XML file that defines a Configuration, or one
of the classes is an instance of Configuration, then
all subsequent files will be read by delegating to the Configuration,
invoking its openModel() method.  A command-line file is assumed to be
a MoML file or a file that can be openened by the specified configuration.
<p>
If a Ptolemy model is instantiated on the command line, either
by giving a MoML file or a -class argument, then parameters of that
model can be set on the command line.  The syntax is:
<pre>
    ptolemy <i>modelfile.xml</i> -<i>paramname</i> "<i>value</i>"
</pre>
where <i>paramname</i> is the name of a parameter at the top level
of a model or in the director of a model.
<p>
Derived classes may provide default configurations. In particular, the
protected method _createDefaultConfiguration() is called before any
arguments are processed to provide a default configuration for those
command-line command-line arguments.  In this base class,
that method returns null, so no default configuration is provided.
<p>
If no arguments are given at all, then a default configuration is instead
obtained by calling the protected method _createEmptyConfiguration().
In this base class, that method also returns null,
so calling this with no arguments will not be very useful.
No configuration will be created and no models will be opened.
Derived classes can specify a configuration that opens some
welcome window, or a blank editor.

@author Edward A. Lee and Steve Neuendorffer
@version $Id$
@see Configuration
*/
public class MoMLApplication {

    /** Parse the specified command-line arguments, instanting classes
     *  and reading files that are specified.
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public MoMLApplication(String args[]) throws Exception {
	super();

        _parseArgs(args);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new instance of this application, passing it the
     *  command-line arguments.
     *  @param args The command-line arguments.
     */
    public static void main(String args[]) {
	try {
	    new MoMLApplication(args);
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

    /** Given the name of a file or a URL, convert it to a URL.
     *  This first attempts to do that directly by invoking a URL constructor.
     *  If that fails, then it tries to interpret the spec as a file name
     *  on the local file system.  If that fails, then it tries to interpret
     *  the spec as a resource accessible to the classloader, which uses
     *  the classpath to find the resource.  If that fails, then it throws
     *  an exception.  The specification can give a file name relative to
     *  current working directory, or the directory in which this application
     *  is started up.
     *  @param spec The specification.
     *  @exception IOException If it cannot convert the specification to
     *   a URL.
     */
    public static URL specToURL(String spec) throws IOException {
        try {
            // First argument is null because we are only
            // processing absolute URLs this way.  Relative
            // URLs are opened as ordinary files.
            return new URL(null, spec);
        } catch (MalformedURLException ex) {
            try {
                File file = new File(spec);
                if(!file.exists()) {
                    throw new MalformedURLException();
                }
                return file.getCanonicalFile().toURL();
            } catch (MalformedURLException ex2) {
                try {
                    // Try one last thing, using the classpath.
                    // Need a class context, and this is a static method, so...
                    // NOTE: There doesn't seem to be any way to convert
                    // this a cononical name, so if a model is opened this
                    // way, and then later opened as a file, the model
                    // directory will think it has two different files.
                    Class refClass = Class.forName(
                            "ptolemy.kernel.util.NamedObj");
                    URL inurl = refClass.getClassLoader().getResource(spec);
                    if (inurl == null) {
                        throw new Exception();
                    } else {
                        return inurl;
                    }
                } catch (Exception exception) {
                    throw new IOException("File not found: " + spec);
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Return a default Configuration, or null to do without one.
     *  This configuration will be created before any command-line arguments
     *  are processed.  If there are no command-line arguments, then
     *  the default configuration is given by _createEmptyConfiguration()
     *  instead.
     *  @return null
     *  @exception Exception Thrown in derived classes if the default
     *   configuration cannot be opened.
     */
    protected Configuration _createDefaultConfiguration() throws Exception {
        return null;
    }

    /** Return a default Configuration to use when there are no command-line
     *  arguments, or null to do without one.  This base class returns the
     *  configuration returned by _createDefaultConfiguration().
     *  @return null
     *  @exception Exception Thrown in derived classes if the empty
     *   configuration cannot be opened.
     */
    protected Configuration _createEmptyConfiguration() throws Exception {
        return _createDefaultConfiguration();
    }

    /** Parse a command-line argument.
     *  @return True if the argument is understood, false otherwise.
     *  @exception Exception If something goes wrong.
     */
    protected boolean _parseArg(String arg) throws Exception {
        if (arg.equals("-class")) {
            _expectingClass = true;
        } else if (arg.equals("-help")) {
            System.out.println(_usage());
            // NOTE: This means the test suites cannot test -help
            System.exit(0);
        } else if (arg.equals("-test")) {
            _test = true;
        } else if (arg.equals("-version")) {
            System.out.println("Version 1.0, Build $Id$");
            // NOTE: This means the test suites cannot test -help
            System.exit(0);
        } else if (arg.equals("")) {
            // Ignore blank argument.
        } else {
            if (_expectingClass) {
                _expectingClass = false;

                // Create the class.
                Class newClass = Class.forName(arg);

                // Instantiate the specified class in a new workspace.
                Workspace workspace = new Workspace();

                // Get the constructor that takes a Workspace argument.
                Class[] argTypes = new Class[1];
                argTypes[0] = workspace.getClass();
                Constructor constructor = newClass.getConstructor(argTypes);

                Object args[] = new Object[1];
                args[0] = workspace;
                NamedObj newModel = (NamedObj)constructor.newInstance(args);

                // If there is a configuration, then create an effigy
                // for the class, and enter it in the directory.
                if (_config != null) {

                    // Create an effigy for the model.
                    PtolemyEffigy effigy
                            = new PtolemyEffigy(_config.workspace());
                    effigy.setModel(newModel);

                    ModelDirectory directory
                             = (ModelDirectory)_config.getEntity("directory");

                    effigy.setName(arg);
                    if (directory != null) {
                        if (directory.getEntity(arg) != null) {
                            // Name is already taken.
                            int count = 2;
                            String newName = arg + " " + count;
                            while (directory.getEntity(newName) != null) {
                                count++;
                            }
                            effigy.setName(newName);
                        }
                    }
                    effigy.setContainer(directory);
                }
	    } else {
                if (!arg.startsWith("-")) {
                    // Assume the argument is a file name or URL.
                    // Attempt to read it.
                    URL inurl = specToURL(arg);

                    // Strangely, the XmlParser does not want as base the
                    // directory containing the file, but rather the
                    // file itself.
                    URL base = inurl;

                    // If a configuration has been found, then
                    // defer to it to read the model.  Otherwise,
                    // assume the file is an XML file.
                    if (_config != null) {
                        ModelDirectory directory =
                                (ModelDirectory)_config.getEntity("directory");
                        if (directory == null) {
                            throw new InternalErrorException(
                                     "No model directory!");
                        }

                        String key = inurl.toExternalForm();

                        // Now defer to the model reader.
                        _config.openModel(base, inurl, key);
                    } else {
                        // No configuration has been encountered.
                        // Assume this is a MoML file, and open it.
                        MoMLParser parser = new MoMLParser(
                                new Workspace(), null);
                        NamedObj toplevel = parser.parse(
                                base, inurl.openStream());
                        if (toplevel instanceof Configuration) {
                            _config = (Configuration)toplevel;
                        }
                    }
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
        if (args.length > 0) {
            _config = _createDefaultConfiguration();
        } else {
            _config = _createEmptyConfiguration();
	}
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (_parseArg(arg) == false) {
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
        // Check saved options to see whether any is setting an attribute.
        Iterator names = _parameterNames.iterator();
        Iterator values = _parameterValues.iterator();
        while (names.hasNext() && values.hasNext()) {
            String name = (String)names.next();
            String value = (String)values.next();

            boolean match = false;
            ModelDirectory directory =
		(ModelDirectory)_config.getEntity("directory");
            if (directory == null) {
                throw new InternalErrorException("No model directory!");
            }
            Iterator proxies
                    = directory.entityList(Effigy.class).iterator();
            while(proxies.hasNext()) {
		Effigy effigy = (Effigy)proxies.next();
		if(effigy instanceof PtolemyEffigy) {
		    NamedObj model = ((PtolemyEffigy)effigy).getModel();
		    Attribute attribute = model.getAttribute(name);
		    if (attribute instanceof Settable) {
			match = true;
			((Settable)attribute).setExpression(value);
                        if (attribute instanceof Variable) {
                            // Force evaluation so that listeners are notified.
                            ((Variable)attribute).getToken();
                        }
		    }
                    if (model instanceof CompositeActor) {
                        Director director
                                = ((CompositeActor)model).getDirector();
		        if (director != null) {
                            attribute = director.getAttribute(name);
                            if (attribute instanceof Settable) {
                                match = true;
                                ((Settable)attribute).setExpression(value);
                                if (attribute instanceof Variable) {
                                    // Force evaluation so that listeners
                                    // are notified.
                                    ((Variable)attribute).getToken();
                                }
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
    protected String _commandTemplate = "moml [ options ] [file ...]";

    // The configuration model of this application.
    protected Configuration _config;

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
