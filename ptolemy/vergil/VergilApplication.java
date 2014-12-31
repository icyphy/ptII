/* An application for editing ptolemy models visually.

 Copyright (c) 1999-2014 The Regents of the University of California.
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
package ptolemy.vergil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;

import ptolemy.actor.gui.ActorGraphicalMessageHandler;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.EffigyFactory;
import ptolemy.actor.gui.MoMLApplication;
import ptolemy.actor.gui.ModelDirectory;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.PtolemyPreferences;
import ptolemy.actor.gui.UserActorLibrary;
import ptolemy.data.expr.Parameter;
import ptolemy.gui.PtGUIUtilities;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.RemoveNonPtinyClasses;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// VergilApplication

/**
 An application for editing ptolemy models visually.

 <p>
 The exact facilities that are available are determined by an optional
 command line argument that names a directory in ptolemy/configs that
 contains a configuration.xml file.  For example, if we call vergil
 -ptiny, then we will use ptolemy/configs/ptiny/configuration.xml and
 ptolemy/configs/ptiny/intro.htm.  The default configuration is
 ptolemy/configs/full/configuration.xml, which is loaded before any
 other command-line arguments are processed.</p>

 <p>This application also takes an optional command line argument pair
 <code>-configuration <i>configurationFile.xml</i></code> that names a configuration
 to be read.  For example,
 <pre>
 $PTII/bin/vergil -configuration ptolemy/configs/ptiny/configuration.xml
 </pre>
 and
 <pre>
 $PTII/bin/vergil -ptiny
 </pre>
 are equivalent
 <p>
 If there are no command-line arguments at all, then the configuration
 file is augmented by the MoML file ptolemy/configs/full/welcomeWindow.xml</p>

 <p>Note that if the configuration starts with "ptiny", then
 {@link ptolemy.moml.filter.RemoveNonPtinyClasses} is used to remove classes
 such as code generators that might be present in the model but are not part
 of the Ptiny configuration.</p>

 @author Edward A. Lee, Steve Neuendorffer, Christopher Hylands, contributor: Chad Berkeley
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 @see ptolemy.actor.gui.ModelFrame
 @see ptolemy.actor.gui.RunTableau
 @see ptolemy.actor.gui.PtExecuteApplication
 */
