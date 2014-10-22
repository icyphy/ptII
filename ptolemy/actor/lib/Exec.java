/* Execute a command in a subprocess.

 Copyright (c) 2004-2014 The Regents of the University of California.
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
package ptolemy.actor.lib;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// Exec

/**
 Execute a command as a separately running subprocess.

 <p>This actor uses java.lang.Runtime.exec() to invoke a subprocess
 named by the <i>command</i> parameter in a specified <i>directory</i> with a
 specified  <i>environment</i>.  Data from the <i>input</i> port (if any) is
 passed to the input of the subprocess.  The subprocess is run until it
 exits and then contents of the output and error streams of the
 subprocess (if any) are passed to the <i>output</i> and <i>error</i>
 ports.</p>

 <p>If the subprocess generates no data on the output or error stream,
 then the data on the corresponding port(s) will consist of the empty
 string.</p>

 <p> To get the effect of executing a command provided in a shell interpreter, set the
 <i>prependPlatformDependentShellCommand</i> parameter to true.
 This will prepend a default platform-dependent shell command to the command
 you wish to execute so that your command is executed within the shell.
 Alternatively, you can set <i>command</i> to "cmd" (Windows) or "sh" (Windows with Cygwin
 or Linux), and then provide commands at the <i>input</i> port.
 In this case, however, your model will only work on platforms that have the shell
 command you have specified.
 Note that in this case each command must be terminated with a newline.
 For example, to open a model in vergil and run it, you can
 set <i>command</i> to "sh" and use a Const actor to provide
 on the <i>input</i> port the string:</p>
 <pre>
 "vergil -run model.xml\n exit\n"
 </pre>

 <p>A much more interesting actor could be written using a
 Kahn Process Network.  This actor would generate output asynchronously
 as the process was executing.</p>

 <p>For information about Runtime.exec(), see:
 <br><a href="http://www.javaworld.com/javaworld/jw-12-2007/jw-1229-traps.html" target="_top">http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html</a></br>
 <br>and</br>
 <br><a href="http://mindprod.com/jgloss/exec.html" target="_top">http://mindprod.com/jgloss/exec.html</a></br>
 </p>

 @author Christopher Hylands Brooks, Contributors: Edward A. Lee, Daniel Crawl
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (cxh) 2/5/04
 @Pt.AcceptedRating Yellow (cxh) 2/24/04
 */
public class Exec extends LimitedFiringSource {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Exec(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Uncomment the next line to see debugging statements
        //addDebugListener(new ptolemy.kernel.util.StreamListener());
        command = new PortParameter(this, "command", new StringToken(
                "echo \"Hello, world.\""));

        // Make command be a StringParameter (no surrounding double quotes).
        command.setStringMode(true);
        new Parameter(command.getPort(), "_showName", BooleanToken.TRUE);

        directory = new FileParameter(this, "directory");
        new Parameter(directory, "allowFiles", BooleanToken.FALSE);
        new Parameter(directory, "allowDirectories", BooleanToken.TRUE);
        directory.setExpression("$CWD");

        environment = new Parameter(this, "environment");

        String[] labels = new String[] { "name", "value" };
        Type[] values = new Type[] { BaseType.STRING, BaseType.STRING };

        // An array of records {{name = "", value = ""}}
        environment
                .setTypeEquals(new ArrayType(new RecordType(labels, values)));

        // Array with an empty name and value means
        // default environment of the calling process.
        environment.setExpression("{{name = \"\", value = \"\"}}");

        error = new TypedIOPort(this, "error", false, true);
        error.setTypeEquals(BaseType.STRING);
        new Parameter(error, "_showName", BooleanToken.TRUE);

        ignoreIOExceptionReadErrors = new Parameter(this,
                "ignoreIOExceptionReadErrors", BooleanToken.FALSE);
        ignoreIOExceptionReadErrors.setTypeEquals(BaseType.BOOLEAN);

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.STRING);
        new Parameter(input, "_showName", BooleanToken.TRUE);

