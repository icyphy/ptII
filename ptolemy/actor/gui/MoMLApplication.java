/* An application that reads one or more files specified on the command line.

 Copyright (c) 1999-2003 The Regents of the University of California.
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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.UIManager;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.data.expr.Variable;
import ptolemy.gui.GraphicalMessageHandler;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.attributes.VersionAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.Documentation;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;


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
a MoML file or a file that can be opened by the specified configuration.
<p>
If a Ptolemy model is instantiated on the command line, either
by giving a MoML file or a -class argument, then parameters of that
model can be set on the command line.  The syntax is:
<pre>
    ptolemy <i>modelFile.xml</i> -<i>parameterName</i> "<i>value</i>"
</pre>
where <i>parameterName</i> is the name of a parameter relative to the top level
of a model or the director of a model.  For instance, if foo.xml defines
a toplevel entity named "x" and x contains an entity named "y" and a parameter
named "a", and y contains a parameter named "b", then:
<pre>
    ptolemy foo.xml -a 5 -y.b 10
</pre>
would set the values of the two parameters.
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

@author Edward A. Lee and Steve Neuendorffer, Contributor: Christopher Hylands
@version $Id$
@since Ptolemy II 0.4
@see Configuration
*/
public class MoMLApplication {


    /** Parse the specified command-line arguments, instanting classes
     *  and reading files that are specified.
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public MoMLApplication(String args[]) throws Exception {
        this("ptolemy/configs", args);
    }

    /** Parse the specified command-line arguments, instanting classes
     *  and reading files that are specified.
     *  @param basePath The basePath to look for configurations
     *  in, usually "ptolemy/configs", but other tools might
     *  have other configurations in other directories
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public MoMLApplication(String basePath, String args[]) throws Exception {
        
        _basePath = basePath;

        // The Java look & feel is pretty lame, so we use the native
        // look and feel of the platform we are running on.
        // NOTE: This creates the only dependence on Swing in this
        // class.  Should this be left to derived classes?
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Ignore exceptions, which only result in the wrong look and feel.
        }

        // Create a parser to use.
        _parser = new MoMLParser();

        // We set the list of MoMLFilters to handle Backward Compatibility.
        _parser.setMoMLFilters(BackwardCompatibility.allFilters());

        // 2/03: Moved the setMessageHandler() to before parseArgs() so
        // that if we get an error in parseArgs() we will get a graphical
        // stack trace.   Such an error could be caused by specifying a model
        // as a command line argument and the model has an invalid parameter.

        MessageHandler.setMessageHandler(new GraphicalMessageHandler());

        _parseArgs(args);

        // Even if the user is set up for foreign locale, use the US locale.
        // This is because certain parts of Ptolemy (like the expression
        // language) are not localized.
        // FIXME: This is a workaround for the locale problem, not a fix.
        // FIXME: In March, 2001, Johan Ecker writes
        // Ptolemy gave tons of exception when started on my laptop
        // which has Swedish settings as default. The Swedish standard
        // for floating points are "2,3", i.e. using a comma as
        // delimiter. However, I think most Swedes are adaptable and
        // do not mind using a dot instead since this more or less has
        // become the world standard, at least in engineering. The
        // problem is that I needed to change my global settings to
        // start Ptolemy and this is quite annoying. I guess that the
        // expression parser should just ignore the delimiter settings
        // on the local computer and always use dot, otherwise Ptolemy
        // will crash using its own init files.
        try {
            java.util.Locale.setDefault(java.util.Locale.US);
        } catch (java.security.AccessControlException accessControl) {
            // FIXME: If the application is run under Web Start, then this
            // exception will be thrown.
        }
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
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            System.exit(0);
        }
    }

    /** Given the name of a file or a URL, convert it to a URL.
     *  This first attempts to do that directly by invoking a URL constructor.
     *  If that fails, then it tries to interpret the spec as a file name
     *  on the local file system.  If that fails, then it tries to interpret
     *  the spec as a resource accessible to the class loader, which uses
     *  the classpath to find the resource.  If that fails, then it throws
     *  an exception.  The specification can give a file name relative to
     *  current working directory, or the directory in which this application
     *  is started up.
     *  @param spec The specification.
     *  @exception IOException If it cannot convert the specification to
     *   a URL.
     */
    public static URL specToURL(String spec) throws IOException {
        // FIXME: There is a bit of a design flaw here because
        // we open a stream to the url (which is probably expensive)
        // and then close it.  The reason for opening the stream
        // is that we want to be sure that the URL is valid,
        // and if it is not, we check the local file system
        // and the classpath.

        // One solution would be to have a method that returned a
        // URLConnection because we can open a stream with a
        // URLConnection and still get the original URL if necessary
        URL specURL = null;
        try {
            // First argument is null because we are only
            // processing absolute URLs this way.  Relative
            // URLs are opened as ordinary files.
            specURL = new URL(null, spec);

            // Make sure that the specURL actually exists
            InputStream urlStream = specURL.openStream();
            urlStream.close();
            return specURL;
        } catch (Exception ex) {
            try {
                // Try as a regular file
                File file = new File(spec);

                // Oddly, under Windows file.exists() might return even
                // though the file does not exist if we changed user.dir.
                // See
                // http://forum.java.sun.com/thread.jsp?forum=31&thread=328939
                // One hack is to convert to an absolute path first
                File absoluteFile = file.getAbsoluteFile();
                try {
                    if (!absoluteFile.exists()) {
                        throw new IOException("File '" + absoluteFile
                                + "' does not exist." );
                    }
                } catch (java.security.AccessControlException accessControl) {
                    IOException exception =
                        new IOException("AccessControlException while "
                                + "trying to read '" + absoluteFile + "'");
                    // IOException does not have a cause argument constructor.
                    exception.initCause(accessControl);
                    throw exception;
                }
                specURL = absoluteFile.getCanonicalFile().toURL();

                //InputStream urlStream = specURL.openStream();
                //urlStream.close();

                return specURL;
            } catch (Exception ex2) {
                try {
                    // Try one last thing, using the classpath.
                    // Need a class context, and this is a static method, so...
                    // we can't use this.getClass().getClassLoader()

                    // NOTE: There doesn't seem to be any way to convert
                    // this a canonical name, so if a model is opened this
                    // way, and then later opened as a file, the model
                    // directory will think it has two different files.

                    //Class refClass = Class.forName(
                    //        "ptolemy.kernel.util.NamedObj");
                    //specURL = refClass.getClassLoader().getResource(spec);

                    // This works in Web Start, see
                    // http://java.sun.com/products/javawebstart/faq.html#54
  
                    specURL = Thread.currentThread()
                        .getContextClassLoader().getResource(spec);
                    
                    if (specURL == null) {
                        throw new Exception("getResource(\"" + spec
                                + "\") returned null.");
                    } else {
                        // If we have a jar URL, convert spaces to %20
                        // so as to avoid multiple windows with the
                        // same file.  Web Start needs this if the Web
                        // Start cache is in a directory that has
                        // spaces in the path, which is the default
                        // under Windows.
                        specURL = JNLPUtilities.canonicalizeJarURL(specURL);
                        // Verify that it can be opened
                        InputStream urlStream = specURL.openStream();
                        urlStream.close();
                        return specURL;
                    }
                } catch (Exception ex3) {
                    // Use a very verbose message in case opening
                    // the configuration fails under Web Start.
                    // Without this error message, users will
                    // have no hope of telling us why Web Start failed.
                    IOException exception =
                        new IOException("File not found: '" + spec
                                + "'\n caused by:\n" + ex
                                + "\n AND:\n"
                                + ex2
                                + "\n AND:\n"
                                + ex3);
                    // IOException does not have a cause argument
                    exception.initCause(ex3);
                    throw exception;
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a string summarizing the command-line arguments,
     *  including an configuration directories in a base path,
     *  typically "ptolemy/configs".
     *  Some subclasses of this class use configuations from ptolemy/configs.
     *  For example, if ptolemy/configs/full/configuration.xml exists
     *  then -full is a legitmate argument.
     *  @param commandTemplate The form of the command line
     *  @param commandOptions Command-line options that take arguments.
     *  @param commandFlags Command-line options that are either present
     *  or not.
     *  @return A usage string.
     *  @see ptolemy.util.StringUtilities#usageString(String, String [][], String [])
     */
    protected String _configurationUsage(String commandTemplate,
            String [][] commandOptions,
            String [] commandFlags) {
        String result = "Usage: " + commandTemplate + "\n\n"
            + "Options that take values:\n";
        int i;
        // Print any command options from this class first
        for (i = 0; i < _commandOptions.length; i++) {
            result += " " + _commandOptions[i][0] +
                " " + _commandOptions[i][1] + "\n";
        }

        for (i = 0; i < commandOptions.length; i++) {
            result += " " + commandOptions[i][0] +
                " " + commandOptions[i][1] + "\n";
        }

        result += "\nBoolean flags:\n";
        // Print any command flags from this class first
        for (i = 0; i < _commandFlags.length; i++) {
            result += " " + _commandFlags[i];
        }

        for (i = 0; i < commandFlags.length; i++) {
            result += " " + commandFlags[i];
        }

        try {
            // Look for configuration directories in _basePath
            // This will likely fail if ptolemy/configs is in a jar file
            // We use a URI here so that we cause call File(URI).
            URI configurationURI =
                new URI(specToURL(_basePath).toExternalForm());
            File configurationDirectory = new File(configurationURI);
            ConfigurationFilenameFilter filter =
                new ConfigurationFilenameFilter();
            File [] configurationDirectories =
                configurationDirectory.listFiles(filter);
            if (configurationDirectories != null) {
                result += "\nThe following Boolean flags start up using "
                    + "different configurations:\n";
                for (i = 0; i < configurationDirectories.length; i++) {
                    result += " -" + configurationDirectories[i].getName();
                    String configurationFileName = configurationDirectories[i]
                        + File.separator
                        + "configuration.xml";
                    boolean printDefaultConfigurationMessage = true;
                    try {
                        // Read the configuration and get the top level docs

                        // FIXME: this will not work if the configs are
                        // in jar files.

                        // FIXME: Skip jxta, since it starts up the jxta config
                        // tools.
                        if (!configurationDirectories[i]
                                .getName().equals("jxta")) {
                            URL specificationURL =
                                specToURL(configurationFileName);
                            Configuration configuration =
                                _readConfiguration(specificationURL);
                            if (configuration != null
                                    && configuration.getAttribute("_doc")
                                    != null
                                    && configuration.getAttribute("_doc")
                                    instanceof Documentation
                                ) {
                                Documentation doc =
                                    (Documentation)configuration
                                    .getAttribute("_doc");
                                result += "\t" + doc.getValue() + "\n";
                                printDefaultConfigurationMessage = false;
                            }
                        }
                    } catch (Exception ex) {
                        //result += "\tCould not read configuration"
                        //    + "\n" + ex;
                        //ex.printStackTrace();
                    }
                    if (printDefaultConfigurationMessage) {
                        result += "\tuses "
                            + configurationFileName + "\n";
                    }
                }
            }
        } catch (Exception ex) {
            result += "Warning: Failed to find configuration(s) in '"
                + _basePath + "'" + ex;
        }
        return result;
    }

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
            System.out.println("Version "
                    + VersionAttribute.CURRENT_VERSION
                    + ", Build $Id$");
            // NOTE: This means the test suites cannot test -version
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
                    URL inURL = specToURL(arg);

                    // Strangely, the XmlParser does not want as base the
                    // directory containing the file, but rather the
                    // file itself.
                    URL base = inURL;

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

                        String key = inURL.toExternalForm();

                        //long startTime = (new Date()).getTime();

                        // Now defer to the model reader.
                        _config.openModel(base, inURL, key);

                        //System.out.println("Model open done: " +
                        //                   Manager.timeAndMemory(startTime));

                    } else {
                        // No configuration has been encountered.
                        // Assume this is a MoML file, and open it.
                        _parser.reset();

                        try {
                            NamedObj toplevel =
                                _parser.parse(base, inURL);
                            if (toplevel instanceof Configuration) {
                                _config = (Configuration)toplevel;
                            }
                        } catch (Exception ex) {
                            // Unfortunately, java.util.zip.ZipException
                            // does not include the file name.

                            // If inURL is a jarURL check for %20
                            String detailMessage = "";
                            try {
                                if (inURL.toString().indexOf("!/") != -1
                                        && inURL.toString().indexOf("%20") != -1) {
                                    detailMessage = " The URL contains "
                                        + "'!/', so it may be a jar "
                                        + "URL, and jar URLs cannot contain "
                                        + "%20. This might happen if the "
                                        + "pathname to the jnlp file had a "
                                        + "space in it";
                                }
                            } catch (Exception ex2) {
                                // Ignored
                            }
                            throw new Exception("Failed to parse '" + inURL
                                    + "'" + detailMessage , ex);
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
                if (arg.trim().startsWith("-")) {
                    if (i >= args.length - 1) {
                        throw new IllegalActionException("Cannot set " +
                                "parameter " + arg + " when no value is " +
                                "given.");
                    }
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
            while (proxies.hasNext()) {
                Effigy effigy = (Effigy)proxies.next();
                if (effigy instanceof PtolemyEffigy) {
                    NamedObj model = ((PtolemyEffigy)effigy).getModel();
                    System.out.println("model = " + model.getFullName());
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
                throw new IllegalActionException("Unrecognized option: " +
                        "No parameter exists with name " + name);
            }
        }
        // If the default configuration contains any Tableaux,
        // then we show them now.  This is deferred until now because
        // how they are shown may depend on command-line arguments
        // and/or parameters in some MoML file that is read.
        if (_config == null) {
            throw new IllegalActionException("No configuration provided.");
        }
        _config.showAll();
    }

    /** Read a Configuration from the URL given by the specified string.
     *  The URL may absolute, or relative to the Ptolemy II tree root,
     *  or in the classpath.  To convert a String to a URL suitable for
     *  use by this method, call specToURL(String).
     *  @param specificationURL A string describing a URL.
     *  @return A configuration.
     *  @exception Exception If the configuration cannot be opened, or
     *   if the contents of the URL is not a configuration.
     */
    protected Configuration _readConfiguration(URL specificationURL)
            throws Exception {
        _parser.reset();

        Configuration toplevel =
            (Configuration) _parser.parse(specificationURL, specificationURL);

        // If the toplevel model is a configuration containing a directory,
        // then create an effigy for the configuration itself, and put it
        // in the directory.
        ComponentEntity directory
            = ((Configuration)toplevel).getEntity("directory");
        if (directory instanceof ModelDirectory) {
            PtolemyEffigy effigy
                = new PtolemyEffigy(
                        (ModelDirectory)directory, toplevel.getName());
            effigy.setModel(toplevel);
            effigy.identifier.setExpression(specificationURL.toExternalForm());
        }
        return toplevel;
    }

    /** Return a string summarizing the command-line arguments.
     *  @return A usage string.
     */
    protected String _usage() {
        // Call the static method that generates the usage strings.
        return StringUtilities.usageString(_commandTemplate,
                _commandOptions, _commandFlags);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The base path of the configuration directory, usually
     *  "ptolemy/configs" for Ptolemy II, but subclasses might
     *  have configurations in a different directory
     */
    protected String _basePath = "ptolemy/configs";


    /** The command-line options that are either present or not. */
    protected String _commandFlags[] = {
        "-help",
        "-test",
        "-version",
    };

    /** The command-line options that take arguments. */
    protected  String _commandOptions[][] = {
        {"-class",  "<classname>"},
        {"-<parameter name>", "<parameter value>"},
    };

    /** The form of the command line. */
    protected String _commandTemplate = "moml [ options ] [file ...]";

    // The configuration model of this application.
    protected Configuration _config;

    // The parser used to construct the configuration.
    protected MoMLParser _parser;

    /** If true, then auto exit after a few seconds. */
    protected static boolean _test = false;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Look for directories that contain files named configuration.xml
     *  and intro.htm.
     */
    class ConfigurationFilenameFilter implements FilenameFilter {

        /** Return true if the specified file names a directory
         *  that contains a file named configuration.xml
         *  and a file named intro.htm
         *  @param directory the directory in which the potential
         *  directory was found.
         *  @param name the name of the directory or file.
         *  @return true if the file is a directory that
         *  contains a file called configuration.xml
         */
        public boolean accept(File directory, String name ) {
            try {
                File configurationDirectory = new File(directory, name);
                if (!configurationDirectory.isDirectory()) {
                    return false;
                }
                File configurationFile = new File(configurationDirectory,
                        "configuration.xml");
                File introFile = new File(configurationDirectory,
                        "intro.htm");
                if (configurationFile.isFile()
                        && introFile.isFile()) {
                    return true;
                }
            } catch (Exception ex) {
                return false;
            }
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Flag indicating that the previous argument was -class.
    private boolean _expectingClass = false;

    // List of parameter names seen on the command line.
    private List _parameterNames = new LinkedList();

    // List of parameter values seen on the command line.
    private List _parameterValues = new LinkedList();
}
