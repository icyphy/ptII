/* An application that executes models specified on the command line.

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

// Ptolemy imports
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Manager;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// PtExecuteApplication
/**
This application executes Ptolemy II models specified on the
command line.  
<p>
The exact facilities that are available are determined by an optional
command line argument that names a directory in ptolemy/configs that
contains a configuration.xml file.  For example, if we call vergil
-ptiny, then we will use ptolemy/configs/ptiny/configuration.xml and
ptolemy/configs/ptiny/intro.htm.  The default configuration is
ptolemy/configs/runConfiguration.xml, which is loaded before any
other command-line arguments are processed.

<p>This application also takes an optional command line argument pair
<code>-conf <i>configurationFile.xml</i></code> that names a configuration
to be read.  For example,
<pre>
$PTII/bin/ptexecute -conf ptolemy/configs/full/configuration.xml ../../domains/sdf/demo/Butterfly/Butterfly.xml
<pre>
and
<pre>
$PTII/bin/ptexecute -full ../../domains/sdf/demo/Butterfly/Butterfly.xml
</pre>
are equivalent
<p>
If no configuration is specified on the command line, then 
the MoML file ptolemy/configs/runConfiguration.xml is loaded before
other command line arguments are processed.
<p>

If one of the command-line arguments is -exit, then System.exit()
is called when all the models are finished running.

If there are no command-line arguments at all, then this class
does nothing.

@author Edward A. Lee, Steve Neuendorffer Christopher Hyland
@version $Id$
@since Ptolemy II 1.0
@see ModelFrame
@see RunTableau
*/
public class PtExecuteApplication extends MoMLApplication
    implements ExecutionListener {

    /** Parse the specified command-line arguments, creating models
     *  and running them.
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public PtExecuteApplication(String args[]) throws Exception {
        // FIXME: Under JDK1.3.1_06, the MoMLApplication constructor
        // calls setLookAndFeel() which invokes getDefaultToolkit()
        // which may cause PtExecuteApplication to not exit.  See
        // http://developer.java.sun.com/developer/bugParade/bugs/4030718.html
        // However, since we now run with JDK1.4.1, this should not
        // be a problem.
        super(args);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Reduce the count of executing models by one.  If the number of
     *  executing models drops ot zero, then notify threads that might
     *  be waiting for this event.
     *  @param manager The manager calling this method.
     *  @param throwable The throwable being reported.
     */
    public synchronized void executionError(Manager manager,
            Throwable throwable) {
        _activeCount--;
        if (_activeCount == 0) {
            notifyAll();
        }
    }

    /**  Reduce the count of executing models by one.  If the number of
     *  executing models drops ot zero, then notify threads that might
     *  be waiting for this event.
     *  @param manager The manager calling this method.
     */
    public synchronized void executionFinished(Manager manager) {
        _activeCount--;
        if (_activeCount == 0) {
            notifyAll();
        }
    }

    /** Create a new instance of this application, passing it the
     *  command-line arguments.
     *  @param args The command-line arguments.
     */
    public static void main(String args[]) {
        try {
            PtExecuteApplication application = new PtExecuteApplication(args);
            application.runModels();
            application.waitForFinish();
            System.exit(0);
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

    /** Do nothing.
     *  @param manager The manager calling this method.
     */
    public void managerStateChanged(Manager manager) {
    }

    /** Return a list of the Ptolemy II models that were created by processing
     *  the command-line arguments.
     *  @return A list of instances of NamedObj.
     */
    public List models() {
        LinkedList result = new LinkedList();
        ModelDirectory directory
            = (ModelDirectory)_configuration
            .getEntity(Configuration._DIRECTORY_NAME);
        Iterator effigies = directory.entityList().iterator();
        while (effigies.hasNext()) {
            Effigy effigy = (Effigy)effigies.next();
            if (effigy instanceof PtolemyEffigy) {
                NamedObj model = ((PtolemyEffigy)effigy).getModel();
                result.add(model);
            }
        }
        return result;
    }

    /** Start the models running, each in a new thread, then return.
     *  @exception IllegalActionException If the manager throws it.
     */
    public void runModels() throws IllegalActionException {
        Iterator models = models().iterator();
        while (models.hasNext()) {
            NamedObj model = (NamedObj)models.next();
            if (model instanceof CompositeActor) {
                CompositeActor actor = (CompositeActor)model;
                // Create a manager if necessary.
                Manager manager = actor.getManager();
                if (manager == null) {
                    manager = new Manager(actor.workspace(), "manager");
                    actor.setManager(manager);
                }
                manager.addExecutionListener(this);
                _activeCount++;
                // Run the model in a new thread.
                manager.startRun();
            }
        }
    }

    /** Wait for all executing runs to finish, then return.
     */
    public synchronized void waitForFinish() {
        while (_activeCount > 0) {
            try {
                wait();
            } catch (InterruptedException ex) {
                break;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a default Configuration, which in this case is given by
     *  the MoML file ptolemy/configuration/runPanelConfiguration.xml.
     *  That configuration supports executing, but not editing,
     *  Ptolemy models.
     *  @return A default configuration.
     *  @exception Exception If the configuration cannot be opened.
     */
    protected Configuration _createDefaultConfiguration() throws Exception {
        if (_configurationURL == null) {
            _configurationURL =
                specToURL("ptolemy/configs/runConfiguration.xml");
        }
        MoMLParser parser = new MoMLParser();
        _configuration =
            (Configuration)parser.parse(_configurationURL,
                    _configurationURL.openStream());
        return _configuration;
    }

    /** Throw an exception.
     *  @return Does not return.
     *  @exception Exception Always thrown.
     */
    protected Configuration _createEmptyConfiguration() throws Exception {
        throw new Exception("No model specified.");
    }

    /** Parse a command-line argument.
     *  @return True if the argument is understood, false otherwise.
     *  @exception Exception If something goes wrong.
     */
    protected boolean _parseArg(String arg) throws Exception {
        if (arg.equals("-exit")) {
            _exit = true;
            return true;
        }
        return super._parseArg(arg);
    }

    /** Parse the command-line arguments. This overrides the base class
     *  only to set the usage information.
     *  @exception Exception If an argument is not understood or triggers
     *   an error.
     */
    protected synchronized void _parseArgs(String args[]) throws Exception {
        _commandTemplate = "ptexecute [ options ] file ...";

        // PtExecuteApplication.super._parseArgs(args)
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
            throw new IllegalActionException(
                    "Missing configuration");
        }
        String [] processedArgs =
            (String [])processedArgsList
            .toArray(new String[processedArgsList.size()]);

        super._parseArgs(processedArgs);
    }

    /** Return a string summarizing the command-line arguments.
     *  @return A usage string.
     */
    protected String _usage() {
        return _configurationUsage(_commandTemplate,
                _localCommandOptions, _localCommandFlags );
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // _localCommandFlags and _commandOptions are static because
    // _usage() may be called from the constructor of the parent class,
    //  in which case non-static variables are null?

    /** The command-line options that are either present or not. */
    protected static String _localCommandFlags[] = {
        "-exit"
    };

    /** The command-line options that take arguments. */
    protected static String _localCommandOptions[][] = {
        {"-config",
         "<configuration URL, defaults to ptolemy/configs/runConfiguration.xml>"},
    };

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
                String potentialConfiguration =
                    "ptolemy/configs/" + _configurationSubdirectory
                    + "/configuration.xml";
                // This will throw an Exception if we can't find the config.
                _configurationURL = specToURL(potentialConfiguration);
            } catch (Exception ex) {
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

    // The count of currently executing runs.
    private int _activeCount = 0;

    // The configuration.
    private Configuration _configuration;

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

    // If true, then exit when the models complete
    private static boolean _exit = false;

    // Flag indicating that the previous argument was -conf
    private boolean _expectingConfiguration = false;

}