        output.setTypeEquals(BaseType.STRING);
        new Parameter(output, "_showName", BooleanToken.TRUE);

        exitCode = new TypedIOPort(this, "exitCode", false, true);
        exitCode.setTypeEquals(BaseType.INT);
        new Parameter(exitCode, "_showName", BooleanToken.TRUE);

        prependPlatformDependentShellCommand = new Parameter(this,
                "prependPlatformDependentShellCommand", BooleanToken.FALSE);
        prependPlatformDependentShellCommand.setTypeEquals(BaseType.BOOLEAN);

        throwExceptionOnNonZeroReturn = new Parameter(this,
                "throwExceptionOnNonZeroReturn", BooleanToken.TRUE);
        throwExceptionOnNonZeroReturn.setTypeEquals(BaseType.BOOLEAN);

        waitForProcess = new Parameter(this, "waitForProcess",
                BooleanToken.TRUE);
        waitForProcess.setTypeEquals(BaseType.BOOLEAN);

        // Show the firingCountLimit parameter last.
        firingCountLimit.moveToLast();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The command to be executed.  The command is parsed by
     * {@link ptolemy.util.StringUtilities#tokenizeForExec(String)}
     * into tokens and then executed as a separate subprocess.
     * The initial default value is the string
     * <code>echo "Hello, world."</code>.
     *
     * <p>The command parameter is read only once during fire().
     * If you want to spawn another different command,
     * use life cycle management actors such RunCompositeActor.</p>
     */
    public PortParameter command;

    /** The directory in which to execute the command.
     *  This parameter is read each time the subprocess is started
     *  in fire(). Once the subprocess is running, this parameter is not
     *  read again until fire() is called again.
     *
     *  <p>The initial default value of this parameter $CWD, which
     *  corresponds with the value of the Java virtual machine
     *  user.dir property which is the user's current working
     *  directory.  Note that if we are running inside a menu launched
     *  application, then ptolemy.actor.gui.jnlp.MenuApplication will
     *  change user.dir to be the value of user.home, which is the
     *  name of the user's home directory.</p>
     */
    public FileParameter directory;

    /** The environment in which to execute the command.
     *  This parameter is read each time the subprocess is started
     *  in fire(). Once the subprocess is running, this parameter is not
     *  read again until fire() is called again.
     *
     *  <p>This parameter is an array of records that name an environment
     *  variable and the value for the value.  The format is:</p>
     *  <pre>
     *  {{name = "<i>NAME1</i>", value = "<i>value1</i>"}...}
     *  </pre>
     *  Where <code><i>NAME1</i></code> is the name of the environment
     *  variable, and <code><i>value1</i></code> is the value.
     *  <p>For example <code>{{name = "PTII", value = "c:/ptII"}}</code>
     *  would set the value of the <code>PTII</code> to <code>c:/ptII</code>.</p>
     *
     *  <p>If the initial value of the parameter is <code>{{name="",
     *  value = ""}}</code>, then the environment from the calling or parent
     *  process is used in the new command.</p>
     *
     *  <p>Note that if this parameter sets any environment variable,
     *  then under Windows the other environment variables in the calling
     *  or parent process might not be passed to the subprocess.  This
     *  behaviour could be platform or JVM dependent. When in doubt,
     *  try setting the <i>command</i> value to "env" to print out the
     *  environment.</p>
     */
    public Parameter environment;

    /** Data that is generated by the subprocess on its standard
     *  error.  While the process is running, any error data generated
     *  by the subprocess is stored until the subprocess exits and
     *  then the stored error data is sent to the <i>error</i> port.
     *  If the subprocess generates no data on standard error, then
     *  the empty string (a string of length zero) is generated.
     *  This port is an output port of type String.
     */
    public TypedIOPort error;

    /** The exit code of the subprocess. Usually, a non-zero exit code
     *  indicate that the subprocess had a problem.  This port is an output
     *  port of type int.
     */
    public TypedIOPort exitCode;

    /** If true, ignore IOException errors from the subprocess.
     *  The initial default value is false, indicating that
     *  read errors are not ignored.
     */
    public Parameter ignoreIOExceptionReadErrors;

    /** Strings to pass to the standard input of the subprocess.
     *  Note that a newline is not appended to the string.  If you
     *  require a newline, add one using the AddSubtract actor.
     *  This port is an input port of type String.
     */
    public TypedIOPort input;

    /** Data that is generated by the subprocess on standard out.
     *  While the process is running, any output data generated
     *  by the subprocess is stored until the subprocess exits and
     *  then the stored output data is sent to the <i>output</i> port.
     *  If the subprocess generates no data on standard out, then
     *  the empty string (a string of length zero) is generated.
     *  This port is an output port of type String.
     */
    // NOTE: output port is inherited from parent class.
    //public TypedIOPort output;

    /** If true, then prepend the platform dependent shell command
     *  to the parsed value of the command parameter.
     *  By setting this argument to true, it is possible to invoke
     *  commands in a platform neutral method.
     *  <p>Under Windows NT or XP, the arguments "cmd.exe" and "/C"
     *  are prepended.  Under Windows 95, the arguments "command.com"
     *  and "/C" are prepended.  Under all other platforms, the
     *  arguments "/bin/sh" and "-c" are prepended.
     *  <p>By prepending sh or cmd, then this actor can use the
     *  file redirection operations.
     *  <p>The default value of this parameter is a boolean of value
     *  false, which allows the user to arbitrarily invoke /bin/sh
     *  scripts on all platforms.
     */
    public Parameter prependPlatformDependentShellCommand;

    /** If true, then throw an exception if the subprocess returns
     *  non-zero.
     *  The default is a boolean of value true.
     *  This parameter is ignored if <i>waitForProcess</i> is false.
     */
    public Parameter throwExceptionOnNonZeroReturn;

    /** If true, then actor will wait until subprocess completes. The
     *  default is a boolean of value true.
     */
    public Parameter waitForProcess;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Invoke a subprocess, read the <i>input</i> data (if any) and
     *  wait for the subprocess to terminate before sending any output
     *  or error data to the appropriate ports.
     *
     *  <p>If there is no data on the <i>input</i> port, then the
     *  subprocess executes without reading any input. If there is no
     *  output or error data from the subprocess, then the empty
     *  string is sent to the appropriate port(s).</p>
     *
     *  @exception IllegalActionException If the subprocess cannot be
     *  started, if the input of the subprocess cannot be written,
     *  if the subprocess gets interrupted, or if the return value
     *  of the process is non-zero.
     */
    @Override
    public void fire() throws IllegalActionException {
        // NOTE: This used to be synchronized, but this causes a
        // deadlock with the UI when parameters are edited while
        // model is running.
        super.fire();

        String line = null;

        _exec();

        if (input.numberOfSources() > 0 && input.hasToken(0)) {
            if ((line = ((StringToken) input.get(0)).stringValue()) != null) {
                if (_debugging) {
                    _debug("Exec: Input: '" + line + "'");
                }

                if (_inputBufferedWriter != null) {
                    try {
                        _inputBufferedWriter.write(line);
                        _inputBufferedWriter.flush();
                    } catch (IOException ex) {
                        throw new IllegalActionException(this, ex,
                                "Problem writing input '" + command + "'");
                    }
                }
            }
        }

        boolean alreadySentOutput = false;

        try {

            // Close the stdin of the subprocess.
            _process.getOutputStream().close();

            boolean waitForProcessValue = ((BooleanToken) waitForProcess
                    .getToken()).booleanValue();

            if (waitForProcessValue) {
                // The next line waits for the subprocess to finish.
                int processReturnCode = _process.waitFor();

                if (processReturnCode != 0) {
                    // We could have a parameter that would enable
                    // or disable this.
                    String outputString = "";
                    String errorString = "";

                    try {
                        errorString = _errorGobbler.getAndReset();
                    } catch (Exception ex) {
                        errorString = ex.toString();
                    }

                    try {
                        outputString = _outputGobbler.getAndReset();
                    } catch (Exception ex) {
                        outputString = ex.toString();
                    }

                    boolean throwExceptionOnNonZeroReturnValue = ((BooleanToken) throwExceptionOnNonZeroReturn
                            .getToken()).booleanValue();

                    if (throwExceptionOnNonZeroReturnValue) {
                        throw new IllegalActionException(
                                this,
                                "Executing command \""
                                        + ((StringToken) command.getToken())
                                                .stringValue()
                                        + "\" returned a non-zero return value of "
                                        + processReturnCode
                                        + ".\nThe last input was: " + line
                                        + ".\nThe standard output was: "
                                        + outputString
                                        + "\nThe error output was: "
                                        + errorString);
                    } else {
                        error.send(0, new StringToken(errorString));
                        output.send(0, new StringToken(outputString));
                        alreadySentOutput = true;
                    }
                }

                exitCode.send(0, new IntToken(processReturnCode));

            }
        } catch (InterruptedException interrupted) {
            throw new InternalErrorException(this, interrupted,
                    "_process.waitFor() was interrupted");
        } catch (IOException io) {
            throw new IllegalActionException(this, io,
                    "Closing stdin of the subprocess threw an IOException.");

        }

        // if we sent output when the return value was non-zero, do not send
        // additional output.
        // see http://bugzilla.ecoinformatics.org/show_bug.cgi?id=4326
        if (!alreadySentOutput) {
            String outputString = _outputGobbler.getAndReset();
            String errorString = _errorGobbler.getAndReset();

            if (_debugging) {
                _debug("Exec: Error: '" + errorString + "'");
                _debug("Exec: Output: '" + outputString + "'");
            }

            // We could have a parameter that if it was set
            // we would throw an exception if there was any error data.
            error.send(0, new StringToken(errorString));
            output.send(0, new StringToken(outputString));
        }
    }

    /** Override the base class and terminate the process.
     */
    @Override
    public void stop() {
        // NOTE: This method used to be synchronized, as
        // was the fire() method, but this caused deadlocks.  EAL
        super.stop();
        _terminateProcess();

    }

    /** Override the base class to stop waiting for input data.
     */
    @Override
    public void stopFire() {
        // NOTE: This method used to be synchronized, as
        // was the fire() method, but this caused deadlocks.  EAL
        super.stopFire();
        _stopFireRequested = true;
        _terminateProcess();
    }

    /** Terminate the subprocess.
     *  This method is invoked exactly once per execution
     *  of an application.  None of the other action methods should be
     *  be invoked after it.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        _terminateProcess();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Execute a command, set _process to point to the subprocess
    // and set up _errorGobbler and _outputGobbler to read data.
    private void _exec() throws IllegalActionException {
        // FIXME: Exec, KeyStoreActor, JTextAreaExec have duplicate code.
        // This is a private method because fire() was getting too long.
        File directoryAsFile = null;
        try {
            _stopFireRequested = false;

            if (_process != null) {
                // Note that we assume that _process is null upon entry
                // to this method, but we check again here just to be sure.
                _terminateProcess();
            }

            Runtime runtime = Runtime.getRuntime();

            command.update();

            List<String> commandList = new LinkedList<String>();

            boolean prependPlatformDependentShellCommandValue = ((BooleanToken) prependPlatformDependentShellCommand
                    .getToken()).booleanValue();
            if (prependPlatformDependentShellCommandValue) {
                commandList = _getCommandList();
            }

            // tokenizeForExec() handles substrings that start and end
            // with a double quote so the substring is considered to
            // be a single token and are returned as a single array
            // element.
            // FIXME: tokenizeForExec should return a List<String>
            String[] commandArray = StringUtilities
                    .tokenizeForExec(((StringToken) command.getToken())
                            .stringValue());
            commandList.addAll(Arrays.asList(commandArray));

            directoryAsFile = directory.asFile();
            if (!directoryAsFile.isDirectory()) {
                throw new IllegalActionException("No such directory: "
                        + directoryAsFile);
            }

            if (_debugging) {
                StringBuffer commands = new StringBuffer();
                for (String aCommand : commandList) {
                    commands.append(aCommand + " ");
                }
                _debug("About to exec \"" + commands + "\"\n in \""
                        + directoryAsFile + "\"\n with environment:");
            }

            // Process the environment parameter.
            ArrayToken environmentTokens = (ArrayToken) environment.getToken();

            if (_debugging) {
                _debug("environmentTokens: " + environmentTokens);
            }

            String[] environmentArray = null;

            if (environmentTokens.length() >= 1) {
                environmentArray = new String[environmentTokens.length()];

                for (int i = 0; i < environmentTokens.length(); i++) {
                    StringToken nameToken = (StringToken) ((RecordToken) environmentTokens
                            .getElement(i)).get("name");
                    StringToken valueToken = (StringToken) ((RecordToken) environmentTokens
                            .getElement(i)).get("value");
                    environmentArray[i] = nameToken.stringValue() + "="
                            + valueToken.stringValue();

                    if (_debugging) {
                        _debug("  " + i + ". \"" + environmentArray[i] + "\"");
                    }

                    if (i == 0 && environmentTokens.length() == 1
                            && environmentArray[0].equals("=")) {
                        if (_debugging) {
                            _debug("There is only one element, "
                                    + "it is a string of length 0,\n so we "
                                    + "pass Runtime.exec() an null "
                                    + "environment so that we use\n "
                                    + "the default environment");
                        }

                        environmentArray = null;
                    }
                }
            }

            commandArray = commandList.toArray(new String[commandList.size()]);
            _process = runtime.exec(commandArray, environmentArray,
                    directoryAsFile);

            // Create two threads to read from the subprocess.
            _outputGobbler = new _StreamReaderThread(_process.getInputStream(),
                    "Exec Stdout Gobbler-" + _streamReaderThreadCount++, this);
            _errorGobbler = new _StreamReaderThread(_process.getErrorStream(),
                    "Exec Stderr Gobbler-" + _streamReaderThreadCount++, this);
            _errorGobbler.start();
            _outputGobbler.start();

            if (_streamReaderThreadCount > 1000) {
                // Avoid overflow in the thread count.
                _streamReaderThreadCount = 0;
            }

            OutputStreamWriter inputStreamWriter = new OutputStreamWriter(
                    _process.getOutputStream());
            _inputBufferedWriter = new BufferedWriter(inputStreamWriter);
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Problem executing the command '" + command.getExpression()
                            + "'\n" + "in the directory: " + directoryAsFile);
        }
    }

    /** Get the command list arguments for exec. */
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

    // Terminate the process and close any associated streams.
    private void _terminateProcess() {
        if (_process != null) {
            _process.destroy();
            _process = null;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    // Private class that reads a stream in a thread and updates the
    // stringBuffer.
    private class _StreamReaderThread extends Thread {
        /** Create a _StreamReaderThread.
         *  @param inputStream The stream to read from.
         *  @param name The name of this StreamReaderThread,
         *  which is useful for debugging.
         *  @param actor The parent actor of this thread, which
         *  is used in error messages.
         */
        _StreamReaderThread(InputStream inputStream, String name, Nameable actor) {
            super(name);
            _inputStream = inputStream;
            _inputStreamReader = new InputStreamReader(_inputStream);
            _actor = actor;
            _stringBuffer = new StringBuffer();
        }

        /** Read any remaining data in the input stream and return the
         *  data read thus far.  Calling this method resets the
         *  cache of data read thus far.
         */
        public String getAndReset() {
            if (_debugging) {
                try {
                    _debug("getAndReset: Gobbler '" + getName() + "' Ready: "
                            + _inputStreamReader.ready() + " Available: "
                            + _inputStream.available());
                } catch (Exception ex) {
                    throw new InternalErrorException(ex);
                }
            }

            try {
                // Read any remaining data.
                _read();
            } catch (Throwable throwable) {
                if (_debugging) {
                    _debug("WARNING: getAndReset(): _read() threw an "
                            + "exception, which we are ignoring.\n"
                            + throwable.getMessage());
                }
            }

            String results = _stringBuffer.toString();
            _stringBuffer = new StringBuffer();

            try {
                _inputStreamReader.close();
                _inputStreamReaderClosed = true;
            } catch (Exception ex) {
                throw new InternalErrorException(null, ex, getName()
                        + " failed to close.");
            }

            return results;
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

        // Read from the stream until we get to the end of the stream
        // This is synchronized so that it is not called simultaneously
        // from run() and getAndReset().
        private synchronized void _read() {
            // We read the data as a char[] instead of using readline()
            // so that we can get strings that do not end in end of
            // line chars.
            char[] chars = new char[80];
            int length; // Number of characters read.

            try {
                // Oddly, InputStreamReader.read() will return -1
                // if there is no data present, but the string can still
                // read.
                while ((length = _inputStreamReader.read(chars, 0, 80)) != -1
                        && !_stopRequested && !_stopFireRequested) {
                    if (_debugging) {
                        // Note that ready might be false here since
                        // we already read the data.
                        _debug("_read(): Gobbler '" + getName() + "' Ready: "
                                + _inputStreamReader.ready() + " Value: '"
                                + String.valueOf(chars, 0, length) + "'");
                    }

                    _stringBuffer.append(chars, 0, length);
                }
            } catch (Throwable throwable) {
                // We set ignoreExceptionReadErrors to true for ExecDemos so that
                // exporting html does not produce an error when the model exits after 30 seconds.
                boolean ignoreIOExceptionReadErrorsValue = false;

                try {
                    ignoreIOExceptionReadErrorsValue = ((BooleanToken) ignoreIOExceptionReadErrors
                            .getToken()).booleanValue();

                } catch (IllegalActionException ex) {
                    throw new InternalErrorException(
                            _actor,
                            ex,
                            getName()
                                    + ": Could not get the value of the ignoreIOExceptionReadErrors "
                                    + "parameter while trying to throw "
                                    + throwable);
                }
                if (ignoreIOExceptionReadErrorsValue
                        && throwable instanceof IOException) {
                    new Exception(
                            "Warning: "
                                    + getFullName()
                                    + " had an exception, but "
                                    + "ignoreIOExceptionReadErrors was true and the exception was an "
                                    + "IOException, so it is being skipped.",
                            throwable).printStackTrace();
                } else {
                    throw new InternalErrorException(
                            _actor,
                            throwable,
                            getName()
                                    + ": Failed while reading from "
                                    + _inputStream
                                    + ". To avoid this, try setting the ignoreIOExceptionReadErrors parameter to true."
                                    + throwable.getCause());
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
    // The subprocess gets its input from this BufferedWriter.
    private BufferedWriter _inputBufferedWriter;

    // StreamReader with which we read stderr.
    private _StreamReaderThread _errorGobbler;

    // StreamReader with which we read stdout.
    private _StreamReaderThread _outputGobbler;

    // The Process that we are running.
    private Process _process;

    // Indicator that stopFire() has been called.
    private boolean _stopFireRequested = false;

    // Instance count of output and error threads, used for debugging.
    // When the value is greater than 1000, we reset it to 0.
    private static int _streamReaderThreadCount = 0;
}
