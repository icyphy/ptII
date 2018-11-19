/* Execute the HLA/CERTI RTIG in a subprocess.

Copyright (c) 2013-2018 The Regents of the University of California.
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

package org.hlacerti.lib;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// CertiRtig

/**
 * <p>Execute the HLA/CERTI RTIG in a subprocess.
 * </p><p>
 * This object is based on the Exec actor implementation. It invokes the
 * HLA/CERTI RTIG as a subprocess in a specified <i>directory</i> with a specified
 * <i>environment</i>. A default platform-dependent shell command is preprended
 * so the RTIG is executed within the shell. If another RTIG process is running
 * the current subprocess is destroyed.
 * </p><p>
 * This object is used by the {@link HlaManager} and need to be performed before
 * the initialization of a HLA/CERTI Federate. That's why we execute this mechanism
 * in the {@link HlaManager} preinitialize() method.
 * </p><p>
 * The specified <i>directory</i> is the current directory of the Ptolemy
 * simulation and needs to contain the Federate Object Management file (.fed).
 * This directory is provided by the specification of the .fed file path during
 * the configuration of the {@link HlaManager} attribute.
 * FIXMEjc: check last changes concerning JCERTI and the way to give the .fed file
 * address.
 * </p><p>
 * For a correct execution, the <i>CERTI_HOME</i> environment variable has to be
 * set. It could be set in the shell (by running one of the scripts provided by
 * CERTI) where Vergil is executed, or as a parameter of the Ptolemy model or as
 * a parameter of the {@link HlaManager}:
 * FIXMEjc: As for today, certi_home is not set in Ptolemy model neither is a
 * parameter in hlaManager. Only in the shel, or in .bashrc ou .bash_profile.
 * </p><pre>
 * CERTI_HOME="/absolute/path/to/certi/"
 * </pre><p>
 * Otherwise, the current implementation is not able to find the CERTI
 * environment, the RTIG binary and to perform its execution.
 * </p><b>
 * Current limitation:
 * </b><br>
 * Federate that has launched the RTIG could shutdown the subprocess at the
 * end of the simulation before the other Federates have left the Federation.
 * Then an exception is throwed.
 * FIXMEjc: the above limitation was present in Oct 2013. Check if it is still true.
 *
 * @author Gilles Lasnier, Christopher Brooks
 * @version $Id: CertiRtig.java 214 2018-04-01 13:32:02Z j.cardoso $
 *
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Yellow (glasnier)
 * @Pt.AcceptedRating Red (glasnier)
 */
public class CertiRtig extends NamedObj {

