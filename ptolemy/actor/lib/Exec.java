/* Execute a command in a subprocess.

 Copyright (c) 2004 The Regents of the University of California.
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

@ProposedRating Yellow (cxh@eecs.berkeley.edu) 2/5/04
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.ArrayType;
import ptolemy.data.ArrayToken;
import ptolemy.data.StringToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Settable;
import ptolemy.util.StringUtilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// Execute
/**
Execute a command as a separately running subprocess.

<p>This actor uses java.lang.Runtime.exec() to invoke a subprocess
named be the <i>command</i> parameter in a <i>directory</i> with an
<i>environment</i>.  Data from the <i>input</i> port (if any) is
passed to the input of the subprocess.  The subprocess is run until it
exits and then contents of the output and error streams of the
subprocess (if any) are passed to the <i>output</i> and <i>error</i>
ports.

<p>If the subprocess generates no data on the output or error stream,
then the data on the corresponding port(s) will consist of the empty string.

<p>A much more interesting actor could be written using a
Kahn Process Network.  This actor would generate output asynchronously
as the process was executing.

<p>Currently, there appears to be no way to get the subprocess to
exit by passing it input. For example, if the <i>command</i> is set
to the <code>cat</code> command, and we pass in a Const with the
value <code>\04</code>, then the cat subprocess does <b>not</b> interpret
this as the end of file marker and exit.

<p>For information about Runtime.exec(), see:
<a href="http://jw.itworld.com/javaworld/jw-12-2000/jw-1229-traps.html" target="_top">http://jw.itworld.com/javaworld/jw-12-2000/jw-1229-traps.html</a>
and
<a href="http://mindprod.com/jgloss/exec.html" target="_top">http://mindprod.com/jgloss/exec.html</a>

@author Christopher Hylands Brooks, Contributor: Edward A. Lee
@version $Id$
@since Ptolemy II 3.1
*/
public class Exec extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Exec(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        command = new PortParameter(this, "command",
                new StringToken("echo 'Hello, world.'"));
        // Make command be a StringParameter (no surrounding double quotes).
        command.setStringMode(true);
                
        directory = new FileParameter(this, "directory");
        directory.setExpression("$CWD");
        // Hide the directory parameter.
        directory.setVisibility(Settable.EXPERT);

        environment = new Parameter(this, "environment");
        String [] labels = new String [] {"name", "value"};
        Type [] values = new Type [] {BaseType.STRING, BaseType.STRING};
            
        // An array of records {{name="", value=""}}
        environment.setTypeEquals(
                new ArrayType(new RecordType(labels, values)));

        // Array with an empty name and value means
        // default environment of the calling process.
        environment.setExpression("{{name=\"\", value=\"\"}}");
        // Hide the environment parameter.
        environment.setVisibility(Settable.EXPERT);

        error = new TypedIOPort(this, "error", false, true);
        error.setTypeEquals(BaseType.STRING);

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.STRING);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.STRING);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The command to be executed.  The command is parsed by
     * {@link ptolemy.util.StringUtilities#tokenizeForExec(String)}
     * into tokens and then executed as a separate subprocess.
     * The initial default value is the string "echo 'Hello, world.'".
     *
     * <p>The command parameter is read only once during fire().
     * If you want to spawn another different command,
     * use life cycle management actors such RunCompositeActor.
     */
    public PortParameter command;

    /** The directory in which to execute the command. 
     *   
     *  <p> This parameter is an Expert mode parameter, so it is
     *  usually hidden.  To edit it, right click on the actor, select
     *  'Configure', then hit the 'Preferences' button and select
     *  'Expert Mode'.
     *   
     *  <p>This parameter is read each time the subprocess is started
     *  in fire(). Once the subprocess is running, this parameter is not
     *  read again until fire() is called again.
     *
     *  <p>The initial default value of this parameter $CWD, which
     *  corresponds with the value of the Java virtual machine
     *  user.dir property which is the user's current working
     *  directory.  Note that if we are running inside a menu launched
     *  application, then ptolemy.actor.gui.jnlp.MenuApplication will
     *  change user.dir to be the value of user.home, which is the
     *  name of the user's home directory.  
     */
    public FileParameter directory;

    /** The environment in which to execute the command.
     *   
     *  <p> This parameter is an Expert mode parameter, so it is
     *  usually hidden.  To edit it, right click on the actor, select
     *  'Configure', then hit the 'Preferences' button and select
     *  'Expert Mode'.
     *   
     *  <p>This parameter is read each time the subprocess is started
     *  in fire(). Once the subprocess is running, this parameter is not
     *  read again until fire() is called again.
     *
     *  <p>This parameter is an array of records that name an environment
     *  variable and the value for the value.  The format is:
     *  <pre>
     *  {{name="<i>NAME1</i>", value="</i>value1</i>"}...}
     *  </pre>
     *  Where <code><i>NAME1</i></code> is the name of the environment
     *  variable, and <code><i>value1</i></code> is the value.
     *  <p>For example <code>{{name="PTII", value="c:/ptII"}}</code>
     *  would set the value of the <code>PTII</code> to <code>c:/ptII</code>.
     *
     *  <p>If the initial value of the parameter is <code>{{name="",
     *  value=""}}</code>, then the environment from the calling or parent
     *  process is used in the new command.
     *
     *  <p>Note that if this parameter sets any environment variable,
     *  then under Windows the other environment variables in the calling
     *  or parent process might not be passed to the subprocess.  This
     *  behaviour could be platform or JVM dependent. When in doubt,
     *  try setting the <i>command</i> value to "env" to print out the
     *  environment.
     */
    public Parameter environment;

    /** Data that is generated by the subprocess on standard error.
     *  If the subprocess generates no data on standard error, then
     *  the empty string (a string of length zero) is generated.
     *  <p>The port is an output port of type String.
     */
    public TypedIOPort error;

    /** Strings to pass to the standard in of the subprocess.
     *  Note that a newline is not appended to the string.  If you
     *  require a newline, add one using the AddSubtract actor.
     *  <p>This port is an input port of type String.
     */
    public TypedIOPort input;

    /** Data that is generated by the subprocess on standard out.
     *  If the subprocess generates no data on standard out, then
     *  the empty string (a string of length zero) is generated.
     *  <p>The port is an output port of type String.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////


    /** Invoke a subprocess and wait for it to terminate before
     *  sending any output or error data.
     *  <p>Start up the subprocess, send the input (if any) to the
     *  subprocess, read output and error data from the subprocess,
     *  wait for the subprocess to terminate and then send
     *  the output and error data on the <i>output</i> and <i>error</i>
     *  ports.  If there is no output or error data, then the empty
     *  string is sent.
     *  @exception IllegalActionException If the subprocess cannot be
     *  started, if the input of the subprocess cannot be written, 
     *  if the subprocess gets interrupted, or if the return value
     *  of the process is non-zero.
     */
    public synchronized void fire() throws IllegalActionException {
        super.fire();
        String line = null;

        _exec();
        if (input.numberOfSources() > 0
                && input.hasToken(0)) {
            if ((line = ((StringToken)input.get(0)).stringValue())
                    != null) {
                if (_debugging) {
                    _debug("Exec: Input: '" + line + "'");
                }
                if (_inputBufferedWriter != null) {
                    try {
                        _inputBufferedWriter.write(line);
                        _inputBufferedWriter.flush();
                    } catch (IOException ex) {
                        throw new IllegalActionException(this, ex,
                                "Problem writing input '"
                                + command + "'");
                    }
                }
            }
        }

        String errorString;
        String outputString;

        try {
            // The next line waits for the subprocess to finish.
            int processReturnCode = _process.waitFor();
            //synchronized(this) {
            //    _process = null;
            //}
            
            if (processReturnCode != 0) {
                // We could have a parameter that would enable
                // or disable this.
                throw new IllegalActionException(this,
                        "Executing command \""
                        + ((StringToken)command.getToken()).stringValue()
                        + "\" returned a non-zero return value of " 
                        + processReturnCode);
            }

        } catch (InterruptedException interrupted) {
            throw new InternalErrorException(this, interrupted,
                    "_process.waitFor() was interrupted");
        }

        outputString = _outputGobbler.getAndReset();
        errorString = _errorGobbler.getAndReset();
        
        if (_debugging) {
            _debug("Exec: Error: '" + errorString + "'");
            _debug("Exec: Output: '" + outputString + "'");
        }

        // We could have a parameter that if it was set
        // we would throw an exception if there was any error data.
        error.send(0, new StringToken(errorString));
        output.send(0, new StringToken(outputString));
    }

    /** Override the base class and terminate the process.
     */
    public /*synchronized*/ void stop() {
        // This method is not synchronized because when the user
        // hits the stop button, the fire() method is probably
        // waiting for the process to terminate.

        // See ptolemy.actor.io.comm.SerialComm for a similar situation.
        super.stop();
        try {
            _terminateProcess();
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex); 
        }
    }

    /** Override the base class to stop waiting for input data.
     */
    public /*synchronized*/ void stopFire() {
        // See the comment in stop() for why this is _not_ synchronized
        super.stopFire();
        _stopFireRequested = true;
        try {
            _terminateProcess();
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex); 
        }
    }

    /** Terminate the subprocess.
     *  This method is invoked exactly once per execution
     *  of an application.  None of the other action methods should be
     *  be invoked after it.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void wrapup() throws IllegalActionException {
        _terminateProcess();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private methods                       ////

    // Execute a command, set _process to point to the subprocess
    // and set up _errorGobbler and _outputGobbler to read data.
    private void _exec() throws IllegalActionException {
        // This is a private method because fire() was getting too long.
        try {
            _stopFireRequested = false;

            if (_process != null) {
                // Note that we assume that _process is null upon entry
                // to this method, but we check again here just to be sure.
                _terminateProcess();
            }

            Runtime runtime = Runtime.getRuntime();

            command.update();

            // tokenizeForExec() handles substrings that start and end
            // with a double quote so the substring is considered to
            // be a single token and are returned as a single array
            // element.
            String [] commandArray = StringUtilities.tokenizeForExec(
                    ((StringToken)command.getToken()).stringValue());

            File directoryAsFile = directory.asFile();

            if (_debugging) {
                _debug("About to exec \""
                        + ((StringToken)command.getToken()).stringValue()
                        + "\""
                        + "\n in \"" + directoryAsFile
                        + "\"\n with environment:");
            }

            // Process the environment parameter.
            ArrayToken environmentTokens = (ArrayToken)environment.getToken();

            if (_debugging) {
                _debug("environmentTokens: " + environmentTokens);
            }
            String [] environmentArray = null;
            if (environmentTokens.length() >= 1) {
                environmentArray = new String[environmentTokens.length()];
                for (int i = 0; i < environmentTokens.length(); i++) {
                    StringToken nameToken= (StringToken)
                        (((RecordToken) environmentTokens.getElement(i))
                        .get("name"));
                    StringToken valueToken= (StringToken)
                        (((RecordToken) environmentTokens.getElement(i))
                        .get("value"));
                    environmentArray[i] = nameToken.stringValue()
                        + "=" + valueToken.stringValue();

                    if (_debugging) {
                        _debug("  " + i + ". \"" + environmentArray[i]
                                + "\"");
                    }

                    if ( i == 0 && environmentTokens.length() == 1
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


            _process = runtime.exec(commandArray, environmentArray,
                    directoryAsFile);

            // Create two threads to read from the subprocess.
            _outputGobbler =
                new _StreamReaderThread(_process.getInputStream(),
                        "Exec Stdout Gobbler-" + _streamReaderThreadCount++,
                        this);
            _errorGobbler =
                new _StreamReaderThread(_process.getErrorStream(),
                        "Exec Stderr Gobbler-" +  _streamReaderThreadCount++,
                        this);
            _errorGobbler.start();
            _outputGobbler.start();

            if (_streamReaderThreadCount > 1000) {
                // Avoid overflow in the thread count.
                _streamReaderThreadCount = 0;
            }

            OutputStreamWriter inputStreamWriter =
                new OutputStreamWriter(_process.getOutputStream());
            _inputBufferedWriter = new BufferedWriter(inputStreamWriter);
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Problem setting up command '" + command + "'");
        }
    }


    // Terminate the process and close any associated streams.
    private void _terminateProcess() throws IllegalActionException {
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
        _StreamReaderThread(InputStream inputStream, String name,
                Nameable actor) {
            super(name);
            _inputStream = inputStream;
            _actor = actor;
            _stringBuffer = new StringBuffer();
        }

        /** Get the current value of the stringBuffer and empty
         *  the contents of the StringBuffer.
         */
        public String getAndReset() {
            String results = _stringBuffer.toString();
            _stringBuffer = new StringBuffer();
            return results;
        }

        /** Read lines from the inputStream and append them to the
         *  stringBuffer.
         */
        public void run() {
            try {
                InputStreamReader inputStreamReader =
                    new InputStreamReader(_inputStream);
                // We read the data as a char[] instead of using readline()
                // so that we can get strings that do not end in end of
                // line chars.
                char [] chars = new char[80];
                int length; // Number of characters read.

                while ((length = inputStreamReader.read(chars, 0, 80))
                        != -1
                        && !_stopRequested
                        && !_stopFireRequested
                       ) {
                    if (_debugging) {
                        _debug("Gobbler: Ready: " + inputStreamReader.ready()
                                       + String.valueOf(chars, 0, length));
                    }
                    // If the inputStream is not ready, then call notifyAll().
                    _stringBuffer.append(chars, 0, length);
                }
            } catch (Throwable throwable) {
                throw new InternalErrorException(_actor, throwable,
                        "Failed while reading from " + _inputStream);
            }
        }

        // The actor associated with this stream reader.
        private Nameable _actor;

        // StringBuffer to update.
        private StringBuffer _stringBuffer;

        // Stream from which to read.
        private InputStream _inputStream;
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
