/* Download, build, start and stop the local authorization entity, Auth.

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

package ptolemy.actor.lib.jjs.modules.iotAuth;

import java.io.File;
import java.io.IOException;

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
import ptolemy.util.MessageHandler;
import ptolemy.util.StringBufferExec;

///////////////////////////////////////////////////////////////////
//// AuthManager

/**
 * Download, build, start and stop the local authorization entity, Auth.
 * 
 * <p>This class requires access to the github repository for Auth.</p>
 *
 * <p>The primary purpose of this class is to make it easy to download,
 * build and start the Auth on a local machine.  This is primarily useful
 * for testing. </p>
 *
 * <p>See org/terraswarm/accessor/accessors/web/net/authSetup for a shell
 * script that does something similar.</p>
 *
 * @author Hokeun Kim
 * @version $Id$
 * @since Ptolemy II 11.0
 *
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class AuthManager extends AbstractInitializableAttribute {

    /** Construct a AuthManager with a name and a container. The container
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
    public AuthManager(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        buildAuth = new Parameter(this, "buildAuth");
        buildAuth.setTypeEquals(BaseType.BOOLEAN);
        buildAuth.setExpression("true");
        
        cleanAuth = new Parameter(this, "cleanAuth");
        cleanAuth.setTypeEquals(BaseType.BOOLEAN);
        cleanAuth.setExpression("false");
        
        authSourceDirectory = new FileParameter(this, "authSourceDirectory");
        new Parameter(authSourceDirectory, "allowFiles", BooleanToken.FALSE);
        new Parameter(authSourceDirectory, "allowDirectories", BooleanToken.TRUE);
        authSourceDirectory.setExpression("$PTII/vendors/iotauth");

        isLocalAuth = new Parameter(this, "isLocalAuth");
        isLocalAuth.setTypeEquals(BaseType.BOOLEAN);
        isLocalAuth.setExpression("true");

        stopAuthInWrapup = new Parameter(this, "stopAuthInWrapup");
        stopAuthInWrapup.setTypeEquals(BaseType.BOOLEAN);
        stopAuthInWrapup.setExpression("true");

        _needToGenerateCredentials = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** If true then build the local copy of the Auth.
     *  The default value is the value true;
     */
    public Parameter buildAuth;
    
    /** If true then remove the contents of the <i>authSourceDirectory</i>
     *  at the start of initialization.  
     *  The default value is the value false.
     */
    public Parameter cleanAuth;
    
    /** The path to the Auth sources.  The default value is
     * "$PTII/vendors/iotauth".
     */
    public FileParameter authSourceDirectory;

    /** Use a local version of Auth.  The default value is true,
     *  meaning that the Auth will be downloaded, built, started and stopped.
     */
    public Parameter isLocalAuth;
    
    /** If true, then stop Auth processes in wrapup().  The default
     *  value is true, meaning that the daemons are stopped.  If
     *  deleteAllGCLsInWrapup is true, the the value of this parameter
     *  is ignored and the Auth processes are always stopped in wrapup.
     *  Set this parameter to true to debug the daemons after running
     *  a model.
     */ 
    public Parameter stopAuthInWrapup;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
 
    /** Download and build Auth.
     *  @param authSourceDirectoryParameter The path to the Auth sources.
     *  @param cleanAuth True if Auth should be cleaned before installing
     *  @param buildAuth True if Auth should be built and installed
     *  @exception IllegalActionException If there are problems accessing the parameter.
     *  @exception IOException If there are problems accessing or build the repositories.
     */   
    public static void downloadAndBuild(FileParameter authSourceDirectoryParameter,
            boolean cleanAuth,
            boolean buildAuth) 
            throws IllegalActionException, IOException {
        // This method is static to make it easier to test.
        File authSourceDirectory = authSourceDirectoryParameter.asFile();

        // Clone or pull the iotauth git repository.
        AuthManager._cloneOrPull(authSourceDirectory,
                "https://github.com/iotauth/iotauth.git",
                _lastAuthRepoPullTime);

        _auth = new File(authSourceDirectory, "iotauth");
        
        if (_needToGenerateCredentials) {
            AuthManager._generateCredentials(authSourceDirectory, "examples");
        }
    }

    /** Initializes the AuthManager attribute.
     *  The auth processes are started.   
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
    }

    /** Preinitialze the AuthManager attribute.
     *  @exception IllegalActionException If there are problems checking
     *  out the repositories while building and initializing Auth.
     * 
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        if (((BooleanToken) isLocalAuth.getToken()).booleanValue()) {
            try {
                AuthManager.downloadAndBuild(authSourceDirectory,
                        ((BooleanToken) cleanAuth.getToken()).booleanValue(),
                        ((BooleanToken) buildAuth.getToken()).booleanValue());
            } catch (IOException ex) {
                throw new IllegalActionException(this, ex, "Failed to build Auth.");
            }
        }
    }


    /** Stop Auth processes.
     *  @exception IllegalActionException If the parent class throws it
     *  of if there are problems terminating the processes
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Clone or pull a repository.
     *  Only pull the repo the first time this is run, and every pull period after that.
     *  @param directory The directory in which to run the git clone command
     *  @param repository The URL of the git repository from which to clone.
     *  @param lastRepoPullTime The time of the repo was last cloned or updated.
     *  @exception IOException If there is a problem cloning or updating the repo.
     */
    private static void _cloneOrPull(File directory, String repository, long lastRepoPullTime)
    throws IOException {
        if (directory.isFile()) {
            throw new IOException(directory + _badFileMessage);
        }
        final StringBufferExec exec = new StringBufferExec(true); // appendToStderrAndStdout
        List execCommands = new LinkedList<String>();

        String commands = "";
        System.out.println("AuthManager._cloneOrPull(" + directory
                + ", " + repository + ", " + lastRepoPullTime);
        
        if (!directory.exists()) {
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    throw new IOException("Could not create " + directory);
                }
            }
            exec.setWorkingDirectory(directory);
            String gitClone = "git clone " + repository + " .";
            execCommands.add(gitClone);
            commands = gitClone;
        } else {
            // Only pull the repo the first time this is run, and every 10 seconds after that.
            final long repoPullPeriod = 10L * 1000L;
            if (!(lastRepoPullTime < 0
                            || (System.currentTimeMillis() - lastRepoPullTime > repoPullPeriod))) {
                MessageHandler.status("Last pull was within last pull period (in ms): " + repoPullPeriod);
                return;
            }
            exec.setWorkingDirectory(directory);
            String gitPull = "git pull origin master";
            execCommands.add(gitPull);
            commands = gitPull;
        }

        exec.setCommands(execCommands);

        exec.setWaitForLastSubprocess(true);
        exec.setPattern("Already up-to-date.");
        exec.start();
        int returnCode = exec.getLastSubprocessReturnCode();
        String patternLog = exec.getPatternLog();
        System.out.println("patternLog: " + patternLog);
        String repositoryShortName = repository.substring(repository.lastIndexOf(File.separatorChar) + 1);
        lastRepoPullTime = System.currentTimeMillis();

        if (returnCode == 0) {
            MessageHandler.status("Checked out the " + repositoryShortName);
            // No pattern "Already up-to-date."
            if (patternLog.length() == 0) {
                MessageHandler.status("New pull was available, credentials need to be (re-)generated.");
                _needToGenerateCredentials = true;
            }
        } else {
            MessageHandler.status("Failed to check out the " + repositoryShortName);
            throw new IOException("Failed to check out the " + repositoryShortName
                    + commands + "\n"
                    + exec.buffer);
        }
        _lastAuthRepoPullTime = lastRepoPullTime;
    }
    
    private static void _generateCredentials(File directory, String subdirectory) throws IOException {

        if (directory.isFile()) {
            throw new IOException(directory + _badFileMessage);
        }
        final StringBufferExec exec = new StringBufferExec(true /*appendToStderrAndStdout*/);
        List execCommands = new LinkedList<String>();

        String commands = "";
        System.out.println("AuthManager._generateCredentials(" + directory + ", " + subdirectory);
        File subdirectoryDirectory = new File(directory, subdirectory);

    }

    private static String _badFileMessage = " is a file, it must either be a directory or not exist.";
    

    /** The location of the Auth repository. */
    private static File _auth;

    /** The hostname. */
    private String _hostName;
        
    /** True if credentials need to be generated for Auth and entities */
    private static boolean _needToGenerateCredentials;
    
    /** Last time of Auth repository update. */
    private static long _lastAuthRepoPullTime = -1L;
}