public class VergilApplication extends MoMLApplication {
    /** Parse the specified command-line arguments, creating models
     *  and frames to interact with them.
     *  Look for configurations in "ptolemy/configs"
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public VergilApplication(String[] args) throws Exception {
        // FindBugs: FIXME: Note that MoMLApplication(String, String[]) starts a
        // thread, which used to mean that the error handler will not be registered before
        // the thread starts.
        this("ptolemy/configs", args);
    }

    /** Parse the specified command-line arguments, creating models
     *  and frames to interact with them.
     *  @param basePath The basePath to look for configurations
     *  in, usually "ptolemy/configs", but other tools might
     *  have other configurations in other directories
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public VergilApplication(String basePath, String[] args) throws Exception {
        // FindBugs: FIXME: Note that MoMLApplication(String,
        // String[]) starts a thread, which used to mean that the
        // message handler and error handler were not registered
        // before the thread starts.
        super("ptolemy/configs", args, new VergilGraphicalMessageHandler(),
                new VergilErrorHandler());
        try {
            // Avoid (cd $PTII/doc/test/junit; make) throwing an exception under Java 1.8
            // under Mac OS X and Linux.  Also, the colors are wrong under Mac OS X 10.9.
            // Another way to get this: "occurred just building a simple SDF model... Open a few libraries and perform a search. "
            // The exception is:
            //    java.awt.color.CMMException: LCMS error 13: Couldn't link the profiles
            //         at sun.java2d.cmm.lcms.LCMS.createNativeTransform(Native Method)
            //         at sun.java2d.cmm.lcms.LCMS.createTransform(LCMS.java:156)
            //         at sun.java2d.cmm.lcms.LCMSTransform.doTransform(LCMSTransform.java:155)
            //         at sun.java2d.cmm.lcms.LCMSTransform.colorConvert(LCMSTransform.java:629)
            //         at java.awt.color.ICC_ColorSpace.toRGB(ICC_ColorSpace.java:182)
            //         at com.sun.pdfview.colorspace.PDFColorSpace.getPaint(PDFColorSpace.java:222)
            //         at com.sun.pdfview.PDFParser.iterate(PDFParser.java:656)
            //         at com.sun.pdfview.BaseWatchable.run(BaseWatchable.java:101)
            //         at java.lang.Thread.run(Thread.java:745)
            // See http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/PDF-renderer
            // and https://stackoverflow.com/questions/26535842/multithreaded-jpeg-image-processing-in-java

            // This code was in ptolemy/vergil/pdfrenderer/PDFIcon.java, but the problem persisted
            // so we are probably invoking ICC_ColorSpace via ImageIO somewhere.
            Class.forName("javax.imageio.ImageIO");
            Class.forName("java.awt.color.ICC_ColorSpace");

            // This class is not present under Linux?
            try {
                Class.forName("sun.java2d.cmm.lcms.LCMS");
            } catch (Throwable throwable) {
                // Ignore.
            }
        } catch (Throwable throwable) {
            new IllegalActionException(null, throwable,
                    "Could not instantiate a Java2d class?"
                    + "See http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/PDF-renderer").printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Print out an error message and stack trace on stderr and then
     * display a dialog box.  This method is used as a fail safe
     * in case there are problems with the configuration
     * We use a Throwable here instead of an Exception because
     * we might get an Error or and Exception. For example, if we
     * are using JNI, then we might get a java.lang.UnsatisfiedLinkError,
     * which is an Error, not and Exception.
     * @param message  The message to be displayed
     * @param args The arguments  to be displayed
     * @param throwable The Throwable that caused the problem.
     */
    public static void errorAndExit(String message, String[] args,
            Throwable throwable) {
        // This is public so that other application frameworks may
        // invoke it
        StringBuffer argsBuffer = new StringBuffer("Command failed");

        if (args.length > 0) {
            argsBuffer.append("\nArguments: " + args[0]);

            for (int i = 1; i < args.length; i++) {
                argsBuffer.append(" " + args[i]);
            }

            argsBuffer.append("\n");
        }

        // First, print out the stack trace so that
        // if the next step fails the user has
        // a chance of seeing the message.
        System.out.println(argsBuffer.toString());
        throwable.printStackTrace();

        // Display the error message in a stack trace
        // If there are problems with the configuration,
        // then there is a chance that we have not
        // registered the GraphicalMessageHandler yet
        // so we do so now so that we are sure
        // the user can see the message.
        // One way to test this is to run vergil -configuration foo
        MessageHandler.setMessageHandler(new ActorGraphicalMessageHandler());

        MessageHandler.error(argsBuffer.toString(), throwable);

        try {
            StringUtilities.exit(0);
        } catch (SecurityException ex) {
            System.out.println("Warning: Failed to call System.exit()."
                    + "(-sandbox always causes this)");
            System.out.println("About to clean configurations");
            Iterator configurations = Configuration.configurations().iterator();
            while (configurations.hasNext()) {
                Configuration configuration = (Configuration) configurations
                        .next();
                try {
                    System.out.println("Setting container of "
                            + configuration.getFullName() + " to null");
                    configuration.setContainer(null);
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }

        }
    }

    /** Create a new instance of this application, passing it the
     *  command-line arguments.
     *  @param args The command-line arguments.
     */
    public static void main(final String[] args) {
        if (PtGUIUtilities.macOSLookAndFeel()) {
            String aboutNameProperty = "com.apple.mrj.application.apple.menu.about.name";
            try {
                // Mac OS X: Set the "About" menu name.  These calls must be made outside
                // the Swing Event Thread.
                System.setProperty(aboutNameProperty, "Vergil");
            } catch (SecurityException ex) {
                if (!_printedSecurityExceptionMessage) {
                    _printedSecurityExceptionMessage = true;
                    System.out
                    .println("Warning: Mac OS X: Failed to set the \""
                            + aboutNameProperty
                            + "\" property. "
                            + "(applets and -sandbox always causes this)");
                }
            }
            // Uncomment the next line to use the screen menu bar instead of a per-window
            // menu bar.
            //System.setProperty("apple.laf.useScreenMenuBar", "true");
        }

        // FIXME: Java superstition dictates that if you want something
        // to work, you should invoke it in event thread.  Otherwise,
        // weird things happens at the user interface level.  This
        // seems to prevent occasional errors rending HTML under Web Start.
        try {
            // NOTE: This is unfortunate... It would be nice
            // if this could be run inside a PtolemyThread, since
            // getting read access the workspace is much more efficient
            // in PtolemyThread.
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        new VergilApplication(args);
                    } catch (Throwable throwable) {
                        // If we get an Error or and Exception while
                        // configuring, we will end up here.
                        errorAndExit("Command failed", args, throwable);
                    }
                }
            });
        } catch (Throwable throwable2) {
            // We are not likely to get here, but just to be safe
            // we try to print the error message and display it in a
            // graphical widget.
            errorAndExit("Command failed", args, throwable2);
        }

        // If the -test arg was set, then exit after 2 seconds.
        if (_test) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }

            StringUtilities.exit(0);
        }
    }

    /**
     *  Open the MoML file at the given location as a new library in the
     *  actor library for this application.
     *
     *  An alternate class can be used to build the library if reading the
     *  MoML is not desired.  The class must extend ptolemy.moml.LibraryBuilder
     *  and the _alternateLibraryBuilder property must be set with the 'value'
     *  set to the class that extends LibraryBuilder.
     *
     *  @param configuration The configuration where we look for the
     *  actor library.
     *  @param file The MoML file to open.
     *  @exception Exception If there is a problem opening the configuration,
     *  opening the MoML file, or opening the MoML file as a new library.
     *  @deprecated Use {@link ptolemy.actor.gui.UserActorLibrary#openLibrary(Configuration, File)}
     */
    @Deprecated
    public static void openLibrary(Configuration configuration, File file)
            throws Exception {
        MoMLParser.setErrorHandler(new VergilErrorHandler());
        UserActorLibrary.openLibrary(configuration, file);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a default Configuration.  The initial default configuration
     *  is the MoML file full/configuration.xml under the _basePath
     *  directory, which is usually ptolemy/configs.
     *  using different command line arguments can change the value
     *  Usually, we also open the user library, which is located
     *  in the directory returned by
     *  {@link ptolemy.util.StringUtilities#preferencesDirectory()}
     *  If the configuration contains a top level Parameter named
     *  _hideUserLibrary, then we do not open the user library.
     *
     *  @return A default configuration.
     *  @exception Exception If the configuration cannot be opened.
     */
    @Override
    protected Configuration _createDefaultConfiguration() throws Exception {
        try {
            if (_configurationURL == null) {
                _configurationURL = specToURL(_basePath
                        + "/full/configuration.xml");
            }
        } catch (IOException ex) {
            try {
                // If we ship HyVisual without a full installation, then
                // we try the hyvisual configuration.
                // vergil -help needs this.
                // FIXME: we could do better than this and either
                // search for configurations or go through a list
                // of them.
                _configurationURL = specToURL(_basePath
                        + "/hyvisual/configuration.xml");
            } catch (IOException ex2) {
                // Throw the original exception.
                throw ex;
            }
        }

        // This has the side effects of merging properties from ptII.properties
        Configuration configuration = super._createDefaultConfiguration();

        try {
            configuration = readConfiguration(_configurationURL);
        } catch (Exception ex) {
            throw new Exception("Failed to read configuration '"
                    + _configurationURL + "'", ex);
        }

        // Read the user preferences, if any.
        PtolemyPreferences.setDefaultPreferences(configuration);

        // If _hideUserLibraryAttribute is not present, or is false,
        // call openUserLibrary().  openUserLibrary() will open either the
        // user library or the library named by the _alternateLibraryBuilder.
        Parameter hideUserLibraryAttribute = (Parameter) configuration
                .getAttribute("_hideUserLibrary", Parameter.class);

        if (hideUserLibraryAttribute == null
                || hideUserLibraryAttribute.getExpression().equals("false")) {

            // Load the user library.
            try {
                MoMLParser.setErrorHandler(new VergilErrorHandler());
                UserActorLibrary.openUserLibrary(configuration);
            } catch (Exception ex) {
                MessageHandler.error("Failed to display user library.", ex);
            }
        }

        return configuration;
    }

    /** Return a default Configuration to use when there are no command-line
     *  arguments. If the configuration contains a parameter
     *  _applicationBlankPtolemyEffigyAtStartup
     *  then we create an empty up an empty PtolemyEffigy.
     *  @return A configuration for when there no command-line arguments.
     *  @exception Exception If the configuration cannot be opened.
     */
    @Override
    protected Configuration _createEmptyConfiguration() throws Exception {
        Configuration configuration = _createDefaultConfiguration();
        URL welcomeURL = null;
        URL introURL = null;

        ModelDirectory directory = configuration.getDirectory();

        Parameter applicationBlankPtolemyEffigyAtStartup = (Parameter) configuration
                .getAttribute("_applicationBlankPtolemyEffigyAtStartup",
                        Parameter.class);
        if (applicationBlankPtolemyEffigyAtStartup != null
                && applicationBlankPtolemyEffigyAtStartup.getExpression()
                .equals("true")) {

            EffigyFactory factory = null;

            Parameter startupEffigyFactoryName = (Parameter) applicationBlankPtolemyEffigyAtStartup
                    .getAttribute("_startupEffigyFactoryName");

            // See if there's a startup name
            if (startupEffigyFactoryName != null) {
                String startupName = startupEffigyFactoryName.getExpression();
                EffigyFactory factoryContainer = (EffigyFactory) configuration
                        .getEntity("effigyFactory");

                // Make sure there is effigyFactory
                if (factoryContainer == null) {
                    throw new IllegalActionException(
                            "Could not find effigyFactory.");
                }

                // Search through the effigy factories for the startup name.
                List factoryList = factoryContainer
                        .entityList(EffigyFactory.class);
                Iterator factories = factoryList.iterator();

                while (factories.hasNext()) {
                    final EffigyFactory currentFactory = (EffigyFactory) factories
                            .next();
                    if (currentFactory.getName().equals(startupName)) {
                        factory = currentFactory;
                    }
                }

                // Make sure we found it.
                if (factory == null) {
                    throw new IllegalActionException(
                            "Could not find effigy factory " + startupName);
                }

            } else {
                factory = new PtolemyEffigy.Factory(directory,
                        directory.uniqueName("ptolemyEffigy"));
            }
            Effigy effigy = factory.createEffigy(directory, null, null);
            configuration.createPrimaryTableau(effigy);
        }

        try {
            // First, we see if we can find the welcome window by
            // looking at the default configuration.
            // FIXME: this seems wrong, we should be able to get
            // an attribute from the configuration that names the
            // welcome window.
            String configurationURLString = _configurationURL.toExternalForm();
            String base = configurationURLString.substring(0,
                    configurationURLString.lastIndexOf("/"));

            welcomeURL = specToURL(base + "/welcomeWindow.xml");
            introURL = specToURL(base + "/intro.htm");
            _parser.reset();
            _parser.setContext(configuration);
            _parser.parse(welcomeURL, welcomeURL);
        } catch (Throwable throwable) {
            // OK, that did not work, try a different method.
            if (_configurationSubdirectory == null) {
                _configurationSubdirectory = "full";
            }

            // FIXME: This code is Dog slow for some reason.
            welcomeURL = specToURL(_basePath + "/" + _configurationSubdirectory
                    + "/welcomeWindow.xml");
            introURL = specToURL(_basePath + "/" + _configurationSubdirectory
                    + "/intro.htm");
            _parser.reset();
            _parser.setContext(configuration);
            _parser.parse(welcomeURL, welcomeURL);
        }

        Effigy doc = (Effigy) configuration.getEntity("directory.doc");

        if (doc == null) {
            throw new InternalErrorException(
                    "Configuration does not have a directory.doc entity?");
        }
        doc.identifier.setExpression(introURL.toExternalForm());

        return configuration;
    }

    /** Parse the command-line arguments.
     *  @exception Exception If an argument is not understood or triggers
     *   an error.
     */
    @Override
    protected void _parseArgs(final String[] args) throws Exception {
        _commandTemplate = "vergil [ options ] [file ...]";

        // VergilApplication.super._parseArgs(args)
        // calls _createDefaultConfiguration() asap,
        // so delay calling it until we process
        // the arguments and possibly get a configuration.
        List processedArgsList = new LinkedList();

        for (int i = 0; i < args.length; i++) {
            // Parse any configuration specific args first so that we can
            // set up the configuration for later use by the parent class.
            if (!_configurationParseArg(args[i])) {
                // If we did not parse the arg, the
                // add it to the list of args to be passed
                // to the super class.
                processedArgsList.add(args[i]);
            }
        }

        if (_expectingConfiguration) {
            throw new IllegalActionException("Missing configuration");
        }

        String[] processedArgs = (String[]) processedArgsList
                .toArray(new String[processedArgsList.size()]);

        super._parseArgs(processedArgs);
    }

    /** Return a string summarizing the command-line arguments.
     *  @return A usage string.
     */
    @Override
    protected String _usage() {
        return _configurationUsage(_commandTemplate, _commandOptions,
                new String[] {});
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    // _commandOptions is static because
    // _usage() may be called from the constructor of the parent class,
    //  in which case non-static variables are null?

    /** The command-line options that take arguments. */
    protected static String[][] _commandOptions = { { "-configuration",
    "<configuration URL, defaults to ptolemy/configs/full/configuration.xml>" }, };

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Parse a command-line argument.  Usually, we would name this
     *  method _parseArg(), but we want to handle any arguments that
     *  handle configuration changes before calling the parent class
     *  _parseArg() because the parent class depends either having a
     *  configuration to work with, or the parent class sets up a
     *  configuration.
     *  @return True if the argument is understood, false otherwise.
     *  @exception Exception If something goes wrong.
     */
    private boolean _configurationParseArg(String arg) throws Exception {
        if (arg.startsWith("-conf")) {
            _expectingConfiguration = true;
        } else if (arg.startsWith("-")) {
            // If the argument names a directory in ptolemy/configs
            // that contains a file named configuration.xml that can
            // be found either as a URL or in the classpath, then
            // assume that it is a configuration.  For example, -ptiny
            // will look for ptolemy/configs/ptiny/configuration.xml
            // If the argument does not name a configuration, then
            // we return false so that the argument can be processed
            // by the parent class.
            try {
                _configurationSubdirectory = arg.substring(1);

                String potentialConfiguration = _basePath + "/"
                        + _configurationSubdirectory + "/configuration.xml";

                // This will throw an Exception if we can't find the config.
                _configurationURL = specToURL(potentialConfiguration);
                if (_configurationSubdirectory.startsWith("ptiny")) {
                    // If the configuration startes with ptiny, then
                    // filter out codegenerators
                    LinkedList filters = new LinkedList();
                    filters.add(new RemoveNonPtinyClasses());
                    MoMLParser.addMoMLFilters(filters);
                }
            } catch (Throwable ex) {
                // The argument did not name a configuration, let the parent
                // class have a shot.
                return false;
            }
        } else if (_expectingConfiguration) {
            _expectingConfiguration = false;
            _configurationURL = specToURL(arg);
        } else {
            return false;
        }

        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The subdirectory (if any) of ptolemy/configs where the configuration
    // may be found.  For example if vergil was called with -ptiny,
    // then this variable will be set to "ptiny", and the configuration
    // should be at ptolemy/configs/ptiny/configuration.xml
    private String _configurationSubdirectory;

    // URL of the configuration to read.
    // The URL may absolute, or relative to the Ptolemy II tree root.
    // We use the URL instead of the string so that if the configuration
    // is set as a command line argument, we can use the processed value
    // from the command line instead of calling specToURL() again, which
    // might be expensive.
    private URL _configurationURL;

    // Flag indicating that the previous argument was -conf
    private boolean _expectingConfiguration = false;

    /** True if we have printed the securityExceptionMessage. */
    private static boolean _printedSecurityExceptionMessage = false;
}
