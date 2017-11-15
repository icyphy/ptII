/* Download, build, start and stop the Global Data Plane (GDP).

@Copyright (c) 2016 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.actor.lib.jjs.modules.gdp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.AbstractInitializableAttribute;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.util.FileUtilities;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringBufferExec;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// GDPManager

/**
 * Download, build, start and stop the Global Data Plane (GDP).
 *
 * <p>This class requires read access to the GDP.  As of June, 2016,
 * the GDP repos are not publicly available.</p>
 *
 * <p>The GDP "provides a data-centric glue for swarm applications."
 * For more information, see <a href="https://swarmlab.eecs.berkeley.edu/projects/4814/global-data-plane">https://swarmlab.eecs.berkeley.edu/projects/4814/global-data-plane</a>.</p>
 *
 * <p>The primary purpose of this class is to make it easy to download,
 * build and start the GDP on a local machine.  This is primarily useful
 * for testing. </p>
 *
 * <p>If the <i>isLocalGDP</i> parameter is false, the the local GDP
 * is <b>not</b> used.</p>
 *
 * <p>See org/terraswarm/accessor/accessors/web/gdp/gdpSetup for a shell
 * script that does something similar.</p>
 *
 * @author Christopher Brooks, based on HlaManager by Gilles Lasnier, Contributors: Patricia Derler, Edward A. Lee, David Come, Yanxuan LI
 * @version $Id$
 * @since Ptolemy II 11.0
 *
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class GDPManager extends AbstractInitializableAttribute {

    /** Construct a GDPManager with a name and a container. The container
     *  argument must not be null, or a NullPointerException will be thrown.
     *  This actor will use the workspace of the container for synchronization
     *  and version counts. If the name argument is null, then the name is set
     *  to the empty string. Increment the version of the workspace.
     *  @param container Container of this attribute.
     *  @param name Name of this attribute.
     *  @exception IllegalActionException If the container is incompatible
     *  with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *  an actor already in the container.
     */
    public GDPManager(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        buildGDP = new Parameter(this, "buildGDP");
        buildGDP.setTypeEquals(BaseType.BOOLEAN);
        buildGDP.setExpression("true");

        cleanGDP = new Parameter(this, "cleanGDP");
        cleanGDP.setTypeEquals(BaseType.BOOLEAN);
        cleanGDP.setExpression("false");

        createNewLog = new Parameter(this, "createNewLog");
        createNewLog.setTypeEquals(BaseType.BOOLEAN);
        createNewLog.setExpression("false");

        deleteAllGCLsInWrapup = new Parameter(this, "deleteAllGCLsInWrapup");
        deleteAllGCLsInWrapup.setTypeEquals(BaseType.BOOLEAN);
        deleteAllGCLsInWrapup.setExpression("true");

        gdpSourceDirectory = new FileParameter(this, "gdpSourceDirectory");
        new Parameter(gdpSourceDirectory, "allowFiles", BooleanToken.FALSE);
        new Parameter(gdpSourceDirectory, "allowDirectories", BooleanToken.TRUE);
        gdpSourceDirectory.setExpression("$PTII/vendors/gdp");

        isLocalGDP = new Parameter(this, "isLocalGDP");
        isLocalGDP.setTypeEquals(BaseType.BOOLEAN);
        isLocalGDP.setExpression("true");

        stopGDPDaemonsInWrapup = new Parameter(this, "stopGDPDaemonsInWrapup");
        stopGDPDaemonsInWrapup.setTypeEquals(BaseType.BOOLEAN);
        stopGDPDaemonsInWrapup.setExpression("true");

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** If true then build the local copy of the GDP.
     *  The default value is the value true;
     */
    public Parameter buildGDP;

    /** If true then remove the contents of the <i>gdpSourceDirectory</i>
     *  at the start of initialization.
     *  The default value is the value false.
     */
    public Parameter cleanGDP;

    /** If true, then create a new log with a random number
     *  appended to the value of <i>logName</i>.  This parameter
     *  can be used instead of <i>deleteAllGCLsIWrapup.</i>.
     *  If this parameter is true and  while creating the log
     *  a log with that name already exists, then an exception
     *  will be thrown.
     */
    public Parameter createNewLog;

    /** If true, the delete all the GCLs parameter in wrapup().  The
     *  default value is true, meaning that all the logs are.
     *  deleted.  If true, then the daemons are stopped in wrapup.  If
     *  ~/.ep_adm_params/ cannot be created, then the gcls are stored
     *  in /var/swarm/gdp/gcls and this parameter has no effect.
     *  For a safer way to create a new log each time, use the
     *  <i>createNewLog</i> parameter
     */
    public Parameter deleteAllGCLsInWrapup;

    /** The path to the GDP sources.  The default value is
     * "$PTII/vendors/gdp".
     */
    public FileParameter gdpSourceDirectory;

    /** Use a local version of the GDP.  The default value is true,
     *  meaning that the GDP will be downloaded, built, started and stopped.
     */
    public Parameter isLocalGDP;

    /** If true, then stop the GDP daemons in wrapup().  The default
     *  value is true, meaning that the daemons are stopped.  If
     *  deleteAllGCLsInWrapup is true, the the value of this parameter
     *  is ignored and the GDP daemons are always stopped in wrapup.
     *  Set this parameter to true to debug the daemons after running
     *  a model.
     */
    public Parameter stopGDPDaemonsInWrapup;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Download and build the gdp and gdp_router.
     *  @param gdpSourceDirectoryParameter The path to the gdp sources.
     *  @param cleanGDP True if the gdp should be cleaned before installing
     *  @param buildGDP True if the gdp should be built and installed
     *  @exception IllegalActionException If there are problems accessing the parameter.
     *  @exception IOException If there are problems accessing or build the repositories.
     */
    public static void downloadAndBuild(FileParameter gdpSourceDirectoryParameter,
            boolean cleanGDP,
            boolean buildGDP)
            throws IllegalActionException, IOException {
        // This method is static to make it easier to test.
        File gdpSourceDirectory = gdpSourceDirectoryParameter.asFile();

        // if cleanGDP is true, the remove the gdp source directory.
        if (cleanGDP) {
            if (gdpSourceDirectory.exists()) {
                //System.out.println("cleanGDP was true, deleting " + gdpSourceDirectory);
                //FileUtilities.deleteDirectory(gdpSourceDirectory.toString());
                System.out.println("cleanGDP was true but we are not deleting " + gdpSourceDirectory);
            }
        }

        if (gdpSourceDirectory.isFile()) {
            throw new IOException(gdpSourceDirectory + _badFileMessage);
        }
        // If the directory does not exist, then create it
        if (!gdpSourceDirectory.exists()) {
            if (!gdpSourceDirectory.mkdirs()) {
                throw new IOException("Could not create " + gdpSourceDirectory);
            }
        }

        // If the gdp directory does not exist, then force the build.
        if (!(new File(gdpSourceDirectory, "gdp").isDirectory())) {
            buildGDP = true;
        }

        // Clone or pull the gdp git repository.
        _lastGDPRepoUpdateTime = GDPManager._cloneOrPull(gdpSourceDirectory,
                "gdp",
                // Default to ssh so that we don't hang while waiting for a username and pass.
                "repoman@repo.eecs.berkeley.edu:projects/swarmlab/gdp.git",
                // "https://repo.eecs.berkeley.edu/git/projects/swarmlab/gdp.git",
                _lastGDPRepoUpdateTime);

        // Clone or pull the gdp_router git repository.
        _lastGDPRouterRepoUpdateTime = GDPManager._cloneOrPull(gdpSourceDirectory,
                "gdp_router",
                // Default to ssh.
                "repoman@repo.eecs.berkeley.edu:projects/swarmlab/gdp_router.git",
                //"https://repo.eecs.berkeley.edu/git/projects/swarmlab/gdp_router.git",
                _lastGDPRouterRepoUpdateTime);

        _gdpRouter = new File(gdpSourceDirectory, "gdp_router");
        _gdp = new File(gdpSourceDirectory, "gdp");

        // Build the gdp.
        if (buildGDP) {
            System.out.println("Building the gdp typically requires installing some packages. "
                    + "see " + _gdp + "/README.md");

            MessageHandler.status("Building the gdp.");
            final StringBufferExec exec = new StringBufferExec(true /*appendToStderrAndStdout*/);
            exec.setWorkingDirectory(_gdp);
            List execCommands = new LinkedList<String>();
            // Do not require libavahi, which is used for zero-conf support.  libavah is not easy to compile under Darwin with homebrew
            // Run clean_java to get rid of jar files from previous versions.
            String makeCommand = "make clean clean_Java all install_Java";
            execCommands.add(makeCommand);
            exec.setCommands(execCommands);
            exec.setWaitForLastSubprocess(true);
            exec.start();
            int returnCode = exec.getLastSubprocessReturnCode();
            if (returnCode == 0) {
                MessageHandler.status("Built the gdp.");
            } else {
                throw new IOException("Failed to build the gdp."
                        + "cd " + _gdp + "; " + makeCommand + "\n"
                        + exec.buffer
                        + "See " + _gdp + "/README.md and run " + _gdp + "/adm/gdp-setup.sh"
                        + "Also, see " + _gdpRouter + "/README.md.");
            }

            // Copy the gdp jar file to $PTII/lib
            String jarFileName = "";

            File[] files = new File(_gdp, "lang/java").listFiles();
            if (files == null) {
                throw new IOException("No files found in " + _gdp + "/lang/java?");
            }
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    jarFileName = files[i].getName();
                    if (jarFileName.matches("gdp-.*.jar")) {
                        break;
                    }
                    jarFileName = "";
                }
            }
            File jarFile = new File(_gdp + File.separator + "lang/java" + File.separator + jarFileName);
            System.out.println("Checking for jar file " + jarFile);

            // FIXME: Copying a new jar file is not likely to cause the new class definitions to
            // be loaded in to the current JVM.
            if (jarFile.exists() && jarFile.isFile()) {
                String destinationFileName = StringUtilities.getProperty("ptolemy.ptII.dir") + File.separator + "lib" + File.separator + jarFileName;
                File destination = new File(destinationFileName);
                if (!destination.exists()) {
                    throw new IOException("Building the GDP Java interface created "
                            + destination + ", which does not exist and therefore is "
                            + "not in the path.  Thus you may be running an old version "
                            + "of the GDP Java interface.  To fix this, edit $PTII/configure.in, "
                            + "then run (cd $PTII; autoconf;./configure; ant) "
                            + "and then restart Ptolemy.");
                }
                if (!destination.delete()) {
                    throw new IOException("Could not delete " + destination
                            + "before renaming " + jarFile + " to that location.");
                }
                String message = "Renaming " + jarFile + " to " + destination;
                MessageHandler.status(message);
                // renameTo sometimes returns false even though the file was
                // renamed.  As we delete the file above, if it exists, then we can
                // assume it was copied.
                if (!jarFile.renameTo(destination)) {
                    File newDestination = new File(destinationFileName);
                    if (!newDestination.exists()) {
                        throw new IOException("Could not rename " + jarFile
                                + " to " + destination);
                    }
                }
            }

            // Copy the shared library file to $PTII/lib.

            // FIXME: Ideally all the shared libraries would be in the jar
            // file where JNA can find them.
            String sharedLibraryFileName = "";

            files = new File(_gdp, "gdp").listFiles();
            if (files == null) {
                throw new IOException("No files found in " + _gdp + "/gdp?");
            }
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    sharedLibraryFileName = files[i].getName();
                    // Match lib*.dylib* and lib*.so*, but not lib*.a
                    if (sharedLibraryFileName.matches("lib.*") && !sharedLibraryFileName.matches("lib.*a")) {
                        break;
                    }
                    sharedLibraryFileName = "";
                }
            }
            File sharedLibraryFile = new File(_gdp + File.separator + "gdp" + File.separator + sharedLibraryFileName);
            System.out.println("Checking for shared library file " + sharedLibraryFile);

            // FIXME: If the shared library has already been loaded by JNA, then updating
            // the shared library is not likely to change much.
            if (sharedLibraryFile.exists() && sharedLibraryFile.isFile()) {
                // Under Linux, JNA wants the name to be libgdp.N.M.so, but the name is libgdp.so.N.M

                String osName = StringUtilities.getProperty("os.name");
                String version = sharedLibraryFileName.substring("libgdp.so.".length());
                String newSharedLibraryFileName = sharedLibraryFileName;
                if (osName.equals("Linux")) {
                    newSharedLibraryFileName = "libgdp." + version + ".so";
                } else if (osName.equals("Mac OS X")) {
                    // Sadly, the gdp creates .so files under Mac OS X.
                    newSharedLibraryFileName = "libgdp." + version + ".dylib";
                }

                File destination = new File(StringUtilities.getProperty("ptolemy.ptII.dir") + File.separator + "lib",  newSharedLibraryFileName);
                String message = "Renaming " + sharedLibraryFile + " to " + destination;
                MessageHandler.status(message);
                if (!sharedLibraryFile.renameTo(destination)) {
                    throw new IOException("Could not rename " + sharedLibraryFile
                            + " to " + destination);
                }
            }
        }

        // Create configuration files for the gdp.

        // This is the default location where the gdp searches for its configuration.
        // Under RHEL, we tried setting EP_PARAM_PATH and calling updateEnvironment(),
        // but it did not work.
        // FIXME: get EP_PARAM_PATH working properly.

        String userHome = StringUtilities.getProperty("user.home");
        if (userHome.length() == 0) {
            System.err.println("Could not get the value of the user.home Java property.  "
                    + "This can happen under applets.  This means that the gdp will expect "
                    + "to be able to write to /var/swarm/gdp/gcls/.");

            _gclsDirectory = new File("/var/swarm/gdp/gcls");

            if (! _gclsDirectory.isDirectory()) {
                if (!_gclsDirectory.mkdirs()) {
                    System.err.println("/var/swarm/gdp/gcls does not exist.  "
                            + "To create this directory, do: "
                            + "sudo mkdir /var/swarm/gdp/gcls; sudo chown $USER /var/swarm/gdp/gcls");
                }
            }
        } else {
            GDPManager.setGdpConfigurationFile(userHome, "swarm.gdp.routers=localhost");

            // Create the directory where the logs are stored.
            _gclsDirectory = new File(gdpSourceDirectory, "gcls");
            if (!_gclsDirectory.exists()) {
                if (!_gclsDirectory.mkdirs()) {
                    throw new IOException("Failed to create " + _gclsDirectory);
                }
            }

            // Create ~/.ep_adm_params/gdplogd
            File gdplogdConfigurationFile = new File(_epAdmParamsDirectory, "gdplogd");
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new OutputStreamWriter(
                                new FileOutputStream(gdplogdConfigurationFile), "utf-8"));
                writer.write("swarm.gdplogd.gcl.dir=" + _gclsDirectory);
                writer.newLine();
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        } // Read user.home property

    }

    /** Initializes the GDPManager attribute.
     *  The gdp and gdp_router processes are started.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        // Add $PTII/lib to the jna.library.path System property.

        // We could use StringUtilities.addDirectoryToJavaLibraryPath(),
        // but adding to jna.library.path is more likely to work.
        String jnaLibraryPath = StringUtilities.getProperty("jna.library.path");
        String ptIILib = StringUtilities.getProperty("ptolemy.ptII.dir") + "/lib";
        if (jnaLibraryPath.indexOf(ptIILib) == -1) {
            String newPath = ptIILib;
            if (jnaLibraryPath.indexOf(File.pathSeparator) != -1) {
                newPath = jnaLibraryPath + File.pathSeparator + ptIILib;
            }
            System.setProperty("jna.library.path", newPath);
            System.out.println("jna.library.path Java property is " + StringUtilities.getProperty("jna.library.path"));
        }

        if (!_gdpRouterRunning || !_gdpLogdRunning
                || ((BooleanToken) deleteAllGCLsInWrapup.getToken()).booleanValue()
                || ((BooleanToken) stopGDPDaemonsInWrapup.getToken()).booleanValue()) {
            // Start the gdp_router.
            _gdpRouterExec = new StringBufferExec(true /*appendToStderrAndStdout*/);
            _gdpRouterExec.setWorkingDirectory(_gdpRouter);
            LinkedList<String> gdpRouterCommands = new LinkedList<String>();

            // Kill any router processes.
            String userName = StringUtilities.getProperty("user.name");
            String pkillUserFlag = userName.length() > 0
                ? "-U " + userName
                : "";

            // FIXME: We should use pkill -f 'python ./src/gdp_router.py', but
            // passing arguments with spaces does not work here.
            gdpRouterCommands.add("pkill " + pkillUserFlag + " python");
            _gdpRouterExec.setCommands(gdpRouterCommands);
            _gdpRouterExec.setWaitForLastSubprocess(true);
            _gdpRouterExec.start();

            // Start the router process.
            gdpRouterCommands = new LinkedList<String>();
            String gdpRouterCommand = "src/gdp_router.py -l" + _gdpRouter + File.separator + "routerLog.txt";
            gdpRouterCommands.add("./" + gdpRouterCommand);
            System.out.println("The command to run the gdp router:\n  "
                    + " " + _gdpRouter + "/" + gdpRouterCommand);

            _gdpRouterExec.setCommands(gdpRouterCommands);
            System.out.println("GDPManager: Using a local copy of the GDP.  To use this copy, do:\n"
                    + "and then run commands in " + _gdp + "/");
            _gdpRouterExec.setWaitForLastSubprocess(false);
            _gdpRouterExec.start();
            _gdpRouterRunning = true;

            // Sleep so that the router can come up.
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                System.err.println("GDPManager: sleep interrupted? " + ex);
            }

            // wrapup() might have removed the gcls log directory.
            if (!_gclsDirectory.exists()) {
                if (!_gclsDirectory.mkdirs()) {
                    throw new IllegalActionException(this, "Failed to create " + _gclsDirectory);
                }
            }

            // Kill any previously running gdplogd processes
            _gdpLogdExec = new StringBufferExec(true /*appendToStderrAndStdout*/);
            _gdpLogdExec.setWorkingDirectory(_gdp);
            LinkedList<String> gdpCommands = new LinkedList<String>();
            gdpCommands.add("pkill " + pkillUserFlag + " gdplogd");
            _gdpLogdExec.setCommands(gdpCommands);
            _gdpLogdExec.setWaitForLastSubprocess(true);
            _gdpLogdExec.start();

            // Start up the gdplogd process.
            gdpCommands = new LinkedList<String>();
            try {
                _hostName = InetAddress.getLocalHost().getHostName();
            } catch (Throwable throwable) {
                throw new IllegalActionException(this, throwable, "Could not get the hostname?");
            }
            //String debug = "-Dgdp.gdp_chan=39";
            String debug = "-Dgdplogd.physlog=39";

            String gdplogdCommand = "gdplogd/gdplogd -F " + debug + " -N " + _hostName;
            System.out.println("The command to run gdplogd:\n  "
                    + " " + _gdp + "/" + gdplogdCommand);
            gdpCommands.add("./" + gdplogdCommand);
            _gdpLogdExec.setCommands(gdpCommands);
            _gdpLogdExec.setWaitForLastSubprocess(false);
            _gdpLogdExec.start();
            _gdpLogdRunning = true;
        }
    }

    /** Preinitialze the GDPManager attribute.
     *  If the gdp and gdp_router sources are checked out, the gdp is built
     *  and configuration files are created.
     *  @exception IllegalActionException If there are problems checking
     *  out the repositories, building the gdp or creating the configuration files.
     *
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        if (((BooleanToken) isLocalGDP.getToken()).booleanValue()) {
            try {
                GDPManager.downloadAndBuild(gdpSourceDirectory,
                        ((BooleanToken) cleanGDP.getToken()).booleanValue(),
                        ((BooleanToken) buildGDP.getToken()).booleanValue());
            } catch (IOException ex) {
                throw new IllegalActionException(this, ex, "Failed to build the gdp.");
            }
        }
    }

    /** Write the value of the settings parameter to ~/.ep_adm_params/gdp.
     *  @param userHome The value of the user.home jvm property.
     *  @param settings The settings to be written, such as "swarm.gdp.routers=localhost"
     *  or "gdp-03.eecs.berkeley.edu; gdp-02.eecs.berkeley.edu".
     *  @exception IOException If the file cannot be created.
     */
    public static void setGdpConfigurationFile(String userHome, String settings)
        throws IOException {
        // Create ~/.ep_adm_params/
        _epAdmParamsDirectory = new File(userHome, ".ep_adm_params");
        if (!_epAdmParamsDirectory.exists()) {
            if (!_epAdmParamsDirectory.mkdirs()) {
                throw new IOException("Failed to create " + _epAdmParamsDirectory);
            }
        }

        System.out.println("GDPManager: Using configuration files in " + _epAdmParamsDirectory);
        System.out.println("GDPManager: gdp settings:\n" + settings);

        // Create ~/.ep_adm_params/gdp
        File gdpConfigurationFile = new File(_epAdmParamsDirectory, "gdp");
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                                new FileOutputStream(gdpConfigurationFile), "utf-8"));
            writer.write(settings);
            writer.newLine();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /** Optionally delete the gcls log directory and terminate the
     *  gdp_router and the gdp processes.
     *  @exception IllegalActionException If the parent class throws it
     *  of if there are problems terminating the processes
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        if (((BooleanToken) deleteAllGCLsInWrapup.getToken()).booleanValue()
                || ((BooleanToken) stopGDPDaemonsInWrapup.getToken()).booleanValue()) {
            MessageHandler.status("Stopping the GDP daemons.");
            try {
                _gdpRouterExec.cancel();
            } finally {
                _gdpRouterRunning = false;
                try {
                    _gdpLogdExec.cancel();
                } finally {
                    _gdpLogdRunning = false;
                }
            }
        }
        if (((BooleanToken) deleteAllGCLsInWrapup.getToken()).booleanValue()) {
            if (_gclsDirectory.toString(). equals("/var/swarm/gdp/gcls")) {
                MessageHandler.status("GDPManager: Not removing /var/swarm/gdp/gcls");
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    System.err.println("GDPManager: sleep interrupted? " + ex);
                }

                MessageHandler.status("GDPManager: Deleting " + _gclsDirectory);
                FileUtilities.deleteDirectory(_gclsDirectory);
            }
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Clone or pull a repository.
     *  Only pull the repo the first time this is run, and every 12 hours after that.
     *  @param directory The directory in which to run the git clone command
     *  @param subdirectory The name of the directory that is created by the git clone
     *  command.  The git pull command is run in directory + File.separator + subdirectory.
     *  @param repository The URL of the git repository from which to clone.
     *  @param lastRepoUpdateTime The time of the repo was last cloned or updated.
     *  @exception IOException If there is a problem cloning or updating the repo.
     */
    private static long _cloneOrPull(File directory, String subdirectory, String repository, long lastRepoUpdateTime)
    throws IOException {
        if (directory.isFile()) {
            throw new IOException(directory + _badFileMessage);
        }
        final StringBufferExec exec = new StringBufferExec(true /*appendToStderrAndStdout*/);
        List execCommands = new LinkedList<String>();

        String commands = "";
        System.out.println("GDPManager._cloneOrPull(" + directory + ", " + subdirectory
                + ", " + repository + ", " + lastRepoUpdateTime);
        File subdirectoryDirectory = new File(directory, subdirectory);

        if (!subdirectoryDirectory.exists()) {
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    throw new IOException("Could not create " + directory);
                }
            }
            exec.setWorkingDirectory(directory);
            String gitClone = "git clone " + repository;
            execCommands.add(gitClone);
            commands = "cd " + directory + "\n" + gitClone;
        } else {
            // Only pull the repo the first time this is run, and every 12 hours after that.
            if (!(lastRepoUpdateTime < 0
                            || (System.currentTimeMillis() - lastRepoUpdateTime > 43200000L))) {
                return lastRepoUpdateTime;
            }
            exec.setWorkingDirectory(subdirectoryDirectory);
            String gitPull = "git pull";
            execCommands.add(gitPull);
            commands = "cd " + subdirectoryDirectory + "\n" + gitPull;
        }

        exec.setCommands(execCommands);

        exec.setWaitForLastSubprocess(true);
        exec.start();
        int returnCode = exec.getLastSubprocessReturnCode();
        String repositoryShortName = repository.substring(repository.lastIndexOf(File.separatorChar) + 1);
        lastRepoUpdateTime = System.currentTimeMillis();

        if (returnCode == 0) {
            MessageHandler.status("Checked out the " + repositoryShortName);
        } else {
            MessageHandler.status("Failed to check out the " + repositoryShortName);
            throw new IOException("Failed to check out the " + repositoryShortName
                    + commands + "\n"
                    + exec.buffer);
        }
        return lastRepoUpdateTime;
    }

    private static String _badFileMessage = " is a file, it must either be a directory or not exist.";

    private boolean _calledWrapupOnce = false;

    /** The ep_adm_params directory, which is read by the gdplogd process. */
    private static File _epAdmParamsDirectory;

    /** The location of the gcls directory, which contains the log.
     *  Wrapup optionally deletes this directory.
     */
    private static File _gclsDirectory;

    /** The location of the gdp repository. */
    private static File _gdp;

    /** The gdplogd process. */
    private StringBufferExec _gdpLogdExec;

    /** True if the gdpLogd process is running. */
    private static boolean _gdpLogdRunning;

    /** The location of the gdp_router repository. */
    private static File _gdpRouter;

    /** The gdp_router process. */
    private StringBufferExec _gdpRouterExec;

    /** True if the gdp_router is running. */
    private static boolean _gdpRouterRunning;

    /** The hostname. */
    private String _hostName;

    /** Last time of gdp repository update. */
    private static long _lastGDPRepoUpdateTime = -1L;

    /** Last time of gdp_router repository update. */
    private static long _lastGDPRouterRepoUpdateTime = -1L;

    /** True if the log should be created in initialize. */
    private boolean _createLog;
}