    /** Construct an actor with the given {@link HlaManager} reference and
     *  debug mode status.
     *  @param hm A reference to the associated {@link HlaManager}.
     *  @param addDebugListener The debug mode status.
     */
    public CertiRtig(HlaManager hm, Boolean addDebugListener) {
        _hlaManager = hm;
        if (addDebugListener) {
            addDebugListener(new ptolemy.kernel.util.StreamListener());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute a command, set _process to point to the subprocess
     *  and set up _errorGobbler and _outputGobbler to read data.
     *  @exception IllegalActionException If the execution of the Runtime.exec()
     *  failed or if the RTIG subprocess it not running.
     */
    public void exec() throws IllegalActionException {
        // XXX: FIXME: remove debug print
        System.out.println("CertiRtig: " + _hlaManager.getFullName()
                + ": About to invoke rtig: " + "\ncommand: "
                + java.util.Arrays.toString(_commandArray) + "\nenvironment: "
                + java.util.Arrays.toString(_environmentArray) + "\ndirectory: "
                + _directoryAsFile);
        try {
            _process = _runtime.exec(_commandArray, _environmentArray,
                    _directoryAsFile);
        } catch (IOException e) {
            throw new IllegalActionException(_hlaManager, e,
                    "CertiRtig: exec(): has failed");
        }

        // Create two threads to read from the subprocess.
        _outputGobbler = new _StreamReaderThread(_process.getInputStream(),
                "Stdout-" + _streamReaderThreadCount++, _hlaManager);
        _errorGobbler = new _StreamReaderThread(_process.getErrorStream(),
                "Stderr-" + _streamReaderThreadCount++, _hlaManager);
        _errorGobbler.start();
        _outputGobbler.start();

        if (_streamReaderThreadCount > 1000) {
            // Avoid overflow in the thread count.
            _streamReaderThreadCount = 0;
        }
    }

    /** Initialize command, arguments and environment variables to
     *  invoke the subprocess.
     *  @param directory The current path where the simulation is executed.
     * FIXMEjc: check if is where the simulation is executed, or where the
     * fed file is.
     *  @exception IllegalActionException If the directory to launch the
     *  RTIG process doesn't exit.
     */
    public void initialize(String directory) throws IllegalActionException {
        _directoryAsFile = null;
        _isAlreadyLaunched = false;
        _runtime = Runtime.getRuntime();

        // Retrieve CERTI_HOME environment variable.
        String certiHome = null;

        // First, look if there is a CERTI_HOME variable set in the current
        // shell environment. If not look if there is a CERTI_HOME attribute
        // set for the model. If not look if there is a CERTI_HOME attribute
        // set for the associated HlaManager. If not, throws an exception.
        // FIXMEjc: as for June, 2018, the plan was to add certi_home in the
        // Ptolemy model or in the HlaManager interface. Is it a good idea?
        if (System.getenv("CERTI_HOME") != null) {
            certiHome = System.getenv("CERTI_HOME");
        } else if (_hlaManager.getContainer()
                .getAttribute("CERTI_HOME") != null) {
            certiHome = ((StringToken) ((Parameter) _hlaManager.getContainer()
                    .getAttribute("CERTI_HOME")).getToken()).stringValue();

        } else if (_hlaManager.getAttribute("CERTI_HOME") != null) {
            certiHome = ((StringToken) ((Parameter) _hlaManager
                    .getAttribute("CERTI_HOME")).getToken()).stringValue();
        } else {
            throw new IllegalActionException(_hlaManager,
                    "CertiRtig: initialize(): No CERTI_HOME variable set");
        }

        if (_debugging) {
            _debug("CertiRtig: initialize(): CERTI_HOME=" + certiHome);
        }

        File fedFileName = new File(directory);

        // The list of command and arguments to execute in the shell.
        List<String> commandList = null;

        // Execute command as shell interpret.
        commandList = _getCommandList();

        // Build the command to execute in the shell: "rtig"
        commandList.add(certiHome + "/bin/rtig");

        _commandArray = commandList.toArray(new String[commandList.size()]);

        // Set the environment variables by prepending the
        // CERTI-specific values to the values from the environment.
        // Under RHEL, rtig is possibly linked with libraries in
        // Matlab, so we need to be sure to include DYLD_LIBRARY_PATH,
        // LD_LIBRARY_PATH or PATH from the environment.
        // FIXMEjc: Under MacOS (since El Capitan) there is an issue
        // with dynamic libraires: they must be in /usr/local/lib
        String pathSeparator = System.getProperty("path.separator");
        String osName = StringUtilities.getProperty("os.name");

        _environmentArray = new String[2];

        // XXX: FIXME: remove debug print
        System.out.println("CertiRtig: the os.name property is: " + osName);

        // Only set the environment variable that is appropriate for
        // the platform.
        // Under MacOS since El Capitain, is not enough to have the dynamic
        // libraries set as below:  /usr/local/lib must have a symbolic link
        // to $CERTI_HOME/lib.
        if (osName.startsWith("Mac OS X")) {
            String dyldLibraryPath = "DYLD_LIBRARY_PATH=" + certiHome + "/lib";
            String dyldVariable = System.getenv("DYLD_LIBRARY_PATH");
            if (dyldVariable != null) {
                dyldLibraryPath += pathSeparator + dyldVariable;
            }
            _environmentArray[0] = dyldLibraryPath;
        } else if (osName.startsWith("Windows")) {
            String path = "PATH=" + certiHome + "/bin";
            String pathVariable = System.getenv("PATH");
            if (pathVariable != null) {
                path += pathSeparator + pathVariable;
            }
            _environmentArray[0] = path;
        } else {
            // Linux or anything else.
            String ldLibraryPath = "LD_LIBRARY_PATH=" + certiHome + "/lib";
            String ldVariable = System.getenv("LD_LIBRARY_PATH");
            if (ldVariable != null) {
                ldLibraryPath += pathSeparator + ldVariable;
            }
            _environmentArray[0] = ldLibraryPath;
        }
        // FIXMEjc: check ISAE tickets for writting the comments.
        String certiFomPath = "";
        if (System.getenv("CERTI_FOM_PATH") != null) {
            String certiFomPathVariable = System.getenv("CERTI_FOM_PATH");
            certiFomPath = "CERTI_FOM_PATH=" + certiFomPathVariable;
        } else {
            certiFomPath = "CERTI_FOM_PATH=" + certiHome + "/share/federations/";
        }
        _environmentArray[1] = certiFomPath;

        _directoryAsFile = new File(fedFileName.getParent());
        if (!_directoryAsFile.isDirectory()) {
            throw new IllegalActionException(_hlaManager,
                    "CertiRtig: initialize(): No such directory: "
                            + _directoryAsFile);
        }
    }

    /** Indicate if the RTIG process is already running somewhere else.
     *  @return True if the RTIG is launched somewhere else, False otherwise.
     */
    public boolean isAlreadyLaunched() {
        return _isAlreadyLaunched;
    }

    /** Indicate if the current subprocess is running.
     * @return True if the RTIG is running, False otherwise.
     */
    public boolean isRunning() {
        try {
            _process.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }
    }

    /** Terminate the process and close any associated streams.
     *  @exception IllegalActionException If the closing stdin of the subprocess
     *  threw an IOException.
     */
    //FIXMEjc: terminateProcess is called in two situations:
    // 1. By CertiRtig.java itself: When a federate is launched and there
    //    is already a rtig process running (because a first federate launched it,
    //    or because it was run by hand);
    // 2. By HlaManager.java, when all federates in a Federation have resigned,
    //    and the Federation was destroyed (see While (!canDestroyRtig) {}).
    public void terminateProcess() throws IllegalActionException {
        if (_process != null) {
            System.out.println("CertiRtig: " + _hlaManager.getFullName()
                    + ": About to terminate rtig.");
            try {
                // Close the stdin of the subprocess.
                _process.getOutputStream().close();

            } catch (NullPointerException ex) {
                // Ignore, _process was set to null elsewhere.
            } catch (IOException io) {
                throw new IllegalActionException(_hlaManager, io,
                        "CertiRtig: terminateProcess(): "
                                + "Closing stdin of the subprocess threw an IOException.");
            }
            if (_process != null) {
                System.out.println("CertiRtig " + _hlaManager.getFullName()
                                   + " before destroy: _process.isAlive is "
                                   + _process.isAlive());
                if (_debugging) {
                    _debug("CertiRtig " + _hlaManager.getFullName()
                           + " before destroy: _process.isAlive is "
                              + _process.isAlive());
                }
                // FIXMEjc: The rtig process is still running after .destroy;
                // tested with MacOS 10.12 and also a VM CentOS 7.
                // After running the same federation several times, e.g, 10 times,
                // (whitout kill the rtig before a new lauching), an exception
                // appears in the last federate to be launched, after a
                // correct execution of the federation:
                // java.lang.NullPointerException
                // at org.hlacerti.lib.CertiRtig.terminateProcess(CertiRtig.java:310)
                // at org.hlacerti.lib.HlaManager.wrapup(HlaManager.java:1323)
                _process.destroy();
                System.out.println("CertiRtig " + _hlaManager.getFullName()
                                   + " after destroy: _process.isAlive is "
                                   + _process.isAlive());
                if (_debugging) {
                    _debug("CertiRtig " + _hlaManager.getFullName()
                           + " after destroy: _process.isAlive is "
                              + _process.isAlive());
                }
                _process = null;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Get the command list arguments for exec.
     *  @return The list of arguments (as strings).
     */
    private List<String> _getCommandList() {
        List<String> retval = new LinkedList<String>();

        String osName = System.getProperty("os.name");
        if (osName.equals("Windows 95")) {
            retval.add("command.com");
            retval.add("/C");
        } else if (osName.startsWith("Windows")) {
            retval.add("cmd.exe");
            retval.add("/C");
        } else {
            retval.add("/bin/sh");
            retval.add("-c");
        }
        return retval;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Private class that reads a stream in a thread and updates the
     *  stringBuffer. This is a subset of the functionnalities provide by
     *  the {@link Exec$$_StreamReaderThread} class.
     *  @author Gilles Lasnier, based on Exec$$_StreamReaderThread.java by
     *  Christopher Hylands Brooks.
     */
    private class _StreamReaderThread extends Thread {
        /** Create a _StreamReaderThread.
         *  @param inputStream The stream to read from.
         *  @param name The name of this StreamReaderThread,
         *  which is useful for debugging.
         *  @param actor The parent actor of this thread, which
         *  is used in error messages.
         */
        _StreamReaderThread(InputStream inputStream, String name,
                Nameable actor) {
            super(name);
            _inputStream = inputStream;
            _inputStreamReader = new InputStreamReader(_inputStream,
                    java.nio.charset.Charset.defaultCharset());
            _actor = actor;
            _stringBuffer = new StringBuffer();
        }

        /** Read lines from the inputStream and append them to the
         *  stringBuffer.
         */
        @Override
        public synchronized void run() {
            if (!_inputStreamReaderClosed) {
                _read();
            }
        }

        /** Read from the stream until we get to the end of the stream
         *  NOTE: This was initially synchronized so that it is not called
         *  simultaneously from run() and getAndReset(). As getAndReset()
         *  is not implemented here we could remove it.
         */
        private synchronized void _read() {
            // We read the data as a char[] instead of using readline()
            // so that we can get strings that do not end in end of
            // line chars.
            char[] chars = new char[80];
            int length; // Number of characters read.

            // Oddly, InputStreamReader.read() will return -1
            // if there is no data present, but the string can still
            // read.
            try {
                while ((length = _inputStreamReader.read(chars, 0, 80)) != -1) {
                    _stringBuffer.append(chars, 0, length);
                    // Under Mac OS X, if there are shared library
                    // problems, we might hang here, so if debugging is
                    // enabled, we print.
                    if (_debugging) {
                        _debug("CertRtig: reading: " + new String(chars));
                    }
                }
            } catch (IOException e) {
                throw new InternalErrorException(_actor, e,
                        getName() + " IOExeception throwed.");
            }

            if (_debugging) {
                _debug("_read(): " + _stringBuffer.toString());
            }

            if (_stringBuffer.toString()
                    .matches(".*SocketUDP:\\sBind:\\sAddress\\s"
                            + "already\\sin\\suse\n.*")) {
                _isAlreadyLaunched = true;

                // If another is running, we don't need this subprocess anymore,
                // so destroy it. This can happen if a first Ptolemy federate
                // already lauched the rtig, or if it was lauched in the shell
                // before lauching any Ptolemy federate.
                try {
                    terminateProcess();
                } catch (IllegalActionException e) {
                    throw new InternalErrorException(_actor, e, getName()
                            + " failed to execute terminateProcess().");
                }
            }
        }

        // The actor associated with this stream reader.
        private Nameable _actor;

        // Stream from which to read.
        private InputStream _inputStream;

        // Stream from which to read.
        private InputStreamReader _inputStreamReader;

        // Indicator that the stream has been closed.
        private boolean _inputStreamReaderClosed = false;

        // StringBuffer to update.
        private StringBuffer _stringBuffer;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** StreamReader with which we read stderr. */
    private _StreamReaderThread _errorGobbler;

    /** StreamReader with which we read stdout. */
    private _StreamReaderThread _outputGobbler;

    /** The Process that we are running. */
    private static Process _process;

    /** Instance count of output and error threads, used for debugging.
     *  When the value is greater than 1000, we reset it to 0. */
    private static int _streamReaderThreadCount = 0;

    /** The directory where the simulation is launched. */
    private File _directoryAsFile;

    /** A reference to the Runtime class that allows the application to
     *  interface with the environment in which the application is running.
     */
    private Runtime _runtime;

    /** The command to execute in the shell. */
    private String[] _commandArray;

    /** The environment variables required to execute the command. */
    private String[] _environmentArray;

    /** A reference to the associated {@link HlaManager}.
     */
    private HlaManager _hlaManager;

    /** Indicate if another RTIG subprocess is already running. */
    private Boolean _isAlreadyLaunched;
}
